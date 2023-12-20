## containerd安装

原创

发布于 2023-04-27 08:58:35

2340

举报

Containerd是一个开源的容器运行时管理器，用于管理容器的生命周期，包括容器的创建、启动、停止、暂停和销毁。它是Docker Engine的核心组件之一，也是Kubernetes、CRI-O等容器平台的基础组件。

## 安装Containerd

Containerd的安装非常简单，可以通过各种包管理工具直接安装，也可以从官方网站下载二进制包进行安装。

### 使用包管理工具安装

#### Ubuntu/Debian

在Ubuntu或Debian上安装Containerd，可以使用以下命令：

```javascript
$ sudo apt-get update
$ sudo apt-get install containerd
```

复制

#### CentOS/Fedora

在CentOS或Fedora上安装Containerd，可以使用以下命令：

```javascript
$ sudo yum update
$ sudo yum install containerd
```

复制

#### Arch Linux

在Arch Linux上安装Containerd，可以使用以下命令：

```javascript
$ sudo pacman -S containerd
```

复制

### 下载二进制包进行安装

另外，你也可以从官方网站下载Containerd的二进制包进行安装，具体步骤如下：

1. 前往Containerd的官方网站，下载对应版本的二进制包。
2. 将下载的二进制包解压到合适的目录下。
3. 配置Containerd的配置文件，具体方法请见下一节。

## 配置Containerd

在安装完Containerd之后，需要进行一些基本的配置，以便Containerd能够正常工作。

### 配置文件

Containerd的配置文件位于`/etc/containerd/config.toml`，你可以使用任意文本编辑器打开该文件进行修改。该配置文件包含了Containerd的各种配置选项，包括日志、网络、镜像存储等等。

以下是一个简单的配置文件示例：

```javascript
[debug]
  level = "info"

[grpc]
  address = "/run/containerd/containerd.sock"

[plugins."io.containerd.grpc.v1.cri"]
  disable_tcp_service = true
  stream_server_address = "/run/containerd/containerd.sock"
  stream_idle_timeout = "4h"
  enable_selinux = false
  sandbox_image = "k8s.gcr.io/pause:3.1"
```

复制

上面的配置文件中，我们设置了Containerd的日志级别为`info`，将GRPC的地址设置为`/run/containerd/containerd.sock`，并开启了CRI插件。

### 镜像存储

在配置Containerd的镜像存储时，需要指定一个或多个镜像存储后端，以便Containerd能够从这些后端中加载镜像。目前Containerd支持的镜像存储后端包括local、docker、oci、registry、snapshot等等，你可以在配置文件中指定这些后端，具体方法如下：

1. 打开配置文件，找到`[plugins."io.containerd.grpc.v1.cri"]`部分。
2. 在该部分下方添加以下内容：

```javascript
[plugins."io.containerd.grpc.v1.cri".containerd]
  snapshotter = "overlayfs"

[plugins."io.containerd.grpc.v1.cri".cni]
  bin_dir = "/opt/cni/bin"
  conf_dir = "/etc/cni/net.d"
```

复制

上面的配置中，我们使用了`overlayfs`作为镜像快照后端，同时指定了CNI的bin和conf目录。

### 启动Containerd

完成了配置之后，就可以启动Containerd了。使用以下命令启动Containerd：

```javascript
$ sudo systemctl start containerd
```

复制

可以通过以下命令查看Containerd的状态：

```javascript
$ sudo systemctl status containerd
```

复制

如果Containerd已经成功启动，你应该能够看到类似于以下的输出：

```javascript
● containerd.service - containerd container runtime
   Loaded: loaded (/usr/lib/systemd/system/containerd.service; disabled; vendor preset: disabled)
   Active: active (running) since Wed 2021-09-01 16:28:23 CST; 13s ago
     Docs: https://containerd.io
 Main PID: 12345 (containerd)
    Tasks: 20
   Memory: 26.6M
   CGroup: /system.slice/containerd.service
           ├─12345 /usr/bin/containerd
           └─12346 /usr/bin/containerd-shim --runtime=containerd --experimental=false --systemd-cgroup=true --log-level=error --start-timeout=2m --state-dir=/run/containerd/io.containerd.runtime.v1.linux/myservice --socket-path=/run/containerd/containerd.sock mycontainer
```

