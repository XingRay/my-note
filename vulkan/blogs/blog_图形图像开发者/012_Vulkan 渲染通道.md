# Vulkan 渲染通道

# 什么是渲染通道

Vulkan 渲染通道（RenderPass）**定义了整个渲染管线的一次执行过程**，包括了渲染过程中使用的所有资源和操作的描述（**比如指定渲染管线的渲染目标，告诉管线要渲染到哪里**）。



**RenderPass 本质上是一个渲染流程的完整描述（管理渲染流程）**，包含了如何渲染这些数据的元数据和指令，但不包含实际的数据（图像），通过与 Framebuffer 结合来获取实际的图像数据 。



在 Vulkan 编程中，**RenderPass 是必不可少的，它必须包含一个或多个子通道（SubPass）。**



每个子通道表示一个渲染阶段，且都是使用 RenderPass 中定义的资源描述这个阶段渲染的步骤。



RenderPass 是通过附件（Attachment）的形式描述图像资源，包括颜色附件（Color Attachment）、深度/模板附件（Depth/Stencil Attachment）、用于多重采样的解析附件（Resolve Attachment）和输入附件（Input Attachment）等。这些附件我们后续都会一一展开讲述。



RenderPass 与 Framebuffer 的关系密切，Framebuffer 代表了 RenderPass 使用的具体内存集合，定义了 RenderPass 中的每个 ImageView 与附件的对应关系 。关于 Framebuffer 我们下一节会具体展开。



**RenderPass 使得开发者能够更精细地控制渲染过程，优化性能，同时适应现代GPU架构的特点。**

# RenderPass 创建

#### 1. 定义附件描述

附件描述定义了在渲染过程中使用的图像资源，包括它们的格式、样本数、加载和存储操作等。


我们定义一个深度附件和一个模板附件：



```
 1// 定义颜色附件描述
 2VkAttachmentDescription colorAttachment = {};
 3colorAttachment.format = swapChainImageFormat; // 交换链图像格式
 4colorAttachment.samples = VK_SAMPLE_COUNT_1_BIT; // 采样数
 5colorAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR; // 在渲染前清除附件
 6colorAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE; // 在渲染后存储附件内容
 7colorAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE; // 不关心模板加载操作
 8colorAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE; // 不关心模板存储操作
 9colorAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED; // 初始布局
10colorAttachment.finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR; // 最终布局
11
12// 定义深度模板附件描述
13VkAttachmentDescription depthAttachment = {};
14depthAttachment.format = findDepthFormat(physicalDevice); // 深度模板格式
15depthAttachment.samples = VK_SAMPLE_COUNT_1_BIT; // 采样数
16depthAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR; // 在渲染前清除附件
17depthAttachment.storeOp = VK_ATTACHMENT_STORE_OP_DONT_CARE; // 渲染后不需要存储附件内容
18depthAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE; // 不关心模板加载操作
19depthAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE; // 不关心模板存储操作
20depthAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED; // 初始布局
21depthAttachment.finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL; // 最终布局
```

#### 2. 定义子通道

**Vulkan 用于定义子通道的结构体 VkSubpassDescription 需要重点关注下：**

```
 1typedef struct VkSubpassDescription {
 2VkSubpassDescriptionFlags    flags;                  // 子通道描述的附加标志，目前必须为0
 3VkPipelineBindPoint          pipelineBindPoint;      // 管线绑定点，必须是 VK_PIPELINE_BIND_POINT_GRAPHICS
 4uint32_t                     inputAttachmentCount;   // 输入附件的数量
 5const VkAttachmentReference* pInputAttachments;      // 输入附件的数组
 6uint32_t                     colorAttachmentCount;   // 颜色附件的数量
 7const VkAttachmentReference* pColorAttachments;      // 颜色附件的数组
 8const VkAttachmentReference* pResolveAttachments;    // 解析附件的数组（可选）
 9const VkAttachmentReference* pDepthStencilAttachment; // 深度模板附件（可选）
10uint32_t                     preserveAttachmentCount; // 保留附件的数量
11const uint32_t*              pPreserveAttachments;   // 保留附件的数组
12} VkSubpassDescription;
```

子通道描述了渲染管道的一个阶段及其输入和输出附件。

```
 1// 定义颜色附件引用
 2VkAttachmentReference colorAttachmentRef = {};
 3colorAttachmentRef.attachment = 0; // 绑定到第一个附件描述
 4colorAttachmentRef.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL; // 最佳颜色附件布局
 5
 6// 定义深度模板附件引用
 7VkAttachmentReference depthAttachmentRef = {};
 8depthAttachmentRef.attachment = 1; // 绑定到第二个附件描述
 9depthAttachmentRef.layout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL; // 最佳深度模板附件布局
10
11// 定义子通道描述
12VkSubpassDescription subpass = {};
13subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS; // 图形管线绑定点
14subpass.colorAttachmentCount = 1; // 颜色附件数量
15subpass.pColorAttachments = &colorAttachmentRef; // 颜色附件引用
16subpass.pDepthStencilAttachment = &depthAttachmentRef; // 深度模板附件引用
```

#### 3. 定义子通道依赖

子通道依赖 VkSubpassDependency 在多通道渲染时比较重要（现在大致了解下，后面还会讲到），**它的作用是管理不同子通道之间的依赖关系，确保数据在管道阶段之间的正确同步。**

VkSubpassDependency 结构体：

```
 1typedef struct VkSubpassDependency {
 2uint32_t srcSubpass;    // 源子通道索引或VK_SUBPASS_EXTERNAL。
 3// 如果设置为VK_SUBPASS_EXTERNAL，表示依赖于渲染通道外部的操作，
 4// 比如在渲染通道开始前或结束后的操作。
 5
 6uint32_t dstSubpass;    // 目标子通道索引或VK_SUBPASS_EXTERNAL。
 7// 如果设置为VK_SUBPASS_EXTERNAL，表示依赖于渲染通道外部的操作。
 8
 9VkPipelineStageFlags srcStageMask;  // 源阶段掩码。
10// 指定在这些阶段结束时，依赖将生效。
11// 常见阶段包括：
12// VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT - 颜色附件输出阶段
13// VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT - 早期片段测试阶段
14// VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT - 晚期片段测试阶段等。
15
16VkPipelineStageFlags dstStageMask;  // 目标阶段掩码。
17// 指定在这些阶段开始前，依赖将生效。
18// 常见阶段包括：
19// VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT - 颜色附件输出阶段
20// VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT - 早期片段测试阶段
21// VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT - 晚期片段测试阶段等。
22
23VkAccessFlags srcAccessMask;        // 源访问掩码。
24// 指定在这些访问类型完成后，依赖将生效。
25// 常见访问类型包括：
26// VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT - 颜色附件写入
27// VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT - 深度模板附件写入等。
28
29VkAccessFlags dstAccessMask;        // 目标访问掩码。
30// 指定在这些访问类型开始前，依赖将生效。
31// 常见访问类型包括：
32// VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT - 颜色附件写入
33// VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT - 深度模板附件写入
34// VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT - 深度模板附件读取等。
35
36VkDependencyFlags dependencyFlags;  // 依赖标志。
37// 可以是0或包含以下标志之一：
38// VK_DEPENDENCY_BY_REGION_BIT - 表示依赖仅在图像的同一区域内生效。
39// VK_DEPENDENCY_VIEW_LOCAL_BIT - 表示依赖在单个描述符的视图上生效。
40// VK_DEPENDENCY_DEVICE_GROUP_BIT - 表示依赖在设备组内生效。
41} VkSubpassDependency;
 1// 定义子通道依赖
 2// 子通道依赖数组，用于布局转换
 3std::array<VkSubpassDependency, 2> dependencies;
 4
 5// 第一个依赖关系
 6dependencies[0].srcSubpass = VK_SUBPASS_EXTERNAL; // 外部到第一个子通道的依赖
 7dependencies[0].dstSubpass = 0; // 目标子通道索引为0，即第一个子通道
 8dependencies[0].srcStageMask = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT; // 源阶段掩码：早期和晚期片段测试阶段
 9dependencies[0].dstStageMask = VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT; // 目标阶段掩码：早期和晚期片段测试阶段
10dependencies[0].srcAccessMask = VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT; // 源访问掩码：深度模板附件写入
11dependencies[0].dstAccessMask = VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT; // 目标访问掩码：深度模板附件写入和读取
12dependencies[0].dependencyFlags = 0; // 无额外依赖标志
13
14// 第二个依赖关系
15dependencies[1].srcSubpass = VK_SUBPASS_EXTERNAL; // 外部到第一个子通道的依赖
16dependencies[1].dstSubpass = 0; // 目标子通道索引为0，即第一个子通道
17dependencies[1].srcStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT; // 源阶段掩码：颜色附件输出阶段
18dependencies[1].dstStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT; // 目标阶段掩码：颜色附件输出阶段
19dependencies[1].srcAccessMask = 0; // 源访问掩码：无特定访问类型
20dependencies[1].dstAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK_ACCESS_COLOR_ATTACHMENT_READ_BIT; // 目标访问掩码：颜色附件写入和读取
21dependencies[1].dependencyFlags = 0; // 无额外依赖标志
```

#### 4. 创建 RenderPass 对象

```
 1// 创建 RenderPass
 2VkAttachmentDescription attachments[] = { colorAttachment, depthAttachment }; // 定义附件数组
 3
 4VkRenderPassCreateInfo renderPassInfo = {};
 5renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
 6renderPassInfo.attachmentCount = 2; // 附件数量
 7renderPassInfo.pAttachments = attachments; // 附件描述
 8renderPassInfo.subpassCount = 1; // 子通道数量
 9renderPassInfo.pSubpasses = &subpass; // 子通道描述
10renderPassInfo.dependencyCount = 2; // 子通道依赖数量
11renderPassInfo.pDependencies = &dependencies; // 子通道依赖描述
12
13VkRenderPass renderPass;
14if (vkCreateRenderPass(device, &renderPassInfo, nullptr, &renderPass) != VK_SUCCESS) {
15    throw std::runtime_error("failed to create render pass!");
16}
```

#### 5.销毁 RenderPass 对象

```
1vkDestroyRenderPass(device, renderPass, nullptr);
```

