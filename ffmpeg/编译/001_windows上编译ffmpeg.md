# windows上编译ffmpeg



安装 msys2

https://www.msys2.org/





配置镜像 略



配置 msvc

修改 msys2\msys2_shell.cmd

```shell
@echo off
setlocal EnableDelayedExpansion
call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"
set "WD=%__CD__%"
if NOT EXIST "%WD%msys-2.0.dll" set "WD=%~dp0usr\bin\"
set "LOGINSHELL=bash"
set /a msys2_shiftCounter=0

rem To activate windows native symlinks uncomment next line
rem set MSYS=winsymlinks:nativestrict

rem Set debugging program for errors
rem set MSYS=error_start:%WD%../../mingw64/bin/qtcreator.exe^|-debug^|^<process-id^>

rem To export full current PATH from environment into MSYS2 use '-use-full-path' parameter
rem or uncomment next line
set MSYS2_PATH_TYPE=inherit
```

增加

执行 msvc 的预处理

```shell
call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"
```

解除注释

继承系统环境

```shell
set MSYS2_PATH_TYPE=inherit
```



启动

```shell
.\msys2_shell.cmd -mingw64
```



安装工具

安装MinGW-w64编译工具

```shell
pacman -S mingw-w64-x86_64-toolchain
```

输入命令后按下回车，会出现安装选项，**再次按下回车**即可，然后输入**Y**进行安装，最后等待安装完成即可

```shell
pacman -S git make nasm yasm diffutils automake autoconf perl libtool mingw-w64-i686-cmake pkg-config mingw-w64-x86_64-SDL2
```



准备

```
cd msys2/usr/bin/
mv link.exe link.bak
```



安装

```
pacman -S nasm yasm
```



配置 MSVC

```
C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Tools\MSVC\14.41.34120\bin\Hostx64\x64
```







进入ffmpeg源码:

执行配置:

```shell
./configure --enable-shared --enable-postproc --enable-gpl --toolchain=msvc --disable-debug --disable-static --disable-doc --disable-ffmpeg --disable-ffplay --disable-ffprobe --disable-symver --disable-stripping --prefix=../build
```



编译 x264

```shell
git clone http://git.videolan.org/git/x264.git
cd x264
```



配置

如需生成dll库，需要加上  --enable-shared

```
./configure --prefix=/home/leixing/code/ffmpeg/build/libx264 --host=x86_64-w64-mingw32 --enable-static --extra-ldflags=-Wl,--output-def=libx264.def
```

构建

使用所有核心的线程

```
make -j
```

使用8线程

```
make -j8
```

安装

```
make install
```



执行完如上命令后是没有生成libx264.lib的，需要手动生成；

首先执行完 configure 命令后，会生成 libx264.def 文件，将其拷贝到   （修改自己的路径）

```
/home/leixing/code/ffmpeg/build/libx264/lib
```



然后进入该路径，执行命令 

```
lib /machine:x64 /def:libx264.def
```

 即可，如下：

```
cp libx264.def /home/leixing/code/ffmpeg/build/libx264/lib/
cd /home/leixing/code/ffmpeg/build/libx264/lib/
```

生成64位lib使用

```
lib /machine:x64 /def:libx264.def
```

生成32位lib使用

```
lib /machine:i386 /def:libx264.def
```



下载和编译 fdk-aac
在ffmpeg文件下，输入命令下载和编译fdk-aac

```
git clone git@github.com:mstorsjo/fdk-aac.git
```

```
cd fdk-aac
./autogen.sh
./configure --prefix=/home/leixing/code/ffmpeg/build/libfdk-aac --enable-static --enable-shared
make -j4
make install
```

有问题的话，可以 make clean 后继续





下载和编译 mp3
在ffmpeg文件下，输入命令下载和编译md3

下载并解压

https://lame.sourceforge.io/index.php

https://sourceforge.net/projects/lame/

```
cd lame
./configure --prefix=/home/leixing/code/ffmpeg/build/libmp3lame --disable-shared --disable-frontend --enable-static
make -j
make install
```



下载和编译 libvpx
在ffmpeg文件下，输入命令下载libvpx

```
git clone git@github.com:webmproject/libvpx.git

cd libvpx
./configure --prefix=/home/leixing/code/ffmpeg/build/libvpx --disable-examples --disable-unit-tests --enable-vp9-highbitdepth --as=yasm
make -j
make install
```

有问题的话，可以 

```
make clean
```

 后继续

导出到 pgk-config

```
export PKG_CONFIG_PATH=/home/leixing/code/ffmpeg/build/libx264/lib/pkgconfig:$PKG_CONFIG_PATH
```





进入ffmpeg源码目录, 执行

```
./configure --prefix=/home/leixing/code/ffmpeg/build/ffmpeg --arch=x86_64 --enable-shared --enable-gpl --enable-libfdk-aac --enable-nonfree --enable-libvpx --enable-libx264 --enable-libmp3lame --extra-cflags="-I/home/leixing/code/ffmpeg/build/libfdk-aac/include" --extra-ldflags="-L/home/leixing/code/ffmpeg/build/libfdk-aac/lib" --extra-cflags="-I/home/leixing/code/ffmpeg/build/libvpx/include" --extra-ldflags="-L/home/leixing/code/ffmpeg/build/libvpx/lib" --extra-cflags="-I/home/leixing/code/ffmpeg/build/libx264/include" --extra-ldflags="-L/home/leixing/code/ffmpeg/build/libx264/lib" --extra-cflags="-I/home/leixing/code/ffmpeg/build/libmp3lame/include" --extra-ldflags="-L/home/leixing/code/ffmpeg/build/libmp3lame/lib"
```



```
make -j
```

```
make install
```





### 测试

手动进入路径 cd /home/17634/ffmpeg/build/ffmpeg-4.2/bin/ 

手动拷贝一个视频到此路径中，然后使用 ffplay.exe 进行播放测试即可

### 添加其他必要的dll库

双击 ffmpeg.exe 或者 ffplay.exe 或者 ffprobe.exe ，会出现异常报错提醒

然后下载一个 Everything 应用程序，全局搜索缺少的库 libfdk-aac-2.dll

拷贝我们自己编译的，将其拷贝到build/ffmpeg-4.2/bin/ 路径下；

然后点击确定，如果还有报错，则根据报错提示，继续查找相应的dll库，继续拷贝到build/ffmpeg-4.2/bin/ 路径下，直到双击 ffmpeg.exe 或者 ffplay.exe 或者 ffprobe.exe 任一程序不再有报错为止。

注意：一般选择我们自行编译的库和msys64\mingw64\bin路径的库！





代码测试:

CMake

```
#手动引入 ffmpeg
set(ffmpeg_ROOT "D:\\develop\\cpp\\ffmpeg\\7.1\\build\\ffmpeg")
set(ffmpeg_INCLUDE_DIRS "${ffmpeg_ROOT}\\include")
set(ffmpeg_LIBRARIES
        "${ffmpeg_ROOT}\\bin\\avformat.lib"
        "${ffmpeg_ROOT}\\bin\\avcodec.lib"
        "${ffmpeg_ROOT}\\bin\\avdevice.lib"
        "${ffmpeg_ROOT}\\bin\\avfilter.lib"
        "${ffmpeg_ROOT}\\bin\\avutil.lib"
        "${ffmpeg_ROOT}\\bin\\swresample.lib"
        "${ffmpeg_ROOT}\\bin\\swscale.lib"
)
```

