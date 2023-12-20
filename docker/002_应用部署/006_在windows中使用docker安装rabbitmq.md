# 在windows中使用docker安装rabbitmq

# 在windows中使用docker搭建nginx



### 1 拉取rabbitmq镜像

```bash
docker pull rabbitmq:latest
```

指定服务器和版本

```bash
docker pull registry.cn-beijing.aliyuncs.com/javashop-k8s-images/rabbitmq:3.6.14
```



### 2 本地磁盘创建rabbitmq目录

```bash
D:\develop\docker\volume\rabbitmq
```

创建3个子目录

```bash
config
data
log
```



### 3 配置



### 4 运行容器

注意路径分隔符使用 `/` 或者 `\\`

```bash
docker run -d --hostname rabbit -p 15672:15672  -p 5672:5672 -p 25672:25672 -p 4369:4369 -p 35672:35672 -v D:/develop/docker/volume/rabbitmq/mqdata:/var/lib/rabbitmq -e RABBITMQ_ERLANG_COOKIE='MY-SECRET-KEY' --name rabbitmq   registry.cn-beijing.aliyuncs.com/javashop-k8s-images/rabbitmq:3.6.14
```



查看运行的容器

```bash
docker ps -a
```



### 5 访问测试

本地浏览器访问 

http://localhost:15672
