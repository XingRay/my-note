## KubeSphere应用部署-redis



### 查找redis镜像

https://hub.docker.com/_/redis

首先在dockerhub搜索redis镜像， 在镜像描述中会有镜像的核心特点，如配置文件的位置，数据存储的位置，环境变量等。参考官方镜像的文档：

```bash
docker run -v /myredis/conf:/usr/local/etc/redis --name myredis redis redis-server /usr/local/etc/redis/redis.conf
```

#### start with persistent storage

```console
$ docker run --name some-redis -d redis redis-server --save 60 1 --loglevel warning
```

There are several different persistence strategies to choose from. This one will save a snapshot of the DB every 60 seconds if at least 1 write operation was performed (it will also lead to more logs, so the `loglevel` option may be desirable). If persistence is enabled, data is stored in the `VOLUME /data`, which can be used with `--volumes-from some-volume-container` or `-v /docker/host/dir:/data` (see [docs.docker volumes](https://docs.docker.com/engine/tutorials/dockervolumes/)).



从官方文档提到的`-v /docker/host/dir:/data` 中可以知道redis的数据存储目录为 `/data` ，典型的redis镜像启动指令：

```bash
docker run -d -p 6379:6379 --restart=always \
-v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf \
-v /mydata/redis-01/data:/data \
--name redis-01 redis:6.2.5 \
redis-server /etc/redis/redis.conf
```

注意：在容器内部配置文件由启动参数指定，即 `redis-server /etc/redis/redis.conf` 指定了容器内部配置文件为 `/etc/redis/redis.conf`，那么在设置卷挂载的时候也要将同样的目录挂载到外部，`-v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf`。这里文件挂载有：

```bash
-v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf \
-v /mydata/redis-01/data:/data \
```

将容器内部的2个目录挂载到了容器的外部。挂载 /data 目录到pvc， 配置ConfigMap，映射到容器内/etc/redis/目录下。key为 redis.conf



#### 创建配置：

创建redis配置文件，基于官方配置文件修改：

```bash
# Save the DB to disk.
# save <seconds> <changes> [<seconds> <changes> ...]
#
# Redis will save the DB if the given number of seconds elapsed and it
# surpassed the given number of write operations against the DB.
#
# Snapshotting can be completely disabled with a single empty string argument
# as in following example:
#
# save ""
#
# Unless specified otherwise, by default Redis will save the DB:
#   * After 3600 seconds (an hour) if at least 1 change was performed
#   * After 300 seconds (5 minutes) if at least 100 changes were performed
#   * After 60 seconds if at least 10000 changes were performed
#
# You can set these explicitly by uncommenting the following line.
#
# save 3600 1 300 100 60 10000

save ""
port 6379 
requirepass 123456 
maxmemory 256mb
appendonly yes
maxmemory-policy allkeys-lru

# Examples:
#
# bind 192.168.1.100 10.0.0.1     # listens on two specific IPv4 addresses
# bind 127.0.0.1 ::1              # listens on loopback IPv4 and IPv6
# bind * -::*                     # like the default, all available interfaces
#
# ~~~ WARNING ~~~ If the computer running Redis is directly exposed to the
# internet, binding to all the interfaces is dangerous and will expose the
# instance to everybody on the internet. So by default we uncomment the
# following bind directive, that will force Redis to listen only on the
# IPv4 and IPv6 (if available) loopback interface addresses (this means Redis
# will only be able to accept client connections from the same host that it is
# running on).
#
# IF YOU ARE SURE YOU WANT YOUR INSTANCE TO LISTEN TO ALL THE INTERFACES
# COMMENT OUT THE FOLLOWING LINE.
#
# You will also need to set a password unless you explicitly disable protected
# mode.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#bind 127.0.0.1

# Redis supports recording timestamp annotations in the AOF to support restoring
# the data from a specific point-in-time. However, using this capability changes
# the AOF format in a way that may not be compatible with existing AOF parsers.
#aof-timestamp-enabled no

# Since version 5 of RDB a CRC64 checksum is placed at the end of the file.
# This makes the format more resistant to corruption but there is a performance
# hit to pay (around 10%) when saving and loading RDB files, so you can disable it
# for maximum performances.
#
# RDB files created with checksum disabled have a checksum of zero that will
# tell the loading code to skip the check.

rdbchecksum no

# By default protected mode is enabled. You should disable it only if
# you are sure you want clients from other hosts to connect to Redis
# even if no authentication is configured.

protected-mode no
```

点击 `配置-配置字典` ，点击创建，在弹出的窗口中设置：

```bash
名称: redis-conf
别名: project-test-redis-configmap
描述: 测试项目redis配置文件
```

点击`下一步` 进入`数据设置`，点击 `添加数据`，这里添加数据需要设置key-value，注意redis的配置文件为 `/etc/redis/redis.conf` ，这里的key就是配置文件的文件名，设置为 `redis.conf` , value 就是上面配置文件的内容，点击 `√` 点击创建。这样在redis部署之前将配置文件提前创建完成。



#### 部署redis

点击 `应用负载-工作负载`，由于redis是中间件，属于有状态的应用，所以这里切换到 `有状态副本集`，点击`创建`

##### 基本信息：

```bash
名称：redis-standalone
别名：project-test-redis-standalone
描述：测试项目的redis单节点服务
```



##### 容器组设置：

容器组副本数量： 1

容器：

​	点击添加容器，设置：

​	镜像：参考dockerhub的镜像描述，这里选择最新版本，直接输入 redis ，输入之后会自动从dockerhub搜索

​	点击 `使用默认端口`

​	资源预留、资源限制：预留就是启动的时候占用的资源（cpu，内存），这里作为测试不预留资源，避免资源浪费。仅做资源限制，这里限制为 1Core 1024Mi



**配置启动命令**：在配置界面下找到 `启动命令`，添加启动命令和参数，

```bash
命令：redis-server
参数：/etc/redis/redis.conf
```

勾选**同步主机时区**，点击`√`，点击下一步。



##### 存储设置

点击 `添加持久卷声明模板`。输入：

```bash
PVC名称前缀： pvc-redis
存储类：nfs-storage
访问模式：ReadWriteOnce
卷容量： 2G
container：
	数据挂载：读写
	挂载路径: /data
```

这里挂载路径有redis镜像决定，redis容器内的数据挂载目录为 /data 这里也要填写 /data



#### 挂载配置文件

回到存储设置页，点击`挂载配置字典或者保密字典`，切换到`配置字典`，选择配置字典为 `redis-conf` ，选择读写方式为 `只读`， 设置挂载路径为：`/etc/redis`, 这样就会 把 ConfigMap 中的key-value的key作为文件名挂载到 `/etc/redis` 目录下。注意：挂载路径+ConfigMap的key 才是容器内配置文件的完整路径  `/etc/redis/redis.conf`  。

点击`√`，点击下一步，进入`高级设置`，高级设置跳过点击 `创建`即可创建容器。



此时在项目的 `应用负载-工作负载-有状态副本集` 页面中就可以看到刚创建的redis容器。点击容器名可以进入详情页。在容器详情页中可以看到

资源状态 修改记录 元数据 监控 环境变量 事件

在资源状态中可以看到容器的列表，点击容器名可以进入容器详情，容器详情中可以点击名字旁边的图标看到容器的日志和进入容器的终端。可以很方便的进入容器，进入容器查看配置文件是否正确挂载：

```bash
cat /etc/redis/redis.conf
```

```bash
# cat /etc/redis/redis.conf
# Save the DB to disk.
# save <seconds> <changes> [<seconds> <changes> ...]
...
```



在`配置-配置字典`中可以修改配置，点击 redis-conf，点击`编辑YAML`进入编辑页面，根据需要编辑下面的配置文件部分：

```yaml
kind: ConfigMap
apiVersion: v1
metadata:
  name: redis-conf
  namespace: mall-cloud
  annotations:
    kubesphere.io/alias-name: mall-cloud-redis-configmap
    kubesphere.io/creator: dev-zhao
    kubesphere.io/description: 商城项目redis配置文件
data:
  redis.conf: >-
    # Save the DB to disk.

    # save <seconds> <changes> [<seconds> <changes> ...]
# ...
```

修改之后保存，一段时间之后在pod中就后自动同步修改后的最新配置，但是由于redis不能热更新，因此需要重新创建容器才能应用最新的设置。

点击 `应用负载-工作负载`， 切换到 有状态副本集 ，点击 roject-test-redis，进入pod详情，点击 `更多操作-重新创建` 即可重新创建pod。



#### 可访问性

到这里redis的pod就部署好了，但是此时还不能从外部访问，要从外部访问，需要在service中暴露端口，将自动生成的service删除，重新创建service。注意删除的时候**不要勾选**关联的资源，关联的资源要保留，仅删除服务。



#### 创建可外网访问的服务

在服务列表，点击创建。点击`指定工作负载`，进入基本信息，输入

名称: redis-service

别名: project-test-redis-service

描述: 测试项目的redis服务

点击下一步，进入`服务设置`

内部访问模式：

​	虚拟IP地址：在集群外也能访问

​	内部域名：仅能在集群内访问

选择虚拟IP地址，



点击指定工作负载：

切换到 `有状态副本集`，选择 redis-standalone



设置端口：

协议： HTTP

名称： HTTP-6379

容器端口: 容器内部的端口，redis使用的是 6379

服务端口: 服务暴露的端口，这里也用 6379



点击下一步，进入高级设置：

勾选 `外部访问`，访问模式选择 `NodePort`，点击 `创建` ，创建好服务后，服务会暴露一个随机的（30000~32767）端口，如果有防火墙，需要在防火墙开启该端口。在外部通过mysql客户端连接任意k8s节点地址（这里是 192.168.0.112/113/114）+**随机端口**（注意不是 6379），密码是123456



#### 创建仅集群内访问的服务

在服务列表，点击创建。点击`指定工作负载`，进入基本信息，输入

名称: redis-service-in-cluster

别名: project-test-redis-service-in-cluster

描述: 电商项目redis服务，仅集群内访问

点击下一步，进入`服务设置`

内部访问模式：

​	虚拟IP地址：在集群外也能访问

​	内部域名：仅能在集群内访问

选择内部域名。



点击指定工作负载：

切换到 `有状态副本集`，选择 redis-standalone



设置端口：

协议： HTTP

名称： HTTP-6379

容器端口: 容器内部的端口，mysql使用的是 6379

服务端口: 服务暴露的端口，这里也用 6379



点击下一步，进入高级设置：

**不要勾选** `外部访问`，直接点击创建。创建服务后，在服务详情页查看服务的dns格式为 `<service-name>-<namespace>`， 如：DNS: redis-service-in-cluster.project-test，在集群内的节点可以通过 mysql-service-in-cluster.project-test+端口来进行访问。



注意：在部署有状态副本集时，应该在创建部署时点击 `添加持久卷声明模板`方式使用pvc，原因时比如要部署多个 mysql / redis 实例，每一个实例都应该使用独立的持久化卷。mysql之间可以自己做数据同步、分库分表。使用 添加持久卷申明模板 有一个优势，就是在实例伸缩时，比如由 1个实例扩容到3个实例时，会自动创建3个持久化卷，每一个redis都使用独立的持久化卷。但是上一篇创建的mysql进行扩容时，比如扩容到2个实例，这2个实例使用的数据是同一份。这样是会有问题的。

**有状态应用创建时，一定要使用 持久卷声明模板 ，而不是手动指定持久化卷申明**





