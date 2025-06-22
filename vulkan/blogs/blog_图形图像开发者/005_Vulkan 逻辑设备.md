# Vulkan 逻辑设备

在 Vulkan 中，**逻辑设备（Logical Device）是与物理设备（Physical Device）交互的接口。它抽象了对特定 GPU （物理设备）的访问，使得应用程序能够提交命令并管理资源，而无需直接与物理硬件打交道。**

举例来说，物理设备可能包含了三种队列：图形、计算和传输。但是逻辑设备创建的时候，可以只关联一个单独的队列（比如图形），这样我们就可以很方便地向队列提交指令缓存了。

## 创建逻辑设备

创建逻辑设备时，你需要指定你希望使用的队列族和队列、启用的扩展、以及一些其他特性。我们通过 vkCreateDevice() 函数创建逻辑设备。其定义如下：

## vkCreateDevice

```
VkResult vkCreateDevice(
VkPhysicalDevice                            physicalDevice,
const VkDeviceCreateInfo*                   pCreateInfo,
const VkAllocationCallbacks*                pAllocator,
VkDevice*                                   pDevice);
```



- physicalDevice 指定在哪一个物理设备上创建逻辑设备。
- pCreateInfo 创建逻辑设备的配置信息。
- pAllocator 内存分配器。为 nullptr 表示使用内部默认分配器，否则为自定义分配器。
- pDevice 创建逻辑设备的结果。

其中 VkDeviceCreateInfo 定义如下：

## VkDeviceCreateInfo

```
typedef struct VkDeviceCreateInfo {
VkStructureType                    sType;
const void*                        pNext;
VkDeviceCreateFlags                flags;
uint32_t                           queueCreateInfoCount;
const VkDeviceQueueCreateInfo*     pQueueCreateInfos;
uint32_t                           enabledLayerCount;
const char* const*                 ppEnabledLayerNames;
uint32_t                           enabledExtensionCount;
const char* const*                 ppEnabledExtensionNames;
const VkPhysicalDeviceFeatures*    pEnabledFeatures;
} VkDeviceCreateInfo;
```



- sType 是该结构体的类型枚举值， 必须 是 VkStructureType::VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO 。
- pNext 要么是 NULL 要么指向其他结构体来扩展该结构体。
- flags 目前没用上，为将来做准备。
- queueCreateInfoCount 指定 pQueueCreateInfos 数组元素个数。
- pQueueCreateInfos 指定 VkDeviceQueueCreateInfo 数组。用于配置要创建的设备队列信息。
- enabledLayerCount 指定 ppEnabledLayerNames 数组元素个数。该成员已被 遗弃 并 忽略 。
- ppEnabledLayerNames 指定要开启的验证层。该成员已被 遗弃 并 忽略 。
- enabledExtensionCount 指定 ppEnabledExtensionNames 数组中元素个数。
- ppEnabledExtensionNames 指定要开启的扩展。该数组数量必须大于等于 enabledExtensionCount 。
- pEnabledFeatures 配置要开启的特性。

其中 queueCreateInfoCount 和 pQueueCreateInfos 用于指定在逻辑设备中需要创建的 设备队列 。其中 VkDeviceQueueCreateInfo 定义如下：

## VkDeviceQueueCreateInfo

```
// 由 VK_VERSION_1_0 提供
typedef struct VkDeviceQueueCreateInfo {
VkStructureType             sType;
const void*                 pNext;
VkDeviceQueueCreateFlags    flags;
uint32_t                    queueFamilyIndex;
uint32_t                    queueCount;
const float*                pQueuePriorities;
} VkDeviceQueueCreateInfo;
```



- **sType** 是该结构体的类型枚举值， 必须 是 VkStructureType::VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO 。
- **pNext** 要么是 NULL 要么指向其他结构体来扩展该结构体。
- **flags** 配置额外的信息。可设置的值定义在 VkDeviceQueueCreateFlagBits 枚举中。
- **queueFamilyIndex** 指定目标设备队列族的索引。
- **queueCount** 指定要在 queueFamilyIndex 中创建设备队列的数量。
- **pQueuePriorities** 指向元素数量为 queueCount 的 float 数组。用于配置创建的每一个设备队列的优先级。

其中 queueFamilyIndex 必须 是目标物理设备中有效的设备队列族索引，并且 queueCount 必须小于等于 queueFamilyIndex 索引对应的设备队列族中的队列数量。

其中 pQueuePriorities 配置的优先级的有效等级范围为[0, 1] ，值越大，优先级越高。其中 0.0 是最低的优先级， 1.0 是最高的优先级。在某些设备中，优先级越高意味着将会得到更多的执行机会，具体的队列调由设备自身管理， Vulkan 并不规定调度规则。

## 设备扩展

前文创建实例的时候可以设置实例的扩展，在 VkDeviceCreateInfo 我们需要通过 enabledExtensionCount 和 ppEnabledExtensionNames 来指定该逻辑设备要开启的 设备扩展 （ Device Extension ）。

在开启设备扩展之前，我们需要通过 vkEnumerateDeviceExtensionProperties(...) 函数获取目标设备支持的扩展。其定义如下：

## vkEnumerateDeviceExtensionProperties

```
// 由 VK_VERSION_1_0 提供
VkResult vkEnumerateDeviceExtensionProperties(
VkPhysicalDevice                            physicalDevice,
const char*                                 pLayerName,
uint32_t*                                   pPropertyCount,
VkExtensionProperties*                      pProperties);
```



- physicalDevice 要查询扩展的目标物理设备。
- pLayerName 要么为 空 要么为 层 的名称。
- pPropertyCount 要么为 空 要么为 pProperties 中元素的数量。
- pProperties 为扩展信息数组。元素个数 必须 大于等于 pPropertyCount 中指定数量。

枚举设备扩展：

```
VkPhysicalDevice physicalDevice;//之前获取的物理设备;

uint32_t extension_property_count = 0;
vkEnumerateDeviceExtensionProperties(physicalDevice, &extension_property_count, nullptr);

std::vector<VkExtensionProperties> extension_properties(extension_property_count);
vkEnumerateDeviceExtensionProperties(physicalDevice, &extension_property_count, extension_properties.data());
```

有 几个常用的设备扩展：

- **VK_KHR_swapchain 交换链**。用于与VK_KHR_surface 和平台相关的 VK_{vender}_{platform}_surface 扩展配合使用。用于窗口化显示渲染结果。
- VK_KHR_display 某些平台支持直接全屏显示渲染结果（比如嵌入式平台：车载、移动平台等）。
- VK_KHR_display_swapchain 全屏显示交换链。与 VK_KHR_display 扩展配合使用。

## 获取设备队列

在创建完逻辑设备后，就可以通过 vkGetDeviceQueue() 函数获取设备队列。其定义如下：

```
void vkGetDeviceQueue(
VkDevice                                    device,
uint32_t                                    queueFamilyIndex,
uint32_t                                    queueIndex,
VkQueue*                                    pQueue);
```



- **device** 目标逻辑设备。
- **queueFamilyIndex** 是前面获取的目标设备队列的队列族索引。
- **queueIndex** 对应 VkDeviceQueueCreateInfo::queueCount 的对应设备队列索引, 用于区分创建的多个队列。
- **pQueue** 对应 VkDeviceQueueCreateInfo::queueCount 创建的第 queueIndex 的设备队列。

# 创建逻辑设备示例

```
//获取目标队列族索引
uint32_t queueFamilyCount = 0;
vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyCount, nullptr);
std::vector<VkQueueFamilyProperties> queueFamilies(queueFamilyCount);
vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyCount, queueFamilies.data());

// 找到支持图形操作的队列族
int graphicsQueueFamilyIndex = -1;
for (uint32_t i = 0; i < queueFamilyCount; i++) {
    if (queueFamilies[i].queueFlags & VK_QUEUE_GRAPHICS_BIT) {
        graphicsQueueFamilyIndex = i;
        break;
    }
}

//定义队列创建信息
float queuePriority = 1.0f;
VkDeviceQueueCreateInfo queueCreateInfo = {};
queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
queueCreateInfo.queueFamilyIndex = graphicsQueueFamilyIndex;
queueCreateInfo.queueCount = 1;
queueCreateInfo.pQueuePriorities = &queuePriority;

//定义逻辑设备创建信息

//设备扩展
std::vector<const char*> device_extensions;
device_extensions.push_back("VK_KHR_swapchain");

VkDeviceCreateInfo deviceCreateInfo = {};
deviceCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
deviceCreateInfo.queueCreateInfoCount = 1;
deviceCreateInfo.pQueueCreateInfos = &queueCreateInfo;
deviceCreateInfo.enabledExtensionCount = static_cast<uint32_t>(device_extensions.size());
deviceCreateInfo.ppEnabledExtensionNames = device_extensions.data();

// 你可以在这里指定设备特性和扩展
VkPhysicalDeviceFeatures deviceFeatures = {};
deviceCreateInfo.pEnabledFeatures = &deviceFeatures;

//调用 vkCreateDevice 函数创建逻辑设备，并获取设备句柄。
VkDevice device;
if (vkCreateDevice(physicalDevice, &deviceCreateInfo, nullptr, &device) != VK_SUCCESS) {
    throw std::runtime_error("failed to create logical device!");
}

// 获取图形队列句柄
VkQueue graphicsQueue;
vkGetDeviceQueue(device, graphicsQueueFamilyIndex, 0, &graphicsQueue);

//Vulkan 编程...

//销毁逻辑设备
vkDestroyDevice(device, nullptr);
```