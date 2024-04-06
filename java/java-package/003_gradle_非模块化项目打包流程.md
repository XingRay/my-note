将依赖包复制到 build/jars

```
task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("${layout.buildDirectory.asFile.get().absolutePath}/jars")
}
```



通过jdeps查询依赖

```
jdeps --multi-release 22 --print-module-deps --ignore-missing-deps --recursive  --add-modules ALL-MODULE-PATH --class-path app/build/classes/java --class-path app/build/classes/kotlin --class-path app/build/jars --module-path app/build/jars
```

注意要将 native 包移出到单独目录

输出:

```
java.base,java.compiler,java.scripting,java.sql,jdk.jfr,jdk.unsupported,jdk.unsupported.desktop
```



通过插件打成flatJar

```
jdeps --multi-release 22 --print-module-deps --ignore-missing-deps --recursive --add-modules ALL-MODULE-PATH  app/build/libs/app-all.jar
```

输出

```
java.base,java.compiler,java.scripting,java.sql,jdk.jfr,jdk.unsupported,jdk.unsupported.desktop
```



```shell
# 如果需要安装包则执行下列命令：
jpackage ^
--icon app/src/main/resources/icon.ico ^
--win-dir-chooser ^
--win-shortcut ^
--install-dir xingray/javafx-opengl ^
--dest app/build/jpackage ^
--name javafx-opengl ^
--app-version "1.0.0" ^
--copyright "xingray.com" ^
--description "demo for jpackage" ^
--vendor "xingray" ^
--resource-dir app/src/main/resources ^
--input app/build/libs ^
--input app/build/nativeJar ^
--main-jar app/build/libs/app-all.jar ^
--main-class org.example.App ^
--add-modules java.base,java.compiler,java.scripting,java.sql,jdk.jfr,jdk.unsupported,jdk.unsupported.desktop ^
```



如果需要安装包则执行下列命令：

```
jpackage --icon app/src/main/resources/icon.ico --win-dir-chooser --win-shortcut --install-dir xingray/javafx-opengl --dest app/build/jpackage --name javafx-opengl --app-version "1.0.0" --copyright "xingray.com" --description "demo for jpackage" --vendor "xingray" --resource-dir app/src/main/resources --input app/build/libs --input app/build/nativeJar --main-jar app/build/libs/app-all.jar --main-class org.example.App --add-modules java.base,java.compiler,java.scripting,java.sql,jdk.jfr,jdk.unsupported,jdk.unsupported.desktop
```

