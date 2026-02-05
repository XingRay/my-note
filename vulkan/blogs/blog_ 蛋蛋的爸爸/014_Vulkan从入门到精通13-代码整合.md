# Vulkan从入门到精通13-代码整合

截止当前，我们已经掌握了以下技能

- 单个多边形，多个多边形的绘制，网格也是可以的
- 基于索引的多边形显示
- 清屏和颜色混合
- vertex 中的uniform
- 视口和裁剪
- 纹理显示
- 深度缓存
- 管线缓存

看上去不少了，但是问题多多，比如程序支持的顶点类型是固定的；不支持多个uniform；只支持填充渲染模式。另外一个严重的问题是一些关联参数的设置是在程序好几处分别设置的，比较零散。等等。此篇我们来解决大部分问题

1.首先提取出一个单独的VK_ValidationLayer用于验证工作。接口如下

```cpp
class VK_ValidationLayer : public VK_Deleter
{
public:
    static VKAPI_ATTR VkBool32 VKAPI_CALL debugCallback(VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity, VkDebugUtilsMessageTypeFlagsEXT messageType, const VkDebugUtilsMessengerCallbackDataEXT* pCallbackData, void* pUserData);
    static inline const std::vector<const char*> validationlayerSets = {"VK_LAYER_KHRONOS_validation", "VK_LAYER_LUNARG_standard_validation"};
public:
    VK_ValidationLayer(bool debug = false);
    ~VK_ValidationLayer();

    void release()override;
public:
    void adjustVkInstanceCreateInfo(VkInstanceCreateInfo& createInfo);
    void adjustVkDeviceCreateInfo(VkDeviceCreateInfo& createInfo);
    bool appendValidationLayerSupport();
    bool setupDebugMessenger(VkInstance instance);

    void cleanup(VkInstance instance);
private:
    void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT &createInfo);
    bool isValidationLayer(const char* name);
private:
    bool vkDebug = false;
    VkDebugUtilsMessengerCreateInfoEXT vkDebugCreateInfo{};
    std::vector<const char*> validationLayers;
    VkDebugUtilsMessengerEXT debugMessenger = 0;
};
```

2. VulkanContext对外暴露VkPipelineRasterizationStateCreateInfo和VkPipelineDepthStencilStateCreateInfo 可读可写

```cpp
    virtual VkPipelineRasterizationStateCreateInfo getPipelineRasterizationStateCreateInfo() = 0;
    virtual void setPipelineRasterizationStateCreateInfo(const VkPipelineRasterizationStateCreateInfo& createInfo) = 0;

    virtual VkPipelineDepthStencilStateCreateInfo getPipelineDepthStencilStateCreateInfo() = 0;
    virtual void setPipelineDepthStencilStateCreateInfo(const VkPipelineDepthStencilStateCreateInfo& createInfo) = 0;
```

默认初始化是在*initVulkanContext里完成，如果需要修改，则可在initVulkanContext调用后修改后再写入*

*有了此二个函数就可以在应用程序里设置深度缓存和光栅模式*

*3.initVulkan分拆为initVulkanDevice和initVulkanContext，前者完成vulkan实例化、表面创建，设备初始化，交换链以及交换链视图的创建。后者完成创建管线外的其余工作*

*4.设置输入顶点数据的相关函数移入VK_ShaderSet。毕竟此对象负责加载和创建Shader模型，对应的顶点类型也可以归并到此*

```cpp
    virtual void appendAttributeDescription(int index, int size) = 0;
    virtual VkVertexInputBindingDescription* getBindingDescription() = 0;
    virtual size_t getAttributeDescriptionCount()const = 0;
    virtual const VkVertexInputAttributeDescription* getAttributeDescriptionData()const = 0;
```

在程序里面就可以这样写了

```text
    auto shaderSet = context->createShaderSet();
    shaderSet->addShader("../shader/vertex/vert.spv", VK_SHADER_STAGE_VERTEX_BIT);
    shaderSet->addShader("../shader/vertex/frag.spv", VK_SHADER_STAGE_FRAGMENT_BIT);

    shaderSet->appendAttributeDescription(0, sizeof (float) * 3);
    shaderSet->appendAttributeDescription(1, sizeof (float) * 4);
```

顶点shader中location=0处为vec3元素，location=1处为vec4元素。

5. VkDescriptorSetLayoutBinding 相关接口移入ShaderSet

```cpp
    static VkDescriptorSetLayoutBinding createDescriptorSetLayoutBinding(uint32_t id, VkDescriptorType type, VkShaderStageFlagBits flag);
    virtual void addDescriptorSetLayoutBinding(const VkDescriptorSetLayoutBinding& binding) = 0;
    virtual size_t getDescriptorSetLayoutBindingCount()const = 0;
    virtual const VkDescriptorSetLayoutBinding* getDescriptorSetLayoutBindingData()const = 0;
```

6. 考虑到VkDescriptorPoolSize和 VkDescriptorSetLayoutBinding关联也移入ShaderSet

```cpp
    virtual size_t getDescriptorPoolSizeCount()const = 0;
    virtual const VkDescriptorPoolSize *getDescriptorPoolSizeData()const = 0;
    virtual void updateDescriptorPoolSize(int32_t size) = 0;
```

具体在程序里这样设置

```cpp
    VkDescriptorSetLayoutBinding uniformBinding = VK_ShaderSet::createDescriptorSetLayoutBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT);
    shaderSet->addDescriptorSetLayoutBinding(uniformBinding);

    auto samplerBinding = VK_ShaderSet::createDescriptorSetLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT);
    auto samplerCreateInfo  = VK_Sampler::createSamplerCreateInfo();
    auto samplerPtr = context->createSampler(samplerCreateInfo);
    VkSampler sampler = samplerPtr->getSampler();
    samplerBinding.pImmutableSamplers = &sampler;

    shaderSet->addDescriptorSetLayoutBinding(samplerBinding);
```

在VK_ShaderSet::createDescriptorSetLayoutBinding函数内根据type增加poolSize如下

```cpp
void VK_ShaderSetImpl::addDescriptorSetLayoutBinding(const VkDescriptorSetLayoutBinding &binding)
{
    descriptorSetLayoutBindings.push_back(binding);
    VkDescriptorPoolSize poolSize;
    poolSize.descriptorCount = 0;
    poolSize.type = binding.descriptorType;
    descriptorPoolSizes.push_back(poolSize);
}
```

然后在VK_ContextImpl::createDescriptorPool函数内需要检查PoolSize个数，要求最少保证一个，如下

```cpp
void VK_ContextImpl::createDescriptorPool()
{
    vkShaderSet->updateDescriptorPoolSize(swapChainImageViews.size());

    VkDescriptorPoolSize poolSize{};
    poolSize.type = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
    poolSize.descriptorCount = static_cast<uint32_t>(swapChainImages.size());

    VkDescriptorPoolCreateInfo poolInfo{};
    poolInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
    if(vkShaderSet->getDescriptorPoolSizeCount() > 0) {
        poolInfo.poolSizeCount = vkShaderSet->getDescriptorPoolSizeCount();
        poolInfo.pPoolSizes = vkShaderSet->getDescriptorPoolSizeData();
    } else {
        poolInfo.poolSizeCount = 1;
        poolInfo.pPoolSizes = &poolSize;
    }
    poolInfo.maxSets = static_cast<uint32_t>(swapChainImages.size());

    if (vkCreateDescriptorPool(device, &poolInfo, nullptr, &descriptorPool) != VK_SUCCESS) {
        std::cerr << "failed to create descriptor pool!" << std::endl;
    }
}
```

最后就可以改写之前的例子了

```cpp
const std::vector<float> vertices = {
    -0.5f, -0.5, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 5.0f, 0.0f,
        0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
        0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 5.0f,
        -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 5.0f, 5.0f
    };

const std::vector<uint16_t> indices = {
    0, 1, 2, 2, 3, 0
};

VK_Context* context = nullptr;

uint32_t updateUniformBufferData(char* & data, uint32_t size)
{
    glm::mat4 model = glm::identity<glm::mat4>();
    memcpy(data, &model[0][0], size);
    return sizeof(model);
}

void onFrameSizeChanged(int width, int height)
{
    auto vp = VK_Viewports::createViewport(width, height);
    VK_Viewports vps;
    vps.addViewport(vp);
    context->setViewports(vps);
}

int main()
{
    VK_ContextConfig config;
    config.debug = false;
    config.name = "Texure Demo";

    context = createVkContext(config);
    context->createWindow(480, 480, true);
    context->setOnFrameSizeChanged(onFrameSizeChanged);

    VK_Context::VK_Config vkConfig;
    context->initVulkanDevice(vkConfig);

    auto shaderSet = context->createShaderSet();
    shaderSet->addShader("../shader/texture/vert.spv", VK_SHADER_STAGE_VERTEX_BIT);
    shaderSet->addShader("../shader/texture/frag.spv", VK_SHADER_STAGE_FRAGMENT_BIT);

    shaderSet->appendAttributeDescription(0, sizeof (float) * 3);
    shaderSet->appendAttributeDescription(1, sizeof (float) * 4);
    shaderSet->appendAttributeDescription(2, sizeof (float) * 2);

    VkDescriptorSetLayoutBinding uniformBinding = VK_ShaderSet::createDescriptorSetLayoutBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT);
    shaderSet->addDescriptorSetLayoutBinding(uniformBinding);

    auto samplerBinding = VK_ShaderSet::createDescriptorSetLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT);
    auto samplerCreateInfo  = VK_Sampler::createSamplerCreateInfo();
    auto samplerPtr = context->createSampler(samplerCreateInfo);
    VkSampler sampler = samplerPtr->getSampler();
    samplerBinding.pImmutableSamplers = &sampler;

    shaderSet->addDescriptorSetLayoutBinding(samplerBinding);

    if(!shaderSet->isValid()) {
        std::cerr << "invalid shaderSet" << std::endl;
        shaderSet->release();
        context->release();
        return -1;
    }

    auto buffer = context->createVertexBuffer(vertices, 9, indices);
    context->addBuffer(buffer);

    auto ubo = context->createUniformBuffer(0, sizeof(float) * 16);
    ubo->setWriteDataCallback(updateUniformBufferData);
    context->addUniformBuffer(ubo);

    auto image = context->createImage("../images/smile.png");
    auto imageViewCreateInfo = VK_ImageView::createImageViewCreateInfo(image->getImage(), VK_FORMAT_R8G8B8A8_SRGB);
    auto imageView = context->createImageView(imageViewCreateInfo);
    context->addImageView(imageView);

    context->initVulkanContext(shaderSet);
    context->initPipeline();
    context->createCommandBuffers();

    context->run();
    context->release();

    return 0;
}
```

其中

```cpp
VkDescriptorSetLayoutBinding uniformBinding = VK_ShaderSet::createDescriptorSetLayoutBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT);
    shaderSet->addDescriptorSetLayoutBinding(uniformBinding);
```

表明[顶点着色器](https://zhida.zhihu.com/search?content_id=186188024&content_type=Article&match_order=1&q=顶点着色器&zhida_source=entity)中binding = 0 处是一个uniform

```cpp
auto samplerBinding = VK_ShaderSet::createDescriptorSetLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT);
    auto samplerCreateInfo  = VK_Sampler::createSamplerCreateInfo();
```

[片段着色器](https://zhida.zhihu.com/search?content_id=186188024&content_type=Article&match_order=1&q=片段着色器&zhida_source=entity) binding = 1处是一个unifrom sampler2D 对象

是不是清晰多了？



代码仓库 -

[https://github.com/ccsdu2004/vulkan-cpp-demo](https://github.com/ccsdu2004/vulkan-cpp-demo)