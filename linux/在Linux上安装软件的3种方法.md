## 在 Linux 上安装软件的 3 种方法

### 前言

学习 Linux 必须要学会如何安装和卸载应用程序，这次就来简单介绍下在 Linux 上安装和卸载软件的几种方法。我使用的是 [Ubuntu](https://so.csdn.net/so/search?q=Ubuntu&spm=1001.2101.3001.7020)，不过对于安装应用程序来说，只要是 Linux 都是大同小异的。

### 应用程序安装的原理

我们知道，在 Windows 下双击安装包即可安装软件，非常的简单方便，但是这种方法是针对大部分普通用户的，作为计算机高手，你可不能止步与表面功夫。因此，我们必须了解系统在安装软件时所做的事情。

Windows 安装软件大概的过程如下：

1. 在用户指定的安装目录下建立安装程序目录
2. 拷贝相关 dll 动态库到安装目录
3. 拷贝 exe 可执行文件到安装目录
4. 拷贝配置文件到安装目录，比如 Windows 下的 ini 配置文件
5. 把启动配置或者程序依赖的配置放入注册表中
6. 如果是服务程序，注册并且启动该服务

Linux 安装软件大概的过程如下：

1. 建立安装目录
2. 拷贝类库
3. 拷贝可执行程序
4. 根据需要选择性配置和启动服务

二者的安装过程几乎是相同的，只是安装方式有些不同。在 Windows 下我们经常使用图形界面来安装，而在 Linux 下经常通过命令行来安装，我们后面介绍。对于卸载过程，二者也是相同的，都是安装的过程逆过程。

另外需要注意以下 2 点：

1. Linux 下的 /usr 目录相当于 Windows 下的 ProgramFile 目录
2. Linux 下的动态库后缀是 .so 而 Windows 下是 .dll

Linux 的软件安装主要有 3 种方法，下面一一介绍。

## 3 种软件安装卸载方法

### 1.安装包安装

这种方法其实就是在 Windows 下安装软件的方法，Linux 软件包也类似于 Windows 下的软件包。常见的 Linux 下的安装包有如下两种：

1. rpm: 红帽 Linux 用的安装包格式
2. ded: Debian Linux 用的安装包格式

安装方式也有 2 种：

1. 双击通过软件管理器安装
2. 使用 dpkg 命令安装

第一种方法比较简单，就是 Windows 使用的方法，而第二种需要我们了解下 dpkg 这个工具。dpkg 的作用主要是打包，查询，检索包信息，包括依赖信息，并安装或者卸载软件包。但是 dpkg 现在不是很常用了，所以这里接不详细介绍了，有兴趣的可以去 Google 查询具体的用法。

卸载也比较简单，直接通过软件管理器点击卸载软件即可。

### 2.通过源码安装

这种方法是最原始的方法，我们需要自己一步一步来编译软件的源代码，然后手动安装软件到系统中，听起来很麻烦，但是做起来其实并不复杂，这个过程主要包含下面 4 个操作命令：

```powershell
cd 软件目录
./configure
make
sudo make install
1234
```

注意：比较容易出错的地方是 `./configure` 和 `make`，这两步可能会出错，如果出错的话，你需要到 Google 去查找出错的原因（一般在 **stackoverflow** 上都能找到答案），很多情况下都是因为缺少某些依赖的库，只要你找到并通过 `apt-get` （后面介绍）来安装就可以了，最后一步其实是拷贝文件的过程，如果提示没有权限而出错，那么你需要使用 `root` 权限来执行这个操作 `sudo make install`。

卸载使用下面的命令：

```powershell
cd 软件目录
sudo make uninstall
12
```

### 3.通过 apt-get 在线安装

`apt-get` 其实就是一个在线安装软件的工具，它的主要作用是：**通过本地的 Linux 连接到网络上的 apt 仓库（源）来下载软件并自动安装**。Linux 默认都是自动安装了这个工具的，我们需要学会使用它提供的几个常用命令：

1. sudo apt-get update : 从网上的源更新安装包信息
2. sudo apt-get upgrade : 升级软件包到最新版本
3. sudo apt-get -f install : 修复软件依赖包的关系
4. sudo apt-get install software_name : 安装软件
5. sudo apt-get remove software_name : 卸载软件，但是卸载不完全
6. sudo apt-get remove --purge software : 常用卸载方式，卸载完全

我是一个喜欢探究原理的人，那么 apt-get 的工作原理是什么呢？通过它来安装软件主要需要下面 4 个过程：

1.扫描本地软件包列表（执行 sudo apt-get update 刷新软件包列表）
2.进行软件包依赖关系检查
3.从软件包指定的 apt 源中下载软件包
4.解压软件包，并且完成安装和配置
5.这又引出下面几个问题：

1. apt 镜像站点地址存在哪里？

```powershell
/etc/apt/sources.list
1
```

1. apt 的下载的 deb 包存在哪里？

```powershell
/var/cache/apt/archives
1
```

我是如何知道的呢？使用 `man apt-get` 来查看，定位到 FILES 即可看到这两个路径，还有其他路径，有兴趣可以去了解，这里主要是告诉大家解决问题的思路，**在 Linux 下通过 man 可以解决很多问题，一定要善于使用。**

我们在安装软件一般或多或少都需要下面 4 个步骤：

1. 添加对应软件包的源，一般你在搜索如何安装一个软件的时候，别人已经给出了源地址，如果没有给出，则可能这个软件包不需要指定自己的源地址
2. sudo apt-get update 更新源列表
3. sudo apt-get install software_name 安装软件
4. 安装过程需要你确认是否安装，输入 y 确认即可

介绍一个小工具 **aptitiude**，这个工具可以自动安装软件的依赖包，当你使用 apt-get 安装过程中提示你需要安装某些依赖的时候，你可以使用这个工具来代替 apt-get 使用，即可自动安装依赖的包。

但是，首先你要先安装这个工具：

```powershell
sudo apt-get install aptitiude
1
```

如果你使用 apt-get 安装软件的过程中提示你需要安装依赖，那么你换成下面的命令重新安装即可：

```powershell
sudo aptitiude software_name
1
```

这样当你安装的软件需要安装其他的依赖的软件包的时候，这个工具会帮助你自动安装，就不需要你手动安装了，是不是特别方便。

注意：如果你不能通过 apt-get 安装软件，你很可能需要更新系统的源地址，我使用的是 aliyun，你可以将下面的地址添加到你的 `sudo vim /etc/apt/sources.list` 中：

```powershell
deb http://mirrors.aliyun.com/ubuntu/ yakkety main universe multiverse restricted
deb http://security.ubuntu.com/ubuntu/ yakkety-security multiverse main universe restricted
12
```

然后执行更新：

```powershell
sudo apt-get update
1
```

之后就可以使用 `apt-get` 安装软件了，这是经常使用的安装方法，务必学会！

# 练习：安装 vim

说了那么多，不如来点实际的强，这里我就使用 3 种方法来分别安装 vim 编辑器到系统中。

### 1. 安装包安装 vim

打开这个下载页面，根据你的系统类型来下载对应的 vim 安装包：

```powershell
https://pkgs.org/download/vim
1
```

下载完成后，直接双击安装包或者通过 dpkg 安装，我在 Ubuntu 下直接双击使用软件中心即可安装，要卸载的时候点击卸载按钮即可卸载，非常简单。

### 2. 源代码安装 vim

我们需要从 vim 官网下载源码，使用浏览器打开下面的网址，点击 All files 那一行的下载链接即可下载源代码：

```powershell
http://www.vim.org/sources.php
1
```

下载完成后，打开命令行，进入下载文件的目录，使用下面的命令解压：

```powershell
tar xjvf vim-7.4.tar.bz2
1
```

解压完后的目录是 vim74
然后进入这个目录，开始配置：

```powershell
cd vim74/
./configure
12
```

注意：配置过程中可能提示缺少 libncurses5-dev 这个依赖而结束，我们使用 `sudo apt-get install libncurses5-dev` 安装即可，在重新配置之前，需要使用 `make clean` 先清理一次。

配置完成，开始 `make`，这个过程需要一段时间：

```powershell
make
1
```

make 完成，直接使用 root 安装：

```powershell
sudo make install
1
```

查看是否安装成功：

查看 vim 的版本信息

```powershell
vim --version
1
```

使用下面的命令卸载 vim，不过你应该不想卸载它：

```powershell
cd vim74/
sudo make uninstall
12
```

再强调一遍：如果你在安装过程中遇到任何问题，请仔细阅读错误提示信息，然后尝试使用 Google 来搜索提示的错误信息，**一般情况下，你在 stackoverflow 网站上都能得到正确的答案，但是前提是你要有基本的英文阅读能力。**

### 3. sudo apt-get install vim

正如标题那样，你只需要在命令行输入上面的命令即可安装 vim：

```powershell
sudo apt-get install vim
1
```

使用下面的命令来卸载 vim：

```powershell
sudo apt-get remove --purge vim
1
```

使用这种方法可以说非常的简单，前提是需要联网。

总结
这次介绍了在 Linux 下安装软件的 3 种方法，其中比较常用的是通过 apt-get 安装，其次是通过源码安装，最后才是使用安装包安装，不过这也要看你自己的爱好和能力。建议大家一定要掌握如何自己编译源码来安装软件，因为很多时候可能没有网络，那么这时你会自己编译源码就比别人强，也更加能够得到别人的青睐，就算不为了这，编译源码你不觉得很酷吗，别人都看不懂你的黑窗口，不是吗？hh

DL