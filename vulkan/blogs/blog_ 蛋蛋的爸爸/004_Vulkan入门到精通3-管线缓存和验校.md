# Vulkan入门到精通3-管线缓存和验校

上文我们用Vulkan绘制了一个三角形，本文介绍下管线缓存和数据验校

Vulkan中渲染上下文的变更和管线密切相关，调整渲染上下文就需要重新创建管线。能想到的方法是建立一个管线Map，之后创建管线后，以管线参数为键，管线为值插入前者，每次需要创建的时候先搜索Map，存在则返回，否则创建。这是设计模式中[享元](https://zhida.zhihu.com/search?content_id=184363709&content_type=Article&match_order=1&q=享元&zhida_source=entity)模式的思路（实际有所区别，后者是小对象复用）。

其实Vulkan提供了一个叫vkPipelineCache的对象，用于管线的创建。创建管线时，把管线缓存作为参数参数传入（当然也可以不使用管线缓存）。其内部实现机制应该是创建管线函数内部，先看预构建管线是否已在缓存内，不在则创建一个新管线，并把新创建的管线放入管线缓存。然后在销毁管线缓存前可以把它保存为[二进制文件](https://zhida.zhihu.com/search?content_id=184363709&content_type=Article&match_order=1&q=二进制文件&zhida_source=entity)，下次启动程序时首先从文件创建管线缓存。

[khronos](https://zhida.zhihu.com/search?content_id=184363709&content_type=Article&match_order=1&q=khronos&zhida_source=entity)对管线缓存的定义是 - **Pipeline cache objects allow the result of pipeline construction to be reused between pipelines and between runs of an application. Reuse between pipelines is achieved by passing the same pipeline cache object when creating multiple related pipelines. Reuse across runs of an application is achieved by retrieving pipeline cache contents in one run of an application, saving the contents, and using them to preinitialize a pipeline cache on a subsequent run. The contents of the pipeline cache objects are managed by the implementation. Applications can manage the host memory consumed by a pipeline cache object and control the amount of data retrieved from a pipeline cache object.**

相关代码如下

```cpp
    auto buffer = readDataFromFile(vkConfig.pipelineCacheFile);
    bool valid = appConfig.debug && isValidPipelineCacheData(vkConfig.pipelineCacheFile, buffer.data(), buffer.size());

    VkPipelineCacheCreateInfo PipelineCacheCreateInfo = {};
    PipelineCacheCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO;
    PipelineCacheCreateInfo.pNext = nullptr;
    PipelineCacheCreateInfo.initialDataSize = valid ? buffer.size() : 0;
    PipelineCacheCreateInfo.pInitialData = valid ? buffer.data() : nullptr;

    if (vkCreatePipelineCache(device, &PipelineCacheCreateInfo, nullptr, &pipelineCache) != VK_SUCCESS)
        std::cerr << "creating pipeline cache error" << std::endl;
```

使用vkCreatePipelineCahce创建管线缓存，对应的CreateInfo结构中指定缓存初始化数据大小和指针，为0则说明无初始化数据。

由于Vulkan和底层硬件密切相关，管线缓存数据并不通用，因此在加载数据时需要做必要的验校工作，具体在isValidPipelineCacheData中，如下

```text
bool VK_ContextImpl::isValidPipelineCacheData(const std::string& filename, const char *buffer, uint32_t size)
{
    if(size > 32) {
        uint32_t header = 0;
        uint32_t version = 0;
        uint32_t vendor = 0;
        uint32_t deviceID = 0;
        uint8_t pipelineCacheUUID[VK_UUID_SIZE] = {};

        memcpy(&header, (uint8_t *)buffer + 0, 4);
        memcpy(&version, (uint8_t *)buffer + 4, 4);
        memcpy(&vendor, (uint8_t *)buffer + 8, 4);
        memcpy(&deviceID, (uint8_t *)buffer + 12, 4);
        memcpy(pipelineCacheUUID, (uint8_t *)buffer + 16, VK_UUID_SIZE);

        if (header <= 0) {
            std::cerr << "bad pipeline cache data header length in " << vkConfig.pipelineCacheFile << std::endl;
            return false;
        }

        if (version != VK_PIPELINE_CACHE_HEADER_VERSION_ONE) {
            std::cerr << "unsupported cache header version in " << filename << std::endl;
            std::cerr << "cache contains: 0x" << std::hex << version << std::endl;
        }

        if (vendor != deviceProperties.vendorID) {
            std::cerr << "vendor id mismatch in " << filename << std::endl;
            std::cerr << "cache contains: 0x" << std::hex << vendor << std::endl;
            std::cerr << "driver expects: 0x" << deviceProperties.vendorID << std::endl;
            return false;
        }

        if (deviceID != deviceProperties.deviceID) {
            std::cerr << "device id mismatch in " << filename << std::endl;
            std::cerr << "cache contains: 0x" << std::hex << deviceID << std::endl;
            std::cerr << "driver expects: 0x" << deviceProperties.deviceID << std::endl;
            return false;
        }

        if (memcmp(pipelineCacheUUID, deviceProperties.pipelineCacheUUID, sizeof(pipelineCacheUUID)) != 0) {
            std::cerr << "uuid mismatch in " << filename << std::endl;
            std::cerr << "cache contains: " << std::endl;

            auto fn = [](uint8_t* uuid) {
                for(int i = 0; i < 16; i++) {
                    std::cout << (int)uuid[i] << " ";
                    if(i % 4 == 3)
                        std::cerr << std::endl;
                }
                std::cerr << std::endl;
            };
            fn(pipelineCacheUUID);
            std::cerr << "driver expects:" << std::endl;
            fn(deviceProperties.pipelineCacheUUID);
            return false;
        }

        return true;
    }
    return false;
}
```

其中deviceProperties是根据物理设备查询得到的，函数调用如下

```text
    vkGetPhysicalDeviceFeatures(physicalDevice, &deviceFeatures);
    vkGetPhysicalDeviceProperties(physicalDevice, &deviceProperties);
```

Feature结构中保存了硬件支持功能参数情况

```text
typedef struct VkPhysicalDeviceFeatures {
    VkBool32    robustBufferAccess;
    VkBool32    fullDrawIndexUint32;
    VkBool32    imageCubeArray;
    VkBool32    independentBlend;
    VkBool32    geometryShader;
    VkBool32    tessellationShader;
    VkBool32    sampleRateShading;
    VkBool32    dualSrcBlend;
    VkBool32    logicOp;
    VkBool32    multiDrawIndirect;
    VkBool32    drawIndirectFirstInstance;
    VkBool32    depthClamp;
    VkBool32    depthBiasClamp;
    VkBool32    fillModeNonSolid;
    VkBool32    depthBounds;
    VkBool32    wideLines;
    VkBool32    largePoints;
    VkBool32    alphaToOne;
    VkBool32    multiViewport;
    VkBool32    samplerAnisotropy;
    VkBool32    textureCompressionETC2;
    VkBool32    textureCompressionASTC_LDR;
    VkBool32    textureCompressionBC;
    VkBool32    occlusionQueryPrecise;
    VkBool32    pipelineStatisticsQuery;
    VkBool32    vertexPipelineStoresAndAtomics;
    VkBool32    fragmentStoresAndAtomics;
    VkBool32    shaderTessellationAndGeometryPointSize;
    VkBool32    shaderImageGatherExtended;
    VkBool32    shaderStorageImageExtendedFormats;
    VkBool32    shaderStorageImageMultisample;
    VkBool32    shaderStorageImageReadWithoutFormat;
    VkBool32    shaderStorageImageWriteWithoutFormat;
    VkBool32    shaderUniformBufferArrayDynamicIndexing;
    VkBool32    shaderSampledImageArrayDynamicIndexing;
    VkBool32    shaderStorageBufferArrayDynamicIndexing;
    VkBool32    shaderStorageImageArrayDynamicIndexing;
    VkBool32    shaderClipDistance;
    VkBool32    shaderCullDistance;
    VkBool32    shaderFloat64;
    VkBool32    shaderInt64;
    VkBool32    shaderInt16;
    VkBool32    shaderResourceResidency;
    VkBool32    shaderResourceMinLod;
    VkBool32    sparseBinding;
    VkBool32    sparseResidencyBuffer;
    VkBool32    sparseResidencyImage2D;
    VkBool32    sparseResidencyImage3D;
    VkBool32    sparseResidency2Samples;
    VkBool32    sparseResidency4Samples;
    VkBool32    sparseResidency8Samples;
    VkBool32    sparseResidency16Samples;
    VkBool32    sparseResidencyAliased;
    VkBool32    variableMultisampleRate;
    VkBool32    inheritedQueries;
} VkPhysicalDeviceFeatures;
```

接近gl中Get函数

```text
typedef struct VkPhysicalDeviceProperties {
    uint32_t                            apiVersion;
    uint32_t                            driverVersion;
    uint32_t                            vendorID;
    uint32_t                            deviceID;
    VkPhysicalDeviceType                deviceType;
    char                                deviceName[VK_MAX_PHYSICAL_DEVICE_NAME_SIZE];
    uint8_t                             pipelineCacheUUID[VK_UUID_SIZE];
    VkPhysicalDeviceLimits              limits;
    VkPhysicalDeviceSparseProperties    sparseProperties;
} VkPhysicalDeviceProperties;
```

DeviceProperties中提供了API版本，驱动版本，vendor信息等硬件情况。

再回归主题

销毁管线缓存前，先获取缓存数据并保存到本地

```text
bool VK_ContextImpl::saveGraphicsPiplineCache()
{
    size_t cacheSize = 0;

    if (vkGetPipelineCacheData(device, pipelineCache, &cacheSize, nullptr) != VK_SUCCESS) {
        std::cerr << "getting cache size fail from pipelinecache" << std::endl;
        return false;
    }

    auto cacheData = std::vector<char>(sizeof(char) * cacheSize, 0);

    if(vkGetPipelineCacheData(device, pipelineCache, &cacheSize, &cacheData[0]) != VK_SUCCESS) {
        std::cerr << "getting cache fail from pipelinecache" << std::endl;
        return false;
    }

    std::ofstream stream(vkConfig.pipelineCacheFile, std::ios::binary);
    if(stream.is_open()) {
        stream.write(cacheData.data(), cacheData.size());
        stream.close();
    } else {
        std::cerr << "open pipeline cache data target file failed!" << std::endl;
        return false;
    }
    return true;
}
```

然后是销毁缓存 - vkDestroyPipelineCache(device, pipelineCache, nullptr);

创建管线时传入管线缓存，调用例子是

```text
    VkGraphicsPipelineCreateInfo pipelineInfo{};
    pipelineInfo.sType = VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
    pipelineInfo.stageCount = vkShaderSet->getCreateInfoCount();
    pipelineInfo.pStages = vkShaderSet->getCreateInfoData();
    pipelineInfo.pVertexInputState = &vertexInputInfo;
    pipelineInfo.pInputAssemblyState = &inputAssembly;
    pipelineInfo.pViewportState = &viewportState;
    pipelineInfo.pRasterizationState = &rasterizer;
    pipelineInfo.pMultisampleState = &multisampling;
    pipelineInfo.pColorBlendState = &colorBlending;
    pipelineInfo.layout = pipelineLayout;
    pipelineInfo.renderPass = renderPass;
    pipelineInfo.subpass = 0;
    pipelineInfo.basePipelineHandle = VK_NULL_HANDLE;
    pipelineInfo.pNext = nullptr;

    if (vkCreateGraphicsPipelines(device, pipelineCache, 1, &pipelineInfo, nullptr, &graphicsPipeline) != VK_SUCCESS) {
        std::cerr << "failed to create graphics pipeline!" << std::endl;
        return false;
    }
```

使用管线缓存的目的是降低管线创建开销，提高程序性能。



本文涉及API有

1. vkGetPhysicalDeviceFeatures
2. vkGetPhysicalDeviceProperties
3. vkCreatePipelineCache
4. vkDestroyPipelineCache
5. vkGetPipelineCacheData
6. vkCreateGraphicsPipelines

代码仓

[https://github.com/ccsdu2004/vulkan-cpp-demogithub.com/ccsdu2004/vulkan-cpp-demo](https://link.zhihu.com/?target=https%3A//github.com/ccsdu2004/vulkan-cpp-demo)



可站内联系作者

