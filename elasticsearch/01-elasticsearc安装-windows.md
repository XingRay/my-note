#  在windows中安装elasticsearch 

### 1.下载

https://www.elastic.co/cn/downloads/elasticsearch

### 2. 配置

#### 2.1 设置data和log路径

编辑 `\config\elasticsearch.yml`  修改如下：

```yaml
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
#
path.data: D:\develop\elastic\elasticsearch\data
#
# Path to log files:
#
path.logs: D:\develop\elastic\elasticsearch\log
```



#### 2.2. 设置启动的java虚拟机的堆内存

在 `\config\jvm.options.d` 目录下任意创建一个文件，后缀为.options, 如 `jvm-heap.options`，内容如下：

```properties
-Xms1g
-Xmx1g
```

上述配置为内存1G，注意最大值和最小值要一致，可以根据实际需求设置。

#### 2.3 elasticsearch 设置

```yaml
# ======================== Elasticsearch Configuration =========================
#
# NOTE: Elasticsearch comes with reasonable defaults for most settings.
#       Before you set out to tweak and tune the configuration, make sure you
#       understand what are you trying to accomplish and the consequences.
#
# The primary way of configuring a node is via this file. This template lists
# the most important settings you may want to configure for a production cluster.
#
# Please consult the documentation for further information on configuration options:
# https://www.elastic.co/guide/en/elasticsearch/reference/index.html
#
# ---------------------------------- Cluster -----------------------------------
#
# Use a descriptive name for your cluster:
#
#cluster.name: my-application
#
# ------------------------------------ Node ------------------------------------
#
# Use a descriptive name for the node:
#
#node.name: node-1
#
# Add custom attributes to the node:
#
#node.attr.rack: r1
#
# ----------------------------------- Paths ------------------------------------
#
# Path to directory where to store the data (separate multiple locations by comma):
#
path.data: D:\develop\elastic\elasticsearch\data
#
# Path to log files:
#
path.logs: D:\develop\elastic\elasticsearch\log
#
# ----------------------------------- Memory -----------------------------------
#
# Lock the memory on startup:
#
#bootstrap.memory_lock: true
#
# Make sure that the heap size is set to about half the memory available
# on the system and that the owner of the process is allowed to use this
# limit.
#
# Elasticsearch performs poorly when the system is swapping the memory.
#
# ---------------------------------- Network -----------------------------------
#
# By default Elasticsearch is only accessible on localhost. Set a different
# address here to expose this node on the network:
#
#network.host: 192.168.0.1
#
# By default Elasticsearch listens for HTTP traffic on the first free port it
# finds starting at 9200. Set a specific HTTP port here:
#
#http.port: 9200
#
# For more information, consult the network module documentation.
#
# --------------------------------- Discovery ----------------------------------
#
# Pass an initial list of hosts to perform discovery when this node is started:
# The default list of hosts is ["127.0.0.1", "[::1]"]
#
#discovery.seed_hosts: ["host1", "host2"]
#
# Bootstrap the cluster using an initial set of master-eligible nodes:
#
#cluster.initial_master_nodes: ["node-1", "node-2"]
#
# For more information, consult the discovery and cluster formation module documentation.
discovery.type: single-node
#
# ---------------------------------- Various -----------------------------------
#
# Allow wildcard deletion of indices:
#
#action.destructive_requires_name: false
```



### 3 初始化

```bash
bin/elasticsearch-service.bat
```



### 4 安装为Windows服务

进入bin目录下执行: elasticsearch-service.bat install

![img](https://img-blog.csdnimg.cn/img_convert/1c60e65e8620ce77dc18d5a0efae36c6.png)



3.查看电脑服务es已经存在了

![img](https://img-blog.csdnimg.cn/img_convert/fae684b0321a710f73d39e1df8b46761.png)

```
elasticsearch-service.bat后面还可以执行这些命令
install: 安装Elasticsearch服务
remove: 删除已安装的Elasticsearch服务（如果启动则停止服务）
start: 启动Elasticsearch服务（如果已安装）
stop: 停止服务（如果启动）
manager:启动GUI来管理已安装的服务
```

可以在任务管理器中启动服务，或者通过 `elasticsearch-service.bat start` 启动



### 4. 重置密码

```bash
bin\elasticsearch-reset-password.bat -u elastic
```



### 5. 下载安装kibana

#### 5.1 下载

https://www.elastic.co/cn/downloads/kibana

解压到指定目录

#### 5.2 重置kibana_system用户密码

```bash
elasticsearch-reset-password.bat -v -b -i -u kibana_system
```

#### 5.3 配置kibana密码

config/kibana.yml

```yaml
# If your Elasticsearch is protected with basic authentication, these settings provide
# the username and password that the Kibana server uses to perform maintenance on the Kibana
# index at startup. Your Kibana users still need to authenticate with Elasticsearch, which
# is proxied through the Kibana server.
elasticsearch.username: "kibana_system"
elasticsearch.password: "kibana"
```



#### 5.4 启动kibana

/bin/kibana.bat

#### 5.5 设置kibana为windows服务

下载 nssm

http://www.nssm.cc/download

解压到指定目录，在 nssm\win64 目录下运行

```bash
nssm.exe install kibana
```

弹出的界面中 Path 选择到  kibana\bin\kibana.bat

Startup directory 选择到  kibana\bin 

点击install service即可，添加服务可能需要一段时间，启动 `任务管理器` -> `服务` 即可在列表中找到 `kibana` 服务，右键启动即可。

