## ubuntu安装k8s集群

安装 vmware station 17

下载ubuntu22-server.iso

安装ubuntu22-server

网络模式选择桥接模式

在vmware中设置虚拟网络，选择桥接模式

安装ubuntu选择网络模式为桥接，使用dhcp自动分配ip地址

安装时将源修改为阿里源

```bash
http://mirrors.aliyun.com/ubuntu/
```

安装完成后在vmware中登录系统



设置root密码

```bash
sudo passwd root
```



设置ssh

```bash
sudo apt install -y openssh-server
```

修改允许远程登录：

```bash
sudo vi /etc/ssh/sshd_config
```

修改下面的配置行：

```bash
PermitRootLogin prohibit-password
```

修改为：

```bash
PermitRootLogin yes
```



```bash
sudo systemctl restart ssh
```



通过 ssh工具连接ubuntu



更新系统

```bash
sudo apt update
sudo apt upgrade -y
```

安装工具

```bash
sudo apt install -y vim wget net-tools
```

做好snapshot备份

```bash
ip addr
```

查看ip信息

```bash
192.168.0.112 k8s-master01
192.168.0.113 k8s-node01
192.168.0.114 k8s-node02
```

确保相互直接可以ping通，并且可以ping通外网

```bash
sudo vi /etc/hosts
```

将上述ip信息复制到各个节点的hosts中

```bash
exec bash
```



确保每个节点上 MAC 地址和 product_uuid 的唯一性

查看mac地址

```bash
ip link
```

或者使用这个命令：

```
ifconfig -a
```



可以使用 下列命令对 product_uuid 校验

```bash
sudo cat /sys/class/dmi/id/product_uuid
```



### 关闭防火墙

ufw查看当前的防火墙状态：inactive状态是防火墙关闭状态 active是开启状态。

```bash
ufw status
```

启动、关闭防火墙

```bash
ufw enable | disable
```

此处如果防火墙是开启的，需要关闭防火墙

```bash
ufw disable
```



### 禁用SELINUX

```bash
apt install -y selinux-utils
```

```bash
setenforce 0
```



```bash
vim /etc/selinux/config
```

添加一行

```bash
SELINUX=disabled
```



### 禁用所有swap交换分区

```bash
swapoff -a
```

查看分区信息，确认swap为0

```
free -h
```

永久禁用swap,删除或注释掉/etc/fstab里的swap设备的挂载命令即可

```bash
vim /etc/fstab
```

注释掉swap分区行

```bash
#/swap.img      none    swap    sw      0       0
```



### 同步时间

查看时间，注意时区

```bash
date
```

如果时区不正确可以通过下面的指令设置

```bash
timedatectl set-timezone Asia/Shanghai
```

安装 ntp

```bash
apt install -y ntp
```

开始ntpd服务,或者做定时任务如：*/5 * * * * /usr/sbin/ntpdate -u 192.168.2.5

```bash
systemctl start ntp
systemctl enable ntp
```



### 加载内核模块

```bash
sudo tee /etc/modules-load.d/containerd.conf <<EOF
overlay
br_netfilter
EOF
```

```bash
sudo modprobe overlay
sudo modprobe br_netfilter
```



### 为 Kubernetes 设置以下内核参数

```bash
sudo tee /etc/sysctl.d/kubernetes.conf <<EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
EOF
```

加载上述更新

```bash
sudo sysctl --system
```



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



修改系统配置文件,增大允许打开的文件数

```bash
vi /etc/security/limits.conf
```

在最后加入  

```bash
*    soft nofile 102400
*    hard nofile 102400
root soft nofile 102400
root hard nofile 102400
```



### 配置服务器支持开启ipvs

```bash
sudo apt-get install ipvsadm
```

```bash
apt install -y ipset
```

执行脚本

```bash
mkdir -p /etc/sysconfig/modules
```



```bash
cat <<EOF> /etc/sysconfig/modules/ipvs.modules
#!/bin/bash
modprobe -- ip_vs
modprobe -- ip_vs_rr
modprobe -- ip_vs_wrr
modprobe -- ip_vs_sh
modprobe -- nf_conntrack
EOF
```

```bash
chmod +x /etc/sysconfig/modules/ipvs.modules
```

```bash
/bin/bash /etc/sysconfig/modules/ipvs.modules
```

```
lsmod | grep -e -ip_vs -e nf_conntrack
```





### 安装 containerd 容器运行时

安装容器运行时依赖项

```bash
sudo apt install -y curl gnupg2 software-properties-common apt-transport-https ca-certificates
```



启用 docker 存储库

```bash
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmour -o /etc/apt/trusted.gpg.d/docker.gpg
```

```bash
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
```



安装 containerd

```bash
sudo apt update
```

```bash
sudo apt install -y containerd.io
```



配置 containerd，使它使用 systemd 作为 cgroup

```bash
containerd config default | sudo tee /etc/containerd/config.toml >/dev/null 2>&1
```

```bash
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/g' /etc/containerd/config.toml
```



将镜像地址换为阿里云地址，否者在初始化时无法拉取到镜像

```bash
sudo sed -i "s#registry.k8s.io/pause#registry.aliyuncs.com/google_containers/pause#g" /etc/containerd/config.toml
```



重启并启用 containerd 服务

```bash
sudo systemctl daemon-reload
sudo systemctl restart containerd
sudo systemctl enable containerd
```



### 添加 Kubernetes apt 存储库

更新 `apt` 包索引并安装使用 Kubernetes `apt` 仓库所需要的包

```bash
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl
```

下载 Google Cloud 公开签名秘钥

```bash
sudo curl -fsSLo /etc/apt/keyrings/kubernetes-archive-keyring.gpg https://packages.cloud.google.com/apt/doc/apt-key.gpg
```

若出现以下报错

```bash
curl: (28) Failed to connect to packages.cloud.google.com port 443 after 129625 ms: 连接超时
```

需手动下载 https://packages.cloud.google.com/apt/doc/apt-key.gpg ，将下载后的apt-key.gpg复制到/usr/share/keyrings/kubernetes-archive-keyring.gpg文件下:

```bash
sudo cp /root/apt-key.gpg  /usr/share/keyrings/kubernetes-archive-keyring.gpg
```



添加 Kubernetes apt仓库

```
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee /etc/apt/sources.list.d/kubernetes.list
```



### 安装Kubectl, kubeadm 和 kubelet

更新索引

```bash
sudo apt-get update
```

报错，是由于没有换源，执行下列操作：

```bash
echo "deb https://mirrors.aliyun.com/kubernetes/apt/ kubernetes-xenial main" | sudo tee /etc/apt/sources.list.d/kubernetes.list
```

更新

```bash
sudo apt-get update
```

接下来还是报错：

```bash
W: GPG error: https://mirrors.aliyun.com/kubernetes/apt kubernetes-xenial InRelease: The following signatures couldn't be verified because the public key is not available: NO_PUBKEY B53DC80D13EDEF05
E: The repository 'https://mirrors.aliyun.com/kubernetes/apt kubernetes-xenial InRelease' is not signed.
```

更新提示缺少公钥

xxxxxx就是缺少的公钥，就是刚刚报错中的那一串

```bash
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys  xxxxxx
```

报错中有：  NO_PUBKEY B53DC80D13EDEF05 ，命令修改为：

```bash
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys  B53DC80D13EDEF05
```



更新 `apt` 包索引，安装 kubelet、kubeadm 和 kubectl，并锁定其版本

```bash
sudo apt-get update
```

安装最新版本

```bash
sudo apt-get install -y kubelet kubeadm kubectl
```

安装指定版本，k8s版本参考 https://github.com/kubernetes/kubernetes/releases

查看可安装版本

```bash
apt-cache madison <app-name>
```

如查看kubeadm版本

```
apt-cache madison kubeadm
```

由于后面要安装KubeSphere，当前KubeSphere最新版为v3.3.2，支持的Kubernetes 版本必须为：v1.20.x、v1.21.x、* v1.22.x、* v1.23.x 和 * v1.24.x，选择 1.24.x 的最新版 1.24.15-00

```bash
apt-get install -y kubelet=1.24.15-00 kubeadm=1.24.15-00 kubectl=1.24.15-00
```

锁定版本，apt不会自动升级这些软件

```bash
sudo apt-mark hold kubelet kubeadm kubectl
```



### 初始化 Kubernetes 集群

修改配置：

```bash
sudo vi /etc/containerd/config.toml
```

将：

```bash
sandbox_image = "registry.aliyuncs.com/google_containers/pause:3.6"
```

修改为：

```bash
sandbox_image = "registry.aliyuncs.com/google_containers/pause:3.9"
```

版本由3.6修改为3.9



```bash
sudo systemctl daemon-reload
sudo systemctl restart containerd
sudo systemctl enable containerd
```





### 主节点初始化

主节点ip 192.168.0.112

各个节点记录cluster-endpoint 的ip：

```bash
echo "192.168.0.112 cluster-endpoint" >> /etc/hosts
```

```bash
kubeadm version
kubectl version
```

查看版本，这里是 1.24.15



在**主节点**执行初始化：

```bash
sudo kubeadm init \
--apiserver-advertise-address=192.168.0.112 \
--control-plane-endpoint=cluster-endpoint \
--image-repository registry.aliyuncs.com/google_containers  \
--kubernetes-version v1.24.15 \
--service-cidr=10.96.0.0/16 \
--pod-network-cidr=10.244.0.0/16 \
--v=6
```

apiserver-advertise-address: 主节点ip

control-plane-endpoint 主节点域名

kubernetes-version k8s版本



初始化成功输出：

```bash
Your Kubernetes control-plane has initialized successfully!

To start using your cluster, you need to run the following as a regular user:

  mkdir -p $HOME/.kube
  sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
  sudo chown $(id -u):$(id -g) $HOME/.kube/config

Alternatively, if you are the root user, you can run:

  export KUBECONFIG=/etc/kubernetes/admin.conf

You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/

You can now join any number of control-plane nodes by copying certificate authorities
and service account keys on each node and then running the following as root:

  kubeadm join cluster-endpoint:6443 --token k30zfa.e5xpshofpupb2q07 \
        --discovery-token-ca-cert-hash sha256:4c81ffa8cb575e00f168bcfd1b7b9a26b426b9af3744f9d2338806f40700939f \
        --control-plane

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join cluster-endpoint:6443 --token k30zfa.e5xpshofpupb2q07 \
        --discovery-token-ca-cert-hash sha256:4c81ffa8cb575e00f168bcfd1b7b9a26b426b9af3744f9d2338806f40700939f
```



主节点执行：

```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config

export KUBECONFIG=/etc/kubernetes/admin.conf
```



在主节点安装flannel网络插件(CNI)

```bash
curl -O https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
```

下载失败可以通过浏览器下载再使用sftp传到虚拟机内

```bash
kubectl apply -f kube-flannel.yml
```

部署好网络插件之后此时再次查看节点可以看到状态变成了ready



### 从节点加入集群

在从节点上通过下列指令加入集群：

```bash
kubeadm join cluster-endpoint:6443 --token k30zfa.e5xpshofpupb2q07 \
        --discovery-token-ca-cert-hash sha256:4c81ffa8cb575e00f168bcfd1b7b9a26b426b9af3744f9d2338806f40700939f
```



如果加入集群的token过期，可以在主节点上运行下列指令重新生成：

```bash
kubeadm token create --print-join-command
```

```bash
kubeadm join cluster-endpoint:6443 --token a8cm3q.7dqc9hvoznvwnh7x --discovery-token-ca-cert-hash sha256:4c81ffa8cb575e00f168bcfd1b7b9a26b426b9af3744f9d2338806f40700939f
```



### 重置集群

可能遇到某些问题后需要重置k8s集群到初始状态，流程如下：

1 先在主节点手动删除 $HOME/.kube ：

```bash
rm -rf $HOME/.kube
```

2 在所有节点执行：

```bash
kubeadm reset
ipvsadm --clear
```

这样集群就处于初始状态，下面就需要重新在主节点初始化，然后从节点加入集群即可。



### 安装nerdctl

下载nerdctl

https://github.com/containerd/nerdctl/releases

https://github.com/containerd/nerdctl/releases/download/v1.4.0/nerdctl-1.4.0-linux-amd64.tar.gz

上传到服务器后执行：

```bash
tar Cxzvvf /usr/local/bin nerdctl-1.4.0-linux-amd64.tar.gz
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

k8s 默认使用的namspace是`k8s.io`,而 nerdctl 默认使用的namspace是 `default`。如果需要查看或者操作 k8s 相关镜像需要加上"--namespace=k8s.io"来指定，如：

```bash
nerdctl images --namespace=k8s.io
nerdctl -n=k8s.io images
```

这样比较麻烦，可以在 nerdctl 配置文件中指定 nerdctl 默认使用 k8s.io namespace。

```bash
mkdir -p /etc/nerdctl/
cat >> /etc/nerdctl/nerdctl.toml << EOF
namespace = "k8s.io"
EOF
```
