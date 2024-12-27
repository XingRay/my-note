# Vulkan Tutorial ： 基本介绍

## 前言

笔者目前已经基本掌握OpenGL这个古老但又经典的图形API，工作方面会慢慢开始接触Vulkan，因此会记录一些学习笔记。有一个比喻非常贴切，如果说OGL是自动挡汽车，那么Vulkan就是手动挡汽车，作为驾驶员，如果你只是希望上下班代步，那你完全选择OGL就可以了。但是如果你想玩漂移，玩飙车，你就得试一试手动挡的汽车。

网络上有一个很好的教程

[Introduction - Vulkan Tutorial](https://link.zhihu.com/?target=https%3A//vulkan-tutorial.com/)

我会按照自己的理解意译上文，然后添加自己认知上的备注，以供后来中文母语者学习Vulkan做参考。

## 正文

### 概览

这个教程会教会你Vulkan的图形和计算API的基础内容。Vulkan是由Khronos group(因OpenGL而闻名遐迩)组织定义的崭新API，他提供了基于现代GPU显卡更好的抽象方式。这个新的API让你可以更加自由的调用GPU的功能完成自己想要完成的3D图形绘制，并且在这个过程中，会得到比OGL或者D3D更高的性能表现。

Vulkan的灵感来源于微软的Direct3D 12以及Apple的Metal。比起这两个巨头推出的类似APIs，Vulkan的优势是在于它可以快速的在不同的平台进行部署和使用，具体包括Windows，Linux以及Android这些平台。显然，在跨平台这一点上D3D12和Metal是逊色于Vulkan的。

**“当你想要得到什么的时候，你必须为其付出什么”**

当你希望使用Vulkan来获得更加卓越的性能，以及更加灵活的图像绘制控制，或者是看中了他的跨平台特性，那么意味着你不得不花更多的精力去了解和学习冗长(verbose)的APIs。这意味着非常多的图形APIs相关的细节控制必须要你去掌控，包括如何初始化一个frame buffer，或者对于buffers或者texture image的[内存管理](https://zhida.zhihu.com/search?content_id=223802177&content_type=Article&match_order=1&q=内存管理&zhida_source=entity)。这些原来由驱动层去做的事情，现在一个不漏的交给了Programer去掌控。Vulkan就像是C语言，他是遵守着“完全相信程序员”的原则的，这意味着Vulkan会做更少的参数检查，这样一方面提升了整体性能，但是一方面也非常考验编程者的水平。Vulkan的驱动层做了非常少的事情，绝大多数事情由开发者在application中去配置。

总结下上文的要点：**Vulkan不是所有人都有必要掌握和学习的**

他的目标人群是，对于“高性能的图形渲染或者计算”有研究热情，并且愿意花费更多的时间去学习的人！

如果你只是喜欢做游戏开发，而不是研究[计算机图形学](https://zhida.zhihu.com/search?content_id=223802177&content_type=Article&match_order=1&q=计算机图形学&zhida_source=entity)，也许OGL或者D3D是一个比Vulkan更加合适的选项。

当然，你也可以这样，使用一个底层调用Vulkan的[游戏引擎](https://zhida.zhihu.com/search?content_id=223802177&content_type=Article&match_order=1&q=游戏引擎&zhida_source=entity)，例如Unreal Engine 或者耳熟能详的 Unity。这些游戏引擎的底层是可以支持Vulkan的。什么？你是引擎的开发者？那么，你不得不去学Vulkan了！



**弄清楚你是否有继续学习Vulkan的必要之后，我们需要明确下学习这个教程的前置条件了(prerequisites)**

1. 一个支持Vulkan的显卡(NVIDIA,AMD,ETC)
2. 熟悉C++
3. 支持C++17的编译器(Visual Studio 2017+ GCC 7+ ETC)
4. 一些计算机图形学的知识

这个教程并没有假设你有OGL或者D3D的一些概念，但是还是要求你有一些计算机图形学的知识，这里并不会讲深层次的[数学原理](https://zhida.zhihu.com/search?content_id=223802177&content_type=Article&match_order=1&q=数学原理&zhida_source=entity)。如果需要补充知识的，可以推荐几本书

[Ray tracing in one weekend](https://link.zhihu.com/?target=https%3A//github.com/RayTracing/raytracing.github.io)

[Physically Based Rendering book](https://link.zhihu.com/?target=https%3A//www.pbr-book.org/)

你可以使用C，但是教程会使用C++，因为C++有更多丰富的库可以使用。如果你喜欢，你也可以使用rust。为了更好理解和跟随，教程会调用Vulkan 的orginal C API。

### 教程结构

首先我们会介绍下Vulkan是如何工作得到，我们接下来会一步步的去画一个最初的三角形。去绘制第一个三角形的每一步都很很小，这样可以慢慢的带着读者了解每一个 环节的作用。接下来我们将会使用Vulkan SDK 去搭建一个Vulkan的[开发环境](https://zhida.zhihu.com/search?content_id=223802177&content_type=Article&match_order=1&q=开发环境&zhida_source=entity)，还会使用到GLM的库去完成一些数学计算，并且也会使用GLFW来完成窗口的创建。教程会展示如何用Visual Studio在Windows上运行Vulkan，也会展示用GCC在Ubuntu上运行。

在那之后，我们将会声明所有关于Vulkan编程中的基础概念，这些概念编程者必须清楚，否则也无法完成一个三角形的绘制。每个章节都会严格的按照下述的几个环节进行叙述

1. 介绍一个新的概念，并且介绍提出这个概念的目的
2. 使用这个概念对应的API到你的程序中
3. 把这个概念抽象成一个helper functions

当然这只是一个教程所有涵盖的，如果需要更加深入的了解其中的概念，可以直接看Vulkan的spec，Vulkan是一个全新的APIs，里面也许会有错误，你甚至可以提交改动到Khronos的仓库中[GitHub - KhronosGroup/Vulkan-Docs: The Vulkan API Specification and related tools](https://link.zhihu.com/?target=https%3A//github.com/KhronosGroup/Vulkan-Docs)。

就像刚刚说的 Vulkan API是一个非常冗长的API，设计的冗长的原因是希望开发者可以拥有最大化的控制底层硬件的能力。这样就导致了，当你希望创建一个texture的时候，需要更多的时间和步骤。当然这个步骤是可以通过封装成helper function来完成的，这样第二次创建texture的时候就不必要再一次重复上述的步骤了。

每个章节都会有对应的示例代码还有注释，并且这个教程是完全开源的，可以得到社区的贡献。

[GitHub - Overv/VulkanTutorial: Tutorial for the Vulkan graphics and compute API](https://link.zhihu.com/?target=https%3A//github.com/Overv/VulkanTutorial)

