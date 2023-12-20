nginx项目实战配置



/etc/nginx/nginx.conf

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
	include /home/leixing/webapp/crmeb/crmeb_java/nginx_conf/*.conf;
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



/home/leixing/webapp/crmeb/crmeb_java/nginx_conf



seller.qiaga.store.https.conf

```nginx
server {
    listen        443;
    listen        [::]:443;
    server_name seller.qiaga.store;
    charset       utf-8;

    ssl_certificate /home/leixing/webapp/crmeb/crmeb_java/ssl/seller.qiaga.store/seller.qiaga.store_bundle.crt;
    ssl_certificate_key /home/leixing/webapp/crmeb/crmeb_java/ssl/seller.qiaga.store/seller.qiaga.store.key;
    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout 5m;
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    #ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
    ssl_prefer_server_ciphers on;

    access_log  /home/leixing/webapp/crmeb/crmeb_java/front/mer_admin/log/access.log;

    location / {
		# ip/serverName+port 会被替换为 root，但是url中的路径会保留，
		# 实际的访问地址是 ${root}/static/，
		# 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
		root   /home/leixing/webapp/crmeb/crmeb_java/front/mer_admin/dist;
		try_files $uri $uri/ /index.html;

    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



seller.qiaga.store.https.4430.conf

```nginx
server {
    listen        4430 ssl http2;
    listen        [::]:4430 ssl http2;
    server_name seller.qiaga.store;
    charset       utf-8;

    ssl_certificate /home/leixing/webapp/crmeb/crmeb_java/ssl/seller.qiaga.store/seller.qiaga.store_bundle.crt;
    ssl_certificate_key /home/leixing/webapp/crmeb/crmeb_java/ssl/seller.qiaga.store/seller.qiaga.store.key;
    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout 5m;
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    #ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
    ssl_prefer_server_ciphers on;

    access_log  /home/leixing/webapp/crmeb/crmeb_java/front/mer_admin/log/access.log;

    location / {
		# ip/serverName+port 会被替换为 root，但是url中的路径会保留，
		# 实际的访问地址是 ${root}/static/，
		# 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
		root   /home/leixing/webapp/crmeb/crmeb_java/front/mer_admin/dist;
		try_files $uri $uri/ /index.html;

    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



seller.qiaga.store.conf

```nginx
server {
    listen        80;
    listen        [::]:80;
    server_name seller.qiaga.store;
    charset       utf-8;

    access_log  /home/leixing/webapp/crmeb/crmeb_java/front/mer_admin/log/access.log;

    location / {
		# ip/serverName+port 会被替换为 root，但是url中的路径会保留，
		# 实际的访问地址是 ${root}/static/，
		# 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
		root   /home/leixing/webapp/crmeb/crmeb_java/front/mer_admin/dist;
		try_files $uri $uri/ /index.html;

    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



qiaga.store.https.conf

```nginx
server {
    listen        443 ssl http2;
    listen        [::]:443 ssl http2;
    server_name   qiaga.store www.qiaga.store;
    charset       utf-8;

    ssl_certificate /home/leixing/webapp/crmeb/crmeb_java/ssl/qiaga.store/qiaga.store_bundle.crt;
    ssl_certificate_key /home/leixing/webapp/crmeb/crmeb_java/ssl/qiaga.store/qiaga.store.key;
    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout 5m;
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    #ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
    ssl_prefer_server_ciphers on;

    access_log  /home/leixing/webapp/crmeb/crmeb_java/front/mer_app/log/access.log;

    location / {
		# ip/serverName+port 会被替换为 root，但是url中的路径会保留，
		# 实际的访问地址是 ${root}/static/，
		# 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
		root   /home/leixing/webapp/crmeb/crmeb_java/front/mer_app/h5;
		index index.html;
		try_files $uri $uri/ /index.html;

    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



qiaga.store.https.4430.conf

```nginx
server {
    listen        4430 ssl http2;
    listen        [::]:4430 ssl http2;
    server_name   qiaga.store www.qiaga.store;
    charset       utf-8;

    ssl_certificate /home/leixing/webapp/crmeb/crmeb_java/ssl/qiaga.store/qiaga.store_bundle.crt;
    ssl_certificate_key /home/leixing/webapp/crmeb/crmeb_java/ssl/qiaga.store/qiaga.store.key;
    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout 5m;
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    #ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
    ssl_prefer_server_ciphers on;

    access_log  /home/leixing/webapp/crmeb/crmeb_java/front/mer_app/log/access.log;

    location / {
		# ip/serverName+port 会被替换为 root，但是url中的路径会保留，
		# 实际的访问地址是 ${root}/static/，
		# 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
		root   /home/leixing/webapp/crmeb/crmeb_java/front/mer_app/h5;
		index index.html;
		try_files $uri $uri/ /index.html;

    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



qiaga.store.conf

```nginx
server {
    listen        80;
    listen        [::]:80;
    server_name   qiaga.store www.qiaga.store;
    charset       utf-8;

    access_log  /home/leixing/webapp/crmeb/crmeb_java/front/mer_app/log/access.log;

    location / {
		# ip/serverName+port 会被替换为 root，但是url中的路径会保留，
		# 实际的访问地址是 ${root}/static/，
		# 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
		root   /home/leixing/webapp/crmeb/crmeb_java/front/mer_app/h5;
		index index.html;
		try_files $uri $uri/ /index.html;

    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



nanosecond_cn-https_443.config

```nginx
server {
    listen        443 ssl http2;
    listen        [::]:443 ssl http2;
    server_name   nanosecond.cn www.nanosecond.cn;
    charset       utf-8;

    ssl_certificate /app/nanosecond/nanosecond-web/nginx/cert/nanosecond.cn.pem;
    ssl_certificate_key /app/nanosecond/nanosecond-web/nginx/cert/nanosecond.cn.key;
    ssl_session_cache    shared:SSL:1m;
    ssl_session_timeout 5m;
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    #ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
    ssl_prefer_server_ciphers on;

    access_log  /app/nanosecond/nanosecond-web/nginx/log/access.log;

    location / {
      # ip/serverName+port 会被替换为 root，但是url中的路径会保留，
      # 实际的访问地址是 ${root}/static/，
		  # 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
		  root   /app/nanosecond/nanosecond-web/web;
		  index index.html;
		  try_files $uri $uri/ /index.html;
    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```



nanosecond_cn-http_80.config

```nginx
server {
    listen        80;
    listen        [::]:80;
    server_name   nanosecond.cn www.nanosecond.cn;
    charset       utf-8;

    access_log  /app/nanosecond/nanosecond-web/nginx/log/access.log;

    location / {
        # ip/serverName+port 会被替换为 root，但是url中的路径会保留，
        # 实际的访问地址是 ${root}/static/，
        # 如 gulimall.com/static/1.jpg 实际映射到 app/gulimall/static/1.jpg
        root   /app/nanosecond/nanosecond-web/web;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```

配置重定向到https

nanosecond_cn-http_80.config

```nginx
server {
    listen        80;
    listen        [::]:80;
    server_name   nanosecond.cn www.nanosecond.cn;
    charset       utf-8;

    access_log  /app/nanosecond/nanosecond-web/nginx/log/access.log;

    return 301 https://nanosecond.cn$request_uri;
}
```





/etc/nginx/nginx.conf

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
	include /home/leixing/webapp/crmeb/crmeb_java/nginx_conf/*.conf;
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



配置完成后执行

```bash
nginx -s reload
```



