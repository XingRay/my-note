# Windows上编译Mediapipe

下载安装msys2

https://www.msys2.org/

当前最新版本

https://github.com/msys2/msys2-installer/releases/download/2024-07-27/msys2-x86_64-20240727.exe

安装到指定目录, 如:

```
D:\develop\msys2
```

将该目录下的 usr/bin 添加到系统PATH

使用管理员打开powershell

```
setx MSYS2_HOME "D:\develop\msys2"
```

添加到PATH

```
add-path %MSYS2_HOME%\usr\bin
```

add-path 是自定义的 powershell 函数, 见 001_powershell编辑环境变量.md





下载安装 Bazel

https://github.com/bazelbuild/bazel/releases

使用的版本要参考 /.bazelversion 文件,当前使用的版本为 6.5.0 

下载 https://github.com/bazelbuild/bazel/releases/download/6.5.0/bazel-6.5.0-windows-x86_64.exe

解压到指定目录

```
D:\develop\bazel\6.5.0
```

并添加该目录到PATH

```
setx BAZEL_HOME "D:\develop\bazel\6.5.0"
add-path %BAZEL_HOME%
```

设置 Bazel bash 路径

```
setx BAZEL_SH %MSYS2_HOME%\usr\bin\bash.exe
```



启动 msys2/mingw64

```
cmd /c D:\develop\msys2\msys2_shell.cmd -mingw64
```

 

升级  msys2 包数据库

```
pacman -Syu
```

可能会需要重启系统





升级msys2基础数据库

```
pacman -Su
```



下载安装必要的包：unzip

```
pacman -S git patch unzip
```



安装 tensorflow

```
pip install tensorflow
```





设置环境变量

你可以按照以下步骤查找路径并设置环境变量：

### 1. 查找 Visual Studio 路径

如果你安装的是完整的 Visual Studio（例如 `Visual Studio 2022`），你可以使用类似以下路径：

`C:\Program Files\Microsoft Visual Studio\2022\Community`

`C:\Program Files\Microsoft Visual Studio\2022\Professional`

`C:\Program Files\Microsoft Visual Studio\2022\Enterprise`

进入到 Visual Studio 安装目录后，找到 `VC` 文件夹，并确认版本信息。



### 2 查找 VC 和 Windows SDK 版本

**VC 路径**: 一般在 `VC\Tools\MSVC` 目录下，例如：

```
makefile


复制代码
C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Tools\MSVC\14.41.34120
```

其中 `14.29.30133` 就是你需要设置的 `BAZEL_VC_FULL_VERSION`。

**Windows SDK 路径**: 通常 Windows SDK 安装在 `C:\Program Files (x86)\Windows Kits\10`，在 `Lib` 文件夹中查看版本号，例如：

```
vbnet


复制代码
C:\Program Files (x86)\Windows Kits\10\Lib\10.0.26100.0
```

其中 `10.0.19041.0` 就是你需要设置的 `BAZEL_WINSDK_FULL_VERSION`。

### 3 设置环境变量

根据上述路径设置环境变量。例如，如果你安装的是 `Visual Studio 2022 Community`，可以如下设置：

```
set BAZEL_VS=C:\Program Files\Microsoft Visual Studio\2022\Community
set BAZEL_VC=C:\Program Files\Microsoft Visual Studio\2022\Community\VC
set BAZEL_VC_FULL_VERSION=14.41.34120
set BAZEL_WINSDK_FULL_VERSION=10.0.26100.0
```

如果你不确定路径，可以在 Visual Studio Installer 中查看你安装的组件，确保 `MSVC` 和 `Windows SDK` 都已安装。



```
set BAZEL_VS=C:\Program Files\Microsoft Visual Studio\2022\Community
set BAZEL_VC=C:\Program Files\Microsoft Visual Studio\2022\Community\VC
set BAZEL_VC_FULL_VERSION=14.41.34120
set BAZEL_WINSDK_FULL_VERSION=10.0.26100.0
```



```
set BAZEL_SH=D:\develop\msys2\usr\bin
```



切换conda的python版本

```
conda install python=3.11
```



```
HERMETIC_PYTHON_VERSION=3.11
```

安装yarn

```
npm install --global yarn
```



设置代理

cmd

```
set HTTP_PROXY=http://127.0.0.1:10809
set HTTPS_PROXY=http://127.0.0.1:10809
```



powershell

```
$env:HTTP_PROXY = "http://127.0.0.1:10809"
$env:HTTPS_PROXY = "http://127.0.0.1:10809"
```

测试

```
curl -X GET "https://www.google.com"
```



启动编译, 并且给 bazel 配置https代理

```
bazel --host_jvm_args "-DhttpsProxyHost=127.0.0.1 -DsocksProxyPort=10809" build -c opt --define MEDIAPIPE_DISABLE_GPU=1 --action_env PYTHON_BIN_PATH="D:\\develop\\python\\3.11.9\\python.exe" mediapipe/examples/desktop/hello_world
```



```
bazel build -c opt --define MEDIAPIPE_DISABLE_GPU=1 --action_env PYTHON_BIN_PATH="D:\\softwares\\anaconda\\python.exe" mediapipe/examples/desktop/hello_world
```



报错:

修改 C:\Users\leixing\_bazel_leixing\fvnevgus\external\local_execution_config_python\BUILD

```
py_runtime(
    name = "py2_runtime",
    interpreter_path = "D:/softwares/anaconda/python.exe",
    python_version = "PY2",
)

py_runtime(
    name = "py3_runtime",
    interpreter_path = "D:/softwares/anaconda/python.exe",
    python_version = "PY3",
)
```



```
...  Access is denied
```

设置:

```
setx BAZEL_SH %MSYS2_HOME%\usr\bin\bash.exe
```

验证:

```
$env:BAZEL_SH
```

```
D:\develop\msys2\usr\bin\bash.exe
```

```
PS D:\code\tmp\mediapipe> bash

leixing@DESKTOP-L20TJIM  /d/code/tmp/mediapipe
$ exit
exit
PS D:\code\tmp\mediapipe>
```





执行编译:

```shell
bazel build -c opt --define MEDIAPIPE_DISABLE_GPU=1 --action_env PYTHON_BIN_PATH="D:/softwares/anaconda/python.exe" --repo_env=HERMETIC_PYTHON_VERSION=3.11 mediapipe/examples/desktop/hello_world
```



执行:

```
set GLOG_logtostderr=1
bazel-bin\mediapipe\examples\desktop\hello_world\hello_world.exe
```



清除缓存

```
bazel clean --expunge
```

指定缓存目录

```
bazel build --repository_cache="D:\\develop\\bazel\\cache" -c opt --define MEDIAPIPE_DISABLE_GPU=1 --action_env PYTHON_BIN_PATH="D:/softwares/anaconda/python.exe" --repo_env=HERMETIC_PYTHON_VERSION=3.11 --verbose_failures mediapipe/examples/desktop/hello_world
```





face_mesh:face_mesh_cpu:

错误:

```
ERROR: C:/users/leixing/_bazel_leixing/fvnevgus/external/local_execution_config_python/BUILD:16:11: in py_runtime rule @@local_execution_config_python//:py3_runtime:
Traceback (most recent call last):
        File "/virtual_builtins_bzl/common/python/py_runtime_rule.bzl", line 40, column 17, in _py_runtime_impl
Error in fail: interpreter_path must be an absolute path
```



报错:

```
external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(361): error C4430: 缺少类型说明符 - 假定为 int。注意: C++ 不支持默认 int external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(361): error C2143: 语法错误: 缺少“,”(在“*”的 前面) external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(368): error C2065: “depth”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(368): error C2065: “rank”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(369): error C2065: “output_shape”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(369): error C2065: “depth”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(370): error C2065: “output”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(370): error C2065: “init”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(371): error C2065: “Op”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(371): error C2065: “input”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(371): error C2065: “window_shape”: 未声明的标识符 external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(371): error C2065: “window_reduce_strides”: 未声明的标识符 
```

原因:

```
C:/users/leixing/_bazel_leixing/fvnevgus/execroot/mediapipe/external/org_tensorflow/tensorflow/lite/kernels/stablehlo_reduce_window.cc(361)
```

这些注释删除就可以正常编译

```
// Recursively computes strided reductions using a sliding window over the
// given tensor.
//
// The window is defined using a shape and a dilation. The shape defines the
// elements that the window will let the reduction see. The dilation defines
// the step between window elements.
//
// For instance: the following window has a [2, 2] shape and [2, 3] dilations.
//
// 3
// ┌────┐
// ┌─┐ ┌─┐
// │X│X X│X│┐
// └─┘ └─┘│2
// X X X X ┘
// ┌─┐ ┌─┐
// │X│X X│X│
// └─┘ └─┘
```

由于编码问题导致,删除后重新编译即可, 也可以通过指定字符编码 

```
--cxxopt="/utf-8"
```

解决, 完整指令:

```
bazel build -c opt --cxxopt="/utf-8" //path/to:target
```





编译  multichannel-audio-tools 

bazel版本 3.7.2, 最高支持vs2019 安装路径 C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC\Tools\MSVC\14.29.30133

临时修改bazel环境变量参数:

```
$env:BAZEL_VS="C:\Program Files (x86)\Microsoft Visual Studio\2019\Community"
$env:BAZEL_VC="C:\Program Files (x86)\Microsoft Visual Studio\2019\Community\VC"
$env:BAZEL_VC_FULL_VERSION="14.29.30133"
```

临时指定bazel的版本:

```
$env:PATH = "D:\develop\bazel\3.7.2;$env:PATH"
```

编译

```
bazel build -c opt --cxxopt="/wd4018" --cxxopt="/std:c++11" //...
```

出现编译选项报错直接修改 bzl 文件中的编译选项,删除即可

```
C:\Users\leixing\_bazel_leixing\oognx6g4\external\com_github_glog_glog\bazel\glog.bzl
```



```
#include <unistd.h>
```

替换为

```
#ifdef _WIN32
#include <io.h>
#define access _access

#else
#include <unistd.h>
#endif
```



编译 task



```

```



编译报错:

```
C:\users\leixing\_bazel_leixing\xaydh3su\execroot\mediapipe\mediapipe\tasks\cc\core\task_api_factory.h
```

修改源码:

```
MP_ASSIGN_OR_RETURN(
        auto runner,
#if !MEDIAPIPE_DISABLE_GPU
        core::TaskRunner::Create(std::move(graph_config), std::move(resolver),
                                 std::move(packets_callback),
                                 std::move(default_executor),
                                 std::move(input_side_packets),
                                 /*resources=*/nullptr, std::move(error_fn)));
#else
        core::TaskRunner::Create(
            std::move(graph_config), std::move(resolver),
            std::move(packets_callback), std::move(default_executor),
            std::move(input_side_packets), std::move(error_fn)));
#endif
```

修改为:

```
#if !MEDIAPIPE_DISABLE_GPU
    MP_ASSIGN_OR_RETURN(
            auto runner,
      core::TaskRunner::Create(std::move(graph_config), std::move(resolver),
                               std::move(packets_callback),
                               std::move(default_executor),
                               std::move(input_side_packets),
                               /*resources=*/nullptr, std::move(error_fn)));
#else
    MP_ASSIGN_OR_RETURN(
            auto runner,
        core::TaskRunner::Create(
            std::move(graph_config), std::move(resolver),
            std::move(packets_callback), std::move(default_executor),
            std::move(input_side_packets), std::move(error_fn)));
#endif
```



1 windows上软链接问题, 
解决方案 开启开发者选项
但是会产生很多链接,希望能关闭



2 python版本脚本问题

总是报错, 解决方案: 手动修改 BUILD 文件

```
D:\code\git\github\mediapipe\bazel_out\uwisdqlp\external\local_execution_config_python\BUILD
```

```
py_runtime(
    name = "py2_runtime",
    interpreter_path = "D:/develop/python/3.12.7/python.exe",
    python_version = "PY2",
)

py_runtime(
    name = "py3_runtime",
    interpreter_path = "D:/develop/python/3.12.7/python.exe",
    python_version = "PY3",
)
```

希望能找到根本解决的办法



参考:

https://ai.google.dev/edge/mediapipe/framework/getting_started/install?hl=zh-cn#installing_on_windows

https://blog.csdn.net/userhu2012/article/details/127697196

https://blog.csdn.net/warcarlyp/article/details/140220672

https://blog.csdn.net/pingchangxin_6/article/details/125634925

https://stubbornhuang.blog.csdn.net/article/details/119546019

https://blog.csdn.net/Jay_Xio/article/details/122853641

https://blog.csdn.net/qq_43469254/article/details/128733683
