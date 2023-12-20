## Docker常用指令

查看镜像详情

```
docker image inspect {image-id}
```



搜索镜像

```
docker search mysql
```



列出所有镜像的id

```bash
docker images -q
```



删除全部镜像，加了 -f 参数，重新打tag的镜像也可以删除

```bash
docker rmi -f $(docker images -q)
```

删除指定镜像仓库的所有镜像

```shell
docker rmi $(docker images devops-demo -aq);
```

删除全部容器

```shell
docker stop $(docker ps -aq) && docker rm $(docker ps -aq)
```

删除指定名称的容器

```shell
if docker ps -a --format "{{.Names}}" | grep -wq devops-demo; then docker stop devops-demo && docker rm devops-demo; fi
```

删除指定名称的容器并删除相关的镜像

```shell
if docker ps -a --format "{{.Names}}" | grep -wq devops-demo; then docker stop devops-demo && docker rm devops-demo && docker rmi $(docker images devops-demo -aq); fi
```



从容器中复制目录到宿主机

```bash
docker cp rabbitmq:/var/lib/rabbitmq/plugins D:/webapp/javashop/docker/rabbitmq
```



进入容器

```bash
docker exec -it <container-name> /bin/bash
```

