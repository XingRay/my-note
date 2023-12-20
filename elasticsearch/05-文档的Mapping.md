## Elasticsearch的文档Mapping

https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html

Mapping是文档及其字段包含了什么样的数据，如何存储，和检索的定义过程。使用mapping来定义：

- 哪些文本字段作为全文检索的字段
- 哪些字段包换数值、日期和地理位置
- 数据的格式
- 动态添加字段的自定义规则



### 1. 数据类型

https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html

#### 1.1 核心类型

字符串(String)

https://www.elastic.co/guide/en/elasticsearch/reference/current/text.html

https://www.elastic.co/guide/en/elasticsearch/reference/current/keyword.html

​	text keyword

数字类型(Numeric)

https://www.elastic.co/guide/en/elasticsearch/reference/current/number.html

​	long/integer/short/byte/double/float/half_float/scaled_float

日期类型(Date)

https://www.elastic.co/guide/en/elasticsearch/reference/current/date.html

​	date

布尔类型(boolean)

https://www.elastic.co/guide/en/elasticsearch/reference/current/boolean.html

​	boolean

二进制类型(binary)

https://www.elastic.co/guide/en/elasticsearch/reference/current/binary.html

​	binary



#### 1.2 核心类型

数组类型 array

https://www.elastic.co/guide/en/elasticsearch/reference/current/array.html

​	Array

对象类型 Object

https://www.elastic.co/guide/en/elasticsearch/reference/current/object.html

​	object 用于单JSON对象

嵌套类型 Nested

https://www.elastic.co/guide/en/elasticsearch/reference/current/nested.html

​	nested 用户JSON数组



#### 1.3 地理类型 Geo

地理坐标 Geo-point

https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-point.html

​	geo_point 用于描述经纬度坐标

地理图形 Geo-Shape

https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html

 	geo_shape 用于描述复杂形状，如多边形



#### 1.4 特性类型

IP类型

https://www.elastic.co/guide/en/elasticsearch/reference/current/ip.html

​	ip 用于ipv4和ipv6地址

补全类型Completion

https://www.elastic.co/guide/en/elasticsearch/reference/current/completion.html

​	completion 提供自动完成提示

令牌计数类型 Token-Count

https://www.elastic.co/guide/en/elasticsearch/reference/current/token-count.html

​	token_count 用于统计字符串中的词条数量

附件类型 attachment

​	参考 mapper-attachment 插件，支持将附件如 Microsoft Office格式 OpenDocument epub HTML等索引为 attachment 数据类型

抽取类型 Percolator

https://www.elastic.co/guide/en/elasticsearch/reference/current/percolator.html

​	接受特定领域查询语言 query-dsl 的查询



### 2. mapping的操作

#### 2.1 查数据的mapping

查询索引为band-account 的mapping

```json
GET /bank-account/_mapping
```

返回值为：

```json
{
  "bank-account": {
    "mappings": {
      "properties": {
        "account_number": {
          "type": "long"
        },
        "address": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "age": {
          "type": "long"
        },
        "balance": {
          "type": "long"
        },
        "city": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "email": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "employer": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "firstname": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "gender": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "lastname": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "state": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        }
      }
    }
  }
}
```

这些字段的mapping都是批量导入数据时es猜测的类型。

**子属性**：

注意 address的mapping

```json
"address": {
	"type": "text",
	"fields": {
		"keyword": {
			"type": "keyword",
			"ignore_above": 256
		}	
	}
},
```

address的类型为 text，因此可以通过全文检索搜索这个字段，这个字段同时有一个子属性 keyword ,类型为keyword，如果通过address.keyword 查询就是一个keyword类型查询，是精确匹配的。es会自动给每个字符串默认为 text，并且生成一个keyword子属性，用于做精确匹配检索。



#### 2.2 创建mapping

https://www.elastic.co/guide/en/elasticsearch/reference/current/explicit-mapping.html

创建索引，指定mapping

```json
PUT /my-index-000001
{
  "mappings": {
    "properties": {
      "age":    { "type": "integer" },  
      "email":  { "type": "keyword"  }, 
      "name":   { "type": "text"  }     
    }
  }
}
```

email 类型为 keyword，不会进行全文检索，只会精确匹配

name是text类型，保存数据的时候进行分词，检索的时候按照分词进行匹配，做全文检索

运行上述指令后就创建了索引，并指定了字段的类型，也就是指定了数据的映射关系mapping



#### 2.3 修改 mapping

##### 2.3.1 添加字段

对已存在的mapping中添加新的字段：

```json
PUT /my-index-000001/_mapping
{
  "properties": {
    "employee-id": {
      "type": "keyword",
      "index": false
    }
  }
}
```

"index": false 表示不需要被索引，默认所有字段的index都是ture

https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-index.html

index为true，es会额外创建数据用于高效检索。字段不需要被检索可以设置为false来节省空间



##### 2.3.2 更改已有字段的mapping

对于已经存在的mapping是不能更新的，如果确实需要更新字段类型，则需要做数据迁移



##### 2.3.3 数据迁移

上面已经查询了 bank-account 的mapping， 现在假设需要将 age 改为 integer，就需要先创建一个新的索引，指定mapping，设置age为integer，然后再将旧数据导入新的索引。

1. 创建新的索引（指定mapping）

```json
put /bank-account-v2
{
  "mappings": {
    "properties": {
      "account_number": {
        "type": "long"
      },
      "address": {
        "type": "text"
      },
      "age": {
        "type": "integer"
      },
      "balance": {
        "type": "long"
      },
      "city": {
        "type": "keyword"
      },
      "email": {
        "type": "keyword"
      },
      "employer": {
        "type": "keyword"
      },
      "firstname": {
        "type": "text"
      },
      "gender": {
        "type": "keyword"
      },
      "lastname": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "state": {
        "type": "keyword"
      }
    }
  }
}
```

修改了各个text字段的类型，需要精确匹配的改为keyword，需要全文检索的删除了keyword子字段



2. 导入数据

```json
POST _reindex
{
  "source": {
    "index": "bank-account"
  },
  "dest": {
    "index": "bank-account-v2"
  }
}
```

*注意旧版本es中有type类型的，需要在source中指定type，type在新版本es中已经删除，不再支持。*



3. 验证

```json
GET /bank-account-v2/_search
{
  "query": {
    "match_all": {}
  }  
}
```

结果：

```json
{
  "took": 557,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 1000,
      "relation": "eq"
    },
    "max_score": 1,
    "hits": [
      {
        "_index": "bank-account-v2",
        "_id": "1",
        "_score": 1,
        "_source": {
          "account_number": 1,
          "balance": 39225,
          "firstname": "Amber",
          "lastname": "Duke",
          "age": 32,
          "gender": "M",
          "address": "880 Holmes Lane",
          "employer": "Pyrami",
          "email": "amberduke@pyrami.com",
          "city": "Brogan",
          "state": "IL"
        }
      },
      // ...
    ]
  }
}
```

说明数据已经迁移成功











