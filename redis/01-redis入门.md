## Redis入门

https://redis.io/

https://redis.io/docs/getting-started/



### 1.下载安装

官方版本

https://redis.io/docs/getting-started/installation/install-redis-on-linux/

https://hub.docker.com/_/redis



windows版非官方编译版

https://github.com/zkteco-home/redis-windows/releases

https://github.com/zkteco-home/redis-windows/releases/download/7.0.11/redis-7.0.11-windows.zip

1.下载安装包

2.解压到指定目录

3.卸载服务(可选)

升级版本时要先卸载旧版本的服务，在旧版本的目录中执行

```bash
redis-server --service-uninstall
```

4.安装服务

以管理员身份运行

```bash
install_redis.cmd
```

5.配置

主要修改一下三条，用于开发测试

```bash
# 设置密码
requirepass 123456 

#注释掉，开发访问
#bind 127.0.0.1

# 关闭保护模式，用于开发测试
protected-mode no
```



### 2.安装客户端

https://github.com/qishibo/AnotherRedisDesktopManager/releases



### 3.SpringBoot项目中整合Redis



#### 3.1 引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

完整的pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.0.5</version>
    </parent>

    <groupId>com.xingray</groupId>
    <artifactId>redis-demo</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>19</maven.compiler.source>
        <maven.compiler.target>19</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.0</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
    </dependencies>

</project>
```



#### 3.2 配置

```yaml
server:
  port: 8080

spring:
  application:
    name: redis-demo
  data:
    redis:
      host: 192.168.0.108
      port: 6379
      password: 123456
```



#### 3.3 配置类

```java
@Configuration
public class RedisConfig {
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //参照StringRedisTemplate内部实现指定序列化器
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setHashKeySerializer(keySerializer());
        redisTemplate.setValueSerializer(valueSerializer());
        redisTemplate.setHashValueSerializer(valueSerializer());
        return redisTemplate;
    }

    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    //使用Jackson序列化器
    private RedisSerializer<Object> valueSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
```



#### 3.4 测试

```java
package com.xingray.redis.demo.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Objects;
import java.util.UUID;

@SpringBootTest
public class RedisTest {


    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisTest(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Test
    public void saveTest() {
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        String saveValue = "redis" + UUID.randomUUID();
        System.out.println("saveValue:" + saveValue);
        operations.set("hello", saveValue);
        String loadValue = operations.get("hello");
        System.out.println("loadValue:" + loadValue);
        assert Objects.equals(loadValue, saveValue);
    }
}
```

输出：

```bash
saveValue:redis26164cee-823d-45d6-96a6-368a16409cb4
loadValue:redis26164cee-823d-45d6-96a6-368a16409cb4
```

