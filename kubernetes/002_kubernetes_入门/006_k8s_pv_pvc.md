## k8s-pv和pvc

nfs 网络存储系统。NFS服务是一个在linux间共享文件的功能。比如A服务器有文件，B，C，D都想访问这个文件，就可以把A服务上的文件共享出来。NFS就是实现这个功能的。



### 搭建nfs

选择一个节点作为nfs服务器，其他的为客户端

服务器A是192.168.0.112　　作为服务端

服务器B是192.168.0.113　　作为客户端

服务器C是192.168.0.114　　作为客户端



#### 搭建服务端

在服务器A上安装nfs，安装 NFS服务器端

```bash
sudo apt-get install -y nfs-kernel-server
```



启动服务

```bash
sudo systemctl enable nfs-server
sudo systemctl start nfs-server
```



查看服务

```bash
sudo systemctl status nfs-server
```



创建一个共享文件夹，增加权限

```bash
sudo mkdir -p /nfs/data
```

打开配置参数文件，设置配置文件

```bash
sudo vim /etc/exports
```

输入设置参数，允许192.168下所有网段都可以访问

```bash
/nfs/data 192.168.0.0/24(insecure,rw,no_root_squash,sync)
```

注意：网段不能写成 `192.168.*.*` 这样写会导致无法挂载

保存并退出

参数说明：

| 参数           | 作用                                                         |
| -------------- | ------------------------------------------------------------ |
| ro             | 只读                                                         |
| rw             | 读写                                                         |
| root_squash    | 当NFS客户端以root管理员访问时，映射为NFS服务器的匿名用户     |
| no_root_squash | 当NFS客户端以root管理员访问时，映射为NFS服务器的root管理员   |
| all_squash     | 无论NFS客户端使用什么账户访问，均映射为NFS服务器的匿名用户   |
| sync           | 同时将数据写入到内存与硬盘中，保证不丢失数据                 |
| async          | 优先将数据保存到内存，然后再写入硬盘，这样效率高，但是可能会丢失数据 |

重启nfs服务

```bash
sudo systemctl restart nfs-server
```



#### 配置客户端

客户端安装nfs工具

```bash
sudo apt-get install -y nfs-common
```

查看服务器的挂载点

```bash
showmount -e 192.168.0.112
```

创建文件目录

```bash
sudo mkdir -p /nfs/data
```

挂载

```bash
sudo mount -t nfs 192.168.0.112:/nfs/data /nfs/data
```

查看挂载结果

```bash
df -h
```

```bash
root@k8s-node01:~# df -h
Filesystem               Size  Used Avail Use% Mounted on
tmpfs                    388M  2.7M  386M   1% /run
/dev/sda2                 40G  9.6G   28G  26% /
tmpfs                    1.9G     0  1.9G   0% /dev/shm
tmpfs                    5.0M     0  5.0M   0% /run/lock
192.168.0.112:/nfs/data   40G  9.2G   28G  25% /nfs/data
```

可以看到已经成功挂载 /nfs/data

取消挂载

```bash
sudo umount /nfs/data
```





### 使用原生的方式挂载

nfs服务器节点运行：

```bash
mkdir -p /nfs/data/nginx-pv
```

注意要先创建好挂载的目录，否则会导致容器无法正常启动

```bash
vi nginx-pv-demo.yaml
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: nginx-pv-demo
  name: nginx-pv-demo
spec:
  replicas: 2
  selector:
    matchLabels:
      app: nginx-pv-demo
  template:
    metadata:
      labels:
        app: nginx-pv-demo
    spec:
      containers:
      - image: nginx
        name: nginx
        volumeMounts:
        - name: html
          mountPath: /usr/share/nginx/html
      volumes:
        - name: html
          nfs:
            server: 192.168.0.112
            path: /nfs/data/nginx-pv
```

参数说明：

```yaml
        volumeMounts:
        - name: html
          mountPath: /usr/share/nginx/html
```

有文件挂载，这个挂载名为 html，挂载的路径为 /usr/share/nginx/html ，即将容器内部的 /usr/share/nginx/html 目录挂载到外面

```yaml
      volumes:
        - name: html
          nfs:
            server: 192.168.0.112
            path: /nfs/data/nginx-pv
```

挂载列表，有一个名为html的数据卷，类型为nfs， 服务器地址为 192.168.0.112， 挂载的目录是nfs服务器中的 /nfs/data/nginx-pv

执行:

```bash
kubectl apply -f nginx-pv-demo.yaml
```

运行成功后，在nfs服务器节点的挂载目录  /nfs/data/nginx-pv 中修改、添加文件：

```bash
echo 111222 > index.html
```

查看pod信息：

```bash
root@k8s-master01:/nfs/data/nginx-pv# kubectl get pod -owide
NAME                             READY   STATUS    RESTARTS   AGE     IP            NODE         NOMINATED NODE   READINESS GATES
nginx-pv-demo-794c76488d-2b8t2   1/1     Running   0          7m3s    10.244.2.32   k8s-node02   <none>           <none>
nginx-pv-demo-794c76488d-n868m   1/1     Running   0          10m     10.244.1.28   k8s-node01   <none>           <none>
```

在节点上通过ip进行访问：

```bash
root@k8s-master01:/nfs/data/nginx-pv# curl 10.244.2.32
111222
root@k8s-master01:/nfs/data/nginx-pv# curl 10.244.1.28
111222
```

可以看到2个pod返回的数据都是在挂载的数据卷中的内容。



#### 使用原生方式挂载的问题

1 挂载的文件夹需要手动创建

2 pod确定不再需要，pod被删除后，挂载的目录不会自动清除

3 对于各个pod挂载的目录使用的磁盘空间没有限制





### PV和PVC

PV：持久卷（**Persistent Volume**），将应用需要持久化的数据保存到指定位置

PVC：持久卷申明（**Persistent Volume Claim**），申明需要使用的持久卷规格



简单来说，持久卷就是想要挂载的目录，在该目录中持久化保持数据。如上面的 /nfs/data/nginx-pv ,PV可以预先设置好给定容量大小。不同于原生挂载直接将pod与目录绑定， 使用pvc可以在pod需要使用pv时提交一份申请，申请需要多少存储空间，这个申请书就是pvc。这样一个申请书（pvc）与一个实际的存储空间进行绑定，当pod被删除时，可以将申请书（pvc）一起删除，pvc被删除时，会连同pv一起回收。



#### 静态供应、pv池

可以在各个节点上预先开辟好不同大小的pv，如 20M 1G 5G 10G 50G 等不同容量的空间，形成一个**pv池**。这种提前规划创建好pv池的方式称为**静态供应**。 当pod需要挂载数据时，可以pod上写一个申请书（pvc），比如需要 1MB的空间，就在pv池中找一个合适的，比如 20M 的pv。当之前运行的pod某种原因下线之后，k8s在另外的节点重新启动一个新的pod时，pod中的申请书（pvc）不变，那么新启动的pod依然会挂载相同的pv。



#### 创建pv池

在nfs主节点执行：

```bash
mkdir -p /nfs/data/pv01
mkdir -p /nfs/data/pv02
mkdir -p /nfs/data/pv03
```

创建pv池配置文件

```bash
vi pv-pool.yaml
```

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv01-10m
spec:
  capacity:
    storage: 10M
  accessModes:
    - ReadWriteMany
  storageClassName: nfs
  nfs:
    path: /nfs/data/pv01
    server: 192.168.0.112
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv02-1gi
spec:
  capacity:
    storage: 1Gi
  accessModes:
    - ReadWriteMany
  storageClassName: nfs
  nfs:
    path: /nfs/data/pv02
    server: 192.168.0.112
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: pv03-3gi
spec:
  capacity:
    storage: 3Gi
  accessModes:
    - ReadWriteMany
  storageClassName: nfs
  nfs:
    path: /nfs/data/pv03
    server: 192.168.0.112
```

分别创建一个10M 1G 和 3G的pv， accessModes:    - ReadWriteMany 表示可读可写，多节点访问。storageClassName: nfs 这个可以自定义，但是需要对应好，后面会使用到。注意 name: pv02-1gi ，不要使用大写字母，统一使用小写字母。

```bash
kubectl apply -f pv-pool.yaml
```

```bash
root@k8s-master01:~# kubectl apply -f pv-pool.yaml
persistentvolume/pv01-10m created
persistentvolume/pv02-1gi created
persistentvolume/pv03-3gi created
```

查看数据卷

```bash
root@k8s-master01:~# kubectl get persistentvolume
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available           nfs                     5m16s
pv02-1gi   1Gi        RWX            Retain           Available           nfs                     5m16s
pv03-3gi   3Gi        RWX            Retain           Available           nfs                     5m16s
```

或者

```bash
kubectl get pv
```





#### 创建pvc

```bash
vi nginx-pvc.yaml
```

```yaml
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: nginx-pvc
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 200Mi
  storageClassName: nfs
```

参数说明

```
metadata:
  name: nginx-pvc
```

申请书的名字，可以自定义

```yaml
accessModes:
    - ReadWriteMany
```

访问模式要求是多节点可以读写的。

```yaml
resources:
    requests:
      storage: 200Mi
```

容量要求是200M

```yaml
storageClassName: nfs
```

**storageClassName**必须与创建pv池时设置的 storageClassName 保持一致。



创建pvc之前，显示pv：

```bash
root@k8s-master01:~# kubectl get persistentvolume
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available           nfs                     5m16s
pv02-1gi   1Gi        RWX            Retain           Available           nfs                     5m16s
pv03-3gi   3Gi        RWX            Retain           Available           nfs                     5m16s
```

创建pvc：

```bash
kubectl apply -f nginx-pvc.yaml
```

```bash
root@k8s-master01:~# kubectl apply -f nginx-pvc.yaml
persistentvolumeclaim/nginx-pvc created
```

创建完pvc之后再显示pv列表：

```bash
root@k8s-master01:~# kubectl get persistentvolume
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM               STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available                       nfs                     14m
pv02-1gi   1Gi        RWX            Retain           Bound       default/nginx-pvc   nfs                     14m
pv03-3gi   3Gi        RWX            Retain           Available                       nfs                     14m
```

注意 pv02-1gi 的状态由`Available`变为了`Bound`，表示被绑定。 CLAIM  为 `default/nginx-pvc`，表示被 `default/nginx-pvc`绑定。

此时删除pvc

```bash
kubectl delete -f nginx-pvc.yaml
```

```bash
root@k8s-master01:~# kubectl delete -f nginx-pvc.yaml
persistentvolumeclaim "nginx-pvc" deleted
root@k8s-master01:~# kubectl get persistentvolume
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM               STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available                       nfs                     17m
pv02-1gi   1Gi        RWX            Retain           Released    default/nginx-pvc   nfs                     17m
pv03-3gi   3Gi        RWX            Retain           Available                       nfs                     17m
```

可以看到 pv的状态为 `Released` ，表示已经被释放了。再次执行创建pvc

```bash
kubectl apply -f nginx-pvc.yaml
```

```bash
root@k8s-master01:~# kubectl apply -f nginx-pvc.yaml
persistentvolumeclaim/nginx-pvc created
root@k8s-master01:~# kubectl get persistentvolume
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM               STORAGECLASS   REASON   AGE
pv01-10m   10M        RWX            Retain           Available                       nfs                     18m
pv02-1gi   1Gi        RWX            Retain           Released    default/nginx-pvc   nfs                     18m
pv03-3gi   3Gi        RWX            Retain           Bound       default/nginx-pvc   nfs                     18m
```

注意此时pvc会重新绑定一个pv，由于 pv02-1gi 还没有完全清空，因此不能被再次绑定。

查看pvc：

```bash
root@k8s-master01:~# kubectl get persistentvolumeclaim
NAME        STATUS   VOLUME     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
nginx-pvc   Bound    pv03-3gi   3Gi        RWX            nfs            103s
```

或者

```bash
kubectl get pvc
```



一般在实际项目会在一个pod内部使用pvc，示例如下：

```bash
vi nginx-deploy-pvc.yaml
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: nginx-deploy-pvc
  name: nginx-deploy-pvc
spec:
  replicas: 2
  selector:
    matchLabels:
      app: nginx-deploy-pvc
  template:
    metadata:
      labels:
        app: nginx-deploy-pvc
    spec:
      containers:
      - image: nginx
        name: nginx
        volumeMounts:
        - name: html
          mountPath: /usr/share/nginx/html
      volumes:
        - name: html
          persistentVolumeClaim:
            claimName: nginx-pvc
```

注意：

```yaml
      volumes:
        - name: html
          persistentVolumeClaim:
            claimName: nginx-pvc
```

这里与原生挂载不同，使用的不再是nfs，而是 persistentVolumeClaim，也就是pvc，名为 nginx-pvc 的pvc已经创建好了，这里可以直接使用。使用pvc创建部署：

```bash
kubectl apply -f nginx-deploy-pvc.yaml
```

```bash
root@k8s-master01:~# kubectl apply -f nginx-deploy-pvc.yaml
deployment.apps/nginx-deploy-pvc created
root@k8s-master01:~# kubectl get pvc
NAME        STATUS   VOLUME     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
nginx-pvc   Bound    pv03-3gi   3Gi        RWX            nfs            7m56s
```

查看此时pv和pvc的情况：

```bash
root@k8s-master01:~# kubectl get pv,pvc
NAME                        CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM               STORAGECLASS   REASON   AGE
persistentvolume/pv01-10m   10M        RWX            Retain           Available                       nfs                     28m
persistentvolume/pv02-1gi   1Gi        RWX            Retain           Released    default/nginx-pvc   nfs                     28m
persistentvolume/pv03-3gi   3Gi        RWX            Retain           Bound       default/nginx-pvc   nfs                     28m

NAME                              STATUS   VOLUME     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
persistentvolumeclaim/nginx-pvc   Bound    pv03-3gi   3Gi        RWX            nfs            10m
```

当前pod使用的pvc为nginx-pvc， 绑定的pv为  persistentvolume/pv03-3gi，也就是主节点的 /nfs/data/pv03目录，主节点执行：

```bash
echo 111222333 > /nfs/data/pv03/index.html
```

查看pod信息：

```bash
root@k8s-master01:~# kubectl get pod -owide
NAME                                READY   STATUS    RESTARTS   AGE   IP            NODE         NOMINATED NODE   READINESS GATES
nginx-deploy-pvc-75d69c7754-2rxxb   1/1     Running   0          6m    10.244.1.29   k8s-node01   <none>           <none>
nginx-deploy-pvc-75d69c7754-7dgvq   1/1     Running   0          6m    10.244.2.34   k8s-node02   <none>           <none>
```

访问任意pod，如：

```bash
root@k8s-master01:~# curl 10.244.1.29
111222333
```

说明数据卷已经挂载成功了。并且这个数据卷具有容量限制，如果pod往该空间存储数据量过大时会报错。



#### pv池动态供应

在后续KubeSphere中进行说明，简单来说就是静态供应只能在已有的pvc池中进行选择使用哪个pv，动态供应就是在pod创建pvc时申请指定大小的空间，这时系统会在节点上**自动创建**指定大小的pv。并且自动绑定好，不需要手动操作绑定pv。



