# 在Ubuntu上安装Harbor



## 1 下载安装脚本

下载离线安装包

https://github.com/goharbor/harbor/releases

当前最新版本为:

https://github.com/goharbor/harbor/releases/download/v2.8.2/harbor-offline-installer-v2.8.2.tgz

将下载的 harbor-offline-installer-v2.8.2.tgz 上传到服务器, 解压

```bash
tar -xzvf harbor-offline-installer-v2.8.2.tgz
```



创建目录

```bash
mkdir -p /root/develop/harbor
```



## 2 配置https

2.1 Generate a Certificate Authority Certificate

```bash
openssl genrsa -out ca.key 4096
```

使用域名

```bash
openssl req -x509 -new -nodes -sha512 -days 3650 \
 -subj "/C=CN/ST=Beijing/L=Beijing/O=my-harbor/OU=Personal/CN=my-harbor.com" \
 -key ca.key \
 -out ca.crt
```

使用ip

```bash
openssl req -x509 -new -nodes -sha512 -days 3650 \
 -subj "/C=CN/ST=Beijing/L=Beijing/O=my-harbor/OU=Personal/CN=192.168.0.140" \
 -key ca.key \
 -out ca.crt
```



2.2 Generate a Server Certificate

```bash
openssl genrsa -out my-harbor.com.key 4096
```

```bash
openssl req -sha512 -new \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=my-harbor/OU=Personal/CN=my-harbor.com" \
    -key my-harbor.com.key \
    -out my-harbor.com.csr
```

使用ip : 

```bash
openssl genrsa -out 192.168.0.140.key 4096
```

```bash
openssl req -sha512 -new \
    -subj "/C=CN/ST=Beijing/L=Beijing/O=my-harbor/OU=Personal/CN=192.168.0.140" \
    -key 192.168.0.140.key \
    -out 192.168.0.140.csr
```



2.3 Generate an x509 v3 extension file

```bash
cat > v3.ext <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1=my-harbor.com
DNS.2=my-harbor
DNS.3=hostname
EOF
```

```bash
openssl x509 -req -sha512 -days 3650 \
    -extfile v3.ext \
    -CA ca.crt -CAkey ca.key -CAcreateserial \
    -in my-harbor.com.csr \
    -out my-harbor.com.crt
```



使用IP

```bash
cat > v3.ext <<-EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
IP.1 = 192.168.0.140
# IP.2 = 172.16.24.85
EOF
```

```bash
openssl x509 -req -sha512 -days 3650 \
    -extfile v3.ext \
    -CA ca.crt -CAkey ca.key -CAcreateserial \
    -in 192.168.0.140.csr \
    -out 192.168.0.140.crt
```



Convert `yourdomain.com.crt` to `yourdomain.com.cert`, for use by Docker.

The Docker daemon interprets `.crt` files as CA certificates and `.cert` files as client certificates.

```bash
openssl x509 -inform PEM -in my-harbor.com.crt -out my-harbor.com.cert
```

使用IP

```bash
openssl x509 -inform PEM -in 192.168.0.140.crt -out 192.168.0.140.cert
```



保存证书到harbor服务器指定目录

创建目录

```bash
mkdir -p /root/develop/harbor/data/cert
```

```bash
cp my-harbor.com.crt /root/develop/harbor/data/cert/
cp my-harbor.com.key /root/develop/harbor/data/cert/
```

使用IP

```bash
cp 192.168.0.140.crt /root/develop/harbor/data/cert/
cp 192.168.0.140.key /root/develop/harbor/data/cert/
```



Copy the server certificate, key and CA files into the Docker certificates folder on the Harbor host. You must create the appropriate folders first.

```bash
mkdir -p /etc/docker/certs.d/
```

```bash
mkdir -p /etc/docker/certs.d/my-harbor.com:50003
```

使用ip

```bash
mkdir -p /etc/docker/certs.d/192.168.0.140:50003
```



### 在docker中安装证书

保存证书到docker证书目录

```sh
cp my-harbor.com.cert /etc/docker/certs.d/my-harbor.com:50003/
cp my-harbor.com.key /etc/docker/certs.d/my-harbor.com:50003/
cp ca.crt /etc/docker/certs.d/my-harbor.com:50003/
```

使用IP

```bash
cp 192.168.0.140.cert /etc/docker/certs.d/192.168.0.140:50003/
cp 192.168.0.140.key /etc/docker/certs.d/192.168.0.140:50003/
cp ca.crt /etc/docker/certs.d/192.168.0.140:50003/
```



重启docker,docker会自动加载证书

```bash
systemctl restart docker
```



### 在ubuntu中安装证书

1 复制证书文件到证书目录

```sh
cp my-harbor.com.cert /usr/local/share/ca-certificates/harbor-my-harbor.com.cert
cp my-harbor.com.key /usr/local/share/ca-certificates/harbor-my-harbor.com.key
cp ca.crt /usr/local/share/ca-certificates/harbor-ca.crt
```

使用IP

```bash
cp 192.168.0.140.cert /usr/local/share/ca-certificates/harbor-192.168.0.140.cert
cp 192.168.0.140.key /usr/local/share/ca-certificates/harbor-192.168.0.140.key
cp ca.crt /usr/local/share/ca-certificates/harbor-ca.crt
```



2 刷新证书

update-ca-certificates 会添加 /etc/ca-certificates.conf 配置文件中指定的证书，另外所有 /usr/local/share/ca-certificates/*.crt 会被列为隐式信任

```bash
sudo update-ca-certificates
```

后续有需要可以删除证书

```bash
sudo rm /usr/local/share/ca-certificates/root_ca.crt
```

```bash
sudo update-ca-certificates --fresh
```



### 向windows中的 docker desktop 导入证书

进入设置页, Docker Engine, 修改设置如下 :

```bash
{
  "builder": {
    "features": {
      "buildkit": true
    },
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://jkzyghm3.mirror.aliyuncs.com",
    "https://docker.mirrors.ustc.edu",
    "https://registry.docker-cn"
  ],
  "tlscacert": "D:\\appdata\\harbor\\cert\\192.168.0.140\\ca.crt"
}
```





## 3 配置Harbor安装脚本

```bash
 cp harbor.yml.tmpl harbor.yml
```

修改配置文件

```yaml
# The IP address or hostname to access admin UI and registry service.
# DO NOT use localhost or 127.0.0.1, because Harbor needs to be accessed by external clients.
hostname: my-harbor.com

# http related config
#http:
  # port for http, default is 80. If https enabled, this port will redirect to https port
#  port: 80

# https related config
https:
  # https port for harbor, default is 443
  port: 50003
  # The path of cert and key files for nginx
  certificate: /root/develop/harbor/data/cert/harbor-my-harbor.com.cert
  private_key: /root/develop/harbor/data/cert/harbor-my-harbor.com.key
  
# # Uncomment following will enable tls communication between all harbor components
# internal_tls:
#   # set enabled to true means internal tls is enabled
#   enabled: true
#   # put your cert and key files on dir
#   dir: /etc/harbor/tls/internal

# Uncomment external_url if you want to enable external proxy
# And when it enabled the hostname will no longer used
external_url: https://my-harbor.com:50003
```

 使用IP

```yaml
# The IP address or hostname to access admin UI and registry service.
# DO NOT use localhost or 127.0.0.1, because Harbor needs to be accessed by external clients.
hostname: 192.168.0.140

# http related config
#http:
  # port for http, default is 80. If https enabled, this port will redirect to https port
#  port: 50002

# https related config
https:
  # https port for harbor, default is 443
  port: 50003
  # The path of cert and key files for nginx
  certificate: /root/develop/harbor/data/cert/192.168.0.140.crt
  private_key: /root/develop/harbor/data/cert/192.168.0.140.key

# # Uncomment following will enable tls communication between all harbor components
# internal_tls:
#   # set enabled to true means internal tls is enabled
#   enabled: true
#   # put your cert and key files on dir
#   dir: /etc/harbor/tls/internal

# Uncomment external_url if you want to enable external proxy
# And when it enabled the hostname will no longer used
external_url: https://192.168.0.140:50003
```

**注意一定要配置 `external_url`** 



执行prepare重新生成一遍配置

```
./prepare
```



开始安装

You can install Harbor in different configurations:

Just Harbor, without Notary and Trivy

```bash
sudo ./install.sh
```

Harbor with Notary

```bash
sudo ./install.sh --with-notary
```

Harbor with Trivy

```bash
sudo ./install.sh --with-trivy
```

Harbor with Notary and Trivy

```bash
sudo ./install.sh --with-notary --with-trivy
```



这里选择

```bash
sudo ./install.sh --with-trivy
```



安装完成后登录

https://my-harbor.com:50003

使用IP

https://192.168.0.140:50003

账号: admin / Harbor12345



#### 创建机器人账号

修改机器人账号的默认后缀

`系统管理-配置管理-系统设置-机器人账户名称前缀`

将默认的`robot$` 修改为 `robot_` , `$` 符号会导致 devops 流水线异常 . 

创建账号 admin ( robot_admin ) ,生成 token 

```bash
rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e
```



登录测试:

```bash
docker login my-harbor.com:50003 -u robot_admin -p rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e
```

```bash
podman login my-harbor.com:50003 -u robot_admin -p rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e
```

```bash
nerdctl login my-harbor.com:50003 -u robot_admin -p rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e
```

使用IP:

```bash
docker login 192.168.0.140:50003 -u robot_admin -p rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e
```

```bash
podman login 192.168.0.140:50003 -u robot_admin -p rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e
```

```bash
nerdctl login 192.168.0.140:50003 -u robot_admin -p rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e
```





### 客户端集群配置

### 同步证书

所有节点执行

```bash
mkdir -p /root/harbor/ca
```

复制证书文件

```
scp -rP 22 root@192.168.0.140:/root/develop/harbor/tmp/my-harbor.com.cert /root/harbor/ca/my-harbor.com.cert
scp -rP 22 root@192.168.0.140:/root/develop/harbor/tmp/my-harbor.com.key /root/harbor/ca/my-harbor.com.key
scp -rP 22 root@192.168.0.140:/root/develop/harbor/tmp/ca.crt /root/harbor/ca/ca.crt
```

使用IP

```bash
scp -rP 22 root@192.168.0.140:/root/develop/harbor/tmp/192.168.0.140.cert /root/harbor/ca/192.168.0.140.cert
scp -rP 22 root@192.168.0.140:/root/develop/harbor/tmp/192.168.0.140.key /root/harbor/ca/192.168.0.140.key
scp -rP 22 root@192.168.0.140:/root/develop/harbor/tmp/ca.crt /root/harbor/ca/ca.crt
```



### 在docker中安装证书

Copy the server certificate, key and CA files into the Docker certificates folder on the Harbor host. You must create the appropriate folders first.

```bash
mkdir -p /etc/docker/certs.d/
```

```bash
mkdir -p /etc/docker/certs.d/my-harbor.com:50003
```

使用ip

```bash
mkdir -p /etc/docker/certs.d/192.168.0.140:50003
```



保存证书到docker证书目录

```sh
cp /root/harbor/ca/my-harbor.com.cert /etc/docker/certs.d/my-harbor.com:50003/
cp /root/harbor/ca/my-harbor.com.key /etc/docker/certs.d/my-harbor.com:50003/
cp /root/harbor/ca/ca.crt /etc/docker/certs.d/my-harbor.com:50003/
```

使用IP

```bash
cp /root/harbor/ca/192.168.0.140.cert /etc/docker/certs.d/192.168.0.140:50003/
cp /root/harbor/ca/192.168.0.140.key /etc/docker/certs.d/192.168.0.140:50003/
cp /root/harbor/ca/ca.crt /etc/docker/certs.d/192.168.0.140:50003/
```



重启docker,docker会自动加载证书

```bash
systemctl restart docker
```



### 在ubuntu中安装证书

1 复制证书文件到证书目录

```sh
cp /root/harbor/ca/my-harbor.com.cert /usr/local/share/ca-certificates/harbor-my-harbor.com.cert
cp /root/harbor/ca/my-harbor.com.key /usr/local/share/ca-certificates/harbor-my-harbor.com.key
cp /root/harbor/ca/ca.crt /usr/local/share/ca-certificates/harbor-ca.crt
```

使用IP

```bash
cp /root/harbor/ca/192.168.0.140.cert /usr/local/share/ca-certificates/harbor-192.168.0.140.cert
cp /root/harbor/ca/192.168.0.140.key /usr/local/share/ca-certificates/harbor-192.168.0.140.key
cp /root/harbor/ca/ca.crt /usr/local/share/ca-certificates/harbor-ca.crt
```



2 刷新证书

update-ca-certificates 会添加 /etc/ca-certificates.conf 配置文件中指定的证书，另外所有 /usr/local/share/ca-certificates/*.crt 会被列为隐式信任

```bash
sudo update-ca-certificates
```

后续有需要可以删除证书

```bash
sudo rm /usr/local/share/ca-certificates/root_ca.crt
```

```bash
sudo update-ca-certificates --fresh
```



#### 在windows中安装证书

将ca.crt下载到windows系统的任意目录中,双击打开

点击 `安装证书` 

存储位置: 本地计算机

将所有的证书都放入下列存储

证书存储:

​	受信任的根证书颁发机构

点击确定即可



重启在dockerdesktop中重启 Docker Engine

执行命令:

```bash
docker login 192.168.0.140:50003 -u robot_admin -p rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e
```

如果出现错误, 可以尝试删除 `C:\Users\<user-name>\.docker` 目录再重启 Docker Engine





### Harbor服务操作

#### 重启Harbor

安装docker-compose

```bash
apt-get install docker-compose
```

切换到harbor安装包解压目录

```bash
cd /root/develop/harbor/harbor
```

停止harbor

```bash
docker-compose down
```

配置文件有改动时需要执行, 这里将https端口由443修改为 50003

```bash
vi harbor.yml
```

```yaml
hostname: 192.168.0.140

# http related config
#http:
  # port for http, default is 80. If https enabled, this port will redirect to https port
#  port: 50002

# https related config
https:
  # https port for harbor, default is 443
  port: 50003
  # The path of cert and key files for nginx
  certificate: /root/develop/harbor/data/cert/192.168.0.140.crt
  private_key: /root/develop/harbor/data/cert/192.168.0.140.key

# # Uncomment following will enable tls communication between all harbor components
# internal_tls:
#   # set enabled to true means internal tls is enabled
#   enabled: true
#   # put your cert and key files on dir
#   dir: /etc/harbor/tls/internal

# Uncomment external_url if you want to enable external proxy
# And when it enabled the hostname will no longer used
external_url: https://192.168.0.140:50003
```

```
./prepare
```

重新启动harbor

```bash
sudo ./install.sh --with-trivy
```

或者直接启动

```bash
docker-compose up -d
```



#### 关闭Harbor

```
cd /root/develop/harbor/harbor
```

```bash
docker-compose down -v
```

或者

```bash
docker-compose stop
```



#### 卸载Harbor

```
cd /root/develop/harbor/harbor
```

```bash
docker-compose stop
```

```bash
rm -rf /root/develop/harbor/harbor
```

