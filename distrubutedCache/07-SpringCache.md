## SpringCache

缓存的模式

1. 读模式

​	即带有缓存的数据的读取流程，即先读取缓存，缓存中不存在再读取数据库（见04-分布式锁，05-redisson）

2. 写模式

​	即带有缓存数据的写入流程，读写锁+失效模式（见06-缓存一致性）

上述两种模式的编码过程都很繁琐，因此Spring提供了SpringCache框架，可以很简洁的解决缓存的读写模式问题。

https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache



### 1.概念

`Cache` 和 `Buffer`

`Buffer`是一个快速短小的对象的中间临时存储，buffer的作用主要是相较于多个小的数据块(chunk)，buffer可以**将对象的一整个区块(block)一次性移动**，提高性能。数据在buffer中读写都是一次性的，并且buffer在整个业务流程中是可见的。

比如在文件IO流中，每次写入磁盘只写一个字节的话，那么写入一个大文件会非常的慢，但是通过buffer，可以一次写入 4096字节（或者更大的buffer），可以极大的提高写入效率，（数据从分散的内存中写入一次，写入磁盘中读取一次，然后buffer就清空了）。

类似的，网络IO中也会大量的使用buffer



`Cache` 是让**同样的数据可以快速地多次读取** ，也可以提高系统的性能。一般cache的存在是隐藏在业务流程中。

比如redis实现的分布式缓存，写入一次之后，只要数据库中的数据不变，缓存中的数据有效，就可以每次从redis中读取，利用redis的读取效率优势，提高系统的性能。



SpringCache

SpringCache提供的缓存抽象作用于java的方法。通过检查缓存的信息可以减少目标方法的调用。每次目标方法被调用时，都要检查是否之前通过传递**同样的参数**调用过，如果被调用过，就返回缓存的数据，这样就不需要真的调用目标方法了。如果目标方法没有被调用过，那么就调用目标方法，并且缓存调用的结果并返回结果给用户，那么下次再调用的时候就可以返回缓存的结果了。

通过这样的方式，不论是CPU密集型还是IO密集型的高消耗的方法，如果传入同样的参数就只会被调用一次，下次传入同样的参数就不需要真正的调用这个方法了。SpringCache的缓存逻辑的加载是无感知的，调用方不需要实现任何的接口。

**注意：SpringCache作用的方法必须保证每次传入的参数相同的情况下，返回值不论调用多少次都必须不变。**



SpringCache的重要接口：

```java
org.springframework.cache.Cache
```

```java
org.springframework.cache.CacheManager
```

Cache就是缓存，操作缓存**数据**的crud，Cache只定义接口，不做具体缓存数据的保存、清除等操作。具体的数据的操作由Cache的实现层来操作，比如ConcurrentMapCache底层基于ConcurrentHashMap实现的本地缓存， RedisCache是基于Redis的分布式缓存，还有很多其他的缓存实现。

CacheManager就是管理各种缓存的，可以管理不同名字的缓存，对缓存对象的创建、删除等。

<img src='D:\myNote\resources\image-20230504202147951.png' style="zoom:40%">



### 2.引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

引入SpringCache抽象层和Redis



### 3.配置

缓存的自动配置相关类，第一步，先找到AutoConfiguration类

```java
org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration
```

在AutoConfiguration上找注解 @EnableConfigurationProperties

```java
 @EnableConfigurationProperties(CacheProperties.class)
```

找到配置属性类

```java
org.springframework.boot.autoconfigure.cache.CacheProperties
```

以及导入注解 @Import

```java
@Import({ CacheConfigurationImportSelector.class, CacheManagerEntityManagerFactoryDependsOnPostProcessor.class })
```

找到导入的配置类

```java
org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.CacheConfigurationImportSelector
```

跟踪方法调用找到

```java
static String getConfigurationClass(CacheType cacheType) {
    String configurationClassName = MAPPINGS.get(cacheType);
    Assert.state(configurationClassName != null, () -> "Unknown cache type " + cacheType);
    return configurationClassName;
}
```

找到CacheType类

```java
org.springframework.boot.autoconfigure.cache.CacheType
```

是一个枚举，找到REDIS的引用处：

```java
mappings.put(CacheType.REDIS, RedisCacheConfiguration.class.getName());
```

找到 RedisCacheConfiguration类

```java
org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration
```

这个配置类就是将Redis的CacheManager放入Spring容器

```java
@Bean
RedisCacheManager cacheManager(...) {
    ...
}
```

其中就有配置的生成规则：

```java
private org.springframework.data.redis.cache.RedisCacheConfiguration createConfiguration(
    CacheProperties cacheProperties, ClassLoader classLoader) {
    Redis redisProperties = cacheProperties.getRedis();
    org.springframework.data.redis.cache.RedisCacheConfiguration config = org.springframework.data.redis.cache.RedisCacheConfiguration
        .defaultCacheConfig();
    config = config
        .serializeValuesWith(SerializationPair.fromSerializer(new JdkSerializationRedisSerializer(classLoader)));
    if (redisProperties.getTimeToLive() != null) {
        config = config.entryTtl(redisProperties.getTimeToLive());
    }
    if (redisProperties.getKeyPrefix() != null) {
        config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
    }
    if (!redisProperties.isCacheNullValues()) {
        config = config.disableCachingNullValues();
    }
    if (!redisProperties.isUseKeyPrefix()) {
        config = config.disableKeyPrefix();
    }
    return config;
}
```

其中 cacheProperties 就是properties文件提供的配置属性。



配置使用redis作为缓存的实现

```properties
spring.cache.type=redis
```



### 4.声明式缓存注解

#### 4.1@Cacheable

https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-annotations-cacheable

数据保存到缓存，表示当前方法的结果需要缓存



##### 设置缓存的 name

每个缓存的数据需要指定一个名字，建议按照业务类型区分。如 `product` `user` 



##### 设置缓存的key

缓存的位置需要分区和key，SpringCache的key的生成的默认策略：

- 如果没有参数，返回 `SimpleKey.EMPTY`.

- 如果只有一个参数，返回该实例

- 如果有不止一个参数，返回一个包括所有参数的 `SimpleKey` 

也可以声明key

```java
@Cacheable(cacheNames="books", key="#isbn")
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

@Cacheable(cacheNames="books", key="#isbn.rawNumber")
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)

@Cacheable(cacheNames="books", key="T(someType).hash(#isbn)")
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)
```

使用自定义KeyGenerator

```java
@Cacheable(cacheNames="books", keyGenerator="myKeyGenerator")
public Book findBook(ISBN isbn, boolean checkWarehouse, boolean includeUsed)
```

还可以指定为固定的字符串，注意不能直接写 "custom-key"，因为参数是作为spel表达式，常量字符串要再用`‘’`，如 `“‘custom-key’”`

```java
@Cacheable(cacheNames="books", key="'books'")
public Book findBooks()
```



##### 设置缓存的有效期

这个需要再配置文件中设置

```yaml
server:
  port: 8081

spring:
  application:
    name: spring-cache-demo
  cache:
    type: redis
    redis:
      # 设置有效期， 单位是ms
      time-to-live: 3600000
      # 给缓存设置前缀
      key-prefix: "com:xingray:cache:"
      # 是否使用前缀
      use-key-prefix: true
      # 是否缓存空值，缓存空值可以防止缓存穿透
      cache-null-values: true

  data:
    redis:
      password: 123456
      host: 192.168.0.108
      port: 6379
```



##### 配置redis的序列化器

这个不能使用配置文件，只能使用代码配置：

```java
package com.xingray.demo.springcache.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            CacheProperties cacheProperties) {
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
```

这里`key`使用`String`序列化器， `value`使用`Jackson`的`JSON`序列化器



代码示例：

```java
@Cacheable(cacheNames = "user", key = "'all'")
@Override
public List<User> list() {
    System.out.println("list");
    try {
        // access db
        System.out.println("access db");
        Thread.sleep(100);

        return new ArrayList<>(userMap.values());
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```



SpEl表达式的官方说明：

##### Available Caching SpEL Evaluation Context

Each `SpEL` expression evaluates against a dedicated [`context`](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#expressions-language-ref). In addition to the built-in parameters, the framework provides dedicated caching-related metadata, such as the argument names. The following table describes the items made available to the context so that you can use them for key and conditional computations:

| Name          | Location           | Description                                                  | Example                                                      |
| :------------ | :----------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| `methodName`  | Root object        | The name of the method being invoked                         | `#root.methodName`                                           |
| `method`      | Root object        | The method being invoked                                     | `#root.method.name`                                          |
| `target`      | Root object        | The target object being invoked                              | `#root.target`                                               |
| `targetClass` | Root object        | The class of the target being invoked                        | `#root.targetClass`                                          |
| `args`        | Root object        | The arguments (as array) used for invoking the target        | `#root.args[0]`                                              |
| `caches`      | Root object        | Collection of caches against which the current method is run | `#root.caches[0].name`                                       |
| Argument name | Evaluation context | Name of any of the method arguments. If the names are not available (perhaps due to having no debug information), the argument names are also available under the `#a<#arg>` where `#arg` stands for the argument index (starting from `0`). | `#iban` or `#a0` (you can also use `#p0` or `#p<#arg>` notation as an alias). |
| `result`      | Evaluation context | The result of the method call (the value to be cached). Only available in `unless` expressions, `cache put` expressions (to compute the `key`), or `cache evict` expressions (when `beforeInvocation` is `false`). For supported wrappers (such as `Optional`), `#result` refers to the actual object, not the wrapper. | `#result`                                                    |





#### 4.2@CachePut

https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-annotations-put

双写模式更新缓存，会将返回值重新写入缓存，要求方法有返回值

示例代码：

```java
@Caching(evict = {
    @CacheEvict(cacheNames = "user", key = "'all'")
}, put = {
    @CachePut(cacheNames = "user", key = "#user.id")
})
@Override
public User updateAndGet(User user) {
    System.out.println("updateAndGet");
    try {
        userMap.put(user.getId(), user);
        // access db
        System.out.println("access db");
        Thread.sleep(100);

        return user;
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```



#### 4.3 @CacheEvict

https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-annotations-evict

删除缓存，在删除数据或者数据更新使用失效模式时可以使用，示例代码：

```java
@Caching(evict = {
    @CacheEvict(cacheNames = "user", key = "#id"),
    @CacheEvict(cacheNames = "user", key = "'all'")
})
@Override
public void delete(Long id) {
    System.out.println("delete");
    try {
        userMap.remove(id);
        // access db
        System.out.println("access db");
        Thread.sleep(100);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```





#### 4.4@Caching

https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-annotations-caching

将多个 @Cacheable @CachePut @CacheEvict 进行组合

代码示例：

```java
@Caching(evict = {
    @CacheEvict(cacheNames = "user", key = "'all'")
}, put = {
    @CachePut(cacheNames = "user", key = "#user.id")
})
@Override
public User updateAndGet(User user) {
    System.out.println("updateAndGet");
    try {
        userMap.put(user.getId(), user);
        // access db
        System.out.println("access db");
        Thread.sleep(100);

        return user;
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```



#### 4.5@CacheConfig

https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-annotations-config

在同一个类上共享相同的缓存配置

@CacheConfig可以放在类上，也可以放在方法上。如果放在类上，则该类中所有的缓存方法都会继承该注解的属性。

@CacheConfig的属性可以被缓存方法上的@Cacheable、@CachePut、@CacheEvict等注解覆盖。

@CacheConfig的属性可以通过Spring的EL表达式进行动态配置。

代码示例：

```java
@CacheConfig(cacheNames = "myCache", cacheManager = "myCacheManager")
@Service
public class MyService {

    @Cacheable(key = "#id")
    public MyObject findById(Long id) {
        // ...
    }

    @CachePut(key = "#myObject.id")
    public MyObject save(MyObject myObject) {
        // ...
    }

    @CacheEvict(key = "#id")
    public void deleteById(Long id) {
        // ...
    }
}
```

在上面的示例中，@CacheConfig注解指定了缓存名称为"myCache"，缓存管理器为"myCacheManager"。这些属性会被该类中所有的缓存方法继承。



### 5.启动缓存功能

在启动类上添加 @EnableCaching



### 6.总结

SpringCache对分布式缓存的支持：

1.读模式

| 问题     | 产生原因                        | 解决方案       | SpringCache的解决方案                                        |
| -------- | ------------------------------- | -------------- | ------------------------------------------------------------ |
| 缓存穿透 | 查询一个不存在的数据            | 支持null值存储 | cache-null-values=true                                       |
| 缓存雪崩 | 大量的key同时过期               | 加锁           | 指定过期时间，存储的时间是分散的，过期时间也是分散的         |
| 缓存击穿 | 大量并发请求同一个刚好过期的key | 随机的过期时间 | 默认无锁，只有@cacheable(sync=true) 支持本地锁，其他注解不支持 |



2.写模式

缓存与数据库的一致性问题

- 加读写锁

- 引入canal，感知mysql的更新同步到缓存

- 读多写多，直接读取数据库



总结：

- 读多写少的常规数据，实时性一致性要求不高额，完全可以使用SpringCache，只要设置缓存过期时间就足够使用。 

- 实时性一致性要求高的特殊数据特殊设计。





### 7. 完整测试示例

pom：

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
    <artifactId>spring-cache-demo</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>19</maven.compiler.source>
        <maven.compiler.target>19</maven.compiler.target>
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

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>

    </dependencies>

</project>

```

application.yml

```
server:
  port: 8081

spring:
  application:
    name: spring-cache-demo
  cache:
    type: redis
    redis:
      # 设置有效期， 单位是ms
      time-to-live: 3600000
      # 给缓存设置前缀
      key-prefix: "com:xingray:cache:"
      # 是否使用前缀
      use-key-prefix: true
      # 是否缓存空值，缓存空值可以防止缓存穿透
      cache-null-values: true

  data:
    redis:
      password: 123456
      host: 192.168.0.108
      port: 6379
```

Application:

```java
package com.xingray.demo.springcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringCacheApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringCacheApplication.class, args);
    }
}
```

Config:

```java
package com.xingray.demo.springcache.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(
            CacheProperties cacheProperties) {
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
        config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()));
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }
        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
```

Entity

```java
package com.xingray.demo.springcache.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private Integer age;

}
```

Service

```java
package com.xingray.demo.springcache.service;

import com.xingray.demo.springcache.entity.User;

import java.util.List;

public interface UserService {

    void add(User user);

    void delete(Long id);

    void update(User user);

    User updateAndGet(User user);

    User find(Long id);

    List<User> list();
}
```

ServiceImpl

```java
package com.xingray.demo.springcache.service.impl;

import com.xingray.demo.springcache.entity.User;
import com.xingray.demo.springcache.service.UserService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> userMap;
    private long id;

    public UserServiceImpl() {
        userMap = new HashMap<>();
        userMap.put(1L, new User(1L, "jack", 18));
        userMap.put(2L, new User(2L, "tom", 19));
        userMap.put(3L, new User(3L, "gerry", 20));

        id = 4;
    }

    @CacheEvict(cacheNames = "user", key = "'all'")
    @Override
    public void add(User user) {
        System.out.println("add");
        try {
            user.setId(id);
            id++;
            userMap.put(user.getId(), user);
            // access db
            System.out.println("access db");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "user", key = "#id"),
            @CacheEvict(cacheNames = "user", key = "'all'")
    })
    @Override
    public void delete(Long id) {
        System.out.println("delete");
        try {
            userMap.remove(id);
            // access db
            System.out.println("access db");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Caching(evict = {
            @CacheEvict(cacheNames = "user", key = "#user.id"),
            @CacheEvict(cacheNames = "user", key = "'all'")
    })
    @Override
    public void update(User user) {
        System.out.println("update");
        try {
            userMap.put(user.getId(), user);
            // access db
            System.out.println("access db");
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "user", key = "'all'")
    }, put = {
            @CachePut(cacheNames = "user", key = "#user.id")
    })
    @Override
    public User updateAndGet(User user) {
        System.out.println("updateAndGet");
        try {
            userMap.put(user.getId(), user);
            // access db
            System.out.println("access db");
            Thread.sleep(100);

            return user;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable(cacheNames = "user", key = "#id")
    @Override
    public User find(Long id) {
        System.out.println("find");
        try {
            // access db
            System.out.println("access db");
            Thread.sleep(100);
            return userMap.get(id);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable(cacheNames = "user", key = "'all'")
    @Override
    public List<User> list() {
        System.out.println("list");
        try {
            // access db
            System.out.println("access db");
            Thread.sleep(100);

            return new ArrayList<>(userMap.values());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

Test:

```java
package com.xingray.demo.springcache.test;

import com.xingray.demo.springcache.entity.User;
import com.xingray.demo.springcache.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringCacheTest {

    private final UserService userService;

    @Autowired
    public SpringCacheTest(UserService userService) {
        this.userService = userService;
    }

    @Test
    public void springCacheTest01() {
        User user = userService.find(1L);
        System.out.println(user);

        User user2 = userService.find(1L);
        System.out.println(user2);
    }

    @Test
    public void deleteTest() {
        User user = userService.find(1L);
        System.out.println(user);

        userService.delete(1L);

        User user2 = userService.find(1L);
        System.out.println(user2);
    }

    @Test
    public void addTest(){
        userService.list();
        userService.list();

        userService.add(new User(0L, "atom", 21));

        userService.list();
    }
}
```

