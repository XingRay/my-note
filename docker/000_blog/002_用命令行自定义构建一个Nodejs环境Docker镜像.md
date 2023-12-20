## 用命令行自定义构建一个Nodejs环境Docker镜像



注意：请先安装Docker工具

一、下载用于构建Nodejs的基础镜像
1、拉取Python3环境镜像
由于构建Nodejs需要Python3环境，为了方便演示，直接使用Python3镜像

docker pull python:3.10-alpine
1
2、查看镜像
docker images
1
注：alpine版本为linux发行版中体积较小的一种

二、启动Python3镜像并进入命令行
1、启动并进入镜像
docker run -it python:3.10-alpine /bin/sh
1
2、安装基础依赖
apk add gcc g++ make libffi-dev openssl-dev libtool wget
1
三、以源码方式构建Nodejs
1、下载源码
cd /home
wget https://nodejs.org/dist/v14.21.1/node-v14.21.1.tar.gz
1
2
2、解压缩归档文件
tar zxvf node-v14.21.1.tar.gz
1
3、编译Nodejs
cd node-v14.21.1
./configure
make && make install
1
2
3
4、验证版本
node -v
1
四、导出Docker镜像
注：导出过程中请勿退出容器

1、查看对应容器Id
docker ps
1
2、将对应Id的容器导出为镜像
docker commit -a 作者名 -m 镜像描述 容器Id 镜像名:镜像版本号
1
例子
docker commit -a "Changeden" -m "Nodejs v14.21.1版本镜像" 123456789 nodejs-v14.21.1c:latest
1
五、优化镜像大小
移除无用的依赖
移除Python3相关内容