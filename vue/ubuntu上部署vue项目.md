## Ubuntu上部署vue项目

### 1 规划

vue前端项目端口 如 80/8080/88 或者 9000/9001 等，下面示例使用9001

后端项目端口,假设为8900, 需要有可以公网访问的地址， 如 http://myserver.com:8900 或者 `http://[2408:xxxx:xxxx:xxxx:xxx:xxxx:xxxx:xxxx]:8900`

项目上传到服务器的目录，如： /webapp/myproject/front/admin-vue



### 2 vue项目打包

打包前需要先修改vue项目的环境配置，如 .env.production：

```javascript
VUE_APP_BASE_API = 'http://[2408:xxxx:xxxx:xxxx:xxx:xxxx:xxxx:xxxx]:8900'
```

执行打包命令：

```bash
npm run build:prod
```

会在项目中产生一个 `dist` 目录，[可选] 将目录打包成 dist.zip 。

将`dist.zip`上传到指定的目录（如：`/webapp/myproject/front/admin-vue` ），并解压资源文件至 dist目录，如： `/webapp/myproject/front/admin-vue/dist`



### 3 安装nginxs

在服务器上安装nginx，如Ubuntu上执行：

```bash
sudo apt-get install nginx
```

修改nginx配置文件，配置文件目录：

```
/etc/nginx
```

```nginx
user root;
worker_processes auto;
pid /run/nginx.pid;
include /etc/nginx/modules-enabled/*.conf;

events {
	worker_connections 768;
	# multi_accept on;
}

http {

	##
	# Basic Settings
	##

	sendfile on;
	tcp_nopush on;
        tcp_nodelay on;
        keepalive_timeout 65;
	types_hash_max_size 2048;
	# server_tokens off;

	# server_names_hash_bucket_size 64;
	# server_name_in_redirect off;

	include /etc/nginx/mime.types;
	default_type application/octet-stream;
 
        #文件夹目录显示
	#开启目录浏览功能；
	autoindex on;
	
	#关闭详细文件大小统计，让文件大小显示MB，GB单位，默认为b；
	autoindex_exact_size off;
	
	#开启以服务器本地时区显示文件修改日期！
	autoindex_localtime on;

	##
	# SSL Settings
	##

	ssl_protocols TLSv1 TLSv1.1 TLSv1.2 TLSv1.3; # Dropping SSLv3, ref: POODLE
	ssl_prefer_server_ciphers on;

	##
	# Logging Settings
	##

	access_log /var/log/nginx/access.log;
	error_log /var/log/nginx/error.log;

	##
	# Gzip Settings
	##

	gzip on;

	# gzip_vary on;
	# gzip_proxied any;
	# gzip_comp_level 6;
	# gzip_buffers 16 8k;
	# gzip_http_version 1.1;
	# gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

	##
	# Virtual Host Configs
	##
 
	include /etc/nginx/conf.d/*.conf;
	include /etc/nginx/sites-enabled/*;
}


#mail {
#	# See sample authentication script at:
#	# http://wiki.nginx.org/ImapAuthenticateWithApachePhpScript
#
#	# auth_http localhost/auth.php;
#	# pop3_capabilities "TOP" "USER";
#	# imap_capabilities "IMAP4rev1" "UIDPLUS";
#
#	server {
#		listen     localhost:110;
#		protocol   pop3;
#		proxy      on;
#	}
#
#	server {
#		listen     localhost:143;
#		protocol   imap;
#		proxy      on;
#	}
#}
```



在 `conf.d`目录下新建一个 ${project-name}.conf 的文件

web-ui 配置

```nginx
server {
    # 监听端口
    listen      	9001;
    
    # 支持ipv6
	listen		[::]:9001;
    
    charset utf-8;
    # 注意：这里必须配置为文件的路径，不能是目录的路径
    access_log  /home/myusername/webapp/myproject/front/admin-vue/log/access.log;

	location / {
		# ip/serverName+port 会被替换为 root，但是url中的路径会保留，
		# 实际的访问地址是 ${root}/，
		# 如 myserver.com/static/1.jpg 实际映射到 /webapp/myproject/front/admin-vue/dist/static/1.jpg
		root   /webapp/myproject/front/admin-vue/dist;
        
        # 防止刷新页面报404
        try_files $uri $uri/ /index.html;
    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



api-server 配置

```nginx
server {
    listen        8800;
    listen        [::]:8800;
    charset       utf-8;

    access_log  /home/leixing/webapp/myproject/api/log/access.log;

    location /api/front {
        // 根据 path 转发
        proxy_pass http://localhost:8901;
    }

    location /static {
        // http://www.mycompany.com/static/img/01.jpg => 
        // /home/myusername/webapp/myproject/static/img/01.jpg
        // path = root + uri
        root /home/myusername/webapp/myproject;
    }

    location / {
                # ip/serverName+port 会被替换为 root，但是url中的路径会保留，
                # 实际的访问地址是 ${root}/static/，
                # 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
                proxy_pass   http://localhost:8900;

    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



重启nginx

```bash
nginx -s relaod
```



