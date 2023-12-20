## SpringBoot接入Https



### 1 证书

#### 自签名

使用jdk自带的工具

```bash
keytool -genkeypair -alias demo-https -keyalg RSA  -keysize 2048 -validity 365  -keystore ./demo-keystore.keystore -storepass storepass123
```

jdk8以上默认密钥库文件格式为 PKCS12



#### 服务商申请

阿里云、腾讯云



### 2 配置

```yaml
server:
  # 默认443，也可以设置为其他的
  port: 443
  ssl:
	#开启https
    enabled: true
    
    #指定存放证书的密钥库文件的位置
    key-store: classpath:demo-keystore.keystore
    
    #密钥库文件的格式
    key-store-type: PKCS12
    
    #别名，需要与创建密钥库时的别名一致
    key-alias: demo-https
    
    #密钥库密码
    key-store-password: storepass123
```

这样就可以使用

https://localhost/api/hello/hello

访问测试端口，测试代码略



### 3 http/https同时支持

添加设置

```java
@Configuration
public class WebmvcConfig {
    @Value("${http.port}")
    private Integer httpPort;

    @Bean
    public ServletWebServerFactory servletContainer(){
        final Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setPort(httpPort);
        final TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(connector);
        return tomcat;
    }
}
```

