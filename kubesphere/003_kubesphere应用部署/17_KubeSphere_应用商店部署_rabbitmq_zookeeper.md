## KubeShpere应用商店部署



### 部署应用商店中已经存在的应用

这里以应用商店中已经存在的Rabbitmq为例：

在项目管理首页，点击左上角  `应用商店` 进入应用商店页，在应用列表中找到 RabbitMq，或者搜索rabbitmq

点击 rabbitmq，进入应用详情页。应用详情页有 `应用信息` 和 `应用详情` ，

点击右上角 `安装`，



基本信息

```bash
名称
rabbitmq-s

版本
0.3.2 [3.8.1] 最新版本

描述
project-test rabbitmq 服务
```

下面的位置：

点击企业空间，选择要部署的 `企业空间` `集群` 和 `项目`



选择后再点击 `下一步`，进入 `应用设置` 页：

Data Persistence： 开启 enable选项，选择持久卷大小 5G， 设置root的账号密码，这里设置为 admin / admin

```bash
Root Username
admin
Root Password
admin
```



点击 `安装` 即可。安装 完成后会自动生成 pvc 和 service 。通过应用商店创建的应用会展示在 `应用负载-应用` 中。在 `应用负载-工作负载-有状态副本集` 中可以看到新创建的pod，`应用负载-服务`  中可以看到新创建的服务，如果需要外部访问，可以点击服务列表右边的 ... 点击`编辑外部访问`，在 `外部访问` 页面选择`访问模式`选择 `NodePort` 点击确定。此时会创建2个对外的端口，分别映射到容器的 5672和 15672 端口，通过浏览器访问15672对应的外部端口：

```bash
http://192.168.0.112:3xxxx
```

账号密码为 `admin / admin`

外部访问：

创建一个服务，输入基本信息，选择内部访问模式为`虚拟IP地址` 端口为 HTTP-15672，勾选`外部访问`，访问模式选择`NodePort`



### 部署应用商店中不存在的应用

想要部署在应用商店中不存在的应用需要使用应用仓库，需要添加k8s的应用仓库，这里需要使用到 `helm` ， https://helm.sh/  ，heml对于k8s的作用就是类似于 dockerhub对于docker，helm是一个k8s部署配置文件的仓库，里面有很多k8s应用的部署配置，在helm中，k8s的配置被称为 charts，在helm首页点击 charts，进入 https://artifacthub.io/ ，在输入框中输入要部署的应用，如 mysql， 在搜索结果中可以看到各个厂商提供的配置文件，在helm中比较大的厂商是bitnami，点击由bitnami提供的mysql配置文件：https://artifacthub.io/packages/helm/bitnami/mysql ，点击右侧的 install ，在弹出的窗口中会有安装需要的指令：

```bash
# Add repository
helm repo add bitnami https://charts.bitnami.com/bitnami
# Install chart
helm install my-mysql bitnami/mysql --version 9.10.4
```

这里可以看到 bitmani的仓库地址： https://charts.bitnami.com/bitnami



登录KubeSphere， 切换具有企业空间权限的账号，如 boss-lei ，登录之后点击进入企业空间，点击 `应用管理-应用仓库`在应用仓库列表中点击 `添加`，在弹出的窗口中输入：

```bash
名称：bitnami
URL：选择https，输入 charts.bitnami.com/bitnami ，点击验证
描述：bitnami 官方仓库
同步间隔：24 小时
```



点击确认，此时可以在 `应用管理-应用仓库`中看到新设置的应仓库。

切换会dev-zhao 账号，进入项目空间，在 `应用负载-应用` 切换到`基于模板的应用`，点击`创建`，点击`从应用模板`，选择应用仓库，选择 `bitnami` ，此时下面会列出bitmani仓库中的配置列表。在搜索框中输入 zookeeper，显示出zookeeper的配置项，点击zookeeper，进入应用配置详情页。点击右侧的 `安装` 。

输入基本信息：

基本信息

```bash
基本信息
名称：zookeeper-d2kls6
版本：11.4.3 [3.8.1]  
描述：zookeeper服务
```

点击`下一步` 进入应用设置页，这里跳过，点击`安装`即可。接下来系统会自动安装zookeeper，安装完成后即可在 `应用负载-应用` 中看到新安装的zookeeper应用。在`应用负载-服务`中可以看到自动创建的2个服务，其中一个具有虚拟IP，可以通过编辑外部访问，选择NodePort的方式暴露外部访问的端口。开启之后点击服务列表中的服务项，进入服务详情，可以看到端口的映射关系：

```bash
TCP 2181：32368
TCP 2888：31370
TCP 3888：31398
```



这样 helm的仓库  https://artifacthub.io/  中存在的k8s配置文件，就可以通过KubeSphere进行部署。

 bitmani的仓库地址： https://charts.bitnami.com/bitnami

































