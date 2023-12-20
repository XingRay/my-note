## swagger文档方案



springcloud+springboot+springcloud-gateway+knife4j

框架 								版本

springboot 					3.0.2

springcloud					2022.0.0

springcloud-alibaba 	2022.0.0.0-RC1

knife4j							4.0.0



主项目pom：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.0.2</version>
    </parent>

    <groupId>com.xingray</groupId>
    <artifactId>demo</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <modules>
        <module>demo-gateway</module>
        <module>demo-user</module>
    </modules>

    <properties>
       	...
        <springcloud.alibaba.version>2022.0.0.0-RC1</springcloud.alibaba.version>
        <springcloud.version>2022.0.0</springcloud.version>
    </properties>

    <dependencies>
    	...
    </dependencies>

    <dependencyManagement>
        <dependencies>
			...
			
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${springcloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>com.alibaba.cloud</groupId>
                <artifactId>spring-cloud-alibaba-dependencies</artifactId>
                <version>${springcloud.alibaba.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>com.github.xiaoymin</groupId>
                <artifactId>knife4j-dependencies</artifactId>
                <version>${knife4j.version</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

</project>
```



gateway:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.xingray</groupId>
        <artifactId>demo</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>demo-gateway</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
        <dependency>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
            <version>2.6</version>
        </dependency>

        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-gateway-spring-boot-starter</artifactId>
            <version>${knife4j.version}</version>
        </dependency>

    </dependencies>

</project>
```

application.yml

```yaml
server:
  port: 30000

spring:
  application:
    name: demo-gateway

  cloud:
    nacos:
      discovery:
        service: ${spring.application.name}
        username: nacos
        password: nacos
        server-addr: 127.0.0.1:8848

    gateway:
      routes:
        - id: route-user
          uri: lb://demo-user
          predicates:
            - Path=/user/**
          filters:
          	# 路由转发时，将请求路径中的/user截取，ip:port/user/** => lb://demo-user/**
            - StripPrefix=1

knife4j:
  # 聚合swagger文档
  gateway:
    enable: true
    routes:
      - name: '用户服务'
        url: '/user/v3/api-docs?group=用户服务'
        service-name: quant-platform-user
        order: 1
```



user服务：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.xingray</groupId>
        <artifactId>quant-platform</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>demo-user</artifactId>

    <dependencies>
        ...
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>

        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
        </dependency>

    </dependencies>
</project>
```

application.yml:

```yaml
server:
  port: 30100
  
spring:
  application:
    name: quant-platform-user
    
  cloud:
    nacos:
      discovery:
        service: ${spring.application.name}
        username: nacos
        password: nacos
        server-addr: 127.0.0.1:8848

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  api-docs:
    path: /v3/api-docs
  group-configs:
    - group: '用户服务'
      paths-to-match: '/**'
      packages-to-scan: com.xingray.quantplatfrom.user

knife4j:
  enable: true
  setting:
    language: zh_cn
    swagger-model-name: '实体类列表'

  documents:
    - name: 项目文档
      locations: classpath:project-document/*
      group: '用户服务'

    - name: 部署文档
      locations: classpath:deploy-document/*
      group: '用户服务'
```



Swagger配置类：

```java
@Configuration
public class SwaggerConfig {
    /**
     * 根据@Tag 上的排序，写入x-order
     *
     * @return the global open api customizer
     */
    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            Random random = new Random();
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x-order", random.nextInt(0, 100));
                    tag.setExtensions(map);
                });
            }
            if (openApi.getPaths() != null) {
                openApi.addExtension("x-test123", "333");
                openApi.getPaths().addExtension("x-abb", random.nextInt(1, 100));
            }
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("demo用户系统API")
                        .version("1.0")
                        .description("demo用户系统API文档")
                        .termsOfService("http://doc.xingray.com")
                        .license(new License().name("Apache 2.0").url("http://doc.xingray.com")));
    }
}
```

其中在resources目录下可以添加说明文档，格式为md文件，说明文档目录示例如下：

resources

​	deploy-document

​		deploy_cn,md

​		deploy_en,md

​	project-document

​		project-introduction_cn.md

​		project-introduction_en.md



