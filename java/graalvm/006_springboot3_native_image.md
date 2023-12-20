# SpringBoot3 native-image 

当前springboot最新版本为 3.11

创建一个项目, 注意引入2个插件

pom : 

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.1</version>
    </parent>

    <groupId>com.xingray</groupId>
    <artifactId>springboot3-native-image</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>20</maven.compiler.source>
        <maven.compiler.target>20</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.graalvm.buildtools</groupId>
                <artifactId>native-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```



NativeImageApplication.java : 

```java
package com.xingray.nativeimage;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NativeImageApplication {
    public static void main(String[] args) {
        SpringApplication.run(NativeImageApplication.class, args);
    }
}
```



TestController.java : 

```bash
package com.xingray.nativeimage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/test")
@RestController
public class TestController {

    @GetMapping("/hello")
    public String hello(){
        return "hello Native Image ~";
    }
}
```



运行 : 

```bash
mvn -Pnative clean compile spring-boot:process-aot native:compile-no-fork
```

即可生成本地可执行文件



制作镜像

Dockerfile

```bash
FROM ubuntu:22.04
WORKDIR /app
COPY ./target/springboot3-native-image /app/springboot3-native-image
ENTRYPOINT ["./springboot3-native-image"]
```

注意:编译的时候使用的是ubuntu22.04 ,所以镜像构建也使用ubuntu22.04作为基础镜像, 如果使用其他镜像,比如 alpine ,那么还需要安装运行的依赖, 静态库so文件等



创建镜像

```bash
docker build -t springboot3-native-image:1.0.0 -f Dockerfile .
```

```bash
docker run -p 8081:81 springboot3-native-image:1.0.0
```

输出

```bash

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.1.1)

2023-07-14T22:27:36.451Z  INFO 1 --- [           main] c.x.nativeimage.NativeImageApplication   : Starting AOT-processed NativeImageApplication using Java 20.0.1 with PID 1 (/app/springboot3-native-image started by root in /app)
2023-07-14T22:27:36.451Z  INFO 1 --- [           main] c.x.nativeimage.NativeImageApplication   : No active profile set, falling back to 1 default profile: "default"
2023-07-14T22:27:36.485Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 81 (http)
2023-07-14T22:27:36.487Z  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2023-07-14T22:27:36.487Z  INFO 1 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.10]
2023-07-14T22:27:36.504Z  INFO 1 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2023-07-14T22:27:36.504Z  INFO 1 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 53 ms
2023-07-14T22:27:36.561Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 81 (http) with context path ''
2023-07-14T22:27:36.562Z  INFO 1 --- [           main] c.x.nativeimage.NativeImageApplication   : Started NativeImageApplication in 0.144 seconds (process running for 0.155)
2023-07-14T22:28:15.914Z  INFO 1 --- [p-nio-81-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2023-07-14T22:28:15.915Z  INFO 1 --- [p-nio-81-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2023-07-14T22:28:15.915Z  INFO 1 --- [p-nio-81-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
```







### 2.2. Building a Native Image Using Buildpacks

Spring Boot includes buildpack support for native images directly for both Maven and Gradle. This means you can just type a single command and quickly get a sensible image into your locally running Docker daemon. The resulting image doesn’t contain a JVM, instead the native image is compiled statically. This leads to smaller images.



The builder used for the images is `paketobuildpacks/builder:tiny`. It has small footprint and reduced attack surface, but you can also use `paketobuildpacks/builder:base` or `paketobuildpacks/builder:full` to have more tools available in the image if required.



#### 2.2.1. System Requirements

Docker should be installed. See [Get Docker](https://docs.docker.com/installation/#installation) for more details. [Configure it to allow non-root user](https://docs.docker.com/engine/install/linux-postinstall/#manage-docker-as-a-non-root-user) if you are on Linux.

You can run `docker run hello-world` (without `sudo`) to check the Docker daemon is reachable as expected. Check the [Maven](https://docs.spring.io/spring-boot/docs/3.1.1/maven-plugin/reference/htmlsingle//#build-image-docker-daemon) or [Gradle](https://docs.spring.io/spring-boot/docs/3.1.1/gradle-plugin/reference/htmlsingle//#build-image-docker-daemon) Spring Boot plugin documentation for more details.

On macOS, it is recommended to increase the memory allocated to Docker to at least `8GB`, and potentially add more CPUs as well. See this [Stack Overflow answer](https://stackoverflow.com/questions/44533319/how-to-assign-more-memory-to-docker-container/44533437#44533437) for more details. On Microsoft Windows, make sure to enable the [Docker WSL 2 backend](https://docs.docker.com/docker-for-windows/wsl/) for better performance.



#### 2.2.2. Using Maven

To build a native image container using Maven you should ensure that your `pom.xml` file uses the `spring-boot-starter-parent` and the `org.graalvm.buildtools:native-maven-plugin`. You should have a `<parent>` section that looks like this:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.1</version>
</parent>
```

You additionally should have this in the `<build> <plugins>` section:

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
</plugin>
```

The `spring-boot-starter-parent` declares a `native` profile that configures the executions that need to run in order to create a native image. You can activate profiles using the `-P` flag on the command line.



If you don’t want to use `spring-boot-starter-parent` you’ll need to configure executions for the `process-aot` goal from Spring Boot’s plugin and the `add-reachability-metadata` goal from the Native Build Tools plugin.



To build the image, you can run the `spring-boot:build-image` goal with the `native` profile active:

```shell
$ mvn -Pnative spring-boot:build-image
```



#### 2.2.3. Using Gradle

The Spring Boot Gradle plugin automatically configures AOT tasks when the GraalVM Native Image plugin is applied. You should check that your Gradle build contains a `plugins` block that includes `org.graalvm.buildtools.native`.

As long as the `org.graalvm.buildtools.native` plugin is applied, the `bootBuildImage` task will generate a native image rather than a JVM one. You can run the task using:

```shell
$ gradle bootBuildImage
```

#### 2.2.4. Running the example

Once you have run the appropriate build command, a Docker image should be available. You can start your application using `docker run`:

```shell
$ docker run --rm -p 8080:8080 docker.io/library/myproject:0.0.1-SNAPSHOT
```

You should see output similar to the following:

```shell
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v3.1.1)
....... . . .
....... . . . (log output here)
....... . . .
........ Started MyApplication in 0.08 seconds (process running for 0.095)
```



The startup time differs from machine to machine, but it should be much faster than a Spring Boot application running on a JVM.



If you open a web browser to `localhost:8080`, you should see the following output:

```
Hello World!
```

To gracefully exit the application, press `ctrl-c`.

