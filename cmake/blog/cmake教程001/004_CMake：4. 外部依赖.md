# CMake：4. 外部依赖

上一篇文章我们讨论了如何使用 CMake 的控制流命令进行条件性编译。



接下来，这篇文章会讨论 CMake 乃至 C++ 项目开发中的重要问题：依赖管理。众所周知，因为 C++ 的历史过于悠久，在其诞生的时候，其生态中缺少一个像 npm、pip 这样的，所有开发者一致采用的包管理工具。不同的操作系统也有不同的组织 C++ 项目的方法。因此管理依赖就成了所有 C++ 开发者最头疼的问题，没有之一。

有人可能会问，C++ 项目管理依赖为什么这么困难呢？

因为它不仅仅是 C++ 这个语言自己的问题——它是 C++、操作系统、编译器所有这些问题杂糅在一起，扭曲缠结而成的一个超大问题！

## C/C++ 库的发布方式

C/C++ 库有两种发布方式：**源码**（source-only）发布和**预构建二进制**（prebuilt binary）发布。源码发布比较简单，开发者直接把源码和项目文件给你，你自己在自己的机器上构建。预构建二进制则是，库开发者帮你编译好，然后把头文件、库打包给你。这两种构建方式并没有说哪种更好，他们都有各自的优点。

### 源码发布

源码发布的优势在于，你可以去掉你不需要的组件，减少依赖的大小；也可以定制编译选项针对平台进行优化。但是相应的，你会需要花很长时间等他编译完，尤其是在一些不好 cross-compile 的嵌入式平台上。而且不同的项目用的构建系统不一样，有的可能用 Makefile，有的可能是 CMake，有些比较烦的你还得开个 Docker 容器在里面跑 Bazel。如果你对构建系统不是那么熟悉的话，折腾源码依赖会让人非常崩溃。

源码发布有一个特殊情况，就是这个库他**只有头文件**（header-only）。这是我最喜欢的 C++ 库形式。因为他整个库里只有头文件，你不用预编译任何东西，也不用链接任何东西，只要把几个头文件复制进项目里 include 就完事了。而且因为头文件里的代码基本都是**内联**（inline）的，你如果没有用到某些功能，编译器在做**死代码消除**（dead code elimination）的时候就会直接把它们去掉，也就不会产生任何二进制体积。当然，这是一种最理想的情况，但是绝大多数库还是需要链接的。

### 预购建二进制发布

预购建二进制发布则相反，不管他项目代码怎么写的，最终到你手上的就是**头文件**（header）和**库文件**（library），在你的项目配置里加上依赖的**头文件包含目录**（include directory）和**链接目标**（link target）就可以了，非常简单。然而，因为构建是库开发者做的，他们不知道你会用到库里哪些功能，一般都会把所有功能全部编出来，整个依赖的体积就会变得非常大。另一个问题是，你没有办法调试它的代码。如果什么东西崩在它的代码里，错误处理和文档又写得不好，你还是得搞一份他的源码来调试。除此之外，如果他们编出来的库如果依赖了环境里的什么动态链接库（比如某个版本的 Visual C++ Redistributable）又没有把这个库一起打包给你，而你本地没有安装，你就算构建成功了也没法运行。

![img](./assets/v2-7edf83ca01177eae5ffe4b7b052dc6f2_1440w.webp)

找不到动态链接库的例子

## 不同操作系统下的软件包结构

不同的操作系统对 C++ 项目的组织方法会不太一样，这与操作系统处理动态链接的方式有关。篇幅所限，在写这段的时候我假设你已经知道静态/动态链接是怎么回事了，下次可以单开篇文章聊聊链接。

### 静态库

各个操作系统的**静态库**（static library）基本是一致的。静态库基本上就是编译好的源码打个包发给你，你把它放进项目之后，编译期还要再做一次链接。目录结构大概是这样：

| 子目录   | 扩展名 | 描述                               |
| -------- | ------ | ---------------------------------- |
| /include | .h     | 头文件，有静态库中所有符号的签名。 |
| /lib     | .a     | Linux、macOS 静态库文件。          |
| /lib     | *.lib  | Windows 静态库文件。               |

### Linux 动态库

Linux 下的动态库又叫做**共享对象**（shared object），在使用上和静态库区别不大。

注意，不论是哪个平台的动态库，都不要忘了把你依赖的动态库都一起发布出去。

| 子目录   | 扩展名 | 描述                               |
| -------- | ------ | ---------------------------------- |
| /include | .h     | 头文件，有动态库中所有符号的签名。 |
| /lib     | .so    | Linux 共享对象文件。               |

### macOS 动态库

macOS 的**动态库**（dynamic library）和 Linux 的共享对象基本是一个东西，所以你把扩展名写成 `.so` 或者 `.dylib` 它都认。

有趣的是，在 WWDC20 大会上，Apple 为了给 M1 芯片的生态开路，在 Xcode 工具链中支持了**通用二进制**（universal binary）动态库。在一个动态库中可以放置多种 CPU 架构（比如 x64 和 arm64）的字节码。

| 子目录   |            |                |
| -------- | ---------- | -------------- |
| /include | .h         | 头文件。       |
| /lib     | .dylib/.so | macOS 动态库。 |

### Windows 动态库

Windows 下的**动态链接库**（**D**ynamic-**L**ink **L**ibrary）比较特殊。他其实不是一个库，而是一个可执行的二进制文件。DLL 有自己的入口点叫做 [DLLMain](https://link.zhihu.com/?target=https%3A//learn.microsoft.com/en-us/windows/win32/dlls/dllmain)，在加载、卸载动态库之类事件发生时会先执行入口点里的代码，大家平时可能不会意识到这一点，因为在你没有显式实现的时候，编译器会自动套一个默认的。

但也因为它是一个二进制文件，DLL 不能直接参与链接。于是 M$VC 会再生成一个没有任何可执行内容，只有函数符号的**导入库**（import library），帮助编译器检查有没有链接到不存在的符号。这个奇葩设计让 Windows 动态库包的结构和其他人有点不一样：

| 子目录   |      |                      |
| -------- | ---- | -------------------- |
| /include | .h   | 头文件。             |
| /lib     | .lib | Windows 导入库。     |
| /bin     | .dll | Windows 动态链接库。 |

## 不同平台下软件包安装的位置

除了软件包内的结构不一样，不同平台放软件包的位置也有不一样的惯例。

Linux 和 macOS 都属于 *nix 平台，沿袭了 Unix 的设计，把所有的平台软件全部放进公共空间的 `/lib` 和用户空间的 `/usr/lib` 里。优点是用户安装软件之后，库的位置对平台上所有的其他软件可见，用户不需要手工指定库的未知。

然而这个优点有时会变成缺点。因为所有软件都在这两个目录下找库，一旦软件对库版本有硬性要求，用户就得绞尽脑汁想办法让操作系统去加载那个正确版本的库。又因为 Linux 下符号默认会加载到进程的全局空间，一旦有两个库有重名的符号但是实现不一样，又会出现**段错误**（segmentation fault）一类的问题。

Windows 为了避免这个依赖问题，要求所有应用程序（包括独立发布的 DLL）都自成一体，开发者应该把所有 `.exe`、`.lib`、`.dll`、`.h` 全部放在一个软件自己的独立目录里。macOS 也推出了一个相似的概念叫**框架**（Framework），除了打包 `.dylib` 和 `.h` 以外，它还规定了软件目录中存放框架标识符、版本和其他资源文件的方式。

除此之外，微软的 Visual Studio 和 Apple 的 Xcode 都属于**多配置构建器**（multi-config builder），用不同的配置构建出来会放到不同的目录里。比如 Debug 编译出来的会放在 `build/Debug` ，Release 构建的会放在 `build/Release`。而 Linux 下常用的 Makefile 或者 Ninja 并不进行这样的区分，属于**单配置构建器**（single-config builder），构建出来的东西统统放在 `build` 下面。这给我们管理依赖又添加了难度。

## 在 CMake 中解决依赖问题

尽管有上面这么多麻烦的问题，这个班还得上啊。接下来我会教大家在 CMake 中正确地添加依赖。

### 子目录依赖

对于**源码发布**的项目来说，子目录依赖是最简单的方式。如果你的依赖也是 CMake 写的项目文件，你可以使用 `add_subdirectory` 引入依赖中定义的**构建目标**（Target）。

一个常见的用法是使用 `git submodule` 把依赖作为子模块加入到 repo 中，然后用 `add_subdirectory` 直接添加。当然，你想直接拷贝一份代码到你的项目里也是可以的，只是以后升级依赖的时候可能会有几千个文件更新，比较脏。

这里我们以 3D 素材加载库 Assimp 举例。先把 Assimp 作为子模块引入到 Git。

```bash
git submodule add https://github.com/assimp/assimp third/assimp
```

然后在 CMake 中加入下面的脚本。

```cmake
# 覆写 assimp/CMakeLists.txt 中的一个选项，让 assimp 构建完了之后不要自动打包。
set(ASSIMP_INSTALL OFF CACHE BOOL "" FORCE)
# 把 assimp 作为子目录加入当前项目。
add_subdirectory(${PROJECT_SOURCE_DIR}/third/assimp)
# 把 assimp 的构建目标注册为链接库，CMake 会用 assimp 构建出来动态库文件跟我们的构建目标链接。
target_link_libraries(${PROJECT_NAME} PRIVATE assimp)
# 注册头文件包含目录。注意 assimp 会在 CMake 配置的时候生成部分头文件，不要忘了包括 assimp 生成目录中的 include。
target_include_directories(${PROJECT_NAME} PRIVATE
    ${PROJECT_SOURCE_DIR}/third/assimp/include
    ${PROJECT_BINARY_DIR}/third/assimp/include)
```

现在你每次构建项目的时候，cmake 都会先保证 Assimp 已经被编译，然后用编译好的 Assimp 你的目标链接。

### 配置期下载源码

在 CMake 3.11 中新增了一个叫做 **FetchContent** 的工具。FetchContent 用于在 CMake 跑脚本配置项目的时候即时下载一些文件，也是适用于**源码发布**的项目。下载目标可以是 Git 仓库的某个 commit，也可以是某个 URL 下的代码压缩包，但它们的内容都必须是 CMake 定义的项目——根目录都必须有 CMakeLists.txt 。

FetchContent 简单来说分为两步：**声明**（declare）和**预备**（make available）。这里我们以 Google Test 为例：

```cmake
# 引用 FetchContent 模块。
include(FetchContent)
# 声明 googletest 的位置和下载方式（git）。这一步不涉及网络。
FetchContent_Declare(
  googletest
  GIT_REPOSITORY https://github.com/google/googletest.git
  GIT_TAG        703bd9caab50b139428cea1aaff9974ebee5742e # release-1.10.0
)
# 下载并解压。
FetchContent_MakeAvailable(googletest)
```

使用 FetchContent 下载的项目会被解压释放到 `build/_deps/googletest-src`，并使用 `add_subdirectory` 包含到当前的环境里。接下来按照 `add_subdirectory` 中的说明添加依赖即可。

```cmake
# gtest 是 googletest 中定义的构建目标。
target_link_libraries(${PROJECT_NAME} PRIVATE gtest)
```

FetchContent 和 submodule 又孰优孰劣呢？在逻辑上我其实比较倾向于 FetchContent。因为 FetchContent 拉下来的只有你指定的那个 commit，但初始化 submodule 的时候指定 shallow clone 还是比较麻烦的，很容易就会把仓库里所有的 commit 全都给拉了下来导致下载非常非常慢。但在国内的网络质量下……我个人感觉 FetchContent 反而更不顺手。每次把 build 目录删掉他就得重新下载一次，反而更花时间，不如 submodule 只在第一次配项目的时候花点时间了。

当然，要省下载时间当然是直接把第三方代码 commit 到自己仓库里来的快了。

### 查找已安装的二进制依赖

很多像 Qt、CUDA Toolkit、LLVM 一类的大型软件，构建一次就要一个小时甚至几天，不太可能翻来覆去从源码编译。所以一般会在系统的层面安装一次**预构建二进制**，然后在 CMake 中指定这些软件的安装位置。

不过所幸这些软件的安装位置往往比较固定。比如 Linux 或者 macOS 选手可能直接用 apt、homebrew 这些系统级包管理器装软件，那就有可能存在 `/usr/lib`或者 `/opt/homebrew/lib` 。Windows 下按照惯例都装在 `C:/Program Files`。有的时候用 Installer 安装的软件还会写入注册表，或者定义一个环境变量（比如 `CUDA_PATH`）。根据这些惯例，CMake 开发者们设计了许多自动在当前环境中寻找软件包的脚本，也就是 `Find[PACKAGE].cmake`，下称 **Find 脚本**。

你可以使用 `find_package` 命令调用 Find 脚本在当前系统环境中寻找软件包。这里我们以 Vulkan SDK 为例：

```cmake
# 在环境中寻找 Vulkan SDK 的安装位置。
# REQUIRED 参数意味着如果找不到 Vulkan SDK，CMake 会报错中止配置。
# 如果找到了，FindVulkan.cmake 会将 Vulkan_FOUND 定义为 TRUE。
find_package(Vulkan REQUIRED)
# 引入 Vulkan 构建对象。
target_link_libraries(${PROJECT_NAME} PRIVATE Vulkan)
```

上面的命令会调用 `FindVulkan.cmake` 进行查找。这个脚本是内置在 CMake 中的，在 CMake 3.7 以后的版本中都可以使用。这些 CMake 内置脚本的文档在官网都可以找到：[https://cmake.org/cmake/help/latest/module/FindVulkan.html](https://link.zhihu.com/?target=https%3A//cmake.org/cmake/help/latest/module/FindVulkan.html)

当然了，`find_package` 并不是只能执行 CMake 官方集成的 Find 脚本。如果它没有在 CMake 内置的脚本中找到对应的 Find 脚本，他会继续在 `CMAKE_MODULE_PATH` 定义的目录中查找。以 Taichi C-API 为例，你可以下载一份 [FindTaichi.cmake](https://link.zhihu.com/?target=https%3A//github.com/taichi-dev/taichi/blob/master/c_api/cmake/FindTaichi.cmake) 到你项目的 `cmake` 目录，然后在你的脚本中添加如下内容：

```cmake
set(CMAKE_MODULE_PATH ${PROJECT_SOURCE_DIR}/cmake)
find_package(taichi REQUIRED)
# 引入 taichi runtime 的构建对象。
target_link_libraries(${PROJECT_NAME} PRIVATE taichi::runtime)
```

不管是官方集成的脚本还是用户自己写的，一般都会定义三个变量：

- 头文件包含目录：`Xxx_INCLUDE_DIRS`
- 链接库目录：`Xxx_LIBRARIES`
- 指示有没有找到库：`Xxx_FOUND`

Find 脚本定义的其他变量一般都在写在脚本开头的文档里。不同的脚本风格不一样，每次使用之前最好都看看。

另外值得注意的是，Find 脚本定义的**没有全大写的变量**大多数情况是**构建对象**。还记得第一课讲的 `PRIVATE`、`PUBLIC` 属性吗？



在你用 `target_link_libraries` 注册依赖的时候，`taichi::runtime` 的 `PUBLIC` 项目会被一并注册到你的构建目标下面。也就是说，绝大多数情况，你不需要再用 `target_include_directories` 注册依赖的包含目录了，非常方便。

换句话说，大多数情况下，下面的代码是等价的：

```cmake
find_package(Vulkan REQUIRED)
target_link_libraries(${PROJECT_NAME} PRIVATE Vulkan)

# - 或者 -

find_package(Vulkan REQUIRED)
target_link_libraries(${PROJECT_NAME} PRIVATE ${Vulkan_LIBRARIES})
target_include_directories(${PROJECT_NAME} PRIVATE ${Vulkan_INCLUDE_DIRS})
```

不过，有朋友可能会问，为啥上面我们用 `add_subdirectory` 引入的构建对象还得手动注册 include directory 呢？

其实可能也不用，但是 CMakeLists.txt 普遍都写得比较脏，可能有些包含目录本来应该是 `PUBLIC` 的它写成 `PRIVATE`的了。Find 脚本本身就是给库用户用的，会比较照顾大家用起来的感受。当然，这只是一种经验之谈，不一定对。

## C++ 的包管理器：vcpkg

比较时髦的朋友可能最近已经在项目里用上 vcpkg了。vcpkg 是微软设计的一个帮助 CMake 项目管理 C++ 依赖的工具。

[Get started with vcpkgvcpkg.io/en/getting-started](https://link.zhihu.com/?target=https%3A//vcpkg.io/en/getting-started)

其实简单来说，vcpkg 做的事情和我们上面讨论过的差不多：

- 从 vcpkg 的源下载软件源码
- 针对平台对源码进行一些修改
- 托管软件包的查找过程

### 安装 vcpkg

和我们以往装软件的方式不一样，我们得把他的整个 repo clone 到项目目录里（不需要切换到另一个目录），然后跑脚本让他下载 `vcpkg`：

```bash
# clone 整个项目。
git clone https://github.com/Microsoft/vcpkg.git
# 运行安装脚本，它会从网上下载 vcpkg 的可执行文件。
./vcpkg/bootstrap-vcpkg.sh # Linux & macOS
./vcpkg/bootstrap-vcpkg.bat # Windows
```

官方也推荐直接把 vcpkg 作为 submodule 放在项目里。

### 使用 vcpkg

安装完成后，我们就可以用 vcpkg 装包了。我们可以在官网浏览 vcpkg 收录的 C++ 项目：

[Browse public vcpkg packagesvcpkg.io/en/packages](https://link.zhihu.com/?target=https%3A//vcpkg.io/en/packages)

这里我们以矩阵代数库 glm 为例：

```bash
# 通过 vcpkg 中安装 glm。
./vcpkg install glm
```

安装完成最后会打印一段简短的软件包使用指南：

```text
[...]
Total install time: 8.4 s
glm provides CMake targets:

  # this is heuristically generated, and may not be correct
  find_package(glm CONFIG REQUIRED)
  target_link_libraries(main PRIVATE glm::glm)
```

我们关注最后两行就行。意思是，只要我们在 CMakeLists.txt 中加入它给的这段代码就可以添加对 glm 的依赖了。这就抄进去：

```cmake
# 通过 vcpkg 查找
find_package(glm CONFIG REQUIRED)
target_link_libraries(${PROJECT_NAME} PRIVATE glm::glm)
```

注意配置项目的时候要在 cmake 命令后加上 vcpkg 的工具链，不然 CMake 还会用自己的逻辑在系统里找包。

```bash
cmake -B build -S . "-DCMAKE_TOOLCHAIN_FILE=./vcpkg/scripts/buildsystems/vcpkg.cmake"
```

## 结语

写到这里这篇文章都已经八千多字了，在写之前完全没想到会这么长。累死了…

这篇文章简单介绍了 C++ 项目中依赖管理的复杂性，介绍了 CMake 中引入其他软件包常用的命令和工具，并顺便介绍了微软推出的 C++ 软件包依赖管理工具 vcpkg。