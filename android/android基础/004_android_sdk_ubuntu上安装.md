# ubuntu上安装android sdk

1 下载sdk

https://developer.android.com/studio

找到:
仅限命令行工具

点击 commandlinetools-linux-11076708_latest.zip , 下滑点击同意条款, 开始下载

下载地址:
https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip?hl=zh-cn

将安装包复制到指定目录:

```shell
mkdir -p ~/download
(base) leixing@desktop-xing:/mnt/e/develop/android/sdk$ cp ./commandlinetools-linux-11076708_latest.zip ~/download/commandlinetools-linux-11076708_latest.zip
```

准备好android sdk 目录

```
mkdir -p ~/develop/android/sdk
```



解压命令行工具:

```
unzip ./commandlinetools-linux-11076708_latest.zip
```

进入目录:

```
cd cmdline-tools/bin
```

列出可用的安装包:

```
./sdkmanager --sdk_root=~/develop/android/sdk --list
```

找到最新稳定版本的各个组件的名称:

```
build-tools;35.0.1
```

```
cmake;3.31.4
```

```
cmdline-tools;latest
```

```
ndk;28.0.12916984
```

```
platforms;android-35
```

```
sources;android-35
```

安装:

```
sdkmanager "build-tools;35.0.1" "cmake;3.31.4" "cmdline-tools;latest" "ndk;28.0.12916984" "platforms;android-35" "sources;android-35" --sdk_root=/home/leixing/develop/android/sdk
```

```
./sdkmanager --install "cmake;3.31.4" --sdk_root=/home/leixing/develop/android/sdk
```

注意这里 sdk_root 参数使用绝对路径, 不要使用 "~/develop/android/sdk"

如果需要其他版本,可以根据需要从list指令输出的列表中查找

此时目标目录已经安装最新版本的 cmdline-tools,

```
(base) leixing@desktop-xing:~/develop/android/sdk/cmdline-tools/latest/bin$ ls
apkanalyzer  avdmanager  d8  lint  profgen  r8  resourceshrinker  retrace  screenshot2  sdkmanager
```

可以把现在目录的cmdline-tools 删除

```
cd ../..
rm -rf cmdline-tools/
```

可以将下载的工具包移动到指定的备用目录:

```
(base) leixing@desktop-xing:~/download$ ls
commandlinetools-linux-11076708_latest.zip
(base) leixing@desktop-xing:~/download$ mkdir -p ~/setup/android
(base) leixing@desktop-xing:~/download$ mv ./commandlinetools-linux-11076708_latest.zip ~/setup/android/commandlinetools-linux-11076708_latest.zip
```

导出环境变量

```
nano ~/.bashrc
```

在末尾添加:

```
export ANDROID_HOME=~/develop/android/sdk

export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

export ANDROID_NDK_HOME=~/develop/android/sdk/ndk/26.3.11579264
export PATH="$ANDROID_NDK_HOME:$PATH"
```

保存退出后立即生效:

```
source ~/.bashrc
```

补充上面使用的常用的ndk版本:

```
sdkmanager "ndk;26.3.11579264"
```

前面导入了path, 这里可以简化指令

