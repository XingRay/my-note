# Renderpass创建



```
vk::SubpassDescription subpassDescription{};
subpassDescription
        .setFlags(mFlags)
        .setPipelineBindPoint(mPipelineBindPoint)
        .setInputAttachments(mInputAttachments)
        .setResolveAttachments(mResolveAttachments)
        .setColorAttachments(mColorAttachments)
        .setPreserveAttachments(mPreserveAttachments);
```

源码:

```
SubpassDescription & setResolveAttachments(
      VULKAN_HPP_NAMESPACE::ArrayProxyNoTemporaries<const VULKAN_HPP_NAMESPACE::AttachmentReference> const & resolveAttachments_ ) VULKAN_HPP_NOEXCEPT
    {
      colorAttachmentCount = static_cast<uint32_t>( resolveAttachments_.size() );
      pResolveAttachments  = resolveAttachments_.data();
      return *this;
    }
```

注意在调用 setResolveAttachments 时,会同时设置 colorAttachmentCount,而没有 resolveAttachmentCount

```
colorAttachmentCount = static_cast<uint32_t>( resolveAttachments_.size() );
```

原因? vulkan要求 resolveAttachmentCount == colorAttachmentCount