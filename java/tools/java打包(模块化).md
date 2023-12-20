# jpackage打包全流程

环境：

Apache Maven 3.8.4 (9b656c72d54e5bacbed989b64718c159fe39b537)
Java version: 17.0.1, vendor: Oracle Corporation
Default locale: zh_CN, platform encoding: GBK
OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"



## 全流程示例(windows平台-模块化)：

```shell
java -version
mvn -version

mvn clean
mvn compile
mvn dependency:copy-dependencies

jdeps ^
--multi-release 17 ^
--print-module-deps ^
--ignore-missing-deps ^
--recursive ^
--add-modules=ALL-MODULE-PATH ^
--class-path .\output\dependency ^
--module-path .\output\dependency ^
.\target\classes


jpackage ^
--icon ./src/main/resources/images/launcher.ico ^
--dest ./output/package ^
--name PackageDemo ^
--app-version "1.0.0" ^
--copyright "xingray.com" ^
--description "java package demo" ^
--vendor "xingray" ^
--win-dir-chooser ^
--win-shortcut ^
--install-dir xingray/PackageDemo ^
--resource-dir ./src/main/resources ^
--module com.xingray.PackageDemo/com.xingray.packagedemo.app.Launcher ^
--add-modules java.base,java.desktop,java.scripting,jdk.jfr,jdk.unsupported ^
--module-path ./target/classes;./output/dependency ^
```

**jpackage命令中的 --add-modules   参数来自于jdeps命令输出**

需要在pom.xml中引入插件

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>17</source>
                <target>17</target>
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

    </plugins>
</build>
```





## windows下打包流程
### jdeps分析依赖

```shell
jdeps --add-modules=ALL-MODULE-PATH --multi-release 17 --list-deps --ignore-missing-deps --recursive --compile-time --module-path ^
D:\develop\apache\maven\repository\org\netbeans\api\org-netbeans-swing-tabcontrol\RELEASE126\org-netbeans-swing-tabcontrol-RELEASE126.jar;^
D:\develop\apache\maven\repository\dom4j\dom4j\1.6.1\dom4j-1.6.1.jar;^
D:\develop\apache\maven\repository\org\projectlombok\lombok\1.18.22\lombok-1.18.22.jar;^
D:\develop\apache\maven\repository\com\alibaba\fastjson\1.2.79\fastjson-1.2.79.jar;^
D:\develop\apache\maven\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;^
D:\develop\apache\maven\repository\commons-lang\commons-lang\2.6\commons-lang-2.6.jar;^
D:\develop\apache\maven\repository\com\fifesoft\rsyntaxtextarea\3.1.6\rsyntaxtextarea-3.1.6.jar;^
 ./target/jsonview-2.3.3.jar
```

### jlink生成定制版jre

```shell
jlink ^
--strip-debug --no-header-files --no-man-pages --strip-native-commands ^
--add-modules java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.xml,jdk.unsupported ^
--module-path ^
D:\develop\apache\maven\repository\org\netbeans\api\org-netbeans-swing-tabcontrol\RELEASE126\org-netbeans-swing-tabcontrol-RELEASE126.jar;^
D:\develop\apache\maven\repository\dom4j\dom4j\1.6.1\dom4j-1.6.1.jar;^
D:\develop\apache\maven\repository\org\projectlombok\lombok\1.18.22\lombok-1.18.22.jar;^
D:\develop\apache\maven\repository\com\alibaba\fastjson\1.2.79\fastjson-1.2.79.jar;^
D:\develop\apache\maven\repository\org\slf4j\slf4j-api\1.7.36\slf4j-api-1.7.36.jar;^
D:\develop\apache\maven\repository\commons-lang\commons-lang\2.6\commons-lang-2.6.jar;^
D:\develop\apache\maven\repository\com\fifesoft\rsyntaxtextarea\3.1.6\rsyntaxtextarea-3.1.6.jar;^
 ^
--output "./package/miniJre"
```

### jpackage生成msi
```shell
jpackage ^
--verbose ^
--runtime-image ./package/miniJre ^
--dest E:\code\workspace\java\JsonView\target\jpackage ^
--app-version 1.0.0 ^
--copyright "JsonView Copyright" ^
--description "Json view" ^
--name JsonView ^
--vendor JsonView ^
--icon E:\code\workspace\java\JsonView\resources\icon.ico ^
--module cy.jsonview.jsonview/cy.jsonview.jsonview.app.MainApp ^
--temp target/tmp ^
--install-dir JsonView/JsonView ^
--resource-dir E:\code\workspace\java\JsonView\src\main\resources ^
--win-dir-chooser ^
--win-shortcut ^
--module-path E:\code\workspace\java\JsonView\target\classes ^
--input ./dependency
```



