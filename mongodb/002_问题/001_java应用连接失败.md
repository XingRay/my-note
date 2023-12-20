使用配置文件里的账号密码连接指定的服务器是可以的，但是在java项目提示连接失败：

```
org.springframework.data.mongodb.UncategorizedMongoDbException: Exception authenticating MongoCredential{mechanism=SCRAM-SHA-256, userName='peter', source='data_export', password=<hidden>, mechanismProperties=<hidden>}; nested exception is com.mongodb.MongoSecurityException: Exception authenticating MongoCredential{mechanism=SCRAM-SHA-256, userName='peter', source='data_export', password=<hidden>, mechanismProperties=<hidden>}，
```



解决办法：给密码加上 ‘ ’ 

```yaml
data:
    mongodb:
      host: mongodb-standalone.project-test
      port: 27017
      database: yygh_hosps
      username: root
      password: 123456
```

修改为：

```yaml
data:
    mongodb:
      host: mongodb-standalone.project-test
      port: 27017
      database: yygh_hosps
      username: root
      password: '123456'
```



