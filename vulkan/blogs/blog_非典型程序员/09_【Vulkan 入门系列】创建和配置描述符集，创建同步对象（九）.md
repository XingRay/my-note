# 【Vulkan 入门系列】创建和配置描述符集，创建同步对象（九）

这一节主要介绍创建和配置描述符集，创建 Vulkan 中的同步对象（信号量和栅栏）。

## 一、创建和配置描述符集

`createDescriptorSets` 用于创建和配置 Vulkan 的描述符集（Descriptor Sets），描述符集是用于将资源（如缓冲区和图像）绑定到着色器的重要机制。

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
createTextureSampler();
createUniformBuffers();
createDescriptorPool();
createDescriptorSets();
  ...
}

void HelloVK::createDescriptorSets() {
std::vector<VkDescriptorSetLayout> layouts(MAX_FRAMES_IN_FLIGHT,
                                             descriptorSetLayout);
  VkDescriptorSetAllocateInfo allocInfo{};
  allocInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
  allocInfo.descriptorPool = descriptorPool;
  allocInfo.descriptorSetCount = static_cast<uint32_t>(MAX_FRAMES_IN_FLIGHT);
  allocInfo.pSetLayouts = layouts.data();

  descriptorSets.resize(MAX_FRAMES_IN_FLIGHT);
VK_CHECK(vkAllocateDescriptorSets(device, &allocInfo, descriptorSets.data()));

for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
    VkDescriptorBufferInfo bufferInfo{};
    bufferInfo.buffer = uniformBuffers[i];
    bufferInfo.offset = 0;
    bufferInfo.range = sizeof(UniformBufferObject);

    VkDescriptorImageInfo imageInfo{};
    imageInfo.imageView = textureImageView;
    imageInfo.sampler = textureSampler;
    imageInfo.imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;

    std::array<VkWriteDescriptorSet, 2> descriptorWrites{};

    // Uniform buffer
    descriptorWrites[0].sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
    descriptorWrites[0].dstSet = descriptorSets[i];
    descriptorWrites[0].dstBinding = 0;
    descriptorWrites[0].dstArrayElement = 0;
    descriptorWrites[0].descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
    descriptorWrites[0].descriptorCount = 1;
    descriptorWrites[0].pBufferInfo = &bufferInfo;

    // Combined image sampler
    descriptorWrites[1].sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
    descriptorWrites[1].dstSet = descriptorSets[i];
    descriptorWrites[1].dstBinding = 1;
    descriptorWrites[1].dstArrayElement = 0;
    descriptorWrites[1].descriptorType =
        VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    descriptorWrites[1].descriptorCount = 1;
    descriptorWrites[1].pImageInfo = &imageInfo;

    vkUpdateDescriptorSets(device,
                           static_cast<uint32_t>(descriptorWrites.size()),
                           descriptorWrites.data(), 0, nullptr);
  }
}
```

### 1.1 描述符集分配

```
std::vector<VkDescriptorSetLayout> layouts(MAX_FRAMES_IN_FLIGHT, descriptorSetLayout);
VkDescriptorSetAllocateInfo allocInfo{};
allocInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
allocInfo.descriptorPool = descriptorPool;
allocInfo.descriptorSetCount = static_cast<uint32_t>(MAX_FRAMES_IN_FLIGHT);
allocInfo.pSetLayouts = layouts.data();

descriptorSets.resize(MAX_FRAMES_IN_FLIGHT);
VK_CHECK(vkAllocateDescriptorSets(device, &allocInfo, descriptorSets.data()));
```

1. 创建一个布局数组，每个帧使用相同的描述符集布局（`descriptorSetLayout`）。
2. 设置分配信息（`VkDescriptorSetAllocateInfo`）：
   1）指定要从哪个描述符池（`descriptorPool`）分配。
   2）要分配的描述符集数量（`MAX_FRAMES_IN_FLIGHT`，为 2）。
   3）每个描述符集的布局。
3. 调整 `descriptorSets` 大小以容纳所有描述符集。
4. 调用 `vkAllocateDescriptorSets` 实际分配描述符集。

### 1.2 配置每个描述符集

对于每个帧的描述符集：

```
for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
```

#### 1.2.1 设置 Uniform 缓冲区信息

创建缓冲区信息结构体并配置。

```
VkDescriptorBufferInfo bufferInfo{};
bufferInfo.buffer = uniformBuffers[i];
bufferInfo.offset = 0;
bufferInfo.range = sizeof(UniformBufferObject);
```

配置缓冲区信息结构体：

- 要绑定的缓冲区（`uniformBuffers[i]`）
- 偏移量（0）
- 范围（整个 UniformBufferObject 的大小）

#### 1.2.2 设置图像采样器信息

创建图像信息结构体并配置。

```
VkDescriptorImageInfo imageInfo{};
imageInfo.imageView = textureImageView;
imageInfo.sampler = textureSampler;
imageInfo.imageLayout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
```

配置图像信息结构体：

- 图像视图（`textureImageView`）
- 采样器（`textureSampler`）
- 图像布局（只读最优布局：`VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL`）

#### 1.2.3 创建描述符写入操作

创建两个描述符写入操作（一个用于 Uniform 缓冲区，一个用于图像采样器）。

```
std::array<VkWriteDescriptorSet, 2> descriptorWrites{};
```

#### 1.2.4 Uniform 缓冲区写入

```
descriptorWrites[0].sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
descriptorWrites[0].dstSet = descriptorSets[i];
descriptorWrites[0].dstBinding = 0;
descriptorWrites[0].dstArrayElement = 0;
descriptorWrites[0].descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
descriptorWrites[0].descriptorCount = 1;
descriptorWrites[0].pBufferInfo = &bufferInfo;
```

- ```
  dstSet
  ```

  ：目标描述符集（`descriptorSets[i]`）。

- ```
  dstBinding
  ```

  ：绑定点（0，对应着色器中的 `layout(binding = 0) uniform...` ）。

- ```
  dstArrayElement
  ```

  ：当绑定的描述符是数组时，指定从数组的哪个元素开始更新。对于非数组描述符设为 0。

- ```
  descriptorType
  ```

  ：描述符类型为 Uniform 缓冲区（`VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER`）。

- 提供之前创建的缓冲区信息。

#### 1.2.5 图像采样器写入

```
descriptorWrites[1].sType = VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
descriptorWrites[1].dstSet = descriptorSets[i];
descriptorWrites[1].dstBinding = 1;
descriptorWrites[1].dstArrayElement = 0;
descriptorWrites[1].descriptorType = VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
descriptorWrites[1].descriptorCount = 1;
descriptorWrites[1].pImageInfo = &imageInfo;
```

- 绑定点（1，对应着色器中的 `layout(binding = 1) uniform sampler2D...`）
- 描述符类型为组合图像采样器（`VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER`）

#### 1.2.6 更新描述符集

传入要执行的写入操作数组，执行实际的描述符集更新操作。

```
vkUpdateDescriptorSets(device,
                      static_cast<uint32_t>(descriptorWrites.size()),
                      descriptorWrites.data(), 0, nullptr);
```

## 二、创建同步对象

`createSyncObjects` 用于创建 Vulkan 的同步对象，包括信号量（Semaphores）和栅栏（Fences），这些对象对于协调 GPU 和 CPU 之间的工作至关重要，特别是在多帧渲染的情况下。

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
createTextureSampler();
createUniformBuffers();
createDescriptorPool();
createDescriptorSets();
createSyncObjects();
  initialized = true;
}

void HelloVK::createSyncObjects() {
  imageAvailableSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
  renderFinishedSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
  inFlightFences.resize(MAX_FRAMES_IN_FLIGHT);

  VkSemaphoreCreateInfo semaphoreInfo{};
  semaphoreInfo.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;

  VkFenceCreateInfo fenceInfo{};
  fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
  fenceInfo.flags = VK_FENCE_CREATE_SIGNALED_BIT;
for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
    VK_CHECK(vkCreateSemaphore(device, &semaphoreInfo, nullptr,
                               &imageAvailableSemaphores[i]));

    VK_CHECK(vkCreateSemaphore(device, &semaphoreInfo, nullptr,
                               &renderFinishedSemaphores[i]));

    VK_CHECK(vkCreateFence(device, &fenceInfo, nullptr, &inFlightFences[i]));
  }
}
```

### 2.1 同步对象容器初始化

调整三个容器的大小以容纳 `MAX_FRAMES_IN_FLIGHT` 个同步对象。

```
imageAvailableSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
renderFinishedSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
inFlightFences.resize(MAX_FRAMES_IN_FLIGHT);
```

- ```
  imageAvailableSemaphores
  ```

  ：表示交换链图像已准备好渲染的信号量。

- ```
  renderFinishedSemaphores
  ```

  ：表示渲染已完成且图像可以呈现的信号量。

- ```
  inFlightFences
  ```

  ：用于确保同一帧不会被多次提交的栅栏。

### 2.2 信号量创建信息

```
VkSemaphoreCreateInfo semaphoreInfo{};
semaphoreInfo.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
```

1. 创建信号量信息结构体。
2. 设置 `sType` 为 `VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO`。
3. 其他字段使用默认值（无特殊标志）。

### 2.3 栅栏创建信息

```
VkFenceCreateInfo fenceInfo{};
fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
fenceInfo.flags = VK_FENCE_CREATE_SIGNALED_BIT;
```

1. 创建栅栏信息结构体。
2. 设置 `sType` 为 `VK_STRUCTURE_TYPE_FENCE_CREATE_INFO`。
3. 设置 `flags` 为 `VK_FENCE_CREATE_SIGNALED_BIT`，表示栅栏初始状态为“已触发”。
4. 这样第一帧可以立即提交而不会等待。

### 2.4 创建同步对象

```
for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
  VK_CHECK(vkCreateSemaphore(device, &semaphoreInfo, nullptr,
                             &imageAvailableSemaphores[i]));

  VK_CHECK(vkCreateSemaphore(device, &semaphoreInfo, nullptr,
                             &renderFinishedSemaphores[i]));

  VK_CHECK(vkCreateFence(device, &fenceInfo, nullptr, &inFlightFences[i]));
}
```

对于每一帧（0 到 `MAX_FRAMES_IN_FLIGHT-1`）：

1. 创建 `imageAvailableSemaphores[i]` 信号量。用于等待交换链图像可用，在 `vkAcquireNextImageKHR` 后发出信号。
2. 创建 `renderFinishedSemaphores[i]` 信号量。用于等待渲染完成，在命令缓冲区执行完成后发出信号，用于 `vkQueuePresentKHR` 的等待。
3. 创建 `inFlightFences[i]` 栅栏。用于确保 CPU 不会在 GPU 完成前一帧之前提交新帧。