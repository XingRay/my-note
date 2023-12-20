nohup执行程序后， 返回进程pid

DancingCat~

于 2022-10-04 11:01:56 发布

614
 收藏
文章标签： linux bash 服务器
版权
话不多说，直接上代码！！！

nohup 命令 [ 参数 ] > 日志文件.log 2>&1 & echo $!

此命令将在后台执行脚本或程序，并将日志信息（包括标准输出和错误输出）重定向到 .log 文件中，将进程pid打印到终端或显示器上。（此处为了清楚命令的使用，直接上中文，满满干货！！！）

此命令可以直接获得进程pid，方便后续的操作（查看进程、将进程 kill 掉）。

进阶操作
在shell编程中，通过此命令可以直接获取pid，进行进一步操作。（比如：设置任务结束直接关机，需要用到pid）

nohup 命令 [ 参数 ] > 日志文件.log 2>&1 & echo $! > 文件.pid

这样可以直接将进程的pid，重定向到某一文件中。（文件不存在会直接创建）

pid=`cat 文件.pid`

通过这个命令，获得进程pid，得到 $pid 变量。





```
if ps -p $PID > /dev/null
then
   echo "$PID is running"
   # Do something knowing the pid exists, i.e. the process with $PID is running
fi
```

