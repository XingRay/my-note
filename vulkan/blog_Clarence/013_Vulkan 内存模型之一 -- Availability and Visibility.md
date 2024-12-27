# Vulkan: 内存模型之一 -- Availability and Visibility

## **前言**

GPU多个核心同时执行多个程序的时候，资源的[内存管理](https://zhida.zhihu.com/search?content_id=235797250&content_type=Article&match_order=1&q=内存管理&zhida_source=entity)和同步变得尤为重要，例如CORE1和CORE2同时执行计算，但是存在CORE2的程序会使用CORE1的程序运行结果，那么就必然会产生同步的问题，需要CORE1运行在CORE2之前，并且当CORE2真正使用CORE1的结果的时候，必须保证这段数据不在cache里面，避免拿的时候，拿到的不是CORE1的正确结果，导致绘制出错。

当CORE1和CORE2不在一个GPC domain的时候，同步问题尤其麻烦，因为一个GPC domain可以依靠L1 cache来共享数据，但是如果core在不同的domain的话，就需要依靠[L2 cache](https://zhida.zhihu.com/search?content_id=235797250&content_type=Article&match_order=1&q=L2+cache&zhida_source=entity)来传递数据。在这种情况下，如果CORE2想正确拿到CORE1的运算结果，就需要CORE1将cache0->cache1->cache2 逐级写传递最新数据，然后，保证CORE2读的时候 cache2->cache1->cache0 逐级读到最新的数据（保证每次读取cache的结果都是miss，触发向上一级cache更新数据）。

## Availability and Visibility

Availability and Visibility是写操作的一种状态，指示这次写操作对于系统的渗透程度（how far the write has permeated the system）。其实也就是指示，哪些agents和references可以观察到这一笔写入。

Availability state is per **memory domain**.

Visibility state is per **(agent,reference)pair.**

Availability and Visibility states are **per-memory location** for each write.

### 先理清memory domain的含义

memory domain的名字是由 对该memory domain进行实际访问的agent来决定的。

> Domains used by shader invocations are organized hierarchically into multiple smaller memory domains which correspond to the different scopes. Each memory domain is considered the dual of a scope, and vice versa.

vulkan 定义了 **scopes** 的概念

> A scope describes a set of shader invocations, where each such set is a scope instance. Each invocation belongs to one or more scope instances, but belongs to no more than one scope instance for each scope.

**scope** 描述了一组[着色器](https://zhida.zhihu.com/search?content_id=235797250&content_type=Article&match_order=1&q=着色器&zhida_source=entity)调用的集合，一组特定的着色调用被称为scope instance。

一个scope instance包含了一组着色器调用(shader invocation)，每个着色器调用(shader invocation)可以属于一个或多个scope instance，但是对于特定的[scope](https://zhida.zhihu.com/search?content_id=235797250&content_type=Article&match_order=13&q=scope&zhida_source=entity)，每个着色器调用只能属于一个scope instance。

回到原来的文本

Domains used by shader invocations are organized hierarchically into multiple smaller memory domains which correspond to the different **scopes**.

着色器调用产生的memory domain 将会被组织为分级的模式，每个层级都会由多个更小的memory domains去管理。这个更小的memory domain是和scopes对应的。

Each memory domain is considered the **dual of a scope**, and vice versa.

Vulkan 定义了如下几种memory domains

1. host - accessible by host agents
2. device - accessible by all device agents for a particular device
3. shader - accessible by shader agents for a particular device, corresponding to the Device scope
4. ...更加复杂的memory domain暂不提及

host 也就是host可以访问的memory domain，一般来说Host也就是CPU.device 是GPU，shader是着色器。

> The memory domains are nested in the order listed above, except for shader call instance domain, with memory domains later in the list nested in the domains earlier in the list. The shader call instance domain is at an implementation-dependent location in the list, and is nested according to that location. The shader call instance domain is not broader than the queue family instance domain.

memory domain是用来管理内存[访问权限](https://zhida.zhihu.com/search?content_id=235797250&content_type=Article&match_order=1&q=访问权限&zhida_source=entity)以及同步的概念，它决定了不同的操作(例如着色器调用)可以访问哪些内存区域，以及这些访问的顺序和同步规则。

memory domain是存在嵌套的，按照上述的顺序进行嵌套，着色器调用比较特殊，由具体实现决定。

### 回到 Availability and Visibility

汇总了三个概念，分别是Availability operation ，Visibility operation， memory domain operations

这三个概念的共同点是，“alter the state of the write operations that happen-before them, and which are included in their source scope to be available or visible to their destination scope.”

核心观点是，它们改变了在它们之前（下发Availability/Visibility/memory domain operation的事件之前）发生的写操作的状态，这些写操作包含在它们的源作用域（source scope）中，使得这些写操作的结果对于后续的操作在目标作用域（destination scope）中是可用或可见的。换句话说，这些操作确保了写操作的同步和顺序性，以便于在图形渲染中正确地管理内存访问和操作的顺序。

> • For an availability operation, the source scope is a set of (agent,reference,memory location) tuples, and the destination scope is a set of memory domains.
> • For a memory domain operation, the source scope is a memory domain and the destination scope is a memory domain.
> • For a visibility operation, the source scope is a set of memory domains and the destination scope is a set of (agent,reference,memory location) tuples.

上文解释了不同的operation,source scope和destination scope分别是什么。

有了这些定义之后，memory dependency做了什么事情就非常清晰了

### memory dependency

在两组[ops](https://zhida.zhihu.com/search?content_id=235797250&content_type=Article&match_order=1&q=ops&zhida_source=entity)之间，如果插入了memory dependency，那么将会做下述三件事情，并且保证这三件事情按顺序执行。

1. 第一组operations，将会在availability operation之前完成。
2. availability operation将会在visibility operation之前完成
3. visibility operation将会在第二组operations之前完成

这样就是先了第一组ops想要传给第二组ops的img正确完成同步。avaliable也就是flush cache，visibility就是invalidate cache。这样依赖cache里的数据就正确对齐了。