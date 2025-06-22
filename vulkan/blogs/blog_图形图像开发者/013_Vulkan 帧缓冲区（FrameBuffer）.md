# Vulkan 帧缓冲区（FrameBuffer）

**Vulkan 帧缓冲区**



**Vulkan 帧缓冲区（Framebuffer）是一个容器对象（资源管理类型的对象），包含了一组图像视图（Image Views），用于在渲染通道（Render Pass）中作为附件（Attachments）进行渲染。**



每个帧缓冲区的附件与[渲染通道](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654177269&idx=1&sn=be0d98bfa045c6e84c07e607a9eaa6a8&chksm=8cf354c6bb84ddd02bdd202df24045d6433950fe048a92130ab3f493b7bcdc957b7c79ed633a&scene=21#wechat_redirect)的附件描述（Attachment Description）相对应，它们共同定义了渲染的目标，重点强调一下，**附件必须与渲染通道的附件描述匹配**。



**帧缓冲区在 Vulkan 中的概念类似于 OpenGL 的帧缓冲对象（FBO）**，每个帧缓冲区包含一组图像视图，这些图像视图表示实际的图像资源，可以是颜色附件、深度附件或模板附件，帧缓冲区用于将渲染输出写入这些图像中（存储渲染通道（Render Pass）输出的图像）。

在 Vulkan 中，每个帧缓冲区至少需要一个颜色附件，而这个颜色附件可以是与 SwapChain 关联的 ImageView。

# 创建 Vulkan 帧缓冲区

帧缓冲区的创建过程通常涉及以下步骤：



1. **确定帧缓冲区需要兼容的 RenderPass**（前文中已经讲过），因为 RenderPass 定义了渲染操作的结构和执行顺序，以及所需的附件的数量、类型和格式。
2. **为每个 SwapChain 图像创建对应的 ImageView** （前文中已经讲过），因为最终渲染的图像需要显示在屏幕上，SwapChain 的 ImageView 就是用来呈现到屏幕上的。
3. 使用 VkFramebufferCreateInfo 结构体来创建 Framebuffer，指定渲染通道、附件个数、附件信息、宽度、高度和层数等参数。

用于创建帧缓冲区（Framebuffer）的结构体 VkFramebufferCreateInfo ：

```
 1typedef struct VkFramebufferCreateInfo {
 2    VkStructureType          sType;         // 必须是 VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO
 3    const void*              pNext;         // 指向扩展结构的指针，通常为 nullptr
 4    VkFramebufferCreateFlags flags;         // 帧缓冲区创建标志，目前保留，必须为 0
 5    VkRenderPass             renderPass;    // 与帧缓冲区相关联的渲染通道（Render Pass）
 6    uint32_t                 attachmentCount; // 帧缓冲区的附件数量
 7    const VkImageView*       pAttachments;  // 指向 VkImageView 的指针数组，每个指针指向一个附件
 8    uint32_t                 width;         // 帧缓冲区的宽度
 9    uint32_t                 height;        // 帧缓冲区的高度
10    uint32_t                 layers;        // 帧缓冲区的层数（通常为 1）
11} VkFramebufferCreateInfo;
```

创建包含颜色、深度模版附件的帧缓冲区：

```
 1// 创建交换链图像视图（作为颜色附件）
 2std::vector<VkImageView> swapChainImageViews(swapChainImages.size());
 3for (size_t i = 0; i < swapChainImages.size(); i++) {
 4    VkImageViewCreateInfo createInfo = {};
 5    createInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
 6    createInfo.image = swapChainImages[i]; // 交换链图像
 7    createInfo.viewType = VK_IMAGE_VIEW_TYPE_2D; // 2D 图像视图
 8    createInfo.format = swapChainImageFormat; // 图像格式
 9    createInfo.components.r = VK_COMPONENT_SWIZZLE_IDENTITY; // 红色分量
10    createInfo.components.g = VK_COMPONENT_SWIZZLE_IDENTITY; // 绿色分量
11    createInfo.components.b = VK_COMPONENT_SWIZZLE_IDENTITY; // 蓝色分量
12    createInfo.components.a = VK_COMPONENT_SWIZZLE_IDENTITY; // 透明度分量
13    createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT; // 颜色附件
14    createInfo.subresourceRange.baseMipLevel = 0; // 基础 mip 级别
15    createInfo.subresourceRange.levelCount = 1; // mip 级别数量
16    createInfo.subresourceRange.baseArrayLayer = 0; // 基础数组层
17    createInfo.subresourceRange.layerCount = 1; // 数组层数量
18
19    if (vkCreateImageView(device, &createInfo, nullptr, &swapChainImageViews[i]) != VK_SUCCESS) {
20        throw std::runtime_error("failed to create image views!");
21    }
22}
23
24//创建深度模版图像视图
25VkImage depthStencilImage;//假设已经创建好
26VkImageView depthStencilImageView;
27VkImageViewCreateInfo viewInfo = {};
28viewInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
29viewInfo.image = depthStencilImage;
30viewInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;
31viewInfo.format = depthFormat;
32viewInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_DEPTH_BIT | VK_IMAGE_ASPECT_STENCIL_BIT;
33viewInfo.subresourceRange.baseMipLevel = 0;
34viewInfo.subresourceRange.levelCount = 1;
35viewInfo.subresourceRange.baseArrayLayer = 0;
36viewInfo.subresourceRange.layerCount = 1;
37
38if (vkCreateImageView(device, &viewInfo, nullptr, &depthStencilImageView) != VK_SUCCESS) {
39    throw std::runtime_error("failed to create depth image view!");
40}
41
42
43// 创建帧缓冲区
44std::vector<VkFramebuffer> swapChainFramebuffers(swapChainImageViews.size());
45for (size_t i = 0; i < swapChainImageViews.size(); i++) {
46    std::array<VkImageView, 2> attachments = {
47        swapChainImageViews[i],   // 颜色附件
48        depthStencilImageView     // 深度模板附件
49    };
50
51    VkFramebufferCreateInfo framebufferInfo = {};
52    framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
53    framebufferInfo.renderPass = renderPass; // 前文创建好的渲染通道
54    framebufferInfo.attachmentCount = static_cast<uint32_t>(attachments.size()); // 附件数量
55    framebufferInfo.pAttachments = attachments.data(); // 附件数组数据
56    framebufferInfo.width = swapChainExtent.width; // 帧缓冲区宽度
57    framebufferInfo.height = swapChainExtent.height; // 帧缓冲区高度
58    framebufferInfo.layers = 1; // 层数量
59
60    if (vkCreateFramebuffer(device, &framebufferInfo, nullptr, &swapChainFramebuffers[i]) != VK_SUCCESS) {
61        throw std::runtime_error("failed to create framebuffer!");
62    }
63}
64
65// Vulkan 编程。。。
66
67// 销毁帧缓冲区
68for (size_t i = 0; i < swapChainFramebuffers.size(); i++) {
69    vkDestroyFramebuffer(device, swapChainFramebuffers[i], nullptr);
70}
71
72// 销毁交换链图像视图
73for (size_t i = 0; i < swapChainImageViews.size(); i++) {
74    vkDestroyImageView(device, swapChainImageViews[i], nullptr);
75}
76
77// 销毁深度模板图像视图
78vkDestroyImageView(device, depthStencilImageView, nullptr);
```

