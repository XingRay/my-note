## Mybatis将查询结果转化为嵌套List



dao接口

```java
public interface AttrDao extends BaseMapper<AttrEntity> {
    List<SpecAttrGroupBo> getSpecAttrValueBySpuIdAndCategoryId(@Param("spu_id") Long spuId, @Param("category_id") Long categoryId);
}
```



mapper.xml

```xml
<resultMap id="group_attr_value" type="com.xingray.gulimall.product.bo.SpecAttrGroupBo">
    <result property="attrGroupId" column="attr_group_id"/>
    <result property="attrGroupName" column="attr_group_name"/>
    <collection property="attrValueList" ofType="com.xingray.gulimall.product.bo.AttrValueBo">
        <result property="attrId" column="attr_id"/>
        <result property="attrName" column="attr_name"/>
        <result property="attrValue" column="attr_value"/>
    </collection>
</resultMap>

<select id="getSpecAttrValueBySpuIdAndCategoryId" resultMap="group_attr_value">
    SELECT
    ag.attr_group_id AS attr_group_id,
    ag.attr_group_name AS attr_group_name,
    aagr.attr_id AS attr_id,
    pav.attr_name AS attr_name,
    pav.attr_value AS attr_value
    FROM `pms_attr_group` ag
    LEFT JOIN `pms_attr_attrgroup_relation` aagr
    ON ag.attr_group_id = aagr.attr_group_id
    LEFT JOIN `pms_product_attr_value` pav
    ON pav.attr_id = aagr.attr_id
    WHERE ag.catelog_id = #{category_id} AND pav.spu_id = #{spu_id}
</select>
```



实体类

```java
@Data
public class SpecAttrGroupBo {
    private Long attrGroupId;
    private String attrGroupName;
    private List<AttrValueBo> attrValueList;
}
```

```java
@Data
public class AttrValueBo {
    private Long attrId;
    private String attrName;
    private String attrValue;
}
```



