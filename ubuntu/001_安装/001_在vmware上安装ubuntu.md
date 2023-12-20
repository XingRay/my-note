## 在VMWare上安装Ubuntu

前置条件

VMWare

Ubuntu-server.iso



网络：桥接模式

硬盘类型：nvme



进入虚拟机开始安装系统：



try or install ubuntu



select your language:

english



continue without updating

Done



installation:

Ubuntu Server

Done



Network

dhcp 自动分配

Done



Proxy address：

Done



Mirror address:

```bash
https://mirrors.aliyun.com/ubuntu/
```

注意要使用https



storage configuartion

Custom storage layout

Done

storage configuration
	available devices
点击这里： free space

点击	ADD GPT Partition
			

​	size:
​				format ext4
​				mount /

点击  create

点击 Done

点击 Continue







配置系统信息：

```bash
your name:	ubuntu-server
your server's name: ubuntu-server
pick a username: ubuntu-user
choose a password: 123456
confirm your password: 123456
```



About ubuntu Pro

skip for now

Continue



[ ] install openssh server 

Done



Featured Server Snaps

Done



开始安装，等待安装结束



安装完成后再对系统进行设置



使用安装的账号登录系统



设置root密码

```bash
sudo passwd root
```



设置ssh

```bash
sudo apt install -y openssh-server
```

修改允许远程登录：

```bash
sudo vi /etc/ssh/sshd_config
```

修改下面的配置行：

```bash
PermitRootLogin prohibit-password
```

修改为：

```bash
PermitRootLogin yes
```

重启ssh服务

```bash
sudo systemctl restart ssh
```



通过 ssh工具连接ubuntu

更新系统

```bash
sudo apt update
```

```bash
sudo apt upgrade -y
```

