## OpenFeign入门

OpenFeign 在 SpringCloud 项目中的使用



### 1. 接口定义

#### 1.1 引入依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

`lombok` 按需使用，也可以使用idea生成getter/setter

#### 1.2 定义接口

```java
@FeignClient(value = "gulimall-coupon", contextId = "memberprice", path = "/coupon/memberprice")
public interface MemberPriceApi {

    /**
     * 列表
     */
    @GetMapping("/list")
    //@RequiresPermissions("coupon:memberprice:list")
    R list(@RequestParam Map<String, Object> params);


    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    //@RequiresPermissions("coupon:memberprice:info")
    R info(@PathVariable("id") Long id);

    /**
     * 保存
     */
    @PostMapping("/save")
    //@RequiresPermissions("coupon:memberprice:save")
    R save(@RequestBody MemberPriceDto memberPrice);

    /**
     * 保存
     */
    @PostMapping("/save-list")
    //@RequiresPermissions("coupon:memberprice:save")
    R saveList(@RequestBody List<MemberPriceDto> memberPriceList);

    /**
     * 修改
     */
    @PutMapping("/update")
    //@RequiresPermissions("coupon:memberprice:update")
    R update(@RequestBody MemberPriceDto memberPrice);

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    //@RequiresPermissions("coupon:memberprice:delete")
    R delete(@RequestBody Long[] ids);

}
```

`@FeignClient(value = "gulimall-coupon", contextId = "memberprice", path = "/coupon/memberprice")`

value: 是服务id，即在注册中心的application-name

contextId:  接口名称，如果不定义contextId，同一个application下的多个接口会冲突

path: 请求路径，类似于 @RequestMapping ,定义接口不同EndPoint的共同的起始路径。

注意： **path参数必须以“/”开始**，且与服务端的@RequestMapping保持一致，返回值类型也要注意是返回数据类型保持一致，是 `Entity`还是统一的包装类(`Result<Entity>`/`Result<List<Entity>>`/`Result<Page<Entity>>`)

#### 1.3 定义dto

```java
@Data
public class MemberPriceDto {

    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * 会员等级id
     */
    private Long memberLevelId;
    /**
     * 会员等级名
     */
    private String memberLevelName;
    /**
     * 会员对应价格
     */
    private BigDecimal memberPrice;
    /**
     * 可否叠加其他优惠[0-不可叠加优惠，1-可叠加]
     */
    private Integer addOther;
}
```

定义接口中使用到的实例的封装



### 2. 服务端-接口实现

#### 2.1 引入依赖

```xml
<dependencies>
        
    <dependency>
        <groupId>com.xingray</groupId>
        <artifactId>gulimall-coupon-api</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

</dependencies>
```

#### 2.2 Controller实现接口

```java
@RestController
@RequestMapping("coupon/memberprice")
public class MemberPriceController {
    @Autowired
    private MemberPriceService memberPriceService;

    /**
     * 列表
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberPriceService.queryPage(params);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberPriceEntity memberPrice = memberPriceService.getById(id);
        return R.ok().put("memberPrice", memberPrice);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody MemberPriceDto memberPrice){
		memberPriceService.save(memberPrice);
        return R.ok();
    }

    /**
     * 保存
     */
    @PostMapping("/save-list")
    R saveList(@RequestBody List<MemberPriceDto> memberPriceList){
        memberPriceService.saveList(memberPriceList);
        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public R update(@RequestBody MemberPriceDto memberPrice){
		memberPriceService.updateById(memberPrice);
        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberPriceService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }

}
```

这里可以考虑用Controller继承api，也可以不继承。使用的entity可以使api中定义的dto，也可以在服务端单独实现，只要json数据兼容即可。



### 3. 客户端-接口调用

#### 3.1 引入依赖

```xml
<dependencies>
    
    <dependency>
        <groupId>com.xingray</groupId>
        <artifactId>gulimall-coupon-api</artifactId>
    </dependency>
    
    <!--
	api模块已经引入了
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
	-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <optional>true</optional>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

</dependencies>
```



#### 3.2 启动类上添加注解

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {MemberPriceApi.class})
public class ProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}
```

如果需要引入多个接口，可以如下设置：

```java
@EnableFeignClients(clients = {SpuBoundsApi.class, SkuLadderApi.class, SkuFullReductionApi.class, MemberPriceApi.class})
```

#### 3.3 在Service中调用

```java
@Service
public class SpuServiceImpl implements SpuService {

    private final EntityConverter entityConverter;
    private final MemberPriceApi memberPriceApi;
    
    public SpuServiceImpl(EntityConverter entityConverter, MemberPriceApi memberPriceApi) {
        this.entityConverter = entityConverter;
        this.memberPriceApi = memberPriceApi;
    }
 
    public void saveSku(Long spuId, Long categoryId, Long brandId, Sku sku) {
		// ...
        List<MemberPrice> memberPriceList = sku.getMemberPrice();
        if (CollectionUtil.hasElement(memberPriceList)) {
            List<MemberPriceDto> memberPriceDtoList = entityConverter.memberPriceListToMemberPriceDtoList(skuId, memberPriceList);
            memberPriceApi.saveList(memberPriceDtoList);
        }
    }
}
    
```



#### 3.4 配置

openFeign支持自定义配置覆盖默认配置，常用的修改配置如下：

| 类型         | 作用           | 说明                                           |
| ------------ | -------------- | ---------------------------------------------- |
| logger-level | 修改日志级别   | 包含四种不同级别：NONE,BASIC,HEADERS,FULL      |
| decoder      | 响应结果解析器 | http远程调用的结果做解析，例如JSON转为Java对象 |
| encoder      | 请求参数编码   | 将请求参数编码，便于通过htp发送请求            |
| contract     | 支持的注解格式 | 默认是SpringMVC注解                            |
| retryer      | 失败重试机制   | 请求失败的重试机制                             |

完整配置参考 org.springframework.cloud.openfeign.FeignClientProperties.FeignClientConfiguration



一般只需要配置日志级别即可，记录日志会消耗一定的性能，一般推荐使用NONE或者BASIC。

| 日志级别 | 说明                                      |
| -------- | ----------------------------------------- |
| None     | 没有任何日志（默认）                      |
| BASIC    | 仅记录请求方法、URL、响应状态码及执行时间 |
| HEADERS  | 在BASIC的基础上加上请求和响应头信息       |
| FULL     | 在HEADERS基础上加上请求体和响应体及元数据 |



修改日志配置的方式：

要显示feign的日志，首先需要在配置文件中调整指定包或者openFeign的接口日志级别，如下:

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            logger-level: NONE
          gulimall-coupon: 
            logger-level: FULL
```

default：所有调用统一配置

gulimall-coupon: 对`gulimall-coupon`服务的所有接口的配置



#### 4.2 基于代码修改配置

##### 4.2.1 全局生效

```java
@Configuration
public class OpenFeignConfig {
    @Bean
    public Logger.Level loggingLevel() {
        return Logger.Level.BASIC;
    }
}
```

或者：

```java
public class OpenFeignConfig {
    @Bean
    public Logger.Level loggingLevel() {
        return Logger.Level.BASIC;
    }
}
```

```java
@SpringBootApplication
@EnableFeignClients(defaultConfiguration = OpenFeignConfig.class)
@EnableDiscoveryClient
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```



##### 4.2.2 针对服务生效

```java
public class FeignClientConfig {
    @Bean
    Logger.Level logLevel() {
        return Logger.Level.FULL;
    }
}
```

```java
@FeignClient(value = "user", path = "/user", configuration = FeignClientConfig.class)
public interface UserClient {
    @GetMapping("/{id}")
    Result<User> findById(@PathVariable("id") Long id);
}
```



### 5. 性能优化

主要是关闭日志和替换底层客户端

#### 5.1 替换底层客户端实现

feign的底层客户端实现有

| 客户端            | 说明                   |
| ----------------- | ---------------------- |
| URLConnection     | 默认实现，不支持连接池 |
| Apache HttpClient | 支持连接池             |
| OKHttp            | 支持连接池             |

##### 5.1.1客户端替换为ApacheHttpClient

###### 5.1.1.1 引入依赖

```xml
<dependency>
	<groupId>io.github.openfeign</groupId>
	<artifactId>feign-httpclient</artifactId>
</dependency>
```

###### 5.1.1.2 开启客户端配置

```yaml
spring:
  cloud:
    openfeign:
      httpclient:
        enabled: true
        max-connections: 200
        max-connections-per-route: 50
```

max-connections 连接池中最大连接数

max-connections-per-route： 单个路径的最大连接数，一般需要根据jmeter等工具进行压力测试得到合适的数值。



##### 5.1.2 客户端替换为okhttp

```xml
<dependency>
	<groupId>io.github.openfeign</groupId>
	<artifactId>feign-okhttp</artifactId>
</dependency>
```

```yaml
spring:
  cloud:
    openfeign:
      okhttp:
        enabled: true
```
