# ElasticSearch数据备份与还原



1 安装 nodejs

需要使用nodejs插件，依赖nodejs，这里使用的nodejs版本为 v18.16.0



2 安装 elasticdump

```
npm install elasticdump -g
```



3 查看es上的所有index

```bash
http://192.168.0.108:49200/_cat/indices?v
```



4 将索引中的数据导出到本地

```
elasticdump --input=http://localhost:9200/demo --output=D:/ES/date/demo.json
```

其中，demo是索引。



5 将本地数据导入es中

```
elasticdump --input=D:/ES/date/demo.json --output=http://localhost:9200/demo1
```



6 将es导入另一个es

```bash
elasticdump --input=http://ip:9200/demo --output=http://127.0.0.1:9200/demo
```

