## prometheus服务无法正常启动问题

安装完成后使用一段时间有可能会遇到 prometheus-k8s-0 或者 prometheus-k8s-1 启动一值报错，已通过指令查看原因：

```bash
kubectl describe pod prometheus-k8s-0 -n kubesphere-monitoring-system
```

可以看到启动失败的原因是  "open to many files"，这个问题的解决方案如下：

https://blog.csdn.net/qq_42969135/article/details/128616803



1 通过指令查看当前所有pod的状态

```bash
kubectl get pod -A
```

可以看到下面的pod列表

```bash
kubesphere-monitoring-system   alertmanager-main-0                                               2/2     Running     14 (23m ago)     5d20h
kubesphere-monitoring-system   alertmanager-main-1                                               2/2     Running     10 (23m ago)     2d20h
kubesphere-monitoring-system   alertmanager-main-2                                               2/2     Running     14 (23m ago)     5d20h
kubesphere-monitoring-system   kube-state-metrics-6f6ffbf895-ztxtd                               3/3     Running     21 (23m ago)     5d20h
kubesphere-monitoring-system   node-exporter-b5zr2                                               2/2     Running     14 (23m ago)     5d20h
kubesphere-monitoring-system   node-exporter-p94sm                                               2/2     Running     14 (23m ago)     5d20h
kubesphere-monitoring-system   node-exporter-tj2r6                                               2/2     Running     14 (23m ago)     5d20h
kubesphere-monitoring-system   notification-manager-deployment-77d5b49896-f4g7n                  2/2     Running     30 (72s ago)     5d20h
kubesphere-monitoring-system   notification-manager-deployment-77d5b49896-hqsrt                  2/2     Running     16 (23m ago)     2d20h
kubesphere-monitoring-system   notification-manager-operator-66c6967d78-jwv4p                    2/2     Running     14 (23m ago)     5d20h
kubesphere-monitoring-system   prometheus-k8s-0                                                  2/2     Running     2 (23m ago)      10h
kubesphere-monitoring-system   prometheus-k8s-1                                                  2/2     Running     0                10h
kubesphere-monitoring-system   prometheus-operator-b56bb98c4-ssvpw                               2/2     Running     14 (23m ago)     5d20h
kubesphere-monitoring-system   thanos-ruler-kubesphere-0                                         2/2     Running     14 (23m ago)     5d20h
kubesphere-monitoring-system   thanos-ruler-kubesphere-1                                         2/2     Running     10 (23m ago)     2d20h
```

正常情况下状态都是Running，如果 prometheus-k8s-0 或者 prometheus-k8s-1 一直初始化失败，查看原因：

```bash
kubectl describe pod prometheus-k8s-0 -n kubesphere-monitoring-system
```

原因如果是 "open to many files"，那么就需要删除**所有 prometheus-k8s 实例的pv中的数据** ，查看pv列表

```bash
kubectl get pv -n kubesphere-monitoring-system
```

结果如下：

```
root@k8s-master01:~#  kubectl get pv -n kubesphere-monitoring-system
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                                                              STORAGECLASS   REASON   AGE
pvc-122b3451-3bcb-4abf-8b98-81fbba292520   2Gi        RWO            Delete           Bound    mall-cloud/mysql-pvc                                               nfs-storage             2d15h
pvc-3ce4879f-0155-4cb1-a2de-1935547651ac   20Gi       RWO            Delete           Bound    kubesphere-system/minio                                            nfs-storage             5d20h
pvc-4478f869-cb9a-438b-809c-797a2d3c8742   8Gi        RWO            Delete           Bound    kubesphere-devops-system/devops-jenkins                            nfs-storage             5d20h
pvc-7212dc25-8a6b-4899-a8bd-1e287ef2e6d3   2Gi        RWO            Delete           Bound    mall-cloud/redis-pvc-mall-cloud-redis-0                            nfs-storage             2d12h
pvc-75089108-dad6-4a2f-ad83-ce35087f30c6   4Gi        RWO            Delete           Bound    kubesphere-logging-system/data-elasticsearch-logging-discovery-0   nfs-storage             5d20h
pvc-858db30c-8859-45f9-92dd-02a5b699df51   20Gi       RWO            Delete           Bound    kubesphere-logging-system/data-elasticsearch-logging-data-1        nfs-storage             5d20h
pvc-8ff10009-6537-4a34-933c-8e5363d13a51   20Gi       RWO            Delete           Bound    kubesphere-logging-system/data-elasticsearch-logging-data-0        nfs-storage             5d20h
pvc-91198f30-4160-438e-99ed-5fe0967b7784   5Gi        RWO            Delete           Bound    mall-cloud/data-rabbitm-cpl51p-rabbitmq-0                          nfs-storage             41h
pvc-a4d1b5f3-5861-4c64-86af-6f7e744bf548   20Gi       RWO            Delete           Bound    kubesphere-monitoring-system/prometheus-k8s-db-prometheus-k8s-0    nfs-storage             5d20h
pvc-bcbbb9e9-1166-42e7-aae6-8bd36849bbe5   8Gi        RWO            Delete           Bound    mall-cloud/data-zookeeper-d2kls6-0                                 nfs-storage             40h
pvc-d6314620-0a92-4e23-98e0-1010fe5f5db8   2Gi        RWO            Delete           Bound    kubesphere-system/openldap-pvc-openldap-0                          nfs-storage             5d20h
pvc-d9f0ff1c-4357-41ea-bc8d-1030517fc71f   20Gi       RWO            Delete           Bound    kubesphere-monitoring-system/prometheus-k8s-db-prometheus-k8s-1    nfs-storage             5d20h
pvc-f2c96e05-301b-4e2f-8058-8abddbc3748b   5Gi        RWO            Delete           Bound    mall-cloud/es-pvc-mall-cloud-es-0                                  nfs-storage             41h
pvc-f80be1b5-2947-457f-908f-2d1d8217a77f   2Gi        RWO            Delete           Bound    kubesphere-system/redis-pvc                                        nfs-storage             5d20h
```

要清理数据的pv有下面2个：

```bash
kubesphere-monitoring-system/prometheus-k8s-db-prometheus-k8s-0
kubesphere-monitoring-system/prometheus-k8s-db-prometheus-k8s-1
```

pv的name分别为：

```bash
pvc-a4d1b5f3-5861-4c64-86af-6f7e744bf548
pvc-d9f0ff1c-4357-41ea-bc8d-1030517fc71f
```

查看这两个pv的详情：

```bash
kubectl get pv pvc-a4d1b5f3-5861-4c64-86af-6f7e744bf548 -o yaml
```

```bash
kubectl get pv pvc-d9f0ff1c-4357-41ea-bc8d-1030517fc71f -o yaml
```

输出如下所示：

```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  annotations:
    pv.kubernetes.io/provisioned-by: k8s-sigs.io/nfs-subdir-external-provisioner
  creationTimestamp: "2023-06-21T04:21:08Z"
  finalizers:
  - kubernetes.io/pv-protection
  name: pvc-a4d1b5f3-5861-4c64-86af-6f7e744bf548
  resourceVersion: "5319"
  uid: a17263c8-59e0-44ea-9551-4f6799ce7261
spec:
  accessModes:
  - ReadWriteOnce
  capacity:
    storage: 20Gi
  claimRef:
    apiVersion: v1
    kind: PersistentVolumeClaim
    name: prometheus-k8s-db-prometheus-k8s-0
    namespace: kubesphere-monitoring-system
    resourceVersion: "5306"
    uid: a4d1b5f3-5861-4c64-86af-6f7e744bf548
  nfs:
    path: /nfs/data/kubesphere-monitoring-system-prometheus-k8s-db-prometheus-k8s-0-pvc-a4d1b5f3-5861-4c64-86af-6f7e744bf548
    server: 192.168.0.112
  persistentVolumeReclaimPolicy: Delete
  storageClassName: nfs-storage
  volumeMode: Filesystem
status:
  phase: Bound
```

可以看到挂载路径为：

```bash
nfs:
    path: /nfs/data/kubesphere-monitoring-system-prometheus-k8s-db-prometheus-k8s-0-pvc-a4d1b5f3-5861-4c64-86af-6f7e744bf548
```

位于nfs的服务器，也就是master节点，注意2个pv的挂载信息都要查，可以得到2个pv的挂载路径为：

```bash
/nfs/data/kubesphere-monitoring-system-prometheus-k8s-db-prometheus-k8s-0-pvc-a4d1b5f3-5861-4c64-86af-6f7e744bf548
/nfs/data/kubesphere-monitoring-system-prometheus-k8s-db-prometheus-k8s-1-pvc-d9f0ff1c-4357-41ea-bc8d-1030517fc71f
```

要删除数据，先要停止运行这2个pod，编辑 prometheus 服务信息：

```bash
kubectl edit prometheus -n kubesphere-monitoring-system k8s
```

找到 replicas 属性，将：

```yaml
replicas: 2
```

修改为

```yaml
replicas: 0
```

保存退出即可，这样就将 prometheus 的副本数量变为0，所有的pod都停止运行了，下面删除pv中的数据，在nfs服务器上运行：

```
cd /nfs/data/kubesphere-monitoring-system-prometheus-k8s-db-prometheus-k8s-0-pvc-a4d1b5f3-5861-4c64-86af-6f7e744bf548
rm -rf prometheus-db/

cd /nfs/data/kubesphere-monitoring-system-prometheus-k8s-db-prometheus-k8s-1-pvc-d9f0ff1c-4357-41ea-bc8d-1030517fc71f
rm -rf prometheus-db/
```

删除完成后重新编辑 prometheus 服务信息：

```bash
kubectl edit prometheus -n kubesphere-monitoring-system k8s
```

将 replicas: 0 => 2 ，恢复原值。最后再把所有k8s节点关闭后再重启各个节点，就可以看到 prometheus 服务可以正常启动了。





