# Android OpenGL 渲染图像读取

**glReadPixels**

glReadPixels 是 OpenGL ES 的 API ，OpenGL ES 2.0 和 3.0 均支持。使用非常方便，下面一行代码即可搞定，但是效率很低。



```
glReadPixels(0, 0, outImage.width, outImage.height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
```



当调用 glReadPixels 时，**首先会影响 CPU 时钟周期，同时 GPU 会等待当前帧绘制完成，读取像素完成之后，才开始下一帧的计算，造成渲染管线停滞**。



值得注意的是 **glReadPixels 读取的是当前绑定 FBO 的颜色缓冲区图像**，所以当使用多个 FBO（帧缓冲区对象）时，需要确定好我们要读那个 FBO 的颜色缓冲区。



glReadPixels 性能瓶颈一般出现在大分辨率图像的读取，所以目前通用的优化方法是**在 shader 中将处理完成的 RGBA 转成 YUV （一般是 YUYV 格式），然后基于 RGBA 的格式读出 YUV 图像，这样传输数据量会降低一半，性能提升明显。**



**PBO**

PBO （**Pixel Buffer Object**）是 OpenGL ES 3.0 的概念，称为像素缓冲区对象，主要被用于异步像素传输操作。PBO 仅用于执行像素传输，不连接到纹理，且与 FBO （帧缓冲区对象）无关。



**PBO 类似于 VBO（顶点缓冲区对象），PBO 开辟的也是 GPU 缓存，而存储的是图像数据。**

**
**

PBO 可以在 GPU 的缓存间快速传递像素数据，不影响 CPU 时钟周期，除此之外，PBO 还支持异步传输。



**PBO 类似于“以空间换时间”策略，在使用一个 PBO 的情况下，性能无法有效地提升，通常需要多个 PBO 交替配合使用。**



![图片](D:\my-note\opengl\assets\640.png)2 个 PBO read pixels

如上图所示，利用 2 个 PBO 从帧缓冲区读回图像数据，使用 glReadPixels 通知 GPU 将图像数据从帧缓冲区读回到 PBO1 中，同时 CPU 可以直接处理 PBO2 中的图像数据。



关于 PBO 的详细使用可以参考文章：[OpenGL ES 3.0 开发连载（22）：PBO ](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654161764&idx=1&sn=cae0909d8ce82051d38fa0182704a25b&chksm=8cf39857bb84114183b72746c90f9bfa5408547ab464b323f793513848ee4aaac56e61e37c69&scene=21#wechat_redirect)， 这里不再赘述。



**ImageReader**

ImageReader 是 Android SDK 提供的 Java 层对象，其内部会创建一个 Surface 对象。



常用于 Android Camera2.0 相机预览，通过 addTarget 将 Surface 对象作为相机预览图像的输出载体，通过回调接口获取预览图像。



```
mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 2);
mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
mSurface = mImageReader.getSurface();

private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
    @Override
    public void onImageAvailable(ImageReader reader) {
        Image image = reader.acquireLatestImage();
        if (image != null) {
            //处理相机预览图像 image
            image.close();
        }
    }
};
```



那么 ImageReader 怎么跟 OpenGL ES 结合使用呢？



**我们知道利用 EGL 创建 OpenGL 上下文环境时，eglCreateWindowSurface 需要传入 ANativeWindow 对象，而 ANativeWindow 又基于 Surface 对象创建的。**



**那我们可以利用 ImageReader 对象的 Surface 对象作为 OpenGL 展示渲染结果的 Window Surface ，每次渲染的结果可以通过 ImageReader 对象的回调获取。**



**HardwareBuffer**

HardwareBuffer 是一个更底层的对象，**代表可由各种硬件单元访问的缓冲区**。



特别地，HardwareBuffer 可以映射到各种硬件系统的存储器，例如 GPU 、 传感器或上下文集线器或其他辅助处理单元。



**HardwareBuffer 是 Android 8 API >= 26 提供的用于替换 GraphicBuffer 的接口，在 API <= 25 时可以使用 GraphicBuffer 。**

**
**

**两者在使用步骤上基本一致，均可以用于快速读取显存（纹理）图像数据，但是 HardwareBuffer 还可以访问其他硬件的存储器，使用更广泛。**



Android 在 Native 层和 Java 层均提供了 HardwareBuffer 实现接口，其中 Native 层叫 AHardwareBuffer 。



AHardwareBuffer 读取显存（纹理）图像数据时，需要与 GLEXT 和 EGLEXT 配合使用 。



主要步骤：**首先需要创建 AHardwareBuffer 和 EGLImageKHR 对象，然后将目标纹理（FBO 的颜色附着）与 EGLImageKHR 对象绑定，渲染结束之后便可以读取纹理图像。**



HardwareBuffer 读取纹理图像数据：



```
unsigned char *ptrReader = nullptr;
AHardwareBuffer_lock(m_HwBuffer, AHARDWAREBUFFER_USAGE_CPU_READ_OFTEN, -1, nullptr,(void **) &ptrReader);
memcpy(dstBuffer, ptrReader, imgWidth * imgHeight * 3 / 2);//直接可以读取 YUV 图像（NV21）
int32_t fence = -1;
AHardwareBuffer_unlock(m_PHwBuffer, &fence);
```



另外，HardwareBuffer 支持直接读取纹理中的 YUV （YUV420）格式的图像，只需要在 shader 中实现 RGB 到 YUV 的格式转换。



GLES 3.0 YUV 扩展直接支持 RGB 到 YUV 的转换：



```
#version 300 es
#extension GL_EXT_YUV_target: require
precision mediump float;
in vec2 v_texCoord;
layout(yuv) out vec4 outColor;
uniform sampler2D s_texture;
void main()
{
    //色彩空间标准公式
    yuvCscStandardEXT conv_standard = itu_601_full_range;
    vec4 rgbaColor = texture(s_texture, v_texCoord);
    //dealwith rgba
    vec3 rgbColor = rgbaColor.rgb;
    vec3 yuv = rgb_2_yuv(rgbColor, conv_standard);//实现 RGB 到 YUV 的格式转换
    outColor = vec4(yuv, 1.0);
}
```



HardwareBuffer 和 GraphicBuffer 具体使用可以参考：https://github.com/fuyufjh/GraphicBuffer



**实测性能对比**

通过在 SDM8150手机上，对比读取相同格式 3k 左右分辨率图像的性能，其中 ImageReader、 PBO 和 HardwareBuffer 明显优于 glReadPixels 方式。



HardwareBuffer、 ImageReader 以及 PBO 三种方式性能相差不大，但是理论上 HardwareBuffer 性能最优。



四种方式中，glReadPixels 使用最方便，HardwareBuffer 实现最复杂，实现复杂度：HardwareBuffer > PBO > ImageReader > glReadPixels 。



结合实测性能和实现难度，Native 层建议选择 PBO 方式，超大分辨率建议尝试 HardwareBuffer 方式，Java 层建议使用 ImageReader 方式。

