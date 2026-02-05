# Vulkan从入门到精通10-不变采样器

在上文我们已经可以在vulkan中显示纹理了。其中设置image view 和 sampler是填充结构VkDescriptorImageInfo来完成的

```cpp
typedef struct VkDescriptorImageInfo {
    VkSampler        sampler;
    VkImageView      imageView;
    VkImageLayout    imageLayout;
} VkDescriptorImageInfo;
```

除此之外，另外一种加载[采样器](https://zhida.zhihu.com/search?content_id=185631699&content_type=Article&match_order=1&q=采样器&zhida_source=entity)的办法是对于描述符类型*VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER，*设置descriptorSetLayoutBinding的pImmutableSamplers字段为具体采样器。例子如下:

```cpp
    {
        VK_DescriptorSetLayoutBindingGroup bindingGroup;
        VkDescriptorSetLayoutBinding uniformBinding = VK_DescriptorSetLayoutBindingGroup::createDescriptorSetLayoutBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT);
        bindingGroup.addDescriptorSetLayoutBinding(uniformBinding);

        auto samplerBinding = VK_DescriptorSetLayoutBindingGroup::createDescriptorSetLayoutBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT);
        auto samplerCreateInfo  = VK_Sampler::createSamplerCreateInfo();
        auto samplerPtr = context->createSampler(samplerCreateInfo);
        VkSampler sampler = samplerPtr->getSampler();
        samplerBinding.pImmutableSamplers = &sampler;

        bindingGroup.addDescriptorSetLayoutBinding(samplerBinding);
        context->setDescriptorSetLayoutBindingGroup(bindingGroup);
    }
```

关于该字段的详细介绍如下

pImmutableSamplers 这是一个指向采样器（由描述符集布局消耗的相应绑定表示）句柄数组的指针。

如果指定的描述符类型 descriptorType 是 **VK_DESCRIPTOR_TYPE_SAMPLER** 或 **VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER**，则该字段用于初始化一组不可变的采样器。 如果 descriptorType 不是这些描述符类型之一，则忽略此字段（pImmutableSamplers）。 一旦不可变的采样器被绑定，它们就不能再次被绑定到集合布局中。 当此字段为0时，采样器槽就是动态的，采样器句柄必须使用此布局绑定到[描述符集](https://zhida.zhihu.com/search?content_id=185631699&content_type=Article&match_order=2&q=描述符集&zhida_source=entity)。



代码仓库 -

[https://github.com/ccsdu2004/vulkan-cpp-demogithub.com/ccsdu2004/vulkan-cpp-demo](https://github.com/ccsdu2004/vulkan-cpp-demo)