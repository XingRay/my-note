# Android 新老 Camera APIs



### 0 背景简介

最近有一部分相机相关的需求，专注于对拍摄的照片、视频的噪点、色温、明暗等方面进行优化。一般我们在生活中使用相机来拍照的话，在前期拍摄时，就可以通过调整相机的拍摄参数，如：曝光时间、光圈大小、ISO、曝光补偿等等，使得拍摄下来的照片本来的色彩、明暗就处于比较理想的状态，无需过度依赖后期处理。而手机端的拍摄则往往受限于硬件和机型，无法在前期如此“收放自如”，我们项目中现有的做法也主要是通过后期处理来优化图像。但是手机拍摄的场景日益增多，用户对于手机端拍摄的要求也日益提高，我们希望能够最大限度地挖掘出手机的能力，在整个出片的过程中，在前期也能对图像有更好的把控。同时手机性能在不断增强，自 Android 5.0（API 21）开始，Google 也发布了一整套新的相机控制用 API，即 camera2，而将原来的 Camera 被标记为 deprecated。按照官方的说法，新的 Camera2 升级了性能也支持了许多新的功能。所以借此机会对 Android 相机硬件的新老版本 API 做了一番调查和梳理，以求在日后实现功能时能有更多的选择和更好的效果。

### 1 Pre-L Camera API

#### 1-1 简介

我们的天天 P 图项目目前使用的是 Lollipop（Android 5.0）以前版本的相机接口。由于出现得较早，Google 给这一版本的定位是：满足“对准 - 拍摄”的需求即可，即一种类似于“傻瓜相机”式的自动调节的拍摄体验。不高的目标加上手机硬件的各种限制，早期的相机 API 设计时“先天不足”、能力有限：

- 有限的照片数据流（拿不到 raw 格式原始数据文件）
- 有限的相机状态信息
- 无法进行手动拍摄控制

#### 1-2 概要（此段非常重要，是对下面 Camera API 参数分析的一个总结）

详细的 Camera（< API Level 21）支持的功能列表见 *附录 1*，不同的功能最低支持版本略有不同。需要注意的是：

- Android 只是开放了这些硬件 API 接口，具体的实现是由硬件以及手机厂商来决定的。不同的硬件和实现方式，自然也会有不太相同的效果。
- 由于硬件和软件实现的不同，并不是所有的机型都支持所有这些功能；支持某一功能的手机，他们支持的程度也可能是不同的，或者对于某一功能给出的可选项是不同的，所以在使用各功能或参数前，应该先调用 API 获取到相机 Parameter 对象，并通过`getSupported...()`，`is...Supported()`，或`getMax...()`等方法判断其是否支持该功能（且支持到何种程度）。
- Camera 的 API 由于比较早，手动控制功能非常有限。
- 调节参数是对整张照片起作用，且直接应用于拍摄结果。

总结起来，测试了多台机器后发现：Android 的 Camera API 不同机型的结果真的是千差万别，可控性也不尽相同。所以 在对拍摄时性能没有非常高的要求（即不担心后期处理帧率无法跟上。另后面也可以看到，通过 Android 相机硬件 API 并不能完全保证性能足够优秀，如：控制曝光时间仍有可能造成卡顿。），且 1) 需要（局部）精细调节或 2) 不同手机期望有统一的效果的情况下，不推荐调用官方的`android.hardware.Camera` API 来处理照片，而可以想办法在拍摄完成后进行后期软件处理。

#### 1-3 Camera.Parameters 支持的参数

Camera.Parameters 支持的参数中，和本次效果调节需求强相关的参数主要有：

##### 1-3-1 Exposure Compensation 曝光补偿

通常我们拍照时，曝光补偿朝正向调整，会使拍摄到的照片变亮；朝负向调整，则使照片变暗。

###### 1 耗时

我在小米 4，Nexus 6，以及 Pixel XL 上都尝试了使用`Camera.Parameters.setExposureCompensation()`方法，来调节拍照时的曝光补偿值。试用下来，在这 3 台手机上，此参数皆可调节，调节范围皆为 25 档（-12 ~ 0 ~ 12）。也测到有的手机的调节范围为 -4 ~ 0 ~ 4。下图是小米 4 不同曝光补偿值下后置摄像头拍摄到的照片，从左到右曝光补偿值依次设置为：-12、-8、-4、0、4、8、12（图片没有经过压缩或缩放，但为了避免合成图过宽，左右三张分别仅截取了左半部分和右半部分）。

![img](D:\my-note\android\camera\assets\vrxn885jt7.jpeg)

***Figure 1.** 米4 后置摄像头在不同曝光补偿值下拍摄的照片*

在同样的光照条件下，使用相机拍照时的曝光值可以经由快门速度和光圈大小控制。而在 Android 设备上，是无法调节光圈大小的，那么 Android 手机是怎么实现曝光补偿的呢？对于不同的手机，所采用的具体实现方式也可能不一样。在这里，只能通过一些有限的测量来给出我自己的一些判断。

首先推测最有可能实现曝光补偿的手段是改变曝光时长（相当于快门速度）。下图是使用不同手机测试并打 log 得到的在不同曝光补偿下的拍照耗时。

![img](D:\my-note\android\camera\assets\addyncji2b.jpeg)

***Figure 2.** 各手机在不同曝光补偿下的拍照耗时*

可以看到，基本上还是 存在一个“曝光补偿值越大，则拍照（曝光）时间越长”的趋势 ，尤其是比较早的小米手机（最早运行 Android 4.4 系统，现在系统为 6.0.1）和各前置摄像头。所以曝光时长应该是调整曝光补偿的（主要）手段之一。

曝光补偿和曝光时间之间的正相关关系只是一个趋势，进一步猜测还有其它影响因素，根据拍摄的理论，推测同时也通过调整感光度 ISO 来修正了曝光补偿。在 Camera 接口中，并没有开放 ISO 的调整接口，但是这并不妨碍预览或拍照时，系统自动调整 ISO 来达到曝光补偿的目的，这也可以解释为什么使用后置摄像头或较新的机型的手机，曝光时间随曝光补偿的变化不明显，因为后置摄像头以及较新的手机的配置一般会更好，它们对 ISO 的调整硬件支持也理应更好，就无需完全依赖曝光时间了。

然而这些主要还只是我个人的 wild guess，总觉得还需要更多一些数据支持，不死心查看了所有手机拍下的照片的 EXIF 信息，惊喜地发现，虽然像小米4 这些较早的机器没有 EXIF 信息，但是谷歌的原生机 Pixel XL 拍摄的照片是记录了拍摄时的 EXIF 信息的！这里补充 Pixel 后置摄像头拍下的一组照片： 

![img](D:\my-note\android\camera\assets\7z68bn71z9.jpeg)

***Figure 3.** Pixel XL 后置摄像头在不同曝光补偿值下拍摄的照片*

查看这组照片的 EXIF，得到其感光度和曝光时间分别如下： 

![img](D:\my-note\android\camera\assets\6f43rhvquq.jpeg)

激动地发现，果然它的 ISO 并不完全随着曝光补偿增加而变大，重点关注曝光补偿为 -8 和 -4 的两组数据。可见调整曝光补偿的值只是调整了总的曝光量组合，至于影响因素 ISO 和曝光时间各自的变化并不一定线性。这样基本上证明了之前曝光补偿受 ISO 和曝光时间影响的猜测是符合真实情况的了。另外，后期对 Camera2 的实验也从侧面佐证了这一点。

###### 2 噪点

缩短曝光时长、提高 ISO 都有可能使拍出来的影像噪点增加，我们这里也来观察一下拍下的照片的噪点的情况。其实从图 1 和图 3 这两组照片来看，改变曝光补偿的值，对照片的噪点无明显影响（小米4 貌似在曝光补偿较大的时候，噪点会略多，但并不明显）。可以解释为：如果我们要提高曝光补偿，可以增加曝光时间或提高 ISO，而它们对噪点的影响作用是相反的，如果同时改变的话，噪点的变化大部分抵消了。

###### 3 暗环境

上面的讨论都是基于光照环境适中的情况讨论的，其实我们之所以要调研曝光补偿这一 API，是希望在暗环境下拍照时，可以适当调亮照片，尤其是将人脸调整到合适的亮度，从而得到更好的人像效果。下面是在较暗环境下（环境整体偏暗偏黄，顶灯较远，灯罩半透明且有部分遮挡），不同曝光补偿下拍摄的到的照片（好想用个美容啊啊啊啊啊，但是为了体现真实效果，这里都是没有加滤镜的原图）：

![img](D:\my-note\android\camera\assets\xrupkooobk.jpeg)

***Figure 4.** 暗环境下，米4 前置摄像头不同曝光补偿值拍摄的照片*

![img](D:\my-note\android\camera\assets\4b41f1l4be.jpeg)

***Figure 5.** 暗环境下，Pixel XL 前置摄像头不同曝光补偿值拍摄的照片*

较新的高端机 Pixel XL 是相机功能非常强大的一款机型，它的摄像头跑分领先于同时代的 iPhone7 。所以可以看到，有强大的硬件支持，在暗环境下，即使不使用曝光补偿，图像中人脸的亮度也已经被自动调节到比较合适的值。但是大部分手机在暗环境下是达不到如此好的效果的。对于一般机型小米4，显然在不使用曝光补偿的情况下，暗环境下人脸显得过暗，若使用曝光补偿，可以明显提升拍照的效果。 不过仔细观察可看到，最右侧的 3 张照片其实亮度上的差别已经不大，即相机能力已达极限，设置极端的曝光补偿意义不大。

我们再来看一下更极端的、除了远远有遮挡的光源外、基本无光的环境下的效果： 

![img](D:\my-note\android\camera\assets\e9hg37jv1f.jpeg)

***Figure 6.** 极端暗环境下，米4 前置摄像头不同曝光补偿值拍摄的照片*

![img](D:\my-note\android\camera\assets\1vycjmclg6.jpeg)

***Figure 7.** 极端暗环境下，Pixel XL 前置摄像头不同曝光补偿值拍摄的照片*

看图说话，在极端的暗环境下，脸部会被衬得比前例更亮，但是背景由于进光量更少了，噪点非常多（Pixel XL 尤为明显）。而无论是米4 还是 Pixel XL，右边的 4 张照片在亮度上也已经基本没有什么区别了，说明即使不手动调整曝光补偿的值，手机已经自动调整至最大进光量来“补偿”环境光的不足。在极暗的环境下，调节 Exposure Compensation 无明显作用。

同时，上面的图 4、5、6、7 中，都没有看见噪点随曝光补偿有明显的变化，所以我们可以 在暗环境下放心使用曝光补偿值来提亮照片。 在后面 Camera2 的讨论中，我们可以看到，Pixel XL 机型的自动曝光补偿并不会使用到相机的极端值，所以可以放心将其调至最大值。当然，在无法确定不同手机 API 的具体底层实现手段时，在任何方向上调整过度，都还是会有效果上的风险，建议多测试几台机器。

##### 1-3-2 White Balance 白平衡

并不是所有的光源都是相同的，光源的颜色由色温来表示。对于我们人眼来说，中性的自然光色温在 5,500 K (Kelvin，开尔文) 到 6,500 K 之间。色温低看上去偏黄，色温高看上去则偏蓝。人眼很善于根据光源颜色变化来进行调节，而相机则不是。在冷白荧光灯下拍摄的照片往往其中的每一件物品都会呈现出蓝蓝的色彩；而标准的钨灯下的照片则是偏黄。

Android 手机通过方法`Camera.Parameters.setWhiteBalance(mode)`，可以使用不同类型的白平衡模式，不同的模式会使照片氛围有极大的不同。通过测试，米4、Nexus6、Pixel XL 上白平衡模式皆可调节，Google API 提供如下几种不同色调的常见模式：

> WHITE_BALANCE_AUTO：自动
>
> WHITE_BALANCE_INCANDESCENT：白炽
>
> WHITE_BALANCE_FLUORESCENT：荧光
>
> WHITE_BALANCE_WARM_FLUORESCENT：暖荧光
>
> WHITE_BALANCE_DAYLIGHT：日光
>
> WHITE_BALANCE_CLOUDY_DAYLIGHT：多云
>
> WHITE_BALANCE_TWILIGHT：黄昏
>
> WHITE_BALANCE_SHADE：遮阳

Nexus6、Pixel XL 通过`Camera.Parameters.getSupportedWhiteBalance()`得到的即为此 8 种模式，小米4 则还多了一个 MANUAL 选项。下图左边自上而下依次为小米4 设为上述白平衡时拍下的照片（最下方一张为 MANUAL 选项结果，为了和 Pixel 的照片对齐比较，样张有适当缩小）；右边则是 Pixel XL 相应白平衡的拍摄效果。 

![img](D:\my-note\android\camera\assets\v4hpzhb75k.jpeg)

***Figure 8.** 小米4 & Pixel XL 不同白平衡模式下的照片*

可轻易看出：

1. 不同的手机，所支持的白平衡模式个数不同；
2. 相同的白平衡模式下，不同手机拍摄到的照片的效果也大相径庭；
3. 对于白平衡，Camera API 也仅给出了模式的切换，并不能调节程度。

总之，Camera 的白平衡接口只是一个比较“粗糙的接口”，不推荐在：1. 对照片的氛围在不同机型上要求有一致的效果；2. 对照片的冷暖度需要进行精细调节的情况下使用 Camera 白平衡接口。在这些情况下，使用软件加滤镜的方式比较合适。（下一步可以比较一下使用硬件接口和软件滤镜的性能上的差别。）

其它较重要的影响成像效果的可调节参数：

##### 1-3-3 Metering Areas 测光区域

指定图像中用于自动白平衡测光的一个或多个区域。虽然官方文档说的是白平衡测光，但是测试下来对亮度也有作用。

一般相机有如下 4 种测光模式（更详细的介绍见 *附录 2*）：

> 中央重点测光  局部测光（包括：中央部分测光）  点测光  评价测光（或称分割测光/矩阵测光/多分区测光

在 Android 手机上，是没有硬件接口设置测光模式的，只能：

1. 使用`setMeteringAreas()`方法来设置测光的矩形；
2. 调用`getMaxNumMeteringAreas()`方法来获得最多可支持的测光区域个数。

尝试了几台手机，发现不同机型这两个 API 的实现区别非常大，如：

1. Google 原生机 Pixel XL 和华为荣耀9 最多可以设置 1 个测光区域，而小米4 则支持 5 个；
2. 调用`setMeteringArea()`方法设置测光区域为全屏，在华为荣耀9 机器上采用的是中央重点测光（拍摄内容为：黑色背景上放置一块白色横条，当白色横条在屏幕上部或下部时，得到的照片会比纯黑背景上暗，横条置于屏幕中部则为最暗的）；在乐视Max 900+ 上则是完全的中心测光（拍摄内容为：黑色背景上放置一块白色块，除非将白色块置于屏幕中央会使照片比纯黑背景的暗，将其置于屏幕任何其它地方，都和纯黑背景拍出的照片明暗一致）。

所以硬件 Camera 接口相比软件调整，效果的统一性方面存在的问题确实非常突出。

##### 1-3-4 Focus Areas 对焦区域

指定图像中对焦的一个或多个区域。

##### 1-3-5 Scene Mode 场景模式

应用预设的拍照模式，如：夜景、沙滩、雪景或烛光等。

### 2 Camera2 API

新的相机 API，即 Camera2，是在 Android 5.0（Lollipop）引进的，自此 Google 开始弃用之前的 Camera 类。

#### 2-1 新增功能

Camera2 类主要更新了如下内容：

- 支持 30fps 的全高清连拍模式（硬件有多快，就能设置成多快）。

这是 Camera2 最重要的优化之一，即在性能上有了大幅提升：Camera 2 系统提供了全分辨率的图像的同时，在速度方面，硬件有多快、拍摄就能有多快。这一提升主要归功于其完整的同步管道模型（synchronized pipeline model）。以 Nexus 5 为例，它可以在一秒内拍摄 30 帧、最大可达 8 M 的图像。

- 支持无快门延迟拍摄、连拍、HDR+ 模式、去燥以及录制视频时快照。
- 支持相机其他全方位的精细手动控制拍摄和后处理。

包括：曝光补偿（Exposure compensation）、自动曝光/自动对焦/自动白平衡模式（AE / AF / AWB mode）、自动曝光/自动白平衡锁（AE / AWB lock）、自动对焦触发器（AF trigger）、拍摄前自动曝光触发器（Precapture AE trigger）、测量区域（Metering regions）、闪光灯触发器（Flash trigger）、曝光时间（Exposure time）、感光度（ISO Sensitivity）、帧间隔（Frame duration）、镜头对焦距离（Lens focus distance）、色彩校正矩阵（Color correction matrix）、JPEG 元数据（JPEG metadata）、色调映射曲线（Tonemap curve）、裁剪区域（Crop region）、目标 FPS 范围（Target FPS range）、拍摄 intent（Capture intent）、硬件视频防抖（Video stabilization）等。

- 支持帧之间的手动设置修改。
- 原生支持拍摄数字负片格式（类似于 RAW 格式）的图片。

总之，Camera2 对我们的 Android 设备来说是飞跃性的进步，我们可以在更少的延迟下得到更高质量的照片，且拥有更深入的自定义控制项和更多的图像数据，使开发人员能够用照相机来完成更多的工作。

#### 2-2 结构简介

相对于旧有的 Camera 类，Camera2 的逻辑和架构都发生了变化，为了支持更多的功能，会更复杂、更难使用一些。

Camera2 引用管道的概念将安卓设备和摄像头之间联通起来，系统向摄像头发送一个个 Capture 请求，而摄像头会返回包含一些图像的元数据 CameraMetadata 和一系列的图像缓冲 image buffers。这一切建立在一个叫作 CameraCaptureSession 的会话中，Camera Device 对于一系列的请求是按顺序处理。

Camera2 主要有 5 个类，更详细的介绍见 *附录 3* 。

#### 2-3 Camera2 部分新增手动控制功能测评

除了 Camera 支持的 Exposure、White Ballance 等参数，Camera2 新增了不少手动控制项，下面对一些直接影响到拍摄的影像的手动项进行试用。（由于我的小米4 机型较老，硬件上不完全支持 Camera2 的 API，下面的所有测试都在 Google Pixel XL 上进行。）

##### 2-3-1 ISO 感光度

相机的 ISO 指的是其对光的敏感度。ISO 的值越低，感光元件对光的敏感度越低，同时噪点也会比较少，所以在进光量足够（光照强）的情况下，建议使用较低的 ISO；ISO 的值越高，感光元件对光的敏感度也越高，一般在对拍摄速度有较高要求（快速移动的物体）、光照又有所不足的场景下应用。在我们手机的数字相机上，ISO 通过改变传给传感器和从传感器得到的信号来调节，为了降低噪点，会在满足拍摄需求的情况下，尽可能地使用较小的 ISO。

在 Camera2 中已经开放了设置相机预览/拍摄时的 ISO 的接口。 在使用之前，我们仍然通过`CameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)`方法获取一下手机支持的 ISO 范围。发现 Pixel XL 的 前置和后置摄像头支持的 ISO 范围是不同的，前置为 50~6000，后置是 50~12800，这也不难理解，前后置摄像头使用的毕竟是不同的硬件。

预览或拍照时的 ISO 设置可以通过接口：`CaptureRequest.Builder.set(CaptureRequest.SENSOR_SENSITIVITY, iso)`来完成，通过改变它，可以调节照片的明暗。

下面我们先来看一组 Pixel 后置摄像头在不同感光度下拍到的照片（控制曝光时间不变，为 50 ms）： 

![img](D:\my-note\android\camera\assets\l083o80al4.jpeg)

![img](D:\my-note\android\camera\assets\gt7h9qkui6.jpeg)

![img](D:\my-note\android\camera\assets\e334l7z3jm.jpeg)

***Figure 9.** Pixel XL 后置摄像头在不同感光度下的照片（曝光时间 = 50 ms）*

从左到右、从上到下的 ISO 依次为：100、800、1600、2400，3200、4000、4800、6400，8000、9600、11200、12800。ISO 在较低值时，噪点确实明显较少，但是图像也会相对比较暗。如果我们希望照片的噪点较少，又有合适的亮度，那就需要配合下一节说到的曝光时间来使用了。

##### 2-3-2 Exposure Time 曝光时间

在 Camera2 中还开放了设置曝光时间的接口：`CaptureRequest.Builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, time)`（注意这里时间的单位是 ns）。尝试控制 ISO 为 100，将曝光时间分别设置为 50 ms，250 ms，500 ms，1000 ms：

![img](D:\my-note\android\camera\assets\p0cul97wh8.jpeg)

***Figure 10.** Pixel XL 后置摄像头在不同曝光时间下的照片（ISO = 100）*

曝光时间越长，总进光量越大，图片越亮。但是图片亮度只要适合就可以了，重要的还是保证图片质量，即需要在维持图像亮度足够时尽可能降低噪点，所以选择低 ISO，并通过增加曝光时间来维持总的曝光量。下面是控制总曝光量（ISO * Exposure_Time(ns)）为 50000000000（5 E 10） 时，拍摄下的一组照片（从左到右，ISO 和 Exposure Time 的组合分别为(100, 500000000)、(3200, 15625000)、(6400, 7812500)、(9600, 5208333)、(12800, 3906250)）：

![img](D:\my-note\android\camera\assets\0cesdmbzuy.jpeg)

***Figure 11.** Pixel XL 后置摄像头控制曝光量拍下的照片*

可以看到，因为曝光量相等，这五张照片的亮度基本上没有差别，但是 ISO 低的照片噪点情况明显好于 ISO 高的照片。然而我们也不能一味地降低 ISO，因为这势必导致曝光时间变长。表现在预览界面就是相机的帧率下降，表现在拍出来的照片上则是（没有固定支架的话）容易糊掉。事实也确实如此，在拍摄 ISO = 100 的照片时，预览界面卡到一秒大概只有 1~2 帧（同 500 ms 的曝光时间完美吻合），拍下的照片是最左边那张，相信大家还是可以看到一些模糊的迹象的。

此外需要注意，后面节选的官方文档也会提到，曝光时间的支持也是有一定范围的，并不能无限拉长，也不能过短。 使用`CameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)`接口拿到的 Pixel XL 前、后置摄像头支持的曝光时间(ns)分别为：25291 ~ 869530839（即 0.025 ~ 870 ms）、10746 ~ 659414140（即 0.01 ~ 660 ms）。使用时间范围较广的前置摄像头（其实这一点是我没有想到的），我们将总的曝光（ISO * Exposure_Time(ns)）控制在 200000000000（2 E 11），改变 ISO 为 100、800、3200、5600，获得下面这组照片： 

![img](D:\my-note\android\camera\assets\odyfws3z09.jpeg)

***Figure 12.** Pixel XL 前置摄像头控制曝光量拍下的照片*

同之前后置摄像头控制曝光量拍下的结果怎么不一样，第一张照片明显暗了？？其实这是因为：前面后置摄像头拍的照片的曝光时间最长 500 ms、最短 3.9 ms，在其支持的 0.01 ~ 660 ms 范围内，所以都设置应用成功了；而这组前置摄像头拍摄的照片，其第一张设置的曝光时间（2 E 11 / 100 / 1 E 6 = 2000 ms）远超出了硬件的支持范围，系统自动减少曝光时间到最接近的可能值，即 870 ms，使其总曝光少于其他 3 张照片，所以偏暗也就不足为奇了。我们查看该照片的 EXIF 信息印证一下，其中记录的真实曝光时间 900 ms，正是硬件支持的最大值了，perfect！

##### 2-3-3 Exposure Compensation 曝光补偿

还记得我们在第一部分的 1-3 中提到，Camera 用来调节照片亮度的曝光补偿吗？Camera 中并不支持设置拍摄的 ISO 和 Exposure Time，所以全部靠曝光补偿来调节明暗；那么在 Camera2 中，既然已经有了 ISO 和 Exposure Time，那还需要 Exposure Compensation 吗？答案是肯定的：Camera2 的 API 仍旧支持 Exposure Compensation。那这不会有点多余吗？难道 Exposure Compensation 同 ISO 及 Exposure Time 的原理或效果并不完全一样？我的答案是：并不多余，其实曝光补偿和感光度加曝光时间的组合的本质是相同的，只是它们适用的情况不同。

我们先从官方文档入手。下面内容节选并翻译自 Google 官方文档：

1. CONTROL_AE_EXPOSURE_COMPENSATION

曝光补偿通过自动曝光调整来调节图像亮度。

注意曝光补偿控制只有在`android.control.aeMode != OFF`时才起效，并且即使`android.control.aeLock == true`也会产生作用。

当曝光补偿被改变了，相机设备可能需要几帧图像才能达到指定的新的曝光值，在这个过程中，`android.control.aeState`处于`SEARCHING`态。一旦到达新的曝光值，`android.control.aeState`将会变为状态：`CONVERGED`、或`LOCKED`（如果 AE lock 开启），或`FLASH_REQUIRED`（如果对于静态图片捕捉来说场景太暗）。

此项在所有设备上可用。

1. SENSOR_SENSITIVITY (API level >= 21)

处理前传感器数据的增益量，是标准的 ISO 感光度。

如果相机设备无法应用指定的感光度，它会将增益减小至所支持的最接近的值。在输出的拍摄结果中可以看到最终使用的感光度。

此控制项仅在`android.control.aeMode`或`android.control.mode`为 OFF 时有效，否则自动曝光算法会改写它的值。

此项是可选的，在某些设备上其值可能为 null。在所有 HARDWARE_LEVEL_FULL 的设备上支持。

1. SENSOR_EXPOSURE_TIME (API level >= 21)

每个像素暴露在光源下的时间。

如果传感器无法在光照下曝光指定的时长，会减少曝光时间到最接近的可用值。最终使用的曝光时长可以在输出的拍摄结果中给出。

此控制项仅在`android.control.aeMode`或`android.control.mode`为 OFF 时有效，否则自动曝光算法会改写它的值。

单位为纳秒。 最小的曝光时间少于 100 us，`android.info.supportedHardwareLevel == FULL`的设备最大曝光时间大于 100ms。此项是可选的，在某些设备上其值可能为 null。在所有 HARDWARE_LEVEL_FULL 的设备上支持。

综合上述 Exposure Compensation、ISO、Exposure Time 的说明，可以看到， Exposure Compensation 仅在 aeMode 开启时才有效；而 ISO 和 Exposure Time 与之相反，仅在 aeMode 关闭时才起作用。即它们的应用场景永远不会重叠，也意味着这三个设置项并不会重复，它们各有其作用。

下面几张图经过尝试验证了这一说法：

![img](D:\my-note\android\camera\assets\vtpv8ihmtz.jpeg)

***Figure 13.** aeMode 模式开启时，Pixel 后置摄像头曝光补偿分别为-12、0、12 时拍下的照片*

![img](D:\my-note\android\camera\assets\7c4zufubhy.jpeg)

***Figure 14.** aeMode 模式开启时，Pixel 后置摄像头 ISO 分别为 100、6400、12800 时拍下的照片*

可见自动曝光模式时，改变曝光补偿起效；改变 ISO 则无效（曝光时间同理，在此不再赘述）。

![img](D:\my-note\android\camera\assets\yvnf6ua6dt.jpeg)

***Figure 15.** aeMode 模式关闭时，Pixel 后置摄像头曝光补偿分别为-12、0、12 时拍下的照片*

aeMode 模式关闭时，调节曝光补偿的出片并无变化；而不同的 ISO 和曝光时间的照片（见图 9、图 10，不重复列举）则差别明显。总之关闭自动曝光后，只有感光度 ISO 和曝光时间的调节是有效的；曝光补偿的变化不再起作用。

aeMode 开启既是开启自动曝光模式，关闭则是进入手动曝光模式。再结合我们在 1-3 节中的讨论，我们完全有理由相信，在 Android 手机上，对曝光（Exposure）即照片亮度的控制本质上就是对感光度 ISO 和 曝光时间 Exposure Time 两个变量的调整。在手动模式下，由使用者自行设置这两个参数；而在自动曝光模式下，设备根据使用者指定的 Exposure Compensation 值，自行计算并选择合适的 ISO 和 Exposure Time 组合。

##### 2-3-4 ISO + 曝光时间 OR 曝光补偿？

那么，既然已经有自动模式了，我们还有没有必要手动调节 ISO 及曝光时间呢？答案是肯定的，这也是谷歌会在新的 API 中开放这两个参数调节的原因。

首先，自动模式只是相机在当前环境下测光以后自动进行曝光调整，这不能满足一些“特殊”或自定义的需求，譬如说：用户就是想拍一张偏暗的的照片；或者拍摄主体本身比较白，但整体环境又比较暗，如果测光后自动提亮就很容易造成拍摄主体过曝。

另一个非常重要的原因是，经过多番尝试，我发现 自动曝光补偿并不会将相机的能力应用到极限，也就是说即使环境再暗，将相机的自动补偿调节到最大，仍然（远远）没有手动提高 ISO（到最大） ，并延长曝光时间（到最长）的提亮效果强！

![img](D:\my-note\android\camera\assets\u7meouy106.jpeg)

![img](D:\my-note\android\camera\assets\xv3q0m8stu.jpeg)

![img](D:\my-note\android\camera\assets\x75t3xex0h.jpeg)

![img](D:\my-note\android\camera\assets\pz9nmkps5j.jpeg)

***Figure 16.** 暗环境下，Pixel 前置摄像头，自动模式下将曝光补偿开到最大，同手动模式下调节 ISO、曝光时间的效果比较*

四排照片，每一排中间（那张相同）的完整图像是将 aeMode 打开 、相机曝光补偿开到最大时（+12），拍照得到的效果；每排左右其它的、截取了半张的照片都是 aeMode 关闭、将 ISO 或曝光时间调节到不同值时拍摄得到的图片。查看 EXIF 信息，aeMode 打开、曝光补偿为 12 的照片，其 ISO = 3198；Exposure Time = 1/13 s，总曝光 Exposure = 246，以此为基准，计算其余照片的 Exposure，比它小的放在其左侧，大的放在右侧。第一排的 6 张半图，ISO 皆为 800，曝光时间从左往右分别为（ms）：200、300、400、500、700、900；第二排的半图，ISO 皆为 1600，曝光时间从左往右分别为（ms）：200、250、300；第三排的半图，ISO 皆为 3200，曝光时间从左往右分别为（ms）：50、150、200；第四排的半图，ISO 皆为 6000，曝光时间从左往右分别为（ms）：25、50、77、100。

可以看到，即使我们在自动模式下，将曝光补偿值设置为最高了，但是其实它还没有达到相机极限（EXIF 信息中的 ISO 和 曝光时间都还有极大的增加空间），通过手动调节 ISO 及曝光时间，我们可以大大提高照片亮度（故意选择了暗环境，符合一般我们希望能进一步改善的场景，这里也主要讨论增加总曝光的可能性）。譬如：第一排最右侧的照片，目测比起自动曝光的基准照要亮很多，不太能感觉出是在暗环境拍摄的照片了，它甚至有过曝的嫌疑了，然而即使其曝光时间已经达到最大的 900，但是它的 ISO 只有 800，距离最大值 6000 还有很大差距，也就是说图像还可以提亮很多倍；再看最右下角的照片，虽然它的 ISO 达到了前置摄像头最大的 6000，但是它的曝光时间 100 只有最大值的九分之一！ （* 这里我们只讨论亮度问题，照片色调偏黄偏蓝属于白平衡相关，在曝光部分就不予以关注了。）

由此可见，如果想更大限度地调节照片亮度，如极暗环境下的拍摄，那么关闭自动曝光模式，对 ISO 和 Exposure Time 分别进行调整还是很有必要的。但是调节时也仍旧需要综合考虑去噪、防抖等各方面问题。也不建议使用过于极限的值，毕竟无论是过高的 ISO 还是过短的曝光时长，都容易引入噪点；同时我们也无法确定不同手机 API 的具体实现手段时，在任何方向上调整过度，可能会使照片效果变差。

##### 2-3-5 手动控制曝光的注意事项

1. 要使用 Camera2 的 ISO、Exposure Time 手动设置接口的话，先需要将自动（曝光）模式关闭。

我们一般使用`CaptureRequest.Builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)`接口即可，开关自动曝光模式代码举例如下：

```javascript

```

复制

这里配置的是 CONTROL_AE_MODE，专用于自动曝光流程的，从名字 AE（Auto Exposure），就可以看出它是专用于控制相机设备的自动曝光流程的。它的可能值有：

```javascript

```

复制

只要设置成任何一个 ON 的模式，即认为自动曝光模式开启，此时我们只能调整 2-3-3 的曝光补偿了，即使设置了不同的 2-3-1 的感光度和 2-3-2 的曝光时间（还有 Frame Duration，即`android.sensor.frameDuration`，在本文中先不予以讨论了），也会被自动计算出的值给覆盖（最终使用的值可以在 CaptureResult 中取得），从而使这两个值的调节失效。（如果选中了闪光模式之一，再设置闪光灯则也会不起效。如果想使用闪光灯的常亮模式，则此模式必须设置（为开启或关闭），并将`android.flash.info.available`设置为 true。）

除了 aeMode，其实还有 另一个可以打开感光度及曝光时间调节的参数：`android.control.mode`，即`CaptureRequest.CONTROL_MODE`。 它是最高的“总指挥”，控制了所有的三个自动模式（3A，auto-exposure、auto-white-balance、auto-focus），将其设置为 OFF 时，所有 3A 的自动模式都被关闭；当其设置为 AUTO，每一个独立的`android.control.*`的自动算法控制都被打开。详情见官方文档，不再详述。

1. 打开/关闭自动模式，`CaptureResult.CONTROL_AE_STATE`会引入新的状态，需要做相应处理。 一般我们在按下拍照按钮后，会先根据预览界面传回的 CaptureResult 的状态来决定接下来的处理步骤，如：是否需要预处理（precapture metering，检测曝光值、对焦等）；还是可以直接拍摄了等。这时候相机的状态就就是判断依据，很重要。在开启/关闭自动曝光模式间切换，总是会将 AE state 重置为 INACTIVE。同样的，在开启/关闭`android.control.mode`间切换，总是会将所有算法（包括 AE）的状态 state 重置为 INACTIVE。 而前述 2-3-3 中节选的官方文档也提到：当曝光补偿被改变、相机设备达到指定的新的曝光值前的几帧中，`android.control.aeState`处于 SEARCHING 态；一旦到达新的曝光值，`android.control.aeState`将会变为状态：CONVERGED 或 LOCKED 或 FLASH_REQUIRED。除此之外，在启用 Camera2、关闭自动模式后，我还发现多了：`CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN`等新状态，所以也必须对它们一一进行相应的处理。详细 CaptureResult 的 State 表见官方文档。
2. （其实这一段颇为重要，它证明了调研 Camera API 的意义所在~） 之前讨论的一直是拍摄时调控硬件项的效果比较，这里再补充一下和拍摄完使用软件调节的结果的粗略对比，看一下软硬件调整对噪点的影响：

![img](D:\my-note\android\camera\assets\nur9jitojj.jpeg)

![img](D:\my-note\android\camera\assets\uf66vn1oka.jpeg)

***Figure 17.** 暗环境下，Pixel XL 后置摄像头，软硬件调整对噪点的影响*

第一排的照片是调整拍摄时的参数，得到的一组照片，从左向右，ISO 和 Exposure Time（ms）的组合依次为：（1600，200），（400，200），（400，300），（200，500），（800，150）；第二排是对照片使用软件调节亮度处理完后的图像，都尽量将其往一排第一张的亮度靠近。其中第一张是使用 Photoshop 对一排第二张进行亮度和对比度调整得到的结果，PS 水平有限，这是我可以调整到的最接近值，因为效果不佳所以放弃 PS 了；第二至第五张分别是一排第二至第五张照片使用天天 P 图调整亮度得到的结果。比较效果可以看到：

- 软件处理基本上是可以达到硬件增加曝光值拍摄得到的亮度效果的；
- 软件处理的效果好坏和使用的算法有很大的关系，所以也要慎选。PS 我用不好，下面仅针对 P 图的调整结果进行讨论；
- 将二排软件调亮的第二至第五张照片按噪点程度由少到多排序： （200，500）<（400，300）<（400，200）<（800，150），所以基本符合 ISO 越小噪点越少。其中（400，300）和（400，200）两组差距比较少，所以曝光时间的影响较小。
- 将第二排二至五张都同一排第一张的拍摄结果比较，（200，500），（400，300）的噪点比（1600，200）明显要少；（800，150）比（1600，200）的要多；（400，200）的则不好说，近景比（1600，200）的要多，远景则少。

所以我的结论是，高 ISO 拍摄的效果不一定比低 ISO 拍摄 + 软件处理的差，重点在于否有一个好的后期软件处理算法，没有或时间有限的话，使用 Camera 硬件接口调节拍摄时的参数是你最好最方便的选择！

#### 2-4 我们的项目中是否应该引入 Camera2

（* 接下来以 天天 P 图 为例展开讨论）

既然谷歌官方已经废弃了原有的 Camera 类，并推荐使用 Camera2 API，前面也看到了 Camera2 的一些手动控制项的长足进步，那么我们的（天天 P 图）项目是否有必要作相应升级？我的结论是，目前还不到升级到 Camera2 的时候。 原因如下：

1. 只有 Android 5.0 Lollipop（API Level 21）以上的设备才可以使用 Camera2。而 P 图的 minSdkVersion 是 15，差别还是非常大的；
2. 同“一代” Camera API 一样，并不是所有的 Android 设备都支持 Camera2 API 中的所有功能，支持与否这完全取决于每一台设备。
3. 相机功能的支持程度也不尽相同，分为 FULL > LIMITED > LEGACY：

> INFO_SUPPORTED_HARDWARE_LEVEL_FULL： 完全的硬件支持，允许高清拍摄和完整手动控制，支持连拍等。  INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED：受限的设备，支持某些或不完整的属性。  INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY：每台 Android 设备上的相机至少支持到这个程度，即旧有的 Camera 类 API 所支持到的程度（然而前面已经看到，对 Camera 类 API 的支持度也是极为层次不齐的）。

1. Camera2 中的新增功能的效果受设备型号影响太大；同时拍摄参数修改的效果是作用于整张照片的，并非局部应用。天天 P 图对[图像处理](https://cloud.tencent.com/product/tiia?from_column=20065&from=20065)的要求还是非常高的，Camera2 在处理效果和可控性方面目前还比不上使用软件方式；
2. 目前对于 RAW 格式的图像的支持还不尽如人意。

RAW 格式的图像是感光元件直接生成的图像信号数据，没有经过图像处理引擎、软件加工或压缩。尽管 RAW 格式可以有更大的后期空间，目前也有手机开始支持 RAW 格式的拍摄及后期处理（如：OnePlus 3T），但对其的支持现状仍很鸡肋，仅适合手机厂商针对单一产品线或某款手机进行专门适配：

- 数据大小：相对我们常见的压缩图像 JPEG 等，RAW 体积更大，通常是其 3 ~ 6 倍，所以数十甚至上百 Mb 的 RAW 照片是很常见的。
- 拍摄性能、后期处理：拍摄、处理体积较大的 RAW 照片，也对设备性能有较高要求，传统都是在 PC 上处理的，在手机端存在很多瓶颈。
- 统一支持：不同设备的 RAW 格式并不完全相同，必须特殊处理，适配困难；而 Adobe 推广的统一格式 DNG 的普及度、适配、效果等各方面也都不尽如人意。

1. 由于结构上的改变，自 Camera 切换到 Camera2，工作人力成本大。

综上，可能在我们提高 minSdkVersion 至 21，且对处理速度有进一步的要求时，再引入 Camera2 会比较合适。

#### 2-5 更普适的 Camera & Camera2 的选择

2-4 讨论的是我们自己的项目，由于历史原因以及该项目的特性：希望能够支持到更多的用户、希望能有统一的可控的效果、目前的软件处理基本能满足需求，我们得出了暂时不升级的结论。然而，更普适地情况下，放着 Camera2 的强大功能不用实在是有点暴殄天物。下面来谈谈个人对 API 版本选择上的一些看法：

1. 如果你只是想用最新的 API 快速实现一些拍照功能、或者想要基本的相机演示 Demo，Camera2 还是非常好用的，其实 Camera2 的所有缺点，1 都有，它结构上的复杂也是有限的，所以只要最低支持版本能接受，放心大胆地使用 Camera2 吧。
2. 如果你也希望项目的最低支持版本不要太高，能被尽可能多的用户用到；同时又对拍摄效果有一定要求，那么其实可以考虑同时接入 Camera 和 Camera2 的方案。即将 Camera2 和 Camera 都封装出一套完全相同的接口，在启动时先尝试打开 Camera2 相机，成功则继续使用 Camera2，失败则退而求其次使用 Camera。当然也可以利用黑白名单来进行功能屏蔽。这种做法当然会增加开发工作量，但是没有基础的情况下，比起实现软件的调整也许还是会更简单一些。

总之，Camera2 的提高还是很明显的，也是大势所趋，条件允许还是应该尽可能升级到 Camera2。

### 3 附录

#### 3-1 Camera（< API Level 21）支持功能列表：

Table 1. Common camera features sorted by the Android API Level in which they were introduced.

![img](D:\my-note\android\camera\assets\uooc6bpizq.jpeg)

Note: These features are not supported on all devices due to hardware differences and software implementation. For information on checking the availability of features on the device where your application is running, see Checking feature availability.

#### 3-2 4 种测光模式

##### 1. 中央重点测光

中央重点测光是采用最多的一种测光模式，几乎所有的相机生产厂商都将中央重点测光作为相机默认的测光方式。中央重点测光主要是考虑到一般摄影者习惯将拍摄主体也就是需要准确曝光的东西放在取景器的中间，所以这部分拍摄内容是最重要的。因此中央部分的测光数据占据绝大部分比例，而画面中央以外的测光数据作为小部分比例起到测光的辅助作用。经过相机的处理器对这两格数值加权平均之后的比例，得到拍摄的相机测光数据。中央部分测光占据整个测光的比各家品牌不同而有所差别。适用个人旅游照片，特殊风景照片等。

##### 2. 局部测光

局部测光方式是对画面的某一局部进行测光。当被摄主体与背景有着强烈明暗反差，而且被摄主体所占画面的比例不大时，运用这种测光方式最合适，在舞台、演出、逆光等场景中这种模式最为合适。如中央部分测光，只对画面中央的一块区域进行测光，测光范围大约是百分之三至百分之十二进行测光。局部测光比第一种测光方式准确，又不象点测光方式那样由于测光点太狭小需要一定测光经验才不容易失误。

##### 3. 点测光

点测光的范围是以观景窗中央的一极小范围区域作为曝光基准点，大多数点测相机的测光区域为百分之一至百分之三，相机根据这个较窄区域测得的光线，作为曝光依据。这是一种相当准确的测光方式，但对于新手来说，却不那么好掌握。点测光的技巧可以在微距拍摄时大放光彩，这样可以让微距部分曝光更加准确。点测光在人像拍摄时也是一个好武器，可以准确的对人物局部（例如脸部、甚至是眼睛）进行准确的曝光。

##### 4. 评价测光（或称分割测光/矩阵测光/多分区测光）

是一种比较新的测光技术，测光方式与中央重点测光最大的不同就是评价测光（或称分割测光）将取景画面分割为若干个测光区域，每个区域独立测光后在整体整合加权计算出一个整体的曝光值。主要用途有：团体照片，家庭合影，一般的风景照片等。

#### 3-3 Camera2 结构简介

Camera2 主要有 5 个类：

- CameraManager

摄像设备（CameraDevice）的管理器，操作的原始发起者，由它来创建 CameraDevice 对象，CameraCharacteristics 对象等等；并检测摄像头，打开系统摄像头；调用`CameraManager.getCameraCharacteristics(String)`可以获取指定摄像头的相关特性。

- CameraDevice

相机设备，相当于原 Camera 对象，但需要创建其他对象来进一步操作。

- CameraCharacteristics

摄像头设备的属性类，与原来的 CameraInfo 有相似性，由 CameraManager 管理。 每个不同的 Camera Device 都包含有关于这个设备的一些特性参数，比如输出图像的大小，是否支持闪光灯等信息，这些信息都通过键值对的形式储存在 CameraCharacteristics 对象中，根据每只 Camera Device 的 Id 获取，CameraCharacteristics 对象是只读的，即只可从中读取属性，不能用来设置属性。

- CameraCaptureSession

由 cameraDevice 创建，用来控制摄像头的预览或者拍照。包含`setRepeatingRequest()`和`capture ()`方法，它们都是向相机设备发送的获取图像的请求，`capture()` 为获取一次，即单张拍摄；而`setRepeatingRequest()`则是不断获取图像数据，所以连拍时调用它（注：图像的预览也是用的`setRepeatingRequest()`，只是无需处理返回数据）。

- CameraRequest 和 CameraRequest.builder

CameraRequest 表示一次捕获请求，CameraRequest.Builder 用来生成 CameraRequest 对象，由 cameraDevice 创建，用于设置拍摄属性，如：预览分辨率，预览目标，对焦模式、曝光模式、其他相机属性，照片属性等等。每个 Builder 可以设置不同的属性，即预览和拍照可分别设置。

要预览或者获得图像，必须要将图像投射到一个 surface 对象上，通常预览目标使用 SurfaceView 或者 TextureView；保存 JPG 或 RAW 时用 ImageReader，还有其他的目标用于视频，YUV 格式，RenderScrip 或 OpenGL 处理。

Camera 2 其他注意事项

- 权限问题：Camera2 的使用权限，5.0 的 Android 设备在配置文件申明即可，6.0 以上的系统需要考虑动态权限问题。
- 必须正确设置预览的分辨率的尺寸，使其符合相机的输出大小、格式，才能成功预览。 Preview 的尺寸必须低于屏幕最高分辨率，也必须是摄像头支持的分辨率。

#### 3-4 Camera 1 & 2 使用步骤对比

- Camera 1 一般使用步骤

1. 调用`Camera.open()`打开相机，默认为后置摄像头，可使用摄像头 ID 来指定所使用的摄像头；
2. 调用`Camera.getParameters()`得到一个`Camera.Parameters`对象，对拍照参数进行设置；
3. 调用`Camera.setPreviewDispaly(SurfaceHolder holder)`，指定显示预览的 SurfaceView，并调用`Camera.startPreview()`方法开始预览取景；
4. 调用`Camera.takePicture()`方法进行拍照；
5. 拍照结束后，调用`Camera.stopPreview()`结束取景预览，再`replease()`释放资源。

- Camera 2 一般使用步骤

1. 获得摄像头管理器 CameraManager mCameraManager，调用`mCameraManager.openCamera()`打开指定摄像头，创建并指定 `openCamera()`所需要的 CameraDevice.StateCallback；
2. 在 CameraDevice.StateCallback 中调用`takePreview()`方法，使用 CaptureRequest.Builder 创建预览需要的 CameraRequest，并初始化 CameraCaptureSession，最后调用了`setRepeatingRequest(previewRequest, null, childHandler)`进行预览；
3. 点击屏幕，调用`takePicture()`方法，其内部调用了`capture(mCaptureRequest, null, childHandler)`进行拍照；
4. 在`new ImageReader.OnImageAvailableListener(){}`回调方法中，对拍摄得到的图片进行处理（保存或展示等）。

#### 3-5 ZSL 模式（Zero Shutter Lag）

*（To do: 有空写个 Demo 测几组性能数据看看）*

中文名称为零延时拍照，是为了减少拍照延时，让拍照 & 回显瞬间完成的一种技术。

Single Shot

当开始预览后，sensor 和 VFE 会产生 preview 和 snapshot 帧, 而最新的 snapshot 帧数据会被存储在 buffer 中。当拍照被触发，系统计算实际的拍照时间，找出在 buffer 中的相应帧，然后返回帧到用户，这就是所谓的“ZERO”。

系统计算出 shutter lag 的时间，然后把某个帧认作是拍照实时的那帧数据。

ZSL 需要实现以下几点：

1. 一个 surfaceView 用于预览
2. 一个队列缓存 snapshot 的数据
3. 拍照动作获取队列某帧数据作为拍照数据输出
4. 输出的照片需要 YUV -> JPEG 数据的转码

首先说一下 ZSL 功能在 android 4.4 和 android 5.0 上实现的区别。

Android 4.4 的实现对于 2 步和 3 步都是在 HAL 层（硬件抽象层 Hardware Abstraction Layer）实现，HAL 层在维护缓存队列，当接收倒 take_picture 命令时直接取得某帧缓存数据，进行转码，然后以正常拍照的流程利用 @link android.hardware.Camera.PictureCallback 通知应用层拍照的数据。

Android 5.0 的实现对于 2 步和 3 步都是在应用层实现，应用层在启动预览时给 HAL 层传递 2 个 surface 给 HAL 层，HAL 层利用其中一个 surface 用于预览数据填充，一个 surface 用于填充 snapshot 的数据填充。应用层不断读取 surface 中 snapshot 的数据去维护一个缓存队列，当用户执行 take_picture，读取缓存队列的数据作为拍照数据。

------

**作者简介：**opalli(李科慧)，天天P图 Android 高级工程师

> 文章后记： 天天P图是由腾讯公司开发的业内领先的图像处理，相机美拍的APP。欢迎扫码或搜索关注我们的微信公众号：“天天P图攻城狮”，那上面将陆续公开分享我们的技术实践，期待一起交流学习！