## ubuntu安装和卸载DEB包

### 1 下载安装包

下载对应版本的deb文件，上传到ubuntu服务器



### 2 安装DEB

```bash
sudo dpkg -i xxx.deb
```



### 3 删除软件

安装后需要去除一个软件，通过deb包安装的。需要2个步骤，一个是删除软件，一个是去除deb包

通过

```bash
dpkg --list | grep xxx
```

命令查看你要卸载的软件

root@80-ubuntu:/etc/zabbix# dpkg --list | grep agent   ##我这里用了个过滤，不然list出来太多了

```bash
ii  gpg-agent                             2.2.27-3ubuntu2.1                       amd64        GNU privacy guard - cryptographic agent
ii  libpolkit-agent-1-0:amd64             0.105-33                                amd64        PolicyKit Authentication Agent API
ii  lxd-agent-loader                      0.5                                     all          LXD - VM agent loader
ii  zabbix-agent                          1:5.0.26-1+ubuntu22.04                  amd64        Zabbix network monitoring solution - agent
```

通过remove删除软件，--purge是删除对应的配置文件

```bash
sudo apt-get --purge remove zabbix-agent
```

卸载成功。



 但是我们是用deb安装的，相当于rpm包，不去掉这个包的话下载安装就还是安装了你要卸载的这个版本包。这是不能允许的，那就也去掉这个deb包。

删除deb包
通过dpkg -l 来删除deb来，同样我也使用了一个过滤，更精确，

```
root@80-ubuntu:/etc# dpkg -l | grep agent
ii  gpg-agent                             2.2.27-3ubuntu2.1                       amd64        GNU privacy guard - cryptographic agent
ii  libpolkit-agent-1-0:amd64             0.105-33                                amd64        PolicyKit Authentication Agent API
ii  lxd-agent-loader                      0.5                                     all          LXD - VM agent loader
root@80-ubuntu:/etc# dpkg -l | grep zabbix
ii  zabbix-release                        1:5.0-2+ubuntu22.04                     all          Zabbix official repository configuration
root@80-ubuntu:/etc# dpkg -r zabbix-release
(Reading database ... 77624 files and directories currently installed.)
Removing zabbix-release (1:5.0-2+ubuntu22.04) ...
root@80-ubuntu:/etc#
```

​    
