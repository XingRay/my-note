# Hello Vulkan Compute Shader: Specialization Constant

我们可能会这样写一个[compute shader](https://zhida.zhihu.com/search?content_id=192220955&content_type=Article&match_order=1&q=compute+shader&zhida_source=entity)，用来将buffer中的float全部乘以2。

```glsl
#version 450

layout (local_size_x = 256) in;
layout(set = 0, binding = 0, std430) buffer StorageBuffer
{
   float data[];
} block;


void main()
{
    //grab global ID
	uint gID = gl_GlobalInvocationID.x;
    block.data[gID] *= 2.0f; 
}
```

上述compute shader来自：[MuGdxy暮狗dan：Hello Vulkan Compute Shader](https://zhuanlan.zhihu.com/p/460093914)

我们现在特别来关心这样一句代码：

```glsl
layout (local_size_x = 256) in;//workgroupSize { x = 256, y = 1, z = 1 }
```

我们指定了工作组大小为256。

这里其实有一些问题：

- 并不是所有的gpu都支持大小为256的工作组。（对应`vkPhysicalDeviceProperties.limits.maxComputeWorkGroupInvocations`）
- 不同gpu上可能有不同的最优工作组划分策略。
- 我们希望一个shader可以应对所有的工作组划分方式，而不是依靠编译多个shader来实现。

非常幸运，Specialization Constant 允许我们在`PipelineShaderStageCreate` 阶段指定某些constant值，对于一般的constant，我们可以这样来处理。

```glsl
layout(constant_id = 0) const int lightCount = 6;
```

`const int lightCount`有默认值初始值6（默认值是必须的，用于不指定常量的时候Roll Back）。

constant_id用于`PipelineShaderStageCreate`时候进行索引。

那么你可能会想到用下面这种方式来解决动态指定工作组大小的问题。

```glsl
layout(constant_id = 0) const int size_x = 256;
layout (local_size_x = size_x) in;
```

很遗憾，这个方式是无法通过编译的。想要对`local_size_x/y/z`进行指定需要这样做：

```glsl
layout (local_size_x = 256) in;
layout (local_size_x_id = 0) in;
```

首先为`local_size_x`指定默认初始值，然后指明`local_size_x_id`，之后就可以在 `PipelineShaderStageCreate`时对`local_size_x`进行指定了。（local_size_y/z的指定也是同理）

在CPU一侧：

```cpp
VkPipelineShaderStageCreateInfo shaderStageCreateInfo = {};
shaderStageCreateInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
shaderStageCreateInfo.module = computeShaderModule;
shaderStageCreateInfo.stage = VK_SHADER_STAGE_COMPUTE_BIT;
shaderStageCreateInfo.pName = "main";

VkSpecializationMapEntry entry{};
entry.constantID = 0;
entry.offset = 0;
entry.size = sizeof(uint32_t);

VkSpecializationInfo specInfo{};
specInfo.mapEntryCount = 1;
specInfo.pMapEntries = &entry;
specInfo.pData = &computeShaderProcessUnit;
specInfo.dataSize = sizeof(computeShaderProcessUnit);

shaderStageCreateInfo.pSpecializationInfo = &specInfo;
```

现对部分参数做一下解释：

- `VkSpecializationMapEntry.offset`指的是，此`entry`（本例中为`local_size_x`） 对应的数据，在数据块`VkSpecializationInfo.pData`中的偏移量
- `VkSpecializationMapEntry.size`指的是，此`entry`对应的数据的大小（这里为一个`uint32_t`的大小）
- `VkSpecializationInfo` 则负责描述一共有多少个entry，并且引用一个数据块，这个数据块包含了所有的entry所需的数据。

## Cite

[GLSLangSpec.4.60 4.11. Specialization-Constant Qualifierwww.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.60.pdf](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/OpenGL/specs/gl/GLSLangSpec.4.60.pdf)

[https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#pipelines-specialization-constantswww.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#pipelines-specialization-constants](https://link.zhihu.com/?target=https%3A//www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html%23pipelines-specialization-constants)

