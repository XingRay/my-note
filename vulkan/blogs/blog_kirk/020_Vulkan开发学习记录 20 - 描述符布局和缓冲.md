# Vulkan开发学习记录 20 - 描述符布局和缓冲

## 介绍

现在，我们已经可以传递顶点属性给[顶点着色器](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=顶点着色器&zhida_source=entity)，但对于一些所有顶点都共享的属性，比如顶点的[变换矩阵](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=变换矩阵&zhida_source=entity)，将它们作为顶点属性为每个顶点都传递一份显然是非常浪费的。

Vulkan提供了资源描述符来解决这一问题。[描述符](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=2&q=描述符&zhida_source=entity)是用来在着色器中访问缓冲和图像数据的一种方式。我们可以将变换矩阵存储在一个缓冲中， 然后通过描述符在着色器中访问它。使用描述符需要进行下面三部分的设置：

• 在管线创建时指定描述符布局

• 从描述符池分配[描述符集](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=描述符集&zhida_source=entity)

• 在渲染时绑定描述符集

描述符布局用于指定可以被管线访问的资源类型，类似渲染流程指定可以被访问的附着类型。描述符集指定了要绑定到描述符上的缓冲和图像资源，类似[帧缓冲](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=帧缓冲&zhida_source=entity)指定绑定到渲染流程附着上的图像视图。最后，将描述符集绑定到绘制指令上，类似绑定[顶点缓冲](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=顶点缓冲&zhida_source=entity)和帧缓冲到绘制指令上。

有多种类型的描述符，但在本章节，我们只使用uniform缓冲对象(UBO)。 在之后的章节，我们会看到其它类型的描述符，它们的使用方式和uniform缓冲对象类似。现在，让我们先用结构体定义我们要在着色器中使用的uniform数据：

```cpp
struct UniformBufferObject {
    glm::mat4 model;
    glm::mat4 view;
    glm::mat4 proj;
};
```

我们将要使用的uniform数据复制到一个VkBuffer中，然后通过一个unfirm缓冲对象描述符在顶点着色器中访问它：

```cpp
layout(binding = 0) uniform UniformBufferObject {
    mat4 model;
    mat4 view;
    mat4 proj;
} ubo;

void main() {
    gl_Position = ubo.proj * ubo.view * ubo.model * vec4(inPosition, 0.0, 1.0);
    fragColor = inColor;
}
```

接下来，我们在每一帧更新模型，视图，投影矩阵，来让上一章节渲染的矩形在[三维空间](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=三维空间&zhida_source=entity)中旋转。

### 顶点着色器

修改顶点着色器包含上面的uniform缓冲对象。这里，我们假定读者对MVP变换矩阵有一定了解。如果没有，读者可以查找资源学习一下。

```cpp
#version 450

layout(binding = 0) uniform UniformBufferObject {
    mat4 model;
    mat4 view;
    mat4 proj;
} ubo;

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec3 inColor;

layout(location = 0) out vec3 fragColor;

void main() {
    gl_Position = ubo.proj * ubo.view * ubo.model * vec4(inPosition, 0.0, 1.0);
    fragColor = inColor;
}
```

unifrom，in和out定义在着色器中出现的顺序是可以任意的。上面代码中的binding修饰符类似于我们对顶点属性使用的location修饰符。我们会在描述符布局引用这个binding值。包含gl_Position的那行现在使用变换矩阵计算顶点最终的[裁剪坐标](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=裁剪坐标&zhida_source=entity)。

### 描述符集布局

下一步，我们在应用程序的代码中定义UBO：

```cpp
struct UniformBufferObject {
    glm::mat4 model;
    glm::mat4 view;
    glm::mat4 proj;
};
```

通过GLM库我们可以准确地匹配我们在着色器中使用变量类型，可以放心地直接使用memcpy函数复制`UniformBufferObject`结构体的数据VkBuffer中。

我们需要在管线创建时提供着色器使用的每一个描述符绑定信息。我们添加了一个叫做createDescriptorSetLayout的函数，来完成这项工作。并在管线创建前调用它：

```cpp
void initVulkan() {
    ...
    createDescriptorSetLayout();
    createGraphicsPipeline();
    ...
}

...

void createDescriptorSetLayout() {

}
```

我们需要使用VkDescriptorSetLayoutBinding结构体来描述每一个绑定：

```cpp
void createDescriptorSetLayout() {
    VkDescriptorSetLayoutBinding uboLayoutBinding{};
    uboLayoutBinding.binding = 0;
    uboLayoutBinding.descriptorType = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
    uboLayoutBinding.descriptorCount = 1;
}
```

`binding`和descriptorType [成员变量](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=成员变量&zhida_source=entity)用于指定着色器使用的描述符绑定和描述符类型。这里我们指定的是一个unifrom缓冲对象。[着色器变量](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=着色器变量&zhida_source=entity)可以用来表示unifrom缓冲对象数组，descriptorCount 成员变量用来指定数组中 元素的个数。我们可以使用数组来指定[骨骼动画](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=骨骼动画&zhida_source=entity)使用的所有变换矩阵。在这里，我们的MVP矩阵只需要一个unifrom缓冲对象，所以我们将descriptorCount 的值设置为1。

```cpp
uboLayoutBinding.stageFlags = VK_SHADER_STAGE_VERTEX_BIT;
```

我们还需要指定描述符在哪一个着色器阶段被使用。stageFlags 成员变量可以指定通过`VkShaderStageFlagBits`组合或VK_SHADER_STAGE_ALL_GRAPHICS指定描述符被使用的着色器阶段。在这里，我们只在顶点着色器使用描述符。

```cpp
uboLayoutBinding.pImmutableSamplers = nullptr; // Optional
```

`pImmutableSamplers`成员变量仅用于和图像采样相关的描述符。这里我们先将其设置为默认值，之后的章节会对它进行介绍。 所有的描述符绑定被组合进一个`VkDescriptorSetLayout`对象。我们在pipelineLayout成员变量的定义上面定义descriptorSetLayout成员变量：

```cpp
VkDescriptorSetLayout descriptorSetLayout;
VkPipelineLayout pipelineLayout;
```

调用`vkCreateDescriptorSetLayout`函数创建VkDescriptorSetLayout 对象。 [vkCreateDescriptorSetLayout](https://www.khronos.org/registry/vulkan/specs/1.0/man/html/vkCreateDescriptorSetLayout.html)函数以`VkDescriptorSetLayoutCreateInfo`结构体作为参数：

```cpp
VkDescriptorSetLayoutCreateInfo layoutInfo{};
layoutInfo.sType = VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
layoutInfo.bindingCount = 1;
layoutInfo.pBindings = &uboLayoutBinding;

if (vkCreateDescriptorSetLayout(device, &layoutInfo, nullptr, &descriptorSetLayout) != VK_SUCCESS) {
    throw std::runtime_error("failed to create descriptor set layout!");
}
```

我们需要在管线创建时指定着色器需要使用的描述符集布局。管线布局对象指定了管线使用的描述符集布局。修改`VkPipelineLayoutCreateInfo`结构体信息引用布局对象：

```cpp
VkPipelineLayoutCreateInfo pipelineLayoutInfo{};
pipelineLayoutInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
pipelineLayoutInfo.setLayoutCount = 1;
pipelineLayoutInfo.pSetLayouts = &descriptorSetLayout;
```

读者可能会有疑问，明明一个VkDescriptorSetLayout 对象就包含了所 有要使用的描述符绑定，为什么这里还可以指定多个VkDescriptorSetLayout 对象，我们会在下一章节作出解释。

描述符布局对象可以在应用程序的整个[生命周期](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=生命周期&zhida_source=entity)使用，即使使用了新的管线对象。通常我们在应用程序退出前才清除它：

```cpp
void cleanup() {
    cleanupSwapChain();

    vkDestroyDescriptorSetLayout(device, descriptorSetLayout, nullptr);

    ...
}
```

### uniform缓冲

在下一章节，我们将会为着色器指定包含UBO数据的缓冲对象。我们首先需要创建用于包含数据的缓冲对象，然后在每一帧将新的UBO数据复制到uniform缓冲。由于需要频繁的数据更新，在这里使用暂存缓冲并不会带来性能提升。

由于我们同时并行渲染多帧的缘故，我们需要多个uniform缓冲，来满足多帧并行渲染的需要。我们可以对并行渲染的每一帧或每一个交换链图像使用独立的uniform缓冲对象。由于我需要在[指令缓冲](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=指令缓冲&zhida_source=entity)中引用uniform缓冲，对于每个[交换链](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=2&q=交换链&zhida_source=entity)图像使用独立的uniform缓冲对象相对来说更加方便。

添加两个新的类成员变量uniformBuffers和uniformBuffersMemory：

```cpp
VkBuffer indexBuffer;
VkDeviceMemory indexBufferMemory;

std::vector<VkBuffer> uniformBuffers;
std::vector<VkDeviceMemory> uniformBuffersMemory;
std::vector<void*> uniformBuffersMapped;
```

添加一个叫做createUniformBuffers的函数，在createIndexBuffer[函数调用](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=函数调用&zhida_source=entity)后调用它分配uniform缓冲对象：

```cpp
void initVulkan() {
    ...
    createVertexBuffer();
    createIndexBuffer();
    createUniformBuffers();
    ...
}

...

void createUniformBuffers() {
    VkDeviceSize bufferSize = sizeof(UniformBufferObject);

    uniformBuffers.resize(MAX_FRAMES_IN_FLIGHT);
    uniformBuffersMemory.resize(MAX_FRAMES_IN_FLIGHT);
    uniformBuffersMapped.resize(MAX_FRAMES_IN_FLIGHT);

    for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        createBuffer(bufferSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, uniformBuffers[i], uniformBuffersMemory[i]);

        vkMapMemory(device, uniformBuffersMemory[i], 0, bufferSize, 0, &uniformBuffersMapped[i]);
    }
}
```

我们会在另外的函数中使用新的变换矩阵更新uniform缓冲，所以在这里没有出现`vkMapMemory`函数调用。应用程序退出前不要忘记清除申请的uniform缓冲对象：

```cpp
void cleanup() {
    ...

    for (size_t i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
        vkDestroyBuffer(device, uniformBuffers[i], nullptr);
        vkFreeMemory(device, uniformBuffersMemory[i], nullptr);
    }

    vkDestroyDescriptorSetLayout(device, descriptorSetLayout, nullptr);

    ...

}
```

### 更新uniform数据

添加一个叫做updateUniformBuffer的函数，然后在drawFrame函数中我们已经可以确定获取交换链图像是哪一个后调用它：

```cpp
void drawFrame() {
    ...

    updateUniformBuffer(currentFrame);

    ...

    VkSubmitInfo submitInfo{};
    submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;

    ...
}

...

void updateUniformBuffer(uint32_t currentImage) {

}
```

调用updateUniformBuffer函数可以在每一帧产生一个新的变换矩阵。 updateUniformBuffer函数的实现需要使用了下面这些[头文件](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=头文件&zhida_source=entity)：

```cpp
#define GLM_FORCE_RADIANS
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>

#include <chrono>
```

包含glm/gtc/matrix_transform.hpp 头文件是为了使用glm::rotate之类的[矩阵变换](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=矩阵变换&zhida_source=entity)函数。`GLM_FORCE_RADIANS`[宏定义](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=宏定义&zhida_source=entity)用来使glm::rotate这些函数使用弧度作为参数的单位。

包含`chrono`头文件是为了使用[计时函数](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=计时函数&zhida_source=entity)。我们将通过计时函数实现每秒旋转90度的效果。

```cpp
void updateUniformBuffer(uint32_t currentImage) {
    static auto startTime = std::chrono::high_resolution_clock::now();

    auto currentTime = std::chrono::high_resolution_clock::now();
    float time = std::chrono::duration<float, std::chrono::seconds::period>(currentTime - startTime).count();
}
```

我们在unifron缓冲对象中定义我们的MVP变换矩阵。模型的渲染被我们设计成绕Z轴渲染time弧度。

```cpp
UniformBufferObject ubo{};
ubo.model = glm::rotate(glm::mat4(1.0f), time * glm::radians(90.0f), glm::vec3(0.0f, 0.0f, 1.0f));
```

glm::rotate函数以矩阵，旋转角度和旋转轴作为参数。glm::mat4(1.0f)用于构造[单位矩阵](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=单位矩阵&zhida_source=entity)。这里，我们通过time * glm::radians(90.0f)完成每秒旋转90度的操作。

```cpp
ubo.view = glm::lookAt(glm::vec3(2.0f, 2.0f, 2.0f), glm::vec3(0.0f, 0.0f, 0.0f), glm::vec3(0.0f, 0.0f, 1.0f));
```

对于视图变换矩阵，我们使用上面代码中的定义。glm::lookAt函数以观察者位置，视点坐标和向上向量为参数生成视图变换矩阵。

```cpp
ubo.proj = glm::perspective(glm::radians(45.0f), swapChainExtent.width / (float) swapChainExtent.height, 0.1f, 10.0f);
```

对于[透视变换](https://zhida.zhihu.com/search?content_id=220909682&content_type=Article&match_order=1&q=透视变换&zhida_source=entity)矩阵，我们使用上面代码中的定义。glm::perspective函数以视域的垂直角度，视域的宽高比以及近平面和远平面距离为参数生成透视变换矩阵。特别需要注意在窗口大小改变后应该使用当前交换链范围来重新计算宽高比。

```cpp
ubo.proj[1][1] *= -1;
```

GLM库最初是为OpenGL设计的，它的裁剪坐标的Y轴和Vulkan是相反的。我们可以通过将投影矩阵的Y轴缩放系数符号取反来使投影矩阵和Vulkan的要求一致。如果不这样做，渲染出来的图像会被倒置。

定义完所有的变换矩阵，我们就可以将最后的变换矩阵数据复制到当前帧对应的unifron缓冲中。复制数据的方法和复制顶点数据到顶点缓冲一 样，除了没有使用暂存缓冲：

```cpp
memcpy(uniformBuffersMapped[currentImage], &ubo, sizeof(ubo));
```

对于在着色器中使用的需要频繁修改的数据，这样使用UBO并非最佳方式。还有一种更加高效的传递少量数据到着色器的方法，我们会在之后的章节介绍它。

### 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

