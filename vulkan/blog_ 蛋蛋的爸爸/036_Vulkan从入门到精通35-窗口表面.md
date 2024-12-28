# Vulkan从入门到精通35-窗口表面

本文来介绍下窗口表面。Vulkan是窗口无关的API，为了将Vulkan 渲染的图像显示在窗口上，我们需要使用 Vulkan扩展。

Vulkan中表面对象是VkSurfaceKHR。它的创建依赖窗口系统。比如，在 Windows 系统上，它的创建需要 HWND 和 HMODULE。 存在一个叫做 VK_KHR_win32_surface 的 Windows 平台特有扩展，用于处理与 Windows 系统窗口交互有关的问题。在X11上则需要xcb_window_t和xcb_connection_t。

在X11上创建vulkan表面，实例代码如下

```cpp
bool createVkSurfaceXCB(const VkInstance theInstance, xcb_connection_t * const xcbConnection, const xcb_window_t & xcbWindow, VkSurfaceKHR & outInstance)
{
	VkResult result;
	VkSurfaceKHR mySurface;

	VkXcbSurfaceCreateInfoKHR xlibSurfaceCreateInfo {
		.sType = VK_STRUCTURE_TYPE_XCB_SURFACE_CREATE_INFO_KHR,
		.pNext = nullptr,
		.flags = 0,
		.connection = xcbConnection,
		.window = xcbWindow
	};

	result = vkCreateXcbSurfaceKHR(theInstance, &xlibSurfaceCreateInfo, nullptr, &mySurface);
	assert(result == VK_SUCCESS);

	outInstance = mySurface;
	return true;
}
```

在glfw框架中使用glfwCreateWindowSurface(instance, window, *getAllocation*(), &surface) 即可创建vulkan表面



Vulkan SDK自带demo中创建surface的函数如下

```cpp
static void demo_create_surface(struct demo *demo) {
    VkResult U_ASSERT_ONLY err;

// Create a WSI surface for the window:
#if defined(VK_USE_PLATFORM_WIN32_KHR)
    VkWin32SurfaceCreateInfoKHR createInfo;
    createInfo.sType = VK_STRUCTURE_TYPE_WIN32_SURFACE_CREATE_INFO_KHR;
    createInfo.pNext = NULL;
    createInfo.flags = 0;
    createInfo.hinstance = demo->connection;
    createInfo.hwnd = demo->window;

    err = vkCreateWin32SurfaceKHR(demo->inst, &createInfo, NULL, &demo->surface);
#elif defined(VK_USE_PLATFORM_WAYLAND_KHR)
    VkWaylandSurfaceCreateInfoKHR createInfo;
    createInfo.sType = VK_STRUCTURE_TYPE_WAYLAND_SURFACE_CREATE_INFO_KHR;
    createInfo.pNext = NULL;
    createInfo.flags = 0;
    createInfo.display = demo->display;
    createInfo.surface = demo->window;

    err = vkCreateWaylandSurfaceKHR(demo->inst, &createInfo, NULL, &demo->surface);
#elif defined(VK_USE_PLATFORM_ANDROID_KHR)
    VkAndroidSurfaceCreateInfoKHR createInfo;
    createInfo.sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR;
    createInfo.pNext = NULL;
    createInfo.flags = 0;
    createInfo.window = (struct ANativeWindow *)(demo->window);

    err = vkCreateAndroidSurfaceKHR(demo->inst, &createInfo, NULL, &demo->surface);
#elif defined(VK_USE_PLATFORM_XLIB_KHR)
    VkXlibSurfaceCreateInfoKHR createInfo;
    createInfo.sType = VK_STRUCTURE_TYPE_XLIB_SURFACE_CREATE_INFO_KHR;
    createInfo.pNext = NULL;
    createInfo.flags = 0;
    createInfo.dpy = demo->display;
    createInfo.window = demo->xlib_window;

    err = vkCreateXlibSurfaceKHR(demo->inst, &createInfo, NULL, &demo->surface);
#elif defined(VK_USE_PLATFORM_XCB_KHR)
    VkXcbSurfaceCreateInfoKHR createInfo;
    createInfo.sType = VK_STRUCTURE_TYPE_XCB_SURFACE_CREATE_INFO_KHR;
    createInfo.pNext = NULL;
    createInfo.flags = 0;
    createInfo.connection = demo->connection;
    createInfo.window = demo->xcb_window;

    err = vkCreateXcbSurfaceKHR(demo->inst, &createInfo, NULL, &demo->surface);
#elif defined(VK_USE_PLATFORM_DIRECTFB_EXT)
    VkDirectFBSurfaceCreateInfoEXT createInfo;
    createInfo.sType = VK_STRUCTURE_TYPE_DIRECTFB_SURFACE_CREATE_INFO_EXT;
    createInfo.pNext = NULL;
    createInfo.flags = 0;
    createInfo.dfb = demo->dfb;
    createInfo.surface = demo->window;

    err = vkCreateDirectFBSurfaceEXT(demo->inst, &createInfo, NULL, &demo->surface);
#elif defined(VK_USE_PLATFORM_DISPLAY_KHR)
    err = demo_create_display_surface(demo);
#elif defined(VK_USE_PLATFORM_METAL_EXT)
    VkMetalSurfaceCreateInfoEXT surface;
    surface.sType = VK_STRUCTURE_TYPE_METAL_SURFACE_CREATE_INFO_EXT;
    surface.pNext = NULL;
    surface.flags = 0;
    surface.pLayer = demo->caMetalLayer;

    err = vkCreateMetalSurfaceEXT(demo->inst, &surface, NULL, &demo->surface);
#endif
    assert(!err);
}
```

对应的销毁函数则统一为vkDestroySurfaceKHR

```cpp
vkDestroySurfaceKHR(instance, surface, getAllocation());
```

------

Vulkan 实现可能对窗口系统进行了支持，但这并不意味着所有平台的 Vulkan 实现都支持同样的特性。而且支持绘制指令的队列族和支持表现的队列族并不一定重叠。所以需要二个队列；

结合具体的物理设备就可以通过vkGetPhysicalDeviceSurfaceSupportKHR查询是否支持呈现能力；

查询是否具有绘制能力则可以通过vkGetPhysicalDeviceQueueFamilyProperties查询

```cpp
QueueFamilyIndices VK_ContextImpl::findQueueFamilies(VkPhysicalDevice device)
{
    QueueFamilyIndices indices;

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

    return indices;
}
```

QueueFamilyIndices结构如下，绘制和呈现二个才可以支持把图形显示到窗口上

```cpp
struct QueueFamilyIndices {
    std::optional<uint32_t> graphicsFamily;
    std::optional<uint32_t> presentFamily;


    bool isComplete()
    {
        return graphicsFamily.has_value() && presentFamily.has_value();
    }
};
```

