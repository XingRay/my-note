# Windows下安装使用curl命令



curl命令网络应用curl命令是一个利用URL规则在命令行下工作的文件传输工具。

CURL支持的通信协议有FTP、FTPS、HTTP、HTTPS、TFTP、SFTP、Gopher、SCP、Telnet、DICT、FILE、LDAP、LDAPS、IMAP、POP3、SMTP和RTSP。

Windows安装：

1.在官方网址下载windows版本的curl工具文件

下载直通车：https://curl.se/windows/

官方地址：curl - Download



2.下载完成，解压压缩包文件  ，直接在文件下夹bin目录，执行cmd命令



 进入到bin目录



 测试:

curl http://www.baidu.com
执行结果：

ok ,至此就结束了，若是想不行在特定文件夹执行，为方便使用，可以将bin文件夹添加到“环境变量”，cmd中随时执行。

一、最常用的curl命令
1、发送GET请求

```bash
curl URL
```

```bash
curl URL?a=1&b=nihao
```

2、发送POST请求

```bash
curl -X POST -d 'a=1&b=nihao' URL
```

3、发送json格式请求：

```bash
curl -H "Content-Type: application/json" -X POST -d '{"abc":123,"bcd":"nihao"}' URL
```

```bash
curl -H "Content-Type: application/json" -X POST -d @test.json URL
```

其中，-H代表header头，-X是指定什么类型请求(POST/GET/HEAD/DELETE/PUT/PATCH)，-d代表传输什么数据。这几个是最常用的。

查看所有curl命令： man curl或者curl -h
请求头：H,A,e
响应头：I,i,D
cookie：b,c,j
传输：F(POST),G(GET),T(PUT),X
输出：o,O,w
断点续传：r
调试：v,--trace,--trace-ascii,--trace-time

具体命令参考：curl命令最全详解_Angel_CG的博客-CSDN博客_curl命令 命令大全

二、curl不仅可以测试网络请求，还可以测试端口
可以用它来测试端口是否开启。

用法：

```bash
curl -v ip:port
```

#出现Connection refused表示端口关闭；

#出现Connected to ip(ip) port(#0)表示端口开启；

#出现No route to host表示IP错误或者iptables限制。