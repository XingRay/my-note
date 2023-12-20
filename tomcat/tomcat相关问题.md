# tomcat相关问题

遇到 no plugin found for prefix 'tomcat 7' in the current project and in the plugin groups 的解决办法



找到这个 settings.xml 文件,进行编辑,在pluginGroups标签下加入下面的配置 


```xml
<pluginGroups>
<pluginGroup>org.apache.tomcat.maven</pluginGroup>
</pluginGroups>
```







[spring - tomcat - NoSuchMethodError setContentLengthLong](https://stackoverflow.com/questions/67026939/spring-tomcat-nosuchmethoderror-setcontentlengthlong)

I am trying to simply provide a static page with spring and tomcat 7.0.85 but I am getting the following error when i access

2021-04-09 15:56:43.677 ERROR 3388 --- [o-8080-exec-169] o.s.b.w.servlet.support.ErrorPageFilter  : Forwarding to error page from request [/] due to exception [javax.servlet.http.HttpServletResponse.setContentLengthLong(J)V]

java.lang.NoSuchMethodError: javax.servlet.http.HttpServletResponse.setContentLengthLong(J)V at  org.springframework.http.server.ServletServerHttpResponse.writeHeaders(ServletServerHttpResponse.java:130) ~[spring-web-5.3.5.jar:5.3.5]


回答：

The method in question (ServletResponse#setContentLengthLong) appeared in the Servlet 3.1 specification and therefore is not supported in Tomcat 7.0 (cf. Tomcat documentation).

You are using the latest version of Spring, so you should use the latest version of Tomcat compatible with it: version 9.0. Spring 5.x requires a Servlet 3.1 container, but can also use Servlet 4.0 features (cf. Spring release notes).

Remark: Tomcat 10 is a Jakarta EE 9 servlet container and there is no version of Spring for it yet.

是由于SpringMvc版本太新导致，要么升级tomcat， 要么把springmvc降级
```xml
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-webmvc</artifactId>
	<version>5.3.17</version>
</dependency>
```
实测5.3.17会触发该问题，

```xml
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-webmvc</artifactId>
	<version>5.1.5.RELEASE</version>
</dependency>
```
改为 5.1.5.RELEASE 问题没有出现





使用tomcat7 run 指令项目没能运行

[INFO] 
[INFO] <<< tomcat7-maven-plugin:2.2:run (default-cli) < process-classes @ springsecurity <<<
[INFO] 
[INFO] 
[INFO] --- tomcat7-maven-plugin:2.2:run (default-cli) @ springsecurity ---
[INFO] Skipping non-war project
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.255 s
[INFO] Finished at: 2022-04-04T21:15:50+08:00
[INFO] ------------------------------------------------------------------------

Process finished with exit code 0

需要在pom.xml中声明打包方式为war

```xml
<packaging>war</packaging>
```
