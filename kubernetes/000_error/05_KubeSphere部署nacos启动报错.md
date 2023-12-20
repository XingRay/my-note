## KubeSphere部署nacos启动报错

```bash
Caused by: com.alibaba.nacos.api.exception.NacosException: java.io.IOException: User limit of inotify instances reached or too many open files
```

这个问题通过使用 `ulimit -n` 修改打开文件数是无效的，解决办法：

在k8s的各个节点宿主机上修改 

```bash
vi /etc/sysctl.conf
```

增加三项：

```bash
fs.inotify.max_queued_events = 32768
fs.inotify.max_user_instances = 65536
fs.inotify.max_user_watches = 1048576
```

使配置立即生效：

```bash
sysctl -p 
```

生效解决