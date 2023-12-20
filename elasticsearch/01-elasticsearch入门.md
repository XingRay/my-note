### Elasticsearch 入门

官方文档

https://www.elastic.co/guide/en/elasticsearch/reference/current/elasticsearch-intro.html



#### 1. 安装

官方文档 

https://www.elastic.co/guide/en/elasticsearch/reference/current/install-elasticsearch.html



见 `/install` 教程



### 2. RestAPI

#### 2.1 基本检索

查看所有节点信息

```bash
GET /_cat/nodes
```

```
127.0.0.1 19 36 -1    cdfhilmrstw * DESKTOP-LEIXING
```



查看es的健康状态

```bash
GET /_cat/health
```

```
1682605497 14:24:57 elasticsearch yellow 1 1 13 13 0 0 2 0 - 86.7%
```



查看主节点

```bash
GET /_cat/master
```

```
fVUiI2rwQ9CTWu1wGZwcfw 127.0.0.1 127.0.0.1 DESKTOP-LEIXING
```



查看所有索引列表（类似于mysql中的 show databases ）

```bash
GET /_cat/indices
```

```
yellow open data     5x_GmLNCRP6PGgT8PpY2vQ 1 1 1 0 3.4kb 3.4kb
yellow open customer rpfIdtasRjujdS3ynOk2WA 1 1 3 0 5.2kb 5.2kb
```
