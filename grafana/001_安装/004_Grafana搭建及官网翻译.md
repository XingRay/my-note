## Grafana搭建及官网翻译（一）

## 一、Grafana的搭建

Grafana 是一个开源的时序性统计和监控平台，支持例如 elasticsearch、graphite、influxdb 等众多的数据源，并以功能强大的界面编辑器著称。

官网：https://grafana.com/

### 1.1 RPM包的安装方式 

直接yum包的方式：

\# cd /opt/

\# yum install https://dl.grafana.com/oss/release/grafana-5.4.2-1.x86_64.rpm

下载下来rpm的方式：

\# wget https://dl.grafana.com/oss/release/grafana-5.4.2-1.x86_64.rpm

\# yum install initscripts fontconfig

\# rpm -Uvh grafana-5.4.2-1.x86_64.rpm

### 1.2 Yun安装的方式 

\#vim /etc/yum.repos.d/grafana.repo

Bash

```bash
[grafana]name=grafana
baseurl=https://packages.grafana.com/oss/rpm
repo_gpgcheck=1
enabled=1
gpgcheck=1
gpgkey=https://packages.grafana.com/gpg.key
sslverify=1
sslcacert=/etc/pki/tls/certs/ca-bundle.crt
```

\#yum install grafana

\#如果您想要测试版，可以使用单独的存储库，如下面的配置：

\#vim /etc/yum.repos.d/grafana.repo

Bash

```bash
[grafana]name=grafana
baseurl=https://packages.grafana.com/oss/rpm-beta
repo_gpgcheck=1
enabled=1
gpgcheck=1
gpgkey=https://packages.grafana.com/gpg.key
sslverify=1
sslcacert=/etc/pki/tls/certs/ca-bundle.crt
```

安装完之后相关文件的位置：

将二进制文件安装到：/usr/sbin/grafana-server

将init.d脚本复制到：/etc/init.d/grafana-server

将默认文件（环境变量）安装到：/etc/sysconfig/grafana-server

将配置文件复制到：/etc/grafana/grafana.ini

安装systemd服务：（如果systemd可用）名称为grafana-server.service

默认配置使用：/var/log/grafana/grafana.log中的日志文件

默认配置在：/var/lib/grafana/grafana.db中指定sqlite3数据库

启动服务：

\#service grafana-server start

\#这将以grafana用户身份启动grafana服务器进程，该用户是在程序包安装期间创建的。 默认HTTP端口为3000，默认用户和组为admin。默认登录名和密码admin/admin

\#如果是systemd命令启动：

\# systemctl daemon-reload

\# systemctl start grafana-server

\# systemctl status grafana-server

添加开机启动：

\#如果是Centos6的话就是下面的命令：

\#/sbin/chkconfig --add grafana-server

\#如果是Centos7的话就是下面的命令：

\#systemctl enable grafana-server.service

Environment file（环境文件）：

systemd服务文件和init.d脚本都使用位于/etc/sysconfig/grafana-server的文件来获取启动后端时使用的环境变量。 在这里，您可以覆盖日志目录，数据目录和其他变量。

Server side image rendering（服务器端图像渲染）：

服务器端图像（png）呈现是一项可选功能，但在共享可视化时非常有用，例如在警报通知中。如果图像缺失文本，请确保已安装字体包。

\#yum install fontconfig freetype* urw-fonts -y

### 1.3 源码安装方式 

参考文档：https://github.com/grafana/grafana

yum安装依赖：

\# yum install go git -y 

安装Node:

\# wget https://nodejs.org/dist/v8.5.0/node-v8.5.0-linux-x64.tar.gz

\# tar zxf node-v8.5.0-linux-x64.tar.gz 

\# mv node-v8.5.0-linux-x64 node-v8.5.0

\# ln -s /opt/node-v8.5.0 /opt/node

\# vim /etc/profile

Bash

```bash
export NODE_HOME="/opt/node"
export PATH=$PATH:$NODE_HOME/bin
```

\# source /etc/profile

\# node -v

Bash

```bash
v8.5.0
```

\# npm -v

Bash

```bash
5.3.0
```

git 项目源码：

\# vim /etc/profile

Bash

```bash
export GOPATH="/opt/"
```

\# source /etc/profile

\# go get github.com/grafana/grafana

\#当然如果半天get不下来可以用# git clone https://github.com/grafana/grafana.git的方式

构建后端：

\#cd $GOPATH/src/github.com/grafana/grafana

\#go run build.go setup

\#go run build.go build 

构建前端：

\#yum install bzip2 -y

\#npm install -g yarn

\#yarn install --pure-lockfile

\#npm run build

运行服务：

./bin/grafana-server

博文来自：[www.51niux.com](http://www.51niux.com/)

### 1.4 tar包的安装方式 

\#yum install fontconfig freetype* urw-fonts -y

\#cd /application/

\# wget https://dl.grafana.com/oss/release/grafana-5.4.3.linux-amd64.tar.gz

\# tar zxf grafana-5.4.3.linux-amd64.tar.gz 

\# cp /application/grafana-5.4.3/conf/defaults.ini /application/grafana-5.4.3/conf/grafana.ini

\#cd /application/grafana-5.4.3

\# ln -s /application/grafana-5.4.3 /application/grafana

\#./bin/grafana-server -config ./conf/grafana.ini & #制定配置文件启动

### 1.5 配置Nginx做反向代理

\#vim /application/nginx/conf/nginx.conf

Bash

```bash
        location / {
            #root   html;
            #index  index.html index.htm;
            proxy_pass http://127.0.0.1:3000;
        }
```

\# /application/nginx/sbin/nginx

### 1.6 登录验证 

\#直接在浏览器输入机器的IP便可：

![image.png](assets/004/201902131550049586715000.png)

\#账号是admin/admin

![image.png](assets/004/201902131550049630154182.png)

\#会让修改密码。

## 

## 二、开始入门（翻译官网）

### 2.1 开始入门 

首次登录：

​    要运行Grafana，请打开浏览器并转到http://localhost:3000/。 如果您尚未配置其他端口，则3000是Grafana侦听的默认http端口。

​    在那里，将看到登录页面。 默认用户名为admin，默认密码为admin。 当第一次登录时，系统会要求您更改密码。 建议您遵循Grafana的最佳做法并更改默认管理员密码。 可以稍后转到用户首选项并更改用户名。

如何添加数据源：

​    在创建第一个仪表板之前，需要添加数据源。

​    首先将光标移动到侧面菜单上的cog，它将显示配置菜单。 如果侧面菜单不可见，请单击左上角的Grafana图标。 配置菜单上的第一项是数据源，单击它，将进入数据源页面，可以在其中添加和编辑数据源。 也可以直接点击齿轮。

​    ![image.png](assets/004/201902271551272179359291.png)

​    单击添加数据源，将进入新数据源的设置页面。

![image.png](assets/004/201902271551272211335691.png)

首先，为数据源指定一个名称，然后选择要创建的数据源类型，有关详细信息以及如何配置数据源，请参阅支持的数据源[http://docs.grafana.org/features/datasources/#supported-data-sources/](http://docs.grafana.org/features/datasources/#supported-data-sources/。)

![image.png](assets/004/201902271551272424389621.png)

配置完数据源后，即可进行保存和测试。

顶部标题：

让我们从创建一个新的仪表板开始。 可以在仪表板选取器的右侧找到新的仪表板链接。 现在有一个空白的仪表板。

![image.png](assets/004/201902271551272544564196.png)

上图显示了仪表板的顶部标题。

\1. 侧面菜单栏切换：这会切换侧面菜单，使你可以专注于仪表板中显示的数据。侧边菜单提供对与仪表板无关的功能（如用户，组织和数据源）的访问。

\2. 仪表板下拉菜单：此下拉列表显示当前正在查看的仪表板，并允许轻松切换到新的仪表板。在此处，还可以创建新的仪表板或文件夹，导入现有仪表板以及管理仪表板播放列表。

\3. 添加面板：将新面板添加到当前仪表板。

\4. Star Dashboard：Star（或unstar）当前仪表板。默认情况下，加星标的仪表板将显示在自己的主页控制台上，是标记您感兴趣的仪表板的便捷方式。

\5. 共享仪表板：通过创建链接或创建其静态快照来共享当前仪表板。确保在共享之前保存仪表板。

\6. 保存仪表板：当前仪表板将与当前仪表板名称一起保存。

\7. 设置：管理仪表板设置和功能，如模板和注释。

仪表板，面板，Grafana的构件......：

  仪表板是Grafana的核心所在。 仪表板由排列在网格上的各个面板组成。 Grafana配有各种面板。 Grafana可以轻松构建正确的查询，并自定义显示属性，以便您可以根据需要创建完美的仪表板。 每个Panel都可以与来自任何已配置的Grafana数据源（目前为Graphite，Prometheus，Elasticsearch，InfluxDB，OpenTSDB，MySQL，PostgreSQL，Microsoft SQL Server和AWS Cloudwatch）的数据进行交互。

![image.png](assets/004/201902271551272752136982.png)

\1. 缩小时间范围

\2. 时间选择器下拉列表。 您可以在此处访问相对时间范围选项，自动刷新选项以及设置自定义绝对时间范围。

\3. 手动刷新按钮。 将导致所有面板刷新（获取新数据）。

\4. 仪表板面板。 可以通过单击面板标题来编辑面板。

\5. 图形图例。 可以直接从图例中更改系列颜色，y轴和系列可见性。

Adding & Editing Graphs and Panels（添加和编辑图形和面板）：

![image.png](assets/004/201902271551272836335234.png)

\1. 可以通过单击顶部菜单上的“添加”面板图标来添加面板。

\2. 要编辑图形，请单击图形标题以打开面板菜单，然后单击编辑。

\3. 这会将你带到“Metrics”选项卡。 在此选项卡中，应该看到默认数据源的编辑器。

单击“Metrics”选项卡时，将显示特定于Panel Data Source的查询编辑器。 使用查询编辑器构建查询，Grafana将实时显示它们。    

Drag-and-Drop panels（拖放面板）：

只需单击并按住Panel标题即可拖放面板，然后将其拖动到新位置。 还可以通过单击（ - ）和（+）图标轻松调整面板大小。

Tips and shortcuts（提示和快捷方式）：

单击图表标题，然后在下拉菜单中快速更改范围或复制面板。

单击菜单中的“Save”图标以使用新名称保存仪表板

单击菜单中的“Save”图标，然后单击“advanced”以将仪表板导出到json文件，或将其设置为默认仪表板。

单击图例中的彩色图标以选择系列颜色

单击图例中的系列名称以隐藏系列

Ctrl / Shift / Meta +单击图例名称以隐藏其他系列

Grafana loves the keyboard：

Ctrl + S保存当前仪表板

Ctrl + F打开仪表板查找器/搜索

Ctrl + H隐藏所有控件（适用于电视显示）

在全屏或编辑模式下，按Escape退出图表

### 2.2 基本概念

Data Source(数据源)：

​    Grafana为你的时间序列数据（数据源）支持许多不同的存储后端。 每个数据源都有一个特定的查询编辑器，该编辑器针对特定数据源公开的特性和功能进行了自定义。官方支持以下数据源：Graphite，InfluxDB，OpenTSDB，Prometheus，Elasticsearch，CloudWatch。每个数据源的查询语言和功能显然非常不同。 可以将来自多个数据源的数据组合到单个仪表板上，但每个Panel都绑定到属于特定组织的特定数据源。

Organization（组织）：

​    Grafana支持多个组织，以支持各种部署模型，包括使用单个Grafana实例为多个可能不受信任的组织提供服务。在许多情况下，Grafana将部署在一个组织中。每个组织可以拥有一个或多个数据源。所有仪表板都归特定组织所有。

​    注意：请务必记住，大多数度量标准数据库不提供任何类型的每用户系列身份验证。 因此，在Grafana中，特定组织中的所有用户都可以使用数据源和仪表板。

User（用户）：

​    用户是Grafana中的指定帐户。 用户可以属于一个或多个组织，可以通过角色为其分配不同级别的权限。Grafana支持各种内部和外部方式，供用户进行身份验证。 这些包括来自其自己的集成数据库，来自外部SQL服务器或来自外部LDAP服务器。

Row：

​    A Row是仪表板中的逻辑分隔符，用于将Panel组合在一起。行总是12“units”宽。 这些单位会根据浏览器的水平分辨率自动缩放。 可以通过设置自己的宽度来控制行内Panel的相对宽度。利用单位抽象，以便Grafana在所有小屏幕和大屏幕上看起来都很棒。

   注意：使用MaxDataPoints功能，无论你的分辨率或时间范围如何，Grafana都可以向你显示完美的数据点数量。

   根据所选的模板变量，利用“Repeating Rows functionality ”功能动态创建或删除整个行（可以使用面板填充）。单击行标题可以折叠行。 如果在折叠行的情况下保存仪表板，它将保存在该状态，并且在该行展开之前不会预加载这些图形。

Panel（面板）：

   该小组是Grafana的基本可视化构建块。每个Panel提供一个查询编辑器（取决于面板中选择的数据源），允许使用查询编辑器提取要在Panel上显示的完美可视化每个面板都有各种各样的样式和格式选项，可以让你创建完美的图片。可以在仪表板上拖放面板并重新排列面板。它们也可以调整大小。

   目前有四种Panel类型：Graph，Singlestat，Dashlist，Table和Text。

   像“Graph”面板这样的面板允许根据需要绘制尽可能多的指标和系列。

   像Singlestat这样的其他面板需要将单个查询减少为单个数字。 

   Dashlist和Text是不连接到任何数据源的特殊面板。通过在面板配置中使用仪表板模板变量字符串（包括通过查询编辑器配置的数据源查询），可以使面板更加动态。利用重复面板功能，根据所选的模板变量动态创建或删除面板。 Panels上的时间范围通常是仪表板时间选择器中设置的时间范围，但可以通过使用Panel特定时间覆盖来覆盖此时间范围。可以通过各种方式轻松共享面板（或整个仪表板）。可以向登录Grafana的人发送链接。可以使用快照功能将当前正在查看的所有数据编码为静态和交互式JSON文档;它比通过电子邮件截图更好！

Query Editor（查询编辑器）：

​    查询编辑器公开数据源的功能，并允许您查询它包含的指标。使用查询编辑器在时间序列数据库中构建一个或多个查询（对于一个或多个系列）。 该面板将立即更新，允许实时有效地探索您的数据，并为该特定面板构建一个完美的查询。可以在查询编辑器中使用查询编辑器中的模板变量。 这提供了一种基于仪表板上选择的模板变量动态探索数据的强大方法。Grafana允许通过查询编辑器中的行来引用查询。 如果向图表添加第二个查询，则只需键入#A即可引用第一个查询。 这提供了一种简单方便的方法来构建复合查询。

Dashboard（仪表盘）：

​    仪表板是它们汇集在一起的地方。 仪表板可以被认为是一组一个或多个面板组织并排列成一个或多个行。仪表板的时间段可以通过仪表板右上角的Dashboard time picker（仪表板时间选择器）进行控制。仪表板可以利用 Templating（模板化）使其更具动态性和交互性。仪表板可以利用Annotations在Panel中显示事件数据。 这有助于将Panel中的时间序列数据与其他事件相关联。可以通过各种方式轻松Shared（共享）仪表板（或特定面板）。 可以向登录Grafana的人发送链接。 可以使用 Snapshot（快照）功能将当前正在查看的所有数据编码为静态和交互式JSON文档; 它比通过电子邮件截图更好！

​    可以标记仪表板，仪表板选择器提供对特定组织中所有仪表板的快速，可搜索的访问。

## 三、跟着官网学习配置文件详解 

官网文档：http://docs.grafana.org/installation/configuration/

​    Grafana后端具有许多配置选项，可以在.ini配置文件中指定或使用环境变量指定。注意。 需要重新启动Grafana才能使任何配置更改生效。

分号（; char）是注释.ini文件中的行的标准方法。一个常见问题是忘记取消注释custom.ini（或grafana.ini）文件中的一行，这会导致忽略配置选项。

使用环境变量：

​    可以使用以下语法使用环境变量覆盖配置文件（下面列出）中的所有选项：GF_<SectionName>_<KeyName>部分名称是括号内的文本。 一切都应该是大写的。 应该用_代替。 例如，给定以下配置设置：

Bash

```bash
# default section
instance_name = ${HOSTNAME}
[security]
admin_user = admin
[auth.google]
client_secret = 0ldS3cretKey
```

然后你可以使用以下方法覆盖它：

Bash

```bash
export GF_DEFAULT_INSTANCE_NAME=my-instance
export GF_SECURITY_ADMIN_USER=true
export GF_AUTH_GOOGLE_CLIENT_SECRET=newS3cretKey
```

例子：

   设置grafana-server实例的名称。 用于记录和内部指标以及群集信息。 默认为：$ {HOSTNAME}，将替换为环境变量HOSTNAME，如果该项为空或不存在，Grafana将尝试使用系统调用来获取计算机名称。

\# vim conf/defaults.ini

Bash

```bash
##################### Grafana Configuration Defaults #####################
app_mode = production   #production或者是development
instance_name = ${HOSTNAME}  #实例名称，如果HOSTNAME var为空，则默认为HOSTNAME环境变量值或主机名

#################################### Paths ###############################
[paths]
data = data  #grafana可以存储临时文件，会话和sqlite3 db的路径（如果使用的话）
temp_data_lifetime = 24h  #应保留数据目录中的临时映像多长时间。 默认为：24h。 支持的修饰符：h（小时），m（分钟），例如：168h，30m，10h30m。 使用0永远不会清理临时文件。
logs = data/log  #Grafana将存储日志的路径。 此路径通常通过init.d脚本中的命令行或systemd服务文件指定。 它可以在配置文件或默认环境变量文件中重写。
plugins = data/plugins   #grafana将自动扫描并查找插件的目录
provisioning = conf/provisioning  #包含grafana将在启动时应用的配置配置文件的文件夹。当json文件发生更改时，将重新加载仪表板

#################################### Server ##############################
[server]
protocol = http  #协议（http，https，套接字）
http_addr =   #要绑定的IP地址。 如果为空将绑定到所有
http_port = 3000  #要绑定的端口，默认为3000.要使用端口80，需要授予Grafana二进制权限
domain = localhost  #面向公众的域名，用于从浏览器访问grafana
enforce_domain = false   #如果主机标头与域不匹配，则重定向到正确的域。防止DNS重新绑定攻击。 默认值为false。
root_url = %(protocol)s://%(domain)s:%(http_port)s/  #这是用于从Web浏览器访问Grafana的完整URL。 如果您使用Google或GitHub OAuth身份验证（以使回调网址正确），这一点非常重要。
注意如果Grafana前面有一个反向代理通过子路径公开它，则此设置也很重要。 在这种情况下，将子路径添加到此URL设置的末尾。
router_logging = false  #记录Web请求,设置为true以使Grafana记录所有HTTP请求（而不仅仅是错误）。 这些被记录为信息级别事件到grafana日志。
static_root_path = public  #前端文件（HTML，JS和CSS文件）所在目录的路径。 默认为public，这就是为什么需要在将工作目录设置为安装路径的情况下执行Grafana二进制文件的原因。
cert_file =   #证书文件的路径（如果协议设置为https）。
cert_key =    #证书文件的路径（如果协议设置为https）。
socket = /tmp/grafana.sock  ##protocol = socket时应创建套接字的路径。 请确保Grafana具有适当的权限。

#################################### Database ############################
[database]
#Grafana需要一个数据库来存储用户和仪表板（以及其他东西）。 默认情况下，它配置为使用sqlite3，这是一个嵌入式数据库（包含在主Grafana二进制文件中）。
type = sqlite3   #可以选择mysql，postgres还是sqlite3。
host = 127.0.0.1:3306  #仅适用于MySQL或Postgres。 包括IP或主机名和端口，或者在unix套接字的情况下包含它的路径。例如，对于运行在与Grafana相同主机上的MySQL：host = 127.0.0.1:3306或使用unix套接字：host = /var/run/mysqld/mysqld.sock
name = grafana   #Grafana数据库的名称。 将它设置为grafana或其他名称。
user = root   #数据库用户（不适用于sqlite3）。
password =    #数据库用户的密码（不适用于sqlite3）。如果密码包含＃或;你必须用三重引号包装它。例如："""#password;"""
url =         #使用以下URL或其他字段配置数据库示例：mysql://user:secret@host:port/database
max_idle_conn = 2   #空闲连接池中的最大连接数。
max_open_conn =     #数据库的最大打开连接数。
conn_max_lifetime = 14400   #设置可以重用连接的最长时间。 默认值为14400（表示14400秒或4小时)。对于MySQL，此设置应短于wait_timeout变量。
log_queries =   #设置为true以记录sql调用和执行时间。
ssl_mode = disable  #对于Postgres，请使用disable，require或verify-full。 对于MySQL，请使用true，false或skip-verify。
ca_cert_path =      #要使用的CA证书的路径。在许多Linux系统上，可以在/etc/ssl/certs中找到证书。
client_key_path =    #客户端密钥的路径。仅当服务器需要客户端身份验证
client_cert_path =   #客户端证书的路径。 仅当服务器需要客户端身份验证
server_cert_name =    #mysql或postgres服务器使用的证书的公用名字段。 如果ssl_mode设置为skip-verify，则不需要。
path = grafana.db     #仅适用于sqlite3数据库。 将存储数据库的文件路径。

#################################### Session #############################
[session]
provider = file  # "memory", "file", "redis", "mysql", "postgres", "memcache",默认为file
provider_config = sessions   #因为上面是file，这里就是data/sessions，如果是memcache就是127.0.0.1:11211
cookie_name = grafana_sess   #会话cookie名称
cookie_secure = false        #如果仅使用https中的会话，则默认为false
session_life_time = 86400    #会话持续多长时间。 默认为86400（24小时）。
gc_interval_time = 86400     #会话生存时间，默认为86400
conn_max_lifetime = 14400    #连接最大生命周期默认值为14400（表示14400秒或4小时）

#################################### Data proxy ###########################
[dataproxy]
logging = false      #这启用了数据代理日志记录，默认为false

#################################### Analytics ###########################
[analytics]
reporting_enabled = true   #启用后，Grafana将向stats.grafana.org发送匿名使用情况统计信息。 没有跟踪IP地址，只有简单的计数器来跟踪正在运行的实例，版本，仪表板和错误计数。这对我们非常有帮助，所以请启用此功能。 计数器每24小时发送一次。 默认值为true。
check_for_updates = true   #设置为false以禁用对新版本（grafana本身和插件）的所有检查到https://grafana.com，在某些UI视图中使用check来通知grafana或插件更新存在此选项不会导致任何自动更新， 也不会向https://grafana.com发送任何GET请求信息以获取最新版本
google_analytics_ua_id =   #如果您想通过Google Analytics分析跟踪Grafana的使用情况，请在此处指定您的Universal Analytics ID。 默认情况下，此功能已禁用。
google_tag_manager_id =    #Google跟踪代码管理器ID，仅在此处指定ID时启用

#################################### Security ############################
[security]
admin_user = admin           #默认Grafana管理员用户的名称（具有完全权限）。默认为admin。
admin_password = admin       #默认Grafana管理员的密码。首次设置一次。默认为admin。
secret_key = SW2YcwTIb9zpOOhoPsMm   #用于签署一些数据源设置，如秘密和密码。 如果不更新数据源设置以重新编码，则无法更改。
login_remember_days = 7      #让我登录/记住我cookie的天数。
cookie_username = grafana_user      
cookie_remember_name = grafana_remember   
disable_gravatar = false     #设置为true可禁用Gravatar用于用户个人资料图像。默认值为false。
data_source_proxy_whitelist =    #数据源代理白名单（ip_or_domain：以空格分隔的端口）
disable_brute_force_login_protection = false   #禁用防止暴力登录尝试，默认是false，也就是不关闭暴力破解保护

#################################### Snapshots ###########################
[snapshots]
external_enabled = true   #设置为false以禁用外部快照发布端点（默认为true）
external_snapshot_url =     #将根URL设置为要发布外部快照的Grafana实例（默认为https://snapshots-origin.raintank.io） 
external_snapshot_name = Publish to snapshot.raintank.io       #设置外部快照按钮的名称。 默认为发布到snapshot.raintank.io
snapshot_remove_expired = true     ##已启用以自动删除过期的快照

#################################### Dashboards ##################
[dashboards]
versions_to_keep = 20    #要保留的仪表板版本（按仪表板）的数量。 默认值：20，最小值：1

#################################### Users ###############################
[users]
allow_sign_up = false     #设置为false以禁止用户注册/创建用户帐户。默认为false。管理员用户仍然可以从Grafana管理页面创建用户
allow_org_create = false    #设置为false以禁止用户创建新组织。 默认为false。
auto_assign_org = true     #设置为true以自动将新用户添加到主组织（标识1）。设置为false时，新用户将自动为该新用户创建新组织。
auto_assign_org_id = 1     #设置此值可自动将新用户添加到提供的组织。这需要将auto_assign_org设置为true。请确保该组织已存在。
auto_assign_org_role = Viewer    #将为主组织分配新用户的角色（如果上述设置设置为true）。默认为Viewer，其他有效选项为Admin和Editor。 例如：auto_assign_org_role = Viewer
verify_email_enabled = false     #注册完成后是否需要电子邮件验证
login_hint = email or username    #登录页面上用户字段的背景文本
default_theme = dark    #默认UI主题（"dark" or "light"）
#外部用户管理
external_manage_link_url =
external_manage_link_name =
external_manage_info =

viewers_can_edit = false     #观看者可以在浏览器中编辑/检查仪表板设置。 但不保存仪表板。 默认为false。
[auth]
disable_login_form = false     #设置为true以禁用（隐藏）登录表单，如果您使用OAuth，则非常有用
disable_signout_menu = false     #设置为true可禁用侧边菜单中的注销链接。 如果你使用auth.proxy很有用
signout_redirect_url =      #注销后用户重定向到的URL
oauth_auto_login = false    #设置为true以尝试自动登录OAuth，跳过登录屏幕。如果配置了多个OAuth提供程序，则忽略此设置。
[auth.anonymous]
enabled = false   #是否启用匿名访问
org_name = Main Org.     #指定应该用于未经身份验证的用户的组织名称
org_role = Viewer     #指定未经身份验证的用户的角色
[auth.github]
enabled = false
allow_sign_up = true  #您可以通过将allow_sign_up选项设置为true来允许用户通过GitHub身份验证进行注册。 当此选项设置为true时，任何通过GitHub身份验证成功进行身份验证的用户都将自动注册。
client_id = some_id   #YOUR_GITHUB_APP_CLIENT_ID
client_secret = some_secret   #YOUR_GITHUB_APP_CLIENT_SECRET
scopes = user:email,read:org
auth_url = https://github.com/login/oauth/authorize
token_url = https://github.com/login/oauth/access_token
api_url = https://api.github.com/user
team_ids =
allowed_organizations =

#################################### GitLab Auth #########################
[auth.gitlab]
enabled = false
allow_sign_up = true
client_id = some_id
client_secret = some_secret
scopes = api
auth_url = https://gitlab.com/oauth/authorize
token_url = https://gitlab.com/oauth/token
api_url = https://gitlab.com/api/v4
allowed_groups =

#################################### Google Auth #########################
[auth.google]
enabled = false
allow_sign_up = true
client_id = some_client_id
client_secret = some_client_secret
scopes = https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email
auth_url = https://accounts.google.com/o/oauth2/auth
token_url = https://accounts.google.com/o/oauth2/token
api_url = https://www.googleapis.com/oauth2/v1/userinfo
allowed_domains =
hosted_domain =

#################################### Grafana.com Auth ####################
# legacy key names (so they work in env variables)
[auth.grafananet]
enabled = false
allow_sign_up = true
client_id = some_id
client_secret = some_secret
scopes = user:email
allowed_organizations =

[auth.grafana_com]
enabled = false
allow_sign_up = true
client_id = some_id
client_secret = some_secret
scopes = user:email
allowed_organizations =

#################################### Generic OAuth #######################
[auth.generic_oauth]
name = OAuth
enabled = false
allow_sign_up = true
client_id = some_id
client_secret = some_secret
scopes = user:email
email_attribute_name = email:primary
auth_url =
token_url =
api_url =
team_ids =
allowed_organizations =
tls_skip_verify_insecure = false
tls_client_cert =
tls_client_key =
tls_client_ca =
#################################### Basic Auth ##########################
[auth.basic]
enabled = true

#################################### Auth Proxy ##########################
[auth.proxy]
enabled = false
header_name = X-WEBAUTH-USER
header_property = username
auto_sign_up = true
ldap_sync_ttl = 60
whitelist =
headers =

#################################### Auth LDAP ###########################
[auth.ldap]
enabled = false
config_file = /etc/grafana/ldap.toml   #LDAP特定配置文件的路径（默认值：/etc/grafana/ldap.toml）
allow_sign_up = true    #允许注册几乎总是为真（默认）以允许创建新的Grafana用户（如果ldap身份验证正常）。 如果设置为false，则只有预先存在的Grafana用户才能登录（如果ldap身份验证正常）。

#################################### SMTP / Emailing #####################
[smtp]
enabled = false   #默认为false就是不启动smtp
host = localhost:25   #默认为localhost:25
user =     #如果是SMTP身份验证，则默认为空
password =    #如果是SMTP身份验证，则默认为空
cert_file =     #证书文件的文件路径，默认为空
key_file =     #密钥文件的文件路径，默认为空
skip_verify = false    #验证smtp服务器的SSL？ 默认为false
from_address = admin@grafana.localhost     #发送电子邮件时使用的地址，默认为admin@grafana.localhost
from_name = Grafana     #发送电子邮件时使用的名称，默认为Grafana
ehlo_identity =     #在SMTP对话框中用作EHLO的客户端标识的名称，默认为instance_name。

[emails]
welcome_email_on_sign_up = false   
templates_pattern = emails/*.html  

#################################### Logging ##########################
[log]
mode = console file     #“console”, “file”, “syslog”.  默认为控制台和文件使用空间分隔多个模式
level = info     #“debug”，“info”，“warn”，“error”，“critical”，默认为“info”
filters =     #可选设置，为特定记录器设置不同的级别。比如：filters = sqlstore:debug
[log.console]
level =
format = console     #日志行格式，有效选项是text，console和json
[log.file]
level =
format = text     #日志行格式，有效选项是text，console和json
log_rotate = true    #这使自动日志旋转（切换以下选项），默认为true
max_lines = 1000000     #单个文件的最大行数，默认为1000000
max_size_shift = 28     #单个文件的最大大小移位，默认为28表示1 << 28,256MB
daily_rotate = true     #按天切分日志
max_days = 7    #日志文件的过期天数（最多天后删除），默认为7
[log.syslog]
level =
format = text     #日志行格式，有效选项是text，console和json
#Syslog网络类型和地址。 这可以是udp，tcp或unix。 如果留空，将使用默认的unix端点。
network =
address =
facility =     #系统日志工具。 user，daemon和local0到local7有效。
tag =     #Syslog标签。 默认情况下，使用进程'argv [0]。

#################################### Usage Quotas ########################
[quota]
enabled = false

####将配额设置为-1以使其无限制。####
org_user = 10     ＃限制每个组织的用户数。
org_dashboard = 100    ＃限制每个组织的仪表板数量。
org_data_source = 10    ＃每个组织限制data_sources的数量。
org_api_key = 10     ＃每个组织限制api_keys的数量。
user_org = 10    ＃限制用户可以创建的组织数量。
global_user = -1    ＃全局用户限制。
global_org = -1     #组织的全局限制。
global_dashboard = -1     #仪表板的全局限制
global_api_key = -1     #api_keys的全局限制
global_session = -1     #全局限制登录用户数

#################################### Alerting ############################
[alerting]
enabled = true    #默认为true。 设置为false以禁用警报引擎并隐藏来自UI的警报。
execute_alerts = true   #可以关闭执行警报规则。
error_or_timeout = alerting   #5.3及以上版本，新警报规则的默认设置。 默认为将错误和超时分类为警报。（alerting, keep_state）
nodata_or_nullvalues = no_data      #5.3及以上版本，Grafana如何处理警报中的nodata或null值的默认设置。（alerting, no_data, keep_state, ok）
concurrent_render_limit = 5      #5.3及以上版本，警报通知可以包括图像，但同时渲染许多图像可能会使服务器过载。 此限制将保护服务器免于渲染重载，并确保快速发送通知。 默认值为5。

#################################### Explore #############################
[explore]
enabled = false     #启用“Explore”部分

#################################### Internal Grafana Metrics ############
[metrics]
enabled           = true   #启用指标报告。默认为true。可通过HTTP API /metrics获得。
interval_seconds  = 10     #将度量标准发送到外部TSDB时的刷新/写入间隔。默认为10秒。
basic_auth_username =      #如果set配置用于在metrics端点上进行基本身份验证的用户名。
basic_auth_password =      #如果set配置密码以用于度量标准端点上的基本身份验证。
[metrics.graphite]         #将内部Grafana指标发送到graphite
address =      #Format <Hostname or ip>:port
prefix = prod.grafana.%(instance_name)s.    #Graphite指标前缀。 默认为prod.grafana.%(instance_name)s
[grafana_net]
url = https://grafana.com
[grafana_com]
url = https://grafana.com

#################################### Distributed tracing ############ 
[tracing.jaeger]
address =      #jaeger目的地（比如：localhost：6831）
always_included_tag =     #创建新跨度时将始终包含的标记。比如：（tag1：value1，tag2：value2）
sampler_type = const    #Type指定采样器的类型：const，probabilistic，rateLimiting或remote
sampler_param = 1    #“const”采样器的jaeger samplerconfig参数，“概率”采样器的0或1总是为false/true，“rateLimiting”采样器的概率介于0和1之间，“远程”采样器的每秒跨度数为param， 与“概率”相同，表示收到实际的采样率之前的初始采样率

#################################### External Image Storage ##############
[external_image_storage]    #这些选项控制图像应该如何公开，以便它们可以在像slack这样的服务上共享。
provider =    #可以选择（s3，webdav，gcs，azure_blob，local）

[external_image_storage.s3]
bucket_url =    #（为了向后兼容，仅在未配置存储桶或区域时才有效）S3的存储桶URL。 可以在URL中指定AWS区域，或默认为“us-east-1”，例如  -  http://grafana.s3.amazonaws.com/  -  https://grafana.s3-ap-southeast-2.amazonaws.com/
bucket =     #S3的存储桶名称。 例如grafana.snapshot
region =     #S3的区域名称。 例如 'us-east-1'，'cn-north-1'等
path =    #存储桶内的可选额外路径，适用于应用过期策略
access_key =
secret_key =

[external_image_storage.webdav]
url =     #Url到Grafana将向图片发送PUT请求的地方
username =    #基本身份验证用户名
password =    #基本验证密码
public_url =    #可选参数。 要在通知中向用户发送的网址。 如果字符串包含序列$ {file}，则它将替换为上传的文件名。 否则，文件名将附加到URL的路径部分，保持任何查询字符串不变。

[external_image_storage.gcs]
key_file =    #与Google服务帐户关联的JSON密钥文件的路径，用于进行身份验证和授权。 可以从https://console.developers.google.com/permissions/serviceaccounts创建和下载服务帐户密钥。服务帐户应具有“存储对象编写器”角色。
bucket =    #Google云端存储上的存储桶名称。
path =    #桶内可选的额外路径

[external_image_storage.azure_blob]
account_name =    #存储帐户名称
account_key =    #存储帐户密钥
container_name =    #容器名称以随机名称存储“Blob”图像的位置。 需要预先创建blob容器。 仅支持公共容器。

[external_image_storage.local]
#不需要任何配置

[rendering]
#配置外部图像渲染服务器的选项，如https://github.com/grafana/grafana-image-renderer
server_url =
callback_url =

[panels]
enable_alpha = false   #如果要测试尚未准备好进行常规使用的面板，请设置为true。

[enterprise]
license_path =
```

## 四、Grafana的权限操作（还是跟着官网学习）



### 4.1 组织角色

   用户可以属于一个或多个组织。 用户的组织成员资格与一个角色相关联，该角色定义了允许用户在该组织中执行的操作。

Admin Role（管理员角色）：

   可以做一切操作的组织。 例如：添加和编辑数据源。添加和编辑组织用户和团队。配置App插件并设置组织设置。

Editor Role（编辑角色）：

   可以创建和修改仪表板和警报规则。 可以在特定文件夹和仪表板上禁用此功能。无法创建或编辑数据源，也无法邀请新用户。

Viewer Role（查看者角色）：

   查看任何仪表板。 可以在特定文件夹和仪表板上禁用此功能。无法创建或编辑仪表板或数据源。

   可以通过Grafana服务器设置viewers_can_edit调整此角色。 如果将此设置为true，则使用Viewer的用户也可以进行瞬态仪表板编辑，这意味着他们可以修改面板和查询，但不能保存更改（也不能创建新的仪表板）。 对于希望匿名用户能够编辑面板和查询但不保存或创建新仪表板的公共Grafana安装非常有用。

### 4.2 Dashboard & Folder权限

   对于仪表板和仪表板文件夹，有一个“权限”页面，可以删除编辑器和查看器的默认基于角色的权限。 在此页面上，您可以添加和分配特定用户和团队的权限。可以为组织角色，用户和团队分配和删除权限。

![image.png](assets/004/201902191550577504662850.png)

### 4.3 Datasource Permissions(数据源权限)

   数据源权限仅适用于Grafana Enterprise。 了解更多关于Grafana Enterprise的信息：http://docs.grafana.org/enterprise/

   数据源权限允许限制用户查询数据源的访问权限。 对于每个数据源，都有一个权限页面，可以启用权限并限制对特定Users and Teams的查询权限。

Restricting Access - Enable Permissions（限制访问 - 启用权限）：

   默认情况下，对数据源禁用权限，组织中的任何用户都可以查询组织中的数据源。 例如，具有Viewer角色的用户仍然可以向数据源发出任何可能的查询，而不仅仅是他/她可以访问的仪表板上存在的那些查询。

   为组织中的数据源启用权限后，您将限制该数据源的管理员和查询访问权限，以管理该组织中的用户。

   要为数据源启用权限，请执行以下操作：

\1. 导航到 Configuration / Data Sources。

\2. 选择要为其启用权限的数据源。

\3. 选择“Permissions”选项卡，然后单击“Enable”按钮。

Allow users and teams to query a datasource（允许用户和团队查询数据源）：

   为数据源启用权限后，可以为用户和团队分配查询权限，以允许访问查询数据源。

   为用户和团队分配查询权限：

\1. 导航到Configuration / Data Sources。

\2. 选择要为其分配查询权限的数据源。

\3. 选择“Permissions”选项卡。

\4. 单击“Add Permission”按钮。

\5. 选择“Team/User”，找到要允许查询访问的team/user，然后单击“Save”按钮。

Restore Default Access - Disable Permissions（恢复默认访问权限 - 禁用权限）：

   如果已启用数据源的权限并希望将数据源权限返回到默认值，即该组织中的任何用户都可以查询数据源，则只需单击按钮即可禁用权限。 请注意，将删除为数据源创建的所有现有权限。

   要禁用数据源的权限，请执行以下操作：

\1. 导航到Configuration / Data Sources。

\2. 选择要禁用其权限的数据源。

\3. 选择“Permissions”选项卡，然后单击“Disable Permissions”按钮。

## 五、Grafana CLI（还是跟着官网学习）

  Grafana cli是一个与Grafana-server捆绑在一起的小型可执行文件，应该在运行Grafana-server的同一台机器上执行。

### 5.1 Plugins（插件）

   CLI允许在运行的计算机上安装，升级和管理插件。

   来自Grafana 3.0+不仅支持数据源插件，还支持面板插件和应用程序。 将面板作为插件，可以轻松创建和添加任何类型的面板，以显示数据或改进喜欢的仪表板。 应用程序是Grafana中的新功能，可以将数据源，面板，仪表板和Grafana页面捆绑在一起，形成一个紧密的体验。

   Grafana已经拥有一个强大的贡献者和插件开发者社区。 通过简化开发和安装插件，我们希望社区能够变得更强大，并开发出我们永远不会想到的新插件。要发现插件，请查看官方插件存储库：https://grafana.com/plugins

  安装插件的最简单方法是使用与grafana捆绑在一起的CLI工具grafana-cli。 在修改插件之后进行任何修改之前，需要重新启动grafana-server。

Grafana插件目录：

   在Linux系统上，grafana-cli将假设grafana插件目录是/var/lib/grafana/plugins。 通过指定-pluginsDir标志，可以覆盖grafana-cli将操作的目录。 在Windows系统上，必须为每个调用指定此参数。

Grafana-cli命令：

grafana-cli plugins list-remote  #列出可用的插件

grafana-cli plugins install <plugin-id> #安装最新版本的插件

grafana-cli plugins install <plugin-id> <version> #安装特定版本的插件

grafana-cli plugins ls  #列出已安装的插件

grafana-cli plugins update-all  #更新所有已安装的插件

grafana-cli plugins update <plugin-id>  #更新一个插件

grafana-cli plugins remove <plugin-id>  #删除一个插件

Installing Plugins Manually（手动安装插件）：

   如果Grafana Server无法访问Internet，则必须下载插件并手动将其复制到Grafana Server。

   Grafana.com API的下载URL采用以下形式：https://grafana.com/api/plugins/<plugin id>/versions/<version number>/download

可以使用--pluginUrl选项指定本地URL：grafana-cli --pluginUrl https://nexus.company.com/grafana/plugins/<plugin-id>-<plugin-version>.zip plugins install <plugin-id>

   要通过Grafana.com API手动安装插件：找到要下载的插件，插件ID可以在Grafana.com上插件页面的“Installation”选项卡上找到。 在这个例子中，插件id是jdbranham-diagram-panel：

第一步：

![image.png](assets/004/201902201550665574734932.png)

Bash

```bash
{
  "id": 145,  
  "typeId": 3,  
  "typeName": "Panel",  
  "typeCode": "panel",  
  "slug": "jdbranham-diagram-panel",  
  "name": "Diagram",  
  "description": "Diagram panel for grafana",
  ...
```

找到下载链接：

Bash

```bash
{
   "rel": "download",   
   "href": "/plugins/jdbranham-diagram-panel/versions/1.4.0/download"}
```

第二步下载插件：

https://grafana.com/api/plugins/<plugin id from step 1>/versions/<current version>/download

（例如：https://grafana.com/api/plugins/jdbranham-diagram-panel/versions/1.4.0/download）。 将下载的文件解压缩到Grafana Server的plugins目录中。

第三步：

重新启动Grafana Server。

### 5.2 Admin

此功能仅适用于Grafana 4.1及更高版本。

要显示所有管理员命令：grafana-cli admin

重置管理员密码：

  可以使用CLI重置admin用户的密码。 丢失管理员密码时，此命令的用例是：grafana-cli admin reset-admin-password ...

   如果运行该命令则返回此错误：无法找到配置默认值，请确保已设置homepath命令行参数或工作目录为homepath，然后有两个标志可用于设置homepath和配置文件路径：grafana-cli admin reset-admin-password --homepath "/usr/share/grafana" newpass

   如果没有丢失管理员密码，那么最好在Grafana UI中进行设置。 如果需要在脚本中设置密码，则可以使用Grafana API。 以下是使用基本身份验证的curl的示例：

Bash

```bash
curl -X PUT -H "Content-Type: application/json" -d '{
  "oldPassword": "admin",
  "newPassword": "newpass",
  "confirmNew": "newpass"
}' http://admin:admin@<your_grafana_host>:3000/api/user/password
```

### 5.3 另外内部指标不属于这里

   Grafana在内部收集有关自身的一些指标。 目前，Grafana支持将指标推向Graphite或将其暴露给普罗米修斯。

   要发出内部指标，您必须在grafana.ini配置文件的[metrics]部分下启用该选项。 如果要将指标推送到Graphite，还必须配置metrics.graphite部分。

## 六、Provisioning Grafana（翻译官网学习）

​    在以前的Grafana版本中，您只能使用API来配置数据源和仪表板。 但是，这需要在开始创建仪表板之前运行服务，并且还需要为HTTP API设置凭据。 在v5.0中，我们决定通过添加使用配置文件的新的主动配置系统来改善这种体验。 这将使GitOps更加自然，因为可以通过可以进行版本控制的文件来定义数据源和仪表板。 我们希望扩展此系统以便稍后添加对用户，组织和警报的支持。

### 6.1 Datasources

此功能适用于v5.0。

   通过在provisioning/datasources目录中添加一个或多个yaml配置文件，可以在Grafana中管理数据源。 每个配置文件都可以包含将在启动期间添加或更新的数据源列表。 如果数据源已存在，Grafana将更新它以匹配配置文件。 配置文件还可以包含应删除的数据源列表。 该列表称为deleteDatasources。 在插入/更新数据源列表中的数据源之前，Grafana将删除deleteDatasources中列出的数据源。

运行多个Grafana实例：

   如果运行的是Grafana的多个实例，如果它们具有不同版本的datasource.yaml配置文件，则可能会遇到问题。 解决此问题的最佳方法是为配置中的每个数据源添加版本号，并在更新配置时增加它。 Grafana只会使用与配置中指定的版本号相同或更低的版本号更新数据源。 这样，如果旧配置同时重新启动，则无法覆盖新配置。

示例数据源配置文件：

Bash

```bash
# config file version
apiVersion: 1

# list of datasources that should be deleted from the database
deleteDatasources:
  - name: Graphite    
    orgId: 1
    
# list of datasources to insert/update depending
# what's available in the database
datasources:
  # <string, required> name of the datasource. Required
  - name: Graphite  
  # <string, required> datasource type. Required
  type: graphite  
  # <string, required> access mode. proxy or direct (Server or Browser in the UI). Required
  access: proxy  
  # <int> org id. will default to orgId 1 if not specified
  orgId: 1
  # <string> url
  url: http://localhost:8080
  # <string> database password, if used
  password:
  # <string> database user, if used
  user:
  # <string> database name, if used
  database:
  # <bool> enable/disable basic auth
  basicAuth:
  # <string> basic auth username
  basicAuthUser:
  # <string> basic auth password
  basicAuthPassword:
  # <bool> enable/disable with credentials headers
  withCredentials:
  # <bool> mark as default datasource. Max one per org
  isDefault:
  # <map> fields that will be converted to json and stored in jsonData
  jsonData:
     graphiteVersion: "1.1"
     tlsAuth: true
     tlsAuthWithCACert: true
  # <string> json object of data that will be encrypted.
  secureJsonData:
    tlsCACert: "..."
    tlsClientCert: "..."
    tlsClientKey: "..."
  version: 1
  # <bool> allow users to edit datasources from the UI.
  editable: false
```

### 6.2 Dashboards(仪表板)

   通过在provisioning/dashboards目录中添加一个或多个yaml配置文件，可以在Grafana中管理仪表板。 每个配置文件都可以包含一个仪表板提供程序列表，它将从本地文件系统将仪表板加载到Grafana中。仪表板提供程序配置文件看起来有点像这样：

Bash

```bash
apiVersion: 1providers:- name: 'default'
  orgId: 1
  folder: ''
  type: file  disableDeletion: false
  updateIntervalSeconds: 10 #how often Grafana will scan for changed dashboards
  options:
    path: /var/lib/grafana/dashboards
```

   当Grafana启动时，它将update/insert配置路径中可用的所有仪表板。 然后稍后轮询该路径并查找更新的json文件并将这些update/insert到数据库中。

更改已配置的仪表板:

   可以在Grafana UI中对配置的仪表板进行更改，但目前无法自动将更改保存回配置源。 但是，如果您对配置的仪表板进行了更改，则可以保存仪表板，这将显示无法保存配置的仪表板对话框，如下面的屏幕截图所示。 此处提供的选项可让您将JSON复制到剪贴板和/或将JSON保存到文件，这可以帮助您将仪表板更改同步回配置源。

   注意：输入字段中显示的JSON以及将Copy JSON用于剪贴板和/或将JSON保存到文件时，将自动删除id字段以帮助配置工作流。

![image.png](assets/004/201902221550833749698641.png)

Reusable Dashboard Urls(可重复使用的仪表板网址):

   如果json文件中的仪表板包含uid，Grafana将强制insert/update该uid。这允许在Grafana实例之间迁移仪表板和从配置中配置Grafana，而不会破坏给定的URL，因为新的仪表板URL使用uid作为标识符。 当Grafana启动时，它将更新/插入配置文件夹中的所有仪表板。 如果修改文件，仪表板也将更新。 默认情况下，如果删除文件，Grafana将删除数据库中的仪表板。 可以使用disableDeletion设置禁用此行为。

   注意。 如果重新使用应该是唯一的设置，则配置允许覆盖现有仪表板，从而导致问题。 注意不要在文件夹或同一安装中的uid中多次重复使用相同的标题，因为这会导致奇怪的行为。

Alert Notification Channels(警报通知频道):

   可以通过在provisioning/notifiers目录中添加一个或多个yaml配置文件来配置警报通知通道。

   每个配置文件都可以包含以下顶级字段： - notifiers，将在启动期间添加或更新的警报通知列表。 如果通知通道已存在，Grafana将更新它以匹配配置文件。 - delete_notifiers，在插入/更新通知程序列表中之前要删除的警报通知列表。供应通过uid查找警报通知，并将使用提供的uid更新任何现有通知。

   默认情况下，将仪表板导出为JSON将使用顺序标识符来引用警报通知。 可以选择指定字段uid以指定警报名称的字符串标识符。

Bash

```bash
{
  ...
      "alert": {
        ...,
        "conditions": [...],
        "frequency": "24h",
        "noDataState": "ok",
        "notifications": [
           {"uid": "notifier1"},
           {"uid": "notifier2"},
        ]
      }
  ...
}
```

警报通知通道配置文件示例：

Bash

```bash
notifiers:
  - name: notification-channel-1
    type: slack    
    uid: notifier1    
    # either
    org_id: 2
    # or
    org_name: Main Org.    
    is_default: true
    # See `Supported Settings` section for settings supporter for each
    # alert notification type.
    settings:
      recipient: "XXX"
      token: "xoxb"
      uploadImage: true
      url: https://slack.com
delete_notifiers:
  - name: notification-channel-1
    uid: notifier1    # either
    org_id: 2
    # or 
    org_name: Main Org.  
  - name: notification-channel-2
    # default org_id: 1
```

支持的设置：

以下部分详细介绍了每种警报通知类型的受支持设置。

警报通知pushover：Name：apiToken、userKey、device、retry、expire

警报通知slack：Name：url、recipient、username、iconEmoji、iconUrl、uploadImage、mention、token

警报通知victorops：Name：url

警报通知kafka：Name：kafkaRestProxy、kafkaTopic

警报通知LINE：Name：token

警报通知pagerduty：Name：integrationKey

警报通知sensu：Name：url、source、handler、username、password

警报通知prometheus-alertmanager：Name：url

警报通知teams：Name：url

警报通知dingding：Name：url

警报通知email：Name：addresses

警报通知hipchat：Name：url、apikey、roomid

警报通知opsgenie：Name：apiKey、apiUrl

警报通知telegram：Name：bottoken、chatid

警报通知threema：Name：gateway_id、recipient_id、api_secret

警报通知webhook：Name：url、username、password

### 6.3 Keyboard shortcuts（键盘快捷键）

​    Grafana v4引入了许多非常强大的键盘快捷键。 现在，可以通过将鼠标悬停在面板上来使其聚焦。 将面板聚焦后，可以简单地按e切换面板编辑模式，或按v切换全屏模式。“p"“r”卸下面板。 "p" "s"打开共享模式。打“?”在键盘上打开快捷方式帮助模态。

Global（全局）：

Bash

```bash
g h   #转到主页仪表板
g p   #转到个人资料
s o   #打开搜索
s s   #带星标过滤器的开放搜索
t     #在标签视图中打开搜索
esc   #退出编辑/设置视图
```

Dashboard：

Bash

```bash
mod+s   #保存仪表板
mod+h   #隐藏行控件
d r     #刷新所有面板
d s     #仪表盘设定
d v     #切换非活动/查看模式
d k     #切换信息亭模式（隐藏顶部导航）
d E     #展开所有行
d C     #折叠所有行
mod+o   #切换共享图形十字线   #mod =在Windows或Linux上为Ctrl，在macOS上为Cmd键
```

Focused Panel:

Bash

```bash
e    #切换面板编辑视图
v    #切换面板全屏视图
p s  #打开面板共享模式
p d  #重复面板
p r  #卸下面板
```

Time Range:

Bash

```bash
t z   #缩小时间范围
t     #将时间范围移回
t     #向前移动时间范围
```