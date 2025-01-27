# MediaPipe学习笔记六（Real-time Streams）

## **Real-time timestamps**

MediaPipe calculator graphs 经常被用在处理音频视频帧的处理streams在交互App上。MediaPipe 框架将连续的packets设计成连续递增的时间戳。一般情况下，实时的calculator和graph使用每个帧的录制时间或者演示时间作为时间戳（从Jan/1/1970:00:00:00之后的毫秒值）。这就表明允许来自各种各样源数据的packet能在全局一致的顺序执行。

## **Real-time scheduling**

一般情况下，每个Calculator在它给定时间戳的所有的输入packet都可用时立即执行。通常，calculator的执行发生在Calculator完成前一帧的处理，并且其输入的每个Calculator都已完成当前帧的处理的时候。一旦满足这些条件，MediaPipe [调度程序](https://zhida.zhihu.com/search?content_id=220109443&content_type=Article&match_order=1&q=调度程序&zhida_source=entity)就会调用每个Calculator。

## **Timestamp bounds**

当一个calculator没有产生任何带有时间戳的输出packets时，它可能反而输出一个“timestamp bound”表明在该时间戳没有packet将生成。这时非常必要的允许下游的calculator即使对于该时间戳的某些流没有packet到达也可以在此时间戳期间运行。这对于[交互式应用程序](https://zhida.zhihu.com/search?content_id=220109443&content_type=Article&match_order=1&q=交互式应用程序&zhida_source=entity)中的实时Graph尤为重要，因为每个calculator都必须尽快开始处理。

如下所示：

```cpp
node {
   calculator: "A"
   input_stream: "alpha_in"
   output_stream: "alpha"
}
node {
   calculator: "B"
   input_stream: "alpha"
   input_stream: "foo"
   output_stream: "beta"
}

```

假设：在时间戳`T`，node `A`没有发送一个packet在它的输出流`alpha`。node `B`获得一个带时间戳`T`的`foo` packet并且等待 带时间戳`T`的`alpha` packet.如果`A`无法发送给`B` `alpha`的 `timestamp bound` ，`B`将一直等待一个来自于`alpha`的packet。同时，`foo`的packet 队列会在`T`，`T+1`等时间累积packets。

想要在stream上输出packet，calculator使用函数`CalculatorContext::Outputs`和`OutputStream::Add`。要改变stream输出的 `timestamp bound`，calculator使用函数`CalculatorContext::Outputs`和`CalculatorContext::SetNextTimestampBound`。指定的边界（bound）是指定输出流上下一个packet的最低允许时间戳。当没有packet输出时，calculator将做以下事情：

```cpp
cc->Outputs().Tag("output_frame").SetNextTimestampBound(
  cc->InputTimestamp().NextAllowedInStream());
```

`Timestamp::NextAllowedInStream` 函数返回连续的时间戳。例如，`Timestamp(1).NextAllowedInStream() == Timestamp(2)`.

## **Propagating timestamp bounds**

为了下游的calculator及时调度，在实时graph中的calculator需要根据输入时间戳范围（timestamp bounds）定义输出时间戳绑定。一个常见的模型是calculator输出具有与输入packet相同时间戳的packet。在这种情况下，只需在每次调用 `Calculator::Process` 时输出一个packet就足以定义输出时间戳范围（timestamp bound）。

然而，calculator不需要遵循这种常见的输出时间戳模式，它们只需要选择[单调递增](https://zhida.zhihu.com/search?content_id=220109443&content_type=Article&match_order=1&q=单调递增&zhida_source=entity)的输出时间戳。因此，某些calculator必须显式计算时间戳边界（timestamp bound）。MediaPipe提供很多工具为了计算合适的时间戳边界为了每个calculator。

1. **SetNextTimestampBound()** 可用于指定输出流的时间戳限制（timestamp bound） t + 1

```cpp
cc->Outputs.Tag("OUT").SetNextTimestampBound(t.NextAllowedInStream());
```

或者，可以生成一个带有时间戳 t 的空数据包来指定时间戳绑定 t + 1。

```cpp
cc->Outputs.Tag("OUT").Add(Packet(), t);
```

输入流的时间戳边界由输入流上的数据包或空数据包指示。

```cpp
Timestamp bound = cc->Inputs().Tag("IN").Value().Timestamp();
```

1. **TimestampOffset()** 可以指定以便自动将时间戳边界从输入流复制到输出流。

```cpp
cc->SetTimestampOffset(0);
```

此设置具有自动传播时间戳边界的优点，即使只有时间戳边界到达并且未调用 Calculator::Process 也是如此。

1. **ProcessTimestampBounds()**可以指定以便为每个新的“已结算时间戳”调用 `Calculator::Process`，其中“已结算时间戳”是当前时间戳范围以下的新的最高时间戳。如果没有 `ProcessTimestampBounds()`，`Calculator::Process` 仅在一个或多个到达数据包时被调用。

```cpp
cc->SetProcessTimestampBounds(true);
```

此设置允许`Calculator`执行自己的时间戳边界计算和传播，即使仅更新输入时间戳也是如此。它可用于复制 `TimestampOffset()` 的效果，但也可用于计算考虑了其他因素的时间戳界限。

例如，为了复制 SetTimestampOffset(0)，calculator可以执行以下操作：

```cpp
absl::Status Open(CalculatorContext* cc) {
  cc->SetProcessTimestampBounds(true);
}

absl::Status Process(CalculatorContext* cc) {
  cc->Outputs.Tag("OUT").SetNextTimestampBound(
      cc->InputTimestamp().NextAllowedInStream());
}
```

## **Scheduling of Calculator::Open and Calculator::Close**

Calculator::Open 是在当所有符合要求的输入包已经完成时调用的。输入侧包可以由封闭的应用程序或graph的“side-packet calculators”提供。可以使用 API 的 `CalculatorGraph::Initialize` 和 `CalculatorGraph::StartRun` 从graph外部指定Side packets 。[calculators](https://zhida.zhihu.com/search?content_id=220109443&content_type=Article&match_order=2&q=calculators&zhida_source=entity)可以使用 `CalculatorGraphConfig::OutputSidePackets` 和 `OutputSidePacket::Set` 指定Side packets 。

当所有输入流通过关闭或达到时间戳绑定 `Timestamp::Done` 成为完成时，将调用 `Calculator::Close`。

注意：如果graph完成所有未决的calculators执行并变为完成，则在某些流变为完成之前，MediaPipe 将调用剩余的对 Calculator::Close 的调用，以便每个calculators都可以产生其最终输出。

TimestampOffset 的使用对 `Calculator::Close` 有一些影响。指定 `SetTimestampOffset(0)` 的calculator将通过设计发出信号，表明当其所有输入流都已变成 `Timestamp::Done` 时，其所有输出流均已变成`Timestamp::Done`，因此不可能有进一步的输出。这可以防止此类calculator在 `Calculator::Close` 期间发出任何数据包。如果calculator需要在 `Calculator::Close` 期间生成摘要数据包，则 `Calculator::Process` 必须指定时间戳边界（`timestamp bounds`），以便至少有一个时间戳（例如 `Timestamp::Max`）在 `Calculator::Close` 期间保持可用。这意味着此类calculator通常不能依赖 `SetTimestampOffset(0)`，而必须使用 `SetNextTimestampBounds()` 显式指定时间戳范围。

