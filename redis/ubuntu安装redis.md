## Ubuntu安装Redis



### Install on Ubuntu/Debian

You can install recent stable versions of Redis from the official `packages.redis.io` APT repository.

Prerequisites

If you're running a very minimal distribution (such as a Docker container) you may need to install `lsb-release` first:

```bash
sudo apt install lsb-release
```

Add the repository to the `apt` index, update it, and then install:

```bash
curl -fsSL https://packages.redis.io/gpg | sudo gpg --dearmor -o /usr/share/keyrings/redis-archive-keyring.gpg

echo "deb [signed-by=/usr/share/keyrings/redis-archive-keyring.gpg] https://packages.redis.io/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/redis.list

sudo apt-get update
sudo apt-get install redis
```



### 修改redis.conf

Redis的默认配置位于/etc/redis/redis.conf中，如果权限不足，修改权限即可

```bash
 chmod 777 redis.conf
```

1、用守护线程的方式启动redis ： daemonize

```bash
redis.conf 配置文件中daemonize守护线程，默认是NO，当前界面将进入redis的命令行界面，exit强制退出或者关闭连接工具(putty,xshell等)都会导致redis进程退出，redis终端窗口启用阻塞方式找开，即启动 redise 服务后，窗口不能干其它事。redis采用的是单进程多线程的模式。当 redis.conf 中选项 daemonize 设置成 yes 时，代表开启守护进程模式。在该模式下，redis会在后台运行，并将进程pid号写入至redis.conf选项pidfile设置的文件中，此时redis将一直运行，除非手动kill该进程。推荐daemonize改为yes，以守护进程运行
```



2、redis监听端口,即服务端口: port 6379

```bash
默认为 6379，如果你设为 0 ，redis 将不在 socket 上监听任何客户端连接。
```



3、数据库的数目： databases 16

```bash
缺省是16个，不需要创建，不建议修改
```



4、设置sedis进行数据库镜像的频率 ： save

```bash
根据给定的时间间隔和写入次数将数据保存到磁盘
注释掉“save”这一行配置项就可以让保存数据库功能失效
save 900 1
save 300 10
save 60 10000
上面例子意思是：
900 秒（15分钟）内如果至少有 1 个 key 的值变化，则保存（则进行数据库保存–持久化）
300 （5分钟）秒内如果至少有 10 个 key 的值变化，则保存（则进行数据库保存–持久化）
60 秒（1分钟）内如果至少有 10000 个 key 的值变化，则保存（则进行数据库保存–持久化）
```



5、开启远程访问:

```bash
默认仅允许本机访问，通过输入您希望Redis服务器监听的接口的值来更改IP地址。
如果您想添加多个IP地址，只需将它们用空格隔开即可
如果希望服务器侦听网络上的所有接口，则可以使用以下命令：bind 0.0.0.0
```



6.设置访问密码

```bash
redis访问缺省是没有密码，找到# requirepass foobared这一行，将注释符号#去掉，将后面修改成自己的密码，例如，设置密码为12345678
```



7.Redis的数据文件

```bash
dbfilename dump.rdb
```



8.数据文件存储路径

```bash
dir /var/lib/redis
```



### 重启redis

```bash
/etc/init.d/redis-server stop
/etc/init.d/redis-server start
/etc/init.d/redis-server restart
```

