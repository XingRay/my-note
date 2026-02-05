# [Spring Security 入门（基本使用） ](https://www.cnblogs.com/CF1314/p/14766623.html)


**目录**

- [1、入门](https://www.cnblogs.com/CF1314/p/14766623.html#_label0)
  - [1.1、什么是 spring security](https://www.cnblogs.com/CF1314/p/14766623.html#_label0_0)
  - [1.2、依赖配置](https://www.cnblogs.com/CF1314/p/14766623.html#_label0_1)
  - [1.3、测试接口](https://www.cnblogs.com/CF1314/p/14766623.html#_label0_2)
- [2、自定义登录页面、登录成功处理器、登录失败处理器、异常处理器、权限逻辑](https://www.cnblogs.com/CF1314/p/14766623.html#_label1)
  - [2.1、自定义登录页面](https://www.cnblogs.com/CF1314/p/14766623.html#_label1_0)
  - [2.2、自定义登录逻辑](https://www.cnblogs.com/CF1314/p/14766623.html#_label1_1)
  - [2.3、自定义登录成功处理器](https://www.cnblogs.com/CF1314/p/14766623.html#_label1_2)
  - [2.4、自定义登录失败处理器](https://www.cnblogs.com/CF1314/p/14766623.html#_label1_3)
  - [2.5、自定义异常处理器](https://www.cnblogs.com/CF1314/p/14766623.html#_label1_4)
  - [2.6、配置 Spring Security](https://www.cnblogs.com/CF1314/p/14766623.html#_label1_5)
  - [2.7、运行测试](https://www.cnblogs.com/CF1314/p/14766623.html#_label1_6)
- [3、自定义用户退出登录](https://www.cnblogs.com/CF1314/p/14766623.html#_label2)
  - [3.1、默认的退出登录](https://www.cnblogs.com/CF1314/p/14766623.html#_label2_0)
  - [3.2、自定义退出登录](https://www.cnblogs.com/CF1314/p/14766623.html#_label2_1)
- [4、基于注解的权限控制](https://www.cnblogs.com/CF1314/p/14766623.html#_label3)
  - [4.1、权限注解参数](https://www.cnblogs.com/CF1314/p/14766623.html#_label3_0)
  - [4.2、启动类添加 @EnableGlobalMethodSecurity](https://www.cnblogs.com/CF1314/p/14766623.html#_label3_1)
  - [4.3、运行测试](https://www.cnblogs.com/CF1314/p/14766623.html#_label3_2)
- [5、自定义登录过滤器](https://www.cnblogs.com/CF1314/p/14766623.html#_label4)
  - [5.1、自定义身份认证处理](https://www.cnblogs.com/CF1314/p/14766623.html#_label4_0)
  - [5.2、自定义登录过滤器](https://www.cnblogs.com/CF1314/p/14766623.html#_label4_1)
  - [5.3、修改spring security 的配置类](https://www.cnblogs.com/CF1314/p/14766623.html#_label4_2)
- [6、基于动态权限的权限控制](https://www.cnblogs.com/CF1314/p/14766623.html#_label5)
  - [6.1、自定义权限过滤器](https://www.cnblogs.com/CF1314/p/14766623.html#_label5_0)
  - [6.2、自定义权限决策管理器](https://www.cnblogs.com/CF1314/p/14766623.html#_label5_1)
  - [6.3、修改 sprIng security 的配置类](https://www.cnblogs.com/CF1314/p/14766623.html#_label5_2)
- [7、参考资料](https://www.cnblogs.com/CF1314/p/14766623.html#_label6)

 
------

## Spring Security 入门（基本使用）

这几天看了下b站关于 spring security 的学习视频，不得不说 spring security 有点复杂，脑袋有点懵懵的，在此整理下学习内容。

[回到顶部](https://www.cnblogs.com/CF1314/p/14766623.html#_labelTop)

### 1、入门

个人理解url 的访问流程大致如下：

![spring security 访问url路径图](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291403148.png)


#### 1.1、什么是 spring security

- spring security 是一个比 shiro 更加强大的安全管理框架，权限颗粒度更细。
- 源自于 spring 家族，能跟 springboot 无缝整合，对 oauth2 的支持也更好。


#### 1.2、依赖配置

```undefined
    <parent>
```xml
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.4.5</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.lin</groupId>
    <artifactId>spring-security-study</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>spring-security-study</name>
    <description>Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- spring security -->
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!-- mysql驱动 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <!-- springboot整合mybatis -->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.4</version>
        </dependency>
        <!-- thymeleaf spring security5 依赖 -->
        <dependency>
            <groupId>org.thymeleaf.extras</groupId>
            <artifactId>thymeleaf-extras-springsecurity5</artifactId>
        </dependency>
        <!-- thymeleaf 依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
    </dependencies>
```


```
#### 1.3、测试接口

添加一个简单的 /hello 接口：

```undefined
```java
@RequestMapping("/hello")
@ResponseBody
public String hello() {
    return "恭喜你登录成功";
}
```

```
启动项目，访问 /hello 接口，会发现自动跳转到 spring security 提供的登录页面：

![image-20210513103731379](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291403776.png)

默认的 username 为 ：**user**，password 在项目启动时随机生成，具体如下：

![image-20210513104009820](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404431.png)

登录成功后即可访问 /hello接口。

[回到顶部](https://www.cnblogs.com/CF1314/p/14766623.html#_labelTop)

### 2、自定义登录页面、登录成功处理器、登录失败处理器、异常处理器、权限逻辑

**项目结构**如下：

![image-20210514174013950](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404917.png)


#### 2.1、自定义登录页面

1、登录页面 **login.html** :

```undefined
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>登陆</title>
</head>
<body>
<form method="post" action="/login">
    用户名：<input type="text" name="username123"><br />
    密码：<input type="password" name="password123"><br />
    <button type="submit">立即登陆</button>
</form>
</body>
</html>
```

2、登录成功跳转页 **main.html**

```undefined
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
登录成功！！！
<a href="/main1.html">跳转权限页</a>
</body>
</html>
```

3、登录失败跳转页 **error.html**

```undefined
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
登录失败，请重新登录<a href="/login.html">跳转</a>
</body>
</html>
```

4、权限页 **main1.html**

**main.html **如果有权限，则能访问该页面，否则报 **403**

```undefined
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
权限控制！！！</a>
</body>
</html>
```


#### 2.2、自定义登录逻辑

自定义登录逻辑主要用于对用户名和密码进行校验，**需要实现 UserDetailService 接口**

```undefined
```java
@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=======执行自定义登录逻辑====");
```
        //校验用户名，实际环境中需要从数据库查询
        if (!username.equals("admin")) {
            throw new UsernameNotFoundException("用户不存在");
        }
        //比较密码，实际需要从数据库取出原密码校验，框架会自动读取登录页面的密码
        String password = bCryptPasswordEncoder.encode("123456");
        //返回UserDetails，实际开发中可拓展UserDetails
        return new User(username, password, 
                        //自定义权限
                        AuthorityUtils.commaSeparatedStringToAuthorityList("permission1"));
    }
}
```


#### 2.3、自定义登录成功处理器

登录成功处理器**实现 AuthenticationSuccessHandler 接口**

```undefined
```java
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private String url;

    public MyAuthenticationSuccessHandler(String url) {
        this.url = url;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //获取IP地址
        System.out.println(request.getRemoteAddr());
        //获取认证用户信息
        User user = (User) authentication.getPrincipal();
        System.out.println("=====" + user.getAuthorities());
        //重定向
        response.sendRedirect(url);
    }
}
```


```
#### 2.4、自定义登录失败处理器

登录失败处理器**实现 AuthenticationFailureHandler接口**

```undefined
```java
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private String url;

    public MyAuthenticationFailureHandler(String url) {
        this.url = url;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //重定向
        response.sendRedirect(url);
    }
}
```


```
#### 2.5、自定义异常处理器

```undefined
```java
@Component
public class MyAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        //响应状态403
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        //返回格式
        response.setHeader("Content-Type", "application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
```
        writer.write("{status: \"error\",\"msg\": \"权限不足，请联系管理员\"}");
        writer.flush();
        writer.close();
    }
}
```


#### 2.6、配置 Spring Security

该类是 Spring Security 的配置类, **继承 WebSecurityConfigurerAdapter**

```undefined
```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler;
    
    /**
     * 指定密码加密的方法
     *
     * @return
     */
    @Bean
    public BCryptPasswordEncoder getPasswordEncode() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //表单提交
        http.formLogin()
                //自定义用户名和密码参数
                .usernameParameter("username123")
                .passwordParameter("password123")
                //自定义登录页面
                .loginPage("/showLogin")
```
                //必须和表单提交的接口一样，执行自定义登录逻辑
                .loginProcessingUrl("/login")
                //自定义登录成功处理器
                .successHandler(new MyAuthenticationSuccessHandler("/main.html"))
                //自定义登录失败处理器
                .failureHandler(new MyAuthenticationFailureHandler("/error.html"));
        
        //授权
        http.authorizeRequests()
                //放行/login.html,不需要认证
                .antMatchers("/showLogin").permitAll()
                //放行/error.html，不需要认证
                .antMatchers("/error.html").permitAll()
                //基于权限判断
                .antMatchers("/main1.html").hasAuthority("permission1")
                //所有请求必须认证
                .anyRequest().authenticated();
        
        //异常处理器
        http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);

        //关闭csrf防护
        http.csrf().disable();
    }

    /**
     * 放行静态资源,css,js,images
     * 
     * @param web
     * @throws Exception
     */
```java
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**")
        .antMatchers("/**/*.png");
    }
}
```


```
#### 2.7、运行测试

1、运行后访问 `http://localhost:8080/login.html`，加载的自定义登录页面如下：

注意我在前面的自定义登录逻辑中写死了 **username: admin**和**password:123456**

![image-20210513145444184](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404273.png)

2、点击**立即登陆**按钮，根据登录成功处理器重定向到登录成功页 **main.html**：

![image-20210513145901250](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404676.png)

3、前面的代码中，如果登录成功则拥有**permission1**权限，而访问权限页刚好需要 **permission1** 权限，

点击**跳转权限页**，来到权限页** main1.html**：

![image-20210513150430940](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404421.png)

4、修改登录成功的权限为 **permission2**,

```undefined
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    System.out.println("=======执行自定义登录逻辑====");
```
    //校验用户名，实际环境中需要从数据库查询
    if (!username.equals("admin")) {
        throw new UsernameNotFoundException("用户不存在");
    }
    //比较密码，实际需要从数据库取出原密码校验，框架会自动读取登录页面的密码
    String password = bCryptPasswordEncoder.encode("123456");
    //返回UserDetails，实际开发中可拓展UserDetails
    return new User(username, password,
            //修改权限为permisson2
            AuthorityUtils.commaSeparatedStringToAuthorityList("permission2"));
}
```

再次访问需要 **permission1** 权限的权限页，打印以下错误：

![image-20210513154239657](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404084.png)

5、如果 username 或者 password 错误，根据登录失败处理器重定向到登录失败页 **error.html**:

![image-20210513151019099](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404173.png)

[回到顶部](https://www.cnblogs.com/CF1314/p/14766623.html#_labelTop)

### 3、自定义用户退出登录


#### 3.1、默认的退出登录

spring security 有默认的退出登录接口，直接访问 **/logout** 接口，就能实现退出登录,下面是简单演示：

**main.html** 添加退出登录的访问链接`logout`:

```undefined
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
登录成功！！！
<a href="/logout">退出</a>
<a href="/main1.html">跳转权限页</a>
</body>
</html>
```

直接就能退出了，简不简单呢？默认跳转到登录页：

![image-20210513171702339](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404180.png)

仔细观察，发现访问路径拼接了 **?logout** 字符串，查看源码可以发现默认的配置如下：

![image-20210513172226418](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291404411.png)


#### 3.2、自定义退出登录

如果默认的退出登录无法满足，可以自定义处理器来解决。

##### 3.2.1、自定义 LogoutHandler

默认情况下清除认证信息 （clearAuthentication），和Session 失效（`invalidateHttpSession`） 已经由内置的`SecurityContextLogoutHandler` 来完成。

这个 **LogoutHandle** 主要用来处理用户信息。

```undefined
/**
 * 登出接口处理器
 */
```java
public class MyLogoutHandler implements LogoutHandler {
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        //执行用户信息操作,如记录用户下线时间...
    }
}
```

```
##### 3.2.2、自定义 LogoutSuccessHandler

这个 **LogoutSuccessHandler** 用于返回响应信息给前端，可以返回 json、重定向页面。

注意配置这个处理器之后，就不需要配置 `logoutSuccessUrl`了。

```undefined
/**
 * 登出成功处理器
 */
```java
public class MyLogoutSuccessHandler implements LogoutSuccessHandler {

    private String url;

    public MyLogoutSuccessHandler(String url) {
        this.url = url;
    }
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //重定向
        response.sendRedirect(url);
    }
}
```

```
##### 3.3.3、spring security 添加配置

```undefined
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    //表单提交
    http.formLogin()
        //自定义用户名和密码参数
        .usernameParameter("username123")
        .passwordParameter("password123")
        //自定义登录页面
        .loginPage("/login.html")
```
        //必须和表单提交的接口一样，执行自定义登录逻辑
        .loginProcessingUrl("/login")
        //自定义登录成功处理器
        .successHandler(new MyAuthenticationSuccessHandler("/main.html"))
        //自定义登录失败处理器
        .failureHandler(new MyAuthenticationFailureHandler("/error.html"));
    //授权
    http.authorizeRequests()
        //放行/login.html,不需要认证
        .antMatchers("/login.html").permitAll()
        //放行/error.html，不需要认证
        .antMatchers("/error.html").permitAll()
        //基于权限判断
        .antMatchers("/main1.html").hasAuthority("permission1")
        //所有请求必须认证
        .anyRequest().authenticated();

    //异常处理器
    http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);

    //登出
    http.logout()
        //登出接口,与表单访问接口一致
        .logoutUrl("/signLogout")
        //登出处理器
        .addLogoutHandler(new MyLogoutHandler())
        //登出成功后跳转的页面
        .logoutSuccessHandler(new MyLogoutSuccessHandler("/login.html"));

    //关闭csrf防护
    http.csrf().disable();
}
```

##### 3.3.4、修改登出接口

**main.html** 修改如下：

```undefined
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
登录成功！！！
<a href="/signLogout">退出</a>
<a href="/main1.html">跳转权限页</a>
</body>
</html>
```

运行测试后，返回 `localhost://8080/login.html`

[回到顶部](https://www.cnblogs.com/CF1314/p/14766623.html#_labelTop)

### 4、基于注解的权限控制


#### 4.1、权限注解参数

关于权限的注解参数共有三个：

- @PreAuthorize：方法执行前进行权限检查
- @PostAuthorize：方法执行后进行权限检查
- @Secured：类似于 @PreAuthorize


#### 4.2、启动类添加 @EnableGlobalMethodSecurity

启动类配置如下：

```undefined
```java
@SpringBootApplication
@EnableGlobalMethodSecurity(securedEnabled = true,prePostEnabled = true)
public class SpringSecurityStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityStudyApplication.class, args);
    }
}
```


```
#### 4.3、运行测试

##### 4.3.1、修改 spring security 和 自定义登录逻辑

successHander(登录成功处理器) 修改为 successForwardUrl（登录成功访问路径），删除 **permission1**的权限判断，改成访问接口时进行权限判断。

```undefined
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    //表单提交
    http.formLogin()
        //自定义用户名和密码参数
        .usernameParameter("username123")
        .passwordParameter("password123")
        //自定义登录页面
        .loginPage("/login.html")
```
        //必须和表单提交的接口一样，执行自定义登录逻辑
        .loginProcessingUrl("/login")
        //登录成功跳转的页面，post请求
        .successForwardUrl("/toMain")
        //自定义登录失败处理器
        .failureHandler(new MyAuthenticationFailureHandler("/error.html"));
    //授权
    http.authorizeRequests()
        //放行/login.html,不需要认证
        .antMatchers("/login.html").permitAll()
        //放行/error.html，不需要认证
        .antMatchers("/error.html").permitAll()
        //所有请求必须认证
        .anyRequest().authenticated();

    //异常处理器
    http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);

    //登出
    http.logout()
        //登出接口,与表单访问接口一致
        .logoutUrl("/signLogout")
        //登出处理器
        .addLogoutHandler(new MyLogoutHandler())
        //登出成功后跳转的页面
        .logoutSuccessHandler(new MyLogoutSuccessHandler("/login.html"));

    //关闭csrf防护
    http.csrf().disable();
}
```

自定义登录逻辑如下：

```undefined
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
```
    //校验用户名，实际环境中需要从数据库查询
    if (!username.equals("admin")) {
        throw new UsernameNotFoundException("用户不存在");
    }
    //比较密码，实际需要从数据库取出原密码校验，框架会自动读取登录页面的密码
    String password = bCryptPasswordEncoder.encode("123456");
    //返回UserDetails，实际开发中可拓展UserDetails
    return new User(username, password,
                    //自定义权限
                    AuthorityUtils.commaSeparatedStringToAuthorityList("permission1"));
}
```

##### 4.3.2、添加测试接口

```undefined
//登录成功跳转页
```java
@PostMapping("/toMain")
//判断是否拥有permission1的权限
@PreAuthorize("hasPermission('permission1')")
public String toMain() {
    //获得认证用户信息
    Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (object instanceof UserDetails) {
        //进行一系列操作
    }
    return "redirect:main.html";
}
```

```
##### 4.3.3、运行测试

登录成功，通过 `/toMain`接口重定向到 `main.html`:

![image-20210513232002281](https://gitee.com/xiaoshengstudy/typoraPicture/raw/master/202407291405864.png)

[回到顶部](https://www.cnblogs.com/CF1314/p/14766623.html#_labelTop)

### 5、自定义登录过滤器

当默认的 `UserDetailsService` 无法满足需求，例如增加图形验证码校验，此时可以自己创建登录过滤器 **UsernamePasswordAuthenticationFilter** 及登录认证处理 **AuthenticationProvider**，对业务逻辑进行拓展。


#### 5.1、自定义身份认证处理

实现 **AuthenticationProvider**

```undefined
/**
 * 登录认证处理
 * @author Lin
 */
```java
public class MyAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        //校验账号，实际需要从DB查询
        if (!username.equals("admin")) {
            throw new UsernameNotFoundException("用户不存在");
        }
```
        //校验密码，实际需要加密并与DB的password比较
        if (!password.equals("123456")) {
            throw new InternalAuthenticationServiceException("密码错误");
        }
        return new UsernamePasswordAuthenticationToken(username, password,
                //自定义权限
                AuthorityUtils.commaSeparatedStringToAuthorityList("permission1,ROLE_abc,/main.html"));
    }

```java
    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
```


```
#### 5.2、自定义登录过滤器

实现 **UsernamePasswordAuthenticationFilter**

```undefined
/**
* 登录过滤器
* @author Lin
*/
```java
public class MyUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

   public MyUsernamePasswordAuthenticationFilter() {
       //登录成功处理器
       this.setAuthenticationSuccessHandler(new MyAuthenticationSuccessHandler("/main.html"));
       //登录失败处理器
       this.setAuthenticationFailureHandler(new MyAuthenticationFailureHandler("/error.html"));
   }

   @Override
   public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
       String username = request.getParameter("username123");
       String password = request.getParameter("password123");
       if (Strings.isBlank(username)) {
           throw new AuthenticationServiceException("账户不能为空");
       }
       if (Strings.isBlank(password)) {
           throw new AuthenticationServiceException("密码不能为空");
       }
       UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
       setDetails(request,authenticationToken);
       return new MyAuthenticationProvider().authenticate(authenticationToken);
   }
}
```


```
#### 5.3、修改spring security 的配置类

中间代码忽略，改动部分如下，增加自定义登录过滤器的配置

```undefined
```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    //表单提交
    http.formLogin()
        //自定义用户名和密码参数
        .usernameParameter("username123")
        .passwordParameter("password123")
        //自定义登录页面
        .loginPage("/login.html")
```
        //必须和表单提交的接口一样，执行自定义登录逻辑
        .loginProcessingUrl("/login")
	....
    ....
    //自定义登录过滤器
    http.addFilterBefore(new MyUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
}
```

运行测试后，`debug` 发现账号认证走的自定义登录过滤器 `MyUsernamePasswordAuthenticationFilter`,说明配置成功。

[回到顶部](https://www.cnblogs.com/CF1314/p/14766623.html#_labelTop)

### 6、基于动态权限的权限控制

上文演示了基于注解的权限控制，但在实际的开发中需要做到动态权限控制，spring security能够支持动态权限，需要实现 **FilterInvocationSecurityMetadataSource** 和 **AccessDecisionManager**。


#### 6.1、自定义权限过滤器

权限过滤器实现 **FilterInvocationSecurityMetadataSource**，用于返回访问 url 所需的权限。

注意：在添加权限过滤器后，所有的请求都会经过该过滤器，包括登录页面`/login.html`，即使 `permitAll()`,因此需要添加匿名角色`ROLE_ANONYMOUS`映射登录页面 `login.html`,如下面代码的`put("/login.html", "ROLE_ANONYMOUS")`。

```undefined
/**
 * 权限过滤器
 *
 * @author Lin
 * @Description 返回url需要的权限
 */
```java
@Component
public class MyFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    private static final Logger log = LoggerFactory.getLogger(MyFilterInvocationSecurityMetadataSource.class);

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        //获取请求路径
        String requestUrl = ((FilterInvocation) object).getRequestUrl();
        log.info("==========requestUrl:" + requestUrl);
        // 这里的需要从DB加载
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        Map<String, String> urlRoleMap = new HashMap<String, String>() {{
            //登录页为匿名角色访问
            put("/login.html", "ROLE_ANONYMOUS");
            put("/toMain", "ROLE_USER");
        }};
        List<String> roleList = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, String> entry : urlRoleMap.entrySet()) {
            if (antPathMatcher.match(entry.getKey(), requestUrl)) {
                roleList.add(entry.getValue());
            }
        }
        List<ConfigAttribute> configAttributes = SecurityConfig.createList(roleList.toArray(new String[roleList.size()]));
        return configAttributes;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
```


```
#### 6.2、自定义权限决策管理器

权限决策管理器实现 **AccessDecisionManager**，判断用户是否有访问 url 的权限。

```undefined
/**
 * 权限决策管理器
 * @author Lin
 * @Description 根据url判断用户是否有访问权限
 */
```java
@Component
public class MyAccessDecisionManager implements AccessDecisionManager {
    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while (iterator.hasNext()) {
            String needPermission = iterator.next().getAttribute();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals(needPermission)){
                    return;
                }
            }
        }
        throw new AccessDeniedException("权限不足");
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }
}
```


```
#### 6.3、修改 sprIng security 的配置类

```undefined
```java
@Override
protected void configure(HttpSecurity http) throws Exception {

    //表单提交
    http.formLogin()
        //自定义用户名和密码参数
        .usernameParameter("username123")
        .passwordParameter("password123")
        //自定义登录页面
        .loginPage("/login.html")
```
        //必须和表单提交的接口一样，执行自定义登录逻辑
        .loginProcessingUrl("/login");

    //授权
    http.authorizeRequests()
        //动态权限控制
        .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
```java
            @Override
            public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                object.setSecurityMetadataSource(myFilterInvocationSecurityMetadataSource);
                object.setAccessDecisionManager(myAccessDecisionManager);
                return object;
            }
        })
        //放行/login.html,不需要认证
        .antMatchers("/login.html").permitAll()
```
        //放行/error.html，不需要认证
        .antMatchers("/error.html").permitAll();


    //异常处理器
    http.exceptionHandling().accessDeniedHandler(myAccessDeniedHandler);


    //登出
    http.logout()
        //登出接口,与表单接口一致
        .logoutUrl("/signLogout")
        //登出处理器
        .addLogoutHandler(new MyLogoutHandler())
        //登出成功后跳转的页面
        .logoutSuccessHandler(new MyLogoutSuccessHandler("/login.html"));

    //关闭csrf防护
    http.csrf().disable();

    //自定义登录过滤器
    http.addFilterBefore(new MyUsernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
}
```

这样动态权限控制便配置完成。

[回到顶部](https://www.cnblogs.com/CF1314/p/14766623.html#_labelTop)

### 7、参考资料

> https://www.bilibili.com/video/BV1Cz4y1k7rd?from=search&seid=8886448532131988851
>
> https://blog.csdn.net/zhaoxichen_10/article/details/88713799

自我控制是最强者的本能-萧伯纳

分类: [SpringBoot](https://www.cnblogs.com/CF1314/category/1798711.html), [spring security](https://www.cnblogs.com/CF1314/category/1974770.html)