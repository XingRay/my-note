## Containerd 高级命令行工具 nerdctl

一、安装nerdctl
精简 (nerdctl–linux-amd64.tar.gz): 只包含 nerdctl
完整 (nerdctl-full–linux-amd64.tar.gz): 包含 containerd, runc, and CNI 等依赖

#下载地址:
wget https://github.com/containerd/nerdctl/releases/download/v1.2.1/nerdctl-1.2.1-linux-amd64.tar.gz

#国内加速下载地址:
wget https://proxy.zyun.vip/https://github.com/containerd/nerdctl/releases/download/v1.2.1/nerdctl-1.2.1-linux-amd64.tar.gz

#解压文件

```bash
mkdir /etc/nerdctl & tar -zxvf  nerdctl-1.2.1-linux-amd64.tar.gz -C /etc/nerdctl/
ln -s /etc/nerdctl/nerdctl /usr/local/bin/
```

#检查结果

```bash
[root@node nerdctl]# nerdctl version
WARN[0000] unable to determine buildctl version: exec: "buildctl": executable file not found in $PATH 
Client:
 Version:       v1.2.1
 OS/Arch:       linux/amd64
 Git commit:    a0bbfd75ba92bcb11ac6059bf4f6f4e50c6da0b8
 buildctl:
  Version:

Server:
 containerd:
  Version:      v1.7.0
  GitCommit:    1fbd70374134b891f97ce19c70b6e50c7b9f4e0d
 runc:
  Version:      1.1.5
  GitCommit:    v1.1.5-0-gf19387a6
```

二、安装buildkit
#下载地址
wget https://github.com/moby/buildkit/releases/download/v0.11.5/buildkit-v0.11.5.linux-amd64.tar.gz

#国内加速下载地址
wget https://proxy.zyun.vip/https://github.com/moby/buildkit/releases/download/v0.11.5/buildkit-v0.11.5.linux-amd64.tar.gz

#解压文件

```bash
mkdir /etc/buildkit && tar -zxvf buildkit-v0.11.5.linux-amd64.tar.gz -C /etc/buildkit/
ln -s /etc/buildkit/bin/buildctl /usr/local/bin/
ln -s /etc/buildkit/bin/buildkitd /usr/local/bin/
```

#使用Systemd来管理buildkitd，创建如下所示的systemd unit文件

```bash
cat >> /etc/systemd/system/buildkit.service <<EOF
[Unit]
Description=BuildKit
Documentation=https://github.com/moby/buildkit

[Service]
ExecStart=/usr/local/bin/buildkitd --oci-worker=false --containerd-worker=true

[Install]
WantedBy=multi-user.target

EOF
```

#启动buildkitd

```bash
systemctl daemon-reload
systemctl enable buildkit --now
systemctl status buildkit
```



三、常用nerdctl命令
k8s 默认使用k8s.io,而 nerdctl 默认使用 default namspace。如果需要查看 k8s 相关镜像需要加上"--namespace=k8s.io"来指定。

```bash
nerdctl images --namespace=k8s.io
nerdctl -n=k8s.io images
```


或者在 nerdctl 配置文件中指定 nerdctl 默认使用 k8s.io namespace。

```bash
mkdir  /etc/nerdctl/
cat >> /etc/nerdctl/nerdctl.toml << EOF
namespace = "k8s.io"
EOF
```



```bash
#nerdctl run :创建容器
nerdctl run -d -p 80:80 --name=nginx --restart=always nginx

#nerdctl exec :进入容器
nerdctl exec -it nginx /bin/sh

#nerdctl ps :列出容器
nerdctl ps -a

#nerdctl inspect :获取容器的详细信息 
nerdctl inspect nginx

#nerdctl logs :获取容器日志
nerdctl logs -f nginx

#nerdctl stop :停止容器
nerdctl stop nginx

#nerdctl rm :删除容器
nerdctl rm -f nginx
nerdctl rmi -f <IMAGE ID>

#nerdctl images：镜像列表
nerdctl images
nerdctl -n=k8s.io images
nerdctl -n=k8s.io images | grep -v '<none>'

#nerdctl pull :拉取镜像
nerdctl pull nginx

#使用 nerdctl login --username xxx --password xxx 进行登录，使用 nerdctl logout 可以注销退出登录
nerdctl login
nerdctl logout

#nerdctl tag :镜像标签
nerdctl tag nginx:latest harbor.k8s/image/nginx:latest

#nerdctl push :推送镜像
nerdctl push harbor.k8s/image/nginx:latest

#nerdctl save :导出镜像
nerdctl save -o busybox.tar.gz busybox:latest

#nerdctl load :导入镜像
nerdctl load -i busybox.tar.gz

#nerdctl rmi :删除镜像
nerdctl rmi busybox

#nerdctl build :从Dockerfile构建镜像
nerdctl build -t centos:v1.0 -f centos.dockerfile .
```