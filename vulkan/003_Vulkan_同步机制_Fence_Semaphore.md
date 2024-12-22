# Vulkan 同步机制 Fence Semaphore

大家好，接下来将为大家介绍Vulkan 同步机制Fence Semaphore。

在Vulkan中，对资源读写所需要的同步操作是应用程序开发者的职责，Vulkan本身只提供了很少的隐式同步机制，其余的都需要在程序中显式地使用Vulkan中的同步机制来实现。

一、提交顺序

提交顺序是Vulkan中的一个非常基本的概念，它本身并不具有任何同步的意义，但是不管是Vulkan提供的隐式同步，还是用户要自己实现的显式同步，都要以这个精确的概念为前提。
在Vulkan中，用户需要将命令写入CommandBuffer中，然后把一个或多个CommandBuffer写入到一个或多个VkSubmitInfo中，再把一个或多个VkSubmitInfo传给vkQueueSubmit，让Queue开始执行向GPU传入的命令，由此，从高往低，提交顺序为：
1.在CPU上通过多次vkQueueSubmit提交了一系列命令，这些命令的提交顺序为调用vkQueueSubmit从前往后的顺序，即先通过vkQueueSubmit提交的命令一定在后通过vkQueueSubmit提交的命令之前。

2.在同一次vkQueueSubmit中，传入了一个或多个VkSubmitInfo，这些VkSubmitInfo中的命令，按照VkSubmitInfo的下标顺序排列，即在pSubmits所指向的VkSubmitInfo数组中，下标靠前的VkSubmitInfo中所记录的所有命令都在下标靠后的VkSubmitInfo中所记录的所有命令之前。

3.在同一个VkSubmitInfo中，填入了一个或多个CommandBuffer，这些CommandBuffer中的命令的提交顺序为按照这些CommandBuffer的下标顺序，类似2中的顺序。

4.在同一个CommandBuffer中，所记录的命令分为两种：
一是不在RenderPass中的命令，即除去所有在vkCmdBeginRenderPass和vkCmdEndRenderPass之间的命令，这些命令的提交顺序为按照在CPU上写入CommandBuffer时的顺序。
二是在RenderPass中的命令，在RenderPass中的命令，只定义在同一SubPass中的其他命令的提交顺序，这些命令的提交顺序也是按照在CPU上写入CommandBuffer时的顺序。注意，如果几个命令在vkCmdBeginRenderPass和vkCmdEndRenderPass之间，但是它们不在同一SubPass中，那么它们之间是不存在任何提交顺序的。

二、Vulkan中的各个同步

1、Fence
Fence用于同步渲染队列和CPU之间的同步，它有两种状态——signaled和unsignaled。
在创建Fence时可以指定它的初始状态，如unsignaled；
在调用vkQueueSubmit时，可以传入一个Fence，这样当Queue中的所有命令都被完成以后，Fence就会被设置成signaled的状态；
通过调用vKResetFences可以让一个Fence恢复成unsignaled的状态；

```c++
VkResult vkQueueSubmit(
    VkQueue                                     queue,
    uint32_t                                    submitCount,
    const VkSubmitInfo*                         pSubmits,
    VkFence                                     fence);

```


vkWaitForFences会让CPU在当前位置被阻塞掉，然后一直等待到它接受的Fence变为signaled的状态，这样就可以实现在某个渲染队列内的所有任务被完成后，CPU再执行某些操作的同步情景。

举一个具体的例子：假如现在SwapChain中一共有3个Image，然后创建了3个CommandBuffer分别代表在渲染到相应Image时所需要执行的所有命令。在每一帧渲染时，我们需要获取当前需要渲染到的Image的编号，然后使用对应的CommandBuffer，传入渲染队列中，执行渲染命令。那么现在就有一个问题，一个CommandBuffer，如果它还没有被执行完全，那么它是不能够再次被开始执行的。也就是说上面所说的那个获取CommandBuffer后，把它传入渲染队列执行的这样一个CPU上的操作一定要在这个CommandBuffer在上一次被执行完全以后才可以执行。所以这里就遇到了一个渲染队列和CPU之间的一个同步情景，此时可以对每个CommandBuffer分别设置一个Fence来实现这样的一种同步，大体的实现如下（这里用到了Semaphore，不过可以先只关注Fence）：


	void draw()
	{
		// Get next image in the swap chain (back/front buffer)
		VK_CHECK_RESULT(swapChain.acquireNextImage(presentCompleteSemaphore, &currentBuffer));
	 
		// Use a fence to wait until the command buffer has finished execution before using it again
		VK_CHECK_RESULT(vkWaitForFences(device, 1, &waitFences[currentBuffer], VK_TRUE, UINT64_MAX));
		VK_CHECK_RESULT(vkResetFences(device, 1, &waitFences[currentBuffer]));
	 
		// Pipeline stage at which the queue submission will wait (via pWaitSemaphores)
		VkPipelineStageFlags waitStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
		// The submit info structure specifices a command buffer queue submission batch
		VkSubmitInfo submitInfo = {};
		submitInfo.sType = VK_STRUCTURE_TYPE_SUBMIT_INFO;
		submitInfo.pWaitDstStageMask = &waitStageMask;									// Pointer to the list of pipeline stages that the semaphore waits will occur at
		submitInfo.pWaitSemaphores = &presentCompleteSemaphore;							// Semaphore(s) to wait upon before the submitted command buffer starts executing
		submitInfo.waitSemaphoreCount = 1;												// One wait semaphore																				
		submitInfo.pSignalSemaphores = &renderCompleteSemaphore;						// Semaphore(s) to be signaled when command buffers have completed
		submitInfo.signalSemaphoreCount = 1;											// One signal semaphore
		submitInfo.pCommandBuffers = &drawCmdBuffers[currentBuffer];					// Command buffers(s) to execute in this batch (submission)
		submitInfo.commandBufferCount = 1;												// One command buffer
	 
		// Submit to the graphics queue passing a wait fence
		VK_CHECK_RESULT(vkQueueSubmit(queue, 1, &submitInfo, waitFences[currentBuffer]));
		
		// Present the current buffer to the swap chain
		// Pass the semaphore signaled by the command buffer submission from the submit info as the wait semaphore for swap chain presentation
		// This ensures that the image is not presented to the windowing system until all commands have been submitted
		VK_CHECK_RESULT(swapChain.queuePresent(queue, currentBuffer, renderCompleteSemaphore));
	}

由此可见Fence是一种比较粗粒度的同步原语，另一个需要关心的问题是：上面只提到了它在操作执行方面的同步，而Vulkan中非常重要的另一种——内存数据的同步，Fence能不能做到呢？

事实上，Fence也具备这种内存数据同步的功能，但是并不需要手动地指定，在使用Fence时，如果它一旦被signaled，那么使用这个Fence的Queue中的所有的命令如果涉及到了对内存的修改，那么这些Memory Access就一定会再signaled之前在Device上变得available（注意只是在Device上有这个效果，如果在CPU上读相关的内存数据，并不能保证读到的是最新的值，所以如果确保CPU也能够获取最新的值的话，就需要再用上其他的同步原语）。

一句话总结，Fence提供了一种粗粒度的，从Device向Host单向传递信息的机制。Host可以使用Fence来查询通过vkQueueSubmit/vkQueueBindSparse所提交的操作是否完成。简言之，在vkQueueSubmit/vkQueueBindSparse的时候，可以附加带上一个Fence对象。之后就可以使用这个对象来查询之前提交的状态了。

当使用vkCreateFence创建fence对象的时候，如果在标志位上填充了VkFenceCreateFlagBits(3)的VK_FENCE_CREATE_SIGNALED_BIT，那么创建出来的fence就是signaled状态，否则都是unsignaled状态的。销毁一个fence对象需要使用vkDestroyFence(3)。

伴随着vkQueueSubmit/vkQueueBindSparse一起提交的fence对象，可以使用vkGetFenceStatus(3)来查询fence的状态。注意vkGetFenceStatus是非阻塞的，如果fence处于signaled状态，这个API返回VK_SUCCESS，否则，立即返回VK_NOT_READY。

等待一个fence，除了使用vkGetFenceStatus轮询之外，还有一个API vkWaitForFences(3)提供了阻塞式地查询方法。这个API可以等待一组fence对象，直到其中至少一个，或者所有的fence都处于signaled状态，或者超时（时间限制由参数给出），才会返回。如果超时的时间设置为0，则这个API简单地看一下是否满足前两个条件，然后根据情况选择返回VK_SUCCESS，或者（虽然没有任何等待）VK_TIMEOUT。

简而言之，对于一个fence对象，Device会将其从unsignaled转到signaled状态，告诉Host一些工作已经完成。所以fence使用在Host/Device之间的，且是一种比较粗粒度的同步机制。

 

2、Semaphore
Semaphore用于渲染队列每次提交的一批命令（batch）之间的同步，和Fence一样，它也有两种状态：signaled和unsignaled。
调用vkQueueSubmit提交命令时，会填充VkSubmitInfo结构，而这个结构体中需要填入pWaitSemaphores、pSignalSemaphores、pWaitDstStageMask，表示此次提交的所有命令在执行到pWaitDstStageMask时，要停下，必须要等待pWaitSemaphores所指向的所有Semaphore的状态变成signaled时才可以继续执行，此次提交的所有命令结束以后，pSignalSemaphores所指向的所有Semaphore的状态都会被设置成signaled。

可以看到Fence和Semaphore都会在vkQueueSubmit时作为参数传入，不同之处是，Fence用于阻塞CPU直到Queue中的命令执行结束（GPU、CPU之间的同步），而Semaphore用于不同的命令提交之间的同步（GPU、GPU之间的同步）。

在Fence中给出的那段代码中，使用了两个Semaphore，用于控制queue提交的present命令（注意swapChain.queuePresent()的实现也是通过queue提交了一个执行present的命令）和render命令之间的同步：在渲染时，需要将渲染的结果写入到ColorAttachment中，我们必须要等待上一次把这个ColorAttachment给present到屏幕上的命令结束以后，才可以完成这个写入操作；
并且，将当前帧渲染结果显示到屏幕上的这个present命令，必须要等到当前帧的render命令完全执行结束以后，才可以开始执行。

和Fence一样，Semaphore也是一种粗粒度的同步，它本身也提供了隐含的内存数据的同步：
1.当让一个semaphore变成signaled时：semaphore之前的所有命令涉及到的内存写操作，都会在semaphore变成signaled之前，达到available的状态
2.当等待一个semaphore变成signaled时：在semaphore变成signaled之后，所有暂停的命令被重新唤醒继续执行之前，所有此后相关的Memory Access，都会达到visible的状态。
也就是说在使用Fence和Semaphore时，一般是不需要对GPU上有关的任何Memory Access做同步处理，这些都会被自动完成。但是，这些隐含的同步只是针对GPU的，CPU上所需要的内存数据同步操作必须由应用程序显式完成，比如：当CPU需要读一个经由GPU修改过的内存数据，就需要加一个MemoryBarrier来确保CPU读到的是最新的数据。

还有一点值得注意的是，在讨论Fence和Semaphore时，都提到了vkQueueSubmit函数，这个函数本身也是隐含了一个内存数据的同步的：就是CPU上所有的内存修改操作，都会在GPU读写之前，对GPU而言变成available的，并且对于所有之后GPU上的MemoryAccess，它们都是visible的。

VkSemaphore(3)用以同步不同的queue之间，或者同一个queue不同的submission之间的执行顺序。类似于fence，semaphore也有signaled和unsignaled的状态之分。然而由于在queue之间或者内部做同步都是device自己控制，所以一个semaphore的初始状态也就不重要了。所以，vkCreateSemaphore(3)就简单地不用任何额外参数创建一个semaphore对象，然后vkDestroySemaphore(3)可以用来销毁一个semaphore对象。不同于fence,没有重置或者等待semaphore的api，因为semaphore只对device（gpu）有效。
