# Docker安装

以下以centos为例；更多其他安装方式，详细参照文档： https://docs.docker.com/engine/install/centos/



1、移除旧版本

```bash
sudo yum remove docker*
```



2、设置docker yum源

```bash
sudo yum install -y yum-utils
sudo yum-config-manager \
--add-repo \
https://download.docker.com/linux/centos/docker-ce.repo
```

3、安装最新docker engine

```
sudo yum install docker-ce docker-ce-cli containerd.io
```

4、安装指定版本docker engine

1、在线安装

找到所有可用docker版本列表

```bash
yum list docker-ce --showduplicates | sort -r
```

安装指定版本，用上面的版本号替换<VERSION_STRING>

```bash
sudo yum install docker-ce-<VERSION_STRING>.x86_64 docker-ce-cli-<VERSION_STRING>.x86_64 containerd.io
```

例如：

```bash
yum install docker-ce-3:20.10.5-3.el7.x86_64 docker-ce-cli-3:20.10.5-3.el7.x86_64 containerd.io
```

注意加上 .x86_64 大版本号



2、离线安装  

https://download.docker.com/linux/centos/7/x86_64/stable/Packages/  

```bash
rpm -ivh xxx.rpm
```

可以下载 tar解压启动即可

https://docs.docker.com/engine/install/binaries/#install-daemon-and-client-binaries-on-linux



5、启动服务

```bash
systemctl start docker
systemctl enable docker
```



6、镜像加速  

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://jkzyghm3.mirror.aliyuncs.com"]
}
EOF

sudo systemctl daemon-reload
sudo systemctl restart docker
```

以后docker下载直接从阿里云拉取相关镜像,  /etc/docker/daemon.json 是Docker的核心配置文件。  注意使用自己的镜像仓库,在云服务的内网环境流量是免流量费用的, 但是使用其他人的镜像服务地址是不免流量费用的



7、可视化界面-Portainer  

1、什么是Portainer

https://documentation.portainer.io/#

Portainer社区版2.0拥有超过50万的普通用户，是功能强大的开源工具集，可让您轻松地在Docker， Swarm，Kubernetes和Azure ACI中构建和管理容器。 Portainer的工作原理是在易于使用的GUI后面隐藏使管理容器变得困难的复杂性。通过消除用户使用CLI，编写YAML或理解清单的需求，Portainer使部署应用程序和解决问题变得如此简单，任何人都可以做到。 Portainer开发团队在这里为您的Docker之旅提供帮助；



2、安装  

服务端部署

```bash
docker run -d -p 8000:8000 -p 9000:9000 --name=portainer --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer-ce
```

访问 9000 端口即可



agent端部署

```bash
docker run -d -p 9001:9001 --name portainer_agent --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/docker/volumes:/var/lib/docker/volumes portainer/agent
```

