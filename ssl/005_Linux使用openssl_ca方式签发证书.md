Linux 使用openssl ca方式签发证书

https://blog.csdn.net/QianLiStudent/article/details/109291424



前言
Linux 使用openssl x509方式签发证书（推荐用这种方式）：https://blog.csdn.net/QianLiStudent/article/details/109818208

客服端到服务端或服务端到服务端的请求方式通常是http居多（这里只考虑一般的系统），但是考虑到安全性的问题，我们会采用给系统添加一个证书来做认证，证书相当于一个身份认证。

之前没有接触过证书的时候，觉得证书的生成步骤很复杂，而且命令又长，但如果对流程做一下分解就好理解了。

概念
根证书：也叫自签名证书、CA证书，由私钥直接生成，用于给其他的证书签名；
服务端证书：由CA证书签名后在服务端配置，比如nginx；
客户端证书：由CA证书签名后服务端保存，并发送给客户端进行配置；
Tip：下面统一称为CA证书和应用证书（服务端证书、客户端证书）

准备
安装openssl，通常系统中是默认安装好的，可通过 openssl version 查看openssl的版本信息，若报不存在则先安装openssl。

可以通过openssl version -a查看openssl的基本信息：

去到该目录下打开openssl.cnf，这个配置文件是openssl生成证书的配置文件。

查看配置文件的内容，有几个地方需要特别关注。


42行：dir——是证书签发的工作目录，可以指定一个目录作为工作目录
43行：certs——证书存放目录，$dir指定的是42行dir指定的工作目录
44行：crl——crl存放的目录
45行：index.txt——记录证书的生成记录
48行：newcerts——证书的备份目录，生成证书后会在该目录中做一份刚生成的证书的备份
50行：cacert.pem——CA证书的名字，表示CA证书需要放置在工作目录下
51行：serial——序列号文件，用来记录签名证书的序列号
55行：private——CA证书私钥存放目录
55行：cakey.pem——CA证书的私钥文件名，需要存放在工作目录下的private目录下
举例：
1、创建目录/usr/lib/ssl/demoCA作为工作目录，则配置文件openssl.cnf中的dir则会认为当前目录为工作目录（参数参考上图）
2、事先创建上述目录和文件，以在生成证书的过程中使用（其中cakey.pem和cacert.pem是CA证书的私钥和证书，后续步骤会创建）



步骤
证书的生成步骤有5步：
1、生成CA证书的私钥（用来生成CA证书）；

openssl genrsa -des3 -out cakey.pem 1024
#genrsa指定加密算法
#des3是一种加密方式，如果指定则需要设置密码，如果不指定则不用设置密码
#out表示输出的文件名，这里是cakey.pem
#1024表示生成的私钥长度
1
2
3
4
5
生成的秘钥文件：


1.1、解除私钥的加密：在使用上述命令生成一个私钥文件的时候会要求输入密码（在指定-des3参数时需要设置密码，若无指定则无需输入密码，本步骤可跳过），这个密码即为该私钥的加密密码，后续只要操作到该私钥文件的时候均需要输入密码去解密，可以使用下面的命令来解除密码：

openssl rsa -in private/cakey.pem -out private/cakey.pem
#rsa表示加密算法
#in表示输入的文件
#out表示输出的文件
1
2
3
4


2、用CA私钥来生成CA证书，即CA证书自签名（用来给服务端证书或客户端证书做签名）；

openssl req -new -x509 -key private/cakey.pem -out cacert.pem -days 180
#req表示管理证书签名请求，这里是签名场景，所以得用到
#new表示生成一个CA证书文件或证书签名请求（csr）文件
#x509是CA证书专属的参数，表示用来做证书签名
#key表示指定私钥文件
#out表示输出的证书文件
#day表示证书的有效期，不填则使用openssl.cnf配置文件中的默认值
1
2
3
4
5
6
7

生成证书过程中的参数：

tip：上图红框中的参数最好是保持一致的，而Common Name和Email Address则是可选填项，不填写直接按回车就跳过了；

3、生成应用证书的私钥；

openssl genrsa -des3 -out app.key 2048
#rsa表示加密算法
#in表示输入的文件
#out表示输出的文件
1
2
3
4
生成应用的私钥：


tip：要解除该应用私钥的密码参考1.1；

4、用3中的应用私钥生成csr（Certificate Signing Request：证书签名请求）文件；

openssl req -new -key app.key -out app.csr
#req表示管理证书签名请求
#new表示创建证书（crt）文件或证书签名请求（csr）文件
#key表示指定私钥文件
#out表示指定输出文件，这里指证书签名请求文件
1
2
3
4
5


5、用CA证书给4中的csr签名得到使用证书；

openssl ca签发证书（需创建一开始说的那些文件，否则读取不到会报错，此种方式用得少）

openssl ca -cert cacert.pem -keyfile private/cakey.pem -config openssl.cnf -in app.csr -out app.crt -days 180
#cert表示CA证书
#keyfile表示CA证书的CA私钥
#config指定openssl.cnf配置文件
#in表示指定输入的文件，这里是待签名的csr文件
#out表示输出签名后的应用证书
#day表示应用证书的有效期
1
2
3
4
5
6
7


FAQ
Q：最后一步CA证书签发应用证书的时候报错：无法读取serial文件？

A：只需要改一下编码即可：echo 00 > serial ，然后重新签发证书


文章知识点与官方知识档案匹配，可进一步学习相关知识
