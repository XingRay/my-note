# mybatis plus坑之 - @TableField(typeHandler) 查询时不生效为null



## 一、场景

实体中经常会有List类型的字段需要映射.
mybatis plus有提供注解方式直接注入，传送门：传送门 - 字段类型处理器

https://www.bookstack.cn/read/mybatis-plus-3.x/99fcff2f38c1be8e.md



## 二、问题

增删改能生效，但是保存数据的格式异于平常，且查询失效。

我的操作如下：

1.自定义类JacksonTypeHandler，由于想要一次性解决所有的映射，所以这里我写成了 @MappedTypes({Object.class})

```java
/**
 * 通用类型的TypeHandler
 */
@Slf4j
@MappedTypes({Object.class})
@MappedJdbcTypes(JdbcType.VARCHAR)
public class JacksonTypeHandler extends BaseTypeHandler<Object> {

	private static ObjectMapper objectMapper;
    private Class<Object> type;

    static {
        objectMapper = new ObjectMapper();
    }

    public JacksonTypeHandler(Class<Object> type) {
        if (log.isTraceEnabled()) {
            log.trace("JacksonTypeHandler(" + type + ")");
        }
        if (null == type) {
            throw new MybatisPlusException("Type argument cannot be null");
        }
        this.type = type;
    }

    private Object parse(String json) {
        try {
            if (json == null || json.length() == 0) {
                return null;
            }
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int columnIndex, Object parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(columnIndex, toJsonString(parameter));
    }
}

```



2.实体写法如下图：

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "goods", autoResultMap = true)
public class Goods extends BaseEntity<Goods > {
	@TableField(typeHandler = JacksonTypeHandler.class, value = "`images`")
    private List<String> images;
}
```

此时，用程序插入数据，数据库中存储的格式变成了带括号和引号，如下图：

![在这里插入图片描述](./003_mybatis plus%E5%9D%91%E4%B9%8B.assets/3f95d989727233ca0f40f2a77c2ade0d.png)

查询数据的时候，该字段返回为 null



## 三、排查步骤

经过反复尝试，发现有几个点需要注意，大家可以根据如下一一排查。

1 实体类上，需要加上注解 @TableName(autoResultMap = true)

2 自定义TypeHandler类时，@MappedTypes({Object.class})是 不生效 的，还是指定 需要具体的type 。比如List类型的字段可以自定义一个 ListTypeHandler（示例代码放在下面）

3 自定义TypeHandler类需加注解：
@MappedJdbcTypes(JdbcType.VARCHAR) //数据库类型
@MappedTypes({List.class}) //java数据类型

4 字段上需要注解标注：@TableField( typeHandler = ListTypeHandler.class)

5 配置文件需要加上：mybatis-plus.type-handlers-package=com.package.handler（包名）



## 四、示例

这里以字段类型为 List 为示例。

自定义TypeHandler类： ListTypeHandler.java

```java
import com.yeepay.shade.org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 具体类型的TypeHandler
 */
@Slf4j
@MappedJdbcTypes(JdbcType.VARCHAR)  // 数据库类型
@MappedTypes({List.class})          // java数据类型
public class ListTypeHandler implements TypeHandler<List<String>> {

    @Override
    public void setParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        String hobbys = dealListToOneStr(parameter);
        ps.setString(i, hobbys);
    }

    /**
     * 集合拼接字符串
     *
     * @param parameter
     * @return
     */
    private String dealListToOneStr(List<String> parameter) {
        if (parameter == null || parameter.size() <= 0)
            return null;
        String res = "";
        for (int i = 0; i < parameter.size(); i++) {
            if (i == parameter.size() - 1) {
                res += parameter.get(i);
                return res;
            }
            res += parameter.get(i) + ",";
        }
        return null;
    }

    @Override
    public List<String> getResult(ResultSet rs, String columnName) throws SQLException {
        if (StringUtils.isBlank(rs.getString(columnName))) {
            return new ArrayList<>();
        }
        return Arrays.asList(rs.getString(columnName).split(","));
    }

    @Override
    public List<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        if (StringUtils.isBlank(rs.getString(columnIndex))) {
            return new ArrayList<>();
        }
        return Arrays.asList(rs.getString(columnIndex).split(","));
    }


    @Override
    public List<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String hobbys = cs.getString(columnIndex);
        if (StringUtils.isBlank(hobbys)) {
            return new ArrayList<>();
        }
        return Arrays.asList(hobbys.split(","));
    }
}

```



2 实体类：Goods.java

```java
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName(value = "goods", autoResultMap = true)
public class Goods extends BaseEntity<Goods > {
	@TableField(typeHandler = ListTypeHandler.class, value = "`images`")
    private List<String> images;
}
```

配置文件：application.yml

```yaml
mybatis-plus:
	#（路径）
	type-handlers-package: com.package.handler
```

这样配置就好啦~

数据库存储的格式也比较正常：

![在这里插入图片描述](./003_mybatis plus%E5%9D%91%E4%B9%8B.assets/27e9b5e5aa2bf748ab90d3d521b9fd09.png)



## 五、疑问

在尝试的过程中，我发现一个有趣的现象~

我是这么操作的：

```
1.在handle目录下，自定义了两个typeHandler类，
  分别是上面所列举的 JacksonTypeHandler 和 ListTypeHandler。

2.然后配置文件正常设置。

3.但是实体类的字段上面，配置的是 **JacksonTypeHandler**
	@TableField(typeHandler = JacksonTypeHandler.class, value = "`images`")
    private List<String> images;
```

也就是说，这个时候程序里面没有引用 ListTypeHandler的地方，而且引用的是 JacksonTypeHandler 。

但是这个时候，查询竟然也生效了？

两者都加上日志后发现，ListTypeHandler 有日志，JacksonTypeHandler没有日志。

原来，虽然没有引用ListTypeHandler ，但是代码自动识别出了类型并且使用了它，所以查询也生效了。

好吧，所以 TypeHandler 还是 需要定义出具体的类型 ~