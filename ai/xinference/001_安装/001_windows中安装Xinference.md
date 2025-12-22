# Windows中安装Xinference

```shell
conda create --name xinference python=3.10
conda activate xinference 

# 不能安装太新的版本
# https://inference.readthedocs.io/zh-cn/latest/getting_started/troubleshooting.html#incompatibility-between-nvidia-driver-and-pytorch-version
# 安装的 CUDA 版本不要小于 11.8，最好版本在 11.8 到 12.1之间
pip install torch==2.5.1 torchvision==0.20.1 torchaudio==2.5.1 --index-url https://download.pytorch.org/whl/cu121

#或者一次安装所有的推理后端引擎
pip install "xinference[all]"
```



仅本机访问

```shell
xinference-local
```

开放访问

```shell
xinference-local -H 0.0.0.0
```



测试：

http://127.0.0.1:9997/ui/#/launch_model/rerank

```shell
xinference launch --model-name Qwen3-Reranker-8B --model-type rerank --model-engine llama.cpp --model-format ggufv2 --quantization Q5_K_M --replica 1 --n-gpu auto
```

