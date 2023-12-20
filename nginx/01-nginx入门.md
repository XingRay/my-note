## nginx入门



### 1. 下载安装

进入nginx下载最新稳定版 http://nginx.org/en/download.html，解压到合适的目录



### 2. 启动、重启、停止

到安装目录双击nginx.exe，或者通过命令行 nginx 运行



重启，重新加载配置文件

```bash
nginx -s reload
```



快速退出

```bash
nginx -s stop
```



优雅退出

```bash
nginx -s quit
```



### 3. 指定配置文件与测试

验证配置文件

```bash
/usr/local/nginx/sbin/nginx -tc /usr/local/nginx/conf/nginx_my.conf
/usr/local/nginx/sbin/nginx -t -c /usr/local/nginx/conf/nginx_my.conf
```

以指定配置文件启动

```bash
/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/nginx_my.conf
```

通过指定配置文件启动后重启

```bash
/usr/local/nginx/sbin/nginx -s reload -c /usr/local/nginx/conf/nginx_my.conf
```

通过指定配置文件启动后停止

```bash
/usr/local/nginx/sbin/nginx -s stop -c /usr/local/nginx/conf/nginx_my.conf
```

### 4. 反向代理

修改nginx.conf文件如下：

```nginx
worker_processes  2;
daemon on;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile      on;

    server {
        listen       80;
        server_name  localhost;

        location / {
            proxy_pass http://localhost:8080;
            
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   ../front/html;
        }
	}
}
```



### 5. 负载均衡

修改nginx.conf如下：

```nginx
worker_processes  2;
daemon on;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile      on;
	
	upstream targetserver{
		server localhost:8080;
		server localhost:8081;
	}

    server {
        listen       80;
        server_name  localhost;

        location / {
            proxy_pass http://targetserver;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   ../front/html;
        }
	}
}
```

可以指定服务器的权重：

```nginx
upstream targetserver{
	server localhost:8080 weight=10;
	server localhost:8081 weight=2;
}
```





mimetype：

在nginx.conf统计目录下新建mime.types文件，内容如下：

```nginx
types {
    text/html                                        html htm shtml;
    text/css                                         css;
    text/xml                                         xml;
    image/gif                                        gif;
    image/jpeg                                       jpeg jpg;
    application/javascript                           js;
    application/atom+xml                             atom;
    application/rss+xml                              rss;

    text/mathml                                      mml;
    text/plain                                       txt;
    text/vnd.sun.j2me.app-descriptor                 jad;
    text/vnd.wap.wml                                 wml;
    text/x-component                                 htc;

    image/avif                                       avif;
    image/png                                        png;
    image/svg+xml                                    svg svgz;
    image/tiff                                       tif tiff;
    image/vnd.wap.wbmp                               wbmp;
    image/webp                                       webp;
    image/x-icon                                     ico;
    image/x-jng                                      jng;
    image/x-ms-bmp                                   bmp;

    font/woff                                        woff;
    font/woff2                                       woff2;

    application/java-archive                         jar war ear;
    application/json                                 json;
    application/mac-binhex40                         hqx;
    application/msword                               doc;
    application/pdf                                  pdf;
    application/postscript                           ps eps ai;
    application/rtf                                  rtf;
    application/vnd.apple.mpegurl                    m3u8;
    application/vnd.google-earth.kml+xml             kml;
    application/vnd.google-earth.kmz                 kmz;
    application/vnd.ms-excel                         xls;
    application/vnd.ms-fontobject                    eot;
    application/vnd.ms-powerpoint                    ppt;
    application/vnd.oasis.opendocument.graphics      odg;
    application/vnd.oasis.opendocument.presentation  odp;
    application/vnd.oasis.opendocument.spreadsheet   ods;
    application/vnd.oasis.opendocument.text          odt;
    application/vnd.openxmlformats-officedocument.presentationml.presentation
                                                     pptx;
    application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
                                                     xlsx;
    application/vnd.openxmlformats-officedocument.wordprocessingml.document
                                                     docx;
    application/vnd.wap.wmlc                         wmlc;
    application/wasm                                 wasm;
    application/x-7z-compressed                      7z;
    application/x-cocoa                              cco;
    application/x-java-archive-diff                  jardiff;
    application/x-java-jnlp-file                     jnlp;
    application/x-makeself                           run;
    application/x-perl                               pl pm;
    application/x-pilot                              prc pdb;
    application/x-rar-compressed                     rar;
    application/x-redhat-package-manager             rpm;
    application/x-sea                                sea;
    application/x-shockwave-flash                    swf;
    application/x-stuffit                            sit;
    application/x-tcl                                tcl tk;
    application/x-x509-ca-cert                       der pem crt;
    application/x-xpinstall                          xpi;
    application/xhtml+xml                            xhtml;
    application/xspf+xml                             xspf;
    application/zip                                  zip;

    application/octet-stream                         bin exe dll;
    application/octet-stream                         deb;
    application/octet-stream                         dmg;
    application/octet-stream                         iso img;
    application/octet-stream                         msi msp msm;

    audio/midi                                       mid midi kar;
    audio/mpeg                                       mp3;
    audio/ogg                                        ogg;
    audio/x-m4a                                      m4a;
    audio/x-realaudio                                ra;

    video/3gpp                                       3gpp 3gp;
    video/mp2t                                       ts;
    video/mp4                                        mp4;
    video/mpeg                                       mpeg mpg;
    video/quicktime                                  mov;
    video/webm                                       webm;
    video/x-flv                                      flv;
    video/x-m4v                                      m4v;
    video/x-mng                                      mng;
    video/x-ms-asf                                   asx asf;
    video/x-ms-wmv                                   wmv;
    video/x-msvideo                                  avi;
}
```





### 6. nginx的配置

```nginx
### 全局块 start ###
# 配置影响nginx全局的指令，如：用户组，nginx进程id存放路径，日志存放路径，配置文件引入，
# 允许生成worker process数等

# 用户组叫做 nginx
user  nginx;
# 工作线程数
worker_processes  1;

# 日志目录
error_log  logs/error.log;
error_log  logs/error.log  notice;
error_log  logs/error.log  info;

# pid文件，里面存有nginx运行的pid
pid        logs/nginx.pid;

### 全局块 end ###

### events start ###
# 配置影响nginx服务器或与用户的网络连接。如：每个进程的最大连接数，选取哪种事件驱动模型处理连接请求，
# 是否允许同时接受多个网络连接，开启多个网络连接序列化等。
events {
	# 最大连接数
    worker_connections  1024;
}

### events end ###



### http start ###
# 可以嵌套多个 server，配置代理，缓存，日志定义等绝大多数功能和第三方模块的配置。如文件引入，mime-type定义，日志自定义
# 是否使用sendfile传输文件，连接超时时间，单连接请求数等。
http {

    ### http 全局块 start ###
    # 如 upstream，错误页面，连接超时等
    
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
    
	### http 全局块 start ###
	
    ### server块 start ###
    server {
        # 配置虚拟主机的相关参数， 一个http中可以有多个server
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            # 配置请求的路由，以及各种页面的处理情况
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
	### server块 end ###

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

### http end ###


```

