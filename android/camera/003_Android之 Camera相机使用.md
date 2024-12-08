Android之 Camera相机使用

12345，catch a tiger

已于 2023-04-26 22:04:54 修改

阅读量2.9k
 收藏 14

点赞数 1
文章标签： android 数码相机
版权
一 简介

1.1 随着信息时代的发展，相机在我们生活中使用越来越频繁，也成为手机的基本配置之一。相机可以用来拍照，拍视频，人脸识别，视频聊天，扫码支付，监控等常见领域

不管什么场景，基本原理都差不多，都要先通过相机采集原始数据，也就是二进制字节数据，我们可以对原始数据做对应的操作，比如保存成图片，或者分析数据内容等等。

1.2 Android相机的API到目前发展了3个版本，如下面官方api文档所示



Camera
此类是用于控制设备相机的旧版 API，现已弃用，在Android5.0以下使用
Camera2
此软件包是用于控制设备相机的主要 API，Android5.0以上使用
CameraX
基于Camera 2 API封装，简化了开发流程，并增加生命周期控制

1.3 各版本优点

Camera

检测设备摄像头，打开相机
创建预览画面，显示实时预览画面
设置相机参数，进行拍照监听
监听中，保存图片资源或者直接操作原始数据
释放相机资源
Camera2

改进了新硬件的性能。Supported Hardware Level的概念，不同厂商对Camera2的支持程度不同，从低到高有LEGACY、LIMITED、FULL 和 LEVEL_3四个级别
以更快的间隔拍摄图像
显示来自多个摄像机的预览
直接应用效果和滤镜
CameraX

CameraX 是一个 Jetpack 支持库，目的是简化Camera的开发工作，它是基于Camera2 API的基础，向后兼容至 Android 5.0（API 级别 21）。
易用性，只需要几行代码就可以实现预览和拍照
保持设备的一致性，在不同相机设备上，对宽高比、屏幕方向、旋转、预览大小和高分辨率图片大小，做到都可以正常使用
相机特性的扩展，增加人像、HDR、夜间模式和美颜等功能
1.4 如果对相机要求不高，只是单纯的解析数据，那用Camera就可以了。虽然官方说已过时，但还是有非常多的场合使用。 

二 基于扫码支付的Camera的使用 

2.1 添加相机权限

<uses-feature
   android:name="android.hardware.camera"
   android:required="true" />

<uses-permission android:name="android.permission.CAMERA" />
2.2 检查相机权限

//权限请求
public final int REQUEST_CAMERA_PERMISSION = 1;
private String cameraPermission = Manifest.permission.CAMERA;

private void checkCameraPermission() {
	//检查是否有相机权限
	if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
		//没权限，请求权限
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
				REQUEST_CAMERA_PERMISSION);
	} else {
		//有权限
		createSurfaceView();
	}
}

//权限请求回调
@Override
public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
	switch (requestCode) {
		case REQUEST_CAMERA_PERMISSION:
			if (grantResults != null && grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//用户同意权限
				createSurfaceView();
			} else {
				// 权限被用户拒绝了，可以提示用户,关闭界面等等。
				Toast.makeText(this, "拒绝权限，请去设置里面手动开启权限", Toast.LENGTH_SHORT).show();
			}
			break;
	}
}

2.3 用SurfaceView来加载相机画面

/**
 * 创建预览画面
 */
 private SurfaceView surfaceView;
 private SurfaceHolder mHolder;

public void createSurfaceView() {
	//创建预览
	surfaceView = new SurfaceView(this);
	flContent.removeAllViews();
	flContent.addView(surfaceView);
	//获取预览的管理器
	mHolder = surfaceView.getHolder();
	//监听预览状态
	mHolder.addCallback(new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
			//预览控件创建成功的时候，打开相机并预览
			openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
		}

		@Override
		public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
	 
		}
	 
		@Override
		public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
			//预览控件销毁的时候，释放相机资源
			releaseCamera();
		}
	});
}
2.4 打开相机

private Camera mCamera;
private Camera.CameraInfo cameraInfo;

/**
 * 打开相机
 * Camera.CameraInfo.CAMERA_FACING_FRONT前置
 * Camera.CameraInfo.CAMERA_FACING_BACK后置
 *
 * @param cameraIndex 摄像头的方位
 */
 public void openCamera(int cameraIndex) {
	try {
		//先释放相机资源
		releaseCamera();
		//获取相机信息
		if (cameraInfo == null) {
			cameraInfo = new Camera.CameraInfo();
		}
		//获取相机个数
		int cameraCount = Camera.getNumberOfCameras();
		//由于不知道第几个是前置摄像头，遍历获取前置摄像头
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);

			if (cameraInfo.facing == cameraIndex) {
				mCamera = Camera.open(camIdx);
				break;
			}
		}
 	 
		//开启预览
		startPreview();
	} catch (Exception e) {
		//获取相机异常
		mCamera = null;
	}
 }
 2.5 开始预览

/**
 * 开始预览
 */
 public void startPreview() {
	try {
		//获取屏幕宽高,预览尺寸默认为屏幕的屏幕
		WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int shortSize = display.getWidth();
		int longSize = display.getHeight();
		//设置相机参数
		initPreviewParams(shortSize, longSize);
		//设置相机方向
		adjustCameraOrientation();
		//预览方式一，没缓冲区，会频繁GC
		//mCamera.setPreviewCallback(previewCallback);
		//绑定预览视图
		mCamera.setPreviewDisplay(mHolder);
		//设置缓冲区
		mCamera.addCallbackBuffer(new byte[shortSize * longSize * 3 / 2]);
		mCamera.setPreviewCallbackWithBuffer(previewCallback);
		//开始预览
		mCamera.startPreview();
	} catch (IOException e) {

	}
 }
 2.6 配置相机参数和预览大小

/**
 * 设置相机参数
 */
 private void initPreviewParams(int shortSize, int longSize) {
	if (mCamera != null) {
		Camera.Parameters parameters = mCamera.getParameters();
		//获取手机支持的尺寸
		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
		//获取合适的预览尺寸，保证不变形
		Camera.Size bestSize = getBestSize(shortSize, longSize, sizes);
		//设置预览大小
		parameters.setPreviewSize(bestSize.width, bestSize.height);
		//设置图片大小，拍照
		parameters.setPictureSize(bestSize.width, bestSize.height);
		//设置格式,所有的相机都支持 NV21格式
		parameters.setPreviewFormat(ImageFormat.NV21);
		//设置聚焦，如果拍照就设置持续对焦FOCUS_MODE_CONTINUOUS_PICTURE，其它可以自动对焦FOCUS_MODE_AUTO
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		mCamera.setParameters(parameters);
	}
 }

/**
 * 获取预览最佳尺寸
 */
 private Camera.Size getBestSize(int shortSize, int longSize, List<Camera.Size> sizes) {
	Camera.Size bestSize = null;
	float uiRatio = (float) longSize / shortSize;
	float minRatio = uiRatio;
	for (Camera.Size previewSize : sizes) {
		float cameraRatio = (float) previewSize.width / previewSize.height;

		//如果找不到比例相同的，找一个最近的,防止预览变形
		float offset = Math.abs(cameraRatio - minRatio);
		if (offset < minRatio) {
			minRatio = offset;
			bestSize = previewSize;
		}
		//比例相同
		if (uiRatio == cameraRatio) {
			bestSize = previewSize;
			break;
		}

	}
	return bestSize;
 }
 2.7 配置预览方向

/**
 * 调整预览方向
 * 由于手机的图片数据都来自摄像头硬件传感器，这个传感器默认的方向横向的，所以要根据前后摄像头调整方向
 */
 private void adjustCameraOrientation() {
	//判断当前的横竖屏
	int rotation = getWindowManager().getDefaultDisplay().getRotation();

	int degress = 0;
	//获取手机的方向
	switch (rotation) {
		case Surface.ROTATION_0:
			degress = 0;
			break;
		case Surface.ROTATION_90:
			degress = 90;
			break;
		case Surface.ROTATION_180:
			degress = 180;
			break;
		case Surface.ROTATION_270:
			degress = 270;
			break;
	}
	int result = 0;
	if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
		//后置摄像头
		result = (cameraInfo.orientation - degress + 360) % 360;
	} else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
		//前置摄像头，多一步镜像
		result = (cameraInfo.orientation + degress) % 360;
		result = (360 - result) % 360;
	}
	mCamera.setDisplayOrientation(result);
 }
  2.8 监听预览数据

 /**
 * 预览数据监听
 */
	Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
	public void onPreviewFrame(byte[] data, Camera camera) {
		if (data != null) {
			//获取预览分辨率
			Camera.Parameters parameters = camera.getParameters();
			Camera.Size size = parameters.getPreviewSize();
			//拿到字节数组，可以生成图片，也可以解析数据(比如二维码扫描，人脸识别)
			//................................

//                //创建解码图像，并转换为原始灰度数据，注意图片是被旋转了90度的
//                Image source = new Image(size.width, size.height, "Y800");
//                //图片旋转了90度，将扫描框的TOP作为left裁剪
//                source.setData(data);//填充数据
//                //解码，返回值为0代表失败，>0表示成功
//                int dataResult = mImageScanner.scanImage(source);
		}

		//不管有没有数据，重新设置缓冲区，避免频繁GC
		camera.addCallbackBuffer(data);
	}
};
2.9  拍照并保存照片

/**
 * 拍照
 */
 public void takePicture() {
	if (mCamera == null) {
		Toast.makeText(this, "请打开相机", Toast.LENGTH_SHORT).show();
		return;
	}
	mCamera.takePicture(new Camera.ShutterCallback() {
		@Override
		public void onShutter() {

		}
	}, null, new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePicAsyncTask(CameraActivity.this, cameraInfo.facing, data).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	});
 }

/**
 * 保存图片
 */
	class SavePicAsyncTask extends AsyncTask<Void, Void, File> {
	Context context;
	int facing;
	byte[] data;

	public SavePicAsyncTask(Context context, int facing, byte[] data) {
		this.context = context;
		this.facing = facing;
		this.data = data;
	}


	@Override
	protected File doInBackground(Void... voids) {
		//保存的文件
		File picFile = null;
		try {
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			if (bitmap == null) {
				return null;
			}
			//保存之前先调整方向
			if (facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				Matrix matrix = new Matrix();
				matrix.postRotate(90);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			} else {
				Matrix matrix = new Matrix();
				matrix.postRotate(270);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			}
	 
			// SD卡根目录
			File dir = context.getExternalFilesDir("print");
			if (!dir.exists()) {
				dir.mkdirs();
			}
			picFile = new File(dir, System.currentTimeMillis() + ".jpg");
			FileOutputStream fos = new FileOutputStream(picFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.close();
			bitmap.recycle();
			return picFile;
	 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return picFile;
	}
	 
	@Override
	protected void onPostExecute(File file) {
		super.onPostExecute(file);
		if (file != null) {
			Toast.makeText(context, "图片保存成功", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(context, "图片保存失败", Toast.LENGTH_SHORT).show();
		}
	}
}
2.10 释放相机资源

/**
 * 释放相机资源
 */
 private void releaseCamera() {
	if (mCamera != null) {
		mCamera.stopPreview();
		mCamera.stopFaceDetection();
		mCamera.setPreviewCallback(null);
		mCamera.release();
		mCamera = null;
	}
 }
 三 重点注意

3.1 对焦模式setFocusMode

FOCUS_MODE_AUTO：自动对焦
FOCUS_MODE_INFINITY：无穷远
FOCUS_MODE_MACRO：微距
FOCUS_MODE_FIXED：固定焦距
FOCUS_MODE_EDOF：景深扩展
FOCUS_MODE_CONTINUOUS_PICTURE：持续对焦(针对照片)
FOCUS_MODE_CONTINUOUS_VIDEO：(针对视频)

3.2  预览格式setPreviewFormat，默认返回NV21的数据

ImageFormat.NV16
ImageFormat.NV21
ImageFormat.YUY2
ImageFormat.YV12
ImgaeFormat.RGB_565
ImageFormat.JPEG

3.3  设置预览尺寸，setPreviewSize。根据屏幕尺寸设备最佳预览尺寸

/**
 * 获取预览最佳尺寸
 */
 private Camera.Size getBestSize(int shortSize, int longSize, List<Camera.Size> sizes) {
    Camera.Size bestSize = null;
    float uiRatio = (float) longSize / shortSize;
    float minRatio = uiRatio;
    for (Camera.Size previewSize : sizes) {
        float cameraRatio = (float) previewSize.width / previewSize.height;

        //如果找不到比例相同的，找一个最近的,防止预览变形
        float offset = Math.abs(cameraRatio - minRatio);
        if (offset < minRatio) {
            minRatio = offset;
            bestSize = previewSize;
        }
        //比例相同
        if (uiRatio == cameraRatio) {
            bestSize = previewSize;
            break;
        }

    }
    return bestSize;
 }

3.4  预览方向，上面说了，传感器方向默认横向的，所以预览成像要调整方向。

下图是传感器方向和需要调整的角度图示



 3.5 拍照图片方向，由于拍照也是存储的传感器方向，所以也需要做旋转

保存之前先调整方向
if (facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
     bitmap = BitmapUtils.rotate(bitmap, 90);
} else {
     bitmap = BitmapUtils.rotate(bitmap, 270);
} 
————————————————

                            版权声明：本文为博主原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接和本声明。

原文链接：https://blog.csdn.net/qq_29848853/article/details/130368476