## Ipv6实现公网访问



路由器设置：

路由器管理界面→路由设置→IPv6设置”中，将IPv6功能开启，WAN口连接类型选择“桥模式”，点击“保存”

开启后访问测试：

http://ipv6.baidu.com/



设置完成后启动一个springboot项目测试即可，注意在公网访问不能使用80和8080端口，如这里在8888端口开启一个访问端点：

```bash
http://[2408:821a:9918:2eb0:824a:a0af:eb41:76c3]:8888/hello/hello
```

注意格式是 `http://[ipv6-addr]:port/...`

像这样的ipv6地址  fe80::2cf9:dc36:ce73:7c4c%35 要去除掉 %35 后缀的，并且只能内网访问。

IPv6 Address. . . . . . . . . . . : 2408:821a:9918:2eb0:824a:a0af:eb41:76c3

这个是全球可以访问的公网地址。



```bash
C:\Users\leixing\Desktop>ipconfig

Windows IP Configuration


Ethernet adapter 以太网 2:

   Connection-specific DNS Suffix  . :
   Link-local IPv6 Address . . . . . : fe80::92c6:b6cb:3f6b:ad51%8
   IPv4 Address. . . . . . . . . . . : 192.168.56.1
   Subnet Mask . . . . . . . . . . . : 255.255.255.0
   Default Gateway . . . . . . . . . :

Ethernet adapter 以太网:

   Connection-specific DNS Suffix  . :
   IPv6 Address. . . . . . . . . . . : 2408:821a:9918:2eb0:824a:a0af:eb41:76c3
   Temporary IPv6 Address. . . . . . : 2408:821a:9918:2eb0:612e:7852:640a:72f1
   Link-local IPv6 Address . . . . . : fe80::5c75:ad65:ceca:8bc6%15
   IPv4 Address. . . . . . . . . . . : 192.168.0.108
   Subnet Mask . . . . . . . . . . . : 255.255.255.0
   Default Gateway . . . . . . . . . : fe80::1%15
                                       192.168.0.1

Ethernet adapter vEthernet (以太网 2):

   Connection-specific DNS Suffix  . :
   Link-local IPv6 Address . . . . . : fe80::2cf9:dc36:ce73:7c4c%35
   IPv4 Address. . . . . . . . . . . : 192.168.160.1
   Subnet Mask . . . . . . . . . . . : 255.255.240.0
   Default Gateway . . . . . . . . . :

Ethernet adapter vEthernet (Default Switch):

   Connection-specific DNS Suffix  . :
   Link-local IPv6 Address . . . . . : fe80::239c:b320:7f2a:3a92%41
   IPv4 Address. . . . . . . . . . . : 172.30.192.1
   Subnet Mask . . . . . . . . . . . : 255.255.240.0
   Default Gateway . . . . . . . . . :

Ethernet adapter vEthernet (以太网):

   Connection-specific DNS Suffix  . :
   Link-local IPv6 Address . . . . . : fe80::f0eb:9e8e:5e58:4a37%46
   IPv4 Address. . . . . . . . . . . : 172.26.160.1
   Subnet Mask . . . . . . . . . . . : 255.255.240.0
   Default Gateway . . . . . . . . . :
```





TPLink官方完整说明：



目前国内的网络正在快速的向IPv6升级中，从网络基础设施如运营商骨干网、城域网，到互联网服务商如各类云服务，以及各类终端设备厂商如手机、电脑、路由器、交换机等。目前运营商提供的IPv6线路主要为不支持前缀授权，本文主要介绍家用路由器关于IPv6的上网设置方法。

**确认宽带线路是否支持****IPv6**

确认宽带支持IPv6最直接的方法是：电脑直接连接宽带，可以获取到IPv6全球地址，则说明宽带支持IPv6，如下图：

![img](https://service.tp-link.com.cn/pages/imageuploadfolder/202104/20210416163835_3793.png)

注意：若电脑直连光猫无法获取到2开头的IPv6公网地址，则说明线路不支持IPv6，需要联系ISP确认和更改光猫设置。

**设置方法**

确认线路支持IPv6后，根据路由器IPv4的上网方式，选择合适的IPv6上网方式。

**1.** **路由器****IPv4****上网方式为“宽带拨号上网”，****IPv6****选“宽带拨号上网”**

若路由器的上网方式为IPv4宽带拨号，且拨号成功后能正常上网，路由器的上网设置界面，如下图：

![img](https://service.tp-link.com.cn/pages/imageuploadfolder/202104/20210416163855_3249.png)

在“路由器管理界面→路由设置→IPv6设置”中，将IPv6功能开启，WAN口连接类型选择宽带拨号上网，并勾选“复用IPv4拨号链路”，然后点击“连接”，如下图：

![img](https://service.tp-link.com.cn/pages/imageuploadfolder/202104/20210416163921_6539.png)

**2.** **路由器****IPv4****上网方式为“自动获得****IP****地址”，****IPv6****选“桥模式”**

若路由器的上网方式为自动获得IP地址，且获取IP地址后能正常上网，路由器的上网设置界面，如下图：

![img](https://service.tp-link.com.cn/pages/imageuploadfolder/202104/20210416163952_2281.png)

则在“路由器管理界面→路由设置→IPv6设置”中，将IPv6功能开启，WAN口连接类型选择“桥模式”，点击“保存”，如下图：

![img](https://service.tp-link.com.cn/pages/imageuploadfolder/202104/20210416164008_6073.png)

**测试电脑获取的****IPv6****地址是否可以正常联网**

设置完路由器的IPv6功能后，电脑重新连接路由器网络，获取到公网IPv6地址后，打开浏览器输入[www.test-ipv6.com](http://www.test-ipv6.com/)，就可以看到线路是否支持IPv6了。







# 使用IPV6外网访问的配置方法

## 查看本机ipv6

在cmd中使用**ipconfig**命令即可查看ip，如图：
![img](https://img-blog.csdnimg.cn/img_convert/1a12386299c93c3e37bffccd5a40de33.png)

## 安装nginx服务

在这里找到对应自己机器的文件，Windows下载.zip文件
解压在不含中文和空格的目录下，**切记：不可直接点击nginx.exe**进行运行，要在cmd中使用***start nginx***进行运行，进入nginx目录下的conf文件夹下，编辑nginx.conf文件，在这里加上一行语句

```
listen [::]:8088 ipv6only=on;//这里的8088是端口号，运营商会封80、8080等端口，所以需要设置其他端口号
```

![img](https://img-blog.csdnimg.cn/img_convert/0f001b2b4523dd33cc7c68099df2bac4.png)
接着使用命令：

```
nginx -s reload
```

来重启nginx服务，这时在外网通过访问**http://[ipv6]:8088**即可访问到nginx默认页面，如图：
![img](https://img-blog.csdnimg.cn/img_convert/b17e7fd043cee80cbe78be97efd980c0.png)


 
 
emm，大概就是这样，虽然只是部署一个nginx没什么用，但是理解了ipv6的使用方法之后，等回了学校就可以在树莓派上或者jetson nano上连接一块硬盘，做一个小型服务器，并且可以尝试做一个私人云。