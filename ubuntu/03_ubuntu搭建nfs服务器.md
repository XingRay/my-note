## ubuntu搭建nfs服务器

nfs 网络存储系统。NFS服务是一个在linux间共享文件的功能。比如A服务器有文件，B，C，D都想访问这个文件，就可以把A服务上的文件共享出来。NFS就是实现这个功能的。



### 搭建nfs

选择一个节点作为nfs服务器，其他的为客户端

服务器A是192.168.0.112　　作为服务端

服务器B是192.168.0.113　　作为客户端

服务器C是192.168.0.114　　作为客户端



#### 搭建服务端

在服务器A上安装nfs，安装 NFS服务器端

```bash
sudo apt-get install -y nfs-kernel-server
```



启动服务

```bash
sudo systemctl enable nfs-server
sudo systemctl start nfs-server
```



查看服务

```bash
sudo systemctl status nfs-server
```



创建一个共享文件夹，增加权限

```bash
sudo mkdir -p /nfs/data
```

打开配置参数文件，设置配置文件

```bash
sudo vim /etc/exports
```

输入设置参数，允许192.168下所有网段都可以访问

```bash
/nfs/data 192.168.0.0/24(insecure,rw,no_root_squash,sync)
```

注意：网段不能写成 `192.168.*.*` 这样写会导致无法挂载

保存并退出

参数说明：

| 参数           | 作用                                                         |
| -------------- | ------------------------------------------------------------ |
| ro             | 只读                                                         |
| rw             | 读写                                                         |
| root_squash    | 当NFS客户端以root管理员访问时，映射为NFS服务器的匿名用户     |
| no_root_squash | 当NFS客户端以root管理员访问时，映射为NFS服务器的root管理员   |
| all_squash     | 无论NFS客户端使用什么账户访问，均映射为NFS服务器的匿名用户   |
| sync           | 同时将数据写入到内存与硬盘中，保证不丢失数据                 |
| async          | 优先将数据保存到内存，然后再写入硬盘，这样效率高，但是可能会丢失数据 |

重启nfs服务

```bash
sudo systemctl restart nfs-server
```



#### 配置客户端

客户端安装nfs工具

```bash
sudo apt-get install -y nfs-common
```

查看服务器的挂载点

```bash
showmount -e 192.168.0.112
```

创建文件目录

```bash
sudo mkdir -p /nfs/data
```

挂载

```bash
sudo mount -t nfs 192.168.0.112:/nfs/data /nfs/data
```

查看挂载结果

```bash
df -h
```

```bash
root@k8s-node01:~# df -h
Filesystem               Size  Used Avail Use% Mounted on
tmpfs                    388M  2.7M  386M   1% /run
/dev/sda2                 40G  9.6G   28G  26% /
tmpfs                    1.9G     0  1.9G   0% /dev/shm
tmpfs                    5.0M     0  5.0M   0% /run/lock
192.168.0.112:/nfs/data   40G  9.2G   28G  25% /nfs/data
```

可以看到已经成功挂载 /nfs/data

取消挂载

```bash
sudo umount /nfs/data
```



