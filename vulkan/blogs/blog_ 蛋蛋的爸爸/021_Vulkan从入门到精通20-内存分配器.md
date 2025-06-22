# Vulkan从入门到精通20-内存分配器

这篇聊聊 vulkan内存分配器。vulkan中除了使用默认的内存方式外，还支持使用自定义内存分配器。

自定义的内存分配器结构体如下

```cpp
typedef struct VkAllocationCallbacks {
    void*                                   pUserData;
    PFN_vkAllocationFunction                pfnAllocation;
    PFN_vkReallocationFunction              pfnReallocation;
    PFN_vkFreeFunction                      pfnFree;
    PFN_vkInternalAllocationNotification    pfnInternalAllocation;
    PFN_vkInternalFreeNotification          pfnInternalFree;
} VkAllocationCallbacks;
```

各个[成员指针](https://zhida.zhihu.com/search?content_id=187840359&content_type=Article&match_order=1&q=成员指针&zhida_source=entity)很容易理解。在这里我们使用基本的malloc/free来实现一套简单的内存分配器。

```cpp
class VK_Allocator {
public:
    VK_Allocator();
    ~VK_Allocator();
public:
    VkAllocationCallbacks* getAllocator();
private:
    VkAllocationCallbacks* allocationCallback = nullptr;
};
```

实现如下

```cpp
void *reallocationFunction(void *userData, void *original, size_t size, size_t alignment,
                           VkSystemAllocationScope allocationScope)
{
    return realloc(original, size);
}

void internalAllocationNotification(void *userData, size_t size,
                                    VkInternalAllocationType allocationType, VkSystemAllocationScope allocationScope)
{
}

void internalFreeNotification(void *userData, size_t size, VkInternalAllocationType allocationType, VkSystemAllocationScope allocationScope)
{
}

VkAllocationCallbacks *VK_Allocator::getAllocator()
{
    if (!allocationCallback)
        allocationCallback = new VkAllocationCallbacks();

    allocationCallback->pUserData = (void *)this;
    allocationCallback->pfnAllocation = (PFN_vkAllocationFunction)(&allocationFunction);
    allocationCallback->pfnReallocation = (PFN_vkReallocationFunction)(&reallocationFunction);
    allocationCallback->pfnFree = (PFN_vkFreeFunction)&freeFunction;
    allocationCallback->pfnInternalAllocation = (PFN_vkInternalAllocationNotification)
            &internalAllocationNotification;
    allocationCallback->pfnInternalFree = (PFN_vkInternalFreeNotification)&internalFreeNotification;
    return allocationCallback;
}

VK_Allocator::VK_Allocator()
{
}

VK_Allocator::~VK_Allocator()
{
    if (allocationCallback)
        delete allocationCallback;
}
```



在vk各个Create/Destroy[函数调用](https://zhida.zhihu.com/search?content_id=187840359&content_type=Article&match_order=1&q=函数调用&zhida_source=entity)中就可以使用getAllocator()代替原有的nullptr。

对了在调用前Context需要增加*virtual* VkAllocationCallbacks* *getAllocation*() = 0; 接口

