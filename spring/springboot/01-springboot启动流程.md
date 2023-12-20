## SpringBoot启动流程

### 一、SpringBoot是什么

SpringBoot 是依赖于 Spring 的，比起 Spring，除了拥有 Spring 的全部功能以外，SpringBoot 无需繁琐的 Xml 配置，这取决于它自身强大的自动装配功能；并且自身已嵌入Tomcat、Jetty 等 web 容器，集成了 SpringMvc，使得 SpringBoot 可以直接运行，不需要额外的容器，提供了一些大型项目中常见的非功能性特性，如嵌入式服务器、安全、指标，健康检测、外部配置等，

其实 Spring 大家都知道，Boot 是启动的意思。所以，Spring Boot 其实就是一个启动 Spring 项目的一个工具而已，总而言之，SpringBoot 是一个服务于框架的框架；也可以说 SpringBoot 是一个工具，这个工具简化了 Spring 的配置；



### 二、Spring Boot的核心功能

1. 可独立运行的 Spring 项目：Spring Boot 可以以 Jar 包的形式独立运行
2.内嵌的 Servlet 容器：Spring Boot 可以选择内嵌 Tomcat、Jetty 或者 Undertow，无须以 war 包形式部署项目
3.简化的 Maven 配置：Spring 提供推荐的基础 POM 文件来简化 Maven 配置
4.自动配置 Spring：Spring Boot会根据项目依赖来自动配置 Spring 框架，极大地减少项目要使用的配置
5.提供生产就绪型功能：提供可以直接在生产环境中使用的功能，如性能指标、应用信息和应用健康检查
6.无代码生成和 Xml 配置：Spring Boot 不生成代码。完全不需要任何 Xml 配置即可实现 Spring 的所有配置

### 三、SpringBoot启动流程

SpringBoot的启动经过了一些一系列的处理，我们先看看整体过程的流程图

![d4ca070b0eb141f0b638ed6a838c8269](D:\myNote\resources\d4ca070b0eb141f0b638ed6a838c8269.png)



#### 3.1、运行 SpringApplication.run() 方法

可以肯定的是，所有的标准的 SpringBoot 的应用程序都是从 run 方法开始的

```java
package com.spring;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
 
@SpringBootApplication
public class App  {
    public static void main(String[] args) {
        // 启动springboot
        ConfigurableApplicationContext run = SpringApplication.run(App.class, args);
    }
}
```

进入 run 方法后，会 new 一个 SpringApplication 对象，创建这个对象的构造函数做了一些准备工作，编号第 2~5 步就是构造函数里面所做的事情

```java
/**
 * Static helper that can be used to run a {@link SpringApplication} from the
 * specified sources using default settings and user supplied arguments.
 * @param primarySources the primary sources to load
 * @param args the application arguments (usually passed from a Java main method)
 * @return the running {@link ApplicationContext}
 */
public static ConfigurableApplicationContext run(Class<?>[] primarySources,
		String[] args) {
	return new SpringApplication(primarySources).run(args);
}
```



####   3.2、确定应用程序类型

  在 SpringApplication 的构造方法内，首先会通过 `WebApplicationType.deduceFromClasspath()；` 方法判断当前应用程序的容器，默认使用的是Servlet 容器，除了 Servlet 之外，还有 NONE 和 REACTIVE （响应式编程）；

![e51dcfcb69ca41699cccca8ab204d02f](D:\myNote\resources\e51dcfcb69ca41699cccca8ab204d02f.png)

具体代码

```java
public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
   // resourceLoader 赋值为 Null
   this.resourceLoader = resourceLoader;
   
   // primarySources不为空，继续向下执行。为空抛异常
   Assert.notNull(primarySources, "PrimarySources must not be null");
   
   // 将 SpringbootdemoApplication（启动类）赋值给 primarySources 
   this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
   
   // 从 classpath 类路径推断 Web 应用类型，有三种 Web 应用类型，分别是
   // NONE: 该应用程序不应作为 Web 应用程序运行，也不应启动嵌入式 Web 服务器
   // SERVLET: 该应用程序应作为基于 servlet 的 Web 应用程序运行，并应启动嵌入式 servlet Web 服务器。
   // REACTIVE: 该应用程序应作为响应式 Web 应用程序运行，并应启动嵌入式响应式 Web 服务器
   this.webApplicationType = WebApplicationType.deduceFromClasspath();
   
   // 初始化 bootstrapRegistryInitializers，通过 getSpringFactoriesInstances（）获取工厂实例，
   // 底层使用的是反射Class<?> instanceClass = ClassUtils.forName(name, classLoader)动态加载实例对象。
   this.bootstrapRegistryInitializers = new ArrayList<>(
         getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
         
   // 初始化 ApplicationContextInitializer 集合
   setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
   
   // 初始化 ApplicationListener
   setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
   
   // 获取 StackTraceElement 数组遍历，通过反射获取堆栈中有 main 方法的类
   this.mainApplicationClass = deduceMainApplicationClass();
}
```



#### 3.3、加载所有的初始化器

这里加载的初始化器是 SpringBoot 自带初始化器，从从 META-INF/spring.factories 配置文件中加载的，那么这个文件在哪呢？自带有 2 个，分别在源码的 Jar 包的 spring-boot-autoconfigure 项目 和 spring-boot 项目里面各有一个

![在这里插入图片描述](D:\myNote\resources\c9a1248273e4444696bd9437fecb8e5d.png)

spring.factories文件里面，看到开头是 org.springframework.context.ApplicationContextInitializer 接口就是初始化器了

![在这里插入图片描述](D:\myNote\resources\5a9d39ba85db467495310ec25882cabb.png)

我们也可以自己实现一个自定义的初始化器：实现 ApplicationContextInitializer 接口既可

MyApplicationContextInitializer.java

```java
package com.spring.application;
 
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
/**
 * 自定义的初始化器
 */
public class MyApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        System.out.println("我是初始化的 MyApplicationContextInitializer...");
    }
}
```

在 resources 目录下添加 META-INF/spring.factories 配置文件，内容如下，将自定义的初始化器注册进去；

```java
org.springframework.context.ApplicationContextInitializer=\
com.spring.application.MyApplicationContextInitializer
```

![在这里插入图片描述](D:\myNote\resources\94b1928fd6f543699d2d7a668ae450c1.png)

启动 SpringBoot 后，就可以看到控制台打印的内容了，在这里我们可以很直观的看到它的执行顺序，是在打印 banner的后面执行的；

![在这里插入图片描述](D:\myNote\resources\e9c1837f38164a0bbb4d5aa61848590f.png)



#### 3.4、加载所有的监听器

加载监听器也是从 META-INF/spring.factories 配置文件中加载的，与初始化不同的是，监听器加载的是实现了 ApplicationListener 接口的类

![在这里插入图片描述](D:\myNote\resources\95a11b1dfe494850a977097af17a19e9.png)

自定义监听器也跟初始化器一样，依葫芦画瓢就可以了，这里不在举例；



#### 3.5、设置程序运行的主类

deduceMainApplicationClass(); 这个方法仅仅是找到 main 方法所在的类，为后面的扫包作准备，deduce 是推断的意思，所以准确地说，这个方法作用是推断出主方法所在的类；

![在这里插入图片描述](D:\myNote\resources\db20327eca8944e99132f03055d10e5e.png)



#### 3.6、开启计时器

程序运行到这里，就已经进入了 run 方法的主体了，第一步调用的 run 方法是静态方法，那个时候还没实例化 SpringApplication 对象，现在调用的 run 方法是非静态的，是需要实例化后才可以调用的，进来后首先会开启计时器，这个计时器有什么作用呢？顾名思义就使用来计时的嘛，计算 SpringBoot 启动花了多长时间；关键代码如下：

```java
// 实例化计时器
StopWatch stopWatch = new StopWatch(); 
// 开始计时
stopWatch.start();
```

run 方法代码段截图

![在这里插入图片描述](D:\myNote\resources\b936e9ace6ac4a3e90deaa5efc5d5f8f.png)



#### 3.7、将 java.awt.headless 设置为 true

这里将 java.awt.headless 设置为 true，表示运行在服务器端，在没有显示器器和鼠标键盘的模式下照样可以工作，模拟输入输出设备功能。

做了这样的操作后，SpringBoot 想干什么呢?其实是想设置该应用程序，即使没有检测到显示器，也允许其启动。对于服务器来说,是不需要显示器的，所以要这样设置

方法主体如下：

```java
private void configureHeadlessProperty() {
	System.setProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, System.getProperty(
			SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, Boolean.toString(this.headless)));
}
```

通过方法可以看到，setProperty() 方法里面又有个 getProperty()；这不多此一举吗？其实 getProperty() 方法里面有2个参数， 第一个 key值，第二个是默认值，意思是通过 key 值查找属性值，如果属性值为空，则返回默认值 true；保证了一定有值的情况；



#### 3.8、获取并启用监听器

这一步 通过监听器来实现初始化的的基本操作，这一步做了 2 件事情

1.创建所有 Spring 运行监听器并发布应用启动事件
2.启用监听器

![在这里插入图片描述](D:\myNote\resources\85aefad2cdc840f99fda7487427a51e3.png)



#### 3.9、设置应用程序参数

将执行 run 方法时传入的参数封装成一个对象

![在这里插入图片描述](D:\myNote\resources\f364315eaf634c3d8672782c06c73091.png)


仅仅是将参数封装成对象，没啥好说的，对象的构造函数如下

```java
public DefaultApplicationArguments(String[] args) {
	Assert.notNull(args, "Args must not be null");
	this.source = new Source(args);
	this.args = args;
}
```

那么问题来了，这个参数是从哪来的呢？其实就是 main 方法里面执行静态 run 方法传入的参数

![在这里插入图片描述](D:\myNote\resources\bcc7b88cc16f44dfb75fe78c7b8cf559.png)



#### 3.10、准备环境变量

准备环境变量，包含系统属性和用户配置的属性，执行的代码块在 prepareEnvironment 方法内

![在这里插入图片描述](D:\myNote\resources\85de6db96b0b4f79a2494e0c2d5a8f4e.png)

打了断点之后可以看到，它将 maven 和系统的环境变量都加载进来了

![在这里插入图片描述](D:\myNote\resources\0c2e56e223bd40af8f808e3550a6915d.png)



#### 3.11、忽略 bean 信息

这个方法 configureIgnoreBeanInfo() 这个方法是将 spring.beaninfo.ignore 的默认值值设为 true，意思是跳过 beanInfo 的搜索，其设置默认值的原理和第 7 步一样；

```java
private void configureIgnoreBeanInfo(ConfigurableEnvironment environment) {
	if (System.getProperty(
			CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME) == null) {
		Boolean ignore = environment.getProperty("spring.beaninfo.ignore",
				Boolean.class, Boolean.TRUE);
		System.setProperty(CachedIntrospectionResults.IGNORE_BEANINFO_PROPERTY_NAME,
				ignore.toString());
	}
}
```


 当然也可以在配置文件中添加以下配置来设为 false

```properties
spring.beaninfo.ignore=false
```



#### 3.12、打印 banner 信息

显而易见，这个流程就是用来打印控制台那个很大的 Spring 的 Banner 的，就是下面这个东东

![在这里插入图片描述](D:\myNote\resources\cdf92f42c31c48d095ceec97ae11058b.png)


那他在哪里打印的呢？他在 SpringBootBanner.java 里面打印的，这个类实现了 Banner 接口，

而且 Banner 信息是直接在代码里面写死的；

![在这里插入图片描述](D:\myNote\resources\53e5e0cd822040359f6b26b8cc324ce2.png)

有些公司喜欢自定义 Banner 信息，如果想要改成自己喜欢的图标该怎么办呢，其实很简单,只需要在 resources 目录下添加一个 banner.txt 的文件即可，txt 文件内容如下

```bash
,-----.                         
|  |) /_  ,---.  ,--,--.,--.--. 
|  .-.  \| .-. :' ,-.  ||  .--' 
|  '--' /\   --.\ '-'  ||  |    
`------'  `----' `--`--'`--'    
```

一定要添加到 resources 目录下，别加错了

![在这里插入图片描述](D:\myNote\resources\d479de9a586946be99de8739b099f1fc.png)


只需要加一个文件即可，其他什么都不用做，然后直接启动 SpringBoot，就可以看到效果了



#### 3.13、创建应用程序的上下文

实例化应用程序的上下文， 调用 createApplicationContext() 方法，这里就是用反射创建对象，没什么好说的；

![在这里插入图片描述](D:\myNote\resources\c3eaf40f8a5d43749332cd02aea15bc1.png)



#### 3.14、实例化异常报告器

异常报告器是用来捕捉全局异常使用的，当 SpringBoot 应用程序在发生异常时，异常报告器会将其捕捉并做相应处理，在 spring.factories 文件里配置了默认的异常报告器

![在这里插入图片描述](D:\myNote\resources\eeffb5c165ca49d885d6908ba0b49409.png)

需要注意的是，这个异常报告器只会捕获启动过程抛出的异常，如果是在启动完成后，在用户请求时报错，异常报告器不会捕获请求中出现的异常

![在这里插入图片描述](D:\myNote\resources\1058f5b5ecd146e0b3ab9089a1dfbe1f.png)


了解原理了，接下来我们自己配置一个异常报告器来玩玩；

MyExceptionReporter.java 继承 SpringBootExceptionReporter 接口

```java
package com.spring.application;
 
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.context.ConfigurableApplicationContext;
 
public class MyExceptionReporter implements SpringBootExceptionReporter {
 
 
    private ConfigurableApplicationContext context;
    // 必须要有一个有参的构造函数，否则启动会报错
    MyExceptionReporter(ConfigurableApplicationContext context) {
        this.context = context;
    }
 
    @Override
    public boolean reportException(Throwable failure) {
        System.out.println("进入异常报告器");
        failure.printStackTrace();
        // 返回false会打印详细springboot错误信息，返回true则只打印异常信息 
        return false;
    }
}
```

在 spring.factories 文件中注册异常报告器

```properties
# Error Reporters 异常报告器
org.springframework.boot.SpringBootExceptionReporter=\
com.spring.application.MyExceptionReporter
```

![在这里插入图片描述](D:\myNote\resources\2c892da4007048b78da3b51e8cff48b3.png)

接着我们在 application.yml 中 把端口号设置为一个很大的值，这样肯定会报错

```java
server:
  port: 80828888
```

启动后，控制台打印如下图

![在这里插入图片描述](D:\myNote\resources\bb6a03964e1746cb852b13656b3d17e5.png)



#### 3.15、准备上下文环境

这里准备的上下文环境是为了下一步刷新做准备的，里面还做了一些额外的事情；

![在这里插入图片描述](D:\myNote\resources\87505d55ace647048735c0d6fd3cbe5d.png)

##### 1、实例化单例的beanName生成器

在 postProcessApplicationContext(context); 方法里面。使用单例模式创建 了BeanNameGenerator 对象，其实就是 beanName 生成器，用来生成 bean 对象的名称

##### 2、执行初始化方法

初始化方法有哪些呢？还记得第 3 步里面加载的初始化器嘛？其实是执行第3步加载出来的所有初始化器，实现 ApplicationContextInitializer 接口的类

##### 3、将启动参数注册到容器中

这里将启动参数以单例的模式注册到容器中，是为了以后方便拿来使用，参数的beanName 为 ：springApplicationArguments



#### 3.16、刷新上下文

刷新上下文已经是 Spring 的范畴了，自动装配和启动 tomcat 就是在这个方法里面完成的，还有其他的 Spring 自带的机制在这里就不一一细说了

![在这里插入图片描述](D:\myNote\resources\50bffb4da1aa4153a3b09246d570eb87.png)



#### 3.17、刷新上下文后置处理

afterRefresh 方法是启动后的一些处理，留给用户扩展使用，目前这个方法里面是空的

```java
/**
 * Called after the context has been refreshed.
 * @param context the application context
 * @param args the application arguments
 */
protected void afterRefresh(ConfigurableApplicationContext context,
		ApplicationArguments args) {
}
```



#### 3.18、结束计时器

到这一步，Springboot 其实就已经完成了，计时器会打印启动 SpringBoot 的时长

![在这里插入图片描述](D:\myNote\resources\a41f171c828a470f97b498b8e8e4325f.png)

在控制台看到启动还是挺快的，不到 2 秒就启动完成了；



#### 3.19、发布上下文准备就绪事件

告诉应用程序，我已经准备好了，可以开始工作了

![在这里插入图片描述](D:\myNote\resources\d261bb6b728948cfb4cc49412e7474d4.png)

执行自定义的 run 方法

这是一个扩展功能，callRunners(context, applicationArguments) 可以在启动完成后执行自定义的 run 方法；有 2 种方式可以实现：

实现 ApplicationRunner 接口
实现 CommandLineRunner 接口
接下来我们验证一把，为了方便代码可读性，我把这 2 种方式都放在同一个类里面

```java
package com.spring.init;
 
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
 
/**
 * 自定义run方法的2种方式
 */
@Component
public class MyRunner implements ApplicationRunner, CommandLineRunner {
 
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println(" 我是自定义的run方法1，实现 ApplicationRunner 接口既可运行"        );
    }
 
    @Override
    public void run(String... args) throws Exception {
        System.out.println(" 我是自定义的run方法2，实现 CommandLineRunner 接口既可运行"        );
    }
}
```

启动 SpringBoot 后就可以看到控制台打印的信息了

![在这里插入图片描述](D:\myNote\resources\ef344208a23149669575a0a8ade4502d.png)


具体代码

```java
public ConfigurableApplicationContext run(String... args) {
   long startTime = System.nanoTime();
   // 通过 BootstrapRegistryInitializer来initialize 默认的 DefaultBootstrapContext
   DefaultBootstrapContext bootstrapContext = createBootstrapContext();
   ConfigurableApplicationContext context = null;
   
   // 配置java.awt.headless属性
   configureHeadlessProperty();
   
   // 获取 SpringApplicationRunListeners 监听器
   SpringApplicationRunListeners listeners = getRunListeners(args);
   
   // 启动 SpringApplicationRunListeners 监听，表示 SpringApplication 启动（触发ApplicationStartingEvent事件）
   listeners.starting(bootstrapContext, this.mainApplicationClass);
   try {
      // 创建 ApplicationArguments 对象，封装了 args 参数
      ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
      
      // 做相关环境准备，绑定到 SpringApplication,返回可配置环境对象ConfigurableEnvironment 
      ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);
      
      // 配置 spring.beaninfo.ignore，设置为 true.即跳过搜索Bean信息
      configureIgnoreBeanInfo(environment);
      
      // 控制台打印 SpringBoot 的 Banner（横幅）标志
      Banner printedBanner = printBanner(environment);
      
      // 根据WebApplicationType从ApplicationContextFactory工厂创建ConfigurableApplicationContext
      context = createApplicationContext();
      
      // 设置ConfigurableApplicationContext中的ApplicationStartup为DefaultApplicationStartup
      context.setApplicationStartup(this.applicationStartup);
      
      // 应用所有的ApplicationContextInitializer容器初始化器初始化context,触发ApplicationContextInitializedEvent事件监听，打印启动日志信息，启动Profile日志信息
      // ConfigurableListableBeanFactory中注册单例Bean（springApplicationArguments）,并为该BeanFactory中的部分属性赋值。
      // 加载所有的source.并将Bean加载到ConfigurableApplicationContext，触发ApplicationPreparedEvent事件监听
      prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
      
      // 刷新容器（在方法中集成了Web容器具体请看 https://editor.csdn.net/md/?articleId=123136262）
      refreshContext(context);
      
      // 刷新容器的后置处理（空方法）
      afterRefresh(context, applicationArguments);
      
      // 启动花费的时间
      Duration timeTakenToStartup = Duration.ofNanos(System.nanoTime() - startTime);
      if (this.logStartupInfo) {
         // 打印日志Started xxx in xxx seconds (JVM running for xxxx)
         new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), timeTakenToStartup);
      }
      
      // 触发 ApplicationStartedEvent 事件监听。上下文已刷新，应用程序已启动。
      listeners.started(context, timeTakenToStartup);
      
      // 调用 ApplicationRunner和CommandLineRunner
      callRunners(context, applicationArguments);
   }
   // 处理运行时发生的异常，触发 ApplicationFailedEvent 事件监听
   catch (Throwable ex) {
      handleRunFailure(context, ex, listeners);
      throw new IllegalStateException(ex);
   }
   try {
      // 启动准备消耗的时间
      Duration timeTakenToReady = Duration.ofNanos(System.nanoTime() - startTime);
      // 在 run 方法完成前立即触发 ApplicationReadyEvent事件监听,表示应用上下文已刷新，并且CommandLineRunners和ApplicationRunners已被调用。
      listeners.ready(context, timeTakenToReady);
   }
   catch (Throwable ex) {
      handleRunFailure(context, ex, null);
      throw new IllegalStateException(ex);
   }
   return context;
}
```

