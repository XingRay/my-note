## nacos集群

### 1. 下载与安装

参考01_nacos入门

### 2. 创建数据库

在mysql中创建数据库，命名为nacos，在nacos安装包中 /conf目录下有mysql-schema.sql文件，导入数据库即可。

### 3. 修改配置

/conf 目录下修改application.properties

```properties
### Default web server port:
server.port=8843

#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
spring.datasource.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=root
db.password.0=123456
```

数据库配置根据实际情况修改



修改 /conf 目录下的cluster.conf文件，内容如下：

```properties
192.168.0.118:8843
192.168.0.118:8845
192.168.0.118:8847
```

将nacos文件夹复制3份，每份作为一个nacos实例，将3个实例依次修改为8843,8845, 8847,注意端口占用的情况，集群模式下实例之间会通过rpc调用，额外占用端口。



### 4. 启动

在各个实例下运行startup脚本即可



### 5. 配置nginx



nginx.conf

```nginx
worker_processes  2;
daemon on;

events {
    worker_connections  1024;
}

http {
  
	upstream nacos-cluster{
		server localhost:8843;
		server localhost:8845;
		server localhost:8847;
	}

    server {
        listen       8848;
        server_name  nacos;

        location /nacos {
            proxy_pass http://nacos-cluster;
        }
	}
}
```

启动nginx 

```bash
nginx -c ./nginx.conf
```



这里配置nginx监听8848端口进行转发，这样微服务的配置可以不修改。也可以监听其他端口，这样微服务的配置也需要做相应的调整。



### 6. 附录



参考文档：

https://nacos.io/zh-cn/docs/v2/guide/admin/cluster-mode-quick-start.html



端口配置为8843 8845 8847，端口占用情况:

    TCP    0.0.0.0:7843           0.0.0.0:0              LISTENING       21780
    TCP    0.0.0.0:7845           0.0.0.0:0              LISTENING       22640
    TCP    0.0.0.0:7847           0.0.0.0:0              LISTENING       24508
    TCP    0.0.0.0:8843           0.0.0.0:0              LISTENING       21780
    TCP    0.0.0.0:8845           0.0.0.0:0              LISTENING       22640
    TCP    0.0.0.0:8847           0.0.0.0:0              LISTENING       24508
    TCP    0.0.0.0:9843           0.0.0.0:0              LISTENING       21780
    TCP    0.0.0.0:9844           0.0.0.0:0              LISTENING       21780
    TCP    0.0.0.0:9845           0.0.0.0:0              LISTENING       22640
    TCP    0.0.0.0:9846           0.0.0.0:0              LISTENING       22640
    TCP    0.0.0.0:9847           0.0.0.0:0              LISTENING       24508
    TCP    0.0.0.0:9848           0.0.0.0:0              LISTENING       24508



