# Javascript 入门

## 1 Hello world

```html
<!DOCTYPE html>
<html lang="zh">
    <head>
        <meta charset="UTF-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Hello World</title>

        <!-- JS代码需要编写到script中 -->
        <script>
            // 弹窗
            alert("哈哈哈哈")

            // 控制台输出
            console.log('你猜我在哪？')

            // 在网页(文档, <body></body>)中输出
            document.write('你猜我在哪？')

        </script>
    </head>
    <body></body>
</html>
```

JS代码需要编写到script中



## 2 编写位置

在网页中，可以在这些位置编写JS代码：



### 1 Script标签

可以将js编写到网页内部的script标签

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hello World</title>

    <script>
        alert("hello world")
    </script>
</head>
<body>
    
</body>
</html>
```



### 2 外部js文件

可以将js编写外部的js文件中，然后通过script标签进行引入

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hello World</title>

    <script src="./script/script.js"></script>
</head>
<body>
    
</body>
</html>
```

注意此时不能在`<script>`标签中写代码



### 3 标签的属性中

可以将js代码编写到指定属性中

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hello World</title>
</head>
<body>
    <button onclick="alert('hello')">按钮</button>
   	<!-- href后直接写代码不行, 会被认为是路径, 需要加上 "javascript:" -->
    <a href="javascript:alert('hello');">超链接</a>
    <!-- 把 "javascript:alert('hello');" 这段直接复制到浏览器的地址栏可以直接执行 -->
    
    
    <!-- 这个超链接点击之后什么也不会发生 -->
    <a href="javascript:;">超链接</a>
    
    <!-- 这个超链接点击之后什么也不会发生 -->
    <a href="javascript:void(0);">超链接</a>
</body>
</html>
```



## 3 基本语法

### 1 注释

注释中的内容会被解释器忽略

可以通过注释来对代码进行解释说明

也可以通过注释来注释掉不想执行的代码



#### 1 单行注释

```javascript
// 单行注释
alert(123) // alert()用来弹出一个警告框
```



#### 2 多行注释

```javascript
/*
    多行注释
*/

/* 多行注释 */
```



### 2 大小写

JS会严格区分字母的大小写，A和a是两个东西，所以注意区分。

```javascript
alert(123) // 可以执行
Alert(123) // 无法执行
```



### 3 忽略空格

和HTML相同，JS中的多个空格和换行会被忽略，所以可以借助空格或换行来对代码进行格式化，不会影响代码的运行逻辑。

```javascript
alert(123) // 可以执行

// 这样也可以执行
alert(
    123
) 
```



### 4 分号

JS中每一条语句都应该以`;`结尾，但是JS解释器中有**自动添加分号**的机制，所以即使不加括号JS解释器会自动根据代码的上下文添加分号，虽然在极少数的情况下会加错，但不写分号依然是偷懒的不错选择，所以加不加`;`完全看你自己的心情。

```javascript
// 可以执行
alert(111)
console.log(222)
```



