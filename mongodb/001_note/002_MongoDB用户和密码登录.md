## MongoDB用户和密码登录



### 一、MongoDB中内置角色

| **角色**             | **介绍**                                                     |
| -------------------- | ------------------------------------------------------------ |
| read                 | 提供读取所有非系统的集合（数据库）                           |
| readWrite            | 提供读写所有非系统的集合（数据库）和读取所有角色的所有权限   |
| dbAdmin              | 提供执行管理任务的功能，例如与架构相关的任务，索引编制，收集统计信息。此角色不授予用户和角色管理权限。 |
| dbOwner              | 提供对数据库执行任何管理操作的功能。此角色组合了readWrite，dbAdmin和userAdmin角色授予的权限。 |
| userAdmin            | 提供在当前数据库上创建和修改角色和用户的功能。由于userAdmin角色允许用户向任何用户（包括他们自己）授予任何权限，因此该角色还间接提供对数据库的超级用户访问权限，或者，如果作用于管理数据库，则提供对群集的访问权限。 |
| clusterAdmin         | 提供最佳的集群管理访问。此角色组合了clusterManager，clusterMonitor和hostManager角色授予的权限。此外，该角色还提供了dropDatabase操作。 |
| readAnyDatabase      | 仅在admin 数据库中使用，提供所有数据库的读权限。             |
| readWriteAnyDatabase | 尽在admin 数据库中使用，提供所有数据库的读写权限             |
| userAdminAnyDatabase | 尽在admin 数据库中使用，提供与userAdmin相同的用户管理操作访问权限，允许用户向任何用户（包括他们自己）授予任何权限，因此该角色还间接提供超级用户访问权限。 |
| dbAdminAnyDatabase   | 仅在admin 数据库中使用，提供与dbAdmin相同的数据库管理操作访问权限，该角色还在整个群集上提供listDatabases操作。 |
| root                 | 尽在admin 数据库中使用，提供超级权限                         |



### 二、创建管理员用户

**创建管理员**

连接mongodb

 

```
mongo --host 10.10.18.11
```

 

```
use admin
db.createUser(
  {
    user: "myUserAdmin",
    pwd: "abc123",
    roles: [ { role: "userAdminAnyDatabase", db: "admin" }, "readWriteAnyDatabase" ]
  }
)
```



创建管理员账号：myUserAdmin 密码：abc123

 

查看创建的管理员账号

```
use admin
db.getUser("myUserAdmin")
```

重启MongoDB实例

**连接MongoDB**

1、类似Mysql一样连接

```
mongo --host 10.10.18.11 -u "myUserAdmin" --authenticationDatabase "admin" -p'abc123'
```

结果：

```
1 MongoDB shell version v4.0.10
2 connecting to: mongodb://10.10.18.11:27017/?authSource=admin&gssapiServiceName=mongodb
3 Implicit session: session { "id" : UUID("3b067347-1b0e-4761-9399-cb3ad4ba6c93") }
4 MongoDB server version: 4.0.10
```

2、登录后进行验证

连接mongodb

```
mongo --host 10.10.18.11
```

进行验证

```
rs0:PRIMARY> use admin
switched to db admin
rs0:PRIMARY> db.auth("myUserAdmin", "abc123" )
1
```

**三、创建普通用户**

**创建一个普通用户**

用户名：myTester
密码：xyz123
权限：读写数据库 test， 只读数据库 reporting。



```
use test
db.createUser(
  {
    user: "myTester",
    pwd: "xyz123",
    roles: [ { role: "readWrite", db: "test" },
             { role: "read", db: "reporting" } ]
  }
)
```



**普通用户连接MongoDB实例**

```
mongo --host 10.10.18.11 -u "myTester" --authenticationDatabase "test" -p'xyz123' 
```

结果：

```
1 MongoDB shell version v4.0.10
2 connecting to: mongodb://10.10.18.11:27017/?authSource=test&gssapiServiceName=mongodb
3 Implicit session: session { "id" : UUID("3e9011ee-729f-4112-acd1-f5d1515490ac") }
4 MongoDB server version: 4.0.10
```

验证权限

在test集合中插入、查询数据

```bash
rs0:PRIMARY> db.test.insertOne({name:"sue",age:19,status:'p'})
```

```json
{
        "acknowledged" : true,
        "insertedId" : ObjectId("5d00b364a75d40ae9b83c64c")
}
```

```bash
rs0:PRIMARY> db.test.find({name:"sue"})
```

```json
{ "_id" : ObjectId("5d00b364a75d40ae9b83c64c"), "name" : "sue", "age" : 19, "status" : "p" }
```

