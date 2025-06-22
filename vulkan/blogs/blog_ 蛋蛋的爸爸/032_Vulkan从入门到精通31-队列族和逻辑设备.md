# Vulkan从入门到精通31-队列族和逻辑设备

Vulkan 的几乎所有操作，从绘制到加载纹理都需要将操作 指令提交给一个队列，然后才能执行。Vulkan 有多种不同类型的队列，它们属于不同的队列族，每个队列族的队列只允许执行特定的一部分指令。 比如，可能存在只允许执行计算相关指令的队列族和只允许执行内存传输的队列族。

在 [蛋蛋的爸爸：Vulkan从入门到精通28-枚举设备和物理设备](https://zhuanlan.zhihu.com/p/458119912) 选择物理设备后，我们需要创建一个逻辑设备。逻辑设备应该是Vulkan中最常用的对象，大部分Vk对象的创建都离不开它。逻辑设备的创建和Vk实例化是一样的，唯一不同的是需要指定需要开启那些特性。需要注意的是在同一物理设备上创建多个逻辑设备也是可以的。在程序中是通过 函数findQueueFamilies来查找出满足我们需求的队列族

```cpp
QueueFamilyIndices VK_ContextImpl::findQueueFamilies(VkPhysicalDevice device)
{
    QueueFamilyIndices indices;

    uint32_t queueFamilyCount = 0;
    vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, nullptr);

    std::vector<VkQueueFamilyProperties> queueFamilies(queueFamilyCount);
    vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, queueFamilies.data());

    int i = 0;
    for (const auto &queueFamily : queueFamilies) {
        if (queueFamily.queueFlags & VK_QUEUE_GRAPHICS_BIT)
            indices.graphicsFamily = i;

        VkBool32 presentSupport = false;
        vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, &presentSupport);

        if (presentSupport)
            indices.presentFamily = i;

        if (indices.isComplete())
            break;
        i++;
    }

    return indices;
}
```

------

逻辑设备创建需要填写 VkDeviceQueueCreateInfo [结构体](https://zhida.zhihu.com/search?content_id=190556940&content_type=Article&match_order=1&q=结构体&zhida_source=entity)。

```cpp
    QueueFamilyIndices indices = findQueueFamilies(physicalDevice);

    std::vector<VkDeviceQueueCreateInfo> queueCreateInfos;
    std::set<uint32_t> uniqueQueueFamilies = {indices.graphicsFamily.value(), indices.presentFamily.value()};

    float queuePriority = 0.0f;
    for (uint32_t queueFamily : uniqueQueueFamilies) {
        VkDeviceQueueCreateInfo queueCreateInfo{};
        queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
        queueCreateInfo.queueFamilyIndex = queueFamily;
        queueCreateInfo.queueCount = 1;
        queueCreateInfo.pQueuePriorities = &queuePriority;
        queueCreateInfos.push_back(queueCreateInfo);
    }
```

在创建逻辑设备时需要指定要开启的属性

```cpp
    VkDeviceCreateInfo createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;

    createInfo.queueCreateInfoCount = static_cast<uint32_t>(queueCreateInfos.size());
    createInfo.pQueueCreateInfos = queueCreateInfos.data();

    createInfo.pEnabledFeatures = &logicalFeatures;

    createInfo.enabledExtensionCount = static_cast<uint32_t>(deviceExtensions.size());
    createInfo.ppEnabledExtensionNames = deviceExtensions.data();

    vkValidationLayer->adjustVkDeviceCreateInfo(createInfo);

    if (vkCreateDevice(physicalDevice, &createInfo, getAllocation(), &device) != VK_SUCCESS) {
        std::cerr << "failed to create logical device!" << std::endl;
        return false;
    }
```

由于每个程序需要开启的特性都不一样，所以定义了一个成员变量 - VkPhysicalDeviceFeatures logicalFeatures{};。在程序中创建vulkan设备前显式指定特性，比如要使用细分着色器则可以在程序中采用以下写法

```cpp
    VkPhysicalDeviceFeatures deviceFeatures{};
    deviceFeatures.tessellationShader = VK_TRUE;
    deviceFeatures.fillModeNonSolid = VK_TRUE;
    context->setLogicalDeviceFeatures(deviceFeatures);
```

和vkCreateDevice相匹配，清理程序时需要调用vkDestroyDevice销毁设备。

创建逻辑设备时指定的队列会随着逻辑设备一同被创建；逻辑设备的队列会在逻辑设备清除时，自动被清除，所以不需要销毁。