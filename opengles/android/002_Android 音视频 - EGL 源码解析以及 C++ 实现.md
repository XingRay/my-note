# Android 音视频 - EGL 源码解析以及 C++ 实现

> OpenGL 是一个跨平台的 API,而不同的操作系统(Windows,Android,IOS)各有自己的屏幕渲染实现。所以 OpenGL 定义了一个中间接口层 EGL（Embedded Graphics Library）标准，具体实现交给各个操作系统本身

# EGL

简单来说 EGL 是一个中间接口层，是一个规范，由于 OpenGL 的跨平台性，所以说这个规范显得尤其重要，不管各个操作系统如何蹦跶，都不能脱离我所定义的规范。

## EGL 的一些基础知识

- EGLDisplay

EGL 定义的一个抽象的系统显示类，用于操作设备窗口。

- EGLConfig

EGL 配置，如 rgba 位数

- EGLSurface

渲染缓存，一块内存空间，所有要渲染到屏幕上的图像数据，都要先缓存在 EGLSurface 上。

- EGLContext

OpenGL 上下文，用于存储 OpenGL 的绘制状态信息、数据。

初始化 EGL 的过程可以说是对上面几个信息进行配置的过程。

## OpenGL ES 绘图完整流程

我们在使用 Java GLSurfaceView 的时候其实只是自定义了 Render,该 Render 实现了 GLsurfaceView.Renderer 接口，然后自定义的 Render 中的 3 个方法就会得到回调，Android 系统其实帮我省掉了其中的很多步骤。所以我们这里来看一下**完整流程**(1). **获取显示设备(对应于上面的 EGLDisplay)**

```objectivec
/*
 * Get an EGL instance */
 mEgl = (EGL10) EGLContext.getEGL();
 
/*
 * Get to the default display. */
 mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
```

(2). **初始化 EGL**

```objectivec
int[] version = new int[2];
//初始化屏幕
if(!mEgl.eglInitialize(mEglDisplay, version)) {
    throw new RuntimeException("eglInitialize failed");
}
```

(3). **选择 Config(用 EGLConfig 配置参数)**

```objectivec
//这段代码的作用是选择EGL配置， 即可以自己先设定好一个你希望的EGL配置，比如说RGB三种颜色各占几位，你可以随便配，而EGL可能不能满足你所有的要求，于是它会返回一些与你的要求最接近的配置供你选择。
if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
 num_config)) {
    throw new IllegalArgumentException("eglChooseConfig#2 failed");
}
```

(4). **创建 EGLContext**

```objectivec
//从上一步EGL返回的配置列表中选择一种配置，用来创建EGL Context。
egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
 mEGLContextClientVersion != 0 ? attrib_list : null);
```

(5). **获取 EGLSurface**

```objectivec
//创建一个窗口Surface，可以看成屏幕所对应的内存
 egl.eglCreateWindowSurface(display, config, nativeWindow, null)
```

> PS 这里的 nativeWindow 是 GLSurfaceView 的 surfaceHolder

(6). **绑定渲染环境到当前线程**

```objectivec
/*
 * Before we can issue GL commands, we need to make sure * the context is current and bound to a surface. */
 if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
    /*
    * Could not make the context current, probably because the underlying * SurfaceView surface has been destroyed. */ 
     logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", mEgl.eglGetError());
     return false;
 }
```

**循环绘制**

```objectivec
loop:{
    //绘制中....
    //(7).交换缓冲区
    mEglHelper.swap();
}

public int swap() {
    if (! mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
        return mEgl.eglGetError();
    }
    return EGL10.EGL_SUCCESS;
}
```

## Java - GLSurfaceView/GLTextureView

上面我们介绍了 EGL 的一些基础知识，接着我们来看在 GLSurfaceView/GLTextureView 中 EGL 的具体实现，我们来从源码上剖析 Android 系统 EGL 及 GL 线程。

### GLThread

我们来看一下 GLThread，GLThread 也是从普通的 Thread 类继承而来，理论上就是一个普通的线程，为什么它拥有 OpenGL 绘图能力？继续往下看，里面最重要的部分就是 guardedRun()方法。

```objectivec
static class GLThread extends Thread {
    ...
    @Override
    public void run() {
      
        try {
                guardedRun();
         } catch (InterruptedException e) {
                // fall thru and exit normally
         } finally {
                sGLThreadManager.threadExiting(this);
         }
    }
}
```

让我们来看一下 guardedRun()方法里有什么东西，guardedRun()里大致做的事情：

```objectivec
private void guardedRun() throws InterruptedException {
    while(true){
        //if ready to draw
        ...
        mEglHelper.start();//对应于上面完整流程中的(1)(2)(3)(4)
        
        ...
        mEglHelper.createSurface()//对应于上面完整流程中的(5)(6)
        
        ...
        回调GLSurfaceView.Renderer的onSurfaceCreated();
        ...
        回调GLSurfaceView.Renderer的onSurfaceChanged();
        ...
        回调GLSurfaceView.Renderer的onDrawFrame();
        
        ...
         mEglHelper.swap();//对应于上面完整流程中的(5)(7)
    }
}
```

从上面我们的分析得知 GLSurfaceView 中的 GLThread 就是一个普通的线程，只不过它按照了 OpenGL 绘图的完整流程正确地操作了下来，因此它有 OpenGL 的绘图能力。那么，如果我们自己创建一个线程，也按这样的操作方法，那我们也可以在自己创建的线程里绘图吗？答案是肯定的(这不正是 EGL 的接口意义)，下面我会给出 EGL 在 Native C/C++中的实现。

## Native - EGL

Android Native 环境中并不存在现成的 EGL 环境，所以我们在进行 OpenGL 的 NDK 开发时就必须自己实现 EGL 环境，那么如何实现呢，我们只需要参照 GLSurfaceView 中的 GLThread 的写法就能实现 Native 中的 EGL

> PS
>
> 以下的内容可能需要你对 C/C++以及 NDK 有一定熟悉

### 第 1 步实现类似于 Java GLSurfaceView 中的 GLThread 的功能

gl_render.h

```objectivec
class GLRender {
    private:
         const char *TAG = "GLRender";
         //OpenGL渲染状态
         enum STATE {
             NO_SURFACE, //没有有效的surface
             FRESH_SURFACE, //持有一个为初始化的新的surface
             RENDERING, //初始化完毕，可以开始渲染
             SURFACE_DESTROY, //surface销毁
             STOP //停止绘制
         };
         JNIEnv *m_env = NULL;
         //线程依附的jvm环境
         JavaVM *m_jvm_for_thread = NULL;
         //Surface引用，必须要使用引用，否则无法在线程中操作
         jobject m_surface_ref = NULL;
         //本地屏幕
         ANativeWindow *m_native_window = NULL;
         //EGL显示表面
         EglSurface *m_egl_surface = NULL;
         int m_window_width = 0;
         int m_window_height = 0;
         
         // 绘制代理器
         ImageRender *pImageRender;
         
         //OpenGL渲染状态
         STATE m_state = NO_SURFACE;
         // 初始化相关的方法
         void InitRenderThread();
         bool InitEGL();
         void InitDspWindow(JNIEnv *env);
         // 创建/销毁 Surface void CreateSurface();
         void DestroySurface();
         // 渲染方法
         void Render();
         void ReleaseSurface();
         void ReleaseWindow();
         // 渲染线程回调方法
         static void sRenderThread(std::shared_ptr<GLRender> that);
    public:
         GLRender(JNIEnv *env);
         ~GLRender();
         //外部传入Surface
         void SetSurface(jobject surface);
      
         void Stop();
         void SetBitmapRender(ImageRender *bitmapRender);
        // 释放资源相关方法
         void ReleaseRender();
         
         ImageRender *GetImageRender();
};
```

gl_render.cpp

```objectivec
//构造函数
GLRender::GLRender(JNIEnv *env) {
     this->m_env = env;
     //获取JVM虚拟机，为创建线程作准备
     env->GetJavaVM(&m_jvm_for_thread);
     InitRenderThread();
}
//析构函数
GLRender::~GLRender() {
    delete m_egl_surface;
}

//初始化渲染线程
void GLRender::InitRenderThread() {
    // 使用智能指针，线程结束时，自动删除本类指针
     std::shared_ptr<GLRender> that(this);
     std::thread t(sRenderThread, that);
     t.detach();
}

//线程回调函数
void GLRender::sRenderThread(std::shared_ptr<GLRender> that) {
    JNIEnv *env;
     //(1) 将线程附加到虚拟机，并获取env
     if (that->m_jvm_for_thread->AttachCurrentThread(&env, NULL) != JNI_OK) {
            LOGE(that->TAG, "线程初始化异常");
            return; 
     }
     // (2) 初始化 EGL 
    if (!that->InitEGL()) {
         //解除线程和jvm关联
         that->m_jvm_for_thread->DetachCurrentThread();
         return; 
     }
     
     //进入循环
    while (true) {
            //根据OpenGL渲染状态进入不同的处理
            switch (that->m_state) {
                //刷新Surface，从外面设置Surface后m_state置为该状态，说明已经从外部(java层)获得Surface的对象了
                case FRESH_SURFACE:
                     LOGI(that->TAG, "Loop Render FRESH_SURFACE")
                     // (3) 初始化Window
                     that->InitDspWindow(env);
                     // (4) 创建EglSurface
                     that->CreateSurface();
                     // m_state置为RENDERING状态进入渲染
                     that->m_state = RENDERING;
                     break; 
                 case RENDERING:
                    LOGI(that->TAG, "Loop Render RENDERING")
                    // (5) 渲染
                    that->Render();
                    break; 
               
                 case STOP:
                    LOGI(that->TAG, "Loop Render STOP")
                    //(6) 解除线程和jvm关联
                     that->ReleaseRender();
                     that->m_jvm_for_thread->DetachCurrentThread();
                     return; 
                case SURFACE_DESTROY:
                    LOGI(that->TAG, "Loop Render SURFACE_DESTROY")
                    //(7) 释放资源
                    that->DestroySurface();
                    that->m_state = NO_SURFACE;
                    break; 
                case NO_SURFACE:
                default:
                    break;
     }
    usleep(20000);
 }
}
```

我们定义的 GLRender 各个流程代码里已经标注了步骤，虽然代码量比较多，但是我们的 c++ class 分析也是跟 java 类似，



![img](D:\my-note\opengles\android\assets\d826069cc49ecdd839d7ad0fba8af107780150ac_2_850x615.png)



> PS 上图中的(3)(4)等步骤对应于代码中的步骤注释

## (1)`将线程附加到虚拟机，并获取env`

这一步简单明了，我们往下看

## EGL 封装准备

我们在上一篇就知道了 EGL 的一些基础知识，`EGLDiaplay`,`EGLConfig`,`EGLSurface`,`EGLContext`，我们需要把这些基础类进行封装，那么如何进行封装呢，我们先看一下对于我们上篇文章中自定义的 GLRender 类需要什么 gl_render.h

```objectivec
//Surface引用，必须要使用引用，否则无法在线程中操作
jobject m_surface_ref = NULL;
//本地屏幕
ANativeWindow *m_native_window = NULL;
//EGL显示表面 注意这里是我们自定义的EglSurface包装类而不是系统提供的EGLSurface哦
EglSurface *m_egl_surface = NULL;
```

对于 gl_render 来说`输入的是外部的Surface对象`，我们这里的是`jobject m_surface_ref`,那么输出需要的是`ANativeWindow`,`EglSurface`。

> 关于`ANativeWindow`可以查看官方文档[ANativeWindow](https://developer.android.com/ndk/reference/group/a-native-window)

那么`EglSurface`呢，

egl_surface.h

```objectivec
class EglSurface {
private:
    const char *TAG = "EglSurface";
    //本地屏幕
     ANativeWindow *m_native_window = NULL;
     //封装了EGLDisplay EGLConfig EGLContext的自定义类
     EglCore *m_core;
     //EGL API提供的 EGLSurface
     EGLSurface m_surface;
}
```

> 可以看到我们上面的定义的思想也是 V(View)和 C(Controller)进行了分离。

egl_core.h

```objectivec
class EglCore {
private:
    const char *TAG = "EglCore";
     //EGL显示窗口
     EGLDisplay m_egl_dsp = EGL_NO_DISPLAY;
     //EGL上下文
     EGLContext m_egl_context = EGL_NO_CONTEXT;
     //EGL配置
     EGLConfig m_egl_config;
}
```

有了上面的准备工作后，我们就跟着流程图的步骤来一步步走。

## (2)初始化 EGL

gl_render.cpp

```objectivec
bool GLRender::InitEGL() {
    //创建EglSurface对象
    m_egl_surface = new EglSurface();
    //调用EglSurface的init方法
    return m_egl_surface->Init();
}
```

egl_surface.cpp

> PS 我们上面也说了 EGL 的初始化主要是对 EGLDisplay EGLConfig EGLContext 的操作，所以现在是对 EGLCore 的操作。

```objectivec
EglSurface::EglSurface() {
    //创建EGLCore
    m_core = new EglCore();
}

bool EglSurface::Init() {
    //调用EGLCore的init方法
    return m_core->Init(NULL);
}
```

egl_core.cpp

```objectivec
EglCore::EglCore() {
}


bool EglCore::Init(EGLContext share_ctx) {
    if (m_egl_dsp != EGL_NO_DISPLAY) {
        LOGE(TAG, "EGL already set up")
        return true;
     }
    if (share_ctx == NULL) {
            share_ctx = EGL_NO_CONTEXT;
     }
     //获取Dispaly
    m_egl_dsp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
     if (m_egl_dsp == EGL_NO_DISPLAY || eglGetError() != EGL_SUCCESS) {
            LOGE(TAG, "EGL init display fail")
            return false;
     }
        EGLint major_ver, minor_ver;
     //初始化egl
     EGLBoolean success = eglInitialize(m_egl_dsp, &major_ver, &minor_ver);
     if (success != EGL_TRUE || eglGetError() != EGL_SUCCESS) {
            LOGE(TAG, "EGL init fail")
            return false;
     }
        LOGI(TAG, "EGL version: %d.%d", major_ver, minor_ver)
     //获取EGLConfig   
     m_egl_config = GetEGLConfig();
     const EGLint attr[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
     //创建EGLContext
     m_egl_context = eglCreateContext(m_egl_dsp, m_egl_config, share_ctx, attr);
     if (m_egl_context == EGL_NO_CONTEXT) {
            LOGE(TAG, "EGL create fail, error is %x", eglGetError());
     return false; }
        EGLint egl_format;
     success = eglGetConfigAttrib(m_egl_dsp, m_egl_config, EGL_NATIVE_VISUAL_ID, &egl_format);
     if (success != EGL_TRUE || eglGetError() != EGL_SUCCESS) {
            LOGE(TAG, "EGL get config fail, error is %x", eglGetError())
            return false;
     }
    LOGI(TAG, "EGL init success")
    return true;
}

EGLConfig EglCore::GetEGLConfig() {
    EGLint numConfigs;
    EGLConfig config;

  //希望的最小配置，
    static const EGLint CONFIG_ATTRIBS[] = {
            EGL_BUFFER_SIZE, EGL_DONT_CARE,
            EGL_RED_SIZE, 8,//R 位数
            EGL_GREEN_SIZE, 8,//G 位数
            EGL_BLUE_SIZE, 8,//B 位数
            EGL_ALPHA_SIZE, 8,//A 位数
            EGL_DEPTH_SIZE, 16,//深度
            EGL_STENCIL_SIZE, EGL_DONT_CARE,
            EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
            EGL_NONE // the end 结束标志
    };
  //根据你所设定的最小配置系统会选择一个满足你最低要求的配置，这个真正的配置往往要比你期望的属性更多
    EGLBoolean success = eglChooseConfig(m_egl_dsp, CONFIG_ATTRIBS, &config, 1, &numConfigs);
    if (!success || eglGetError() != EGL_SUCCESS) {
        LOGE(TAG, "EGL config fail")
        return NULL;
    }
    return config;
}
```

## （3）创建 Window

gl_render.cpp

```objectivec
void GLRender::InitDspWindow(JNIEnv *env) {
  //传进来的Surface对象的引用
    if (m_surface_ref != NULL) {
        // 初始化窗口
        m_native_window = ANativeWindow_fromSurface(env, m_surface_ref);

        // 绘制区域的宽高
        m_window_width = ANativeWindow_getWidth(m_native_window);
        m_window_height = ANativeWindow_getHeight(m_native_window);

        //设置宽高限制缓冲区中的像素数量
        ANativeWindow_setBuffersGeometry(m_native_window, m_window_width,
                                         m_window_height, WINDOW_FORMAT_RGBA_8888);

        LOGD(TAG, "View Port width: %d, height: %d", m_window_width, m_window_height)
    }
}
```

## (4)创建 EglSurface 并绑定到线程

gl_render.cpp

```objectivec
void GLRender::CreateSurface() {
    m_egl_surface->CreateEglSurface(m_native_window, m_window_width, m_window_height);
    glViewport(0, 0, m_window_width, m_window_height);
}
```

egl_surface.cpp

```objectivec
/**
 * 
 * @param native_window 传入上一步创建的ANativeWindow
 * @param width 
 * @param height 
 */
void EglSurface::CreateEglSurface(ANativeWindow *native_window, int width, int height) {
    if (native_window != NULL) {
        this->m_native_window = native_window;
        m_surface = m_core->CreateWindSurface(m_native_window);
    } else {
        m_surface = m_core->CreateOffScreenSurface(width, height);
    }
    if (m_surface == NULL) {
        LOGE(TAG, "EGL create window surface fail")
        Release();
    }
    MakeCurrent();
}

void EglSurface::MakeCurrent() {
    m_core->MakeCurrent(m_surface);
}
```

egl_core.cpp

```objectivec
EGLSurface EglCore::CreateWindSurface(ANativeWindow *window) {
  //调用EGL Native API创建Window Surface
    EGLSurface surface = eglCreateWindowSurface(m_egl_dsp, m_egl_config, window, 0);
    if (eglGetError() != EGL_SUCCESS) {
        LOGI(TAG, "EGL create window surface fail")
        return NULL;
    }
    return surface;
}

void EglCore::MakeCurrent(EGLSurface egl_surface) {
  //调用EGL Native API 绑定渲染环境到当前线程
    if (!eglMakeCurrent(m_egl_dsp, egl_surface, egl_surface, m_egl_context)) {
        LOGE(TAG, "EGL make current fail");
    }
}
```

## (5)渲染

gl_render.cpp

```objectivec
void GLRender::Render() {
    if (RENDERING == m_state) {
        pImageRender->DoDraw();//画画画....
        m_egl_surface->SwapBuffers();
    }
}
```

egl_surface.cpp

```objectivec
void EglSurface::SwapBuffers() {
    m_core->SwapBuffer(m_surface);
}
```

egl_core.cpp

```objectivec
void EglCore::SwapBuffer(EGLSurface egl_surface) {
  //调用EGL Native API
    eglSwapBuffers(m_egl_dsp, egl_surface);
}
```

后面的停止与销毁就交给读者自行研究了。

## 代码

[EGLDemoActivity.java](https://github.com/LoveWFan/BlogDemo/blob/master/app/src/main/java/com/poney/blogdemo/demo1/EGLDemoActivity.java)

[EGL Native](https://github.com/LoveWFan/BlogDemo/tree/master/app/src/main/cpp/eg l)