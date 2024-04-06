# jextract的使用

写这个博客的目的：新人去看jextract的官网是看不懂的，就算看懂了也不会使用，一头雾水，我会从0开始教如何使用，如何搭配java去调用c函数。
首先我们得了解jextract是什么，官网的解释是一种从本机库头文件机械地生成 Java 绑定的工具。该工具利用 clang C API 来解析与给定本机库关联的标头，生成的 Java 绑定基于 Foreign Function & Memory API。该工具最初是在 Project Panama 的背景下开发的（然后在 Project Panama Early Access 二进制文件中提供）。
官网链接：https://github.com/openjdk/jextract
为什么会考虑到使用jextract呢？因为jni的那个太老而且操作太繁琐了
接下来开始讲解如何使用jextract

# jextract-21的文件的下载

jextract-21的windows版本的文件下载链接：https://download.java.net/java/early_access/jextract/1/openjdk-21-jextract+1-2_windows-x64_bin.tar.gz
下载并解压：
[![img](D:\my-note\java\jextract\assets\2916233-20231221123324909-1924452416.png)](https://img2023.cnblogs.com/blog/2916233/202312/2916233-20231221123324909-1924452416.png)
然后配置一下环境变量：
[![img](D:\my-note\java\jextract\assets\2916233-20231221123408005-376677558.png)](https://img2023.cnblogs.com/blog/2916233/202312/2916233-20231221123408005-376677558.png)

# jextract的使用

先写个.h的头文件
[![img](D:\my-note\java\jextract\assets\2916233-20231221125307955-1004166961.png)](https://img2023.cnblogs.com/blog/2916233/202312/2916233-20231221125307955-1004166961.png)
代码：



```cpp
//point.h
#include<stdio.h>
void sayHello();
```

然后再写个.c的实现源文件
[![img](D:\my-note\java\jextract\assets\2916233-20231221125411503-385204982.png)](https://img2023.cnblogs.com/blog/2916233/202312/2916233-20231221125411503-385204982.png)



```cpp
#include "point.h"
void sayHello(){
   printf("hello world");
}
```

接下来就是编译链接成lib
[![img](D:\my-note\java\jextract\assets\2916233-20231221125518422-1025838191.png)](https://img2023.cnblogs.com/blog/2916233/202312/2916233-20231221125518422-1025838191.png)



```r
gcc -c point.c
ar -cr pointlib.lib point.o
```

然后我们看一下jextract的使用说明书
[![img](D:\my-note\java\jextract\assets\2916233-20231221182456252-357664478.png)](https://img2023.cnblogs.com/blog/2916233/202312/2916233-20231221182456252-357664478.png)
可以看出--source是生成java资源文件的
而-t是生成到某个目录的名称
比如说：



```mipsasm
jextract --source -t org.jextract point.h
```

就是生成java源代码并存放到本地目录的org.jextract目录下面
使用jextract
[![img](D:\my-note\java\jextract\assets\2916233-20231221125633272-825030178.png)](https://img2023.cnblogs.com/blog/2916233/202312/2916233-20231221125633272-825030178.png)



```mipsasm
jextract --source -t org.jextract point.h
```

展示效果图：
[![img](D:\my-note\java\jextract\assets\2916233-20231221125741243-570489575.png)](https://img2023.cnblogs.com/blog/2916233/202312/2916233-20231221125741243-570489575.png)
然后你运行是肯定必报错的，因为没有链接到lib里面
真正使用jextract应该这样使用



```bash
jextract --source -t 生成的包目录 -l链接lib库 文件.h
```

这样就链接到了。
不过我的建议是官网有一个这样的例子：
https://github.com/openjdk/jextract/tree/master/samples/helloworld
兄弟们可以下载起来，然后在linux环境下跑一跑。
好，完结