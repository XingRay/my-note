# Vulkan从入门到精通39-次级命令缓冲

Vulkan Command Buffer 用于记录一些vulkan API的调用命令序列，比如绘制操作和内存传输。然后这些调用序列就可以提交给硬件执行。提交操作可以以多线程方式进行。

Vulkan提供二个层次的Command Buffer。一个是主命令缓冲 - 他可以执行需要提交给命令队列的次级命令缓冲； 二是次级命令缓冲 - 他不可以直接被提交给命令队列。

------

使用次级命令缓冲的主要优势有二点。一是次级命令缓冲可以并行分配和记录操作命令,这意味着可以充分发挥硬件优势。二是辅助命令缓冲区的生存期可以彼此独立管理，也就是可以混合使用长寿命或永久性的次级命令缓冲，这些缓冲区与频繁更新的辅助[命令缓冲区](https://zhida.zhihu.com/search?content_id=206716090&content_type=Article&match_order=2&q=命令缓冲区&zhida_source=entity)配合使用，从而减少创建程序每帧所需的命令缓冲区数量

尽管以上二大优势主命令缓冲也有，但是多个后者不能在同一个渲染通道中执行。也就是如果打算在同一帧执行多个主命令缓冲，每个主命令缓冲就需要以vkCmdBeginRenderPass开始，以vvkCmdEndRenderPass 结束。

这听起来可能没什么大不了的，但开始渲染过程实例可能是一个相当繁重的操作，并且需要在每帧中多次执行此操作可能会破坏某些硬件上的性能。辅助命令缓冲区通过能够从调用它的主命令缓冲区继承渲染过程实例以及其他状态来避免此问题。

------

总而言之，次级命令缓冲有二个优点

1. 支持以多线程模式执行，提高了程序运行效率
2. 允许以更为精细的方式控制渲染细节。

------

下面是使用次级命令缓冲的方式

```cpp
    class VK_SecondaryCommandBuffer : public VK_Deleter
    {
    public:
        VK_SecondaryCommandBuffer(VK_Context* vkContext, VkCommandPool pool);
        ~VK_SecondaryCommandBuffer();
    public:
        bool create(uint32_t count);
        VkCommandBuffer at(uint32_t index);
        void executeCommandBuffer(VkCommandBuffer command, VkFramebuffer frameBuffer);
        void release() override;
    private:
        VK_Context* context = nullptr;
        VkCommandPool commandPool = nullptr;
        std::vector<VkCommandBuffer> buffers;
    };
```

对应实现如下

```cpp
    #include <VK_SecondaryCommandBuffer.h>
    #include <VK_Context.h>
    VK_SecondaryCommandBuffer::VK_SecondaryCommandBuffer(VK_Context *vkContext, VkCommandPool pool):
        context(vkContext),
        commandPool(pool)
    {
    }
    VK_SecondaryCommandBuffer::~VK_SecondaryCommandBuffer()
    {
    }
    bool VK_SecondaryCommandBuffer::create(uint32_t count)
    {
        VkCommandBufferAllocateInfo cmdAlloc = {};
        cmdAlloc.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
        cmdAlloc.pNext = NULL;
        cmdAlloc.commandPool = commandPool;
        cmdAlloc.level = VK_COMMAND_BUFFER_LEVEL_SECONDARY;
        cmdAlloc.commandBufferCount = count;
        buffers.resize(count);
        return vkAllocateCommandBuffers(context->getDevice(), &cmdAlloc, buffers.data()) == VK_SUCCESS;
    }
    VkCommandBuffer VK_SecondaryCommandBuffer::at(uint32_t index)
    {
        return buffers.at(index);
    }
    void VK_SecondaryCommandBuffer::executeCommandBuffer(VkCommandBuffer command,
                                                         VkFramebuffer frameBuffer)
    {
        VkRenderPassBeginInfo rpBegin;
        rpBegin.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
        rpBegin.pNext = NULL;
        rpBegin.renderPass = context->getRenderPass();
        rpBegin.framebuffer = frameBuffer;
        rpBegin.renderArea.offset.x = 0;
        rpBegin.renderArea.offset.y = 0;
        rpBegin.renderArea.extent.width = context->getSwapChainExtent().width;
        rpBegin.renderArea.extent.height = context->getSwapChainExtent().height;
        rpBegin.clearValueCount = 1;
        VkClearValue cv[2] = {};
        cv[1].depthStencil = {1.0f, 0};
        rpBegin.pClearValues = cv;
        vkCmdBeginRenderPass(command, &rpBegin, VK_SUBPASS_CONTENTS_SECONDARY_COMMAND_BUFFERS);
        vkCmdExecuteCommands(command, buffers.size(), buffers.data());
        vkCmdEndRenderPass(command);
    }
    void VK_SecondaryCommandBuffer::release()
    {
        vkFreeCommandBuffers(context->getDevice(), commandPool, buffers.size(), buffers.data());
        buffers.clear();
        delete this;
    }
```

同时VK_CommandPool增加VK_SecondaryCommandBuffer* createSecondaryCommand(uint32_t count);接口

实现为

```cpp
    VK_SecondaryCommandBuffer *VK_CommandPool::createSecondaryCommand(uint32_t count)
    {
        auto commandBuffer = new VK_SecondaryCommandBuffer(context, pool);
        commandBuffer->create(count);
        return commandBuffer;
    }
```

增加VK_SecondaryCommandBufferCallback 用于设置命令记录绘制函数

```cpp
    class VK_SecondaryCommandBufferCallback
    {
    public:
        virtual void execute(VK_Context *context, VkCommandBuffer commandBuffer, uint32_t current,
                             uint32_t total) = 0;
    };
```

VK_Context增加函数接口和实现如下

```cpp
    bool VK_ContextImpl::createSecondaryCommandBuffer(uint32_t secondaryCommandBufferCount,
                                                      std::shared_ptr<VK_SecondaryCommandBufferCallback> caller)
    {
        if (secondaryCommandBufferCount == 0)
            return false;
        this->secondaryCommandBufferCount = secondaryCommandBufferCount;
        secondaryCommandBufferCaller = caller;
        secondaryCommandBuffers.resize(swapChainFramebuffers.size());
        for (uint32_t i = 0; i < swapChainFramebuffers.size(); i++) {
            secondaryCommandBuffers[i] = new VK_SecondaryCommandBuffer(this,
                                                                       getCommandPool()->getCommandPool());
            secondaryCommandBuffers[i]->create(secondaryCommandBufferCount);
            VkCommandBufferInheritanceInfo info = {};
            info.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_INFO,
            info.pNext = NULL;
            info.renderPass = getRenderPass();
            info.subpass = 0;
            info.framebuffer = swapChainFramebuffers[i];
            info.occlusionQueryEnable = VK_FALSE;
            info.queryFlags = 0;
            info.pipelineStatistics = 0;
            VkCommandBufferBeginInfo secondaryCommandBufferBeginInfo = {};
            secondaryCommandBufferBeginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
            secondaryCommandBufferBeginInfo.pNext = NULL;
            secondaryCommandBufferBeginInfo.flags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT |
                                                    VK_COMMAND_BUFFER_USAGE_RENDER_PASS_CONTINUE_BIT;
            secondaryCommandBufferBeginInfo.pInheritanceInfo = &info;
            for (uint32_t j = 0; j < secondaryCommandBufferCount; j++) {
                auto commandBuffer = secondaryCommandBuffers[i]->at(j);
                vkBeginCommandBuffer(commandBuffer, &secondaryCommandBufferBeginInfo);
                for (auto pipeline : pipelines) {
                    pipeline->render(commandBuffer, i, caller, j, secondaryCommandBufferCount);
                }
                vkEndCommandBuffer(commandBuffer);
            }
        }
        return true;
    }
```

在VK_ContextImpl修改createCommadBuffers函数

修改处为在vkBeginCommandBuffer和vkEndCommandBuffer之间增加

```cpp
    if (!secondaryCommandBuffers.empty()) {
                auto current = secondaryCommandBuffers.at(i);
                current->executeCommandBuffer(commandBuffers[i], swapChainFramebuffers.at(i));
            } else 
```

如果次级命令缓冲不为空则执行调用他，否则调用主命令缓冲

------

样例 - 在之前的三角形绘制例子上增加以下代码

```cpp
    #define COMMAND_BUFFER_COUNT 64
    class ThisSecondaryCommandBufferCallback : public VK_SecondaryCommandBufferCallback
    {
        // VK_SecondaryCommandBufferCallback interface
    public:
        void execute(VK_Context *context, VkCommandBuffer commandBuffer, uint32_t current,
                     uint32_t total) override
        {
            auto width = context->getSwapChainExtent().width;
            auto height = context->getSwapChainExtent().height;
            int n = sqrt(COMMAND_BUFFER_COUNT);
            VkViewport viewport;
            viewport.minDepth = 0.0f;
            viewport.maxDepth = 1.0f;
            viewport.x = (current % n) * width / n;
            viewport.y = (current / n) * height / n;
            viewport.width = width / n;
            viewport.height = height / n;
            vkCmdSetViewport(commandBuffer, 0, 1, &viewport);
        }
    };
```

然后在pipeline->addRenderBuffer后，context->createCommandBuffers前增加以下代码

```cpp
        std::shared_ptr<ThisSecondaryCommandBufferCallback> caller =
            std::make_shared<ThisSecondaryCommandBufferCallback>();
        context->createSecondaryCommandBuffer(COMMAND_BUFFER_COUNT, caller);
```

------

对应渲染效果见封面

\-----

备注 - 截至当前有2个用例很能明显反映出显卡性能，一个是之前的多实例渲染，一个是本用例。前者一般显卡只能渲染几个实例，本例一般显卡只能渲染几十个。如果显卡能力强悍，后者可以支持上千个次级命令缓冲的执行。