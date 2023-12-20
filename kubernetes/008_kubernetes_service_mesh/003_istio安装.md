## istio的安装部署

[toc]

前提: 已经安装部署了 k8s 集群

### 1 下载

https://github.com/istio/istio/releases

https://github.com/istio/istio/releases/download/1.18.1/istio-1.18.1-linux-amd64.tar.gz

当前最新版本为 1.18.1



### 2 安装

下载后上传至服务器, 解压

```bash
mkdir -p /app/istio
```

```bash
tar -zxvf istio-1.18.1-linux-amd64.tar.gz -C /app/istio
```

解压后的目录为

```bash
/app/istio/istio-1.18.1
```

添加PATH

```bash
vi /etc/profile
```

在末尾添加

```bash
export ISTIO_HOME=/app/istio/istio-1.18.1
PATH=$ISTIO_HOME/bin:$PATH

export PATH
```

```bash
source /etc/profile
```

这样在任意目录执行

```bash
istioctl version
```

输出:

```
root@k8s-master01:~# istioctl version
no ready Istio pods in "istio-system"
1.18.1
```

说明安装完成了



### 3 常用指令

帮助文档:

```bash
istioctl help
```

```bash
root@k8s-master01:~# istioctl help
Istio configuration command line utility for service operators to
debug and diagnose their Istio mesh.

Usage:
  istioctl [command]

Available Commands:
  admin                Manage control plane (istiod) configuration
  analyze              Analyze Istio configuration and print validation messages
  authz                (authz is experimental. Use `istioctl experimental authz`)
  bug-report           Cluster information and log capture support tool.
  completion           Generate the autocompletion script for the specified shell
  create-remote-secret Create a secret with credentials to allow Istio to access remote Kubernetes apiservers
  dashboard            Access to Istio web UIs
  experimental         Experimental commands that may be modified or deprecated
  help                 Help about any command
  install              Applies an Istio manifest, installing or reconfiguring Istio on a cluster.
  kube-inject          Inject Istio sidecar into Kubernetes pod resources
  manifest             Commands related to Istio manifests
  operator             Commands related to Istio operator controller.
  profile              Commands related to Istio configuration profiles
  proxy-config         Retrieve information about proxy configuration from Envoy [kube only]
  proxy-status         Retrieves the synchronization status of each Envoy in the mesh [kube only]
  remote-clusters      Lists the remote clusters each istiod instance is connected to.
  tag                  Command group used to interact with revision tags
  uninstall            Uninstall Istio from a cluster
  upgrade              Upgrade Istio control plane in-place
  validate             Validate Istio policy and rules files
  verify-install       Verifies Istio Installation Status
  version              Prints out build version information

Flags:
      --context string                The name of the kubeconfig context to use
  -h, --help                          help for istioctl
  -i, --istioNamespace string         Istio system namespace (default "istio-system")
  -c, --kubeconfig string             Kubernetes configuration file
  -n, --namespace string              Config namespace
      --s2a_enable_appengine_dialer   If true, opportunistically use AppEngine-specific dialer to call S2A.
      --s2a_timeout duration          Timeout enforced on the connection to the S2A service for handshake. (default 3s)
      --vklog Level                   number for the log level verbosity. Like -v flag. ex: --vklog=9

Additional help topics:
  istioctl options                           Displays istioctl global options

Use "istioctl [command] --help" for more information about a command.
```



### 4 安装istio服务

使用 `istioctl install` 命令可以安装istio服务,需要指定一个配置文件, 配置文件在istio的解压目录的 manifests 目录中找到

```bash
ls /app/istio/istio-1.18.1/manifests/profiles
```

```bash
ambient.yaml  default.yaml  demo.yaml  empty.yaml  external.yaml  minimal.yaml  openshift.yaml  preview.yaml  remote.yaml
```

可以看到有很多的配置文件, 这里作为演示,选择这个 demo.yml 即可, 在任意目录执行:

```bash
istioctl install --set profile=demo -y
```

```bash
root@k8s-master01:~# istioctl install --set profile=demo -y
✔ Istio core installed
✔ Istiod installed
✔ Egress gateways installed
✔ Ingress gateways installed
✔ Installation complete                                                                                                         Making this installation the default for injection and validation.
```

安装完成后会在k8s中创建一个名为 `istio-system` 的命名空间, 查看资源

```bash
kubectl -n istio-system get all
```

```bash
root@k8s-master01:~# kubectl -n istio-system get all
NAME                                        READY   STATUS    RESTARTS   AGE
pod/istio-egressgateway-7f784577c4-m6fkk    1/1     Running   0          157m
pod/istio-ingressgateway-5b957c89fd-rfl54   1/1     Running   0          157m
pod/istiod-57cc56994d-nnvk8                 1/1     Running   0          158m

NAME                           TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)                                                                      AGE
service/istio-egressgateway    ClusterIP      10.233.32.153   <none>        80/TCP,443/TCP                                                               157m
service/istio-ingressgateway   LoadBalancer   10.233.4.197    <pending>     15021:32033/TCP,80:30223/TCP,443:30395/TCP,31400:32168/TCP,15443:30284/TCP   157m
service/istiod                 ClusterIP      10.233.53.224   <none>        15010/TCP,15012/TCP,443/TCP,15014/TCP                                        158m

NAME                                   READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/istio-egressgateway    1/1     1            1           157m
deployment.apps/istio-ingressgateway   1/1     1            1           157m
deployment.apps/istiod                 1/1     1            1           158m

NAME                                              DESIRED   CURRENT   READY   AGE
replicaset.apps/istio-egressgateway-7f784577c4    1         1         1       157m
replicaset.apps/istio-ingressgateway-5b957c89fd   1         1         1       157m
replicaset.apps/istiod-57cc56994d                 1         1         1       158m
```



### 5 安装插件

为了使用遥测等相关功能,还需要安装一些额外的插件

```bash
cd /app/istio/istio-1.18.1
```

```bash
kubectl apply -f samples/addons
```

samples/addons文件中包含了常用的 kiali  prometheus grafana jaeger 等相关插件, 

```bash
root@k8s-master01:/app/istio/istio-1.18.1# kubectl apply -f samples/addons
serviceaccount/grafana created
configmap/grafana created
service/grafana created
deployment.apps/grafana created
configmap/istio-grafana-dashboards created
configmap/istio-services-grafana-dashboards created
deployment.apps/jaeger created
service/tracing created
service/zipkin created
service/jaeger-collector created
serviceaccount/kiali created
configmap/kiali created
clusterrole.rbac.authorization.k8s.io/kiali-viewer created
clusterrole.rbac.authorization.k8s.io/kiali created
clusterrolebinding.rbac.authorization.k8s.io/kiali created
role.rbac.authorization.k8s.io/kiali-controlplane created
rolebinding.rbac.authorization.k8s.io/kiali-controlplane created
service/kiali created
deployment.apps/kiali created
serviceaccount/loki created
configmap/loki created
configmap/loki-runtime created
service/loki-memberlist created
service/loki-headless created
service/loki created
statefulset.apps/loki created
serviceaccount/prometheus created
configmap/prometheus created
clusterrole.rbac.authorization.k8s.io/prometheus created
clusterrolebinding.rbac.authorization.k8s.io/prometheus created
service/prometheus created
deployment.apps/prometheus created
```

安装完成后还需要将 kiali 的端口进行暴露

```bash
kubectl -n istio-system patch svc kiali -p '{"spec":{"type":"NodePort", "ports":[{"port":20001, "protocal":"TCP", "targetPort":20001, "nodePort":30001}]}}'
```

```bash
root@k8s-master01:/app/istio/istio-1.18.1# kubectl -n istio-system patch svc kiali -p '{"spec":{"type":"NodePort", "ports":[{"port":20001, "protocal":"TCP", "targetPort":20001, "nodePort":30001}]}}'
service/kiali patched
```

再次查看istio的资源

```bash
kubectl -n istio-system get all
```

```bash
root@k8s-master01:/app/istio/istio-1.18.1# kubectl -n istio-system get all
NAME                                        READY   STATUS    RESTARTS   AGE
pod/grafana-cc959c75c-k9hxh                 1/1     Running   0          32m
pod/istio-egressgateway-7f784577c4-m6fkk    1/1     Running   0          3h10m
pod/istio-ingressgateway-5b957c89fd-rfl54   1/1     Running   0          3h10m
pod/istiod-57cc56994d-nnvk8                 1/1     Running   0          3h11m
pod/jaeger-7747d44bfc-wl4jk                 1/1     Running   0          32m
pod/kiali-67946bcc89-cp8m8                  1/1     Running   0          32m
pod/prometheus-85674d4cb8-s25d6             2/2     Running   0          32m

NAME                           TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)                                                                      AGE
service/grafana                ClusterIP      10.233.45.176   <none>        3000/TCP                                                                     32m
service/istio-egressgateway    ClusterIP      10.233.32.153   <none>        80/TCP,443/TCP                                                               3h10m
service/istio-ingressgateway   LoadBalancer   10.233.4.197    <pending>     15021:32033/TCP,80:30223/TCP,443:30395/TCP,31400:32168/TCP,15443:30284/TCP   3h10m
service/istiod                 ClusterIP      10.233.53.224   <none>        15010/TCP,15012/TCP,443/TCP,15014/TCP                                        3h11m
service/jaeger-collector       ClusterIP      10.233.42.187   <none>        14268/TCP,14250/TCP,9411/TCP                                                 32m
service/kiali                  NodePort       10.233.63.23    <none>        20001:30001/TCP,9090:30330/TCP                                               32m
service/loki-headless          ClusterIP      None            <none>        3100/TCP                                                                     32m
service/prometheus             ClusterIP      10.233.41.189   <none>        9090/TCP                                                                     32m
service/tracing                ClusterIP      10.233.45.8     <none>        80/TCP,16685/TCP                                                             32m
service/zipkin                 ClusterIP      10.233.0.54     <none>        9411/TCP                                                                     32m

NAME                                   READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/grafana                1/1     1            1           32m
deployment.apps/istio-egressgateway    1/1     1            1           3h10m
deployment.apps/istio-ingressgateway   1/1     1            1           3h10m
deployment.apps/istiod                 1/1     1            1           3h11m
deployment.apps/jaeger                 1/1     1            1           32m
deployment.apps/kiali                  1/1     1            1           32m
deployment.apps/prometheus             1/1     1            1           32m

NAME                                              DESIRED   CURRENT   READY   AGE
replicaset.apps/grafana-cc959c75c                 1         1         1       32m
replicaset.apps/istio-egressgateway-7f784577c4    1         1         1       3h10m
replicaset.apps/istio-ingressgateway-5b957c89fd   1         1         1       3h10m
replicaset.apps/istiod-57cc56994d                 1         1         1       3h11m
replicaset.apps/jaeger-7747d44bfc                 1         1         1       32m
replicaset.apps/kiali-67946bcc89                  1         1         1       32m
replicaset.apps/prometheus-85674d4cb8             1         1         1       32m
```

可以看到 kiali prometheus grafana jaeger 等服务都已经正常运行, 并且暴露了kiali的20001端口

