# NGINX高可用部署



## 1 进行编译

```shell
# 安装编译环境
yum install gcc -y

# 下载解压nginx二进制文件
# wget http://nginx.org/download/nginx-1.25.1.tar.gz
tar xvf nginx-*.tar.gz
cd nginx-*

# 进行编译
./configure --with-stream --without-http --without-http_uwsgi_module --without-http_scgi_module --without-http_fastcgi_module
make && make install 

# 拷贝编译好的nginx
node='k8s-master02 k8s-master03 k8s-node01 k8s-node02'
for NODE in $node; do scp -r /usr/local/nginx/ $NODE:/usr/local/nginx/; done

# 这是一系列命令行指令，用于编译和安装软件。
# 
# 1. `./configure` 是用于配置软件的命令。在这个例子中，配置的软件是一个Web服务器，指定了一些选项来启用流模块，并禁用了HTTP、uwsgi、scgi和fastcgi模块。
# 2. `--with-stream` 指定启用流模块。流模块通常用于代理TCP和UDP流量。
# 3. `--without-http` 指定禁用HTTP模块。这意味着编译的软件将没有HTTP服务器功能。
# 4. `--without-http_uwsgi_module` 指定禁用uwsgi模块。uwsgi是一种Web服务器和应用服务器之间的通信协议。
# 5. `--without-http_scgi_module` 指定禁用scgi模块。scgi是一种用于将Web服务器请求传递到应用服务器的协议。
# 6. `--without-http_fastcgi_module` 指定禁用fastcgi模块。fastcgi是一种用于在Web服务器和应用服务器之间交换数据的协议。
# 7. `make` 是用于编译软件的命令。该命令将根据之前的配置生成可执行文件。
# 8. `make install` 用于安装软件。该命令将生成的可执行文件和其他必要文件复制到系统的适当位置，以便可以使用该软件。
# 
# 总之，这个命令序列用于编译一个配置了特定选项的Web服务器，并将其安装到系统中。
```



## 2 写入启动配置

在所有主机上执行

```shell
# 写入nginx配置文件
cat > /usr/local/nginx/conf/kube-nginx.conf <<EOF
worker_processes 1;
events {
    worker_connections  1024;
}
stream {
    upstream backend {
        least_conn;
        hash $remote_addr consistent;
        server 192.168.0.31:6443        max_fails=3 fail_timeout=30s;
        server 192.168.0.32:6443        max_fails=3 fail_timeout=30s;
        server 192.168.0.33:6443        max_fails=3 fail_timeout=30s;
    }
    server {
        listen 127.0.0.1:8443;
        proxy_connect_timeout 1s;
        proxy_pass backend;
    }
}
EOF
# 这段配置是一个nginx的stream模块的配置，用于代理TCP和UDP流量。
# 
# 首先，`worker_processes 1;`表示启动一个worker进程用于处理流量。
# 接下来，`events { worker_connections 1024; }`表示每个worker进程可以同时处理最多1024个连接。
# 在stream块里面，定义了一个名为`backend`的upstream，用于负载均衡和故障转移。
# `least_conn`表示使用最少连接算法进行负载均衡。
# `hash $remote_addr consistent`表示用客户端的IP地址进行哈希分配请求，保持相同IP的请求始终访问同一台服务器。
# `server`指令用于定义后端的服务器，每个服务器都有一个IP地址和端口号，以及一些可选的参数。
# `max_fails=3`表示当一个服务器连续失败3次时将其标记为不可用。
# `fail_timeout=30s`表示如果一个服务器被标记为不可用，nginx将在30秒后重新尝试。
# 在server块内部，定义了一个监听地址为127.0.0.1:8443的服务器。
# `proxy_connect_timeout 1s`表示与后端服务器建立连接的超时时间为1秒。
# `proxy_pass backend`表示将流量代理到名为backend的上游服务器组。
# 
# 总结起来，这段配置将流量代理到一个包含3个后端服务器的上游服务器组中，使用最少连接算法进行负载均衡，并根据客户端的IP地址进行哈希分配请求。如果一个服务器连续失败3次，则将其标记为不可用，并在30秒后重新尝试。


# 写入启动配置文件
cat > /etc/systemd/system/kube-nginx.service <<EOF
[Unit]
Description=kube-apiserver nginx proxy
After=network.target
After=network-online.target
Wants=network-online.target

[Service]
Type=forking
ExecStartPre=/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/kube-nginx.conf -p /usr/local/nginx -t
ExecStart=/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/kube-nginx.conf -p /usr/local/nginx
ExecReload=/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/kube-nginx.conf -p /usr/local/nginx -s reload
PrivateTmp=true
Restart=always
RestartSec=5
StartLimitInterval=0
LimitNOFILE=65536
 
[Install]
WantedBy=multi-user.target
EOF
# 这是一个用于kube-apiserver的NGINX代理的systemd单位文件。
# 
# [Unit]部分包含了单位的描述和依赖关系。它指定了在network.target和network-online.target之后启动，并且需要network-online.target。
# 
# [Service]部分定义了如何运行该服务。Type指定了服务进程的类型（forking表示主进程会派生一个子进程）。ExecStartPre指定了在服务启动之前需要运行的命令，用于检查NGINX配置文件的语法是否正确。ExecStart指定了启动服务所需的命令。ExecReload指定了在重新加载配置文件时运行的命令。PrivateTmp设置为true表示将为服务创建一个私有的临时文件系统。Restart和RestartSec用于设置服务的自动重启机制。StartLimitInterval设置为0表示无需等待，可以立即重启服务。LimitNOFILE指定了服务的文件描述符的限制。
# 
# [Install]部分指定了在哪些target下该单位应该被启用。
# 
# 综上所述，此单位文件用于启动和管理kube-apiserver的NGINX代理服务。它通过NGINX来反向代理和负载均衡kube-apiserver的请求。该服务会在系统启动时自动启动，并具有自动重启的机制。


# 设置开机自启

systemctl daemon-reload
# 用于重新加载systemd管理的单位文件。当你新增或修改了某个单位文件（如.service文件、.socket文件等），需要运行该命令来刷新systemd对该文件的配置。
systemctl enable --now kube-nginx.service
# 启用并立即启动kube-nginx.service单元。kube-nginx.service是kube-nginx守护进程的systemd服务单元。
systemctl restart kube-nginx.service
# 重启kube-nginx.service单元，即重新启动kube-nginx守护进程。
systemctl status kube-nginx.service
# kube-nginx.service单元的当前状态，包括运行状态、是否启用等信息。
```

