# OGL 图像渲染 Framebuffer笔记

[OpenGL 4.6 (Core Profile) - May 5, 2022 (khronos.org)](https://link.zhihu.com/?target=https%3A//registry.khronos.org/OpenGL/specs/gl/glspec46.core.pdf) Chapter 9

## 前言

所有GL的渲染操作都是向framebuffer对象进行渲染的。如果想读一些渲染后的结果数据也是需要从framebuffer对象中拿。

GL提供default framebuffer，default framebuffer的储存大小，维度，格式等信息完全取决于窗口管理系统，不可能通过OGL去改变default framebuffer的属性。这表现在，default buffer是在wglMakeCurrent阶段创建的，今后OGL的API不能去改变default framebuffer的状态。

本章介绍framebuffer的[数据结构](https://zhida.zhihu.com/search?content_id=222927888&content_type=Article&match_order=1&q=数据结构&zhida_source=entity)概览，描述如何创建，摧毁，修改framebuffer相关的state，并且描述用户如何创建一个属于自己的framebuffer object。

## 概览

显然，一帧数据是由众多个像素点组成的。每一个像素点由多个bits来组成（例如RGB888一个word存放一个像素点颜色信息），具体到一个像素点由几个bits组成由以下几点要素决定

1. openGL的实现代码
2. 选择的frambuffer类型
3. 创建framebuffer的时候传入的参数

一个像素点中所有相同类型的bit的集合我们定义为bitplane，而一帧中所有像素的bitplane又组成了新的集合，称之为logical buffer，它可以是

1. color buffer : 储存颜色相关的信息 例如RGB888，每八个bit决定一种颜色的强度
2. depth buffer：储存深度相关的信息
3. stencil buffer：储存模板相关的信息

重点说说color buffer

对于**default framebuffer** ，有四个color buffer，分别是

1. front right uffer
2. front right buffer
3. back left buffer
4. back right buffer

如果设备支持双缓存，那么渲染首先会渲染到 back buffer中，然后通过swap交换back和front的内容。如果设备不支持双缓存，那只能渲染到front buffer中。

值得注意的时候，所有的color buffers的格式都应当一致，default framebuffer默认会有一个color buffer以及一个depth buffer 和 stencil buffer。

对于**用户创建的framebuffer** 在窗口系统中完全是不可见的，其对应的color buffer完全没有和default framebuffer的color buffer有任何关联。用户创建的framebuffer是通过关联一些texture或者renderbuffers到framebuffer的attachment节点中来完成渲染的。任何一个用户创建的framebuffer都包含了多个color buffer 附件的位置以及一个depth buffer以及一个stencil buffer。

color buffer当中每个pixel的格式是由四个部分组成 R G B A，每一个通道的bit长度以及如何使用bit来表达颜色,我们可以把他理解为颜色格式Color Format。

default framebuffer的时候Color Format是固定的，在framebuffer创建的时候就固定下来，通过ChoosePixelFormat以及pfi格式来选定。

用户创建的framebuffer 对应的Color Format可以由对应的attachment的格式来决定。

GL拥有两个framebuffer一个是用来写的draw framebuffer，一个是用来读的read framebuffer。他们常常使用同一个framebuffer对象。

## framebuffer object

因为我们支持将texture以及renderbuffer放入framebuffer的attachment中，因此我们完全可以提供 **off-screen rendering**的功能。进一步去说这个feature，实际就是GL是支持将渲染的结果放到用户指定的 texture中的。也就是**render to texture**功能。

framebuffer的create和bind的过程省略，也就是相同的方式调用glGen* 以及glBind*。

下面小结default framebuffer与用户创建的framebuffer的区别

1. 最重要的区别：用户创建的framebuffer支持attach不同的Logic buffer(也就是renderbuffer或者texture)可以随时attach也可以随时detache。并且attachment的大小和格式也可以随时使用GL去修改。这个过程是完全不会受到窗口系统的事件干扰的，例如，重新选择像素格式，窗口大小变化，或者显示模式变化。
2. pixel ownership 测试永远成功。framebuffer objects 拥有所有像素。
3. color buffer相对独立
4. color buffer只与COLOR_ATTACHMENT0-~相关，depth buffer只与DEPTH_ATTACHMENT相关stencil buffer只与STENCIL_ATTACHEMNT相关



