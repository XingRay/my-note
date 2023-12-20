## k8s-service

servie时k8s用于做服务发现功能的，将一组pod公开为网络服务的抽象方法。

比如有一个xxx管理系统的服务，部署成了多个pod，然后有这个管理系统的vue项目，那么在vue项目中设置访问url设置为任何的pod的ip都是不对的，因为该pod可能会下线，这个时候就需要有个service为所有的pod统一暴露地址。vue项目只需要访问service，service会自动负载均衡到各个pod。并且如果某个pod下线，流量会经由service自动转给其他的pod。



测试：

查看现有的pod

```bash
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-fd9cm   1/1     Running   0          25m
my-dep-5688dd958f-hmh8f   1/1     Running   0          25m
my-dep-5688dd958f-j57l5   1/1     Running   0          25m
my-dep-5688dd958f-stkdj   1/1     Running   0          25m
```

现在有4个pod，通过dashboard，进入各个pod，修改 /usr/share/nginx/html/index.html

分别执行

```bash
echo 1111 > /usr/share/nginx/html/index.html
echo 2222 > /usr/share/nginx/html/index.html
echo 3333 > /usr/share/nginx/html/index.html
echo 4444 > /usr/share/nginx/html/index.html
```

修改完后每个pod的nginx首页输出都不一样。

```bash
root@k8s-master01:~# curl 10.244.1.16
1111
root@k8s-master01:~# curl 10.244.1.15
3333
root@k8s-master01:~# curl 10.244.2.23
4444
root@k8s-master01:~# curl 10.244.2.24
2222
```

现在给这4个pod配置一个统一的对外的访问地址：

**暴露服务**

```
kubectl expose deploy <deployment-name> --port=<service-port> --target-port=<pod-port>
```

deployment-name： 部署的deploy名称，如：my-dep

service-port： service对外公开的端口，外部服务只需要service-ip : service-port 即可访问服务

pod-port： port在系统内部暴漏的端口，这里部署的是nginx，nginx默认使用的是80端口

```bash
kubectl expose deploy my-dep --port=8000 --target-port=80
```

```bash
root@k8s-master01:~# kubectl expose deploy my-dep --port=8000 --target-port=80
service/my-dep exposed
```



**查看服务**

```bash
kubectl get service
```

```bash
root@k8s-master01:~# kubectl get service
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP    7h40m
my-dep       ClusterIP   10.96.230.187   <none>        8000/TCP   2m5s
```

可以看到 my-de 的ip地址为 10.96.230.187，端口为 8000，访问这个服务：

```bash
curl 10.96.230.187:8000
```

```bash
root@k8s-master01:~# curl 10.96.230.187:8000
1111
root@k8s-master01:~# curl 10.96.230.187:8000
4444
root@k8s-master01:~# curl 10.96.230.187:8000
3333
root@k8s-master01:~# curl 10.96.230.187:8000
4444
root@k8s-master01:~# curl 10.96.230.187:8000
1111
root@k8s-master01:~# curl 10.96.230.187:8000
2222
root@k8s-master01:~# curl 10.96.230.187:8000
2222
```

可以看出来，这个服务对4个pod进行了负载均衡。



**删除服务**

```bash
kubectl delete service <service-name>
```

```bash
kubectl delete service my-dep
```



除了可以使用命令进行服务暴露，还可以通过配置文件的形式：

```yaml
apiVersion: v1
kind: Service
metadata:
  labels:
    app: my-dep
  name: my-dep
spec:
  selector:
    app: my-dep
  ports:
  - port: 8000
    protocol: TCP
    targetPort: 80
```

```
kind: Service 
```

资源类型为service



```
labels:
    app: my-dep
  name: my-dep
```

service的名称为 my-dep



```
spec:
  selector:
    app: my-dep
```

pod的选择器，决定这个服务把哪些pod暴露出来

```
ports:
- port: 8000
  protocol: TCP
  targetPort: 80
```

定义内部和外部的端口



显示pod的标签：

```bash
kubectl get pod --show-labels
```

```
root@k8s-master01:~# kubectl get pod --show-labels
NAME                      READY   STATUS    RESTARTS   AGE   LABELS
my-dep-5688dd958f-fd9cm   1/1     Running   0          47m   app=my-dep,pod-template-hash=5688dd958f
my-dep-5688dd958f-hmh8f   1/1     Running   0          47m   app=my-dep,pod-template-hash=5688dd958f
my-dep-5688dd958f-j57l5   1/1     Running   0          47m   app=my-dep,pod-template-hash=5688dd958f
my-dep-5688dd958f-stkdj   1/1     Running   0          47m   app=my-dep,pod-template-hash=5688dd958f
```

注意这些pod都有一个label 

```bash
app=my-dep
```

标签名为 app 值为 my-dep， 所以这些pod都能被选择器

```bash
spec:
  selector:
    app: my-dep
```

选中



注意：默认的暴露方式只能在集群内部通过 service-ip : port 负载均衡访问，在pod内部也可以通过域名+port访问，域名通过service查看：

```bash
root@k8s-master01:~# kubectl get service
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP    7h40m
my-dep       ClusterIP   10.96.230.187   <none>        8000/TCP   2m5s
```

可以看到这个service名为my-dep，域名=`<service-name>.<namespace-name>.svc`， 在**pod内部**就可以通过 域名:port 访问，如：

```
curl my-dep.default.svc:8000
```

```bash
root@my-dep-5688dd958f-fd9cm:/# curl my-dep.default.svc:8000
4444
root@my-dep-5688dd958f-fd9cm:/# curl my-dep.default.svc:8000
3333
root@my-dep-5688dd958f-fd9cm:/# curl my-dep.default.svc:8000
3333
root@my-dep-5688dd958f-fd9cm:/# curl my-dep.default.svc:8000
3333
root@my-dep-5688dd958f-fd9cm:/# curl my-dep.default.svc:8000
1111
root@my-dep-5688dd958f-fd9cm:/# curl my-dep.default.svc:8000
2222
```

**注意要在pod内部访问，直接在节点访问是无法识别域名的** 这样在前面假设的vue项目中就可以使用这样的service域名：port或者serviceIP：port访问service。

这种就是默认ip的类型为`ClusterIP`的情况。



测试service的访问情况：

```bash
root@k8s-master01:~# curl 10.96.230.187:8000
3333
root@k8s-master01:~# curl 10.96.230.187:8000
1111
root@k8s-master01:~# curl 10.96.230.187:8000
3333
root@k8s-master01:~# curl 10.96.230.187:8000
2222
root@k8s-master01:~# curl 10.96.230.187:8000
4444
root@k8s-master01:~# curl 10.96.230.187:8000
3333
root@k8s-master01:~# curl 10.96.230.187:8000
1111
root@k8s-master01:~# curl 10.96.230.187:8000
2222
root@k8s-master01:~# curl 10.96.230.187:8000
3333
root@k8s-master01:~# curl 10.96.230.187:8000
4444
```

现在对这此部署进行缩容，通过dashboard进行操作后：

```bash
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-hmh8f   1/1     Running   0          67m
my-dep-5688dd958f-j57l5   1/1     Running   0          67m
```

在进行访问测试：

```bash
root@k8s-master01:~# curl 10.96.230.187:8000
4444
root@k8s-master01:~# curl 10.96.230.187:8000
4444
root@k8s-master01:~# curl 10.96.230.187:8000
3333
root@k8s-master01:~# curl 10.96.230.187:8000
3333
root@k8s-master01:~# curl 10.96.230.187:8000
4444
root@k8s-master01:~# curl 10.96.230.187:8000
3333
```

可以看到只剩下3333和4444了，此时再进行扩容：

```bash
root@k8s-master01:~# kubectl get pod
NAME                      READY   STATUS    RESTARTS   AGE
my-dep-5688dd958f-hmh8f   1/1     Running   0          69m
my-dep-5688dd958f-j57l5   1/1     Running   0          69m
my-dep-5688dd958f-zrf5p   1/1     Running   0          12s
```

再进行访问测试：

```bash
root@k8s-master01:~# curl 10.96.230.187:8000
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
html { color-scheme: light dark; }
body { width: 35em; margin: 0 auto;
font-family: Tahoma, Verdana, Arial, sans-serif; }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>
root@k8s-master01:~# curl 10.96.230.187:8000
3333
root@k8s-master01:~# curl 10.96.230.187:8000
4444
root@k8s-master01:~# curl 10.96.230.187:8000
4444
```

可以看到扩容后产生的pod是启动了一个全新的容器，因此会返回nginx默认的主页。



注意：默认的暴露服务的指令：

```bash
kubectl expose deploy <deployment-name> --port=<service-port> --target-port=<pod-port>
```

等价于

```bash
kubectl expose deploy <deployment-name> --port=<service-port> --target-port=<pod-port> --type=ClusterIP
```

参数--type=ClusterIP 设置ip的类型为**集群ip**，只能在集群的内部访问。如果想要在公网上进行访问，那就需要使用

```bash
kubectl expose deploy <deployment-name> --port=<service-port> --target-port=<pod-port> --type=NodePort
```

使用**节点端口**模式，这样暴露的服务可以在集群外进行访问。这种模式会在每个节点都开一个端口，这样就可以使用节点的公网ip+端口的方式进行访问。

查看现有的service

```bash
kubectl get service
```

```bash
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP    8h
my-dep       ClusterIP   10.96.230.187   <none>        8000/TCP   39m
```

删除现有的service

```bash
kubectl delete service my-dep
```

```bash
service "my-dep" deleted
```

重新暴露service

```bash
kubectl expose deploy my-dep --port=8000 --target-port=80 --type=NodePort
```

```bash
service/my-dep exposed
```

查看service

```bash
root@k8s-master01:~# kubectl get service
NAME         TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
kubernetes   ClusterIP   10.96.0.1     <none>        443/TCP          8h
my-dep       NodePort    10.96.1.122   <none>        8000:31736/TCP   2m6s
```

注意此时 my-dep 有了 CLUSTER-IP = 10.96.1.122，可以通过 10.96.1.122:8000访问服务，另外在port列中，除了有8000端口外，k8s随机的给了一个 30000-32767之间的一个端口，这里是 31736，通过这个任意节点的公网ip+随机端口 可以访问服务。

在宿主机上运行：

```bash
curl http://192.168.0.112:31736/
```

就可以看到服务的返回了。

```bash
C:\Users\leixing\Desktop>curl http://192.168.0.112:31736/
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
html { color-scheme: light dark; }
body { width: 35em; margin: 0 auto;
font-family: Tahoma, Verdana, Arial, sans-serif; }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>

<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>

<p><em>Thank you for using nginx.</em></p>
</body>
</html>

C:\Users\leixing\Desktop>curl http://192.168.0.112:31736/
3333

C:\Users\leixing\Desktop>curl http://192.168.0.112:31736/
4444
```

