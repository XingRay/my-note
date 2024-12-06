# ubuntu安装android环境

安装jdk

```
sudo apt install openjdk-21-jdk-headless
```



安装 android command line tools

https://developer.android.com/studio/#downloads



找到 Command line tools only
下载

commandlinetools-linux-11076708_latest.zip

https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip



```
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
mkdir -p Android/Sdk
unzip commandlinetools-linux-11076708_latest.zip -d Android/Sdk

export ANDROID_HOME=$HOME/Android/Sdk
# Make sure emulator path comes before tools. Had trouble on Ubuntu with emulator from /tools being loaded
# instead of the one from /emulator
export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/platform-tools:$PATH"

sdkmanager --sdk_root=${ANDROID_HOME} "tools"

sdkmanager --update
sdkmanager --list
sdkmanager "build-tools;28.0.3" "platform-tools" "platforms;android-28" "tools"
sdkmanager --licenses

sudo apt install gradle
```



解压到任意目录

```shell
/mnt/d/tmp/
```

解压后是一个目录 cmdline-tools :

```shell
/mnt/d/tmp/cmdline-tools
```



设置 sdk 保存的目录及环境变量:

```shell
export ANDROID_HOME=~/develop/android/sdk

export PATH="$ANDROID_HOME/emulator:$ANDROID_HOME/cmdline-tools/latest:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

export ANDROID_NDK_HOME=~/develop/android/sdk/ndk/26.3.11579264
export PATH="$ANDROID_NDK_HOME:$PATH"
```

```
cd /mnt/d/tmp/cmdline-tools/bin
```

重新安装最新版本 cmdline-tools

```shell
./sdkmanager --sdk_root=${ANDROID_HOME} "cmdline-tools;latest"
```

会下载和安装最想新版本的 cmdline-tools 并解压到  ${ANDROID_HOME}/cmdline-tools/latest 目录



```shell
sdkmanager "cmdline-tools;latest"
sdkmanager "build-tools;35.0.0"
sdkmanager "platform-tools"
sdkmanager "ndk;27.2.12479018"
sdkmanager "platforms;android-35"
```



ndk

```
ndk;27.2.12479018
ndk;26.3.11579264
ndk;25.2.9519653
ndk;24.0.8215888
ndk;23.2.8568313
ndk;22.1.7171670
ndk;21.4.7075529
```

```shell
sdkmanager "ndk;27.2.12479018" "ndk;26.3.11579264" "ndk;25.2.9519653" "ndk;24.0.8215888" "ndk;23.2.8568313" "ndk;22.1.7171670" "ndk;21.4.7075529"
```

```shell
sdkmanager --licenses
```



引入 ndk rule

https://github.com/bazelbuild/rules_android_ndk



WORKSPACE

```python
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# # Requires that the ANDROID_HOME environment variable is set to the Android SDK path.
android_sdk_repository(
    name = "androidsdk",
    
    path = "/home/leixing/develop/android/sdk", # linux on wsl2 of windows
    # path = "D:/develop/android/sdk",

    api_level = 35,
    build_tools_version = "35.0.0"
)


# android ndk

http_archive(
    name = "rules_android_ndk",
    sha256 = "65aedff0cd728bee394f6fb8e65ba39c4c5efb11b29b766356922d4a74c623f5",
    strip_prefix = "rules_android_ndk-0.1.2",
    url = "https://github.com/bazelbuild/rules_android_ndk/releases/download/v0.1.2/rules_android_ndk-v0.1.2.tar.gz",
)
load("@rules_android_ndk//:rules.bzl", "android_ndk_repository")

ANDROID_NDK_VERSION = "26.3.11579264"

android_ndk_repository(
    name = "androidndk",
    path = "/home/leixing/develop/android/sdk/ndk/" + ANDROID_NDK_VERSION, # linux on wsl2 of windows
    # path = "D:/develop/android/sdk/ndk/" + ANDROID_NDK_VERSION, # windows
)

register_toolchains("@androidndk//:all")
```





编译指令:

```shell
bazel build //app/src/main:app --fat_apk_cpu=armeabi-v7a,arm64-v8a,x86,x86_64 --android_crosstool_top=@androidndk//:toolchain
```

