# Vulkan 三角形之旅-6

> 这里是记录笔者Vulkan的学习记录，参照该教程[vulkan-tutorial.com](https://link.zhihu.com/?target=https%3A//vulkan-tutorial.com/Drawing_a_triangle/Drawing/Command_buffers)这里是记录笔者Vulkan的学习记录，如果你想识别Vulkan相比于之前的传统图形API有什么区别和优势的话，欢迎看我的另外一篇文章[初探Vulkan](https://zhuanlan.zhihu.com/p/554631289)。相信应该能够帮助你识别Vulkan的优势所在。

## **Frames in flight**

在上面我们已经完成了三角形的渲染。但是现在我们的Render Loop有一个明显的缺陷。我们使用Fence来等待前一帧完成，才能开始渲染下一帧，这会导致Host占用率不高，也就是说性能不够高

解决这个问题的方法是允许多个帧同时进行，也就是说，允许一帧的渲染不干扰下一帧的Record Command Buffer。我们如何做到这一点？在渲染期间关于GPU的资源都必须复制一份。因此，我们需要多个Command Buffer 、Semaphores和Fences。在后面的章节中，我们还将添加其他资源的多个实例，因此我们将看到这个概念再次出现。 首先在程序顶部添加一个常量，该常量定义应我们同时处理的帧数数量：

```cpp
const int MAX_FRAMES_IN_FLIGHT = 2;
```

我们选择2是因为我们不希望 CPU 远远领先于 GPU。有 2 帧在运行中，CPU 和 GPU 可以同时处理它们自己的任务。如果 CPU 提前完成，它将等到 GPU 完成渲染后再提交更多工作。如果有 3 帧或更多帧在运行，CPU 可能会领先于 GPU，从而增加帧延迟。通常我们不希望有额外的延迟。但是让应用程序控制运行的帧数是 Vulkan 显式的另一个例子。 每个帧都应该有自己的Command Buffer 、Semaphores和Fences。重命名然后将它们更改为std::vector格式

```cpp
std::vector<VkCommandBuffer> commandBuffers;

...

std::vector<VkSemaphore> imageAvailableSemaphores;
std::vector<VkSemaphore> renderFinishedSemaphores;
std::vector<VkFence> inFlightFences;
```

关于Command Buffer 的修改如下所示

```cpp
void createCommandBuffers() {
    commandBuffers.resize(MAX_FRAMES_IN_FLIGHT);
    ...
    allocInfo.commandBufferCount = (uint32_t) commandBuffers.size();

    if (vkAllocateCommandBuffers(device, &allocInfo, commandBuffers.data()) != VK_SUCCESS) {
        throw std::runtime_error("failed to allocate command buffers!");
    }
}
```

创建[同步原语](https://zhida.zhihu.com/search?content_id=211873461&content_type=Article&match_order=1&q=同步原语&zhida_source=entity)的操作如下所示

```cpp
void createSyncObjects() {
    imageAvailableSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
    renderFinishedSemaphores.resize(MAX_FRAMES_IN_FLIGHT);
    inFlightFences.resize(MAX_FRAMES_IN_FLIGHT);

    VkSemaphoreCreateInfo semaphoreInfo{};
    semaphoreInfo.sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;

    VkFenceCreateInfo fenceInfo{};
    fenceInfo.sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
    fenceInfo.flags = VK_FENCE_CREATE_SIGNALED_BIT;

    for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        if (vkCreateSemaphore(device, &semaphoreInfo, nullptr, &imageAvailableSemaphores[i]) != VK_SUCCESS ||
            vkCreateSemaphore(device, &semaphoreInfo, nullptr, &renderFinishedSemaphores[i]) != VK_SUCCESS ||
            vkCreateFence(device, &fenceInfo, nullptr, &inFlightFences[i]) != VK_SUCCESS) {

            throw std::runtime_error("failed to create synchronization objects for a frame!");
        }
    }
}
```

同样在Clean的时候，也要进行操作。

```cpp
for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        vkDestroySemaphore(device, renderFinishedSemaphores[i], nullptr);
        vkDestroySemaphore(device, imageAvailableSemaphores[i], nullptr);
        vkDestroyFence(device, inFlightFences[i], nullptr);
}
// 注意在这里我们不需要显式的去销毁Command Buffer
```

要在每一帧都使用正确的Command Buffer 和 同步原语，我们需要跟踪当前帧。为此，我们将使用帧索引，以及对于drawFrame进行修改。

```cpp
void drawFrame() {
    vkWaitForFences(device, 1, &inFlightFences[currentFrame], VK_TRUE, UINT64_MAX);
    vkResetFences(device, 1, &inFlightFences[currentFrame]);

    vkAcquireNextImageKHR(device, swapChain, UINT64_MAX, imageAvailableSemaphores[currentFrame], VK_NULL_HANDLE, &imageIndex);

    ...

    vkResetCommandBuffer(commandBuffers[currentFrame],  0);
    recordCommandBuffer(commandBuffers[currentFrame], imageIndex);

    ...

    submitInfo.pCommandBuffers = &commandBuffers[currentFrame];

    ...

    VkSemaphore waitSemaphores[] = {imageAvailableSemaphores[currentFrame]};

    ...

    VkSemaphore signalSemaphores[] = {renderFinishedSemaphores[currentFrame]};

    ...

    if (vkQueueSubmit(graphicsQueue, 1, &submitInfo, inFlightFences[currentFrame]) != VK_SUCCESS) 
        // 当然要记得每次都推进一帧
        currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;

}
```

## **Swap chain recreation**

我们现在的应用程序成功地画出了一个三角形，但有一些情况它还没有处理好。Window Surface有可能发生变化，以至于Swap Chain不再与之兼容。可能导致这种情况发生的原因之一是Window的大小发生变化。我们必须这些监听事件并重新创建Swap Chain。

### **Recreating the swap chain**

创建一个新的 recreateSwapChain 函数，该函数调用 createSwapChain 以及依赖于Swap Chain或window大小的对象的所有创建函数。

```cpp
void recreateSwapChain() {
    vkDeviceWaitIdle(device);

    createSwapChain();
    createImageViews();
    createFramebuffers();
}
```

我们首先调用 vkDeviceWaitIdle，因为就像在之前的操作中一样，我们不应该读取或者使用可能仍在使用的资源。显然，我们必须重新创建Swap Chain本身。ImageView需要重新创建，因为它们直接基于Swap Chain图像。最后，FrameBuffer直接依赖于Swap Chain图像，因此也必须重新创建。 为了确保这些对象的旧版本在重新创建它们之前被清理，我们应该将一些清理代码移动到一个单独的函数中，我们可以从 recreateSwapChain 函数调用该函数。我们称之为 cleanupSwapChain：

```cpp
void cleanupSwapChain() {

}
void recreateSwapChain() {
    vkDeviceWaitIdle(device);

    cleanupSwapChain();
        ....
}
```

请注意，为简单起见，我们不会在此处重新创建Render Pass。理论上，Swap Chain Format可能会在应用程序的生命周期内发生变化，例如将窗口从标准范围移动到HDR时。这可能需要应用程序重新创建RenderPass，以确保正确反映动态范围之间的变化。 我们会将作为Swap Chain刷新的一部分重新创建的所有对象的清理代码从 cleanup 移动到 cleanupSwapChain：

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
    ...
    cleanupSwapChain();
}
```

请注意，在 chooseSwapExtent 中，我们已经查询了新窗口的大小以确保Swap Chain图像具有正确大小，因此无需修改 chooseSwapExtent（请记住，我们已经使用 glfwGetFramebufferSize 获取表面的分辨率创建Swap Chain时的像素）。 这就是重新创建Swap Chain所需的全部内容！但是，这种方法的缺点是我们需要在创建新的Swap Chain之前停止所有渲染。可以在来自旧Swap Chain的图像上的绘图命令仍在进行中时创建新的Swap Chain。您需要将之前的Swap Chain传递给 VkSwapchainCreateInfoKHR 结构中的 oldSwapChain 字段，并在使用完旧Swap Chain后立即销毁它。

### **Suboptimal or out-of-date swap chain**

现在我们只需要弄清楚何时需要重新创建Swap Chain并调用我们的新 recreateSwapChain 函数。幸运的是，Vulkan 通常只会告诉我们Swap Chain在Present过程中已经不能够使用了。 vkAcquireNextImageKHR 和 vkQueuePresentKHR 函数可以返回以下特殊值来表明这一点。 VK_ERROR_OUT_OF_DATE_KHR：Swap Chain与Surface不兼容，不能再用于渲染。通常发生在window调整大小之后。 VK_SUBOPTIMAL_KHR：Swap Chain仍可用于成功Present到Window Surface，但Surface属性不再完全匹配。

```cpp
VkResult result = vkAcquireNextImageKHR(device, swapChain, UINT64_MAX, imageAvailableSemaphores[currentFrame], VK_NULL_HANDLE, &imageIndex);

if (result == VK_ERROR_OUT_OF_DATE_KHR) {
    recreateSwapChain();
    return;
} else if (result != VK_SUCCESS && result != VK_SUBOPTIMAL_KHR) {
    throw std::runtime_error("failed to acquire swap chain image!");
}
```

如果在尝试获取图像时Swap Chain已过期，则无法再向其Present。因此我们应该立即重新创建Swap Chain并在下一次 drawFrame 调用中重试。 如果Swap Chain不是最理想的，您也可以决定这样做，但我选择在这种情况下继续进行，因为我们已经获取了图像。 VK_SUCCESS 和 VK_SUBOPTIMAL_KHR 都被视为“成功”返回码。

```cpp
result = vkQueuePresentKHR(presentQueue, &presentInfo);

if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR) {
    recreateSwapChain();
} else if (result != VK_SUCCESS) {
    throw std::runtime_error("failed to present swap chain image!");
}

currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
```

vkQueuePresentKHR 函数返回具有相同含义的相同值。在这种情况下，如果Swap Chain不是最理想的，我们也会重新创建它，因为我们想要最好的结果。

### **Fixing a deadlock**

如果我们现在尝试运行代码，可能会遇到死锁。调试代码，我们发现应用程序到达 vkWaitForFences 但从未继续通过它。这是因为当 vkAcquireNextImageKHR 返回 VK_ERROR_OUT_OF_DATE_KHR 时，我们重新创建了Swap Chain，然后从 drawFrame 返回。但在此之前，当前帧的Fence被等待并重置。由于我们立即返回，因此没有人格工作提交执行，并且Fence永远不会发出信号，导致 vkWaitForFences 让Render Loop永远停止。 有一个简单的解决方法。延迟重置Fence，直到我们确定我们将提交使用它的工作。因此，如果我们提前返回，Fence仍然会发出信号，并且 vkWaitForFences 不会在我们下次使用同一个Fence对象时死锁。

```cpp
if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR) {
   ...
}
// Only reset the fence if we are submitting work
vkResetFences(device, 1, &inFlightFences[currentFrame]);
```

### **Handling resizes explicitly**

尽管许多[驱动程序](https://zhida.zhihu.com/search?content_id=211873461&content_type=Article&match_order=1&q=驱动程序&zhida_source=entity)和平台在调整窗口大小后会自动触发 VK_ERROR_OUT_OF_DATE_KHR，但不能保证一定会发生。这就是为什么我们将添加一些额外的代码来显式地处理Window调整大小。首先添加一个新的成员变量来标记是否发生了Window调整大小的操作：

```cpp
bool framebufferResized = false;

if (result == VK_ERROR_OUT_OF_DATE_KHR || result == VK_SUBOPTIMAL_KHR || framebufferResized) {
    framebufferResized = false;
    recreateSwapChain();
} else if (result != VK_SUCCESS) {
    ...
}
```

在 vkQueuePresentKHR 之后执行此操作很重要，以确保信号量处于一致状态，否则可能永远无法正确等待已发出信号量。现在要实际检测调整大小，我们可以使用 GLFW 框架中的 glfwSetFramebufferSizeCallback 函数来设置回调：

```cpp
void initWindow() {
    glfwInit();

    glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

    window = glfwCreateWindow(WIDTH, HEIGHT, "Vulkan", nullptr, nullptr);
        glfwSetWindowUserPointer(window, this);
    glfwSetFramebufferSizeCallback(window, framebufferResizeCallback);
}

static void framebufferResizeCallback(GLFWwindow* window, int width, int height) {
    auto app = reinterpret_cast<HelloTriangleApplication*>(glfwGetWindowUserPointer(window));
    app->framebufferResized = true;
}
```

我们创建一个[静态函数](https://zhida.zhihu.com/search?content_id=211873461&content_type=Article&match_order=1&q=静态函数&zhida_source=entity)作为Callback的原因是因为 GLFW 不知道如何正确调用带有指向 HelloTriangleApplication 实例的 this 指针的成员函数。 但是，我们确实在Callback中获得了对 GLFWwindow 的引用，并且还有另一个 GLFW 函数允许您在其中存储任意指针：glfwSetWindowUserPointer。现在尝试运行程序并调整窗口大小，以查看Frame Buffer是否确实与Window正确调整大小。

### **Handling minimization**

还有另一种情况，Swap Chain可能会过时，这是一种特殊的Window大小调整：Window最小化。这种情况很特殊，因为它会导致Frame Buffer大小为 0。在本教程中，我们将通过扩展 recreateSwapChain 函数暂停直到窗口再次处于前台来处理它：

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

对 glfwGetFramebufferSize 的初始调用处理大小已经正确且 glfwWaitEvents 没有任何等待的情况。 恭喜，您现在已经完成了您的第一个表现良好的 Vulkan 程序！在后续我们将摆脱[顶点着色器](https://zhida.zhihu.com/search?content_id=211873461&content_type=Article&match_order=1&q=顶点着色器&zhida_source=entity)中的硬编码顶点，并实际使用[顶点缓冲区](https://zhida.zhihu.com/search?content_id=211873461&content_type=Article&match_order=1&q=顶点缓冲区&zhida_source=entity)。到了这里你已经在Vulkan当中完成了渲染一个三角形的艰难工作！！！ good job ！！