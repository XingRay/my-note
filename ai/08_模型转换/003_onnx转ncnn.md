# onnx模型转ncnn

windows平台使用pnnx工具进行转换



1 下载 pnnx 工具

https://github.com/pnnx/pnnx
当前最新版本:
https://github.com/pnnx/pnnx/releases/download/20241223/pnnx-20241223-windows.zip
下载解压后添加到系统path即可, 测试:

```
PS D:\tmp\model\ncnn> pnnx
Usage: pnnx [model.pt] [(key=value)...]
  pnnxparam=model.pnnx.param
  pnnxbin=model.pnnx.bin
  pnnxpy=model_pnnx.py
  pnnxonnx=model.pnnx.onnx
  ncnnparam=model.ncnn.param
  ncnnbin=model.ncnn.bin
  ncnnpy=model_ncnn.py
  fp16=1
  optlevel=2
  device=cpu/gpu
  inputshape=[1,3,224,224],...
  inputshape2=[1,3,320,320],...
  customop=C:\Users\nihui\AppData\Local\torch_extensions\torch_extensions\Cache\fused\fused.dll,...
  moduleop=models.common.Focus,models.yolo.Detect,...
Sample usage: pnnx mobilenet_v2.pt inputshape=[1,3,224,224]
              pnnx yolov5s.pt inputshape=[1,3,640,640]f32 inputshape2=[1,3,320,320]f32 device=gpu moduleop=models.common.Focus,models.yolo.Detect
```



在powershell中如果使用pnnx输出乱码要注意要切换编码:

```
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
```

