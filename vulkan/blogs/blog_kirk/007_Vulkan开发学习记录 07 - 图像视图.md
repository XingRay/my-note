# Vulkan开发学习记录 07 - 图像视图

## 简述

使用任何Vulkan对象，包括处于[交换链](https://zhida.zhihu.com/search?content_id=217783761&content_type=Article&match_order=1&q=交换链&zhida_source=entity)中的，处于渲染管线中的，都 需要我们创建一个VkImageView对象来绑定访问它。图像视图描述了访问图像的方式，以及图像的哪一部分可以被访问。比如，图像可以被图像视图描述为一个没有细化级别的二维深度纹理，进而可以在其上进行与二维深度纹理相关的操作。

## 图像视图

首先，我们编写了一个叫做createImageView函数来为交换链中的每一个图像建立图像视图。

```cpp
std::vector<VkImageView> swapChainImageViews;
```

然后，添加createImageView函数，并在InitVulkan[函数调用](https://zhida.zhihu.com/search?content_id=217783761&content_type=Article&match_order=1&q=函数调用&zhida_source=entity)createSwapChain函数创建交换链之后调用它：

```cpp
void initVulkan() {
    createInstance();
    setupDebugMessenger();
    createSurface();
    pickPhysicalDevice();
    createLogicalDevice();
    createSwapChain();
    createImageViews();
}

void createImageViews() {

}
```

接着，我们分配足够的[数组空间](https://zhida.zhihu.com/search?content_id=217783761&content_type=Article&match_order=1&q=数组空间&zhida_source=entity)来存储图像视图：

```cpp
void createImageViews() {
    swapChainImageViews.resize(swapChainImages.size());

}
```

遍历所有交换链图像，创建图像视图：

```cpp
for (size_t i = 0; i < swapChainImages.size(); i++) {

}
```

图像视图的创建需要我们填写VkImageViewCreateInfo结构体

```cpp
VkImageViewCreateInfo createInfo{};
createInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
createInfo.image = swapChainImages[i];
```

viewType和format成员变量用于指定图像数据的解释方式。viewType[成员变量](https://zhida.zhihu.com/search?content_id=217783761&content_type=Article&match_order=2&q=成员变量&zhida_source=entity)用于指定图像被看作是一维纹理、二维纹理、三维纹理还是[立方体贴图](https://zhida.zhihu.com/search?content_id=217783761&content_type=Article&match_order=1&q=立方体贴图&zhida_source=entity)。

```cpp
createInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;
createInfo.format = swapChainImageFormat;
```

components成员变量用于进行图像[颜色通道](https://zhida.zhihu.com/search?content_id=217783761&content_type=Article&match_order=1&q=颜色通道&zhida_source=entity)的[映射](https://zhida.zhihu.com/search?content_id=217783761&content_type=Article&match_order=1&q=映射&zhida_source=entity)。比如，对于单色纹理，我们可以将所有颜色通道映射到红色通道。我们也可以直接将颜色通道的[值映射](https://zhida.zhihu.com/search?content_id=217783761&content_type=Article&match_order=1&q=值映射&zhida_source=entity)为常数0或1。在这里，我们只使用默认的映射：

```cpp
createInfo.components.r = VK_COMPONENT_SWIZZLE_IDENTITY;
createInfo.components.g = VK_COMPONENT_SWIZZLE_IDENTITY;
createInfo.components.b = VK_COMPONENT_SWIZZLE_IDENTITY;
createInfo.components.a = VK_COMPONENT_SWIZZLE_IDENTITY;
```

subresourceRange成员变量用于指定图像的用途和图像的哪一部分可以被访问。在这里，我们的图像被用作渲染目标，并且没有细分级别，只存在一个图层：

```cpp
createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
createInfo.subresourceRange.baseMipLevel = 0;
createInfo.subresourceRange.levelCount = 1;
createInfo.subresourceRange.baseArrayLayer = 0;
createInfo.subresourceRange.layerCount = 1;
```

调用vkCreateImageView函数创建图像视图：

```text
if (vkCreateImageView(device, &createInfo, nullptr, &swapChainImageViews[i]) != VK_SUCCESS) {
    throw std::runtime_error("failed to create image views!");
}
```

与交换链图像不同，图像视图是由我们显式创建的，因此我们需要添加一个类似的循环以在程序结束时再次销毁它们：

```cpp
void cleanup() {
    for (auto imageView : swapChainImageViews) {
        vkDestroyImageView(device, imageView, nullptr);
    }

    ...
}
```

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

