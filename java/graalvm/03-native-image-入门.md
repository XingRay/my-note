## GraalVM 原生应用开发

在GraalVM中安装了配置了 GraalVM和 native-image 插件及相关开发环境后，下面介绍基于 graalvm的原生应用开发流程



### 1. 通过 Class

```bash
native-image [options] class [imagename] [options]
```

如：

```java
public class HelloWorld {
     public static void main(String[] args) {
         System.out.println("Hello, Native World!");
     }
 }
```

先使用javac编译，再讲class文件转化为 native-image

```bash
javac HelloWorld.java
native-image HelloWorld
```

执行

```bash
./helloworld
```

或者

```cmd
.\HelloWorld.exe
```

如果是存在package路径，则使用下列命令

```bash
javac .\com\xingray\nativeimage\hello\HelloWorld.java
native-image com.xingray.nativeimage.hello.HelloWorld
```

注意 native-image 接收的是**全类名**

或者

```cmd
mvn clean compile
native-image --module-path target\classes com.xingray.nativeimage.hello.HelloWorld
```

```bash
mvn clean compile
native-image --class-path target\classes com.xingray.nativeimage.hello.HelloWorld
```

注意 classpath和 modulepath 不要包含package，指向根目录即可



### 2. 通过jar文件生成

创建一个maven项目，代码如下：

```java
package com.xingray.nativeimage.jarfile;

public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("hello world");
    }
}
```

执行程序如下

```bash
# 编译
mvn clean compile
# 切换到class目录
cd target\classes
# 打包，注意要加上mainclass
jar -c -f native-image-02-jarfile-HelloWorld.jar --main-class=com.xingray.nativeimage.jarfile.HelloWorld com\xingray\nativeimage\jarfile\HelloWorld.class
# 测试jar包
java -jar native-image-02-jarfile-HelloWorld.jar
# 打包成 native-image
native-image -jar native-image-02-jarfile-HelloWorld.jar
# 测试 native-image
native-image-02-jarfile-HelloWorld.exe
```

其中打成jar包部分可以通过maven插件实现

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>com.xingray.nativeimage.jarfile.HelloWorld</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

运行mvn指令，即可打出jar包

```bash
mvn clean package
```

基于java模块化技术，可以通过一下指令产生基于模块化的native-image

```bash
# 打包
mvn clean package

# 测试
java --module-path target/native-image-02-jarfile-1.0.0.jar --module com.xingray.nativeimage.jarfile

# 生成 native-image
native-image --module-path target/native-image-02-jarfile-1.0.0.jar --module com.xingray.nativeimage.jarfile

```





