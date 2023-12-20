## vagrant入门



### 1.下载安装 vagrant

https://developer.hashicorp.com/vagrant/downloads

根据系统下载并安装最新版本



### 2. 下载安装virtualbox

https://www.virtualbox.org/wiki/Downloads

根据自身系统下载并安装最新版本



### 3. 安装操作系统

以centos7为例：

#### 3.1 初始化

首先在指定目录下（会在该目录下生成文件，最好单独新建文件夹）运行指令：

```bash
vagrant init centos/7
```

执行完上面的命令后，会在用户的家目录下生成Vagrantfile文件

其中系统名称来自于vagrant提供的镜像，镜像网站：[vagrant-box-search](https://app.vagrantup.com/boxes/search) ，列表的标题部分包含镜像名称等其他信息。



#### 3.2下载并启动镜像

**注意：**最好先在virtualbox软件设置中设置虚拟机保存的路径，否则会默认保存在user目录下。

```bash
vagrant up
```

如果下载很慢也可以预先下载号镜像文件再加载：

```bash
vagrant box add centos/7 D:\dolwnload\CentOS-7-x86_64-Vagrant-1905_01.VirtualBox.box
```

再运行

```bash
vagrant up
```



#### 3.3 进入系统

启动系统后通过ssh进入系统

```bash
vagrant ssh
```

注意：系统的默认用户名密码为 vagrant/vagrant



#### 3.4 配置网络环境

##### 3.4.1 查看本机网络配置

找到virtualbox的虚拟网卡ip

```bash
ipconfig

Windows IP 配置

以太网适配器 VirtualBox Host-Only Network:

   连接特定的 DNS 后缀 . . . . . . . :
   本地链接 IPv6 地址. . . . . . . . : fe80::a00c:1ffa:a39a:c8c2%16
   IPv4 地址 . . . . . . . . . . . . : 192.168.56.1
   子网掩码  . . . . . . . . . . . . : 255.255.255.0
   默认网关. . . . . . . . . . . . . :
```

例如这里网卡地址为: 192.168.56.1 ，则后续需要将虚拟系统的ip设置为该网段的地址，例如 192.168.56.10



##### 3.4.2 修改vagrant配置

修改文件 Vagrantfile，解除私有网络配置，并修改如下：

```bash
config.vm.network "private_network", ip: "192.168.56.10"
```

加载配置：

```bash
vagrant reload
```



##### 3.4.3 检查网络环境

进入虚拟系统ping本机

```bash
vagrant ssh
```

进入后：

```
ping 192.168.0.108
```

本机ping虚拟系统

```bash
ping 192.168.56.10
```

相互能ping通则说明网络环境配置正确



### 3.5 配置远程登录

修改配置文件：

```
sudo vi /etc/ssh/sshd_config
```

i进入编辑模式， esc退出编辑模式，:wq 保存修改并退出， root密码为vagrant

```bash
PermitRootLogin yes 
PasswordAuthentication yes
```

将上述两行的注释打开， 注意吧 PasswordAuthentication no注释或者删除

重启ssh

```
systemctl restart sshd
```

再次在本机中使用ssh连入虚拟机可以使用账号密码 vagrant/vagrant 连入

