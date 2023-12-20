## KubeSphere应用部署-mysql

使用具有项目 operator 权限的账号 `dev-zhao` 登录KubeSphere系统，进入指定项目

点击 用用负载-工作负载，工作负载分为三种：

| 工作负载     | 名称        | 说明                                                         |
| ------------ | ----------- | ------------------------------------------------------------ |
| 部署         | Deployment  | 部署的是无状态应用，比如java开发的微服务                     |
| 有状态副本集 | StatefulSet | 部署之后带有数据存储，比如mysql/redis等中间件                |
| 守护进程集   | DaemonSet   | 在各个节点上有且只有一份。比如在每台k8s节点上部署的日志收集器，将日志收集至大数据平台 |



KubeSphere还有其他的应用负载

| 应用负载 | 说明                                           |
| -------- | ---------------------------------------------- |
| 容器组   | 也就是pod，应用部署完成后就会产生容器组        |
| 服务     | service， 暴露服务分为 ClusterIP和NodePort方式 |
| 应用路由 | ingress                                        |
| 任务     | 定时任务                                       |
| 应用     | 一键部署应用，自动部署其他的负载               |



存储管理

| 存储   | 说明                        |
| ------ | --------------------------- |
| 存储卷 | 存储pod的数据，对应k8s的pvc |



配置管理 

配置集 ConfigMap



### 部署应用的3要素

1 应用的部署方式，有状态、无状态 ...

2 应用的数据挂载，数据、配置文件 ...

3 应用的可访问性，负载均衡网络



### 部署mysql

下面以部署mysql为例

https://hub.docker.com/_/mysql

要部署中间件，首先在dockerhub搜索对应的镜像， 在镜像描述中会有镜像的核心特点，如配置文件的位置，数据存储的位置，环境变量等。先分析mysql是如何启动的，所有的启动参数都可以参考官方镜像的文档，如：

```bash
docker run --name some-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:tag
```

这里 `-e MYSQL_ROOT_PASSWORD=my-secret-pw` 是用于指定mysql root账号的密码的。官方文档对自定义配置文件也有示例：

```bash
docker run --name some-mysql -v /my/custom:/etc/mysql/conf.d -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:tag
```

典型的mysql镜像启动指令：

```bash
docker run -p 3306:3306 --name mysql-01 \
-v /mydata/mysql/log:/var/log/mysql \
-v /mydata/mysql/data:/var/lib/mysql \
-v /mydata/mysql/conf:/etc/mysql/conf.d \
-e MYSQL_ROOT_PASSWORD=root \
--restart=always \
-d mysql:8.0.33
```

这里通过

```bash
-v /mydata/mysql/log:/var/log/mysql \
-v /mydata/mysql/data:/var/lib/mysql \
-v /mydata/mysql/conf:/etc/mysql/conf.d \
```

将容器内部的3个目录挂载到了容器的外部。

在k8s中启动mysql，需要有一个mysql的pod，挂载 /var/lib/mysql 目录到pvc， 配置ConfigMap，映射到容器内/etc/mysql/conf.d目录下。key为 xxx.cnf 即可。



#### 创建配置：

使用具有 project-operator权限的账户登录 KubeSphere，点击进入项目。点击 `配置-配置字典` ，点击创建，在弹出的窗口中设置：

名称:  自定义，如 mysql-conf

别名:  用于区分资源，如：project-test-mysql-configmap

描述: 配置文件的说明，如： mysql的配置文件

点击`下一步` 进入`数据设置`，点击 `添加数据`，这里添加数据需要设置key-value，注意在 /etc/mysql/conf 目录下的任何 xxx.cnf 的文件都会被mysql认为配置文件。这里的key就是配置文件的文件名，可以设置为 `my.cnf` , value 就是配置文件的内容，如：

```bash
[client]
# 设置mysql客户端默认字符集
default-character-set=utf8mb4
 
[mysql]
# 设置mysql客户端默认字符集
default-character-set=utf8mb4
 
[mysqld]
# 服务端使用的字符集
init_connect='SET collation_connection = utf8mb4_unicode_ci'
init_connect='SET NAMES utf8mb4'
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

skip-character-set-client-handshake
skip-name-resolve

# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB
# 设置时区
default-time_zone='+8:00'
# 允许最大连接数
max_connections=200
# 允许连接失败的次数。这是为了防止有人从该主机试图攻击数据库系统
max_connect_errors=10

# 服务器标识ID
server-id=13306
#二进制日志文件格式
log-bin=mysql-bin
```

点击 `√` 点击创建。这样在mysql部署之前将配置文件提前创建完成。



#### 创建PVC：

点击 `存储-持久卷声明`，点击创建，输入基本信息：

名称：pvc-mysql

别名：project-test-mysql-data-pvc

描述：mysql持久卷申明

点击下一步进入 `存储设置` ，

创建方式：使用默认设置：通过存储类创建

存储类：使用默认的存储类，如：nfs-storage

访问模式：ReadWriteOnce 单节点读写， ReadOnlyMany 多节点只读 ReadWriteMany 多节点读写，这里mysql是有状态应用，一般使用单节点读写。

卷容量：根据实际需要设置容量大小，这里作为测试选择 2G。

点击下一步：高级设置

添加元数据：

这里跳过，点击创建。这样就创建完成了，在持久卷声明列表中可以看到刚创建的pvc



#### 部署mysql

点击 `应用负载-工作负载`，由于mysql是中间件，属于有状态的应用，所以这里切换到 `有状态副本集`，点击`创建`

##### 基本信息：

```bash
名称：mysql-standalone
别名：project-test-mysql-standalone
描述：测试项目的mysql数据库
```



##### 容器组设置：

容器组副本数量： 1

容器：

​	点击添加容器，设置：

​	镜像：参考dockerhub的镜像描述，这里选择 `mysql:8.0.33` ，输入之后会自动从dockerhub搜索，也可以配置私有镜像仓库。参考：https://www.kubesphere.io/docs/v3.3/project-user-guide/configuration/image-registry/ 。配置之后就可以选择dockerhub或者私有仓库了。

​	点击 `使用默认端口`

​	资源预留、资源限制：预留就是启动的时候占用的资源（cpu，内存），这里作为测试不预留资源，避免资源浪费。仅做资源限制，这里限制为 1 Core 2048 Mi



**配置环境变量**：

在配置界面下找到 `环境变量`

创建保密字典：

点击下面的提示文本的 `创建一个保密字典`，创建一个保密字典，基本信息：

```bash
名称：mysql-root-password
别名：project-test-mysql-root-password
描述：测试项目mysql的root账号的密码
```

点击`下一步`进入数据设置，`类型`选择为：`默认`，添加元数据key-value为：

```bash
key： MYSQL_ROOT_PASSWORD
value：123456
```

点击`创建`，返回容器组设置页。



添加root密码设置：

```bash
数据源选择： `来自保密字典`
key： MYSQL_ROOT_PASSWORD
保密字典：mysql-root-password
资源中的键：MYSQL_ROOT_PASSWORD
```

这样就配置mysql启动需要的环境变量 `MYSQL_ROOT_PASSWORD`，设置root账号密码，这里设置为`123456`，并且密码保存在保密字典中了。



勾选**同步主机时区**，这样mysql容器的时间和主机的时区保持一致。

点击`√`，容器配置完成。其他跳过，点击下一步。进入存储设置



##### 存储设置

使用挂载卷

点击 `挂载卷`，切换到`持久卷`，点击 `选择持久卷申明`，选择 `mysql-pvc` ，在下面的 container 中设置：

```bash
读写方式： 读写
挂载路径： /var/lib/mysql
```

点击 `√`



使用持久卷声明模板

点击 `添加持久卷声明模板`，存储设置：

```bash
PVC 名称前缀： pvc-mysql
存储类型：nfs-storage
访问模式：ReadWriteOnce
卷容量：2Gi
挂载路径：
	读写模式：读写
	挂载路径：/var/lib/mysql
```

点击√



回到存储设置页，点击`挂载配置字典或者保密字典`，切换到 `配置字典`，

点击 `选择配置字典` 选择 `mysql-conf` 

container设置：

```bash
读取方式：只读
挂载路径：/etc/mysql/conf.d
```

这样就会把 ConfigMap 中的key-value的key：my.cnf 作为文件名挂载到 `/etc/mysql/conf.d` 目录下。

点击`√`，点击下一步，进入`高级设置`，高级设置跳过点击 `创建`即可创建容器。



此时在项目的 `应用负载-工作负载-有状态副本集` 页面中就可以看到刚创建的mysql容器。点击容器名可以进入详情页。在容器详情页中可以看到 `资源状态` `修改记录` `元数据` `监控` `环境变量` `事件`等信息。



在资源状态中可以看到容器的列表，点击容器名可以进入容器详情，容器详情中可以点击名字旁边的图标看到容器的日志和进入容器的终端。可以很方便的进入容器，进入容器查看配置文件是否正确挂载：

```bash
cat /etc/mysql/conf.d/my.cnf
```

在`配置-配置字典`中可以修改配置，点击 mysql-conf，点击`编辑YAML`进入编辑页面，根据需要编辑下面的配置文件部分：

```yaml
data:
  my.cnf: |-
    [client]
    # 设置mysql客户端默认字符集
    default-character-set=utf8mb4
     
    [mysql]
    # 设置mysql客户端默认字符集
    default-character-set=utf8mb4
# ...
```

修改之后保存。也可以点击`更多操作-编辑设置`在数据列表中点击右边的编辑按钮直接对配置文件进行编辑。一段时间之后在pod中就后自动同步修改后的最新配置，但是由于mysql不能热更新，因此需要重新创建容器才能应用最新的设置。



点击 应用负载-工作负载， 切换到 有状态副本集 ，点击 mall-cloud-mysql，进入pod详情，点击 `更多操作-重新创建` 即可重新创建pod。



#### 可访问性

到这里mysql的pod就部署好了，但是此时还不能从外部访问，要从外部访问，需要在service中暴露端口。在 `应用负载-服务`中可以看到默认创建的服务，这个服务的访问方式是Headless，点击 mysql服务，进入详情页，可以看到 mysql服务的属性中有  DNS:  mysql-standalone-o4av.project-test 属性，在集群内可以通过 这个dns域名访问mysql容器。在进入 应用负载-工作负载，进入pod详情页，进入容器的终端，执行：

```bash
mysql -uroot -hmysql-standalone-o4av.project-test -p
```

输入设置的root密码123456，可以正常访问mysql：

```bash
sh-4.4# mysql -uroot -hmysql-standalone-o4av.project-test -p
Enter password:
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 8
Server version: 8.0.33 MySQL Community Server - GPL

Copyright (c) 2000, 2023, Oracle and/or its affiliates.

Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.

Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.

mysql>
```

想要在集群外访问，这个自动生成的service可以删除，重新创建一个。注意删除的时候**不要勾选**关联的资源，关联的资源要保留，仅删除服务。



#### 创建可外网访问的服务

在服务列表，点击创建。点击`指定工作负载`，进入基本信息，输入

名称: mysql-service

别名: project-test-mysql-service

描述: mysql服务

点击下一步，进入`服务设置`

内部访问模式：

​	虚拟IP地址：在集群外也能访问

​	内部域名：仅能在集群内访问

选择虚拟IP地址，



点击指定工作负载：

切换到 `有状态副本集`，选择 mysql-standalone



设置端口：

协议： HTTP

名称： HTTP-3306

容器端口: 容器内部的端口，mysql使用的是 3306

服务端口: 服务暴露的端口，这里也用 3306



点击下一步，进入高级设置：

勾选 `外部访问`，访问模式选择 `NodePort`，点击 `创建` ，创建好服务后，服务会暴露一个随机的（30000~32767）端口，如果有防火墙，需要在防火墙开启该端口。在外部通过mysql客户端连接任意k8s节点地址（这里是 192.168.0.112/113/114）+**随机端口**（注意不是 3306），账号：root，密码：123456



#### 创建仅集群内访问的服务

在服务列表，点击创建。点击`指定工作负载`，进入基本信息，输入

名称: mysql-service-in-cluster

别名: project-test-mysql-service-in-cluster

描述: mysql服务，仅集群内访问

点击下一步，进入`服务设置`

内部访问模式：

​	虚拟IP地址：在集群外也能访问

​	内部域名：仅能在集群内访问

选择内部域名。



点击指定工作负载：

切换到 `有状态副本集`，选择 project-test--mysql 



设置端口：

协议： HTTP

名称： HTTP-3306

容器端口: 容器内部的端口，mysql使用的是 3306

服务端口: 服务暴露的端口，这里也用 3306



点击下一步，进入高级设置：

**不要勾选** `外部访问`，直接点击创建。创建服务后，在服务详情页查看服务的dns格式为 `<service-name>-<namespace>`， 如：DNS: mysql-service-in-cluster.project-test-，在集群内的节点可以通过 mysql-service-in-cluster.project-test +端口来进行访问。



其实可以不用单独创建进内部访问的集群，因为使用NodePort方式访问的服务本身可有用于内部访问的dns，内部节点同样可以通过dns+port的方式访问。但是生产环境下，除非要连接生产库拍错，否则外部访问服务是不创建的。mysql只能由部署的应用访问，但是在开发环境下有可以外部直接访问的mysql服务可以方便开发调试。

