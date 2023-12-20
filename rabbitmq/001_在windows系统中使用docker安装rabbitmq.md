### 在windows系统中使用docker安装rabbitmq

### 1 准备目录

创建rabbitmq目录

```bash
D:/webapp/javashop/docker/rabbitmq
```

创建子目录

```bash
config
data
log
plugins
```



### 2 准备配置文件

在config目录下创建主配置文件 rabbitmq.conf ，注意要使用新版本的conf的格式，内容如下：

```bash
loopback_users.guest = false
listeners.tcp.default = 5672
default_pass = 123456
default_user = admin
management.tcp.port = 15672
```

注意这里在配置文件中设置了默认的用户名和密码，就不能在docker run 中的环境变量中再设置用户名和密码，会产生冲突报错。



在config目录下创建插件启用配置文件 enabled_plugins ，内容如下

```bash
[autocluster,rabbitmq_consistent_hash_exchange,rabbitmq_delayed_message_exchange,rabbitmq_federation,rabbitmq_federation_management,rabbitmq_management,rabbitmq_mqtt,rabbitmq_shovel,rabbitmq_shovel_management,rabbitmq_stomp,rabbitmq_web_stomp].
```

这里大部分插件在rabbitmq的容器目录 `/opt/rabbitmq/plugins` 已经存在，不需要单独下载，这里只有 rabbitmq_delayed_message_exchange 需要单独下载



### 3 下载插件

在 https://www.rabbitmq.com/community-plugins.html 中查找到需要下载的插件的下载地址 

```bash
rabbitmq_delayed_message_exchange
A plugin that adds delayed-messaging (or scheduled-messaging) to RabbitMQ.
Releases
Author: Alvaro Videla
GitHub: rabbitmq/rabbitmq-delayed-message-exchange
```

https://github.com/rabbitmq/rabbitmq-delayed-message-exchange

https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases

插件版本根据rabbitmq的版本进行选择，这里插件和rabbitmq一样选择 3.8.9 

https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/3.8.9/rabbitmq_delayed_message_exchange-3.8.9-0199d11c.ez

下载插件，保存到 plugins 目录，通过文件挂载方式挂载到容器内的插件目录 /opt/rabbitmq/plugins，注意不要目录挂载，会把容器内部的自带插件覆盖。



### 4 创建容器

```bash
docker run --name javashop-rabbitmq -d --hostname host-rabbit -p 45670:5672 -p 45671:15672 -p 45672:25672 -p 45673:35672 -p 44369:4369 -v D:/webapp/javashop/docker/rabbitmq/config/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf -v D:/webapp/javashop/docker/rabbitmq/config/enabled_plugins:/etc/rabbitmq/enabled_plugins -v D:/webapp/javashop/docker/rabbitmq/data:/var/lib/rabbitmq/mnesia -v D:/webapp/javashop/docker/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.8.9-0199d11c.ez:/opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-3.8.9-0199d11c.ez -e RABBITMQ_ERLANG_COOKIE='MY-SECRET-KEY' rabbitmq:3.8.9
```



参考：

rabbitmq配置文件

https://www.rabbitmq.com/configure.html#config-file



rabbitmq插件

https://www.rabbitmq.com/community-plugins.html



rabbitmq插件-rabbitmq-delayed-message-exchange

https://github.com/rabbitmq/rabbitmq-delayed-message-exchange

