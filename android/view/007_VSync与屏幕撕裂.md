![VSync与屏幕撕裂](D:\my-note\android\view\assets\v2-97c49ecf1d7ee7a0468965a10402a522_1440w.png)

# VSync与屏幕撕裂

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l-1707456672656-252.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a-1707456672656-253.jpg)

同济大学 计算机系统结构硕士

关注

66 人赞同了该文章



在购买电竞显示器时，除了分辨率，刷新率，色彩覆盖率，灰度响应时间等，还有一个参数通常也会被提及，那就是**FreeSync**。

![img](D:\my-note\android\view\assets\v2-33d572e291b1706c068ba1009f674c49_1440w.webp)

但是在Android手机上，我们却很少听到这个概念，那么手机上支不支持**FreeSync**类似功能呢？

> 在正式开始之前，我们先介绍一下**VSync引入的前因后果**，此为上篇。
> 中篇介绍**如何解决触控时延**；
> 下篇介绍PC上**的FreeSync，GSync，FastSync，以及Android手机上的QSync等机制**。



通常我们说一个游戏跑起来很顺畅，从显示角度上讲——**帧率高**（屏幕刷新率要高），**不拖影**（灰阶响应时间快），**不卡顿**（不丢帧，对于显示来说不撕裂），**不粘滞**（触控响应快）；

刷新率已经耳熟能详，自不必说。

灰阶响应时间是指RGB颜色切换时间，如果切换慢，则会有拖影，这是LCD（LED）上存在的问题。目前旗舰手机上普遍采用OLED屏幕，由于是自发光，不需要背光等模块参与，响应时间已经可以忽略了。（**VR对于响应时间要求跟高，屏幕也都采用oled，如今采用了fastlcd，未来趋势还是micro led**）

卡顿取决于很多因素，主要和性能有关，不在本文讨论范围以内。本文主要介绍***如何解决显示相关的撕裂问题以及触控延时\***。

## **何为撕裂？**

![img](D:\my-note\android\view\assets\v2-c3b3c747424f95ed4dc0ac679d4536a4_1440w.webp)

如上，这张图我们应该看过很多遍了，撕裂就是一张图在屏幕上明显表现为上下两截，不是完整的一帧。但其实这个概念应该存在更好的定义，下面我会讲解原因。

**撕裂概念的澄清**

我们需要将“撕裂”加入一个前提——**撕裂不是源于屏幕本身，而是源于数据传输。**也就是说MIPI（LCD）读取framebuffer时，数据已经是不完整的了，通常来说**内容被覆写**了。尽管这已经是约定俗成的，网上提到的撕裂也都是指代此意，但我们还是有必要澄清一下，它们事实上隐藏了前提。

因为如果不加人这个前提，屏幕永远都是撕裂的，这源于屏幕的刷新机制。

**逐行扫描**

无论是CRT时代，还是现在LCD或OLED，显示器刷新的原理一直没变，当它刷新一帧的时候并不是一次性把整个画面全部刷新出来，而是同一时刻只处理一个点，从上到下一行一行逐渐把图片绘制出来的。因为人眼的视觉暂停的，如果在短时间内处理完所有的点，就仿佛看到的同一帧画面。

![img](D:\my-note\android\view\assets\v2-30a7d6317f69503bdaac033bac338716_1440w.webp)

这张图不够动感，可以看下面这张动图（图片来自啃芝士视频，忽略图右的隔行扫描，目前和CRT一样基本不复存在了）

![img](D:\my-note\android\view\assets\v2-a53a83b8e4b9af58fc0404d12e3319a9_1440w.webp)

但这张图也存在一定的问题，甚至有点误导的意思。因为它不能回答我曾经的疑惑——当屏幕上半部分一行行扫描时，**屏幕下半部分显示的是什么**？（这是显示领域一个非常重要的问题！）

这张图展示的是背景色。但其实是残留上一帧的内容。通过高速摄像机的动图如下：

![img](D:\my-note\android\view\assets\v2-d41ab261ca9385be349fb1759d6a7c1f_1440w.webp)

**如果我们的眼睛能像相机如此敏感，那么展现在我们眼中的都是撕裂的图片**。事实上，因为视觉暂停，我们觉察不到这样的变化。

**(注意：并不是所有设备上都是如此，残留上一帧内容是手机上的通用做法，但放在VR上并不适用，VR是没有残留上一帧，就是黑色，这样视觉停留影响会小)**

如果使用微距镜头近距离的观看屏幕，则是这样（通过RGB色彩与背光调节显示内容）：

![img](D:\my-note\android\view\assets\v2-b11064590d63e37a2faf955c96ae117a_1440w.webp)

**你可能会问，我似乎仍没见过这种撕裂？**

> 如上所述，“快就有理”——**如果帧率足够高，即便图像有撕裂，你也感受不到**——这正是PC上如果一个游戏稳定运行有140帧以上，事实上**存在撕裂**，你也感受不到的原因（PC游戏默认不开Vsync）。
> **如果你能看到如此明显的撕裂，只能说明当前帧率极其低了**。你通常不太会看到这样的撕裂。你能感受到的只是不顺滑，像上面这张图所展示的通常是快速截取一帧所呈现出来的现象。

**撕裂为何会导致卡顿？**

> 硬件茶谈中有个形象的比喻：比如1s中屏幕本应该刷新3张画面，但你在屏幕上只看到2.5张画面，那么帧率就从3fps调到了2.5fps，相当于帧率下降了，自然会卡顿。

## 如何解决屏幕撕裂？

通常我们指的屏幕撕裂，可分为两种情况解释：

其一，PC上，我们的显示系统默认都是采用的双Buffer机制，通过SwapBuffer切换前/后缓冲。

**如果采用单buffer，会出现什么场景呢？**

我们看过很多文章，回答基本是这样的——当这个buffer在屏幕上显示时，GPU绘制也在同时发生，撕裂将无可避免。

**但事实并非如此绝对，单Buffer机制并不必然会导致屏幕撕裂**。试想下这样一个场景：显示器在刷新屏幕上半部分的时候，GPU绘制下半部分就可以避免撕裂，这也称为Strip渲染。

Strip rendering 策略如下：

> **Strip rendering的想法就是，改变还没有显示出的那部分buffer，**有2种不同的策略可供选择。
> **1. Beam Racing**
> 改变下一步将显示的屏幕的那部分，**因为我们想要跑在扫描线前更新还未被显示的那部分内存**。
> **2. Beam Chasing：在扫描线后更新画面。**
> **对于优化延迟这个目的来说，beam racing策略会更好，但是同时也更难实现。**GPU需要保证在很紧的时间窗口完成渲染。这非常难保证，尤其是在一个多线程的操作系统中，所有运作都发生在后台，同时，GPU可能会渲染到其他buffer的区域中。所以Beam Chasing会更容易实现。
> VR 应用取得显示的上一个Vsync 的时间，来调整自己以保证自己有渲染整个画面的时间。为了计算开始渲染的时间。我们需要条的尺寸。strip的尺寸，和使用多少strip，在执行时被定义。这取决于画面需要多久被显示，和我们需要多久去渲染这个画面。在多数情况下，2条是最合适的，当然也可以更多。

VR都是使用了2条strip，左右眼各一个。而这就是VR的默认刷新方式（左右眼buffer其实充当了一部分SwapBuffer的功能，**左右眼内容是分开的**）。

![img](D:\my-note\android\view\assets\v2-07e74f0caca6297707273fa4f1d37417_1440w.png)

其二，回到双Buffer上来，**即使使用了前后缓冲，撕裂仍是可能发生的**，原因在于GPU绘制速度快于显示器刷新速度，则后缓冲填满之后，则前缓冲仍会被新数据覆盖；

那么既然GPU速度快了不行，那么我远低于显示器刷新速度就没问题了？很遗憾，仍然不行。GPU正在填写后缓冲时，屏幕就已经开始显示该帧内容了，同样不是一帧完整的数据。

这就难办了，因为**只有GPU与显示器步调一致时才能不撕裂**。

这就需要**VSync机制**了，无论谁快了，都只能等待。**如果GPU快了，则无法继续绘图，原地sleep。而显示器快了，则继续刷新上一帧**。

## VSync是什么？

让我们回溯到CRT时代，如果你对前肩（front porch）和后肩（back porch）同步信号（sync pulse）等概念一脸懵逼时，可以看看这篇文章[1]，在我看来讲述的是最通俗易懂的。这在VSync参数调节中非常重要。如下图所示，**以屏幕最后一行为例。**

![img](D:\my-note\android\view\assets\v2-cb2a9274165382f6143d24e32aa1e210_1440w.webp)

> 由于回扫的过程中不能采集（或者输出）信号，因此需要关闭电子束（**CRT时代的概念**），称作blank（Horizontal/Vertical balnk）。回扫结束后（回到了最左边或者最右边），需要重新打开电子束，称作unblank。
> 有效的视频信号必须要在回扫开始前结束（即上图中“t2”处），从有效视频信号结束到回扫开始的那一段时间，称作前肩（front porch），这段时间就是blank的时间。回扫完成后，unblank操作时，电平从接近零值上升到正常值也需要一定的时间，称作后肩（back porch）。
> 每一行的有效信号之后，会有一段horizontal blanking interval，每一帧的有效信号之后，会有一段vertical blanking interval。编码器利用这些空隙时间，传递了一种特殊信号---同步信号（HSYNC和VSYNC），用于告知接收端每一行以及**每一帧视频信号的开始和结束**。而这里的**Vsync就是我们今天的主角**。

## **Android上的VSync**

我们在网上看到的大部分分析vysnc的文章都来自于4.4，不得不说4.4是个让人印象深刻的版本，也是寿命最长的版本，Android 9时代了，低端机仍然在使用4.4。

图片大约如下（谷歌官方介绍）：

![img](D:\my-note\android\view\assets\v2-0fcbf123dc4abeffc721992c13aa3e50_1440w.webp)

但其实这种图片不太适合Android，真正的Android的VSync的图片应该长下面这样，App绘制完一帧后并不会在下一帧立刻显示出来，后面我们还会用到这张图。

![img](D:\my-note\android\view\assets\v2-f1bbf540442583f3bf985f6e89106fb0_1440w.webp)

## **VSync 为何总是伴随着三缓冲**

**不仅是Android上，即使PC上，如果开启VSync，都是同时开启三缓冲**。原理都是一样的，如下图。

![img](D:\my-note\android\view\assets\v2-1f0aac7398209132f1d05922a7cbd09d_1440w.webp)

![img](D:\my-note\android\view\assets\v2-5bb1aa1dbcbc8b33b0cd5615ec0a5751_1440w.webp)

> 谷歌文档中，给出了这样的解释：假如缓存区B的处理需要较长时间，并且A正在使用中显示当前帧。此时引入第三个缓存区，则CPU与GPU不会空等，而是系统创建C缓冲区，可以避免下一次Jank，从而让系统变得流程，通俗来讲就是减小红绿灯的等待。

## **三缓冲对于Jank的影响**

> 以60HZ屏幕为例，janky frames是绘制时间大于16ms的帧，但由于3缓冲机制，其实很可能不会发生真正的jank（**视觉停留在屏幕上的时间多于16ms**）。在弹力球测试时发现，即使发生20次绘制janky frames，仍然不会发生一次真正的jank（高通平台可通过mdss_*fb_*0查看真正的jank）。这就是三缓冲的作用。

## **那为什么不用四缓冲？**

既然三缓冲如此有效，四缓冲还不炸裂，我开10个缓冲，那不更流畅吗？

事实上，别说三缓冲，**PC的游戏VSync默认都不开**（似乎在Android手机上难以想象）。

这些都是因为***操控延时\***。

> **在解决触控延时上，Android又是怎么处理的呢？**
> **相比PC，Android上为何总是开着VSync呢？**
> **三缓冲是否加重了触控延时呢，是否存在着四缓冲？**
> **Android手机上的延时大约是多少？**
> *文章太长了，我们放在下一篇讲解——*

[沧浪之水：Android上的FreeSync（中）显示延时34 赞同 · 2 评论文章![img](D:\my-note\android\view\assets\v2-6c7c926cb48b7f6865c8c50df7538464_180x120.jpg)](https://zhuanlan.zhihu.com/p/226274721)

[沧浪之水：Android上的FreeSync（下）实现方法30 赞同 · 6 评论文章![img](D:\my-note\android\view\assets\v2-20c4c0b98855fd9e416b0cfeef3fc8cc_180x120.jpg)](https://zhuanlan.zhihu.com/p/240795648)

## **参考**

[http://www.wowotech.net/display/crt_intro.html](https://link.zhihu.com/?target=http%3A//www.wowotech.net/display/crt_intro.html)

https://zhuanlan.zhihu.com/p/41848908

[通过使用单buffer strip渲染来减少移动VR中的延迟](https://link.zhihu.com/?target=https%3A//www.sohu.com/a/78851635_335284)

[https://blog.csdn.net/wangxueming/article/details/64457436](https://link.zhihu.com/?target=https%3A//blog.csdn.net/wangxueming/article/details/64457436)

[【中字】慢镜头下的电视工作原理科普（CRT/LED/OLED)_哔哩哔哩 (゜-゜)つロ 干杯~-bilibili](https://link.zhihu.com/?target=https%3A//www.bilibili.com/video/BV1fW411K7ik%3Ft%3D7)

编辑于 2022-06-17 18:01