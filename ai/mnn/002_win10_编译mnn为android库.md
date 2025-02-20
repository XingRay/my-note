# win10_编译mnn为android库

1 下载 mnn 源码

```
git clone git@github.com:alibaba/MNN.git
```



2 编译

创建编译脚本 build_android_arm64-v8a.ps1

```
# 获取当前目录
$CurrentDir = Get-Location

# 计算可用的 CPU 线程数
$NumThreads = [System.Environment]::ProcessorCount

# 创建必要的目录（等效于 mkdir -p）
New-Item -ItemType Directory -Path "$CurrentDir/build/android_arm64-v8a_vulkan" -Force
New-Item -ItemType Directory -Path "$CurrentDir/install/android_arm64-v8a_vulkan" -Force

# 运行 CMake 配置
cmake -S "$CurrentDir/MNN" `
      -B "$CurrentDir/build/android_arm64-v8a_vulkan" `
	  -G Ninja `
	  -DCMAKE_INSTALL_PREFIX="$CurrentDir/install/android_arm64-v8a_vulkan" `
      -DCMAKE_TOOLCHAIN_FILE="D:/develop/android/android-sdk-windows/ndk/27.2.12479018/build/cmake/android.toolchain.cmake" `
	  -DANDROID_ABI="arm64-v8a" `
	  -DANDROID_PLATFORM=android-21 `
	  -DANDROID_NATIVE_API_LEVEL=android-21  `
	  -DANDROID_STL=c++_static `
      -DMNN_VULKAN=ON `
      -DMNN_BUILD_DEMO=ON `
	  -DMNN_USE_LOGCAT=false `
	  -DMNN_BUILD_BENCHMARK=ON `
	  -DMNN_USE_SSE=OFF `
	  -DMNN_BUILD_TEST=ON `
	  -DMNN_BUILD_FOR_ANDROID_COMMAND=true `
	  -DNATIVE_LIBRARY_OUTPUT="$CurrentDir/install/android_arm64-v8a_vulkan" `
	  -DNATIVE_INCLUDE_OUTPUT="$CurrentDir/install/android_arm64-v8a_vulkan" `
      -DCMAKE_BUILD_TYPE=Release

# 使用多线程编译
cmake --build "$CurrentDir/build/android_arm64-v8a_vulkan" --config Release -j24

# 安装
cmake --install "$CurrentDir/build/android_arm64-v8a_vulkan" --config Release
```

执行脚本即可