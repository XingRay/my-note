# springboot获取properties属性值的多种方式总结

 更新时间：2022年03月21日 11:57:11  作者：zhongzunfa  

这篇文章主要介绍了springboot获取properties属性值的多种方式总结，具有很好的参考价值，希望对大家有所帮助。如有错误或未考虑完全的地方，望不吝赐教

**+**

##### 目录



## 获取properties属性值方式总结

spring boot 在多环境情况下我们需要根据不同的获取不一样的值， 我们会配置在不同的文件中,

**那么我们怎么获取配置的属性值呢！ 下面介绍几种用法。**



### 1. 除了默认配置在 application.properties的多环境中添加属性

我们会在application.properties 中激活不同方式选择下面的不同文件进行发布。

设置的激活参数：dev, test, prod

```
spring.profiles.active=prod``url.lm=editMessage``url.orgCode=``100120171116031838``url.ybd=http:``//www.test.com/sales/``url.PostUrl=/LmCpa/apply/applyInfo  
```

获取属性可以, 定义配置类：

```
@ConfigurationProperties``(prefix = ``"url"``)   ``public` `class`  `ManyEnvProperties{  ``  ``private` `String lm;  ``  ``private` `String orgCode;  ``  ``private` `String ybd;  ``  ``private` `String postUrl;  ``  ``// 省列getter setter 方法  ``}  
```



### 2. 使用之前在spring中加载的value值形式

```
@Component`  `public` `class` `ManyEnvProperties {  ``  ``@Value``(``"${url.lm}"``)  ``  ``private` `String lmPage;  ``  ``@Value``(``"${url.ybd}"``)  ``  ``private` `String sendYbdUrl;  ``  ``@Value``(``"${url.orgCode}"``)  ``  ``private` `String orgCode;  ``  ``@Value``(``"${url.PostUrl}"``)  ``  ``private` `String PostUrl;  ``  ``// 省列getter setter 方法  ``}  
```



### 3. 也可以使用springboot里面的Environment 直接取值

显示注入， 其次是在需要的地方获取值

```
@Autowired`  `private` `Environment env;  ``logger.info(``"===============》 "` `+ env.getProperty(``"url.lm"``));
```



### 4. 如果是自己新建的一个properties文件

```
@Component`  `@ConfigurationProperties``(prefix = ``"url"``)  ``@PropertySource``(``"classpath:/platform.properties"``)  ``public` `class` `PropertiesEnv {  ``  ``private` `String lm;  ``  ``private` `String orgCode;  ``  ``private` `String ybd;  ``  ``private` `String postUrl;``  ``// 省列getter setter 方法  ``} 
```



## 获取多个自定义属性值

使用@Value 注入每个自定义配置，当自定义配置的属性值过多时就比较麻烦了，这时通过springboot提供了基于类型安全的配置方法，通过@ConfigurationProperties将properties中的属性和一个bean的属性关联，从而实现类型安全的配置，



### 比如在application中自定义属性

```
note.author=yzh``note.name=china
```

可以通过

```
@ConfigurationProperties``(prefix=``"note"``)
```

需要注意的是自定义属性值的前缀统一为note才可以获取到对应的属性值.属性值名称要跟配置文件里面的名称对应起来

同时通过这种方法需要生成属性值的get/set 方法，否则获取不到对应的属性值 