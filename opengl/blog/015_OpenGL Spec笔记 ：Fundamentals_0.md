# OpenGL Spec笔记 ：Fundamentals_0

这一章将会介绍一些基础概念，包括

1. OpenGL execution model
2. API syntax
3. contexts
4. threads
5. numeric representation
6. context state
7. state queries
8. different types of objects and shader

他会为规格书剩余其他更加具体的解释每个指令和行为的作用的章节提供一个基本框架。

## 2.1 Execution Model(执行模型)

OpenGL（下文简称GL）只会关心储存在GPU内存中的数据，包括渲染到[帧缓存](https://zhida.zhihu.com/search?content_id=215932379&content_type=Article&match_order=1&q=帧缓存&zhida_source=entity)(framebuffer)以及读取储存在帧缓存中的数据。它不会支持任何形式的[输入输出设备](https://zhida.zhihu.com/search?content_id=215932379&content_type=Article&match_order=1&q=输入输出设备&zhida_source=entity)，用户需要根据自己的设备来选择对应的工具链提供输出输出。

GL绘制primitives(图元)是通过contex state控制一系列着色器程序(shader programs)以及固定功能的处理单元实现的。

图元可以是 point,line segment，patch，polygon

当绘制不同的图元的时候Context state需要被改变为独立不同的状态。一个图元的状态设置改变不会影响其他的状态，尽管状态和在着色器相互作用共同决定framebuffer中最终产生的结果。当状态设定完成，图元被绘制，接下来的GL操作将会由用户发送Common函数来完成。

图元(primitives)由一个或者一组Vertices定义。Vertices定义一个点，或者一个线段的两端，或者多边形两条边的交点。数据包含 **位置坐标(positional coordinates) ，颜色，法线，纹理坐标，**等等。这些数据共同组成了一个vertex。

每个vertex被处理的时候都是**完全独立的，按顺序的，相同方式**的。有一个**特殊的情况**，不适用于上述的规则，那就是启用**Clippe测试**的时候，如果一个vertices刚好落在了剪切测试之外，那么这个vertices数据将会被修改，并且产生一个新的vertices，至于具体怎么剪裁，这是要由这组vertices数据是描述什么图元来决定。

一旦收到Commands，指令总是按照顺序执行的，只不过有些指令要等待一段时间才能看到产生的效果。举个例子，当有两个图元排队绘制在framebuffer中时，先绘制的图元一定首先绘制在framebuffer中。

这个性质还有一个重要推论，任何查询操作或者是pixel读取操作，返回的结果一定是之前所有相关的GL指令执行完成之后的结果，除非由其他特别的明文规定。一般来说GL commands不管是作用在GL state还是framebuffer都必须完成之后，后续的commands才可以产生作用。

**[数据绑定](https://zhida.zhihu.com/search?content_id=215932379&content_type=Article&match_order=1&q=数据绑定&zhida_source=entity)在调用的时候发生**。意味着一旦指令被GL接收到，传给GL指令的数据就会被立刻解释。这意味着，如果传入指令的数据是一个[数据指针](https://zhida.zhihu.com/search?content_id=215932379&content_type=Article&match_order=1&q=数据指针&zhida_source=entity)，一旦指令被GL接收到，GL会立刻寻找指针对应的数据。指令结束之后，你再去修改这个指针对应的数据对于上一次的命令是没有效果的。

GL在3D和2D图像方面提供了直接的控制手段。包含了规范的参数，用户自定义的着色器程序（展现 平移 旋转等移动操作，光照，纹理，以及其他渲染操作）也可以实现内建函数以完成反锯齿(antialiasing)和纹理滤波(texture filtering)。**GL并没有提供任何描述或建模复杂几何图形的方法,尽管着色器可以生成这样的对象。**

总而言之，**OpenGL提供了一套机制去描述复杂的几何对象是如何被渲染的，而不是使用一套机制去描述复杂对象本身。**

**解释GL指令的模型是 Client-server形式**。这意味着一个程序(Client端)发起一个指令，这个指令由GL来解释和执行(server端)。这就意味着Client端和server端可以不在一个环境中

GL中包含两类framebuffers，一类是窗口系统提供的framebuffer，窗口系统提供的会作为默认的framebuffer。应用程序也可以创建一个framebuffer，一个上下文将会关联两个framebuffer。

GL只提供每一个API的规范，功能是什么，输入输出参数是什么，不会要求具体如何实现，以此来保证GL可以在任何设备上运行。

