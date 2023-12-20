# [在Docker Alpine容器中启动shell](https://qa.1r1g.com/sf/ask/1/) [¶](https://qa.1r1g.com/sf/r/2498273991/)

[Ole*_*Ole ](https://qa.1r1g.com/sf/users/117898861/) 131 [linux](https://qa.1r1g.com/sf/ask/tagged/linux/) [containers](https://qa.1r1g.com/sf/ask/tagged/containers/) [docker](https://qa.1r1g.com/sf/ask/tagged/docker/) [alpine-linux](https://qa.1r1g.com/sf/ask/tagged/alpine-linux/)



要为Ubuntu映像启动交互式shell,我们可以运行:

```
ole@T:~$ docker run -it --rm ubuntu
root@1a6721e1fb64:/# ls
bin  boot  dev  etc  home  lib  lib64  media  mnt  opt  proc  root  run  sbin  srv  sys  tmp  usr  var
```

但是当为[Alpine Docker镜像](https://hub.docker.com/_/alpine/)运行时,会产生以下结果:

```
ole@T:~$ docker run -it --rm alpine
Error response from daemon: No command specified
```

在Alpine基础容器中启动交互式shell的命令是什么？



[Ole*_*Ole ](https://qa.1r1g.com/sf/users/117898861/) 233



```
ole@T:~$ docker run -it --rm alpine /bin/ash
(inside container) / # 
```

上面使用的选项:

- `/bin/ash`是Ash([Almquist Shell](http://www.in-ulm.de/~mascheck/various/ash/#busybox))由BusyBox提供
- `--rm`退出时自动删除容器(`docker run --help`)
- `-i` 交互模式(即使没有附加也保持STDIN打开)
- `-t` 分配伪TTY



- +1 注意到 Alpine 有 `ash` 而不是 `bash`，这让我不太熟悉 Alpine，这让我无法更早地访问容器。 (3认同)
- 当然 - 好主意 - ash 是 shell，--rm 在运行完成后删除容器。因此，如果您希望容器在运行后仍然可用，请跳过 --rm 的使用。我正在使用它，因为我只是在尝试 ATM。 (2认同)

------

[jan*_*hez ](https://qa.1r1g.com/sf/users/177156801/) 57



通常情况下,高山Linux映像不包含`bash`,相反,你可以使用`/bin/ash`,`/bin/sh`,`ash`或只`sh`.

/斌/灰

```
docker run -it --rm alpine /bin/ash
```

/ bin/sh的

```
docker run -it --rm alpine /bin/sh
```

灰

```
docker run -it --rm alpine ash
```

SH

```
docker run -it --rm alpine sh
```

我希望这些信息对您有所帮助.



- 嗨@peter-mortensen，不同之处在于`ash` 只是一个指向`/bin/ash` 的符号链接。一些 linux 发行版没有符号链接。 (2认同)

------

[小智 ](https://qa.1r1g.com/sf/users/726083151/) 24



如果容器已经在运行：

```
docker exec -it container_id_or_name ash
```





------

[val*_*ano ](https://qa.1r1g.com/sf/users/507943901/) 20



如今，`/bin/sh`默认情况下，Alpine映像将直接启动，而无需指定要执行的shell：

```
$ sudo docker run -it --rm alpine  
/ # echo $0  
/bin/sh  
```

这是因为`alpine`镜像Dockerfile现在包含一个[`CMD`](https://docs.docker.com/engine/reference/builder/)命令，该命令指定了在容器启动时要执行的shell ： `CMD ["/bin/sh"]`。

在较旧的Alpine映像版本（2017之前）中，未使用CMD命令，因为Docker曾为CMD创建一个额外的层，这导致映像大小增加。这是Alpine图片开发人员想要避免的事情。在最新的Docker版本（1.10+）中，CMD不再占据一层，因此已将其添加到`alpine`映像中。因此，只要不覆盖CMD，最近的Alpine映像都将启动`/bin/sh`。

作为参考，请参阅Glider Labs对官方Alpine Dockerfile的以下承诺：[https](https://github.com/gliderlabs/docker-alpine/commit/ddc19dd95ceb3584ced58be0b8d7e9169d04c7a3#diff-db3dfdee92c17cf53a96578d4900cb5b) :
[//github.com/gliderlabs/docker-alpine/commit/ddc19dd95ceb3584ced58be0b8d7e9169d04c7a3#diff-db3dfdee92c17cf53a96578d4900cb5b](https://github.com/gliderlabs/docker-alpine/commit/ddc19dd95ceb3584ced58be0b8d7e9169d04c7a3#diff-db3dfdee92c17cf53a96578d4900cb5b)