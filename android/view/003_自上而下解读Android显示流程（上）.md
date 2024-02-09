首发于[移动端显示技术杂谈](https://www.zhihu.com/column/c_1286062381815140352)



写文章

![点击打开XingRay的主页](D:\my-note\android\view\assets\bd8e13804fa61e3c241c03cc6620fb59_l.jpg)

![自上而下解读Android显示流程（上）](D:\my-note\android\view\assets\v2-9d290a5182f6d220ecd17d26adaa3cf2_1440w.jpeg)

# 自上而下解读Android显示流程（上）

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l-1707456175913-94.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a-1707456175913-95.jpg)

同济大学 计算机系统结构硕士

关注

156 人赞同了该文章



当我们点击“知乎”这个应用后，它是怎么在屏幕上显示出来的？

这个问题困扰我很久了，当我刚接触显示的时候，大约是十年前的事情了，当时我连Framebuffer都不了解。尽管与显示芯片接触的越来越久，但萦绕在我心头的疑惑也并没有减少，此时大部分时间都与硬件交互，对上层的理解仍是糊里糊涂。

我当时就挺希望有人能从头到尾的介绍一下整个显示流程，可惜网上并没有这样的教程，实际接触到的同事基本分管单个模块，从上而下也很难说清。感谢曾经在联芯的日子，那时候空闲时间多了不少。大约2017年左右，使我有时间整理了一份60页的文档，这份文档如今也留在了原公司，当然**本系列文章会按照PPT的形式介绍大概流程**。

从事Android显示相关领域已经很多年了，如今越来越偏离这个领域，总心有不忍，不愿完全离开这个方向，所以一直也比较关注这个领域的发展。

## 背景知识

为了更好的了解整个框架，我们先了解一下背景知识；**因为很多人并不太了解Graphics与Display的区别，主要原因还在于PC时代的影响**。

**PC显卡与移动端GPU的区别**

![img](D:\my-note\android\view\assets\v2-e0ad6b58183253614452123efe8a1cef_1440w.webp)

*上面是ARM给出的视频流显示过程，主要介绍AFBC的带宽压缩技术*。但我们给出这张图的主要目的是为了说明PC上的显卡与移动端GPU的区别。在PC上这整个流程都在显卡内完成。

**PC的显卡 = 移动端GPU + Display Processor + Video Processor**

PC上Linux系统显示流程在2012年就已全部采用drm框架了，如下图，从框架中我们能看出drm中renderX节点就是服务于GPU（应用有权限，**内容端**），而ControlDX是服务于DisplayProcessor（系统权限，**控制端**）。

![img](D:\my-note\android\view\assets\v2-88c870ef6058a3742dd5f8b1b69acf51_1440w.webp)

Android在2018年左右也采用了这个框架，但移动端这个区分在硬件上早就是存在的。后面我们会常用到**内容端与控制端**这2个概念。

> 说句题外话，游戏对于Graphics（GPU）要求比较高，而除了Adreno、Mali少数的几个能提供GPU平台的公司（它们也同时提供DP芯片），更多的手机公司基本都是从事Display的工作。

## Android系统特点

![img](D:\my-note\android\view\assets\v2-70f073f8c8b85bd480103b9dfe93a889_1440w.webp)

我们知道Android是采用的Client-Server架构，而不是微内核架构。如上图，应用与硬件交互都需要经过binder委托给系统服务。但是有个例外——

**GPU是用户进程唯一可以直接操作的硬件**

![img](D:\my-note\android\view\assets\v2-d803ff5e3fe4dbf3227c6ce51b13ef46_1440w.webp)

这主要是为了性能考虑，毕竟绘图的99%的工作都由GPU完成。

在了解了背景知识后，我们从上至下了解整个框架。如“Android上的FreeSync——显示延时”那一篇文章提到的，Android的显示是三段式的。

![img](D:\my-note\android\view\assets\v2-c751df632646121e074e05c88e82d706_1440w.png)

关于这个系列我们将分为上中下三篇——**上篇只介绍绘制部分，也就是内容端；中篇是承上启下的SurfaceFlinger、Hwcomposer（合成）, 下篇主要讲解硬件相关的DisplayProcessor以及显示接口如LCD（显示）。**

## **显示流程**

下图是显示一帧所经过的流程，可以看出来经过5个主要模块——

![img](D:\my-note\android\view\assets\v2-0838bc62d3d5791b2293327089afb6c9_1440w.webp)

- Application--Renderer
- Frameworks/SurfaceFlinger
- Hwcomposer
- Display Driver
- Display Device

为了避免概念分歧，本文主要讲解**绘制，也就是内容端**，当然Frameworks/SurfaceFlinger属于承上启下，不可避免也会涉及到。

## 一、Application/Activity/View

关于这个概念网上已经有了很多，我们只简单介绍一下。

- Application包括4大组件：Activity、Service、Broadcast、ContentProvider

- Activity委托View负责显示：

- - TextView、EditView、Button、Custom View
  - Custom main view：setContentView(R.layout.main_activity)

**View的3个重要方法**

1. Measure：系统会先根据xml布局文件和代码中对控件属性的设置，来获取或者计算出每个View和ViewGrop的尺寸，并将这些尺寸保存下来。

*显示大小，应用可调，由服务端WMS最终计算确定（控制端）*

2. Layout：根据测量出的结果以及对应的参数，来确定每一个控件应该显示的位置。

*显示位置，应用可调，由服务端WMS最终计算确定（控制端）*

3. Draw：确定好位置后，就将这些控件绘制到屏幕上。

*显示内容，应用决定，此时才会用到GPU（内容端）*

所以，我们也可以理解，为什么onCreate里面的setContentView的时候，实际是没有分配Buffer的，真正分配Buffer的时候是始于Draw的时候。

**最重要的View——DectorView**

Android窗口主要分为两种：

1. 应用窗口——PhoneWindow

一个activity有一个主窗口，弹出的对话框也有一个窗口，Menu菜单也是一个窗口。在同一个activity中，主窗口、对话框、Menu窗口之间通过该activity关联起来。

和应用相关的窗口表示类是**PhoneWindow**，其继承于Window，针对手机屏幕做了一些优化工作，里面核心的是**mDecorView**这个变量，**mDecorView**是一个顶层的View，窗口的添加就是通过WindowManager.addView()把该mDecorView添加到WindowManager中。

![img](D:\my-note\android\view\assets\v2-8c560a8d871940f6331468dd2cc5314e_1440w.webp)

2. 公共界面的窗口

如最近运行对话框、关机对话框、状态栏下拉栏、锁屏界面等。这些窗口都是系统级别的窗口，不从属于任何应用，和activity没有任何关系。这种窗口没有任何窗口类来封装，也是直接调用WindowManager.addView()来把一个view添加到WindowManager中。

至此流程如下（具体细节不再展开，图片来自之前一个同事，借用一下）

![img](D:\my-note\android\view\assets\v2-515a6c8c065b74c1fe454ca0a38ca2b5_1440w.webp)

## **二、Framework的控制端与内容端**

**控制端**

WindowManagerService （**服务端，与绘制无关**）

- 窗口的状态、属性，如大小，位置；(WindowState，与上面的DectorView一一对应)
- View增加、删除、更新
- 窗口顺序
- Input Event 消息收集和处理等（与绘制无关，可不用关心）

SurfaceFlinger（**服务端，与绘制无关**）

- Layer的大小、位置（Layer与上面的WindowState一一对应）
- Layer的增加、删除、更新；
- Layer的zorder顺序

**内容端——绘制**

framework/base: Canvas

- SoftwareCanvas （skia/CPU）
- HardwareCanvas （hwui/GPU）

framework/base: Surface

- 区别于WMS的Surface概念，与Canvas一一对应，内容生产者

![img](D:\my-note\android\view\assets\v2-12d9c36dba2e266e60e3c79ba499fd54_1440w.webp)

framework/native:

- Surface: 负责分配buffer与填充（由上面的Surface传下来）

- SurfaceFlinger

- - Layer数据已填充好，与上面提到的Surface同样是一一对应
  - ***可见Layer这个概念即是控制端又是内容端\***
  - 当然，SF更重要的是合成，后文会继续讲解

至此流程如下（具体细节不再展开）

![img](D:\my-note\android\view\assets\v2-1cca88a218cb642431043728e579659e_1440w.webp)

## **三、概念澄清**

**在这里各种Surface，Window，View、Layer实在是太乱了，下面就区分一下这些概念。**

> Window -> DecorView-> ViewRootImpl -> WindowState -> Surface -> Layer 是一一对应的。
> 一般的Activity包括的多个View会组成View hierachy的树形结构，只有最顶层的DecorView，也就是根结点视图，才是对WMS可见的，即有对应的Window和WindowState。
> 一个应用程序窗口分别位于应用程序进程和WMS服务中的**两个Surface对象**有什么区别呢？
> 虽然它们都是用来操作位于SurfaceFlinger服务中的同一个Layer对象的，不过，它们的操作方式却不一样。具体来说，就是位于应用程序进程这一侧的Surface对象负责绘制应用程序窗口的UI，即往应用程序窗口的图形缓冲区填充UI数据，而位于WMS服务这一侧的Surface对象负责设置应用程序窗口的属性，例如位置、大小等属性。
> 这两种不同的操作方式分别是通过C++层的Surface对象和SurfaceControl对象来完成的，因此，位于应用程序进程和WMS服务中的两个Surface对象的用法是有区别的。之所以会有这样的区别，是因为**绘制应用程序窗口是独立的，由应用程序进程来完即可，而设置应用程序窗口的属性却需要全局考虑，即需要由WMS服务来统筹安排**。

## **四、软件绘制**

软件绘制就是使用CPU去绘制，Skia之前就是一个CPU实现的2D库，当然现在也用GPU去实现了。

硬件加速就是使用GPU去绘制，这看起来似乎天经地义，其实Android并不是一开始就支持的，而是在Android 3.0之后才开启了默认硬件加速，**如果你的应用target API < 14，还是默认使用软件绘制。**

**但在Android 3.0之前，GPU也是存在的，其意义何在？**

> 1）虽然UI是默认软件绘制的，但是并不是说所有的应用都不是硬件绘制的，即使在Android3.0之前，BootAnimation也是一个典型的GPU应用；Camera的后处理特效；还有大部分游戏都是硬件加速的。
> 2）SurfaceFlinger是用来合成Layer的，而且是用GPU合成Layer的，这也需要GPU的功能，只需2D模块就够了。其实在早期的GPU上，比如imagination，vivant都是有这个2D模块的，尽管现在都已经取消了，甚至这2个GPU已经消失在大众的视野了。
> 3）在Android3.0之前，你如果没有GPU硬件，那么SF是否就无法启动呢？遇到BootAnimation是否就挂掉了呢？这到也不是，当时Android给了一个模拟GPU的libGLES_android库，但是只支持固定管线Opengles 1.1，主要用于调试用。合成Layer是没有什么问题，BootAnimation也没啥问题。有问题的是复杂的游戏等，所以GPU还是得标配。

**软件绘制的典型场景**

> 1. 鼠标---直接调用Surface->lock
> 2. Mali对于小layer的旋转
> 3. Target API < 14等应用强制使用软件绘制的情形

软件绘制的Buffer管理

![img](D:\my-note\android\view\assets\v2-15e3f2b1645124342d2c25af950e20f7_1440w.webp)

软件绘制流程比较简单

> 在Android应用程序进程这一侧，每一个窗口都关联有一个Surface。每当窗口需要绘制UI时，就会调用其关联的Surface的成员函数lock获得一个Canvas，其本质上是向SurfaceFlinger服务dequeue一个Graphic Buffer。
> Canvas封装了由Skia提供的2D UI绘制接口，并且都是在前面获得的Graphic Buffer上面进行绘制的，这个Canvas的底层是一个bitmap，也就是说，绘制都发生在这个Bitmap上。绘制完成之后，Android应用程序进程再调用前面获得的Canvas的成员函数unlockAndPost请求显示在屏幕中，其本质上是向SurfaceFlinger服务queue一个Graphic Buffer，以便SurfaceFlinger服务可以对Graphic Buffer的内容进行合成，以及显示到屏幕上去。

![img](D:\my-note\android\view\assets\v2-7a1d5f460feb6300413565c0f7433e6b_1440w.webp)

**如何确定是否软件绘制**

> CPU负载会相对高；
> Systrace无RenderThreader线程；
> Systrace中无eglSwapBuffer等opengl api （opengl在Android尚无无软件实现）
> Dumpsys gfxinfo pid 得不到相应的绘制信息

**如何强制使用软件或硬件加速**

其实我们真正有可能用到的是关闭或开启硬件加速，比如在调试的过程中看下是否是GPU的兼容性引起的问题。

> **1. 针对应用**
> 在Android中，可以在四个不同层次上打开或关闭硬件加速：
> **Application** ：< application android:hardwareAccelerated=“false”>
> **Activity**：< activity android:hardwareAccelerated=" false ">
> **Window**：getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED); //此为打开，
> **View**：view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
> 在这四个层次中，**应用和Activity是可以选择的，Window只能打开，View只能关闭**。
> **设置targetSDKVersion**
> 在apk的AndroidManifest中，如果指定了minSDKVersion&targetSDKVersion=7，会使得应用无法使用硬件加速进行绘图。如果targetSDKVersion<14或者不设置都是默认软件绘图。
> **2. 针对整个系统**
> 关闭硬件加速：
> [BoardConfigVendor.mk](https://link.zhihu.com/?target=http%3A//boardconfigvendor.mk/) file ：Set the USE_OPENGL_RENDERER attribute to False.
> 使能硬件加速：
> Settings→Developer options→Force GPU rendering： This forces the apps to use the GPU for 2D drawing.

## 五、硬件绘制

硬件绘制相对比较复杂，在这里只简单带过。

**硬件buffer管理与软件绘制基本是一致的。**

![img](D:\my-note\android\view\assets\v2-a857e8690fbb88578460a6ff1444f449_1440w.webp)

> 硬件加速渲染和软件渲染一样，在开始渲染之前，都是要先向SurfaceFlinger服务dequeue一个Graphic Buffer。不过对硬件加速渲染来说，这个Graphic Buffer会被封装成一个ANativeWindow，并且传递给Open GL进行硬件加速渲染环境初始化。
> 在Android系统中，ANativeWindow和Surface可以是认为等价的，只不过是ANativeWindow常用于Native层中，而Surface常用于Java层中。 Open GL获得了一个ANativeWindow，并且进行了硬件加速渲染环境初始化工作之后，Android应用程序就可以调用Open GL提供的API进行UI绘制了，绘制出来内容就保存在前面获得的Graphic Buffer中。
> 当绘制完毕，Android应用程序再调用libegl库（一般由第三方提供）的eglSwapBuffer接口请求将绘制好的UI显示到屏幕中，其本质上与软件渲染过程是一样的。

Android自带的libGLES_android如下，但实际使用的GPU驱动mali或者adreno稍有不同，mali的是先dequeueBuffer再queueBuffer。adreno完全闭源。

```text
EGLBoolean egl_window_surface_v2_t::swapBuffers() {
…………
nativeWindow->queueBuffer(nativeWindow, buffer, -1);//此处也有等待fence
buffer = 0;

// dequeue a new buffer
int fenceFd = -1;
if (nativeWindow->dequeueBuffer(nativeWindow, &buffer, &fenceFd) == NO_ERROR) {
sp<Fence> fence(new Fence(fenceFd));
if (fence->wait(Fence::TIMEOUT_NEVER)) {
nativeWindow->cancelBuffer(nativeWindow, buffer, fenceFd);
return setError(EGL_BAD_ALLOC, EGL_FALSE);
}
…………
}
```

整个流程如下（首帧）：

![img](D:\my-note\android\view\assets\v2-6f0160c861330af95792cd6ba6b18807_1440w.webp)

**MainThread和RenderThread的分离**
在Android 5.0之前，Android应用程序的Main Thread不仅负责用户输入，同时也是一个OpenGL线程，也负责渲染UI。通过引进Render Thread，我们就可以将UI渲染工作从Main Thread释放出来，交由Render Thread来处理，从而也使得Main Thread可以更专注高效地处理用户输入，这样使得在提高UI绘制效率的同时，也使得UI具有更高的响应性。

![img](D:\my-note\android\view\assets\v2-a6a2182b25f71c0070c39dc04531c23f_1440w.webp)

对于上层应用而言，UI thread仍然是Main Thread，它并不清楚Render Thread的存在，而**对于SurfaceView，UI thread不是Main Thread，而是重新启用了一个新的线程**。

![img](D:\my-note\android\view\assets\v2-b903492c9ce0e51b2a891931a56946ec_1440w.webp)

> 在Android应用程序窗口中，每一个View都抽象为一个Render Node，而且如果一个View设置有Background，这个Background也被抽象为一个Render Node。这是由于在OpenGLRenderer库中，并没有View的概念，所有的一切可绘制的元素都抽象为一个Render Node。
> 每一个Render Node都关联有一个Display List Renderer。这里又涉及到另外一个概念——**Display List**。Display List是一个绘制命令缓冲区。也就是说，当View的成员函数onDraw被调用时，我们调用通过参数传递进来的Canvas的drawXXX成员函数绘制图形时，我们实际上只是将对应的绘制命令以及参数保存在一个Display List中。接下来再通过Display List Renderer执行这个Display List的命令，这个过程称为Display List Replay。
> 引进Display List的概念有什么好处呢？主要是两个好处。
> 第一个好处是在下一帧绘制中，如果一个View的内容不需要更新，那么就不用重建它的Display List，也就是不需要调用它的onDraw成员函数。
> 第二个好处是在下一帧中，如果一个View仅仅是一些简单的属性发生变化，例如位置和Alpha值发生变化，那么也无需要重建它的Display List，只需要在上一次建立的Display List中修改一下对应的属性就可以了，这也意味着不需要调用它的onDraw成员函数。这两个好处使用在绘制应用程序窗口的一帧时，省去很多应用程序代码的执行，也就是大大地节省了CPU的执行时间。

虽然从Android3.0开始到Android8.0，每一次都会对硬件绘制升级，也显示了硬件绘制的重要地位，但基本原理没有变，还是利用了DisplayList。

简单的TextView的DisplayList命令是这样的

```text
Save 3
DrawPatch
Save 3
ClipRect 20.00, 4.00, 99.00, 44.00, 1
Translate 20.00, 12.00
DrawText 9, 18, 9, 0.00, 19.00, 0x17e898
Restore
RestoreToCount 0
```

DisplayList最后的GPU命令如下，**如果是游戏通常直接调用下面类似的命令**，这样效率更高。

```text
glUniformMatrix4fv(location = 2, count = 1, transpose = false, value = [1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0])
glBindBuffer(target = GL_ARRAY_BUFFER, buffer = 0)
glGenBuffers(n = 1, buffers = [3])
glBindBuffer(target = GL_ELEMENT_ARRAY_BUFFER, buffer = 3)
glBufferData(target = GL_ELEMENT_ARRAY_BUFFER, size = 24576, data = [ 24576 bytes ], usage = GL_STATIC_DRAW)
glVertexAttribPointer(indx = 0, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0xbefdcf18)
glVertexAttribPointer(indx = 1, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0xbefdcf20)
glVertexAttribPointerData(indx = 0, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0x??, minIndex = 0, maxIndex = 48)
glVertexAttribPointerData(indx = 1, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0x??, minIndex = 0, maxIndex = 48)
glDrawElements(mode = GL_MAP_INVALIDATE_RANGE_BIT, count = 72, type = GL_UNSIGNED_SHORT, indices = 0x0)
glBindBuffer(target = GL_ARRAY_BUFFER, buffer = 2)
glBufferSubData(target = GL_ARRAY_BUFFER, offset = 768, size = 576, data = [ 576 bytes ])
glDisable(cap = GL_BLEND)
glUniformMatrix4fv(location = 2, count = 1, transpose = false, value = [1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 33.0, 0.0, 1.0])
glVertexAttribPointer(indx = 0, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0x300)
glVertexAttribPointer(indx = 1, size = 2, type = GL_FLOAT, normalized = false, stride = 16, ptr = 0x308)
glDrawElements(mode = GL_MAP_INVALIDATE_RANGE_BIT, count = 54, type = GL_UNSIGNED_SHORT, indices = 0x0)
eglSwapBuffers()
```

## 总结

**GPU是应用绘制的主力, 我们称之为内容端，也就是真正的Buffer；而控制端则由DisplayProcessor完成；无论是Frameworks中的WMS还是SF都是在解耦这两个功能**。

但是你可能会觉得奇怪，**SF中用GPU的地方也很多啊，这属于内容端还是控制端呢？SF中GPU是绘制还是合成？**

显示系统中还有另外一个让人混淆的地方就是谁来绘制，谁来合成，谁来显示的问题。这与上面的内容端与控制端又属于2个维度，其中我们可以认为绘制属于内容端，而显示属于控制端，而合成则介于两者之间。详见下一篇文章。

硬件绘制流程更详细版本：[沧浪之水：Android GPU硬件加速渲染流程（上）](https://zhuanlan.zhihu.com/p/464492155)

## 参考：

[https://blog.csdn.net/luoshengyang/article/details/45601143](https://link.zhihu.com/?target=https%3A//blog.csdn.net/luoshengyang/article/details/45601143)