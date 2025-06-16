# 染管线中不同阶段



在 Vulkan 中，`vk::PipelineStageFlagBits` 表示渲染管线中不同阶段的逻辑划分，这些阶段定义了 GPU 执行命令的顺序和同步范围。以下是按渲染流程顺序排列的管线阶段及其核心作用：

------

### **1. 顶点输入阶段**

**标志位**：`VK_PIPELINE_STAGE_VERTEX_INPUT_BIT`
 ​**作用**​：从顶点缓冲（Vertex Buffer）和索引缓冲（Index Buffer）读取顶点数据，为后续着色阶段准备输入数据。此阶段是顶点处理的起点

。



------

### **2. 顶点着色器阶段**

**标志位**：`VK_PIPELINE_STAGE_VERTEX_SHADER_BIT`
 ​**作用**​：执行顶点着色器程序，对顶点进行坐标变换、光照计算等操作，生成变换后的顶点数据

。



------

### **3. 细分曲面控制阶段**

**标志位**：`VK_PIPELINE_STAGE_TESSELLATION_CONTROL_SHADER_BIT`
 ​**作用**​：运行细分控制着色器（Tessellation Control Shader），生成细分因子（Tessellation Factors）和面片（Patch）参数，控制几何体的细分级别

。



------

### **4. 细分曲面评估阶段**

**标志位**：`VK_PIPELINE_STAGE_TESSELLATION_EVALUATION_SHADER_BIT`
 ​**作用**​：执行细分评估着色器（Tessellation Evaluation Shader），根据细分因子生成新的顶点数据，将面片细分为更小的几何图元

。



------

### **5. 几何着色器阶段**

**标志位**：`VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT`
 ​**作用**​：运行几何着色器（Geometry Shader），修改或生成新的几何图元（如点、线、三角形），支持动态几何体生成

。



------

### **6. 光栅化阶段**

**标志位**：无直接对应标志，但涉及以下阶段：

- 

  预光栅化阶段

  （

  ```
  VK_PIPELINE_STAGE_PRE_RASTERIZATION_SHADERS_BIT
  ```

  ，Vulkan 1.3+）

  

  作用

  ：将几何图元转换为屏幕空间的片元（Fragment），执行视口变换、裁剪和剔除操作

  。

------

### **7. 早期片段测试阶段**

**标志位**：`VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT`
 ​**作用**​：在片段着色器之前执行深度和模板测试，提前剔除不可见的片元以节省计算资源

。



------

### **8. 片段着色器阶段**

**标志位**：`VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT`
 ​**作用**​：执行片段着色器（Fragment Shader），计算片元的颜色、纹理采样等最终输出值

。



------

### **9. 晚期片段测试阶段**

**标志位**：`VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT`
 ​**作用**​：在片段着色器之后再次执行深度和模板测试，确保深度和模板值正确写入附件

。



------

### **10. 颜色附件输出阶段**

**标志位**：`VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT`
 ​**作用**​：将片元颜色写入颜色附件，执行混合（Blending）和逻辑操作（Logical Operations）

。