# Vulkan从入门到精通34-指令池和指令缓冲

Vulkan 下的指令，比如绘制指令和内存传输指令并不是直接通过函数调用执行的。我们需要将所有要执行的操作记录在一个指令缓冲对象，然后提交给可以执行这些操作的队列才能执行。这使得我们可以在程序初始化时就准备好所有要指定的指令序列，在渲染时直接提交执行。也使得多线程提交指令变得更加容易。我们只需要在需要指定执行的使用，将指令缓冲对象提交给Vulkan 处理接口。

创建指令缓冲和描述集一样，先要构建指令池。指令池对象用于管理指令缓冲对象使用的内存，并负责指令缓冲对象的分配。

创建指令池的代码如下

```cpp
bool VK_ContextImpl::createCommandPool()
{
    QueueFamilyIndices queueFamilyIndices = findQueueFamilies(physicalDevice);

    VkCommandPoolCreateInfo poolInfo{};
    poolInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
    poolInfo.queueFamilyIndex = queueFamilyIndices.graphicsFamily.value();
    poolInfo.pNext = nullptr;
    poolInfo.flags = 0;

    if (vkCreateCommandPool(device, &poolInfo, getAllocation(), &commandPool) != VK_SUCCESS) {
        std::cerr << "failed to create command pool!" << std::endl;
        return false;
    }
    return true;
}
```

上面queueFamilyIndex设置队列簇索引。每个指令池对象分配的指令缓冲对象只能提交给一个特定类型的队列。这里使用的是绘制指令，它可以被提交给支持图形操作的队列。

对应的flag可以指定为以下选项

- VK_COMMAND_POOL_CREATE_TRANSIENT_BIT - 使用它分配的指令[缓冲对象](https://zhida.zhihu.com/search?content_id=191118447&content_type=Article&match_order=6&q=缓冲对象&zhida_source=entity)被频繁用来记录新的指令
- VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT - 指令缓冲对象之间相互独立，不会被一起重置
- VK_COMMAND_POOL_CREATE_PROTECTED_BIT - 指定从命令池中分配的[命令缓冲](https://zhida.zhihu.com/search?content_id=191118447&content_type=Article&match_order=1&q=命令缓冲&zhida_source=entity)是是protected类型

通过vkDestroyCommandPool(device, commandPool, *getAllocation*());即可销毁指令池。

------

创建完指令池，即可创建指令缓冲

首先要分配指令缓冲对象，如下

```cpp
    VkCommandBufferAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
    allocInfo.commandPool = commandPool;
    allocInfo.commandBufferCount = 1;

    VkCommandBuffer commandBuffer;
    vkAllocateCommandBuffers(device, &allocInfo, &commandBuffer);
```

level这里指定的是指令缓冲的类型，这里使用的是主指令缓冲，次级指令缓冲后继再讲；只要知道主指令缓冲可以被提交到队列中执行，但不可以被其他指令缓冲所调用即可。

之后即可使用vkBeginCommandBuffer 开始记录指令缓冲，使用vkEndCommandBuffer 来完成记录指令。

调用例子如下

```cpp
    VkCommandBufferBeginInfo beginInfo{};
    beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
    beginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;

    vkBeginCommandBuffer(commandBuffer, &beginInfo);
```

flags值和说明如下

1. VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT - 指令缓冲在执行一次后，就被用来记录新的指令。
2. VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT - 只在一个渲染流程内使用的辅助指令缓冲。
3. VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT - 在指令缓冲等待执行时，仍然可以提交这一指令缓冲。

指令缓冲对象记录指令后，调用 vkBeginCommandBuffer 函数会重置指令缓冲对象。

commandBuffer还有另外一个函数 - vkResetCommandBuffer 用于重置指令缓冲到初始化状态。

在vkBeginCommandBuffer开启后即可设置具体处理命令，比如下面的代码被用来传输vkImage

```cpp
    VkCommandBufferBeginInfo cmd_buf_info = {};
    cmd_buf_info.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
    cmd_buf_info.pNext = NULL;
    cmd_buf_info.flags = 0;
    cmd_buf_info.pInheritanceInfo = NULL;

    res = vkBeginCommandBuffer(commandBuffer, &cmd_buf_info);

    vkCmdBlitImage(commandBuffer, bltSrcImage, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, bltDstImage, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                   1, &region, VK_FILTER_LINEAR);

    VkImageMemoryBarrier memBarrier = {};
    memBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
    memBarrier.pNext = NULL;
    memBarrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
    memBarrier.dstAccessMask = VK_ACCESS_MEMORY_READ_BIT;
    memBarrier.oldLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
    memBarrier.newLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
    memBarrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    memBarrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    memBarrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    memBarrier.subresourceRange.baseMipLevel = 0;
    memBarrier.subresourceRange.levelCount = 1;
    memBarrier.subresourceRange.baseArrayLayer = 0;
    memBarrier.subresourceRange.layerCount = 1;
    memBarrier.image = bltDstImage;
    vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_TRANSFER_BIT, 0, 0, NULL, 0, NULL, 1,
                         &memBarrier);

    VkImageMemoryBarrier prePresentBarrier = {};
    prePresentBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
    prePresentBarrier.pNext = NULL;
    prePresentBarrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
    prePresentBarrier.dstAccessMask = VK_ACCESS_MEMORY_READ_BIT;
    prePresentBarrier.oldLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
    prePresentBarrier.newLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
    prePresentBarrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    prePresentBarrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    prePresentBarrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    prePresentBarrier.subresourceRange.baseMipLevel = 0;
    prePresentBarrier.subresourceRange.levelCount = 1;
    prePresentBarrier.subresourceRange.baseArrayLayer = 0;
    prePresentBarrier.subresourceRange.layerCount = 1;
    prePresentBarrier.image = image;
    vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT, 0, 0, NULL, 0, NULL, 1,
                         &prePresentBarrier);

    res = vkEndCommandBuffer(commandBuffer);
```

