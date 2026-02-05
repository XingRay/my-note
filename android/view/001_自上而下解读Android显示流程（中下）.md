![自上而下解读Android显示流程（中下）：Graphic Buffer的共享、分配、同步](D:\my-note\android\view\assets\v2-b6ac5e6bc10f3d7273dc8539a94f16e7_1440w.png)

# 自上而下解读Android显示流程（中下）：Graphic Buffer的共享、分配、同步

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a.jpg)

同济大学 计算机系统结构硕士

关注

57 人赞同了该文章





目录

收起

1. Buffer共享

1.1 进程间共享

1.2 硬件共享

1.2.1 CPU如何访问Graphic Buffer

1.2.2 Graphic Buffer常用的共享方式：ion fd

2. Buffer分配

2.1 Gralloc

2.1.1 Server端：Allocate

2.1.2 Android S blastQueue

2.1.3 Client端：Mapper

2.2 BufferQueue

3.1 最可能的路径1：（绘制——合成——显示）

3.1.1 三级流水线的必要性

3.2 路径2：（仅包含 绘制——合成）

3.3 路径3：（仅包含 合成——显示）

4 Fence代码分析（虽然代码较老，但框架没变）

4.1 acquireFence: （见下图 ：acquireFence流程）

4.2 releaseFence: （见下图 ：releaseFence流程）

参考：

我们继续讨论显示的3个流水线：绘制——合成——显示中的Buffer流转过程：分为3部分：**Graphic Buffer的共享、分配、同步**。

## 1. Buffer共享

### **1.1 进程间共享**

如果我们按照进程模型来说，**绘制为Client端（APP），而合成为Server端（SF）**，最后的显示与合成关系更近，也算是Server端。

![img](D:\my-note\android\view\assets\v2-97a5aa3591fb2166fa3b31b09bb6d0e7_1440w.png)

图1 Graphic buffer进程间共享

**Graphic Buffer**能被Client与Server端同时认识，则需要共享机制，在**Android上采用的是Binder机制**；在Linux上则是采用**Socket**。

### **1.2 硬件共享**

如果按照硬件来说，**Graphic Buffer会被GPU，CPU，DPU访问**（其中CPU除了非常特殊情况下，是不会访问这个Buffer，更多的是状态的改变）

**PC独立显卡的显存就是GPU与DPU共享的。**

> Splitting DRM and KMS device nodes
> While most devices of the **3 major x86 desktop GPU-providers have GPU and display-controllers merged on a single card**, recent development **(especially on ARM)** shows that rendering (via **GPU)** and mode-setting (via **display-controller or called DPU**) **are not necessarily bound to the same device.** To better support such devices, several changes are being worked on for DRM.
>
> 1) Render-nodes
> 2) Mode-setting nodes
>
> Rendering and mode setting are split into two different APIs and are accessible through /dev/dri/renderX and /dev/dri/controlDX

但是**对于嵌入式设备，GPU与DPU不属于同一个硬件**。

如果多个硬件设备能访问同一个Buffer，就需要一个共享机制，而这种**跨硬件的共享方式就是Prime fd**（dma buffer）。而Linux系统2012年引入的原因正是为了解决**集成显卡与独立显卡的Buffer共享**问题（英伟达的Optimus技术）

![img](D:\my-note\android\view\assets\v2-dd8a469a23696c42c1a6a82fbcaaf0eb_1440w.png)

**在Android HAL层中是ION方式实现，也就是ion fd；**

CPU与GPU是有较为频繁的交互，*但并不是针对Graphic Buffer，主要是绘制命令和纹理贴图数据。只有Graphic Buffer才采用ion，在Android上通过EGL mem体现，而其他buffer则通过GL mem体现，后者才是大头。*

对于移动设备，这个Graphic Buffer地址CPU也是能快速访问的（**统一内存架构**），对于**PC上独立显卡**则需要经过**PCI-e**传输而慢了很多，**但CPU通常并不会访问它**。

### **1.2.1 CPU如何访问Graphic Buffer**

**图1**是有年代感的图了，会有个误导：就是mmap函数（*与后面提到的*GraphicBufferMapper*还不一样*），**这主要是用于CPU的共享方式，或用于CPU与GPU共享**，但其实**Graphic Buffer通常是在GPU、DPU之间的共享**，所以这个mmap函数几乎是很少使用的，我们也能从Gralloc的实现演进看出来。

> mmap的时候很少，此时CPU与GPU访问一块地址（*对于移动设备，GPU没有独立显存时，这个函数很快，但如果独显，则需要PCIE传输，就要考虑这个延时*）；

CPU何时使用GraphicBuffer？

> **surface-> lock（）**就是map这块地址了，**这时我们需要这块地址addr**，此时是CPU来访问了。但几乎不会用到。[Android图形缓冲区映射过程源码分析_深入剖析Android系统-CSDN博客](https://blog.csdn.net/yangwen123/article/details/12234931) 中还是**比较久远的gralloc 1.0，其中register也使用了map函数，但目前register的实现也不会如此**。

### **1.2.2 Graphic Buffer常用的共享方式：ion fd**

正常的时候，**GPU与DPU直接进行交互，只需要传递Fd即可**，**DPU压根不关心这个具体地址**。通常Gralloc中的是这样使用的，如下只是fd就够了。（importBuffer是关键函数）

> **[initWithHandle](http://androidxref.com/9.0.0_r3/s?refs=initWithHandle&project=frameworks)-->**[mBufferMapper](http://androidxref.com/9.0.0_r3/s?defs=mBufferMapper&project=frameworks).[importBuffer](http://androidxref.com/9.0.0_r3/s?defs=importBuffer&project=frameworks)([handle](http://androidxref.com/9.0.0_r3/s?defs=handle&project=frameworks), [width](http://androidxref.com/9.0.0_r3/s?defs=width&project=frameworks), [height](http://androidxref.com/9.0.0_r3/s?defs=height&project=frameworks), [206](http://androidxref.com/9.0.0_r3/xref/frameworks/native/libs/ui/GraphicBuffer.cpp%23206)[layerCount](http://androidxref.com/9.0.0_r3/s?defs=layerCount&project=frameworks), [format](http://androidxref.com/9.0.0_r3/s?defs=format&project=frameworks), [usage](http://androidxref.com/9.0.0_r3/s?defs=usage&project=frameworks), [stride](http://androidxref.com/9.0.0_r3/s?defs=stride&project=frameworks), &[importedHandle](http://androidxref.com/9.0.0_r3/s?defs=importedHandle&project=frameworks));

但是你看arm的gralloc开源实现，你会发现importBuffer里面竟然也有mapper函数，这与上面讲述的怎么有冲突了？[https://developer.arm.com/downloads/-/mali-drivers/android-gralloc-module](https://developer.arm.com/downloads/-/mali-drivers/android-gralloc-module)

其实原因在于gralloc增加了metadata，用来兼容gralloc buffer的各种信息，而不用去直接访问private_handle，每个厂家都去自己实现的类。（private_handle目前有2个fd，一个fd用于graphic buffer，另外一个fd就是metadata，而metadata是需要CPU访问的信息，这个就要map，而graphic buffer则不需要）

## 2. Buffer分配

### **2.1 Gralloc**

顾名思义：graphic alloc；但其实包含了：**分配Buffer + 使用Buffer**；

不像PC，**独立显卡有独立显存**，Graphic Buffer自然只能使用这样的显存（效率才高）。最近发布的Mac使用统一内存架构，而这个早已是手机上通用的架构了**，GPU与CPU是通用RAM的，包括Graphic Buffer都是在RAM上分配的。***（比如高通Adreno，高速显存**有1-3M**左右，但不用于Graphic Buffer）*

**问题：Gralloc分配的Buffer是物理地址连续的吗？**

> 对于普通的GraphicBuffer（drm_framebuffer）是不需要的，**因为GPU、DPU都有MMU功能，无需物理地址连续，即分配system heap就可以**；
> 对于视频、Camera可能会有要求，要看VPU，ISP是否支持MMU，或者其他要求；

### **2.1.1 Server端**：**Allocate**

GraphicBufferAllocator ：分配Buffer，由 SurfaceFlinger负责（也可以用单独的服务，这个可以配置，后面默认就用SF）；**只有两个接口：alloc与free**

![img](D:\my-note\android\view\assets\v2-921674ce31371f797d20df1d7476c826_1440w.png)

老式方式-Server端分配（SF分配）

### **2.1.2 Android S blastQueue**

> 为了减小SF的负载，Android S开始强制**Client端分配buffer，**而linux上早已如此处理。Android 12 Google将BufferQueue（简称BQ）组件从SF端移动到了客户端，BQ组件的初始化也放在BlastBQ的初始化中。
>
> Android S 版本之前
> *应用绘制缓冲区仅能通过 BufferQueue IGBP（IGraphicBufferProducer） 提交；*
> *应用窗口Geometry的改变仅能通过事务（Transaction）提交；*
> *通过合并事务（Transaction.merge()）或延迟事务来更改应用窗口间的Geometry；*
> *多进程缓冲区之间无法做到同步。*
> Android S 或更高版本
> *应用绘制缓冲区可以通过事务Transaction.setBuffer()进行提交；*
> *应用窗口Geometry的改变可以通过BlastBufferQueue进行提交；*
> *应用绘制的缓冲区和应用窗口Geometry可以进行同步；*
> *多应用绘制的缓冲区之间可以进行同步。*
> 作者：大天使之剑
> 链接：[https://www.jianshu.com/p/50a30fa6952e](https://www.jianshu.com/p/50a30fa6952e)
> 来源：简书

![img](D:\my-note\android\view\assets\v2-d4f946646db723e2ada181f12a257db5_1440w.png)

能看出来，这个新的架构，混合了数据端与控制端，之前bufferqueue只负责数据端，而SurfaceControl负责控制端，如今可以混合使用了，BufferQueue也可以控制大小，宽高，而SurfaceControl可以更新buffer。

所以说，假如buffer与gemetry同时更新，则在一个调用里面就可以完成。

### 2.1.3 **Client端**：**Mapper**

GraphicBufferMapper ：应用使用Buffer，由GPU填东西了；**接口主要为：importBuffer（之前是registerBuffer），lock**，导入当前进程地址空间。

![img](D:\my-note\android\view\assets\v2-d75477aca5a5be96d436a64d86d7e21e_1440w.png)

> Android 系统中，真正会分配图形缓冲区的进程仅有 Surfaceflinger（**如今也可以配置由client端负责，Android S上已经强制如此**），尽管可能会创建 BufferQueue 的进程有多个。
> GraphicBufferAllocator 和 GraphicBufferMapper 在对象创建的时候，都会通过 **Gralloc1::Loader 加载 HAL gralloc**。只有需要分配图形缓冲区的进程才需要创建 GraphicBufferAllocator，只有需要访问图形缓冲区的进程，才需要创建 GraphicBufferMapper 对象。
> 从 Android 的日志分析，可以看到，只有 Surfaceflinger 进程中，同时发生了为创建 GraphicBufferAllocator 和 GraphicBufferMapper 而加载 HAL gralloc。而为创建 GraphicBufferMapper而加载 HAL gralloc 则发生在 zygote、bootanimation 等多个进程。可见能够访问图形缓冲区的进程包括 Android Java 应用等多个进程。

**Zygote在preload会加载GraphicBufferMapper，因为zygote是所有应用的父进程**，也就是说每个应用也都会调用这个mapper，这样的话，应用才能正常使用这个buffer，拿来绘制。Zygote Preload的资源还有就是*常用texture 和 常用shader*。

下图为**谷歌**设计的显示栈，可以看到**gralloc**.**minigbm**在整个架构中的位置（高通还是按照ION框架实现，虽然底层也是drm buffer）

![img](D:\my-note\android\view\assets\v2-db989d7e34179a50c4796a4912b6fb11_1440w.png)

Google DRM for Android：Android on an Upstream Stack

### **2.2 BufferQueue**

GraphicBuffer **如上图所示，是GraphicBufferAllocator 与GraphicBufferMapper 结合体**，接口包含分配，也包含使用。

GraphicBuffer的队列放在缓冲队列BufferQueue中（**Client和Producer在这里是同义的**）。

**BufferQueue对App端的接口为IGraphicBufferProducer，实现类为Surface**，对SurfaceFlinger端的接口为IGraphicBufferConsumer，实现类为SurfaceFlingerConsumer（最新版本改名了，但不影响讨论，android S 已经基本取消了bufferQueueLayer，默认都使用BufferStateLayer）

BufferQueue中对每一个GraphiBuffer都有BufferState标记着它的状态，

> 比如new Surface是不会真正分配的，只有在**dequeuBuffer的时候才会请求分配，此时会调用**new GraphicBuffer则会真正分配。
> 在状态分配时，对于Client端有dequeueBuffer(请求), queueBuffer（绘制结束，发送至服务端） ；

![img](D:\my-note\android\view\assets\v2-4323396c749db0540293ae9ff11382a6_1440w.webp)

> 除了上面提到的dequeue/queue/acquire/release这些基本操作函数外，BufferQueue还为我们提供了一些特殊函数接口，方便调用者在一些非常规流程中使用（用于视频、Camera，正常UI不调用attach与detach）。
> Producer：
> *attachBuffer 不涉及到buffer的分配动作*
> *dequeueBuffer 可能会涉及到buffer的分配动作*
> *detachBuffer 释放buffer，slot —> mFreeSlots*
> *cancelBuffer 不释放buffer，slot —> mFreeBuffers*
> Consumer：
> *attachBuffer 直接从FREE —> ACQUIRED*
> *acquireBuffer 必须是 QUEUED —> ACQUIRED*
> *detachBuffer 释放buffer，slot —> mFreeSlots*
> *releaseBuffer 不释放buffer，slot —> mFreeBuffers*
> 原文链接：[BufferQueue 学习总结（内附动态图）](https://blog.csdn.net/hexiaolong2009/article/details/99225637)

**一个显示流程需要几个BufferQueue呢？**

1. 如上面所述，正常情况一个BufferQueue就够了，APP就是生产端，SurfaceFlinger就是消费端。
2. 对于Camera来说，则涉及三个缓冲区队列：

- `App` - 该应用使用 `SurfaceTexture` 实例从相机接收帧，并将其转换为外部 GLES 纹理。
- `SurfaceFlinger` - 应用声明用来显示帧的 `SurfaceView` 实例。
- `MediaServer` - 您可以使用输入 Surface 配置 `MediaCodec` 编码器，以创建视频（与显示关系不大）。

在上图中，箭头指示相机的数据传输路径。 `BufferQueue` 实例则用颜色标示（**生产方为青色，使用方为绿色**）。

![img](D:\my-note\android\view\assets\v2-7e5627812dea74dc28b7bf6a63b1098c_1440w.png)

3. 上面这个过程实际上还有可能增加一个BufferQueue：也就是GPU合成的时候

![img](D:\my-note\android\view\assets\v2-854ad60cde33da9f2364409adddb7137_1440w.png)

此时SurfaceFlinger则又增加一个BufferQueue，此时有4个BufferQueue了（关乎显示流程的是3个）。

**3 Buffer 同步：Fence**

在 Android 里面，总共有三类fence：`acquire fence，release fence 和 **present fence**`。其中acquire fence和release fence隶属于Layer，用于buffer同步，***present fence隶属于帧（即 Layers），不用于buffer同步，只有统计意义, present fence 通常等于 release fence（layers）：\***

- `acquire fence`：App将Buffer通过queueBuffer()还给BufferQueue的时候，此时该Buffer的GPU侧其实是还没有完成的，此时会带上一个fence，这个fence就是acquire fence。当SurfaceFlinger/HWC要读取Buffer以进行合成操作的时候，需要等acquire fence释放之后才行。

- `release fence`：当App通过dequeueBuffer()从BufferQueue申请Buffer，要对Buffer进行绘制的时候，需要保证HWC已经不再需要这个Buffer了，即需要等release fence signal才能对 Buffer进行写操作。

- `present fence`：在HWC1的时候称为retire fence，在HWC2中改名为present fence。当前帧成功显示到屏幕的时候，`present fence就会signal`。

- - Present fence 存在的意义有两个：

  - - ***1. 软件vsync的统计同步；***

![img](D:\my-note\android\view\assets\v2-88138b466a2d6982da5f66b41c8f923d_1440w.webp)

DispSync是利用HW_VSYNC和PresentFence来判断是否需要开启HW_VSYNC.HW_VSYNC 最少要3个, 最多是32个, 实际上要用几个则不一定（目前是6个）,DispSync拿到3个HW_VSYNC后就会计算出SW_VSYNC,只要收到的PresentFence没有超过误差,则HW_VSYNC就会关掉, 不然会继续开启HW_VSYNC计算SW_VSYNC的值, 直到误差小，如下图。

![img](D:\my-note\android\view\assets\v2-cf7a44ca4978fb21e6638fbed19aab84_1440w.png)

采用软件Vsync原因——下面是谷歌给出的解释。但我认为**节省功耗是一个很重要的原因**。

```text
SurfaceFlinger: SW-based vsync events     
This change adds the DispSync class, which models the hardware vsync event    
times to allow vsync event callbacks to be done at an arbitrary phase offset    
from the hardware vsync.  
This can be used to reduce the minimum latency from   
Choreographer wake-up to on-screen image presentation. 
```

- - - ***2. 计算layer帧率。***

![img](D:\my-note\android\view\assets\v2-48352a6b8bcae8bb30302299db901318_1440w.png)

因为present fence是表征所有layers的，即任何layer更新并被显示后，hwc均会signal surfaceflinger。那么假设要统计 fps 的 Layer 没有更新，但是别的 Layer 更新了，这种情况下 present fence 也会正常 signal，如上图，只有有效帧才会被统计。

下面继续将Buffer的同步：（acquire fence，release fence）

BufferQueue里面的QUEUED，DEQUEUE等状态一定程度上说明了该GraphicBuffer的归属，但仅仅指示了CPU里的状态，而GraphicBuffer的真正使用者是GPU和DPU。也就是说，当生产者把一个GraphicBuffer放入BufferQueue时，仅仅是在CPU层面完毕了归属的转移。但GPU说不定还在用，假设还在用的话消费者是不能拿去合成的。这时候GraphicBuffer和生产消费者的关系就比較暧昧了。消费者对GraphicBuffer具有拥有权。但无使用权，它须要等一个信号，告诉它GPU用完了，消费者才真正拥有使用权。

**这就需要一种不仅是跨进程的，也是跨硬件的同步机制: Fence 机制**

[Android中的GraphicBuffer同步机制-Fence - brucemengbm - 博客园](https://www.cnblogs.com/brucemengbm/p/6881925.html)

> GPU编程和纯CPU编程一个非常大的不同是它是异步的。也就是说当我们调用GL command返回时这条命令并不一定完毕了。仅仅是把这个命令放在本地的command buffer里。详细什么时候这条GL command被真正运行完毕CPU是不知道的，除非CPU使用glFinish()等待这些命令运行完，第二种方法就是基于同步对象的Fence机制。

一个说明：

**我们这里的同步基本到合成结束，因为显示这一部分：我们默认buffer早已经是完整的，且完全更新过了（尽管可以在最后一行刷新的时候准备好最后一行就好了，就像VR那样，但手机上我们不这么做）**

同步也就是在GPU、DPU、CPU之间，其中CPU只处理Buffer状态，并不触及内容，真正的内容同步是GPU、DPU之间的，这是通过Fence完成的。如下图，可以看出有3个不同的Client与Server路径。

（从下图，能看出一个有意思的现象，为何3Buffer机制中FB确使用了2个buffer呢？后面路径2会解释）

![img](D:\my-note\android\view\assets\v2-96085cbca1cdd96d505ca49cbcbd5d73_1440w.webp)

原图



![img](D:\my-note\android\view\assets\v2-cabb2ebcd7fa9a429991d82dc9b8a569_1440w.webp)

注释路径

### **3.1 最可能的路径1：**（绘制——合成——显示）

在Client与Server端同步一般会遇到2个问题：

1）在client端GPU还在绘制的时候，Server端DPU是不能拿来用的，否则就是撕裂的画面；（此时是等待acquireFence，**具体的流程可参加最后一节\*Fence代码流程\*，直接引用参考文献**）

![img](D:\my-note\android\view\assets\v2-c011f3df90b571365510e4c20c7f4e81_1440w.png)

如上，Client的Renderer发送Buffer的时候是顺便带着一个Fence的，这个fence经过SF、HWC传到DisplayDriver，此时会wait(acquire_fences())，然后在vsync的时候传给Display。

2）在Server端还在显示的时候，这个Buffer同样是不能被Client端GPU拿来使用的，否则显示也会撕裂；（此时是ReleaseFence）如上图，Client的Renderer在dequeueBuffer的时候也会等待。

大部分情况下，正是走的这条路径，这样的话2个流水线是能走完的，那剩下的时间都干嘛去了？如下图：

![img](D:\my-note\android\view\assets\v2-f220c8b034311b594f8ca71b482ab1be_1440w.png)

**可见真正的绘制是很长的，可以有接近2个vsync周期**；看起来是有些浪费（主要是时间的浪费，GPU资源并没有浪费，流水线是能充分利用资源），如果我们将流水线设置为2段（绘制+合成作为一个），这与PC上是类似的；

### 3.1.1 三级流水线的必要性

按照上面这样的描述，似乎使用2个流水线（**绘制——显示**）就能搞定，**这也是Fence引入时所希望的**，但为什么我们并没有这么做？（上一节并没有讲的那么清楚）。



![img](D:\my-note\android\view\assets\v2-5b913ebe1766568df69495b112f431c0_1440w.png)

**问题的回答：**

但是如前所述，如果我们采用VSYNC，则需要3 Buffer（**PC游戏上3 Buffer与Vsync也是相辅相成**）

> 谷歌文档中，给出了这样的解释：假如缓存区B的处理需要较长时间，并且A正在使用中显示当前帧。此时引入第三个缓存区，则CPU与GPU不会空等，而是系统创建C缓冲区，可以避免下一次Jank，从而让系统变得流程，通俗来讲就是减小红绿灯的等待。

![img](D:\my-note\android\view\assets\v2-db7b1a02352892b7d48d0de12bbdf6e4_1440w.png)

但这个图是有误导作用的，首先对于Buffer A、B、C来说，CPU是不会触碰到的，只有GPU与Display才是使用者，**对于CPU只是处理状态，这样的蓝色B，C，A就是对于当前业务逻辑的处理**，而且这样的业务逻辑是随机的，所以就会出现上面这样的情况；如下图所示

![img](D:\my-note\android\view\assets\v2-89ed1560acd2f58afb6f3b59b520d870_1440w.png)

依赖于GPU的地方在于，**flush_commands 和 eglSwapBuffer**，如下图，CPU占比还是比较重的，也就是帧率稳定性会受到较大的波动（出现谷歌文档出现的现象）

![img](D:\my-note\android\view\assets\v2-56dbc6f07c4ccf6f516b7ec1e3c6f6e9_1440w.png)

（从以上可以看出，CPU的处理过程占了很大的比重，对于游戏来说，通常来说CPU的处理是恒常的，但如转场动画、触控事件都会导致不确定性；但假如游戏设计的很好，双Buffer不是不可行的，但是这需要Android重新设计流水线了，同时也不要等待Vsync调节了）

### **3.2 路径2：**（**仅包含 绘制——合成**）

但显然还有另外一种情况，那就是如果我们不得不Fallback to GL composer时，该如何呢？

1）在client端GPU还在绘制的时候，Server端（此时是GPU了，GPU是支持多实例的）不能拿来合成至FrameBufferSurface，否则合成后便是撕裂的画面；

2）在Server端（同样是GPU）在合成至FrameBufferSurface（doing），这个Buffer同样是不能被Client端GPU拿来使用的。

***FB之所以使用2个Buffer，则是因为使用2个流水线，这个FB buffer，绘制阶段是用不到的。\***

### **3.3 路径3：**（**仅包含 合成——显示**）

但其实路径2还没结束

此时又是一个Cleint端与Server端的流程（尽管此时Client与Server端都分别对应SF与composer）

1）在client端GPU还在合成的时候，Server端DPU是不能拿来用的，否则就是撕裂的画面；

2）在Server端还在显示的时候，这个Buffer同样是不能被Client端GPU拿来使用的，否则显示也会撕裂；

------

## **4 Fence代码分析（***虽然代码较老，但框架没变***）**

**全部援引参考：**[Android中的GraphicBuffer同步机制-Fence - brucemengbm - 博客园](https://www.cnblogs.com/brucemengbm/p/6881925.html)

### **4.1 acquireFence: （见下图 ：acquireFence流程）**

![img](D:\my-note\android\view\assets\v2-264f8a4cebb1e9683d6324c6b1087df4_1440w.webp)

当App端通过queueBuffer()向BufferQueue插入GraphicBuffer时，会顺带一个Fence，这个Fence指示这个GraphicBuffer是否已被生产者用好。之后该GraphicBuffer被消费者通过acquireBuffer()拿走，同一时候也会取出这个acquireFence。之后消费者（也就是SurfaceFlinger）要把它拿来渲染时，须要等待Fence被触发。假设该层是通过GPU渲染的，那么使用它的地方是Layer::onDraw()。当中会通过bindTextureImage()绑定纹理：

```c
status_t err = mSurfaceFlingerConsumer->bindTextureImage();
```

该函数最后会调用doGLFenceWaitLocked()等待acquireFence触发。由于再接下来就是要拿来画了。假设这儿不等待直接往下走，那渲染出来的就是错误的内容。
假设该层是HWC渲染的Overlay层，那么不须要经过GPU，那就须要把这些层相应的acquireFence传到HWC中。这样。HWC在合成前就能确认这个buffer是否已被生产者使用完，因此一个正常点的HWC须要等这些个acquireFence全被触发才干去绘制。这个设置的工作是在SurfaceFlinger::doComposeSurfaces()中完毕的。该函数会调用每一个层的layer::setAcquireFence()函数：

```text
428    if (layer.getCompositionType() == HWC_OVERLAY) {
429        sp<Fence> fence = mSurfaceFlingerConsumer->getCurrentFence();
...
431            fenceFd = fence->dup();
...
437    layer.setAcquireFenceFd(fenceFd);
```

能够看到当中忽略了非Overlay的层，由于HWC不须要直接和非Overlay层同步，它仅仅要和这些非Overlay层合成的结果FramebufferTarget同步就能够了。GPU渲染完非Overlay的层后，通过queueBuffer()将GraphicBuffer放入FramebufferSurface相应的BufferQueue。然后FramebufferSurface::onFrameAvailable()被调用。它先会通过nextBuffer()->acquireBufferLocked()从BufferQueue中拿一个GraphicBuffer，附带拿到它的acquireFence。
接着调用HWComposer::fbPost()->setFramebufferTarget()，当中会把刚才acquire的GraphicBuffer连带acquireFence设到HWC的Layer list中的FramebufferTarget slot中：

```text
580    acquireFenceFd = acquireFence->dup();
...
586    disp.framebufferTarget->acquireFenceFd = acquireFenceFd;
```


综上，HWC进行最后处理的前提是Overlay层的acquireFence及FramebufferTarget的acquireFence都被触发。

### **4.2 releaseFence: （见下图 ：releaseFence流程）** 

![img](D:\my-note\android\view\assets\v2-264f8a4cebb1e9683d6324c6b1087df4_1440w.webp)

前面提到合成的过程先是GPU工作，在doComposition()函数中合成非Overlay的层，结果放在framebuffer中。然后SurfaceFlinger会调用postFramebuffer()让HWC開始工作。
postFramebuffer()中最主要是调用HWC的set()接口通知HWC进行合成显示，然后会将HWC中产生的releaseFence（如有）同步到SurfaceFlingerConsumer中。实现位于Layer的onLayerDisplayed()函数中：

```aspectj
mSurfaceFlingerConsumer->setReleaseFence(layer->getAndResetReleaseFence());
```

上面主要是针对Overlay的层，那对于GPU绘制的层呢？在收到INVALIDATE消息时，SurfaceFlinger会依次调用handleMessageInvalidate()->handlePageFlip()->Layer::latchBuffer()->SurfaceFlingerConsumer::updateTexImage() ，当中会调用该层相应Consumer的GLConsumer::updateAndReleaseLocked() 函数。
该函数会释放老的GraphicBuffer，释放前会通过syncForReleaseLocked()函数插入releaseFence，代表假设触发时该GraphicBuffer消费者已经使用完成。然后调用releaseBufferLocked()还给BufferQueue，当然还带着这个releaseFence。
这样。当这个GraphicBuffer被生产者再次通过dequeueBuffer()拿出时。就能够通过这个releaseFence来推断消费者是否仍然在使用。
还有一方面，HWC合成完成后，SurfaceFlinger会依次调用DisplayDevice::onSwapBuffersCompleted() -> FramebufferSurface::onFrameCommitted()。onFrameCommitted()核心代码例如以下：

```zephir
148    sp<Fence> fence = mHwc.getAndResetReleaseFence(mDisplayType);
...
151    status_t err = addReleaseFence(mCurrentBufferSlot,
152                mCurrentBuffer, fence);
```

此处拿到HWC生成的FramebufferTarget的releaseFence，设到FramebufferSurface中相应的GraphicBuffer Slot中。这样FramebufferSurface相应的GraphicBuffer也能够被释放回BufferQueue了。当将来EGL从中拿到这个buffer时，照例也要先等待这个releaseFence触发才干使用。

## 参考：

[Android中的GraphicBuffer同步机制-Fence - brucemengbm - 博客园](https://www.cnblogs.com/brucemengbm/p/6881925.html)

[https://www.jianshu.com/p/cdc60627df90](https://www.jianshu.com/p/cdc60627df90)

[BufferQueue 学习总结（内附动态图](https://blog.csdn.net/hexiaolong2009/article/details/99225637)