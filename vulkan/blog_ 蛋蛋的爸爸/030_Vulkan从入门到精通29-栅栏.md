# Vulkan从入门到精通29-栅栏

Vulkan原生支持以[并行方式](https://zhida.zhihu.com/search?content_id=190349580&content_type=Article&match_order=1&q=并行方式&zhida_source=entity)异步运行。在程序的各个执行点，需要使主机和设备的各个单位保持同步。Vulkan中的同步是通过使用多个同步原语实现的。常见的有三种[同步原语](https://zhida.zhihu.com/search?content_id=190349580&content_type=Article&match_order=2&q=同步原语&zhida_source=entity)类型:

- Fence - 栅栏 --- 用于等待设备完成大量数据的提交工作
- Event - 事件 --- 一个[细粒度](https://zhida.zhihu.com/search?content_id=190349580&content_type=Article&match_order=1&q=细粒度&zhida_source=entity)的同步原语，可由主机或者设备发出，当设备发出信号时，可以在[命令缓冲区](https://zhida.zhihu.com/search?content_id=190349580&content_type=Article&match_order=1&q=命令缓冲区&zhida_source=entity)中通知他，并可在管线的特定点上由设备等待他。
- Semaphore - [信号量](https://zhida.zhihu.com/search?content_id=190349580&content_type=Article&match_order=1&q=信号量&zhida_source=entity) ----- 用于控制设备上不同队列对资源的所有权。用于同步不同队列上可能的异步工作。

本文主要来介绍栅栏。

栅栏是中等量级的同步原语，通常借助操作系统来完成。当执行提交[类函数](https://zhida.zhihu.com/search?content_id=190349580&content_type=Article&match_order=1&q=类函数&zhida_source=entity)时，函数返回并不意味着执行结束，需要待栅栏状态变更后，才意味着函数执行结束。因此栅栏发生在CPU端。

在vulkan中创建栅栏是通过调用vkCreateFence来完成的，原型如下

```cpp
VkResult vkCreateFence(
    VkDevice                                    device,
    const VkFenceCreateInfo*                    pCreateInfo,
    const VkAllocationCallbacks*                pAllocator,
    VkFence*  
```

除了调用，其他几个相关函数有

```cpp
void vkDestroyFence(
    VkDevice                                    device,
    VkFence                                     fence,
    const VkAllocationCallbacks*                pAllocator);

VkResult vkResetFences(
    VkDevice                                    device,
    uint32_t                                    fenceCount,
    const VkFence*                              pFences);

VkResult vkGetFenceStatus(
    VkDevice                                    device,
    VkFence                                     fence);

VkResult vkWaitForFences(
    VkDevice                                    device,
    uint32_t                                    fenceCount,
    const VkFence*                              pFences,
    VkBool32                                    waitAll,
    uint64_t                                    timeout);
```

销毁和创建对应，vkResetFances用于重置栅栏为初始化状态，vkGetFenceStatus用于获取状态，而vkWaitForFences则为等待状态改变。vkWaitForFences要么等待所有栅栏信号都变成有信号状态，要么在任一有信号时返回（由waitAll控制）。由于此函数可能会等待很长时间，所以需要设置超时参数。

一个简单的栅栏使用样例如下

```cpp
    VkFenceCreateInfo fenceInfo;
    VkFence fence;
    fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    fenceInfo.pNext = NULL;
    fenceInfo.flags = 0;
    vkCreateFence(device, &fenceInfo, NULL, &fence);

    VkSubmitInfo submit_info[1] = {};
    submit_info[0].pNext = NULL;
    submit_info[0].sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
    submit_info[0].waitSemaphoreCount = 0;
    submit_info[0].pWaitSemaphores = NULL;
    submit_info[0].pWaitDstStageMask = NULL;
    submit_info[0].commandBufferCount = 1;
    submit_info[0].pCommandBuffers = cmd_bufs;
    submit_info[0].signalSemaphoreCount = 0;
    submit_info[0].pSignalSemaphores = NULL;

    res = vkQueueSubmit(queue, 1, submit_info, fence);
    assert(res == VK_SUCCESS);

    do {
        res = vkWaitForFences(device, 1, &fence, VK_TRUE, FENCE_TIMEOUT);
    } while (res == VK_TIMEOUT);
    assert(res == VK_SUCCESS);

    vkDestroyFence(device, fence, NULL);
```

调用vkQueueSubmit提交数据后，通过调用vkWaitForFences等到提交完成