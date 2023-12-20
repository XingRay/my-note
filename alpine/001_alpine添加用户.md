# [当我使用Alpine作为基本图像时,如何添加用户？](https://qa.1r1g.com/sf/ask/1/) [¶](https://qa.1r1g.com/sf/r/3496856821/)

[Dan*_*ann ](https://qa.1r1g.com/sf/users/28156201/) 63 [docker](https://qa.1r1g.com/sf/ask/tagged/docker/) [alpine-linux](https://qa.1r1g.com/sf/ask/tagged/alpine-linux/)



我正在使用`alpine`(或基于Alpine的图像)作为我的Dockerfile中的基本图像.我需要添加哪些说明来创建用户？

最终我将使用此用户运行我将放入容器的应用程序,以便root用户不会.



[Dan*_*ann ](https://qa.1r1g.com/sf/users/28156201/) 127



Alpine使用该命令`adduser`并`addgroup`创建用户和组(而不是`useradd`和`usergroup`).

```
FROM alpine:latest

# Create a group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Tell docker that all future commands should run as the appuser user
USER appuser
```

标志`adduser`是:

```
Usage: adduser [OPTIONS] USER [GROUP]

Create new user, or add USER to GROUP

        -h DIR          Home directory
        -g GECOS        GECOS field
        -s SHELL        Login shell
        -G GRP          Group
        -S              Create a system user
        -D              Don't assign a password
        -H              Don't create home directory
        -u UID          User id
        -k SKEL         Skeleton directory (/etc/skel)
```



- 为什么不使用“ USER guest”？ (5认同)
- 或者，您可以使用以下命令替换上面的整个代码段：```USER 405```，它是Alpine Linux中的来宾用户。 (4认同)
- 我会创建一个新用户，因为我希望该用户与主机操作系统上的用户具有相同的 UID/GID，这样在 Linux 中运行 docker 时就不会出现权限问题。（对于 macOS/Windows 用户来说不是问题） (3认同)
- 请注意，由于Alpine是基于BusyBox的，因此它的`adduser`和`addgroup`命令不同于Debian和Ubuntu提供的`adduser`和`addgroup`，而后者又是`useradd`和`groupadd`的前端。值得注意的是，Debian和Ubuntu命令仅支持长格式选项。参见：https://manpages.debian.org/stretch/adduser/adduser.8.en.html (3认同)
- 什么是“系统”用户？ (2认同)

------

[rex*_*poo ](https://qa.1r1g.com/sf/users/771091191/) 31



命令是`adduser`和`addgroup`。

这是Docker的模板，可以在busybox环境（高山）和基于Debian的环境（Ubuntu等）中使用：

```
ENV USER=docker
ENV UID=12345
ENV GID=23456

RUN addgroup --gid "$GID" "$USER" \
    && adduser \
    --disabled-password \
    --gecos "" \
    --home "$(pwd)" \
    --ingroup "$USER" \
    --no-create-home \
    --uid "$UID" \
    "$USER"
```

请注意以下几点：

- `--disabled-password` 防止提示输入密码
- `--gecos ""` 在基于Debian的系统上绕过“全名”等提示
- `--home "$(pwd)"`将用户的住所设置为WORKDIR。***您可能不想要这个。\***
- `--no-create-home` 防止将Cruft从以下位置复制到目录中 `/etc/skel`

这些应用程序的用法说明**缺少**[adduser](https://git.busybox.net/busybox/tree/loginutils/adduser.c)和[addgroup](https://git.busybox.net/busybox/tree/loginutils/addgroup.c)代码中**的长标志**。

以下长格式标志在高山以及debian衍生物中均应起作用：

## 添加用户

```
BusyBox v1.28.4 (2018-05-30 10:45:57 UTC) multi-call binary.

Usage: adduser [OPTIONS] USER [GROUP]

Create new user, or add USER to GROUP

        --home DIR           Home directory
        --gecos GECOS        GECOS field
        --shell SHELL        Login shell
        --ingroup GRP        Group (by name)
        --system             Create a system user
        --disabled-password  Don't assign a password
        --no-create-home     Don't create home directory
        --uid UID            User id
```

要注意的一件事是，如果`--ingroup`未设置，则会分配GID以匹配UID。如果与提供的UID对应的GID已经存在，则adduser将失败。

## 添加组

```
BusyBox v1.28.4 (2018-05-30 10:45:57 UTC) multi-call binary.

Usage: addgroup [-g GID] [-S] [USER] GROUP

Add a group or add a user to a group

        --gid GID  Group id
        --system   Create a system group
```

在尝试编写自己的替代方法来[代替fixuid](https://github.com/boxboat/fixuid)项目时，我发现了所有这些内容，以便将容器作为主机UID / GID运行。

[我的入口点帮助程序脚本](https://github.com/Rexypoo/docker-entrypoint-helper)可以在GitHub上找到。

目的是在该脚本之前添加第一个参数，`ENTRYPOINT`该参数将导致Docker从相关的绑定安装中推断UID和GID。

可能需要环境变量“ TEMPLATE”来确定应从何处推断出权限。

（在撰写本文时，我没有脚本的文档。它仍在待办事项清单上！）



- +1，将长格式用作命令args可提高可读性并简化维护。编写Shell脚本时，请始终使用长格式（Dockerfile RUN *仅是Shell脚本*）。 (2认同)
- 我找到了问题的答案：安装“shadow”。 (2认同)

------

[gav*_*koa ](https://qa.1r1g.com/sf/users/12120461/) 8



有包`shadow`带来`useradd`& `usermod`。

`adduser`有一些愚蠢的限制：

```
$ sudo adduser --disabled-password root
adduser: user 'root' in use
```

但`usermod`不是：

```
$ sudo apk add shadow
$ sudo usermod --unlock root
```