## Ubuntu22.04 配置静态IP

### 1 适用系统

本文介绍的静态 ip 的配置方法适用于 Ubuntu 系统的 18.04 及其以上版本。

#### 1.1 确认以太网连接的网络接口

一般情况下的个人 PC 只会有一张网卡，但在服务器中可能存在多张网卡的情况，使用 ifconfig 命令查看对应 ip 的网络接口。
若提示未找到 ifconfig 命令则使用如下命令按装该工具。

```bash
sudo apt-get install net-tools
```

#### 1.2 查看需要修改的网卡

如下可以查到当前所有的网络接口信息，由于我的主机只有一块网卡，因此就只对它修改即可。

```bash
ifconfig
```

```bash
docker0: flags=4099<UP,BROADCAST,MULTICAST>  mtu 1500
        inet 172.17.0.1  netmask 255.255.0.0  broadcast 172.17.255.255
        ether 02:42:25:ae:52:bf  txqueuelen 0  (Ethernet)
        RX packets 0  bytes 0 (0.0 B)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 0  bytes 0 (0.0 B)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0

ens33: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500
        inet 192.168.0.112  netmask 255.255.255.0  broadcast 192.168.0.255
        inet6 fe80::20c:29ff:febb:f83c  prefixlen 64  scopeid 0x20<link>
        inet6 2408:821b:9918:49f0:20c:29ff:febb:f83c  prefixlen 64  scopeid 0x0<global>
        ether 00:0c:29:bb:f8:3c  txqueuelen 1000  (Ethernet)
        RX packets 4303  bytes 514971 (514.9 KB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 5968  bytes 2078579 (2.0 MB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0

lo: flags=73<UP,LOOPBACK,RUNNING>  mtu 65536
        inet 127.0.0.1  netmask 255.0.0.0
        inet6 ::1  prefixlen 128  scopeid 0x10<host>
        loop  txqueuelen 1000  (Local Loopback)
        RX packets 91824  bytes 19697051 (19.6 MB)
        RX errors 0  dropped 0  overruns 0  frame 0
        TX packets 91824  bytes 19697051 (19.6 MB)
        TX errors 0  dropped 0 overruns 0  carrier 0  collisions 0
```



#### 1.3 修改网卡默认配置文件

其默认配置信息的路径为： `/etc/netplan/00-installer-config.yaml`，需要修改这个文件，修改前先备份：

```bash
sudo cp /etc/netplan/00-installer-config.yaml /etc/netplan/00-installer-config.yaml.bak
```

```bash
sudo vi /etc/netplan/00-installer-config.yaml
```



ubuntu20.04 / 18.04 参考以下配置

```yaml
network:
    ethernets:
        ens33:
            dhcp4: no
            addresses: [192.168.1.10/24]
            optional: true
            gateway4: 192.168.1.1
            nameservers:
                    addresses: [114.114.114.114,8.8.8.8]
version: 2
```



3.2. ubuntu22.04 参考以下配置

```yaml
# This is the network config written by 'subiquity'
network:
  ethernets:
    ens33:
      dhcp4: no
      addresses:
        - 192.168.0.112/24
      routes:
        - to: default
          via: 192.168.0.1
      nameservers:
        addresses:
          - 114.114.114.114
          - 8.8.8.8
      dhcp6: true
  version: 2
  renderer: networkd
```



应用该配置

```bash
sudo netplan apply
```

如果ip设置没有生效最后再重启系统即可

```bash
sudo reboot
```





## 路由器配置静态IP

另外可以在路由器中设置mac地址与ip绑定的方法视线ubuntu系统的静态ip效果

登录路由器设置页面，如： http://192.168.0.1/

1 打开ubuntu服务器，通过dhcp先分配一个ip

2 在路由器设置页 点击 应用管理-IP与MAC绑定

3 在 `IP与MAC映射表` 中找到ubuntu系统项，点击右边的`＋`号，添加到绑定设置

4 在下面的 `IP与MAC绑定设置` 中找到Ubuntu系统的绑定项，点击右边的图标进行编辑

5 编辑 主机名，ip地址信息，点击保存

6 重启ubuntu系统即可重新分配ip地址为上面设置的指定的ip地址





参考：

Ubuntu22.04 配置静态IP 

https://blog.csdn.net/qq_36393978/article/details/124868232



TP-LINK路由器如何设置联网设备的IP？ 

https://blog.csdn.net/qq_40640910/article/details/129569572