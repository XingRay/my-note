查看所有开放的端口

```bash
netstat -aptn
```

结果示例：

```bash
(Not all processes could be identified, non-owned process info
 will not be shown, you would have to be root to see it all.)
Active Internet connections (servers and established)
Proto Recv-Q Send-Q Local Address           Foreign Address         State       PID/Program name
tcp        0      0 127.0.0.1:33060         0.0.0.0:*               LISTEN      -
tcp        0      0 0.0.0.0:3306            0.0.0.0:*               LISTEN      -
tcp        0      0 127.0.0.53:53           0.0.0.0:*               LISTEN      -
tcp        0      0 0.0.0.0:139             0.0.0.0:*               LISTEN      -
tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN      -
tcp        0      0 0.0.0.0:445             0.0.0.0:*               LISTEN      -
tcp        0      0 127.0.0.1:6010          0.0.0.0:*               LISTEN      -
tcp        0      0 192.168.0.105:445       192.168.0.118:50323     ESTABLISHED -
tcp        0     64 192.168.0.105:22        192.168.0.118:50855     ESTABLISHED -
tcp        0      0 192.168.0.105:22        192.168.0.118:50856     ESTABLISHED -
tcp6       0      0 :::7848                 :::*                    LISTEN      -
tcp6       0      0 ::1:6010                :::*                    LISTEN      -
tcp6       0      0 :::9848                 :::*                    LISTEN      -
tcp6       0      0 :::9849                 :::*                    LISTEN      -
tcp6       0      0 :::139                  :::*                    LISTEN      -
tcp6       0      0 :::22                   :::*                    LISTEN      -
tcp6       0      0 :::445                  :::*                    LISTEN      -
tcp6       0      0 :::8848                 :::*                    LISTEN      -
tcp6       0      0 192.168.0.105:9848      192.168.0.118:50670     ESTABLISHED -
tcp6       0      0 192.168.0.105:9848      192.168.0.118:50616     ESTABLISHED -
tcp6       0      0 192.168.0.105:9848      192.168.0.118:62410     ESTABLISHED -
tcp6       0      0 192.168.0.105:57962     192.168.0.105:7848      ESTABLISHED -
tcp6       0      0 192.168.0.105:9848      192.168.0.118:50665     ESTABLISHED -
tcp6       0      0 192.168.0.105:9848      192.168.0.118:50667     ESTABLISHED -
tcp6       0      0 192.168.0.105:9848      192.168.0.118:50381     ESTABLISHED -
tcp6       0      0 192.168.0.105:9848      192.168.0.118:50668     ESTABLISHED -
tcp6       0      0 192.168.0.105:7848      192.168.0.105:57962     ESTABLISHED -
tcp6       0      0 192.168.0.105:9848      192.168.0.118:50617     ESTABLISHED -

```







1. 删除文件夹的内容包括文件夹：

rm -rf 文件夹的名字    （-r 是 循环的意思， f是不询问的意思）

2 .删除文件夹的内容不包括文件夹：

rm -rf 文件夹的名字/*   (后面加上/*表示删除内容不删除文件夹)

清空当前目录

```bash
rm -rf ./*
```

如果您当前的工作目录是 `/var/www`，并且您想删除其中名为 `sample` 的目录中的所有文件和子目录，您可以简单地使用 命令：

```bash
sudo rm -rf sample/*
```

或者，如果使用绝对路径，可以运行以下命令。

```bash
sudo rm -rf /var/www/sample/*
```



1.查看终端历史

```bash
history
```

2.导出终端历史为文本

```cobol
history >screenlog.txt
```
