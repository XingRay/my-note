## 安装nerdctl

https://github.com/containerd/nerdctl

https://github.com/containerd/nerdctl/releases

https://blog.csdn.net/catoop/article/details/128033743

nerdctl 是用于 containerd 并且 兼容 docker cli 习惯的管理工具，主要适用于刚从 docker 转到 containerd 的用户，操作 containerd 的命令行工具 ctr 和 crictl 不怎么好用，所以就有了 nerdctl。要特别说明的是：nerdctl 操作的是 containerd 而非 docker，所以 nerdctl images 和 docker images 看到的内容不同，它只是用法保持了 docker cli 的习惯，实质上操作的是 containerd。

下载nerdctl

https://github.com/containerd/nerdctl/releases

https://github.com/containerd/nerdctl/releases/download/v1.4.0/nerdctl-1.4.0-linux-amd64.tar.gz



可以把安装包放在一台服务器上后,所有节点执行

```bash
mkdir -p /root/setup
```

```bash
scp -rP 22 root@192.168.0.140:/root/setup/nerdctl-1.4.0-linux-amd64.tar.gz /root/setup/nerdctl-1.4.0-linux-amd64.tar.gz
```

上传到服务器后执行：

```bash
tar -zxvf /root/setup/nerdctl-1.4.0-linux-amd64.tar.gz -C /usr/local/bin
```

```bash
-rwxr-xr-x root/root  24379392 2023-05-20 22:30 nerdctl
-rwxr-xr-x root/root     21622 2023-05-20 22:29 containerd-rootless-setuptool.sh
-rwxr-xr-x root/root      7187 2023-05-20 22:29 containerd-rootless.sh
```

查看版本：

```bash
nerdctl --version
```

```bash
root@k8s-master01:~# nerdctl --version
nerdctl version 1.4.0
```



查看名称空间

```bash
nerdctl namespace ls
```

```bash
root@k8s-master01:~# nerdctl namespace ls
NAME      CONTAINERS    IMAGES    VOLUMES    LABELS
k8s.io    34            27        0
```



列出k8s.io名称空间下的镜像

```bash
nerdctl -n <namespace-name> images
```

没有  -n 参数默认为 default

```bash
root@k8s-master01:~# nerdctl -n k8s.io images
REPOSITORY                                                         TAG        IMAGE ID        CREATED         PLATFORM       SIZE         BLOB SIZE
flannel/flannel-cni-plugin                                         v1.1.2     bf4b62b13166    22 hours ago    linux/amd64    8.2 MiB      3.7 MiB
flannel/flannel-cni-plugin                                         <none>     bf4b62b13166    22 hours ago    linux/amd64    8.2 MiB      3.7 MiB
flannel/flannel                                                    v0.22.0    5f83f1243057    22 hours ago    linux/amd64    69.7 MiB     25.6 MiB
flannel/flannel                                                    <none>     5f83f1243057    22 hours ago    linux/amd64    69.7 MiB     25.6 MiB
registry.aliyuncs.com/google_containers/coredns                    v1.10.1    a0ead06651cf    22 hours ago    linux/amd64    51.1 MiB     15.4 MiB
registry.aliyuncs.com/google_containers/coredns                    <none>     a0ead06651cf    22 hours ago    linux/amd64    51.1 MiB     15.4 MiB
registry.aliyuncs.com/google_containers/etcd                       3.5.7-0    51eae8381dcb    22 hours ago    linux/amd64    285.8 MiB    96.9 MiB
registry.aliyuncs.com/google_containers/etcd                       <none>     51eae8381dcb    22 hours ago    linux/amd64    285.8 MiB    96.9 MiB
registry.aliyuncs.com/google_containers/kube-apiserver             v1.27.3    fd03335dd2e7    22 hours ago    linux/amd64    118.9 MiB    31.8 MiB
registry.aliyuncs.com/google_containers/kube-apiserver             <none>     fd03335dd2e7    22 hours ago    linux/amd64    118.9 MiB    31.8 MiB
registry.aliyuncs.com/google_containers/kube-controller-manager    v1.27.3    1ad8df2b525e    22 hours ago    linux/amd64    111.1 MiB    29.5 MiB
registry.aliyuncs.com/google_containers/kube-controller-manager    <none>     1ad8df2b525e    22 hours ago    linux/amd64    111.1 MiB    29.5 MiB
registry.aliyuncs.com/google_containers/kube-proxy                 v1.27.3    fb2bd59aae95    22 hours ago    linux/amd64    72.1 MiB     22.8 MiB
registry.aliyuncs.com/google_containers/kube-proxy                 <none>     fb2bd59aae95    22 hours ago    linux/amd64    72.1 MiB     22.8 MiB
registry.aliyuncs.com/google_containers/kube-scheduler             v1.27.3    77b8db7564e3    22 hours ago    linux/amd64    59.5 MiB     17.4 MiB
registry.aliyuncs.com/google_containers/kube-scheduler             <none>     77b8db7564e3    22 hours ago    linux/amd64    59.5 MiB     17.4 MiB
registry.aliyuncs.com/google_containers/pause                      3.9        7031c1b28338    22 hours ago    linux/amd64    732.0 KiB    314.0 KiB
registry.aliyuncs.com/google_containers/pause                      <none>     7031c1b28338    22 hours ago    linux/amd64    732.0 KiB    314.0 KiB
<none>                                                             <none>     fd03335dd2e7    22 hours ago    linux/amd64    118.9 MiB    31.8 MiB
<none>                                                             <none>     5f83f1243057    22 hours ago    linux/amd64    69.7 MiB     25.6 MiB
<none>                                                             <none>     77b8db7564e3    22 hours ago    linux/amd64    59.5 MiB     17.4 MiB
<none>                                                             <none>     fb2bd59aae95    22 hours ago    linux/amd64    72.1 MiB     22.8 MiB
<none>                                                             <none>     bf4b62b13166    22 hours ago    linux/amd64    8.2 MiB      3.7 MiB
<none>                                                             <none>     1ad8df2b525e    22 hours ago    linux/amd64    111.1 MiB    29.5 MiB
<none>                                                             <none>     51eae8381dcb    22 hours ago    linux/amd64    285.8 MiB    96.9 MiB
<none>                                                             <none>     7031c1b28338    22 hours ago    linux/amd64    732.0 KiB    314.0 KiB
<none>                                                             <none>     a0ead06651cf    22 hours ago    linux/amd64    51.1 MiB     15.4 MiB
```



通过配置文件来生成默认的名称空间

```bash
cd /etc
mkdir nerdctl
cd nerdctl
vi nerdctl.toml
```

输入：

```bash
namespace="k8s.io"
```

保存即可。



然后再通过指令查看镜像

```bash
nerdctl images
```

可以显示出k8s.io命名空间下的镜像就说明默认命名空间已经改为了 k8s.io