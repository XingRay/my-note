## KubeSphere安装

https://www.kubesphere.io/zh/

https://www.kubesphere.io/zh/docs/v3.3/



### 安装部署k8s集群

略

注意：当前KubeSphere最新版为v3.3.2，支持的Kubernetes 版本必须为：v1.20.x、v1.21.x、* v1.22.x、* v1.23.x 和 * v1.24.x，最新版本为 1.24.15-00



### 安装KubeSphere前置环境

#### 配置nfs文件系统

略

主节点导出 /nfs/data， 工作节点挂载该目录



### 配置默认存储

```bash
vi storage-class.yaml
```

```yaml
## 创建了一个存储类
apiVersion: storage.k8s.io/v1
kind: StorageClass
metadata:
  name: nfs-storage
  annotations:
    storageclass.kubernetes.io/is-default-class: "true"
provisioner: k8s-sigs.io/nfs-subdir-external-provisioner
parameters:
  archiveOnDelete: "true"  ## 删除pv的时候，pv的内容是否要备份

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: nfs-client-provisioner
  labels:
    app: nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: default
spec:
  replicas: 1
  strategy:
    type: Recreate
  selector:
    matchLabels:
      app: nfs-client-provisioner
  template:
    metadata:
      labels:
        app: nfs-client-provisioner
    spec:
      serviceAccountName: nfs-client-provisioner
      containers:
        - name: nfs-client-provisioner
          image: registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/nfs-subdir-external-provisioner:v4.0.2
          # resources:
          #    limits:
          #      cpu: 10m
          #    requests:
          #      cpu: 10m
          volumeMounts:
            - name: nfs-client-root
              mountPath: /persistentvolumes
          env:
            - name: PROVISIONER_NAME
              value: k8s-sigs.io/nfs-subdir-external-provisioner
            - name: NFS_SERVER
              value: 192.168.0.112 ## 指定自己nfs服务器地址
            - name: NFS_PATH  
              value: /nfs/data  ## nfs服务器共享的目录
      volumes:
        - name: nfs-client-root
          nfs:
            server: 192.168.0.112
            path: /nfs/data
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: default
---
kind: ClusterRole
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: nfs-client-provisioner-runner
rules:
  - apiGroups: [""]
    resources: ["nodes"]
    verbs: ["get", "list", "watch"]
  - apiGroups: [""]
    resources: ["persistentvolumes"]
    verbs: ["get", "list", "watch", "create", "delete"]
  - apiGroups: [""]
    resources: ["persistentvolumeclaims"]
    verbs: ["get", "list", "watch", "update"]
  - apiGroups: ["storage.k8s.io"]
    resources: ["storageclasses"]
    verbs: ["get", "list", "watch"]
  - apiGroups: [""]
    resources: ["events"]
    verbs: ["create", "update", "patch"]
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: run-nfs-client-provisioner
subjects:
  - kind: ServiceAccount
    name: nfs-client-provisioner
    # replace with namespace where provisioner is deployed
    namespace: default
roleRef:
  kind: ClusterRole
  name: nfs-client-provisioner-runner
  apiGroup: rbac.authorization.k8s.io
---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: leader-locking-nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: default
rules:
  - apiGroups: [""]
    resources: ["endpoints"]
    verbs: ["get", "list", "watch", "create", "update", "patch"]
---
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: leader-locking-nfs-client-provisioner
  # replace with namespace where provisioner is deployed
  namespace: default
subjects:
  - kind: ServiceAccount
    name: nfs-client-provisioner
    # replace with namespace where provisioner is deployed
    namespace: default
roleRef:
  kind: Role
  name: leader-locking-nfs-client-provisioner
  apiGroup: rbac.authorization.k8s.io
```

主机将2处IP地址改为主节点，nfs服务器的ip地址，应用配置文件

```
kubectl apply -f storage-class.yaml
```

这个配置文件创建了资源，类型为存储类型，storageclass，查看存储类型

```bash
kubectl get storageclass
```

或者

```bash
kubectl get sc
```

```
root@k8s-master01:~# kubectl get storageclass
NAME                    PROVISIONER                                   RECLAIMPOLICY   VOLUMEBINDINGMODE   ALLOWVOLUMEEXPANSION   AGE
nfs-storage (default)   k8s-sigs.io/nfs-subdir-external-provisioner   Delete          Immediate           false                  35s
```

可以看到有一个存储类型 nfs-storage (default)  ，default表示是默认的存储类型。

现在就可以使用pv池的动态供应了，测试：

```bash
vi pvc-dynamic.yaml
```

```
kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: pvc-dynamic
spec:
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 200Mi
# 注意不要指定storageClassName，这样会使用系统默认的存储类型，或者指定为 nfs-storage
#  storageClassName: nfs
```

```bash
kubectl apply -f pvc-dynamic.yaml
```

```bash
root@k8s-master01:~# kubectl apply -f pvc-dynamic.yaml
persistentvolumeclaim/pvc-dynamic created
```

查看创建的pvc

```bash
root@k8s-master01:~# kubectl get pvc
NAME          STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
pvc-dynamic   Bound    pvc-db63fa75-898c-4186-8f4f-4f3a29740b03   200Mi      RWX            nfs-storage    39s
```

可以看到这个pvc目前是绑定状态，绑定了一个pv，下面查看pv

```bash
root@k8s-master01:~# kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                 STORAGECLASS   REASON   AGE
pvc-db63fa75-898c-4186-8f4f-4f3a29740b03   200Mi      RWX            Delete           Bound    default/pvc-dynamic   nfs-storage             88s
```

这个pv时自动创建的，而且pvc声明空间是多少，这里pv的容量就是多少。



### metrics-server

集群指标监控组件，监控集群的cpu占用率，内存占用率等。

```bash
vi metrics-server.yaml
```

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  labels:
    k8s-app: metrics-server
  name: metrics-server
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  labels:
    k8s-app: metrics-server
    rbac.authorization.k8s.io/aggregate-to-admin: "true"
    rbac.authorization.k8s.io/aggregate-to-edit: "true"
    rbac.authorization.k8s.io/aggregate-to-view: "true"
  name: system:aggregated-metrics-reader
rules:
- apiGroups:
  - metrics.k8s.io
  resources:
  - pods
  - nodes
  verbs:
  - get
  - list
  - watch
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  labels:
    k8s-app: metrics-server
  name: system:metrics-server
rules:
- apiGroups:
  - ""
  resources:
  - pods
  - nodes
  - nodes/stats
  - namespaces
  - configmaps
  verbs:
  - get
  - list
  - watch
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  labels:
    k8s-app: metrics-server
  name: metrics-server-auth-reader
  namespace: kube-system
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: extension-apiserver-authentication-reader
subjects:
- kind: ServiceAccount
  name: metrics-server
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  labels:
    k8s-app: metrics-server
  name: metrics-server:system:auth-delegator
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:auth-delegator
subjects:
- kind: ServiceAccount
  name: metrics-server
  namespace: kube-system
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  labels:
    k8s-app: metrics-server
  name: system:metrics-server
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: system:metrics-server
subjects:
- kind: ServiceAccount
  name: metrics-server
  namespace: kube-system
---
apiVersion: v1
kind: Service
metadata:
  labels:
    k8s-app: metrics-server
  name: metrics-server
  namespace: kube-system
spec:
  ports:
  - name: https
    port: 443
    protocol: TCP
    targetPort: https
  selector:
    k8s-app: metrics-server
---
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    k8s-app: metrics-server
  name: metrics-server
  namespace: kube-system
spec:
  selector:
    matchLabels:
      k8s-app: metrics-server
  strategy:
    rollingUpdate:
      maxUnavailable: 0
  template:
    metadata:
      labels:
        k8s-app: metrics-server
    spec:
      containers:
      - args:
        - --cert-dir=/tmp
        - --kubelet-insecure-tls
        - --secure-port=4443
        - --kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname
        - --kubelet-use-node-status-port
        image: registry.cn-hangzhou.aliyuncs.com/lfy_k8s_images/metrics-server:v0.4.3
        imagePullPolicy: IfNotPresent
        livenessProbe:
          failureThreshold: 3
          httpGet:
            path: /livez
            port: https
            scheme: HTTPS
          periodSeconds: 10
        name: metrics-server
        ports:
        - containerPort: 4443
          name: https
          protocol: TCP
        readinessProbe:
          failureThreshold: 3
          httpGet:
            path: /readyz
            port: https
            scheme: HTTPS
          periodSeconds: 10
        securityContext:
          readOnlyRootFilesystem: true
          runAsNonRoot: true
          runAsUser: 1000
        volumeMounts:
        - mountPath: /tmp
          name: tmp-dir
      nodeSelector:
        kubernetes.io/os: linux
      priorityClassName: system-cluster-critical
      serviceAccountName: metrics-server
      volumes:
      - emptyDir: {}
        name: tmp-dir
---
apiVersion: apiregistration.k8s.io/v1
kind: APIService
metadata:
  labels:
    k8s-app: metrics-server
  name: v1beta1.metrics.k8s.io
spec:
  group: metrics.k8s.io
  groupPriorityMinimum: 100
  insecureSkipTLSVerify: true
  service:
    name: metrics-server
    namespace: kube-system
  version: v1beta1
  versionPriority: 100
```

```bash
kubectl apply -f metrics-server.yaml
```

安装完成后查看pod

```bash
kubectl get pod -A
```

```bash
root@k8s-master01:~# kubectl get pod -A
NAMESPACE              NAME                                         READY   STATUS      RESTARTS      AGE
kube-system            metrics-server-69b7f7bb96-bzxm7              1/1     Running     0             63s
```

等 metrics-server   正常运行，执行测试命令：

```bash
kubectl top nodes
```

```bash
root@k8s-master01:~# kubectl top nodes
NAME           CPU(cores)   CPU%   MEMORY(bytes)   MEMORY%
k8s-master01   51m          1%     1784Mi          47%
k8s-node01     13m          0%     1312Mi          34%
k8s-node02     7m           0%     1516Mi          40%
```

说明metrics服务正常运行，这里的指标中 cpu的指标是 1核心=1000m，查看pod的指标

```bash
kubectl top pods -A
```

```bash
root@k8s-master01:~# kubectl top pods -A
NAMESPACE              NAME                                         CPU(cores)   MEMORY(bytes)
default                nfs-client-provisioner-58c776bc57-h8wjm      2m           6Mi
ingress-nginx          ingress-nginx-controller-5d6b74cf5c-td4c5    1m           91Mi
kube-flannel           kube-flannel-ds-5bxr7                        6m           13Mi
kube-flannel           kube-flannel-ds-mbs46                        6m           12Mi
kube-flannel           kube-flannel-ds-nzn9w                        5m           12Mi
kube-system            coredns-7bdc4cb885-64zwl                     2m           13Mi
kube-system            coredns-7bdc4cb885-txnlf                     2m           13Mi
kube-system            etcd-k8s-master01                            22m          51Mi
kube-system            kube-apiserver-k8s-master01                  38m          267Mi
kube-system            kube-controller-manager-k8s-master01         18m          45Mi
kube-system            kube-proxy-6lt6z                             1m           15Mi
kube-system            kube-proxy-9ssx8                             1m           16Mi
kube-system            kube-proxy-dj74d                             1m           16Mi
kube-system            kube-scheduler-k8s-master01                  5m           19Mi
kube-system            metrics-server-69b7f7bb96-bzxm7              3m           13Mi
kubernetes-dashboard   dashboard-metrics-scraper-5cb4f4bb9c-fwg8q   1m           8Mi
kubernetes-dashboard   kubernetes-dashboard-6967859bff-tkj69        1m           11Mi
```

可以看到各个pod占用的cpu资源和内存资源



### 安装KubeSphere

```bash
kubectl apply -f https://github.com/kubesphere/ks-installer/releases/download/v3.3.2/kubesphere-installer.yaml
kubectl apply -f https://github.com/kubesphere/ks-installer/releases/download/v3.3.2/cluster-configuration.yaml
```

无法下载就使用浏览器下载，下载后 kubesphere-installer.yaml 不需要修改，

修改cluster-configuration.yaml

```yaml
spec:
  persistence:
    storageClass: ""        # If there is no default StorageClass in your cluster, you need to specify an existing StorageClass here.
```

storageClass 如果没有默认存储类型需要在这里指定存储类型，已经配置了默认存储类型这里就不需要修改。下面就是把各个功能开关的false改为true。false表示关闭某项功能，这里使用全功能，全部改为true。参考： 启用可插拔组件

https://www.kubesphere.io/zh/docs/v3.3/pluggable-components/

```yaml
etcd:
    monitoring: true       # Enable or disable etcd monitoring dashboard installation. You have to create a Secret for etcd before you enable it.
    endpointIps: 192.168.0.112  # etcd cluster EndpointIps. It can be a bunch of IPs here.
```

开启etcd的监控功能，注意ip地址要修改为主节点的集群私有ip地址。

网络监控这里如果安装的网络插件是calico，type:就设置为calico， 这里k8s集群使用的是 flannel，这里就填了 flannel

```
  network:
    networkpolicy: # Network policies allow network isolation within the same cluster, which means firewalls can be set up between certain instances (Pods).
      # Make sure that the CNI network plugin used by the cluster supports NetworkPolicy. There are a number of CNI network plugins that support NetworkPolicy, including Calico, Cilium, Kube-router, Romana and Weave Net.
      enabled: true # Enable or disable network policies.
    ippool: # Use Pod IP Pools to manage the Pod network address space. Pods to be created can be assigned IP addresses from a Pod IP Pool.
      type: flannel # Specify "calico" for this field if Calico is used as your CNI plugin. "none" means that Pod IP Pools are disabled.
```



其他的功能根据情况都可以打开即可。使用vi编辑器在服务器中修改：

```bash
vi cluster-configuration-updated.yaml
```

或者先修改后再上传到服务器中，修改后文文件示例：

```yaml
---
apiVersion: installer.kubesphere.io/v1alpha1
kind: ClusterConfiguration
metadata:
  name: ks-installer
  namespace: kubesphere-system
  labels:
    version: v3.3.2
spec:
  persistence:
    storageClass: ""        # If there is no default StorageClass in your cluster, you need to specify an existing StorageClass here.
  authentication:
    # adminPassword: ""     # Custom password of the admin user. If the parameter exists but the value is empty, a random password is generated. If the parameter does not exist, P@88w0rd is used.
    jwtSecret: ""           # Keep the jwtSecret consistent with the Host Cluster. Retrieve the jwtSecret by executing "kubectl -n kubesphere-system get cm kubesphere-config -o yaml | grep -v "apiVersion" | grep jwtSecret" on the Host Cluster.
  local_registry: ""        # Add your private registry address if it is needed.
  # dev_tag: ""               # Add your kubesphere image tag you want to install, by default it's same as ks-installer release version.
  etcd:
    monitoring: true       # Enable or disable etcd monitoring dashboard installation. You have to create a Secret for etcd before you enable it.
    endpointIps: 192.168.0.112  # etcd cluster EndpointIps. It can be a bunch of IPs here.
    port: 2379              # etcd port.
    tlsEnable: true
  common:
    core:
      console:
        enableMultiLogin: true  # Enable or disable simultaneous logins. It allows different users to log in with the same account at the same time.
        port: 30880
        type: NodePort

    # apiserver:            # Enlarge the apiserver and controller manager's resource requests and limits for the large cluster
    #  resources: {}
    # controllerManager:
    #  resources: {}
    redis:
      enabled: true
      enableHA: false
      volumeSize: 2Gi # Redis PVC size.
    openldap:
      enabled: true
      volumeSize: 2Gi   # openldap PVC size.
    minio:
      volumeSize: 20Gi # Minio PVC size.
    monitoring:
      # type: external   # Whether to specify the external prometheus stack, and need to modify the endpoint at the next line.
      endpoint: http://prometheus-operated.kubesphere-monitoring-system.svc:9090 # Prometheus endpoint to get metrics data.
      GPUMonitoring:     # Enable or disable the GPU-related metrics. If you enable this switch but have no GPU resources, Kubesphere will set it to zero.
        enabled: false
    gpu:                 # Install GPUKinds. The default GPU kind is nvidia.com/gpu. Other GPU kinds can be added here according to your needs.
      kinds:
      - resourceName: "nvidia.com/gpu"
        resourceType: "GPU"
        default: false
    es:   # Storage backend for logging, events and auditing.
      # master:
      #   volumeSize: 4Gi  # The volume size of Elasticsearch master nodes.
      #   replicas: 1      # The total number of master nodes. Even numbers are not allowed.
      #   resources: {}
      # data:
      #   volumeSize: 20Gi  # The volume size of Elasticsearch data nodes.
      #   replicas: 1       # The total number of data nodes.
      #   resources: {}
      logMaxAge: 7             # Log retention time in built-in Elasticsearch. It is 7 days by default.
      elkPrefix: logstash      # The string making up index names. The index name will be formatted as ks-<elk_prefix>-log.
      basicAuth:
        enabled: false
        username: ""
        password: ""
      externalElasticsearchHost: ""
      externalElasticsearchPort: ""
  alerting:                # (CPU: 0.1 Core, Memory: 100 MiB) It enables users to customize alerting policies to send messages to receivers in time with different time intervals and alerting levels to choose from.
    enabled: true         # Enable or disable the KubeSphere Alerting System.
    # thanosruler:
    #   replicas: 1
    #   resources: {}
  auditing:                # Provide a security-relevant chronological set of records，recording the sequence of activities happening on the platform, initiated by different tenants.
    enabled: true         # Enable or disable the KubeSphere Auditing Log System.
    # operator:
    #   resources: {}
    # webhook:
    #   resources: {}
  devops:                  # (CPU: 0.47 Core, Memory: 8.6 G) Provide an out-of-the-box CI/CD system based on Jenkins, and automated workflow tools including Source-to-Image & Binary-to-Image.
    enabled: true             # Enable or disable the KubeSphere DevOps System.
    # resources: {}
    jenkinsMemoryLim: 4Gi      # Jenkins memory limit.
    jenkinsMemoryReq: 2Gi   # Jenkins memory request.
    jenkinsVolumeSize: 8Gi     # Jenkins volume size.
  events:                  # Provide a graphical web console for Kubernetes Events exporting, filtering and alerting in multi-tenant Kubernetes clusters.
    enabled: true         # Enable or disable the KubeSphere Events System.
    # operator:
    #   resources: {}
    # exporter:
    #   resources: {}
    # ruler:
    #   enabled: true
    #   replicas: 2
    #   resources: {}
  logging:                 # (CPU: 57 m, Memory: 2.76 G) Flexible logging functions are provided for log query, collection and management in a unified console. Additional log collectors can be added, such as Elasticsearch, Kafka and Fluentd.
    enabled: true         # Enable or disable the KubeSphere Logging System.
    logsidecar:
      enabled: true
      replicas: 2
      # resources: {}
  metrics_server:                    # (CPU: 56 m, Memory: 44.35 MiB) It enables HPA (Horizontal Pod Autoscaler).
    enabled: false                   # Enable or disable metrics-server.
  monitoring:
    storageClass: ""                 # If there is an independent StorageClass you need for Prometheus, you can specify it here. The default StorageClass is used by default.
    node_exporter:
      port: 9100
      # resources: {}
    # kube_rbac_proxy:
    #   resources: {}
    # kube_state_metrics:
    #   resources: {}
    # prometheus:
    #   replicas: 1  # Prometheus replicas are responsible for monitoring different segments of data source and providing high availability.
    #   volumeSize: 20Gi  # Prometheus PVC size.
    #   resources: {}
    #   operator:
    #     resources: {}
    # alertmanager:
    #   replicas: 1          # AlertManager Replicas.
    #   resources: {}
    # notification_manager:
    #   resources: {}
    #   operator:
    #     resources: {}
    #   proxy:
    #     resources: {}
    gpu:                           # GPU monitoring-related plug-in installation.
      nvidia_dcgm_exporter:        # Ensure that gpu resources on your hosts can be used normally, otherwise this plug-in will not work properly.
        enabled: false             # Check whether the labels on the GPU hosts contain "nvidia.com/gpu.present=true" to ensure that the DCGM pod is scheduled to these nodes.
        # resources: {}
  multicluster:
    clusterRole: none  # host | member | none  # You can install a solo cluster, or specify it as the Host or Member Cluster.
  network:
    networkpolicy: # Network policies allow network isolation within the same cluster, which means firewalls can be set up between certain instances (Pods).
      # Make sure that the CNI network plugin used by the cluster supports NetworkPolicy. There are a number of CNI network plugins that support NetworkPolicy, including Calico, Cilium, Kube-router, Romana and Weave Net.
      enabled: true # Enable or disable network policies.
    ippool: # Use Pod IP Pools to manage the Pod network address space. Pods to be created can be assigned IP addresses from a Pod IP Pool.
      type: flannel # Specify "calico" for this field if Calico is used as your CNI plugin. "none" means that Pod IP Pools are disabled.
    topology: # Use Service Topology to view Service-to-Service communication based on Weave Scope.
      type: none # Specify "weave-scope" for this field to enable Service Topology. "none" means that Service Topology is disabled.
  openpitrix: # An App Store that is accessible to all platform tenants. You can use it to manage apps across their entire lifecycle.
    store:
      enabled: true # Enable or disable the KubeSphere App Store.
  servicemesh:         # (0.3 Core, 300 MiB) Provide fine-grained traffic management, observability and tracing, and visualized traffic topology.
    enabled: true     # Base component (pilot). Enable or disable KubeSphere Service Mesh (Istio-based).
    istio:  # Customizing the istio installation configuration, refer to https://istio.io/latest/docs/setup/additional-setup/customize-installation/
      components:
        ingressGateways:
        - name: istio-ingressgateway
          enabled: false
        cni:
          enabled: false
  edgeruntime:          # Add edge nodes to your cluster and deploy workloads on edge nodes.
    enabled: false
    kubeedge:        # kubeedge configurations
      enabled: true
      cloudCore:
        cloudHub:
          advertiseAddress: # At least a public IP address or an IP address which can be accessed by edge nodes must be provided.
            - ""            # Note that once KubeEdge is enabled, CloudCore will malfunction if the address is not provided.
        service:
          cloudhubNodePort: "30000"
          cloudhubQuicNodePort: "30001"
          cloudhubHttpsNodePort: "30002"
          cloudstreamNodePort: "30003"
          tunnelNodePort: "30004"
        # resources: {}
        # hostNetWork: false
      iptables-manager:
        enabled: true 
        mode: "external"
        # resources: {}
      # edgeService:
      #   resources: {}
  gatekeeper:        # Provide admission policy and rule management, A validating (mutating TBA) webhook that enforces CRD-based policies executed by Open Policy Agent.
    enabled: false   # Enable or disable Gatekeeper.
    # controller_manager:
    #   resources: {}
    # audit:
    #   resources: {}
  terminal:
    # image: 'alpine:3.15' # There must be an nsenter program in the image
    timeout: 600         # Container timeout, if set to 0, no timeout will be used. The unit is seconds
```

修改完成后保存为 cluster-configuration-updated.yaml。执行

```bash
kubectl apply -f kubesphere-installer.yaml
```

```bash
kubectl apply -f cluster-configuration-updated.yaml
```



安装过程可以使用指令监控：

```bash
kubectl logs -n kubesphere-system $(kubectl get pod -n kubesphere-system -l 'app in (ks-install, ks-installer)' -o jsonpath='{.items[0].metadata.name}') -f
```

等待一段时候后输出：

```bash
#####################################################
###              Welcome to KubeSphere!           ###
#####################################################

Console: http://192.168.0.112:30880
Account: admin
Password: P@88w0rd
NOTES：
  1. After you log into the console, please check the
     monitoring status of service components in
     "Cluster Management". If any service is not
     ready, please wait patiently until all components
     are up and running.
  2. Please change the default password after login.

#####################################################
https://kubesphere.io             2023-06-21 12:27:11
#####################################################
```

则说明安装完成了。安装完成之后再查看所有pod的状态，执行

```bash
kubectl get pod -A
```

要等所有的pod都是正常运行才能说明初始化完成，这时会发先 pod  prometheus-k8s 一直初始化失败，查看原因：

```bash
kubectl describe pod prometheus-k8s-0 -n kubesphere-monitoring-system
```

```bash
 Events:
  Type     Reason            Age                  From               Message
  ----     ------            ----                 ----               -------
  Warning  FailedScheduling  22m                  default-scheduler  0/3 nodes are available: 3 pod has unbound immediate PersistentVolumeClaims. preemption: 0/3 nodes are available: 3 Preemption is not helpful for scheduling.
  Normal   Scheduled         22m                  default-scheduler  Successfully assigned kubesphere-monitoring-system/prometheus-k8s-0 to k8s-node02
  Warning  FailedMount       13m                  kubelet            Unable to attach or mount volumes: unmounted volumes=[secret-kube-etcd-client-certs], unattached volumes=[config config-out prometheus-k8s-rulefiles-0 kube-api-access-c6mj8 tls-assets prometheus-k8s-db web-config secret-kube-etcd-client-certs]: timed out waiting for the condition
  Warning  FailedMount       11m                  kubelet            Unable to attach or mount volumes: unmounted volumes=[secret-kube-etcd-client-certs], unattached volumes=[secret-kube-etcd-client-certs config config-out prometheus-k8s-rulefiles-0 kube-api-access-c6mj8 tls-assets prometheus-k8s-db web-config]: timed out waiting for the condition
  Warning  FailedMount       6m45s (x2 over 17m)  kubelet            Unable to attach or mount volumes: unmounted volumes=[secret-kube-etcd-client-certs], unattached volumes=[tls-assets prometheus-k8s-db web-config secret-kube-etcd-client-certs config config-out prometheus-k8s-rulefiles-0 kube-api-access-c6mj8]: timed out waiting for the condition
  Warning  FailedMount       4m29s (x3 over 20m)  kubelet            Unable to attach or mount volumes: unmounted volumes=[secret-kube-etcd-client-certs], unattached volumes=[web-config secret-kube-etcd-client-certs config config-out prometheus-k8s-rulefiles-0 kube-api-access-c6mj8 tls-assets prometheus-k8s-db]: timed out waiting for the condition
  Warning  FailedMount       2m13s (x2 over 15m)  kubelet            Unable to attach or mount volumes: unmounted volumes=[secret-kube-etcd-client-certs], unattached volumes=[kube-api-access-c6mj8 tls-assets prometheus-k8s-db web-config secret-kube-etcd-client-certs config config-out prometheus-k8s-rulefiles-0]: timed out waiting for the condition
  Warning  FailedMount       109s (x18 over 22m)  kubelet            MountVolume.SetUp failed for volume "secret-kube-etcd-client-certs" : secret "kube-etcd-client-certs" not found
```

原因是 etcd监控证书找不到，执行：

```bash
kubectl -n kubesphere-monitoring-system create secret generic kube-etcd-client-certs  --from-file=etcd-client-ca.crt=/etc/kubernetes/pki/etcd/ca.crt  --from-file=etcd-client.crt=/etc/kubernetes/pki/apiserver-etcd-client.crt  --from-file=etcd-client.key=/etc/kubernetes/pki/apiserver-etcd-client.key
```

经过一段时间等待之后，所有的pod都初始化成功：

```bash
root@k8s-master01:~# kubectl get pod -A
NAMESPACE                      NAME                                                       READY   STATUS      RESTARTS      AGE
argocd                         devops-argocd-application-controller-0                     1/1     Running     0             19m
argocd                         devops-argocd-applicationset-controller-5869494cd7-95vmk   1/1     Running     0             24m
argocd                         devops-argocd-dex-server-7474bf98bd-gztgx                  1/1     Running     0             24m
argocd                         devops-argocd-notifications-controller-566f997c7-x2w8g     1/1     Running     0             24m
argocd                         devops-argocd-redis-574f87f4b-ps64f                        1/1     Running     0             24m
argocd                         devops-argocd-repo-server-58779844b8-7hkrc                 1/1     Running     0             24m
argocd                         devops-argocd-server-766fbc5597-xmhzr                      1/1     Running     0             24m
default                        nfs-client-provisioner-74966b6476-2zd82                    1/1     Running     0             47m
istio-system                   istiod-1-11-2-f84d77d8d-d7qsf                              1/1     Running     0             38m
istio-system                   jaeger-collector-7fd6c7d5d8-szdfg                          1/1     Running     0             33m
istio-system                   jaeger-operator-64d7c95889-9qpfv                           1/1     Running     0             36m
istio-system                   jaeger-query-6787f54877-hqt4w                              2/2     Running     0             31m
istio-system                   kiali-7bcb64795d-kt2dd                                     1/1     Running     0             32m
istio-system                   kiali-operator-6c7c999d74-7hrkl                            1/1     Running     0             36m
kube-flannel                   kube-flannel-ds-4ss9x                                      1/1     Running     0             57m
kube-flannel                   kube-flannel-ds-l2hsm                                      1/1     Running     0             58m
kube-flannel                   kube-flannel-ds-zp7wf                                      1/1     Running     0             57m
kube-system                    coredns-74586cf9b6-tnt29                                   1/1     Running     0             60m
kube-system                    coredns-74586cf9b6-zxq6j                                   1/1     Running     0             60m
kube-system                    etcd-k8s-master01                                          1/1     Running     0             60m
kube-system                    kube-apiserver-k8s-master01                                1/1     Running     0             60m
kube-system                    kube-controller-manager-k8s-master01                       1/1     Running     0             60m
kube-system                    kube-proxy-mlvcj                                           1/1     Running     0             57m
kube-system                    kube-proxy-shl9v                                           1/1     Running     0             60m
kube-system                    kube-proxy-v2l4z                                           1/1     Running     0             57m
kube-system                    kube-scheduler-k8s-master01                                1/1     Running     0             60m
kube-system                    metrics-server-557b4967b9-qpbhv                            1/1     Running     0             24m
kube-system                    snapshot-controller-0                                      1/1     Running     0             19m
kubesphere-controls-system     default-http-backend-69478ff5f9-5m68c                      1/1     Running     0             24m
kubesphere-controls-system     kubectl-admin-568b698d7-trvhx                              1/1     Running     0             18m
kubesphere-devops-system       devops-28122030-sffwv                                      0/1     Completed   0             25m
kubesphere-devops-system       devops-apiserver-766d59f888-6slwr                          1/1     Running     0             37m
kubesphere-devops-system       devops-controller-79fdc8d6d8-7tjhp                         1/1     Running     0             24m
kubesphere-devops-system       devops-jenkins-694d78fbdc-pk792                            1/1     Running     3 (18m ago)   24m
kubesphere-devops-system       s2ioperator-0                                              1/1     Running     0             37m
kubesphere-logging-system      elasticsearch-logging-data-0                               1/1     Running     0             40m
kubesphere-logging-system      elasticsearch-logging-data-1                               1/1     Running     0             18m
kubesphere-logging-system      elasticsearch-logging-discovery-0                          1/1     Running     0             19m
kubesphere-logging-system      fluent-bit-4qcqm                                           1/1     Running     0             37m
kubesphere-logging-system      fluent-bit-rmgnm                                           1/1     Running     0             37m
kubesphere-logging-system      fluent-bit-wqxsf                                           1/1     Running     0             37m
kubesphere-logging-system      fluentbit-operator-577b4b94bd-jj44q                        1/1     Running     0             24m
kubesphere-logging-system      ks-events-exporter-d9b4b9cc6-75bsp                         2/2     Running     0             37m
kubesphere-logging-system      ks-events-operator-58ffc98496-fvfqs                        1/1     Running     0             38m
kubesphere-logging-system      ks-events-ruler-77c579687f-lh5vh                           2/2     Running     0             24m
kubesphere-logging-system      ks-events-ruler-77c579687f-v5sdh                           2/2     Running     0             37m
kubesphere-logging-system      kube-auditing-operator-5bbc8cb47-s7brr                     1/1     Running     0             24m
kubesphere-logging-system      kube-auditing-webhook-deploy-6bfd6f7976-d49cz              1/1     Running     0             37m
kubesphere-logging-system      kube-auditing-webhook-deploy-6bfd6f7976-hvxf6              1/1     Running     0             24m
kubesphere-logging-system      logsidecar-injector-deploy-7c458bc857-hd6mw                2/2     Running     0             38m
kubesphere-logging-system      logsidecar-injector-deploy-7c458bc857-pxbtq                2/2     Running     0             24m
kubesphere-monitoring-system   alertmanager-main-0                                        2/2     Running     0             34m
kubesphere-monitoring-system   alertmanager-main-1                                        2/2     Running     0             18m
kubesphere-monitoring-system   alertmanager-main-2                                        2/2     Running     0             34m
kubesphere-monitoring-system   kube-state-metrics-6f6ffbf895-ztxtd                        3/3     Running     0             36m
kubesphere-monitoring-system   node-exporter-b5zr2                                        2/2     Running     0             36m
kubesphere-monitoring-system   node-exporter-p94sm                                        2/2     Running     0             36m
kubesphere-monitoring-system   node-exporter-tj2r6                                        2/2     Running     0             36m
kubesphere-monitoring-system   notification-manager-deployment-77d5b49896-f4g7n           2/2     Running     4 (24m ago)   24m
kubesphere-monitoring-system   notification-manager-deployment-77d5b49896-wfs4l           2/2     Running     0             19m
kubesphere-monitoring-system   notification-manager-operator-66c6967d78-jwv4p             2/2     Running     0             36m
kubesphere-monitoring-system   prometheus-k8s-0                                           2/2     Running     0             16m
kubesphere-monitoring-system   prometheus-k8s-1                                           2/2     Running     0             19m
kubesphere-monitoring-system   prometheus-operator-b56bb98c4-ssvpw                        2/2     Running     0             36m
kubesphere-monitoring-system   thanos-ruler-kubesphere-0                                  2/2     Running     0             34m
kubesphere-monitoring-system   thanos-ruler-kubesphere-1                                  2/2     Running     0             18m
kubesphere-system              ks-apiserver-fdc78597b-kd8wp                               1/1     Running     0             28m
kubesphere-system              ks-console-68f9d9d945-t42wf                                1/1     Running     0             28m
kubesphere-system              ks-controller-manager-68455bfd6c-v4sb2                     1/1     Running     0             18m
kubesphere-system              ks-installer-97b474f7c-5bjs7                               1/1     Running     0             42m
kubesphere-system              minio-68df76c95f-ckrqq                                     1/1     Running     0             41m
kubesphere-system              openldap-0                                                 1/1     Running     0             19m
kubesphere-system              openpitrix-import-job-4rz27                                0/1     Completed   0             24m
kubesphere-system              redis-6d6cd48876-d2wn5                                     1/1     Running     0             41m
```



### devops-jenkins 初始化失败问题

有可能会遇到 devops-jenkins 初始化失败显示原因为 OOM Killed的问题，这个一般是由于资源配置不足导致。解决办法：调整 devops-jenkins 部署配置中的JVM 配置，增加 最大堆大小 和 初始堆大小

```bash
-Xmx：最大堆大小
-Xms：初始堆大小
```

设置初始堆为 512m，最大堆为 4G。查找部署文件：

```bash
kubectl get deploy -A
```

```bash
root@k8s-master01:~# kubectl get deploy -A
NAMESPACE                      NAME                                      READY   UP-TO-DATE   AVAILABLE   AGE
argocd                         devops-argocd-applicationset-controller   1/1     1            1           13m
argocd                         devops-argocd-dex-server                  1/1     1            1           13m
argocd                         devops-argocd-notifications-controller    1/1     1            1           13m
argocd                         devops-argocd-redis                       1/1     1            1           13m
argocd                         devops-argocd-repo-server                 1/1     1            1           13m
argocd                         devops-argocd-server                      1/1     1            1           13m
default                        nfs-client-provisioner                    1/1     1            1           20m
istio-system                   istiod-1-11-2                             1/1     1            1           13m
istio-system                   jaeger-operator                           1/1     1            1           13m
istio-system                   kiali                                     1/1     1            1           11m
istio-system                   kiali-operator                            1/1     1            1           12m
kube-system                    coredns                                   2/2     2            2           35m
kube-system                    metrics-server                            1/1     1            1           19m
kubesphere-controls-system     default-http-backend                      1/1     1            1           14m
kubesphere-controls-system     kubectl-admin                             1/1     1            1           10m
kubesphere-devops-system       devops-apiserver                          1/1     1            1           12m
kubesphere-devops-system       devops-controller                         1/1     1            1           12m
kubesphere-devops-system       devops-jenkins                            0/1     1            0           12m
kubesphere-logging-system      fluentbit-operator                        1/1     1            1           15m
kubesphere-logging-system      ks-events-exporter                        1/1     1            1           13m
kubesphere-logging-system      ks-events-operator                        1/1     1            1           13m
kubesphere-logging-system      ks-events-ruler                           2/2     2            2           13m
kubesphere-logging-system      kube-auditing-operator                    1/1     1            1           13m
kubesphere-logging-system      kube-auditing-webhook-deploy              2/2     2            2           13m
kubesphere-logging-system      logsidecar-injector-deploy                2/2     2            2           13m
kubesphere-monitoring-system   kube-state-metrics                        1/1     1            1           11m
kubesphere-monitoring-system   notification-manager-deployment           2/2     2            2           10m
kubesphere-monitoring-system   notification-manager-operator             1/1     1            1           11m
kubesphere-monitoring-system   prometheus-operator                       1/1     1            1           11m
kubesphere-system              ks-apiserver                              1/1     1            1           14m
kubesphere-system              ks-console                                1/1     1            1           14m
kubesphere-system              ks-controller-manager                     1/1     1            1           14m
kubesphere-system              ks-installer                              1/1     1            1           17m
kubesphere-system              minio                                     1/1     1            1           16m
kubesphere-system              redis                                     1/1     1            1           16m
```

可以找到部署配置为：

```bash
kubesphere-devops-system       devops-jenkins                            0/1     1            0           12m
```

编辑部署文件：

```bash
kubectl -n kubesphere-devops-system edit deployment devops-jenkins
```

修改下面的节点：

```yaml
spec:
  template:
    spec:
      containers:
      - args:
        env:
        - name: JAVA_TOOL_OPTIONS
          value: '-XX:MaxRAMPercentage=80 -XX:MinRAMPercentage=60 -Dhudson.slaves.NodeProvisioner.initialDelay=20
            -Dhudson.slaves.NodeProvisioner.MARGIN=50 -Dhudson.slaves.NodeProvisioner.MARGIN0=0.85
            -Dhudson.model.LoadStatistics.clock=5000 -Dhudson.model.LoadStatistics.decay=0.2
            -Dhudson.slaves.NodeProvisioner.recurrencePeriod=5000 -Dhudson.security.csrf.DefaultCrumbIssuer.EXCLUDE_SESSION_ID=true
            -Dio.jenkins.plugins.casc.ConfigurationAsCode.initialDelay=10000 -Djenkins.install.runSetupWizard=false
            -XX:+AlwaysPreTouch -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC -XX:+UseStringDeduplication
            -XX:+ParallelRefProcEnabled -XX:+DisableExplicitGC -XX:+UnlockDiagnosticVMOptions
            -XX:+UnlockExperimentalVMOptions '
```

增加 JVM参数： `-Xmx4096m -Xms512m`

```yaml
spec:
  template:
    spec:
      containers:
      - args:
        env:
        - name: JAVA_TOOL_OPTIONS
          value: '-XX:MaxRAMPercentage=80 -XX:MinRAMPercentage=60 -Dhudson.slaves.NodeProvisioner.initialDelay=20
            -Dhudson.slaves.NodeProvisioner.MARGIN=50 -Dhudson.slaves.NodeProvisioner.MARGIN0=0.85
            -Dhudson.model.LoadStatistics.clock=5000 -Dhudson.model.LoadStatistics.decay=0.2
            -Dhudson.slaves.NodeProvisioner.recurrencePeriod=5000 -Dhudson.security.csrf.DefaultCrumbIssuer.EXCLUDE_SESSION_ID=true
            -Dio.jenkins.plugins.casc.ConfigurationAsCode.initialDelay=10000 -Djenkins.install.runSetupWizard=false
            -XX:+AlwaysPreTouch -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC -XX:+UseStringDeduplication
            -XX:+ParallelRefProcEnabled -XX:+DisableExplicitGC -XX:+UnlockDiagnosticVMOptions
            -XX:+UnlockExperimentalVMOptions 
            -Xmx4096m 
            -Xms512m '
```

保存退出即可



### 登录KubeSphere

http://192.168.0.112:30880 

账号：admin

密码：P@88w0rd
