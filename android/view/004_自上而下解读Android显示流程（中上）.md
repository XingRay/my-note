![自上而下解读Android显示流程（中上）](D:\my-note\android\view\assets\v2-703eed496f5e9da17a3b302b975903bd_1440w.png)

# 自上而下解读Android显示流程（中上）

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l-1707456208301-138.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a-1707456208292-136.jpg)

同济大学 计算机系统结构硕士

关注

91 人赞同了该文章





目录

收起

前提概要

Hwcomposer

1 功能

2 HWC的历史

2.1 Overlay模块

2.2 HWC 1.0

2.3 FB到ADF的转变

2.4 HWC 2.0

2.5 FB/ADF到DRM(KMS)的转变

2.6 composer 作为单独的服务

3 合成策略

3.1 基本策略

3.2 多显示设备

3.3 drm_hwcomposer中的改进方案（一个蛮有意思的点）

4 事件处理

5 显示后处理

6 遗留问题回答

7 最后

参考

*（由于内容太多，显示流程中篇——也分为上下两篇）*

如上文所述，Android显示流水线大约分为3部分，上文只有讲解**绘制（客户端，对应于DRM）**，本文主要讲解一下**合成部分（服务端，对应于KMS）**，合成部分是承上启下，也是理解显示框架的核心部分，我们知道Linux系统上目前的窗口系统Wayland最重要的模块便是Wayland Composer（Weston），后面会专门讲解一下linux系统的窗口管理系统与Android的区别。

## 前提概要

**1. 按照显示流水线来看，合成阶段发生在SurfaceFlinger 和 Hwcomposer**

![img](D:\my-note\android\view\assets\v2-703eed496f5e9da17a3b302b975903bd_1440w.png)

> SurfaceFlinger 顾名思义：Surface + Flinger, 即将Surface组合一起；
> Hwcomposer 顾名思义：HW + composer：硬件合成器

所以，本文重点即是讲解发送在SF和HWC阶段的合成，但讲解顺序为先HWC后SF，主要原因在于HWC更为基础，是理解合成阶段的关键；***对于SF，我们会结合Buffer管理讲解，其中包括Gralloc和Fence等概念。（在中下篇）***

**2. 按照显示模块关系来看，SF的位置承上启下，如下图（标注橙色模块为OEM厂商负责）**

![img](D:\my-note\android\view\assets\v2-4dbb240694d2665db4d7af5064056032_1440w.png)

> OpenGLES/Vulkan(GPU) : 主要发生在绘制阶段，上文已讲解（合成也需要，主要2D功能）
> Gralloc(ION)：分配和使用Buffer；
> Hwcomposer(KMS)：决定合成策略；

**3. 是否能将显示与合成放在一个流水线里面，这理论上是更理想的流水线方式，如下图对比，延时会明显降低，这是个思考题，最后我们会讨论一下。**

![img](D:\my-note\android\view\assets\v2-4eb81ab31880c4628b1e4122a3f2f8d2_1440w.png)

## Hwcomposer

这是Android发展到2.0之后才出现的概念，算是SF的附属子模块，之前是运行在SF主线程的，如今改成单独的Service，这是Android的设计理念，效率其实是下降了，两个进程的交互是相当频繁的。

![img](D:\my-note\android\view\assets\v2-6f1e4a1b0136bda3725bdf7cd8ae3b70_1440w.png)

如上图**rebuildLayerStack、prepare、doComposition**就是SF进程调用HWC模块

## 1 功能

![img](D:\my-note\android\view\assets\v2-cd55750dbc5eac8cd3d40b3b471f6368_1440w.webp)

其中合成策略的整体方法如下：

> A scene from SurfaceFlinger would be composited via a combination of putting some layers directly on overlay planes and flattening the rest with the GL compositor (and putting the result on a hardware plane).

![img](D:\my-note\android\view\assets\v2-0006d5a603d28e59496149c0a3280a87_1440w.png)

下面有个更直观的图，决定layer列表是由GPU合成还是overlay planes合成：其中3个小图层使用了overlay planes，而另外几个图层由GPU合成后，又分配了一个overlay plane。

![img](D:\my-note\android\view\assets\v2-9eeb630562acb79432716079face971c_1440w.png)

对于上面这张图，我们给出一个对应的DPU设计图（高通称MDP，就是移动显示处理器），如下，（显示下篇会主要详细介绍DPU），如下每个overlay就按照顺序对应着上面图中的每个layer buffer（如Status Bar等4个）

![img](D:\my-note\android\view\assets\v2-7e86a2d4798576672d808e57621a085a_1440w.webp)

## 2 HWC的历史

## 2.1 Overlay模块

**前身：overlay模块，大约在Android2.0之前**

**用途：**假如没有这样一个模块，所有合成任务都将有GPU完成，则会带来性能与功耗的负担，GPU主要用于绘制，参与合成则会造成资源的浪费，特别是在10年前，GPU参与合成，性能会急剧下降——对于如今来说，并没有那么大影响了，基本顺手捎带着就完成了，如今的考虑主要在于功耗，GPU合成的效率太低了。

我们来看GPU合成的操作是不需要3D渲染的，所做的工作无非是：

- 格式转换；
- 缩放操作；
- 拷贝操作；

这种操作也称为Blit2D，最早的GPU会拆分一个模块去做这件事情，比如vivante、Imagenation，但后来这些接口都废弃不用了，承担这一责任的就是DPU了，效率显然更高。（**GPU也不再提供专门的2D模块，毕竟2D是3D的子集**）

**DPU：Display Processor Unit**

DPU的能力有强有弱，至少要存在1个通道，称为Graphic Pipe，如果只有1个通道，则一般由GPU合成输出。***如果DPU要有合成功能（layer mixer），则至少要有2个通道。\***

最早的DPU增加的overlay plane还主要用于视频播放，支持的格式也只是YUV；当然这也与Android最早的Overlay模块的默认使用的是V4L2功能，主要处理视频输出。其分为2部分——***数据与控制，这与Android系统上的SurfaceFlinger的数据与控制端是类似的***。但HWC中则与应用Client（数据端）解耦了，只与Server有关了。

*——我最早接触的DPU，都没有alpha blending的功能，视频只能支持全屏输出，带UI的就无能无力了，苟且的方案是colorkey，但这个效果太差，是不能在项目上落地的。*

## 2.2 HWC 1.0

随着DPU能力的增强，支持的overlay通道越来越多，而且overlay的能力也越来越强，另外此时也支持多个显示设备了，所以Hwc 1.0应运而生，这个版本持续时间还比较长，大约一直持续到Android M。

接口非常简单，可以说只有2个函数：prepare() set()

![img](D:\my-note\android\view\assets\v2-b22b35ffd10fbddd2178d9f9619dad00_1440w.png)

> Prepare：先验证下是否能用overlay通道合成，能的设置为HWC_OVERLAY，其余的则标记为HWC_FRAMEBUFFER,统一由GPU合成；
> Set：将最终的Buffer列表传给DPU，等vsync到来显示出来；

但其实比如VSync事件处理、HDMI插拔事件处理、还有些显示后处理的操作基本也会被OEM厂商实现了，也就是越接近硬件的功能都会放在这里实现。

如果来看数据流程图，也比较清晰（Fence会专门放在后面讲解，这是理解显示流程的关键）；

![img](D:\my-note\android\view\assets\v2-71f3cb6dc5765d2559688589fd85008f_1440w.png)

## 2.3 FB到ADF的转变

![img](D:\my-note\android\view\assets\v2-0a0e0b10b87eaa3f9aacdefbe32cd547_1440w.webp)

ADF是短暂的过渡，高通没有使用该框架；还是坚持FB的框架（显示下篇我们会介绍经典的FB框架是如何实现的），但ARM是使用了ADF框架的（**ADF是兼容FB的，在内核中简单实现了接口**）。

ADF代码写的很不错，框架是非常清晰的：

1. 定义了drm buffer、overlay planes、显示设备CRTC；所有这些和Linux的DRI框架KMS是类似的（如下drm框架）。

![img](D:\my-note\android\view\assets\v2-487dfb770a76950154d99de998d86be9_1440w.webp)

2. 显示框架中明确加入了Fence的流程（之前这是留给OEM厂商去实现的）
3. 明确支持了vsync与hdmi插拔事件（之前也是留给OEM厂商去实现的）

## 2.4 HWC 2.0

![img](D:\my-note\android\view\assets\v2-ebd87299cf97abfe95f4868e3d7a6a86_1440w.png)

虽然HWC版本升级了，但基本的工作还是没变，只是将之前很多OEM厂商自己要做的事情，由HWC模块规范化了，这也吸收了ADF的一些特性；

以drm_hwcomposer为例，图中d0代表显示设备display0，dn代表显示设备displayn

![img](D:\my-note\android\view\assets\v2-65b53786f3f8ff28a17b8b9fb472478d_1440w.png)



伴随着HWC2的是从FB/ADF到DRM（KMS）的转变，这是最大的变动，见下节。

## 2.5 FB/ADF到DRM(KMS)的转变

如前所述，ADF框架高通并未采用，也许高通知道谷歌的规划是将采用Linux系统下的DRM框架，这在上一篇文章中已经讲过了。

![img](D:\my-note\android\view\assets\v2-7c1fa17308b40db2866c86b0eedf9ca5_1440w.png)

> 1. Direct rendering manager (DRM), is introduced to deal with graphic cards embedding GPUs
> 2. Kernel mode setting (KMS) is a subpart of the DRM API, sets mode on the given display
> 3. Rendering and mode setting are split into two different APIs and are accessible through /dev/dri/renderX and/dev/dri/controlDX
> 4. KMS provides a way to configure the display pipeline of a graphic card (or an embedded system)
> 5. KMS is an alternative to frame buffer dev (FBDEV)

FB框架到DRM框架是整个框架的改变，终于和Linux实现了兼容，FB框架（在显示下篇会介绍）基本上被废弃了，对于上层用户来说，’/dev/fb0’是找不到了，但在drm框架下兼容FB接口还是非常简单的，主要看OEM厂商的意愿了。

*还需要兼容这些接口吗？那就看谁在使用FB/ADF/DRM的接口呢？*

**其一，Android启动后，hwcomposer就是唯一的使用者；**

**其二，Android启动前，大约有下面3个情况：**

- ***Recovery——显示调用比较简单，可以通过这个了解drm的基本显示流程；***
- ***关机充电；***
- ***开机从kernel到android的启动前这段时间；基本不会主动绘制（通常是一个logo，由bootloader传过来），这是在kernel完成的***

**可以看出来使用这些接口都是系统完成的，由谷歌自己完成了升级，第三方是没有权限调这样的接口的，兼容是没必要了。**

相比FB，所有的hwcomposer的接口也随之改变，开源的drm_hwcomposer给了很好的范例，也增加了很多代码。

- Plane objects managed through properties and atomic mode set commit；
- Features exposed through plane properties；
- Default allocation in driver. User mode can query and override, virtualization is taken care in CRTC

## 2.6 composer 作为单独的服务

Anroid 为了将vendor与system分隔开，composer被拿出来作为单独的服务，效率肯定是变差了。我们以使用开源的drm_hwcomposer为例，需要下面这样的配置：

```text
+    <hal format="hidl">
+        <name>android.hardware.graphics.composer</name>
+        <transport>hwbinder</transport>
+        <version>2.3</version>
+        <interface>
+            <name>IComposer</name>
+            <instance>default</instance>
+        </interface>
     </hal>

+PRODUCT_PACKAGES += android.hardware.graphics.composer@2.3-service \
+        libhwc2on1adapter \
+        libhwc2onfbadapter
+PRODUCT_PACKAGES +=  hwcomposer.drm
+PRODUCT_PROPERTY_OVERRIDES += \
+       ro.hardware.hwcomposer=drm \
+       hwc.drm.device=/dev/dri/card0
```

## 3 合成策略

## 3.1 基本策略

1）当前buffer格式是否overlay所支持的，目前基本上能支持几乎所有格式；

2）当前buffer输入大小是否overlay所支持的，一般有最大最小限制；

3）当前buffer输出大小是否overlay所支持的，一般有最大最小限制；

4）当前buffer缩放比例（输出/输入）是overlay所支持，一般0.25-4倍；

5）当前buffer旋转方向是否overlay所支持的，一般90,180,270是可以的；

***主要是针对视频，因为视频是VPU解码出来的，未经过GPU。而经过GPU绘制的全部预旋转过了（这在SurfaceFlinger是可以配置的），也就是说全部是0度，即使是你旋转屏幕。说到底这也是为了DPU方便处理；***

6）当前buffer是否带有alpha值，overlay通道中有些是不支持的；

——其实在调试DPU的时候，我们遇到最大的问题是DPU的带宽不够（比如缩放比例过小，比如旋转），这会导致屏幕的闪烁——但目前DPU设计的带宽都足够了，但减小系统带宽一直是芯片厂商努力的方向，因为可以这可以减小功耗，***比如Arm的AFBC，比如高通的UBWC，都是减小从GPU/VPU到DPU的带宽。\***

如果说上面的6条措施是硬性规定，并没有什么策略性问题，但当前buffer list是否超过overlay个数，我们不得不面对这样的决策——我们该选择哪几个layer用于GPU合成？这里的原则也是减小带宽。

**通常用于GPU合成的layer是：**

1） 首先是不怎么变化的layer；

2） 其次再看Layer比较小的（较大的留给overlay）；

3）个别情况还需要计算，哪几个layer总的pixel值最小；

4）***屏幕几乎静止的时刻，选择方案也会很有趣，此时会采用GPU合成——原因在于关闭DPU几个通道此时对功耗更有利。\***

**目前GPU合成情况少之又少**

随着DPU发展迅速，现在已经支持到**16个layer**了，用到GPU的情况是越来越小了。而且有些DPU可以通过4个1080P组成4K的输出，对于4K的视频也不需要GPU参与合成了。

对于3D的支持也都是目前DPU标配，之前这也是通过GPU来辅助处理的；

## 3.2 多显示设备

对于多个显示设备，考虑的情形则更多：

1. 主显示设备——LCD；
2. 辅助显示设备——HDMI；
3. 虚拟显示设备——WFD，没有真正的物理设备；

对于LCD + HDMI这样的组合，比如我们可以有2个overlay赋值给LCD，2个overlay赋值给HDMI，也可以3+1，也可以是1+3等；我们之前为了减小DP的带宽，将HDMI的输出格式改为RGB565（两个通道的配置是完全可分开的）。

对于虚拟显示设备，为了减小带宽，则使用了**Writeback**功能，这也是DPU的一个功能，也就是将DPU的数据直接给到虚拟显示设备的output buffer中去，但是对于宽高以及比例都一定的要求。

## **3.3 drm_hwcomposer中的改进方案（一个蛮有意思的点）**

![img](D:\my-note\android\view\assets\v2-04f40b78b4355ab22715c2c299cbafd7_1440w.webp)

先说结论，这个方法减小了功耗，但是ARM，高通都不支持，所以现在没人用，又改回去了，如下图。

![img](D:\my-note\android\view\assets\v2-dc43184d92cffe784c3966b08de98bb4_1440w.png)

但这个思路值得研究，来龙去脉在此：

![img](D:\my-note\android\view\assets\v2-77acb22886537d51e0d9bf94bdb9d61d_1440w.png)

基于硬件优化的GL composer是没有fallback 到SF，而在HWC完成合成，期间无需blending。

![img](D:\my-note\android\view\assets\v2-c00a2b52cbdb8290a7487c3dec55c815_1440w.png)

![img](D:\my-note\android\view\assets\v2-c77212506f6b017123ca1fa2d0fb2fb7_1440w.webp)

这种方法缩短了显示流程，降低了功耗，但带来的优势还是非常小的——**因为fall back到GPU的过程本身就是极小概率（16个layer），而且DPU通常也设计出overlay专门用于no blending hardware，功耗足够低。**

## 4 事件处理

除了上面这种合成所做的事情，还会处理一些其他事件，也都是和显示相关的。

> VSync（包括对于freesync处理）；
> HDMI插拔；
> 分辨率切换；
> 局部刷新；

## 5 显示后处理

事实上，在现代DPU的设计中，后处理的功能越来越多，早已超过了仅仅合成的功能，在显示下篇我们会详细介绍这一部分，在这里只是简短说一下。

> 1. 在我之前写过的“DC调光”那篇文章中提到的，蒙版dither的方法，添加一个dim layer，其逻辑实现就是放在HWC中，这就是一种显示后处理的方法。
> 2. Pixelworks 的 “独显芯片”中的后处理逻辑实现；
> 3. 支持HDR这样的Tone mapper方法；
> 4. 通过内容调节背光的逻辑也在于此；

## 6 遗留问题回答

对于最开始的问题，能否将绘制与合成2个流水线缩减为1个流水线，其实另一篇文章已经有讲述。

[沧浪之水：Android系统上的操控延时34 赞同 · 2 评论文章![img](D:\my-note\android\view\assets\v2-9e84ebd49c9b48324bf8b3530e7c584e_180x120.jpg)](https://zhuanlan.zhihu.com/p/226274721)

——我再复述一下结论，首先缩减流水线的目的是为了减小操控延时，但这样对于帧率稳定性是不利的，事实上，Android不仅没有这样做，反而在高刷的机器上将显示加到了4级流水线。

## 7 最后

本文主要讲解了hwcompser这个模块，但里面有个关键的概念Fence却没有涉及，是专门留给了下一篇文章，而Fence是理解显示流程中Buffer运转的关键点，既然是Buffer，我们也会结合Gralloc模块。

## **参考**

drm_hwcomposer 2.0官网

drm_*kms官网*