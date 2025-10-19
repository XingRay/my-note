# fluxgym安装和使用

下载安装conda

启用conda环境

```
conda create -n fluxgym python=3.10
conda activate fluxgym
```

python版本选择3.10.6 

https://github.com/kohya-ss/sd-scripts?tab=readme-ov-file#windows-required-dependencies



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



下载模型， 注意不要加 `--local-dir` 参数

```shell
hf download multimodalart/Florence-2-large-no-flash-attn
```

会保存到用户目录 `.cache`目录下



安装指定版本的 transformers 

```shell
pip uninstall -y transformers
pip install transformers==4.49.0
```

自动安装的版本太新会导致报错：

```shell
AttributeError: 'Florence2ForConditionalGeneration' object has no attribute '_supports_sdpa
```

https://github.com/cocktailpeanut/fluxgym/issues/464



启动：

```
python app.py
```
