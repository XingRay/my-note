# Docker 安装 (完整详细版)




1、选择要安装的平台
Docker要求CentOS系统的内核版本高于3.10

uname -r #通过 uname -r 命令查看你当前的内核版本


 安装文档地址



 2、选择要安装的操作系统


 3、首先卸载已安装的Docker
使用Root权限登录 Centos。确保yum包更新到最新。

sudo yum update




如果你的操作系统没有安装过Docker , 就不需要执行卸载命令。

 sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine


 4、建立仓库
## 安装Docker所需要的一些工具包
sudo yum install -y yum-utils

## 建立Docker仓库 (映射仓库地址)
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo




 5、安装Docker引擎
 sudo yum install docker-ce docker-ce-cli containerd.io






6、启动Docker
sudo systemctl start docker
7、测试 Docker 是否安装正常
sudo docker run hello-world




