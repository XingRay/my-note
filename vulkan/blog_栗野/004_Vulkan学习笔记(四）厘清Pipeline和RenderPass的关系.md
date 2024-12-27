# Vulkan学习笔记(四）厘清Pipeline和RenderPass的关系

接上文：

[栗野：Vulkan学习笔记(三）利用CMake在项目中编译配置Shader6 赞同 · 0 评论文章![img](./assets/v2-fef9860b499784cd7df9c5055ca9fdac_180x120-1735288570462-411.jpg)](https://zhuanlan.zhihu.com/p/539561176)

### 渲染步骤与[渲染目标](https://zhida.zhihu.com/search?content_id=208070767&content_type=Article&match_order=1&q=渲染目标&zhida_source=entity)

### RenderPass 渲染步骤

RenderPass这个概念在之前学习Unity Shader的时候就有所接触。Unity中一个shader的某一个SubShader中会有一个或是多个pass。对于每一个pass，都可设置这一个pass的**[着色器代码](https://zhida.zhihu.com/search?content_id=208070767&content_type=Article&match_order=1&q=着色器代码&zhida_source=entity)**和**管线固定功能**包括剔除、Blending、ColorMask等等。实际上在Vulkan中，RenderPass(渲染步骤)和Subpass(子步骤)这两者的设计彻底明确了pass的概念，可以让我们窥探所谓游戏渲染中pass设计的一二。

上文中我们提到，每一套ShaderModule需要绑定到一条图形管线才能发挥作用。而在一帧渲染流程中，RenderPass或准确的说**一个subpass对应了一套图形管线状态下的一次渲染**。图形管线的作用就是描述一套pass流程中流程的所有状态。Vulkan中管线叫做`VkGraphicsPipeline`，而DX12中的管线状态对象`PipelineStateObject`的名字某种程度也更好地反应了管线的本质。

在图形管线创建的代码中也有体现，每一个pipeline创建的时候都需要指定一个RenderPass和它的Subpass的索引：

```cpp
pipelineCreateInfo.renderPass = m_renderPass;
pipelineCreateInfo.subpass = 0; // 子流程数组中的索引
```

### Attachment 渲染目标

而Vulkan所谓的Attachment也即DX12的RenderTarget，是一次渲染步骤执行时写入的渲染目标。这个目标可能只需要Color，也有可能需要Depth或是Stencil等等。而在[延迟渲染](https://zhida.zhihu.com/search?content_id=208070767&content_type=Article&match_order=1&q=延迟渲染&zhida_source=entity)管线中，甚至可能需要多个Attachment来供一个RenderPass输出。

```cpp
VkAttachmentDescription colorAttachment = {};
colorAttachment.format = m_swapChainImageFormat;
colorAttachment.samples = VK_SAMPLE_COUNT_1_BIT;
// 颜色和深度缓冲的存取策略
colorAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
colorAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
// stencil缓冲的存取策略
colorAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
colorAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
colorAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
colorAttachment.finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;  // 在内存中的分布方式为用作呈现方式
```

实际上RenderPass和Attachment的提出解决了移动端的GPU架构使用的硬件渲染器（TBDR）片上缓存的问题。对于TBDR渲染器面对多个pass而言，某些pass的渲染结果是直接被下一个Subpass使用而不需要写回显存在FrameBuffer中展示出来的，因此存储在缓存之中直接被下一个pass读取是最优性能的策略，那么就可以设置上一个pass的attachment的`storeOp`为`VK_ATTACHMENT_STORE_OP_DONT_CARE`；对于某些Subpass不需要考虑之前的pass的结果，那么就可以设置attachment的`loadOp`为`VK_ATTACHMENT_LOAD_OP_DONT_CARE`。

### Subpass子步骤

Subpass相当于一个大车间中的两个流水线。当两个流水线的输入输出能够无缝衔接时，直接把两个流水线放在同一个大车间即可，而不必分别放到两个车间中。每一个Subpass都会有一个Attachment的引用，并且指定指定其布局。这个编号也就是[片元着色器](https://zhida.zhihu.com/search?content_id=208070767&content_type=Article&match_order=1&q=片元着色器&zhida_source=entity)中最终指定的输出`layout(location = 0) out vec4 outColor`的location编号。

```cpp
// 附着引用
VkAttachmentReference colorAttachReference = {};
colorAttachReference.attachment = 0;    // 表示description在数组中的引用索引，也是shader中片元最终输出时layout (location = 0)的索引
colorAttachReference.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
// 渲染流程可能包含多个子流程，其依赖于上一流程处理后的帧缓冲内容
VkSubpassDescription subpass = {};
subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;    // 这是一个图形渲染的子流程
subpass.colorAttachmentCount = 1;
subpass.pColorAttachments = &colorAttachReference;  // 颜色帧缓冲附着会被在frag中使用作为输出
```

### 子流程依赖

上文中提到多个子流程之间存在顺序关系，那么就需要描述他们在执行时的依赖关系。

```cpp
// 子流程依赖
VkSubpassDependency dependency = {};
dependency.srcSubpass = VK_SUBPASS_EXTERNAL;//隐含的子流程
dependency.dstSubpass = 0; 
dependency.srcStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT; // 需要等待交换链读取完图像
dependency.srcAccessMask = 0;
dependency.dstStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT; // 为等待颜色附着的输出阶段
dependency.dstAccessMask = VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
```

`srcStageMask`和`srcAccessMask`用于指定需要等待的管线阶段 和子流程将进行的操作类型。我们需要等待交换链结束对图像的读取才能对图像进行访问操作，也就是等待颜色附着输出这一管线阶段。`dstStageMask`和`dstAccessMask`用于指定需要等待的管线阶段和子流程将进行的操作类型。在这里，我们的设置为等待颜色附着的输出阶段，子流程将会进行颜色附着的读写操作。这样设置后，图像布局变换直到必要时才会进行。

### 设置渲染步骤

```cpp
// 渲染流程
VkRenderPassCreateInfo renderPassCreateInfo = {};
renderPassCreateInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
renderPassCreateInfo.attachmentCount = 1;
renderPassCreateInfo.pAttachments = &colorAttachment;
renderPassCreateInfo.subpassCount = 1;
renderPassCreateInfo.pSubpasses = &subpass;
renderPassCreateInfo.dependencyCount = 1;
renderPassCreateInfo.pDependencies = &dependency;
VkResult res = vkCreateRenderPass(m_device, &renderPassCreateInfo, nullptr, &m_renderPass);
if (res != VK_SUCCESS) {
    throw std::runtime_error("failed to create render pass!");
}
```

### 图形管线

在理解了图形管线和渲染步骤之间的关系之后，配置图形管线的繁杂的参数反而不是什么困难的问题了。我们遵循一下的配置流程：

```text
flowchart LR
ShaderModule --> VertexInput --> InputAssembly --> viewport --> sccissor --> Rasterization--> MultiSample --> DepthStencil --> ColorBlend --> dynamicState --> pipelineLayout --> renderPass
```

这不免是一串又臭又长的配置代码，具体的细节在注释中给出。

```cpp
VkResult res;
VkShaderModule vertexShaderModule = createShaderModule(VERTEX_VERT);
VkShaderModule fragmentShaderModule = createShaderModule(FRAGMENT_FRAG);
// 指定着色器在管线的阶段
VkPipelineShaderStageCreateInfo vertStageCreateInfo = {};
vertStageCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
vertStageCreateInfo.pName = "main";
vertStageCreateInfo.module = vertexShaderModule;
vertStageCreateInfo.stage = VK_SHADER_STAGE_VERTEX_BIT;

VkPipelineShaderStageCreateInfo fragStageCreateInfo = {};
fragStageCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
fragStageCreateInfo.pName = "main";
fragStageCreateInfo.module = fragmentShaderModule;
fragStageCreateInfo.stage = VK_SHADER_STAGE_FRAGMENT_BIT;

VkPipelineShaderStageCreateInfo shaderStages[2] = {
    vertStageCreateInfo, fragStageCreateInfo
};

// 获取顶点的绑定信息和属性信息
auto bindDesc = Vertex::getBindDescription();
auto attrDesc = Vertex::getAttributeDescriptions();
// 顶点输入
VkPipelineVertexInputStateCreateInfo vertexInputCreateInfo = {};
vertexInputCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
vertexInputCreateInfo.vertexBindingDescriptionCount = 1;
vertexInputCreateInfo.pVertexBindingDescriptions = &bindDesc;
vertexInputCreateInfo.vertexAttributeDescriptionCount = static_cast<uint32_t>(attrDesc.size());
vertexInputCreateInfo.pVertexAttributeDescriptions = attrDesc.data();

// 顶点输入装配
VkPipelineInputAssemblyStateCreateInfo vertexAssemblyCreateInfo = {};
vertexAssemblyCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
vertexAssemblyCreateInfo.topology = VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
vertexAssemblyCreateInfo.primitiveRestartEnable = VK_FALSE; // 是否在与STRIP图元重启索引绘制

//视口与剪裁,视口定义的是映射关系，剪裁是定义的显示区域
VkViewport viewport = {};
viewport.x = 0.0f;
viewport.y = 0.0f;
viewport.width = (float)m_swapChainImageExtent.width;
viewport.height = (float)m_swapChainImageExtent.height;
viewport.maxDepth = 1.0f;
viewport.minDepth = 0.0f;

VkRect2D scissor = {};
scissor.offset = { 0, 0 };
scissor.extent = m_swapChainImageExtent;

VkPipelineViewportStateCreateInfo viewportCreateInfo = {};
viewportCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
viewportCreateInfo.viewportCount = 1;
viewportCreateInfo.pViewports = &viewport;
viewportCreateInfo.scissorCount = 1;
viewportCreateInfo.pScissors = &scissor;

// 光栅化阶段，除了fill mode外其他光栅化方式也需要GPU特性
VkPipelineRasterizationStateCreateInfo rasterizationCreateInfo = {};
rasterizationCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
rasterizationCreateInfo.depthClampEnable = VK_FALSE;    //是否截断片元在远近平面上
rasterizationCreateInfo.rasterizerDiscardEnable = VK_FALSE;
rasterizationCreateInfo.lineWidth = 1.0f;
rasterizationCreateInfo.cullMode = VK_CULL_MODE_BACK_BIT;       // 剔除背面
rasterizationCreateInfo.frontFace = VK_FRONT_FACE_CLOCKWISE;    // 顺时针顶点序为正面
rasterizationCreateInfo.depthBiasEnable = VK_FALSE; // 阴影贴图的 alias
rasterizationCreateInfo.depthBiasConstantFactor = 0.0f;
rasterizationCreateInfo.depthBiasClamp = 0.0f;
rasterizationCreateInfo.depthBiasSlopeFactor = 0.0f;

// 多重采样，启用需要GPU特性
VkPipelineMultisampleStateCreateInfo multisampleCreateInfo = {};
multisampleCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
multisampleCreateInfo.sampleShadingEnable = VK_FALSE;
multisampleCreateInfo.rasterizationSamples = VK_SAMPLE_COUNT_1_BIT;
multisampleCreateInfo.minSampleShading = 1.0f;
multisampleCreateInfo.pSampleMask = nullptr;
multisampleCreateInfo.alphaToCoverageEnable = VK_FALSE;
multisampleCreateInfo.alphaToOneEnable = VK_FALSE;

// 深度与模板测试,这里没有就不设置了
// VkPipelineDepthStencilStateCreateInfo depthCreateInfo = {};

// 颜色混合
VkPipelineColorBlendStateCreateInfo colorBlendCreateInfo = {};  // 全局的帧缓冲设置
VkPipelineColorBlendAttachmentState colorBlendAttachment = {};  // 单独的帧缓冲设置
colorBlendAttachment.colorWriteMask =
    VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT |
    VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT;
// 下面的帧缓冲设置实现是通过alpha混合实现半透明效果
colorBlendAttachment.blendEnable = VK_TRUE;
colorBlendAttachment.srcColorBlendFactor = VK_BLEND_FACTOR_SRC_ALPHA;
colorBlendAttachment.dstColorBlendFactor = VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
colorBlendAttachment.colorBlendOp = VK_BLEND_OP_ADD;
colorBlendAttachment.srcAlphaBlendFactor = VK_BLEND_FACTOR_ONE;
colorBlendAttachment.dstAlphaBlendFactor = VK_BLEND_FACTOR_ZERO;
colorBlendAttachment.alphaBlendOp = VK_BLEND_OP_ADD;
// 全局帧缓冲设置
colorBlendCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
colorBlendCreateInfo.logicOpEnable = VK_FALSE;  // 会禁用所有混合设置
colorBlendCreateInfo.logicOp = VK_LOGIC_OP_COPY;
colorBlendCreateInfo.attachmentCount = 1;
colorBlendCreateInfo.pAttachments = &colorBlendAttachment;  // 添加单独帧缓冲设置
colorBlendCreateInfo.blendConstants[0] = 0.0f;
colorBlendCreateInfo.blendConstants[1] = 0.0f;
colorBlendCreateInfo.blendConstants[2] = 0.0f;
colorBlendCreateInfo.blendConstants[3] = 0.0f;

//动态状态
VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = {};
VkDynamicState dynamicStates[2] = {
    VK_DYNAMIC_STATE_VIEWPORT,
    VK_DYNAMIC_STATE_SCISSOR
};
dynamicStateCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;
dynamicStateCreateInfo.dynamicStateCount = 2;
dynamicStateCreateInfo.pDynamicStates = dynamicStates;

// 管线布局
VkPipelineLayoutCreateInfo layoutCreateInfo = {};
layoutCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
layoutCreateInfo.setLayoutCount = 0;
layoutCreateInfo.pSetLayouts = nullptr;
layoutCreateInfo.pushConstantRangeCount = 0;
layoutCreateInfo.pPushConstantRanges = nullptr;
res = vkCreatePipelineLayout(m_device, &layoutCreateInfo, nullptr, &m_pipelineLayout);
if (res != VK_SUCCESS) {
    throw std::runtime_error("failed to create pipeline layout!");
}

VkGraphicsPipelineCreateInfo pipelineCreateInfo = {};
pipelineCreateInfo.sType = VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
pipelineCreateInfo.stageCount = 2;
pipelineCreateInfo.pStages = shaderStages;  // 指定着色器阶段

pipelineCreateInfo.pVertexInputState = &vertexInputCreateInfo;  // 指定输入
pipelineCreateInfo.pInputAssemblyState = &vertexAssemblyCreateInfo; // 指定顶点装配
pipelineCreateInfo.pViewportState = &viewportCreateInfo;    // 指定视口
pipelineCreateInfo.pRasterizationState = &rasterizationCreateInfo; // 指定光栅器设置
pipelineCreateInfo.pMultisampleState = &multisampleCreateInfo; // 指定多重采样设置
pipelineCreateInfo.pDepthStencilState = nullptr;    // 指定深度模板缓冲
pipelineCreateInfo.pColorBlendState = &colorBlendCreateInfo;
pipelineCreateInfo.pDynamicState = &dynamicStateCreateInfo;
pipelineCreateInfo.layout = m_pipelineLayout;

pipelineCreateInfo.renderPass = m_renderPass;
pipelineCreateInfo.subpass = 0; // 子流程数组中的索引

pipelineCreateInfo.basePipelineHandle = VK_NULL_HANDLE; // 通过已有管线创造新的管线
pipelineCreateInfo.basePipelineIndex = -1;

res = vkCreateGraphicsPipelines(m_device, VK_NULL_HANDLE, 1, &pipelineCreateInfo, nullptr, &m_graphicsPipeline);
// 其中pipelinecache 是管线缓存对象，加速管线创立
if (res != VK_SUCCESS) {
    throw std::runtime_error("failed to create graphics pipeline!");
}
// 销毁创建的shaderModule
vkDestroyShaderModule(m_device, vertexShaderModule, nullptr);
vkDestroyShaderModule(m_device, fragmentShaderModule, nullptr);
```

值得注意的是，虽然对于一套管线在运行时不允许更改状态，但是部分的参数可以通过dynamicState的调整改变管线的设置。比如我们允许了`VK_DYNAMIC_STATE_VIEWPORT/VK_DYNAMIC_STATE_SCISSOR`的改变，那么最终使用`VkCmd_`开头的指令就可以改变对应的状态。如下：

```cpp
VkViewport viewport{};
// ***
vkCmdSetViewport(commandBuffer, 0, 1, &viewport);

VkRect2D scissor{};
// ***
vkCmdSetScissor(commandBuffer, 0, 1, &scissor);
```

下表中给出了可以动态更改的一些状态：

```cpp
VK_DYNAMIC_STATE_VIEWPORT = 0,
VK_DYNAMIC_STATE_SCISSOR = 1,
VK_DYNAMIC_STATE_LINE_WIDTH = 2,
VK_DYNAMIC_STATE_DEPTH_BIAS = 3,
VK_DYNAMIC_STATE_BLEND_CONSTANTS = 4,
VK_DYNAMIC_STATE_DEPTH_BOUNDS = 5,
VK_DYNAMIC_STATE_STENCIL_COMPARE_MASK = 6,
VK_DYNAMIC_STATE_STENCIL_WRITE_MASK = 7,
VK_DYNAMIC_STATE_STENCIL_REFERENCE = 8,
```

对于某些场景中渲染规则复杂的物体可能需要创建很多的管线，Vulkan还提供了两种方式复用管线，包括Pipeline Cache和Pipeline Derivatization，这些就是后话了。

