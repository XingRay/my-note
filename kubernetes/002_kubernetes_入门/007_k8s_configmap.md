### ConfigMap

挂载目录可以使用pv和pvc，但是挂载配置文件，可以使用ConfigMap，比如redis的配置文件 redis.conf 和mysql的配置文件等。

使用configmap通常分为2步

1 将应用的配置创建为配置集

2 创建pod时应用配置集



下面以启动redis为例：

创建redis配置文件 redis.conf

```bash
vi redis.conf
```

内容如下：

```bash
appendonly yes
```



创建配置集ConfigMap

```
kubectl create configmap <config-map-name> --from-file=<config-file>
```

```bash
kubectl create cm <config-map-name> --from-file=<config-file>
```

根据redis配置文件创建配置集：

```bash
kubectl create cm redis-conf --from-file=redis.conf
```

查看配置集：

```bash
kubectl get configmap
```

或者

```bash
kubectl get cm
```

执行：

```
root@k8s-master01:~# kubectl get configmap
NAME               DATA   AGE
kube-root-ca.crt   1      28h
redis-conf         1      59s
```

创建完成后就可以把 redis.conf 文件删除

```bash
rm redis.conf
```

创建好的配置集这时已经存储到k8s的etcd数据可以中了，k8s中的所有的数据，档案都存储在etcd数据库中。

查看配置集的详细信息：

```bash
kubectl get configmap <config-map-name> -oyaml
```

或者

```
kubectl get cm <config-map-name> -oyaml
```

执行：

```bash
kubectl get cm redis-conf -oyaml
```

以yaml文件的形式查看配置集

```bash
root@k8s-master01:~# kubectl get cm redis-conf -oyaml
```

```yaml
apiVersion: v1
data:
  redis.conf: |
    appendonly yes
kind: ConfigMap
metadata:
  creationTimestamp: "2023-06-20T08:10:27Z"
  name: redis-conf
  namespace: default
  resourceVersion: "98929"
  uid: b209decb-6431-4055-a5be-57c164012e92
```

说明：

```yaml
metadata:
  name: redis-conf
  namespace: default
```

说明了资源（ConfigMap）的名字和所在的命名空间

```yaml
kind: ConfigMap
```

资源的类型为 ConfigMap

```yaml
data:
  redis.conf: |
    appendonly yes
```

配置集的真正的数据在data中。data是key-value形式，key是文件名 redis.conf，value是配置文件的内容。这里相当于把之前的配置文件保存到configmap中了。创建pod，在pod启动的时候挂载 configmap。示例：

```bash
vi redis-pod.yaml
```

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: redis
spec:
  containers:
  - name: redis
    image: redis
    command:
      - redis-server
      - "/redis-master/redis.conf"
    ports:
    - containerPort: 6379
    volumeMounts:
    - mountPath: /data
      name: data
    - mountPath: /redis-master
      name: config
  volumes:
    - name: data
      emptyDir: {}
    - name: config
      configMap:
        name: redis-conf
        items:
        - key: redis.conf
          path: redis.conf
```

说明：

```yaml
     command:
      - redis-server
      - "/redis-master/redis.conf"
```

  指的是redis容器内部的位置，相当于启动容器时运行redis的启动命令：

```bash
redis-server /redis-master/redis.conf
```

参考 docker启动redis， redis使用自定义配置文件启动

```bash
docker run -v /data/redis/redis.conf:/etc/redis/redis.conf \
-v /data/redis/data:/data \
-d --name myredis \
-p 6379:6379 \
redis:latest  redis-server /etc/redis/redis.conf
```



```yaml
volumeMounts:
    - mountPath: /data
      name: data
    - mountPath: /redis-master
      name: config
```

卷挂载，把redis容器内部的 /data 目录挂载出去，挂载名为 data , 同样会将容器内部的 /redis-master 目录挂载出去，挂载名为 config。这2个挂载的具体的挂载方式由下面的配置定义：

```yaml
volumes:
    - name: data
      emptyDir: {}
    - name: config
      configMap:
        name: redis-conf
        items:
        - key: redis.conf
          path: redis.conf
```

名为data的挂载指定为 emptyDir: {}， 意思是由k8s分配一个临时目录即可。

名为config的挂载方式指定为 configMap， 具体为：

```yaml
    - name: config
      configMap:
        name: redis-conf
        items:
        - key: redis.conf
          path: redis.conf
```

这个ConfigMap的名称由配置 name: redis-conf 定义，即为 redis-conf。配置集redis-conf内容如下：

```yaml
apiVersion: v1
data:
  redis.conf: |
    appendonly yes
kind: ConfigMap
metadata:
  creationTimestamp: "2023-06-20T08:10:27Z"
  name: redis-conf
  namespace: default
  resourceVersion: "98929"
  uid: b209decb-6431-4055-a5be-57c164012e92
```

data可能会有很多的项，所以在pod挂载中指定items：

```yaml
        items:
        - key: redis.conf
          path: redis.conf
```

指定key为 redis.conf，也就是在redis-conf的data

```yaml
data:
  redis.conf: |
    appendonly yes
```

中找到 redis.conf 这一项。把这一项的内容取出，取出之后要放在某一个目录下，这个目录就由 `path: redis.conf` 参数决定。而这个路径是要挂载到 `- mountPath: /redis-master`指定的 /redis-master 目录下的。所以系统会在容器内创建 /redis-master/redis.conf 文件，文件的内容就是 名为 redis-conf 的configmap 的data中key为 redis.conf 对应的value，也就是 `appendonly yes`。key和path都是可以自定义，但是要能pod部署文件中的key与configmap的key一致，path与pod中容器的启动参数中定义配置文件的路径一致即可。

无论是redis，还是mysql等任何中间件，使用configmap的步骤都是这2步。



执行：

```bash
kubectl apply -f redis-pod.yaml
```

查看pod

```bash
root@k8s-master01:~# kubectl get pod
NAME                                READY   STATUS    RESTARTS   AGE
redis                               1/1     Running   0          21s
```

可以看到redis已经正常启动了。

此时通过dashboard可以进入redis容器内部，查看目录

```bash
root@redis:/# ls
bin  boot  data  dev  etc  home  lib  lib32  lib64  libx32  media  mnt  opt  proc  redis-master  root  run  sbin  srv  sys  tmp  usr  var
```

可以看到根目录下有一个 redis-master  目录，进入目录继续查看

```bash
root@redis:/# cd redis-master/
root@redis:/redis-master# ls
redis.conf
root@redis:/redis-master# cat redis.conf 
appendonly yes
```

可以看到有一个redis.conf 目录并且内容为指定的配置。另外使用configmap还可以动态修改配置。执行命令修改configmap

```bash
kubectl edit cm redis-conf
```

将：

```yaml
apiVersion: v1
data:
  redis.conf: |
    appendonly yes
kind: ConfigMap
metadata:
  creationTimestamp: "2023-06-20T08:10:27Z"
  name: redis-conf
  namespace: default
  resourceVersion: "98929"
  uid: b209decb-6431-4055-a5be-57c164012e92
```

修改为：

```yaml
apiVersion: v1
data:
  redis.conf: |
    appendonly yes
    requirepass 123456
kind: ConfigMap
metadata:
  creationTimestamp: "2023-06-20T08:10:27Z"
  name: redis-conf
  namespace: default
  resourceVersion: "98929"
  uid: b209decb-6431-4055-a5be-57c164012e92
```

添加配置     requirepass 123456 ，保存退出。等待一段时间（大概1分钟）后，configmap中修改后的配置就会同步到容器内

```bash
root@redis:/redis-master# cat redis.conf 
appendonly yes
requirepass 123456
```

注意pod没有重启，redis不具备配置热更新能力，修改后的redis配置并没有生效，需要重启pod才能使得redis应用最新的配置。如果pod中部署的应用具有热更新功能，可以实时感知配置文件的变化才不需要重启pod。