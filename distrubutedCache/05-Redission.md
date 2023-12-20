## Redission

基于Redis的分布式锁的实现

https://redis.io/docs/manual/patterns/distributed-locks/

Java版本

https://github.com/redisson/redisson



### 1.引入依赖

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson</artifactId>
    <version>3.21.0</version>
</dependency>
```



### 2.设置

参考文档

https://github.com/redisson/redisson/wiki/Table-of-Content

单节点模式：

https://github.com/redisson/redisson/wiki/2.-Configuration/#26-single-instance-mode

```java
package com.xingray.redis.demo.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    // 服务停止后会调用这个 bean对象， RedissonClient的shutdown方法
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://192.168.0.108:6379")
                .setPassword("123456");
        return Redisson.create(config);
    }
}
```



### 3.测试

```java
package com.xingray.redis.demo.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RedissonTest {

    private final RedissonClient redissonClient;

    @Autowired
    public RedissonTest(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Test
    public void redissonTest01(){
        RLock lock = redissonClient.getLock("redisson-test-lock");
        lock.lock();
        try {
            Thread.sleep(200);
            System.out.println("do something");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
```



### 4.使用

Redisson的锁实现了juc中的锁的接口，使用方式与juc锁一致，各个实例中获取锁 `redissonClient.getLock("redisson-test-lock");`传入的锁的 `name`  一样，则获取的锁也是一样的。



#### 4.1 自动加锁

```java
@Test
public void redissonTest01(){
    // 获取锁，name相同，则获取的锁也是相同的
    RLock lock = redissonClient.getLock("redisson-test-lock");

    // 尝试加锁，同步阻塞式
    lock.lock();
    try {
        // 模拟超长业务流程
        Thread.sleep(30000);
        System.out.println("do something");
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        // 释放锁
        lock.unlock();
    }
}
```

在redis中的锁保存的数据为：

```
key ： redisson-test-lock 
类型： hash
值：
	key：258d381c-5348-4279-858d-4baaf750eeb7:1 
	value：1
```

其中key `258d381c-5348-4279-858d-4baaf750eeb7:1`  是由一串uuid加上线程号组成，是全局唯一的

另外redisson锁的特性：

1. 锁的默认 TTL 为 30s，加锁线程崩溃，锁会自动失效
2. 锁会自动续期，redisson有看门狗机制，调用`lock()`方法会启动一个`timer`，生效时间走完1/3后会再续期

相关源码实现参考：

```java
org.redisson.RedissonLock#lock(long, java.util.concurrent.TimeUnit, boolean)
```



#### 4.2 设置超时时间

```java
@Test
public void redissonTest01(){
    // 获取锁，name相同，则获取的锁也是相同的
    RLock lock = redissonClient.getLock("redisson-test-lock");

    // 尝试加锁，同步阻塞式
    // 设置超时时间为 5s
    lock.lock(5, TimeUnit.SECONDS);
    try {
        // 模拟超长业务流程
        Thread.sleep(10000);
        System.out.println("do something");
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    } finally {
        // 释放锁
        lock.unlock();
    }
}
```

这里假设设置5秒超时时间，业务执行10s，必定会超时，此时解锁失败

```bash
java.lang.IllegalMonitorStateException: attempt to unlock lock, not locked by current thread by node id: 3ebd00eb-fe0b-4a2e-b752-73414900df97 thread-id: 1

	at org.redisson.RedissonBaseLock.lambda$unlockAsync0$2(RedissonBaseLock.java:293)
	at java.base/java.util.concurrent.CompletableFuture.uniHandle(CompletableFuture.java:934)
	at java.base/java.util.concurrent.CompletableFuture$UniHandle.tryFire(CompletableFuture.java:911)
	at java.base/java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:510)
	at java.base/java.util.concurrent.CompletableFuture.complete(CompletableFuture.java:2179)
	...
```

注意：使用带超时时间的lock方法，reddisson**不会自动续期**，业务执行时间一定要小于锁的超时时间。

相关源码：

```java
private <T> RFuture<Long> tryAcquireAsync(long waitTime, long leaseTime, TimeUnit unit, long threadId) {
    RFuture<Long> ttlRemainingFuture;
    if (leaseTime > 0) {
        ttlRemainingFuture = tryLockInnerAsync(waitTime, leaseTime, unit, threadId, RedisCommands.EVAL_LONG);
    } else {
        ttlRemainingFuture = tryLockInnerAsync(waitTime, internalLockLeaseTime,
                                               TimeUnit.MILLISECONDS, threadId, RedisCommands.EVAL_LONG);
    }
    CompletionStage<Long> s = handleNoSync(threadId, ttlRemainingFuture);
    ttlRemainingFuture = new CompletableFutureWrapper<>(s);

    CompletionStage<Long> f = ttlRemainingFuture.thenApply(ttlRemaining -> {
        // lock acquired
        if (ttlRemaining == null) {
            if (leaseTime > 0) {
                internalLockLeaseTime = unit.toMillis(leaseTime);
            } else {
                scheduleExpirationRenewal(threadId);
            }
        }
        return ttlRemaining;
    });
    return new CompletableFutureWrapper<>(f);
}
```

关键处：

```java
if (leaseTime > 0) {
    internalLockLeaseTime = unit.toMillis(leaseTime);
} else {
    scheduleExpirationRenewal(threadId);
}
```

传递了超时时间，不会启动自动续期，没有传入超时时间，会调用 `scheduleExpirationRenewal` 进行自动续期

自动续期会执行下面的放阿飞：

```java
private void renewExpiration() {
    ExpirationEntry ee = EXPIRATION_RENEWAL_MAP.get(getEntryName());
    if (ee == null) {
        return;
    }

    Timeout task = commandExecutor.getServiceManager().newTimeout(new TimerTask() {
        @Override
        public void run(Timeout timeout) throws Exception {
            ExpirationEntry ent = EXPIRATION_RENEWAL_MAP.get(getEntryName());
            if (ent == null) {
                return;
            }
            Long threadId = ent.getFirstThreadId();
            if (threadId == null) {
                return;
            }

            CompletionStage<Boolean> future = renewExpirationAsync(threadId);
            future.whenComplete((res, e) -> {
                if (e != null) {
                    log.error("Can't update lock {} expiration", getRawName(), e);
                    EXPIRATION_RENEWAL_MAP.remove(getEntryName());
                    return;
                }

                if (res) {
                    // reschedule itself
                    renewExpiration();
                } else {
                    cancelExpirationRenewal(null);
                }
            });
        }
    }, internalLockLeaseTime / 3, TimeUnit.MILLISECONDS);

    ee.setTimeout(task);
}
```

注意 `newTimeout`的参数 `internalLockLeaseTime / 3` 即 设置超时时间的 1/3后重新续期，并且续期后执行程序的内部会再次调用

```java
// reschedule itself
renewExpiration();
```

实现无限续期



更多关于redisson的锁的使用参考官方文档

https://github.com/redisson/redisson/wiki/8.-distributed-locks-and-synchronizers#81-lock

或者 `/document/Redisson-distributed-locks-and-synchronizers`



### 5.项目实战

```java
private Map<String, List<CategoryLevel2Vo>> loadCategoryMapFromCachedRedisson() throws JsonProcessingException {
    log.info("loadCategoryMapFromCachedRedisson()");
    ObjectMapper objectMapper = new ObjectMapper();
    ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();

    // 先尝试读取缓存
    String json = operations.get(REDIS_KEY_PRODUCT_CATEGORY_MAP);
    if (json == null) {
        RLock lock = redissonClient.getLock(REDIS_KEY_PRODUCT_CATEGORY_MAP_LOCK);
        lock.lock();
        try {
            // 获取到锁，还需要再次检测缓存，double check
            json = operations.get(REDIS_KEY_PRODUCT_CATEGORY_MAP);
            if (json == null) {
                // 还是没有读取到缓存, 则可以确认缓存中没有数据，读取数据库
                HashMap<String, List<CategoryLevel2Vo>> categoryMapFromDb = loadCategoryMapFromDb();
                // 防止缓存穿透，空值也要保存为自定义标识数据
                json = CollectionUtil.hasElement(categoryMapFromDb) ? objectMapper.writeValueAsString(categoryMapFromDb) : REDIS_NULL;
                // 存入缓存
                operations.set(REDIS_KEY_PRODUCT_CATEGORY_MAP, json);
                // 返回数据
                return categoryMapFromDb;
            } else {
                // 读取到缓存
                if (json.equals(REDIS_NULL)) {
                    // 读取到空数据标识，返回空数据
                    return Collections.emptyMap();
                } else {
                    // 返回数据
                    return objectMapper.readValue(json, new TypeReference<>() {
                    });
                }
            }
        } finally {
            lock.unlock();
        }
    } else {
        // 读取到缓存
        if (json.equals(REDIS_NULL)) {
            // 缓存中为空数据标识，返回空数据
            return Collections.emptyMap();
        } else {
            // 返回数据
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        }
    }
}
```

