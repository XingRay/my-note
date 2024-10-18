# CPP代码编译为androoid使用的so库



## 安装android-ndk

1 通过android-studio 安装,  tools/sdk-manager 即可

2 手动安装
https://developer.android.com/ndk/downloads?hl=zh-cn

最新版本下载地址:

https://dl.google.com/android/repository/android-ndk-r27c-windows.zip



设置系统环境变量

ANDROID_NDK = path-to-ndk

如:

需要使用管理员权限

```shell
setx ANDROID_NDK "D:\develop\android\android-sdk-windows\ndk\27.2.12479018" /M
```

或者通过 设置/系统/系统信息/高级系统设置/环境变量 页面手动设置

在PATH中添加 %ANDROID_NDK%

```shell
echo %PATH%
setx PATH "%PATH%;%ANDROID_NDK%" /M
echo %PATH%
```

验证:

```shell
 ndk-build --version
```

输出

```
GNU Make 4.3
Built for Windows32
Copyright (C) 1988-2020 Free Software Foundation, Inc.
License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.
```





## 安装ninja

https://github.com/ninja-build/ninja

下载页面

https://github.com/ninja-build/ninja/releases

最新版本下载地址

https://github.com/ninja-build/ninja/releases/download/v1.12.1/ninja-win.zip



下载后保存到指定目录, 将目录设置为环境变量,并添加到PATH

```
setx NINJA_HOME "D:\develop\ninja\1.12.1" /M
setx PATH "%PATH%;%NINJA_HOME%" /M
```

如果遇到调用了 python 下载的ninja, 可以将手动下载安装的ninja的顺序提前

```
setx PATH "%NINJA_HOME%;%PATH%" /M
```

验证:

```
ninja --version
```

输出

```
1.12.1
```





## native代码结构及cmake脚本

android项目 src/main/cpp目录作为native源码根目录, 或者是其他独立的目录

根目录编写CMake脚本, 下面示例引入了 openv和ncnn



CMakeLists.txt

```cmake
cmake_minimum_required(VERSION 3.22.1)

project("makeup_sdk")

##################
## set lib path ##
##################

# glm
# https://github.com/g-truc/glm
set(GLM_INSTALL_DIR D:\\develop\\opengl\\glm\\glm-0.9.9.8)

# opencv
# https://github.com/opencv/opencv
set(OPENCV_INSTALL_DIR D:\\develop\\opencv\\4.10.0\\sdk\\android\\OpenCV-android-sdk)

# opencv-mobile
# https://github.com/nihui/opencv-mobile
#set(OPENCV_INSTALL_DIR D:\\develop\\opencv-mobile\\4.8.1\\opencv-mobile-4.8.1-android)

# ncnn
# https://github.com/Tencent/ncnn
set(NCNN_INSTALL_DIR D:\\develop\\ncnn\\ncnn-20240102\\ncnn-20240102-android-vulkan)


if (NOT ANDROID_ABI)
        # 或者 armeabi-v7a
        set(ANDROID_ABI arm64-v8a)
endif()

################
## set params ##
################

set(OpenCV_DIR ${OPENCV_INSTALL_DIR}\\sdk\\native\\jni)
message("OpenCV_DIR = ${OpenCV_DIR}")
message("ANDROID_ABI = ${ANDROID_ABI}")
set(ANDROID_NDK_ABI_NAME ${ANDROID_ABI})
message("ANDROID_NDK_ABI_NAME = ${ANDROID_NDK_ABI_NAME}")
find_package(OpenCV REQUIRED)

set(OPENCV_INCLUDE ${OPENCV_INSTALL_DIR}\\sdk\\native\\jni\\include)
message("OpenCV_INCLUDE_DIRS = ${OpenCV_INCLUDE_DIRS}")
message("OpenCV_LIBS = ${OpenCV_LIBS}")


set(ncnn_DIR ${NCNN_INSTALL_DIR}/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)
set(NCNN_INCLUDE ${NCNN_INSTALL_DIR}/${ANDROID_ABI}/include)
message("ncnn_DIR = ${ncnn_DIR}")
message("ncnn_INCLUDE_DIRS = ${ncnn_INCLUDE_DIRS}")
message("ncnn_LIBS = ${ncnn_LIBS}")

include_directories(
        ${GLM_INSTALL_DIR}/glm
        ${OPENCV_INCLUDE}
        ${NCNN_INCLUDE}
        android
#        android/camera
        image
        engine
        engine/blaze
        opengl/filter
        opengl/texture
        renderer/camera_makeup
        renderer/camera_preview
        renderer/face_landmark
        renderer/image_makeup
        shader
        vendor/stb
        opencv/triangle
        jni
        makeup
        opengl
        util
)

# glm
set(BUILD_STATIC_LIBS ON)
add_subdirectory(${GLM_INSTALL_DIR}/glm/glm ${CMAKE_BINARY_DIR}/vendor/glm)

link_libraries(
        glm_static
)


file(GLOB_RECURSE SOURCE_FILES "${CMAKE_CURRENT_SOURCE_DIR}/*.cpp")

add_library( # Sets the name of the library.
        ${PROJECT_NAME}

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${SOURCE_FILES})


target_link_libraries( # Specifies the target library.
        ${PROJECT_NAME}
        # Links the target library to the log library
        # included in the NDK.
        android
        camera2ndk
        mediandk
        log
        EGL
        GLESv3
        ${OpenCV_LIBS}
        ncnn
        )
```



在源码目录的根目录创建编译脚本 build.cmd :

```shell
@echo off
setlocal

rem 设置默认值
set "DEFAULT_ABI=arm64-v8a"
set "DEFAULT_PLATFORM=android-24"

rem 检查是否传入参数，如果没有则使用默认值
if "%~1"=="" (
    set "ANDROID_ABI=%DEFAULT_ABI%"
) else (
    set "ANDROID_ABI=%~1"
)

if "%~2"=="" (
    set "ANDROID_PLATFORM=%DEFAULT_PLATFORM%"
) else (
    set "ANDROID_PLATFORM=%~2"
)

echo Using ANDROID_ABI: %ANDROID_ABI%
echo Using ANDROID_PLATFORM: %ANDROID_PLATFORM%

rem 运行 CMake
cmd /c cmake -DCMAKE_TOOLCHAIN_FILE="%ANDROID_NDK%/build/cmake/android.toolchain.cmake" -DANDROID_ABI=%ANDROID_ABI% -DANDROID_PLATFORM=%ANDROID_PLATFORM% -G Ninja -B ./build

rem 运行 Ninja
cmd /c ninja -C .\build\

endlocal
```



直接运行

```shell
./build.cm
```

或者

```shell
./build.cmd arm64-v8a android-24
```



## gradle编译脚本

android-lib库的gradle编译脚本, 同时支持 32位和64版本

配置:

```kotlin
android {
	// ...
	
    defaultConfig {
		// ...

        ndk{
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }
```



分别支持32位和64位

```kotlin
android {
	// ...
	
	flavorDimensions.add("abi")

    productFlavors {
        create("arm64") {
            dimension = "abi"
            ndk {
                abiFilters.add("arm64-v8a")
            }
        }

        create("armeabi") {
            dimension = "abi"
            ndk {
                abiFilters.add("armeabi-v7a")
            }
        }
    }
}
```



下面是完整的示例:

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.nanosecond.makeupsdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        externalNativeBuild {
            cmake {
                arguments += listOf("-DANDROID_STL=c++_shared")
            }
        }

        ndk{
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:\\develop\\android\\key\\test\\test.jks")
            storePassword = "123456"
            keyAlias = "key0"
            keyPassword = "123456"
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "DEBUG", "true")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs["debug"]

            externalNativeBuild {
                cmake {
//                    cppFlags("-DBUILD_TYPE_RELEASE")
                }
            }
        }

        release {
            buildConfigField("boolean", "DEBUG", "false")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs["release"]

            externalNativeBuild {
                cmake {
                    cppFlags("-DBUILD_TYPE_RELEASE")
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
//        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    ndkVersion = "28.0.12433566"
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // MediaPipe Library
    implementation(libs.tasks.vision)
}
```

