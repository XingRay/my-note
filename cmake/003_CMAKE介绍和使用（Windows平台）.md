# CMAKE介绍和使用（Windows平台）



CMake是一个跨平台的安装（编译）工具，可以用简单的语句来描述所有平台的安装(编译过程)。
Windows平台
cmake工具的下载
cmake工具下载官网： Download | CMake

https://cmake.org/download/

![img](D:\my-note\cmake\assets\e41dc7acbebd41f6bfb2378f31a9ad0b.png)

下载压缩包后解压，解压后的bin文件路径加到电脑系统环境变量中 

设置完成后打开命令行(cmd)，输入命令cmake -version可查看版本号

![img](D:\my-note\cmake\assets\3040dc63f9b54c6bba8ffe63b6a28a54.png)

编译和执行
在windows 平台下使用 CMake 生成 Makefile 并编译的流程如下：

1. 写 CMake 配置文件 CMakeLists.txt 。
2. 执行命令： cmake PATH -G "MinGW Makefiles"  使用cmake生成Makefile文件（ PATH 是 CMakeLists.txt 所在的目录）
3. 执行命令：cmake --build PTAH  使用 cmake 命令进行编译生成exe可执行程序（ PATH 是 CMakeLists.txt 所在的目录）



入门案例：
假设测试代码是如下结构：

![img](D:\my-note\cmake\assets\75a141dc752647dfb3b8ce38b998bb91.png)

func.c的示例代码：

```
#include <stdio.h>
 
int add(int a, int b)
{
	int c = a + b;
	return c;
}
```

main.c的示例代码：

```
#include <stdio.h>
#include <stdlib.h>
#include "func.h"
 
int main(int argc, char* argv[])
{
	if(argc < 3)
	{
		printf("Usage: %s input error\n", argv[0]);
		return -1;
	}
 
	int a = atoi(argv[1]);
	int b = atoi(argv[2]);
 
	int sum = add(a,b);
 
	printf("%d\n", sum);
	return 0;
}
```

 CMakeList.txt的示例代码：

```
cmake_minimum_required (VERSION 3.8)
 
project(demo)
 
include_directories(./)
 
aux_source_directory(. DIR_SRCS)
 
add_executable(Demo ${DIR_SRCS})
 
 
#test
enable_testing()
 
add_test(test_run Demo 2 3)
 
add_test(test_usage Demo)
set_tests_properties(test_usage PROPERTIES PASS_REGULAR_EXPRESSION "Usage")
 
add_test(test Demo 3 3)
set_tests_properties(test PROPERTIES PASS_REGULAR_EXPRESSION "6")
```



cmake生成Makefile文件

![img](D:\my-note\cmake\assets\be1ebdabd34a439eb5b31ba44b53a39b.png)



cmake 命令进行编译生成exe可执行程序Demo.exe

![img](D:\my-note\cmake\assets\69d2e3221e70450c9d8f8b4ec4e65fe4.png)



执行程序

![img](D:\my-note\cmake\assets\c441510529334c4095cb9151b8430d30.png)



ctest执行自动测试

![img](D:\my-note\cmake\assets\7b14e5e6995b45bfa79818b6f459d468.png)