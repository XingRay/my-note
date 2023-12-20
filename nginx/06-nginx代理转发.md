```nginx
server {
    listen        8800;
    listen        [::]:8800;
    charset       utf-8;

	access_log  /home/leixing/webapp/myproject/myshop_java/api/log/access.log;

    location /api/front {
        proxy_pass	http://localhost:8901;
    }
    
    location / {
    	proxy_pass	http://localhost:8900;
    }
    
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   static/html;
    }
}
```

