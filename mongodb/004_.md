## mongo导入数据



### 1 安装mongosh 

https://www.mongodb.com/try/download/shell ，

https://downloads.mongodb.com/compass/mongosh-1.10.1-win32-x64.zip

下载后将 bin 目录配置到系统 path



### 2 登录mongodb

```bash
mongosh --username mongo --password 123456 --host 192.168.0.112 --port 32631 --authenticationDatabase admin
```



### 3 创建数据库

```bash
use yygh_hosps
```



### 4 创建用户

再给数据库创建一个用户

```bash
db.createUser({user:"root", pwd:"123456", roles:[{role:"dbAdmin",db:"yygh_hosps"}]})
```



### 5 安装 MongoDB Command Line Database Tools

https://www.mongodb.com/compatibility/json-to-mongodb

下载 MongoDB Command Line Database Tools

https://www.mongodb.com/try/download/database-tools

https://fastdl.mongodb.org/tools/db/mongodb-database-tools-windows-x86_64-100.7.3.zip

下载后将 bin 目录添加到系统path



### 6 导入数据

数据保存在 /data/json 目录下，在项目的根目录执行执行

```bash
mongoimport --uri mongodb://root:123456@192.168.0.112:32631/yygh_hosps --collection Department --jsonArray data\json\Department.json
```

```bash
mongoimport --uri mongodb://root:123456@192.168.0.112:32631/yygh_hosps --collection Hospital --jsonArray data\json\Hospital.json
```

```bash
mongoimport --uri mongodb://root:123456@192.168.0.112:32631/yygh_hosps --collection Schedule --jsonArray data\json\Schedule.json
```

