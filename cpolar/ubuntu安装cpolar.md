## Linux安装Cpolar内网穿透

https://www.cpolar.com/blog/linux-system-installation-cpolar?channel=0&invite=4W3F



自动安装方式：一键自动安装脚本

手动安装方式：在[官网下载](https://www.cpolar.com/download)下载适用于Linux平台的zip压缩包，解压后得到cpolar，然后通过命令行带参数运行即可。

### 一键自动安装脚本

#### 环境需求：

该脚本适用于Ubuntu16.04/18.04/20.04及以后，Centos7/8及以后版本，树莓派最新官方镜像，及支持systemd的新式Linux操作系统，该脚本会自动判断CPU架构（i386/amd64/mips/arm/arm64等等），自动下载对应cpolar客户端，并自动部署安装。

#### 1. cpolar 安装（国内使用）

```shell
curl -L https://www.cpolar.com/static/downloads/install-release-cpolar.sh | sudo bash
```

或 cpolar短链接安装方式：(国外使用）

```shell
curl -sL https://git.io/cpolar | sudo bash
```



#### 2. 查看版本号，有正常显示版本号即为安装成功

```shell
cpolar version
```



#### 3. token认证

登录cpolar官网[后台](https://dashboard.cpolar.com/get-started)，点击左侧的`验证`，查看自己的认证token，之后将token贴在命令行里

```shell
cpolar authtoken xxxxxxx
```



![20230111103532](D:\myNote\resources\-Synology20230111103532.png)

```bash
leixing@ubuntu:/etc/nginx$ cpolar authtoken Zjc2OTIyN**********************************
Authtoken saved to configuration file: /usr/local/etc/cpolar/cpolar.yml
```

#### 4. 简单穿透测试

```shell
cpolar http 8080
```

按ctrl+c退出



#### 5. 向系统添加服务

```shell
sudo systemctl enable cpolar
```



#### 6. 启动cpolar服务

```shell
sudo systemctl start cpolar
```



#### 7. 查看服务状态

```shell
sudo systemctl status cpolar
```



![20221222164000](D:\myNote\resources\-Synology20221222164000.png)



#### 8. 登录后台，查看隧道在线状态

https://dashboard.cpolar.com/status



#### 9. 安装完成

可以参考系列文章进一步使用cpolar——[`linux系列教程文章`](https://www.cpolar.com/blog/build-a-website-on-ubuntu-system)

#### 注: cpolar 卸载方法

```shell
curl -L https://www.cpolar.com/static/downloads/install-release-cpolar.sh | sudo bash -s -- --remove
```



### 安装说明：

- cpolar默认安装路径 /usr/local/bin/cpolar,
- 安装脚本会自动配置systemd服务脚本，启动以后，可以开机自启动。
- 如果第一次安装，会默认配置一个简单的样例配置文件，创建了两个样例隧道，一个web，一个ssh
- cpolar配置文件路径: /usr/local/etc/cpolar/cpolar.yml



```bash
client_dashboard_addr: 192.168.0.105:9890
authtoken: Zjc2O********************************************
tunnels:
  xxx-service:
    id: a938e3ab-3705-4387-add7-8db52cf4deff
    proto: http
    addr: "9528"
    inspect: "false"
    bind_tls: both
    region: cn_top
    disable_keep_alives: "false"
    redirect_https: "false"
    start_type: enable
email: leixing1012@qq.com
```

注意 这里配置 Cpolar-WebUI路径时，如果设置为 client_dashboard_addr: 127.0.0.1:9890 则只能本机访问，局域网内无法访问。



### 重启cpolar

执行命令

```bash
sudo systemctl restart cpolar
```

