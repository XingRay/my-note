1 下载 CMake
https://cmake.org/download/

2 安装cmake
下载安装cmake到自定义目录. 并添加到系统 path
验证:

```
cmake --version
```

输出

```
cmake version 4.0.3

CMake suite maintained and supported by Kitware (kitware.com/cmake).
```



3 基本测试

main.cpp:

```cpp
#include <cstdio>

int main(int argc, const char** argv){
    printf("hello cmake");
}
```

CMakeLists.txt

```cmake
# 指定 CMake 的最小版本号，低于此版本的 CMake 将终止建构档的生成过程
cmake_minimum_required(VERSION 3.31.6)

# 创建项目
project(
        Demo          # 设置项目名
        LANGUAGES CXX # 指定语言，未指定的语言将不参与构建，例如 test.c 文件
)

# 创建可执行文件
add_executable(
        demo     # 目标名
        main.cpp # 用到的源文件
)
```

执行命令:
构建

```bash
cmake -S . -B ./build
```

编译

```bash
cmake --build ./build --config Release
```

执行

```bash
.\build\Release\demo.exe
```