

## 安装cfssl

CFSSL是CloudFlare开源的一款PKI/TLS工具。 CFSSL 包含一个命令行工具 和一个用于 签名，验证并且捆绑TLS证书的 HTTP API 服务。 使用Go语言编写。

Github 地址： https://github.com/cloudflare/cfssl



安装：去官网下载  `cfssl-certinfo_linux-amd64` `cfssljson_linux-amd64` `cfssl_linux-amd64` 这三个组件

目前最新版本:

https://github.com/cloudflare/cfssl/releases/download/v1.6.4/cfssl_1.6.4_linux_amd64

https://github.com/cloudflare/cfssl/releases/download/v1.6.4/cfssl-certinfo_1.6.4_linux_amd64

https://github.com/cloudflare/cfssl/releases/download/v1.6.4/cfssljson_1.6.4_linux_amd64

下载后上传至服务器 `k8s_install/cfssl` 目录



以下是一个 shell 脚本的示例，它会将当前目录下 cfssl 子目录中的文件到 /usr/bin 目录，并为这些文件添加执行权限：你可以通过使用 ls 命令来获取当前 cfssl 目录中的文件列表，然后动态地设置 files 数组。以下是相应的修改脚本：

```shell
vi cfssl_install.sh
```



```shell
#!/bin/bash

# 获取当前 cfssl 目录中的文件列表
files=$(ls cfssl)

# 循环遍历所有节点，并进行文件复制和权限设置

for file in $files; do
	cp "cfssl/$file" "/usr/bin/$file"
	chmod +x "/usr/bin/$file"
	echo "Copied and chmod +x $file."
done


echo "File copying and permission setting complete."
```

这个脚本中，我们使用了 ls cfssl 来获取当前 cfssl 目录中的文件列表，并将其赋值给 files 变量。然后脚本会循环遍历所有节点和文件，使用 scp 命令将每个文件复制到目标节点的 /usr/bin 目录，并使用 SSH 命令为每个文件添加执行权限。

将以上内容保存为一个文件（比如 cfssl_install.sh），然后给脚本执行权限：

    chmod +x cfssl_install.sh

然后你就可以在任意节点上执行这个脚本：

    ./cfssl_install.sh

这个脚本会动态地获取 cfssl 目录中的文件列表，并将它们复制到所有节点的 /usr/bin 目录下，并为这些文件添加执行权限。



卸载

当你想要删除之前复制的文件，可以编写一个简单的 shell 脚本来卸载这些文件。以下是一个示例脚本，它会删除 `/usr/bin` 目录下的特定文件：

```shell
#!/bin/bash

# 列出要删除的文件列表
files=(
  "cfssl"
  "cfssl-certinfo"
  "cfssljson"
)

# 循环遍历文件列表并删除文件
for file in "${files[@]}"; do
  rm "/usr/bin/$file"
  echo "Removed $file from /usr/bin/"
done

echo "File removal complete."
```

将以上内容保存为一个文件（比如 `uninstall.sh`），然后给脚本执行权限：

```shell
chmod +x cfssl_uninstall.sh
```

然后你就可以在终端中执行这个脚本：

```shell
./cfssl_uninstall.sh
```

这个脚本会遍历指定的文件列表，并从 `/usr/bin` 目录中删除这些文件。

请确保在执行卸载脚本之前备份重要文件，以免误删除。



## 8 生成证书

创建一个ssl目录

```shell
mkdir ssl
cd ssl
```



再创建k8s目录

```shell
mkdir k8s
cd k8s
```

创建配置文件

```shell
vi ca-config.json
```

```json
{
	"signing":{
		"default":{
			"expiry":"87600h"
		},
		"profiles":{
			"kubernetes":{
				"usages":[
					"signing",
					"key encipherment",
					"server auth",
					"client auth"
				],
				"expiry":"87600h"
			}
		}
	}
}
```

常见ca申请书:

```shell
vi ca-csr.json
```

```json
{
  "CN": "kubernetes",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "L": "BeiJing",
      "O": "kubernetes",
      "ST": "BeiJing",
      "OU": "kubernetes"
    }
  ]
}
```

生成ca证书

```shell
cfssl gencert -initca ca-csr.json | cfssljson -bare ca
```

```shell
root@k8s-master01:~/k8s_install/ssl/k8s# cfssl gencert -initca ca-csr.json | cfssljson -bare ca
2023/08/21 13:38:33 [INFO] generating a new CA key and certificate from CSR
2023/08/21 13:38:33 [INFO] generate received request
2023/08/21 13:38:33 [INFO] received CSR
2023/08/21 13:38:33 [INFO] generating key: rsa-2048
2023/08/21 13:38:33 [INFO] encoded CSR
2023/08/21 13:38:33 [INFO] signed certificate with serial number 638546255871888892923988801920906380534866016781
root@k8s-master01:~/k8s_install/ssl/k8s# ls
ca-config.json  ca.csr  ca-csr.json  ca-key.pem  ca.pem
```

生成CA所必需的文件ca-key.pem（私钥）和ca.pem（证书），还会生成ca.csr（证书签名请求），用于交叉签名或重新签名。  



生成ca配置

client certificate： 用于服务端认证客户端,例如etcdctl、etcd proxy、fleetctl、docker 客户端

server certificate: 服务端使用，客户端以此验证服务端身份,例如docker服务端、kube-apiserver

peer certificate: 双向证书，用于 etcd 集群成员间通信



创建ca配置文件  ca-config.json

相当于证书颁发机构的工作规章制度

"ca-config.json"：可以定义多个 profiles，分别指定不同的过期时间、使用场景等参数；后续在签名证书时使用某个 profile

"signing"：表示该证书可用于签名其它证书；生成的 ca.pem 证书中 CA=TRUE

"server auth"：表示client可以用该 CA 对server提供的证书进行验证

 "client auth"：表示server可以用该CA对client提供的证书进行验证

```json
{
  "signing": {
    "default": {
      "expiry": "43800h"
    },
    "profiles": {
      "server": {
        "expiry": "43800h",
        "usages": [
          "signing",
          "key encipherment",
          "server auth"
        ]
      },
      "client": {
        "expiry": "43800h",
        "usages": [
          "signing",
          "key encipherment",
          "client auth"
        ]
      },
      "peer": {
        "expiry": "43800h",
        "usages": [
          "signing",
          "key encipherment",
          "server auth",
          "client auth"
        ]
      },
      "kubernetes": {
        "expiry": "43800h",
        "usages": [
          "signing",
          "key encipherment",
          "server auth",
          "client auth"
        ]
      },
      "etcd": {
        "expiry": "43800h",
        "usages": [
          "signing",
          "key encipherment",
          "server auth",
          "client auth"
        ]
      }
    }
  }
}
```



准备一个证书申请请求书 `csr.json` 。证书机构就会根据我们请求签发证书,  使用命令打印模板

```
cfssl print-defaults csr
```

```json
{
    "CN": "example.net",
    "hosts": [
        "example.net",
        "www.example.net"
    ],
    "key": {
        "algo": "ecdsa",
        "size": 256
    },
    "names": [
        {
            "C": "US",
            "ST": "CA",
            "L": "San Francisco"
        }
    ]
}
```

"CN": "example.net", 浏览器验证该字段是否合法，一般写域名，非常重要.  

创建ca证书签名 ca-csr.json

"CN"：Common Name，从证书中提取该字段作为请求的用户名 (User Name)；浏览器使用该字段验证网站是否合法； 

"O"：Organization，从证书中提取该字段作为请求用户所属的组 (Group)；

这两个参数在后面的 kubernetes 启用 RBAC 模式中很重要，因为需要设置kubelet、admin 等角色权限，那么在配置证书的时候就必须配置对了，具体后面在部署 kubernetes 的时候会进行讲解。在etcd这两个参数没太大的重要意义，跟着配置就好。

```shell
vi ca-csr.json
```

```json
{
  "CN": "SelfSignedCa",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "L": "beijing",
      "O": "cfssl",
      "ST": "beijing",
      "OU": "System"
    }
  ]
}
```



生成ca证书和私钥  

```shell
cfssl gencert -initca ca-csr.json | cfssljson -bare ca -
```

```shell
root@k8s-master01:~/k8s_install/ssl/k8s# cfssl gencert -initca ca-csr.json | cfssljson -bare ca -
2023/08/21 14:11:01 [INFO] generating a new CA key and certificate from CSR
2023/08/21 14:11:01 [INFO] generate received request
2023/08/21 14:11:01 [INFO] received CSR
2023/08/21 14:11:01 [INFO] generating key: rsa-2048
2023/08/21 14:11:01 [INFO] encoded CSR
2023/08/21 14:11:01 [INFO] signed certificate with serial number 493471227768287772316098840576288191284241835548
root@k8s-master01:~/k8s_install/ssl/k8s# ls
ca-config.json  ca.csr  ca-csr.json  ca-key.pem  ca.pem
```

ca.csr   证书签名请求, 用于交叉签名或重新签名。  

ca.pem  ca公钥

ca-key.pem  ca私钥,妥善保管



创建etcd证书签名 etcd-csr.json

```shell
vi etcd-csr.json
```

```json
{
  "CN": "etcd",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "L": "shanghai",
      "O": "etcd",
      "ST": "shanghai",
      "OU": "System"
    }
  ]
}
```



生成etcd证书  

```shell
cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=etcd etcd-csr.json | cfssljson -bare etcd
```

```shell
root@k8s-master01:~/k8s_install/ssl/k8s# cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=etcd etcd-csr.json | cfssljson -bare etcd
2023/08/21 14:16:18 [INFO] generate received request
2023/08/21 14:16:18 [INFO] received CSR
2023/08/21 14:16:18 [INFO] generating key: rsa-2048
2023/08/21 14:16:18 [INFO] encoded CSR
2023/08/21 14:16:18 [INFO] signed certificate with serial number 504898311543665953941966912219142147336727229124
2023/08/21 14:16:18 [WARNING] This certificate lacks a "hosts" field. This makes it unsuitable for
websites. For more information see the Baseline Requirements for the Issuance and Management
of Publicly-Trusted Certificates, v.1.1.6, from the CA/Browser Forum (https://cabforum.org);
specifically, section 10.2.3 ("Information Requirements").
root@k8s-master01:~/k8s_install/ssl/k8s# ls
ca-config.json  ca.csr  ca-csr.json  ca-key.pem  ca.pem  etcd.csr  etcd-csr.json  etcd-key.pem  etcd.pem
```



创建kubernetes证书签名(kubernetes-csr.json)  

```json
{
  "CN": "kubernetes",
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "L": "shanghai",
      "O": "kubernetes",
      "ST": "shanghai",
      "OU": "System"
    }
  ]
}
```

生成k8s证书

```shell
cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes kubernetes-csr.json | cfssljson -bare kubernetes
```

```shell
root@k8s-master01:~/k8s_install/ssl/k8s# cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes kubernetes-csr.json | cfssljson -bare kubernetes
2023/08/21 14:19:20 [INFO] generate received request
2023/08/21 14:19:20 [INFO] received CSR
2023/08/21 14:19:20 [INFO] generating key: rsa-2048
2023/08/21 14:19:20 [INFO] encoded CSR
2023/08/21 14:19:20 [INFO] signed certificate with serial number 453460888699692490471303734356758149505949678045
2023/08/21 14:19:20 [WARNING] This certificate lacks a "hosts" field. This makes it unsuitable for
websites. For more information see the Baseline Requirements for the Issuance and Management
of Publicly-Trusted Certificates, v.1.1.6, from the CA/Browser Forum (https://cabforum.org);
specifically, section 10.2.3 ("Information Requirements").
root@k8s-master01:~/k8s_install/ssl/k8s# ls
ca-config.json  ca.csr  ca-csr.json  ca-key.pem  ca.pem  etcd.csr  etcd-csr.json  etcd-key.pem  etcd.pem  kubernetes.csr  kubernetes-csr.json  kubernetes-key.pem  kubernetes.pem
```

kubernetes.csr 

kubernetes-key.pem 

kubernetes.pem



最后校验证书是否合适  

```shell
openssl x509 -in ca.pem -text -noout
openssl x509 -in etcd.pem -text -noout
openssl x509 -in kubernetes.pem -text -noout
```

