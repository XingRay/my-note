# MediaPipe学习笔记四（Synchronization）

## **Synchronization（同步）**

Synchronization是MediaPipe非常重要的机制，保证了calculator 同步执行。

## **Scheduling mechanics（调度机制）**

MediaPipe graph中的数据处理发生在定义为 CalculatorBase 子类的处理节点内。调度系统决定每个calculator何时运行。

每个graph至少有一个[调度程序](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=调度程序&zhida_source=entity)队列（**scheduler queue**）。每个调度器队列只有一个执行器（**executor**）。Nodes 静态分配给队列（因此分配了一个执行器）。默认情况下有一个队列，其执行者是一个[线程池](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=线程池&zhida_source=entity)，其中包含基于系统能力的多个线程。

每个nodes都有一个调度状态，可以是未就绪(*not ready*)、就绪(*ready*)、正在运行(*running*)。A readiness function确定node是否准备运行，每当node运行挖成时以及node输入状态发生改变时，都会在graph初始化时调用此函数。

The readiness function的使用取决于节点的类型，一个node没有输入流被称为[源节点](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=源节点&zhida_source=entity)（**source node**）；源节点始终准备运行直到他们告诉框架他们没有更多的数据可以输出，此时他们被关闭。

如果非源节点（Non-source nodes）有输入要处理，或者如果这些输入根据节点的输入策略（ **input policy** ）（下面讨论）设置的条件形成有效的[输入集](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=输入集&zhida_source=entity)时非源节点（Non-source nodes）成就绪状态。设置的大多数节点使用默认输入策略，但有些节点指定不同的输入策略。

注意：因为更改输入策略会改变calculator的代码对其输入的预期保证，所以通常不可能混合和匹配具有任意输入策略的calculator。 因此，应该为它编写一个使用特殊输入策略的calculator，并声明它。

当一个node变成就绪状态时，一个任务被添加到对应的[调度队列](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=调度队列&zhida_source=entity)中，这个队列有优先级。[优先级函数](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=优先级函数&zhida_source=entity)目前是固定的，并考虑了节点的静态属性及其在图中的[拓扑排序](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=拓扑排序&zhida_source=entity)。例如，靠近graph输出端的节点具有更高的优先级，而源节点具有最低的优先级。

每个队列都由一个执行器提供服务，它负责通过调用calculator的代码来实际运行任务。 可以提供和配置不同的执行器； 这可用于自定义执行资源的使用，例如 通过在[低优先级线程](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=低优先级线程&zhida_source=entity)上运行某些节点。

## **Timestamp Synchronization（时间戳同步）**

MediaPipe graph 的执行是分散的：没有全局时钟，不同节点可以同时处理来自不同时间戳的数据。

然而，时间信息对于很多感知工作流程是非常重要的，需要注意的是很多输入流是需要协同完成某项工作的。例如，一个检测对象可能输出帧的矩形边界列表。这些信息可能输入到一个渲染节点，这时需要跟之前原始的视频帧图片一起处理。

因此，MediaPipe 框架的关键职责之一就是为节点提供输入同步。在框架机制上，时间戳的主要作用是作为同步关键（**synchronization key**）。

此外，MediaPipe 旨在支持确定性操作，这在许多场景（测试、模拟、批处理等）中都很重要，同时允许graph作者在需要时放宽确定性以满足实时约束。

同步和确定性这两个目标是多种设计选择的基础。值得注意的是，推送到给定流中的数据包必须具有[单调递增](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=单调递增&zhida_source=entity)的时间戳：这不仅是许多节点的有用假设，而且[同步逻辑](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=同步逻辑&zhida_source=entity)也依赖于它.每个stream都有一个时间戳绑定(**timestamp bound**),这是流上新的packet允许的最低可能时间戳。当时间戳为 `T` 的数据包到达时，边界自动前进到 `T+1`。这时框架确定不会再有时间戳低于 `T` 的`packet`到达。

## **Input policies（输入策略）**

`Synchronization`在每个`node`通过使用`node`指定的输入策略在本地处理完成的。

默认输入策略由 `DefaultInputStreamHandler` 定义，提供输入的确定性同步，具有以下保证：

- 如果在多个输入流上提供具有相同时间戳的packets，则无论它们的实时到达顺序如何，它们将始终一起处理。
- 输入集按照严格的时间戳升序处理。
- 没有packets被丢弃，并且处理是完全确定的。
- 在上述保证的情况下，节点将尽快准备好处理数据。

注意：这样做的一个重要后果是，如果calculator在输出packet时始终使用当前输入时间戳，则输出将固有地服从单调递增的时间戳要求。

警告：另一方面，不能保证输入packets始终可用于所有streams。如果stream中的时间戳低于时间戳界限，我们就说它是确定的。 换句话说，一旦确定知道该时间戳的输入状态，就会为streams设置时间戳：要么有packet，要么可以确定具有该时间戳的数据包不会到达。

注意：出于这个原因，MediaPipe 还允许流生产者显式地将时间戳边界提前到比最后一个packet更远，即提供更紧密的边界。这可以让下游节点更快地解决他们的输入。

如果时间戳是在每个流上确定的，那么它就是跨多个流确定的。此外，如果一个时间戳确定，则意味着所有先前的时间戳也已经确定。因此，确定的时间戳可以按升序确定地处理。

给定这个定义，如果有一个跨所有的输入流并且在至少一个输入流上包含packet，则有默认输入策略的calculator将准备就绪。输入策略将所有可用的packets作为一个单一的输入集提供给calculator。

这种确定性行为的一个结果是，对于具有多个输入流的节点，理论上可以无限制地等待时间戳被确定，同时可以缓冲无限制数量的`packets`。（比如一个有两个输入流的节点，其中一个不断发送`packets`，而另一个不发送任何内容并且不推进边界。）

因此，我们还提供自定义输入策略：例如，将输入拆分到由 `SyncSetInputStreamHandler` 定义的不同同步集中，或者完全避免同步并在输入到达时立即处理由 `ImmediateInputStreamHandler` 定义。

## **Flow control（[流量控制](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=流量控制&zhida_source=entity)）**

主要有两种流量控制机制，当stream中缓冲的数据包达到由 `CalculatorGraphConfig::max_queue_size` 定义的（可配置的）限制时，backpressure mechanism会限制上游节点的执行。该机制保持确定性行为，并包括一个[死锁避免](https://zhida.zhihu.com/search?content_id=220002159&content_type=Article&match_order=1&q=死锁避免&zhida_source=entity)系统(deadlock avoidance system)，该系统可在需要时放宽配置的限制。

第二个系统包括插入特殊节点，这些节点可以根据 `FlowLimiterCalculator` 定义的实时约束（通常使用自定义输入策略）丢弃packets。例如，一个常见的模式在subgraph的输入处放置一个流量控制节点(flow-control node)，从最终输出到流量控制节点有一个环回连接。因此，流量控制节点能够跟踪下游graph中正在处理的时间戳数量，并在该计数达到（可配置的）限制时丢弃数据包;并且由于数据包在上游被丢弃，我们避免了由于部分处理时间戳然后在中间阶段之间丢弃数据包而导致的浪费工作。

这种calculator-based 的方法使graph作者可以控制丢弃数据包的位置，并允许根据资源限制灵活地调整和自定义graph的行为。

参考：[Synchronization](https://google.github.io/mediapipe/framework_concepts/synchronization.html%23flow-control)

