## CompletableFuture

使用场景

多个异步任务，如A、B、C，如A异步返回后，得到结果才能执行C，而B可以单独运行，不依赖A。可以使用`CompletableFuture`做**异步编排**



### 1 创建异步操作

```java
// 使用默认线程池执行异步任务，返回的Futrue类型为Void
public static CompletableFuture<Void> runAsync(Runnable runnable)
// 使用指定线程池执行异步任务，返回的Futrue类型为Void
public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
// 使用默认线程池执行异步任务，返回的Futrue类型为泛型类型U
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
// 使用指定线程池执行异步任务，返回的Futrue类型为泛型类型U
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```

带有 `executor` 参数的可以指定线程池



#### 1.1 runAsync

```java
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    System.out.println("当前线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    try {
        // 模拟耗时操作
        Thread.sleep(50);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    int result = 10 / 2;
    System.out.println("result:" + result);
}, executorService);

future.get();
System.out.println("main end");
```

```bash
当前线程:pool-1-thread-1, id:32
result:5
main end
```



#### 1.2 supplyAsync

```java
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
    System.out.println("当前线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    try {
        // 模拟耗时操作
        Thread.sleep(50);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    int result = 10 / 2;
    System.out.println("result:" + result);
    return result;
}, executorService);

Integer result = future.get();
System.out.println("main end result:" + result);
```

```bash
当前线程:pool-1-thread-1, id:32
result:5
main end result:5
```



### 2 任务结果处理

上述方法都可以返回一个future对象，拿到future对象后可以使用future的方法进行任务结果的处理

```java
// 同步阻塞返回结果
public T get()
// 任务完成后，在任务的执行线程执行action回调
public CompletableFuture<T> whenComplete(BiConsumer<? super T, ? super Throwable> action);
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action)
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor)
public CompletableFuture<T> exceptionally(Function<Throwable, ? extends T> fn)
public <U> CompletableFuture<U> handle(BiFunction<? super T, Throwable, ? extends U> fn)
```



#### 2.1 get 

```java
Integer result = future.get();
System.out.println("main end result:" + result);
```

```bash
main end result:5
```



#### 2.2 whenComplete/whenCompleteAsync

```java
CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    return 10 / 2;
}, executorService).whenComplete((result, exception) -> {
    System.out.println("回调线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    System.out.println("result:" + result);
    System.out.println("exception:" + exception);
});
```

```bash
计算线程:pool-1-thread-1, id:32
回调线程:pool-1-thread-1, id:32
result:5
exception:null
```



需要指定线程池使用 whenCompleteAsync

```java
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    return 10 / 2;
}, executorService).whenCompleteAsync((result, exception) -> {
    System.out.println("回调线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    System.out.println("result:" + result);
    System.out.println("exception:" + exception);
}, executorService);

Integer result = future.get();
System.out.println("main end result:" + result);
```

```java
计算线程:pool-1-thread-1, id:32
回调线程:pool-1-thread-2, id:33
result:5
exception:null
main end result:5
```



出现异常时，异常通过exception参数传递

```java
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    return 10 / 0;
}, executorService).whenCompleteAsync((result, exception) -> {
    System.out.println("回调线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    // 任务完成后的感知
    System.out.println("result:" + result);
    System.out.println("出现异常:" + exception.getMessage());
}, executorService);

Integer result = null;
try {
    result = future.get();
} catch (Exception e) {
    // ignore
}

System.out.println("main end result:" + result);
```

```bash
计算线程:pool-1-thread-1, id:32
回调线程:pool-1-thread-2, id:33
result:null
出现异常:java.lang.ArithmeticException: / by zero
main end result:null
```

whenCompleteAsync可以感知异常，但是无法真正处理异常，需要在出现异常的情况下任然有返回值，需要使用 exceptionally/exceptionallyAsync



#### 2.3 exceptionally/exceptionallyAsync

指定异常处理回调

```java
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    return 10 / 0;
}, executorService).whenCompleteAsync((result, exception) -> {
    System.out.println("回调线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    // 虽然可以得到异常信息，但是无法修改返回数据
    System.out.println("result:" + result);
    System.out.println("exception:" + exception);
}, executorService).exceptionallyAsync((throwable -> {
    System.out.println("异常处理线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    System.out.println("出现异常:" + throwable.getMessage());
    // 如果出现异常就 返回0
    return 0;
}), executorService);

// 虽然计算会产生异常，但是此处结果返回0，不会再抛出异常
Integer result = future.get();

System.out.println("result:" + result);
System.out.println("main end");
```

```bash
计算线程:pool-1-thread-1, id:32
回调线程:pool-1-thread-2, id:33
result:null
exception:java.util.concurrent.CompletionException: java.lang.ArithmeticException: / by zero
异常处理线程:pool-1-thread-3, id:34
出现异常:java.lang.ArithmeticException: / by zero
result:0
main end
```



#### 2.4 handle/handleAsync

可以同时处理结果和异常

```java
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    return 10 / 0;
}, executorService).handleAsync((result, throwable) -> {
    // 任务完成后的处理
    if (result != null) {
        return result * 2;
    }
    if (throwable != null) {
        System.out.println("throwable:" + throwable);
        return 0;
    }
    return 0;
}, executorService);

Integer result = future.get();
// 虽然计算会产生异常，但是根据异常处理流程，此处结果返回0
System.out.println("result:" + result);
System.out.println("main end");
```

```bash
计算线程:pool-1-thread-1, id:32
throwable:java.util.concurrent.CompletionException: java.lang.ArithmeticException: / by zero
result:0
main end
```



### 3 任务传递

```java
public CompletableFuture<Void> thenRun(Runnable action)
public CompletableFuture<Void> thenRunAsync(Runnable action)
public CompletableFuture<Void> thenRunAsync(Runnable action,Executor executor)

public CompletableFuture<Void> thenAccept(Consumer<? super T> action)
public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action)
public CompletableFuture<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor)

public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)
```



#### 3.1 thenRun/thenRunAsync

**不获取**到上一步的执行结果开启下一个任务，新开启的任务**没有返回值**

```java
CompletableFuture<Void> task = CompletableFuture.supplyAsync(() -> {
    System.out.println("任务1开始");
    int result = 10 / 4;
    System.out.println("任务1计算结果:" + result);
    return result;
}, executorService).thenRunAsync(new Runnable() {
    @Override
    public void run() {
        System.out.println("任务2开始");
        System.out.println("do sth");
        System.out.println("任务2完成");
    }
}, executorService);

task.get();
```

```bash
任务1开始
任务1计算结果:2
任务2开始
do sth
任务2完成
```



#### 3.2 thenAccept/thenAcceptAsync

获取上一步的执行结果开启下一个任务，新开启的任务没有返回值

```java
CompletableFuture<Void> task = CompletableFuture.supplyAsync(() -> {
    System.out.println("任务1计算线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 10 / 4;
    System.out.println("任务1计算结果:" + result);
    return result;
}, executorService).thenAcceptAsync(result -> {
    System.out.println("任务2开始，接收到上一步的计算结果:" + result);
    System.out.println("do sth");
    System.out.println("任务2结束");
}, executorService);

task.get();
```

```bash
任务1计算线程:pool-1-thread-1, id:32
任务1计算结果:2
任务2开始，接收到上一步的计算结果:2
do sth
任务2结束
```



#### 3.3 thenApply/thenApplyAsync

获取到上一步的执行结果开启下一个任务，新开启的任务带有返回值

```java
CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
    System.out.println("任务1计算线程:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 3 + 7;
    System.out.println("任务1计算结果:" + result);
    return result;
}, executorService).thenApplyAsync(result -> {
    // async 版将后续任务提交到线程池执行，传入线程池则使用指定的线程池，没有传入线程池使用上一步任务的线程池
    System.out.println("任务2接收到上一步的计算结果:" + result);
    int result2 = result * 2;
    System.out.println("任务2处理后的数据为:" + result2);
    return result2;
}, executorService);

try {
    System.out.println(future.get());
} catch (InterruptedException | ExecutionException e) {
    throw new RuntimeException(e);
}
```

```bash
任务1计算线程:pool-1-thread-1, id:32
任务1计算结果:10
任务2接收到上一步的计算结果:10
任务2处理后的数据为:20
20
```



### 4 两任务组合-全部完成

组合的两个任务都完成后再执行后续的操作

```java
public CompletableFuture<Void> runAfterBoth(CompletionStage<?> other, Runnable action)
public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action)
public CompletableFuture<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor)

public <U> CompletableFuture<Void> thenAcceptBoth(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action)
public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action)
public <U> CompletableFuture<Void> thenAcceptBothAsync(CompletionStage<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor)
        
public <U,V> CompletableFuture<V> thenCombine(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn)
public <U,V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn)
public <U,V> CompletableFuture<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T,? super U,? extends V> fn, Executor executor)
```



#### 4.1 runAfterBoth/runAfterBothAsync

不接收前面两个任务的结果，后续的操作也没有返回值

```java
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程1:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 3 + 7;
    System.out.println("计算结果1:" + result);
    return result;
}, executorService);

CompletableFuture<Void> task = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程2:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 5 * 3;
    System.out.println("计算结果2:" + result);
    return result;
}).runAfterBothAsync(task1, () -> {
    System.out.println("所有任务都完成了，开始任务3");
}, executorService);

task.get();
System.out.println("main end");
```

```
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算线程1:pool-1-thread-1, id:32
计算结果2:15
计算结果1:10
所有任务都完成了，开始任务3
main end
```



#### 4.2 thenAcceptBoth/thenAcceptBothAsync

接收前面两个任务的结果，但是后续的操作没有返回值

```java
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程1:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 3 + 7;
    System.out.println("计算结果1:" + result);
    return result;
}, executorService);

CompletableFuture<Void> task = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程2:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 5 * 3;
    System.out.println("计算结果2:" + result);
    return result;
}).thenAcceptBothAsync(task1, (result1, result2) -> {
    System.out.println("所有任务都完成了，结果分别为: result1:" + result1 + " result2:" + result2);
    System.out.println("开始任务3");
    System.out.println("do sth");
    System.out.println("任务3完成");
}, executorService);

System.out.println(task.get());
```

```
计算线程1:pool-1-thread-1, id:32
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算结果1:10
计算结果2:15
所有任务都完成了，结果分别为: result1:15 result2:10
开始任务3
do sth
任务3完成
null
```



#### 4.3 thenCombine/thenCombineAsync

接收前面两个任务的结果，后续的操作有返回值

```java
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程1:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 3 + 7;
    System.out.println("计算结果1:" + result);
    return result;
}, executorService);

CompletableFuture<String> task = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程2:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 5 * 3;
    System.out.println("计算结果2:" + result);
    return result;
}).thenCombineAsync(task1, (result1, result2) -> {
    System.out.println("所有任务都完成了，结果分别为: result1:" + result1 + " result2:" + result2);
    System.out.println("开始任务3");
    return "最终结果：(" + result1 + " , " + result2 + ")";
}, executorService);

System.out.println(task.get());
```

```bash
计算线程1:pool-1-thread-1, id:32
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算结果1:10
计算结果2:15
所有任务都完成了，结果分别为: result1:15 result2:10
开始任务3
最终结果：(15 , 10)
```



### 5 两任务组合-任意一个完成

组合的两个任务，任何一个完成了就开始执行后续的操作

```java
public CompletableFuture<Void> runAfterEither(CompletionStage<?> other, Runnable action)
public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action)
public CompletableFuture<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor)

public CompletableFuture<Void> acceptEither(CompletionStage<? extends T> other, Consumer<? super T> action)
public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action)
public CompletableFuture<Void> acceptEitherAsync(CompletionStage<? extends T> other, Consumer<? super T> action,Executor executor)

public <U> CompletableFuture<U> applyToEither(CompletionStage<? extends T> other, Function<? super T, U> fn)
public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn)
public <U> CompletableFuture<U> applyToEitherAsync(CompletionStage<? extends T> other, Function<? super T, U> fn, Executor executor)
```



#### 5.1 runAfterEither/runAfterEitherAsync

两个任务组合，有任意一个完成之后，就会触发后续的任务，后续的任务没有返回值

```java
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程1:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    try {
        Thread.sleep(200+new Random().nextInt(300));
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    int result = 3 + 7;
    System.out.println("计算结果1:" + result);
    return result;
}, executorService);

CompletableFuture<Void> task = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程2:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    try {
        Thread.sleep(200+new Random().nextInt(300));
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    int result = 5 * 3;
    System.out.println("计算结果2:" + result);
    return result;
}).runAfterEitherAsync(task1, () -> {
    System.out.println("有一个任务完成了，开始最终任务");
    System.out.println("do sth");
    System.out.println("最终任务完成");
}, executorService);

task.get();
System.out.println("main end");
```

有可能任务1先完成，然后立即触发最终任务

```bash
计算线程1:pool-1-thread-1, id:32
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算结果1:10
有一个任务完成了，开始最终任务
do sth
最终任务完成
main end
```

也可能任务2先完成，然后立即触发最终任务

```bash
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算线程1:pool-1-thread-1, id:32
计算结果2:15
有一个任务完成了，开始最终任务
do sth
最终任务完成
main end
计算结果1:10
```

也有可能2个任务完成的时间很接近，最终任务启动时，2个任务都已经完成了

```bash
计算线程1:pool-1-thread-1, id:32
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算结果2:15
计算结果1:10
有一个任务完成了，开始最终任务
do sth
最终任务完成
main end
```



#### 5.2 acceptEither/acceptEitherAsync

两个任务组合，有任意一个完成之后，可以拿到这个完成的任务的执行结果再执行后续的任务，后续的任务没有返回值

```java
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程1:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 3 + 7;
    System.out.println("计算结果1:" + result);
    return result;
}, executorService);

CompletableFuture<Void> task = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程2:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 5 * 3;
    System.out.println("计算结果2:" + result);
    return result;
}).acceptEitherAsync(task1, (result) -> {
    System.out.println("有任务都完成了，result:" + result);
    System.out.println("开始任务3");
    int finalResult = result * 2;
    System.out.println("最终结果：" + finalResult);
}, executorService);

System.out.println(task.get());
```

```bash
计算线程1:pool-1-thread-1, id:32
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算结果2:15
计算结果1:10
有任务都完成了，result:15
开始任务3
最终结果：30
null
```



#### 5.3 applyToEither/applyToEitherAsync

两个任务组合，有任意一个完成之后，可以拿到这个完成的任务的执行结果再执行后续的任务，后续的任务有返回值，可以继续向后传递

```java
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程1:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 3 + 7;
    System.out.println("计算结果1:" + result);
    return result;
}, executorService);

CompletableFuture<Integer> task = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程2:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 5 * 3;
    System.out.println("计算结果2:" + result);
    return result;
}).applyToEitherAsync(task1, (result) -> {
    System.out.println("所任务完成了，结果为: result:" + result);
    System.out.println("开始任务3");
    int finalResult = result * 2;
    System.out.println("最终结果：" + finalResult);
    return finalResult;
}, executorService);

System.out.println(task.get());
```

```bash
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算线程1:pool-1-thread-1, id:32
计算结果1:10
计算结果2:15
所任务完成了，结果为: result:15
开始任务3
最终结果：30
30
```



### 6 多任务组合-全部完成

```java
public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs)
```

所有的任务都完成之后，才能继续执行后续操作

```java
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程1:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 3 + 7;
    System.out.println("计算结果1:" + result);
    return result;
}, executorService);

CompletableFuture<Integer> task2 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程2:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 5 * 3;
    System.out.println("计算结果2:" + result);
    return result;
});

CompletableFuture<Integer> task3 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程3:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 6 * 7;
    System.out.println("计算结果3:" + result);
    return result;
});

CompletableFuture<Void> finalTask = CompletableFuture.allOf(task1, task2, task3);
finalTask.get();
System.out.println("所有任务都已完成");
```

```bash
计算线程1:pool-1-thread-1, id:32
计算线程3:ForkJoinPool.commonPool-worker-2, id:34
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算结果3:42
计算结果2:15
计算结果1:10
所有任务都已完成
```



### 7 多任务组合-任意一个完成

```java
public static CompletableFuture<Object> anyOf(CompletableFuture<?>... cfs)
```

多个任务组合，任意一个任务完成后，就可以执行后续的任务

```java
CompletableFuture<Integer> task1 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程1:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 3 + 7;
    System.out.println("计算结果1:" + result);
    return result;
}, executorService);

CompletableFuture<Integer> task2 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程2:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 5 * 3;
    System.out.println("计算结果2:" + result);
    return result;
});

CompletableFuture<Integer> task3 = CompletableFuture.supplyAsync(() -> {
    System.out.println("计算线程3:" + Thread.currentThread().getName() + ", id:" + Thread.currentThread().threadId());
    int result = 6 * 7;
    System.out.println("计算结果3:" + result);
    return result;
});

CompletableFuture<Object> task = CompletableFuture.anyOf(task1, task2, task3);

Object result = task.get();
System.out.println("有任务已完成, 结果为:" + result);
```

```bash
计算线程3:ForkJoinPool.commonPool-worker-2, id:34
计算线程1:pool-1-thread-1, id:32
计算线程2:ForkJoinPool.commonPool-worker-1, id:33
计算结果3:42
计算结果1:10
计算结果2:15
有任务已完成, 结果为:42
```

