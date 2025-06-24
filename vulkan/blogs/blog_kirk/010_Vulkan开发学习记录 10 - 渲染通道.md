# Vulkan开发学习记录 10 - 渲染通道

## 配置

在进行管线创建之前，我们还需要设置用于渲染的帧缓冲附着。我们需要指定使用的颜色和[深度缓冲](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=1&q=深度缓冲&zhida_source=entity)，以及采样数，渲染操作如何处理缓冲 的内容。所有这些信息被Vulkan包装为一个渲染流程对象，我们添加了一个叫做createRenderPass的函数来创建这一对象，然后在initVulkan函数中createGraphicsPipeline[函数调用](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=1&q=函数调用&zhida_source=entity)之前调用createRenderPass函数：

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
}

...

void createRenderPass() {

}
```

## 附着描述

在这里，我们只使用了一个代表[交换链](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=1&q=交换链&zhida_source=entity)图像的颜色缓冲附着。

```cpp
void createRenderPass() {
    VkAttachmentDescription colorAttachment{};
    colorAttachment.format = swapChainImageFormat;
    colorAttachment.samples = VK_SAMPLE_COUNT_1_BIT;
}
```

format成员变量用于指定颜色缓冲附着的格式。format成员变量用于指定采样数，在这里，我们没有使用[多重采样](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=1&q=多重采样&zhida_source=entity)，所以将采样数设置为1。

```cpp
colorAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
colorAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
```

loadOp和storeOp[成员变量](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=3&q=成员变量&zhida_source=entity)用于指定在渲染之前和渲染之后对附着中的数据进行的操作。对于loadOp成员变量，可以设置为下面这些值：

- `VK_ATTACHMENT_LOAD_OP_LOAD`：保留附着的现有内容
- `VK_ATTACHMENT_LOAD_OP_CLEAR`：在开始时将值清除为常量
- `VK_ATTACHMENT_LOAD_OP_DONT_CARE`：不关心附着现存的内容

在这里，我们设置loadOp成员变量的值为VK_ATTACHMENT_LOAD_OP_CLEAR， 在每次渲染新的一帧前使用黑色清除[帧缓冲](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=2&q=帧缓冲&zhida_source=entity)。storeOp成员变量可以设置为下面这些值：

- `VK_ATTACHMENT_STORE_OP_STORE`: 渲染后的内容会存储在内存中，以后可以读取。
- `VK_ATTACHMENT_STORE_OP_DONT_CARE`: 渲染后，不会读取帧缓冲的内容 。

```cpp
colorAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
colorAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
```

loadOp和storeOp成员变量的设置会对颜色和深度缓冲起效。stencilLoadOp成员变量和stencilStoreOp成员变量会对模板缓冲起效。在这里，我们没有使用[模板缓冲](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=2&q=模板缓冲&zhida_source=entity)，所以设置对模板缓冲不关心即可。

```cpp
colorAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
colorAttachment.finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
```

Vulkan中的纹理和帧缓冲由特定像素格式的VkImage对象来表示。图像的像素数据在内存中的分布取决于我们要对图像进行的操作。 下面是一些常用的图形内存布局设置：

- `VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL`：用作颜色附着的图像
- `VK_IMAGE_LAYOUT_PRESENT_SRC_KHR`：要在交换链中呈现的图像
- `VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL`: 用作内存复制操作目标的图像

在这里，我们需要做的是指定适合我们在之后进行的渲染操作的图像布局即可。

initialLayout成员变量用于指定渲染流程开始前的图像布局方式。finalLayout成员变量用于指定渲染流程结束后的图像布局方式。将initialLayout成员变量设置为VK_IMAGE_LAYOUT_UNDEFINED表示我们不关心之前的图像布 局方式。使用这一值后，图像的内容不保证会被保留，但对于我们的应用程序，每次渲染前都要清除图像，所以这样的设置更符合我们的需求。对于finalLayout成员变量，我们设置为VK_IMAGE_LAYOUT_PRESENT_SRC_KHR， 使得渲染后的图像可以被交换链呈现。

## 子流程和附着引用

一个渲染流程可以包含多个子流程。子流程依赖于上一流程处理后的帧缓冲内容。比如，许多叠加的后期处理效果就是在上一次的处理结果上进行的。我们将多个子流程组成一个[渲染流程](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=5&q=渲染流程&zhida_source=entity)后，Vulkan可以对其进行一定程度的优化。对于我们这个渲染三角形的程序，我们只使用了一个子流程。

每个子流程可以引用一个或多个附着，这些引用的附着是通过VkAttachmentReference结构体指定的：

```cpp
VkAttachmentReference colorAttachmentRef{};
colorAttachmentRef.attachment = 0;
colorAttachmentRef.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
```

attachment成员变量用于指定要引用的附着在附着描述[结构体数组](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=1&q=结构体数组&zhida_source=entity)中的索引。在这里，我们的`VkAttachmentDescription`数组只包含了一个附着信息，所以将attachment指定为0即可。layout成员变量用于指定进行子流程时引用的附着使用的布局方式，Vulkan会在子流程开始时自动将引用的附着转换到layout成员变量指定的图像布局。我们推荐将layout成员变量设置为VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL，一般而言，它的性能表现最佳。

我们使用VkSubpassDescription结构体来描述子流程：

```cpp
VkSubpassDescription subpass{};
subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
```

Vulkan在未来也可能会支持计算子流程，所以，我们还需要显式地指定这是一个图形渲染的子流程。接着，我们指定引用的颜色附着：

```cpp
subpass.colorAttachmentCount = 1;
subpass.pColorAttachments = &colorAttachmentRef;
```

这里设置的颜色附着在数组中的索引会被[片段着色器](https://zhida.zhihu.com/search?content_id=217934533&content_type=Article&match_order=1&q=片段着色器&zhida_source=entity)使用，对应我们在片段着色器中使用的layout(location = 0) out vec4 outColor 语句。下面是其它一些可以被子流程引用的附着类型：

- `pInputAttachments`：从着色器读取的附着
- `pResolveAttachments`：用于多重采样颜色附件的附着
- `pDepthStencilAttachment`: 深度和模板数据的附着
- `pPreserveAttachments`：此子通道未使用但必须保留其数据的附着

## 渲染流程

现在，我们已经设置好了附着和引用它的子流程，可以开始创建渲染流程对象。首先，我们在pipelineLayout变量定义之前添加一个VkRenderPass类型的成员变量：

```cpp
VkRenderPass renderPass;
VkPipelineLayout pipelineLayout;
```

创建渲染流程对象需要填写VkRenderPassCreateInfo结构体。

```cpp
VkRenderPassCreateInfo renderPassInfo{};
renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
renderPassInfo.attachmentCount = 1;
renderPassInfo.pAttachments = &colorAttachment;
renderPassInfo.subpassCount = 1;
renderPassInfo.pSubpasses = &subpass;

if (vkCreateRenderPass(device, &renderPassInfo, nullptr, &renderPass) != VK_SUCCESS) {
    throw std::runtime_error("failed to create render pass!");
}
```

和管线布局对象一样，我们需要在应用程序结束前，清除渲染流程对象：

```cpp
void cleanup() {
    vkDestroyPipelineLayout(device, pipelineLayout, nullptr);
    vkDestroyRenderPass(device, renderPass, nullptr);
    ...
}
```

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

