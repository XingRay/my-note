## rabbitmq入门



### 1 安装

首先需要确定安装的rabbitmq及对应兼容的erlang的版本

https://www.rabbitmq.com/which-erlang.html

这里以rabbitmq 3.11.16 和 ErlangOTP_25.3.2为例



1.1 安装 erlang

下载

https://www.erlang.org/patches/otp-25.3.2

https://github.com/erlang/otp/releases

使用管理员身份安装 erlang ，安装的路径不要包含中文字符，安装完成后设置环境变量

```
ERLANG_HOME = D:\ProgramFiles\ErlangOTP
```

将 ERLANG_HOME  加入系统path

测试命令 

```bash
erl
```



1.2 安装 rabbitmq

https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.11.16/rabbitmq-server-3.11.16.exe

以管理员身份安装rabbitmq，安装的路径不要包含中文

```
在window系统下安装遇到的 RabbitMq的服务名失效
首先，如果你在启动服务的时候报这个错，那么不要怀疑，不要在别的地方找原因，肯定是你安装的时候出了问题，至于为什么安装的时候出了问题，是因为你之前的卸载不干净。
第一步 ，win+r，输入control打开控制面板，找到程序和功能卸载RabbitMq。
第二步 ，在你的c盘C:\Users\XXXX\AppData\Roaming下面有个RabbitMq文件夹删掉。
第三步 ，win+R输入regedit打开注册表，在

HKEY_LOCAL_MACHINE\SOFTWARE\Ericsson\Erlang下面有个HKEY_LOCAL_MACHINE\SOFTWARE\Ericsson\Erlang\ErlSrv删掉就行了。
```

设置系统环境变量设置rabbitmq的数据保存路径：

```
RABBITMQ_BASE = D:\develop\rabbitmq\data
```



2 整体流程

![image-20230520010525844](D:\myNote\resources\image-20230520010525844.png)
