# 【Vulkan 入门系列】创建纹理图像，将暂存区图像拷贝到纹理图像，创建纹理图像视图（七）

这一节主要关注创建纹理图像，将暂存区图像拷贝到纹理图像和创建纹理图像视图。

## 一、创建纹理图像

`createTextureImage` 是纹理初始化的核心部分，但需配合数据传输和布局管理才能完整使用纹理。

```
void HelloVK::initVulkan() {
createInstance();
createSurface();
pickPhysicalDevice();
createLogicalDeviceAndQueue();
setupDebugMessenger();
establishDisplaySizeIdentity();
createSwapChain();
createImageViews();
createRenderPass();
createDescriptorSetLayout();
createGraphicsPipeline();
createFramebuffers();
createCommandPool();
createCommandBuffer();
decodeImage();
createTextureImage();
  ...
}

void HelloVK::createTextureImage() {
  VkImageCreateInfo imageInfo{};
  imageInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;
  imageInfo.imageType = VK_IMAGE_TYPE_2D;
  imageInfo.extent.width = textureWidth;
  imageInfo.extent.height = textureHeight;
  imageInfo.extent.depth = 1;
  imageInfo.mipLevels = 1;
  imageInfo.arrayLayers = 1;
  imageInfo.format = VK_FORMAT_R8G8B8A8_UNORM;
  imageInfo.tiling = VK_IMAGE_TILING_OPTIMAL;
  imageInfo.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
  imageInfo.usage =
      VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT;
  imageInfo.samples = VK_SAMPLE_COUNT_1_BIT;
  imageInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;

VK_CHECK(vkCreateImage(device, &imageInfo, nullptr, &textureImage));

  VkMemoryRequirements memRequirements;
vkGetImageMemoryRequirements(device, textureImage, &memRequirements);

  VkMemoryAllocateInfo allocInfo{};
  allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
  allocInfo.allocationSize = memRequirements.size;
  allocInfo.memoryTypeIndex = findMemoryType(memRequirements.memoryTypeBits,
                                          VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

VK_CHECK(vkAllocateMemory(device, &allocInfo, nullptr, &textureImageMemory));

vkBindImageMemory(device, textureImage, textureImageMemory, 0);
}
```

### 1.1 图像创建信息配置 (`VkImageCreateInfo`)

```
VkImageCreateInfo imageInfo{};
imageInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO; // 结构体类型标识
imageInfo.imageType = VK_IMAGE_TYPE_2D; // 2D 纹理
imageInfo.extent.width = textureWidth;  // 纹理宽度
imageInfo.extent.height = textureHeight; // 纹理高度
imageInfo.extent.depth = 1;             // 深度为 1（2D 图像）
imageInfo.mipLevels = 1;                // 无多级渐远纹理
imageInfo.arrayLayers = 1;              // 无分层（非纹理数组）
imageInfo.format = VK_FORMAT_R8G8B8A8_UNORM; // R8G8B8A8 归一化格式
imageInfo.tiling = VK_IMAGE_TILING_OPTIMAL;  // 内存布局对 GPU 高效
imageInfo.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED; // 初始布局未定义
imageInfo.usage = VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT; // 用途：传输目标 + 采样器读取
imageInfo.samples = VK_SAMPLE_COUNT_1_BIT; // 无多重采样
imageInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE; // 独占访问（无需跨队列共享）
```

### 1.2 创建图像对象 (`vkCreateImage`)

使用配置的 `imageInfo` 创建 `VkImage` 对象 `textureImage`。

```
VK_CHECK(vkCreateImage(device, &imageInfo, nullptr, &textureImage));
```

### 1.3 查询内存需求 (`vkGetImageMemoryRequirements`)

获取图像需要的内存大小、对齐和兼容的内存类型（`memRequirements.memoryTypeBits`）。

```
VkMemoryRequirements memRequirements;
vkGetImageMemoryRequirements(device, textureImage, &memRequirements);
```

### 1.4 内存分配信息配置 (`VkMemoryAllocateInfo`)

```
VkMemoryAllocateInfo allocInfo{};
allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
allocInfo.allocationSize = memRequirements.size; // 所需内存大小
allocInfo.memoryTypeIndex = findMemoryType(      // 查找合适的内存类型
    memRequirements.memoryTypeBits, 
    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
);
```

### 1.5 分配设备内存 (`vkAllocateMemory`)

根据 `allocInfo` 分配设备内存 `textureImageMemory`。

```
VK_CHECK(vkAllocateMemory(device, &allocInfo, nullptr, &textureImageMemory));
```

### 1.6 绑定内存到图像 (`vkBindImageMemory`)

将分配的 `textureImageMemory` 绑定到 `textureImage`，偏移量为 `0`。

```
vkBindImageMemory(device, textureImage, textureImageMemory, 0);
```

## 二、将暂存区图像拷贝到纹理图像

```
void HelloVK::initVulkan() {
createInstance();
createSurface();
pickPhysicalDevice();
createLogicalDeviceAndQueue();
setupDebugMessenger();
establishDisplaySizeIdentity();
createSwapChain();
createImageViews();
createRenderPass();
createDescriptorSetLayout();
createGraphicsPipeline();
createFramebuffers();
createCommandPool();
createCommandBuffer();
decodeImage();
createTextureImage();
copyBufferToImage();
  ...
}

void HelloVK::copyBufferToImage() {
  VkImageSubresourceRange subresourceRange{};
  subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
  subresourceRange.baseMipLevel = 0;
  subresourceRange.levelCount = 1;
  subresourceRange.layerCount = 1;

  VkImageMemoryBarrier imageMemoryBarrier{};
  imageMemoryBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
  imageMemoryBarrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
  imageMemoryBarrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
  imageMemoryBarrier.image = textureImage;
  imageMemoryBarrier.subresourceRange = subresourceRange;
  imageMemoryBarrier.srcAccessMask = 0;
  imageMemoryBarrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
  imageMemoryBarrier.oldLayout = VK_IMAGE_LAYOUT_UNDEFINED;
  imageMemoryBarrier.newLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;

  VkCommandBuffer cmd;
  VkCommandBufferAllocateInfo cmdAllocInfo{};
  cmdAllocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
  cmdAllocInfo.commandPool = commandPool;
  cmdAllocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
  cmdAllocInfo.commandBufferCount = 1;

VK_CHECK(vkAllocateCommandBuffers(device, &cmdAllocInfo, &cmd));

  VkCommandBufferBeginInfo beginInfo{};
  beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
vkBeginCommandBuffer(cmd, &beginInfo);

vkCmdPipelineBarrier(cmd, VK_PIPELINE_STAGE_HOST_BIT,
                       VK_PIPELINE_STAGE_TRANSFER_BIT, 0, 0, nullptr, 0,
                       nullptr, 1, &imageMemoryBarrier);

  VkBufferImageCopy bufferImageCopy{};
  bufferImageCopy.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
  bufferImageCopy.imageSubresource.mipLevel = 0;
  bufferImageCopy.imageSubresource.baseArrayLayer = 0;
  bufferImageCopy.imageSubresource.layerCount = 1;
  bufferImageCopy.imageExtent.width = textureWidth;
  bufferImageCopy.imageExtent.height = textureHeight;
  bufferImageCopy.imageExtent.depth = 1;
  bufferImageCopy.bufferOffset = 0;

vkCmdCopyBufferToImage(cmd, stagingBuffer, textureImage,
                         VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                         1, &bufferImageCopy);

  imageMemoryBarrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;
  imageMemoryBarrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT;
  imageMemoryBarrier.oldLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
  imageMemoryBarrier.newLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

vkCmdPipelineBarrier(cmd, VK_PIPELINE_STAGE_TRANSFER_BIT,
                       VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT, 0, 0, nullptr,
                       0, nullptr, 1, &imageMemoryBarrier);

vkEndCommandBuffer(cmd);

  VkSubmitInfo submitInfo{};
  submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
  submitInfo.commandBufferCount = 1;
  submitInfo.pCommandBuffers = &cmd;

VK_CHECK(vkQueueSubmit(graphicsQueue, 1, &submitInfo, VK_NULL_HANDLE));
vkQueueWaitIdle(graphicsQueue);
}
```

### 2.1 图像子资源范围定义 (`VkImageSubresourceRange`)

定义图像中被操作的部分（颜色通道、Mip 层级、数组层），用于后续的屏障和复制操作。

```
VkImageSubresourceRange subresourceRange{};
subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT; // 操作颜色通道
subresourceRange.baseMipLevel = 0;                        // 起始 Mip 层级
subresourceRange.levelCount = 1;                          // 仅操作 1 个 Mip 层级
subresourceRange.layerCount = 1;                          // 仅操作 1 个数组层
```

- ```
  VK_IMAGE_ASPECT_COLOR_BIT
  ```

  ：深度通道

- ```
  VK_IMAGE_ASPECT_DEPTH_BIT
  ```

  ：深度通道

### 2.2 第一次图像内存屏障 (`VkImageMemoryBarrier`)

转换图像布局为 `VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL`，确保图像准备好接收传输数据。布局转换必须通过管线屏障完成，以保障 GPU 操作的顺序性。

```
VkImageMemoryBarrier imageMemoryBarrier{};
imageMemoryBarrier.sType = VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER;
imageMemoryBarrier.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED; // 无队列族切换
imageMemoryBarrier.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED;
imageMemoryBarrier.image = textureImage;                          // 目标图像
imageMemoryBarrier.subresourceRange = subresourceRange;           // 操作的子资源范围
imageMemoryBarrier.srcAccessMask = 0;                             // 之前没有需要同步的访问操作。
imageMemoryBarrier.dstAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT;  // 后续操作需要写入权限
imageMemoryBarrier.oldLayout = VK_IMAGE_LAYOUT_UNDEFINED;         // 原始布局
imageMemoryBarrier.newLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL; // 目标布局
```

`VkImageMemoryBarrier` 是 Vulkan 中用于同步图像内存访问和布局转换的核心结构体。它定义了图像资源的访问依赖关系和布局转换规则，确保 GPU 操作之间的正确顺序和数据一致性。以下是对其关键字段和作用的详细解释：

```
1.srcAccessMask 和 ``dstAccessMask
```

`srcAccessMask`：在屏障之前的操作需要同步的访问类型。
`dstAccessMask`：在屏障之后的操作需要的访问类型。

**常见值**

- ```
  VK_ACCESS_TRANSFER_WRITE_BIT
  ```

  （传输写入）

- ```
  VK_ACCESS_SHADER_READ_BIT
  ```

  （着色器读取）

- ```
  VK_ACCESS_HOST_READ_BIT
  ```

  （主机读取）

```
2. oldLayout 和 ``newLayout
```

`oldLayout`：当前图像布局。
`newLayout`：目标图像布局。

定义图像布局转换。Vulkan 要求显式声明图像布局，不同布局对 GPU 操作的访问方式有严格限制。

**常见布局**

- ```
  VK_IMAGE_LAYOUT_UNDEFINED
  ```

  ：初始布局（内容可能被丢弃）。

- ```
  VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
  ```

  ：传输写入最优布局。

- ```
  VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
  ```

  ：着色器采样最优布局。

```
3. srcQueueFamilyIndex 和 ``dstQueueFamilyIndex
```

`srcQueueFamilyIndex`：源队列族索引。
`dstQueueFamilyIndex`：目标队列族索引。

处理队列族所有权转移。若图像需要从一个队列族转移到另一个队列族（如从传输队列到图形队列），需在此指定队列族索引。

**特殊值**

- ```
  VK_QUEUE_FAMILY_IGNORED
  ```

  ：不进行所有权转移。

- ```
  VK_QUEUE_FAMILY_EXTERNAL
  ```

  ：用于跨设备或外部 API 的同步。

```
4. image
```

：指定要操作的图像。

### 2.3 分配并开始记录命令缓冲区

分配一个主命令缓冲区并开始记录传输操作。

```
VkCommandBuffer cmd;
// 分配命令缓冲区
VkCommandBufferAllocateInfo cmdAllocInfo{};
cmdAllocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
cmdAllocInfo.commandPool = commandPool;                   // 使用的命令池
cmdAllocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;     // 主命令缓冲区
cmdAllocInfo.commandBufferCount = 1;
VK_CHECK(vkAllocateCommandBuffers(device, &cmdAllocInfo, &cmd));

// 开始记录命令
VkCommandBufferBeginInfo beginInfo{};
beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
vkBeginCommandBuffer(cmd, &beginInfo);
```

### 2.4 执行第一次管线屏障（布局转换）

插入管线屏障，将图像布局从 `VK_IMAGE_LAYOUT_UNDEFINED` 转换为 `VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL`。此屏障确保图像在传输操作开始前处于正确的布局。

```
vkCmdPipelineBarrier(
    cmd, 
    VK_PIPELINE_STAGE_HOST_BIT,           // 源阶段：主机准备数据完成
    VK_PIPELINE_STAGE_TRANSFER_BIT,       // 目标阶段：传输操作
    0, 0, nullptr, 0, nullptr, 
    1, &imageMemoryBarrier
);
```

- 源阶段：`VK_PIPELINE_STAGE_HOST_BIT` 表示数据已由 CPU（主机）准备就绪（例如通过暂存缓冲区写入）。
- 目标阶段：`VK_PIPELINE_STAGE_TRANSFER_BIT` 表示后续将进行传输操作。

### 2.5 从缓冲区复制数据到图像

将数据从暂存缓冲区 (`stagingBuffer`) 复制到图像。

```
VkBufferImageCopy bufferImageCopy{};
bufferImageCopy.imageSubresource.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
bufferImageCopy.imageSubresource.mipLevel = 0;                    // 目标Mip层级
bufferImageCopy.imageSubresource.baseArrayLayer = 0;              // 起始数组层
bufferImageCopy.imageSubresource.layerCount = 1;                  // 复制的层数
bufferImageCopy.imageExtent.width = textureWidth;                 //图像宽
bufferImageCopy.imageExtent.height = textureHeight;                //图像高
bufferImageCopy.imageExtent.depth = 1;                   //深度
bufferImageCopy.bufferOffset = 0;                                 // 缓冲区起始偏移

vkCmdCopyBufferToImage(
    cmd, 
    stagingBuffer,                        // 源缓冲区（暂存缓冲区）
    textureImage,                         // 目标图像
    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, // 当前图像布局
    1, &bufferImageCopy                   // 复制区域描述
);
```

- ```
  stagingBuffer
  ```

   通常是 CPU 可写的缓冲区，用于临时存储纹理数据。

- 图像必须处于 `VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL` 布局才能接收数据。

### 2.6 第二次图像内存屏障（布局转换）

将图像布局从 `VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL` 转换为 `VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL`。此屏障确保图像在着色器读取前处于正确布局，并避免读写冲突。

```
imageMemoryBarrier.srcAccessMask = VK_ACCESS_TRANSFER_WRITE_BIT; // 传输写入完成
imageMemoryBarrier.dstAccessMask = VK_ACCESS_SHADER_READ_BIT;    // 后续需要着色器读取
imageMemoryBarrier.oldLayout = VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL;
imageMemoryBarrier.newLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

vkCmdPipelineBarrier(
    cmd, 
    VK_PIPELINE_STAGE_TRANSFER_BIT,          // 源阶段：传输完成
    VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,   // 目标阶段：片段着色器读取
    0, 0, nullptr, 0, nullptr, 
    1, &imageMemoryBarrier
);
```

- 源阶段：`VK_PIPELINE_STAGE_TRANSFER_BIT` 表示传输操作已完成。
- 目标阶段：`VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT` 表示后续片段着色器可能读取此图像。

### 2.7 提交命令并等待完成

提交命令缓冲区到图形队列执行，并等待操作完成。

```
vkEndCommandBuffer(cmd); // 结束记录
// 提交到队列执行
VkSubmitInfo submitInfo{};
submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
submitInfo.commandBufferCount = 1;
submitInfo.pCommandBuffers = &cmd;

VK_CHECK(vkQueueSubmit(graphicsQueue, 1, &submitInfo, VK_NULL_HANDLE));
vkQueueWaitIdle(graphicsQueue); // 等待操作完成
```

`vkQueueWaitIdle` 确保数据复制和布局转换完成后再继续后续操作。

### 2.8 关键总结

1. 同步与布局管理

Vulkan 要求显式管理图像布局和内存依赖。两次管线屏障确保：

- 传输前图像布局正确。
- 传输后图像布局适合着色器读取。

1. 数据传输流程

典型流程为：

![图片](./assets/640-1750606327378-85.webp)

## 三、创建纹理图像视图

```
void HelloVK::initVulkan() {
createInstance();
createSurface();
pickPhysicalDevice();
createLogicalDeviceAndQueue();
setupDebugMessenger();
establishDisplaySizeIdentity();
createSwapChain();
createImageViews();
createRenderPass();
createDescriptorSetLayout();
createGraphicsPipeline();
createFramebuffers();
createCommandPool();
createCommandBuffer();
decodeImage();
createTextureImage();
copyBufferToImage();
createTextureImageViews();
  ...
}

void HelloVK::createTextureImageViews() {
  VkImageViewCreateInfo createInfo{};
  createInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
  createInfo.image = textureImage;
  createInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;
  createInfo.format = VK_FORMAT_R8G8B8A8_UNORM;
  createInfo.components.r = VK_COMPONENT_SWIZZLE_IDENTITY;
  createInfo.components.g = VK_COMPONENT_SWIZZLE_IDENTITY;
  createInfo.components.b = VK_COMPONENT_SWIZZLE_IDENTITY;
  createInfo.components.a = VK_COMPONENT_SWIZZLE_IDENTITY;
  createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
  createInfo.subresourceRange.baseMipLevel = 0;
  createInfo.subresourceRange.levelCount = 1;
  createInfo.subresourceRange.baseArrayLayer = 0;
  createInfo.subresourceRange.layerCount = 1;

VK_CHECK(vkCreateImageView(device, &createInfo, nullptr, &textureImageView));
}
```

### 3.1 图像视图创建信息结构体 (`VkImageViewCreateInfo`)

定义如何从原始图像 (`textureImage`) 创建视图，以便在管线（如着色器采样）中使用。

```
VkImageViewCreateInfo createInfo{};
createInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;  // 结构体类型标识
createInfo.image = textureImage;                              // 关联的原始图像对象
createInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;                 // 2D 视图类型
createInfo.format = VK_FORMAT_R8G8B8A8_UNORM;                // 图像格式（需与原始图像一致）
```

- ```
  viewType
  ```

  ：指定视图维度，需与图像类型匹配（例如 2D 图像对应 `VK_IMAGE_VIEW_TYPE_2D`）。

- ```
  format
  ```

  ：必须与原始图像创建时的格式一致（此处为 `VK_FORMAT_R8G8B8A8_UNORM`）。

### 3.2 通道重映射配置 (`components`)

控制图像通道的映射方式，允许调整颜色通道顺序或替换值。

```
createInfo.components.r = VK_COMPONENT_SWIZZLE_IDENTITY;  // 红色通道保持原样
createInfo.components.g = VK_COMPONENT_SWIZZLE_IDENTITY;  // 绿色通道保持原样
createInfo.components.b = VK_COMPONENT_SWIZZLE_IDENTITY;  // 蓝色通道保持原样
createInfo.components.a = VK_COMPONENT_SWIZZLE_IDENTITY;  // Alpha通道保持原样
```

1. 若需交换红蓝通道：`r = VK_COMPONENT_SWIZZLE_B`, `b = VK_COMPONENT_SWIZZLE_R`。
2. 若忽略 Alpha 通道：`a = VK_COMPONENT_SWIZZLE_ONE`（强制设为 1.0）。

### 3.3 子资源范围定义 (`subresourceRange`)

指定视图可访问的图像子资源范围。

```
createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;  // 操作颜色通道
createInfo.subresourceRange.baseMipLevel = 0;                        // 起始 Mip 层级
createInfo.subresourceRange.levelCount = 1;                          // 使用 1 个 Mip 层级
createInfo.subresourceRange.baseArrayLayer = 0;                      // 起始数组层
createInfo.subresourceRange.layerCount = 1;                          // 使用 1 个数组层
```

- ```
  aspectMask
  ```

  ：必须与图像用途匹配（如深度图像应使用 `VK_IMAGE_ASPECT_DEPTH_BIT`）。

- Mip 层级：若图像包含多级渐远纹理（Mipmaps），可调整 `baseMipLevel` 和 `levelCount`。

- 数组层：对纹理数组或立方体贴图，可指定访问的层数。

### 2.4. 创建图像视图 (`vkCreateImageView`)

基于配置信息，生成一个可被着色器访问的纹理视图。

```
VK_CHECK(vkCreateImageView(device, &createInfo, nullptr, &textureImageView));
```