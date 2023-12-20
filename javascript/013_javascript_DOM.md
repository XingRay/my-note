# javascript DOM



## 01 helloWorld

要使用DOM来操作网页，我们需要浏览器至少得先给我一个对象才能去完成各种操作

所以浏览器已经为我们提供了一个document对象，它是一个全局变量可以直接使用 document代表的是整个的网页

```javascript
console.log(document)

// 获取btn对象
const btn = document.getElementById("btn")

// console.log(btn)
// 修改btn中的文字
btn.innerText = "Click ME"
```



## 02 document对象

document对象表示的是整个网页



document对象的原型链
HTMLDocument -> Document -> Node -> EventTarget -> Object.prototype -> null

凡是在原型链上存在的对象的属性和方法都可以通过Document去调用



部分属性：
document.documentElement --> html根元素
document.head --> head元素
document.title --> title元素
document.body --> body元素
document.links --> 获取页面中所有的超链接
...

```javascript
console.log(document.links)
```



## 03 元素节点

元素节点对象（element）
在网页中，每一个标签都是一个元素节点

如何获取元素节点对象？
1 通过document对象来获取元素节点

2 通过document对象来创建元素节点



通过document来获取已有的元素节点：

```javascript
document.getElementById()
```

```javascript
const btn = document.getElementById("btn")
```



根据id获取一个元素节点对象

```javascript
document.getElementsByClassName()
```

```javascript
const spans = document.getElementsByClassName("s1")
```

根据元素的class属性值获取一组元素节点对象
返回的是一个类数组对象
该方法返回的结果是一个实时更新的集合
当网页中新添加元素时，集合也会实时的刷新



根据标签名获取一组元素节点对象

```javascript
document.getElementsByTagName()
```

```javascript
const divs = document.getElementsByTagName("div")
```

返回的结果是可以实时更新的集合



 获取页面中所有的元素

```javascript
document.getElementsByTagName("*")
```



根据name属性获取一组元素节点对象

```javascript
document.getElementsByName()
```

```javascript
const genderInput = document.getElementsByName("gender")
```

返回一个实时更新的集合
主要用于表单项



根据选择器去页面中查询元素

```javascript
document.querySelectorAll()
```

```javascript
const divs2 = document.querySelectorAll("div")
```

会返回一个类数组（不会实时更新）



根据选择器去页面中查询第一个符合条件的元素

```javascript
document.querySelector()
```

```javascript
const div = document.querySelector("div")
```



创建一个元素节点

```javascript
document.createElement()
```

```javascript
const h2 = document.createElement("h2")
```

根据标签名创建一个元素节点对象





div元素的原型链
HTMLDivElement -> HTMLElement -> Element -> Node -> ...



通过元素节点对象获取其他节点的方法



获取当前元素的子节点（会包含空白的子节点）

element.childNodes 



获取当前元素的子元素

element.children 



获取当前元素的第一个子元素

element.firstElementChild 



获取当前元素的最后一个子元素

element.lastElementChild 



获取当前元素的下一个兄弟元素

element.nextElementSibling 



获取当前元素的前一个兄弟元素

element.previousElementSibling 



获取当前元素的父节点

element.parentNode 



获取当前元素的标签名

element.tagName 



```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Document</title>
</head>
<body>
<div id="box1">
    我是box1
    <span class="s1">我是s1</span>
    <span class="s1">我是s1</span>
</div>

<span class="s1">我是s1</span>

<script>
    const box1 = document.getElementById("box1")
    const spans = box1.getElementsByTagName("span")
    const spans2 = box1.getElementsByClassName("s1")
    const cns = box1.childNodes
    const children = box1.children
    console.log(children.length)
</script>
</body>
</html>
```



## 04 文本节点

在DOM中，网页中所有的文本内容都是文本节点对象,
可以通过元素来获取其中的文本节点对象，但是我们通常不会这么做



我们可以直接通过元素去修改其中的文本
修改文本的三个属性



element.textContent 获取或修改元素中的文本内容
获取的是标签中的内容，不会考虑css样式



element.innerText 获取或修改元素中的文本内容
innerText获取内容时，会考虑css样式
通过innerText去读取CSS样式，会触发网页的重排（计算CSS样式）
当字符串中有标签时，会自动对标签进行转义
<li> --> &lt;li&gt;



element.innerHTML 获取或修改元素中的html代码
可以直接向元素中添加html代码
innerHTML插入内容时，有被xss注入的风险



```html
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Document</title>
</head>
<body>
<div id="box1">
    <span style="text-transform: uppercase;">我是box1</span>
</div>

<script>
    const box1 = document.getElementById("box1")
    const text = box1.firstChild
    console.log(text)
    
    box1.innerText = "xxxx"
    console.log(box1.textContent)

    box1.textContent = "新的内容"

    // xss 攻击 , 加载攻击者指定的脚本
    box1.innerHTML = "<、script src='https://xxx.com/abc.js'><\/script>"


</script>
</body>
</html>

```



## 05 属性节点

属性节点（Attr）
在DOM也是一个对象，通常不需要获取对象而是直接通过元素即可完成对其的各种操作



如何操作属性节点：

### 方式一：

#### 读取：

```javascript
元素.属性名
```

（注意，`class` 属性需要使用 `className` 来读取）
读取一个布尔值时，会返回true或false



#### 修改：

```javascript
元素.属性名 = 属性值
```



### 方式二：

#### 读取：

```javascript
元素.getAttribute(属性名)
```

#### 修改：

```javascript
元素.setAttribute(属性名, 属性值)
```

#### 删除：

```javascript
元素.removeAttribute(属性名)
```



```javascript
const input = document.getElementsByName("username")[0]
const input = document.querySelector("[name=username]")

console.log(input.type)
console.log(input.getAttribute("type"))

input.setAttribute("value", "孙悟空")
input.setAttribute("disabled", "disabled")
```



## 06 事件

事件（event）



事件就是用户和页面之间发生的交互行为
比如：点击按钮、鼠标移动、双击按钮、敲击键盘、松开按键...  



可以通过为事件绑定响应函数（回调函数），来完成和用户之间的交互



绑定响应函数的方式：
1 可以直接在元素的属性中设置
2 可以通过为元素的指定属性设置回调函数的形式来绑定事件（一个事件只能绑定一个响应函数）
3 可以通过元素 `addEventListener()` 方法来绑定事件

```javascript
// 获取到按钮对象
const btn = document.getElementById("btn")

// 为按钮对象的事件属性设置响应函数
btn.onclick = function(){
    alert("我又被点了一下~~")
}

// btn.onclick = fn 的方式只能绑定一个处理回调, 最后一次设置的生效
btn.onclick = function(){
    alert("1123111")
}

// btn.addEventListener("click", fn) 的方式可以绑定多个处理回调, 都可以生效
btn.addEventListener("click", function () {
    alert("哈哈哈")
})

btn.addEventListener("click", function () {
    alert("嘻嘻嘻")
})

btn.addEventListener("click", function () {
    alert("呜呜呜")
})
```



## 07 文档的加载

网页是自上向下加载的，如果将js代码编写到网页的上边，
js代码在执行时，网页还没有加载完毕，这时会出现无法获取到DOM对象的情况

window.onload 事件会在窗口中的内容加载完毕之后才触发
document的DOMContentLoaded事件会在当前文档加载完毕之后触发

如何解决这个问题：
1 将script标签编写到body的最后（*****）
2 将代码编写到window.onload的回调函数中
3 将代码编写到document对象的DOMContentLoaded的回调函数中（执行时机更早）
4 将代码编写到外部的js文件中，然后以`defer`的形式进行引入（执行时机更早，早于DOMContentLoaded）（*****）



```javascript
window.onload = function () {
    const btn = document.getElementById("btn")
    console.log(btn)
}

window.addEventListener("load", function () {
    const btn = document.getElementById("btn")
    alert(btn)
})

document.addEventListener("DOMContentLoaded", function () {
    const btn = document.getElementById("btn")
    alert(btn)
})
```



```
<!DOCTYPE html>
<html lang="zh">
<head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Document</title>
    <script defer src="./script/script.js"></script>
</head>
<body>
<button id="btn">点我一下</button>

<iframe src="https://www.lilichao.com" frameborder="0"></iframe>

<script>
    const btn = document.getElementById("btn")
    console.log(btn)
</script>
</body>
</html>
```



## 08 练习

### 轮播图

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
                width: 640px;
                margin: 50px auto;
                text-align: center;
            }
        </style>
        <script>
            window.onload = function () {
                /* 
                    点击按钮切换图片
                */

                // 获取info
                const info = document.getElementById("info")

                // 获取到图片
                const img = document.getElementsByTagName("img")[0]

                // 获取两个按钮
                const prev = document.getElementById("prev")
                const next = document.getElementById("next")

                // 创建一个数组来存储图片的路径
                const imgArr = [
                    "./images/1.png",
                    "./images/2.png",
                    "./images/3.png",
                    "./images/4.png",
                    "./images/5.png",
                ]

                // 创建一个变量记录当前图片的索引
                let current = 0

                info.textContent = `总共 ${imgArr.length} 张图片，当前第 ${current + 1} 张`

                // 上一张
                prev.onclick = function () {
                    current--

                    //检查current的值是否合法
                    if(current < 0){
                        // current = 0
                        current = imgArr.length - 1
                    }

                    img.src = imgArr[current]

                    info.textContent = `总共 ${imgArr.length} 张图片，当前第 ${current + 1} 张`

                }

                // 点击next按钮后，切换图片
                next.onclick = function () {
                    current++

                    if(current > imgArr.length - 1){
                        // current = imgArr.length - 1
                        current = 0
                    }

                    // 切换图片 --> 2.png 就是修改img的src属性
                    img.src = imgArr[current]

                    info.textContent = `总共 ${imgArr.length} 张图片，当前第 ${current + 1} 张`

                }
            }
        </script>
    </head>
    <body>
        <div class="outer">
            <p id="info">
                总共n张图片，当前第m张        
            </p>
            <div class="img-wrapper">
                <img src="./images/1.png" alt="这是一个图片" />
            </div>

            <div class="btn-wrapper">
                <button id="prev">上一张</button>
                <button id="next">下一张</button>
            </div>
        </div>
    </body>
</html>

```



### 表单全选与反选 

```html
<!DOCTYPE html>
<html lang="zh">
    <head>
        <meta charset="UTF-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Document</title>

        <script>
            window.onload = function () {
                /* 
                    全选功能
                    取消
                    反选
                    提交
                    小checkbox和大checkbox同步
                */

                /* 
                    全选功能
                        - 点击按钮后，使四个多选框都变成选中状态
                */

                // 获取四个多选框
                const hobbies = document.getElementsByName("hobby")

                // 获取全选按钮
                const allBtn = document.getElementById("all")

                const checkAllBox = document.getElementById("check-all")

                // 为按钮绑定单级响应函数
                allBtn.onclick = function () {
                    // 将多选框设置为选中状态
                    for (let i = 0; i < hobbies.length; i++) {
                        hobbies[i].checked = true
                    }

                    checkAllBox.checked = true
                }

                /* 
                    取消功能
                        - 点击取消按钮后，取消所有的选中的状态
                */

                // 获取取消按钮
                const noBtn = document.getElementById("no")
                noBtn.onclick = function () {
                    for (let i = 0; i < hobbies.length; i++) {
                        hobbies[i].checked = false
                    }

                    checkAllBox.checked = false
                }

                /* 
                    反选功能
                        - 点击按钮后，选中的取消，没选中的选中
                */
                const reverseBtn = document.getElementById("reverse")

                reverseBtn.onclick = function () {
                    for (let i = 0; i < hobbies.length; i++) {
                        hobbies[i].checked = !hobbies[i].checked
                    }

                    // 获取所有选中的checkbox
                    const checkedBox = document.querySelectorAll(
                        "[name=hobby]:checked"
                    )

                    // 判断hobbies是否全选
                    if (hobbies.length === checkedBox.length) {
                        checkAllBox.checked = true
                    } else {
                        checkAllBox.checked = false
                    }
                }

                /* 
                    提交按钮
                        - 点击按钮后，将选中的内容显示出来
                */
                const sendBtn = document.getElementById("send")
                sendBtn.onclick = function () {
                    for (let i = 0; i < hobbies.length; i++) {
                        // if (hobbies[i].checked) {
                        //     alert(hobbies[i].value)
                        // }
                        hobbies[i].checked && alert(hobbies[i].value)
                    }
                }

                /* 
                    check-all
                        - 全选checkbox发生变化后，将小的checkbox和它同步
                */

                checkAllBox.onchange = function () {
                    // console.log(this)
                    // 在事件的响应函数中，响应函数绑定给谁 this就是谁（箭头函数除外）

                    for (let i = 0; i < hobbies.length; i++) {
                        hobbies[i].checked = this.checked
                    }
                }

                /* 
                    使全选checkbox和四个checkbox进行同步
                        如果四个全选了，则全选checkbox也选中
                        如果四个没全选，则全选checkbox也不选中
                */

                for (let i = 0; i < hobbies.length; i++) {
                    hobbies[i].onchange = function () {
                        // 获取所有选中的checkbox
                        const checkedBox = document.querySelectorAll(
                            "[name=hobby]:checked"
                        )

                        // 判断hobbies是否全选
                        if (hobbies.length === checkedBox.length) {
                            checkAllBox.checked = true
                        } else {
                            checkAllBox.checked = false
                        }
                    }
                }
            }
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



## 09 DOM的修改

创建标签元素

```javascript
document.createElement("li")
```

向元素中中添加文本             

```javascript
li.textContent = "唐僧"
```

给li添加id属性

```javascript
li.id = "ts"
```



### 添加子节点

```javascript
appendChild()
```

如:

```javascript
const list = document.getElementById("list")
const li = document.createElement("li")
list.appendChild(li)
```



向元素的任意位置添加元素

```
insertAdjacentElement()
insertAdjacentHTML()
```

两个参数：1 要添加的位置 2 要添加的元素

| 参数        | 添加的位置     | 元素关系                           |
| ----------- | -------------- | ---------------------------------- |
| beforeend   | 元素内部的最后 | 新添加的元素成为当前元素的子元素   |
| afterbegin  | 元素内部的开始 | 新添加的元素成为当前元素的子元素   |
| beforebegin | 在元素的前边   | 新添加的元素成为当前元素的兄弟元素 |
| afterend    | 在元素的后边   | 新添加的元素成为当前元素的兄弟元素 |

示例:

```javascript
list.insertAdjacentElement("beforeend", li)
list.insertAdjacentHTML("beforeend", "<li id='bgj'>白骨精</li>")
```



### 替换

使用一个元素替换当前元素

```javascript
replaceWith()
```

如:

```
const swk = document.getElementById("swk")
swk.replaceWith(li)
```



### 删除

删除当前元素

```javascript
remove()
```

如:

```javascript
const swk = document.getElementById("swk")
swk.remove()
```



### 练习: 员工信息表

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

        <script>
            document.addEventListener("DOMContentLoaded", function () {
                /* 
                    点击删除超链接后，删除当前的员工信息
                */

                function delEmpHandler() {
                    // 本练习中的超链接，我们是不希望发生跳转，但是跳转行为是超链接的默认行为
                    // 只要点击超链接就会触发页面的跳转，事件中可以通过取消默认行为来阻止超链接的跳转
                    // 使用return false来取消默认行为，只在 xxx.xxx = function(){}这种形式绑定的事件中才适用
                    // return false

                    // 删除当前员工 删除当前超链接所在的tr
                    // console.log(this)

                    // this表示当前点击的超链接
                    const tr = this.parentNode.parentNode

                    // 获取要删除的员工的姓名
                    // const empName = tr.getElementsByTagName("td")[0].textContent
                    const empName = tr.firstElementChild.textContent

                    // 弹出一个友好的提示
                    if (confirm("确认要删除【" + empName + "】吗？")) {
                        // 删除tr
                        tr.remove()
                    }
                }

                // 获取所有的超链接
                const links = document.links
                // 为他们绑定单级响应函数
                for (let i = 0; i < links.length; i++) {
                    links[i].onclick = delEmpHandler

                    // links[i].addEventListener("click", function(){
                    //     alert(123)
                    //     return false // 无法取消默认行为
                    // })
                }

                /* 
                    点击按钮后，将用户的信息插入到表格中
                */
                // 获取tbody
                const tbody = document.querySelector("tbody")
                const btn = document.getElementById("btn")
                btn.onclick = function () {
                    // 获取用户输入的数据
                    const name = document.getElementById("name").value
                    const email = document.getElementById("email").value
                    const salary = document.getElementById("salary").value

                    // 将获取到的数据设置DOM对象
                    /* 
                        <tr>
                            <td>孙悟空</td>
                            <td>swk@hgs.com</td>
                            <td>10000</td>
                            <td><a href="javascript:;">删除</a></td>
                        </tr>
                    */

                    // 创建元素
                    const tr = document.createElement("tr")

                    // 创建td
                    const nameTd = document.createElement("td")
                    const emailTd = document.createElement("td")
                    const salaryTd = document.createElement("td")

                    // 添加文本
                    nameTd.innerText = name
                    emailTd.textContent = email
                    salaryTd.textContent = salary

                    // 将三个td添加到tr中
                    tr.appendChild(nameTd)
                    tr.appendChild(emailTd)
                    tr.appendChild(salaryTd)
                    tr.insertAdjacentHTML("beforeend", '<td><a href="javascript:;">删除</a></td>')

                    tbody.appendChild(tr)


                    // 由于上边的超链接是新添加的，所以它的上边并没有绑定单级响应函数，所以新添加的员工无法删除
                    // 解决方式：为新添加的超链接单独绑定响应函数
                    links[links.length - 1].onclick = delEmpHandler

                }
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



## 10 节点的复制

```javascript
cloneNode()
```

使用 `cloneNode()` 方法对节点进行复制时，它会复制节点的所有特点包括各种属性
这个方法默认只会复制当前节点，而不会复制节点的子节点
可以传递一个 `true` 作为参数，这样该方法也会将元素的子节点一起复制



示例:

点击按钮后，将id为l1的元素添加list2中

```javascript
const list2 = document.getElementById("list2")
const l1 = document.getElementById("l1")
const btn01 = document.getElementById("btn01")
btn01.onclick = function () {
    const newL1 = l1.cloneNode(true) // 用来对节点进行复制的
    newL1.id = "newL1"
    list2.appendChild(newL1)
}
```



## 11 读取CSS样式

读取样式的方法

```javascript
getComputedStyle()
```

它会返回一个对象，这个对象中包含了当前元素所有的生效的样式
参数：
1 要获取样式的对象
2 要获取的伪元素
返回值：
返回的一个对象，对象中存储了当前元素的样式

注意：
样式对象中返回的样式值，不一定能来拿来直接计算
所以使用时，一定要确保值是可以计算的才去计算

示例:

```javascript
const btn = document.getElementById("btn")
const box1 = document.querySelector(".box1")
    
const styleObj = getComputedStyle(box1)

console.log(styleObj.width)
console.log(styleObj.left)

console.log(parseInt(styleObj.width) + 100)
// parseInt("100px") -> 100 , 注意设置到 box1.style.width 时补上 "px"
box1.style.width = parseInt(styleObj.width) + 100 + "px"

console.log(styleObj.backgroundColor)

// "::before" 伪元素选择器
const beforeStyle = getComputedStyle(box1, "::before")
console.log(beforeStyle.color)

console.log(box1.firstElementChild)
```



其他读取样式的方式:

获取元素内部的宽度和高度（包括内容区和内边距）

```javascript
元素.clientHeight
元素.clientWidth
```



获取元素的可见框的大小（包括内容区、内边距和边框）

```javascript
元素.offsetHeight
元素.offsetWidth
```



获取元素滚动区域的大小

```javascript
元素.scrollHeight
元素.scrollWidth
```



获取元素的定位父元素

```javascript
元素.offsetParent
```

定位父元素：离当前元素最近的开启了定位的祖先元素，
如果所有的元素都没有开启定位则返回body



获取元素相对于其定位父元素的偏移量

```javascript
元素.offsetTop
元素.offsetLeft
```



获取或设置元素滚动条的偏移量

```javascript
元素.scrollTop
元素.scrollLeft
```



示例:

```javascript
const btn = document.getElementById("btn")
const box1 = document.getElementById("box1")

console.log(box1.scrollHeight)
console.log(box1.scrollWidth)

console.log(box1.offsetParent)

console.log(box1.offsetLeft)
console.log(box1.offsetTop)

console.log(box1.scrollTop)
```



## 12 修改css样式

修改样式的方法

```javascript
元素.style.样式名 = 样式值
```

如果样式名中含有-，则需要将样式表修改为驼峰命名法
如: background-color --> backgroundColor

示例:

```javascript
const btn = document.getElementById("btn")
const box1 = document.querySelector(".box1")

btn.onclick = function () {
    box1.style.width = "400px"
    box1.style.height = "400px"
    // background-color -> backgroundColor
    box1.style.backgroundColor = "yellow"
}
```



除了直接修改样式外，也可以通过修改class属性来间接的修改样式

通过class修改样式的好处：
1 可以一次性修改多个样式
2 对JS和CSS进行解耦

如:

```javascript
box1.className += " box2"
```



`元素.classList` 是一个对象，对象中提供了对当前元素的类的各种操作方法



向元素中添加一个或多个class

```javascript
元素.classList.add()
```

```javascript
box1.classList.add("box2", "box3", "box4")
box1.classList.add("box1")
```



移除元素中的一个或多个class

```javascript
元素.classList.remove() 
```

```javascript
box1.classList.remove("box2")
```



切换元素中的class

```javascript
元素.classList.toggle()
```

```javascript
box1.classList.toggle("box2")
```



替换class

```javascript
元素.classList.replace() 
```

```javascript
box1.classList.replace("box1", "box2")
```



检查class

```javascript
元素.classList.contains()
```

```javascript
let result = box1.classList.contains("box3")
console.log(result)
```



## 13 事件对象

event 事件
事件对象是有浏览器在事件触发时所创建的对象，这个对象中封装了事件相关的各种信息,  通过事件对象可以获取到事件的详细信息
比如：鼠标的坐标、键盘的按键..
浏览器在创建事件对象后，会将事件对象作为响应函数的参数传递，
所以我们可以在事件的回调函数中定义一个形参来接收事件对象

示例:

```javascript
const box1 = document.getElementById("box1")

box1.onmousemove = event => {
    console.log(event)
}

box1.addEventListener("mousemove", event => {
    console.log(event.clientX, event.clientY)
    box1.textContent = event.clientX + "," + event.clientY
})
```



在DOM中存在着多种不同类型的事件对象, 多种事件对象有一个共同的祖先 Event

event.target 触发事件的对象
event.currentTarget 绑定事件的对象（同this）
event.stopPropagation() 停止事件的传导
event.preventDefault() 取消默认行为



事件的冒泡（bubble）
事件的冒泡就是指事件的向上传导
当元素上的某个事件被触发后，其祖先元素上的相同事件也会同时被触发
冒泡的存在大大的简化了代码的编写，但是在一些场景下我们并不希望冒泡存在
不希望事件冒泡时，可以通过事件对象来取消冒泡

示例:

```html
<div id="box1">
    <div id="box2">
        <div id="box3"></div>
    </div>
</div>
<a id="chao" href="https://lilichao.com">超链接</a>
```

样式: 略

```javascript
const box1 = document.getElementById("box1")
const box2 = document.getElementById("box2")
const box3 = document.getElementById("box3")
const chao = document.getElementById("chao")

chao.addEventListener("click", (event) => {
    event.preventDefault() // 取消默认行为
    alert("被点了~~~")
})

box1.addEventListener("click", function (event) {
    alert(event)
    /*
    在事件的响应函数中：
        event.target 表示的是触发事件的对象
        this 绑定事件的对象
    */
    console.log(event.target)
    console.log(this)
    console.log(event.currentTarget)
    alert("Hello 我是box1")
})

box2.addEventListener("click", function(event){
    event.stopPropagation()
    alert("我是box2")
})

box3.addEventListener("click", function(event){
    event.stopPropagation() // 取消事件的传到
    alert("我是box3")
})
```



## 14 冒泡

使小绿球可以跟随鼠标一起移动
事件的冒泡和元素的样式无关，之和结构相关

```html
<div id="box1"></div>

<div id="box2"></div>

<div id="box3" onclick="alert(3)">
    <div id="box4" onclick="alert(4)"></div>
</div>
```

```javascript
const box1 = document.getElementById("box1")
const box2 = document.getElementById("box2")

document.addEventListener("mousemove", (event) => {
    box1.style.left = event.x + "px"
    box1.style.top = event.y + "px"
})

box2.addEventListener("mousemove", event => {
    event.stopPropagation()
})
```



## 15 事件的委派

需求：
只绑定一次事件，既可以让所有的超链接，包括当前的和未来新建的超链接都具有这些事件

思路：
可以将事件统一绑定给document，这样点击超链接时由于事件的冒泡，
会导致document上的点击事件被触发，这样只绑定一次，所有的超链接都会具有这些事件

委派就是将本该绑定给多个元素的事件，统一绑定给document，这样可以降低代码复杂度方便维护

```html
<button id="btn">点我一下</button>
<hr />
<ul id="list">
    <li><a href="javascript:;">链接一</a></li>
    <li><a href="javascript:;">链接二</a></li>
    <li><a href="javascript:;">链接三</a></li>
    <li><a href="javascript:;">链接四</a></li>
</ul>
```

```javascript
const list = document.getElementById("list")
const btn = document.getElementById("btn")

// 获取list中的所有链接
const links = list.getElementsByTagName("a")

document.addEventListener("click", (event) => {
    // 在执行代码前，先来判断一下事件是由谁触发
    // 检查event.target 是否在 links 中存在
    console.log(Array.from(links))
    if ([...links].includes(event.target)) {
        alert(event.target.textContent)
    }
})

// 点击按钮后，在ul中添加一个新的li
btn.addEventListener("click", () => {
    list.insertAdjacentHTML(
        "beforeend",
        "<li><a href='javascript:;'>新超链接</a></li>"
    )
})
```



## 16 事件的捕获

### 事件的传播机制

在DOM中，事件的传播可以分为三个阶段：
1.捕获阶段 （由祖先元素向目标元素进行事件的捕获）（默认情况下，事件不会在捕获阶段触发）
2.目标阶段 （触发事件的对象）
3.冒泡阶段 （由目标元素向祖先元素进行事件的冒泡）



事件的捕获，指事件从外向内的传导，
当前元素触发事件以后，会先从当前元素最大的祖先元素开始向当前元素进行事件的捕获



如果希望在捕获阶段触发事件，可以将 `addEventListener` 的第三个参数设置为true
一般情况下我们不希望事件在捕获阶段触发，所有通常都不需要设置第三个参数



事件触发的阶段

```javascript
event.eventPhase
```

1 : 捕获阶段 

2 : 目标阶段 

3 : 冒泡阶段



示例:

```html
<div id="box1">
    <div id="box2">
        <div id="box3"></div>
    </div>
</div>
```

样式略

```javascript
const box1 = document.getElementById("box1")
const box2 = document.getElementById("box2")
const box3 = document.getElementById("box3")

box1.addEventListener("click", event => {
    alert("1" + event.eventPhase) // eventPhase 表示事件触发的阶段
    //1 捕获阶段 2 目标阶段 3 冒泡阶段
})

box2.addEventListener("click", event => {

    alert("2" + event.eventPhase)
})

box3.addEventListener("click", event => {
    alert("3" + event.eventPhase)
})
```

