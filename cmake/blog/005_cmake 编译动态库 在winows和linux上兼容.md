# cmake 编译动态库 在winows和linux上兼容

要在 Windows 和 Linux 上使用 CMake 编译一个兼容的动态库，您可以遵循以下步骤。下面提供了一个示例项目，包括项目结构、CMake 配置和源代码。

### 项目结构

假设您的项目结构如下：

```
/my_library
    /src
        my_library.cpp
        my_library.h
    CMakeLists.txt
```

### 1. 创建 `CMakeLists.txt` 文件

在根目录下创建或编辑 `CMakeLists.txt` 文件，内容如下：

```
cmake_minimum_required(VERSION 3.10)
project(MyLibrary)
# 设置 C++ 标准
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED True)
# 查找源文件
file(GLOB SOURCES "src/*.cpp")
include_directories(src)
# 定义动态库类型（共享库）
add_library(my_library SHARED ${SOURCES})
# 在 Windows 上设置导出符号定义
if(WIN32)
    target_compile_definitions(my_library PRIVATE MY_LIBRARY_EXPORTS)
endif()
# 安装目标（可选）
install(TARGETS my_library DESTINATION lib)
install(FILES src/my_library.h DESTINATION include)
```

### 2. 编写源代码

#### **my_library.h**

```
#ifdef MY_LIBRARY_EXPORTS // Only defined when building the library
#define MY_LIBRARY_API __declspec(dllexport) // Windows: export symbol
#else
#define MY_LIBRARY_API __declspec(dllimport) // Windows: import symbol 
#endif
extern "C" {
    MY_LIBRARY_API void hello();
}
```

#### **my_library.cpp**

```
#include "my_library.h"
#include <iostream>
void hello() {
    std::cout << "Hello from my library!" << std::endl;
}
```

### 3. 编译指令

#### 在 Linux 上:

打开终端并运行以下命令：

```
mkdir build && cd build          # 创建构建目录并进入其中
cmake ..                         # 配置项目
make                             # 编译生成库文件 (libmy_library.so)
```

#### 在 Windows 上:

在命令提示符或 PowerShell 中执行以下命令（确保已经安装 Visual Studio 或者合适的 C++ 开发环境）：

```
mkdir build && cd build         # 创建构建目录并进入其中
cmake ..                        # 配置项目 
cmake --build .                 # 编译生成库文件 (my_library.dll)
```

### 注意事项

1. **Windows 特有指令**：
   - 使用 `__declspec(dllexport)` 和 `__declspec(dllimport)` 来控制符号的导入和导出。这对于生成 DLL 是必要的，以便其他程序能够调用该动态库中的函数。
2. **平台差异处理**：
   - 您可能会根据操作系统使用不同的特性。在本示例中，我们通过检查 `WIN32` 来判断当前是否在 Windows 系统上。
3. **测试与验证**：
   - 建议编写一个小的测试程序来调用这个动态库，以验证其功能是否正常。可以创建另一个简单的应用程序来链接到这个库并调用 `hello()` 函数。
4. **CMake版本要求**：
   - 确保您的 CMake 版本是相对较新的（建议 3.10 或更高），以支持更多功能和改善兼容性。
5. **安装路径配置**：
   - 如果需要将编译出的库安装到系统目录，可以使用 `install()` 指令，这样可以方便后续的使用和部署。

通过上述步骤，您应该能够成功创建一个在 Windows 和 Linux 上均可兼容使用的动态库。如有进一步问题，请随时询问！