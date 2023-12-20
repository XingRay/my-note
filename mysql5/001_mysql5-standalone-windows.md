## 在Windows中以standalone模式安装mysql5

### 1 删除以前安装的MySQL服务
#### 1.1 查找以前是否装有mysql

```cmd
sc query mysql
```

无结果，说明未安装过mysql或者已经卸载mysql服务，接下来直接安装mysql即可。

有结果则需要执行1.2：删除之前安装的mysql服务

#### 1.2 删除mysql服务

以管理员模式打开命令运行，运行下面命令

##### 1.2.1 停止mysql服务：

```cmd
net stop mysql
```

注：**删除服务之前必须先停止服务，否则会删除失败** 

##### 1.2.2 删除mysql服务：

```cmd
sc delete mysql
```

##### 1.2.3 检查mysql是否已删除

```cmd
sc query mysql
```



### 2 下载mysql二进制包

https://dev.mysql.com/downloads/mysql/

https://cdn.mysql.com//Downloads/MySQL-5.7/mysql-5.7.42-winx64.zip

下载后解压至指定目录，如：

```bash
D:/develop/mysql/mysql5/app/mysql-5.7.42-winx64
```



### 3 安装前的准备

#### 3.1 data目录

如果需要指定data目录，可以在指定目录创建好data目录，如 

```bash
D:/develop/mysql/mysql5/standalone/data
```



#### 3.2 mysql5程序的安装目录

mysql安装的zip包的解压后保存的路径，如：

```bash
D:/develop/mysql/mysql5/app/mysql-5.7.42-winx64
```



#### 3.1 my.ini

在指定目录创建 my.ini，如 

```bash
D:/develop/mysql/mysql5/standalone/config/my.ini
```

注意根据实际情况修改

```ini
[mysqld]
# 设置服务器的监听端口
port=3406
# 设置mysql的安装目录 ---这里输入你安装的文件路径----
basedir="D:/develop/mysql/mysql5/app/mysql-5.7.42-winx64"
# 设置mysql数据库的数据的存放目录
datadir="D:/develop/mysql/mysql5/standalone/data"
```

完整的内容如下：

```ini
[mysqld]
# 服务端使用的字符集
character-set-server=utf8mb4
# 设置服务器的监听端口
port=3406
# 设置mysql的安装目录 ---这里输入你安装的文件路径----
basedir="D:/develop/mysql/mysql5/app/mysql-5.7.42-winx64"
# 设置mysql数据库的数据的存放目录
datadir="D:/develop/mysql/mysql5/standalone/data"

# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB
# 设置时区
default-time_zone='+8:00'
# 允许最大连接数
max_connections=200
# 允许连接失败的次数。这是为了防止有人从该主机试图攻击数据库系统
max_connect_errors=10

# 服务器标识ID
server-id=13501
#二进制日志文件格式
log-bin=mysql-bin

# 严格模式
#sql_mode="NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES"
# 默认使用“mysql_native_password”插件认证
#default_authentication_plugin=mysql_native_password
# 跳过密码验证,设置密码后注释掉
#skip-grant-tables

[mysql]
# 设置mysql客户端默认字符集
default-character-set=utf8mb4


[client]
# 客户端使用的字符集
default-character-set=utf8mb4
# 设置mysql客户端连接服务端时默认使用的端口
port=3406
```



### 4 初始化mysqld

#### 4.1 执行命令初始化mysqld

```bash
D:/develop/mysql/mysql5/app/mysql-5.7.42-winx64/bin/mysqld.exe --defaults-file="D:/develop/mysql/mysql5/standalone/config/my.ini" --initialize --console
```

注意初始化成功后控制台会输出root用户的初始密码，如：

```bash
A temporary password is generated for root@localhost: Pa/pFOfq4hW)
```

复制到记事本保留。如果忘记密码，可以清空data目录，重新执行此步骤



#### 4.2 安装mysql服务

注意根据需要修改服务名

```bash
D:/develop/mysql/mysql5/app/mysql-5.7.42-winx64/bin/mysqld.exe --install mysql5-standalone --defaults-file="D:/develop/mysql/mysql5/standalone/config/my.ini"
```



#### 4.3 启动mysql

这里服务名要与上面设置的一致

```bash
net start mysql5-standalone
```



### 5 重置root密码

#### 5.1 登录到mysql

使用命令行工具连接mysq

```bash
D:/develop/mysql/mysql5/app/mysql-5.7.42-winx64/bin/mysql.exe -h localhost -u root -P 3406 -p
```

输入初始化时生成的随机密码即可



#### 5.2 修改root用户密码

```bash
alter user 'root'@'localhost' identified with mysql_native_password by '123456';
```

```bash
flush privileges;
```



#### 5.3 配置远程访问

```sql
use mysql;
create user 'root'@'%' identified by '123456';
grant all on *.* to 'root'@'%';
alter user 'root'@'%' identified with mysql_native_password by '123456';
flush privileges;
```

安装完成



### 6 相关命令

查询服务

```cmd
sc query mysql
```

删除服务

```cmd
sc delete mysql
```

启动服务

```cmd
net start mysql
```

停止服务

```cmd
net stop mysql
```
