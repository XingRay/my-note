# Vulkan从入门到精通38-使用vkCmdCopyImageToBuffer保存VkImage图像

上篇 - [蛋蛋的爸爸：Vulkan从入门到精通37-管线屏障和vkCmdClearColorImage](https://zhuanlan.zhihu.com/p/464134337) 讲的是管线屏障和使用vkCmdClearColorImage来修改Image图像，本文使用vkCmdCopytImageToBuffer函数把帧图像保存为本地[图像文件](https://zhida.zhihu.com/search?content_id=192446055&content_type=Article&match_order=1&q=图像文件&zhida_source=entity)。

在opengl中，使用glReadPixel来读取像素数据到内存，之后再保存到本地文件。

在vulkan中，要把图像帧数据保存为本地文件，过程比较繁琐，大致步骤如下

- 修改原图像格式为 *VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL*
- *创建一个对应大小的主机缓冲*
- *调用*vkCmdCopyImageToBuffer 复制数据
- 调用vkMapMemory 把图像数据从设备映射到主机端
- 写文件
- 调用vkUnmapMemory解除映射
- 复原原图像格式

程序中如果要实现点击鼠标即可保存对应帧图像数据，则实现方式如下

调用vkAcquireNextImageKHR获取帧图像后

```cpp
    if (captureScreen) {
        captureScreen = false;
        std::stringstream stream;
        stream << "capture-";
        stream << currentFrameIndex;
        stream << ".ppm";

        VkImage image = swapChainImages[imageIndex];

        auto cmd = getCommandPool()->beginSingleTimeCommands();
        adjustImageLayout(cmd, image, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
        getCommandPool()->endSingleTimeCommands(cmd, graphicsQueue);
        writeFile(this, stream.str(), image, getSwapChainExtent().width,
                  getSwapChainExtent().height);

        cmd = getCommandPool()->beginSingleTimeCommands();
        adjustImageLayout(cmd, image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
        getCommandPool()->endSingleTimeCommands(cmd, graphicsQueue);
    }
```

writeFile函数实现如下

```cpp
void writeFile(VK_Context *context, const std::string &file, VkImage image, uint32_t width,
               uint32_t height)
{
    VkBuffer imageBuffer;
    VkDeviceMemory imageBufferMemory;

    VkPhysicalDeviceMemoryProperties memoryProperties;
    vkGetPhysicalDeviceMemoryProperties(context->getPhysicalDevice(), &memoryProperties);

    context->createBuffer(width * height * 4, VK_BUFFER_USAGE_TRANSFER_DST_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,//0,
                          imageBuffer, imageBufferMemory);

    VkCommandBuffer commandBuffer;
    {
        VkCommandBufferAllocateInfo allocateInfo = {
            .sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO,
            .pNext = NULL,
            .commandPool = context->getCommandPool()->getCommandPool(),
            .level = VK_COMMAND_BUFFER_LEVEL_PRIMARY,
            .commandBufferCount = 1,
        };
        assert(vkAllocateCommandBuffers(context->getDevice(), &allocateInfo, &commandBuffer) == VK_SUCCESS);
    }

    {
        VkCommandBufferBeginInfo beginInfo = {
            .sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO,
            .pNext = NULL,
            .flags = 0,
            .pInheritanceInfo = NULL,
        };
        assert(vkBeginCommandBuffer(commandBuffer, &beginInfo) == VK_SUCCESS);

        VkBufferImageCopy copy = {
            .bufferOffset = 0,
            .bufferRowLength = 0,
            .bufferImageHeight = 0,
            .imageSubresource = {VK_IMAGE_ASPECT_COLOR_BIT, 0, 0, 1},
            .imageOffset = {0, 0, 0},
            .imageExtent = {
                .width = width,
                .height = height,
                .depth = 1
            },
        };

        vkCmdCopyImageToBuffer(commandBuffer, image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                               imageBuffer, 1, &copy);

        VkBufferMemoryBarrier transferBarrier = {
            .sType = VK_STRUCTURE_TYPE_BUFFER_MEMORY_BARRIER,
            .pNext = 0,
            .srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT,
            .dstAccessMask = VK_ACCESS_HOST_READ_BIT,
            .srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED,
            .dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED,
            .buffer = imageBuffer,
            .offset = 0,
            .size = VK_WHOLE_SIZE,
        };
        vkCmdPipelineBarrier(commandBuffer, VK_PIPELINE_STAGE_TRANSFER_BIT, VK_PIPELINE_STAGE_HOST_BIT, 0, 0,
                             NULL, 1, &transferBarrier, 0, NULL);
        assert(vkEndCommandBuffer(commandBuffer) == VK_SUCCESS);
    }

    VkFence fence;
    {
        VkFenceCreateInfo createInfo = {
            .sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO,
            .pNext = 0,
            .flags = 0,
        };
        assert(vkCreateFence(context->getDevice(), &createInfo, NULL, &fence) == VK_SUCCESS);
    }

    {
        VkSubmitInfo submitInfo = {
            .sType = VK_STRUCTURE_TYPE_SUBMIT_INFO,
            .pNext = NULL,
            .waitSemaphoreCount = 0,
            .pWaitSemaphores = NULL,
            .pWaitDstStageMask = NULL,
            .commandBufferCount = 1,
            .pCommandBuffers = &commandBuffer,
            .signalSemaphoreCount = 0,
            .pSignalSemaphores = NULL,
        };
        assert(vkQueueSubmit(context->getGraphicQueue(), 1, &submitInfo, fence) == VK_SUCCESS);

        assert(vkWaitForFences(context->getDevice(), 1, &fence, VK_TRUE, UINT64_MAX) == VK_SUCCESS);
        char *imageData;
        assert(vkMapMemory(context->getDevice(), imageBufferMemory, 0, VK_WHOLE_SIZE,
                           0, (void **) &imageData) == VK_SUCCESS);

        VkMappedMemoryRange flushRange = {
            .sType = VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE,
            .pNext = NULL,
            .memory = imageBufferMemory,
            .offset = 0,
            .size = VK_WHOLE_SIZE,
        };

        const VkExtent2D renderSize = {
            .width = width,
            .height = height
        };

        assert(vkInvalidateMappedMemoryRanges(context->getDevice(), 1, &flushRange) == VK_SUCCESS);
        assert(writeTiff(file.data(), imageData, renderSize, 4) == 0);
        vkUnmapMemory(context->getDevice(), imageBufferMemory);
    }

    assert(vkQueueWaitIdle(context->getGraphicQueue()) == VK_SUCCESS);
    vkDestroyFence(context->getDevice(), fence, NULL);

    vkFreeMemory(context->getDevice(), imageBufferMemory, context->getAllocation());
    vkDestroyBuffer(context->getDevice(), imageBuffer, context->getAllocation());
}
```

比较繁琐的原因是 vulkan是异步执行，格式转换等都需要同步

图像格式大致如下

VK_IMAGE_LAYOUT_PRESENT_SRC_KHR - 适合呈现到输出表面

VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL - [片段着色器](https://zhida.zhihu.com/search?content_id=192446055&content_type=Article&match_order=1&q=片段着色器&zhida_source=entity)中写入颜色的最佳附件

VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL - 作为传输操作的最佳源，如vkCmdCopyImageToBuffer

VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL - 作为传输操作的最佳目的地，如vkCmdCopyBufferToImage

VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL: - 适合着色器采样

如果图像是从文件读进去的，则再保存为文件大致格式如下

```cpp
    auto image = context->createImage("../images/cat.png");

    auto cmd = context->getCommandPool()->beginSingleTimeCommands();
    adjustImageLayout(cmd, image->getImage(), VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
    context->getCommandPool()->endSingleTimeCommands(cmd, context->getGraphicQueue());
    writeFile(context, "cat.ppm", image->getImage(), image->getWidth(),
              image->getHeight());

    cmd = context->getCommandPool()->beginSingleTimeCommands();
    adjustImageLayout(cmd, image->getImage(), VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
    context->getCommandPool()->endSingleTimeCommands(cmd, context->getGraphicQueue());
```

vkInvalidateMappedMemoryRanges 函数作用是把[内存映射](https://zhida.zhihu.com/search?content_id=192446055&content_type=Article&match_order=1&q=内存映射&zhida_source=entity)为无效，把指定内存范围内设置为对主机(CPU)可见可读可写状态。

```cpp
VkResult vkInvalidateMappedMemoryRanges(
    VkDevice                                    device,
    uint32_t                                    memoryRangeCount,
    const VkMappedMemoryRange*                  pMemoryRanges);
```

------

vkCmdCopyImageToBuffer 函数作用是复制指定Image内存到指定缓冲

函数原型

```cpp
// Provided by VK_VERSION_1_0
void vkCmdCopyImageToBuffer(
    VkCommandBuffer                             commandBuffer,
    VkImage                                     srcImage,
    VkImageLayout                               srcImageLayout,
    VkBuffer                                    dstBuffer,
    uint32_t                                    regionCount,
    const VkBufferImageCopy*                    pRegions);
 
```

详细说明 见 - [vkCmdCopyImageToBuffer(3)](https://registry.khronos.org/vulkan/specs/latest/man/html/vkCmdCopyImageToBuffer.html)