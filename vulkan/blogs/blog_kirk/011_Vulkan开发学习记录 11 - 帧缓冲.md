# Vulkan开发学习记录 11 - 帧缓冲

## 配置

我们在创建渲染流程对象时指定使用的附着需要绑定在帧缓冲对象上使用。帧缓冲[对象引用](https://zhida.zhihu.com/search?content_id=218155893&content_type=Article&match_order=1&q=对象引用&zhida_source=entity)了用于表示附着的VkImageView 对象。对于我们的程序，我们只使用了一个颜色附着。但这并不意味着我们只需要使用一张 图像，每个附着对应的图像个数依赖于[交换链](https://zhida.zhihu.com/search?content_id=218155893&content_type=Article&match_order=1&q=交换链&zhida_source=entity)用于呈现操作的图像个数。 我们需要为交换链中的每个图像创建对应的[帧缓冲](https://zhida.zhihu.com/search?content_id=218155893&content_type=Article&match_order=3&q=帧缓冲&zhida_source=entity)，在渲染时，渲染到对应的帧缓冲上。

添加一个向量作为[成员变量](https://zhida.zhihu.com/search?content_id=218155893&content_type=Article&match_order=1&q=成员变量&zhida_source=entity)来存储所有帧缓冲对象：

```cpp
std::vector<VkFramebuffer> swapChainFramebuffers;
```

添加一个叫做`createFramebuffers`的函数来完成所有帧缓冲对象的创建， 并在initVulkan函数中创建图形管线的操作之后调用它：

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
}

...

void createFramebuffers() {

}
```

分配足够的空间来存储所有帧缓冲对象：

```cpp
void createFramebuffers() {
    swapChainFramebuffers.resize(swapChainImageViews.size());
}
```

为交换链的每一个图像视图对象创建对应的帧缓冲：

```cpp
for (size_t i = 0; i < swapChainImageViews.size(); i++) {
    VkImageView attachments[] = {
        swapChainImageViews[i]
    };

    VkFramebufferCreateInfo framebufferInfo{};
    framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
    framebufferInfo.renderPass = renderPass;
    framebufferInfo.attachmentCount = 1;
    framebufferInfo.pAttachments = attachments;
    framebufferInfo.width = swapChainExtent.width;
    framebufferInfo.height = swapChainExtent.height;
    framebufferInfo.layers = 1;

    if (vkCreateFramebuffer(device, &framebufferInfo, nullptr, &swapChainFramebuffers[i]) != VK_SUCCESS) {
        throw std::runtime_error("failed to create framebuffer!");
    }
}
```

如所看到的，创建帧缓冲的操作非常直白。我们首先指定帧缓冲需要兼容的渲染流程对象。之后的渲染操作，我们可以使用与这个指定的[渲染流程](https://zhida.zhihu.com/search?content_id=218155893&content_type=Article&match_order=3&q=渲染流程&zhida_source=entity)对象相兼容的其它渲染流程对象。一般来说，使用相同数量，相同类型附着的渲染流程对象是相兼容的。

attachmentCount和pAttachments成员变量用于指定附着个数，以及渲染流程对象用于描述附着信息的pAttachments数组。

width和height成员变量用于指定帧缓冲的大小，layer成员变量用于指定图像层数。我们使用的交换链图像都是单层的，所以将扬扡批扥扲扳成员变量设置为1。

我们需要在应用程序结束前，清除图像视图和渲染流程对象前清除帧缓冲对象：

```cpp
void cleanup() {
    for (auto framebuffer : swapChainFramebuffers) {
        vkDestroyFramebuffer(device, framebuffer, nullptr);
    }

    ...
}
```

## [工程链接](https://zhida.zhihu.com/search?content_id=218155893&content_type=Article&match_order=1&q=工程链接&zhida_source=entity)

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

