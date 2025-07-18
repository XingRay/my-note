# 【02】CMake 的文件分布

在正式开始学习 CMake 之前，需要清楚 CMake 文件一般在项目中是如何分布的，只有清楚各个文件是如何分布的，才能正确且合理的书写 `CMakeLists.txt` 文件。

## [1. 按模块分层的 CMakeLists.txt](https://www.cccolt.top/tutorial/cmake/02.html#_1-按模块分层的-cmakelists-txt)

一般来说，自己在使用 CMake 的时候，如果项目不大，往往都喜欢仅使用一个 `CMakeLists.txt` 来管理所有的属性配置。但随着项目越做越大，一个 `CMakeLists.txt` 在可读性、可维护性上都会有所降低。为此需要对每个特定的模块编写 `CMakeLists.txt`。可以参考 OpenCV 的 `CMakeLists.txt` 布局。



```
.
├── cmake
│   └── ...
├── CMakeLists.txt
├── include
│   ├── CMakeLists.txt (用于全局包含的 opencv.hpp 的导出)
│   └── ...
├── modules
│   └── core
│       ├── CMakeLists.txt
│       ├── include
│       │   └── ...
│       ├── src
│       │   └── ...
│       └── test
│           └── ...
├── README.md
└── samples
```

很显然，每级模块（目录）的文件夹下都会存在一个 `CMakeLists.txt`。

- 最底层的 `CMakeLists.txt` 是为了管理该模块功能的编译、测试文件编译，以及提供指定的接口（可以是库，也可以是若干 CMake 变量）给外部。OpenCV 中使用语句 `ocv_add_module()` 来添加模块。例如：

  

  ```
  # 定义于 <opencv-path>/modules/core/CMakeLists.txt 中的内容
  ocv_add_module(core
                 OPTIONAL opencv_cudev
                 WRAP java objc python js)
  ```

  这个语句并不是 CMake 原生语法中自带的，而是项目自行设计的宏。CMake 中的宏与C/C++中使用 `#define` 创建的宏原理类似，都是起到了文本替换的作用。根据这个宏以及此 `CMakeLists.txt` 的其余功能，可以创建该 `core` 模块的目标：`opencv_core`，并为此添加合适的依赖项。

- OpenCV 4.x 系列版本设置两级目录的 `CMakeList.txt`。根目录下即最高级目录的 `CMakeLists.txt` 一般是对于整个项目有关编译选项的设置，以及对系统、平台、编译器等编译环境信息的兼容处理。OpenCV 项目根目录的 `CMakeLists.txt` 前一大段都是在对当前平台、编译配置的信息做处理，例如：

  

  ```
  # 定义于 <opencv-path>/CMakeLists.txt 中的内容
  if(CMAKE_SYSTEM_NAME MATCHES WindowsPhone OR CMAKE_SYSTEM_NAME MATCHES WindowsStore)
    cmake_minimum_required(VERSION 3.1 FATAL_ERROR)
    cmake_policy(VERSION 2.8)
  else()
    cmake_minimum_required(VERSION "${MIN_VER_CMAKE}" FATAL_ERROR)
  endif()
  ```

## [2. 其余 CMake 文件](https://www.cccolt.top/tutorial/cmake/02.html#_2-其余-cmake-文件)

CMake 中除了解析 CMakeLists.txt 文件，还允许解析扩展名为 `.cmake` 的文件。一般有两种操作可以对这类文件进行访问，分别是

1. `find_package()`，用于发现第三方依赖库
2. `include()`，用于包含本地的或 CMake 标准中提供的 `*.cmake` 文件，一般这类文件会提供一系列宏或函数来辅助我们完成建构档的生成。

其中第一种方案会在[【08】find_package 详解](https://www.cccolt.top/tutorial/cmake/08.html)中介绍。

### [2.1 `*.cmake` 文件](https://www.cccolt.top/tutorial/cmake/02.html#_2-1-cmake-文件)

OpenCV 中，自定义的`*.cmake`文件全部存放在`<opencv-path>/cmake`文件夹下，常见的名称有

- `Findxxx.cmake`
- `OpenCVxxx.cmake`

第一种是本地项目中用于 `find_package()` 的，例如 FindONNX.cmake 等文件。第二种则是添加了各种各样的能够减少重复操作的宏与函数，`ocv_add_module()` 就是其中之一，他被定义在 `OpenCVModule.cmake` 中，这种一般需要执行 `include()` 操作，例如：



```
# 定义于 <opencv-path>/CMakeLists.txt 中的内容
# --- OpenCL ---
if(WITH_OPENCL)
  include(cmake/OpenCVDetectOpenCL.cmake)
endif()

# --- Halide ---
if(WITH_HALIDE)
  include(cmake/OpenCVDetectHalide.cmake)
endif()

# --- VkCom ---
if(WITH_VULKAN)
  include(cmake/OpenCVDetectVulkan.cmake)
endif()

# --- WebNN ---
if(WITH_WEBNN)
  include(cmake/OpenCVDetectWebNN.cmake)
endif()
```

### [2.2 `*.in` 文件](https://www.cccolt.top/tutorial/cmake/02.html#_2-2-in-文件)

`*.in` 模板文件一般是用于在 cmake 执行阶段（准确来说，应该是配置阶段）将其嵌入的 CMake 变量、列表展开，并生成目标文件的过程中的。这一步通常是由 `configure_file()` 完成，会在[【06】变量参与 C++ 的编译](https://www.cccolt.top/tutorial/cmake/06.html) 中进行介绍。`*.in` 的后缀是约定俗成的，当然也可以使用其他形式的后缀。

OpenCV中的此类文件一般放在 `<opencv-path>/cmake/template` 文件夹下，主要是用于在执行



```
cmake ..
```

的时候将模板文件转换为具体的文件。另外，在



```
cmake --install .
```

的时候会将生成的 `OpenCVConfig.cmake` 等一系列文件安装至目标路径下，而 `OpenCVConfig.cmake` 文件的产生，就需要 `<opencv-path>/cmake/template` 文件夹下的 `OpenCVConfig.cmake.in` 文件。

总结

1. 项目的根目录以及各模块都会管理一个 `CMakeLists.txt` 文件
2. CMake 还会解析 `*.cmake` 后缀的文件
3. `*.in` 文件一般作为模板文件，一般在 CMake 解析期间转换为具体的文件