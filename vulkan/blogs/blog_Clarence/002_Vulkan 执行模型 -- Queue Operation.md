# Vulkan 执行模型 -- Queue Operation

Vulkan 里的queues的作用是---提供了一个访问设备执行引擎(execution engines)的接口。

需要发送给(execution engines)的command，在真正执行之前会被recorded到command buffers中，然后会被submit到queue中等待被最终执行。一旦待执行的指令被submit到queue中，指令将会立即开始执行直到结束，在这个过程中应用程序无法干预，至于queue中储存的指令的执行顺序，他是会依赖于一些 显式和隐式的约束。简而言之，[vulkan](https://zhida.zhihu.com/search?content_id=224764375&content_type=Article&match_order=1&q=vulkan&zhida_source=entity)是会一股脑的将待执行的指令放到command buffer容器中，最终submit到queue中完成执行的，执行顺序依照一定的规则。

“需要做哪些工作”这些信息是通过传递到queues中完成的，这个过程会使用 queue submission commands相关的数据类型(大多数是以vkQueue*打头)。

queue中可以插入一系列的semaphores，实现在执行某个工作前等待某个[semaphore](https://zhida.zhihu.com/search?content_id=224764375&content_type=Article&match_order=2&q=semaphore&zhida_source=entity)，以及完成某项工作后置起某个semaphore，这些信号操作实际上都是queue相关的操作。

一旦将装载有“需要完成的工作”信息的command buffer放入queue ，这次提交就完成了，但是注意，提交完成不代表工作已经做完了。提交操作将会立刻返回，而不等待工作完成。

在不同的queue之间，或者queue和host之间是没有隐式的顺序约束的，也就是两个queue同时得到command buffer，但是他们之间的运行顺序是没有一定的规范约束的，如果想要清晰的控制queue的执行顺序，可以通过semaphores和fences来实现。（Explicit ordering constraints between different queues or with the host can be expressed with semaphores and fences.）

当command buffer会被提交到单独的queue中，那么执行的顺序应当遵守 submisson order 以及 其他的implicit ordering guarantee 规范，否则执行顺序将会未知。有些queue是可以完全不需要注意command顺序的（例如spares memory binding ）, 如果非要控制执行顺序的话，可以使用semaphores以及fences。

虽然指令执行的顺序经常是无序的，但是vulkan可以保证的点

1. 当fence以及semaphore被置起，那么这个fench以及semaphore之前被提交的command一定是执行完成的，并且如果其中有写memory的操作，那么一定也写完了。后面的指令可以放心的使用已经被正确写入的内存。
2. 等待fence以及semaphore被置起，可以保证之前的写操作是有效的，写操作后的结果对后续等到[信号量](https://zhida.zhihu.com/search?content_id=224764375&content_type=Article&match_order=1&q=信号量&zhida_source=entity)之后的指令是可见的。

command buffer之间的边界，无论是两个同样是command buffer 的 primary command [buffers](https://zhida.zhihu.com/search?content_id=224764375&content_type=Article&match_order=2&q=buffers&zhida_source=entity)，还是一个command buffer的primary command buffer和secondary command buffer，当他们被提交到queue之后，他们之间的边界并不会约束指令的执行顺序。换句话说，不管分几次将不同的command buffer放入queue中，他都相当于一次command buffer提交的过程，并没有任何区别，除非每次command buffer的末尾指令中包含了reset指令。

command将会被recorded到command buffers中，可以实现诸多的功能，可以执行某个actions或者设置一个状态，或者和其他的command保持同步，或者间接启动其他command