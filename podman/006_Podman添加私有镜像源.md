## Podman添加私有镜像源

配置 registries.conf配置文件路径 /etc/containers/registries.conf

Podman 3.0.0+版本配置文件格式发生了变化，最显著的是现在默认会有一条 unqualified-search-registries = ["docker.io", "quay.io"]

按照如下格式添加镜像：

```properties
[[registry]]
prefix = "192.168.0.1:7000"
location = "192.168.0.1:7000"
insecure = true
```

其中

```bash
prefix是pull的时候指定的镜像前缀，
location是获取镜像的地址，如果不指定prefix则默认和location一致。
insecure=true表示允许通过HTTP协议来获取镜像，对于私有化部署/内网测试环境下无https证书的环境来说很有帮助。
```

如果配置错误可能会出现以下错误提示：

```bash
Error: error getting default registries to try: error loading registries configuration "/etc/containers/registries.conf": toml: cannot load TOML value of type map[string]interface {} into a Go slice

Error: error getting default registries to try: error loading registries configuration "/etc/containers/registries.conf": toml: cannot load TOML value of type []map[string]interface {} into a Go boolean
```

 

Podman 3.0之前配置文件格式如下：

[registries.search]
registries = ['docker.io']
[registries.insecure]
registries = ['192.168.0.1:7000']
其中registries.search与新版unqualified-search-registries含义相同，registries.insecure表示这些registry允许不安全的HTTP拉取