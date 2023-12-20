# Docker容器的导入和导出



#### 1 容器的导出

容器的导出是将当前容器变成一个容器包

```bash
docker ps -a
```

```bash
CONTAINER ID  IMAGE         COMMAND                 CREATED     STATUS      PORTS                                     NAMES
66b23477cdc6  nginx:1.21.3  "/docker-entrypoint.…"  7 days ago  Up 3 hours  80/tcp, 0.0.0.0:80->80/tcp, :::80->80/tcp nginx_51tj
```

```bash
docker export -o nginx-export.tar 66b23477cdc6
```



#### 2 容器包的导入

```bash
docker import nginx-export.tar nginx:1.21.3-new
```

> `export` 和 `import` 导出的是一个容器的快照, 不是镜像本身, 也就是说没有 `layer`。
>
> 你的 `dockerfile` 里的 `workdir`, `entrypoint `之类的所有东西都会丢失，`commit` 过的话也会丢失。
>
> 快照文件将丢弃所有的历史记录和元数据信息（即仅保存容器当时的快照状态），而镜像存储文件将保存完整记录，体积也更大。

注意：

- docker save 保存的是镜像（image），docker export 保存的是容器（container）；
- docker load 用来载入镜像包，docker import 用来载入容器包，但两者都会恢复为镜像；
- docker load 不能对载入的镜像重命名，而 docker import 可以为镜像指定新名称。