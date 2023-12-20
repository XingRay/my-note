## KubeSphere中devops-jenkins一直启动失败

devops-jenkins 一直启动失败，在KubeSphere平台上查看日志，显示是 OOM Killed ，这个一般是由于资源配置不足导致。参考：

https://www.kubesphere.io/forum/d/22249-devops-jenkinsqi-bu-lai

解决办法：

### 1 调整部署 JVM 配置

JVM参数调整，增加 最大堆大小 和 初始堆大小

```bash
-Xmx：最大堆大小
-Xms：初始堆大小
```

设置初始堆为 512m，最大堆为 4G

```yaml
env:
	- name: JAVA_TOOL_OPTIONS
        value: >-
            -XX:MaxRAMPercentage=80 -XX:MinRAMPercentage=60
            -Dhudson.slaves.NodeProvisioner.initialDelay=20
            -Dhudson.slaves.NodeProvisioner.MARGIN=50
            -Dhudson.slaves.NodeProvisioner.MARGIN0=0.85
            -Dhudson.model.LoadStatistics.clock=5000
            -Dhudson.model.LoadStatistics.decay=0.2
            -Dhudson.slaves.NodeProvisioner.recurrencePeriod=5000
            -Dhudson.security.csrf.DefaultCrumbIssuer.EXCLUDE_SESSION_ID=true
            -Dio.jenkins.plugins.casc.ConfigurationAsCode.initialDelay=10000
            -Djenkins.install.runSetupWizard=false -XX:+AlwaysPreTouch
            -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC
            -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled
            -XX:+DisableExplicitGC -XX:+UnlockDiagnosticVMOptions
            -XX:+UnlockExperimentalVMOptions  
            -Xmx4096m 
            -Xms512m
```

一般调整后重新创建pod即可，如果还有问题，可以参考下面的修改：



### 2 调整部署资源限制配置

在KubeSphere平台中找到 deployment devops-jenkins ，修改 resourceLimit 和 jvm参数相关的配置：

资源限制，内存由默认的4G调整到8G

```yaml
resources:
	limits:
		cpu: '4'
		memory: 8Gi
```



### 3 调整集群配置

调整下 ClusterConfiguration ks-installer 里 Jenkins 相关的配置，执行命令:

```bash
kubectl -n kubesphere-system edit cc ks-installer
```

修改下列部分，默认内存限制为4G，可以调整成8G：

```yaml
devops:
    jenkinsCpuLim: 2  
    jenkinsMemoryLim: 8Gi
```

