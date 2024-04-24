# Visual Studio 2022 以 UTF-8 编译 CMake 项目

```
// 设置枚举的底层类型
enum Example {
    A = 1,
    B = 2,
    C = 3,
};
```

代码编译报错, 

原因: 上面的注释使用了中文, 源码是utf-8 编码, 但是windows 默认GBK, 导致源码乱码, 编译失败

解决方案: 源码指定按照utf8进行编译

CMakeList.txt : 

```
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /utf-8")
```

本文适用 Visual Studio 2017 以及更高版本

默认情况下，MSVC 使用当前系统的代码页（Code Page）编译源文件，这样在编译**字符串字面量**的时候就和当前系统的代码页一致。

在编译别人的 CMake 项目的时候，特别是在 Linux 平台下开发的项目，源文件默认使用 UTF-8 编码，如果在源码中出现 Unicode 字符，编译就会报错：

例如编译 [nlohmann/json](https://github.com/nlohmann/json)

```
json\tests\src\unit-class_parser.cpp(1): warning C4819: 该文件包含不能在当前代码页(936)中表示的字符。请将该文件保存为 Unicode 格式以防止数据丢失
json\tests\src\unit-class_parser.cpp(466): error C2001: 常量中有换行符
json\tests\src\unit-class_parser.cpp(467): error C2001: 常量中有换行符
json\tests\src\unit-class_parser.cpp(779): error C2001: 常量中有换行符
json\tests\src\unit-class_parser.cpp(466): fatal error C1057: 宏扩展中遇到意外的文件结束
```

使用编译参数 `/utf-8` 解决这个问题，在 `CMakeSettings.json` 里添加 **CMake 命令参数**：

```
-D CMAKE_CXX_FLAGS=/utf-8
```

### 参考资料

[Set source and execution character sets to UTF-8](https://docs.microsoft.com/en-us/cpp/build/reference/utf-8-set-source-and-executable-character-sets-to-utf-8?view=msvc-170)

## 跨平台源码编辑

以上解决了编译问题，编辑源文件的时候需要定义一个 `EditorConfig` 文件定义项目全局的格式与项目保持一致，例如字符编码、换行和缩进等。

在项目根目录下新建一个名为 `.editorconfig` 的文本文件，内容如下：

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
indent_style = space
```

### 参考资料

[Create portable, custom editor settings with EditorConfig](https://docs.microsoft.com/en-us/visualstudio/ide/create-portable-custom-editor-options?view=vs-2022)

[CMake](https://lvv.me/tags/cmake/)





# `/utf-8` (Set source and execution character sets to UTF-8)

- Article
- 02/01/2022
- 7 contributors

Feedback

In this article[Syntax](https://learn.microsoft.com/en-us/cpp/build/reference/utf-8-set-source-and-executable-character-sets-to-utf-8?view=msvc-170#syntax)[Remarks](https://learn.microsoft.com/en-us/cpp/build/reference/utf-8-set-source-and-executable-character-sets-to-utf-8?view=msvc-170#remarks)[Set the option in Visual Studio or programmatically](https://learn.microsoft.com/en-us/cpp/build/reference/utf-8-set-source-and-executable-character-sets-to-utf-8?view=msvc-170#set-the-option-in-visual-studio-or-programmatically)[See also](https://learn.microsoft.com/en-us/cpp/build/reference/utf-8-set-source-and-executable-character-sets-to-utf-8?view=msvc-170#see-also)

Specifies both the source character set and the execution character set as UTF-8.



## Syntax

> **`/utf-8`**



## Remarks

You can use the **`/utf-8`** option to specify both the source and execution character sets as encoded by using UTF-8. It's equivalent to specifying **`/source-charset:utf-8 /execution-charset:utf-8`** on the command line. Any of these options also enables the **`/validate-charset`** option by default. For a list of supported code page identifiers and character set names, see [Code Page Identifiers](https://learn.microsoft.com/en-us/windows/win32/Intl/code-page-identifiers).

By default, Visual Studio detects a byte-order mark to determine if the source file is in an encoded Unicode format, for example, UTF-16 or UTF-8. If no byte-order mark is found, it assumes that the source file is encoded in the current user code page, unless you've specified a code page by using **`/utf-8`** or the **`/source-charset`** option. Visual Studio allows you to save your C++ source code in any of several character encodings. For information about source and execution character sets, see [Character sets](https://learn.microsoft.com/en-us/cpp/cpp/character-sets?view=msvc-170) in the language documentation.



## Set the option in Visual Studio or programmatically



### To set this compiler option in the Visual Studio development environment

1. Open the project **Property Pages** dialog box. For more information, see [Set C++ compiler and build properties in Visual Studio](https://learn.microsoft.com/en-us/cpp/build/working-with-project-properties?view=msvc-170).
2. Select the **Configuration Properties** > **C/C++** > **Command Line** property page.
3. In **Additional Options**, add the **`/utf-8`** option to specify your preferred encoding.
4. Choose **OK** to save your changes.



### To set this compiler option programmatically

- See [AdditionalOptions](https://learn.microsoft.com/en-us/dotnet/api/microsoft.visualstudio.vcprojectengine.vcclcompilertool.additionaloptions).



## See also

[MSVC compiler options](https://learn.microsoft.com/en-us/cpp/build/reference/compiler-options?view=msvc-170)
[MSVC compiler command-line syntax](https://learn.microsoft.com/en-us/cpp/build/reference/compiler-command-line-syntax?view=msvc-170)
[`/execution-charset` (Set execution character set)](https://learn.microsoft.com/en-us/cpp/build/reference/execution-charset-set-execution-character-set?view=msvc-170)
[`/source-charset` (Set source character set)](https://learn.microsoft.com/en-us/cpp/build/reference/source-charset-set-source-character-set?view=msvc-170)
[`/validate-charset` (Validate for compatible characters)](https://learn.microsoft.com/en-us/cpp/build/reference/validate-charset-validate-for-compatible-characters?view=msvc-170)