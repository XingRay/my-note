Nginx学习笔记

Huathy-雨落江南，浮生若梦

于 2022-08-28 23:57:47 发布

683
 收藏 1
分类专栏： Java学习随笔 文章标签： nginx 学习 运维
版权

Java学习随笔
专栏收录该内容
37 篇文章0 订阅
订阅专栏
简介与环境搭建
简介
Nginx (engine x) 是一个高性能的HTTP和反向代理web服务器，同时也提供了IMAP/POP3/SMTP服务。
功能：正向代理，反向代理，负载均衡，动静分离。

正向代理：VPN。客户端通过代理服务器请求资源服务器。代理客户端。
反向代理：代理服务器资源的代理，称为反向代理。代理服务器端。
负载均衡：内置策略（轮询、加权轮询，ip hash）

下载与安装
Nginx官网下载地址：http://nginx.org/en/download.html
下载之后解压，就可以运行。

# 启动Nginx
start nginx
# 停止Nginx
nginx -s stop	/  nginx -s quit
# 重新载入配置
nginx -s reload
# 重新打开日志
nginx -s reopen
# 查看版本
nginx -v
# 查看nginx进程是否启动
tasklist /fi "imagename eq nginx.exe"
1
2
3
4
5
6
7
8
9
10
11
12
Nginx配置文件结构与格式规范
http {		# 每一对大括号结束部分为上下文，上下文中可以包含其他的上下文
  server{	# Server称为虚拟主机
    location {	# 一个Server可以监听多个localtion
      # listen可以配置成IP+prot 示例如下： listen 8000; listen *:8000; 
      # listen localhost:8000; listen 127.0.0.1:8000; listen 127.0.0.1;（ 端口不写,默认80 ）
      listen 80;
      # server_name是用以区分的，可以任意。可以使用变量$hostname配置为主机名称。或者域名。
      server_name localhost;	
    }
    # 如果有多个Server，那么listen+server_name不可以重复。
    location  / {	# location的根目录总是指向root目录。
    	# 当多个location监听同一端口时，会根据server_name，去匹配访问不同的资源。
    	listen 80;
    	server_name nginx-dev;
    }
  }
  server{
  }
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
这里我们使用VSCode中的Nginx Configuration和Remote SSH插件对Nginx进行操作。

Nginx静态页面
修改配置文件 -> 执行nginx -s reload -> 访问IP:8000
静态页面配置一个AdminLTE后台管理系统。
配置文件示例：

server{
    listen 8000;
    server_name localhost;

    location / {
        root /home/AdminLTE-3.2.0;
        index index.html index.jsp;
    }
}
1
2
3
4
5
6
7
8
9
反向代理
Http反向代理
正向代理：客户端的代理，如VPN。
反向代理：服务器端的代理，如Nginx。
这里我省略了后端程序ruoyi部署过程，只保留关键的Nginx配置。

server{
    listen 8001;
    server_name ruoyi;
    location / {
        proxy_pass http://localhost:8088;
        # 由于使用反向代理，后端服务无法获取用户的真是IP地址，所以，一般使用反向代理，都要设置一下header信息。
        # 代理请求头：Nginx主机地址
        proxy_set_header Host $http_host;
        # 代理请求头：用户真实IP，即客户端IP
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

    }
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
常用的变量值
$host：Nginx的主机IP，例如192.168.0.101
$http_host：nginx主机IP和端口，192.168.0.101:8001
proxy_host：localhost:8088，proxy_pass里配置的主机名和端口。
remote_addr：用户的真实IP，即客户端IP地址。
proxy_pass：代理路径。如果地址只配置到了/，不包括URL，那么location将会被追加到转发的地址中。但是如果proxy_pass包括了URL，则location不会被追加到地址中。
eg：

# 以下这种写法，会被代理到 	http://localhost:8888/some/path/page.html
location /some/path {
	proxy_pass http://localhost:8888/;
}
# 以下这种写法，会被代理到 	http://localhost:8888/zh-cn//page.html
location /some/path {
	proxy_pass http://localhost:8888/zh-cn/;
}
1
2
3
4
5
6
7
8
动静分离
为什么需要动静分离？
Apache Tocmat 严格来说是一款java EE服务器，主要是用来处理 servlet请求。处理css、js、图片这些静态文件的IO性能不够好，因此，将静态文件交给nginx处理，可以提高系统的访问速度，减少tomcat的请求次数，有效的给后端服务器降压。

图例
				-> Tomcat  8080
nginx 8888
				-> 本地静态资源 /home/www/static
1
2
3
Nginx配置文件示例：
server{
    listen 8002;
    server_name ruoyi-tomcat;

    location / {
        proxy_pass http://localhost:8080;
    }
    
    location = /html/ie.html {
        root /home/www/static;
    }
    
    location ^~ /fonts/ {
        root /home/www/static;
    }


​    
    ## 代理以.js等结尾的静态文件 ~区分大小写 ~*不区分大小写
    location ~ \.(css|js|png|jpg|gif|ico) {
        root /home/www/static;
    }
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
修饰符与优先级
精确匹配(=)
前缀匹配(^~)表示普通字符匹配。使用前缀匹配。如果匹配成功，则不再匹配其它 location。优先级第二高。
正则匹配(~和～*）
不写
缓冲与缓存


缓冲（Buffer）
缓冲一般放在内存中，如果不适合放入内存，则会将响应写入磁盘临时文件中。
启用缓冲后，Nginx先将后端请求响应（response）放入缓冲区，等到整个响应完成后，再发送给客户端。减少服务器长链接数量，释放资源。
Nginx默认是开启了缓冲。也可以用proxy_buffering off来手动的关闭Nginx缓冲。

location / {
# proxy_buffers 指令设置每个连接读取响应的缓冲区的大小和数量 。默认的，缓冲区大小等于一个内存页，4K 或 8K，具体取决于操作系统。
    proxy_buffers 16 4k;
# 来自后端服务器响应的第一部分存储在单独的缓冲区中，其大小通过 proxy_buffer_size 指令进行设置，此部分通常是相对较小的响应headers，通常将其设置成小于默认值。
    proxy_buffer_size 2k;
    proxy_pass http://localhost:8088;
}
1
2
3
4
5
6
7
如果整个响应不适合存到内存里，则将其中的一部分保存到磁盘上的‎‎临时文件中‎‎。
‎‎proxy_max_temp_file_size‎‎设置临时文件的最大值。
‎‎proxy_temp_file_write_size‎‎设置一次写入临时文件的大小。

缓存（cache）
启用缓存，nginx将响应保存到磁盘中，返回给客户端的数据首先从缓存中读取，这样相同的请求不会每次都发送到后端服务器，减少后端请求的数量。
启用缓存，需要在http上下文中使用proxy_cache_path指令，来设置缓存的本地目录，名称，大小。
缓存区可以被多个server共享，使用proxy_cache可以指定使用哪个缓存区。

http {
    proxy_cache_path /data/nginx/cache keys_zone=mycache:10m;
    server {
        proxy_cache mycache;
        location / {
            proxy_pass http://localhost:8000;
        }
    }
}
1
2
3
4
5
6
7
8
9
示例：
proxy_cache_path /var/cache/nginx/static keys_zone=static:100m;

server{
    listen 8001;
    server_name ruoyi;
    location / {
        proxy_pass http://localhost:8088;
        # 代理请求头：Nginx主机地址
        proxy_set_header Host $http_host;
        # 代理请求头：用户真实IP，即客户端IP
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    
        # 缓冲设置
        proxy_buffers 16 4k;
        proxy_buffer_size 2k;
    }
    
    # 代理以.js等结尾的静态文件 ~区分大小写 ~*不区分大小写
    location ~ \.(css|js|png|jpg|gif|ico) {
        proxy_cache static;
        # 缓存失效时间配置，单位分钟
        proxy_cache_valid 200 302 10m;
        proxy_cache_valid 404 1m;
        proxy_cache_valid any 5m;
    
        proxy_pass http://localhost:8088;
    }
    
    location = /html/ie.html {
         proxy_cache static;
        # 缓存失效时间配置，单位分钟
        proxy_cache_valid 200 302 10m;
        proxy_cache_valid 404 1m;
        proxy_cache_valid any 5m;
    
        proxy_pass http://localhost:8088;
    }
    
    location ^~ /fonts/ {
         proxy_cache static;
        # 缓存失效时间配置，单位分钟
        proxy_cache_valid 200 302 10m;
        proxy_cache_valid 404 1m;
        proxy_cache_valid any 5m;
    
        proxy_pass http://localhost:8088;
    }
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
负载均衡
配置图
				-->		tomcat(8080)
nginx(8083)  
				-->		ruoyi-admin.jar(8088)
1
2
3
负载均衡策略
策略	含义	语法
轮循机制
round-robin	默认机制，以轮循机制方式分发	     不写即为默认轮询机制     
最小连接
least-connected	将下一个请求分配给活动连接数最少的服务器
（较为空闲的服务器）	
ip-hash	客户端的 IP 地址将用作哈希键，
来自同一个ip的请求会被转发到相同的服务器	
hash	通用hash，允许用户自定义hash的key，
key可以是字符串、变量或组合。
consistent当有机器宕机时，只对不可用的请求IP重新HASH。	
随机‎‎
random	每个请求都将传递到随机选择的服务器	
权重
weight	根据指定的权重进行转发	
健康监测
在Nginx的反向代理中，后台服务器如果在一定时间内响应失败超过指定数，Nginx会将该机器标记为失败，并且在后续的一段时间不再将请求打到这个服务器。
参数：检查周期fail_timeout‎‎，默认为10秒。
参数：失败次数max_fails‎，默认为1次。‎
# 如果Nginx无法向服务器发送请求或在30秒内请求8088失败超过3次，则会将服务器标记为不可用30秒，
upstream ruoyi-apps {
    server localhost:8080;
    server localhost:8088 max_fails=3 fail_timeout=30s;
}
1
2
3
4
5
HTTPS配置
server {
    # ssl默认是443端口
    listen 8004 ssl;
    server_name ruoyi-https;
    ssl_certificate /etc/nginx/server.crt;
    ssl_certificate_key /etc/nginx/server.key;
    ssl_protocols       TLSv1 TLSv1.1 TLSv1.2;
    ssl_ciphers         HIGH:!aNULL:!MD5;
    location / {
        proxy_pass http://localhost:8088;
    }
    ssl_password_file   /etc/nginx/cert.pass;
}
1
2
3
4
5
6
7
8
9
10
11
12
13
TCP反向代理
stream{
    server {
        listen 13306;
        proxy_pass localhost:3306;
    }
}
1
2
3
4
5
6
TCP负载均衡
stream {

  upstream backend-mysql {

    server localhost:3306;
    server localhost:3307;
    
    # keepalive定义连接池里空闲连接的数量。
    keepalive 8;
  }

  server {
    listen 13306;
    proxy_pass backend-mysql;
  }
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
重写return/rewrite
转发与重定向。转发是服务器端的操作，而重定向是客户端变更请求的URL地址。反向代理属于转发，而重写属于重定向。
重定向状态码：301（永久重定向），302（临时重定向）

return
server{
    listen 8888;
    server_name retn-test;
    return 301 https://www.baidu.com;
}
1
2
3
4
5
rewrite
server{
    listen 8002;
    server_name ruoyi-tomcat;

	 rewrite_log on;
	 rewrite ^/profile/upload http://192.168.56.101:8080$request_uri;
}
1
2
3
4
5
6
7
last和break
last：若当前规则不匹配，停止后续匹配rewrite规则，使用重写后的路径，重新搜索loacaton及块内指令。
break：若当前规则不匹配，停止处理后续rewrite规则，执行{}块内其他指令。

其他指令
gzip压缩
压缩响应，减少传输大小，占用CPU。

# 默认打开
gzip on;
# 压缩类型，默认只对html压缩。
gzip_types text/plain applicaton/xml;
# 压缩大小，超过1000字节才压缩
gzip_min_length 1000;
1
2
3
4
5
6
sendfile
nginx默认会在传输文件前将其复制到缓冲区，使用sendfile指令，可以禁用复制到缓冲区。类似Java中的零拷贝（zero copy）。

location /download{
	sendfile on;
	# 能在获得数据后立即获得响应标头
	tcp_nopush on;
}
1
2
3
4
5
try_files
检查指定的文件或目录是否存在，若不存在可以指定位置或状态码。

server{
	root www/data;
	location /images/ {
		try_files $uri /images/default.jpg;
	}
	// 可以指定多 个路径
	location / {
		try_files $uri $uri/ /$uri.html = 404;
	}
}
1
2
3
4
5
6
7
8
9
10
error_page
error_page 404 	   /404.html
error_page 500 502 503 504    /500.html
1
2
推荐写法和注意事项
注意事项	示例
重复的配置可以继承自父级	
不要将所有的请求都代理到后端服务器	
若非必要，不要缓存动态请求，仅缓存静态文件	
检查文件是否存在用try_files替代if -f	
在重写路径中包含http和https	
保持重写规则简单干净	
不要将所有的请求都代理到后端服务器
配置示例：先去本地目录查询，查找不到再去后台查找

server{
    listen 8002;
    server_name ruoyi-tomcat;

    location / {
        try_files $uri $uri/ @proxy;
    }
    
    location @proxy {
        proxy_pass http://localhost:8080;
        # 代理请求头：Nginx主机地址
        proxy_set_header Host $http_host;
        # 代理请求头：用户真实IP，即客户端IP
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
注意事项：
正确的配置未生效，可以清除或禁用浏览器缓存。
在https中不要启用SSLv3。并可以使用ssl_protocols TLSv1 TLSv1.1 TLSv1.2来禁用。
不要将root目录配置成/或/root
谨慎的使用chmod 777 。可以使用namei -on /path/to/check来显示路径上的权限。
不要将部署的项目拷贝到默认目录。（在升级nginx的时候或许覆盖）
致谢：B站UP主，阿姨洗铁路我洗海带哟
Nginx课程：BV1rG4y1e7BQ
课件链接：https://www.yuque.com/wukong-zorrm/cql6cz
虚拟机网盘链接:https://pan.baidu.com/s/1NmCR-vdAcZLouRRn9V1yTA 密码: 1b60
————————————————
版权声明：本文为CSDN博主「Huathy-雨落江南，浮生若梦」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/qq_40366738/article/details/113839727