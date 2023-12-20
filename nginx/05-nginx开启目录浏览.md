## Nginx开启目录浏览



在http块呢加上一下设置即可：

```nginx
#文件夹目录显示
#开启目录浏览功能；
autoindex on;

#关闭详细文件大小统计，让文件大小显示MB，GB单位，默认为b；
autoindex_exact_size off;

#开启以服务器本地时区显示文件修改日期！
autoindex_localtime on;
```



完整配置示例如下：

```nginx
#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;


events {
    worker_connections  1024;
}


http {
    include       mime.types;
    default_type  application/octet-stream;
	
    #文件夹目录显示
	#开启目录浏览功能；
	autoindex on;
	
	#关闭详细文件大小统计，让文件大小显示MB，GB单位，默认为b；
	autoindex_exact_size off;
	
	#开启以服务器本地时区显示文件修改日期！
	autoindex_localtime on;


    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;
	
	# 配置上游服务器，起一个名字，如 gulimall
	upstream gulimall{
		# 上游服务器包含的具体的服务器列表，每一个服务器是一个 server标签，可有有多个
		server 192.168.0.108:88;
		# server 192.168.0.108:89;
	}
	
	include /develop/nginx/nginx_instance/config/nginxConfigs/*.conf;
}
```

