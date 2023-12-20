# python入门



## 1. Python基础学习路线

1. python入门

   1.1 环境搭建

   1.2 变量

   1.3 输入输出

   1.4 数据类型

2. 流程控制

   1. 条件语句
   2. 循环

3. 数据序列

   1. 字符串
   2. 列表
   3. 字典
   4. 元组

4. 函数

   1. 参数
   2. 返回值
   3. 递归
   4. lambda表达式

5. 文件操作

   1. 打开和关闭
   2. 读取和写入

6. 面向对象

   1. 类和对象
   2. 继承
   3. 面向对象高级

7. 模块、包

8. 异常



## 2. python入门

### 2.1 注释

```python
# 行注释
"""
块注释
"""

'''
块注释
'''
```



### 2.2 变量

#### 2.2.1 什么是变量

数据在内存中储存后再定义一个名字，这个名字就是变量。变量就是一个存储数据的时候，当前数据所在内存地址的名字。

#### 2.2.2 定义变量

```python
变量名 = 值
```

#### 2.2.3 标识符

- 数字、字母、下划线
- 不能由数字开头
- 不能使用关键字
- 区分大小写

#### 2.2.4 命名习惯

- 大驼峰
- 小驼峰
- 下划线



### 2.3 数据类型

数据类型：

- 数值 int/float
- 布尔型 True/Flase
- str
- list
- tuple
- set
- dict

```python
# type() 获取数据类型


num1 = 1
# <class 'int'>
print(type(num1))


num2 = 1.1
#<class 'float'>
print(type(num2))


s = 'hello world'
#<class 'str'>
print(type(s))


b = True
#<class 'bool'>
print(type(b))


c = [10, 20, 30]
#<class 'list'>
print(type(c))


d = (10, 20, 30)
#<class 'tuple'>
print(type(d))


e = {10, 20, 30}
#<class 'set'>
print(type(e))


f = {'name':'Tom', 'age': 18}
#<class 'dict'>
print(type(f))
```


