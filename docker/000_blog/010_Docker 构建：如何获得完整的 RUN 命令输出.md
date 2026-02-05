# Docker 构建：如何获得完整的 RUN 命令输出？

**【问题标题】：Docker build: how to get full RUN command output?Docker 构建：如何获得完整的 RUN 命令输出？**
**【发布时间】：2022-01-14 11:21:09**
**【问题描述】：**

**更新**：这个问题用 MVRE 重构。

**有没有办法可以从使用 `docker build` 构建的 Dockerfile 中查看完整的 `RUN` 命令？** 例如。如果我的 Dockerfile 有语句：

```
# Dockerfile
FROM alpine:3.7 as base
RUN echo "this is the song that doesn't end. Yes it goes on and on, my friends. Some people started singing it not knowing what it was, and they'll continue singing it forever just because..."
```

...有没有办法可以看到完整的命令，即`echo "this is the song that doesn't end. Yes it goes on and on, my friends. Some people started singing it not knowing what it was, and they'll continue singing it forever just because..."` 以及运行该命令的完整输出？

我使用 docker build kit 构建（我不希望禁用它），默认情况下，它会折叠输出，并截断相对于终端宽度执行的命令，最终可能看起来像这样：

```
$ docker build --no-cache -t tmp:tmp .
[+] Building 16.2s (6/6) FINISHED
 => [internal] load build definition from Dockerfile                                 0.1s
 => => transferring dockerfile: 281B                                                 0.0s
 => [internal] load .dockerignore                                                    0.2s
 => => transferring context: 2B                                                      0.0s
 => [internal] load metadata for docker.io/library/alpine:3.7                        0.0s
 => CACHED [1/2] FROM docker.io/library/alpine:3.7                                   0.0s
 => [2/2] RUN echo "this is the song that doesn't end. Yes it goes on and on, my fr  2.0s
 => exporting to image                                                              13.9s
 => => exporting layers                                                              0.4s
 => => writing image sha256:d72d9f0e36f38227e2a28dce31781dc9b6089b01cf5645c70f33b2  13.5s
 => => naming to docker.io/library/tmp:tmp                                           0.0s
```

...即*命令*和*它的输出*都被截断/折叠。

[This article](https://www.likecs.com/default/index/tourl?u=aHR0cHM6Ly9zdGFja292ZXJmbG93LmNvbS9xdWVzdGlvbnMvNDM2NzUwNzUvaG93LXRvLWdldC1kb2NrZXItcnVuLWZ1bGwtYXJndW1lbnRz) 表示 `docker inspect` 应该用于此目的，在我的情况下：`docker inspect tmp:tmp`，我的答案将在输出的 `$[0].Config.Cmd` 部分，但该部分不包含相关资料：

```
$ docker inspect tmp:tmp
[
    {
        ...
        "Config": {
            ...
            "Cmd": [
                "/bin/sh"
            ],
            ...
```

...`docker inspect` 命令的任何其他部分也不包含相关信息（在我的示例中为 `cmake` 语句）。

[This article](https://www.likecs.com/default/index/tourl?u=aHR0cHM6Ly9zdGFja292ZXJmbG93LmNvbS9xdWVzdGlvbnMvNTU3NTYzNzIvd2hlbi11c2luZy1idWlsZGtpdC13aXRoLWRvY2tlci1ob3ctZG8taS1zZWUtdGhlLW91dHB1dC1vZi1ydW4tY29tbWFuZHM=) 建议对`docker build` 使用`--progress plain` 选项。这会展开*命令的输出*，但它仍然会截断*命令本身*，例如：

```
$ docker build --progress plain --no-cache -t tmp:tmp .
#1 [internal] load build definition from Dockerfile
#1 transferring dockerfile: 44B done
#1 DONE 0.0s

#2 [internal] load .dockerignore
#2 transferring context: 2B done
#2 DONE 0.1s

#3 [internal] load metadata for docker.io/library/alpine:3.7
#3 DONE 0.0s

#4 [1/2] FROM docker.io/library/alpine:3.7
#4 CACHED

#5 [2/2] RUN echo "this is the song that doesn't end. Yes it goes on and on...
#5 1.542 this is the song that doesn't end. Yes it goes on and on, my friends. Some people
 started singing it not knowing what it was, and they'll continue singing it forever just
because...
#5 DONE 2.1s

#6 exporting to image
#6 exporting layers
#6 exporting layers 0.7s done
#6 writing image sha256:0ce39b23377d91e47e7aa9b4e10e50d5a62a4ef9ec281f1b3e244e4b66a17d02
#6 writing image sha256:0ce39b23377d91e47e7aa9b4e10e50d5a62a4ef9ec281f1b3e244e4b66a17d02 1
3.3s done
#6 naming to docker.io/library/tmp:tmp done
#6 DONE 14.0s
```

**有没有办法可以查看 Dockerfile `RUN` 语句执行的完整（未截断）命令（以及命令的未折叠输出）？**

我执行`docker history`的输出：

```
$ docker history tmp:tmp
IMAGE               CREATED             CREATED BY                                      SIZE                COMMENT
0ce39b23377d        3 minutes ago       RUN /bin/sh -c echo "this is the song that d…   0B                  buildkit.dockerfile.v0
<missing>           2 years ago         /bin/sh -c #(nop)  CMD ["/bin/sh"]              0B
<missing>           2 years ago         /bin/sh -c #(nop) ADD file:aa17928040e31624c…   4.21MB
```

【问题讨论】：

标签： [docker](https://www.likecs.com/default/index/asktags?tag=docker) [docker-build](https://www.likecs.com/default/index/asktags?tag=docker-build) [docker-buildkit](https://www.likecs.com/default/index/asktags?tag=docker-buildkit)



**【解决方案1】：**

设置 env-var `PROGRESS_NO_TRUNC=1` 和 `--progress plain` 就可以了：

```
$ PROGRESS_NO_TRUNC=1 docker build --progress plain --no-cache -t tmp:tmp .
#1 [internal] load .dockerignore
#1 sha256:0c3d9a77560c6997674ff903c1fd8166c2b0a0c56b8267c8919f9435df2b6360
#1 transferring context: 0.0s
#1 transferring context: 2B 0.0s done
#1 DONE 0.2s

#2 [internal] load build definition from Dockerfile
#2 sha256:637986daa013bdd36af757aa03cf8b23447a85ed9e3e103fda6234a9d97332cd
#2 transferring dockerfile: 44B 0.1s done
#2 DONE 0.2s

#3 [internal] load metadata for docker.io/library/alpine:3.7
#3 sha256:d05d2c4bcea3dce001a657515352ca6040d02fcc707293d5f7167602950d71ce
#3 DONE 0.0s

#4 [1/2] FROM docker.io/library/alpine:3.7
#4 sha256:c139e859151268321f8b3d9af4bf0195aab52a1b66880ee4294469151c73bfb9
#4 CACHED

#5 [2/2] RUN echo "this is the song that doesn't end. Yes it goes on and on, my friends. S
ome people started singing it not knowing what it was, and they'll continue singing it for
ever just because..."
#5 sha256:63a60e7b5a4ce0944e5135780a681e85c9fc52a776b498fcf0652f563bc0c470
#5 1.381 this is the song that doesn't end. Yes it goes on and on, my friends. Some people
 started singing it not knowing what it was, and they'll continue singing it forever just
because...
#5 DONE 1.7s

#6 exporting to image
#6 sha256:e8c613e07b0b7ff33893b694f7759a10d42e180f2b4dc349fb57dc6b71dcab00
#6 exporting layers
#6 exporting layers 0.2s done
#6 writing image sha256:523ddeb4ae29e8f8bbd9e346a07d980024e58d867222ed3d30d552587df72685 0
.0s done
#6 naming to docker.io/library/tmp:tmp 0.0s done
#6 DONE 0.2s
```

`RUN` 语句本身以及执行该语句的输出都完整呈现。





# [如何查看docker build“RUN命令”标准输出？（用于 Windows 的泊坞窗）](https://qa.1r1g.com/sf/ask/1/) [¶](https://qa.1r1g.com/sf/r/4590605981/)

[Tho*_*odi ](https://qa.1r1g.com/sf/users/577294861/) 8 [docker](https://qa.1r1g.com/sf/ask/tagged/docker/) [docker-build](https://qa.1r1g.com/sf/ask/tagged/docker-build/) [docker-for-windows](https://qa.1r1g.com/sf/ask/tagged/docker-for-windows/)



在过去，我可以简单地做这样的事情：

`Dockerfile`：

```
FROM ubuntu
RUN echo "test"
```

这将输出`test`到我的外壳。我用它作为调试我的构建的一种方式。

在 docker for windows 的最新稳定版本中，构建输出看起来完全不同，并且没有显示任何命令的任何标准输出。

如何在构建过程中查看任何命令的输出？

对于上面的`Dockerfile`示例，当前输出如下所示：

```
[+] Building 4.7s (6/6) FINISHED
 => [internal] load build definition from Dockerfile                                                                       0.0s 
 => => transferring dockerfile: 65B                                                                                        0.0s 
 => [internal] load .dockerignore                                                                                          0.0s 
 => => transferring context: 2B                                                                                            0.0s 
 => [internal] load metadata for docker.io/library/ubuntu:latest                                                           2.0s 
 => [1/2] FROM docker.io/library/ubuntu@sha256:c95a8e48bf88e9849f3e0f723d9f49fa12c5a00cfc6e60d2bc99d87555295e4c            2.3s 
 => => resolve docker.io/library/ubuntu@sha256:c95a8e48bf88e9849f3e0f723d9f49fa12c5a00cfc6e60d2bc99d87555295e4c            0.0s 
 => => sha256:f643c72bc25212974c16f3348b3a898b1ec1eb13ec1539e10a103e6e217eb2f1 3.32kB / 3.32kB                             0.0s 
 => => sha256:da7391352a9bb76b292a568c066aa4c3cbae8d494e6a3c68e3c596d34f7c75f8 28.56MB / 28.56MB                           1.2s 
 => => sha256:14428a6d4bcdba49a64127900a0691fb00a3f329aced25eb77e3b65646638f8d 847B / 847B                                 0.5s 
 => => sha256:c95a8e48bf88e9849f3e0f723d9f49fa12c5a00cfc6e60d2bc99d87555295e4c 1.20kB / 1.20kB                             0.0s 
 => => sha256:4e4bc990609ed865e07afc8427c30ffdddca5153fd4e82c20d8f0783a291e241 943B / 943B                                 0.0s 
 => => extracting sha256:da7391352a9bb76b292a568c066aa4c3cbae8d494e6a3c68e3c596d34f7c75f8                                  0.8s 
 => => extracting sha256:14428a6d4bcdba49a64127900a0691fb00a3f329aced25eb77e3b65646638f8d                                  0.0s 
 => => extracting sha256:2c2d948710f21ad82dce71743b1654b45acb5c059cf5c19da491582cef6f2601                                  0.0s 
 => [2/2] RUN echo "test"                                                                                                  0.3s 
 => exporting to image                                                                                                     0.0s 
 => => exporting layers                                                                                                    0.0s 
 => => writing image sha256:8c61a015c1cc5af925e0db03bb56a627ce3624818c456fca11379c92c2e9d864                               0.0s 
```



[Tho*_*odi ](https://qa.1r1g.com/sf/users/577294861/) 7



通过阅读[使用BuildKit与码头工人，我怎么看RUN命令的输出？](https://qa.1r1g.com/sf/ask/3902946071/) 更改输出格式的原因是，现在使用名为“buildkit”的新功能代替“经典”docker build。

## 方法一（取自以上问题答案）

使用`docker build --progress=plain .`看平输出。

要将进度永久设置为普通，`BUILDKIT_PROGRESS=plain`可以设置ENV 。

为了快速获胜（非永久），我只是在脚本的开头添加了以下行：

- 电源外壳： `$env:BUILDKIT_PROGRESS="plain"`
- 指令： `set BUILDKIT_PROGRESS=plain`

## 方法二（不推荐）

在设置 docker 引擎功能中禁用 buildkit [![在此处输入图片说明](https://i.stack.imgur.com/D9ipA.png)](https://i.stack.imgur.com/D9ipA.png)



- 值得注意的是，使用 BuildKit 它会输出到 `stderr` 而不是 `stdout`：https://github.com/moby/buildkit/issues/1186 (4认同)
- 当我将“--progress plain”添加到命令中时，什么也没有发生。我没有看到任何“RUN echo hello”命令的输出。 (3认同)
- @trusktr 尝试将“echo”重定向到 stderr：“RUN echo hello &gt;&amp;2” (2认同)
- 