## 线程池

### 1任务提交

ThreadPool提交任务可以是exec和submit，区别是submit支持传入callable，返回FutureTask

```java
public class ThreadPoolDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            Future<Integer> task = executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return 10;
                }
            });

            Integer result = task.get();
            System.out.println("result:" + result);

            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("do sth");
                }
            });
        }
    }
}
```



### 2 线程池的创建

#### 2.1 线程池的构造方法

```java
/**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler)
```



#### 2.2 参数说明

1.`int corePoolSize`

核心线程数，线程池创建好之后就准备就绪的线程数量，就算是没有任务，处于空闲状态也不会回收，会一直等待任务。

如果设置了 allowCoreThreadTimeOut 才会在指定的空闲时间后回收。



2.`int maximumPoolSize`

最大线程数，控制并发时的资源占用



3.`long keepAliveTime,TimeUnit unit`

最大存活时间，如果当前正在运行的线程数量大于核心线程数，超出核心线程数的部分会释放，释放的条件是线程没有任务执行，处于空闲状态，并且处于空闲的时间大于最大存活时间，那么这部分非核心线程就会被终止，释放系统资源。这部分的线程数 = maximumPoolSize - corePoolSize



4.`BlockingQueue<Runnable> workQueue`

任务队列，提交的任务太多时，会先在队列中保存，等待线程空闲时从任务队列中获取新的任务执行。



5.`ThreadFactory threadFactory`

线程工厂，用于创建Thread对象，可以用于自定线程名等。



6.`RejectedExecutionHandler handler`

拒绝策略，当任务队列（workQueue）满的时候，这时再提交任务的处理方式。



#### 2.3 工作顺序

1 线程池创建后，会准备好 `corePoolSize` 个核心线程，准备接收任务。

2 任务提交到线程池后，会直接交给空闲的核心线程执行。如果没有空闲的核心线程了，会放入任务队列中。有空闲的线程就回去任务队列中获取。

3 任务继续提交，如果任务队列满了，没有空间放置提交的任务，这时会创建新的线程（要求 `maximumPoolSize` > `corePoolSize`）

4 任务继续提交，此时总线程数到达`maximumPoolSize` ，则此时不会再创建线程了，新提交的任务会由 `handler` 处理。

5 任务执行完后，`maximumPoolSize` - `corePoolSize` 这部分线程如果在指定的 `keepAliveTime`时间内都没有接收到新任务，这部分线程会销毁。



上述所有的线程的创建都是由 `threadFactory`  完成的。一般设置为 `Executors.defaultThreadFactory()`



任务队列常用的是 `LinkedBlockingQueue` 注意在创建时要设置最大容量，默认是 `Integer.MAX_VALUE` ，这个值太大，容易导致内存占满。这个值一般由压力测试得到系统峰值得出。



`RejectedExecutionHandler` 常用的实现有

| 拒绝侧率            | 说明                                                      | 默认 |
| ------------------- | --------------------------------------------------------- | ---- |
| DiscardOldestPolicy | 丢弃最早的任务                                            |      |
| AbortPolicy         | 丢弃任务，会抛出异常                                      |      |
| CallerRunsPolicy    | 提交任务时，如果线程池没有关闭则直接执行任务的 `run` 方法 |      |
| DiscardPolicy       | 丢弃任务，不会抛出异常                                    |      |



示例：

问：一个线程池的配置： core： 7  ，max： 20，queue：50 ，此时有100个并发任务，线程池会怎么处理？

答：1. 7个任务立即由core线程执行，再50个出入queue， 再 20-7=13 个创建线程执行，这已经处理了 7+50+13=70个，剩余 100-70=30个由拒绝策略处理。



#### 2.4  常见的线程池

newCachedThreadPool

核心线程数为0，全部都是可回收线程

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```



newFixedThreadPool

核心线程数=最大线程数 固定线程数，全部都不可回收

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}
```



newScheduledThreadPool

可以做定时任务

```java
public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
    return new ScheduledThreadPoolExecutor(corePoolSize);
}
```



newSingleThreadExecutor

单线程的线程池，在后台从任务队列获取任务逐个执行

```java
	public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }
```



### 3 线程池的作用

- 降低资源消耗

通过重复利用已经创建好的线程来降低创建线程和销毁线程的损耗



- 提高响应速度

当有线程空闲时，无需等待就只立即执行提交的任务



- 提高线程的可管理性

线程池根据系统特性对池内线程做优化，减少创建和销毁的损耗。无限制创建和销毁线程会大量消耗系统资源，降低系统稳定性。使用线程池进行统一分配可以提高系统的稳定性。

比如可以创建两个线程池，A线程池处理业务流程，B线程池处理简单任务，当系统负载高时，可以临时关闭B线程池(shutdown)释放资源。

