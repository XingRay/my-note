# Android Camera2 预览输出大小

标签 [android](https://www.coder.work/blog?tag=android) [opengl-es](https://www.coder.work/blog?tag=opengl-es) [android-camera](https://www.coder.work/blog?tag=android-camera) [yuv](https://www.coder.work/blog?tag=yuv) [android-camera2](https://www.coder.work/blog?tag=android-camera2)



我正在尝试使用 Camera2 API 通过 ImageReader(YUV_420_888 格式)设置相机预览。首先，我需要选择支持的预览尺寸:

```
StreamConfigurationMap scmap = camCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
Size previewSizes[] = scmap.getOutputSizes(ImageReader.class);
```

我的 Nexus 5X 支持以下尺寸:

```
[4032x3024,4000x3000,3840x2160,3288x2480,3200x2400,2592x1944,2688x1512,2048x1536,1920x1080,1600x1200,1440x1080,1280x960,1280x768,1280x720,1024x768,800x600,864x480,800x480,720x480,640x480,640x360,352x288,320x240,176x144,160x120]
```

准备一个 ImageReader 的实例并使用重复的捕获请求启动 CaptureSession:

```
mImageReader = ImageReader.newInstance(W,H, ImageFormat.YUV_420_888,1);
```

然后我尝试在 OnImageAvailableListener 中读取每个预览帧(以便通过 GLES 进一步处理和显示)。我想知道的是 - 我收到了多少 Y channel 字节:

```
public void onImageAvailable(ImageReader reader) {
        ByteBuffer yBuffer = mImageReader.acquireNextImage().getPlanes()[0].getBuffer();
        Log.d("Camera2Debug","Y-channel bytes received: " + yBuffer.remaining());
        ...
    }
```

YUV_420_888 图像的 Y channel 应包含 **WxH** 字节，其中 **W** - 宽度，**H** - 高度考虑的形象。

**问题:** *对于某些支持的预览尺寸，yBuffer 的实际尺寸与预期值不匹配* (**WxH**)。

例如:

```
Preview Size  | Y-bytes received | Y-bytes expected  |   match
4032x3024     | 12 192 768       | 12 192 768        |    yes
1920x1080     |  2 073 600       |  2 073 600        |    yes
1440x1080     |  1 589 728       |  1 555 200        |    no
1280x960      |  1 228 800       |  1 228 800        |    yes
1280x768      |    983 040       |    983 040        |    yes
800x600       |    499 168       |    480 000        |    no
...
499168
```

因此，由于这个问题，即使设备支持，我也无法使用必要的预览尺寸。

我做错了什么？



**最佳答案**



您可能没有考虑 https://developer.android.com/reference/android/media/Image.Plane.html#getRowStride() ，它可能大于宽度(但至少等于它)。实际缓冲区大小将是

```
size = rowStride * height - (rowStride - width)
```

减去 (rowStride - width) 因为最后一行不包含超过最后一个像素的任何填充。



关于Android Camera2 预览输出大小，我们在Stack Overflow上找到一个类似的问题： https://stackoverflow.com/questions/40030533/