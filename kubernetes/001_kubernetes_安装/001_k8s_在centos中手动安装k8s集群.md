## k8s集群搭建-安装基础环境



### 1 安装docker

首先卸载已经安装的docker

```bash
sudo yum remove docker \
docker-client \
docker-client-latest \
docker-common \
docker-latest \
docker-latest-logrotate \
docker-logrotate \
docker-engine
```



安装docker-ce的前置依赖

```bash
sudo yum install -y yum-utils device-mapper-persistent-data lvm2
```



设置 docker repo 的 yum 位置 

```bash
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
cat /etc/yum.repos.d/docker-ce.repo
```



安装 docker，以及 docker-cli 

```bash
sudo yum install -y docker-ce docker-ce-cli containerd.io
```



启动docker并且查看版本

```bash
systemctl start docker
systemctl enable docker
docker --version
docker version
```



配置镜像加速

使用阿里云的容器镜像加速：

登录阿里云->控制台->左侧菜单/产品与服务/容器服务/容器镜像服务->镜像工具/镜像加速器->操作文档/centos

https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors



创建docker配置目录

```
sudo mkdir -p /etc/docker
```

将配置写入docker配置文件

```bash
cat >/etc/docker/daemon.json<<EOF
{
"registry-mirrors": ["http://hub-mirror.c.163.com"]
}
EOF
```

```
systemctl reload docker
systemctl status docker
systemctl status containerd
```



```bash
cp /etc/containerd/config.toml /etc/containerd/config.toml.ori
containerd config default > /etc/containerd/config.toml
cp /etc/containerd/config.toml /etc/containerd/config.toml.bak

sed -i 's#registry.k8s.io#registry.aliyuncs.com/google_containers#g' /etc/containerd/config.toml
sed -i 's/SystemdCgroup = false/SystemdCgroup = true/g' /etc/containerd/config.toml
sed -i 's#root = "/var/lib/containerd"#root = "/data/containerd"#g' /etc/containerd/config.toml

diff /etc/containerd/config.toml /etc/containerd/config.toml.bak
```

```bash
systemctl daemon-reload
systemctl enable containerd
systemctl restart containerd
```



#####################

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://jkzyghm3.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```



```bash
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://docker.mirrors.ustc.edu.cn","https://jkzyghm3.mirror.aliyuncs.com"],
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
EOF
```

```bash
https://hub-mirror.c.163.com 
https://registry.aliyuncs.com
https://docker.mirrors.ustc.edu.cn
https://82m9ar63.mirror.aliyuncs.com

https://jkzyghm3.mirror.aliyuncs.com
```

加载配置文件

```bash
sudo systemctl daemon-reload
```

重启docker

```bash
sudo systemctl restart docker
```

设置docker自启动

```bash
sudo systemctl enable docker --now
```

####################   ####################







### 2 安装kubernetes

添加阿里云 yum 源

```bash
cat <<EOF | sudo tee /etc/yum.repos.d/kubernetes.repo
[kubernetes]
name=Kubernetes
baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg
   http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
exclude=kubelet kubeadm kubectl
EOF
```



安装 kubeadm，kubelet 和 kubectl 

查看kube软件安装源列表

```bash
yum list|grep kube 
```

安装k8s

disableexcludes=kubernetes：禁掉除了这个kubernetes之外的别的仓库 



```bash
sudo yum install -y kubelet kubeadm kubectl --disableexcludes=kubernetes
```

=== 安装指定版本 ====

```bash
sudo yum install -y kubelet-1.20.9 kubeadm-1.20.9 kubectl-1.20.9 --disableexcludes=kubernetes
```



设置kubelet开启启动。设置为开机自启并现在立刻启动服务 --now：立刻启动服务

```bash
sudo systemctl enable --now kubelet
```

启动kubelet

```bash
systemctl start kubelet
```

```
kubectl version
yum info kubeadm
```

此时通过指令

```bash
systemctl status kubelet
```

查看kubelet的状态会发现kubelet无法启动，是正常现象，还有配置需要调整。



### 3 配置crictl

```bash
cat > /etc/crictl.yaml <<EOF
runtime-endpoint: unix:///run/containerd/containerd.sock
image-endpoint: unix:///run/containerd/containerd.sock
timeout: 10
debug: false
EOF
```

```
crictl images
crictl pods
```



### 3 使用kubeadm引导集群

===================

下载各个机器需要的镜像

```bash
sudo tee ./images.sh <<-'EOF'
#!/bin/bash
images=(
kube-apiserver:v1.20.9
kube-proxy:v1.20.9
kube-controller-manager:v1.20.9
kube-scheduler:v1.20.9
coredns:1.7.0
etcd:3.4.13-0
pause:3.2
)
for imageName in ${images[@]} ; do
docker pull registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/$imageName
done
EOF
```

执行脚本

```bash
chmod +x ./images.sh && ./images.sh
```

===================   ========================



挑选一台主机作为master，比如这里选择 k8s-node1作为master

在master上执行 

```bash
ip addr
```

查看默认网卡 eth0 的ip地址： `10.0.2.7`

在每个节点上执行：

```bash
echo "10.0.2.7  cluster-endpoint" >> /etc/hosts
```

主节点初始化

```bash
kubeadm config images pull --image-repository=registry.cn-hangzhou.aliyuncs.com/google_containers
```



```
kubeadm init \
--apiserver-advertise-address=10.0.2.7 \
--control-plane-endpoint=cluster-endpoint \
--image-repository registry.aliyuncs.com/google_containers \
--kubernetes-version v1.27.3 \
--service-cidr=10.1.0.0/16 \
--pod-network-cidr=10.244.0.0/16
```

=========

```
kubeadm init \
--apiserver-advertise-address=10.0.2.15 \
--control-plane-endpoint=cluster-endpoint \
--image-repository registry.aliyuncs.com/google_containers \
--kubernetes-version v1.20.9 \
--service-cidr=10.96.0.0/16 \
--pod-network-cidr=192.168.0.0/16
```

============   ===========

注意：apiserver-advertise-address=10.0.2.15 的值部分一定要是主节点的ip地址， control-plane-endpoint=cluster-endpoint 取值一定是上一步中在hosts文件中设置的主机名。

--image-repository registry.cn-hangzhou.aliyuncs.com/google_containers 这里设置的是阿里云的镜像仓库，可以根据实际需要配修改。不设置的情况默认使用的是 registry.k8s.io



在主节点上执行上述命令后，得到下列输出表明k8s主节点初始化成功：

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

  kubeadm join cluster-endpoint:6443 --token u2z47e.0d6gmlh0js9p6q5f \
    --discovery-token-ca-cert-hash sha256:a3b909593e4f02af43da068dce2708a776a3b9d844a3328b9fb0d518fdb6668b \
    --control-plane

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join cluster-endpoint:6443 --token u2z47e.0d6gmlh0js9p6q5f \
    --discovery-token-ca-cert-hash sha256:a3b909593e4f02af43da068dce2708a776a3b9d844a3328b9fb0d518fdb6668b
```

根据提示，如果要再加入主节点到集群中，

```
You can now join any number of control-plane nodes by copying certificate authorities
and service account keys on each node and then running the following as root:
```

使用命令：

```bash
kubeadm join cluster-endpoint:6443 --token u2z47e.0d6gmlh0js9p6q5f \
    --discovery-token-ca-cert-hash sha256:a3b909593e4f02af43da068dce2708a776a3b9d844a3328b9fb0d518fdb6668b \
    --control-plane
```

如果加入一般节点，

```bash
Then you can join any number of worker nodes by running the following on each as root:
```

则需要执行下列指令：

```bash
kubeadm join cluster-endpoint:6443 --token u2z47e.0d6gmlh0js9p6q5f \
    --discovery-token-ca-cert-hash sha256:a3b909593e4f02af43da068dce2708a776a3b9d844a3328b9fb0d518fdb6668b
```

在此之前，需要根据提示在主节点运行下列指令：

```bash
mkdir -p $HOME/.kube
```

```bash
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
```

```bash
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```



### 4 安装自动补全的插件

```bash
yum install bash-completion -y
echo 'source <(kubectl completion bash)' >>~/.bashrc
echo 'alias k=kubectl' >>~/.bashrc
echo 'complete -F __start_kubectl k' >>~/.bashrc
source ~/.bashrc
```

测试插件：

```bash
k get nodes
```



### 4 安装网络插件

完成上述操作后，后可以通过指令查看k8s的状态：

```bash
kubectl get nodes
```

输出为：

```bash
NAME        STATUS     ROLES                  AGE   VERSION
k8s-node1   NotReady   control-plane,master   14m   v1.20.9
```

只有一个节点，并且状态为 notready，原因是还没有安装pod网络插件。

根据提示

```bash
You should now deploy a pod network to the cluster.
Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
  https://kubernetes.io/docs/concepts/cluster-administration/addons/
```

可以在官网 https://kubernetes.io/docs/concepts/cluster-administration/addons/ 中看到很多种的网络插件，



=====================

这里以flannel为例：

```bash
wget  https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
```

如果无法下载也可以使用浏览器下载，再通过sftp上传到虚拟机内。

安装插件：

```bash
kubectl apply -f kube-flannel.yml
```

查看所有的pods信息

```bash
kubectl get pods --all-namespaces
```



这里以calico为例：

```
wget  https://docs.projectcalico.org/v3.21/manifests/calico.yaml
```

安装插件：

```bash
kubectl apply -f calico.yaml
```

查看pod状态

```bash
kubectl get pod -A
```

=====================   =====================





### Install Calico[](https://docs.tigera.io/calico/latest/getting-started/kubernetes/quickstart#install-calico)

1. Install the Tigera Calico operator and custom resource definitions.

   ```text
   kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.26.0/manifests/tigera-operator.yaml
   ```

   

   NOTE

   Due to the large size of the CRD bundle, `kubectl apply` might exceed request limits. Instead, use `kubectl create` or `kubectl replace`.

2. Install Calico by creating the necessary custom resource. For more information on configuration options available in this manifest, see [the installation reference](https://docs.tigera.io/calico/latest/reference/installation/api).

   ```text
   kubectl create -f https://raw.githubusercontent.com/projectcalico/calico/v3.26.0/manifests/custom-resources.yaml
   ```

   

   NOTE

   Before creating this manifest, read its contents and make sure its settings are correct for your environment. For example, you may need to change the default IP pool CIDR to match your pod network CIDR.

3. Confirm that all of the pods are running with the following command.

   ```text
   watch kubectl get pods -n calico-system
   ```

   

   Wait until each pod has the `STATUS` of `Running`.

   NOTE

   The Tigera operator installs resources in the `calico-system` namespace. Other install methods may use the `kube-system` namespace instead.

4. Remove the taints on the control plane so that you can schedule pods on it.

```bash
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
kubectl taint nodes --all node-role.kubernetes.io/master-
```



It should return the following.

```text
node/<your-hostname> untainted
```



1. Confirm that you now have a node in your cluster with the following command.

   ```text
   kubectl get nodes -o wide
   ```

   

   It should return something like the following.

   ```text
   NAME              STATUS   ROLES    AGE   VERSION   INTERNAL-IP   EXTERNAL-IP   OS-IMAGE             KERNEL-VERSION    CONTAINER-RUNTIME
   <your-hostname>   Ready    master   52m   v1.12.2   10.128.0.28   <none>        Ubuntu 18.04.1 LTS   4.15.0-1023-gcp   docker://18.6.1
   ```

   

Congratulations! You now have a single-host Kubernetes cluster with Calico.







```bash
curl --proxy http://192.168.31.206:10809
https://raw.githubusercontent.com/projectcalico/calico/v3.25.0/manifests/tig
era-operator.yaml -O
```

```bash
kubectl create -f tigera-operator.yaml
```



```bash
curl --proxy http://192.168.31.206:10809
https://raw.githubusercontent.com/projectcalico/calico/v3.25.0/manifests/cus
tom-resources.yaml -O
```



修改文件第13行，修改为使用kubeadm init --pod-network-cidr 10.244.0.0/16 vim custom-resources.yaml

cidr: 10.244.0.0/16 设置为kubeadmin初始化时 --pod-network-cidr=10.244.0.0/16 的值即可。

```bash
ipPools:
- blockSize: 26
cidr: 10.244.0.0/16
```

```bash
kubectl create -f custom-resources.yaml
```

```bash
watch kubectl get pods -n tigera-operator
```



删除污点

```bash
export hostname="k8s-maste"
kubectl taint nodes $(hostname) node-role.kubernetes.io/master:NoSchedulekubectl taint nodes $(hostname) node-role.kubernetes.io/controlplane:NoSchedule
```







