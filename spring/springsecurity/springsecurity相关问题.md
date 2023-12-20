退出后再登录  BadCredentialsException: 用户名或密码错误

问题：学习springsecurity使用过程中写了一个demo，其中使用了如下类做测试：

```java
public class UserDetailServiceInMemory implements UserDetailsService {

    private final Map<String, UserDetails> userDetailsMap;

    public UserDetailServiceInMemory() {
        userDetailsMap = new HashMap<>();
        addUser(User.withUsername("aaa").password("123").authorities("p1").build());
        addUser(User.withUsername("bbb").password("123").authorities("p2").build());
        addUser(User.withUsername("ccc").password("123").authorities("p1").build());
        addUser(User.withUsername("ddd").password("123").authorities("p2").build());
    }

    private void addUser(UserDetails userDetails) {
        userDetailsMap.put(userDetails.getUsername(), userDetails);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDetailsMap.get(username);
    }
}
```





用于简单测试登录功能，但是发现使用同一账号密码登录后退出再登录会报错，浮现步骤如下

1. 使用账号密码登录 aaa 123 ，登录成功
2. 调用logout ，可以在页面做一个按钮，通过post /logout 调用，成功退出
3. 再使用账号密码登录 aaa 123 ，登录失败，提示  BadCredentialsException: 用户名或密码错误



原因：用于测试的UserDetailServiceInMemory对象持有Map持有UserDetails对象，

loadUserByUsername对象对外返回UserDetails对象后，会在springsecurity框架内将UserDetailServiceInMemory#loadUserByUsername返回的UserDetails#password设置为null，也就是将UserDetailServiceInMemory中的UserDetails对象的password置为null，下次一再登录是，会把提交的password与null进行对比，所以会报错： BadCredentialsException: 用户名或密码错误





解决方案：

测试代码可以改为如下：

添加dto对象，模拟数据库中的数据

```java
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    private String username;
    private String password;
    private List<String> authorities;
}
```



修改UserDetailServiceInMemory实现：

```java
public class UserDetailServiceInMemory implements UserDetailsService {

    public static final String ENCODED_PASSWORD = "$2a$10$CRF52qI.8GtJ6W8vVFJp4uDSWZl8fu4gVRpKVF3NGmQp5fIGnYpYi";
    public static final String RAW_PASSWORD = "123";
    private static final String PASSWORD = ENCODED_PASSWORD;

    private final Map<String, UserDto> userDetailsMap;

    public UserDetailServiceInMemory() {
        userDetailsMap = new HashMap<>();

        addUser("aaa", PASSWORD, "p1");
        addUser("bbb", PASSWORD, "p2");
        addUser("ccc", PASSWORD, "p1");
        addUser("ddd", PASSWORD, "p2");
    }

    private void addUser(String username, String password, String authority) {
        List<String> authorities = new ArrayList<>();
        authorities.add(authority);
        userDetailsMap.put(username, new UserDto(username, password, authorities));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto userDto = userDetailsMap.get(username);
        if (userDto == null) {
            return null;
        }

        return User.withUsername(userDto.getUsername())
                .password(userDto.getPassword())
                .authorities(userDto.getAuthorities().toArray(new String[0]))
                .build();
    }
}

```

















SpringSecurity+oauth2 配置完成进行测试

1. http://127.0.0.1:53020/uaa/oauth/authorize?client_id=c1&response_type=code&scope=all&redirect_uri=http://www.baidu.com 

   调用/oauth/authorize申请授权码

2. 通过 http://127.0.0.1:53020/uaa/oauth/token 接口获取token，使用apifox/postman进行测试

结果在第二步时返回401，开发者账号密码均配置正确

Access is denied (user is anonymous); redirecting to authentication entry point

原因：apifox/postman测试改接口时，需要设置authorization为Basic auth，并且在auth页面输入开发者账号及密码。

![](E:\myNote\java\springsecurity\resources\Snipaste_2022-04-07_00-35-56.png)



3Basic auth基本授权认证
Basic auth基本授权认证随请求一起发送经过验证的用户名和密码。在请求“Authorization ”选项卡中，从“TYPE”下拉列表中选择“Basic auth”。
在“ Username”和“ Password”字段中输入您的API登录信息，也采用变量引用方式，便于管理和安全保护。

![](E:\myNote\java\springsecurity\resources\20200528120415512.png)

在请求头中，您将看到Authorization标头将向API传递一个代表您的用户名和密码值的Base64编码的字符串，该字符串附加到文本“ Basic”中，如下所示：

![](E:\myNote\java\springsecurity\resources\001.jpg)









springboot  @EnableGlobalMethodSecurity

SpringBoor+SpringSecurity项目，配置类如下：

```java
@Configuration
@EnableGlobalMethodSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests()
//                .antMatchers("/r/r1").hasAuthority("p1")
//                .antMatchers("/r/r2").hasAuthority("p2")
                .antMatchers("/r/**").authenticated()
                .anyRequest().permitAll();
    }
}
```

启动server时报错：

Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'metaDataSourceAdvisor': Cannot resolve reference to bean 'methodSecurityMetadataSource' while setting constructor argument; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'methodSecurityMetadataSource' defined in class path resource [org/springframework/security/config/annotation/method/configuration/GlobalMethodSecurityConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.security.access.method.MethodSecurityMetadataSource]: Factory method 'methodSecurityMetadataSource' threw exception; nested exception is java.lang.IllegalStateException: In the composition of all global method configuration, no annotation support was actually activated



原因：In the composition of all global method configuration, no annotation support was actually activated

配置了EnableGlobalMethodSecurity，但是都没有启用，至少需要启用一个配置。修改如下：

```java
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();

        http.authorizeRequests()
//                .antMatchers("/r/r1").hasAuthority("p1")
//                .antMatchers("/r/r2").hasAuthority("p2")
                .antMatchers("/r/**").authenticated()
                .anyRequest().permitAll();
    }
}
```

在注解@EnableGlobalMethodSecurity后加上参数(securedEnabled = true, prePostEnabled = true)即可。



