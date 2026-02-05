# Harbor v2.4.3部署（支持https）



> Harbor介绍
> Harbor是一个用于存储和分发Docker镜像的企业级Registry服务器，通过添加一些企业必需的功能特性，例如安全、标识和管理等，扩展了开源Docker Distribution。作为一个企业级私有Registry服务器，Harbor提供了更好的性能和安全。提升用户使用Registry构建和运行环境传输镜像的效率。Harbor支持安装在多个Registry节点的镜像资源复制，镜像全部保存在私有Registry中， 确保数据和知识产权在公司内部网络中管控。另外，Harbor也提供了高级的安全特性，诸如用户管理，访问控制和活动审计等。
>
> 官网地址：[https://goharbor.io/](https://links.jianshu.com/go?to=https://goharbor.io/)
>
> Harbor特性
> 1、基于角色的访问控制 ：用户与Docker镜像仓库通过“项目”进行组织管理，一个用户可以对多个镜像仓库在同一命名空间（project）里有不同的权限。
>
> 2、镜像复制 ： 镜像可以在多个Registry实例中复制（同步）。尤其适合于负载均衡，高可用，混合云和多云的场景。
>
> 3、图形化用户界面 ： 用户可以通过浏览器来浏览，检索当前Docker镜像仓库，管理项目和命名空间。
>
> 4、AD/LDAP 支持 ： Harbor可以集成企业内部已有的AD/LDAP，用于鉴权认证管理。
>
> 5、审计管理 ： 所有针对镜像仓库的操作都可以被记录追溯，用于审计管理。
>
> 6、国际化 ： 已拥有英文、中文、德文、日文和俄文的本地化版本。更多的语言将会添加进来。
>
> 7、RESTful API ： RESTful API 提供给管理员对于Harbor更多的操控, 使得与其它管理软件集成变得更容易。
>
> 8、部署简单 ： 提供在线和离线两种安装工具， 也可以安装到vSphere平台(OVA方式)虚拟设备。
>
> Harbor组件
> Harbor在架构上主要由6个组件构成：
> 1、harbor-adminserver：harbor系统管理接口，可以修改系统配置以及获取系统信息
> 2、harbor-db：存储项目的元数据、用户、规则、复制策略等信息
> 3、harbor-jobservice：harbor里面主要是为了镜像仓库之前同步使用的
> 4、harbor-log：收集其他harbor的日志信息。rsyslogd
> 5、harbor-ui：一个用户界面模块，用来管理registry。主要是前端的页面和后端CURD的接口
> 6、nginx：harbor的一个反向代理组件，代理registry、ui、token等服务。这个代理会转发harbor web和docker client的各种请求到后端服务上。是个nginx。nginx负责流量转发和安全验证，对外提供的流量都是从nginx中转，它将流量分发到后端的ui和正在docker镜像存储的docker registry
> 7、registry：存储docker images的服务，并且提供pull/push服务。harbor需要对image的访问进行访问控制，当client每次进行pull、push的时候，registry都需要client去token服务获取一个可用的token。
> 8、redis：存储缓存信息
> 9、webhook：当registry中的image状态发生变化的时候去记录更新日志、复制等操作。

> Harbor的实现
> Harbor各个组件由Docker容器的形式构建，官方也是使用的Docker Compose来进行编排部署，用于部署Harbor的Docker Compose模板位于 harbor/docker-compose.yml。本版本（比较新）由9个组件组成：

```bash
[root@CentOS8 harbor]# docker-compose ps
      Name                     Command                  State                           Ports
------------------------------------------------------------------------------------------------------------------
harbor-core         /harbor/entrypoint.sh            Up (healthy)
harbor-db           /docker-entrypoint.sh 96 13      Up (healthy)
harbor-jobservice   /harbor/entrypoint.sh            Up (healthy)
harbor-log          /bin/sh -c /usr/local/bin/ ...   Up (healthy)   127.0.0.1:1514->10514/tcp
harbor-portal       nginx -g daemon off;             Up (healthy)
nginx               nginx -g daemon off;             Up (healthy)   0.0.0.0:8088->8080/tcp, 0.0.0.0:8443->8443/tcp
redis               redis-server /etc/redis.conf     Up (healthy)
registry            /home/harbor/entrypoint.sh       Up (healthy)
registryctl         /home/harbor/start.sh            Up (healthy)

#这几个容器通过Docker link的形式连接在一起，这样，在容器之间可以通过容器名字互相访问。对终端用户而言，只需要暴露proxy （即Nginx）的服务端口。
```

# 安装

## docker安装



```csharp
#docker安装
# Ubuntu
#更新 apt 包索引
apt-get update
#使用HTTPS获取仓库
apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
#添加Docker官方GPG密钥
curl -fsSL https://mirrors.ustc.edu.cn/docker-ce/linux/ubuntu/gpg | sudo apt-key add -
#验证密钥（此步骤可省略）
apt-key fingerprint 0EBFCD88
#设置稳定版仓库
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
#安装最新版本Docker引擎（这一步比较慢）
apt-get install docker-ce docker-ce-cli containerd.io -y

#centos
yum install -y yum-utils device-mapper-persistent-data lvm2 libseccomp-devel

#添加软件源信息：
sudo yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
#更新 yum 缓存：
sudo yum makecache fast
#安装 Docker-ce：
sudo yum -y install docker-ce
#启动 Docker 后台服务
sudo systemctl start docker


#卸载Docker
#Ubuntu
#卸载Docker引擎、CLI和Containerd包
apt-get purge docker-ce docker-ce-cli containerd.io
#删除镜像，容器和数据卷

rm -rf /var/lib/docker
rm -rf /var/lib/containerd

#centos

#杀死所有运行容器
  docker kill $(docker ps -a -q)

#删除所有容器
  docker rm $(docker ps -a -q)

#删除所有镜像
  docker rmi $(docker images -q)

#停止 docker 服务
  systemctl stop docker

#删除存储目录
  rm -rf /etc/docker
  rm -rf /run/docker
  rm -rf /var/lib/dockershim
  rm -rf /var/lib/docker

#如果发现删除不掉，需要先 umount，如umount /var/lib/docker/devicemapper

#卸载 docker
#查看已安装的 docker 包
  yum list installed | grep docker

#卸载相关包
  sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
```

## docker-compose安装



```ruby
apt-get install python3-pip
pip3 install docker-compose

#或者
curl -SL https://github.com/docker/compose/releases/download/v2.7.0/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

#查看docker-compose版本
[root@node3 ~]# docker-compose --version
```

# Harbor正式安装



```ruby
#下载地址：https://github.com/goharbor/harbor/releases

[root@CentOS8 harbor]# tar xf harbor-offline-installer-v2.4.3.tgz -C /usr/local/src/

#生成证书颁发CA证书
[root@CentOS8 harbor]# cd /usr/local/src/harbor && mkdir certs

[root@CentOS8 harbor]# cd certs/
#生成 CA 证书私钥。
/usr/local/src/harbor/certs ]# openssl genrsa -out ca.key 4096

#生成 CA 证书。

#-days 3650 指定证书有效时间
#-subj 指定ca签发信息 最重要的是CN=harbor.urbancabin.local 必须是要包含域名信息

/usr/local/src/harbor/certs ]# openssl req -x509 -new -nodes -sha512 -days 3650 \
 -subj "/C=CN/ST=Beijing/L=Beijing/O=example/OU=Personal/CN=harbor.urbancabin.local" \
 -key ca.key \
 -out ca.crt

#生成harbor服务器证书
#生成私钥。

/usr/local/src/harbor/certs ]# openssl genrsa -out harbor.urbancabin.key 4096
#生成证书签名请求 （CSR）

#调整选项中的值以反映您的组织。如果使用 FQDN 连接 Harbor 主机，则必须将其指定为公用名 （） 属性，并在键和 CSR 文件名中使用它。-subj CN

/usr/local/src/harbor/certs ]# openssl req -sha512 -new \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=example/OU=Personal/CN=harbor.urbancabin.local" \
    -key harbor.urbancabin.key \
    -out  harbor.urbancabin.csr
#生成 x509 v3 扩展名文件。

#无论使用 FQDN 还是 IP 地址连接到 Harbor 主机，都必须创建此文件，以便为 Harbor 主机生成符合使用者备用名称 （SAN） 和 x509 v3 扩展要求的证书。替换条目以反映您的域。DNS

/usr/local/src/harbor/certs ]# cat > v3.ext <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]   #alt_names必须要包含harbor服务的域名信息
DNS.1=harbor.urbancabin.local
DNS.2=harbor.urbancabin.local.cn
DNS.3=harbor.urbancabin
EOF
#使用该v3.ext文件为您的 Harbor 主机生成证书

/usr/local/src/harbor/certs ]# openssl x509 -req -sha512 -days 3650 \
    -extfile v3.ext \
    -CA ca.crt -CAkey ca.key -CAcreateserial \
    -in harbor.urbancabin.csr \
    -out harbor.urbancabin.crt
#修改harbor.cfg
root@harbor:/usr/local/src/harbor# cat harbor.yml
# Configuration file of Harbor

# The IP address or hostname to access admin UI and registry service.
# DO NOT use localhost or 127.0.0.1, because Harbor needs to be accessed by external clients.
hostname: harbor.urbancabin.local   #证书签署的域名

# http related config
http:
  # port for http, default is 80. If https enabled, this port will redirect to https port
  port: 80

# https related config
https:
  # https port for harbor, default is 443
  port: 443
  # The path of cert and key files for nginx
  certificate: /usr/local/src/harbor/certs/harbor.urbancabin.crt  #指定证书文件
  private_key: /usr/local/src/harbor/certs/harbor.urbancabin.key  #指定私钥文件
#安装harbor
/usr/local/src/harbor# ./prepare   #生成docker-compose.yml
/usr/local/src/harbor# ./install.sh --with-trivy


#检擦服务是否正常
[root@CentOS8 harbor]# docker-compose -f docker-compose.yml ps
      Name                     Command                  State                           Ports
------------------------------------------------------------------------------------------------------------------
harbor-core         /harbor/entrypoint.sh            Up (healthy)
harbor-db           /docker-entrypoint.sh 96 13      Up (healthy)
harbor-jobservice   /harbor/entrypoint.sh            Up (healthy)
harbor-log          /bin/sh -c /usr/local/bin/ ...   Up (healthy)   127.0.0.1:1514->10514/tcp
harbor-portal       nginx -g daemon off;             Up (healthy)
nginx               nginx -g daemon off;             Up (healthy)   0.0.0.0:8088->8080/tcp, 0.0.0.0:8443->8443/tcp
redis               redis-server /etc/redis.conf     Up (healthy)
registry            /home/harbor/entrypoint.sh       Up (healthy)
registryctl         /home/harbor/start.sh            Up (healthy)
```

# 客户端push镜像测试



```csharp
[root@CentOS8 harbor]# docker login harbor.urbancabin.local
Authenticating with existing credentials...
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded


#错误日志
#新版本harbor必须要将harbor证书拷贝至客户端才能正常pull镜像
[root@CentOS8 harbor]# docker  pull docker pull harbor.urbancabin.local/library/nginx:1.23.1-amd64
Error response from daemon: Get "https://harbor.urbancabin.local/v2/": x509: certificate signed by unknown authority
#1、docker客户端
docker 客户端，需要将harbor证书同步过来，且必须要创建一个/etc/docker/certs.d/<yourdomain.com>
[root@CentOS8 harbor]# mkdir -p /etc/docker/certs.d/harbor.urbancabin.local
#2、同步harbor证书
[root@CentOS8 harbor]# scp /usr/local/src/harbor/certs/harbor.urbancabin.crt  DockerClientHost:/etc/docker/certs.d/yourdomain.com
harbor# scp /usr/local/src/harbor/certs/harbor.urbancabin.crt  /etc/docker/certs.d/harbor.urbancabin.local
#3、下载镜像测试
[root@CentOS8 harbor]# docker pull harbor.urbancabin.local/library/nginx:1.23.1-amd64
1.23.1-amd64: Pulling from library/nginx
Digest: sha256:186c79dc14ab93e43d315143ee4b0774506dc4fd952388c20e35d3d37058ab8d
Status: Image is up to date for harbor.urbancabin.local/library/nginx:1.23.1-amd64
harbor.urbancabin.local/library/nginx:1.23.1-amd64
#4、containerd客户端
#配置文件配置
[root@CentOS8 harbor]# vim /etc/containerd/config.toml

 [plugins."io.containerd.grpc.v1.cri".registry.mirrors] #在这一行下面添加如下内容
 [plugins."io.containerd.grpc.v1.cri".registry.mirrors."harbor.urbancabin.local"]   ## 意思是harbor服务器是myharbor.com
  endpoint = ["https://harbor.urbancabin.local"]
 [plugins."io.containerd.grpc.v1.cri".registry.configs."harbor.urbancabin.local".tls]  #跳过证书校验
  insecure_skip_verify = true
 [plugins."io.containerd.grpc.v1.cri".registry.configs."harbor.urbancabin.local".auth]  #使用账号密码登入
  username = "admin"
  password = "Harbor12345"
2、测试下载镜像
[root@CentOS8 harbor]# crictl pull harbor.urbancabin.local/library/nginx:1.23.1-amd64
Image is up to date for sha256:670dcc86b69df89a9d5a9e1a7ae5b8f67619c1c74e19de8a35f57d6c06505fd4
```

# Tips



```csharp
#重启Harbor：

[root@CentOS8 harbor]# cd /usr/local/harbor
[root@CentOS8 harbor]# docker-compose down
[root@CentOS8 harbor]# ./prepare   #配置文件有改动时需要执行
[root@CentOS8 harbor]# docker-compose up -d

#关闭Harbor
[root@CentOS8 harbor]# cd /usr/local/harbor
[root@CentOS8 harbor]# docker-compose down -v
# 或者直接
[root@CentOS8 harbor]# docker-compose stop

#卸载Harbor
[root@CentOS8 harbor]# cd /usr/local/harbor
[root@CentOS8 harbor]# docker-compose stop
[root@CentOS8 harbor]# rm -rf /usr/local/harbor
```

> 参考链接：[https://blog.csdn.net/m0_57776598/article/details/123698967](https://links.jianshu.com/go?to=https://blog.csdn.net/m0_57776598/article/details/123698967)

最后编辑于 ：2022.08.23 10:56:23