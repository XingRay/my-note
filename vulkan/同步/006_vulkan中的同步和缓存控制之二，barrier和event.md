# vulkan中的同步和缓存控制之二，barrier和event

## Introduction

近些日子忙着工作上的事情，阅读SPEC和做笔记的时间变得少了，专栏也迟迟没有更新。现在稍微闲暇了下来，似乎也到了该还债的时候了。

这篇接着很早之前的一篇，主要还是关注vulkan中的同步和缓存控制。这里我们先介绍了[barrier](https://zhida.zhihu.com/search?content_id=106061125&content_type=Article&match_order=1&q=barrier&zhida_source=entity)的概念，并且展示了两种不同的barrier, [execution barrier](https://zhida.zhihu.com/search?content_id=106061125&content_type=Article&match_order=1&q=execution+barrier&zhida_source=entity)和memory brrier。随后，引入了event的概念。本文最后，我将列出这篇文章的参考资料和一些额外的阅读资料，方便大家理解和学习。

## Barrier

Barrier是**同一个queue**中的command，或者**同一个subpass**中的command所明确指定的依赖关系。barrier的中文释义一般叫栅栏或者屏障，我们可以想象一下有一大串的command乱序执行（实际上可能是顺序开始，乱序结束），barrier就是在中间树立一道栅栏，要求栅栏前后保持一定的顺序。

[vkCmdPipelineBarrier(3) ](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/man/html/vkCmdPipelineBarrier.html)API可以用于创建一个Pipeline中的Barrier。注意这个API与fence/semaphore的不同，这个API的前缀是`vkCmd`，这意味着这是一个向command buffer中记录命令的API：

```c
void vkCmdPipelineBarrier(
    VkCommandBuffer                             commandBuffer,
    VkPipelineStageFlags                        srcStageMask,
    VkPipelineStageFlags                        dstStageMask,
    VkDependencyFlags                           dependencyFlags,
    uint32_t                                    memoryBarrierCount,
    const VkMemoryBarrier*                      pMemoryBarriers,
    uint32_t                                    bufferMemoryBarrierCount,
    const VkBufferMemoryBarrier*                pBufferMemoryBarriers,
    uint32_t                                    imageMemoryBarrierCount,
    const VkImageMemoryBarrier*                 pImageMemoryBarriers);
```

第一个参数`command buffer`就是这个命令将要被记录的command buffer。pipe line barrier涉及到两个同步范围，这两个同步范围所处的stage分别由`srcStageMask`和`dstStageMask`指明，注意着两个都是mask，所以每一个都可以设置多个阶段。关于各个stage后续有机会我再介绍。`dependencyFlags`是个比较高级的使用方法，我们这里不多介绍，后续有机会我会开专门的文章来尝试理解。

然后就是由vulkan API一贯味道的三个数组了 ，分别是`memoryBarrier` bufferMemoryBarrier`以及最后的`ImageMemoryBarrier。当着三个数组都为空的时候，将会在当前执行环境创建一个Execution Barrier，否则，则创建一个Memory Barrier。

### Execution Barrier

Execution Barrier就是简单的执行屏障。我们这里举一个来自于khronos官方的例子。

```text
vkCmdDispatch(...);

vkCmdPipelineBarrier(
    ...
    VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, // srcStageMask
    VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, // dstStageMask
    ...);
    
vkCmdDispatch(...);
```

两个dispatch中，如果涉及到执行的先后顺序，就需要一个execution barrier。如果没有这个barrier，那么这两个dispatch先后顺序是无法预测的。很可能的情况是，第一个dispatch开始后，第二个dispatch也马上开始。至于谁先结束，无法预测。

Execution Barrier可以有效控制queue中的command或者subpass中的command的执行顺序。

这个示例使用了vulkan中的compute path，毕竟这条路比graphic那条路简单了许多。对于compute path而言，可能的stage只有top, indirect, compute/transfer, bottom。如果是graphic path，可能的stage就多了去了。后续有机会我们会再介绍这个。现在只有理解个大概就可以了。

### 缺点

Execution Barrier只保证了执行的顺序，对于存储修改的顺序，execution barrier无能为力。

例如，在典型的Read-After-Write的问题中，我们让一个dispatch写入一个resource，随后在下一个dispatch中读取这个resource。如果没有execution barrier，就很可能会出现第一个dispatch正在执行的时候，第二个dispatch也已经开始执行的情况了。这样一来，第二个dispatch所读取的resource的内容就无法保证了。

为了保证这两个dispatch之间的顺序，我们可以在中间插入一个barrier。最开始，我们简单滴插入一个execution barrier。这样一来，只有当第一个dispatch执行完成后，第二个dispatch才能开始执行。execution barrier可以正确保证执行的顺序。然而，这就够了吗？

对于理想模型而言，第一个dispatch执行完毕，更新resource的内容。等到第二个dispatch开始的时候，第二个dispatch可以立即获取resource中更新后的结果。然而，事实上，由于现代GPU同样采取了复杂的缓存控制机制，这个理想的模型是不存在的。一种可能的结果是，第一个dispatch执行完毕后, resource最新的内容被缓存到了某一级cache中。不幸的是，第二个dispatch开始执行的时候，这个cache对第二个dispatch不可见（例如，两个dispatch被分派到了不同的执行单元中）。尽管从顺序上，这的确保证了第一个dispatch先执行，然后才是第二个dispatch，但是我们仍然无法保证第二个dispatch能够看到第一个dispatch更新后的结果。

究其原因，就是因为execution barrier只能保证执行上的顺序，而无法保证对于存储操作的顺序。在多核CPU中我们同样也能看到这样的问题存在，有兴趣的读者可以去翻一下我的专栏，了解多核CPU同步原语的背后思想。为了解决execution barrier无法控制存储的缺点，我们需要引入新的barrier，即[memory barrier](https://zhida.zhihu.com/search?content_id=106061125&content_type=Article&match_order=1&q=memory+barrier&zhida_source=entity)。

### Memory Barrier

memory barrier是一种更严格意义上的barrier。一个memory barrier同时兼备了execution barrier语义。memory barrier的引入主要是为了解决execution barrier中，无法有效控制缓存的缺点。我们当然可以选择让所有的execution barrier天然具有memory barrier语义，这样一来，所有的execution barrier实际上都变成了memory barrier。但是粗暴的设计无法让我们精确地获取所需要的极致性能。

例如，我们有一个Write-After-Read的情况。第一个dispatch先读取一个resource的情况，接下来是第二个dispatch来更新这个resource。如果设计上要求所有的execution barrier都需要具有缓存同步的语义，那么情况就变成了这个样子。第一个dispatch读取resource，然后flush修改到一个合适的部分(可能是GPU全局可见的某一级cache，或者是global memory)，然而第二个dispatch才能开始执行。这个额外增加的flush动作，实际上没有flush任何数据，因为第一个dispatch对resource的访问时只读访问，没有修改任何数据。这里，我们只需要一个不带有缓存同步的barrie就可以了。

在Pipeline Barrier API中，可以指定三个数组。这三个数组，分别定义了不同类型的memory barrier:

- 全局memory barrier
- buffer上的memory barrier
- image上的memory barrier

全局memory barrier只有src的访问mask和dst的访问mask，因此作用于当前所有的resource。需要具体操纵某个resource的时候，根据resource的类型，分别使用buffer或者image的memory barrier.

### 再思考，fence和semaphore是否带有缓存同步语义

既然barrier分为普通的execution barrier和加强了缓存同步控制的memory barrier，我们在之前所介绍的粗粒度的fence和semaphore是否带有缓存同步语义呢？答案是显然的，因为实际上我们没有看到任何为fence和semaphore指明缓存控制的额外参数，因此fence和semaphore必须带有缓存同步的语义。这也是为何fence和semaphore被称为**粗粒度**同步的原因所在了。semaphore就是我们想要始终具有缓存同步语义的barrier

## Event

Barrier，类比于CPU同步多核原语中的barrier（好像术语都一样）。barrier为乱序执行树立一道屏障，屏障之前和之后必须保证执行（和缓存可见性）顺序。

类比于CPU同步多核原语，除了树立一道屏障以外，我们在CPU中还可以将这道屏障拆成两部分，即CPU中的release-acquire。在GPU中，我们可以使用event来达到这样的效果。除了在同一个queue内部使用外，event还可以用在host和一个queue中做同步。

注意，event不能用于不同queue之间同步。

### 创建一个Event

一个event，基本上和semaphore或者fence一样，由host创建，API为[vkCreateEvent(3)](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/man/html/vkCreateEvent.html)：

```c
VkResult vkCreateEvent(
    VkDevice                                    device,
    const VkEventCreateInfo*                    pCreateInfo,
    const VkAllocationCallbacks*                pAllocator,
    VkEvent*                                    pEvent);
```

创建event基本不需要额外的信息，并且在host端使用event也非常简单明了，比较复杂的是如何在device端使用event。

### Event支持的操作

device上可以使用[vkCmdSetEvent(3)](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/man/html/vkCmdSetEvent.html)触发(set)一个event，可以使用[vkCmdResetEvent(3)](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/man/html/vkCmdResetEvent.html)重置一个event，还可以使用[vkCmdWaitEvents(3)](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/man/html/vkCmdWaitEvents.html)等待一个event被触发。其中，WaitEvents有着和barrier极为类似的设计，可以支持缓存控制。

host上可以使用[vkSetEvent(3)](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/man/html/vkSetEvent.html)触发event，也可以使用[vkResetEvent(3)](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/man/html/vkResetEvent.html)重置一个Event。如果host上需要等待event，需要使用[vkGetEventStatus(3)](https://www.khronos.org/registry/vulkan/specs/1.1-extensions/man/html/vkGetEventStatus.html)来查询状态

## 一些感想

Vulkan实在是太繁琐了。仅仅是同步原语就提供了四种，每种还有不同的用途。从vulkan的API设计中，我们也能对未来的高性能计算的前途初窥端倪。在后摩尔时代，软件的粗狂式发展趋势必将衰落。以前软件写的烂不要紧，过了十八个月，硬件的提升后，以前跑不起来的application也能运行的飞起。然而，现在的硬件发展似乎的的确确约到了瓶颈。为了尽可能压榨硬件的性能，软件必须要精耕细作，充分利用好硬件所能提供的每一分计算能力。这也必然要求软件开发者对硬件有一定的了解才行，所以一定程度上也会提高软件开发入门的门槛。

面对这样的挑战，另外的做法就是组织一批了解硬件的软件工程师，为上层软件提供SDK等。上层开发不需要了解底层细节，无脑调用SDK就是了。核弹厂已经证明这种模式的优越性：开发者友好的同时，还能保持性能的优势。不知道其他厂商也能够跟进。

## 参考资料

这里有一些延伸的阅读资料，有兴趣的读者可以自行查阅。

首先，自然是Khronos的官方规范了：

[Vulkan® 1.1.121 - A Specificationwww.khronos.org/registry/vulkan/specs/1.1/html/chap6.html#synchronization](https://www.khronos.org/registry/vulkan/specs/1.1/html/chap6.html%23synchronization)

这是一篇非常新的关于vulkan中同步机制的总结：

[Yet another blog explaining Vulkan synchronizationthemaister.net/blog/2019/08/14/yet-another-blog-explaining-vulkan-synchronization/](http://themaister.net/blog/2019/08/14/yet-another-blog-explaining-vulkan-synchronization/)

Khronos也在官方Github上提供了一些同步机制的使用例子：

[![img](./assets/v2-143fec8f0f9e23987e6b40f3ba3e9720_ipico.jpg)KhronosGroup/Vulkan-Docsgithub.com/KhronosGroup/Vulkan-Docs/wiki/Synchronization-Examples](https://github.com/KhronosGroup/Vulkan-Docs/wiki/Synchronization-Examples)

