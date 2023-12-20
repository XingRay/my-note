# Elasticsearch-7.14.x 实战教程笔记

## 前言

学习地址：[bilibili -【编程不良人】ElasticSearch最新实战教程](https://www.bilibili.com/video/BV1SQ4y1m7Ds)

## 索引（index）操作

索引只有查看、创建、删除操作，没有修改操作

### 查看已存在的索引

请求：

```bash
GET /_cat/indices?v
```

返回：

```bash
health  status  index  uuid  pri  rep  docs.count  docs.deleted  store.size  pri.store.size
xxx     xxx     xxx    xxx   xxx  xxx  xxx         xxx           xxx         xxx
```

`health`：索引健康状态

`status`：索引的打开状态

`index`：索引名称

`uuid`：索引的唯一标识

`pri`：即 primary，索引主分片数量

`rep`：即 replica，副本分片数量

`docs.count`：索引文档数

`docs.deleted`：索引中删除的文档数量

`store.size`：索引占用磁盘大小

`pri.store.size`：主分片存储大小

### 创建索引

请求：

```bash
PUT /products
```

返回：

```bash
{
  "acknowledged" : true,
  "shards_acknowledged" : true,
  "index" : "products"
}
```

`"acknowledged" : true` ：索引创建成功

`"shards_acknowledged" : true` ：分片创建成功

`"index" : "products"` ：索引名称为 products

### 创建副本分片数为 0 的索引

```bash
PUT /orders
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
```

### 删除索引

```bash
DELETE /products
```

## 映射（mapping）操作

映射可以由 ES 根据数据自动创建，但是生产环境一般都是手动创建

**注意：映射不能删除和修改**

### 常见字段类型

`字符串类型`：keyword（不分词）、text（分词）

`数字类型`：integer、long、float、double

`布尔类型`：boolean

`日期类型`：date，日期类型可以设置支持格式，常用的有 `yyyy-MM-dd HH:mm:ss` 、`yyyy-MM-dd` 、`epoch_millis`，可同时支持多种

### 创建索引 & 映射

```bash
# 创建商品索引 products，指定 mapping {id, title, price, created_at, description}
PUT /products
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "integer"
      },
      "title": {
        "type": "keyword"
      },
      "price": {
        "type": "double"
      },
      "created_at": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      },
      "description": {
        "type": "text"
      }
    }
  }
}
```

### 查看映射

```bash
GET /products/_mapping
```

## 文档基础操作

### 添加文档并指定 id

```bash
POST /products/_doc/1
{
  "id": 1,
  "title": "苹果",
  "price": 4.8,
  "created_at": "2022-09-07",
  "description": "苹果真好吃，真好吃"
}
```

### 添加文档

```bash
POST /products/_doc
{
  "title": "香蕉",
  "price": 3.6,
  "created_at": "2022-09-07",
  "description": "香蕉真好吃，真好吃"
}
```

### 文档查询，基于 id

```bash
GET /products/_doc/1
```

### 删除文档，基于 id

```bash
DELETE /products/_doc/1
```

### 更新文档

```bash
POST /products/_update/1
{
  "doc": {
    "price": 4.5
  }
}
```

**下面是一种 PUT 更新方式，会丢弃掉未传递的属性，慎用**

```bash
PUT /products/_doc/1
{
  "price": 4.5
}
```

## 文档批量操作

文档批量操作时不会因为一个失败而导致全部失败，而是继续执行后续操作，返回时按照批量操作的顺序依次返回每个操作的执行结果

### 批量添加

```bash
POST /products/_bulk
{"index":{"_id":2}}
{"id":2,"title":"香蕉","price":3.6,"created_at":"2022-09-07","description":"香蕉真好吃，真好吃"}
```

### 批量添加，不指定 id

```bash
POST /products/_bulk
{"index":{}}
{"id":1024,"title":"猕猴桃","price":3.6,"created_at":"2022-09-07","description":"猕猴桃真好吃，真好吃"}
```

### 批量添加、更新、删除

```bash
POST /products/_bulk
{"index":{"_id":3}}
{"id":3,"title":"橘子","price":3.6,"created_at":"2022-09-07","description":"橘子真好吃，真好吃"}
{"update":{"_id":1}}
{"doc":{"title":"红富士苹果"}}
{"delete":{"_id":2}}
```

## Query DSL（高级查询）

语法：

```bash
GET /{index}/_search {json}
```

### 数据准备

```bash
DELETE /products

# 创建商品索引 products，指定 mapping {id, title, price, created_at, description}
PUT /products
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "integer"
      },
      "title": {
        "type": "keyword"
      },
      "price": {
        "type": "double"
      },
      "created_at": {
        "type": "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      },
      "description": {
        "type": "text"
      }
    }
  }
}

# 批量添加数据
POST /products/_bulk
{"index":{"_id":1}}
{"id":1,"title":"苹果","price":4.8,"created_at":"2022-09-07 16:30:57","description":"苹果真好吃，真好吃"}
{"index":{"_id":2}}
{"id":2,"title":"香蕉","price":3.6,"created_at":"2022-09-07 16:31:08","description":"香蕉真好吃，真好吃"}
{"index":{"_id":3}}
{"id":3,"title":"orange","price":2.5,"created_at":"2022-09-07 16:31:19","description":"Oranges are not good, not good."}
{"index":{"_id":4}}
{"id":4,"title":"猕猴桃","price":6.5,"created_at":"2022-09-07 16:31:29","description":"猕猴桃真好吃，真好吃"}
{"index":{"_id":5}}
{"id":5,"title":"火龙果","price":2.5,"created_at":"2022-09-07 16:31:36","description":"火龙果不好吃，不好吃"}
{"index":{"_id":6}}
{"id":6,"title":"红烧排骨鱼翅","price":86,"created_at":"2022-09-07 17:05:31","description":"红烧排骨鱼翅，这个菜真独特"}
{"index":{"_id":7}}
{"id":7,"title":"小鱼豆腐","price":86,"created_at":"2022-09-07 17:05:31","description":"鱼豆腐真好吃，真好吃"}
```

### 查询所有（match_all）

```bash
GET /products/_search
{
  "query": {
    "match_all": {}
  }
}
```

### 关键词查询（term）

`text` 类型默认使用 es 标准分词，中文单字分词，英文单词分词，其他类型不分词

```bash
GET /products/_search
{
  "query": {
    "term": {
      "description": {
        "value": "吃"
      }
    }
  }
}
```

### 范围查询（range）

查询价格指定区间内的记录

```bash
GET /products/_search
{
  "query": {
    "range": {
      "price": {
        "gte": 3.6,
        "lte": 5
      }
    }
  }
}
```

查询创建时间在指定时间段内的记录

```bash
GET /products/_search
{
  "query": {
    "range": {
      "created_at": {
        "gte": "2022-09-07 16:30:57",
        "lte": "2022-09-07 16:31:08"
      }
    }
  }
}
```

`gte`：大于等于

`lte`：小于等于

`gt`：大于

`lt`：小于

### 前缀查询（prefix）

`title` 字段是 `keyword` 类型，没有分词，用前缀查询可以查出文档

```bash
GET /products/_search
{
  "query": {
    "prefix": {
      "title": {
        "value": "火龙"
      }
    }
  }
}
```

### 通配符查询（wildcard）

`?` 代表一个字符

`*` 代表任意多个字符

```bash
GET /products/_search
{
  "query": {
    "wildcard": {
      "description": {
        "value": "goo?"
      }
    }
  }
}
```

### ids 查询（ids）

```bash
GET /products/_search
{
  "query": {
    "ids": {
      "values": ["1", "2", "4"]
    }
  }
}
```

### 模糊查询（fuzzy）

用来模糊查询含有指定关键字的文档，最大模糊错误必须在 0 - 2 之间

**搜索关键词长度为 2，不允许存在模糊**

```bash
# 搜索关键词长度为 2，不允许存在模糊，无匹配查询结果
GET /products/_search
{
  "query": {
    "fuzzy": {
      "title": "红烧"
    }
  }
}

# 搜索关键词长度为 2，全字匹配，匹配到查询结果
GET /products/_search
{
  "query": {
    "fuzzy": {
      "title": "香蕉"
    }
  }
}
```

**搜索关键词长度为 3 - 5，允许 1 次模糊**

```bash
# 关键词长度为 4，允许 1 次模糊，匹配到查询结果
GET /products/_search
{
  "query": {
    "fuzzy": {
      "title": "小鱼豆豆"
    }
  }
}

# 出现两次模糊，无匹配查询结果
GET /products/_search
{
  "query": {
    "fuzzy": {
      "title": "小鱼小豆"
    }
  }
}
```

**搜索关键词长度大于 5，允许最大 2 次模糊**

```bash
# 关键词长度为 6，出现两次模糊，匹配到查询结果
GET /products/_search
{
  "query": {
    "fuzzy": {
      "title": "红烧排骨小小"
    }
  }
}

# 超过两次模糊，无匹配查询结果
GET /products/_search
{
  "query": {
    "fuzzy": {
      "title": "红烧排小小小"
    }
  }
}
```

### 布尔查询（bool）

用来组合多个条件实现复杂查询

`must`：相当于 `&&`，需同时成立

`should`：相当于 `||`，仅需一个成立

`must_not`：相当于 `!`，不能满足任何一个条件

**must 查询**：

```bash
# 只能查出苹果
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "ids": {
            "values": [1, 2]
          }
        },
        {
          "term": {
            "title": {
              "value": "苹果"
            }
          }
        }
      ]
    }
  }
}

# 不存在 id 为 1 或 2，同时标题为猕猴桃的商品，无匹配查询结果
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "ids": {
            "values": [1, 2]
          }
        },
        {
          "term": {
            "title": {
              "value": "猕猴桃"
            }
          }
        }
      ]
    }
  }
}
```

**should 查询**：

```bash
# 查询出 id 为 1 或 2，以及标题为猕猴桃的所有记录，合计三条
GET /products/_search
{
  "query": {
    "bool": {
      "should": [
        {
          "ids": {
            "values": [1, 2]
          }
        },
        {
          "term": {
            "title": {
              "value": "猕猴桃"
            }
          }
        }
      ]
    }
  }
}
```

**must_not 查询：**

```bash
# 查询出 id 不为 1、2，同时标题不为猕猴桃的所有记录
GET /products/_search
{
  "query": {
    "bool": {
      "must_not": [
        {
          "ids": {
            "values": [1, 2]
          }
        },
        {
          "term": {
            "title": {
              "value": "猕猴桃"
            }
          }
        }
      ]
    }
  }
}
```

### 多字段查询（multi_match）

会根据字段类型确定是否做分词，然后进行查询

```bash
# title 字段是 keyword 类型，不分词，description 字段是 text，会做分词，“火果” 被分为 “火” 和 “果” 然后做分词查询，故可以返回苹果和火龙果两条记录
GET /products/_search
{
  "query": {
    "multi_match": {
      "query": "火果",
      "fields": ["title", "description"]
    }
  }
}
```

### 默认字段分词查询（query_string）

仅对指定的字段做分词查询，如果指定的字段不支持分词，也就不做分词

```bash
GET /products/_search
{
  "query": {
    "query_string": {
      "default_field": "description",
      "query": "鱼儿真可爱"
    }
  }
}
```

### 高亮查询（highlight）

仅对分词字段做高亮，单独显示在 `highlight` 属性节点中，不修改 `source`。返回结果会对分词添加 `<em>` 标签，在 html 页面添加对应 css 样式，实现高亮

```bash
GET /products/_search
{
  "query": {
    "query_string": {
      "default_field": "description",
      "query": "鱼儿真可爱"
    }
  },
  "highlight": {
    "fields": {
      "*": {}
    }
  }
}
```

不使用 `<em>` 标签，使用自定义高亮标签

```bash
GET /products/_search
{
  "query": {
    "query_string": {
      "default_field": "description",
      "query": "鱼儿真可爱"
    }
  },
  "highlight": {
    "fields": {
      "*": {}
    },
    "require_field_match": false,
    "pre_tags": ["<span style='color:red;'>"],
    "post_tags": ["</span>"]
  }
}
require_field_match` 的默认值为 `true`，搜索 `description` 字段的时候，我们希望对其他字段中的关键字也能高亮。这个时候我们需要把 `require_field_match` 属性的值设置为 `false
```

### 查询分页

es 默认仅返回前 10 条文档，通过 `size` 来指定返回条数

```bash
GET /products/_search
{
  "query": {
    "match_all": {}
  },
  "size": 5
}
```

`from` 默认为 0，通过 `size` 配合 `from` 实现分页查询

```bash
# 分页大小为 5，从第五条开始，即查询第二页的结果
GET /products/_search
{
  "query": {
    "match_all": {}
  },
  "size": 5,
  "from": 5
}
```

### 查询排序

按照价格降序，`sort` 支持多个排序字段

```bash
GET /products/_search
{
  "query": {
    "match_all": {}
  },
  "sort": [
    {
      "price": {
        "order": "desc"
      }
    }
  ]
}
```

### 返回指定字段

仅返回 `title` 和 `description`

```bash
GET /products/_search
{
  "query": {
    "match_all": {}
  },
  "_source": ["title", "description"]
}
```

## 索引原理

视频教程：https://www.bilibili.com/video/BV1SQ4y1m7Ds?p=23&vd_source=802755c185d6a0a2c0119172012bd979

### 倒排索引

`Elasticsearch` 底层使用的是倒排索引（inverted index），也叫反向索引，正向索引是根据 key 找 value，反向索引即通过 value 找 key。

标签: [ElasticSearch](https://www.cnblogs.com/nihaorz/tag/ElasticSearch/)