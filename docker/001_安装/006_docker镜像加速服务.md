配置镜像加速

使用阿里云的容器镜像加速：

登录阿里云->控制台->左侧菜单/产品与服务/容器服务/容器镜像服务->镜像工具/镜像加速器->操作文档/centos

https://cr.console.aliyun.com/cn-hangzhou/instances/mirrors



添加镜像加速：

创建docker配置目录

```
sudo mkdir -p /etc/docker
```

将配置写入docker配置文件

```bash
cat >/etc/docker/daemon.json<<EOF
{
"registry-mirrors": ["http://hub-mirror.c.163.com"]
}
EOF
```

```
systemctl reload docker
systemctl status docker
systemctl status containerd
```



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



添加多个镜像加速服务：

```bash
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://docker.mirrors.ustc.edu.cn","https://jkzyghm3.mirror.aliyuncs.com"],
  "exec-opts": ["native.cgroupdriver=systemd"],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m"
  },
  "storage-driver": "overlay2"
}
EOF
```

```bash
https://hub-mirror.c.163.com 
https://registry.aliyuncs.com
https://docker.mirrors.ustc.edu.cn
https://82m9ar63.mirror.aliyuncs.com

https://jkzyghm3.mirror.aliyuncs.com
```

