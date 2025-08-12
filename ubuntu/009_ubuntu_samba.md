# ubuntu 通过samba共享文件

## 1 ssh

安装

```shell
sudo apt install openssh-server
```

配置

```shell
sudo nano /etc/ssh/sshd_config
```

修改

```shell
PermitRootLogin no
```

开放端口

```shell
sudo ufw allow ssh
```

重启服务

```shell
systemctl daemon-reload
```

安装网络工具

```shell
sudo apt install net-tools
```

查看ip信息

```shell
ifconfig -a
```

通过ssh客户端远程登录访问



## 2 查看磁盘

```shell
sudo fdisk -l
```

输出如下

```shell
Disk /dev/sda: 119.24 GiB, 128035676160 bytes, 250069680 sectors
Disk model: ADATA SP900
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: gpt
Disk identifier: 9AB8F09D-45FF-47AB-B4EF-61882044530F

Device       Start       End   Sectors   Size Type
/dev/sda1     2048   2203647   2201600     1G EFI System
/dev/sda2  2203648   6397951   4194304     2G Linux swap
/dev/sda3  6397952 250066943 243668992 116.2G Linux filesystem


Disk /dev/sdb: 14.75 GiB, 15837691904 bytes, 30932992 sectors
Disk model: BORY M500 16G
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
Disklabel type: gpt
Disk identifier: 6B1C2DB5-C238-4A20-ADE8-B65384011A77

Device       Start      End  Sectors  Size Type
/dev/sdb1       34     2047     2014 1007K BIOS boot
/dev/sdb2     2048  1050623  1048576  512M EFI System
/dev/sdb3  1050624 30932958 29882335 14.2G Linux LVM


Disk /dev/sdd: 3.64 TiB, 4000787030016 bytes, 7814037168 sectors
Disk model: ST4000VN008-2DR1
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 4096 bytes
I/O size (minimum/optimal): 4096 bytes / 4096 bytes
Disklabel type: gpt
Disk identifier: 1F7B55B2-D30B-4BDF-951B-F193217C89C3

Device     Start        End    Sectors  Size Type
/dev/sdd1  32768 7814033407 7814000640  3.6T Microsoft basic data


Disk /dev/sdc: 3.64 TiB, 4000787030016 bytes, 7814037168 sectors
Disk model: ST4000VX007-2DT1
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 4096 bytes
I/O size (minimum/optimal): 4096 bytes / 4096 bytes
Disklabel type: gpt
Disk identifier: 24EBA7DC-79B7-4B3A-994A-3B584CC34267

Device     Start        End    Sectors  Size Type
/dev/sdc1   2048 7814037134 7814035087  3.6T Linux filesystem


Disk /dev/sdf: 7.28 TiB, 8001563222016 bytes, 15628053168 sectors
Disk model: ST8000VN004-2M21
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 4096 bytes
I/O size (minimum/optimal): 4096 bytes / 4096 bytes
Disklabel type: gpt
Disk identifier: 1BBE0B36-96AB-42CC-B1C2-CC7FEFB64CC1

Device     Start         End     Sectors  Size Type
/dev/sdf1   2048 15628053134 15628051087  7.3T Linux filesystem


Disk /dev/sde: 1.82 TiB, 2000398934016 bytes, 3907029168 sectors
Disk model: ST2000DM001-1CH1
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 4096 bytes
I/O size (minimum/optimal): 4096 bytes / 4096 bytes
Disklabel type: gpt
Disk identifier: 82F588E6-24C6-4904-8737-3E058F889C3A

Device     Start        End    Sectors  Size Type
/dev/sde1   2048 3907029134 3907027087  1.8T Linux filesystem


Disk /dev/mapper/pve-swap: 1.75 GiB, 1879048192 bytes, 3670016 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes


Disk /dev/mapper/pve-root: 3.5 GiB, 3758096384 bytes, 7340032 sectors
Units: sectors of 1 * 512 = 512 bytes
Sector size (logical/physical): 512 bytes / 512 bytes
I/O size (minimum/optimal): 512 bytes / 512 bytes
```

找到要挂载的各个磁盘分区的名字:

```shell
...
Device     Start        End    Sectors  Size Type
/dev/sdd1  32768 7814033407 7814000640  3.6T Microsoft basic data

...
Device     Start        End    Sectors  Size Type
/dev/sdc1   2048 7814037134 7814035087  3.6T Linux filesystem

...
Device     Start         End     Sectors  Size Type
/dev/sdf1   2048 15628053134 15628051087  7.3T Linux filesystem

...
Device     Start        End    Sectors  Size Type
/dev/sde1   2048 3907029134 3907027087  1.8T Linux filesystem
```



2 查看分区信息

```shell
sudo blkid /dev/sdc1
sudo blkid /dev/sdd1
sudo blkid /dev/sde1
sudo blkid /dev/sdf1
```

分别输出如下：

```shell
/dev/sdc1: UUID="5f0a68b0-0833-4e98-9ec6-1ee284b74539" BLOCK_SIZE="4096" TYPE="ext4" PARTUUID="9d1db31f-4e1e-8b4d-9333-6decb83550b6"
```

```shell
/dev/sdd1: LABEL="media" BLOCK_SIZE="512" UUID="049C8DD89C8DC4A0" TYPE="ntfs" PARTLABEL="Basic data partition" PARTUUID="5e47ce2e-8beb-464e-afc0-2c109f137cc9"
```

```shell
/dev/sde1: UUID="dad1f23f-a06c-4c91-a174-007a2679d52c" BLOCK_SIZE="4096" TYPE="ext4" PARTLABEL="Linux filesystem" PARTUUID="c43ac682-f5ab-46c3-b4ec-3bdcf7e60c05"
```

```shell
/dev/sdf1: UUID="745d1451-4157-4855-bdb1-aff6fb873081" BLOCK_SIZE="4096" TYPE="ext4" PARTUUID="7ebe464e-a3c3-7a40-a12f-302a239555f3"
```

可以查询到各个磁盘分区的 id 和文件系统



## 3 挂载目录

创建挂在目录

```shell
sudo mkdir -p /mnt/HomeDisk01
sudo mkdir -p /mnt/HomeDisk02
sudo mkdir -p /mnt/HomeDisk03
sudo mkdir -p /mnt/HomeDisk04
```

修改分区表

```shell
sudo nano /etc/fstab
```

添加

```shell
PARTUUID=9d1db31f-4e1e-8b4d-9333-6decb83550b6 /mnt/HomeDisk01 ext4 defaults 0 2
PARTUUID=5e47ce2e-8beb-464e-afc0-2c109f137cc9 /mnt/HomeDisk02 ntfs defaults 0 2
PARTUUID=c43ac682-f5ab-46c3-b4ec-3bdcf7e60c05 /mnt/HomeDisk03 ext4 defaults 0 2
PARTUUID=7ebe464e-a3c3-7a40-a12f-302a239555f3 /mnt/HomeDisk04 ext4 defaults 0 2
```

重启系统

```shell
sudo reboot
```



## 4 通过samba分享文件夹

安装

```shell
sudo apt install samba
```

创建samba账号及设置密码

```shell
sudo smbpasswd -a my_username_xxx
```

将上面指令中的 `my_username_xxx` 替换成想要的用户名即可

然后再输入密码及确认密码



共享目录， 修改配置

```shell
sudo nano /etc/samba/smb.conf
```

在文件末尾添加:

```
[HomeDisk01]
    path = /mnt/HomeDisk01
    read only = no
    guest ok = yes


[HomeDisk02]
    path = /mnt/HomeDisk02
    read only = no
    guest ok = yes


[HomeDisk03]
    path = /mnt/HomeDisk03
    read only = no
    guest ok = yes


[HomeDisk04]
    path = /mnt/HomeDisk04
    read only = no
    guest ok = yes
```



重启samba服务

```
sudo service smbd restart
```



开放端口

```
sudo ufw allow samba
```

