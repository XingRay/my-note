# Vulkan学习资料汇总

到今天，vulkan正式发布基本上快一年了。作为新一代的图形API，vulkan的前景是非常明朗的。然而不可否认的一点是，vulkan设计的初衷之一是尽量减少driver层面上去揣度AP的情况。驱动不再去猜测应用程序究竟想要做什么，那么就必须由应用程序来显式地告诉驱动它需要的一切。这样带来的后果就是核心概念和代码量相对于OpenGL的显著增加。尤其是一些[图形概念](https://zhida.zhihu.com/search?content_id=2105975&content_type=Article&match_order=1&q=图形概念&zhida_source=entity)的引入，例如command buffer和queue等，更是让人无从下手。甚至于出现了[茴香豆](https://zhida.zhihu.com/search?content_id=2105975&content_type=Article&match_order=1&q=茴香豆&zhida_source=entity)的几种写法式的同步机制，memory barrier/semaphore/event/fence，真的是望而生畏。

不过这一年来vulkan的发展也非常迅速，很多的学习资料纷纷出现。然而，令人遗憾的是，[中文社区](https://zhida.zhihu.com/search?content_id=2105975&content_type=Article&match_order=1&q=中文社区&zhida_source=entity)目前而言并没有比较出彩的相关信息。因此，本部分主要汇总了英文的学习资料，在后续会尝试以翻译外文资料，或者其他的方式，来尽量为中文的vulkan资料添砖加瓦，抛砖引玉。

闲话少叙，首先介绍的自然是khronos关于vulkan的spec，地址如下：[KhronosGroup/Vulkan-Docs](https://github.com/KhronosGroup/Vulkan-Docs)。vulkan的spec采用了开放的方式，托管在github上，因此如果有了更新可以第一时间获取更新的内容，带来的不便之处在于我们就需要自己来生成相关的文档了。如果懒的自己搞，可以去Khronos的官网，[下载vulkan的spec](https://www.khronos.org/registry/vulkan/)。

第二个要介绍的就是vulkan开发使用的SDK，LunarG，官网参见[LunarXchange](https://vulkan.lunarg.com/)。LunarG提供了vulkan开发所需要的基本的工具，包括glslang，spir-v等。此外，安装完LunarG之后，在[安装目录](https://zhida.zhihu.com/search?content_id=2105975&content_type=Article&match_order=1&q=安装目录&zhida_source=entity)下还有一个sample/API-Samples文件夹，里边有一些关于vulkan API等sample，用来学习vulkan的API应该是非常不错的。

此外，Intel公司的Pawel Lapinski也有一系列关于，项目地址在[GameTechDev/IntroductionToVulkan](https://github.com/GameTechDev/IntroductionToVulkan)。难能可贵的是，每一个示例都有着详细的文档介绍，包括一些背景概念的补全。如果是初次学习vulkan的话，强烈建议从这个工程开始学习。到今天为止，已经有了7个示例，并且前五个都有了详细的文档介绍。

Github上还有许多关于vulkan的优秀的内容，例如[SaschaWillems/Vulkan](https://github.com/SaschaWillems/Vulkan)，这里边介绍了很多vulkan的概念对应的示例，可以作为进阶的素材。另外，[vulkan-tutorial网站](https://vulkan-tutorial.com/)也是一个非常不错的用以入门vulkan的学习资料。

虽然这里介绍了三四个非常优秀的示例程序库，不过鉴于vulkan目前时日尚短，并没有一种比较好的使用框架，所介绍的示例代码都是在vulkan简单的api上做一层封装而已。所以，还是建议先从intel的API-without secret的介绍入手，等待熟悉了相关的概念之后，就可以边啃官方的spec，边研究其他的示例代码了。优秀的vulkan应用框架，目前而言就只能静待时日了。

除了上述的示例代码外，关于vulkan技术的讨论，主要有[Vulkan Archives - GPUOpen](http://gpuopen.com/tag/vulkan/)，Nvidia/Intel/AMD，以及Khronos的官方网站。

在知乎上也有许多关于vulkan的优秀资料，不过总的来说，入门的资料还是比较少，大部分是一些比较细的topic的讨论，例如

[@文刀秋二](https://www.zhihu.com/people/59827f0a435d7b054c8339422dde6773)

大大的[Vulkan - 高性能渲染](https://zhuanlan.zhihu.com/p/20712354#)! 这方面的内容可能需要使用vulkan一段时间之后再来看，才会有更加深刻的体悟。



此外，关于vulkan， 

[@Vinjn张静](https://www.zhihu.com/people/0effe9e423faad125fa9c63418dd288a)

 大大也在[Github](https://github.com/vinjn/awesome-vulkan)上有一个非常全面的资料汇总，项目名称awesome-vulkan，主页在[这里](http://vinjn.github.io/awesome-vulkan/)。有志于深入研究vulkan的童鞋切不可错过这份优质汇总。





2018年6月1日儿童节更新：

Github上现在Vulkan相关的项目越来越多了，找资料再也不像一年前那样难了。其中有一个[Vulkan-Cookbook](https://github.com/PacktPublishing/Vulkan-Cookbook)的repo，汇总了一本Vulkan CookBook书中的相关代码，有兴趣的可以去研究一下。另外，这本书网上也是能搜到电子版的，大概扫了一眼，排版质量比较差，也不知道怎么排的，看起来还没有Vulkan的[specification](https://zhida.zhihu.com/search?content_id=2105975&content_type=Article&match_order=1&q=specification&zhida_source=entity)舒服。





https://www.vinjn.com/awesome-vulkan/

