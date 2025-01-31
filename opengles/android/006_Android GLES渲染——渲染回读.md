# Android GLES渲染——渲染回读

## 渲染回读

渲染数据回读在项目上经常会使用到，不管是用于排查渲染流程还是用于后续其他的操作，如何高效的获取渲染数据是项目在实际开发过程中需要重点考虑的。如下整理了Android侧常用的渲染回读(部分属于GL通用方案，即其他端侧亦可使用)



## glReadPixels

glReadPixels 是 OpenGL ES 的 API ，OpenGL ES 2.0 和 3.0 均支持。 使用非常方便，下面一行代码即可搞定，但是效率也是最低的。

glReadPixels(0, 0, UI_WIDTH, UI_HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, pScreen);
glReadPixel是最方便，也是最常用的渲染数据回读方案，其回读的数据主要是当前渲染绑定的FBO的数据。因此，若存在多个FBO，则需要关注当前bind的FBO是否是需要回读的FBO。

glReadPixel的问题在于，当调用 glReadPixels 时， GPU 会等待当前帧绘制完成，相当于调用了一次glFinish，读取像素完成之后，才开始下一帧的计算，造成渲染管线停滞。 不仅如此，由于串行执行GPU->CPU数据的拷贝，如果数据量大，频繁执行会导致CPU占用率显著提高。、



## PBO

OpenGL PBO（Pixel Buffer Object），被称为像素缓冲区对象，主要被用于异步像素传输操作。PBO 仅用于执行像素传输，不连接到纹理，且与 FBO （帧缓冲区对象）无关。类似于VBO，PBO开辟的也是GPU的缓存，不同于VBO，PBO存储的是图像数据。

在使用PBO时，常见的标签为GL_PIXEL_UNPACK_BUFFER 和 GL_PIXEL_PACK_BUFFER。绑定为 GL_PIXEL_UNPACK_BUFFER 表示该 PBO 用于将像素数据从程序传送到 OpenGL 中；绑定为 GL_PIXEL_PACK_BUFFER 表示该 PBO 用于从 OpenGL 中读回像素数据。 本文主要解释如何用PBO实现渲染的回读(PBO实现纹理更新方式类似，绑定PBO后，使用glTexImage2D或者glTexSubImage2D，借助PBO的异步操作更新GPU纹理数据，减少了CPU的等待)。 首先是创建标签为GL_PIXEL_PACK_BUFFER的PBO

```
int imgByteSize = m_Image.width * m_Image.height * 4;//RGBA
glGenBuffers(1, &downloadPboId); 
glBindBuffer(GL_PIXEL_PACK_BUFFER, downloadPboId); 
glBufferData(GL_PIXEL_PACK_BUFFER, imgByteSize, 0, GL_STREAM_DRAW);
glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
```

渲染后，回读数据

```
glBindBuffer(GL_PIXEL_PACK_BUFFER, downloadPboId);
glReadPixels(0, 0, UI_WIDTH, UI_HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, pScreen);
//或者使用glMapBufferRange///
GLubyte *bufPtr = static_cast<GLubyte *>(glMapBufferRange(GL_PIXEL_PACK_BUFFER, 0,
                                                           dataSize,
                                                           GL_MAP_READ_BIT));
 
if (bufPtr) {
    nativeImage.ppPlane[0] = bufPtr;
    //NativeImageUtil::DumpNativeImage(&nativeImage, "/sdcard/DCIM", "PBO");
    glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
}
//
```

经过实测，PBO相对于直接调用glReadPixel,通过PBO异步的特性，可以很好的减低CPU的使用。



## HardwareBuffer

HardwareBuffer，即native的AHardwareBuffer的Java层包装，官方介绍为一种底层的内存 buffer 对象，可在不同进程间共享，可映射到不同硬件系统，如 GPU、传感器等。

该部分主要介绍Android在API26开放的native层接口AHardwareBuffer实现渲染回读。

    // 离屏渲染
    void SeihoRenderContext::CreateOffScreenRenderOESTexture(int outputWidth, int outputHeight) {
        绑定离屏渲染的framebuffer和texture，同时用AHardwarebuffer绑定到OES。
     
        创建Framebuffer
        glGenFramebuffers(1, &outputFramebufferID);
        glBindFramebuffer(GL_FRAMEBUFFER, outputFramebufferID);
        GLUtils::CheckGLError("glGenFramebuffers");
         创建纹理
        glGenTextures(1, &outputFrameBufferTextureID);
        glBindTexture(GL_TEXTURE_2D, outputFrameBufferTextureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLUtils::CheckGLError("glGenTextures");
        AHardwareBuffer_Desc desc = {
                static_cast<uint32_t>(UI_WIDTH),
                static_cast<uint32_t>(UI_HEIGHT),
                1,
                AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM,
                AHARDWAREBUFFER_USAGE_CPU_READ_NEVER | AHARDWAREBUFFER_USAGE_CPU_WRITE_NEVER |
                AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE | AHARDWAREBUFFER_USAGE_GPU_COLOR_OUTPUT,
                0,
                0,
                0};
        AHardwareBuffer_allocate(&desc, &outputHWBuffer);
        EGLint eglImageAttributes[] = {EGL_IMAGE_PRESERVED_KHR, EGL_TRUE, EGL_NONE};
        EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        clientBuffer = eglGetNativeClientBufferANDROID(outputHWBuffer);
        eglImage_ = eglCreateImageKHR(display, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID,
                                             clientBuffer, eglImageAttributes);
        if (eglImage_ == EGL_NO_IMAGE_KHR) {
            GLUtils::CheckGLError("eglCreateImageKHR");
        }
        GLUtils::CheckGLError("eglCreateImageKHR");
        glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, (GLeglImageOES) eglImage_);
        GLUtils::CheckGLError("glEGLImageTargetTexture2DOES");
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, outputFrameBufferTextureID, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_RENDERBUFFER, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
上述代码主要通过EGLImageKHR和AHardwareBuffer绑定，实现渲染的回读。EGLImage 来自于 EGL 的一个扩展 EGL_KHR_image_base，用于在不同的 EGL 环境之间共享数据，而 Android 系统通过另一个扩展 EGL_ANDROID_get_native_client_buffer 支持从 HardwareBuffer 创建 EGLImage。AHardwareBuffer实现回读，必须将EGLImage，通过glEGLImageTargetTexture2DOES绑定。OES纹理为OpenGL ES的拓展，在Android侧，OES纹理可以同GraphicBuffer共享，实现零拷贝，而AHardwareBuffer本质依旧是GraphicBuffer。

至此，AHardwareBuffer和FBO的绑定完成。通过如下代码回读即可。

```
unsigned char *ptrReader = nullptr;
ret = AHardwareBuffer_lock(inputHWBuffer, AHARDWAREBUFFER_USAGE_CPU_READ_OFTEN, -1, nullptr, (void **) &ptrReader); 
/ptrReader已拿到了渲染的纹理，后续可以任意操作。
ret = AHardwareBuffer_unlock(inputHWBuffer, nullptr);
```

经过项目侧测试，AHardwareBuffer可以完全实现回读零拷贝，也不占用CPU时钟周期，若数据在native层传递，或Java层通过SurfaceTexture、HardwareBuffer传递，AHardwareBuffer的渲染回读应该作为首选。