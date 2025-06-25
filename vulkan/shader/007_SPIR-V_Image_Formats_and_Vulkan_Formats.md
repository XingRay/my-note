### Compatibility Between SPIR-V Image Formats and Vulkan Formats

SPIR-V `Image` `Format` values are compatible with [VkFormat](https://registry.khronos.org/vulkan/specs/latest/html/vkspec.html#VkFormat) values as defined below:

| SPIR-V Image Format | Compatible Vulkan Format             |
| :------------------ | :----------------------------------- |
| `Unknown`           | Any                                  |
| `R8`                | `VK_FORMAT_R8_UNORM`                 |
| `R8Snorm`           | `VK_FORMAT_R8_SNORM`                 |
| `R8ui`              | `VK_FORMAT_R8_UINT`                  |
| `R8i`               | `VK_FORMAT_R8_SINT`                  |
| `Rg8`               | `VK_FORMAT_R8G8_UNORM`               |
| `Rg8Snorm`          | `VK_FORMAT_R8G8_SNORM`               |
| `Rg8ui`             | `VK_FORMAT_R8G8_UINT`                |
| `Rg8i`              | `VK_FORMAT_R8G8_SINT`                |
| `Rgba8`             | `VK_FORMAT_R8G8B8A8_UNORM`           |
| `Rgba8Snorm`        | `VK_FORMAT_R8G8B8A8_SNORM`           |
| `Rgba8ui`           | `VK_FORMAT_R8G8B8A8_UINT`            |
| `Rgba8i`            | `VK_FORMAT_R8G8B8A8_SINT`            |
| `Rgb10A2`           | `VK_FORMAT_A2B10G10R10_UNORM_PACK32` |
| `Rgb10a2ui`         | `VK_FORMAT_A2B10G10R10_UINT_PACK32`  |
| `R16`               | `VK_FORMAT_R16_UNORM`                |
| `R16Snorm`          | `VK_FORMAT_R16_SNORM`                |
| `R16ui`             | `VK_FORMAT_R16_UINT`                 |
| `R16i`              | `VK_FORMAT_R16_SINT`                 |
| `R16f`              | `VK_FORMAT_R16_SFLOAT`               |
| `Rg16`              | `VK_FORMAT_R16G16_UNORM`             |
| `Rg16Snorm`         | `VK_FORMAT_R16G16_SNORM`             |
| `Rg16ui`            | `VK_FORMAT_R16G16_UINT`              |
| `Rg16i`             | `VK_FORMAT_R16G16_SINT`              |
| `Rg16f`             | `VK_FORMAT_R16G16_SFLOAT`            |
| `Rgba16`            | `VK_FORMAT_R16G16B16A16_UNORM`       |
| `Rgba16Snorm`       | `VK_FORMAT_R16G16B16A16_SNORM`       |
| `Rgba16ui`          | `VK_FORMAT_R16G16B16A16_UINT`        |
| `Rgba16i`           | `VK_FORMAT_R16G16B16A16_SINT`        |
| `Rgba16f`           | `VK_FORMAT_R16G16B16A16_SFLOAT`      |
| `R32ui`             | `VK_FORMAT_R32_UINT`                 |
| `R32i`              | `VK_FORMAT_R32_SINT`                 |
| `R32f`              | `VK_FORMAT_R32_SFLOAT`               |
| `Rg32ui`            | `VK_FORMAT_R32G32_UINT`              |
| `Rg32i`             | `VK_FORMAT_R32G32_SINT`              |
| `Rg32f`             | `VK_FORMAT_R32G32_SFLOAT`            |
| `Rgba32ui`          | `VK_FORMAT_R32G32B32A32_UINT`        |
| `Rgba32i`           | `VK_FORMAT_R32G32B32A32_SINT`        |
| `Rgba32f`           | `VK_FORMAT_R32G32B32A32_SFLOAT`      |
| `R64ui`             | `VK_FORMAT_R64_UINT`                 |
| `R64i`              | `VK_FORMAT_R64_SINT`                 |
| `R11fG11fB10f`      | `VK_FORMAT_B10G11R11_UFLOAT_PACK32`  |