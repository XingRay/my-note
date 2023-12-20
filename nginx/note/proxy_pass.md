# nginx 之 proxy_pass详解

![img](https://csdnimg.cn/release/blogv2/dist/pc/img/reprint.png)

[资料收集库](https://blog.csdn.net/u010433704)![img](https://csdnimg.cn/release/blogv2/dist/pc/img/newCurrentTime2.png)于 2019-08-21 11:05:41 发布![img](https://csdnimg.cn/release/blogv2/dist/pc/img/articleReadEyes2.png)259241![img](https://csdnimg.cn/release/blogv2/dist/pc/img/tobarCollect2.png) 收藏 168

分类专栏： [nginx](https://blog.csdn.net/u010433704/category_8776420.html)

版权

[![img](https://img-blog.csdnimg.cn/20201014180756922.png?x-oss-process=image/resize,m_fixed,h_64,w_64)nginx专栏收录该内容](https://blog.csdn.net/u010433704/category_8776420.html)

16 篇文章1 订阅

订阅专栏

在[nginx](https://so.csdn.net/so/search?q=nginx&spm=1001.2101.3001.7020)中配置proxy_pass代理转发时，如果在proxy_pass后面的url加/，表示绝对根路径；如果没有/，表示相对路径，把匹配的路径部分也给代理走。

假设下面四种情况分别用 http://192.168.1.1/proxy/test.html 进行访问。

第一种：
location /proxy/ {
proxy_pass http://127.0.0.1/;
}
代理到URL：http://127.0.0.1/test.html

第二种（相对于第一种，最后少一个 / ）
location /proxy/ {
proxy_pass http://127.0.0.1;
}
代理到URL：http://127.0.0.1/proxy/test.html

第三种：
location /proxy/ {
proxy_pass http://127.0.0.1/aaa/;
}
代理到URL：http://127.0.0.1/aaa/test.html

第四种（相对于第三种，最后少一个 / ）
location /proxy/ {
proxy_pass http://127.0.0.1/aaa;
}
代理到URL：http://127.0.0.1/aaatest.html

nginx中有两个模块都有`proxy_pass`指令。

- `ngx_http_proxy_module`的`proxy_pass`：

```cobol
语法: proxy_pass URL;场景: location, if in location, limit_except说明: 设置后端代理服务器的协议(protocol)和地址(address),以及location中可以匹配的一个可选的URI。协议可以是"http"或"https"。地址可以是一个域名或ip地址和端口，或者一个 unix-domain socket 路径。  详见官方文档: http://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_passURI的匹配，本文第四部分重点讨论。
```

- `ngx_stream_proxy_module`的`proxy_pass`：

```cobol
语法: proxy_pass address;场景: server说明: 设置后端代理服务器的地址。这个地址(address)可以是一个域名或ip地址和端口，或者一个 unix-domain socket路径。  详见官方文档: http://nginx.org/en/docs/stream/ngx_stream_proxy_module.html#proxy_pass
```

## 二、两个`proxy_pass`的关系和区别

在两个模块中，两个`proxy_pass`都是用来做后端代理的指令。
`ngx_stream_proxy_module`模块的`proxy_pass`指令只能在server段使用使用, 只需要提供域名或ip地址和端口。可以理解为端口转发，可以是tcp端口，也可以是udp端口。
`ngx_http_proxy_module`模块的`proxy_pass`指令需要在location段，location中的if段，limit_except段中使用，处理需要提供域名或ip地址和端口外，还需要提供协议，如"http"或"https"，还有一个可选的uri可以配置。

## 三、proxy_pass的具体用法

### `ngx_stream_proxy_module`模块的`proxy_pass`指令

```cobol
server {



    listen 127.0.0.1:12345;



    proxy_pass 127.0.0.1:8080;



}



 



server {



    listen 12345;



    proxy_connect_timeout 1s;



    proxy_timeout 1m;



    proxy_pass example.com:12345;



}



 



server {



    listen 53 udp;



    proxy_responses 1;



    proxy_timeout 20s;



    proxy_pass dns.example.com:53;



}



 



server {



    listen [::1]:12345;



    proxy_pass unix:/tmp/stream.socket;



}
```

### `ngx_http_proxy_module`模块的`proxy_pass`指令

```cobol
server {



    listen      80;



    server_name www.test.com;



 



    # 正常代理，不修改后端url的



    location /some/path/ {



        proxy_pass http://127.0.0.1;



    }



 



    # 修改后端url地址的代理（本例后端地址中，最后带了一个斜线)



    location /testb {



        proxy_pass http://www.other.com:8801/;



    }



 



    # 使用 if in location



    location /google {



        if ( $geoip_country_code ~ (RU|CN) ) {



            proxy_pass http://www.google.hk;



        }



    }



 



    location /yongfu/ {



        # 没有匹配 limit_except 的，代理到 unix:/tmp/backend.socket:/uri/



        proxy_pass http://unix:/tmp/backend.socket:/uri/;;



 



        # 匹配到请求方法为: PUT or DELETE, 代理到9080



        limit_except PUT DELETE {



            proxy_pass http://127.0.0.1:9080;



        }



    }



 



}
```

## 四、`proxy_pass`后，后端服务器的`url`(`request_uri`)情况分析

```cobol
server {



    listen      80;



    server_name www.test.com;



 



    # 情形A



    # 访问 http://www.test.com/testa/aaaa



    # 后端的request_uri为: /testa/aaaa



    location ^~ /testa/ {



        proxy_pass http://127.0.0.1:8801;



    }



    



    # 情形B



    # 访问 http://www.test.com/testb/bbbb



    # 后端的request_uri为: /bbbb



    location ^~ /testb/ {



        proxy_pass http://127.0.0.1:8801/;



    }



 



    # 情形C



    # 下面这段location是正确的



    location ~ /testc {



        proxy_pass http://127.0.0.1:8801;



    }



 



    # 情形D



    # 下面这段location是错误的



    #



    # nginx -t 时，会报如下错误: 



    #



    # nginx: [emerg] "proxy_pass" cannot have URI part in location given by regular 



    # expression, or inside named location, or inside "if" statement, or inside 



    # "limit_except" block in /opt/app/nginx/conf/vhost/test.conf:17



    # 



    # 当location为正则表达式时，proxy_pass 不能包含URI部分。本例中包含了"/"



    location ~ /testd {



        proxy_pass http://127.0.0.1:8801/;   # 记住，location为正则表达式时，不能这样写！！！



    }



 



    # 情形E



    # 访问 http://www.test.com/ccc/bbbb



    # 后端的request_uri为: /aaa/ccc/bbbb



    location /ccc/ {



        proxy_pass http://127.0.0.1:8801/aaa$request_uri;



    }



 



    # 情形F



    # 访问 http://www.test.com/namea/ddd



    # 后端的request_uri为: /yongfu?namea=ddd



    location /namea/ {



        rewrite    /namea/([^/]+) /yongfu?namea=$1 break;



        proxy_pass http://127.0.0.1:8801;



    }



 



    # 情形G



    # 访问 http://www.test.com/nameb/eee



    # 后端的request_uri为: /yongfu?nameb=eee



    location /nameb/ {



        rewrite    /nameb/([^/]+) /yongfu?nameb=$1 break;



        proxy_pass http://127.0.0.1:8801/;



    }



 



    access_log /data/logs/www/www.test.com.log;



}



 



server {



    listen      8801;



    server_name www.test.com;



    



    root        /data/www/test;



    index       index.php index.html;



 



    rewrite ^(.*)$ /test.php?u=$1 last;



 



    location ~ \.php$ {



        try_files $uri =404;



        fastcgi_pass unix:/tmp/php-cgi.sock;



        fastcgi_index index.php;



        include fastcgi.conf;



    }



 



    access_log /data/logs/www/www.test.com.8801.log;



}



 



 
```

文件: `/data/www/test/test.php`

```php
<?php



echo '$_SERVER[REQUEST_URI]:' . $_SERVER['REQUEST_URI'];
```

通过查看 $_SERVER['REQUEST_URI'] 的值，我们可以看到每次请求的后端的request_uri的值，进行验证。

小结

情形A和情形B进行对比，可以知道`proxy_pass`后带一个URI,可以是斜杠(/)也可以是其他uri，对后端`request_uri`变量的影响。
情形D说明，当location为正则表达式时，`proxy_pass`不能包含URI部分。
情形E通过变量($request_uri, 也可以是其他变量)，对后端的`request_uri`进行改写。
情形F和情形G通过rewrite配合break标志,对url进行改写，并改写后端的`request_uri`。需要注意，`proxy_pass`地址的URI部分在情形G中无效，不管如何设置，都会被忽略。







nginx中proxy_pass配置说明

吃爆米花的怪蜀黍

于 2021-09-07 11:59:17 发布

5543
 收藏 19
文章标签： nginx
版权

华为云开发者联盟
该内容已被华为云开发者联盟社区收录
加入社区
官方说明：在nginx中配置proxy_pass代理转发时，如果在proxy_pass后面的url加/，表示绝对根路径；如果没有/，表示相对路径，把匹配的路径部分也给代理走。

一般proxy_pass后面有四种情况：ip:端口；ip:端口/；ip:端口/上下文；ip:端口/上下文/下面我就这四种情况进行举例说明。
例如访问地址：http://192.168.2.39:8081/hussarApi/test/getList

1、ip:端口

location /hussarApi/ {
	proxy_pass http://192.168.2.188:8280;	
}
1
2
3
代理地址：http://192.168.2.188:8280/hussarApi/test/getList
//url后无/，为相对路径,连同匹配部分hussarApi也追加到代理地址上

2、ip:端口/

location /hussarApi/ {
	proxy_pass http://192.168.2.188:8280/;	
}
1
2
3
代理地址：http://192.168.2.188:8280/test/getList
//url后有/，为绝对根路径，会将hussarApi部分去掉，代理地址拼接hussarApi后面路径

3、ip:端口/上下文

location /hussarApi/ {
	proxy_pass http://192.168.2.188:8280/hussarApi;	
}
1
2
3
代理地址：http://192.168.2.188:8280/hussarApitest/getList
//url后有上下文，会将访问地址hussarApi去掉，代理地址直接拼接访问地址hussarApi后面路径

4、ip:端口/上下文/

location /hussarApi/ {
	proxy_pass http://192.168.2.188:8280/hussarApi/;	
}
1
2
3
代理地址：http://192.168.2.188:8280/hussarApi/test/getList
//url后有上下文，会将访问地址hussarApi去掉，代理地址直接拼接访问地址hussarApi后面路径

3和4的差别在于proxy_pass url后面加了上下文地址，不带/和带着/两种情况，代理规则是一样的，都是取匹配部分后面的地址进行拼接，所以如果proxy_pass url端口后面带着上下文的，统一在最后面添加/

注意：location 为正则表达式的时候2不能使用
location 正则情况，url后面不能加 /

location ~ /hussarApi/ {
	proxy_pass http://192.168.2.188:8280/;
}
1
2
3
location 非正则情况，可以添加/

location /hussarApi/ {
	proxy_pass http://192.168.2.188:8280/;
}
1
2
3
特别鸣谢大神贴：https://blog.csdn.net/u010433704/article/details/99945557
————————————————
版权声明：本文为CSDN博主「吃爆米花的怪蜀黍」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/xiaowochaochao/article/details/120153964