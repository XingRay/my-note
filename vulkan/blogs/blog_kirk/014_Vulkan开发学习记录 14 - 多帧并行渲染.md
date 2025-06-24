# Vulkan开发学习记录 14 - 多帧并行渲染

如果读者开启校验层后运行程序，观察应用程序的内存使用情况，可以发现我们的应用程序的内存使用量一直在慢慢增加。这是由于我们的drawFrame 函数以很快地速度提交指令，但却没有在下一次指令提交时检查上一次提交 的指令是否已经执行结束。也就是说CPU 提交指令快过GPU 对指令的处理速度，造成GPU 需要处理的指令大量堆积。更糟糕的是这种情况下，我们实际上对多个帧同时使用了相同的imageAvailableSemaphores 和renderFinishedSemaphores [信号量](https://zhida.zhihu.com/search?content_id=218606885&content_type=Article&match_order=1&q=信号量&zhida_source=entity)。

最简单的解决上面这一问题的方法是使用vkQueueWaitIdle 函数来等待上一次提交的指令结束执行，再提交下一帧的指令：

```cpp
void drawFrame() {
     vkQueuePresentKHR(presentQueue,&presentInfo);
     vkQueueWaitIdle(presentQueue);
}
```

但这样做，是对GPU 计算资源的大大浪费。图形管线可能大部分时间都处于空闲状态。为了充分利用GPU 的计算资源，现在我们扩展我们的应用程序，让它可以同时渲染多帧。 首先，我们在[源代码](https://zhida.zhihu.com/search?content_id=218606885&content_type=Article&match_order=1&q=源代码&zhida_source=entity)的头部添加一个常量来定义可以同时[并行处理](https://zhida.zhihu.com/search?content_id=218606885&content_type=Article&match_order=1&q=并行处理&zhida_source=entity)的帧数：

```cpp
const int MAX_FRAMES_IN_FLIGHT = 2;
```

为了避免同步干扰，我们为每一帧创建属于它们自己的[命令缓冲区](https://zhida.zhihu.com/search?content_id=218606885&content_type=Article&match_order=1&q=命令缓冲区&zhida_source=entity)、信号量和栅栏：

```cpp
std::vector<VkCommandBuffer> commandBuffers;

...

std::vector<VkSemaphore> imageAvailableSemaphores;
std::vector<VkSemaphore> renderFinishedSemaphores;
std::vector<VkFence> inFlightFences;
```

然后我们需要创建多个命令缓冲区。重命名`createCommandBuffer`为`createCommandBuffers`接下来，我们需要将命令缓冲区[向量](https://zhida.zhihu.com/search?content_id=218606885&content_type=Article&match_order=1&q=向量&zhida_source=entity)的大小调整为的大小`MAX_FRAMES_IN_FLIGHT`，将更改`VkCommandBufferAllocateInfo`为包含这么多命令缓冲区，然后将目标更改为我们的命令缓冲区向量：

```cpp
void createCommandBuffers() {
    commandBuffers.resize(MAX_FRAMES_IN_FLIGHT);
    ...
    allocInfo.commandBufferCount = (uint32_t) commandBuffers.size();

    if (vkAllocateCommandBuffers(device, &allocInfo, commandBuffers.data()) != VK_SUCCESS) {
        throw std::runtime_error("failed to allocate command buffers!");
    }
}
```

我们需要对createSyncObjects 函数进行修改来创建每一帧需要的所有对象：

```cpp
void createSyncObjects() {
    imageAvailableSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
    renderFinishedSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
    inFlightFences.resize(MAX_FRAMES_IN_FLIGHT);

    VkSemaphoreCreateInfo semaphoreInfo{};
    semaphoreInfo.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;

    VkFenceCreateInfo fenceInfo{};
    fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    fenceInfo.flags = VK_FENCE_CREATE_SIGNALED_BIT;

    for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        if (vkCreateSemaphore(device, &semaphoreInfo, nullptr, &imageAvailableSemaphores[i]) != VK_SUCCESS ||
            vkCreateSemaphore(device, &semaphoreInfo, nullptr, &renderFinishedSemaphores[i]) != VK_SUCCESS ||
            vkCreateFence(device, &fenceInfo, nullptr, &inFlightFences[i]) != VK_SUCCESS) {

            throw std::runtime_error("failed to create synchronization objects for a frame!");
        }
    }
}
}
```

在应用程序结束前，我们需要清除为每一帧创建的所有对象：

```cpp
void cleanup() {
    for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        vkDestroySemaphore(device, renderFinishedSemaphores[i], nullptr);
        vkDestroySemaphore(device, imageAvailableSemaphores[i], nullptr);
        vkDestroyFence(device, inFlightFences[i], nullptr);
    }

    ...
}
```

我们添加一个叫做currentFrame 的变量来追踪当前渲染的是哪一帧。 之后，我们通过这一变量来选择当前帧应该使用的信号量：

```cpp
uint32_t currentFrame = 0;
```

修改drawFrame函数，使用正确的信号量对象：

```cpp
void drawFrame() {
    vkWaitForFences(device, 1, &inFlightFences[currentFrame], VK_TRUE, UINT64_MAX);
    vkResetFences(device, 1, &inFlightFences[currentFrame]);

    vkAcquireNextImageKHR(device, swapChain, UINT64_MAX, imageAvailableSemaphores[currentFrame], VK_NULL_HANDLE, &imageIndex);

    ...

    vkResetCommandBuffer(commandBuffers[currentFrame],  0);
    recordCommandBuffer(commandBuffers[currentFrame], imageIndex);

    ...

    submitInfo.pCommandBuffers = &commandBuffers[currentFrame];

    ...

    VkSemaphore waitSemaphores[] = {imageAvailableSemaphores[currentFrame]};

    ...

    VkSemaphore signalSemaphores[] = {renderFinishedSemaphores[currentFrame]};

    ...

    if (vkQueueSubmit(graphicsQueue, 1, &submitInfo, inFlightFences[currentFrame]) != VK_SUCCESS) {
}
```

最后，不要忘记更新currentFrame 变量

```cpp
void drawFrame() {
    ...

    currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
}
```

上面代码，我们使用[模运算](https://zhida.zhihu.com/search?content_id=218606885&content_type=Article&match_order=1&q=模运算&zhida_source=entity)（%）来使currentFrame 变量的值在0 到才MAX_FRAMES_IN_FLIGHT之间进行循环。

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

