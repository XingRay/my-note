# Vulkan 三角形之旅-2

> 这里是记录笔者Vulkan的学习记录，参照该教程[Vulkan-tutorial](https://link.zhihu.com/?target=https%3A//vulkan-tutorial.com/Drawing_a_triangle)如果你想识别Vulkan相比于之前的传统图形API有什么区别和优势的话，欢迎看我的另外一篇文章 [初探Vulkan](https://zhuanlan.zhihu.com/p/554631289)。相信应该能够帮助你识别Vulkan的优势所在。

在Vulkan当中绘制一个三角形流程可以分为如下

- 创建一个 VkInstance
- 选择支持的硬件设备（VkPhysicalDevice）
- 创建用于Draw和Presentation的VkDevice 和 VkQueue
- 创建窗口(window)、窗口表面(window surface)和[交换链](https://zhida.zhihu.com/search?content_id=211579363&content_type=Article&match_order=1&q=交换链&zhida_source=entity) (Swap Chain)
- 将Swap Chain Image 包装到 VkImageView
- 创建一个指定Render Target和用途的RenderPass
- 为RenderPass创建FrameBuffer
- 设置PipeLine
- 为每个可能的Swap Chain Image分配并记录带有绘制命令的Command Buffer
- 通过从Swap Chain获取的图像并在上面绘制，提交正确的Commander Buffer，并将绘制完的图像返回到Swap Chain去显示。

## 逻辑设备(logical device)和队列(queue)

在上一部分，我们已经完成了关于Instance和Validation layers和物理设备的设置。在我们选择要使用的物理设备之后，我们需要设置一个逻辑设备(Logical Device)用于交互。逻辑设备创建过程与instance创建过程类似，也需要描述我们需要使用的功能。因为我们已经查询过哪些QueueFamily可用，在这里需要进一步为逻辑设备创建具体类型的Command Queue。如果有不同的需求，也可以基于同一个物理设备创建多个逻辑设备。

逻辑设备(Logical Device)的创建涉及在结构体中再次指定一堆细节，其中第一个将是 VkDeviceQueueCreateInfo。这个结构描述了我们想要的单个QueueFamily的Queue数量。现在我们只对具有图形功能的Queue感兴趣。

```cpp
// 这里是在Mac上运行Vulkan必须添加的一个额外Extension。
const std::vector<const char*> deviceExtensions = {
    // 在mac上的vulkan运行时必须开启该extension
    "VK_KHR_portability_subset",
};

VkQueue graphicsQueue; //用于存储图形queue

void createLogicalDevice() {
    QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
    VkDeviceQueueCreateInfo queueCreateInfo{};
    queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueCreateInfo.queueFamilyIndex = indices.graphicsFamily.value();
    queueCreateInfo.queueCount = 1;
    // Vulkan允许使用0.0到1.0之间的浮点数分配队列优先级来影响Command Buffer执行的调用。
        // 即使只有一个queue也是必须的
    float queuePriority = 1.0f;
    queueCreateInfo.pQueuePriorities = &queuePriority;

        // 需要指定相应的设备要使用的功能特性。这些是我们在上一节中用vkGetPhysicalDeviceFeatures查询支持的功能，
      // 比如Geometry Shaders。现在我们不需要任何特殊的功能，所以我们可以简单的定义它并将所有内容保留到VK_FALSE。
    VkPhysicalDeviceFeatures deviceFeatures{};

    VkDeviceCreateInfo createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
    // 添加指向队列创建信息和相应创建的数量
    createInfo.pQueueCreateInfos = &queueCreateInfo;
    createInfo.queueCreateInfoCount = 1;
    // 指定开启什么feature
    createInfo.pEnabledFeatures = &deviceFeatures;
    // 添加相应的extension
    createInfo.enabledExtensionCount = static_cast<uint32_t>(deviceExtensions.size());
    createInfo.ppEnabledExtensionNames = deviceExtensions.data();
    // 之前的Instance和Device关于Validation Layer的设置是区分的。现在基本只用Instance Validation Layer。
    // 但是为了和旧实现的兼容最好还是写一下
    if (enableValidationLayers) {
        createInfo.enabledLayerCount = static_cast<uint32_t>(validationLayers.size());
        createInfo.ppEnabledLayerNames = validationLayers.data();
    } else {
        createInfo.enabledLayerCount = 0;
    }

    // 正式创建device对象
        // 这里有一个注意的点，逻辑设备不与instance交互，所以参数中不包含instance。
    if (vkCreateDevice(physicalDevice, &createInfo, nullptr, &device) != VK_SUCCESS) {
        throw std::runtime_error("failed to create logical device!");
    }

    // 创建graphicsqueue，因为我们只从这个QueueFamily中创建一个队列，所以我们将简单地使用索引 0。
    vkGetDeviceQueue(device, indices.graphicsFamily.value(), 0, &graphicsQueue);
}
```

在这里我们关于Logic Device的创建就完成啦。那么接下来是关于Window，Window Surface以及Swap Chain的创建了。

## 窗口(window)、窗口表面(window surface)和交换链 (Swap Chain)

到目前为止，我们了解到Vulkan是一个与平台特性无关联的API。它不能直接与window系统进行交互。为了将渲染结果呈现到屏幕，需要建立Vulkan与Window系统之间的连接，我们需要使用WSI(窗体系统集成)Extension。在这里我们将讨论第一个，即**VK_KHR_surface**。它暴露了**VkSurfaceKHR**，它代表Surface的一个抽象类型，用以Present渲染图像使用。我们程序中将要使用的Surface是由我们已经引入的GLFW扩展及其打开的相关Window支持的。简单来说Surface就是Vulkan与Window系统的连接桥梁。

VK_KHR_surface是一个Instance Extension，实际上已经启用了它，它被包含`glfwGetRequiredInstanceExtensions`返回的列表中。其中还包括一些其他的WSI扩展，将在后面使用。

需要在Instance创建之后立即创建Window Surface，因为它会影响物理设备的选择。之所以将Surface[创建逻辑](https://zhida.zhihu.com/search?content_id=211579363&content_type=Article&match_order=1&q=创建逻辑&zhida_source=entity)在这讨论，是因为Window Surface对于渲染、展现方式是一个比较大的课题，如果过早的在创建物理设备加入这部分内容，会混淆基本的物理设备设置工作。另外Window Surface本身对于Vulkan也是非强制的。Vulkan允许这样做，不需要同OpenGL一样必须要创建Window Surface才能够渲染。

```cpp
VkSurfaceKHR surface;

void createSurface(){
      // 创建Surface
            // 使用glfwCreateWindowSurface 函数完全执行此操作，每个平台都有不同的实现。可以帮助我们创建vulkan Surface
            // 其实是相当于vkCreateWin32SurfaceKHR函数。glfw只是帮助我们完成多平台的适配。
      if (glfwCreateWindowSurface(instance, window, nullptr, &surface) != VK_SUCCESS) {
              throw std::runtime_error("failed to create window surface!");
      }
     ......
// 记得要使用完之后清除(在CleanUp函数当中)
vkDestroySurfaceKHR(instance, surface, nullptr);
```

尽管 Vulkan 支持WSI，但这并不意味着该系统中的每个设备都支持它。因此，我们需要扩展 isDeviceSuitable 以确保设备可以将图像呈现到我们创建的Surface。由于Presentation是特定于队列的功能，因此问题实际上是关于找到支持Present我们创建的Surface的QueueFamily。 实际上，支持Graphics和Presentation的QueueFamily可能不重叠。

```cpp
// 对QueueFamilyIndices做如下扩展
struct QueueFamilyIndices {
    std::optional<uint32_t> graphicsFamily;
    std::optional<uint32_t> presentFamily;

    bool isComplete() {
        return graphicsFamily.has_value() && presentFamily.has_value();
    }
};

QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device) {
        QueueFamilyIndices indices;
        uint32_t queueFamilyCount = 0;
        vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, nullptr);
        std::vector<VkQueueFamilyProperties> queueFamilies(queueFamilyCount);
        vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, queueFamilies.data());
        int i = 0;
        for (const auto& queueFamily : queueFamilies) {
            VkBool32 presentSupport = false;
            // 通过vkGetPhysicalDeviceSurfaceSupportKHR检查是否是支持present的queue
            vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, &presentSupport);
            if (presentSupport) {
                indices.presentFamily = i;
            }
            if (queueFamily.queueFlags & VK_QUEUE_GRAPHICS_BIT) {
                indices.graphicsFamily = i;
            }
            if (indices.isComplete()) {
                   break;
            }
            i++;
        }
        return indices;
  }
// 最后我们在createLogicalDevice() 创建presentqueue，因为我们只从这个族中创建一个队列，所以我们将简单地使用索引 0。
vkGetDeviceQueue(device, indices.presentFamily.value(), 0, &presentQueue);
```

到这里我们创建好了我们所需的带有Presentation功能的Queue了。

### Swap Chain

Vulkan没有 "默认帧缓冲区(Default FrameBuffer) "的概念，因此它需要一个基础设施，在我们将缓冲区在屏幕上可视化之前，它将拥有我们要渲染的缓冲区。这个基础设施被称为Swap Chain，必须在Vulkan中明确创建。Swap Chain本质上是一个等待被Present在屏幕上的图像队列。我们的应用程序将获取这样的图像并在图像上进行绘制，然后将其返回到队列中。队列究竟如何工作以及从队列中Present图像的条件取决于Swap Chain是如何设置的，但是Swap Chain的一般目的是使图像的Present与屏幕的刷新率同步。

由于各种原因，并不是所有的显卡都能够直接将图像Present在屏幕上，例如因为它们是为服务器设计的，没有任何显示输出。其次，由于图像呈现与Window系统和与Window Surface有很大的关系，所以它实际上不是Vulkan核心的一部分。你必须在查询设备是否支持该VK_KHR_swapchain Extension，并且显式启用它。

为此，我们首先要扩展isDeviceSuitable函数来检查这个扩展是否被支持。我们之前已经看到了如何添加一个VkPhysicalDevice所支持的扩展，所以这样做应该是相当直接的。请注意，Vulkan头文件提供了一个不错的宏VK_KHR_SWAPCHAIN_EXTENSION_NAME，它被定义为VK_KHR_swapchain。使用这个宏的好处是，编译器会捕捉到错误的拼写。

那么还是Vulkan的老规矩。在使用之前，我们还需要验证这个Device支不支持这个关于Swap Chain扩展，并且显式启用(Vulkan 不把这个放进去核心包也是离谱，为啥还是Extension)。

```cpp
const std::vector<const char*> deviceExtensions = {
    // 添加关于SwapChain的extension
    VK_KHR_SWAPCHAIN_EXTENSION_NAME,
    // 在mac上的vulkan运行时必须开启该extension
    "VK_KHR_portability_subset",
};

bool checkDeviceExtensionSupport(VkPhysicalDevice device) {
      // 检查device extension数量
      uint32_t extensionCount;
      vkEnumerateDeviceExtensionProperties(device, nullptr, &extensionCount, nullptr);
      // 检查device extension 数组
      std::vector<VkExtensionProperties> availableExtensions(extensionCount);
      vkEnumerateDeviceExtensionProperties(device, nullptr, &extensionCount, availableExtensions.data());
      std::set<std::string> requiredExtensions(deviceExtensions.begin(), deviceExtensions.end());
         // 查询想要的Extension是不是都有。
            for (const auto& extension : availableExtensions) {
          requiredExtensions.erase(extension.extensionName);
      }
      return requiredExtensions.empty();
  }

// 在createLogicalDevice添加相应的Extension
createInfo.enabledExtensionCount = static_cast<uint32_t>(deviceExtensions.size());
createInfo.ppEnabledExtensionNames = deviceExtensions.data();
```

仅仅检查一个Swap Chain是否可用是不够的，因为它可能实际上与我们的Window Surface不兼容。创建Swap Chain还涉及到比创建Instance和Device更多的设置，所以在我们能够继续下去之前，我们需要更多的信息。

基本上有三种属性是我们需要检查的：

- 基本Surface能力(Swap Chain中图像的最小/最大数量，图像的最小/最大宽度和高度)。
- Surface格式(像素格式、使用什么色彩空间)。
- 使用什么Presentation Mode。

与 findQueueFamilies 类似，我们将使用一个结构体来传递这些细节，一旦它们被查询到。上述三种类型的属性是以下列结构的形式出现的。

```cpp
struct SwapChainSupportDetails {
    VkSurfaceCapabilitiesKHR capabilities;
    std::vector<VkSurfaceFormatKHR> formats;
    std::vector<VkPresentModeKHR> presentModes;
};
```

接下来就来查询这些信息吧

```cpp
SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device) {
    SwapChainSupportDetails details;
    // 通过vkGetPhysicalDeviceSurfaceCapabilitiesKHR确定swapChain支持的能力
    vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, &details.capabilities);
    // 确定Surface支持几种格式
    uint32_t formatCount;
    vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, &formatCount, nullptr);
    if (formatCount != 0) {
        details.formats.resize(formatCount);
        // 获取所有支持的格式
        vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, &formatCount, details.formats.data());
    }

    // 查询支持几种Presentation Mode
    uint32_t presentModeCount;
    vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, &presentModeCount, nullptr);

    if (presentModeCount != 0) {
        details.presentModes.resize(presentModeCount);
        // 获取所有的Presentation Mode
        vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, &presentModeCount, details.presentModes.data());
    }
    return details;
}
```

然后接下来改造isDeviceSuitable函数添加Swap Chain相关。

```cpp
bool isDeviceSuitable(VkPhysicalDevice device) {
        QueueFamilyIndices indices = findQueueFamilies(device);
        bool extensionsSupported = checkDeviceExtensionSupport(device);

        // 验证swapChain是否可用
        bool swapChainAdequate = false;
        if (extensionsSupported) {
            SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device);
                        // 只需要一种像素格式和Present Mode在这里就足够使用啦
            swapChainAdequate = !swapChainSupport.formats.empty() && !swapChainSupport.presentModes.empty();
        }
        return indices.isComplete() && extensionsSupported && swapChainAdequate;
    }
```

在这里我们已经获取到了我们所需要的所有细节，满足Swap Chain的需求是肯定够了。但可能仍然有许多不同的最优模式。我们现在将编写几个函数来找到最佳Swap Chain的设置。先从Surface Format开始。

```cpp
// 每个 VkSurfaceFormatKHR 条目都包含一个格式和一个颜色空间成员。
VkSurfaceFormatKHR chooseSwapSurfaceFormat(const std::vector<VkSurfaceFormatKHR>& availableFormats) {
      // 选择最佳的格式和颜色空间
      for (const auto& availableFormat : availableFormats) {
                // VK_FORMAT_B8G8R8A8_SRGB 表示我们以 8 位无符号整数的顺序存储 B、G、R 和 alpha 通道，每个像素总共 32 位。 
                // VK_COLOR_SPACE_SRGB_NONLINEAR_KHR 标志指示是否支持 SRGB 颜色空间。
          if (availableFormat.format == VK_FORMAT_B8G8R8A8_SRGB && availableFormat.colorSpace == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
              return availableFormat;
          }
      }
      return availableFormats[0];
}
```

Presentation Mode可以说是Swap Chain最重要的设置，因为它代表了将图像Present到屏幕的实际条件。 Vulkan 中有四种可能的模式：

- VK_PRESENT_MODE_IMMEDIATE_KHR：您的应用程序提交的图像会立即转移到屏幕上，这可能会导致画面撕裂。
- VK_PRESENT_MODE_FIFO_KHR：Swap Chain是一个队列，当显示器刷新时，显示器从队列前面获取图像，程序在队列后面插入渲染出的图像。如果队列已满，则程序必须等待。这与现代游戏中的[垂直同步](https://zhida.zhihu.com/search?content_id=211579363&content_type=Article&match_order=1&q=垂直同步&zhida_source=entity)最为相似。刷新显示的那一刻称为“垂直空白(vertical blank)”。
- VK_PRESENT_MODE_FIFO_RELAXED_KHR: 这个模式只有在应用程序有延迟，并且在最后一次垂直空白时队列是空的情况下才与前一个模式不同。图像不是等待下一个垂直空白，而是在它最终到达时立即传输。这可能会导致可见的[画面撕裂](https://zhida.zhihu.com/search?content_id=211579363&content_type=Article&match_order=2&q=画面撕裂&zhida_source=entity)现象。
- VK_PRESENT_MODE_MAILBOX_KHR：这是第二种模式的另一种变体。队列已满时不会阻塞应用程序，而是将已排队的图像简单地替换为较新的图像。此模式可用于尽可能快地渲染帧，同时仍避免画面撕裂，与标准垂直同步相比，延迟问题更少。这就是俗称的“[三重缓冲](https://zhida.zhihu.com/search?content_id=211579363&content_type=Article&match_order=1&q=三重缓冲&zhida_source=entity)(triple buffering)”，尽管仅有三个缓冲区的存在并不一定意味着帧率的解锁。

只有 VK_PRESENT_MODE_FIFO_KHR 模式先保证可用，所以我们必须再次编写一个函数来寻找可用的最佳模式：

```cpp
VkPresentModeKHR chooseSwapPresentMode(const std::vector<VkPresentModeKHR>& availablePresentModes) {
    for (const auto& availablePresentMode : availablePresentModes) {
                // 但是在条件允许的情况下，能用三重缓冲还是三重缓冲吧
                // 它允许我们通过渲染尽可能最新的新图像直到垂直空白来避免撕裂，同时仍然保持相当低的延迟。
        if (availablePresentMode == VK_PRESENT_MODE_MAILBOX_KHR) {
            return availablePresentMode;
        }
    }

    return VK_PRESENT_MODE_FIFO_KHR;
}
```

设置完了Present Mode之后，接着往下看就是Swap Extent。是Swap Extent是Swap Chain Image的分辨率，它几乎总是完全等于我们创建Window的分辨率。Swap Extent在 VkSurfaceCapabilitiesKHR 结构中定义。 Vulkan 告诉我们通过在 currentExtent 成员中设置宽度和高度来匹配Window分辨率。然而，一些Window管理器确实允许我们在这里有所不同，这通过将 currentExtent 中的宽度和高度设置为一个特殊值来表示：uint32_t 的最大值。在这种情况下，我们将在 minImageExtent 和 maxImageExtent 范围内选择与窗口最匹配的分辨率。但是我们必须以正确的单位指定分辨率。

GLFW 在测量尺寸时使用两个单位：像素和屏幕坐标。例如，我们之前在创建窗口时指定的分辨率WIDTH, HEIGHT是在屏幕坐标中测量的。但是 Vulkan 使用像素，因此Swap extent也必须以像素为单位指定。不幸的是，如果您使用的是高 DPI 显示器（如 Apple 的 Retina 显示器），屏幕坐标与像素不对应。相反，由于更高的像素密度，以像素为单位的Window分辨率将大于以屏幕坐标为单位的分辨率。因此，如果 Vulkan 没有为我们修复Swap extent，我们就不能只使用原始的 {WIDTH, HEIGHT}。相反，我们必须使用 glfwGetFramebufferSize 来查询Window的分辨率（以像素为单位），然后再将其与最小和最大图像范围进行匹配。

```cpp
VkExtent2D chooseSwapExtent(const VkSurfaceCapabilitiesKHR& capabilities) {
    if (capabilities.currentExtent.width != std::numeric_limits<uint32_t>::max()) {
            return capabilities.currentExtent;
    } else {
        int width, height;
        glfwGetFramebufferSize(window, &width, &height);

        VkExtent2D actualExtent = {
            static_cast<uint32_t>(width),
            static_cast<uint32_t>(height)
        };

        actualExtent.width = std::clamp(actualExtent.width, capabilities.minImageExtent.width, capabilities.maxImageExtent.width);
        actualExtent.height = std::clamp(actualExtent.height, capabilities.minImageExtent.height, capabilities.maxImageExtent.height);

        return actualExtent;
    }
}
```

现在我们有了创建Swap Chain所需的所有信息。那么接下来让我们来创建Swap Chain吧

```cpp
void createSwapChain() {
    // 获取SwapChain相关属性。
    SwapChainSupportDetails swapChainSupport = querySwapChainSupport(physicalDevice);
    VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
    VkPresentModeKHR presentMode = chooseSwapPresentMode(swapChainSupport.presentModes);
    VkExtent2D extent = chooseSwapExtent(swapChainSupport.capabilities);
    // 简单地坚持这个最小值意味着我们有时可能不得不等待驱动程序完成内部操作，然后才能获取另一个图像进行渲染。
        // 因此，我们建议至少要比最小值多请求一个图像。
    uint32_t imageCount = swapChainSupport.capabilities.minImageCount + 1;
    // 同样要保证做这个事情的时候不要超过最大数量
    if (swapChainSupport.capabilities.maxImageCount > 0 && imageCount > swapChainSupport.capabilities.maxImageCount) {
        imageCount = swapChainSupport.capabilities.maxImageCount;
    }
        // Vulkan老传统了，创建Swap Chain也需要填充一个大型结构体。
    VkSwapchainCreateInfoKHR createInfo{};
    createInfo.sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
    createInfo.surface = surface;
    createInfo.minImageCount = imageCount;
    createInfo.imageFormat = surfaceFormat.format;
    createInfo.imageColorSpace = surfaceFormat.colorSpace;
    createInfo.imageExtent = extent;
    // imageArrayLayers指定了每个图像所包含的层的数量,这里只需要一层
    createInfo.imageArrayLayers = 1;
        // imageUsage 字段指定我们将使用Swap Chain中的图像进行何种操作。
    // VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT 使用内存操作将渲染出的图像转移到swap Chain的图像上
        // 难在这里就是简单的作为Color Attachment
    createInfo.imageUsage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;

        // 我们需要指定如何处理将跨多个QueueFamily使用的Swap Chain Image。
        // 如果graphicsQueue与presentQueue的队列族不同，我们需要做不同的处理。
    QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
    uint32_t queueFamilyIndices[] = {indices.graphicsFamily.value(), indices.presentFamily.value()};

    if (indices.graphicsFamily != indices.presentFamily) {
        // 一个Image一次由一个queue拥有，在另一个queue中使用该图像之前必须明确转移所有权。这个选项提供了最好的性能。
        createInfo.imageSharingMode = VK_SHARING_MODE_CONCURRENT;
        createInfo.queueFamilyIndexCount = 2;
        createInfo.pQueueFamilyIndices = queueFamilyIndices;
    } else {
        //  图像可以在多个queue中使用，不需要明确的所有权转移。
        createInfo.imageSharingMode = VK_SHARING_MODE_EXCLUSIVE;
        createInfo.queueFamilyIndexCount = 0; // Optional
        createInfo.pQueueFamilyIndices = nullptr; // Optional
    }
    // 指定一些图像的转换。比如顺时针旋转90度或水平翻转。要指定你不想要任何变换，只需指定当前的变换。
    createInfo.preTransform = swapChainSupport.capabilities.currentTransform;

    // compositeAlpha字段指定是否应该使用alpha通道与Window系统中的其他Window进行混合。
        // 你几乎总是想简单地忽略alpha通道，因此VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR。
    createInfo.compositeAlpha = VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;

    createInfo.presentMode = presentMode;
    // 被设置为VK_TRUE，那么这意味着我们不关心那些被遮挡的像素的颜色，比如说因为另一个窗口在它们前面。
        // 除非你真的需要能够读回这些像素并得到可预测的结果，否则你将通过启用Clip获得最佳性能。
    createInfo.clipped = VK_TRUE;
    // 在Vulkan中Swap Chain有可能在你的应用程序运行时变得无效或未被优化，
        // 例如因为Window被调整了大小。在这种情况下，Swap Chain实际上需要从头开始创建，必须在这个字段中指定对旧Swap Chain的引用。
    // 现在我们假设我们只创建一个Swap Chain。
    createInfo.oldSwapchain = VK_NULL_HANDLE;

        // 创建 VkSwapchainKHR对象。
    if (vkCreateSwapchainKHR(device, &createInfo, nullptr, &swapChain) != VK_SUCCESS) {
        throw std::runtime_error("failed to create swap chain!");
    }
}
```

在上面我们就完成了关于Swap Chain的创建，所以剩下的就是检索其中的 VkImage 。在后续的渲染操作中将引用这些Image。添加一个类成员来存储这些：

```cpp
VkSwapchainKHR swapChain;
std::vector<VkImage> swapChainImages;
VkFormat swapChainImageFormat;
VkExtent2D swapChainExtent;

// 这些图像是由Swap Chain的实现创建的，一旦Swap Chain被销毁，它们将被自动清理，因此我们不需要添加任何清理代码。
// 我在createSwapChain函数的末尾添加了检索图像的代码，就在vkCreateSwapchainKHR调用之后。
// 检索它们与我们从Vulkan中检索对象数组的其他时候非常相似。
// 请记住，我们只指定了Swap Chain中图像的最低数量，所以实现允许创建一个有更多图像的Swap Chain。
// 这就是为什么我们要先用vkGetSwapchainImagesKHR查询最终的图像数量，然后调整容器的大小，最后再调用它来检索。

vkGetSwapchainImagesKHR(device, swapChain, &imageCount, nullptr);
swapChainImages.resize(imageCount);
vkGetSwapchainImagesKHR(device, swapChain, &imageCount, swapChainImages.data());

// 同时还需要将这个格式和分辨率保存下来。
swapChainImageFormat = surfaceFormat.format;
swapChainExtent = extent;
```

在这里关于Swap Chain创建就全部完成啦。千辛万苦终于搞定了。创建一个Swap Chain真不容易啊。在Metal中这些基本都有一个View来帮你掩盖掉这些细节。

## 将Image 包装到 VkImageView

为了在PipeLine中使用任何VkImage，包括Swap Chain中的那些Image，我们必须创建一个VkImageView对象。ImageView实际上是对图像的一种观察。它描述了如何访问图像以及访问图像的哪一部分，我们必须通过ImageView来能够读取Image。例如，如果它应该被当作一个没有任何mipmapping级别的2D纹理。

在这里中，我们将编写一个createImageViews函数，为Swap Chain中的每个Image创建一个基本的ImageView，这样我们以后就可以把它们作为Color Target。

```cpp
// 保存imageView
std::vector<VkImageView> swapChainImageViews;
void createImageViews() {
    swapChainImageViews.resize(swapChainImages.size());
    for (size_t i = 0; i < swapChainImages.size(); i++) {
                // 用于创建图像视图的参数在 VkImageViewCreateInfo 结构中指定。
        VkImageViewCreateInfo createInfo{};
        createInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
        createInfo.image = swapChainImages[i];
        // viewType 和格式字段指定应如何解释图像数据。
        createInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;
        createInfo.format = swapChainImageFormat;
        // components字段允许您调整颜色通道。例如，你可以把所有的通道都映射到R通道上，形成一个单色纹理。你也可以将0和1的常量值映射到一个通道。在我们的例子中，我们将坚持使用默认的映射。
        createInfo.components.r = VK_COMPONENT_SWIZZLE_IDENTITY;
        createInfo.components.g = VK_COMPONENT_SWIZZLE_IDENTITY;
        createInfo.components.b = VK_COMPONENT_SWIZZLE_IDENTITY;
        createInfo.components.a = VK_COMPONENT_SWIZZLE_IDENTITY;
        // subresourceRange 字段描述了图像的用途以及应该访问图像的哪一部分。我们的图像将用作Color Target，没有任何 mipmapping 级别或多层。
        createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        createInfo.subresourceRange.baseMipLevel = 0;
        createInfo.subresourceRange.levelCount = 1;
        createInfo.subresourceRange.baseArrayLayer = 0;
        createInfo.subresourceRange.layerCount = 1;
        // 创建ImageView
        if (vkCreateImageView(device, &createInfo, nullptr, &swapChainImageViews[i]) != VK_SUCCESS) {
            throw std::runtime_error("failed to create image views!");
        }
    }
}
```

在这里我们完成了关于Window的设置，并且还提到一个关键的概念:Swap Chain。这关系到我们渲染出的图像显示到屏幕的细节。这些细节在Vulkan当中是完全放开的，再往下就是PipeLine的设置。