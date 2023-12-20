# 小记Podman基本操作.1

Ubuntu22.04, root环境下进行



# 安装

```shell
apt install podman
```



# 修改国内镜像源

修改全局配置文件`/etc/containers/registries.conf`，如果非root用户，也可以通过修改`~/.config/containers/registries.conf`文件去覆盖全局配置

```shell
root@strluck-ubuntu:~# vim /etc/containers/registries.conf

# 删除文件中注解项，并修改内容
unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "hub-mirror.c.163.com"
```



# 修改存储配置

由于我在安装`Ubuntu`的时候并没有给`/var`目录分配多大的容量，所以避免日后使用时出现问题，对存储位置进行修改

查看一下podman的信息

```shell
root@strluck-ubuntu:~# podman info
    ......
store:
  configFile: /etc/containers/storage.conf
  containerStore:
    number: 0
    paused: 0
    running: 0
    stopped: 0
  graphDriverName: overlay
  graphOptions: {}
  graphRoot: /var/lib/containers/storage
  graphStatus:
    Backing Filesystem: extfs
    Native Overlay Diff: "true"
    Supports d_type: "true"
    Using metacopy: "false"
  imageStore:
    number: 0
  runRoot: /run/containers/storage
  volumePath: /var/lib/containers/storage/volumes
    ......
```

可以看到比较有用的四个参数

```shell
# 存储配置文件位置
configFile: /etc/containers/storage.conf
# 容器存储的主要读/写位置
graphRoot: /var/lib/containers/storage
# 临时存放位置
runRoot: /run/containers/storage
# 数据卷存放位置
volumePath: /var/lib/containers/storage/volumes
```

查看一下存储配置文件

```shell
root@strluck-ubuntu:~# cat /etc/containers/storage.conf
cat: /etc/containers/storage.conf: No such file or directory
```

emmm...文件不存在，怪得很

既然存储配置文件不存在，那就直接给`/var/lib/containers`目录挂载一个大点的存储设备

首先也是检查一下目录是否存在

```shell
root@strluck-ubuntu:~# tree /var/lib/containers
/var/lib/containers
└── storage
    ├── libpod
    │   ├── bolt_state.db
    │   └── defaultCNINetExists
    ├── mounts
    ├── overlay
    │   └── l
    ├── overlay-containers
    │   └── containers.lock
    ├── overlay-images
    │   └── images.lock
    ├── overlay-layers
    │   └── layers.lock
    ├── storage.lock
    ├── tmp
    └── userns.lock
```

然后对磁盘进行分区，挂载操作

查看所有硬盘

```shell
root@strluck-ubuntu:~# fdisk -l
    ......
Disk /dev/sda: 476.94 GiB, 512110190592 bytes, 1000215216 sectors
Disk model: ZHITAI SC001 Act
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: gpt
Disk identifier: DFA1CEE2-290C-43D7-85CE-9AD09B88A232

Device      Start          End      Sectors   Size Type
/dev/sda1    2048   1000214527   1000212480   476.9G Linux filesystem
```

选择一个空闲的硬盘，我这里刚装上去一个新的固态，看一下设备名为`/dev/sda`

尝试将已挂载的硬盘卸载

```shell
umount -l /dev/sda1
```

如果卸载还未挂载的硬盘，也会有如下的提示

```shell
root@strluck-ubuntu:~# umount /dev/sda1
umount: /dev/sda1: not mounted.
```

对硬盘进行重新分区，这里我使用`cfdisk`方便一些，也可以直接使用`fdisk`命令进行分区

```shell
root@strluck-ubuntu:~# cfdisk /dev/sda

# 确认一下设备名是否正确，然后回车键开始分区
                      Disk: /dev/sda
  Size: 476.94 GiB, 512110190592 bytes, 1000215216 sectors
Label: gpt, identifier: DFA1CEE2-290C-43D7-85CE-9AD09B88A232

    Device       Start          End      Sectors   Size Type
>>  Free space    2048   1000212446   1000210399   476.9G

# 这里我把我的硬盘分为一个300g一个176g
Partition size: 300G
Partition size: 176.9G

    Device           Start          End     Sectors    Size Type
>>  /dev/sda1         2048    629147647   629145600    300G Linux filesystem
    /dev/sda2    629147648   1000215182   371067535    176.9G Linux filesystem

# 选择Write写入操作
[ Delete ]  [ Resize ]  [  Quit  ]  [  Type  ]  [  Help  ]  [  Write ]  [  Dump  ]
Are you sure you want to write the partition table to disk? yes
```

格式化分区

```shell
mkfs.ext4 /dev/sda1
mkfs.ext4 /dev/sda2
```

将准备用于容器存储的盘挂载到一个临时目录，并将`/var/lib/containers`中的文件拷贝到新的存储位置中

```shell
mkdir /tmp/containers
mount /dev/sda2 /tmp/containers
cp -r /var/lib/containers/* /tmp/containers
```

将磁盘卸载，并挂载到`/var/lib/containers`

```shell
umount /dev/sda2
mount /dev/sda2 /var/lib/containers
```

修改`/etc/fstab`文件使得开机自动挂载磁盘

首先查看设备的uuid

```shell
root@strluck-ubuntu:~# ls -l /dev/disk/by-uuid/
total 0
lrwxrwxrwx 1 root root 15 May 21 15:17 0016-82F4 -> ../../nvme0n1p1
lrwxrwxrwx 1 root root 15 May 21 15:17 0afb5de8-1cc8-402f-9182-74e84e493f69 -> ../../nvme0n1p4
lrwxrwxrwx 1 root root 15 May 21 15:17 21052882-6adf-42a8-afc0-ccabd1018096 -> ../../nvme0n1p2
lrwxrwxrwx 1 root root 10 May 21 15:26 404c1a2f-14a7-492e-8258-7c287bd91a03 -> ../../sda1
lrwxrwxrwx 1 root root 10 May 21 15:27 65491e28-b941-4533-b032-d4d34275c423 -> ../../sda2
lrwxrwxrwx 1 root root 15 May 21 15:17 79d2cdad-0ff9-4da9-8632-550ec3cc5204 -> ../../nvme0n1p5
lrwxrwxrwx 1 root root 15 May 21 15:17 b2f8808d-4819-477c-81e2-14c1def2b39b -> ../../nvme0n1p3
lrwxrwxrwx 1 root root 15 May 21 15:17 f6d8aa62-49f9-49d1-a6a9-902ed6d32041 -> ../../nvme0n1p6
```

找到要挂载的设备，我这里是`65491e28-b941-4533-b032-d4d34275c423 -> ../../sda2`

修改`/etc/fstab`文件

```shell
root@strluck-ubuntu:~# vim /etc/fstab
# 在末尾添加，注意设备的uuid
UUID=65491e28-b941-4533-b032-d4d34275c423 /var/lib/containers auto defaults 0 0
```



# 测试

跑一个hello world

```shell
root@strluck-ubuntu:~# podman run hello-world

Hello from Docker!
......
```

（podman容器显示docker，这何尝不是一种___

最后把`hello-world`删除

```shell
# 查看容器id
root@strluck-ubuntu:~# podman ps -a
CONTAINER ID  IMAGE                                 COMMAND  CREATED        STATUS                    PORTS       NAMES
54d8c0a4fca8  docker.io/library/hello-world:latest  /hello   9 minutes ago  Exited (0) 9 minutes ago  objective_varahamihira

# 删除容器
root@strluck-ubuntu:~# podman rm 54d8c0a4fca8
54d8c0a4fca892a6cf1b3996bfdebc592401426cd5f248bbe68f0d68022820bf

# 查看镜像id
root@strluck-ubuntu:~# podman images
REPOSITORY                     TAG         IMAGE ID      CREATED       SIZE
docker.io/library/hello-world  latest      feb5d9fea6a5  7 months ago  19.9 kB

# 删除镜像
root@strluck-ubuntu:~# podman rmi feb5d9fea6a5
Untagged: docker.io/library/hello-world:latest
Deleted: feb5d9fea6a5e9606aa995e879d862b825965ba48de054caab5ef356dc6b3412
```