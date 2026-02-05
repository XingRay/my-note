# Vulkan从入门到精通12-实例化和验证层

从本篇起我们开始穿插引入vulkan环境初始化，先讲讲vulkan实例化和验证层

Vulkan API 使用`vkInstance` 对象来存储应用级别的状态。在进行其他的操作之前，应用必须创建一个Vulkan实例。

基础的Vulkan架构看起来像这样

![img](./assets/v2-db735940fa21eb0bbcf0af59d9ef0cd6_1440w.png)

vulkan应用与Vulkan 库相关联，使用*loader*。实例的创建会初始化loader。这个loader会加载和初始化一个由GPU提供的二级的图形驱动。

具体实例化和其他vulkan对象一样，需要先构建一个对应的createinfo

```cpp
    VkApplicationInfo appInfo;
    appInfo.pNext = nullptr;
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = appConfig.name.data();
    appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.pEngineName = "Vulkan Framework";
    appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.apiVersion = VK_API_VERSION_1_0;
```

其中pApplicationName填入程序名字，pEngineName写入引擎名称，其他几个写入版本等。

```cpp
    VkInstanceCreateInfo createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;
```

之后就可以创建vulkan实例了

```cpp
    if (vkCreateInstance(&createInfo, nullptr, &instance) != VK_SUCCESS) {
        std::cerr << "failed to create instance!" << std::endl;
        return false;
    }
```

而validation是驱动的错误校验。在Vulkan中，驱动比在其他的API更加的轻量化，这正是因为驱动将validation功能给layers代理的结果。layers并不是必须的，而且在每一次创建实例的时候，layers可以被选择性的加载。在开发过程中我们了解到程序执行过程中驱动反馈的错误信息，就可以借助验证层获取相关信息。

比如VkDescriptorSetLayoutBinding和VkDescriptorPoolSize数目不匹配时错误信息看上去是这个样子

> validation layer: Validation Error: [ VUID-VkDescriptorSetAllocateInfo-descriptorPool-00307 ] Object 0: handle = 0xba7514000000002a, type = VK_OBJECT_TYPE_UNKNOWN; | MessageID = 0x21859338 | vkAllocateDescriptorSets(): Unable to allocate 3 [descriptors](https://zhida.zhihu.com/search?content_id=186124165&content_type=Article&match_order=1&q=descriptors&zhida_source=entity) of type VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER from VkNonDispatchableHandle 0xba7514000000002a[]. This pool only has 0 descriptors of this type remaining. The Vulkan spec states: descriptorPool must have enough free descriptor capacity remaining to allocate the descriptor sets of the specified [layouts](https://zhida.zhihu.com/search?content_id=186124165&content_type=Article&match_order=1&q=layouts&zhida_source=entity) ([https://vulkan.lunarg.com/doc/view/1.2.189.2/windows/1.2-extensions/vkspec.html#VUID-VkDescriptorSetAllocateInfo-descriptorPool-00307](https://vulkan.lunarg.com/doc/view/1.2.189.2/windows/1.2-extensions/vkspec.html%23VUID-VkDescriptorSetAllocateInfo-descriptorPool-00307))

对程序开发相当有帮助。

具体的验证层部分代码如下

实例化前先获取验证层支持结果

```cpp
bool VK_ContextImpl::checkValidationLayerSupport()
{
    uint32_t layerCount;
    vkEnumerateInstanceLayerProperties(&layerCount, nullptr);

    std::vector<VkLayerProperties> availableLayers(layerCount);
    vkEnumerateInstanceLayerProperties(&layerCount, availableLayers.data());

    bool layerFound = false;
    for (const char* layerName : validationLayers) {
        for (const auto& layerProperties : availableLayers) {
	    if (strcmp(layerName, layerProperties.layerName) == 0) {
                layerFound = true;
                break;
            }
        }
    }
    
    return layerFound;
}
```

笔者在windows和uos上发现验证层并不一样，一个是VK_LAYER_KHRONOS_validation，另外一个则是*VK_LAYER_LUNARG_standard_validation。*

然后在实例化前设置createInfo中的字段

```cpp
        VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo{};
        createInfo.enabledLayerCount = static_cast<uint32_t>(validationLayers.size());
        createInfo.ppEnabledLayerNames = validationLayers.data();

        populateDebugMessengerCreateInfo(debugCreateInfo);
        createInfo.pNext = (VkDebugUtilsMessengerCreateInfoEXT*) &debugCreateInfo;
```

然后在实例化后设置debug callback输出回调

```cpp
    VkDebugUtilsMessengerCreateInfoEXT createInfo;
    populateDebugMessengerCreateInfo(createInfo);

    if (createDebugUtilsMessengerEXT(instance, &createInfo, nullptr, &debugMessenger) != VK_SUCCESS) {
        std::cout << "failed to set up debug messenger!" << std::endl;
        return false;
    }

void VK_ContextImpl::populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT &createInfo)
{
    createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
    createInfo.messageSeverity = VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
    createInfo.messageType = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
    createInfo.pfnUserCallback = debugCallback;
}
```

debugCallback实现如下

```cpp
VkBool32 VK_ContextImpl::debugCallback(VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity, VkDebugUtilsMessageTypeFlagsEXT messageType, const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData, void *pUserData)
{
    if(messageSeverity == VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
        std::cerr << "validation layer: " << pCallbackData->pMessage << std::endl;
    return VK_FALSE;
}
```

另外一种层验证的办法如下

定义

```cpp
    PFN_vkCreateDebugReportCallbackEXT dbgCreateDebugReportCallback = nullptr;
    PFN_vkDestroyDebugReportCallbackEXT dbgDestroyDebugReportCallback = nullptr;
```

定义debug[回调函数](https://zhida.zhihu.com/search?content_id=186124165&content_type=Article&match_order=1&q=回调函数&zhida_source=entity)

```cpp
#define APP_SHORT_NAME "Debug Report Callback"

VKAPI_ATTR VkBool32 VKAPI_CALL dbgFunc(VkDebugReportFlagsEXT msgFlags, VkDebugReportObjectTypeEXT objType, uint64_t srcObject,
                                       size_t location, int32_t msgCode, const char *pLayerPrefix, const char *pMsg,
                                       void *pUserData)
{
    std::ostringstream message;

    if (msgFlags & VK_DEBUG_REPORT_ERROR_BIT_EXT) {
        message << "ERROR: ";
    } else if (msgFlags & VK_DEBUG_REPORT_WARNING_BIT_EXT) {
        message << "WARNING: ";
    } else if (msgFlags & VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT) {
        message << "PERFORMANCE WARNING: ";
    } else if (msgFlags & VK_DEBUG_REPORT_INFORMATION_BIT_EXT) {
        message << "INFO: ";
    } else if (msgFlags & VK_DEBUG_REPORT_DEBUG_BIT_EXT) {
        message << "DEBUG: ";
    }
    message << "[" << pLayerPrefix << "] Code " << msgCode << " : " << pMsg;

    std::cerr << message.str() << std::endl;
    return false;
}
```

setDebugMessager函数改成如下

```cpp
bool VK_ContextImpl::setupDebugMessenger()
{
    if (!appConfig.debug)
        return true;

    dbgCreateDebugReportCallback =
        (PFN_vkCreateDebugReportCallbackEXT)vkGetInstanceProcAddr(instance, "vkCreateDebugReportCallbackEXT");
    if (!dbgCreateDebugReportCallback) {
        std::cout << "GetInstanceProcAddr: Unable to find "
                  "vkCreateDebugReportCallbackEXT function."
                  << std::endl;
        return false;
    }

    dbgDestroyDebugReportCallback =
        (PFN_vkDestroyDebugReportCallbackEXT)vkGetInstanceProcAddr(instance, "vkDestroyDebugReportCallbackEXT");
    if (!dbgDestroyDebugReportCallback) {
        std::cout << "GetInstanceProcAddr: Unable to find "
                  "vkDestroyDebugReportCallbackEXT function."
                  << std::endl;
        return false;
    }

    VkDebugReportCallbackCreateInfoEXT debugReportCreateInfo = {};
    debugReportCreateInfo.sType = VK_STRUCTURE_TYPE_DEBUG_REPORT_CREATE_INFO_EXT;
    debugReportCreateInfo.pNext = NULL;
    debugReportCreateInfo.flags = VK_DEBUG_REPORT_ERROR_BIT_EXT | VK_DEBUG_REPORT_WARNING_BIT_EXT;
    debugReportCreateInfo.pfnCallback = dbgFunc;
    debugReportCreateInfo.pUserData = NULL;

    auto res = dbgCreateDebugReportCallback(instance, &debugReportCreateInfo, NULL, &debugReportCallback);
    switch (res) {
        case VK_SUCCESS:
            break;
        case VK_ERROR_OUT_OF_HOST_MEMORY:
            std::cout << "dbgCreateDebugReportCallback: out of host memory\n" << std::endl;
            return false;
        default:
            std::cout << "dbgCreateDebugReportCallback: unknown failure\n" << std::endl;
            return false;
    }

    return true;
}
```

最后别忘了销毁[callback](https://zhida.zhihu.com/search?content_id=186124165&content_type=Article&match_order=2&q=callback&zhida_source=entity)

```cpp
        if(dbgDestroyDebugReportCallback)
            dbgDestroyDebugReportCallback(instance, debugReportCallback, NULL);
```

代码仓库 -

[https://github.com/ccsdu2004/vulkan-cpp-demogithub.com/ccsdu2004/vulkan-cpp-demo](https://github.com/ccsdu2004/vulkan-cpp-demo)

api 列表

- vkCreateInstance
- vkDestroyInstance
- vkGetInstanceProcAddr