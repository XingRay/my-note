# HTTP协议

## 网络基础

网络的服务器基于请求和响应的

如:  https://lilichao.com/hello/index.html

`https://`部分为协议名, 比如还有:  `http` `ftp` ...

`lilichao.com`部分为域名 domain,  整个网络中存在着无数个服务器，每一个我服务器都有它自己的唯一标识, 这个标识被称为 ip地址 192.168.1.17，但是ip地址不方便记忆, 域名就相当于是ip地址的别名

`/hello/index.html` 部分为 网站资源路径



## 1 当在浏览器中输入地址以后发生了什么？

https://lilichao.com/hello/index.html

① DNS解析，获取网站的ip地址
② 浏览器需要和服务器建立连接（tcp/ip）（三次握手）
③ 向服务器发送请求（http协议）
④ 服务器处理请求，并返回响应（http协议）
⑤ 浏览器将响应的页面渲染
⑥ 断开和服务器的连接（四次挥手）



## 2 客户端如何和服务器建立（断开）连接

通过三次握手和四次挥手



### 三次握手（建立连接）

三次握手是客户端和服务器建立连接的过程



1 客户端向服务器发送连接请求
SYN



2 服务器收到连接请求，向客户端返回消息
SYN ACK 



3 客户端向服务器发送同意连接的信息
ACK



### 四次挥手（断开连接）

1 客户端向服务器发送请求，通过之服务器数据发送完毕，请求断开来接
FIN



2 服务器向客户端返回数据，知道了
ACK



3 服务器向客户端返回数据，收完了，可以断开连接
FIN ACK



4 客户端向服务器发数据，可以断开了
ACK



请求和响应实际上就是一段数据，只是这段数据需要遵循一个特殊的格式，这个特殊的格式由HTTP协议来规定



## TCP/IP 协议族

TCP/IP协议族中包含了一组协议
这组协议规定了互联网中所有的通信的细节
网络通信的过程由四层组成

| 网络通信层 | 说明                                           |
| ---------- | ---------------------------------------------- |
| 应用层     | 软件的层面，浏览器 服务器都属于应用层          |
| 传输层     | 负责对数据进行拆分，把大数据拆分为一个一个小包 |
| 网络层     | 负责给数据包，添加信息                         |
| 数据链路层 | 传输信息                                       |



## http

HTTP协议就是应用层的协议，
用来规定客户端和服务器间通信的报文格式的



### 报文（message）

浏览器和服务器之间通信是基于请求和响应的
浏览器向服务器发送请求（request）
服务器向浏览器返回响应（response）
浏览器向服务器发送请求相当于浏览器给服务器写信
服务器向浏览器返回响应，相当于服务器给浏览器回信
这个信在HTTP协议中就被称为报文
而HTTP协议就是对这个报文的格式进行规定



服务器

一个服务器的主要功能：
1.可以接收到浏览器发送的请求报文
2.可以向浏览器返回响应报文



### 请求报文（request）

客户端发送给服务器的报文称为请求报文
请求报文的格式如下：

```shell
请求首行
请求头
空行
请求体
```



#### 请求首行

请求首行就是请求报文的第一行

```shell
GET /index.html?username=sunwukong HTTP/1.1
```



##### 第一部分 

**GET** /index.html?username=sunwukong HTTP/1.1

get

表示**请求的方式**，get表示发送的是get请求
现在常用的方式就是get和post请求
get请求主要用来向服务器请求资源
post请求主要用来向服务器发送数据



##### 第二部分 

GET **/index.html?username=sunwukong** HTTP/1.1

/index.html?username=sunwukong
表示**请求资源的路径**，

? 后边的内容叫做**查询字符串**
查询字符串是一个名值对结构，一个名字对应一个值
使用=连接，多个名值对之间使用&分割
username=admin&password=123123



get请求通过查询字符串将数据发送给服务器
由于查询字符串会在浏览器地址栏中直接显示
所以，它安全性较差
同时，由于url地址长度有限制，所以get请求无法发送较大的数据



post请求通过请求体来发送数据
在chrome中通过 载荷 可以查看
post请求通过请求体发送数据，无法在地址栏直接查看
所以安全性较好
请求体的大小没有限制，可以发送任意大小的数据



如果你需要向服务器发送数据，能用post尽量使用post



##### 第三部分

GET /index.html?username=sunwukong **HTTP/1.1**

HTTP/1.1 协议的版本





#### 请求头

请求头也是名值对结构，用来告诉服务器我们浏览器的信息
每一个请求头都有它的作用：

| 请求头          | 作用                                         |
| --------------- | -------------------------------------------- |
| Accept          | 浏览器可以接受的文件类型                     |
| Accept-Encoding | 浏览器允许的压缩的编码                       |
| User-Agent      | 用户代理，它是一段用来描述浏览器信息的字符串 |



#### 空行

用来分隔请求头和请求体



#### 请求体

post请求通过请求体来发送数据


网页、css、 js、图片这些资源会作为响应报文中的哪一部分发送



### 响应报文：

响应首行
响应头
空行
响应体



#### 响应首行

HTTP/1.1 200 OK



200 响应状态码
ok 对响应状态码的描述



响应状态码的规则

| 响应状态码 | 含义             |
| ---------- | ---------------- |
| 1xx        | 请求处理中       |
| 2xx        | 表示成功         |
| 3xx        | 表示请求的重定向 |
| 4xx        | 表示客户端错误   |
| 5xx        | 表示服务器的错误 |



#### 响应头

响应头也是一个一个的名值对结构，用来告诉浏览器响应的信息

如: 

Content-Type: text/html; charset=UTF-8

Content-Length: 2017

| 响应头         | 描述                 |
| -------------- | -------------------- |
| Content-Type   | 用来描述响应体的类型 |
| Content-Length | 用来描述响应体大小   |



#### 空行

空行用来分隔响应头和响应体



#### 响应体

响应体就是服务器返回给客户端的内容



### 示例

请求报文：  

```
GET /07_http%E5%8D%8F%E8%AE%AE/01_http%E5%8D%8F%E8%AE%AE.html?username=sunwukong HTTP/1.1
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
Accept-Encoding: gzip, deflate, br
Accept-Language: zh-CN,zh;q=0.9,en;q=0.8,la;q=0.7
Cache-Control: max-age=0
Connection: keep-alive
Host: 127.0.0.1:5500
If-Modified-Since: Thu, 27 Oct 2022 11:10:28 GMT
If-None-Match: W/"1b8-1841922e832"
Referer: http://127.0.0.1:5500/07_http%E5%8D%8F%E8%AE%AE/01_http%E5%8D%8F%E8%AE%AE.html?username=sunwukong
Sec-Fetch-Dest: document
Sec-Fetch-Mode: navigate
Sec-Fetch-Site: same-origin
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/106.0.0.0 Safari/537.36
sec-ch-ua: "Chromium";v="106", "Google Chrome";v="106", "Not;A=Brand";v="99"
sec-ch-ua-mobile: ?0
sec-ch-ua-platform: "Windows"
```

响应报文：

```
HTTP/1.1 200 OK
Vary: Origin
Access-Control-Allow-Credentials: true
Accept-Ranges: bytes
Cache-Control: public, max-age=0
Last-Modified: Thu, 27 Oct 2022 11:31:54 GMT
ETag: W/"20c-18419368696"
Content-Type: text/html; charset=UTF-8
Content-Length: 2017
Date: Thu, 27 Oct 2022 11:52:29 GMT
Connection: keep-alive
Keep-Alive: timeout=5
```

