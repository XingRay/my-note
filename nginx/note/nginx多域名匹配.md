## nginx域名匹配使用server_name指令进行配置



匹配规则如下

精准匹配
多个域名都写上,精准匹配

```nginx
server {
    listen       80;
    server_name  a.com b.com a.b.com;
}
```

通配符在前
以通配符*开始

```NGINX
server {
    listen       80;
    server_name  *.abc.com;
}
```

结尾

```nginx
server {
    listen       80;
    server_name  abc.*;
}
```

正则匹配
以正则表达式来匹配

```nginx
server {
    listen       80;
    server_name  ~^(?.+)\.abc\.com$;
}
```

不能只使用一个*来匹配所有域名

server_name优先级如下：

1、精准匹配
2、通配符在前
3、通配符在后的
4、正则匹配
前面的匹配到就不会进行后面的规则
如果都不匹配
1、优先选择listen配置项后有default或default_server的
2、找到匹配listen端口的第一个server块

```nginx
server {
    listen 80 default;
    server_name  aaa.com; 
}
```

绑定多个端口
有以下两种方式

配置多个server

```nginx
server {
    listen       80;
    server_name  abc.com;
}
server {
    listen       8080;
    server_name  abc.com;
}
```

一个server里写多个listen

```nginx
server {
    listen       80;
    listen       8080;
    server_name  abc.com;
}
```

