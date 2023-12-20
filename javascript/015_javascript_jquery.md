# javascript jquery



## 01 引入jQuery

```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
    <!-- <script src="./scripts/jquery-3.6.1.js"></script> -->
    <script src="https://lf9-cdn-tos.bytecdntp.com/cdn/expire-1-M/jquery/3.6.0/jquery.js"></script>
</head>
<body>
    <script>
        console.log($)
    </script>
</body>
</html>
```



## 02 jQuery的核心函数

引入jQuery库，其实就是向网页中添加了一个新的函数$(jQuery)
$ 是jQuery中的核心函数，jQuery的所有功能都是通过该函数来进行的

```javascript
console.log(jQuery) 
console.log($)	
```

核心函数的功能
两种作用



1 将它作为工具类使用
在核心函数中jQuery为我们提供了多个工具方法

```javascript
var num = 10
function fn(){}

console.log($.isFunction(num)) // false
console.log($.isFunction(fn)) // true
console.log(typeof fn === "function") // true
```



2 将它作为函数使用
将一个函数作为$的参数
这个函数会在文档加载完毕之后执行
相当于：

```javascript
document.addEventListener("DOMContentLoaded", function(){
    ...
})
```



将选择器字符串作为参数
jQuery自动去网页中查找元素
作用类似于 

```javascript
document.querySelectorAll("...")
```

注意：
通过jQuery核心函数查询到的结果并不是原生的DOM对象，
而是一个经过jQuery包装过的新的对象，这个对象我们称其为 **jQuery对象**



jQuery对象中为我们提供了很多新的方法，方便我们做各种DOM操作
但是jQuery对象不能直接调用原生DOM对象的方法
通过我们为jQuery对象命名时，会使用$开头，加以区分



将DOM对象作为参数
可以将DOM对象转换为jQuery对象，从而使用jQuery对象的方法



将html代码作为参数
会根据html代码来创建元素（jQuery对象）



示例:

```javascript
$(function(){
    $("#btn01").click(function(){
        alert("你点我干嘛~~")
        var btn = document.getElementById("btn01") // [object HTMLButtonElement]
        var $btn = $("#btn01") // [object Object]
        alert($(btn))	// 将DOM对象转换为jQuery对象
        var $h1 = $("<h1>我是一个标题</h1>") // 根据html代码来创建元素（jQuery对象）
        $("div").append($h1)
    })
})
```



## 03 jQuery对象

通过jQuery核心函数获取到的对象就是jQuery对象
jQuery对象是jQuery中定义的对象
可以将其理解为是DOM对象的升级版，在jQuery对象中为我们提供了很多简单易用的方法
来帮助我们简化DOM操作



jQuery对象本质上是一个DOM对象的数组（**类数组**, 很类似于数组, 可以遍历, 但不是 js 中的数组）
可以通过索引获取jQuery对象中的DOM对象

当我们修改jQuery对象时，它会自动修改jQuery(类数组)中的所有元素
这一特点称为jQuery的隐式迭代

通常情况下，jQuery对象方法的返回值依然是一个jQuery对象
所以我们可以在调用一个方法后继续调用其他的jQuery对象的方法
这一特性，称为jQuery对象的 链式调用



示例:

```javascript
$("#btn01").click(function () {
    var $li = $("li")			// $li 本质上是一个类数组
    alert($li[0].textContent)	// $li[0] 通过索引获取jQuery对象中的DOM对象

    $li.text("哈哈哈")			  // 类数组中所有的元素都设置了text, 隐式迭代

    var text = $li.text() 		// 读取文本，返回所有标签中的文本
    var id = $li.attr("id") 	// 读取属性时，返回第一个标签的属性

    alert(id)

    var result = $li.text("新的文本内容")
    alert(result === $li)

    $li.text("新的文本内容").css("color", "red")
})
```



## 04 jQuery对象的方法

https://api.jquery.com/category/manipulation/class-attribute/



### addClass()

可以为元素添加一个或多个class

```javascript
$(function () {
    // 为按钮绑定响应函数
    $("#btn").click(function () {
        // 为box1添加class
        // addClass() 可以为元素添加一个或多个class
        $(".box1").addClass(["box2", "box3"])

        // addClass可以接收一个回调函数作为参数
        $(".box1").addClass(function (index, className) {
            // 在回调函数中，this表示的是当前的元素
			
            // 使用 html DOM 方式
            if (index % 2 === 0) {
                // 添加box2
                this.classList.add("box2")
            } else {
                // 添加box3
                this.classList.add("box3")
            }

            // 使用 jQuery 方式
            if (index % 2 === 0) {
                // 添加box2
                $(this).addClass("box2")
            } else {
                // 添加box3
                $(this).addClass("box3")
            }
        })

        $(".box1").addClass(function (index) {

            // 回调函数的返回值会成为当前元素的class
            // return ["box2", "box3"]

            if (index % 2 === 0) {
                return "box2"
            } else {
                return "box3"
            }
        })
    })
})
```



### clone()

复制jQuery对象

```javascript
$(function () {
    $("#list li:nth-child(1)").click(function () {
        alert("孙悟空")
    })

    /*
        clone() 用来复制jQuery对象
    */
    var $swk = $("#list li:nth-child(1)").clone(true)
    var $list2 = $("#list2")

    $("#btn").click(function () {
        $list2.append($swk)
    })
})
```



### unwrap() 

删除外层父元素



### wrap() 

为当前元素添加一个容器



### wrapAll() 

为当前的所有元素统一添加容器



### wrapInner() 

为当前元素添加一个内部容器

```javascript
$(function () {
    $("#btn").click(function () {
        // $("#list li").unwrap()				// 删掉外层的 #list 元素 <ul><li></li> <li></li> </ul> => <li></li> <li></li>
        // $("#list li").wrap("<div/>")			// 每一个 li 外面添加一个 div, <div><li>...</li><div> <div><li>...</li><div>
        // $("#list li").wrapAll("<div/>")		// 所有的 li 一起外面包裹一个 div <div> <li>...</li> <li>...</li> <div>
        $("#list li").wrapInner("<div/>")		// 每一个li内部添加一个div  <li>...<div></div><li> <li>...<div></div><li>
    })
})
```



### append()

向父元素后边添加一个子元素

```javascript
$("#box1").append("<div id='box2'/>")
```



### appendTo()

将子元素添加到父元素后边

```javascript
$("<div id='box2'/>").appendTo("#box1")
```



### prepend()

向父元素的前边添加子元素



### prependTo()

将子元素添加到父元素前边



### text()

获取/设置元素的文本内容



### html()

获取/设置元素的html代码

```
$("#btn").click(function () {

        $("#box1").append("<div id='box2'/>")

        $("<div id='box2'/>").appendTo("#box1")

        $("#box1").prepend("<div id='box2'/>")
        $("<div id='box2'/>").prependTo("#box1")


    })
```



### empty() 

删除所有的子元素



### remove() 

移除元素（移除事件）

```javascript
$("li:nth-child(1)").remove()
```



### detach() 

移除元素

```javascript
$("li:nth-child(1)").detach()
```



### attr() 

用来读取或设置元素的属性

读取布尔值返回实际值

```javascript
var type = $("input[type=text]").attr("type")
```



### prop() 

用来读取或设置元素的属性

读取布尔值返回 true/false
读取布尔值属性时与 `attr()` 返回不同的值

```javascript
var name = $("input[type=text]").prop("name")
var checked = $("input[type=radio]").prop("checked")
$("input[type=text]").prop("value","哈哈")
$("input[type=radio]").prop("checked", true)
```



### val()

读取元素的 `value` 值, 如 `input`, `select` 和`textarea` 元素 , 调用的集合为空时, 返回 `undefined`.

```javascript
// Get the value from a dropdown select directly
$( "select#foo" ).val();
```



### val( value )

为每一个匹配的元素设置 value 值

```javascript
$( "input[type=text].tags" ).val(function( index, value ) {
  return value.trim();
});

$("input").val(text);
```



### css()

https://api.jquery.com/category/css/

css 可以用来读取或设置元素的样式

```javascript
var $width = $("#box1").css("width")
alert($("#box1").css("background-color"))
alert($("#box1").css("left"))

$("#box1").css("width", 300)
$("#box1").css({width:300, height:500, backgroundColor:"yellow"})

alert($("#box1").innerHeight())
$("#box1").innerHeight(500)

alert($("#box1").offset().top + '--' + $("#box1").offset().left)
```



### offset()

读取或者设置集合中第一个元素的坐标

```javascript
var offset = p.offset();
alert( "left: " + offset.left + ", top: " + offset.top );
```

```javascript
$( "p" ).last().offset({ top: 10, left: 30 });
```



## 05 筛选方法

### filter()

筛选元素

```javascript
$(".box1").filter(".a").css("background-color", "#bfa")
```



### []

获取索引指定的元素

```javascript
$(".box1")[0]
```



### get()

获取索引指定的元素

```
$(".box1").get(0)
```



### eq()

用来获取jQuery对象中指定索引的元素

```javascript
$(".box1").eq(-1).css("background-color", "#bfa")
```



### first() 

获取第一个元素



### last() 

获取最后一个元素



### even() 

获取索引为偶数的元素

```javascript
$(".box1").even().css("background-color", "#bfa")
```



### odd() 

获取索引为奇数的元素



### slice() 

切片

```javascript
$(".box1").slice(1, 3).css("background-color", "#bfa")
```



### end()

将jQuery对象恢复到筛选前的状态

```javascript
$(".box1")
    .filter(".a")
    .css("background-color", "#bfa")
    .end()
    .css("border-color", "blue")
```



### add() 

向jQuery对象中添加元素

```javascript
$(".box1").add(".box2").css("background-color", "#bfa")
```



### contents() 

获取当前jQuery对象中所有元素的直接子元素, 包括文本节点和注释节点,  与 children() 类似, 但是 children() 不能获取 文本节点和注释节点

```javascript
$(".box1").contents().css("background-color", "#bfa")
```



### addBack()

将之前的元素集合添加到元素栈中的当前的集合, 有一个可选参数: 过滤器

```javascript
$(".box1").contents().addBack().css("background-color", "#bfa")
```



## 06 事件

可以通过指定方法来为jQuery对象绑定事件

```javascript
$(document.body).click(function(){
    alert("body")
})
```



在jQuery的事件响应函数中，同样有事件对象，但是这个对象是经过jQuery包装过的新的对象
包装该对象主要是为了解决兼容性的问题

在jQuery的事件回调函数中，可以通过return false来取消默认行为和冒泡

```javascript
$("a").click(function (event) {
    // event 是 jquery 包装的对象
    
    // 阻止事件传播    
    event.stopPropagation()
    // 阻止默认行为
    event.preventDefault()

    alert(123)

    // 在jQuery的事件回调函数中，可以通过return false来取消默认行为和冒泡
    // return false
})
```



### on()

也可以通过on()方法来绑定事件

```javascript
$(".box1").on("click", function(){
    alert("哈哈")
})
```

可以通过  `click.key-name`的方式给键盘绑定按键事件监听

```javascript
$("#btn01").on("click.a", function () {
    // 键盘 a 按键事件
    alert("通过on绑定的事件！")
})

$("#btn01").on("click.b", function () {
    // 键盘 b 按键事件    
    alert("通过on绑定的事件！2")
})

$("#btn03").on("click", function () {
    $(document.body).append("<div class='box1'/>")
})

$(document).on("click",".box1", function(event){
    alert("哈哈")
})
```



### off()

可以用来取消事件的绑定

```javascript
$("#btn02").on("click", function () {
    $("#btn01").off("click.a")
})
```



### one()

one()用来绑定一个一次性的事件

```javascript
$(".box1").one("click", function () {
    alert('嘻嘻')
})
```



## 练习

### 全选-反选

```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Document</title>
    <script src="./scripts/jquery-3.6.1.js"></script>
    <script>
        $(function () {
            // 点击全选框后，所有其他的多选框同步切换状态
            // 获取全选框
            var $checkAll = $("#check-all")
            // 获取四个多选框
            var $hobbies = $("[name=hobby]")

            $checkAll.click(function () {
                // 在事件的响应函数中，this是绑定事件的对象，这点在jQuery中同样适用
                // 在这里 this 是dom对象
                // alert(this.checked)
                $hobbies.prop("checked", this.checked)
            })

            // 使得全选框和四个小框同步
            $hobbies.click(function () {
                // 判断四个多选框是否全选
                // var flag = $hobbies.filter(":checked").length !== 0
                // var flag = $hobbies.is(":not(:checked)")
                $checkAll.prop("checked", !$hobbies.is(":not(:checked)"))
            })

            // 全选
            $("#all").click(function () {
                // add()不会影响原来的jQuery对象
                $hobbies.add($checkAll).prop("checked", true)
            })

            // 全不选
            $("#no").click(function () {
                // add()不会影响原来的jQuery对象
                $hobbies.add($checkAll).prop("checked", false)
            })

            // 反选
            $("#reverse").click(function () {
                // $hobbies.prop("checked", function(index, oldValue){
                //     return !oldValue
                // })

                // $checkAll.prop("checked", !$hobbies.is(":not(:checked)"))

                $checkAll.prop(
                    "checked",
                    !$hobbies
                        .prop("checked", function (index, oldValue) {
                            return !oldValue
                        })
                        .is(":not(:checked)")
                )
            })

            // 提交
            $("#send").click(function () {
                // 打印选中的内容
                // alert($hobbies.val())
                // for(var i=0; i<$hobbies.length; i++){
                //     alert($hobbies[i].value)
                // }

                // each() 用来遍历jQuery元素，需要一个函数做为参数
                // 函数会执行多次，每次执行时都会将当前元素设置为函数中的this
                // $hobbies.each(function(index, ele){
                //     this.checked && alert(this.value)
                // })

                $hobbies.filter(":checked").each(function(index, ele){
                    alert(this.value)
                })
            })
        })
    </script>
</head>
<body>
<div>
    <form action="#">
        <div>
            请选择你的爱好：
            <input type="checkbox" id="check-all" /> 全选
        </div>
        <div>
            <input type="checkbox" name="hobby" value="乒乓球" /> 乒乓球
            <input type="checkbox" name="hobby" value="篮球" /> 篮球
            <input type="checkbox" name="hobby" value="羽毛球" /> 羽毛球
            <input type="checkbox" name="hobby" value="足球" /> 足球
        </div>
        <div>
            <button type="button" id="all">全选</button>
            <button type="button" id="no">取消</button>
            <button type="button" id="reverse">反选</button>
            <button type="button" id="send">提交</button>
        </div>
    </form>
</div>
</body>
</html>
```



### 员工管理

```html
<!DOCTYPE html>
<html lang="zh">
    <head>
        <meta charset="UTF-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Document</title>
        <style>
            .outer {
                width: 400px;
                margin: 100px auto;
                text-align: center;
            }

            table {
                width: 400px;
                border-collapse: collapse;
                margin-bottom: 20px;
            }

            td,
            th {
                border: 1px black solid;
                padding: 10px 0;
            }

            form div {
                margin: 10px 0;
            }
        </style>

        <script src="./scripts/jquery-3.6.1.js"></script>
        <script>
            $(function () {
                // 删除
                $(document).on("click", "a", function () {
                    // alert(this) 委托时 jq将this设置为了触发事件的对象
                    // 获取当前tr
                    // var tr = this.parentNode.parentNode

                    var $tr = $(this).parents("tr") // 在当前元素的祖先中寻找tr

                    if (
                        confirm(
                            "确认删除【" +
                                $tr.children()[0].textContent +
                                "】吗？"
                        )
                    ) {
                        $tr.remove()
                    }

                    return false
                })

                // 添加
                $("#btn").on("click", function () {
                    // 获取用户输入的内容
                    var name = $("#name").val().trim()
                    var email = $("#email").val().trim()
                    var salary = $("#salary").val().trim()

                    // console.log(name + "--" + email + "--" + salary)
                    // $("tbody").append(
                    //     "<tr><td>" +
                    //         name +
                    //         "</td><td>" +
                    //         email +
                    //         "</td><td>" +
                    //         salary +
                    //         "</td><td><a href='javascript:;'>删除</a></td></tr>"
                    // )

                    // 创建一个tr
                    var $tr = $(
                        "<tr><td/><td/><td/><td><a href='javascript:;'>删除</a></td><tr>"
                    )
                    // 添加内容
                    var $tds = $tr.find("td")
                    $tds.eq(0).text(name)
                    $tds.eq(1).text(email)
                    $tds.eq(2).text(salary)
                    // 将tr添加到tbody中
                    $("tbody").append($tr)
                })
            })
        </script>
    </head>
    <body>
        <div class="outer">
            <table>
                <tbody>
                    <tr>
                        <th>姓名</th>
                        <th>邮件</th>
                        <th>薪资</th>
                        <th>操作</th>
                    </tr>
                    <tr>
                        <td>孙悟空</td>
                        <td>swk@hgs.com</td>
                        <td>10000</td>
                        <td><a href="javascript:;">删除</a></td>
                    </tr>
                    <tr>
                        <td>猪八戒</td>
                        <td>zbj@glz.com</td>
                        <td>8000</td>
                        <td><a href="javascript:;">删除</a></td>
                    </tr>
                    <tr>
                        <td>沙和尚</td>
                        <td>shs@lsh.com</td>
                        <td>6000</td>
                        <td><a href="javascript:;">删除</a></td>
                    </tr>
                </tbody>
            </table>

            <form action="#">
                <div>
                    <label for="name">姓名</label>
                    <input type="text" id="name" />
                </div>
                <div>
                    <label for="email">邮件</label>
                    <input type="email" id="email" />
                </div>
                <div>
                    <label for="salary">薪资</label>
                    <input type="number" id="salary" />
                </div>
                <button id="btn" type="button">添加</button>
            </form>
        </div>
    </body>
</html>
```

