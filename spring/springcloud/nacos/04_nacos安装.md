## 安装nacos

### 1. 环境

ubuntu server 22.04

java openjdk19



### 2. 安装过程

#### 2.1 安装配置Java

参考 \software-project\deploy\java\jdk\jdk安装.md

#### 2.2 下载nacos

下载地址 ：[nacos](https://github.com/alibaba/nacos)

下载 nacos-server-2.2.0.tar.gz到本地，再通过sftp上传至ubuntu server

#### 2.3 安装nacos

创建安装目录

```bash
 sudo mkdir /usr/local/software/java/nacos
```

复制安装包

```bash
sudo cp ./nacos-server-2.2.0.tar.gz /usr/local/software/java/nacos/
```

切换目录

```bash
cd /usr/local/software/java/nacos/
```

为当前版本创建一个目录

```bash
sudo mkdir 2.2.0
```

解压文件到指定目录

```bash
sudo tar -zxvf nacos-server-2.2.0.tar.gz -C 2.2.0
```

此时直接启动会提示找不到JAVA_HOME

```bash
sudo ./2.2.0/nacos/bin/startup.sh -m standalone

readlink: missing operand
Try 'readlink --help' for more information.
dirname: missing operand
Try 'dirname --help' for more information.
ERROR: Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! !!
```



在启动文件startup.sh中加入JAVA_HOME信息：

```bash
sudo nano ./2.2.0/nacos/bin/startup.sh
```

```bash
#!/bin/bash

# Copyright 1999-2018 Alibaba Group Holding Ltd.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

export JAVA_HOME=/usr/local/software/java/jdk/19/jdk-19.0.2


cygwin=false
darwin=false
os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Darwin*) darwin=true;;
OS400*) os400=true;;
esac
...
```

单机模式启动nacos

```bash
sudo ./2.2.0/nacos/bin/startup.sh -m standalone
```

nacos启动后，在浏览器中打开http://ip:8848/nacos 如 http://192.168.0.1:8848/nacos，可以看到Nacos的界面，输入初始用户名密码（nacos/nacos），即可登录Nacos

停止nacos

```bash
sudo ./2.2.0/nacos/bin/shutdown.sh -m standalone
```



#### 2.4 配置Nacos为系统服务

将nacos配置为自启动服务。
首先，在/usr/local/nacos文件夹下创建nacos.service文件：

```bash
sudo nano /usr/local/nacos/nacos.service
```

然后将服务信息输入到该文件中：

```bash
[Unit]
Description=nacos
After=network.target

[Service]
Type=forking

ExecStart=/usr/local/software/java/nacos/2.2.0/nacos/bin/startup.sh -m standalone
ExecReload=/usr/local/software/java/nacos/2.2.0/nacos/bin/shutdown.sh
ExecStop=/usr/local/software/java/nacos/2.2.0/nacos/bin/shutdown.sh

# Let systemd restart this service always
Restart=always

PrivateTmp=true

[Install]
WantedBy=multi-user.target
Alias=nacos.service
```

输入完成后，保存退出。接下来让服务生效：

在/lib/systemd/system中创建nacos.service的软连接（主要是为了升级方便）

```bash
sudo ln -s /usr/local/software/java/nacos/nacos.service  /lib/systemd/system/nacos.service
```

让服务生效

```bash
sudo systemctl daemon-reload
sudo systemctl enable nacos.service
```

启动nacos服务

```bash
sudo systemctl start nacos
```

为测试效果，可以直接重启Ubuntu；当系统重启后，nacos服务将自动启动。



#### 2.5 配置MySQL数据库

在Nacos集群中，必须采用MySQL数据库存储配置Nacos信息，且只能使用MySQL（其他数据库会忽略）。

5.1 创建数据库
首先在MySQL中创建一个名称为nacos的表：

```sql
CREATE DATABASE  `nacos` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```


然后创建用于访问nacos数据库的用户并授权：

创建用户

```sql
create user 'nacos'@'%' identified by 'nacos';
```

nacos.* 表示nacos的所有表

授予权限

```sql
grant all privileges on nacos.* to 'nacos'@'%' with grant option;
flush privileges;
```

5.2 导入SQL语句
将nacos安装目录中\nacos\conf文件夹下的 mysql-schema.sql 导入数据库nacos中。

5.3 配置Nacos
打开nacos配置文件：

sudo nano application.properties

修改一下内容

```bash
#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
spring.datasource.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=fals>
db.user.0=nacos
db.password.0=nacos
```

配置完成后，重启nacos服务即可。

```bash
sudo systemctl stop nacos
sudo systemctl start nacos
```



防火墙相关设置

```bash
sudo ufw status
sudo ufw enable
sudo ufw allow 8848
sudo ufw allow 9848
sudo ufw allow 3306
```

参考：

https://blog.csdn.net/houor/article/details/126683909