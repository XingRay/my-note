# VMware虚拟机安装MacOS系统

Mac OS 13.4.1版本安装：

https://www.bilibili.com/read/cv24720361?spm_id_from=333.999.0.0

一、准备工作

1.安装环境：

2.所需工具：

3.资源下载：

二、安装教程

三、优化

大部分Windows用户都想体验一下简洁易用的MacOS系统，但是如果Windows+MacOS双系统UEFI引导，安装黑苹果，如果没有好用的EFI，安装非常花费时间，那么可以使用VMware体验一把MacOS系统，当让也可以进行办公娱乐等等，只不过稍微有点卡，没有固态双系统体验那么好。

OK！开始干！

# 一、准备工作

因为VMware默认是不支持安装MacOS的，因此我们需要使用解锁工具解锁VMware。

# 1.安装环境：

宿主机Windows 10 20H2 + 虚拟机VMware15.5（小e的安装环境）

Windows和VMware版本可以和今夕德曦不同，VMware安装教程今夕德曦已经分享过，若没有安装VMware，见VMware安装教程！

# 2.所需工具：

VMware解锁工具MK-Unlocker 或 Unlocker_v3.0.3 + MacOS Mojave 10.14懒人包

# 3.资源下载：

**全部资源：**

```
资源包括：   1.解锁工具（MK-Unlocker + Unlocker_v3.0.3）   
2.MacOS镜像懒人包（MacOS Mojave 10.14.6懒人包 + MacOS Catalina10.15.5懒人包 ）   
3.优化卡顿工具（beamoff） 链接：https://pan.baidu.com/s/1UYK4e--BA8512pSD5Xl4qA 提取码：zqrr
```

如果不想下载全部，仅想下载部分资源。

**部分资源：**

**1.解锁工具（MK-Unlocker + Unlocker_v3.0.3）**

```
链接：https://pan.baidu.com/s/1lXzyW2YRui_OJzVGtOWPJA 提取码：txgp
```

**2.MacOS镜像懒人包（MacOS Mojave 10.14.6懒人包 + MacOS Catalina10.15.5懒人包 ）**

```
链接：https://pan.baidu.com/s/1tp-1DIRssL9WMOTmGyDQRw 提取码：9e60
```

**3.优化卡顿工具（beamoff）**

```
链接：https://pan.baidu.com/s/1ceag0nXeBgv-OT_CkriXaQ 提取码：y7vh
```



# 二、安装教程

首先安装VMware，VMware的安装今夕德曦在此就不赘述了，见之前VMware安装教程！

**注意：**

**安装完虚拟机后记得要在BIOS中开启intel VT（虚拟化），否则安装过程中会出错，提示“Intel VT-x处于禁用状态”。**

默认的VMware不支持识别和安装MacOS镜像，需要解锁，解锁前记得关闭杀毒软件以及windefender。

关闭虚拟机，解压解锁工具MK-Unlocker，以管理员身份运行[win-install.cmd]。

运行后会弹出dos命令窗口，等待运行完成，运行完成后会自动关闭窗口。

注：MK-Unlocker文件路径不能出现中文，否则会出现`Can't load frozen modules`的错误。

![img](assets/002_macos_vmware/a885760c7d0329383218da39a47e9daa184abcb7.png@1256w_666h_!web-article-pic.avif)

解锁后打开VMware15.5虚拟机，创建一个新的虚拟机。

勾选[自定义（高级）]，下一步。

![img](assets/002_macos_vmware/7bc135635ee937b493799bbf13358548de0ff707.png@!web-article-pic.avif)

硬件兼容性选择[Workstation 15.x]，下一步。

![img](assets/002_macos_vmware/36ec029c7c641029b4acd203ef02daeb25c170c6.png@!web-article-pic.avif)



如果你下载今夕德曦打包的MacOS懒人包，那么下载的10.14的懒人包后缀为`.iso`，需要把`.iso`后缀改为`.cdr`，懒人包都是用原版镜像制作的。（也可以在网上自行找`.cdr`懒人包，那么忽略此步骤）

重命名，直接把iso后缀改为cdr即可。

![img](assets/002_macos_vmware/93f18b9e4aa073c0bdbcaf0d95318af055ce0dbc.png@1256w_702h_!web-article-pic.avif)

点击[浏览]，选择后缀为`.cdr`的懒人包，注意把右下角选择[所有文件]，选中后[打开]。

![img](assets/002_macos_vmware/ab07ba24bd0f266e91d8e2328e48e0746f6f7fdd.png@1256w_600h_!web-article-pic.avif)

操作系统选择[Apple Mac OS X]，版本选择[macOS 10.14]，此为Vmware虚拟机解锁后的效果，如果前面没有解锁或者解锁失败，此处是没有[Apple Mac OS X]选择项的。

![img](assets/002_macos_vmware/0fbeb68718a2249b7af3a0c1ac4b542cb4521de0.png@!web-article-pic.avif)

修改MacOS的安装位置，建议新建一个专门的文件夹，路径中不要出现中文。

![img](assets/002_macos_vmware/9ff50e58744a72a5ba31d1673c2f909b1ac8e6f9.png@!web-article-pic.avif)

由于今夕德曦笔记本硬件为1个处理器，总共4核，因此给虚拟机设置1处理器和2个核的配置，大家按照自己的电脑实际配置自行设置。（宿主机处理器核配置可以在设备管理器中都能看到）

![img](assets/002_macos_vmware/64acd4bcee7f9a1334a3858400bd56d2c2ee5d58.png@!web-article-pic.avif)



今夕德曦宿主机内存大小为16G，给虚拟机设置4G左右内存大小，按照自己电脑配置自行设置。（内存大小可以直接在任务管理器中查看）

![img](assets/002_macos_vmware/20ca1ca9f0d31cfcf5a5fe0755c2181a4df5f28e.png@!web-article-pic.avif)

网络连接选择[使用网络地址转换]。

![img](assets/002_macos_vmware/a743269b472ad653ee2957240ab9d4a382783041.png@!web-article-pic.avif)

使用默认推荐设置[LSI Logic]。

![img](assets/002_macos_vmware/1ca81e59ef1da381f05932d70ca03c10fe9e7d2b.png@!web-article-pic.avif)

硬盘类型选择推荐设置[SATA]。

![img](assets/002_macos_vmware/fc39a74febeed90d65dc4cfb564a721baa9d9660.png@!web-article-pic.avif)

在虚拟机中安装系统需要创建虚拟磁盘用来安装操作系统，勾选[创建新虚拟磁盘]。

![img](assets/002_macos_vmware/9feb6fe7e8767d60de1c8eb10d77c75dadd17a50.png@!web-article-pic.avif)

虚拟磁盘大小自行设置，小e这里设置100G大小，其他默认。

![img](assets/002_macos_vmware/3518c65415b16b386093fb5f7926c601699aa2fc.png@!web-article-pic.avif)

配置完成，点击[完成]，配置好后先不要启动MacOS系统。

![img](assets/002_macos_vmware/77f1a9a2c08c30c563b086f991e18f83c84e6bad.png@!web-article-pic.avif)

找到MacOS的安装位置（上面步骤中已自行设置），使用记事本打开后缀为`.vmx`的[macOS 10.14.vmx]的文件。

![img](assets/002_macos_vmware/f1884b9ed8212515022daae2d5708122e71cb603.png@1256w_656h_!web-article-pic.avif)

在最后添加以下代码

```
smc.version = 0
```

保存退出。

![img](assets/002_macos_vmware/50c464cac3892a51d2c88183d747a8851a18d26b.png@1256w_660h_!web-article-pic.avif)

这时候[开启虚拟机]，启动MacOS系统

![img](assets/002_macos_vmware/ca8b75cd465537adf4b8fec4a052afb1b6fce81f.png@1256w_672h_!web-article-pic.avif)

启动MacOS界面。

选择语言[简体中文]。

![img](assets/002_macos_vmware/02a94a87d04d4ee87be5539ddf6e78d0ffe64850.png@1256w_946h_!web-article-pic.avif)

点击[磁盘工具]，如果没有此界面，可以在[实用工具]下找到。

![img](assets/002_macos_vmware/9e5f511caee523e0166a2059a1f5dce6c5959aa2.png@1256w_930h_!web-article-pic.avif)

选择刚才新建的虚拟磁盘，因为MacOS和Windows磁盘大小的计算方式不一样，所以刚才设置的100G大小的虚拟磁盘在Mac中显示并不是100G，但是相差不大，选择近似的即可。

虚拟磁盘还好，不会对宿主机本地磁盘有影响，但是在黑苹果双系统安装中一定要仔细分辨出哪个是Windows的安装分区，哪个是MacOS安装分区，否则一旦抹掉，数据就会全部丢失，具体今夕德曦在双系统安装黑苹果教程中再谈！

选择正确磁盘后点击[抹掉]，相当于格式化磁盘。

![img](assets/002_macos_vmware/8cd90b98641eaed8b955c3e998f66632ca4e4938.png@1256w_936h_!web-article-pic.avif)

名称自行设置，[格式]设置为[Mac OS扩展（日志式）]，方案为[GUID分区图]，点击[抹掉]。

![img](assets/002_macos_vmware/7533e8f782397abead40ccc368aed258ed75e3c1.png@1256w_944h_!web-article-pic.avif)

如果想对这块虚拟磁盘分区的话也可以点击分区，进行分区，小e在此使用一个分区为例，不再分区了。

![img](assets/002_macos_vmware/dfd778067017dddc21544e8ed2607ac9c5b0ca56.png@1256w_942h_!web-article-pic.avif)

磁盘抹掉后关闭磁盘工具，点击[安装mac OS]。

![img](assets/002_macos_vmware/2d730f1f070e8053e237fcae05964c6367ddf748.png@1256w_924h_!web-article-pic.avif)

点击继续。

![img](assets/002_macos_vmware/6b4643d52d89b1abe6ae6086cc054e1d73332315.png@1256w_944h_!web-article-pic.avif)

同意。

![img](assets/002_macos_vmware/63ed3818cbed7caaaf4886520b608f9b74f22eb1.png@1256w_936h_!web-article-pic.avif)

选择刚才抹掉的磁盘来安装系统，磁盘名称即为刚才抹盘时设置的。

![img](assets/002_macos_vmware/2f63ced21086a47b0bfd80a491462fd8a8d59930.png@1256w_950h_!web-article-pic.avif)

耐心等待。

![img](assets/002_macos_vmware/ff22af28ba08fc9f5d0317d2ac125131da13e848.png@1256w_934h_!web-article-pic.avif)

耐心等待！设置区域[中国大陆]。

![img](assets/002_macos_vmware/c2c4e496ca0402f52c617e25ac633d171d5dab72.png@1256w_968h_!web-article-pic.avif)

键盘选择[ABC]，至于简体中文后面进入系统后可以自己添加。

![img](assets/002_macos_vmware/52748a048febc99fa3094ac65ebbfa35a6b3c554.png@1256w_948h_!web-article-pic.avif)

继续。

![img](assets/002_macos_vmware/bafd9065f43cf0f78864a2e9df1e8001d051d34a.png@1256w_932h_!web-article-pic.avif)

勾选[现在不传输任何信息]，没有进系统前能不设置就不设置。

![img](assets/002_macos_vmware/198401226e9624446298ced11e94fb453cb7ded2.png@1256w_940h_!web-article-pic.avif)

稍后设置，跳过。

![img](assets/002_macos_vmware/5af32d347bde02512737b4fa06226696ff3d99df.png@1256w_932h_!web-article-pic.avif)

同意。

![img](assets/002_macos_vmware/c7f0fc0be3ac16ac825c06a5a0867733ecfb8334.png@1256w_948h_!web-article-pic.avif)

创建账户和设置密码，自行设置。

![img](assets/002_macos_vmware/fbec57721e29fb3f2836544570e2b9ce12dbf23e.png@1256w_960h_!web-article-pic.avif)

继续。

![img](assets/002_macos_vmware/1b02e679c56babadfb647b427852e818a2104a8a.png@1256w_942h_!web-article-pic.avif)

选择外观，自行选择。

![img](assets/002_macos_vmware/9eebebcbbc067640d78e2807e7e0abbe2ba2e936.png@1256w_948h_!web-article-pic.avif)

进入系统，可以看到系统界面很小，VMware虚拟机需要安装`VMware Tools`才能全屏。

![img](assets/002_macos_vmware/498a8e9c53daaee2a45769e3a95abd7bfc20a1a0.png@1256w_710h_!web-article-pic.avif)

安装`VMware Tools`前右键先推出安装程序`install macOS Mojave`。

![img](assets/002_macos_vmware/0731d593fbb36e2fdb81033a970eb37b43fe295a.png@1256w_942h_!web-article-pic.avif)

点击VMware上方选项卡[虚拟机]->[安装VMware Tools]，出现如下界面，双击安装VMware Tools。

![img](assets/002_macos_vmware/60b35b580469c75a29b105a36698b3160a38e719.png@1256w_944h_!web-article-pic.avif)

Mac上安装软件不需要像Windows那样麻烦，因为Mac的程序管理非常方便，直接安装即可，安装成功后重新启动。

![img](assets/002_macos_vmware/bcc534a0954ce75a0113a73e63596c0de854d98b.png@1256w_944h_!web-article-pic.avif)

重新启动后就可以看到铺满了，如果想要全屏的话在VMware选项卡[查看]中全屏即可。

OK！MacOS在虚拟机上的安装就到此结束了。

# 三、优化

安装成功后重启Mac系统，你会发现启动后很卡，重启后完全加载出桌面可能好久，而且在日常使用Mac虚拟机时，可能你会感到有点卡，比如你已经右键鼠标了，但是过了一两秒，Mac才弹出右键菜单。

这是因为VMware不支持给MacOS图形加速，像Windows和MacOS这种大型GUI桌面系统，没有3D图形加速要想流畅确实不太可能。

再者看到虚拟机给MacOS的图形显存只有128M，实在是太小了。综上，虚拟机对MacOS的优化支持实在有点差。

根本原因还是macOS系统只被允许在苹果的硬件设备上运行，在非苹果设备上公开支持macOS肯定是违规的，也就没有厂商愿意冒着风险开发显卡优化程序。

![img](assets/002_macos_vmware/58e8f54a3e1afad53f63db1ca3ee2bea6ec1db16.png@1256w_680h_!web-article-pic.avif)

不过小的优化还是有的，有一款MacOS虚拟机优化软件`beamoff`，GitHub项目地址：`https://github.com/JasF/beamoff`。

`beamoff`是VM上Mac虚拟机的优化神器，下载链接今夕德曦都打包了，见`资源下载`。

下载后在宿主机解压，因为已经安装了VM Tools，因此直接从宿主机拖动到Mac虚拟机桌面。（如果拖动中mac出现隐私安全弹窗，按照提示设置即可）

然后从桌面拖动到应用程序（打开访达即可看到）即可安装。

![img](assets/002_macos_vmware/aa48ea7451faf7f81ed6d4115ec5856107f566d7.png@1256w_546h_!web-article-pic.avif)

安装后要设置为每次开机自启动，在设置->用户与群组，点击你的账户，点击右侧登录项，点击+号添加`beamoff`应用程序即可。

然后重启你就会感到开机加载桌面没有以前那么慢了，很明显。在日常使用时延迟也没有之前那么大了。

OK！长篇大文，两千多字，码字好几个小时，终于完成了，以后今夕德曦有时间还会分享Mac系统的新手配置教程，敬请期待！

分享不易，希望大家多多支持今夕德曦！

![img](assets/002_macos_vmware/6a6a6b1fa4ffd50cf0e9342f70bda3d23ec10360.png@1256w_926h_!web-article-pic.avif)



本文为我原创