# Vulkan开发学习记录 15 - 重建交换链

## 介绍

我们已经成功地编写代码使用Vulkan 在屏幕上绘制出了一个三角形， 但这个程序还有许多细节问题我们没有处理。比如，窗口大小改变会导致[交换链](https://zhida.zhihu.com/search?content_id=218608961&content_type=Article&match_order=1&q=交换链&zhida_source=entity)和窗口不再适配，我们需要重新对交换链进行处理。

## 重建交换链

我们添加一个叫做recreateSwapChain 的函数，它会调用createSwapChain 函数和其它一些依赖于交换链和窗口大小的对象的创建函数：

```cpp
void recreateSwapChain() {
    vkDeviceWaitIdle(device);

    createSwapChain();
    createImageViews();
    createFramebuffers();
}
```

上面代码，我们首先调用vkDeviceWaitIdle 函数等待设备处于空闲状态，避免在对象的使用过程中将其清除重建。接着，我们重新创建了交换链。图形视图是直接依赖于交换链图像的，所以也需要被重建图像视图。渲染流程依赖于交换链图像的格式，虽然像窗口大小改变不会引起使用的交换链图像格式改变，但我们还是应该对它进行处理。视口和裁剪矩形在管线创建时被指定，窗口大小改变，这些设置也需要修改，所以我们也需要重建管线。实际上，我们可以通过使用动态状态来设置视口和裁剪矩形来避免重建管线。帧缓冲和[指令缓冲](https://zhida.zhihu.com/search?content_id=218608961&content_type=Article&match_order=1&q=指令缓冲&zhida_source=entity)直接依赖于交换链图像，也需要重建。

我们需要在重建前，清除之前使用的对象，所以我们将交换链相关的清除代码从[cleanup函数](https://zhida.zhihu.com/search?content_id=218608961&content_type=Article&match_order=1&q=cleanup函数&zhida_source=entity)中分离出来作为一个独立的cleanupSwapChain 函数，然后在recreateSwapChain 函数中调用它：

```cpp
void cleanupSwapChain() {

}

void recreateSwapChain() {
    vkDeviceWaitIdle(device);

    cleanupSwapChain();

    createSwapChain();
    createImageViews();
    createFramebuffers();
}
```

将交换链相关的清除代码从 cleanup 中移出到 cleanupSwapChain 函数中：

```cpp
void cleanupSwapChain() {
    for (size_t i = 0; i < swapChainFramebuffers.size(); i++) {
        vkDestroyFramebuffer(device, swapChainFramebuffers[i], nullptr);
    }

    for (size_t i = 0; i < swapChainImageViews.size(); i++) {
        vkDestroyImageView(device, swapChainImageViews[i], nullptr);
    }

    vkDestroySwapchainKHR(device, swapChain, nullptr);
}

void cleanup() {
    cleanupSwapChain();

    vkDestroyPipeline(device, graphicsPipeline, nullptr);
    vkDestroyPipelineLayout(device, pipelineLayout, nullptr);

    vkDestroyRenderPass(device, renderPass, nullptr);

    for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        vkDestroySemaphore(device, renderFinishedSemaphores[i], nullptr);
        vkDestroySemaphore(device, imageAvailableSemaphores[i], nullptr);
        vkDestroyFence(device, inFlightFences[i], nullptr);
    }

    vkDestroyCommandPool(device, commandPool, nullptr);

    vkDestroyDevice(device, nullptr);

    if (enableValidationLayers) {
        DestroyDebugUtilsMessengerEXT(instance, debugMessenger, nullptr);
    }

    vkDestroySurfaceKHR(instance, surface, nullptr);
    vkDestroyInstance(instance, nullptr);

    glfwDestroyWindow(window);

    glfwTerminate();
}
```

对于指令池对象，我们不需要重建，只需要调用vkFreeCommandBuffers 函数清除它分配的指令缓冲对象即可。

窗口大小改变后，我们需要重新设置交换链图像的大小，这一设置可以通过修改chooseSwapExtent 函数，让它设置交换范围为当前的帧缓冲的实际大小，然后我们在需要的地方调用chooseSwapExtent 函数即可。

至此，我们就完成了交换链重建的所有工作！但是，我们使用的这一重建方法需要等待正在执行的所有设备操作结束才能进行。实际上，是可以在渲染操作执行，原来的交换链仍在使用时重建新的交换链，只需要在创建新的交换链时使用VkSwapchainCreateInfoKHR 结构体的oldSwapChain [成员变量](https://zhida.zhihu.com/search?content_id=218608961&content_type=Article&match_order=1&q=成员变量&zhida_source=entity)引用原来的交换链即可。之后，在旧的交换链结束使用时就可以清除它。

## 交换链不完全匹配和交换链过期

现在我们只需要在交换链必须被重建时调用recreateSwapChain 函数重建交换链即可。我们可以根据vkAcquireNextImageKHR 和vkQueuePresentKHR 函数返回的信息来判定交换链是否需要重建：

• VK_ERROR_OUT_OF_DATE_KHR：交换链不能继续使用。通常发生在窗口大小改变后。

• VK_SUBOPTIMAL_KHR：交换链仍然可以使用，但表面属性已经不能准确匹配。

```cpp
VkResult result = vkAcquireNextImageKHR(device, swapChain, UINT64_MAX, imageAvailableSemaphores[currentFrame], VK_NULL_HANDLE, &imageIndex);

if (result == VK_ERROR_OUT_OF_DATE_KHR) {
    recreateSwapChain();
    return;
} else if (result != VK_SUCCESS && result != VK_SUBOPTIMAL_KHR) {
    throw std::runtime_error("failed to acquire swap chain image!");
}
```

当交换链过期时，我们就不能再使用它，必须重建交换链。 但是，如果在获取交换链不可继续使用后，立即跳出这一帧的渲染， 会导致我们使用的栅栏（fence）处于我们不能确定得状态。所以，我们应该在重建交换链时，重置栅栏（fence）对象，这可以通过调用vkResetFences 函数完成。

我们可以在交换链不完全匹配时进行一些处理，在这里，我们没有选 择在交换链不完全匹配时中断这一帧的渲染，毕竟，这种情况下，我们实际 取得了可以用来绘制的交换链的图像。可以认为VK_SUCCESS 和VK_SUBOPTIMAL_KHR 都说明成功获取到了交换链图像：

```cpp
result = vkQueuePresentKHR(presentQueue, &presentInfo);

if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR) {
    recreateSwapChain();
} else if (result != VK_SUCCESS) {
    throw std::runtime_error("failed to present swap chain image!");
}

currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
```

vkQueuePresentKHR 函数的返回值与vkAcquireNextImageKHR 函数的返回值有着相同的意义。在这里，为了保证最佳渲染效果，我们选择在交换链不完全匹配时也重建交换链。

## 显式处理窗口大小改变

尽管许多[驱动程序](https://zhida.zhihu.com/search?content_id=218608961&content_type=Article&match_order=1&q=驱动程序&zhida_source=entity)会在窗口大小改变后触发VK_ERROR_OUT_OF_DATE_KHR 信息，但这种触发并不可靠，所以我们最好添加一些代码来显式地在窗口大小改变时重建交换链。我们添加一个新的变量来标记窗口大小是否发生改 变：

```cpp
std::vector<VkFence> inFlightFences;

bool framebufferResized = false;
```

修改drawFrame 函数，检测我们加入的标记：

```cpp
if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR || framebufferResized) {
    framebufferResized = false;
    recreateSwapChain();
} else if (result != VK_SUCCESS) {
    ...
}
```

使用`glfwSetFramebufferSizeCallback` 函数设置处理窗口大小改变的[回调函数](https://zhida.zhihu.com/search?content_id=218608961&content_type=Article&match_order=1&q=回调函数&zhida_source=entity)：

```cpp
void initWindow() {
    glfwInit();

    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

    window = glfwCreateWindow(WIDTH, HEIGHT, "Vulkan", nullptr, nullptr);
    glfwSetFramebufferSizeCallback(window, framebufferResizeCallback);
}

static void framebufferResizeCallback(GLFWwindow* window, int width, int height) {

}
```

需要注意代码中，我们定义framebufferResizeCallback为[静态函数](https://zhida.zhihu.com/search?content_id=218608961&content_type=Article&match_order=1&q=静态函数&zhida_source=entity)，这样才能将其用作回调函数。framebufferResizeCallback 回调函数有一个GLFWWindow 类型的参数，我们可以使用这一参数来引用GLFW 窗口。GLFW 允许我们将任意的指针使用glfwSetWindowUserPointer 函数存储在GLFW 窗口相关的数据中：

```cpp
window = glfwCreateWindow(WIDTH, HEIGHT, "Vulkan", nullptr, nullptr);
glfwSetWindowUserPointer(window, this);
glfwSetFramebufferSizeCallback(window, framebufferResizeCallback);
```

我们可以通过glfwSetWindowUserPointer 函数获取使用glfwSetWindowUserPointer函数存储的指针，在这里我们使用它们来存储应用程序类的this 指针：

```cpp
static void framebufferResizeCallback(GLFWwindow* window, int width, int height) {
    auto app = reinterpret_cast<HelloTriangleApplication*>(glfwGetWindowUserPointer(window));
    app->framebufferResized = true;
}
```

现在编译运行程序，改变应用程序窗口大小，观察帧缓冲是否随着窗 口大小改变而被正确地重新设置。

## 处理窗口最小化

还有一种特殊情况需要处理，这就是窗口的最小化。这时窗口的帧缓冲实际大小为0。在这里，我们设置应用程序在窗口最小化后停止渲染，直到窗口重新可见时重建交换链：

```cpp
void recreateSwapChain() {
    int width = 0, height = 0;
    glfwGetFramebufferSize(window, &width, &height);
    while (width == 0 || height == 0) {
        glfwGetFramebufferSize(window, &width, &height);
        glfwWaitEvents();
    }

    vkDeviceWaitIdle(device);

    ...
}
```

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

