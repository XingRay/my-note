# Vulkan 三角形之旅-4

> 这里是记录笔者Vulkan的学习记录，参照该教程[vulkan-tutorial.com](https://link.zhihu.com/?target=https%3A//vulkan-tutorial.com/Drawing_a_triangle/Drawing/Command_buffers)这里是记录笔者Vulkan的学习记录，如果你想识别Vulkan相比于之前的传统图形API有什么区别和优势的话，欢迎看我的另外一篇文章[初探Vulkan](https://zhuanlan.zhihu.com/p/554631289)。相信应该能够帮助你识别Vulkan的优势所在。

## OverView

笔者这里是使用Mac来进行Vulkan的学习。简单来说Vulkan如果想要在Mac上运行的话，就必须要使用MoltenVK来完成。在Mac上Vulkan代码本质上还是会转化为Metal代码在Mac、IOS以及tvOS上运行的。关于这个怎么安装编译的，我们不必在意，安装Mac版本的Vulkan SDK就可以了。SDK会帮助你解决。并且在这里我们引入了计算线性代数的GLM库以及创建窗口的GLFW库。需要的东西并不多，关于这个环境配置问题可以参考上面原教程中的操作

在Vulkan当中绘制一个三角形流程可以分为如下

- 创建一个 VkInstance
- 选择支持的硬件设备（VkPhysicalDevice）
- 创建用于Draw和Presentation的VkDevice 和 VkQueue
- 创建窗口(window)、窗口表面(window surface)和[交换链](https://zhida.zhihu.com/search?content_id=211719919&content_type=Article&match_order=1&q=交换链&zhida_source=entity) (Swap Chain)
- 将Swap Chain Image 包装到 VkImageView
- 创建一个指定Render Target和用途的RenderPass
- 为RenderPass创建FrameBuffer
- 设置PipeLine
- 为每个可能的Swap Chain Image分配并记录带有绘制命令的Command Buffer
- 通过从Swap Chain获取的图像并在上面绘制，提交正确的Commander Buffer，并将绘制完的图像返回到Swap Chain去显示。

这么多步骤这是仅仅为了画一个三角形，其中如果你对于[渲染管线](https://zhida.zhihu.com/search?content_id=211719919&content_type=Article&match_order=1&q=渲染管线&zhida_source=entity)不熟悉或者没用其他现代图形API的话，建议还是去了解了解，不然再Vulkan许多的配置会让人一知半解，这边建议如果有Mac的话。可以先上手Metal，比较易用。可以后面再来挑战Vulkan。****

## 创建Frame Buffer

我们已经设置了RenderPass以期望获取到一个与Swap Chain格式相同的Frame Buffer，但我们实际上还没有创建任何Frame Buffer。

在RenderPass创建阶段我们指定了具体的Attachment，并通过**VkFramebuffer**对象包装绑定。FramerBuffer对象引用表示为Attachment的所有的**VkImageView**对象。在我们的例子中只会使用一个FramerBuffer也就是作为Color Attachment。然而我们作为Attachment的图像依赖Swap Chain用于Present时返回的图像。这意味着我们必须为Swap Chain中的所有Image创建一个Frame Buffer，并在绘制的时候使用对应的图像。

```cpp
std::vector<VkFramebuffer> swapChainFramebuffers;

void createFramebuffers() {
    swapChainFramebuffers.resize(swapChainImageViews.size());
    for (size_t i = 0; i < swapChainImageViews.size(); i++) {
        VkImageView attachments[] = {
            swapChainImageViews[i]
        };
                // 填充用于创建Frame Buffer的结构体。
        VkFramebufferCreateInfo framebufferInfo{};
        framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
                // 首先需要指定FrameBuffer需要与哪个RenderPass兼容。
                // 只能将一个framebuffer与它所兼容的RenderPass一起使用，这大致上意味着它们使用相同数量和类型的附件。
        framebufferInfo.renderPass = renderPass;
                // attachmentCount和pAttachments参数指定了VkImageView对象，
                // 这些对象应该被绑定到RenderPass中pAttachment数组中的各自的Attachment描述的VkImageView对象。
        framebufferInfo.attachmentCount = 1;
        framebufferInfo.pAttachments = attachments;
                // 设置宽高
        framebufferInfo.width = swapChainExtent.width;
        framebufferInfo.height = swapChainExtent.height;
                // 设置Layer 我们的Swap Chain图像是单个图像，所以Layers是1。
        framebufferInfo.layers = 1;

        if (vkCreateFramebuffer(device, &framebufferInfo, nullptr, &swapChainFramebuffers[i]) != VK_SUCCESS) {
            throw std::runtime_error("failed to create framebuffer!");
        }
    }
}
```

## Command Buffer

Vulkan 中的命令，如绘图操作和内存传输，不是直接使用函数调用执行的。您必须在Command Buffer中录制所有要执行的操作。这样做的好处是，当我们准备好告诉 Vulkan 我们想要做什么时，所有的Command都会一起提交到queue中执行，并且 Vulkan 可以更有效地处理这些命令，因为它们都可以一起使用。此外，如果需要，这允许在多个线程中进行录制Command。

我们在使用任何Command Buffers之前需要创建命令[对象池](https://zhida.zhihu.com/search?content_id=211719919&content_type=Article&match_order=1&q=对象池&zhida_source=entity)Command Pool。Command Pools管理用于Buffer存储的内存，并从中分配Command Buffer。添加新的类成员保存**VkCommandPool**:

```cpp
VkCommandPool commandPool;

QueueFamilyIndices queueFamilyIndices = findQueueFamilies(physicalDevice);

VkCommandPoolCreateInfo poolInfo{};
poolInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
poolInfo.flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
// 每个命令池只能分配在单一类型队列上提交的Command Buffer。我们将记录绘图命令，这就是我们选择图形队列系列的原因。
poolInfo.queueFamilyIndex = queueFamilyIndices.graphicsFamily.value();

// 创建Command Pool
if (vkCreateCommandPool(device, &poolInfo, nullptr, &commandPool) != VK_SUCCESS) {
            throw std::runtime_error("failed to create command pool!");
}
```

Command Pool有两种的Flag:

- VK_COMMAND_POOL_CREATE_TRANSIENT_BIT: 提示Command Buffer非常频繁的重新记录新命令(可能会改变内存分配行为
- VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT: 允许Command Buffer被单独重新记录，没有这个标志，所有的Command Buffer都必须一起重置

我们将在每一帧记录一个Command Buffer，因为我们希望能够在它上面重置并重新录制。因此，我们需要为我们的命令池设置VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT标志位。

我们现在可以开始分配Command Buffer。 创建一个 VkCommandBuffer 对象作为类成员。Command Buffer将在其Command Pool被销毁时自动释放，因此我们不需要显式清理。

```cpp
VkCommandBuffer commandBuffer;

VkCommandBufferAllocateInfo allocInfo{};
allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
// 指定使用什么Command Pool
allocInfo.commandPool = commandPool;
allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
allocInfo.commandBufferCount = 1;

//通过vkAllocateCommandBuffers来创建Command Buffer
if (vkAllocateCommandBuffers(device, &allocInfo, &commandBuffer) != VK_SUCCESS) {
    throw std::runtime_error("failed to allocate command buffers!");
}
```

Level参数指定分配的Command Buffer的主从关系，有两个描述如下所示：

- **VK_COMMAND_BUFFER_LEVEL_PRIMARY**: 可以提交到队列执行，但不能从其他的[命令缓冲区](https://zhida.zhihu.com/search?content_id=211719919&content_type=Article&match_order=1&q=命令缓冲区&zhida_source=entity)调用。
- **VK_COMMAND_BUFFER_LEVEL_SECONDARY**: 无法直接提交，但是可以从主Command Buffer调用。

我们不会在这里使用辅助缓冲区功能，但是可以想像，对于复用主缓冲区的常用操作很有帮助。

## **Command buffer recording**

我们现在将开始处理 recordCommandBuffer 函数，它将我们想要执行的命令写入Command Buffer。使用的 VkCommandBuffer 将作为参数传入，以及我们要写入的当前Swap Chain 图像的索引。

```cpp
void recordCommandBuffer(VkCommandBuffer commandBuffer, uint32_t imageIndex) {

    // 通过使用VkCommandBufferBeginInfo 构作为参数调用 vkBeginCommandBuffer 来开始记录CommandBuffer，该
    // 结构指定有关此特定CommandBuffer使用的一些细节。
    VkCommandBufferBeginInfo beginInfo{};
    beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
    beginInfo.flags = 0; // Optional
    beginInfo.pInheritanceInfo = nullptr; // Optional

    if (vkBeginCommandBuffer(commandBuffer, &beginInfo) != VK_SUCCESS) {
        throw std::runtime_error("failed to begin recording command buffer!");
    }
}
```

flags标志位参数用于指定如何使用Command Buffer。可选的参数类型如下:

- **VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT**: Command Buffer将在执行一次后立即重新记录。
- **VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT**: 这是一个辅助缓冲区，它限制在一个RenderPass中。
- **VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT**: Command Buffer也可以重新提交，同时它也在等待执行。

这些标志目前都不适用于我们。**pInheritanceInfo**参数与辅助缓冲区相关。它指定从主命令缓冲区继承的状态。如果Command Buffer已经被记录一次，那么调用**vkBeginCommandBuffer**会隐式地重置它。否则将命令附加到缓冲区是不可能的。

### 启动RenderPass

绘制从使用 vkCmdBeginRenderPass 开始。[渲染通道](https://zhida.zhihu.com/search?content_id=211719919&content_type=Article&match_order=1&q=渲染通道&zhida_source=entity)是使用 VkRenderPassBeginInfo 结构中的一些参数配置的。

```cpp
VkRenderPassBeginInfo renderPassInfo{};
renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
// 结构体第一个参数传递为绑定到对应Attachment的RenderPass本身。
// 我们为每一个Swap Chain的图像创建Frame Buffer，并指定为Color Attachment。
renderPassInfo.framebuffer = swapChainFramebuffers[imageIndex];

// 接下来的两个参数定义渲染区域的大小。渲染区域定义着色器加载和存储将发生的位置。此区域之外的像素将具有未定义的值。
renderPassInfo.renderArea.offset = {0, 0};
renderPassInfo.renderArea.extent = swapChainExtent;
// 最后两个参数定义了用于 VK_ATTACHMENT_LOAD_OP_CLEAR 的清除值，我们将其用作颜色附件的加载操作。
// 将其定义为具有 100% 不透明度的黑色。
VkClearValue clearColor = {{{0.0f, 0.0f, 0.0f, 1.0f}}};
renderPassInfo.clearValueCount = 1;
renderPassInfo.pClearValues = &clearColor;

vkCmdBeginRenderPass(commandBuffer, &renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
```

关于vkCmdBeginRenderPass，RenderPass现在可以启用。所有记录命令的函数都可以通过它们的 vkCmd 前缀来识别。它们都返回 void，所以在结束记录之前不会有任何错误处理。

对于每个命令，第一个参数总是记录该命令的Command Buffer。第二个参数指定我们传递的RenderPass的具体信息。最后的参数控制如何提供RenderPass将要应用的绘制命令。它使用以下数值任意一个:

- **VK_SUBPASS_CONTENTS_INLINE**: RenderPass命令被嵌入在主命令缓冲区中，没有辅助缓冲区执行。
- **VK_SUBPASS_CONTENTS_SECONDARY_COOMAND_BUFFERS**: RenderPass命令将会从辅助命令缓冲区执行。

我们不会使用辅助命令缓冲区，所以我们选择第一个。

接下来将PipeLine的动态属性需要设置。我们确实为这个PipeLine指定了ViewPort和Scissor状态为动态的。所以我们需要在发出我们的[绘图命令](https://zhida.zhihu.com/search?content_id=211719919&content_type=Article&match_order=2&q=绘图命令&zhida_source=entity)之前将它们设置在Command Buffer中：

```cpp
// 第二个参数指定管道对象是图形还是计算管道。
vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

// 我们确实为这个管道指定了ViewPort和scissor为动态属性的。
// 所以我们需要在发出我们的绘图命令之前将它们设置在命令缓冲区中：
VkViewport viewport{};
viewport.x = 0.0f;
viewport.y = 0.0f;
viewport.width = static_cast<float>(swapChainExtent.width);
viewport.height = static_cast<float>(swapChainExtent.height);
viewport.minDepth = 0.0f;
viewport.maxDepth = 1.0f;
vkCmdSetViewport(commandBuffer, 0, 1, &viewport);

VkRect2D scissor{};
scissor.offset = {0, 0};
scissor.extent = swapChainExtent;
vkCmdSetScissor(commandBuffer, 0, 1, &scissor);

vkCmdDraw(commandBuffer, 3, 1, 0, 0);

// 结束渲染
vkCmdEndRenderPass(commandBuffer);

// 停止record命令。
if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
    throw std::runtime_error("failed to record command buffer!");
}
```

实际的 vkCmdDraw 函数有点虎头蛇尾，但是因为我们提前指定了所有的信息，所以才这么简单。除了命令缓冲区之外，它还有以下参数：

- vertexCount：即使我们没有[顶点缓冲区](https://zhida.zhihu.com/search?content_id=211719919&content_type=Article&match_order=1&q=顶点缓冲区&zhida_source=entity)，从技术上讲，我们仍然有 3 个顶点要绘制。
- instanceCount：用于实例化渲染，如果不这样做，请使用 1。
- firstVertex：用作顶点缓冲区的偏移量，定义 gl_VertexIndex 的最小值。
- firstInstance：用作实例化渲染的偏移量，定义 gl_InstanceIndex 的最小值。

到了这里我们将Command Buffer的步骤也完成。接下来我们将真正的渲染出我们的第一个三角形，以及将其显示在屏幕上。