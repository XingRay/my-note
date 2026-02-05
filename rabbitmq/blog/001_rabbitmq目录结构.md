# rabbitmq目录结构

文章目录
1. 配置目录
2.数据目录
3.日志文件
4.脚本目录
1. 配置目录
rabbitmq配置目录：/etc/rabbitmq/

 常见配置文件有：

（1）配置文件 rabbitmq.conf

（2）环境变量文件 rabbitmq-env.conf

（3）补充配置文件 advanced.config

2.数据目录
rabbitmq数据目录：/var/lib/rabbitmq/

目录文件有：


3.日志文件
rabbitmq日志文件: /var/log/rabbitmq

 目录文件有：


4.脚本目录
rabbitmq命令脚本：/usr/lib/rabbitmq/


1.bin目录


bin目录脚本是由目录/usr/lib/rabbitmq/lib/rabbitmq_server-3.8.6/sbin/创建的软链接

2.lib目录

ll ./rabbitmq_server-3.8.6


escript目录存放rabbitmq真正启动的命令


plugins目录

存放各种插件压缩包（可重第三方下载插件放置此位置，解压后启动插件
