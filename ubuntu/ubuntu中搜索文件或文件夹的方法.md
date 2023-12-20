## ubuntu中搜索文件或文件夹的方法

### whereis+文件名

用于程序名的搜索，搜索结果只限于二进制文件（参数-b）、man说明文件（参数-m）和源代码文件（参数-s），如果省略参数，则返回所有信息。

```
$ whereis python
python: /usr/bin/python3.6m /usr/bin/python2.7 /usr/bin/python3.8 /usr/bin/python3.8-config /usr/bin/python /usr/bin/python3.6 /usr/lib/python2.7 /usr/lib/python3.9 /usr/lib/python3.6 /usr/lib/python3.8 /etc/python3.6 /etc/python3.8 /etc/python /etc/python2.7 /usr/local/lib/python2.7 /usr/local/lib/python3.8 /usr/local/lib/python3.6 /usr/include/python3.8 /usr/share/python
```



### find / -name +文件名

find是在指定的目录下遍历查找，如果目录使用 / 则表示在所有目录下查找，find方式查找文件消耗资源比较大，速度也慢一点。

```
$ find /usr -name python
/usr/include/boost/python
/usr/include/boost/parameter/aux_/python
/usr/include/boost/mpi/python
/usr/local/lib/python3.8/dist-packages/virtualenv/activation/python
/usr/local/cuda-11.4/nsight-compute-2021.2.0/extras/python
/usr/local/cuda-11.4/share/gdb/python
/usr/local/cuda-11.4/nsight-systems-2021.2.4/host-linux-x64/python
/usr/local/cuda-11.4/nsight-systems-2021.2.4/host-linux-x64/python/bin/python
/usr/local/cuda-11.4/nsight-systems-2021.2.4/target-linux-armv8/python
/usr/local/cuda-11.4/nsight-systems-2021.2.4/target-linux-armv8/python/bin/python
/usr/local/cuda-11.4/nsight-systems-2021.2.4/target-linux-x64/python
/usr/local/cuda-11.4/nsight-systems-2021.2.4/target-linux-x64/python/bin/python
/usr/bin/python
/usr/share/python
/usr/share/gcc/python
/usr/share/gdb/python
```

### locate+文件名

linux会把系统内所有的文件都记录在一个数据库文件中，使用locate+文件名的方法会在linux系统维护的这个数据库中去查找目标，相比find命令去遍历磁盘查找的方式，效率会高很多，比较推荐使用这种方法。

```
$ locate tesseract-4.1.1.tar.gz
/home/ubuntu/.local/share/Trash/files/tesseract-4.1.1.tar.gz
/home/ubuntu/.local/share/Trash/info/tesseract-4.1.1.tar.gz.trashinfo
```

但有一个问题是数据库文件不是实时更新的，一般会每周更新一次，所以使用locate命令查找到的结果不一定是准确的。当然可以在使用locate之前通过 updatedb 命令更新一次数据库，保证结果的性。

如何安装locate

```
$ apt-get install mlocate
$ updatedb
```

### which+可执行文件名

which的作用是在PATH变量指定的路径中，搜索某个系统命令的位置，并且返回第一个搜索结果。
使用which命令，就可以看到某个系统命令是否存在，以及执行的到底是哪一个位置的命令。
which指令会在环境变量$PATH设置的目录里查找符合条件的文件，所以基本的功能是寻找可执行文件。

```
$ which python3
/usr/bin/python3
```

