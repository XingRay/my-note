# Vulkan 图像视图

这一系列文章，前期都将围绕着这一张图展开来讲。

众所周知，Vulkan 编程的代码量相对于 OpenGL 多了一个数量级（不用害怕，后面Vulkan封装一下，用起来也会非常简洁），本文避免一上去就讲一大堆代码，奉行概念先行。

概念掌握的差不多了，再去看代码, 这样思路不容易卡住，大致就可以把握住整体代码逻辑。最后在看代码的过程中，理解和巩固概念，这样 Vulkan 学起来将事半功倍。

前面有读者提建议说，每一篇文章讲的知识点太少了，**其实这一系列文章主要面向的是初学者，不宜一次性写太多的概念，每次讲 1~2 个重要知识点，理解起来刚刚好，也不会因为太累而放弃"治疗"。**

本期说说 Vulkan 图像视图 VkImageView 。

# 什么是 Vulkan 图像视图

Vulkan 图像视图（VkImageView）**用于描述如何访问 VkImage 对象以及访问图像的哪一部分。**

**图像视图定义了图像的格式和访问方式，它允许渲染管线与图像进行交互，无论是作为纹理、颜色附件还是深度/模板附件。**

在交换链中，我们需要为每个图像创建一个基本的图像视图，以便将它们用作颜色附件。

VkImage 作为纹理采样时，也必须要创建其对应的图像视图来更新描述符集 （DescriptorSet）。

# 创建图像视图

我们通过函数 vkCreateImageView 创建图像视图。

```c
// vkCreateImageView 函数用于创建图像视图
VkResult vkCreateImageView(
    VkDevice device,                                  // Vulkan 逻辑设备的句柄，表示在哪个设备上创建图像视图
    const VkImageViewCreateInfo* pCreateInfo,         // 指向包含图像视图创建信息的 VkImageViewCreateInfo 结构体的指针
    const VkAllocationCallbacks* pAllocator,          // 指向自定义分配函数的指针，可以为 nullptr 表示使用默认分配器
    VkImageView* pView                                // 指向 VkImageView 变量的指针，用于存储创建的图像视图句柄
);
```

其中 VkImageViewCreateInfo 是一个包含图像视图创建信息的结构体。

```c
typedef struct VkImageViewCreateInfo {
    VkStructureType    sType;                // 结构体的类型，对于此结构体应为VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO
    const void*        pNext;                // 指向扩展特定数据的指针，对于核心Vulkan功能，通常为NULL
    VkImageViewCreateFlags flags;           // 创建图像视图时使用的标志位
    VkImage            image;                // 要创建视图的图像的句柄
    VkImageViewType    viewType;             // 图像视图的类型，决定了视图是如何解释图像的
    VkFormat           format;               // 图像数据的格式，定义了图像数据的解释方式
    VkComponentMapping components;           // 用于在创建视图时重新映射图像的颜色分量
    VkImageSubresourceRange subresourceRange; // 定义了图像视图的子资源范围，包括mipmap级别、数组层以及深度层的范围
} VkImageViewCreateInfo;
```

其中 VkImageViewType、VkComponentMapping、VkImageSubresourceRange 类型的属性重点关注下。

## VkImageViewType

VkImageViewType 指定了图像视图的类型。

```c
// VkImageViewType 是一个枚举类型，指定了图像视图的类型。
typedef enum VkImageViewType {
    VK_IMAGE_VIEW_TYPE_1D = 0,          // 一维图像视图
    VK_IMAGE_VIEW_TYPE_2D = 1,          // 二维图像视图
    VK_IMAGE_VIEW_TYPE_3D = 2,          // 三维图像视图
    VK_IMAGE_VIEW_TYPE_CUBE = 3,        // 立方体贴图视图（六个面）
    VK_IMAGE_VIEW_TYPE_1D_ARRAY = 4,    // 一维图像数组
    VK_IMAGE_VIEW_TYPE_2D_ARRAY = 5,    // 二维图像数组
    VK_IMAGE_VIEW_TYPE_CUBE_ARRAY = 6   // 立方体贴图数组（六个面 * 图层数）
} VkImageViewType;
```

## VkComponentMapping

VkComponentMapping 用于指定如何从源图像中的颜色分量映射到目标图像视图的颜色分量。

通过 VkComponentMapping 类型的属性（components） 可以灵活地控制图像视图中每个颜色分量的来源，这对于图像处理非常有用，例如转换图像格式、调整颜色通道等。

默认情况下，我们将每个分量设置为 VK_COMPONENT_SWIZZLE_IDENTITY，这意味着它们将直接从源图像中取值。

```c
// VkComponentMapping 结构体定义了图像视图中每个颜色分量的来源。
typedef struct VkComponentMapping {
    VkComponentSwizzle r;  // R 分量的来源
    VkComponentSwizzle g;  // G 分量的来源
    VkComponentSwizzle b;  // B 分量的来源
    VkComponentSwizzle a;  // A 分量的来源
} VkComponentMapping;

// VkComponentSwizzle 是一个枚举类型，定义了分量的来源。
typedef enum VkComponentSwizzle {
    VK_COMPONENT_SWIZZLE_IDENTITY = 0,  // 使用原始分量
    VK_COMPONENT_SWIZZLE_ZERO = 1,      // 使用零值
    VK_COMPONENT_SWIZZLE_ONE = 2,       // 使用一值
    VK_COMPONENT_SWIZZLE_R = 100,       // 使用 R 分量
    VK_COMPONENT_SWIZZLE_G = 101,       // 使用 G 分量
    VK_COMPONENT_SWIZZLE_B = 102,       // 使用 B 分量
    VK_COMPONENT_SWIZZLE_A = 103        // 使用 A 分量
} VkComponentSwizzle;
```

## VkImageSubresourceRange

VkImageSubresourceRange 允许你选择图像的哪些层面和 mip 级别应该被包括在图像视图中。

```c
// VkImageSubresourceRange 结构体定义了图像视图应覆盖的图像子资源范围。
typedef struct VkImageSubresourceRange {
    VkImageAspectFlags aspectMask;  // 需要包括的图像方面
    uint32_t baseMipLevel;          // mip 级别的起始级别
    uint32_t levelCount;            // mip 级别的数量
    uint32_t baseArrayLayer;        // 数组层的起始索引
    uint32_t layerCount;            // 数组层数量
} VkImageSubresourceRange;
```

aspectMask 这个属性比较难理解，它用于指定需要包括在图像视图中的图像方面。

例如，如果图像有深度和/或模板信息，你可以选择只包括颜色方面 (VK_IMAGE_ASPECT_COLOR_BIT)，或者包括深度方面 (VK_IMAGE_ASPECT_DEPTH_BIT) 和/或模板方面 (VK_IMAGE_ASPECT_STENCIL_BIT)。

# 示例代码

```c
// 定义 VkImageCreateInfo 结构体并初始化
VkImageCreateInfo imageInfo = {};
imageInfo.sType = VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO;      // 结构体类型
imageInfo.pNext = nullptr;                                  // 指向扩展信息的指针
imageInfo.flags = 0;                                        // 图像创建标志，当前必须为 0
imageInfo.imageType = VK_IMAGE_TYPE_2D;                     // 图像类型为 2D
imageInfo.format = VK_FORMAT_R8G8B8A8_SRGB;                 // 图像格式为 sRGB
imageInfo.extent.width = texWidth;                          // 图像宽度
imageInfo.extent.height = texHeight;                        // 图像高度
imageInfo.extent.depth = 1;                                 // 图像深度（对于 2D 图像为 1）
imageInfo.mipLevels = 1;                                    // MIP 级别数量
imageInfo.arrayLayers = 1;                                  // 图像数组层数
imageInfo.samples = VK_SAMPLE_COUNT_1_BIT;                  // 多重采样数量（1 表示不使用多重采样）
imageInfo.tiling = VK_IMAGE_TILING_OPTIMAL;                 // 图像数据的存储方式（优化存储）
imageInfo.usage = VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT; // 图像用途（作为传输目标和采样器）
imageInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE;          // 共享模式（独占模式）
imageInfo.queueFamilyIndexCount = 0;                        // 使用队列族索引的数量
imageInfo.pQueueFamilyIndices = &queueFamilyIndex;          // 队列族索引的指针
imageInfo.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;        // 图像的初始布局（未定义）

VkImage textureImage; // 定义 VkImage 变量
// 创建图像
if (vkCreateImage(device, &imageInfo, nullptr, &textureImage) != VK_SUCCESS) {
    throw std::runtime_error("failed to create texture image!"); // 如果创建图像失败，抛出异常
}

// 获取图像的内存需求
VkMemoryRequirements memRequirements;
vkGetImageMemoryRequirements(device, textureImage, &memRequirements);

// 创建 VkMemoryAllocateInfo 结构体并初始化
VkMemoryAllocateInfo allocInfo = {};
allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO; // 结构体类型
allocInfo.allocationSize = memRequirements.size;          // 所需内存大小
allocInfo.memoryTypeIndex = findMemoryType(memRequirements.memoryTypeBits,
    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);                 // 内存类型索引

VkDeviceMemory textureImageMemory; // 定义 VkDeviceMemory 变量
// 分配内存
if (vkAllocateMemory(device, &allocInfo, nullptr, &textureImageMemory) != VK_SUCCESS) {
    throw std::runtime_error("failed to allocate texture image memory!"); // 如果分配内存失败，抛出异常
}

// 将内存绑定到图像
vkBindImageMemory(device, textureImage, textureImageMemory, 0);

// 定义 VkImageViewCreateInfo 结构体并初始化
VkImageViewCreateInfo viewInfo = {};
viewInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;  // 结构体类型
viewInfo.pNext = nullptr;                                   // 指向扩展信息的指针
viewInfo.flags = 0;                                         // 图像视图创建标志
viewInfo.image = textureImage;                              // 要创建视图的图像句柄
viewInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;                  // 图像视图类型为 2D
viewInfo.format = VK_FORMAT_R8G8B8A8_UNORM;                  // 图像视图的格式
viewInfo.components.r = VK_COMPONENT_SWIZZLE_R;      // R 分量映射
viewInfo.components.g = VK_COMPONENT_SWIZZLE_G;      // G 分量映射
viewInfo.components.b = VK_COMPONENT_SWIZZLE_B;      // B 分量映射
viewInfo.components.a = VK_COMPONENT_SWIZZLE_A;      // A 分量映射
viewInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT; // 视图的子资源范围（颜色部分）
viewInfo.subresourceRange.baseMipLevel = 0;                 // 基础 MIP 级别
viewInfo.subresourceRange.levelCount = 1;                   // MIP 级别数量
viewInfo.subresourceRange.baseArrayLayer = 0;               // 基础数组层
viewInfo.subresourceRange.layerCount = 1;                   // 数组层数量

VkImageView textureImageView; // 定义 VkImageView 变量
// 创建图像视图
if (vkCreateImageView(device, &viewInfo, nullptr, &textureImageView) != VK_SUCCESS) {
    throw std::runtime_error("failed to create texture image view!"); // 如果创建视图失败，抛出异常
}

// Vulkan 编程...

// 使用完成后销毁资源
vkDestroyImageView(device, textureImageView, nullptr); // 销毁图像视图
vkDestroyImage(device, textureImage, nullptr);         // 销毁图像
vkFreeMemory(device, textureImageMemory, nullptr);     // 释放图像内存
```

-- END --
