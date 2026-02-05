# 用mediepipe的模型(tflite)转onnx再转TRT使用



mediapipe的模型有很多都是tflite格式的，精度和模型尺寸都不错。

做个实验分二篇：

第一篇：介绍用tf2onnx然后修改onnx文件中转TRT不支持的op操作，最后转成engine文件。

第二篇：结合tensorrtX的github，介绍修改tflite文件从中间输出，然后一步步用TensorRT的API搭网络，一层层输出与tflite的中间层的输出对比。

github.com/google/mediapipe

![img](assets/002/v2-77180f0b98c6edc8d8a225aaa8501525_180x120.jpg)



https://github.com/google/mediapipe

![img](assets/002/v2-bdbb16cff4419cbaddc9c3f0c64509bd_720w.webp)

以下选用Hands检测项目，具体tflite的目录如下：

[google/mediapipegithub.com/google/mediapipe/tree/master/mediapipe/modules/palm_detection



![img](assets/002/v2-84d15591bb33292e08eead12a0d6621e_180x120.jpg)



https://github.com/google/mediapipe/tree/master/mediapipe/modules/palm_detection



第一步：从tflite转onnx，这个有现成的参考，注意--opset的参数设置

github.com/onnx/tensorflow-onnx.git



![img](assets/002/v2-a1b36a8c6662ccc1bee5a5b972af5e27_180x120.jpg)



https://github.com/onnx/tensorflow-onnx.git



![img](assets/002/v2-dbea348408a9c397fb525e2ecc301b7d_720w.png)

然后尝试直接转engine（当然肯定会报错）

![img](assets/002/v2-4772da01f29e9b32e4b651fc9d6320e3_720w.webp)

主要的问题就是Pad，我的理解是TensorRT的Pad只支持在W和H这二个维度上，打开onnx查看发现，我们需要在C的维度上Pad

![img](assets/002/v2-0de94b65860e0d7a358dd22efcd4682e_720w.webp)

开始对onnx的修改

首先打开onnx然后找找一共有几个Pad需要修改，

![img](assets/002/v2-1efaf96555efb1fd7cc91cebbc14882a_720w.webp)

一共就三处需要修改

![img](assets/002/v2-1e6ca522c04de783aff75c2f5414fa79_720w.webp)

核心思想也很简单

1）删除原来Pad的op

![img](assets/002/v2-aa409f71b058ec8105f1a558aa2587b0_720w.png)

2）添加新的Transpose的op，先把[B，C，W，H]转成[B，W，H，C]，另外二个节点类似

![img](assets/002/v2-48a598d09b4994e88b30e60d5fb51bad_720w.webp)

注意：此处的插入位置32必须要小于Add的op的号，否则在check_model时会报错

3）然后添加新的Pad的op，对最后一个维度进行pad，另外二个节点类似

![img](assets/002/v2-973b897f4b94027e207b44bc6b58f187_720w.webp)

4）最后添加新的Transpose的op，把[B，W，H，C]转回[B，C，W，H]，另外二个节点类似

![img](assets/002/v2-c09929c53c8162ec3ca83de261e8155f_720w.webp)

转换之后与之前的对比（展示第三个Pad的onnx图）

![img](assets/002/v2-996f0d2ffb9c300fcd29a76272e5d823_720w.webp)

最终转换成功，并且check_model也没有报错

![img](assets/002/v2-ddb59901519f125cd8904ba753176cd7_720w.webp)

然后用onnx-simplifier一下

github.com/daquexian/onnx-simplifier.git



![img](assets/002/v2-f11d4d64fe853b08e98485a25080c1ce_180x120.jpg)



https://github.com/daquexian/onnx-simplifier.git)

![img](assets/002/v2-25cfbd9db6c36c2cd60b9ae6a834f6ac_720w.png)

最后转TRT的engine文件，没有其他报错。

![img](assets/002/v2-2de8f3f038ede4fc6010b0daf729222f_720w.webp)



