# Docker 日常命令大全(完整详细版)


Docker命令及用法
命令大全

Docker 命令	命令说明	具体用法
docker run	创建一个新的容器并运行一个命令	具体用法
docker start	启动一个或多个已经被停止的容器	
docker start 容器名称 / 容器ID

docker stop	停止一个运行中的容器	docker stop 容器名称 / 容器ID
docker restart	重启容器	docker restart 容器名称 / 容器ID
docker kill	杀掉一个运行中的容器	docker kill -s KILL 容器名称 / 容器ID
docker rm	删除一个或多个容器	docker rm -f 容器名称 / 容器ID
docker pause	暂停容器中所有的进程	docker pause 容器名称 / 容器ID
docker unpause	恢复容器中所有的进程	docker unpause 容器名称 / 容器ID
docker create	创建一个新的容器但不启动它	具体用法
docker exec	在运行的容器中执行命令	具体用法
docker ps 	列出容器	具体用法
docker logs	获取容器的日志	具体用法
docker login	登陆Docker镜像仓库，默认为官方仓库 Docker Hub	具体用法
docker logout 	登出Docker镜像仓库，默认为官方仓库 Docker Hub	docker logout
docker pull 	从镜像仓库中拉取或者更新指定镜像	具体用法
docker search	 从Docker Hub查找镜像	docker search 容器名称
docker images 	列出本地镜像	具体用法
docker build	命令用于使用 Dockerfile 创建镜像	具体用法
docker info	显示 Docker 系统信息，包括镜像和容器数	docker info
docker version	显示 Docker 版本信息	docker version
日常使用到的命令
## 查看本地镜像
docker images

## 查看运行中的镜像
docker ps 

## 查看所有镜像,包括未运行的
docker ps -a

## 启动某个镜像
docker start mysql

## 关闭某个镜像
docker stop mysql

## 重启某个镜像
docker restart mysql

## 强制关闭运行中的容器
docker kill -s KILL mysql

## 进入某个容器内部 (如 : mysql)
docker exec -it mysql /bin/bash
