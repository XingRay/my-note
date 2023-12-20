## k8s-namespace



###  k8s中资源的创建方式

1 命令行

2 yaml

```bash
kubectl apply -f xxx.yaml
```



### namespace

名称空间，用于隔离资源，资源分组。

比如名称空间 dev/test/prod 用于区分资源的不同的用途。

k8s安装之后会有几个 kube- 的系统名称空间和一个default名称空间

获取名称空间

```bash
kubectl get namespace
```

或者

```bash
kubectl get ns
```

输出如下：

```bash
root@k8s-master01:~# kubectl get namespace
NAME                   STATUS   AGE
default                Active   106m
kube-flannel           Active   100m
kube-node-lease        Active   106m
kube-public            Active   106m
kube-system            Active   106m
kubernetes-dashboard   Active   32m
root@k8s-master01:~# kubectl get ns
NAME                   STATUS   AGE
default                Active   106m
kube-flannel           Active   100m
kube-node-lease        Active   106m
kube-public            Active   106m
kube-system            Active   106m
kubernetes-dashboard   Active   32m
```



获取k8s的所有的pod

```bash
kubectl get pods -A
```

输出如下：

```bash
kubectl get pods -A
NAMESPACE              NAME                                         READY   STATUS    RESTARTS      AGE
kube-flannel           kube-flannel-ds-5bxr7                        1/1     Running   1 (59m ago)   100m
kube-flannel           kube-flannel-ds-mbs46                        1/1     Running   2 (57m ago)   100m
kube-flannel           kube-flannel-ds-nzn9w                        1/1     Running   1 (59m ago)   102m
kube-system            coredns-7bdc4cb885-64zwl                     1/1     Running   1 (59m ago)   107m
kube-system            coredns-7bdc4cb885-txnlf                     1/1     Running   1 (59m ago)   107m
kube-system            etcd-k8s-master01                            1/1     Running   1 (59m ago)   108m
kube-system            kube-apiserver-k8s-master01                  1/1     Running   1 (59m ago)   108m
kube-system            kube-controller-manager-k8s-master01         1/1     Running   1 (59m ago)   108m
kube-system            kube-proxy-6lt6z                             1/1     Running   2 (57m ago)   100m
kube-system            kube-proxy-9ssx8                             1/1     Running   1 (59m ago)   107m
kube-system            kube-proxy-dj74d                             1/1     Running   1 (59m ago)   100m
kube-system            kube-scheduler-k8s-master01                  1/1     Running   1 (59m ago)   108m
kubernetes-dashboard   dashboard-metrics-scraper-5cb4f4bb9c-fwg8q   1/1     Running   0             34m
kubernetes-dashboard   kubernetes-dashboard-6967859bff-rmgxz        1/1     Running   0             34m
```

后面的参数 -A 表示获取所有名称空间，如果不加该参数，表示获取默认名称空间 （default）

```bash
kubectl get pods
```

输出：

```bash
kubectl get pods
No resources found in default namespace.
```

目前default名称空间没有部署任何应用。

查看指定名称空间的应用：

```bash
kubectl get pods -n kubernetes-dashboard
```

输出：

```bash
kubectl get pods -n kubernetes-dashboard
NAME                                         READY   STATUS    RESTARTS   AGE
dashboard-metrics-scraper-5cb4f4bb9c-fwg8q   1/1     Running   0          37m
kubernetes-dashboard-6967859bff-rmgxz        1/1     Running   0          37m
```



**创建资源时如果不指定名称空间，默认都是创建到default名称空间下**



创建名称空间

```
kubectl create ns hello
```

删除名称空间

```bash
kubectl delete ns hello
```

删除名称空间时会把该名称空间下的所有连带资源全部删除。

也可以通过配置文件的方式创建和删除名称空间，创建一个yaml文件：hello.yaml

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: hello
```

```bash
vi hello.yaml
```

输入上述内容

```bash
kubectl apply -f hello.yaml
```

这样就可以更具配置文件创建一个hello名称空间了

```bash
kubectl apply -f hello.yaml
namespace/hello created
```

同过配置文件创建的资源可以通过配置文件删除：

```bash
kubectl delete -f hello.yaml
```

```bash
kubectl delete -f hello.yaml
namespace "hello" deleted
```