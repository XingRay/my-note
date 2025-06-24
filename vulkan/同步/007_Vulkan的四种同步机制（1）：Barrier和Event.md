# Vulkan的四种同步机制（1）：Barrier和Event

首先我们要清楚为什么要同步。**同步这个概念是和异步相对的**，异步的理念是同时能够做多件事，并且彼此之间不存在依赖关系互不打扰。而与之对应的就是同步的概念，必须处理完某个操作之后才能进行另外一个操作。

用生活中的例子来比较的话：

 异步就是一边吃饭一边玩手机
 同步是吃完饭才能够玩手机



**在Vulkan中我们为什么要实现同步？为什么Vulkan的同步原语设计的这么复杂？**
在Vulkan中，我们的命令是按照队列先进先出的顺序进行的，但是在队列提交完命令发送到相关的执行单元时，由于不同的执行单元执行速度不同等原因，很可能导致后边要读取的一块内存，前边还没有计算完成。**换句话说提交的命令是同步的，但是每个执行命令的单元是异步工作的**。这时我们就需要告诉执行单元，执行单元的先后顺序是怎么样的，即把异步执行的单元给同步化。

Vulkan提出了四种同步原语使我们在不同情况下实现同步：
**四种原语分别为：Barrier，Event，Semaphore，Fence**。
在介绍这几种同步原语之前我们要先介绍以下一些基础概念。



**Queue**
首先，我们要知道学习Vulkan很重要的一个机制：**命令缓冲区是没有边界概念的**。虽然在实际的流程中，我们是先将一个一个命令添加到对应的Command Buffer中，最后在Loop阶段将每一个Command加入到命令缓冲区当中。
这也意味着**所有的同步操作都是在全局命令中完成的，而不是单单的某一段命令缓冲区**。



**Pipeline Stage**
Pipeline Stage是指渲染管线的阶段，Vulkan将渲染管线的每个阶段都封装成了一个枚举类型VkPipelineStageFlags。进行同步的单位是整条流水线上阐述的工作，而并非单个的命令。以下是几个常见的VkPipelineStageFlags的成员：

```
VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT:当设备开始处理命令时，马上认为访问到了管线的顶端。
VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT:当一个挥之命令产生的所有片元着色器调用都完成时，这个阶段通过。
VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT:在片元着色器开始运行之前可能发生的所有逐片元操作都完成了。
VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT:管线产生的片段都已经写入到了颜色副件中。
VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT:调度产生的计算着色器调用已经完成。
VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT:图形管线的任何一部分的操作都已经完成
```

而其中的VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT和VK_PIPELINE_STAG–E_BOTTOM_OF_PIPE这两个渲染管线没有实际的工作，本质是“帮助程序”阶段：

 每个命令将首先执行TOP_OF_PIPE阶段，这基本上市GPU上解析命令的命令处理器
 在所有工作完成后，BOTTOM_OF_PIPE是通知命令退出的地方。



**Barrier**
Barrier是一种同步机制，用来管理内存访问，以及在Vulkan管线各个阶段里的资源状态变化。对资源访问进行同步和改变状态的主要命令是

```
vkCmdPipelineBarrier(
	VkCommandBuffer commandBuffer,
	VkPipelineStageFlags srcStageMask,,
	VkPipelineStageFlags dstStageMask,
	VkDependencyFlags dependencyFlags,
	uint32_t memoryBarrierCount,
	const VkMemoryBarrier* pMemoryBarriers,
	uint32_t bufferMemoryBarrierCount,
	const VkBufferMemoryBarrier* pBufferMemoryBarriers,
	uint32_t imageMemoryBarrierCount,
	const VkImageMemoryBarrier* pImageMemoryBarriers
)
```

Bariier将命令流一分为二。



**SrcStageMask**
Vulkan不支持在单个命令之间添加细粒度的依赖关系。这意味着，必须查看在某些管线阶段中发生的所有工作，例如：

```
vkCmdDispatch(VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT)
vkCmdCopyBuffer(VK_PIPELINE_STAGE_TRANSFER_BIT)
vkCmdDispatch(VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT)
vkCmdPipelineBarrier(srcStageMask = ???)
```

以上边例子为例，如果srcStageMask为以下几个状态时：

  VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT:等待之前的两个vkCmdDispatch命令完成
  VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT|VK_PIPELINE_STAGE_TRANSFER_BIT:等待之前的三个任务完成
  ALL_COMMANDS_BIT:等待之前的所有工作命令完成
  ALL_GRAPHICS_BIT:等待之前的所有渲染流程中的命令完成
  TOP_OF_PIPELINE:基本什么命令都不等待

本质上，我们正在等待的工作是：**曾经提交到队列的所有命令（包括我们正在记录的命令缓冲区中的所有先前命令）执行完srcStageMas–k指定的所有管线阶段（不用等待那些不需要执行这些阶段的命令）**。

**dstStageMask**

```
1.vkCmdDispatch
2.vkCmdDispatch
3.vkCmdDispatch
4.vkCmdPipelineBarrier(srcStageMask = COMPUTE,dstStageMask = COMPUTE)
5.vkCmdDispatch
6.vkCmdDispatch
7.vkCmdDispatch
```

在上述执行中，｛1，2，3｝可以无序执行，｛5，6，7｝也可以无序执行，但是这两套命令不能交错执行。在规范术语中，｛1，2，3｝发生在｛5，6，7｝之前。
dstStageMask的含义是：之后提交到队列的所有命令（包括我们正在记录的命令缓冲区中的所有之后的命令）必须等到所有等待工作完成，才能执行dstStageMask指定的所有管线阶段。



**EVENT**
事件(event)：表示一个细粒度的同步原语，可由主机或设备发出。当设备发出信号是，可以在命令缓冲区中通知它，并且在管线中的特定点上可以由设备等待它。

```
vkCmdDispatch
vkCmdDispatch
vkCmdSetEvent(event,srcStageMask = COMPUTE)
vkCmdDispatch
vkCmdSetEvent(event,dstStageMask = COMPUTE) 
vkCmdDispatch
vkCmdDispatch
```

事件的行为与屏障十分相似。在上述代码块中“前”集合是｛1，2｝，“后”集合是｛6，7｝。而4不受任何同步的影响。



**Execution dependency chain**
需要注意的是我们在等待一个操作的时候，必须停下后续所有与这个操作相关的操作。如下：

```
1.vkCmdDispatch
2.vkCmdDispatch
3.vkCmdPipelineBarrier(srcStageMask = COMPUTE,dstStageMask = TRANSFER)
4.vkCmdMagicDummyTransferOperation
5.vkCmdPipelineBarrier(srcStageMask = TRANSFER,dstStageMask = COMPUTE)
6.vkCmdDispatch
7.vkCmdDispatch
```

当{4}操作在等待{1，2}完成时，由于｛6，7｝与｛4｝存在依赖性，所以｛6，7｝也要停下等待｛4｝的完成，这时形成依赖链条的基础。



**Render Pipeline**
与Compute Pipeline相比，渲染流程要复杂很多。渲染流程可以分为Geometry和Fragment两大阶段。
**Geometry**

```
DRAW_INDIRECT - Parses indirect buffers
VERTEX_INPUT - Consumes fixed function VBOs and IBOs
VERTEX_SHADER - Actual vertex shader
TESSELLATION_CONTROL_SHADER
TESSELLATION_EVALUATION_SHADER
GEOMETRY_SHADER
```


**Fragment**

```
EARLY_FRAGMENT_TESTS
FRAGMENT_SHADER
LATE_FRAGMENT_TESTS
COLOR_ATTACHMENT_OUTPUT
```

其中EARLY_FRAGMENT_TESTS为读取阶段，LATE_FRAGMENT_TESTS为写入阶段。



**Memory barriers**
Memory barrier全局内存屏障，在vkCmdPipelineBarrier()中通过memoryBarri-erCount指定要出发的数量，如果这是一个非零值，则其数组的每一个元素都代表一个内存屏障。
先介绍与内存相关的两个术语：

  available(使内存可用)：刷新L2 cache(将最新的数据更新到L2 cache中，即拿到GPU中)
  visible(使内存可见)：刷新L1 cache(将L2 cache中的数据更新到L1 cache中，即传入Chip里)
Vulkan规定：一旦着色器阶段写入内存，L2缓存就不再拥有最新的数据，因此不再认为内存可用。如果其他缓存尝试从L2读取，它将看到未定义的数据。因此**无论写入什么数据，都必须使这些写入可用，才能够使数据再次可见**。

```
typedef struct VkMemoryBarrier{
	VkStructureType sType;
	const void* pNext
	VkAccessFlags srcAccessMask;
	VkAccessFlags dstAccessMask;
}VkMemoryBarrier;
```

此数据类型中有两个字段srcAccessMask和dstAccessMask，分别表示源与目标的访问掩码。
VkAccessFlags用于表示资源访问的标识位，表示了这个资源在经历了某个操作前和操作后的访问状态。

在vkCmcPipelineBarrier里添加VkMemoryBarrier，意味着以下四件事依次发生：

1 等待srcStageMask完成
2 使srcStageMask+srcAccessMask的可能组合执行的所有写操作均可用（将之前的写操作刷新到L2中）
3 使可用内存对dstStageMask+dstAccessMask的可能组合可见（将L2中的可用数据更新到L1中）
4 取消组织dstStageMask中的工作