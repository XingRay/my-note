## ubuntu 解决too many open files问题

### 一、产生原因

too many open files(打开的文件过多)是Linux系统中常见的错误，从字面意思上看就是说程序打开的文件数过多，不过这里的files不单是文件的意思，也包括打开的通讯链接(比如socket)，正在监听的端口等等，所以有时候也可以叫做句柄(handle)，这个错误通常也可以叫做句柄数超出系统限制。 
引起的原因就是进程在某个时刻打开了超过系统限制的文件数量以及通讯链接数，通过命令ulimit -a可以查看当前系统设置的最大句柄数是多少：

```bash
root@instance-myaj5rsw:~# ulimit -a
core file size          (blocks, -c) unlimited
data seg size           (kbytes, -d) unlimited
scheduling priority             (-e) 0
file size               (blocks, -f) unlimited
pending signals                 (-i) 15731
max locked memory       (kbytes, -l) 64
max memory size         (kbytes, -m) unlimited
open files                      (-n) 65534
pipe size            (512 bytes, -p) 8
POSIX message queues     (bytes, -q) 819200
real-time priority              (-r) 0
stack size              (kbytes, -s) 8192
cpu time               (seconds, -t) unlimited
max user processes              (-u) 15731
virtual memory          (kbytes, -v) unlimited
file locks                      (-x) unlimited
```

open files那一行就代表系统目前允许单个进程打开的最大句柄数，这里是1024。 
使用命令lsof -p 进程id可以查看单个进程所有打开的文件详情，使用命令lsof -p 进程id | wc -l可以统计进程打开了多少文件：

```bash
root@instance-myaj5rsw:~# ps -aux|grep python
root      71847 43.4  2.4 565404 50800 ?        Rl   11:40  95:12 python run_cloudweb.py
root      98890  0.0  0.0  10468   920 pts/0    S+   15:19   0:00 grep --color=auto python
root@instance-myaj5rsw:~# lsof -p 71847|wc -l
1063
```

可以看到它目前打开了1063个文件数，如果文件数过多，使用lsof -p 进程id命令无法完全查看的话，可以使用lsof -p 进程id > openfiles.log将执行结果内容输出到日志文件中查看。



### 二、解决方法

1、保证session required pam_limits.so被打开
编辑/etc/pam.d/su，找到下列行

```bash
#下面这一行一定要打开
session required pam_limits.so
```

2、增大允许打开的文件数——命令方式

```bash
ulimit -n 2048
```

这样就可以把当前用户的最大允许打开文件数量设置为2048了，但这种设置方法在重启后会还原为默认值。 
ulimit -n命令非root用户只能设置到4096。 
想要设置到8192需要sudo权限或者root用户。

3、增大允许打开的文件数——修改系统配置文件

```bash
vi /etc/security/limits.conf
```

在最后加入  

```bash
*    soft nofile 65534
*    hard nofile 65534
root soft nofile 65534
root hard nofile 65534 
```

或者只加入

```
* - nofile 8192
```

最前的 * 表示所有用户，可根据需要设置某一用户，例如

```bash
roy soft nofile 8192
roy hard nofile 8192
```

注意”nofile”项有两个可能的限制措施。就是项下的hard和soft。 要使修改过得最大打开文件数生效，必须对这两种限制进行设定。 如果使用”-“字符设定, 则hard和soft设定会同时被设定。

说明：* 代表针对所有用户

noproc 是代表最大进程数（*   soft noproc   11000      *   hard noproc   11000  ）

nofile 是代表最大文件打开数

ubuntu的root用户必须注明用户

4、检查程序问题

如果你对你的程序有一定的解的话，应该对程序打开文件数(链接数)上限有一定的估算，如果感觉数字异常，请使用第一步的lsof -p 进程id > openfiles.log命令，获得当前占用句柄的全部详情进行分析，

```bash
1）打开的这些文件是不是都是必要的？
2）定位到打开这些文件的代码
3）是否程序操作了文件写入，但是没有进行正常关闭
4）是否程序进行了通讯，但是没有正常关闭(也就是没有超时结束的机制)
```

如果程序中存在这些问题的话，无论系统句柄数设置的多么大，随着时间的推移，也一定会占用完。

参考链接：https://dengqsintyt.iteye.com/blog/2087342

https://blog.csdn.net/yuchunhai321/article/details/85985621

另一种解决方式方法，参考下面链接：

https://blog.csdn.net/xahehongyuan/article/details/78163601
