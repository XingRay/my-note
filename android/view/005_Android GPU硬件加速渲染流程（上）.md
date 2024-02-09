![Android GPU硬件加速渲染流程（上）](D:\my-note\android\view\assets\v2-e3d8be9a0bde1588ca5002135320c9f3_1440w.png)

# Android GPU硬件加速渲染流程（上）

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l-1707456236479-186.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a-1707456236479-187.jpg)

同济大学 计算机系统结构硕士

关注

38 人赞同了该文章





目录

收起

1、基本概念

1.1 Window（窗口）：

1.2 View（视图）：

1.3 Window与View之间关系

2. 软件绘制

2.1 Actvity窗口绘制流程

2.2软件绘制流程

2.3何时使用软件绘制

3硬件绘制

3.1 硬件绘制流程

3.2 硬件加速渲染的环境初始化过程分析

3.2.1 MainThread和RenderThread的分离

3.2.1.1 SurfaceView简介

3.2.1.2 SurfaceTexture、TextureView、GLSurfaceview的区别与联系

3.2.2 OpenGL环境初始化

3.2.3 系统预加载资源地图集

*本文希望通过两篇文章，详细介绍Android上层的**硬件加速渲染流程**，大约2万字，此为上篇。*

## 1、基本概念

### 1.1 Window（窗口）：

Android窗口主要分为两种：

1）一是应用窗口：一个activity有一个主窗口，弹出的对话框也有一个窗口，Menu菜单也是一个窗口。在同一个activity中，主窗口、对话框、Menu窗口之间通过该activity关联起来。

和应用相关的窗口表示类是PhoneWindow，其继承于Window，针对手机屏幕做了一些优化工作，里面核心的是mDecorView这个变量，mDecorView是一个顶层的View，窗口的添加就是通过调用getDecorView()获取到mDecorView并且调WindowManager.addView()把该View添加到WindowManager中。但也有例外，比如悬浮窗口虽然与activity相关联，但并不是PhoneWindow，直接调用通过WindowManager.addView()添加。

如果我们想给所有的应用都加一个比如最大、最小化、关闭的导航条，那只需更改mDecorView即可（Android N为支持多窗口将DecorView从PhoneWindow中分离成一个单独的文件）。

2）二是公共界面的窗口：如最近运行对话框、关机对话框、状态栏下拉栏、锁屏界面等。这些窗口都是系统级别的窗口，不从属于任何应用，和activity没有任何关系。这种窗口没有任何窗口类来封装，也是直接调用WindowManager.addView()来把一个view添加到WindowManager中。

### 1.2 View（视图）：

```text
1. <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
2.  xmlns:tools="http://schemas.android.com/tools"
3.  android:layout_width="match_parent"
4.  android:layout_height="match_parent"
5.  tools:context="com.example.firstapp.MainActivity" >
6.  
7.  <TextView
8.  android:layout_width="wrap_content"
9.  android:layout_height="wrap_content"
10.  android:text="@string/hello_world" />
11. </RelativeLayout>
```

从一个简单的layout说起，其包含2个View：RelativeLayout、TextView，其中RelativeLayout为ViewGroup，TextView为窗口控件，直接继承View，它们是以树形结构组织在一起形成整个窗口的UI的。（如果作为应用窗口，还隐形包括一个DecorView）

![img](D:\my-note\android\view\assets\v2-b2710eba4583569fd316657e46814c59_1440w.webp)

图1 应用窗口结构示意图以及DecorView、TextView的类关系图

### 1.3 Window与View之间关系

应用客户端：**Window**（可包含多个**View**，一个PhoneWindow对应一个DecorView）

Framework客户端： **ViewRootImpl**，（同一应用多个窗口可能共用WindowManager，比如dialog的mWindowManager变量其实就是Activity对象的mWindowManager变量，从而获得mParentWindow,从而与Activity相关联*[http://blog.csdn.net/mr_liabill/article/details/49966479](https://link.zhihu.com/?target=http%3A//blog.csdn.net/mr_liabill/article/details/49966479)* ）

Framework服务端：**WindowState**

SurfaceFlinger 客户端：**Surface**（SurfaceControl与Surface 一一对应，是其内部变量）

SurfaceFlinger 服务端：**Layer**

上述***Window -> ViewRootImpl -> WindowState -> Surface -> Layer\*** 是一一对应的。

一般的Activity包括的多个View会组成View hierachy的树形结构，只有最顶层的DecorView，也就是根结点视图，才是对WMS可见的，即有对应的Window和WindowState

一个应用程序窗口分别位于应用程序进程和WindowManagerService服务中的两个Surface对象有什么区别呢？虽然它们都是用来操作位于SurfaceFlinger服务中的同一个Layer对象的，不过，它们的操作方式却不一样。具体来说，就是位于应用程序进程这一侧的Surface对象负责绘制应用程序窗口的UI，即往应用程序窗口的图形缓冲区填充UI数据，而位于WindowManagerService服务这一侧的Surface对象负责设置应用程序窗口的属性，例如位置、大小等属性。这两种不同的操作方式分别是通过C++层的Surface对象和SurfaceControl对象来完成的，因此，位于应用程序进程和WindowManagerService服务中的两个Surface对象的用法是有区别的。之所以会有这样的区别，是因为绘制应用程序窗口是独立的，由应用程序进程来完即可，而设置应用程序窗口的属性却需要全局考虑，即需要由WindowManagerService服务来统筹安排。

SurfaceFlinger中Layer主要的任务是合成，其内容已经由客户端绘制完成，使用GPU合成则调用其OnDraw函数，使用Display合成该函数是不会调用的，仅仅设置一下位置大小传递给hwcomposer即可。Layer内容更新由Layer的成员变量mSurfaceFlingerCounsumer->updateTexImage完成（GLCounsumer的默认updateTexImage是需要绑定纹理的），SurfaceFlingerCounsumer覆盖了该函数，其只是通过acquireBuffer更新一下buffer,具体绑定纹理是在OnDraw完成的，对于非GPU合成的Layer来说OnDraw不会调用也就不会绑定纹理，其在SurfaceFlinger中的工作量相对是很少的。

其中一个窗口从添加到显示可用以下时序图2表示：

![img](D:\my-note\android\view\assets\v2-32d74996bc288c8edc297c1fdde6fe2d_1440w.png)

图2 窗口显示时序图

其中addView的调用流程如图3,其中scheduleTraversals会注册一个VSYNC等待刷帧：

```text
1.  mChoreographer.postCallback(
2.  Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
```

Vsync的回调函数最终会调用performTraversals，其分为三步，分别是测量performMesure、布局performLayout和绘制performDraw。

![img](D:\my-note\android\view\assets\v2-b99b8d62fb3da63f35795b99ed70fe2f_1440w.webp)

图3 addView时序图

通过系统的Systrace我们可以看下Setting应用的简单调用关系，其中Choreographer#doFrame即是Vsync的回调函数，真正负责绘制的也并不是主线程，而是RenderThread，这也是硬件绘制的一个标志，后面会详细介绍。

![img](D:\my-note\android\view\assets\v2-f324fc79eda04f6a50d966a60a6d1c45_1440w.webp)

图4 Setting应用启动的Systrace

## 2. 软件绘制

### 2.1 Actvity窗口绘制流程

（以下参考[http://blog.csdn.net/luoshengyang/article/details/45601143](https://link.zhihu.com/?target=http%3A//blog.csdn.net/luoshengyang/article/details/45601143)）

如上节所述，Activity窗口的UI绘制操作分为三步来走，分别是测量、布局和绘制。

**1. 测量**：为了能告诉父视图自己的所占据的空间的大小，所有控件都必须要重写父类View的成员函数onMeasure。

**2. 布局**：前面的测量工作实际上是确定了控件的大小，但是控件的位置还未确定。控件的位置是通过布局这个操作来完成的。 控件是按照树形结构组织在一起的，其中，子控件的位置由父控件来设置，也就是说，一般只有容器类控件才需要执行布局操作，这是通过重写父类View的成员函数onLayout来实现的。从Activity窗口的结构可以知道，它的顶层视图是一个DecorView，这是一个容器类控件。Activity窗口的布局操作就是从其顶层视图开始执行的，每碰到一个容器类的子控件，就调用它的成员函数onLayout来让它有机会对自己的子控件的位置进行设置，依次类推。

我们常见的FrameLayout、LinearLayout、RelativeLayout、TableLayout和AbsoluteLayout，都是属于容器类控件，因此，它们都需要重写父类View的成员函数onLayout。尽管TextView控件不是容器类控件，但它仍然重写了父类View的成员函数onLayout以达到对齐目的。

**3. 绘制**：有了前面两个操作之后，控件的位置的大小就确定下来了，接下来就可以对它们的UI进行绘制了。控件(非容器类)为了能够绘制自己的UI，必须要重写父类View的成员函数onDraw。一个窗口的所有控件的UI都是绘制在窗口的绘图表面上的，即一个窗口的所有控件的UI数据都是填写在同一块图形缓冲区中。

这里我们也可以看出View和Window并不是一一对应的，一个Window可包含多个View。这也是可以理解的，比如：DecorView和TextView，如果分配2个Window，则存在极大的浪费和没有意义，因为DecorView包含TextView，这相当于TextView将有2个备份。从后面章节可以看到这两个View完全可以对应2个RenderNode (只是一些渲染命令列表)。

ViewRootImpl 中绘制函数调用关系：

performTraversals -> performDraw -> **draw**

![img](D:\my-note\android\view\assets\v2-2d0c795406f2c6512c5c89283f5ae5d7_1440w.png)

### 2.2软件绘制流程

![img](D:\my-note\android\view\assets\v2-d2c6ef5f6584cf0708ab4f59e7bcbca7_1440w.png)

在支持Android应用程序UI硬件加速渲染之前，Android应用程序UI的绘制是以软件方式进行的。

![img](D:\my-note\android\view\assets\v2-0f4ffdf53bc8772520bb350b2dd26c2c_1440w.webp)

图5 Android应用程序UI软件渲染过程

如图5，在Android应用程序进程这一侧，每一个窗口都关联有一个Surface。每当窗口需要绘制UI时，就会调用其关联的Surface的成员函数lock获得一个Canvas，其本质上是向SurfaceFlinger服务dequeue一个Graphic Buffer。Canvas封装了由Skia提供的2D UI绘制接口，并且都是在前面获得的Graphic Buffer上面进行绘制的，这个Canvas的底层是一个bitmap，也就是说，绘制都发生在这个Bitmap上。绘制完成之后，Android应用程序进程再调用前面获得的Canvas的成员函数unlockAndPost请求显示在屏幕中，其本质上是向SurfaceFlinger服务queue一个Graphic Buffer，以便SurfaceFlinger服务可以对Graphic Buffer的内容进行合成，以及显示到屏幕上去。

看下SurfaceFlinger端对应lock的描述

```c
29. status_t Surface::lock(
30.  ANativeWindow_Buffer* outBuffer, ARect* inOutDirtyBounds)
31. {
32.  ……..
33.  if (!mConnectedToCpu) { //此处说明我们要使用CPU绘制
34.      int err = Surface::connect(NATIVE_WINDOW_API_CPU);
35.      if (err) {
36.          return err;
37.       }
38.       // we're intending to do software rendering from this point
39.       setUsage(GRALLOC_USAGE_SW_READ_OFTEN |GRALLOC_USAGE_SW_WRITE_OFTEN); 
40.   }
41.   ……
42. }
```

接下来，我们看绘制的中间过程：View中draw函数

```c
1. public void draw(Canvas canvas) {  
2.  ......  
3.  // Step 1, draw the background, if needed 
4.  if (!dirtyOpaque) {  
5.      drawBackground(canvas);  
6.   }  
7.         ......  
8.  // Step 2, save the canvas' layers 
9.         ......  
10.  // Step 3, draw the content 
11.  if (!dirtyOpaque) onDraw(canvas);  
12.  // Step 4, draw the children 
13.  dispatchDraw(canvas);  //viewGroup, canvas会传递下去，一个窗口有一个canvas
14.  // Step 5, draw the fade effect and restore layers 
15.  ......  
16.  // Step 6, draw decorations (scrollbars) 
17.  onDrawScrollBars(canvas);  
18.   ......  
19.  }  
```

一个View的主要UI是由子类实现的成员函数onDraw绘制的。这个成员函数通过参数canvas可以获得一个Canvas，然后就调用这个Canvas提供的API就可以绘制一个View的UI。这意味着对一个View来说，当它的成员函数onDraw被调用时，它是不需要区别它是通过硬件渲染还是软件渲染的，对于硬件绘制则为DisplayListCanvas。如果当前正在处理的View是一个View Group，那么它的子View是通过View类的成员函数dispatchDraw来递归绘制的，一个窗口只有一个Canvas，后面会有更进一步的描述。

### 2.3何时使用软件绘制

那么对于TextView或者SurfaceView，我们使用哪种绘制方式呢？答案是不一定，这要看场景，除了TextureView（必为硬件绘制）之外其他View是由应用或系统决定，这也是本节要讨论的问题。目前应用默认使用硬件加速，但硬件加速也不是每一个需要绘制UI的进程都必需的，原因有二：

> 1. 并不是所有的Canvas API都可以被GPU支持。如果应用程序使用到了这些不被GPU支持的API，那么就需要禁用硬件加速渲染；比如对于一些不经过java层直接调用Skia库绘制的UI；再比如应用中直接通过mSurface.lockCanvas申请的Canvas。
> 2. 支持硬件加速渲染的代价是增加了内存开销。例如，只是硬件加速渲染环境初始化这一操作，就要花掉8M的内存。

由于上述第二个原因，对于以下两类进程并不是很适合使用硬件加速渲染。

1. **Persistent进程**。Persistent进程是一种常驻进程，它们的优先级别很高，即使在内存紧张的时候也不会被AMS杀掉。对于低内存设备，这类进程是不适合使用硬件加速渲染的。在这种情况下，它们会将HardwareRenderer类的静态成员变量sRendererDisabled设置为true，表明要禁用硬件加速渲染。这里顺便提一下，一个应用程序进程可以在AndroidManifest.xml文件将Application标签的**persistent**属性设置为true来将自己设置为Persistent进程，不过只有系统级别的应用设置才有效。类似的**NFC**应用，由于目前android设备的内存都在增大，因此Persistent进程的应用越来越少。
2. **System进程**。System进程有很多线程是需要显示UI的。这些UI一般都是比较简单的，并且System进程也像Persistent进程一样，在内存紧张时是无法杀掉的，因此它们完全没有必要通过硬件加速来渲染。于是，System进程就会将HardwareRenderer类的静态成员变量sRendererDisabled和sSystemRendererDisabled都会被设置为true，表示它要禁用硬件加速渲染。对于System进程，有两种UI需要特殊处理： 第一种UI是**Starting Window**，尽管Starting Window是通过**软件方式**渲染的，但Starting Window在绘制的过程中将不会被缓存。 第二种UI是**锁屏界面**，它允许使用**硬件加速渲染**。但是System进程又表明了它要禁用硬件加速渲染，这时候就通过将参数attrs指向的一个WindowManager.LayoutParams对象的成员变量privateFlags的位WindowManager.LayoutParams.PRIVATE_FLAG_FORCE_HARDWARE_ACCELERATED设置为1来强调锁屏界面不受System进程禁用硬件加速的限制。

那对于常用的Phone、SystemUI 、Lancher是用软件还是硬件绘制呢？

> 20. radio 5875 1610 990196 68124 SyS_epoll_ 0000000000 S com.android.phone
> 21. u0_a17 5940 1610 1066688 94944 SyS_epoll_ 0000000000 S com.android.systemui
> 22. u0_a8 7260 1610 1135640 109612 SyS_epoll_ 0000000000 S com.android.launcher3

其Pid不为系统进程，且AndroidManifest.xml文件将Application标签的persistent属性未配置，默认为false，为硬件绘制。

***注释**：上面描述还是Android4.4之前的场景，目前几乎所有应用都是硬件绘制了，能使用软件绘制场景的地方已经不多了，典型软件绘制场景如下*

> 1. 鼠标
> 2. Mali对于小layer的旋转
> 3. Target API < 14等应用强制使用软件绘制的情形

## 3硬件绘制

### 3.1 硬件绘制流程

本章关于硬件绘制的内容将基于Android6.0。

```c
1. private void draw(boolean fullRedrawNeeded) {
2. ……
3. mAttachInfo.mHardwareRenderer.draw(mView, mAttachInfo, this);
4. …….
5. }
                    
```

![img](D:\my-note\android\view\assets\v2-d79e3770ca812f74fa7a6a16339fa1e9_1440w.png)

图6 Android应用程序UI硬件加速渲染过程

从图6可以看到，硬件加速渲染和软件渲染一样，在开始渲染之前，都是要先向SurfaceFlinger服务dequeue一个Graphic Buffer。不过对硬件加速渲染来说，这个Graphic Buffer会被封装成一个ANativeWindow，并且传递给Open GL进行硬件加速渲染环境初始化。在Android系统中，ANativeWindow和Surface可以是认为等价的，只不过是ANativeWindow常用于Native层中，而Surface常用于Java层中。 Open GL获得了一个ANativeWindow，并且进行了硬件加速渲染环境初始化工作之后，Android应用程序就可以调用Open GL提供的API进行UI绘制了，绘制出来内容就保存在前面获得的Graphic Buffer中。当绘制完毕，Android应用程序再调用libegl库（一般由第三方提供）的eglSwapBuffer接口请求将绘制好的UI显示到屏幕中，其本质上与软件渲染过程是一样的。

![img](D:\my-note\android\view\assets\v2-59b5e5f989a7e62a583f696c6f1d3b86_1440w.webp)

对于硬件绘制而言，其基本流程如下：

![img](D:\my-note\android\view\assets\v2-bc48f099edaef1da15b272d203884f5f_1440w.png)

图7 Android Graphics Pipeline

### 3.2 硬件加速渲染的环境初始化过程分析

### 3.2.1 MainThread和RenderThread的分离

在Android 5.0之前，Android应用程序的Main Thread不仅负责用户输入，同时也是一个OpenGL线程，也负责渲染UI。通过引进Render Thread，我们就可以将UI渲染工作从Main Thread释放出来，交由Render Thread来处理，从而也使得Main Thread可以更专注高效地处理用户输入，这样使得在提高UI绘制效率的同时，也使得UI具有更高的响应性，如图7所示。

![img](D:\my-note\android\view\assets\v2-49e7d969005960341001ebd85e33b03b_1440w.png)

但是对于上层应用而言，UI thread仍然是Main Thread，它并不清楚Render Thread的存在，而对于SurfaceView，UI thread不是Main Thread，而是重新启用了一个新的线程。

### 3.2.1.1 SurfaceView简介

Android在很早就推出了SurfaceView，目前Video播放仍然使用SurfaceView。Camera也曾使用SurfaceView（版本<16），目前都切换至TextureView。引入SurfaceView的原因也是由于MainThread不要有太重的绘制任务，将绘制任务移到另外一个线程。可以看下其draw函数只是将自己清空，相当于挖个洞，具体内容由绘制线程来做。

```c
1.  @Override
2.  public void draw(Canvas canvas) {
3.   …
4.  canvas.drawColor(0, PorterDuff.Mode.CLEAR);
5.  ….
6.  super.draw(canvas);
7.  }
```

其使用方法可通过一个例子看出来（也可参考另外一个Camera也曾用过的类：GLSurfaceView.java，原理是一样的）：

```c
8. public class HardwareCanvasSurfaceViewActivity extends Activity implements Callback {
9.  private SurfaceView mSurfaceView;
10.  private HardwareCanvasSurfaceViewActivity.RenderingThread mThread;
11.  
12.  @Override
13.  protected void onCreate(Bundle savedInstanceState) {
14.  …
15.  mSurfaceView = new SurfaceView(this);
16.  mSurfaceView.getHolder().addCallback(this);
17.   …
18.  setContentView(content);
19.  }
20.  
21.  @Override
22.  public void surfaceCreated(SurfaceHolder holder) {
23.    mThread = new RenderingThread(holder.getSurface());
24.    mThread.start();
25.  }
26.  
27.  @Override
28.  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
29.    mThread.setSize(width, height);
30.  }
31.  
32.  @Override
33.  public void surfaceDestroyed(SurfaceHolder holder) {
34.    if (mThread != null) mThread.stopRendering();
35.  }
36.  
37.  private static class RenderingThread extends Thread {
38.  private final Surface mSurface;
39.  private volatile boolean mRunning = true;
40.  private int mWidth, mHeight;
41.  
42.  public RenderingThread(Surface surface) {
43.   mSurface = surface;
44.  }
45.  ….
46.  @Override
47.  public void run() {
48.  …
49.  Paint paint = new Paint();
50.  paint.setColor(0xff00ff00);
51.  
52.  while (mRunning && !Thread.interrupted()) {
53.    final Canvas canvas = mSurface.lockHardwareCanvas();//此为硬件绘制，软件绘制为lockCanvas()
54.    try {
55.      canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
56.      canvas.drawRect(x, y, x + 20.0f, y + 20.0f, paint);
57.    } finally {
58.      mSurface.unlockCanvasAndPost(canvas);
59.    }
60.  ….
61.  }
62.  }
63.  void stopRendering() {
64.    interrupt();
65.    mRunning = false;
66.  }
67.  }
68. }
```

### 3.2.1.2 SurfaceTexture、TextureView、GLSurfaceview的区别与联系

**1. SurfaceView：**从Android 1.0(API level 1)时就有 。它继承自类View，因此它本质上是一个View。但与普通View不同的是，它有自己的Surface。我们知道，一般的Activity包括的多个View会组成View hierachy的树形结构，只有最顶层的DecorView，也就是根结点视图，才是对WMS可见的。这个DecorView在WMS中有一个对应的WindowState。相应地，在SF中对应的Layer。而SurfaceView自带一个Surface，这个Surface在WMS中有自己对应的WindowState，在SF中也会有自己的Layer。以下图8所示：

![img](D:\my-note\android\view\assets\v2-1245dd0ef235cd7462cd67c04ad2ffe1_1440w.webp)

图8 SurfaceView图层

也就是说，虽然在App端它仍在View hierachy中，但在Server端(WMS和SF)中，它与宿主窗口是分离的。这样的好处是对这个Surface的渲染可以放到单独线程去做，渲染时可以有自己的GL context。这对一些游戏、视频等性能相干的利用非常有益，由于它不会影响主线程对事件的响应。但它也有缺点，由于这个Surface不在View hierachy中，它的显示也不受View的属性控制，所以不能进行平移，缩放等变换，也不能放在其它ViewGroup中，一些View中的特性也没法使用。当然它还有个好处是可以用硬件图层 overlay进行显示。

**2. GLSurfaceView：**从Android 1.5(API level 3)开始加入，作为SurfaceView的补充。它可以看做是SurfaceView的一种典型使用模式。在SurfaceView的基础上，它加入了EGL的管理，并自带了渲染线程。另外它定义了用户需要实现的Render接口，提供了用Strategy pattern更改具体Render行动的灵活性。作为GLSurfaceView的Client，只需要将实现了渲染函数的Renderer的实现类设置给GLSurfaceView便可

**3. SurfaceTexture：**从Android 3.0(API level 11)加入。和SurfaceView不同的是，它对图象流的处理其实不直接显示，而是转为GL外部纹理，因此可用于图象流数据的2次处理(如Camera滤镜，桌面殊效等)。比如Camera的预览数据，变成纹理后可以交给GLSurfaceView直接显示，也能够通过SurfaceTexture交给TextureView作为View heirachy中的一个硬件加速层来显示。首先，SurfaceTexture从图象流(来自Camera预览，视频解码，GL绘制场景等)中取得帧数据，当调用updateTexImage()时，根据内容流中最近的图象更新SurfaceTexture对应的GL纹理对象，接下来，就能够像操作普通GL纹理一样操作它了。从下面的类图中可以看出，它核心管理着一个BufferQueue的Consumer和Producer两端。Producer端用于内容流的源输出数据，Consumer端用于拿GraphicBuffer并生成纹理。SurfaceTexture可以用作非直接输出的内容流，这样就提供2次处理的机会。与SurfaceView直接输出相比，这样会有若干帧的延迟。同时，由于它本身管理BufferQueue，因此内存消耗也会略微大一些。

**4. TextureView：**在4.0(API level 14)中引入。它可以将内容流直接投影到View中，可以用于实现Live preview等功能。和SurfaceView不同，它不会在WMS中单独创建窗口，而是作为View hierachy中的一个普通View，因此可以和其它普通View一样进行移动，旋转，缩放，动画等变化。值得注意的是TextureView必须在硬件加速的窗口中。它显示的内容流数据可以来自App进程或是远端进程。从类图中可以看到，TextureView继承自View，它与其它的View一样在View hierachy中管理与绘制。TextureView重载了draw()方法，其中主要把SurfaceTexture中收到的图象数据作为纹理更新到对应的HardwareLayer中。TextureView本身也包括了SurfaceTexture。它与SurfaceView+SurfaceTexture组合相比可以完成类似的功能(即把内容流上的图象转成纹理，然后输出)。

![img](D:\my-note\android\view\assets\v2-b32f0075d98cea6c22593b315482ff41_1440w.webp)

图9 以SurfaceTexture为中心的数据流

### 3.2.2 OpenGL环境初始化

因此在每当有新的Activity窗口启动时，系统都会为其初始化好Open GL环境。Open GL环境也称为Open GL渲染上下文。一个Open GL渲染上下文只能与一个线程关联。在一个Open GL渲染上下文创建的Open GL对象一般来说只能在关联的Open GL线程中操作。这样就可以避免发生多线程并发访问发生的冲突问题。这与大多数的UI[架构](https://link.zhihu.com/?target=http%3A//lib.csdn.net/base/16)限制UI操作只能发生在UI线程的原理是差不多的。从Android 5.0之后，Android应用程序的Open GL线程就独立出来了，称为Render Thread，如图7所示：

**1.** Render Thread有一个Task Queue，Main Thread通过一个代理对象Render Proxy向这个Task Queue发送一个**drawFrame**命令，从而驱使Render Thread执行一次渲染操作。因此，Android应用程序UI硬件加速渲染环境的初始化过程任务**之一**就是要**创建一个Render Thread**。

**2.** 一个Android应用程序可能存在多个Activity组件。在Android系统中，每一个Activity组件都是一个独立渲染的窗口。由于**一个Android应用程序只有一个Render Thread（**mRenderThread是通过getInstance创建，返回的是一个RenderThread单例，也可从图4看出**）**，因此当Main Thread向Render Thread发出渲染命令时，Render Thread要知道当前要渲染的窗口是什么。从这个角度看，Android应用程序UI硬件加速渲染环境的初始化过程任务**之二**就是要告诉**Render Thread当前要渲染的窗口是什么**

mHardwareRenderer.draw(mView, mAttachInfo, this) 中mHardwareRenderer是为***ThreadedRenderer\***对象，其初始化有3步：

**1.** 调用nCreateRootRenderNode在Native层创建了一个***RenderNode\***，这个Render Node即为窗口的***RootRenderNode\***，保存了窗口所有的绘制命令。

**2.** 调用nCreateProxy在Native层创建了一个***RenderProxy\***对象。该Render Proxy对象以后将负责从Main Thread向Render Thread发送命令，初始化主要在这里完成。RenderProxy类有三个重要的成员变量mRenderThread、mContext和mDrawFrameTask，它们的类型分别为***RenderThread**、**CanvasContext**和**DrawFrameTask***。其中，mRenderThread描述的就是Render Thread，mContext描述的是一个画布上下文，mDrawFrameTask描述的是一个用来执行渲染任务的Task。

Render Thread在运行时主要是做以下两件事情：

**1.** 执行Task Queue的任务，这些Task一般就是由Main Thread发送过来的，例如，Main Thread通过发送一个Draw Frame Task给Render Thread的Task Queue中，请求Render Thread渲染窗口的下一帧。

**2.** 渲染线程在RenderNode中除存有渲染帧的所有信息，且还监听VSync信号，因此可以独立做一些属性动画。它通过执行Pending Registration Frame Callbacks列表的IFrameCallback回调接口。每一个IFrameCallback回调接口代表的是一个动画帧，这些动画帧被同步到Vsync信号到来由Render Thread自动执行，这个目前暂不分析。

**CanvasContext**初始化函数如下：

```c
69. void CanvasContext::setSurface(ANativeWindow* window) {  
70.  mNativeWindow = window;  //渲染的当前窗口
71.  
72.  if (mEglSurface != EGL_NO_SURFACE) {  
73.         mEglManager.destroySurface(mEglSurface);  
74.         mEglSurface = EGL_NO_SURFACE;  
75.  }  
76.  
77.  if (window) {  
78.     mEglSurface = mEglManager.createSurface(window);  
79.   }  
80.  
81.  if (mEglSurface != EGL_NO_SURFACE) {  
82.         ......  
83.         mHaveNewSurface = true;  
84.         makeCurrent();  //创建渲染当前上下文
85.   }   
86.  
87.   ......  
88. }  
```

**3.** 调用AtlasInitializer类的成员函数init初始化一个**系统预加载资源的地图集**。通过这个地图集，可以优化资源的内存使用。

### 3.2.3 系统预加载资源地图集

资源预加载是发生在Zygote进程的，然后Zygote进程fork了应用程序进程，于是就使得预加载的资源可以在Zygote进程与所有的应用程序进程进行共享。这种内存共享机制是由Linux进程创建方式决定的。也就是说，父进程fork子进程之后，只要它们都不去修改某一块内存，那么这块内存就可以在父进程和子进程之间进行共享。一旦父进程或者子进程修改了某一块内存，那么Linux内核就会通过一种称为COW（Copy On Wrtie）的技术为要修改的进程创建一块内存拷贝出来，这时候被修改的内存就不再可以共享。

对于预加载资源来说，它们都是只读的，因此就可以保证它们在Zygote进程与所有的应用程序进程进行共享。这在应用程序UI使用软件方式渲染时可以工作得很好。但是当应用程序UI使用硬件加速渲染时，情况就发生了变化。资源一般是作为纹理来使用的。这意味着每一个应用程序都会将它要使用的预加载资源作为一个纹理上传到GPU去

因此，这种做法会浪费GPU内存。为了节省GPU内存，Android系统在System进程中运行了一个Asset Atlas Service。这个Asset Atlas Service将预加载资源合成为一个纹理，并且上传到GPU去。应用程序进程可以向Asset Atlas Service请求上传后的纹理，从而使得它们不需要再单独去上传一份，这样就可以起到在GPU级别共享的作用，如图7所示：

![img](D:\my-note\android\view\assets\v2-5f621c168e7d173083fb55b5fc152726_1440w.png)

图10应用程序在GPU级别共享预加载资源

在图10中，最右侧显示的是应用程序进程的Render Thread，它们通过Asset Atlas Service获得已经上传到GPU的预加载资源纹理，这样就可以直接使用它们，而不再需要独立上传。

事实上，预加载资源地图集纹理的作用远不止于此。后面我们分析Render Thread渲染窗口UI的时候，可以看到一个Open GL绘制命令合并渲染优化的操作。通过合并若干个Open GL绘制命令为一个Open GL绘制命令，可以减少Open GL渲染管线的状态切换操作，从而提高渲染的效率。预加载资源地图集纹理为这种合并渲染优化提供了可能。

（未完待续，参考文献见下篇）

[沧浪之水：Android GPU硬件加速渲染流程（下）45 赞同 · 5 评论文章![img](D:\my-note\android\view\assets\v2-a153dcd9dee13ffd156be25d9741a516_180x120.jpg)](https://zhuanlan.zhihu.com/p/464564859)



编辑于 2022-02-07 16:56