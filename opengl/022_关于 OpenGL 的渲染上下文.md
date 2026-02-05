## 关于 OpenGL 的渲染上下文

**OpenGL 上下文**

OpenGL 的上下文（OpenGL context）是一个 OpenGL 绘图环境的抽象概念，它包括了所有 OpenGL 状态信息和资源，以便OpenGL能够正确地渲染图形。

**OpenGL 在渲染的时候需要一个 Context 来记录了 OpenGL 渲染需要的所有信息和状态**，可以把它理解成一个大的结构体，它里面记录了当前使用 OpenGL 函数调用设置的状态和状态属性。

OpenGL 是个状态机，OpenGL采用了客户端-[服务器](https://cloud.tencent.com/act/pro/promotion-cvm?from_column=20065&from=20065)模式，我们可以认为每一个硬件 [GPU](https://cloud.tencent.com/act/pro/promotion-cvm?from_column=20065&from=20065) 相当于一台服务器，可对应多个客户端即上下文，一个客户端维护着一组状态机。

**大部分的 OpenGL 命令都是异步的，不代表真正地执行，只是客户端向服务器发送了一些命令（同时有一些****API****可实现同步功能）。**

申请绘制上下文，意味着系统资源的申请，每个绘制上下文还是需要不少资源的，所有的OpenGL 调用，都需要指定是在哪个上下文环境下调用的。

**渲染上下文和线程**

**OpenGL 的绘制命令都是作用在当前的 Context 上，上下文是线程私有的，可以为同一个线程创建多个上下文，但是一次只能指定一个。**

多个线程不能同时指定同一个 Context ，否则会导致崩溃。当有需要多个并行的绘制任务时，则要创建多个 Context，为并行的线程分别绑定不同的上下文。

可以通过共享上下文的方式为别的线程创建上下文，这些线程之间可以共享一部分资源。

### **共享上下文**

一个是进程可以创建多个 Context，它们可以分别描绘出不同的图形界面，就像一个应用程序可以打开多个窗口一样。每个 OpenGL Context 是相互独立的，它们都有自己的 OpenGL 对象集。

但有时会有场景需要多个上下文使用同一份纹理资源的情况，创建 Context，意味着系统资源的占用，同一份纹理重复申请会造成资源浪费，因此 OpenGL 上下文允许共享一部分资源。

大部分 OpenGL Objects 是可以共享的，包括 Sync Object 和 GLSL Objects。

Container Objects 和 Query Objects 是不能共享的。例如纹理、shader、Buffer 等资源是可以共享的，但 Frame Buffer Object (FBO)、Vertex Array Object（VAO）等[容器](https://cloud.tencent.com/product/tke?from_column=20065&from=20065)对象不可共享，但可将共享的纹理和 VBO 绑定到各自上下文的容器对象上。

可以共享的资源：

- **纹理；**
- **shader；**
- **program 着色器程序；**
- **buffer 类对象，如 VBO、 EBO、 RBO 等 。**

不可以共享的资源：

- **FBO 帧缓冲区对象（不属于 buffer 类）；**
- **VAO 顶点数组对象（不属于 buffer 类）。**

**这里解释下，在不可以共享的资源中，FBO 和 VAO 属于资源管理型对象，FBO 负责管理几种缓冲区，本身不占用资源，VAO 负责管理 VBO 或 EBO ，本身也不占用资源。**

**参考文章**

https://blog.csdn.net/shenyi0_0/article/details/109382509

https://www.cnblogs.com/liuwq/p/5444641.html

[OpenGL ES 共享上下文实现多线程渲染](https://cloud.tencent.com/developer/tools/blog-entry?target=http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654165674&idx=1&sn=5ad986771f5364b016ac45ccd301e740&chksm=8cf38999bb84008fc390bbfdc6201a99f9c0a785aec6d079a96d92ee6fdc5effb4a784559792&scene=21%23wechat_redirect&source=article&objectId=2357696)

-- END --



# OpenGL ES 共享上下文实现多线程渲染

原创 字节流动 [字节流动](javascript:void(0);) *2021-06-04 08:17*

![img](assets/300.png)

**字节流动**

移动端音视频 & OpenGL 高级工程师、面试官，8 年大厂经验，日常技术干货、个人总结、职场经验分享。

210篇原创内容



公众号

OpenGL ES 共享上下文时，可以共享哪些资源？



![图片](assets/640.gif)

共享上下文实现多线程渲染

**EGL 概念回顾**

**EGL 是 OpenGL ES 和本地窗口系统（Native Window System）之间的通信接口**，它的主要作用：



- **与设备的原生窗口系统通信；**
- 查询绘图表面的可用类型和配置；
- **创建绘图表面；**
- **在OpenGL ES 和其他图形渲染API之间同步渲染；**
- **管理纹理贴图等渲染资源。**



**OpenGL ES 的平台无关性正是借助 EGL 实现的**，EGL 屏蔽了不同平台的差异（Apple 提供了自己的 EGL API 的 iOS 实现，自称 EAGL）。



本地窗口相关的 API 提供了访问本地窗口系统的接口，而 E**GL 可以创建渲染表面 EGLSurface ，同时提供了图形渲染上下文 EGLContext，用来进行状态管理**，接下来 OpenGL ES 就可以在这个渲染表面上绘制。



![图片](assets/640-17122634875687.webp)egl、opengles 和设备之间的关系



图片中：



- Display (EGLDisplay) 是对实际显示设备的抽象；
- Surface（EGLSurface）是**对用来存储图像的内存区域 FrameBuffer 的抽象**，包括 Color Buffer（颜色缓冲区）， Stencil Buffer（模板缓冲区） ，Depth Buffer（深度缓冲区）；
- **Context (EGLContext) 存储 OpenGL ES 绘图的一些状态信息**；



在 Android 平台上开发 OpenGL ES 应用时，**类 GLSurfaceView 已经为我们提供了对 Display , Surface , Context 的管理**。



即 **GLSurfaceView 内部实现了对 EGL 的封装**，可以很方便地利用接口 GLSurfaceView.Renderer 的实现，使用 OpenGL ES API 进行渲染绘制，很大程度上提升了 OpenGLES 开发的便利性。



当然我们也可以自己实现对 EGL 的封装，本文就是在 Native 层对 EGL 进行封装，不借助于 GLSurfaceView ，实现图片后台渲染，利用 GPU 完成对图像的高效处理。



关于 EGL 更详细的使用结束，可以参考系列文章中的[你还不知道 OpenGL ES 和 EGL 的关系？](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654162834&idx=2&sn=6272e659b0ad5037743bcd748f576ca1&chksm=8cf39ca1bb8415b70cad95a3da3aa77fa684ad1c0d58e7d770156ba1defda9c955b429066977&scene=21#wechat_redirect)

# 共享上下文时可以共享哪些资源

**共享上下文时，可以跨线程共享哪些资源？**这个是本文要讲的重点。



为了照顾一些读者大人的耐心，这里直接说结论。



可以共享的资源：



- **纹理；**
- **shader；**
- **program 着色器程序；**
- **buffer 类对象，如 VBO、 EBO、 RBO 等 。**



不可以共享的资源：



- **FBO 帧缓冲区对象（不属于 buffer 类）；**
- **VAO 顶点数组对象（不属于 buffer 类）。**



这里解释下，**在不可以共享的资源中，FBO 和 VAO 属于资源管理型对象，FBO 负责管理几种缓冲区，本身不占用资源，VAO 负责管理 VBO 或 EBO ，本身也不占用资源**。



结论说完了，将在下一节进行结论验证，我们将在主渲染线程之外开辟一个新的渲染线程，然后将主渲染线程生成的纹理、 program 等资源分享给新的渲染线程使用。

# 共享上下文多线程渲染

![图片](assets/640-17122634875688.webp)



共享上下文多线程渲染流程



本小节将**在主渲染线程之外通过共享 EGLContext 的方式开辟一个新的离屏渲染线程，之后将主渲染线程生成的纹理、 program 、VBO 资源分享给新的渲染线程使用**，最后将保存（新渲染线程）渲染结果的纹理返回给主线程进行上屏渲染。

## 共享上下文

在 EGL_VERSION_1_4 （Android 5.0）版本，在当前渲染线程直接调用 eglGetCurrentContext 就可以直接获取到上下文对象 EGLContext 。



C++ ，Java 层均有对应获取上下文对象的 API 实现：



```
//Java
EGL14.eglGetCurrentContext();

//C++
#include "egl.h"
eglGetCurrentContext();
```



我们在新线程中使用 EGL 创建渲染环境时，通过主渲染线程获取的 sharedContext 来创建新线程的上下文对象。



```
EGLContext context = eglCreateContext(mEGLDisplay, config,
                                              sharedContext, attrib2_list);
```



由于我们在新线程要渲染到屏幕外的区域，需要创建 **PbufferSurface** 。



```
EGLSurface eglSurface = eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttribs);
```



国际惯例，我们将 EGL 的操作都封装到一个类 EglCore 中方便使用，具体代码可以参考文末的项目。

## 多线程渲染

类比 Android Java 层的 Looper 类，我们在 C++ 实现 Looper 用于创建新线程并管理线程中的消息。



```
class Looper {

public:
    Looper();
    Looper&operator=(const Looper& ) = delete;
    Looper(Looper&) = delete;
    virtual ~Looper();

    void postMessage(int what, bool flush = false);
    void postMessage(int what, void *obj, bool flush = false);
    void postMessage(int what, int arg1, int arg2, bool flush = false);
    void postMessage(int what, int arg1, int arg2, void *obj, bool flush = false);

    void quit();

    virtual void handleMessage(LooperMessage *msg);

private:
    void addMessage(LooperMessage *msg, bool flush);

    static void *trampoline(void *p);

    void loop(void);

    LooperMessage *head;
    pthread_t worker;
    sem_t headWriteProtect;
    sem_t headDataAvailable;
    bool running;

};
```



在 GLRenderLooper 类中分别定义 OnSurfaceCreated、 OnSurfaceChanged、 OnDrawFrame 用于处理对应的事件。



```
enum {
    MSG_SurfaceCreated,
    MSG_SurfaceChanged,
    MSG_DrawFrame,
    MSG_SurfaceDestroyed,
};

class GLRenderLooper : public Looper {
public:
    GLRenderLooper();
    virtual ~GLRenderLooper();

    static GLRenderLooper* GetInstance();
    static void ReleaseInstance();

private:
    virtual void handleMessage(LooperMessage *msg);

    void OnSurfaceCreated();
    void OnSurfaceChanged(int w, int h);
    void OnDrawFrame();
    void OnSurfaceDestroyed();

    bool CreateFrameBufferObj();

private:
    static mutex m_Mutex;
    static GLRenderLooper* m_Instance;

    GLEnv *m_GLEnv;
    EglCore *m_EglCore = nullptr;
    OffscreenSurface *m_OffscreenSurface = nullptr;
    GLuint m_VaoId;
    GLuint m_FboTextureId;
    GLuint m_FboId;
};
```



在函数 GLRenderLooper::OnSurfaceCreated 中，利用 sharedContext 创建 OpenGL 渲染环境。



```
void GLRenderLooper::OnSurfaceCreated() {

    //利用 sharedContext 创建 OpenGL 离屏渲染环境
    m_EglCore = new EglCore(m_GLEnv->sharedCtx, FLAG_RECORDABLE);
    SizeF imgSizeF = m_GLEnv->imgSize;
    m_OffscreenSurface = new OffscreenSurface(m_EglCore, imgSizeF.width, imgSizeF.height);
    m_OffscreenSurface->makeCurrent();

    glGenVertexArrays(1, &m_VaoId);
    glBindVertexArray(m_VaoId);

    glBindBuffer(GL_ARRAY_BUFFER, m_GLEnv->vboIds[0]);
    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 3 * sizeof(GLfloat), (const void *)0);
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);

    glBindBuffer(GL_ARRAY_BUFFER, m_GLEnv->vboIds[1]);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat), (const void *)0);
    glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m_GLEnv->vboIds[2]);
    GO_CHECK_GL_ERROR();
    glBindVertexArray(GL_NONE);

    if (!CreateFrameBufferObj())
    {
        LOGCATE("GLRenderLooper::OnSurfaceCreated CreateFrameBufferObj fail");
    }
}
```

**
**

GLRenderLooper::OnDrawFrame 函数中，**绘制完成注意交换缓冲区**，然后将保存绘制结果的纹理，通过回调函数传递给主线程进行上屏渲染。

**
**

```
void GLRenderLooper::OnDrawFrame() {
    LOGCATE("GLRenderLooper::OnDrawFrame");
    SizeF imgSizeF = m_GLEnv->imgSize;

    glBindFramebuffer(GL_FRAMEBUFFER, m_FboId);
    glViewport(0, 0, imgSizeF.width, imgSizeF.height);
    glUseProgram(m_GLEnv->program);
    glBindVertexArray(m_VaoId);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, m_GLEnv->inputTexId);
    GLUtils::setInt(m_GLEnv->program, "s_TextureMap", 0);
    float offset = (sin(m_FrameIndex * MATH_PI / 80) + 1.0f) / 2.0f;
    GLUtils::setFloat(m_GLEnv->program, "u_Offset", offset);
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, (const void *)0);
    glBindVertexArray(0);
    glBindTexture(GL_TEXTURE_2D, 0);

    //注意交换缓冲区
    m_OffscreenSurface->swapBuffers();
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    //将保存绘制结果的纹理 m_FboTextureId 传递给主线程进行上屏渲染    
    m_GLEnv->renderDone(m_GLEnv->callbackCtx, m_FboTextureId);
    m_FrameIndex++;
}
```



回到渲染主线程，Init 时**将主渲染生成的纹理、 program 、VBO 资源以及 EGLContext 传递给新线程**。



```
m_GLEnv.sharedCtx     = eglGetCurrentContext();
m_GLEnv.program       = m_FboProgramObj;
m_GLEnv.inputTexId    = m_ImageTextureId;
m_GLEnv.vboIds[0]     = m_VboIds[0];
m_GLEnv.vboIds[1]     = m_VboIds[2];
m_GLEnv.vboIds[2]     = m_VboIds[3];
m_GLEnv.imgSize       = imgSize;
m_GLEnv.renderDone    = OnAsyncRenderDone;//主线程回调函数
m_GLEnv.callbackCtx   = this;

//将共享的资源发送给新线程
GLRenderLooper::GetInstance()->postMessage(MSG_SurfaceCreated, &m_GLEnv);

GLRenderLooper::GetInstance()->postMessage(MSG_SurfaceChanged, m_RenderImage.width, m_RenderImage.height);
```



主线程渲染时，首先**向新线程发送渲染指令，然后等待其渲染结束**，新线程渲染结束后会调用 OnAsyncRenderDone 函数通知主线程进行上屏渲染。



```
void SharedEGLContextSample::Draw(int screenW, int screenH)
{
    {
        //向新线程发送渲染指令，然后等待其渲染结束
        unique_lock<mutex> lock(m_Mutex);
        GLRenderLooper::GetInstance()->postMessage(MSG_DrawFrame);
        m_Cond.wait(lock);
    }

    //主线程进行上屏渲染
    glViewport(0, 0, screenW, screenH);
    glUseProgram(m_ProgramObj);
    GO_CHECK_GL_ERROR();
    glBindVertexArray(m_VaoId);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, m_FboTextureId);
    GLUtils::setInt(m_ProgramObj, "s_TextureMap", 0);
    GO_CHECK_GL_ERROR();
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, (const void *)0);
    GO_CHECK_GL_ERROR();
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
    glBindVertexArray(GL_NONE);

}

void SharedEGLContextSample::OnAsyncRenderDone(void *callback, int fboTexId) {
    //新线程渲染结束后会调用 OnAsyncRenderDone 函数通知主线程进行上屏渲染
    SharedEGLContextSample *ctx = static_cast<SharedEGLContextSample *>(callback);
    unique_lock<mutex> lock(ctx->m_Mutex);
    ctx->m_FboTextureId = fboTexId;
    ctx->m_Cond.notify_all();
}
```



最后需要注意的是：**多线程渲染要确保纹理等共享资源不会被同时访问，否则会导致渲染出错。**



完整代码参考下面项目，选择 Multi-Thread Render：



```
https://github.com/githubhaohao/NDK_OpenGLES_3_0
```







-- END --



进技术交流群，扫码添加我的微信：Byte-Flow



![图片](assets/640-17122634875689.webp)



获取视频教程和源码





推荐：

[Android OpenGL ES 从入门到精通系统性学习教程](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654162516&idx=1&sn=6b19a9dbd38d15cc3dd47a446e5bd933&chksm=8cf39d67bb841471eed05c8d4452b6493c396cfc9746a3ee23781dfef178a161fd82e6b007c4&scene=21#wechat_redirect)

[全网最全的 Android 音视频和 OpenGL ES 干货，都在这了](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654164951&idx=1&sn=32f687de8fa113fe794194d565db686f&chksm=8cf384e4bb840df218e25eddd552c80b4e133e6d7577e7682a6ecf913be248c223c9d9928779&scene=21#wechat_redirect)

[你还不知道 OpenGL ES 和 EGL 的关系？](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654162834&idx=2&sn=6272e659b0ad5037743bcd748f576ca1&chksm=8cf39ca1bb8415b70cad95a3da3aa77fa684ad1c0d58e7d770156ba1defda9c955b429066977&scene=21#wechat_redirect)