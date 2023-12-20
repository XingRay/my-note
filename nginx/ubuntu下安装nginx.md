## 在Ubuntu系统安装中安装nginx

### 1 安装nginx

```bash
sudo apt-get install nginx
```



### 2 修改配置文件

```bash
/etc/nginx
```

修改`nginx.conf`



### 3 重启nginx

```bash
sudo nginx -s reload
```



nginx的日志默认配置

```nginx
access_log /var/log/nginx/access.log;
error_log /var/log/nginx/error.log;
```

可以使用 

```bash
tail -f /var/log/nginx/access.log
```

来进行监控

