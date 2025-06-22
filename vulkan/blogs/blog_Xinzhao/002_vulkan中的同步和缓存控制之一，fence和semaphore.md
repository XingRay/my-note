# vulkan中的同步和缓存控制之一，fence和semaphore

## Introduction

这部分内容主要是vulkan的[spec](https://link.zhihu.com/?target=https%3A//github.com/KhronosGroup/Vulkan-Docs)的第六章部分的一些阅读笔记。

对于同一个资源的访问之间的同步是AP应负责的内容之一，vulkan中一共提供了如下四种同步机制：

1. Fence
2. Semaphore
3. Event
4. Barrier

用以同步host/device之间，queues之间，queue submissions之间，以及一个单独的command buffer的commands之间的同步。

## Fence

首先我们介绍最简单的[Fence](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/VkFence.html)。一句话总结，Fence提供了一种粗粒度的，从Device向Host单向传递信息的机制。Host可以使用Fence来查询通过[vkQueueSubmit](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkQueueSubmit.html)/[vkQueueBindSparse](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkQueueBindSparse.html)所提交的操作是否完成。简言之，在[vkQueueSubmit](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkQueueSubmit.html)/[vkQueueBindSparse](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkQueueBindSparse.html)的时候，可以附加带上一个Fence对象。之后就可以使用这个对象来查询之前提交的状态了。例如：

```c
VkResult vkQueueSubmit(
    VkQueue                                     queue,
    uint32_t                                    submitCount,
    const VkSubmitInfo*                         pSubmits,
    VkFence                                     fence);
```

的原型中，最后一个参数可以是一个有效的fence对象，当然，也可以指定为VK_NULL_HANDLE，标明不需要Fence。有趣的是，在[vkQueueSubmit](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkQueueSubmit.html)的时候，如果给定一个有效的fence对象，但是不提交任何信息，即submitCount为0，那么同样也可以算作一次成功的提交，等待**之前所有**提交到queue的任务都完成后，这个fence也就signaled了。这种使用方式提供了一种机制，可以让我们查询一个queue现在到底忙不忙。

Fence本身只有两种状态，unsignaled或者signaled，大致可以认为fence是触发态还是未触发态。当使用[vkCreateFence](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkCreateFence.html)创建fence对象的时候，如果在标志位上填充了[VkFenceCreateFlagBits(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/VkFenceCreateFlagBits.html)的VK_FENCE_CREATE_SIGNALED_BIT，那么创建出来的fence就是signaled状态，否则都是unsignaled状态的。销毁一个fence对象需要使用[vkDestroyFence(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkDestroyFence.html)。

伴随着[vkQueueSubmit](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkQueueSubmit.html)/[vkQueueBindSparse](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkQueueBindSparse.html)一起提交的fence对象，可以使用[vkGetFenceStatus(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkGetFenceStatus.html)来查询fence的状态。注意vkGetFenceStatus是非阻塞的，如果fence处于signaled状态，这个API返回VK_SUCCESS，否则，立即返回VK_NOT_READY。

当然，fence被触发到signaled状态，必须存在一种方法，将之转回到unsignaled状态，这个功能由[vkResetFences(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkResetFences.html)完成，这个API一次可以将多个fence对象转到unsignaled状态。这个API结合VK_FENCE_CREATE_SIGNALED_BIT位，可以达到一种类似于C中do {} while;的效果，即loop的代码有着一致的表现：loop开始之前，所有的fence都创建位signaled状态，每次loop开始的时候，所用到的fence都由这个API转到unsignaled状态，伴随着submit提交过去。

等待一个fence，除了使用vkGetFenceStatus轮询之外，还有一个API [vkWaitForFences(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkWaitForFences.html)提供了阻塞式地查询方法。这个API可以等待一组fence对象，直到其中至少一个，或者所有的fence都处于signaled状态，或者超时（[时间限制](https://zhida.zhihu.com/search?content_id=2113688&content_type=Article&match_order=1&q=时间限制&zhida_source=entity)由参数给出），才会返回。如果超时的时间设置为0，则这个API简单地看一下是否满足前两个条件，然后根据情况选择返回VK_SUCCESS，或者（虽然没有任何等待）VK_TIMEOUT。

简而言之，对于一个fence对象，Device会将其从unsignaled转到signaled状态，告诉Host一些工作已经完成。所以fence使用在Host/Device之间的，且是一种比较粗粒度的同步机制。



## Semaphore

[VkSemaphore(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/VkSemaphore.html)用以同步不同的queue之间，或者同一个queue不同的submission之间的执行顺序。类似于fence，semaphore也有signaled和unsignaled的状态之分。然而由于在queue之间或者内部做同步都是device自己控制，所以一个semaphore的初始状态也就不重要了。所以，[vkCreateSemaphore(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkCreateSemaphore.html)就简单地不用任何额外参数创建一个semaphore对象，然后[vkDestroySemaphore(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkDestroySemaphore.html)可以用来销毁一个semaphore对象。不同于fence,没有重置或者等待semaphore的api，因为semaphore只对device有效。

在device上使用[semaphore](https://zhida.zhihu.com/search?content_id=2113688&content_type=Article&match_order=7&q=semaphore&zhida_source=entity)的最典型的场景，就是通过[vkQueueSubmit](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/vkQueueSubmit.html)提交command buffer时候，所需要的参数[VkSubmitInfo(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/VkSubmitInfo.html)中的控制，

```c
typedef struct VkSubmitInfo {
    VkStructureType                sType;
    const void*                    pNext;
    uint32_t                       waitSemaphoreCount;
    const VkSemaphore*             pWaitSemaphores;
    const VkPipelineStageFlags*    pWaitDstStageMask;
    uint32_t                       commandBufferCount;
    const VkCommandBuffer*         pCommandBuffers;
    uint32_t                       signalSemaphoreCount;
    const VkSemaphore*             pSignalSemaphores;
} VkSubmitInfo;
```

可以看出，在[VkSubmitInfo(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/VkSubmitInfo.html)，实际上存在三组array结构，他们分别是

- 由waitSemaphoreCount决定长度，pWaitSemaphores决定起始地址的wait semaphore数组
- 由waitSemaphoreCount决定长度，pWaitDstStageMask决定起始地址的semaphore等待阶段的数组
- 由signalSemaphoreCount决定长度，pSignalSemaphores决定起始地址的signal semaphore数组

通过[VkSubmitInfo(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/VkSubmitInfo.html)中的这三组数组，可以达到如下效果：所提交的command buffer将在执行到每个semaphore等待阶段时候，检查并等待每个对应的wait semaphore数组中的semaphore是否被signal, 且等到command buffer执行完毕以后，将所有signal semaphore数组中的semaphore都signal起来。



[VkSubmitInfo(3)](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.0/man/html/VkSubmitInfo.html)通过这种方式，实际上提供了一种非常灵活的同步queue之间或者queue内部不同command buffer之间的方法，通过组合使用semaphore，AP可以显式地指明不同command buffer之间的资源依赖关系，从而可以让driver在遵守这个依赖关系的前提下，最大程度地[并行化](https://zhida.zhihu.com/search?content_id=2113688&content_type=Article&match_order=1&q=并行化&zhida_source=entity)，以提高GPU的利用效率。

类似于Fence, Semaphore也是一种较粗粒度的同步机制。在大多数时候这两个同步机制已经足够使用了。然而，如果需要在更细的粒度上控制同步，就需要使用Event了。

我们将在另外后续文章中介绍Event。