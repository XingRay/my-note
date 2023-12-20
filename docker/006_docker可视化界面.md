# docker可视化界面

可视化界面-Portainer

1、什么是Portainer

https://documentation.portainer.io/#

Portainer社区版2.0拥有超过50万的普通用户，是功能强大的开源工具集，可让您轻松地在Docker， Swarm，Kubernetes和Azure ACI中构建和管理容器。 Portainer的工作原理是在易于使用的GUI后面隐藏使管理容器变得困难的复杂性。通过消除用户使用CLI，编写YAML或理解清单的需求，Portainer使部署应用程序和解决问题变得如此简单，任何人都可以做到。 Portainer开发团队在这里为您的Docker之旅提供帮助；



2、安装

服务端部署

```bash
docker run -d -p 8000:8000 -p 9000:9000 --name=portainer --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer-ce
```

-v /var/run/docker.sock:/var/run/docker.sock 因为这时一个 docker 的可视化界面, 需要操作docker, 所以要进程间通信, 使用这个 sock 实现 Portainer 与 docker 之间的通信, 需要与 docker 进行通信的容器一般都需要加上这个 sock

访问 9000 端口即可, 默认账号 admin , 第一次等需要任意设置一个密码



如果是docker集群, 那么在其他的节点需要安装 agent

agent端部署

```bash
docker run -d -p 9001:9001 --name portainer_agent --restart=always -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/docker/volumes:/var/lib/docker/volumes portainer/agent
```