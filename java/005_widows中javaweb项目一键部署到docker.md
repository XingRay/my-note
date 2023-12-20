# Windows中javaweb项目一键部署到docker

文件目录结构如下

```bash
├───javashop_install
│   │   Docker Desktop Installer.exe
│   │   install.cmd
│   │   readme.md
│   │   volume.zip
│   │   
│   ├───app
│   │   ├───admin-server
│   │   │       Dockerfile
│   │   │       sba-server-7.2.2.jar
│   │   │       
│   │   ├───base-api
│   │   │       base-api-7.2.2.jar
│   │   │       Dockerfile
│   │   │       
│   │   ├───buyer-api
│   │   │       buyer-api-7.2.2.jar
│   │   │       Dockerfile
│   │   │       
│   │   ├───config-server
│   │   │       config-server-7.2.2.jar
│   │   │       Dockerfile
│   │   │       
│   │   ├───consumer
│   │   │       consumer-7.2.2.jar
│   │   │       Dockerfile
│   │   │       
│   │   ├───manager-api
│   │   │       Dockerfile
│   │   │       manager-api-7.2.2.jar
│   │   │       
│   │   ├───seller-api
│   │   │       Dockerfile
│   │   │       seller-api-7.2.2.jar
│   │   │       
│   │   ├───ui-buyer-pc
│   │   │   │   Dockerfile
│   │   │   │   nginx.conf
│   │   │   │   
│   │   │   └───dist
│   │   │           index.html
│   │   │           
│   │   ├───ui-buyer-wap
│   │   │   │   Dockerfile
│   │   │   │   nginx.conf
│   │   │   │   
│   │   │   └───dist
│   │   │           index.html
│   │   │           
│   │   ├───ui-manager-admin
│   │   │   │   Dockerfile
│   │   │   │   nginx.conf
│   │   │   │   set-envs.sh
│   │   │   │   
│   │   │   └───dist
│   │   │           index.html
│   │   │           
│   │   └───ui-manager-seller
│   │       │   Dockerfile
│   │       │   nginx.conf
│   │       │   set-envs.sh
│   │       │   
│   │       └───dist
│   │               index.html
│   │           
│   ├───data
│   │   ├───es
│   │   │       javashop_goods_analyzer.json
│   │   │       javashop_goods_data.json
│   │   │       javashop_goods_mapping.json
│   │   │       javashop_pt_analyzer.json
│   │   │       javashop_pt_data.json
│   │   │       javashop_pt_mapping.json
│   │   │       
│   │   └───sql
│   │           javashop_db.sql
│   │           javashop_xxl_job.sql
│   │           
│   └───software
│       ├───7z
│       │           
│       ├───curl
│       │           
│       ├───mysql
│       │       
│       ├───node
│       │                   
│       └───wget
```

软件的内部目录及ui项目的文件这里省略，文件说明：

```bash
windows系统中的docker环境
│   │   Docker Desktop Installer.exe

安装脚本
│   │   install.cmd

安装说明
│   │   readme.md

所有模块及中间件的挂载文件的备份，在脚本中会自动解压
│   │   volume.zip
│   │   

项目模块编译打包文件目录
│   ├───app

java项目模块 admin-server 编译打包文件目录，包含dockerfile和jar包
│   │   ├───admin-server
│   │   │       Dockerfile
│   │   │       sba-server-7.2.2.jar
│   │   │       
│   │   ├───base-api
│   │   │       base-api-7.2.2.jar
│   │   │       Dockerfile
│   │   │       
│   │   ├───buyer-api
│   │   │       buyer-api-7.2.2.jar
│   │   │       Dockerfile
│   │   │       
│   │   ├───config-server
│   │   │       config-server-7.2.2.jar
│   │   │       Dockerfile
│   │   │       
│   │   ├───consumer
│   │   │       consumer-7.2.2.jar
│   │   │       Dockerfile
│   │   │       
│   │   ├───manager-api
│   │   │       Dockerfile
│   │   │       manager-api-7.2.2.jar
│   │   │       
│   │   ├───seller-api
│   │   │       Dockerfile
│   │   │       seller-api-7.2.2.jar
│   │   │       
nodejs项目模块 ui-buyer-pc 编译打包文件目录，包含dockerfile和静态编译包dist
│   │   ├───ui-buyer-pc
│   │   │   │   Dockerfile
│   │   │   │   nginx.conf
│   │   │   │   
静态打包文件目录，这里省略了其他的文件
│   │   │   └───dist
│   │   │           index.html
│   │   │           
│   │   ├───ui-buyer-wap
│   │   │   │   Dockerfile
│   │   │   │   nginx.conf
│   │   │   │   
│   │   │   └───dist
│   │   │           index.html
│   │   │           
│   │   ├───ui-manager-admin
│   │   │   │   Dockerfile
│   │   │   │   nginx.conf
│   │   │   │   set-envs.sh
│   │   │   │   
│   │   │   └───dist
│   │   │           index.html
│   │   │           
│   │   └───ui-manager-seller
│   │       │   Dockerfile
│   │       │   nginx.conf
│   │       │   set-envs.sh
│   │       │   
│   │       └───dist
│   │               index.html
│   │           
中间件的初始数据包，安装脚本会将这些数据自动导入到对应的中间件
│   ├───data
│   │   ├───es
es的数据包中，每个索引都包括 analyzer/mapping/data ，要按照 analyzer/mapping/data 的顺序依次导入
│   │   │       javashop_goods_analyzer.json
│   │   │       javashop_goods_data.json
│   │   │       javashop_goods_mapping.json
│   │   │       javashop_pt_analyzer.json
│   │   │       javashop_pt_data.json
│   │   │       javashop_pt_mapping.json
│   │   │       
│   │   └───sql
项目的数据库备份数据
│   │           javashop_db.sql
│   │           javashop_xxl_job.sql
│   │           
安装过程中需要的各种软件
│   └───software
解压软件，使用7z命令行版 7za
│       ├───7z
│       │           
│       ├───curl
│       │           
从标准的mysql-server中提取，仅包含 mysql.exe libssl-1_1-x64.dll libcrypto-1_1-x64.dll 三个文件，
作为mysql的cli客户端使用，用于数据库访问
│       ├───mysql
│       │       
nodejs
│       ├───node
│       │                   
windows版本的wget
│       └───wget
```



java项目的Dockerfile

```bash
FROM openjdk:8-jdk
LABEL maintainer=leixing

ENV CONFIG_SERVER_URL http://localhost:8888
ENV PARAMS="--server.port=8080 --spring.cloud.config.profile=dev --spring.cloud.config.label=master"

#RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

COPY *.jar /app.jar
EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","java -Xmx1024m -Xms128m -Dfile.encoding=utf8  -Djava.security.egd=file:/dev/./urandom -Duser.timezone=Asia/Shanghai -jar /app.jar ${PARAMS} --spring.cloud.config.uri=${CONFIG_SERVER_URL}"]
```



nodejs项目的Dockerfile

```bash
FROM nginx:alpine
MAINTAINER leixing

RUN mkdir -p /app/
COPY ./dist /app/
COPY ./nginx.conf /etc/nginx/nginx.conf

EXPOSE 443 80
```

nginx.conf

```bash
#这个文件给docker用的
#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;
    client_max_body_size 10m;
    gzip on;
    gzip_min_length  5k;
    gzip_buffers     4 16k;
    gzip_http_version 1.0;
    gzip_comp_level 4;
    gzip_types       text/plain application/x-javascript application/javascript text/css application/xml text/javascript application/x-httpd-php image/jpeg image/gif image/png;
    gzip_vary on;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root /app;
            try_files $uri $uri/ /index.html $uri/ =404;
            index  index.html index.htm;
        }
    }
}
```



nodejs项目带初始化脚本版

Dockerfile

```bash
FROM nginx:alpine
MAINTAINER leixing

RUN mkdir -p /app/
COPY ./dist /app/
COPY ./nginx.conf /etc/nginx/nginx.conf
COPY ./set-envs.sh /app/set-envs.sh

RUN ["chmod", "+x", "/app/set-envs.sh"]

EXPOSE 443 80

CMD sh -c "/app/set-envs.sh && exec nginx -g 'daemon off;'"
```



```nginx
#这个文件给docker用的
#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;
    client_max_body_size 10m;
    gzip on;
    gzip_min_length  5k;
    gzip_buffers     4 16k;
    gzip_http_version 1.0;
    gzip_comp_level 4;
    gzip_types       text/plain application/x-javascript application/javascript text/css application/xml text/javascript application/x-httpd-php image/jpeg image/gif image/png;
    gzip_vary on;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root /app;
            try_files $uri $uri/ /index.html $uri/ =404;
            index  index.html index.htm;
        }
    }
}
```

set-env.sh

```bash
echo "window.__NUXT__ = { state: { env: {\
API_MODEL: '${API_MODEL}',\
API_BASE: '${API_BASE}',\
API_BUYER: '${API_BUYER}',\
API_SELLER: '${API_SELLER}',\
API_ADMIN: '${API_ADMIN}',\
DOMAIN_BUYER_PC: '${DOMAIN_BUYER_PC}',\
DOMAIN_BUYER_WAP: '${DOMAIN_BUYER_WAP}',\
DOMAIN_SELLER: '${DOMAIN_SELLER}',\
DOMAIN_ADMIN: '${DOMAIN_ADMIN}'\
} } }" > /app/static/js/envs.js
```





安装脚本如下：

```bash
@echo off
chcp 65001

@echo off
setlocal enabledelayedexpansion

REM 获取所有 IP 地址
for /f "tokens=2 delims=:" %%i in ('ipconfig ^| findstr "IPv4"') do (
    set ip=%%i
    REM 检查 IP 地址的最后一段是否为 1
    for /f "tokens=4 delims=." %%j in ("!ip!") do (
        if "%%j" NEQ "1" (
            REM 找到不是以 1 结尾的 IP 地址
            set "physical_ip=!ip!"
            goto :output
        )
        if not defined physical_ip (
            REM 记录第一个 IP 地址
            set "physical_ip=!ip!"
        )
    )
)

:output
REM 去除物理网卡的 IP 地址前面的空格
set "physical_ip=%physical_ip: =%"

REM 输出物理网卡的 IP 地址（去除空格）
echo 物理网卡 IP 地址为: %physical_ip%

REM 将物理网卡的 IP 地址保存到环境变量中
setx physical_ip "%physical_ip%"

endlocal


set ip=%physical_ip%
@rem set ip=192.168.0.108

set currentPath=%cd%
set volumeDirName=volume
set volumePath=%currentPath%\%volumeDirName%
set esLocation=%ip%:49200
set esIndexPrefix=javashop


echo ip = %ip%
echo currentPath = %currentPath%
echo volumeDirName = %volumeDirName%
echo volumePath = %volumePath%
echo esLocation = %esLocation%
echo esIndexPrefix = %esIndexPrefix%

echo 查看docker版本
echo docker -v
docker -v

echo 停止和删除 javashop-elasticsearch ...
docker stop  javashop-elasticsearch
docker rm  javashop-elasticsearch

echo 停止和删除 javashop-mysql ...
docker stop javashop-mysql
docker rm javashop-mysql

echo 停止和删除 javashop-redis ...
docker stop javashop-redis
docker rm javashop-redis

echo 停止和删除 javashop-rabbitmq ...
docker stop javashop-rabbitmq
docker rm javashop-rabbitmq

echo 停止和删除 javashop-deploy ...
docker stop javashop-deploy
docker rm javashop-deploy

echo 停止和删除 javashop-xxl-job-admin ...
docker stop javashop-xxl-job-admin
docker rm javashop-xxl-job-admin



echo 停止程序 javashop-config-server
echo docker stop javashop-config-server
docker stop javashop-config-server

echo 删除程序 javashop-config-server
echo docker rm javashop-config-server
docker rm javashop-config-server

echo 删除镜像 javashop-config-server
echo docker rmi javashop-config-server:7.2.2
docker rmi javashop-config-server:7.2.2



echo 停止程序 javashop-admin-server
echo docker stop javashop-admin-server
docker stop javashop-admin-server

echo 删除程序 javashop-admin-server
echo docker rm javashop-admin-server
docker rm javashop-admin-server

echo 删除镜像 javashop-admin-server
echo docker rmi javashop-admin-server:7.2.2
docker rmi javashop-admin-server:7.2.2



echo 停止程序 javashop-base-api
echo docker stop javashop-base-api
docker stop javashop-base-api

echo 删除程序 javashop-base-api
echo docker rm javashop-base-api
docker rm javashop-base-api

echo 删除镜像 javashop-base-api
echo docker rmi javashop-base-api:7.2.2
docker rmi javashop-base-api:7.2.2




echo 停止程序 javashop-buyer-api
echo docker stop javashop-buyer-api
docker stop javashop-buyer-api

echo 删除程序 javashop-buyer-api
echo docker rm javashop-buyer-api
docker rm javashop-buyer-api

echo 删除镜像 javashop-buyer-api
echo docker rmi javashop-buyer-api:7.2.2
docker rmi javashop-buyer-api:7.2.2



echo 停止程序 javashop-consumer
echo docker stop javashop-consumer
docker stop javashop-consumer

echo 删除程序 javashop-consumer
echo docker rm javashop-consumer
docker rm javashop-consumer

echo 删除镜像 javashop-consumer
echo docker rmi javashop-consumer:7.2.2
docker rmi javashop-consumer:7.2.2



echo 停止程序 javashop-manager-api
echo docker stop javashop-manager-api
docker stop javashop-manager-api

echo 删除程序 javashop-manager-api
echo docker rm javashop-manager-api
docker rm javashop-manager-api

echo 删除镜像 javashop-manager-api
echo docker rmi javashop-manager-api:7.2.2
docker rmi javashop-manager-api:7.2.2



echo 停止程序 javashop-seller-api
echo docker stop javashop-seller-api
docker stop javashop-seller-api

echo 删除程序 javashop-seller-api
echo docker rm javashop-seller-api
docker rm javashop-seller-api

echo 删除镜像 javashop-seller-api
echo docker rmi javashop-seller-api:7.2.2
docker rmi javashop-seller-api:7.2.2


echo 停止程序 javashop-ui-manager-admin
echo docker stop javashop-ui-manager-admin
docker stop javashop-ui-manager-admin

echo 删除程序 javashop-ui-manager-admin
echo docker rm javashop-ui-manager-admin
docker rm javashop-ui-manager-admin

echo 删除镜像 javashop-ui-manager-admin:7.2.2
echo docker rmi javashop-ui-manager-admin:7.2.2
docker rmi javashop-ui-manager-admin:7.2.2


echo 停止程序 javashop-ui-manager-seller
echo docker stop javashop-ui-manager-seller
docker stop javashop-ui-manager-seller

echo 删除程序 javashop-ui-manager-seller
echo docker rm javashop-ui-manager-seller
docker rm javashop-ui-manager-seller

echo 删除镜像 javashop-ui-manager-seller:7.2.2
echo docker rmi javashop-ui-manager-seller:7.2.2
docker rmi javashop-ui-manager-seller:7.2.2



echo 停止程序 javashop-ui-buyer-pc
echo docker stop javashop-ui-buyer-pc
docker stop javashop-ui-buyer-pc

echo 删除程序 javashop-ui-buyer-pc
echo docker rm javashop-ui-buyer-pc
docker rm javashop-ui-buyer-pc

echo 删除镜像 javashop-ui-buyer-pc:7.2.2
echo docker rmi javashop-ui-buyer-pc:7.2.2
docker rmi javashop-ui-buyer-pc:7.2.2



echo 停止程序 javashop-ui-buyer-wap
echo docker stop javashop-ui-buyer-wap
docker stop javashop-ui-buyer-wap

echo 删除程序 javashop-ui-buyer-wap
echo docker rm javashop-ui-buyer-wap
docker rm javashop-ui-buyer-wap

echo 删除镜像 javashop-ui-buyer-wap:7.2.2
echo docker rmi javashop-ui-buyer-wap:7.2.2
docker rmi javashop-ui-buyer-wap:7.2.2



echo 删除挂载目录  %currentPath%\%volumeDirName%
echo rmdir /S /Q  %currentPath%\%volumeDirName%
rmdir /S /Q  %currentPath%\%volumeDirName%


echo 从备份文件中恢复挂载目录: %currentPath%\%volumeDirName%.zip
echo %currentPath%\software\7z\7za.exe x -o%currentPath% %currentPath%\%volumeDirName%.zip
%currentPath%\software\7z\7za.exe x -o%currentPath% %currentPath%\%volumeDirName%.zip

echo 调整配置

setlocal enabledelayedexpansion

set "OldIP=192.168.0.108"

if "%ip%"=="%OldIP%" (
    echo 当前ip与配置文件中的一致，不需要替换
) else (
    echo 当前ip与配置文件中不同，开始修改配置文件中ip

	for /f "delims=" %%a in (%volumePath%\config-server\b2b2c-config\application-dev.yml) do (
		set "line=%%a"
		set "line=!line:%OldIP%=%ip%!"
		echo !line! >> %volumePath%\config-server\b2b2c-config\application-dev_new.yml
	)

	echo application-dev.yml 重命名为 application-dev_old.yml
	echo ren %volumePath%\config-server\b2b2c-config\application-dev.yml application-dev_old.yml
	ren %volumePath%\config-server\b2b2c-config\application-dev.yml application-dev_old.yml
	
	echo application-dev_new.yml 重命名为 application-dev.yml
	echo %volumePath%\config-server\b2b2c-config\application-dev_new.yml application-dev.yml
	ren %volumePath%\config-server\b2b2c-config\application-dev_new.yml application-dev.yml

	echo ip替换完成
)



echo 开始安装 javashop-mysql
echo docker run --name javashop-mysql -d -p 43306:3306 -e MYSQL_ROOT_PASSWORD=123456 -v %volumePath%/mysql/config/my.cnf:/etc/mysql/my.cnf -v %volumePath%/mysql/log:/opt/mysql/log -v %volumePath%/mysql/data:/var/lib/mysql mysql:5.7.42
docker run --name javashop-mysql -d -p 43306:3306 -e MYSQL_ROOT_PASSWORD=123456 -v %volumePath%/mysql/config/my.cnf:/etc/mysql/my.cnf -v %volumePath%/mysql/log:/opt/mysql/log -v %volumePath%/mysql/data:/var/lib/mysql mysql:5.7.42

echo 请等待程序初始化完成再继续
timeout /nobreak /t 60


echo 开始导入数据到mysql数据库
echo 开始导入数据到主数据库
echo %currentPath%\software\mysql\mysql.exe -h%ip% -P43306 -uroot -p123456 < %currentPath%/data/sql/javashop_db.sql
%currentPath%\software\mysql\mysql.exe -h%ip% -P43306 -uroot -p123456 < %currentPath%/data/sql/javashop_db.sql

echo 开始导入数据到任务调度数据库
echo %currentPath%\software\mysql\mysql.exe -h%ip% -P43306 -uroot -p123456 < %currentPath%/data/sql/javashop_xxl_job.sql
%currentPath%\software\mysql\mysql.exe -h%ip% -P43306 -uroot -p123456 < %currentPath%/data/sql/javashop_xxl_job.sql




echo 开始安装 javashop-elasticsearch
echo docker run --name javashop-elasticsearch -d -v %volumePath%/elasticsearch/data:/usr/share/elasticsearch/data  -v %volumePath%/elasticsearch/plugins:/usr/share/elasticsearch/plugins  -p 49200:9200 -p 49300:9300 -e "discovery.type=single-node" elasticsearch:6.4.3
docker run --name javashop-elasticsearch -d -v %volumePath%/elasticsearch/data:/usr/share/elasticsearch/data  -v %volumePath%/elasticsearch/plugins:/usr/share/elasticsearch/plugins  -p 49200:9200 -p 49300:9300 -e "discovery.type=single-node" elasticsearch:6.4.3

echo 请等待程序初始化完成再继续
timeout /nobreak /t 20

@rem echo 准备导入数据到ElasticSearch
@rem echo 安装导入工具 - nodejs插件elasticdump
@rem echo %currentPath%\software\node\npm.cmd  install elasticdump -g
@rem cmd /c %currentPath%\software\node\npm.cmd  install elasticdump -g

echo 开始导入数据到 ElasticSearch

echo 导入数据到索引 %esIndexPrefix%_pt
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_analyzer.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=analyzer
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_analyzer.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=analyzer
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_mapping.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=mapping
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_mapping.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=mapping
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_data.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=data
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_pt_data.json --output=http://%esLocation%/%esIndexPrefix%_pt --all=true --type=data

echo 导入数据到索引 %esIndexPrefix%_goods
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_analyzer.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=analyzer
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_analyzer.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=analyzer
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_mapping.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=mapping
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_mapping.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=mapping
echo %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_data.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=data
cmd /c %currentPath%\software\node\elasticdump.cmd --input=%currentPath%/data/es/%esIndexPrefix%_goods_data.json --output=http://%esLocation%/%esIndexPrefix%_goods --all=true --type=data

echo 开启索引
cmd /c curl -X POST http://%esLocation%/%esIndexPrefix%_pt/_open
cmd /c curl -X POST http://%esLocation%/%esIndexPrefix%_goods/_open




echo 开始安装 javashop-redis
echo docker run --name javashop-redis -d -p 46379:6379 -v %volumePath%/redis/config:/usr/local/redis/config -v %volumePath%/redis/data:/usr/local/redis/data  -v %volumePath%/redis/log:/usr/local/redis/log redis:5.0.4-alpine redis-server /usr/local/redis/config/redis.conf
docker run --name javashop-redis -d -p 46379:6379 -v %volumePath%/redis/config:/usr/local/redis/config -v %volumePath%/redis/data:/usr/local/redis/data  -v %volumePath%/redis/log:/usr/local/redis/log redis:5.0.4-alpine redis-server /usr/local/redis/config/redis.conf

echo 开始安装 javashop-rabbitmq
echo docker run --name javashop-rabbitmq -d --hostname host-rabbit -p 45670:5672 -p 45671:15672 -p 45672:25672 -p 45673:35672 -p 44369:4369 -v %volumePath%/rabbitmq/config/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf -v %volumePath%/rabbitmq/config/enabled_plugins:/etc/rabbitmq/enabled_plugins -v %volumePath%/rabbitmq/data:/var/lib/rabbitmq/mnesia -v %volumePath%/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.8.9-0199d11c.ez:/opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.8.9-0199d11c.ez -e RABBITMQ_ERLANG_COOKIE='MY-SECRET-KEY' rabbitmq:3.8.9
docker run --name javashop-rabbitmq -d --hostname host-rabbit -p 45670:5672 -p 45671:15672 -p 45672:25672 -p 45673:35672 -p 44369:4369 -v %volumePath%/rabbitmq/config/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf -v %volumePath%/rabbitmq/config/enabled_plugins:/etc/rabbitmq/enabled_plugins -v %volumePath%/rabbitmq/data:/var/lib/rabbitmq/mnesia -v %volumePath%/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.8.9-0199d11c.ez:/opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.8.9-0199d11c.ez -e RABBITMQ_ERLANG_COOKIE='MY-SECRET-KEY' rabbitmq:3.8.9

echo 开始安装 javashop-deploy
echo docker run --name javashop-deploy -d -p 47005:7005 registry.cn-beijing.aliyuncs.com/javashop-k8s-images/deploy:7.2.2
docker run --name javashop-deploy -d -p 47005:7005 registry.cn-beijing.aliyuncs.com/javashop-k8s-images/deploy:7.2.2

echo 开始安装 javashop-xxl-job-admin
echo docker run --name javashop-xxl-job-admin -d -e PARAMS="--spring.datasource.url=jdbc:mysql://%ip%:43306/javashop_xxl_job?Unicode=true&characterEncoding=UTF-8  --spring.datasource.username=root --spring.datasource.password=123456" -p 48080:8080 -v %volumePath%/xxl-job-admin/log:/data/applogs registry.cn-beijing.aliyuncs.com/javashop-k8s-images/xxl-job-admin:2.0.0
docker run --name javashop-xxl-job-admin -d -e PARAMS="--spring.datasource.url=jdbc:mysql://%ip%:43306/javashop_xxl_job?Unicode=true&characterEncoding=UTF-8  --spring.datasource.username=root --spring.datasource.password=123456" -p 48080:8080 -v %volumePath%/xxl-job-admin/log:/data/applogs registry.cn-beijing.aliyuncs.com/javashop-k8s-images/xxl-job-admin:2.0.0





echo 开始安装应用

echo 安装 javashop-config-server

echo 构建镜像 javashop-config-server
echo docker build -t javashop-config-server:7.2.2 -f %currentPath%/app/config-server/Dockerfile %currentPath%/app/config-server
docker build -t javashop-config-server:7.2.2 -f %currentPath%/app/config-server/Dockerfile %currentPath%/app/config-server

echo 运行程序 javashop-config-server
echo docker run --name javashop-config-server -d -v %volumePath%/config-server/b2b2c-config:/b2b2c-config -p 48888:8080 javashop-config-server:7.2.2
docker run --name javashop-config-server -d -v %volumePath%/config-server/b2b2c-config:/b2b2c-config -p 48888:8080 javashop-config-server:7.2.2


echo 请等待系统初始化完成再继续
timeout /nobreak /t 15



echo 安装 javashop-admin-server

echo 构建镜像 javashop-admin-server
echo docker build -t javashop-admin-server:7.2.2 -f %currentPath%/app/admin-server/Dockerfile %currentPath%/app/admin-server
docker build -t javashop-admin-server:7.2.2 -f %currentPath%/app/admin-server/Dockerfile %currentPath%/app/admin-server

echo 运行程序 javashop-admin-server
echo docker run --name javashop-admin-server -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47006:8080 javashop-admin-server:7.2.2
docker run --name javashop-admin-server -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47006:8080 javashop-admin-server:7.2.2



echo 安装 javashop-base-api

echo 构建镜像 javashop-base-api
echo docker build -t javashop-base-api:7.2.2 -f %currentPath%/app/base-api/Dockerfile %currentPath%/app/base-api
docker build -t javashop-base-api:7.2.2 -f %currentPath%/app/base-api/Dockerfile %currentPath%/app/base-api

echo 运行程序 javashop-base-api
echo docker run --name javashop-base-api -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47000:8080 javashop-base-api:7.2.2
docker run --name javashop-base-api -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47000:8080 javashop-base-api:7.2.2



echo 安装 javashop-buyer-api

echo 构建镜像 javashop-buyer-api
echo docker build -t javashop-buyer-api:7.2.2 -f %currentPath%/app/buyer-api/Dockerfile %currentPath%/app/buyer-api
docker build -t javashop-buyer-api:7.2.2 -f %currentPath%/app/buyer-api/Dockerfile %currentPath%/app/buyer-api

echo 运行程序 javashop-buyer-api
echo docker run --name javashop-buyer-api -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47002:8080 javashop-buyer-api:7.2.2
docker run --name javashop-buyer-api -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47002:8080 javashop-buyer-api:7.2.2



echo 安装 javashop-consumer

echo 构建镜像 javashop-consumer
echo docker build -t javashop-consumer:7.2.2 -f %currentPath%/app/consumer/Dockerfile %currentPath%/app/consumer
docker build -t javashop-consumer:7.2.2 -f %currentPath%/app/consumer/Dockerfile %currentPath%/app/consumer

echo 运行程序 javashop-consumer
echo docker run --name javashop-consumer -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 46000:8080 javashop-consumer:7.2.2
docker run --name javashop-consumer -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 46000:8080 javashop-consumer:7.2.2



echo 安装 javashop-manager-api

echo 构建镜像 javashop-manager-api
echo docker build -t javashop-manager-api:7.2.2 -f %currentPath%/app/manager-api/Dockerfile %currentPath%/app/manager-api
docker build -t javashop-manager-api:7.2.2 -f %currentPath%/app/manager-api/Dockerfile %currentPath%/app/manager-api

echo 运行程序 javashop-manager-api
echo docker run --name javashop-manager-api -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47004:8080 javashop-manager-api:7.2.2
docker run --name javashop-manager-api -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47004:8080 javashop-manager-api:7.2.2



echo 安装 javashop-seller-api

echo 构建镜像 javashop-seller-api
echo docker build -t javashop-seller-api:7.2.2 -f %currentPath%/app/seller-api/Dockerfile %currentPath%/app/seller-api
docker build -t javashop-seller-api:7.2.2 -f %currentPath%/app/seller-api/Dockerfile %currentPath%/app/seller-api

echo 运行程序 javashop-seller-api
echo docker run --name javashop-seller-api -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47003:8080 javashop-seller-api:7.2.2
docker run --name javashop-seller-api -d -e "CONFIG_SERVER_URL=http://%ip%:48888" -p 47003:8080 javashop-seller-api:7.2.2

echo 安装 ui-manager-admin
echo 构建镜像 ui-manager-admin
echo docker build -t javashop-ui-manager-admin:7.2.2 -f %currentPath%/app/ui-manager-admin/Dockerfile %currentPath%/app/ui-manager-admin
docker build -t javashop-ui-manager-admin:7.2.2 -f %currentPath%/app/ui-manager-admin/Dockerfile %currentPath%/app/ui-manager-admin
echo 运行程序 ui-manager-admin
docker run --name javashop-ui-manager-admin -d -p 43003:80 javashop-ui-manager-admin:7.2.2



echo 安装 ui-manager-seller
echo 构建镜像 ui-manager-seller
echo docker build -t javashop-ui-manager-seller:7.2.2 -f %currentPath%/app/ui-manager-seller/Dockerfile %currentPath%/app/ui-manager-seller
docker build -t javashop-ui-manager-seller:7.2.2 -f %currentPath%/app/ui-manager-seller/Dockerfile %currentPath%/app/ui-manager-seller
echo 运行程序 ui-manager-seller
echo docker run --name javashop-ui-manager-seller -d -p 43002:80 javashop-ui-manager-seller:7.2.2
docker run --name javashop-ui-manager-seller -d -p 43002:80 javashop-ui-manager-seller:7.2.2




echo 安装 ui-buyer-pc
echo 构建镜像 ui-buyer-pc
echo docker build -t javashop-ui-buyer-pc:7.2.2 -f %currentPath%/app/ui-buyer-pc/Dockerfile %currentPath%/app/ui-buyer-pc
docker build -t javashop-ui-buyer-pc:7.2.2 -f %currentPath%/app/ui-buyer-pc/Dockerfile %currentPath%/app/ui-buyer-pc
echo 运行程序 ui-buyer-pc
echo docker run --name javashop-ui-buyer-pc -d -p 43000:80 javashop-ui-buyer-pc:7.2.2
docker run --name javashop-ui-buyer-pc -d -p 43000:80 javashop-ui-buyer-pc:7.2.2




echo 安装 ui-buyer-wap
echo 构建镜像 ui-buyer-wap
echo docker build -t javashop-ui-buyer-wap:7.2.2 -f %currentPath%/app/ui-buyer-wap/Dockerfile %currentPath%/app/ui-buyer-wap
docker build -t javashop-ui-buyer-wap:7.2.2 -f %currentPath%/app/ui-buyer-wap/Dockerfile %currentPath%/app/ui-buyer-wap
echo 运行程序 ui-buyer-wap
echo docker run --name javashop-ui-buyer-wap -d -p 43001:80 javashop-ui-buyer-wap:7.2.2
docker run --name javashop-ui-buyer-wap -d -p 43001:80 javashop-ui-buyer-wap:7.2.2





echo "+----------------------------+"
echo "+                            +"
echo "+                            +"
echo "+          安装完成           +"
echo "+                            +"
echo "+                            +"
echo "+----------------------------+"
echo  按任意键退出
pause
```



启动脚本，从下到上启动

```bash
@echo off

echo 启动 javashop-mysql
echo docker start javashop-mysql
docker start javashop-mysql

echo 请等待程序初始化完成再继续
timeout /nobreak /t 30



echo 启动 javashop-elasticsearch
echo docker start javashop-elasticsearch
docker start javashop-elasticsearch

echo 请等待程序初始化完成再继续
timeout /nobreak /t 20


echo 启动 javashop-redis
echo docker start javashop-redis
docker start javashop-redis

echo 启动 javashop-rabbitmq
echo docker start javashop-rabbitmq
docker start javashop-rabbitmq

echo 启动 javashop-deploy
echo docker start javashop-deploy
docker start javashop-deploy

echo 启动 javashop-xxl-job-admin
echo docker start javashop-xxl-job-admin
docker start javashop-xxl-job-admin



echo 启动应用

echo 启动 javashop-config-server
echo docker start javashop-config-server
docker start javashop-config-server

echo 请等待系统初始化完成再继续
timeout /nobreak /t 15


echo 启动 javashop-admin-server
echo docker start javashop-admin-server
docker start javashop-admin-server


echo 启动 javashop-base-api
echo docker start javashop-base-api
docker start javashop-base-api


echo 启动 javashop-buyer-api
echo docker start javashop-buyer-api
docker start javashop-buyer-api


echo 启动 javashop-consumer
echo docker start javashop-consumer
docker start javashop-consumer


echo 启动 javashop-manager-api
echo docker start javashop-manager-api
docker start javashop-manager-api


echo 启动 javashop-seller-api
echo docker start javashop-seller-api
docker start javashop-seller-api

echo 启动 ui-manager-admin
echo docker start javashop-ui-manager-admin
docker start javashop-ui-manager-admin


echo 启动 ui-manager-seller
echo docker start javashop-ui-manager-seller
docker start javashop-ui-manager-seller


echo 启动 ui-buyer-pc
echo docker start javashop-ui-buyer-pc
docker start javashop-ui-buyer-pc


echo 启动 ui-buyer-wap
echo docker start javashop-ui-buyer-wap
docker start javashop-ui-buyer-wap




echo "+----------------------------+"
echo "+                            +"
echo "+                            +"
echo "+          操作完成          +"
echo "+                            +"
echo "+                            +"
echo "+----------------------------+"
echo  按任意键退出
pause
```





停止脚本，从上到下停止

```bash
@echo off

echo 停止程序 javashop-ui-buyer-wap
echo docker stop javashop-ui-buyer-wap
docker stop javashop-ui-buyer-wap

echo 停止程序 javashop-ui-buyer-pc
echo docker stop javashop-ui-buyer-pc
docker stop javashop-ui-buyer-pc

echo 停止程序 javashop-ui-manager-seller
echo docker stop javashop-ui-manager-seller
docker stop javashop-ui-manager-seller

echo 停止程序 javashop-ui-manager-admin
echo docker stop javashop-ui-manager-admin
docker stop javashop-ui-manager-admin




echo 停止程序 javashop-seller-api
echo docker stop javashop-seller-api
docker stop javashop-seller-api

echo 停止程序 javashop-manager-api
echo docker stop javashop-manager-api
docker stop javashop-manager-api

echo 停止程序 javashop-consumer
echo docker stop javashop-consumer
docker stop javashop-consumer

echo 停止程序 javashop-buyer-api
echo docker stop javashop-buyer-api
docker stop javashop-buyer-api

echo 停止程序 javashop-base-api
echo docker stop javashop-base-api
docker stop javashop-base-api

echo 停止程序 javashop-admin-server
echo docker stop javashop-admin-server
docker stop javashop-admin-server

echo 停止程序 javashop-config-server
echo docker stop javashop-config-server
docker stop javashop-config-server

echo 停止 javashop-xxl-job-admin ...
echo docker stop javashop-xxl-job-admin
docker stop javashop-xxl-job-admin

echo 停止 javashop-deploy ...
echo docker stop javashop-deploy
docker stop javashop-deploy



echo 停止 javashop-elasticsearch
echo docker stop  javashop-elasticsearch
docker stop  javashop-elasticsearch

echo 停止 javashop-mysql
echo docker stop javashop-mysql
docker stop javashop-mysql

echo 停止 javashop-redis
echo docker stop javashop-redis
docker stop javashop-redis

echo 停止和删除 javashop-rabbitmq
echo docker stop javashop-rabbitmq
docker stop javashop-rabbitmq



echo "+----------------------------+"
echo "+                            +"
echo "+                            +"
echo "+          操作完成          +"
echo "+                            +"
echo "+                            +"
echo "+----------------------------+"
echo  按任意键退出
pause
```









