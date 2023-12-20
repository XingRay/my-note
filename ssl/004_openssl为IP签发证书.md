# openssl为IP签发证书（支持多IP/内外网）

时间:2022-10-23

本文章向大家介绍openssl为IP签发证书（支持多IP/内外网），主要内容包括背景、依赖、签发证书、利用nodejs项目验证证书是否可行、将证书导入本地（windows）、基本概念、基础应用、原理机制和需要注意的事项等，并结合实例形式分析了其使用技巧，希望通过本文能帮助到大家理解应用这部分内容。

> **参考文档**： [1. OpenSSL自签发配置有多域名或ip地址的证书](https://links.jianshu.com/go?to=https%3A%2F%2Fblog.csdn.net%2Fu013066244%2Farticle%2Fdetails%2F78725842) [2. 如何创建一个自签名的SSL证书(X509)](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.cnblogs.com%2Fdinglin1%2Fp%2F9279831.html) [3. 如何创建自签名证书？](https://links.jianshu.com/go?to=https%3A%2F%2Fwww.racent.com%2Fblog%2Farticle-how-to-create-a-self-signed-certificate)

## 背景

- 开启https必须要有ssl证书，而安全的证书来源于受信任的CA机构签发，通常需要付费，并且他们只能为域名和外网IP签发证书。
- 证书有两个基本目的：分发公有密钥和验证服务器的身份。只有当证书是由受信任的第三方所签署的情形下，服务器的身份才能得到恰当验证，因为任何攻击者都可以创建自签名证书并发起中间人攻击。
- 但自签名证书可应用于以下背景：
  - 企业内部网。当客户只需要通过本地企业内部网络时，中间人攻击几乎是完全没有机会的。
  - 开发服务器。当你只是在开发或测试应用程序时，花费额外的金钱去购买受信任的证书是完全没有必要的。
  - 访问量很小的个人站点。如果你有一个小型个人站点，而该站点传输的是不重要的信息，那么攻击者很少会有动机去攻击这些连接。

## 依赖

利用 OpenSSL 签发证书需要 OpenSSL 软件及库，一般情况下 CentOS、Ubuntu 等系统均已内置， 可执行 `openssl` 确认，如果提示 `oepnssl: command not found`,则需手动安装，以Centos为例：

```
yum install openssl openssl-devel -y
```

## 签发证书

#### step1: 生成证书请求文件

新建openssl.cnf，内容如下：

```
[req]
distinguished_name = req_distinguished_name
req_extensions = v3_req

[req_distinguished_name]
countryName = Country Name (2 letter code)
countryName_default = CH
stateOrProvinceName = State or Province Name (full name)
stateOrProvinceName_default = GD
localityName = Locality Name (eg, city)
localityName_default = ShenZhen
organizationalUnitName  = Organizational Unit Name (eg, section)
organizationalUnitName_default  = organizationalUnitName
commonName = Internet Widgits Ltd
commonName_max  = 64

[ v3_req ]
# Extensions to add to a certificate request
basicConstraints = CA:FALSE
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = @alt_names

[alt_names]

# 改成自己的域名
#DNS.1 = kb.example.com
#DNS.2 = helpdesk.example.org
#DNS.3 = systems.example.net

# 改成自己的ip
IP.1 = 172.16.24.143
IP.2 = 172.16.24.85
```

#### step2: 生成私钥

`san_domain_com` 为最终生成的文件名，一般以服务器命名，可改。

```
openssl genrsa -out san_domain_com.key 2048
```

#### step3: 创建CSR文件

创建CSR文件命令：

```
openssl req -new -out san_domain_com.csr -key san_domain_com.key -config openssl.cnf
```

执行后，系统会提示输入组织等信息，按提示输入如即可。

测试CSR文件是否生成成功，可以使用下面的命令：

```
openssl req -text -noout -in san_domain_com.csr

//执行后，会看到类似如下的信息：
Certificate Request:
    Data:
        Version: 0 (0x0)
        Subject: C=US, ST=MN, L=Minneapolis, OU=Domain Control Validated, CN=zz
        Subject Public Key Info:
            Public Key Algorithm: rsaEncryption
                Public-Key: (2048 bit)
                //...
```

#### step4: 自签名并创建证书

```
openssl x509 -req -days 3650 -in san_domain_com.csr -signkey san_domain_com.key -out san_domain_com.crt -extensions v3_req -extfile openssl.cnf
```

执行后，可看到本目录下多了以下三个文件

```
san_domain_com.crt

san_domain_com.csr

san_domain_com.key
```

至此，使用openssl生成证书已完成，以下 `nodejs项目验证` 和 `将证书导入本地` 仅是验证证书是否正常可用。

## 利用nodejs项目验证证书是否可行

#### step1. 环境

安装node和npm.

#### step2. 项目结构

```
ssl-test
├─ cert   //打包所需静态资源
│  ├─ san_domain_com.crt
│  └─ san_domain_com.key
├─ package.json
└─ https.js
```

#### step3. https.js

文件内容如下：

```
'use strict'
var https = require("https");
var fs = require('fs');
var options = {
    key: fs.readFileSync("./cert/san_domain_com.key", 'utf8'),
    cert: fs.readFileSync("./cert/san_domain_com.crt", 'utf8')
};

var app = https.createServer(options, function (req, res) {
    res.writeHead(200, {'Cntent-Type': 'text/plain'});

    res.end('Https !')
}).listen(443, '0.0.0.0');
```

#### step4. 启动项目

执行以下命令：

```
node https.js
```

执行后，即可访问 https://127.0.0.1，chrome浏览器上点击“不安全”->"证书"，即可查看证书详细。

1.png

## 将证书导入本地（windows）

上一步中，使用chrome访问 https://127.0.0.1 提示了“不安全”。

- 对于web端用户，可以“手动点击信任”来绕过此提示。
- 但安卓端、C++等终端需要调用https站点的API时，他们应该如何解决？有以下两种方式：
  - 客户端在代码层面直接忽略掉不安全的提示。（不提倡，某些恶意网站可能也会被忽略）
  - 将`san_domain_com.crt`证书导入到客户端中。

此处，以浏览器作为客户端演示如何将证书设置为受信任的证书。

#### step1. 环境

windows 7系统、chrome浏览器

#### step2. 背景

- 为什么使用自签发的证书时会提示不安全？

因为操作系统上会默认存有受信任机构CA的证书。—— `电脑的“运行”工具弹窗->输入"certmgr.msc"` 而我们自签发的证书不在这个信任列表中，需要手动导入到这个“白名单”中。

2.png

- https的认证流程是什么样子的？

image.png

你可能会问，为什么是这样的流程？详见下一篇文章 [https诞生背景及原理解析](https://links.jianshu.com/go?to=%5Bhttps%3A%2F%2Fwww.jianshu.com%2Fp%2F58ede2c83240%5D(https%3A%2F%2Fwww.jianshu.com%2Fp%2F58ede2c83240))

#### step3. 导入证书

右键"受信任的证书颁发机构"->"所有任务"->"导入"，按照提示选择`san_domain_com.crt`文件即可。

1.png

清除缓存，重启浏览器，浏览https://127.0.0.1，不再提示不安全了。

image.png

至此，完成了证书的生成和客户端的导入验证。