项目中子模块使用springboot时引入springboot依赖需要加入<relativePath>标签，如下所示：

```xml
<parent>
    <artifactId>spring-boot-parent</artifactId>
    <groupId>org.springframework.boot</groupId>
    <version>2.1.3.RELEASE</version>
    <relativePath/>
</parent>
```







springboot使用jsp

1. 需要引入依赖 springmvc 和 jsp解析器

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
   </dependency>
   
   <dependency>
       <groupId>org.apache.tomcat.embed</groupId>
       <artifactId>tomcat-embed-jasper</artifactId>
       <scope>provided</scope>
   </dependency>
   ```

   注意scope为 <scope>provided</scope>

2. build标签内配置resources

   ```xml
   <resources>
       <resource>
           <directory>src/main/webapp</directory>
           <targetPath>META-INF/resources</targetPath>
           <includes>
               <include>**/**</include>
           </includes>
       </resource>
   </resources>
   ```

   

3. 项目目录结构
 ![image-20220405163121521](resources\image-20220405163121521.png)

4. 配置文件中定义视图前缀和后缀

```yml
pring:
  mvc:
    view:
      prefix: /WEB-INF/view/
      suffix: .jsp
```



5. 配置视图url

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/login-view");
        registry.addViewController("/login-view").setViewName("login");
    }
}

```









无法获取datasource问题

现象：

启动报错：

org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'javax.sql.DataSource' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}



已引入依赖：

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```



配置文件：

```yml
spring:  
  datasource:
    hikari:
      username: root
      password: leixing
      jdbc-url: jdbc:mysql://localhost:3306/user_db?useUnicode=true
      driver-class-name: com.mysql.cj.jdbc.Driver
```



问题原因：

没有引入spring-boot-starter-jdbc启动器，添加依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

