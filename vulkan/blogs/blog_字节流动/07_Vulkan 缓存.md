# Vulkan 缓存

## 什么是 VkBuffer

Vulkan 中的缓存资源通过 VkBuffer 对象来表示, 它是一种用于存储通用数据的资源，**可以用来存储顶点数据、索引数据、Uniform 数据等**。

**VkBuffer 表示的是一个线性内存块，这意味着它的内存布局是连续的，类似于数组。**这种布局特别适合存储顺序访问的数据，如顶点数据和索引数据，但也支持随机访问。

我们创建 VkBuffer 时，**可以通过设置 VkBufferCreateInfo 不同的 usage 标志来指定 VkBuffer 的用途**，例如 VK_BUFFER_USAGE_VERTEX_BUFFER_BIT（顶点缓冲）、VK_BUFFER_USAGE_INDEX_BUFFER_BIT（索引缓冲）、VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT（Uniform 缓冲）等。

值得注意的是，**在 Vulkan 中创建的所有资源（VkBuffer、VkImage 等）都是虚资源。**

换句话说就是，创建的资源仅仅是一个资源句柄，并没有对应存储资源数据的内存，后续需要通过函数 vkBindBufferMemory 将资源绑定到相应的设备内存 VkDeviceMemory ，所以数据实际上是存储在设备内存。

**一旦设备内存绑定到一个资源对象（VkBuffer、VkImage）上，这个内存绑定就不能再次改变了。**

在设备内存绑定到资源上之前，需要确定使用什么类型的内存，以及资源需要多少内存。

这个时候，可以使用vkGetBufferMemoryRequirements 获取缓冲区内存需求，包括内存大小、对齐要求以及适合的内存类型。

## 创建 VkBuffer

缓存资源通过 vkCreateBuffer() 函数创建，其定义如下：

#### vkCreateBuffer

```
VkResult vkCreateBuffer(
VkDevice                                    device,
const VkBufferCreateInfo*                   pCreateInfo,
const VkAllocationCallbacks*                pAllocator,
VkBuffer*                                   pBuffer);
```

其中 pCreateInfo 为缓存创建配置信息，对应的 VkBufferCreateInfo 类型定义如下：

#### VkBufferCreateInfo

```
typedef struct VkBufferCreateInfo {
VkStructureType        sType;//必须是VkStructureType::VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO 。
const void*            pNext;//nullptr
VkBufferCreateFlags    flags;//0
VkDeviceSize           size;//缓存大小,单位为字节
VkBufferUsageFlags     usage;//指定该缓存的用途(重要)
VkSharingMode          sharingMode;//配置该缓存的共享模式, 是否会被多个设备队列访问
uint32_t               queueFamilyIndexCount;//指定 pQueueFamilyIndices 数组中元素数量
const uint32_t*        pQueueFamilyIndices;//指定将会访问该缓存的设备队列
} VkBufferCreateInfo;
```

##### VkBufferUsageFlagBits （重点关注）

```
typedef enum VkBufferUsageFlagBits {
VK_BUFFER_USAGE_TRANSFER_SRC_BIT = 0x00000001,
VK_BUFFER_USAGE_TRANSFER_DST_BIT = 0x00000002,
VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT = 0x00000004,
VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT = 0x00000008,
VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT = 0x00000010,
VK_BUFFER_USAGE_STORAGE_BUFFER_BIT = 0x00000020,
VK_BUFFER_USAGE_INDEX_BUFFER_BIT = 0x00000040,
VK_BUFFER_USAGE_VERTEX_BUFFER_BIT = 0x00000080,
VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT = 0x00000100
} VkBufferUsageFlagBits;
```

- **VK_BUFFER_USAGE_TRANSFER_SRC_BIT** 该缓存用于数据传输的数据源。
- **VK_BUFFER_USAGE_TRANSFER_DST_BIT** 该缓存用于数据传输的目的数据。
- VK_BUFFER_USAGE_UNIFORM_TEXEL_BUFFER_BIT 该缓存用于存储纹素数据。用于设备读取。
- VK_BUFFER_USAGE_STORAGE_TEXEL_BUFFER_BIT 该缓存用于存储纹素数据。用于设备读取和存储。
- **VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT** 该缓存用于存储任意格式数据。用于设备读取。
- VK_BUFFER_USAGE_STORAGE_BUFFER_BIT 该缓存用于存储任意格式数据。用于设备读取和存储。
- **VK_BUFFER_USAGE_INDEX_BUFFER_BIT** 该缓存用于存储整型索引数据。
- **VK_BUFFER_USAGE_VERTEX_BUFFER_BIT** 该缓存用于存储具有相同结构的顶点数据。
- VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT 该缓存用于间接数据。用于存储指令参数，设备可一次性读取这些参数。

##### 资源共享类型 VkSharingMode

```
typedef enum VkSharingMode {
VK_SHARING_MODE_EXCLUSIVE = 0,//设备队列独享资源。该资源一次只能被一种设备队列族中的队列访问。
VK_SHARING_MODE_CONCURRENT = 1,//设备队列共享资源。该资源一次能被多种设备队列族中的队列访问。
} VkSharingMode;
```

获取缓冲区内存需求的函数 vkGetBufferMemoryRequirements :

```
void vkGetBufferMemoryRequirements(
    VkDevice device,// Vulkan 设备句柄
    VkBuffer buffer,//需要查询内存需求的 VkBuffer 对象
    VkMemoryRequirements* pMemoryRequirements//内存需求信息
);
```

VkMemoryRequirements 该结构体包含了缓冲区的内存需求信息：

```
typedef struct VkMemoryRequirements {
VkDeviceSize    size;           // 缓冲区所需的内存大小（以字节为单位）
VkDeviceSize    alignment;      // 缓冲区内存的对齐要求
uint32_t        memoryTypeBits; // 缓冲区可用的内存类型位掩码
} VkMemoryRequirements;
```

## VkBuffer 使用示例代码

```
// 顶点数据，4个顶点坐标
std::vector<float> vertices= {
        1.0f,  1.0f, 0.0f,
        1.0f,  1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
};

// 创建 VkBuffer 用于存储顶点数据
VkBufferCreateInfo bufferInfo = {};
bufferInfo.sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
bufferInfo.pNext = nullptr;
bufferInfo.flags = 0;
bufferInfo.size = sizeof(vertices); // 设置缓冲区大小
bufferInfo.usage = VK_BUFFER_USAGE_VERTEX_BUFFER_BIT; // 设置缓冲区用途
bufferInfo.sharingMode = VK_SHARING_MODE_EXCLUSIVE; // 共享模式
bufferInfo.queueFamilyIndexCount = 1;
bufferInfo.pQueueFamilyIndices = graphicsQueueFamilyIndex;//设备队列索引

VkBuffer vertexBuffer;
if (vkCreateBuffer(device, &bufferInfo, nullptr, &vertexBuffer) != VK_SUCCESS) {
    throw std::runtime_error("failed to create vertex buffer!");
}

// 获取内存需求, 包含 Vulkan 内存对齐信息，以及内存对齐之后，内存的 size\memoryTypeBits
VkMemoryRequirements memRequirements;
vkGetBufferMemoryRequirements(device, vertexBuffer, &memRequirements);

// 分配设备内存
VkMemoryAllocateInfo allocInfo = {};
allocInfo.sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
allocInfo.allocationSize = memRequirements.size;
// 获取到对 CPU 可见且自动同步的设备内存类型
allocInfo.memoryTypeIndex = findMemoryType(memRequirements.memoryTypeBits, 
    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

VkDeviceMemory vertexBufferMemory;
if (vkAllocateMemory(device, &allocInfo, nullptr, &vertexBufferMemory) != VK_SUCCESS) {
    throw std::runtime_error("failed to allocate vertex buffer memory!");
}

// 绑定内存
vkBindBufferMemory(device, vertexBuffer, vertexBufferMemory, 0);

// 内存映射，填充数据
void* data;
vkMapMemory(device, vertexBufferMemory, 0, bufferInfo.size, 0, &data);//获取设备内存映射的内存地址
memcpy(data, vertices, (size_t) bufferInfo.size);//将顶点数据拷贝到设备内存映射的内存地址

// 内存解映射
vkUnmapMemory(device, vertexBufferMemory);
```

内存类型选择函数

```
uint32_t findMemoryType(uint32_t typeFilter, VkMemoryPropertyFlags properties) {
    VkPhysicalDeviceMemoryProperties memProperties;
    vkGetPhysicalDeviceMemoryProperties(physicalDevice, &memProperties);

    for (uint32_t i = 0; i < memProperties.memoryTypeCount; i++) {
        if ((typeFilter & (1 << i)) && (memProperties.memoryTypes[i].propertyFlags & properties) == properties) {
            return i;
        }
    }

    throw std::runtime_error("failed to find suitable memory type!");
}
```



-- END --