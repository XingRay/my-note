# nodejs安装

## 01 手动安装

在nodejs官方网站download页面 ( https://nodejs.org/zh-cn/download )中选择需要使用的版本,可以选择LTS或current,选择对应系统右键复制链接地址


如果需要下载历史版本可以选择download页面底部PreviousReleases,也可以直接访问 ( http://nodejs.org/dist/ ) 或 ( https://nodejs.org/download/release/ )

当然你也可以使用淘宝npm镜像下载对应的node版本下载地址

### 下载

下载nodejs压缩文件

#### windows

浏览器直接下载 / 下载工具

https://nodejs.org/dist/v18.18.0/node-v18.18.0-win-x64.zip



#### ubuntu

```bash
wget https://nodejs.org/dist/v18.18.0/node-v18.18.0-linux-x64.tar.xz
```



### 解压

#### windows

压缩软件解压即可



#### ubuntu

```bash
tar -xvf node-v18.18.0-linux-x64.tar.xz -C nodejs
```



### 设置系统环境变量

#### windows

```shell
SystemPropertiesAdvanced
```

进入系统环境变量设置, 将nodejs目录路径设置为 NODE_HOME , 将 %NODE_HOME% 加入 Path 中



#### ubuntu

切换并查看当前node所在路径

```bash
cd node-v18.18.0-linux-x64/bin
pwd
```


将node和npm设置为全局

```bash
sudo ln /home/ubuntu/node-v18.18.0-linux-x64/bin/node /usr/local/bin/node
sudo ln /home/ubuntu/node-v18.18.0-linux-x64/bin/npm /usr/local/bin/npm
```


也可以使用ubuntu自带apt-get安装

```bash
sudo apt-get install nodejs-legacy nodejs
sudo apt-get install npm
```

推荐使用方法一，直接安装在系统环境/usr/bin目录下，之后使用npm -g安装其他插件也会安装到/usr/lib/node_modules’(需要使用sudo权限)‘。
如果使用方法二，将nodejs路径链接到/usr/local/bin目录下，则每次npm -g安装插件都会安装在nodejs原路径下的node_modules(比如/home/ubuntu/node-v18.18.0-linux-x64/lib/node_modules)，每次代码中引用插件也需要到此目录下去找



### 查看node版本

```bash
node -v
```



## 02 nvm

windows

nvm-windows https://github.com/coreybutler/nvm-windows



ubuntu

Node Version Manager  ( https://github.com/nvm-sh/nvm ) 



### 命令

#### 配置nvm的镜像服务器

```shell
nvm node_mirror https://npmmirror.com/mirrors/node/
```



#### 显示已安装的node版本

```shell
nvm list
```



#### 安装指定版本的node

```shell
nvm install 版本
```

```shell
nvm install 18
```

示例:

```shell
PS C:\Users\leixing> nvm install 16
Downloading node.js version 16.20.2 (64-bit)...
Extracting node and npm...
Complete
npm v8.19.4 installed successfully.


Installation complete. If you want to use this version, type

nvm use 16.20.2
```



#### 指定要使用的node版本

```shell
nvm use 版本
```

```shell
nvm use 18
```

```shell
nvm use 16.20.2
```

查看node版本

```shell
node -v
```



