# Linux 使用openssl x509方式签发证书


## 前言

Linux 使用openssl ca方式签发证书（不推荐用这种方式）：https://blog.csdn.net/QianLiStudent/article/details/109291424

客服端到服务端或服务端到服务端的请求方式通常是http居多（这里只考虑一般的系统），但是考虑到安全性的问题，我们会采用给系统添加一个证书来做认证，证书相当于一个身份认证。

之前没有接触过证书的时候，觉得证书的生成步骤很复杂，而且命令又长，但如果对流程做一下分解就好理解了。

## 概念

根证书：也叫自签名证书、CA证书，由私钥直接生成，用于给其他的证书签名；
服务端证书：由CA证书签名后在服务端配置，比如nginx；
客户端证书：由CA证书签名后服务端保存，并发送给客户端进行配置；
Tip：下面统一称为CA证书和应用证书（服务端证书、客户端证书）

## 准备

安装openssl，通常系统中是默认安装好的，可通过 openssl version 查看openssl的版本信息，若报不存在则先安装openssl。

可以通过openssl version -a查看openssl的基本信息：

## 步骤

证书的生成步骤有5步：
1、生成CA证书的私钥（用来生成CA证书）；

openssl genrsa -des3 -out ca.key 1024
#genrsa指定加密算法
#des3是一种加密方式，如果指定则需要设置密码，如果不指定则不用设置密码
#out表示输出的文件名，这里是cakey.pem
#1024表示生成的私钥长度
1.1、解除私钥的加密：在使用上述命令生成一个私钥文件的时候会要求输入密码（在指定-des3参数时需要设置密码，若无指定则无需输入密码，本步骤可跳过），这个密码即为该私钥的加密密码，后续只要操作到该私钥文件的时候均需要输入密码去解密，可以使用下面的命令来解除密码：

openssl rsa -in ca.key -out ca.key
#rsa表示加密算法
#in表示输入的文件
#out表示输出的文件
2、用CA私钥来生成CA证书，即CA证书自签名（用来给服务端证书或客户端证书做签名）；

openssl req -new -x509 -key ca.key -out ca.crt -days 180
#req表示管理证书签名请求，这里是签名场景，所以得用到
#new表示生成一个CA证书文件或证书签名请求（csr）文件
#x509是CA证书专属的参数，表示用来做证书签名
#key表示指定私钥文件
#out表示输出的证书文件
#day表示证书的有效期，不填则使用openssl.cnf配置文件中的默认值

tip：上图红框中的参数最好是保持一致的，而Common Name和Email Address则是可选填项，不填写直接按回车就跳过了；
3、生成应用证书的私钥；

openssl genrsa -des3 -out app.key 2048
#rsa表示加密算法
#in表示输入的文件
#out表示输出的文件
tip：要解除该应用私钥的密码参考1.1；

4、用3中的应用私钥生成csr（Certificate Signing Request：证书签名请求）文件；

openssl req -new -key app.key -out app.csr
#req表示管理证书签名请求
#new表示创建证书（crt）文件或证书签名请求（csr）文件
#key表示指定私钥文件
#out表示指定输出文件，这里指证书签名请求文件
5、用CA证书给4中的csr签名得到使用证书；

openssl x509签发证书（需要事先创建一个v3.ext文件来做SAN扩展）

#SAN扩展文件内容
#IP选这个
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = IP:192.168.100.222	#至少要有这一行，这行即表示SAN扩展，表示指定证书给这个IP用
--------------------------------------------------------------------------------------
#域名选这个
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth
subjectAltName = @alt_names

[alt_names]
DNS.1=www.abc.com
DNS.2=www.def.com
#签发应用证书
openssl x509 -req -CA ca.crt -CAkey ca.key -CAcreateserial -extfile v3.ext -in app.csr -out app.crt -days 365
# CA指定CA证书
#CAkey指定CA证书的私钥
#CAcreateserial表示记录证书序列号的serial文件不存在的时候自动创建
#extfile指定一个SAN扩展文件
文章知识点与官方知识档案匹配，可进一步学习相关知识
————————————————
版权声明：本文为CSDN博主「QianLiStudent」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/QianLiStudent/article/details/109818208