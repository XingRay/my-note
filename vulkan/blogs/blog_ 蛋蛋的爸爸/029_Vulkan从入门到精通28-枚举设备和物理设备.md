# Vulkan从入门到精通28-枚举设备和物理设备

本文来点基础的，在 [蛋蛋的爸爸：Vulkan从入门到精通12-实例化和验证层](https://zhuanlan.zhihu.com/p/440975542) 一文我们讲了vulkan实例初始化；创建完成实例后，就可以查询系统当前的图形显卡了。当然，有可能有一个合适的显卡，也有可能有多个，没有合适的显卡也是正常的。

通过函数 vkEnumeratePhysicalDevices 就可以拿到物理显卡的个数了

此[函数原型](https://zhida.zhihu.com/search?content_id=189935074&content_type=Article&match_order=1&q=函数原型&zhida_source=entity)如下

```cpp
VkResult vkEnumeratePhysicalDevices(
    VkInstance                                  instance,
    uint32_t*                                   pPhysicalDeviceCount,
    VkPhysicalDevice*                           pPhysicalDevices);
```

参数分别是vk实例，设备个数和物理设备指针。介绍原文是

*If `pPhysicalDevices` is `NULL`, then the number of physical devices available is returned in `pPhysicalDeviceCount`. Otherwise, `pPhysicalDeviceCount`**must** point to a variable set by the user to the number of elements in the `pPhysicalDevices` [array](https://zhida.zhihu.com/search?content_id=189935074&content_type=Article&match_order=1&q=array&zhida_source=entity), and on return the variable is overwritten with the number of handles actually written to `pPhysicalDevices`. If `pPhysicalDeviceCount` is less than the number of physical devices available, at most `pPhysicalDeviceCount` structures will be written, and `VK_INCOMPLETE` will be returned instead of `VK_SUCCESS`, to indicate that not all the available physical devices were returned.*

不得不说，这个函数设计有点奇葩。如果传入设备指针为nullptr，那么传出的是设备个数；否则根据个数创建一个数组，然后再次调用就可以拿到具体设备指针。

具体调用如下

函数 - VK_ContextImpl::pickPhysicalDevice()

```cpp
    uint32_t deviceCount = 0;
    vkEnumeratePhysicalDevices(instance, &deviceCount, nullptr);

    if (deviceCount == 0) {
        std::cerr << "failed to find GPUs with Vulkan support!" << std::endl;
        return false;
    }

    std::vector<VkPhysicalDevice> devices(deviceCount);
    vkEnumeratePhysicalDevices(instance, &deviceCount, devices.data());
```

查询出的设备，还需要查询相当特性，看看是不是满足我们的要求。

然后是

```cpp
vkGetPhysicalDeviceFeatures(device, &deviceFeatures);
vkGetPhysicalDeviceProperties(device, &deviceProperties);
```

前者使用查询对[纹理压缩](https://zhida.zhihu.com/search?content_id=189935074&content_type=Article&match_order=1&q=纹理压缩&zhida_source=entity)，64位浮点和多视口渲染 等可选功能的支持：

后者则是查询基本设备属性，如名称，类型和支持的Vulkan版本。

对于笔者所用的笔记本，查询出的设备如下

- GeForce MX330
- Intel(R) Iris(R) Xe Graphics

------

针对设备，还需要查询设备是不是满足我们的要求。对于之前的大部分Demo，使用GeForce或者Xe Graphics都是可以的，但是对于mip贴图等，后者是不支持的，需要使用前者。如强行使用后者，则会出现程序崩溃的错误，排查起来比较麻烦。 VulkanTutorial教程中使用第一个物理设备是有道理的。

针对具体设备，使用vkGetPhysicalDeviceQueueFamilyProperties查询设备可用的Queue簇，也就是VkQueueFamilyProperties。函数用法和vkEnumeratePhysicalDevices一致。

之后即可遍历VkQueueFamilyProperties，看看是不是包含VK_QUEUE_GRAPHICS_BIT，以及检查是不是支持呈现到设备表面。同时满足二者才代表设备可用。

代码大致如下

```cpp
    uint32_t queueFamilyCount = 0;
    vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, nullptr);

    std::vector<VkQueueFamilyProperties> queueFamilies(queueFamilyCount);
    vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, queueFamilies.data());

    int i = 0;
    for (const auto &queueFamily : queueFamilies) {
        if (queueFamily.queueFlags & VK_QUEUE_GRAPHICS_BIT) {
            indices.graphicsFamily = i;
        }

        VkBool32 presentSupport = false;
        vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, &presentSupport);

        if (presentSupport)
            indices.presentFamily = i;

        if (indices.isComplete())
            break;
        i++;
    }
```

单查询这个不够，还需要查询扩展支持情况，交换链以及设备属性等。

```cpp
bool VK_ContextImpl::isDeviceSuitable(VkPhysicalDevice device)
{
    QueueFamilyIndices list = findQueueFamilies(device);

    bool extensionsSupported = checkDeviceExtensionSupport(device);

    bool swapChainAdequate = false;
    if (extensionsSupported) {
        SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device);
        swapChainAdequate = !swapChainSupport.formats.empty() && !swapChainSupport.presentModes.empty();
    }

    vkGetPhysicalDeviceFeatures(device, &deviceFeatures);
    return list.isComplete() && extensionsSupported && swapChainAdequate
           && deviceFeatures.samplerAnisotropy;
}
```

在下面的函数中检查扩展

```text
bool VK_ContextImpl::checkDeviceExtensionSupport(VkPhysicalDevice device)
{
    uint32_t extensionCount;
    vkEnumerateDeviceExtensionProperties(device, nullptr, &extensionCount, nullptr);

    std::vector<VkExtensionProperties> availableExtensions(extensionCount);
    vkEnumerateDeviceExtensionProperties(device, nullptr, &extensionCount, availableExtensions.data());

    std::set<std::string> requiredExtensions(deviceExtensions.begin(), deviceExtensions.end());

    for (const auto &extension : availableExtensions) {
        requiredExtensions.erase(extension.extensionName);
    }

    return requiredExtensions.empty();
}
```

程序中强制检查的扩展就一个 VK_KHR_swapchain，存在则符合预期

在下面的函数中检查交换链支持情况

```cpp
SwapChainSupportDetails VK_ContextImpl::querySwapChainSupport(VkPhysicalDevice device)
{
    SwapChainSupportDetails details;

    vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, &details.capabilities);

    uint32_t formatCount;
    vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, &formatCount, nullptr);

    if (formatCount != 0) {
        details.formats.resize(formatCount);
        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, &formatCount, details.formats.data());
    }

    uint32_t presentModeCount;
    vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, &presentModeCount, nullptr);

    if (presentModeCount != 0) {
        details.presentModes.resize(presentModeCount);
        vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, &presentModeCount,
                details.presentModes.data());
    }

    return details;
}
```

最后是使用vkGetPhysicalDeviceFeatures(device, &deviceFeatures);查询设备特性

当前对设备特性的要求是支持[各向异性](https://zhida.zhihu.com/search?content_id=189935074&content_type=Article&match_order=1&q=各向异性&zhida_source=entity)，具体如下

```text
    return list.isComplete() && extensionsSupported && swapChainAdequate
           && deviceFeatures.samplerAnisotropy;
```