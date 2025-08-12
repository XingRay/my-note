# windows下编译ncnn

下载源码

```shell
git clone git@github.com:Tencent/ncnn.git
```

或者从 Release 页 https://github.com/Tencent/ncnn/releases 中下载指定版本源码, 如:
https://github.com/Tencent/ncnn/archive/refs/tags/20250503.zip



## 1 编译为android库

### 1.1 debug

```shell
set "ROOT_DIR=%cd%"
set "BUILD_DIR=%ROOT_DIR%\build\android\arm64-v8a\debug"
set "INSTALL_DIR=%ROOT_DIR%\install\android\arm64-v8a\debug"
set "ANDROID_TOOLCHAIN_VERSION=29.0.13599879"


set "ANDROID_TOOLCHAIN_FILE=D:/develop/android/android-sdk-windows/ndk/%ANDROID_TOOLCHAIN_VERSION%/build/cmake/android.toolchain.cmake"


cmake ^
-S "%ROOT_DIR%/ncnn" ^
-B "%BUILD_DIR%" ^
-G Ninja ^
-DCMAKE_INSTALL_PREFIX="%INSTALL_DIR%" ^
-DCMAKE_TOOLCHAIN_FILE="%ANDROID_TOOLCHAIN_FILE%" ^
-DANDROID_ABI="arm64-v8a" ^
-DANDROID_PLATFORM=android-26 ^
-DNCNN_VULKAN=ON ^
-DNCNN_DISABLE_RTTI=OFF ^
-DNCNN_DISABLE_EXCEPTION=OFF ^
-DNCNN_SIMPLEVK=ON

cmake --build "%BUILD_DIR%" --config Debug -j 32

cmake --install "%BUILD_DIR%" --config Debug

```



### 1.2 release

```shell
set "ROOT_DIR=%cd%"
set "BUILD_DIR=%ROOT_DIR%\build\android\arm64-v8a\release"
set "INSTALL_DIR=%ROOT_DIR%\install\android\arm64-v8a\release"
set "ANDROID_TOOLCHAIN_VERSION=29.0.13599879"


set "ANDROID_TOOLCHAIN_FILE=D:/develop/android/android-sdk-windows/ndk/%ANDROID_TOOLCHAIN_VERSION%/build/cmake/android.toolchain.cmake"


cmake ^
-S "%ROOT_DIR%/ncnn" ^
-B "%BUILD_DIR%" ^
-G Ninja ^
-DCMAKE_INSTALL_PREFIX="%INSTALL_DIR%" ^
-DCMAKE_TOOLCHAIN_FILE="%ANDROID_TOOLCHAIN_FILE%" ^
-DANDROID_ABI="arm64-v8a" ^
-DANDROID_PLATFORM=android-26 ^
-DNCNN_VULKAN=ON ^
-DNCNN_DISABLE_RTTI=OFF ^
-DNCNN_DISABLE_EXCEPTION=OFF ^
-DNCNN_SIMPLEVK=OFF

cmake --build "%BUILD_DIR%" --config Release -j 32

cmake --install "%BUILD_DIR%" --config Release

```





## 2 编译为windows库

### 2.1 debug

```shell
set "ROOT_DIR=%cd%"
set "BUILD_DIR=%ROOT_DIR%\build\windows\x64\debug"
set "INSTALL_DIR=%ROOT_DIR%\install\windows\x64\debug"

cmake ^
-S "%ROOT_DIR%/ncnn" ^
-B "%BUILD_DIR%" ^
-A x64 ^
-G "Visual Studio 17 2022" ^
-DCMAKE_INSTALL_PREFIX="%INSTALL_DIR%" ^
-DNCNN_VULKAN=ON ^
-DNCNN_DISABLE_RTTI=OFF ^
-DNCNN_DISABLE_EXCEPTION=OFF ^
-DNCNN_SIMPLEVK=OFF 


cmake --build "%BUILD_DIR%" --config Debug -j 32


cmake --install "%BUILD_DIR%" --config Debug

```



### 2.2 release

```shell
set "ROOT_DIR=%cd%"
set "BUILD_DIR=%ROOT_DIR%\build\windows\x64\release"
set "INSTALL_DIR=%ROOT_DIR%\install\windows\x64\release"

cmake ^
-S "%ROOT_DIR%/ncnn" ^
-B "%BUILD_DIR%" ^
-A x64 ^
-G "Visual Studio 17 2022" ^
-DCMAKE_INSTALL_PREFIX="%INSTALL_DIR%" ^
-DNCNN_VULKAN=ON ^
-DNCNN_DISABLE_RTTI=OFF ^
-DNCNN_DISABLE_EXCEPTION=OFF ^
-DNCNN_SIMPLEVK=OFF 


cmake --build "%BUILD_DIR%" --config Release -j 32


cmake --install "%BUILD_DIR%" --config Release

```



