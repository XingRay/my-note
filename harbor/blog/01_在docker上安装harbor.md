## Harbor部署



https://goharbor.io/docs/2.8.0/install-config/demo-server/

https://goharbor.io/docs/2.8.0/install-config/





# 1. harbor

无论是使用Docker-distribution去自建仓库，还是通过官方镜像跑容器的方式去自建仓库，通过前面的演示我们可以发现其是非常的简陋的，还不如直接使用官方的Docker Hub去管理镜像来得方便，至少官方的Docker Hub能够通过web界面来管理镜像，还能在web界面执行搜索，还能基于Dockerfile利用Webhooks和Automated Builds实现自动构建镜像的功能，用户不需要在本地执行docker build，而是把所有build上下文的文件作为一个仓库推送到github上，让Docker Hub可以从github上去pull这些文件来完成自动构建。但无论官方的Docker Hub有多强大，它毕竟是在国外，所以速度是最大的瓶颈，我们很多时候是不可能去考虑使用官方的仓库的，但是上面说的两种自建仓库方式又十分简陋，不便管理，所以后来就出现了一个被 CNCF 组织青睐的项目，其名为Harbor。

# 2. harbor简介

Harbor是由VMWare在Docker Registry的基础之上进行了二次封装，加进去了很多额外程序，而且提供了一个非常漂亮的web界面。

Project Harbor是一个开源的可信云本地注册项目，用于存储、标记和扫描上下文。
Harbor扩展了开源Docker分发版，增加了用户通常需要的功能，如安全、身份和管理。
Harbor支持高级特性，如用户管理、访问控制、活动监视和实例之间的复制。

# harbor的功能

功能：

- 多租户内容签名和验证
- 安全性和脆弱性分析
- 审计日志记录
- 身份集成和基于角色的访问控制
- 实例之间的映像复制
- 可扩展的API和图形用户界面
- 国际化(目前为中英文)

# 3. docker compose

Harbor在物理机上部署是非常难的，而为了简化Harbor的应用，Harbor官方直接把Harbor做成了在容器中运行的应用，而且这个容器在Harbor中依赖类似redis、mysql、pgsql等很多存储系统，所以它需要编排很多容器协同起来工作，因此VMWare Harbor在部署和使用时，需要借助于Docker的单机编排工具(Docker compose)来实现。

Compose是一个用于定义和运行多容器Docker应用程序的工具。使用Compose，您可以使用一个YAML文件来配置应用程序的服务。然后，使用一个命令创建并启动配置中的所有服务。

# 4. harbor部署

```bash
[root@slave ~]# dnf list all|grep docker-compose
docker-compose-plugin.x86_64                                      2.6.0-3.el8                                                docker-ce-stable 
[root@slave ~]# 
[root@slave ~]# hostnamectl set-hostname client
[root@slave ~]# bash
[root@client ~]# cd /etc/yum.repos.d/
[root@client yum.repos.d]# ls
CentOS-Stream-AppStream.repo         CentOS-Stream-RealTime.repo
CentOS-Stream-BaseOS.repo            docker-ce.repo
CentOS-Stream-Debuginfo.repo         epel-modular.repo
CentOS-Stream-Extras.repo            epel-playground.repo
CentOS-Stream-HighAvailability.repo  epel-testing-modular.repo
CentOS-Stream-Media.repo             epel-testing.repo
CentOS-Stream-PowerTools.repo        epel.repo
[root@client yum.repos.d]# scp docker-ce.repo 192.168.29.140:/etc/yum.repos.d/
The authenticity of host '192.168.29.140 (192.168.29.140)' can't be established.
ECDSA key fingerprint is SHA256:tGhqSo3u9wWuDd+55Xaw4UgDdkolb4FOhqKtT398KTA.
Are you sure you want to continue connecting (yes/no/[fingerprint])? yes
Warning: Permanently added '192.168.29.140' (ECDSA) to the list of known hosts.
root@192.168.29.140's password: 
docker-ce.repo                         100% 2261     2.8MB/s   00:00    
[root@client yum.repos.d]# 
打开另一台主机192.168.29.140
[root@master ~]# hostnamectl set-hostname harbor
[root@master ~]# bash
[root@harbor ~]# cd /etc/yum.repos.d/
[root@harbor yum.repos.d]# ls
CentOS-Base.repo  epel-modular.repo          epel-testing.repo
docker-ce.repo    epel-testing-modular.repo  epel.repo
[root@harbor yum.repos.d]# dnf -y install docker-ce
......
  python3-setools-4.3.0-2.el8.x86_64                                     
  slirp4netns-1.1.8-1.module_el8.5.0+890+6b136101.x86_64                 

Complete!
[root@harbor yum.repos.d]# cd
[root@harbor ~]# ls -a
.               anaconda-ks.cfg
..              apr-1.7.0
.bash_history   apr-1.7.0.tar.gz
.bash_logout    apr-util-1.6.1
.bash_profile   apr-util-1.6.1.tar.gz
.bashrc         httpd-2.4.54
.config         httpd-2.4.54.tar.gz
.cshrc          mysql-5.7.38-linux-glibc2.12-x86_64.tar.gz
.mysql_history  pass
.tcshrc         php-7.4.30
.viminfo        php-7.4.30.tar.xz
.wget-hsts
[root@harbor ~]# DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
[root@harbor ~]# mkdir -p $DOCKER_CONFIG/cli-plugins
[root@harbor ~]# ls -a
.               .wget-hsts
..              anaconda-ks.cfg
.bash_history   apr-1.7.0
.bash_logout    apr-1.7.0.tar.gz
.bash_profile   apr-util-1.6.1
.bashrc         apr-util-1.6.1.tar.gz
.config         httpd-2.4.54
.cshrc          httpd-2.4.54.tar.gz
.docker         mysql-5.7.38-linux-glibc2.12-x86_64.tar.gz
.mysql_history  pass
.tcshrc         php-7.4.30
.viminfo        php-7.4.30.tar.xz
[root@harbor ~]# ls .docker/
cli-plugins
[root@harbor ~]# curl -SL https://github.com/docker/compose/releases/download/v2.7.0/docker-compose-linux-x86_64 -o $DOCKER_CONFIG/cli-plugins/docker-compose
[root@harbor cli-plugins]# ls
docker-compose
[root@harbor cli-plugins]# pwd
/root/.docker/cli-plugins
[root@harbor cli-plugins]#systemctl enable --now docker
Created symlink /etc/systemd/system/multi-user.target.wants/docker.service → /usr/lib/systemd/system/docker.service.
 [root@harbor cli-plugins]# chmod +x docker-compose 
[root@harbor cli-plugins]# ll
total 25188
-rwxr-xr-x 1 root root 25792512 Aug 11 19:27 docker-compose
[root@harbor cli-plugins]# ln -s /root/.docker/cli-plugins/docker-compose  /usr/bin
[root@harbor cli-plugins]# cd
[root@harbor ~]# which docker-compose 
/usr/bin/docker-compose
[root@harbor ~]# docker compose version
Docker Compose version v2.7.0
[root@harbor ~]# ls /usr/local/
bin  etc  games  include  lib  lib64  libexec  sbin  share  src
[root@harbor ~]# ls
anaconda-ks.cfg
apr-1.7.0
apr-1.7.0.tar.gz
apr-util-1.6.1
apr-util-1.6.1.tar.gz
harbor-offline-installer-v2.5.3.tgz
httpd-2.4.54
httpd-2.4.54.tar.gz
mysql-5.7.38-linux-glibc2.12-x86_64.tar.gz
pass
php-7.4.30
php-7.4.30.tar.xz
[root@harbor ~]# tar xf harbor-offline-installer-v2.5.3.tgz -C /usr/local/
[root@harbor ~]# cd /usr/local/
[root@harbor local]# ls
apache  apr-util  etc    harbor   lib    libexec  php7  share
apr     bin       games  include  lib64  mysql    sbin  src
[root@harbor local]# cd harbor/
[root@harbor harbor]# ls
LICENSE    harbor.v2.5.3.tar.gz  install.sh
common.sh  harbor.yml.tmpl       prepare

[root@harbor harbor]# hostnamectl set-hostname harbor.example.com
[root@harbor harbor]# bash
[root@harbor harbor]# hostname
harbor.example.com
[root@harbor harbor]# cp harbor.yml.tmpl harbor.yml
[root@harbor harbor]# vim harbor.yml
hostname: harbor.example.com

#https:（注释这一栏）
  # https port for harbor, default is 443
  # port: 443(注释这一栏）
# The path of cert and key files for nginx
  #certificate: /your/certificate/path(注释这一栏）
  #private_key: /your/private/key/path(注释这一栏）
[root@harbor harbor]# ls
LICENSE    harbor.v2.5.3.tar.gz  harbor.yml.tmpl  prepare
common.sh  harbor.yml            install.sh
[root@harbor harbor]# ./install.sh 

[Step 0]: checking if docker is installed ...

Note: docker version: 20.10.17

[Step 1]: checking docker-compose is installed ...

Note: docker-compose version: 2.7.0
......
 ? Container nginx              Started                                         5.1s
 ? Container harbor-jobservice  Started                                         5.0s
? ----Harbor has been installed and started successfully.----
[root@harbor harbor]# ss -antl
State    Recv-Q   Send-Q      Local Address:Port       Peer Address:Port   Process   
LISTEN   0        128             127.0.0.1:9000            0.0.0.0:*                
LISTEN   0        128             127.0.0.1:1514            0.0.0.0:*                
LISTEN   0        128               0.0.0.0:80              0.0.0.0:*                
LISTEN   0        128               0.0.0.0:22              0.0.0.0:*                
LISTEN   0        128                  [::]:80                 [::]:*                
LISTEN   0        128                  [::]:22                 [::]:*          
[root@harbor harbor]#
```

![img](https://img2022.cnblogs.com/blog/2917034/202208/2917034-20220812003157655-1434057812.png)
![img](https://img2022.cnblogs.com/blog/2917034/202208/2917034-20220812003504415-344423559.png)

使用Harbor的注意事项：

- 在客户端上传镜像时一定要记得执行docker login进行用户认证，否则无法直接push
- 在客户端使用的时候如果不是用的https则必须要在客户端的/etc/docker/daemon.json配置文件中配置insecure-registries参数
- 数据存放路径应在配置文件中配置到一个容量比较充足的共享存储中
- Harbor是使用docker-compose命令来管理的，如果需要停止Harbor也应用docker-compose stop来停止，其他参数请--help

# 5. Harbor应用

```bash
[root@harbor ~]# hostname
harbor.example.com
[root@client ~]# ping harbor.example.com
ping: harbor.example.com: Name or service not known
[root@client ~]# vim /etc/hosts
192.168.29.140 harbor.example.com
[root@client ~]# ping harbor.example.com
PING harbor.example.com (192.168.29.140) 56(84) bytes of data.
64 bytes from harbor.example.com (192.168.29.140): icmp_seq=1 ttl=64 time=1.47 ms
64 bytes from harbor.example.com (192.168.29.140): icmp_seq=2 ttl=64 time=0.245 ms
64 bytes from harbor.example.com (192.168.29.140): icmp_seq=3 ttl=64 time=4.33 ms
^C
--- harbor.example.com ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 2012ms
rtt min/avg/max/mdev = 0.245/2.015/4.332/1.713 ms
[root@client ~]# 
{
            "registry-mirrors": ["https://j3m2itm3.mirror.aliyuncs.com"],
            "insecure-registries":["harbor.example.com"],
                "bip":"192.168.1.1/24"
}
[root@client ~]# systemctl restart docker
[root@client ~]# docker login harbor.example.com
Username: admin
Password: 
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
[root@client ~]# docker images
REPOSITORY       TAG       IMAGE ID       CREATED        SIZE
marui123/httpd   1.0       ac64e3db3533   36 hours ago   696MB
marui123/test    v6.66     ac64e3db3533   36 hours ago   696MB
[root@client ~]# docker tag marui123/test:v6.66 harbor.example.com/library/test:v5.20
[root@client ~]# docker images
REPOSITORY                        TAG       IMAGE ID       CREATED        SIZE
harbor.example.com/library/test   v5.20     ac64e3db3533   36 hours ago   696MB
marui123/httpd                    1.0       ac64e3db3533   36 hours ago   696MB
marui123/test                     v6.66     ac64e3db3533   36 hours ago   696MB
[root@client ~]# docker push harbor.example.com/library/test:v5.20
The push refers to repository [harbor.example.com/library/test]
d13106674366: Pushed 
74ddd0ec08fa: Pushed 
v5.20: digest: sha256:68f785a775d7398139830684030b509acfa4f36cb2eef66b790b88f386eb8baa size: 742
[root@client ~]# 
```

![img](https://img2022.cnblogs.com/blog/2917034/202208/2917034-20220812214725320-1340692860.png)
![img](https://img2022.cnblogs.com/blog/2917034/202208/2917034-20220812214820815-1712588257.png)

```bash
[root@client ~]# docker images
REPOSITORY                        TAG       IMAGE ID       CREATED        SIZE
marui123/httpd                    1.0       ac64e3db3533   36 hours ago   696MB
marui123/test                     v6.66     ac64e3db3533   36 hours ago   696MB
harbor.example.com/library/test   v5.20     ac64e3db3533   36 hours ago   696MB
[root@client ~]# docker rmi -f harbor.example.com/library/test:v5.20
Untagged: harbor.example.com/library/test:v5.20
Untagged: harbor.example.com/library/test@sha256:68f785a775d7398139830684030b509acfa4f36cb2eef66b790b88f386eb8baa
[root@client ~]# docker images
REPOSITORY       TAG       IMAGE ID       CREATED        SIZE
marui123/httpd   1.0       ac64e3db3533   36 hours ago   696MB
marui123/test    v6.66     ac64e3db3533   36 hours ago   696MB
[root@client ~]# docker pull harbor.example.com/library/test:v5.20
v5.20: Pulling from library/test
Digest: sha256:68f785a775d7398139830684030b509acfa4f36cb2eef66b790b88f386eb8baa
Status: Downloaded newer image for harbor.example.com/library/test:v5.20
harbor.example.com/library/test:v5.20
[root@client ~]# docker images
REPOSITORY                        TAG       IMAGE ID       CREATED        SIZE
marui123/httpd                    1.0       ac64e3db3533   36 hours ago   696MB
marui123/test                     v6.66     ac64e3db3533   36 hours ago   696MB
harbor.example.com/library/test   v5.20     ac64e3db3533   36 hours ago   696MB
```