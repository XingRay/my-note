## ubuntu安装nodejs

在nodejs官方网站download页面中选择需要使用的版本,可以选择LTS或current,选择对应系统右键复制链接地址


如果需要下载历史版本可以选择download页面底部PreviousReleases,也可以直接访问http://nodejs.org/dist/或https://nodejs.org/download/release/

当然你也可以使用淘宝npm镜像下载对应的node版本下载地址
下载nodejs压缩文件

```bash
wget https://nodejs.org/dist/v8.1.0/node-v8.1.0-linux-x64.tar.xz
```

解压

```bash
tar -xvf node-v8.1.0-linux-x64.tar.xz
```


切换并查看当前node所在路径

```bash
cd node-v8.1.0-linux-x64/bin
pwd
```

查看node版本

```bash
./node -v
```


将node和npm设置为全局

```bash
sudo ln /home/ubuntu/node-v8.1.0-linux-x64/bin/node /usr/local/bin/node
sudo ln /home/ubuntu/node-v8.1.0-linux-x64/bin/npm /usr/local/bin/npm
```


也可以使用ubuntu自带apt-get安装,安装后使用node -v查看版本

```bash
sudo apt-get install nodejs-legacy nodejs
sudo apt-get install npm
```

安装完成
推荐使用方法一，直接安装在系统环境/usr/bin目录下，之后使用npm -g安装其他插件也会安装到/usr/lib/node_modules’(需要使用sudo权限)‘。
如果使用方法二，将nodejs路径链接到/usr/local/bin目录下，则每次npm -g安装插件都会安装在nodejs原路径下的node_modules(比如/home/ubuntu/node-v8.1.0-linux-x64/lib/node_modules)，每次代码中引用插件也需要到此目录下去找



此时可以在ui项目中使用指令

安装依赖

```bash
npm install
```

指定镜像源

```bash
npm install --registry=https://registry.npm.taobao.org
```

指定安装生产环境依赖

```bash
npm install --registry=https://registry.npm.taobao.org --production
```



运行

开发环境

```bash
npm run dev
```



打包

```bash
npm run build
```

