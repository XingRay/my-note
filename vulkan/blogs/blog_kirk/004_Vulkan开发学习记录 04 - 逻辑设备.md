# Vulkan开发学习记录 04 - 逻辑设备

## 简述

选择要使用的物理设备后，我们需要设置一个*逻辑设备*来与其交互。逻辑设备创建过程与实例创建过程类似，描述了我们要使用的功能。

创建逻辑设备的方法是create_vulkan_devices，其中主要包括获取物理设备队列家族列表和队列家族属性、遍历队列家族列表及创建逻辑设备等步骤。

## 创建逻辑设备

首先调用vkGetPhysicalDeviceQueueFamilyProperties方法获取了指定物理设备中的队列家族数量，然后通过vkGetPhysicalDeviceQueueFamilyProperties方法填充了对应的队列家族属性列表。

```cpp
vkGetPhysicalDeviceQueueFamilyProperties(gpus[0], &queueFamilyCount, NULL);//获取物理设备0中队列家族的数量
printf("[Vulkan硬件设备0支持的队列家族数量为%d]\n", queueFamilyCount);
queueFamilyprops.resize(queueFamilyCount);//随队列家族数量改变vector长度
vkGetPhysicalDeviceQueueFamilyProperties(gpus[0], &queueFamilyCount, queueFamilyprops.data());//填充物理设备0队列家族属性列表
printf("[成功获取Vulkan硬件设备0支持的队列家族属性列表]\n");
```

[遍历队列](https://zhida.zhihu.com/search?content_id=216913833&content_type=Article&match_order=2&q=遍历队列&zhida_source=entity)家族属性列表，找到其中支持图形工作的一个队列家族并记录其索引。

```cpp
for (unsigned int i = 0; i < queueFamilyCount; i++) {//遍历所有队列家族
	if (queueFamilyprops[i].queueFlags & VK_QUEUE_GRAPHICS_BIT) {//若当前队列家族支持图形工作
		queueInfo.queueFamilyIndex = i;//绑定此队列家族索引
		queueGraphicsFamilyIndex = i;//记录支持图形工作的队列家族索引
		printf("[支持GRAPHICS工作的一个队列家族的索引为%d]\n", i);
		printf("[此家族中的实际队列数量是%d]\n", queueFamilyprops[i].queueCount);
		found = true;
		break;
	}
}
```

构建设备队列创建信息结构实例。首先设置结构体的类型，然后指定队列数量并给出每个队列的优先级，最后绑定前面得到的支持图形操作的队列家族索引。

```cpp
float queue_priorities[1] = { 0.0 };//创建队列优先级数组
queueInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;//给出结构体类型
queueInfo.pNext = NULL;//自定义数据的指针
queueInfo.queueCount = 1;//指定队列数量
queueInfo.pQueuePriorities = queue_priorities;//给出每个队列的优先级
queueInfo.queueFamilyIndex = queueGraphicsFamilyIndex;//绑定队列家族索引
```

设置逻辑设备所需的扩展名称列表，这里所需的扩展只有一个，名称为VK_KHR_SWAPCHAIN_EXTENSION_NAME，其功能为使创建的设备支持[交换链](https://zhida.zhihu.com/search?content_id=216913833&content_type=Article&match_order=1&q=交换链&zhida_source=entity)的使用。若不打开此设备扩展，则程序无法执行在目标平台下呈现画面的工作。

```cpp
deviceExtensionNames.push_back(VK_KHR_SWAPCHAIN_EXTENSION_NAME);//设置所需扩展
```

构建设备创建信息结构实例。首先设置结构体类型，接着设置队列创建信息结构体的数量，然后给定队列创建信息结构实例列表，接着给出所需设备扩展的数量和名称列表，然后设置启动Layer的而数量和对应的Layer的名称列表，最后给出启用的设备特性。

```cpp
VkDeviceCreateInfo deviceInfo = {};//构建逻辑设备创建信息结构体实例
deviceInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;//给出结构体类型
deviceInfo.pNext = NULL;//自定义数据的指针
deviceInfo.queueCreateInfoCount = 1;//指定设备队列创建信息结构体数量
deviceInfo.pQueueCreateInfos = &queueInfo;//给定设备队列创建信息结构体列表
deviceInfo.enabledExtensionCount = deviceExtensionNames.size();//所需扩展数量
deviceInfo.ppEnabledExtensionNames = deviceExtensionNames.data();//所需扩展列表
deviceInfo.enabledLayerCount = 0;//需启动Layer的数量
deviceInfo.ppEnabledLayerNames = NULL;//需启动Layer的名称列表
deviceInfo.pEnabledFeatures = NULL;//启用的设备特性
```

执行逻辑设备的创建并检查逻辑设备是否创建成功。

```cpp
VkResult result = vkCreateDevice(gpus[0], &deviceInfo, NULL, &device);//创建逻辑设备
assert(result == VK_SUCCESS);//检查逻辑设备是否创建成功
```

