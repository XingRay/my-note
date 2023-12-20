# 在windows中使用docker搭建nginx



### 1 拉取elasticsearch镜像

```bash
docker pull elasticsearch:latest
```

指定服务器和版本

```bash
docker pull registry.cn-beijing.aliyuncs.com/javashop-k8s-images/elasticsearch:6.2.2
```



### 2 本地磁盘创建elasticsearch目录

```bash
D:\develop\docker\volume\elasticsearch
```

创建3个子目录

```bash
config
data
log
```



### 3 配置扩展词典api

在配置目录下创建子目录

```bash
D:\develop\docker\volume\elasticsearch\config\ik
```

创建文件 `IKAnalyzer.cfg.xml`  ，内容如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <comment>IK Analyzer 扩展配置</comment>
    <!--用户可以在这里配置远程扩展字典 -->
    <entry key="remote_ext_dict">http://api.base.test.com/load-customwords?secret_key=secret_value</entry>
</properties>
```

其中域名要根据实际规划的域名进行配置,在此处设置secret_key的值需要记录下,所有部署完成后,需要在管理端进行保存



### 4 运行容器

注意路径分隔符使用 `/` 或者 `\\`

```bash
docker run --rm -d --name elasticsearch -v D:/develop/docker/volume/elasticsearch/data:/usr/share/elasticsearch/data  -v D:/develop/docker/volume/elasticsearch/config/ik/IKAnalyzer.cfg.xml:/usr/share/elasticsearch/plugins/ik/config/IKAnalyzer.cfg.xml  -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" registry.cn-beijing.aliyuncs.com/javashop-k8s-images/elasticsearch:6.2.2
```



查看运行的容器

```bash
docker ps -a
```

```bash
PS C:\Users\leixing> docker ps -a
CONTAINER ID   IMAGE                                                                      COMMAND                  CREATED          STATUS          PORTS                                            NA
MES
e43a87ed1d1b   registry.cn-beijing.aliyuncs.com/javashop-k8s-images/elasticsearch:6.2.2   "/usr/local/bin/dock…"   21 seconds ago   Up 21 seconds   0.0.0.0:9200->9200/tcp, 0.0.0.0:9300->9300/tcp   el
asticsearch
```



### 5 访问测试

本地浏览器访问 

http://localhost:9200/_cluster/health