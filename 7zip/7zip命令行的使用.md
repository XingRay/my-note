# Windows上使用7z命令行进行压缩解压



https://7-zip.org/download.html

可以下载不需要安装版的zip

https://7-zip.org/a/7z2301-extra.7z

解压后将目录添加到path，即可使用7za代替7z，进行压缩和解压



解压文件

```bash
%currentPath%\software\7z\7za.exe x -o%currentPath% %currentPath%\%volumeDirName%.zip
```



版权
配置
下载安装7z：官网 7-Zip

配置环境变量：win键按下，搜索 env，打开编辑环境变量，选择环境变量，在系统变量下的 path 中添加你的7zip安装位置，如 C:\Program Files\7-Zip\，一路OK确认，关闭窗口

检查可用性：打开cmd，输入7z命令，查看是否可用

压缩
7z a -t[format] archive_name file_name

参数 a 表示加进压缩包

-t[format] 表示压缩包格式，自己指定，如 -tzip 为 zip 压缩包

archive_name 压缩包名字

file_name 文件名，带扩展名，可以一个一个罗列出来，也可以用通配符，如

*.txt 匹配所有txt文件

*.* 匹配所有文件

举例：将当前目录下所有 docx 文件打包压缩，压缩包名为 archive_name.zip

7z a -tzip archive_name.zip *.docx

解压
参数有两种：一个是e，一个是x

区别：e解压出来的没有文件夹结构，x解压出来的有文件夹结构

一般都用 x

下面说明 x 的语法

7z x -o[output_dir] archive_name

-o[output_dir] 输出文件夹，举例：-otest 表示当前目录下的 test 文件夹下，不写就是当前目录

注意输出 -o 和文件夹名称要连着写，中间没有空格

举例：解压缩archive.zip包到当前目录下的source_file文件夹下

7z x -osource_file archive.zip

查看文件
-l 表示列出所有文件 (英文字母L)

7z l test.zip