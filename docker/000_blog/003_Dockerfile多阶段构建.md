# Dockerfile多阶段构建

------

[Docker](https://so.csdn.net/so/search?q=Docker&spm=1001.2101.3001.7020) 17.05版本以后，新增了Dockerfile多阶段构建。所谓多阶段构建，实际上是允许一个Dockerfile 中出现多个 `FROM` 指令。这样做有什么意义呢？

> 老版本Docker中为什么不支持多个 FROM 指令

在17.05版本之前的Docker，只允许Dockerfile中出现一个FROM指令，这得从镜像的本质说起。

你可以简单理解Docker的镜像是一个压缩文件，其中包含了你需要的程序和一个文件系统。其实这样说是不严谨的，Docker镜像并非只是一个文件，而是由一堆文件组成，最主要的文件是**层**。

Dockerfile 中，大多数指令会生成一个层，比如下方的两个例子：

```cobol
# 示例一，foo 镜像的Dockerfile



# 基础镜像中已经存在若干个层了



FROM ubuntu:16.04



 



# RUN指令会增加一层，在这一层中，安装了 git 软件



RUN apt-get update \



  && apt-get install -y --no-install-recommends git \



  && apt-get clean \



  && rm -rf /var/lib/apt/lists/*
# 示例二，bar 镜像的Dockerfile



FROM foo



 



# RUN指令会增加一层，在这一层中，安装了 nginx



RUN apt-get update \



  && apt-get install -y --no-install-recommends nginx \



  && apt-get clean \



  && rm -rf /var/lib/apt/lists/*
```

假设基础镜像 `ubuntu:16.04` 已经存在5层，使用第一个Dockerfile打包成镜像 foo，则foo有6层，又使用第二个Dockerfile打包成镜像bar，则bar中有7层。

如果 `ubuntu:16.04` 等其他镜像不算，如果系统中只存在 foo 和 bar 两个镜像，那么系统中一共保存了多少层呢？

是7层，并非13层，这是因为，foo和bar共享了6层。层的共享机制可以节约大量的磁盘空间和传输带宽，比如你本地已经有了foo镜像，又从镜像仓库中拉取bar镜像时，只拉取本地所没有的最后一层就可以了，不需要把整个bar镜像连根拉一遍。但是层共享是怎样实现的呢？

原来，Docker镜像的每一层只记录文件变更，在容器启动时，***\*Docker会将镜像的各个层进行计算，最后生成一个文件系统，这个被称为联合挂载。\****对此感兴趣的话可以进一步了解一下 `AUFS`。

Docker的各个层是有相关性的，在联合挂载的过程中，系统需要知道在什么样的基础上再增加新的文件。那么这就要求一个Docker镜像只能有一个起始层，只能有一个根。所以，Dockerfile中，就只允许一个 `FROM` 指令。因为多个 `FROM` 指令会造成多根，则是无法实现的。但为什么 Docker 17.05 版本以后允许 Dockerfile支持多个 `FROM` 指令了呢，莫非已经支持了多根？

 

## 多个 FROM 指令的意义

------

 多个 FROM 指令并不是为了生成多根的层关系，最后生成的镜像，仍以最后一条 FROM 为准，之前的 FROM 会被抛弃，那么之前的FROM 又有什么意义呢？

每一条 FROM 指令都是一个构建阶段，多条 FROM 就是多阶段构建，虽然最后生成的镜像只能是最后一个阶段的结果，但是，***\*能够将前置阶段中的文件拷贝到后边的阶段中，这就是多阶段构建的最大意义。\****

最大的使用场景是将编译环境和运行环境分离，比如，之前我们需要构建一个Go语言程序，那么就需要用到go命令等编译环境，我们的Dockerfile可能是这样的： 

```cobol
# Go语言环境基础镜像



FROM golang:1.10.3



 



# 将源码拷贝到镜像中



COPY server.go /build/



 



# 指定工作目录



WORKDIR /build



 



# 编译镜像时，运行 go build 编译生成 server 程序



RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 GOARM=6 go build -ldflags '-w -s' -o server



 



# 指定容器运行时入口程序 server



ENTRYPOINT ["/build/server"]
```

基础镜像 `golang:1.10.3` 是非常庞大的，***\*因为其中包含了所有的Go语言编译工具和库，而运行时候我们仅仅需要编译后的 `server` 程序就行了，不需要编译时的编译工具，最后生成的大体积镜像就是一种浪费。\****

将编译接口拷贝到镜像中就行了，那么Dockerfile的基础镜像并不需要包含Go编译环境：

```bash
# 不需要Go语言编译环境



FROM scratch



 



# 将编译结果拷贝到容器中



COPY server /server



 



# 指定容器运行时入口程序 server



ENTRYPOINT ["/server"]
```

> 提示： `scratch` 是内置关键词，并不是一个真实存在的镜像。 `FROM scratch` 会使用一个完全干净的文件系统，不包含任何文件。 ***\*因为Go语言编译后不需要运行时，也就不需要安装任何的运行库。 `FROM scratch` 可以使得最后生成的镜像最小化，其中只包含了 server 程序。\****

在 Docker 17.05版本以后，就有了新的解决方案，直接一个Dockerfile就可以解决：

```cobol
# 编译阶段



FROM golang:1.10.3



 



COPY server.go /build/



 



WORKDIR /build



 



RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 GOARM=6 go build -ldflags '-w -s' -o server



 



# 运行阶段



FROM scratch



 



# 从编译阶段的中拷贝编译结果到当前镜像中



COPY --from=0 /build/server /



 



ENTRYPOINT ["/server"]
```

 这个 Dockerfile 的玄妙之处就在于 COPY 指令的 `--from=0` 参数，从前边的阶段中拷贝文件到当前阶段中，多个FROM语句时，0代表第一个阶段。除了使用数字，我们还可以给阶段命名，比如：

```cobol
# 编译阶段 命名为 builder



FROM golang:1.10.3 as builder



 



# ... 省略



 



# 运行阶段



FROM scratch



 



# 从编译阶段的中拷贝编译结果到当前镜像中



COPY --from=builder /build/server /
```

更为强大的是，`COPY --from` 不但可以从前置阶段中拷贝，还可以直接从一个已经存在的镜像中拷贝。比如：

```cobol
FROM ubuntu:16.04



 



COPY --from=quay.io/coreos/etcd:v3.3.9 /usr/local/bin/etcd /usr/local/bin/
```

我们直接将etcd镜像中的程序拷贝到了我们的镜像中，这样，在生成我们的程序镜像时，就不需要源码编译etcd了，直接将官方编译好的程序文件拿过来就行了。

有些程序要么没有apt源，要么apt源中的版本太老，要么干脆只提供源码需要自己编译，使用这些程序时，我们可以方便地使用已经存在的Docker镜像作为我们的基础镜像。但是我们的软件有时候可能需要依赖多个这种文件，我们并不能同时将 nginx 和 etcd 的镜像同时作为我们的基础镜像（不支持多根），这种情况下，使用 `COPY --from` 就非常方便实用了。