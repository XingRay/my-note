# Vulkan学习笔记(二）创建交换链之前的设备检测

接上文

[栗野：Vulkan学习笔记(一）开发环境搭建与教程代码框架6 赞同 · 5 评论文章![img](./assets/v2-fef9860b499784cd7df9c5055ca9fdac_180x120-1735288521694-401.jpg)](https://zhuanlan.zhihu.com/p/538299827)

## **物理设备与队列族**

### **[设备检测](https://zhida.zhihu.com/search?content_id=207906615&content_type=Article&match_order=1&q=设备检测&zhida_source=entity)**

首先我们需要选取一个可用的GPU设备，和之前查找扩展的方式相同，依然是通过遍历所有设备查找可用设备选取。这里我们只选取第一台可用设备，但是Vulkan是支持多设备GPU工作的。

```cpp
void LearnVKApp::pickPhysicalDevice() {
    uint32_t deviceCount = 0;
    vkEnumeratePhysicalDevices(m_vkInstance, &deviceCount, nullptr);
    if (deviceCount == 0) {
        throw std::runtime_error("failed to pick a GPU with Vulkan support!");
    }
    std::vector<VkPhysicalDevice> physicalDevices(deviceCount);
    vkEnumeratePhysicalDevices(m_vkInstance, &deviceCount, physicalDevices.data());
    for (auto& device : physicalDevices) {
        if (isDeviceSuitable(device)) {
            m_physicalDevice = device;
            break;
        }
    }
    if (m_physicalDevice == VK_NULL_HANDLE) {
        throw std::runtime_error("failed to pick a suitable GPU!");
    }
}
```

### **设备需求检测**

设备主要包含两种属性properties和features，分别代表设备基础信息如名称、类型、支持的Vulkan版本等和设备的特性如是否支持[纹理压缩](https://zhida.zhihu.com/search?content_id=207906615&content_type=Article&match_order=1&q=纹理压缩&zhida_source=entity)、64位浮点等。这些属性信息需要根据我们的应用对硬件的需求去查找，并将逻辑写在`isDeviceSuitable`中。

获取properties和features的方式如下：

```cpp
vkGetPhysicalDeviceProperties(physicalDevice, &properties);
vkGetPhysicalDeviceFeatures(physicalDevice, &features);
```

获取到之后即可对设备的要求进行查找比对，如果找到符合要求的设备就返回true。

### **获取队列族**

Vulkan的种种操作指令基本上都是通过提交到队列的方式实现的，而不同指令会提交到不同的队列族。对于不同的硬件设备它所支持的队列族也是不同的，因此也就要求我们找到一个满足队列族需求的硬件设备从而创建队列族和逻辑设备。根据上述描述，我们需要三个函数来完成这些逻辑：

- `isDeviceSuitable`在这里我们需要判断是否已有的物理设备满足条件。
- `findDeviceQueueFamilies`判断当前物理设备是否满足队列族的条件，如果找到一个满足条件的队列族，则返回队列族的索引。教程中队列族的条件只有一条，只要`queueFlag`有`VK_QUEUE_GRAPHICS_BIT`位的标记。
- `createLogicalDevice`当获取了符合的队列族索引，就可以根据索引创建这个队列。创建队列的过程是在创建逻辑设备时一并完成的。创建逻辑设备时除了队列，还需要设置校验层。[设备校验](https://zhida.zhihu.com/search?content_id=207906615&content_type=Article&match_order=1&q=设备校验&zhida_source=entity)层设置与实例校验层基本一致。而且我们已经完成了校验层的检查，这里就不需要再次检查了。最后创建了逻辑设备通过`vkGetDeviceQueue`将其队列保存到成员变量句柄中以备以后使用。



## **窗口表面**

Vulkan的实现与平台无关，因此需要第三方的窗口来显示渲染的内容。Vulkan提供了与外部窗口的接口扩展`VK_KHR_surface`建立了窗口与Vulkan系统的连接，它暴露的API`VkSurfaceKHR`就是抽象的窗口表面。

这里我们使用的是`glfw`作为我们的窗口库，它已经帮我们封装好了相关的API，我们可以通过`glfwCreateWindowSurface`获得surface。

**呈现支持**：不一定所有设备都支持Vulkan的特性，而且支持绘制和表现的队列族也可能不同（不过大部分情况相同，代码中如果相同就使用同一个队列族），因此我们需要在查询设备支持的队列族中添加呈现队列(`presentQueue`)。修改`QueueFamiliyIndices`如下：

```cpp
struct QueueFamiliyIndices
{
	std::set<uint32_t> familiesIndexSet;
	int graphicsFamily = -1;
	int presentFamily = -1;
	bool isComplete() {
		return graphicsFamily >= 0 && presentFamily >= 0;
	}
	bool isSameFamily() {
		return graphicsFamily == presentFamily;
	}
	int familyCount() {
		return static_cast<uint32_t>(familiesIndexSet.size());
	}
};
```

为此我们要为每一个队列族在创建逻辑设备的时候创建一个队列，修改创建逻辑设备的填写队列信息部分的代码：

```cpp
QueueFamiliyIndices indices = findDeviceQueueFamilies(m_physicalDevice);
std::vector<VkDeviceQueueCreateInfo> queueCreateInfos;
float queuePriority = 1.0f;
for (auto index : indices.familiesIndexSet) {
	VkDeviceQueueCreateInfo queueCreateInfo = {};
	queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
	queueCreateInfo.queueCount = 1;
	queueCreateInfo.queueFamilyIndex = index;
	queueCreateInfo.pQueuePriorities = &queuePriority;
	queueCreateInfos.push_back(queueCreateInfo);
}
```

创建好逻辑设备后，通过`vkGetDeviceQueue`获取队列，并根据名字保存到map中备用

```cpp
VkQueue queue;
vkGetDeviceQueue(m_device, static_cast<uint32_t>(indices.graphicsFamily), 0, &queue);	// 获取到队列句柄,并添加到map中去
m_queueMap.insert(std::make_pair("graphicsFamily", queue));
vkGetDeviceQueue(m_device, static_cast<uint32_t>(indices.presentFamily), 0, &queue);
m_queueMap.insert(std::make_pair("presentFamily", queue));
```



## **[交换链](https://zhida.zhihu.com/search?content_id=207906615&content_type=Article&match_order=1&q=交换链&zhida_source=entity)**

不同于OpenGL有默认的[帧缓冲](https://zhida.zhihu.com/search?content_id=207906615&content_type=Article&match_order=1&q=帧缓冲&zhida_source=entity)。Vulkan想要在创建的Surface绘制需要我们自己创建交换链。交换链和`VK_KHR_Surface`一样，也是一个Vulkan的扩展。不过与Surface是Instance扩展不同，它是一个Device的扩展。我们可以通过`vkEnumerateDeviceExtensionProperties`获取到所有当前设备支持的Device扩展。

因为是扩展，所以需要显示地对swap chain进行物理设备的扩展检测之后再创建。我们需要再次修改`isSuitableDevice`来考察设备是否支持交换链。

```cpp
// 查询设备是否支持要求的扩展
bool extentionsSupport = checkDeviceExtentionsSupport(physicalDevice);
bool swapChainAdequate = false;
if (extentionsSupport) {
	auto swapChainDetails = queryDeviceSwapChainSupport(physicalDevice);
	swapChainAdequate = !swapChainDetails.formats.empty() && !swapChainDetails.presentModes.empty();
}
```

同时，[swap chain](https://zhida.zhihu.com/search?content_id=207906615&content_type=Article&match_order=2&q=swap+chain&zhida_source=entity)的创建需要三大重要参数`PresentMode`和`Format`以及`Extent2D`的支持，他们是swap chain的呈现模式，以及所承载地图片的格式色彩空间和规格大小。为了保证最佳的呈现模式以及格式，以及他们的平台支持性，我们需要编写`queryDeviceSwapChainSupport`查询设备的支持。

```cpp
SwapChainSupportDetails LearnVKApp::queryDeviceSwapChainSupport(VkPhysicalDevice physicalDevice) {
	SwapChainSupportDetails details;
	vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, m_surface, &details.surfaceCapabilities);
	// 获取物理设备表面支持的格式
	uint32_t formatCount = 0;
	vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, m_surface, &formatCount, nullptr);
	if (formatCount != 0) {
		details.formats.resize(formatCount);
		vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, m_surface, &formatCount, details.formats.data());
	}
	uint32_t presentModeCount = 0;
	vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, m_surface, &presentModeCount, nullptr);
	if (presentModeCount != 0) {
		details.presentModes.resize(presentModeCount);
		vkGetPhysicalDeviceSurfacePresentModesKHR(physicalDevice, m_surface, &presentModeCount, details.presentModes.data());
	}
	return details;
}
```

然后分别编写选择呈现模式、格式以及规格大小的函数确定最后使用的最优方案。选择好最优方案之后，在创建的交换链时也有一个地方需要注意。因为swap chain在vulkan程序中被创建之后是作为资源存在，通过`VkQueue`发送执行命令完成绘制。因为在之前窗口表面支持部分添加了`presentFamily`队列族，多个队列族中的队列访问资源这就有了并发性以及图像所有权上的问题。因此在创建swap chain时，需要指定不同的队列族在访问swap chain时为协同模式。

```cpp
QueueFamiliyIndices queueFamilyIndices = findDeviceQueueFamilies(m_physicalDevice);
auto familyIndicesSet = queueFamilyIndices.familiesIndexSet;
if (familyIndicesSet.size() != 1) {	// 存在多个队列族就会出现并发申请图片资源的问题
	createInfo.imageSharingMode = VK_SHARING_MODE_CONCURRENT;
	createInfo.queueFamilyIndexCount = static_cast<uint32_t>(familyIndicesSet.size());
	createInfo.pQueueFamilyIndices = std::vector<uint32_t>(familyIndicesSet.begin(), familyIndicesSet.end()).data();
} else {
	createInfo.imageSharingMode = VK_SHARING_MODE_EXCLUSIVE;
	createInfo.queueFamilyIndexCount = 0;		// 如果只有一个就不存在并发问题，这里随意填写即可
	createInfo.pQueueFamilyIndices = nullptr;
}
```

- `VK_SHARING_MODE_EXCLUSIVE`：一张图像同一时间只能被一个队列族所拥有。在另一队列族使用前，必须显式改变图像所有权。这一模式下性能表现最佳。
- `VK_SHARING_MODE_CONCURRENT`：图像可以在多个队列族间 使用，不需要显式地改变图像所有权。

然后就是指定一些之前创建的swap chain的参数，然后通过`vkCreateSwapchainKHR`创建交换链，然后获取其中所有图片的句柄保存以备之后使用。同时还需要保存图像的格式和规格大小。

```cpp
uint32_t imgCount = 0;
// 获取交换链图像的句柄
vkGetSwapchainImagesKHR(m_device, m_swapChain, &imgCount, nullptr);
m_swapChainImages.resize(imgCount);
vkGetSwapchainImagesKHR(m_device, m_swapChain, &imgCount, m_swapChainImages.data());
m_swapChainImageFormat = surfaceFormat.format;
m_swapChainImageExtent = extent;
```

## **图像视图**

图像视图描述了访问图像的方式，以及图像的哪一部分可以被访问。比如，图像可以被图像视图描述为一个没有细化级别的二维深度纹理，进而可以在其上进行与二维深度纹理相关的操作。 我们可以通过使用`vkCreateImageView`来对swap chain中获得的图像句柄进行设置图像视图。

下一篇：

[栗野：Vulkan学习笔记(三）利用CMake在项目中编译配置Shader6 赞同 · 0 评论文章![img](./assets/v2-fef9860b499784cd7df9c5055ca9fdac_180x120-1735288521694-401.jpg)](https://zhuanlan.zhihu.com/p/539561176)



