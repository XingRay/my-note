## MongoDB用户权限管理及配置



### 1 角色及权限

Built-In Roles（内置角色）：

| 类型           | 角色                                                         |
| -------------- | ------------------------------------------------------------ |
| 数据库用户角色 | read、readWrite                                              |
| 数据库管理角色 | dbAdmin、dbOwner、userAdmin                                  |
| 集群管理角色   | clusterAdmin、clusterManager、clusterMonitor、hostManager    |
| 备份恢复角色   | backup、restore                                              |
| 所有数据库角色 | readAnyDatabase、readWriteAnyDatabase、userAdminAnyDatabase、dbAdminAnyDatabase |
| 超级用户角色   | root  这里还有几个角色间接或直接提供了系统超级用户的访问（dbOwner 、userAdmin、userAdminAnyDatabase） |
| 内部角色       | __system                                                     |



具体角色的功能：

| 权限                 | 说明                                                         |
| :------------------- | :----------------------------------------------------------- |
| read                 | 允许用户读取指定数据库                                       |
| readWrite            | 允许用户读写指定数据库                                       |
| dbAdmin              | 允许用户在指定数据库中执行管理函数，如索引创建、删除、查看统计或访问system.profile |
| userAdmin            | 允许用户向system.users集合写入，可以在指定数据库中创建、删除和管理用户 |
| clusterAdmin         | 必须在admin数据库中定义，赋予用户所有分片和复制集相关函数的管理权限 |
| readAnyDatabase      | 必须在admin数据库中定义，赋予用户所有数据库的读权限          |
| readWriteAnyDatabase | 必须在admin数据库中定义，赋予用户所有数据库的读写权限        |
| userAdminAnyDatabase | 必须在admin数据库中定义，赋予用户所有数据库的userAdmin权限   |
| dbAdminAnyDatabase   | 必须在admin数据库中定义，赋予用户所有数据库的dbAdmin权限     |
| root                 | 必须在admin数据库中定义，超级账号，超级权限                  |


MongoDB操作-备份和恢复，导入和导出
　　mongodb数据备份和恢复主要分为二种：一种是针对库的mongodump和mongorestore，一种是针对库中表的mongoexport和mongoimport

2. 新建账号

账号跟数据库是绑定的，所以要先指定数据库

```bash
use 数据库;
show users; //查看已有账号
db.createUser(
 {
 user:"test",
 pwd:"123456",
 roles:[{role:"root",db:"admin"},{role: "userAdminAnyDatabase", db: "admin"}]
 }
)
```

查看全部用户用db.getUsers()；
用新建的账号连接试一下

新建数据库cicatDb
use cicatDb；
在数据库的基础上创建用户，然后连接，这里注意，如果是admin数据库的账号，连接是不需要写数据库的，如果不是，如下方，连接的话要写上数据库，否则连接不上

```bash
mongo 192.168.100.16:27016/cicatDb -u 用户名 -p
```


2.修改账号密码

超级用户root，修改其密码

use 数据库;

```bash
db.changeUserPassword('username','test'); 
```

或者

```bash
db.updateUser(‘username',{pwd:'456'})
```



3. 删除账号

```bash
db.dropUser(<user_name>) 
```

删除某个用户，接受字符串参数

示例：

```bash
db.dropUser(“admin”)
```



4. 修改角色

添加用户权限（切换到有权限操作的数据库下）

```bash
db.grantRolesToUser(‘username’,[{role:‘XXX’,db:‘YYY’}])
```

移除用户权限（切换到有权限操作的数据库下）

```bash
db.revokeRolesFromUser(‘username’,[{role:‘’,db:‘’}])
```

