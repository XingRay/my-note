# grep 精准匹配

grep 精准匹配用法，多用于脚本处理时，避免匹配多个文件或者字符。

示例如下

1.txt内容：

```shell
a,aa,aaa,abc
```

```shell
cat filename | grep aa
```

结果：aa,aaa

```shell
cat filename | grep -w aa
```

结果：aa
