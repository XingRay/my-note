# Vulkan从入门到精通33-推送描述符

本文来聊聊推送描述符， 在[蛋蛋的爸爸：Vulkan从入门到精通27-推送常量](https://zhuanlan.zhihu.com/p/457676618) 一文中我们基于管线布局把unniform常量推送给了着色器。其实还可以以推送描述符的形式，直接在命令缓冲区中推送描述符集。使用方式如下。

shader 顶点

```cpp
#version 400
#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
layout (std140, binding = 0) uniform buf {
    mat4 mvp;
} ubuf;
layout (location = 0) in vec3 pos;
layout (location = 1) in vec4 color;
layout (location = 2) in vec2 inTexCoords;
layout (location = 0) out vec2 texcoord;
void main() {
   texcoord = inTexCoords;
   gl_Position = ubuf.mvp * vec4(pos,1.0);
}
```

shader 片段

```cpp
#version 450

layout(binding = 1) uniform sampler2D texSampler;
layout(location = 0) in vec2 fragTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = texture(texSampler, fragTexCoord);
}
```

在[命令缓冲区](https://zhida.zhihu.com/search?content_id=191030758&content_type=Article&match_order=2&q=命令缓冲区&zhida_source=entity)调用前，加载VkWriteDescriptorSet。

使用例子如下

```cpp
#include <iostream>
#include <cstring>
#include <glm/mat4x4.hpp>
#include <glm/gtx/transform.hpp>
#include "VK_UniformBuffer.h"
#include "VK_Context.h"
#include "VK_Image.h"
#include "VK_Texture.h"
#include "VK_Pipeline.h"
#include "VK_DynamicState.h"

using namespace std;

const std::vector<float> vertices = {
    -0.5f, -0.5, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.05f, 0.0f,
        0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
        0.5f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.5f,
        -0.5f, 0.5f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.5f, 1.5f
    };

const std::vector<uint32_t> indices = {
    0, 1, 2, 2, 3, 0
};

VK_Context *context = nullptr;
VK_Pipeline *pipeline = nullptr;

uint32_t updateUniformBufferData(char *&data, uint32_t size)
{
    glm::mat4 model = glm::identity<glm::mat4>();
    memcpy(data, &model[0][0], size);
    return sizeof(model);
}

void onFrameSizeChanged(int width, int height)
{
    pipeline->getDynamicState()->applyDynamicViewport({0, 0, (float)width, (float)height, 0, 1});
}

int main()
{
    VK_ContextConfig config;
    config.debug = true;
    config.name = "Texure Demo";

    context = createVkContext(config);
    context->createWindow(480, 480, true);
    context->setOnFrameSizeChanged(onFrameSizeChanged);

    VK_Context::VK_Config vkConfig;
    context->initVulkanDevice(vkConfig);

    auto shaderSet = context->createShaderSet();
    shaderSet->addShader("../shader/push-descriptor/vert.spv", VK_SHADER_STAGE_VERTEX_BIT);
    shaderSet->addShader("../shader/push-descriptor/frag.spv", VK_SHADER_STAGE_FRAGMENT_BIT);

    shaderSet->appendAttributeDescription(0, sizeof (float) * 3, VK_FORMAT_R32G32B32_SFLOAT, 0);
    shaderSet->appendAttributeDescription(1, sizeof (float) * 4, VK_FORMAT_R32G32B32A32_SFLOAT,
                                          sizeof(float) * 3);
    shaderSet->appendAttributeDescription(2, sizeof (float) * 2, VK_FORMAT_R32G32_SFLOAT,
                                          sizeof(float) * 7);

    shaderSet->appendVertexInputBindingDescription(9 * sizeof(float), 0, VK_VERTEX_INPUT_RATE_VERTEX);

    VkDescriptorSetLayoutBinding uniformBinding = VK_ShaderSet::createDescriptorSetLayoutBinding(0,
            VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT);
    shaderSet->addDescriptorSetLayoutBinding(uniformBinding);

    auto samplerBinding = VK_ShaderSet::createDescriptorSetLayoutBinding(1,
                          VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER,
                          VK_SHADER_STAGE_FRAGMENT_BIT);
    auto samplerCreateInfo  = VK_Sampler::createSamplerCreateInfo();
    auto samplerPtr = context->createSampler(samplerCreateInfo);
    VkSampler sampler = samplerPtr->getSampler();
    samplerBinding.pImmutableSamplers = &sampler;

    shaderSet->addDescriptorSetLayoutBinding(samplerBinding);

    if (!shaderSet->isValid()) {
        std::cerr << "invalid shaderSet" << std::endl;
        shaderSet->release();
        context->release();
        return -1;
    }

    auto ubo = shaderSet->addUniformBuffer(0, sizeof(float) * 16);
    ubo->setWriteDataCallback(updateUniformBufferData);

    auto image = context->createImage("../images/cat.png");
    auto imageViewCreateInfo = VK_ImageView::createImageViewCreateInfo(image->getImage(),
                               VK_FORMAT_R8G8B8A8_SRGB);
    auto imageView = context->createImageView(imageViewCreateInfo);
    shaderSet->addImageView(imageView);

    auto uboDescriptor = ubo->createWriteDescriptorSet(0, nullptr);
    auto imageViewDescriptor = imageView->createWriteDescriptorSet(nullptr);

    context->initVulkanContext();
    pipeline = context->createPipeline(shaderSet);
    pipeline->getDynamicState()->addDynamicState(VK_DYNAMIC_STATE_VIEWPORT);
    pipeline->create();
    pipeline->getDynamicState()->applyDynamicViewport({0, 0, 480, 480, 0, 1});

    pipeline->addPushDescriptor(uboDescriptor);
    pipeline->addPushDescriptor(imageViewDescriptor);

    auto buffer = context->createVertexBuffer(vertices, 5, indices);
    pipeline->addRenderBuffer(buffer);

    context->createCommandBuffers();

    context->run();
    context->release();

    return 0;
}
```

VK_PushDescriptor实现如下

```cpp
VK_PushDescriptor::VK_PushDescriptor(VK_Context *vkContext):
    context(vkContext)
{
    vkCmdPushDescriptorSetKHR = (PFN_vkCmdPushDescriptorSetKHR)vkGetDeviceProcAddr(context->getDevice(), "vkCmdPushDescriptorSetKHR");
}

void VK_PushDescriptor::addDescriptor(const VkWriteDescriptorSet &descriptor)
{
    descriptors.push_back(descriptor);
}

void VK_PushDescriptor::push(VkCommandBuffer commandBuffer, VkPipelineLayout layout)
{
    if(vkCmdPushDescriptorSetKHR)
        vkCmdPushDescriptorSetKHR(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, layout, 0, descriptors.size(), descriptors.data());
}
```

vkCmdPushDescriorSetKHR调用在绑定管线后，在渲染顶点数据之前。