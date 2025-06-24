# Vulkan开发学习记录 05 - 窗口表面

## 简述

Vulkan 是一个平台无关的API，它不能直接和窗口系统交互。为了将Vulkan 渲染的图像显示在窗口上，我们需要使用WSI(Window System Integration)扩展。在本章节，我们首先介绍VK_KHR_surface 扩展，它通过VkSurfaceKHR 对象抽象出Vulkan 渲染的表面。我们使用GLFW 来获取VkSurface 对象。

VK_KHR_surface 是一个实例级别的扩展，它已经被包含在使用glfwRequiredInstanceExtensions 函数获取的扩展列表中，所以，我们不需要自己请求这一扩展。WSI 扩展同样也被包含在glfwRequiredInstanceExtensions 函数获取的扩展列表中，也不需要我们自己请求。 由于窗口表面对物理设备的选择有一定影响，它的创建只能在Vulkan 实例创建之后进行。

## 创建窗口表面

```text
VkSurfaceKHR surface;
```

尽管VkSurfaceKHR 对象是平台无关的，但它的创建依赖[窗口系统](https://zhida.zhihu.com/search?content_id=217216597&content_type=Article&match_order=2&q=窗口系统&zhida_source=entity)。比如，在Window系统上，它的创建需要HWND和HMODULE。存在一个叫做VK_KHR_win32_surface平台特有扩展，用于处理与Window 系统窗口交互有关的问题，这一扩展也被包含在了glfwRequiredInstanceExtensions 函数获取的扩展列表中。

接下来，我们将会演示如何使用这一Window 系统特有扩展来创建表面，但对于之后的章节，我们不会使用这一特定平台扩展，而是直接使GLFW库来完成相关操作。我们可以使用glfwCreateWindowSurface 函数来完成表面创建。这里演示如何使用这一平台特定扩展，是出于学习目的，让读者能明白我们使用的GLFW库在背后究竟做了什么。

我们需要填写VkWin32SurfaceCreateInfoKHR结构体来完成VkSurfaceKHR对象的创建。这一结构体包含了两个非常重要的成员：hwnd和hinstace。它们分别对应Window系统的[窗口句柄](https://zhida.zhihu.com/search?content_id=217216597&content_type=Article&match_order=1&q=窗口句柄&zhida_source=entity)和进程实例句柄：

```cpp
VkWin32SurfaceCreateInfoKHR createInfo{};
createInfo.sType = VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR;
createInfo.hwnd = glfwGetWin32Window(window);
createInfo.hinstance = GetModuleHandle(nullptr);
```

glfwGetWin32Window函数可以获取GLFW[窗口对象](https://zhida.zhihu.com/search?content_id=217216597&content_type=Article&match_order=1&q=窗口对象&zhida_source=entity)的Window平台窗口句柄。GetModuleHandle函数可以获取当前进程的实例句柄。

vkCreateWin32SurfaceKHR 函数需要我们自己加载。加载后使用Vulkan 实例，要创建的表面信息，自定义[内存分配器](https://zhida.zhihu.com/search?content_id=217216597&content_type=Article&match_order=1&q=内存分配器&zhida_source=entity)和要存储表面对象的内存地址为参数调用：

```cpp
if (vkCreateWin32SurfaceKHR(instance, &createInfo, nullptr, &surface) != VK_SUCCESS) {
    throw std::runtime_error("failed to create window surface!");
}
```

其它平台的处理方式与之类似，比如Linux平台，可以通过vkCreateXcbSurfaceKHR函数完成表面创建的工作。

GLFW库的glfwCreateWindowSurface函数在不同平台的实现是不同的，可以[跨平台](https://zhida.zhihu.com/search?content_id=217216597&content_type=Article&match_order=1&q=跨平台&zhida_source=entity)使用。现在，我们将它集成到我们的应用程序中。添加一个叫createSurface的函数，然后在InitVulkan 函数中在Vulkan 实例创建和setUpDebugCallBack调用之后调用它：

```cpp
void initVulkan() {
    createInstance();
    setupDebugMessenger();
    createSurface();
    pickPhysicalDevice();
    createLogicalDevice();
}

void createSurface() {
}
```

它的参数依次是VkInstance 对象GLFW窗口指针，自定义内存分配器，存储返回的VkSurface 对象的内存地址。调用后，它会返回VkResult 来指示创建是否成功。表面在应用程序退出需要被清理，GLFW并没有提供清除表面的函数，我们可以自己调用vkDestorySurfaceKHR函数完成这一工作：

```cpp
void cleanup() {
        ...
        vkDestroySurfaceKHR(instance, surface, nullptr);
        vkDestroyInstance(instance, nullptr);
        ...
    }
```

## 查询呈现支持

尽管，具体的Vulkan 实现可能对窗口系统进行了支持，但这并不意味着所有平台的Vulkan 实现都支持同样的特性。所以，我们需要扩展isDeivceSuitable 函数来确保设备可以在我们创建的表面上显示图像。

实际上，支持绘制指令的[队列族](https://zhida.zhihu.com/search?content_id=217216597&content_type=Article&match_order=1&q=队列族&zhida_source=entity)和支持表现的队列族并不一定重叠。 所以，我们需要修改QueueFamilyIndices结构体，添加[成员变量](https://zhida.zhihu.com/search?content_id=217216597&content_type=Article&match_order=1&q=成员变量&zhida_source=entity)存储表现队列族的索引：

```cpp
struct QueueFamilyIndices {
    std::optional<uint32_t> graphicsFamily;
    std::optional<uint32_t> presentFamily;

    bool isComplete() {
        return graphicsFamily.has_value() && presentFamily.has_value();
    }
};
```

接着，我们还需要修改findQueueFamilies 函数，查找带有呈现图像到窗口表面能力的队列族。我们可以在检查队列族是否具有VK_QUEUE_GRAPHICS_BIT 的 同级循环调用vkGetPhysicsDevicesSurfaceSupportKHR 函数来检查物理设备是否具有呈现能力：

```cpp
VkBool32 presentSupport = false;
vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, &presentSupport);
```

然后，根据队列族中的队列数量和是否支持表现确定使用的表现队列族的索引：

```cpp
if (presentSupport) {
    indices.presentFamily = i;
}
```

按照上面的方法最后选择使用的绘制指令队列族和呈现队列族很有可能是同一个队列族。但为了统一操作，即使两者是同一个队列族，我们也按照它们是不同的队列族来对待。实际上，读者可以显式地指定绘制和呈现队列族是同一个的物理设备来提高性能表现。

## 创建呈现队列

现在，我们可以修改逻辑设备的创建过程，创建呈现队列，并将队列句柄保存在成员变量中：

```cpp
VkQueue presentQueue;
```

我们需要多个VkDeviceQueueCreateInfo 结构体来创建所有使用的队列族。一个优雅的处理方式是使用STL的集合创建每一个不同的队列族：

```cpp
#include <set>

...

QueueFamilyIndices indices = findQueueFamilies(physicalDevice);

std::vector<VkDeviceQueueCreateInfo> queueCreateInfos;
std::set<uint32_t> uniqueQueueFamilies = {indices.graphicsFamily.value(), indices.presentFamily.value()};

float queuePriority = 1.0f;
for (uint32_t queueFamily : uniqueQueueFamilies) {
    VkDeviceQueueCreateInfo queueCreateInfo{};
    queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueCreateInfo.queueFamilyIndex = queueFamily;
    queueCreateInfo.queueCount = 1;
    queueCreateInfo.pQueuePriorities = &queuePriority;
    queueCreateInfos.push_back(queueCreateInfo);
}
```

修改VkDevicesCreateInfo结构体的pQueueCreateInfos：

```cpp
createInfo.queueCreateInfoCount = static_cast<uint32_t>(queueCreateInfos.size());
createInfo.pQueueCreateInfos = queueCreateInfos.data();
```

对于同一个队列族，我们只需要传递它的索引一次。最后，调用vkGetDeviceQueue函数获取队列句柄：

```cpp
vkGetDeviceQueue(device, indices.presentFamily.value(), 0, &presentQueue);
```

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)