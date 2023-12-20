## docker安装

环境centos 7



1. 删除旧版本

```bash
sudo yum remove docker \
docker-client \
docker-client-latest \
docker-common \
docker-latest \
docker-latest-logrotate \
docker-logrotate \
docker-engine
```

   

2. 安装依赖工具

```bash
sudo yum install -y yum-utils
```




3. 设置仓库

```bash
sudo yum-config-manager \
--add-repo \
https://download.docker.com/linux/centos/docker-ce.repo
```



4. 安装docker

```bash
sudo yum install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```



5. 启动docker

```bash
sudo systemctl start docker
```



6. 设置docker自启动

```bash
sudo systemctl enable docker
```



7. 配置镜像加速

使用阿里云的容器镜像加速：

登录阿里云->控制台->左侧菜单/产品与服务/容器服务/容器镜像服务->镜像工具/镜像加速器->操作文档/centos

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



