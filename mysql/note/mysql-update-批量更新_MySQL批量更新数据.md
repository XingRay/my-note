mysql update 批量更新_MySQL批量更新数据

淡定男

于 2021-01-23 05:43:25 发布

25007
 收藏 10
文章标签： mysql update 批量更新
版权

华为云开发者联盟
该内容已被华为云开发者联盟社区收录
加入社区
昨天晚上遇到更新数据到表中特别慢的情况，因为程序是循环一条一条更新数据的，下面是一条sql语句实现批量更新的具体方法。

1.mysql更新语句很简单，更新一条数据的某个字段，一般这样写：

UPDATE mytable SET myfield = 'value' WHERE other_field = 'other_value';
如果更新多条数据为不同的值，可能很多人会循环一条一条的更新记录。

一条记录update一次，这样性能很差，也很容易造成阻塞。

2.mysql并没有提供直接的方法来实现批量更新，但是可以使用case when 这个小技巧来实现批量更新。

UPDATE categories SET    display_order = CASE id        WHEN 1 THEN 3        WHEN 2 THEN 4        WHEN 3 THEN 5    ENDWHERE id IN (1,2,3)
这句sql的意思是，更新display_order 字段：

如果id=1 则display_order 的值为3，

如果id=2 则 display_order 的值为4，

如果id=3 则 display_order 的值为5。

即是将条件语句写在了一起。

这里的where部分不影响代码的执行，但是会提高sql执行的效率。

确保sql语句仅执行需要修改的行数，这里只有3条数据进行更新，而where子句确保只有3行数据执行。

3.更新多值
UPDATE categories SET    display_order = CASE id        WHEN 1 THEN 3        WHEN 2 THEN 4        WHEN 3 THEN 5    END,    title = CASE id        WHEN 1 THEN 'New Title 1'        WHEN 2 THEN 'New Title 2'        WHEN 3 THEN 'New Title 3'    ENDWHERE id IN (1,2,3)
如图：



这样就能实现mysql语句更新多条记录了。
————————————————
版权声明：本文为CSDN博主「淡定男」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/weixin_42351363/article/details/113088471