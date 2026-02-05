## k8s集群reset恢复重置


一、概述
因k8s集群故障，无法恢复，所以进行重置k8s集群。

参考

K8S集群重新初始化

https://www.cnblogs.com/-abm/p/16629954.html

二、master1重置
1、重置
在master1节点执行下面reset命令：
//过程会询问是否重置，输入y然后回车

# 重置
[root@master1 ~]# kubeadm reset
[reset] Reading configuration from the cluster...
[reset] FYI: You can look at this config file with 'kubectl -n kube-system get cm kubeadm-config -o yaml'
W1130 10:07:07.846763   19873 reset.go:99] [reset] Unable to fetch the kubeadm-config ConfigMap from cluster: failed to get config map: Get "https://192.168.0.212:6443/api/v1/namespaces/kube-system/configmaps/kubeadm-config?timeout=10s": dial tcp 192.168.0.212:6443: connect: connection refused
[reset] WARNING: Changes made to this host by 'kubeadm init' or 'kubeadm join' will be reverted.
[reset] Are you sure you want to proceed? [y/N]: y
[preflight] Running pre-flight checks
W1130 10:07:11.085145   19873 removeetcdmember.go:79] [reset] No kubeadm config, using etcd pod spec to get data directory
[reset] Stopping the kubelet service
[reset] Unmounting mounted directories in "/var/lib/kubelet"
[reset] Deleting contents of config directories: [/etc/kubernetes/manifests /etc/kubernetes/pki]
[reset] Deleting files: [/etc/kubernetes/admin.conf /etc/kubernetes/kubelet.conf /etc/kubernetes/bootstrap-kubelet.conf /etc/kubernetes/controller-manager.conf /etc/kubernetes/scheduler.conf]
[reset] Deleting contents of stateful directories: [/var/lib/etcd /var/lib/kubelet /var/lib/dockershim /var/run/kubernetes /var/lib/cni]

The reset process does not clean CNI configuration. To do so, you must remove /etc/cni/net.d

The reset process does not reset or clean up iptables rules or IPVS tables.
If you wish to reset iptables, you must do so manually by using the "iptables" command.

If your cluster was setup to utilize IPVS, run ipvsadm --clear (or similar)
to reset your system's IPVS tables.

The reset process does not clean your kubeconfig files and you must remove them manually.
Please, check the contents of the $HOME/.kube/config file.
2、手动清除配置信息
手动清除配置信息，这一步很关键：

（1）清除遗留文件
[root@master1 cni]# rm -rf /root/.kube
[root@master1 cni]# rm -rf /etc/cni/net.d
（2）清理ipvsadm
[root@master1 cni]# yum install -y ipvsadm
Loaded plugins: fastestmirror, product-id, search-disabled-repos, subscription-manager

# 省略
......

Installed:
  ipvsadm.x86_64 0:1.27-8.el7                                                                                                                                                             

Complete!
[root@master1 cni]# ipvsadm -C
[root@master1 cni]# iptables -F && iptables -t nat -F && iptables -t mangle -F && iptables -X
3、重新引导集群
（1）初始化
[root@master1 cni]# cd /root/
[root@master1 ~]# ls
anaconda-ks.cfg  grafana.ini  inmaster-3.pcap  k8s  prometheusgrafana  v1.21.2.tar
[root@master1 ~]# kubeadm init --image-repository=registry.aliyuncs.com/google_containers --control-plane-endpoint=192.168.0.212 --kubernetes-version=v1.21.2 --apiserver-advertise-address=192.168.0.212 --pod-network-cidr=10.244.0.0/16 --upload-certs | tee kubeadm-init.log
[init] Using Kubernetes version: v1.21.2

# 省略
......

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

You can now join any number of the control-plane node running the following command on each as root:

  kubeadm join 192.168.0.212:6443 --token 5nokjb.i1okf7ljckcxdis8 \
	--discovery-token-ca-cert-hash sha256:aad97ca5808e6e01aec8f730bd900e6722573b3d2f830ac79e641837285c9600 \
	--control-plane --certificate-key dcd71cc84107d465a76373e537dc52903488a8f70e8ad9b1b956c868e9fd8b56

Please note that the certificate-key gives access to cluster sensitive data, keep it secret!
As a safeguard, uploaded-certs will be deleted in two hours; If necessary, you can use
"kubeadm init phase upload-certs --upload-certs" to reload certs afterward.

Then you can join any number of worker nodes by running the following on each as root:

kubeadm join 192.168.0.212:6443 --token 5nokjb.i1okf7ljckcxdis8 \
	--discovery-token-ca-cert-hash sha256:aad97ca5808e6e01aec8f730bd900e6722573b3d2f830ac79e641837285c9600 
可能出现的问题
1、初始化失败问题处理：

[ERROR CRI]: container runtime is not running: output: time="2022-08-27T10:20:58+08:00" level=fatal msg="getting status of runtime: rpc error: code = Unimplemented desc = unknown service runtime.v1alpha2.RuntimeService"
解决方式：

[root@k8s-master01 ~]# rm /etc/containerd/config.toml
rm：是否删除普通文件 "/etc/containerd/config.toml"？y
[root@k8s-master01 ~]# systemctl restart containerd
（2）复制目录
创建配置目录，并复制权限配置文件到用户目录下

[root@master1 ~]# mkdir -p $HOME/.kube
[root@master1 ~]# sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
[root@master1 ~]# sudo chown $(id -u):$(id -g) $HOME/.kube/config
[root@master1 ~]# echo $KUBECONFIG
/etc/kubernetes/admin.conf
[root@master1 ~]# kubectl get nodes
NAME      STATUS     ROLES                  AGE   VERSION
master1   NotReady   control-plane,master   74s   v1.21.2
（3）健康检查
[root@master1 ~]# cd k8s/
[root@master1 k8s]# ls
admin.conf  dashboard  desktop.ini  elk  harbor-registry.yaml  helm  images  ingress  kafka  kube-flannel.yml  postgresql  pv  redis  storage-class  vm

# 查看
[root@master1 k8s]# kubectl get cs
Warning: v1 ComponentStatus is deprecated in v1.19+
NAME                 STATUS      MESSAGE                                                                                       ERROR
controller-manager   Unhealthy   Get "http://127.0.0.1:10252/healthz": dial tcp 127.0.0.1:10252: connect: connection refused   
scheduler            Unhealthy   Get "http://127.0.0.1:10251/healthz": dial tcp 127.0.0.1:10251: connect: connection refused   
etcd-0               Healthy     {"health":"true"}                                                                             

# 修改
[root@master1 k8s]# sed -i 's/- --port=0/#- --port=0/g' /etc/kubernetes/manifests/kube-scheduler.yaml 
[root@master1 k8s]# sed -i 's/- --port=0/#- --port=0/g' /etc/kubernetes/manifests/kube-controller-manager.yaml 

# 等几分钟，再次查看
[root@master1 k8s]# kubectl get cs
Warning: v1 ComponentStatus is deprecated in v1.19+
NAME                 STATUS    MESSAGE             ERROR
controller-manager   Healthy   ok                  
scheduler            Healthy   ok                  
etcd-0               Healthy   {"health":"true"}   
4、部署网络插件
# 部署网络插件
[root@master1 k8s]# ls
admin.conf  dashboard  desktop.ini  elk  harbor-registry.yaml  helm  images  ingress  kafka  kube-flannel.yml  postgresql  pv  redis  storage-class  vm
[root@master1 k8s]# kubectl apply -f kube-flannel.yml
Warning: policy/v1beta1 PodSecurityPolicy is deprecated in v1.21+, unavailable in v1.25+
podsecuritypolicy.policy/psp.flannel.unprivileged created
Warning: rbac.authorization.k8s.io/v1beta1 ClusterRole is deprecated in v1.17+, unavailable in v1.22+; use rbac.authorization.k8s.io/v1 ClusterRole
clusterrole.rbac.authorization.k8s.io/flannel created
Warning: rbac.authorization.k8s.io/v1beta1 ClusterRoleBinding is deprecated in v1.17+, unavailable in v1.22+; use rbac.authorization.k8s.io/v1 ClusterRoleBinding
clusterrolebinding.rbac.authorization.k8s.io/flannel created
serviceaccount/flannel created
configmap/kube-flannel-cfg created
daemonset.apps/kube-flannel-ds-amd64 created
daemonset.apps/kube-flannel-ds-arm64 created
daemonset.apps/kube-flannel-ds-arm created
daemonset.apps/kube-flannel-ds-ppc64le created
daemonset.apps/kube-flannel-ds-s390x created

[root@master1 k8s]# kubectl get nodes
NAME      STATUS   ROLES                  AGE     VERSION
master1   Ready    control-plane,master   7m32s   v1.21.2
5、部署dashboard
[root@master1 k8s]# ls
admin.conf  dashboard  desktop.ini  elk  harbor-registry.yaml  helm  images  ingress  kafka  kube-flannel.yml  postgresql  pv  redis  storage-class  vm
[root@master1 k8s]# cd dashboard/
[root@master1 dashboard]# ls
dashboard-adminuser.yaml  dashboard-admin.yaml  kubernetes-dashboard.yaml  metrics-server-master  metrics-server.zip
[root@master1 dashboard]# kubectl apply -f kubernetes-dashboard.yaml
namespace/kubernetes-dashboard created
serviceaccount/kubernetes-dashboard created
service/kubernetes-dashboard created
secret/kubernetes-dashboard-certs created
secret/kubernetes-dashboard-csrf created
secret/kubernetes-dashboard-key-holder created
configmap/kubernetes-dashboard-settings created
role.rbac.authorization.k8s.io/kubernetes-dashboard created
clusterrole.rbac.authorization.k8s.io/kubernetes-dashboard created
rolebinding.rbac.authorization.k8s.io/kubernetes-dashboard created
clusterrolebinding.rbac.authorization.k8s.io/kubernetes-dashboard created
deployment.apps/kubernetes-dashboard created
service/dashboard-metrics-scraper created
deployment.apps/dashboard-metrics-scraper created
[root@master1 dashboard]# kubectl apply -f dashboard-adminuser.yaml
serviceaccount/admin-user created
[root@master1 dashboard]# kubectl apply -f dashboard-admin.yaml
clusterrolebinding.rbac.authorization.k8s.io/admin-user created
[root@master1 dashboard]# kubectl -n kubernetes-dashboard describe secret $(kubectl -n kubernetes-dashboard get secret | grep admin-user | awk '{print $1}')
Name:         admin-user-token-t2bmg
Namespace:    kubernetes-dashboard
Labels:       <none>
Annotations:  kubernetes.io/service-account.name: admin-user
              kubernetes.io/service-account.uid: de39cf3e-18b4-492f-ab06-26e0a8fc0594

Type:  kubernetes.io/service-account-token

Data
====
ca.crt:     1066 bytes
namespace:  20 bytes
token:      eyJhbGciOiJSUzI1NiIsImtpZCI6IkhVLTE2RUp2S1NETlRtb2VBZERCWVJSWWNlUE5wTG9zQXlNRExvLTRKSE0ifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlcm5ldGVzLWRhc2hib2FyZCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi11c2VyLXRva2VuLXQyYm1nIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImFkbWluLXVzZXIiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiJkZTM5Y2YzZS0xOGI0LTQ5MmYtYWIwNi0yNmUwYThmYzA1OTQiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZXJuZXRlcy1kYXNoYm9hcmQ6YWRtaW4tdXNlciJ9.c90anAgvbGHsWwL1RXenGIEnFnDPf2Jhp-9bv0q4aQOFJkeBHMH5J13fuEMTk5Fl585jZJWI876sWE_0eD8HRP9TQ4a9RkCoQGAW_6RO_IU_N0GFufzOOED5Kz0M0EyYz1ect7v5ll79_E4koYhrZTVZxzWteaC0odNslwFcHILeSbnz2VBG4696zXcYuvWklt7BR8b5y96W3SeD5AOTAwqV1GABWo8-qe6TaxAgeNABlkP3XRR4g6xr02DHJBWyADAFQrDS6e2_D5R-oIVesnqzSjPui9Smc545zKBBjyo04FmWYLUUYKBwioS4symhTzpDHjK7b5xZlkRq08mvjw
[root@master1 dashboard]# kubectl get pods --all-namespaces
NAMESPACE              NAME                                         READY   STATUS    RESTARTS   AGE
kube-system            coredns-59d64cd4d4-d2zlm                     1/1     Running   0          16m
kube-system            coredns-59d64cd4d4-fwznn                     1/1     Running   0          16m
kube-system            etcd-master1                                 1/1     Running   0          16m
kube-system            etcd-master2                                 1/1     Running   0          5m55s
kube-system            kube-apiserver-master1                       1/1     Running   0          16m
kube-system            kube-apiserver-master2                       1/1     Running   0          5m49s
kube-system            kube-controller-manager-master1              1/1     Running   0          13m
kube-system            kube-controller-manager-master2              1/1     Running   0          5m51s
kube-system            kube-flannel-ds-amd64-74k28                  1/1     Running   0          9m44s
kube-system            kube-flannel-ds-amd64-cq5lh                  1/1     Running   0          5m58s
kube-system            kube-flannel-ds-amd64-l6g92                  1/1     Running   0          102s
kube-system            kube-flannel-ds-amd64-qc5p9                  1/1     Running   0          3m9s
kube-system            kube-flannel-ds-amd64-sz7hb                  1/1     Running   0          2m28s
kube-system            kube-flannel-ds-amd64-vds9p                  1/1     Running   0          4m13s
kube-system            kube-proxy-fcbv9                             1/1     Running   0          102s
kube-system            kube-proxy-fccvx                             1/1     Running   0          2m28s
kube-system            kube-proxy-fqkbs                             1/1     Running   0          4m13s
kube-system            kube-proxy-mtn9w                             1/1     Running   0          16m
kube-system            kube-proxy-n46dl                             1/1     Running   0          3m9s
kube-system            kube-proxy-tnxfv                             1/1     Running   0          5m58s
kube-system            kube-scheduler-master1                       1/1     Running   0          13m
kube-system            kube-scheduler-master2                       1/1     Running   0          5m52s
kubernetes-dashboard   dashboard-metrics-scraper-6ddd77bc75-fdrbg   1/1     Running   0          47s
kubernetes-dashboard   kubernetes-dashboard-8c9c48775-7z7h4         1/1     Running   0          47s
[root@master1 dashboard]# ls
dashboard-adminuser.yaml  dashboard-admin.yaml  kubernetes-dashboard.yaml  metrics-server-master  metrics-server.zip
6、部署监控
[root@master1 dashboard]# cd metrics-server
-bash: cd: metrics-server: No such file or directory
[root@master1 dashboard]# ls
dashboard-adminuser.yaml  dashboard-admin.yaml  kubernetes-dashboard.yaml  metrics-server-master  metrics-server.zip
[root@master1 dashboard]# cd metrics-server-master/deploy/kubernetes/
[root@master1 kubernetes]# ls
aggregated-metrics-reader.yaml  auth-delegator.yaml  auth-reader.yaml  metrics-apiservice.yaml  metrics-server-deployment.yaml  metrics-server-service.yaml  resource-reader.yaml
[root@master1 kubernetes]# kubectl apply -f ./
clusterrole.rbac.authorization.k8s.io/system:aggregated-metrics-reader created
clusterrolebinding.rbac.authorization.k8s.io/metrics-server:system:auth-delegator created
rolebinding.rbac.authorization.k8s.io/metrics-server-auth-reader created
Warning: apiregistration.k8s.io/v1beta1 APIService is deprecated in v1.19+, unavailable in v1.22+; use apiregistration.k8s.io/v1 APIService
apiservice.apiregistration.k8s.io/v1beta1.metrics.k8s.io created
serviceaccount/metrics-server created
deployment.apps/metrics-server created
service/metrics-server created
clusterrole.rbac.authorization.k8s.io/system:metrics-server created
clusterrolebinding.rbac.authorization.k8s.io/system:metrics-server created
[root@master1 kubernetes]# kubectl get pods --all-namespaces
NAMESPACE              NAME                                         READY   STATUS    RESTARTS   AGE
kube-system            coredns-59d64cd4d4-d2zlm                     1/1     Running   0          17m
kube-system            coredns-59d64cd4d4-fwznn                     1/1     Running   0          17m
kube-system            etcd-master1                                 1/1     Running   0          18m
kube-system            etcd-master2                                 1/1     Running   0          7m6s
kube-system            kube-apiserver-master1                       1/1     Running   0          18m
kube-system            kube-apiserver-master2                       1/1     Running   0          7m
kube-system            kube-controller-manager-master1              1/1     Running   0          14m
kube-system            kube-controller-manager-master2              1/1     Running   0          7m2s
kube-system            kube-flannel-ds-amd64-74k28                  1/1     Running   0          10m
kube-system            kube-flannel-ds-amd64-cq5lh                  1/1     Running   0          7m9s
kube-system            kube-flannel-ds-amd64-l6g92                  1/1     Running   0          2m53s
kube-system            kube-flannel-ds-amd64-qc5p9                  1/1     Running   0          4m20s
kube-system            kube-flannel-ds-amd64-sz7hb                  1/1     Running   0          3m39s
kube-system            kube-flannel-ds-amd64-vds9p                  1/1     Running   0          5m24s
kube-system            kube-proxy-fcbv9                             1/1     Running   0          2m53s
kube-system            kube-proxy-fccvx                             1/1     Running   0          3m39s
kube-system            kube-proxy-fqkbs                             1/1     Running   0          5m24s
kube-system            kube-proxy-mtn9w                             1/1     Running   0          17m
kube-system            kube-proxy-n46dl                             1/1     Running   0          4m20s
kube-system            kube-proxy-tnxfv                             1/1     Running   0          7m9s
kube-system            kube-scheduler-master1                       1/1     Running   0          14m
kube-system            kube-scheduler-master2                       1/1     Running   0          7m3s
kube-system            metrics-server-7b9dbdddb5-n6jb9              1/1     Running   0          8s
kubernetes-dashboard   dashboard-metrics-scraper-6ddd77bc75-fdrbg   1/1     Running   0          118s
kubernetes-dashboard   kubernetes-dashboard-8c9c48775-7z7h4         1/1     Running   0          118s

[root@master1 kubernetes]# kubectl top node
W1130 10:33:41.359340   27120 top_node.go:119] Using json format to get metrics. Next release will switch to protocol-buffers, switch early by passing --use-protocol-buffers flag
error: metrics not available yet

# 时间等待长一点，再次执行
[root@master1 prometheusgrafana]# kubectl top node
W1201 15:44:11.481045   25848 top_node.go:119] Using json format to get metrics. Next release will switch to protocol-buffers, switch early by passing --use-protocol-buffers flag
NAME      CPU(cores)   CPU%   MEMORY(bytes)   MEMORY%   
master1   135m         3%     2291Mi          14% 

[root@master1 kubernetes]# kubectl get svc -A
NAMESPACE              NAME                        TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)                  AGE
default                kubernetes                  ClusterIP   10.96.0.1      <none>        443/TCP                  19m
kube-system            kube-dns                    ClusterIP   10.96.0.10     <none>        53/UDP,53/TCP,9153/TCP   19m
kube-system            metrics-server              ClusterIP   10.98.47.6     <none>        443/TCP                  98s
kubernetes-dashboard   dashboard-metrics-scraper   ClusterIP   10.97.65.41    <none>        8000/TCP                 3m28s
kubernetes-dashboard   kubernetes-dashboard        NodePort    10.97.63.207   <none>        443:30043/TCP            3m28s
三、master2重置
1、重置
[root@master2 ~]# kubeadm reset
[reset] Reading configuration from the cluster...
[reset] FYI: You can look at this config file with 'kubectl -n kube-system get cm kubeadm-config -o yaml'
W1130 10:23:18.677417   21579 reset.go:99] [reset] Unable to fetch the kubeadm-config ConfigMap from cluster: failed to get config map: Get "https://192.168.0.212:6443/api/v1/namespaces/kube-system/configmaps/kubeadm-config?timeout=10s": x509: certificate signed by unknown authority (possibly because of "crypto/rsa: verification error" while trying to verify candidate authority certificate "kubernetes")
[reset] WARNING: Changes made to this host by 'kubeadm init' or 'kubeadm join' will be reverted.
[reset] Are you sure you want to proceed? [y/N]: y
[preflight] Running pre-flight checks
W1130 10:23:21.264256   21579 removeetcdmember.go:79] [reset] No kubeadm config, using etcd pod spec to get data directory
[reset] Stopping the kubelet service
[reset] Unmounting mounted directories in "/var/lib/kubelet"
[reset] Deleting contents of config directories: [/etc/kubernetes/manifests /etc/kubernetes/pki]
[reset] Deleting files: [/etc/kubernetes/admin.conf /etc/kubernetes/kubelet.conf /etc/kubernetes/bootstrap-kubelet.conf /etc/kubernetes/controller-manager.conf /etc/kubernetes/scheduler.conf]
[reset] Deleting contents of stateful directories: [/var/lib/etcd /var/lib/kubelet /var/lib/dockershim /var/run/kubernetes /var/lib/cni]

The reset process does not clean CNI configuration. To do so, you must remove /etc/cni/net.d

The reset process does not reset or clean up iptables rules or IPVS tables.
If you wish to reset iptables, you must do so manually by using the "iptables" command.

If your cluster was setup to utilize IPVS, run ipvsadm --clear (or similar)
to reset your system's IPVS tables.

The reset process does not clean your kubeconfig files and you must remove them manually.
Please, check the contents of the $HOME/.kube/config file.
2、手动清除配置信息
（1）清除遗留文件
[root@master2 ~]# rm -rf /root/.kube
[root@master2 ~]# rm -rf /etc/cni/net.d
（2）清理ipvsadm
[root@master2 ~]# yum install -y ipvsadm
Loaded plugins: fastestmirror, product-id, search-disabled-repos, subscription-manager

# 省略
......

Installed:
  ipvsadm.x86_64 0:1.27-8.el7                                                                                                                                                             

Complete!
[root@master2 ~]# ipvsadm -C
[root@master2 ~]# iptables -F && iptables -t nat -F && iptables -t mangle -F && iptables -X
3、重新加入集群
[root@master2 ~]# kubeadm join 192.168.0.212:6443 --token 5nokjb.i1okf7ljckcxdis8 \
> --discovery-token-ca-cert-hash sha256:aad97ca5808e6e01aec8f730bd900e6722573b3d2f830ac79e641837285c9600 \
> --control-plane --certificate-key dcd71cc84107d465a76373e537dc52903488a8f70e8ad9b1b956c868e9fd8b56
> [preflight] Running pre-flight checks

# 省略
......

To start administering your cluster from this node, you need to run the following as a regular user:

	mkdir -p $HOME/.kube
	sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
	sudo chown $(id -u):$(id -g) $HOME/.kube/config

Run 'kubectl get nodes' to see this node join the cluster.

[root@master2 ~]# mkdir -p $HOME/.kube
[root@master2 ~]# sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
[root@master2 ~]# sudo chown $(id -u):$(id -g) $HOME/.kube/config
[root@master2 ~]# kubectl get nodes
NAME      STATUS   ROLES                  AGE   VERSION
master1   Ready    control-plane,master   11m   v1.21.2
master2   Ready    control-plane,master   23s   v1.21.2
四、node1
1、重置
[root@node1 ~]# kubeadm reset
[reset] WARNING: Changes made to this host by 'kubeadm init' or 'kubeadm join' will be reverted.
[reset] Are you sure you want to proceed? [y/N]: y
[preflight] Running pre-flight checks
W1130 10:27:17.733683   23266 removeetcdmember.go:79] [reset] No kubeadm config, using etcd pod spec to get data directory
[reset] No etcd config found. Assuming external etcd
[reset] Please, manually reset etcd to prevent further issues
[reset] Stopping the kubelet service
[reset] Unmounting mounted directories in "/var/lib/kubelet"
[reset] Deleting contents of config directories: [/etc/kubernetes/manifests /etc/kubernetes/pki]
[reset] Deleting files: [/etc/kubernetes/admin.conf /etc/kubernetes/kubelet.conf /etc/kubernetes/bootstrap-kubelet.conf /etc/kubernetes/controller-manager.conf /etc/kubernetes/scheduler.conf]
[reset] Deleting contents of stateful directories: [/var/lib/kubelet /var/lib/dockershim /var/run/kubernetes /var/lib/cni]

The reset process does not clean CNI configuration. To do so, you must remove /etc/cni/net.d

The reset process does not reset or clean up iptables rules or IPVS tables.
If you wish to reset iptables, you must do so manually by using the "iptables" command.

If your cluster was setup to utilize IPVS, run ipvsadm --clear (or similar)
to reset your system's IPVS tables.

The reset process does not clean your kubeconfig files and you must remove them manually.
Please, check the contents of the $HOME/.kube/config file.
2、手动清除配置信息
（1）清除遗留文件
[root@node1 ~]# rm -rf /root/.kube
[root@node1 ~]# rm -rf /etc/cni/net.d
[root@node1 ~]# rm -rf /etc/kubernetes/*
（2）清理ipvsadm
[root@node1 ~]# ipvsadm -C
-bash: ipvsadm: command not found
[root@node1 ~]# iptables -F && iptables -t nat -F && iptables -t mangle -F && iptables -X
3、重新加入集群
[root@node1 ~]# kubeadm join 192.168.0.212:6443 --token 5nokjb.i1okf7ljckcxdis8 \
> --discovery-token-ca-cert-hash sha256:aad97ca5808e6e01aec8f730bd900e6722573b3d2f830ac79e641837285c9600 
> [preflight] Running pre-flight checks
> [preflight] Reading configuration from the cluster...
> [preflight] FYI: You can look at this config file with 'kubectl -n kube-system get cm kubeadm-config -o yaml'
> [kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
> [kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
> [kubelet-start] Starting the kubelet
> [kubelet-start] Waiting for the kubelet to perform the TLS Bootstrap...

This node has joined the cluster:
* Certificate signing request was sent to apiserver and a response was received.
* The Kubelet was informed of the new secure connection details.

Run 'kubectl get nodes' on the control-plane to see this node join the cluster.

[root@node1 ~]# 
————————————————
版权声明：本文为CSDN博主「菜鸟小窝」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_25775675/article/details/128135528