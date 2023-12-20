# ajax和fetch

## mvc模式

传统的服务器的结构是基于MVC模式

Model -- 数据模型
View -- 视图，用来呈现
Controller -- 控制器，复杂加载数据并选择视图来呈现数据



传统的服务器是直接为客户端返回一个页面
但是传统的服务器并不能适用于现在的应用场景

现在的应用场景，一个应用通常都会有多个客户端（client）存在
web端    移动端（app）    pc端  

如果服务器直接返回html页面，那么服务器就只能为web端提供服务
其他类型的客户端还需要单独开发服务器，这样就提高了开发和维护的成本



如何解决这个问题？
传统的服务器需要做两件事情，第一个加载数据，第二个要将模型渲染进视图
解决方案就将渲染视图的功能从服务器中剥离出来，
服务器只负责向客户端返回数据，渲染视图的工作由客户端自行完成
分离以后，服务器只提供数据，一个服务器可以同时为多种客户端提供服务器
同时将视图渲染的工作交给客户端以后，简化了服务器代码的编写



## Rest

REpresentational State Transfer 
表示层状态的传输

Rest实际上就是一种服务器的设计风格
它的主要特点就是，服务器只返回数据
服务器和客户端传输数据时通常会使用JSON作为数据格式



请求的方法：
GET    加载数据
POST   新建或添加数据
PUT    添加或修改数据
PATCH  修改数据
DELETE 删除数据
OPTION 由浏览器自动发送，检查请求的一些权限
API（接口） Endpoint（端点）
GET /user
POST /user
DELETE /user/:id
...



## AJAX

**A**synchronous **J**avascript **A**nd **X**ML, 异步JavaScript和XML

在js中向服务器发送的请求加载数的技术叫AJAX,  它的作用就是通过js向服务器发送请求来加载数据,  xml是早期AJAX使用的数据格式

```xml
<student>
<name>孙悟空</name>
</student>
```

目前数据格式都使用json

```json
{"name" :"孙悟空"}
```



可以选择的方案：
① XMLHTTPRequest（xhr）
② Fetch
③ Axios



## CORS (跨域资源共享)

https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS



跨域请求
如果两个网站的完整的域名不相同
a网站：http://haha.com
b网站：http://heihei.com
跨域需要检查三个东西：
协议 域名 端口号
http://localhost:5000
http://127.0.0.1:5000
三个只要有一个不同，就算跨域

当我们通过AJAX去发送跨域请求时，
浏览器为了服务器的安全，会阻止JS读取到服务器的数据

解决方案
在服务器中设置一个允许跨域的头

```
Access-Control-Allow-Origin
```

允许那些客户端访问我们的服务器
如在express服务器中:

```javascript
app.use((req, res) => {
    // 设置响应头
    res.setHeader("Access-Control-Allow-Origin", "*")
    res.setHeader("Access-Control-Allow-Methods", "GET,POST")
    res.setHeader("Access-Control-Allow-Headers", "Content-type")
    // Access-Control-Allow-Origin 设置指定值时只能设置一个
    // res.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500")
    // Access-Control-Allow-Methods 允许的请求的方式
    // Access-Control-Allow-Headers 允许传递的请求头
})
```



## xhr

创建一个新的xhr对象，xhr表示请求信息

```javascript
// 创建一个xhr对象
const xhr = new XMLHttpRequest()

// 设置响应体的类型，设置后会自动对数据进行类型转换
xhr.responseType = "json"

// 可以为xhr对象绑定一个load事件
xhr.onload = function () {
    // xhr.status 表示响应状态码
    console.log(xhr.status)
    if (xhr.status === 200) {
        // xhr.response 表示响应信息
        // const result = JSON.parse(xhr.response)
        // console.log(result.status, result.data)
        // 读取响应信息
        console.log(xhr.response)
        const result = xhr.response
        // 判断数据是否正确
        if (result.status === "ok") {
            // 创建一个ul
            const ul = document.createElement("ul")
            // 将ul插入到root中
            root.appendChild(ul)
            // 遍历数据
            for (let stu of result.data) {
                ul.insertAdjacentHTML(
                    "beforeend",
                    `<li>${stu.id} - ${stu.name} - ${stu.age} - ${stu.gender} - ${stu.address}</li>`
                )
            }
        }
    }
}

// 设置请求的信息
xhr.open("get", "http://localhost:3000/students")
// 发送请求
xhr.send()
```



## fetch

fetch是xhr的升级版，采用的是Promise API
作用和AJAX是一样的，但是使用起来更加友好
fetch原生js就支持的一种ajax请求的方式

```javascript
fetch("http://localhost:3000/students")
        .then((res) => {
            if(res.status === 200){
                // res.json() 可以用来读取json格式的数据
                return res.json()
            }else{
                throw new Error("加载失败！")
            }
        })
        .then(res => {
            // 获取到数据后，将数据渲染到页面中
            if(res.status === "ok"){
                console.log(res.data)
                // ...
            }
        })
        .catch((err) => {
            console.log("出错了！", err)
        })
```



发送post请求

```javascript
fetch("http://localhost:3000/students", {
        method: "post",
        headers:{
            // application/x-www-form-urlencoded
            "Content-type":"application/json"
        },

        // 通过body去发送数据时，必须通过请求头来指定数据的类型
        body: JSON.stringify({
            name: "白骨精",
            age: 16,
            gender: "女",
            address: "白骨洞"
        })
    }).then(...)
```



fetch 请求的取消

```javascript
let controller

btn01.onclick = () => {
    // 创建一个AbortController
    controller = new AbortController()
    // setTimeout(()=>{
    //     controller.abort()
    // }, 3000)

    // 终止请求
    // 点击按钮向test发送请求
    fetch("http://localhost:3000/test", {
        signal: controller.signal
    })
        .then((res) => console.log(res))
        .catch((err) => console.log("出错了", err))
}

btn02.onclick = () => {
    controller && controller.abort()
}
```



async-await 写法

注意：将promise改写为await时，一定要写try-catch

```javascript
btn03.onclick = async () => {
    // fetch("http://localhost:3000/test").then()...
    try {
        const res = await fetch("http://localhost:3000/students")
        const data = await res.json()
        console.log(data)
    } catch (e) {
        console.log("出错了", e)
    }
}
```



登录示例:

点击login-btn后实现登录功能

```javascript
// 点击login-btn后实现登录功能
const loginBtn = document.getElementById("login-btn")
const root = document.getElementById("root")
loginBtn.onclick = () => {
    // 获取用户输入的用户名和密码
    const username = document
        .getElementById("username")
        .value.trim()
    const password = document
        .getElementById("password")
        .value.trim()

    // 调用fetch发送请求来完成登录
    fetch("http://localhost:3000/login", {
        method: "POST",
        headers: {
            "Content-type": "application/json"
        },
        body: JSON.stringify({ username, password })
    })
        .then((res) => res.json())
        .then((res) => {

            if(res.status !== "ok"){
                throw new Error("用户名或密码错误")
            }

            // console.log(res)
            // 登录成功
            root.innerHTML = `
                            <h1>欢迎 ${res.data.nickname} 回来！</h1>
                            <hr>
                            <button id="load-btn">加载数据</button>
                        `
        })
        .catch((err) => {
            console.log("出错了！", err)
            // 这里是登录失败
            document.getElementById("info").innerText = "用户名或密码错误"
        })
}
```

登录案例中的问题：
现在是登录以后直接将用户信息存储到了 localStorage 
主要存在两个问题：
1 数据安全问题
2 服务器不知道你有没有登录

```javascript
// 点击login-btn后实现登录功能
const loginBtn = document.getElementById("login-btn")
const root = document.getElementById("root")

function loadData() {
    fetch("http://localhost:3000/students")
        .then((res) => {
            if (res.status === 200) {
                // res.json() 可以用来读取json格式的数据
                return res.json()
            } else {
                throw new Error("加载失败！")
            }
        })
        .then((res) => {
            // 获取到数据后，将数据渲染到页面中
            if (res.status === "ok") {
                // 创建一个table
                const dataDiv = document.getElementById("data")
                const table = document.createElement("table")
                dataDiv.appendChild(table)
                table.insertAdjacentHTML(
                    "beforeend",
                    "<caption>学生列表</caption>"
                )
                table.insertAdjacentHTML(
                    "beforeend",
                    `
                                <thead>
                                    <tr>
                                        <th>学号</th>    
                                        <th>姓名</th>    
                                        <th>年龄</th>    
                                        <th>性别</th>    
                                        <th>地址</th>    
                                    </tr> 
                                </thead>
                            `
                )

                const tbody = document.createElement("tbody")
                table.appendChild(tbody)

                // 遍历数据
                for (let stu of res.data) {
                    tbody.insertAdjacentHTML(
                        "beforeend",
                        `
                                    <tr>
                                        <td>${stu.id}</td>    
                                        <td>${stu.name}</td>    
                                        <td>${stu.age}</td>    
                                        <td>${stu.gender}</td>    
                                        <td>${stu.address}</td>    
                                    </tr>
                                `
                    )
                }
            }
        })
        .catch((err) => {
            console.log("出错了！", err)
        })
}

// 判断用户是否登录
if (localStorage.getItem("nickname")) {
    // 用户已经登录
    // 登录成功
    root.innerHTML = `
                            <h1>欢迎 ${localStorage.getItem(
        "nickname"
    )} 回来！</h1>
                            <hr>
                            <button id="load-btn" onclick="loadData()">加载数据</button>
                            <hr>
                            <div id="data"></div>
                        `
} else {
    loginBtn.onclick = () => {
        // 获取用户输入的用户名和密码
        const username = document
            .getElementById("username")
            .value.trim()
        const password = document
            .getElementById("password")
            .value.trim()

        // 调用fetch发送请求来完成登录
        fetch("http://localhost:3000/login", {
            method: "POST",
            headers: {
                "Content-type": "application/json"
            },
            body: JSON.stringify({ username, password })
        })
            .then((res) => res.json())
            .then((res) => {
                if (res.status !== "ok") {
                    throw new Error("用户名或密码错误")
                }

                // 登录成功以后，需要保持用户的登录的状态，需要将用户的信息存储到某个地方
                // 需要将用户信息存储到本地存储
                /*
                所谓的本地存储就是指浏览器自身的存储空间，
                    可以将用户的数据存储到浏览器内部
                    sessionStorage 中存储的数据 页面一关闭就会丢失
                    localStorage 存储的时间比较长
            */

                // sessionStorage
                // localStorage
                // console.log(res)
                // 登录成功，向本地存储中插入用户的信息
                localStorage.setItem("username", res.data.username)
                localStorage.setItem("userId", res.data.id)
                localStorage.setItem("nickname", res.data.nickname)

                // 登录成功
                root.innerHTML = `
                            <h1>欢迎 ${res.data.nickname} 回来！</h1>
                            <hr>
                            <button id="load-btn" onclick="loadData()">加载数据</button>
                            <hr>
                            <div id="data"></div>
                        `
            })
            .catch((err) => {
                console.log("出错了！", err)
                // 这里是登录失败
                document.getElementById("info").innerText =
                    "用户名或密码错误"
            })
    }
}
```



## localStorage

存储数据

```javascript
localStorage.setItem("key", "value") 
```



获取数据

```javascript
let value = localStorage.getItem("key")
```



删除数据

```javascript
localStorage.removeItem("key") 
```



清空数据

```javascript
localStorage.clear() 
```



```javascript
localStorage.setItem("name", "孙悟空")
localStorage.setItem("age", "18")
localStorage.setItem("gender", "男")
localStorage.setItem("address", "花果山")
    
const name = sessionStorage.getItem("name")
console.log(name)
sessionStorage.removeItem("name")
sessionStorage.clear()
```



## jsonwebtoken

现在是登录以后直接将用户信息存储到了 localStorage



主要存在两个问题：
1.数据安全问题
2.服务器不知道你有没有登录



解决问题：
如何告诉服务器客户端的登录状态
rest风格的服务器是无状态的服务器，所以注意不要在服务器中存储用户的数据
服务器中不能存储用户信息，可以将用户信息发送给客户端保存
比如：

```json
{id:"xxx", username:"xxx", email:"xxx"}
```



客户端每次访问服务器时，直接将用户信息发回，服务器就可以根据用户信息来识别用户的身份
但是如果将数据直接发送给客户端同样会有数据安全的问题，
所以我们必须对数据进行加密，加密以后在发送给客户端保存，这样即可避免数据的泄露



在node中( 服务端 )可以直接使用 jsonwebtoken 这个包来对数据进行加密
jsonwebtoken（jwt） --> 通过对json加密后，生成一个web中使用的令牌
使用步骤：



1 安装

```javascript
yarn add jsonwebtoken
```



2 引入

```javascript
const jwt = require("jsonwebtoken")
```



3 使用

服务端:

```javascript
// 定义一个登录的路由
app.post("/login", (req, res) => {
    // 获取用户输入的用户名和密码
    const { username, password } = req.body
    // 验证用户名和密码
    if (username === "admin" && password === "123123") {
        // 登录成功，生成token
        const token = jwt.sign(
            {
                id: "12345",
                username: "admin",
                nickname: "超级管理员"
            },
            // 自定义加密密钥, 一定不能泄露, 生产环境使用配置文件设置, 这里只是做演示
            "chaojianquanmima",
            // 配置项
            {
                // 过期时间
                expiresIn: "1d"
            }
        )

        // 登录成功
        res.send({
            status: "ok",
            data: {
                token,
                nickname: "超级管理员"
            }
        })
    } else {
        // 登录失败
        res.status(403).send({
            status: "error",
            data: "用户名或密码错误"
        })
    }
})
```



客户端 : 

```javascript
// 点击login-btn后实现登录功能
const loginBtn = document.getElementById("login-btn")
const root = document.getElementById("root")

function loadData() {

    // 当我们访问的是需要权限的api时，必须在请求中附加权限的信息
    // token一般都是通过请求头来发送
    const token = localStorage.getItem("token")
    fetch("http://localhost:3000/students", {
        headers:{
            // "Bearer xxxxxx"
            "Authorization":`Bearer ${token}`
        }
    })
        .then((res) => {
            if (res.status === 200) {
                // res.json() 可以用来读取json格式的数据
                return res.json()
            } else {
                throw new Error("加载失败！")
            }
        })
        .then((res) => {
            // 获取到数据后，将数据渲染到页面中
            if (res.status === "ok") {
                console.log(res.data)
                // ...
            }
        })
        .catch((err) => {
            console.log("出错了！", err)
        })
}

// 判断用户是否登录
if (localStorage.getItem("token")) {
    // 用户已经登录
    // 登录成功
    root.innerHTML = `
                            <h1>欢迎 ${localStorage.getItem(
        "nickname"
    )} 回来！</h1>
                            <hr>
                            <button id="load-btn" onclick="loadData()">加载数据</button>
                            <button onclick="localStorage.clear()">注销</button>
                            <hr>
                            <div id="data"></div>
                        `
} else {
    loginBtn.onclick = () => {
        // 获取用户输入的用户名和密码
        const username = document
            .getElementById("username")
            .value.trim()
        const password = document
            .getElementById("password")
            .value.trim()

        // 调用fetch发送请求来完成登录
        fetch("http://localhost:3000/login", {
            method: "POST",
            headers: {
                "Content-type": "application/json"
            },
            body: JSON.stringify({ username, password })
        })
            .then((res) => res.json())
            .then((res) => {
                if (res.status !== "ok") {
                    throw new Error("用户名或密码错误")
                }

                // 登录成功以后，需要保持用户的登录的状态，需要将用户的信息存储到某个地方
                // 需要将用户信息存储到本地存储
                /*
                所谓的本地存储就是指浏览器自身的存储空间，
                    可以将用户的数据存储到浏览器内部
                    sessionStorage 中存储的数据 页面一关闭就会丢失
                    localStorage 存储的时间比较长
            */

                // sessionStorage
                // localStorage
                // console.log(res)
                // 登录成功，向本地存储中插入用户的信息
                localStorage.setItem("token", res.data.token)
                localStorage.setItem("nickname", res.data.nickname)

                // 登录成功
                root.innerHTML = `
                            <h1>欢迎 ${res.data.nickname} 回来！</h1>
                            <hr>
                            <button id="load-btn" onclick="loadData()">加载数据</button>
                            <button onclick="localStorage.clear()">注销</button>
                            <hr>
                            <div id="data"></div>
                        `
            })
            .catch((err) => {
                console.log("出错了！", err)
                // 这里是登录失败
                document.getElementById("info").innerText =
                    "用户名或密码错误"
            })
    }
}
```



## axios

### 发送请求

直接调用axios发送请求

```javascript
axios(config)
```

示例:

```javascript
// 直接调用axios发送请求
    // axios(config)
    axios({
        method: "get",
        url: "http://localhost:3000/students",

    })
        .then((result) => {
            // axios默认只会在响应状态为2xx时才会调用then
            // result是axios封装过
            console.log(result.data)
        })
        .catch((err) => {
            console.log("出错了！", err)
        })
```



```javascript
axios({
        method: "post",
        url: "http://localhost:3000/students",
        data: {
            name: "唐僧",
            age: 18,
            gender: "男",
            address: "女儿国"
        } // 请求参数

        // data:"name=swk&age=18"
    })
        .then((result) => {
            // result是axios封装过
            console.log(result.data)
        })
        .catch((err) => {
            console.log("出错了！", err)
        })
```



### 配置对象

```javascript
document.getElementById("btn1").onclick = () => {
    // 直接调用axios发送请求
    // axios(config)
    axios({
        // baseURL 指定服务器的根目录（路径的前缀）
        baseURL:"http://localhost:3000",
        // 请求地址
        url:"test",

        // 请求方法，默认是get
        method:"get",

        // 指定请求头
        // headers:{"Content-type":"application/json"}

        // 请求体
        // data:"name=唐僧&age=16"
        data: {
            name: "唐僧",
            age: 18,
            gender: "男",
            address: "女儿国"
        },

        // params 用来指定路径中的查询字符串
        params:{
            id:1,
            name:"swk"
        },

        //timeout 过期时间
        timeout:1000,

        // 用来终止请求
        // signal

        // transformRequest 可以用来处理请求数据（data）
        // 它需要一个数组作为参数，数组可以接收多个函数，请求发送时多个函数会按照顺序执行
        // 函数在执行时，会接收到两个参数 data 和 headers
        transformRequest:[function(data, headers){
            // 可以在函数中对data和headers进行修改
            data.name = "猪八戒"
            headers["Content-Type"] = "application/json"
            return data
        }, function(data, headers){
            // 最后一个函数必须返回一个字符串，才能使得数据有效
            return JSON.stringify(data)
        }]

    })
        .then((result) => {
            // result是axios封装过
            console.log(result.data)
        })
        .catch((err) => {
            console.log("出错了！", err)
        })
}
```



默认配置

通过 axios.defaults 修改默认配置

```javascript
axios.defaults.baseURL = "http://localhost:3000"
            axios.defaults.headers.common[
                "Authorization"
            ] = `Bearer ${localStorage.getItem("token")}`

            document.getElementById("btn1").onclick = () => {
                axios({
                    url: "students",
                    method: "post",
                    data: {
                        name: "唐僧",
                        age: 18,
                        gender: "男",
                        address: "女儿国"
                    },

                    timeout: 1000
                })
                    .then((result) => {
                        // result是axios封装过
                        console.log(result.data)
                    })
                    .catch((err) => {
                        console.log("出错了！", err)
                    })
            }
```



### axios实例

axios实例相当于是axios的一个副本，它的功能和axios一样
axios的默认配置在实例也同样会生效
但是我可以单独修改axios实例的默认配置

```javascript
axios.defaults.baseURL = "http://localhost:3000"
axios.defaults.headers.common[
    "Authorization"
    ] = `Bearer ${localStorage.getItem("token")}`
    
```

```javascript
const instance = axios.create({
    baseURL:"http://localhost:4000"
})
```

或者

```javascript
const instance = axios.create()
instance.defaults.baseURL = "xxx"
```



使用 axios 实例

```javascript
document.getElementById("btn1").onclick = () => {
    instance
        .get("students")
        .then((res) => console.log(res.data))
        .catch((err) => {
            console.log("出错了", err)
        })
}
```



### 响应拦截器

axios的拦截器可以对请求或响应进行拦截，在请求发送前和响应读取前处理数据
拦截器只对当前的实例有效

添加请求拦截器

```javascript
axios.interceptors.response.use(function (response) {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    return response;
}, function (error) {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    return Promise.reject(error);
});
```

示例:

```javascript
axios.interceptors.request.use(
    function (config) {
        // console.log("拦截器执行了")
        // config 表示axios中的配置对象
        // config.data.name = "猪哈哈"
        config.headers["Authorization"] = `Bearer ${localStorage.getItem("token")}`

        // 在发送请求之前做些什么
        return config
    },
    function (error) {
        // 对请求错误做些什么
        return Promise.reject(error)
    }
)

document.getElementById("btn1").onclick = () => {
    axios({
        url: "students",
        method: "post",
        data: {name: "猪八戒"}
    })
        .then((res) => console.log(res.data))
        .catch((err) => {
            console.log("出错了", err)
        })
}
```

