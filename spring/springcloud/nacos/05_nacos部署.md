ubuntu 22 Docker部署Nacos

首先docker拉取镜像

sudo docker pull nacos/nacos-server

查看一下images列表

sudo docker images

启动nacos镜像容器，服务器上创建日志映射文件件/nacos/logs

sudo docker run -d -e prefer_host_mode=服务器ip地址  -e MODE=standalone  -v  /nacos/logs:/home/nacos/logs  -p 8848:8848  --name nacosdemo --restart=always   nacos/nacos-server

启动后，在服务器上查看日志，上一步已经映射出来了

tail -f /nacos.logs/nacos.log

访问nacos控制台，http://服务器ip:8848/nacos。默认用户名密码：nacos/nacos

原文链接：https://blog.csdn.net/airyearth/article/details/128040958