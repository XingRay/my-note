# Windows 下 Pytorch GPU版本安装



注意:不要直接根据官方网站首页: https://pytorch.org/get-started/locally/ 的说明安装, 这样安装不支持 cuda

要根据这个页面  https://pytorch.org/get-started/previous-versions/ 的提示安装, 如:



## INSTALLING PREVIOUS VERSIONS OF PYTORCH

We’d prefer you install the [latest version](https://pytorch.org/get-started/locally), but old binaries and installation instructions are provided below for your convenience.

## COMMANDS FOR VERSIONS >= 1.0.0

### v2.2.1

#### Conda

##### OSX

```
# conda
conda install pytorch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 -c pytorch
```

##### Linux and Windows

```
# CUDA 11.8
conda install pytorch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 pytorch-cuda=11.8 -c pytorch -c nvidia
# CUDA 12.1
conda install pytorch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 pytorch-cuda=12.1 -c pytorch -c nvidia
# CPU Only
conda install pytorch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 cpuonly -c pytorch
```

#### Wheel

##### OSX

```
pip install torch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1
```

##### Linux and Windows

```
# ROCM 5.7 (Linux only)
pip install torch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 --index-url https://download.pytorch.org/whl/rocm5.7
# CUDA 11.8
pip install torch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 --index-url https://download.pytorch.org/whl/cu118
# CUDA 12.1
pip install torch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 --index-url https://download.pytorch.org/whl/cu121
# CPU only
pip install torch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 --index-url https://download.pytorch.org/whl/cpu
```





那么在 windows 11 CUDA为12.4 的环境下使用下面的执行安装即可:

```
conda install pytorch==2.2.1 torchvision==0.17.1 torchaudio==2.2.1 pytorch-cuda=12.1 -c pytorch -c nvidia
```



