# Vulkan开发学习记录 17 - 创建顶点缓冲

## 介绍

Vulkan 的缓冲是可以存储任意数据的可以被[显卡](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=显卡&zhida_source=entity)读取的内存。缓冲除了用来存储顶点数据，还有很多其它用途。和之前我们见到的Vulkan 对象不同，缓冲对象并不自动地为它们自己分配内存。

## 创建缓冲

添加一个叫做createVertexBuffer 函数，然后在initVulkan 函数中createCommandBuffers [函数调用](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=函数调用&zhida_source=entity)之后调用它：

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
    createVertexBuffer();
    createCommandBuffers();
    createSyncObjects();
}

...

void createVertexBuffer() {

}
```

填写VkBufferCreateInfo 结构体：

```cpp
VkBufferCreateInfo bufferInfo{};
bufferInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
bufferInfo.size = sizeof(vertices[0]) * vertices.size();
```

size 成员变量用于指定要创建的缓冲所占字节大小。我们可以通过sizeof 来计算顶点数据数组的字节大小。

```cpp
bufferInfo.usage = VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
```

usage [成员变量](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=2&q=成员变量&zhida_source=entity)用于指定缓冲中的数据的使用目的。可以使用位或运算来指定多个使用目的。在这里，我们将缓冲用作存储顶点数据。

```cpp
bufferInfo.sharingMode = VK SHARING_MODE_EXCLUSIVE;
```

和交换链图像一样，缓冲可以被特定的队列族所拥有，也可以同时在多个队列族之前共享。在这里，我们只使用了一个队列，所以选择使用独有模式。

flags 成员变量用于配置缓冲的内存稀疏程度，我们将其设置为0使用默认值。

填写完结构体信息，我们就可以调用vkCreateBuffer 函数来完成缓冲创建。我们添加一个类成员变量来存储创建的缓冲的句柄：

```cpp
VkBuffer vertexBuffer;

...

void createVertexBuffer() {
    VkBufferCreateInfo bufferInfo{};
    bufferInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
    bufferInfo.size = sizeof(vertices[0]) * vertices.size();
    bufferInfo.usage = VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
    bufferInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;

    if (vkCreateBuffer(device, &bufferInfo, nullptr, &vertexBuffer) != VK_SUCCESS) {
        throw std::runtime_error("failed to create vertex buffer!");
    }
}
```

缓冲不依赖[交换链](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=2&q=交换链&zhida_source=entity)，所以我们不需要在交换链重建时重建缓冲。应用程序结束时，我们需要清除我们创建的缓冲对象：

```cpp
void cleanup() {
    cleanupSwapChain();

    vkDestroyBuffer(device, vertexBuffer, nullptr);

    ...
}
```

## 内存需求

缓冲创建后实际还没有给它分配任何内存。分配[缓冲内存](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=缓冲内存&zhida_source=entity)前，我们需要调用vkGetBufferMemoryRequirements 函数获取缓冲的内存需求。

```cpp
VkMemoryRequirements memRequirements;
vkGetBufferMemoryRequirements(device, vertexBuffer, &memRequirements);
```

vkGetBufferMemoryRequirements 函数返回的VkMemoryRequirements 结构体有下面这三个成员变量：

• size：缓冲需要的内存的字节大小，它可能和bufferInfo.size 的值不同。

• alignment：缓冲在实际被分配的内存中的开始位置。它的值依赖于bufferInfo.usage 和bufferInfo.flags

• memoryTypeBits：指示适合该缓冲使用的[内存类型](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=内存类型&zhida_source=entity)的[位域](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=位域&zhida_source=entity)。

显卡可以分配不同类型的内存作为缓冲使用。不同类型的内存所允许进行的操作以及操作的效率有所不同。我们需要结合自己的需求选择最合适的内存类型使用。我们添加一个叫做findMemoryType 的函数来做件事：

```cpp
uint32_t findMemoryType(uint32_t typeFilter, VkMemoryPropertyFlags properties) {

}
```

首先，我们使用vkGetPhysicalDeviceMemoryProperties 函数查询物理设备可用的内存类型：

```cpp
VkPhysicalDeviceMemoryProperties memProperties;
vkGetPhysicalDeviceMemoryProperties(physicalDevice, &memProperties);
```

VkPhysicalDeviceMemoryProperties 扳函数返回的vkGetPhysicalDeviceMemoryProperties 结构体包含了memoryTypes 和memoryHeaps 两个数组成员变量。memoryHeaps 数组成员变量中的每个元素是一种内存来源，比如[显存](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=显存&zhida_source=entity)以及显存用尽后的位于主内存种的[交换空间](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=交换空间&zhida_source=entity)。

现在，我们暂时不关心这些内存的来源。 遍历数组，查找缓冲可用的内存类型：

```cpp
for (uint32_t i = 0; i < memProperties.memoryTypeCount; i++) {
    if (typeFilter & (1 << i)) {
        return i;
    }
}

throw std::runtime_error("failed to find suitable memory type!");
```

typeFilter 参数用于指定我们需要的内存类型的位域。我们只需要遍历可用内存类型数组，检测每个内存类型是否满足我们需要即可(相应位域为1)。 我们需要位域满足VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT (用于从CPU 写入数据) 和VK_MEMORY_PROPERTY_HOST_COHERENT_BIT 的内存类型。修改代码，检测我们满足我们需要的内存类型：

```cpp
for (uint32_t i = 0; i < memProperties.memoryTypeCount; i++) {
    if ((typeFilter & (1 << i)) && (memProperties.memoryTypes[i].propertyFlags & properties) == properties) {
        return i;
    }
}
```

由于我们不只一个需要的内存属性，所以仅仅检测位与运算的结果是否非0是不够的，还需要检测它是否与我们需要的属性的位域完全相同。

## [内存分配](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=内存分配&zhida_source=entity)

确定好使用的内存类型后，我们可以填写VkMemoryAllocateInfo 结构体来分配需要的内存：

```cpp
VkMemoryAllocateInfo allocInfo{};
allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
allocInfo.allocationSize = memRequirements.size;
allocInfo.memoryTypeIndex = findMemoryType(memRequirements.memoryTypeBits, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
```

内存分配只需要填写好需要的内存大小和内存类型，然后调用vkAllocateMemory 函数分配内存即可：

```cpp
VkBuffer vertexBuffer;
VkDeviceMemory vertexBufferMemory;

...

if (vkAllocateMemory(device, &allocInfo, nullptr, &vertexBufferMemory) != VK_SUCCESS) {
    throw std::runtime_error("failed to allocate vertex buffer memory!");
}
```

如果内存分配成功，我们就可以使用vkBindBufferMemory 函数将分配的内存和缓冲对象进行关联：

```cpp
vkBindBufferMemory(device, vertexBuffer, vertexBufferMemory, 0);
```

vkBindBufferMemory 函数的前三个参数非常直白，第四个参数是[偏移值](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=偏移值&zhida_source=entity)。这里我们将内存用作[顶点缓冲](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=顶点缓冲&zhida_source=entity)，可以将其设置为0。偏移值需要满足能够被memRequirements.alignment 整除。

和C++ 的[动态内存分配](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=动态内存分配&zhida_source=entity)一样，这里分配的内存需要我们自己进行释放。 通常我们在缓冲不再使用时，释放它所关联的内存：

```cpp
void cleanup() {
    cleanupSwapChain();

    vkDestroyBuffer(device, vertexBuffer, nullptr);
    vkFreeMemory(device, vertexBufferMemory, nullptr);
}
```

## 填充顶点缓冲

现在，我们可以开始将顶点数据复制到缓冲中。我们需要使用vkMapMemory 函数将缓冲关联的内存映射到GPU 可以访问的内存：

```cpp
void* data;
vkMapMemory(device, vertexBufferMemory, 0, bufferInfo.size, 0, &data);
```

vkMapMemory 函数允许我们通过给定的内存偏移值和内存大小访问特定的内存资源。这里我们使用的偏移值和内存大小分别时0 和 bufferInfo.size。 还有一个特殊值VK_WHOLE_SIZE 可以用来映射整个申请的内存。vkMapMemory 函数的倒数第二个参数可以用来指定一个标记，但对于目前版本的Vulkan 来说，这个参数还没有可以使用的标记，必须将其设置为9。最后一个参数用于返回[内存映射](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=2&q=内存映射&zhida_source=entity)后的地址。

```cpp
void* data;
vkMapMemory(device, vertexBufferMemory, 0, bufferInfo.size, 0, &data);
    memcpy(data, vertices.data(), (size_t) bufferInfo.size);
vkUnmapMemory(device, vertexBufferMemory);
```

现在可以使用memcpy 将顶点数据复制到映射后的内存，然后调用`vkUnmapMemory`函数来结束内存映射。然而，[驱动程序](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=驱动程序&zhida_source=entity)可能并不会立即复制数据到缓冲关联的内存中去，这是由于现代处理器都存在缓存这一设计，写入内存的数据并不一定在多个核心同时可见，有下面两种方法可以保证数据被立即复制到缓冲关联的内存中去：

• 使用带有VK_MEMORY_PROPERTY_HOST_COHERENT_BIT 属性的内存类型，保证内存可见的一致性 .

• 写入数据到映射的内存后，调用`vkFlushMappedMemoryRanges`函数， 读取映射的内存数据前调用`vkInvalidateMappedMemoryRanges`函数

在这里，我们使用第一种方法，它可以保证映射的内存的内容和缓冲关联的内存的内容一致。但使用这种方式，会比第二种方式些许降低性能表现。

## 绑定顶点缓冲

扩展createCommandBuffers 函数，使用顶点缓冲进行渲染操作：

```cpp
vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

VkBuffer vertexBuffers[] = {vertexBuffer};
VkDeviceSize offsets[] = {0};
vkCmdBindVertexBuffers(commandBuffer, 0, 1, vertexBuffers, offsets);

vkCmdDraw(commandBuffer, static_cast<uint32_t>(vertices.size()), 1, 0, 0);
```

我们使用`vkCmdBindVertexBuffers`函数来绑定顶点缓冲。它的第二和第三个参数用于指定偏移值和我们要绑定的顶点缓冲的数量。它的最后两个参数用于指定需要绑定的顶点缓冲数组以及顶点数据在顶点缓冲中的偏移值数组。我们还需要修改vkCmdDraw 函数调用，使用顶点缓冲中的顶点个数替换之前[硬编码](https://zhida.zhihu.com/search?content_id=219040750&content_type=Article&match_order=1&q=硬编码&zhida_source=entity)的数字 3。

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)