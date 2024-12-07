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









解决 npm 下载依赖的问题:

思路: 在系统中安装 node yarn, 配置 yarn 代理, 自行通过 yarn install 提前下载缓存, 在通过 bazel build 构建



安装 Node.js v16.19.0 可以通过以下步骤实现：

------

### 1. **检查系统环境**

首先更新系统的包管理工具，确保系统环境最新。

```
bash


复制代码
sudo apt update && sudo apt upgrade -y
```

------

### 2. **安装 Node.js 使用 NVM**

使用 [Node Version Manager (NVM)](https://github.com/nvm-sh/nvm) 是最推荐的方法，它允许灵活管理不同版本的 Node.js。

#### (1) 安装 NVM

```
bash


复制代码
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.3/install.sh | bash
```

#### (2) 加载 NVM 环境变量

```
bash复制代码export NVM_DIR="$HOME/.nvm"
source "$NVM_DIR/nvm.sh"
```

你可以把这两行添加到 `~/.bashrc` 或 `~/.zshrc` 文件中以便每次自动加载。

#### (3) 安装 Node.js v16.19.0

使用 NVM 安装指定版本的 Node.js：

```
bash


复制代码
nvm install 16.19.0
```

#### (4) 验证安装

```
bash


复制代码
node -v
```

应输出 `v16.19.0`。

------

### 3. **直接使用 Node.js 官方二进制包**

如果你不想使用 NVM，可以手动安装 Node.js：

#### (1) 下载二进制文件

从 Node.js 官方下载 Node.js v16.19.0 的二进制包：

```
bash


复制代码
wget https://nodejs.org/dist/v16.19.0/node-v16.19.0-linux-x64.tar.xz
```

#### (2) 解压文件

```
bash


复制代码
tar -xf node-v16.19.0-linux-x64.tar.xz
```

#### (3) 移动到 `/usr/local`（全局可用）

```
bash


复制代码
sudo mv node-v16.19.0-linux-x64 /usr/local/node-v16.19.0
```

#### (4) 添加到环境变量

编辑你的 `~/.bashrc` 或 `~/.zshrc` 文件，添加以下内容：

```
bash


复制代码
export PATH="/usr/local/node-v16.19.0/bin:$PATH"
```

然后刷新配置：

```
bash


复制代码
source ~/.bashrc
```

#### (5) 验证安装

```
bash


复制代码
node -v
```

------

### 4. **使用包管理工具 APT 安装 Node.js**

如果想使用 APT 来管理 Node.js，可以使用官方的 NodeSource 仓库：

#### (1) 添加 Node.js 16.x 的仓库

```
bash


复制代码
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
```

#### (2) 安装 Node.js

```
bash


复制代码
sudo apt install -y nodejs
```

#### (3) 验证安装

```
bash


复制代码
node -v
```

------

### 总结

推荐使用 **NVM** 方法，方便灵活管理多个 Node.js 版本。如果不需要多个版本，可以使用 APT 方法简化安装过程。





在安装完 Node.js v16.19.0 后，安装 Yarn 

### 1. **检查网络代理设置**

查看 `npm` 使用的代理配置：

```
bash复制代码npm config get proxy
npm config get https-proxy
```

**常见问题：**

- 如果输出的 URL 格式错误（如不完整或包含非法字符），需要重新设置代理。

**重置代理：**

```
bash复制代码npm config delete proxy
npm config delete https-proxy
```

如果需要重新设置代理，请确保格式正确。例如：

```
bash复制代码
npm config set proxy http://<proxy_host>:<proxy_port>
npm config set https-proxy http://<proxy_host>:<proxy_port>
```

如:

```
npm config set proxy http://172.25.176.1:10809
npm config set https-proxy http://172.25.176.1:10809
```



### 2. **验证代理是否可用**

测试代理连接是否正常：

```
curl -x http://<proxy_host>:<proxy_port> https://registry.npmjs.org/
```

如果无法通过代理连接，检查你的代理服务配置或网络状态。



### 3. **检查 `npm` 配置文件**

打开 `~/.npmrc`，检查是否有错误的配置条目：

```
cat ~/.npmrc
```

删除或修复包含错误 URL 的配置项。例如：

```
proxy=http://
https-proxy=http://
```

可以通过以下命令清理：

```
npm config delete proxy
npm config delete https-proxy
```



### 5. **尝试使用镜像源**

设置 npm 使用国内镜像源（例如淘宝源）：

```
npm config set registry https://registry.npmmirror.com/
npm install -g yarn
```

验证是否生效：

```
npm config get registry
```

输出应为：

```
https://registry.npmmirror.com/
```



安装 yarn

```
npm install -g yarn
```



检查版本

```
yarn -v
```



下载依赖

进入工作目录, 注意最后的 "_" :

```
cd ~/code/git/github/mediapipe/.bazel_out/645a05f6e36e2779eb3e47884d4e72b9/external/npm/_
```

下载缓存到指定目录:

```
yarn --frozen-lockfile --mutex network --cache-folder /home/leixing/code/git/github/mediapipe/.bazel_out/645a05f6e36e2779eb3e47884d4e72b9/external/npm/_yarn_cache install
```

下载完成后再回到项目根目录执行编译:

```
cd ~/code/git/github/mediapipe
```



开始编译:

arm64

```
bazel build -c opt --config=android_arm64 mediapipe/tasks/libmptask/vision/face_landmarker:face_landmarker_android.so
```



arm32

```
bazel build -c opt --config=android_arm mediapipe/tasks/libmptask/vision/face_landmarker:face_landmarker_android.so
```



