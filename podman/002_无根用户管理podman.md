## 无根用户管理podman

### podman安装

[root@localhost ~]# dnf install -y podman

Failed to set locale, defaulting to C.UTF-8

CentOS Stream 8 - AppStream 2.1 kB/s | 4.4 kB 00:02

CentOS Stream 8 - AppStream 4.4 MB/s | 24 MB 00:05

CentOS Stream 8 - BaseOS 381 B/s | 3.9 kB 00:10

CentOS Stream 8 - BaseOS 3.0 MB/s | 25 MB 00:08

CentOS Stream 8 - Extras 5.8 kB/s | 2.9 kB 00:00

Dependencies resolved.

=================================================================================================================================================================================================

Package Architecture Version Repository Size

=================================================================================================================================================================================================

Installing:

podman x86_64 2:4.0.2-1.module_el8.7.0+1106+45480ee0 appstream 13 M

Upgrading:

libsemanage x86_64 2.9-8.el8 baseos 168 k

配置加速器
[root@localhost ~]# vim /etc/containers/registries.conf

unqualified-search-registries = ["docker.io"]


[[registry]]

prefix = "docker.io"

location = "4e3uv4d0.mirror.aliyuncs.com"
用户操作
在允许没有root特权的用户运行Podman之前，管理员必须安装或构建Podman并完成以下配置 
cgroup V2Linux内核功能允许用户限制普通用户容器可以使用的资源，如果使用cgroupV2启用了运行Podman的Linux发行版，则可能需要更改默认的OCI运行时。某些较旧的版本runc不适用于cgroupV2，必须切换到备用OCI运行时crun。

更改默认的OCI
[root@localhost ~]# dnf -y install crun

Failed to set locale, defaulting to C.UTF-8

Last metadata expiration check: 23:22:18 ago on Tue Aug 16 00:31:18 2022.

Dependencies resolved.

=================================================================================================================================================================================================

Package Architecture Version Repository Size

=================================================================================================================================================================================================

Installing:

crun x86_64 1.4.3-1.module_el8.7.0+1106+45480ee0 appstream 209 k

Complete!

[root@localhost ~]# vim /usr/share/containers/containers.conf

runtime = "crun" //取消注释

#runtime = "runc" //注释此行

[root@localhost ~]# podman run -d --name web nginx

Resolving "nginx" using unqualified-search registries (/etc/containers/registries.conf)

Trying to pull docker.io/library/nginx:latest...

Getting image source signatures

Copying blob 186b1aaa4aa6 done

Copying blob a2abf6c4d29d done

Copying blob b4df32aa5a72 done

Copying blob a0bcbecc962e done

Copying blob a9edb18cadd1 done

Copying blob 589b7251471a done

Copying config 605c77e624 done

Writing manifest to image destination

Storing signatures

e7d07749f9d639da39d3d04c6837a2628b6965f7554c3b65801751350c725462

[root@localhost ~]# podman ps

CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES

e7d07749f9d6 docker.io/library/nginx:latest nginx -g daemon o... 38 seconds ago Up 38 seconds ago web

[root@localhost ~]# podman inspect web | grep -i ociruntime

"OCIRuntime": "crun",

安装slirp4netns和fuse-overlayfs
[root@localhost ~]# dnf -y install slirp4netns fuse-overlayfs

Failed to set locale, defaulting to C.UTF-8

Last metadata expiration check: 23:28:26 ago on Tue Aug 16 00:31:18 2022.

Package slirp4netns-1.1.8-2.module_el8.7.0+1106+45480ee0.x86_64 is already installed.

Package fuse-overlayfs-1.8.2-1.module_el8.7.0+1106+45480ee0.x86_64 is already installed.

Dependencies resolved.

Nothing to do.

Complete!

[root@localhost ~]# vim /etc/containers/storage.conf

92 mount_program = "/usr/bin/fuse-overlayfs" //取消注释

配置subuid及subgid

[root@localhost ~]# dnf -y install shadow-utils

Failed to set locale, defaulting to C.UTF-8

Last metadata expiration check: 23:30:35 ago on Tue Aug 16 00:31:18 2022.

Package shadow-utils-2:4.6-12.el8.x86_64 is already installed.

Dependencies resolved.

=================================================================================================================================================================================================

Package Architecture Version Repository Size

=================================================================================================================================================================================================

Upgrading:

shadow-utils x86_64 2:4.6-17.el8 baseos 1.2 M

Complete!

[root@localhost ~]# useradd lisi

[root@localhost ~]# useradd zhangsan

[root@localhost ~]# cat /etc/subuid

lisi:100000:65536

zhangsan:165536:65536

[root@localhost ~]# cat /etc/subgid

lisi:100000:65536

zhangsan:165536:65536


[root@localhost ~]# usermod --add-subuids 200000-201000 --add-subgids 200000-201000 lisi //usermod修改起始id

[root@localhost ~]# cat /etc/subuid

lisi:100000:65536

zhangsan:165536:65536

lisi:200000:1001

[root@localhost ~]# cat /etc/subgid

lisi:100000:65536

zhangsan:165536:65536

lisi:200000:1001

启动非特权ping
[root@localhost ~]# sysctl -w "net.ipv4.ping_group_range=0 200000"

net.ipv4.ping_group_range = 0 200000

[root@localhost ~]# vim /etc/sysctl.conf //修改配置文件永久生效

net.ipv4.ping_group_range = 0 200000 //0 200000就是从100000开始，到200000区间内的用户都可使用podman
修改用户配置文件
[root@localhost ~]# vim /etc/containers/storage.conf

[storage]

driver = "overlay" //修改为overlay

runroot = "/run/containers/storage" //默认

graphroot = "/var/lib/containers/storage" //默认


mount_program = "/usr/bin/fuse-overlayfs" //取消注释

[root@localhost ~]# vim /etc/sysctl.conf //设置无根用户数量

user.max_user_namespaces = 15000

授权文件

[root@localhost ~]# podman login

Username: zzh2002

Password:

Login Succeeded!

[root@localhost ~]# cat /run/user/0/containers/auth.json

{

"auths": {

"docker.io": {

"auth": "enpoMjAwMjo3MzgyODk4WlpIIQ=="

}

}

}

无根用户(普通用户)是无法看见根用户(root用户)的镜像的;相反根用户也是无法看见无根用户的镜像

[root@localhost ~]# podman images //root用户

REPOSITORY TAG IMAGE ID CREATED SIZE

docker.io/library/nginx latest 605c77e624dd 7 months ago 146 MB

[root@localhost ~]# su - zhangsan

[zhangsan@localhost ~]$ podman images //普通用户

REPOSITORY TAG IMAGE ID CREATED SIZE

无根用户的卷
容器与root用户一起运行，则root容器中的用户实际上就是主机上的用户。 
UID GID是在/etc/subuid和/etc/subgid等中用户映射中指定的第一个UID GID。 
如果普通用户的身份从主机目录挂载到容器中，并在该目录中以根用户身份创建文件，则会看到它实际上是你的用户在主机上拥有的。

[root@localhost ~]# su - zhangsan

[zhangsan@localhost ~]$ mkdir wangwu

[zhangsan@localhost ~]$ podman run -it -v `pwd`/wangwu:/wangwu docker.io/library/busybox /bin/sh

Trying to pull docker.io/library/busybox:latest...

Getting image source signatures

Copying blob 5cc84ad355aa done

Copying config beae173cca done

Writing manifest to image destination

Storing signatures

/ # ls -ld wangwu/

drwxrwxr-x 2 root root 6 Aug 17 04:38 wangwu/

/ # touch 123

/ # ls -l

total 16

-rw-r--r-- 1 root root 0 Aug 17 04:39 123

[zhangsan@localhost ~]$ ll wangwu/

total 0

-rw-r--r-- 1 lis1 lis1 0 Aug 17 04：44 123

[zhangsan@localhost ~]$ echo "hello world!" > wangwu/123

[zhangsan@localhost ~]$ cat wangwu/123

hello world!

[zhangsan@localhost ~]$ podman start -l

d0857c885b828149211d5845a48c310664c393f09315f2a64d3a70471ed5d8d4

[zhangsan@localhost ~]$ podman exec -it -l /bin/sh

/ # cat wangwu/123

hello world！

普通用户映射容器端口
[zhangsan@localhost ~]$ podman run -d -p 82:80 nginx //使用普通用户映射容器端口时会报“ permission denied”的错误

Resolving "nginx" using unqualified-search registries (/etc/containers/registries.conf)

Trying to pull docker.io/library/nginx:latest...

Getting image source signatures

Copying blob 186b1aaa4aa6 done

Copying blob a0bcbecc962e done

Copying blob 589b7251471a done

Copying blob b4df32aa5a72 done

Copying blob a2abf6c4d29d done

Copying blob a9edb18cadd1 done

Copying config 605c77e624 done

Writing manifest to image destination

Storing signatures

Error: rootlessport cannot expose privileged port 82, you can add 'net.ipv4.ip_unprivileged_port_start=82' to /etc/sysctl.conf (currently 1024), or choose a larger port number (>= 1024): listen tcp 0.0.0.0:82: bind: permission denied


[zhangsan@localhost ~]$ su root //到root用户修改配置文件，生效配置

Password:

[root@localhost zhangsan]# cd

[root@localhost ~]# vim /etc/sysctl.conf

net.ipv4.ip_unprivileged_port_start = 80

[root@localhost ~]# sysctl -p

net.ipv4.ping_group_range = 0 200000

user.max_user_namespaces = 15000

net.ipv4.ip_unprivileged_port_start = 80


[root@localhost ~]# su - zhangsan 到无根用户做端口映射

Last login: Wed Aug 17 00:45:18 EDT 2022 on pts/4

[zhangsan@localhost ~]$ podman run -d -p 82:80 nginx

f1be5b2e9c8107b02588e7e9a0fecb439a8cd0658fae2b41429a992d22a4ce3a

[zhangsan@localhost ~]$ podman ps

CONTAINER ID IMAGE COMMAND CREATED STATUS PORTS NAMES

d0857c885b82 docker.io/library/busybox:latest /bin/sh 10 minutes ago Up 7 minutes ago upbeat_nightingale

f1be5b2e9c81 docker.io/library/nginx:latest nginx -g daemon o... 8 seconds ago Up 8 seconds ago 0.0.0.0:82->80/tcp unruffled_wilson

大多数使用 Podman 运行的容器和 Pod 都遵循几个简单的场景。默认情况下，rootful Podman 将创建一个桥接网络。这是 Podman 最直接和首选的网络设置。桥接网络在内部桥接网络上为容器创建一个接口，然后通过网络地址转换 (NAT) 连接到 Internet。我们还看到用户也希望macvlan 用于网络。这macvlan插件将整个网络接口从主机转发到容器中，允许它访问主机所连接的网络。最后，无根容器的默认网络配置是 slirp4netns。slirp4netns 网络模式功能有限，但可以在没有 root 权限的用户上运行。它创建从主机到容器的隧道以转发流量。

无根用户容器网络设置
[zhangsan@localhost ~]$ podman run -itd --name wrq busybox

8f136a43a814aac23e0e1dacb3bab63aba4dc1556f9c8e1c2dc798b1da373f49

[zhangsan@localhost ~]$ podman inspect -l |grep -i ipaddress

"IPAddress": "",

[zhangsan@localhost ~]$ podman exec -it -l /bin/sh //没有ip不影响访问外网，会生成一张tap0的虚拟网卡

/ # ip a

1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue qlen 1000

link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00

inet 127.0.0.1/8 scope host lo

valid_lft forever preferred_lft forever

inet6 ::1/128 scope host

valid_lft forever preferred_lft forever

2: tap0: <BROADCAST,UP,LOWER_UP> mtu 65520 qdisc fq_codel qlen 1000

link/ether 3e:be:ab:e8:30:e6 brd ff:ff:ff:ff:ff:ff

inet 10.0.2.100/24 brd 10.0.2.255 scope global tap0

valid_lft forever preferred_lft forever

inet6 fd00::3cbe:abff:fee8:30e6/64 scope global dynamic flags 100

valid_lft 86382sec preferred_lft 14382sec

inet6 fe80::3cbe:abff:fee8:30e6/64 scope link

valid_lft forever preferred_lft forever

/ # ping www.baidu.com

PING www.baidu.com (14.215.177.39): 56 data bytes

64 bytes from 14.215.177.39: seq=0 ttl=255 time=28.320 ms

64 bytes from 14.215.177.39: seq=1 ttl=255 time=27.437 ms

^C

--- www.baidu.com ping statistics ---

2 packets transmitted, 2 packets received, 0% packet loss

round-trip min/avg/max = 27.437/27.878/28.320 ms

有根容器网络和无根容器网络之间的区别: 
odman 容器联网的指导因素之一是容器是否由 root 用户运行。这是因为非特权用户无法在主机上创建网络接口。因此，对于无根容器，默认的网络模式是 slirp4netns。由于权限的限制，slirp4netns 相比 rootful Podman 的联网，缺乏联网的一些特性；例如，slirp4netns 不能给容器一个可路由的 IP 地址。另一端的 rootful 容器的默认联网模式是 netavark，它允许容器有一个可路由的 IP 地址。