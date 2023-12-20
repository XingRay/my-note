# SpringCloud + K8s 最佳实战



SpringCloud和K8s在核心功能上有很大一部分交集,如 K8s 中service提供的服务注册/负载均衡 configmap 和 secret 提供了配置管理相关功能,  还可以使用 `ingress controller` 作为网关, 这些功能组件在 SpringCloud项目中也有很多实际应用



下面使用k8s作为springcloud的注册中心/配置中心,用以简化微服务应用的整体架构,更好的与云原生环境融合



spring-cloud-kubernetes

spring-cloud-kubernetes 是Spring Cloud 社区为k8s环境提供的开箱即用的 服务发现 / 配置分发 方案. 该项目实现了SpringCloud中的几个核心接口,允许开发者在Kubernetes上构建和运行SpringCloud应用 

https://github.com/spring-cloud/spring-cloud-kubernetes

https://spring.io/projects/spring-cloud-kubernetes

https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/



1 starter

fabric8 版本:

Starters that begin with `spring-cloud-starter-kubernetes-fabric8` provide implementations using the [Fabric8 Kubernetes Java Client](https://github.com/fabric8io/kubernetes-client). 

官方版本:

Starters that begin with `spring-cloud-starter-kubernetes-client` provide implementations using the [Kubernetes Java Client](https://github.com/kubernetes-client/java).



https://github.com/fabric8io/kubernetes-client

Java client for Kubernetes & OpenShift

OpenShift是红帽(Red Hat)的云开发平台即服务PaaS





https://github.com/kubernetes-client/java

Official Java client library for kubernetes



服务发现:

[Discovery Client](https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/#discoveryclient-for-kubernetes) implementation that resolves service names to Kubernetes Services.

Fabric8 Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-fabric8</artifactId>
</dependency>
```

Kubernetes Client Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-client</artifactId>
</dependency>
```



配置加载 :

Load application properties from Kubernetes [ConfigMaps](https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/#configmap-propertysource) and [Secrets](https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/#secrets-propertysource). [Reload](https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/#propertysource-reload) application properties when a ConfigMap or Secret changes.

Fabric8 Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-fabric8-config</artifactId>
</dependency>
```

Kubernetes Client Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-client-config</artifactId>
</dependency>
```



全功能 :

All Spring Cloud Kubernetes features.

Fabric8 Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-fabric8-all</artifactId>
</dependency>
```

Kubernetes Client Dependency

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-client-all</artifactId>
</dependency>
```



下面演示项目中均使用 fabric8 客户端实现版本





服务发现

spring-cloud-starter-kubernetes-fabric8 项目为Kubernetes提供了客户端服务发现的实现, 可以从客户端按名称查询 Kubernetes 中的 service 关联的 endpoint ,客户端如果运行在k8s集群中则可以直接访问者些 endpoint ,还可以在此之上实现负载均衡

可以使用指令查看k8s中service具体关联的endpoint

```bash
kubectl get services
kubectl get endpoint
```

k8s通过service提供了服务发现(server side)的能力, 参考 



