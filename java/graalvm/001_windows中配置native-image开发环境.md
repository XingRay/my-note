## 在 windows 平台配置 GraalVM native-image 编译环境

### 1. 安装GraalVM

[GraalVM](https://www.graalvm.org/) 

[GraalVM Community](https://www.graalvm.org/community/)

#### 1.1 下载

https://www.graalvm.org/



根据平台和需要的java版本选择，需要native-image需要同时下载native-image插件，一下以windows平台，jdk21为例

下载 graalvm for jdk21

https://download.oracle.com/graalvm/21/latest/graalvm-jdk-21_windows-x64_bin.zip



[native-image-installable-svm-java19-windows-amd64-22.3.1.jar](https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.1/native-image-installable-svm-java19-windows-amd64-22.3.1.jar)



GraalVM JDK with Native Image

```
docker pull container-registry.oracle.com/graalvm/native-image:21
```

GraalVM JDK without Native Image

```
docker pull container-registry.oracle.com/graalvm/jdk:21
```



#### 1.2 安装

将 graalvm-jdk-21_windows-x64_bin.zip 解压多指定目录，如

```cmd
D:\develop\java\jdk\graalvm\21\graalvm-jdk-21.0.2+13.1
```

#### 1.3 环境变量配置

添加系统环境变量

```cmd
GRAALVM_HOME = D:\develop\java\jdk\graalvm\21\graalvm-jdk-21.0.2+13.1
```



（可选）如果想要在控制台直接输入native-image调用可以将JAVA_HOME设置为GRAALVM_HOME，这样系统会默认使用GRAALVM作为jdk

```cmd
JAVA_HOME = %GRAALVM_HOME%
```

确保系统环境变量Path中包含 `%JAVA_HOME%\bin`，如果不设置的话，在控制台通过加前缀`%GRAALVM_HOME%\bin\` 启动`graalvm` 的相关命令，如 `%GRAALVM_HOME%\bin\java`, `%GRAALVM_HOME%\bin\gu`, `%GRAALVM_HOME%\bin\native-image` ，下文均假设设置了 `JAVA_HOME=GRALVM_HOME` 

#### 1.4 测试

```
java -version
```

输出

```bash
java version "21.0.2" 2024-01-16 LTS
Java(TM) SE Runtime Environment Oracle GraalVM 21.0.2+13.1 (build 21.0.2+13-LTS-jvmci-23.1-b30)
Java HotSpot(TM) 64-Bit Server VM Oracle GraalVM 21.0.2+13.1 (build 21.0.2+13-LTS-jvmci-23.1-b30, mixed mode, sharing)
```

注意提示中有 GraalVM



### 2. 配置 native-image

[native-image](https://www.graalvm.org/latest/reference-manual/native-image/)

新版Graalvm自带 native-image 工具, 在 %GRAALVM_HOME%\bin 目录下, 可以通过指令检测 native-image 工具是否已经安装:

```
native-image --version
```

输出:

```
native-image 21.0.2 2024-01-16
GraalVM Runtime Environment Oracle GraalVM 21.0.2+13.1 (build 21.0.2+13-LTS-jvmci-23.1-b30)
Substrate VM Oracle GraalVM 21.0.2+13.1 (build 21.0.2+13-LTS, serial gc, compressed references)
```

则说明已经安装成功



#### 2.1 windows平台环境配置

##### 2.1.1 下载 visual studio

[visual studio](https://visualstudio.microsoft.com/zh-hans/)

[visual studio community](https://c2rsetup.officeapps.live.com/c2r/downloadVS.aspx?sku=community&channel=Release&version=VS2022&source=VSLandingPage&includeRecommended=true&cid=2030:3a5639295da0468dbc77c5e73262e620)

下载 visual studio community 版本即可，这里以 visual studio community 2022 版本为例

##### 2.1.2 安装 visual studio

工作负荷

- [x]   使用C++的桌面开发

右侧 “可选项” 中勾选 

- [x] MSVC v142 -VS 2019 C++ x64/x86生成工具(v14.39)
- [x] MSVC v141 -VS 2017 C++ x64/x86生成工具(v14.16)
- [x] MSVC v140 -VS 2015 C++ 生成工具(v14.00)



**！！！！！注意，语言包一定要取消中文，只使用英文的语言包！！！！！**

否则可能会报错

```bash
Native-image building on Windows currently only supports target architecture: AMD64 (?? unsupported)
```

如果已经安装了vs, 并且设置语言包为中文, 可以通过下面的方式修改为英文:

1 打开 Visual Studio Installer
![image-20240216153103802](D:\my-note\java\graalvm\assets\image-20240216153103802.png)

点击修改

![image-20240216153131097](D:\my-note\java\graalvm\assets\image-20240216153131097.png)

点击 语言包 , 将 中文 反选, 再选择 英文, 此时右下角按钮会变为 "修改", 点击 修改 按钮, 等待 vs-installer 安装英文语言包即可



##### 2.1.3 配置环境变量

新建系统环境变量  INCLUDE ，设置值为：

具体路径根据具体安装vs的版本及安装路径会有不同

```cmd
C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Tools\MSVC\14.39.33519\include
C:\Program Files (x86)\Windows Kits\10\Include\10.0.22621.0\shared
C:\Program Files (x86)\Windows Kits\10\Include\10.0.22621.0\ucrt
C:\Program Files (x86)\Windows Kits\10\Include\10.0.22621.0\um
```

值之间可用 `;` 分隔，有两个变量值之后， 关闭添加窗口重新打开，windows环境变量编辑工具可以显示为多行。



再添加系统环境变量 LIB

```cmd
C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Tools\MSVC\14.39.33519\lib\x64
C:\Program Files (x86)\Windows Kits\10\Lib\10.0.22621.0\um\x64
C:\Program Files (x86)\Windows Kits\10\Lib\10.0.22621.0\ucrt\x64
```



在  PATH 中添加 cl.exe 的路径， cl.exe 是vs的C编译器

```cmd
C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Tools\MSVC\14.39.33519\bin\Hostx64\x64
```

或者创建系统环境变量

```
MSVC = C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Tools\MSVC\14.39.33519
```

在Path中添加

```
%MSVC%\bin\Hostx64\x64
```



通过指令验证`cl`是否配置正确

```bash
cl
```

输出:

```
用于 x64 的 Microsoft (R) C/C++ 优化编译器 19.38.33135 版
版权所有(C) Microsoft Corporation。保留所有权利。

用法: cl [ 选项... ] 文件名... [ /link 链接选项... ]
```



### 2.2 native-image demo

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

输出:

```
========================================================================================================================
GraalVM Native Image: Generating 'helloworld' (executable)...
========================================================================================================================
For detailed information and explanations on the build output, visit:
https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/BuildOutput.md
------------------------------------------------------------------------------------------------------------------------
[1/8] Initializing...                                                                                   (11.5s @ 0.13GB)
 Java version: 21.0.2+13-LTS, vendor version: Oracle GraalVM 21.0.2+13.1
 Graal compiler: optimization level: 2, target machine: x86-64-v3, PGO: ML-inferred
 C compiler: cl.exe (microsoft, x64, 19.39.33519)
 Garbage collector: Serial GC (max heap size: 80% of RAM)
 1 user-specific feature(s):
 - com.oracle.svm.thirdparty.gson.GsonFeature
------------------------------------------------------------------------------------------------------------------------
Build resources:
 - 26.49GB of memory (41.4% of 63.93GB system memory, determined at start)
 - 24 thread(s) (100.0% of 24 available processor(s), determined at start)
[2/8] Performing analysis...  [*****]                                                                    (3.6s @ 0.19GB)
    2,088 reachable types   (61.4% of    3,402 total)
    1,995 reachable fields  (45.6% of    4,372 total)
    9,649 reachable methods (38.5% of   25,042 total)
      769 types,   109 fields, and   474 methods registered for reflection
       53 types,    30 fields, and    48 methods registered for JNI access
        1 native library: version
[3/8] Building universe...                                                                               (0.8s @ 0.30GB)
[4/8] Parsing methods...      [*]                                                                        (1.3s @ 0.30GB)
[5/8] Inlining methods...     [***]                                                                      (0.4s @ 0.29GB)
[6/8] Compiling methods...    [***]                                                                      (6.4s @ 0.35GB)
[7/8] Layouting methods...    [*]                                                                        (0.8s @ 0.33GB)
[8/8] Creating image...       [*]                                                                        (0.9s @ 0.37GB)
   3.49MB (47.52%) for code area:     4,554 compilation units
   3.77MB (51.38%) for image heap:   57,132 objects and 71 resources
  82.23kB ( 1.09%) for other data
   7.34MB in total
------------------------------------------------------------------------------------------------------------------------
Top 10 origins of code area:                                Top 10 object types in image heap:
   1.83MB java.base                                          880.34kB byte[] for code metadata
   1.35MB svm.jar (Native Image)                             723.24kB byte[] for java.lang.String
  90.25kB com.oracle.svm.svm_enterprise                      431.28kB heap alignment
  42.46kB jdk.proxy3                                         383.23kB java.lang.String
  40.50kB jdk.proxy1                                         332.00kB java.lang.Class
  30.21kB org.graalvm.nativeimage.base                       154.03kB java.util.HashMap$Node
  29.85kB org.graalvm.collections                            114.01kB char[]
  21.47kB jdk.internal.vm.ci                                 100.64kB byte[] for reflection metadata
  17.29kB jdk.internal.vm.compiler                            91.68kB java.lang.Object[]
  11.90kB jdk.proxy2                                          81.56kB com.oracle.svm.core.hub.DynamicHubCompanion
  389.00B for 1 more packages                                571.98kB for 553 more object types
                              Use '-H:+BuildReport' to create a report with more details.
------------------------------------------------------------------------------------------------------------------------
Security report:
 - Binary does not include Java deserialization.
 - Use '--enable-sbom' to embed a Software Bill of Materials (SBOM) in the binary.
------------------------------------------------------------------------------------------------------------------------
Recommendations:
 PGO:  Use Profile-Guided Optimizations ('--pgo') for improved throughput.
 INIT: Adopt '--strict-image-heap' to prepare for the next GraalVM release.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
 QBM:  Use the quick build mode ('-Ob') to speed up builds during development.
------------------------------------------------------------------------------------------------------------------------
                        1.0s (3.8% of total time) in 117 GCs | Peak RSS: 0.97GB | CPU load: 6.32
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 D:\tmp\hello\helloworld.exe (executable)
========================================================================================================================
Finished generating 'helloworld' in 26.5s.
```



即可看到输出文件`helloworld.exe`，再通过运行指令启动 `helloworld.exe`即可看到输出

```cmd
Hello, Native World!
```

