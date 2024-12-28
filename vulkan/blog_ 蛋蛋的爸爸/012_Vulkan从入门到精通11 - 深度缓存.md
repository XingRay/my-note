# Vulkan从入门到精通11 - 深度缓存

本文来谈谈深度缓存。我们在用OpenGL来绘制图形的时候，希望有些图形在前，有些图形在后，这时候就用到了[z坐标](https://zhida.zhihu.com/search?content_id=185931031&content_type=Article&match_order=1&q=z坐标&zhida_source=entity)。当然，不一定z坐标大的物体一定在z坐标小的物体后面。因为，这取决于我们的观察平面，即摄像机的位置。当然如果我们不改动摄像机的位置（即初始化状态），那就可以这么认为的了。但是，如果z坐标超出到了屏幕的外面，那当然我们是看不到的了。深度缓存（区）的原理就是把一个距离观察平面的深度值（或距离）与窗口的每个像素相关联。

在绘制之前，先通过glClear(GL_DEPTH_BUFFER_BIT) 来清除深度缓存。然后在绘制的时候OpenGL会计算绘制图形与观察平面的距离。如果启用了深度缓存区，那么，在绘制的时候会首先对新的深度值和当前窗口中的深度值进行比较，如果小于，则替换当前像素位置的深度值和像素值。反之，就会被遮挡。当然在使用深度测试前需要通过glEnable(GL_*DEPTH_TEST*)开启深度测试，通过glDepthRange调整深度范围，通过glDepthFunc设置深度方程。

在vulkan中深度是基于图像的，就像颜色附件一样。不同的是交换链不自动产生[深度图](https://zhida.zhihu.com/search?content_id=185931031&content_type=Article&match_order=1&q=深度图&zhida_source=entity)。 所以需要定义一个vkImage和vkImageView对象。

```cpp
    VK_ImageImpl* vkDepthImage = nullptr;
    VK_ImageViewImpl* vkDepthImageView = nullptr;
```

然后在createDepthResource中创建和初始化

```cpp
void VK_ContextImpl::createDepthResources()
{
    VkFormat depthFormat = findDepthFormat();

    vkDepthImage = new VK_ImageImpl(device, this);
    vkDepthImage->createImage(vkSwapChainExtent.width, vkSwapChainExtent.height, depthFormat, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

    auto createInfo = VK_ImageView::createImageViewCreateInfo(vkDepthImage->getImage(), depthFormat);
    createInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_DEPTH_BIT;
    vkDepthImageView = new VK_ImageViewImpl(device, this);
    vkDepthImageView->create(createInfo);
}
```

深度图需要从硬件设备查询，具体如下

```cpp
    for (VkFormat format : candidates) {
        VkFormatProperties props;
        vkGetPhysicalDeviceFormatProperties(physicalDevice, format, &props);

        if (tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures & features) == features) {
            return format;
        } else if (tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures & features) == features) {
            return format;
        }
    }

    std::cerr << "failed to find supported format!" << std::endl;
    return VkFormat();
```

depthImage和depthImageView的创建和之前的image比较类似，只是VkImageAspectFlags 取VK_IMAGE_ASPECT_DEPTH_BIT。

然后在renderpass 中增加深度附件描述

```cpp
    VkAttachmentDescription depthAttachment{};
    depthAttachment.format = findDepthFormat();
    depthAttachment.samples = VK_SAMPLE_COUNT_1_BIT;
    depthAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
    depthAttachment.storeOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
    depthAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
    depthAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
    depthAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
    depthAttachment.finalLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;
```

深度附件索引和subpass附件

```cpp
    VkAttachmentReference depthAttachmentRef{};
    depthAttachmentRef.attachment = 1;
    depthAttachmentRef.layout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL;

    VkSubpassDescription subpass{};
    subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
    subpass.colorAttachmentCount = 1;
    subpass.pColorAttachments = &colorAttachmentRef;
    subpass.pDepthStencilAttachment = &depthAttachmentRef;
```

然后挂载在VkRenderPassCreateInfo上

然后是附件描述增加深度附件，见下

```text
    std::array<VkAttachmentDescription, 2> attachments = {colorAttachment, depthAttachment};
    VkRenderPassCreateInfo renderPassInfo{};
    renderPassInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
    renderPassInfo.attachmentCount = static_cast<uint32_t>(attachments.size());
    renderPassInfo.pAttachments = attachments.data();
    renderPassInfo.subpassCount = 1;
    renderPassInfo.pSubpasses = &subpass;
    renderPassInfo.dependencyCount = 1;
    renderPassInfo.pDependencies = &dependency;
```



framebuffer中附件数总是和renderpass中的附件数一一对应的，所以需要修改framebuffer部分，见下

```cpp
        std::array<VkImageView, 2> attachments = {
            swapChainImageViews[i]->getImageView(),
            vkDepthImageView->getImageView()
        };

        VkFramebufferCreateInfo framebufferInfo{};
        framebufferInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
        framebufferInfo.renderPass = renderPass;
        framebufferInfo.attachmentCount = static_cast<uint32_t>(attachments.size());
        framebufferInfo.pAttachments = attachments.data();
        framebufferInfo.width = vkSwapChainExtent.width;
        framebufferInfo.height = vkSwapChainExtent.height;
        framebufferInfo.layers = 1;
```

清空也需要指定2个

```cpp
        std::array<VkClearValue, 2> clearValues{};
        memcpy((char*)&clearValues[0], &vkClearValue, sizeof(vkClearValue));
        clearValues[1].depthStencil = {1.0f, 0};

        renderPassInfo.clearValueCount = static_cast<uint32_t>(clearValues.size());
        renderPassInfo.pClearValues = clearValues.data();
```

最后在创建管线时需要开启[深度测试](https://zhida.zhihu.com/search?content_id=185931031&content_type=Article&match_order=3&q=深度测试&zhida_source=entity)

```text
    VkPipelineDepthStencilStateCreateInfo depthStencil{};
    depthStencil.sType = VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;
    depthStencil.depthTestEnable = VK_TRUE;
    depthStencil.depthWriteEnable = VK_TRUE;
    depthStencil.depthCompareOp = VK_COMPARE_OP_LESS;
    depthStencil.depthBoundsTestEnable = VK_FALSE;
    depthStencil.stencilTestEnable = VK_FALSE;
    depthStencil.minDepthBounds = 0.0f;
    depthStencil.maxDepthBounds = 1.0f;
```

除了sType成员，其他在opengl中都有相关函数

代码仓库 -

[https://github.com/ccsdu2004/vulkan-cpp-demogithub.com/ccsdu2004/vulkan-cpp-demo](https://link.zhihu.com/?target=https%3A//github.com/ccsdu2004/vulkan-cpp-demo)