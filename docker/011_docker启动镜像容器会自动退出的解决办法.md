# docker 启动镜像，容器会自动退出的解决办法

今天在修改开发环境数据库参数，修改后restart容器，启动后容器会自动退出，容器启动后，使用 `docker ps -a` 命令 进行查看, 会发现容器已经退出。

## 原因

Docker容器后台运行,就必须有一个前台进程.容器运行的命令如果不是那些一直挂起的命令（比如运行top，tail），就是会自动退出的。

## 解决办法

在启动脚本里面增加一个执行进程：

tail -f /dev/null

```
tail -f /dev/null
```

如果是别人的镜像你不想修改，可以用-dit参数

```bash
docker run -dit --name ubuntu2 ubuntu
```

或

```bash
docker run -d --name ubuntu ubuntu /bin/bash -c "tail -f /dev/null"
```



