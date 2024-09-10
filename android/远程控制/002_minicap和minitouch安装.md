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

```bash
git clone https://github.com/openstf/minicap
cd minicap
git submodule init
git submodule update
ndk-build APP_PLATFORM=android-28 PLATFORM_SDK_VERSION=28

adb shell getprop ro.product.cpu.abi
arm64-v8a

adb shell getprop ro.build.version.sdk
34

# 注意根据上面的输出修改下面的ABI和SDK
adb push libs/arm64-v8a/minicap /data/local/tmp/
adb push jni/minicap-shared/aosp/libs/android-34/arm64-v8a/minicap.so /data/local/tmp/

adb shell chmod 755 /data/local/tmp/minicap
```

测试

```
adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -h

Usage: /data/local/tmp/minicap [-h] [-n <name>]
  -d <id>:       Display ID. (0)
  -n <name>:     Change the name of the abtract unix domain socket. (minicap)
  -P <value>:    Display projection (<w>x<h>@<w>x<h>/{0|90|180|270}).
  -Q <value>:    JPEG quality (0-100).
  -s:            Take a screenshot and output it to stdout. Needs -P.
  -S:            Skip frames when they cannot be consumed quickly enough.
  -r <value>:    Frame rate (frames/s)  -t:            Attempt to get the capture method running, then exit.
  -i:            Get display information in JSON format. May segfault.
  -h:            Show help.
```



查看屏幕分辨率

```
adb shell wm size
Physical size: 1440x3120
```





```
adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P : Display projection (x@x/{0|90|180|270}).
```

```
adb shell LD_LIBRARY_PATH=/data/local/tmp /data/local/tmp/minicap -P 1440x3120@1440x3120/0
```

这里和分别表示设备屏幕的宽和高，中间是小写字母 x 。

cmd下 安装minitouch：

```bash
git clone https://github.com/openstf/minitouch
cd minitouch
git submodule init
git submodule update
ndk-build

adb shell getprop ro.product.cpu.abi

adb push libs\arm64-v8a\minitouch /data/local/tmp/

adb shell chmod 755 /data/local/tmp/minitouch
```

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