# Vulkan开发学习记录 18 - 暂存缓冲

## 介绍

现在我们创建的[顶点缓冲](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=1&q=顶点缓冲&zhida_source=entity)已经可以使用了，但我们的顶点缓冲使用的[内存类型](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=1&q=内存类型&zhida_source=entity)并不是适合显卡读取的最佳内存类型。最适合显卡读取的内存类型具有VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT 标记，含有这一 标记的内存类型通常CPU 无法直接访问。在本章节，我们会创建两个顶点缓冲。一个用于GPU 加载数据，一个用于显卡设备读取数据。我们通过缓冲复制指令将CPU 加载到的缓冲中的数据复制到显卡可以快速读取的缓冲中去。

## 传输队列

缓冲复制指令需要提交给支持传输操作的队列执行，我们可以查询队列族是否支持VK_QUEUE_TRANSFER_BIT 特性，确定是否可以使用缓冲 复制指令。对于支持VK_QUEUE_GRAPHICS_BIT 或`VK_QUEUE_COMPUTE_BIT` 特性的队列族，VK_QUEUE_TRANSFER_BIT 特性一定被支持，所以我们不需要显式地检测[队列族](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=3&q=队列族&zhida_source=entity)是否支持VK_QUEUE_TRANSFER_BIT 特性。

如果读者喜欢挑战，可以尝试使用其它支持传输操作的队列族。这需要读者按照下面对程序进行一定地修改：

- 修改`QueueFamilyIndices`并`findQueueFamilies`显式查找带有`VK_QUEUE_TRANSFER_BIT`bit 的队列系列，而不是 `VK_QUEUE_GRAPHICS_BIT`.
- 修改`createLogicalDevice`以请求传输队列的句柄
- 为在传输队列系列上提交的命令缓冲区创建第二个命令池
- `sharingMode`将资源更改为`VK_SHARING_MODE_CONCURRENT`并指定图形和传输队列族
- 将任何传输命令`vkCmdCopyBuffer`（我们将在本章中使用）提交到传输队列而不是图形队列

## 创建缓冲的辅助函数

我们添加一个叫做createBuffer 的辅助函数来帮助我们创建缓冲：

```cpp
void createBuffer(VkDeviceSize size, VkBufferUsageFlags usage, VkMemoryPropertyFlags properties, VkBuffer& buffer, VkDeviceMemory& bufferMemory) {
    VkBufferCreateInfo bufferInfo{};
    bufferInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
    bufferInfo.size = size;
    bufferInfo.usage = usage;
    bufferInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;

    if (vkCreateBuffer(device, &bufferInfo, nullptr, &buffer) != VK_SUCCESS) {
        throw std::runtime_error("failed to create buffer!");
    }

    VkMemoryRequirements memRequirements;
    vkGetBufferMemoryRequirements(device, buffer, &memRequirements);

    VkMemoryAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
    allocInfo.allocationSize = memRequirements.size;
    allocInfo.memoryTypeIndex = findMemoryType(memRequirements.memoryTypeBits, properties);

    if (vkAllocateMemory(device, &allocInfo, nullptr, &bufferMemory) != VK_SUCCESS) {
        throw std::runtime_error("failed to allocate buffer memory!");
    }

    vkBindBufferMemory(device, buffer, bufferMemory, 0);
}
```

我们添加了一些参数来方便地使用不同的缓冲大小，内存类型来创建我们需要的缓冲。createBuffer 函数的最后两个参数用于返回创建的缓冲对象和它关联的内存对象。

现在我们可以使用createBuffer 函数替换之前createVertexBuffer 函数的实现内容：

```cpp
void createVertexBuffer() {
    VkDeviceSize bufferSize = sizeof(vertices[0]) * vertices.size();
    createBuffer(bufferSize, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, vertexBuffer, vertexBufferMemory);

    void* data;
    vkMapMemory(device, vertexBufferMemory, 0, bufferSize, 0, &data);
        memcpy(data, vertices.data(), (size_t) bufferSize);
    vkUnmapMemory(device, vertexBufferMemory);
}
```

编译运行程序，确保顶点缓冲工作正常。

## 使用暂存缓冲

修改createVertexBuffer 函数，使用CPU 可见的缓冲作为临时缓冲，使用显卡读取较快的缓冲作为真正的顶点缓冲：

```cpp
void createVertexBuffer() {
    VkDeviceSize bufferSize = sizeof(vertices[0]) * vertices.size();

    VkBuffer stagingBuffer;
    VkDeviceMemory stagingBufferMemory;
    createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer, stagingBufferMemory);

    void* data;
    vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, &data);
        memcpy(data, vertices.data(), (size_t) bufferSize);
    vkUnmapMemory(device, stagingBufferMemory);

    createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, vertexBuffer, vertexBufferMemory);
}
```

现在我们可以使用新的关联stagingBufferMemory 作为内存的stagingBuffer 缓冲对象来存放CPU 加载的顶点数据。在本章节，我们会使用下面两个缓冲使用标记：

- `VK_BUFFER_USAGE_TRANSFER_SRC_BIT`：[缓冲区](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=2&q=缓冲区&zhida_source=entity)可以用作内存传输操作中的源。
- `VK_BUFFER_USAGE_TRANSFER_DST_BIT`：缓冲区可用作内存传输操作中的目标。

vertexBuffer 现在关联的内存是设备所有的，不能vkMapMemory 函数对它关联的内存进行[映射](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=1&q=映射&zhida_source=entity)。我们只能通stagingBuffer 来向vertexBuffer 复制数据。我们需要使用标记指明我们使用缓冲进行传输操作。

我们现在要编写一个函数，将内容从一个缓冲区复制到另一个缓冲区，称为`copyBuffer`.

```cpp
void copyBuffer(VkBuffer srcBuffer, VkBuffer dstBuffer, VkDeviceSize size) {

}
```

我们需要一个支持内存传输指令的[指令缓冲](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=1&q=指令缓冲&zhida_source=entity)来记录内存传输指令，然后提交到内存传输指令队列执行内存传输。通常，我们会为内存传输指令使用的指令缓冲创建另外的指令池对象，这是因为内存传输指令的指令缓存通常[生命周期](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=1&q=生命周期&zhida_source=entity)很短，为它们使用独立的指令池对象，可以进行更好的优化。我们可以在创建指令池对象时为它指定VK_COMMAND_POOL_CREATE_TRANSIENT_BIT 标记。

```cpp
void copyBuffer(VkBuffer srcBuffer, VkBuffer dstBuffer, VkDeviceSize size) {
    VkCommandBufferAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
    allocInfo.commandPool = commandPool;
    allocInfo.commandBufferCount = 1;

    VkCommandBuffer commandBuffer;
    vkAllocateCommandBuffers(device, &allocInfo, &commandBuffer);
}
```

开始记录内存传输指令：

```cpp
VkCommandBufferBeginInfo beginInfo{};
beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
beginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;

vkBeginCommandBuffer(commandBuffer, &beginInfo);
```

我们之前对绘制指令缓冲使用的VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT 标记，在这里不是必须的，这是因为我们只使用这个指令缓冲一次，等待复制操作完成后才继续程序的执行。我们可以使用VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT 标记告诉[驱动程序](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=1&q=驱动程序&zhida_source=entity)我们如何使用这个指令缓冲，来让驱动程序进行更好的优化。

```cpp
VkBufferCopy copyRegion{};
copyRegion.srcOffset = 0; // Optional
copyRegion.dstOffset = 0; // Optional
copyRegion.size = size;
vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, 1, &copyRegion);
```

我们使用vkEndCommandBuffer 指令来进行缓冲的复制操作。它以源缓冲对象和目的缓冲对象，以及一个VkBufferCopy 数组为参数。VkBufferCopy 数组指定了复制操作的源缓冲位置偏移，目的缓冲位置偏移，以及要复制的数据长度。和vkMapMemory 指令不同，这里不能使用VK_WHOLE_SIZE 来指定要复制的数据长度。

```cpp
vkEndCommandBuffer(commandBuffer);
```

我们的内存传输操作使用的指令缓冲只包含了复制指令，记录完复制指令后，我们就可以结束指令缓冲的记录操作，提交指令缓冲完成传输操作的执行：

```cpp
VkSubmitInfo submitInfo{};
submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
submitInfo.commandBufferCount = 1;
submitInfo.pCommandBuffers = &commandBuffer;

vkQueueSubmit(graphicsQueue, 1, &submitInfo, VK_NULL_HANDLE);
vkQueueWaitIdle(graphicsQueue);
```

和绘制指令不同，这一次我们直接等待传输操作完成。有两种等待内存传输操作完成的方法：一种是使用栅栏，通过vkWaitForFences 函数等待。另一种是通过vkQueueWaitIdle 函数等待。使用栅栏可以同步多个不同的内存传输操作，给驱动程序的优化空间也更大。

```cpp
vkFreeCommandBuffers(device, commandPool, 1, &commandBuffer);
```

最后，传输操作完成后我们需要清除我们使用的指令缓冲对象。 接着，我们可以在createVertexBuffer 函数中调用copyBuffer 函数复制顶点数据到显卡读取较快的缓冲中：

```cpp
createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, vertexBuffer, vertexBufferMemory);

copyBuffer(stagingBuffer, vertexBuffer, bufferSize);
```

最后，不要忘记清除我们使用的[缓冲对象](https://zhida.zhihu.com/search?content_id=219043721&content_type=Article&match_order=6&q=缓冲对象&zhida_source=entity)和它关联的内存对象：

```cpp
    ...

    copyBuffer(stagingBuffer, vertexBuffer, bufferSize);

    vkDestroyBuffer(device, stagingBuffer, nullptr);
    vkFreeMemory(device, stagingBufferMemory, nullptr);
}
```

编译运行程序，确保一切正常。暂时由于我们使用的顶点数据过于简单，性能提升并不明显。当我们渲染更为复杂的对象时，可以看到更为明显的提升。

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

