# 基于 KubeSphere 玩转 K8s ｜一文搞定 Harbor 离线部署

**本系列文档内容涵盖 (但不限于) 以下技术领域：**

- **KubeSphere**
- **Kubernetes**
- **Ansible**
- **自动化运维**
- **CNCF 技术栈**

## **1. 本文简介**

本文详细展示了采用 HTTPS 协议的 Harbor 的安装部署、安装后的初始化以及与 KubeSphere 对接的详细过程， 本文的安装部署方案适用于中小规模生产环境。

> 本文知识量

- 阅读时长：15分
- 行：775
- 单词：4400+
- 字符：22700+
- 图片：30 张

> **本文知识点**

- 定级：**入门级**
- Harbor 安装部署
- Harbor 基本配置
- Harbor 启用 HTTPS 访问
- KubeSphere 对接 Harbor
- CoreDNS 和 NodeLocalDNS 的配置
- KubeSphere 使用私有仓库创建工作负载

> **演示服务器配置**

| 主机名           | IP           | CPU  | 内存 | 系统盘 | 数据盘 | 用途                             |
| ---------------- | ------------ | ---- | ---- | ------ | ------ | -------------------------------- |
| zdeops-master    | 192.168.9.9  | 2    | 4    | 40     | 200    | Ansible 运维控制节点             |
| ks-k8s-master-0  | 192.168.9.91 | 8    | 32   | 40     | 200    | KubeSphere/k8s-master/k8s-worker |
| ks-k8s-master-1  | 192.168.9.92 | 8    | 32   | 40     | 200    | KubeSphere/k8s-master/k8s-worker |
| ks-k8s-master-2  | 192.168.9.93 | 8    | 32   | 40     | 200    | KubeSphere/k8s-master/k8s-worker |
| glusterfs-node-0 | 192.168.9.95 | 4    | 8    | 40     | 200    | GlusterFS                        |
| glusterfs-node-1 | 192.168.9.96 | 4    | 8    | 40     | 200    | GlusterFS                        |
| glusterfs-node-2 | 192.168.9.97 | 4    | 8    | 40     | 200    | GlusterFS                        |
| harbor           | 192.168.9.89 | 4    | 16   | 40     | 200    | Harbor 和 Gitlab                 |

> **演示环境涉及软件版本信息**

- 操作系统：**CentOS-7.9-x86_64**
- Ansible：**2.8.20**
- Harbor：**2.5.1**

## **2. 安装 Harbor 的前提条件**

### **2.1. 硬件**

下表列出了安装 Harbor 的最低和推荐的硬件配置要求。

| Resource | Minimum | Recommended |
| -------- | ------- | ----------- |
| CPU      | 2 CPU   | 4 CPU       |
| Mem      | 4 GB    | 8 GB        |
| Disk     | 40 GB   | 160 GB      |

### **2.2. 软件**

下表列出了安装 Harbor 需要安装的相关软件及版本。

| Software       | Version                       | Description                                                  |
| -------------- | ----------------------------- | ------------------------------------------------------------ |
| Docker engine  | Version 17.06.0-ce+ or higher | For installation instructions, see Docker Engine documentation |
| Docker Compose | Version 1.18.0 or higher      | For installation instructions, see Docker Compose documentation |
| Openssl        | Latest is preferred           | Used to generate certificate and keys for Harbor             |

### **2.3. 网络**

需要在安装 Harbor 的服务器上开放以下端口。

| Port | Protocol | Description                                                  |
| ---- | -------- | ------------------------------------------------------------ |
| 443  | HTTPS    | Harbor portal and core API accept HTTPS requests on this port. You can change this port in the configuration file. |
| 4443 | HTTPS    | Connections to the Docker Content Trust service for Harbor. Only required if Notary is enabled. You can change this port in the configuration file. |
| 80   | HTTP     | Harbor portal and core API accept HTTP requests on this port. You can change this port in the configuration file. |

## **3. Harbor 服务器初始化配置**

### **3.1. Ansible 增加 hosts**

```text
# 主要增加 harbor 节点配置, 注意没有主机组只有一条主机记录

harbor ansible_ssh_host=192.168.9.89 host_name=harbor
```

### **3.2. 检测服务器连通性**

```text
# 利用 ansible 检测服务器的连通性

cd /data/ansible/ansible-zdevops/inventories/dev/
source /opt/ansible2.8/bin/activate
ansible -m ping harbor
```

### **3.3. 初始化服务器配置**

```text
# 利用 ansible-playbook 初始化服务器配置

ansible-playbook ../../playbooks/init-base.yaml -l harbor
```

### **3.4. 挂载数据盘**

- **挂载数据盘**

```text
# 利用 ansible-playbook 初始化主机数据盘
# 注意 -e data_disk_path="/data" 指定挂载目录

ansible-playbook ../../playbooks/init-disk.yaml -e data_disk_path="/data" -l harbor
```

- **挂载验证**

```text
# 利用 ansible 验证数据盘是否格式化并挂载
ansible harbor -m shell -a 'df -h'

# 利用 ansible 验证数据盘是否配置自动挂载

ansible harbor -m shell -a 'tail -1  /etc/fstab'
```

## **4. Docker 安装配置**

- 安装配置 Docker 和 Docker Compose

```text
# 利用 ansible-playbook 安装配置 Docker
ansible-playbook ../../playbooks/deploy-docker.yaml -l harbor
```

## **5. Harbor 离线安装配置**

### **5.1. 准备域名 https 证书**

本文使用了已购域名加免费 SSL 证书的方式，如果是自定义域名或是 IP 的自签名证书请参考官方文档**[Configure HTTPS Access to Harbor](https://goharbor.io/docs/2.5.0/install-config/configure-https/)**。

建议购买一个域名，不要自定义域名也不要用 IP。

> **免费 SSL 证书**

- **阿里云 (一年)**
- 腾讯云 (一年)
- Let's Encrypt(90 天)

我的域名 ([http://registry.zdevops.com.cn](http://registry.zdevops.com.cn)) 是在阿里云买的，所有就选择阿里云的 SSL 免费证书，申请过程略。

申请后，下载 **Nginx** 或是**其他**类型的证书。

将下载的证书文件放到服务器指定目录。

```text
# 最小化安装的服务器需要安装 unzip
yum install unzip -y

unzip 7966948_registry.zdevops.com.cn_nginx.zip -d /data/harbor-certs
```

### **5.2. 离线安装包下载**

- 下载安装包

```text
cd /data
#wget https://github.com/goharbor/harbor/releases/download/v2.5.1/harbor-offline-installer-v2.5.1.tgz
curl https://github.com/goharbor/harbor/releases/download/v2.5.1/harbor-offline-installer-v2.5.1.tgz -O harbor-offline-installer-v2.5.1.tgz
```

- 解压安装包

```text
tar xzvf harbor-offline-installer-v2.5.1.tgz
```

- 更改解压后的目录名 (个人习惯)

```text
mv harbor harbor-v2.5.1
```

### **5.3. 配置 Harbor YML 文件**

本文只说明需要修改的重点参数，更完整的参数说明，请参考官方文档**[Configure the Harbor YML File](https://goharbor.io/docs/2.5.0/install-config/configure-yml-file/)**。

本文的 Harbor 使用默认的 HTTPS 443 端口，提供给内网客户端直接访问，不通过防火墙转发到外部，如果需要通过转发的方式暴露给外网，需要使用 **external_url** 的配置项。

- 编辑配置文件

```text
cd /data/harbor-v2.5.1/
cp harbor.yml.tmpl harbor.yml
vi harbor.yml
```

- 必须修改的参数

```text
# Harbor 服务器的主机名或是 IP
hostname: registry.zdevops.com.cn

# 生产环境一定要使用 https
https:
  # https 端口, 默认443, 根据实际环境修改
  port: 443
  # Nginx 使用的 cert 和 key 文件(绝对路径)
  certificate: /data/harbor-certs/7966948_registry.zdevops.com.cn.pem
  private_key: /data/harbor-certs/7966948_registry.zdevops.com.cn.key
  
# Harbor admin用户的初始密码，配置文件里可以不用改，但是部署完必须第一时间登录Harbor，更改密码
harbor_admin_password: Harbor12345

# Harbor DB configuration
database:
  # Harbor DB root用户的密码, 必须修改.
  password: root123

# Harbor数据存储路径
data_volume: /data/harbor
```

- 非必需但是重要参数

```text
# 如果 harbor 需要通过防火墙或是其他方式转发给外部访问，需要配置此参数为外网 IP 或是外部域名。
external_url: https://reg.mydomain.com:8433

# 如果是离线内网环境，并且启用trivy的场景，还需要配置以下两个参数
trivy:
  # skipUpdate The flag to enable or disable Trivy DB downloads from GitHub
  #
  # You might want to enable this flag in test or CI/CD environments to avoid GitHub rate limiting issues.
  # If the flag is enabled you have to download the `trivy-offline.tar.gz` archive manually, extract `trivy.db` and
  # `metadata.json` files and mount them in the `/home/scanner/.cache/trivy/db` path.
  skip_update: true
  #
  # The offline_scan option prevents Trivy from sending API requests to identify dependencies.
  # Scanning JAR files and pom.xml may require Internet access for better detection, but this option tries to avoid it.
  # For example, the offline mode will not try to resolve transitive dependencies in pom.xml when the dependency doesn't
  # exist in the local repositories. It means a number of detected vulnerabilities might be fewer in offline mode.
  # It would work if all the dependencies are in local.
  # This option doesn’t affect DB download. You need to specify "skip-update" as well as "offline-scan" in an air-gapped environment.
  offline_scan: true
```

### **5.4. 运行安装脚本**

官方默认安装命令没有启用 Notary, Trivy 和 Chart Repository Service。

- Notary：镜像签名认证
- **Trivy： 容器漏洞扫描**
- **Chart Repository Service： Helm chart 仓库服务**

**生产环境我启用了 Trivy 和 Chart Repository Service 功能，因此执行下面的命令。**

```text
./install.sh --with-trivy --with-chartmuseum
```

如果想启用所有插件，可以执行下面的命令。

```text
./install.sh --with-notary --with-trivy --with-chartmuseum
```

### **5.5. 安装成功效果**

能看到类似下面的输出，说明安装成功。

```text
[Step 5]: starting Harbor ...
[+] Running 13/13
 ⠿ Network harbor-v251_harbor-chartmuseum  Created                                                                                    0.2s
 ⠿ Network harbor-v251_harbor              Created                                                                                    0.1s
 ⠿ Container harbor-log                    Started                                                                                    1.5s
 ⠿ Container redis                         Started                                                                                    5.2s
 ⠿ Container registryctl                   Started                                                                                    5.2s
 ⠿ Container registry                      Started                                                                                    4.4s
 ⠿ Container harbor-db                     Started                                                                                    3.2s
 ⠿ Container harbor-portal                 Started                                                                                    5.3s
 ⠿ Container chartmuseum                   Started                                                                                    4.2s
 ⠿ Container trivy-adapter                 Started                                                                                    7.0s
 ⠿ Container harbor-core                   Started                                                                                    7.0s
 ⠿ Container harbor-jobservice             Started                                                                                    8.3s
 ⠿ Container nginx                         Started                                                                                    8.7s
✔ ----Harbor has been installed and started successfully.----
```

### **5.6. 客户端配置**

**由于是内网服务器配置的域名 [http://registry.zdevops.com.cn](http://registry.zdevops.com.cn)，所有访问 Harbor 的服务器需要手动配置 /etc/hosts 文件解析。**

```text
# 利用 ansible 配置服务器的/etc/hosts
ansible all -m shell -a 'echo "192.168.9.89   registry.zdevops.com.cn" >> /etc/hosts'
```

## **6. Harbor 初始化配置**

### **6.1. 修改 admin 用户密码**

初次登陆系统，必须修改 admin 用户的默认密码。

- 通过浏览器登录 Harbor 控制台，[https://registry.zdevops.com.cn](https://registry.zdevops.com.cn)。默认用户 admin，默认密码 Harbor12345。

![img](assets/v2-2bca5c173d3520e0143ba675516484d5_720w.webp)

kube-harbor-0

- 右上角 **admin**->**修改密码**。

![img](assets/v2-2926e767fdd523d4aeb9c37c650d41ca_720w.webp)

kube-harbor-1

- 在弹出的**修改密码**对话框中修改。

![img](assets/v2-24da76189a40b004adc33301c878f074_720w.webp)

kube-harbor-3

### **6.2. 创建管理员用户**

创建一个新的具有管理员权限的用户用于日常管理。

- **系统管理**->**用户管理**->**创建用户**，在弹出的**创建用户**对话框中按提示输入用户信息。

![img](assets/v2-bbc8d1ac52fc591fb136c7685f6aa0f7_720w.webp)

kube-harbor-4

![img](assets/v2-a4b012fd1326a96b1f3b71fd993771c9_720w.webp)

kube-harbor-5

- 用户创建完成后，自动返回**用户管理**页面，选择新创建的用户，点击**设置为管理员**。

![img](assets/v2-18e7ef5025a873274d8eb80eacb52eab_720w.webp)

kube-harbor-6

![img](assets/v2-7ac597cb05d1724b9a5c763f9cc1ebff_720w.webp)

kube-harbor-7

### **6.3. 创建 devops 用户**

为了实现 CI/CD 工作流，需要创建一个用于 Devops 的全局系统级机器人账户 **zdevops**。当然，也可以创建一个额外管理员用户用于自动化操作，建议使用机器人账户，权限控制的更精细。

如果多租户使用、项目比较多，建议创建项目级机器人账户。

默认的机器人账户名称前缀为 **robot，默认创建的全局机器人账户的名字为∗∗，默认创建的全局机器人账户的名字为∗∗�����zdevops**，这种带 **$** 号的形式在有些 CI/CD 系统里会有识别问题，因此最好修改一下。

- 修改机器人账户名称前缀，**系统管理**->**配置管理**->**系统设置**，修改**机器人账户名称前缀**为 **robot-**。

![img](assets/v2-64e86d04a252b2bc5a537dc0c9af8eb5_720w.webp)

kube-harbor-8

- 创建机器人账户，**系统管理**->**机器人账户**->**添加机器人账户**，在弹出的**创建系统级机器人账户**窗口，按图示填写账户信息，点击**添加**。

![img](assets/v2-0a169595fd5b6c41051f8f092b7db63b_720w.webp)

kube-harbor-9

![img](assets/v2-032fda02ea72e0d5687555b86b638032_720w.webp)

kube-harbor-10

- 账户创建成功，弹出**机器人账户令牌**界面，选择**导出到文件中**，并妥善保存。也可以选择复制令牌到自己的密码管理软件中。

![img](assets/v2-367278e76116057ed9bccf77eca03207_720w.webp)

kube-harbor-11

- 令牌导出后，自动返回**机器人账户**列表。

![img](assets/v2-bbbce9cc45f0bc7bc945e2ce747b8c5b_720w.webp)

kube-harbor-12

### **6.4. 创建项目**

创建一个新的项目存放自定义的镜像。

- **项目**->**新建项目**，在弹出的**新建项目**对话框中填写信息，点击**确定**，自动返回项目列表。

![img](assets/v2-f1c974e4ddfe6fb3a4fb8ded66d428a5_720w.webp)

kube-harbor-13

![img](assets/v2-c4263d8a46efb345520447ef5482c83c_720w.webp)

kube-harbor-14

![img](assets/v2-44786c06aa523fa150c252aec1e37d70_720w.webp)

kube-harbor-15

- 设置自动漏洞扫描，在项目列表页面选择新创建的项目，**配置管理**，勾选**自动扫描镜像**。

![img](assets/v2-e60a33dd85687e105f6d4c2acb33d224_720w.webp)

kube-harbor-16

## **7. KubeSphere 对接 Harbor**

### **7.1 配置 Harbor 域名解析**

- 测试域名连通性

```text
# 使用 busybox ping 测 
[root@ks-k8s-master-0 ~]# kubectl run busybox --image=busybox --command -- ping registry.zdevops.com.cn

# 创建pod error，log显示错误信息
[root@ks-k8s-master-0 ~]# kubectl logs busybox
ping: bad address 'registry.zdevops.com.cn'

# 删除测试 pod
[root@ks-k8s-master-0 ~]# kubectl delete pod busybox
```

- 编辑 coredns 的 configmap

```text
kubectl edit cm coredns -n kube-system
```

- 添加自定义的域名解析记录

```text
# fallthrough 必须添加，否则会造成内部域名全部无法解析
# hosts是CoreDNS的一个plugin，用于自定义hosts解析，fallthrough 表示如果hosts找不到，则进入下一个plugin继续。缺少这个指令，后面的plugins配置就无意义了。

hosts {
   192.168.9.89 registry.zdevops.com.cn
   fallthrough
}
```

- 编辑后完整的 configmap

```text
apiVersion: v1
data:
  Corefile: |
    .:53 {
        errors
        health {
           lameduck 5s
        }
        ready
        kubernetes cluster.local in-addr.arpa ip6.arpa {
           pods insecure
           fallthrough in-addr.arpa ip6.arpa
           ttl 30
        }
        hosts {
           192.168.9.89 registry.zdevops.com.cn
           fallthrough
        }
        prometheus :9153
        forward . /etc/resolv.conf {
           max_concurrent 1000
        }
        cache 30
        loop
        reload
        loadbalance
    }
kind: ConfigMap
metadata:
  creationTimestamp: "2022-04-09T14:33:53Z"
  name: coredns
  namespace: kube-system
  resourceVersion: "258"
  selfLink: /api/v1/namespaces/kube-system/configmaps/coredns
  uid: f0d9774c-a330-4de7-9f77-334dc82f710b
```

- 重启 coreDNS，使配置生效

```text
# 查看现有 coredns 状态
[root@ks-k8s-master-0 ~]# kubectl get deployment -n kube-system
NAME                          READY   UP-TO-DATE   AVAILABLE   AGE
calico-kube-controllers       1/1     1            1           69d
coredns                       2/2     2            2           69d
openebs-localpv-provisioner   1/1     1            1           69d
rbd-provisioner               1/1     1            1           22d

# 将 Deployment replicas 设置为0
[root@ks-k8s-master-0 ~]# kubectl scale deployment coredns -n kube-system --replicas=0
deployment.apps/coredns scaled

# 查看变更后 coredns 状态
[root@ks-k8s-master-0 ~]# kubectl get deployment -n kube-system
NAME                          READY   UP-TO-DATE   AVAILABLE   AGE
calico-kube-controllers       1/1     1            1           69d
coredns                       0/0     0            0           69d
openebs-localpv-provisioner   1/1     1            1           69d
rbd-provisioner               1/1     1            1           22d

# 将 Deployment replicas 设置为2
[root@ks-k8s-master-0 ~]# kubectl scale deployment coredns -n kube-system --replicas=2
deployment.apps/coredns scaled

# 查看变更后 coredns 状态
[root@ks-k8s-master-0 ~]# kubectl get deployment -n kube-system
NAME                          READY   UP-TO-DATE   AVAILABLE   AGE
calico-kube-controllers       1/1     1            1           69d
coredns                       2/2     2            2           69d
openebs-localpv-provisioner   1/1     1            1           69d
rbd-provisioner               1/1     1            1           22d
```

- 再次测试，结果依旧失败

原因：K8S 集群使用 kubesphere 的 kubekey 工具部署，默认部署了 coredns 和 nodelocaldns，`NodeLocal DNS Cache` 通过在集群节点上作为 DaemonSet 运行 dns 缓存代理来提高集群 DNS 性能。集群中 `kube-proxy` 运行在 IPVS 模式，在此模式下，`nodelocaldns Pods` 只会侦听 `<node-local-address>` 的地址，不会侦听 `coreDNS` 服务的 IP 地址。

```text
[root@ks-k8s-master-0 ~]# kubectl get cm kube-proxy -n kube-system -o yaml | grep mode
    mode: ipvs
```

`nodelocaldns` 通过添加 `iptables` 规则能够接收节点上所有发往 `169.254.20.10` 的 `dns` 查询请求，把针对集群内部域名查询请求路由到 `coredns`；把集群外部域名请求直接通过 `host` 网络发往集群外部 `dns` 服务器。之所以 `coredns` 的 `hosts` 插件不起作用 , 是因为集群 `pod` 使用的 `dns` 是 `169.254.20.10`, 也就是请求 `nodelocaldns`，而 `nodelocaldns` 配置的 `forward` 如下 :

```text
[root@ks-k8s-master-0 ~]# kubectl get cm  nodelocaldns -n kube-system -o yaml
apiVersion: v1
data:
  Corefile: |
    cluster.local:53 {
        errors
        cache {
            success 9984 30
            denial 9984 5
        }
        reload
        loop
        bind 169.254.25.10
        forward . 10.233.0.3 {
            force_tcp
        }
        prometheus :9253
        health 169.254.25.10:9254
    }
    in-addr.arpa:53 {
        errors
        cache 30
        reload
        loop
        bind 169.254.25.10
        forward . 10.233.0.3 {
            force_tcp
        }
        prometheus :9253
    }
    ip6.arpa:53 {
        errors
        cache 30
        reload
        loop
        bind 169.254.25.10
        forward . 10.233.0.3 {
            force_tcp
        }
        prometheus :9253
    }
    .:53 {
        errors
        cache 30
        reload
        loop
        bind 169.254.25.10
        forward . /etc/resolv.conf
        prometheus :9253
    }
kind: ConfigMap
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"v1","data":{"Corefile":"cluster.local:53 {\n    errors\n    cache {\n        success 9984 30\n        denial 9984 5\n    }\n    reload\n    loop\n    bind 169.254.25.10\n    forward . 10.233.0.3 {\n        force_tcp\n    }\n    prometheus :9253\n    health 169.254.25.10:9254\n}\nin-addr.arpa:53 {\n    errors\n    cache 30\n    reload\n    loop\n    bind 169.254.25.10\n    forward . 10.233.0.3 {\n        force_tcp\n    }\n    prometheus :9253\n}\nip6.arpa:53 {\n    errors\n    cache 30\n    reload\n    loop\n    bind 169.254.25.10\n    forward . 10.233.0.3 {\n        force_tcp\n    }\n    prometheus :9253\n}\n.:53 {\n    errors\n    cache 30\n    reload\n    loop\n    bind 169.254.25.10\n    forward . /etc/resolv.conf\n    prometheus :9253\n}\n"},"kind":"ConfigMap","metadata":{"annotations":{},"labels":{"addonmanager.kubernetes.io/mode":"EnsureExists"},"name":"nodelocaldns","namespace":"kube-system"}}
  creationTimestamp: "2022-04-09T14:33:58Z"
  labels:
    addonmanager.kubernetes.io/mode: EnsureExists
  name: nodelocaldns
  namespace: kube-system
  resourceVersion: "291"
  selfLink: /api/v1/namespaces/kube-system/configmaps/nodelocaldns
  uid: fe05a75c-b462-45bf-b265-8f9c393a20a1
```

重点在最后一段配置 **:53**, `10.233.0.3` 为 `coredns` 的 `service` `ip`, 所以集群内部域名会转发给 `coredns`, 而非集群内部域名会转发给 `/etc/resolv.conf`, 根本就不会转发给 `coredns`, 所以 `coredns` 里面配置的 `hosts` 自然不会生效

解决：需要将 `nodelocaldns Pods` 中 的 `/etc/resolv.conf` 也就是 `__PILLAR__UPSTREAM__SERVERS__` 变量自动配置的值，设置为 `coreDNS` 服务的 IP 地址。

- 修改 nodelocaldns 配置

```text
# 确认 coredns 的 IP 地址
[root@ks-k8s-master-0 ~]# kubectl get svc -n kube-system | grep coredns
coredns                       ClusterIP   10.233.0.3   <none>        53/UDP,53/TCP,9153/TCP         69d

# 编辑 nodelocaldns 的 configmap
root@ks-k8s-master-0 ~]# kubectl edit cm nodelocaldns -n kube-system
```

- 修改对比

```text
# 修改前
.:53 {
    errors
    cache 30
    reload
    loop
    bind 169.254.25.10
    forward . /etc/resolv.conf
    prometheus :9253
}

# 修改后
.:53 {
    errors
    cache 30
    reload
    loop
    bind 169.254.25.10
    forward . 10.233.0.3
    prometheus :9253
}
```

- 再次测试

```text
[root@ks-k8s-master-0 ~]# kubectl run busybox --image=busybox --command -- ping -c 4 registry.zdevops.com.cn

[root@ks-k8s-master-0 ~]# kubectl logs busybox
PING registry.zdevops.com.cn (192.168.9.89): 56 data bytes
64 bytes from 192.168.9.89: seq=0 ttl=63 time=0.467 ms
64 bytes from 192.168.9.89: seq=1 ttl=63 time=0.614 ms
64 bytes from 192.168.9.89: seq=2 ttl=63 time=0.435 ms
64 bytes from 192.168.9.89: seq=3 ttl=63 time=1.186 ms

--- registry.zdevops.com.cn ping statistics ---
4 packets transmitted, 4 packets received, 0% packet loss
round-trip min/avg/max = 0.435/0.675/1.186 ms

[root@ks-k8s-master-0 ~]# kubectl delete pod busybox
```

### **7.2. 创建 Secret**

- 用项目管理员账户登录 KubeSphere 控制台。

- **在具体项目 zdevos 下**->**配置**->**保密字典**->**创建**, 按图示填写相关信息。

- - **类型：** 镜像仓库信息
  - **仓库地址：** 镜像仓库的地址，自建或是 Docker Hub 仓库
  - **用户名：** 登录镜像仓库所需的用户名
  - **密码：** 登录镜像仓库所需用户的密码

![img](assets/v2-44fbe9c09b29254a44c86a50030cf737_720w.webp)

kube-harbor-17

![img](assets/v2-ffc763180a3f6e1179f8205bb8aeb061_720w.webp)

kube-harbor-18

![img](assets/v2-1ff7a260e43236d6907e2630b3e07715_720w.webp)

kube-harbor-19

- 输入正确的信息后，点击**验证**按钮，如图显示 **镜像仓库验证通过**，点击**创建**，返回保密字典列表。

![img](assets/v2-783340161feb8ef7a35fbb4c6a13a039_720w.webp)

kube-harbor-20

## **8. 验证测试**

### **8.1. 准备 Nginx 测试镜像**

- 下载镜像

```text
docker pull nginx
```

- 重新打 tag

```text
docker tag nginx:latest registry.zdevops.com.cn/zdevops/nginx:latest
```

- 登录到 Harbor 仓库

```text
[root@zdevops-master data]# docker login registry.zdevops.com.cn
Username: z
Password: 
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
```

- 推送到 Harbor 仓库

```text
[root@zdevops-master data]# docker push registry.zdevops.com.cn/zdevops/nginx:latest
The push refers to repository [registry.zdevops.com.cn/zdevops/nginx]
d874fd2bc83b: Pushed 
32ce5f6a5106: Pushed 
f1db227348d0: Pushed 
b8d6e692a25e: Pushed 
e379e8aedd4d: Pushed 
2edcec3590a4: Pushed 
latest: digest: sha256:ee89b00528ff4f02f2405e4ee221743ebc3f8e8dd0bfd5c4c20a2fa2aaa7ede3 size: 1570
```

### **8.2. 部署 Nginx**

- 用项目管理员账户登录 KubeSphere 控制台
- **在具体项目 zdevos 下**->**应用负载**->**工作负载**->**部署**->**创建**, **创建部署**页面输入**名称**。

![img](assets/v2-33cea686bd5b5cb1a19ea1e515a76397_720w.webp)

kube-harbor-21

- 点击**添加容器**，容器设置里选择自定义容器仓库。

![img](assets/v2-521ff89d10f8d11e7973aa86356f137f_720w.webp)

kube-harbor-22

![img](assets/v2-84de6beacfb0b736a6aef5934c8e822b_720w.webp)

kube-harbor-23

- 镜像选择：输入上传的 nginx 私有镜像的仓库地址

![img](assets/v2-d4048ea43c50248b5f4d1034973322ac_720w.webp)

kube-harbor-24

- 接下来按图示下一步即可。

![img](assets/v2-907e59946071bd76ec3bf0c994063914_720w.webp)

kube-harbor-25

![img](assets/v2-54ad4e970f87b53d823c3a10a00f44b0_720w.webp)

kube-harbor-26

![img](assets/v2-5369824a47e12322b4b03245fb1656e8_720w.webp)

kube-harbor-27

![img](assets/v2-adeb24bc85b2ec9d34b125727aaa165e_720w.webp)

kube-harbor-28

- 查看创建的 nginx 的 YAML 配置文件

![img](assets/v2-4e4edf730483f2bdc7b467794113b309_720w.webp)

kube-harbor-29

![img](assets/v2-d02a05b02bbe8389835095a9490d540c_720w.webp)

kube-harbor-30

## **9. 总结**

本文详细演示了以下相关内容：

- Harbor 服务器的安装部署和安装后的初始化过程
- K8S 配置自定义域名解析
- KubeSphere 图形化创建镜像仓库类型的 Secret
- KubeSphere 利用私有镜像仓库图形化创建工作负载

> **参考文档**
>
> **Get 文档**

- Github [https://github.com/devops/z-notes](https://github.com/devops/z-notes)
- Gitee [https://gitee.com/zdevops/z-notes](https://gitee.com/zdevops/z-notes)

> **Get 代码**

- Github [https://github.com/devops/ansible-zdevops](https://github.com/devops/ansible-zdevops)
- Gitee [https://gitee.com/zdevops/ansible-zdevops](https://gitee.com/zdevops/ansible-zdevops)

> **B 站**

- **[老 Z 手记](https://space.bilibili.com/1039301316)**[https://space.bilibili.com/1039301316](https://space.bilibili.com/1039301316)

> **版权声明**

- 所有内容均属于原创，整理不易，感谢收藏，转载请标明出处。

> **About Me**

- 昵称：老 Z
- 坐标：山东济南
- 职业：运维架构师 / 高级运维工程师 =**运维**
- 微信：zdevops
- 关注的领域：云计算 / 云原生技术运维，自动化运维
- 技能标签：OpenStack、Ansible、K8S、Python、Go、CNCF