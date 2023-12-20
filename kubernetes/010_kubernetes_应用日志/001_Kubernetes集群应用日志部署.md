## Kubernetes集群应用日志的部署

日志生产部署最佳实践



KubeSphere为日志收集/查询/管理提供了一个强大的/全面的/易于使用的日志系统, 它涵盖了不同层级的日志,包括租户,基础设置和应用. 用户可以从项目,工作负载,容器组和关键字等不同的维度对日志进行搜索.

与kibana相比, KubeSphere基于租户的日志系统中, 每个租户只能查看自己的日志, 从而可以在租户之间提供更好的隔离性和安全性. 除了KubeSphere自身的日志系统,该容器平台还允许用户添加第三方日志收集器, 比如 ElasticSearch Kafka Fluentd

Fluent Bit

https://fluentbit.io/

https://github.com/fluent/fluent-operator

Flunt Bit 是一个开源的日志处理器和转发器, 允许您从不同的来源收集任何数据, 如指标和日志, 用过滤器过滤日志,并将日志发送到多个目的地





日志收集路径如下:

![image-20230723090347777](assets/001_Kubernetes集群应用日志部署/image-20230723090347777.png)



1 启用日志组件

```bash
kubectl -n kubesphere-system edit cc ks-installer
```

将 logging 配置修改为

如果容器运行环境是 docker

```
 logging:
    containerruntime: containerd
    enabled: true
    logsidecar:
      enabled: true
      replicas: 2
```

如果容器运行环境是 containerd

```bash
 logging:
    containerruntime: containerd
    enabled: true
    logsidecar:
      enabled: true
      replicas: 2
```

enabled设置为 true ,保存退出



查看安装状态信息

```bash
kubectl logs -n kubesphere-system $(kubectl get pod -n kubesphere-system -l 'app in (ks-install, ks-installer)' -o jsonpath='{.items[0].metadata.name}') -f
```



安装完成后可以在KubeSphere平台, 登录之后再页面的右下角的工具箱内点击 `容器日志查询`



