## Spring Boot 参数校验与异常处理



### 0. 从 Web 的参数接收说起

我们常用来获取 Web 参数的注解有以下三个：

| 注解          | 说明                                                         |
| ------------- | ------------------------------------------------------------ |
| @RequestParam | 获取 URL "?" 后所携带的参数，如：localhost:8080/user?id=1    |
| @PathVariable | 主要配合 RESTful 风格使用，可以获取 URL 路径上所包含的参数，如：localhost:8080/user/{id} |
| @RequestBody  | 用于接收请求体中的参数，常用于 `application/json` 类型的 `POST` 请求 |

本文分别以这三个注解为出发点，以参数校验、异常处理为主线，将一些相关的琐碎知识点串联起来。



### 1. 从 @RequestParam 出发

#### 1.1 required 校验非 null 引发的异常

`@RequestParam` 注解提供 `required` 属性来设置参数是否必需，默认值 `true` ，即无需特别注明 `required` 属性，在请求参数缺失时，就会抛出异常。

```java
java复制代码@GetMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> getUserInfo(@RequestParam String id) {
    // doSomething()
}
```

我们用 Postman 来测试上述代码，当不传递参数 id 时，将得到如下响应信息，状态码为 400 ：

![img](D:\myNote\resources\f8f4f153691249308053507210c3de90~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

在服务日志中，我们可以看到异常的提示信息：

![2.jpg](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/39d707c63c0c4767a026345108dede95~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp?)

## 1.2 异常提示信息 message 哪里去了？

部分小伙伴儿会遇到跟我相同的问题，异常的提示信息仅能在服务日志中看到，并没有包含在响应体中，那么 `message` 哪里去了？

这是由于 Spring Boot 版本造成的，查阅了 Spring Boot 的 [版本日志](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2Fspring-projects%2Fspring-boot%2Fwiki%2FSpring-Boot-2.5-Release-Notes) ，`2.5.x` 起，默认的异常响应信息中的 `message` 属性被移除了：

![3.jpg](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/42254070e34f49348c50468e77969c23~tplv-k3u1fbpfcp-zoom-in-crop-mark:4536:0:0:0.awebp?)

如果仍然希望异常响应时显示详细的提示信息，则需要增加如下配置：

```yaml
yaml复制代码server: 
  error: 
    include-message: always
```

有了上述配置，`message` 就回来了：

![4.jpg](D:\myNote\resources\65871d6b758b47d3ac999c4727e4459c~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

## 1.3 `server.error.include-` 还有其它配置么？

既然我们可以通过配置在异常响应体中增加 `message` ，那么还有什么其他可配置的信息么？

基于 `Spring Boot 2.7.8` ，将异常响应信息做如下梳理：

| 属性名    | 属性说明                     | 固定 / 可配置 | 配置项及默认值                                |
| --------- | ---------------------------- | ------------- | --------------------------------------------- |
| timestamp | 异常发生时的时间             | 固定          |                                               |
| status    | http 响应状态码              | 固定          |                                               |
| error     | 与状态码对应的异常原因       | 固定          |                                               |
| path      | 异常发生时的请求路径         | 固定          |                                               |
| message   | 异常的提示信息               | 可配置        | `server.error.include-message = never`        |
| exception | 异常的类名                   | 可配置        | `server.error.include-exception = false`      |
| trace     | 异常跟踪堆栈信息             | 可配置        | `server.error.include-stacktrace = never`     |
| errors    | `BindingResult` 中的错误信息 | 可配置        | `server.error.include-binding-errors = never` |

除了 `server.error.include-exception` 是布尔值外，其它三项配置可选值如下：

| 可选值   | 配置说明                                                     |
| -------- | ------------------------------------------------------------ |
| never    | 异常响应体中不会包含对应的信息                               |
| always   | 异常响应体中包含对应的信息                                   |
| on-param | 当请求参数中包含相应的参数名（`message`、`trace`、`errors`），且参数值不为 `false` 时，异常响应体中将包含对应的信息 |

`on-param` 的效果如下图：

![5.jpg](D:\myNote\resources\a7e6fc48a94c41b69a349b3a8fc42575~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

> **更多的细节与说明，可阅读相关源码：**
> [spring-boot-x.x.x.jar] org.springframework.boot.web.servlet.error.DefaultErrorAttributes
> [spring-boot-x.x.x.jar] org.springframework.boot.web.error.ErrorAttributeOptions
> [spring-boot-autoconfigure-x.x.x.jar] org.springframework.boot.autoconfigure.web.ErrorProperties

## 1.4 如何在异常响应体中添加自定义信息？

如何在上述 8 个属性的基础上，扩展自定义的信息呢？

- 创建一个类，继承 `org.springframework.boot.web.servlet.error.DefaultErrorAttributes` （此处需要特别注意基类的路径，容易错误引用为 `org.springframework.boot.web.reactive.error.DefaultErrorAttributes` ）；
- 通过 `@Component` 注解将类交托于 Spring 管理；
- 重写 `getErrorAttributes` 方法，调用基类同名方法，在获取的 `Map` 结果集中进行自定义信息的扩展。

```java
java复制代码@Component
public class MyErrorAttributes extends DefaultErrorAttributes {
	
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String,Object> map = super.getErrorAttributes(webRequest, options);
        map.put("system", "XXXX 系统");
        map.put("company", "XXXX 公司");
        return map;
    }

}
```

![6.jpg](D:\myNote\resources\6ee087256286419f8fae05fdc441df00~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

有点神奇是不是？为啥向 Spring 容器中加了个 Bean 就实现了？不会和原有的 `DefaultErrorAttributes` 冲突么？

我们可以在源码（ `[spring-boot-autoconfigure-x.x.x.jar] org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration` ）中找到答案：

```java
java复制代码@Bean
@ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
public DefaultErrorAttributes errorAttributes() {
    return new DefaultErrorAttributes();
}
```

`@ConditionalOnMissingBean` 注解表名了仅当 Spring 在缺失 `ErrorAttributes` 类型实例的情况下，才会创建一个 `DefaultErrorAttributes` 实例。当我们已经提供了一个继承于 `DefaultErrorAttributes` （该类实现了 `ErrorAttributes` 接口）的实例，默认的自然就不会创建了。

# 2. 从 @PathVariable 出发

## 2.1 迷惑的 required 属性

`@RequestParam` 的 `required` 可以帮助我们实现参数的非 null 校验，`@PathVariable` 注解同样提供了 `required` 属性（默认值也为 `true` ），我们当然期待它能有相同的表现，但事实却并非如此：

（1）当 URL 中的某一级路径完全作为参数的值时，不传递该参数，则会因为请求路径匹配失败而返回 404 错误，而并非参数校验失败：

```java
java复制代码@GetMapping(value = "item/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> getItemInfo1(@PathVariable String id) {
    // doSomething()
}
```

![7.jpg](D:\myNote\resources\f390e565f9114486bdf999ab5997feac~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

（2）当 URL 中的某一级路径，不仅仅由参数占位符组成，还包含一些其他的固定字符，此时不传递该参数，参数会被初始化为空字符串， `required` 校验同样没有成功：

```java
java复制代码@GetMapping(value = "item/i_{id}", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> getItemInfo2(@PathVariable String id) {
    // doSomething()
}
```

![8.jpg](D:\myNote\resources\2c732607bdfa45be842a409eb12d5927~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

## 2.2 手动判断非空并抛出异常

既然 `@PathVariable` 的 `required` 没有办法帮我们完成参数的校验，那我们只能自行通过代码实现了。

对于字符串的非空校验，有非常多的方法，下面列举出几种笔者常用的方式，当判定参数为空时，则手动抛出异常：

```java
java复制代码if (id != null && id.trim().length() != 0) {
    throw new IllegalArgumentException("参数 {id} 不能为空。");
}
if (!org.springframework.util.StringUtils.hasText(id)) {
    throw new IllegalArgumentException("参数 {id} 不能为空。");
}
if (!org.apache.commons.lang.StringUtils.isNotBlank(id)) {
    throw new IllegalArgumentException("参数 {id} 不能为空。");
}
```

![9.jpg](D:\myNote\resources\ca8925518cfa4dca888a501504d45e3e~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

## 2.3 能否更优雅一点？ => 使用断言

`if...throw...` 这样的代码，显得有些笨拙了，我们能否有更为优雅一点的方式呢？Spring 的 **断言（Assert）** 可以帮到我们。

所谓 “断言” ，就是断定某个实际的运行值和预想的值一样，若不一样则抛出异常。

先来看断言如何简化了我们的代码：

```java
java
复制代码Assert.hasText(id, "参数 {id} 不能为空。");
```

查看 `Assert` 的源码，不难发现，`if...throw...` 这样的逻辑代码，`Assert` 帮我们实现了，因此可以使我们的代码更加简洁。源码如下：

```java
java复制代码public static void hasText(@Nullable String text, String message) {
    if (!StringUtils.hasText(text)) {
        throw new IllegalArgumentException(message);
    }
}
```

`Assert` 常用的方法梳理如下：

| 方法名（参数列表）                                           | 方法作用                                                 |
| ------------------------------------------------------------ | -------------------------------------------------------- |
| isTrue(boolean, String / Supplier<String>)                   | 逻辑断言，如果条件为假则抛出 `IllegalArgumentException`  |
| state(boolean, String / Supplier<String>)                    | 同 `isTrue` ，但抛出的异常类型为 `IllegalStateException` |
| isNull(Object, String / Supplier<String>)                    | 假设对象不为 null                                        |
| notNull(Object, String / Supplier<String>)                   | 假设对象为 null                                          |
| isInstanceOf(Class<?>, Object, String / Supplier<String>)    | 假设对象实例为指定类型                                   |
| isAssignable(Class<?>, Class<?>, String / Supplier<String>)  | 假设类型为指定类型的子类或接口实现                       |
| hasLength(String, String / Supplier<String>)                 | 假设文本至少包含一个字符（可为空白字符）                 |
| hasText(String, String / Supplier<String>)                   | 假设文本至少包含一个非空白字符                           |
| doesNotContain(String, String / Supplier<String>)            | 假设文本不包含指定的文本片段                             |
| notEmpty(Object[] / Collection<?> / Map<?, ?>, String / Supplier<String>) | 假设数组、集合、Map 不为 null 且至少包含一个元素         |
| noNullElements(Object[] / Collection<?>, String / Supplier<String>) | 假设数组、集合本身不为 null ，且不包含为 null 的元素     |

# 3. 从 @RequestBody 出发

## 3.1 依然先看看 required 属性

`@RequestBody` 同样提供 `required` 属性，默认值 `true` ，与 `@RequestParam` 一样，能够校验参数是否为 null ：

```java
java复制代码@PostMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> addUser(@RequestBody UserDTO user) {
    // doSomething()
}
```

![10.jpg](D:\myNote\resources\4ddb938eebd54d539a42044e38065438~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

不过很显然此项校验没有太大的实际意义，因为 `@RequestBody` 常被我们用于接收前端的 JSON 数据并映射为后端的 Bean 对象，相比于校验 Bean 对象是否为 null ，我们更为关注的是 Bean 对象中的某些属性是否为 null 。

## 3.2 引入校验框架

为了实现 Bean 对象内部属性的校验，我们引入校验框架，增加如下依赖：

```xml
xml复制代码<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

我们可以看一下 `spring-boot-starter-validation` 的依赖关系：

![11.jpg](D:\myNote\resources\c283ce9b77494518a660d041e356faf5~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

- `jakarta.validation-api` 是 Bean Validation 的规范；
- `hibernate-validator` 是对 Bean Validation 规范的实现与扩展，我们所使用的校验功能主要就是 `hibernate-validator` 在起作用。

## 3.3 提供了哪些约束规则？

从源码中我们可以找到可使用的约束注解：

![12.jpg](D:\myNote\resources\b9b81c403b1c43d3a67d376bc4c60313~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

Bean Validation 标准注解整理如下：

| 分类         | 注解                                    | 作用说明                                                     |
| ------------ | --------------------------------------- | ------------------------------------------------------------ |
| 空值检查     | @Null / @NotNull                        | 只能 / 不能为 null                                           |
|              | @NotEmpty                               | 非 null ，且字符串和数组的 length 、Collection 和 Map 的 size 大于 0 |
|              | @NotBlank                               | 字符串不能为 null 且至少有一个非空字符                       |
| Boolean 检查 | @AssertTrue / @AssertFalse              | 值必须为 true / false                                        |
| 长度检查     | @Size(min, max)                         | 长度必须在 min 和 max 之间，作用于字符串 、Collection 、Map 、数组 |
| 日期检查     | @Past / @Future                         | 必须是一个过去的 / 将来的日期                                |
|              | @PastOrPresent / @FutureOrPresent       | 必须是一个过去或当前的 / 将来或当前的日期                    |
| 数值检查     | @Min(value) / @Max(value)               | 必须小于等于 / 大于等于指定数值                              |
|              | @DecimalMin(value) / @DecimalMax(value) | 必须小于等于 / 大于等于指定数值（支持作用于字符串）          |
|              | @Digits(integer, fraction)              | 必须为小数，且整数部分精度不能超过 integer ，小数部分精度不能超过 fraction |
|              | @Positive / @Negative                   | 必须为正数 / 负数                                            |
|              | @PositiveOrZero / @NegativeOrZero       | 必须为正数或0 / 负数或0                                      |
| 其它检查     | @Email                                  | 必须是电子邮箱地址                                           |
|              | @Pattern(regexp)                        | 必须符合正在表达式                                           |

将所需的约束注解，加在 Bean 对象的属性上：

```java
java复制代码@NotBlank(message = "账号不能为空")
@Size(max = 30, min = 4, message = "账号长度在 4 ~ 30 之间")
private String account;
```

## 3.4 使校验生效：添加 @Valid 或 @Validated

为了让约束生效，我们还需要在 `@RequestBody` 所接收的参数对象前，增加 `@Valid` 或 `@Validated` 注解：

```java
java复制代码@PostMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> addUser(@RequestBody @Valid UserDTO user) {
    // doSomething()
}
```

![13.jpg](D:\myNote\resources\ceb6b5ee80234022a98637001b6e1cee~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

可以看到，`@Valid`（或 `@Validated` ）校验未通过时，会抛出 `MethodArgumentNotValidException` 异常，响应状态码 400。

## 3.5 详细约束提示信息的花式玩法

上图中，`message` 仅会告知哪个 Bean 对象校验失败，以及有多少条约束规则没有校验通过，但并不会告知更多细节。

针对详细的约束提示信息，可以有以下几种玩法：

### 3.5.1 【玩法一】 include-binding-errors

- `@Valid`（或 `@Validated` ）注解参数对象
- 将 `server.error.include-binding-errors` 设置为 `always` 或 `on-param`

这样就可以将详细的约束提示信息通过 `errors` 返回。但这样需要前端额外对 `errors` 进行解析处理，并不友好。

![14.jpg](D:\myNote\resources\097c8adcb001477b8734aa02f27262b6~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

### 3.5.2 【玩法二】 BindingResult

- `@Valid`（或 `@Validated` ）注解参数对象
- 在所校验的参数对象后，紧跟一个 `BindingResult` 对象，用来接收校验结果
- 方法内部对 `BindingResult` 进行人工处理，可以遍历其中的提示信息进行拼接，作为方法的返回信息进行正常返回（即响应状态码 200 ）；也可以粗暴的将第一条提示信息以异常的形式抛出

```java
java复制代码@PostMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> addUser(@RequestBody @Valid UserDTO user, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        throw new IllegalArgumentException(bindingResult.getAllErrors().get(0).getDefaultMessage());
    }
    // doSomething()
}
```

![15.jpg](D:\myNote\resources\5aec4453f1c04bd981f70f5ebe439266~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

当增加了 `BindingResult` 对象后，校验失败时，程序将不再抛出异常，并继续执行。因此需要在方法内部对 `BindingResult` 人为干预。但很显然，这种方式会使代码变得更加笨重。

### 3.5.3 【玩法三】异常拦截器

- `@Valid`（或 `@Validated` ）注解参数对象
- 创建全局的拦截器类，使用 `@ControllerAdvice` 注解该类
- 创建 `MethodArgumentNotValidException` 异常的拦截处理方法，使用 `@ExceptionHandler` 注解该方法
- 通过方法参数 `MethodArgumentNotValidException` 可以获取到 `BindingResult` ，进而获取所有约束提示信息进行遍历与拼接

```java
java复制代码@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> methodArgumentNotValidExceptionHandler
            (HttpServletRequest request, MethodArgumentNotValidException e) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", System.currentTimeMillis());
        map.put("status", HttpStatus.BAD_REQUEST.value());
        map.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        map.put("path", request.getRequestURI());
        map.put("exception", MethodArgumentNotValidException.class);
		
        BindingResult result = e.getBindingResult();
        String message = result.getFieldErrors()
				.stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("；"));
        map.put("message", message);
        return map;
    }

}
```

![16.jpg](D:\myNote\resources\96cbaadcf0cc452885000586168a2834~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

使用异常拦截器可以对前端返回统一、规范的异常信息，如上面的代码，将 `MethodArgumentNotValidException` 统一处理为 BAD_REQUEST ，所返回的字段项也按照 Spring Boot 标准的异常信息进行模拟（当然，你也完全可以使其以 200 状态码返回，格式上也可以任意发挥）。

### 3.5.4 【玩法四】 “奇技淫巧”

- `@Valid`（不可以用 `@Validated` 替代）注解参数对象
- 在所校验的参数对象后，紧跟一个 `BindingResult` 对象，用来接收校验结果
- 方法内部对 `BindingResult` 不做任何处理
- Controller 类上使用 `@Validated` 注解（不可以用 `@Valid` 替代）

```java
java复制代码@RestController
@Validated
public class DemoController {

    @PostMapping(value = "user", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> addUser(@RequestBody @Valid UserDTO user, BindingResult bindingResult) {
        // doSomething()
    }
        
}
```

![17.jpg](D:\myNote\resources\b0d0f26c16f444af9996ebe0a20e6e23~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

校验未通过时，会抛出 `ConstraintViolationException` 异常，响应状态码 500 ，由校验框架完成多条约束提示信息的拼接，并通过 `message` 返回。

## 3.6 @Valid 与 @Validated 有啥区别？

`@Valid` 是标准的 JSR-303 规范（ Bean Validation 规范）注解，而 `@Validated` 是由 Spring 提供的对于 JSR-303 的一个变种，提供了分组功能（可以根据不同的分组采用不同的校验机制）。

`@Valid` 可以用在方法、字段、构造器和参数上， `@Validated` 只能用在类型、方法、参数上。

在大多数相对简单的校验场景中，这两个注解并没有太大的差别。

## 3.7 嵌套校验如何实现？

当 Bean 对象中嵌套了 Bean 对象，需要在内嵌的 Bean 属性上增加 `@Valid` 注解（只能是 `@Valid` ，`@Validated` 是无法作用在字段上的）。

```java
java复制代码public class UserDTO {
	
    @NotBlank(message = "账号不能为空")
    @Size(max = 30, min = 2, message = "账号长度在 2 ~ 30 之间")
    private String account;
	
    @Valid
    private CompanyDTO company;

}

public class CompanyDTO {
	
    @NotBlank(message = "单位名称不能为空")
    @Length(max = 30, min = 4, message = "单位名称长度在 4 ~ 30 之间")
    private String name;

}
```

![18.jpg](D:\myNote\resources\bbaeecebc8cc40ff971d6b81b9df023e~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

## 3.8 List 校验如何实现？

`@Valid` 作用的对象是 Java Bean ，而 `List<E>` 并不是 Java Bean ，因此直接使用 `@Valid` 去注解 `List<E>` 类型的参数，无法起到我们预想的校验效果。

有三种解决方式：

### 3.8.1 【方式一】又见 “奇技淫巧”

- `@Valid`（不可以用 `@Validated` 替代）注解 `List<E>` 对象
- `List<E>` 对象后，可以跟一个 `BindingResult` 对象，也可以不跟，并没有什么影响
- Controller 类上使用 `@Validated` 注解（不可以用 `@Valid` 替代）

```java
java复制代码@RestController
@Validated
public class DemoController {

    @PostMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> addUsers(@RequestBody @Valid List<UserDTO> list) {
        // doSomething()
    }
    
}
```

![19.jpg](D:\myNote\resources\cf9fb32a64e24770a36cbd252fbaa4f7~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

校验未通过时，由校验框架完成多条约束提示信息的拼接，并通过 `message` 返回，但 `message` 的拼接格式无法改变，且不会走统一的异常拦截器（拦截器拦截的是 `MethodArgumentNotValidException` ，而经过框架的干涉后，异常转变为 `ConstraintViolationException` ）。

### 3.8.2 【方式二】封装 ListWrapper

- 将 `List<E>` 作为成员属性，封装进 Java Bean 中，并使用 `@Valid` 注解该属性
- 封装类提供构造函数
- Controller 使用封装类接收前端入参，并使用 `@Valid`（或 `@Validated` ）注解该参数对象

```java
java复制代码public class ListWrapper<E> {
	
    @Valid
    private List<E> list;

    public ListWrapper() {
        super();
        this.list = new ArrayList<>();
    }

    public ListWrapper(List<E> list) {
        super();
        this.list = list;
    }

    // getter、setter

}
java复制代码@PostMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> addUsers(@RequestBody @Validated ListWrapper<UserDTO> listWrapper) {
    // doSomething()
}
```

![20.jpg](D:\myNote\resources\79b5378424a04d6db209dbc3ccc94dee~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

需要注意，由于对 `List<E>` 进行了封装，因此前端传参时也需要多出一层包装。

采用封装 ListWrapper 这种方式，框架依然抛出 `MethodArgumentNotValidException` 异常，因此可以被统一的异常拦截器拦截处理。只不过处理逻辑需要进一步细化，否则在多条数据校验不通过时，按原有的处理逻辑所生成的 `message` 就会像图中那样，未指明错误对应的数据，十分的不友好。

### 3.8.3 【方式三】自定义 List

- 自定义一个 `ValidList<E>` ，实现 `List<E>` 接口
- 将 `List<E>` 作为成员属性，封装进 Java Bean 中，并使用 `@Valid` 注解该属性
- 封装类提供构造函数
- 使用成员属性所对应的方法，对 `List<E>` 接口的方法一一进行重写实现
- Controller 使用封装类接收前端入参，并使用 `@Valid`（或 `@Validated` ）注解该参数对象

```java
java复制代码public class ValidList<E> implements List<E> {
	
    @Valid
    private List<E> list;

    public ValidList() {
        super();
        this.list = new ArrayList<>();
    }

    public ValidList(@Valid List<E> list) {
        super();
        this.list = list;
    }

    @Override
    public boolean add(E arg0) {
        return this.list.add(arg0);
    }

    // 此处省略其它需要重写的方法......

}
java复制代码@PostMapping(value = "users", produces = MediaType.APPLICATION_JSON_VALUE)
public Map<String, Object> addUsers(@RequestBody @Validated ValidList<UserDTO> list) {
    // doSomething()
}
```

![21.jpg](D:\myNote\resources\11e106afc83c4fa187c496fb9a076ec4~tplv-k3u1fbpfcp-zoom-in-crop-mark.awebp)

自定义的 List ，即是 Java Bean ，又具有 List 的特性，相比上一种封装的方式显得更为优雅一些，前端传参时也不需要额外的包装了。

不过使用自定义 List 这种方式的缺陷也很明显，就是无法获得详细的校验约束信息，体现在以下两点：

- 在参数列表中增加 `BindingResult` ，框架仍抛出异常，无法进入方法内部，得到 `BindingResult`
- 框架抛出的异常类型并不是 `MethodArgumentNotValidException` ，后端控制台实际类型为 `NotReadablePropertyException` ，前端响应所显示异常类型为 `IllegalStateException` ，无论哪一种，都不包含 `BindingResult` 信息

由此，若需要详细的校验约束信息，则不能采用自定义 List 这种方式。