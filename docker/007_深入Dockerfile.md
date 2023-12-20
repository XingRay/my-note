# 深入Dockerfile

https://docs.docker.com/reference/

https://docs.docker.com/engine/reference/builder/



Dockerfile由一行行命令语句组成，并且支持以#开头的注释行。

基础的小linux系统。jdk；

一般而言，Dockerfile可以分为四部分

基础镜像信息 

维护者信息 

镜像操作指令 

启动时执行指令





| 指令        | 说明                                                         |
| ----------- | ------------------------------------------------------------ |
| FROM        | 指定基础镜像                                                 |
| MAINTAINER  | 指定维护者信息，已经过时，可以使用LABEL maintainer=xxx 来替代 |
| RUN         | 运行命令 v                                                   |
| CMD         | 指定启动容器时默认的命令 v                                   |
| ENTRYPOINT  | 指定镜像的默认入口.运行命令 v                                |
| EXPOSE      | 声明镜像内服务监听的端口 v                                   |
| ENV         | 指定环境变量，可以在docker run的时候使用-e改变 v；会被固化到image的config里面 |
| ADD         | 复制指定的src路径下的内容到容器中的dest路径下，src可以为url会自动下载，可以为tar文件，会自动解压 |
| COPY        | 复制本地主机的src路径下的内容到镜像中的dest路径下，但不会自动解压等 |
| LABEL       | 指定生成镜像的元数据标签信息                                 |
| VOLUME      | 创建数据卷挂载点                                             |
| USER        | 指定运行容器时的用户名或UID                                  |
| WORKDIR     | 配置工作目录，为后续的RUN、CMD、ENTRYPOINT指令配置工作目录   |
| ARG         | 指定镜像内使用的参数（如版本号信息等），可以在build的时候，使用--buildargs改变 v |
| OBBUILD     | 配置当创建的镜像作为其他镜像的基础镜像是，所指定的创建操作指令 |
| STOPSIGNAL  | 容器退出的信号值                                             |
| HEALTHCHECK | 健康检查                                                     |
| SHELL       | 指定使用shell时的默认shell类型                               |



## 1 FROM

FROM 指定基础镜像, 如:

```dockerfile
FROM alpine:latest
```

最好挑一些 `apline`，`slim`之类的基础小镜像. 

`scratch` 镜像是一个空镜像，常用于多阶段构建

如何确定我需要什么要的基础镜像？Java应用当然是java基础镜像（SpringBoot应用）或者Tomcat基础镜像（War应用）JS模块化应用一般用nodejs基础镜像其他各种语言用自己的服务器或者基础环境镜像，如python、golang、java、php等

`FROM` 不能引用多个基础镜像, 只能引用一个基础镜像, 但是可以多阶段构建





## 2 LABEL

标注镜像的一些说明信息。

如: 维护者信息推荐不再使用 MAINTAINER  , 而是使用LABEL

```dockerfile
LABEL maintainer=tom
```

如果 value 中有空格或者特殊字符, 那么可以使用`""`, 如:

```dockerfile
LABEL maintainer="tom & jerry"
```

如果需要打多个标签

```dockerfile
LABEL k1=v1 k2=v2 k3=v3 ...
```

也可以换行书写

```dockerfile
LABEL k1=v1 \
k2=v2 \
k3=v3 ...
```

这些标签使用 docker image inspect 可以看到, 相当于作者给镜像添加的描述



## 3 RUN

镜像构建期间在镜像中执行的操作指令

比如要做一个mysql镜像, 可以在现有的linux镜像基础上安装mysql

```dockerfile
FROM ubuntu:latest
RUN apt-get install mysql
```

或者

```dockerfile
FROM centos:latest
RUN yum install mysql
```

RUN 指令就相当于在FROM 指定的操作系统中的终端上执行指令

RUN 指令默认使用 id=0 的用户, 也就是镜像使用的系统的 root 用户执行, 这里镜像的系统指的是 FROM 指令指定的系统, 比如 FROM alpine , 那么 RUN 指令就是默认使用 alpine 系统的 root 用户的身份, 与宿主机无关.



示例:

```bash\
vi Dockerfile
```

输入一下内容

```dockerfile
FROM alpine
RUN echo 111111
CMD echo start;sleep 10;echo success
```

如果想要镜像启动时执行多个命令, 可以:

1 准备一个脚本文件 ( 大多数镜像都是采用这种方式 )

2 直接在CMD命令中用`;` 分割指令

注意: 

RUN 是在镜像构建时, 也就是执行 docker build 过程中执行

CMD 是在容器运行时执行, 也就是 docker run 之后执行



RUN 能执行什么指令取决于 FROM 指定的基础镜像, 如果是 FROM java-xxx 镜像, 那么 RUN 指令就可以执行 `java -jar` `java compile`等指令

CMD 能执行什么指令取决于 FROM 指定的基础镜像和构建过程中安装的程序, 比如 RUN `apt-get install mysql`  那么就可以在 CMD 中执行 mysql 相关指令



生成镜像

```bash
docker build -t my-alpine:v1.0 -f Dockerfile .
```

```bash
root@ubuntu-dev:~/tmp# docker build -t my-alpine:v1.0 -f Dockerfile .
[+] Building 10.6s (6/6) FINISHED                                                                                docker:default
 => [internal] load build definition from Dockerfile                                                             0.0s
 => => transferring dockerfile: 91B                                                                              0.0s
 => [internal] load .dockerignore                                                                                0.0s
 => => transferring context: 2B                                                                                  0.0s
 => [internal] load metadata for docker.io/library/alpine:latest                                                 5.1s
 => [1/2] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1  5.0s
 => => resolve docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1  0.0s
 => => sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1 1.64kB / 1.64kB                   0.0s
 => => sha256:25fad2a32ad1f6f510e528448ae1ec69a28ef81916a004d3629874104f8a7f70 528B / 528B                       0.0s
 => => sha256:c1aabb73d2339c5ebaa3681de2e9d9c18d57485045a4e311d9f8004bec208d67 1.47kB / 1.47kB                   0.0s
 => => sha256:31e352740f534f9ad170f75378a84fe453d6156e40700b882d737a8f4a6988a3 3.40MB / 3.40MB                   4.8s
 => => extracting sha256:31e352740f534f9ad170f75378a84fe453d6156e40700b882d737a8f4a6988a3                        0.1s
 => [2/2] RUN echo 111111                                                                                        0.4s
 => exporting to image                                                                                           0.0s
 => => exporting layers                                                                                          0.0s
 => => writing image sha256:aca3a6a7859c46124cdb62ab94c6ca3e81efa865929fa35df2f268f0ea511385                     0.0s
 => => naming to docker.io/library/my-alpine:v1.0         
```

可以看到 `=> [2/2] RUN echo 111111`  说明 RUN 指令是在构建期间运行的.



以交互模式运行这个镜像

```bash
docker run --name hello-alpine -it my-alpine:v1.0
```

```bash
root@ubuntu-dev:~/tmp# docker run --name hello-alpine -it my-alpine:v1.0
start
success
```

执行完后该容器就已经退出了, 使用 docker ps 看不到这个容器, 使用指令

```bash
docker ps -a
```

```bash
root@ubuntu-dev:~/tmp# docker ps -a
CONTAINER ID   IMAGE           COMMAND                  CREATED              STATUS                          PORTS  NAMES
418feca1c6ed   my-alpine:v1.0  "/bin/sh -c 'echo st…"   About a minute ago   Exited (0) About a minute ago          hello-alpine
```

可以看到这这个容器的状态为 `Exited`





RUN 指令在层顶部的新层执行任何命令，并提交结果，生成新的镜像层。生成的提交映像将用于Dockerfile中的下一步。 分层运行RUN指令并生成提交符合Docker的核心概念，就像源代码控制一样。



RUN 指令有2种形式

1 shell command 形式

```dockerfile
RUN <command>
```

其实就是 `bash -c`  的形式, RUN 指令的 command 作为 bash -c 的参数, 比如 

```dockerfile
RUN echo 1111
```

相当于

```bash
bash -c "echo 1111"
```



2 exec 形式

如果命令很长, 可以这样写:

```dockerfile
RUN ["executable", "param1", "param2"]
```

如:

```dockerfile
RUN ["echo", "2222"]
```

示例:

```bash
vi d2
```

```dockerfile
FROM alpine
RUN echo 111111
RUN ["echo", "2222"]
CMD echo start;sleep 5;echo success
```

```bash
docker build -t my-alpine:v1.1 -f d2 .
```

```bash
root@ubuntu-dev:~/tmp# docker build -t my-alpine:v1.1 -f d2 .
[+] Building 2.6s (7/7) FINISHED                                                           docker:default
 => [internal] load build definition from d2                                                         0.0s
 => => transferring dockerfile: 114B                                                                 0.0s
 => [internal] load .dockerignore                                                                    0.0s
 => => transferring context: 2B                                                                      0.0s
 => [internal] load metadata for docker.io/library/alpine:latest                                     2.6s
 => [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe  0.0s
 => CACHED [2/3] RUN echo 111111                                                                     0.0s
 => CACHED [3/3] RUN ["echo", "2222"]                                                                0.0s
 => exporting to image                                                                               0.0s
 => => exporting layers                                                                              0.0s
 => => writing image sha256:38c828ad4bc14c52baf787a2172d5b1e54ef907f34c135429a248a55cd7142b6         0.0s
 => => naming to docker.io/library/my-alpine:v1.1                                                    0.0s
```

这里可以看到之前缓存了导致看不到输出, 可以禁用缓存

```bash
docker build -t my-alpine:v1.1 -f d2 --no-cache .
```

```bash
root@ubuntu-dev:~/tmp# docker build -t my-alpine:v1.1 -f d2 --no-cache .
[+] Building 2.3s (7/7) FINISHED                                                           docker:default
 => [internal] load build definition from d2                                                         0.0s
 => => transferring dockerfile: 114B                                                                 0.0s
 => [internal] load .dockerignore                                                                    0.0s
 => => transferring context: 2B                                                                      0.0s
 => [internal] load metadata for docker.io/library/alpine:latest                                     1.3s
 => CACHED [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9  0.0s
 => [2/3] RUN echo 111111                                                                            0.3s
 => [3/3] RUN ["echo", "2222"]                                                                       0.6s
 => exporting to image                                                                               0.0s
 => => exporting layers                                                                              0.0s
 => => writing image sha256:19499066dd55e68e251e59c122490f40e430e344430c5963f427796b407d29de         0.0s
 => => naming to docker.io/library/my-alpine:v1.1                                                    0.0s
```



参数的设置与读取

```bash
vi dockerfile-cmd-param
```

```dockerfile
FROM alpine
ENV envParam=envParam-111
ARG argParam=argParam-222
// 填充下面的各个语句
```

```bash
docker build -t alpine-cmd-param:v1.0.0 -f dockerfile-cmd-param --no-cache --progress=plain . && docker run --name my-alpine-cmd-param alpine-cmd-param:v1.0.0 && docker stop my-alpine-cmd-param && docker rm my-alpine-cmd-param && docker rmi alpine-cmd-param:v1.0.0
```

| cmd\param              | $param                                                       | '$param'                                                     | "$param"                                                     | ${param}                                                     | '${param}'                                                   | "${param}"                                                   |
| ---------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| RUN echo envParam      | RUN echo $envParam<br />正常读取参数<br />输出: env-param-111 | RUN echo '$envParam'<br />无法读取参数<br />输出: $envParam  | RUN echo "$envParam"<br />正常读取参数<br />输出: env-param-111 | RUN echo ${envParam}<br />正常读取参数<br />输出: env-param-111 | RUN echo '${envParam}'<br />无法读取参数<br />输出: ${envParam} | RUN echo "${envParam}"<br />正常读取参数<br />输出: env-param-111 |
| RUN ["echo", envParam] | RUN ["echo", $envParam]<br />语法错误<br />输出: echo,: not found | RUN ["echo", '$envParam']<br />语法错误<br />输出: echo,: not found | RUN ["echo", "$envParam"]<br />无法读取参数<br />输出: $envParam | RUN ["echo", ${envParam}]<br />语法错误<br />输出: echo,: not found | RUN ["echo", '${envParam}']<br />语法错误<br />输出: echo,: not found | RUN ["echo", "${envParam}"]<br />无法读取参数<br />输出: ${envParam} |
| RUN echo argParam      | RUN echo $argParam<br />正常读取参数<br />输出: arg-param-222 | RUN echo '$argParam'<br />无法读取参数<br />输出: $argParam  | RUN echo "$argParam"<br />正常读取参数<br />输出: arg-param-222 | RUN echo ${argParam}<br />正常读取参数<br />输出: arg-param-222 | RUN echo '${argParam}'<br />无法读取参数<br />输出: ${argParam} | RUN echo "${argParam}"<br />正常读取参数<br />输出: arg-param-222 |
| RUN ["echo", argParam] | RUN ["echo" $argParam]<br />语法错误<br />输出: echo,: not found | RUN ["echo", '$argParam']<br />语法错误<br />输出: echo,: not found | RUN ["echo", "$argParam"]<br />无法读取参数<br />输出: $argParam | RUN ["echo", ${argParam}]<br />语法错误<br />输出: echo,: not found | RUN ["echo", '${argParam}']<br />语法错误<br />输出: echo,: not found | RUN ["echo", "${argParam}"]<br />无法读取参数<br />输出: ${argParam} |
| CMD echo envParam      | CMD echo $envParam<br />正常读取参数<br />输出: env-param-111 | CMD echo '$envParam'<br />无法读取参数<br />输出: $envParam  | CMD echo "$envParam"<br />正常读取参数<br />输出: env-param-111 | CMD echo ${envParam}<br />正常读取参数<br />输出: env-param-111 | CMD echo '${envParam}'<br />无法读取参数<br />输出: ${envParam} | CMD echo "${envParam}"<br />正常读取参数<br />输出: env-param-111 |
| CMD ["echo", envParam] | CMD ["echo" $envParam]<br />语法错误<br />输出: /bin/sh: [echo: not found | CMD ["echo", '$envParam']<br />语法错误<br />输出: /bin/sh: [echo: not found | CMD ["echo", "$envParam"]<br />无法读取参数<br />输出: $envParam | CMD ["echo", ${envParam}]<br />语法错误<br />输出: /bin/sh: [echo: not found | CMD ["echo", '${envParam}']<br />语法错误<br />输出: /bin/sh: [echo: not found | CMD ["echo", "${envParam}"]<br />无法读取参数<br />输出: ${envParam} |
| CMD echo argParam      | CMD echo $argParam<br />无法读取参数<br />输出: 空           | CMD echo '$argParam'<br />无法读取参数<br />输出: $argParam  | CMD echo "$argParam"<br />无法读取参数<br />输出: 空         | CMD echo ${argParam}<br />无法读取参数<br />输出: 空         | CMD echo '${argParam}'<br />无法读取参数<br />输出: ${argParam} | CMD echo "${argParam}"<br />无法读取参数<br />输出: 空       |
| CMD ["echo", argParam] | CMD ["echo" $argParam]<br />语法错误<br />输出: /bin/sh: [echo: not found | CMD ["echo", '$argParam']<br />语法错误<br />输出: /bin/sh: [echo: not found | CMD ["echo", "$argParam"]<br />无法读取参数<br />输出: $argParam | CMD ["echo", ${argParam}]<br />语法错误<br />输出: /bin/sh: [echo: not found | CMD ["echo", '${argParam}']<br />语法错误<br />输出: /bin/sh: [echo: not found | CMD ["echo", "${argParam}"]<br />无法读取参数<br />输出: ${argParam} |

综合上述的测试 ,  可以正常获取参数的值的指令如下:

```dockerfile
FROM alpine
ENV envParam=envParam-111
ARG argParam=argParam-222

RUN echo $envParam
RUN echo "$envParam"
RUN echo ${envParam}
RUN echo "${envParam}"
RUN echo $argParam
RUN echo "$argParam"
RUN echo ${argParam}
RUN echo "${argParam}"

CMD echo $envParam && echo "$envParam" && echo ${envParam} && echo "${envParam}"
```

```bash
root@ubuntu-dev:~# docker build -t alpine-cmd-param:v1.0.0 -f dockerfile-cmd-param --no-cache --progress=plain . && docker run --name my-alpine-cmd-param alpine-cmd-param:v1.0.0 && docker stop my-alpine-cmd-param && docker rm my-alpine-cmd-param && docker rmi alpine-cmd-param:v1.0.0
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-cmd-param
#2 transferring dockerfile: 364B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.9s

#4 [1/9] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [2/9] RUN echo envParam-111
#5 0.272 envParam-111
#5 DONE 0.3s

#6 [3/9] RUN echo "envParam-111"
#6 0.418 envParam-111
#6 DONE 0.4s

#7 [4/9] RUN echo envParam-111
#7 0.404 envParam-111
#7 DONE 0.4s

#8 [5/9] RUN echo "envParam-111"
#8 0.405 envParam-111
#8 DONE 0.4s

#9 [6/9] RUN echo argParam-222
#9 0.393 argParam-222
#9 DONE 0.4s

#10 [7/9] RUN echo "argParam-222"
#10 0.406 argParam-222
#10 DONE 0.4s

#11 [8/9] RUN echo argParam-222
#11 0.332 argParam-222
#11 DONE 0.3s

#12 [9/9] RUN echo "argParam-222"
#12 0.375 argParam-222
#12 DONE 0.4s

#13 exporting to image
#13 exporting layers 0.1s done
#13 writing image sha256:df6dec6aa846fa27c67d232418718222bc09bc41d036f685af1460bb946aba5c done
#13 naming to docker.io/library/alpine-cmd-param:v1.0.0 done
#13 DONE 0.1s
envParam-111
envParam-111
envParam-111
envParam-111
my-alpine-cmd-param
my-alpine-cmd-param
Untagged: alpine-cmd-param:v1.0.0
Deleted: sha256:df6dec6aa846fa27c67d232418718222bc09bc41d036f685af1460bb946aba5c
```

原理:

1 只有 `/bin/sh -c "cmd ..."`指令可以解析参数

2 参数的形式是 ${param} , 如果param只有一个单词,可以省略 `{}` 简写为 `$param`  , 但是如果是 `${param-name}` 这种格式简写为 $param-name 会解析错误, 会被解析为 ${param}-name , 这种情况下 `${param-name}`  中的 `{}`不能省略 , $param 和 ${param} 可以用 `""` 包围, 但是不能用 `''`包围, 被 `''`包围的会直接认为是字符串的字面值, 而不会被认为是表达式.

3 RUN cmd ... 和 CMD cmd ... 形式的指令本质上等价于  RUN ["/bin/sh", "-c", "cmd ...."] 和 CMD ["/bin/sh", "-c", "cmd ...."] , 因为是通过 `sh -c` 执行的指令, 所以可以解析参数表达式

4 RUN ["cmd", "arg",  ... ] 形式的指令, 如果执行不是调用 `sh -c` , 那么指令就不具有解析参数表达式的能力, 因此这些指令中 `${param}` 表示式会被认为是字符串字面值. 因此无法正确解析参数 .



重新执行构建和运行, 不删除容器和镜像

```bash
docker build -t alpine-cmd-param:v1.0.0 -f dockerfile-cmd-param --no-cache --progress=plain . && docker run --name my-alpine-cmd-param alpine-cmd-param:v1.0.0
```

执行完毕后

查看镜像信息

```bash
docker image inspect alpine-cmd-param:v1.0.0
```

```json
[
    {
        "Id": "sha256:458e2b6394c49da13c6330a7808e979d967162e6427d4af1ae7f01ba74526749",
        "RepoTags": [
            "alpine-cmd-param:v1.0.0"
        ],
        "RepoDigests": [],
        "Parent": "",
        "Comment": "buildkit.dockerfile.v0",
        "Created": "2023-08-02T11:49:09.961666998Z",
        "Container": "",
        "ContainerConfig": {
            "Hostname": "",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": null,
            "Cmd": null,
            "Image": "",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": null
        },
        "DockerVersion": "",
        "Author": "",
        "Config": {
            "Hostname": "",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "envParam=envParam-111"
            ],
            "Cmd": [
                "/bin/sh",
                "-c",
                "echo $envParam && echo \"$envParam\" && echo ${envParam} && echo \"${envParam}\""
            ],
            "ArgsEscaped": true,
            "Image": "",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": null
        },
        "Architecture": "amd64",
        "Os": "linux",
        "Size": 7331611,
        "VirtualSize": 7331611,
        "GraphDriver": {
            "Data": {
                "LowerDir": "/var/lib/docker/overlay2/lnoxzbb1rw3plw09nv30iexzu/diff:/var/lib/docker/overlay2/j4c8qld4c9unwd3ikjki212iv/diff:/var/lib/docker/overlay2/oq9q10bazi87ty5a1dysc5pia/diff:/var/lib/docker/overlay2/rsv1t0dw8ygve3d0tjnih06e6/diff:/var/lib/docker/overlay2/9fdla5ieuxh9ir9v24ao4fu6m/diff:/var/lib/docker/overlay2/z6myutfzcj5g4yjb2l60d5jt5/diff:/var/lib/docker/overlay2/wvyar43xzf94p0ak5legcpsv4/diff:/var/lib/docker/overlay2/45c34602ad76632ef7f1a4c05fa0b61fde4fe053bcb8209c9817ccfc68fc9394/diff",
                "MergedDir": "/var/lib/docker/overlay2/dlnox1kszor6b33a1kya0te08/merged",
                "UpperDir": "/var/lib/docker/overlay2/dlnox1kszor6b33a1kya0te08/diff",
                "WorkDir": "/var/lib/docker/overlay2/dlnox1kszor6b33a1kya0te08/work"
            },
            "Name": "overlay2"
        },
        "RootFS": {
            "Type": "layers",
            "Layers": [
                "sha256:78a822fe2a2d2c84f3de4a403188c45f623017d6a4521d23047c9fbb0801794c",
                "sha256:ace500a0355263208d0a2824c6c074917c0c0796f7d4f701a2b79ca4e162257f",
                "sha256:ace500a0355263208d0a2824c6c074917c0c0796f7d4f701a2b79ca4e162257f",
                "sha256:ace500a0355263208d0a2824c6c074917c0c0796f7d4f701a2b79ca4e162257f",
                "sha256:5cb5f5bfcc8ce69e59961ac2bbb8418952385b89b0c744cf7cff94afde64c7f3",
                "sha256:5cb5f5bfcc8ce69e59961ac2bbb8418952385b89b0c744cf7cff94afde64c7f3",
                "sha256:f22e0b457b4cdccde49df65978e6f4d059fa2f9a6c994b96aa29f26ca603a0eb",
                "sha256:f22e0b457b4cdccde49df65978e6f4d059fa2f9a6c994b96aa29f26ca603a0eb",
                "sha256:f22e0b457b4cdccde49df65978e6f4d059fa2f9a6c994b96aa29f26ca603a0eb"
            ]
        },
        "Metadata": {
            "LastTagTime": "2023-08-02T11:49:10.016843444Z"
        }
    }
]
```

可以看到环境变量的信息存储在镜像的配置信息

```json
"Env": [
    "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
    "envParam=envParam-111"
]
```



查看容器信息

```bash
docker inspect my-alpine-cmd-param
```

```json
[
    {
        "Id": "609db32a7d3b2b6be4ca48850c4922dd9bf7968c98fb8e1e6c1e15e6364ec6a8",
        "Created": "2023-08-02T11:49:10.058098987Z",
        "Path": "/bin/sh",
        "Args": [
            "-c",
            "echo $envParam && echo \"$envParam\" && echo ${envParam} && echo \"${envParam}\""
        ],
        "State": {
            "Status": "exited",
            "Running": false,
            "Paused": false,
            "Restarting": false,
            "OOMKilled": false,
            "Dead": false,
            "Pid": 0,
            "ExitCode": 0,
            "Error": "",
            "StartedAt": "2023-08-02T11:49:10.404388215Z",
            "FinishedAt": "2023-08-02T11:49:10.40350671Z"
        },
        "Image": "sha256:458e2b6394c49da13c6330a7808e979d967162e6427d4af1ae7f01ba74526749",
        "ResolvConfPath": "/var/lib/docker/containers/609db32a7d3b2b6be4ca48850c4922dd9bf7968c98fb8e1e6c1e15e6364ec6a8/resolv.conf",
        "HostnamePath": "/var/lib/docker/containers/609db32a7d3b2b6be4ca48850c4922dd9bf7968c98fb8e1e6c1e15e6364ec6a8/hostname",
        "HostsPath": "/var/lib/docker/containers/609db32a7d3b2b6be4ca48850c4922dd9bf7968c98fb8e1e6c1e15e6364ec6a8/hosts",
        "LogPath": "/var/lib/docker/containers/609db32a7d3b2b6be4ca48850c4922dd9bf7968c98fb8e1e6c1e15e6364ec6a8/609db32a7d3b2b6be4ca48850c4922dd9bf7968c98fb8e1e6c1e15e6364ec6a8-json.log",
        "Name": "/my-alpine-cmd-param",
        "RestartCount": 0,
        "Driver": "overlay2",
        "Platform": "linux",
        "MountLabel": "",
        "ProcessLabel": "",
        "AppArmorProfile": "docker-default",
        "ExecIDs": null,
        "HostConfig": {
            "Binds": null,
            "ContainerIDFile": "",
            "LogConfig": {
                "Type": "json-file",
                "Config": {}
            },
            "NetworkMode": "default",
            "PortBindings": {},
            "RestartPolicy": {
                "Name": "no",
                "MaximumRetryCount": 0
            },
            "AutoRemove": false,
            "VolumeDriver": "",
            "VolumesFrom": null,
            "ConsoleSize": [
                60,
                198
            ],
            "CapAdd": null,
            "CapDrop": null,
            "CgroupnsMode": "private",
            "Dns": [],
            "DnsOptions": [],
            "DnsSearch": [],
            "ExtraHosts": null,
            "GroupAdd": null,
            "IpcMode": "private",
            "Cgroup": "",
            "Links": null,
            "OomScoreAdj": 0,
            "PidMode": "",
            "Privileged": false,
            "PublishAllPorts": false,
            "ReadonlyRootfs": false,
            "SecurityOpt": null,
            "UTSMode": "",
            "UsernsMode": "",
            "ShmSize": 67108864,
            "Runtime": "runc",
            "Isolation": "",
            "CpuShares": 0,
            "Memory": 0,
            "NanoCpus": 0,
            "CgroupParent": "",
            "BlkioWeight": 0,
            "BlkioWeightDevice": [],
            "BlkioDeviceReadBps": [],
            "BlkioDeviceWriteBps": [],
            "BlkioDeviceReadIOps": [],
            "BlkioDeviceWriteIOps": [],
            "CpuPeriod": 0,
            "CpuQuota": 0,
            "CpuRealtimePeriod": 0,
            "CpuRealtimeRuntime": 0,
            "CpusetCpus": "",
            "CpusetMems": "",
            "Devices": [],
            "DeviceCgroupRules": null,
            "DeviceRequests": null,
            "MemoryReservation": 0,
            "MemorySwap": 0,
            "MemorySwappiness": null,
            "OomKillDisable": null,
            "PidsLimit": null,
            "Ulimits": null,
            "CpuCount": 0,
            "CpuPercent": 0,
            "IOMaximumIOps": 0,
            "IOMaximumBandwidth": 0,
            "MaskedPaths": [
                "/proc/asound",
                "/proc/acpi",
                "/proc/kcore",
                "/proc/keys",
                "/proc/latency_stats",
                "/proc/timer_list",
                "/proc/timer_stats",
                "/proc/sched_debug",
                "/proc/scsi",
                "/sys/firmware"
            ],
            "ReadonlyPaths": [
                "/proc/bus",
                "/proc/fs",
                "/proc/irq",
                "/proc/sys",
                "/proc/sysrq-trigger"
            ]
        },
        "GraphDriver": {
            "Data": {
                "LowerDir": "/var/lib/docker/overlay2/f924463586ea8bae086a897da6fb264b48bbb881ab0e7df7029728c060d02799-init/diff:/var/lib/docker/overlay2/dlnox1kszor6b33a1kya0te08/diff:/var/lib/docker/overlay2/lnoxzbb1rw3plw09nv30iexzu/diff:/var/lib/docker/overlay2/j4c8qld4c9unwd3ikjki212iv/diff:/var/lib/docker/overlay2/oq9q10bazi87ty5a1dysc5pia/diff:/var/lib/docker/overlay2/rsv1t0dw8ygve3d0tjnih06e6/diff:/var/lib/docker/overlay2/9fdla5ieuxh9ir9v24ao4fu6m/diff:/var/lib/docker/overlay2/z6myutfzcj5g4yjb2l60d5jt5/diff:/var/lib/docker/overlay2/wvyar43xzf94p0ak5legcpsv4/diff:/var/lib/docker/overlay2/45c34602ad76632ef7f1a4c05fa0b61fde4fe053bcb8209c9817ccfc68fc9394/diff",
                "MergedDir": "/var/lib/docker/overlay2/f924463586ea8bae086a897da6fb264b48bbb881ab0e7df7029728c060d02799/merged",
                "UpperDir": "/var/lib/docker/overlay2/f924463586ea8bae086a897da6fb264b48bbb881ab0e7df7029728c060d02799/diff",
                "WorkDir": "/var/lib/docker/overlay2/f924463586ea8bae086a897da6fb264b48bbb881ab0e7df7029728c060d02799/work"
            },
            "Name": "overlay2"
        },
        "Mounts": [],
        "Config": {
            "Hostname": "609db32a7d3b",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": true,
            "AttachStderr": true,
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "envParam=envParam-111"
            ],
            "Cmd": [
                "/bin/sh",
                "-c",
                "echo $envParam && echo \"$envParam\" && echo ${envParam} && echo \"${envParam}\""
            ],
            "Image": "alpine-cmd-param:v1.0.0",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": {}
        },
        "NetworkSettings": {
            "Bridge": "",
            "SandboxID": "ce956f95e4a8733d14e616cf2f97bf1878ce40e1504d05bf1fd0d08d4e971b69",
            "HairpinMode": false,
            "LinkLocalIPv6Address": "",
            "LinkLocalIPv6PrefixLen": 0,
            "Ports": {},
            "SandboxKey": "/var/run/docker/netns/ce956f95e4a8",
            "SecondaryIPAddresses": null,
            "SecondaryIPv6Addresses": null,
            "EndpointID": "",
            "Gateway": "",
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "",
            "IPPrefixLen": 0,
            "IPv6Gateway": "",
            "MacAddress": "",
            "Networks": {
                "bridge": {
                    "IPAMConfig": null,
                    "Links": null,
                    "Aliases": null,
                    "NetworkID": "36b68725ca38ea5db659fd94f6ef77249b9ca444c0ba354f991667941e5ee5d3",
                    "EndpointID": "",
                    "Gateway": "",
                    "IPAddress": "",
                    "IPPrefixLen": 0,
                    "IPv6Gateway": "",
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "MacAddress": "",
                    "DriverOpts": null
                }
            }
        }
    }
]
```

可以看到 ENV 指令的参数被保存到容器的配置信息中了

```json
"Env": [
	"PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
	"envParam=envParam-111"
]
```

ARG 是构建参数, 只在构建期间起作用, 在容器运行期不起作用, 因此 CMD 指令无法读取 ARG 指令设置的参数的值



RUN 是构建时期运行的指令, 构建时期指的就是 `docker build`  指令根据 Dockerfile 创建一个镜像的整个过程时期

CMD 是运行时期指令, 运行期指的是根据创建的镜像启动一个容器, 容器启动默认运行的命令, 具体来说就是执行了 `docker run` 或者 `docker start` 时在容器内执行的命令







ENV

可以使用ENV指定可以变化的部分, 如 param

```bash
vi d3
```

```dockerfile
FROM alpine
ENV param=111
RUN echo $param
RUN ["echo", $param]
RUN ["echo", '$param']
RUN ["echo", "$param"]
RUN ["echo", ${param}]
RUN ["echo", '${param}']
RUN ["echo", "${param}"]

CMD echo start;sleep 5;echo success
```

```bash
docker build -t my-alpine:v3 -f d3 .
```





exec形式可以避免破坏shell字符串，并使用不包含指定shell可执行文件的基本映像运行RUN命令。可以使用SHELL命令更改shell形式的默认shell。 在shell形式中，您可以使用\（反斜杠）将一条RUN指令继续到下一行。

```
RUN <command> ( shell 形式, /bin/sh -c 的方式运行，避免破坏shell字符串)
RUN ["executable", "param1", "param2"] ( exec 形式)
```



```
RUN /bin/bash -c 'source $HOME/.bashrc; \
echo $HOME'
#上面等于下面这种写法
RUN /bin/bash -c 'source $HOME/.bashrc; echo $HOME'
RUN ["/bin/bash", "-c", "echo hello"]
```



```
# 测试案例
FROM alpine
LABEL maintainer=leifengyang xx=aa
ENV msg='hello atguigu itdachang'
RUN echo $msg
RUN ["echo","$msg"]
RUN /bin/sh -c 'echo $msg'
RUN ["/bin/sh","-c","echo $msg"]
CMD sleep 10000
#总结； 由于[]不是shell形式，所以不能输出变量信息，而是输出$msg。其他任何/bin/sh -c 的形式都
可以输出变量信息
```



总结：什么是shell和exec形式  



```
1. shell 是 /bin/sh -c <command>的方式，
2. exec ["/bin/sh","-c",command] 的方式 == shell方式
也就是exec 默认方式不会进行变量替换
```



## 4 CMD和ENTRYPOINT  

ENTRYPOINT 是容器启动真正的唯一的入口, 在 Dockerfile中定义, 写入镜像后, 运行容器时不可以修改

CMD是命令,  是进入入口的时候携带的命令, 在 Dockerfile中定义, 写入镜像后, 运行容器时可以被 `docker run <image> <cmd>` 命令覆盖

```dockerfile
FROM alpine
CMD ping baidu.com
```

默认启动时执行 `ping baidu.com`, 但是如果执行 

```bash
docker run demo:v1.0 ping qq.com
```

那么容器运行时实际会执行 ping qq.com

但是如果改为

```dockerfile
FROM alpine
ENTRYPOINT ping baidu.com
```

那么再执行

```bash
docker run demo:v1.0 ping qq.com
```

容器运行时仍然是执行 ping baidu.com 

注意:  `docker run <image> <cmd>` 指令中传入的 `<cmd>` 只会覆盖 Dockerfile 中 CMD 指令的值, 不会对 ENTRYPOINT 指令的值有影响



最佳实践:

```dockerfile
ENTRYPOINT ["ping"]
CMD baidu.com
```

不变化的部分写在 ENTRYPOINT 中, 变化的部分写在 CMD 中, CMD 为 ENTRYPOINT 提供参数



0、都可以作为容器启动入口

CMD 的三种写法：

exec 方式, 首选方式

```dockerfile
CMD ["executable","param1","param2"]
```

为ENTRYPOINT提供默认参数

```dockerfile
CMD ["param1","param2"]
```

shell 形式

```dockerfile
CMD command param1 param2
```



ENTRYPOINT 的两种写法：

exec 方式, 首选方式

```dockerfile
ENTRYPOINT ["executable", "param1", "param2"]
```

shell 形式

```dockerfile
ENTRYPOINT command param1 param2
```



```
# 一个示例
FROM alpine
LABEL maintainer=leifengyang
CMD ["1111"]
CMD ["2222"]
ENTRYPOINT ["echo"]
#构建出如上镜像后测试
docker run xxxx：效果 echo 1111
```





1、只能有一个CMD生效

Dockerfile中只能有一条CMD指令。 如果一个Dockerfile中存在多个CMD指令，则只有最后一个CMD指令会生效。

CMD的主要目的是为执行中的容器提供默认值。 这些默认值可以包含可执行文件，也可以省略可执行文件，在这种情况下，您还必须指定ENTRYPOINT指令。



2、只能有一个 ENTRYPOINT 生效

Dockerfile中只能有一条 ENTRYPOINT  指令。 如果一个Dockerfile中存在多个 ENTRYPOINT  指令，则只有最后一个 ENTRYPOINT  指令会生效。



2、CMD为ENTRYPOINT提供默认参数  

如果使用CMD为ENTRYPOINT指令提供默认参数，则CMD和ENTRYPOINT指令均应使用JSON数组格式指定。  



3、组合最终效果  

|                                | 无 <br />ENTRYPOINT                      | ENTRYPOINT<br />exec_entry p1_entry | ENTRYPOINT<br />[“exec_entry”, “p1_entry”]     |
| ------------------------------ | ---------------------------------------- | ----------------------------------- | ---------------------------------------------- |
| 无CMD                          | 错误, 不允许的写法<br />容器没有启动命令 | /bin/sh -c exec_entry p1_entry      | exec_entry p1_entry                            |
| CMD<br />[“exec_cmd”,“p1_cmd”] | exec_cmd p1_cmd                          | /bin/sh -c exec_entry p1_entry      | exec_entry p1_entry exec_cmd p1_cmd            |
| CMD<br />[“p1_cmd”,“p2_cmd”]   | p1_cmd p2_cmd                            | /bin/sh -c exec_entry p1_entry      | exec_entry p1_entry p1_cmd p2_cmd              |
| CMD exec_cmd p1_cmd            | /bin/sh -c exec_cmd p1_cmd               | /bin/sh -c exec_entry p1_entry      | exec_entry p1_entry /bin/sh -c exec_cmd p1_cmd |
|                                |                                          | 这条竖线总是以 ENTRYPOINT 为准      | 这条竖线，ENTRYPOINT和CMD共同作用              |





Understand how CMD and ENTRYPOINT interact

Both `CMD` and `ENTRYPOINT` instructions define what command gets executed when running a container. There are few rules that describe their co-operation.

1. Dockerfile should specify at least one of `CMD` or `ENTRYPOINT` commands.
2. `ENTRYPOINT` should be defined when using the container as an executable.
3. `CMD` should be used as a way of defining default arguments for an `ENTRYPOINT` command or for executing an ad-hoc command in a container.
4. `CMD` will be overridden when running the container with alternative arguments.

The table below shows what command is executed for different `ENTRYPOINT` / `CMD` combinations:

|                                | No ENTRYPOINT              | ENTRYPOINT exec_entry p1_entry | ENTRYPOINT [“exec_entry”, “p1_entry”]          |
| :----------------------------- | :------------------------- | :----------------------------- | :--------------------------------------------- |
| **No CMD**                     | *error, not allowed*       | /bin/sh -c exec_entry p1_entry | exec_entry p1_entry                            |
| **CMD [“exec_cmd”, “p1_cmd”]** | exec_cmd p1_cmd            | /bin/sh -c exec_entry p1_entry | exec_entry p1_entry exec_cmd p1_cmd            |
| **CMD exec_cmd p1_cmd**        | /bin/sh -c exec_cmd p1_cmd | /bin/sh -c exec_entry p1_entry | exec_entry p1_entry /bin/sh -c exec_cmd p1_cmd |

> **Note**
>
> If `CMD` is defined from the base image, setting `ENTRYPOINT` will reset `CMD` to an empty value. In this scenario, `CMD` must be defined in the current image to have a value.





4、docker run 启动参数会覆盖CMD内容  

```dockerfile
# 一个示例
FROM alpine
CMD ["1111"]
ENTRYPOINT ["echo"]
```

#构建出如上镜像后测试

什么都不传则 echo 1111

```bash
docker run xxx
```

传入arg1 则echo arg1

```bash
docker run xxx arg1
```



如果CMD中定义了多个参数, 但是 docker run 中无论传多少个参数, 都会将 CMD 中所有的参数覆盖

```dockerfile
FROM alpine
ENTRYPOINT ["ping", "-c"]
CMD ["5", "baidu.com"]
```

默认是ping baidu.com 5次, 但是如果想要在docker run 中覆盖CMD 中指定参数, 可以执行

```bash
docker run demo:v1 6 qq.com
```

容器可以正常运行, 会执行 

```bash
ping -c 6 qq.com
```

但是如果只执行

```
docker run demo:v1 6
```

会运行错误, 因为 CMD 中的所有参数都被覆盖了, 此时容器运行时执行的命令是

```bash
ping -c 6 
```

无法正常执行





## 5 ARG和ENV  

### 5.1 ARG

1 指令定义了一个变量，用户可以在构建时使用 --build-arg param=value ,多个参数使用 --build-arg param1=value1 --build-arg param2=value2 ... 传递，docker build命令会将其传递给构建器。

```bash
vi dockerfile-arg
```

```dockerfile
FROM alpine
RUN echo "argParam is $argParam"
ARG argParam=123456
RUN echo "argParam is $argParam"
```

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache .
```

```bash
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-arg
#2 transferring dockerfile: 139B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 0.9s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [2/3] RUN echo "argParam is $argParam"
#5 0.280 argParam is
#5 DONE 0.3s

#6 [3/3] RUN echo "argParam is 123456"
#6 0.387 argParam is 123456
#6 DONE 0.4s

#7 exporting to image
#7 exporting layers 0.0s done
#7 writing image sha256:1abbc320e51d0c21527771b3443402d7a6a6aa25e6c913252d395984badc5450 done
#7 naming to docker.io/library/arg-demo:v1 done
#7 DONE 0.0s
```

注意输出的

```bash
#5 0.280 argParam is
#6 0.387 argParam is 123456
```

说明在变量在定义之后的环节才能生效, 但是不包括运行时, `CMD` 指令和 `ENTRYPOINT` 指令都是运行时的指令



2 `--build-arg` 指定参数会覆盖Dockerfile 中指定的同名参数

```bash
vi dockerfile-arg
```

```dockerfile
FROM alpine
ARG argParam=123456
ARG msg="hello docker"
RUN echo "argParam is $argParam"
RUN echo "msg is $msg"
```

注意定义多个参数时要使用多个 ARG 指令, 每一条 ARG 指令定义一个参数

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache .
```

```bash
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-arg
#2 transferring dockerfile: 152B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.9s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [2/3] RUN echo "argParam is 123456"
#5 0.261 argParam is 123456
#5 DONE 0.3s

#6 [3/3] RUN echo "msg is hello docker"
#6 0.390 msg is hello docker
#6 DONE 0.4s

#7 exporting to image
#7 exporting layers 0.0s done
#7 writing image sha256:d0c35984ff2d2b0f07c39bf0f1da23402427fe93e3e1040cae67acb8535dfc12 done
#7 naming to docker.io/library/arg-demo:v1 done
#7 DONE 0.0s
```

默认情况下输出:

```bash
#5 0.261 argParam is 123456
#6 0.390 msg is hello docker
```

如果要修改参数的值, 可以使用下面的指令

```
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache --build-arg argParam="11 22 33" --build-arg msg="hello from docker" .
```

```bash
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-arg
#2 transferring dockerfile: 152B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.7s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [2/3] RUN echo "argParam is 11 22 33"
#5 0.269 argParam is 11 22 33
#5 DONE 0.3s

#6 [3/3] RUN echo "msg is hello from docker"
#6 0.371 msg is hello from docker
#6 DONE 0.4s

#7 exporting to image
#7 exporting layers
#7 exporting layers 0.0s done
#7 writing image sha256:e19f7fe690a8dae3f306eec6452c69a7cd216fef0fe656ef2f492e4feaf4868b done
#7 naming to docker.io/library/arg-demo:v1 done
#7 DONE 0.0s
```

可以看到输出, 参数的值为通过 `--build-arg` 传入的值

```bash
#5 0.269 argParam is 11 22 33
#6 0.371 msg is hello from docker
```



3 如果用户指定了 未在Dockerfile中定义的构建参数 ，则构建会输出 警告 。

4 ARG只在构建期有效，运行期无效

```dockerfile
FROM alpine
ARG argParam=123456
CMD ["/bin/sh", "-c", "echo \"argParam is $argParam\""]
```

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache .
```

```bash
docker run --name arg-demo arg-demo:v1
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker run --name arg-demo arg-demo:v1
argParam is
```

可以看到输出中没有获取到参数的值



5 不建议使用构建时变量来传递诸如github密钥，用户凭据等机密。因为构建时变量值使用docker history是可见的  

6 ARG变量定义从Dockerfile中定义的行开始生效  

7 使用ENV指令定义的环境变量始终会覆盖同名的ARG指令



使用场景:

```dockerfile
ARG version=3.13.4
FROM alpine:$version
RUN echo "version is $version"
```

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache .
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache .
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-arg
#2 transferring dockerfile: 112B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:3.13.4
#3 DONE 4.0s

#4 [1/2] FROM docker.io/library/alpine:3.13.4@sha256:ec14c7992a97fc11425907e908340c6c3d6ff602f5f13d899e6b7027c9b4133a
#4 resolve docker.io/library/alpine:3.13.4@sha256:ec14c7992a97fc11425907e908340c6c3d6ff602f5f13d899e6b7027c9b4133a done
#4 sha256:ca3cd42a7c9525f6ce3d64c1a70982613a8235f0cc057ec9244052921853ef15 0B / 2.81MB 0.2s
#4 sha256:ec14c7992a97fc11425907e908340c6c3d6ff602f5f13d899e6b7027c9b4133a 1.64kB / 1.64kB done
#4 sha256:e103c1b4bf019dc290bcc7aca538dc2bf7a9d0fc836e186f5fa34945c5168310 528B / 528B done
#4 sha256:49f356fa4513676c5e22e3a8404aad6c7262cc7aaed15341458265320786c58c 1.47kB / 1.47kB done
#4 sha256:ca3cd42a7c9525f6ce3d64c1a70982613a8235f0cc057ec9244052921853ef15 1.05MB / 2.81MB 3.4s
#4 sha256:ca3cd42a7c9525f6ce3d64c1a70982613a8235f0cc057ec9244052921853ef15 2.10MB / 2.81MB 4.0s
#4 extracting sha256:ca3cd42a7c9525f6ce3d64c1a70982613a8235f0cc057ec9244052921853ef15
#4 sha256:ca3cd42a7c9525f6ce3d64c1a70982613a8235f0cc057ec9244052921853ef15 2.81MB / 2.81MB 4.3s done
#4 extracting sha256:ca3cd42a7c9525f6ce3d64c1a70982613a8235f0cc057ec9244052921853ef15 0.1s done
#4 DONE 4.5s

#5 [2/2] RUN echo "version is $version"
#5 0.289 version is
#5 DONE 0.3s

#6 exporting to image
#6 exporting layers done
#6 writing image sha256:1f38ada2e50c1452820fa151593c8ac249e66ef9c6818f1567192d03e2a53d37 done
#6 naming to docker.io/library/arg-demo:v1 done
#6 DONE 0.0s
```

可以看到使用了 3.13.4 版本的 `alpine` 镜像, 需要的时候可以通过 `--build-arg` 指定版本

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache --build-arg version=3.13 .
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache --build-arg version=3.13 .
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-arg
#2 transferring dockerfile: 112B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:3.13
#3 DONE 4.1s

#4 [1/2] FROM docker.io/library/alpine:3.13@sha256:469b6e04ee185740477efa44ed5bdd64a07bbdd6c7e5f5d169e540889597b911
#4 resolve docker.io/library/alpine:3.13@sha256:469b6e04ee185740477efa44ed5bdd64a07bbdd6c7e5f5d169e540889597b911 done
#4 sha256:469b6e04ee185740477efa44ed5bdd64a07bbdd6c7e5f5d169e540889597b911 1.64kB / 1.64kB done
#4 sha256:16fd981ddc557fd3b38209d15e7ee8e3e6d9d4d579655e8e47243e2c8525b503 528B / 528B done
#4 sha256:6b5c5e00213a401b500630fd8a03f6964f033ef0e3a6845c83e780fcd5deda5c 1.47kB / 1.47kB done
#4 sha256:72cfd02ff4d01b1f319eed108b53120dea0185b916d2abeb4e6121879cbf7a65 0B / 2.83MB 0.2s
#4 sha256:72cfd02ff4d01b1f319eed108b53120dea0185b916d2abeb4e6121879cbf7a65 0B / 2.83MB 5.2s
#4 sha256:72cfd02ff4d01b1f319eed108b53120dea0185b916d2abeb4e6121879cbf7a65 1.05MB / 2.83MB 6.7s
#4 sha256:72cfd02ff4d01b1f319eed108b53120dea0185b916d2abeb4e6121879cbf7a65 2.10MB / 2.83MB 10.2s
#4 sha256:72cfd02ff4d01b1f319eed108b53120dea0185b916d2abeb4e6121879cbf7a65 2.83MB / 2.83MB 12.4s done
#4 extracting sha256:72cfd02ff4d01b1f319eed108b53120dea0185b916d2abeb4e6121879cbf7a65
#4 extracting sha256:72cfd02ff4d01b1f319eed108b53120dea0185b916d2abeb4e6121879cbf7a65 0.1s done
#4 DONE 12.6s

#5 [2/2] RUN echo "version is $version"
#5 0.317 version is
#5 DONE 0.3s

#6 exporting to image
#6 exporting layers 0.0s done
#6 writing image sha256:725a9585d621b30144db209ad60c1aefe07c8ae6bd6de0aee54e6353601138b3 done
#6 naming to docker.io/library/arg-demo:v1 done
#6 DONE 0.0s
```

可以看到使用 `3.13` 版本的 `alpine` 镜像

### 5.2 ENV

构建期和运行期都可以生效, 可以在运行期修改

1 在构建阶段中所有后续指令的环境中使用，并且在许多情况下也可以内联替换。

2 引号和反斜杠可用于在值中包含空格。

3 ENV 可以使用key value的写法，

```dockerfile
ENV MY_MSG hello
```

但是这种不建议使用了，后续版本可能会删除

```dockerfile
ENV MY_NAME="John Doe"
ENV MY_DOG=Rex\ The\ Dog
ENV MY_CAT=fluffy
#多行写法如下
ENV MY_NAME="John Doe" MY_DOG=Rex\ The\ Dog \
MY_CAT=fluffy
```

4 可以修改env的值

在构建期, 也就是使用 docker build 指令是无法修改 env 的值的, 但是在运行期可以修改 env 的值

```bash
docker run --env key1=value1 -e key2=value2
```



5 容器运行时ENV值可以生效

6 ENV在image阶段就会被解析并持久化（docker inspect image查看）



示例 :

```dockerfile
FROM alpine
ENV app=demo
RUN echo "app is $app"
CMD echo "app is $app"
```

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache .
```

```bash
docker run --name demo arg-demo:v1
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker run --name demo arg-demo:v1
app is demo
```

修改 env 的值

```bash
docker stop demo && docker rm demo
```

```bash
docker run --name demo --env app="new demo" arg-demo:v1
```

可以看到输出了参数设置的 `env` 的值

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker run --name demo --env app="new demo" arg-demo:v1
app is new demo
```



env引用arg的值

```dockerfile
FROM alpine
ARG msg=hello
ENV name=${msg}

RUN echo ${msg}
RUN echo ${name}

CMD echo ${name}
```

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache .
```

```
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-arg
#2 transferring dockerfile: 136B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.9s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [2/3] RUN echo hello
#5 0.271 hello
#5 DONE 0.3s

#6 [3/3] RUN echo hello
#6 0.411 hello
#6 DONE 0.4s

#7 exporting to image
#7 exporting layers 0.0s done
#7 writing image sha256:80b6891710ea586487e87954b487f6a965910193351d37b97802dd9ef7c06a6b done
#7 naming to docker.io/library/arg-demo:v1 done
#7 DONE 0.0s
```

```bash
docker stop demo && docker rm demo && docker run --name demo arg-demo:v1
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker stop demo && docker rm demo && docker run --name demo arg-demo:v1
demo
demo
hello
```

可以看到 CMD 指令输出 hello , 这个值是由 ARG 定义并传递给 ENV



env引用env的值

```dockerfile
FROM alpine

ENV msg1=hello
ENV msg2=$msg1

RUN echo ${msg1}
RUN echo ${msg2}

CMD ["/bin/sh", "-c", "echo ${msg1}; echo ${msg2};"]
```

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache . && docker run --name demo arg-demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache . && docker run --name demo arg-demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-arg
#2 transferring dockerfile: 176B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.9s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [2/3] RUN echo hello
#5 0.291 hello
#5 DONE 0.3s

#6 [3/3] RUN echo hello
#6 0.401 hello
#6 DONE 0.4s

#7 exporting to image
#7 exporting layers 0.0s done
#7 writing image sha256:543caa527ba5753b7ded0ce3f8342a36ea918aed058a171d5c164c3c180674fc done
#7 naming to docker.io/library/arg-demo:v1 done
#7 DONE 0.0s
hello
hello
demo
demo
```

可以看到最后容器运行时输出了2个hello, 说明env可以正常传递参数, 查看镜像详情

```bash
docker image inspect arg-demo:v1
```

```json
[
    {
        "Id": "sha256:8e49c3770bfb1e961dbd99f98028c9709b880ba325b91f54b74f8bfd6956c24e",
        "RepoTags": [
            "arg-demo:v1"
        ],
        "RepoDigests": [],
        "Parent": "",
        "Comment": "buildkit.dockerfile.v0",
        "Created": "2023-08-02T14:07:34.35701858Z",
        "Container": "",
        "ContainerConfig": {
            "Hostname": "",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": null,
            "Cmd": null,
            "Image": "",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": null
        },
        "DockerVersion": "",
        "Author": "",
        "Config": {
            "Hostname": "",
            "Domainname": "",
            "User": "",
            "AttachStdin": false,
            "AttachStdout": false,
            "AttachStderr": false,
            "Tty": false,
            "OpenStdin": false,
            "StdinOnce": false,
            "Env": [
                "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
                "msg1=hello",
                "msg2=hello"
            ],
            "Cmd": [
                "/bin/sh",
                "-c",
                "echo ${msg1}; echo ${msg2};"
            ],
            "ArgsEscaped": true,
            "Image": "",
            "Volumes": null,
            "WorkingDir": "",
            "Entrypoint": null,
            "OnBuild": null,
            "Labels": null
        },
        "Architecture": "amd64",
        "Os": "linux",
        "Size": 7331611,
        "VirtualSize": 7331611,
        "GraphDriver": {
            "Data": {
                "LowerDir": "/var/lib/docker/overlay2/m0fk60ry59jaq5cr5sh4t97r9/diff:/var/lib/docker/overlay2/45c34602ad76632ef7f1a4c05fa0b61fde4fe053bcb8209c9817ccfc68fc9394/diff",
                "MergedDir": "/var/lib/docker/overlay2/5huy1e9nld1u2y82eexo2s8wy/merged",
                "UpperDir": "/var/lib/docker/overlay2/5huy1e9nld1u2y82eexo2s8wy/diff",
                "WorkDir": "/var/lib/docker/overlay2/5huy1e9nld1u2y82eexo2s8wy/work"
            },
            "Name": "overlay2"
        },
        "RootFS": {
            "Type": "layers",
            "Layers": [
                "sha256:78a822fe2a2d2c84f3de4a403188c45f623017d6a4521d23047c9fbb0801794c",
                "sha256:7722d8174d8920fd7e9645ae4bd7d3fbc16be610b8994d63e76add61fd6c45ac",
                "sha256:791425fe9c202b47fd1b8154a6e74e3ab7ae21c8493652d41182d22d0a9dfc6f"
            ]
        },
        "Metadata": {
            "LastTagTime": "2023-08-02T14:07:34.380880845Z"
        }
    }
]
```

注意此时env的值

```json
"Env": [
    "PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
    "msg1=hello",
    "msg2=hello"
]
```

注意这里 msg2 的值为字面值, 而不再时引用 msg1 . 也就是说运行时 msg1 的值通过 --env 修改不会影响 msg2

如果通过 --env 修改 msg1 的值

```bash
docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache . && docker run --name demo --env msg1="world" arg-demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t arg-demo:v1 -f dockerfile-arg --progress=plain --no-cache . && docker run --name demo --env msg1="world" arg-demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from dockerfile-arg
#2 transferring dockerfile: 176B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.3s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [2/3] RUN echo hello
#5 0.259 hello
#5 DONE 0.3s

#6 [3/3] RUN echo hello
#6 0.401 hello
#6 DONE 0.4s

#7 exporting to image
#7 exporting layers 0.0s done
#7 writing image sha256:c330167f05ab2900d21a5014b5a128e2893de56501d46e1bc2bb11b55ecefb7a done
#7 naming to docker.io/library/arg-demo:v1 done
#7 DONE 0.0s
world
hello
demo
demo
```

可以看到容器运行时的输出为 world (msg1) 和 hello (msg2)







```dockerfile
FROM alpine
ENV arg=1111111
ENV runcmd=$arg
RUN echo $runcmd
CMD echo $runcmd
# 改变arg，会不会改变 echo的值，会改变哪些值，如果修改这些值?
```



3、综合测试示例  

```dockerfile
FROM alpine
ARG arg1=22222
ENV arg2=1111111
ENV runcmd=$arg1
RUN echo $arg1 $arg2 $runcmd
CMD echo $arg1 $arg2 $runcmd
```





## 6 ADD和COPY

### 6.1 ADD

把上下文 ( Context ) 中指定的内容复制到镜像中, 严格来说 ADD 是添加, COPY 是复制, ADD 和 COPY 的功能几乎一样, 唯一的区别是 ADD 添加的内容是压缩文件,则添加到镜像中的时候会自动解压, 如果是远程文件会自动下载

如:

```bash
vi Dockerfile
```

```dockerfile
FROM alpine
ADD https://download.redis.io/redis-stable.tar.gz /dest/
RUN cd /dest && ls -al
```

注意:

1 `/dest/` 是指的镜像中的 alpine linux 系统中的 /dest 目录, 而不是宿主的目录, 

2 目标目录要写作 `/dest/` , 而不能是 `/dest`, `/dest`会使得下载文件重命名为 dest 放置在 `/` 目录下

3 `RUN cd /dest && ls -al` 不能写作两条 RUN 指令, 写作

```dockerfile
RUN cd /dest
RUN ls -al
```

那么最后展示的还是 `/` 目录的文件列表, 原因是 RUN 指令之间是没有上下文关联的, 每一个RUN指令都是独立的



```
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 transferring dockerfile: 129B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.1s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 https://download.redis.io/redis-stable.tar.gz
#5 DONE 1.1s

#5 https://download.redis.io/redis-stable.tar.gz
#5 CACHED

#6 [2/3] ADD https://download.redis.io/redis-stable.tar.gz /dest/
#6 DONE 0.0s

#7 [3/3] RUN cd /dest && ls -al
#7 0.264 total 3008
#7 0.264 drwxr-xr-x    2 root     root          4096 Aug  2 14:47 .
#7 0.264 drwxr-xr-x    1 root     root          4096 Aug  2 14:47 ..
#7 0.264 -rw-------    1 root     root       3071850 Jul 10 11:52 redis-stable.tar.gz
#7 DONE 0.3s

#8 exporting to image
#8 exporting layers 0.0s done
#8 writing image sha256:7095e7a3a570ce29a18cdbd347427a4886fcae1158e23fae9fc7aeca07ebfacc done
#8 naming to docker.io/library/demo:v1 done
#8 DONE 0.0s
demo
demo
```

可以看到远程文件已经自动下载并放在了 /dest 目录下了

```bash
#7 0.264 -rw-------    1 root     root       3071850 Jul 10 11:52 redis-stable.tar.gz
```



添加本地文件

首先下载文件

```bash
apt-get install -y wget
```

```bash
wget https://download.redis.io/redis-stable.tar.gz
```

```bash
vi Dockerfile
```

```dockerfile
FROM alpine
ADD *.tar.gz /dest/
RUN cd /dest && ls -al
```

```
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 transferring dockerfile: 92B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 2.0s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [internal] load build context
#5 transferring context: 3.07MB 0.0s done
#5 DONE 0.0s

#6 [2/3] ADD *.tar.gz /dest/
#6 DONE 0.8s

#7 [3/3] RUN cd /dest && ls -al
#7 0.393 total 12
#7 0.395 drwxr-xr-x    3 root     root          4096 Aug  2 15:01 .
#7 0.395 drwxr-xr-x    1 root     root          4096 Aug  2 15:01 ..
#7 0.395 drwxrwxr-x    8 1000     1000          4096 Jul 10 11:39 redis-stable
#7 DONE 0.4s

#8 exporting to image
#8 exporting layers 0.1s done
#8 writing image sha256:7996d5a15ef8a805255b4226d1a5a3cac8c5b426f7c414edc7f20ec30e42e34b
#8 writing image sha256:7996d5a15ef8a805255b4226d1a5a3cac8c5b426f7c414edc7f20ec30e42e34b done
#8 naming to docker.io/library/demo:v1 done
#8 DONE 0.1s
demo
demo
```

注意:

1 docker build 指令最后的 `.` 表示docker指令的工作目录指定为 `.`, 即当前目录, 也可以设置为其他任意目录, 如 `/` 或者 `/bin` 等, 当有 ADD/COPY 指令时, 要注意如果 ADD/COPY 的源文件使用相对路径, 那么源文件的相对路径是相对于工作目录的路径, 也就是相对于 docker build 的工作目录参数的路径,  如果这个Dockerfile在其他目录,如 `/`, 那么执行镜像构建会报错

```bash
cd /
cp /root/tmp/dockerfiles/Dockerfile Dockerfile
```

```
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:/# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 transferring dockerfile: 92B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.9s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [internal] load build context
#5 transferring context: 2B 7.9s done
#5 DONE 7.9s

#6 [2/3] ADD *.tar.gz /dest/
#6 DONE 0.0s

#7 [3/3] RUN cd /dest && ls -al
#7 0.290 /bin/sh: cd: line 0: can't cd to /dest: No such file or directory
#7 ERROR: process "/bin/sh -c cd /dest && ls -al" did not complete successfully: exit code: 2
------
 > [3/3] RUN cd /dest && ls -al:
line 0: can't cd to /dest: No such file or directory
------
Dockerfile:3
--------------------
   1 |     FROM alpine
   2 |     ADD *.tar.gz /dest/
   3 | >>> RUN cd /dest && ls -al
   4 |
--------------------
ERROR: failed to solve: process "/bin/sh -c cd /dest && ls -al" did not complete successfully: exit code: 2
```

原因就是在 `/`目录执行`docker build`指令时, 工作目录设置为 `.`也就是当前目录 `/`, 而在Dockerfile中 

```bash
ADD *.tar.gz /dest/
```

会在工作目录中寻找 `.tar.gz` 文件, 而在宿主的 `/`目录下没有任何`.tar.gz`文件, 就导致没有添加任何文件, 也就不会在镜像中自动创建 `/dest` 目录, 因此 cd 指令失败导致构建失败.

如果要在任意目录都可以执行使用这个Dockerfile通过 `docker build` 构建镜像, 那么需要在 `docker build` 指令中指定工作目录, 如下:

```
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache /root/tmp/dockerfiles && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:/# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache /root/tmp/dockerfiles && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 transferring dockerfile: 92B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 2.1s

#4 [1/3] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [internal] load build context
#5 transferring context: 43B done
#5 DONE 0.0s

#6 [2/3] ADD *.tar.gz /dest/
#6 DONE 0.8s

#7 [3/3] RUN cd /dest && ls -al
#7 0.311 total 12
#7 0.311 drwxr-xr-x    3 root     root          4096 Aug  2 15:16 .
#7 0.311 drwxr-xr-x    1 root     root          4096 Aug  2 15:16 ..
#7 0.311 drwxrwxr-x    8 1000     1000          4096 Jul 10 11:39 redis-stable
#7 DONE 0.3s

#8 exporting to image
#8 exporting layers
#8 exporting layers 0.1s done
#8 writing image sha256:1784654e861dc29a8add0915c16211f1a1f2892ccec7f878c05277cd25c1b076 done
#8 naming to docker.io/library/demo:v1 done
#8 DONE 0.1s
demo
demo
```

可以看到正常添加压缩文件并自动解压了

```bash
#7 0.311 drwxrwxr-x    8 1000     1000          4096 Jul 10 11:39 redis-stable
```



2 dockerfile中 `ADD *.tar.gz /dest/`表示把工作目录中的任意 `.tar.gz`结尾的文件添加到镜像的 `/dest` 目录下

3 压缩包已经自动解压

```bash
#7 0.395 drwxrwxr-x    8 1000     1000          4096 Jul 10 11:39 redis-stable
```





### 6.2 COPY

COPY命令和ADD的功能几乎一模一样, 唯一的区别是当添加的是压缩文件时, ADD命令会自动解压, 而 COPY命令是仅复制, 不自动解压



COPY的两种写法

```dockerfile
COPY [--chown=<user>:<group>] <src>... <dest>
COPY [--chown=<user>:<group>] ["<src>",... "<dest>"]
```



`--chown`功能仅在用于构建Linux容器的Dockerfiles上受支持，而在Windows容器上不起作用

COPY指令从 src 复制新文件或目录，并将它们添加到容器的文件系统中，路径为 dest 。

可以指定多个 src 资源，但是文件和目录的路径将被解释为相对于构建上下文的源。

每个 src 都可以包含通配符，并且匹配将使用Go的filepath.Match规则进行。



```dockerfile
COPY hom* /mydir/ #当前上下文，以home开始的所有资源
COPY hom?.txt /mydir/ # ?匹配单个字符
COPY test.txt relativeDir/ # 目标路径如果设置为相对路径，则相对与 WORKDIR 开始
# 把 “test.txt” 添加到 <WORKDIR>/relativeDir/
COPY test.txt /absoluteDir/ #也可以使用绝对路径，复制到容器指定位置
#所有复制的新文件都是uid(0)/gid(0)的用户，可以使用--chown改变
COPY --chown=55:mygroup files* /somedir/
COPY --chown=bin files* /somedir/
COPY --chown=1 files* /somedir/
COPY --chown=10:11 files* /somedir/
```



示例:

```dockerfile
FROM alpine

COPY *.txt /a.txt
RUN ls -al
RUN echo 222 >> a.txt
```

```bash
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 transferring dockerfile: 101B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 0.9s

#4 [1/4] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [internal] load build context
#5 transferring context: 26B done
#5 DONE 0.0s

#6 [2/4] COPY *.txt /a.txt
#6 DONE 0.0s

#7 [3/4] RUN ls -al
#7 0.288 total 68
#7 0.288 drwxr-xr-x    1 root     root          4096 Aug  3 03:59 .
#7 0.288 drwxr-xr-x    1 root     root          4096 Aug  3 03:59 ..
#7 0.288 -rw-r--r--    1 root     root             8 Aug  2 16:24 a.txt
#7 0.288 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 bin
#7 0.288 drwxr-xr-x    5 root     root           340 Aug  3 03:59 dev
#7 0.288 drwxr-xr-x    1 root     root          4096 Aug  3 03:59 etc
#7 0.288 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 home
#7 0.288 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 lib
#7 0.288 drwxr-xr-x    5 root     root          4096 Jun 14 15:03 media
#7 0.288 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 mnt
#7 0.288 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 opt
#7 0.288 dr-xr-xr-x  413 root     root             0 Aug  3 03:59 proc
#7 0.288 drwx------    2 root     root          4096 Jun 14 15:03 root
#7 0.288 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 run
#7 0.288 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 sbin
#7 0.288 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 srv
#7 0.288 dr-xr-xr-x   13 root     root             0 Aug  3 03:59 sys
#7 0.288 drwxrwxrwt    2 root     root          4096 Jun 14 15:03 tmp
#7 0.288 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 usr
#7 0.288 drwxr-xr-x   12 root     root          4096 Jun 14 15:03 var
#7 DONE 0.3s

#8 [4/4] RUN echo 222 >> a.txt
#8 DONE 0.5s

#9 exporting to image
#9 exporting layers 0.0s done
#9 writing image sha256:639430365aca98dab08f495bc540371c978e4ebb433c4c82ffee3a38df3be8db
#9 writing image sha256:639430365aca98dab08f495bc540371c978e4ebb433c4c82ffee3a38df3be8db done
#9 naming to docker.io/library/demo:v1 done
#9 DONE 0.0s
demo
demo
```

可以正常复制和修改, 但是配合 USER 指令使用指定的用户身份进行操作:

```dockerfile
FROM alpine

RUN addgroup -S abc && adduser -S abc -G abc
USER abc:abc

COPY *.txt /a.txt
RUN ls -al
RUN echo 222 >> a.txt
```

```bash
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 transferring dockerfile: 160B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 2.0s

#4 [1/5] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [internal] load build context
#5 transferring context: 26B done
#5 DONE 0.0s

#6 [2/5] RUN addgroup -S abc && adduser -S abc -G abc
#6 DONE 0.6s

#7 [3/5] COPY *.txt /a.txt
#7 DONE 0.0s

#8 [4/5] RUN ls -al
#8 0.427 total 68
#8 0.428 drwxr-xr-x    1 root     root          4096 Aug  3 04:17 .
#8 0.428 drwxr-xr-x    1 root     root          4096 Aug  3 04:17 ..
#8 0.428 -rw-r--r--    1 root     root             8 Aug  2 16:24 a.txt
#8 0.428 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 bin
#8 0.428 drwxr-xr-x    5 root     root           340 Aug  3 04:17 dev
#8 0.428 drwxr-xr-x    1 root     root          4096 Aug  3 04:17 etc
#8 0.428 drwxr-xr-x    1 root     root          4096 Aug  3 04:17 home
#8 0.428 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 lib
#8 0.428 drwxr-xr-x    5 root     root          4096 Jun 14 15:03 media
#8 0.428 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 mnt
#8 0.428 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 opt
#8 0.428 dr-xr-xr-x  411 root     root             0 Aug  3 04:17 proc
#8 0.428 drwx------    2 root     root          4096 Jun 14 15:03 root
#8 0.428 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 run
#8 0.428 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 sbin
#8 0.428 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 srv
#8 0.428 dr-xr-xr-x   13 root     root             0 Aug  3 04:17 sys
#8 0.428 drwxrwxrwt    2 root     root          4096 Jun 14 15:03 tmp
#8 0.428 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 usr
#8 0.428 drwxr-xr-x   12 root     root          4096 Jun 14 15:03 var
#8 DONE 0.5s

#9 [5/5] RUN echo 222 >> a.txt
#9 0.377 /bin/sh: can't create a.txt: Permission denied
#9 ERROR: process "/bin/sh -c echo 222 >> a.txt" did not complete successfully: exit code: 1
------
 > [5/5] RUN echo 222 >> a.txt:
0.377 /bin/sh: can't create a.txt: Permission denied
------
Dockerfile:8
--------------------
   6 |     COPY *.txt /a.txt
   7 |     RUN ls -al
   8 | >>> RUN echo 222 >> a.txt
   9 |
--------------------
ERROR: failed to solve: process "/bin/sh -c echo 222 >> a.txt" did not complete successfully: exit code: 1
```

可以看到提示新创建的用户权限不足

```bash
#9 0.377 /bin/sh: can't create a.txt: Permission denied
```

因此需要在复制文件时, 通过 `--chown` 参数自动修改文件的所有者

```dockerfile
FROM alpine

RUN addgroup -S abc && adduser -S abc -G abc
USER abc:abc

COPY --chown=abc:abc *.txt /a.txt
RUN ls -al
RUN echo 222 >> a.txt
```

```bash
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache . && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load build definition from Dockerfile
#1 transferring dockerfile: 178B done
#1 DONE 0.0s

#2 [internal] load .dockerignore
#2 transferring context: 2B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 0.9s

#4 [1/5] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [internal] load build context
#5 transferring context: 26B done
#5 DONE 0.0s

#6 [2/5] RUN addgroup -S abc && adduser -S abc -G abc
#6 DONE 0.3s

#7 [3/5] COPY --chown=abc:abc *.txt /a.txt
#7 DONE 0.0s

#8 [4/5] RUN ls -al
#8 0.367 total 68
#8 0.367 drwxr-xr-x    1 root     root          4096 Aug  3 04:21 .
#8 0.367 drwxr-xr-x    1 root     root          4096 Aug  3 04:21 ..
#8 0.367 -rw-r--r--    1 abc      abc              8 Aug  2 16:24 a.txt
#8 0.367 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 bin
#8 0.367 drwxr-xr-x    5 root     root           340 Aug  3 04:21 dev
#8 0.367 drwxr-xr-x    1 root     root          4096 Aug  3 04:21 etc
#8 0.367 drwxr-xr-x    1 root     root          4096 Aug  3 04:21 home
#8 0.367 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 lib
#8 0.367 drwxr-xr-x    5 root     root          4096 Jun 14 15:03 media
#8 0.367 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 mnt
#8 0.367 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 opt
#8 0.367 dr-xr-xr-x  418 root     root             0 Aug  3 04:21 proc
#8 0.367 drwx------    2 root     root          4096 Jun 14 15:03 root
#8 0.367 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 run
#8 0.367 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 sbin
#8 0.367 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 srv
#8 0.367 dr-xr-xr-x   13 root     root             0 Aug  3 04:21 sys
#8 0.367 drwxrwxrwt    2 root     root          4096 Jun 14 15:03 tmp
#8 0.367 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 usr
#8 0.367 drwxr-xr-x   12 root     root          4096 Jun 14 15:03 var
#8 DONE 0.4s

#9 [5/5] RUN echo 222 >> a.txt
#9 DONE 0.4s

#10 exporting to image
#10 exporting layers 0.0s done
#10 writing image sha256:27a92bf8cf7517561b6707b38cd56a1966f8344ae9731f1fcb08905a7120efe5 done
#10 naming to docker.io/library/demo:v1 done
#10 DONE 0.0s
demo
demo
```

可以正常的复制文件, 并且文件的所有者为指定的用户

```bash
#8 0.367 -rw-r--r--    1 abc      abc              8 Aug  2 16:24 a.txt
```



2、ADD

同COPY用法，不过 ADD拥有自动下载远程文件和解压的功能。

注意：

src 路径必须在构建的上下文中； 不能使用 ../something /something 这种方式，因为docker构建的第一步是将上下文目录（和子目录）发送到docker守护程序。

如果 src 是URL，并且 dest 不以斜杠结尾，则从URL下载文件并将其复制到 dest 。

如果 dest 以斜杠结尾，将自动推断出url的名字（保留最后一部分），保存到 dest如果 src 是目录，则将复制目录的整个内容，包括文件系统元数据。





## 7 WORKDIR和VOLUME  

### 7.1 WORKDIR

WORKDIR指令为Dockerfile中跟随它的所有 RUN，CMD，ENTRYPOINT，COPY，ADD 指令设置工作目录。 如果WORKDIR不存在，即使以后的Dockerfile指令中未使用它也将被创建。

WORKDIR指令可在Dockerfile中多次使用。 如果提供了相对路径，则它将相对于上一个WORKDIR指令的路径。 例如：

```
WORKDIR /a
WORKDIR b
WORKDIR c
RUN pwd
#结果 /a/b/c
```

也可以用到环境变量  

```
ENV DIRPATH=/path
WORKDIR $DIRPATH/$DIRNAME
RUN pwd
#结果 /path/$DIRNAME
```



示例

```bash
echo 111 >> 1.txt
```

```bash
vi Dockerfile
```

```dockerfile
FROM alpine

RUN pwd && ls -al
WORKDIR /app
RUN pwd && ls -al
COPY *.txt ./
RUN pwd && ls -al
```

```bash
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache /root/tmp/dockerfiles && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache /root/tmp/dockerfiles && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 transferring dockerfile: 131B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 1.9s

#4 [1/6] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [internal] load build context
#5 transferring context: 36B done
#5 DONE 0.0s

#6 [2/6] RUN pwd && ls -al
#6 0.293 /
#6 0.294 total 64
#6 0.294 drwxr-xr-x    1 root     root          4096 Aug  2 15:41 .
#6 0.294 drwxr-xr-x    1 root     root          4096 Aug  2 15:41 ..
#6 0.294 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 bin
#6 0.294 drwxr-xr-x    5 root     root           340 Aug  2 15:41 dev
#6 0.294 drwxr-xr-x    1 root     root          4096 Aug  2 15:41 etc
#6 0.294 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 home
#6 0.294 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 lib
#6 0.294 drwxr-xr-x    5 root     root          4096 Jun 14 15:03 media
#6 0.294 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 mnt
#6 0.294 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 opt
#6 0.294 dr-xr-xr-x  391 root     root             0 Aug  2 15:41 proc
#6 0.294 drwx------    2 root     root          4096 Jun 14 15:03 root
#6 0.294 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 run
#6 0.294 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 sbin
#6 0.294 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 srv
#6 0.294 dr-xr-xr-x   13 root     root             0 Aug  2 15:41 sys
#6 0.294 drwxrwxrwt    2 root     root          4096 Jun 14 15:03 tmp
#6 0.294 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 usr
#6 0.294 drwxr-xr-x   12 root     root          4096 Jun 14 15:03 var
#6 DONE 0.3s

#7 [3/6] WORKDIR /app
#7 DONE 0.0s

#8 [4/6] RUN pwd && ls -al
#8 0.345 /app
#8 0.345 total 8
#8 0.345 drwxr-xr-x    2 root     root          4096 Aug  2 15:41 .
#8 0.345 drwxr-xr-x    1 root     root          4096 Aug  2 15:41 ..
#8 DONE 0.4s

#9 [5/6] COPY *.txt ./
#9 DONE 0.0s

#10 [6/6] RUN pwd && ls -al
#10 0.563 /app
#10 0.564 total 12
#10 0.564 drwxr-xr-x    1 root     root          4096 Aug  2 15:41 .
#10 0.564 drwxr-xr-x    1 root     root          4096 Aug  2 15:41 ..
#10 0.564 -rw-r--r--    1 root     root             4 Aug  2 15:41 1.txt
#10 DONE 0.6s

#11 exporting to image
#11 exporting layers 0.0s done
#11 writing image sha256:b072db5bcdc04b2d5c36f88aa3bdfd7025f4f9d8a42ef9b7b9d3ed784e6eb1a8
#11 writing image sha256:b072db5bcdc04b2d5c36f88aa3bdfd7025f4f9d8a42ef9b7b9d3ed784e6eb1a8 done
#11 naming to docker.io/library/demo:v1 done
#11 DONE 0.0s
demo
demo
```

可以看到第一个RUN指令在 `/`目录执行, 第二个 RUN 指令在  `/app`  目录执行, COPY指定将宿主机中的当前目录的 *.txt 复制到了镜像的 `./`, 是相对于工作目录的当前目录, 此时工作目录已经是 /app, 所以COPY和第三个 RUN 指令还是在 /app 目录下执行

也就是说 WORKDIR 会为后续所有的指令指定镜像内的工作目录, 注意 `docker build` 的工作目录参数指定的是宿主机中的目录 . 用于为COPY/ADD指令指定源文件提供基础目录



WORKDIR的嵌套

```dockerfile
FROM alpine

RUN pwd && ls -al
WORKDIR /app
WORKDIR abc
RUN pwd && ls -al
COPY *.txt ./
RUN pwd && ls -al
```

```bash
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache /root/tmp/dockerfiles && docker run --name demo demo:v1 && docker stop demo && docker rm demo
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache /root/tmp/dockerfiles && docker run --name demo demo:v1 && docker stop demo && docker rm demo
#0 building with "default" instance using docker driver

#1 [internal] load .dockerignore
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 transferring dockerfile: 143B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/alpine:latest
#3 DONE 2.2s

#4 [1/7] FROM docker.io/library/alpine@sha256:82d1e9d7ed48a7523bdebc18cf6290bdb97b82302a8a9c27d4fe885949ea94d1
#4 CACHED

#5 [internal] load build context
#5 transferring context: 26B done
#5 DONE 0.0s

#6 [2/7] RUN pwd && ls -al
#6 0.283 /
#6 0.284 total 64
#6 0.284 drwxr-xr-x    1 root     root          4096 Aug  2 15:50 .
#6 0.284 drwxr-xr-x    1 root     root          4096 Aug  2 15:50 ..
#6 0.284 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 bin
#6 0.284 drwxr-xr-x    5 root     root           340 Aug  2 15:50 dev
#6 0.284 drwxr-xr-x    1 root     root          4096 Aug  2 15:50 etc
#6 0.284 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 home
#6 0.284 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 lib
#6 0.284 drwxr-xr-x    5 root     root          4096 Jun 14 15:03 media
#6 0.284 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 mnt
#6 0.284 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 opt
#6 0.284 dr-xr-xr-x  390 root     root             0 Aug  2 15:50 proc
#6 0.284 drwx------    2 root     root          4096 Jun 14 15:03 root
#6 0.284 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 run
#6 0.284 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 sbin
#6 0.284 drwxr-xr-x    2 root     root          4096 Jun 14 15:03 srv
#6 0.284 dr-xr-xr-x   13 root     root             0 Aug  2 15:50 sys
#6 0.284 drwxrwxrwt    2 root     root          4096 Jun 14 15:03 tmp
#6 0.284 drwxr-xr-x    7 root     root          4096 Jun 14 15:03 usr
#6 0.284 drwxr-xr-x   12 root     root          4096 Jun 14 15:03 var
#6 DONE 0.3s

#7 [3/7] WORKDIR /app
#7 DONE 0.0s

#8 [4/7] WORKDIR abc
#8 DONE 0.1s

#9 [5/7] RUN pwd && ls -al
#9 0.333 /app/abc
#9 0.334 total 8
#9 0.334 drwxr-xr-x    2 root     root          4096 Aug  2 15:50 .
#9 0.334 drwxr-xr-x    1 root     root          4096 Aug  2 15:50 ..
#9 DONE 0.3s

#10 [6/7] COPY *.txt ./
#10 DONE 0.0s

#11 [7/7] RUN pwd && ls -al
#11 0.383 /app/abc
#11 0.383 total 16
#11 0.383 drwxr-xr-x    1 root     root          4096 Aug  2 15:50 .
#11 0.383 drwxr-xr-x    1 root     root          4096 Aug  2 15:50 ..
#11 0.383 -rw-r--r--    1 root     root             4 Aug  2 15:41 1.txt
#11 DONE 0.4s

#12 exporting to image
#12 exporting layers 0.0s done
#12 writing image sha256:ffb68ed57802c2c95c30374de53d075f787665b883fe705505306156c500efa8 done
#12 naming to docker.io/library/demo:v1 done
#12 DONE 0.0s
demo
demo
```

可以看到第二个 WORKDIR 指令在前一个 WORKDIR 指令的基础上使用相对路径的方式又自动创建了 abc 目录



通过 `docker exec -it`   进入容器时的默认路径是 WORKDIR  指定的路径, 如果没有 WORKDIR 指令, 那么默认路径是 `/`

```dockerfile
FROM alpine

WORKDIR /app/abc
CMD ping baidu.com
```

```bash
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache /root/tmp/dockerfiles && docker run --name demo -d demo:v1
```

```bash
docker exec -it demo /bin/sh
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker exec -it demo /bin/sh
/app/abc #
```

```
docker stop demo && docker rm demo
```



### 7.2 VOLUME  

作用：把容器的某些文件夹映射到主机外部

写法：  

```
VOLUME ["/var/log/"] #可以是JSON数组
VOLUME /var/log #可以直接写
VOLUME /var/log /var/db #可以空格分割多个
```

注意：

用 VOLUME 声明了卷，那么以后对于卷内容的修改会被丢弃，所以， 一定在volume声明之前修改内容 ；

示例:

```dockerfile
FROM alpine
VOLUME ["/hello", "/app"]
CMD ping baidu.com
```

1 VOLUME 命令指定的路径如果在镜像中不存在会自动创建

2 Dockerfile中使用了VOLUME 指令, 那么在启动镜像时即使没有使用 `-v` 参数也会使用自动匿名卷挂载



注意通过 VOLUME 指令挂载的文件和目录, 后续的修改可以保存

```dockerfile
FROM alpine

RUN mkdir /hello /app && echo 111 >> /hello/a.txt && echo 222 >> /app/b.txt
VOLUME ["/hello", "/app"]
RUN echo 333 >> /hello/a.txt && echo 444 >> /app/b.txt
CMD ping baidu.com
```

```bash
docker build -t demo:v1 -f Dockerfile --progress=plain --no-cache /root/tmp/dockerfiles && docker run --name demo -d demo:v1
```

进入容器查看 /hello/a.txt 和 /app/b.txt

```bash
docker exec -it demo /bin/sh
```

```bash
root@ubuntu-dev:~/tmp/dockerfiles# docker exec -it demo /bin/sh
/ # cat /hello/a.txt
111
333
/ # cat /app/b.txt
222
444
```

```bash
docker stop demo && docker rm demo
```



1 不建议在VOLUME 之后对已经挂载出去的文件进行修改

2 如果对将要挂载出去的文件进行修改, 建议将 VOLUME 指令放在文件修改之后执行



volume 的应用场景:

如: 将 log 目录默认挂载出去

```dockerfile
VOLUME ["/log"]
```



## 8 USER

写法：

```
USER <user>[:<group>]
USER <UID>[:<GID>]
```

USER指令设置运行映像时要使用的用户名（或UID）以及可选的用户组（或GID），以及Dockerfile中USER后面所有RUN，CMD和ENTRYPOINT指令。

示例:

```dockerfile
FROM alpine

RUN addgroup -S abc && adduser -S abc -G abc
USER abc:abc

COPY --chown=abc:abc *.txt /a.txt
RUN ls -al
RUN echo 222 >> a.txt
```

在alpine中使用下面的指令创建用户群组和用户

```bash
addgroup -S abc && adduser -S abc -G abc
```



## 9 EXPOSE

EXPOSE指令通知Docker容器在运行时在指定的网络端口上进行侦听。 可以指定端口是侦听TCP还是UDP，如果未指定协议，则默认值为TCP。

EXPOSE指令实际上不会发布端口。 它充当构建映像的人员和运行容器的人员之间的一种文档，即有关打算发布哪些端口的信息。 要在运行容器时实际发布端口，请在docker run上使用-p标志发布并映射一个或多个端口，或使用-P标志发布所有公开的端口并将其映射到高阶端口。

```
EXPOSE <port> [<port>/<protocol>...]
EXPOSE [80,443]
EXPOSE 80/tcp
EXPOSE 80/udp
```



1 EXPOSE 指令只是一个声明, 对容器本身没有产生任何影响, 主要作用的是给开发者作为参考, 如果容器内部的程序没有监听 EXPOSE 指令指定的端口, 那么这个端口可以访问, 但是不会有任何返回, 容器内的程序监听某个端口, 如8080, 即使没有通过 EXPOSE 指令暴露, 也仍然可以通过 `docker run -p 58080:8080` 这样的方式手动绑定端口进行访问 . 

2 `docker run -P` 指令指定随机端口时会为每一个 EXPOPSE 暴露的端口自动绑定一个宿主机端口

如果容器内部有监听端口, 建议都写上 EXPOSE 指令



## 10 multi-stage builds

示例场景:java项目镜像

```dockerfile
FROM alpine
RUN "安装maven"
RUN "安装jdk/jre"
RUN mvn clean package
RUN cp xx.jar /app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
```

这样的镜像会很大, java项目最轻量的环境是jre, 也可以手动打包,这样比较麻烦, 可以通过多阶段构建整个流程自动化执行

多阶段构建, 一个镜像分为多个大的阶段进行构建, 最终的构建结果是最后一个阶段的结果

```dockerfile
# 第一阶段构建打包
FROM alpine AS build
xxx

# 第二阶段执行
FROM jre
# 从第一阶段将构建的构建结果, 如可执行文件或者 jar 包等
COPY --from=build xxx.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

看起来有2个 FROM 指令, 但是最终打包的之后JRE的镜像包, 第一个阶段只是辅助阶段



示例

```dockerfile
# 第一阶段, 构建jar包, 第二阶段要引用第一阶段, 所以需要给这个阶段起一个名字 这里叫做 build
FROM maven:3.6.1-jdk-8-alpine AS build
RUN mvn -v

WORKDIR /app
COPY pom.xml .
COPY src .
RUN mvn clean package -Dmaven.test.skip=true
RUN pwd && ls -l

RUN cp target/*.jar app.jar
RUN ls -al
# 第一阶段结束, jar包已经制作好了

# 第二阶段, 运行jar包, 只需要一个jre就可以了
FROM JRE
ENV JAVA_OPTS="-Xmx1024m -Xms128m"
ENV PARAMS="--server.port=8080 --spring.profiles.active=prod"

#修改时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

# 从指定的阶段复制文件
# COPY --from=<stage-name> <src-file> <target-file>
COPY --from=build /app/app.jar /app.jar

ENTRYPOINT ["/bin/sh","-c","java ${JAVA_OPTS} -Dfile.encoding=utf8 -Duser.timezone=Asia/Shanghai -Djava.security.egd=file:/dev/./urandom -jar /app.jar ${PARAMS}"]
```

多阶段构建可以减少镜像的体积, 前面构建阶段需要的maven环境在最终的镜像中是不存在的





一个自动化的多阶段构建流程

1 通过git从代码仓库下载指定的代码

2 把项目自动打包,制作镜像

3 运行镜像即可



多阶段构建  

1、使用

https://docs.docker.com/develop/develop-images/multistage-build/

解决：如何让一个镜像变得更小; 多阶段构建的典型示例

```dockerfile
### 我们如何打包一个Java镜像
FROM maven
WORKDIR /app
COPY . .
RUN mvn clean package
COPY /app/target/*.jar /app/app.jar
ENTRYPOINT java -jar app.jar
## 这样的镜像有多大？
## 我们最小做到多大？？
```



2、生产示例  



```
#以下所有前提 保证Dockerfile和项目在同一个文件夹  
# 第一阶段：环境构建;
FROM maven:3.5.0-jdk-8-alpine AS builder
WORKDIR /app
ADD ./ /app
RUN mvn clean package -Dmaven.test.skip=true
# 第二阶段，最小运行时环境，只需要jre
FROM openjdk:8-jre-alpine
# 修改时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
LABEL maintainer="534096094@qq.com"
# 从上一个阶段复制内容
COPY --from=builder /app/target/*.jar /app.jar
ENV JAVA_OPTS=""
ENV PARAMS=""
# 运行jar包
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom
$JAVA_OPTS -jar /app.jar $PARAMS" ]
```



```xml
<!--为了加速下载需要在pom文件中复制如下 -->
<repositories>
<repository>
<id>aliyun</id>
<name>Nexus Snapshot Repository</name>
<url>https://maven.aliyun.com/repository/public</url>
<layout>default</layout>
<releases>
<enabled>true</enabled>
</releases>
<!--snapshots默认是关闭的,需要开启 -->
<snapshots>
<enabled>true</enabled>
</snapshots>
</repository>
</repositories>
<pluginRepositories>
<pluginRepository>
<id>aliyun</id>
<name>Nexus Snapshot Repository</name>
<url>https://maven.aliyun.com/repository/public</url>
<layout>default</layout>
<releases>
<enabled>true</enabled>
</releases>
<snapshots>
<enabled>true</enabled>
</snapshots>
</pluginRepository>
</pluginRepositories>
```



## 11 Images瘦身实践  



1 选择最小的基础镜像

2 合并RUN环节的所有指令，少生成一些层

3 RUN期间可能安装其他程序会生成临时缓存，要自行删除。如：

开发阶段, 逐层验证是否正确

```dockerfile
RUN xxx
RUN xxx
RUN xxx
```

在开发验证完成后, 把多行的RUN指令合并

```dockerfile
RUN apt-get update && apt-get install -y \
bzr \
cvs \
git \
mercurial \
subversion \
&& rm -rf /var/lib/apt/lists/*
```

4 使用 .dockerignore 文件，排除上下文中无需参与构建的资源, 类似于 gitignore

```bash
vi .dockerignore
```

```bash
*.impl
/target/*
```



5 使用多阶段构建

6 合理使用构建缓存加速构建。--no-cache

学习更多Dockerfile的写法：https://github.com/docker-library/  



springboot java 最终写法  

```dockerfile
# 考虑线上排错 监控等可以使用
FROM openjdk:8-jre-alpine
LABEL maintainer="my-name"

COPY target/*.jar /app.jar

# 设置时区
# touch /app.jar 不会修改文件内容, 只会更新文件的最后修改时间, 便于排查问题时方便根据复制的时间追溯jar包
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo'Asia/Shanghai' >/etc/timezone && touch /app.jar


# docker run -e JAVA_OPTS="xxxx" -e PARAMS="xxx" image-name:v1.0
ENV JAVA_OPTS=""
ENV PARAMS=""

ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom $JAVA_OPTS -jar /app.jar $PARAMS" ]

# 运行命令 docker run -e JAVA_OPTS="-Xmx512m -Xms33 -" -e PARAMS="--
spring.profiles=dev --server.port=8080" -jar /app/app.jar
```

