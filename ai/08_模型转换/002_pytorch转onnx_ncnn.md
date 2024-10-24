# pytorch转onnx ncnn



## 1 环境配置

conda

pytorch



## 2 转化

### 1 定义模型路径

```python
model_name = "net001"
out_path = "../../../out/" + model_name
pytorch_model_path = out_path + "/pytorch/" + model_name + ".model"
pytorch_pretrained_weights_path = out_path + "/pytorch/" + model_name + ".pth"

onnx_dir = out_path + "/onnx"
onnx_model_path = onnx_dir+"/"+model_name+".onnx"

ncnn_dir = out_path + "/ncnn"
ncnn_bin = ncnn_dir + "/" + model_name + ".ncnn.bin"
ncnn_param = ncnn_dir + "/" + model_name + ".ncnn.param"
ncnn_test_py = model_name + "_ncnn_inference_test.py"

pnnx_dir = out_path + "/pnnx"
pnnx_bin = pnnx_dir + "/" + model_name + ".pnnx.bin"
pnnx_param = pnnx_dir + "/" + model_name + ".pnnx.param"
pnnx_onnx = pnnx_dir + "/" + model_name + ".pnnx.onnx"
pnnx_test_py = model_name + "_pnnx_inference_test.py"
```



### 2 转化到onnx

```python
import torch

from net001_path import onnx_model_path
from net001_path import pytorch_model_path

if __name__ == '__main__':
    model = torch.load(pytorch_model_path, weights_only=False)
    model.eval()

    dummy_input = torch.randn(1, 2)

    torch.onnx.export(model, dummy_input, onnx_model_path,
                      export_params=True,  # 是否导出参数
                      opset_version=12,  # ONNX 的 opset 版本
                      do_constant_folding=True,  # 是否执行常量折叠优化
                      input_names=['input'],  # 输入名称
                      output_names=['output'],  # 输出名称
                      dynamic_axes={'input': {0: 'batch_size'},  # 动态批量大小
                                    'output': {0: 'batch_size'}})

    print(f"Model has been exported to {onnx_model_path}")
```



### 3 转化到ncnn

```python
import pnnx
import torch

from net001_path import ncnn_bin
from net001_path import ncnn_param
from net001_path import ncnn_test_py
from net001_path import pnnx_bin
from net001_path import pnnx_onnx
from net001_path import pnnx_param
from net001_path import pnnx_test_py
from net001_path import pytorch_model_path

if __name__ == '__main__':
    model = torch.load(pytorch_model_path, weights_only=False)
    model.eval()

    # 创建一个输入示例
    dummy_input = torch.randn(1, 2)  # 根据您的模型输入大小调整

    opt_model = pnnx.export(model, pytorch_model_path, dummy_input,
                            ncnnbin=ncnn_bin, ncnnparam=ncnn_param, ncnnpy=ncnn_test_py,
                            pnnxbin=pnnx_bin, pnnxparam=pnnx_param, pnnxonnx=pnnx_onnx, pnnxpy=pnnx_test_py)
```



### pnnx

https://github.com/pnnx/pnnx/

优化导出pytorch模型

```python
import torch
import torchvision.models as models
import pnnx

model = models.resnet18(pretrained=True)
x = torch.rand(1, 3, 224, 224)
opt_model = pnnx.export(model, "resnet18.pt", x)
```



将 torchscript / onnx 转成 python 代码定义的Model类



onnx转ncnn

```python
pnnx.convert('resnet18.onnx', x)
```

具体见 https://github.com/pnnx/pnnx

