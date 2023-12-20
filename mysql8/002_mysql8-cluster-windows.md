## 在Windows中以cluster模式安装mysql8



./mysqld.exe --defaults-file="D:\develop\oracle\mysql\my.ini" --initialize --console
./mysqld.exe --install mysql --defaults-file="D:\develop\oracle\mysql\my.ini"
net start mysql
./mysql.exe -h localhost -u root -P 3306 -p

alter user 'root'@'localhost' identified with mysql_native_password by '123456';
flush privileges;

use mysql;
create user 'root'@'%' identified by '123456';
grant all on *.* to 'root'@'%';
alter user 'root'@'%' identified with mysql_native_password by '123456';
flush privileges;









./mysqld.exe --defaults-file="D:\develop\oracle\mysql2\my.ini" --initialize --console
./mysqld.exe --install mysql2 --defaults-file="D:\develop\oracle\mysql2\my.ini"
net start mysql2
./mysql.exe -h localhost -u root -P 3307 -p

alter user 'root'@'localhost' identified with mysql_native_password by '123456';
flush privileges;

use mysql;
create user 'root'@'%' identified by '123456';
grant all on *.* to 'root'@'%';
alter user 'root'@'%' identified with mysql_native_password by '123456';
flush privileges;









master:
my.ini:

[mysqld]
# 服务器标识ID
server-id=13306
#二进制日志文件格式
log-bin=mysql-bin

sql:

create user 'slave'@'127.0.0.1' identified with mysql_native_password by '123456';
show master status;

+------------------+----------+--------------+------------------+-------------------+
| File             | Position | Binlog_Do_DB | Binlog_Ignore_DB | Executed_Gtid_Set |
+------------------+----------+--------------+------------------+-------------------+
| mysql-bin.000001 |     1771 |              |                  |                   |
+------------------+----------+--------------+------------------+-------------------+
1 row in set (0.00 sec)



slave:

my.ini:

[mysqld]
# 服务器标识ID
server-id=13307
#二进制日志文件格式
log-bin=mysql-bin
# 从库二进制日志
relay-log=mysql-relay



sql:

change master to master_host='127.0.0.1',
master_user='slave',
master_password='123456',
master_log_file='mysql-bin.000001',
master_log_pos=1771;

start slave;
show slave status \G





