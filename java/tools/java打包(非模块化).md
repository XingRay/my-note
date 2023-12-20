# jpackage打包全流程

环境：

Apache Maven 3.8.4 (9b656c72d54e5bacbed989b64718c159fe39b537)
Java version: 17.0.1, vendor: Oracle Corporation
Default locale: zh_CN, platform encoding: GBK
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"



## 全流程示例(windows平台)：

## 1. 创建项目，引入依赖，编写代码，测试运行通过

在项目中需要通过pom引入插件

```xml
<properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <java.version>17</java.version>
        <javafx.version>17.0.2</javafx.version>
        <jackson.version>2.13.2</jackson.version>
        <launcher.class>com.xingray.demo.launcher.Launcher</launcher.class>
</properties>
<dependencies>
    ...
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <release>${java.version}</release>
                <target>${java.version}</target>
            </configuration>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <version>3.3.0</version>
            <configuration>
                <outputDirectory>output/dependency</outputDirectory>
            </configuration>
        </plugin>

        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>2.4</version>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>${launcher.class}</mainClass>
                    </manifest>
                </archive>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <outputDirectory>${project.basedir}/output/jarWithDependencies</outputDirectory>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

```

代码运行测试通过后执行下列指令:



```shell
# 清理代码产物
mvn clean

# 打包，并且通过插件，生成 Demo-1.0.0-jar-with-dependencies.jar，保存在output/jarWithDependencies目录中
mvn package

# 将项目依赖的所有jar包复制到 output/dependency 目录中
mvn dependency:copy-dependencies

# 执行jdeps命令，获取项目的依赖模块列表
jdeps ^
--multi-release 17 ^
--print-module-deps ^
--ignore-missing-deps ^
--recursive ^
--add-modules=ALL-MODULE-PATH ^
--class-path .\output\dependency ^
--module-path .\output\dependency ^
./output/jarWithDependencies/Demo-0.0.1-jar-with-dependencies.jar

#输出示例：
java.base,java.desktop,java.instrument,java.management,java.naming,java.scripting,java.sql,jdk.compiler,jdk.jfr,jdk.unsupported

# 根据具体的项目，依赖的模块会有不同，这个模块列表在jpackage中作为 --add-modules 参数

# 执行jpackage打包，此处示例直接输出exe文件
jpackage ^
--icon ./src/main/resources/images/launcher.ico ^
--type app-image ^
--dest ./output/package ^
--name Demo ^
--app-version "1.0.0" ^
--copyright "xingray.com" ^
--description "demo for jpackage" ^
--vendor "xingray" ^
--resource-dir ./src/main/resources ^
--input ./output/jarWithDependencies ^
--main-jar Demo-0.0.1-jar-with-dependencies.jar ^
--main-class com.xingray.demo.launcher.Launcher ^
--module-path ./target/classes;./output/dependency ^
--add-modules java.base,java.desktop,java.instrument,java.management,java.naming,java.scripting,java.sql,jdk.compiler,jdk.jfr,jdk.unsupported ^

# exe文件生成并保存在 output/package/Demo 目录下


# 如果需要安装包则执行下列命令：
jpackage ^
--icon ./src/main/resources/images/launcher.ico ^
--win-dir-chooser ^
--win-shortcut ^
--install-dir xingray/Demo ^
--dest ./output/package ^
--name demo ^
--app-version "1.0.0" ^
--copyright "xingray.com" ^
--description "demo for jpackage" ^
--vendor "xingray" ^
--resource-dir ./src/main/resources ^
--input ./output/jarWithDependencies ^
--main-jar Demo-0.0.1-jar-with-dependencies.jar ^
--main-class com.xingray.demo.launcher.Launcher ^
--module-path ./target/classes;./output/dependency ^
--add-modules java.base,java.desktop,java.instrument,java.management,java.naming,java.scripting,java.sql,jdk.compiler,jdk.jfr,jdk.unsupported ^

# 安装包保存到了 /output/package 目录下
```
