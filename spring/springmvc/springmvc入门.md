## SpringMvc入门



### 1. SpringMvc项目搭建

#### 1.1 环境准备

java： openjdk 19

tomcat: tomcat 10.0.27



#### 1.2 创建maven工程

通过idea创建maven工程



#### 1.3 引入依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.xingray</groupId>
    <artifactId>springmvc-demo</artifactId>
    <version>1.0.0</version>
    <packaging>war</packaging>

    <properties>
        <maven.compiler.source>19</maven.compiler.source>
        <maven.compiler.target>19</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>6.0.4</version>
        </dependency>

        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.tomcat</groupId>
            <artifactId>tomcat-servlet-api</artifactId>
            <version>10.0.27</version>
        </dependency>

    </dependencies>

    <build>
        <finalName>springmvc-demo</finalName>

        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <target>${maven.compiler.target}</target>
                    <source>${maven.compiler.source}</source>
                </configuration>
            </plugin>
        </plugins>

    </build>

</project>
```

注意需要引入 `tomcat-servlet-api`依赖，版本与运行的tomcat版本一致即可。



#### 1.4 项目结构

项目结构如下，如有缺失，需要自行补全：

```bash
D:.
├─main
│  ├─java
│  │  └─com
│  │      └─xingray
│  │          └─springmvc
│  │              └─demo
│  │                  ├─config
│  │                  │      ApplicationConfig.java
│  │                  │      WebConfig.java
│  │                  │
│  │                  ├─controller
│  │                  └─init
│  │                          SpringApplicationInitializer.java
│  │
│  ├─resources
│  └─webapp
│      └─WEB-INF
│          └─view
│                  hello.jsp
│
└─test
    └─java

```

其中：

SpringApplicationInitializer：

```java
package com.xingray.springmvc.demo.init;

import com.xingray.springmvc.demo.session.config.ApplicationConfig;
import com.xingray.springmvc.demo.config.WebConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class SpringApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{ApplicationConfig.class};
    }
    
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}
```

设置应用配置类和 webmvc 配置类，及 url-mapping



ApplicationConfig:

```java
package com.xingray.springmvc.demo.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;

@Configuration
@ComponentScan(basePackages = "com.xingray.springmvc.demo",
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class)}
)
public class ApplicationConfig {

}
```

作为应用配置类。



WebConfig：

```java
package com.xingray.springmvc.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.xingray.springmvc.demo",
        includeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class)}
)
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/view/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("hello");
    }
}
```

作为springmvc的配置类。



#### 1.5 启动项目

在idea启动工具条中点击 `Edit Configurations` ，再弹出的 `Run/Debug Configurations` 窗口左边点击 `+` 号，在弹出的 `Add New Configuration`中选择`TomcatServer`下的`Local`，再弹窗中的Server页设置 `Application  Server`选择本地的Tomcat的路径，在Deployment中的`Deploy at the server startup`中点击`+` 添加 `Artifact`选择 `springmvc-demo:war exploded` 即可。确认关闭窗口后即可点击运行按钮启动tomcat服务器。

