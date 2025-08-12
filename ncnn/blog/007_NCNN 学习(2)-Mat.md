# NCNN 学习(2)-Mat

Mat 是 NCNN 中最重要数据结构之一，NCNN 的很多计算都会涉及到 Mat。



## 1 数据成员

Mat 的定义在 https://github.com/Tencent/ncnn/blob/master/src/mat.h。从代码中可以看到，Mat 有这样的几个主要数据成员：

    class NCNN_EXPORT Mat
    {
    public:
        void* data;
        int* refcount;
        size_t elemsize;
        int elempack;
        Allocator* allocator;
    
        int dims;
        int w;
        int h;
        int d;
        int c;
    
        size_t cstep;
    };
数据成员作用：

data：指向数据存放的内存地址
refcount：引用计数
elemsize：表示 Mat 中每一个元素的大小，Mat 中的元素是无类型的，只有一个元素大小的信息
elempack：通常用于 SIMD 的场景，可简单认为一个 SIMD 的寄存器里存了几个元素
allocator：Mat 对象的内存分配方法
dims：指示 Mat 实际有几个维度，最多允许 4 维
c/d/h/w：具体每个维度的值，其中 d 是指的 depth，通常在 3d 卷积中会用到
cstep：Mat 中的数据排布是 CHW 顺序，这个值表示了每两个 channel 之间的 stride



## 2 构造函数

Mat 提供了众多的构造函数：

```
Mat();

Mat(int w, size_t elemsize = 4u, Allocator* allocator = 0);
Mat(int w, int h, size_t elemsize = 4u, Allocator* allocator = 0);
Mat(int w, int h, int c, size_t elemsize = 4u, Allocator* allocator = 0);
Mat(int w, int h, int d, int c, size_t elemsize = 4u, Allocator* allocator = 0);

Mat(int w, size_t elemsize, int elempack, Allocator* allocator = 0);
Mat(int w, int h, size_t elemsize, int elempack, Allocator* allocator = 0);
Mat(int w, int h, int c, size_t elemsize, int elempack, Allocator* allocator = 0);
Mat(int w, int h, int d, int c, size_t elemsize, int elempack, Allocator* allocator = 0);

Mat(const Mat& m);

Mat(int w, void* data, size_t elemsize = 4u, Allocator* allocator = 0);
Mat(int w, int h, void* data, size_t elemsize = 4u, Allocator* allocator = 0);
Mat(int w, int h, int c, void* data, size_t elemsize = 4u, Allocator* allocator = 0);
Mat(int w, int h, int d, int c, void* data, size_t elemsize = 4u, Allocator* allocator = 0);

Mat(int w, void* data, size_t elemsize, int elempack, Allocator* allocator = 0);
Mat(int w, int h, void* data, size_t elemsize, int elempack, Allocator* allocator = 0);
Mat(int w, int h, int c, void* data, size_t elemsize, int elempack, Allocator* allocator = 0);
Mat(int w, int h, int d, int c, void* data, size_t elemsize, int elempack, Allocator* allocator = 0);
```

关于构造函数的说明：

Mat();和Mat(const Mat& m);分别是空构造函数拷贝构造函数
其余的构造函数每四个一组，分成四组。组内的 4 个分别是 dims=1,2,3,4 的构造函数
第一组：不用指明 elempack 的值，这时构造函数内会对其赋值 1，这一组构造函数会****进行内存分配
第二组：这一组指明了 elempack 的值，这一组构造函数会****进行内存分配
第三组：不用指明 elempack 的值，这时构造函数内会对其赋值 1，这一组构造函数使用外部数据指针，不会进行内存分配
第四组：这一组指明了 elempack 的值，这一组构造函数使用外部数据指针，不会进行内存分配
这里的内存分配，会使用 Mat 内部的 create 方法来进行的，例如对于 dims=3 的内存分配：

    void Mat::create(int _w, int _h, int _c, size_t _elemsize, Allocator* _allocator)
    {
        if (dims == 3 && w == _w && h == _h && c == _c && elemsize == _elemsize && elempack == 1 && allocator == _allocator)
            return;
    
        release();
    
        elemsize = _elemsize;
        elempack = 1;
        allocator = _allocator;
    
        dims = 3;
        w = _w;
        h = _h;
        d = 1;
        c = _c;
    
        cstep = alignSize((size_t)w * h * elemsize, 16) / elemsize;
    
        size_t totalsize = alignSize(total() * elemsize, 4);
        if (totalsize > 0)
        {
            if (allocator)
                data = allocator->fastMalloc(totalsize + (int)sizeof(*refcount));
            else
                data = fastMalloc(totalsize + (int)sizeof(*refcount));
        }
    
        if (data)
        {
            refcount = (int*)(((unsigned char*)data) + totalsize);
            *refcount = 1;
        }
    }
说明：

数据成员赋值，有些成员会有默认值，cstep 会按照 16 字节对齐
在allocator为空的情况下，会使用fastMalloc进行内存分配，这个函数很直接，使用posix_memalign或者malloc来进行内存分配；在allocator不为空的情况下，使用传入的分配器来进行分配。NCNN 中实现了PoolAllocator，是一个简单的内存池
给引用计数赋初值 1



## 3 数据排布和内存对齐

Mat 中的数据排布按照 CHW 的顺序，channel 是最外层的维度，w 是最内层的维度，并且 Mat 中的数据在 channel 这个维度做了内存对齐的，两个 channel 之间的间距用前面提到的 cstep 来表示：

```
cstep = alignSize((size_t)w * h * elemsize, 16) / elemsize;
```

这里对 cstep 做内存对齐的原因是 NCNN 的算子实现广泛使用了 SIMD 的指令来进行加速，例如 ARM 平台的 NEON 寄存器，位宽是 128bit，即 16 字节，在对数据进行 16 字节对齐之后，相应的 NEON 运算能获得更高的性能；在 x86 平台下的 avx/avx2/avx512/SSE 等对于内存对齐也都有相应的要求。

如果需要使用 HWC 的数据排布，有两种解决思路：

在具体 Layer 的 forward 之前，对数据进行一次重排，做一次CHW2HWC的操作，forward 之后再做一次数据重排，即HWC2CHW的操作，这种方案的效率偏低，因为有额外的重排开销
自定义一套 HWC 的 Mat，以及相应的 Net 和 Extractor，这个代码改动会比较多，但是可以避免上面所说的重排开销



## 4 引用计数

### 4.1 引用计数初始化

在前面提到了在 Mat 对象构造的过程中，会给引用计数分配内存，并初始化为 1。

这里需要注意引用计数使用的内存和 Mat 中存放的数据用的是同一块内存，可以看到，分配 Mat 对象内存的时候，多申请了(int)sizeof(*refcount)个字节，这个做法是一种技巧，会比单独分配一块内存更有效率，这和 modern C++ 中的shared_ptr的make_shared这种创建方式原理相同。

### 4.2 引用计数加1

Mat 内部使用了引用计数，这样 Mat 就像智能指针一样，可以对内存自动进行管理，拷贝构造函数的实现：

```
NCNN_FORCEINLINE Mat::Mat(const Mat& m) {
    if (refcount)
      NCNN_XADD(refcount, 1);
}
```

除了拷贝构造之外，赋值构造也会对引用计数加 1。上面代码中的NCNN_XADD是原子操作，保证refcount的加 1 操作在多线程情况下的正确性，NCNN_XADD的实现：

```
#if NCNN_THREADS
// exchange-add operation for atomic operations on reference counters
#if defined __riscv && !defined __riscv_atomic
// riscv target without A extension
static NCNN_FORCEINLINE int NCNN_XADD(int* addr, int delta) {
    int tmp = *addr;
    *addr += delta;
    return tmp;
}
#elif defined __INTEL_COMPILER && !(defined WIN32 || defined _WIN32)
// atomic increment on the linux version of the Intel(tm) compiler
#define NCNN_XADD(addr, delta) (int)_InterlockedExchangeAdd(const_cast<void*>(reinterpret_cast<volatile void*>(addr)), delta)
#elif defined __GNUC__
#if defined __clang__ && __clang_major__ >= 3 && !defined __ANDROID__ && !defined __EMSCRIPTEN__ && !defined(__CUDACC__)
#ifdef __ATOMIC_ACQ_REL
#define NCNN_XADD(addr, delta) __c11_atomic_fetch_add((_Atomic(int)*)(addr), delta, __ATOMIC_ACQ_REL)
#else
#define NCNN_XADD(addr, delta) __atomic_fetch_add((_Atomic(int)*)(addr), delta, 4)
#endif
#else
#if defined __ATOMIC_ACQ_REL && !defined __clang__
// version for gcc >= 4.7
#define NCNN_XADD(addr, delta) (int)__atomic_fetch_add((unsigned*)(addr), (unsigned)(delta), __ATOMIC_ACQ_REL)
#else
#define NCNN_XADD(addr, delta) (int)__sync_fetch_and_add((unsigned*)(addr), (unsigned)(delta))
#endif
#endif
#elif defined _MSC_VER && !defined RC_INVOKED
#define NCNN_XADD(addr, delta) (int)_InterlockedExchangeAdd((long volatile*)addr, delta)
#else
// thread-unsafe branch
static NCNN_FORCEINLINE int NCNN_XADD(int* addr, int delta)
{
    int tmp = *addr;
    *addr += delta;
    return tmp;
}
#endif
#else  // NCNN_THREADS
static NCNN_FORCEINLINE int NCNN_XADD(int* addr, int delta)
{
    int tmp = *addr;
    *addr += delta;
    return tmp;
}
#endif // NCNN_THREADS
```


这段代码里面 NCNN 做的主要工作就是判断所用平台或者所用编译器，然后调用相应的加 1 操作原子实现。只有 clang、gcc、windows compiler 提供了相应的原子加 1 函数，其他的情况都没有提供原子加，只适用于单线程的情况，非线程安全。



### 4.3 引用计数减 1

在 Mat 对象析构的时候，就会进行减 1 的操作，具体的实现被封装到了release这个函数中：

    NCNN_FORCEINLINE void Mat::release()
    {
        if (refcount && NCNN_XADD(refcount, -1) == 1)
        {
            if (allocator)
                allocator->fastFree(data);
            else
                fastFree(data);
        }
    
        data = 0;
    
        elemsize = 0;
        elempack = 0;
    
        dims = 0;
        w = 0;
        h = 0;
        d = 0;
        c = 0;
    
        cstep = 0;
    
        refcount = 0;
    }
release的过程仍然是使用NCNN_XADD宏，这里是进行加 -1 的操作，即减1，然后判断引用计数是否为 1。这里需要注意，NCNN_XADD的返回值是加或减之前的值，这样的话，如果NCNN_XADD的返回值为 1 的话，说明不再有人在使用这个对象，则调用fastFree来释放内存，以及把所有数据成员置为 0。



## 5 辅助函数

Mat 中有很多 helper function，来方便开发者使用。



### 5.1 内容填充

Mat 内部有一批名为fill的成员函数，可以对 Mat 的数据内容用指定的数据来进行填充，下面以整形填充为例：

    NCNN_FORCEINLINE void Mat::fill(int _v) {
        int size = (int)total();
        int* ptr = (int*)data;
    
        int i = 0;
    #if __ARM_NEON
        int32x4_t _c = vdupq_n_s32(_v);
        for (; i + 3 < size; i += 4) {
            vst1q_s32(ptr, _c);
            ptr += 4;
        }
    #endif // __ARM_NEON
        for (; i < size; i++) {
            *ptr++ = _v;
        }
    }
这里的实现通过__ARM_NEON这个条件编译宏来 enable NEON 的使用。在这个条件编译宏没有开启的情况下，通过第二个 for 循环来挨个进行 Naive 方式的赋值；在这个条件编译宏开启的情况下，则通过vst1q_s32这个 NEON 指令进行加速填充，vst1q_s32指令可以一次把一个int32x4_t大小的 vector 存入 memory 中。



### 5.2 归一化

这个功能指的是substract_mean_normalize这个函数，它是用来减均值除方差的，主要实现如下：

    void Mat::substract_mean_normalize(const float* mean_vals, const float* norm_vals) {
        Layer* op;
    
        // substract mean and normalize
        op = create_layer(LayerType::Scale);
        ...
        Mat weights[2];
        weights[0] = Mat(c);
        weights[1] = Mat(c);
        for (int q = 0; q < c; q++) {
            weights[0][q] = norm_vals[q];
            weights[1][q] = -mean_vals[q] * norm_vals[q];
        }
    
        op->load_model(ModelBinFromMatArray(weights));
        op->create_pipeline(opt);
        op->forward_inplace(*this, opt);
        op->destroy_pipeline(opt);
    
        delete op;
    }
这个函数会对mean_vals和norm_vals是否为 nullptr 来分别处理，上面的代码只包含了全部不为空的情况，这个功能通过 Scale 这个 Layer 实现的。



### 5.3 图像处理

这个功能对应一系列的静态函数，主要用来辅助处理图片，基本都以from_pixels和to_pixels开头，下面以from_pixels为例做一些简单说明。

简单来说，这个函数主要用来把cv::Mat中的数据（opencv读取的图片内容）转换到ncnn::Mat中：

    Mat Mat::from_pixels(const unsigned char* pixels, int type, int w, int h, Allocator* allocator) {
        int type_from = type & PIXEL_FORMAT_MASK;
    
        if (type_from == PIXEL_RGB || type_from == PIXEL_BGR) {
            return Mat::from_pixels(pixels, type, w, h, w * 3, allocator);
        } else if (type_from == PIXEL_GRAY) {
            return Mat::from_pixels(pixels, type, w, h, w * 1, allocator);
        } else if (type_from == PIXEL_RGBA || type_from == PIXEL_BGRA) {
            return Mat::from_pixels(pixels, type, w, h, w * 4, allocator);
        }
    
        // unknown convert type
        NCNN_LOGE("unknown convert type %d", type);
        return Mat();
    }
从上面的if/else分发可以看出来，这个函数主要处理的是灰度图（单通道）、RGB图（三通道）、RGBA图（四通道），这里以三通道处理为例，继续深入看一下，最终会调到下面这个函数：

    static int from_rgb2bgr(const unsigned char* rgb, int w, int h, int stride, Mat& m, Allocator* allocator) {
        m.create(w, h, 3, 4u, allocator);
    
        w = w * h;
        h = 1;
    
        float* ptr0 = m.channel(0);
        float* ptr1 = m.channel(1);
        float* ptr2 = m.channel(2);
    
    #if __ARM_NEON
        int nn = w >> 3;
        int remain = w - (nn << 3);
    #else
        int remain = w;
    #endif // __ARM_NEON
    
    #if __ARM_NEON
    #if __aarch64__
        for (; nn > 0; nn--) {
            uint8x8x3_t _rgb = vld3_u8(rgb);
            uint16x8_t _r16 = vmovl_u8(_rgb.val[0]);
            uint16x8_t _g16 = vmovl_u8(_rgb.val[1]);
            uint16x8_t _b16 = vmovl_u8(_rgb.val[2]);
    
            float32x4_t _rlow = vcvtq_f32_u32(vmovl_u16(vget_low_u16(_r16)));
            float32x4_t _rhigh = vcvtq_f32_u32(vmovl_u16(vget_high_u16(_r16)));
            float32x4_t _glow = vcvtq_f32_u32(vmovl_u16(vget_low_u16(_g16)));
            float32x4_t _ghigh = vcvtq_f32_u32(vmovl_u16(vget_high_u16(_g16)));
            float32x4_t _blow = vcvtq_f32_u32(vmovl_u16(vget_low_u16(_b16)));
            float32x4_t _bhigh = vcvtq_f32_u32(vmovl_u16(vget_high_u16(_b16)));
    
            vst1q_f32(ptr2, _rlow);
            vst1q_f32(ptr2 + 4, _rhigh);
            vst1q_f32(ptr1, _glow);
            vst1q_f32(ptr1 + 4, _ghigh);
            vst1q_f32(ptr0, _blow);
            vst1q_f32(ptr0 + 4, _bhigh);
    
            rgb += 3 * 8;
            ptr0 += 8;
            ptr1 += 8;
            ptr2 += 8;
        }
    #endif // __aarch64__
    #endif // __ARM_NEON
        for (; remain > 0; remain--) {
            *ptr0 = rgb[2];
            *ptr1 = rgb[1];
            *ptr2 = rgb[0];
    
            rgb += 3;
            ptr0++;
            ptr1++;
            ptr2++;
        }
    
        return 0;
    }

这段代码的主要过程：

通过调用 Mat 的create方法给 Mat 申请内存，shape 为 3hw，大小为 4x3xhxw bytes，乘以 4 是因为 Mat 中使用 float 来存储传进来的 rgb 或者 bgr 图
如果 NEON 功能是 enable 的，那就使用 NEON intrinsics 来进行加速，先使用vld3_u8读入 24 个字节，再使用vmovl_u8把每个 8bit 表示的数转为 16bit 表示，继续使用vmovl_u16把 16bit 表示的数转为 32bit 表示，再使用vcvtq_f32_u32把 32bit 整数转为浮点数，最后使用vst1q_f32把浮点数存入 Mat 对象中
最后一部分是一段 Naive 的 C++ 赋值实现，如果 NEON 是 enable 的，就用来处理 NEON 没有处理完的数，如果 NEON 是 disable 的，就用来处理所有的数