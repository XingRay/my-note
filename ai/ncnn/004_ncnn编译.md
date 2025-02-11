# NCNN编译

在windows下编译ncnn源码:



## 1 编译为windows库

### 编译protobuf

下载源码

 https://github.com/google/protobuf/archive/v3.11.2.zip

创建目录

```
mkdir build
mkdir install
```

配置和编译

```
cmake -S D:\develop\cpp\protobuf\3.11.2\protobuf-3.11.2\cmake -B D:\develop\cpp\protobuf\3.11.2\build -DCMAKE_INSTALL_PREFIX=D:\develop\cpp\protobuf\3.11.2\install -G "Visual Studio 17 2022" -DCMAKE_CXX_STANDARD=14 -Dprotobuf_MSVC_STATIC_RUNTIME=OFF -Dprotobuf_BUILD_TESTS=OFF

cmd /c cmake --build D:\develop\cpp\protobuf\3.11.2\build --config Release

cmd /c cmake --install D:\develop\cpp\protobuf\3.11.2\build --config Release
```



### 编译ncnn

下载源码

```
git clone git@github.com:Tencent/ncnn.git
```

创建目录

```
mkdir build-windows-x64
mkdir install-windows-x64
```

配置和启动编译

```
cmake -S %cd%/ncnn -B %cd%/build-windows-x64 -A x64 -G "Visual Studio 17 2022" -DCMAKE_INSTALL_PREFIX=%cd%/install-windows-x64 -Dprotobuf_DIR=D:\develop\cpp\protobuf\3.11.2\install\cmake -DNCNN_VULKAN=ON -DOpenCV_DIR=D:\develop\opencv\4.10.0\sdk\windows\opencv\build

cmd /c cmake --build %cd%/build-windows-x64 --config Release

cmd /c cmake --install %cd%/build-windows-x64 --config Release
```



## 2 编译为android库

下载源码

```
git clone git@github.com:Tencent/ncnn.git
```

创建目录

```
mkdir build-android-aarch64
mkdir install-android-aarch64
```

配置和启动编译

```
cmake -S %cd%/ncnn -B %cd%/build-android-aarch64 -G Ninja -DCMAKE_INSTALL_PREFIX=%cd%/install-android-aarch64 -DCMAKE_TOOLCHAIN_FILE="D:/develop/android/android-sdk-windows/ndk/27.2.12479018/build/cmake/android.toolchain.cmake" -DANDROID_ABI="arm64-v8a" -DANDROID_PLATFORM=android-21 -DNCNN_VULKAN=ON

cmd /c cmake --build %cd%/build-android-aarch64 --config Release

cmd /c cmake --install %cd%/build-android-aarch64 --config Release
```

**注意** 编译为android库要加上 `-G Ninja` 编译参数, 官方文档上没有这个参数,使用cmake编译会报错

