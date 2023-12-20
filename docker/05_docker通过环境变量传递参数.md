## docker、docker-compose环境变量以及传参

docker中传参可用ARG接收参数

```bash
#构建镜像
docker build -f /manage/src/main/docker/Dockerfile -t manage:1.0 --build-arg server_name= --build-arg server_version=1.0 /manage/target
```

dockerFile

```dockerfile
# 该镜像需要依赖的基础镜像

FROM java:8
#传参
ARG server_name
ARG server_version
#环境变量
ENV jar_name=$server_name

# 将当前目录下的jar包复制到docker容器的/目录下

ADD ${server_name}-${server_version}.jar /${server_name}.jar

# 运行过程中创建一个api.jar文件

RUN bash -c 'touch /'+${server_name}+'.jar' && \
    echo "Asia/Shanghai" > /etc/timezone
#声明服务运行在8082端口
EXPOSE 8082

# 指定docker容器启动时运行jar包
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar /${jar_name}.jar

# 指定维护者的名字
MAINTAINER mouxiaoshi
```

tips:

```bash
如果外部参数想在构建镜像内部使用需声明：
ENV jar_name=$server_name
```

 

ENV
设置环境变量，定义了环境变量，那么在后续的指令中，就可以使用这个环境变量。

格式：

```dockerfile
ENV <key> <value>
ENV <key1>=<value1> <key2>=<value2>...
```

以下示例设置 NODE_VERSION = 7.2.0 ， 在后续的指令中可以通过 $NODE_VERSION 引用：

```dockerfile
ENV NODE_VERSION 7.2.0

RUN curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.xz" \
  && curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/SHASUMS256.txt.asc"
```

ARG
构建参数，与 ENV 作用一至。不过作用域不一样。ARG 设置的环境变量仅对 Dockerfile 内有效，也就是说只有 docker build 的过程中有效，构建好的镜像内不存在此环境变量。

构建命令 docker build 中可以用 --build-arg <参数名>=<值> 来覆盖。

格式：

```bash
ARG <参数名>[=<默认值>]
```

docker-compose

```dockerfile
version: '3'
services:
  manage:
    image: manage:${server_version}
```

“.env”文件

```bash
$cat .env 
server_version=1.0
```

默认情况下，该docker-compose 命令将.env在您运行该命令的目录中查找一个名为的文件。



参考内容 ：

Compose中的环境变量

https://docs.docker.com/compose/environment-variables/



Docker Dockerfile

https://www.runoob.com/docker/docker-dockerfile.html



如何将ARG值传递给ENTRYPOINT？

https://stackoverflow.com/questions/34324277/how-to-pass-arg-value-to-entrypoint

