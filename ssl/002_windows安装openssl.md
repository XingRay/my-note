# Windows中安装openssl

下载源码编译

https://github.com/openssl/openssl


下载已经编译好的安装包

https://slproweb.com/products/Win32OpenSSL.html


Windows 下OpenSSL安装过程及错误解决办法


Windows下使用OpenSSL有两种方式：
一.直接下载别人编译好的安装包：http://slproweb.com/products/Win32OpenSSL.html
二.自己编译安装：
1.下载并安装perl
http://www.activestate.com/activeperl/downloads/
2.安装与配置：
直接运行安装文件（例如：ActivePerl-5.16.3.1604-MSWin32-x86-298023.msi）即可完成安装；安装过程将自动完成环境变量的配置（安装完成之后，可以在系统环境变量里看到perl的bin目录（例如： C:\Program Files\perl\site\bin;）已经被加入进来），无需再手工配置；
3.测试安装是否成功：
进入perl安装目录的eg文件夹，执行“perl example.pl”若显示“Hello from ActivePerl!”，则说明Perl安装成功。如下图所示：

perl安装成功之后就可以开始使用Perl的相关命令来进行OpenSSL的安装了。
4.openssl可以自己下载源码编译也可以直接下载安装包安装完之后即可使用。
5.使用源码编译openssl
1) 下载openssl源码的路径：
http://www.openssl.org/source/
2）配置VS2012的环境变量（因为后面编译openssl时，将会用到vs2012自带的nmake工具）。
执行VS2012的bin目录下（例如：E:\Visuol Studio 2012\VC\bin）的vcvars32.bat文件即可完成配置，我的因为配置完了什么都不显示了。就不截图了。
3）配置openssl
以下是我在网上找到的配置OpenSSL的方法，这里我上链接吧：https://blog.csdn.net/houjixin/article/details/25806151


注意：
我按照他的方法进行操作，出现两种错误，
1.

tmp32dll\sha1-586.asm(1432) : error A2070:invalid instruction operands
tmp32dll\sha1-586.asm(1576) : error A2070:invalid instruction operands
NMAKE : fatal error U1077: “"E:\Visuol Studio 2012\VC\BIN\cl.EXE"”: 返回代码“0x1”
2.

tmp32dll\sha1-586.asm(1432) : error A2070:invalid instruction operands
tmp32dll\sha1-586.asm(1576) : error A2070:invalid instruction operandsN
MAKE : fatal error U1077: “"E:\Visuol Studio 2012\VC\BIN\cl.EXE"”: 返回代码“0x2”
废了九牛二虎之力才找到了解决的方法，
针对第一种错误的解决方法是：禁用汇编

perl Configure VC-WIN32 no-asm
第二种错误的解决方法为：这个在openssl官方网站上找到了,方法是禁用IPV6
可以参考：http://rt.openssl.org/Ticket/Display.html?id=2097&user=guest&pass=guest

perl Configure VC-WIN32 -DOPENSSL_USE_IPV6=0
最终我改成了：

perl Configure VC-WIN32 -DOPENSSL_USE_IPV6=0 no-asm
到这里很开心啊，刷刷的一直在编译，说明成功啦，duangduangduang，结果出现了：

  link /nologo /subsystem:console /opt:ref /debug /dll /out:out32dll\libeay32.dll /def:ms/LIBEAY32.def @C:\Users\ADMINI~1\AppData\Local\Temp\nmE10C.tmp
   正在创建库 out32dll\libeay32.lib 和对象 out32dll\libeay32.exp
cryptlib.obj : error LNK2019: 无法解析的外部符号 _OPENSSL_ia32_cpuid，该符号在函数 _OPENSSL_cpuid_setup 中被引用
cryptlib.obj : error LNK2001: 无法解析的外部符号 _OPENSSL_ia32cap_P
md5_dgst.obj : error LNK2019: 无法解析的外部符号 _md5_block_asm_data_order，该符号在函数 _MD5_Final 中被引用
sha1dgst.obj : error LNK2019: 无法解析的外部符号 _sha1_block_data_order，该符号在函数 _SHA1_Final 中被引用
sha256.obj : error LNK2019: 无法解析的外部符号 _sha256_block_data_order，该符号在函数 _SHA256_Final 中被引用
sha512.obj : error LNK2019: 无法解析的外部符号 _sha512_block_data_order，该符号在函数 _SHA512_Final 中被引用
out32dll\libeay32.dll : fatal error LNK1120: 6 个无法解析的外部命令
NMAKE : fatal error U1077: “"E:\Visuol Studio 2012\VC\BIN\link.EXE"”: 返回代码“0x460”

这就很蛋疼啊，但是我还是没有放弃，这个问题我觉得不是很难解决。后来我在百度上找到了这样一篇文章：https://blog.csdn.net/mfcing/article/details/43059105，其实跟出现的这个错误并没有什么相关联的，但是我看到了一条这样的代码：

测试动态库：
    nmake -f ms\ntdll.mak test
    测试静态库：
    nmake -f ms\nt.mak test

    安装动态库：
    nmake -f ms\ntdll.mak install
    安装静态库：
    nmake -f ms\nt.mak install
    
    清除上次动态库的编译，以便重新编译：
    nmake -f ms\ntdll.mak clean
    清除上次静态库的编译，以便重新编译：
    nmake -f ms\nt.mak clean
我就在想是不是跟我之前编译过了一次有关系？于是我敲了：

nmake -f ms\ntdll.mak clean
再从头来了一遍

1.perl Configure VC-WIN32 -DOPENSSL_USE_IPV6=0 no-asm
2.ms\do_ms.bat
3.nmake -f ms\ntdll.mak
刷刷刷刷刷刷。。。。。。。。。。等了一分多钟，，，终于可以了！


我又执行了一下nmake -f ms\ntdll.mak test 测试了一下。


说明：本次测试在WIN10 64 VS2012 openssl-1.0.2o 版本测试成功，其他情况并不确保成功。

