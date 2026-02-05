# Vulkan开发学习记录 21 - 图像

## 介绍

之前我们使用[顶点颜色](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=顶点颜色&zhida_source=entity)来为几何图元进行着色，这样可以产生的效果 相当有限。接下来我们将介绍使用[纹理图像](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=纹理图像&zhida_source=entity)来为几何图元着色。在之后加 载绘制简单的[三维模型](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=三维模型&zhida_source=entity)时，我们也会用纹理图像来对它进行着色。

在我们的程序中使用纹理，需要采取下面的步骤：

• 创建设备内存（显存）支持的图像对象

• 加载图像文件的像素数据

• 创建[图像采样器](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=图像采样器&zhida_source=entity)

• 使用图像采样器描述符采样纹理数据

之前，我们对图像对象已经有所熟悉，我们的渲染操作是在我们获取的[交换链](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=交换链&zhida_source=entity)图像上进行的，但我们还没有自己创建过图像对象。现在，我们将开始自己创建一个图像对象，这一过程有点类似[顶点缓冲对象](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=顶点缓冲对象&zhida_source=entity)的创建。我们首先会创建一个暂存资源，然后使用像素数据填充它，接着将像素数据从暂存资源复制到我们用来渲染的图像对象。实际上也可以直接创建暂存图像来用于渲染，Vulkan允许我们直接从VkBuffer中复制像素数据到图像对象，并且在一些硬件平台这样做效率确实要高很多。我们首先创建缓冲然后填充像素数据，接着创建一个图像对象，将缓冲中的数据复制到图像对象。创建图像对象和创建缓冲的方式区别不大，需要我们查询内存需求，分配并绑定设备内存。

但创建图像对象还是有一些地方需要我们注意。图像的布局会影响它的像素数据在内存中的组织方式。一般而言，对于现在的硬件设备，直接将像素数据按行进行存储并不能得到最佳的性能表现。不同的图像布局对图像操作也有一定的影响。在之前的章节，我们已经了解了一些图像布局：

- VK_IMAGE_LAYOUT_PRESENT_SRC_KHR：：适合呈现操作
- VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL：适合作为颜色附着，在[片段着色器](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=片段着色器&zhida_source=entity)中写入颜色数据。
- VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL：适合作为传输操作的数据来源，比如`vkCmdCopyImageToBuffer`
- VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL：适合作为传输操作的目的位置，比如`vkCmdCopyBufferToImage`
- VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL：适合在 着色器中进行采样操作

最常用的变换图像布局的方式是使用管线屏障（*pipeline barrier*）。管线屏障（*pipeline barrier*）主要被用来同步资源访问，比如保证图像在被读取之前数据被写入。它也可以被用来变换图像布局。在本章节，我们使用它进行 图像布局变换。如果队列的所有模式为VK_SHARING_MODE_EXCLUSIVE， 管线屏障（*pipeline barrier*）还可以被用来传递队列所有权。

## 图像库

有许多可以使用的加载图像资源的库，甚至读者也可以自己编写代码来加载一些格式比较简单的图像文件，比如BMP和PPM图像文件。在本教程，我们使用stb_Image库来加载图像文件。这一图像库只有一个[头文件](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=头文件&zhida_source=entity)stb_Image.h，读者可以下载它，放在一个方便的位置，然后将存放它的位置加入[编译器](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=编译器&zhida_source=entity)的包含路径，就可以使用它了。

## 载入图像

包含图像库头文件：

```cpp
#define STB_IMAGE_IMPLEMENTATION
```cpp
#include <stb_image.h>
```

```
默认情况下stb_Image.h文件只定义了[函数原型](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=函数原型&zhida_source=entity)，我们需要在包含stb_Image.h文件前定义`STB_IMAGE_IMPLEMENTATION`宏，来让它将函数实现包含进来。

```cpp
void initVulkan() {
    ...
    createCommandPool();
    createTextureImage();
    createVertexBuffer();
    ...
}

...

void createTextureImage() {

}
```

添加一个叫做createTextureImage的函数用于加载图像数据到一个Vulkan图像对象。我们需要使用[指令缓冲](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=指令缓冲&zhida_source=entity)来完成加载，所以createTextureImage函数会在createCommandPool[函数调用](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=函数调用&zhida_source=entity)之后被调用。

创建一个叫做`textures`的和`shaders`目录同级的目录用于存放图像文件。 我们会从textures目录载入一个叫做texture.jpg的图像文件。我们使用的图像大小为512x512 像素。这里使用的图像库可以载入常见格式的图像文件， 比如JPEG，PNG，BMP和GIF图像文件。

![img](./assets/v2-619b9c1fbb27d24f2e27f8e3e5b37f39_1440w.jpg)

使用图像库载入文件非常简单：

```cpp
void createTextureImage() {
    int texWidth, texHeight, texChannels;
    stbi_uc* pixels = stbi_load("textures/texture.jpg", &texWidth, &texHeight, &texChannels, STBI_rgb_alpha);
    VkDeviceSize imageSize = texWidth * texHeight * 4;

    if (!pixels) {
        throw std::runtime_error("failed to load texture image!");
    }
}
```

上面代码中的`stbi_load`函数以图像文件路径和需要加载的[颜色通道](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=颜色通道&zhida_source=entity)作为参数来加载图像文件。使用`STBI_rgb_alpha`通道参数可以强制载入[alpha通道](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=alpha通道&zhida_source=entity)，即使图像数据不包含这一通道，也会被添加上一个默认的alpha值作为alpha通道的图像数据，这为我们的处理带来了方便。stbi_load 函数还可以返回图像的宽度，高度和图像数据实际存在的颜色通道。stbi_load 函数的返回值是一个指向解析后的[图像像素](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=图像像素&zhida_source=entity)数据的指针。使用STBI_rgb_alpha 作为通道参数，每个像素需要4个字节存储，所有像素按照行的方式依次存储，总共整个图像需要texWidth * texHeight * 4 字节来存储。

## 暂存缓冲

我们创建一个CPU可见的缓冲，调用`vkMapMemory`函数映射内存，将图像像素数据复制到其中。在`createTextureImage`函数中添加临时的缓冲变量：

```cpp
VkBuffer stagingBuffer;
VkDeviceMemory stagingBufferMemory;
```

我们使用的[缓冲内存](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=缓冲内存&zhida_source=entity)需要对CPU可见，这样，我们才能[映射内存](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=2&q=映射内存&zhida_source=entity)，将图像数据复制到其中：

```cpp
createBuffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer, stagingBufferMemory);
```

接着，我们就可以映射内存，将图像数据复制到缓冲中：

```cpp
void* data;
vkMapMemory(device, stagingBufferMemory, 0, imageSize, 0, &data);
    memcpy(data, pixels, static_cast<size_t>(imageSize));
vkUnmapMemory(device, stagingBufferMemory);
```

最后，不要忘记清除我们解析得到的图像像素数据：

```cpp
stbi_image_free(pixels);
```

## 纹理图像

尽管，我们可以在着色器直接访问缓冲中的像素数据，但使用Vulkan的图像对象会更好。Vulkan的图像对象允许我们使用二维坐标来快速获取颜色数据。图像对象的像素数据也被叫做纹素。现在，让我们添加新的[类成员变量](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=类成员变量&zhida_source=entity)：

```cpp
VkImage textureImage;
VkDeviceMemory textureImageMemory;
```

创建图像参数我们需要填写`VkImageCreateInfo`结构体：

```cpp
VkImageCreateInfo imageInfo{};
imageInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
imageInfo.imageType = VK_IMAGE_TYPE_2D;
imageInfo.extent.width = static_cast<uint32_t>(texWidth);
imageInfo.extent.height = static_cast<uint32_t>(texHeight);
imageInfo.extent.depth = 1;
imageInfo.mipLevels = 1;
imageInfo.arrayLayers = 1;
```

`imageType`成员变量用于指定图像类型，Vulkan 通过它来确定图像数据的[坐标系](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=坐标系&zhida_source=entity)。图像类型可以是一维，二维和三维图像。一维图像通常被用来存储数组数据或[梯度数据](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=梯度数据&zhida_source=entity)。二维图像通常被用来存储纹理。三维图像通常被用类存储体素数据。extent成员变量用于指定图像在每个维度的范围，也就是在每个[坐标轴](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=坐标轴&zhida_source=entity)有多少纹素。我们在这里使用的是二维图像，所以depth 的值被我们设置为1，并且我们现在没有使用分级细化，所以将其设置为1。

```cpp
imageInfo.format = VK_FORMAT_R8G8B8A8_SRGB;
```

Vulkan 支持多种格式的图像数据，这里我们使用的是图像库解析的像素数据格式。

```cpp
imageInfo.tiling = VK_IMAGE_TILING_OPTIMAL;
```

tiling 成员变量可以是下面这两个值之一：

- `VK_IMAGE_TILING_LINEAR`: 纹素以[行主序](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=行主序&zhida_source=entity)的方式排列
- `VK_IMAGE_TILING_OPTIMAL`: 纹素以一种对访问优化的方式排列

tiling 成员变量的设置在之后不可以修改。如果读者需要直接访问图像数据，应该将tiling 成员变量设置为VK_IMAGE_TILING_LINEAR。由于这里我们使用暂存缓冲而不是暂存图像来存储图像数据，设置为VK_IMAGE_TILING_LINEAR 是不必要的，我们使用`VK_IMAGE_TILING_OPTIMAL`来获得更好的访问性能。

```cpp
 imageInfo.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
```

`initialLayout`成员变量可以设置为下面这些值：

• VK_IMAGE_LAYOUT_UNDEFINED：GPU不可用，纹素在第一次变换会被丢弃。

• VK_IMAGE_LAYOUT_PREINITIALIZED：GPU不可用，纹素在第 一次变换会被保留。

大多数情况下对于第一次变换，纹素没有保留的必要。但如果读者使用图像对象以及VK_IMAGE_TILING_LINEAR标记来暂存[纹理数据](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=2&q=纹理数据&zhida_source=entity)，这种情况下，纹理数据作为数据传输来源不会被丢弃。但在这里，我们是将图像对象作为传输数据的接收方，将纹理数据从缓冲对象传输到图像对象，所以我们不需要保留图像对象第一次变换时的纹理数据，使用VK_IMAGE_LAYOUT_UNDEFINED更好。

```cpp
imageInfo.usage = VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT;
```

`usage`成员变量的用法和创建缓冲时使用的`usage`成员变量用法相同。这里，我们创建的图像对象被用作传输数据的接收方。并且图像数据需要被 着色器采样，所以我们使用了VK_IMAGE_USAGE_TRANSFER_DST_BIT 和VK_IMAGE_USAGE_SAMPLED_BIT 两个使用标记。

```cpp
imageInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
```

我们的图像对象只被一个[队列族](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=队列族&zhida_source=entity)使用：支持传输操作的队列族。所以 这里我们使用[独占模式](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=独占模式&zhida_source=entity)。

```cpp
imageInfo.samples = VK_SAMPLE_COUNT_1_BIT;
imageInfo.flags = 0; // Optional
```

`samples` 成员变量用于设置[多重采样](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=多重采样&zhida_source=entity)。这一设置只对用作附着的图像对象有效，我们的图像不用于附着，将其设置为采样1次。有许多用于稀疏图像的优化标记可以使用。稀疏图像是一种离散存储图像数据的方法。比如， 我们可以使用稀疏图像来存储体素地形，避免为“空气”部分分配内存。在这里，我们没有使用flags标记，将其设置为默认值0。

```cpp
if (vkCreateImage(device, &imageInfo, nullptr, &textureImage) != VK_SUCCESS) {
    throw std::runtime_error("failed to create image!");
}
```

调用`vkCreateImage`函数创建图像对象，它的参数没有需要特别说明的地方。实际上，图形硬件也可能不支持VK_FORMAT_R8G8B8A8_SRGB 格式，读者可以使用图形硬件支持的格式来替换它。我们这里跳过检测图形硬件是否支持这一格式是因为这一格式的支持已经十分普遍。使用其它格式还需要一些其它处理。我们会在之后的章节再详细讨论与之相关的问题。

```cpp
VkMemoryRequirements memRequirements;
vkGetImageMemoryRequirements(device, textureImage, &memRequirements);

VkMemoryAllocateInfo allocInfo{};
allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
allocInfo.allocationSize = memRequirements.size;
allocInfo.memoryTypeIndex = findMemoryType(memRequirements.memoryTypeBits, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

if (vkAllocateMemory(device, &allocInfo, nullptr, &textureImageMemory) != VK_SUCCESS) {
    throw std::runtime_error("failed to allocate image memory!");
}

vkBindImageMemory(device, textureImage, textureImageMemory, 0);
```

分配图像内存的方法和分配缓冲内存几乎一模一样。首先调用`vkGetImageMemoryRequirements`函数获取图像对象的内存需求，然后调用vkAllocateMemory 函数分配内存，最后调用vkBindImageMemory函数将图像对象和内存进行关联即可。

为了简化图像对象的创建操作，我们编写了一个叫做`createImage`的辅助函数：

```cpp
void createImage(uint32_t width, uint32_t height, VkFormat format, VkImageTiling tiling, VkImageUsageFlags usage, VkMemoryPropertyFlags properties, VkImage& image, VkDeviceMemory& imageMemory) {
    VkImageCreateInfo imageInfo{};
    imageInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
    imageInfo.imageType = VK_IMAGE_TYPE_2D;
    imageInfo.extent.width = width;
    imageInfo.extent.height = height;
    imageInfo.extent.depth = 1;
    imageInfo.mipLevels = 1;
    imageInfo.arrayLayers = 1;
    imageInfo.format = format;
    imageInfo.tiling = tiling;
    imageInfo.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
    imageInfo.usage = usage;
    imageInfo.samples = VK_SAMPLE_COUNT_1_BIT;
    imageInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;

    if (vkCreateImage(device, &imageInfo, nullptr, &image) != VK_SUCCESS) {
        throw std::runtime_error("failed to create image!");
    }

    VkMemoryRequirements memRequirements;
    vkGetImageMemoryRequirements(device, image, &memRequirements);

    VkMemoryAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
    allocInfo.allocationSize = memRequirements.size;
    allocInfo.memoryTypeIndex = findMemoryType(memRequirements.memoryTypeBits, properties);

    if (vkAllocateMemory(device, &allocInfo, nullptr, &imageMemory) != VK_SUCCESS) {
        throw std::runtime_error("failed to allocate image memory!");
    }

    vkBindImageMemory(device, image, imageMemory, 0);
}
```

上面代码我们将图像宽度、高度、格式、tilling 模式、使用标记、内存属性作为函数参数，之后的章节，我们将直接使用这一函数来创建图像对象。

现在`createTextureImage`函数可以简化为下面这个样子：

```cpp
 void createTextureImage() {
    int texWidth, texHeight, texChannels;
    stbi_uc* pixels = stbi_load("textures/texture.jpg", &texWidth, &texHeight, &texChannels, STBI_rgb_alpha);
    VkDeviceSize imageSize = texWidth * texHeight * 4;

    if (!pixels) {
        throw std::runtime_error("failed to load texture image!");
    }

    VkBuffer stagingBuffer;
    VkDeviceMemory stagingBufferMemory;
    createBuffer(imageSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBuffer, stagingBufferMemory);

    void* data;
    vkMapMemory(device, stagingBufferMemory, 0, imageSize, 0, &data);
        memcpy(data, pixels, static_cast<size_t>(imageSize));
    vkUnmapMemory(device, stagingBufferMemory);

    stbi_image_free(pixels);

    createImage(texWidth, texHeight, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, textureImage, textureImageMemory);
}
```

## 布局变换

接下来我们开始记录传输指令到指令缓冲，我们为此编写了两个辅助函数：

```cpp
VkCommandBuffer beginSingleTimeCommands() {
    VkCommandBufferAllocateInfo allocInfo{};
    allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
    allocInfo.commandPool = commandPool;
    allocInfo.commandBufferCount = 1;

    VkCommandBuffer commandBuffer;
    vkAllocateCommandBuffers(device, &allocInfo, &commandBuffer);

    VkCommandBufferBeginInfo beginInfo{};
    beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
    beginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;

    vkBeginCommandBuffer(commandBuffer, &beginInfo);

    return commandBuffer;
}

void endSingleTimeCommands(VkCommandBuffer commandBuffer) {
    vkEndCommandBuffer(commandBuffer);

    VkSubmitInfo submitInfo{};
    submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
    submitInfo.commandBufferCount = 1;
    submitInfo.pCommandBuffers = &commandBuffer;

    vkQueueSubmit(graphicsQueue, 1, &submitInfo, VK_NULL_HANDLE);
    vkQueueWaitIdle(graphicsQueue);

    vkFreeCommandBuffers(device, commandPool, 1, &commandBuffer);
}
```

上面的代码大部分来自copyBuffer 函数，现在我们可以用它来简化copyBuffer 函数的实现：

```cpp
void copyBuffer(VkBuffer srcBuffer, VkBuffer dstBuffer, VkDeviceSize size) {
    VkCommandBuffer commandBuffer = beginSingleTimeCommands();

    VkBufferCopy copyRegion{};
    copyRegion.size = size;
    vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, 1, &copyRegion);

    endSingleTimeCommands(commandBuffer);
}
```

如果我们使用的是缓冲对象而不是图像对象，那么就可以记录传输指令，然后调用`vkCmdCopyBufferToImage`函数结束工作，但这一指令需要图像满足一定的布局要求，所以需要我们编写一个新的函数来进行图像布局变换：

```cpp
void transitionImageLayout(VkImage image, VkFormat format, VkImageLayout oldLayout, VkImageLayout newLayout) {
    VkCommandBuffer commandBuffer = beginSingleTimeCommands();

    endSingleTimeCommands(commandBuffer);
}
```

通过图像[内存屏障](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=内存屏障&zhida_source=entity)我们可以对图像布局进行变换。管线屏障主要被用来同步资源访问，比如保证图像在被读取之前数据被写入。它也可以被用来变换图像布局。在本章节，我们使用它进行图像布局变换。如果队列的所有模式为`VK_SHARING_MODE_EXCLUSIVE`， 管线屏障还可以被用来传递队列所有权。对于缓冲对象也有一个可以实现同样效果的缓冲内存屏障。

```cpp
VkImageMemoryBarrier barrier{};
barrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
barrier.oldLayout = oldLayout;
barrier.newLayout = newLayout;
```

`oldLayout`和`newLayout`成员变量用于指定布局变换。如果不需要访问之前的图像数据，可以将`oldLayout`设置为`VK_IMAGE_LAYOUT_UNDEFINED`来获得更好的性能表现。

```cpp
barrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
barrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
```

如果读者使用屏障来传递队列族所有权，那么就需要对srcQueueFamilyIndex 和dstQueueFamilyIndex 变量进行设置。如果读者不进行队列所有权传递，则必须将这两个成员变量的值设置为`VK_QUEUE_FAMILY_IGNORED`。

```cpp
 barrier.image = image;
barrier.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
barrier.subresourceRange.baseMipLevel = 0;
barrier.subresourceRange.levelCount = 1;
barrier.subresourceRange.baseArrayLayer = 0;
barrier.subresourceRange.layerCount = 1;
```

`image`和`subresourceRange`成员变量用于指定进行布局变换的图像对象， 以及受影响的图像范围。这里，我们使用的图像不存在细分级别，所以将level 和layer 的值都设置为1。

```cpp
barrier.srcAccessMask = 0; // TODO
barrier.dstAccessMask = 0; // TODO
```

我们需要指定在屏障之前必须发生的资源操作类型，以及必须等待屏障的资源操作类型。虽然我们已经使用`vkQueueWaitIdle`函数来手动地进行同步，但还是需要我们进行这一设置。但这一设置依赖旧布局和新布局， 所以我们会在确定使用的布局变换后再来设置它。

```cpp
vkCmdPipelineBarrier(
    commandBuffer,
    0 /* TODO */, 0 /* TODO */,
    0,
    0, nullptr,
    0, nullptr,
    1, &barrier
);
```

提交管线屏障对象需要调用vkCmdPipelineBarrier 函数。vkCmdPipelineBarrier 函数除了指令缓冲对象外的第一个参数用于指定发生在屏障之前的管线阶段， 第二个参数用于指定发生在屏障之后的管线阶段。如果读者想要在一个屏障之后读取uniform，应该指定`VK_ACCESS_UNIFORM_READ_BIT`使用标 记和最早读取uniform 的着色器阶段，比如VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT。 当指定和使用标记不匹配的管线阶段时校验层会发出警告信息。

第三个参数可以设置为0 或VK_DEPENDENCY_BY_REGION_BIT。设置为VK_DEPENDENCY_BY_REGION_BIT的话，屏障就变成了一个区域 条件。这允许我们读取资源目前已经写入的那部分。

最后戶个参数用于引用三种可用的管线屏障数组：内存屏障，缓冲内存屏障和图像内存屏障。我们这里使用的是图像内存屏障。需要注意这里我们没有使用`VkFormat`参数，但我们会在之后章节使 用它进行特殊的变换操作。

## 复制缓冲到图像

在我们回到createTextureImage 函数之前，先编写一个新的辅助函数： copyBufferToImage。

```cpp
void copyBufferToImage(VkBuffer buffer, VkImage image, uint32_t width, uint32_t height) {
    VkCommandBuffer commandBuffer = beginSingleTimeCommands();

    endSingleTimeCommands(commandBuffer);
}
```

和复制缓冲数据一样，我们需要使用`VkBufferImageCopy`结构体指定将数据复制到图像的哪一部分。

```cpp
VkBufferImageCopy region{};
region.bufferOffset = 0;
region.bufferRowLength = 0;
region.bufferImageHeight = 0;

region.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
region.imageSubresource.mipLevel = 0;
region.imageSubresource.baseArrayLayer = 0;
region.imageSubresource.layerCount = 1;

region.imageOffset = {0, 0, 0};
region.imageExtent = {
    width,
    height,
};
```

`bufferOffset`成员变量用于指定要复制的数据在缓冲中的偏移位置。`bufferRowLength`和`bufferImageHeight`变量用于指定数据在内存中的存放方式。通过这两个成员变量我们可以对每行图像数据使用额外的空间进行对齐。将这两个成员变量的值都设置为0，数据将会在内存中被紧凑存放。imageSubresource、`imageOffset`和`imageExtent`成员变量用于指定数据被复制到图像的哪一部分。

从缓冲复制数据到图像需要调用`vkCmdCopyBufferToImage`函数来记录指令到指令缓冲：

```cpp
vkCmdCopyBufferToImage(
    commandBuffer,
    buffer,
    image,
    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
    1,
    &region
);
```

vkCmdCopyBufferToImage函数的第4个参数用于指定目的图像当前使 用的图像布局。这里我们假设图像已经被变换为最适合作为复制目的的布 局。我们只复制了一张图像，实际上是可以指定一个VkBufferImageCopy数组来一次从一个缓冲复制数据到多个不同的图像对象。

## 准备纹理图像

现在我们可以回到`createTextureImage`函数。复制暂存缓冲中的数据到纹理图像，我们需要进行下面两步操作：

- 变换纹理图像到`VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL`
- 执行图像数据复制操作

```cpp
transitionImageLayout(textureImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
copyBufferToImage(stagingBuffer, textureImage, static_cast<uint32_t>(texWidth), static_cast<uint32_t>(texHeight));
```

## 变换屏障掩码

如果读者在开启校验层的情况下运行程序，会发现校验层报告`transitionImageLayout`中 使用的访问掩码和管线阶段是无效的。我们需要根据布局变换设置`transitionImageLayout`。

我们需要处理两种变换：

• 未定义→传输目的：传输操作的数据写入不需要等待

• 传输目的→着色器读取：着色器读取图像数据需要等待传输操作的写入结束。

我们使用下面的代码指定变换规则：

```cpp
VkPipelineStageFlags sourceStage;
VkPipelineStageFlags destinationStage;

if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
    barrier.srcAccessMask = 0;
    barrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;

    sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
    destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
} else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
    barrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
    barrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT;

    sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
    destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
} else {
    throw std::invalid_argument("unsupported layout transition!");
}

vkCmdPipelineBarrier(
    commandBuffer,
    sourceStage, destinationStage,
    0,
    0, nullptr,
    0, nullptr,
    1, &barrier
);
```

传输的写入操作必须在管线传输阶段进行。这里因为我们的写入操作不需要等待任何对象，我们可以指定一个空的的访问掩码，使用`VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT`最早出现的管线阶段。需要注意`VK_PIPELINE_STAGE_TRANSFER_BIT`并非图形和计算管线中真实存在的管线阶段，它实际上是一个伪阶段，出现在传输操作发生时。更多信息可以参考官方文档中有关伪阶段的说明。

图像数据需要在片段着色器读取，所以我们指定了片段着色器管线阶段的读取访问掩码。

之后如果还需要进行更多的图像布局变换，我们会扩展transitionImageLayout函数。

指令缓冲的提交会隐式地进行`VK_ACCESS_HOST_WRITE_BIT`同步。 因为transitionImageLayout函数执行的指令缓冲只包含了一条指令，如果布局转换依赖VK_ACCESS_HOST_WRITE_BIT，我们可以通过设置`srcAccessMask`的值为0来使用这一隐含的同步。不过，我们最好显式地定义一切，依赖于[隐含属性](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=隐含属性&zhida_source=entity)，不就变得和OpenGL一样容易出现错误。

有一个特殊的支持所有操作的图像布局类型：VK_IMAGE_LAYOUT_GENERAL。 但它并不保证能为所有操作都带来最佳性能表现。对于一些特殊情况，比如将图像同时用作输入和输出对象，或读取一个已经改变初始化时的布局的图像时，需要使用它。

目前为止，我们编写的包含提交指令操作的辅助函数都被设置为通过等待队列空闲来进行同步。对于实用的程序，更推荐组合多个操作到一个指令缓冲对象，通过异步执行来增大[吞吐量](https://zhida.zhihu.com/search?content_id=223592074&content_type=Article&match_order=1&q=吞吐量&zhida_source=entity)，特别对于`createTextureImage`函数中的变换和数据复制操作，这样做可以获得很大的性能提升。我们可以编写一个叫做`setupCommandBuffer`的辅助函数来记录指令，编写一个叫做`flushSetupCommands`来提交执行记录的指令，通过实验得到最佳的同步策略。

## 清理

最后，不要忘记在`createTextureImage`函数的结尾清除我们使用的暂存缓冲和它关联的内存:

```cpp
    transitionImageLayout(textureImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

    vkDestroyBuffer(device, stagingBuffer, nullptr);
    vkFreeMemory(device, stagingBufferMemory, nullptr);
```

纹理图像一直被使用到程序结束才被我们清除：

```cpp
void cleanup() {
    cleanupSwapChain();

    vkDestroyImage(device, textureImage, nullptr);
    vkFreeMemory(device, textureImageMemory, nullptr);

    ...
}
```

图像数据被加载到图像对象后，还需要一定的设置才能被访问。下一 章节，我们会介绍访问我们加载的图像数据的方法。

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

