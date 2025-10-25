## slf4j+logback



### 1.引入依赖

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.17</version>
</dependency>

<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.20</version>
</dependency>
```

或者

```kotlin
implementation("org.slf4j:slf4j-api:2.0.17")
implementation("ch.qos.logback:logback-classic:1.5.20")
```



### 2.创建配置文件

在resource目录下创建配置文件 logback.xml ，内容如下，根据需要进行修改即可：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <property name="LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level at %c.%M (%F:%L\) %msg%n"/>

    <property name="SIMPLE_LOG_PATTERN"
              value="%msg%n"/>

    <property name="LOG_PATH" value="./log"/>
    <property name="LOG_NAME" value="MyApp"/>

    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <appender name="RollingFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${LOG_NAME}.log</file>

        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>

        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${LOG_NAME}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
        </rollingPolicy>

    </appender>

    <root level="debug">
        <appender-ref ref="Console"/>
        <appender-ref ref="RollingFile"/>
    </root>
</configuration>
```

简易版：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>

    <property name="LOG_PATTERN"
              value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level at %c.%M (%F:%L\) %msg%n"/>

    <property name="SIMPLE_LOG_PATTERN"
              value="%msg%n"/>

    <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>

    <root level="debug">
        <appender-ref ref="Console"/>
    </root>
</configuration>
```



### 3. 创建logger对象

在需要输出日志的类中创建logger对象：

```java
private static final Logger logger = LoggerFactory.getLogger(Main.class);
```

调用logger的方法即可

