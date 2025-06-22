# Vulkan从入门到精通25-代码重构

写到vulkan第25篇，vulkan已经入门，但是回顾之前的代码有种种问题。一个vulkan程序可以有多个pipeline，但是之前的框架里只使用了用了一个pipeline。对于一个大中型程序，可能有多组shader，比如一组shader用于渲染草地，一组shader用于渲染牛羊；或者一个pipeline渲染填充模型，一个pipelien渲染线框模型。对于这种略为复杂点的程序，之前的框架无能为力，只能重构框架。

重构的一点是把pipleline和创建pileline的createInfo，以及相关createinfo抽成一个单独的VK_Pipleline，同时把dynamicState移入pipeline对象。同时一个pipleline需要指定要渲染的buffer,所以需要增加一个渲染Buffer的函数。

VK_Pipleline接口如下

```cpp
class VK_Pipeline : public VK_Deleter
{
public:
    virtual void setVertexInputStateCreateInfo(const VkPipelineVertexInputStateCreateInfo& createInfo) = 0;
    virtual VkPipelineVertexInputStateCreateInfo getVertexInputStateCreateInfo()const = 0;

    virtual void setInputAssemblyStateCreateInfo(const VkPipelineInputAssemblyStateCreateInfo &createInfo) = 0;
    virtual VkPipelineInputAssemblyStateCreateInfo getInputAssemblyStateCreateInfo()const = 0;

    virtual void setRasterizationStateCreateInfo(const VkPipelineRasterizationStateCreateInfo &createInfo) = 0;
    virtual VkPipelineRasterizationStateCreateInfo getRasterizationStateCreateInfo()const = 0;

    virtual VkPipelineDepthStencilStateCreateInfo getDepthStencilStateCreateInfo()const = 0;
    virtual void setDepthStencilStateCreateInfo(const VkPipelineDepthStencilStateCreateInfo &createInfo) = 0;

    static VkPipelineTessellationStateCreateInfo createPipelineTessellationStateCreateInfo(uint32_t patch);
    virtual void setTessellationStateCreateInfo(const VkPipelineTessellationStateCreateInfo &createInfo) = 0;
    virtual VkPipelineTessellationStateCreateInfo getTessellationStateCreateInfo() = 0;

    virtual void setViewports(const VK_Viewports& viewports) = 0;
    virtual VK_Viewports getViewports()const = 0;

    virtual void setMultisampleStateCreateInfo(const VkPipelineMultisampleStateCreateInfo& createInfo) = 0;
    virtual VkPipelineMultisampleStateCreateInfo getMultisampleStateCreateInfo()const = 0;

    virtual void setColorBlendStateCreateInfo(const VkPipelineColorBlendStateCreateInfo& createInfo) = 0;
    virtual VkPipelineColorBlendStateCreateInfo getColorBlendStateCreateInfo()const = 0;

    virtual VK_DynamicState* getDynamicState()const = 0;
public:
    virtual bool create() = 0;
    virtual void addRenderBuffer(VK_Buffer* buffer) = 0;
    virtual VK_Pipeline* fork(VK_ShaderSet* shaderSet = nullptr) = 0;
    virtual bool needRecreate()const = 0;
};
```

create函数

```cpp
bool VK_PipelineImpl::create()
{
    for(auto child : chilren)
        child->release();
    chilren.clear();

    pipelineCreateInfo.sType = VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
    pipelineCreateInfo.flags = VK_PIPELINE_CREATE_ALLOW_DERIVATIVES_BIT;

    {
        VK_ShaderSet* shader = shaderSet;

        if(!shader) {
            assert(parent);
            shader = parent->getShaderSet();
        }

        pipelineCreateInfo.stageCount = shader->getCreateInfoCount();
        pipelineCreateInfo.pStages = shader->getCreateInfoData();
    }

    auto vertexInputStateCreateInfo = getVertexInputStateCreateInfo();
    pipelineCreateInfo.pVertexInputState = &vertexInputStateCreateInfo;

    auto inputAssemblyStateCreateInfo = getInputAssemblyStateCreateInfo();
    pipelineCreateInfo.pInputAssemblyState = &inputAssemblyStateCreateInfo;

    VkPipelineViewportStateCreateInfo viewportState{};
    viewportState.sType = VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;

    auto viewports = getViewports();
    viewportState.viewportCount = viewports.getViewportCount();
    viewportState.pViewports = viewports.getViewportData();
    viewportState.scissorCount = viewports.getViewportCount();
    viewportState.pScissors = viewports.getScissorData();
    pipelineCreateInfo.pViewportState = &viewportState;

    auto rasterizationStateCreateInfo = getRasterizationStateCreateInfo();
    rasterizationStateCreateInfo.cullMode = VK_CULL_MODE_NONE;
    pipelineCreateInfo.pRasterizationState = &rasterizationStateCreateInfo;

    auto depthStencilStateCreateInfo = getDepthStencilStateCreateInfo();
    pipelineCreateInfo.pDepthStencilState = &depthStencilStateCreateInfo;

    auto multiSampleStateCreateInfo = getMultisampleStateCreateInfo();
    pipelineCreateInfo.pMultisampleState = &multiSampleStateCreateInfo;

    auto colorBlendStateCreateInfo = getColorBlendStateCreateInfo();
    pipelineCreateInfo.pColorBlendState = &colorBlendStateCreateInfo;

    if (!tessellationStateCreateInfo.has_value())
        pipelineCreateInfo.pTessellationState = nullptr;
    else
        pipelineCreateInfo.pTessellationState = &tessellationStateCreateInfo.value();

    auto dynamicState = getDynamicState()->createDynamicStateCreateInfo(0);
    pipelineCreateInfo.pDynamicState = &dynamicState;
    pipelineCreateInfo.layout = context->getPipelineLayout();
    pipelineCreateInfo.renderPass = context->getRenderPass();
    pipelineCreateInfo.subpass = 0;
    pipelineCreateInfo.basePipelineHandle = VK_NULL_HANDLE;
    pipelineCreateInfo.pNext = nullptr;

    if (vkCreateGraphicsPipelines(context->getDevice(), context->getPipelineCache()->getPipelineCache(), 1, &pipelineCreateInfo, context->getAllocation(),
                                  &pipeline) != VK_SUCCESS) {
        std::cerr << "failed to create graphics pipeline!" << std::endl;
        return false;
    }

    needUpdate = false;
    return true;
}
```

注意flag字段是 *VK_PIPELINE_CREATE_ALLOW_DERIVATIVES_BIT 允许从此管线派生管线*

*[渲染管线](https://zhida.zhihu.com/search?content_id=189797317&content_type=Article&match_order=1&q=渲染管线&zhida_source=entity)时调用函数render*

```cpp
void VK_PipelineImpl::render(VkCommandBuffer command)
{
    vkDynamicState->apply(command);
    vkCmdBindPipeline(command, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);

    for(auto buffer : buffers)
        buffer->render(command);
```

相关对外接口也做了一定调整

之前是context->initPipeline()

现在修改成了

```cpp
pipeline = context->createPipeline();
pipeline->create()
```

先创建一个VK_Pipeline实例，在create函数中调用vkCreateGraphicsPipelines创建管线

其他几处修改是把generateMipmaps，copyBufferToImage和transitionImageLayout从context移入VK_Image。这几个函数都是处理图形的，移入后者比较合适

最后一处修改是移除掉VK_Viewport，所有视口设置全部通过VkCmdSetViewport来完成。

在创建管线时这样调用

```cpp
    pipeline = context->createPipeline();
    pipeline->getDynamicState()->addDynamicState(VK_DYNAMIC_STATE_VIEWPORT);
    pipeline->create();
    pipeline->getDynamicState()->applyDynamicViewport({0, 0, 480, 480, 0, 1});
```

窗口大小改变时或需要重设视口时这样操作

```cpp
     pipeline->getDynamicState()->applyDynamicViewport({0, 0, (float)width, (float)height, 0, 1});
```

