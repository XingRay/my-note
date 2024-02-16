# 从源码角度剖析Android系统EGL及GL线程

从事OpenGL ES相关开发的技术人员，常常会对一些问题感到困惑，例如GL线程究竟是什么？为什么在这个GL线程申请的texture不能在另外一个GL线程使用？如何打破这种限制等。这些问题在我们团队中也曾经十分让人困惑，因为在网上也找不到详细的解释，这篇文章将回答以下一些棘手而又很难搜到答案的问题：

（1）GL线程和普通线程有什么区别？

（2）texture所占用的空间是跟GL线程绑定的吗？

（3）为什么通常一个GL线程的texture等数据，在另一个GL线程没法用？

（4）为什么通常GL线程销毁后，为什么texture也跟着销毁了？

（5）不同线程如何共享OpenGL数据？

#### 一、OpenGL ES绘图完整流程

首先来看看使用OpenGL ES在手机上绘图的完整流程，这里为什么强调“完整流程”，难道平时用的都是不完整的流程？基本可以这么说，因为“完整流程”相当复杂，而Android系统把复杂的过程封装好了，开发人员接触到的部分是比较简洁易用的，一般情况下也不需要去关心Android帮我们封装好的复杂部分，因此才说一般情况下我们所接触的OpenGL ES绘图流程都是“不完整”的。

以下是OpenGL ES在手机上绘图的完整流程：

(1)获取显示设备

![img](D:\my-note\opengl\assets\x67dz5ylvs.jpeg)

这段代码的作用是获取一个代表屏幕的对象，即EGLDisplay，传的参数是EGL10.EGL_DEFAULT_DISPLAY，代表获取默认的屏幕，因为有些设备上可能不止一个屏幕。

(2)初始化

![img](D:\my-note\opengl\assets\70xnoyocqh.png)

这段代码的作用是初始化屏幕。

(3)选择config

![img](D:\my-note\opengl\assets\2jg0tjoili.png)

这段代码的作用是选择EGL配置， 即可以自己先设定好一个你希望的EGL配置，比如说RGB三种颜色各占几位，你可以随便配，而EGL可能不能满足你所有的要求，于是它会返回一些与你的要求最接近的配置供你选择。

(4)创建Context

![img](D:\my-note\opengl\assets\6n8zgayt8r.jpeg)

这段代码的作用就是用从上一步EGL返回的配置列表中选择一种配置，用来创建EGL Context。

(5)获取Surface

![img](D:\my-note\opengl\assets\q3vezlt6vg.jpeg)

这段代码的作用是获取一个EGLSurface，可以把它想象成是一个屏幕对应的内存区域。注意这里有一个参数surfaceHolder，它对应着GLSurfaceView的surfaceHolder。

(6)将渲染环境设置到当前线程

![img](D:\my-note\opengl\assets\7acnle8y4z.png)

这段代码的作用是将渲染环境设置到当前线程，相当于让当前线程拥有了Open GL的绘图能力，为什么做了这步操作，线程就拥有了Open GL的绘图能力？后面会讲解。

接下来就是绘图逻辑了：

![img](D:\my-note\opengl\assets\9wng2iju0b.jpeg)

以上步骤，对于不经常接触EGL的开发人员，也许看起来比较陌生，让我们来看看我们比较熟悉的GLSurfaceView，看看它里面和刚说的那一堆乱七八糟的东西有什么关系。

二、GLSurfaceView内部的EGL相关逻辑

查看GLSurfaceView的源码，可以看见里面有一个类叫GLThread，就是所谓的“GL线程”:

![img](D:\my-note\opengl\assets\rbahtz9abo.jpeg)

可以看到，虽然它名叫GLThread，但是它也是从普通的Thread类继承而来，理论上就是一个普通的线程，为什么它拥有OpenGL绘图能力？继续往下看，里面最重要的部分就是guardedRun()方法，让我们来看一下guardedRun()方法里有什么东西，guardedRun()里大致做的事情：

![img](D:\my-note\opengl\assets\ilo2nxnnlp.jpeg)

仔细看一下guardedRun()的源码会发现，里面做的事情和之前说的“完整流程”能都一一对应着，里面还有我们非常熟悉的onSurfaceCreated()、onSurfaceChanged()和onDrawFrame()这三个回调，而一般情况下，我们使用OpenGL绘图，就是在onDrawFrame()回调里绘制的，完全不用关心“完整流程”中的复杂步骤，这就是前文为什么说“完整流程”相当复杂，而Android系统帮我们把复杂的过程封装好了，我们接触到的部分是比较简洁易用的，一般情况下也不需要去关心Android帮我们封装好的复杂部分。

至此，得到一个结论，那就是所谓的GL线程和普通线程没有什么本质的区别，它就是一个普通的线程，只不过它按照了OpenGL绘图的完整流程正确地操作了下来，因此它有OpenGL的绘图能力。那么，如果我们自己创建一个线程，也按这样的操作方法，那我们也可以在自己创建的线程里绘图吗？当然可以！

#### 三、EGL如何协助OpenGL

我们先随便看一下OpenGL的常用方法，例如最常用的GLES2.0.glGenTextures()和GLES2.0.glDeleteTextures()，在Android Studio里点进去看一下：

![img](D:\my-note\opengl\assets\imeilnxe3r.jpeg)

![img](D:\my-note\opengl\assets\7agzxqoy02.jpeg)

是native的方法，并且是静态的，看起来和EGL没有关系，它怎样知道是GL线程去调的还是普通线程去调的？它又怎样把GLES2.glDeleteTextures()和GLES2.0.glGenTextures()的对应到正确的线程上？我们来看看底层的源码：

![img](D:\my-note\opengl\assets\c0yl87nnwy.jpeg)

![img](D:\my-note\opengl\assets\wsm5h9hxy8.jpeg)

可以看到，在底层，它会去拿一个context，实际上这个context就是保存在底层的EGL context，而这个EGL context，它是Thread Specific的。什么是Thread Specific？就是说，不同的线程去拿，得到的EGL context可能不一样，这取决于给这个线程设置的EGL context是什么，可以想象成每个线程都有一个储物柜，去里面拿东西能得到什么，取决于你之前给这个线程在储物柜里放了什么东西，这是一个形象化的比喻，代码时的实现其实是给线程里自己维护了一个存储空间，相当于储物柜，因此每个线程去拿东西的时候，只能拿到自己储物柜里的东西，因此是Thread Specific的。

那么是什么时候把EGL context放到线程的储物柜里去的呢？还记得前面提到过eglMakeCurrent()这个东西吗？我们来看看它的底层:

![img](D:\my-note\opengl\assets\rsg0vsl0zr.jpeg)

可以看到，在调用eglMakeCurrent()时，会通过setGLThreadSpecific()将传给eglMakeCurrent()的EGL Context在底层保存一份到调用线程的储物柜里。我们再来仔细看一下eglMakeCurrent()里一步一步做了什么，这对于理解线程绑定OpenGL渲染环境至关重要：

![img](D:\my-note\opengl\assets\p9tzojqhc7.jpeg)

归纳下来就是这么几点：

1.获取当前线程的EGL Context current(底层用ogles_context_t存储)

2.判断传递过来的EGL Context gl是不是还处于IS_CURRENT状态

3.如果gl是IS_CURRENT状态但又不是当前线程的EGL Context，则return

4.如果gl不是IS_CURRENT状态，将current置为非IS_CURRENT状态

5.将gl置为IS_CURRENT状态并将gl设置为当前线程的Thread Local的EGL Context

因此有两点结论：

1.如果一个EGL Context已被一个线程makeCurrent()，它不能再次被另一个线程makeCurrent()

2.makeCurrent()另外一个EGL Context后会与当前EGL Context脱离关系

继续看GLES2.0.glGenTextures()：

![img](D:\my-note\opengl\assets\w2abq21gtu.jpeg)

上面给出了glGenTextures()底层的一些调用关系，下面我有一个图来展示一下调了glGenTextures()，分配的texture放在哪里了：

![img](D:\my-note\opengl\assets\prlsz5ivjq.jpeg)

这其实没那么重要，因为这里只是存了一个texture id，并不是texture真正所占的存储空间，这很好理解，因此调glGenTextures()方法的时候，也没指定要多大的texture嘛。那么texture真正所占的存储空间在什么地方呢？那就要看看给texture分配存储空间的方法了，也就是glTexImage2D()方法：

![img](D:\my-note\opengl\assets\ra8s9uzvvd.jpeg)

下面再给出一个图展示texture所占用的存储空间的空间放在什么地方：

![img](D:\my-note\opengl\assets\f0xp36pbel.jpeg)

到这里，又有了一个结论：本质上texture是跟EGL Context绑定的，并不是跟GL线程绑定的，因此GL线程销毁时，如果不销毁EGL Context，则texture没有销毁。我们可能常常听说这样一种说法：GL线程销毁后，GL的上下文环境就被销毁了，在其中分配的texture也自然就被销毁了。这种说法会让人误为texture是跟GL线程绑定在一起的，误认为GL线程销毁后texture也自动销毁，其实GL线程并不会自动处理texture的销毁，而需要手动销毁。有人想问了，我们平时用GLSurfaceView时，当GLSurfaceView销毁时，我们如果没有delete掉分配的texture，这些texture也会没自己释放，这是怎么回事？这是因为GLSurfaceView销毁时帮你把texture销毁了，我们来看看GLSurfaceView里相关的代码：

![img](D:\my-note\opengl\assets\c9dixfaqis.jpeg)

因此如果你自己创建了一个GL线程，当GL线程销毁时，如果你不主动销毁texture，那么texture实际上是不会自动销毁的。

#### 四、总结

下面总结一下本文，回答文章开头提出的问题：

1）GL线程和普通线程有什么区别？

答：没有本质区别，只是它按OpenGL的完整绘图流程正确的跑了下来，因而可以用OpenGL绘图

2）texture所占用的空间是跟GL线程绑定的吗？

答：跟EGL Context绑定，本质上与线程无关

3）为什么通常一个GL线程的texture等数据，在另一个GL线程没法用？

答：因为调用OpenGL接口时，在底层会获取Thread Specific的EGL Context，因此通常情况下，不同线程获取到的EGL Context是不一样的，而texture又放在EGL Context中，因此获取不到另外一个线程创建的texture等数据

4）为什么通常GL线程销毁后，为什么texture也跟着销毁了？

答：因为通常是用GLSurfaceView，它销毁时显式调用了eglDestroyContext()销毁与之绑定的EGL Context，从而其中的texture也跟着被销毁

5）不同线程如何共享OpenGL数据？

答：在一个线程中调用eglCreateContext()里传入另一个线程的EGL Context作为share context，或者先让一个线程解绑EGL Context，再让另一个线程绑定这个EGL Context。

由于笔者知识水平有限，文中若有理解错误之处，请见谅指正。