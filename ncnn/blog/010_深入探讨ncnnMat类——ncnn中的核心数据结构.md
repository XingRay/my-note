# 深入探讨ncnn::Mat类——ncnn中的核心数据结构




最近在学习 ncnn 推理框架，下面整理了 ncnn::Mat 的使用方法。

ncnn作为一个高性能的神经网络推理框架，其核心数据结构ncnn::Mat在数据存储与处理上扮演了至关重要的角色。本文将从基础到高级，详细介绍ncnn::Mat类的各个方面，帮助开发者全面理解并高效利用这一强大的数据结构。



## 1 Mat类简介

ncnn::Mat是ncnn框架中用于存储和管理张量数据的核心数据结构。它支持最多4维的数据（NCDHW格式），能够高效处理神经网络中的特征图、权重以及其他相关数据。通过Mat类，ncnn实现了对数据的高效内存管理和快速访问，为深度学习推理提供了坚实的基础。

    class NCNN_EXPORT Mat
    {
    public:
        // 指向实际数据的指针
        void* data;
    
        // 元素字节大小（如float32为4字节）
        size_t elemsize;
    
        // 数据打包数，用于SIMD优化
        int elempack;
    
        // 分配器
        Allocator* allocator;
    
        // 数据维度（1-4）
        int dims;
    
        // 宽、高、深度、通道数
        int w;
        int h;
        int d;
        int c;
    
        // 通道步长
        size_t cstep;
    };


## 2 基本属性和结构

Mat类包含多个成员变量，用于描述和管理数据的属性和结构：

data: 指向实际数据的内存地址。
elemsize: 每个元素的字节大小，例如float32为4字节。
elempack: 数据打包的数量，用于SIMD（单指令多数据）优化。
allocator: 用于内存分配的分配器对象。
dims: 数据的维度（1到4）。
w, h, d, c: 分别表示数据的宽度、高度、深度和通道数。
cstep: 通道步长，表示每个通道的数据跨度，便于内存对齐和快速访问。

示例

```
// 创建一个3通道的32x32图像Mat
Mat feat(32, 32, 3); // w=32, h=32, c=3
printf("Total size: %zu\n", feat.total()); // 输出总元素数：32*32*3

// 创建一个float32的向量
Mat vector(100, 4u); // w=100, elemsize=4 (float)
```



## 3 构造函数详解

Mat类提供了多种构造函数，以适应不同的数据存储需求：

```
// 空Mat
Mat();

// 一维向量
Mat(int w, size_t elemsize = 4u, Allocator* allocator = 0);

// 二维矩阵/图像
Mat(int w, int h, size_t elemsize = 4u, Allocator* allocator = 0);

// 三维张量
Mat(int w, int h, int c, size_t elemsize = 4u, Allocator* allocator = 0);

// 四维张量
Mat(int w, int h, int d, int c, size_t elemsize = 4u, Allocator* allocator = 0);

// 拷贝构造
Mat(const Mat& m);

// 外部内存构造（不拷贝数据）
Mat(int w, void* data, size_t elemsize = 4u, Allocator* allocator = 0);
```

使用示例

```
// 创建3x224x224的RGB图像
Mat img(224, 224, 3);

// 创建batch=4的特征图
Mat feat(56, 56, 64, 4); // 4张56x56的64通道特征图

// 创建float16数据
Mat weight(64, 2u); // elemsize=2表示float16

// 使用外部内存构造Mat，不会发生数据拷贝
float external_data[1000];
Mat external_mat(100, external_data, 4u);
```



## 4 数据访问和操作

Mat类提供了多种方式来访问和操作数据，包括使用下标操作符、行访问、通道访问以及范围访问等。

使用下标操作符
适用于一维数据的快速访问。

```
Mat v(10);
v[0] = 1.0f;
v[1] = 2.0f;
// ...
```

行访问
通过row()函数访问特定行的数据指针。

```
Mat m(100, 100);
float* row_ptr = m.row(50);
row_ptr[10] = 3.14f;
```

通道访问
通过channel()函数访问特定通道的数据。

```
Mat img(224, 224, 3); // RGB图像
Mat r_channel = img.channel(0); // 获取R通道
Mat g_channel = img.channel(1); // 获取G通道
Mat b_channel = img.channel(2); // 获取B通道
```

范围访问
使用range()、channel_range()和row_range()函数进行子区域的数据访问。

```
// 一维向量前10个元素
Mat subset = img.range(0, 10);

// 提取前2个通道
Mat first_two_channels = img.channel_range(0, 2);

// 提取25-74行
Mat central_rows = img.row_range(25, 50);
```



## 5 内存管理机制

Mat类采用引用计数（reference counting）机制管理内存，确保内存的高效利用和避免内存泄漏。

引用计数相关成员

```
// 引用计数指针
int* refcount;

// 增加引用计数
void addref();

// 释放内存，减少引用计数
void release();
```

引用计数机制示例

```
Mat a(224, 224, 3);
Mat b = a; // b和a共享同一数据，引用计数增加
// 当b和a都被销毁或释放时，数据才会被真正释放
```

拷贝赋值操作
赋值运算符重载实现了浅拷贝，增加引用计数而不复制数据。

    Mat& Mat::operator=(const Mat& m)
    {
        if (this == &m)
            return *this;
    
        if (m.refcount)
            NCNN_XADD(m.refcount, 1); // 引用计数+1
    
        release(); // 释放当前数据
    
        data = m.data;
        refcount = m.refcount;
        elemsize = m.elemsize;
        elempack = m.elempack;
        allocator = m.allocator;
    
        dims = m.dims;
        w = m.w;
        h = m.h;
        d = m.d;
        c = m.c;
        cstep = m.cstep;
    
        return *this;
    }
深拷贝操作
通过clone()和clone_from()函数实现数据的完整复制，创建独立的数据副本。

```
Mat a(224, 224, 3);
Mat b = a.clone(); // b拥有a的数据副本，不共享
```



## 6 Vulkan支持

为了提升性能，ncnn提供了基于Vulkan的VkMat和VkImageMat类，支持GPU加速的数据操作。

VkMat类概述
VkMat类与Mat类似，但数据存储在GPU的缓冲区中，适用于在Vulkan环境下的高效计算。

    class NCNN_EXPORT VkMat
    {
    public:
        VkBufferMemory* data; // GPU缓冲区
        size_t elemsize;
        int elempack;
        VkAllocator* allocator;
        int dims;
        int w, h, d, c;
        size_t cstep;
    
        // 数据访问
        Mat mapped() const;
        void* mapped_ptr() const;
    };


VkImageMat类概述
VkImageMat类用于存储图像格式的数据，适用于需要纹理处理的场景。

    class NCNN_EXPORT VkImageMat
    {
    public:
        VkImageMemory* data; // GPU图像内存
        size_t elemsize;
        int elempack;
        VkAllocator* allocator;
        int dims;
        int w, h, d, c;
    
        // 数据访问
        Mat mapped() const;
        void* mapped_ptr() const;
    };
使用示例

```
// 创建一个GPU缓冲区中的Mat
VkAllocator vkalloc;
VkMat vk_mat;
vk_mat.create(224, 224, 3, sizeof(float), &vkalloc);

// 映射到CPU内存进行访问
Mat cpu_mat = vk_mat.mapped();
cpu_mat.fill(1.0f); // 填充数据
```



## 7 SIMD优化支持

为了充分利用现代CPU的并行计算能力，Mat类通过elempack支持SIMD（单指令多数据）数据打包优化，支持多种指令集如ARM NEON、x86 SSE/AVX、MIPS MSA和RISC-V向量扩展。

SIMD优化成员函数

```
#if __ARM_NEON
    void fill(float32x4_t _v);
    void fill(uint16x4_t _v);
    // ...
#endif

#if __SSE2__
    void fill(__m128 _v);
    void fill(__m256 _v, int i = 0);
    // ...
#endif

#if __mips_msa
    void fill(v4f32 _v);
#endif

#if __riscv_vector
    void fill(vfloat32m1_t _v);
    void fill(vuint16m1_t _v);
    // ...
#endif
```

使用示例

```
Mat m(8, 8, 64);
#if __ARM_NEON
float32x4_t _v = vdupq_n_f32(1.f);
m.fill(_v); // NEON 4路并行填充
#endif
```



## 8 像素图像处理支持

Mat类提供了丰富的图像处理功能，包括图像格式转换、调整图像大小、ROI裁剪等。

常用枚举 - PixelType

```
enum PixelType
{
    PIXEL_RGB = 1,
    PIXEL_BGR = 2,
    PIXEL_GRAY = 3,
    PIXEL_RGBA = 4,
    PIXEL_BGRA = 5,
    // 其他转换类型...
};
```

构造与转换函数

```
// 从像素数据构造Mat
static Mat from_pixels(const unsigned char* pixels, int type, int w, int h);

// 调整图像大小
static Mat from_pixels_resize(const unsigned char* pixels, int type, int w, int h, 
                             int target_w, int target_h);

// ROI裁剪
static Mat from_pixels_roi(const unsigned char* pixels, int type, int w, int h,
                           int roix, int roiy, int roiw, int roih);

// 转换Mat到像素数据
void to_pixels(unsigned char* pixels, int type) const;

// 调整大小并转换
void to_pixels_resize(unsigned char* pixels, int type, int target_w, int target_h) const;
```

使用示例

```
// 从RGB像素数据创建Mat
unsigned char rgb_data[224 * 224 * 3];
Mat img = Mat::from_pixels(rgb_data, PIXEL_RGB, 224, 224);

// 调整图像大小
Mat resized_img = Mat::from_pixels_resize(rgb_data, PIXEL_RGB, 224, 224, 112, 112);

// ROI裁剪
Mat roi_img = Mat::from_pixels_roi(rgb_data, PIXEL_RGB, 224, 224, 50, 50, 100, 100);

// 转换Mat到BGR像素数据
unsigned char bgr_data[112 * 112 * 3];
resized_img.to_pixels(bgr_data, PIXEL_BGR);
```



## 9 边界扩充操作

在图像处理和卷积操作中，经常需要对图像进行边界扩充。Mat类提供了多种边界填充模式，确保数据处理的完整性和准确性。

边界填充模式枚举

```
enum BorderType
{
    BORDER_CONSTANT = 0,    // 常数填充
    BORDER_REPLICATE = 1,   // 边缘复制
    BORDER_REFLECT = 2,     // 边缘镜像
    BORDER_TRANSPARENT = -233  // 透明填充
};
```

边界扩充函数

```
// 二维图像边界扩充
void copy_make_border(const Mat& src, Mat& dst, 
                     int top, int bottom, int left, int right,
                     int type, float v, const Option& opt = Option());

// 三维张量边界扩充
void copy_make_border_3d(const Mat& src, Mat& dst, 
                        int top, int bottom, int left, int right, 
                        int front, int behind,
                        int type, float v, const Option& opt = Option());

// 边界裁剪函数
void copy_cut_border(const Mat& src, Mat& dst, 
                    int top, int bottom, int left, int right, 
                    const Option& opt = Option());
```

使用示例

```
Mat src_img(224, 224, 3);
Mat dst_img;

// 使用常数填充，上下各10像素，左右各20像素，填充值为0
copy_make_border(src_img, dst_img, 10, 10, 20, 20, BORDER_CONSTANT, 0.f);

// 使用边缘复制填充
copy_make_border(src_img, dst_img, 5, 5, 5, 5, BORDER_REPLICATE, 0.f);

// 对三维张量进行边界扩充
Mat src_feat(56, 56, 64);
Mat dst_feat;
copy_make_border_3d(src_feat, dst_feat, 2, 2, 2, 2, 1, 1, BORDER_REFLECT, 0.f);
```



## 10 高级数据转换

Mat类支持多种数据类型的转换，包括不同浮点精度的转换、量化与反量化等，以满足不同计算需求和硬件优化。

数据类型转换函数

```
// float32 与 float16 互转
void cast_float32_to_float16(const Mat& src, Mat& dst);
void cast_float16_to_float32(const Mat& src, Mat& dst);

// float32 与 bfloat16 互转
unsigned short float32_to_bfloat16(float value);
float bfloat16_to_float32(unsigned short value);
void cast_float32_to_bfloat16(const Mat& src, Mat& dst);
void cast_bfloat16_to_float32(const Mat& src, Mat& dst);

// 量化与反量化
void quantize_to_int8(const Mat& src, Mat& dst, const Mat& scale_data, const Option& opt = Option());
void dequantize_from_int32(const Mat& src, Mat& dst, 
                          const Mat& scale_data, const Mat& bias_data, const Option& opt = Option());
void requantize_from_int32_to_int8(const Mat& src, Mat& dst, 
                                  const Mat& scale_in_data, const Mat& scale_out_data, 
                                  const Mat& bias_data, int activation_type, 
                                  const Mat& activation_params, const Option& opt = Option());
```

使用示例

```
// float32 转 float16
Mat float32_mat(100, 224, 224, 3);
Mat float16_mat;
cast_float32_to_float16(float32_mat, float16_mat);

// 量化为int8
Mat quantized_mat;
Mat scale_data = Mat::from_pixels(...); // 假设已经定义
quantize_to_int8(float32_mat, quantized_mat, scale_data);

// 反量化为float32
Mat dequantized_mat;
Mat bias_data = Mat::from_pixels(...); // 假设已经定义
dequantize_from_int32(quantized_mat, dequantized_mat, scale_data, bias_data);
```



## 11 自定义内存分配器

Mat类支持使用自定义内存分配器，以满足特定的内存管理需求或优化策略。

定义自定义分配器
    class CustomAllocator : public Allocator 
    {
    public:
        virtual void* fastMalloc(size_t size) override
        {
            // 自定义内存分配逻辑
            return malloc(size);
        }
        
        virtual void fastFree(void* ptr) override
        {
            // 自定义内存释放逻辑
            free(ptr);
        }
    };
使用自定义分配器

```
CustomAllocator myalloc;
Mat m(224, 224, 3, &myalloc);

// 使用自定义分配器创建Mat
Mat a(100, 100, 3, &myalloc);
```



## 12 跨平台编译优化

Mat类通过条件编译支持多种指令集和平台优化，确保在不同硬件环境下都能高效运行。

条件编译支持

```
#if __ARM_NEON
    // ARM NEON优化代码
#endif

#if __SSE2__
    // x86 SSE2优化代码
#endif

#if __mips_msa
    // MIPS MSA优化代码
#endif

#if __riscv_vector
    // RISC-V向量扩展优化代码
#endif
```

导出符号控制
通过宏定义控制导出符号，确保跨平台兼容性。

```
// 导出符号控制宏
#define NCNN_EXPORT 
```



## 13 图像处理扩展功能

Mat类不仅提供基本的图像处理功能，还支持高级的旋转和仿射变换操作，满足复杂的图像处理需求。

图像旋转支持
支持8种不同的旋转方式，基于EXIF的方向信息进行图像旋转。

```
// 图像旋转类型枚举
enum RotateType {
    ROTATE_0 = 1,
    ROTATE_90 = 6,
    ROTATE_180 = 3,
    ROTATE_270 = 8,
    // 其他旋转类型可扩展
};

// 旋转操作函数
void kanna_rotate_c3(const unsigned char* src, int srcw, int srch,
                    unsigned char* dst, int w, int h, int type);
```

图像仿射变换支持
支持通过旋转角度、缩放因子和偏移量计算仿射变换矩阵，并应用于图像数据。

```
// 获取旋转仿射变换矩阵
void get_rotation_matrix(float angle, float scale, float dx, float dy, float* tm);

// 应用仿射变换
void warpaffine_bilinear_c3(const unsigned char* src, int srcw, int srch,
                           unsigned char* dst, int w, int h, const float* tm, 
                           int type = 0, unsigned int v = 0);
```

使用示例

```
// 图像旋转
const unsigned char* src_pixels = ...; // 原始图像数据
unsigned char* rotated_pixels = new unsigned char[new_width * new_height * 3];
kanna_rotate_c3(src_pixels, original_width, original_height, 
               rotated_pixels, new_width, new_height, ROTATE_90);

// 图像仿射变换
float tm[6];
get_rotation_matrix(45.0f, 1.0f, 0.0f, 0.0f, tm);
warpaffine_bilinear_c3(src_pixels, original_width, original_height, 
                       transformed_pixels, transformed_width, transformed_height, tm);
```



## 14 Mat深拷贝与浅拷贝

Mat类支持深拷贝和浅拷贝两种数据复制方式，分别用于不同的使用场景。

浅拷贝机制
默认情况下，Mat通过浅拷贝构造函数和赋值运算符实现数据的共享，仅复制指针，引用计数增加。

```
Mat a(224, 224, 3);
Mat b = a; // b和a共享同一数据，引用计数增加
Mat c(a);  // 同样是浅拷贝
```

深拷贝操作
通过clone()和clone_from()函数实现数据的完整复制，创建独立的数据副本。

```
Mat a(224, 224, 3);
Mat b = a.clone(); // b拥有a的数据副本，不共享
Mat c;
c.clone_from(a);    // 将a的数据深拷贝到c
```

使用示例

```
// 浅拷贝
Mat a(100, 100, 3);
Mat b = a;
// 修改b的数据也会影响a
b[0] = 1.0f;

// 深拷贝
Mat c = a.clone();
c[0] = 2.0f;
// a的数据不受c的修改影响
```



## 15 数据遍历模式

根据不同的应用需求，Mat类支持多种数据遍历模式，包括通道优先和行优先两种常见方式。

通道优先遍历
先遍历通道，再遍历每个通道内的行和列。

```
Mat m(w, h, c);
for (int q = 0; q < c; q++) // 通道循环最外层
{
    const float* ptr = m.channel(q);
    for (int i = 0; i < h; i++)
    {
        for (int j = 0; j < w; j++)
        {
            float val = ptr[i * w + j];
            // 处理数据
        }
    }
}
```

行优先遍历
先遍历行，再遍历每行内的通道和列。

```
for (int i = 0; i < h; i++)
{
    for (int q = 0; q < c; q++)
    {
        const float* ptr = m.channel(q).row(i);
        for (int j = 0; j < w; j++)
        {
            float val = ptr[j];
            // 处理数据
        }
    }
}
```

使用示例

```
Mat img(640, 480, 3); // RGB图像

// 通道优先遍历
for (int c = 0; c < img.c; c++)
{
    Mat channel = img.channel(c);
    for (int y = 0; y < img.h; y++)
    {
        float* row = channel.row(y);
        for (int x = 0; x < img.w; x++)
        {
            row[x] = 255.0f; // 设置像素值
        }
    }
}

// 行优先遍历
for (int y = 0; y < img.h; y++)
{
    for (int c = 0; c < img.c; c++)
    {
        float* row = img.channel(c).row(y);
        for (int x = 0; x < img.w; x++)
        {
            row[x] = 128.0f; // 设置像素值
        }
    }
}
```



## 16 YUV图像处理

在移动设备和视频处理应用中，YUV格式广泛应用。Mat类提供了高效的YUV格式转换函数，支持快速将YUV图像转换为RGB格式。

YUV转换函数

```
// YUV420sp(NV21)转RGB，快速近似版本
void yuv420sp2rgb(const unsigned char* yuv420sp, int w, int h, unsigned char* rgb);

// YUV420sp(NV12)转RGB，快速近似版本
void yuv420sp2rgb_nv12(const unsigned char* yuv420sp, int w, int h, unsigned char* rgb);

// YUV420sp(NV21)转RGB并半尺寸缩放，更快的近似版本
void yuv420sp2rgb_half(const unsigned char* yuv420sp, int w, int h, unsigned char* rgb);
```

使用示例

```
unsigned char yuv_data[640 * 480 * 3 / 2]; // YUV420sp数据
unsigned char rgb_data[640 * 480 * 3];      // RGB数据

// YUV420sp(NV21)转RGB
yuv420sp2rgb(yuv_data, 640, 480, rgb_data);

// YUV420sp(NV12)转RGB
yuv420sp2rgb_nv12(yuv_data, 640, 480, rgb_data);

// YUV420sp(NV21)转RGB并缩放到320x240
unsigned char resized_rgb_data[320 * 240 * 3];
yuv420sp2rgb_half(yuv_data, 640, 480, resized_rgb_data);
```



## 17 线性代数操作

Mat类支持多种线性代数操作，如矩阵变形、切片等，方便进行矩阵运算和数据处理。

矩阵变形
通过reshape()函数，可以改变Mat的维度而不复制数据。

```
// 1D向量转2D矩阵
Mat v(100);
Mat m = v.reshape(10, 10); // 10x10矩阵

// 2D矩阵转3D张量
Mat m2(10, 10);
Mat t = m2.reshape(10, 10, 3); // 10x10x3张量
```

矩阵切片
使用range()、channel_range()和row_range()函数进行子区域的数据访问。

```
// 切片示例
Mat img(100, 100, 3);
Mat top_left = img.range(0, 50).row_range(0, 50); // 前50行前50列
Mat first_two_channels = img.channel_range(0, 2); // 前两个通道
```

使用示例

```
// 矩阵变形
Mat vector(100);
Mat matrix = vector.reshape(10, 10);

// 矩阵切片
Mat img(100, 100, 3);
Mat middle_section = img.range(25, 50).row_range(25, 50).channel_range(1, 1); // 中间区域的G通道
```



## 18 异常处理与内存安全

为了确保数据操作的可靠性和内存的安全性，Mat类提供了多种机制进行异常处理和边界检查。

空指针检查
在访问数据前，始终检查Mat是否为空，防止空指针访问导致的程序崩溃。

```
bool Mat::empty() const 
{
    return data == 0 || total() == 0;
}

// 安全的数据访问示例
void process_mat(const Mat& m) 
{
    if (m.empty()) {
        // 处理空Mat情况
        return;
    }
    // 安全处理数据
    float* ptr = (float*)m.data;
    // ...
}
```

维度边界检查
在访问特定通道或行时，必须确保索引在有效范围内，防止越界访问。

```
Mat m(224, 224, 3);

// 访问通道时进行范围检查
int c = 2;
if (c >= 0 && c < m.c) {
    Mat channel = m.channel(c);
}

// 访问行时进行范围检查
int y = 50;
if (y >= 0 && y < m.h) {
    float* row_ptr = m.row(y);
}
```

使用示例

```
Mat img(224, 224, 3);

// 安全访问第4个通道（索引为3）
int channel_index = 3;
if (channel_index >= 0 && channel_index < img.c) {
    Mat channel = img.channel(channel_index);
    // 操作channel
} else {
    // 处理无效通道索引
}
```



## 19 高级内存管理特性

Mat类通过内存池复用和自动内存对齐等高级内存管理特性，提升内存利用率和访问效率。

内存池复用
通过自定义分配器，实现内存池复用，减少频繁的内存分配与释放，提高性能。

    class PoolAllocator : public Allocator 
    {
        std::vector<std::pair<size_t, void*>> freed_memory;
    public:
        virtual void* fastMalloc(size_t size) override 
        {
            // 从freed_memory中查找合适的内存块
            for (auto it = freed_memory.begin(); it != freed_memory.end(); ++it) {
                if (it->first >= size) {
                    void* ptr = it->second;
                    freed_memory.erase(it);
                    return ptr;
                }
            }
            // 如果没有合适的，才新建
            return malloc(size);
        }
        
        virtual void fastFree(void* ptr) override 
        {
            // 将内存块放入内存池
            size_t size = /* 获取ptr对应的大小 */;
            freed_memory.emplace_back(size, ptr);
        }
    };
内存对齐优化
通过alignSize()函数，实现对齐后的内存分配，提升数据访问效率。

```
// 计算对齐后的大小
static size_t alignSize(size_t sz, int n) 
{
    return (sz + n - 1) & -n;
}

// 使用示例
size_t aligned_width = alignSize(width, 16);  // 16字节对齐
Mat m(aligned_width, height, channels);
```

使用示例

```
PoolAllocator pool_alloc;
Mat m(224, 224, 3, &pool_alloc);
```



## 20 性能分析工具

为了优化性能，开发者可以通过内存使用统计和操作耗时统计工具，评估和提升Mat类的使用效率。

内存使用统计
通过跟踪内存分配和释放，实现对内存使用情况的统计和监控。

```
class TrackedAllocator : public Allocator 
{
    size_t total_allocated = 0;
    size_t peak_allocated = 0;
    std::unordered_map<void*, size_t> size_map;
public:
    virtual void* fastMalloc(size_t size) override 
    {
        void* ptr = malloc(size);
        total_allocated += size;
        peak_allocated = std::max(peak_allocated, total_allocated);
        size_map[ptr] = size;
        return ptr;
    }
    
    virtual void fastFree(void* ptr) override 
    {
        if (size_map.find(ptr) != size_map.end()) {
            total_allocated -= size_map[ptr];
            size_map.erase(ptr);
        }
        free(ptr);
    }

    size_t get_total_allocated() const { return total_allocated; }
    size_t get_peak_allocated() const { return peak_allocated; }
};
```


操作耗时统计
通过封装分配和释放函数，记录各类操作的耗时，帮助识别性能瓶颈。

    class BenchmarkAllocator : public Allocator 
    {
        double total_alloc_time = 0.0;
        double total_free_time = 0.0;
        
        double get_current_time() const 
        {
            return std::chrono::duration<double>(std::chrono::steady_clock::now().time_since_epoch()).count();
        }
    
    public:
        virtual void* fastMalloc(size_t size) override 
        {
            double start = get_current_time();
            void* ptr = malloc(size);
            double end = get_current_time();
            total_alloc_time += (end - start);
            return ptr;
        }
        
        virtual void fastFree(void* ptr) override 
        {
            double start = get_current_time();
            free(ptr);
            double end = get_current_time();
            total_free_time += (end - start);
        }
    
        double get_total_alloc_time() const { return total_alloc_time; }
        double get_total_free_time() const { return total_free_time; }
    };

使用示例

```
BenchmarkAllocator bench_alloc;
Mat m(224, 224, 3, &bench_alloc);

// 执行若干操作...

printf("Total allocation time: %f seconds\n", bench_alloc.get_total_alloc_time());
printf("Total free time: %f seconds\n", bench_alloc.get_total_free_time());
```



## 21 特殊格式数据处理

Mat类支持处理特殊格式的数据，如稀疏矩阵和压缩数据，进一步提升数据处理的灵活性和效率。

稀疏矩阵支持
使用结构体存储稀疏矩阵的非零元素，并提供转换函数。    

    struct SparseMatEntry 
    {
        int i, j;    // 位置索引
        float value; // 非零值
    };
    
    // 将稀疏数据转换为Mat
    Mat sparse_to_dense(const std::vector<SparseMatEntry>& entries, 
                       int w, int h) 
    {
        Mat m(w, h);
        m.fill(0.f); // 填充0
        
        for (const auto& entry : entries) {
            float* ptr = m.row(entry.i);
            ptr[entry.j] = entry.value;
        }
        return m;
    }
压缩数据处理
支持游程编码（Run-Length Encoding, RLE）等压缩数据格式的解码，节省存储空间。    

    // 游程编码数据结构
    struct RLEEntry 
    {
        float value; // 值
        int count;    // 连续次数
    };
    
    // 解码游程编码数据到Mat
    Mat rle_decode(const std::vector<RLEEntry>& rle_data, 
                   int w, int h) 
    {
        Mat m(w, h);
        float* ptr = (float*)m.data;
        
        for (const auto& run : rle_data) {
            std::fill(ptr, ptr + run.count, run.value);
            ptr += run.count;
        }
        return m;
    }
使用示例

```
// 稀疏矩阵转换
std::vector<SparseMatEntry> sparse_entries = {
    {0, 0, 1.0f},
    {0, 1, 2.0f},
    {1, 0, 3.0f},
    // 其他非零元素...
};
Mat dense_mat = sparse_to_dense(sparse_entries, 100, 100);

// 游程编码解码
std::vector<RLEEntry> rle_data = {
    {0.0f, 50},
    {1.0f, 100},
    {2.0f, 50},
    // 其他游程...
};
Mat decoded_mat = rle_decode(rle_data, 200, 200);
```



22 辅助调试功能
Mat类提供了多种辅助调试功能，帮助开发者快速获取数据的形状信息和内存使用情况，便于调试和优化。

形状信息获取

```
// 获取元素位数
int elembits() const;

// 获取形状信息Mat
Mat shape() const;

// 判断是否为空
bool empty() const;
```

内存分析

```
// 计算总元素数
size_t total() const;

// 获取通道步长
size_t cstep;
```

使用示例

```
Mat img(224, 224, 3);

// 获取形状信息
Mat shape_info = img.shape();
printf("Shape total elements: %zu\n", shape_info.total());

// 检查是否为空
if (img.empty()) {
    printf("Mat is empty.\n");
} else {
    printf("Mat is not empty.\n");
}

// 输出通道步长
printf("Channel step: %zu\n", img.cstep);
```



## 23 总结

ncnn::Mat作为ncnn框架的基础数据结构，具备以下显著特点：

1 多维数据支持：支持1到4维的数据存储，涵盖向量、矩阵、张量等多种数据形式。
2 高效内存管理：通过引用计数和自定义分配器，确保内存的高效利用和安全性。
3 SIMD优化：支持多种指令集的SIMD优化，提升数据处理的并行性能。
4 丰富的数据操作接口：提供多种访问和操作方式，适应不同的数据处理需求。
5 跨平台兼容性：通过条件编译和导出符号控制，确保在不同硬件和平台上高效运行。
6 图像处理扩展：支持高级的图像旋转、仿射变换等操作，满足复杂的图像处理需求。
7 调试与分析工具：内置多种调试功能和性能分析工具，帮助开发者优化和调试代码。

合理使用ncnn::Mat类，可以显著提升深度学习推理的效率和性能，为开发高效的人工智能应用打下坚实的基础。

无论是在数据管理、内存优化还是高级数据处理上，Mat类都展现出了强大的功能和灵活性。希望这篇博客能帮助您更好地理解和利用ncnn::Mat，在您的深度学习项目中发挥更大的作用。