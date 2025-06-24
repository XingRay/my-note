# Vulkan开发学习记录 16 - 顶点输入描述

## 简述

在接下来的章节，我们会将之前在顶点着色器中直接硬编码的顶点数据替换为[顶点缓冲](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=顶点缓冲&zhida_source=entity)来定义。我们首先创建一个CPU 的缓冲，然后将我们要使用的顶点数据先复制到这个CPU 缓冲中，最后，我们复制CPU 缓冲中的数据到阶段缓冲。

## [顶点着色器](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=2&q=顶点着色器&zhida_source=entity)

修改顶点着色器，去掉代码中包含的硬编码顶点数据。使用in 关键字使用顶点缓冲中的顶点数据：

```glsl
#version 450

layout(location = 0) in vec2 inPosition;
layout(location = 1) in vec3 inColor;

layout(location = 0) out vec3 fragColor;

void main() {
    gl_Position = vec4(inPosition, 0.0, 1.0);
    fragColor = inColor;
}
```

上面代码中的inPosition 和inColor 变量是顶点属性。它们代表了顶点缓冲中的每个顶点数据，就跟我们使用数组定义的顶点数据是一样的。重新编译顶点着色器，保证没有出现问题。

layout(location = x) 用于[指定变量](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=指定变量&zhida_source=entity)在顶点数据中的索引。特别需要注意，对于64 位变量，比如dvec3 类型的变量，它占用了不止一个索引位置，我们在定义这种类型的顶点属性变量之后的顶点变量，要注意索引号的增加并不是1：

```glsl
layout(location = 0) in dvec3 inPosition;
layout(location = 2) in vec3 inColor;
```

## [顶点数据](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=8&q=顶点数据&zhida_source=entity)

我们将顶点数据从顶点着色器代码移动到我们的应用程序的代码中。 我们首先包含GLM 库的[头文件](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=头文件&zhida_source=entity)，它提供了我们需要使用的[线性代数](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=线性代数&zhida_source=entity)库：

```cpp
#include <glm/glm.hpp>
```

创建一个叫做Vertex 的新的[结构体类型](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=结构体类型&zhida_source=entity)，我们使用它来定义单个顶点的数据：

```cpp
struct Vertex {
    glm::vec2 pos;
    glm::vec3 color;
};
```

GLM 库提供了能够完全兼容GLSL 的C++ [向量](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=向量&zhida_source=entity)类型：

```cpp
const std::vector<Vertex> vertices = {
    {{0.0f, -0.5f}, {1.0f, 0.0f, 0.0f}},
    {{0.5f, 0.5f}, {0.0f, 1.0f, 0.0f}},
    {{-0.5f, 0.5f}, {0.0f, 0.0f, 1.0f}}
};
```

现在，我们可以使用Vertex [结构体数组](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=结构体数组&zhida_source=entity)来定义我们的顶点数据。这次定义和之前在顶点着色器中进行的略有不同，之前在顶点着色器中，我们将顶点位置和顶点颜色数据定义在了不同的数组中，这次，我们将它们定义在了同一个结构体数组中。这种顶点属性定义方式定义的顶点数据也被叫做交叉顶点属性。

## 绑定描述

定义好顶点数据，我们需要将CPU 缓冲(也就是我们定义的Vertex 结构体数组)中顶点数据的存放方式传递给GPU ，以便GPU 可以正确地加载这些数据到[显存](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=显存&zhida_source=entity)中。我们给Vertex 结构体添加一个叫做getBindingDescription的[静态成员函数](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=静态成员函数&zhida_source=entity)，在里面编写代码返回Vertex 结构体的顶点数据存放方式：

```cpp
struct Vertex {
    glm::vec2 pos;
    glm::vec3 color;

    static VkVertexInputBindingDescription getBindingDescription() {
        VkVertexInputBindingDescription bindingDescription{};

        return bindingDescription;
    }
};
```

顶点绑定描述了在整个顶点中从内存加载数据的速率。它指定数据条目之间的字节数，以及是在每个顶点之后还是在每个实例之后移动到下一个数据条目。

```cpp
VkVertexInputBindingDescription bindingDescription{};
bindingDescription.binding = 0;
bindingDescription.stride = sizeof(Vertex);
bindingDescription.inputRate = VK_VERTEX_INPUT_RATE_VERTEX;
```

我们所有的[逐顶点](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=逐顶点&zhida_source=entity)数据都打包在一个数组中，因此我们只需要一个绑定。该`binding`参数指定绑定数组中绑定的索引。该`stride`参数指定从一个条目到下一个条目的字节数，该`inputRate`参数可以具有以下值之一：

- `VK_VERTEX_INPUT_RATE_VERTEX`：移动到每个顶点后的下一个数据条目
- `VK_VERTEX_INPUT_RATE_INSTANCE`：移动到每个实例后的下一个数据条目

我们不打算使用实例化渲染，所以我们将继续使用逐顶点数据。

## 属性说明

处理顶点输入的第二个结构是`VkVertexInputAttributeDescription`。我们将添加另一个辅助函数`Vertex`来填充这些结构。

```cpp
#include <array>

...

static std::array<VkVertexInputAttributeDescription, 2> getAttributeDescriptions() {
    std::array<VkVertexInputAttributeDescription, 2> attributeDescriptions{};

    return attributeDescriptions;
}
```

如[函数原型](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=函数原型&zhida_source=entity)所示，将有两个这样的结构。属性描述结构描述了如何从源自绑定描述的顶点数据块中提取顶点属性。我们有两个属性，位置和颜色，所以我们需要两个属性描述结构。

```cpp
attributeDescriptions[0].binding = 0;
attributeDescriptions[0].location = 0;
attributeDescriptions[0].format = VK_FORMAT_R32G32_SFLOAT;
attributeDescriptions[0].offset = offsetof(Vertex, pos);
```

该`binding`参数告诉 Vulkan 每个顶点数据来自哪个绑定。该`location`参数引用`location`顶点着色器中输入的指令。带有位置的顶点着色器中的输入`0`是位置，它有两个 32 位浮点分量。

该`format`参数描述属性的数据类型。有点令人困惑的是，格式是使用与颜色格式相同的枚举来指定的。以下着色器类型和格式通常一起使用：

- `float`:`VK_FORMAT_R32_SFLOAT`
- `vec2`:`VK_FORMAT_R32G32_SFLOAT`
- `vec3`:`VK_FORMAT_R32G32B32_SFLOAT`
- `vec4`:`VK_FORMAT_R32G32B32A32_SFLOAT`

如您所见，您应该使用[颜色通道](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=颜色通道&zhida_source=entity)数量与着色器数据类型中的组件数量相匹配的格式。允许使用比着色器中组件数量更多的通道，但它们将被静默丢弃。如果通道数小于元件数，则 BGA 元件将使用默认值`(0, 0, 1)`. 颜色类型 ( `SFLOAT`, `UINT`, `SINT`) 和位宽也应与着色器输入的类型相匹配。请参阅以下示例：

- `ivec2`: `VK_FORMAT_R32G32_SINT`，一个由 32 位有符号整数组成的 2 分量向量
- `uvec4`: `VK_FORMAT_R32G32B32A32_UINT`，一个由 32 位无符号整数组成的 4 分量向量
- `double`: `VK_FORMAT_R64_SFLOAT`，双精度（64 位）浮点数

该`format`参数隐式定义了属性数据的字节大小，并且该`offset`参数指定了自要读取的每顶点数据开始以来的字节数。

```cpp
attributeDescriptions[1].binding = 0;
attributeDescriptions[1].location = 1;
attributeDescriptions[1].format = VK_FORMAT_R32G32B32_SFLOAT;
attributeDescriptions[1].offset = offsetof(Vertex, color);
```

## 管道顶点输入

我们现在需要设置图形管道以通过引用`createGraphicsPipeline`找到`vertexInputInfo`结构并修改它以引用两个描述：

```cpp
auto bindingDescription = Vertex::getBindingDescription();
auto attributeDescriptions = Vertex::getAttributeDescriptions();

vertexInputInfo.vertexBindingDescriptionCount = 1;
vertexInputInfo.vertexAttributeDescriptionCount = static_cast<uint32_t>(attributeDescriptions.size());
vertexInputInfo.pVertexBindingDescriptions = &bindingDescription;
vertexInputInfo.pVertexAttributeDescriptions = attributeDescriptions.data();
```

管道现在已准备好接受`vertices`容器格式的顶点数据并将其传递给我们的顶点着色器。如果您现在在启用验证层的情况下运行该程序，您会看到它抱怨没有绑定到绑定的[顶点缓冲区](https://zhida.zhihu.com/search?content_id=218610584&content_type=Article&match_order=1&q=顶点缓冲区&zhida_source=entity)。下一步是创建一个顶点缓冲区并将顶点数据移动到其中，以便 GPU 能够访问它。

## 工程链接

[https://github.com/Kirkice/JourneyThroughVulkangithub.com/Kirkice/JourneyThroughVulkan](https://github.com/Kirkice/JourneyThroughVulkan)

