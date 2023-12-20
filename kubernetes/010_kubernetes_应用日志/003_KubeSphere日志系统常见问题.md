## KubeSphere日志系统常见问题



### 1 如何将日志存储改为外部ElasticSearch 并关闭内部 ElasticSearch

1 修改 ks-installer 设置

```
kubectl -n kubesphere-system edit cc ks-installer
```



2 将外部es配置打开

将 es.elasticsearchDataXXX es.elasticsearchMasterXXX 和 status.logging 注释放开, 将es.externalElasticsearchUrl 设置为外部es的地址, 将 es.externalElasticsearchPort 设置为外部es的端口

```yaml
apiVersion: installer.kubesphere.io/v1alpha1
kind: ClusterConfiguration
metadata:
  name: ks-installer
  namespace: kubesphere-system
  ...
spec:
  ...
  common:
    es:
      # elasticsearchDataReplicas: 1
      # elasticsearchDataVolumeSize: 20Gi
      # elasticsearchMasterReplicas: 1
      # elasticsearchMasterVolumeSize: 4Gi
      elkPrefix: logstash
      logMaxAge: 7
      externalElasticsearchHost: <192.168.0.2>
      externalElasticsearchPort: <9200>
  ...
status:
  ...
  # logging:
  #  enabledTime: 2020-08-10T02:05:13UTC
  #  status: enabled
  ...
```

3 重新运行 ks-installer

```bash
kubectl rollout restart deploy -n kubesphere-system ks-installer
```



4 备份内部es数据



5 删除内部es

```
helm uninstall -n kubesphere-logging-system elasticsearch-logging
```



6 如果启用了istio 修改jaeger设置

```bash
kubectl -n istio-system edit jaeger
```

修改下列部分

```
...
 options:
      es:
        index-prefix: logstash
        server-urls: http://elasticsearch-logging-data.kubesphere-logging-system.svc:9200  # 修改为外部地址
```





## 如何设置审计、事件、日志及 Istio 日志信息的保留期限

KubeSphere v3.3 还支持您设置日志、审计、事件及 Istio 日志信息的保留期限。

您需要更新 KubeKey 配置并重新运行 `ks-installer`。

1. 执行以下命令：

   ```
   kubectl edit cc -n kubesphere-system ks-installer
   ```

2. 在 YAML 文件中，如果您只想修改日志的保存期限，可以直接修改 `logMaxAge` 的默认值。如果您想设置审计、事件及 Istio 日志信息的保留期限，需要添加参数 `auditingMaxAge`、`eventMaxAge` 和 `istioMaxAge`，并分别设置它们的保存期限，如下例所示：

   ```
   apiVersion: installer.kubesphere.io/v1alpha1
   kind: ClusterConfiguration
   metadata:
     name: ks-installer
     namespace: kubesphere-system
     ...
   spec:
     ...
     common:
       es:   # Storage backend for logging, events and auditing.
         ...
         logMaxAge: 7             # Log retention time in built-in Elasticsearch. It is 7 days by default.
         auditingMaxAge: 2
         eventMaxAge: 1
         istioMaxAge: 4
     ...
   ```

3. 重新运行 `ks-installer`。

   ```
   kubectl rollout restart deploy -n kubesphere-system ks-installer
   ```





## 工具箱中的日志查询页面在加载时卡住

如果您发现日志查询页面在加载时卡住，请检查您所使用的存储系统。例如，配置不当的 NFS 存储系统可能会导致此问题。

## 工具箱显示今天没有日志记录

请检查您的日志存储卷是否超过了 Elasticsearch 的存储限制。如果是，请增加 Elasticsearch 的磁盘存储卷容量。

## 在工具箱中查看日志时，报告内部服务器错误

如果您在工具箱中看到内部服务器错误，可能有以下几个原因：

- 网络分区
- 无效的 Elasticsearch 主机和端口
- Elasticsearch 健康状态为红色



## 如何让 KubeSphere 只收集指定工作负载的日志

KubeSphere 的日志代理由 Fluent Bit 所提供，您需要更新 Fluent Bit 配置来排除某些工作负载的日志。若要修改 Fluent Bit 输入配置，请运行以下命令：

```
kubectl edit input -n kubesphere-logging-system tail
```

更新 `Input.Spec.Tail.ExcludePath` 字段。例如，将路径设置为 `/var/log/containers/*_kube*-system_*.log`，以排除系统组件的全部日志。

有关更多信息，请参见 [Fluent Bit Operator](https://github.com/kubesphere/fluentbit-operator)。



