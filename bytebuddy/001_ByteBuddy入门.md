## ByteBuddy入门

JavaAgent是在JDK5之后提供的新特性，也可以叫java代理。

开发者通过这种机制(Instrumentation)可以在加载class文件之前修改方法的字节码(此时字节码尚未加入JVM)，动态更改类方法实现AOP，提供监控服务如；方法调用时长、可用率、内存等。

关于此文版本
此文不断迭代，最新版本，请参考：

ByteBuddy（史上最全） - 疯狂创客圈 - 博客园 (cnblogs.com)

Java字节码简介
Java字节码是众多字节码增强技术的知识基础。

Java语言写出的源代码首先需要编译成class文件，即字节码文件，然后被JVM加载并运行，每个 class文件 具有如下固定的数据格式，

ClassFile {
    u4             magic;           // 魔数，固定为0xCAFEBABE
    u2             minor_version;   // 次版本
    u2             major_version;   // 主版本，常见版本：52对应1.8，51对应1.7，其他依次类推
    u2             constant_pool_count;                     // 常量池个数
    cp_info        constant_pool[constant_pool_count-1];    // 常量池定义
    u2             access_flags;    // 访问标志：ACC_PUBLIC, ACC_INTERFACE, ACC_ABSTRACT等
    u2             this_class;      // 类索引
    u2             super_class;     // 父类索引
    u2             interfaces_count;
    u2             interfaces[interfaces_count];
    u2             fields_count;
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
可以看到，class文件总是一个魔数开头，后面跟着版本号，然后就是常量定义、访问标志、类索引、父类索引、接口个数和索引表、字段个数和索引表、方法个数和索引表、属性个数和索引表。

class文件本质上是一个字节码流，每个字节码所处的位置代表着一定的指令和含义。如何对class文件中定义的指令和字节码进行解读、增强定义、编排，这是字节码增强技术所要完成的事情。

了解Java字节码有助于字节码增强的开发，但并不是实现字节码增强开发的必要条件，最新主流的众多字节码增强工具框架类库都将字节码的编排进行了不同程度封装，在可读性、易编排性、排错性上提供开发便利性，学习曲线和开发难度得到了较好的改善。

Java字节码增强支持
对于字节码增强的开发来说，JVMTI是一个在实践中应该被熟悉的工具技术。

JVM从1.5版本开始提供 JVM Tool Interface ，这是JVM对外的、用于Java应用监控和调试的一系列工具接口，是JVM平台调试架构的重要组成部分。

下图是 JVM平台调试架构图 ，

The Java™ Platform Debugger Architecture is structured as follows:
           Components                          Debugger Interfaces

                /    |--------------|
               /     |     VM       |
 debuggee ----(      |--------------|  <------- JVM TI - Java VM Tool Interface（Jvm服务端调试接口）
               \     |   back-end   |
                \    |--------------|
                /           |
 comm channel -(            |  <--------------- JDWP - Java Debug Wire Protocol （Java调试通信协议）
                \           |
                     |--------------|
                     | front-end    |
                     |--------------|  <------- JDI - Java Debug Interface （客户端调试接口和调试应用）
                     |      UI      |
                     |--------------|
JVM启动支持加载agent代理，而agent代理本身就是一个JVM TI的客户端，其通过监听事件的方式获取Java应用运行状态，调用JVM TI提供的接口对应用进行控制。

我们可以看下Java agent代理的两个入口函数定义，

// 用于JVM刚启动时调用，其执行时应用类文件还未加载到JVM
```java
public static void premain(String agentArgs, Instrumentation inst);

// 用于JVM启动后，在运行时刻加载
public static void agentmain(String agentArgs, Instrumentation inst);
```
这两个入口函数定义分别对应于JVM TI专门提供了执行 字节码增强（bytecode instrumentation） 的两个接口。

加载时刻增强（JVM 启动时加载），类字节码文件在JVM加载的时候进行增强，。
动态增强（JVM 运行时加载），已经被JVM加载的class字节码文件，当被修改或更新时进行增强，从JDK 1.6开始支持。
这两个接口都是从JDK 1.6开始支持。

我们无需对上面JVM TI提供的两个接口规范了解太多，Java Agent和 Java Instrument类包 封装好了字节码增强的上述接口通信。

上面我们已经说到了， 有两处地方可以进行 Java Agent 的加载，分别是 目标JVM启动时加载 和 目标JVM运行时加载，这两种不同的加载模式使用不同的入口函数：

1、JVM 启动时加载

入口函数如下所示：

 // 函数1
```java
public static void premain(String agentArgs, Instrumentation inst);
// 函数2
public static void premain(String agentArgs);
```
JVM 首先寻找函数1，如果没有发现函数1，则会寻找函数2

2、JVM 运行时加载

入口函数如下所示：

// 函数1
```java
public static void agentmain(String agentArgs, Instrumentation inst);
// 函数2
public static void agentmain(String agentArgs);
```
与上述一致，JVM 首先寻找函数1，如果没有发现函数1，则会寻找函数2

这两组方法的第一个参数 agentArgs 是随同 “-javaagent” 一起传入的程序参数，如果这个字符串代表了多个参数，就需要自己解析这参数，inst 是 Instrumentation 类型的对象，是 JVM 自己传入的，我们可以那这个参数进行参数的增强操作。

演示类AgentDemo
```java
package com.crazymaker.agent.javassist.demo;
import java.lang.instrument.Instrumentation;

public class AgentDemo {
    /**
     * JVM 首先尝试在代理类上调用以下方法
     * 该方法在main方法之前运行，
     * 与main方法运行在同一个JVM中
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("=========premain方法执行 1========");
        System.out.println("agentArgs:="+agentArgs);

    }
     
    /**
     * 候选的、兜底 方法：
     * 如果不存在 premain(String agentArgs, Instrumentation inst)
     * 则会执行 premain(String agentArgs)
     *
     */
    public static void premain(String agentArgs) {
        System.out.println("=========premain 方法执行 2========");
        System.out.println("agentArgs:="+agentArgs);
    }

}
生效的声明方法
```
当定义完这两组方法后，要使之生效还需要手动声明，声明方式有两种：

1、使用 MANIFEST.MF 文件

我们需要创建resources/META-INF.MANIFEST.MF 文件，当 jar包打包时将文件一并打包，文件内容如下：

Manifest-Version: 1.0
Can-Redefine-Classes: true   # true表示能重定义此代理所需的类，默认值为 false（可选）
Can-Retransform-Classes: true    # true 表示能重转换此代理所需的类，默认值为 false （可选）
Premain-Class:  com.crazymaker.agent.javassist.demo.AgentDemo   #premain方法所在类的位置

2、如果是maven项目，在pom.xml加入

        <profile>
            <id>java-agent-demo</id>
            <properties>
                <hello>world</hello>
            </properties>
            <activation>
                <activeByDefault>false</activeByDefault>
            </activation>
            <build>
                <finalName>java-agent-demo</finalName>
                <plugins>
                    <plugin>
                        <artifactId>maven-shade-plugin</artifactId>
                        <executions>
                            <execution>
                                <phase>package</phase>
                                <goals>
                                    <goal>shade</goal>
                                </goals>
                                <configuration>
                                    <shadedArtifactAttached>false</shadedArtifactAttached>
                                    <createDependencyReducedPom>true</createDependencyReducedPom>
                                    <createSourcesJar>true</createSourcesJar>
                                    <shadeSourcesContent>true</shadeSourcesContent>
                                    <transformers>
                                        <transformer
                                                implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                            <manifestEntries>
                                                <Premain-Class>com.crazymaker.agent.javassist.demo.AgentDemo
                                                </Premain-Class>
                                                <Can-Redefine-Classes>true</Can-Redefine-Classes>
                                                <Can-Retransform-Classes>true</Can-Retransform-Classes>
                                            </manifestEntries>
                                        </transformer>
                                    </transformers>
                                    <artifactSet>
                                        <excludes>
                                            <exclude>*:gson</exclude>
                                            <exclude>io.netty:*</exclude>
                                            <exclude>io.opencensus:*</exclude>
                                            <exclude>com.google.*:*</exclude>
                                            <exclude>com.google.guava:guava</exclude>
                                            <exclude>org.checkerframework:checker-compat-qual</exclude>
                                            <exclude>org.codehaus.mojo:animal-sniffer-annotations</exclude>
                                            <exclude>io.perfmark:*</exclude>
                                            <exclude>org.slf4j:*</exclude>
                                        </excludes>
    
                                        <!-- 将javassist包打包到Agent中 -->
    
                                        <includes>
                                            <include>javassist:javassist:jar:</include>
                                        </includes>
                                    </artifactSet>
                                    <filters>
                                        <filter>
                                            <artifact>net.bytebuddy:byte-buddy</artifact>
                                            <excludes>
                                                <exclude>META-INF/versions/9/module-info.class</exclude>
                                            </excludes>
                                        </filter>
                                    </filters>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>

Agent的简单使用
要让目标JVM认你这个 Agent ，你就要给目标JVM介绍这个 Agent

1、JVM 启动时加载

我们直接在 JVM 启动参数中加入 -javaagent 参数并指定 jar 文件的位置

# 指定agent程序并运行该类
java -javaagent:-javaagent:D:\dev\SuperAPM\apm-agent\target\javassist-demo.jar  TargetJvm


/** * VM options： * -javaagent:D:\dev\SuperAPM\apm-agent\target\javassist-demo.jar */


2、JVM 运行时加载

要实现动态调试，我们就不能将目标JVM停机后再重新启动，这不符合我们的初衷，因此我们可以使用 JDK 的 Attach Api 来实现运行时挂载 Agent。

Attach Api 是 SUN 公司提供的一套扩展 API，用来向目标 JVM 附着（attach）在目标程序上，有了它我们可以很方便地监控一个 JVM。

Attach Api 对应的代码位于 com.sun.tools.attach包下，提供的功能也非常简单：

列出当前所有的 JVM 实例描述
Attach 到其中一个 JVM 上，建立通信管道
让目标JVM加载Agent
该包下有一个类 VirtualMachine，它提供了两个重要的方法：

VirtualMachine attach(String var0)
传递一个进程号，返回目标 JVM 进程的 vm 对象，该方法是 JVM进程之间指令传递的桥梁，底层是通过 socket 进行通信

void loadAgent(String var1)
该方法允许我们将 agent 对应的 jar 文件地址作为参数传递给目标 JVM，目标 JVM 收到该命令后会加载这个 Agent

有了 Attach Api ，我们就可以创建一个java进程，用它attach到对应的jvm，并加载agent。

ClassFileTransformer
我们需要了解的是，上述入口函数传入的第二个参数Instrumentation实例，即Java Instrument类 java.lang.instrument.Instrumentation ，查看其类定义，可以看到其提供的核心方法只有一个addTransformer，用于添加多个ClassFileTransformer，

// 说明：添加ClassFileTransformer
// 第一个参数：transformer，类转换器
// 第二个参数：canRetransform，经过transformer转换过的类是否允许再次转换
void Instrumentation.addTransformer(ClassFileTransformer transformer, boolean canRetransform)
而 ClassFileTransformer 则提供了tranform()方法，用于对加载的类进行增强重定义，返回新的类字节码流。

需要特别注意的是，若不进行任何增强，当前方法返回null即可，若需要增强转换，则需要先拷贝一份classfileBuffer，在拷贝上进行增强转换，然后返回拷贝。

// 说明：对类字节码进行增强，返回新的类字节码定义
// 第一个参数：loader，类加载器
// 第二个参数：className，内部定义的类全路径
// 第三个参数：classBeingRedefined，待重定义/转换的类
// 第四个参数：protectionDomain，保护域
// 第五个参数：classfileBuffer，待重定义/转换的类字节码（不要直接在这个classfileBuffer对象上修改，需拷贝后进行）
// 注：若不进行任何增强，当前方法返回null即可，若需要增强转换，则需要先拷贝一份classfileBuffer，在拷贝上进行增强转换，然后返回拷贝。
byte[] ClassFileTransformer.transform(ClassLoader loader, String className, Class classBeingRedefined, ProtectionDomain protectionDomain, byte classfileBuffer)
演示类TransformerAgentDemo


Javassist 修改字节码
加入一个转换器 Transformer ，之后所有的目标类加载都会被 Transformer 拦截，可自定义实现 ClassFileTransformer 接口，

重写ClassFileTransformer 接口的唯一方法 transform() 方法，返回值是转换后的类字节码文件

在 transform 方法中，通过 Javassist 修改字节码


测试案例
```java
package com.crazymaker.circle.agent;

public class Helloworld {
    public void sayHello()  {

        System.out.println("hello  world from 疯狂创客圈");  // Hello World!
    }
}


执行结果
Transformer:org/junit/runner/notification/RunNotifier$3   transforming skip, not the target class.
Transformer:org/junit/runners/model/FrameworkMethod$1   transforming skip, not the target class.
Disconnected from the target VM, address: '127.0.0.1:64236', transport: 'socket'
com/crazymaker/circle/agent/Helloworld:class transformed = %s
begin of sayhello()
hello  world from 疯狂创客圈
end of sayhello()
hi transformerAgentDemo
Transformer:org/junit/runner/notification/RunNotifier$7   transforming skip, not the target class.
Transformer:org/junit/runner/notification/RunNotifier$2   transforming skip, not the target class.
Transformer:java/lang/Shutdown   transforming skip, not the target class.
Transformer:java/lang/Shutdown$Lock   transforming skip, not the target class.
Instrumentation接口和ClassFileTransformer
```
上面的例子，使用 Java Instrumentation 来完成动态类修改的功能，并且在 Instrumentation 接口中我们可以通过 addTransformer() 方法来增加一个类转换器，

类转换器由类 ClassFileTransformer 接口实现。

该接口中有一个唯一的方法 transform() 用于实现类的转换，也就是我们可以增强类处理的地方！

当类被加载的时候就会调用 transform()方法，实现对类加载的事件进行拦截并返回转换后新的字节码，通过 redefineClasses()或retransformClasses()都可以触发类的重新加载事件。

首先我们先了解一下 Instrumentation 这个接口，其中有几个方法：

addTransformer(ClassFileTransformer transformer, boolean canRetransform)
自定义一个字节码转换器 Transformer ，之后所有的目标类加载都会被 Transformer 拦截，、

如何定义呢？

可自定义实现 ClassFileTransformer 接口，重写该接口的唯一方法 transform() 方法，返回值是转换后的类字节码文件

retransformClasses(Class<?>... classes)
对 JVM 已经加载的类重新触发类加载，使用上面自定义的转换器进行处理。该方法可以修改方法体，常量池和属性值，但不能新增、删除、重命名属性或方法，也不能修改方法的签名

redefineClasses(ClassDefinition... definitions)
此方法用于替换类的定义，而不引用现有类文件字节。

getObjectSize(Object objectToSize)
获取一个对象的大小

appendToBootstrapClassLoaderSearch(JarFile jarfile)
将一个 jar 文件添加到 bootstrap classload 的 classPath 中

getAllLoadedClasses()
获取当前被 JVM 加载的所有类对象

redefineClasses 和 retransformClasses 补充说明

两者区别：
redefineClasses 是自己提供字节码文件替换掉已存在的 class 文件
retransformClasses 是在已存在的字节码文件上修改后再进行替换

替换后生效的时机
如果一个被修改的方法已经在栈帧中存在，则栈帧中的方法会继续使用旧字节码运行，新字节码会在新栈帧中运行

注意点
两个方法都是只能改变类的方法体、常量池和属性值，但不能新增、删除、重命名属性或方法，也不能修改方法的签名

Java字节码增强类库 - Javassist
Javassist 是一个非常早的字节码操作类库，开始于1999年，

它能够支持两种编辑方式：

源码级别
字节码指令级别，
相比于晦涩的字节码级别，源码级别更加人性化，代码编写起来更加易懂。

以上面的ASM字节码指令编辑为例，换成对应的Javassist源码级别编辑方式，如下所示，

CtMethod m = cc.getDeclaredMethod("sayHello");
m.insertBefore("{ System.out.println(\"begin of sayhello()\"); }");
相信大多数程序员更愿意接受源码级别编辑方式，翻译成直接码指令的工作就交给Javassist完成，目前源码级别方式Javassist只支持Java语言语法。

演示代码

下载演示代码，见 这里 。

编译项目 mvn clean package

运行命令

java -javaagent:./demo-javaassist/target/agent-jassist.jar -jar ./demo-app/target/demo-app.jar
可以通过控制台查看日志。
Java字节码增强类库 - ASM
ASM 是一个Java字节码解析和操作框架，整个类包非常小，还不到120KB，但其非常注重对类字节码的操作速度，

这种高性能来自于它的设计模式 - 访问者模式，即通过Reader、Visitor和Writer模式。

ASM是直接操作类字节码数据，因此其读写的是字节码指令，比如，

mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitLdcInsn("begin of sayhello().");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
这种指令比较晦涩难懂，在实际操作过程中，会先将期望的类源码文件写好，编译后查看字节码文件，然后复制相关字节码指令。

演示代码

下载演示代码，见 这里 。

编译项目 mvn clean package

运行命令

java -javaagent:./demo-asm/target/agent-asm.jar -jar ./demo-app/target/demo-app.jar
可以通过控制台查看日志。
Java字节码增强工具关系图


需要提一下，JDK Proxy和Cglib也是以代码方式进行类方法的切面增强，但它们都是以框架的方式实现了Java类的动态扩展，主要应用在框架级别的字节码增强，在某种程度上JDK Proxy和Cglib技术对应用是有代码侵入的，这里的侵入不仅仅是框架代码侵入，而且包括增强的类中依赖JDK Proxy和Cglib类。

与此相比，ButeBuddy API是以无侵入方式加强类代码，设计理念更优。

Java字节码增强工具对比
对比	ASM	Javassist	JDK Proxy	Cglib	ByteBuddy
起源时间	2002	1999	2000	2011	2014
包大小	130KB （版本9.3）	788KB （版本3.28.0-GA）			3.7MB （版本1.10.19）
增强方式	字节码指令	字节码指令和源码（注：源码文本）	源码	源码	源码
源码编译	NA	不支持	支持	支持	支持
agent支持	支持	支持	不支持，依赖框架	不支持，依赖框架	支持
性能	高	中	低	中	中
维护状态	是	是	停止升级	停止维护	活跃
优点	超高性能，应用场景广泛	同时支持字节码指令和源码两种增强方式	JDK原生类库支持		零侵入，提供良好的API扩展编程
缺点	字节码指令对应用开发者不友好		场景非常局限，只适用于Java接口	已经不再维护，对于新版JDK17+支持不好，官网建议切换到ByteBuddy	
应用场景	小，高性能，广泛用于语言级别			广泛用于框架场景	广泛用于Trace场景
注：相关性能数据来自 这里

综合了上述的字节码增强工具对比，比较了开发便利性和需求目标，我们最后选择了ByteBuddy来实现Trace跟踪技术。

Byte Buddy简介
Byte Buddy是一个字节码生成和操作库，用于在Java应用程序运行时创建和修改Java类，而无需编译器的帮助。

除了Java类库附带的代码生成实用程序外，Byte Buddy还允许创建任意类，并且不限于实现用于创建运行时代理的接口。

此外，Byte Buddy提供了一种方便的API，可以使用Java代理或在构建过程中手动更改类。

无需理解字节码指令，即可使用简单的 API 就能很容易操作字节码，控制类和方法。

已支持Java 11，库轻量，仅取决于Java字节代码解析器库ASM的访问者API，它本身不需要任何其他依赖项。
比起JDK动态代理、cglib、Javassist，Byte Buddy在性能上具有一定的优势。

就像它的官网介绍；

Byte Buddy 是一个代码生成和操作库，用于在 Java 应用程序运行时创建和修改 Java 类，而无需编译器的帮助。除了 Java 类库附带的代码生成实用程序外，Byte Buddy 还允许创建任意类，并且不限于实现用于创建运行时代理的接口。

此外，Byte Buddy 提供了一种方便的 API，可以使用 Java 代理或在构建过程中手动更改类。

无需理解字节码指令，即可使用简单的 API 就能很容易操作字节码，控制类和方法。
已支持Java 11，库轻量，仅取决于Java字节代码解析器库ASM的访问者API，它本身不需要任何其他依赖项。
比起JDK动态代理、cglib、Javassist，Byte Buddy在性能上具有一定的优势。
2015年10月，Byte Buddy被 Oracle 授予了 Duke’s Choice大奖。

该奖项对Byte Buddy的“ Java技术方面的巨大创新 ”表示赞赏。我们为获得此奖项感到非常荣幸，并感谢所有帮助Byte Buddy取得成功的用户以及其他所有人。我们真的很感激！

除了这些简单的介绍外，还可以通过官网：https://bytebuddy.net，去了解更多关于 Byte Buddy 的内容。

开发环境
JDK 1.8.0
byte-buddy 1.10.19
byte-buddy-agent 1.10.19
使用bytebuddy只需要简单的引入其maven依赖即可

```xml
      <dependency>
            <groupId>net.bytebuddy</groupId>
            <artifactId>byte-buddy</artifactId>
            <version>1.10.19</version>
        </dependency>
官网经典例子
```
在我们看官网文档中，从它的介绍了就已经提供了一个非常简单的例子，用于输出 HelloWorld，

我们在这展示并讲解下。

HelloWorld案例代码：
String helloWorld = new ByteBuddy()
            .subclass(Object.class)
            .method(named("toString"))
            .intercept(FixedValue.value("Hello World!"))
            .make()
            .load(getClass().getClassLoader())
            .getLoaded()
            .newInstance()
            .toString();    

```java
System.out.println(helloWorld);  // Hello World!

```
他的运行结果就是一行，Hello World!，

整个代码块核心功能:

step 1: 通过 method(named(“toString”))，找到 toString 方法，

step 2:再通过拦截 intercept，设定此方法的返回值。FixedValue.value(“Hello World!”)。

到这里其实一个基本的方法就通过 Byte-buddy ，改造完成。

step 3:接下来的这一段主要是用于加载生成后的 Class
stetp4: newInstance().toString(); 的作用 是： 执行以及调用方法 toString()。
也就是最终我们输出了想要的结果。


各个调用是干啥的：

subclass(Object.class) ：创建一个Object的子类
name(“ExampleClass”) : 新建的类名叫做“ExampleClass” ,暂时没有用到
method() ：要拦截“ExampleClass”中的方法
ElementMatchers.named(“toString”) ：拦截条件，拦截toString()这个方法, 没有条件，表示所有的方法
intercept() ：指定了拦截到的方法要修改成什么样子，是不是和 Spring AOP有点像了
make() ：创建上面生成的这个类型
load() ：加载这个生成的类
newInstance() ：Java 反射的API，创建实例
编译后的Class文件
我们通过字节码输出到文件，看下具体被改造后的样子

在Byte buddy中默认提供了一个 dynamicType.saveIn() 方法，可以保存编译后的Class文件


可以更加清晰的看到每一步对字节码编程后，所创建出来的方法样子(clazz)

输出的class 文件，反编译过来的 java 文件，idea打开 如下：


输出的class 文件

ObjectB y t e B u d d y ByteBuddyByteBuddyXXX

自定义输出的类名
如果不写类名， dynamicType.saveIn() 方法会自动生成要给类名。


可以拿到字节码之后，自定义输出字节码方法

```java
private static void outputClazz(byte[] bytes,String clazzName) {
        FileOutputStream out = null;
        try {
            String pathName = BytebuddyTest.class.getResource("/").getPath() + clazzName+".class";
            out = new FileOutputStream(new File(pathName));
            System.out.println("类输出路径：" + pathName);
            out.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

```
这个方主要就是一个 Java 基础的内容，输出字节码到文件中。

接下来，使用这个方法。

一共两step：

step1： name 设置 类名

step2： output 里边，设置文件名称


输出的class 文件，反编译过来的 java 文件，idea打开 如下：


为了可以更加清晰的看到每一步对字节码编程后，所创建出来的方法样子(clazz)，

字节码创建类和方法
接下来的例子会通过一点点的增加代码梳理，不断的把一个方法完整的创建出来。

创建类的基础结构
subclass 设置基类

name 设置类的名称, 如果不写类名会自动生成要给类名。

例子如下：

DynamicType.Unloaded<?> dynamicType = new ByteBuddy()
        .subclass(Object.class)
        .name("com.crazymaker.circle.bytecode.enhancement.HelloWorld")
        .make();

通过增强之后，得到 DynamicType.Unloaded 对象，

三种动态增强方式
DynamicType.Unloaded<?> dynamicType = new ByteBuddy()
        .subclass(Object.class) // 生成 Object的子类
        .name("com.fatsnake.Type")   // 生成类的名称为"com.xxx.Type"
        .make();
subclass：

对应 ByteBuddy.subclass() 方法。这种方式比较好理解，就是为目标类（即被增强的类）生成一个子类，在子类方法中插入动态代码。

rebasing：

对应 ByteBuddy.rebasing() 方法。

当使用 rebasing 方式增强一个类时，Byte Buddy 保存目标类中所有方法的实现，

也就是说，当 Byte Buddy 遇到冲突的字段或方法时，会将原来的字段或方法实现复制到具有兼容签名的重新命名的私有方法中，而不会抛弃这些字段和方法实现。

从而达到不丢失实现的目的。

这些重命名的方法可以继续通过重命名后的名称进行调用。

例如：

```java
class Foo { // Foo的原始定义

  String bar() { return "bar"; }
}

class Foo { // 增强后的Foo定义
  String bar() { return "foo" + bar$original(); }
// 目标类原有方法
  private String bar$original() { return "bar"; }
redefinition：

```
对应 ByteBuddy.redefine() 方法。

当重定义一个类时，Byte Buddy 可以对一个已有的类添加属性和方法，删除已经存在的方法实现。

如果使用其他的方法实现, 去替换已经存在的方法实现，则原来存在的方法实现就会消失。

例如，这里依然是增强 Foo 类的 bar() 方法使其直接返回 “unknow” 字符串，增强结果如下：

```java
class Foo { // 增强后的Foo定义
  String bar() { return "unknow"; }
}
类加载策略
```
DynamicType.Unloaded 对象，表示的是一个未加载的类型，通过在 ClassLoadingStrategy.Default中定义的加载策略，加载此类型。

Class<?> loaded = new ByteBuddy()
        .subclass(Object.class)
        .name("com.xxx.Type")
        .make()
        // 使用 WRAPPER 策略加载生成的动态类型
        .load(Main2.class.getClassLoader(), 
              ClassLoadingStrategy.Default.WRAPPER)
        .getLoaded();
WRAPPER 策略：创建一个新的 ClassLoader 来加载动态生成的类型。
CHILD_FIRST 策略：创建一个子类优先加载的 ClassLoader，即打破了双亲委派模型。
INJECTION 策略：使用反射, 将动态生成的类型直接注入到当前 ClassLoader 中。
创建方法
defineMethod 定义方法
withParameter 设置参数
intercept 拦截设置返回值
创建main方法的代码如下：


与上面相比新增的代码片段；

defineMethod(“main”, String.class, Modifier.PUBLIC + Modifier.STATIC)，

定义方法；名称、返回类型、属性public static void

Modifier.PUBLIC + Modifier.STATIC，这是一个是二进制相加，每一个类型都在二进制中占有一位。例如 1 2 4 8 … 对应的二进制占位 1111。既可以执行相加运算，并又能保留原有单元的属性。

withParameter(String[].class, “args”)，

定义参数；参数类型、参数名称

intercept(FixedValue.value(“Hello World!”))，

拦截设置返回值，但此时还能满足我们的要求。

输出的class 文件，反编译过来的 java 文件，idea打开 如下：


此时基本已经可以看到我们平常编写的 Hello World 影子了，但还能输出结果。

注意，如果返回值为void，那么 intercept设置的，变成了 一个局部变量了

可以尝试一下

创建字段
defineField() 方法：创建字段。

实现接口
implement() 方法：实现接口。

下面是一个例子


输出的class 文件，反编译过来的 java 文件，idea打开 如下：


重点：委托函数调用
这是重点：

为了能让我们使用字节码编程创建的方法,去调用另外一个同名方法，那么这里需要使用到委托。

委托函数调用实例
通过 MethodDelegation 去完成

在intercept方法中，使用MethodDelegation.to委托到静态方法
intercept(MethodDelegation.to(DelegateClazz.class)) // 委托到 DelegateClazz 的静态方法

在intercept方法中，使用MethodDelegation.to委托到成员方法
intercept(MethodDelegation.to(new DelegateClazz()) // 委托到 DelegateClazz 的实例方法

实例：委托到静态方法


上面的 intercept(MethodDelegation.to(DelegateClazz.class))是一个委托操作，一段委托函数，真正去执行输出被委托的函数方法。

被委托的方法，需要是 public 类
被委托的方法与需要与原方法有着一样的入参、出参、方法名，否则不能映射上
输出的class 文件，反编译过来的 java 文件，idea打开 如下：


那么此时就可以输出我们需要的内容了，


委托并不是根据名称来的，而是和 Java 编译器在选重载时用的参数绑定类似

实例：委托到动态方法
前面示例中要委托到 DelegateClazz 的静态方法，这里要委托到 DelegateClazz 的实例方法需要在 MethodDelegation.to() 方法中传递


intercept(MethodDelegation.to(DelegateClazz.class)) // 委托到 Interceptor的静态方法
    
MethodDelegation.to(new DelegateClazz()) // 委托到 DelegateClazz 的实例方法
通过反射执行方法
这个和bytebuddy已经没有太多关系了

通过getLoaded()，可以拿到 bytebuddy 生产的字节码锁加载之后的 class 对象

然后通过class对象的反射机制，为了可以让整个方法运行起来，

我们需要添加字节码加载和反射调用的代码块，如下；

// 加载类clazz
Class<?> clazz = type.getLoaded();

// 反射调用
try {
String bar = (String) clazz.getMethod("foo").invoke(clazz.newInstance());
```java
System.out.println(bar);

} catch (InvocationTargetException e) {
e.printStackTrace();
} catch (NoSuchMethodException e) {
e.printStackTrace();
}


注解方式
```
除了通过上述 API 拦截方法并将方法实现委托给 Interceptor 增强之外，Byte Buddy 还提供了一些预定义的注解，

通过这些注解我们可以告诉 Byte Buddy 将哪些需要的数据注入到 Interceptor 中

常用注解含义
```java
@RuntimeType 注解：

```
告诉 Byte Buddy 不要进行严格的参数类型检测，在参数匹配失败时，尝试使用类型转换方式（runtime type casting）进行类型转换，匹配相应方法。

```java
@This 注解：

注入被拦截的目标对象。

@AllArguments 注解：

```
注入目标方法的全部参数，是不是感觉与 Java 反射的那套 API 有点类似了？

```java
@Origin 注解：

```
注入目标方法对应的 Method 对象。如果拦截的是字段的话，该注解应该标注到 Field 类型参数。

```java
@Super 注解：

```
注入目标对象。通过该对象可以调用目标对象的所有方法。

```java
@SuperCall：

```
这个注解比较特殊，我们要在 intercept() 方法中调用目标方法的话，需要通过这种方式注入，

```java
@SuperCall与 Spring AOP 中的 ProceedingJoinPoint.proceed() 方法有点类似，需要注意的是，这里不能修改调用参数，从上面的示例的调用也能看出来，参数不用单独传递，都包含在其中了。

```
另外，@SuperCall 注解还可以修饰 Runnable 类型的参数，只不过目标方法的返回值就拿不到了。

使用注解的例子

```java
  public   static  class DelegeteFoo {
        public String hello(String name) {
            System.out.println("DelegeteFoo:" + name);
            return null;
        }
    }


    public  static class Interceptor {
        @RuntimeType
        public Object intercept(
                @This Object obj, // 目标对象
                @AllArguments Object[] allArguments, // 注入目标方法的全部参数
```
                @SuperCall Callable<?> zuper, // 调用目标方法，必不可少哦
```java
                @Origin Method method, // 目标方法
                @Super DelegeteFoo delegeteFoo // 目标对象
        ) throws Exception {
            System.out.println("obj="+obj);
            System.out.println("delegeteFoo ="+ delegeteFoo);
```
            // 从上面两行输出可以看出，obj和db是一个对象
            try {
                return zuper.call(); // 调用目标方法
            } finally {
            }
        }
    
    }


```java
@Test
    public void annotateDelegateTest() throws IllegalAccessException, InstantiationException {

        DynamicType.Unloaded<DelegeteFoo> dynamicType = new ByteBuddy()
                .subclass(DelegeteFoo.class)
                .name("com.crazymaker.circle.bytecode.enhancement.Foo")
                .method(named("hello"))
                .intercept(MethodDelegation.to(new Interceptor()))
                .make();
    
        // 加载字节码
        DynamicType.Loaded<DelegeteFoo> type = dynamicType.load(getClass().getClassLoader());
    
        // 输出类字节码
        outputClazz(dynamicType.getBytes(), "com.crazymaker.circle.bytecode.enhancement.Foo");


        //加载类
        Class<?> clazz = type.getLoaded();
    
        // 反射调用
        try {
            String bar = (String) clazz.getMethod("hello",String.class).invoke(clazz.newInstance(),"bar - from 疯狂创客圈");
            System.out.println(bar);
    
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


    }
执行的结果

obj=com.crazymaker.circle.bytecode.enhancement.Foo@5e316c74
delegeteFoo =com.crazymaker.circle.bytecode.enhancement.Foo@5e316c74
DelegeteFoo:bar - from 疯狂创客圈
 result from DelegeteFoo 

```
输出的class 文件，反编译过来的 java 文件，idea打开 如下：


```java
@Morph与@SuperCall
```
@SuperCall 注解注入的 Callable 参数来调用目标方法时，是无法动态修改参数的，

如果想要动态修改参数，则需要用到 @Morph 注解以及一些绑定操作

  DynamicType.Unloaded<DelegeteFoo> dynamicType = new ByteBuddy()
                .subclass(DelegeteFoo.class)
                .name("com.crazymaker.circle.bytecode.enhancement.Foo")
                .method(named("hello"))
                .intercept(MethodDelegation.withDefaultConfiguration()
                .withBinders(
                        // 要用@Morph注解之前，需要通过 Morph.Binder 告诉 Byte Buddy
                        // 要注入的参数是什么类型
                        Morph.Binder.install(OverrideCallable.class)
                ).to(new InterceptorMorph()))
                .make();
Interceptor 会使用 @Morph 注解注入一个 OverrideCallable 对象作为参数，然后通过该 OverrideCallable 对象调用目标方法

```java
 public  static   class InterceptorMorph {
        @RuntimeType
        public Object intercept(@This Object obj,
                                @AllArguments Object[] allArguments,// 注入目标方法的全部参数
                                @Origin Method method,
                                @Super DelegeteFoo delegeteFoo,
                                @Morph OverrideCallable callable // 通过@Morph注解注入
        ) throws Throwable {
            try {
                System.out.println("obj="+obj);
                System.out.println("delegeteFoo ="+ delegeteFoo);
                System.out.println("method ="+method);
                System.out.println("callable ="+callable);
                System.out.println("allArguments ="+allArguments);
                System.out.println("before");
```
                // 通过 OverrideCallable.call()方法调用目标方法，此时需要传递参数
                allArguments[0]="word replaced";
                Object result = callable.call(allArguments);
```java
                System.out.println("result ="+result);
                System.out.println("after");
                return result;
            } catch (Throwable t) {
                throw t;
            } finally {
                System.out.println("finally");
            }
        }
    }

```
最后，这里使用的 OverrideCallable 是一个自定义的接口，如下所示：

```java
public interface OverrideCallable {
    Object call(Object[] args);
}

拦截构造方法
```
除了拦截 static 方法和实例方法，Byte Buddy 还可以拦截构造方法，这里依然通过一个示例进行说明。

拦截构造方法的步骤：

使用 constructor() 方法拦截构造方法，

并且使用 SuperMethodCall 调用构造方法并委托给 Interceptor 实例，

首先修改 DelegeteFoo 这个类，为它添加一个构造方法，如下所示：


```java
  public   static  class DelegeteFoo {

      public DelegeteFoo(String name) {
          System.out.println(" 构造器 DelegeteFoo ： " +name);
      }
      public String hello(String name) {
            System.out.println("DelegeteFoo:" + name);
            return " result from DelegeteFoo ";
        }
    }

使用的 Interceptor 与前文使用的类似：

    class ConstructorInterceptor {
        @RuntimeType
        public void intercept(@This Object obj,
                              @AllArguments Object[] allArguments) {
            System.out.println("after!");
        }
    }

```
这里不再使用 method() 方法拦截，而是使用 constructor() 方法拦截构造方法，

并且使用 SuperMethodCall 调用构造方法并委托给 Interceptor 实例，具体实现如下：

```java
@Test
    public void constructorInterceptTest() throws IllegalAccessException, InstantiationException {

        DynamicType.Unloaded<DelegeteFoo> dynamicType = new ByteBuddy()
                .subclass(DelegeteFoo.class)
                .name("com.crazymaker.circle.bytecode.enhancement.Foo")
                .constructor(any())
                // 通过constructor()方法拦截所有构造方法
```
                // 拦截的操作：首先调用目标对象的构造方法，根据前面自动匹配，
                // 这里直接匹配到参数为String.class的构造方法
                .intercept(SuperMethodCall.INSTANCE.andThen(
                        // 执行完原始构造方法，再开始执行interceptor的代码
                        MethodDelegation.withDefaultConfiguration().to(new ConstructorInterceptor())
                ))
    
                .make();
    
        // 加载字节码
        DynamicType.Loaded<DelegeteFoo> type = dynamicType.load(getClass().getClassLoader(), INJECTION);
    
        // 输出类字节码
        outputClazz(dynamicType.getBytes(), "com.crazymaker.circle.bytecode.enhancement.Foo");


        //加载类
        Class<?> clazz = type.getLoaded();
    
        // 反射调用
        try {
    
            Constructor<?> constructor = clazz.getConstructor(String.class);
            DelegeteFoo foo = (DelegeteFoo) constructor.newInstance("name from 疯狂创客圈");
```java
            System.out.println(foo.hello("hello form 疯狂创客圈"));
    
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }


    }

```
SuperMethodCall 会在新生成的方法中先调用目标方法，

如果未找到目标方法则抛出异常，如果目标方法是构造方法，则根据方法签名匹配。

输出如下

构造器 DelegeteFoo ： name from 疯狂创客圈
after constructor!
DelegeteFoo:hello form 疯狂创客圈
 result of DelegeteFoo 

拦截实例通过bytebuddy进行耗时计算
拦截器代码
```java
package com.crazymaker.agent.demo.bytebuddy;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class MethodCostTime {

    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
        long start = System.currentTimeMillis();
        try {
            // 原有函数执行
            return callable.call();
        } finally {
            System.out.println(method + " 方法耗时：" + (System.currentTimeMillis() - start) + "ms");
        }
    }

}
拦截器的使用
package com.crazymaker.agent.demo.bytebuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ByteBuddyAgentDemo {

    private static final Logger log = LoggerFactory.getLogger(ByteBuddyAgentDemo.class);
    
    private final static String scanPackage = "com.crazymaker.circle.agent.demo";
    
    private final static String targetMethod = "sayHello";

//    private final static String implInterface = "org.springframework.cloud.gateway.filter.GlobalFilter";


    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println(">>>>> ByteBuddyAgentDemo - premain()");
        final ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(false));
        new AgentBuilder.Default(byteBuddy)
                .type(nameStartsWith(scanPackage))
                .transform(new Transformer()) // update the byte code
                .with(new Listener())
                .installOn(inst);
    }
    
    /**
     *
     */
    private static class Transformer implements AgentBuilder.Transformer {
        @Override
        public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
            if (typeDescription.getPackage().getActualName().equals(scanPackage)
//                    && typeDescription.getInterfaces().size() > 0
//                    && typeDescription.getInterfaces().get(0).getActualName().equals(implInterface)
                    ) {
                String targetClassName = typeDescription.getSimpleName();
                System.out.println("----------------------- target class:" + targetClassName);

                // 委托
                return builder.method(named(targetMethod)
                        .and(isPublic())).intercept(MethodDelegation.to(MethodCostTime.class));
    
            }
            return builder;
        }
    }
    
    /**
     * Listener
     */
    private static class Listener implements AgentBuilder.Listener {
    
        private int count;
    
        @Override
        public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            if (typeName.startsWith(scanPackage)) {
                System.out.println("--- onDiscovery ---" + typeName);
            }
        }
    
        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded, DynamicType dynamicType) {
            if (typeDescription.getSimpleName().startsWith(scanPackage)) {
                System.out.println("--- onTransformation ---" + typeDescription.getSimpleName());
            }
        }
    
        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, boolean loaded) {
        }
    
        @Override
        public void onError(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded, Throwable throwable) {
            if (typeName.startsWith(scanPackage)) {
                System.out.println("--- onError ---" + throwable);
            }
        }
    
        @Override
        public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
            if (typeName.startsWith(scanPackage)) {
                System.out.println("--- onComplete ---" + typeName);
            }
        }
    }

}
测试效果

    /**
     * VM options：
     * -javaagent:D:\dev\SuperAPM\apm-agent\target\byteBuddy-demo.jar
     */
    
    @Test
    public void byteBuddyAgentDemo() throws IllegalAccessException, InstantiationException {
    
        new Helloworld().sayHello();
        System.out.println("hi byteBuddy AgentDemo ----------");
    
    }


性能
```
在选择字节码操作库时，往往需要考虑库本身的性能。对于许多应用程序，生成代码的运行时特性更有可能确定最佳选择。而在生成的代码本身的运行时间之外，用于创建动态类的运行时也是一个问题。官网对库进行了性能测试，给出以下结果图：


图中的每一行分别为，类的创建、接口实现、方法调用、类型扩展、父类方法调用的性能结果。从性能报告中可以看出，Byte Buddy 的主要侧重点在于以最少的运行时生成代码，需要注意的是，我们这些衡量 Java 代码性能的测试，都由 Java 虚拟机即时编译器优化过，如果你的代码只是偶尔运行，没有得到虚拟机的优化，可能性能会有所偏差。所以我们在使用 Byte Buddy 开发时，我们希望监控这些指标，以避免在添加新功能时造成性能损失。

参考文献
https://bytebuddy.net/#/tutorial bytebuddy官方文档
https://juejin.cn/post/6844903965553852423#heading-12

https://blog.csdn.net/m0_71777195/article/details/125638010

https://bytebuddy.net/#/tutorial

https://www.jianshu.com/p/9d9b345aedc0

https://blog.csdn.net/qq_37362891/article/details/119904045

https://www.cnblogs.com/hlkawa/p/16187162.html

https://www.cnblogs.com/xuxiaojian/p/14492018.html

https://my.oschina.net/itstack/blog/4409838

https://blog.csdn.net/unix21/article/details/81908817

https://zhuanlan.zhihu.com/p/441580854

https://my.oschina.net/itstack/blog/4409838

https://www.cnblogs.com/xuxiaojian/p/14492018.html

