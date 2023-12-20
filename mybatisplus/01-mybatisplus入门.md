## mybatis-plus入门



### 1. 引入依赖

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.3.1</version>
</dependency>

<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```



### 2. 配置

application.yml:

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 123456
    url: jdbc:mysql://localhost:3306/demo?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
```

map-underscore-to-camel-case： 下划线（数据库中）转为驼峰命名（java对象字段）

id-type: auto 数据库自动生成id，不配置此项mybatis-plus会自动生成一个数值很大的id



### 3. 对象模型

```java
@Data
@TableName("t_user")
public class UserDto {
    private String id;
    private String username;
    private String password;
    private String fullName;
    private String phoneNumber;
}
```

通过 @TableName 注解声明数据库表的名称



### 4. Mapper

```java
@Mapper
public interface UserMapper extends BaseMapper<UserDto> {
    @Select("""
            select * from t_permission where id in(
                select permission_id from t_role_permission where role_id in (
                    select role_id from t_user_role where user_id = #{userId}
                )
            )
            """)
    List<PermissionDto> findPermissionByUserId(@Param("userId") String userId);
}
```

Mapper需要添加 `@Mapper` 注解

BaseMapper中提供了一系列CRUD方法，可以直接调用

自定义方法可以通过注解实现，如 `@Select` ,参数在sql中通过 #{param_name} 声明，在方法参数中通过 `@Param("param_name")`  传入



### 5. Service

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDto> implements UserService {
    @Override
    public UserDto findUserByUsername(String username) {
        LambdaQueryWrapper<UserDto> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDto::getUsername, username);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<PermissionDto> findPermissionByUserId(String userId) {
        return getBaseMapper().findPermissionByUserId(userId);
    }
}
```

service需要添加 `@Service` 注解， 继承 ServiceImpl<Mapper, Entity> 

可以通过继承的方法实现基本的CRUD

可以通过 getBaseMapper() 获得Mapper对象调用自定义方法

