## Kryo序列化介绍以及在SpringbootRedis中的使用



1、Kryo 的简介
（官方文档传送门：https://github.com/EsotericSoftware/kryo/blob/master/README.md）

（下面这段简介摘自：https://www.cnblogs.com/hntyzgn/p/7122709.html）

Kryo 是一个快速序列化/反序列化工具，其使用了字节码生成机制（底层依赖了 ASM 库），因此具有比较好的运行速度。

Kryo 序列化出来的结果，是其自定义的、独有的一种格式，不再是 JSON 或者其他现有的通用格式；而且，其序列化出来的结果是二进制的（即 byte[]；而 JSON 本质上是字符串 String）；二进制数据显然体积更小，序列化、反序列化时的速度也更快。

Kryo 一般只用来进行序列化（然后作为缓存，或者落地到存储设备之中）、反序列化，而不用于在多个系统、甚至多种语言间进行数据交换 —— 目前 kryo 也只有 java 实现。

像 Redis 这样的存储工具，是可以安全地存储二进制数据的，所以可以直接把 Kryo 序列化出来的数据存进去。

2、Kryo 的使用入门
pom依赖：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.3.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>kryo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>kryo</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
        <kryo.version>5.0.0-RC9</kryo.version>
    </properties>
     
    <dependencies>
        <dependency>
            <groupId>com.esotericsoftware</groupId>
            <artifactId>kryo</artifactId>
            <version>${kryo.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>
     
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>
     
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

Kryo序列化与反序列化的简单使用 ：

```java
@Test
public void testKryo() throws FileNotFoundException {
    User user = new User("id1", "aaa", 11);
    Kryo kryo = new Kryo();
    kryo.register(User.class);
    Output output = new Output(new FileOutputStream("test.txt"));
    kryo.writeObject(output, user);
    output.close();
    Input input = new Input(new FileInputStream("test.txt"));
    User user1 = kryo.readObject(input, User.class);
    input.close();
    assertTrue(user.getId().equals(user1.getId()));
}
```
```java
package com.example.kryo.redis;

public class User {
    private String id;
    private String name;
    private Integer age;

    public User() {
    }
     
    public User(String id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
     
    public String getId() {
        return id;
    }
     
    public void setId(String id) {
        this.id = id;
    }
     
    public String getName() {
        return name;
    }
     
    public void setName(String name) {
        this.name = name;
    }
     
    public Integer getAge() {
        return age;
    }
     
    public void setAge(Integer age) {
        this.age = age;
    }
     
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

}
```

序列化之后的test.txt中的内容是没有人眼可读性的：



3、Redis中的序列化与反序列化介绍
在项目中引入redis的pom依赖：

```xml
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
</dependency>
```
该组件提供了org.springframework.data.redis.serializer.RedisSerializer接口，实现了该接口的类，即可作为redis的序列化方式。同时该jar包也提供了一些已有的实现类，而且一些其他组件，像是我们常用的fastjson，也是有该接口实现类的，可以直接使用。（官方文档：https://docs.spring.io/spring-data/redis/docs/current/reference/html/#redis:serializer）



4、Redis中Kryo的使用示例
首先对RedisSerializer接口进行Kryo实现

```java
package com.example.kryo.utils;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayOutputStream;

/**
 * Redis的Kryo序列化
 *
 * @author DangerShi
 */
public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    private static final Logger logger = LoggerFactory.getLogger(KryoRedisSerializer.class);

    private KryoPool pool;
    private Class<T> clazz;

    public KryoRedisSerializer(Class<T> clazz) {
        this.clazz = clazz;
        this.pool = new KryoPool(clazz);
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);

        pool.obtain().writeObjectOrNull(output, t, clazz);
        output.flush();
        return byteArrayOutputStream.toByteArray();

    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        Input input = new Input(bytes);
        return pool.obtain().readObjectOrNull(input, clazz);
    }
}
```

 其中使用的KryoPool，需要我们自行对Kryo中的Pool抽象类进行实现：

```java
package com.example.kryo.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.Pool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 * Kryo池
   *

 * @author DangerShi
   */
   public class KryoPool extends Pool<Kryo> {

   private List<Class> classes = new ArrayList<>();


    public KryoPool(Class... clss) {
        super(true, true);
        Collections.addAll(classes, clss);
    }
     
    @Override
    protected Kryo create() {
        Kryo kryo = new Kryo();
        // 可自定义配置
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
     
        return kryo;
    }

}
```

Redis使用Kryo序列化的测试类如下：

```java
package com.example.kryo.redis;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.example.kryo.utils.KryoRedisSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**

 * Redis-Kryo测试
   *

 * @author DangerShi
   */
   public class RedisCacheImplTest {

   private Logger logger = LoggerFactory.getLogger(RedisCacheImplTest.class);

   private static RedisTemplate redisTemplate = new RedisTemplate();

   private static String host = "ip";

   private static int port = 6379;

   private static int maxTotal = 5;

   private static int maxIdle = 5;

   private static long maxWaitMillis = 3000;

   private static boolean testOnBorrow = true;

   private static RedisSerializer<String> keySerializer = new StringRedisSerializer();
   private RedisSerializer<User> valueSerializer = new KryoRedisSerializer<>(User.class);

   @BeforeAll
   public static void setUp() throws Exception {
       JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
       jedisPoolConfig.setMaxTotal(maxTotal);
       jedisPoolConfig.setMaxIdle(maxIdle);
       jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
       jedisPoolConfig.setTestOnBorrow(testOnBorrow);

       JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(jedisPoolConfig);
       jedisConnectionFactory.setHostName(host);
       jedisConnectionFactory.setPort(port);
       redisTemplate.setConnectionFactory(jedisConnectionFactory);
       redisTemplate.setKeySerializer(keySerializer);
       redisTemplate.afterPropertiesSet();

   }

   @AfterAll
   public static void tearDown() throws Exception {
   }

   @Test
   public void testKryo() throws FileNotFoundException {
       User user = new User("id1", "aaa", 11);
       Kryo kryo = new Kryo();
       kryo.register(User.class);
       Output output = new Output(new FileOutputStream("test.txt"));
       kryo.writeObject(output, user);
       output.close();
       Input input = new Input(new FileInputStream("test.txt"));
       User user1 = kryo.readObject(input, User.class);
       input.close();
       assertTrue(user.getId().equals(user1.getId()));
   }

   @Test
   public void writeUser() {
       User user = new User("id3", "ccc", 22);
       redisTemplate.setValueSerializer(valueSerializer);

       redisTemplate.executePipelined(new RedisCallback<Void>() {
           @Override
           public Void doInRedis(RedisConnection connection) throws DataAccessException {
               connection.set(redisTemplate.getKeySerializer().serialize(user.getId()),
                       redisTemplate.getValueSerializer().serialize(user));
               return null;
           }
       });

   }

   @Test
   public void readUser() {
       redisTemplate.setValueSerializer(valueSerializer);

       List<User> users = redisTemplate.executePipelined(new RedisCallback<User>() {
           @Override
           public User doInRedis(RedisConnection connection) throws DataAccessException {
               connection.get(redisTemplate.getKeySerializer().serialize("id3"));
               return null;
           }
       });
       System.out.println(users.toString());

   }


}
```

序列化写入后，



测试读取如下：



参考：

Kryo使用指南： https://www.cnblogs.com/hntyzgn/p/7122709.html

深入理解 RPC 之序列化篇 --Kryo ： https://www.cnkirito.moe/rpc-serialize-1/
