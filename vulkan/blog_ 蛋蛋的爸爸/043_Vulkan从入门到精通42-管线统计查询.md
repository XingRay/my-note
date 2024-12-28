# Vulkan从入门到精通42-管线统计查询

使用Vulkan可以拿到当前渲染目标的一些相关信息，比如顶点个数，图元个数，顶点着色调用次数，片段调用次数等等。

如何使用能？

1. 创建(逻辑)设备前开启管线统计查询

```cpp
    VkPhysicalDeviceFeatures deviceFeatures{};
    deviceFeatures.tessellationShader = VK_TRUE;
    deviceFeatures.fillModeNonSolid = VK_TRUE;
    deviceFeatures.pipelineStatisticsQuery = VK_TRUE;
    context->setLogicalDeviceFeatures(deviceFeatures);
```

vulkan比较奇怪，有些例子，不需要开启此属性可以正常工作，有些例子不开启就会出错，报出需要开启pipelineStatisticsQuery的错误提示。

2. 管线统计查询都是基于VkQueryPool对象的，涉及创建，重置，开启查询，结束查询和销毁。

我把它封装成了一个类，如下

```cpp
class VK_QueryPoolImpl : public VK_QueryPool
{
public:
    VK_QueryPoolImpl() = delete;
    VK_QueryPoolImpl(VK_Context *inputContext, uint32_t count, VkQueryPipelineStatisticFlags flag);
public:
    void release() override;
    void reset(VkCommandBuffer commandBuffer) override;
    void startQeury(VkCommandBuffer commandBuffer) override;
    void endQuery(VkCommandBuffer commandBuffer) override;
    void setQueryCallback(std::function<void(const std::vector<uint64_t>& data)> fn) override;

    void query();
private:
    VK_Context *context = nullptr;
    VkQueryPool queryPool = nullptr;
    std::vector<uint64_t> queryData;
    std::function<void(const std::vector<uint64_t>& data)> queryCallback;
};
```

实现

```cpp
#include <VK_QueryPoolImpl.h>


VK_QueryPoolImpl::VK_QueryPoolImpl(VK_Context *inputContext,
                                   uint32_t count, VkQueryPipelineStatisticFlags flag):
    context(inputContext)
{
    VkQueryPoolCreateInfo queryPoolCreateInfo{};
    queryPoolCreateInfo.sType = VK_STRUCTURE_TYPE_QUERY_POOL_CREATE_INFO;
    queryPoolCreateInfo.queryType  = VK_QUERY_TYPE_PIPELINE_STATISTICS;
    queryPoolCreateInfo.queryCount = count;
    queryPoolCreateInfo.pipelineStatistics = flag;
    auto status = vkCreateQueryPool(context->getDevice(), &queryPoolCreateInfo,
                                    context->getAllocation(),
                                    &queryPool);
    assert(status == VK_SUCCESS);


    queryData.resize(count);
}


void VK_QueryPoolImpl::release()
{
    vkDestroyQueryPool(context->getDevice(), queryPool, context->getAllocation());
}


void VK_QueryPoolImpl::reset(VkCommandBuffer commandBuffer)
{
    vkCmdResetQueryPool(commandBuffer, queryPool, 0, queryData.size());
}


void VK_QueryPoolImpl::startQeury(VkCommandBuffer commandBuffer)
{
    vkCmdBeginQuery(commandBuffer, queryPool, 0, 0);
}


void VK_QueryPoolImpl::endQuery(VkCommandBuffer commandBuffer)
{
    vkCmdEndQuery(commandBuffer, queryPool, 0);
}


void VK_QueryPoolImpl::setQueryCallback(std::function<void (const std::vector<uint64_t> &)> fn)
{
    if (fn)
        queryCallback = fn;
}


void VK_QueryPoolImpl::query()
{
    queryData.assign(queryData.size(), 0);

    vkGetQueryPoolResults(
        context->getDevice(),
        queryPool,
        0, 1, sizeof(uint64_t) * queryData.size(), queryData.data(), sizeof(uint64_t),
        VK_QUERY_RESULT_64_BIT
    );


    if (queryCallback)
        queryCallback(queryData);
}
```

3点说明

1. VkQueryPoolCreateInfo的queryCount是字段pipelineStatistics设置值或元素的个数。感觉这个字段需要去掉，从pipelineStatistics字段也可以拿到查询对象个数的。
2. vkGetQueryPoolResults 把查询统计结果类型固化为了uint64_t类型
3. 查询结束后使用了[回调函数](https://zhida.zhihu.com/search?content_id=207444393&content_type=Article&match_order=1&q=回调函数&zhida_source=entity)发出查询结果。

------

一个使用例子如下

```cpp
auto queryCallback = [](const std::vector<uint64_t> &data)
{
    assert(data.size() == 8);
    std::vector<std::string> items = {
        "Vertex count",
        "Primitives count",
        "Vert shader invocations",
        "Clipping invocations",
        "Clipping primtives",
        "Frag shader invocations",
        "Tessellation control shader patches",
        "Tessellation evaluation shader invocations"
    };

    for (int i = 0; i < 8; i++)
        std::cout << items[i] << ":" << data[i] << std::endl;
    std::cout << std::endl;
};
```

上面是查询回调结果显示

```text
auto query = context->createQueryPool(8, VK_QUERY_PIPELINE_STATISTIC_INPUT_ASSEMBLY_VERTICES_BIT |
                                          VK_QUERY_PIPELINE_STATISTIC_INPUT_ASSEMBLY_PRIMITIVES_BIT |
                                          VK_QUERY_PIPELINE_STATISTIC_VERTEX_SHADER_INVOCATIONS_BIT |
                                          VK_QUERY_PIPELINE_STATISTIC_FRAGMENT_SHADER_INVOCATIONS_BIT |
                                          VK_QUERY_PIPELINE_STATISTIC_CLIPPING_PRIMITIVES_BIT |
                                          VK_QUERY_PIPELINE_STATISTIC_CLIPPING_INVOCATIONS_BIT |
                                          VK_QUERY_PIPELINE_STATISTIC_TESSELLATION_CONTROL_SHADER_PATCHES_BIT |
                                          VK_QUERY_PIPELINE_STATISTIC_TESSELLATION_EVALUATION_SHADER_INVOCATIONS_BIT, queryCallback);




    context->createCommandBuffers();
```

在createCommandBuffers之前创建queryPool。

以上代码判断是在之前绘制小狗细分模型的基础上改的。显示效果不变。某时刻显示查询数据如下

```text
Vertex count:3975
Primitives count:1325
Vert shader invocations:3975
Clipping invocations:21200
Clipping primtives:21200
Frag shader invocations:351552
Tessellation control shader patches:1325
Tessellation evaluation shader invocations:29150
```