# 实战：win10安装docker并用docker-compose构建运行容器

前言
Docker 并非是一个通用的容器工具，它依赖于已存在并运行的 Linux 内核环境。但是某些时候我们没有Linux环境怎么破？为了解决这个问题我们可以用VM虚拟机上安装Linux系统进行处理。然而对于我们的WIN10系统自带Hyper-V虚拟机，简直不要太爽。我们可以直接开启Hyper-V服务，并安装docker desktop即可。

Docker Desktop
docker desktop是基于windos的Hyper-V服务和WSL2内核在windos上创建一个子系统(linux),从而实现其在windows上运行docker。
Docker Desktop 官方下载地址： https://docs.docker.com/desktop/install/windows-install/

Hyper-V
Hyper-V 是微软开发的虚拟机，类似于 VMWare 或 VirtualBox，仅适用于 Windows 10。这是 Docker Desktop for Windows 所使用的虚拟机。
值得注意的是这个虚拟机一旦启用，QEMU、VirtualBox 或 VMWare Workstation 15 及以下版本将无法使用。

开启 Hyper-V
同时按下键盘上的【Win+R】
输入命令:appwiz.cpl

![在这里插入图片描述](assets/010_/d2beb433c50b44b58424de051107cf78.png)

打开程序和功能
启用或关闭Windows功能
选中Hyper-V

![在这里插入图片描述](assets/010_/53973ae371e449cea9de494861eb2547.png)


也可以通过命令来启用 Hyper-V ，请右键开始菜单并以管理员身份运行 PowerShell，执行以下命令：
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All

安装 Docker Desktop for Windows
下载docker desktop
进入官网 https://docs.docker.com/desktop/install/windows-install/，并下载 Windows 的版本。

![在这里插入图片描述](assets/010_/71e1ab2b7d32444981a73edba520b785.png)

Docker安装目录软连接
在其他盘创建\Program Files\Docker目录
这里要保证C盘下要链接的Docker文件不存在，D盘下Docker文件夹则已经存在
管理员权限运行CMD:
mklink /j “C:\Program Files\Docker” “D:\Program Files\Docker”

![在这里插入图片描述](assets/010_/6678a8ed108f4ceeb0aff49b2e3d9c84.png)

运行Docker Desktop安装文件
双击下载的 Docker Desktop Installer.exe 安装文件

![在这里插入图片描述](assets/010_/76b0470547b64e3a82eb1100192d0951.png)

安装完成后选择关闭并重启电脑，注意这里会直接重启电脑！！！！

![在这里插入图片描述](assets/010_/826bace3aa554925ba11c9fba25244e4.png)


安装完成后，Docker 会自动启动，应用列表会出现个小鲸鱼的图标，服务默认自动。

![在这里插入图片描述](assets/010_/2132e257f6ae4be69984f9b35c237f0a.png)

![在这里插入图片描述](assets/010_/501dc9af21bb4ecba50aaae18a1d7503.png)

Docker Desktop验证
我们可以在命令行执行
docker --version #查看docker版本
docker-compose --version #查看docker-compose版本

![在这里插入图片描述](assets/010_/da0fda06687c404cb35c204ee1b00698.png)


docker-compose构建运行管理容器
创建文件目录及配置文件
在D盘新增app文件夹，创建dockerfile、docker-compose.yml、logs

dockerfile

this is test_demo dockerfile

version 1.0

基础镜像

FROM openjdk:8-jre

维护人

MAINTAINER senfel<187@sina.cn>

拷贝项目jar

COPY test-demo-0.0.1-SNAPSHOT.jar /home/app/app.jar

执行命令启动jar

ENTRYPOINT ["java","-jar","/home/app/app.jar"]

暴露端口

EXPOSE 9999

docker-compose.yml
注意：挂载目录需要在docker桌面控制台设置增加resource目录

version: '3.3'  #docker-compose版本
services: #服务列表
  demo: #服务名
    container_name: demo #容器名称
    build: #启动服务时，先将build中指定的dockerfile打包成镜像，再运行该镜像
      context: ./ #指定上下文目录dockerfile所在目录[相对、绝对路径都可以]
      dockerfile: Dockerfile #文件名称[在指定的context的目录下指定那个Dockerfile文件名称]
    ports: #端口映射

      - 8888:9999
        volumes: #目录挂载
            - D:/app/logs:/opt/logs
            restart: always #自动重启
            environment: #环境变量
                  TZ: Asia/Shanghai    #时区

全部目录和文件如下：

![在这里插入图片描述](assets/010_/71a90a3d5307493b9e66839cc1526390.png)

构建并启动容器
docker-compose up -d

![在这里插入图片描述](assets/010_/1fa0ed2416ca4abc831a4d336e71ef57.png)

修改配置文件后可以强制重新构建和启动容器
docker-compose up --force-recreate -d

![在这里插入图片描述](assets/010_/356019819e4c4279b9f4e8435d08ceb3.png)

查看启动的容器并验证
docker ps

![在这里插入图片描述](assets/010_/e5fc34890b6b4f1192b4c8fde6c5c15b.png)

查看我们挂载的日志文件

![在这里插入图片描述](assets/010_/6854e482250441d194df1064d2211e89.png)

写在最后
win10安装docker并用docker-compose构建运行容器较为简单，只需要在程序和功能中开启Hyper-V虚拟功能，然后安装docker desktop即可。