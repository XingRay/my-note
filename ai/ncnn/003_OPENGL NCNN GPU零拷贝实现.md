# OPENGL NCNN GPU零拷贝实现

## 概要

OPENGL拿到的相机帧，通过有拷贝的方式进行GPU推理CPU占用率太高，而NCNN没有提供OPENGL零拷贝GPU推理的接口，因此只能自己实现



## 整体流程

主要方法是使用Android Hardware Buffer 实现纹理的共享，在OPENGL上对相机数据进行预处理后，将纹理信息写入到Android Hardware Buffer，随后在vulkan上进行转格式，最后使用NCNN的GPU推理，实现GPU的零拷贝。

非官方实现，主要是多了一步RGBA转RGB的操作，会有几个ms的开销，如果从零开始的话，建议尝试其他自带OPENGL零拷贝接口的推理框架。



## 具体实现

首先在opengl初始化的时候创建Android Hardware Buffer，这边创建的是一块320*256大小的RGBA unsigned char内存块。将buffer绑定到EGLimage上，再将EGLimage与opengl的纹理进行绑定。

    AHardwareBuffer_Desc desc = {
        .width = 320,
        .height = 256,
        .layers = 1,
        .format = AHARDWAREBUFFER_FORMAT_R16G16B16A16_FLOAT,
        .usage = AHARDWAREBUFFER_USAGE_GPU_COLOR_OUTPUT| AHARDWAREBUFFER_USAGE_GPU_FRAMEBUFFER | AHARDWAREBUFFER_USAGE_GPU_SAMPLED_IMAGE ,
    };
    int ret = AHardwareBuffer_allocate(&desc, &buffer);
    if (ret != 0) {
        ALOGE("AHardwareBuffer_allocate error");
    }
    //通过EGL与opengl纹理进行绑定
    EGLClientBuffer clientBuffer = eglGetNativeClientBufferANDROID(buffer);
    if (!clientBuffer) {
        ALOGE("clientBuffer error");
    }
    if (EglImage != EGL_NO_IMAGE_KHR) {
        eglDestroyImageKHR(display, EglImage);
    }
    EGLint eglImageAttributes[] = { EGL_NONE };
    EglImage = eglCreateImageKHR(display, EGL_NO_CONTEXT, EGL_NATIVE_BUFFER_ANDROID,
        clientBuffer, eglImageAttributes);
    if (EglImage == EGL_NO_IMAGE_KHR) {
        ALOGE("EglImage error");
    }
     
    //建立faceDetectAlignFramerbuffer
    faceDetectAlignFramerbuffer = 0;
    glGenFramebuffers(1, &faceDetectAlignFramerbuffer);
    glGenTextures(1, &faceDetectAlignTexture);
    glBindFramebuffer(GL_FRAMEBUFFER, faceDetectAlignFramerbuffer);
    glBindTexture(GL_TEXTURE_2D, faceDetectAlignTexture);
    glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, (GLeglImageOES)EglImage);
usage这里的标志位自测没什么影响，改成一个或者CPU那些标志位也不影响使用，时间上或许有影响，没测过。

绑定完后就可以按照FBO的流程对这块纹理进行绘制，绘制完毕后数据就被写入到Android Hardware Buffer上了。注意在glDrawElements后要加上glFinish()，否则Android Hardware Buffer会没有数据（正常渲染是不需要加glFinish的，但是渲染到Android Hardware Buffer就必须要加，具体原因我也不清楚，了解的朋友可以告诉我）。

此时，Android Hardware Buffer就已经有了相机的数据了，那么接下来就是把Android Hardware Buffer绑定到VkImageMat上。

头文件定义：

	ncnn::Option opt;		
	ncnn::VulkanDevice* g_vkdev = 0;
	ncnn::VkAllocator* g_blob_vkallocator = 0;
	ncnn::VkAllocator* g_staging_vkallocator = 0;
	ncnn::UnlockedPoolAllocator g_blob_pool_allocator;
	ncnn::PoolAllocator g_workspace_pool_allocator;
	ncnn::VkImageMat VkImageMatOut;
	ncnn::Mat MatOut;
	ncnn::VkImageMat VkImageMatIn;
	ncnn::VkImageMat VkImageMatOrigin;
	ncnn::VkCompute *vkcompute;
	ncnn::ImportAndroidHardwareBufferPipeline *import_pipeline;
	ncnn::VkAndroidHardwareBufferImageAllocator* ahb_im_allocator;
初始化函数：

```
int initParam(AHardwareBuffer* buffer, int w, int h, int c)
{	
        g_vkdev = ncnn::get_gpu_device(0);
		g_blob_vkallocator = new ncnn::VkBlobAllocator(g_vkdev);
		g_staging_vkallocator = new ncnn::VkStagingAllocator(g_vkdev);
		opt.blob_vkallocator = g_blob_vkallocator;
		opt.workspace_vkallocator = g_blob_vkallocator;
		opt.staging_vkallocator = g_staging_vkallocator;
		g_blob_vkallocator->clear();
		g_staging_vkallocator->clear();
 
		opt.lightmode = true;
		opt.num_threads = 1;
		g_blob_pool_allocator.set_size_compare_ratio(0.0f);
		g_workspace_pool_allocator.set_size_compare_ratio(0.5f);
		opt.blob_allocator = &g_blob_pool_allocator;
		opt.workspace_allocator = &g_workspace_pool_allocator;
		opt.use_winograd_convolution = true;
		opt.use_sgemm_convolution = true;
		opt.use_int8_inference = true;
		opt.use_vulkan_compute = true;
		opt.use_fp16_packed = true;
		opt.use_fp16_storage = true;
		opt.use_fp16_arithmetic = true;
		opt.use_int8_storage = true;
		opt.use_int8_arithmetic = true;
		opt.use_packing_layout = false;
		opt.use_shader_pack8 = false;
		opt.use_image_storage = true;
 
		g_blob_pool_allocator.clear();
		g_workspace_pool_allocator.clear();
 
		pNet->opt = opt;
		pNet->set_vulkan_device(g_vkdev);
 
		import_pipeline = new ncnn::ImportAndroidHardwareBufferPipeline(g_vkdev);
		vkcompute = new ncnn::VkCompute(g_vkdev);
		ahb_im_allocator = new ncnn::VkAndroidHardwareBufferImageAllocator(g_vkdev, buffer);
		import_pipeline->create(ahb_im_allocator, 1, 1, w, h, opt);
		VkImageMatOrigin.create(w, h, c, sizeof(float16_t), ahb_im_allocator);
		VkImageMatIn.create(w, h, 3, sizeof(float16_t), g_blob_vkallocator);
}
```

w,h,c代表Android Hardware Buffer的width，height和channel，这里分别为320,256,4。

执行函数：

		ncnn::Extractor ex = pNet->create_extractor();
		ex.set_blob_vkallocator(g_blob_vkallocator);
		ex.set_workspace_vkallocator(g_blob_vkallocator);
		ex.set_staging_vkallocator(g_staging_vkallocator);
	 
		vkcompute->record_import_android_hardware_buffer(import_pipeline, VkImageMatOrigin, VkImageMatIn);
		ex.input(FirstNodeNam, VkImageMatIn);
		ex.extract(EndNodeName, VkImageMatOut, *vkcompute);
		vkcompute->record_download(VkImageMatOut, MatOut, opt);
		vkcompute->submit_and_wait();
		vkcompute->reset();
最后修改NCNN convert_ycbcr.comp文件，把

```
vec3 rgb = texture(android_hardware_buffer_image, pos).rgb * 255.f;
```

改成

```
vec3 rgb = texture(android_hardware_buffer_image, pos).rgb;
```

修改完毕后再重新编译，这样就可以成功运行NCNN的GPU零拷贝了。

以下是在8155下运行320*256大小depth_multiple: 0.2，width_multiple: 0.15 的yolov5n的对比：

|                | 单人脸检测速度 | CPU使用率 | GPU使用率 |
| :------------: | :------------: | :-------: | :-------: |
| MNN GPU有拷贝  |      30ms      |    66%    |    22%    |
| NCNN GPU零拷贝 |      25ms      |    21%    |    65%    |

检测速度指的是整个线程执行一帧的时间，包含opengl的渲染、数据的拷贝以及后处理。

CPU使用率指单核的使用率。

因芯片性能、运行环境不同，性能对比仅供参考。



## 细节

我是在ncnn 20231027版本上实现的，其他版本没有测试过。

如果Android Hardware Buffer可以创建R16G16B16或者R32G32B32的数据，就不需要上述这些操作直接进行NCNN GPU推理了，但是我尝试下来Android Hardware Buffer并不支持。就算创建R16G16B16A16内存块，且OPENGL输出R16G16B16，最后得到的也是R16G16B16A16的数据。（有了解的朋友可以告诉我）

后来参考这里的代码

https://github.com/yyangoO/cam-ncnn-win/blob/0412b3767c0e65f7b81379fa7a73be459788baf9/app/src/main/jni/main_activity_jni.cpp#L784

在他的注释里面有数据的转换record_import_android_hardware_buffer，不过他转的是yuv420，一开始看代码的时候，ImportAndroidHardwareBufferPipeline的create_sampler函数有用到VkSamplerYcbcrConversionInfoKHR结构体。

GPT对它的描述：用于将纹理数据从 Y'CbCr 格式转换为 RGB 格式的过程中使用的采样器转换对象。它是使用 Vulkan 中的 VK_KHR_sampler_ycbcr_conversion 扩展实现的。

以为RGBA转不了，后来试了一下，这个VkSamplerYcbcrConversionInfoKHR也可以转RGBA，所以就打算用record_import_android_hardware_buffer进行RGBA到RGB的转换。

花了几天尝试后，发现down的图没有进行归一化，只能继续看NCNN的代码，找到record_import_android_hardware_buffer的vulkan着色器文件convert_ycbcr.comp，发现它在绘制的时候乘了255.f，把这段去掉就可以得到归一化的图了。

另外

```
import_pipeline->create(ahb_im_allocator, 1, 1, w, h, opt);
```

这个函数的实现在pipeline.cpp里，第二第三个参数代表着色器代码里的type_to和rotate_from，因为我需要将RGBA转为RGB，所以第二个参数为1，我不需要旋转，所以第三个参数为1。

目前尝试下来AHARDWAREBUFFER_FORMAT_R16G16B16A16_FLOAT和AHARDWAREBUFFER_FORMAT_R8G8B8A8_UNORM都可以实现，AHARDWAREBUFFER_FORMAT_R8G8B8_UNORM,AHARDWAREBUFFER_FORMAT_Y8Cb8Cr8_420没试过但应该也是可行的，其他的16、32数据类型都试过不可行，在绑定到opengl纹理的时候就报错了。为什么不直接用AHARDWAREBUFFER_FORMAT_R8G8B8_UNORM进行推理，因为模型的输入必须是半精度float或者float型的，所以还是需要转。

最后，VkImageMat的定义一定要放到初始化函数里，如果放到执行函数里，它会造成函数内外执行时间的不一致，也就是在函数内部测出来时间没增加，但是在函数外部测出来时间增加了，我用过两种计时工具都测出来时间不一致，具体原因我也不是很清楚，了解的朋友可以告诉我一下。

======================================================================

2024 2.28更新----------无转格式版NCNN 零拷贝
======================================================================

一次偶然间发现，AHARDWAREBUFFER_FORMAT_R8G8B8_UNORM的android hardware buffer放到float16_t的vulkanimageMat里去做推理是可行的。

将vulkanimageMat down到本地，查看图像，和原来一致。验证人脸置信度和FD的效果，也和之前一样。

那么就可以跳过原来转格式这个步骤，直接opengl渲染输出R8G8B8的图像到android hardware buffer，然后直接进行推理。

代码如下：

AHardwareBuffer的创建和之前一样，唯一的区别是format改成AHARDWAREBUFFER_FORMAT_R8G8B8_UNORM。

```
void Init(AHardwareBuffer* buffer)
{
  ahb_im_allocator = new ncnn::VkAndroidHardwareBufferImageAllocator(g_vkdev, buffer);
  VkImageMatOrigin.create(w, h, 3, sizeof(float16_t), ahb_im_allocator);
}
```



```
void Process()
{
  	ncnn::Extractor ex = pNet->create_extractor();
	ex.set_blob_vkallocator(g_blob_vkallocator);
	ex.set_workspace_vkallocator(g_blob_vkallocator);
	ex.set_staging_vkallocator(g_staging_vkallocator);
 
	ex.input(FirstNodeName, VkImageMatOrigin);
	ex.extract(EndNodeName, VkImageMatOut, *vkcompute);
	vkcompute->record_download(VkImageMatOut, MatOut, opt);
	vkcompute->submit_and_wait();
	vkcompute->reset();
}
```

因为无需转格式，所以之前的修改NCNN convert_ycbcr.comp文件步骤也不需要了，直接使用原版NCNN就可以。

最后测一下速度：

|                        | 单人脸检测速度 | CPU使用率 | GPU使用率 |
| :--------------------: | :------------: | :-------: | :-------: |
|     MNN GPU有拷贝      |      30ms      |    66%    |    22%    |
|     NCNN GPU零拷贝     |      25ms      |    21%    |    65%    |
| NCNN GPU无转格式零拷贝 |      16ms      |    25%    |    57%    |

尝试过在opengl输出数据时对其做过减均值操作，但是会导致图像异常。因为unsigned char的范围是0-255。减均值会使数据截断，从而造成图像异常。这也从侧面说明了opengl 输出的确实是unsigned char数据，也就是说在vulkanimageMat里存的是unsigned char数据。个人猜测在推理的时候unsigned char被转为了float16_t并进行了归一化。

所以这种无转格式的方式对数据的预处理有要求，即模型在训练时均值必须为0，且只有一个/255的归一化操作。否则要想在推理时保持一致，则仍需转格式的方式去修改数据的值。

这种方式属于野路子方法，因为是直接把unsigned char数据放在float16_t数组里去做的推理，可能会有一些未知的问题。正规的方式还是得使用转格式的形式去做推理。



## 总结

因项目需要实现GPU零拷贝，从开始编译NCNN到最后实现GPU零拷贝，总共花了2个多月时间。感谢nihui和NCNN交流群的各位朋友们帮忙答疑，如果nihui当初不说logcat 搜索 ncnn，logcat显示no vulkan device我甚至不知道设备没有开启vulkan，也就没有后面的实现了。

在这里也抛砖引玉，分享NCNN 的OPENGL GPU零拷贝的实现，如果大家有更好的实现方式，可以一起交流。
