## nacos入门



### 1. 下载安装

https://github.com/alibaba/nacos/releases

### 2. 启动和停止

启动：

win: \nacos\bin\startup.cmd

linux: \nacos\bin\startup.sh

停止：

win: \nacos\bin\shutdown.cmd

linux: \nacos\bin\shutdown.sh



### 3. 引入依赖

父工程 pom.xml

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.0.2</version>
</parent>

<properties>
	<springcloud.version>2022.0.2</springcloud.version>
	<springcloud.alibaba.version>2022.0.0.0-RC1</springcloud.alibaba.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${springcloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>${springcloud.alibaba.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
    
```

- springboot/springcloud/springcloud-alibaba版本选择参考：https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E



子工程 pom.xml

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
</dependencies>
```

### 4. 客户端配置

```yaml
spring:
  application:
    name: service-name
  cloud:
    nacos:
      discovery:
        service: ${spring.application.name}
        username: nacos
        password: nacos
        server-addr: localhost:8848
        cluster-name: cluster-name
        # 是否是临时实例
        ephemeral: true
```
- 临时实例注册到nacos后再改为非临时实例后启动服务将报错，无法注册到nacos
  errCode: 400, errMsg: Current service DEFAULT_GROUP@@order is ephemeral service, can't register persistent instance. ;
  
  原因：以前是临时实例的实例直接修改为非临时实例，ip和端口都和以前一样，再向nacos注册时会失败，nacos不允许临时服务直接注册为非临时实例。
  
  解决方案：关闭nacos和服务，清空nacos文件目录\nacos\data\protocol\raft，该目录存储了服务的元信息。然后再重启nacos和服务即可。

在启动类上添加相应的注解：

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```



### 5. 远程调用

注意：远程调用需要依赖 spring-cloud-starter-loadbalancer ，loadbalancer可以将服务名解析为具体的地址。



#### 5.1 配置RestTemplate

```java
package com.xingray.springcloud.order.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.loadbalancer.NacosLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class RestClientConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

//    @Bean
//    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment environment, LoadBalancerClientFactory factory) {
//        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
//        log.info("randomLoadBalancer name:{}", name);
//        return new RandomLoadBalancer(factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
//    }

//    @Bean
//    public ReactorLoadBalancer<ServiceInstance> roundRobinLoadBalancer(Environment environment, LoadBalancerClientFactory factory){
//        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
//        log.info("roundRobinLoadBalancer name:{}", name);
//        return new RoundRobinLoadBalancer(factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
//    }

    @Bean
    public ReactorLoadBalancer<ServiceInstance> nacosLoadBalancer(Environment environment, LoadBalancerClientFactory factory) {
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        log.info("randomLoadBalancer name:{}", name);
        NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
        return new NacosLoadBalancer(factory.getLazyProvider(name, ServiceInstanceListSupplier.class), name, properties);
    }
}
```

可根据需要选择loadbalancer

使用自定义负载均衡策略需要再启动类上添加相应的注解：

```java
@SpringBootApplication
@EnableDiscoveryClient
@LoadBalancerClients({@LoadBalancerClient(
        value = "user",
        configuration = RestClientConfig.class
)})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```



#### 5.2 发起远程调用

```java
User user = restTemplate.getForObject("http://user-service/user/" + order.getUserId(), User.class);
```

### 6. 负载均衡

#### 6.1 配置


#### 6.2 服务实例权重设置
进入nacos控制台-服务列表，找到服务列表中的服务，点击详情，进入服务详情页，找到集群信息中的实例列表，点击编辑，在弹窗中修改权重属性。
作用：负载均衡算法会根据权重来分配请求的数量，可以根据机器的性能配置不同的权重。权重为0时将不会访问该服务，可用于服务更新时让小部分服务逐步更新测试，进行灰度发布。

### 7. 环境隔离
一般环境可以分为开发环境develop，测试环境test，生产环境product。不同环境的服务和数据是不能相互访问的。
#### 7.1 创建环境
nacos默认创建public环境， 在nacos控制台-命名空间页中可以进行管理，点击新建命名空间，输入命名空间名(如develop/test/product)和描述即可，id会自动生成uuid。
#### 7.2 设置服务实例所属环境
spring.cloud.nacos.discovery.namespace=d8043d03-778f-4b6a-9a37-6891c8e17792
环境的命名空间id在nacos控制台-命名空间页面的列表中可以找到。



