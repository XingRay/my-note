# 【Vulkan 入门系列】创建 Vulkan 实例、Surface 和选择物理设备（二）

使用 Vulkan API 的第一步是先创建它的实例，并搭建 Surface 和创建逻辑设备。我们通过学习 Android Hello VK Demo 来一步步熟悉 Vulkan API。

## 一、Hello VK Demo

Hello VK 是一个用于绘制简单 Hello World 三角形的 Android C++ 示例。

源码 URL：https://github.com/android/getting-started-with-vulkan-on-android-codelab.git

Vulkan 使用的核心代码被封装到 HelloVK 类中，对外接口包括初始化 Vulkan、渲染、清理资源、清理交换链和重置五个方法，使用 Vulkan 绘制三角形主要就是调用这个五个方法实现。

代码结构如下：

hellovk.h

```
class HelloVK {
public:
void initVulkan();
void render();
void cleanup();
void cleanupSwapChain();
void reset(ANativeWindow *newWindow, AAssetManager *newManager);
bool initialized = false;

private:
  ...
}
```

## 二、创建实例

创建实例是初始化 Vulkan 初始化的第一步，接下来详细分析 `createInstance` 函数都做了什么？Vulkan 实例是管理 Vulkan 应用程序状态的核心对象，是所有其他 Vulkan 对象的基础。

hellovk.h

```
void HelloVK::initVulkan() {
createInstance();
  ...
}

void HelloVK::createInstance() {
assert(!enableValidationLayers ||
         checkValidationLayerSupport());  // validation layers requested, but
                                          // not available!
auto requiredExtensions = getRequiredExtensions(enableValidationLayers);

  VkApplicationInfo appInfo{};
  appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
  appInfo.pApplicationName = "Hello Triangle";
  appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
  appInfo.pEngineName = "No Engine";
  appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
// appInfo.apiVersion = VK_API_VERSION_1_0;
  appInfo.apiVersion = VK_API_VERSION_1_1;

  VkInstanceCreateInfo createInfo{};
  createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
  createInfo.pApplicationInfo = &appInfo;
  createInfo.enabledExtensionCount = (uint32_t)requiredExtensions.size();
  createInfo.ppEnabledExtensionNames = requiredExtensions.data();
  createInfo.pApplicationInfo = &appInfo;

if (enableValidationLayers) {
    VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo{};
    createInfo.enabledLayerCount =
        static_cast<uint32_t>(validationLayers.size());
    createInfo.ppEnabledLayerNames = validationLayers.data();
    populateDebugMessengerCreateInfo(debugCreateInfo);
    createInfo.pNext = (VkDebugUtilsMessengerCreateInfoEXT *)&debugCreateInfo;
  } else {
    createInfo.enabledLayerCount = 0;
    createInfo.pNext = nullptr;
  }
VK_CHECK(vkCreateInstance(&createInfo, nullptr, &instance));

uint32_t extensionCount = 0;
vkEnumerateInstanceExtensionProperties(nullptr, &extensionCount, nullptr);
std::vector<VkExtensionProperties> extensions(extensionCount);
vkEnumerateInstanceExtensionProperties(nullptr, &extensionCount,
                                         extensions.data());
LOGI("available extensions");
for (constauto &extension : extensions) {
    LOGI("\t %s", extension.extensionName);
  }
}
```

### 2.1 验证层检查

确保当启用验证层（`enableValidationLayers` 为 `true`）时，系统支持请求的验证层。

```
assert(!enableValidationLayers || checkValidationLayerSupport());
```

如果 `enableValidationLayers` 为 `true`，则 `checkValidationLayerSupport()` 必须返回 `true`，否则断言失败。验证层用于调试 Vulkan 应用程序，例如检查 API 误用或内存泄漏。如果系统不支持请求的验证层，程序会终止。

```
const std::vector<constchar *> validationLayers = {
      "VK_LAYER_KHRONOS_validation"};
      
bool HelloVK::checkValidationLayerSupport() {
uint32_t layerCount;
vkEnumerateInstanceLayerProperties(&layerCount, nullptr);

std::vector<VkLayerProperties> availableLayers(layerCount);
vkEnumerateInstanceLayerProperties(&layerCount, availableLayers.data());

for (constchar *layerName : validationLayers) {
    bool layerFound = false;
    for (constauto &layerProperties : availableLayers) {
      if (strcmp(layerName, layerProperties.layerName) == 0) {
        layerFound = true;
        break;
      }
    }

    if (!layerFound) {
      returnfalse;
    }
  }
returntrue;
}
```

1. 调用 `vkEnumerateInstanceLayerProperties` 函数获取系统中可用的 Vulkan 层数量。
2. 再次调用 `vkEnumerateInstanceLayerProperties`，但这次传入预先分配好的 `availableLayers` 容器，获取所有可用层的详细信息。
3. 遍历指定的验证层列表 `validationLayers`（`"VK_LAYER_KHRONOS_validation"`）。
4. 对于每个需要的层，遍历系统中所有可用层（`availableLayers`），通过 `strcmp` 比较名称是否匹配。
5. 如果某个层未找到，立即返回 `false`；否则，遍历完成后返回 `true`。

```
typedef struct VkLayerProperties {
    char        layerName[VK_MAX_EXTENSION_NAME_SIZE];    // 层名称
    uint32_t    specVersion;                              // 层符合的 Vulkan 规范版本
    uint32_t    implementationVersion;                    // 层的实现版本
    char        description[VK_MAX_DESCRIPTION_SIZE];     // 层的描述文本
} VkLayerProperties;
```

`VkLayerProperties` 是 Vulkan API 中用于描述一个层（Layer）的属性的结构体。层在 Vulkan 中可以是验证层（用于调试）、设备兼容性层或其他功能扩展层。

### 2.2 获取所需扩展

获取创建 Vulkan 实例所需的扩展列表。

```
auto requiredExtensions = getRequiredExtensions(enableValidationLayers);
```

`getRequiredExtensions` 函数根据是否启用验证层返回不同的扩展列表。如果启用验证层，通常会添加调试扩展。

```
std::vector<const char *> HelloVK::getRequiredExtensions(
    bool enableValidationLayers) {
  std::vector<const char *> extensions;
  extensions.push_back("VK_KHR_surface");
  extensions.push_back("VK_KHR_android_surface");
  if (enableValidationLayers) {
    extensions.push_back(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
  }
  return extensions;
}
```

1. 扩展列表初始化。

- ```
  VK_KHR_surface
  ```

   所有平台通用的 Surface 扩展，用于 Vulkan 与窗口系统交互（例如渲染到窗口）。

- ```
  VK_KHR_android_surface
  ```

   Android 平台专用的 Surface 扩展，用于创建 Android 本地窗口（如 `ANativeWindow`）的 Vulkan Surface。该扩展仅在 Android 平台需要，其他平台需替换为对应扩展（如 Windows 的 `VK_KHR_win32_surface`）。

- 条件添加调试扩展。

`VK_EXT_DEBUG_UTILS_EXTENSION_NAME` 调试工具扩展，用于支持 Vulkan 的调试功能（如输出验证层错误、警告信息）。仅在启用验证层时添加，因为调试扩展通常与验证层配合使用。

### 2.3 填充应用信息

描述应用程序和使用的 Vulkan API 版本。

```
VkApplicationInfo appInfo{};
appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
appInfo.pApplicationName = "Hello Triangle";
appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
appInfo.pEngineName = "No Engine";
appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
appInfo.apiVersion = VK_API_VERSION_1_1;
```

- ```
  sType
  ```

  ：指定结构体类型（必须为 `VK_STRUCTURE_TYPE_APPLICATION_INFO`）。

- ```
  pApplicationName
  ```

   和 `pEngineName`：应用程序和引擎的名称。

- ```
  applicationVersion
  ```

   和 `engineVersion`：应用程序和引擎的版本。

- ```
  apiVersion
  ```

  ：指定使用的 Vulkan API 版本（此处为 1.1）。

### 2.4 配置实例创建信息

定义如何创建 Vulkan 实例。

```
VkInstanceCreateInfo createInfo{};
createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
createInfo.pApplicationInfo = &appInfo;
createInfo.enabledExtensionCount = (uint32_t)requiredExtensions.size();
createInfo.ppEnabledExtensionNames = requiredExtensions.data();
```

- ```
  pApplicationInfo
  ```

  ：指向上一步的 `appInfo`。

- ```
  enabledExtensionCount
  ```

   和 `ppEnabledExtensionNames`：启用扩展的列表。

### 2.5 处理验证层和调试信息

配置调试回调以接收验证层的错误/警告信息。

```
if (enableValidationLayers) {
    VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo{};
    createInfo.enabledLayerCount = static_cast<uint32_t>(validationLayers.size());
    createInfo.ppEnabledLayerNames = validationLayers.data();
    populateDebugMessengerCreateInfo(debugCreateInfo);
    createInfo.pNext = (VkDebugUtilsMessengerCreateInfoEXT*)&debugCreateInfo;
} else {
    createInfo.enabledLayerCount = 0;
    createInfo.pNext = nullptr;
}
```

如果启用验证层，设置验证层名称（`"VK_LAYER_KHRONOS_validation"`）。`populateDebugMessengerCreateInfo` 填充调试回调的详细信息（如消息级别和回调函数）。`pNext` 是 Vulkan 中用于链式扩展结构的指针，此处链接调试信息。

```
const char *toStringMessageSeverity(VkDebugUtilsMessageSeverityFlagBitsEXT s) {
switch (s) {
    case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT:
      return"VERBOSE";
    case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:
      return"ERROR";
    case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:
      return"WARNING";
    case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:
      return"INFO";
    default:
      return"UNKNOWN";
  }
}
const char *toStringMessageType(VkDebugUtilsMessageTypeFlagsEXT s) {
if (s == (VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT))
    return"General | Validation | Performance";
if (s == (VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT))
    return"Validation | Performance";
if (s == (VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT))
    return"General | Performance";
if (s == (VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT))
    return"Performance";
if (s == (VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
            VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT))
    return"General | Validation";
if (s == VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT) return"Validation";
if (s == VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT) return"General";
return"Unknown";
}

static VKAPI_ATTR VkBool32 VKAPI_CALL
debugCallback(VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
              VkDebugUtilsMessageTypeFlagsEXT messageType,
              const VkDebugUtilsMessengerCallbackDataEXT *pCallbackData,
              void * /* pUserData */) {
auto ms = toStringMessageSeverity(messageSeverity);
auto mt = toStringMessageType(messageType);
printf("[%s: %s]\n%s\n", ms, mt, pCallbackData->pMessage);

return VK_FALSE;
}

static void populateDebugMessengerCreateInfo(
    VkDebugUtilsMessengerCreateInfoEXT &createInfo) {
  createInfo = {};
  createInfo.sType = VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
  createInfo.messageSeverity = VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT |
                               VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT |
                               VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
  createInfo.messageType = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT |
                           VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT |
                           VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
  createInfo.pfnUserCallback = debugCallback;
}
```

#### 2.5.1 toStringMessageSeverity

将 Vulkan 调试消息的严重性级别（`VkDebugUtilsMessageSeverityFlagBitsEXT`）转换为可读字符串。

#### 2.5.2 toStringMessageType

将消息类型（`VkDebugUtilsMessageTypeFlagsEXT`，位掩码）转换为组合字符串。

#### 2.5.3 debugCallback

Vulkan 调试消息的实际处理函数，格式化输出消息内容。

- ```
  messageSeverity
  ```

  ：消息严重性（单个标志）。

- ```
  messageType
  ```

  ：消息类型（可能为多个标志的组合）。

- ```
  pCallbackData
  ```

  ：包含详细消息内容的结构体。

- ```
  pUserData
  ```

  ：用户自定义数据（此处未使用）。

#### 2.5.4 populateDebugMessengerCreateInfo

初始化调试信使的配置结构体，定义消息过滤规则和回调函数。

- ```
  messageSeverity
  ```

  ：指定接收的消息严重性级别（此处为 Verbose、Warning、Error）。

- ```
  messageType
  ```

  ：指定接收的消息类型。

- ```
  pfnUserCallback
  ```

  ：指向调试回调函数 `debugCallback`。

### 2.6 创建 Vulkan 实例

实际创建 Vulkan 实例。

```
VK_CHECK(vkCreateInstance(&createInfo, nullptr, &instance));
```

`vkCreateInstance` 是 Vulkan 核心函数，成功时返回 `VK_SUCCESS`。`VK_CHECK` 是一个宏，用于检查返回值是否为 `VK_SUCCESS`，否则抛出错误。创建结果存储在 `instance` 成员变量中。

```
#define VK_CHECK(x)                           \
  do {                                        \
    VkResult err = x;                         \
    if (err) {                                \
      LOGE("Detected Vulkan error: %d", err); \
      abort();                                \
    }                                         \
  } while (0)
```

VK_CHECK 是一个高效的错误检查工具，用于：

1. 自动化错误检测：确保所有 Vulkan API 调用被检查。
2. 简化代码：用一行宏替代重复的错误处理逻辑。
3. 快速失败：在开发阶段立即暴露问题，避免隐藏错误。

`do { ... } while (0)` 这是一个常见的宏定义技巧，目的是将多个语句包裹成一个逻辑块，确保宏在使用时不会因分号或作用域问题导致语法错误（例如直接用在 if 语句中）。`x` 是一个 Vulkan 函数调用（例如 `vkCreateInstance`）。Vulkan 函数的返回值类型为 `VkResult`，成功时为 `VK_SUCCESS`（值为 0），失败时为错误码（非零值）。如果检测到错误 `（err != 0）`，则执行以下操作：

1. 记录错误：通过 `LOGE` 输出错误码。
2. 终止程序：调用 `abort()` 立即终止程序，防止错误进一步传播。

### 2.7 枚举并记录可用扩展

列出所有可用的 Vulkan 实例扩展。

```
uint32_t extensionCount = 0;
vkEnumerateInstanceExtensionProperties(nullptr, &extensionCount, nullptr);
std::vector<VkExtensionProperties> extensions(extensionCount);
vkEnumerateInstanceExtensionProperties(nullptr, &extensionCount, extensions.data());
LOGI("available extensions");
for (const auto& extension : extensions) {
    LOGI("\t %s", extension.extensionName);
}
```

两次调用 `vkEnumerateInstanceExtensionProperties`：

1. 第一次获取扩展数量（`extensionCount`）。
2. 第二次填充扩展详细信息到 `extensions` 向量。

遍历扩展并打印名称（用于调试或日志）。

## 三、创建 Surface

用于在 Android 平台 上创建一个 Vulkan Surface。Surface 是 Vulkan 与平台窗口系统（如 Android 的 `ANativeWindow`）之间的桥梁，负责将渲染内容显示到屏幕上。

```
void HelloVK::initVulkan() {
createInstance();
createSurface();
  ...
}

void HelloVK::createSurface() {
assert(window != nullptr);  // window not initialized
const VkAndroidSurfaceCreateInfoKHR create_info{
      .sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR,
      .pNext = nullptr,
      .flags = 0,
      .window = window.get()};

VK_CHECK(vkCreateAndroidSurfaceKHR(instance, &create_info,
                                     nullptr/* pAllocator */, &surface));
}
```

1. 窗口有效性检查。确保 `window` 指针有效（非空）。`window` 是 Android 原生窗口（`ANativeWindow*`）的封装。
2. 配置 Surface 创建信息：

- ```
  sType
  ```

  ：标识结构体类型（必须为 VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR）。

- ```
  pNext
  ```

  ：扩展链指针，无扩展时设为 nullptr。

- ```
  flags
  ```

  ：保留字段，当前未使用，设为 0。

- ```
  window
  ```

  ：指向 Android 原生窗口的指针（`ANativeWindow*`）。`window.get()` 表示从智能指针 `std::unique_ptr` 中获取原始指针。

1. 调用 Vulkan 函数创建 Surface

`vkCreateAndroidSurfaceKHR` 入参解析：

- ```
  instance
  ```

  ：已创建的 Vulkan 实例（`VkInstance`）。

- ```
  &create_info
  ```

  ：指向配置好的 Surface 创建信息结构体。

- ```
  nullptr
  ```

  ：自定义内存分配器（通常无需指定）。

- ```
  &surface
  ```

  ：输出参数，存储创建的 Surface 句柄（`VkSurfaceKHR`）。

## 四、选择物理设备

`pickPhysicalDevice` 用于从系统中选择一个可用的 Vulkan 物理设备（通常是 GPU），供后续图形操作使用。物理设备是 Vulkan 管理的硬件抽象，代表实际的图形处理器。

```
void HelloVK::initVulkan() {
createInstance();
createSurface();
pickPhysicalDevice();
  ...
}

void HelloVK::pickPhysicalDevice() {
uint32_t deviceCount = 0;
vkEnumeratePhysicalDevices(instance, &deviceCount, nullptr);

assert(deviceCount > 0);  // failed to find GPUs with Vulkan support!

std::vector<VkPhysicalDevice> devices(deviceCount);
vkEnumeratePhysicalDevices(instance, &deviceCount, devices.data());

for (constauto &device : devices) {
    if (isDeviceSuitable(device)) {
      physicalDevice = device;
      break;
    }
  }

assert(physicalDevice != VK_NULL_HANDLE);  // failed to find a suitable GPU!
}
```

1. 获取物理设备数量。`vkEnumeratePhysicalDevices` 是 Vulkan API 函数，用于枚举当前系统中支持 Vulkan 的所有物理设备。
2. 断言验证设备存在性，确保至少存在一个支持 Vulkan 的物理设备。
3. 获取物理设备列表。
4. 遍历所有设备，使用自定义的 `isDeviceSuitable(device)` 检查设备是否满足需求，选择第一个符合条件的设备。
5. 断言验证设备选择结果。确保最终选择的物理设备句柄有效。

```
const std::vector<constchar *> deviceExtensions = {
      VK_KHR_SWAPCHAIN_EXTENSION_NAME};
      
structQueueFamilyIndices {
  std::optional<uint32_t> graphicsFamily;
  std::optional<uint32_t> presentFamily;
bool isComplete() {
    return graphicsFamily.has_value() && presentFamily.has_value();
  }
};

structSwapChainSupportDetails {
  VkSurfaceCapabilitiesKHR capabilities;
  std::vector<VkSurfaceFormatKHR> formats;
  std::vector<VkPresentModeKHR> presentModes;
};
      
QueueFamilyIndices HelloVK::findQueueFamilies(VkPhysicalDevice device) {
  QueueFamilyIndices indices;

uint32_t queueFamilyCount = 0;
vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, nullptr);

std::vector<VkQueueFamilyProperties> queueFamilies(queueFamilyCount);
vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount,
                                           queueFamilies.data());

int i = 0;
for (constauto &queueFamily : queueFamilies) {
    if (queueFamily.queueFlags & VK_QUEUE_GRAPHICS_BIT) {
      indices.graphicsFamily = i;
    }

    VkBool32 presentSupport = false;
    vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, &presentSupport);
    if (presentSupport) {
      indices.presentFamily = i;
    }

    if (indices.isComplete()) {
      break;
    }

    i++;
  }
return indices;
}

bool HelloVK::checkDeviceExtensionSupport(VkPhysicalDevice device) {
uint32_t extensionCount;
vkEnumerateDeviceExtensionProperties(device, nullptr, &extensionCount,
                                       nullptr);

std::vector<VkExtensionProperties> availableExtensions(extensionCount);
vkEnumerateDeviceExtensionProperties(device, nullptr, &extensionCount,
                                       availableExtensions.data());

std::set<std::string> requiredExtensions(deviceExtensions.begin(),
                                           deviceExtensions.end());

for (constauto &extension : availableExtensions) {
    requiredExtensions.erase(extension.extensionName);
  }

return requiredExtensions.empty();
}

SwapChainSupportDetails HelloVK::querySwapChainSupport(
    VkPhysicalDevice device) {
  SwapChainSupportDetails details;

vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface,
                                            &details.capabilities);

uint32_t formatCount;
vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, &formatCount, nullptr);

if (formatCount != 0) {
    details.formats.resize(formatCount);
    vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, &formatCount,
                                         details.formats.data());
  }

uint32_t presentModeCount;
vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, &presentModeCount,
                                            nullptr);

if (presentModeCount != 0) {
    details.presentModes.resize(presentModeCount);
    vkGetPhysicalDeviceSurfacePresentModesKHR(
        device, surface, &presentModeCount, details.presentModes.data());
  }
return details;
}

bool HelloVK::isDeviceSuitable(VkPhysicalDevice device) {
  QueueFamilyIndices indices = findQueueFamilies(device);
bool extensionsSupported = checkDeviceExtensionSupport(device);
bool swapChainAdequate = false;
if (extensionsSupported) {
    SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device);
    swapChainAdequate = !swapChainSupport.formats.empty() &&
                        !swapChainSupport.presentModes.empty();
  }
return indices.isComplete() && extensionsSupported && swapChainAdequate;
}
```

### 4.1 `findQueueFamilies` 函数

寻找物理设备支持的 **图形队列族** 和 **呈现队列族**。

**1. 获取队列族数量**

```
vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, nullptr);
```

调用 Vulkan API 获取设备支持的队列族数量。

**2. 获取队列族属性**

```
std::vector<VkQueueFamilyProperties> queueFamilies(queueFamilyCount);
vkGetPhysicalDeviceQueueFamilyProperties(device, &queueFamilyCount, queueFamilies.data());
```

存储所有队列族的属性（如支持的队列类型、数量等）。

**3. 遍历队列族**

**检查图形队列支持**

```
if (queueFamily.queueFlags & VK_QUEUE_GRAPHICS_BIT) {
  indices.graphicsFamily = i;
}
```

若队列族支持图形操作（`VK_QUEUE_GRAPHICS_BIT`），记录其索引。

**检查呈现队列支持**

```
vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, &presentSupport);
if (presentSupport) {
  indices.presentFamily = i;
}
```

检查队列族是否能向目标 Surface 提交图像。

**提前终止条件**

```
if (indices.isComplete()) { break; }
```

当找到图形和呈现队列族后，停止遍历。

`QueueFamilyIndices` 结构包含 `graphicsFamily` 和 `presentFamily`，并通过 `isComplete()` 验证完整性。

### 4.2 `checkDeviceExtensionSupport` 函数

验证物理设备是否支持所有必需的设备扩展（交换链扩展 `VK_KHR_SWAPCHAIN_EXTENSION_NAME`）。

**1. 获取设备扩展数量**

```
vkEnumerateDeviceExtensionProperties(device, nullptr, &extensionCount, nullptr);
```

调用 Vulkan API 获取设备支持的扩展数量。

**2. 获取扩展属性**

```
std::vector<VkExtensionProperties> availableExtensions(extensionCount);
vkEnumerateDeviceExtensionProperties(device, nullptr, &extensionCount, availableExtensions.data());
```

存储所有支持的扩展名称和版本。

**3. 验证扩展支持**

```
std::set<std::string> requiredExtensions(deviceExtensions.begin(), deviceExtensions.end());
for (const auto &extension : availableExtensions) {
  requiredExtensions.erase(extension.extensionName);
}
return requiredExtensions.empty();
```

将设备支持的扩展从必需扩展集合中删除，若集合为空则所有扩展均被支持。

### 4.3 `querySwapChainSupport` 函数

查询物理设备对交换链（Swapchain）的支持细节，包括 Surface 能力、格式和呈现模式。

**1. 获取 Surface 能力**

```
vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, &details.capabilities);
```

获取 Surface 的基本属性（如最小/最大图像数量、图像尺寸限制）。

**2. 获取 Surface 格式**

```
uint32_t formatCount;
vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, &formatCount, nullptr);

if (formatCount != 0) {
  details.formats.resize(formatCount);
  vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, &formatCount, details.formats.data());
}
```

存储支持的像素格式（如 `VK_FORMAT_B8G8R8A8_SRGB`）和颜色空间。

**3. 获取呈现模式**

```
uint32_t presentModeCount;
vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, &presentModeCount, nullptr);

if (presentModeCount != 0) {
  details.presentModes.resize(presentModeCount);
  vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, &presentModeCount, details.presentModes.data());
}
```

存储支持的呈现模式（如垂直同步 `VK_PRESENT_MODE_FIFO_KHR` 或立即模式 `VK_PRESENT_MODE_IMMEDIATE_KHR`）。

### 4.4 `isDeviceSuitable` 函数

综合验证物理设备是否满足应用程序需求。

**1. 检查队列族完整性**

```
QueueFamilyIndices indices = findQueueFamilies(device);
```

确保设备支持图形和呈现队列。

**2. 检查扩展支持**

```
bool extensionsSupported = checkDeviceExtensionSupport(device);
```

**3. 检查交换链充分性**

```
bool swapChainAdequate = false;
if (extensionsSupported) {
  SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device);
  swapChainAdequate = !swapChainSupport.formats.empty() && !swapChainSupport.presentModes.empty();
}
```

交换链检查仅在扩展支持时进行，设备需支持至少一种 Surface 格式和一种呈现模式。