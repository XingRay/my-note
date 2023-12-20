## nacos配置管理

### 1. 创建配置
nacos控制台-配置管理-配置列表，点击右侧+号进行创建。
Data-ID：配置文件的名称，需要与服务对应。一般用${service-name}-${profile}.yaml。如userservice-dev.yaml或userservice-dev.properties。注意后缀名使用yaml而不是yml。
Group：一般不用修改。
描述：简单介绍即可。
配置内容：需要通过nacos进行热更新的配置。如数据库配置一般不会改动，不需要热更新。业务开关可能经常变动，需要经常修改，需要在线更改配置。
如：

Data-ID: user-dev.yaml

配置内容:

```yaml
pattern:
  dateformat: yyyy-MM-dd HH:mm:ss
```
点击发布即创建完成。

### 2.配置读取流程
没有nacos配置时：
~~~mermaid
graph LR
项目启动 --> 读取本地配置文件(application.yml) --> 创建spring容器 --> 加载bean
~~~

存在nacos配置时：

~~~mermaid
graph LR
项目启动 --> 读取nacos配置 --> 读取本地配置文件(application.yml) --> 创建spring容器 --> 加载bean
~~~

由于nacos的配置存在于application.yml中，所以需要在项目启动时优先加载nacos相关配置。spring提供了bootstrap.yml配置，优先级要高于application.yml
引入bootstrap.yml配置：

~~~mermaid
graph LR
项目启动 --> 读取bootstrap.yml --> 读取nacos配置 --> 读取本地配置文件(application.yml) --> 创建spring容器 --> 加载bean
~~~



#### 2.1 引入依赖

引入nacos配置管理客户端依赖
```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```



#### 2.2 创建引导文件

在项目的resource目录中创建bootstrap.yml文件

```yaml
spring:
  application:
    name: user

  profiles:
    active: dev
  cloud:
    nacos:
      discovery:
        service: ${spring.application.name}
        username: nacos
        password: nacos
        server-addr: 127.0.0.1:8848
      config:
        file-extension: yaml

```

在application.yml中将重复的配置删除。项目启动后就会自动从nacos中加载${spring.application.name}-${spring.profiles.active}.${spring.cloud.nacos.config.file-extension}配置文件，即user-dev.yaml



#### 2.3 读取配置信息

##### 2.3.1 属性注入

在项目中需要使用配置的地方添加属性和@Value注解，如：

```java
@RestController
@RequestMapping("/user")
public class UserController {

    @Value("${pattern.dateformat}")
    private String dateFormat;

    @GetMapping("/dateTime")
    public Result<String> getDateTime() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFormat));
        return Result.success(dateTime);
    }
}
```



##### 2.3.2 配置类

引入依赖：

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-configuration-processor</artifactId>
	<optional>true</optional>
</dependency>
```

创建配置类：

```java
@Component
@ConfigurationProperties("pattern")
@Data
public class PatternConfig {
    private String dateformat;
}
```

在需要的地方注入配置类即可，如：

```java
@RestController
@Slf4j
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private PatternConfig patternConfig;

    @GetMapping("/timeformat")
    public Result<String> timeFormat() {
        return Result.success(patternConfig.getDateformat());
    }
}
```



### 3. 配置的热更新

#### 3.1 添加注解

在配置属性所在类上添加@RefreshScope注解，如：

```java
@RestController
@RequestMapping("/user")
@RefreshScope
public class UserController {

    @Value("${pattern.dateformat}")
    private String dateFormat;

    @GetMapping("/dateTime")
    public Result<String> getDateTime() {
        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(dateFormat));
        return Result.success(dateTime);
    }
}
```

使用配置类的方式则代码不需要更改。

#### 3.2 更新配置

在nacos控制台-配置管理-配置列表 点击配置项的操作-编辑进入配置编辑页，修改配置如下：

```yaml
pattern:
  dateformat: yyyy年MM月dd日 HH:mm:ss
```

点击发布-确认发布即可。在重新调用接口即可看到新的配置已经生效。



### 4. 多环境共享配置

服务启动时，会从nacos中拉取 ${spring.application.name}-${spring.profiles.active}.${spring.cloud.nacos.config.file-extension} 和 ${spring.application.name}.${spring.cloud.nacos.config.file-extension}，如：user-dev.yaml和user.yaml，因此可以把与环境无关的配置项放到${spring.application.name}.${spring.cloud.nacos.config.file-extension}配置文件中。

#### 4.1 创建多环境共享配置文件

在nacos控制台，nacos控制台-配置管理-配置列表，点击右侧+号进行创建。Data-ID为 user.yaml

配置内容:

```yaml
pattern:
  shareValue: share
```

点击发布即创建完成。

为了便于测试，需要再创建测试环境配置：Data-ID为 user-test.yaml

配置内容：

```yaml
pattern:
  dateformat: yyyy年MM月dd日 HH:mm:ss
```

将开发环境的配置修改为

```yaml
pattern:
  dateformat: yyyy/MM/dd HH:mm:ss
```



#### 4.2 读取配置

使用方式同环境配置一致，如：

配置类

```java
@Component
@ConfigurationProperties("pattern")
@Data
public class PatternConfig {
    // 每个环境单独的配置
    private String dateformat;
    
    //多环境共享属性
    private String shareValue;
}
```



```java
@RestController
@Slf4j
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private PatternConfig patternConfig;

    @GetMapping("/pattern")
    public Result<PatternConfig> pattern() {
        return Result.success(patternConfig);
    }
}
```



#### 4.3 更改环境

##### 4.3.1 更改bootstrap.yml文件

在bootstrap.yml中可以更改环境

```yaml
spring:
  profiles:
    active: dev
```

更改为:

```yaml
spring:
  profiles:
    active: test
```

重启服务即可



##### 4.3.2 通过IDEA修改启动参数

Run/Debug Configuations -> active profiles  填写环境名即可。可以在IDEA中复制启动配置，再进行修改，注意测试环境的端口也需要与开发环境不同，可通过添加vm参数 -Dserver.port=30103 进行修改，再同时启动dev和test环境的服务即可进行接口的测试。



#### 4.4 优先级

当本地的配置文件(applicaition.yml)，nacos中的 user.yaml 和  user-dev.yaml 配置文件中都存在相同的配置时，优先级是

user-dev.yaml (nacos) > user.yaml (nacos) > applicaiton.yml (本地文件)

环境配置>共享配置>本地配置

