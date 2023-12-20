## thymeleaf入门

https://www.thymeleaf.org/

https://www.thymeleaf.org/documentation.html

https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.pdf

### 1. 引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```



### 2.配置

```yaml
spring:
  thymeleaf:
    cache: false
  mvc:
    static-path-pattern: /static/**
```

开发期间禁用缓存，这样可以看到实时效果

thymeleaf默认设置：

```java
public static final String DEFAULT_PREFIX = "classpath:/templates/";
public static final String DEFAULT_SUFFIX = ".html";
```

在resource中的 templates/ 目录下的所有 .html 文件为thymeleaf的模板，所有的controller(不能使 RestController)返回的是字符串，不是JSON ，视图解析器就会去 resource/template/xx.html中渲染模板



在templates目录下放置 index.html

在static目录下放置，完成后的目录结构如下：

```bash
└───resources
    │   application.yml
    │   bootstrap.yml
    │
    ├───mapper
    │   └───product
    │           AttrAttrgroupRelationDao.xml
    │
    ├───static
    │   └───index
    │       ├───css
    │       │       GL.css
    │       │
    │       ├───img
    │       │       01.png
    │       │
    │       ├───js
    │       │       catalogLoader.js
    │       │
    │       └───json
    │               catalog.json
    │
    └───templates
            index.html
```

启动服务访问指定端口即可。

springboot做了默认配置，访问端口自动转到index，更多默认配置可以查看 

```java
org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
```

```
org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties
```

```
org.springframework.boot.autoconfigure.web.WebProperties.Resources
```

```java
private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/META-INF/resources/",
				"classpath:/resources/", "classpath:/static/", "classpath:/public/" };
```



### 3. 开发页面

访问 http://localhost:30100/index.html 返回404，是因为没有处理这个请求，

在项目中创建web包，用于处理web页面的请求，创建IndexController, / 和 /index.html 都进入首页：

```java
@Controller
public class IndexController {

    private final CategoryService categoryService;

    public IndexController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping({"/", "index.html"})
    public String index(Model model) {
        List<CategoryEntity> categoryListByLevel = categoryService.getCategoryListByLevel(1);
        model.addAttribute("categoryListLevel1", categoryListByLevel);
        return "index";
    }
}

```

注意model的key最好使用驼峰，不要带有 `-` ，避免解析失败



在index.html页面中：

1 引入thymeleaf的命名空间

```xml
 xmlns:th="http://www.thymeleaf.org"
```

加到`<html>`标签上，`<html xmlns:th="http://www.thymeleaf.org">` 如：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
	<head>
		<title>Good Thymes Virtual Grocery</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" media="all" href="../../css/gtvg.css" th:href="@{/css/gtvg.css}" />
	</head>
	<body>
		<p th:text="#{home.welcome}">Welcome to our grocery store!</p>
	</body>
</html>
```

就可以在html页面中使用thymeleaf的标签了。



### 4. 取值表达式

`${}` 可以从 model 中取值

```html
<p>Today is: <span th:text="${today}">13 february 2011</span></p>
```



### 5. 遍历

`th:each` 可以遍历列表

如：

```html
<tr th:each="item : ${list}">
```

完整示例：

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
	<head>
		<title>Good Thymes Virtual Grocery</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" media="all"
			  href="../../../css/gtvg.css" th:href="@{/css/gtvg.css}" />
	</head>
	<body>
		<h1>Product list</h1>
		<table>
			<tr>
				<th>NAME</th>
				<th>PRICE</th>
				<th>IN STOCK</th>
			</tr>
			<tr th:each="prod : ${prods}">
				<td th:text="${prod.name}">Onions</td>
				<td th:text="${prod.price}">2.41</td>
				<td th:text="${prod.inStock}? #{true} : #{false}">yes</td>
			</tr>
		</table>
		<p>
			<a href="../home.html" th:href="@{/}">Return to home</a>
		</p>
	</body>
</html>
```



### 6. 自定义属性

使用 `th:attr="customKey=${custom.value}"`

```html
<input type="submit" value="Subscribe!" th:attr="value=${subscribe.submit}"/>
```

如：

```html
<div class="header_main_left">
	<ul>
		<li th:each=" category : ${categoryListLevel1}">
			<a href="#" class="header_main_left_a" th:attr=" categoryId=${category.catId}">
				<b th:text="${category.name}">家用电器</b>
			</a>
		</li>
	</ul>
</div>
```





### 6. 页面热更新

#### 6.1 引入dev-tools

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-devtools</artifactId>
	<optional>true</optional>
</dependency>
```

#### 6.2 刷新页面

修改页面 index.html 之后，点击idea的 Build->Recompile "index.html" 或者 Ctrl+Shift+F9 ，再在浏览器中刷新页面即可。

快捷键可以在idea的keymaps中修改

注意：一定要关掉thymeleaf的缓存才能热更新

```yaml
spring:
  thymeleaf:
    cache: false
```

修改了代码和配置，建议还是重启服务，避免异常问题。



### 7. 项目实战



```java
@Controller
@Slf4j
public class IndexController {

    private final CategoryService categoryService;

    public IndexController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping({"/", "index.html"})
    public String index(Model model) {
        List<CategoryEntity> categoryListByLevel = categoryService.getCategoryListByLevel(1);
        log.info("{}", StringUtil.toString(categoryListByLevel, "\n"));
        model.addAttribute("categoryListLevel1", categoryListByLevel);
        return "index";
    }
}
```

index.html

```html
<div class="header_main_left">
	<ul>
		<li th:each=" category : ${categoryListLevel1}">
			<a href="#" class="header_main_left_a" ctg-data="3">
				<b th:text="${category.name}">家用电器222</b>
			</a>
		</li>
	</ul>
</div>
```









