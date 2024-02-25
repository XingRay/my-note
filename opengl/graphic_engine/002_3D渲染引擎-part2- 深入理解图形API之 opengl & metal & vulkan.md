# 3D渲染引擎-part2- 深入理解图形API之 opengl & metal & vulkan

在工作中有很长一段时间都被一个问题困扰着：跨平台对多个图形API是怎么友好的封装的？

1、OpenGL、OpenGL ES是单绘制线程+上下文 +逐个绘制元素的DrawCall。

2、Metal、Vulkan、DX 的API是怎么调用的，怎么实现的多线程？

3、这些API怎么统一抽象？

**基于原理基础理解的开发，是最简单友善的。**

**当认真的接触完每一个API,并实现一个具体的Demo,这些问题也就有了答案**。

跨平台渲染引擎的一个重要任务就是支持多图形API.理解每一个图形API(Opengl、Metal 、Vulkan、Dx)的渲染流程和特性，使用一套通用的逻辑接口来实现应用层数据和具体的图形API驱动的隔离。成熟的渲染引擎例如Unreal Engine 使用的是 RHI（Render Hardware Interface）的封装和命令转发.Cocos3D使用的是GFX(Graphics Force Express)的封装和命令转发。

**Graphics API - OpenGL/OpenGL ES**

对OpenGL 一些基础概念的了解强烈推荐**：**[LearnOpenGL CN](https://link.zhihu.com/?target=https%3A//learnopengl-cn.github.io/)

![img](D:\my-note\opengl\graphic_engine\assets\v2-f8772fa7c37b91d52f775780272251d2_1440w.webp)



- **OpenGL ES 的平台无关性是借助 EGL 实现的，EGL 屏蔽了不同平台的环境差异，EGL 是 OpenGL ES 和本地窗口系统（Native Window System）之间的通信接口。本地窗口相关的 API 提供了访问本地窗口系统的接口，而 EGL 可以创建渲染表面 EGLSurface ，同时提供了图形渲染上下文 EGLContext，用来进行状态管理，接下来 OpenGL ES 就可以在这个渲染表面上绘制。**

![img](D:\my-note\opengl\graphic_engine\assets\v2-c7b0a79d19241b7f1138d7203c9b01cb_1440w.webp)

EGL

![img](D:\my-note\opengl\graphic_engine\assets\v2-238a52ad4bb703cf5cc942a39a284f85_1440w.webp)

![img](D:\my-note\opengl\graphic_engine\assets\v2-22652c6e5a13fd46eefc528df71b8c34_1440w.webp)

![img](D:\my-note\opengl\graphic_engine\assets\v2-debb91b0124c06ef9a4020685296507e_1440w.webp)

data flow



**对于可编程管线阶段我们对着色器的处理主要在顶点着色器和片源着色器**。

![img](D:\my-note\opengl\graphic_engine\assets\v2-1f5a9134defed87373b22722401f548f_1440w.webp)

![img](D:\my-note\opengl\graphic_engine\assets\v2-b54971420f6983814ba2a9449e8dbd79_1440w.webp)



对Opengl/Opengl ES的使用其实就是参考：[无我：3D渲染引擎-part1-渲染管线](https://zhuanlan.zhihu.com/p/528435838)



**Graphics API - Metal**

![img](D:\my-note\opengl\graphic_engine\assets\v2-b09c1570f48b0450876fb6ecfad671c8_1440w.webp)

一些常见概念：

MTLDevice： GPU的抽象

MTLCommandQueue: 创建和管理CommandBuffer顺序

![img](D:\my-note\opengl\graphic_engine\assets\v2-222779dbfbf465443507400e0773187d_1440w.webp)

MTLCommandQueue

MTLCommandBuffer: Encoder缓冲区

MTLCommandEncoder: 编码命令

- MTLRenderCommandEncoder
- MTLComputeCommandEncoder
- MTLBlitCommandEncoder
- MTLParallelRenderCommandEncoder

![img](D:\my-note\opengl\graphic_engine\assets\v2-cbf129c39021e36fe4b9ac4394e394ba_1440w.webp)

![img](D:\my-note\opengl\graphic_engine\assets\v2-aa0e2fe31db2cd80d83ff98ff363d471_1440w.webp)

![img](D:\my-note\opengl\graphic_engine\assets\v2-bb239ac5c1fb3fe6147bb787aa6535bb_1440w.webp)



多线程的一些理解：

**Metal 单线程执行：**

![img](D:\my-note\opengl\graphic_engine\assets\v2-56d845fcb1311ed6baddedfc97f0da8a_1440w.webp)



**Metal多线程执行：**

![img](D:\my-note\opengl\graphic_engine\assets\v2-31a9e24e37332b0a18660936068802df_1440w.webp)

Metal多线程之间的同步：

应用负责手动同步 Metal 不跟踪的资源（从`MTLHeap`s 分配的资源不会自动跟踪），可以使用这些机制同步资源，这些机制按范围升序排列。

![img](D:\my-note\opengl\graphic_engine\assets\v2-cf44db08afdae4472850b9cd5c09f5cc_1440w.webp)



- Memory barriers
- Memory fences
- Metal events
- Metal shared events

![img](D:\my-note\opengl\graphic_engine\assets\v2-e7908bbb729f2331ec73a34a195bf87a_1440w.webp)



![img](D:\my-note\opengl\graphic_engine\assets\v2-b244ced155002e6a10873ab48e3dcbbc_1440w.webp)

![img](D:\my-note\opengl\graphic_engine\assets\v2-b2d88fffc02b7c4f3718115e7e0d5cc5_1440w.webp)



### Graphics API - Vulkan

开始理解Vulkan之前，我们先来看一些基础概念：

![img](D:\my-note\opengl\graphic_engine\assets\v2-3b8c099402f738a50873c10168bae0a0_1440w.webp)

Instances & Devices & Queue & CommandBuffer & CommandPool

1、Instances:用户应用逻辑和Vulkan逻辑隔离，可以理解为Vulkan运行上下文（**类似Opengl eglContext**）

2、Devices包括Physical Devices & Logic Devices

- Physical Devices:理解为物理设备
- Logic Devices: 逻辑设备代表实际设备的视图

3、Queue CommandBuffer提交到的命令缓存队列。

![img](D:\my-note\opengl\graphic_engine\assets\v2-2636ef40dbc20548675ab1dce266e7c6_1440w.webp)

vkQueue type

![img](D:\my-note\opengl\graphic_engine\assets\v2-ab53feea835a80b15a405b1a107c0696_1440w.webp)

Queue Family





4、CommandBuffer ：**任何要由 GPU 执行的工作都首先记录到命令缓冲区**。

![img](D:\my-note\opengl\graphic_engine\assets\v2-48dccfd0770296aa73bbfc85b7ed68fa_1440w.webp)

5、CommandPool: 分配CommandBuffer

**Note：最好是每一个线程维护自己的CommandPool.**

![img](D:\my-note\opengl\graphic_engine\assets\v2-31211cbdfb5be2f1f42a944082333066_1440w.webp)



**Vulkan Components：(Cocos3D的GFX部分抽象高度参考了Vulkan API )**

![img](D:\my-note\opengl\graphic_engine\assets\v2-42142519252b8fc16fd4e946f77f0c83_1440w.webp)



对这些基础概念有了一些认识之后，参考渲染管线：[无我：3D渲染-part1-渲染管线](https://zhuanlan.zhihu.com/p/528435838)，我们的绘制流程大概是：

![img](D:\my-note\opengl\graphic_engine\assets\v2-b7ea4e4d90bc931c21ec93da851038ce_1440w.webp)

Vulkan单线程抽象

我们来看一下简单的绘制一个三角形，会涉及到哪些基础资源的使用：

![img](D:\my-note\opengl\graphic_engine\assets\v2-c0ba73be8d0de240012c01125a7b366c_1440w.webp)

![img](D:\my-note\opengl\graphic_engine\assets\v2-449defe9a7d59003e63d4288c7b09367_1440w.webp)

渲染资源



在理解了单线程的执行过程后，**为什么要使用多线程？**

**很显然单线程的任务提交可能会遇到一个线程忙碌，另外的core空闲，不能提高并发性。**

![img](D:\my-note\opengl\graphic_engine\assets\v2-f42f9b19852c941a48f3a6d41a2ff4fa_1440w.webp)

**使用多线程能充分利用CPU的并行性。**

![img](D:\my-note\opengl\graphic_engine\assets\v2-713b539427276959f0594871deb92e32_1440w.webp)



绘制流程如下：

![img](D:\my-note\opengl\graphic_engine\assets\v2-7a5bf80bd97a3fba8bbd0ab705d1eae9_1440w.webp)

有多线程的地方，就有多线程的同步问题：

Vulkan的各个组件之间的同步关系如下：

- Command Buffer 与Command Buffer之间通过event & barrier来实现同步，event or barrier是最小的同步原语
- Queue与Queue之间通过semaphores来实现同步,semaphores是相对小的同步原语
- Device与Host之间通过Fences来实现同步，Fences是最大的同步原语

![img](D:\my-note\opengl\graphic_engine\assets\v2-2282e876cea34f24083b0db8d959f1a8_1440w.webp)

![img](D:\my-note\opengl\graphic_engine\assets\v2-55fb5017811e7787c0f9e0a4fce70820_1440w.webp)

vulkan的同步机制



从对OpenGL/OpenGL ES & Metal & Vulkan 有一个直观的理解后，我们发现 OpenGL/OpenGL ES 和 Metal & Vulkan的最大区别就是在多线程处理上。

1、OpenGL/OpenGL ES 是单线程的。

2、Metal & Vulkan 在 Device & Command Queue & CommandBuffer & RenderEncoder(Pipeline & Descriptor)等的概念上如此一致。

3、在线程同步问题上，需要注意的是 Vulkan 的 events & Semaphores & Fences 和 Metal 的MTLFence & MTLEvent在颗粒度上的概念有所不同，但是完成的同步事情是一致的。

了解了上边的三个问题后，我们对GFX层的抽象可能有一个更直观的概念。后边会讲一下 Unreal Engine & Cocos3D在这块的处理。



关于OpenGL/OpenGL ES的参考：

[LearnOpenGL CN](https://link.zhihu.com/?target=https%3A//learnopengl-cn.github.io/)

[柯灵杰：20分钟让你了解OpenGL——OpenGL全流程详细解读](https://zhuanlan.zhihu.com/p/56693625)

[杨殿铭：OpenGLES 与 EGL 基础概念](https://zhuanlan.zhihu.com/p/74006499)

[EGLSurface 和 OpenGL ES | Android 开源项目 | Android Open Source Project](https://link.zhihu.com/?target=https%3A//source.android.google.cn/devices/graphics/arch-egl-opengl%3Fhl%3Dzh-cn)

[The Book of Shaders](https://link.zhihu.com/?target=https%3A//thebookofshaders.com/)

[chenjd：聊聊那些不常见的Shader](https://zhuanlan.zhihu.com/p/31529550)

[How to apply textures to a game character using OpenGL ES — Harold Serrano - Game Engine Developer](https://link.zhihu.com/?target=https%3A//www.haroldserrano.com/blog/how-to-apply-textures-to-a-character-in-ios)

[Articles - Harold Serrano | Game Engine Developer](https://link.zhihu.com/?target=https%3A//www.haroldserrano.com/articles/%23gameenginesection)



关于Metal的参考：

[Metal Programming Guide(中文版)](https://link.zhihu.com/?target=https%3A//colin19941.gitbooks.io/metal-programming-guide-zh/content/)

[Metal多线程渲染](https://link.zhihu.com/?target=https%3A//www.jianshu.com/p/080b83aa057b)

[丛越：游戏引擎随笔 0x08：现代图形 API 实战回顾-Metal 篇](https://zhuanlan.zhihu.com/p/114072278)

[Apple Developer Documentation](https://link.zhihu.com/?target=https%3A//developer.apple.com/documentation/metal/resource_synchronization)



关于Vulkan的参考：

[Learning Vulkan](https://link.zhihu.com/?target=https%3A//www.oreilly.com/library/view/learning-vulkan/9781786469809/ch03s05.html)

[Vulkan多线程渲染](https://link.zhihu.com/?target=https%3A//www.jianshu.com/p/5acee1167f32)

[https://developer.nvidia.com/sites/default/files/akamai/gameworks/blog/munich/mschott_vulkan_multi_threading.pdf](https://link.zhihu.com/?target=https%3A//developer.nvidia.com/sites/default/files/akamai/gameworks/blog/munich/mschott_vulkan_multi_threading.pdf)

[队列 Queue · Vulkan 学习笔记](https://link.zhihu.com/?target=https%3A//gavinkg.github.io/ILearnVulkanFromScratch-CN/mdroot/%E6%A6%82%E5%BF%B5%E6%B1%87%E6%80%BB/%E9%98%9F%E5%88%97.html)

[Introduction - Vulkan Tutorial](https://link.zhihu.com/?target=https%3A//vulkan-tutorial.com/)

[What is actually a Queue family in Vulkan?](https://link.zhihu.com/?target=https%3A//stackoverflow.com/questions/55272626/what-is-actually-a-queue-family-in-vulkan)

[https://ourmachinery.com/post/vulkan-command-buffer-management/](https://link.zhihu.com/?target=https%3A//ourmachinery.com/post/vulkan-command-buffer-management/)

[https://ourmachinery.com/post/vulkan-descriptor-sets-management/](https://link.zhihu.com/?target=https%3A//ourmachinery.com/post/vulkan-descriptor-sets-management/)

[lyf：android graphic(15)—fence](https://zhuanlan.zhihu.com/p/68782630)

[【Vulkan学习记录-基础篇-4】Vulkan中的同步机制_syddf_shadow的博客-CSDN博客_vulkan的同步机制](https://link.zhihu.com/?target=https%3A//blog.csdn.net/yjr3426619/article/details/101371746)