# OpenGL Context



## OpenGL Context



OpenGL Context与窗口系统相关，所以在不同平台上创建方式都不一样。

1 Windows上使用的是WGL
2 Linux上有GLX和EGL两种，分别对应两种窗口系统XWindow和Wayland
3 Mac上使用的是AEL
4 IOS上使用的是EAGL，对应的是OpenGL ES
5 Android上使用的是EGL

EGL 是OpenGL官方Khronos推出的，既可以使用用于OpenGL Context，也可以用于OpenGL ES Context。Google的ANGLE项目使用了EGL，支持各个平台，提供将OpenGL ES/WebGL转译为Vulkan、DirectX、Metal 等系统图形API，Chrome浏览器中的WebGL使用了ANGLE。



## Context与线程

1 一条线程同时只能绑定一个OpenGL Context，Context也只能同时绑定到一个线程，OpenGL在绑定的线程当中相当于全局的。如果线程没有绑定Context，则不能调用OpenGL函数。

2 OpenGL Context可以在任意线程创建，创建完成后可以将Context绑定到任意线程，也可以从线程解除绑定，然后从新绑定到新的线程。绑定和解绑需要使用MakeCurrent函数，WGL、GLX、EGL、AGL都有自己对应的MakeCurrent函数。WGL的MakeCurrent函数是wglMakeCurrent。

3 OpenGL Context虽然创建的时候需要一个窗口，但实际上Context不要任何窗口相关，也就是说创建完成后可将Context绑定到其他窗口上，这样可以实现一个OpenGL Context在多个不同的窗口上渲染。

4 EGL创建Context时可以使用pBuffer，使用eglCreatePbufferSurface函数。这让EGL可以在没有窗口系统的服务器系统，以及Docker中使用。



## 多线程使用OpenGL

### 消息队列

消息队列是简单直接的方法，通过创建一条线程，让后将OpenGL Context绑定到线程，其他线程通过发生消息的方式调用OpenGL函数，Android中OpenGL ES默认使用的是这种方法。消息队列可以是阻塞队列Blocking Queue实现。



**优点**：实现简单，使用方便直观，调试简单。



**缺点**：内存管理比较麻烦，例如主线程要更新UBO内存，要么等等渲染线程map一个指针回来，要么发生整个内存数据到渲染线程，但这时主线程不知道渲染线程什么时候更新完UBO，这导致难以回收和复用这内存。



例子：CppTest/openGL/OpenGLQueue.cpp · 小康6650/StudyProject - Gitee.com  

https://gitee.com/Kyle12/StudyProject/blob/master/CppTest/openGL/OpenGLQueue.cpp



### 在不同线程绑定Context

这种方式是每次需要使用OpenGL时则绑定Context，使用完后又解绑。为了防止多个线程同时绑定Context，绑定时需要加锁，解绑时需要解锁。因为同时只有一个线程可以使用OpenGL，一个线程在使用时另一个线程只能等待。所以如果渲染线程很快，可能会导致主线程等待绑定OpenGL而产生卡顿。这种方法适合帧率不高的情况，直播软件OBS Studio使用的就是这种方法。

例子：CppTest/openGL/OpenGLLock.cpp · 小康6650/StudyProject - Gitee.com

https://gitee.com/Kyle12/StudyProject/blob/master/CppTest/openGL/OpenGLLock.cpp



### 不同 context 之间共享数据

这种方式是为每个线程创建一个OpenGL Context，然后在不同Context之间共享OpenGL对象。

这种方式程序需要负责OpenGL同步，防止一个Context在渲染纹理，而另一个线程在读取纹理。





**Context理解**　　

　　OpenGL Context，中文解释就是OpenGL的上下文。OpenGL只是图形API，它只负责渲染，渲染指令执行所需要的那些东西就是Context，比如：

1. 渲染到哪个缓存？缓存参数是什么？
2. 渲染配置是什么？比如当前的渲染颜色、是否进行光照计算等等；

　　这就好比一个画家作图，OpenGL就是这个画家，而画家作画需要的画笔、画布等东西就是Context。Context的切换就像画家同时在作多幅画，当要到另一幅画前绘画时，画家需要放下原来的画笔，拿起这幅画所需的画笔。

　　当然OpenGL的Context不只是包含上述内容，具体内容可以查看OpenGL的Context的结构体。

　　总的来说，OpenGL的Context记录了OpenGL渲染需要的所有信息，它是一个大的结构体，它里面记录了当前绘制使用的颜色、是否有光照计算以及开启的光源等非常多我们使用OpenGL函数调用设置的状态和状态属性。在OpenGL 3.0版本之前，OpenGL创建Context都是一致的，随着升级会新增一些内容（例如从OpenGL1.1升级到1.5，会新增一些状态变量或者属性，并添加一些设置这些内容的函数），整体上来说没有什么大的变化。但是从OpenGL 3.0开始，OpenGL为了摆脱历史的“包袱”，想要彻底的废弃掉之前的许多特性，但是无奈市面上已经有大量依赖OpenGL之前版本的代码，导致OpenGL维护小组的这一想法难以付诸实施，于是在OpenGL 3.1开始引入了OpenGL Context的一些分类，比如引入了CoreProfile等概念，之后随着版本发展到3.3，一切算是确定下来。

　　所以到了OpenGL3.3之后，OpenGL的context profile分为了两个版本，core pfofile和compatibility profile，前者表示删除任何标记为deprecated（弃用）的功能，后者则表示不删除任何功能。context除了core profile（核心渲染模式）和compatibility profile（立即渲染模式）外，还有一种模式：foward compatibility，这个表示所有标记为deprecated的函数都禁用，这个模式只对opengl3.0及以上的版本有效。但这个选项对OpenGL 3.2+ compatibility Profile Context没有任何作用。

### Forward compatibility

> A context, of version 3.0 or greater, can be created with the "forward compatibility" bit set. This will cause, for the given profile, all functionality marked "deprecated" to be removed. You can combine the forward compatibility bit with core and compatibility contexts.
>
> For 3.0, this means that all deprecated functionality will no longer be available. This simulates the 3.1 experience.
>
> For 3.1, this means that any *remaining* deprecated functionality (things deprecated in 3.0 but *not* removed in 3.1) will be removed. Basically, wide-lines. Also, you're not likely to see implementations offer ARB_compatibility if you pass forward compatibility.
>
> For 3.2+ compatibility, it should mean nothing at all. Since no functionality is marked deprecated in the compatibility profile, the forward compatibility bit removes nothing.
>
> For 3.2+ core, it again means that all functionality that is still deprecated (wide-lines) will be removed.
>
> **Recommendation:** You should use the forward compatibility bit only if you need compatibility with MacOS. That API requires the forward compatibility bit to create any core profile context.

 

**context指定方式**

　　我们常用glut或glfw来作为学习OpenGL的图形界面，glut有两个版本，分别是原始的glut和freeglut，其中glut是由Mark Kilgard编写，不过现在已经不维护了，而freeglut，由帕维尔·W. Olszta、安德烈亚斯·乌姆巴赫、史蒂夫·贝克编写维护，2015年更新到3.0版本，基本也处于停滞更新的状态；glfw则是另一个轻量级的图形界面框架，托管在 [www.glfw.org](http://www.glfw.org/)，现在非常的活跃，如果新入门可以优先考虑glfw，相关的教程网上也比较多。**
**

　　注意在这两个图形界面框架中，对于forward compatibility都是按一个选项来配置的，也就是说forward compatibility针对的是标记为deprecated的功能，而不是profile。

　　在glut指定context的方式：

```
glutInitContextVersion(3,3);``glutInitContextProfile(GLUT_CORE_PROFILE);``//glutInitContextFlags(GLUT_FORWARD_COMPATIBLE);　　//设置forward compatibility
```

 

　　glfw的context指定方式位：

```
glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR,3);``glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR,3);``glfwWindowHint(GLFW_OPENGL_PROFILE,GLFW_OPENGL_CORE_PROFILE);``glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT,GL_TRUE);　　``//设置forward compatibility
```

　　其中最后一行在macOS中必须指定。

## 线程私有

　　OpenGL的绘制命令都是作用在当前的Context上，Current Context是一个线程私有（thread-local）的变量，也就是说如果我们在线程中绘制，那么需要为每个线程指定一个Current Context的，而且多个线程不能同时指定同一个Context为Current Context。

　　

**参考资料** 

　　**[\**https://www.khronos.org/opengl/wiki/OpenGL_Context\**](https://www.khronos.org/opengl/wiki/OpenGL_Context)**

　　**https://blog.csdn.net/csxiaoshui/article/details/79032464**

