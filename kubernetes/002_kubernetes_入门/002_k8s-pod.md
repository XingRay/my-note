### k8s-pod

pod时运行中的一组容器，pod时k8s中应用的最小单位。k8s不直接操作容器，而是把一个或者多个容器封装成一个pod，k8s操作和管理pod，启动、停止、删除 pod

示例：创建一个pod，里面通过nginx镜像运行一个容器：

```bash
kubectl run mynginx --image=nginx
```

参数说明： pod的名字为 mynginx，使用的镜像为 nginx

类似于 `docker run` 命令。等一段时间后，这个新创建的pod就运行起来了

```bash
kubectl get pod
NAME      READY   STATUS    RESTARTS   AGE
mynginx   1/1     Running   0          104s
```

如果运行出现异常，可以使用：

```bash
kubectl describe pod <pod-name> -n <namespace>
```

```bash
kubectl describe pod mynginx -n default
```

查看运行的情况。

```bash
kubectl describe pod mynginx -n default
Name:             mynginx
Namespace:        default
Priority:         0
Service Account:  default
Node:             k8s-node02/192.168.0.114
Start Time:       Mon, 19 Jun 2023 14:10:12 +0800
Labels:           run=mynginx
Annotations:      <none>
Status:           Running
IP:               10.244.2.3
IPs:
  IP:  10.244.2.3
Containers:
  mynginx:
    Container ID:   containerd://d666365cbce20e4c5200fcf25463fb65b9ccffafd75b14e3fe0194b2b12e71c5
    Image:          nginx
    Image ID:       docker.io/library/nginx@sha256:593dac25b7733ffb7afe1a72649a43e574778bf025ad60514ef40f6b5d606247
    Port:           <none>
    Host Port:      <none>
    State:          Running
      Started:      Mon, 19 Jun 2023 14:11:11 +0800
    Ready:          True
    Restart Count:  0
    Environment:    <none>
    Mounts:
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-krqnm (ro)
Conditions:
  Type              Status
  Initialized       True
  Ready             True
  ContainersReady   True
  PodScheduled      True
Volumes:
  kube-api-access-krqnm:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       <nil>
    DownwardAPI:             true
QoS Class:                   BestEffort
Node-Selectors:              <none>
Tolerations:                 node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type    Reason     Age    From               Message
  ----    ------     ----   ----               -------
  Normal  Scheduled  4m11s  default-scheduler  Successfully assigned default/mynginx to k8s-node02
  Normal  Pulling    4m11s  kubelet            Pulling image "nginx"
  Normal  Pulled     3m12s  kubelet            Successfully pulled image "nginx" in 58.775022403s (58.775072634s including waiting)
  Normal  Created    3m12s  kubelet            Created container mynginx
  Normal  Started    3m12s  kubelet            Started container mynginx
```



删除pod

```
kubectl delete pod <pod-name> -n <namespace>
```

```bash
kubectl delete pod mynginx -n default
```



在k8s中，pod也是一种资源，可以通过命令行创建，也可以通过配置文件创建和删除。创建一个yaml文件

```yaml
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: mynginx
  name: mynginx
#  namespace: default
spec:
  containers:
  - image: nginx
    name: mynginx
```

```bash
vi pod-mynginx.yaml
```

将上述内容复制到文件，保存后执行：

```bash
kubectl apply -f pod-mynginx.yaml
```

创建pod。也可以通过配置文件删除pod

```bash
kubectl delete -f pod-mynginx.yaml
```



还可以在dashboard中通过web-ui创建

在右上角点击 “ + ” 按钮，创建资源。选择 输入并创建，输入

```bash
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: mynginx
  name: mynginx
#  namespace: default
spec:
  containers:
  - image: nginx
    name: mynginx
```

此时名称空间未选择的情况下点击上传，会提示：

```bash
Deploying file has failed
the server does not allow this method on the requested resource
```

原因是创建pod必须指定namespace，可以在dashboard页面上选择要部署的namespace，也可以在配置文件中指定namespace

```bash
namespace: default
```

如果提示

```bash
Deploying file has failed Unauthorized
```

则需要退出当前账号，通过指令

```bash
kubectl -n kubernetes-dashboard create token admin-user
```

重新生成token再登录即可。



点击上传后即可创建pod，pod创建完成后可以通过点击pod的名字进入详情页，可以看到pod创建的详细信息。



查看pod运行的log

```bash
kubectl logs <pod-name>
```

```
kubectl logs -f <pod-name>
```

如：

```bash
kubectl logs mynginx
```

在dashboard中，在pod列表右侧点击 ... ，在菜单中点击 日志 ，即可看到运行的日志



查看更加完善的信息

```bash
kubectl get pod -o wide
```

可以将 -o  wide 连在一起

```bash
kubectl get pod -owide
```

输出：

```
kubectl get pod -owide
NAME      READY   STATUS    RESTARTS   AGE   IP           NODE         NOMINATED NODE   READINESS GATES
mynginx   1/1     Running   0          23m   10.244.2.4   k8s-node02   <none>           <none>
```

通过curl指令访问nginx，nginx

```bash
curl 10.244.2.4
```

每个pod，k8s都会分配一个ip，要查看这个ip就需要使用

```bash
kubectl get pod -owide
```

查看IP，再加上port就可以访问容器了。



如果想要修改nginx的html页面，那么需要进入容器执行命令：

```bash
kubectl exec -it <pod-name> -n <namespace> -- /bin/bash
```

或者

```bash
kubectl exec -it <pod-name> -n <namespace> -- /bin/sh
```

如：

```
kubectl exec -it mynginx -n default -- /bin/bash
```

类似于

```bash
docker exec -it 
```



可以通过指令进入pod：

```bash
kubectl exec -it mynginx -- /bin/bash
root@mynginx:/# ls
bin  boot  dev  docker-entrypoint.d  docker-entrypoint.sh  etc  home  lib  lib32  lib64  libx32  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
```

```bash
cd /usr/share/nginx/html/
root@mynginx:/usr/share/nginx/html# ls
50x.html  index.html
```

在 `index.html` 输入 11111

```bash
echo "11111" > index.html
exit
```

再次访问

```
root@k8s-master01:~# kubectl get pod -owide
NAME      READY   STATUS    RESTARTS   AGE   IP           NODE         NOMINATED NODE   READINESS GATES
mynginx   1/1     Running   0          56m   10.244.2.4   k8s-node02   <none>           <none>

root@k8s-master01:~# curl 10.244.2.4
11111
```





可以在一个pod中部署多个容器，例如：

```bash
vi multi-container-pod.yaml
```

```yaml
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: myapp
  name: myapp
spec:
  containers:
  - image: nginx
    name: nginx
  - image: tomcat:8.5.68
    name: tomcat
```

```bash
kubectl apply -f multi-container-pod.yaml
```

等待运行起来后，可以通过指令查看pod的ip地址

```bash
kubectl get pod -owide
NAME    READY   STATUS    RESTARTS   AGE   IP           NODE         NOMINATED NODE   READINESS GATES
myapp   2/2     Running   0          33m   10.244.1.4   k8s-node01   <none>           <none>
```

此时在集群的任意一个节点通过指令：

```bash
curl 10.244.1.4
```

```bash
curl 10.244.1.4:8080
```

即可访问nginx和tomcat容器，由于此时nginx和tomcat在同一个容器内，因此nginx可以直接通过 localhost 直接访问tomcat

通过dashboard进入nginx容器内执行：

```bash
curl 127.0.0.1:8080
```

```bash
curl localhost:8080
```

可以看到在nginx容器可以直接访问tomcat



在k8s内，一个pod就相当于一台虚拟机，在一个pod内的两个容器如果占用相同的端口，则会导致运行失败。

例如部署下列的pod会导致运行失败

```yaml
apiVersion: v1
kind: Pod
metadata:
  labels:
    run: myapp-02
  name: myapp-02
spec:
  containers:
  - image: nginx
    name: nginx01
  - image: nginx
    name: nginx02
```



