## Vagrant 创建虚拟机集群

### 1 创建虚拟机

在指定目录下创建一个文件，文件名为 `Vagrantfile`

```bash
Vagrant.configure("2") do |config|
   (1..3).each do |i|
        config.vm.define "k8s-node#{i}" do |node|
            # 设置虚拟机的Box
            node.vm.box = "centos/7"

            # 设置虚拟机的主机名
            node.vm.hostname="k8s-node#{i}"

            # 设置虚拟机的IP
            node.vm.network "private_network", ip: "192.168.56.#{99+i}", netmask: "255.255.255.0"

            # 设置主机与虚拟机的共享目录
            # node.vm.synced_folder "~/Documents/vagrant/share", "/home/vagrant/share"

            # VirtaulBox相关配置
            node.vm.provider "virtualbox" do |v|
                # 设置虚拟机的名称
                v.name = "k8s-node#{i}"
                # 设置虚拟机的内存大小
                v.memory = 4096
                # 设置虚拟机的CPU个数
                v.cpus = 4
            end
        end
   end
end
```

在此目录下运行 

```bash
vagrant up
```

即可，vagrant会根据 `Vagrantfile` 脚本创建虚拟机



### 2 配置ssh连接

创建完成后，此时3台虚拟机还不能直接用ssh账号密码连接，需要进行ssh访问设置，在宿主机下通过指令

```bash
vagrant ssh k8s-node1
```

连接上第一台虚拟机，虚拟及的名称 k8s-node1 是由脚本中 `node.vm.hostname="k8s-node#{i}"`设置的。

连接上之后切换至root用户

```bash
su root
```

密码是 vagrant

编辑ssh配置文件

```bash
vi /etc/ssh/sshd_config
```

输入 `i` 进入编辑模式，向下找到 

```bash
PasswordAuthentication no
```

修改为

```bash
PasswordAuthentication yes
```

按 `esc` 键退出编辑模式，再输入 `:` 进入命令模式，输入 `wq` 保存并退出

重启`sshd`服务

```bash
service sshd restart
```

输入

```bash
exit
```

退出root用户，再输入一次退出虚拟机。

上述流程在 `k8s-node2` `k8s-node3` 上分别执行一次。

这样就可以使用shell工具通过账号密码使用ssh连接这3台虚拟机了



### 3 配置虚拟机网络

通过ssh工具连接进入虚拟机后，输入

```bash
ip route show
```

显示网络信息如下：

```bash
default via 10.0.2.2 dev eth0 proto dhcp metric 101
10.0.2.0/24 dev eth0 proto kernel scope link src 10.0.2.15 metric 101
192.168.56.0/24 dev eth1 proto kernel scope link src 192.168.56.100 metric 100
```

可以看到默认网卡为 eth0

通过指令查看ip信息：

```bash
ip addr
```

```bash
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 52:54:00:4d:77:d3 brd ff:ff:ff:ff:ff:ff
    inet 10.0.2.15/24 brd 10.0.2.255 scope global noprefixroute dynamic eth0
       valid_lft 84354sec preferred_lft 84354sec
    inet6 fe80::5054:ff:fe4d:77d3/64 scope link
       valid_lft forever preferred_lft forever
3: eth1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 08:00:27:ef:f5:ad brd ff:ff:ff:ff:ff:ff
    inet 192.168.56.100/24 brd 192.168.56.255 scope global noprefixroute eth1
       valid_lft forever preferred_lft forever
    inet6 fe80::a00:27ff:feef:f5ad/64 scope link
       valid_lft forever preferred_lft forever
```

会发现3台的ip地址相同，都是 10.0.2.15 ，原因是创建虚拟机时使用的是网络地址转换，使用相同的ip地址，通过端口映射进行转发。在搭建k8s集群时不能使用这种模式，因此需要进行修改。

打开virtualbox，将这三台虚拟机关闭(关闭电源/poweroff)。

点击virtualbox 的tools菜单，找到`NAT Networks`点击 左上角`Create`  按钮，创建一个 Nat网络。

在虚拟机列表中，找到 k8s-node1，右键弹出菜单，或者右边的页面中点击 settings 。在弹出的settings窗口中点击左边的network，在网络设置中点击`adapter1`，在 `Attached To` 中选择 `NAT Network`，下面的Name中会自动选择创建的NAT网络。点击`Advanced`打开高级选项，点击`MAC Address`后面的按钮重新生成MAC地址，如果不重新生成可能会导致mac冲突。点击`OK`。

配置完成后选中3台虚拟机，右键`start`=> `headless start` 使用无界面启动



启动之后再此查看默认网卡：

```bash
ip route show
```

```bash
default via 10.0.2.1 dev eth0 proto dhcp metric 101
10.0.2.0/24 dev eth0 proto kernel scope link src 10.0.2.15 metric 101
192.168.56.0/24 dev eth1 proto kernel scope link src 192.168.56.100 metric 100
```

可以看到默认网卡还是 eth0，查看ip信息

```bash
ip addr
```

```bash
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 08:00:27:50:0b:66 brd ff:ff:ff:ff:ff:ff
    inet 10.0.2.15/24 brd 10.0.2.255 scope global noprefixroute dynamic eth0
       valid_lft 451sec preferred_lft 451sec
    inet6 fe80::a00:27ff:fe50:b66/64 scope link
       valid_lft forever preferred_lft forever
3: eth1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 08:00:27:ef:f5:ad brd ff:ff:ff:ff:ff:ff
    inet 192.168.56.100/24 brd 192.168.56.255 scope global noprefixroute eth1
       valid_lft forever preferred_lft forever
    inet6 fe80::a00:27ff:feef:f5ad/64 scope link
       valid_lft forever preferred_lft forever
```

可以看到这台虚拟机的ip地址为 `10.0.2.15` ，再在其他的虚拟机上查看ip信息

```bash
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 08:00:27:5d:23:f8 brd ff:ff:ff:ff:ff:ff
    inet 10.0.2.5/24 brd 10.0.2.255 scope global noprefixroute dynamic eth0
       valid_lft 443sec preferred_lft 443sec
    inet6 fe80::a00:27ff:fe5d:23f8/64 scope link
       valid_lft forever preferred_lft forever
3: eth1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 08:00:27:9f:7f:1d brd ff:ff:ff:ff:ff:ff
    inet 192.168.56.101/24 brd 192.168.56.255 scope global noprefixroute eth1
       valid_lft forever preferred_lft forever
    inet6 fe80::a00:27ff:fe9f:7f1d/64 scope link
       valid_lft forever preferred_lft forever

```

可以看到ip地址为  10.0.2.5



```bash
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host
       valid_lft forever preferred_lft forever
2: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 08:00:27:11:11:61 brd ff:ff:ff:ff:ff:ff
    inet 10.0.2.4/24 brd 10.0.2.255 scope global noprefixroute dynamic eth0
       valid_lft 434sec preferred_lft 434sec
    inet6 fe80::a00:27ff:fe11:1161/64 scope link
       valid_lft forever preferred_lft forever
3: eth1: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 08:00:27:0c:08:5d brd ff:ff:ff:ff:ff:ff
    inet 192.168.56.102/24 brd 192.168.56.255 scope global noprefixroute eth1
       valid_lft forever preferred_lft forever
    inet6 fe80::a00:27ff:fe0c:85d/64 scope link
       valid_lft forever preferred_lft forever
```

ip地址为 10.0.2.4



通过shell工具的多重执行功能，在各个虚拟机上执行 

```bash
ping 10.0.2.15
ping 10.0.2.4
ping 10.0.2.5
```

测试各个虚拟机之间是否能相互ping通。如果配置正确，这时应该是可以相互ping通的。然后再测试是否可以连接外网，这里通过ping百度的服务器作为测试：

```bash
ping baidu.com
```

所有虚拟机正常情况下应该都可以ping通。



### 7 配置主机名

执行命令可以查看主机名：

```
hostname
```

主机名一定不能是  localhost，而且各个虚拟机必须不能重复，如果需要修改可以通过命令：

```bash
hostnamectl set-hostname <new-hostname>
```

指定新的主机名

通过 `hostname`指令和`ip addr`指令查看各个虚拟机的ip和hostname，准备好文件记录ip和主机名的关系，如：

```bash
echo "10.0.2.15 k8s-node1" >> /etc/hosts
echo "10.0.2.4 k8s-node2" >> /etc/hosts
echo "10.0.2.5 k8s-node3" >> /etc/hosts
```

查看各个虚拟机的host文件

```bash
cat /etc/hosts
```



### 5 安装wget

```bash
yum install -y wget
```



### 5 配置yum源

1 进入yum目录

```bash
cd /etc/yum.repos.d/
```

2 把默认yum源备份(可选项)

```bash
cp CentOS-Base.repo CentOS-Base.repo-cp
```

3 下载ailiyun的yum源配置文件到/etc/yum.repos.d/

```bash
wget -O /etc/yum.repos.d/CentOS-Base.repo https://mirrors.aliyun.com/repo/Centos-7.repo
```

4 清除缓存

清除全部缓存

```bash
yum clean all
```

把yum源缓存到本地，加快软件的搜索好安装速度

```
yum makecache
```

更新yum库存

```
sudo yum -y update
```

列举包列表

```
yum repolist
```



### 5 安装常用的工具

```bash
yum install -y wget jq psmisc vim net-tools nfs-utils telnet yum-utils device-mapper-persistent-data lvm2 git network-scripts tar curl
```



### 4 关闭防火墙

开发模式下直接关闭防火墙，这样可以不用单独设置防火墙进出规则。执行：

停止当前运行的防火墙

```bash
systemctl stop firewalld
```

禁用开机启动

```bash
systemctl disable firewalld
```



### 5 关闭安全策略

修改`/etc/selinux/config` 将安全策略配置有默认的 `enforcing` 启用状态修改为  `disabled` 禁用状态

执行 

```bash
sed -i 's/enforcing/disabled/' /etc/selinux/config
```

查看一下时候修改成功

```bash
cat /etc/selinux/config
```

再执行

```bash
setenforce 0
```

在当前会话中禁用selinux，可以通过一下指令查看是否已经关闭：

```
getenforce
```



### 6 时间同步

```bash
yum install -y chrony
```

```bash
systemctl start chronyd
```

```bash
systemctl enable chronyd
```

```bash
chronyc sources
```

```bash
date
```



### 6 关闭swap

执行命令：

```bash
swapoff -a
```

临时关闭swap功能。永久关闭swap需要修改 /etc/fstab文件，执行命令：

```bash
sed -ri 's/.*swap.*/#&/' /etc/fstab
```

通过 

```bash
free -g
```

可以验证swap是否已关闭，swap为0则为已关闭



### 7 ssh互信

```bash
yum install -y sshpass
```



```bash
ssh-keygen -f /root/.ssh/id_rsa -P ''
export IP="k8s-node1 k8s-node2 k8s-node3"
export SSHPASS=vagrant
for HOST in $IP;do
sshpass -e ssh-copy-id -o StrictHostKeyChecking=no $HOST
done
```



### 8 流量监控

将桥接的 IPv4 流量传递到 iptables 的链，如果不执行这个指令会导致流量统计指标消失。为了精确统计流量指标，需要执行下列命令：

```
cat <<EOF | sudo tee /etc/modules-load.d/k8s.conf
overlay
br_netfilter
EOF

sudo modprobe overlay
sudo modprobe br_netfilter

# 设置所需的 sysctl 参数，参数在重新启动后保持不变
cat <<EOF | sudo tee /etc/sysctl.d/k8s.conf
net.bridge.bridge-nf-call-iptables = 1
net.bridge.bridge-nf-call-ip6tables = 1
net.ipv4.ip_forward = 1
EOF


# 应用 sysctl 参数而不重新启动
sudo sysctl --system
```



检查是否生效，通过运行以下指令确认 br_netfilter 和 overlay 模块被加载 通过运行以下指令确认 net.bridge.bridge-nf-call-iptables 、 net.bridge.bridge-nf-callip6tables 和 net.ipv4.ip_forward 系统变量在你的 sysctl 配置中被设置为 1

```bash
lsmod | grep br_netfilter
lsmod | grep overlay
sysctl net.bridge.bridge-nf-call-iptables net.bridge.bridge-nf-call-ip6tables net.ipv4.ip_forward
```



### 9 配置docker源

```bash
yum -y install yum-utils
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
cat /etc/yum.repos.d/docker-ce.repo
```

### 9 配置k8s源

```bash
cat > /etc/yum.repos.d/kubernetes.repo << EOF
[k8s]
name=k8s
enabled=1
gpgcheck=0
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64/
EOF


cat /etc/yum.repos.d/kubernetes.repo
```



### 9 放开文件数量限制

```
ulimit -SHn 65535
cp /etc/security/limits.conf /etc/security/limits.conf.bak
cat >> /etc/security/limits.conf <<EOF
* soft nofile 655360
* hard nofile 131072
* soft nproc 655350
* hard nproc 655350
* seft memlock unlimited
* hard memlock unlimitedd
EOF


diff /etc/security/limits.conf /etc/security/limits.conf.bak
```



### 9 确认mac地址和uuid唯一

```
cat /sys/class/dmi/id/product_uuid
ifconfig
```



### 9 备份

这样虚拟机的前期准备就完成了。完成以后可以将3台主机进行备份。

打开virtualbox，在虚拟机列表中，点击虚拟机右侧菜单，点击 snapshot，点击右侧页面的take

输入snapshot Name 和描述信息 ，如：init，这里根据情况设置。

后续如果有需要，可以在虚拟机关机的情况下选择备份进行恢复。



