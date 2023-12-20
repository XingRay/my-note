## Elasticsearch的Java客户端

es官方支持的客户端列表

https://www.elastic.co/guide/en/elasticsearch/client/index.html

java客户端：

https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/index.html

spring-data-elasticsearch

https://spring.io/projects/spring-data-elasticsearch/



### 1.  引入依赖

gradle

```groovy
dependencies {
    implementation 'co.elastic.clients:elasticsearch-java:8.7.0'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.12.3'
}
```

maven

```xml
<dependencies>

    <dependency>
        <groupId>co.elastic.clients</groupId>
        <artifactId>elasticsearch-java</artifactId>
        <version>8.7.0</version>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.12.3</version>
    </dependency>

</dependencies>
```



项目的完整pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.0.5</version>
    </parent>

    <groupId>com.xingray</groupId>
    <artifactId>es-demo</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>19</maven.compiler.source>
        <maven.compiler.target>19</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>

        <dependency>
            <groupId>co.elastic.clients</groupId>
            <artifactId>elasticsearch-java</artifactId>
            <version>8.7.0</version>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.12.7.1</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.0</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>

</project>
```



### 3. 配置

https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/8.7/_basic_authentication.html#_basic_authentication

https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/connecting.html



在 resource下创建 elasticsearch.properties

```properties
elasticsearch.username=elastic
elasticsearch.password=NkJlhp03WQ0Yn9pnkkSm
elasticsearch.host=localhost
elasticsearch.port=9200
```

创建cofig包，在config目录下创建

```java
package com.xingray.demo.es.config;

import ...

@Configuration
@PropertySource("classpath:elasticsearch.properties")
public class ElasticSearchConfig {

    @Value("${elasticsearch.username}")
    private String esUsername;
    @Value("${elasticsearch.password}")
    private String esPassword;
    @Value("${elasticsearch.host}")
    private String esHost;
    @Value("${elasticsearch.port}")
    private Integer esPort;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esUsername, esPassword));
        RestClient restClient = RestClient.builder(new HttpHost(esHost, esPort))
                .setHttpClientConfigCallback(builder -> {
                    builder.disableAuthCaching();
                    return builder.setDefaultCredentialsProvider(credentialsProvider);
                })
                .build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
```

注意： 使用 @PropertySource("classpath:elasticsearch.properties") 注解引入配置文件，文件名前加上 classpath:  

使用@Value注解引入配置值，@Value("${elasticsearch.username}") 配置值的key要在 ${} 中



### 4. 测试

创建 po 包，添加pojo类

```java
package com.xingray.demo.es.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BankAccount {
    @JsonProperty("account_number")
    private Long accountNumber;
    @JsonProperty("address")
    private String address;
    @JsonProperty("age")
    private Integer age;
    @JsonProperty("balance")
    private Long balance;
    @JsonProperty("city")
    private String city;
    @JsonProperty("email")
    private String email;
    @JsonProperty("employer")
    private String employer;
    @JsonProperty("firstname")
    private String firstname;
    @JsonProperty("lastname")
    private String lastname;
    @JsonProperty("gender")
    private String gender;
    @JsonProperty("state")
    private String state;
}
```

这里使用lombok简化代码，es支持使用jackson做JSON转化



在测试目录下创建测试类 ElasticSearchClientTest

```java
package com.xingray.demo.es.test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.xingray.demo.es.po.BankAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class ElasticSearchClientTest {

    private final ElasticsearchClient client;

    @Autowired
    public ElasticSearchClientTest(ElasticsearchClient client) {
        this.client = client;
    }

    @Test
    public void testSearch01() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder().index("bank-account-v2").query(QueryBuilders.matchAll(builder -> builder)).build();
        SearchResponse<BankAccount> response = client.search(searchRequest, BankAccount.class);
        List<Hit<BankAccount>> hits = response.hits().hits();
        for(Hit<BankAccount> hit : hits){
            BankAccount bankAccount = hit.source();
            System.out.println(bankAccount);
        }
    }
}
```

执行测试得到结果：

```bash
2023-04-30T14:56:44.788+08:00  INFO 17756 --- [           main] c.x.d.es.test.ElasticSearchClientTest    : Starting ElasticSearchClientTest using Java 19.0.2 with PID 17756 (started by leixing in D:\code\workspace\demo\java\es-demo)
2023-04-30T14:56:44.789+08:00  INFO 17756 --- [           main] c.x.d.es.test.ElasticSearchClientTest    : No active profile set, falling back to 1 default profile: "default"
2023-04-30T14:56:45.861+08:00  INFO 17756 --- [           main] c.x.d.es.test.ElasticSearchClientTest    : Started ElasticSearchClientTest in 1.289 seconds (process running for 1.894)
BankAccount(accountNumber=1, address=880 Holmes Lane, age=32, balance=39225, city=Brogan, email=amberduke@pyrami.com, employer=Pyrami, firstname=Amber, lastname=Duke, gender=M, state=IL)
BankAccount(accountNumber=6, address=671 Bristol Street, age=36, balance=5686, city=Dante, email=hattiebond@netagy.com, employer=Netagy, firstname=Hattie, lastname=Bond, gender=M, state=TN)
BankAccount(accountNumber=13, address=789 Madison Street, age=28, balance=32838, city=Nogal, email=nanettebates@quility.com, employer=Quility, firstname=Nanette, lastname=Bates, gender=F, state=VA)
BankAccount(accountNumber=18, address=467 Hutchinson Court, age=33, balance=4180, city=Orick, email=daleadams@boink.com, employer=Boink, firstname=Dale, lastname=Adams, gender=M, state=MD)
BankAccount(accountNumber=20, address=282 Kings Place, age=36, balance=16418, city=Ribera, email=elinorratliff@scentric.com, employer=Scentric, firstname=Elinor, lastname=Ratliff, gender=M, state=WA)
BankAccount(accountNumber=25, address=171 Putnam Avenue, age=39, balance=40540, city=Nicholson, email=virginiaayala@filodyne.com, employer=Filodyne, firstname=Virginia, lastname=Ayala, gender=F, state=PA)
BankAccount(accountNumber=32, address=702 Quentin Street, age=34, balance=48086, city=Veguita, email=dillardmcpherson@quailcom.com, employer=Quailcom, firstname=Dillard, lastname=Mcpherson, gender=F, state=IN)
BankAccount(accountNumber=37, address=826 Fillmore Place, age=39, balance=18612, city=Tooleville, email=mcgeemooney@reversus.com, employer=Reversus, firstname=Mcgee, lastname=Mooney, gender=M, state=OK)
BankAccount(accountNumber=44, address=502 Baycliff Terrace, age=37, balance=34487, city=Yardville, email=aureliaharding@orbalix.com, employer=Orbalix, firstname=Aurelia, lastname=Harding, gender=M, state=DE)
BankAccount(accountNumber=49, address=451 Humboldt Street, age=23, balance=29104, city=Sunriver, email=fultonholt@anocha.com, employer=Anocha, firstname=Fulton, lastname=Holt, gender=F, state=RI)
```



### 6. 对文档进行操作

创建对象类

```java
package com.xingray.demo.es.test.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String sku;
    private String name;
    private Double price;
}
```



#### 6.1 插入数据

向es中插入数据，如果index不存在会自动创建

执行测试：

```java
	@Test
    public void indexTest01() throws IOException {
        Product product = new Product("bk-1", "City bike", 123.0);

        IndexResponse response = client.index(i -> i
                .index("products")
                .id(product.getSku())
                .document(product)
        );

        System.out.println("Indexed with version " + response.version());
    }
```

在es中创建索引并存入一个product对象，查询一下：

```json
GET /products/_search
{
  "query": {
    "match_all": {}
  }
}
```

返回

```json
{
  "took": 0,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 1,
      "relation": "eq"
    },
    "max_score": 1,
    "hits": [
      {
        "_index": "products",
        "_id": "bk-1",
        "_score": 1,
        "_source": {
          "sku": "bk-1",
          "name": "City bike",
          "price": 123
        }
      }
    ]
  }
}
```



#### 6.2 更新数据

```java
	@Test
    public void indexUpdateTest() throws IOException {
        Product product = new Product("bk-1", "new City bike", 150.0);

        IndexResponse response = client.index(i -> i
                .index("products")
                .id(product.getSku())
                .document(product)
        );

        System.out.println("Indexed with version " + response.version());
    }
```

这里 id 值为 product.getSku() 任然是 "bk-1" ，更新了 name 和 price

搜索结果：

```json
{
  "took": 467,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 1,
      "relation": "eq"
    },
    "max_score": 1,
    "hits": [
      {
        "_index": "products",
        "_id": "bk-1",
        "_score": 1,
        "_source": {
          "sku": "bk-1",
          "name": "new City bike",
          "price": 150
        }
      }
    ]
  }
}
```



#### 6.3 删除数据

根据id删除

```
	@Test
    public void deleteDocumentTest() throws IOException {
        Product product = new Product("bk-1", "new City bike", 150.0);

        DeleteResponse response = client.delete(builder -> {
            builder.index("products")
                    .id(product.getSku());
            return builder;
        });

        System.out.println(response);
    }
```

返回：

```bash
DeleteResponse: {"_id":"bk-1","_index":"products","_primary_term":1,"result":"deleted","_seq_no":2,"_shards":{"failed":0.0,"successful":1.0,"total":2.0},"_version":3}
```



#### 6.4 查看数据

等价于请求：

```json
GET /products/_search
{
  "query": {
    "match_all": {}
  }
}
```

java代码实现：

```java
	@Test
    public void queryDocumentTest() throws IOException {
        SearchResponse<Product> searchResponse = client.search(search -> {
            search.index("products");
            search.query(query -> query.matchAll(matchAll -> matchAll));
            return search;
        }, Product.class);

        System.out.println(searchResponse);
    }
```

结果：

```bash
SearchResponse: {"took":0,"timed_out":false,"_shards":{"failed":0.0,"successful":1.0,"total":1.0,"skipped":0.0},"hits":{"total":{"relation":"eq","value":1},"hits":[{"_index":"products","_id":"bk-1","_score":1.0,"_source":"Product(sku=bk-1, name=new City bike, price=150.0)"}],"max_score":1.0}}
```



### 7. 检索



### 8.聚合

如下：

```
GET /bank-account-v2/_search
{
  "query": {
    "match_all": {}
  },
  
  "size": 0, 
  
  "aggs": {
    "ageAgg":{
      "terms": {
        "field": "age",
        "size": 10
      }
    },
    "ageAvg":{
      "avg": {
        "field": "age"
      }
    },
    "balanceAvg":{
      "avg": {
        "field": "balance"
      }
    }
  }
}
```

java代码实现：

```java
	@Test
    public void mappingCreate() throws IOException {
        SearchResponse<BankAccount> searchResponse = client.search(search ->
                        search.index("bank-account-v2")
                                .query(query -> query.matchAll(matchAll -> matchAll))
                                .aggregations("ageAgg", ageAgg ->
                                        ageAgg.terms(terms ->
                                                terms.field("age")))
                                .aggregations("ageAvg", ageAvg -> {
                                    return ageAvg.avg(avg ->
                                            avg.field("age"));
                                })
                                .aggregations("balanceAvg", balanceAvg ->
                                        balanceAvg.avg(avg ->
                                                avg.field("balance"))),
                BankAccount.class);
        System.out.println(searchResponse);
    }
```

返回：

```json
{
	"_shards":{
		"total":1.0,
		"failed":0.0,
		"successful":1.0,
		"skipped":0.0
	},
	"hits":{
		"hits":[
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=1, address=880 Holmes Lane, age=32, balance=39225, city=Brogan, email=amberduke@pyrami.com, employer=Pyrami, firstname=Amber, lastname=Duke, gender=M, state=IL)",
				"_id":"1",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=6, address=671 Bristol Street, age=36, balance=5686, city=Dante, email=hattiebond@netagy.com, employer=Netagy, firstname=Hattie, lastname=Bond, gender=M, state=TN)",
				"_id":"6",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=13, address=789 Madison Street, age=28, balance=32838, city=Nogal, email=nanettebates@quility.com, employer=Quility, firstname=Nanette, lastname=Bates, gender=F, state=VA)",
				"_id":"13",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=18, address=467 Hutchinson Court, age=33, balance=4180, city=Orick, email=daleadams@boink.com, employer=Boink, firstname=Dale, lastname=Adams, gender=M, state=MD)",
				"_id":"18",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=20, address=282 Kings Place, age=36, balance=16418, city=Ribera, email=elinorratliff@scentric.com, employer=Scentric, firstname=Elinor, lastname=Ratliff, gender=M, state=WA)",
				"_id":"20",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=25, address=171 Putnam Avenue, age=39, balance=40540, city=Nicholson, email=virginiaayala@filodyne.com, employer=Filodyne, firstname=Virginia, lastname=Ayala, gender=F, state=PA)",
				"_id":"25",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=32, address=702 Quentin Street, age=34, balance=48086, city=Veguita, email=dillardmcpherson@quailcom.com, employer=Quailcom, firstname=Dillard, lastname=Mcpherson, gender=F, state=IN)",
				"_id":"32",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=37, address=826 Fillmore Place, age=39, balance=18612, city=Tooleville, email=mcgeemooney@reversus.com, employer=Reversus, firstname=Mcgee, lastname=Mooney, gender=M, state=OK)",
				"_id":"37",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=44, address=502 Baycliff Terrace, age=37, balance=34487, city=Yardville, email=aureliaharding@orbalix.com, employer=Orbalix, firstname=Aurelia, lastname=Harding, gender=M, state=DE)",
				"_id":"44",
				"_score":1.0
			},
			{
				"_index":"bank-account-v2",
				"_source":"BankAccount(accountNumber=49, address=451 Humboldt Street, age=23, balance=29104, city=Sunriver, email=fultonholt@anocha.com, employer=Anocha, firstname=Fulton, lastname=Holt, gender=F, state=RI)",
				"_id":"49",
				"_score":1.0
			}
		],
		"total":{
			"value":1000,
			"relation":"eq"
		},
		"max_score":1.0
	},
	"took":2,
	"timed_out":false,
	"aggregations":{
		"lterms#ageAgg":{
			"doc_count_error_upper_bound":0,
			"sum_other_doc_count":463,
			"buckets":[
				{
					"doc_count":61,
					"key":31
				},
				{
					"doc_count":60,
					"key":39
				},
				{
					"doc_count":59,
					"key":26
				},
				{
					"doc_count":52,
					"key":32
				},
				{
					"doc_count":52,
					"key":35
				},
				{
					"doc_count":52,
					"key":36
				},
				{
					"doc_count":51,
					"key":22
				},
				{
					"doc_count":51,
					"key":28
				},
				{
					"doc_count":50,
					"key":33
				},
				{
					"doc_count":49,
					"key":34
				}
			]
		},
		"avg#ageAvg":{
			"value":30.171
		},
		"avg#balanceAvg":{
			"value":25714.837
		}
	}
}
```





附：

elasticsearch.properties：

```properties
elasticsearch.username=elastic
elasticsearch.password=NkJlhp03WQ0Yn9pnkkSm
elasticsearch.host=localhost
elasticsearch.port=9200
```



Config：

```java
@PropertySource("classpath:elasticsearch.properties")
public class ElasticSearchConfig {

    @Value("${elasticsearch.username}")
    private String esUsername;
    @Value("${elasticsearch.password}")
    private String esPassword;
    @Value("${elasticsearch.host}")
    private String esHost;
    @Value("${elasticsearch.port}")
    private Integer esPort;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esUsername, esPassword));
        RestClient restClient = RestClient.builder(new HttpHost(esHost, esPort))
                .setHttpClientConfigCallback(builder -> {
                    builder.disableAuthCaching();
                    return builder.setDefaultCredentialsProvider(credentialsProvider);
                })
                .build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
```



Test：

```java
@SpringBootTest
public class ElasticSearchClientTest {

    private final ElasticsearchClient client;

    @Autowired
    public ElasticSearchClientTest(ElasticsearchClient client) {
        this.client = client;
    }

    @Test
    public void testSearch01() throws IOException {
        SearchRequest searchRequest = new SearchRequest.Builder().index("bank-account-v2").query(QueryBuilders.matchAll(builder -> builder)).build();
        SearchResponse<BankAccount> response = client.search(searchRequest, BankAccount.class);
        List<Hit<BankAccount>> hits = response.hits().hits();
        for (Hit<BankAccount> hit : hits) {
            BankAccount bankAccount = hit.source();
            System.out.println(bankAccount);
        }
    }

    @Test
    public void indexTest01() throws IOException {
        Product product = new Product("bk-1", "City bike", 123.0);

        IndexResponse response = client.index(i -> i
                .index("products")
                .id(product.getSku())
                .document(product)
        );

        System.out.println("Indexed with version " + response.version());
    }

    @Test
    public void indexUpdateTest() throws IOException {
        Product product = new Product("bk-1", "new City bike", 150.0);

        IndexResponse response = client.index(i -> i
                .index("products")
                .id(product.getSku())
                .document(product)
        );

        System.out.println("Indexed with version " + response.version());
    }

    @Test
    public void deleteDocumentTest() throws IOException {
        Product product = new Product("bk-1", "new City bike", 150.0);

        DeleteResponse response = client.delete(builder -> {
            builder.index("products")
                    .id(product.getSku());
            return builder;
        });

        System.out.println(response);
    }

    @Test
    public void queryDocumentTest() throws IOException {
        SearchResponse<Product> searchResponse = client.search(search -> {
            search.index("products");
            search.query(query -> query.matchAll(matchAll -> matchAll));
            return search;
        }, Product.class);

        System.out.println(searchResponse);
    }

    @Test
    public void mappingCreate() throws IOException {
        SearchResponse<BankAccount> searchResponse = client.search(search ->
                        search.index("bank-account-v2").from(0).size(10)
                                .query(query -> query.matchAll(matchAll -> matchAll))
                                .aggregations("ageAgg", ageAgg ->
                                        ageAgg.terms(terms ->
                                                terms.field("age")))
                                .aggregations("ageAvg", ageAvg -> {
                                    return ageAvg.avg(avg ->
                                            avg.field("age"));
                                })
                                .aggregations("balanceAvg", balanceAvg ->
                                        balanceAvg.avg(avg ->
                                                avg.field("balance"))),
                BankAccount.class);
        System.out.println(searchResponse);
    }
}
```

