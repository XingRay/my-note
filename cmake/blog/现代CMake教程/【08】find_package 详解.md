# 【08】find_package 详解

## [1. 前言](https://www.cccolt.top/tutorial/cmake/08.html#_1-前言)

先看以下寻找并包含 OpenCV 的 `CMakeLists.txt` 示例



```
cmake_minimum_required(VERSION 3.10)

project(Demo)

find_package(OpenCV REQUIRED)

add_executable(demo main.cpp)

target_link_libraries(
  demo
  PRIVATE ${OpenCV_LIBS}
)
```

这是一段再熟悉不过的 CMakeLists.txt 代码，我们通过 `find_package` 命令寻找到了OpenCV库，并获得了其中的导入目标列表。我们可以使用 `message` 验证一下 `OpenCV_LIBS` 包含的内容



```
foreach(m ${OpenCV_LIBS})
  message(STATUS ${m})
endforeach()

# 输出结果
# opencv_core;opencv_imgproc;opencv_highgui;opencv_imgcodecs; ...
```

那么CMake是如何通过 `find_package` 命令找到OpenCV并且完成配置的呢?

## [2. find_package 解析过程](https://www.cccolt.top/tutorial/cmake/08.html#_2-find-package-解析过程)

**Module 模式**

这种模式下，`find_package` 需要解析 `Findxxx.cmake` 文件，这也是 `find_package` 默认工作的模式

**Config 模式**

这种模式下，`find_package` 需要解析 `xxxConfig.cmake` 文件或者 `xxx-config.cmake` 文件。实际上只有 `Findxxx.cmake` 文件无法找到，即 Module 模式执行不成功，才会进入 Config 模式。

## [3. Module 模式](https://www.cccolt.top/tutorial/cmake/08.html#_3-module-模式)

### [3.1 用法](https://www.cccolt.top/tutorial/cmake/08.html#_3-1-用法)

**Module** 模式下 `find_package` 的参数为：



```
find_package(<package> [VERSION] [EXACT] [QUIET] [MODULE]
             [REQUIRED] [[COMPONENTS] [components...]]
             [OPTIONAL_COMPONENTS components...]
             [NO_POLICY_SCOPE])
```

这里对上述提及到的参数做个简单的解释：

|   参数名称   | 可选性 | 名称                                                         | 备注                 |
| :----------: | :----: | :----------------------------------------------------------- | :------------------- |
|  `package`   |  必填  | 需要查找的包名                                               | 注意大小写           |
|  `VERSION`   |  可选  | 指定版本，如果指定就必须检查找到的 包的版本是否和`VERSION`兼容 |                      |
|   `EXACT`    |  可选  | 如果指定该参数，则表示需要寻找必须 完全匹配的版本而不是兼容版本 | 配合`VERSION`使用    |
|  `REQUIRED`  |  可选  | 表示一定要找到包，找不到的话就立即 停掉整个 CMake 解析过程   |                      |
|   `QUIET`    |  可选  | 表示如果查找失败，不会在屏幕进行输出                         | 不得与`REQUIRED`搭配 |
|   `MODULE`   |  可选  | 前面提到说 “ **Module** 模式执行失败， 才会进入 **Config** 模式”，但是假如加入了 `MODULE`选项，那么就只在 **Module** 模式 查找，如果 **Module** 模式下查找失败并不 切换到 **Config** 模式查找。 |                      |
| `COMPONENTS` |  可选  | 表示查找的包中必须要找到的组件 (components)，如果有任何一个找不到 就算失败，类似于`REQUIRED`，导致 CMake 停止执行。 |                      |

### [3.2 文件搜索过程](https://www.cccolt.top/tutorial/cmake/08.html#_3-2-文件搜索过程)

- [#2](https://www.cccolt.top/tutorial/cmake/08.html#_2-find-package-解析过程) 提到过，Module 模式下需要查找到名为 `Find<PackageName>.cmake` 的配置文件。该文件的查找过程只涉及到两个路径：`CMAKE_MODULE_PATH` 和 CMake 安装路径下的 Modules 目录。即，搜索路径依次为：
- 先在 `CMAKE_MODULE_PATH` 变量对应的路径中查找。如果路径为空，或者路径中查找失败，则在 CMake 安装目录（即 `CMAKE_ROOT` 变量）下的 Modules 目录中查找。这个目录通常为 `/usr/share/cmake-x.xx/Modules`。这两个变量可以在`CMakeLists.txt`文件中打印查看具体内容：
- 其中 `CMAKE_MODULE_PATH` 默认为空，可以利用 `set` 命令对该变量进行设置。
- 在安装 CMake 时，CMake 为我们提供了很多开发库的 `FindXXX.cmake` 模块文件，这些文件都定义在 `CMAKE_ROOT` 指代路径的文件夹里面。可以在终端通过键入以下命令进行查询

## [4. Config 模式](https://www.cccolt.top/tutorial/cmake/08.html#_4-config-模式)

### [4.1 用法](https://www.cccolt.top/tutorial/cmake/08.html#_4-1-用法)

**Config** 模式下 `find_package` 的完整命令参数如下，不必全部了解。

- 相比于 **Module** 模式，**Config** 模式的参数更多，也更复杂，但实际在使用过程中我们并不会用到所有参数，大部分参数都是可选的，我们只需要掌握基本的参数用法即可。
- 其中具体查找库并给 `XXX_INCLUDE_DIRS` 和 `XXX_LIBS` 两个变量赋值的操作由 `XXXConfig.cmake` 模块完成。
- 两种模式看起来似乎差不多，不过 CMake 默认采取 **Module** 模式，如果 **Module** 模式未找到库，才会采取 **Config** 模式。总之，**Config** 模式是一个备选策略。通常，库安装时会拷贝一份 `XXXConfig.cmake` 到系统目录中，因此在没有显式指定搜索路径时也可以顺利找到。

### [4.2 文件搜索过程](https://www.cccolt.top/tutorial/cmake/08.html#_4-2-文件搜索过程)

- 与 **Module** 模式不同，**Config** 模式需要查找的路径非常非常非常多，也要匹配很多的可能性，因此有些路径是首先作为根目录，然后进行子目录的匹配，而有些则是直接指定到具体的 `XXXConfig.cmake` 文件。具体将会按照如下顺序进行查找：
  1. `<PackageName>_DIR`
  2. `CMAKE_PREFIX_PATH`、`CMAKE_FRAMEWORK_PATH`、`CMAKE_APPBUNDLE_PATH`
  3. `PATH`环境变量路径
  4. `CMAKE_SYSTEM_PREFIX_PATH`等系统变量路径

**<PackageName>_DIR**

首先搜索名为`<PackageName>_DIR`的 CMake 变量或环境变量路径，这个变量默认为空。这个路径需要直接指定到`<PackageName>Config.cmake`或`<lower-case-package-name>-config.cmake`文件所在目录才能找到，可参考以下示例：

**CMAKE_PREFIX_PATH 等变量**

如果按照`<PackageName>_DIR`搜索不到相应的`XXXConfig.cmake`文件的话，则会查找名为`CMAKE_PREFIX_PATH`、`CMAKE_FRAMEWORK_PATH`、`CMAKE_APPBUNDLE_PATH`的 CMake 变量或环境变量路径。这些变量指定的路径将作为查找时的根目录，**它们默认都为空**。根目录的相关内容在后文会提及。

提示

如果你电脑中安装了 **ROS** 并配置好之后，你在终端执行 `echo $CMAKE_PREFIX_PATH` 会发现 **ROS** 会将 `CMAKE_PREFIX_PATH` 这个变量设置为 **ROS** 中的库的路径，意思是会首先查找 **ROS** 安装的库，如果恰好你在 **ROS** 中安装了 **OpenCV **库，就会发现首先找到的是 **ROS** 中的 **OpenCV**，而不是你自己安装到系统中的 **OpenCV**。

**PATH 环境变量路径**

- 若还找不到，则是搜索 `PATH` 环境变量路径，这个路径也是根目录，默认为系统环境 `PATH` 环境变量值。

- 其实这个路径才是 **Config** 模式大部分情况下能够查找到安装到系统中各种库的原因。这个路径的查找规则为：遍历 `PATH` 环境变量中的各路径，如果该路径如果以 `bin` 或 `sbin` 结尾，则自动回退到上一级目录得到根目录。在终端键入以下内容以查看系统环境变量路径：

  

  ```
  echo $PATH
  ```

  一般情况输出结果如下，这里对每个路径都做了换行处理，并且加了些简单的注释。

  

  ```
  /home/<user-name>/.local/bin: # 回退到 /home/<user-name>/.local 后再进行搜索
  /usr/local/sbin:              # 回退到 /usr/local 后再进行搜索
  /usr/local/bin:               # 同上
  /usr/sbin:                    # 回退到 /usr 后再进行搜索
  /usr/bin:                     # 同上
  /sbin:                        # 回退到 / 后再进行搜索
  /bin:                         # 同上
  /usr/games:
  /usr/local/games:
  /snap/bin                     # 回退到 /snap 后再进行搜索
  ```

  说明这些路径会被默认添加在 **Config** 模式的搜索路径中。

**CMAKE_SYSTEM_PREFIX_PATH 等变量**

- 我们在安装 OpenCV 的时候，可以在 `cmake-gui` 中修改 `CMAKE_INSTALL_PREFIX` 变量的内容，把 OpenCV 安装在 `/opt/opencv` 下，我们在使用 `find_package` 时也不需要指名路径，说明除了上面的一些变量之外，还存在另外一系列路径。这里列出最重要的一个变量：`CMAKE_SYSTEM_PREFIX_PATH`

- 我们可以在一个 CMakeLists.txt 文件下，输入以下内容：

  

  ```
  foreach(path ${CMAKE_SYSTEM_PREFIX_PATH})
    message(STATUS "${path}")
  endforeach()
  ```

  会打印出以下内容

  

  ```
  -- /usr/local
  -- /usr
  -- /
  -- /usr/local
  -- /usr/local
  -- /usr/X11R6
  -- /usr/pkg
  -- /opt
  ```

- 留意该变量最后一点包含的内容，这正是我们可以把第三方库安装在 `/opt` 的原因。事实上，很多软件包都是默认安装在 `/opt` 下的，譬如 ROS。

补充

相关包含的内容还有非常之多，有兴趣的可以阅读官网和源码，这里给出官网和源码 GitLab 地址：

- `find_package` 寻找包含路径的源码 GitLab：[源码 GitLab 地址](https://gitlab.kitware.com/cmake/cmake/-/blob/7a869ebaf13e241d8a2c52a1b5ab3dd4191bf2b6/Modules/Platform/UnixPaths.cmake#L50)
- `find_package` 寻找包含路径的官网：[官网网址](https://cmake.org/cmake/help/latest/command/find_package.html#search-procedure)

### [4.3 根目录路径详解](https://www.cccolt.top/tutorial/cmake/08.html#_4-3-根目录路径详解)

上述提及到的根目录路径，在指明这些内容时，CMake 会首先检查这些根目录路径下是否有名为 `<PackageName>Config.cmake` 或 `<lower-case-package-name>-config.cmake` 的模块文件，如果没有，CMake 会继续检查或匹配这些根目录下的以下路径：



```
<prefix>/(lib/<arch>|lib|share)/cmake/<name>*/
<prefix>/(lib/<arch>|lib|share)/<name>*/
<prefix>/(lib/<arch>|lib|share)/<name>*/(cmake|CMake)/
```

其中为 `arch` 系统架构名，如 Ubuntu 下一般为：`x86_64-linux-gnu`，整个 `(lib/<arch>|lib|share)` 为可选路径，例如 OpenCV 库而言会检查或匹配以下内容：

1. `<prefix>/lib/x86_64-linux-gnu/opencv4/`
2. `<prefix>/lib/cmake/opencv4/`
3. `<prefix>/share/opencv4/`
4. ……

注意

上文提及过的 `<PackageName>_DIR` 路径不是根目录路径

## [5. FindXXX.cmake 简单写法](https://www.cccolt.top/tutorial/cmake/08.html#_5-findxxx-cmake-简单写法)

我们使用 Module 模式，写一个 `FindXXX.cmake` 文件，来体验一下自己的`find_package`

### [5.1 案例](https://www.cccolt.top/tutorial/cmake/08.html#_5-1-案例)

假设某家相机厂商的 SDK 提供了以下内容，假设将其放在了 `/opt/camera` 文件夹下，请创建一个 `CameraDemo` 项目，并设计一个 `FindCamera.cmake` 文件供本项目中的其余目标包含，使其能够链接至本相机的 SDK。



```
.
├── include
│   ├── CameraApi.h
│   ├── CameraDefine.h
│   └── CameraStatus.h
└── lib
    └── libMVSDK.so
```

### [5.2 分析](https://www.cccolt.top/tutorial/cmake/08.html#_5-2-分析)

我们在[【05】目标构建](https://www.cccolt.top/tutorial/cmake/05.html)的导入目标中遇到过类似的情况，但这里的相机 SDK 并没有位于本项目中。

注意到相机厂商的 SDK 并无提供相应的 `CameraConfig.cmake` 或 `camera-config.cmake` 文件，因此需要自己手写 `FindCamera.cmake` 文件。

我们先把相机项目的部署框架给大概定义好：



```
.
├── cmake
│   └── FindCamera.cmake
├── CMakeLists.txt
└── main.cpp
```

涉及到的两个 CMake 文件已经标红，这里简单描述一下

- `FindCamera.cmake`：其他库直接包含、链接 SDK 提供的库肯定是不行的，需要事先找到 SDK 的路径，此文件就是为了提前让整个 CMake 工程找到此 SDK。

- 项目根目录的 `CMakeLists.txt`：框架整体配置，包括项目、编译属性、可执行程序的配置。这里 `main.cpp` 直接用到了 SDK 的内容，通过以下语句进行链接

  

  ```
  target_link_libraries(
    demo
    PRIVATE camera
  )
  ```

### [5.3 配置](https://www.cccolt.top/tutorial/cmake/08.html#_5-3-配置)

#### [5.3.1 项目根目录的 CMakeLists.txt](https://www.cccolt.top/tutorial/cmake/08.html#_5-3-1-项目根目录的-cmakelists-txt)

① 主要目的

1. 设置 `CMAKE_MODULE_PATH` 模块模式的工作路径
2. 解析 `FindCamera.cmake`，并找到 camera 对应的库
3. 创建可执行程序目标并链接

② 示例代码



```
cmake_minimum_required(VERSION 3.10)
project(CameraDemo)
# 本篇 2.2.2 节提及过，下一句是让 CMake 工程能够找到 FindCamera.cmake
list(APPEND CMAKE_MODULE_PATH "${CMAKE_SOURCE_DIR}/cmake")

# 找到 SDK
find_package(Camera)

# 添加可执行文件并链接
add_executable(demo main.cpp)
target_link_libraries(
  demo
  PRIVATE camera
)
```

#### [5.3.2 FindCamera.cmake](https://www.cccolt.top/tutorial/cmake/08.html#_5-3-2-findcamera-cmake)

① 主要任务

1. 提供一个 CMake 目标变量（一般是导入目标）
   1. 找到 SDK 的头文件
   2. 找到 SDK 的库文件
2. 提供有无找到的状态信息

② 详细注释的示例代码



```
# 设置 mvsdk 的路径（可自行设置）
set(mvsdk_root "/opt/camera")

# 发现头文件路径，并设置 Camera_INCLUDE_DIRS
find_path(
  Camera_INCLUDE_DIR
  NAMES CameraApi.h CameraDefine.h CameraStatus.h
  PATHS "${mvsdk_root}/include"
  NO_DEFAULT_PATH # 默认路径下会包含 /usr/local 等内容，这句话表明不包含默认路径
)

# 发现库文件路径
find_library(
  Camera_LIB
  NAMES "libMVSDK.so"
  PATHS "${mvsdk_root}/lib"
  NO_DEFAULT_PATH # 同 find_path
)

# 设置导入目标，称为 mvsdk
if(NOT TARGET mvsdk)
  # 将目标设置为在项目中全局可见 GLOBAL
  add_library(mvsdk SHARED IMPORTED GLOBAL)
  # 为导入目标添加相关属性
  set_target_properties(mvsdk PROPERTIES
    INTERFACE_INCLUDE_DIRECTORIES "${Camera_INCLUDE_DIR}"
    IMPORTED_LOCATION "${Camera_LIB}"
  )
endif()

set(Camera_INCLUDE_DIRS ${Camera_INCLUDE_DIR})
set(Camera_LIBS mvsdk)

# 这里用到了一个 CMake 自带的函数，会根据指定内容设置 Camera_FOUND 变量
include(FindPackageHandleStandardArgs)
# 意为必需的参数是 Camera_LIBS，如果 Camera_LIBS 为空，则 Camera_FOUND
# 将被设置为 FALSE，并在使用 find_package(Camera REQUIRED) 时也会报错
find_package_handle_standard_args(
  Camera
  REQUIRED_VARS Camera_LIBS Camera_INCLUDE_DIRS
)
```

③ [FindPackageHandleStandardArgs详解](https://cmake.org/cmake/help/latest/module/FindPackageHandleStandardArgs.html?highlight=find_package_handle_standard_args#command:find_package_handle_standard_args)

## [6. XXXConfig.cmake](https://www.cccolt.top/tutorial/cmake/08.html#_6-xxxconfig-cmake)

关于 `XXXConfig.cmake` 或 `xxx-config.cmake` 的写法会在后续文章中详细说明：

- [【09】生成器表达式](https://www.cccolt.top/tutorial/cmake/09.html)
- [【10】项目的导出与安装](https://www.cccolt.top/tutorial/cmake/10.html)



