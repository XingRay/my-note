# MyBatis通过注解方式批量添加、修改、删除

## 一、数据库实体DO

```
 public class User implements Serializable {
     private Long     　id;     //用户ID
     private String     name;   //用户姓名
     private Integer    age;    //用户年龄
     .......
 }
```

## 二、数据库操作

　　2.1、批量插入用户

```
     @Insert("<script>"  +
                 "insert into user(id, name, age) VALUES " +
                 "<foreach collection='list' item='item' index='index' separator=','> " +
                     "(#{item.id},#{item.name},#{item.age}) " +
                 "</foreach>" +
             "</script>")
     void batchInsert(@Param("list")List<User> list); //批量添加用户
```

　　2.2、批量修改用户

```
     @Update({"<script>"  +
                 "<foreach collection='list' item='item' index='index' open='(' separator=',' close=')'> " +
                     "update user set name= #{item.name}, age= #{item.age} " +
                     "where id = #{item.id} " +
                 "</foreach>" +
             "</script>"})
     void batchUpdate(@Param("list")List<User> list);//批量修改用户
```

　　2.3、批量删除用户

```java
     @Delete("<script>"  +
                 "delete from user where id in " +
                 "<foreach collection='array' item='id' open='('separator=',' close=')'> " +
                     "#{id}" +
                 "</foreach>" +
             "</script>")
     void batchDelete(long[] ids);//批量删除用户      //☆☆☆ 如何获取 long[] ids ？？？
     //1、获取要删除用户的集合
     List<User> user = userMapper.selectAll();//用的是tk.mybatis下获取所有用户
     //2、根据集合获取 long[] ids
          //朋友，如果你还在用遍历、创建数组等 SAO 操作.....你 OUT 了
          //让我们看看jdk8的stream流是怎么搞的：
         List<Long> idList = user.stream.map(User::getId).collect(Collectors.toList());//获取id集合
         long[] ids = idList.stream.mapToLong(i -> i).toArray();// 获的long[] ids          就两步就实现了（其实就一步），它不香吗？？？
```

