# Vulkan从入门到精通37-管线屏障和vkCmdClearColorImage

这篇说说管线屏障，在 [蛋蛋的爸爸：Vulkan从入门到精通29-栅栏](https://zhuanlan.zhihu.com/p/459987267) 一文中我们讲过vkFence栅栏，说白了栅栏用于CPU段同步；

管线是一种同步机制，用来管理内存访问、以及在vulkan管线各个阶段里的资源状态变化。

关联[函数原型](https://zhida.zhihu.com/search?content_id=191271613&content_type=Article&match_order=1&q=函数原型&zhida_source=entity) -

```cpp
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

各参数解释如下

- commandBuffer - 用于记录命令的[命令缓冲](https://zhida.zhihu.com/search?content_id=191271613&content_type=Article&match_order=1&q=命令缓冲&zhida_source=entity)
- srcStageMask - 管线的那个阶段可以写[数据掩码](https://zhida.zhihu.com/search?content_id=191271613&content_type=Article&match_order=1&q=数据掩码&zhida_source=entity)
- dstStageMask - 管线的那个解读可以从资源读数据掩码
- dependecyFlags - 依赖标志位集合，当前只通常取0值
- memoryBarrierCount - 管线[内存屏障](https://zhida.zhihu.com/search?content_id=191271613&content_type=Article&match_order=1&q=内存屏障&zhida_source=entity)个数
- pMemoryBarriers - 管线内存屏障数据
- `bufferMemoryBarrierCount` - 缓冲内存屏障个数
- `pBufferMemoryBarriers`- 缓冲内存屏障数据
- `imageMemoryBarrierCount` - 图像内存屏障个数
- `pImageMemoryBarriers`- 图像内存屏障数据

个人看法

- 感觉是复合函数，具体用时可以考虑使用内存屏障、图像屏障或者缓冲区屏障
- 第一个参数是VkCommandBuffer - 意味着此函数调用时只能在记录命令缓冲中
- srcStageMask、dstStageMash取什么值取决于使用此函数的目的
- [屏障函数](https://zhida.zhihu.com/search?content_id=191271613&content_type=Article&match_order=1&q=屏障函数&zhida_source=entity)的作用时保障函数在执行时的严格次序，这意味的执行次序是调用次序 - 只有屏障前操作执行完成，才会执行后继操作
- 管线屏障作用于设备端

举一个使用例子

```cpp
auto image = context->createImage("../images/cat.png");
    auto command = context->getCommandPool()->beginSingleTimeCommands();
    adjustImageLayout(command, image->getImage(), VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
    VkImageSubresourceRange srRange = {};
    srRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    srRange.baseMipLevel = 0;
    srRange.levelCount = VK_REMAINING_MIP_LEVELS;
    srRange.baseArrayLayer = 0;
    srRange.layerCount = VK_REMAINING_ARRAY_LAYERS;

    VkClearColorValue ccv;
    ccv.float32[0] = 0.6f;
    ccv.float32[1] = 0.9f;
    ccv.float32[2] = 0.2f;
    ccv.float32[3] = 0.6f;

    vkCmdClearColorImage(command, image->getImage(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, &ccv, 1, &srRange);

    adjustImageLayout(command, image->getImage(), VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
    context->getCommandPool()->endSingleTimeCommands(command, context->getGraphicQueue());
```

这端代码的作用是把从文件中载入的图像，修改为指定颜色的图像。首先创建一个commandBuffer，然后把图像布局由 VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL 调整为 K_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL，然后调用vkCmdClearColorImage修改图像颜色，最后把图像布局调整回去。

其中adjustImageLayout代码如下

```cpp
void adjustImageLayout(VkCommandBuffer command, VkImage image, VkImageLayout oldLayout, VkImageLayout newLayout, uint32_t levelCount)
{
    VkImageMemoryBarrier barrier{};
    barrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
    barrier.oldLayout = oldLayout;
    barrier.newLayout = newLayout;
    barrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
    barrier.image = image;
    barrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    barrier.subresourceRange.baseMipLevel = 0;
    barrier.subresourceRange.levelCount = levelCount;
    barrier.subresourceRange.baseArrayLayer = 0;
    barrier.subresourceRange.layerCount = 1;


    VkPipelineStageFlags sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
    VkPipelineStageFlags destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;


    if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
        barrier.srcAccessMask = 0;
        barrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;


        sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
        destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
    } else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
               && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
        barrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
        barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT;


        sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
        destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
    } else if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED
               && newLayout == VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL) {
        barrier.srcAccessMask = 0;
        barrier.dstAccessMask = VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT |
                                VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT;


        sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
        destinationStage = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT;
    } else {
        barrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
        barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT;


        sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
        destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
    }


    vkCmdPipelineBarrier(
        command,
        sourceStage, destinationStage,
        0,
        0, nullptr,
        0, nullptr,
        1, &barrier
    );
}
```

函数是从之前transitionImageLayout函数改的，最终效果显示一个草黄色的正方形