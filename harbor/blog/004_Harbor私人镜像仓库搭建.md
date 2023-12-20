Harbor私人镜像仓库搭建（2.4.2版本）

让我三行代码

于 2022-03-24 15:52:00 发布

2223
 收藏 6
分类专栏： CI/CD 文章标签： devops 云原生 容器
版权

CI/CD
专栏收录该内容
3 篇文章1 订阅
订阅专栏
Harbor介绍
Harbor是一个用于存储和分发Docker镜像的企业级Registry服务器，通过添加一些企业必需的功能特性，例如安全、标识和管理等，扩展了开源Docker Distribution。作为一个企业级私有Registry服务器，Harbor提供了更好的性能和安全。提升用户使用Registry构建和运行环境传输镜像的效率。Harbor支持安装在多个Registry节点的镜像资源复制，镜像全部保存在私有Registry中， 确保数据和知识产权在公司内部网络中管控。另外，Harbor也提供了高级的安全特性，诸如用户管理，访问控制和活动审计等。

官网地址：https://goharbor.io/

Harbor特性
1、基于角色的访问控制 ：用户与Docker镜像仓库通过“项目”进行组织管理，一个用户可以对多个镜像仓库在同一命名空间（project）里有不同的权限。

2、镜像复制 ： 镜像可以在多个Registry实例中复制（同步）。尤其适合于负载均衡，高可用，混合云和多云的场景。

3、图形化用户界面 ： 用户可以通过浏览器来浏览，检索当前Docker镜像仓库，管理项目和命名空间。

4、AD/LDAP 支持 ： Harbor可以集成企业内部已有的AD/LDAP，用于鉴权认证管理。

5、审计管理 ： 所有针对镜像仓库的操作都可以被记录追溯，用于审计管理。

6、国际化 ： 已拥有英文、中文、德文、日文和俄文的本地化版本。更多的语言将会添加进来。

7、RESTful API ： RESTful API 提供给管理员对于Harbor更多的操控, 使得与其它管理软件集成变得更容易。

8、部署简单 ： 提供在线和离线两种安装工具， 也可以安装到vSphere平台(OVA方式)虚拟设备。

Harbor组件
Harbor在架构上主要由6个组件构成：
1、harbor-adminserver：harbor系统管理接口，可以修改系统配置以及获取系统信息
2、harbor-db：存储项目的元数据、用户、规则、复制策略等信息
3、harbor-jobservice：harbor里面主要是为了镜像仓库之前同步使用的
4、harbor-log：收集其他harbor的日志信息。rsyslogd
5、harbor-ui：一个用户界面模块，用来管理registry。主要是前端的页面和后端CURD的接口
6、nginx：harbor的一个反向代理组件，代理registry、ui、token等服务。这个代理会转发harbor web和docker client的各种请求到后端服务上。是个nginx。nginx负责流量转发和安全验证，对外提供的流量都是从nginx中转，它将流量分发到后端的ui和正在docker镜像存储的docker registry
7、registry：存储docker images的服务，并且提供pull/push服务。harbor需要对image的访问进行访问控制，当client每次进行pull、push的时候，registry都需要client去token服务获取一个可用的token。
8、redis：存储缓存信息
9、webhook：当registry中的image状态发生变化的时候去记录更新日志、复制等操作。

Harbor的实现
Harbor各个组件由Docker容器的形式构建，官方也是使用的Docker Compose来进行编排部署，用于部署Harbor的Docker Compose模板位于 harbor/docker-compose.yml。本版本（比较新）由9个组件组成：

[root@node3 harbor]# docker-compose ps
      Name                     Command                  State                         Ports                   
--------------------------------------------------------------------------------------------------------------
harbor-core         /harbor/entrypoint.sh            Up (healthy)                                             
harbor-db           /docker-entrypoint.sh 96 13      Up (healthy)                                             
harbor-jobservice   /harbor/entrypoint.sh            Up (healthy)                                             
harbor-log          /bin/sh -c /usr/local/bin/ ...   Up (healthy)   127.0.0.1:1514->10514/tcp                 
harbor-portal       nginx -g daemon off;             Up (healthy)                                             
nginx               nginx -g daemon off;             Up (healthy)   0.0.0.0:20080->8080/tcp,:::20080->8080/tcp
redis               redis-server /etc/redis.conf     Up (healthy)                                             
registry            /home/harbor/entrypoint.sh       Up (healthy)                                             
registryctl         /home/harbor/start.sh            Up (healthy)      
1
2
3
4
5
6
7
8
9
10
11
12
这几个容器通过Docker link的形式连接在一起，这样，在容器之间可以通过容器名字互相访问。对终端用户而言，只需要暴露proxy （即Nginx）的服务端口。

安装并配置Harbor
1、先看看我的实验环境

[root@node3 harbor]# uname -a
Linux node3 3.10.0-862.el7.x86_64 #1 SMP Fri Apr 20 16:44:24 UTC 2018 x86_64 x86_64 x86_64 GNU/Linux
[root@node3 harbor]# cat /etc/redhat-release 
CentOS Linux release 7.5.1804 (Core) 
#本机IP：192.168.9.132
1
2
3
4
5
2、安装Docker
使用阿里云的Docker的Yum源，这里放个网址，不再演示，有需要自己复制粘贴即可。
https://developer.aliyun.com/mirror/docker-ce?spm=a2c6h.13651102.0.0.3e221b11g0gJxB

yum install -y docker-ce
systemctl restart docker
systemctl enable docker
#查看docker的版本
[root@node3 ~]# docker --version
Docker version 20.10.13, build a224086
1
2
3
4
5
6
3、安装docker-compose
官方网址：https://docs.docker.com/compose/install/。
其实就一行命令，粘贴复制即可：

#直接运行此命令，它会下载docker-compose这个可执行文件，默认在/usr/local/bin/
curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
#授予可执行权限
chmod +x /usr/local/bin/docker-compose
#查看docker-compose版本
[root@node3 ~]# docker-compose --version
docker-compose version 1.29.2, build 5becea4c
1
2
3
4
5
6
7
安装Harbor
下载地址：https://github.com/goharbor/harbor/releases
这里我们下载在线安装，并且是最新版本：2.4.2版本

下载本地，通过xshell或者CRT传到Linux中。

1、解压Harbor到root目录下

tar xzvf harbor-online-installer-v2.4.2.tgz
1
2、配置Harbor，进入harbor解压目录根据harbor配置模板文件拷贝一份harbor配置文件

cd harbor
cp harbor.yml.tmpl harbor.yml
1
2
3、修改harbor.yml文件

vi harbor.yml
...
hostname: 192.168.9.132                #Harbor所在的服务器地址，可以写域名和IP
port: 20080                            #设置Harbor监听的端口
harbor_admin_password: admin           #设置登陆Harbor的密码
data_volume: /root/bo                  #设置Harbor的数据存放目录，需要提前自己创建此目录
location: /var/log/harbor              #设置Harbor日志存放目录，需要提前自己创建
...
1
2
3
4
5
6
7
8
现在的docker都需要https协议去拉取镜像，配置Harbor的https连接放到文章末尾讲解，这里我们先把https注释掉。如果想使用https取登陆Harbor需要认证证书，这里我们先把https注释掉，下文介绍Harbor的自签证书。
注释还是编辑harbor.yml，注释Https内容：

4、安装Harbor
进入到harbor目录
执行以下命令后，会开始下载镜像，并生成docker-compose.yml文件。

./prepare
1
执行 ./install.sh 脚本进行安装

./install.sh
1
执行以上命令后，会开始进行所需要镜像的下载，并进行安装（可能会比较慢）安装成功，并已成功启动harbor,如下图所示

使用docker-compose ps 命令可以查看容器启动情况

[root@node3 harbor]# docker-compose ps
      Name                     Command                  State                         Ports                   
--------------------------------------------------------------------------------------------------------------
harbor-core         /harbor/entrypoint.sh            Up (healthy)                                             
harbor-db           /docker-entrypoint.sh 96 13      Up (healthy)                                             
harbor-jobservice   /harbor/entrypoint.sh            Up (healthy)                                             
harbor-log          /bin/sh -c /usr/local/bin/ ...   Up (healthy)   127.0.0.1:1514->10514/tcp                 
harbor-portal       nginx -g daemon off;             Up (healthy)                                             
nginx               nginx -g daemon off;             Up (healthy)   0.0.0.0:20080->8080/tcp,:::20080->8080/tcp
redis               redis-server /etc/redis.conf     Up (healthy)                                             
registry            /home/harbor/entrypoint.sh       Up (healthy)                                             
registryctl         /home/harbor/start.sh            Up (healthy)   
#后续对上面众多容器的操作，都必须在harbor这个目录下进行，比如：
#docker-compose restart     重启容器，
#docker-compose down        删除全部容器
#docker-compose up -d       启动容器...
#众多命令参考：docker-compose --help
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
5、登陆Harbor
在浏览器中输入：IP:20080
用户/密码：admin/自己设置的密码

之后就进入到Harbor的主页面了：


使用命令行登陆Harbor，并上传镜像和下载镜像
1、先配置docker registry，我们需要在docker的运行文件中配置harbor的地址

[root@node3 ~]# vi /etc/docker/daemon.json        #注意这个文件，需要在哪台服务器上登陆Harbor，那么此内容就需要写在那台服务器上。
{
  "registry-mirrors": ["https://pve03e8m.mirror.aliyuncs.com"],       #这个是使用阿里云的加速器
  "insecure-registries": ["192.168.9.132:20080"]                      #我们需要添加这一行，ip:20080需要换成自己的
}
1
2
3
4
5
重启docker

systemctl daemon-reload
systemctl restart docker
1
2
2、命令行登陆

[root@node2 ~]# docker login http://192.168.9.132:20080
Username: admin
Password: 
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded           #看见"Login Succeeded"表示登陆成功。
1
2
3
4
5
6
7
8
3、push，和pull镜像到harbor
（1）首先我们先从互联网中拉取个nginx镜像

docker pull nginx
1
（2）将原nginx镜像改标签

#改标签
docker tag nginx:latest 192.168.9.132:20080/library/nginx:2.5
#查看镜像
docker images
REPOSITORY                          TAG       IMAGE ID       CREATED        SIZE
goharbor/redis-photon               v2.4.2    61d136910774   8 days ago     158MB
goharbor/harbor-registryctl         v2.4.2    f43545bdfd12   8 days ago     138MB
goharbor/registry-photon            v2.4.2    1927be8b8775   8 days ago     80.8MB
goharbor/nginx-photon               v2.4.2    4189bfe82749   8 days ago     47.3MB
goharbor/harbor-log                 v2.4.2    b2279d3a2ba5   8 days ago     162MB
goharbor/harbor-jobservice          v2.4.2    d22f0a749835   8 days ago     222MB
goharbor/harbor-core                v2.4.2    672a56385d29   8 days ago     199MB
goharbor/harbor-portal              v2.4.2    bc60d9eaf4ad   8 days ago     56.3MB
goharbor/harbor-db                  v2.4.2    91d13ec46b2c   8 days ago     226MB
goharbor/prepare                    v2.4.2    d2100ed70ba4   8 days ago     269MB
192.168.9.132:20080/library/nginx   2.5       605c77e624dd   2 months ago   141MB        #可以发现已经改tag成功了
nginx                               latest    605c77e624dd   2 months ago   141MB
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
（3）将192.168.9.132:20080/library/nginx:2.5镜像push到harbor仓库中去：

[root@node3 ~]# docker push 192.168.9.132:20080/library/nginx:2.5         #由于我已经push过了，所以显示已存在
The push refers to repository [192.168.9.132:20080/library/nginx]
d874fd2bc83b: Layer already exists 
32ce5f6a5106: Layer already exists 
f1db227348d0: Layer already exists 
b8d6e692a25e: Layer already exists 
e379e8aedd4d: Layer already exists 
2edcec3590a4: Layer already exists 
2.5: digest: sha256:ee89b00528ff4f02f2405e4ee221743ebc3f8e8dd0bfd5c4c20a2fa2aaa7ede3 size: 1570
1
2
3
4
5
6
7
8
9
（4）在Web界面查看，点击"library"仓库，这个仓库是默认的，由于我们没有创建其它的镜像仓库，所以我们使用默认的"library"

(5)从harbor中pull"192.168.9.132:20080/library/nginx:2.5"镜像

#先把原来的删除
[root@node3 ~]# docker rmi 192.168.9.132:20080/library/nginx:2.5
[root@node3 ~]# docker pull 192.168.9.132:20080/library/nginx:2.5           #在重新拉取
2.5: Pulling from library/nginx
Digest: sha256:ee89b00528ff4f02f2405e4ee221743ebc3f8e8dd0bfd5c4c20a2fa2aaa7ede3
Status: Downloaded newer image for 192.168.9.132:20080/library/nginx:2.5
192.168.9.132:20080/library/nginx:2.5
#可以再次查看镜像，可以看到已经拉取成功
[root@node3 ~]# docker images
REPOSITORY                          TAG       IMAGE ID       CREATED        SIZE
goharbor/redis-photon               v2.4.2    61d136910774   8 days ago     158MB
goharbor/harbor-registryctl         v2.4.2    f43545bdfd12   8 days ago     138MB
goharbor/registry-photon            v2.4.2    1927be8b8775   8 days ago     80.8MB
goharbor/nginx-photon               v2.4.2    4189bfe82749   8 days ago     47.3MB
goharbor/harbor-log                 v2.4.2    b2279d3a2ba5   8 days ago     162MB
goharbor/harbor-jobservice          v2.4.2    d22f0a749835   8 days ago     222MB
goharbor/harbor-core                v2.4.2    672a56385d29   8 days ago     199MB
goharbor/harbor-portal              v2.4.2    bc60d9eaf4ad   8 days ago     56.3MB
goharbor/harbor-db                  v2.4.2    91d13ec46b2c   9 days ago     226MB
goharbor/prepare                    v2.4.2    d2100ed70ba4   9 days ago     269MB
nginx                               latest    605c77e624dd   2 months ago   141MB
192.168.9.132:20080/library/nginx   2.5       605c77e624dd   2 months ago   141MB
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
配置Harbor的HTTPS访问
我们知道Https相较于Http更安全，而且目前docker已经采用https的连接去拉取镜像，因此我们还需要给Harbor颁发自签证书，在实验和开发环境我们自己签发的ca证书就能满足需要。从而配置Harbor的https连接访问。

环境说明：由于我是虚拟机，并且没有域名，因此我们通过IP去自签CA证书。
自签证书的Harbor的官网地址：
https://goharbor.io/docs/2.4.0/install-config/configure-https/

我是以官方文档为参考而进行的配置：

官方是以OpenSSL创建的CA，我按照官方的来。
在生产环境中，应从 CA 获取证书。在测试或开发环境中，可以生成自己的 CA。若要生成 CA 证书，请运行以下命令：

生成证书颁发机构证书
1、生成 CA 证书私钥：

openssl genrsa -out ca.key 4096
1
2、生成 CA 证书：

openssl req -x509 -new -nodes -sha512 -days 3650 \
 -subj "/C=CN/ST=Beijing/L=Beijing/O=example/OU=Personal/CN=192.168.9.132" \
 -key ca.key \
 -out ca.crt
1
2
3
4
3、查看CA密钥和证书

[root@node3 ~]# ll
-rw-r--r--. 1 root root  2033 Mar 23 23:43 ca.crt
-rw-r--r--. 1 root root  3243 Mar 23 23:42 ca.key
1
2
3
生成服务器证书
1、生成 CA 证书私钥：

openssl genrsa -out ca.key 4096
1
2、生成 CA 证书：

openssl req -sha512 -new \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=example/OU=Personal/CN=192.168.9.132" \
    -key yourdomain.com.key \
    -out yourdomain.com.csr
1
2
3
4
3、生成 x509 v3 扩展文件：

cat > v3.ext <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = IP:192.168.9.132
1
2
3
4
5
6
4、使用v3.ext文件为 Harbor 主机生成证书：

openssl x509 -req -sha512 -days 3650 \
    -extfile v3.ext \
    -CA ca.crt -CAkey ca.key -CAcreateserial \
    -in 192.168.9.132.csr \
    -out 192.168.9.132.crt
1
2
3
4
5
向Harbor和Docker提供证书
1、向Harbor提供证书。编辑harbor.ml文件，将https注释取消（默认是没有取消的，在上面我做实验先注释掉了）

https:
  # https port for harbor, default is 443
  port: 443
  # The path of cert and key files for nginx
  certificate: /root/192.168.9.132.crt                 #填写crt文件的位置，这里我是直接放在root目录下的
  private_key: /root/192.168.9.132.key                 #填写key文件的位置，这里我是直接放在root目录下的
1
2
3
4
5
6
2、为Docker提供证书
（1）使Docker 守护程序将文件解释为 CA 证书，将文件解释为客户端证书：

openssl x509 -inform PEM -in 192.168.9.132.crt -out 192.168.9.132.cert
1
（2）将服务器证书、密钥和 CA 文件复制到 Harbor 主机上的 Docker 证书文件夹中。您必须先创建相应的文件夹：

[root@node3 ~]# cp 192.168.9.132.cert /etc/docker/certs.d/192.168.9.132/
[root@node3 ~]# cp 192.168.9.132.key /etc/docker/certs.d/192.168.9.132/
[root@node3 ~]# cp ca.crt /etc/docker/certs.d/192.168.9.132/
1
2
3
这里端口就是用户默认的443端口

（3）重新启动 Docker 引擎：

systemctl restart docker
1
部署或重新配置Harbor
（1）如果还未安装，就直接运行：

./install.sh 
1
（2）如果已使用 HTTP 部署了 Harbor，并希望将其重新配置为使用 HTTPS，请执行以下步骤：

#运行脚本以启用 HTTPS：
./prepare

#请停止并删除现有实例：
docker-compose down -v

#重新启动Harbor：
docker-compose up -d
1
2
3
4
5
6
7
8
验证HTTPS连接
在浏览器中输入：
https://192.168.9.132


最后点击"高级"，就ok了。
————————————————
版权声明：本文为CSDN博主「让我三行代码」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/m0_57776598/article/details/123698967