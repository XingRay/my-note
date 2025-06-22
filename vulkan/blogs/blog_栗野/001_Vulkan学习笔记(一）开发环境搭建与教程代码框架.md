# Vulkan学习笔记(一）开发环境搭建与教程代码框架

本系列文章是个人学习[https://vulkan-tutorial.com](https://link.zhihu.com/?target=https%3A//vulkan-tutorial.com) Vulkan教程的学习笔记，主要为了梳理Vulkan API各种复杂概念和状态记录了教程中重点的备忘，文中如有疏漏的部分可以去看译文和原文的内容。在这里也非常感谢fangcun010的翻译与分享。这篇文章也是系列的第一篇，主要记录的是[开发环境](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=开发环境&zhida_source=entity)搭建和教程基本代码框架的描述。在接下来一两个月中会定期发布这个系列，一直总结到教程学习结束。本系列对应的github仓库：[https://github.com/kurinokaitou/LearnVK](https://link.zhihu.com/?target=https%3A//github.com/kurinokaitou/LearnVK)

### 环境配置

本文中开发环境的配置与教程中有所差别，不过基本上都是配置相同的依赖库，包括Vulkan SDK、glfw和glm等。具体的C++和Vulkan开发环境网络上详细的教程已经有很多，这里提供一种比较简单的方式。

我使用的是Visual Studio 2022 + CMake + vcpkg的方式配置的本教程的开发环境。CMake和VS自不用说，VS对CMake跨平台工程的构建提供了非常多的支持，这里主要引入的是vcpkg C++包管理器和两者使用上的配合。

**安装vcpkg并安装[依赖库](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=2&q=依赖库&zhida_source=entity)**

安装vcpkg之前需要确保安装了最新的CMake和PowerShell，否则会自动下载，而且它的默认下载源网速基本上很难下载成功。安装好vcpkg之后在[环境变量](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=环境变量&zhida_source=entity)中添加`VCPKG_ROOT_PATH`代表安装的[根目录](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=根目录&zhida_source=entity)之后，通过命令行可以用`./vcpkg search`搜索想要安装的包。这里我们要安装的是`glm`和`glfw`，分别是常用的矩阵向量数学库和GUI框架库，安装两个库的命令如下：

```bash
./vcpkg install glm:x64-windows
./vcpkg install glfw3:x64-windows
```

之后只需要通过在CMakeLists.txt中使用`find_package`就可以引入项目，甚至也不需要vcpkg集成到VS，可以说非常方便。

**安装Vulkan SDK**

Vulkan SDK在vcpkg里的版本比较旧，而且本身Vulkan SDK也有一个自己的安装管理器来自定义安装，所以我们还是使用直接到LunarG官网[https://vulkan.lunarg.com/sdk/home](https://link.zhihu.com/?target=https%3A//vulkan.lunarg.com/sdk/home)下载SDK的方式来配置Vulkan。下载安装除了本体还可以选择（VMA）Vulkan Memory Allocator来帮助管理Vulkan程序的内存。

**通过CMake来构建项目**

这里直接给出一个简单项目的CMakeLists.txt，在VS中创建CMake项目之后将原始CMakeLists.txt文件替换即可。

```cmake
cmake_minimum_required (VERSION 3.8)
# 设置cmake工具链为vcpkg
if(NOT DEFINED ENV{VCPKG_ROOT_PATH})
    message(FATAL_ERROR "VCPKG_ROOT_PATH not defined!")
endif()
set(VCPKG_PATH $ENV{VCPKG_ROOT_PATH})
set(VCPKG_ROOT ${VCPKG_PATH}/scripts/buildsystems/vcpkg.cmake CACHE PATH "")
set(CMAKE_TOOLCHAIN_FILE ${VCPKG_ROOT})

# 项目设置
project ("LearnVK") # 替换为自己的项目名称
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
# Vulkan SDK设置
if(NOT DEFINED ENV{VK_SDK_PATH})
    message(FATAL_ERROR "VK_SDK_PATH not defined!")
endif()
set(VK_SDK_PATH $ENV{VK_SDK_PATH})
set(VK_SDK_INCLUDE ${VK_SDK_PATH}/Include)
set(VK_SDK_LIB ${VK_SDK_PATH}/Lib/vulkan-1.lib)

# vcpkg安装的glm和glfw3引入包
find_package(glm CONFIG REQUIRED)
find_package(glfw3 CONFIG REQUIRED)

# 将源代码添加到此项目的可执行文件
file(GLOB_RECURSE HEADER_FILES ${PROJECT_SOURCE_DIR} "include/*.h")
file(GLOB_RECURSE SOURCE_FILES ${PROJECT_SOURCE_DIR} "src/*.cpp")

source_group(TREE "${CMAKE_CURRENT_SOURCE_DIR}" FILES ${HEADER_FILES} ${SOURCE_FILES})
add_executable(LearnVK ${SOURCE_FILES})

# 链接到库文件
target_link_libraries(LearnVK PRIVATE glm::glm)
target_link_libraries(LearnVK PRIVATE glfw)
target_link_libraries(LearnVK PRIVATE ${VK_SDK_LIB})

# 添加包含目录
target_include_directories(LearnVK PRIVATE ${PROJECT_SOURCE_DIR}/include)
target_include_directories(LearnVK PRIVATE ${VK_SDK_INCLUDE})
```

配置完开发环境，就可以正式开始Vulkan的学习了。

### 初始Vulkan：画一个三角形

### 渲染流程概述

1. 创建`VkInstance`实例和选择物理设备`VkPhysicalDevice`
2. 创建逻辑设备`VkDevice`然后指定使用的[队列族](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=队列族&zhida_source=entity)：一个队列族是一个特定的操作集合。
3. 窗口步骤和交换链：窗口可以使用`glfw`。对于窗口的渲染，需要用的窗口表面`VkSurfaceKHR`和`VkSwapChainKHR`。
4. 图形视图和帧缓冲。大致得依赖关系如下：

```text
flowchart LR
     VkFrameBuffer --> VkImageView  --> Image
```

1. 渲染流程，渲染一个三角形只需要渲染到一张图像的颜色目标即可([渲染目标](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=渲染目标&zhida_source=entity)），渲染前先创建并清除`FrameBuffer`。

2. 图形管线。`VkPipeline`描述的是显卡可以配置的状态。比如其中`VkShaderModule`对象的可编程状态。图形管线的配需要在渲染前完成，意味着重新使用另外的着色器或是顶点布局需要重新创建整个图形管线。因此我们的策略是**提前创建你好所有需要的图形管线**，这样切换不同的管线也会更快，并且渲染预期效果也会更容易。

3. 指令池和[指令缓冲](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=指令缓冲&zhida_source=entity)。所有操作被提交到`VkCommandBuffer`对象，然后提交给队列。这个对象由`VkCommandPool`分配。这些指令包括开始渲染、绑定管线、真正绘制、结束渲染。每个指令缓冲对应交换链中的一张图像，需要在主循环开始前分配。

4. 主循环：

5. 1. 首先使 用`vkAcquireNextImageKHR`函数从交换链获取一张图像。**（获取）**
   2. 接着使用`vkQueueSubmit`函数提交图像对应的指令缓冲。**（提交）**
   3. 最后，使用`vkQueuePresentKHR`函数将图像 返回给交换链，显示图像到屏幕**（返回）**

这一期间提交队列的操作会被异步执行，因此需要信号量来同步。上面三部必须按照顺序进行。

### Vulkan[编码规范](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=编码规范&zhida_source=entity)

- Vulkan的函数都带有一个小写的vk前缀，枚举和结构体名带有一个Vk前缀，[枚举值](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=枚举值&zhida_source=entity)带有一个VK_前缀。Vulkan对结构体非常依赖，大量函数的参数由结构体提供，类似于DX的Description，通过填入结构体为指定的API设置参数。
- 所有的API都会返回一个`VkResult`来表示成功与否，错误会返回不同的代码。这就需要我们处理好API的异常，配合后面提到的校验层建立一套[异常处理](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=异常处理&zhida_source=entity)机制。

### Vulkan校验层

为了高性能低驱动的特性，在发生硬件错误会直接崩溃。因此在调试的时候需要一层Vulkan的校验层供调试，在官方的SDK中就给出了校验层的实现。

### 一个基础的Vulkan Application类

### 框架代码

```cpp
class LearnVKApp
{
public:
    void run(); // 开始运行

private:
    void initVK();  // 初始化Vulkan实例、包括检测扩展、校验层、初始化debug等 

    void initWindows();  // 初始化glfw的窗口

    void loop();    // 主循环

    void clear();   // 释放所有申请的资源

    GLFWwindow* m_window = nullptr;

    VkInstance m_vkInstance;

    const int WINDOW_WIDTH = 800;
    const int WINDOW_HEIGHT = 600;
};
```

### 初始化Vulkan实例

```cpp
void LearnVKApp::createVKInstance() {
    // 检查校验层
    if (enableValidationLayers && !checkValidationLayersProperties()) {
        throw std::runtime_error("failed to use validation layers!");
    }
    // 填写应用创建信息
    VkApplicationInfo appInfo = {};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "LearnVKApp";
    appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.pEngineName = "No Engine";
    appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.apiVersion = VK_API_VERSION_1_0;
    // 填写Vulkan实例创建信息
    VkInstanceCreateInfo createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;
    // 填写glfw窗口系统扩展和debug messager扩展的信息
    auto extentionsName = getRequiredExtentions();
    createInfo.enabledExtensionCount =  static_cast<uint32_t>(extentionsName.size());
    createInfo.ppEnabledExtensionNames = extentionsName.data();
    // 填写校验层信息
    if (enableValidationLayers) {
        createInfo.enabledLayerCount = static_cast<uint32_t>(m_validationLayers.size());
        createInfo.ppEnabledLayerNames = m_validationLayers.data();
    } else {
        createInfo.enabledLayerCount = 0;
        createInfo.ppEnabledLayerNames = nullptr;
    }
    // 在实例创建之前检查实例支持的扩展列表
    if (!checkExtentionsProperties(extentionsName)) {
        throw std::runtime_error("failed to support instance extention");
    }
    VkResult res = vkCreateInstance(&createInfo, nullptr, &m_vkInstance);
    if (res != VK_SUCCESS) {
        throw std::runtime_error("failed to create vk instance");
    }
}
```

其中包括填写应用引擎信息、检验扩展信息等。

### 校验层设置

Vulkan的校验层主要负责检查参数是否合法、追踪内存、log之类的工作。一般是对真正进行工作的API进行封装。

### 使用LunarG提供的校验层

我们使用LunarG校验层只需要在`VkIntanceCreateInfo`中填入`ppEnabledLayesName`和`enabledExtensionCount`即可。不过在创建真正的实例之前，和检查扩展一样，LunarG校验层也需要使用`vkEnumerateInstanceLayerProperties`来查找我们需求的Layer名字是否可用。如果可用的话就可以创建实例。

```cpp
bool LearnVKApp::checkValidationLayersProperties() {
    uint32_t validationLayersCount = 0;
    vkEnumerateInstanceLayerProperties(&validationLayersCount, nullptr);
    std::vector<VkLayerProperties> availableLayers(validationLayersCount);
    vkEnumerateInstanceLayerProperties(&validationLayersCount, availableLayers.data());
    // 检查所有m_validationLayers中要求的layer层是否都支持
    for (auto& layer : m_validationLayers) {
        if (std::find_if(availableLayers.begin(), availableLayers.end(), [layer](VkLayerProperties properties) {
            return std::strcmp(properties.layerName, layer) == 0;
            }) == availableLayers.end()) {
            return false;
        }
    }
    return true;
}
```

当然有了校验层之后如果没有添加任何信息的[回调函数](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=回调函数&zhida_source=entity)的话也不会知道校验层发出的信息，因此需要用`vkCreateDebugUtilsMessengerEXT`设置debugCallback才能实现接受Vulkan校验层发出的错误。这个函数需要我们启用扩展`VK_EXT_debug_utils`。因此需要在之前创建实例时通过在`extentsName`添加`VK_EXT_DEBUG_UTILS_EXTENSION_NAME`加入这一扩展。

因为是扩展函数不能被Vulkan直接调用，因此我们需要设置其[代理函数](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=代理函数&zhida_source=entity)：通过`vkGetInstantceProcAddr`获取扩展的函数地址来调用这个函数。具体的代理函数使用方式如下：

```cpp
VkResult createDebugUtilsMessengerEXT(VkInstance instance,
    const VkDebugUtilsMessengerCreateInfoEXT* pCreateInfo,
    const VkAllocationCallbacks* pAllocator,
    VkDebugUtilsMessengerEXT* pCallback) {
    auto func = (PFN_vkCreateDebugUtilsMessengerEXT)
        // 根据函数名获取函数地址，使用这个api必须在实例中加入VK_EXT_debug_utils扩展
        vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT");
    if (func != nullptr) {
        return func(instance, pCreateInfo, pAllocator, pCallback);
    } else {
        return VK_ERROR_EXTENSION_NOT_PRESENT;
    }
}
```

最后因为我们通过代理创建了一个`VkDebugUtilsMessengerEXT`的实例，因此也需要显示地使用另一个销毁它的代理函数释放它的内存，否则就会发生[内存泄露](https://zhida.zhihu.com/search?content_id=207755695&content_type=Article&match_order=1&q=内存泄露&zhida_source=entity)在关闭页面时产生报错。

在设置完毕所有校验层和debugUtilsMessenger并且正确完成对扩展和校验层的建言，之后如果能够正常打开窗口，则说明所有的设置都是正确的。

下文：

[栗野：Vulkan学习笔记(二）创建交换链之前的设备检测0 赞同 · 0 评论文章![img](./assets/v2-fef9860b499784cd7df9c5055ca9fdac_180x120.jpg)](https://zhuanlan.zhihu.com/p/538979880)



