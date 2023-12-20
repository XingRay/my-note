# wget爬虫方法

使用wget下载父目录下的整个子目录

```bash
wget -r --level=0 -E --ignore-length -x -k -p -erobots=off -np -N https://youtube.com（网站URL）
```

这条命令会下载远程服务器的整个文件夹到当前文件目录下。



使用wget下载一个目录下的所有文件

```bash
wget -r -np -nH -R index.html https://youtube.com（网站URL）
```

-r：遍历所有子目录
-np：不到上一层子目录去
-nH：不要将文件保存到主机名文件夹
-R index.html：不下载index.html文件





使用wget下载整个网站或特定目录
需要下载某个目录下面的所有文件：

```bash
wget -c -r -np -k -L -p https://youtube.com`（网站URL）
```

在下载时，有用到外部域名的图片或链接，如果需要同时下载就要用-H参数：

```bash
wget -np -nH -r --span-hosts https://youtube.com（网站URL）
```



常见参数

-c：断点续传
-r：递归下载，下载指定网页某一目录下（包括子目录）的所有文件
-nd：递归下载是不创建一层一层的目录，把所有文件下载到当前目录
-np：递归下载时不搜索上层目录
-k：将绝对链接转为相对链接，下载整个站点后脱机浏览网页，最好加上这个参数
-L：递归时不进入其他主机
-p：下载网页所需的所有文件
-A：指定要下载的文件样式列表，多个样式用逗号分隔
-i：后面跟一个文件，文件内指明要下载的URL