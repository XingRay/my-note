# 从hf-mirror下载模型



huggingface地址

https://huggingface.co/



hf-mirror镜像站地址

https://hf-mirror.com/

备用镜像站地址：

https://alpha.hf-mirror.com



## 1 安装python

https://www.python.org/

下载最新版本python，安装即可， 当前使用的版本是

windows：

https://www.python.org/ftp/python/3.13.7/python-3.13.7-amd64.exe



## 2 安装 huggingface下载工具

```shell
pip install -U huggingface_hub
```



## 3 配置huggingface下载工具使用的下载地址

windows：

临时设置

```shell
$env:HF_ENDPOINT = "https://hf-mirror.com"
```

永久设置：

创建系统环境变量 `HF_ENDPOINT` 取值为 `https://hf-mirror.com` 

验证：

```shell
PS E:\develop\huggingface> $env:HF_ENDPOINT
https://hf-mirror.com
```

也可以使用备用镜像站地址：

```shell
PS E:\develop\huggingface> $env:HF_ENDPOINT = "https://alpha.hf-mirror.com"
PS E:\develop\huggingface> $env:HF_ENDPOINT
https://alpha.hf-mirror.com
```



## 4 下载模型

在指定目录执行 

```shell
hf download username/repo-name --local-dir LOCAL_DIR
```

如：

```shell
hf download comfyanonymous/flux_text_encoders --local-dir comfyanonymous/flux_text_encoders
```

更多使用方法参考指令：

```shell
hf download -h
```

