# 云原生架构之SpringCloudKubernetes 服务注册发现方案(东西流量)

[kaliarch](https://juejin.cn/user/976022055951582/posts)

2022-09-03 10:341795

“我报名参加金石计划1期挑战——瓜分10万奖池，这是我的第4篇文章，[点击查看活动详情](https://s.juejin.cn/ds/jooSN7t)”

## 一 方案概述

通常将spring cloud应用上K8s集群，使用spring cloud kubernetes，服务注册依旧为使用k8s服务注册，服务发现利用其discover可以通过K8s api发现一个服务后的一组实例，负载均衡使用spring cloud kubernetes ribbon实现（改方案负责均衡为客户端负载均衡）。

## 二 SpringBoot + K8s service服务注册发现方案

### 2.1 方案简介

在k8s中部署spring cloud 项目，可采用spring cloud kubernetes完成服务发现、动态配置，使用spring cloud openFeign完成服务间的通信，spring cloud ribbon实现负载均衡.采用如上几个模块，可以在k8s的环境下，实现重试，超时，限流，负载均衡等常用功能。

![img](assets/003/0e7311daa0314ec7a1a836cf44475b47tplv-k3u1fbpfcp-zoom-in-crop-mark4536000.webp)

简单来讲，该模块主要封装了跟api Servier的http交互，方便项目中对api Server的请求和读取k8s中的 服务注册发现`Services`/`Endpoints`和配置中心的`ConfigMaps` 和 `Secrets` 。

例如获取服务列表或服务的实例信息

![img](assets/003/22b0ad92680a44bbbaa2270b187d0addtplv-k3u1fbpfcp-zoom-in-crop-mark4536000.webp)

### 2.2 服务注册发现实现

#### 2.2.1 服务注册过程：

spring cloud 服务配置有serivce的服务，启动后k8s集群针对调用该service，后端会返回具体的pod列表。

#### 2.2.2 服务发现过程：

通过 spring cloud kubernetes的服务发现，调用k8s api获取pod实例的ip和端口，从而发现轮训调用。

#### 2.3 方案特点

- 优点：
  - 服务直接通过k8s服务发现，获取service后的实例POD IP和端口，流量直接对POD发起，不经过service转发，性能好。
- 不足：
  - 负责均衡算法在服务调用端（客户端）实现，例如ribbon实现，如果后期使用服务治理框架例如istio/linkerd不适用。

## 三 实战

### 3.1 服务提供者

#### 3.1.1 提供者逻辑

springboot-init 为服务提供者，对外提供 /services 用于获取 K8s 名称空间的名称，及返回相应 POD 的hostname。服务注册，仅需为服务编写 service yaml 资源清单即可。

#### 3.1.2 服务发现

通过引入 spring-cloud-starter-kubernetes-client-all 进行服务发现

```yaml
yaml复制代码<dependency>
<groupId>org.springframework.cloud</groupId>
<artifactId>spring-cloud-starter-kubernetes-client-all</artifactId>
<version>2.1.3</version>
</dependency>
```

#### 3.1.3 项目目录结构

```shell
shell复制代码├── Dockerfile					// Dockerfile
├── HELP.md
├── README.md
├── deploy						// 部署文件
│   └── deployment.yaml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── springboot-init.iml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── xuel
│   │   │           └── springbootinit
│   │   │               ├── DiscoveryService.java				// 服务发现，提供接口
│   │   │               └── SpringbootInitApplication.java		
│   │   └── resources
│   │       ├── application.yml									// 配置文件
│   │       ├── static
│   │       └── templates
```

#### 3.1.4 k8s部署文件

K8s 部署文件中仅需deploy 和 svc即可，注意在deploy 中切记配置 `readinessProbe`，如果服务提供者启动多副本，服务消费端获取到endpoints中的pod，才发起请求。

```yaml
yaml复制代码apiVersion: apps/v1
kind: Deployment
metadata:
  namespace: spring
  name: springboot-init
  labels:
    app: springboot-init
spec:
  replicas: 3
  selector:
    matchLabels:
      app: springboot-init
  template:
    metadata:
      labels:
        app: springboot-init
    spec:
      containers:
        - name: springboot-init
          image: ccr.ccs.tencentyun.com/xxxxxxxxx-dev/springbootinit:v5
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
              protocol: TCP
          livenessProbe:
            httpGet:
              port: 8080
              path: /health
            periodSeconds: 10
            initialDelaySeconds: 3
            terminationGracePeriodSeconds: 10
            failureThreshold: 5
            timeoutSeconds: 10
          readinessProbe:
            httpGet:
              port: 8080
              path: /health
            initialDelaySeconds: 5
            periodSeconds: 10
            failureThreshold: 5
            timeoutSeconds: 10

---
apiVersion: v1
kind: Service
metadata:
  name: springbootinit
  namespace: spring
  labels:
    app: springboot-init
spec:
  ports:
    - port: 8080
      protocol: TCP
      targetPort: 8080
  type: ClusterIP
  selector:
    app: springboot-init
```

#### 3.1.5 项目源码

项目位置：[github.com/redhatxl/cl…](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2Fredhatxl%2Fcloudnative-java)

### 3.2 服务消费端

#### 3.2.1 消费者逻辑

服务注册，仅需为服务编写 service yaml 资源清单即可。 springboot-client 为使用 k8s service 进行服务发现 endpoints 列表，通过 ribbon 进行服务发起(客户端)进行负载均衡。

#### 3.2.2 服务发现

通过引入 spring-cloud-starter-kubernetes-client-all 进行服务发现； 通过引入 spring-cloud-loadbalancer 进行客户端负载均衡； 通过引入 spring-cloud-starter-openfeign 进行客户端http 请求发起。

```yaml
yaml复制代码<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
    <version>3.1.3</version>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-loadbalancer</artifactId>
    <version>3.1.3</version>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-client-all</artifactId>
    <version>2.1.3</version>
</dependency>
```

#### 3.2.3 项目目录结构

```shell
shell复制代码├── Dockerfile
├── HELP.md
├── README.md
├── deploy
│   └── deploy.yaml
├── mvnw
├── mvnw.cmd
├── pom.xml
├── springboot-client.iml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── xuel
│   │   │           ├── controller								// controller
│   │   │           │   └── FeignDemoController.java
│   │   │           ├── feign
│   │   │           │   └── ServiceDemoFeign.java			   // 定义远程调用feign接口
│   │   │           ├── service									// service 
│   │   │           │   ├── FeignDemoService.java
│   │   │           │   └── impl
│   │   │           │       └── FeignDemoServiceImpl.java	   // service 实现
│   │   │           └── springbootclient
│   │   │               └── SpringbootClientApplication.java   // 启动类
│   │   └── resources
│   │       ├── application.properties
│   │       ├── static
│   │       └── templates
```

#### 3.2.4 k8s部署文件

参考 3.1.4 基本一致。

#### 3.2.5 项目源码

项目位置：[github.com/redhatxl/cl…](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2Fredhatxl%2Fcloudnative-java)

## 四 测试

#### 4.1 访问服务提供者

由于本地mac 使用kind部署，通过port-forward进行映射到宿主机。

```shell
shell
复制代码k port-forward -n spring --address 0.0.0.0 svc/springbootinit 9999:8080
```

`/services`: 获取到对的service 列表，当前的时间，相应的 Pod 主机名称。

![img](assets/003/6d57a89dadef49fa890e6d067900a094tplv-k3u1fbpfcp-zoom-in-crop-mark4536000.webp)

#### 4.2 访问服务消费者

```shell
shell
复制代码k port-forward -n spring --address 0.0.0.0 svc/springbootclient 8888:8080
```

`/clientsvc` : 获取 K8s 内部列表，即请求到的 POD hostname。

![img](assets/003/382d6f1dc4804ba08cbe10f591496fcbtplv-k3u1fbpfcp-zoom-in-crop-mark4536000.webp)

![img](assets/003/c492810f1e6e427c8cd0f0cba2e6ba4ftplv-k3u1fbpfcp-zoom-in-crop-mark4536000.webp)

连续刷新，可以看到负载均衡效果。

#### 4.3 查看资源

![img](assets/003/b9b3c17e84774ae3a5a66247a49bf9d0tplv-k3u1fbpfcp-zoom-in-crop-mark4536000.webp)

## 五 其他

总结下来，服务注册通过给服务添加service，服务发现通过springcloudkubernetes 的 discover进行发现endpoints 中pod，通过feign发起调用，通过ribbon进行负责均衡。

## 参考链接

- [spring.io/projects/sp…](https://link.juejin.cn/?target=https%3A%2F%2Fspring.io%2Fprojects%2Fspring-cloud-kubernetes)
- [github.com/spring-clou…](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2Fspring-cloud%2Fspring-cloud-kubernetes)
- [blog.csdn.net/boling_cava…](https://link.juejin.cn/?target=https%3A%2F%2Fblog.csdn.net%2Fboling_cavalry%2Farticle%2Fdetails%2F91346780)
- [zhuanlan.zhihu.com/p/358817098](https://link.juejin.cn/?target=https%3A%2F%2Fzhuanlan.zhihu.com%2Fp%2F358817098)