GLSurfaceView中GL相关资源销毁问题

ryfdizuo

于 2015-02-13 16:20:32 发布

阅读量8.7k
 收藏 3

点赞数 2
分类专栏： 移动开发 随想&amp;&amp;感想 OpenGL Android OpenGL技术专栏 文章标签： android android ndk opengl es opengl
版权

随想&&感想
同时被 3 个专栏收录
102 篇文章0 订阅
订阅专栏

OpenGL
81 篇文章2 订阅
订阅专栏

OpenGL技术专栏
36 篇文章22 订阅
订阅专栏
1. YY下传统的GLUT框架
没有context概念，Main函数，Display，mouse，keyboarrd，reshape，这些回调函数中都可以直接调用OpenGL命令。
纹理等gl相关资源需要自己管理，及时释放。从而防止显存不足，gl资源分配失败。。。

glut框架下所有gl函数指令只局限在主线程中，不支持多线程调用。

2. Android GLSurfaceview
gl的context作用域只局限在GLSurfaceView.Renderer的onSurfaceCreated，onSurfaceChanged，onDrawFrame 三个函数中。其他函数都是UI线程中调用，如view的onPause，onResume这类。

在UI线程函数中调用gl的函数时，会报如下错误：
02-06 10:00:51.318: E/libEGL(4458): call to OpenGL ES API with no current context (logged once per thread)

3. 纹理等GL资源释放情况
不需要自己释放，android自己管理。 Activity的onPause调用了GLSurfaceView::onPause，此时只需要将所有缓存的gl对象句柄清零，而不需要显式调用glDeleteXXX函数释放。例如纹理不需要glDeleteTextures释放，因为此时在UI线程中。下次onResume进入，创建方式两种：
静态创建，在Renderer::onSurfaceCreated函数中，预创建所有需要纹理，各种XXO对象。关于该函数的描述：
Since this method is called at the beginning of rendering, as well as every time the EGL context is lost, this method is a convenient place to put code to create resources that need to be created when the rendering starts, and that need to be recreated when the EGL context is lost. Textures are an example of a resource that you might want to create here.

延迟到onDrawFrame中，发现纹理等资源的句柄为0，不可用时重新动态创建。
ref : http://stackoverflow.com/questions/4925065/how-to-detect-when-textures-are-destroyed