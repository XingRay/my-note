# anaconda3安装教程及更改默认环境保存路径


官网链接：https://www.anaconda.com/
要是嫌官网下太慢，镜像链接：https://mirrors.tuna.tsinghua.edu.cn/anaconda/archive/

安装步骤


我为了方便和磁盘空间，就把软件装在D盘了

我没自动添加过环境变量，所以还是之勾选了下面的python环境


安装ing


手动配置环境变量：我的电脑右键->属性->高级系统设置->环境变量

输入这三条，D:\anaconda的部分是自己安装anaconda的目录
D:\anaconda
D:\anaconda\Scripts
D:\anaconda\Library\bin

弄好记得按确定

安装完成
接下来按win+R打开命令管理器，输入cmd
输入：conda --version


输入：conda info

输入：activate，再输入python

显示如上界面说明安装完成

换源操作（可以加快安装第三方库的速度）：
打开Anaconda prompt（在软件里面找），复制如下指令

conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free/
conda config --add channels https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/
conda config --set show_channel_urls yes

查看是否更换好通道：

conda config --show channels


完成。

更改新建环境保存路径
走了很多弯路，弄了很久终于成功。
先是跟着网上大部分教程更改了.condarc的配置（文件在C盘-用户-自己设置的用户名-划到底下会有一个）

我打开长这样，红色部分是新添加的指令

envs_dirs:
  - D://anaconda//envs

设置完进anaconda prompt，输入“conda info”，查看环境和库的保存路径

我这提示的已经更改成D盘了，但是输入如下指令后，（下面展示的是改好后的图），输入完就还是在C盘的保存路径下，接下来更改文件夹的权限

conda create -n test python=3.7

找到anaconda的目录，右键属性-安全，把user的权限全开了

我这时候会报错，提示，但是当时我关掉后看权限是全√的就没管它了，真是大错特错

点了取消后，点开，上图中的高级选项，查看所有者是不是自己要的，然后我是点开了主体的三个看权限，然后点击“启用继承”，它就开始检查文件安全性，然后确认后返回上一界面。![在这里插入图片描述](https://img-blog.csdnimg.cn/7f65aa405ed847fb966430cb7f3daa1b.png
点击如下框框后，又开始检查安全性，然后就可以正常确认。最后再创建新的环境，就在D盘下了。


## 用配置文件修改conda环境和缓存包默认的存储路径

技术标签： [python](https://codeleading.com/tag/python/) [anaconda](https://codeleading.com/tag/anaconda/)

查看当前conda的信息：
xxxx 是用户名

```yaml
(TF2.1) C:\Users\xxxx>conda info

     active environment : TF2.1
    active env location : d:\ProgramData\Anaconda3\envs\TF2.1
            shell level : 3
       user config file : C:\Users\xxxx\.condarc
 populated config files : C:\Users\xxxx\.condarc
          conda version : 4.8.3
    conda-build version : 3.18.11
         python version : 3.7.6.final.0
       virtual packages : __cuda=10.1
       base environment : d:\ProgramData\Anaconda3  (writable)
           channel URLs : https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/msys2/win-64
                          https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/msys2/noarch
                          https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/conda-forge/win-64
                          https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/conda-forge/noarch
                          https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/win-64
                          https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/noarch
                          https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free/win-64
                          https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free/noarch
```java
          package cache : d:\ProgramData\Anaconda3\pkgs
                          C:\Users\xxxx\.conda\pkgs
                          C:\Users\xxxx\AppData\Local\conda\conda\pkgs
       envs directories : d:\ProgramData\Anaconda3\envs
                          C:\Users\xxxx\.conda\envs
                          C:\Users\xxxx\AppData\Local\conda\conda\envs
               platform : win-64
             user-agent : conda/4.8.3 requests/2.22.0 CPython/3.7.6 Windows/10 Windows/10.0.18362
          administrator : False
             netrc file : None
           offline mode : False

1234567891011121314151617181920212223242526272829303132
```

```
window下配置文件在C:\Users\xxxx目录下.condarc，保证不同用户之间的配置文件不同

**不同环境和缓存可以存放不同位置，可以调整其前后顺序，默认的位置为第一个
多个环境分散存储在不同磁盘，解决磁盘扎堆，空间不足的压力。**

```yaml
channels:
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/msys2/
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/cloud/conda-forge
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free
show_channel_urls: true
envs_dirs:
  - d:\ProgramData\Anaconda3\envs
  - C:\Users\xxxx\.conda\envs
  - C:\Users\xxxx\AppData\Local\conda\conda\envs                   
pkgs_dirs:
  - d:\ProgramData\Anaconda3\pkgs
  - C:\Users\xxxx\.conda\pkgs
  - C:\Users\xxxx\AppData\Local\conda\conda\pkgs
```


```
channels:
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/main/
  - https://mirrors.tuna.tsinghua.edu.cn/anaconda/pkgs/free/
  - defaults
show_channel_urls: true

envs_dirs:
  - D:\develop\python\conda\env
pkgs_dirs:
  - D:\develop\python\conda\pkgs
```

