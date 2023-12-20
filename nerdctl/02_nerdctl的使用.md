Containerd高级命令行工具nerdctl安装及使用


一、实验环境
实验环境：
1、win10笔记本
2、1台centos7.6虚机(vmwrokstation虚机)
 cri-containerd-cni-1.5.5-linux-amd64.tar.gz
 nerdctl-0.12.1-linux-amd64.tar.gz
二、实验软件
GitHub - containerd/nerdctl: contaiNERD CTL - Docker-compatible CLI for containerd, with support for Compose, Rootless, eStargz, OCIcrypt, IPFS, ...

GitHub - moby/buildkit: concurrent, cache-efficient, and Dockerfile-agnostic builder toolkit

三、nerdctl安装
同样直接在 GitHub Release 页面下载对应的压缩包解压到 PATH 路径下即可：

https://github.com/containerd/nerdctl


下载nerdctl-0.12.1-linux-amd64.tar.gz软件包：

如果没有安装 containerd，则可以下载 nerdctl-full-<VERSION>-linux-amd64.tar.gz 包进行安装

```bash
wget https://github.com/containerd/nerdctl/releases/download/v0.12.1/nerdctl-0.12.1-linux-amd64.tar.gz
```

如果有限制，也可以替换成下面的 URL 加速下载

```bash
wget https://download.fastgit.org/containerd/nerdctl/releases/download/v0.12.1/nerdctl-0.12.1-linux-amd64.tar.gz
https://download.fastgit.org/containerd/nerdctl/releases/download/v0.12.1/nerdctl-0.12.1-linux-amd64.tar.gz
Resolving download.fastgit.org (download.fastgit.org)... 88.198.10.254
Connecting to download.fastgit.org (download.fastgit.org)|88.198.10.254|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 7528755 (7.2M) [application/octet-stream]
Saving to: ‘nerdctl-0.12.1-linux-amd64.tar.gz’

100%[===============================================================================================================================>] 7,528,755   3.31MB/s   in 2.2s

2021-10-25 13:13:46 (3.31 MB/s) - ‘nerdctl-0.12.1-linux-amd64.tar.gz’ saved [7528755/7528755][root@containerd ~]#ll -h nerdctl-0.12.1-linux-amd64.tar.gz
-rw-r--r-- 1 root root 7.2M Oct  5 15:10 nerdctl-0.12.1-linux-amd64.tar.gz
[root@containerd ~]#
```

解压软件包到相应目录：

```bash
[root@containerd ~]#tar tf nerdctl-0.12.1-linux-amd64.tar.gz #先查看下压缩包文件内容
nerdctl
containerd-rootless-setuptool.sh
containerd-rootless.sh
[root@containerd ~]#mkdir -p /usr/local/containerd/bin  && tar -zxvf nerdctl-0.12.1-linux-amd64.tar.gz nerdctl && mv nerdctl /usr/local/containerd/bin
nerdctl
[root@containerd ~]#ln -s /usr/local/containerd/bin/nerdctl /usr/bin/nerdctl
```

验证：

```bash
[root@containerd ~]#nerdctl version
Client:
 Version:       v0.12.1
 Git commit:    6f0c8b7bc63270404c9f5810a899e6bae7546608

Server:
 containerd:
  Version:      v1.5.5
  GitCommit:    72cec4be58a9eb6b2910f5d10f1c01ca47d231c0
[root@containerd ~]#
```

至此，nerdctl安装完成。

安装完成后接下来学习下 nerdctl 命令行工具的使用。

0 nerd帮助命令

```bash
[root@containerd ~]#nerdctl
NAME:
   nerdctl - Docker-compatible CLI for containerd

USAGE:
   nerdctl [global options] command [command options] [arguments...]

VERSION:
   0.12.1

COMMANDS:
   run         Run a command in a new container
   exec        Run a command in a running container
   ps          List containers
   logs        Fetch the logs of a container. Currently, only containers created with `nerdctl run -d` are supported.
   port        List port mappings or a specific mapping for the container
   stop        Stop one or more running containers
   start       Start one or more running containers
   kill        Kill one or more running containers
   rm          Remove one or more containers
   pause       Pause all processes within one or more containers
   unpause     Unpause all processes within one or more containers
   commit      [flags] CONTAINER REPOSITORY[:TAG]
   wait        Block until one or more containers stop, then print their exit codes.
   build       Build an image from a Dockerfile. Needs buildkitd to be running.
   images      List images
   pull        Pull an image from a registry
   push        Push an image or a repository to a registry
   load        Load an image from a tar archive or STDIN
   save        Save one or more images to a tar archive (streamed to STDOUT by default)
   tag         Create a tag TARGET_IMAGE that refers to SOURCE_IMAGE
   rmi         Remove one or more images
   events      Get real time events from the server
   info        Display system-wide information
   version     Show the nerdctl version information
   inspect     Return low-level information on objects.
   top         Display the running processes of a container
   login       Log in to a Docker registry
   logout      Log out from a Docker registry
   compose     Compose
   completion  Show shell completion
   help, h     Shows a list of commands or help for one command
   Management:
     container  Manage containers
     image      Manage images
     network    Manage networks
     volume     Manage volumes
     system     Manage containerd
     namespace  Manage containerd namespaces

GLOBAL OPTIONS:
   --debug                                            debug mode (default: false)
   --debug-full                                       debug mode (with full output) (default: false)
   --address value, -a value, --host value, -H value  containerd address, optionally with "unix://" prefix (default: "/run/containerd/containerd.sock") [$CONTAINERD_ADDRESS]
   --namespace value, -n value                        containerd namespace, such as "moby" for Docker, "k8s.io" for Kubernetes (default: "default") [$CONTAINERD_NAMESPACE]
   --snapshotter value, --storage-driver value        containerd snapshotter (default: "overlayfs") [$CONTAINERD_SNAPSHOTTER]
   --cni-path value                                   Set the cni-plugins binary directory (default: "/opt/cni/bin") [$CNI_PATH]
   --cni-netconfpath value                            Set the CNI config directory (default: "/etc/cni/net.d") [$NETCONFPATH]
   --data-root value                                  Root directory of persistent nerdctl state (managed by nerdctl, not by containerd) (default: "/var/lib/nerdctl")
   --cgroup-manager value                             Cgroup manager to use ("cgroupfs"|"systemd") (default: "cgroupfs")
   --insecure-registry                                skips verifying HTTPS certs, and allows falling back to plain HTTP (default: false)
   --help, -h                                         show help (default: false)
   --version, -v                                      print the version (default: false)
[root@containerd ~]#
```

1 Run&Exec 
nerdctl run 和 docker run 类似可以使用 nerdctl run 命令运行容器，例如：

```bash
nerdctl run -d -p 80:80 --name=nginx --restart=always nginx:alpine
docker.io/library/nginx:alpine:                                                   resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:bead42240255ae1485653a956ef41c9e458eb077fcb6dc664cbc3aa9701a05ce:    done ```

可选的参数使用和 docker run 基本一直，比如 -i、-t、--cpus、--memory 等选项，可以使用 nerdctl run --help 获取可使用的命令选项：

```bash
[root@containerd ~]#nerdctl run --help
NAME:
   nerdctl run - Run a command in a new container

USAGE:
   nerdctl run [command options] [arguments...]

OPTIONS:
   --help                        show help (default: false)
   --tty, -t                     (Currently -t needs to correspond to -i) (default: false)
   --interactive, -i             Keep STDIN open even if not attached (default: false)
   --detach, -d                  Run container in background and print container ID (default: false)
   --restart value               Restart policy to apply when a container exits (implemented values: "no"|"always") (default: "no")
   --rm                          Automatically remove the container when it exits (default: false)
   --pull value                  Pull image before running ("always"|"missing"|"never") (default: "missing")
   --network value, --net value  Connect a container to a network ("bridge"|"host"|"none") (default: "bridge")
   --dns value                   Set custom DNS servers
   --publish value, -p value     Publish a container's port(s) to the host
   --hostname value, -h value    Container host name
   --cpus value                  Number of CPUs (default: 0)
   --memory value, -m value      Memory limit
   --pid value                   PID namespace to use
   --pids-limit value            Tune container pids limit (set -1 for unlimited) (default: -1)
   --cgroupns value              Cgroup namespace to use, the default depends on the cgroup version ("host"|"private") (default: "host")
   --cpuset-cpus value           CPUs in which to allow execution (0-3, 0,1)
   --cpu-shares value            CPU shares (relative weight) (default: 0)
   --device value                Add a host device to the container
   --user value, -u value        Username or UID (format: <name|uid>[:<group|gid>])
   --security-opt value          Security options
   --cap-add value               Add Linux capabilities
   --cap-drop value              Drop Linux capabilities
   --privileged                  Give extended privileges to this container (default: false)
   --runtime value               Runtime to use for this container, e.g. "crun", or "io.containerd.runsc.v1" (default: "io.containerd.runc.v2")
   --sysctl value                Sysctl options
   --gpus value                  GPU devices to add to the container ('all' to pass all GPUs)
   --volume value, -v value      Bind mount a volume
   --read-only                   Mount the container's root filesystem as read only (default: false)
   --rootfs                      The first argument is not an image but the rootfs to the exploded container (default: false)
   --entrypoint value            Overwrite the default ENTRYPOINT of the image
   --workdir value, -w value     Working directory inside the container
   --env value, -e value         Set environment variables
   --add-host value              Add a custom host-to-IP mapping (host:ip)
   --env-file value              Set environment variables from file
   --name value                  Assign a name to the container
   --label value, -l value       Set meta data on a container
   --label-file value            Read in a line delimited file of labels
   --cidfile value               Write the container ID to the file
   --shm-size value              Size of /dev/shm
   --pidfile value               file path to write the task's pid
   --ulimit value                Ulimit options

[root@containerd ~]#
```

nerdctl exec 同样也可以使用 exec 命令执行容器相关命令，例如：

```bash
nerdctl exec -it nginx /bin/sh
/ # date
Thu Aug 19 06:43:19 UTC 2021
/ #
```

2、容器管理
nerdctl ps：列出容器
使用 nerdctl ps 命令可以列出所有容器。

```bash
nerdctl ps
CONTAINER ID    IMAGE                             COMMAND                   CREATED           STATUS    PORTS                 NAMES
6e489777d2f7    docker.io/library/nginx:alpine    "/docker-entrypoint.…"    10 minutes ago    Up        0.0.0.0:80->80/tcp    nginx
```

同样可以使用 -a 选项显示所有的容器列表，默认只显示正在运行的容器，不过需要注意的是 nerdctl ps 命令并没有实现 docker ps 下面的 --filter、--format、--last、--size 等选项。


nerdctl inspect：获取容器的详细信息。

```bash
nerdctl inspect nginx
[
    {
        "Id": "6e489777d2f73dda8a310cdf8da9df38353c1aa2021d3c2270b30eff1806bcf8",
        "Created": "2021-08-19T06:35:46.403464674Z",
        "Path": "/docker-entrypoint.sh",
        "Args": [
            "nginx",
            "-g",
            "daemon off;"
        ],
        "State": {
            "Status": "running",
            "Running": true,
            "Paused": false,
            "Pid": 2002,
            "ExitCode": 0,
            "FinishedAt": "0001-01-01T00:00:00Z"
        },
        "Image": "docker.io/library/nginx:alpine",
        "ResolvConfPath": "/var/lib/nerdctl/1935db59/containers/default/6e489777d2f73dda8a310cdf8da9df38353c1aa2021d3c2270b30eff1806bcf8/resolv.conf",
        "LogPath": "/var/lib/nerdctl/1935db59/containers/default/6e489777d2f73dda8a310cdf8da9df38353c1aa2021d3c2270b30eff1806bcf8/6e489777d2f73dda8a310cdf8da9df38353c1aa2021d3c2270b30eff1806bcf8-json.log",
        "Name": "nginx",
        "Driver": "overlayfs",
        "Platform": "linux",
        "AppArmorProfile": "nerdctl-default",
        "NetworkSettings": {
            "Ports": {
                "80/tcp": [
                    {
                        "HostIp": "0.0.0.0",
                        "HostPort": "80"
                    }
                ]
            },
            "GlobalIPv6Address": "",
            "GlobalIPv6PrefixLen": 0,
            "IPAddress": "10.4.0.3",
            "IPPrefixLen": 24,
            "MacAddress": "f2:b1:8e:a2:fe:18",
            "Networks": {
                "unknown-eth0": {
                    "IPAddress": "10.4.0.3",
                    "IPPrefixLen": 24,
                    "GlobalIPv6Address": "",
                    "GlobalIPv6PrefixLen": 0,
                    "MacAddress": "f2:b1:8e:a2:fe:18"
                }
            }
        }
    }
]
```

可以看到显示结果和 docker inspect 也基本一致的。

nerdctl logs：获取容器日志
查看容器日志是我们平时经常会使用到的一个功能，同样我们可以使用 nerdctl logs 来获取日志数据：

```bash
nerdctl logs -f nginx
......
2021/08/19 06:35:46 [notice] 1#1: start worker processes
2021/08/19 06:35:46 [notice] 1#1: start worker process 32
2021/08/19 06:35:46 [notice] 1#1: start worker process 33
```

同样支持 -f、-t、-n、--since、--until 这些选项。

```bash
#-n选项：
[root@containerd ~]#nerdctl logs -n 3 nginx_bak
2021/10/24 23:17:40 [notice] 1#1: start worker process 32
2021/10/24 23:17:40 [notice] 1#1: start worker process 33
10.4.0.1 - - [24/Oct/2021:23:42:57 +0000] "GET / HTTP/1.1" 200 615 "-" "curl/7.29.0" "-"
[root@containerd ~]#
```

nerdctl stop：停止容器

```bash
nerdctl stop nginx
nginx

nerdctl ps
CONTAINER ID    IMAGE    COMMAND    CREATED    STATUS    PORTS    NAMES

nerdctl ps -a
CONTAINER ID    IMAGE                             COMMAND                   CREATED           STATUS    PORTS                 NAMES
6e489777d2f7    docker.io/library/nginx:alpine    "/docker-entrypoint.…"    20 minutes ago    Up        0.0.0.0:80->80/tcp    nginx
```

nerdctl rm：删除容器

```bash
nerdctl rm nginx
You cannot remove a running container f4ac170235595f28bf962bad68aa81b20fc83b741751e7f3355bd77d8016462d. Stop the container before attempting removal or force remove

nerdctl rm -f ginx
nginx

nerdctl ps
CONTAINER ID    IMAGE    COMMAND    CREATED    STATUS    PORTS    NAMES
```

要强制删除同样可以使用 -f 或 --force 选项来操作。

3、镜像管理
nerdctl images：镜像列表

```bash
nerdctl images
REPOSITORY    TAG       IMAGE ID        CREATED           SIZE
alpine        latest    eb3e4e175ba6    6 days ago        5.9 MiB
nginx         alpine    bead42240255    29 minutes ago    16.0 KiB
```

也需要注意的是没有实现 docker images 的一些选项，比如 --all、--digests、--filter、--format。

nerdctl images 和 ctr i ls的对比，nerctl更友好：

```bash
[root@containerd ~]#nerdctl images
REPOSITORY    TAG       IMAGE ID        CREATED         SIZE
nginx         alpine    686aac2769fd    38 hours ago    24.9 MiB
[root@containerd ~]#ctr i ls
REF                            TYPE                                                      DIGEST                                                                  SIZE    PLATFORMS                                                                                LABELS
docker.io/library/nginx:alpine application/vnd.docker.distribution.manifest.list.v2+json sha256:686aac2769fd6e7bab67663fd38750c135b72d993d0bb0a942ab02ef647fc9c3 9.5 MiB linux/386,linux/amd64,linux/arm/v6,linux/arm/v7,linux/arm64/v8,linux/ppc64le,linux/s390x -
[root@containerd ~]#
```

nerdctl pull：拉取镜像

nerdctl很优秀，可以直接接镜像名的，而不像ctr命令那样繁琐。

```bash
[root@containerd ~]#nerdctl images
REPOSITORY    TAG       IMAGE ID        CREATED         SIZE
nginx         alpine    686aac2769fd    38 hours ago    24.9 MiB
[root@containerd ~]#nerdctl pull busybox 
docker.io/library/busybox:latest:                                                 resolved       |++++++++++++++++++++++++++++++++++++++|
index-sha256:f7ca5a32c10d51aeda3b4d01c61c6061f497893d7f6628b92f822f7117182a57:    done           |++++++++++++++++++++++++++++++++++++++|
manifest-sha256:febcf61cd6e1ac9628f6ac14fa40836d16f3c6ddef3b303ff0321606e55ddd0b: done           |++++++++++++++++++++++++++++++++++++++|
config-sha256:16ea53ea7c652456803632d67517b78a4f9075a10bfdc4fc6b7b4cbf2bc98497:   done           |++++++++++++++++++++++++++++++++++++++|
layer-sha256:24fb2886d6f6c5d16481dd7608b47e78a8e92a13d6e64d87d57cb16d5f766d63:    done           |++++++++++++++++++++++++++++++++++++++|
elapsed: 5.9 s                                                                    total:  753.5  (127.7 KiB/s)                       
[root@containerd ~]#nerdctl images
REPOSITORY    TAG       IMAGE ID        CREATED          SIZE
busybox       latest    f7ca5a32c10d    2 seconds ago    1.2 MiB
nginx         alpine    686aac2769fd    38 hours ago     24.9 MiB
[root@containerd ~]#
```

nerdctl push：推送镜像
当然在推送镜像之前也可以使用 nerdctl login 命令登录到镜像仓库，然后再执行 push 操作。

可以使用 nerdctl login --username xxx --password xxx 进行登录，使用 nerdctl logout 可以注销退出登录。

```bash
[root@containerd ~]#nerdctl push harbor.k8s.local/course/nginx:alpine

[root@containerd ~]#nerdctl login --username xxx --password xxx harbor.k8s.local

[root@containerd ~]#nerdctl logout
Removing login credentials for https://index.docker.io/v1/
[root@containerd ~]#
```

nerdctl tag：镜像标签
使用 tag 命令可以为一个镜像创建一个别名镜像：

```bash
nerdctl images
REPOSITORY    TAG                  IMAGE ID        CREATED           SIZE
busybox       latest               0f354ec1728d    6 minutes ago     1.3 MiB
nginx         alpine               bead42240255    41 minutes ago    16.0 KiB
nerdctl tag nginx:alpine harbor.k8s.local/course/nginx:alpine
nerdctl images
REPOSITORY                       TAG                  IMAGE ID        CREATED           SIZE
busybox                          latest               0f354ec1728d    7 minutes ago     1.3 MiB
nginx                            alpine               bead42240255    41 minutes ago    16.0 KiB
harbor.k8s.local/course/nginx    alpine               bead42240255    2 seconds ago     16.0 KiB
```

注意：用tag打的镜像，其Image ID都是相同的：

image-20211025101448781

nerdctl save：导出镜像
使用 save 命令可以导出镜像为一个 tar 压缩包。

```bash
nerdctl save -o busybox.tar.gz busybox:latest
ls -lh busybox.tar.gz
-rw-r--r-- 1 root root 761K Aug 19 15:19 busybox.tar.gz
```

nerdctl rmi：删除镜像

```bash
nerdctl rmi busybox
Untagged: docker.io/library/busybox:latest@sha256:0f354ec1728d9ff32edcd7d1b8bbdfc798277ad36120dc3dc683be44524c8b60
Deleted: sha256:5b8c72934dfc08c7d2bd707e93197550f06c0751023dabb3a045b723c5e7b373
```

nerdctl load：导入镜像
使用 load 命令可以将上面导出的镜像再次导入：

```bash
nerdctl load -i busybox.tar.gz
unpacking docker.io/library/busybox:latest (sha256:0f354ec1728d9ff32edcd7d1b8bbdfc798277ad36120dc3dc683be44524c8b60)...done
```

使用 -i 或 --input 选项指定需要导入的压缩包。

4、镜像构建
镜像构建是平时我们非常重要的一个需求，我们知道 ctr 并没有构建镜像的命令，而现在我们又不使用 Docker 了，那么如何进行镜像构建了，幸运的是 nerdctl 就提供了 nerdctl build 这样的镜像构建命令。

nerdctl build：从 Dockerfile 构建镜像
比如现在我们定制一个 nginx 镜像，新建一个如下所示的 Dockerfile 文件：

```bash
[root@containerd ~]#mkdir -p /root/nerctl_demo
[root@containerd ~]#cd /root/nerctl_demo/
[root@containerd nerctl_demo]#cat > Dockerfile <<EOF

> FROM nginx:alpine
> RUN echo 'Hello Nerdctl From Containerd' > /usr/share/nginx/html/index.html
> EOF
> [root@containerd nerctl_demo]#cat Dockerfile
> FROM nginx:alpine
> RUN echo 'Hello Nerdctl From Containerd' > /usr/share/nginx/html/index.html
> 然后在文件所在目录执行镜像构建命令：

[root@containerd nerctl_demo]#nerdctl build -t nginx:nerctl -f Dockefile .
FATA[0000] `buildctl` needs to be installed and `buildkitd` needs to be running, see https://github.com/moby/buildkit: exec: "buildctl": executable file not found in $PATH
[root@containerd nerctl_demo]#
```

注意：也可以加上这个--no-cache选项

```bash
#--no-cache选项
--no-cache                Do not use cache when building the image (default: false)
```

可以看到有一个错误提示，需要我们安装 buildctl 并运行 buildkitd，这是因为 nerdctl build 需要依赖 buildkit 工具。

buildkit 项目也是 Docker 公司开源的一个构建工具包，支持 OCI 标准的镜像构建。它主要包含以下部分:

服务端 buildkitd：当前支持 runc 和 containerd 作为 worker，默认是 runc，我们这里使用 containerd

客户端 buildctl：负责解析 Dockerfile，并向服务端 buildkitd 发出构建请求

buildkit 是典型的 C/S 架构，客户端和服务端是可以不在一台服务器上，而 nerdctl 在构建镜像的时候也作为 buildkitd 的客户端，所以需要我们安装并运行 buildkitd。

https://github.com/moby/buildkit



所以接下来我们先来安装 buildkit：

```bash
wget https://github.com/moby/buildkit/releases/download/v0.9.1/buildkit-v0.9.1.linux-amd64.tar.gz
```

# 如果有限制，也可以替换成下面的 URL 加速下载

```bash
wget https://download.fastgit.org/moby/buildkit/releases/download/v0.9.1/buildkit-v0.9.1.linux-amd64.tar.gz
[root@containerd ~]#ll -h buildkit-v0.9.1.linux-amd64.tar.gz
-rw-r--r-- 1 root root 46M Oct  5 03:51 buildkit-v0.9.1.linux-amd64.tar.gz
[root@containerd ~]#tar tf buildkit-v0.9.1.linux-amd64.tar.gz
bin/
bin/buildctl
bin/buildkit-qemu-aarch64
bin/buildkit-qemu-arm
bin/buildkit-qemu-i386
bin/buildkit-qemu-mips64
bin/buildkit-qemu-mips64el
bin/buildkit-qemu-ppc64le
bin/buildkit-qemu-riscv64
bin/buildkit-qemu-s390x
bin/buildkit-runc
bin/buildkitd
[root@containerd ~]#

tar -zxvf buildkit-v0.9.1.linux-amd64.tar.gz -C /usr/local/containerd/
bin/
bin/buildctl
bin/buildkit-qemu-aarch64
bin/buildkit-qemu-arm
bin/buildkit-qemu-i386
bin/buildkit-qemu-mips64
bin/buildkit-qemu-mips64el
bin/buildkit-qemu-ppc64le
bin/buildkit-qemu-riscv64
bin/buildkit-qemu-s390x
bin/buildkit-runc
bin/buildkitd

ln -s /usr/local/containerd/bin/buildkitd /usr/local/bin/buildkitd
ln -s /usr/local/containerd/bin/buildctl /usr/local/bin/buildctl
```

这里我们使用 Systemd 来管理 buildkitd，创建如下所示的 systemd unit 文件：

```bash
cat > /etc/systemd/system/buildkit.service <<EOF
[Unit]
Description=BuildKit
Documentation=https://github.com/moby/buildkit

[Service]
ExecStart=/usr/local/bin/buildkitd --oci-worker=false --containerd-worker=true

[Install]
WantedBy=multi-user.target
EOF
```

然后启动 buildkitd：

```bash
[root@containerd ~]#systemctl deamon-reload
Unknown operation 'deamon-reload'.
[root@containerd ~]#systemctl daemon-reload
[root@containerd ~]#systemctl enable buildkit --now
Created symlink from /etc/systemd/system/multi-user.target.wants/buildkit.service to /etc/systemd/system/buildkit.service.
[root@containerd ~]#systemctl status buildkit
● buildkit.service - BuildKit
   Loaded: loaded (/etc/systemd/system/buildkit.service; enabled; vendor preset: disabled)
   Active: active (running) since Mon 2021-10-25 16:11:47 CST; 13s ago
     Docs: https://github.com/moby/buildkit
 Main PID: 26680 (buildkitd)
    Tasks: 7
   Memory: 13.5M
   CGroup: /system.slice/buildkit.service
           └─26680 /usr/local/bin/buildkitd --oci-worker=false --containerd-worker=true

Oct 25 16:11:47 containerd systemd[1]: Started BuildKit.
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=warning msg="using host network as the default"
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=info msg="found worker \"72ur53vv5olwy9wv0oc46...
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=info msg="found 1 workers, default=\"72u...cc6\""
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=warning msg="currently, only the default...used."
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=info msg="running server on /run/buildki....sock"
Hint: Some lines were ellipsized, use -l to show in full.
[root@containerd ~]#
```

可以看下日志

```bash
[root@containerd ~]#journalctl -u buildkit
-- Logs begin at Sat 2021-10-23 13:52:41 CST, end at Mon 2021-10-25 16:11:47 CST. --
Oct 25 16:11:47 containerd systemd[1]: Started BuildKit.
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=warning msg="using host network as the default"
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=info msg="found worker \"72ur53vv5olwy9wv0oc46bcc
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=info msg="found 1 workers, default=\"72ur53vv5olw
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=warning msg="currently, only the default worker c
Oct 25 16:11:47 containerd buildkitd[26680]: time="2021-10-25T16:11:47+08:00" level=info msg="running server on /run/buildkit/buildki
lines 1-7/7 (END)
```

现在我们再来重新构建镜像：

```bash
[root@containerd ~]#cd nerctl_demo/
[root@containerd nerctl_demo]#ls
Dockerfile
[root@containerd nerctl_demo]#nerdctl build  -t nginx:nerctl -f Dockerfile .
[+] Building 7.2s (6/6) FINISHED
 => [internal] load build definition from Dockerfile                                                                            0.0s
 => => transferring dockerfile: 131B                                                                                            0.0s
 => [internal] load .dockerignore                                                                                               0.0s
 => => transferring context: 2B                                                                                                 0.0s
 => [internal] load metadata for docker.io/library/nginx:alpine                                                                 4.1s
 => [1/2] FROM docker.io/library/nginx:alpine@sha256:686aac2769fd6e7bab67663fd38750c135b72d993d0bb0a942ab02ef647fc9c3           1.1s
 => => resolve docker.io/library/nginx:alpine@sha256:686aac2769fd6e7bab67663fd38750c135b72d993d0bb0a942ab02ef647fc9c3           0.0s
 => => extracting sha256:a0d0a0d46f8b52473982a3c466318f479767577551a53ffc9074c9fa7035982e                                       0.2s
 => => extracting sha256:4dd4efe90939ab5711aaf5fcd9fd8feb34307bab48ba93030e8b845f8312ed8e                                       0.8s
 => => extracting sha256:c1368e94e1ec563b31c3fb1fea02c9fbdc4c79a95e9ad0cac6df29c228ee2df3                                       0.0s
 => => extracting sha256:3e72c40d0ff43c52c5cc37713b75053e8cb5baea8e137a784d480123814982a2                                       0.0s
 => => extracting sha256:969825a5ca61c8320c63ff9ce0e8b24b83442503d79c5940ba4e2f0bd9e34df8                                       0.0s
 => => extracting sha256:61074acc7dd227cfbeaf719f9b5cdfb64711bc6b60b3865c7b886b7099c15d15                                       0.0s
 => [2/2] RUN echo 'Hello Nerdctl From Containerd' > /usr/share/nginx/html/index.html                                           0.5s
 => exporting to oci image format                                                                                               1.3s
 => => exporting layers                                                                                                         0.3s
 => => exporting manifest sha256:c5ab5ef3d410c1e7e8140eaf48f92c7b2a70d6f8d75a4bd26636db0e886101aa                               0.0s
 => => exporting config sha256:faa17ba50c10a48d128f1369bca7640083c48249239d9dd95ea30f88a4e387b5                                 0.0s
 => => sending tarball                                                                                                          0.9s
unpacking docker.io/library/nginx:nerctl (sha256:c5ab5ef3d410c1e7e8140eaf48f92c7b2a70d6f8d75a4bd26636db0e886101aa)...done
[root@containerd nerctl_demo]#nerdctl images
REPOSITORY    TAG       IMAGE ID        CREATED          SIZE
nginx         alpine    686aac2769fd    47 hours ago     24.9 MiB
nginx         nerctl    c5ab5ef3d410    9 seconds ago    24.9 MiB
[root@containerd nerctl_demo]#
```

构建完成后查看镜像是否构建成功：

```bash
[root@containerd nerctl_demo]#nerdctl images
REPOSITORY    TAG       IMAGE ID        CREATED          SIZE
nginx         alpine    686aac2769fd    47 hours ago     24.9 MiB
nginx         nerctl    c5ab5ef3d410    9 seconds ago    24.9 MiB
[root@containerd nerctl_demo]#
```

我们可以看到已经有我们构建的 nginx:nerdctl 镜像了。接下来使用上面我们构建的镜像来启动一个容器进行测试：

```bash
[root@containerd ~]#nerdctl ps -a
CONTAINER ID    IMAGE    COMMAND    CREATED    STATUS    PORTS    NAMES
[root@containerd ~]#nerdctl images
REPOSITORY    TAG       IMAGE ID        CREATED          SIZE
nginx         alpine    686aac2769fd    47 hours ago     24.9 MiB
nginx         nerctl    c5ab5ef3d410    4 minutes ago    24.9 MiB
[root@containerd ~]#nerdctl run -d -p 80:80 --name=nginx88  nginx:nerctl
1a5ae8262e78b3c0bf9e9da56789b9b6529e11ab7b53934841ada4e712210001
[root@containerd ~]#nerdctl ps -a
CONTAINER ID    IMAGE                             COMMAND                   CREATED          STATUS    PORTS                 NAMES
1a5ae8262e78    docker.io/library/nginx:nerctl    "/docker-entrypoint.…"    6 seconds ago    Up        0.0.0.0:80->80/tcp    nginx88
[root@containerd ~]#curl localhost
Hello Nerdctl From Containerd
[root@containerd ~]#
```

这样我们就使用 nerdctl + buildkitd 轻松完成了容器镜像的构建。完美。

当然如果你还想在单机环境下使用 Docker Compose，在 containerd 模式下，我们也可以使用 nerdctl 来兼容该功能。同样我们可以使用 nerdctl compose、nerdctl compose up、nerdctl compose logs、nerdctl compose build、nerdctl compose down 等命令来管理 Compose 服务。这样使用 containerd、nerdctl 结合 buildkit 等工具就完全可以替代 docker 在镜像构建、镜像容器方面的管理功能了。

```bash
[root@containerd ~]#nerdctl volume ls
VOLUME NAME    DIRECTORY
[root@containerd ~]#nerdctl network ls
NETWORK ID    NAME              FILE
0             bridge
              containerd-net    /etc/cni/net.d/10-containerd-net.conflist
              host
              none
[root@containerd ~]#nerdctl namespace ls
NAME        CONTAINERS    IMAGES    VOLUMES
buildkit    0             0         0
default     1             2         0
test        0             1         0
[root@containerd ~]#
```

四、需要注意的问题 
:question:(待解决)nerdctl run命令创建出的容器在宿主机上看不到端口号，奇怪
很奇怪，这个现象：

创建的容器映射到宿主机上的端口，在宿主机上没有查看的到。-->是个遗留问题，老师说正常情况是可以看到的才对。可能是底层走的是iptables。老师也没排查出来，当做遗留问题。

测试过程如下：

查看环境：

```bash
[root@containerd ~]#ctr i ls -q
docker.io/library/nginx:alpine
[root@containerd ~]#ctr t ls
TASK    PID    STATUS
[root@containerd ~]#netstat -ntlp #没有80端口被占用
Active Internet connections (only servers)
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name
tcp        0      0 127.0.0.1:42797         0.0.0.0:*               LISTEN      8116/containerd
tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      6587/sshd
tcp        0      0 127.0.0.1:25            0.0.0.0:*               LISTEN      6858/master
tcp6       0      0 :::22                   :::*                    LISTEN      6587/sshd
tcp6       0      0 ::1:25                  :::*                    LISTEN      6858/master
[root@containerd ~]#
```

创建容器：

```bash
[root@containerd ~]#nerdctl run -d -p 80:80 --name=nginx_bak docker.io/library/nginx:alpine
daf6ed8901335002c2edde96a3639da4a201f44a1ed74cb2b6a29221bf2603cb
[root@containerd ~]#
```

验证：

发现容器是创建成功了，但是宿主机上是没有出现这个80端口，很奇怪：

```bash
[root@containerd ~]#ctr t ls
TASK                                                                PID      STATUS
daf6ed8901335002c2edde96a3639da4a201f44a1ed74cb2b6a29221bf2603cb    28327    RUNNING
[root@containerd ~]#ctr c ls
CONTAINER                                                           IMAGE                             RUNTIME
daf6ed8901335002c2edde96a3639da4a201f44a1ed74cb2b6a29221bf2603cb    docker.io/library/nginx:alpine    io.containerd.runc.v2
[root@containerd ~]#ps -ef|grep nginx
root      28327  28296  0 07:17 ?        00:00:00 nginx: master process nginx -g daemon off;
101       28441  28327  0 07:17 ?        00:00:00 nginx: worker process
101       28442  28327  0 07:17 ?        00:00:00 nginx: worker process
root      28503  28223  0 07:41 pts/0    00:00:00 grep --color=auto nginx
[root@containerd ~]#
[root@containerd ~]#curl localhost
```
```html
<!DOCTYPE html>
<html>
<head>
<title>Welcome to nginx!</title>
<style>
html { color-scheme: light dark; }
body { width: 35em; margin: 0 auto;
font-family: Tahoma, Verdana, Arial, sans-serif; }
</style>
</head>
<body>
<h1>Welcome to nginx!</h1>
<p>If you see this page, the nginx web server is successfully installed and
working. Further configuration is required.</p>


<p>For online documentation and support please refer to
<a href="http://nginx.org/">nginx.org</a>.<br/>
Commercial support is available at
<a href="http://nginx.com/">nginx.com</a>.</p>


<p><em>Thank you for using nginx.</em></p>

</body>
</html>
```

```bash
[root@containerd ~]#

[root@containerd ~]#netstat -ntlp
Active Internet connections (only servers)
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name
tcp        0      0 127.0.0.1:42797         0.0.0.0:*               LISTEN      8116/containerd
tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      6587/sshd
tcp        0      0 127.0.0.1:25            0.0.0.0:*               LISTEN      6858/master
tcp6       0      0 :::22                   :::*                    LISTEN      6587/sshd
tcp6       0      0 ::1:25                  :::*                    LISTEN      6858/master
[root@containerd ~]#ctr c info daf6ed8901335002c2edde96a3639da4a201f44a1ed74cb2b6a29221bf2603cb|l
```

(待解决)还有个奇怪的问题就是：容器名被占用问题。。。


这个我就想不通，原来创建的容器已经被删除了，怎么创建同名的容器还报错呢。。。

(已解决)nerdctl为啥没有自动补全呢？
已解决！

和这个kubectl的一样，按这个方法配置下就可以自动补全命令了，666.

```bash
[root@containerd ~]#yum install -y epel-release bash-completion
[root@containerd ~]#source /usr/share/bash-completion/bash_completion
[root@containerd ~]#source <(nerdctl completion bash)
[root@containerd ~]#echo "source <(nerdctl completion bash)" >> ~/.bashrc
[root@containerd ~]#source ~/.bashrc
[root@containerd ~]#nerdctl r
rm   rmi  run
[root@containerd ~]#nerdctl r
```

注意：不推荐使用commit去构建对象，而是要用build去构建对象
不推荐使用commit去构建对象，而是要用build去构建对象。--->不知道为啥。。。。

```bash
build       Build an image from a Dockerfile. Needs buildkitd to be running.
commit      [flags] CONTAINER REPOSITORY[:TAG]
```

注意：用build构建镜像时，下面这个.问题。
docker的架构是cs架构：all请求，包括构建镜像



很多同学认为这里的点代表dockerfile的地址，其实这里的.和dockerfile的地址是有区别的：

dockerfile的地址你可以任意去指定；

其实这个点表示：构建上下文，就是你所要构建文件上下文是在哪些地方。就是你dockerfile里面的东西都是相对于你上下文的，因为你构建上下文all的事情都是上传到dockerd后台的，你不要考虑在本地。

因此，有个不好的情况就是，假如说你的dockerfile文件在/目录下：



可想而知，它会把你/目录下all文件全部上传到dockerd上面去，就会特别特别慢，会出问题的。

所以，这里的点构建上下文并不是随便指定的。



注意：containerd网络，它对接的是cni

```bash
[root@containerd ~]#nerdctl network ls
NETWORK ID    NAME              FILE
0             bridge
              containerd-net    /etc/cni/net.d/10-containerd-net.conflist
              host
              none
[root@containerd ~]#
```

/etc/cni/net.d/,这个其实是我们标准CNI的一个配置方式：

就是我们后面要讲的flannel都是放在这个下面的。

但是这种情况下，我们直接用这个配置文件去创建我们的k8s集群，即使你是创建的一个flannel网络插件，但是启动起来pod后，它还是不会使用这个flannel插件的。就是这个目录下的all conflist cni插件，好像是按这个字母顺序来加载的，所以会优先加载这个10-containerd-net.conflist的，也就是containerd本地的一个网络配置：



```bash
[root@containerd net.d]#cat 10-containerd-net.conflist
{
  "cniVersion": "0.4.0",
  "name": "containerd-net",
  "plugins": [
    {
      "type": "bridge",
      "bridge": "cni0",
      "isGateway": true,
      "ipMasq": true,
      "promiscMode": true,
      "ipam": {
        "type": "host-local",
        "ranges": [
          [{
            "subnet": "10.88.0.0/16"
          }],
          [{
            "subnet": "2001:4860:4860::/64"
          }]
        ],
        "routes": [
          { "dst": "0.0.0.0/0" },
          { "dst": "::/0" }
        ]
      }
    },
    {
      "type": "portmap",
      "capabilities": {"portMappings": true}
    }
  ]
}
[root@containerd net.d]#
```

就是一个bridge模式。

如果你自己要去创建网络也是可以的啊，和docker一样，其实都是对接的我们cni，你可以认为它是可以直接去对接的：

```bash
[root@containerd ~]#nerdctl network ls
NETWORK ID    NAME              FILE
0             bridge
              containerd-net    /etc/cni/net.d/10-containerd-net.conflist
              host
              none
[root@containerd ~]#
```

就是你可以把这个/etc/cni/net.d/10-containerd-net.conflist给删除掉，然后你自己在下面创建一个cni的配置，也是可以的。

所以，你后面在k8s里如果使用containerd来搭建集群的话，你需要把/etc/cni/net.d/10-containerd-net.conflist这个给移除掉，否则可能你创建出的pod它所使用的网段都是/etc/cni/net.d/10-containerd-net.conflist里面的"subnet": "2001:4860:4860::/64"，没有使用你的flannel插件配置或者你的其他网络插件。

注意：nerdctl在构建镜像时可以配置.dockerignore文件的。
也就是说在构建镜像时，可以配置下要忽略的文件，否则这个文件也一起会被提交到buildkitd后台的。