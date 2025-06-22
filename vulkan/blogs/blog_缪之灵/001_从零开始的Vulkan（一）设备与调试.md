# 从零开始的Vulkan（一）:设备与调试

## 前言

今年上半年Khronos正式推出了Vulkan1.3标准，借此机会正好让我这个已经两年没有碰过图形学的人重温一下“十分简约”的Vulkan标准，从零开始记录用Vulkan封装渲染引擎的过程。

介于我的大部分源码是在Vulkan1.1版本下写成的，且使用hpp版本，所以会有很多新特性无法顾及，以及代码会显得较为臃肿，若有不当之处还请见谅。

[开发环境](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=开发环境&zhida_source=entity)：ViusalStudio2022 C++11 Win32 Vulkan1.3.216.0

## 一切的开始：创建Vulkan实例和设备

在此，我们将完成三件事：

1. 创建Vulkan实例（VkInstance），并将必要的层与实例扩展添加上去。
2. 借用Vulkan实例创建一个用于验证层反馈错误的debugMessenger。
3. 找到可用的GPU物理设备，并依托物理设备创建出统管全局的逻辑设备（VkDevice），同时添加需要的附加特性和设备扩展。

### vkInfo结构体

由于Vulkan在使用过程中会有大量的结构体和对象冗余，所以我用了一个大结构体来容纳这些东西，这个结构体在后面的过程会经常用到：

```cpp
struct Vulkan {
	vk::Format format;

	vk::Instance instance;
	vk::PhysicalDevice gpu;
	vk::Device device;
	vk::Queue queue;
	vk::SurfaceKHR surface;
	vk::SwapchainKHR swapchain;
	vk::CommandPool cmdPool;
	std::vector<vk::CommandBuffer> cmd;

	vk::Semaphore imageAcquiredSemaphore;
	vk::Fence fence;

#ifndef NDEBUG
	vk::DebugUtilsMessengerEXT debugMessenger;
#endif

	struct {
		vk::VertexInputBindingDescription binding;
		std::vector<vk::VertexInputAttributeDescription> attrib;
	}vertex;

	std::unordered_map<std::string, vk::Pipeline> pipelines;
	std::unordered_map<std::string, vk::PipelineLayout> pipelineLayout;
	
	vk::DescriptorPool descPool;

	std::vector<const char*> instanceExtensions;
	std::vector<const char*> deviceExtensions;
	std::vector<const char*> validationLayers;
	std::vector<vk::QueueFamilyProperties> queueProp;
	std::vector<vk::Image> swapchainImages;
	std::vector<vk::ImageView> swapchainImageViews;

	uint32_t width, height;
	uint32_t graphicsQueueFamilyIndex;
	uint32_t frameCount;
};
```

### 层与扩展

层和扩展是在进行Vulkan开发时可以添加的附加功能，且随着Vulkan版本的更新，层和扩展也会不断增加，它们通常不是绝对必要的，但可以为开发提供极大便利。其中层被链接在驱动中，一般用于错误校验，调试输出，例如验证层——一个对Vulkan开发和DEBUG至关重要的功能，如果没有它我们将寸步难行。

为了将合适的验证层添加到我们的项目中，提供Vulkan的Debug功能，我们需要一个用于检测验证层可用性的方法，以防止不必要的错误，它的实现如下：

```cpp
bool CheckValidationLayerSupport(const std::vector<const char*>& validationLayers) {
	uint32_t layerCount = 0;
	vk::enumerateInstanceLayerProperties(&layerCount, static_cast<vk::LayerProperties*>(nullptr));
	if (layerCount == 0)
		return false;
	std::vector<vk::LayerProperties> availableLayers(layerCount);
	vk::enumerateInstanceLayerProperties(&layerCount, availableLayers.data());

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
}
```

在这里，我们会发现一个在Vulkan中会经常使用的逻辑，注意两次enumerateInstanceLayerProperties的使用，第一次确认的验证层的数量，第二次才实际获取了实际的验证层属性。

这个函数的功能是在不存在可用验证层或无法找到符合条件的验证层时返回false值，接下来我们将用这个函数检测我们要用的验证层是否可用。这里我们只使用Khronos和LunarG提供的标准验证层，它们是最基础和最必要的：

```cpp
#ifdef NDEBUG
	const bool enableValidationLayers = false;
#else
	const bool enableValidationLayers = true;
#endif

std::vector<const char*> validationLayers = {
	"VK_LAYER_KHRONOS_validation",
	"VK_LAYER_LUNARG_monitor"
};

if (enableValidationLayers && !CheckValidationLayerSupport(validationLayers)) {
	//Do not support validation layer
}

if (enableValidationLayers) {
	vkInfo.validationLayers = validationLayers;
	vkInfo.instanceExtensions.push_back(VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
}
```

我们这里添加了一个用于DEBUG的Vulkan扩展，除此以外我们想要在Win32平台上使用Vulkan也需要以下的几个基本扩展：

```cpp
//以下宏确保了Vulkan对于Win32平台的正确识别：
#define VK_USE_PLATFORM_WIN32_KHR

...

vkInfo.instanceExtensions.push_back(VK_KHR_WIN32_SURFACE_EXTENSION_NAME);
vkInfo.instanceExtensions.push_back(VK_KHR_SURFACE_EXTENSION_NAME);
vkInfo.deviceExtensions.push_back(VK_KHR_SWAPCHAIN_EXTENSION_NAME);
```

与层类似，我们也可以用vkEnumerateInstanceExtensionProperties()函数来查询层下所暴露出来的扩展数量和属性，并通过枚举来获取我们所需的扩展。

### CreateInstance

接下来我们就可以创建Vulkan实例，这需要两个结构体，将它们填写完成灌进函数中：

```cpp
//Application info
vk::ApplicationInfo applicationInfo;
applicationInfo.setApiVersion(VK_API_VERSION_1_3);
applicationInfo.setPEngineName("Vulkan Graphics Engine");
applicationInfo.setEngineVersion(1);
applicationInfo.setPApplicationName("Base Project");
applicationInfo.setApplicationVersion(1);

//Instance info
vk::InstanceCreateInfo instanceInfo;
instanceInfo.setEnabledExtensionCount(vkInfo.instanceExtensions.size());
instanceInfo.setPpEnabledExtensionNames(vkInfo.instanceExtensions.data());
instanceInfo.setPApplicationInfo(&applicationInfo);
instanceInfo.setEnabledLayerCount(vkInfo.validationLayers.size());
instanceInfo.setPpEnabledLayerNames(vkInfo.validationLayers.data());
```

ApplicationInfo这个结构体标记了和应用有关的信息，比较好理解，用于填写API版本和引擎相关信息。

VulkanAPI版本可以用以下函数进行枚举：

```cpp
// Provided by VK_VERSION_1_1
VkResult vkEnumerateInstanceVersion(uint32_t* pApiVersion);
```

Vulkan中很多用于创建接口的函数都会返回一个VkResult的值，将它与VkResult::VK_SUCCESS进行比较可以判断这个接口的创建是否正确进行。

InstanceInfo这个结构体需要灌进去实例扩展和验证层相关的信息，我在之前已准备完毕，接下来就可以完成创建：

```cpp
if (vk::createInstance(&instanceInfo, 0, &vkInfo.instance) != vk::Result::eSuccess) {
	//Error
}
```

值得一提的是，Vulkan在许多函数中提供了VkAllocationCallbacks参数用以手动管理主机的[内存分配](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=内存分配&zhida_source=entity)，但是大多数时候可以将其置空值来省事。

相应的，Vulkan也提供了回收实例对象的方法，有创建就有回收：

```cpp
vkInfo.instance.destroy();
```

### 创建DebugMessenger

我们需要一个方法来接受DEBUG的输出信息，否则之前的验证层就白设置了，这要求我们创建一个DebugUtilsMessengerCallback方法，并将其强转成PFN_vkDebugUtilsMessengerCallbackEXT的[函数指针](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=函数指针&zhida_source=entity)类型，它规定了DebugMessenger执行的原则，并允许我们去决定如何对待DEBUG信息，它的实现如下：

```cpp
static VKAPI_ATTR vk::Bool32 VKAPI_CALL DebugCallback(
	vk::DebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
	vk::DebugUtilsMessageTypeFlagsEXT messageType,
	const vk::DebugUtilsMessengerCallbackDataEXT* pCallbackData,
	void* pUserData) {
	OutputDebugStringA(pCallbackData->pMessage);
	return VK_FALSE;
}
PFN_vkDebugUtilsMessengerCallbackEXT CallbackFunc = reinterpret_cast<PFN_vkDebugUtilsMessengerCallbackEXT>(DebugCallback);
```

在定义该函数时，使用了几个Vulkan中函数的调用约定宏，即VKAPI_ATTR和VKAPI_CALL。

> 根据官方文档所述，VKAPI_ATTR放在[函数声明](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=函数声明&zhida_source=entity)的返回类型之前，用于 C++11 和 GCC/Clang 风格的函数属性语法；VKAPI_CALL放在函数声明中的返回类型之后，用于 MSVC 样式的调用约定语法。除此之外还有另一个调用约定宏，即VKAPI_PTR，放在函数指针类型的“(”和 "*" 之间，并且通常具有与“或”相同的定义，具体取决于编译器。

这个函数中的几个参数需要注意，messageSeverity规定了DebugMessenger需要输出的信息粒度，messageType规定了接受信息的种类，而另两个参数则是以指针形式存在的，pCallbackData负责接收[callback](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=callback&zhida_source=entity)的数据，pUserData负责接收用户定义的数据。在这个函数中，我们使用OutputDebugStringA()来将输出的Debug信息反馈在Visual Studio的输出栏中。

DebugCallback回调具有布尔返回值。VK_TRUE的返回值表示即使在发生错误后，[命令链](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=命令链&zhida_source=entity)仍然会继续传递到后续的验证层。但是，VK_FALSE值指示验证层在发生错误时[中止执行](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=中止执行&zhida_source=entity)。 建议在第一个错误处停止执行。

之后，我们通过填写一个VkDebugUtilsMessengerCreateInfoEXT结构体来创建debugMessenger：

```cpp
auto debugMessengerInfo = vk::DebugUtilsMessengerCreateInfoEXT()
		.setMessageSeverity(vk::DebugUtilsMessageSeverityFlagBitsEXT::eVerbose | vk::DebugUtilsMessageSeverityFlagBitsEXT::eWarning | vk::DebugUtilsMessageSeverityFlagBitsEXT::eError)
		.setMessageType(vk::DebugUtilsMessageTypeFlagBitsEXT::eGeneral | vk::DebugUtilsMessageTypeFlagBitsEXT::eValidation | vk::DebugUtilsMessageTypeFlagBitsEXT::ePerformance)
		.setPfnUserCallback(CallbackFunc)
		.setPUserData(nullptr);
vk::DispatchLoaderDynamic dispatch(vkInfo.instance, GetInstanceProcAddr);
if (vkInfo.instance.createDebugUtilsMessengerEXT(&debugMessengerInfo, 0, &vkInfo.debugMessenger, dispatch) != vk::Result::eSuccess) {
	//Error
}
```

该结构体中的内容对应了之前我们定义CallbackFunc时的参数。

在MessageSeverity中，我们指定接收信息的严重性程度，我选择需要除了info以外的所有程度信息，包括警告和错误；在MessageType中，我们知道接收的信息类型，这里我选择所有信息；由于我并不需要用户自定义数据，所以pUserData置空。

在最后，我们需要将DebugMessenger实际创建出来，但这里会遇到一个麻烦，因为VkCreateDebugUtilsMessengerEXT()是包含在扩展中的函数，所以它的定义并不会被直接[静态链接](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=静态链接&zhida_source=entity)到程序当中，所以我们需要Vulkan提供的vkGetInstanceProcAddr()函数进行查询，并用[动态链接](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=动态链接&zhida_source=entity)的方法将这个函数的定义链接进来，实现方式如下：

```cpp
vk::DynamicLoader dl;
PFN_vkGetInstanceProcAddr GetInstanceProcAddr = dl.getProcAddress<PFN_vkGetInstanceProcAddr>("vkGetInstanceProcAddr");
```

使用一个DynamicLoader对象得到VkGetInstanceProcAddr的函数指针之后，使用该指针和instance句柄创建一个用于分派动态链接的VkDispatchLoaderDynamic对象，之后就可以正确调用VkCreateDebugUtilsMessengerEXT()方法，实现如下（这边的写法有点玄学，我最开始搞了半天没搞清）：

```cpp
vk::DispatchLoaderDynamic dispatch(vkInfo.instance, GetInstanceProcAddr);
if (vkInfo.instance.createDebugUtilsMessengerEXT(&debugMessengerInfo, 0, &vkInfo.debugMessenger, dispatch) != vk::Result::eSuccess) {
	//Error
}
```

至此，我们的调试工具已经完成了配置。

### CreateDevice

在创建逻辑设备之前，通过枚举本机设备来查询可用的物理设备（GPU），它的实现如下：

```cpp
/*Enumerate physical device*/
uint32_t gpuCount = 0;
if (vkInfo.instance.enumeratePhysicalDevices(&gpuCount, static_cast<vk::PhysicalDevice*>(nullptr)) != vk::Result::eSuccess) {
	//Error
}

if (gpuCount > 0) {
	std::vector<vk::PhysicalDevice> gpus(gpuCount);
	vkInfo.instance.enumeratePhysicalDevices(&gpuCount, gpus.data());
	vkInfo.gpu = gpus[0];
}
else {
	//Cannot find GPU
}
```

两次enumeratePhysicalDevices的使用，第一次确认的[物理设备](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=4&q=物理设备&zhida_source=entity)的数量，第二次才实际获取了物理设备对应的句柄，这样的逻辑在最开始已经演示过了。

大部分枚举出的GPU设备会有两个，分别对应计算机的独显和集显，一般情况下选用第一个，当然也可以使用以下方法来获取设备信息，以查询使用的究竟是哪一个GPU：

```cpp
vk::PhysicalDeviceProperties properties;
vkInfo.gpu.getProperties(&properties);
```

通过查询VkSpec可以获取和properties有关的更多信息。

在获取GPU句柄之后，通过以下方法来获取GPU可用的[队列族](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=队列族&zhida_source=entity)属性：

```cpp
uint32_t queueFamilyCount = 0;

vkInfo.gpu.getQueueFamilyProperties(&queueFamilyCount, static_cast<vk::QueueFamilyProperties*>(nullptr));
vkInfo.queueProp.resize(queueFamilyCount);
vkInfo.gpu.getQueueFamilyProperties(&queueFamilyCount, vkInfo.queueProp.data());

bool found = false;
for (size_t i = 0; i < queueFamilyCount; i++) {
	if (vkInfo.queueProp[i].queueFlags & vk::QueueFlagBits::eGraphics) {
		vkInfo.graphicsQueueFamilyIndex = i;
		found = true;
		break;
	}
}
if (!found)
	//Cannot find queue family
```

在这里，我们用到了和上面获取GPU时相似的做法，不同的是我们需要检测获取的队列族是否满足拥有图形队列的条件，如果是，就直接将这个队列族作为要使用的队列族，这里使用了这样的方法进行判断：

```cpp
queueFlags & vk::QueueFlagBits::eGraphics
```

Vulkan提供了以下几种可以使用的队列族，分别对应了GPU处理时发挥不同作用的引擎，常用的有图形队列，计算队列和转移队列，而用于[视频流](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=视频流&zhida_source=entity)解码的队列族需要启用相应的扩展才可使用：

```cpp
typedef enum VkQueueFlagBits {
  VK_QUEUE_GRAPHICS_BIT = 0x00000001,
  VK_QUEUE_COMPUTE_BIT = 0x00000002,
  VK_QUEUE_TRANSFER_BIT = 0x00000004,
  VK_QUEUE_SPARSE_BINDING_BIT = 0x00000008,
  // Provided by VK_VERSION_1_1
  VK_QUEUE_PROTECTED_BIT = 0x00000010,
#ifdef VK_ENABLE_BETA_EXTENSIONS
  // Provided by VK_KHR_video_decode_queue
  VK_QUEUE_VIDEO_DECODE_BIT_KHR = 0x00000020,
#endif
#ifdef VK_ENABLE_BETA_EXTENSIONS
  // Provided by VK_KHR_video_encode_queue
  VK_QUEUE_VIDEO_ENCODE_BIT_KHR = 0x00000040,
#endif
} VkQueueFlagBits;
```

在VkSpec中介绍了queueProp中另外两个参数的含义：timestampVaildBits表示[时间戳](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=时间戳&zhida_source=entity)，用于为命令的执行进行计时。 最后一个参数minImageTransferGranularity指定当前队列族中支持的最小粒度的图像传输操作。

为了创建VkDevice接口，需要填写一个VkDeviceCreateInfo结构体，我们将queueCreateInfo，device扩展和enabledFeatures灌进去。

```cpp
auto feature = vk::PhysicalDeviceFeatures()
		.setGeometryShader(VK_TRUE);

float priorities[1] = { 0.0f };
deviceQueueInfo.setQueueCount(1);
deviceQueueInfo.setPQueuePriorities(priorities);
deviceQueueInfo.setQueueFamilyIndex(vkInfo.graphicsQueueFamilyIndex);
deviceInfo.setQueueCreateInfoCount(1);
deviceInfo.setPQueueCreateInfos(&deviceQueueInfo);
deviceInfo.setEnabledExtensionCount(vkInfo.deviceExtensions.size());
deviceInfo.setPpEnabledExtensionNames(vkInfo.deviceExtensions.data());
deviceInfo.setPEnabledFeatures(&feature);
```

VkPhysicalDeviceFeatures标记了一些需要启用的特性，例如此处为了方便之后使用几何着色器，我选择启用GeometryShader。

VkDeviceQueueInfo标记了如何使用队列族中的队列，这里我们将使用之前获取的图形队列族，同时为该队列族创建一个队列。pQueuePriorities为一个指向浮点数组的指针，用于指定提交给每个创建队列的作业的优先级。这里我们只创建一个队列，所以只会使用一个VkDeviceQueueInfo。

接下来创建出逻辑设备，获取VkDevice句柄：

```cpp
if (vkInfo.gpu.createDevice(&deviceInfo, 0, &vkInfo.device) != vk::Result::eSuccess) {
	//Error
}
```

至此，我们已经完成了创建instance和device的基本工作，真是可喜可贺。

## 补充

在Vulkan1.1，Khronos提供了可以控制VulkanSDK详细属性的vkconfig，位于VulkanSDK\1.3.216.0\Bin\vkconfig.exe目录下，有了这一工具我们可以更方便地对Vulkan[程序测试](https://zhida.zhihu.com/search?content_id=206942079&content_type=Article&match_order=1&q=程序测试&zhida_source=entity)进行调控，例如可以调整验证层VK_LAYER_KHRONOS_validation的属性，不过我注重于代码方面，对vkconfig的使用不会很多。

![img](./assets/v2-b9a4946abca6eeded5624afeb6b116be_1440w.jpg)



