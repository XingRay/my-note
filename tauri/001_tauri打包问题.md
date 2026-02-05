# tauri打包问题

发布于 2023-12-27 09:15:29

4860

举报

文章被收录于专栏：[ghostsf](https://cloud.tencent.com/developer/column/95953)

### 1. wixtoolset下载问题

>  Info Verifying wix package Downloading [https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip](https://cloud.tencent.com/developer/tools/blog-entry?target=https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip&source=article&objectId=2374909)    Error failed to bundle project: `Io Error: 由于连接方在一段时间后没有正确答复或连接的主机没有反应，连接尝试失败。 (os error 10060)` error Command failed with exit code 1.

解决办法：

先把这个文件下载下来 [https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip](https://cloud.tencent.com/developer/tools/blog-entry?target=https://github.com/wixtoolset/wix3/releases/download/wix3112rtm/wix311-binaries.zip&source=article&objectId=2374909)

在缓存目录下新建 tauri/WixTools 目录，放在wixtools目录下解压，再执行即可 平台	目录	例子 Linux	$HOME/.cache	/home/用户名/.cache macOS	$HOME/Library/Caches	/Users/用户名/Library/Caches Windows	{FOLDERID_LocalAppData}	C:\Users\用户名\AppData\Local

当然也可以设置代理处理

### 2.  nsis 下载和处理失败

nsis 命令行下载失败 。先用迅雷或者其他方法下载下来 [https://ghproxy.com/https://github.com/tauri-apps/binary-releases/releases/download/nsis-3/nsis-3.zip](https://cloud.tencent.com/developer/tools/blog-entry?target=https://ghproxy.com/https://github.com/tauri-apps/binary-releases/releases/download/nsis-3/nsis-3.zip&source=article&objectId=2374909) 在上面wixtools的目录新建 NSIS，也即tauri/NSIS

然后将nsis-3.zip 解压到nsis目录 [https://github.com/tauri-apps/binary-releases/releases/download/nsis-plugins-v0/NSIS-ApplicationID.zip](https://cloud.tencent.com/developer/tools/blog-entry?target=https://github.com/tauri-apps/binary-releases/releases/download/nsis-plugins-v0/NSIS-ApplicationID.zip&source=article&objectId=2374909) 下载后 解压到 nsis的plugins目录下

然后将releaseunicode 下的applicationid.dll 和nsis_tauri_utils.dll 两个文件复制到 x86-unicode 目录

nsis_tauri_utils.dll下载地址 [nsis_tauri_utils.dll](https://cloud.tencent.com/developer/tools/blog-entry?target=https://knsay.com/upload/default/20230614/b0c63b57f4729e1d3a245023672df79a.zip&source=article&objectId=2374909)



nsis 相关文件这里已经打包好了

 [tauri.zip](assets\tauri.zip) 