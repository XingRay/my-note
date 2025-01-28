# MediaPipe学习笔记一

## **前言**

设备端机器学习是指在 Android 或 iOS 智能手机、物联网设备或网络浏览器等终端设备上所做的机器学习。MediaPipe专注于设备端机器学习开发以及部署流程。以帮助所有开发者在应用内搭建自己的机器学习技术。

简单一句话：MediaPipe 是谷歌开发的为直播和流媒体提供跨平台，可定制的机器学习解决方案；

### **特点：**

- **端到端加速**（**End-to-End acceleration**）：内置加速机器学习推理处理
- **一次构建，随处部署**（**Build once, deploy anywhere**）：统一的解决方案适用于Android，iOS，桌面/云，网络和物联网；
- **开箱即用**（**Ready-to-use solutions**）： 顶尖的机器学习解决方案展示了该框架的强大能力；
- **开源：**架构和解决方案在Apache2.0下，完成可拓展和可定制；

## **基础概念**

### **Packet**

基本数据流单元。数据包由时间戳和指针组成。数据可以是任何类型，如一帧图片或者检测数。**Calculators**通过发送和接送数据包通信。

**Creating a packet**

Packet通常使用`mediapipe::MakePacket<T>()`或者`mediapipe::Adopt()`（来自 packet.h）

```cpp
// Create a packet containing some new data.
Packet p = MakePacket<MyDataClass>("constructor_argument");
// Make a new packet with the same data and a different timestamp.
Packet p2 = p.At(Timestamp::PostStream());
```

或

```cpp
// Create some new data.
auto data = absl::make_unique<MyDataClass>("constructor_argument");
// Create a packet to own the data.
Packet p = Adopt(data.release()).At(Timestamp::PostStream());
```

数据包中的数据通过`Packet::Get<T>()`访问；

### **Graph**

**Graph**定义了节点(**Node**)之间的数据包流路径。一个图可以有任意数量的输入和输出，**Packet** 可以向前流动，也可构成循环，其流动路径与实际业务相关。用户通过编写[配置文件](https://zhida.zhihu.com/search?content_id=219956549&content_type=Article&match_order=1&q=配置文件&zhida_source=entity)来描述 **Graph** 的[拓扑结构](https://zhida.zhihu.com/search?content_id=219956549&content_type=Article&match_order=1&q=拓扑结构&zhida_source=entity)以及实现的业务流程，可在配置文件中定义 **Graph** 的输入和输出。

### **Nodes**

**Node** 为 **Graph** 的基础模块，如视频解码、图片解码、图片前处理、[模型推理](https://zhida.zhihu.com/search?content_id=219956549&content_type=Article&match_order=1&q=模型推理&zhida_source=entity)，都可以作为一个 **Node**。也被称为“**[calculators](https://zhida.zhihu.com/search?content_id=219956549&content_type=Article&match_order=1&q=calculators&zhida_source=entity)**”，**Node** 相互连接构成了一个 **Graph** ，**Graph** 启动后插件运行在 **Node** 中。

### **Streams**

streams流是承载一系列数据包的两个**nodes**(节点)之间的连接，其时间戳必须单调递增。

### **Side packets**

**Side packet** 连接未指定时间戳的携带数据包的节点，通常用来提供保持不变的数据，而**stream**（流）表示随着时间变化的数据流

### **Packet Ports**

**a port**(端口)有关联的类型； 通过该端口传输的**packets**(数据包)必须属于该类型。 一个输出流端口(**a output stream port** )可以连接到任意数量的相同类型的输入流端口； 每个消费者都会收到一份单独的输出数据包副本，**并有自己的队列**，因此可以按照自己的节奏消费它们。 类似地， **a side packet output port** (侧包输出端口)可以连接到所需数量的**side packet input ports**(侧包输入端口)。

**注意**：即使需要流(**stream**)连接，流(**stream**)也可能会不携带任何时间戳的数据包。

## **Input and output**

数据流可能源自没有输入流的[源节点](https://zhida.zhihu.com/search?content_id=219956549&content_type=Article&match_order=1&q=源节点&zhida_source=entity)（**source nodes**），例如读取文件，也可能来自于让应用程序将数据包传入图（**Graph**）中的**graph input streams**

## **Runtime behavior**

### **Graph 生命周期**

一旦**Graph**被初始化，它就可以开始处理数据，并且可以处理数据包流，直到每个流被关闭或**Graph**被取消。然后**Graph**可以被销毁或重新开始。

### **Node 生命周期**

在node上有三种主要的生命周期方法：

- Open：在其他方法之前调用一次。当它被调用时，node所需的所有输入端数据包(input side packets)都将可用。
- Process：多次调用，当一组新的输入是可用的，根据节点输入策略；
- Close：在结束时，调用一次；

此外，每一个calculator都可以定义构造函数和[析构函数](https://zhida.zhihu.com/search?content_id=219956549&content_type=Article&match_order=1&q=析构函数&zhida_source=entity)，用于创建和释放与处理数据无关的资源。

### **Input 策略**

一个默认的输入策略是按照时间戳对数据包进行的，节点调用其**Process**方法接收同一时间戳的所有输入。连续的[输入集](https://zhida.zhihu.com/search?content_id=219956549&content_type=Article&match_order=1&q=输入集&zhida_source=entity)按照时间戳顺序接收。这可能需要延迟某些数据包的处理，直到在所有输入流上都收到具有相同时间戳的数据包，或者直到可以保证具有该时间戳的数据包不会到达尚未接收到它的流。

其他的输入策略参见**InputStreamHandler**

### **Real-time streams**

**MediaPipe calculator graphs** 通常用于处理交互式应用程序的视频或音频帧流。通常，每个**calculator**都会在给定时间戳的所有输入数据包可用时立即运行。**real-time graphs**中使用的**Calculators**需要根据输入时间戳边界定义输出时间戳边界，以便能够及时调度下游**Calculators**。

