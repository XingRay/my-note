## Linux拷贝服务器文件夹到本地

### 方法1：scp命令

scp命令是用于通过 SSH 协议安全地将文件复制到远程系统和从远程系统复制文件到本地的命令。Linux SCP 命令是一种在两个远程节点之间传输文件的便捷且安全的方式，使用 SSH 意味着它享有与 SSH 相同级别的数据加密，因此被认为是跨两个远程主机传输文件的安全方式。

举例SSH指令

```
ssh root@10.111.100.10 -p 22
```

由于执行SSH指令【ssh user@server-ip -p PORT】后，终端就切换到了远程服务器的环境，填写个人路径时查不到，此时的环境已经不再是本地环境，只需要再新建一个终端窗口来执行：

```
scp [option] /path/to/source/file user@server-ip:/path/to/destination/directory
```

scp命令常用的几个选项：

```
-C - 这会在复制过程中压缩文件或目录
-P - 如果默认 SSH 端口不是 22，则使用此选项指定 SSH 端口
-r - 此选项递归复制目录及其内容
-p - 保留文件的访问和修改时间
```

前面地址为源地址，后面地址为目标地址，根据需要改，本地和服务器互相传改变前后位置即可

root：用户名，10.100.100.10：IP，51：端口号，根据需要改，举例说明如下：

Linux服务器内部拷贝文件夹source到des

```bash
scp -r /workspace/source /workspace/des
```



Mac拷贝本地文件夹到服务器

```bash
scp -rP 51 Users/source root@10.100.100.10:/workspace/des
```

Mac拷贝服务器文件夹到本地

```bash
scp -rP 51 root@10.100.100.10:/workspace/source /Users/des
```

Mac拷贝服务器文件到本地

```bash
scp -P 51 root@10.100.100.10:/workspace/data.py /Users/des
```

Windows拷贝本地文件到服务器

```bash
scp -rP 51 E:\source root@10.100.100.10:/workspace/des
```

注意：本地复制到Linux出现No such file or directory：win环境必须用绝对路径



### 方法2：sftp

1.通过命令行登录服务器：

```bash
ssh user@server-ip -p PORT
```

2.本地登录sftp：

```bash
sftp -P PORT user@server-ip    
```

3.本地文件推到服务器：

```bash
put [-r]  local_file_path remote_server_path
```

4.服务器文件拉到本地：

```bash
get -r remote_server_files_path 
```

5.退出sftp：

```bash
quit
```

