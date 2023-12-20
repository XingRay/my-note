## SpringDataElasticsearch的使用

https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#preface

### 1. 版本

| lib                       | version |
| ------------------------- | ------- |
| springboot                | 3.0.5   |
| spring-data-elasticsearch | 5.0.5   |
| elasticsearch-client      | 8.5.3   |
| elasticsearch             | 8.7.0   |



### 2. 项目初始化

pom.xml

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

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.data</groupId>
                <artifactId>spring-data-bom</artifactId>
                <version>2022.0.5</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>

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

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
        </dependency>

    </dependencies>

</project>

```



application.yml 设置es相关参数

```yaml
server:
  port: 8080

spring:
  application:
    name: es-demo
  elasticsearch:
    username: elastic
    password: NkJlhp03WQ0Yn9pnkkSm
    uris: localhost:9200
```



### 3. 创建测试相关类

创建po类

```java
package com.xingray.demo.es.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "product")
public class Product {

    @Id
    private String sku;

    @Field(type = FieldType.Text, store = true)
    private String name;

    @Field(type = FieldType.Double, store = true)
    private Double price;
}
```

创建runner确保索引和映射关系被创建

```java
package com.xingray.demo.es.config;

import com.xingray.demo.es.po.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EsApplicationRunner implements ApplicationRunner {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IndexCoordinates indexCoordinates = IndexCoordinates.of("product");
        IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);
        if (!indexOperations.exists()) {
            indexOperations.create();
            indexOperations.refresh();

            indexOperations.putMapping(Product.class);
            indexOperations.refresh();

            log.info("创建索引成功");
        }
    }
}
```



声明repo接口

```java
package com.xingray.demo.es.repository;


import com.xingray.demo.es.po.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductRepository extends ElasticsearchRepository<Product, String> {

}
```



测试

```java
package com.xingray.demo.es.test;

import com.xingray.demo.es.po.Product;
import com.xingray.demo.es.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
public class SpringDataElasticTest {

    private final ProductRepository productRepository;

    @Autowired
    public SpringDataElasticTest(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Test
    public void saveTest() {
        Product product = new Product("product-001", "product-name-001", 120.0);
        Product savedProduct = productRepository.save(product);
        System.out.println(savedProduct);
    }

    @Test
    public void findByIdTest() {
        Optional<Product> productOptional = productRepository.findById("product-001");
        productOptional.ifPresent(System.out::println);
    }

    @Test
    public void deleteByIdTest(){
        productRepository.deleteById("product-001");
    }

    @Test
    public void queryTest(){
        Iterable<Product> all = productRepository.findAll();
        all.forEach(System.out::println);
    }
}
```





测试类2

```java
package com.xingray.demo.es.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "bank-account-v2")
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount {
    @JsonProperty("account_number")
    @Id
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

repository

根据jpa的规则，按照规则声明接口会自动实现该方法：

https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/#elasticsearch.repositories.autocreation



```json
package com.xingray.demo.es.repository;

import com.xingray.demo.es.po.BankAccount;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BankAccountRepository extends ElasticsearchRepository<BankAccount, Long> {
    Iterable<BankAccount> findByAddressLike(String address);
}
```

使用 @Query

```java
package com.xingray.demo.es.repository;

import com.xingray.demo.es.po.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BankAccountRepository extends ElasticsearchRepository<BankAccount, Long> {
    Iterable<BankAccount> findByAddressLike(String address);

    @Query("""
            {
                "term": {
                       "age": {
                         "value": ?0
                       }
                }
            }

            """)
    Page<BankAccount> findByAge(Integer age, Pageable pageable);
}
```

自定义`Repository`中使用`@Query`注解时，直接从语法中**`query`之后的内容开始写**，直接从`kibana`中复制的语句会导致请求失败

注意分页请求方法的参数必须包含Pageable， 测试方法：

```java
@Test
public void queryAnnotationTest(){
    PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "balance"));
    Page<BankAccount> page = bankAccountRepository.findByAge(38, pageRequest);
    page.getContent().forEach(System.out::println);
}
```



### 4. 项目实战

在电商项目中，需要保存以下 index/mapping

```json
put /gulimall-search-sku
{
  "mappings": {
    "properties": {
      "sku_id": {
        "type": "long"
      },
      "spu_id": {
        "type": "keyword"
      },
      "brand_id": {
        "type": "long"
      },
      "brand_name": {
        "type": "keyword",
        "index": false,
        "doc_values": false
      },
      "brand_image_url": {
        "type": "keyword",
        "index": false,
        "doc_values": false
      },
      "category_id": {
        "type": "long"
      },
      "category_name": {
        "type": "keyword",
        "index": false,
        "doc_values": false
      },
      "sku_title": {
        "type": "text",
        "analyzer": "ik_smart"
      },
      "sku_price": {
        "type": "keyword"
      },
      "sku_image_url": {
        "type": "keyword",
        "index": false,
        "doc_values": false
      },
      "sale_count": {
        "type": "long"
      },
      "has_stock": {
        "type": "boolean"
      },
      "hot_score": {
        "type": "long"
      },
      "attrs": {
        "type": "nested",
        "properties": {
          "attr_id": {
            "type": "long"
          },
          "attr_name": {
            "type": "keyword",
            "index": false,
            "doc_values": false
          },
          "attr_value": {
            "type": "keyword"
          }
        }
      }
    }
  }
}
```



可以定义java对象：

```java
@Data
@Document(indexName = "demo-search-sku", writeTypeHint = WriteTypeHint.FALSE)
public class SkuEntity {
   
    @Id
    @Field(value = "sku_id", type = FieldType.Long)
    private Long skuId;

    @Field(value = "spu_id", type = FieldType.Long)
    private Long spuId;

    @Field(value = "brand_id", type = FieldType.Long)
    private Long brandId;

    @Field(value = "brand_name", type = FieldType.Keyword, index = false, docValues = false)
    private String brandName;

    @Field(value = "brand_image_url", type = FieldType.Keyword, index = false, docValues = false)
    private String brandImageUrl;

    @Field(value = "category_id", type = FieldType.Long)
    private Long categoryId;

    @Field(value = "category_name", type = FieldType.Keyword, index = false, docValues = false)
    private String categoryName;

    @Field(value = "sku_title", type = FieldType.Text, analyzer = "ik_smart")
    private String skuTitle;

    @Field(value = "sku_price", type = FieldType.Keyword)
    private BigDecimal skuPrice;

    @Field(value = "sku_image_url",type = FieldType.Keyword, index = false, docValues = false)
    private String skuImageUrl;

    @Field(value = "sale_count",type = FieldType.Long)
    private Long saleCount;

    @Field(value = "has_stock", type = FieldType.Boolean)
    private Boolean hasStock;

    @Field(value = "hot_score", type = FieldType.Long)
    private Long hotScore;

    @Field(value = "attrs", type = FieldType.Nested)
    private List<AttrValueEntity> attrs;
}
```



```java
@Data
public class AttrValueEntity {

    @Field(value = "attr_id", type = FieldType.Long)
    private Long attrId;


    @Field(value = "attr_name", type = FieldType.Keyword, index = false, docValues = false)
    private String attrName;


    @Field(value = "attr_value", type = FieldType.Keyword)
    private String attrValue;
}
```



使用spring-data-elasticsearch 保存数据必须要 @Id， 否则保存时会提示：

```bash
No id property found for class com.xingray.demo.es.modules.product.entity.SkuEntity!
```

`@Id` 的字段类型强制为 `keyword`
     

默认情况下 spring-data-es 会自动给数据 _source 中添加一个字段

```json
"_class": "com.xingray.demo.es.modules.product.entity.SkuEntity"
```

保存数据时会默认额外保存全类名，保存数据后再移动类，即全类名改变，但是字段不变，不影响读取（当前的类的全类名与保存时不一致不影响数据读取），如果不想要保存全类名，可以设置 

```java
writeTypeHint = WriteTypeHint.FALSE
```



注意：项目中使用了`ElasticsearchRepository`时，会在**项目启动时**构建`Repository`对象时检测`Entity`上的 `@Document`注解，如果注解的 `createIndex`为`true`(默认值为`true`)时，会检查es上是否存在mapping， 如果es上不存在mapping则会根据字段的注解生成mapping，并提交到es。



现象：

项目启动后，在es上删除index/mapping，再保存数据，mapping会与@Field注解定义的不一致。

在es上删除index/mapping，再启动单元测试保存数据则mapping不会出错，与@Field注解定义一致。

原因：**项目启动后**在es上删除mapping或者index，则spring不会自动再根据entity注解生成mapping，spring只是在启动项目时对mapping做检查，在后续在提交数据时，spring将不再检查是否存在mapping， es如果不存在index/mapping，会根据数据自动创建index/mapping，es自动创建的mapping很可能与entity的@Field注解定义的不一致，并且es自动创建mapping后，Spring检测到es存在mapping则不会再创建mapping提交到es。

注意：**如果在es上删除了mapping，建议重启Spring应用，让应用程序自动创建mapping并提交到es**。



自主创建mapping并提交es：

```java
Document document = SkuEntity.class.getAnnotation(Document.class);
IndexCoordinates indexCoordinates = IndexCoordinates.of(document.indexName());
IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);
indexOperations.putMapping(SkuEntity.class);
```

 

mapping中，如：

```json
"sku_image_url": {
	"type": "keyword",
	"index": false,
	"doc_values": false
},
```

或者

```java
@Field(value = "sku_image_url",type = FieldType.Keyword, index = false, docValues = false)
private String skuImageUrl;
```

index： false  表示该字段不能被索引

doc_values： false 表示该字段不能用于聚合，统计

设置为false可以避免es为该字段创建额外的数据，节省内存空间。



附：

Repository

```java
package com.xingray.demo.es.modules.product.repository;


import com.xingray.demo.es.modules.product.entity.SkuEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SkuRepository extends ElasticsearchRepository<SkuEntity, Long> {
}
```



Service

```java
public interface SkuService {
    void saveList(List<SkuEntity> skuEntityList);

    void save(SkuEntity skuEntity);

    Iterable<SkuEntity> findAll();
}
```



ServiceImpl

```java
@Service
public class SkuServiceImpl implements SkuService {

    private final SkuRepository skuRepository;

    public SkuServiceImpl(SkuRepository skuRepository) {
        this.skuRepository = skuRepository;
    }

    @Override
    public void saveList(List<SkuEntity> skuEntityList) {
        skuRepository.saveAll(skuEntityList);
    }

    @Override
    public void save(SkuEntity skuEntity) {
        skuRepository.save(skuEntity);
    }

    @Override
    public Iterable<SkuEntity> findAll() {
        return skuRepository.findAll();
    }
}
```



Test：

```java
@SpringBootTest
public class SkuServiceTest {

    private final SkuService skuService;

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public SkuServiceTest(SkuService skuService, ElasticsearchOperations elasticsearchOperations) {
        this.skuService = skuService;
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Test
    public void saveTest() {
        SkuEntity skuEntity = new SkuEntity();
        skuEntity.setSkuId(101L);
        skuEntity.setSkuTitle("sku-title-101");
        skuEntity.setSkuPrice(BigDecimal.valueOf(101));


        skuService.save(skuEntity);
    }

    @Test
    public void findAllTest() {
        Iterable<SkuEntity> skuEntityIterable = skuService.findAll();
        skuEntityIterable.forEach(System.out::println);
    }

    /**
     * 可以使用这个方法创建index和mapping
     */
    @Test
    public void createMappingTest() {
        Document document = SkuEntity.class.getAnnotation(Document.class);
        IndexCoordinates indexCoordinates = IndexCoordinates.of(document.indexName());
        IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);
        indexOperations.putMapping(SkuEntity.class);
    }
}
```

