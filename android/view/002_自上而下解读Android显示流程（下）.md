![自上而下解读Android显示流程（下）—Display Processor的设计](D:\my-note\android\view\assets\v2-c8563d246a2667d8e13cf8b25354a5ea_1440w.png)

# 自上而下解读Android显示流程（下）—Display Processor的设计

[![沧浪之水](D:\my-note\android\view\assets\v2-3c2814021f8d537e8c6d4aa37289c8b8_l-1707456131462-68.jpg)](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[沧浪之水](https://www.zhihu.com/people/jian-xing-jian-yuan-63-46)

[](https://www.zhihu.com/question/48510028)![img](D:\my-note\android\view\assets\v2-4812630bc27d642f7cafcd6cdeca3d7a-1707456131456-66.jpg)

同济大学 计算机系统结构硕士

关注

60 人赞同了该文章



***本文DPU代表Display Processor Unit，并不是机器学习的Deep-LeaningPU\***

PC上DPU是嵌入在显卡上，不管是独立显卡还是集成显卡都是如此。由于GPU能力越来越强，**DPU目前基本是附赠的功能，但从历史来看，GPU才是后有的新鲜之物**，最早的只有DPU，从最早的Framebuffer机制就能看出，DRM框架中最早版本中也是不存在GPU的代码。

DPU最简单的功能便是将Frambuffer数据输出到显示设备上去，而Framebuffer的来源也都是来自于CPU的软件绘制，而非GPU绘制。

![img](D:\my-note\android\view\assets\v2-a0bba1891a066da7a617ffb79813898f_1440w.png)

原始DPU

上图没有给我们很大启发，因为这离我们现代的DPU设计差别太远。

## **1. DPU与GPU的耦合是历史产物，完全可以独立出来**

### **【DPU用于控制端，GPU用于内容端】**

![img](D:\my-note\android\view\assets\v2-ff775f0f4c69584746b45fc3df9d8617_1440w.png)

通过Linux的dri显示框架，也能看出KMS的相对独立性，对应于系统侧的composer，而drm则在于内容相关的应用侧。对于Android系统也是一样的，GPU对应于drm（不过高通与mali并没有遵循这个开源drm框架）是用来绘制的，属于应用端的进程；而DPU对应于KMS，运行于服务端，可以认为在SurfaceFlinger（composer）中，开机就会初始化，然后保持不变，两者的分离更加彻底。

**PC上Linux与移动端Android的不同**

PC上耦合还是非常强的，DPU与GPU共享显存，代码也放在一个文件里，Buffer管理（GEM/TTM）自然是互通的，linux中默认代码是合并一块的，这是历史遗留问题——Andriod则不同，天生就是分离的，而ION是Android分配buffer的标准。

**Linux平台：**我们拿高通adreno的Linux开源代码来看，系统将DPU与GPU合并在一个文件夹下: drivers/gpu/drm/msm，功能基本也大体是分开的，比如GPU相关的为：adreno、msm_gpu.c，msm_ringbuffer.c，比如DPU相关的为disp，edp，hdmi等。**但仍然有一部分代码是耦合在一起的**，比如msm_gem.c, mem_drv.c。GPU命令还是使用drm标准的或定制的命令。

对于GPU来说，UMD使用的是mesa（高通并没有官方linux的支持）

**Android平台：**高通官方代码则在两个完全不同的仓库，不存在任何代码的共享，GPU放在drivers/gpu/msm，配置的是KGSL，DPU则是不开源的私有库（OEM厂商可以拿到）。这也说明两者逻辑上并不存在那么紧密的联系，也就是传个framebuffer。

对于GPU来说，UMD是libGLES_xx.so(包含GL和EGL)，并没有GEM和DRM那套东西，完全闭源，OEM也拿不到源码。

**GPU与DPU完全可以采用不同的厂商，但通常也是一家的，原因何在呢？**

Buffer共享更高效：虽然buffer共享是通过ion，但是为了节省DDR带宽，通常会将共享的buffer压缩，比如Arm的AFBC，高通的UBWC。

如果使用不同的厂家，其实也能做到这一点，比如对于ARM，如今mali gpu还是广泛被使用，但mali dpu已经少有人用了，那就附赠一个**AFBC Decode模块**，如下图。（高通并没有放开这个限制）

![img](D:\my-note\android\view\assets\v2-6ae1bfeb42927c6b5ad3356ad5ff65d3_1440w.png)

**DPU的基本功能应该有哪些呢？**

DPU的设计相比GPU来说还是简单的，在于其功能的固定性，不可编程，其基本功能大约有2个。

1）2D加速（缩放，合成）

最早的linux代码还能看出痕迹，一开始2D加速功能都是使用CPU；后面2D加速开始使用GPU来实现。到Android系统后，则由GPU专门的2D模块来实现（**甚至会配置为双GPU，其中一个GPU只做2D加速**），然后专门的DPU出现代替了GPU的2D模块（后面GPU再没有专门的2D模块，因为2D本来就是3D的子集，虽然专门设计的2D模块效率会高一点，但也没有DPU效果高，所以逐渐淘汰）。

2）vout的管理（连接LCD，HDMI等设备）。

下面给出DPU的一个基本设计原型，这包含4个部分。

## 2. DPU的原型设计

### **2.1【DPU的四大组成部分】**

这是2013年的DPU设计图，当年Android发布了升级最大的4.4（也许是最成功的一代）。从下图可以看出DPU的设计大体分为四部分：

![img](D:\my-note\android\view\assets\v2-2b4e20b4bfd31a74b3b2597bc09e2585_1440w.png)

DPU Design

**1）Source Surface Pipes（Pipe也称overlay，后面不再区分）:** 支持4个overlay通道（V1-V4），支持RGBX，YUV等多个格式，缩放比例（1/4 - 4），且每一个layer都支持alpha通道，

- C1、C2是鼠标层，对于PC来说很重要，但对于手机来说基本没人使用。
- 当时还不支持旋转；
- 支持4个layer的alpha blending，在当时还是比较奢侈的，比如监控就没必要这样设计了，更离谱的有的设计了16个layer看着很唬人，但支持alpha 只有1个，也没有任何用处，对于Android系统来说alpha的layer特别多。

**2）Blender：** 支持2个Blender，对应于2个Path（除了LCD外，对应于DP或HDMI投屏）；

**3）Destination surface post-processor**：支持dither，gamma调整；**目前的趋势是这部分越来越重要**。

**4）Display Interface：**支持最多2路同时的输出设备（物理显示设备，虚拟显示设备不需要实际的输出设备）；支持LVDS，DSI，CVBS，HDMI等显示设备；

DPU更细节的图如下：

![img](D:\my-note\android\view\assets\v2-9569f62117090309a6332e01473fb426_1440w.png)

如果放在Android系统中，我们来看一个HDR视频的播放流程的话，则能更好的看出这4个部分。

![img](D:\my-note\android\view\assets\v2-73890da99bcf27210c11b8e0e58da717_1440w.png)

### **2.2【KSM与DPU】**

其实这张图也和我们常见的DRM的KSM框架图非常契合，也就是说**KSM与DPU功能几乎等同**：

![img](D:\my-note\android\view\assets\v2-ce5b15b3eafefeb9f1281361a014a2b2_1440w.png)

此图DRM Framebuffer应该是DRM Framebuffer list

- Source Surface Pipes：每个overlay对应一个Plane，**每个ovelay中都有一个DRM Framebuffer**；在dumpsys SurfaceFlinger的时候，每个Layer就是一个overlay，一个DRM Framebuffer。

```text
-----------------------------------------------------------------------------------------------------------------------------------------------
 Layer name
           Z |  Window Type |  Layer Class |  Comp Type |  Transform |   Disp Frame (LTRB) |          Source Crop (LTRB) |     Frame Rate (Explicit) [Focused]
-----------------------------------------------------------------------------------------------------------------------------------------------
 com.android.systemui.ImageWallpaper#0
  rel      0 |         2013 |            0 |     DEVICE |          0 |    0    0 1080 2400 |    0.0    0.0 1080.0 2400.0 |                              [ ]
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
 com.miui.home/com.miui.home.launcher.Launcher#0
  rel      0 |            1 |            0 |     DEVICE |          0 |    0    0 1080 2400 |    0.0    0.0 1080.0 2400.0 |                              [*]
```

- **CRTC**：对应于一个Path，每个Path至少有一个**Blender，DSPP**也在此处（如下图）；所有的layer都会做alpha blending，输出到一个显示通路，通常格式为**RGB24和RGB30（10bit），也就是显示在屏幕上的内容**。
- Display Interface：Encoder，Connector是与显示设备相关的；最终这个RGB24的数据会通过MIPI、DP传给显示设备，这些协议相关的实现也在DPU模块内完成。

![img](D:\my-note\android\view\assets\v2-57032f0aa4faf16c43f1cdbab70ba504_1440w.png)

## 3. DPU的最新设计

### 3.1【Source Suface Pipes or Overlays】

1）Pipes（也有叫overlays）一般分为两种：（这也不能叫趋势，高通从一开始就有这两种）

- 一种支持**缩放、旋转，锐化**等复杂功能的Video overlay（当然，video overlay可以适用于任何layer，video只是一个称呼，更适用于游戏和视频）；***这里的缩放，锐化是单layer的，与后面的整个屏幕的缩放不同。\***
- 一种简单功能的Graphic overlay；支持格式转换，**也支持alpha**；

2）支持输入的分辨率更大，比如支持4K的输入，这需要更高的DPU频率；

3）**随着XR（AR、VR）设备的出现，目前单眼4k已经出现（DPU就要支持8K的输入），这样带宽压力太大，所以目前的做法通常并不是直接4K的输入，而是切分成2个2k（当然这样可用的layer就会减小一半），这就是Split功能；**（这也不是新功能，因为很多年前4k视频就出现了）。

4）支持旋转，主要用于视频播放，其他场景基本用不上，GPU会预旋转。（mali dp 650支持这个就不好，主要带宽影响太大，从kernel开源代码看，dp650后，mali似乎便没有更新）

5）pipe越来愈多，比如8个，16个（基本也不会比这更多了）

- 对于手机至少需要6个：1. Main activity (2 layers); 2. status and navi bar (2 layers) 3. round cornor(2 layers，高通针对round cornor这种永远不会变化的区域也有优化）；
- 对于应用于电视的Box则要考虑缩放，每个layer都会被缩放（所以需要一个dest的缩放，而非source）

6）支持压缩格式（UBWC或AFBC）；减小内存带宽，特别是与GPU的交互带宽。

![img](D:\my-note\android\view\assets\v2-82f3f1de67b9ab71793e44134eb3fbc7_1440w.png)

**小结：这些技术都出现很多年了，也看不出未来变化的趋势，除了第三点，因为XR对于分辨率的追求仍没有到头，单眼8K也会到来，这样DPU要支持16K的输入，这个带宽压力太大了（特别是在scale down的时候），即使切成2个8K，压力仍然很大，所以未来是不是搞2个DPU出来也未可知。**

### 3.2【Blender】

1）合成layer越来愈多，比如支持10个layer的合成（大部分layer其实不会互相叠加）；

2）合成path越来愈多，比如支持4个（同时使用3个的场景已经非常罕见）

- WFD（虚拟显示设备）也算是一个path，对于XR来说每个2D应用都是通过wfd来实现的，而WFD是DPU的writeback功能实现的，而writeback功能一般也只支持一个path，如果有多个wfd，则只能借助GPU来实现了。
- 如果未来有发展，便在于是否增加Writeback的path，如果这样不合算，则需要考虑只采用一个虚拟显示设备，所有的2D应用都放在此处。

3）支持3D功能；（可以区分左右眼，因为3D功能是很多年前便普及的了，所以不是新技术）

4）Dim layer：Android上的常用场景，作为渐变色，只有灰度值的变化，其他不变；

- 如果大家对于Oled屏幕上的DC调光有了解，便会知道，Oppo最初的方案便是增加一个dim layer，然后调整这个灰度值去让屏幕显得没有那么亮，从而避免PWM调光。

5）Background color：对于单色图片，也有一些优化方案。

小结：对于4和5，完全是根据应用场景增加的优化方案，为了节省功耗，也算是一点点抠了。未来XR的发展，可能会针对Writeback功能做进一步优化；

### 3.3【Destination surface post-processor】

最开始后处理还只是dither、gamma校正、还有亮度、对比度、饱和度调整这些功能，在四个模块中并不重要，但却是近几年发展最快的一个模块了。现在旗舰手机很多用上了**独立显示芯片PixelWorks（后面简称PW**），宣传的功能便是：**MEMC、HDR、阳光屏（CABL）、护眼模式、Demura、超分**；这些功能高通都有，全放在自己的后处理中。

1） **超分与锐化**

*这里的超分指的是Destination Scaler，是对整个屏幕数据做的，与前面的Source pipe的针对layer的超分是不同的，虽然算法是一样的。*

目前平台几乎都不再使用简单的双线性插值，而是自己的算法，但目前仍是基于单帧的技术，虽然MTK宣称已经支持AI超分，但效果并没有让大家觉得特别亮眼。

PC上的有英伟达的AI超分DLSS、AMD的传统超分FSR，在网上反映都还比较不错，但放在手机上要么功耗高，要么在手机上这种高PPI的应用场景，超分的优势就没那么大了。（***在PC上表现良好的FSR超分算法在手机上效果真的是不好***）

随着XR对于分辨率越来越高，所以这个需求还会继续发展，也是未来的一个发展方向。

2）支持HDR，SDR to HDR，都是基本操作。

3）亮度调整：区别于Android根据环境光调整，主要是基于内容的背光调整算法。可以区分为indoor和outdoor。Indoor光线不强，主要由CABL和FOSS，其中分别针对LCD和OLED屏幕；outdoor则使用ARM的阳光屏技术，当然高通在后面采用了自己的Local Tone Mapper策略（既可以用于indoor，也可以用于outdoor）替换了ARM的阳光屏技术，主要拉升图像暗处细节，也不能让高亮的地方出现过饱和。

4）MEMC

电视上的标配，目前手机上也都是放在视频上，是PW最开始引入手机上最重要的原因，通过插帧实现30帧都60帧视频的流畅。

5）demura：oled上的必备流程

**小结：同样工作放在DPU中处理功耗也会低一点，PW是放在后处理后的interface模块，所以PW去做功耗则会高一点；如果DDIC去做，则功耗会更高一点，越靠前则功耗更低。不仅在于流程，还在于制程，所以PW存在的价值在于其算法能力，是否能超过高通或MTK。**

> **DPU –> PW -> DDIC**

### 3.4【Display Interface】

东西很多，不再一一列举（后面专门讲下mipi），可见未来的发展还在于XR。

![img](D:\my-note\android\view\assets\v2-f6ea58e571c316b8bb53afc0652d34bc_1440w.png)

## 4. 总结

DPU分为4部分，功能已经比较稳定：其中显示后处理是以后升级的重点（其中超分与锐化又是优化的重点），同样的功能，相比独立显示芯片PW或DDIC去做有更好的功耗；

XR会极大左右DPU的发展：无论是分辨率带来的带宽压力，还是最新的注视点传输这样的技术，都需要DPU做出较大改变。