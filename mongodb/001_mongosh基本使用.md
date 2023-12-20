## mongosh基本使用



### 1 下载

https://www.mongodb.com/try/download/shell



### 2 连接mongodb

```bash
mongosh mongodb://<username>:<password>@<ip/host>:<port>/<database>
```

host：默认本机

port：默认 27017 



```bash
mongosh mongodb://root:123456@192.168.0.112:32631/
```

