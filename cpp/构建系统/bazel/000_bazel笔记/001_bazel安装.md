# bazel安装



## 在windows上安装

在bazel页面

https://github.com/bazelbuild/bazel/releases
下载 bazel 可执行文件,如:
https://github.com/bazelbuild/bazel/releases/download/8.0.1/bazel-8.0.1-windows-x86_64.exe
添加到系统 PATH 即可



## 在ubuntu上安装

https://github.com/bazelbuild/bazel/releases
下载指定版本的可执行文件, 如:
https://github.com/bazelbuild/bazel/releases/download/6.5.0/bazel-6.5.0-linux-x86_64

保存到指定目录, 并改名为 bazel, 如:

```
~/develop/bazel/6.5.0/bazel
```

导出到path:

修改 bash配置:

```
nano ~/.bashrc
```

添加

```shell
export PATH=~/develop/bazel/6.5.0:$PATH
```

保存后退出, 使修改生效

```
source ~/.bashrc
```

测试:

```
bazel --version
```

输出:

```
bazel 6.5.0
```

