# fluxgym安装和使用

下载安装conda

启用conda环境

```
conda create -n fluxgym python=3.10
conda activate fluxgym
```

下载源码

```
git clone git@github.com:cocktailpeanut/fluxgym.git
cd fluxgym
```



安装pytorch

```
pip3 install torch torchvision --index-url https://download.pytorch.org/whl/cu129
```

```
pip install -U bitsandbytes
```

安装onnx-runtime （可选）

```
pip install onnxruntime-gpu
```



安装依赖

```shell
cd sd-scripts
pip install -r requirements.txt

cd ..
pip install -r requirements.txt
```



下载模型

```
hf download multimodalart/Florence-2-large-no-flash-attn --local-dir multimodalart/Florence-2-large-no-flash-attn         
```



启动：

```
python app.py
```



报错修复：

```
AttributeError: 'Florence2ForConditionalGeneration' object has no attribute '_supports_sdpa
```

```shell
pip show transformers
pip uninstall -y transformers
pip install transformers==4.51.3
```



