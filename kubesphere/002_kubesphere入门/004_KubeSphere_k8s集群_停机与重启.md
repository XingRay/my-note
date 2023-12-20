# K8s集群停机与重启



## 1 单个Master节点的停机与重启

当master节点需要维护/迁移,就需要平滑的停止 / 启动该节点, 尽量减少启停中对集群造成的影响



前提 :

Kubernetes集群为使用KubeKey安装

为了确保Kubenretes集群能够安全恢复,在操作前对Kubernetes数据进行备份

为了确保重启Master节点期间Kubernetes集群能够使用,集群中master节点数量要大于等于3



提示:

若果启停的master节点上有etcd服务,在启停该master节点期间,不能对集群的资源进行任何操作, 包括 添加 / 更新 / 删除 等.否则会导致启停节点的 etcd 数据失效 .



启停流程 :

1 登录 KubeSphere

2 备份数据

3 停止 master 节点调度

4 驱逐 master 节点的工作负载

5 停止 master 节点

6 恢复 master 节点

7 允许 master 节点调度



#### 1.1 登录 KubeSphere

登录web页面



#### 1.2 备份数据

略, 参考 k8s集群_备份与恢复



#### 1.3 停止 master 节点调度 

进入 平台 - 集群管理 - 节点 - 集群节点  点击要停止的 master 节点, 进入节点的详情页

点击 停止调度

停止调度后,运行在这个节点上的工作负载不会受到影响, 新的工作负载不会被调度到这个节点上



#### 1.4 驱逐 master 节点的工作负载

由于有部分组件会自动调度到 master 节点上, 因此需要驱逐 master 节点上的工作负载 . 

运行指令

```bash
kubectl drain ks.master3 --ignore-daemonsets --delete-local-data
```

驱逐此节点的工作负载后, kubernetes 会自动在其他的节点上创建工作负载 , 执行驱逐命令后, 需要查看相关 pod 的状态 . 以确保驱逐完毕



#### 1.5 停止 master 节点

ssh 登录该节点

停止 kubelet

```bash
systemctl stop kubelet
```

停止 etcd

```bash
systemctl stop etcd
```

停止docker

```bash
systemctl stop docker
```

查看服务状态

```bash
systemctl status docker
```

```bash
systemctl status etcd
```

```
systemctl status kubelet
```

上面的服务都停止后

```bash
exit
```

此时该节点就已经下线了, 在其他的master节点查看集群节点状态,确保目标节点已经正常下线

```bash
kubectl get nodes
```



#### 1.6 恢复 master 节点

ssh登录该节点

启动docker

```bash
systemctl start docker
```

启动etcd

```bash
systemctl start etcd
```

启动kubelet

```bash
systemctl start kubelet
```



查看服务状态

```bash
systemctl status docker
```

```bash
systemctl status etcd
```

```
systemctl status kubelet
```

当所有的服务正常运行后 , 查看节点状态

```bash
kubectl get nodes
```

节点状态为 ready ,则该节点已正常上线



#### 1.7 允许 master 节点调度

登录KubeSphere, 



查看节点状态

进入 平台 - 集群管理 - 节点 - 集群节点  点击要停止的 master 节点, 进入节点的详情页

查看健康状态, 当所有的健康状态为正常时表示该节点已就绪



查看 etcd 状态

监控和报警 - 集群状态 - ETCD监控 _ETCD监控 - ETCD节点  确保每一个节点的状态都为正常运行



确认节点状态没有问题后 , 点击 开始调度





## 2 单个Worker节点的停机与重启

场景: 当某个worker节点需要维护 / 迁移 , 需要平滑的停止 / 启动该节点 , 尽量减少启停中对集群 / 业务造成影响 . 

提示:

移除worker节点的操作中, 该 worker节点上的工作负载将会被驱逐到其他的节点上, 注意要确保集群资源充足.



流程:

1 登录 KubeSphere

2 停止 worker节点调度

3 驱逐 worker节点的工作负载

4 停止 worker节点

5 恢复 worker节点

6 允许 worker节点调度



### 2.1 登录 KubeSphere

登录web页面



### 2.2 停止 worker节点调度

进入 平台 - 集群管理 - 节点 - 集群节点  点击要停止的 worker 节点, 进入节点的详情页

点击 停止调度

停止调度后,运行在这个节点上的工作负载不会受到影响, 新的工作负载不会被调度到这个节点上



### 2.3 驱逐 worker节点的工作负载

由于有部分组件会自动调度到 master 节点上, 因此需要驱逐 master 节点上的工作负载 . 

运行指令

```bash
kubectl drain ks.worker --ignore-daemonsets --delete-local-data
```

驱逐此节点的工作负载后, kubernetes 会自动在其他的节点上创建工作负载 , 执行驱逐命令后, 需要查看相关 pod 的状态 . 以确保驱逐完毕



### 2.4 停止 worker节点

ssh 登录该节点

停止 kubelet

```bash
systemctl stop kubelet
```

停止docker

```bash
systemctl stop docker
```

查看服务状态

```bash
systemctl status docker
```

```
systemctl status kubelet
```

上面的服务都停止后

```bash
exit
```

此时该节点就已经下线了, 在其他的master节点查看集群节点状态,确保目标节点已经正常下线

```bash
kubectl get nodes
```





### 2.5 恢复 worker节点

ssh登录该节点

启动docker

```bash
systemctl start docker
```

启动kubelet

```bash
systemctl start kubelet
```



查看服务状态

```bash
systemctl status docker
```

```
systemctl status kubelet
```

当所有的服务正常运行后 , 查看节点状态

```bash
kubectl get nodes
```

节点状态为 ready ,则该节点已正常上线





### 2.6 允许 worker节点调度

登录KubeSphere, 



查看节点状态

进入 平台 - 集群管理 - 节点 - 集群节点  点击要停止的 master 节点, 进入节点的详情页

查看健康状态, 当所有的健康状态为正常时表示该节点已就绪



确认节点状态没有问题后 , 点击 开始调度



## 3 整个集群的停机与重启

场景:

计划内的机房断电/断网等特殊情况可能发生, 在此之前需要平滑地停止Kubernetes集群,在环境恢复后再平滑地启动集群,从而避免集群异常



前提:

Kubernetes集群为使用Kubekey安装

为确保Kubenetes集群能够安全恢复, 在操作前需要对Kubernetes数据进行备份, 参考: 备份与恢复

控制节点能够通过ssh使用root用户登录到任何节点并执行命令



提示: 

重启集群时高危操作,请确认是否必要且有完备的重启方案.

启停Kubernetes集群期间不要对集群进行任何其他操作

重启后请检查集群状态, 确保集群所有节点已就绪



k8s集群启停的类型和场景

单集群启停

k8s集群启停,用于紧急情况下停止集群,放置集群异常



多集群启停

多集群即联邦集群,为k8s提供的管理多个集群解决方案,多集群启停用于特殊情况下的停止和恢复集群,放置集群异常.



这里说明的是单集群启停



操作概览

1 登录控制节点

2 备份数据

3 停止所有节点

4 恢复所有节点

5 检查集群状态

6 常见问题排查



### 3.1 登录控制节点

ssh连入master节点

查看节点状态

```bash
kubectl get nodes
```



### 3.2 备份数据

略, 参考 备份与恢复



### 3.3 停止所有节点

```bash
nodes=$(kubectl get nodes -o name | awk -F[/] '{print $2}')
etcdnodes='k8s-master01 k8s-master02 k8s-master03'
```

停止kubelet

```bash
for node in ${nodes[@]}
do
	echo "===== Stop kubelet on $node ======"
	ssh root@$node systemctl stop kubelet
done
```



停止etcd集群

```bash
for etcdnode in ${etcdnodes[@]}
do
	echo "===== Stop etcd on $etcdnode ======"
	ssh root@$etcdnode systemctl stop etcd
done
```



停止docker服务

```bash
for node in ${nodes[@]}
do
	echo "===== Stop docker on $node ======"
	ssh root@$node systemctl stop docker
done
```



查看服务状态

```bash
systemctl status docker
```

```bash
systemctl status etcd
```

```bash
systemctl status kubelet
```

当所有节点都退出后,k8s集群就已完全停止

一键停止集群

```bash
nodes=$(kubectl get nodes -o name | awk -F[/] '{print $2}')
etcdnodes='k8s-master01 k8s-master02 k8s-master03'

for node in ${nodes[@]}
do
	echo "===== Stop kubelet on $node ======"
	ssh root@$node systemctl stop kubelet
done

for etcdnode in ${etcdnodes[@]}
do
	echo "===== Stop etcd on $etcdnode ======"
	ssh root@$etcdnode systemctl stop etcd
done

for node in ${nodes[@]}
do
	echo "===== Stop docker on $node ======"
	ssh root@$node systemctl stop docker
done
```



### 3.4 恢复所有节点

登录集群控制节点

```bash
nodes='ks.master01 ks.master02 ks.master03 ks.worker01 ks.worker02'
```

启动docker服务

```bash
for node in ${nodes[@]}
do
	echo "===== Stop docker on $node ======"
	ssh root $node systemctl start docker
done
```



启动etcd集群

```bash
etcdnodes='ks.master01 ks.master02 ks.master03'
for etcdnode in ${etcdnodes[@]}
do
	echo "===== Stop etcd on $etcdnode ======"
	ssh root $etcdnode systemctl start etcd
done
```



启动kubelet

```bash
for node in ${nodes[@]}
do
	echo "===== Stop kubelet on $node ======"
	ssh root $node systemctl stop kubelet
done
```



kubelet启动之后可以使用kubectl命令查看节点状态

```bash
kubectl get nodes
```

带所有节点状态为ready时, 说明节点准备就绪



查看etcd集群的状态

```bash
source /etc/etcd.env
```

```bash
etcdctl --endpoints=${ETCD_LISTEN_CLIENT_URLS} \
--ca-file=${ETCD_TRUSTED_CA_FILE} \
--cert-file=${ETCD_CERT_FILE} \
--key-file=${ETCD_KEY_FILE} \
cluster-health
```

显示每个节点的状态为 member xxxxx is healthy 则说明etcd集群正常启动



### 3.5 检查集群状态

登录KubeSphere, 



查看节点状态

进入 平台 - 集群管理 - 节点 - 集群节点 

查看健康状态, 当所有的节点的健康状态为正常时表示集群已经正常启动



查看 etcd 状态

监控和报警 - 集群状态  查看  ETCD监控 / APIServer 监控 / Scheduler监控 ,确认各服务启动正常,说明会集群已启动完毕



### 3.6 常见问题排查

1 etcd集群启动失败

先检查docker服务状态,docker服务启动异常,请先启动docker服务

检查etcd服务状态, 如果因为etcd数据问题导致启动失败 ,需要 恢复etcd数据, 参考 k8s集群_备份与恢复



2 部分节点加入集群失败

先检查节点所在服务器状态, 服务器网络状态, 防火墙规则, 检查节点的docker服务, kubelet服务状态



3 部分pod不断重启

集群重启后,集群内的所有pod都会重启,如果一个pod启动依赖其他的pod,就会出现该pod不断重启的问题,直到其依赖的pod启动完成, 可以查看pod的日志定位pod重启的原因

