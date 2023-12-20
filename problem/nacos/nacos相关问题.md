### 1. 启动微服务报错

现象：

nacos部署在局域网内的服务器上，在idea中启动微服务时,部分微服务报错：

```bash
Connection refused: no further information: /127.0.0.1:9848
```



报错的微服务的配置文件 bootstrap.yml:

```yaml
spring:
  application:
    name: demo-service

  profiles:
    active: dev
  cloud:
    nacos:
      discovery:
        service: ${spring.application.name}
        username: nacos
        password: nacos
        server-addr: 192.168.0.100:8848
      config:
        file-extension: yaml
```



报错原因：

将nacos作为配置中心没有配置服务器信息，



解决方案：

将配置文件修改：

```yaml
spring:
  application:
    name: demo-service

  profiles:
    active: dev
  cloud:
    nacos:
      discovery:
        service: ${spring.application.name}
        username: nacos
        password: nacos
        server-addr: 192.168.0.100:8848
      config:
        file-extension: yaml
        username: nacos
        password: nacos
        server-addr: 192.168.0.100:8848
```

