## Thread入门

### 1.创建Thread的方式

1.继承`Thread`

```java
public class ThreadDemo {
    public static void main(String[] args) {
        new MyThread().start();
    }
    
    private static class MyThread extends Thread{
        @Override
        public void run() {
            System.out.println("do sth");
        }
    } 
}
```

2.实现`Runnable`

```java
public class RunnableDemo {
    public static void main(String[] args) {
        new Thread(new MyRunnable()).start();
    }
    
    private static class MyRunnable implements Runnable{
        @Override
        public void run() {
            System.out.println("do sth");
        }
    } 
}
```

3.Callable+FutureTask

```java
public class CallableDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> task = new FutureTask<>(new MyCallable());
        new Thread(task).start();
        Integer result = task.get();
        System.out.println("result:" + result);
    }

    private static class MyCallable implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("do calc");
            Integer result = 1 + 1;
            System.out.println("result:" + result);
            return result;
        }
    }
}
```

4.ThreadPool

```java
public class ThreadPoolDemo {
    public static void main(String[] args) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("do sth");
                }
            });
        }
    }
}
```



注意：前三种方式执行异步任务会无法控制计算资源，可能会导致系统崩溃，开发中都是使用线程池的方式执行异步任务。一个系统中一般只有一两个配置好的`ThreadPool`。
