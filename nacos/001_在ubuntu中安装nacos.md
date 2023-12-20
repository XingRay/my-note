# 在Ubuntu中安装nacos



https://nacos.io/zh-cn/

https://github.com/alibaba/nacos



## 1 下载nacos

下载最新版本, 当前最新版本下在地址如下 : 

https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.zip

https://github.com/alibaba/nacos/releases/download/2.2.3/nacos-server-2.2.3.tar.gz



## 2 安装

将下载的 nacos-server-2.2.3.tar.gz 上传至服务器

创建nacos目录

```bash
mkdir -p /root/develop/nacos
```

解压

```bash
tar -zxvf nacos-server-2.2.3.tar.gz -C /root/develop/nacos/
```



### 3 将Nacos添加为Service

服务器在运行过程中宕机是不可避免的，为了在服务器宕机的时候不需要去重启应用，我们会把应用封装成服务，然后设置其开机自动启动。本文以Nacos注册中心为例，演示如何在Ubuntu服务器中将应用封装成服务，并设置开机自启动。



配置步骤

1 修改启动脚本

```bash
vi /root/develop/nacos/nacos/bin/startup.sh
```

```bash
export JAVA_HOME=/root/develop/java/jdk/20/graalvm-jdk-20.0.1+9.1
```



2 创建service文件

首先需要在`/lib/systemd/system`目录下创建nacos.service文件

```bash
vi /lib/systemd/system/nacos.service
```

文件内容如下

```properties
[Unit]
# 服务的描述
Description=alibaba-nacos-server
# 在网络启动之后启动
After=network.target
# 文档地址
Documentation=https://nacos.io/zh-cn/

[Service]
User=root
Group=root
Type=forking

# 服务启动命令
ExecStart=/root/develop/nacos/nacos/bin/startup.sh -m standalone

# 服务停止命令
ExecStop=/root/develop/nacos/nacos/bin/shutdown.sh

# 服务重启命令
ExecReload=/root/develop/nacos/nacos/bin/shutdown.sh

# 服务重启策略
#Restart=on-failure
Restart=always


PrivateTmp=true

[Install]
WantedBy=multi-user.target
Alias=nacos.service
```



3 保存服务并设置开机自启

创建完文件保存之后，需要重新加载服务，并设置开机自启。设置开机自启动

重新加载服务

```bash
systemctl daemon-reload
```

设置开机自启

```bash
systemctl enable nacos.service
```

验证是否设置成功

```bash
systemctl is-enabled nacos.service
```

启动nacos

```bash
systemctl start nacos.service
```



## 4 配置nacos数据源使用mysql

使用navecat等mysql客户端连入mysql,创建数据库,名为 nacos , 字符编码为 utf8mb4 ,导入 nacos 数据库初始化脚本

```bash
/root/develop/nacos/nacos/conf/mysql-schema.sql
```



修改nacos配置文件

```bash
vi /root/develop/nacos/nacos/conf/application.properties
```

修改数据源部分 : 

```properties
#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
### Deprecated configuration property, it is recommended to use `spring.sql.init.platform` replaced.
# spring.datasource.platform=mysql
spring.sql.init.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=root
db.password.0=123456
```

数据库的配置根据实际情况修改,注意修改数据库的url , 数据库名, 账号, 密码

配置完成后重启nacos

```bash
systemctl stop nacos.service
systemctl start nacos.service
```



访问 http://192.168.0.140:8848/nacos 账号密码为 nacos / nacos



修改log目录

```bash
mkdir -p /root/develop/nacos/log
```

修改application.properties

```bash
vi /root/develop/nacos/nacos/conf/application.properties
```

修改如下字段 : 

```
### The directory of access log:
server.tomcat.basedir=/root/develop/nacos/log
```

