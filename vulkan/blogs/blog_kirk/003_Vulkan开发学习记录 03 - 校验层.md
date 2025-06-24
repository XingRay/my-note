# Vulkan开发学习记录 03 - 校验层

## 简述

Vulkan API 的设计是紧紧围绕最小化[驱动程序](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=驱动程序&zhida_source=entity)开销进行的，所以，默认情况下，Vulkan API 提供的错误检查功能非常有限。很多很基本的错误都没有被 Vulkan 显式地处理，遇到错误程序会直接崩溃或者发生未被明确定义的行为。Vulkan 需要我们显式地定义每一个操作，所以就很容易在使用过程中产生一些小错误，比如使用了一个新的GPU 特性，却忘记在逻辑设备创建时请求这一特性。

然而，这并不意味着我们不能将错误检查加入 API 调用。Vulkan 引入了校验层来优雅地解决这个问题。校验层是一个可选的可以用来在 Vulkan API [函数调用](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=函数调用&zhida_source=entity)上进行附加操作的组件。

## 校验层

校验层常被用来做下面的工作：

• 检测参数值是否合法

• 追踪对象的创建和清除操作，发现资源泄漏问题

• 追踪调用来自的线程，检测是否[线程安全](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=线程安全&zhida_source=entity)

• 将 API 调用和调用的参数写入日志

• 追踪 API 调用进行分析和回放

下面的代码演示了 Vulkan 的校验层是如何工作的：

```cpp
VkResult vkCreateInstance(
    const VkInstanceCreateInfo* pCreateInfo,
    const VkAllocationCallbacks* pAllocator,
    VkInstance* instance) {

    if (pCreateInfo == nullptr || instance == nullptr) {
        log("Null pointer passed to required parameter!");
        return VK_ERROR_INITIALIZATION_FAILED;
    }

    return real_vkCreateInstance(pCreateInfo, pAllocator, instance);
}
```

校验层可以被自由堆叠包含任何读者感兴趣的调试功能。我们可以在开发时使用校验层，然后在发布应用程序时，禁用校验层来提高程序的运行表现。

Vulkan 库本身并没有提供任何内建的校验层，但 LunarG Vulkan SDK 供了一个非常不错的校验层实现。读者可以使用这个校验层实现来保证自 己的应用程序在不同的驱动程序下能够尽可能得表现一致，而不是依赖于某个驱动程序的未定义行为。

校验层只能用于安装了它们的系统，比如，LunarG 的校验层只可以在安装了Vulkan SDK 的 PC 上使用。

Vulkan 可以使用两种不同类型的校验层：实例校验层和[设备校验](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=设备校验&zhida_source=entity)层。 实例校验层只检查和全局Vulkan对象相关的调用，比如Vulkan 实例。设备校验层只检查和特定 GPU 相关的调用。设备校验层现在已经不推荐使用， 也就是说，应该使用实例校验层来检测所有 Vulkan 调用。Vulkan 规范文档为了兼容性仍推荐启用设备校验层。在本教程，为了简便，我们为实例和设备指定相同的校验层。

## 使用校验层

我们将使用LunarG Vulkan SDK 提供的校验层。和使用扩展一样，使用校验层需要指定校验层的名称。LunarG Vulkan SDK 允许我们通过 VK_LAYER_KHRONOS_validation 来隐式地开启所有可用的校验 层。

首先，让我们添加两个变量到程序中来控制是否启用指定的校验层。 这里，我们通过[条件编译](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=条件编译&zhida_source=entity)来设定是否启用校验层。代码中的NDEBUG 是C++标准的一部分，表示是否处于非调试模式下：

```cpp
const uint32_t WIDTH = 800;
const uint32_t HEIGHT = 600;

const std::vector<const char*> validationLayers = {
    "VK_LAYER_KHRONOS_validation"
};

#ifdef NDEBUG
    const bool enableValidationLayers = false;
#else
    const bool enableValidationLayers = true;
#endif
```

接着，我们添加了一个叫做 checkValidationLayerSupport 的函数来请求所有可用的校验层。首先，我们调用vkEnumerateInstanceLayerProperties 函数获取了所有可用的校验层列表。这一函数的用法和前面我们在创建Vulkan 实例中使用的vkEnumerateInstanceExtensionProperties 函数相同。

```cpp
bool checkValidationLayerSupport() {
    uint32_t layerCount;
    vkEnumerateInstanceLayerProperties(&layerCount, nullptr);

    std::vector<VkLayerProperties> availableLayers(layerCount);
    vkEnumerateInstanceLayerProperties(&layerCount, availableLayers.data());

    return false;
}
```

接着，检查是否所有validationLayers 列表中的校验层都可以在availableLayers 列表中找到：

```cpp
for (const char* layerName : validationLayers) {
    bool layerFound = false;

    for (const auto& layerProperties : availableLayers) {
        if (strcmp(layerName, layerProperties.layerName) == 0) {
            layerFound = true;
            break;
        }
    }

    if (!layerFound) {
        return false;
    }
}

return true;
```

现在，我们在createInstance 函数中调用它：

```cpp
void createInstance() {
    if (enableValidationLayers && !checkValidationLayerSupport()) {
        throw std::runtime_error("validation layers requested, but not available!");
    }

    ...
}
```

现在，在调试模式下编译运行程序，确保没有错误出现。如果程序运行时出现错误，请确保正确安装了Vulkan SDK。如果程序报告缺少可用的校验层，可以查阅LunarG Vulkan SDK 的官方文档寻找解决方法。 最后，修改我们之前的填写的VkInstanceCreateInfo结构体信息，在校验层启用时使用校验层：

```cpp
if (enableValidationLayers) {
    createInfo.enabledLayerCount = static_cast<uint32_t>(validationLayers.size());
    createInfo.ppEnabledLayerNames = validationLayers.data();
} else {
    createInfo.enabledLayerCount = 0;
}
```

如果校验层检查成功，vkCreateInstance 函数调用就不会返回VK_ERROR_LAYER_NOT_PRESENT 一错误代码，但为了保险起见，读者应该运行程序来确保没有问题出现。

## 消息回调

仅仅启用校验层并没有任何用处，我们不能得到任何有用的调试信息。 为了获得调试信息，我们需要使用VK_EXT_debug_utils 扩展，设置[回调函数](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=回调函数&zhida_source=entity)来接受调试信息。 我们添加了一个叫getRequiredExtensions 的函数，这一函数根据是否启用校验层，返回所需的扩展列表：

```cpp
std::vector<const char*> getRequiredExtensions() {
    uint32_t glfwExtensionCount = 0;
    const char** glfwExtensions;
    glfwExtensions = glfwGetRequiredInstanceExtensions(&glfwExtensionCount);

    std::vector<const char*> extensions(glfwExtensions, glfwExtensions + glfwExtensionCount);

    if (enableValidationLayers) {
        extensions.push_back(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
    }

    return extensions;
}
```

GLFW 指定的扩展是必需的，调试报告相关的扩展根据校验层是否启用添加。代码中我们使用了一个VK_EXT_DEBUG_UTILS_EXTENSION_NAME， 它等价于VK_EXT_debug_utils，使用它是为了避免打字时的手误。 现在，我们在createInstance 函数中调用这一函数：

```cpp
auto extensions = getRequiredExtensions();
createInfo.enabledExtensionCount = static_cast<uint32_t>(extensions.size());
createInfo.ppEnabledExtensionNames = extensions.data();
```

接着，编译运行程序，确保没有出现VK_ERROR_EXTENSION_NOT_PRESENT 错误。校验层的可用已经隐含说明对应的扩展存在，所以我们不需要额外去做扩展是否存在的检查。 现在，让我们来看接受调试信息的回调函数。我们在程序中以vkDebugUtilsMessengerCallbackEXT 为原型添加一个叫做debugCallback 的[静态函数](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=静态函数&zhida_source=entity)。这一函数使用VKAPI_ATTR 和VKAPI_CALL 定 义，确保它可以被Vulkan 库调用。

```cpp
static VKAPI_ATTR VkBool32 VKAPI_CALL debugCallback(
    VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
    VkDebugUtilsMessageTypeFlagsEXT messageType,
    const VkDebugUtilsMessengerCallbackDataEXT* pCallbackData,
    void* pUserData) {

    std::cerr << "validation layer: " << pCallbackData->pMessage << std::endl;

    return VK_FALSE;
}
```

函数的第一个参数指定了消息的级别，它可以是下面这些值：

• VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT：诊断信息

• VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT：资源创建之类的信息

• VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT：警告信息

• VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT：不合法和可能造成崩溃的操作信息

这些值经过一定设计，可以使用[比较运算符](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=比较运算符&zhida_source=entity)来过滤处理一定级别以上的调试信息：

```cpp
if (messageSeverity >= VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT) {
    // Message is important enough to show
}
```

messageType 参数可以是下面这些值：

• VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT：发生了一些与规范和性能无关的事件

• VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT：出现了违反规范的情况或发生了一个可能的错误

• VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT： 进行了可能影响Vulkan 性能的行为

pCallbackData 参数是一个指向VkDebugUtilsMessengerCallbackDataEXT 结构体的指针，这一结构体包含了下面这些非常重要的成员：

• pMessage：一个以扮扵扬扬结尾的包含调试信息的字符串

• pObjects：存储有和消息相关的扖扵扬扫扡扮对象句柄的数组

• objectCount：数组中的对象个数

最后一个参数pUserData 是一个指向了我们设置回调函数时，传递的数据的指针。

回调函数返回了一个[布尔值](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=布尔值&zhida_source=entity)，用来表示引发校验层处理的Vulkan API 用是否被中断。如果返回值为true，对应Vulkan API 调用就会返回VK_ERROR_VALIDATION_FAILED_EXT 错误代码。通常，只在测试校验层本身时会返回true，其余情况下，回调函数 应该返回VK_FALSE。

定义完回调函数，接下来要做的就是设置Vulkan 使用这一回调函数。 我们需要一个vkDebugUtilsMessengerCallbackEXT 对象来存储回调函数信息，然后将它提交给Vulkan 完成回调函数的设置：

```cpp
VkDebugUtilsMessengerCallbackEXT callback
```

现在，我们在initVulkan 函数中，在createInstance 函数调用之后添加一个setupDebugCallback 函数调用：

```cpp
void initVulkan() {
    createInstance();
    setupDebugMessenger();
}

void setupDebugMessenger() {
    if (!enableValidationLayers) return;

}
```

接着，我们需要填写vkDebugUtilsMessengerCallbackEXT 结构体所需的信息：

```cpp
VkDebugUtilsMessengerCreateInfoEXT createInfo{};
createInfo.sType = VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
createInfo.messageSeverity = VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
createInfo.messageType = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
createInfo.pfnUserCallback = debugCallback;
createInfo.pUserData = nullptr; // Optional
```

messageSeverity 域可以用来指定回调函数处理的消息级别。在这里，我们设置回调函数处理除了VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT外的所有级别的消息，这使得我们的回调函数可以接收到可能的问题信息， 同时忽略掉冗长的一般调试信息。

messageType域用来指定回调函数处理的消息类型。在这里，我们设置处理所有类型的消息。读者可以根据自己的需要开启和禁用处理的消息类 型。

pfnUserCallback域是一个指向回调函数的指针。pUserData 是一个指向用户自定义数据的指针，它是可选的，这个指针所指的地址会被作为回 调函数的参数，用来向回调函数传递用户数据。

有许多方式配置校验层消息和回调，更多信息可以参考扩展的规范文档。

填写完结构体信息后，我们将它作为一个参数调用vkCreateDebugUtilsMessengerEXT 对象。由于vkCreateDebugUtilsMessengerEXT 函数是一个[扩展函数](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=1&q=扩展函数&zhida_source=entity)，不会被Vulkan 库自动加载，所以需要我们自己使用vkGetInstance 函数来加载它。在这里，我们创建了一个代理函数，来载入vkCreateDebugUtilsMessengerEXT函数：

```cpp
VkResult CreateDebugUtilsMessengerEXT(VkInstance instance, const VkDebugUtilsMessengerCreateInfoEXT* pCreateInfo, const VkAllocationCallbacks* pAllocator, VkDebugUtilsMessengerEXT* pDebugMessenger) {
    auto func = (PFN_vkCreateDebugUtilsMessengerEXT) vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT");
    if (func != nullptr) {
        return func(instance, pCreateInfo, pAllocator, pDebugMessenger);
    } else {
        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }
}
```

vkGetInstanceProcAddr 函数如果不能被加载，那么我们的[代理函数](https://zhida.zhihu.com/search?content_id=216693875&content_type=Article&match_order=2&q=代理函数&zhida_source=entity)就会发挥nullptr。现在我们可以使用这个代理函数来创建扩展对象：

```cpp
if (CreateDebugUtilsMessengerEXT(instance, &createInfo, nullptr, &debugMessenger) != VK_SUCCESS) {
    throw std::runtime_error("failed to set up debug messenger!");
}
```

函数的第二个参数是可选的分配器回调函数，我们没有自定义的分配器，所以将其设置为nullptr。由于我们的调试回调是针对特定Vulkan 实例和它的校验层，所以需要在第一个参数指定调试回调作用的Vulkan 实例。

VkDebugUtilsMessengerEXT 对象在程序结束前通过调用vkDestroyDebugUtilsMessengerEXT 函数来清除掉。 和vkCreateDebugUtilsMessengerEXT 函数相同，Vulkan 库没有自动加载这个函数，需要我们自己加载它。控制台窗口出现多次相同的错误信息是正常的，这是因为有多个校验层检查发现了这个问题。 现在，让我们创建CreateDebugUtilsMessengerEXT 函数的代理函数:

```cpp
void DestroyDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerEXT debugMessenger, const VkAllocationCallbacks* pAllocator) {
    auto func = (PFN_vkDestroyDebugUtilsMessengerEXT) vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT");
    if (func != nullptr) {
        func(instance, debugMessenger, pAllocator);
    }
}
```

这个代理函数需要被定义为类的静态成员函数或者被定义在类之外。 我们在CleanUp 函数中调用这个函数：

```cpp
void cleanup() {
    if (enableValidationLayers) {
        DestroyDebugUtilsMessengerEXT(instance, debugMessenger, nullptr);
    }

    vkDestroyInstance(instance, nullptr);

    glfwDestroyWindow(window);

    glfwTerminate();
}
```

现在，再次编译运行程序，如果一切顺利，错误信息这次就不会出现。 如果读者想要了解到底是哪个函数调用引发了错误消息，可以在处理消息 的回调函数设置断点，然后运行程序，观察程序在断点位置时的调用栈， 就可以确定引发错误消息的函数调用。

