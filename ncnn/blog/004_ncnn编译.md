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
cd ncnn 
git submodule update --init
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
cd ncnn 
git submodule update --init
```

创建目录

```
mkdir -p build/android/arm64-v8a
mkdir -p install/android/arm64-v8a
```

配置和启动编译

```
cmake -S %cd%/ncnn -B %cd%/build/android/arm64-v8a -G Ninja -DCMAKE_INSTALL_PREFIX=%cd%/install/android/arm64-v8a -DCMAKE_TOOLCHAIN_FILE="D:/develop/android/android-sdk-windows/ndk/27.2.12479018/build/cmake/android.toolchain.cmake" -DANDROID_ABI="arm64-v8a" -DANDROID_PLATFORM=android-21 -DNCNN_VULKAN=ON

cmd /c cmake --build %cd%/build/android/arm64-v8a --config Release

cmd /c cmake --install %cd%/build/android/arm64-v8a --config Release
```

这里启用了 vulkan ,加上了 `-DNCNN_VULKAN=ON` 参数

**注意** 编译为android库要加上 `-G Ninja` 编译参数, 官方文档上没有这个参数,使用cmake编译会报错



android的build默认关闭了 exception 和 rtti, 可以启用 exception 和 rtti

https://github.com/Tencent/ncnn/issues/5431

添加编译参数:

```
-DNCNN_DISABLE_RTTI=OFF -DNCNN_DISABLE_EXCEPTION=OFF
```

完整编译指令:

```
cmake -S %cd%/ncnn -B %cd%/build/android/arm64-v8a -G Ninja -DCMAKE_INSTALL_PREFIX=%cd%/install/android/arm64-v8a -DCMAKE_TOOLCHAIN_FILE="D:/develop/android/android-sdk-windows/ndk/27.2.12479018/build/cmake/android.toolchain.cmake" -DANDROID_ABI="arm64-v8a" -DANDROID_PLATFORM=android-21 -DNCNN_VULKAN=ON -DNCNN_DISABLE_RTTI=OFF -DNCNN_DISABLE_EXCEPTION=OFF

cmd /c cmake --build %cd%/build/android/arm64-v8a --config Release

cmd /c cmake --install %cd%/build/android/arm64-v8a --config Release
```



补充:
项目中引入了 [Vulkan-Headers](https://github.com/KhronosGroup/Vulkan-Headers) 后会与ncnn冲突:
https://github.com/Tencent/ncnn/issues/5912
解决 simplevk 冲突的办法:

```
cmake -DNCNN_SIMPLEVK=OFF ..
```

但是只修改这个会导致编译失败:

```
Could NOT find Vulkan (missing: Vulkan_LIBRARY)
```

还需要按照这个说明 
https://github.com/nihui/ncnn-android-yolov5/issues/10
提高sdk版本不低于24, 修改后的编译脚本:

```
cmake -S %cd%/ncnn -B %cd%/build/android/arm64-v8a -G Ninja -DCMAKE_INSTALL_PREFIX=%cd%/install/android/arm64-v8a -DCMAKE_TOOLCHAIN_FILE="D:/develop/android/android-sdk-windows/ndk/27.2.12479018/build/cmake/android.toolchain.cmake" -DANDROID_ABI="arm64-v8a" -DANDROID_PLATFORM=android-24 -DNCNN_VULKAN=ON -DNCNN_DISABLE_RTTI=OFF -DNCNN_DISABLE_EXCEPTION=OFF -DNCNN_SIMPLEVK=OFF

cmd /c cmake --build %cd%/build/android/arm64-v8a --config Release

cmd /c cmake --install %cd%/build/android/arm64-v8a --config Release
```

编译后修改 ncnn/platfrom.h

```
#if __ANDROID_API__ >= 26
#define VK_USE_PLATFORM_ANDROID_KHR
#endif // __ANDROID_API__ >= 26
```

添加 #ifndef :

```
#if __ANDROID_API__ >= 26
#ifndef VK_USE_PLATFORM_ANDROID_KHR
#define VK_USE_PLATFORM_ANDROID_KHR
#endif
#endif // __ANDROID_API__ >= 26
```

