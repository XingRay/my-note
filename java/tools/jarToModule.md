# 使用jlink将jar转化为module


```shell
jpackage --icon ./src/main/resources/images/launcher.ico --type app-image --dest ./output/package --name PackageDemo --app-version "1.0.0" --copyright "xingray.com" --description "java package demo" --vendor "xingray" --resource-dir ./s
rc/main/resources --module com.xingray.PackageDemo/com.xingray.packagedemo.app.Launcher --module-path ./target/classes;./output/dependency --add-modules java.base,java.desktop,java.instrument,java.logging,java.management,java.naming,java.scripting,jdk.compiler,jdk.jfr,jd
k.unsupported
jlink 失败，出现 错误: 自动模块不能用于来自 file:///E:/code/workspace/java/PackageDemo/./output/dependency/logback-core-1.2.11.jar 的 jlink: logback.core
```



java开发的桌面软件，使用jlink打包后，用户可以直接直接运行，不需要安装java环境，关键是体积也比jre小很多。这都得益于java9推出的模块化功能。我在使用中遇到不少坑，总算一一解决，在此记录一下，希望能帮助到遇到同样问题的朋友。



1.模块化开发时注意事项大部分第三方库都没有提供模块化版本，因此在module-info.java中添加这些包的依赖时，只能作为自动模块。模块名就是jar包名去掉版本号，横线用点代替，网上说法是不能包含数字，但是像commons-lang3-3.4.1.jar，经过实验必须写为 requires commons.lang3;才能识别，虽然IDE会给出错误提示“找不到模块”，但用maven编译时是没有问题的。另外，如果自己开发的模块，命名中有数字的话，编译也只是给出警告而已。



2.创建JMOD时注意事项指定的模块文件扩展名必须是jmod，不然后面使用jlink打包时，会提示找不到模块



3.运行jlink提示自动模块不能用于来自XXXX原因是项目依赖了自动模块，系统不知道自动模块依赖哪些其他模块。解决办法就是把没有模块化的这些jar包，一个个转成模块化的jar包。

1)创建module-info.java文件，这需要知道jar包依赖哪些模块，输出哪些模块，可以使用命令jdeps -s xxx.jar 查看依赖的模块，或者直接让它输出一个module-info.java例如：jdeps --generate-module-info . commons-codec-1.9.jar如果jar还依赖其他第三方jar，会导致生成module-info.java失败，这种只能自己创建，requires部分写入jdeps -s 查到的依赖模块，exports部分尽量把包都写上



2)把jar包中的class解压到一个目录，比如 bin，使用javac编译module-info.java，指定目标目录为jar解压的目录，例如：

javac -d bin src/module-info.java



3)使用jar命令，把编译后的module-info.class和解压的其他class一起，打包为一个模块化包，例如：jar -c -f my_module.jar -e com.Main --module-version 1.0 -C bin/ .



4)使用jlink打包，--strip-debug 不打包调式信息 --compress=2 开启2级别压缩，这两项减小生成的文件体积jlink --module-path xxx;. --add-modules mymodule --output target --launcher Hello=com.Main --strip-debug --compress=2





```shell
jdeps --generate-module-info . dependency/rsyntaxtextarea-3.1.6.jar

# 查看依赖
jdeps -s .\dependency\fastjson-1.2.79.jar 

# 生成module-info文件，保存在.\modules\moduleInfo 目录
jdeps --ignore-missing-deps --generate-module-info .\modules\moduleInfo .\dependency\fastjson-1.2.79.jar

# 解压 fastjson-1.2.79.jar
unzip .\dependency\fastjson-1.2.79.jar


javac -d .\modules\rsyntaxtextarea-3.1.6 .\rsyntaxtextarea\module-info.java 

jar --create --file ./modules/rsyntaxtextarea-3.1.6.jar --module-version 3.1.6 -C .\modules\rsyntaxtextarea-3.1.6 .

jar --create --file .\modules\fastjson-1.2.79.jar --module-version 1.2.79 -C .\modules\fastjson-1.2.79\ .


```



javac -d .\modules\fastjson-1.2.79 .\modules\moduleInfo\fastjson\module-info.java 
.\modules\moduleInfo\fastjson\module-info.java:26: 错误: 程序包javax.ws.rs.ext不存在
    provides javax.ws.rs.ext.MessageBodyReader with
                            ^
.\modules\moduleInfo\fastjson\module-info.java:28: 错误: 程序包javax.ws.rs.ext不存在
    provides javax.ws.rs.ext.MessageBodyWriter with
                            ^
.\modules\moduleInfo\fastjson\module-info.java:30: 错误: 程序包javax.ws.rs.ext不存在
    provides javax.ws.rs.ext.Providers with
                            ^
.\modules\moduleInfo\fastjson\module-info.java:32: 错误: 程序包org.glassfish.jersey.internal.spi不存在
    provides org.glassfish.jersey.internal.spi.AutoDiscoverable with



在生成的.\modules\moduleInfo\fastjson\module-info.java 中删除部分provides语句

```java
module fastjson {
    requires java.sql;

    requires transitive java.desktop;
    requires transitive java.xml;

    exports com.alibaba.fastjson;
    exports com.alibaba.fastjson.annotation;
    exports com.alibaba.fastjson.asm;
    exports com.alibaba.fastjson.parser;
    exports com.alibaba.fastjson.parser.deserializer;
    exports com.alibaba.fastjson.serializer;
    exports com.alibaba.fastjson.spi;
    exports com.alibaba.fastjson.support.config;
    exports com.alibaba.fastjson.support.geo;
    exports com.alibaba.fastjson.support.hsf;
    exports com.alibaba.fastjson.support.jaxrs;
    exports com.alibaba.fastjson.support.moneta;
    exports com.alibaba.fastjson.support.retrofit;
    exports com.alibaba.fastjson.support.spring;
    exports com.alibaba.fastjson.support.spring.annotation;
    exports com.alibaba.fastjson.support.spring.messaging;
    exports com.alibaba.fastjson.support.springfox;
    exports com.alibaba.fastjson.util;

//    provides javax.ws.rs.ext.MessageBodyReader with
//        com.alibaba.fastjson.support.jaxrs.FastJsonProvider;
//    provides javax.ws.rs.ext.MessageBodyWriter with
//        com.alibaba.fastjson.support.jaxrs.FastJsonProvider;
//    provides javax.ws.rs.ext.Providers with
//        com.alibaba.fastjson.support.jaxrs.FastJsonProvider;
//    provides org.glassfish.jersey.internal.spi.AutoDiscoverable with
//        com.alibaba.fastjson.support.jaxrs.FastJsonAutoDiscoverable;

}
```





