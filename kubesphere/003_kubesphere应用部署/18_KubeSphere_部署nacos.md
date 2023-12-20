## KubeSphere部署nacos

在k8s上部署nacos需要依赖mysql，将mysql作为nacos的数据源。

### 1 部署mysql

参考 `myNote\software-project\deploy\kubernetes\14_KubeSphere_应用部署_mysql.md` 文档部署mysql，部署完成后按照nacos的要求创建`nacos`数据库，导入 `nacos-server-2.2.3\nacos\conf\mysql-schema.sql`到nacos数据库即可。



### 2 nacos部署

#### 2.1 单机模式

在`应用负载-服务` ，点击`创建`，点击 `创建服务` 选择 `有状态服务`

```bash
名称：nacos-standalone
别名：project-test-nacos-standalone
版本：v1
描述：测试项目的nacos单节点服务
```

点击`下一步`进入`容器组设置`页

点击 docker镜像， nacos的镜像可以在dockerhub上搜索nacos得到官方镜像页面： https://hub.docker.com/r/nacos/nacos-server  当前最新版本的pull指令：

```bash
docker pull nacos/nacos-server:v2.2.3
```

在KubeSphere中输入 `nacos/nacos-server:v2.2.3`  按enter进行搜索，选择 `nacos/nacos-server:v2.2.3`，点击`使用默认端口`，

设置资源限制：1Core 1024 Mi

添加`环境变量` 

```bash
key： MODE
value： standalone
```

在下面勾选 `同步主机时区`

点击`√`，点击`下一步`

nacos的部署不需要挂载数据，数据存储在mysql中。但是有配置文件，这里先不挂载任何配置，直接使用默认配置部署nacos，

点击`下一步`，点击`创建`

可以看到nacos服务已经创建，名为 `nacos-standalone`

在`应用负载-服务`点击新创建的nacos服务的详情页，可以看到这个nacos服务的dns为：`nacos-standalone.project-test`。

在`应用负载-工作负载`，切换至`有状态副本集`，可以看到 `nacos-standalone-v1` 点击进入详情页

可以看到创建的pod，名为 `nacos-standalone-v1-0` ，可以增加副本的数量，可以看到新增加的pod名为 `nacos-standalone-v1-1`，可以直到pod的命名规则为 `<service-name>-<version>-<pod-number>` ，将副本的数量重新改为1，自动删除新创建的pod，进入 nacos-standalone-v1-0 的bash，执行：

```bash
ping nacos-standalone.project-test
```

输出：

```bash
sh-4.2# ping nacos-standalone.project-test
PING nacos-standalone.project-test.svc.cluster.local (10.244.2.89) 56(84) bytes of data.
64 bytes from nacos-standalone-v1-0.nacos-standalone.project-test.svc.cluster.local (10.244.2.89): icmp_seq=1 ttl=64 time=0.013 ms
64 bytes from nacos-standalone-v1-0.nacos-standalone.project-test.svc.cluster.local (10.244.2.89): icmp_seq=2 ttl=64 time=0.034 ms
64 bytes from nacos-standalone-v1-0.nacos-standalone.project-test.svc.cluster.local (10.244.2.89): icmp_seq=3 ttl=64 time=0.040 ms
64 bytes from nacos-standalone-v1-0.nacos-standalone.project-test.svc.cluster.local (10.244.2.89): icmp_seq=4 ttl=64 time=0.041 ms
64 bytes from nacos-standalone-v1-0.nacos-standalone.project-test.svc.cluster.local (10.244.2.89): icmp_seq=5 ttl=64 time=0.026 ms
64 bytes from nacos-standalone-v1-0.nacos-standalone.project-test.svc.cluster.local (10.244.2.89): icmp_seq=6 ttl=64 time=0.034 ms
64 bytes from nacos-standalone-v1-0.nacos-standalone.project-test.svc.cluster.local (10.244.2.89): icmp_seq=7 ttl=64 time=0.047 ms
^C
--- nacos-standalone.project-test.svc.cluster.local ping statistics ---
7 packets transmitted, 7 received, 0% packet loss, time 6132ms
rtt min/avg/max/mdev = 0.013/0.033/0.047/0.012 ms
```

可以看到是使用域名 `nacos-standalone-v1-0.nacos-standalone.project-test.svc.cluster.local` 进行访问，这个域名指向 pod `nacos-standalone-v1-0`，命名规则是：`<pod-name>.<service-name>.<namespace>.svc.cluster.local`。这个规则可以应用到集群模式的节点配置中。

此时创建好的 `nacos-standalone` 服务是一个headless服务，无法通过外部访问，要外部访问这个nacos需要再创建一个服务。



创建开放服务

`应用负载-服务` 点击`创建`，选择 `指定工作负载` ，

```
名称：nacos-standalone-service
别名：project-test-nacos-standalone-service
描述：测试项目的nacos单节点服务
```

点击`下一步`，内部访问模式选择`虚拟IP地址`，点击 `指定工作负载`，弹窗中切换到 `有状态副本集`，选择 `nacos-standalone-v1`，点击`确定`，设置端口8848，点击`下一步`，勾选`外部访问`，访问模式选择`NodePort`，点击`创建`。此时回到 `应用负载-服务`，可以看到新创建的服务 `nacos-standalone-service`，暴露外部访问端口 `3****`，使用浏览器访问 `http://192.168.0.112:3****/nacos` 即可。



#### 2.2 集群模式

##### 2.2.1 集群地址配置

在k8s上部署nacos采用集群模式，如果是本地集群部署，需要将 nacos-server-2.2.3\nacos\conf 目录下的 cluster.conf.example 文件复制一份，重命名为 cluster.conf ，作为集群节点的配置。默认配置如下：

```properties
#it is ip
#example
192.168.16.101:8847
192.168.16.102
192.168.16.103
```

在k8s中部署，需要将其中的ip地址更改为k8s集群中的 ip/域名+端口，在集群中pod可能会重启，ip地址会变化，所以nacos集群配置中不能使用ip，而k8s中各个pod都有一个固定的可在k8s集群中内部访问的dns，这个dns的命名规则由上面的单机部署中得到：

```bash
<pod-name>.<service-name>.<namespace>.svc.cluster.local
```

其中pod的名称也是自动生成的，所以这个dns的详细规则如下：

```
<service-name>-<version>-<pod-number>.<service-name>.<namespace>.svc.cluster.local
```

其中`namespace`为项目名。这里nacos服务名设置为 `nacos-cluster` ，这样就可以确定在 `cluster.conf` 配置文件中的地址列表，这里使用3个节点：

```bash
nacos-cluster-v1-0.nacos-cluster.project-test.svc.cluster.local:8848
nacos-cluster-v1-1.nacos-cluster.project-test.svc.cluster.local:8848
nacos-cluster-v1-2.nacos-cluster.project-test.svc.cluster.local:8848
```

可以将这个dns列表配置到nacos集群配置中。



##### 2.2.2 配置挂载

配置使用nacos集群，需要挂载到外面的配置有 `nacos-server-2.2.3\nacos\conf` 目录下的主配置文件 `application.properties` 和集群配置文件 `cluster.conf`。其中 `application.properties` 可以配置数据源为mysql，以及安全访问相关的key 。

在nacos-standalone的pod中找到nacos的目录为 `/home/nacos`

```bash
sh-4.2# pwd
/home/nacos
sh-4.2# ls
LICENSE  NOTICE  bin  conf  data  derby.log  file:  logs  start.out  target  work
```

其中配置文件目录为 `/home/nacos/conf`

```bash
sh-4.2# pwd
/home/nacos/conf
sh-4.2# ls
1.4.0-ipv6_support-update.sql  application.properties  mysql-schema.sql
announcement.conf              derby-schema.sql        nacos-logback.xml
```

默认`cluster.conf`不存在或者内容为空，需要手动配置。总之，就是要把nacos的容器内的 `/home/nacos/conf`目录下的`application.properties` 和`cluster.conf`挂载到`ConfigMap`中。

`application.properties`

```properties
#
# Copyright 1999-2021 Alibaba Group Holding Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#*************** Spring Boot Related Configurations ***************#
### Default web context path:
server.servlet.contextPath=/nacos
### Include message field
server.error.include-message=ALWAYS
### Default web server port:
server.port=8848

#*************** Network Related Configurations ***************#
### If prefer hostname over ip for Nacos server addresses in cluster.conf:
# nacos.inetutils.prefer-hostname-over-ip=false

### Specify local server's IP:
# nacos.inetutils.ip-address=


#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
### Deprecated configuration property, it is recommended to use `spring.sql.init.platform` replaced.
# spring.datasource.platform=mysql
spring.sql.init.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://mysql-standalone-o4av.project-test:3306/nacos?characterEncoding=utf8&connectTimeout=3000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.user.0=root
db.password.0=123456

### Connection pool configuration: hikariCP
db.pool.config.connectionTimeout=30000
db.pool.config.validationTimeout=10000
db.pool.config.maximumPoolSize=20
db.pool.config.minimumIdle=2

#*************** Naming Module Related Configurations ***************#

### If enable data warmup. If set to false, the server would accept request without local data preparation:
# nacos.naming.data.warmup=true

### If enable the instance auto expiration, kind like of health check of instance:
# nacos.naming.expireInstance=true

### Add in 2.0.0
### The interval to clean empty service, unit: milliseconds.
# nacos.naming.clean.empty-service.interval=60000

### The expired time to clean empty service, unit: milliseconds.
# nacos.naming.clean.empty-service.expired-time=60000

### The interval to clean expired metadata, unit: milliseconds.
# nacos.naming.clean.expired-metadata.interval=5000

### The expired time to clean metadata, unit: milliseconds.
# nacos.naming.clean.expired-metadata.expired-time=60000

### The delay time before push task to execute from service changed, unit: milliseconds.
# nacos.naming.push.pushTaskDelay=500

### The timeout for push task execute, unit: milliseconds.
# nacos.naming.push.pushTaskTimeout=5000

### The delay time for retrying failed push task, unit: milliseconds.
# nacos.naming.push.pushTaskRetryDelay=1000

### Since 2.0.3
### The expired time for inactive client, unit: milliseconds.
# nacos.naming.client.expired.time=180000

#*************** CMDB Module Related Configurations ***************#
### The interval to dump external CMDB in seconds:
# nacos.cmdb.dumpTaskInterval=3600

### The interval of polling data change event in seconds:
# nacos.cmdb.eventTaskInterval=10

### The interval of loading labels in seconds:
# nacos.cmdb.labelTaskInterval=300

### If turn on data loading task:
# nacos.cmdb.loadDataAtStart=false


#*************** Metrics Related Configurations ***************#
### Metrics for prometheus
#management.endpoints.web.exposure.include=*

### Metrics for elastic search
management.metrics.export.elastic.enabled=false
#management.metrics.export.elastic.host=http://localhost:9200

### Metrics for influx
management.metrics.export.influx.enabled=false
#management.metrics.export.influx.db=springboot
#management.metrics.export.influx.uri=http://localhost:8086
#management.metrics.export.influx.auto-create-db=true
#management.metrics.export.influx.consistency=one
#management.metrics.export.influx.compressed=true

#*************** Access Log Related Configurations ***************#
### If turn on the access log:
server.tomcat.accesslog.enabled=true

### The access log pattern:
server.tomcat.accesslog.pattern=%h %l %u %t "%r" %s %b %D %{User-Agent}i %{Request-Source}i

### The directory of access log:
server.tomcat.basedir=file:.

#*************** Access Control Related Configurations ***************#
### If enable spring security, this option is deprecated in 1.2.0:
#spring.security.enabled=false

### The ignore urls of auth
nacos.security.ignore.urls=/,/error,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.ico,/console-ui/public/**,/v1/auth/**,/v1/console/health/**,/actuator/**,/v1/console/server/**

### The auth system to use, currently only 'nacos' and 'ldap' is supported:
nacos.core.auth.system.type=nacos

### If turn on auth system:
nacos.core.auth.enabled=true

### Turn on/off caching of auth information. By turning on this switch, the update of auth information would have a 15 seconds delay.
nacos.core.auth.caching.enabled=true

### Since 1.4.1, Turn on/off white auth for user-agent: nacos-server, only for upgrade from old version.
nacos.core.auth.enable.userAgentAuthWhite=false

### Since 1.4.1, worked when nacos.core.auth.enabled=true and nacos.core.auth.enable.userAgentAuthWhite=false.
### The two properties is the white list for auth and used by identity the request from other server.
nacos.core.auth.server.identity.key=nacos-core-auth-server-identity-key
nacos.core.auth.server.identity.value=nacos-core-auth-server-identity-value

### worked when nacos.core.auth.system.type=nacos
### The token expiration in seconds:
nacos.core.auth.plugin.nacos.token.cache.enable=false
nacos.core.auth.plugin.nacos.token.expire.seconds=18000
### The default token (Base64 String):
nacos.core.auth.plugin.nacos.token.secret.key=bmFjb3MuY29yZS5hdXRoLnBsdWdpbi5uYWNvcy50b2tlbi5zZWNyZXQua2V5

### worked when nacos.core.auth.system.type=ldap，{0} is Placeholder,replace login username
#nacos.core.auth.ldap.url=ldap://localhost:389
#nacos.core.auth.ldap.basedc=dc=example,dc=org
#nacos.core.auth.ldap.userDn=cn=admin,${nacos.core.auth.ldap.basedc}
#nacos.core.auth.ldap.password=admin
#nacos.core.auth.ldap.userdn=cn={0},dc=example,dc=org
#nacos.core.auth.ldap.filter.prefix=uid
#nacos.core.auth.ldap.case.sensitive=true


#*************** Istio Related Configurations ***************#
### If turn on the MCP server:
nacos.istio.mcp.server.enabled=false

#*************** Core Related Configurations ***************#

### set the WorkerID manually
# nacos.core.snowflake.worker-id=

### Member-MetaData
# nacos.core.member.meta.site=
# nacos.core.member.meta.adweight=
# nacos.core.member.meta.weight=

### MemberLookup
### Addressing pattern category, If set, the priority is highest
# nacos.core.member.lookup.type=[file,address-server]
## Set the cluster list with a configuration file or command-line argument
# nacos.member.list=192.168.16.101:8847?raft_port=8807,192.168.16.101?raft_port=8808,192.168.16.101:8849?raft_port=8809
## for AddressServerMemberLookup
# Maximum number of retries to query the address server upon initialization
# nacos.core.address-server.retry=5
## Server domain name address of [address-server] mode
# address.server.domain=jmenv.tbsite.net
## Server port of [address-server] mode
# address.server.port=8080
## Request address of [address-server] mode
# address.server.url=/nacos/serverlist

#*************** JRaft Related Configurations ***************#

### Sets the Raft cluster election timeout, default value is 5 second
# nacos.core.protocol.raft.data.election_timeout_ms=5000
### Sets the amount of time the Raft snapshot will execute periodically, default is 30 minute
# nacos.core.protocol.raft.data.snapshot_interval_secs=30
### raft internal worker threads
# nacos.core.protocol.raft.data.core_thread_num=8
### Number of threads required for raft business request processing
# nacos.core.protocol.raft.data.cli_service_thread_num=4
### raft linear read strategy. Safe linear reads are used by default, that is, the Leader tenure is confirmed by heartbeat
# nacos.core.protocol.raft.data.read_index_type=ReadOnlySafe
### rpc request timeout, default 5 seconds
# nacos.core.protocol.raft.data.rpc_request_timeout_ms=5000

#*************** Distro Related Configurations ***************#

### Distro data sync delay time, when sync task delayed, task will be merged for same data key. Default 1 second.
# nacos.core.protocol.distro.data.sync.delayMs=1000

### Distro data sync timeout for one sync data, default 3 seconds.
# nacos.core.protocol.distro.data.sync.timeoutMs=3000

### Distro data sync retry delay time when sync data failed or timeout, same behavior with delayMs, default 3 seconds.
# nacos.core.protocol.distro.data.sync.retryDelayMs=3000

### Distro data verify interval time, verify synced data whether expired for a interval. Default 5 seconds.
# nacos.core.protocol.distro.data.verify.intervalMs=5000

### Distro data verify timeout for one verify, default 3 seconds.
# nacos.core.protocol.distro.data.verify.timeoutMs=3000

### Distro data load retry delay when load snapshot data failed, default 30 seconds.
# nacos.core.protocol.distro.data.load.retryDelayMs=30000

### enable to support prometheus service discovery
#nacos.prometheus.metrics.enabled=true

### Since 2.3
#*************** Grpc Configurations ***************#

## sdk grpc(between nacos server and client) configuration
## Sets the maximum message size allowed to be received on the server.
#nacos.remote.server.grpc.sdk.max-inbound-message-size=10485760

## Sets the time(milliseconds) without read activity before sending a keepalive ping. The typical default is two hours.
#nacos.remote.server.grpc.sdk.keep-alive-time=7200000

## Sets a time(milliseconds) waiting for read activity after sending a keepalive ping. Defaults to 20 seconds.
#nacos.remote.server.grpc.sdk.keep-alive-timeout=20000


## Sets a time(milliseconds) that specify the most aggressive keep-alive time clients are permitted to configure. The typical default is 5 minutes
#nacos.remote.server.grpc.sdk.permit-keep-alive-time=300000

## cluster grpc(inside the nacos server) configuration
#nacos.remote.server.grpc.cluster.max-inbound-message-size=10485760

## Sets the time(milliseconds) without read activity before sending a keepalive ping. The typical default is two hours.
#nacos.remote.server.grpc.cluster.keep-alive-time=7200000

## Sets a time(milliseconds) waiting for read activity after sending a keepalive ping. Defaults to 20 seconds.
#nacos.remote.server.grpc.cluster.keep-alive-timeout=20000

## Sets a time(milliseconds) that specify the most aggressive keep-alive time clients are permitted to configure. The typical default is 5 minutes
#nacos.remote.server.grpc.cluster.permit-keep-alive-time=300000
```

注意mysql配置需要根据项目的实际情况修改，这里使用KubeSphere中部署的mysql服务，dns为：`mysql-standalone-o4av.project-test`

```properties
#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
### Deprecated configuration property, it is recommended to use `spring.sql.init.platform` replaced.
# spring.datasource.platform=mysql
spring.sql.init.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://mysql-standalone-o4av.project-test:3306/nacos?characterEncoding=utf8&connectTimeout=3000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.user.0=root
db.password.0=123456
```

注意：

```bash
1 mysql的域名要使用实际项目中的headless服务的dns,开放了NodePort的mysql服务 1 无法使用dns访问，原因？？？ 2 开放的服务正式上线会被关闭
2 serverTimezone=Asia/Shanghai 这里要根据实际情况修改，默认配置里时区是UTC
3 确保数据库中确实根据nacos提供的sql脚本创建nacos数据库
```

安全认证相关的配置也需要根据实际情况修改

```properties
### Since 1.4.1, worked when nacos.core.auth.enabled=true and nacos.core.auth.enable.userAgentAuthWhite=false.
### The two properties is the white list for auth and used by identity the request from other server.
nacos.core.auth.server.identity.key=nacos-core-auth-server-identity-key
nacos.core.auth.server.identity.value=nacos-core-auth-server-identity-value

### worked when nacos.core.auth.system.type=nacos
### The token expiration in seconds:
nacos.core.auth.plugin.nacos.token.cache.enable=false
nacos.core.auth.plugin.nacos.token.expire.seconds=18000
### The default token (Base64 String):
nacos.core.auth.plugin.nacos.token.secret.key=bmFjb3MuY29yZS5hdXRoLnBsdWdpbi5uYWNvcy50b2tlbi5zZWNyZXQua2V5
```



`cluster.conf`

```properties
nacos-cluster-v1-0.nacos-cluster.project-test.svc.cluster.local:8848
nacos-cluster-v1-1.nacos-cluster.project-test.svc.cluster.local:8848
nacos-cluster-v1-2.nacos-cluster.project-test.svc.cluster.local:8848
```



在`配置-配置字典`中点击创建，基本信息：

```bash
名称：nacos-cluster-config
别名：project-test-nacos-cluster-config
描述：测试项目的nacos集群配置
```

点击`下一步`进入`数据设置`，点击`添加数据`，

键：application.properties 值：application.properties文件的内容。

键：cluster.conf	值：cluster.conf文件的内容。



##### 2.2.3 创建服务

使用KuebSphere创建`工作负载-有状态副本集`的方式会连带自动创建一个service，但是这个service名字是随机的，这里需要使用固定的命名规则，所以不使用这种方式创建。这里使用手动创建service的方式固定service的名称。

在`应用负载-服务` ，点击`创建`，点击 `创建服务` 选择 `有状态服务`

```bash
名称：nacos-cluster
别名：project-test-nacos-cluster
版本：v1
描述：测试项目的nacos集群
```

点击`下一步`进入`容器组设置`页

点击`添加容器`，选择容器镜像， 这里nacos版本选择`nacos/nacos-server:v2.2.3`  按enter进行搜索，选择 `nacos/nacos-server:v2.2.3`，点击`使用默认端口`

设置资源限制：1Core 1024 Mi

添加`环境变量` 

```bash
key： MODE
value： cluster
```

在下面勾选 `同步主机时区`点击`√`，点击`下一步`，进入`存储设置`。

nacos的部署不需要挂载数据，数据存储在mysql中。但是有配置文件，需要挂载上面准备的 `application.properties` 和 `cluster.conf` ，挂载的目录为 `/home/nacos/conf` 。考虑到容器中的配置目录还有其他的配置文件，不能直接覆盖，这里要使用子路径的方式进行配置的挂载。



挂载 application.properties

点击 `挂载配置字典或保密字典`，切换到 `配置字典`，点击`选择配置字典`，选择 `nacos-cluster-config`，挂载方式选择 `只读`，挂载路径 `/home/nacos/conf/application.properties`,点击挂载路径右边的`指定子路径`按钮，设置子路径为 `application.properties`，点击确定，勾选`选择特定键`，`键`选择为 `application.properties`，`路径`设置为 `application.properties`，点击`√`



挂载 cluster.conf

点击 `挂载配置字典或保密字典`，切换到 `配置字典`，点击`选择配置字典`，选择 `nacos-cluster-config`，挂载方式选择 `只读`，挂载路径 `/home/nacos/conf/cluster.conf`,点击挂载路径右边的`指定子路径`按钮，设置子路径为 `cluster.conf`，点击确定，勾选`选择特定键`，`键`选择为 `cluster.conf`，`路径`设置为 `cluster.conf`，点击`√`



点击`下一步`，点击`创建`。

如果出现问题如下：

```bash
Caused by: com.alibaba.nacos.api.exception.NacosException: java.io.IOException: User limit of inotify instances reached or too many open files
```

解决办法：在k8s的各个节点宿主机上修改 

```bash
vi /etc/sysctl.conf
```

增加三项：

```bash
fs.inotify.max_queued_events = 32768
fs.inotify.max_user_instances = 65536
fs.inotify.max_user_watches = 1048576
```

使配置立即生效：

```bash
sysctl -p 
```

生效解决



##### 2.2.4 创建开放服务

`应用负载-服务` 点击`创建`，选择 `指定工作负载` ，

```
名称：nacos-cluster-service
别名：project-test-nacos-cluster-service
描述：测试项目的nacos集群服务
```

点击`下一步`，内部访问模式选择`虚拟IP地址`，点击 `指定工作负载`，弹窗中切换到 `有状态副本集`，选择 `nacos-cluster-v1`，点击`确定`，设置端口8848，点击`下一步`，勾选`外部访问`，访问模式选择`NodePort`，点击`创建`。此时回到 `应用负载-服务`，可以看到新创建的服务 `nacos-standalone-service`，暴露外部访问端口 `3****`，使用浏览器访问 `http://192.168.0.112:3****/nacos` 即可。使用nacos/nacos登录后可以在 `集群管理-节点列表` 中看到集群的节点信息。



官方提供的配置属性，可以设置为环境变量：

| name                                    | description                                                  | option                                                       |
| --------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| MODE                                    | cluster/standalone                                           | cluster/standalone default **cluster**                       |
| NACOS_SERVERS                           | nacos cluster address                                        | eg. ip1:port1 ip2:port2 ip3:port3                            |
| PREFER_HOST_MODE                        | Whether hostname are supported                               | hostname/ip default **ip**                                   |
| NACOS_APPLICATION_PORT                  | nacos server port                                            | default **8848**                                             |
| NACOS_SERVER_IP                         | custom nacos server ip when network was mutil-network        |                                                              |
| SPRING_DATASOURCE_PLATFORM              | standalone support mysql                                     | mysql / empty default empty                                  |
| MYSQL_SERVICE_HOST                      | mysql host                                                   |                                                              |
| MYSQL_SERVICE_PORT                      | mysql database port                                          | default : **3306**                                           |
| MYSQL_SERVICE_DB_NAME                   | mysql database name                                          |                                                              |
| MYSQL_SERVICE_USER                      | username of database                                         |                                                              |
| MYSQL_SERVICE_PASSWORD                  | password of database                                         |                                                              |
| MYSQL_DATABASE_NUM                      | It indicates the number of database                          | default :**1**                                               |
| MYSQL_SERVICE_DB_PARAM                  | Database url parameter                                       | default : **characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false** |
| JVM_XMS                                 | -Xms                                                         | default :1g                                                  |
| JVM_XMX                                 | -Xmx                                                         | default :1g                                                  |
| JVM_XMN                                 | -Xmn                                                         | default :512m                                                |
| JVM_MS                                  | -XX:MetaspaceSize                                            | default :128m                                                |
| JVM_MMS                                 | -XX:MaxMetaspaceSize                                         | default :320m                                                |
| NACOS_DEBUG                             | enable remote debug                                          | y/n default :n                                               |
| TOMCAT_ACCESSLOG_ENABLED                | server.tomcat.accesslog.enabled                              | default :false                                               |
| NACOS_AUTH_SYSTEM_TYPE                  | The auth system to use, currently only 'nacos' is supported  | default :nacos                                               |
| NACOS_AUTH_ENABLE                       | If turn on auth system                                       | default :false                                               |
| NACOS_AUTH_TOKEN_EXPIRE_SECONDS         | The token expiration in seconds                              | default :18000                                               |
| NACOS_AUTH_TOKEN                        | The default token                                            | default :SecretKey012345678901234567890123456789012345678901234567890123456789 |
| NACOS_AUTH_CACHE_ENABLE                 | Turn on/off caching of auth information. By turning on this switch, the update of auth information would have a 15 seconds delay. | default : false                                              |
| MEMBER_LIST                             | Set the cluster list with a configuration file or command-line argument | eg:192.168.16.101:8847?raft_port=8807,192.168.16.101?raft_port=8808,192.168.16.101:8849?raft_port=8809 |
| EMBEDDED_STORAGE                        | Use embedded storage in cluster mode without mysql           | `embedded` default : none                                    |
| NACOS_AUTH_CACHE_ENABLE                 | nacos.core.auth.caching.enabled                              | default : false                                              |
| NACOS_AUTH_USER_AGENT_AUTH_WHITE_ENABLE | nacos.core.auth.enable.userAgentAuthWhite                    | default : false                                              |
| NACOS_AUTH_IDENTITY_KEY                 | nacos.core.auth.server.identity.key                          | default : serverIdentity                                     |
| NACOS_AUTH_IDENTITY_VALUE               | nacos.core.auth.server.identity.value                        | default : security                                           |
| NACOS_SECURITY_IGNORE_URLS              | nacos.security.ignore.urls                                   | default : `/,/error,/**/*.css,/**/*.js,/**/*.html,/**/*.map,/**/*.svg,/**/*.png,/**/*.ico,/console-fe/public/**,/v1/auth/**,/v1/console/health/**,/actuator/**,/v1/console/server/**` |



参考：

Nacos Docker

https://github.com/nacos-group/nacos-docker



Nacos

https://nacos.io/zh-cn/index.html



Nacos Docker 快速开始

https://nacos.io/zh-cn/docs/v2/quickstart/quick-start-docker.html



K8s---【KubeSphere部署nacos单机模式和集群模式】

https://blog.51cto.com/u_15670038/5889021