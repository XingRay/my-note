# Vulkan开发学习记录 12 - 命令缓冲

## 简述

Vulkan下的命令，比如绘制命令和内存传输命令并不是直接通过[函数调用](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=函数调用&zhida_source=entity)执行的。我们需要将所有要执行的操作记录在一个命令缓冲对象，然后提交给可以执行这些操作的队列才能执行。这使得我们可以在程序初始化时就准备好所有要指定的命令序列，在渲染时直接提交执行。也使得[多线程](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=多线程&zhida_source=entity)提交命令变得更加容易。我们只需要在需要指定执行的使用，将命令缓冲对象提交给Vulkan处理接口。

## 命令池

在创建命令缓冲对象之前，我们需要先创建命令池对象。命令池对象用于管理命令缓冲对象使用的内存，并负责命令缓冲对象的分配。我们添加了一个VkCommandPool[成员变量](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=成员变量&zhida_source=entity)到类中：

```cpp
 VkCommandPool commandPool;
```

添加一个叫做createCommandPool的函数，并在initVulkan函数中帧缓冲对象创建之后调用它：

```cpp
void initVulkan() {
    createInstance();
    setupDebugMessenger();
    createSurface();
    pickPhysicalDevice();
    createLogicalDevice();
    createSwapChain();
    createImageViews();
    createRenderPass();
    createGraphicsPipeline();
    createFramebuffers();
    createCommandPool();
}

...

void createCommandPool() {

}
```

命令池对象的创建只需要填写两个参数：

```cpp
QueueFamilyIndices queueFamilyIndices = findQueueFamilies(physicalDevice);

VkCommandPoolCreateInfo poolInfo{};
poolInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
poolInfo.flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
poolInfo.queueFamilyIndex = queueFamilyIndices.graphicsFamily.value();
```

命令缓冲对象在被提交给我们之前获取的队列后，被Vulkan执行。每个命令池对象分配的命令缓冲对象只能提交给一个特定类型的队列。在这里，我们使用的是绘制命令，它可以被提交给支持图形操作的队列。

有下面两种用于命令池对象创建的标记，可以提供有用的信息给Vulkan的[驱动程序](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=驱动程序&zhida_source=entity)进行一定优化处理：

- `VK_COMMAND_POOL_CREATE_TRANSIENT_BIT`：提示[命令缓冲区](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=命令缓冲区&zhida_source=entity)经常用新命令重新记录。（可能会改变[内存分配](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=内存分配&zhida_source=entity)行为）
- `VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT`: 允许单独重新记录命令缓冲区，如果没有这个标志，它们都必须一起重置。

对于我们的程序，我们只在程序初始化时记录命令到命令缓冲对象， 然后在程序的主循环中执行命令，所以，我们不使用上面这两个标记。

```cpp
if (vkCreateCommandPool(device, &poolInfo, nullptr, &commandPool) != VK_SUCCESS) {
    throw std::runtime_error("failed to create command pool!");
}
```

我们通过调用vkCreateCommandPool函数来完成命令池对象的创建。 应用程序结束前我们需要清除创建的命令池对象：

```cpp
void cleanup() {
    vkDestroyCommandPool(device, commandPool, nullptr);

    ...
}
```

## 分配[命令缓冲](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=11&q=命令缓冲&zhida_source=entity)

现在，我们可以开始分配命令缓冲对象，使用它记录绘制命令。由于绘制操作是在[帧缓冲](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=2&q=帧缓冲&zhida_source=entity)上进行的，我们需要为[交换链](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=交换链&zhida_source=entity)中的每一个图像分配一个命令缓冲对象。为此，我们添加了一个数组作为成员变量来存储创建的VkCommandBuffer对象。命令缓冲对象会在命令池对象被清除时自动被清除，不需要我们自己显式地清除它。

```cpp
std::vector<VkCommandBuffer> commandBuffers;
```

添加一个叫做createCommandBuffers的函数为交换链中的每一个图像创建命令缓冲对象：

```cpp
void initVulkan() {
    createInstance();
    setupDebugMessenger();
    createSurface();
    pickPhysicalDevice();
    createLogicalDevice();
    createSwapChain();
    createImageViews();
    createRenderPass();
    createGraphicsPipeline();
    createFramebuffers();
    createCommandPool();
    createCommandBuffer();
}

...

void createCommandBuffer() {
    commandBuffers.resize(swapChainFramebuffers.size());
}
```

命令缓冲对象可以通过调用ckAllocateCommandBuffers函数分配得到。 调用这一函数需要填写VkCommandBufferAllocateInfo结构体来指定分配使用的命令池和需要分配的命令缓冲对象个数：

```cpp
VkCommandBufferAllocateInfo allocInfo{};
allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
allocInfo.commandPool = commandPool;
allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
allocInfo.commandBufferCount = 1;

if (vkAllocateCommandBuffers(device, &allocInfo, &commandBuffer) != VK_SUCCESS) {
    throw std::runtime_error("failed to allocate command buffers!");
}
```

level成员变量用于指定分配的命令缓冲对象是主要命令缓冲对象还是辅助命令缓冲对象：

- `VK_COMMAND_BUFFER_LEVEL_PRIMARY`: 可以提交到队列执行，但不能从其他命令缓冲区调用。
- `VK_COMMAND_BUFFER_LEVEL_SECONDARY`: 不能直接提交，但可以从主命令缓冲区调用。

在这里，我们没有使用辅助命令缓冲对象，但辅助治理给缓冲对象的好处是显而易见的，我们可以把一些常用的命令存储在辅助命令缓冲对象，然后在主要命令缓冲对象中调用执行。

### 记录命令到命令缓冲

我们通过调用vkBeginCommandBuffer函数开始命令缓冲的记录操作， 这一函数以VkCommandBufferInfo结构体作为参数来指定一些有关命令缓冲的使用细节。

```cpp
VkCommandBufferBeginInfo beginInfo{};
beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
beginInfo.flags = 0; // Optional
beginInfo.pInheritanceInfo = nullptr; // Optional

if (vkBeginCommandBuffer(commandBuffer, &beginInfo) != VK_SUCCESS) {
    throw std::runtime_error("failed to begin recording command buffer!");
}
```

flags成员变量用于指定我们将要怎样使用命令缓冲。它的值可以是下面这些：

- `VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT`：命令缓冲区执行一次后会立即重新记录。
- `VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT`：这是一个辅助命令缓冲区，将完全在单个渲染过程中。
- `VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT`：命令缓冲区可以在它也已经挂起执行时重新提交。

在这里，我们使用了VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT标记，这使得我们可以在上一帧还未结束渲染时，提交下一帧的[渲染命令](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=渲染命令&zhida_source=entity)。 pInheritanceInfo成员变量只用于辅助命令缓冲，可以用它来指定从调用它的主要命令缓冲继承的状态。

命令[缓冲对象](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=23&q=缓冲对象&zhida_source=entity)记录命令后，调用vkBeginCommandBuffer函数会重置命令缓冲对象。

## 开始渲染流程

调用`vkCmdBeginRenderPass`函数可以开始一个渲染流程。这一函数需要我们使用`VkRenderPassBeginInfo`结构体来指定使用的渲染流程对象：

```cpp
VkRenderPassBeginInfo renderPassInfo{};
renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
renderPassInfo.renderPass = renderPass;
renderPassInfo.framebuffer = swapChainFramebuffers[imageIndex];
```

renderPass成员变量用于指定使用的渲染流程对象，frameBuffer成员变量用于指定使用的帧缓冲对象。

```cpp
 renderPassInfo.renderArea.offset = {0, 0};
renderPassInfo.renderArea.extent = swapChainExtent;
```

renderArea 成员变量用于指定用于渲染的区域。位于这一区域外的像素数据会处于未定义状态。通常，我们将这一区域设置为和我们使用的附着大小完全一样。

```cpp
VkClearValue clearColor = {{{0.0f, 0.0f, 0.0f, 1.0f}}};
renderPassInfo.clearValueCount = 1;
renderPassInfo.pClearValues = &clearColor;
```

clearValueCount和pClearValues成员变量用于指定使用VK_ATTACHMENT_LOAD_OP_CLEAR标记后，使用的清除值。在这里，我们使用完全不透明的黑色作为清除值。

```cpp
vkCmdBeginRenderPass(commandBuffer, &renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
```

所有可以记录命令到命令缓冲的函数的函数名都带有一个vkCmd前缀， 并且这些函数的返回值都是void，也就是说在命令记录操作完全结束前，不用进行任何错误处理。

这类函数的第一个参数是用于记录命令的命令缓冲对象。第二个参数是使用的渲染流程的信息。最后一个参数是用来指定[渲染流程](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=6&q=渲染流程&zhida_source=entity)如何提供绘制命令的标记，它可以是下面这两个值之一：

- `VK_SUBPASS_CONTENTS_INLINE`：渲染过程命令将嵌入主命令缓冲区本身，不会执行辅助命令缓冲区。
- `VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS`：渲染过程命令将从辅助命令缓冲区执行。

由于我们没有使用辅助命令缓冲，所以我们使用 VK_SUBPASS_CONTENTS_INLINE。

## 基础绘制命令

现在，绑定图形管线：

```cpp
vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
```

vkCmdBindPipeline函数的第二个参数用于指定管线对象是图形管线还是计算管线。至此，我们已经提交了需要图形管线执行的命令，以及片段[着色器](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=着色器&zhida_source=entity)使用的附着，可以开始调用命令进行三角形的绘制操作：

```cpp
vkCmdDraw(commandBuffer, 3, 1, 0, 0);
```

我们使用vkCmdDraw函数来提交绘制操作到命令缓冲，它的第一个参数是记录有要执行的命令的命令缓冲对象，它的剩余参数依次是：

- `vertexCount`：尽管这里我们没有使用[顶点缓冲](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=顶点缓冲&zhida_source=entity)，但仍然需要指定三个顶点用于三角形的绘制。
- `instanceCount`：用于实例渲染，为1时表示不进行实例渲染。
- `firstVertex`: 用于定义[着色器变量](https://zhida.zhihu.com/search?content_id=218156355&content_type=Article&match_order=1&q=着色器变量&zhida_source=entity)gl_VertexIndex的值。
- `firstInstance`：用于定义着色器变量gl_InstanceIndex的值。

## 结束渲染流程

接着，我们调用vkCmdEndRenderPass函数结束渲染流程：

```cpp
vkCmdEndRenderPass(commandBuffer);
```

然后，结束记录指令到命令缓冲：

```cpp
if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
    throw std::runtime_error("failed to record command buffer!");
}
```

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

