1. springboot打包无法通过java -jar运行

普通的java项目可以通过maven-compiler-plugin插件打包

```bash
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<version>3.10.1</version>
	<configuration>
		<source>${java.version}</source>
		<target>${java.version}</target>
	</configuration>
</plugin>
```

但是springboot项目通过maven-compiler-plugin打包后通过java -jar命令运行会报错，提示无法找到主类，解决办法：

在springboot项目主pom中添加打包插件

```xml
<plugins>
	<plugin>
		<artifactId>spring-boot-maven-plugin</artifactId>
		<version>3.0.2</version>
		<groupId>org.springframework.boot</groupId>
	</plugin>
</plugins>
```

maven-compiler-plugin相关配置可以通过下列方式设置

```xml
<properties>
	<maven.compiler.source>19</maven.compiler.source>
	<maven.compiler.target>19</maven.compiler.target>
	<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	...
</properties>
```

再通过maven打包就可以正常运行了，打包命令：

```bash
sudo mvn -DskipTests=true clean package
```



2. java -jar 传入-D参数无效

**注意**：`-D`错误地配置在了`-jar`之后。

处理方式是将`-D`正确地配置在`-jar`之前即可。同样地，对`java 类名`方式启动也一样，注意要将`-D`配置在类名之前。