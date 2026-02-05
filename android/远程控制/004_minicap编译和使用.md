# minicap编译和使用


minicap是一个截屏并实时传输的工具。

minicap技术特点：实时截屏；通过socket通信传送截屏数据。

利用该工具可以在电脑上实时查看安卓机器上面的画面操作。

一.源码下载
minicap源码：https://github.com/openstf/minicap
二.用NDK编译源码
1.需要先配置好NDK的环境变量。在cmd窗口中输入 ndk-build -version，测试ndk环境是否安装好。

输出一下信息表示NDK环境已经装好：

PS F:\Github\minicap-master> ndk-build -version
GNU Make 3.81
Copyright (C) 2006  Free Software Foundation, Inc.
This is free software; see the source for copying conditions.
There is NO warranty; not even for MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.

This program built for i686-w64-mingw32
2.编译指令
在minicap根目录下执行：ndk-build.cmd APP_PLATFORM=android-28 PLATFORM_SDK_VERSION=28

3.编译报错：
1.不支持gnustl_static，改成c++_static
修改minicap-master\jni\Application.mk文件 :

#APP_STL := gnustl_static
APP_STL := c++_static
2.GCC不在支持NDK_TOOLCHAIN_VERSION := 4.9,去掉这一行
修改minicap-master\jni\Application.mk文件 :

#NDK_TOOLCHAIN_VERSION := 4.9
修改后编译通过。

三.编译生成的文件：
编译生成可执行文件和so文件。要主要so文件需要对应不同的CPU的abi类型

可执行文件生成目录：
\minicap-master\libs\arm64-v8a\minicap

so文件一定用下面的目录中的so：
minicap-master\jni\minicap-shared\aosp\libs\android-28\arm64-v8a\minicap.so

我的机器CPU是arm64-v8a架构的，一定要用这个目录里面的so，否则因为跟系统不匹配导致运行报错。

四.拷贝到安卓目录中：
adb push F:\Github\minicap-master\libs\arm64-v8a\minicap /data/local/tmp/
adb push F:\Github\minicap-master\jni\minicap-shared\aosp\libs\android-28\arm64-v8a\minicap.so /data/local/tmp/
需要修改minicap 和minicap.so为777的权限：

adb shell chmod 777 /data/local/tmp/minicap*
五.adb命令运行minicap
注意屏幕的尺寸：

//start minicap
adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1920x1080@1920x1080/0
此时minicap安卓端的程序已经运行起来了，并且开启了一个socket服务器。

六.本地的端口映射到minicap工具上,端口随意
新开一个cmd窗口

//local port
adb forward tcp:1717 localabstract:minicap
七.运行nodejs
minicap源码目录中提供了一个example的nodejs程序，可以运行起来查看截图的效果。

node F:\Github\minicap-master\example\example\app.js
运行之前需要安装安卓NodeJs环境：

官网下载软件安装：https://nodejs.org/en/
安装Module
npm install -g ws
npm install -g express
运行node app.js时可能还会报错找不到Module ws，执行一下操作尝试解决：
npm cache clean -f
npm i
npm audit fix
npm audit fix --force
在新的cmd窗口中执行：

node F:\Github\minicap-master\example\example\app.js
八.打开浏览器
http://localhost:9002/

总结：
使用的命令：

//使用adb命令查看CPU版本架构信息
//CPU Version
adb shell cat /proc/cpuinfo
adb shell cat /system/build.prop
adb shell getprop ro.product.cpu.abi

//OS Version
adb shell getprop ro.build.version.sdk

//Display size
adb shell wm size

adb push F:\Github\minicap-master\libs\arm64-v8a\minicap /data/local/tmp/
adb push F:\Github\minicap-master\jni\minicap-shared\aosp\libs\android-28\arm64-v8a\minicap.so /data/local/tmp/

//chmod
adb shell chmod 777 /data/local/tmp/minicap*

//test minicap is usefull or not
adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1920x1080@1920x1080/0 -t

//start minicap
adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1920x1080@1920x1080/0

//local port
adb forward tcp:1717 localabstract:minicap

// node js
npm install -g ws
npm install -g express

npm cache clean -f
npm i
npm audit fix
npm audit fix --force

//run js
node F:\Github\minicap-master\example\example\app.js
node app.js

//ndk compile
ndk-build.cmd APP_PLATFORM=android-28
ndk-build.cmd APP_PLATFORM=android-28 PLATFORM_SDK_VERSION=28

