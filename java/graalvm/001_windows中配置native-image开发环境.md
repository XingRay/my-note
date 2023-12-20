## GraalVM入门

### 1. GraalVM

[GraalVM](https://www.graalvm.org/) 

[GraalVM Community](https://www.graalvm.org/community/)

#### 1.1 下载

[graalvm-ce-builds](https://github.com/graalvm/graalvm-ce-builds/releases)

根据平台和需要的java版本选择，需要native-image需要同时下载native-image插件，一下以windows平台，jdk19为例

下载

[graalvm-ce-java19-windows-amd64-22.3.1.zip](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.1/graalvm-ce-java19-windows-amd64-22.3.1.zip) 

[native-image-installable-svm-java19-windows-amd64-22.3.1.jar](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.1/native-image-installable-svm-java19-windows-amd64-22.3.1.jar)

#### 1.2 安装

将 graalvm-ce-java19-windows-amd64-22.3.1.zip 解压多指定目录，如

```cmd
D:\develop\java\jdk\graalvm\19\graalvm-ce-java19-22.3.1
```

#### 1.3 环境变量配置

添加系统环境变量

```cmd
GRAALVM_HOME = D:\develop\java\jdk\graalvm\19\graalvm-ce-java19-22.3.1
```



（可选）如果想要在控制台直接输入native-image调用可以将JAVA_HOME设置为GRAALVM_HOME，这样系统会默认使用GRAALVM作为jdk

```cmd
JAVA_HOME = %GRAALVM_HOME%
```

确保系统环境变量Path中包含 `%JAVA_HOME%\bin`，如果不设置的话，在控制台通过加前缀`%GRAALVM_HOME%\bin\` 启动`graalvm` 的相关命令，如 `%GRAALVM_HOME%\bin\java`, `%GRAALVM_HOME%\bin\gu`, `%GRAALVM_HOME%\bin\native-image` ，下文均假设设置了 `JAVA_HOME=GRALVM_HOME` 

#### 1.4 测试

```bash
java --version
openjdk 19.0.2 2023-01-17
OpenJDK Runtime Environment GraalVM CE 22.3.1 (build 19.0.2+7-jvmci-22.3-b12)
OpenJDK 64-Bit Server VM GraalVM CE 22.3.1 (build 19.0.2+7-jvmci-22.3-b12, mixed mode, sharing)
```

注意提示中有 GraalVM CE

### 2. native-image

[native-image](https://www.graalvm.org/latest/reference-manual/native-image/)

#### 2.1 下载插件

[native-image-installable-svm-java19-windows-amd64-22.3.1.jar](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.1/native-image-installable-svm-java19-windows-amd64-22.3.1.jar)

#### 2.2 安装插件

```bash
gu -L install D:\develop\java\jdk\graalvm\19\installable-svm-java19-windows-amd64-22.3.1.jar
```

这里`gu`是GraalVM的自带的脚本，位于 

```bash
D:\develop\java\jdk\graalvm\19\graalvm-ce-java19-22.3.1\bin\gu.cmd
```

#### 2.3 windows平台环境配置

##### 2.3.1 下载 visual studio

[visual studio](https://visualstudio.microsoft.com/zh-hans/)

[visual studio community](https://c2rsetup.officeapps.live.com/c2r/downloadVS.aspx?sku=community&channel=Release&version=VS2022&source=VSLandingPage&includeRecommended=true&cid=2030:3a5639295da0468dbc77c5e73262e620)

下载 visual studio community 版本即可，这里以 visual studio community 2022 版本为例

##### 2.3.2 安装 visual studio

工作负荷

- [x]   使用C++的桌面开发

右侧 “可选项” 中勾选 

- [x] MSVC v142 -VS 2019 C++ x64/x86生成工具(v14.29)
- [x] MSVC v141 -VS 2017 C++ x64/x86生成工具(v14.16)
- [x] MSVC v140 -VS 2015 C++ 生成工具(v14.00)



**！！！！！注意，语言包一定要取消中文，只使用英文的语言包！！！！！**

否则可能会报错

```bash
Native-image building on Windows currently only supports target architecture: AMD64 (?? unsupported)
```



##### 2.3.3 配置环境变量

新建系统环境变量  INCLUDE ，设置值为：

```cmd
D:\develop\MicrosoftVisualStudio\2022\Community\VC\Tools\MSVC\14.35.32215\include
C:\Program Files (x86)\Windows Kits\10\Include\10.0.22000.0\shared
C:\Program Files (x86)\Windows Kits\10\Include\10.0.22000.0\ucrt
C:\Program Files (x86)\Windows Kits\10\Include\10.0.22000.0\um
```

值之间可用 `;` 分隔，有两个变量值之后， 关闭添加窗口重新打开，windows环境变量编辑工具可以显示为多行。



添加 LIB

```cmd
C:\Program Files (x86)\Windows Kits\10\Lib\10.0.22000.0\um\x64
C:\Program Files (x86)\Windows Kits\10\Lib\10.0.22000.0\ucrt\x64
D:\develop\MicrosoftVisualStudio\2022\Community\VC\Tools\MSVC\14.35.32215\lib\x64
```



在  PATH 中添加 cl.exe 的路径， cl.exe 是vs的C编译器

```cmd
D:\develop\MicrosoftVisualStudio\2022\Community\VC\Tools\MSVC\14.35.32215\bin\Hostx64\x64
```

可以通过指令验证`cl`是否配置正确

```bash
cl -version
```



##### 2.3.4 native-image demo

在任意目录，如：`D:\tmp\hello` 下新建文件 `HelloWorld.java`

```java
public class HelloWorld {
     public static void main(String[] args) {
         System.out.println("Hello, Native World!");
     }
 }
```

在当前目录下运行命令：

```bash
javac HelloWorld.java
native-image HelloWorld
```

即可看到输出文件`helloworld.exe`，再通过运行指令启动 `helloworld.exe`即可看到输出

```cmd
Hello, Native World!
```

