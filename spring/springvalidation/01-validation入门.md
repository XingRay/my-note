## validation入门

基于`JSR303`规范实现参数校验



### 1. 引入依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```



### 2. 在entity上加上注解

```java
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空")
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "logo必须是一个合法的url地址")
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是英文字母")
	@NotEmpty
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0, message = "排序值必须大于等于0")
	@NotNull
	private Integer sort;

}
```

注意 

`@NotNull` 不能等于null

`@NotEmpty`  不能为null，也不能为'' 长度为0的空串

`@NotBlank` 不能为null，不能为''，也不能为 '   ' 完全由空白字符组成的字符串



### 3. 在controller方法上加上注解 @Valid

```java
@RequestMapping("/save")
//@RequiresPermissions("product:brand:save")
public R save(@Valid @RequestBody BrandEntity brand){
	brandService.save(brand);
	return R.ok();
}
```



### 4. 发送请求

通过api工具发送 http://localhost:88/api/product/brand/save 

```json
{
  "name": ""
}
```

返回400，需要将具体的错误信息返回需要配置



### 5. 配置返回错误信息

```yaml
server:
  error:
    include-binding-errors: always
```



### 6. 自定义错误返回信息

```java
@NotBlank(message = "品牌名不能为空")
private String name;
```



### 7. 获取校验结果

在被校验的字段后面紧跟上一个 `BindingResult` 对象就可以获取到校验的结果

```java
@RequestMapping("/save")
//@RequiresPermissions("product:brand:save")
public R save(@Valid @RequestBody BrandEntity brand, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(new Consumer<FieldError>() {
            @Override
            public void accept(FieldError fieldError) {
                //  错误提示信息
                String message = fieldError.getDefaultMessage();
                // 错误的字段
                String fieldName = fieldError.getField();

                errors.put(fieldName, message);
            }
        });
        return R.error(400, "提交数据不合法").put("data", errors);
    }
    brandService.save(brand);

    return R.ok();
}
```



### 8. 统一的异常处理

可以使用springmvc的ControllerAdvice/RestControllerAdvice

```java
@RestControllerAdvice(basePackages = "com.xingray.gulimall.product.controller")
@Slf4j
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现异常:{}, 异常类型:{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> errors = new HashMap<>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            //  错误提示信息
            String message = fieldError.getDefaultMessage();
            // 错误的字段
            String fieldName = fieldError.getField();
            errors.put(fieldName, message);
        });
        return R.error(BizError.INVALID_PARAM.getCode(), BizError.INVALID_PARAM.getMsg()).put("data", errors);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleValidException(Throwable e) {
        e.printStackTrace();
        log.error("出现异常:{}, 异常类型:{}", e.getMessage(), e.getClass());
        return R.error(BizError.UNKNOWN_ERROR.getCode(), BizError.UNKNOWN_ERROR.getMsg());
    }
}
```

在项目的common模块中定义统一错误码

```java
public enum BizError {
    UNKNOWN_ERROR(10000, "未知错误"),
    INVALID_PARAM(10001, "参数校验错误");
    private final int code;
    private final String msg;

    BizError(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
```

Controller中将BindingResult参数删除

```java
@RequestMapping("/save")
//@RequiresPermissions("product:brand:save")
public R save(@Valid @RequestBody BrandEntity brand/*, BindingResult bindingResult*/) {
    /*if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(new Consumer<FieldError>() {
                @Override
                public void accept(FieldError fieldError) {
                    //  错误提示信息
                    String message = fieldError.getDefaultMessage();
                    // 错误的字段
                    String fieldName = fieldError.getField();

                    errors.put(fieldName, message);
                }
            });
            return R.error(400, "提交数据不合法").put("data", errors);
        }*/
    brandService.save(brand);

    return R.ok();
}
```

删除注释的代码

```java
@RequestMapping("/save")
//@RequiresPermissions("product:brand:save")
public R save(@Valid @RequestBody BrandEntity brand) {
    brandService.save(brand);
    return R.ok();
}
```

此时再次发送请求可以返回统一的错误提示



### 9. 分组校验

比如对于Brand对象，创建时不能带有id，服务器/数据库会自动分配，更新时又必须要携带id。这时就需要使用分组校验。

#### 9.1 创建group接口

在common模块创建空的接口类，不需要实现

```java
public interface AddGroup {
}
```

```java
public interface UpdateGroup {
}
```

```java
public interface DeleteGroup {
}
```

#### 9.2 在被校验的entity类上加上`groups`注解参数

```java
/**
 * 品牌id
 */
@TableId
@NotNull(message = "必须指定品牌id", groups = {UpdateGroup.class, DeleteGroup.class})
@Null(message = "不能指定品牌id", groups = AddGroup.class)
private Long brandId;

/**
 * 品牌名
 */
@NotBlank(message = "品牌名不能为空", groups = {AddGroup.class})
private String name;
```



#### 9.3 在Controller方法中声明group

在controller的方法中使用Spring提供的`@Validated`注解代替 `@Valid`，参数为要声明的分组，可以声明多个

```java
/**
  * 保存
  */
@RequestMapping("/save")
//@RequiresPermissions("product:brand:save")
public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand) {
    brandService.save(brand);
    return R.ok();
}

/**
  * 修改
  */
@RequestMapping("/update")
//@RequiresPermissions("product:brand:update")
public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
    brandService.updateById(brand);
    return R.ok();
}
```

注意：在Controller方法的`@Validated(XxxGroup.class)` 内有Group参数的时候，entity中没有设置`groups`的校验条件是不生效的，要让校验条件生效就必须要给entity上的校验条件注解加上设置分组。

在Controller方法的`@Validated` 内没有Group参数的时候，entity中没有设置`groups`的校验条件是生效的，此时带有group参数的校验条件不生效。



### 10. 自定义校验

在某些场景下，基于预定义的校验条件注解不能满足要求，则需要自定义校验注解

#### 10.1 自定义校验注解

```java
@Documented
@Constraint(validatedBy = {})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface ListValue {

    // 出错信息
    String message() default "{com.xingray.gulimall.common.validate.constraint.ListValue.message}";

    // 支持分组校验
    Class<?>[] groups() default {};

    // 自定义负载信息
    Class<? extends Payload>[] payload() default {};

    int[] values() default {};

}
```

根据`JSR0303`规范，该注解必须有 message() groups() payload() 方法， 这里再额外定义了values()方法，作为此校验注解的功能，被验证的字段取值必须为设定的值之一。

在resources目录下创建文件 `ValidationMessages.properties`

```properties
com.xingray.gulimall.common.validate.constraint.ListValue.message=必须提交指定的值
```

这样系统会根据放法 String message() default "{com.xingray.gulimall.common.validate.constraint.ListValue.message}"; 设置message的默认值。

如果要指定语言，则可以创建 ValidationMessages_zh.properties ValidationMessages_en.properties 等不同语言版本的文件。



#### 10.2 自定义校验器

```java
package com.xingray.gulimall.common.validate.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> valueSet;

    @Override
    public void initialize(ListValue constraintAnnotation) {
        this.valueSet = new HashSet<>();
        int[] values = constraintAnnotation.values();
        for (int value : values) {
            valueSet.add(value);
        }
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return valueSet.contains(value);
    }
}
```

注意 ConstraintValidator<ListValue, Integer> 泛型的第一个参数为被校验的注解，第二个参数为字段的取值类型。



#### 10.3  关联 自定义校验注解 和 自定义校验器

在注解中引用校验器的类 `@Constraint(validatedBy = {ListValueConstraintValidator.class})`

```java
@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface ListValue {
	// ...
}
```

注意：一个校验注解可以被用于多个类型的字段，此时可以使用多个不同数值类型的校验器，

如：

```java
public class PositiveValidatorForDouble implements ConstraintValidator<Positive, Double> {}
```

```java
public class PositiveValidatorForInteger implements ConstraintValidator<Positive, Integer> {}
```

使用的都是 `@Positive`注解，但是可以校验不同的数据类型

```java
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@Documented
@Constraint(validatedBy = { })
public @interface Positive {
	// ...
}
```



