![Android上的FreeSync（下）实现方法](D:\my-note\android\view\assets\v2-20c4c0b98855fd9e416b0cfeef3fc8cc_1440w.jpeg)

# Android上的FreeSync（下）实现方法

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l-1707456717463-313.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a-1707456717463-314.jpg)

同济大学 计算机系统结构硕士

关注

30 人赞同了该文章



前情提要：

[沧浪之水：Android中的FreeSync——上篇66 赞同 · 1 评论文章![img](D:\my-note\android\view\assets\v2-97c49ecf1d7ee7a0468965a10402a522_180x120-1707456717468-318.jpg)](https://zhuanlan.zhihu.com/p/217298155)

[沧浪之水：Android上的FreeSync（中）显示延时34 赞同 · 2 评论文章![img](D:\my-note\android\view\assets\v2-6c7c926cb48b7f6865c8c50df7538464_180x120-1707456717468-320.jpg)](https://zhuanlan.zhihu.com/p/226274721)

我们文章主要目的仍在于介绍PC上的相关技术，以及看下是否能在手机上使用。如同前两篇文章，我们不仅仅简单的科普，还会介绍实现的具体细节。

PC上关于VSync的各种概念非常多，比如**FastSync，AdaptiveSync，G-Sync，FreeSync。**在这里我通过一张表格总结一下。

![img](D:\my-note\android\view\assets\v2-1bd6994f4262dd79214acaff3e1c555a_1440w.webp)

图上60HZ指代显示器刷新率最高值，并不代表一定是60HZ

其中Android中使用的是VSync On + 三缓冲；为了减小操控延时，能参考的方案为Fast Vsync与FreeSync（G-Sync原理类似），其中FreeSync显然是更好的方案。

## 【G-Sync与FreeSync】

> FreeSync，顾名思义，就不是固定的VSync，而是采用可调整的VSync，让显卡与显示器步调一致。
> G-Sync和FreeSync都是基于**VRR(variable refresh rate)的技术**，只是实现思路不同。 而思路的差异主要在于成本/兼容性等方面的考虑。
> 但显示器刷新率有下限，LCD也不适合低刷新率工作，比如60HZ的屏幕，min fresh rate有的是30，有的是55，要看屏幕素质。
> 如果游戏帧率在VRR Window内(Min Refresh Rate ~ Max Refresh Rate), GSync与FreeSync工作的比较好，但低于此帧率的情况下，则又产生在vsync on(stutter)和vsync off(tearing)之间做出取舍问题了，为了解决这种问题，G-Sync引入了LFC低帧补偿技术，后面FreeSync也引入了这一技术。

**LFC 低帧补偿技术**

> 当帧率降低到一定程度时，刷新率必须变成帧率的整数倍才能在不引入低刷新率副作用的前提下模拟出帧率等于刷新率的效果。
> 这个功能是通过驱动算法实现的，但是显示器的Scaler需要有足够的能力去响应显卡输出到显示器的指令（动态变化的VBLANK）。能够变动的刷新率范围越大，越有利于驱动算法的实现。而目前标准的Scaler性能有限，导致Freesync在LFC方面无法做到定制编程的FPGA核心的GSYNC显示器的效果。

GSync还加入了**ULMB：超低运动模糊——**减少快速游戏（射击，体育）中由于快速视角切换等造成的运动模糊。事实上**HDR**也打包在GSync概念里。

## 【适用场景】

**PC适用场景**

虽然GSync与FreeSync很好用，但是游戏帧率超过刷新率，那么他们就不适用了。

*在帧间隔永远大于刷新周期且不至于长到启用LFC机制时，GSYNC + VSYNC ON和GSYNC + VSYNC OFF没有区别。使用GSYNC时永远推荐搭配限帧使用，不要让帧率高于刷新率。*

因此游戏Up主得出的经验：**将游戏帧率锁在屏幕刷新率低2-3帧效果最佳（此时开不开Vsync无影响）。也就是说在屏幕刷新率为144HZ情况下，游戏跑在140-142帧最好。**

**手机适用场景**

首先描述一下手游的典型场景：

1. 第一**国民游戏王者荣耀**：60帧稳定运行，不需要任何技术，任何策略都行；
2. 排行第二的和平精英，可以选择90帧的流畅，与60帧的HDR。帧率越高越耗费CPU，但90帧的游戏一般也能扛住。为了操控延时，用户可以选择屏幕120HZ的刷新率，而不是视觉上更顺畅的90HZ，在这种情况下，FreeSync会有些用处，减小一点不顺畅，对于延时也没有影响。
3. **吃辣椒酱的QQ飞车**已经有120fps的版本了，对于很多赛道来说，帧率是不能稳定在这个帧率的，此时也非常适合FreeSync。
4. 崩坏3的1080P非常吃GPU，帧率很难稳定在60fps，此时FreeSync也是有用的。

对于移动端的Android系统而言，VRR技术与LFC技术都很有用，但是LFC对于手机端的帮助可能不大，原因在于手机端动态切换刷新率是刚需，比如120fps的游戏，当低于60fps的时候，最好的办法就是切换到60HZ的刷新率，而不是开启LFC。

## 【是否需要GPU配合】

在Android上不需要GPU配合，这就需要了解PC上显卡的概念了，显卡不仅包含移动端的GPU，还包括DisplayPU与VideoPU。

对于PC上与显示设备配合的是显卡，而手机上就是DPU，无需GPU参与。比如前面章节提到的Android显示流程是三段式的，你可以理解为对应的硬件设备为：**GPU---DPU---LCD**。

> GPU 运算能力很强大，但若将所有的处理工作都放在 GPU 上，势必会使其功耗过大，若能将固定的处理工作放到 显示处理器上，将可有效降低整体的系统功耗，并有助于效能上的提升。 GPU 的能力主要是在绘图而非在显示数据的处理上，虽 GPU 也可进行压缩，但其处理方式与[DPU](https://link.zhihu.com/?target=https%3A//www.eefocus.com/tag/DPU)大不相同。 相较于 DPU 可处理完多项任务后进行压缩与一次性输出，GPU 则必须进行多次读取、处理与输出，不利于降低整体系统功耗。

DPU是连接GPU与LCD的桥梁，如下DisplayDriver就是管理DPU的。

![img](D:\my-note\android\view\assets\v2-bf169f3544f8c77b2bd65ba44b8b0459_1440w.webp)

## 【高通的QSync】

既然PC上已经证明了，最好用的策略就是FreeSync，手机上只需抄作业就行了，通过上述原理可知，最重要的是需要支持VRR（手机上一般称Dynamic Refresh Rate）。

比如高通在自己的平台上就支持了FreeSync（QSync，即Qcom Sync）。原理与PC上的FreeSync别无二致。

在绘制时，有无FreeSync流程对比如下。

![img](D:\my-note\android\view\assets\v2-dcd025edf5973ded707e5527c2fa30fa_1440w.webp)

绘制d2阶段时间比1个Vsync周期长了一点，但还没有2个Vsync周期。绘制d3也只能等待下一个VSync周期，***中间这段时间是闲置的**，*同样在屏幕上显示也延时1个VSync周期。

再看加入QSync之后，绘制d2在没有绘制完成之前一直不发送VSync信号，直到绘制结束。此时绘制d3会立刻渲染。同理显示在屏幕上的时间也会变快。如上图所示，同样绘制4帧，有QSync机制的总时间会更短，吞吐量也就增大***。\*无论对于帧率的稳定性，还是对于操控的延时效果都非常好。**

QSync存在三种模式：one-shot，every-commit；continue（默认）

我们知道在往屏幕上commit帧的时候，我们必定要做的一个动作就是等待这一帧绘制完成。大体的样子如下，所以DPU模块是很容易知道当前帧是否绘制完成的。

```text
for （layer ： layerlist）      wait_fence_timeout(layer)  
```

![img](D:\my-note\android\view\assets\v2-4381762650cf76994924ba3ba23f3bf6_1440w.webp)

## 【屏幕VRR支持】

我们知道，目前的主流屏幕分两种：video与commd屏。

**Video屏**

如果你是video屏，那么恭喜你，基本上你是可以用上FreeSync的，因为显示器也是video屏，实现起来就容易的多了。

对于Video屏来说，动态刷新率的实现方法有2大类方法：

1）直接改变PLL的频率；

2）改变Vertical blanking或者horizontal blanking，其中前者改动更小，是默认的实现方式。如果你还记得本系列第一篇文章中介绍Vsync时那些前肩，后肩的概念时，你就会知道该怎么调整Vsync的时长了。

同样的也需要配置一个***Min Refresh Rate\***。

**Command屏**

相比Video屏的多个选项，Cmd屏则需要厂商决定是否支持，幸运的是通常CMD屏都是支持这个特性的，因为它和VRR是一脉相承的。而手机屏幕对于VRR是刚需，毕竟在手机上一直90/120HZ还是非常耗电的。Cmd屏支持freesync的原理并没有本质不同，它是通过调整TE信号去调整vsync的周期。如下图：

![img](D:\my-note\android\view\assets\v2-d6549a7db140013b3eb4b3b0ba4ac839_1440w.webp)

如上，vsync的长度是不能随意变换的，只能以一个pulse为单位，比如上面的pulse是2ms，也就是11ms的vsync可以拉长到13ms，但无法设置为12ms。

同样的也需要配置一个***Min Refresh Rate\***。

**存在问题**

尽管cmd屏基本都支持freesync技术，但仍然存在着不能回避的问题，参见下图

![img](D:\my-note\android\view\assets\v2-0a7c7f34f2339817ac856d23807b3da9_1440w.webp)

可见，动态刷新率与freesync不能同时存在，否则extend的pulse值就不匹配。但其实这也不是难解决的事情，但似乎厂商并无并无解决意愿，我的猜想可能是freesync模式与正常模式互相切换会花屏。

留下自己的解决方法：

> 背景：在freesync使能的情况下，是不能切换刷新率的。这样很好理解，因为freesync会调整Vsync周期：vsync_period（基于之前的刷新率） + add_porches。另外，在刷新率确定的情况下，此时打开freesync是可行的；
> **那么切换刷新率之前关闭freesync，切换成功后再打开freesync理论上并没有什么问题。
> **
> 对于cmd屏来说，***如果在最高刷新率时打开freesync同样是没有任何问题的\*。**当需要freesync时，将从正常模式切换至freesync模式。如果需要切换刷新率，那么同样没有问题，将freesync切换回正常模式，再将120HZ切换为60HZ（假设最高刷新率120HZ，游戏帧率为60fps）

**终极解决方案——设计1HZ-120HZ的VRR**

屏厂目前都已经实现了动态刷新率的切换，比如120HZ，90HZ，60HZ三种自由切换。但如果设计为无级调节，那么不仅可以提升游戏的流畅度，而且节省功耗，手机上就没freesync什么事情了。但这确实存在着巨大的挑战——

事实上，90HZ的屏幕切换到60HZ的时候，不仅仅是调整vsync，还有一组gamma参数，这样屏幕的效果才会更好，否则会出现低亮度色阶，颜色不均匀等问题。但这个gamma对于每个特定的刷新率并不是完美存在的。

![img](D:\my-note\android\view\assets\v2-738d716585f6ca2ee7fec50f7a8af3ae_1440w.webp)

听说三星正在研究这项技术，已经有demo出来了，但目前还不得而知。如果真正出现了这个技术，那将是巨大进步。

## 【留下的思考】

**为何手机上没有大面积普及**

原因大约有两个：

其一，目前旗舰手机几乎都采用cmd屏，而这受限于屏厂。通常屏厂在动态刷新率与freeSync是复用寄存器的（尽管存在解决方案，但切换瞬间可能会花屏），也就是只能二选一，那么手机厂商的选择只能是动态刷新率，毕竟对于手机而言功耗非常重要。

其二，freesync带来的好处主要是游戏帧率的稳定性与触控延时的减小，对于大部分旗舰手机可能并不太着重强调这个，毕竟拍照比这个更重要。 而这个带来的提升同样也不是那么显而易见的，比如和平精英，几毫秒的延时差距与零点几帧的提升对于厂商的宣传并不利。

**为何FreeSync看似如此简单的道理，实现起来却并不简单**

其中很重要一个原因就是——道理是一般的东西，普遍的东西，抽象的东西，是和具体情况相对的。比如物极必反，山东人都豪爽。

这个说法还隐含着，普遍性对于理论家是重要的，但对于实干家则没什么用。物极必反，这没有错，但也没什么用，因为麻烦总在于弄不清楚什么时候是极点。你深知物极必反的道理，但你还是不知道什么时候该买进股票，什么时候抛出。

这也是平时自己面对这些问题时，自己的困惑。解决问题的时候并不能完全依靠自己懂的道理，很多时候是特殊的。

## 【参考】

[【硬件科普】全网最详细易懂的G-sync Freesync 垂直同步工作原理科普_哔哩哔哩 (゜-゜)つロ 干杯~-bilibili](https://link.zhihu.com/?target=https%3A//www.bilibili.com/video/BV1FK4y1x7bk)

[gsync和高刷新率哪个更重要？](https://www.zhihu.com/question/278191604/answer/406374168)

[浮梁卖茶人：【什么是画面撕裂？垂直同步，G-sync，Freesync到底有啥用？】](https://zhuanlan.zhihu.com/p/41848908)

[谷歌开发者：在 Android 上进行高刷新率渲染](https://zhuanlan.zhihu.com/p/142212769)

[https://www.bilibili.com/video/BV1FK4y1x7bk](https://link.zhihu.com/?target=https%3A//www.bilibili.com/video/BV1FK4y1x7bk)











发布于 2020-09-19 21:39