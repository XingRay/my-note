# 从零开始的Vulkan（五）：描述符与渲染通道

## 描述符池与[描述符集](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=描述符集&zhida_source=entity)

### 创建描述符池

在上一节我们创建管线的时候，指定了管线布局需要应用的描述符集，但若要在绘制时真正使用管线布局中的描述符，还需要将描述符集实际创建出来再进行绑定。创建描述符的过程和创建[命令缓冲区](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=命令缓冲区&zhida_source=entity)类似，需要先创建描述符池，再从描述符池中分配出描述符集。

创建描述符池的方法如下所示：

```cpp
auto descriptorPoolInfo = vk::DescriptorPoolCreateInfo()
		.setMaxSets(maxSetsCount)
		.setPoolSizeCount(typeCount.size())
		.setPPoolSizes(typeCount.data());
vkInfo->device.createDescriptorPool(&descriptorPoolInfo, 0, &vkInfo->descPool);
```

从一个描述符池中可以分配出多个描述符集，而maxSets就指定了可以分配出描述符集的最大数量。poolSizeCount和pPoolSizes指定了描述符池中允许的不同描述符类型的数量。

> 在Vulkan1.3中，可以给pNext指定VkDescriptorPoolInlineUniformBlockCreateInfo结构体并通过maxInlineUniformBlockBindings来指定要分配的内联UniformBlock数量。
> 可以给flags指定VK_DESCRIPTOR_POOL_CREATE_FREE_DESCRIPTOR_SET_BIT允许描述符池中分配的描述符集进行自由释放，即允许vkFreeDescriptorSets操作，若不指定该flag，则只有vkAllocateDescriptorSets和vkResetDescriptorPool操作是被允许的。
> 在Vulkan1.2后，可以给flags指定VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT允许为分配的描述符集指定VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT旗标。

以下是一个指定poolSizes的例子：

```cpp
std::vector<vk::DescriptorPoolSize> typeCount(5);
typeCount[0].setType(vk::DescriptorType::eUniformBuffer);
typeCount[0].setDescriptorCount(5);
typeCount[1].setType(vk::DescriptorType::eSampledImage);
typeCount[1].setDescriptorCount(1);
typeCount[2].setType(vk::DescriptorType::eSampler);
typeCount[2].setDescriptorCount(1);
typeCount[3].setType(vk::DescriptorType::eCombinedImageSampler);
typeCount[3].setDescriptorCount(2);
typeCount[4].setType(vk::DescriptorType::eInputAttachment);
typeCount[4].setDescriptorCount(6);
```

在这个例子中，用VkDescriptorPoolSize结构体指定了5个UniformBuffer描述符，1个用于采样的贴图描述符，一个采样器描述符，2个采样器贴图结合描述符，6个输入附件描述符。poolSizes可以依据个人的用途进行具体的指定。

在创建完描述符池以后，我们就可以从描述符池中分配出要用的描述符集。

### 分配描述符集

通过填写一个VkDescriptorSetAllocateInfo结构体就可以实现描述符集的分配，具体实现方式如下：

```cpp
auto descSetAllocInfo = vk::DescriptorSetAllocateInfo()
			.setDescriptorPool(vkInfo->descPool)
			.setDescriptorSetCount(1)
			.setPSetLayouts(&descSetLayout);
vkInfo->device.allocateDescriptorSets(&descSetAllocInfo, &descSet);
```

通过descriptorSetCount和pSetLayouts就可以根据descSetLayout分配出所需的描述符集，descSetLayout的创建方法在上一节已经详细讲过。分配的描述符集句柄会被存储在descSet用于之后更新和绑定。

> 在Vulkan1.2后，可以给pNext指定VkDescriptorSetVariableDescriptorCountAllocateInfo结构体并用pDescriptorCounts[i]指定可变大小描述符绑定中的描述符数量；如果相应描述符集布局中的可变大小描述符绑定的描述符类型为VK_DESCRIPTOR_TYPE_INLINE_UNIFORM_BLOCK，则pDescriptorCounts[i]指定绑定的容量以字节为单位；如果VkDescriptorSetAllocateInfo::pSetLayouts[i]不包含可变大小的描述符绑定，则 pDescriptorCounts[i]被忽略。

### 更新描述符集

Vulkan提供了两种更新描述符集的方式，分别是直接写入和复制写入。首先介绍直接写入的方式：

```cpp
vk::WriteDescriptorSet descSetWrites[1];
descSetWrites[0].setDescriptorCount(1);
descSetWrites[0].setDescriptorType(vk::DescriptorType::eUniformBuffer);
descSetWrites[0].setDstArrayElement(0);
descSetWrites[0].setDstBinding(0);
descSetWrites[0].setDstSet(descSet);
descSetWrites[0].setPBufferInfo(&bufferInfo);
descSetWrites[0].setPImageInfo(&imageInfo);
vkInfo->device.updateDescriptorSets(1, descSetWrites, 0, 0);
```

- setDescriptorCount指定了需要更新的描述符数量。
- setDescriptorType指定了需要更新的描述符类型。
- 如果描述符引用的是缓存资源，则需要填写bufferInfo结构体；如果描述符引用的是图像或采样器资源，则需要填写imageInfo结构体。bufferInfo或imageInfo的数量需要与descriptorCount保持一致。
- setDstArrayElement指定了需要更新的描述符在数组中的索引。
- setDstBinding指定了需要更新的描述符在描述符集中的绑定序号。
- setDstSet指定了目标描述符集。

> 若dstSet和dstBinding是InlineUniformBuffer类型，则setDstArrayElement指定了更新的描述符在绑定时的字节偏移量；setDescriptorCount指定了更新的描述符所占的字节大小。
> 如果dstArrayElement+descriptorCount超过了该dstBinding所拥有的元素数量，则会自动跳转至下一个dstBinding的[0号元素](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=0号元素&zhida_source=entity)处进行更新。

VkDescriptorBufferInfo的定义如下所示：

```cpp
auto bufferInfo = vk::DescriptorBufferInfo()
		.setBuffer(dstBuffer->GetBuffer())
		.setOffset(0)
		.setRange(bufferSize);
```

- setBuffer需要指定一个可用的[buffer对象](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=buffer对象&zhida_source=entity)句柄。
- setOffset指定了缓冲的[字节偏移量](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=2&q=字节偏移量&zhida_source=entity)。
- setRange指定了需要使用的缓冲的字节大小（或直接填写VK_WHOLE_SIZE）。

> 对于UnformBufferDynamic或StorageBufferDynamic，setOffset指定的是动态偏移量的基准值。

VkDescriptorImageInfo的定义如下所示：

```cpp
auto imageInfo = vk::DescriptorImageInfo()
		.setImageLayout(vk::ImageLayout::eShaderReadOnlyOptimal)
		.setImageView(image.GetImageView(&vkInfo->device))
		.setSampler(sampler);
```

- setImageLayout指定了所用图像资源的布局。
- setImageView需要指定一个可用的imageView对象句柄。
- setSampler指定了使用的采样器对象。

当采样器和图像资源以Sampler和SampledImage之类的形式分开被着色器引用时，imageInfo中只需要填写imageLayout，imageView或sampler；但当采样器和图像资源以CombinedImageSampler的形式用一个描述符同时被着色器引用时，必须同时填写三个参数。

接下来再介绍复制写入的方式，具体实现方式如下：

```cpp
vk::CopyDescriptorSet descSetCopy[1];
descSetCopy[0].setDescriptorCount(1);
descSetCopy[0].setDstArrayElement(0);
descSetCopy[0].setSrcArrayElement(0);
descSetCopy[0].setDstBinding(0);
descSetCopy[0].setSrcBinding(0);
descSetCopy[0].setDstSet(dstSet);
descSetCopy[0].setSrcSet(srcSet);
vkInfo->device.updateDescriptorSets(0, 0, 1, descSetCopy);
```

VkCopyDescriptorSet的参数比较好理解，就是把对应element和对应binding的描述符从srcSet拷贝至dstSrc，这里不再赘述。

### 描述符模板化更新

对于一个庞大的描述符集而言，填写范式的Vulkan数据结构和重复的更新带来的开销是昂贵的，因此在Vulkan1.1版本后提供了VkDescriptorUpdateTemplate来优化更新描述带来的开销问题。

首先，我们需要使用一系列VkDescriptorUpdateTemplateEntry结构体来规定需要更新的描述符的入口点，如下所示：

```cpp
auto updateEntry = vk::DescriptorUpdateTemplateEntry()
			.setDescriptorCount(1)
			.setDescriptorType(vk::DescriptorType::eUniformBuffer)
			.setDstArrayElement(0)
			.setDstBinding(0)
			.setOffset(0)
			.setStride(sizeof(ObjectConstants));
```

与之前的更新方式不同的是，这边多出了一个stride成员变量，它指定了两个ArrayElement之间的字节偏移量。当描述符类型为InlineUniformBuffer时，这里stride将被忽视且被自动认定为1。

准备完所有VkDescriptorUpdateTemplateEntry之后，就可以填写VkDescriptorUpdateTemplateCreateInfo结构体了：

```cpp
auto updateTemplateInfo = vk::DescriptorUpdateTemplateCreateInfo()
			.setPipelineLayout(pipelineLayout)
			.setPipelineBindPoint(vk::PipelineBindPoint::eGraphics)
			.setDescriptorSetLayout(descSetLayout)
			.setDescriptorUpdateEntryCount(1)
			.setPDescriptorUpdateEntries(&updateEntry)
			.setTemplateType(vk::DescriptorUpdateTemplateType::eDescriptorSet)
			.setSet(0);
```

- setTemplateType可以选择DescriptorSet或PushDescriptorsKHR，需要注意使用PushDescriptorsKHR必须启用VK_KHR_push_descriptor扩展。
- setSet指定了pipeline layout中需要更新的描述符集的序号。

> 若templateType不指定PushDescriptorsKHR，则这里的setPipelineLayout和setSet都是无效的。

在填写完后就可以完成描述符更新模板的创建：

```cpp
vk::DescriptorUpdateTemplate updateTemp;
vkInfo->device.createDescriptorUpdateTemplate(&updateTemplateInfo, 0, &updateTemp);
```

一旦描述符更新模板被创建完成，我们就可以通过调用vkUpdateDescriptorSetWithTemplate来更新描述符集:

```cpp
vkInfo->device.updateDescriptorSetWithTemplate(descSet, updateTemp, pData);
```

pData是指向一个或多个用于更新描述符的VkDescriptorImageInfo，VkDescriptorBufferInfo或VkBufferView或VkAccelerationStructureKHR的指针，这些结构体的用法和普通的描述符更新类似。

## 采样器

采样器（Sampler）是着色器在访问图像资源时必不可少的，它详细规定了处理和访问图像资源的方法，依据需求我们可以自由定制要用的采样器，不同类型的采样器可以在不同的场合大显身手。

### 创建采样器对象

通过简单地填写一个VkSamplerCreateInfo结构体，我们就可以创建不同类型的采样器。创建一个最基础的采样器如下所示：

```cpp
auto samplerInfo = vk::SamplerCreateInfo()
			.setAnisotropyEnable(VK_FALSE)
                        .setMaxAnisotropy(0.0f)
			.setBorderColor(vk::BorderColor::eIntOpaqueBlack)
			.setCompareEnable(VK_FALSE)
			.setCompareOp(vk::CompareOp::eAlways)
			.setMagFilter(vk::Filter::eLinear)
			.setMinFilter(vk::Filter::eLinear)
			.setMaxLod(1.0f)
			.setMinLod(0.0f)
			.setMipLodBias(0.0f)
			.setMipmapMode(vk::SamplerMipmapMode::eLinear)
			.setUnnormalizedCoordinates(VK_FALSE);
                        .setAddressModeU(vk::SamplerAddressMode::eRepeat);
		        .setAddressModeV(vk::SamplerAddressMode::eRepeat);
		        .setAddressModeW(vk::SamplerAddressMode::eRepeat);
vkInfo->device.createSampler(&samplerInfo, 0, &repeatSampler);
```

- setMagFilter和setMinFilter分别指定了所采样的图像放大和缩小时所使用的过滤方法，有[线性过滤](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=线性过滤&zhida_source=entity)（Linear），最近过滤（Nearest），立方过滤（Cubic）三种。考虑到图像放大时的质量需求，一般选择线性过滤。
- setMipmapMode指定了拥有多级Mipmap的图像处于两个层级之中时的过滤方式，有线性过滤和最近过滤两种。
- setUnnormalizedCoordinates指定了是否使用非归一化[纹理坐标](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=纹理坐标&zhida_source=entity)，若不开启，则纹理坐标将被钳制在0-1之间；若开启，则纹理坐标将对应与图像的像素大小。
- setMaxLod和setMinLod用于指定细节层次（LOD）的钳制范围，将maxLod设置为VK_LOD_CLAMP_NONE可以避免对于LOD最大值的钳制。采样器的LOD设置将影响到其对于图像mipmap的使用。
- setMipLodBias指定了添加到mipmap LOD计算的偏移量。
- setAnisotropyEnable和setMaxAnisotropy指定了采样器的各项异性过滤，该过滤器有助于缓解当多边形法向量与摄像机观察向量之间夹角过大所导致的失真现象。这种过滤器的开销最大，但是其校正失真的效果确实对得起它所消耗的资源。
- setAddressModeUVW分别指定了在图像的UVW方向上采用的[寻址模式](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=寻址模式&zhida_source=entity)。重复寻址模式（Repeat）通过在坐标的每个整数点处重复绘制图像来拓充[纹理函数](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=纹理函数&zhida_source=entity)；边框颜色寻址模式（ClampToBorder）通过将范围外的坐标都映射为程序原指定的颜色来拓充纹理函数；钳位寻址模式（ClampToEdge）通过将范围外的坐标都映射为范围内最近的点来拓充纹理函数。重复寻址模式和钳位寻址模式都有其对应的镜像模式（Mirror）。
- setBorderColor指定了在边框颜色寻址模式下的边框颜色。

> 在Vulkan1.2后，可以给pNext指定VkSamplerReductionModeCreateInfo结构体并通过设置reductionMode来指定过滤器结合纹素的方式，默认为VK_SAMPLER_REDUCTION_MODE_WEIGHTED_AVERAGE。
> 在Vulkan1.1后，可以给pNext指定VkSamplerYcbcrConversionInfo结构体并通过设置conversion来指定采样时RGB和Y′CBCR颜色空间的转换。（需要开启samplerYcbcrConversion feature）

### 比较采样器

在VkSamplerCreateInfo中还有compareEnable和compareOp参数我尚未提及，它们是用于创建比较采样器的，它的常见用途就是PCF（百分比渐进过滤）阴影映射技术，大部分GPU硬件都提供了对于比较采样结果的内部支持，比如在HLSL里就可以通过以下代码来执行9核PCF：

```cpp
const float2 offsets[9] = {
	float2(-dx, -dx), float2(0.0f, -dx), float2(dx, -dx),
	float2(-dx, 0.0f), float2(0.0f, 0.0f), float2(dx, 0.0f),
	float2(-dx, dx), float2(0, dx), float2(dx, dx)
};

for (int i = 0; i < 9; i++)
	percentLit += shadowMap.SampleCmpLevelZero(compareSampler, shadowPos.xy + offsets[i], depth).r;
```

这其中的SampleCmpLevelZero函数就需要一个比较采样器的支持，通过将compareOp设为LessOrEqual就可以得到一个比较方程为小于等于的比较采样器，之后就可以将其用于PCF阴影的实现，在这里我们不对这种技术进行过多探讨，只需知道比较采样器的用途即可。

## 渲染通道

为了将帧缓冲和渲染过程结合在一起，Vulkan独创了RenderPass（渲染通道）概念，但因为RenderPass的过于臃肿以及缺乏实用性在早期Vulkan版本被广为诟病。虽然Vulkan1.3提供了动态渲染可以解决一部分过于臃肿的问题，但是我们还是有必要将RenderPass的基础概念给理清楚。

### 创建RenderPass

Vulkan的RenderPass由**附件（Attachments）**和**[子通道](https://zhida.zhihu.com/search?content_id=208516189&content_type=Article&match_order=1&q=子通道&zhida_source=entity)（Subpass）**组成，要创建RenderPass对象就要完成这两个部分的指定。

首先来介绍一下比较容易理解的附件，它与帧缓冲相对应，可以细分为颜色附件，深度附件，模板附件这几种类型，规定了在渲染通道中颜色缓冲/深度缓冲/模板缓冲将扮演什么样的角色以及被怎样使用，同时还拥有指定和改变图像布局的功能，比如一个颜色附件就是这样指定的：

```cpp
auto colorAttachment = vk::AttachmentDescription()
		.setFormat(vk::Format::eR16G16B16A16Sfloat)
		.setSamples(vk::SampleCountFlagBits::e1)
		.setLoadOp(vk::AttachmentLoadOp::eLoad)
		.setStoreOp(vk::AttachmentStoreOp::eStore)
		.setStencilLoadOp(vk::AttachmentLoadOp::eDontCare)
		.setStencilStoreOp(vk::AttachmentStoreOp::eDontCare)
		.setInitialLayout(vk::ImageLayout::eUndefined)
		.setFinalLayout(vk::ImageLayout::eColorAttachmentOptimal);
```

在这里除了指定附件的格式和多重采样数量以外，可以看到还指定了附件的初始布局（InitialLayout）和最终布局（FinalLayout），读写操作（LoadOp&StoreOp），模板读写操作（StencilLoadOp&StencilStoreOp），这些参数共同决定了该附件在整个RenderPass执行的操作。对于颜色附件而言，我们不关心它的模板操作，所以StencilLoadOp&StencilStoreOp皆为DontCare，而我们希望颜色附件的读写操作都被允许，所以将LoadOp设为Load，将StoreOp设为Store；我们需要将附件的最终布局设为ColorAttachmentOptimal以便作为颜色附件来使用。

类似的，深度附件可以按以下方法指定：

```cpp
auto depthAttachment = vk::AttachmentDescription()
		.setFormat(vk::Format::eD16Unorm)
		.setSamples(vk::SampleCountFlagBits::e1)
		.setLoadOp(vk::AttachmentLoadOp::eLoad)
		.setStoreOp(vk::AttachmentStoreOp::eStore)
		.setStencilLoadOp(vk::AttachmentLoadOp::eDontCare)
		.setStencilStoreOp(vk::AttachmentStoreOp::eDontCare)
		.setInitialLayout(vk::ImageLayout::eDepthStencilAttachmentOptimal)
		.setFinalLayout(vk::ImageLayout::eDepthStencilAttachmentOptimal);
```

介绍完附件之后，我们还需要指定**子通道（Subpass）**。Subpass同样是Vulkan独创的一个概念，目的是为了让Shader能够（借助InputAttachment）读取上一个Subpass输出的数据进行使用，并在移动端等低带宽的环境下取得较好的优化，但实际情况是Subpass带来的优化相当有限，而且由于InputAttachment仅能为着色器提供帧缓冲中相同位置的数据，Subpass的实际用途相当局限，通常也就是用来实现诸如G-Buffer的技术。但大部分需要多Subpass的场合都可以通过多RenderPass来解决，所以并不需要特意为了优化使用多Subpass。

关于Subpass和InputAttachment的更多内容将在下面讲解，这里我们仅仅为RenderPass指定单Subpass，并为其绑定需要用的附件：

```cpp
auto colorReference = vk::AttachmentReference()
		.setAttachment(0)
		.setLayout(vk::ImageLayout::eColorAttachmentOptimal);

auto depthReference = vk::AttachmentReference()
		.setAttachment(1)
		.setLayout(vk::ImageLayout::eDepthStencilAttachmentOptimal);

auto subpass = vk::SubpassDescription()
		.setPipelineBindPoint(vk::PipelineBindPoint::eGraphics)
		.setColorAttachmentCount(1)
		.setPColorAttachments(&colorReference)
		.setPDepthStencilAttachment(&depthReference);
```

在准备完所有附件和子通道之后，就可以完成RenderPass的创建：

```cpp
vk::AttachmentDescription attachments[] = {
	colorAttachment, depthAttachment
};

auto renderPassInfo = vk::RenderPassCreateInfo()
		.setAttachmentCount(2)
		.setPAttachments(attachments)
		.setSubpassCount(1)
		.setPSubpasses(&subpass);
vkInfo->device.createRenderPass(&renderPassInfo, 0, &renderPass);
```

> 在Vulkan1.1后，可以为pNext指定VkRenderPassInputAttachmentAspectCreateInfo结构体并通过AspectReferences来为InputAttachment的写入指定掩码。
> 在Vulkan1.1后，可以为pNext指定VkRenderPassMultiviewCreateInfo结构体并通过设置ViewMasks，ViewOffsets和CorrelationMasks来为RenderPass指定多视图的读写操作。

### 创建帧缓冲

Vulkan中的**帧缓冲（Framebuffer）**是一个包括了颜色缓冲，深度缓冲，模板缓冲的综合概念，并且与Subpass的AttachmentReference紧密对应着。在上面的RenderPass创建过程中我们仅仅是规定了每一个Attachment扮演的角色，而在Framebuffer中，我们就需要真正地为Attachment指定ImageView，告知RenderPass我们的渲染结果要输出到哪个图像当中。这整个过程就相当于将RenderPass和帧缓冲绑定在了一起。

创建一个包含了图像缓冲和深度缓冲的Framebuffer如下所示：

```cpp
vk::ImageView framebufferView[2];
framebufferView[0] = renderTarget.imageView;
framebufferView[1] = depthTarget.imageView;
auto framebufferInfo = vk::FramebufferCreateInfo()
		.setRenderPass(renderPass)
		.setAttachmentCount(2)
		.setPAttachments(framebufferView)
		.setWidth(vkInfo->width)
		.setHeight(vkInfo->height)
		.setLayers(1);
vkInfo->device.createFramebuffer(&framebufferInfo, 0, &framebuffer);
```

setRenderPass指定了Framebuffer绑定的RenderPass，setWidth和setHeight指定了帧缓冲的宽高，setLayers用于指定多视图的RenderPass，若未为RenderPass指定VkRenderPassMultiviewCreateInfo，则layers的值设为1。

> 在Vulkan1.2后，可以为flags指定Imageless旗标，为pNext指定VkFramebufferAttachmentsCreateInfo结构体，这会使得创建Framebuffer时指定ImageView是不必要的，可以单纯通过AttachmentImageInfos指定帧缓冲图像的属性。

### Subpass和InputAttachment

在上面创建RenderPass时我们已经简单了解过Subpass和它的用途，这里就来深入研究以下假如真的需要多Subpass，那么该如何实现。

Subpass通常和InputAttachment搭配在一起使用，这里假定我们使用两个Subpass（不会真有人需要两个以上Subpass吧），指定两个subpassDescriptions，再将第一个Subpass的输出指定为第二个Subpass的InputAttachment，注意不仅是颜色输出，深度和模板输出值等也可以作为InputAttachment使用。

在这里，我设定了一个类似G-Buffer的情景：在第一个Subpass中，将diffuse，normal，materialProperties，position，shadowPos这5种不同的数据存储在颜色缓冲中从像素着色器中输出，将深度值存储在深度缓冲中一同输出，再在第二个Subpass中，将上述的这6种数据全部作为InputAttachment输入到着色器中使用。需要注意的是，InputAttachment是由Subpass和描述符共同指定的，也就是说我们不仅需要在Subpass中说明InputAttachment，也需要为所有的InputAttachment创建一个专属的描述符集。

两个Subpass的写法如下所示：

```cpp
vk::AttachmentReference colorReference[5];
colorReference[0].setAttachment(2);
colorReference[0].setLayout(vk::ImageLayout::eColorAttachmentOptimal);
colorReference[1].setAttachment(3);
colorReference[1].setLayout(vk::ImageLayout::eColorAttachmentOptimal);
colorReference[2].setAttachment(4);
colorReference[2].setLayout(vk::ImageLayout::eColorAttachmentOptimal);
colorReference[3].setAttachment(5);
colorReference[3].setLayout(vk::ImageLayout::eColorAttachmentOptimal);
colorReference[4].setAttachment(6);
colorReference[4].setLayout(vk::ImageLayout::eColorAttachmentOptimal);

vk::AttachmentReference inputReference[6];
inputReference[0].setAttachment(2);
inputReference[0].setLayout(vk::ImageLayout::eShaderReadOnlyOptimal);
inputReference[1].setAttachment(3);
inputReference[1].setLayout(vk::ImageLayout::eShaderReadOnlyOptimal);
inputReference[2].setAttachment(4);
inputReference[2].setLayout(vk::ImageLayout::eShaderReadOnlyOptimal);
inputReference[3].setAttachment(5);
inputReference[3].setLayout(vk::ImageLayout::eShaderReadOnlyOptimal);
inputReference[4].setAttachment(6);
inputReference[4].setLayout(vk::ImageLayout::eShaderReadOnlyOptimal);
inputReference[5].setAttachment(1);
inputReference[5].setLayout(vk::ImageLayout::eShaderReadOnlyOptimal);

vk::AttachmentReference renderTargetReference;
renderTargetReference.setAttachment(0);
renderTargetReference.setLayout(vk::ImageLayout::eColorAttachmentOptimal);

vk::AttachmentReference depthReference;
depthReference.setAttachment(1);
depthReference.setLayout(vk::ImageLayout::eDepthStencilAttachmentOptimal);
	
std::vector<vk::SubpassDescription> subpassDescriptions(2);
subpassDescriptions[0].setPipelineBindPoint(vk::PipelineBindPoint::eGraphics);
subpassDescriptions[0].setColorAttachmentCount(5);
subpassDescriptions[0].setPColorAttachments(colorReference);
subpassDescriptions[0].setPDepthStencilAttachment(&depthReference);

subpassDescriptions[1].setPipelineBindPoint(vk::PipelineBindPoint::eGraphics);
subpassDescriptions[1].setColorAttachmentCount(1);
subpassDescriptions[1].setPColorAttachments(&renderTargetReference);
subpassDescriptions[1].setInputAttachmentCount(6);
subpassDescriptions[1].setPInputAttachments(inputReference);
```

为InputAttachment创建描述符集并全部完成更新如下所示：

```cpp
//Create descriptor set
std::array<vk::DescriptorSetLayoutBinding, 6> layoutBinding_inputAttach;
layoutBinding_inputAttach[0].setBinding(0);
layoutBinding_inputAttach[0].setDescriptorCount(1);
layoutBinding_inputAttach[0].setDescriptorType(vk::DescriptorType::eInputAttachment);
layoutBinding_inputAttach[0].setStageFlags(vk::ShaderStageFlagBits::eFragment);
layoutBinding_inputAttach[1].setBinding(1);
layoutBinding_inputAttach[1].setDescriptorCount(1);
layoutBinding_inputAttach[1].setDescriptorType(vk::DescriptorType::eInputAttachment);
layoutBinding_inputAttach[1].setStageFlags(vk::ShaderStageFlagBits::eFragment);
layoutBinding_inputAttach[2].setBinding(2);
layoutBinding_inputAttach[2].setDescriptorCount(1);
layoutBinding_inputAttach[2].setDescriptorType(vk::DescriptorType::eInputAttachment);
layoutBinding_inputAttach[2].setStageFlags(vk::ShaderStageFlagBits::eFragment);
layoutBinding_inputAttach[3].setBinding(3);
layoutBinding_inputAttach[3].setDescriptorCount(1);
layoutBinding_inputAttach[3].setDescriptorType(vk::DescriptorType::eInputAttachment);
layoutBinding_inputAttach[3].setStageFlags(vk::ShaderStageFlagBits::eFragment);
layoutBinding_inputAttach[4].setBinding(4);
layoutBinding_inputAttach[4].setDescriptorCount(1);
layoutBinding_inputAttach[4].setDescriptorType(vk::DescriptorType::eInputAttachment);
layoutBinding_inputAttach[4].setStageFlags(vk::ShaderStageFlagBits::eFragment);
layoutBinding_inputAttach[5].setBinding(5);
layoutBinding_inputAttach[5].setDescriptorCount(1);
layoutBinding_inputAttach[5].setDescriptorType(vk::DescriptorType::eInputAttachment);
layoutBinding_inputAttach[5].setStageFlags(vk::ShaderStageFlagBits::eFragment);
auto descSetLayoutInfo = vk::DescriptorSetLayoutCreateInfo()
			.setBindingCount(layoutBinding_inputAttach.size())
			.setPBindings(layoutBinding_inputAttach.data());
vkInfo->device.createDescriptorSetLayout(&descSetLayoutInfo, 0, &descSetLayout);

//Update descriptor set
std::array<vk::WriteDescriptorSet, 6> updateInfo;

auto diffuseAttachInfo = vk::DescriptorImageInfo()
			.setImageLayout(vk::ImageLayout::eShaderReadOnlyOptimal)
			.setImageView(gbuffer.diffuseAttach.imageView);
updateInfo[0] = vk::WriteDescriptorSet()
			.setDescriptorCount(1)
			.setDescriptorType(vk::DescriptorType::eInputAttachment)
			.setDstArrayElement(0)
			.setDstBinding(0)
			.setDstSet(gbuffer.descSet)
			.setPImageInfo(&diffuseAttachInfo);

auto normalAttachInfo = vk::DescriptorImageInfo()
			.setImageLayout(vk::ImageLayout::eShaderReadOnlyOptimal)
			.setImageView(gbuffer.normalAttach.imageView);
updateInfo[1] = vk::WriteDescriptorSet()
			.setDescriptorCount(1)
			.setDescriptorType(vk::DescriptorType::eInputAttachment)
			.setDstArrayElement(0)
			.setDstBinding(1)
			.setDstSet(gbuffer.descSet)
			.setPImageInfo(&normalAttachInfo);

auto materialAttachInfo = vk::DescriptorImageInfo()
			.setImageLayout(vk::ImageLayout::eShaderReadOnlyOptimal)
			.setImageView(gbuffer.materialAttach.imageView);
updateInfo[2] = vk::WriteDescriptorSet()
			.setDescriptorCount(1)
			.setDescriptorType(vk::DescriptorType::eInputAttachment)
			.setDstArrayElement(0)
			.setDstBinding(2)
			.setDstSet(gbuffer.descSet)
			.setPImageInfo(&materialAttachInfo);

auto positionAttachInfo = vk::DescriptorImageInfo()
			.setImageLayout(vk::ImageLayout::eShaderReadOnlyOptimal)
			.setImageView(gbuffer.positionAttach.imageView);
updateInfo[3] = vk::WriteDescriptorSet()
			.setDescriptorCount(1)
			.setDescriptorType(vk::DescriptorType::eInputAttachment)
			.setDstArrayElement(0)
			.setDstBinding(3)
			.setDstSet(gbuffer.descSet)
			.setPImageInfo(&positionAttachInfo);

auto shadowPosAttachInfo = vk::DescriptorImageInfo()
			.setImageLayout(vk::ImageLayout::eShaderReadOnlyOptimal)
			.setImageView(gbuffer.shadowPosAttach.imageView);
updateInfo[4] = vk::WriteDescriptorSet()
			.setDescriptorCount(1)
			.setDescriptorType(vk::DescriptorType::eInputAttachment)
			.setDstArrayElement(0)
			.setDstBinding(4)
			.setDstSet(gbuffer.descSet)
			.setPImageInfo(&shadowPosAttachInfo);

auto depthAttachInfo = vk::DescriptorImageInfo()
			.setImageLayout(vk::ImageLayout::eShaderReadOnlyOptimal)
			.setImageView(depthTarget.imageView);
updateInfo[5] = vk::WriteDescriptorSet()
			.setDescriptorCount(1)
			.setDescriptorType(vk::DescriptorType::eInputAttachment)
			.setDstArrayElement(0)
			.setDstBinding(5)
			.setDstSet(gbuffer.descSet)
			.setPImageInfo(&depthAttachInfo);

vkInfo->device.updateDescriptorSets(updateInfo.size(), updateInfo.data(), 0, 0);
```

InputAttachment在着色器中的引用方式如下所示：

```cpp
//HLSL
[vk::input_attachment_index(0)] [vk::binding(0, 1)] SubpassInput inDiffuseAlbedo;
[vk::input_attachment_index(1)] [vk::binding(1, 1)] SubpassInput inNormal;
[vk::input_attachment_index(2)] [vk::binding(2, 1)] SubpassInput inMaterialProperties;
[vk::input_attachment_index(3)] [vk::binding(3, 1)] SubpassInput inPosition;
[vk::input_attachment_index(4)] [vk::binding(4, 1)] SubpassInput inShadowPos;
[vk::input_attachment_index(5)] [vk::binding(5, 1)] SubpassInput inDepth;
```

[vk::input_attachment_index( )]指定了Subpass中InputAttachment的索引位置，[vk::binding( )]指定了InputAttachment在管线布局中的位置。

到此，我们已经简单完成了Subpass和InputAttachment的指定，但我们的工作并未就此结束，可以看到之前在指定VkRenderPassCreateInfo时，我们有一个名为pDependencies的参数尚未使用，它对应结构体VkSubpassDependencies，称为**子通道依赖**，它用于在渲染通道中指定资源的同步依赖，相当于隐式地放置管线屏障。

> 当 srcSubpass 和 dstSubpass 相同时，表示在一个子通道内部设置同步依赖；当 srcSubpass 或 dstSubpass 中存在 VK_SUBPASS_EXTERNAL 时，表示在进入或离开渲染通道时设置同步依赖。

为了让不同的Subpass在访问资源时不会紊乱，像下面一样设立一个VkSubpassDependencies是必要的：

```cpp
std::vector<vk::SubpassDependency> subpassDependencies(1);
subpassDependencies[0].setSrcSubpass(0);
subpassDependencies[0].setDstSubpass(1);
subpassDependencies[0].setSrcAccessMask(vk::AccessFlagBits::eColorAttachmentWrite);
subpassDependencies[0].setDstAccessMask(vk::AccessFlagBits::eInputAttachmentRead);
subpassDependencies[0].setSrcStageMask(vk::PipelineStageFlagBits::eColorAttachmentOutput);
subpassDependencies[0].setDstStageMask(vk::PipelineStageFlagBits::eFragmentShader);
subpassDependencies[0].setDependencyFlags(vk::DependencyFlagBits::eByRegion);

renderPassInfo.setSubpassCount(subpassDescriptions.size());
renderPassInfo.setPSubpasses(subpassDescriptions.data());
```

- srcSubpass和dstSubpass指定了子通道依赖作用前后的子通道。
- srcAccessMask和dstAccessMask指定了许可操作的改变，这里将颜色附件写入转变为InputAttachment读出。
- srcStageMask和dstStageMask指定了前后的管线阶段，这里将dstStageMask设为FragmentShader表示InputAttachment是供给像素着色器使用的。
- 这里将dependencyFlags指定为ByRegion表示子通道依赖是相对于帧缓冲执行的，除了ByRegion以外，还有DeviceGroup和ViewLocal，通常不需要使用，具体的用法可以查阅VkSpec。

至此，我们已经完成了所有繁琐的准备工作，接下来就可以愉快地使用多Subpass了。

### RenderPass相关命令

> 在Vulkan1.2后添加了新版本的vkCmdBeginRenderPass2和vkCmdEndRenderPass2函数，不过和初版函数基本没有区别，所以接下来我都直接使用初版函数。

- **开启RenderPass**

使用vkCmdBeginRenderPass向命令缓冲区添加开启RenderPass命令，如下所示：

```cpp
vk::ClearValue clearValue[2];
clearValue[0].setColor(vk::ClearColorValue(std::array<float, 4>({ 0.0f, 0.0f, 0.0f, 0.0f })));
clearValue[1].setDepthStencil(vk::ClearDepthStencilValue(1.0f, 0.0f));

auto renderPassBeginInfo = vk::RenderPassBeginInfo()
		.setClearValueCount(2)
		.setPClearValues(clearValue)
		.setFramebuffer(framebuffer)
		.setRenderPass(renderPass)
		.setRenderArea(vk::Rect2D(vk::Offset2D(0.0f, 0.0f), vk::Extent2D(vkInfo->width, vkInfo->height)));
cmd.beginRenderPass(&renderPassBeginInfo, vk::SubpassContents::eInline);
```

开启RenderPass后会自动将Attachments绑定的图像清除为特定值，这个值由VkClearValue指定，分为颜色值和深度模板值，之后所有渲染都会展开在renderArea指定的范围内。

vkCmdBeginRenderPass函数中的contents参数指定了第一个Subpass中的命令的呈递方式，Inline表示Subpass中的命令将被内联在主级命令缓冲区中，同时不允许次级命令缓冲区中的命令在Subpass中执行；SecondaryCommandBuffers表示记录在次级命令缓冲区中的内容将从主级命令缓冲区执行，并且vkCmdExecuteCommands会在vkCmdNextSubpass或vkCmdEndRenderPass后起效。

> 在Vulkan1.1后，可以为pNext指定VkDeviceGroupRenderPassBeginInfo结构体并通过设定设备掩码和deviceRenderAreas来为不同的GPU设备分派不同的渲染区域。
> 在Vulkan1.2后，可以为pNext指定VkRenderPassAttachmentBeginInfo结构体来为未指明ImageView的RenderPass指定ImageView。

- **切换至下一个Subpass**

```cpp
cmd.nextSubpass(vk::SubpassContents::eInline);
```

contents参数的含义和上面所讲的相同。

- **结束RenderPass**

```cpp
cmd.endRenderPass();
```

- **获取RenderPass的渲染粒度**

```cpp
vk::Extent2D granularity;
vkInfo->device.getRenderAreaGranularity(renderPass, &granularity);
```

### 动态渲染

动态渲染是Vulkan1.3搞出来的新东西，它允许我们可以在不创建RenderPass对象的前提下，只需要通过指定一系列渲染相关的参数就可以直接开始渲染，可以减轻一部分的代码工作量。下面我们就来了解一下动态渲染的使用方式。

使用动态渲染需要开启dynamicRendering feature，开启此feature需要在创建逻辑设备时将VkPhysicalDeviceVulkan13Feature添加到pNext中：

```cpp
auto feature13 = vk::PhysicalDeviceVulkan13Features()
		.setDynamicRendering(VK_TRUE);
deviceInfo.setPNext(&feature13);
vkInfo.gpu.createDevice(&deviceInfo, 0, &vkInfo.device);
```

- **开始动态渲染**

使用vkCmdBeginRendering向命令缓冲区添加开启动态渲染命令，如下所示：

```cpp
auto colorAttach = vk::RenderingAttachmentInfo()
		.setClearValue(vk::ClearColorValue(std::array<float, 4>({ 0.0f, 0.0f, 0.0f, 0.0f })))
		.setImageLayout(vk::ImageLayout::eColorAttachmentOptimal)
		.setImageView(renderTarget.imageView)
		.setLoadOp(vk::AttachmentLoadOp::eLoad)
		.setStoreOp(vk::AttachmentStoreOp::eStore);

auto depthAttach = vk::RenderingAttachmentInfo()
		.setClearValue(vk::ClearDepthStencilValue(1.0f, 0.0f))
		.setImageLayout(vk::ImageLayout::eDepthStencilAttachmentOptimal)
		.setImageView(depthTarget.imageView)
		.setLoadOp(vk::AttachmentLoadOp::eLoad)
		.setStoreOp(vk::AttachmentStoreOp::eStore);

auto renderInfo = vk::RenderingInfo()
		.setColorAttachmentCount(1)
		.setColorAttachmentCount(1)
		.setPColorAttachments(&colorAttach)
		.setPDepthAttachment(&depthAttach)
		.setRenderArea(vk::Rect2D(vk::Offset2D(0.0f, 0.0f), vk::Extent2D(vkInfo->width, vkInfo->height)))
		.setLayerCount(1)
		.setViewMask(0);
cmd.beginRendering(&renderInfo);
```

这里的大部分参数设置和创建RenderPass时是类似的，同样需要指定Attachment，不同的是多了resolveMode，resolveImageView和resolveImageLayout这些参数：resolveMode指定了写入imageView的多重采样数据将被怎样解析，resolveImageView和resolveImageLayout则指定了解析后的多重采样数据将被写入的位置。layerCount和viewMask用于指定多视图渲染，当viewMask设为0，layerCount设为1时，将不采用多视图。

flags指定ContentsSecondaryCommandBuffers表示该动态渲染通道中的DrawCall将被记录在次级命令缓冲区中；指定Suspended表示该渲染通道将被延宕，指定Resuming表示该渲染通道是恢复自一个延宕的渲染通道，被延宕和被恢复的动态渲染通道必须拥有相同的RenderingInfo，同时，在延宕和恢复动态渲染通道之间，动作/同步命令和其它的RenderPass实例都是不被允许的。

> 和vkCmdBeginRenderPass一样，VkDeviceGroupRenderPassBeginInfo可用于pNext，除此之外还有VkMultiviewPerViewAttributesInfoNVX，VkRenderingFragmentDensityMapAttachmentInfoEXT和VkRenderingFragmentShadingRateAttachmentInfoKHR扩展结构可用于pNext。

- **结束动态渲染**

```cpp
cmd.endRendering();
```

值得一提的是，动态渲染是不提供Subpass功能的，大概Khronos自己也知道Subpass根本不好用吧（笑）。

