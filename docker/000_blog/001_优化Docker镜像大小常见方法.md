# 优化 Docker 镜像大小常见方法

平时我们构建的 Docker 镜像通常比较大，占用大量的磁盘空间，随着容器的大规模部署，同样也会浪费宝贵的带宽资源。本文将介绍几种常用的方法来优化 Docker 镜像大小，这里我们使用 Docker Hub 官方上的 Redis 镜像进行说明。

## 手动**管理**

我们能够直接想到的方法就是直接修改官方的 Redis 镜像 Dockerfile 文件，手动删除容器运行后不需要的组件，然后重新构建一个新镜像。这种方法理论上是可行的，但是容易出错，而且效果也不是特别明显。主要是不能和官方的镜像实时同步。

## **多阶段构建**

Docker 在17.05 版本起提供了多阶段构建的功能来解决这个问题，这种方法是通过丢弃中间层来实现的，并通过中间层来提供有关如何创建最终镜像及其内容信息来完成的，只需要保留容器化应用所需的组件即可。在更上层的实现如下所示：

- 以一些镜像作为构建的基础

- 和平常一样运行命令来构造你的应用文章来源地址https://www.yii666.com/article/690349.html?action=onAll

- 将所需的制品复制到另外一个单独的镜像

## Distroless

在严重依赖容器化技术，尤其是 Docker 之后，谷歌早就意识到了使用臃肿镜像的弊端。所以他们提供了自己的方法来解决这个问题，即 distroless 镜像。与典型的Linux 基础镜像（绑定了很多软件）不同，在 distroless 上对你的应用进行 docker化，最终的镜像只包含应用及其运行时的依赖项，大多数 Linux 发行版中包含的标准软件，如包管理器，甚至 shell 都被会被排除在外。[文章来源地址https://www.yii666.com/article/690349.html?action=onAll](https://www.yii666.com/article/690349.html?action=onAll)

同样的，要使用 Google 的 distroless 镜像，需要使用上面我们提到的多阶段构建，如下所示：

```
FROM redis:latest AS build
ARG TIME_ZONE
RUN mkdir -p /opt/etc && \
    cp -a --parents /lib/x86_64-linux-gnu/libm.so.* /opt && \
    cp -a --parents /lib/x86_64-linux-gnu/libdl.so.* /opt && \
    cp -a --parents /lib/x86_64-linux-gnu/libpthread.so.* /opt && \
    cp -a --parents /lib/x86_64-linux-gnu/libc.so.* /opt && \
    cp -a --parents /usr/local/bin/redis-server /opt && \
    cp -a --parents /usr/local/bin/redis-sentinel /opt && \
    cp /usr/share/zoneinfo/${TIME_ZONE:-UTC} /opt/etc/localtime

FROM gcr.io/distroless/base
COPY --from=build /opt /
VOLUME /data
WORKDIR /data
ENTRYPOINT ["redis-server"]
```

使用`redis:latest`为基础镜像，然后保留需要的一些二进制文件（`redis-server`二进制文件以及所有的相关依赖），然后使用 distroless 镜像作为构建的最终镜像的基础，将`opt`目录内容复制到该镜像目录中来。

然后我们只需要重新构建镜像即可：

```
$ docker build -t redis:distroless .
$ docker images
REPOSITORY        TAG                 IMAGE ID                   CREATED             SIZE
redis                        distroless     7d50bd873bea        15 seconds ago      28.2MB
redis                        latest              1319b1eaa0b7        3 days ago          104MB
```

我们可以看到镜像由以前的 104MB 变成了 28.2MB，大大降低了镜像的大小。

> 注意：在 Linux 下面我们可以使用 ldd 工具来查找指定的二进制文件所需要的依赖，比如
> `$ ldd $(which redis-server)` 。

使用 distroless 镜像来降低 Docker 镜像的大小是一个非常有效的方法，但是这样做也有一个明显的缺点就是最终的镜像中没有 shell 程序了，使得调试 Docker 容器就非常非常困难，当然这样也降低了应用被攻击的危险，使其更加安全，如果我们将应用部署到 Kubernetes 集群的话，我们可以利用 `kubectl-debug `这样的工具来辅助调试应用。

## Alpine **Linux**

另外一种比较常见的方式是选择在 Alpine Linux 基础上构建应用镜像，Alpine Linux 是一个特别适合创建最小化 Docker 镜像的发行版。Apline Linux 使用较小的 musl C 库代替 glibc，并将其静态链接，这意味着针对 musl 编译的程序将变成可重定位的 （relocatable）的二进制文件，从而无需包含共享对象，从而可以显著降低镜像的大小。

`redis:alpine` 镜像大概为 30MB 左右，这样做的缺点是，通常 musl 的性能不如 glibc。当然也有另外一个好处，那就是和上面的 distroless 相比，Alpine 是成熟的 Linux 发行版，提供基本的 shell 访问，使得调试 Docker 容器应用更为方便。在 Docker Hub 上面也可以找到几乎所有流行软件的 Alpine 版本，比如 Redis、Nginx、MySQL 等等。

## **GNU Guix**

最后，我们可以使用 GNU Guix，一个多功能的软件包管理工具，其中就有一项可以创建 Docker 镜像的功能。Guix 区分了包的运行时依赖与构建依赖，所以 Guix 构建的 Docker 镜像将只包含明确指定的程序，加上他们的运行时依赖，就像 distroless 的方法一样。但和 distroless 不同的时候，distroless 需要你自己去查程序的运行时依赖关系（当然也要写 Dockerfile），而 Guix 只需要运行一条命令即可：`$ guix pack -f docker redis` 。

通过上面的命令创建的 Redis 镜像大小约为 70MB，和原本的镜像相比有明显的减少，虽然比 distroless 和 Alpine 方法创建的镜像稍大，但使用 Guinx 确实提供了一些其他的优点。比如，如果你想让你的最终镜像也包含一个 shell，以便像 Alpine 那样去调试，那么只需要在 Guxi 打包的时候指定上就可以了：`$ guix pack -f docker redis bash` ，如果你想包含其他软件，也可以继续在后面添加即可。

Guix 的功能特性意味着包的构建可以100%复用，所以我们可以在 CI/CD 流水线管道中加入 Guix 支持，这样构建过程就非常顺畅了。

有的人可能会觉得 Guix 听起来很酷，但是并不想为了构建更小的 Docker 镜像而去下载安装另外一个工具，更何况 Guix 只在 Linux 下面工作，很多开发者还是 MacOS 用户，去配置 Guix 也挺麻烦。其实这点并不用担心，Guix 本身也有 Docker 镜像在 Docker Hub 上，所以使用起来也并不会太复杂，只需要简单的使用 `$ docker run guix` 命令即可。

除了 Guix 之外，值得一提的还有一个名为 Nix 的软件包管理工具，对 Guix 所述的每一点都同样有效并且适用于 Nix。https://www.yii666.com/

## 原文链接

- https://docs.docker.com/develop/develop-images/multistage-build/文章来源地址:https://www.yii666.com/article/690349.html?action=onAll**文章来源地址:https://www.yii666.com/article/690349.html?action=onAll**

- https://github.com/GoogleContainerTools/distroless

- https://alpinelinux.org/

- http://guix.gnu.org/
- https://appfleet.com/blog/methods-optimize-docker-image-size/



### 优化 Docker 镜像大小常见方法-相关文章

1. docker nginx-php容器镜像瘦身优化

   

2. 优化 Docker 镜像大小常见方法

   

3. NodeJS 服务 Docker 镜像极致优化指北

   

4. Docker容器技术-优化Docker镜像

   

5. Dockerfile 自动制作 Docker 镜像（三）—— 镜像的分层与 Dockerfile 的优化

   

6. Docker系列03-容器Docker镜像的使用

   

7. Docker 镜像针对不同语言的精简策略

   

8. [阿里mysql同步工具otter的docker镜像](https://www.yii666.com/article/690343.html)