Linux下配置环境变量最常用的两种方法—— .bashrc 和 /etc/profile

小异常

于 2018-04-11 20:22:19 发布

32302
 收藏 91
分类专栏： linux 文章标签： linux
版权

linux
专栏收录该内容
8 篇文章0 订阅
订阅专栏
版权声明：本文为 小异常 原创文章，非商用自由转载-保持署名-注明出处，谢谢！
本文网址：https://blog.csdn.net/sun8112133/article/details/79901527

　　首先简单说一下什么是环境变量？环境变量简单的说就是当前环境下的参数或者变量。如果说的专业一点就是指在操作系统中用来指定操作系统的一些参数。
　　
　　举个我们最常见的环境变量 —— PATH，它的用途就是当用户要求系统运行一个程序而没有告诉它程序所在的完整路径时，系统除了在当前目录下寻找此程序外，还要到PATH变量中指定的路径去寻找。用户可以通过设置PATH变量，来更好的运行进程。举个常见的例子，在Windows系统中，当我们将JDK安装好后，如果直接在命令提示行（cmd）中输入 java 或 javac 相关的命令时，它会提示：“java不是内部或外部命令，也不是可运行的程序或批处理文件。”这时候我们就需要将JDK中bin目录的路径加入到PATH变量中去。
　　
　　好了，说了这么多，相信大家对环境变量及PATH变量有了一个大体的了解，下面开始进入主题。
　　

在Linux系统下配置环境变量最常用的两种方法：
　　1、修改家目录下的 .bashrc 文件
　　2、修改 /etc/profile 文件
　　（注：最后我们会对比两种方法的利弊，大家可以根据自己的情况选用最适合的方法。）




1、修改家目录下的 .bashrc 文件
　　.bashrc 文件主要保存着个人的一些个性化设置，如：命令别名、环境变量等。

　　1）先切换回家目录
cd ~
1
　　2）修改 .bashrc 文件
vi .bashrc
1
　　3）在 .bashrc 文件的 最后 加入环境变量
变量名=变量值
...=...
export 变量名 ...
如：JAVA_HOME=/opt/jdk1.8.0_91
　　CLASSPATH=.:./bin
　　PATH=$JAVA_HOME/bin:$PATH
　　export JAVA_HOME CLASSPATH PATH
1
2
3
4
5
6
7
　　4）退出并保存
:wq
1
　　5）立即生效（此时应该在家目录下）
source .bashrc
1
　　注：如果不执行 source 命令，则需重启系统才能生效

2、修改 /etc/profile 文件
　　/etc/profile 文件是系统为每个用户设置的环境信息，当用户第一次登录时，该文件被执行。此文件的改变会涉及到系统的环境，也就是有关Linux环境变量的东西。

　　1）修改 /etc/profile 文件
sudo vi /etc/profile
1
　　2）在 /etc/profile 文件的 最后 加入环境变量
变量名=变量值
...=...
export 变量名 ...
如：JAVA_HOME=/opt/jdk1.8.0_91
　　CLASSPATH=.:./bin
　　PATH=$JAVA_HOME/bin:$PATH
　　export JAVA_HOME CLASSPATH PATH
1
2
3
4
5
6
7
　　3）退出并保存
:wq
1
　　4）立即生效
source /etc/profile
1
　　注：如果不执行 source 命令，则需重启系统才能生效

3、对比两种方法
　　修改.bashrc文件，它可以把使用这些环境变量的权限控制到用户级别，只是针对某一个特定的用户。而修改 /etc/profile 文件，它是针对于所有的用户，使所有用户都有权使用这些环境变量。
　　相比较起来，第一种方法更加安全，因为如果采用第二种方法，它可能会给系统带来安全性的问题。
　　建议：如果你的计算机仅仅作为开发使用，则推荐第二种方法，否则最好使用 第一种方法。
————————————————
版权声明：本文为CSDN博主「小异常」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/sun8112133/article/details/79901527







# ~/.bashrc-Linux环境变量配置超详细教程



### 文章目录

- [~/.bashrc-Linux环境变量配置超详细教程](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#bashrcLinux_0?login=from_csdn)
- - [前言：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#_2?login=from_csdn)
  - [参考链接：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#_30?login=from_csdn)
  - [1.最简单的环境变量配置全流程，新手向：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#1_37?login=from_csdn)
  - - [1.1 Linux读取环境变量的两种方法](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#11_Linux_54?login=from_csdn)
  - [2.为同一个环境变量添加多个路径](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#2_83?login=from_csdn)
  - - [2.1一行式冒号:添加多个路径](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#21_101?login=from_csdn)
    - [2.2$PATHNAME实现增量式添加](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#22PATHNAME_117?login=from_csdn)
    - - [2.2.1利用$PATH在后面!增加!新的路径](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#221PATH_122?login=from_csdn)
      - [2.2.2利用$PATH在前面!增加!新的路径](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#222PATH_135?login=from_csdn)
  - [3.大括号，单引号，双引号的作用：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#3_148?login=from_csdn)
  - - [不加引号的效果：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#_153?login=from_csdn)
    - [双引号的效果：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#_165?login=from_csdn)
    - [单引号的效果：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#_178?login=from_csdn)
  - [总结：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#_193?login=from_csdn)
  - [联系方式：](https://huaweicloud.csdn.net/63560e35d3efff3090b59115.html?spm=1001.2101.3001.6650.1&utm_medium=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~CTRLIST~activity-1-115130945-blog-79901527.pc_relevant_3mothn_strategy_and_data_recovery&utm_relevant_index=2#_198?login=from_csdn)



## 前言：

今天本来是想将代码调通的，但是一直受限于mujoco_py的渲染问题，前段时间发现两台Ubuntu18.04的机子可以offscreen渲染，另外三台Ubuntu16.04的机子无法offscreen渲染。
我以为我找到规律了，但是问题可能没有这么简单。
在解决这个问题的过程中，发现了mujoco_py的issues上有这样一个回复：
https://github.com/openai/mujoco-py/issues/408#issuecomment-735674851
提到这句话:

> A non-working machine will typically include dependencies like:
> /usr/lib/x86_64-linux-gnu/libGLEW.so (0x00007fa44cf82000)

离谱，我几乎所有的机子上都加了这个路径；
但是我的Ubuntu16的主机，加了太乱了，我也不知道那些句子使得路径生效，因此就百度搜索了好多~/.bashrc的环境变量配置问题，并没有找到合适的教程。
本篇博客将带着大家看懂下面的几句话的意思：

```routeros
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/us/.mujoco/mujoco200/bin:/usr/lib/nvidia-460:${CUDA_HOME}/lib64
export LD_PRELOAD=/usr/lib/x86_64-linux-gnu/libGLEW.so
export PATH="/home/us/Downloads/pycharm-2020.1/bin:$PATH"
export PATH="/home/us/Downloads/pycharm-2018.1/bin"
export CUDA_HOME=/usr/local/cuda-11.2
```

首先我们知道这个export是添加路径的意思，空格后面是环境变量名，有上面有四个不同的环境名，还有一个变量名被导入两次算是什么意思？
这个$PATHNAME是什么意思？为什么有不同的位置？
这个冒号:是什么意思？
这个大括号{}有什么作用？
如何验证我们的路径是否导入正确？
花里胡哨的操作，很难直接百度到想要的答案，我没系统的看过Linux的基础书记，总之这篇文章就都列出来做个分享~

## 参考链接：

1. [超详干货！Linux 环境变量配置全攻略](https://zhuanlan.zhihu.com/p/317282094?login=from_csdn)
2. [Linux环境变量及配置相关命令](https://zhuanlan.zhihu.com/p/348197061?login=from_csdn)
3. [修改Ubuntu的环境变量$PATH](https://blog.csdn.net/fisher_jiang/article/details/4193144?login=from_csdn)
4. [环境变量$PATH:](https://zhuanlan.zhihu.com/p/258881197?login=from_csdn)

## 1.最简单的环境变量配置全流程，新手向：

1.利用vim进入~/.bashrc文档，摁住ctrl+g，直接跳到最后一行，摁一下i键，进入插入模式，现在可以编辑文档了。
2.跳到最后一个字符，摁一下换行，可以在下一行输入命令。
3.一般来说在Ubuntu的终端中，ctrl+shift+c是复制，Ctrl+shift+v是粘贴；
4.我们输入第一个环境变量配置：

```routeros
export DEMOPATH="/xxx/xxx/xx1x/"
```

5.路径添加好了，该关闭~/.bashrc了，摁一下esc键，退出插入模式；
6.输入冒号:wq，关闭bashrc；
7.这时候还没有生效!需要source一下，[source命令的含义的博客](https://blog.csdn.net/gui951753/article/details/79166407?login=from_csdn)：

```bash
source ~/.bashrc
```

8.刚才配置的环境终于在这个终端生效了！，我们验证一下该环境变量到底有没有我们加的东西，我们有两种方法：

### 1.1 Linux读取环境变量的两种方法

读取环境变量的方法：

- `export`命令显示当前系统定义的所有环境变量
- `echo $PATH`命令输出当前的PATH环境变量的值
- `$PATH`直接调取当前PATH的值。
  使用中间的这个，终端输入：

```bash
echo $DEMOPATH
```

显示结果：

```awk
/xxx/xxx/xx1x/
```

我们使用后者，在终端输入：

```gams
$DEMOPATH
```

显示了：

```gradle
bash: /xxx/xxx/xx1x/: No such file or directory
```

虽然我们的这个路径没有显示的文件夹，但是变量名和路径是整上去了。
现在我们开整第二个功能：

## 2.为同一个环境变量添加多个路径

我们在python中经常会遇到一个环境变量，需要加多个路径，但如何添加多个路径呢？
直观上用个分号;行不行？不行！
我们需要用冒号:来分开！离谱！
好的我们现在在~/.bashrc中再加一句路径，变成了下面的样子：

```routeros
export DEMOPATH="/xxx/xxx/xx1x/"
export DEMOPATH="/xxx/xxx/xx2x/"
```

生效后，我们检验一下效果：

```gradle
bash: /xxx/xxx/xx2x/: No such file or directory
```

嗯？我的路径1 呢？
原来是被覆盖掉了！
下面我展示几种同一个环境变量添加多个路径的方法：

### 2.1一行式冒号:添加多个路径

上面我们知道，划分多个路径，通过冒号。
那么我们将同一个环境变量的路径，写到一行，路径之间用冒号隔开：

```routeros
export DEMOPATH="/xxx/xxx/xx1x/":"/xxx/xxx/xx2x/"
```

显示效果如下：

```gradle
bash: /xxx/xxx/xx1x/:/xxx/xxx/xx2x/: No such file or directory
```

终于成功了！
但是这个不优雅，一句话写太长了，中间错了都不知道错哪儿了。
因此我们需要一个增量式的路径添加：

### 2.2$PATHNAME实现增量式添加

上面我们用了好几次这个美元符 ， 这 个 符 号 是 什 么 意 思 呢 ？ 直 观 的 理 解 就 是 ， 直 接 调 取 当 前 环 境 变 量 的 值 。 因 此 我 们 可 以 用 这 个 ，这个符号是什么意思呢？ 直观的理解就是，直接调取当前环境变量的值。 因此我们可以用这个 ，这个符号是什么意思呢？直观的理解就是，直接调取当前环境变量的值。因此我们可以用这个PATH，来代替原本环境变量的路径，要是加东西的话，在前面或者后面加一个分号就行了~

#### 2.2.1利用$PATH在后面!增加!新的路径

```routeros
export DEMOPATH="/xxx/xxx/xx1x/"
export DEMOPATH="$DEMOPATH:/xxx/xxx/xx2x/"
```

打印结果如下：

```awk
/xxx/xxx/xx1x/:/xxx/xxx/xx2x/
```

确实在后面，效果是先调用之前的1，再在后面加上2；

#### 2.2.2利用$PATH在前面!增加!新的路径

```routeros
export DEMOPATH="/xxx/xxx/xx1x/"
export DEMOPATH="/xxx/xxx/xx2x/:$DEMOPATH"
```

打印结果如下：

```awk
/xxx/xxx/xx2x/:/xxx/xxx/xx1x/
```

效果就不用描述了~

## 3.大括号，单引号，双引号的作用：

- 大括号{}用在变量名的身上，用处目前没看出来有没有明显的区别；
- 路径可以直接写，也可以加双引号，但是一定不能加单引号！

### 不加引号的效果：

输入：

```awk
export DEMOPATH=/xxx/xxx/xx1x/
export DEMOPATH=/xxx/xxx/xx2x/:$DEMOPATH
```

输出

```awk
$ echo $DEMOPATH
/xxx/xxx/xx2x/:/xxx/xxx/xx1x/
```

### 双引号的效果：

输入：

```routeros
export DEMOPATH="/xxx/xxx/xx1x/"
export DEMOPATH="/xxx/xxx/xx2x/:$DEMOPATH"
```

输出

```awk
/xxx/xxx/xx2x/:/xxx/xxx/xx1x/
```

加不加我没看出来，如果有什么隐含的区别，请大家告知！

### 单引号的效果：

输入：

```routeros
export DEMOPATH='/xxx/xxx/xx1x/'
export DEMOPATH='/xxx/xxx/xx2x/:$DEMOPATH'
```

输出：

```awk
/xxx/xxx/xx2x/:$DEMOPATH
```

这时候就没有转义成功！