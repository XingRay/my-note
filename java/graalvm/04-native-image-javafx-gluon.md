## Windows平台基于Gluon的JavaFx项目的native-image制作

### 1. 关于Gluon

Gluon

首页：[Home](https://gluonhq.com/)
文档：[Doc](https://docs.gluonhq.com/)
项目生成：[Start](https://start.gluon.io/)
Gluon-GraalVM发行版：[Release](https://github.com/gluonhq/graal/releases)

### 2.环境
os：windows10
java: jdk19

### 2.1 vs

需要先下载安装vs，可以使用 visual studio community 2022， 配置相关环境变量，参考 ./01-graalvm-入门.md



### 2.2 Gluon-GraalVM

下载Gluon-GraalVM发行版：[Release](https://github.com/gluonhq/graal/releases) ，本文使用 [graalvm-svm-java17-windows-gluon-22.1.0.1-Final.zip](https://github.com/gluonhq/graal/releases/download/gluon-22.1.0.1-Final/graalvm-svm-java17-windows-gluon-22.1.0.1-Final.zip)

基于windows平台，java版本为17。下载保存到本地目录，解压至指定路径，如：

```bash
D:\develop\java\jdk\gluon\17\graalvm-svm-java17-windows-gluon-22.1.0.1-Final
```

### 2.3 生成项目

在Gluon提供的项目骨架生成页[Gluon Start](https://start.gluon.io/)填写信息生成项目。下面是简单示例，可以根据需要修改：

#### Application Details
Name: gluon-javafx-native-image-demo

Group: com.mycompany

Artifact: gluon-javafx-native-image-demo

#### JavaFX 

Version: 19[Lastest]

Modules: base,graphics, controls

#### Gluon Features

全部取消，只做简单的demo不需要GluonFeatures

点击 Generate Project 下载项目，会得到一个名为 `gluon-project.zip` 的压缩文件，解压项目到指定目录，如：

```bash
D:\code\demo\gluon\gluon-javafx-native-image-demo
```

项目结构如下：

```bash
:.
│   mvnw
│   mvnw.cmd
│   pom.xml
│   README.md
│
├───.mvn
│   └───wrapper
│           maven-wrapper.jar
│           maven-wrapper.properties
│
└───src
    └───main
        ├───java
        │   └───com
        │       └───mycompany
        │           └───sample
        │                   Main.java
        │
        └───resources
            └───com
                └───mycompany
                    └───sample
                            openduke.png
                            styles.css
```

- 提示：创建此项目后，后续可以直接通过idea创建一个空的maven项目，然后将 .mvn 文件夹，mvnw.cmd mvnw 复制到新的项目的根目录，然后在 pom.xml 中引入 gluonfx-maven-plugin 插件即可生成native-image



项目的pom文件如下：

```xml
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.mycompany</groupId>
    <artifactId>gluon-javafx-native-image-demo</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>gluon-javafx-native-image-demo</name>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.release>11</maven.compiler.release>
        <javafx.version>19</javafx.version>
        <javafx.plugin.version>0.0.8</javafx.plugin.version>
        <gluonfx.plugin.version>1.0.16</gluonfx.plugin.version>
        <main.class>com.mycompany.sample.Main</main.class>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-base</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-graphics</artifactId>
            <version>${javafx.version}</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>${javafx.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
            </plugin>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <version>${javafx.plugin.version}</version>
                <configuration>
                    <mainClass>${main.class}</mainClass>
                </configuration>
            </plugin>
            <plugin>
                <groupId>com.gluonhq</groupId>
                <artifactId>gluonfx-maven-plugin</artifactId>
                <version>${gluonfx.plugin.version}</version>
                <configuration>
                    <target>${gluonfx.target}</target>
                    <mainClass>${main.class}</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <id>android</id>
            <properties>
                <gluonfx.target>android</gluonfx.target>
            </properties>
        </profile>
        <profile>
            <id>ios</id>
            <properties>
                <gluonfx.target>ios</gluonfx.target>
            </properties>
        </profile>
    </profiles>
</project>
```



### 2.4 构建项目

运行指令：

```bash
mvnw.cmd clean gluonfx:build
```

开始下载相关依赖，并构建项目。可以从[这里](https://pan.baidu.com/s/1aYcAwITJvSE6P6nzNFXQkA?pwd=0000)直接下载依赖，放在C:\Users\ [user-name] 目录。下载完成后的文件结构

C:\Users\\[user-name]\\.gluon

```bash
│   gluonmobile.log
│
└───substrate
    │   openjfx-17-windows-x86_64-static.zip
    │   openjfx-20-ea+7-windows-x86_64-static.zip
    │
    └───javafxStaticSdk
        ├───17
        ...
```

C:\Users\\[user-name]\\.m2

```

C:\Users\leixing\.m2>tree /F
Folder PATH listing
Volume serial number is 7883-D102
C:.
├───repository
│   ├───aopalliance
│   │   └───aopalliance
│   │       └───1.0
│   │               aopalliance-1.0.jar
│   │               aopalliance-1.0.jar.sha1
│   │               aopalliance-1.0.pom
|	|	...
│
└───wrapper
    └───dists
        └───apache-maven-3.8.1-bin
            └───2l5mhf2pq2clrde7f7qp1rdt5m
                │   apache-maven-3.8.1-bin.zip
                │
                └───apache-maven-3.8.1
                    │   LICENSE
                    ...
```

项目构建过程中会报错：

```bash
[周五 3月 17 21:49:58 CST 2023][严重] Process link failed with result: 1120
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  02:33 min
[INFO] Finished at: 2023-03-17T21:49:58+08:00
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal com.gluonhq:gluonfx-maven-plugin:1.0.16:link (default-cli) on project gluon-javafx-native-image-demo: Linking failed -> [Help 1]
```

原因是没有使用Gluon提供的GraalVM发行版导致，可以设置系统环境变量 GRAALVM_HOME为

```bash
D:\develop\java\jdk\gluon\17\graalvm-svm-java17-windows-gluon-22.1.0.1-Final
```

或者在pom.xml文件中设置插件参数：

```xml
<plugin>
    <groupId>com.gluonhq</groupId>
    <artifactId>gluonfx-maven-plugin</artifactId>
    <version>${gluonfx.plugin.version}</version>
    <configuration>
        <graalvmHome>D:\develop\java\jdk\gluon\17\graalvm-svm-java17-windows-gluon-22.1.0.1-Final</graalvmHome>
    </configuration>
</plugin>
```

再重新执行指令：

```bash
mvnw.cmd clean gluonfx:build
```

就可以编译成功了，生成的exe文件位于 

```cmd
[ProjectRoot]\target\gluonfx\x86_64-windows\gluon-javafx-native-image-demo.exe
```

双击运行 exe，即可看到显示图片的窗口。将exe文件单独复制到任何其他目录也可以直接运行。



将sample包下的Main.java删除，复制下列两个类：

MainApplication.java:

```java
package com.mycompany.sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private int num = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane root = new AnchorPane();
        Label text = new Label();
        AnchorPane.setLeftAnchor(text, 100.0);
        AnchorPane.setTopAnchor(text, 100.0);

        Button button = new Button("click me");

        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                num++;
                text.setText("click " + num);
                System.out.println("click " + num);
            }
        });
        root.getChildren().addAll(button, text);
        Scene scene = new Scene(root, 240.0, 220.0);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

```

Launcher.java

```java
package com.mycompany.sample;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("Launcher#main: " + String.join(" ", args));
        Application.launch(MainApplication.class, args);
    }
}
```

并且在pom文件中修改mainClass配置

```xml
<properties>
    ...
    <main.class>com.mycompany.sample.Launcher</main.class>
</properties>
```

直接在idea中运行launcher是可以正常运行的，下面编译成 native-image

```bash
mvnw.cmd clean gluonfx:build
```

编译成功，exe文件生正常生成，但是双击exe文件会发现没有任何反应，使用console启动如下：

```bash
D:\code\demo\gluon\gluon-javafx-native-image-demo>.\target\gluonfx\x86_64-windows\gluon-javafx-native-image-demo.exe

D:\code\demo\gluon\gluon-javafx-native-image-demo>
```

没有任何提示就退出了。这时可以通过命令启动 

```bash
mvnw.cmd gluonfx:nativerun
```

启动，这时可以看到运行的报错信息：

```bash
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] Launcher#main: 
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] 3�� 17, 2023 10:09:30 ���� com.sun.javafx.application.PlatformImpl startup
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] ����: Unsupported JavaFX configuration: classes were loaded from 'unnamed module @58ceff1'
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] Exception in Application constructor
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] Exception in thread "main" java.lang.RuntimeException: Unable to construct Application instance: class com.mycompany.sample.MainApplication
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] 	at com.sun.javafx.application.LauncherImpl.launchApplication1(LauncherImpl.java:891)
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] 	at com.sun.javafx.application.LauncherImpl.lambda$launchApplication$2(LauncherImpl.java:196)
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] 	at java.lang.Thread.run(Thread.java:833)
[周五 3月 17 22:09:30 CST 2023][信息] [SUB] 	at com.oracle.svm.core.thread.PlatformThreads.threadStartRoutine(PlatformThreads.java:704)
```

是因为这里

```java
Application.launch(MainApplication.class, args);
```

反射构造了 MainApplication，所以要在插件中配置：

```xml
<plugin>
    <groupId>com.gluonhq</groupId>
    <artifactId>gluonfx-maven-plugin</artifactId>
    <version>${gluonfx.plugin.version}</version>
    <configuration>
        ...
        <reflectionList>
			<list>com.mycompany.sample.MainApplication</list>
        </reflectionList>
    </configuration>
</plugin>
```

其他需要反射访问的类也需要同在加载列表中。再次编译可以正常生成exe文件。

demo[下载地址](https://pan.baidu.com/s/1zg3Dr7doAGZ01plqBpC1Mg?pwd=0000) 
