# CMake自定义编译选项

**前言**

  CMake 允许为项目增加编译选项，从而可以根据用户的环境和需求选择最合适的编译方案，即用户定义自定义编译选项，这样用户可以在编译时选择是否开启某项特性。

下面实例入手总结CMake的自定义编译选项：

  考虑一个简单的C语言项目，我们想让用户决定是否启用一个名为FEATURE_X的自定义特性。如果启用，我们将编译带有特殊行为的代码；如果不启用，我们将编译默认行为的代码。

**一、目录结构**

```
project
│
├── CMakeLists.txt
├── include
│   └── feature_x.h
└── src
    ├── feature_x.c
    └── main.c
```

**二、步骤详解**

**1、编写源代码**

**feature_x.h**

```
#ifndef FEATURE_X_H
#define FEATURE_X_H

void feature_x(void);

#endif
```

**feature_x.c**

```
#include "feature_x.h"
#include <stdio.h>

void feature_x() {
    printf("Feature X is enabled!\n");
}
```

**main.c**

```
#include <stdio.h>

#ifdef USE_FEATURE_X
#include "feature_x.h"
#endif

int main() {
#ifdef USE_FEATURE_X
    feature_x();
#else
    printf("Feature X is not enabled.\n");
#endif
    return 0;
}
```

**2、配置CMakeLists.txt**

```
cmake_minimum_required(VERSION 3.10)
project(DemoProject)
# 添加编译选项
option(USE_FEATURE_X "Use feature X" OFF)

# 根据选项决定是否添加编译定义
if(USE_FEATURE_X)
    add_definitions(-DUSE_FEATURE_X)
endif()

# 指定头文件的搜索路径
include_directories(${PROJECT_SOURCE_DIR}/include)

# 指定源文件
add_executable(DemoProject src/main.c)
# 当USE_FEATURE_X启用时，指示CMake包含feature_x.c文件
if(USE_FEATURE_X)
    target_sources(DemoProject PRIVATE src/feature_x.c)
endif()
```

**3、构建项目**

**不使用FEATURE_X特性构建项目**

```
cd project
mkdir build && cd build
cmake ..
make
```

**启用FEATURE_X特性构建项目**

```
cd project/build # 假设你已经在build目录内
cmake .. -DUSE_FEATURE_X=ON
make
```

  需要注意的是，一旦在运行 cmake 时指定了某个选项的值，它将被缓存起来，并在随后的配置运行中使用，除非通过命令行再次明确设置或使用 ccmake 或 CMake GUI 清除缓存。缓存的目的是为了避免每次运行 cmake 命令时都必须重新指定工程的配置选项。

**三、解析**

> 1、option(USE_FEATURE_X “Use feature X” OFF)
>
>   这一行在CMake中定义一个编译选项USE_FEATURE_X，默认值为OFF。它允许用户在命令行通过-DUSE_FEATURE_X=ON来启用该特性。

> 2、add_definitions(-DUSE_FEATURE_X)
>
>   这一行在编译时定义一个预处理器宏USE_FEATURE_X，使得源码中的#ifdef USE_FEATURE_X判断为真，从而包含和执行feature_x相关的代码。

> 3、target_sources(DemoProject PRIVATE src/feature_x.c)
>
>   这一行告诉CMake，如果USE_FEATURE_X被开启，应该将feature_x.c文件包含到目标DemoProject的源文件列表中。

> 4、include_directories(${PROJECT_SOURCE_DIR}/include)
>
>   通过在CMakeLists.txt中使用include_directories()，我们添加了一个额外的目录到编译器的头文件搜索路径。这意味着在编译时，编译器会在指定的目录下查找头文件。这里我们指定了project/include目录

  以上就是在CMake中使用自定义编译选项的一个示例。通过这种方式，你可以轻松地为你的项目添加可配置的特性，提高代码的可复用性和可维护性。

  这是一种使用自定义编译选项的方式，还有另一种方式是使用config文件来进行自定义编译选项，之后的文章我们继续详细总结。