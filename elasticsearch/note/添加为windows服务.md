ELK：Windows下将Elasticsearch、Logstash、Kibana添加为后台服务

依然是那个墨镜

已于 2022-09-24 16:15:40 修改

698
 收藏 1
分类专栏： ELK 文章标签： elasticsearch windows 大数据 elk
版权

ELK
专栏收录该内容
2 篇文章0 订阅
订阅专栏
目录

1.elasticsearch添加后台服务

2.Logstash添加为后台服务

3.kibana添加为后台服务

1.elasticsearch添加后台服务
管理员模式运行CMD窗口，进入elasticsearch的bin目录，运行：

elasticsearch-service.bat install
，运行后显示如下。



 显示服务已经安装，这时进入windows下的服务窗口就能看到安装的elasticsearch服务了。



2.Logstash添加为后台服务
Logstash添加后台服务用的是 NSSM，NSSM下载地址：http://www.nssm.cc/download。

解压后



把对应的nssm.exe文件放到“D:\devsofts\elk7.6.2”下。



 切换到logstash的bin目录下，创建“run-logstash.bat”文件，编辑器打开后，写入以下代码：

logstash.bat -f ./logstash.conf
保存退出。

管理员模式运行CMD窗口，切换到exe目录，执行“nssm.exe install logstash”，



弹出以下窗口，选择logstash的bin目录。点击“Install Serviec”安装。



  出现上面界面后，去“服务”里启动logstash吧。



3.kibana添加为后台服务
kibana添加后台服务同样使用的是 NSSM，NSSM下载地址：http://www.nssm.cc/download。

解压后



 把对应的nssm.exe文件放到“D:\devsofts\elk7.6.2”下。



 管理员模式运行CMD窗口，切换到exe目录，执行“nssm.exe install kibana”，弹出以下窗口，选择kibana目录。点击“Install Serviec”安装。



 安装成功后在“服务”窗口就可以看到kibana服务了，至此就可以手动在后台启动和停止服务了。



————————————————
版权声明：本文为CSDN博主「依然是那个墨镜」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/louis_lee7812/article/details/127018598