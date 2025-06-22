# 从零开始的Vulkan（三）：资源与内存管理

在之前使用[Vulkan API](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=Vulkan+API&zhida_source=entity)的过程中，VkAllocationCallbacks的身影频繁出现，但是我并未对它作出解释，它实际上是使用Vulkan进行CPU内存管理的一个工具，这里我们就来好好研究一下。

## [内存管理](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=2&q=内存管理&zhida_source=entity)

Vulkan中的内存分为两种：主机内存和设备内存。

### 主机内存

主机内存是使用Vulkan过程中需要的一种设备不可见的存储，VkAllocationCallbacks就是Vulkan提供一种用于手动管理主级[内存分配](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=内存分配&zhida_source=entity)的重要工具，尽管这并不是必要的，当我们不进行指定时，Vulkan就会使用默认的分配方式。VkAllocationCallbacks的定义如下：

```cpp
typedef struct VkAllocationCallbacks {
  void* pUserData;
  PFN_vkAllocationFunction pfnAllocation;
  PFN_vkReallocationFunction pfnReallocation;
  PFN_vkFreeFunction pfnFree;
  PFN_vkInternalAllocationNotification pfnInternalAllocation;
  PFN_vkInternalFreeNotification pfnInternalFree;
} VkAllocationCallbacks;
```

pUserData是由用户自行解释的值。当VkAllocationCallbacks之中任何[回调函数](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=回调函数&zhida_source=entity)被调用，这个值都会作为第一个参数被传递给回调函数， 每一次VkAllocationCallbacks被传入命令时这个值都可以改变。除了pUserData之外其余的参数均为指定了内存行为的[函数指针](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=函数指针&zhida_source=entity)。

PFN_vkAllocationFunction是一个指向应用程序定义的内存[分配函数](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=分配函数&zhida_source=entity)的指针：

```cpp
typedef void* (VKAPI_PTR *PFN_vkAllocationFunction)(
  void* pUserData,
  size_t size,
  size_t alignment,
  VkSystemAllocationScope allocationScope);
```

- size指定了分配内存的字节大小。
- alignment指定了内存分配的对齐方式，以字节为单位，必须是2的倍数。
- allocationScope指定了内存分配的生命周期，它的值如下所示：

```cpp
typedef enum VkSystemAllocationScope {
  VK_SYSTEM_ALLOCATION_SCOPE_COMMAND = 0,     //在Vulkan命令期间
  VK_SYSTEM_ALLOCATION_SCOPE_OBJECT = 1,      //在对象被创建和使用期间
  VK_SYSTEM_ALLOCATION_SCOPE_CACHE = 2,       //与VkPipelineCache对象关联
  VK_SYSTEM_ALLOCATION_SCOPE_DEVICE = 3,      //在Vulkan device期间
  VK_SYSTEM_ALLOCATION_SCOPE_INSTANCE = 4,    //在Vulkan instance期间
} VkSystemAllocationScope;
```

PFN_vkReallocationFunction是一个指向应用程序定义的内存重分配函数的指针：

```cpp
typedef void* (VKAPI_PTR *PFN_vkReallocationFunction)(
  void* pUserData,
  void* pOriginal,
  size_t size,
  size_t alignment,
  VkSystemAllocationScope allocationScope);
```

pOriginal必须是NULL或者是同一个内存分配器的pfnReallocation或pfnAllocation返回的指针，其余参数同上。当size被设为0时，调用vkReallocationFunction等同于释放内存；当size大于pOriginal的size时，多余的内存是未指定的。

PFN_vkFreeFunction是一个指向应用程序定义的[内存释放](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=内存释放&zhida_source=entity)函数：

```cpp
typedef void (VKAPI_PTR *PFN_vkFreeFunction)(
  void* pUserData,
  void* pMemory);
```

pMemory指向了要释放的[内存地址](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=内存地址&zhida_source=entity)。

PFN_vkInternalAllocationNotification是一个指向应用程序定义的函数的指针，当被Vulkan实现调用时，就进行内部内存分配：

```cpp
typedef void (VKAPI_PTR *PFN_vkInternalAllocationNotification)(
  void* pUserData,
  size_t size,
  VkInternalAllocationType allocationType,
  VkSystemAllocationScope allocationScope);
```

PFN_vkInternalFreeNotification是一个指向应用程序定义的函数的指针，当被Vulkan实现调用时，就释放内部内存：

```cpp
typedef void (VKAPI_PTR *PFN_vkInternalFreeNotification)(
  void* pUserData,
  size_t size,
  VkInternalAllocationType allocationType,
  VkSystemAllocationScope allocationScope);
```

allocationType的值为VK_INTERNAL_ALLOCATION_TYPE_EXECUTABLE，表示分配的内存是计划给CPU端使用的。

Vulkan为了应用程序实现的安全性，让用户去手动处理所有内存的分配是不显示的，所以Vulkan使用内部的内存分配机制时会调用vkInternalAllocationNotification和vkInternalFreeNotification函数来告知用户，以实现内存分配的透明。

### 设备内存

设备内存是一种设备可见的内存，常见的[缓冲内存](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=缓冲内存&zhida_source=entity)和图像内存，可以被设备本地使用。由于我们分配设备内存是基于物理设备上的，所以我们必须知道物理设备上的[内存堆](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=内存堆&zhida_source=entity)和可用的内存类型。用vkGetPhysicalDeviceMemoryProperties函数查询后可以得到一个VkPhysicalDeviceMemoryProperties结构体中记载了物理设备上的内存属性。VkPhysicalDeviceMemoryProperties包括flags和heaps，我们可以借此使用辅助函数来找到们需要的heap，VkSpec中就介绍了这个函数的实现方法：

```cpp
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
// Try to find an optimal memory type, or if it does not exist try fallback memory
type
// `device` is the VkDevice
// `image` is the VkImage that requires memory to be bound
// `memoryProperties` properties as returned by vkGetPhysicalDeviceMemoryProperties
// `requiredProperties` are the property flags that must be present
// `optimalProperties` are the property flags that are preferred by the application
VkMemoryRequirements memoryRequirements;
vkGetImageMemoryRequirements(device, image, &memoryRequirements);
int32_t memoryType =
  findProperties(&memoryProperties, memoryRequirements.memoryTypeBits,
optimalProperties);
if (memoryType == -1) // not found; try fallback properties
  memoryType =
  findProperties(&memoryProperties, memoryRequirements.memoryTypeBits,
requiredProperties);
```

VkMemoryPropertyFlags负责根据资源查询需要的内存属性，我们将在下文提及。但这个辅助函数太过臃肿，我这里根据需求使用了更简单的一个函数：

```cpp
bool MemoryTypeFromProperties(vk::PhysicalDeviceMemoryProperties memProp, uint32_t typeBits, vk::MemoryPropertyFlags requirementMask, uint32_t& typeIndex) {
	for (uint32_t i = 0; i < VK_MAX_MEMORY_TYPES; i++) {
		if ((typeBits & (1 << i)) && (memProp.memoryTypes[i].propertyFlags & requirementMask) == requirementMask) {
			typeIndex = i;
			return true;
		}
	}
	return false;
}
```

用vkAllocateMemory函数来分配VkDeviceMemory类型的设备内存对象，它的实现方法如下：

```cpp
VkResult vkAllocateMemory(
  VkDevice device,
  const VkMemoryAllocateInfo* pAllocateInfo,
  const VkAllocationCallbacks* pAllocator,
  VkDeviceMemory* pMemory);

typedef struct VkMemoryAllocateInfo {
  VkStructureType sType;
  const void* pNext;
  VkDeviceSize allocationSize;
  uint32_t memoryTypeIndex;
} VkMemoryAllocateInfo;
```

allocationSize指定了分配的内存大小（字节），memoryTypeIndex的值就可以用刚刚的辅助函数获取。pNext扩展指针可以填入一个VkMemoryDedicatedAllocateInfo结构体，这样就可以在分配内存的时候指定一个专用Buffer或Image对象（将在下文讲解）：

```cpp
// Provided by VK_VERSION_1_1
typedef struct VkMemoryDedicatedAllocateInfo {
  VkStructureType sType;
  const void* pNext;
  VkImage image;
  VkBuffer buffer;
} VkMemoryDedicatedAllocateInfo;
```

VkMemoryAllocateInfo的pNext扩展指针也可以填入VkExportMemoryAllocateInfo，VkExportMemoryWin32HandleInfoKHR或VkImportMemoryWin32HandleInfoKHR，并借此向外部导出或从外部导入内存，这里不作过多讲解，具体可以查阅VkSpec。

解决了内存分配的问题，但目前仍还有一个巨大的问题等待着我们去解决：GPU绘制需要各种资源，但资源通常是存储在CPU内存中的，和GPU内存并不互通，无法被GPU直接访问，因此我们需要一个方法把资源放到GPU内存中而且能被GPU按照一定的规矩访问，而不是乱来，那么接下来我们就来解决这个问题。

Vulkan为我们提供了两种不同的资源类型，分别是**缓冲（Buffer）**和**图像（Image）**，下面我们就来分别了解它们的具体使用方法。

## 缓冲

### 缓冲的创建

Buffer对象代表了一段连续的不指明格式的内存数据，它可以通过调用[命令缓冲区](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=命令缓冲区&zhida_source=entity)来绑定，交由GPU硬件操作。Vulkan中用VkBuffer句柄来指示Buffer对象，并且用以下方法进行创建：

```cpp
typedef struct VkBufferCreateInfo {
  VkStructureType sType;
  const void* pNext;
  VkBufferCreateFlags flags;
  VkDeviceSize size;
  VkBufferUsageFlags usage;
  VkSharingMode sharingMode;
  uint32_t queueFamilyIndexCount;
  const uint32_t* pQueueFamilyIndices;
} VkBufferCreateInfo;

VkResult vkCreateBuffer(
  VkDevice device,
  const VkBufferCreateInfo* pCreateInfo,
  const VkAllocationCallbacks* pAllocator,
  VkBuffer* pBuffer);
```

- flags是一个VkBufferCreateFlagBits类型的枚举量，以下关于它的说明翻自VkSpec：

| Flags                                              |                                                              |
| -------------------------------------------------- | ------------------------------------------------------------ |
| VK_BUFFER_CREATE_SPARSE_BINDING_BIT                | Buffer可以用于绑定在稀疏内存中。                             |
| VK_BUFFER_CREATE_SPARSE_RESIDENCY_BIT              | Buffer可以部分绑定在稀疏内存中（必须同时指明VK_BUFFER_CREATE_SPARSE_BINDING_BIT）。 |
| VK_BUFFER_CREATE_SPARSE_ALIASED_BIT                | Buffer绑定的稀疏内存区域可以同时被别的Buffer绑定（必须同时指明VK_BUFFER_CREATE_SPARSE_BINDING_BIT）。 |
| VK_BUFFER_CREATE_PROTECTED_BIT                     | Buffer是被保护的内存。                                       |
| VK_BUFFER_CREATE_DEVICE_ADDRESS_CAPTURE_REPLAY_BIT | Buffer的地址可以被保存并被用于随后的运行中（例如捕获和回放）。 |

- size指明了VkBuffer映射的一段区域的内存大小，即数据大小。
- usage指明了Buffer的具体功用，例如用作[顶点缓存](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=顶点缓存&zhida_source=entity)，索引缓存，转移缓存。以下摘自VkSpec：

```cpp
typedef enum VkBufferUsageFlagBits {
  VK_BUFFER_USAGE_TRANSFER_SRC_BIT = 0x00000001,
  VK_BUFFER_USAGE_TRANSFER_DST_BIT = 0x00000002,
  VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT = 0x00000004,
  VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT = 0x00000008,
  VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT = 0x00000010,
  VK_BUFFER_USAGE_STORAGE_BUFFER_BIT = 0x00000020,
  VK_BUFFER_USAGE_INDEX_BUFFER_BIT = 0x00000040,
  VK_BUFFER_USAGE_VERTEX_BUFFER_BIT = 0x00000080,
  VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT = 0x00000100,
  // Provided by VK_VERSION_1_2
  VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT = 0x00020000,
#ifdef VK_ENABLE_BETA_EXTENSIONS
  // Provided by VK_KHR_video_decode_queue
  VK_BUFFER_USAGE_VIDEO_DECODE_SRC_BIT_KHR = 0x00002000,
#endif
#ifdef VK_ENABLE_BETA_EXTENSIONS
  // Provided by VK_KHR_video_decode_queue
  VK_BUFFER_USAGE_VIDEO_DECODE_DST_BIT_KHR = 0x00004000,
#endif
  // Provided by VK_KHR_acceleration_structure
  VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_BUILD_INPUT_READ_ONLY_BIT_KHR = 0x00080000,
  // Provided by VK_KHR_acceleration_structure
  VK_BUFFER_USAGE_ACCELERATION_STRUCTURE_STORAGE_BIT_KHR = 0x00100000,
  // Provided by VK_KHR_ray_tracing_pipeline
  VK_BUFFER_USAGE_SHADER_BINDING_TABLE_BIT_KHR = 0x00000400,
#ifdef VK_ENABLE_BETA_EXTENSIONS
  // Provided by VK_KHR_video_encode_queue
  VK_BUFFER_USAGE_VIDEO_ENCODE_DST_BIT_KHR = 0x00008000,
#endif
#ifdef VK_ENABLE_BETA_EXTENSIONS
  // Provided by VK_KHR_video_encode_queue
  VK_BUFFER_USAGE_VIDEO_ENCODE_SRC_BIT_KHR = 0x00010000,
#endif
  // Provided by VK_KHR_buffer_device_address
  VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT_KHR =
VK_BUFFER_USAGE_SHADER_DEVICE_ADDRESS_BIT,
} VkBufferUsageFlagBits;
```

- sharingMode指定了Buffer将以什么样的形式被多个队列族共享访问，这在之前的[交换链](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=交换链&zhida_source=entity)创建中已经说明过了，我们选择VK_SHARING_MODE_EXCLUSIVE。
- queueFamilyIndexCount和pQueueFamilyIndices指定了访问这个Buffer的队列族，我们就使用已有的队列族即可。
- 在Vulkan1.1版本后，pNext扩展指针允许我们使用VkExternalMemoryBufferCreateInfo结构体来指定创建一个用于后背存储的外部缓冲，该结构体中的handleTypes定义了外部缓冲的类型。外部缓冲允许跨API使用缓冲（例如使用D3D11中的IDXGIResource::GetSharedHandle，D3D12中的ID3D12Device::CreateSharedHandle），这里不作过多讲解。
- 在Vulkan1.2版本后，pNext扩展指针允许我们使用VkBufferOpaqueCaptureAddressCreateInfo结构体来为Buffer要求具体的设备地址，用一个uint64_t类型的opaqueCaptureAddress来指定这一地址，它可以通过vkGetBufferOpaqueCaptureAddress函数进行检索。

### 缓冲视图BufferView

缓冲视图允许我们以类似读取图像数据的方式读取缓冲数据。

> 注意，只有当Buffer的usage包含VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT或VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT的时候才可以为其创建缓冲视图。

用以下方式创建缓冲视图：

```cpp
typedef struct VkBufferViewCreateInfo {
  VkStructureType sType;
  const void* pNext;
  VkBufferViewCreateFlags flags;
  VkBuffer buffer;
  VkFormat format;
  VkDeviceSize offset;
  VkDeviceSize range;
} VkBufferViewCreateInfo;

VkResult vkCreateBufferView(
  VkDevice device,
  const VkBufferViewCreateInfo* pCreateInfo,
  const VkAllocationCallbacks* pAllocator,
  VkBufferView* pView);
```

- buffer指向了之前创建的VkBuffer句柄。
- format用于指定缓冲中每个元素具体的格式类型，与我们使用的数据类型保持相同。
- offset指定了最开始访问的位置在缓冲地址中的偏移值。
- range指定了访问的缓冲范围，若range是VK_WHOLE_SIZE，则使用整个缓冲。

> 如果在使用VK_WHOLE_SIZE的情况下，访问范围并非是texel block size的整数倍，则访问范围将自动减小至texel block size的整数倍值。若不使用VK_WHOLE_SIZE，那么range也必须是texel block size的整数倍。对于不同格式，texel block size的大小也不同，例如VK_FORMAT_R8_UNORM格式的texel block size是8位，而VK_FORMAT_R8G8_UNORM格式的texel block size是16位。

### 为缓冲关联内存

资源在一开始被创建时是被虚拟分配的，并不包含实际的内存，设备内存必须要被单独分配然后再被关联到资源。对于稀疏资源和非稀疏资源，关联内存的过程是不相同的，这里主要讲解非稀疏资源。

为了给缓冲创建内存，我们必须首先知道缓冲资源需要的内存需求，这里我们使用vkGetBufferMemoryRequirements函数来实现，虽然新版本的Vulkan提供了vkGetBufferMemoryRequirements2和vkGetDeviceBufferMemoryRequirements的新方法，但是没有什么特别使用的必要。

```cpp
void vkGetBufferMemoryRequirements(
  VkDevice device,
  VkBuffer buffer,
  VkMemoryRequirements* pMemoryRequirements);

typedef struct VkMemoryRequirements {
  VkDeviceSize size;
  VkDeviceSize alignment;
  uint32_t memoryTypeBits;
} VkMemoryRequirements;
```

使用之后可以得到一个VkMemoryRequirements的结构体，它告知了需要的一些内存属性：

- size指定了需要分配的内存大小。
- alignment指定了[内存对齐](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=内存对齐&zhida_source=entity)需要的位偏移量，必定是2的整数倍。
- memoryTypeBits指定了需要的内存类型的位掩码，可以依次查询可用内存堆索引。

我们将VkMemoryRequirements和上文提及的查询可用内存堆的辅助函数结合起来分配内存，如下所示：

```cpp
vk::MemoryRequirements memReqs;
device->getBufferMemoryRequirements(buffer, &memReqs);
auto memoryInfo = vk::MemoryAllocateInfo()
			.setAllocationSize(memReqs.size);
MemoryTypeFromProperties(gpuProp, memReqs.memoryTypeBits, memProp, memoryInfo.memoryTypeIndex);
device->allocateMemory(&memoryInfo, 0, &memory);
```

最后用VkBindBufferMemory函数将缓冲和内存绑定在一起，就成功完成了缓冲的创建：

```cpp
device->bindBufferMemory(buffer, memory, 0);
```

### 缓冲的映射

为了从CPU端向缓冲写入数据，我们需要将缓冲映射到CPU内存中，并用指针进行读写。映射缓冲的方法如下：

```cpp
device->mapMemory(memory, 0, elementByteSize * (uint64_t)elementCount, vk::MemoryMapFlags(), reinterpret_cast<void**>(&mappedData));
```

mapMemory函数指定了要映射的内存偏移量和内存大小（都以字节为单位），获取了相应的指针，比较容易理解不作赘述。在获取了指针后就可以用memcpy函数进行数据拷贝，之后结束映射：

```cpp
memcpy(&mappedData[elementIndex * elementByteSize], data, elementByteSize * elementCount);
device->unmapMemory(memory);
```

### Buffer辅助类

综合了以上讲解的创建和映射Buffer的方法，我们可以用一个Buffer辅助模板类来方便之后我们对缓冲的使用，模板类最大的好处就是可以套用所有的数据格式类型。Buffer辅助类的具体实现如下：

```cpp
template<typename T>
class Buffer {
public:
	Buffer(vk::Device* device, uint32_t elementCount, vk::BufferUsageFlags usage, vk::PhysicalDeviceMemoryProperties gpuProp, vk::MemoryPropertyFlags memProp, bool mapped) {
		elementByteSize = sizeof(T);
		this->mapped = mapped;

		auto bufferInfo = vk::BufferCreateInfo()
			.setSize(elementByteSize * (uint64_t)elementCount)
			.setUsage(usage);
		
		device->createBuffer(&bufferInfo, 0, &buffer);

		vk::MemoryRequirements memReqs;
		device->getBufferMemoryRequirements(buffer, &memReqs);
		
		auto memoryInfo = vk::MemoryAllocateInfo()
			.setAllocationSize(memReqs.size);

		MemoryTypeFromProperties(gpuProp, memReqs.memoryTypeBits, memProp, memoryInfo.memoryTypeIndex);

		device->allocateMemory(&memoryInfo, 0, &memory);

		device->bindBufferMemory(buffer, memory, 0);

		if (mapped)
			device->mapMemory(memory, 0, elementByteSize * (uint64_t)elementCount, vk::MemoryMapFlags(), reinterpret_cast<void**>(&mappedData));
	}

	void DestroyBuffer(vk::Device* device) {
		if (mapped)
			device->unmapMemory(memory);

		device->destroyBuffer(buffer, 0);
		device->freeMemory(memory, 0);
	}

	void CopyData(vk::Device* device, uint32_t elementIndex, uint32_t elementCount, const T* data) {
		if (!mapped)
			device->mapMemory(memory, 0, elementByteSize * (uint64_t)elementCount, vk::MemoryMapFlags(), reinterpret_cast<void**>(&mappedData));

		memcpy(&mappedData[elementIndex * elementByteSize], data, elementByteSize * elementCount);

		if (!mapped)
			device->unmapMemory(memory);
	}

	vk::Buffer GetBuffer()const {
		return buffer;
	}

private:
	vk::Buffer buffer;
	vk::DeviceMemory memory;

	bool mapped = false;

	BYTE* mappedData = nullptr;
	uint64_t elementByteSize = 0;
};
```

## 图像

### 图像的创建

我们讲完了缓冲的使用方法，图像和缓冲类似只不过存储和使用数据的方法不同。用以下方法创建Image对象：

```cpp
typedef struct VkImageCreateInfo {
  VkStructureType sType;
  const void* pNext;
  VkImageCreateFlags flags;
  VkImageType imageType;
  VkFormat format;
  VkExtent3D extent;
  uint32_t mipLevels;
  uint32_t arrayLayers;
  VkSampleCountFlagBits samples;
  VkImageTiling tiling;
  VkImageUsageFlags usage;
  VkSharingMode sharingMode;
  uint32_t queueFamilyIndexCount;
  const uint32_t* pQueueFamilyIndices;
  VkImageLayout initialLayout;
} VkImageCreateInfo;

VkResult vkCreateImage(
  VkDevice device,
  const VkImageCreateInfo* pCreateInfo,
  const VkAllocationCallbacks* pAllocator,
  VkImage* pImage);
```

- imageType指定了图像的种类，再前一节创建交换链时已大概了解过。
- format指定了图像资源的格式。
- extent指定了图像在xyz方向的大小。
- mipLevels指定了图像的Mipmap层级。
- arrayLayers指定了图像数组的成员数量。
- samples指定了多重采样的数量（详见下一节[渲染管线](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=渲染管线&zhida_source=entity)）。
- tiling指定了图像在内存中的排布方式，Optimal表示按照最佳的[图像存储](https://zhida.zhihu.com/search?content_id=207498668&content_type=Article&match_order=1&q=图像存储&zhida_source=entity)模式排布，Linear表示按照传统的行主序模式排布。
- usage指定了图像资源的用处。
- initialLayout指定了图像的初始布局。对于不同用途的图像设定特定的图像布局可以优化对图像的访问，例如图像附件就设定为ColorAttachmentOptimal，深度图附件就设定为DepthAttachmentOptimal。

### 图像视图ImageView

有关图像视图的创建已经在上一节创建交换链的时候讲解过，这里的原理也是一样的，注意图像和图像视图中部分参数的对应：

```cpp
auto texImageViewInfo = vk::ImageViewCreateInfo()
			.setComponents(vk::ComponentMapping(vk::ComponentSwizzle::eR, vk::ComponentSwizzle::eG, vk::ComponentSwizzle::eB, vk::ComponentSwizzle::eA))
			.setFormat(format)
			.setImage(image)
			.setSubresourceRange(vk::ImageSubresourceRange(vk::ImageAspectFlagBits::eColor, 0, 1, 0, 1))
			.setViewType(vk::ImageViewType::e2D);
device->createImageView(&texImageViewInfo, 0, &imageView);
```

### 为图像关联内存

创建了VkImage对象后就可以用和缓冲类似的方法为其分配和绑定设备内存：

```cpp
vk::MemoryRequirements imageMemReqs;
device->getImageMemoryRequirements(image, &imageMemReqs);

auto imageMemoryInfo = vk::MemoryAllocateInfo()
		.setAllocationSize(imageMemReqs.size);
MemoryTypeFromProperties(gpuProp, imageMemReqs.memoryTypeBits, vk::MemoryPropertyFlagBits::eDeviceLocal, imageMemoryInfo.memoryTypeIndex);
device->allocateMemory(&imageMemoryInfo, 0, &imageMemory);

device->bindImageMemory(image, imageMemory, 0);
```

### 用stb图像库载入图像数据

和缓冲不一样的是，由于大多数图像在内存中并非以线性方式存储的，所以我们不能直接通过映射将数据存入图像中，而是要绕一下远路，首先将图像数据存入一个缓冲中，以这个缓冲作为媒介，通过GPU执行命令缓冲区中的命令将缓冲中的数据转存入图像中。

首先我们需要获得所需的图像数据，stb图像解码库是一个非常好用的单文件图像解码库，大部分常用的图像格式都可以用它进行加载，而且较为易用，所以这里只展示了用stb库的加载方式。诸如微软的WIC库也可以使用而且对于bmp图像的支持较好，不过用起来比较复杂，这里不作介绍。

下面演示了用stb库加载图像数据并创建缓冲存储数据：

```cpp
#include "stb/stb_image.h"

void LoadPixelWithSTB(const char* path, uint32_t BPP, Texture& texture, vk::Device* device, vk::PhysicalDeviceMemoryProperties gpuProp) {
	int channelInFile;
	stbi_uc* source = stbi_load(path, reinterpret_cast<int*>(&texture.width), reinterpret_cast<int*>(&texture.height), &channelInFile, 4);

	texture.BPP = BPP;

	//Calculate image information to use
	uint64_t pixelRowPitch = (uint64_t(texture.width) * uint64_t(texture.BPP) + 7) / 8;
	texture.imageSize = pixelRowPitch * (uint64_t)texture.height;

	//Create buffer
	auto bufferInfo = vk::BufferCreateInfo()
		.setUsage(vk::BufferUsageFlagBits::eTransferSrc)
		.setSize(texture.imageSize);
	device->createBuffer(&bufferInfo, 0, &texture.uploader);

	//Allocate the memory of buffer
	vk::MemoryRequirements uploaderMemReqs;
	device->getBufferMemoryRequirements(texture.uploader, &uploaderMemReqs);

	auto bufferMemoryInfo = vk::MemoryAllocateInfo()
		.setAllocationSize(uploaderMemReqs.size);
	MemoryTypeFromProperties(gpuProp, uploaderMemReqs.memoryTypeBits, vk::MemoryPropertyFlagBits::eHostVisible | vk::MemoryPropertyFlagBits::eHostCoherent, bufferMemoryInfo.memoryTypeIndex);
	device->allocateMemory(&bufferMemoryInfo, 0, &texture.bufferMemory);

	device->bindBufferMemory(texture.uploader, texture.bufferMemory, 0);

	//Copy pixel data to the upload buffer
	BYTE* dst = nullptr;
	device->mapMemory(texture.bufferMemory, 0, uploaderMemReqs.size, vk::MemoryMapFlags(), reinterpret_cast<void**>(&dst));

	memcpy(dst, source, texture.imageSize);

	device->unmapMemory(texture.bufferMemory);aaa。
	stbi_image_free(source);
}
```

BPP指Bit per pixel（每个像素的位数），如果是32位RGBA则BPP就是32，然后pixelRowPitch就可以用图像的宽度乘以BPP再向上取整得出，整个图像的内存大小就是pixelRowPitch乘以图像的高度。注意在创建Buffer时，usage必须要指定TransferSrc使它能用作数据转移的源。

### 将数据存入Image中

我们需要GPU帮我们做将图像数据从缓冲转移至图像的工作，所以我们需要准备一个一次性的命令缓冲区。我定义了用于分配一次性命令缓冲区的函数，这样可以方便随时使用命令缓冲区：

```cpp
vk::CommandBuffer BeginSingleTimeCommand(vk::Device* device, const vk::CommandPool& cmdPool) {
	auto cmdAllocInfo = vk::CommandBufferAllocateInfo()
		.setCommandBufferCount(1)
		.setCommandPool(cmdPool)
		.setLevel(vk::CommandBufferLevel::ePrimary);

	vk::CommandBuffer cmd;
	device->allocateCommandBuffers(&cmdAllocInfo, &cmd);

	auto beginInfo = vk::CommandBufferBeginInfo()
		.setFlags(vk::CommandBufferUsageFlagBits::eOneTimeSubmit);
	cmd.begin(&beginInfo);

	return cmd;
}

void EndSingleTimeCommand(vk::CommandBuffer* cmd, vk::CommandPool& cmdPool, vk::Device* device, vk::Queue* queue) {
	cmd->end();

	auto submitInfo = vk::SubmitInfo()
		.setCommandBufferCount(1)
		.setPCommandBuffers(cmd);

	queue->submit(1, &submitInfo, vk::Fence());
	queue->waitIdle();

	device->freeCommandBuffers(cmdPool, 1, cmd);
}
```

这里我们要使用的命令是cmd下的copyBufferToImage，也就是将数据从缓冲拷贝至图像，对此我们也需要一个结构体来描述拷贝的方式。

```cpp
std::vector<vk::BufferImageCopy> copyRegion;

auto copyRegionIndex = vk::BufferImageCopy()
			.setBufferOffset(0)
			.setBufferImageHeight(0)
			.setBufferRowLength(0)
			.setImageOffset(vk::Offset3D(0, 0, 0))
			.setImageExtent(vk::Extent3D(width, height, 1))
			.setImageSubresource(vk::ImageSubresourceLayers(vk::ImageAspectFlagBits::eColor, 0, 0, 1));
copyRegion.push_back(copyRegionIndex);
```

copyRegion定义了拷贝的区域的属性，它的一些参数的介绍如下（以下单位皆为字节）：

- bufferOffset指定了拷贝的数据相对于缓冲的偏移量。
- bufferRowLength和bufferImageHeight以纹素的形式指定缓冲中较大的图像的子区域，并控制寻址计算。若这两个参量中有一个为0，则表示缓冲内存和imageExtent是紧密贴合的。
- imageOffset和imageExtent指定了拷贝图像的区域。
- imageSubresource用于指定带有Mipmap的图像或者图像数组的拷贝。

接下来我们需要使用管线屏障来确保GPU在访问数据时不会因为尚未同步而出现意向不到的错误，换句话说，就是确保了GPU数据拷贝操作的安全性，它的作用方式时这样的：管线屏障确保拷贝的目标图像准备完毕->数据拷贝->管线屏障确保拷贝完毕。管线屏障不仅可以确保GPU操作的同步，也可以起到转换图像布局的作用，是必不可少的一个操作。下面就演示了管线屏障和拷贝的具体操作：

```cpp
auto barrier = vk::ImageMemoryBarrier()
		.setImage(image)
		.setOldLayout(vk::ImageLayout::eUndefined)
		.setNewLayout(vk::ImageLayout::eTransferDstOptimal)
		.setSrcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
		.setDstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
		.setSubresourceRange(vk::ImageSubresourceRange(vk::ImageAspectFlagBits::eColor, 0, 1, 0, 1))
		.setDstAccessMask(vk::AccessFlagBits::eTransferWrite);
cmd.pipelineBarrier(vk::PipelineStageFlagBits::eTopOfPipe, vk::PipelineStageFlagBits::eTransfer, vk::DependencyFlags(), 0, 0, 0, 0, 1, &barrier);
	
cmd.copyBufferToImage(uploader, image, vk::ImageLayout::eTransferDstOptimal, copyRegion.size(), copyRegion.data());

barrier = vk::ImageMemoryBarrier()
		.setImage(image)
		.setOldLayout(vk::ImageLayout::eTransferDstOptimal)
		.setNewLayout(vk::ImageLayout::eShaderReadOnlyOptimal)
		.setSrcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
		.setDstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED)
		.setSubresourceRange(vk::ImageSubresourceRange(vk::ImageAspectFlagBits::eColor, 0, 1, 0, 1))
		.setDstAccessMask(vk::AccessFlagBits::eShaderRead);
cmd.pipelineBarrier(vk::PipelineStageFlagBits::eTransfer, vk::PipelineStageFlagBits::eFragmentShader, vk::DependencyFlags(), 0, 0, 0, 0, 1, &barrier);
```

VkImageMemoryBarrier的一些参数的介绍如下：

- oldLayout和newLayout指定了图像的原始布局和转换后的布局，前一个管线屏障需要将转换后的布局设定为TransferDstOptimal来标记它是一个用作拷贝目标的图像，后一个管线屏障需要将转换后的布局设定为ShaderReadOnlyOptimal来标记它是一个可以被着色器访问的图像资源。
- srcQueueFamilyIndex和dstQueueFamilyIndex可以改变图像对应的队列族，通常我们不需要，所以直接填入VK_QUEUE_FAMILY_IGNORED。
- subresourceRange指定了拷贝的子资源范围，和ImageSubresourceLayers的意义类似。
- setDstAccessMask指定了目标的许可掩码，标记了转换后的资源允许的操作，前一个管线屏障设定为TransferWrite标记为转移写入，后一个管线屏障设定为ShaderRead标记为着色器访问。

在拷贝结束后不要忘记释放已经失去用处的缓冲：

```cpp
device->destroyBuffer(uploader);
device->freeMemory(bufferMemory);
```

至此我们已经完成了图像资源的创建，接下来就可以将图像资源用在渲染管线中了。下一节会详细讲解渲染管线相关内容。

