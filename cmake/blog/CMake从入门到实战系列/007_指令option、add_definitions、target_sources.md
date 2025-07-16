# 指令option、add_definitions、target_sources

上一篇文章《CMake从入门到实战系列（六）——CMake自定义编译选项》中的demo用到了三个CMake指令，在之前的文章《CMake从入门到实战系列（三）——CMake常用指令》没有提到，此篇文章，我们对这三个指令option、add_definitions、target_sources进行一下总结，后期我们还会对CMake其他指令进行总结，比如条件语句、循环语句等等。

**一、option**

CMake 中的 option 指令是用来提供一个开关选项给用户，以便用户在进行项目配置时选择是否启用某个功能。这种开关通常用于控制编译条件、功能模块的启用/禁用等。option 指令可以在命令行中通过 -D 参数指定，也可以在图形界面中选择。

**【1】基本语法**

option 指令的基本语法如下：

```
option(<option_variable> "description of the option" [initial_value])
```

**【2】参数含义**

> **<option_variable>：**开关的名称，影响这个变量名的布尔值（ON 或 OFF）。
>
> 
>
> **“description of the option”：**该开关的描述，解释这个选项做什么用，以便用户理解。
>
> 
>
> **[initial_value]：**可选参数，设置选项的初始值（默认是OFF），如果不指定，则认为是 OFF。

**【3】示例**

这个选项在首次运行 cmake 时会根据提供的初始值进行设置，用户可以通过-D参数显式地设置该选项的值，如-D<option_variable>=ON或-D<option_variable>=OFF。

例如，你有一个项目想要根据用户需求选择是否构建测试：

```
option(BUILD_TESTS "Build the test suite" OFF)

if(BUILD_TESTS)
    add_subdirectory(tests)
endif()
```

在这个例子中，如果用户在配置项目时不做任何操作，BUILD_TESTS 默认被设置为OFF，测试套件不会被构建。如果一个用户想要构建测试套件，他们需要在运行cmake 时设置BUILD_TESTS为ON，例如：

```
cmake -DBUILD_TESTS=ON ..
```

需要注意的是，一旦在运行cmake 时指定了某个选项的值，它将被缓存起来，并在随后的配置运行中使用，除非通过命令行再次明确设置或使用ccmake 或 CMake GUI 清除缓存。缓存的目的是为了避免每次运行cmake 命令时都必须重新指定工程的配置选项。

总之，option 指令是 CMake 中非常有用的功能，允许用户方便地启用或禁用项目中的各种特性，从而对构建过程进行灵活的控制。

**二、add_definitions**

add_definitions 是CMake 的一条指令，用于向CMake 生成的构建系统中添加编译器定义。当这条指令在CMakeLists.txt文件中被调用时，它会为之后定义的目标（例如，通过add_executable或add_library 创建的目标）添加预处理器定义。

**【1】基本语法**

```
add_definitions(-DDEFINITION)
```

**【2】参数含义**

> **-DDEFINITION：**会告知 CMake 在后续的编译步骤中添加 -D 前缀的编译器标志，用于定义预处理器宏。一般情况下，你可以重复使用 add_definitions 来添加多个定义。

**【3】示例**

```
add_definitions(-DUSE_FEATURE_X)
add_definitions(-DMY_ANOTHER_DEFINE)
```

以上命令会添加 USE_FEATURE_X 和 MY_ANOTHER_DEFINE 宏到编译器的命令行中，使它们在整个项目的编译过程中都被定义。如果在源代码中使用了#ifdef USE_FEATURE_X，那么它将被编译器识别，并且相关的代码会被包含在编译过程中。

**【4】注意事项**

add_definitions 对整个项目目录及其子目录有效，影响所有的目标（即使在 add_definitions 之后才定义的目标）。

建议使用target_compile_definitions代替add_definitions，因为 target_compile_definitions 允许你更精细地控制哪些目标需要被添加定义，而不会影响全局。

target_compile_definitions 用于为指定的目标添加编译器定义，而不是全局应用。这更有利于项目的模块化管理，因为它避免了对全局 CMake 环境的污染。

使用 target_compile_definitions

```
target_compile_definitions(target_name PRIVATE -DDEFINITION)
```

在这个例子中，target_name 应该被替换为实际目标的名称（例如可执行文件或库的名称），DEFINITION将只会添加到那个特定目标的编译定义中。

**结论**

虽然 add_definitions 是一个有用的指令，但是其全局性可能会导致一些不易调试的问题。对于大型项目和需要更细致管理的情况，建议使用 target_compile_definitions 来为特定目标设置特定的编译器定义，以保持项目的清晰和模块化。

**三、target_sources**

target_sources 是 CMake 中的命令，用于向已经通过 add_executable 或 add_library 等命令创建的目标添加额外的源文件。它允许你在项目的不同部分为目标指定额外的源文件，而无需在原始 add_executable 或 add_library 调用中包含所有源文件，这有助于增强 CMake 列表文件的模块化。

**【1】基本语法**

```
target_sources(<target> PRIVATE|PUBLIC|INTERFACE <source>...)
```

**【2】参数含义**

> **< target > ：**是之前已经定义的目标的名称（如可执行程序或库名）。
>
> 
>
> **< source >：**是一个或多个要添加的源文件。
>
> 
>
> **PRIVATE,PUBLIC, INTERFACE：**关键字用来定义源文件的范围和提供给消费者的接口。
>
> 
>
> 关键字说明
>
> **PRIVATE：**添加的源文件只在构建< target > 时使用，不会在链接此目标的其他目标（如可执行文件或库）中使用。
>
> 
>
> **PUBLIC：**源文件既用于目标自身的构建，也提供给链接了此目标的消费者使用。
>
> 
>
> **INTERFACE**：源文件不用于目标自身的构建，但是会提供给那些链接此目标的消费者。

**【3】示例**

假设我们有一个叫做 MyLib 的库，我们想为它添加额外的源文件：

```
add_library(MyLib STATIC src/MyLib.cpp)
# as a good practice, specify the source files relative to the current CMakeList.txt directory
target_sources(MyLib PRIVATE src/AdditionalFile1.cpp src/AdditionalFile2.cpp)
```

在此示例中，我们首先创建了一个名为 MyLib 的静态库，它只有一个源文件 src/MyLib.cpp。然后，我们使用 target_sources 为 MyLib 添加了两个额外的私有源文件src/AdditionalFile1.cpp 和 src/AdditionalFile2.cpp。这些私有源文件将只用于构建 MyLib，不会影响链接了MyLib 的其他目标。

target_sources 命令通常在与目标相关的 CMakeLists.txt文件中使用，它有助于你的项目保持组织和模块化。

**【4】注意事项**

> 1、使用 target_sources 命令时，应该确保 < target > 已经被定义，否则 CMake 将会报错。
>
> 
>
> 2、< source > 文件应该是相对于当前 CMakeLists.txt 文件的路径或绝对路径。
>
> 
>
> 3、target_sources不能用来移除以前添加的源文件，它只能用来添加新的源文件。
>
> 
>
> 4、对于多平台或有条件的源文件配置，你可以结合 if 语句和 target_sources 命令来根据特定条件选择性地包含源文件。

欢迎关注我的公众号【**嵌入式技术部落**】，交流讨论。