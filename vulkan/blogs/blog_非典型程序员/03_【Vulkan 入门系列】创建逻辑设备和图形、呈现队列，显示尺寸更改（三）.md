# 【Vulkan 入门系列】创建逻辑设备和图形、呈现队列，显示尺寸更改（三）

在选择要使用的物理设备后，我们需要设置一个逻辑设备来与它交互。逻辑设备的创建过程类似于实例的创建过程，并描述了我们想要使用的功能。我们还需要指定要创建哪些队列，现在我们已经查询了哪些队列族可用。接下来设置调试信使，最后再来确定 Surface 的宽高来完成这一节的内容。

## 一、创建逻辑设备和图形、呈现队列

该函数用于创建 Vulkan 的逻辑设备（Logical Device） 并获取其图形队列和呈现队列句柄。逻辑设备是物理设备的抽象接口，负责管理资源分配和操作执行；队列则是向 GPU 提交指令的通道。

```
void HelloVK::createLogicalDeviceAndQueue() {
  QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
  std::vector<VkDeviceQueueCreateInfo> queueCreateInfos;
  std::set<uint32_t> uniqueQueueFamilies = {indices.graphicsFamily.value(),
                                            indices.presentFamily.value()};
float queuePriority = 1.0f;
for (uint32_t queueFamily : uniqueQueueFamilies) {
    VkDeviceQueueCreateInfo queueCreateInfo{};
    queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueCreateInfo.queueFamilyIndex = queueFamily;
    queueCreateInfo.queueCount = 1;
    queueCreateInfo.pQueuePriorities = &queuePriority;
    queueCreateInfos.push_back(queueCreateInfo);
  }

  VkPhysicalDeviceFeatures deviceFeatures{};

  VkDeviceCreateInfo createInfo{};
  createInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
  createInfo.queueCreateInfoCount =
      static_cast<uint32_t>(queueCreateInfos.size());
  createInfo.pQueueCreateInfos = queueCreateInfos.data();
  createInfo.pEnabledFeatures = &deviceFeatures;
  createInfo.enabledExtensionCount =
      static_cast<uint32_t>(deviceExtensions.size());
  createInfo.ppEnabledExtensionNames = deviceExtensions.data();
if (enableValidationLayers) {
    createInfo.enabledLayerCount =
        static_cast<uint32_t>(validationLayers.size());
    createInfo.ppEnabledLayerNames = validationLayers.data();
  } else {
    createInfo.enabledLayerCount = 0;
  }

VK_CHECK(vkCreateDevice(physicalDevice, &createInfo, nullptr, &device));

vkGetDeviceQueue(device, indices.graphicsFamily.value(), 0, &graphicsQueue);
vkGetDeviceQueue(device, indices.presentFamily.value(), 0, &presentQueue);
}
```

### 1.1 获取队列族索引

通过 `findQueueFamilies` 函数获取物理设备支持的图形队列族和呈现队列族的索引。

```
QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
```

`indices` 包含两个成员：

- ```
  graphicsFamily
  ```

  ：图形队列族索引（用于渲染指令）

- ```
  presentFamily
  ```

  ：呈现队列族索引（用于显示到窗口）

### 1.2 去重队列族

去重队列族索引。

```
std::set<uint32_t> uniqueQueueFamilies = {
    indices.graphicsFamily.value(), 
    indices.presentFamily.value()
};
```

使用 `std::set` 自动去重，例如：

- 若 `graphicsFamily=1`, `presentFamily=1` → `uniqueQueueFamilies={1}`
- 若 `graphicsFamily=0`, `presentFamily=1` → `uniqueQueueFamilies={0,1}`

### 1.3 配置队列创建信息

为每个唯一队列族生成一个队列配置结构体，后续用于设备创建。

```
float queuePriority = 1.0f;
for (uint32_t queueFamily : uniqueQueueFamilies) {
  VkDeviceQueueCreateInfo queueCreateInfo{};
  queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
  queueCreateInfo.queueFamilyIndex = queueFamily; // 队列族索引
  queueCreateInfo.queueCount = 1;                 // 创建1个队列
  queueCreateInfo.pQueuePriorities = &queuePriority; // 队列优先级（0.0~1.0）
  queueCreateInfos.push_back(queueCreateInfo);
}
```

- ```
  queueCount
  ```

  ：从该队列族中创建的队列数量（通常每个族只需1个队列）。

- ```
  pQueuePriorities
  ```

  ：队列优先级（影响 GPU 调度，1.0 表示最高优先级）。

### 1.4 配置设备特性

启用物理设备的硬件特性（如几何着色器、宽线条支持等）。此处未启用任何特性（空结构体），表示仅依赖默认功能。

```
VkPhysicalDeviceFeatures deviceFeatures{};
```

### 1.5 配置逻辑设备创建信息

```
VkDeviceCreateInfo createInfo{};
createInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
createInfo.queueCreateInfoCount = static_cast<uint32_t>(queueCreateInfos.size());
createInfo.pQueueCreateInfos = queueCreateInfos.data(); // 队列配置
createInfo.pEnabledFeatures = &deviceFeatures;          // 设备特性
createInfo.enabledExtensionCount = static_cast<uint32_t>(deviceExtensions.size());
createInfo.ppEnabledExtensionNames = deviceExtensions.data(); // 扩展（如交换链）
// 处理验证层
if (enableValidationLayers) {
  createInfo.enabledLayerCount =  static_cast<uint32_t>(validationLayers.size());
  createInfo.ppEnabledLayerNames = validationLayers.data();
} else {
  createInfo.enabledLayerCount = 0;
}
```

- ```
  ppEnabledExtensionNames
  ```

  ：包含 `VK_KHR_SWAPCHAIN_EXTENSION_NAME`（交换链扩展）。

- ```
  ppEnabledLayerNames
  ```

  ：仅调试时启用验证层，发布时关闭以提升性能。

### 1.6 创建逻辑设备

```
VK_CHECK(vkCreateDevice(physicalDevice, &createInfo, nullptr, &device));
```

- ```
  physicalDevice
  ```

  ：之前选择的物理设备。

- ```
  &createInfo
  ```

  ：配置信息结构体。

- ```
  nullptr
  ```

  ：自定义内存分配器（通常无需指定）。

- ```
  &device
  ```

  ：输出逻辑设备句柄。

### 1.7 获取队列句柄

每个队列族可以创建多个队列。

- 图形队列用于提交渲染命令。
- 呈现队列用于提交图像到窗口。

```
vkGetDeviceQueue(device, indices.graphicsFamily.value(), 0, &graphicsQueue);
vkGetDeviceQueue(device, indices.presentFamily.value(), 0, &presentQueue);
```

- ```
  device
  ```

  ：已创建的逻辑设备。

- ```
  queueFamilyIndex
  ```

  ：队列族索引（来自 `indices`）。

- ```
  queueIndex
  ```

  ：队列索引（同一队列族中第几个队列，此处为 0）。

- ```
  &graphicsQueue
  ```

  /`&presentQueue`：输出队列句柄。

### 1.8 关键概念

#### 1. 逻辑设备 vs 物理设备

**物理设备**：硬件抽象。
**逻辑设备**：基于物理设备的软件接口，管理资源（缓冲区、纹理等）。

#### 2. 队列族（Queue Families）

每个队列族支持特定类型的操作（图形、计算、传输等）。同一队列族的队列共享相同能力，但可独立提交指令。

#### 3. 设备扩展（Device Extensions）

- 必需扩展（如 `VK_KHR_swapchain`）：启用交换链功能以实现画面呈现。
- 可选扩展（如 `VK_NV_ray_tracing`）：启用光线追踪等高级功能。

![图片](./assets/640-1750606200982-67.webp)

## 二、设置调试信使

用于在 Vulkan 中创建调试信使（Debug Messenger），用于接收并处理来自验证层（Validation Layers）的调试信息（如错误、警告、性能提示等）。调试信使是 Vulkan 调试工具链的核心组件，通过回调函数将消息传递到用户自定义的处理逻辑中。

```
void HelloVK::initVulkan() {
createInstance();
createSurface();
pickPhysicalDevice();
createLogicalDeviceAndQueue();
setupDebugMessenger();
  ...
}

static VkResult CreateDebugUtilsMessengerEXT(
    VkInstance instance, const VkDebugUtilsMessengerCreateInfoEXT *pCreateInfo,
    const VkAllocationCallbacks *pAllocator,
    VkDebugUtilsMessengerEXT *pDebugMessenger) {
auto func = (PFN_vkCreateDebugUtilsMessengerEXT)vkGetInstanceProcAddr(
      instance, "vkCreateDebugUtilsMessengerEXT");
if (func != nullptr) {
    returnfunc(instance, pCreateInfo, pAllocator, pDebugMessenger);
  } else {
    return VK_ERROR_EXTENSION_NOT_PRESENT;
  }
}

void HelloVK::setupDebugMessenger() {
if (!enableValidationLayers) {
    return;
  }

  VkDebugUtilsMessengerCreateInfoEXT createInfo{};
populateDebugMessengerCreateInfo(createInfo);

VK_CHECK(CreateDebugUtilsMessengerEXT(instance, &createInfo, nullptr,
                                        &debugMessenger));
}
```

### 2.1 `CreateDebugUtilsMessengerEXT` 函数

**1. 动态加载扩展函数**

`vkGetInstanceProcAddr`：Vulkan 核心函数，用于根据名称动态获取扩展函数的指针。

- ```
  instance
  ```

  ：已创建的 Vulkan 实例。

- ```
  "vkCreateDebugUtilsMessengerEXT"
  ```

  ：目标扩展函数名称。

`PFN_vkCreateDebugUtilsMessengerEXT`：函数指针类型，匹配 `vkCreateDebugUtilsMessengerEXT` 的签名。

**2. 检查函数有效性**

如果 `func` 非空，可以安全调用该函数创建调试信使。如果 `func` 为空，表示扩展未启用或不受支持，返回 `VK_ERROR_EXTENSION_NOT_PRESENT`。

**3. 为何需要动态加载？**

Vulkan 扩展函数（如 `vkCreateDebugUtilsMessengerEXT`）不在核心 API 中。直接链接会导致未实现扩展时程序崩溃，动态加载允许运行时安全检查。

### 2.2 `setupDebugMessenger` 方法

1. ```
   enableValidationLayers
   ```

    是用户控制的标志，仅在调试时启用验证层。若未启用验证层，跳过调试信使的创建以节省资源。

2. 填充 `VkDebugUtilsMessengerCreateInfoEXT` 结构体。

3. 调用 `CreateDebugUtilsMessengerEXT` 函数创建调试信使。

**`VkDebugUtilsMessengerCreateInfoEXT` 结构体**

```
typedef struct VkDebugUtilsMessengerCreateInfoEXT {
  VkStructureType                  sType;                 // 结构体类型
  const void*                      pNext;                 // 扩展链指针
  VkDebugUtilsMessengerCreateFlagsEXT flags;              // 保留字段（通常为 0）
  VkDebugUtilsMessageSeverityFlagsEXT messageSeverity;    // 消息严重性过滤
  VkDebugUtilsMessageTypeFlagsEXT    messageType;         // 消息类型过滤
  PFN_vkDebugUtilsMessengerCallbackEXT pfnUserCallback;   // 回调函数
  void*                            pUserData;             // 用户自定义数据
} VkDebugUtilsMessengerCreateInfoEXT;
```

## 三、显示尺寸更改

主要解决设备屏幕物理旋转（如手机横竖屏切换）导致的 Surface 尺寸方向不一致的问题，确保后续渲染流程使用正确的宽高比例。

```
void HelloVK::initVulkan() {
createInstance();
createSurface();
pickPhysicalDevice();
createLogicalDeviceAndQueue();
setupDebugMessenger();
establishDisplaySizeIdentity();
  ...
}

void HelloVK::establishDisplaySizeIdentity() {
  VkSurfaceCapabilitiesKHR capabilities;
vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface,
                                            &capabilities);

uint32_t width = capabilities.currentExtent.width;
uint32_t height = capabilities.currentExtent.height;
if (capabilities.currentTransform & VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR ||
      capabilities.currentTransform & VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR) {
    // Swap to get identity width and height
    capabilities.currentExtent.height = width;
    capabilities.currentExtent.width = height;
  }

  displaySizeIdentity = capabilities.currentExtent;
}
```

### 3.1 获取 Surface 能力

```
VkSurfaceCapabilitiesKHR capabilities;
vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, &capabilities);
```

`vkGetPhysicalDeviceSurfaceCapabilitiesKHR`：Vulkan API，查询物理设备与 Surface 相关的能力属性，包括：

- ```
  currentExtent
  ```

  ：Surface 的当前像素尺寸（宽高）。

- ```
  currentTransform
  ```

  ：Surface 当前的几何变换（如旋转、镜像）。

### 3.2 提取原始宽高

记录 Surface 的原始尺寸。例如，手机竖屏时可能为 `1080x1920`，横屏时为 `1920x1080`。

```
uint32_t width = capabilities.currentExtent.width;
uint32_t height = capabilities.currentExtent.height;
```

### 3.3 检测旋转变换

如果 Surface 应用了 90 度或 270 度的旋转，交换宽高值。

```
if (capabilities.currentTransform & VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR ||
    capabilities.currentTransform & VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR) {
  capabilities.currentExtent.height = width;
  capabilities.currentExtent.width = height;
}
```

**Surface 变换（Surface Transform）**

表示 Surface 在物理显示设备上的几何变换（如旋转、镜像）。

常见值：

- ```
  VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR
  ```

  ：无旋转，逻辑尺寸=物理尺寸。

- ```
  VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR
  ```

  ：顺时针旋转 90 度。

- ```
  VK_SURFACE_TRANSFORM_ROTATE_180_BIT_KHR
  ```

  ：顺旋转 180 度。

- ```
  VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR
  ```

  ：顺时针旋转 270 度（等效逆时针 90 度）。

### 3.4 保存尺寸

`displaySizeIdentity`：存储修正后的宽高，供后续渲染流程（如交换链创建、视口设置）使用。

```
displaySizeIdentity = capabilities.currentExtent;
```