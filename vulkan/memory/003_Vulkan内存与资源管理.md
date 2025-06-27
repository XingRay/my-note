# Vulkan内存与资源管理

在OpenGL中，当我们创建resource的时候，内存会被自动分配。

不同于OpenGL，vulkan是更加底层的API，需要显式的内存管理。显式的内存管理可以在资源复用与特定平台的优化方面带来好处。

# 1.Vulkan 内存分类

Vulkan内存分为2类：Host memory和Device memory。

- Device memory：指显存(GPU可直接访问，速度快)
- Host memory：指系统内存，即CPU可以直接访问的内存。

## 1.1 Host Memory

Host Memory是vulkan implementation需要的，对device不可见的存储。这类memory用来存储vulkan object的状态以及实现。

Vulkan为application提供了代替vulkan实现来进行host memory allocation的选项。如果该feature没有被使用到，vulkan实现将会使用自己的memory allocation方法。鉴于大多数memory allocation都不在关键路径上，因此这个feature并不是一个performance feature。相对的，这对于嵌入式系统中的debug或者memory allocation logging等目的是挺有用的。

Application可以把Allocators以指针的方式提交给VkAllocationCallbacks结构体：

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
// Provided by VK_VERSION_1_0
typedef struct VkAllocationCallbacks {
    void*                                   pUserData;
    PFN_vkAllocationFunction                pfnAllocation;
    PFN_vkReallocationFunction              pfnReallocation;
    PFN_vkFreeFunction                      pfnFree;
    PFN_vkInternalAllocationNotification    pfnInternalAllocation;
    PFN_vkInternalFreeNotification          pfnInternalFree;
} VkAllocationCallbacks;
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

## 1.2 Device Memory

Device Memory是device(GPU)可见的memory，即显存。举例：image的内容或者buffer对象，这些可以被device原生地(natively)使用。

Device Memory Properties

Physical device的memory properties描述了memory heaps和可用的memory类型。**对于不同的场景，我们需要指定不同的memory properties来使得内存访问效率最大化，后文会介绍不同memory property的特点。**

可以通过调用下列函数查询memory properties:

```
// Provided by VK_VERSION_1_0
void vkGetPhysicalDeviceMemoryProperties(
    VkPhysicalDevice                            physicalDevice,
    VkPhysicalDeviceMemoryProperties*           pMemoryProperties);
```

- physicalDevice是要查询的device的handle
- pMemoryProperties里是返回的properties

VkPhysicalDeviceMemoryProperties结构提定义：

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
// Provided by VK_VERSION_1_0
typedef struct VkPhysicalDeviceMemoryProperties {
    uint32_t        memoryTypeCount;
    VkMemoryType    memoryTypes[VK_MAX_MEMORY_TYPES];
    uint32_t        memoryHeapCount;
    VkMemoryHeap    memoryHeaps[VK_MAX_MEMORY_HEAPS];
} VkPhysicalDeviceMemoryProperties;
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

- memoryTypeCount: memoryTypes数组中有效元素的个数。
- memoryTypes: 描述了用来访问堆分配内存的memory类型。
- memoryHeapCount: memoryHeaps中有效元素的个数
- memoryHeaps: 描述可以被分配的memory heaps的数组

VkPhysicalDeviceMemoryProperties结构描述了多个内存堆以及可用于访问在这些堆中分配的内存的多个内存类型。每个堆描述特定大小的内存资源，每个内存类型描述可与给定内存堆一起使用的一组内存属性（例如，host cached vs uncached）。使用特定内存类型的分配将消耗该内存类型的堆索引所指示的堆中的资源。多于一种的内存类型可以共享每个堆，并且堆和存内存类型提供了一种机制来通告物理内存资源的准确大小，同时允许内存以各种不同的属性一起使用。

至少有一个堆必须在VkMemoryHeap:：flags中包括VK_MEMORY_heap_DEVICE_LOCAL_BIT。如果有多个堆都具有相似的性能特性，则它们可以都包括VK_MEMORY_HEAP_DEVICE_LOCAL_BIT。在统一内存体系结构（UMA）系统中，通常只有一个内存堆，它被认为是host和device的同等“本地”内存堆，并且这样的实现必须将该堆宣传为设备本地内存堆。

 

# 2. Memory Heaps与types

Heaps: Physical memory，描述了memory的物理属性，Heap限制了Type flags的值

Types：描述了内存的属性

## 2.1 常用Memory Heaps解析

Device 端的内存堆及其内存类型可以通过vkGetPhysicalDeviceMemoryProperties 查询 以AMD 独立显卡为例，可查询的堆类型如下

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
//Heap 0
VK_MEMORY_HEAP_DEVICE_LOCAL_BIT
代表GPU 设备上的显存，并且不能被映射到CPU 端
MemoryType0
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
GPU 读写以及原子操作速度最快
不能用vkMapMemory 映射到CPU 端

//Heap 1
VK_MEMORY_HEAP_DEVICE_LOCAL_BIT
代表GPU 设备上且被CPU访问的显存 （通常来说GPU 会预留一小部分显存直接供CPU 访问）
MemoryType1
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
可以通过vkMapMemory访问
GPU 读写速度及原子操作速度最快
CPU 端访问是uncache，CPU写入是write-combine，读取时uncache模式

//Heap 2
表示主机系统内存，并可以被GPU访问
可以使用vkMapMemory
GPU 读取buffer 和 texture 通常被GPU L2 缓存
GPU L2 missing 会导致PCIE 读 系统内存
GPU L2 missing 延迟较高
MemoryType2
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
CPU 写是write combine，读是uncache
MemoryType3
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
VK_MEMORY_PROPERTY_HOST_CACHED_BIT
CPU 读写可以通过 CPU cache
GPU 读通过snoop CPU cache，snoop 是一种硬件管理的Cache 一致性处理方式
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

 

## 2.2 常用Memory Types解析

- **VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT**：表示以此flag分配的内存，对device access来说是最高效的。需要注意的是仅当heap的flag为VK_MEMORY_HEAP_DEVICE_LOCAL_BIT时才可以设置，Device-Local Memory就是指显存；

由显存分配，可以被GPU快速访问，不能直接被CPU访问，内存Map也不行；

GPU读/写都是最快的；

适用场景为CPU写一次，GPU经常读/写的场景。

由于Device-Local Memory无法被CPU访问，因此CPU写时需要先写到一个Host可见的Memory，然后再通过指令拷贝到对应的Device Memory。我们一般把这个持有Host可见的Memory的buffer称为Staging Buffer。关于staging buffer的介绍和使用可以参考：[Staging Buffer](https://link.zhihu.com/?target=https%3A//vulkan-tutorial.com/Vertex_buffers/Staging_buffer)

- **VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT**：表示以此flag分配的内存，可以通过调用vkMapMemory函数map给CPU访问。

由系统内存分配，可以Map，可以同时被CPU和GPU访问（kernel中应该进行了实现，即map的方式），显而易见的是GPU的访问比较慢(需要通过总线)

适用场景为CPU写数据(多次)，GPU读一次(仅读一次，不用担心CPU写数据的过程中，GPU Cache可能带来的数据污染)；或者大量数据被GPU读取。

- **VK_MEMORY_PROPERTY_HOST_COHERENT_BIT**：该标志位表示CPU的write数据flush给GPU时，无需调用vkFlushMappedMemoryRanges；GPU的write的数据想要对CPU可见时，无需调用vkInvalidateMappedMemoryRanges。

解析：

简化模型：

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
+-------------------------+               +-----------------------+
|  GPU |GPUCache/LocalMemory |               | HostMemory/CPUCache |CPU |
|      |                  |               |                  |    |
+-------------------------+               +-----------------------+
       |                                                     |
       |                      PCIE bus                       |
       +-----------------------------------------------------+
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

有Cache 就存在cache 不一致问题。这里考虑的都是CPU Memory的cache一致性问题。对于GPU cache如何操作去解决一致性问题，并未提及。

CPU的write数据是暂存在CPU cache中，如果要刷给GPU，就需要手动调用vkFlushMappedMemoryRanges使数据从CPU Cache刷到memory中，这样GPU才能看到这个数据。

GPU的write数据是暂存在GPU cache中，刷到memory中如果要被CPU看到，则需要invalidate CPU cache，就是让CPU cache中相对应缓存的对应数据失效，只有这样CPU才会从memory中取拿最新的数据；并且可以避免CPU cache中的过时数据被挤到memory中。

这里提到的vkFlush和vkInvalidate都是对CPU cache的操作。没提到的GPU cache的flush和invalidate，是GPU自己控制的，一般是flush会被自动加入到command line中，并且flush后会隐式调用invalidate。

- **VK_MEMORY_PROPERTY_HOST_CACHED_BIT**：该标志位表示以此flag分配的内存是CPU cached的；CPU对uncached memory的访问速度要比cached memory要慢；然而uncached memory总是host coherent的，这个也很好理解，没有CPU cache，写和读都会直接从memory里操作，也就无需再调用vkFlushMappedMemoryRanges和vkInvalidateMappedMemoryRanges。
- **VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT** and **VK_MEMORY_PROPERTY_HOST_CACHED_BIT**：兼顾两种属性，即具备cpu cache，同时可以被CPU和GPU访问，适合GPU写，CPU读取。

## 2.3 vulkan memory property Flags

vulkan propertyFlags有如下值

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
0
 
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
 
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_CACHED_BIT
 
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_CACHED_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_CACHED_BIT
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_CACHED_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_LAZILY_ALLOCATED_BIT
 
VK_MEMORY_PROPERTY_PROTECTED_BIT
 
VK_MEMORY_PROPERTY_PROTECTED_BIT | VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
 
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD
 
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_CACHED_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_CACHED_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD
 
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD |
VK_MEMORY_PROPERTY_DEVICE_UNCACHED_BIT_AMD
 
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_CACHED_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD |
VK_MEMORY_PROPERTY_DEVICE_UNCACHED_BIT_AMD
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD |
VK_MEMORY_PROPERTY_DEVICE_UNCACHED_BIT_AMD
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD |
VK_MEMORY_PROPERTY_DEVICE_UNCACHED_BIT_AMD
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT |
VK_MEMORY_PROPERTY_HOST_CACHED_BIT |
VK_MEMORY_PROPERTY_HOST_COHERENT_BIT |
VK_MEMORY_PROPERTY_DEVICE_COHERENT_BIT_AMD |
VK_MEMORY_PROPERTY_DEVICE_UNCACHED_BIT_AMD
 
VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT |
VK_MEMORY_PROPERTY_RDMA_CAPABLE_BIT_NV
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

# 3. Device Memory的分配与申请流程

## 3.1 Memory Support查询

编程实践中，第一步就是查看我们需要的内存类型是否时当前硬件/软件环境支持的，Application通过如下方式查找所需的内存类型是否在支持列表中：

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
// Find a memory in `memoryTypeBitsRequirement` that includes all of `requiredProperties`
int32_t findProperties(const VkPhysicalDeviceMemoryProperties* pMemoryProperties,
                       uint32_t memoryTypeBitsRequirement,
                       VkMemoryPropertyFlags requiredProperties) {
    const uint32_t memoryCount = pMemoryProperties->memoryTypeCount;
    for (uint32_t memoryIndex = 0; memoryIndex < memoryCount; ++memoryIndex) {
        const uint32_t memoryTypeBits = (1 << memoryIndex);
        const bool isRequiredMemoryType = memoryTypeBitsRequirement & memoryTypeBits;
 
        const VkMemoryPropertyFlags properties =
            pMemoryProperties->memoryTypes[memoryIndex].propertyFlags;
        const bool hasRequiredProperties =
            (properties & requiredProperties) == requiredProperties;
 
        if (isRequiredMemoryType && hasRequiredProperties)
            return static_cast<int32_t>(memoryIndex);
    }
 
    // failed to find memory type
    return -1;
}
 
// Try to find an optimal memory type, or if it does not exist try fallback memory type
// `device` is the VkDevice
// `image` is the VkImage that requires memory to be bound
// `memoryProperties` properties as returned by vkGetPhysicalDeviceMemoryProperties
// `requiredProperties` are the property flags that must be present
// `optimalProperties` are the property flags that are preferred by the application
VkMemoryRequirements memoryRequirements;
vkGetImageMemoryRequirements(device, image, &memoryRequirements);
int32_t memoryType =
    findProperties(&memoryProperties, memoryRequirements.memoryTypeBits, optimalProperties);
if (memoryType == -1) // not found; try fallback properties
    memoryType =
        findProperties(&memoryProperties, memoryRequirements.memoryTypeBits, requiredProperties);
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

## 3.2 Memory Allocation

### 3.2.1 Direct Device Memory Allocation

allocate memory objects时，调用

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
// Provided by VK_VERSION_1_0
VkResult vkAllocateMemory(
    VkDevice                                    device,
    const VkMemoryAllocateInfo*                 pAllocateInfo,
    const VkAllocationCallbacks*                pAllocator,
    VkDeviceMemory*                             pMemory);

//device is the logical device that owns the memory.
//pAllocateInfo is a pointer to a VkMemoryAllocateInfo structure describing parameters of the allocation. A successfully returned allocation must use the requested parameters — no substitution is permitted by the implementation.
//pAllocator controls host memory allocation as described in the Memory Allocation chapter.
//pMemory is a pointer to a VkDeviceMemory handle in which information about the allocated memory is returned.

//The VkMemoryAllocateInfo structure 定义:

// Provided by VK_VERSION_1_0
typedef struct VkMemoryAllocateInfo {
    VkStructureType    sType;
    const void*        pNext;
    VkDeviceSize       allocationSize;
    uint32_t           memoryTypeIndex;
} VkMemoryAllocateInfo;

//sType is a VkStructureType value identifying this structure.
//pNext is NULL or a pointer to a structure extending this structure.
//allocationSize is the size of the allocation in bytes.
//memoryTypeIndex is an index identifying a memory type from the memoryTypes array of the VkPhysicalDeviceMemoryProperties structure.
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

### 3.2.2 Android Hardware Buffer External Memory

当我们需要从在vulkan instance之外创建的Android hardware buffer来import memory时，可以在[VkMemoryAllocateInfo](https://docs.vulkan.org/spec/latest/chapters/memory.html#VkMemoryAllocateInfo)结构体的pNext加一个`VkImportAndroidHardwareBufferInfoANDROID`结构体，定义如下：

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
// Provided by VK_ANDROID_external_memory_android_hardware_buffer
typedef struct VkImportAndroidHardwareBufferInfoANDROID {
    VkStructureType            sType;
    const void*                pNext;
    struct AHardwareBuffer*    buffer;
} VkImportAndroidHardwareBufferInfoANDROID;


//sType is a VkStructureType value identifying this structure.
//pNext is NULL or a pointer to a structure extending this structure.
//buffer is the Android hardware buffer to import.

当需要把一个vulkan device memory对象声明为Android hardware buffer引用时，调用

// Provided by VK_ANDROID_external_memory_android_hardware_buffer
VkResult vkGetMemoryAndroidHardwareBufferANDROID(
    VkDevice                                    device,
    const VkMemoryGetAndroidHardwareBufferInfoANDROID* pInfo,
    struct AHardwareBuffer**                    pBuffer);


//device is the logical device that created the device memory being exported.
//pInfo is a pointer to a VkMemoryGetAndroidHardwareBufferInfoANDROID structure containing parameters of the export operation.
//pBuffer will return an Android hardware buffer referencing the payload of the device memory object.
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

 

## 3.3 Vulkan Memory申请流程

Step1: 获取physical device支持的memory属性

Step2:根据申请的resource的类型获取这块memory的requirements

Step3: 遍历Step1与Step2的结果，看当前physical device是否支持当前resource的memory requirement

Step4: 根据memory requirement设置memory allocation info

Step5: 调用vkAllocateMemory申请内存

Step6: 调用vkBindImageMemory把申请的内存memory和resource image绑定在一起

 

# 4. VkMemoryPropertyFlagBits描述

https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkMemoryPropertyFlagBits.html

 

# 5. Vulkan的缓冲(Buffer)与图像(Image)

解决了内存分配的问题，但目前仍还有一个巨大的问题等待着我们去解决：GPU绘制需要各种资源，但资源通常是存储在CPU内存中的，和GPU内存并不互通，无法被GPU直接访问，因此我们需要一个方法把资源放到GPU内存中而且能被GPU按照一定的规矩访问，而不是乱来，那么接下来我们就来解决这个问题。

Vulkan为我们提供了两种不同的资源类型，分别是缓冲（Buffer）和图像（Image），这两个都是vulkan中的resource。在利用相应的vulkan API 创建完VkBuffer或者VkImage之后，就可以遵循上文3.3 Vulkan Memory申请流程进行memory的申请和resource/memory绑定了。

 

## 参考链接

- Vulkan Memory Management：https://www.youtube.com/watch?v=gM93bbKQ0P8
- https://vulkan-tutorial.com/Vertex_buffers/Vertex_buffer_creation
- 从零开始的Vulkan（三）：资源与内存管理 https://zhuanlan.zhihu.com/p/537142901?utm_id=0
- Vulkan内存属性解析 https://zhuanlan.zhihu.com/p/527481097