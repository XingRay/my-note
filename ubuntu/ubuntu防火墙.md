## Ubuntu 防火墙

查看ufw版本

```bash
sudo ufw version
```

查看防火墙状态，inactive是关闭状态，active是开启状态

```bash
sudo ufw status
```

开启防火墙

```bash
sudo ufw enable
```

关闭防火墙

```bash
sudo ufw disable
```



允许外部访问80端口，协议包含tcp和udp

```bash
sudo ufw allow 80
```



禁止外部访问80 端口

```bash
sudo ufw delete allow 80
```



允许其它主机使用tcp协议访问本机80端口

```bash
sudo ufw allow 80/tcp
```



允许此IP访问所有的本机端口

```bash
sudo ufw allow from 192.168.1.1
```



sudo ufw default deny：启动默认防御，作用：关闭所有外部对本机的访问(本机访问外部正常)

sudo ufw allow|deny [service]：禁用防火墙

sudo ufw deny smtp：禁止外部访问smtp服务

sudo ufw reject out smtp：禁止访问外部smtp端口，不告知“被防火墙阻止”

sudo ufw deny out to 192.168.1.1：禁止本机192.168.1.1对外访问，告知“被防火墙阻止”

sudo ufw delete allow smtp：删除上面建立的某条规则

sudo ufw delete deny 80/tcp：要删除规则，只要在命令中加入delete就行了

sudo ufw deny proto tcp from 10.0.0.0/8 to 192.168.0.1 port 22：要拒绝所有的TCP流量从10.0.0.0/8 到192.168.0.1地址的22端口

可以允许所有RFC1918网络（局域网/无线局域网的）访问这个主机（/8,/16,/12是一种网络分级）：

sudo ufw allow from 10.0.0.0/8

sudo ufw allow from 172.16.0.0/12

sudo ufw allow from 192.168.0.0/16



***\*实例：\****

1.设置允许访问 SSH
sudo ufw allow 22/tcp

2.设置允许访问 http
sudo ufw allow 80/tcp

3.设置允许访问 https
sudo ufw allow 443/tcp

4.设置允许访问 pptp
sudo ufw allow 1723/tcp









在服务器中安装防火墙软件就相当于给所有的端口配了一把锁，任何程序要开端口监听或对外连接，都需要通过防火墙的规则去允许他。在[Ubuntu](https://so.csdn.net/so/search?q=Ubuntu&spm=1001.2101.3001.7020) Server服务器中可以使用UFW防火墙软件。

可以使用如下命令查看防火墙的安装状态：

sudo ufw [status](https://so.csdn.net/so/search?q=status&spm=1001.2101.3001.7020)(如果你是root，则去掉sudo，ufw status)

如果返回的是inactive，则说明防火墙没有被启用。Ubuntu系统默认是安装UFW防火墙的，如果没有安装可以使用如下命令安装：

sudo apt-get install ufw

还可以使用如下命令查看防火墙的版本：

sudo ufw version

1 防火墙的启用

防火墙安装完成后可以使用如下命令开启防火墙：

sudo ufw enable

sudo ufw default deny

第一条命令开启防火墙，第二条命令设置在系统启动时自动开启防火墙。关闭防火墙的命令为：

sudo ufw disable

可以使用如下命令开启或关闭防火墙的日志：

ufw logging on|off

2 端口设置

防火墙开启后默认关闭所有外部对本机的访问，但本机访问外部正常。如果要允许外部访问服务器需要开启相应的端口。UFW开启或关闭端口的命令为：

sudo ufw allow|deny [service]

例如：

sudo ufw allow smtp　 #允许所有的外部IP访问本机的25/tcp (smtp)端口

sudo ufw allow 22/tcp #允许所有的外部IP访问本机的22/tcp (ssh)端口

sudo ufw allow 53 #允许外部访问53端口(tcp/udp)

sudo ufw allow from 192.168.1.100 #允许此IP访问所有的本机端口

ufw allow proto tcp from 10.0.1.0/10 to 本机ip port 25:允许自10.0.1.0/10的tcp封包访问本机的25端口

sudo ufw deny smtp #禁止外部访问smtp服务

sudo ufw delete allow from 192.168.254.254

sudo ufw delete allow smtp #删除上面建立的某条规则

3 UFW相关的文件和文件夹

/etc /ufw/：里面是一些ufw的环境设定文件，如 before.rules、after.rules、sysctl.conf、ufw.conf，及 for ip6 的 before6.rule 及 after6.rules。这些文件一般按照默认的设置进行就ok。

开启ufw之 后，/etc/ufw/sysctl.conf会覆盖默认的/etc/sysctl.conf文件，若你原来的/etc/sysctl.conf做了修改，启动ufw后，若/etc/ufw/sysctl.conf中有新赋值，则会覆盖/etc/sysctl.conf的，否则还以/etc /sysctl.conf为准。当然你可以通过修改/etc/default/ufw中的“IPT_SYSCTL=”条目来设置使用哪个 sysctrl.conf.

/var/lib/ufw/user.rules 这个文件中是我们设置的一些防火墙规则，打开大概就能看明白，有时我们可以直接修改这个文件，不用使用命令来设定。修改后记得ufw reload重启ufw使得新规则生效。





查看是否安装iptables

```bash
whereis iptables
```

删除iptables

```bash
 sudo apt-get remove iptables
```

删除firewalld

```bash
 sudo apt-get remove firewalld
```





# Ubuntu 安装与使用UFW防火墙 | ufw与iptables

![img](https://csdnimg.cn/release/blogv2/dist/pc/img/reprint.png)

[童话破灭i](https://blog.csdn.net/m0_37801862)![img](https://csdnimg.cn/release/blogv2/dist/pc/img/newCurrentTime2.png)于 2022-02-13 18:17:10 发布![img](https://csdnimg.cn/release/blogv2/dist/pc/img/articleReadEyes2.png)1835![img](https://csdnimg.cn/release/blogv2/dist/pc/img/tobarCollect2.png) 收藏 6

分类专栏： [Linux](https://blog.csdn.net/m0_37801862/category_11631576.html) 文章标签： [ubuntu](https://so.csdn.net/so/search/s.do?q=ubuntu&t=all&o=vip&s=&l=&f=&viparticle=) [linux](https://so.csdn.net/so/search/s.do?q=linux&t=all&o=vip&s=&l=&f=&viparticle=) [服务器](https://so.csdn.net/so/search/s.do?q=服务器&t=all&o=vip&s=&l=&f=&viparticle=)

[![img](https://img-blog.csdnimg.cn/20201014180756757.png?x-oss-process=image/resize,m_fixed,h_64,w_64)Linux专栏收录该内容](https://blog.csdn.net/m0_37801862/category_11631576.html)

2 篇文章0 订阅

订阅专栏

​    **iptables** 是一个通过控制 Linux 内核的 [netfilter](https://so.csdn.net/so/search?q=netfilter&spm=1001.2101.3001.7020) 模块来管理网络数据包的流动与转送的应用软件，其功能包括不仅仅包括防火墙的控制出入流量，还有端口转发等等。iptables 内部有表 tables、链 chains、规则 rules 这三种概念。iptables 的每一个 “表” 都和不同的数据包处理有关、决定数据包是否可以穿越的是 “链”、而一条 “规则” 在链里面则可以决定是否送往下一条链（或其它的动作）。

​    **UFW（Uncomplicated Firewal）**是 Ubuntu 下基于 [iptables](https://so.csdn.net/so/search?q=iptables&spm=1001.2101.3001.7020) 的接口，旨在简化配置防火墙的过程。默认情况下 UFW 为关闭状态，开启时默认为拒绝所有传入链接，并允许所有传出连接。这意味着任何人尝试到达您的服务器将无法连接，而服务器内的任何应用程序能够达到外部世界。

**尽量避免ufw与iptables同时使用。**

```bash
sudo ufw enable   # 启用



sudo ufw default deny  # 作用：开启了防火墙并随系统启动同时关闭所有外部对本机的访问（本机访问外部正常）。



sudo ufw disable  # 关闭



sudo ufw status   # 查看防火墙状态



sudo ufw reset  # 重置所有规则



sudo ufw allow 22  #开放22端口



sudo ufw allow 
ssh  #开放ssh端口



sudo ufw delete allow 22  #关闭22端口
```



**安装**

```bash
# 默认已安装
sudo apt-get install ufw
```



**查看服务是否已启动及防火墙规则**

```bash
# 激活：已启动 不激活：已关闭
sudo ufw status

# 查看详细信息
sudo ufw status verbose

# 查看带编号的服务信息 [用于 remove 时的编号参数]
sudo ufw status numbered
```



**开启关闭防火墙**

```bash
# 关闭防火墙
sudo ufw disable

# 启动防火墙
sudo ufw enable
```



**设置默认策略**

```bash
# 默认禁止所有其它主机连接该主机
sudo ufw default deny incoming

# 默认允许该主机所有对外连接请求
sudo ufw default allow outgoing
```



**设置允许连接规则**

```bash
# 允许 ssh 服务（服务名）
sudo ufw allow ssh

# 允许 ssh 服务（端口号）
sudo ufw allow 22

# 允许特定协议的端口访问
sudo ufw allow 21/tcp

# 允许特定端口范围
sudo ufw allow 6000:6007/tcp
sudo ufw allow 6000:6007/udp

# 允许特定IP地址访问
sudo ufw allow from 192.168.1.100

# 允许特定范围主机（15.15.15.1 - 15.15.15.254）
sudo ufw allow from 15.15.15.0/24

# 允许特定范围主机访问特定端口
sudo ufw allow from 15.15.15.0/24 to any port 22

# 允许连接到特定的网卡
sudo ufw allow in on eth0 to any port 80
```



**设置拒绝连接规则**

```bash
# 将 allow 替换为 deny
sudo ufw deny http
sudo ufw deny from 192.168.1.100
```



**删除规则**

```bash
# 查看所有规则并显示规则编号
sudo ufw status numbered

# 按编号删除
sudo ufw delete allow 2

# 按服务删除
sudo ufw delete allow ssh
```



**重置UFW**

```bash
# 恢复至初始状态
sudo ufw reset
```



**修改配置文件**

```bash
# 打开配置文件
sudo vi /etc/default/ufw

# 命令操作同时对IPv4和IPv6都生效
IPV6=yes
```



**配置生效**

```bash
# 重启防火墙
sudo ufw disable
sudo ufw enable
```

