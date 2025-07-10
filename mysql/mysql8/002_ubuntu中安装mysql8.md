# 在Ubuntu中安装Mysql8

安装

```bash
sudo apt update
```

```bash
sudo apt install -y mysql-server
```



免密登录

```bash
sudo mysql -uroot
```



修改root密码

```bash
alter user 'root'@'localhost' identified with mysql_native_password by 'your_new _password';
```

如:

```bash
alter user 'root'@'localhost' identified with mysql_native_password by '123456';
```



配置远程登录

```bash
mysql -uroot -p
```



```sql
use mysql;
```

```sql
update user set host='%' where user='root'
```

```sql
flush privileges;
```

```sql
grant all on *.* to 'root'@'%';
```

```sql
flush privileges;
```





解决无法远程访问的问题:

```sql
use mysql;
select Host,User from user;
```

```bash
mysql> use mysql;
Reading table information for completion of table and column names
You can turn off this feature to get a quicker startup with -A

Database changed
mysql> select Host,User from user;
+-----------+------------------+
| Host      | User             |
+-----------+------------------+
| %         | root             |
| localhost | debian-sys-maint |
| localhost | mysql.infoschema |
| localhost | mysql.session    |
| localhost | mysql.sys        |
+-----------+------------------+
5 rows in set (0.00 sec)
```



查看端口占用:

```bash
sudo apt install -y net-tools
```

```bash
netstat -lntp | grep 3306
```

```bash
root@ubuntu-dev:~# netstat -lntp | grep 3306
tcp        0      0 127.0.0.1:3306          0.0.0.0:*               LISTEN      27728/mysqld
tcp        0      0 127.0.0.1:33060         0.0.0.0:*               LISTEN      27728/mysqld
```



修改mysql配置

```
vi /etc/mysql/mysql.conf.d/mysqld.cnf
```

找到bind-address,把127.0.0.1修改成0.0.0.0

```properties
# If MySQL is running as a replication slave, this should be
# changed. Ref https://dev.mysql.com/doc/refman/8.0/en/server-system-variables.html#sysvar_tmpdir
# tmpdir                = /tmp
#
# Instead of skip-networking the default is now to listen only on
# localhost which is more compatible and is not less secure.
bind-address            = 0.0.0.0
mysqlx-bind-address     = 127.0.0.1
```



重启mysql

```bash
service mysql restart
```

再次查看端口

```bash
netstat -lntp | grep 3306
```

```bash
root@ubuntu-dev:~# netstat -lntp | grep 3306
tcp        0      0 127.0.0.1:33060         0.0.0.0:*               LISTEN      56900/mysqld
tcp        0      0 0.0.0.0:3306            0.0.0.0:*               LISTEN      56900/mysqld
```

这样就可以远程连接了



