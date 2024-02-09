# Android CameraX 知识点



### **1. 前言**



主要是`CameraX`中一些关键知识点的汇总介绍。并不会完整介绍`CameraX`的使用。

更多的是一些概念和注意点，以及名词介绍等内容。

### **2 CameraX 和 Camera2的区别**

`Android` 现在使用相机主要是通过`CameraX`和`Camera2`。常见应用的开发使用`CameraX`库调用相机就可以了。

因为`CameraX` 比`Camera2`简单，简单，简单。 很多功能都直接封装到`api`里面了方便我们调用，同时兼容性也高。

`CameraX`是基于`Camera2`软件包构建的。如果要低级别（更底层）的相机控件来支持复杂用例。那么`Camera2`就是我们的最优选择。

原先使用相机，需要自己配置很多选项。并且要注意相机对象和预览数据的释放等，配置繁琐。而`Google`封装了`Camera2`，帮我们简化了很多相机的配置和管理，让开发者只需要关注预览，拍照，分析。等实际场景。将这一整套方法库封装成了`CameraX`，后来合并到`Jetpack`库中。

#### **2.1 `CameraX`的特点：**

总结一下`CameraX`的一些特点，一家之言。仅供参考。

1. 支持Android API 21 及以上版本，覆盖现有Android设备的98%以上。（数据是官方提供的，更低版本不支持）
2. 易用性高：直接处理封装了预览，图片分析，图片拍摄，视频拍摄。以上功能直接提供api方便操作。
3. 兼容性强：不管什么设备，图片的宽高比，旋转角度，大小等全部封装统一了。
4. 扩展性好：提供Extensions API可以实现与原生相机应用相同的特性和功能。

也就是我们可以不用管相机的配置和销毁。关注于相机输出的图片等数据。

### **3. CameraX 知识**

我们常见的功能分为：

- 预览： 将Camera拍摄的数据实时在app指定区域进行显示，使用`PreviewView`主要进行预览显示
- 图片分析：将相机拍摄的图片进行数据分析，例如[人脸识别](https://cloud.tencent.com/product/facerecognition?from_column=20065&from=20065)，动作识别等都是需要相机拍摄的图片进行分析的。
- 图片拍摄：这个功能主要就是存储，将预览显示的图片效果，进行本地存储。
- 视频拍摄：主要通过VideoCapture类，将音视频数据进行存储。

我们app主要使用相机也是在这四个基本功能进行后续的业务实现。

而以上四个功能并不是必须按照顺序使用和调用的。他们都可以单独使用，例如我只需要图片分析，不需求其他功能等。

也可以全部组合使用，而常见的组合就是 预览+分析+拍摄了。

上面的功能就是用例了。而我们使用CameraX就是通过组合各种用例来达到要求了。

#### **3.1 生命周期**

CameraX在使用过程中，并不需要我们管理相机的生命周期，系统会自动帮助我们进行控制相机的打卡与关闭等。会基于当前页面的生命周期进行控制。是通过系统默认的`LifecycleOwner`进行控制的: 实例如下：通常this 是`Activity`或者`Fragment`

```javascript
 cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase);
```

复制

而如果我们想自己控制`CameraX`的生命周期那么可以通过自定义来实现了：

```javascript
public class MyActivity extends Activity implements LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
    }

    @Override
    public void onStart() {
        super.onStart();
        lifecycleRegistry.markState(Lifecycle.State.STARTED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }
}
```

复制

然后将`bindToLifecycle`中的 `lifecycleOwner`对象改为 我们自定义的`lifecycleRegistry`。

#### **3.2 设备等级**

如果要同时支持预览+视频拍摄，那么相机都能支持。但是如果想预览+视频拍摄+图片拍摄，那么需要`LIMITED`以及更好的相机，

而预览+视频拍摄+分析 就需要`LEVEL_3`以及更好的相机。

因为很多设备前后双摄的性能是不一样的。所以有些功能后摄支持而前摄不支持。那么如何了解呢？

可以通过`Camera2CameraInfo`类来了解。

实例：以下代码可检查默认的后置摄像头是否是 `LEVEL_3` 设备：

```javascript
@androidx.annotation.OptIn(markerClass = ExperimentalCamera2Interop.class)
Boolean isBackCameraLevel3Device(ProcessCameraProvider cameraProvider) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        List\ filteredCameraInfos = CameraSelector.DEFAULT_BACK_CAMERA
                .filter(cameraProvider.getAvailableCameraInfos());
        if (!filteredCameraInfos.isEmpty()) {
            return Objects.equals(
                Camera2CameraInfo.from(filteredCameraInfos.get(0)).getCameraCharacteristic(
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL),
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3);
        }
    }
    return false;
}
```

复制

等级范围为：`LEGACY < LIMITED < FULL < LEVEL_3`。

如果我们使用的用例组合（预览+分析+拍照+录像），那么会在首次调用 `createCaptureSession()`出现异常。

#### **3.3 选择摄像头**

CameraX 会根据应用的要求和用例自动选择最佳摄像头设备。如果您希望使用的设备与系统为您选择的设备不同，有以下几种选项供您选择：

- 使用 `CameraSelector.DEFAULT_FRONT_CAMERA` 请求默认的前置摄像头。
- 使用 `CameraSelector.DEFAULT_BACK_CAMERA`请求默认的后置摄像头。
- 使用 `CameraSelector.Builder.addCameraFilter()` 按 `CameraCharacteristics` 过滤可用设备的列表。

所有的摄像头设备都必须经过系统识别，并显示在CameraManager.getCameraIdList()中才能被我们使用。

而这个识别过程，是设备厂商需要完成的工作。

```javascript
CameraSelector cameraSelector =
        CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
```

复制

然后将cameraSelector传给CameraProvider进行配置相机就可以了。

#### **3.4 相机旋转角度**

默认预览Preview获取到的角度就是已经执行了选择的，而**ImageAnalysis**图片分析和图片拍照**ImageCapture**没有，需要我们根据传递过来的`imageProxy.getImageInfo().getRotationDegrees()`进行处理。

例如ImageCapture可以直接执行旋转：

```javascript
    ImageCapture imageCapture = new ImageCapture.Builder().build();

    OrientationEventListener orientationEventListener = new OrientationEventListener((Context)this) {
       @Override
       public void onOrientationChanged(int orientation) {
           int rotation;

           // Monitors orientation values to determine the target rotation value
           if (orientation >= 45 && orientation < 135) {
               rotation = Surface.ROTATION_270;
           } else if (orientation >= 135 && orientation < 225) {
               rotation = Surface.ROTATION_180;
           } else if (orientation >= 225 && orientation < 315) {
               rotation = Surface.ROTATION_90;
           } else {
               rotation = Surface.ROTATION_0;
           }

           imageCapture.setTargetRotation(rotation);
       }
    };

    orientationEventListener.enable();
```

复制

#### **3.5 分辨率**

CameraX默认会使用系统最佳分辨率展示，而图片拍摄**ImageCapture**和图片分析**ImageAnalysis**模式下，默认会采用`4:3`的宽高比值 也就是`640*480`。

CameraX会针对不同的用例匹配不同的分辨率。并不是全部一致的。默认情况下

**预览模式**：默认最高预览分辨率。照着屏蔽分辨率进行匹配的最佳尺寸。（可以调整）

**分析模式**：默认分辨率为640*480。（可以调整）

**拍摄模式**：默认最高可用分辨率，或与上述宽高比匹配的最高设备首选分辨率。

我们也可以通过`setTargetResolution(Size resolution)`指定特定的分辨率，进行分析拍摄。实例：

```javascript
ImageAnalysis imageAnalysis =
  new ImageAnalysis.Builder()
    .setTargetResolution(new Size(1280, 720))
    .build();
```

复制

上面是指定了分辨率，我们还可以通过指定宽高比来实现：`setTargetAspectRatio`

但是，这两个参数配置项不能同时设置，否则会出现`IllegalArgumentException` 异常。

也就是一个用例中不能给它设置宽高比的同时设置分辨率。

同时，可以通过`StreamConfigurationMap.getOutputSizes(int)`来查看当前设备支持的特点分辨率。

#### **3.6 闪光灯**

主要是拍照的时候回使用到闪光灯，而开启方法比较简单：

```javascript
ImageCapture mImageCapture = new ImageCapture.Builder().setFlashMode(ImageCapture.FLASH_MODE_AUTO).build();
```

复制

setFashMode ：是否开启闪光灯，而主要有四种模式：

- ImageCapture.FLASH_MODE_UNKNOWN：未知模式
- ImageCapture.FLASH_MODE_AUTO：根据环境光感自动开启闪光灯
- ImageCapture.FLASH_MODE_ON：每次都开启闪光灯
- ImageCapture.FLASH_MODE_OFF：关闭闪光灯

#### **3.7 相机控制**

我们可以通过相机控制实现变焦，手电筒，对焦测光（点按对焦），曝光补偿

```javascript
Camera camera = processCameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)

//可以配置常用的相机功能
CameraControl cameraControl = camera.getCameraControl()
// 可以查询这些常用相机功能的状态。
CameraInfo cameraInfo = camera.getCameraInfo()
```

复制

变焦：`CameraControl.setZoomRatio()`用于按变焦比例设置变焦,`CameraControl.setZoomRatio()`用于0到1.0之间的线性变焦。

手电筒：`CameraControl.enableTorch(boolean)` 可以启用或停用手电筒（也称为手电）。

对焦：`CameraControl.startFocusAndMetering()` 可根据指定的 FocusMeteringAction 设置 AF/AE/AWB 测光区域，以触发自动对焦和曝光测光。有许多相机应用通过这种方式实现“点按即可对焦”功能。

#### **3.8 VideoCapture** 

主要是相机录制视频的配置，其他都大同小异，主要介绍一些常见配置项：

```javascript
VideoCapture mVideoCapture = new VideoCapture.Builder().build();//用于录制视频
processCameraProvider.bindToLifecycle(this, mCameraSelector, mVideoCapture, mPreview);
```

复制

我们在VideoCapture进行build之前可以配置以下选项：

```javascript
        VideoCapture capture= new VideoCapture.Builder()
                .setVideoFrameRate()
                .setBitRate()
                .setAudioBitRate()
                ...
                .build()
```

复制

- setVideoFrameRate()：帧率，默认为30；
- setBitRate()：比特率，默认为8 * 1024 * 1024;
- setIFrameInterval()：帧间隔，默认1；
- setAudioBitRate()：音频比特率，默认为64000；
- setAudioSampleRate()：音频采集频率，默认8000；
- setAudioChannelCount()：音频通道数，默认1；
- setAudioMinBufferSize()：音频最小缓存大小，默认为1024；
- setMaxResolution()：最大分辨率，默认为1920, 1080
- setTargetAspectRatio()：宽高比，默认为16：9；

> 参考资料：https://developer.android.google.cn/training/camerax

