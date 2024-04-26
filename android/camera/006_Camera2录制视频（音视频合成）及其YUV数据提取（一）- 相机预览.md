# Camera2录制视频（音视频合成）及其YUV数据提取（一）- 相机预览

2021-10-222,386阅读5分钟

### 系列

- [***Camera2录制视频（音视频合成）及其YUV数据提取（一）- 相机预览***](https://juejin.cn/post/7021736151522213919)
- [Camera2录制视频（音视频合成）及其YUV数据提取（二）- YUV提取及图像转化](https://juejin.cn/post/7021793249278820359)
- [Camera2录制视频（音视频合成）及其YUV数据提取（二）- MediaCodec与MediaMuxe联合使用](https://juejin.cn/post/7022866113029472270)

# 相机预览

------

## 简介

Camera2 是最新的 Android 相机框架 API，它取代了已弃用的相机框架库。 Camera2 为复杂的用例提供深入的控制，但需要您管理特定于设备的配置。

------

# 使用

### 步骤（一）获取管理器

获取`Camera2 Manager`管理类,初始化子线程

```java
java复制代码private CameraManager mCameraManager;
private Handler mBgHandler;

this.mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
HandlerThread hThread = new HandlerThread("camera2");
this.mBgHandler = new Handler(hThread.getLooper())
```

**CameraManager** 是一个负责查询和建立相机连接的系统服务

### 步骤（二）获取相机信息

通过`CameraManager`获取当前设备的`camera list`列表,并用`cameraID`获取相应相机信息`CameraCharacteristics`

```java
java复制代码private String mRearCameraID, mFrontCameraID, mCurrentCameraId;
private CameraCharacteristics mRearCameraCs, mFrontCameraCs;

private void getCameraListCameraCharacteristics() {
    try {
        //获取当前手机的所有摄像头id
        String[] cameraList = this.mCameraManager.getCameraIdList();
        for (String id : cameraList) {
            //通过id获取对应相机信息结构
            CameraCharacteristics ccs = this.mCameraManager.getCameraCharacteristics(id);
            //获取前置/后置摄像头
            Integer facing = ccs.get(CameraCharacteristics.LENS_FACING);
            if (facing != null) {
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    //后置
                    this.mRearCameraCs = ccs;
                    this.mRearCameraID = id;
                    //设置默认后置
                    this.mCurrentCameraId = this.mRearCameraID;
                } else if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    //前置
                    this.mFrontCameraCs = ccs;
                    this.mFrontCameraID = id;
                }
            }
        }
    } catch (CameraAccessException e) {
        e.printStackTrace();
    }
}
```

**CameraCharacteristics** 相机信息提供类，类内部有很多相机信息常量，其中有代表相机方向的 `LENS_FACING`；判断闪光灯是否可用的 `FLASH_INFO_AVAILABLE`；获取所有可用 AE 模式的 `CONTROL_AE_AVAILABLE_MODES` 等。

这里主要去拿到前后置摄像头并且设置当前cameraId(`mCurrentCareamId`)默认为后置

### 步骤（三）寻找最佳预览尺寸

寻找最佳预览尺寸`BestPreviewSize`

```java
java复制代码private Size previewSize;

//判断是前置or后置拿到对应的相机信息
CameraCharacteristics tmpCss = this.mCurrentCameraId.equals(this.mRearCameraID) ? this.mRearCameraCs : this.mFrontCameraCs;
//获取摄像头支持的所有输出格式和尺寸
StreamConfigurationMap map = tmpCss.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
if (map != null) {
    //根据SurfaceTexture获取输出大小尺寸列表
    Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
    //寻找最佳尺寸
    this.previewSize = findSuitablePreviewSize(Arrays.asList(sizes));
    if (this.previewSizeCallback != null) {
        //预览尺寸回调
        this.previewSizeCallback.onBestSize(this.previewSize.getWidth(), this.previewSize.getHeight());
    }
}
```

**StreamConfigurationMap** 这是所有输出格式的权威列表（和大小分别 * 适用于该格式）相机设备支持。 这还包含每个格式/大小的最小帧持续时间和停顿持续时间

### 步骤（四）初始化ImageReader类

设置输出格式回调主要设置格式是`YUV_420_888`即`I420`

```java
java复制代码private ImageReader mImageReader;
private ImageReader.OnImageAvailableListener imageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireNextImage();
            //...后续主要代码
            //close必须调用
            image.close();
        }
    };
//初始化ImageReader传入预览尺寸宽高，设置相机输出图像格式I420
this.mImageReader = ImageReader.newInstance(this.previewSize.getWidth(), this.previewSize.getHeight(), ImageFormat.YUV_420_888, 1);
//设置相机数据回调监听
this.mImageReader.setOnImageAvailableListener(this.imageAvailableListener, this.mBgHandler);
```

Image对象的data被存储在Image类里面，构造参数`maxImages`控制了最多缓存几帧，新的images通过ImageReader的surface发送给ImageReader，类似一个队列，需要通过`acquireLatestImage()`或者`acquireNextImage()`方法取出Image。

> **需要在不使用完毕后将image.close()进行关闭，否则无法展示下一帧的图像以及数据会看到界面卡主现象**

### 步骤（五）打开相机

相机的准备操作

```java
java复制代码private CameraDevice mCameraDevice;//相机设备驱动
private CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
        }
    };


try {
    //设置需要打开的摄像头id，设置相机状态回调
    this.mCameraManager.openCamera(this.mCurrentCameraId, this.cameraStateCallback, this.mBgHandler);
} catch (CameraAccessException e) {
    e.printStackTrace();
}
```

**CameraDevice.StateCallback** 用于接收有关摄像机设备状态的更新的回调对象。必须提供此回调实例才能打开摄像机设备。 这些状态更新包括有关设备完成启动（允许调用`CameraDevice.createCaptureSession（SessionConfiguration）`），设备断开或关闭以及有关意外设备错误的通知。

### 步骤（六）相机预览

在此之前需要再xml中创建一个`TextureView`作为`预览View`进行`输出图像`,保证在`onSurfaceTextureAvailable`调用TextureView的draw方法时，如果还没有初始化SurfaceTexture。那么就会初始化它。初始化好时，就会回调这个接口。SurfaceTexture初始化好时，就表示可以接收外界的绘制指令。

```java
java复制代码private TextureView mAutoFitTextureView;
//相机请求构建者
private CaptureRequest.Builder mCaptureRequestBuilder;

this.mAutoFitTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        //ready
        startPreview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }
});
private void startPreview() {
    //获取TextureView中的SurfaceView
    SurfaceTexture surfaceTexture = this.mAutoFitTextureView.getSurfaceTexture();
    //设置预览数据中的缓冲区大小，按照最佳预览尺寸设置
    surfaceTexture.setDefaultBufferSize(this.previewSize.getWidth(), this.previewSize.getHeight());
    //创建一个新的surface用于添加target list
    Surface surface = new Surface(surfaceTexture);
    try {
        //创建预览请求 TEMPLATE_PREVIEW
        this.mCaptureRequestBuilder = this.mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        this.mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        //add布局中的Surface 用于预览图像
        this.mCaptureRequestBuilder.addTarget(surface);
        //将回调预览数据data给ImageReader的Surface对象，此时就会回调设置的ImageReader监听器
        this.mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
        //相机设备驱动创建session 第一个参数传入设置的target<Surface>list 上面加了下面list也要加入，第二个参数session状态回调，第三handler线程
        mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), this.cameraSessionStateCallback, this.mBgHandler);
    } catch (CameraAccessException e) {
        e.printStackTrace();
    }
}

private CameraCaptureSession mCameraCaptureSession;
private CameraCaptureSession.StateCallback cameraSessionStateCallback = new CameraCaptureSession.StateCallback() {
    @Override
    public void onConfigured(@NonNull CameraCaptureSession session) {
        mCameraCaptureSession = session;
        try {
           //利用session#setRepeatingRequest发送无限重复的指令（预览类型指令），第一个参数即传 请求构建者.build()该对象上面已经设置为预览（TEMPLATE_PREVIEW）类型
           mCameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
            }, mBgHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

    }
};
```

`CaptureRequest` 是向 `CameraCaptureSession` 提交 `Capture` 请求时的信息build，内部包括了本次 `Capture` 的参数配置和接收图像数据的 `Surface`,`CaptureRequest` 可配置的信息，包括图像格式、图像分辨率、传感器控制、闪光灯控制、3A 控制等等，绝大部分的相机参数都是通过 `CaptureRequest` 配置的。值得注意的是每一个 `CaptureRequest` 表示`一帧`画面的操作，这意味着你可以精确控制每一帧的 `Capture` 操作。

`CameraCaptureSession` 是配置了目标 `Surface` 的 `Pipeline` 实例，在使用相机功能之前必须先创建 `CameraCaptureSession` 实例。一个 `CameraDevice` 一次只能开启一个 `CameraCaptureSession`，大部分的相机操作都是通过向 `CameraCaptureSession` 提交一个 `Capture` 请求实现的，例如拍照、连拍、设置闪光灯模式、触摸对焦、显示预览画面等等。

当设置完`setRepeatingRequest`的时候再界面中就已经能看到相机的预览效果，并且也在不断的回调ImageReader监听

# 结尾

> **由于本内容动较多，加上camer2步骤还是比较繁琐的，下一篇会说一下再`YUV_420_888`的预览数据输出格式下如何提取`YUV420`（`I420`，`NV21`及`NV12` 都属于`YUV420范畴`）的数据进行图像操作**

![21-10-25-14-16-23_01.gif](./assets/998ba82e35f34fae90d4cdbdb851c70btplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

下一篇：[Camera2录制视频（音视频合成）及其YUV数据提取（二）- YUV提取及图像转化](https://juejin.cn/post/7021793249278820359)

标签：

[Android](https://juejin.cn/tag/Android)