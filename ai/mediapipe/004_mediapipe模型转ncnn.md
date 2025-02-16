# Mediapipe模型转NCNN模型



## 1 背景介绍

Mediapipe

https://github.com/google-ai-edge/mediapipe



NCNN

https://github.com/Tencent/ncnn



目标: 通过NCNN引擎部署Mediapipe项目中的tflite模型, 通过vulkan api 使用GPU加速推理



## 2 模型文件

https://ai.google.dev/edge/mediapipe/solutions/vision/face_landmarker

face_landmarker 任务需要使用3个网络模型, 分别是

1 FaceDetector
用于在大图片中检测人脸, 输出人脸的位置(矩形框)和6个关键点坐标(左右耳左右眼鼻尖嘴唇)

2 FaceMesh-V2
检测人脸关键点

3 BlendShape
检测表情 (嘴巴张开程度, 眼睛张开程度 .... )


这里首先转换 FaceDetector 模型

### 下载模型文件

在页面

https://ai.google.dev/edge/mediapipe/solutions/vision/face_landmarker
拉到最后:

| Model bundle                                                 | Input shape                                                  | Data type | Model Cards                                                  | Versions                                                     |
| :----------------------------------------------------------- | :----------------------------------------------------------- | :-------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| [FaceLandmarker](https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task) | FaceDetector: 192 x 192 FaceMesh-V2: 256 x 256 Blendshape: 1 x 146 x 2 | float 16  | [FaceDetector](https://storage.googleapis.com/mediapipe-assets/MediaPipe BlazeFace Model Card (Short Range).pdf) [FaceMesh-V2](https://storage.googleapis.com/mediapipe-assets/Model Card MediaPipe Face Mesh V2.pdf) [Blendshape](https://storage.googleapis.com/mediapipe-assets/Model Card Blendshape V2.pdf) | [Latest](https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task) |

这里表格中点击 [FaceLandmarker](https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task) 或者 [Latest](https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task)

下载task文件:
https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/latest/face_landmarker.task
解压即可有下列文件:

```
face_blendshapes.tflite
face_detector.tflite
face_landmarks_detector.tflite
geometry_pipeline_metadata_landmarks.binarypb
```

其中 face_detector.tflite 就是 face_detector 的模型文件, 可以使用 netron 打开



### netron

https://github.com/lutzroeder/netron
下载地址:
https://github.com/lutzroeder/netron/releases
当前最新版本:
https://github.com/lutzroeder/netron/releases/download/v8.1.7/Netron-Setup-8.1.7.exe



## 3 转换模型

### 1 tflite转onnx

将tflite转onnx需要使用 onnx 官方工具 [tensorflow-onnx](https://github.com/onnx/tensorflow-onnx)
https://github.com/onnx/tensorflow-onnx

使用方法, 在python环境下运行:

安装环境

```
pip install tensorflow
pip install onnxruntime
pip install -U tf2onnx
```

执行转化

```
python -m tf2onnx.convert --opset 16 --tflite tflite-file --output model.onnx
```

这里转化 face_detection

```
python -m tf2onnx.convert --opset 16 --tflite ./face_detector.tflite --output ./face_detector.onnx
```

输出日志如下:

```
2025-02-15 17:08:47.434967: I tensorflow/core/util/port.cc:153] oneDNN custom operations are on. You may see slightly different numerical results due to floating-point round-off errors from different computation orders. To turn them off, set the environment variable `TF_ENABLE_ONEDNN_OPTS=0`.
2025-02-15 17:08:50.084043: I tensorflow/core/util/port.cc:153] oneDNN custom operations are on. You may see slightly different numerical results due to floating-point round-off errors from different computation orders. To turn them off, set the environment variable `TF_ENABLE_ONEDNN_OPTS=0`.
WARNING:tensorflow:From D:\develop\python\3.12.7\Lib\site-packages\tf2onnx\tf_loader.py:68: The name tf.reset_default_graph is deprecated. Please use tf.compat.v1.reset_default_graph instead.

WARNING:tensorflow:From D:\develop\python\3.12.7\Lib\site-packages\tf2onnx\tf_loader.py:72: The name tf.train.import_meta_graph is deprecated. Please use tf.compat.v1.train.import_meta_graph instead.

<frozen runpy>:128: RuntimeWarning: 'tf2onnx.convert' found in sys.modules after import of package 'tf2onnx', but prior to execution of 'tf2onnx.convert'; this may result in unpredictable behaviour
2025-02-15 17:08:54,521 - WARNING - ***IMPORTANT*** Installed protobuf is not cpp accelerated. Conversion will be extremely slow. See https://github.com/onnx/tensorflow-onnx/issues/1557
2025-02-15 17:08:54.527547: I tensorflow/core/platform/cpu_feature_guard.cc:210] This TensorFlow binary is optimized to use available CPU instructions in performance-critical operations.
To enable the following instructions: AVX2 FMA, in other operations, rebuild TensorFlow with the appropriate compiler flags.
2025-02-15 17:08:54,536 - INFO - Using tensorflow=2.18.0, onnx=1.17.0, tf2onnx=1.16.1/15c810
2025-02-15 17:08:54,536 - INFO - Using opset <onnx, 16>
INFO: Created TensorFlow Lite XNNPACK delegate for CPU.
2025-02-15 17:08:54,720 - INFO - Optimizing ONNX model
2025-02-15 17:08:55,529 - INFO - After optimization: Cast -78 (78->0), Const -41 (116->75), Identity -2 (2->0), Reshape -16 (20->4), Transpose -149 (154->5)
2025-02-15 17:08:55,561 - INFO -
2025-02-15 17:08:55,561 - INFO - Successfully converted TensorFlow model ./face_detector.tflite to ONNX
2025-02-15 17:08:55,561 - INFO - Model inputs: ['input']
2025-02-15 17:08:55,562 - INFO - Model outputs: ['regressors', 'classificators']
2025-02-15 17:08:55,562 - INFO - ONNX model is saved at ./face_detector.onnx
```

说明转化成功  

目前pytorch支持的最大opset如下定义:
"...\Lib\site-packages\torch\onnx\_constants.py"

```
ONNX_BASE_OPSET = 9
ONNX_MIN_OPSET = 7
ONNX_MAX_OPSET = 20
```

但是实际测试最高支持到 18

使用opset19

```
ValueError: make_sure failure: Opset 19 is not supported yet. Please use a lower opset
```

使用opset18

```
python -m tf2onnx.convert --opset 18 --tflite ./face_detector.tflite --output ./face_detector.onnx
```



### 2 onnx转ncnn

onnx模型转ncnn需要使用ncnn的官方转换工具 pnnx

https://github.com/pnnx/pnnx

下载到本地, 并加入到系统path, 当前最新版本如下

https://github.com/pnnx/pnnx/releases/download/20241223/pnnx-20241223-windows.zip

使用pnnx进行模型转化

```
pnnx ./face_detector.onnx inputshape=[1,128,128,3]
```

转化输出文件:

```
face_detector.ncnn.bin
face_detector.ncnn.param
face_detector.pnnx.bin
face_detector.pnnx.onnx
face_detector.pnnx.param
face_detector.pnnxsim.onnx
face_detector_ncnn.py
face_detector_pnnx.py
```

输出日志如下:

```
pnnxparam = ./face_detector.pnnx.param
pnnxbin = ./face_detector.pnnx.bin
pnnxpy = ./face_detector_pnnx.py
pnnxonnx = ./face_detector.pnnx.onnx
ncnnparam = ./face_detector.ncnn.param
ncnnbin = ./face_detector.ncnn.bin
ncnnpy = ./face_detector_ncnn.py
fp16 = 1
optlevel = 2
device = cpu
inputshape = [1,128,128,3]f32
inputshape2 =
customop =
moduleop =
############# pass_level0 onnx
inline_containers ...                 0.01ms
eliminate_noop ...                    0.08ms
fold_constants ...                    0.07ms
canonicalize ...                      0.10ms
shape_inference ...                  33.16ms
fold_constants_dynamic_shape ...      0.10ms
inline_if_graph ...                   0.02ms
fuse_constant_as_attribute ...        0.12ms
eliminate_noop_with_shape ...         0.06ms
┌──────────────────┬──────────┬──────────┐
│                  │ orig     │ opt      │
├──────────────────┼──────────┼──────────┤
│ node             │ 95       │ 95       │
│ initializer      │ 75       │ 70       │
│ functions        │ 0        │ 0        │
├──────────────────┼──────────┼──────────┤
│ nn module op     │ 0        │ 0        │
│ custom module op │ 0        │ 0        │
│ aten op          │ 0        │ 0        │
│ prims op         │ 0        │ 0        │
│ onnx native op   │ 95       │ 95       │
├──────────────────┼──────────┼──────────┤
│ Add              │ 16       │ 16       │
│ Concat           │ 2        │ 2        │
│ Conv             │ 37       │ 37       │
│ MaxPool          │ 3        │ 3        │
│ Pad              │ 11       │ 11       │
│ Relu             │ 17       │ 17       │
│ Reshape          │ 4        │ 4        │
│ Transpose        │ 5        │ 5        │
└──────────────────┴──────────┴──────────┘
############# pass_level1 onnx
############# pass_level2
############# pass_level3
open failed
############# pass_level4
############# pass_level5
todo Conv
todo Pad
todo Pad
todo Conv
todo Pad
todo Pad
todo Pad
todo Conv
todo Pad
todo Pad
todo Pad
todo Pad
todo Pad
todo Pad
todo Conv
############# pass_ncnn
force batch axis 233 for operand 3
force batch axis 233 for operand 18
force batch axis 233 for operand 36
force batch axis 233 for operand 72
ignore Conv Conv_1 param dilations=(1,1)
ignore Conv Conv_1 param group=1
ignore Conv Conv_1 param kernel_shape=(5,5)
ignore Conv Conv_1 param pads=(1,1,2,2)
ignore Conv Conv_1 param strides=(2,2)
ignore Pad Pad_9 param pads=(0,0,0,0,0,4,0,0)
ignore Pad Pad_13 param pads=(0,0,0,0,0,4,0,0)
ignore Conv Conv_14 param dilations=(1,1)
ignore Conv Conv_14 param group=28
ignore Conv Conv_14 param kernel_shape=(3,3)
ignore Conv Conv_14 param pads=(0,0,1,1)
ignore Conv Conv_14 param strides=(2,2)
ignore Pad Pad_20 param pads=(0,0,0,0,0,4,0,0)
ignore Pad Pad_25 param pads=(0,0,0,0,0,6,0,0)
ignore Pad Pad_29 param pads=(0,0,0,0,0,6,0,0)
ignore Conv Conv_30 param dilations=(1,1)
ignore Conv Conv_30 param group=42
ignore Conv Conv_30 param kernel_shape=(3,3)
ignore Conv Conv_30 param pads=(0,0,1,1)
ignore Conv Conv_30 param strides=(2,2)
ignore Pad Pad_36 param pads=(0,0,0,0,0,8,0,0)
ignore Pad Pad_41 param pads=(0,0,0,0,0,8,0,0)
ignore Pad Pad_46 param pads=(0,0,0,0,0,8,0,0)
ignore Pad Pad_51 param pads=(0,0,0,0,0,8,0,0)
ignore Pad Pad_56 param pads=(0,0,0,0,0,8,0,0)
ignore Pad Pad_63 param pads=(0,0,0,0,0,8,0,0)
ignore Conv Conv_64 param dilations=(1,1)
ignore Conv Conv_64 param group=88
ignore Conv Conv_64 param kernel_shape=(3,3)
ignore Conv Conv_64 param pads=(0,0,1,1)
ignore Conv Conv_64 param strides=(2,2)
```

注意这里提示很多个 Conv 和 Pad 算子没有转化成功, 这个是由于 pnnx 不支持转化非对称的padding参数的conv算子和padding算子

进一步的原因可能是pytorch的conv算子和padding算子的 padding参数不支持非对称参数导致, 解决的办法是在conv算子前手动加一个padding算子, padding算子转化为支持非对称的padding算子, 实现方式可以通过修改pnnx生成的 `face_detector_pnnx.py` 文件来实现.

pnnx生成的 `face_detector_pnnx.py` 文件如下:

```
import os
import numpy as np
import tempfile, zipfile
import torch
import torch.nn as nn
import torch.nn.functional as F
try:
    import torchvision
    import torchaudio
except:
    pass

class Model(nn.Module):
    def __init__(self):
        super(Model, self).__init__()

        self.conv2d_0 = nn.Conv2d(bias=True, dilation=(1,1), groups=24, in_channels=24, kernel_size=(3,3), out_channels=24, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_1 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=24, kernel_size=(1,1), out_channels=24, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_2 = nn.Conv2d(bias=True, dilation=(1,1), groups=24, in_channels=24, kernel_size=(3,3), out_channels=24, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_3 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=24, kernel_size=(1,1), out_channels=28, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_4 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=28, kernel_size=(1,1), out_channels=32, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_5 = nn.Conv2d(bias=True, dilation=(1,1), groups=32, in_channels=32, kernel_size=(3,3), out_channels=32, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_6 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=32, kernel_size=(1,1), out_channels=36, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_7 = nn.Conv2d(bias=True, dilation=(1,1), groups=36, in_channels=36, kernel_size=(3,3), out_channels=36, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_8 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=36, kernel_size=(1,1), out_channels=42, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_9 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=42, kernel_size=(1,1), out_channels=48, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_10 = nn.Conv2d(bias=True, dilation=(1,1), groups=48, in_channels=48, kernel_size=(3,3), out_channels=48, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_11 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=48, kernel_size=(1,1), out_channels=56, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_12 = nn.Conv2d(bias=True, dilation=(1,1), groups=56, in_channels=56, kernel_size=(3,3), out_channels=56, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_13 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=56, kernel_size=(1,1), out_channels=64, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_14 = nn.Conv2d(bias=True, dilation=(1,1), groups=64, in_channels=64, kernel_size=(3,3), out_channels=64, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_15 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=64, kernel_size=(1,1), out_channels=72, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_16 = nn.Conv2d(bias=True, dilation=(1,1), groups=72, in_channels=72, kernel_size=(3,3), out_channels=72, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_17 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=72, kernel_size=(1,1), out_channels=80, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_18 = nn.Conv2d(bias=True, dilation=(1,1), groups=80, in_channels=80, kernel_size=(3,3), out_channels=80, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_19 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=80, kernel_size=(1,1), out_channels=88, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_20 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=88, kernel_size=(1,1), out_channels=32, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_21 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=88, kernel_size=(1,1), out_channels=96, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_22 = nn.Conv2d(bias=True, dilation=(1,1), groups=96, in_channels=96, kernel_size=(3,3), out_channels=96, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_23 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=96, kernel_size=(1,1), out_channels=96, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_24 = nn.Conv2d(bias=True, dilation=(1,1), groups=96, in_channels=96, kernel_size=(3,3), out_channels=96, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_25 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=96, kernel_size=(1,1), out_channels=96, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_26 = nn.Conv2d(bias=True, dilation=(1,1), groups=96, in_channels=96, kernel_size=(3,3), out_channels=96, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_27 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=96, kernel_size=(1,1), out_channels=96, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_28 = nn.Conv2d(bias=True, dilation=(1,1), groups=96, in_channels=96, kernel_size=(3,3), out_channels=96, padding=(1,1), padding_mode='zeros', stride=(1,1))
        self.conv2d_29 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=96, kernel_size=(1,1), out_channels=96, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_30 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=96, kernel_size=(1,1), out_channels=96, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_31 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=96, kernel_size=(1,1), out_channels=6, padding=(0,0), padding_mode='zeros', stride=(1,1))
        self.conv2d_32 = nn.Conv2d(bias=True, dilation=(1,1), groups=1, in_channels=88, kernel_size=(1,1), out_channels=2, padding=(0,0), padding_mode='zeros', stride=(1,1))

        archive = zipfile.ZipFile('./face_detector.pnnx.bin', 'r')
        self.conv2d_0.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_0.bias', (24), 'float32')
        self.conv2d_0.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_0.weight', (24,1,3,3), 'float32')
        self.conv2d_1.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_1.bias', (24), 'float32')
        self.conv2d_1.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_1.weight', (24,24,1,1), 'float32')
        self.conv2d_2.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_2.bias', (24), 'float32')
        self.conv2d_2.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_2.weight', (24,1,3,3), 'float32')
        self.conv2d_3.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_3.bias', (28), 'float32')
        self.conv2d_3.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_3.weight', (28,24,1,1), 'float32')
        self.conv2d_4.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_4.bias', (32), 'float32')
        self.conv2d_4.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_4.weight', (32,28,1,1), 'float32')
        self.conv2d_5.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_5.bias', (32), 'float32')
        self.conv2d_5.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_5.weight', (32,1,3,3), 'float32')
        self.conv2d_6.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_6.bias', (36), 'float32')
        self.conv2d_6.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_6.weight', (36,32,1,1), 'float32')
        self.conv2d_7.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_7.bias', (36), 'float32')
        self.conv2d_7.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_7.weight', (36,1,3,3), 'float32')
        self.conv2d_8.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_8.bias', (42), 'float32')
        self.conv2d_8.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_8.weight', (42,36,1,1), 'float32')
        self.conv2d_9.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_9.bias', (48), 'float32')
        self.conv2d_9.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_9.weight', (48,42,1,1), 'float32')
        self.conv2d_10.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_10.bias', (48), 'float32')
        self.conv2d_10.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_10.weight', (48,1,3,3), 'float32')
        self.conv2d_11.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_11.bias', (56), 'float32')
        self.conv2d_11.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_11.weight', (56,48,1,1), 'float32')
        self.conv2d_12.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_12.bias', (56), 'float32')
        self.conv2d_12.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_12.weight', (56,1,3,3), 'float32')
        self.conv2d_13.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_13.bias', (64), 'float32')
        self.conv2d_13.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_13.weight', (64,56,1,1), 'float32')
        self.conv2d_14.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_14.bias', (64), 'float32')
        self.conv2d_14.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_14.weight', (64,1,3,3), 'float32')
        self.conv2d_15.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_15.bias', (72), 'float32')
        self.conv2d_15.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_15.weight', (72,64,1,1), 'float32')
        self.conv2d_16.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_16.bias', (72), 'float32')
        self.conv2d_16.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_16.weight', (72,1,3,3), 'float32')
        self.conv2d_17.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_17.bias', (80), 'float32')
        self.conv2d_17.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_17.weight', (80,72,1,1), 'float32')
        self.conv2d_18.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_18.bias', (80), 'float32')
        self.conv2d_18.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_18.weight', (80,1,3,3), 'float32')
        self.conv2d_19.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_19.bias', (88), 'float32')
        self.conv2d_19.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_19.weight', (88,80,1,1), 'float32')
        self.conv2d_20.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_20.bias', (32), 'float32')
        self.conv2d_20.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_20.weight', (32,88,1,1), 'float32')
        self.conv2d_21.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_21.bias', (96), 'float32')
        self.conv2d_21.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_21.weight', (96,88,1,1), 'float32')
        self.conv2d_22.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_22.bias', (96), 'float32')
        self.conv2d_22.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_22.weight', (96,1,3,3), 'float32')
        self.conv2d_23.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_23.bias', (96), 'float32')
        self.conv2d_23.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_23.weight', (96,96,1,1), 'float32')
        self.conv2d_24.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_24.bias', (96), 'float32')
        self.conv2d_24.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_24.weight', (96,1,3,3), 'float32')
        self.conv2d_25.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_25.bias', (96), 'float32')
        self.conv2d_25.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_25.weight', (96,96,1,1), 'float32')
        self.conv2d_26.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_26.bias', (96), 'float32')
        self.conv2d_26.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_26.weight', (96,1,3,3), 'float32')
        self.conv2d_27.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_27.bias', (96), 'float32')
        self.conv2d_27.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_27.weight', (96,96,1,1), 'float32')
        self.conv2d_28.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_28.bias', (96), 'float32')
        self.conv2d_28.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_28.weight', (96,1,3,3), 'float32')
        self.conv2d_29.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_29.bias', (96), 'float32')
        self.conv2d_29.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_29.weight', (96,96,1,1), 'float32')
        self.conv2d_30.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_30.bias', (96), 'float32')
        self.conv2d_30.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_30.weight', (96,96,1,1), 'float32')
        self.conv2d_31.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_31.bias', (6), 'float32')
        self.conv2d_31.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_31.weight', (6,96,1,1), 'float32')
        self.conv2d_32.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_32.bias', (2), 'float32')
        self.conv2d_32.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_32.weight', (2,88,1,1), 'float32')
        self.const_fold_opt__417_data = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__417.data', (24,3,5,5,), 'float32')
        self.const_fold_opt__382_data = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__382.data', (24,), 'float32')
        self.const_fold_opt__485_data = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__485.data', (28,1,3,3,), 'float32')
        self.const_fold_opt__322_data = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__322.data', (28,), 'float32')
        self.const_fold_opt__480_data = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__480.data', (42,1,3,3,), 'float32')
        self.const_fold_opt__346_data = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__346.data', (42,), 'float32')
        self.const_fold_opt__477_data = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__477.data', (88,1,3,3,), 'float32')
        self.const_fold_opt__329_data = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__329.data', (88,), 'float32')
        archive.close()

    def load_pnnx_bin_as_parameter(self, archive, key, shape, dtype, requires_grad=True):
        return nn.Parameter(self.load_pnnx_bin_as_tensor(archive, key, shape, dtype), requires_grad)

    def load_pnnx_bin_as_tensor(self, archive, key, shape, dtype):
        fd, tmppath = tempfile.mkstemp()
        with os.fdopen(fd, 'wb') as tmpf, archive.open(key) as keyfile:
            tmpf.write(keyfile.read())
        m = np.memmap(tmppath, dtype=dtype, mode='r', shape=shape).copy()
        os.remove(tmppath)
        return torch.from_numpy(m)

    def forward(self, v_0):
        v_1 = v_0.permute(dims=(0,3,1,2))
        v_2 = self.const_fold_opt__417_data
        v_3 = self.const_fold_opt__382_data
        v_4 = Conv(v_1, v_2, v_3, dilations=(1,1), group=1, kernel_shape=(5,5), pads=(1,1,2,2), strides=(2,2))
        v_5 = F.relu(input=v_4)
        v_6 = self.conv2d_0(v_5)
        v_7 = self.conv2d_1(v_6)
        v_8 = (v_5 + v_7)
        v_9 = F.relu(input=v_8)
        v_10 = self.conv2d_2(v_9)
        v_11 = self.conv2d_3(v_10)
        v_12 = Pad(v_9, pads=(0,0,0,0,0,4,0,0))
        v_13 = (v_12 + v_11)
        v_14 = F.relu(input=v_13)
        v_15 = F.max_pool2d(input=v_14, ceil_mode=False, kernel_size=(2,2), padding=(0,0), return_indices=False, stride=(2,2))
        v_16 = Pad(v_15, pads=(0,0,0,0,0,4,0,0))
        v_17 = self.const_fold_opt__485_data
        v_18 = self.const_fold_opt__322_data
        v_19 = Conv(v_14, v_17, v_18, dilations=(1,1), group=28, kernel_shape=(3,3), pads=(0,0,1,1), strides=(2,2))
        v_20 = self.conv2d_4(v_19)
        v_21 = (v_16 + v_20)
        v_22 = F.relu(input=v_21)
        v_23 = self.conv2d_5(v_22)
        v_24 = self.conv2d_6(v_23)
        v_25 = Pad(v_22, pads=(0,0,0,0,0,4,0,0))
        v_26 = (v_25 + v_24)
        v_27 = F.relu(input=v_26)
        v_28 = self.conv2d_7(v_27)
        v_29 = self.conv2d_8(v_28)
        v_30 = Pad(v_27, pads=(0,0,0,0,0,6,0,0))
        v_31 = (v_30 + v_29)
        v_32 = F.relu(input=v_31)
        v_33 = F.max_pool2d(input=v_32, ceil_mode=False, kernel_size=(2,2), padding=(0,0), return_indices=False, stride=(2,2))
        v_34 = Pad(v_33, pads=(0,0,0,0,0,6,0,0))
        v_35 = self.const_fold_opt__480_data
        v_36 = self.const_fold_opt__346_data
        v_37 = Conv(v_32, v_35, v_36, dilations=(1,1), group=42, kernel_shape=(3,3), pads=(0,0,1,1), strides=(2,2))
        v_38 = self.conv2d_9(v_37)
        v_39 = (v_34 + v_38)
        v_40 = F.relu(input=v_39)
        v_41 = self.conv2d_10(v_40)
        v_42 = self.conv2d_11(v_41)
        v_43 = Pad(v_40, pads=(0,0,0,0,0,8,0,0))
        v_44 = (v_43 + v_42)
        v_45 = F.relu(input=v_44)
        v_46 = self.conv2d_12(v_45)
        v_47 = self.conv2d_13(v_46)
        v_48 = Pad(v_45, pads=(0,0,0,0,0,8,0,0))
        v_49 = (v_48 + v_47)
        v_50 = F.relu(input=v_49)
        v_51 = self.conv2d_14(v_50)
        v_52 = self.conv2d_15(v_51)
        v_53 = Pad(v_50, pads=(0,0,0,0,0,8,0,0))
        v_54 = (v_53 + v_52)
        v_55 = F.relu(input=v_54)
        v_56 = self.conv2d_16(v_55)
        v_57 = self.conv2d_17(v_56)
        v_58 = Pad(v_55, pads=(0,0,0,0,0,8,0,0))
        v_59 = (v_58 + v_57)
        v_60 = F.relu(input=v_59)
        v_61 = self.conv2d_18(v_60)
        v_62 = self.conv2d_19(v_61)
        v_63 = Pad(v_60, pads=(0,0,0,0,0,8,0,0))
        v_64 = (v_63 + v_62)
        v_65 = F.relu(input=v_64)
        v_66 = self.conv2d_20(v_65)
        v_67 = v_66.permute(dims=(0,2,3,1))
        v_68 = v_67.reshape(1, 512, 16)
        v_69 = F.max_pool2d(input=v_65, ceil_mode=False, kernel_size=(2,2), padding=(0,0), return_indices=False, stride=(2,2))
        v_70 = Pad(v_69, pads=(0,0,0,0,0,8,0,0))
        v_71 = self.const_fold_opt__477_data
        v_72 = self.const_fold_opt__329_data
        v_73 = Conv(v_65, v_71, v_72, dilations=(1,1), group=88, kernel_shape=(3,3), pads=(0,0,1,1), strides=(2,2))
        v_74 = self.conv2d_21(v_73)
        v_75 = (v_70 + v_74)
        v_76 = F.relu(input=v_75)
        v_77 = self.conv2d_22(v_76)
        v_78 = self.conv2d_23(v_77)
        v_79 = (v_76 + v_78)
        v_80 = F.relu(input=v_79)
        v_81 = self.conv2d_24(v_80)
        v_82 = self.conv2d_25(v_81)
        v_83 = (v_80 + v_82)
        v_84 = F.relu(input=v_83)
        v_85 = self.conv2d_26(v_84)
        v_86 = self.conv2d_27(v_85)
        v_87 = (v_84 + v_86)
        v_88 = F.relu(input=v_87)
        v_89 = self.conv2d_28(v_88)
        v_90 = self.conv2d_29(v_89)
        v_91 = (v_88 + v_90)
        v_92 = F.relu(input=v_91)
        v_93 = self.conv2d_30(v_92)
        v_94 = v_93.permute(dims=(0,2,3,1))
        v_95 = v_94.reshape(1, 384, 16)
        v_96 = torch.cat((v_68, v_95), dim=1)
        v_97 = self.conv2d_31(v_92)
        v_98 = v_97.permute(dims=(0,2,3,1))
        v_99 = v_98.reshape(1, 384, 1)
        v_100 = self.conv2d_32(v_65)
        v_101 = v_100.permute(dims=(0,2,3,1))
        v_102 = v_101.reshape(1, 512, 1)
        v_103 = torch.cat((v_102, v_99), dim=1)
        return v_96, v_103

def export_torchscript():
    net = Model()
    net.float()
    net.eval()

    torch.manual_seed(0)
    v_0 = torch.rand(1, 128, 128, 3, dtype=torch.float)

    mod = torch.jit.trace(net, v_0)
    mod.save("./face_detector_pnnx.py.pt")

def export_onnx():
    net = Model()
    net.float()
    net.eval()

    torch.manual_seed(0)
    v_0 = torch.rand(1, 128, 128, 3, dtype=torch.float)

    torch.onnx.export(net, v_0, "./face_detector_pnnx.py.onnx", export_params=True, operator_export_type=torch.onnx.OperatorExportTypes.ONNX_ATEN_FALLBACK, opset_version=13, input_names=['in0'], output_names=['out0', 'out1'])

def test_inference():
    net = Model()
    net.float()
    net.eval()

    torch.manual_seed(0)
    v_0 = torch.rand(1, 128, 128, 3, dtype=torch.float)

    return net(v_0)

if __name__ == "__main__":
    print(test_inference())

```

修改之后的代码如下:

```
import os
import tempfile
import zipfile

import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F

import onnx

import math
from dataclasses import dataclass

import cv2
import ncnn
import numpy as np

try:
    import torchvision
    import torchaudio
except:
    pass


class Model(nn.Module):
    def __init__(self):
        super(Model, self).__init__()

        self.conv2d_0 = nn.Conv2d(bias=True, dilation=(1, 1), groups=24, in_channels=24, kernel_size=(3, 3), out_channels=24, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_1 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=24, kernel_size=(1, 1), out_channels=24, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_2 = nn.Conv2d(bias=True, dilation=(1, 1), groups=24, in_channels=24, kernel_size=(3, 3), out_channels=24, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_3 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=24, kernel_size=(1, 1), out_channels=28, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_4 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=28, kernel_size=(1, 1), out_channels=32, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_5 = nn.Conv2d(bias=True, dilation=(1, 1), groups=32, in_channels=32, kernel_size=(3, 3), out_channels=32, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_6 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=32, kernel_size=(1, 1), out_channels=36, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_7 = nn.Conv2d(bias=True, dilation=(1, 1), groups=36, in_channels=36, kernel_size=(3, 3), out_channels=36, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_8 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=36, kernel_size=(1, 1), out_channels=42, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_9 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=42, kernel_size=(1, 1), out_channels=48, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_10 = nn.Conv2d(bias=True, dilation=(1, 1), groups=48, in_channels=48, kernel_size=(3, 3), out_channels=48, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_11 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=48, kernel_size=(1, 1), out_channels=56, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_12 = nn.Conv2d(bias=True, dilation=(1, 1), groups=56, in_channels=56, kernel_size=(3, 3), out_channels=56, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_13 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=56, kernel_size=(1, 1), out_channels=64, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_14 = nn.Conv2d(bias=True, dilation=(1, 1), groups=64, in_channels=64, kernel_size=(3, 3), out_channels=64, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_15 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=64, kernel_size=(1, 1), out_channels=72, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_16 = nn.Conv2d(bias=True, dilation=(1, 1), groups=72, in_channels=72, kernel_size=(3, 3), out_channels=72, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_17 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=72, kernel_size=(1, 1), out_channels=80, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_18 = nn.Conv2d(bias=True, dilation=(1, 1), groups=80, in_channels=80, kernel_size=(3, 3), out_channels=80, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_19 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=80, kernel_size=(1, 1), out_channels=88, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_20 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=88, kernel_size=(1, 1), out_channels=32, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_21 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=88, kernel_size=(1, 1), out_channels=96, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_22 = nn.Conv2d(bias=True, dilation=(1, 1), groups=96, in_channels=96, kernel_size=(3, 3), out_channels=96, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_23 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=96, kernel_size=(1, 1), out_channels=96, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_24 = nn.Conv2d(bias=True, dilation=(1, 1), groups=96, in_channels=96, kernel_size=(3, 3), out_channels=96, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_25 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=96, kernel_size=(1, 1), out_channels=96, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_26 = nn.Conv2d(bias=True, dilation=(1, 1), groups=96, in_channels=96, kernel_size=(3, 3), out_channels=96, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_27 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=96, kernel_size=(1, 1), out_channels=96, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_28 = nn.Conv2d(bias=True, dilation=(1, 1), groups=96, in_channels=96, kernel_size=(3, 3), out_channels=96, padding=(1, 1), padding_mode='zeros', stride=(1, 1))
        self.conv2d_29 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=96, kernel_size=(1, 1), out_channels=96, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_30 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=96, kernel_size=(1, 1), out_channels=96, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_31 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=96, kernel_size=(1, 1), out_channels=6, padding=(0, 0), padding_mode='zeros', stride=(1, 1))
        self.conv2d_32 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=88, kernel_size=(1, 1), out_channels=2, padding=(0, 0), padding_mode='zeros', stride=(1, 1))

        # v_2 = self.const_fold_opt__437_data
        # v_3 = self.const_fold_opt__441_data
        # v_4 = Conv(v_1, v_2, v_3, dilations=(1, 1), group=1, kernel_shape=(5, 5), pads=(1, 1, 2, 2), strides=(2, 2))
        self.pad_conv_33 = nn.ZeroPad2d(padding=(1, 2, 1, 2))
        self.conv2d_33 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=3, kernel_size=(5, 5), out_channels=24, padding=(0, 0), padding_mode='zeros', stride=(2, 2))

        # v_17 = self.const_fold_opt__415_data
        # v_18 = self.const_fold_opt__329_data
        # v_19 = Conv(v_14, v_17, v_18, dilations=(1, 1), group=28, kernel_shape=(3, 3), pads=(0, 0, 1, 1), strides=(2, 2))
        self.pad_conv_34 = nn.ZeroPad2d(padding=(0, 1, 0, 1))
        self.conv2d_34 = nn.Conv2d(bias=True, dilation=(1, 1), groups=28, in_channels=28, kernel_size=(3, 3), out_channels=28, padding=(0, 0), padding_mode='zeros', stride=(2, 2))

        # v_35 = self.const_fold_opt__480_data
        # v_36 = self.const_fold_opt__448_data
        # v_37 = Conv(v_32, v_35, v_36, dilations=(1, 1), group=42, kernel_shape=(3, 3), pads=(0, 0, 1, 1), strides=(2, 2))
        self.pad_conv_35 = nn.ZeroPad2d(padding=(0, 1, 0, 1))
        self.conv2d_35 = nn.Conv2d(bias=True, dilation=(1, 1), groups=42, in_channels=42, kernel_size=(3, 3), out_channels=42, padding=(0, 0), padding_mode='zeros', stride=(2, 2))

        # v_71 = self.const_fold_opt__470_data
        # v_72 = self.const_fold_opt__376_data
        # v_73 = Conv(v_65, v_71, v_72, dilations=(1, 1), group=88, kernel_shape=(3, 3), pads=(0, 0, 1, 1), strides=(2, 2))
        self.pad_conv_36 = nn.ZeroPad2d(padding=(0, 1, 0, 1))
        self.conv2d_36 = nn.Conv2d(bias=True, dilation=(1, 1), groups=88, in_channels=88, kernel_size=(3, 3), out_channels=88, padding=(0, 0), padding_mode='zeros', stride=(2, 2))

        # v_12 = Pad(v_9, pads=(0,0,0,0,0,4,0,0))
        self.pad_0 = nn.ZeroPad2d(padding=(0, 4, 0, 0))

        # v_16 = Pad(v_15, pads=(0, 0, 0, 0, 0, 4, 0, 0))
        self.pad_1 = nn.ZeroPad2d(padding=(0, 0, 0, 4))

        # v_25 = Pad(v_22, pads=(0, 0, 0, 0, 0, 4, 0, 0))
        self.pad_2 = nn.ZeroPad2d(padding=(0, 0, 0, 4))

        # v_30 = Pad(v_27, pads=(0, 0, 0, 0, 0, 6, 0, 0))
        self.pad_3 = nn.ZeroPad2d(padding=(0, 0, 0, 6))

        # v_34 = Pad(v_33, pads=(0, 0, 0, 0, 0, 6, 0, 0))
        self.pad_4 = nn.ZeroPad2d(padding=(0, 0, 0, 6))

        # v_43 = Pad(v_40, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        self.pad_5 = nn.ZeroPad2d(padding=(0, 0, 0, 8))

        # v_48 = Pad(v_45, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        self.pad_6 = nn.ZeroPad2d(padding=(0, 0, 0, 8))

        # v_53 = Pad(v_50, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        self.pad_7 = nn.ZeroPad2d(padding=(0, 0, 0, 8))

        # v_58 = Pad(v_55, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        self.pad_8 = nn.ZeroPad2d(padding=(0, 0, 0, 8))

        # v_63 = Pad(v_60, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        self.pad_9 = nn.ZeroPad2d(padding=(0, 0, 0, 8))

        # v_70 = Pad(v_69, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        self.pad_10 = nn.ZeroPad2d(padding=(0, 0, 0, 8))

        archive = zipfile.ZipFile('./face_detector.pnnx.bin', 'r')
        self.conv2d_0.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_0.bias', (24), 'float32')
        self.conv2d_0.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_0.weight', (24, 1, 3, 3), 'float32')
        self.conv2d_1.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_1.bias', (24), 'float32')
        self.conv2d_1.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_1.weight', (24, 24, 1, 1), 'float32')
        self.conv2d_2.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_2.bias', (24), 'float32')
        self.conv2d_2.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_2.weight', (24, 1, 3, 3), 'float32')
        self.conv2d_3.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_3.bias', (28), 'float32')
        self.conv2d_3.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_3.weight', (28, 24, 1, 1), 'float32')
        self.conv2d_4.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_4.bias', (32), 'float32')
        self.conv2d_4.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_4.weight', (32, 28, 1, 1), 'float32')
        self.conv2d_5.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_5.bias', (32), 'float32')
        self.conv2d_5.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_5.weight', (32, 1, 3, 3), 'float32')
        self.conv2d_6.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_6.bias', (36), 'float32')
        self.conv2d_6.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_6.weight', (36, 32, 1, 1), 'float32')
        self.conv2d_7.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_7.bias', (36), 'float32')
        self.conv2d_7.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_7.weight', (36, 1, 3, 3), 'float32')
        self.conv2d_8.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_8.bias', (42), 'float32')
        self.conv2d_8.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_8.weight', (42, 36, 1, 1), 'float32')
        self.conv2d_9.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_9.bias', (48), 'float32')
        self.conv2d_9.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_9.weight', (48, 42, 1, 1), 'float32')
        self.conv2d_10.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_10.bias', (48), 'float32')
        self.conv2d_10.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_10.weight', (48, 1, 3, 3), 'float32')
        self.conv2d_11.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_11.bias', (56), 'float32')
        self.conv2d_11.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_11.weight', (56, 48, 1, 1), 'float32')
        self.conv2d_12.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_12.bias', (56), 'float32')
        self.conv2d_12.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_12.weight', (56, 1, 3, 3), 'float32')
        self.conv2d_13.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_13.bias', (64), 'float32')
        self.conv2d_13.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_13.weight', (64, 56, 1, 1), 'float32')
        self.conv2d_14.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_14.bias', (64), 'float32')
        self.conv2d_14.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_14.weight', (64, 1, 3, 3), 'float32')
        self.conv2d_15.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_15.bias', (72), 'float32')
        self.conv2d_15.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_15.weight', (72, 64, 1, 1), 'float32')
        self.conv2d_16.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_16.bias', (72), 'float32')
        self.conv2d_16.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_16.weight', (72, 1, 3, 3), 'float32')
        self.conv2d_17.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_17.bias', (80), 'float32')
        self.conv2d_17.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_17.weight', (80, 72, 1, 1), 'float32')
        self.conv2d_18.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_18.bias', (80), 'float32')
        self.conv2d_18.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_18.weight', (80, 1, 3, 3), 'float32')
        self.conv2d_19.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_19.bias', (88), 'float32')
        self.conv2d_19.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_19.weight', (88, 80, 1, 1), 'float32')
        self.conv2d_20.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_20.bias', (32), 'float32')
        self.conv2d_20.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_20.weight', (32, 88, 1, 1), 'float32')
        self.conv2d_21.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_21.bias', (96), 'float32')
        self.conv2d_21.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_21.weight', (96, 88, 1, 1), 'float32')
        self.conv2d_22.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_22.bias', (96), 'float32')
        self.conv2d_22.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_22.weight', (96, 1, 3, 3), 'float32')
        self.conv2d_23.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_23.bias', (96), 'float32')
        self.conv2d_23.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_23.weight', (96, 96, 1, 1), 'float32')
        self.conv2d_24.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_24.bias', (96), 'float32')
        self.conv2d_24.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_24.weight', (96, 1, 3, 3), 'float32')
        self.conv2d_25.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_25.bias', (96), 'float32')
        self.conv2d_25.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_25.weight', (96, 96, 1, 1), 'float32')
        self.conv2d_26.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_26.bias', (96), 'float32')
        self.conv2d_26.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_26.weight', (96, 1, 3, 3), 'float32')
        self.conv2d_27.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_27.bias', (96), 'float32')
        self.conv2d_27.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_27.weight', (96, 96, 1, 1), 'float32')
        self.conv2d_28.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_28.bias', (96), 'float32')
        self.conv2d_28.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_28.weight', (96, 1, 3, 3), 'float32')
        self.conv2d_29.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_29.bias', (96), 'float32')
        self.conv2d_29.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_29.weight', (96, 96, 1, 1), 'float32')
        self.conv2d_30.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_30.bias', (96), 'float32')
        self.conv2d_30.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_30.weight', (96, 96, 1, 1), 'float32')
        self.conv2d_31.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_31.bias', (6), 'float32')
        self.conv2d_31.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_31.weight', (6, 96, 1, 1), 'float32')
        self.conv2d_32.bias = self.load_pnnx_bin_as_parameter(archive, 'conv2d_32.bias', (2), 'float32')
        self.conv2d_32.weight = self.load_pnnx_bin_as_parameter(archive, 'conv2d_32.weight', (2, 88, 1, 1), 'float32')

        self.conv2d_33.bias = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__441.data', (24,), 'float32')
        self.conv2d_33.weight = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__437.data', (24, 3, 5, 5,), 'float32')

        self.conv2d_34.bias = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__329.data', (28,), 'float32')
        self.conv2d_34.weight = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__415.data', (28, 1, 3, 3,), 'float32')

        self.conv2d_35.bias = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__448.data', (42,), 'float32')
        self.conv2d_35.weight = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__480.data', (42, 1, 3, 3,), 'float32')

        self.conv2d_36.bias = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__376.data', (88,), 'float32')
        self.conv2d_36.weight = self.load_pnnx_bin_as_parameter(archive, 'const_fold_opt__470.data', (88, 1, 3, 3,), 'float32')

        archive.close()

    def load_pnnx_bin_as_parameter(self, archive, key, shape, dtype, requires_grad=True):
        return nn.Parameter(self.load_pnnx_bin_as_tensor(archive, key, shape, dtype), requires_grad)

    def load_pnnx_bin_as_tensor(self, archive, key, shape, dtype):
        fd, tmppath = tempfile.mkstemp()
        with os.fdopen(fd, 'wb') as tmpf, archive.open(key) as keyfile:
            tmpf.write(keyfile.read())
        m = np.memmap(tmppath, dtype=dtype, mode='r', shape=shape).copy()
        os.remove(tmppath)
        return torch.from_numpy(m)

    def forward(self, v_0):
        print("v_0:", v_0.shape)
        v_1 = v_0.permute(dims=(0, 3, 1, 2))
        print("v_1:", v_1.shape)

        # v_2 = self.const_fold_opt__437_data
        # v_3 = self.const_fold_opt__441_data
        # v_4 = Conv(v_1, v_2, v_3, dilations=(1, 1), group=1, kernel_shape=(5, 5), pads=(1, 1, 2, 2), strides=(2, 2))
        v_pad_conv_33 = self.pad_conv_33(v_1)
        print("v_pad_conv_33:", v_pad_conv_33.shape)
        v_4 = self.conv2d_33(v_pad_conv_33)
        print("v_4:", v_4.shape)

        v_5 = F.relu(input=v_4)
        print("v_5:", v_5.shape)
        v_6 = self.conv2d_0(v_5)
        print("v_6:", v_6.shape)
        v_7 = self.conv2d_1(v_6)
        print("v_7:", v_7.shape)
        v_8 = (v_5 + v_7)
        print("v_8:", v_8.shape)
        v_9 = F.relu(input=v_8)
        print("v_9:", v_9.shape)
        v_10 = self.conv2d_2(v_9)
        print("v_10:", v_10.shape)
        v_11 = self.conv2d_3(v_10)
        print("v_11:", v_11.shape)

        # v_12 = Pad(v_9, pads=(0,0,0,0,0,4,0,0))
        v_12 = F.pad(v_9, pad=(0, 0, 0, 0, 0, 4, 0, 0), mode='constant', value=0)
        # v_12 = self.pad_0(v_9)
        print("v_12:", v_12.shape)

        v_13 = (v_12 + v_11)
        print("v_13:", v_13.shape)
        v_14 = F.relu(input=v_13)
        print("v_14:", v_14.shape)
        v_15 = F.max_pool2d(input=v_14, ceil_mode=False, kernel_size=(2, 2), padding=(0, 0), return_indices=False, stride=(2, 2))
        print("v_15:", v_15.shape)

        # v_16 = Pad(v_15, pads=(0, 0, 0, 0, 0, 4, 0, 0))
        # v_16 = self.pad_1(v_15)
        v_16 = F.pad(v_15, pad=(0, 0, 0, 0, 0, 4, 0, 0), mode='constant', value=0)
        print("v_16:", v_16.shape)

        # v_17 = self.const_fold_opt__415_data
        # v_18 = self.const_fold_opt__329_data
        # v_19 = Conv(v_14, v_17, v_18, dilations=(1, 1), group=28, kernel_shape=(3, 3), pads=(0, 0, 1, 1), strides=(2, 2))
        v_pad_conv_34 = self.pad_conv_34(v_14)
        print("v_pad_conv_34:", v_pad_conv_34.shape)
        v_19 = self.conv2d_34(v_pad_conv_34)
        print("v_19:", v_19.shape)
        v_20 = self.conv2d_4(v_19)
        print("v_20:", v_20.shape)

        v_21 = (v_16 + v_20)
        print("v_21:", v_21.shape)

        v_22 = F.relu(input=v_21)
        print("v_22:", v_22.shape)

        v_23 = self.conv2d_5(v_22)
        print("v_23:", v_23.shape)
        v_24 = self.conv2d_6(v_23)
        print("v_24:", v_24.shape)

        # v_25 = Pad(v_22, pads=(0, 0, 0, 0, 0, 4, 0, 0))
        # v_25 = self.pad_2(v_22)
        v_25 = F.pad(v_22, pad=(0, 0, 0, 0, 0, 4, 0, 0), mode='constant', value=0)
        print("v_25:", v_25.shape)

        v_26 = (v_25 + v_24)
        print("v_26:", v_26.shape)

        v_27 = F.relu(input=v_26)
        print("v_27:", v_27.shape)

        v_28 = self.conv2d_7(v_27)
        print("v_28:", v_28.shape)
        v_29 = self.conv2d_8(v_28)
        print("v_29:", v_29.shape)

        # v_30 = Pad(v_27, pads=(0, 0, 0, 0, 0, 6, 0, 0))
        # v_30 = self.pad_3(v_27)
        v_30 = F.pad(v_27, pad=(0, 0, 0, 0, 0, 6, 0, 0), mode='constant', value=0)
        print("v_30:", v_30.shape)

        v_31 = (v_30 + v_29)
        print("v_31:", v_31.shape)

        v_32 = F.relu(input=v_31)
        print("v_32:", v_32.shape)

        v_33 = F.max_pool2d(input=v_32, ceil_mode=False, kernel_size=(2, 2), padding=(0, 0), return_indices=False, stride=(2, 2))
        print("v_33:", v_33.shape)

        # v_34 = Pad(v_33, pads=(0, 0, 0, 0, 0, 6, 0, 0))
        # v_34 = self.pad_4(v_33)
        v_34 = F.pad(v_33, pad=(0, 0, 0, 0, 0, 6, 0, 0), mode='constant', value=0)
        print("v_34:", v_34.shape)

        # v_35 = self.const_fold_opt__480_data
        # v_36 = self.const_fold_opt__448_data
        # v_37 = Conv(v_32, v_35, v_36, dilations=(1, 1), group=42, kernel_shape=(3, 3), pads=(0, 0, 1, 1), strides=(2, 2))
        pad_conv_35 = self.pad_conv_35(v_32)
        print("pad_conv_35:", pad_conv_35.shape)
        v_37 = self.conv2d_35(pad_conv_35)
        print("v_37:", v_37.shape)
        v_38 = self.conv2d_9(v_37)
        print("v_38:", v_38.shape)

        v_39 = (v_34 + v_38)
        print("v_39:", v_39.shape)

        v_40 = F.relu(input=v_39)
        print("v_40:", v_40.shape)

        v_41 = self.conv2d_10(v_40)
        print("v_41:", v_41.shape)
        v_42 = self.conv2d_11(v_41)
        print("v_42:", v_42.shape)

        # v_43 = Pad(v_40, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        # v_43 = self.pad_5(v_40)
        v_43 = F.pad(v_40, pad=(0, 0, 0, 0, 0, 8, 0, 0), mode='constant', value=0)
        print("v_43:", v_43.shape)

        v_44 = (v_43 + v_42)
        print("v_44:", v_44.shape)

        v_45 = F.relu(input=v_44)
        print("v_45:", v_45.shape)

        v_46 = self.conv2d_12(v_45)
        print("v_46:", v_46.shape)
        v_47 = self.conv2d_13(v_46)
        print("v_47:", v_47.shape)

        # v_48 = Pad(v_45, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        # v_48 = self.pad_6(v_45)
        v_48 = F.pad(v_45, pad=(0, 0, 0, 0, 0, 8, 0, 0), mode='constant', value=0)
        print("v_48:", v_48.shape)

        v_49 = (v_48 + v_47)
        print("v_49:", v_49.shape)

        v_50 = F.relu(input=v_49)
        print("v_50:", v_50.shape)

        v_51 = self.conv2d_14(v_50)
        print("v_51:", v_51.shape)
        v_52 = self.conv2d_15(v_51)
        print("v_52:", v_52.shape)

        # v_53 = Pad(v_50, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        # v_53 = self.pad_7(v_50)
        v_53 = F.pad(v_50, pad=(0, 0, 0, 0, 0, 8, 0, 0), mode='constant', value=0)
        print("v_53:", v_53.shape)

        v_54 = (v_53 + v_52)
        print("v_54:", v_54.shape)

        v_55 = F.relu(input=v_54)
        print("v_55:", v_55.shape)

        v_56 = self.conv2d_16(v_55)
        print("v_56:", v_56.shape)
        v_57 = self.conv2d_17(v_56)
        print("v_57:", v_57.shape)

        # v_58 = Pad(v_55, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        # v_58 = self.pad_8(v_55)
        v_58 = F.pad(v_55, pad=(0, 0, 0, 0, 0, 8, 0, 0), mode='constant', value=0)
        print("v_58:", v_58.shape)

        v_59 = (v_58 + v_57)
        print("v_59:", v_59.shape)

        v_60 = F.relu(input=v_59)
        print("v_60:", v_60.shape)

        v_61 = self.conv2d_18(v_60)
        print("v_61:", v_61.shape)
        v_62 = self.conv2d_19(v_61)
        print("v_62:", v_62.shape)

        # v_63 = Pad(v_60, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        # v_63 = self.pad_9(v_60)
        v_63 = F.pad(v_60, pad=(0, 0, 0, 0, 0, 8, 0, 0), mode='constant', value=0)
        print("v_63:", v_63.shape)

        v_64 = (v_63 + v_62)
        print("v_64:", v_64.shape)

        v_65 = F.relu(input=v_64)
        print("v_65:", v_65.shape)

        v_66 = self.conv2d_20(v_65)
        print("v_66:", v_66.shape)

        v_67 = v_66.permute(dims=(0, 2, 3, 1))
        print("v_67:", v_67.shape)
        v_68 = v_67.reshape(1, 512, 16)
        print("v_68:", v_68.shape)

        v_69 = F.max_pool2d(input=v_65, ceil_mode=False, kernel_size=(2, 2), padding=(0, 0), return_indices=False, stride=(2, 2))
        print("v_69:", v_69.shape)

        # v_70 = Pad(v_69, pads=(0, 0, 0, 0, 0, 8, 0, 0))
        # v_70 = self.pad_10(v_69)
        v_70 = F.pad(v_69, pad=(0, 0, 0, 0, 0, 8, 0, 0), mode='constant', value=0)
        print("v_70:", v_70.shape)

        # v_71 = self.const_fold_opt__470_data
        # v_72 = self.const_fold_opt__376_data
        # v_73 = Conv(v_65, v_71, v_72, dilations=(1, 1), group=88, kernel_shape=(3, 3), pads=(0, 0, 1, 1), strides=(2, 2))
        pad_conv_36 = self.pad_conv_36(v_65)
        print("pad_conv_36:", pad_conv_36.shape)
        v_73 = self.conv2d_36(pad_conv_36)
        print("v_73:", v_73.shape)
        v_74 = self.conv2d_21(v_73)
        print("v_74:", v_74.shape)

        v_75 = (v_70 + v_74)
        print("v_75:", v_75.shape)

        v_76 = F.relu(input=v_75)
        print("v_76:", v_76.shape)

        v_77 = self.conv2d_22(v_76)
        print("v_77:", v_77.shape)
        v_78 = self.conv2d_23(v_77)
        print("v_78:", v_78.shape)

        v_79 = (v_76 + v_78)
        print("v_79:", v_79.shape)

        v_80 = F.relu(input=v_79)
        print("v_80:", v_80.shape)

        v_81 = self.conv2d_24(v_80)
        print("v_81:", v_81.shape)
        v_82 = self.conv2d_25(v_81)
        print("v_82:", v_82.shape)

        v_83 = (v_80 + v_82)
        print("v_83:", v_83.shape)

        v_84 = F.relu(input=v_83)
        print("v_84:", v_84.shape)

        v_85 = self.conv2d_26(v_84)
        print("v_85:", v_85.shape)
        v_86 = self.conv2d_27(v_85)
        print("v_86:", v_86.shape)

        v_87 = (v_84 + v_86)
        print("v_87:", v_87.shape)

        v_88 = F.relu(input=v_87)
        print("v_88:", v_88.shape)

        v_89 = self.conv2d_28(v_88)
        print("v_89:", v_89.shape)
        v_90 = self.conv2d_29(v_89)
        print("v_90:", v_90.shape)

        v_91 = (v_88 + v_90)
        print("v_91:", v_91.shape)

        v_92 = F.relu(input=v_91)
        print("v_92:", v_92.shape)

        v_93 = self.conv2d_30(v_92)
        print("v_93:", v_93.shape)

        v_94 = v_93.permute(dims=(0, 2, 3, 1))
        print("v_94:", v_94.shape)
        v_95 = v_94.reshape(1, 384, 16)
        print("v_95:", v_95.shape)

        v_96 = torch.cat((v_68, v_95), dim=1)
        print("v_96:", v_96.shape)

        v_97 = self.conv2d_31(v_92)
        print("v_97:", v_97.shape)

        v_98 = v_97.permute(dims=(0, 2, 3, 1))
        print("v_98:", v_98.shape)
        v_99 = v_98.reshape(1, 384, 1)
        print("v_99:", v_99.shape)

        v_100 = self.conv2d_32(v_65)
        print("v_100:", v_100.shape)
        v_101 = v_100.permute(dims=(0, 2, 3, 1))
        print("v_101:", v_101.shape)
        v_102 = v_101.reshape(1, 512, 1)
        print("v_102:", v_102.shape)
        v_103 = torch.cat((v_102, v_99), dim=1)
        print("v_103:", v_103.shape)

        return v_96, v_103


def export_torchscript():
    net = Model()
    net.float()
    net.eval()

    torch.manual_seed(0)
    v_0 = torch.rand(1, 128, 128, 3, dtype=torch.float)

    mod = torch.jit.trace(net, v_0)
    mod.save("./face_detector_pnnx.py.pt")


def export_onnx():
    net = Model()
    net.float()
    net.eval()

    torch.manual_seed(0)
    v_0 = torch.rand(1, 128, 128, 3, dtype=torch.float)

    torch.onnx.export(net, v_0, "./face_detector_pnnx.py.onnx", export_params=True, operator_export_type=torch.onnx.OperatorExportTypes.ONNX_ATEN_FALLBACK, opset_version=21, input_names=['in0'],
                      output_names=['out0', 'out1'])


@dataclass
class Anchor:
    x_center: float  # 归一化坐标 [0,1]
    y_center: float  # 归一化坐标 [0,1]
    w: float  # 归一化宽度 [0,1]
    h: float  # 归一化高度 [0,1]


def calculate_scale(min_scale: float, max_scale: float, stride_index: int, num_strides: int) -> float:
    """精确对应 C++ 的 CalculateScale 函数"""
    if num_strides == 1:
        return (min_scale + max_scale) * 0.5
    else:
        return min_scale + (max_scale - min_scale) * stride_index / (num_strides - 1.0)


def generate_face_detection_anchors(input_size=128):
    """生成与 face_detection_short_range.tflite 完全匹配的锚点 (896个)"""
    # 配置参数（来自 face_detection_short_range.pbtxt）
    options = {
        "num_layers": 4,
        "min_scale": 0.1484375,  # 128x128 输入对应的参数  19/128
        "max_scale": 0.75,
        "input_size_height": 128,
        "input_size_width": 128,
        "anchor_offset_x": 0.5,
        "anchor_offset_y": 0.5,
        "strides": [8, 16, 16, 16],  # 关键配置
        "aspect_ratios": [1.0],
        "fixed_anchor_size": True,
        "interpolated_scale_aspect_ratio": 1.0,
    }

    anchors = []
    layer_id = 0

    num_layers = options["num_layers"]
    min_scale = options["min_scale"]
    max_scale = options["max_scale"]
    input_size_height = options["input_size_height"]
    input_size_width = options["input_size_width"]
    anchor_offset_x = options["anchor_offset_x"]
    anchor_offset_y = options["anchor_offset_y"]
    strides = options["strides"]
    aspect_ratios = options["aspect_ratios"]
    fixed_anchor_size = options["fixed_anchor_size"]
    interpolated_scale_aspect_ratio = options["interpolated_scale_aspect_ratio"]

    reduce_boxes_in_lowest_layer = options.get("reduce_boxes_in_lowest_layer", False)

    # 遍历每一层
    while layer_id < options["num_layers"]:
        print("layer_id:", layer_id)
        anchor_height = []
        anchor_width = []
        aspect_ratios_layer = []
        scales_layer = []

        # 合并相同 stride 的层
        last_same_stride_layer = layer_id
        while (last_same_stride_layer < len(strides) and strides[last_same_stride_layer] == strides[layer_id]):
            current_stride_index = last_same_stride_layer
            scale = calculate_scale(min_scale, max_scale, current_stride_index, len(strides))

            if current_stride_index == 0 and reduce_boxes_in_lowest_layer:
                # 添加预定义锚点 (1.0, 2.0, 0.5)
                aspect_ratios_layer.extend([1.0, 2.0, 0.5])
                scales_layer.extend([0.1, scale, scale])
            else:
                # 添加普通锚点
                for ratio in aspect_ratios:
                    aspect_ratios_layer.append(ratio)
                    scales_layer.append(scale)

                # 处理插值比例
                if interpolated_scale_aspect_ratio > 0:
                    if current_stride_index == len(strides) - 1:
                        scale_next = 1.0
                    else:
                        scale_next = calculate_scale(min_scale, max_scale, current_stride_index + 1, len(strides))

                    scales_layer.append(math.sqrt(scale * scale_next))
                    aspect_ratios_layer.append(interpolated_scale_aspect_ratio)

            last_same_stride_layer += 1

        # 计算宽高
        for i in range(len(aspect_ratios_layer)):
            ratio_sqrts = math.sqrt(aspect_ratios_layer[i])
            anchor_height.append(scales_layer[i] / ratio_sqrts)
            anchor_width.append(scales_layer[i] * ratio_sqrts)

        # 计算特征图尺寸
        if "feature_map_height" in options and "feature_map_width" in options:
            feature_map_height = options["feature_map_height"][layer_id]
            feature_map_width = options["feature_map_width"][layer_id]
        else:
            stride = strides[layer_id]
            feature_map_height = math.ceil(input_size_height / stride)
            feature_map_width = math.ceil(input_size_width / stride)

        # 生成锚点
        for y in range(feature_map_height):
            for x in range(feature_map_width):
                for anchor_id in range(len(anchor_height)):
                    # 计算归一化中心坐标
                    x_center = (x + anchor_offset_x) / feature_map_width
                    y_center = (y + anchor_offset_y) / feature_map_height

                    # 创建锚点
                    if fixed_anchor_size:
                        w = 1.0
                        h = 1.0
                    else:
                        w = anchor_width[anchor_id]
                        h = anchor_height[anchor_id]

                    anchors.append(Anchor(
                        x_center=x_center,
                        y_center=y_center,
                        w=w,
                        h=h
                    ))

        # 移动到下一组不同 stride 的层
        layer_id = last_same_stride_layer
        print("loop end, layer_id:", layer_id)
    return anchors


def draw_detection(image, bbox, keypoints):
    """绘制检测框和关键点"""
    x, y, w, h = bbox
    pt1 = (int(x), int(y))
    pt2 = (int(x + w), int(y + h))
    cv2.rectangle(image, pt1, pt2, (0, 255, 0), 2)
    for (kp_x, kp_y) in keypoints:
        cv2.circle(image, (int(kp_x), int(kp_y)), 2, (0, 0, 255), -1)
    return image


def letterbox_padding(image, target_size):
    """返回填充后的图像和缩放参数"""
    h, w = image.shape[:2]
    target_h, target_w = target_size

    # 计算缩放比例（保持宽高比）
    scale = min(target_w / w, target_h / h)
    new_w = int(w * scale)
    new_h = int(h * scale)

    # 缩放图像
    resized = cv2.resize(image, (new_w, new_h))

    # 计算填充位置
    pad_w = (target_w - new_w) // 2
    pad_h = (target_h - new_h) // 2

    # 记录缩放和填充参数
    params = {
        "scale": scale,
        "pad_top": pad_h,
        "pad_bottom": target_h - new_h - pad_h,
        "pad_left": pad_w,
        "pad_right": target_w - new_w - pad_w,
        "original_size": (w, h),
        "padded_size": (target_w, target_h)
    }

    # 填充黑边
    padded = cv2.copyMakeBorder(
        resized,
        pad_h, target_h - new_h - pad_h,
        pad_w, target_w - new_w - pad_w,
        cv2.BORDER_CONSTANT,
        value=(0, 0, 0)
    )
    return padded, params


def transform_coords_back(x, y, params):
    """将检测坐标转换回原始图像坐标系"""
    # 去除填充偏移
    x_unpad = x - params["pad_left"]
    y_unpad = y - params["pad_top"]

    # 缩放还原
    x_orig = x_unpad / params["scale"]
    y_orig = y_unpad / params["scale"]

    # 确保坐标在原始图像范围内
    x_orig = np.clip(x_orig, 0, params["original_size"][0])
    y_orig = np.clip(y_orig, 0, params["original_size"][1])
    return x_orig, y_orig


def test_inference():
    net = Model()
    net.float()
    net.eval()

    # torch.manual_seed(0)
    # v_0 = torch.rand(1, 128, 128, 3, dtype=torch.float)

    original_img_path = "D:\\tmp\\image\\face_image_1080_1920.png"
    output_img_path = "D:\\tmp\\image\\face_detector_pytorch.png"
    original_with_detection_output_img_path = "D:\\tmp\\image\\face_detector_pytorch_on_original.png"

    anchors = generate_face_detection_anchors(input_size=128)

    # 加载图像并预处理
    originalImg = cv2.imread(original_img_path, cv2.IMREAD_UNCHANGED)
    if originalImg is None:
        raise FileNotFoundError(f"图片未找到: {original_img_path}")

    # 处理通道
    if originalImg.shape[2] == 4:
        originalImg = cv2.cvtColor(originalImg, cv2.COLOR_BGRA2RGB)
    else:
        originalImg = cv2.cvtColor(originalImg, cv2.COLOR_BGR2RGB)

    # 进行letterbox处理并记录参数
    padded, padding_params = letterbox_padding(originalImg, (128, 128))

    # 归一化到 [0, 1]
    input_data = padded.astype(np.float32) / 255.0
    input_data = np.expand_dims(input_data, axis=0)

    # 将 NumPy 数组转换为 torch.Tensor
    input_tensor = torch.from_numpy(input_data)
    # 如果需要，可以保证数据类型为 float32（通常模型期望的类型）
    input_tensor = input_tensor.float()

    regressors, scores = net(input_tensor)
    # 将结果转换为 numpy 数组
    regressors = regressors.detach().cpu().numpy()
    scores = scores.detach().cpu().numpy()

    # 解析分数
    scores_1d = scores.reshape(-1)
    scores_1d = np.clip(scores_1d, -100, 100)
    scores_1d = 1 / (1 + np.exp(-scores_1d))
    max_index = np.argmax(scores_1d)
    max_score = scores_1d[max_index]

    # 解析回归值
    best_regressor = regressors[0, max_index]
    dx, dy, w, h = best_regressor[:4]
    keypoints = best_regressor[4:]

    anchor = anchors[max_index]
    # 计算边界框
    box_center_x = dx + anchor.x_center * 128.0
    box_center_y = dy + anchor.y_center * 128.0
    box_w = w
    box_h = h
    box_x = box_center_x - box_w / 2
    box_y = box_center_y - box_h / 2
    bbox = (box_x, box_y, box_w, box_h)

    # 解析关键点
    keypoints_coords = []
    for i in range(0, len(keypoints), 2):
        kp_dx = keypoints[i]
        kp_dy = keypoints[i + 1]
        kp_x = kp_dx * anchor.w + anchor.x_center * 128.0
        kp_y = kp_dy * anchor.h + anchor.y_center * 128.0
        keypoints_coords.append((kp_x, kp_y))

    # 绘制结果
    result_img = padded.copy().astype(np.uint8)
    result_img = draw_detection(result_img, bbox, keypoints_coords)
    result_bgr = cv2.cvtColor(result_img, cv2.COLOR_RGB2BGR)
    cv2.imwrite(output_img_path, result_bgr)
    print(f"结果保存至: {output_img_path}")

    # 转换坐标到原始图像
    def transform_bbox(bbox, params):
        x, y, w, h = bbox
        # 转换左上角坐标
        x1_orig, y1_orig = transform_coords_back(x, y, params)
        # 转换右下角坐标
        x2_orig, y2_orig = transform_coords_back(x + w, y + h, params)
        return (x1_orig, y1_orig, x2_orig - x1_orig, y2_orig - y1_orig)

    def transform_keypoints(keypoints, params):
        return [transform_coords_back(kp[0], kp[1], params) for kp in keypoints]

    # 转换检测结果
    original_bbox = transform_bbox(bbox, padding_params)
    original_keypoints = transform_keypoints(keypoints_coords, padding_params)

    # 在原始图像上绘制结果
    original_with_detection_output_img = originalImg.copy()
    original_with_detection_output_img = draw_detection(original_with_detection_output_img,
                                                        original_bbox,
                                                        original_keypoints)

    # 保存结果（处理后的和原始的）
    cv2.imwrite(original_with_detection_output_img_path,
                cv2.cvtColor(original_with_detection_output_img, cv2.COLOR_RGB2BGR))
    print(f"原始图像检测结果保存至: {original_with_detection_output_img_path}")


if __name__ == "__main__":
    # print(test_inference())
    export_onnx()
```

这里修改过后的代码添加了测试代码部分

修复模型的关键点:

1 给每一个输入和输出添加输出 shape 的日志, 如

```
print("v_0:", v_0.shape)
v_1 = v_0.permute(dims=(0, 3, 1, 2))
print("v_1:", v_1.shape)
```



2 pnnx生成的Pad算子要转化为 pytroch 的 pad 方法:

```
import torch.nn.functional as F

# v_63 = Pad(v_60, pads=(0, 0, 0, 0, 0, 8, 0, 0))
v_63 = F.pad(v_60, pad=(0, 0, 0, 0, 0, 8, 0, 0), mode='constant', value=0)
```



3 每一个卷积层要手动在前面添加一个padding层并清除conv算子中的padding, 如:

```
# v_2 = self.const_fold_opt__437_data
# v_3 = self.const_fold_opt__441_data
# v_4 = Conv(v_1, v_2, v_3, dilations=(1, 1), group=1, kernel_shape=(5, 5), pads=(1, 1, 2, 2), strides=(2, 2))

self.pad_conv_33 = nn.ZeroPad2d(padding=(1, 2, 1, 2))
self.conv2d_33 = nn.Conv2d(bias=True, dilation=(1, 1), groups=1, in_channels=3, kernel_size=(5, 5), out_channels=24, padding=(0, 0), padding_mode='zeros', stride=(2, 2))
```

这里要注意pnnx生成的代码中是根据onnx模型的参数顺序输出的, 但是ncnn中的顺序是不一样的, 需要调整

生成的代码中 pads=(1, 1, 2, 2) 的顺序是 左上右下 , 而pytorch中 nn.ZeroPad2d(padding=(1, 2, 1, 2)) 顺序是 左右 上下 (left, right, top, bottom) 
注意: nn.ZeroPad2d 算子只能填充 WH 维度, 不能填充 NC 维度,所以其他的padding 算子要使用 F.pad 函数来实现



4 与原始 tflite 对照

在转换的过程中需要使用 netron 工具打开原始的 tflite 文件, 注意其中每个输入与输出的维度, 注意: 

**TFLite** 默认使用 **NHWC**（批次、Height、Width、Channels）数据格式，因为它继承自 TensorFlow 的默认布局。

**ONNX** 和 **ncnn** 中，大部分模型和算子（尤其是从 PyTorch 转换过来的）通常使用 **NCHW**（批次、Channels、Height、Width）格式。

不过需要注意的是，ONNX 本身并不强制规定具体的内存布局，具体格式取决于模型来源和转换工具，但在实际应用中，很多 ONNX 模型采用的是 NCHW 格式，而 ncnn 的设计也是基于 NCHW 格式的。

对照的过程中主要就是注意 channel 维度的位置不同



### 导出模型

修改完成代码后运行测试示例, 确认使用 pytorch 定义的模型正确 (与tflite模型输出一致) 后就可以再次导出onnx模型

```
if __name__ == "__main__":
    # print(test_inference())
    export_onnx()
```

上面代码中分别是测试推理和导出onnx模型, 导出的onnx 模型文件为 `face_detector_pnnx.py.onnx`, 然后再次将onnx模型转化为ncnn模型

为方便区分, 新建一个目录, 将 `face_detector_pnnx.py.onnx` 文件复制并重命名为 `face_detector.onnx`, 再次执行命令:

```
pnnx ./face_detector.onnx inputshape=[1,128,128,3]
```

输出:

```
PS D:\tmp\ncnn_pytorch> pnnx ./face_detector.onnx inputshape=[1,128,128,3]
pnnxparam = ./face_detector.pnnx.param
pnnxbin = ./face_detector.pnnx.bin
pnnxpy = ./face_detector_pnnx.py
pnnxonnx = ./face_detector.pnnx.onnx
ncnnparam = ./face_detector.ncnn.param
ncnnbin = ./face_detector.ncnn.bin
ncnnpy = ./face_detector_ncnn.py
fp16 = 1
optlevel = 2
device = cpu
inputshape = [1,128,128,3]f32
inputshape2 =
customop =
moduleop =
############# pass_level0 onnx
inline_containers ...                 0.00ms
eliminate_noop ...                    0.20ms
fold_constants ...                   43.27ms
canonicalize ...                      0.12ms
shape_inference ...                  12.15ms
fold_constants_dynamic_shape ...      0.12ms
inline_if_graph ...                   0.04ms
fuse_constant_as_attribute ...        0.15ms
eliminate_noop_with_shape ...         0.06ms
┌──────────────────┬──────────┬──────────┐
│                  │ orig     │ opt      │
├──────────────────┼──────────┼──────────┤
│ node             │ 347      │ 99       │
│ initializer      │ 70       │ 70       │
│ functions        │ 0        │ 0        │
├──────────────────┼──────────┼──────────┤
│ nn module op     │ 0        │ 0        │
│ custom module op │ 0        │ 0        │
│ aten op          │ 0        │ 0        │
│ prims op         │ 0        │ 0        │
│ onnx native op   │ 347      │ 99       │
├──────────────────┼──────────┼──────────┤
│ Add              │ 16       │ 16       │
│ Cast             │ 15       │ 0        │
│ Concat           │ 17       │ 2        │
│ Constant         │ 139      │ 0        │
│ ConstantOfShape  │ 15       │ 0        │
│ Conv             │ 37       │ 37       │
│ Identity         │ 4        │ 0        │
│ MaxPool          │ 3        │ 3        │
│ Pad              │ 15       │ 15       │
│ Relu             │ 17       │ 17       │
│ Reshape          │ 34       │ 4        │
│ Slice            │ 15       │ 0        │
│ Transpose        │ 20       │ 5        │
└──────────────────┴──────────┴──────────┘
############# pass_level1 onnx
############# pass_level2
############# pass_level3
open failed
############# pass_level4
############# pass_level5
############# pass_ncnn
```

可以看到这次没有再提示算子转化失败

```
todo Conv
todo Pad
```



测试ncnn模型

```
import math
from dataclasses import dataclass

import cv2
import ncnn
import numpy as np


@dataclass
class Anchor:
    x_center: float  # 归一化坐标 [0,1]
    y_center: float  # 归一化坐标 [0,1]
    w: float  # 归一化宽度 [0,1]
    h: float  # 归一化高度 [0,1]


def calculate_scale(min_scale: float, max_scale: float, stride_index: int, num_strides: int) -> float:
    """精确对应 C++ 的 CalculateScale 函数"""
    if num_strides == 1:
        return (min_scale + max_scale) * 0.5
    else:
        return min_scale + (max_scale - min_scale) * stride_index / (num_strides - 1.0)


def generate_face_detection_anchors(input_size=128):
    """生成与 face_detection_short_range.tflite 完全匹配的锚点 (896个)"""
    # 配置参数（来自 face_detection_short_range.pbtxt）
    options = {
        "num_layers": 4,
        "min_scale": 0.1484375,  # 128x128 输入对应的参数  19/128
        "max_scale": 0.75,
        "input_size_height": 128,
        "input_size_width": 128,
        "anchor_offset_x": 0.5,
        "anchor_offset_y": 0.5,
        "strides": [8, 16, 16, 16],  # 关键配置
        "aspect_ratios": [1.0],
        "fixed_anchor_size": True,
        "interpolated_scale_aspect_ratio": 1.0,
    }

    anchors = []
    layer_id = 0

    num_layers = options["num_layers"]
    min_scale = options["min_scale"]
    max_scale = options["max_scale"]
    input_size_height = options["input_size_height"]
    input_size_width = options["input_size_width"]
    anchor_offset_x = options["anchor_offset_x"]
    anchor_offset_y = options["anchor_offset_y"]
    strides = options["strides"]
    aspect_ratios = options["aspect_ratios"]
    fixed_anchor_size = options["fixed_anchor_size"]
    interpolated_scale_aspect_ratio = options["interpolated_scale_aspect_ratio"]

    reduce_boxes_in_lowest_layer = options.get("reduce_boxes_in_lowest_layer", False)

    # 遍历每一层
    while layer_id < options["num_layers"]:
        print("layer_id:", layer_id)
        anchor_height = []
        anchor_width = []
        aspect_ratios_layer = []
        scales_layer = []

        # 合并相同 stride 的层
        last_same_stride_layer = layer_id
        while (last_same_stride_layer < len(strides) and strides[last_same_stride_layer] == strides[layer_id]):
            current_stride_index = last_same_stride_layer
            scale = calculate_scale(min_scale, max_scale, current_stride_index, len(strides))

            if current_stride_index == 0 and reduce_boxes_in_lowest_layer:
                # 添加预定义锚点 (1.0, 2.0, 0.5)
                aspect_ratios_layer.extend([1.0, 2.0, 0.5])
                scales_layer.extend([0.1, scale, scale])
            else:
                # 添加普通锚点
                for ratio in aspect_ratios:
                    aspect_ratios_layer.append(ratio)
                    scales_layer.append(scale)

                # 处理插值比例
                if interpolated_scale_aspect_ratio > 0:
                    if current_stride_index == len(strides) - 1:
                        scale_next = 1.0
                    else:
                        scale_next = calculate_scale(min_scale, max_scale, current_stride_index + 1, len(strides))

                    scales_layer.append(math.sqrt(scale * scale_next))
                    aspect_ratios_layer.append(interpolated_scale_aspect_ratio)

            last_same_stride_layer += 1

        # 计算宽高
        for i in range(len(aspect_ratios_layer)):
            ratio_sqrts = math.sqrt(aspect_ratios_layer[i])
            anchor_height.append(scales_layer[i] / ratio_sqrts)
            anchor_width.append(scales_layer[i] * ratio_sqrts)

        # 计算特征图尺寸
        if "feature_map_height" in options and "feature_map_width" in options:
            feature_map_height = options["feature_map_height"][layer_id]
            feature_map_width = options["feature_map_width"][layer_id]
        else:
            stride = strides[layer_id]
            feature_map_height = math.ceil(input_size_height / stride)
            feature_map_width = math.ceil(input_size_width / stride)

        # 生成锚点
        for y in range(feature_map_height):
            for x in range(feature_map_width):
                for anchor_id in range(len(anchor_height)):
                    # 计算归一化中心坐标
                    x_center = (x + anchor_offset_x) / feature_map_width
                    y_center = (y + anchor_offset_y) / feature_map_height

                    # 创建锚点
                    if fixed_anchor_size:
                        w = 1.0
                        h = 1.0
                    else:
                        w = anchor_width[anchor_id]
                        h = anchor_height[anchor_id]

                    anchors.append(Anchor(
                        x_center=x_center,
                        y_center=y_center,
                        w=w,
                        h=h
                    ))

        # 移动到下一组不同 stride 的层
        layer_id = last_same_stride_layer
        print("loop end, layer_id:", layer_id)
    return anchors


def draw_detection(image, bbox, keypoints):
    """绘制检测框和关键点"""
    x, y, w, h = bbox
    pt1 = (int(x), int(y))
    pt2 = (int(x + w), int(y + h))
    cv2.rectangle(image, pt1, pt2, (0, 255, 0), 2)
    for (kp_x, kp_y) in keypoints:
        cv2.circle(image, (int(kp_x), int(kp_y)), 2, (0, 0, 255), -1)
    return image


def letterbox_padding(image, target_size):
    """返回填充后的图像和缩放参数"""
    h, w = image.shape[:2]
    target_h, target_w = target_size

    # 计算缩放比例（保持宽高比）
    scale = min(target_w / w, target_h / h)
    new_w = int(w * scale)
    new_h = int(h * scale)

    # 缩放图像
    resized = cv2.resize(image, (new_w, new_h))

    # 计算填充位置
    pad_w = (target_w - new_w) // 2
    pad_h = (target_h - new_h) // 2

    # 记录缩放和填充参数
    params = {
        "scale": scale,
        "pad_top": pad_h,
        "pad_bottom": target_h - new_h - pad_h,
        "pad_left": pad_w,
        "pad_right": target_w - new_w - pad_w,
        "original_size": (w, h),
        "padded_size": (target_w, target_h)
    }

    # 填充黑边
    padded = cv2.copyMakeBorder(
        resized,
        pad_h, target_h - new_h - pad_h,
        pad_w, target_w - new_w - pad_w,
        cv2.BORDER_CONSTANT,
        value=(0, 0, 0)
    )
    return padded, params


def transform_coords_back(x, y, params):
    """将检测坐标转换回原始图像坐标系"""
    # 去除填充偏移
    x_unpad = x - params["pad_left"]
    y_unpad = y - params["pad_top"]

    # 缩放还原
    x_orig = x_unpad / params["scale"]
    y_orig = y_unpad / params["scale"]

    # 确保坐标在原始图像范围内
    x_orig = np.clip(x_orig, 0, params["original_size"][0])
    y_orig = np.clip(y_orig, 0, params["original_size"][1])
    return x_orig, y_orig


if __name__ == '__main__':
    param_path = "D:\\tmp\\ncnn_pytorch\\face_detector.ncnn.param"
    # param_path = "D:\\tmp\\ncnn\\face_detector.ncnn_edit.param"
    bin_path = "D:\\tmp\\ncnn_pytorch\\face_detector.ncnn.bin"

    # original_img_path = "D:\\tmp\\image\\face_image_1080_1920.png"
    # original_img_path = "D:\\tmp\\image\\o\\face_image_2.png"
    # original_img_path = "D:\\tmp\\image\\o\\3b24ee3b73a37321163d7a218e70cfd9.jpeg"
    # original_img_path = "D:\\tmp\\image\\o\\8b4939d79538604539d5593312bbe024.jpeg"
    original_img_path = "D:\\tmp\\image\\o\\4620abc08cbc04e913e17a0c8f7f6cd3.jpeg"


    output_img_path = "D:\\tmp\\image\\face_detector_ncnn.png"
    original_with_detection_output_img_path = "D:\\tmp\\image\\face_detector_ncnn_with_original.png"

    anchors = generate_face_detection_anchors(input_size=128)

    # 输出前5个锚点信息
    print(f"生成的锚点总数: {len(anchors)}")
    for i in range(5):
        a = anchors[i]
        print(f"锚点 {i}: (x={a.x_center:.4f}, y={a.y_center:.4f}, w={a.w:.4f}, h={a.h:.4f})")

    # 加载图像并预处理
    originalImg = cv2.imread(original_img_path, cv2.IMREAD_UNCHANGED)
    if originalImg is None:
        raise FileNotFoundError(f"图片未找到: {original_img_path}")

    # 处理通道
    if originalImg.shape[2] == 4:
        originalImg = cv2.cvtColor(originalImg, cv2.COLOR_BGRA2RGB)
    else:
        originalImg = cv2.cvtColor(originalImg, cv2.COLOR_BGR2RGB)

    # 进行letterbox处理并记录参数
    padded, padding_params = letterbox_padding(originalImg, (128, 128))

    # 归一化到 [0, 1]
    input_data = padded.astype(np.float32) / 255.0
    input_data = np.expand_dims(input_data, axis=0)

    in_mat = ncnn.Mat(input_data)

    # 初始化网络
    net = ncnn.Net()
    # 加载模型
    print("load_param:", param_path)
    net.load_param(param_path)
    print("load_model:", bin_path)
    net.load_model(bin_path)
    # 创建提取器（类似 ONNX 的 session）
    ex = net.create_extractor()

    # 设置输入
    ex.input("in0", in_mat)

    # 执行推理
    ret1, regressors  = ex.extract("out0")
    ret2, scores = ex.extract("out1")

    # 解析输出数据
    regressors = np.array(regressors)  # 形状: (1, 896, 16)
    scores = np.array(scores)  # 形状: (1, 896, 1)

    # 解析分数
    scores_1d = scores.reshape(-1)
    scores_1d = np.clip(scores_1d, -100, 100)
    scores_1d = 1 / (1 + np.exp(-scores_1d))
    max_index = np.argmax(scores_1d)
    max_score = scores_1d[max_index]

    # 解析回归值
    best_regressor = regressors[max_index]
    dx, dy, w, h = best_regressor[:4]
    keypoints = best_regressor[4:]

    anchor = anchors[max_index]
    # 计算边界框
    box_center_x = dx + anchor.x_center * 128.0
    box_center_y = dy + anchor.y_center * 128.0
    box_w = w
    box_h = h
    box_x = box_center_x - box_w / 2
    box_y = box_center_y - box_h / 2
    bbox = (box_x, box_y, box_w, box_h)

    # 解析关键点
    keypoints_coords = []
    for i in range(0, len(keypoints), 2):
        kp_dx = keypoints[i]
        kp_dy = keypoints[i + 1]
        kp_x = kp_dx * anchor.w + anchor.x_center * 128.0
        kp_y = kp_dy * anchor.h + anchor.y_center * 128.0
        keypoints_coords.append((kp_x, kp_y))

    # 绘制结果
    result_img = padded.copy().astype(np.uint8)
    result_img = draw_detection(result_img, bbox, keypoints_coords)
    result_bgr = cv2.cvtColor(result_img, cv2.COLOR_RGB2BGR)
    cv2.imwrite(output_img_path, result_bgr)
    print(f"结果保存至: {output_img_path}")


    # 转换坐标到原始图像
    def transform_bbox(bbox, params):
        x, y, w, h = bbox
        # 转换左上角坐标
        x1_orig, y1_orig = transform_coords_back(x, y, params)
        # 转换右下角坐标
        x2_orig, y2_orig = transform_coords_back(x + w, y + h, params)
        return (x1_orig, y1_orig, x2_orig - x1_orig, y2_orig - y1_orig)


    def transform_keypoints(keypoints, params):
        return [transform_coords_back(kp[0], kp[1], params) for kp in keypoints]


    # 转换检测结果
    original_bbox = transform_bbox(bbox, padding_params)
    original_keypoints = transform_keypoints(keypoints_coords, padding_params)

    # 在原始图像上绘制结果
    original_with_detection_output_img = originalImg.copy()
    original_with_detection_output_img = draw_detection(original_with_detection_output_img,
                                                        original_bbox,
                                                        original_keypoints)

    # 保存结果（处理后的和原始的）
    cv2.imwrite(original_with_detection_output_img_path,
                cv2.cvtColor(original_with_detection_output_img, cv2.COLOR_RGB2BGR))
    print(f"原始图像检测结果保存至: {original_with_detection_output_img_path}")
```

需要注意的点:
1 数据需要归一化和转化为ncnn参数的类型

```
# 归一化到 [0, 1]
input_data = padded.astype(np.float32) / 255.0
input_data = np.expand_dims(input_data, axis=0)

in_mat = ncnn.Mat(input_data)
```

2 返回值处理

```
# 执行推理
ret1, regressors  = ex.extract("out0")
ret2, scores = ex.extract("out1")

# 解析输出数据
regressors = np.array(regressors)  # 形状: (1, 896, 16)
scores = np.array(scores)  # 形状: (1, 896, 1)
```

返回值需要转化为 numpy 类型供后续处理



另外其他模型的测试代码如下:

tflite模型测试:

face_detector_tflite_inference.py

```
import os

os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'

import cv2
import numpy as np
import tensorflow as tf

import math
from dataclasses import dataclass


@dataclass
class Anchor:
    x_center: float  # 归一化坐标 [0,1]
    y_center: float  # 归一化坐标 [0,1]
    w: float  # 归一化宽度 [0,1]
    h: float  # 归一化高度 [0,1]


def calculate_scale(min_scale: float, max_scale: float, stride_index: int, num_strides: int) -> float:
    """精确对应 C++ 的 CalculateScale 函数"""
    if num_strides == 1:
        return (min_scale + max_scale) * 0.5
    else:
        return min_scale + (max_scale - min_scale) * stride_index / (num_strides - 1.0)


def generate_face_detection_anchors(input_size=128):
    """生成与 face_detection_short_range.tflite 完全匹配的锚点 (896个)"""
    # 配置参数（来自 face_detection_short_range.pbtxt）
    options = {
        "num_layers": 4,
        "min_scale": 0.1484375,  # 128x128 输入对应的参数  19/128
        "max_scale": 0.75,
        "input_size_height": 128,
        "input_size_width": 128,
        "anchor_offset_x": 0.5,
        "anchor_offset_y": 0.5,
        "strides": [8, 16, 16, 16],  # 关键配置
        "aspect_ratios": [1.0],
        "fixed_anchor_size": True,
        "interpolated_scale_aspect_ratio": 1.0,
    }

    anchors = []
    layer_id = 0

    num_layers = options["num_layers"]
    min_scale = options["min_scale"]
    max_scale = options["max_scale"]
    input_size_height = options["input_size_height"]
    input_size_width = options["input_size_width"]
    anchor_offset_x = options["anchor_offset_x"]
    anchor_offset_y = options["anchor_offset_y"]
    strides = options["strides"]
    aspect_ratios = options["aspect_ratios"]
    fixed_anchor_size = options["fixed_anchor_size"]
    interpolated_scale_aspect_ratio = options["interpolated_scale_aspect_ratio"]

    reduce_boxes_in_lowest_layer = options.get("reduce_boxes_in_lowest_layer", False)

    # 遍历每一层
    while layer_id < options["num_layers"]:
        print("layer_id:", layer_id)
        anchor_height = []
        anchor_width = []
        aspect_ratios_layer = []
        scales_layer = []

        # 合并相同 stride 的层
        last_same_stride_layer = layer_id
        while (last_same_stride_layer < len(strides) and strides[last_same_stride_layer] == strides[layer_id]):
            current_stride_index = last_same_stride_layer
            scale = calculate_scale(min_scale, max_scale, current_stride_index, len(strides))

            if current_stride_index == 0 and reduce_boxes_in_lowest_layer:
                # 添加预定义锚点 (1.0, 2.0, 0.5)
                aspect_ratios_layer.extend([1.0, 2.0, 0.5])
                scales_layer.extend([0.1, scale, scale])
            else:
                # 添加普通锚点
                for ratio in aspect_ratios:
                    aspect_ratios_layer.append(ratio)
                    scales_layer.append(scale)

                # 处理插值比例
                if interpolated_scale_aspect_ratio > 0:
                    if current_stride_index == len(strides) - 1:
                        scale_next = 1.0
                    else:
                        scale_next = calculate_scale(min_scale, max_scale, current_stride_index + 1, len(strides))

                    scales_layer.append(math.sqrt(scale * scale_next))
                    aspect_ratios_layer.append(interpolated_scale_aspect_ratio)

            last_same_stride_layer += 1

        # 计算宽高
        for i in range(len(aspect_ratios_layer)):
            ratio_sqrts = math.sqrt(aspect_ratios_layer[i])
            anchor_height.append(scales_layer[i] / ratio_sqrts)
            anchor_width.append(scales_layer[i] * ratio_sqrts)

        # 计算特征图尺寸
        if "feature_map_height" in options and "feature_map_width" in options:
            feature_map_height = options["feature_map_height"][layer_id]
            feature_map_width = options["feature_map_width"][layer_id]
        else:
            stride = strides[layer_id]
            feature_map_height = math.ceil(input_size_height / stride)
            feature_map_width = math.ceil(input_size_width / stride)

        # 生成锚点
        for y in range(feature_map_height):
            for x in range(feature_map_width):
                for anchor_id in range(len(anchor_height)):
                    # 计算归一化中心坐标
                    x_center = (x + anchor_offset_x) / feature_map_width
                    y_center = (y + anchor_offset_y) / feature_map_height

                    # 创建锚点
                    if fixed_anchor_size:
                        w = 1.0
                        h = 1.0
                    else:
                        w = anchor_width[anchor_id]
                        h = anchor_height[anchor_id]

                    anchors.append(Anchor(
                        x_center=x_center,
                        y_center=y_center,
                        w=w,
                        h=h
                    ))

        # 移动到下一组不同 stride 的层
        layer_id = last_same_stride_layer
        print("loop end, layer_id:", layer_id)
    return anchors


def draw_detection(image, bbox, keypoints):
    """绘制检测框和关键点"""
    x, y, w, h = bbox
    pt1 = (int(x), int(y))
    pt2 = (int(x + w), int(y + h))
    cv2.rectangle(image, pt1, pt2, (0, 255, 0), 2)
    for (kp_x, kp_y) in keypoints:
        cv2.circle(image, (int(kp_x), int(kp_y)), 2, (0, 0, 255), -1)
    return image


def letterbox_padding(image, target_size):
    """返回填充后的图像和缩放参数"""
    h, w = image.shape[:2]
    target_h, target_w = target_size

    # 计算缩放比例（保持宽高比）
    scale = min(target_w / w, target_h / h)
    new_w = int(w * scale)
    new_h = int(h * scale)

    # 缩放图像
    resized = cv2.resize(image, (new_w, new_h))

    # 计算填充位置
    pad_w = (target_w - new_w) // 2
    pad_h = (target_h - new_h) // 2

    # 记录缩放和填充参数
    params = {
        "scale": scale,
        "pad_top": pad_h,
        "pad_bottom": target_h - new_h - pad_h,
        "pad_left": pad_w,
        "pad_right": target_w - new_w - pad_w,
        "original_size": (w, h),
        "padded_size": (target_w, target_h)
    }

    # 填充黑边
    padded = cv2.copyMakeBorder(
        resized,
        pad_h, target_h - new_h - pad_h,
        pad_w, target_w - new_w - pad_w,
        cv2.BORDER_CONSTANT,
        value=(0, 0, 0)
    )
    return padded, params


def transform_coords_back(x, y, params):
    """将检测坐标转换回原始图像坐标系"""
    # 去除填充偏移
    x_unpad = x - params["pad_left"]
    y_unpad = y - params["pad_top"]

    # 缩放还原
    x_orig = x_unpad / params["scale"]
    y_orig = y_unpad / params["scale"]

    # 确保坐标在原始图像范围内
    x_orig = np.clip(x_orig, 0, params["original_size"][0])
    y_orig = np.clip(y_orig, 0, params["original_size"][1])
    return x_orig, y_orig


if __name__ == '__main__':
    model_path = "D:\\tmp\\mediapipe\\blaze_face_short_range.tflite"
    # original_img_path = "D:\\tmp\\image\\face_image_1080_1920.png"
    # original_img_path = "D:\\tmp\\image\\face_image_2.png"
    # original_img_path = "D:\\download\\4620abc08cbc04e913e17a0c8f7f6cd3.jpeg"
    original_img_path = "D:\\download\\3b24ee3b73a37321163d7a218e70cfd9.jpeg"

    output_img_path = "D:\\tmp\\image\\face_detector_tflite.png"
    original_with_detection_output_img_path = "D:\\tmp\\image\\face_detector_tflite_original_with_detection.png"

    anchors = generate_face_detection_anchors(input_size=128)

    # 输出前5个锚点信息
    print(f"生成的锚点总数: {len(anchors)}")
    for i in range(5):
        a = anchors[i]
        print(f"锚点 {i}: (x={a.x_center:.4f}, y={a.y_center:.4f}, w={a.w:.4f}, h={a.h:.4f})")

    # 加载模型
    interpreter = tf.lite.Interpreter(model_path=model_path)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    # 加载图像并预处理
    originalImg = cv2.imread(original_img_path, cv2.IMREAD_UNCHANGED)
    if originalImg is None:
        raise FileNotFoundError(f"图片未找到: {original_img_path}")

    # 处理通道
    if originalImg.shape[2] == 4:
        originalImg = cv2.cvtColor(originalImg, cv2.COLOR_BGRA2RGB)
    else:
        originalImg = cv2.cvtColor(originalImg, cv2.COLOR_BGR2RGB)

    # 进行letterbox处理并记录参数
    padded, padding_params = letterbox_padding(originalImg, (128, 128))

    # 归一化到 [-1, 1]
    input_data = padded.astype(np.float32) / 255.0
    input_data = np.expand_dims(input_data, axis=0)

    # 推理
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()

    # 获取输出（修正索引）
    regressors = interpreter.get_tensor(output_details[0]['index'])  # 输出0: 分数
    scores = interpreter.get_tensor(output_details[1]['index'])  # 输出1: 回归值

    # 解析分数
    scores_1d = scores.reshape(-1)
    scores_1d = np.clip(scores_1d, -100, 100)
    scores_1d = 1 / (1 + np.exp(-scores_1d))
    max_index = np.argmax(scores_1d)
    max_score = scores_1d[max_index]

    # 解析回归值
    best_regressor = regressors[0, max_index]
    dx, dy, w, h = best_regressor[:4]
    keypoints = best_regressor[4:]

    anchor = anchors[max_index]
    # 计算边界框
    box_center_x = dx + anchor.x_center * 128.0
    box_center_y = dy + anchor.y_center * 128.0
    box_w = w
    box_h = h
    box_x = box_center_x - box_w / 2
    box_y = box_center_y - box_h / 2
    bbox = (box_x, box_y, box_w, box_h)

    # 解析关键点
    keypoints_coords = []
    for i in range(0, len(keypoints), 2):
        kp_dx = keypoints[i]
        kp_dy = keypoints[i + 1]
        kp_x = kp_dx * anchor.w + anchor.x_center * 128.0
        kp_y = kp_dy * anchor.h + anchor.y_center * 128.0
        keypoints_coords.append((kp_x, kp_y))

    # 绘制结果
    result_img = padded.copy().astype(np.uint8)
    result_img = draw_detection(result_img, bbox, keypoints_coords)
    result_bgr = cv2.cvtColor(result_img, cv2.COLOR_RGB2BGR)
    cv2.imwrite(output_img_path, result_bgr)
    print(f"结果保存至: {output_img_path}")


    # 转换坐标到原始图像
    def transform_bbox(bbox, params):
        x, y, w, h = bbox
        # 转换左上角坐标
        x1_orig, y1_orig = transform_coords_back(x, y, params)
        # 转换右下角坐标
        x2_orig, y2_orig = transform_coords_back(x + w, y + h, params)
        return (x1_orig, y1_orig, x2_orig - x1_orig, y2_orig - y1_orig)

    def transform_keypoints(keypoints, params):
        return [transform_coords_back(kp[0], kp[1], params) for kp in keypoints]

    # 转换检测结果
    original_bbox = transform_bbox(bbox, padding_params)
    original_keypoints = transform_keypoints(keypoints_coords, padding_params)

    # 在原始图像上绘制结果
    original_with_detection_output_img = originalImg.copy()
    original_with_detection_output_img = draw_detection(original_with_detection_output_img,
                                                             original_bbox,
                                                             original_keypoints)

    # 保存结果（处理后的和原始的）
    cv2.imwrite(original_with_detection_output_img_path, cv2.cvtColor(original_with_detection_output_img, cv2.COLOR_RGB2BGR))
    print(f"原始图像检测结果保存至: {original_with_detection_output_img_path}")
```



onnx模型测试代码:

face_detector_onnx_inference.py

```
import math
from dataclasses import dataclass

import cv2
import ncnn
import numpy as np


@dataclass
class Anchor:
    x_center: float  # 归一化坐标 [0,1]
    y_center: float  # 归一化坐标 [0,1]
    w: float  # 归一化宽度 [0,1]
    h: float  # 归一化高度 [0,1]


def calculate_scale(min_scale: float, max_scale: float, stride_index: int, num_strides: int) -> float:
    """精确对应 C++ 的 CalculateScale 函数"""
    if num_strides == 1:
        return (min_scale + max_scale) * 0.5
    else:
        return min_scale + (max_scale - min_scale) * stride_index / (num_strides - 1.0)


def generate_face_detection_anchors(input_size=128):
    """生成与 face_detection_short_range.tflite 完全匹配的锚点 (896个)"""
    # 配置参数（来自 face_detection_short_range.pbtxt）
    options = {
        "num_layers": 4,
        "min_scale": 0.1484375,  # 128x128 输入对应的参数  19/128
        "max_scale": 0.75,
        "input_size_height": 128,
        "input_size_width": 128,
        "anchor_offset_x": 0.5,
        "anchor_offset_y": 0.5,
        "strides": [8, 16, 16, 16],  # 关键配置
        "aspect_ratios": [1.0],
        "fixed_anchor_size": True,
        "interpolated_scale_aspect_ratio": 1.0,
    }

    anchors = []
    layer_id = 0

    num_layers = options["num_layers"]
    min_scale = options["min_scale"]
    max_scale = options["max_scale"]
    input_size_height = options["input_size_height"]
    input_size_width = options["input_size_width"]
    anchor_offset_x = options["anchor_offset_x"]
    anchor_offset_y = options["anchor_offset_y"]
    strides = options["strides"]
    aspect_ratios = options["aspect_ratios"]
    fixed_anchor_size = options["fixed_anchor_size"]
    interpolated_scale_aspect_ratio = options["interpolated_scale_aspect_ratio"]

    reduce_boxes_in_lowest_layer = options.get("reduce_boxes_in_lowest_layer", False)

    # 遍历每一层
    while layer_id < options["num_layers"]:
        print("layer_id:", layer_id)
        anchor_height = []
        anchor_width = []
        aspect_ratios_layer = []
        scales_layer = []

        # 合并相同 stride 的层
        last_same_stride_layer = layer_id
        while (last_same_stride_layer < len(strides) and strides[last_same_stride_layer] == strides[layer_id]):
            current_stride_index = last_same_stride_layer
            scale = calculate_scale(min_scale, max_scale, current_stride_index, len(strides))

            if current_stride_index == 0 and reduce_boxes_in_lowest_layer:
                # 添加预定义锚点 (1.0, 2.0, 0.5)
                aspect_ratios_layer.extend([1.0, 2.0, 0.5])
                scales_layer.extend([0.1, scale, scale])
            else:
                # 添加普通锚点
                for ratio in aspect_ratios:
                    aspect_ratios_layer.append(ratio)
                    scales_layer.append(scale)

                # 处理插值比例
                if interpolated_scale_aspect_ratio > 0:
                    if current_stride_index == len(strides) - 1:
                        scale_next = 1.0
                    else:
                        scale_next = calculate_scale(min_scale, max_scale, current_stride_index + 1, len(strides))

                    scales_layer.append(math.sqrt(scale * scale_next))
                    aspect_ratios_layer.append(interpolated_scale_aspect_ratio)

            last_same_stride_layer += 1

        # 计算宽高
        for i in range(len(aspect_ratios_layer)):
            ratio_sqrts = math.sqrt(aspect_ratios_layer[i])
            anchor_height.append(scales_layer[i] / ratio_sqrts)
            anchor_width.append(scales_layer[i] * ratio_sqrts)

        # 计算特征图尺寸
        if "feature_map_height" in options and "feature_map_width" in options:
            feature_map_height = options["feature_map_height"][layer_id]
            feature_map_width = options["feature_map_width"][layer_id]
        else:
            stride = strides[layer_id]
            feature_map_height = math.ceil(input_size_height / stride)
            feature_map_width = math.ceil(input_size_width / stride)

        # 生成锚点
        for y in range(feature_map_height):
            for x in range(feature_map_width):
                for anchor_id in range(len(anchor_height)):
                    # 计算归一化中心坐标
                    x_center = (x + anchor_offset_x) / feature_map_width
                    y_center = (y + anchor_offset_y) / feature_map_height

                    # 创建锚点
                    if fixed_anchor_size:
                        w = 1.0
                        h = 1.0
                    else:
                        w = anchor_width[anchor_id]
                        h = anchor_height[anchor_id]

                    anchors.append(Anchor(
                        x_center=x_center,
                        y_center=y_center,
                        w=w,
                        h=h
                    ))

        # 移动到下一组不同 stride 的层
        layer_id = last_same_stride_layer
        print("loop end, layer_id:", layer_id)
    return anchors


def draw_detection(image, bbox, keypoints):
    """绘制检测框和关键点"""
    x, y, w, h = bbox
    pt1 = (int(x), int(y))
    pt2 = (int(x + w), int(y + h))
    cv2.rectangle(image, pt1, pt2, (0, 255, 0), 2)
    for (kp_x, kp_y) in keypoints:
        cv2.circle(image, (int(kp_x), int(kp_y)), 2, (0, 0, 255), -1)
    return image


def letterbox_padding(image, target_size):
    """返回填充后的图像和缩放参数"""
    h, w = image.shape[:2]
    target_h, target_w = target_size

    # 计算缩放比例（保持宽高比）
    scale = min(target_w / w, target_h / h)
    new_w = int(w * scale)
    new_h = int(h * scale)

    # 缩放图像
    resized = cv2.resize(image, (new_w, new_h))

    # 计算填充位置
    pad_w = (target_w - new_w) // 2
    pad_h = (target_h - new_h) // 2

    # 记录缩放和填充参数
    params = {
        "scale": scale,
        "pad_top": pad_h,
        "pad_bottom": target_h - new_h - pad_h,
        "pad_left": pad_w,
        "pad_right": target_w - new_w - pad_w,
        "original_size": (w, h),
        "padded_size": (target_w, target_h)
    }

    # 填充黑边
    padded = cv2.copyMakeBorder(
        resized,
        pad_h, target_h - new_h - pad_h,
        pad_w, target_w - new_w - pad_w,
        cv2.BORDER_CONSTANT,
        value=(0, 0, 0)
    )
    return padded, params


def transform_coords_back(x, y, params):
    """将检测坐标转换回原始图像坐标系"""
    # 去除填充偏移
    x_unpad = x - params["pad_left"]
    y_unpad = y - params["pad_top"]

    # 缩放还原
    x_orig = x_unpad / params["scale"]
    y_orig = y_unpad / params["scale"]

    # 确保坐标在原始图像范围内
    x_orig = np.clip(x_orig, 0, params["original_size"][0])
    y_orig = np.clip(y_orig, 0, params["original_size"][1])
    return x_orig, y_orig


if __name__ == '__main__':
    param_path = "D:\\tmp\\ncnn_pytorch\\face_detector.ncnn.param"
    # param_path = "D:\\tmp\\ncnn\\face_detector.ncnn_edit.param"
    bin_path = "D:\\tmp\\ncnn_pytorch\\face_detector.ncnn.bin"

    original_img_path = "D:\\tmp\\image\\o\\face_image_1080_1920.png"
    # original_img_path = "D:\\tmp\\image\\o\\face_image_2.png"
    # original_img_path = "D:\\tmp\\image\\o\\3b24ee3b73a37321163d7a218e70cfd9.jpeg"
    # original_img_path = "D:\\tmp\\image\\o\\8b4939d79538604539d5593312bbe024.jpeg"
    # original_img_path = "D:\\tmp\\image\\o\\4620abc08cbc04e913e17a0c8f7f6cd3.jpeg"

    output_img_path = "D:\\tmp\\image\\face_detector_ncnn.png"
    original_with_detection_output_img_path = "D:\\tmp\\image\\face_detector_ncnn_with_original.png"

    anchors = generate_face_detection_anchors(input_size=128)

    # 输出前5个锚点信息
    print(f"生成的锚点总数: {len(anchors)}")
    for i in range(5):
        a = anchors[i]
        print(f"锚点 {i}: (x={a.x_center:.4f}, y={a.y_center:.4f}, w={a.w:.4f}, h={a.h:.4f})")

    # 加载图像并预处理
    originalImg = cv2.imread(original_img_path, cv2.IMREAD_UNCHANGED)
    if originalImg is None:
        raise FileNotFoundError(f"图片未找到: {original_img_path}")

    # 处理通道
    if originalImg.shape[2] == 4:
        originalImg = cv2.cvtColor(originalImg, cv2.COLOR_BGRA2RGB)
    else:
        originalImg = cv2.cvtColor(originalImg, cv2.COLOR_BGR2RGB)

    # 进行letterbox处理并记录参数
    padded, padding_params = letterbox_padding(originalImg, (128, 128))

    # 归一化到 [0, 1]
    input_data = padded.astype(np.float32) / 255.0
    print("input_data", input_data.shape)
    input_data = np.expand_dims(input_data, axis=0)
    print("input_data", input_data.shape)
    in_mat = ncnn.Mat(input_data)
    print("in_mat:", in_mat.d, in_mat.c, in_mat.h, in_mat.w)

    # 初始化网络
    net = ncnn.Net()
    # 加载模型
    print("load_param:", param_path)
    net.load_param(param_path)
    print("load_model:", bin_path)
    net.load_model(bin_path)
    # 创建提取器（类似 ONNX 的 session）
    ex = net.create_extractor()

    # 设置输入
    ex.input("in0", in_mat)

    # 执行推理
    ret1, regressors = ex.extract("out0")
    ret2, scores = ex.extract("out1")

    # 解析输出数据
    regressors = np.array(regressors)  # 形状: (1, 896, 16)
    scores = np.array(scores)  # 形状: (1, 896, 1)

    # 解析分数
    scores_1d = scores.reshape(-1)
    scores_1d = np.clip(scores_1d, -100, 100)
    scores_1d = 1 / (1 + np.exp(-scores_1d))
    max_index = np.argmax(scores_1d)
    max_score = scores_1d[max_index]
    print(f"max_index:{max_index}, max_score: {max_score}")
    # 解析回归值
    best_regressor = regressors[max_index]
    dx, dy, w, h = best_regressor[:4]
    keypoints = best_regressor[4:]

    anchor = anchors[max_index]
    # 计算边界框
    box_center_x = dx + anchor.x_center * 128.0
    box_center_y = dy + anchor.y_center * 128.0
    box_w = w
    box_h = h
    box_x = box_center_x - box_w / 2
    box_y = box_center_y - box_h / 2
    bbox = (box_x, box_y, box_w, box_h)

    # 解析关键点
    keypoints_coords = []
    for i in range(0, len(keypoints), 2):
        kp_dx = keypoints[i]
        kp_dy = keypoints[i + 1]
        kp_x = kp_dx * anchor.w + anchor.x_center * 128.0
        kp_y = kp_dy * anchor.h + anchor.y_center * 128.0
        keypoints_coords.append((kp_x, kp_y))

    # 绘制结果
    result_img = padded.copy().astype(np.uint8)
    result_img = draw_detection(result_img, bbox, keypoints_coords)
    result_bgr = cv2.cvtColor(result_img, cv2.COLOR_RGB2BGR)
    cv2.imwrite(output_img_path, result_bgr)
    print(f"结果保存至: {output_img_path}")


    # 转换坐标到原始图像
    def transform_bbox(bbox, params):
        x, y, w, h = bbox
        # 转换左上角坐标
        x1_orig, y1_orig = transform_coords_back(x, y, params)
        # 转换右下角坐标
        x2_orig, y2_orig = transform_coords_back(x + w, y + h, params)
        return (x1_orig, y1_orig, x2_orig - x1_orig, y2_orig - y1_orig)


    def transform_keypoints(keypoints, params):
        return [transform_coords_back(kp[0], kp[1], params) for kp in keypoints]


    # 转换检测结果
    original_bbox = transform_bbox(bbox, padding_params)
    original_keypoints = transform_keypoints(keypoints_coords, padding_params)

    # 在原始图像上绘制结果
    original_with_detection_output_img = originalImg.copy()
    original_with_detection_output_img = draw_detection(original_with_detection_output_img,
                                                        original_bbox,
                                                        original_keypoints)

    # 保存结果（处理后的和原始的）
    cv2.imwrite(original_with_detection_output_img_path,
                cv2.cvtColor(original_with_detection_output_img, cv2.COLOR_RGB2BGR))
    print(f"原始图像检测结果保存至: {original_with_detection_output_img_path}")
```

这里遗留了一个问题:

```
    # 归一化到 [0, 1]
    input_data = padded.astype(np.float32) / 255.0
    print("input_data", input_data.shape)
    input_data = np.expand_dims(input_data, axis=0)
    print("input_data", input_data.shape)
    in_mat = ncnn.Mat(input_data)
    print("in_mat:", in_mat.d, in_mat.c, in_mat.h, in_mat.w)
```

输出:

```
input_data (128, 128, 3)
input_data (1, 128, 128, 3)
in_mat: 128 1 128 3
```

输入的张量的维度很混乱, 需要调整



C++测试代码:

main.cpp

```
#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <ctime>
#include <vector>
#include <string>
#include <iostream>
#include <algorithm>
#include <cassert>

#include <opencv2/opencv.hpp>
#include "ncnn/net.h"
#include "Log.h"

using std::vector;
using std::string;

// ---------------------- 数据结构定义 ----------------------

struct Anchor {
    float x_center; // 归一化坐标 [0,1]
    float y_center; // 归一化坐标 [0,1]
    float w; // 归一化宽度 [0,1]
    float h; // 归一化高度 [0,1]
};

struct PaddingParams {
    float scale;
    int pad_top;
    int pad_bottom;
    int pad_left;
    int pad_right;
    int original_w;
    int original_h;
    int padded_w;
    int padded_h;
};

// ---------------------- 工具函数 ----------------------

// 计算 scale，与 Python 中的 calculate_scale 函数相同
float calculate_scale(float min_scale, float max_scale, int stride_index, int num_strides) {
    if (num_strides == 1)
        return (min_scale + max_scale) * 0.5f;
    else
        return min_scale + (max_scale - min_scale) * stride_index / (num_strides - 1.0f);
}

vector<Anchor> generate_face_detection_anchors(int input_size/*= 128*/) {
    int num_layers = 4;
    float min_scale = 0.1484375f; // 19/128
    float max_scale = 0.75f;
    int input_size_height = input_size;
    int input_size_width = input_size;
    float anchor_offset_x = 0.5f;
    float anchor_offset_y = 0.5f;
    vector<int> strides = {8, 16, 16, 16};
    vector<float> aspect_ratios = {1.0f};
    bool fixed_anchor_size = true;
    float interpolated_scale_aspect_ratio = 1.0f;
    bool reduce_boxes_in_lowest_layer = false;

    vector<Anchor> anchors;
    int layer_id = 0;
    int num_strides = strides.size();

    while (layer_id < num_layers) {
        LOG_I("layer_id: %d", layer_id);
        vector<float> anchor_heights;
        vector<float> anchor_widths;
        vector<float> aspect_ratios_layer;
        vector<float> scales_layer;

        int last_same_stride_layer = layer_id;
        while (last_same_stride_layer < num_strides && strides[last_same_stride_layer] == strides[layer_id]) {
            int current_stride_index = last_same_stride_layer;
            float scale = calculate_scale(min_scale, max_scale, current_stride_index, num_strides);
            if (current_stride_index == 0 && reduce_boxes_in_lowest_layer) {
                aspect_ratios_layer.push_back(1.0f);
                aspect_ratios_layer.push_back(2.0f);
                aspect_ratios_layer.push_back(0.5f);
                scales_layer.push_back(0.1f);
                scales_layer.push_back(scale);
                scales_layer.push_back(scale);
            } else {
                for (float ratio: aspect_ratios) {
                    aspect_ratios_layer.push_back(ratio);
                    scales_layer.push_back(scale);
                }
                if (interpolated_scale_aspect_ratio > 0) {
                    float scale_next = (current_stride_index == num_strides - 1)
                                           ? 1.0f
                                           : calculate_scale(min_scale, max_scale, current_stride_index + 1, num_strides);
                    scales_layer.push_back(std::sqrt(scale * scale_next));
                    aspect_ratios_layer.push_back(interpolated_scale_aspect_ratio);
                }
            }
            last_same_stride_layer++;
        }

        for (size_t i = 0; i < aspect_ratios_layer.size(); i++) {
            float ratio_sqrt = std::sqrt(aspect_ratios_layer[i]);
            anchor_heights.push_back(scales_layer[i] / ratio_sqrt);
            anchor_widths.push_back(scales_layer[i] * ratio_sqrt);
        }

        int stride = strides[layer_id];
        int feature_map_height = (int) std::ceil((float) input_size_height / stride);
        int feature_map_width = (int) std::ceil((float) input_size_width / stride);

        for (int y = 0; y < feature_map_height; y++) {
            for (int x = 0; x < feature_map_width; x++) {
                for (size_t anchor_id = 0; anchor_id < anchor_heights.size(); anchor_id++) {
                    float x_center = (x + anchor_offset_x) / feature_map_width;
                    float y_center = (y + anchor_offset_y) / feature_map_height;
                    float w, h;
                    if (fixed_anchor_size) {
                        w = 1.0f;
                        h = 1.0f;
                    } else {
                        w = anchor_widths[anchor_id];
                        h = anchor_heights[anchor_id];
                    }
                    anchors.push_back({x_center, y_center, w, h});
                }
            }
        }
        layer_id = last_same_stride_layer;
        LOG_I("loop end, layer_id: %d", layer_id);
    }
    return anchors;
}

// 绘制检测结果：在 image 上绘制 bbox 和关键点，返回绘制后的图像
cv::Mat draw_detection(const cv::Mat &image, const cv::Rect &bbox, const vector<cv::Point> &keypoints) {
    cv::Mat out_img = image.clone();
    cv::rectangle(out_img, bbox, cv::Scalar(0, 255, 0), 2);
    for (const auto &pt: keypoints) {
        cv::circle(out_img, pt, 2, cv::Scalar(0, 0, 255), -1);
    }
    return out_img;
}

// letterbox_padding: 将图像 resize 保持比例，然后填充黑边，返回填充后的图像和参数
cv::Mat letterbox_padding(const cv::Mat &image, const cv::Size &target_size, PaddingParams &params) {
    int h = image.rows, w = image.cols;
    int target_h = target_size.height, target_w = target_size.width;
    float scale = std::min((float) target_w / w, (float) target_h / h);
    int new_w = std::round(w * scale);
    int new_h = std::round(h * scale);
    cv::Mat resized;
    cv::resize(image, resized, cv::Size(new_w, new_h));

    int pad_w = (target_w - new_w) / 2;
    int pad_h = (target_h - new_h) / 2;
    int pad_right = target_w - new_w - pad_w;
    int pad_bottom = target_h - new_h - pad_h;

    params.scale = scale;
    params.pad_top = pad_h;
    params.pad_bottom = pad_bottom;
    params.pad_left = pad_w;
    params.pad_right = pad_right;
    params.original_w = w;
    params.original_h = h;
    params.padded_w = target_w;
    params.padded_h = target_h;

    cv::Mat padded;
    cv::copyMakeBorder(resized, padded, pad_h, pad_bottom, pad_w, pad_right, cv::BORDER_CONSTANT, cv::Scalar(0, 0, 0));
    return padded;
}

// 将检测框坐标从 padded 图像转换回原始图像坐标
void transform_coords_back(float x, float y, const PaddingParams &params, float &x_orig, float &y_orig) {
    float x_unpad = x - params.pad_left;
    float y_unpad = y - params.pad_top;
    x_orig = x_unpad / params.scale;
    y_orig = y_unpad / params.scale;
    x_orig = std::min(std::max(x_orig, 0.0f), (float) params.original_w);
    y_orig = std::min(std::max(y_orig, 0.0f), (float) params.original_h);
}

// 将一组关键点坐标从 padded 图像转换回原始图像坐标
vector<cv::Point> transform_keypoints(const vector<cv::Point2f> &keypoints, const PaddingParams &params) {
    vector<cv::Point> pts;
    for (const auto &kp: keypoints) {
        float x, y;
        transform_coords_back(kp.x, kp.y, params, x, y);
        pts.push_back(cv::Point(std::round(x), std::round(y)));
    }
    return pts;
}

void print_ncnn_mat(const ncnn::Mat& mat, const char* name) {
    std::string output = std::string(name) + " = [";

    for (int c = 0; c < mat.c; c++) {
        const float* ptr = mat.channel(c);
        for (int y = 0; y < mat.h; y++) {
            for (int x = 0; x < mat.w; x++) {
                output += std::to_string(ptr[y * mat.w + x]) + " ";
            }
            output +="\n";
        }
        output += ";\n";  // 频道分隔符
    }
    output += "]\n";

    LOG_D("%s", output.c_str());
}


void print_cv_mat(const cv::Mat& mat, const std::string& name = "mat") {
    LOG_D("===== %s =====", name.c_str());
    LOG_D("Size: %d x %d", mat.rows, mat.cols);
    LOG_D("Channels: %d", mat.channels());
    LOG_D("Type: %d (Depth: %d)", mat.type(), CV_MAT_DEPTH(mat.type()));

    // // 仅打印前 5 行、5 列
    // int rows = std::min(mat.rows, 5);
    // int cols = std::min(mat.cols, 5);
    //
    // for (int i = 0; i < rows; i++) {
    //     std::string row_output;
    //     for (int j = 0; j < cols; j++) {
    //         if (mat.type() == CV_8UC3) {
    //             cv::Vec3b pixel = mat.at<cv::Vec3b>(i, j);
    //             row_output += "[" + std::to_string(pixel[0]) + ", " + std::to_string(pixel[1]) + ", " + std::to_string(pixel[2]) + "]\t";
    //         } else if (mat.type() == CV_32FC3) {
    //             cv::Vec3f pixel = mat.at<cv::Vec3f>(i, j);
    //             row_output += "[" + std::to_string(pixel[0]) + ", " + std::to_string(pixel[1]) + ", " + std::to_string(pixel[2]) + "]\t";
    //         } else {
    //             row_output += "(Unsupported Type)\t";
    //         }
    //     }
    //     LOG_D("%s", row_output.c_str());
    // }
    LOG_D("=======================");
}


// ---------------------- 主函数 ----------------------

int main() {
    SetConsoleOutputCP(CP_UTF8);
    LOG_I("开始 face detection test...");

    // 文件路径配置
    string param_path = R"(D:\tmp\ncnn_pytorch\face_detector.ncnn.param)";
    string bin_path = R"(D:\tmp\ncnn_pytorch\face_detector.ncnn.bin)";
    // 选择一张图片（注意替换为你实际的图片路径）
    // string original_img_path = R"(D:\tmp\image\o\4620abc08cbc04e913e17a0c8f7f6cd3.jpeg)";
    string original_img_path = R"(D:\tmp\image\o\face_image_1080_1920.png)";

    // 保存 padded 图片到指定目录
    std::string padded_image_save_path = R"(D:\tmp\image\face_detector_ncnn_padded.png)";  // 你可以修改为所需路径

    string output_img_path = R"(D:\tmp\image\face_detector_ncnn.png)";
    string original_with_detection_output_img_path = R"(D:\tmp\image\face_detector_ncnn_with_original.png)";

    // 加载图像
    cv::Mat originalImg = cv::imread(original_img_path, cv::IMREAD_UNCHANGED);
    if (originalImg.empty()) {
        LOG_E("图片未找到: %s", original_img_path.c_str());
        return -1;
    }

    // 转换通道：如果图像有 4 通道，转换为 RGB；否则从 BGR 转换为 RGB
    if (originalImg.channels() == 4) {
        LOG_D("COLOR_BGRA2RGB");
        cv::cvtColor(originalImg, originalImg, cv::COLOR_BGRA2RGB);
    }else {
        LOG_D("COLOR_BGR2RGB");
        cv::cvtColor(originalImg, originalImg, cv::COLOR_BGR2RGB);
    }

    // 1. letterbox处理后得到 padded 图像，尺寸为 128x128，格式为 RGB
    PaddingParams padding_params{};
    cv::Mat padded = letterbox_padding(originalImg, cv::Size(128, 128), padding_params);
    print_cv_mat(padded, "padded");

    // 由于 OpenCV 的 imwrite 期望 BGR 格式，因此需要转换回 BGR 再保存
    cv::Mat padded_bgr;
    cv::cvtColor(padded, padded_bgr, cv::COLOR_RGB2BGR);

    if (cv::imwrite(padded_image_save_path, padded_bgr)) {
        LOG_D("padded 图像已保存: %s", padded_image_save_path.c_str());
    } else {
        LOG_E("保存 padded 图像失败: %s", padded_image_save_path.c_str());
    }

    // 2. 归一化到 [0,1]，转换为 CV_32FC3
    cv::Mat padded_float;
    padded.convertTo(padded_float, CV_32FC3, 1.0 / 255.0);
    print_cv_mat(padded_float, "padded_float");

    if (padded_float.empty())
    {
        LOG_E("padded_float is empty!");
        return -1;
    }
    if (padded_float.channels() != 3)
    {
        LOG_E("padded_float has wrong channels: %d", padded_float.channels());
        return -1;
    }

    // 3. 构造 NHWC 4D数据
    // padded_float 的尺寸为 (128, 128, 3)（HWC），我们需要扩展 batch 维度1
    int H = padded_float.rows;      // 128
    int W = padded_float.cols;      // 128
    int C = padded_float.channels();// 3
    int total = H * W * C;          // 128*128*3

    // 分配一个新的 float 数组保存 NHWC 数据（batch=1 与 HWC 内存完全一致）
    float* nhwc_data = new float[1 * total];
    memcpy(nhwc_data, padded_float.ptr<float>(), total * sizeof(float));

    // todo 修改 pytorch / onnx / ncnn 模型的输入
    ncnn::Mat in_mat(3, 128, 128, 1, nhwc_data);

    LOG_D("in_mat shape: d=%d, c=%d, h=%d, w=%d", in_mat.d, in_mat.c, in_mat.h, in_mat.w);
    print_ncnn_mat(in_mat, "in_mat");

    // 初始化 ncnn 网络并加载模型
    ncnn::Net net;
    LOG_I("load_param: %s", param_path.c_str());
    if (net.load_param(param_path.c_str()) != 0) {
        LOG_E("加载 param 文件失败");
        return -1;
    }
    LOG_I("load_model: %s", bin_path.c_str());
    if (net.load_model(bin_path.c_str()) != 0) {
        LOG_E("加载 bin 文件失败");
        return -1;
    }

    ncnn::Extractor ex = net.create_extractor();
    // 设置输入节点名称为 "in0"
    LOG_D("ex.input");
    ex.input("in0", in_mat);

    // 执行推理，提取输出 "out0" 和 "out1"
    LOG_D("ex.extract");
    ncnn::Mat regressors, scores;
    ex.extract("out0", regressors);
    ex.extract("out1", scores);

    LOG_D("regressors shape: w=%d, h=%d, c=%d", regressors.w, regressors.h, regressors.c);
    print_ncnn_mat(regressors, "regressors");

    LOG_D("scores shape: w=%d, h=%d, c=%d", scores.w, scores.h, scores.c);
    print_ncnn_mat(scores, "scores");

    // 假设输出尺寸：regressors: 896 x 16, scores: 896 x 1
    // 将输出转换为 std::vector<float>
    int num_regressors = regressors.w * regressors.h * regressors.c; // 896*16
    int num_scores = scores.w * scores.h * scores.c; // 896
    float* regressors_data = (float*)regressors.data;
    float* scores_data = (float*)scores.data;

    vector<float> reg_vec(regressors_data, regressors_data + num_regressors);
    vector<float> score_vec(scores_data, scores_data + num_scores);

    // 对 score_vec 执行 clip(-100,100) 并计算 sigmoid
    for (auto &s: score_vec) {
        if (s < -100.0f) s = -100.0f;
        if (s > 100.0f) s = 100.0f;
        s = 1.0f / (1.0f + std::exp(-s));
    }
    // 找到最大分数索引
    int max_index = std::distance(score_vec.begin(), std::max_element(score_vec.begin(), score_vec.end()));
    float max_score = score_vec[max_index];
    LOG_I("最大分数: %.4f, 索引: %d", max_score, max_index);

    // 从 reg_vec 中取出 best regressor（16 个数）
    assert(max_index * 16 + 16 <= (int)reg_vec.size());
    vector<float> best_regressor(reg_vec.begin() + max_index * 16, reg_vec.begin() + max_index * 16 + 16);
    float dx = best_regressor[0], dy = best_regressor[1], w_box = best_regressor[2], h_box = best_regressor[3];
    vector<float> keypoints(best_regressor.begin() + 4, best_regressor.end());

    ex.clear();
    net.clear();

    // 生成锚点
    vector<Anchor> anchors = generate_face_detection_anchors(128);
    LOG_I("生成的锚点总数: %zu", anchors.size());
    for (int i = 0; i < 5 && i < (int) anchors.size(); i++) {
        const Anchor &a = anchors[i];
        LOG_I("锚点 %d: (x=%.4f, y=%.4f, w=%.4f, h=%.4f)", i, a.x_center, a.y_center, a.w, a.h);
    }

    // 对应的 anchor
    Anchor anchor = anchors[max_index];

    // 计算边界框（以 128 为基准尺寸）
    float box_center_x = dx + anchor.x_center * 128.0f;
    float box_center_y = dy + anchor.y_center * 128.0f;
    float box_w = w_box;
    float box_h = h_box;
    float box_x = box_center_x - box_w / 2.0f;
    float box_y = box_center_y - box_h / 2.0f;
    cv::Rect bbox(cv::Point(std::round(box_x), std::round(box_y)),
                  cv::Size(std::round(box_w), std::round(box_h)));

    // 解析关键点
    vector<cv::Point2f> kps;
    for (size_t i = 0; i + 1 < keypoints.size(); i += 2) {
        float kp_dx = keypoints[i];
        float kp_dy = keypoints[i + 1];
        float kp_x = kp_dx * anchor.w + anchor.x_center * 128.0f;
        float kp_y = kp_dy * anchor.h + anchor.y_center * 128.0f;
        kps.push_back(cv::Point2f(kp_x, kp_y));
    }

    // 绘制检测结果在 padded 图像上
    cv::Mat result_img = padded.clone();
    cv::Mat result_draw = draw_detection(result_img, bbox,
                                         // 转换关键点为 cv::Point (四舍五入)
                                         vector<cv::Point>(kps.begin(), kps.end()));
    // 保存结果图像（转换为 BGR 保存）
    cv::Mat result_bgr;
    cv::cvtColor(result_draw, result_bgr, cv::COLOR_RGB2BGR);
    cv::imwrite(output_img_path, result_bgr);
    LOG_I("检测结果保存至: %s", output_img_path.c_str());

    // 将检测框和关键点转换回原始图像坐标
    auto transform_bbox = [&](const cv::Rect &bbox, const PaddingParams &params) -> cv::Rect {
        float x1_orig, y1_orig, x2_orig, y2_orig;
        transform_coords_back(bbox.x, bbox.y, params, x1_orig, y1_orig);
        transform_coords_back(bbox.x + bbox.width, bbox.y + bbox.height, params, x2_orig, y2_orig);
        int new_x = std::round(x1_orig);
        int new_y = std::round(y1_orig);
        int new_w = std::round(x2_orig - x1_orig);
        int new_h = std::round(y2_orig - y1_orig);
        return cv::Rect(new_x, new_y, new_w, new_h);
    };

    auto transform_keypoints_func = [&](const vector<cv::Point2f> &kps, const PaddingParams &params) -> vector<cv::Point> {
        return transform_keypoints(kps, params);
    };

    cv::Rect original_bbox = transform_bbox(bbox, padding_params);
    vector<cv::Point> original_keypoints = transform_keypoints_func(kps, padding_params);

    cv::Mat original_with_det = originalImg.clone();
    cv::Mat original_draw = draw_detection(original_with_det, original_bbox, original_keypoints);
    cv::Mat original_bgr;
    cv::cvtColor(original_draw, original_bgr, cv::COLOR_RGB2BGR);
    cv::imwrite(original_with_detection_output_img_path, original_bgr);
    LOG_I("原始图像检测结果保存至: %s", original_with_detection_output_img_path.c_str());

    return 0;
}
```



Log.h

```
//
// Created by one on 2024/12/10.
//

#pragma once

static const char *TAG = "LOG";

#ifdef WIN32

#include <cstdio>
#include <ctime>
#include <string>

// 时间戳获取函数
inline std::string currentDateTime() {
    char buffer[100];
    time_t now = time(nullptr);
    struct tm tstruct;
    localtime_s(&tstruct, &now); // 安全的时间格式化函数
    strftime(buffer, sizeof(buffer), "%Y-%m-%d %X", &tstruct);
    return std::string(buffer);
}

// Windows 日志宏定义
#define LOG_V(...) (printf("[VERBOSE] [%s] [%s] ", currentDateTime().c_str(), TAG), printf(__VA_ARGS__), printf("\n"))
#define LOG_D(...) (printf("[DEBUG]   [%s] [%s] ", currentDateTime().c_str(), TAG), printf(__VA_ARGS__), printf("\n"))
#define LOG_I(...) (printf("[INFO]    [%s] [%s] ", currentDateTime().c_str(), TAG), printf(__VA_ARGS__), printf("\n"))
#define LOG_W(...) (printf("[WARN]    [%s] [%s] ", currentDateTime().c_str(), TAG), printf(__VA_ARGS__), printf("\n"))
#define LOG_E(...) (printf("[ERROR]   [%s] [%s] ", currentDateTime().c_str(), TAG), printf(__VA_ARGS__), printf("\n"))

#endif


#ifdef __ANDROID__
#include <android/log.h>
#define LOG_V(...) ((void)__android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__))
#define LOG_D(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))
#define LOG_I(...) ((void)__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__))
#define LOG_W(...) ((void)__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__))
#define LOG_E(...) ((void)__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__))
#endif
```



CMakeLists.txt

```
cmake_minimum_required(VERSION 3.28)
project(ncnn_test)

set(CMAKE_CXX_STANDARD 23)

add_compile_options("/utf-8")

#set(CMAKE_CXX_FLAGS_DEBUG "/MDd")
#set(CMAKE_CXX_FLAGS_RELEASE "/MD")

# ncnn
# https:/github.com/Tencent/ncnn
set(NCNN_INSTALL_DIR D:/develop/ncnn/ncnn-20240820/ncnn-20240820-windows-vs2022)
set(NCNN_INCLUDE ${NCNN_INSTALL_DIR}/x64/include)
set(ncnn_DIR ${NCNN_INSTALL_DIR}/x64/lib/cmake/ncnn)
find_package(ncnn REQUIRED)
link_directories("${NCNN_INSTALL_DIR}/x64/lib")

#opencv
# https://opencv.org/
# https://github.com/opencv/opencv
set(OPENCV_INSTALL_DIR D:/develop/opencv/4.11.0/sdk/windows/opencv)
set(OpenCV_DIR ${OPENCV_INSTALL_DIR}/build)
find_package(OpenCV REQUIRED)

# 目标可执行文件的输出目录
set(TARGET_OUTPUT_DIR ${CMAKE_RUNTIME_OUTPUT_DIRECTORY})

include_directories(
        ${NCNN_INCLUDE}
        ${OpenCV_INCLUDE_DIRS}
)

file(GLOB_RECURSE SOURCE_FILES "${CMAKE_CURRENT_SOURCE_DIR}/code/src/*.cpp")
add_executable(${PROJECT_NAME}
        ${SOURCE_FILES}
)

target_link_libraries(
        ${PROJECT_NAME}
        ncnn
        ${OpenCV_LIBS}
)

#if(NOT TARGET_OUTPUT_DIR)
#    set(TARGET_OUTPUT_DIR ${CMAKE_BINARY_DIR})
#endif()

# 复制 OpenCV DLL 文件
add_custom_command(TARGET ${PROJECT_NAME} POST_BUILD
        COMMAND ${CMAKE_COMMAND} -E copy_if_different
        "${OpenCV_DIR}/x64/vc16/bin/opencv_world4110.dll"
        "$<TARGET_FILE_DIR:${PROJECT_NAME}>"
)
```

