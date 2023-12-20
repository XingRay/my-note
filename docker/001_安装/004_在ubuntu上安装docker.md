## 在Ubuntu上安装Docker

#### Set up the repository

1 Update the `apt` package index and install packages to allow `apt` to use a repository over HTTPS:

```bash
sudo apt-get update
sudo apt-get install ca-certificates curl gnupg
```



2 Add Docker’s official GPG key:

```bash
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
```



3 Use the following command to set up the repository:

```bash
echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```



#### Install Docker Engine

Update the `apt` package index:

```bash
sudo apt-get update
```



Install Docker Engine, containerd, and Docker Compose.

```bash
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```



Verify that the Docker Engine installation is successful by running the `hello-world` image.

```bash
sudo docker run hello-world
```



启动docker并且查看版本

```bash
systemctl start docker
systemctl enable docker
docker --version
docker version
```



配置镜像加速

使用阿里云的容器镜像加速：

登录阿里云->控制台->左侧菜单/产品与服务/容器服务/容器镜像服务->镜像工具/镜像加速器->操作文档/centos

https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors



创建docker配置目录

```
sudo mkdir -p /etc/docker
```

将配置写入docker配置文件

```bash
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://jkzyghm3.mirror.aliyuncs.com"]
}
EOF
```

```bash
systemctl daemon-reload
systemctl restart docker
```

查看状态

```bash
systemctl status docker
systemctl status containerd
```

