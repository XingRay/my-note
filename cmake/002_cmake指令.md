# CMake 命令行参数全解析

## 1 核心配置参数

### 1.1 变量定义参数

```shell
-D <var>:<type>=<value>
```

动态设置 CMake 缓存变量，支持布尔值、字符串、路径等类型。例如：

```shell
cmake -DCMAKE_BUILD_TYPE=Release -DBUILD_TESTS=ON
```

`CMAKE_BUILD_TYPE`：控制构建类型（Debug/Release/RelWithDebInfo）

`BUILD_SHARED_LIBS`：全局控制生成动态库或静态库



### 1.2 生成器选择

```shell
-G <generator>
```

指定构建系统生成器，支持多平台适配：

```cmake
cmake -G "Ninja"          # 生成 Ninja 构建文件
cmake -G "Visual Studio 17 2022"  # 生成 VS 解决方案
```

其他常用选项：`"Unix Makefiles"`、`"Xcode"`、`"Ninja Multi-Config"`

```cmake
cmake -G "Unix Makefiles"
cmake -G "Xcode"
cmake -G "Ninja Multi-Config"
```



## 2 路径控制参数

### 2.1 源码与构建目录

```shell
-S <path>
```

显式指定源码根目录（含 `CMakeLists.txt` 的路径）

```shell
-B <path>
```

设定构建目录（二进制输出目录），避免污染源码：

```cmake
cmake -S src -B build
```

替代传统用法 

```shell
mkdir build && cd build && cmake ..
```



### 2.2 输出路径定制

通过变量定义可执行文件/库的存放路径：

`CMAKE_RUNTIME_OUTPUT_DIRECTORY` 可执行文件输出目录（如 bin/）

`CMAKE_LIBRARY_OUTPUT_DIRECTORY` 动态库输出目录（如 lib/）

`CMAKE_ARCHIVE_OUTPUT_DIRECTORY` 静态库输出目录（如 lib/static/）

示例：

```shell
cmake -DCMAKE_RUNTIME_OUTPUT_DIRECTORY=bin -DCMAKE_LIBRARY_OUTPUT_DIRECTORY=lib
```

这3个路径也可以在cmake脚本中设置,如:

```cmake
# 全局设置
# 设置可执行文件、库文件的输出路径（相对路径或绝对路径）
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/output/runtime)
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/output/library)
set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/output/archive)

# 针对 target 设置
set_target_properties(my_share PROPERTIES
        # target属性会覆盖全局 CMAKE_[ARCHIVE/LIBRARY/RUNTIME]_OUTPUT_DIRECTORY

        # 在 Linux/macOS 中 shared 库被认为是 Library
        # 由 LIBRARY_OUTPUT_DIRECTORY 属性控制输出目录
        LIBRARY_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/output/library"
        # 在Windows 中 shared 库被认为是 Runtime
        # 由 RUNTIME_OUTPUT_DIRECTORY 属性控制输出目录
        RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/output/library"
)
```



## 3 构建与编译控制

### 3.1 构建命令

```shell
--build <dir>
```

触发实际编译过程，替代直接调用 make 或 ninja：

```shell
cmake --build build --parallel 8 --clean-first
```

| 参数            | 作用                                   | 等效命令                 | 适用场景                 |
| --------------- | -------------------------------------- | ------------------------ | ------------------------ |
| `--parallel 8`  | 启用 8 线程并行编译，加速构建过程      | `make -j8` / `ninja -j8` | 大型项目编译优化         |
| `-j8`           | 与 `--parallel 8` 功能相同，为缩写形式 | 同上                     | 命令行快捷操作           |
| `--clean-first` | 构建前清理所有中间文件，确保全新构建   | `make clean && make`     | 解决缓存问题或切换配置时 |



`--target`：指定编译目标（如 install 或自定义目标）

`--config`：多配置生成器下的构建类型选择, 如:

```
--config Debug
```



### 3.2 编译器选项

通过变量传递编译标志：

`CMAKE_CXX_FLAGS` C++ 编译选项（如优化级别 `-O3`）

`CMAKE_EXE_LINKER_FLAGS` 可执行文件链接选项（如 -fsanitize=address）



示例：

```shell
cmake -DCMAKE_CXX_FLAGS="-Wall -Wextra"
```



## 4 安装与部署参数

### 4.1 安装路径配置

`CMAKE_INSTALL_PREFIX` 定义 make install 的默认安装路径：

```
cmake -DCMAKE_INSTALL_PREFIX=/usr/local
```

支持分组件安装（如头文件、库文件分离）



### 4.2 安装命令扩展

通过 `install()` 命令在 `CMakeLists.txt` 中定义安装规则：

```cmake
install(TARGETS myapp DESTINATION bin)
install(DIRECTORY include/ DESTINATION include)
```

支持 `RUNTIME`（可执行文件）、`LIBRARY`（动态库）、`ARCHIVE`（静态库）分类



## 5 调试与日志参数

### 5.1 日志级别控制

```shell
--log-level=<level>
```

设置日志详细程度（`ERROR`, `WARNING`, `NOTICE`, `STATUS`, `VERBOSE`, `DEBUG`）：

```shell
cmake --log-level=DEBUG
```



### 5.2 跟踪与诊断

```shell
--trace
```

打印 CMake 执行过程的详细步骤

```shell
--debug-output
```

显示调试信息（如变量展开过程）



## 6 其他实用参数

### 6.1 工具链文件

```shell
-DCMAKE_TOOLCHAIN_FILE=<path>
```

指定交叉编译工具链配置文件（常用于嵌入式开发）

```shell
cmake -DCMAKE_TOOLCHAIN_FILE=arm-gcc.cmake
```



### 6.2 模块路径扩展

```shell
 -DCMAKE_MODULE_PATH=<path>
```

添加自定义 `CMake` 模块搜索路径（如第三方库的 `Find*.cmake` 文件）



### 6.3 清空缓存

```shell
-U <glob_expr>
```

删除匹配的缓存变量（支持通配符 * 和 ?）：

```shell
cmake -U "BUILD_*"  # 删除所有以 BUILD_ 开头的缓存变量
```

完整参数速查表

| **参数类别** | **关键参数示例**                           |
| ------------ | ------------------------------------------ |
| 变量定义     | `-D` `-U`                                  |
| 目录控制     | `-S` `-B` `CMAKE_*_OUTPUT_DIRECTORY`       |
| 构建控制     | `--build` `--target` `--config`            |
| 安装配置     | `CMAKE_INSTALL_PREFIX` `install()`         |
| 调试工具     | `--trace` `--debug-output` `--log-level`   |
| 高级定制     | `CMAKE_TOOLCHAIN_FILE` `CMAKE_MODULE_PATH` |

注：本文档整合自 CMake 3.28+ 版本特性，部分参数可能因版本差异存在变化，建议参考官方文档获取最新信息。如需完整参数列表，可通过 cmake --help-full 命令查看。