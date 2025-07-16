# CMake：1. 基本要素

我的朋友们都有各自爱用的构建系统，有喜欢 Bazel 的，有用 xmake 的，甚至有个老哥能手写 MSBuild 项目文件（谢谢你 UE4）。但大家普遍都对 CMake 表示深恶痛绝，这些新潮的构建系统的簇拥也普遍想把 CMake 赶出他们的项目，寻求一个有正经包管理的、脚本好写的、速度快的替代品。

然而现实就是，主流的 C++ 开源项目全都在用 CMake，Python 和 Java 这些虚拟机语言如果要在自己的大包工具里直接构建 native 代码和脚本，很多都要靠 scikit-build、Gradle 这些构建系统隔空调一下 CMake。所以不管你喜不喜欢 CMake，他就是通用 C++ 构建系统的版本答案，他的触手已经探入工程世界的方方面面。你是永远也躲不掉他的，所以打不过，就加入它吧——至少事情甩到你头上了，你得自己擦干净不是？

所以为了减轻各位的痛苦，今天抽空写一篇文章给大家简单介绍一下 CMake。讲一讲它是什么，一个常见项目文件（CMakeFiles.txt）的结构，一些工程上的 best practice，以及我的一些习惯。

## CMake 的历史

你可能会觉得「CMake 这个垃圾东西有什么历史好讲的」。你说的对，但是：

> CMake是为了解决美国国家医学图书馆出资的Visible Human Project项目下的Insight Segmentation and Registration Toolkit（ITK）软件的跨平台建构的需求而创造出来的，[…]，Brad King为了支持CABLE和GCC-XML这套自动包装工具也加了几项功能，通用电气公司的研发部门则用在内部的测试系统DART，还有一些功能是为了让VTK可以过渡到CMake和支持**洛斯阿拉莫斯国家实验室**的Advanced Computing Lab的平行视觉系统ParaView而加的。

首先，CMake 的开发商 Kitware 是可视化领域的元老，CMake 支持的功能是从非常成熟的商业项目中迭代出来的。所以 CMake 一定包含你可能会需要的任何功能，你觉得缺功能，很可能是因为你没找到位置。其次注意这个「洛斯阿拉莫斯国家实验室」……

> **洛斯阿拉莫斯国家实验室**（英语：Los Alamos National Laboratory，缩写：**LANL**；前称“**Y计划**”、**洛斯阿拉莫斯实验室**、**洛斯阿拉莫斯科学实验室**）是美国承担核子武器设计工作的两个国家实验室之一，另一个是劳伦斯利弗莫尔国家实验室（始于1952年）。[2][3]洛斯阿拉莫斯国家实验室建立于1943年曼哈顿计划期间，最初负责原子弹的制造、由伯克利加州大学负责管理，首任主任是“原子弹之父”罗伯特·奥本海默。[2][4][5]

……WTF？

## 使用 CMake 进行构建

在学习 CMake 脚本的编撰之前，我们先从宏观视角简单过一下 CMake 本身的使用。我通常用下面的命令构建从 Github 上 clone 下来的代码仓库：

```bash
cd /path/to/repository # 进入代码仓库的根目录，也就是置有 CMakeLists.txt 的目录位置。
mkdir build # 创建一个叫 build 的构建目录
cmake . -B build # 生成 cmake 项目；所有生成出来的文件会被保存到 build 目录中
cmake --build build # 使用 build 目录下产生的项目文件进行构建
```

## CMakeLists.txt 的基本结构

一个最基本的 CMakeLists.txt 文件长这个样子：

```cmake
cmake_minimum_required(VERSION 3.24)
project(ExampleProject)
```

首先第一行 `cmake_minimum_required` 约定了这个项目只能通过版本高于 3.24 的 CMake 进行构建。这是我的个人习惯，一般比较老的平台（比如上一个版本的 NDK）3.10 会比较通用。

第二行的 `project` 命令声明了一个**项目**（project）。这个命令会将 `PROJECT_SOURCE_DIR` 设定为 CMakeLists.txt 所在的目录，这是项目概念上的源码目录；`PROJECT_BINARY_DIR` 设定为你前面通过 `-B` 传入的构建目录（如果使用上一小节列出命令构建则是 `build`），这是概念上的输出目录，所有的中间文件和编译结果都（应该）被放在这里。

我们通常使用关于这两个特殊目录的相对路径指定文件位置。比如说，如果我想引用项目目录下 `src/main.cpp` 这个文件，就可以使用 `${PROJECT_SOURCE_DIR}/src/main.cpp` 进行指定。其中 `${PROJECT_SOURCE_DIR}` 是对 `PROJECT_SOURCE_DIR` 这个变量进行**取值**（evaluation）。CMake 的取值仅仅是简单的文本替换。

使用这两个目录变量仅仅是一种习惯，你也可以通过绝对、相对目录或者其他变量来指定。但这样其他人可能就看不懂你在干什么，引起一些额外的问题。

目前为止，我们还没有定义任何**构建目标**（build target）。但是你依然可以通过这条命令让 CMake 进行对他进行生成：

```bash
mkdir build
cmake . -B build
```

CMake 会吐出来下面这些 Log：

```text
-- The C compiler identification is AppleClang 14.0.0.14000029
-- The CXX compiler identification is AppleClang 14.0.0.14000029
-- Detecting C compiler ABI info
-- Detecting C compiler ABI info - done
-- Check for working C compiler: /Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/cc - skipped
-- Detecting C compile features
-- Detecting C compile features - done
-- Detecting CXX compiler ABI info
-- Detecting CXX compiler ABI info - done
-- Check for working CXX compiler: /Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/c++ - skipped
-- Detecting CXX compile features
-- Detecting CXX compile features - done
-- Configuring done
-- Generating done
-- Build files have been written to: /Users/penguinliong/Repositories/cmake-101/01-minimal-project/build
```

其中 `Configuring done` 和 `Generating done` 是两个重要的节点。其中 `Configuring` 指的是执行你写的 CMake 脚本进行**配置**（configuration）的过程；而 `Generating` 指的是组织构建目标、分析目标间依赖并**生成**（generation）构建器代码的过程——是的，CMake 本身没有构建的能力，他的构建过程是通过调用外部构建工具，比如 Visual Studio 的 MSBuild、XCode 的 `xcodebuild`或者 GNU Makefile 完成的。**CMake 只负责生成其他构建工具的项目文件。**

让我们仔细看看上面的输出。首先 CMake 会对你当前的环境进行一些检查，看看你用的是什么编译器，版本是什么（因为有的版本有 bug 可能需要 patch）。在 `Check for working C compiler` 这一步 CMake 会编译一个很简单的小程序确保这个编译器能够产出有效的二进制文件。如果你写了其他的配置逻辑，在 `Detecting CXX compile features - Done` 这一行后面还会有别的 log 打印出来 。而当你看到 `Generating done` 这一行的时候，基本可以判断配置成功了。

## 全局配置和针对构建目标的配置

一个 C/C++ 项目的构建目标主要由以下几个部分组成：

- 要构建的**源码文件**（source file）
- **包含目录**（include directory）
- **链接库**（link library）
- **宏定义**（macro definition）

**源码文件**（`.c`, `.cpp`, `.cxx`）**定义**（define）了所有外部可以访问的**符号**（symbol）的具体**实现**（implementation）。简单来说就是**所有函数的实现**，而**符号则是这个函数的名字**。

**包含目录**中的**头文件**（`.h`, `.hpp`）则**声明**（declare）了这些符号，让不同的源码文件能够**共享同一份符号的声明信息**。

**链接库**含有**已经编译好的符号实现**。

**宏定义**用来**选择性编译源码文件中的部分代码**。

当然这只是一个简单的介绍，如果你对这些内容抱有疑问，我觉得你应该从头学习一下 C++ 代码的基本组织结构了。但是真正重要的是，我们如何通过 CMake 脚本向一个构建目标指定这些信息。

### 全局配置

在比较老版本的 CMake 项目中，你可能比较常见到这种形式的脚本：

```cmake
add_definitions(
    -DFOO=1
    -DBAR=2)
include_directories(
    ${PROJECT_SOURCE_DIR}/external/include)
link_libraries(
    ${PROJECT_SOURCE_DIR}/external/lib/libfoo.a)
add_executable(ExampleApplication
    ${PROJECT_SOURCE_DIR}/src/main.cpp)
```

在上面的脚本中：

- `add_compile_definitions` 定义了 `FOO` 和 `BAR` 两个宏；
- `include_directories` 将 `external/include` 添加为包含目录；
- `link_libraries` 将 `external/lib/libfoo.a` 标记为需要链接的库；
- `add_executable` 添加了一个名字叫做 `ExampleApplication` 的构建目标，`main.cpp` 是这个构建目标中的源码文件。**这条命令前的所有配置都会对这个构建目标生效。**

### 针对构建目标的配置

你马上就会意识到，你要是有多个构建目标，这种全局控制的指令会让项目的组织变的非常困难（比如说你有两个库目标想要分别配置宏定义）。所以在新版本的 CMake 中加入了以 `target_` 开头的，针对每个构建目标的配置命令：

```cmake
add_executable(ExampleProject)
target_sources(ExampleProject PRIVATE
    ${PROJECT_SOURCE_DIR}/src/main.cpp)
target_compile_definitions(ExampleProject PRIVATE
    -DFOO=1
    -DBAR=2)
target_include_directories(ExampleProject PRIVATE
    ${PROJECT_SOURCE_DIR}/external/include)
target_link_libraries(ExampleProject PRIVATE
    ${PROJECT_SOURCE_DIR}/external/lib/libfoo.a)
```

这和上一小节中的脚本在功能上是等价的，但是所有配置都只对 `ExampleProject` 这一个目标生效。其中`PRIVATE` 限定了这些配置的传播行为，使这个配置只对当前构建目标生效。如果你把他改成 `PUBLIC`，任何依赖于这个构建目标的目标，也会应用这些 `PUBLIC` 的配置。

### 描述构建目标间的依赖关系

当你的一个项目中存在多个构建目标时，比如有一个**可执行程序**（executable）调用了一个功能**库**（library）里的一些接口，那么他们之间就存在着**依赖关系**（dependency），你可以通过如下脚本描述他们之间的依赖关系：

```cmake
add_library(TheLibrary STATIC)
add_executable(TheExecutable)
target_link_libraries(TheExecutable PRIVATE TheLibrary)
```

是的，还是 `target_link_libraries`。但是 CMake 会意识到 `TheLibrary` 是一个构建目标，所以在链接这个库之前优先构建 `TheLibrary`，并把它产生的 artifact 作为库依赖链入 `TheExecutable` 。

## 结语

那么到这里，你已经掌握了 CMake 最为基本的用法。我们在未来的文章中会继续讨论 CMake 的跨平台编译、控制流、第三方依赖及其他技巧。

顺便打个广告。对**图形技术**、**深度学习部署**或者**高性能计算**感兴趣的朋友，可以加入这个 QQ 群：745821200。

## 

## [附录 A] CMakeLists.txt 模版

最后的最后，对于没有外部依赖的简单项目，下面这份 CMakeLists.txt 模版已经足够了。

```cmake
cmake_minimum_required(VERSION 3.24)

project(ExampleProject)

add_executable(ExampleProject)
target_sources(ExampleProject PRIVATE
    ${PROJECT_SOURCE_DIR}/src/main.cpp)
target_compile_definitions(ExampleProject PRIVATE
    -DFOO=1
    -DBAR=2)
target_include_directories(ExampleProject PRIVATE
    ${PROJECT_SOURCE_DIR}/external/include)
target_link_libraries(ExampleProject PRIVATE
    ${PROJECT_SOURCE_DIR}/external/lib/libfoo.a)
```

