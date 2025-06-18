# Vulkan中VkRenderPass、VkSubpass、VkSubpassDependency的概念和理解，以及多subpass下的指令记录



VkRenderPass 是一个描述性的对象，它定义了在渲染过程中附件的布局变化、同步操作、以及一系列子流程之间的关系。

VkRenderPass 的创建是在描述这些信息，而真正的命令记录则发生在命令缓冲构建的过程中。

1、附件（颜色附件和深度/模板缓冲附件）
当你创建附件时，它的描述结构体中并没有指定该附件是颜色附件还是深度附件，仅仅是描述了这块内存的像素格式等信息

对于附件描述符内的 initialLayout 、finalLayout 指定的是该附件在Renderpass之前和之后的布局，这个布局会在渲染过程中发生改变（subpass可能会改变布局），不过最终布局还是会变成 finalLayout 指定的那样

图像布局：不同的布局会让对应的操作变得高效，这涉及到GPU的访问控制以及内存中的该数据的排布方式。


```
void setupRenderPass()
{
	// 两个附件的描述结构体
	std::array<VkAttachmentDescription, 2> attachments = {};

	attachments[0].format = VK_FORMAT_B8G8R8A8_UNORM;                
	attachments[0].samples = VK_SAMPLE_COUNT_1_BIT;                  // 不使用多重采样
	attachments[0].loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;             // 在渲染通道开始时清除此附件
	attachments[0].storeOp = VK_ATTACHMENT_STORE_OP_STORE;           // 在渲染通道结束后保留其内容（以供显示）
	attachments[0].stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;  // 不使用模板，所以不需要加载
	attachments[0].stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;// 同上
	attachments[0].initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;         // 渲染通道开始时的布局。初始布局并不重要，所以我们使用未定义的布局
	attachments[0].finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;     // 渲染通道结束时转换到的布局，由于我们想将颜色缓冲区呈现到交换链，因此我们转换为PRESENT_KHR布局

	attachments[1].format = VK_FORMAT_D32_SFLOAT_S8_UINT;                                           
	attachments[1].samples = VK_SAMPLE_COUNT_1_BIT;
	attachments[1].loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;                           
	attachments[1].storeOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;                     
	attachments[1].stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;                // 没使用模板
	attachments[1].stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;              
	attachments[1].initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;                     
	attachments[1].finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL; 
```

2、附件引用
附件引用，就是附件的引用，每个subpass可以指定的流程中需要用到的附件是哪些，以及使用该附件时，该附件的布局方式应该变为什么（layout字段指定）。

通过在VkAttachmentReference中为每个附件指定layout字段，可以灵活地控制附件在子流程中的使用布局，而不仅仅受限于初始布局。这种灵活性有助于优化渲染流程，提高性能。

这里就能够看出为啥要用引用而不是直接使用附件了，灵活

	// Setup attachment references
	VkAttachmentReference colorReference = {};
	colorReference.attachment = 0;                                    
	colorReference.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL; 
	
	VkAttachmentReference depthReference = {};
	depthReference.attachment = 1;                                            
	depthReference.layout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL; 
	
	// 如果还有subpass2，它需要的颜色附件布局类型是不同的，则应该重新创建颜色附件的引用
	VkAttachmentReference colorReferenceForPass2 = {};
	colorReference.attachment = 0;                                    
	colorReference.layout = VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL; // shader只读
	// 如果还有subpass3，它的需求跟subpass1一样，那就直接重用上面的colorReference 即可


3、子通道
子通道的处理过程中，附件引用的布局在创建引用的时候就指定过了

```
VkSubpassDescription subpassDescription = {};
subpassDescription.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
subpassDescription.colorAttachmentCount = 1;                            
subpassDescription.pColorAttachments = &colorReference;      
subpassDescription.pDepthStencilAttachment = &depthReference;
```


4、子通道依赖
子流程依赖VkSubpassDependency，主要用于描述在渲染流程的不同子流程之间的同步和布局转换关系。通过显式定义子流程依赖关系，可以确保在渲染过程中的不同阶段之间正确同步资源的访问，并在需要时执行布局转换。这有助于优化渲染流程的性能和正确性。



同步资源访问： 确保在不同的子流程中正确同步对共享资源（例如图像附件）的读写操作，避免数据竞争和不一致性。



布局转换： 在子流程之间可能需要进行图像布局的转换，以满足不同操作阶段的需求。显式指定依赖关系可以确保在子流程之间进行正确的布局转换。



提高性能： 显式指定的依赖关系可以帮助渲染引擎更好地理解渲染流程的执行顺序，从而更有效地进行优化



对于依赖关系中的内存读取和写入，通常是指对于GPU某个资源（比如图像或缓冲区）的读取和写入，不是主机内存



每个子通道对应的附件布局会根据需要进行布局转换，通过子通道依赖进行定义，比如将颜色附件布局 COLOR_ATTACHMENT_OPTIMAL 转换为 TRANSFER_SRC_OPTIMAL 以提高图像数据拷贝的操作执行速度。



本例中，在只有一个子流程的情况下，通常不需要显式定义任何依赖关系。渲染流程会有两个默认的隐式依赖关系 ，分别处理渲染流程开始时的布局转换（从initialLayout转换成subpass的附件引用指定的布局）和结束时的布局转换（从subpass附件引用指定的layout转换成finalLayout）。



注意：在多个子流程的情况下，仍然存在这两个默认的隐式依赖关系。子通道开始前和结束后的所有隐式的操作 可以分别看做一个subpass



开始时的依赖 (VK_SUBPASS_EXTERNAL 到第一个子流程)： 这个依赖关系会确保在渲染流程开始时，图像的布局会正确地转换到第一个子流程所期望的布局。这是一个隐含的依赖，通常不需要显式定义。

结束时的依赖 (最后一个子流程到 VK_SUBPASS_EXTERNAL)： 这个依赖关系确保在渲染流程结束时，图像的布局会正确地转换回到适合交换链呈现的布局。这同样是一个隐含的依赖，通常也不需要显式定义。
VK_SUBPASS_EXTERNAL 是一个特殊的常数，它代表所有在实际渲染通道之外执行的命令

如果显示定义依赖的话，则大概是这样：

```
VkSubpassDependency dependencyBegin = {};
dependencyBegin.srcSubpass = VK_SUBPASS_EXTERNAL;
dependencyBegin.dstSubpass = 0;
dependencyBegin.srcStageMask = VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
dependencyBegin.dstStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
dependencyBegin.srcAccessMask = VK_ACCESS_MEMORY_READ_BIT;
dependencyBegin.dstAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
dependencyBegin.dependencyFlags = VK_DEPENDENCY_BY_REGION_BIT;

VkSubpassDependency dependencyEnd = {};
dependencyEnd.srcSubpass = 0;
dependencyEnd.dstSubpass = VK_SUBPASS_EXTERNAL;
dependencyEnd.srcStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
dependencyEnd.dstStageMask = VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT;
dependencyEnd.srcAccessMask = VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
dependencyEnd.dstAccessMask = VK_ACCESS_MEMORY_READ_BIT;
dependencyEnd.dependencyFlags = VK_DEPENDENCY_BY_REGION_BIT;
```

这些 Mask 和 Flags 在 Vulkan 中用于描述同步和依赖关系的细节。



字段解释：

srcSubpass ：源子通道，被依赖者

dstSubpass ：目标子通道，依赖者，也可理解为在这里做同步

srcStageMask：源子通道的阶段标志，即被依赖的或者说是同步的操作在哪个管线阶段，VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT表示管线底部，即读取附件内容呈现到表面

dstStageMask ： 目标子通道的阶段标志，即这个子通道的哪个管线阶段需要指明依赖关系？谁需要同步操作？VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;表示附件写入阶段（输出内容到附件上）

srcAccessMask ： 源子通道的操作标志，即这个阶段GPU在干啥，VK_ACCESS_MEMORY_READ_BIT;表示在读取内存呢

dstAccessMask ： 目标子通道的操作标志，VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT表示这个阶段我要写入操作，肯定依赖于上一个阶段对这里的读取已经完毕了才行啊，同步同步



5、创建Render pass               

	    // Create the actual renderpass
	    VkRenderPassCreateInfo renderPassInfo = {};
	    renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
	    renderPassInfo.attachmentCount = static_cast<uint32_t>(attachments.size());  // renderpass附件数
	    renderPassInfo.pAttachments = attachments.data();                            
	    renderPassInfo.subpassCount = 1;                                             // subpass个数
	    renderPassInfo.pSubpasses = &subpassDescription;                             
	    renderPassInfo.dependencyCount = static_cast<uint32_t>(dependencies.size()); // subpass dependencies个数
	    renderPassInfo.pDependencies = dependencies.data();        
	    VK_CHECK_RESULT(vkCreateRenderPass(device, &renderPassInfo, nullptr, &renderPass));
	}

6、多subpass情况下的渲染指令记录
在命令缓冲构建的过程中，通过使用 vkCmdBeginRenderPass、vkCmdNextSubpass 和 vkCmdEndRenderPass 记录 Vulkan 命令，指示 Vulkan API 在 GPU 上执行渲染操作。这些命令会在每个子流程之间进行切换，执行渲染操作，并保证正确的同步和布局变化。

假设有4个subpass，那么在命令缓冲构建的过程中，你需要在每个子流程之间切换，并在每个子流程中执行相应的渲染命令。以下是一个简化的示例：

```
// 开始renderpass
vkCmdBeginRenderPass(commandBuffer, &renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

// 第一个子流程
// ...

// 切换到下一个子流程
vkCmdNextSubpass(commandBuffer, VK_SUBPASS_CONTENTS_INLINE);

// 第二个子流程
// ...

// 切换到下一个子流程
vkCmdNextSubpass(commandBuffer, VK_SUBPASS_CONTENTS_INLINE);

// 第三个子流程
// ...

// 切换到下一个子流程
vkCmdNextSubpass(commandBuffer, VK_SUBPASS_CONTENTS_INLINE);

// 第四个子流程
// ...

// 结束Renderpass
vkCmdEndRenderPass(commandBuffer);
```

在上述示例中，通过使用 vkCmdNextSubpass 命令在每个子流程之间切换。在每个子流程中，可以执行相应的渲染命令，比如绘制图元、设置管线状态等。切换子流程的目的是确保 Vulkan 在 GPU 上以正确的顺序和同步执行渲染操作。



通过记录这些命令，实际上描述了在 VkRenderPass 中定义的渲染过程的执行步骤。这种分离设计允许 Vulkan 提供更大的灵活性，允许开发者在渲染过程中的每个子流程中执行自定义的命令序列。
