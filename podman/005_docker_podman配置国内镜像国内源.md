# docker,podman配置国内镜像国内源

[kfepiza](https://juejin.cn/user/2925150561636920/posts)

2022-10-05 22:562145

### 一些国内镜像地址

1. Docker中国区官方镜像 , 221005好像不能用 [registry.docker-cn.com](https://link.juejin.cn/?target=https%3A%2F%2Fregistry.docker-cn.com)
2. 网易, 221005能用 [hub-mirror.c.163.com](https://link.juejin.cn/?target=http%3A%2F%2Fhub-mirror.c.163.com)
3. 中国科技大学 , 221005好像不能用 [docker.mirrors.ustc.edu.cn](https://link.juejin.cn/?target=https%3A%2F%2Fdocker.mirrors.ustc.edu.cn)
4. 阿里云容器 生成自己的加速地址 登录：[cr.console.aliyun.com/#/accelerat…](https://link.juejin.cn/?target=https%3A%2F%2Fcr.console.aliyun.com%2F%23%2Faccelerator) 点击“创建我的容器镜像”，得到专属加速地址。





### docker配置文件 `/etc/docker/daemon.json`

`/etc/docker/daemon.json` , 如果没有就创建一个

```bash
bash
复制代码sudo touch /etc/docker/daemon.json
```

编辑

```bash
bash
复制代码sudo vi /etc/docker/daemon.json
```

查看

```bash
bash
复制代码sudo cat /etc/docker/daemon.json
```

文件格式如下

```json
json复制代码{
    "registry-mirrors": [
        "http://hub-mirror.c.163.com",
        "https://docker.mirrors.ustc.edu.cn",
        "https://registry.docker-cn.com"
    ]
}
```

用脚本,不存在则创建

```bash
bash复制代码#!/bin/bash
test -d /etc/docker || mkdir -p /etc/docker
test -f /etc/docker/daemon.json || echo '
{
    "registry-mirrors": [
        "http://hub-mirror.c.163.com",
        "https://docker.mirrors.ustc.edu.cn",
        "https://registry.docker-cn.com"
    ]
}
' | sudo tee /etc/docker/daemon.json
```

测试,查看信息

```bash
bash
复制代码sudo docker run hello-world
bash
复制代码sudo docker info
```





### podman的配置文件 `/etc/containers/registries.conf`

- 全局配置文件: `/etc/containers/registries.conf`
- 用户配置文件: `~/.config/containers/registries.conf`

备份原文件

```bash
bash
复制代码sudo cp /etc/containers/registries.conf /etc/containers/registries.conf.bak
bash
复制代码sudo cp /etc/containers/registries.conf /etc/containers/registries.conf.`date "+%Y-%m-%dT%H:%M:%S"`.bak
bash
复制代码sudo cp /etc/containers/registries.conf /etc/containers/registries.conf.`date "+%Y%m%d%H%M%S"`.bak
bash
复制代码sudo cp /etc/containers/registries.conf /etc/containers/registries.conf.`date "+%y%m%d%H%M%S"`.bak
bash复制代码[ -e /etc/containers/registries.conf.BackupDir ] || mkdir /etc/containers/registries.conf.BackupDir
sudo cp /etc/containers/registries.conf /etc/containers/registries.conf.BackupDir/registries.conf.`date "+%Y%m%d%H%M%S"`.bak
```

编辑

```bash
bash
复制代码sudo vi /etc/containers/registries.conf
bash
复制代码sudo vi ~/.config/containers/registries.conf
```

查看

```bash
bash
复制代码sudo cat /etc/containers/registries.conf
bash
复制代码sudo cat ~/.config/containers/registries.conf
bash
复制代码sudo more /etc/containers/registries.conf
bash
复制代码sudo less /etc/containers/registries.conf
```

其中prefix是pull的时候指定的镜像前缀，location是获取镜像的地址，如果不指定prefix则默认和location一致。insecure=true表示允许通过HTTP协议来获取镜像，对于私有化部署/内网测试环境下无https证书的环境来说很有帮助。

配置单个镜像源

使用中科大源

```text
text复制代码###  CentOS9原版 unqualified-search-registries
# unqualified-search-registries = ["registry.fedoraproject.org", "registry.access.redhat.com", "registry.centos.org", "quay.io", "docker.io"]

###  Fedora36原版 unqualified-search-registries
# unqualified-search-registries = ["registry.fedoraproject.org", "registry.access.redhat.com", "docker.io", "quay.io"]

###  Rocky9原版 unqualified-search-registries
# unqualified-search-registries = ["registry.fedoraproject.org", "registry.access.redhat.com", "registry.centos.org", "quay.io", "docker.io"]

###  AlmaLinux9原版 unqualified-search-registries
# unqualified-search-registries = ["registry.access.redhat.com", "registry.redhat.io", "docker.io"]

### 取消从默认地址搜索的仓库域名
unqualified-search-registries = ["docker.io"]

### 自定义搜索器
[[registry]]
### 仓库前缀
prefix = "docker.io"
### 加速器地址
location = "docker.mirrors.ustc.edu.cn"
### 允许通过http协议获取镜像
insecure = true
```

使用docker中国区的源

```txt
txt复制代码unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "registry.docker-cn.com"
insecure = true
```

使用163源

```txt
txt复制代码unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "hub-mirror.c.163.com"
insecure = true
```

使用阿里源

```text
text复制代码unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "xxxxxx.mirror.aliyuncs.com"
```

配置多个镜像源

```txt
txt复制代码unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "hub-mirror.c.163.com"
insecure = true

[[registry.mirror]]
location = "docker.mirrors.ustc.edu.cn"
insecure = true
[[registry.mirror]]
location = "hub-mirror.c.163.com"
insecure = true
[[registry.mirror]]
location = "registry.docker-cn.com"
insecure = true
```

用脚本配置

```bash
bash复制代码#!/bin/bash
[ -e /etc/containers/registries.conf.BackupDir ] || mkdir /etc/containers/registries.conf.BackupDir
sudo cp /etc/containers/registries.conf /etc/containers/registries.conf.BackupDir/registries.conf.`date "+%Y%m%d%H%M%S"`.bak
printf '
unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "hub-mirror.c.163.com"
insecure = true
[[registry.mirror]]
location = "docker.mirrors.ustc.edu.cn"
insecure = true
[[registry.mirror]]
location = "hub-mirror.c.163.com"
insecure = true
[[registry.mirror]]
location = "registry.docker-cn.com"
insecure = true
' | sudo tee /etc/containers/registries.conf
```

测试,查看信息

```bash
bash
复制代码sudo podman run hello-world
bash
复制代码sudo podman info
```

标签：

[容器](https://juejin.cn/tag/容器)[Docker](https://juejin.cn/tag/Docker)