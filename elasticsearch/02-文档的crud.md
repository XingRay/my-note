## ElasticSearch文档的操作

官方文档 https://www.elastic.co/guide/en/elasticsearch/reference/current/docs.html

### 1. 保存

将文档保存到`{index}`索引下，保存的文档id为`{id}` ,注意，es8中取消了type，旧版本的type在新版本中统一指定为`_doc`

```bash
PUT /{index}/_doc/{id}
```

如：

```http
http://127.0.0.1:9200/user/_doc/1
{
    "name": "jack"
}
```

```json
{
    "_index": "user",
    "_id": "1",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 0,
    "_primary_term": 1
}
```

注意：PUT请求方式必须带${id}，${id}对应的数据不存在则创建，存在则覆盖旧数据



或者使用post请求

```json
POSThttp://127.0.0.1:9200/user/_doc
{
    "name": "Donna Martinez"
}
```

```json
{
    "_index": "user",
    "_id": "0fAlw4cBVBx4L_rZy43S",
    "_version": 1,
    "result": "created",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 1,
    "_primary_term": 1
}
```



使用POST请求可以带${id}, 数据存在则覆盖，不存在则创建

```json
POSThttp://127.0.0.1:9200/user/_doc/1
{
    "name": "tom"
}
```

```json
{
    "_index": "user",
    "_id": "1",
    "_version": 3,
    "result": "updated",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 3,
    "_primary_term": 1
}
```

注意，上述接口保存数据时，如果数据已存在会覆盖数据，并且`_version`和`_seq_no`会自增，可以作为乐观锁实现，如：

客户端1：

```
http://127.0.0.1:9200/user/_doc/1?if_seq_no=7&if_primary_term=1
{
    "name": "jack"
}
```

```json
{
    "_index": "user",
    "_id": "1",
    "_version": 8,
    "result": "updated",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 8,
    "_primary_term": 1
}
```

同时客户端2也发送请求

```http
http://127.0.0.1:9200/user/_doc/1?if_seq_no=&if_seq_no=7&if_primary_term=1
{
    "name": "tom"
}
```

```json
{
    "error": {
        "root_cause": [
            {
                "type": "version_conflict_engine_exception",
                "reason": "[1]: version conflict, required seqNo [7], primary term [1]. current document has seqNo [8] and primary term [1]",
                "index_uuid": "JQA6rNZpSZWf5meJ38nM5A",
                "shard": "0",
                "index": "user"
            }
        ],
        "type": "version_conflict_engine_exception",
        "reason": "[1]: version conflict, required seqNo [7], primary term [1]. current document has seqNo [8] and primary term [1]",
        "index_uuid": "JQA6rNZpSZWf5meJ38nM5A",
        "shard": "0",
        "index": "user"
    },
    "status": 409
}
```

客户端1成功，客户端2失败，客户端2想要操作数据就必须要在请求一次最新数据再操作。



### 2. 查看数据

```bash
GET /{index}/_doc/{id}
```

查看指定{index}索引，指定{id}的数据，如：

```http
GET http://127.0.0.1:9200/user/_doc/1
```

```json
{
    "_index": "user",
    "_id": "1",
    "_version": 7,
    "_seq_no": 7,
    "_primary_term": 1,
    "found": true,
    "_source": {
        "name": "jack"
    }
}
```

也可以只查看source部分

```http
GET /{index}/_source/{id}
```

```json
{
  "name": "Michael Clark"
}
```



### 3. 更新文档

```http
POST /{index}/_update/{id}
```

如：

```
POST http://127.0.0.1:9200/user/_update/1
{
    "doc": {
        "name": "Michael Clark"
    }
}
```

```json
{
    "_index": "user",
    "_id": "1",
    "_version": 9,
    "result": "updated",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 9,
    "_primary_term": 1
}
```

如果要更新的数据与已存储的数值一致，使用 /_update 操作没有实际更新，返回值如下：

```json
{
    "_index": "user",
    "_id": "1",
    "_version": 10,
    "result": "noop",
    "_shards": {
        "total": 0,
        "successful": 0,
        "failed": 0
    },
    "_seq_no": 10,
    "_primary_term": 1
}
```

`_version` `_seq_no` 不变，result": "noop", 表示没有操作

注意 前面**2.2.1**的`POST`/`PUT`方式的更新本质会覆盖旧数据，不会做对比。因此会更新`_version`和`_seq_no`



### 4. 删除数据

```http
DELETE /{index}/_doc/{id}
```

如：

```http
DELETE http://127.0.0.1:9200/user/_doc/1
```

```json
{
    "_index": "user",
    "_id": "1",
    "_version": 11,
    "result": "deleted",
    "_shards": {
        "total": 2,
        "successful": 1,
        "failed": 0
    },
    "_seq_no": 11,
    "_primary_term": 2
}
```



### 5. 批量操作数据

注意最后要有一个空行，body部分并不是一个标准的json



```http
POST /_bulk
POST /{index}/_bulk
```

```http
POST /_bulk
{ "delete" : { "_index" : "user", "_id" : "1" } }
{ "index" : { "_index" : "user", "_id" : "2" } }
{ "name" : "jim" }
{ "create" : { "_index" : "user", "_id" : "3" } }
{ "name" : "mike" }
{ "update" : {"_index" : "user", "_id" : "4"} }
{ "doc" : {"name" : "bob"} }

```

```json
{
  "took": 304,
  "errors": true,
  "items": [
    {
      "index": {
        "_index": "user",
        "_id": "2",
        "_version": 1,
        "result": "created",
        "_shards": {
          "total": 2,
          "successful": 1,
          "failed": 0
        },
        "_seq_no": 14,
        "_primary_term": 2,
        "status": 201
      }
    },
    {
      "delete": {
        "_index": "test",
        "_id": "1",
        "_version": 1,
        "result": "not_found",
        "_shards": {
          "total": 2,
          "successful": 1,
          "failed": 0
        },
        "_seq_no": 0,
        "_primary_term": 1,
        "status": 404
      }
    },
    {
      "create": {
        "_index": "test",
        "_id": "3",
        "_version": 1,
        "result": "created",
        "_shards": {
          "total": 2,
          "successful": 1,
          "failed": 0
        },
        "_seq_no": 1,
        "_primary_term": 1,
        "status": 201
      }
    },
    {
      "update": {
        "_index": "4",
        "_id": "1",
        "status": 404,
        "error": {
          "type": "document_missing_exception",
          "reason": "[1]: document missing",
          "index_uuid": "IjupY6mBR5Gu9ONDugd1fA",
          "shard": "0",
          "index": "4"
        }
      }
    }
  ]
}
```

每个操作的结果相互独立，在返回结果中都有展示

向指定索引批量导入数据：

```json
POST http://127.0.0.1:9200/bank-account/_bulk
{"index":{"_id":"1"}}
{"account_number":1,"balance":39225,"firstname":"Amber","lastname":"Duke","age":32,"gender":"M","address":"880 Holmes Lane","employer":"Pyrami","email":"amberduke@pyrami.com","city":"Brogan","state":"IL"}
{"index":{"_id":"6"}}
{"account_number":6,"balance":5686,"firstname":"Hattie","lastname":"Bond","age":36,"gender":"M","address":"671 Bristol Street","employer":"Netagy","email":"hattiebond@netagy.com","city":"Dante","state":"TN"}
{"index":{"_id":"13"}}
{"account_number":13,"balance":32838,"firstname":"Nanette","lastname":"Bates","age":28,"gender":"F","address":"789 Madison Street","employer":"Quility","email":"nanettebates@quility.com","city":"Nogal","state":"VA"}
{"index":{"_id":"18"}}
{"account_number":18,"balance":4180,"firstname":"Dale","lastname":"Adams","age":33,"gender":"M","address":"467 Hutchinson Court","employer":"Boink","email":"daleadams@boink.com","city":"Orick","state":"MD"}

```

**注意最后必须要有一个空行**

