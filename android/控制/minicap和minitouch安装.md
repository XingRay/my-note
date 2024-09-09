# minicap和minitouch安装



安装 adb
https://dl.google.com/android/repository/platform-tools-latest-windows.zip

解压后会出现platform-tools文件夹，把文件夹地址加入系统环境变量当中。

如果adb安装成功，在cmd中输入adb会有相应提示。

安装 NDK
minicap需要使用ndk-build进行编译。

下载ndk
https://developer.android.com/ndk/downloads/

解压后将文件夹地址加入到环境变量当中。

如果 ndk 安装成功，在cmd中输入ndk-build会有相应提示。

安装 make

下载：http://www.equation.com/servlet/equation.cmd?fa=make

然后将下载下来的make.exe位置加入环境变量。

安装 minicap
git clone https://github.com/openstf/minicap
cd minicap
git submodule init
git submodule update
ndk-build

测试
adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P : Display projection (x@x/{0|90|180|270}).

adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1080x1920@1080x1920/0
这里和分别表示设备屏幕的宽和高，中间是小写字母 x 。

cmd下 安装minitouch：
git clone https://github.com/openstf/minitouch
cd minitouch
git submodule init
git submodule update
ndk-build

执行 git submodule update 这一步出现两个问题，解决方法如下：

Android NDK: APP_PLATFORM not set. Defaulting to minimum supported version android-14.

从网站下载 libevdev-1.5.9 ：https://www.freedesktop.org/software/libevdev/
找到 Application.mk 文件，然后修改这个文件就解决了

添加内容
APP_PLATFORM := android-14

参考链接：https://www.jianshu.com/p/65b2a613dfe5

fatal error: ‘libevdev.h’ file not found
从 libevdev-1.5.9 里面取出 libevdev文件夹， 复制到
C:\Users\xxx\minitouch\jni\vendor\libevdev\source\
然后再次输入 ndk-build 成功了。