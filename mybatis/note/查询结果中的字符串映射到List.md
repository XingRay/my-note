## mybatis将mysql查询的字符串映射为List



Java对象中的attValueList属性声明为`List<String>`类型，如下所示：

```java
@Data
public class AttrBo {
    private Long attrId;
    private String attrName;
    private List<String> attValueList;
}
```

使用自定义的TypeHandler，在自定义TypeHandler中，可以将字符串值按照逗号分隔符切分成多个String值，并将它们添加到List中。以下是TypeHandler实现：

```java
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeReference;

public class StringListTypeHandler extends BaseTypeHandler<List<String>> {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType)
      throws SQLException {
    String parameterAsString = String.join(",", parameter);
    ps.setString(i, parameterAsString);
  }

  @Override
  public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return toList(rs.getString(columnName));
  }

  @Override
  public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return toList(rs.getString(columnIndex));
  }

  @Override
  public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return toList(cs.getString(columnIndex));
  }

  private List<String> toList(String s) {
    if (s == null || s.isEmpty()) {
      return null;
    }
    return Arrays.asList(s.split(","));
  }
}
```

在上面的代码中定义了一个TypeHandler，它将`List<String>`转换为逗号分隔的字符串，并将字符串转换为`List<String>`。在映射文件中，需要为attValueList属性指定TypeHandler，如下所示：

```xml
<resultMap id="attrBoResultMap" type="com.xingray.gulimall.product.bo.AttrBo">
    <result property="attValueList" column="attr_value_list" jdbcType="VARCHAR" javaType="java.lang.String"
            typeHandler="com.xingray.gulimall.product.config.mybatis.StringListTypeHandler"/>
</resultMap>

<select id="getSaleAttrBySpuId" resultMap="attrBoResultMap">
    SELECT ssav.`attr_id`                    AS attr_id,
    GROUP_CONCAT(DISTINCT ssav.`attr_name`)  AS attr_name,
    GROUP_CONCAT(DISTINCT ssav.`attr_value`) AS attr_value_list
    FROM `pms_sku_info` info
    LEFT JOIN `pms_sku_sale_attr_value` ssav
    ON info.`sku_id` = ssav.`sku_id`
    WHERE `spu_id` = #{spu_id}
    GROUP BY ssav.`attr_id`
</select>
```

在上面的代码中，我们在result标签中为attValueList属性指定了StringListTypeHandler类型处理器。这告诉MyBatis在将数据库中的att_value_list列值映射到Java对象中的attValueList属性时使用我们定义的TypeHandler。



