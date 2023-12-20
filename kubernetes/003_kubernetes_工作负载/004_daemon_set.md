# kubernetes DaemonSet

k8s集群的每个机器(每一个节点)都运行一个程序（默认master除外，master节点默认不会把Pod调度过去）

无需指定副本数量；因为默认给每个机器都部署一个（master除外） 



DaemonSet 控制器确保所有（或一部分）的节点都运行了一个指定的 Pod 副本。

每当向集群中添加一个节点时，指定的 Pod 副本也将添加到该节点上

当节点从集群中移除时，Pod 也就被垃圾回收了

删除一个 DaemonSet 可以清理所有由其创建的 Pod 



DaemonSet 的典型使用场景有：

在每个节点上运行集群的存储守护进程，例如 glusterd、ceph

在每个节点上运行日志收集守护进程，例如 fluentd、logstash

在每个节点上运行监控守护进程，例如 Prometheus Node Exporter、Sysdig Agent、collectd、Dynatrace OneAgent、APPDynamics Agent、Datadog agent、New Relic agent、Ganglia gmond、Instana Agent 等



```yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: logging
  labels:
    app: logging
spec:
  selector:
    matchLabels:
      name: logging
  template:
    metadata:
      labels:
        name: logging
    spec:
      containers:
      - name: logging
        image: nginx
        resources:
          limits:
            memory: 200Mi
          requests:
            cpu: 100m
            memory: 200Mi
      tolerations: #设置容忍master的污点
        - key: node-role.kubernetes.io/master
          effect: NoSchedule
```

查看效果

```shell
kubectl get pod -l name=logging -o wide
```



