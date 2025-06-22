# 【Vulkan 入门系列】创建交换链、图像视图和渲染通道（四）

Vulkan 没有“默认帧缓冲”的概念，因此它需要一个基础设施来拥有我们将要渲染的缓冲，然后我们才能在屏幕上可视化它们。这个基础设施被称为交换链，必须在 Vulkan 中显式创建。交换链本质上是一个等待呈现到屏幕的图像队列。我们的应用程序将获取这样的图像来绘制它，然后将其返回到队列。队列的确切工作方式以及从队列中呈现图像的条件取决于交换链的设置方式，但交换链的总体目的是将图像的呈现与屏幕的刷新率同步。

要在渲染管线中使用任何 `VkImage`，包括交换链中的那些，我们必须创建一个 `VkImageView` 对象。图像视图字面上就是图像的视图，它描述了如何访问图像以及访问图像的哪一部分。

我们需要告诉 Vulkan 将在渲染时使用的帧缓冲附件。我们需要指定将有多少颜色和深度缓冲区，每个缓冲区使用多少个采样，以及它们的内容在整个渲染操作中应该如何处理。所有这些信息都封装在一个渲染过程对象中，我们将为此创建一个新的 `createRenderPass` 函数来创建渲染通道。

## 一、创建交换链

下面是使用 Vulkan API 创建交换链（Swap Chain）的过程。

```
void HelloVK::initVulkan() {
createInstance();
createSurface();
pickPhysicalDevice();
createLogicalDeviceAndQueue();
setupDebugMessenger();
establishDisplaySizeIdentity();
createSwapChain();
  ...
}

void HelloVK::createSwapChain() {
  SwapChainSupportDetails swapChainSupport =
      querySwapChainSupport(physicalDevice);

auto chooseSwapSurfaceFormat =
      [](const std::vector<VkSurfaceFormatKHR> &availableFormats) {
        for (constauto &availableFormat : availableFormats) {
          if (availableFormat.format == VK_FORMAT_B8G8R8A8_SRGB &&
              availableFormat.colorSpace == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
            return availableFormat;
          }
        }
        return availableFormats[0];
      };

  VkSurfaceFormatKHR surfaceFormat =
      chooseSwapSurfaceFormat(swapChainSupport.formats);

// Please check
// https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkPresentModeKHR.html
// for a discourse on different present modes.
//
// VK_PRESENT_MODE_FIFO_KHR = Hard Vsync
// This is always supported on Android phones
  VkPresentModeKHR presentMode = VK_PRESENT_MODE_FIFO_KHR;

uint32_t imageCount = swapChainSupport.capabilities.minImageCount + 1;
if (swapChainSupport.capabilities.maxImageCount > 0 &&
      imageCount > swapChainSupport.capabilities.maxImageCount) {
    imageCount = swapChainSupport.capabilities.maxImageCount;
  }
  pretransformFlag = swapChainSupport.capabilities.currentTransform;

  VkSwapchainCreateInfoKHR createInfo{};
  createInfo.sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
  createInfo.surface = surface;
  createInfo.minImageCount = imageCount;
  createInfo.imageFormat = surfaceFormat.format;
  createInfo.imageColorSpace = surfaceFormat.colorSpace;
  createInfo.imageExtent = displaySizeIdentity;
  createInfo.imageArrayLayers = 1;
  createInfo.imageUsage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
  createInfo.preTransform = pretransformFlag;

  QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
uint32_t queueFamilyIndices[] = {indices.graphicsFamily.value(),
                                   indices.presentFamily.value()};

if (indices.graphicsFamily != indices.presentFamily) {
    createInfo.imageSharingMode = VK_SHARING_MODE_CONCURRENT;
    createInfo.queueFamilyIndexCount = 2;
    createInfo.pQueueFamilyIndices = queueFamilyIndices;
  } else {
    createInfo.imageSharingMode = VK_SHARING_MODE_EXCLUSIVE;
    createInfo.queueFamilyIndexCount = 0;
    createInfo.pQueueFamilyIndices = nullptr;
  }
  createInfo.compositeAlpha = VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR;
  createInfo.presentMode = presentMode;
  createInfo.clipped = VK_TRUE;
  createInfo.oldSwapchain = VK_NULL_HANDLE;

VK_CHECK(vkCreateSwapchainKHR(device, &createInfo, nullptr, &swapChain));

vkGetSwapchainImagesKHR(device, swapChain, &imageCount, nullptr);
  swapChainImages.resize(imageCount);
vkGetSwapchainImagesKHR(device, swapChain, &imageCount,
                          swapChainImages.data());

  swapChainImageFormat = surfaceFormat.format;
  swapChainExtent = displaySizeIdentity;
}
```

### 1.1 查询交换链支持信息

通过物理设备（`physicalDevice`）查询当前 Surface 支持的交换链属性。在前面已经介绍过这个方法 `querySwapChainSupport`。

```
SwapChainSupportDetails swapChainSupport = querySwapChainSupport(physicalDevice);
```

返回的 `SwapChainSupportDetails` 结构包含以下字段：

- Surface 格式（`formats`）：像素格式和颜色空间。
- 呈现模式（`presentModes`）：图像如何从应用程序提交到屏幕。
- 能力（`capabilities`）：最小/最大图像数量、图像尺寸限制等。

### 1.2 选择 Surface 格式

优先选择 `B8G8R8A8_SRGB` 格式和 `SRGB_NONLINEAR` 颜色空间，以获得更准确的色彩表现。如果首选不可用，返回第一个可用格式。

```
auto chooseSwapSurfaceFormat = [](const std::vector<VkSurfaceFormatKHR>& availableFormats) {
    for (const auto& format : availableFormats) {
        if (format.format == VK_FORMAT_B8G8R8A8_SRGB && 
            format.colorSpace == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
            return format;
        }
    }
    return availableFormats[0];
};
VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
```

### 1.3 选择呈现模式

选择图像提交到屏幕的方式。

`VK_PRESENT_MODE_FIFO_KHR`：强制垂直同步（VSync），类似于双缓冲，确保无撕裂且兼容所有设备（如 Android）。

```
VkPresentModeKHR presentMode = VK_PRESENT_MODE_FIFO_KHR;
```

### 1.4 确定交换链图像数量

在支持的范围内选择比最小值多一个的图像数量（例如双缓冲或三缓冲）。如果设备有最大限制（`maxImageCount > 0`），确保不超过。

```
uint32_t imageCount = swapChainSupport.capabilities.minImageCount + 1;
if (swapChainSupport.capabilities.maxImageCount > 0 && 
    imageCount > swapChainSupport.capabilities.maxImageCount) {
    imageCount = swapChainSupport.capabilities.maxImageCount;
}
```

### 1.5 处理显示变换

记录当前 Surface 的变换（如旋转 90 度、水平翻转等），后续可用于调整渲染输出方向。

```
pretransformFlag = swapChainSupport.capabilities.currentTransform;
```

### 1.6 填充交换链创建信息

```
VkSwapchainCreateInfoKHR createInfo{};
createInfo.sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
createInfo.surface = surface;                       // 关联的窗口 Surface
createInfo.minImageCount = imageCount;              // 交换链图像数量
createInfo.imageFormat = surfaceFormat.format;      // 像素格式
createInfo.imageColorSpace = surfaceFormat.colorSpace; // 颜色空间
createInfo.imageExtent = displaySizeIdentity;       // 图像分辨率（需与 Surface 匹配）
createInfo.imageArrayLayers = 1;                    // 图像的层数（通常为 1，立体渲染可能需要 2）
createInfo.imageUsage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT; // 用作颜色附件
createInfo.preTransform = pretransformFlag;         // 应用显示变换
```

`imageArrayLayers`: 图像的层数，通常为 1。用于立体渲染（如 VR）时可能设置为 2。
`imageUsage`: 通过位掩码组合（`VkImageUsageFlags`）定义图像的使用方式：

**常用标志**

- ```
  VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT
  ```

  : 图像作为颜色附件（即渲染目标）。

- ```
  VK_IMAGE_USAGE_TRANSFER_DST_BIT
  ```

  : 图像作为传输目标（用于后处理或离屏渲染）。

### 1.7 处理多队列族

如果图形队列和呈现队列不同，需配置共享模式；否则使用独占模式。

```
QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
uint32_t queueFamilyIndices[] = {indices.graphicsFamily.value(), indices.presentFamily.value()};

if (indices.graphicsFamily != indices.presentFamily) {
    createInfo.imageSharingMode = VK_SHARING_MODE_CONCURRENT; // 图像在多个队列族间共享
    createInfo.queueFamilyIndexCount = 2;
    createInfo.pQueueFamilyIndices = queueFamilyIndices;
} else {
    createInfo.imageSharingMode = VK_SHARING_MODE_EXCLUSIVE;  // 单一队列族独占，性能更优
    createInfo.queueFamilyIndexCount = 0;
    createInfo.pQueueFamilyIndices = nullptr;
}
```

### 1.8 其他配置

```
createInfo.compositeAlpha = VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR; // Alpha 通道处理（可能需根据平台调整）
createInfo.presentMode = presentMode;         // 选择的呈现模式
createInfo.clipped = VK_TRUE;                 // 允许裁剪不可见区域
createInfo.oldSwapchain = VK_NULL_HANDLE;     // 首次创建，无旧交换链
```

`compositeAlpha`：定义 Alpha 通道如何与窗口系统合成：

**常用值**

`VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR`: 忽略 Alpha 通道（不透明）。
`VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR`: 继承窗口系统的 Alpha 行为。

### 1.9 创建交换链

调用 Vulkan 函数创建交换链。

```
VK_CHECK(vkCreateSwapchainKHR(device, &createInfo, nullptr, &swapChain));
```

### 1.10 获取交换链图像

获取交换链中的图像句柄，存储在 `swapChainImages` 中供后续使用。

```
vkGetSwapchainImagesKHR(device, swapChain, &imageCount, nullptr);
swapChainImages.resize(imageCount);
vkGetSwapchainImagesKHR(device, swapChain, &imageCount, swapChainImages.data());
```

### 1.11 保存格式和分辨率

记录交换链的像素格式和图像分辨率，用于后续渲染流程。

```
swapChainImageFormat = surfaceFormat.format;
swapChainExtent = displaySizeIdentity;
```

## 二、创建图像视图

`createImageViews` 用于为交换链（Swap Chain）中的每个图像创建对应的图像视图（Image View），以便在 Vulkan 渲染管线中访问这些图像。

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
  ...
}

void HelloVK::createImageViews() {
  swapChainImageViews.resize(swapChainImages.size());
for (size_t i = 0; i < swapChainImages.size(); i++) {
    VkImageViewCreateInfo createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
    createInfo.image = swapChainImages[i];
    createInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;
    createInfo.format = swapChainImageFormat;
    createInfo.components.r = VK_COMPONENT_SWIZZLE_IDENTITY;
    createInfo.components.g = VK_COMPONENT_SWIZZLE_IDENTITY;
    createInfo.components.b = VK_COMPONENT_SWIZZLE_IDENTITY;
    createInfo.components.a = VK_COMPONENT_SWIZZLE_IDENTITY;
    createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
    createInfo.subresourceRange.baseMipLevel = 0;
    createInfo.subresourceRange.levelCount = 1;
    createInfo.subresourceRange.baseArrayLayer = 0;
    createInfo.subresourceRange.layerCount = 1;
    VK_CHECK(vkCreateImageView(device, &createInfo, nullptr,
                               &swapChainImageViews[i]));
  }
}
```

为交换链中的每个图像（`swapChainImages`）创建一个图像视图（`VkImageView`），存储在 `swapChainImageViews` 数组中。

### 2.1 关键概念

#### 1. 图像视图（Image View）的作用

Vulkan 中，直接操作图像（`VkImage`）需要通过图像视图（`VkImageView`）。图像视图定义了如何解释图像数据（格式、通道映射和子资源范围等）。

#### 2. 通道映射（Swizzling）

允许重新排列颜色通道（如将 `BGR` 格式的图像映射为 `RGB`）。

#### 3. 子资源范围（Subresource Range）

用于指定图像的部分区域或特定属性：

- ```
  aspectMask
  ```

  ：可以是颜色（`COLOR`）、深度（`DEPTH`）、模板（`STENCIL`）。

- - mipmap 层级：支持多级 mipmap 时，可指定起始层级和数量。
  - 数组层：用于立体渲染或纹理数组。

### 2.2 代码解释

1. 调整图像视图数组大小
2. 遍历交换链中的每个图像，循环处理交换链中的每一个图像（例如双缓冲需要处理2个图像，三缓冲需要处理3个）。
3. 填充图像视图创建信息 `VkImageViewCreateInfo`。

- ```
  sType
  ```

  ：标识结构体类型为图像视图创建信息（`VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO`）。

- ```
  image
  ```

  ：指定要创建视图的原始图像（`VkImage` 对象），此处为交换链中的第 `i` 个图像。关联图像与视图。

- ```
  viewType
  ```

  ：定义视图的类型，此处为 2D 图像视图（`VK_IMAGE_VIEW_TYPE_2D`），适用于普通 2D 渲染目标。

- ```
  format
  ```

  ：图像的像素格式（如 `VK_FORMAT_B8G8R8A8_SRGB`），必须与交换链的格式（`swapChainImageFormat`）一致。

- ```
  components.r/g/b/a
  ```

  ：定义颜色通道的映射方式。`VK_COMPONENT_SWIZZLE_IDENTITY` 表示不修改通道顺序（例如，`r` 通道对应图像数据的红色分量）。

```
createInfo.components.r = VK_COMPONENT_SWIZZLE_IDENTITY;
createInfo.components.g = VK_COMPONENT_SWIZZLE_IDENTITY;
createInfo.components.b = VK_COMPONENT_SWIZZLE_IDENTITY;
createInfo.components.a = VK_COMPONENT_SWIZZLE_IDENTITY;
```

- ```
  subresourceRange
  ```

  ：定义子资源范围。`aspectMask` 指定图像的哪一部分被访问，此处为颜色数据（`VK_IMAGE_ASPECT_COLOR_BIT`）。`baseMipLevel` 和 `levelCount` 定义使用的 mipmap 层级范围。此处只使用第 0 级，且层级数为 1（无 mipmap）。`baseArrayLayer` 和 `layerCount` 定义使用的图像数组层。此处只使用第 0 层，且层数为 1（非立体渲染）。

```
createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
createInfo.subresourceRange.baseMipLevel = 0;
createInfo.subresourceRange.levelCount = 1;
createInfo.subresourceRange.baseArrayLayer = 0;
createInfo.subresourceRange.layerCount = 1;
```

4. 调用 `vkCreateImageView` 创建图像视图。`swapChainImageViews[i]` 存储新创建的图像视图句柄。

```
VK_CHECK(vkCreateImageView(device, &createInfo, nullptr, &swapChainImageViews[i]));
```

## 三、创建渲染通道

创建一个渲染流程（Render Pass），描述如何管理颜色附件（如交换链图像）的生命周期、布局转换以及子流程之间的同步关系。这是将渲染结果输出到屏幕的关键配置。

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
  ...
}

void HelloVK::createRenderPass() {
  VkAttachmentDescription colorAttachment{};
  colorAttachment.format = swapChainImageFormat;
  colorAttachment.samples = VK_SAMPLE_COUNT_1_BIT;

  colorAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
  colorAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;

  colorAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
  colorAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;

  colorAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
  colorAttachment.finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;

  VkAttachmentReference colorAttachmentRef{};
  colorAttachmentRef.attachment = 0;
  colorAttachmentRef.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;

  VkSubpassDescription subpass{};
  subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
  subpass.colorAttachmentCount = 1;
  subpass.pColorAttachments = &colorAttachmentRef;

  VkSubpassDependency dependency{};
  dependency.srcSubpass = VK_SUBPASS_EXTERNAL;
  dependency.dstSubpass = 0;
  dependency.srcStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
  dependency.srcAccessMask = 0;
  dependency.dstStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
  dependency.dstAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;

  VkRenderPassCreateInfo renderPassInfo{};
  renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
  renderPassInfo.attachmentCount = 1;
  renderPassInfo.pAttachments = &colorAttachment;
  renderPassInfo.subpassCount = 1;
  renderPassInfo.pSubpasses = &subpass;
  renderPassInfo.dependencyCount = 1;
  renderPassInfo.pDependencies = &dependency;

VK_CHECK(vkCreateRenderPass(device, &renderPassInfo, nullptr, &renderPass));
}
```

### 3.1 关键概念

#### 1. 附件（Attachment）

- 定义渲染过程中使用的图像资源（如颜色、深度附件）。
- 生命周期管理：通过 `loadOp` 和 `storeOp` 控制数据的加载与存储。
- 布局转换：Vulkan 要求显式管理图像布局（如 `UNDEFINED` → `COLOR_ATTACHMENT_OPTIMAL` → `PRESENT_SRC_KHR`）。

#### 2. 子流程（Subpass）

将渲染流程分解为多个阶段，每个阶段可以依赖前一个阶段的结果。

#### 3. 子流程依赖（Subpass Dependency）

同步机制：确保不同子流程（或外部操作）之间的执行顺序和资源访问安全。例如，在写入颜色附件前，必须等待交换链图像获取完成。

### 3.2 代码解释

#### 1. 定义颜色附件描述

描述颜色附件的属性。

```
VkAttachmentDescription colorAttachment{};
colorAttachment.format = swapChainImageFormat;       // 使用交换链的像素格式（如B8G8R8A8_SRGB）
colorAttachment.samples = VK_SAMPLE_COUNT_1_BIT;    // 禁用多重采样（1x采样）
```

`format`：与交换链图像格式一致，确保渲染结果可以直接呈现。
`samples`：采样数设置为 1，表示不使用多重采样抗锯齿（MSAA）。

#### 2. 颜色附件的加载与存储操作

```
colorAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;   // 渲染前清除附件内容
colorAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE; // 渲染后保存附件内容
```

`loadOp`:

`VK_ATTACHMENT_LOAD_OP_CLEAR`：在渲染开始前将附件内容清除为预设值（如黑色）。
`VK_ATTACHMENT_LOAD_OP_LOAD`：保留附件的现有内容。
`VK_ATTACHMENT_LOAD_OP_DONT_CARE`：现有内容未定义，不关心。

`storeOp`：

`VK_ATTACHMENT_STORE_OP_STORE`：渲染结束后保存结果，以便显示。
`VK_ATTACHMENT_STORE_OP_DONT_CARE`：渲染操作后帧缓冲区的内容将未定义。

#### 3. 模板附件的加载与存储操作

由于当前渲染流程未使用模板测试，因此忽略模板附件的操作。

```
colorAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;  // 不关心模板数据加载
colorAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE; // 不保存模板数据
```

#### 4. 图像布局转换

```
colorAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;         // 初始布局：未定义
colorAttachment.finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;    // 最终布局：呈现源
```

- ```
  initialLayout
  ```

  : 渲染流程开始前的图像布局。`VK_IMAGE_LAYOUT_UNDEFINED` 表示不关心初始布局，Vulkan 会自动处理转换。

- ```
  finalLayout
  ```

  : 渲染流程结束后的目标布局。`VK_IMAGE_LAYOUT_PRESENT_SRC_KHR` 表示图像将直接用于屏幕呈现。

一些最常见的布局是：

`VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL`：用作颜色附件的图像。
`VK_IMAGE_LAYOUT_PRESENT_SRC_KHR`：在交换链中呈现的图像。
`VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL`：用作内存复制操作目标的图像。

#### 5. 定义附件引用

在子流程中指定附件的使用方式。

```
VkAttachmentReference colorAttachmentRef{};
colorAttachmentRef.attachment = 0;                  // 引用第一个附件（索引 0）
colorAttachmentRef.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL; // 子流程中附件的布局
```

`attachment`：对应 `VkAttachmentDescription` 数组的索引（此处仅一个附件）。
`layout`：子流程运行时附件的布局，优化为颜色附件写入模式（`VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL`）。

#### 6. 定义子流程

描述一个子流程（仅一个子流程）。

```
VkSubpassDescription subpass{};
subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS; // 子流程用于图形渲染
subpass.colorAttachmentCount = 1;                            // 使用的颜色附件数量
subpass.pColorAttachments = &colorAttachmentRef;             // 指向颜色附件引用
```

`pipelineBindPoint`：指定子流程类型为图形渲染。
`colorAttachmentCount` 和 `pColorAttachments`：绑定颜色附件引用，供片段着色器输出。

#### 7. 定义子流程依赖

同步子流程之间的执行顺序和资源访问。

```
VkSubpassDependency dependency{};
dependency.srcSubpass = VK_SUBPASS_EXTERNAL;          // 依赖源：外部（渲染流程开始前）
dependency.dstSubpass = 0;                            // 依赖目标：第一个子流程（索引 0）
dependency.srcStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT; // 源阶段：颜色附件输出
dependency.srcAccessMask = 0;                         // 源访问权限：无
dependency.dstStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT; // 目标阶段：颜色附件输出
dependency.dstAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;         // 目标权限：允许写入
```

确保在子流程 0 开始写入颜色附件之前，外部操作（如交换链图像获取）已完成。

1. 源阶段：外部操作的颜色附件输出阶段。
2. 目标阶段：子流程 0 的颜色附件输出阶段。
3. 内存屏障：确保子流程 0 写入颜色附件时，外部操作不会冲突。

`VkSubpassDependency` 是 Vulkan 中用于定义渲染流程（`VkRenderPass`）中子流程（`Subpass`）之间或子流程与外部操作之间同步关系的结构体。它确保在正确的时间点对资源（如附件、内存）进行访问，避免数据竞争和未定义行为。

**结构体定义**

```
typedef struct VkSubpassDependency {
    uint32_t                srcSubpass;      // 源子流程索引
    uint32_t                dstSubpass;      // 目标子流程索引
    VkPipelineStageFlags    srcStageMask;    // 源阶段的管线阶段掩码
    VkPipelineStageFlags    dstStageMask;    // 目标阶段的管线阶段掩码
    VkAccessFlags           srcAccessMask;   // 源阶段的内存访问权限掩码
    VkAccessFlags           dstAccessMask;   // 目标阶段的内存访问权限掩码
    VkDependencyFlags       dependencyFlags; // 依赖标志（如按区域依赖）
} VkSubpassDependency;
```

**1. `srcSubpass` 和 `dstSubpass`**：定义依赖关系的源子流程和目标子流程。

- ```
  VK_SUBPASS_EXTERNAL
  ```

  ：表示依赖来自渲染流程外的操作（如交换链图像获取）或渲染流程结束后的操作。

**2. `srcStageMask` 和 `dstStageMask`**：指定源和目标子流程中需要同步的管线阶段（Pipeline Stages）。

- **常见阶段**：

- - ```
    VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT
    ```

    ：颜色附件写入阶段。

  - ```
    VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT
    ```

    ：深度/模板测试阶段。

  - ```
    VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT
    ```

    ：管线的最开始阶段（占位符）。

  - ```
    VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT
    ```

    ：管线的最后阶段（占位符）。

**3. `srcAccessMask` 和 `dstAccessMask`**：指定源和目标子流程中需要同步的内存访问类型。

- **常见访问类型**：

- - ```
    VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT
    ```

    ：对颜色附件的写入操作。

  - ```
    VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT
    ```

    ：深度/模板附件的读取操作。

  - ```
    VK_ACCESS_INPUT_ATTACHMENT_READ_BIT
    ```

    ：输入附件的读取操作。

**4. `dependencyFlags`**：控制依赖关系的特殊行为。

- **常见标志**：

- - ```
    VK_DEPENDENCY_BY_REGION_BIT
    ```

    ：依赖按区域（Tile）划分，适用于移动端分块渲染。

  - ```
    VK_DEPENDENCY_VIEW_LOCAL_BIT
    ```

    ：多视图渲染中的局部依赖。

#### 8. 创建渲染流程

将所有配置整合到 `VkRenderPassCreateInfo` 并创建渲染流程对象。

```
VkRenderPassCreateInfo renderPassInfo{};
renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
renderPassInfo.attachmentCount = 1;                   // 附件数量
renderPassInfo.pAttachments = &colorAttachment;       // 附件描述数组
renderPassInfo.subpassCount = 1;                      // 子流程数量
renderPassInfo.pSubpasses = &subpass;                 // 子流程描述数组
renderPassInfo.dependencyCount = 1;                   // 依赖数量
renderPassInfo.pDependencies = &dependency;           // 依赖描述数组

VK_CHECK(vkCreateRenderPass(device, &renderPassInfo, nullptr, &renderPass));
```