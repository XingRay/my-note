# 使用CPP风格Vulkan API

在桌面环境中, 如windows, 可以直接使用 vulkan sdk,引入 `<vulkan/vulkan.hpp>` 使用 cpp 风格api, 但是在android平台, 通常不使用静态api, 而是通过动态加载的方式调用, 那么要使用cpp风格api就需要先加载 dispatcher .



1 查找Vulkan api 版本号

找到 ndk 中的 `vulkan_core.h` 中找到 header 版本号定义:

路径: 

```shell
/android-sdk/ndk/27.2.12479018/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include/vulkan/vulkan_core.h
```

找到下面的定义:

```
// Version of this file
#define VK_HEADER_VERSION 275
```

这里header版本号为 275



2 下载版本对应的头文件库

Vulkan头文件库

https://github.com/KhronosGroup/Vulkan-Hpp

找到release列表

https://github.com/KhronosGroup/Vulkan-Hpp/releases
在列表中找到版本号后缀为 header 版本号的 release 版本, 如: 275 

下载源码:
https://github.com/KhronosGroup/Vulkan-Hpp/archive/refs/tags/v1.3.275.zip

解压到本地目录



3 引入项目

CMakeLists.txt

```cmake
cmake_minimum_required(VERSION 3.22.1)
project("vulkan_demo")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Werror -std=c++17 -DVK_USE_PLATFORM_ANDROID_KHR")

# ...

# Vulkan-Hpp
# https://github.com/KhronosGroup/Vulkan-Hpp
set(VULKAN_HPP_INSTALL_DIR D:/develop/vulkan/Vulkan-Headers/Vulkan-Headers-1.3.275)
set(VULKAN_HPP_INCLUDE_DIR ${VULKAN_HPP_INSTALL_DIR}/include)

# ...

target_include_directories(${PROJECT_NAME} PUBLIC
        # ...
		# Vulkan-Hpp
        ${VULKAN_HPP_INCLUDE_DIR}
		# ...
)

```

注意在android项目上要加 `VK_USE_PLATFORM_ANDROID_KHR` 宏定义



4 设置默认 dispatcher

头文件中引入:

engine.h

```c++
#include <vulkan_wrapper.h>

#define VULKAN_HPP_DISPATCH_LOADER_DYNAMIC 1
#include <vulkan/vulkan.hpp>
```

在引入 `<vulkan/vulkan.hpp>` 前添加宏定义 `VULKAN_HPP_DISPATCH_LOADER_DYNAMIC` 可以阻止使用静态api, 而是用动态分发

在源文件中注意要使用宏声明默认 dispatcher

engine.cpp

```c++
#include "engine.h"
#include "Log.h"
#include <cassert>

VULKAN_HPP_DEFAULT_DISPATCH_LOADER_DYNAMIC_STORAGE

namespace engine {
	// ...
}
```



5 创建 VkInstance

需要先引入c api , 并且创建一个 `VkInstance` 对象

```c++
vk::DynamicLoader dl;
auto vkGetInstanceProcAddr = dl.getProcAddress<PFN_vkGetInstanceProcAddr>("vkGetInstanceProcAddr");
if (!vkGetInstanceProcAddr) {
    throw std::runtime_error("Failed to load vkGetInstanceProcAddr");
}

VkApplicationInfo appInfo = {
        .sType = VK_STRUCTURE_TYPE_APPLICATION_INFO,
        .pNext = nullptr,
        .pApplicationName = "vulkan_android_demo",
        .applicationVersion = VK_MAKE_VERSION(1, 0, 0),
        .pEngineName = "vk_engine",
        .engineVersion = VK_MAKE_VERSION(1, 0, 0),
        .apiVersion = VK_API_VERSION_1_3,
};

std::vector<const char *> instance_extensions;

instance_extensions.push_back("VK_KHR_surface");
instance_extensions.push_back("VK_KHR_android_surface");

VkInstanceCreateInfo instanceCreateInfo{
        .sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO,
        .pNext = nullptr,
        .pApplicationInfo = &appInfo,
        .enabledLayerCount = 0,
        .ppEnabledLayerNames = nullptr,
        .enabledExtensionCount =
        static_cast<uint32_t>(instance_extensions.size()),
        .ppEnabledExtensionNames = instance_extensions.data(),
};

auto vkCreateInstance = dl.getProcAddress<PFN_vkCreateInstance>("vkCreateInstance");
if (!vkCreateInstance) {
    throw std::runtime_error("Failed to load vkCreateInstance");
}

CALL_VK(vkCreateInstance(&instanceCreateInfo, nullptr, (VkInstance *) &device.instance));
```

注意这里 device.instance 类型就是 vk::Instance , 避免重复创建 



6 加载默认dispatcher

```c++
vk::DispatchLoaderDynamic dld(device.instance, vkGetInstanceProcAddr);
dld.init(dl);
vk::defaultDispatchLoaderDynamic = dld;

vk::AndroidSurfaceCreateInfoKHR createInfo = vk::AndroidSurfaceCreateInfoKHR()
        .setFlags(vk::AndroidSurfaceCreateFlagsKHR{})
        .setWindow(window);

device.surface = device.instance.createAndroidSurfaceKHR(createInfo);
```

创建 DispatchLoaderDynamic 需要有一个 VkInstance 对象, 会自动调用 vkGetInstanceProcAddr 查找大部分 api 的函数指针, 关于 instance 相关的 api 则需要通过 `dld.init(dl);` 方法, 将相关的api的函数指针从 DynamicLoader 中复制到 DispatchLoaderDynamic 对象中. 将完整的 DispatchLoaderDynamic 对象复制给 vk::defaultDispatchLoaderDynamic 后, 就可以正常调用 cpp api 了



7 原理分析

所有 cpp 风格api中的参数列表都有:

```c++
Dispatch const & d                                                  VULKAN_HPP_DEFAULT_DISPATCHER_ASSIGNMENT 
```

这样额定义, 其中 Dispatch 是 

```c++
typename Dispatch = VULKAN_HPP_DEFAULT_DISPATCHER_TYPE
```

定义的, 实际上就是 `vk::DispatchLoaderDynamic` ,而后面的 `VULKAN_HPP_DEFAULT_DISPATCHER_ASSIGNMENT`就是 `= VULKAN_HPP_DEFAULT_DISPATCHER`, 而  `VULKAN_HPP_DEFAULT_DISPATCHER` 就是 `vk::defaultDispatchLoaderDynamic`, 全部替换之后, 起始就是:

```c++
vk::DispatchLoaderDynamic const & d = vk::defaultDispatchLoaderDynamic 
```

也就是默认值为 vk::defaultDispatchLoaderDynamic

而宏 `VULKAN_HPP_DEFAULT_DISPATCH_LOADER_DYNAMIC_STORAGE` 的定义就是

```
namespace VULKAN_HPP_NAMESPACE                                                                       
{                                                                                                    
	VULKAN_HPP_STORAGE_API ::VULKAN_HPP_NAMESPACE::DispatchLoaderDynamic defaultDispatchLoaderDynamic; 
}
```

宏替换之后就是

```c++
namespace vk                                                                       
{                                                                                                    
	::vk::DispatchLoaderDynamic defaultDispatchLoaderDynamic;
}
```

也就是定义了 defaultDispatchLoaderDynamic .

所以在

```c++
vk::defaultDispatchLoaderDynamic = dld;
```

给 vk::defaultDispatchLoaderDynamic 赋值之后, 就可以正常使用 cpp api 了, 不传入 vk::DispatchLoaderDynamic 对象的情况下会使用全局变量 defaultDispatchLoaderDynamic  作为默认值.

而cpp api 的调用都是通过 DispatchLoaderDynamic  对象调用持有的 c api 来实现的, 如:

```c++
template <typename PhysicalDeviceAllocator, typename Dispatch>
  VULKAN_HPP_NODISCARD VULKAN_HPP_INLINE typename ResultValueType<std::vector<VULKAN_HPP_NAMESPACE::PhysicalDevice, PhysicalDeviceAllocator>>::type
                       Instance::enumeratePhysicalDevices( Dispatch const & d ) const
  {
    VULKAN_HPP_ASSERT( d.getVkHeaderVersion() == VK_HEADER_VERSION );

    std::vector<VULKAN_HPP_NAMESPACE::PhysicalDevice, PhysicalDeviceAllocator> physicalDevices;
    uint32_t                                                                   physicalDeviceCount;
    VULKAN_HPP_NAMESPACE::Result                                               result;
    do
    {
      result = static_cast<VULKAN_HPP_NAMESPACE::Result>( d.vkEnumeratePhysicalDevices( m_instance, &physicalDeviceCount, nullptr ) );
      if ( ( result == VULKAN_HPP_NAMESPACE::Result::eSuccess ) && physicalDeviceCount )
      {
        physicalDevices.resize( physicalDeviceCount );
        result = static_cast<VULKAN_HPP_NAMESPACE::Result>(
          d.vkEnumeratePhysicalDevices( m_instance, &physicalDeviceCount, reinterpret_cast<VkPhysicalDevice *>( physicalDevices.data() ) ) );
      }
    } while ( result == VULKAN_HPP_NAMESPACE::Result::eIncomplete );
    resultCheck( result, VULKAN_HPP_NAMESPACE_STRING "::Instance::enumeratePhysicalDevices" );
    VULKAN_HPP_ASSERT( physicalDeviceCount <= physicalDevices.size() );
    if ( physicalDeviceCount < physicalDevices.size() )
    {
      physicalDevices.resize( physicalDeviceCount );
    }
    return createResultValueType( result, physicalDevices );
  }
```

简单来看就是:

```c++
R Instance::enumeratePhysicalDevices(vk::DispatchLoaderDynamic const & d = vk::defaultDispatchLoaderDynamic){
	VULKAN_HPP_ASSERT( d.getVkHeaderVersion() == VK_HEADER_VERSION );
    do{
	    d.vkEnumeratePhysicalDevices( m_instance, &physicalDeviceCount, nullptr )        
    }while(xxx);
	// ...
}
```

其他的api也是类似
