# 【Vulkan 入门系列】创建帧缓冲、命令池、命令缓存，和获取图片（六）

这一节主要介绍创建帧缓冲（Framebuffer），创建命令池，创建命令缓存，和从文件加载 PNG 图像数据，解码为 RGBA 格式，并将像素数据暂存到 Vulkan 的 暂存缓冲区中。

## 一、创建帧缓冲

`createFramebuffers` 用于创建帧缓冲（Framebuffer）的核心部分，其功能是为交换链（Swap Chain）中的每个图像视图（Image View）创建对应的帧缓冲对象。

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
  ...
}

void HelloVK::createFramebuffers() {
  swapChainFramebuffers.resize(swapChainImageViews.size());
for (size_t i = 0; i < swapChainImageViews.size(); i++) {
    VkImageView attachments[] = {swapChainImageViews[i]};

    VkFramebufferCreateInfo framebufferInfo{};
    framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
    framebufferInfo.renderPass = renderPass;
    framebufferInfo.attachmentCount = 1;
    framebufferInfo.pAttachments = attachments;
    framebufferInfo.width = swapChainExtent.width;
    framebufferInfo.height = swapChainExtent.height;
    framebufferInfo.layers = 1;

    VK_CHECK(vkCreateFramebuffer(device, &framebufferInfo, nullptr,
                                 &swapChainFramebuffers[i]));
  }
}
```

### 1.1 调整帧缓冲数组大小

根据交换链图像视图的数量调整帧缓冲数组的大小，确保两者一一对应。

```
swapChainFramebuffers.resize(swapChainImageViews.size());
```

### 1.2 遍历交换链图像视图

对每个交换链图像视图创建对应的帧缓冲。

```
for (size_t i = 0; i < swapChainImageViews.size(); i++) {...}
```

### 1.3 定义附件

此处仅使用颜色附件（`swapChainImageViews[i]`），即渲染结果将写入交换链图像。若需要深度、模板测试，需额外添加对应的图像视图。

```
VkImageView attachments[] = {swapChainImageViews[i]};
```

### 1.4 配置帧缓冲创建信息

```
VkFramebufferCreateInfo framebufferInfo{};
framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
framebufferInfo.renderPass = renderPass;           // 关联的渲染流程
framebufferInfo.attachmentCount = 1;               // 附件数量
framebufferInfo.pAttachments = attachments;        // 附件数组指针
framebufferInfo.width = swapChainExtent.width;     // 帧缓冲宽度
framebufferInfo.height = swapChainExtent.height;    // 帧缓冲高度
framebufferInfo.layers = 1;                        // 层数（用于多视口/立体渲染）
```

**关键参数**

- ```
  renderPass
  ```

  ：帧缓冲必须与渲染流程兼容（即附件格式、数量与渲染流程定义一致）。

- ```
  width
  ```

   和 `height`：必须与交换链图像尺寸一致，否则渲染结果可能无效。

- ```
  layers
  ```

  ：通常为 1，用于多图层渲染（如 VR 立体视图）。

### 1.5 创建帧缓冲

`vkCreateFramebuffer` 创建实际的 Vulkan 帧缓冲对象。

```
VK_CHECK(vkCreateFramebuffer(device, &framebufferInfo, nullptr, &swapChainFramebuffers[i]));
```

## 二、创建命令池

创建一个命令池，用于分配和管理命令缓冲的内存。命令缓冲用于记录 GPU 执行的渲染或计算指令。

命令池与特定的队列族（Queue Family）绑定，确保命令缓冲被提交到正确的硬件队列（如图形队列）。

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
  ...
}

void HelloVK::createCommandPool() {
  QueueFamilyIndices queueFamilyIndices = findQueueFamilies(physicalDevice);
  VkCommandPoolCreateInfo poolInfo{};
  poolInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
  poolInfo.flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
  poolInfo.queueFamilyIndex = queueFamilyIndices.graphicsFamily.value();
VK_CHECK(vkCreateCommandPool(device, &poolInfo, nullptr, &commandPool));
}
```

### 2.1 获取队列族索引 `QueueFamilyIndices`

`findQueueFamilies` 函数在前面已经详细分析过，用于寻找物理设备支持的图形队列族和呈现队列族。

### 2.2 配置命令池创建信息

```
VkCommandPoolCreateInfo poolInfo{};
poolInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
poolInfo.flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
poolInfo.queueFamilyIndex = queueFamilyIndices.graphicsFamily.value();
```

- ```
  sType
  ```

  ：指定结构体类型为命令池创建信息。

- ```
  flags
  ```

  ：控制命令池的行为，此处设置为 `VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT`，允许单个命令缓冲通过 `vkResetCommandBuffer` 重置，而无需重置整个命令池。

- ```
  queueFamilyIndex
  ```

  ：指定命令池关联的队列族索引（此处为图形队列族），确保命令缓冲提交到正确的队列。

### 2.3 创建命令池

`vkCreateCommandPool` 调用 Vulkan API 创建命令池。

```
VK_CHECK(vkCreateCommandPool(device, &poolInfo, nullptr, &commandPool));
```

## 三、创建命令缓存

从已创建的命令池（`commandPool`）中分配一组主命令缓冲（Primary Command Buffers），用于记录 GPU 执行的渲染指令。

使用 `MAX_FRAMES_IN_FLIGHT` 控制帧的并发数量（如双缓冲或三缓冲），避免 CPU 和 GPU 之间的资源竞争。

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
createCommandBuffer()；
  ...
}

void HelloVK::createCommandBuffer() {
  commandBuffers.resize(MAX_FRAMES_IN_FLIGHT);
  VkCommandBufferAllocateInfo allocInfo{};
  allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
  allocInfo.commandPool = commandPool;
  allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
  allocInfo.commandBufferCount = commandBuffers.size();

VK_CHECK(vkAllocateCommandBuffers(device, &allocInfo, commandBuffers.data()));
}
```

### 3.1 调整命令缓冲数组大小

根据预定义的 `MAX_FRAMES_IN_FLIGHT`（代码内设置为 2）设置命令缓冲数组的大小。每个飞行的帧需要一个独立的命令缓冲，确保 CPU 在录制下一帧时不会覆盖正在被 GPU 处理的帧数据。

```
commandBuffers.resize(MAX_FRAMES_IN_FLIGHT);
```

### 3.2 配置命令缓冲分配信息

```
VkCommandBufferAllocateInfo allocInfo{};
allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
allocInfo.commandPool = commandPool;                // 关联的命令池
allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;  // 主命令缓冲级别
allocInfo.commandBufferCount = commandBuffers.size(); // 分配的缓冲数量
```

**关键参数**

- ```
  commandPool
  ```

  ：指定从哪个命令池分配内存。命令池的类型需与后续提交的队列兼容。

- ```
  level
  ```

  ：设置为 `VK_COMMAND_BUFFER_LEVEL_PRIMARY`，表示分配的是主命令缓冲（可直接提交到队列）。

| 级别                              | 用途                                                         |
| --------------------------------- | ------------------------------------------------------------ |
| VK_COMMAND_BUFFER_LEVEL_PRIMARY   | 直接提交到队列，可调用次级缓冲。适用于每帧的主要渲染指令。   |
| VK_COMMAND_BUFFER_LEVEL_SECONDARY | 嵌入到主缓冲中，需通过主缓冲执行。适用于复用指令或并行录制。 |

- ```
  commandBufferCount
  ```

  ：需要分配的缓冲数量，与 `MAX_FRAMES_IN_FLIGHT` 一致。

### 3.3 分配命令缓冲

`vkAllocateCommandBuffers` 从命令池中分配指定数量的命令缓冲。

```
VK_CHECK(vkAllocateCommandBuffers(device, &allocInfo, commandBuffers.data()));
```

### 3.4 核心概念

#### 3.4.1 主命令缓冲（Primary Command Buffer）

- 直接提交到队列：主缓冲可独立提交到队列执行，通常包含完整的渲染指令序列。
- 次级缓冲的依赖：次级缓冲（`SECONDARY`）需通过 `vkCmdExecuteCommands` 在主缓冲中调用，适用于复用指令或并行录制。

#### 3.4.2 帧并发控制（MAX_FRAMES_IN_FLIGHT）

- 双缓冲/三缓冲：通过设置 2 或 3 个缓冲，允许 CPU 准备下一帧数据的同时，GPU 处理当前帧，避免资源冲突。
- 同步机制：需配合信号量（Semaphore）或栅栏（Fence）确保帧的正确同步。

#### 3.4.3 命令池与缓冲的关系

- 内存管理：命令池负责底层内存分配，缓冲的生命周期由其所属池控制。
- 重置行为：若命令池创建时指定了 `VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT`，可单独重置缓冲，否则需重置整个池。

### 3.5 完整工作流程示例

1. 初始化阶段：创建命令池 → 分配命令缓冲。

2. 渲染循环：

3. - 等待前一帧完成（通过栅栏）。
   - 重置命令缓冲 → 录制渲染指令（如绑定管线、绘制调用）。
   - 提交命令缓冲到队列 → 呈现交换链图像。

4. 清理阶段：销毁命令池（自动释放所有关联的缓冲）。

## 四、获取图片

从文件加载 PNG 图像数据，解码为 RGBA 格式，并将像素数据暂存到 Vulkan 的 暂存缓冲区（Staging Buffer） 中，为后续将数据复制到 GPU 专用的纹理图像做准备。

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
decodeImage();
  ...
}

void HelloVK::decodeImage() {
  std::vector<uint8_t> imageData = LoadBinaryFileToVector("texture.png",
                                                          assetManager);
if (imageData.size() == 0) {
      LOGE("Fail to load image.");
      return;
  }

// Make sure we have an alpha channel, not all hardware can do linear filtering of RGB888.
constint requiredChannels = 4;
unsignedchar* decodedData = stbi_load_from_memory(imageData.data(),
      imageData.size(), &textureWidth, &textureHeight, &textureChannels, requiredChannels);
if (decodedData == nullptr) {
      LOGE("Fail to load image to memory, %s", stbi_failure_reason());
      return;
  }

if (textureChannels != requiredChannels) {
    textureChannels = requiredChannels;
  }

size_t imageSize = textureWidth * textureHeight * textureChannels;

  VkBufferCreateInfo createInfo{};
  createInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
  createInfo.size = imageSize;
  createInfo.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
  createInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;
VK_CHECK(vkCreateBuffer(device, &createInfo, nullptr, &stagingBuffer));

  VkMemoryRequirements memRequirements;
vkGetBufferMemoryRequirements(device, stagingBuffer, &memRequirements);

  VkMemoryAllocateInfo allocInfo{};
  allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
  allocInfo.allocationSize = memRequirements.size;
  allocInfo.memoryTypeIndex = findMemoryType(memRequirements.memoryTypeBits,
      VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

VK_CHECK(vkAllocateMemory(device, &allocInfo, nullptr, &stagingMemory));
VK_CHECK(vkBindBufferMemory(device, stagingBuffer, stagingMemory, 0));

uint8_t *data;
VK_CHECK(vkMapMemory(device, stagingMemory, 0, memRequirements.size, 0,
                       (void **)&data));
memcpy(data, decodedData, imageSize);
vkUnmapMemory(device, stagingMemory);

stbi_image_free(decodedData);
}
```

### 4.1 加载图像文件到内存

调用 `LoadBinaryFileToVector` 将文件内容读取到字节数组 `imageData`。若文件加载失败（如路径错误或文件不存在），记录错误并退出。

```
std::vector<uint8_t> imageData = LoadBinaryFileToVector("texture.png", assetManager);
if (imageData.size() == 0) {
    LOGE("Fail to load image.");
    return;
}
```

### 4.2 解码图像数据

使用 STB 图像库中的函数 `stbi_load_from_memory` 从内存解码图像。

```
const int requiredChannels = 4;
unsignedchar* decodedData = stbi_load_from_memory(
    imageData.data(), imageData.size(), 
    &textureWidth, &textureHeight, &textureChannels, 
    requiredChannels
);
if (decodedData == nullptr) {
    LOGE("Fail to load image to memory, %s", stbi_failure_reason());
    return;
}

if (textureChannels != requiredChannels) {
    textureChannels = requiredChannels; // 强制设为 4
}
```

- ```
  requiredChannels = 4
  ```

  ：强制解码为 RGBA 格式（4 通道），确保兼容性（某些 GPU 对 RGB 格式的线性过滤支持不佳）。

- 输出参数：`textureWidth`、`textureHeight`（图像尺寸）、`textureChannels`（实际解码的通道数）。

### 4.3 计算图像数据大小

```
size_t imageSize = textureWidth * textureHeight * textureChannels; // 总字节数
```

### 4.4 创建暂存缓冲区

暂存缓冲区作为 CPU 与 GPU 之间的数据传输桥梁。后续需通过传输命令将数据从此缓冲区复制到 GPU 专用的纹理图像。

```
VkBufferCreateInfo createInfo{};
createInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
createInfo.size = imageSize;                           // 缓冲区大小
createInfo.usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT;   // 用途：传输源
createInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;    // 独占访问模式
VK_CHECK(vkCreateBuffer(device, &createInfo, nullptr, &stagingBuffer));
```

**关键参数**

- ```
  usage = VK_BUFFER_USAGE_TRANSFER_SRC_BIT
  ```

  ：标记为传输源。

- ```
  sharingMode = VK_SHARING_MODE_EXCLUSIVE
  ```

  ：缓冲区仅由图形队列独占使用（无需多队列共享）。

### 4.5 查询内存需求

调用 `vkGetBufferMemoryRequirements` 获取缓冲区的内存需求（大小、对齐、内存类型掩码）。

```
VkMemoryRequirements memRequirements;
vkGetBufferMemoryRequirements(device, stagingBuffer, &memRequirements);
```

### 4.6 分配暂存内存

调用 `vkAllocateMemory` 用于分配暂存内存。

```
VkMemoryAllocateInfo allocInfo{};
allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
allocInfo.allocationSize = memRequirements.size;
allocInfo.memoryTypeIndex = findMemoryType(
    memRequirements.memoryTypeBits,
    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
);
VK_CHECK(vkAllocateMemory(device, &allocInfo, nullptr, &stagingMemory));
```

- ```
  VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
  ```

  ：内存可被 CPU 直接访问（通过 `vkMapMemory`）。

- ```
  VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
  ```

  ：确保 CPU 与 GPU 内存访问的自动一致性（无需手动刷新缓存）。

`findMemoryType` 用于找到符合特定缓冲区内存要求的内存堆的索引。Vulkan 将这些要求以位集的形式管理，在这种情况下通过 `uint32_t` 来表示。

```
uint32_t HelloVK::findMemoryType(uint32_t typeFilter,
                                 VkMemoryPropertyFlags properties) {
  VkPhysicalDeviceMemoryProperties memProperties;
vkGetPhysicalDeviceMemoryProperties(physicalDevice, &memProperties);

for (uint32_t i = 0; i < memProperties.memoryTypeCount; i++) {
    if ((typeFilter & (1 << i)) && (memProperties.memoryTypes[i].propertyFlags &
                                    properties) == properties) {
      return i;
    }
  }

assert(false);  // failed to find suitable memory type!
return-1;
}
```

1. 调用 `vkGetPhysicalDeviceMemoryProperties` 获取物理设备的内存信息，包括内存类型（`memoryTypes`）和内存堆（`memoryHeaps`）。

2. 遍历所有可用的内存类型（通常数量较小）。

3. ```
   typeFilter & (1 << i)
   ```

    检查第 `i` 位是否为 1。若为真，表示内存类型 `i` 是候选类型。

4. ```
   (memProperties.memoryTypes[i].propertyFlags & properties) == properties
   ```

    确保内存类型的属性（`propertyFlags`）包含 `properties` 的所有标志。例如，若 `properties` 要求内存同时是主机可见和一致的，则内存类型必须同时具备这两个属性。

5. 返回第一个满足条件的内存类型索引。

6. 若未找到合适内存类型，触发断言错误（调试模式下终止程序），并返回无效值 -1。

### 4.7 绑定内存到缓冲区

将分配的内存与缓冲区关联，偏移量设为 0（从内存起始位置绑定）。

```
VK_CHECK(vkBindBufferMemory(device, stagingBuffer, stagingMemory, 0));
```

### 4.8 映射内存并拷贝数据

```
uint8_t *data;
VK_CHECK(vkMapMemory(device, stagingMemory, 0, memRequirements.size, 0, (void **)&data));
memcpy(data, decodedData, imageSize);
vkUnmapMemory(device, stagingMemory);
```

1. ```
   vkMapMemory
   ```

    将 GPU 内存映射到 CPU 可访问的指针 `data`。

2. ```
   memcpy
   ```

    将解码后的像素数据复制到映射的内存中。

3. ```
   vkUnmapMemory
   ```

    解除映射，确保数据写入完成。

### 4.9 释放解码数据

STB 库要求手动释放解码后的像素数据，避免内存泄漏。

```
stbi_image_free(decodedData);
```

### 4.10 关键概念

#### 4.10.1 暂存缓冲区（Staging Buffer）

GPU 专用内存通常无法直接被 CPU 访问，需通过暂存缓冲区中转。

**典型流程**

1. CPU 将数据写入暂存缓冲区。
2. 提交传输命令（如 `vkCmdCopyBufferToImage`），将数据复制到设备本地纹理。
3. 销毁暂存资源。

#### 4.10.2 内存一致性

`HOST_COHERENT_BIT`：确保 CPU 写入的数据立即可被 GPU 读取（无缓存同步问题）。若无此标志需手动调用 `vkFlushMappedMemoryRanges` 和 `vkInvalidateMappedMemoryRanges` 刷新缓存。