![Android系统上的操控延时](D:\my-note\android\view\assets\v2-9e84ebd49c9b48324bf8b3530e7c584e_1440w.png)

# Android系统上的操控延时

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l-1707456693417-288.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a-1707456693417-289.jpg)

同济大学 计算机系统结构硕士

关注

34 人赞同了该文章



如果您还没看过前一篇，推荐您先了解一下Android中VSync引入的前因后果。

[沧浪之水：Android中的FreeSync——上篇66 赞同 · 1 评论文章![img](D:\my-note\android\view\assets\v2-97c49ecf1d7ee7a0468965a10402a522_180x120.jpg)](https://zhuanlan.zhihu.com/p/217298155)

无论PC还是Android手机端，操控延时中占大头的总是**显示延时**。

## 显示延时分析

接上一篇文章所述，android的显示流程是三段式的：

![img](D:\my-note\android\view\assets\v2-e3cfbf21fa0cbbd3fdc1e8c904c2a121_1440w.png)

**绘制：**

> 1. 应用的 UI 线程处理输入事件，调用应用的回调，并更新视图 (View) 层次结构中记录的 绘图命令列表；
> 2. 应用的 RenderThread 将记录的命令发送到 GPU ；
> 3. GPU 绘制这一帧；

通过systrace可以看到绘制流程如下：

![img](D:\my-note\android\view\assets\v2-c84531cc1f8c79c0abb63f80a8bd133f_1440w.webp)

**合成：**SurfaceFlinger 会组合出屏幕应该最终显示出的内容，并将画面提交给屏幕的硬件抽象层 (HAL)；

**显示：**屏幕最终呈现该帧的内容。

其中绘制耗费时间最长，消耗CPU，GPU最多，也是引起卡顿的主要原因。

*为何要等待VSync呢，绘制命令来了就显示，该合成了就合成，无需等待这明显更快啊，对于 显示延时来说，无疑这是正确的。 **但对于流畅性，则是非常不利的**，原因就是——*

【**流水线的应用**】

我们最熟悉的流水线莫过于**指令流水线**了，它增加了系统的吞吐量，如下是经典的三级流水线。

![img](D:\my-note\android\view\assets\v2-fc6f86df6593b8f3a65faeaac6c2bf5f_1440w.webp)

这个图似曾相识，Android显示也是采用了流水线的设计，其中Choreographer就是指挥中枢。 无论是绘制时的doFrame，还是合成时的OnMessageReceived都是在VSync的节拍下工作。

【**资源相关**】

我们知道，为了发挥流水线的作用，需要减小资源相关，数据相关，控制相关。比如译码器在一个时钟周期只能被一条指令使用，而第三条指令可能需要第一条指令的计算结果。

类似的，Android显示的三级流水线同样受限于资源与数据相关：

> 1. CPU,GPU在绘制和合成的时候可能同时用到，但由于多核的存在，对于CPU的影响不大。
> 2. 显示、绘制、合成阶段不能使用同一个Buffer（缓冲与buffer是一个意思，下同），这个时候多Buffer的作用就体现出来了。

**为了发挥三级流水线的作用，三缓冲是必要的。**

【**三缓冲会不会引起延时**】

关于这个问题，网上回答的人不多，而且出现截然相反的两种观点：

> 一种认为VSync是设置了红绿灯，而3缓冲相比2缓冲减小了红绿灯的等待，自然触控延时就会提升。
> 另一种认为3缓冲会引起触控延时，因为多了一个缓冲，触控事情必然会多等待一个Vsync周期。从直观上看，这个似乎也有道理。

其实，**Android上三缓冲不仅比双缓冲流畅，而且平均延时还低。这取决于显示系统使用了几级流水线（**比如PC上采用了二级流水，双缓冲比三缓冲的显示延时低**）。**

由于三级流水线的原因，即使你使用双缓冲，**最好的情况，显示也要在第三个周期内完成，这至少需要2.5frame**（显示在屏幕中间算是0.5frame——扫描到一半的时间）。

我们分为三种情况分析：

> **绘制稳定**：即绘制与显示步调一致，显示足够流畅，那么三缓冲与二缓冲没有什么区别，待显示队列都只有1帧；
> **绘制太快**：待显示队列中多了一帧，但由于三级流水线的原因，待显队列的前一帧会在当前帧‘合成’阶段显示在屏幕上，当前帧的延时时间仍是2.5frame。
> **绘制太慢**：双缓冲不够用了，如果此时能用第三个缓冲，则显示屏幕时间为2.5frame。而如果没有第三个缓冲，则此时丢帧。在下一帧开始绘制，此时延时达到3.5frame。

如果使用二级流水，也就是绘制完成+合成作为一级流水线，那么显然结论是相反的，同样根据上面的3种情况分析。

> 绘制稳定，相同；
> 绘制太快，缓冲队列中多了一帧，双缓冲则需要1.5frame，而三缓冲则需要2.5frame
> 绘制太慢，均在2.5frame后显示在屏幕上；

【**拆分流水线：四级流水线】**

在指令流水线中得知，流水线级数越高，并行执行的阶段就越多，执行效率越高。

同样在指令流水线得知，一般通过拆分长节拍提高流水线级数。

对于显示来说，为了帧率的稳定性，同样也可以采用同等策略。在120HZ到来的时刻，为了缓解绘制压力，将负载最重的绘制切分为2个阶段。此时使用4级流水线，如下图，

![img](D:\my-note\android\view\assets\v2-f71624f9c6d12e41a8236f8dfeb45626_1440w.webp)

（来自Android官方，应该是Android11上的特性）

同时，这会增加单帧的延迟 (延迟量为 number_of_pipeline_stages x longest_pipeline_stage)，但由于120HZ的原因，即使增加一级流水，也比60HZ的延时也好很多。

为了4级流水线的顺畅运行，则最好使用4个buffer。

【**Android Q目前缓冲数量】**

目前Android Q仍然还是采用3 缓冲，而流水线看起来也仍然是3级的（4级可能会出现于Android R）。

```text
微博（3 buffer）
0x72f1671880: 10200.00 KiB | 1080 (1088) x 2400 |    1 |       1 | 0x10000900 | com.sina.weibo/com.sina.weibo.VisitorMainTabActivity#0
0x72f16718f0: 10200.00 KiB | 1080 (1088) x 2400 |    1 |       1 | 0x10000900 | com.sina.weibo/com.sina.weibo.VisitorMainTabActivity#0
0x72f1671960: 10200.00 KiB | 1080 (1088) x 2400 |    1 |       1 | 0x10000900 | com.sina.weibo/com.sina.weibo.VisitorMainTabActivity#0
```

**那么有没有什么地方会用到4缓冲或更多呢**？答案当然是存在的：

游戏与普通UI一般是不同的，**游戏有自己的渲染线程，有自己的buffer管理，不受3缓冲限制**，可以开10 buffer，也可以开2 buffer。（甚至可以有自己的流水线控制，但只能针对绘制阶段做控制，合成与显示是不受应用控制的）

```text
和平精英（4 buffer）
0x72f1670230: 4800.00 KiB |  720 ( 768) x1600 |    1 |        2 | 0x10000900 | SurfaceView -com.tencent.tmgp.pubgmhd/com.epicgames.ue4.GameActivity#0
0x72f1671650: 4800.00 KiB |  720 ( 768) x1600 |    1 |        2 | 0x10000900 | SurfaceView -com.tencent.tmgp.pubgmhd/com.epicgames.ue4.GameActivity#1
0x72f1671b20: 4800.00 KiB |  720 ( 768) x1600 |    1 |        2 | 0x10000900 | SurfaceView -com.tencent.tmgp.pubgmhd/com.epicgames.ue4.GameActivity#1
0x72f1671c00: 4800.00 KiB |  720 ( 768) x1600 |    1 |        2 | 0x10000900 | SurfaceView -com.tencent.tmgp.pubgmhd/com.epicgames.ue4.GameActivity#1

王者荣耀：（3 buffer）
0x72f1672a70:10191.50 KiB | 1078 (1088) x 2398 |    1|        2 | 0x10000900 | SurfaceView -com.tencent.tmgp.sgame/com.tencent.tmgp.sgame.SGameActivity#1
0x72f1672b50:10191.50 KiB | 1078 (1088) x 2398 |    1|        2 | 0x10000900 | SurfaceView -com.tencent.tmgp.sgame/com.tencent.tmgp.sgame.SGameActivity#1
0x72f1672d10:10191.50 KiB | 1078 (1088) x 2398 |    1|        2 | 0x10000900 | SurfaceView -com.tencent.tmgp.sgame/com.tencent.tmgp.sgame.SGameActivity#1

而视频播放：（16个buffer...省略）
```

**游戏的绘制时机也要等待VSync吗？**

正常的UI线程对于VSync是无感知的，对于开发者是透明的，绘制时间完全由系统自动决定。但对于游戏则不然，**他们绘制时间是由开发者决定，不必然等待VSync**，他们可以通过Sleep函数来控制帧率。

## Android 如何解决显示延时

在网上我们看到更多的描述VSync的图片又与上方给出的不同，通常是下面这个样子

![img](D:\my-note\android\view\assets\v2-bba76e58c0b2f97e5d7bdec85b8f3994_1440w.webp)

我们发现Android增加了2个offset值，一个值是App offset，一个是SF offset。

默认情况下这2个值都是1ms（高通平台采用默认值），这是希望应用在收到vsync后能有一定的余量去做准备工作。谷歌给出这两个值最重要的作用可不仅仅为了这点效用，而是为了减小触控延时，比如Pixel 3手机上App offset值为1ms，而SF offset为6ms。

> 而原理显而易见： 如前所述，APP绘制完成，发送给SF合成，最后发送给DP显示。如果App与SF在Vsync到来时同时工作，那么显示延时至少在2.5帧。 如果让SurfaceFlinger在APP就绪好后就能开始工作，无需等待下一个VSync。App开始工作的时候是Vsync+offset1，SurfaceFlinger工作的时刻在Vsync+offset1+offset2。

这样的效果的确也是立竿见影。在60HZ屏幕上，当我们把SF offset设置为6ms，当**一帧合成时间小于5ms或者大于17小于22ms时，那么触控延时能减小一帧**。

实测结果显示：**触控延时平均38.2ms减小到30.7ms，大约半帧时间**

**效果这么好，为什么高通平台默认没开呢？**

首先，这个机制与动态刷新率相冲，因为不同刷新率上需要配置不同的offset；而厂商可以根据自己屏幕配置，而平台提供方只需要提供标准支持方案；

其次，这个会影响吞吐量，也就是帧率的稳定性。如果流水线长度不够完不成当前任务，只能延迟到下一个周期。

1） 对于SF的offset设置要相对保守一些，如果手机性能较差，合成时间较长，不仅对于延时无帮助，还会严重影响帧率；

2） 即使SF的offset设置合理，也不乏出现如下的情况，**这种情况相对于无offset而言虽然frame1的显示延时减小了一帧，但却导致了丢帧**。

![img](D:\my-note\android\view\assets\v2-546f6dc72c03a6a5290a0f35dc8146cc_1440w.webp)

实测证明在60HZ上设置6ms是较理想的值，但仍免不了丢帧。

因此，是否开启这个选项还是要权衡下当前系统要求，一般来说，游戏对于触控延时较高。 如果只是感觉没有粘滞感，通常用户一般对于延时的要求在100ms。对于射击类游戏，操作延时是致命的。他们宁可丢帧，但不能延时太多，差10ms都是生死局。

**这个方法对于游戏是有价值的吗？**

如果对于游戏没有意义，那么这个方法就没有意义。触控延时的需求只会出现在游戏中。 如前面所说，游戏很可能不会等待系统的VSync（和平精英从trace上看，绘制时不会等待VSync）。

答案——仍然是有意义的。因为合成到显示的时间被压缩了，那么整个流水线的时间也会缩短，但是吞吐量同样也会下降。 实际证明来看，也的确是有价值的，数据如下：

实测结果显示：**触控延时平均52.2ms减小到48.6ms**

**回到原点思考**

> 比如一段路走1s，那么这段时间可以刷60帧，也可以刷30帧，这中间30帧根本没有绘制，全堵在dequeue buffer上，细节信息少了，触控延时也增大了。
> 还有一种可能，中间30帧都绘制完成，但都丢掉了，那么细节信息也变少了，但是触控延时并没有增大。

后面这种方法就是不等待VSync，PC默认就是采用后面一种方法。

**为什么Android会采用前一种策略呢？**

> 首先，从上面描述可以看出，VSync使能显然更省功耗，对于手机而言这个是非常重要的。
> 其次，这与输入设备有关，很显然鼠标的视角切换更快，反馈更快。而触摸屏达不到这种速度，人们很难感知这种差别。

**【PC的解决方案】**

既然鼠标相对于触摸屏对于操控延时要求更高，那么PC上采用何种措施？

> 高刷：（目前PC，VR，手机上都是采用高刷，**这的确是最有用的**，我们也没什么好讲的）
> 默认策略：垂直同步关闭（撕裂无所谓）；二重缓冲（从这里看出来渲染流水线是2级）；
> 开启FreeSync或G-Sync：同时游戏帧数限制为低于屏幕刷新率2-3帧。G-Sync和FreeSync同步只有在帧数低于显示器最大刷新率时开启。

**【VR的解决方案】**

VR要求则更高了，因为它需要多个感官沉浸其中，如果与现实世界不同则会带来眩晕感，延时要求在20ms以内，策略也就更激进。

> VR使用了单buffer(上一篇文章已经讲过)；
> 它甚至跳过了SF合成阶段，绘制完成后直接拿去显示；
> 这还没完，对于可预见的丢帧，它会直接根据当前位移的角度重新模拟一帧；

**总结**

PC上游戏都是能跑多快就跑多快，而Android游戏通常都是特定帧率——比如30,60,90fps。另外Android游戏缓冲数量，用户无法配置，所以第一条路被堵死。

这样看来，提升触控延而又避免屏幕撕裂的方法就是FreeSync，实际上Android也是可以采用FreeSync的，下一篇文件将详细介绍。

触控延时的大头就是显示延时，另外一部分主要是来自于sensor或者TP端，与FreeSync主题已经没有关系了。而VR的实现策略也与FreeSync主题无关，所以我们将放在触控延时的专题中继续讲解。

[沧浪之水：Android上的FreeSync（下）实现方法30 赞同 · 6 评论文章![img](D:\my-note\android\view\assets\v2-20c4c0b98855fd9e416b0cfeef3fc8cc_180x120-1707456693423-300.jpg)](https://zhuanlan.zhihu.com/p/240795648)

## 参考

[谷歌开发者：在 Android 上进行高刷新率渲染](https://zhuanlan.zhihu.com/p/142212769)

编辑于 2022-03-12 11:02