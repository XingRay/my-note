# MongoDB 安装



## 1 下载安装包

下载mongodb

https://www.mongodb.com/try/download/community

https://fastdl.mongodb.org/windows/mongodb-windows-x86_64-8.2.2.zip



下载 compass

https://www.mongodb.com/try/download/compass

https://downloads.mongodb.com/compass/mongodb-compass-1.48.2-win32-x64.exe



下载 mongodb shell

https://www.mongodb.com/try/download/shell

https://downloads.mongodb.com/compass/mongosh-2.5.10-win32-x64.zip



## 2 安装mongodb

将mongodb解压到指定目录，将解压出来的带版本号的目录修改为 mongodb ， 并创建 config data log 目录，创建 start.cmd 脚本，完成后目录如下：

```
PS D:\develop\mongo\mongodb> ls

    Directory: D:\develop\mongo\mongodb

Mode                 LastWriteTime         Length Name
----                 -------------         ------ ----
d----          2025/12/14    13:30                config
d----          2025/12/14    13:38                data
d----          2025/12/14    13:31                log
d----            1980/1/1     0:00                mongodb
-a---          2025/12/14    13:32             51 start.cmd
```



在config目录中创建 mongod.cfg 文件， 内容如下：

```yaml
net:
   bindIp: localhost
   port: 27017
storage:
   dbPath: D:/develop/mongo/mongodb/data
systemLog:
   destination: file
   path: "D:/develop/mongo/mongodb/log/mongod.log"
   logAppend: true
```

详细配置： https://www.mongodb.com/zh-cn/docs/manual/administration/configuration/



在log目录中创建空文件 mongod.log



编辑 start.cmd

```shell
mongodb/bin/mongod.exe --config ./config/mongod.cfg
```

点击start启动即可



## 3 安装 MongoDB Shell

https://www.mongodb.com/zh-cn/docs/mongodb-shell/connect/?operating-system=windows&windows-installation-method=zip

解压 mongodb shell 安装包到指定目录

将 bin目录添加到系统path， 如：

```shell
D:\develop\mongo\mongosh\mongosh-2.5.10-win32-x64\bin
```

连接：

```shell
mongosh "mongodb://localhost:27017"
```

简写

```
mongosh
```



连接到非标准端口：

```shell
mongosh "mongodb://localhost:28015"
```

```shell
mongosh --port 28015
```



连接到远程主机

```shell
mongosh "mongodb://mongodb0.example.com:28015"
```

```shell
mongosh --host mongodb0.example.com --port 28015
```



指定连接选项：

以用户 `alice` 的身份在 `admin` 数据库上进行身份验证

```
mongosh "mongodb://mongodb0.example.com:28015" --username alice --authenticationDatabase admin
```

如要在连接命令中提供密码而不是使用提示符，请使用 --password



## 4 安装mongodb compass

运行 mongodb compass 程序即可， 程序会在桌面创建快捷方式， 可以将安装在 <user> 目录下的程序移动到指定目录







