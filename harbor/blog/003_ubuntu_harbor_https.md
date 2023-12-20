# Ubuntu18 安装Harbor 并配置https

实验环境：

软件名称	版本
docker-ce	v19.03.15
docker-compose	v1.29.0
Harbor	v2.3.2
一、Harbor简介


Harbor是由VMware公司开源的企业级的Docker Registry管理项目，拥有更丰富的权限权利和完善的架构设计，适用大规模docker集群部署提供仓库服务。
Harbor 是 CNCF 毕业的项目，可提供合规性、性能和互操作性，帮助跨云原生计算平台（如 Kubernetes 和 Docker）一致且安全地管理工件。
Harbor 提供了 Dcoker Registry 管理界面UI，可基于角色访问控制,镜像复制， AD/LDAP 集成，日志审核等功能，并且完全的支持中文。
二、Harbor 的主要功能
基于角色的访问控制
用户与Docker镜像仓库通过“项目”进行组织管理，一个用户可以对多个镜像仓库在同一命名空间（project）里有不同的权限。

基于镜像的复制策略
镜像可以在多个Registry实例中复制（可以将仓库中的镜像同步到远程的Harbor，类似于MySQL主从同步功能），尤其适合于负载均衡，高可用，混合云和多云的场景。

图形化用户界面
用户可以通过浏览器来浏览，检索当前Docker镜像仓库，管理项目和命名空间。

支持 AD/LDAP
Harbor可以集成企业内部已有的AD/LDAP，用于鉴权认证管理。

镜像删除和垃圾回收
Harbor支持在Web删除镜像，回收无用的镜像，释放磁盘空间。image可以被删除并且回收image占用的空间。

审计管理
所有针对镜像仓库的操作都可以被记录追溯，用于审计管理。

RESTful API
RESTful API 提供给管理员对于Harbor更多的操控, 使得与其它管理软件集成变得更容易。

部署简单
提供在线和离线两种安装工具， 直接使用docker部署，但需要依赖docker官方的 Docker Compose 容器编排工具

支持Helm charts仓库
         支持Helm charts仓库的功能，需要在安装的时候指定下参数，如 ./install.sh --with-chartmuseum


​          

三、Harbor 架构组件


Habor组件相对较多，看下都运行了哪些容器：

root@manager:/opt/harbor# docker-compose ps
      Name                     Command                  State                                          Ports                                    
------------------------------------------------------------------------------------------------------------------------------------------------
harbor-core         /harbor/entrypoint.sh            Up (healthy)                                                                               
harbor-db           /docker-entrypoint.sh 96 13      Up (healthy)                                                                               
harbor-jobservice   /harbor/entrypoint.sh            Up (healthy)                                                                               
harbor-log          /bin/sh -c /usr/local/bin/ ...   Up (healthy)   127.0.0.1:1514->10514/tcp                                                   
harbor-portal       nginx -g daemon off;             Up (healthy)                                                                               
nginx               nginx -g daemon off;             Up (healthy)   0.0.0.0:80->8080/tcp,:::80->8080/tcp, 0.0.0.0:443->8443/tcp,:::443->8443/tcp
redis               redis-server /etc/redis.conf     Up (healthy)                                                                               
registry            /home/harbor/entrypoint.sh       Up (healthy)                                                                               
registryctl         /home/harbor/start.sh            Up (healthy)                      
组件名称	说明	实现方式
Proxy	用于转发用户的请求到registry/ui/token service 的反向代理	nginx：使用nginx官方的镜像进行配置
Registry	镜像的push/pull 命令实现的功能	registry：使用registry官方镜像
Database	保存项目/用户/角色/复制策略等信息到数据库中	harbor-db: Mariadb的官方镜像，用于保存Harbor的数据库信息
Core-service	用户进行镜像操作的界面实现，通过webhook的机制保证镜像状态的变化harbor能够及时了解以便进行日志更新等操作，而项目用户角色则通过token进行镜像的push/pull等操作	harbor-ui 等
Job services	镜像复制，可以在harbor实例之间进行镜像的复制或者同步等操作	harbor-jobservice 
Log collector	负责收集各个镜像的日志信息进行统一管理	harbor-log：日志默认保存在/var/log/harbor
四、Harbor 部署
1、安装docker-ce 
安装docker采用清华镜像源：https://mirrors.tuna.tsinghua.edu.cn/help/docker-ce/

使用阿里云镜像加速

4.1.1、如果你过去安装过 docker，先删掉:

sudo 
apt-get remove docker docker-engine docker.io
4.1.2、首先安装依赖:

sudo apt-get install apt-transport-https ca-certificates curl gnupg2 software-properties-common
4.1.3、根据你的发行版，下面的内容有所不同。你使用的发行版： 

信任 Docker 的 GPG 公钥:

sudo 
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
4.1.4、对于 amd64 架构的计算机，添加软件仓库:

sudo add-apt-repository \
"deb [arch=amd64] https://mirrors.tuna.tsinghua.edu.cn/docker-ce/linux/ubuntu \
$(lsb_release -cs) \
stable"

4.1.5、安装指定版本的Docker-CE:

 Step 1: 查找Docker-CE的版本:

apt-cache madison docker-ce
 Step 1: 安装制定版本： 安装docker-ce 会自动依赖安装上docker-cli [docker 客户端工具]

sudo apt-get install docker-ce=5:19.03.15~3-0~ubuntu-bionic -y
4.1.6、使用阿里云docker 镜像加速

登录到阿里云，选择容器镜像服务，点击镜像加速器





4.1.7、配置镜像加速器

可以通过修改daemon配置文件/etc/docker/daemon.json来使用加速器

sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://u2vzou7d.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
2、安装docker-compose
从GitHub上 下载docker-compose 二进制包： Release 1.29.0 · docker/compose · GitHub

下载  docker-compose-Linux-x86_64 版本



#把下载下来的软件包上传到/root 路径下
root@manager:~# mv docker-compose-Linux-x86_64 /usr/bin/docker-compose 
root@manager:~# chmod +x  /usr/bin/docker-compose

#安装后查看下版本
root@manager:~# docker-compose version
docker-compose version 1.29.0, build 07737305
docker-py version: 5.0.0
CPython version: 3.7.10
OpenSSL version: OpenSSL 1.1.0l  10 Sep 2019
3、创建harbor证书
PS:  公司一般都会有商业的证书，直接拿过来使用即可

如果只是想做一张内网用的电子证书或不想花钱去找个 CA 签署，可以造一张自签 (Self-signed)的电子证书。当然这类电子证书没有任何保证，浏览器遇到这证书会发出警告，甚至不接收这类证书。使用自签名(self-signed)的证书，它的主要目的不是防伪，而是使用户和系统之间能够进行SSL通信，保证密码等个人信息传输时的安全。

#创建证书路径
root@manager:/# mkdir /root/cert
root@manager:~/cert# cd /root/cert/
    

#生成一个.key文件
root@manager:~/cert# openssl genrsa  -out ssl.key 1024


#根据这个key文件生成.csr 证书请求文件
root@manager:~/cert# openssl req -new -key ssl.key -out ssl.csr
Can't load /root/.rnd into RNG
140061599584704:error:2406F079:random number generator:RAND_load_file:Cannot open file:../crypto/rand/randfile.c:88:Filename=/root/.rnd
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [AU]:CN
State or Province Name (full name) [Some-State]:BeiJing
Locality Name (eg, city) []:BeiJing
Organization Name (eg, company) [Internet Widgits Pty Ltd]:fxkjnj
Organizational Unit Name (eg, section) []:fxkjnj
Common Name (e.g. server FQDN or YOUR name) []:harbor.fxkjnj.com
Email Address []:admin@fxkjnj.com

Please enter the following 'extra' attributes
to be sent with your certificate request
A challenge password []:
An optional company name []:

 

#根据这2个文件生成.crt证书文件,10年有效期
root@manager:~/cert# openssl x509 -req -days 3650 -in ssl.csr -signkey ssl.key -out ssl.crt
Signature ok
subject=C = CN, ST = BeiJing, L = Beijing, O = FXKJNJ, OU = FXKJNJ, CN = harbor.fxkjnj.com, emailAddress = admin@fxkjnj.com
Getting Private key


root@manager:~/cert# ls -l
total 12
-rw-r--r-- 1 root root 977 Sep 29 16:01 ssl.crt
-rw-r--r-- 1 root root 708 Sep 29 15:07 ssl.csr
-rw------- 1 root root 887 Sep 29 14:59 ssl.key
4、安装harbor
从GitHub上 下载Harbor的软件包： Release v2.3.2 · goharbor/harbor · GitHub

下载 harbor-offline-installer-v2.3.2.tgz 离线版



4.1、 把下载下来的软件包上传到/root 路径下

root@manager:~# tar -xf harbor-offline-installer-v2.3.2.tgz -C /opt/
root@manager:~# cd /opt/harbor
root@manager:/opt/harbor# ls -l
total 594384
-rw-r--r-- 1 root root      3361 Aug 18 16:51 common.sh
-rw-r--r-- 1 root root 608611132 Aug 18 16:52 harbor.v2.3.2.tar.gz
-rw-r--r-- 1 root root      7840 Aug 18 16:51 harbor.yml.tmpl
-rwxr-xr-x 1 root root      2500 Aug 18 16:51 install.sh
-rw-r--r-- 1 root root     11347 Aug 18 16:51 LICENSE
-rwxr-xr-x 1 root root      1881 Aug 18 16:51 prepare
4.2、 把创建的证书，复制到/opt/harbor 路径下

root@manager:/opt/harbor# cp /root/cert/{ssl.key,ssl.crt}  /opt/harbor/
root@manager:/opt/harbor# ls -l
total 594428
drwxr-xr-x 5 root  root       4096 Sep 29 16:04 ./
drwxr-xr-x 4 root  root       4096 Sep 29 15:12 ../
drwxr-xr-x 3 root  root       4096 Sep 29 15:50 common/
-rw-r--r-- 1 root  root       3361 Aug 18 16:51 common.sh
drwxr-xr-x 9 root  root       4096 Sep 29 15:56 data/
-rw-r--r-- 1 root  root       7058 Sep 29 15:56 docker-compose.yml
-rw-r--r-- 1 root  root  608611132 Aug 18 16:52 harbor.v2.3.2.tar.gz
-rw-r--r-- 1 root  root       7871 Sep 29 15:55 harbor.yml
-rw-r--r-- 1 root  root       7840 Aug 18 16:51 harbor.yml.tmpl
-rwxr-xr-x 1 root  root       2500 Aug 18 16:51 install.sh*
-rw-r--r-- 1 root  root      11347 Aug 18 16:51 LICENSE
drwxr-xr-x 2 10000 10000      4096 Sep 29 15:57 log/
-rwxr-xr-x 1 root  root       1881 Aug 18 16:51 prepare*
-rw-r--r-- 1 root  root        977 Sep 29 16:04 ssl.crt
-rw------- 1 root  root        887 Sep 29 16:04 ssl.key

4.3、 创建harbor配置文件

root@manager:/opt# cd /opt/haror
root@manager:/opt/harbor# cp harbor.yml.tmpl harbor.yml

root@manager:/opt/harbor# vim harbor.yml
#只需要修改成如下内容

.................
#配置Harbor域名访问地址
hostname: harbor.fxkjnj.com
.................
#配置Harbor证书路径
certificate:  /opt/harbor/ssl.crt
private_key:  /opt/harbor/ssl.key
.................
#配置Harbor管理员密码
harbor_admin_password: fxkjnj
.................
#配置Harbor数据存放路径
data_volume: /opt/harbor/data
.................
#配置Harbor日志路径
location: /opt/harbor/log
.................

#保存并退出
4.4、创建harbor数据目录和日志目录

root@manager:/opt# mkdir /opt/harbor/data
root@manager:/opt# mkdir /opt/harbor/log
4.5、执行./install.sh 脚本安装harbor

# 为了后期使用Helm 方便，我们之间安装上helm，添加安装参数：--with-chartmuseum 

root@manager:/opt# cd /opt/harbor
root@manager:/opt/harbor# ./install.sh --with-chartmuseum

[Step 0]: checking if docker is installed ...

Note: docker version: 20.10.8

[Step 1]: checking docker-compose is installed ...

Note: docker-compose version: 1.29.0

[Step 2]: loading Harbor images ...
Loaded image: goharbor/redis-photon:v2.3.2
Loaded image: goharbor/nginx-photon:v2.3.2
Loaded image: goharbor/harbor-portal:v2.3.2
Loaded image: goharbor/trivy-adapter-photon:v2.3.2
Loaded image: goharbor/chartmuseum-photon:v2.3.2
Loaded image: goharbor/notary-signer-photon:v2.3.2
Loaded image: goharbor/harbor-core:v2.3.2
Loaded image: goharbor/harbor-log:v2.3.2
Loaded image: goharbor/harbor-registryctl:v2.3.2
Loaded image: goharbor/harbor-exporter:v2.3.2
Loaded image: goharbor/notary-server-photon:v2.3.2
Loaded image: goharbor/prepare:v2.3.2
Loaded image: goharbor/harbor-db:v2.3.2
Loaded image: goharbor/harbor-jobservice:v2.3.2
Loaded image: goharbor/registry-photon:v2.3.2


[Step 3]: preparing environment ...

[Step 4]: preparing harbor configs ...
prepare base dir is set to /opt/harbor
Generated configuration file: /config/portal/nginx.conf
Generated configuration file: /config/log/logrotate.conf
Generated configuration file: /config/log/rsyslog_docker.conf
Generated configuration file: /config/nginx/nginx.conf
Generated configuration file: /config/core/env
Generated configuration file: /config/core/app.conf
Generated configuration file: /config/registry/config.yml
Generated configuration file: /config/registryctl/env
Generated configuration file: /config/registryctl/config.yml
Generated configuration file: /config/db/env
Generated configuration file: /config/jobservice/env
Generated configuration file: /config/jobservice/config.yml
Generated and saved secret to file: /data/secret/keys/secretkey
Successfully called func: create_root_cert
Generated configuration file: /config/chartserver/env
Generated configuration file: /compose_location/docker-compose.yml
Clean up the input dir


Note: stopping existing Harbor instance ...
Removing harbor-jobservice ... done
Removing nginx             ... done
Removing harbor-core       ... done
Removing registry          ... done
Removing harbor-portal     ... done
Removing redis             ... done
Removing registryctl       ... done
Removing harbor-db         ... done
Removing harbor-log        ... done
Removing network harbor_harbor
Removing network harbor_harbor-chartmuseum
WARNING: Network harbor_harbor-chartmuseum not found.


[Step 5]: starting Harbor ...
Creating network "harbor_harbor" with the default driver
Creating network "harbor_harbor-chartmuseum" with the default driver
Creating harbor-log ... done
Creating redis         ... done
Creating harbor-db     ... done
Creating chartmuseum   ... done
Creating registry      ... done
Creating registryctl   ... done
Creating harbor-portal ... done
Creating harbor-core   ... done
Creating harbor-jobservice ... done
Creating nginx             ... done
✔ ----Harbor has been installed and started successfully.----

 


root@manager:/opt/harbor# docker-compose ps
      Name                     Command                  State                                          Ports                                    
------------------------------------------------------------------------------------------------------------------------------------------------
chartmuseum         ./docker-entrypoint.sh           Up (healthy)                                                                               
harbor-core         /harbor/entrypoint.sh            Up (healthy)                                                                               
harbor-db           /docker-entrypoint.sh 96 13      Up (healthy)                                                                               
harbor-jobservice   /harbor/entrypoint.sh            Up (healthy)                                                                               
harbor-log          /bin/sh -c /usr/local/bin/ ...   Up (healthy)   127.0.0.1:1514->10514/tcp                                                   
harbor-portal       nginx -g daemon off;             Up (healthy)                                                                               
nginx               nginx -g daemon off;             Up (healthy)   0.0.0.0:80->8080/tcp,:::80->8080/tcp, 0.0.0.0:443->8443/tcp,:::443->8443/tcp
redis               redis-server /etc/redis.conf     Up (healthy)                                                                               
registry            /home/harbor/entrypoint.sh       Up (healthy)                                                                               
registryctl         /home/harbor/start.sh            Up (healthy)         

4.6、登录到harbor控制台，并创建一个私有的项目

这里，我使用域名去访问harbor, 就需要手动添加修改hosts文件，添加ip 和 域名的映射关系

windows：   编辑   C:\Windows\System32\drivers\etc\hosts 文件



Linux ：  vim /etc/hosts



访问Harbor： https://harbor.fxkjnj.com/

输入用户名/密码 登录



新建一个私有项目





可以在控制台界面，查看到推送命令：



5、客户端从Harbor上上传，下载镜像
 找一台有docker环境的ubuntu 机器，模拟往harbor上上传，下载镜像

#注意，这里，我使用域名去访问harbor, 就需要手动添加修改hosts文件，添加ip 和 域名的映射关系



root@ubuntu:~# docker version
Client: Docker Engine - Community
 Version:           20.10.8
 API version:       1.40
 Go version:        go1.16.6
 Git commit:        3967b7d
 Built:             Fri Jul 30 19:54:08 2021
 OS/Arch:           linux/amd64
 Context:           default
 Experimental:      true

Server: Docker Engine - Community
 Engine:
  Version:          19.03.15
  API version:      1.40 (minimum version 1.12)
  Go version:       go1.13.15
  Git commit:       99e3ed8919
  Built:            Sat Jan 30 03:15:20 2021
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          1.4.9
  GitCommit:        e25210fe30a0a703442421b0f60afac609f950a3
 runc:
  Version:          1.0.1
  GitCommit:        v1.0.1-0-g4144b63
 docker-init:
  Version:          0.18.0
  GitCommit:        fec3683
5.1、从dockerhub上下载一个nginx:1.20.1 的镜像

root@ubuntu:~# docker pull nginx:1.20.1
1.20.1: Pulling from library/nginx
07aded7c29c6: Already exists 
ccf8c35cea14: Pull complete 
21ed194ca997: Pull complete 
b2329d3f240e: Pull complete 
00c4a11249b0: Pull complete 
547cf440fa42: Pull complete 
Digest: sha256:af635cf83a20ecaf45abc818f54808130da49345d84786d19c074f7fc8de31c7
Status: Downloaded newer image for nginx:1.20.1
docker.io/library/nginx:1.20.1
5.2、给镜像打上标记，以便推送到harhor私有仓库中

root@ubuntu:~# docker tag nginx:1.20.1 harbor.fxkjnj.com/fxkj/nginx:1.20.1
5.3、修改docker 配置文件，添加对私有仓库的信任, insecure-registries

root@ubuntu:~# vim /etc/docker/daemon.json
{
  "registry-mirrors": ["https://u2vzou7d.mirror.aliyuncs.com"],
  "insecure-registries": ["harbor.fxkjnj.com"]
}
5.3、重启docker

systemctl restart docker
5.4、登录到harbor ，并上传镜像

root@ubuntu:~/.docker# docker login harbor.fxkjnj.com
Username: admin
Password: 
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded


#往harbor上传镜像
root@ubuntu:~/.docker# docker push harbor.fxkjnj.com/fxkj/nginx:1.20.1 
The push refers to repository [harbor.fxkjnj.com/fxkj/nginx]
0bdf8da939c8: Pushed 
a77307502458: Pushed 
b4f0619f5d91: Pushed 
2af43e00049a: Pushed 
d926ce0c8121: Pushed 
476baebdfbf7: Pushed 
1.20.1: digest: sha256:00406bbfff1a168789df84ee39bf2267fa61224da7b50badc1f4984513999331 size: 1570
可以看到已经上传成功



5.5、登录到harbor ，下载镜像

PS： 如果我们有几千台docker宿主机需要去访问habor上传，下载镜像，我们难道要一台一台机器登录，然后docker login ? 吗 ，

不，我们只需要在已经登录harbor的一台docker 主机上，拷贝走/root/.docker/config.json  到本机/root/.docker/上，就可以免密去访问habor上传，下载镜像

(1)、 修改docker 配置文件，添加对私有仓库的信任, insecure-registries

root@ubuntu:~# vim /etc/docker/daemon.json
{
  "registry-mirrors": ["https://u2vzou7d.mirror.aliyuncs.com"],
  "insecure-registries": ["harbor.fxkjnj.com"]
}
(2)、重启docker

systemctl restart docker
(3)、拷贝 config.json 文件到本机/root/.docker/下

scp -rp /root/.docker/config.json  k8s-node1:/root/.docker/
(4)、从harbor 上下载镜像下来：

root@k8s-node1:~# docker pull harbor.fxkjnj.com/fxkj/nginx:1.16
1.16: Pulling from fxkj/nginx
54fec2fa59d0: Pull complete 
5546cfc92772: Pull complete 
50f62e3cdaf7: Pull complete 
Digest: sha256:2963fc49cc50883ba9af25f977a9997ff9af06b45c12d968b7985dc1e9254e4b
Status: Downloaded newer image for harbor.fxkjnj.com/fxkj/nginx:1.16
harbor.fxkjnj.com/fxkj/nginx:1.16
6、常见问题
1、在登录harbor 的时候，提示  Error response from daemon: Get https://192.168.31.100/v2/: dial tcp 192.168.30.24:443: connect: connection refused

#解决这个问题其实就在/etc/docker/daemon.json文件下，添加对私有仓库的认证就可以登录了,也就是添加可信任厂库地址

[root@k8s-master ~]# vim /etc/docker/daemon.json
{
        "registry-mirrors": ["http://f1361db2.m.daocloud.io"],
        "insecure-registries": ["harbor.fxkj.com"]
}

#重启docker
[root@k8s-master ~]# systemctl restart docker
2、使用自签证书，登录Harbor 时，无法打开安全页面



只需要 在当前页面 手动 输入   thisisunsafe 即可打开页面

本文参考了以下内容，原文链接已贴

https://www.yisu.com/zixun/154614.html

https://www.cnblogs.com/tianzhendengni/p/14071523.html

实验环境：

软件名称	版本
docker-ce	v19.03.15
docker-compose	v1.29.0
Harbor	v2.3.2
一、Harbor简介


Harbor是由VMware公司开源的企业级的Docker Registry管理项目，拥有更丰富的权限权利和完善的架构设计，适用大规模docker集群部署提供仓库服务。
Harbor 是 CNCF 毕业的项目，可提供合规性、性能和互操作性，帮助跨云原生计算平台（如 Kubernetes 和 Docker）一致且安全地管理工件。
Harbor 提供了 Dcoker Registry 管理界面UI，可基于角色访问控制,镜像复制， AD/LDAP 集成，日志审核等功能，并且完全的支持中文。
二、Harbor 的主要功能
基于角色的访问控制
用户与Docker镜像仓库通过“项目”进行组织管理，一个用户可以对多个镜像仓库在同一命名空间（project）里有不同的权限。

基于镜像的复制策略
镜像可以在多个Registry实例中复制（可以将仓库中的镜像同步到远程的Harbor，类似于MySQL主从同步功能），尤其适合于负载均衡，高可用，混合云和多云的场景。

图形化用户界面
用户可以通过浏览器来浏览，检索当前Docker镜像仓库，管理项目和命名空间。

支持 AD/LDAP
Harbor可以集成企业内部已有的AD/LDAP，用于鉴权认证管理。

镜像删除和垃圾回收
Harbor支持在Web删除镜像，回收无用的镜像，释放磁盘空间。image可以被删除并且回收image占用的空间。

审计管理
所有针对镜像仓库的操作都可以被记录追溯，用于审计管理。

RESTful API
RESTful API 提供给管理员对于Harbor更多的操控, 使得与其它管理软件集成变得更容易。

部署简单
提供在线和离线两种安装工具， 直接使用docker部署，但需要依赖docker官方的 Docker Compose 容器编排工具

支持Helm charts仓库
         支持Helm charts仓库的功能，需要在安装的时候指定下参数，如 ./install.sh --with-chartmuseum


​          

三、Harbor 架构组件


Habor组件相对较多，看下都运行了哪些容器：

root@manager:/opt/harbor# docker-compose ps
      Name                     Command                  State                                          Ports                                    
------------------------------------------------------------------------------------------------------------------------------------------------
harbor-core         /harbor/entrypoint.sh            Up (healthy)                                                                               
harbor-db           /docker-entrypoint.sh 96 13      Up (healthy)                                                                               
harbor-jobservice   /harbor/entrypoint.sh            Up (healthy)                                                                               
harbor-log          /bin/sh -c /usr/local/bin/ ...   Up (healthy)   127.0.0.1:1514->10514/tcp                                                   
harbor-portal       nginx -g daemon off;             Up (healthy)                                                                               
nginx               nginx -g daemon off;             Up (healthy)   0.0.0.0:80->8080/tcp,:::80->8080/tcp, 0.0.0.0:443->8443/tcp,:::443->8443/tcp
redis               redis-server /etc/redis.conf     Up (healthy)                                                                               
registry            /home/harbor/entrypoint.sh       Up (healthy)                                                                               
registryctl         /home/harbor/start.sh            Up (healthy)                      
组件名称	说明	实现方式
Proxy	用于转发用户的请求到registry/ui/token service 的反向代理	nginx：使用nginx官方的镜像进行配置
Registry	镜像的push/pull 命令实现的功能	registry：使用registry官方镜像
Database	保存项目/用户/角色/复制策略等信息到数据库中	harbor-db: Mariadb的官方镜像，用于保存Harbor的数据库信息
Core-service	用户进行镜像操作的界面实现，通过webhook的机制保证镜像状态的变化harbor能够及时了解以便进行日志更新等操作，而项目用户角色则通过token进行镜像的push/pull等操作	harbor-ui 等
Job services	镜像复制，可以在harbor实例之间进行镜像的复制或者同步等操作	harbor-jobservice 
Log collector	负责收集各个镜像的日志信息进行统一管理	harbor-log：日志默认保存在/var/log/harbor
四、Harbor 部署
1、安装docker-ce 
安装docker采用清华镜像源：https://mirrors.tuna.tsinghua.edu.cn/help/docker-ce/

使用阿里云镜像加速

4.1.1、如果你过去安装过 docker，先删掉:

sudo 
apt-get remove docker docker-engine docker.io
4.1.2、首先安装依赖:

sudo apt-get install apt-transport-https ca-certificates curl gnupg2 software-properties-common
4.1.3、根据你的发行版，下面的内容有所不同。你使用的发行版： 

信任 Docker 的 GPG 公钥:

sudo 
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
4.1.4、对于 amd64 架构的计算机，添加软件仓库:

sudo add-apt-repository \
"deb [arch=amd64] https://mirrors.tuna.tsinghua.edu.cn/docker-ce/linux/ubuntu \
$(lsb_release -cs) \
stable"

4.1.5、安装指定版本的Docker-CE:

 Step 1: 查找Docker-CE的版本:

apt-cache madison docker-ce
 Step 1: 安装制定版本： 安装docker-ce 会自动依赖安装上docker-cli [docker 客户端工具]

sudo apt-get install docker-ce=5:19.03.15~3-0~ubuntu-bionic -y
4.1.6、使用阿里云docker 镜像加速

登录到阿里云，选择容器镜像服务，点击镜像加速器





4.1.7、配置镜像加速器

可以通过修改daemon配置文件/etc/docker/daemon.json来使用加速器

sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://u2vzou7d.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
2、安装docker-compose
从GitHub上 下载docker-compose 二进制包： Release 1.29.0 · docker/compose · GitHub

下载  docker-compose-Linux-x86_64 版本



#把下载下来的软件包上传到/root 路径下
root@manager:~# mv docker-compose-Linux-x86_64 /usr/bin/docker-compose 
root@manager:~# chmod +x  /usr/bin/docker-compose

#安装后查看下版本
root@manager:~# docker-compose version
docker-compose version 1.29.0, build 07737305
docker-py version: 5.0.0
CPython version: 3.7.10
OpenSSL version: OpenSSL 1.1.0l  10 Sep 2019
3、创建harbor证书
PS:  公司一般都会有商业的证书，直接拿过来使用即可

如果只是想做一张内网用的电子证书或不想花钱去找个 CA 签署，可以造一张自签 (Self-signed)的电子证书。当然这类电子证书没有任何保证，浏览器遇到这证书会发出警告，甚至不接收这类证书。使用自签名(self-signed)的证书，它的主要目的不是防伪，而是使用户和系统之间能够进行SSL通信，保证密码等个人信息传输时的安全。

#创建证书路径
root@manager:/# mkdir /root/cert
root@manager:~/cert# cd /root/cert/
    

#生成一个.key文件
root@manager:~/cert# openssl genrsa  -out ssl.key 1024


#根据这个key文件生成.csr 证书请求文件
root@manager:~/cert# openssl req -new -key ssl.key -out ssl.csr
Can't load /root/.rnd into RNG
140061599584704:error:2406F079:random number generator:RAND_load_file:Cannot open file:../crypto/rand/randfile.c:88:Filename=/root/.rnd
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [AU]:CN
State or Province Name (full name) [Some-State]:BeiJing
Locality Name (eg, city) []:BeiJing
Organization Name (eg, company) [Internet Widgits Pty Ltd]:fxkjnj
Organizational Unit Name (eg, section) []:fxkjnj
Common Name (e.g. server FQDN or YOUR name) []:harbor.fxkjnj.com
Email Address []:admin@fxkjnj.com

Please enter the following 'extra' attributes
to be sent with your certificate request
A challenge password []:
An optional company name []:

 

#根据这2个文件生成.crt证书文件,10年有效期
root@manager:~/cert# openssl x509 -req -days 3650 -in ssl.csr -signkey ssl.key -out ssl.crt
Signature ok
subject=C = CN, ST = BeiJing, L = Beijing, O = FXKJNJ, OU = FXKJNJ, CN = harbor.fxkjnj.com, emailAddress = admin@fxkjnj.com
Getting Private key


root@manager:~/cert# ls -l
total 12
-rw-r--r-- 1 root root 977 Sep 29 16:01 ssl.crt
-rw-r--r-- 1 root root 708 Sep 29 15:07 ssl.csr
-rw------- 1 root root 887 Sep 29 14:59 ssl.key
4、安装harbor
从GitHub上 下载Harbor的软件包： Release v2.3.2 · goharbor/harbor · GitHub

下载 harbor-offline-installer-v2.3.2.tgz 离线版



4.1、 把下载下来的软件包上传到/root 路径下

root@manager:~# tar -xf harbor-offline-installer-v2.3.2.tgz -C /opt/
root@manager:~# cd /opt/harbor
root@manager:/opt/harbor# ls -l
total 594384
-rw-r--r-- 1 root root      3361 Aug 18 16:51 common.sh
-rw-r--r-- 1 root root 608611132 Aug 18 16:52 harbor.v2.3.2.tar.gz
-rw-r--r-- 1 root root      7840 Aug 18 16:51 harbor.yml.tmpl
-rwxr-xr-x 1 root root      2500 Aug 18 16:51 install.sh
-rw-r--r-- 1 root root     11347 Aug 18 16:51 LICENSE
-rwxr-xr-x 1 root root      1881 Aug 18 16:51 prepare
4.2、 把创建的证书，复制到/opt/harbor 路径下

root@manager:/opt/harbor# cp /root/cert/{ssl.key,ssl.crt}  /opt/harbor/
root@manager:/opt/harbor# ls -l
total 594428
drwxr-xr-x 5 root  root       4096 Sep 29 16:04 ./
drwxr-xr-x 4 root  root       4096 Sep 29 15:12 ../
drwxr-xr-x 3 root  root       4096 Sep 29 15:50 common/
-rw-r--r-- 1 root  root       3361 Aug 18 16:51 common.sh
drwxr-xr-x 9 root  root       4096 Sep 29 15:56 data/
-rw-r--r-- 1 root  root       7058 Sep 29 15:56 docker-compose.yml
-rw-r--r-- 1 root  root  608611132 Aug 18 16:52 harbor.v2.3.2.tar.gz
-rw-r--r-- 1 root  root       7871 Sep 29 15:55 harbor.yml
-rw-r--r-- 1 root  root       7840 Aug 18 16:51 harbor.yml.tmpl
-rwxr-xr-x 1 root  root       2500 Aug 18 16:51 install.sh*
-rw-r--r-- 1 root  root      11347 Aug 18 16:51 LICENSE
drwxr-xr-x 2 10000 10000      4096 Sep 29 15:57 log/
-rwxr-xr-x 1 root  root       1881 Aug 18 16:51 prepare*
-rw-r--r-- 1 root  root        977 Sep 29 16:04 ssl.crt
-rw------- 1 root  root        887 Sep 29 16:04 ssl.key

4.3、 创建harbor配置文件

root@manager:/opt# cd /opt/haror
root@manager:/opt/harbor# cp harbor.yml.tmpl harbor.yml

root@manager:/opt/harbor# vim harbor.yml
#只需要修改成如下内容

.................
#配置Harbor域名访问地址
hostname: harbor.fxkjnj.com
.................
#配置Harbor证书路径
certificate:  /opt/harbor/ssl.crt
private_key:  /opt/harbor/ssl.key
.................
#配置Harbor管理员密码
harbor_admin_password: fxkjnj
.................
#配置Harbor数据存放路径
data_volume: /opt/harbor/data
.................
#配置Harbor日志路径
location: /opt/harbor/log
.................

#保存并退出
4.4、创建harbor数据目录和日志目录

root@manager:/opt# mkdir /opt/harbor/data
root@manager:/opt# mkdir /opt/harbor/log
4.5、执行./install.sh 脚本安装harbor

# 为了后期使用Helm 方便，我们之间安装上helm，添加安装参数：--with-chartmuseum 

root@manager:/opt# cd /opt/harbor
root@manager:/opt/harbor# ./install.sh --with-chartmuseum

[Step 0]: checking if docker is installed ...

Note: docker version: 20.10.8

[Step 1]: checking docker-compose is installed ...

Note: docker-compose version: 1.29.0

[Step 2]: loading Harbor images ...
Loaded image: goharbor/redis-photon:v2.3.2
Loaded image: goharbor/nginx-photon:v2.3.2
Loaded image: goharbor/harbor-portal:v2.3.2
Loaded image: goharbor/trivy-adapter-photon:v2.3.2
Loaded image: goharbor/chartmuseum-photon:v2.3.2
Loaded image: goharbor/notary-signer-photon:v2.3.2
Loaded image: goharbor/harbor-core:v2.3.2
Loaded image: goharbor/harbor-log:v2.3.2
Loaded image: goharbor/harbor-registryctl:v2.3.2
Loaded image: goharbor/harbor-exporter:v2.3.2
Loaded image: goharbor/notary-server-photon:v2.3.2
Loaded image: goharbor/prepare:v2.3.2
Loaded image: goharbor/harbor-db:v2.3.2
Loaded image: goharbor/harbor-jobservice:v2.3.2
Loaded image: goharbor/registry-photon:v2.3.2


[Step 3]: preparing environment ...

[Step 4]: preparing harbor configs ...
prepare base dir is set to /opt/harbor
Generated configuration file: /config/portal/nginx.conf
Generated configuration file: /config/log/logrotate.conf
Generated configuration file: /config/log/rsyslog_docker.conf
Generated configuration file: /config/nginx/nginx.conf
Generated configuration file: /config/core/env
Generated configuration file: /config/core/app.conf
Generated configuration file: /config/registry/config.yml
Generated configuration file: /config/registryctl/env
Generated configuration file: /config/registryctl/config.yml
Generated configuration file: /config/db/env
Generated configuration file: /config/jobservice/env
Generated configuration file: /config/jobservice/config.yml
Generated and saved secret to file: /data/secret/keys/secretkey
Successfully called func: create_root_cert
Generated configuration file: /config/chartserver/env
Generated configuration file: /compose_location/docker-compose.yml
Clean up the input dir


Note: stopping existing Harbor instance ...
Removing harbor-jobservice ... done
Removing nginx             ... done
Removing harbor-core       ... done
Removing registry          ... done
Removing harbor-portal     ... done
Removing redis             ... done
Removing registryctl       ... done
Removing harbor-db         ... done
Removing harbor-log        ... done
Removing network harbor_harbor
Removing network harbor_harbor-chartmuseum
WARNING: Network harbor_harbor-chartmuseum not found.


[Step 5]: starting Harbor ...
Creating network "harbor_harbor" with the default driver
Creating network "harbor_harbor-chartmuseum" with the default driver
Creating harbor-log ... done
Creating redis         ... done
Creating harbor-db     ... done
Creating chartmuseum   ... done
Creating registry      ... done
Creating registryctl   ... done
Creating harbor-portal ... done
Creating harbor-core   ... done
Creating harbor-jobservice ... done
Creating nginx             ... done
✔ ----Harbor has been installed and started successfully.----

 


root@manager:/opt/harbor# docker-compose ps
      Name                     Command                  State                                          Ports                                    
------------------------------------------------------------------------------------------------------------------------------------------------
chartmuseum         ./docker-entrypoint.sh           Up (healthy)                                                                               
harbor-core         /harbor/entrypoint.sh            Up (healthy)                                                                               
harbor-db           /docker-entrypoint.sh 96 13      Up (healthy)                                                                               
harbor-jobservice   /harbor/entrypoint.sh            Up (healthy)                                                                               
harbor-log          /bin/sh -c /usr/local/bin/ ...   Up (healthy)   127.0.0.1:1514->10514/tcp                                                   
harbor-portal       nginx -g daemon off;             Up (healthy)                                                                               
nginx               nginx -g daemon off;             Up (healthy)   0.0.0.0:80->8080/tcp,:::80->8080/tcp, 0.0.0.0:443->8443/tcp,:::443->8443/tcp
redis               redis-server /etc/redis.conf     Up (healthy)                                                                               
registry            /home/harbor/entrypoint.sh       Up (healthy)                                                                               
registryctl         /home/harbor/start.sh            Up (healthy)         

4.6、登录到harbor控制台，并创建一个私有的项目

这里，我使用域名去访问harbor, 就需要手动添加修改hosts文件，添加ip 和 域名的映射关系

windows：   编辑   C:\Windows\System32\drivers\etc\hosts 文件



Linux ：  vim /etc/hosts



访问Harbor： https://harbor.fxkjnj.com/

输入用户名/密码 登录



新建一个私有项目





可以在控制台界面，查看到推送命令：



5、客户端从Harbor上上传，下载镜像
 找一台有docker环境的ubuntu 机器，模拟往harbor上上传，下载镜像

#注意，这里，我使用域名去访问harbor, 就需要手动添加修改hosts文件，添加ip 和 域名的映射关系



root@ubuntu:~# docker version
Client: Docker Engine - Community
 Version:           20.10.8
 API version:       1.40
 Go version:        go1.16.6
 Git commit:        3967b7d
 Built:             Fri Jul 30 19:54:08 2021
 OS/Arch:           linux/amd64
 Context:           default
 Experimental:      true

Server: Docker Engine - Community
 Engine:
  Version:          19.03.15
  API version:      1.40 (minimum version 1.12)
  Go version:       go1.13.15
  Git commit:       99e3ed8919
  Built:            Sat Jan 30 03:15:20 2021
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          1.4.9
  GitCommit:        e25210fe30a0a703442421b0f60afac609f950a3
 runc:
  Version:          1.0.1
  GitCommit:        v1.0.1-0-g4144b63
 docker-init:
  Version:          0.18.0
  GitCommit:        fec3683
5.1、从dockerhub上下载一个nginx:1.20.1 的镜像

root@ubuntu:~# docker pull nginx:1.20.1
1.20.1: Pulling from library/nginx
07aded7c29c6: Already exists 
ccf8c35cea14: Pull complete 
21ed194ca997: Pull complete 
b2329d3f240e: Pull complete 
00c4a11249b0: Pull complete 
547cf440fa42: Pull complete 
Digest: sha256:af635cf83a20ecaf45abc818f54808130da49345d84786d19c074f7fc8de31c7
Status: Downloaded newer image for nginx:1.20.1
docker.io/library/nginx:1.20.1
5.2、给镜像打上标记，以便推送到harhor私有仓库中

root@ubuntu:~# docker tag nginx:1.20.1 harbor.fxkjnj.com/fxkj/nginx:1.20.1
5.3、修改docker 配置文件，添加对私有仓库的信任, insecure-registries

root@ubuntu:~# vim /etc/docker/daemon.json
{
  "registry-mirrors": ["https://u2vzou7d.mirror.aliyuncs.com"],
  "insecure-registries": ["harbor.fxkjnj.com"]
}
5.3、重启docker

systemctl restart docker
5.4、登录到harbor ，并上传镜像

root@ubuntu:~/.docker# docker login harbor.fxkjnj.com
Username: admin
Password: 
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded


#往harbor上传镜像
root@ubuntu:~/.docker# docker push harbor.fxkjnj.com/fxkj/nginx:1.20.1 
The push refers to repository [harbor.fxkjnj.com/fxkj/nginx]
0bdf8da939c8: Pushed 
a77307502458: Pushed 
b4f0619f5d91: Pushed 
2af43e00049a: Pushed 
d926ce0c8121: Pushed 
476baebdfbf7: Pushed 
1.20.1: digest: sha256:00406bbfff1a168789df84ee39bf2267fa61224da7b50badc1f4984513999331 size: 1570
可以看到已经上传成功



5.5、登录到harbor ，下载镜像

PS： 如果我们有几千台docker宿主机需要去访问habor上传，下载镜像，我们难道要一台一台机器登录，然后docker login ? 吗 ，

不，我们只需要在已经登录harbor的一台docker 主机上，拷贝走/root/.docker/config.json  到本机/root/.docker/上，就可以免密去访问habor上传，下载镜像

(1)、 修改docker 配置文件，添加对私有仓库的信任, insecure-registries

root@ubuntu:~# vim /etc/docker/daemon.json
{
  "registry-mirrors": ["https://u2vzou7d.mirror.aliyuncs.com"],
  "insecure-registries": ["harbor.fxkjnj.com"]
}
(2)、重启docker

systemctl restart docker
(3)、拷贝 config.json 文件到本机/root/.docker/下

scp -rp /root/.docker/config.json  k8s-node1:/root/.docker/
(4)、从harbor 上下载镜像下来：

root@k8s-node1:~# docker pull harbor.fxkjnj.com/fxkj/nginx:1.16
1.16: Pulling from fxkj/nginx
54fec2fa59d0: Pull complete 
5546cfc92772: Pull complete 
50f62e3cdaf7: Pull complete 
Digest: sha256:2963fc49cc50883ba9af25f977a9997ff9af06b45c12d968b7985dc1e9254e4b
Status: Downloaded newer image for harbor.fxkjnj.com/fxkj/nginx:1.16
harbor.fxkjnj.com/fxkj/nginx:1.16
6、常见问题
1、在登录harbor 的时候，提示  Error response from daemon: Get https://192.168.31.100/v2/: dial tcp 192.168.30.24:443: connect: connection refused

#解决这个问题其实就在/etc/docker/daemon.json文件下，添加对私有仓库的认证就可以登录了,也就是添加可信任厂库地址

[root@k8s-master ~]# vim /etc/docker/daemon.json
{
        "registry-mirrors": ["http://f1361db2.m.daocloud.io"],
        "insecure-registries": ["harbor.fxkj.com"]
}

#重启docker
[root@k8s-master ~]# systemctl restart docker
2、使用自签证书，登录Harbor 时，无法打开安全页面



只需要 在当前页面 手动 输入   thisisunsafe 即可打开页面

本文参考了以下内容，原文链接已贴

https://www.yisu.com/zixun/154614.html

https://www.cnblogs.com/tianzhendengni/p/14071523.html
