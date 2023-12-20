## Nginx快速配置



### 1. 下载安装

下载最新版本nginx，解压到本地指定目录

http://nginx.org/

http://nginx.org/download/nginx-1.24.0.zip

http://nginx.org/download/nginx-1.24.0.tar.gz



### 2. 添加系统路径

打开windows系统设置

```console
SystemPropertiesAdvanced
```

设置系统环境变量 %NGINX_HOME% 指向nginx的安装目录。 将%NGINX_HOME%的目录添加到系统路径 Path 中



### 3. 创建运行目录

在指定路径创建文件夹 nginx_instance

1 将下载的 nginx 的 /nginx/conf /nginx/html/nginx/logs /nginx/temp 复制到 /nginx_instance 目录下

2 创建static目录，用于存放静态资源，将/nginx/html 目录复制到 /nginx_instance/static 目录下

3 在nginx_instance目录下新建3个脚本文件 

start.bat 

```bash
nginx -c .\config\nginx.conf
```

stop.bat 

```bash
nginx -s stop -c .\config\nginx.conf
```

reload.bat

```bash
nginx  -s reload -c .\config\nginx.conf
```

此时nginx_instance的目录结构

```bash
.
│   reload.bat
│   start.bat
│   stop.bat
│
├───config
│       fastcgi.conf
│       fastcgi_params
│       koi-utf
│       koi-win
│       mime.types
│       nginx.conf
│       scgi_params
│       uwsgi_params
│       win-utf
│
├───logs
│
├───static
│   └───html
│       │   50x.html
│       │   index.html
│
└───temp
```



### 4. 修改配置

根据需要修改nginx的配置文件

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

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            root   static/html;
            index  index.html index.htm;
        }

        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        #
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   static/html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }


    # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


    # HTTPS server
    #
    #server {
    #    listen       443 ssl;
    #    server_name  localhost;

    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;

    #    ssl_session_cache    shared:SSL:1m;
    #    ssl_session_timeout  5m;

    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers  on;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}
}
```



此处配置

```nginx
location / {
	root   static/html;
	index  index.html index.htm;
}
```

将 / 路径映射到 static/html 目录，并且将 index.html设置为首页



### 5. 启动

通过命令行脚本  /start.bat  即可启动



### 6. 安装为windows Service

1 在windows系统设置中设置系统环境变量 %NGINX_HOME% 指向nginx的安装目录。

2 下载安装好 windows-service-wrapper

https://github.com/winsw/winsw

https://github.com/winsw/winsw/releases/download/v3.0.0-alpha.11/WinSW-x64.exe



3 在nginx_instance目录下创建文件 nginx-windows-service.xml

```xml
<service>
  <id>nginx</id>
  <name>nginx</name>
  <description>nginx [engine x] is an HTTP and reverse proxy server, a mail proxy server, and a generic TCP/UDP proxy server</description>
  
  <executable>%NGINX_HOME%\nginx.exe</executable>
  <arguments>-c D:\develop\nginx\nginx_instance\config\nginx.conf</arguments>
  <workingdirectory>D:\develop\nginx\nginx_instance</workingdirectory>
  
  <stopexecutable>%NGINX_HOME%\nginx.exe</stopexecutable>
  <stoparguments> -s stop -c D:\develop\nginx\nginx_instance\config\nginx.conf</stoparguments>
  
  <log mode="roll" />
  <logpath>D:\develop\nginx\nginx_instance\logs</logpath>
  
  <onfailure action="restart" />
</service>

```

 4 安装nginx服务

```bash
D:\develop\windows-service-wrapper\winsw.exe install D:\develop\nginx\nginx_instance\nginx-windows-service.xml
```

5 启动nginx 服务

```bash
D:\develop\windows-service-wrapper\winsw.exe start D:\develop\nginx\nginx_instance\nginx-windows-service.xml
```

6 查看nginx 服务状态

```bash
D:\develop\windows-service-wrapper\winsw.exe status D:\develop\nginx\nginx_instance\nginx-windows-service.xml
```



安装好nginx服务后可以通过windows的任务管理器查看和管理nginx服务



