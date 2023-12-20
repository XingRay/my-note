## k8s-deployment



使用pod部署功能比较单一，做不到多副本、自愈、扩缩容等功能。因此需要使用`deployment`

使用 `kubectl run` 可以部署1个pod，但是如果某个应用、服务需要多个实例、副本，这样就不合适了。这时就可以使用 deployment



### 1 deployment 的自愈能力

对比 pod 和 deployment的区别：

```bash
kubectl run mynginx --image=nginx
```

```bash
kubectl create deployment mytomcat --image=tomcat:8.5.68
```

等待部署完成，

```bash
root@k8s-master01:~# kubectl get pod
NAMESPACE              NAME                                         READY   STATUS    RESTARTS       AGE
default                mynginx                                      1/1     Running   0              119s
default                mytomcat-675657766b-r5p8c                    1/1     Running   0              95s
```

此时再执行：

```bash
kubectl delete pod mynginx
```

可以看到nginx这个pod确实被删除了

```bash
kubectl get pod
NAME                        READY   STATUS    RESTARTS   AGE
mytomcat-675657766b-r5p8c   1/1     Running   0          3m28s
```

再执行：

```bash
kubectl delete pod mytomcat-675657766b-r5p8c
```

```bash
root@k8s-master01:~# kubectl delete pod mytomcat-675657766b-r5p8c
pod "mytomcat-675657766b-r5p8c" deleted
root@k8s-master01:~# kubectl get pod
NAME                        READY   STATUS    RESTARTS   AGE
mytomcat-675657766b-5mjsj   1/1     Running   0          4s
```

会看到删除这个pod后，k8s会立即启动一个新的pod，这个就是deployment与pod的一个区别。使用deployment部署的pod，在应用崩溃，服务器宕机等情况下会有自愈能力。

如果确实要删除这次deployment，需要执行：

```bash
kubectl get deploy
```

```bash
kubectl get deploy
NAME       READY   UP-TO-DATE   AVAILABLE   AGE
mytomcat   1/1     1            1           9m28s
```

找到这次部署，再执行

```bash
kubectl delete deploy <deployment-name>
```

```bash
kubectl delete deploy mytomcat
```



### 2 deployment的多副本

```bash
kubectl create deployment <deployment-name> --image=<image-name> --replicas=<replicas-num>
```

```
kubectl create deployment my-dep --image=nginx --replicas=3
```

部署3个nginx的pod

```bash
root@k8s-master01:~# kubectl create deployment my-dep --image=nginx --replicas=3
deployment.apps/my-dep created
root@k8s-master01:~# kubectl get deploy
NAME     READY   UP-TO-DATE   AVAILABLE   AGE
my-dep   3/3     3            3           23s
```

```bash
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-94pfr   1/1     Running   0          65s
my-dep-5688dd958f-gph67   1/1     Running   0          65s
my-dep-5688dd958f-qpp2d   1/1     Running   0          65s
```

```bash
root@k8s-master01:~# kubectl get pod -owide
NAME                      READY   STATUS    RESTARTS   AGE   IP           NODE         NOMINATED NODE   READINESS GATES
my-dep-5688dd958f-94pfr   1/1     Running   0          97s   10.244.2.6   k8s-node02   <none>           <none>
my-dep-5688dd958f-gph67   1/1     Running   0          97s   10.244.1.7   k8s-node01   <none>           <none>
my-dep-5688dd958f-qpp2d   1/1     Running   0          97s   10.244.2.7   k8s-node02   <none>           <none>
```

在dashboard操作deployment很方便，在dashboard中，点击左侧的deployment就可以看到deployment列表。可以点击删除，也可以点击右上角 + 号，选择从表单创建。

```bash
应用名称： my-dep-01
容器镜像： nginx
pod的数量： 5
service： none
命名空间： default
```

点击 部署即可。pod正常运行后查看pod信息：

```bash
root@k8s-master01:~# kubectl get pod -owide
NAME                        READY   STATUS    RESTARTS   AGE   IP            NODE         NOMINATED NODE   READINESS GATES
my-dep-01-bf65b4976-ljsmb   1/1     Running   0          53s   10.244.2.9    k8s-node02   <none>           <none>
my-dep-01-bf65b4976-t2c2l   1/1     Running   0          53s   10.244.2.10   k8s-node02   <none>           <none>
my-dep-01-bf65b4976-xbrdf   1/1     Running   0          53s   10.244.1.8    k8s-node01   <none>           <none>
my-dep-01-bf65b4976-zpg7f   1/1     Running   0          53s   10.244.1.9    k8s-node01   <none>           <none>
my-dep-01-bf65b4976-zrp7g   1/1     Running   0          53s   10.244.2.8    k8s-node02   <none>           <none>
```



除了使用命令行和dashboard外，还可以使用配置文件，如：

```bash
vi my-dep.yaml
```



```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: my-dep
  name: my-dep
spec:
  replicas: 3
  selector:
    matchLabels:
      app: my-dep
  template:
    metadata:
      labels:
        app: my-dep
    spec:
      containers:
      - image: nginx
        name: nginx
```

注意，这里配置了 replicas: 3，即部署3个副本。

```bash
kubectl apply -f my-dep.yaml
```

```bash
root@k8s-master01:~# kubectl get pod -owide
NAME                      READY   STATUS    RESTARTS   AGE   IP            NODE         NOMINATED NODE   READINESS GATES
my-dep-5688dd958f-4rwrp   1/1     Running   0          23s   10.244.2.11   k8s-node02   <none>           <none>
my-dep-5688dd958f-7dtvz   1/1     Running   0          23s   10.244.2.12   k8s-node02   <none>           <none>
my-dep-5688dd958f-kr4zt   1/1     Running   0          23s   10.244.1.10   k8s-node01   <none>           <none>
```

删除deployment

```bash
kubectl delete -f my-dep.yaml
```



### deployment扩缩容

kubectl scale

应用负载高时，可以增加pod，这个叫做扩容，当负载降低时，可以删除几个pod，这个叫做缩容。k8s可以根据一定的条件自动进行扩缩容。

先部署一个deployment

```
kubectl apply -f my-dep.yaml
```

```bash
root@k8s-master01:~# kubectl apply -f my-dep.yaml
deployment.apps/my-dep created
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-ph5f7   1/1     Running   0          6s
my-dep-5688dd958f-tgt7q   1/1     Running   0          6s
my-dep-5688dd958f-xvf7f   1/1     Running   0          6s
```

默认是部署了3个pod，现在进行手动扩容：

```bash
kubectl scale deploy/<deployment-name> --replicas=<replicas-num>
```

```bash
kubectl scale deploy/my-dep --replicas=5
```

```bash
root@k8s-master01:~# kubectl scale deploy/my-dep --replicas=5
deployment.apps/my-dep scaled
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-gn97q   1/1     Running   0          9s
my-dep-5688dd958f-ph5f7   1/1     Running   0          2m47s
my-dep-5688dd958f-tgt7q   1/1     Running   0          2m47s
my-dep-5688dd958f-vx59q   1/1     Running   0          9s
my-dep-5688dd958f-xvf7f   1/1     Running   0          2m47s
```

可以看到此时pod有5个。再手动缩容

```bash
kubectl scale deploy/my-dep --replicas=2
```

```bash
root@k8s-master01:~# kubectl scale deploy/my-dep --replicas=2
deployment.apps/my-dep scaled
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-tgt7q   1/1     Running   0          3m56s
my-dep-5688dd958f-xvf7f   1/1     Running   0          3m56s
```

可以看到此时pod只有2个。还可以通过修改deployment的配置文件来进行扩缩容：

```bash
kubectl edit deploy <deployment-name>
```

```bash
kubectl edit deploy my-dep
```

进入deployment的编辑模式，找到

```yaml
spec:
  progressDeadlineSeconds: 600
  replicas: 2
```

修改 replicas 的值，如：

```yaml
spec:
  progressDeadlineSeconds: 600
  replicas: 4
```

保存退出后：

```bash
root@k8s-master01:~# kubectl edit deploy my-dep
deployment.apps/my-dep edited
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-97cw8   1/1     Running   0          15s
my-dep-5688dd958f-tgt7q   1/1     Running   0          8m25s
my-dep-5688dd958f-xvf7f   1/1     Running   0          8m25s
my-dep-5688dd958f-zcqzh   1/1     Running   0          15s
```

可以看到pod根据设置扩容成了4个。

在dashboard中扩缩容也很简单。在deployments页面，在列表中点击 ... ，点击scale，在弹出的窗口中选择要扩缩容的pod数量即可。





### deployment自愈和故障转移

当k8s在运行过程中，某个pod故障了，k8s会尝试重启这个pod修复这个故障，这个就是k8s系统具有的**自愈能力**。

另外有可能某个节点宕机了，pod无法重启，这时k8s可能感知到这个节点下线，k8s会把这个节点上运行的所有的pod在其他的节点再启动，这个就是k8s的**故障转移能力**

示例：现在已经有一个deployment

```bash
root@k8s-master01:~# kubectl get pod -owide
NAME                      READY   STATUS    RESTARTS   AGE     IP            NODE         NOMINATED NODE   READINESS GATES
my-dep-5688dd958f-97cw8   1/1     Running   0          8m55s   10.244.2.16   k8s-node02   <none>           <none>
my-dep-5688dd958f-tgt7q   1/1     Running   0          17m     10.244.1.11   k8s-node01   <none>           <none>
my-dep-5688dd958f-xvf7f   1/1     Running   0          17m     10.244.1.12   k8s-node01   <none>           <none>
my-dep-5688dd958f-zcqzh   1/1     Running   0          8m55s   10.244.2.17   k8s-node02   <none>           <none>
```

可以看到node01和node02分别部署了2个pod，现在把node01关机。一段时间之后，可以看到在node02上运行了4个pod

```bash
root@k8s-master01:~# kubectl get pod -owide
NAME                      READY   STATUS        RESTARTS   AGE   IP            NODE         NOMINATED NODE   READINESS GATES
my-dep-5688dd958f-97cw8   1/1     Running       0          35m   10.244.2.16   k8s-node02   <none>           <none>
my-dep-5688dd958f-c845s   1/1     Running       0          17m   10.244.2.19   k8s-node02   <none>           <none>
my-dep-5688dd958f-tgt7q   1/1     Terminating   0          43m   10.244.1.11   k8s-node01   <none>           <none>
my-dep-5688dd958f-xvf7f   1/1     Terminating   0          43m   10.244.1.12   k8s-node01   <none>           <none>
my-dep-5688dd958f-zcqzh   1/1     Running       0          35m   10.244.2.17   k8s-node02   <none>           <none>
my-dep-5688dd958f-zrhc9   1/1     Running       0          17m   10.244.2.20   k8s-node02   <none>           <none>
```

再次启动node01，之前node01上的pod会清除掉。

```bash
root@k8s-master01:~# kubectl get pod -owide
NAME                      READY   STATUS    RESTARTS   AGE   IP            NODE         NOMINATED NODE   READINESS GATES
my-dep-5688dd958f-97cw8   1/1     Running   0          38m   10.244.2.16   k8s-node02   <none>           <none>
my-dep-5688dd958f-c845s   1/1     Running   0          21m   10.244.2.19   k8s-node02   <none>           <none>
my-dep-5688dd958f-zcqzh   1/1     Running   0          38m   10.244.2.17   k8s-node02   <none>           <none>
my-dep-5688dd958f-zrhc9   1/1     Running   0          21m   10.244.2.20   k8s-node02   <none>           <none>
```



### deployment滚动更新

假设现在pod部署的应用的v1版本，现在需要升级到v2版本。现在不间断的还有外部的流量进入系统（用户的请求），想要在不间断流量的同时更新pod的应用的版本，就需要先启动一个v2版本的pod，启动成功后流量转到这个新启动额pod，再下线一个v1版本的pod。然后再以此类推，逐个将所有的v1版本升级到v2。全程不停机更新，也是滚动更新。

查看现有的部署的deployment，以yaml形式输出：

```bash
kubectl get deploy <deployment-name> -oyaml
```

```
kubectl get deploy my-dep -oyaml
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  annotations:
    deployment.kubernetes.io/revision: "1"
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"apps/v1","kind":"Deployment","metadata":{"annotations":{},"labels":{"app":"my-dep"},"name":"my-dep","namespace":"default"},"spec":{"replicas":3,"selector":{"matchLabels":{"app":"my-dep"}},"template":{"metadata":{"labels":{"app":"my-dep"}},"spec":{"containers":[{"image":"nginx","name":"nginx"}]}}}}
  creationTimestamp: "2023-06-19T09:41:24Z"
  generation: 4
  labels:
    app: my-dep
  name: my-dep
  namespace: default
  resourceVersion: "34377"
  uid: 3c6651c8-f8e8-4c66-aaa1-986f035fd808
spec:
  progressDeadlineSeconds: 600
  replicas: 4
  revisionHistoryLimit: 10
  selector:
    matchLabels:
      app: my-dep
  strategy:
    rollingUpdate:
      maxSurge: 25%
      maxUnavailable: 25%
    type: RollingUpdate
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: my-dep
    spec:
      containers:
      - image: nginx
        imagePullPolicy: Always
        name: nginx
        resources: {}
        terminationMessagePath: /dev/termination-log
        terminationMessagePolicy: File
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      schedulerName: default-scheduler
      securityContext: {}
      terminationGracePeriodSeconds: 30
status:
  availableReplicas: 4
  conditions:
  - lastTransitionTime: "2023-06-19T09:41:24Z"
    lastUpdateTime: "2023-06-19T09:41:28Z"
    message: ReplicaSet "my-dep-5688dd958f" has successfully progressed.
    reason: NewReplicaSetAvailable
    status: "True"
    type: Progressing
  - lastTransitionTime: "2023-06-19T10:07:17Z"
    lastUpdateTime: "2023-06-19T10:07:17Z"
    message: Deployment has minimum availability.
    reason: MinimumReplicasAvailable
    status: "True"
    type: Available
  observedGeneration: 4
  readyReplicas: 4
  replicas: 4
  updatedReplicas: 4
```

可以查看使用的镜像

```bash
	spec:
      containers:
      - image: nginx
```

镜像为nginx的最新版本。更新前先查看pod信息

```bash
kubectl get pod -w
```

```bash
root@k8s-master01:~# kubectl get pod -w
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-97cw8   1/1     Running   0          48m
my-dep-5688dd958f-c845s   1/1     Running   0          31m
my-dep-5688dd958f-zcqzh   1/1     Running   0          48m
my-dep-5688dd958f-zrhc9   1/1     Running   0          31m
```

开始滚动更新：

```bash
kubectl set image deploy/<deployment-name> <old-image-name>=<new-image-name> --record
```

```bash
kubectl set image deploy/my-dep nginx=nginx:1.16.1 --record
```

参数：--record 升级的过程将会被记录

```bash
root@k8s-master01:~# kubectl get pod -w
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-97cw8   1/1     Running   0          55m
my-dep-5688dd958f-c845s   1/1     Running   0          38m
my-dep-5688dd958f-zcqzh   1/1     Running   0          55m
my-dep-5688dd958f-zrhc9   1/1     Running   0          38m
my-dep-64d4bc84-j8bjd     0/1     Pending   0          0s
my-dep-5688dd958f-zrhc9   1/1     Terminating   0          39m
my-dep-64d4bc84-j8bjd     0/1     Pending       0          0s
my-dep-64d4bc84-j8bjd     0/1     ContainerCreating   0          0s
my-dep-64d4bc84-d2xsx     0/1     Pending             0          0s
my-dep-64d4bc84-d2xsx     0/1     Pending             0          0s
my-dep-64d4bc84-d2xsx     0/1     ContainerCreating   0          0s
my-dep-5688dd958f-zrhc9   0/1     Terminating         0          39m
my-dep-5688dd958f-zrhc9   0/1     Terminating         0          39m
my-dep-5688dd958f-zrhc9   0/1     Terminating         0          39m
my-dep-5688dd958f-zrhc9   0/1     Terminating         0          39m
my-dep-64d4bc84-d2xsx     1/1     Running             0          18s
my-dep-5688dd958f-c845s   1/1     Terminating         0          39m
my-dep-64d4bc84-d2dc8     0/1     Pending             0          0s
my-dep-64d4bc84-d2dc8     0/1     Pending             0          0s
my-dep-64d4bc84-d2dc8     0/1     ContainerCreating   0          0s
my-dep-5688dd958f-c845s   0/1     Terminating         0          39m
my-dep-5688dd958f-c845s   0/1     Terminating         0          39m
my-dep-5688dd958f-c845s   0/1     Terminating         0          39m
my-dep-5688dd958f-c845s   0/1     Terminating         0          39m
my-dep-64d4bc84-j8bjd     1/1     Running             0          23s
my-dep-5688dd958f-97cw8   1/1     Terminating         0          56m
my-dep-64d4bc84-w2gcj     0/1     Pending             0          0s
my-dep-64d4bc84-w2gcj     0/1     Pending             0          0s
my-dep-64d4bc84-w2gcj     0/1     ContainerCreating   0          0s
my-dep-5688dd958f-97cw8   0/1     Terminating         0          56m
my-dep-5688dd958f-97cw8   0/1     Terminating         0          56m
my-dep-5688dd958f-97cw8   0/1     Terminating         0          56m
my-dep-5688dd958f-97cw8   0/1     Terminating         0          56m
my-dep-64d4bc84-d2dc8     1/1     Running             0          6s
my-dep-5688dd958f-zcqzh   1/1     Terminating         0          56m
my-dep-5688dd958f-zcqzh   0/1     Terminating         0          56m
my-dep-5688dd958f-zcqzh   0/1     Terminating         0          56m
my-dep-5688dd958f-zcqzh   0/1     Terminating         0          56m
my-dep-5688dd958f-zcqzh   0/1     Terminating         0          56m
my-dep-64d4bc84-w2gcj     1/1     Running             0          2s
```

升级完成后：

```bash
root@k8s-master01:~# kubectl get pod -owide
NAME                    READY   STATUS    RESTARTS   AGE     IP            NODE         NOMINATED NODE   READINESS GATES
my-dep-64d4bc84-d2dc8   1/1     Running   0          2m19s   10.244.1.14   k8s-node01   <none>           <none>
my-dep-64d4bc84-d2xsx   1/1     Running   0          2m37s   10.244.2.21   k8s-node02   <none>           <none>
my-dep-64d4bc84-j8bjd   1/1     Running   0          2m37s   10.244.1.13   k8s-node01   <none>           <none>
my-dep-64d4bc84-w2gcj   1/1     Running   0          2m14s   10.244.2.22   k8s-node02   <none>           <none>
```





### deployment版本回退

查看deployment的部署记录

```bash
kubectl rollout history deployment/<deployment-name>
```

查看my-dep的部署历史记录：

```bash
kubectl rollout history deployment/my-dep
```

```
root@k8s-master01:~# kubectl rollout history deployment/my-dep
deployment.apps/my-dep
REVISION  CHANGE-CAUSE
1         <none>
2         kubectl set image deploy/my-dep nginx=nginx:1.16.1 --record=true
```

可以看到有2个版本，revision为1和2， CHANGE-CAUSE 版本改变原因，会记录升级版本的指令。

回滚部署：

```bash
kubectl rollout undo deploy/<deployment-name> --to-revision=<target-reversion>
```

现在将这次部署回滚到 reversion为1的状态：

```bash
kubectl rollout undo deploy/my-dep --to-revision=1
```

```bash
root@k8s-master01:~# kubectl rollout undo deploy/my-dep --to-revision=1
deployment.apps/my-dep rolled back
```

查看pod

```bash
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-fd9cm   1/1     Running   0          33s
my-dep-5688dd958f-hmh8f   1/1     Running   0          36s
my-dep-5688dd958f-j57l5   1/1     Running   0          36s
my-dep-5688dd958f-stkdj   1/1     Running   0          33s
```

这时在输出一次deploy的信息，使用yaml格式：

```bash
kubectl get deploy/my-dep -oyaml | grep image
```

```bash
root@k8s-master01:~# kubectl get deploy/my-dep -oyaml | grep image
      {"apiVersion":"apps/v1","kind":"Deployment","metadata":{"annotations":{},"labels":{"app":"my-dep"},"name":"my-dep","namespace":"default"},"spec":{"replicas":3,"selector":{"matchLabels":{"app":"my-dep"}},"template":{"metadata":{"labels":{"app":"my-dep"}},"spec":{"containers":[{"image":"nginx","name":"nginx"}]}}}}
      - image: nginx
        imagePullPolicy: Always
```

可以看到 image已经是 nginx了。再次查看部署历史：

```bash
root@k8s-master01:~# kubectl rollout history deployment/my-dep
deployment.apps/my-dep
REVISION  CHANGE-CAUSE
2         kubectl set image deploy/my-dep nginx=nginx:1.16.1 --record=true
3         <none>
```



### k8s工作负载

在k8s中，除了deployment外，还有Stateful、DaemonSet、Job等类型资源，统称为工作负载。

有状态的应用使用 StatefulSet部署，无状态的应用使用 Deployment部署。



Deployment **无状态**应用部署，比如微服务，提供多副本等功能。

StatefulSet：**有状态**应用部署，比如redis，提供稳定的存储、网络等功能。（稳定的网络，比如提供固定的ip地址，另外一般数据挂载在外部，也就是稳定存储）

DaemonSet：**守护型**应用部署，比如日志收集组件，在每个机器中运行一份。

Job/CronJob：**定时任务**部署，比如垃圾清理组件，可以在指定的时间运行。



在k8s的实际使用中，不会直接部署pod，虽然pod才是应用真正的载体，但是一般是使用工作负载来控制pod，让每个pod具有比原先更强大的功能。





