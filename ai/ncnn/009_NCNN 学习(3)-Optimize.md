# NCNN 学习(3)-Optimize

NCNN 的 tools 中包含对模型进行优化的代码，主要是tools/modelwriter.h和tools/ncnnoptimize.cpp 这两个文件。

这两个文件里主要是ModelWriter和NetOptimize两个类，ModelWriter继承自Net，NetOptimize继承自ModelWriter。这里Net是 NCNN 的基础数据结构之一，用来抽象和管理 NCNN 的模型中的不变部分，主要是模型结构和模型参数。

ModelWriter主要是为了做模型优化添加的一个辅助类，主要用于输出模型，NetOptimize是 ncnn 用来做模型优化的主要的一个类，基本上所有的优化 pass，都是在这个类中实现。

ncnnoptimize的使用方法：

```
ncnnoptimize [inparam] [inbin] [outparam] [outbin] [flag] [cutstart] [cutend]

```

前四个参数分别是输入输出的模型相关文件名;
第五个参数是存储格式，0 表示 fp32，1 表示 fp16；
最后两个参数可以通过指定起止 layer name，来保存优化后模型的一个 subgraph 出来。
NCNN 的优化主要有 fuse、replace、eliminate 这三类。



## 1 fuse

NCNN 的融合主要是以 conv 类的算子为主，主要的融合组合包含以下三类：

bn + scale

```
bn + scale --> bn
optimizer.fuse_batchnorm_scale();

```

conv/deconv/dwconv + bn/mul/add

```
conv + bn --> conv
optimizer.fuse_convolution_batchnorm();

conv + mul --> conv
optimizer.fuse_convolution_mul();

conv + add --> conv
optimizer.fuse_convolution_add();

deconv + bn --> deconv
optimizer.fuse_deconvolution_batchnorm();

deconv + mul --> deconv
optimizer.fuse_deconvolution_mul();

deconv + add --> deconv
optimizer.fuse_deconvolution_add();

dwconv + bn --> dwconv
optimizer.fuse_convolutiondepthwise_batchnorm();

dwconv + mul --> dwconv
optimizer.fuse_convolutiondepthwise_mul();

dwconv + add --> dwconv
optimizer.fuse_convolutiondepthwise_add();

```

conv/deconv/dwconv + act_func(relu, clip, sigmoid, …)

```
conv + act_func --> conv
optimizer.fuse_deconvolution_activation();

deconv + act_func --> deconv
optimizer.fuse_deconvolution_activation();

dwconv + act_func --> dwconv
optimizer.fuse_deconvolutiondepthwise_activation();

```

以 bn 和 scale 的融合为例，看下是怎么做的融合：

```
int NetOptimize::fuse_batchnorm_scale() {
    const size_t layer_count = layers.size();
    // 遍历所有的layer
    for (size_t i = 0; i < layer_count; i++) {
        // 只care类型为BatchNorm的layer
        if (layers[i]->type != "BatchNorm")
            continue;
        // BatchNorm - Scale
        int top_blob_index = layers[i]->tops[0];
        // 从找到的BatchNorm layer开始，找后面为Scale的layer
        size_t j = i + 1;
        for (; j < layer_count; j++) {
            if (layers[j]->type != "Scale")
                continue;
            if (layers[j]->bottoms.size() != 1)
                continue;
            if (layers[j]->bottoms[0] == top_blob_index)
                break;
        }
        // 没找到Scale layer
        if (j == layer_count)
            continue;
        // fuse BatchNorm - Scale to BatchNorm
        ncnn::BatchNorm* batchnorm = (ncnn::BatchNorm*)layers[i];
        ncnn::Scale* scale = (ncnn::Scale*)layers[j];
        int channels = batchnorm->channels;
        float* slope = batchnorm->slope_data;
        float* bias = batchnorm->bias_data;
        // 把Scale中的参数合并到BatchNorm的参数中
        for (int q = 0; q < channels; q++) {
            slope[q] = slope[q] * scale->scale_data[q];
            if (scale->bias_term)
                bias[q] = bias[q] * scale->scale_data[q] + scale->bias_data[q];
            else
                bias[q] = bias[q] * scale->scale_data[q];
        }
        // 更新融合后节点的输入输出连接，以及融合后的layer type
        int top_blob_index_final = scale->tops[0];
        batchnorm->tops[0] = top_blob_index_final;
        blobs[top_blob_index_final].producer = i;
        scale->type = "ncnnfused";
    }
    return 0;
}

```

上面 fuse 的主要步骤:

找到 layer type 为BatchNorm的 layer
继续找后继 layer 为Scale的 layer
把Scale中的参数合并到BatchNorm中
更新 merge 后的 layer 的连接关系以及Scale的 layer type 为 ncnnfused



## 2 replace

这类优化是用来把一种 layer 替换为另一种，主要的优化组合如下：

```
mean-reduce + mean_reduce --> global_average_pooling
optimizer.replace_reduction_with_global_pooling();

prelu --> leaky_relu
optimizer.replace_prelu_with_leaky_relu();

global_average_pooling + conv --> global_average_pooling + inner_product
optimizer.replace_convolution_with_innerproduct_after_global_pooling();

inner_product + conv --> inner_conv + inner_conv
optimizer.replace_convolution_with_innerproduct_after_innerproduct();

```

mean_reduce 做 replace 的过程：

```
int NetOptimize::replace_reduction_with_global_pooling() {
    // 遍历所有的layer
    const size_t layer_count = layers.size();
    for (size_t i = 0; i < layer_count; i++) {
        // 只care类型为Reduction的layer，且Reduction的类型必须是mean reduce，
        // 且不能是reduce_all，以及reduce的coeff值必须为1
        if (layers[i]->type != "Reduction")
            continue;
        ncnn::Reduction* reduction1 = (ncnn::Reduction*)layers[i];
        if (reduction1->operation != 3 || reduction1->reduce_all != 0 || reduction1->coeff != 1.f)
            continue;
        // 只能reduce一个维度，且必须是2或者3
        if (reduction1->axes.w != 1)
            continue;
        const int* axes_ptr = reduction1->axes;
        if (axes_ptr[0] != 2 && axes_ptr[0] != 3)
            continue;
        // Reduction(2/3) - Reduction(2)
        int top_blob_index = layers[i]->tops[0];
        // 寻找下一个Reduction layer，只有和上面找到的的Reduction layer紧挨着才满足条件
        size_t j = i + 1;
        for (; j < layer_count; j++) {
            if (layers[j]->type != "Reduction")
                continue;
            if (layers[j]->bottoms.size() != 1)
                continue;
            if (layers[j]->bottoms[0] == top_blob_index)
                break;
        }
        if (j == layer_count)
            continue;
        ncnn::Reduction* reduction2 = (ncnn::Reduction*)layers[j];
        if (reduction2->operation != 3 || reduction2->reduce_all != 0 || reduction2->coeff != 1.f)
            continue;
        if (reduction2->axes.w != 1)
            continue;
        const int* axes2_ptr = reduction2->axes;
        if (axes2_ptr[0] != 2)
            continue;
        fprintf(stderr, "replace_reduction_with_global_pooling %s %s\n", reduction1->name.c_str(), reduction2->name.c_str());
        // 创建一个pooling layer，将其type设为average pooling，且是global pooling
        ncnn::Pooling* pooling = (ncnn::Pooling*)ncnn::create_layer("Pooling");
        pooling->type = "Pooling";
        pooling->name = reduction2->name;
        pooling->bottoms = reduction2->bottoms;
        pooling->tops = reduction2->tops;
        ncnn::ParamDict pd;
        pooling->load_param(pd);
        pooling->pooling_type = 1;
        pooling->global_pooling = 1;
        // 删除第二个reduction layer，将创建的pooling节点插入到图中，将第一个reduction layer标记为ncnnfused
        layers[j] = pooling;
        delete reduction2;
        int bottom_blob_index_final = reduction1->bottoms[0];
        pooling->bottoms[0] = bottom_blob_index_final;
        blobs[bottom_blob_index_final].consumer = j;
        reduction1->type = "ncnnfused";
    }
    return 0;
}

```

找到layer type为Reduction的layer，它还必须满足这些条件：
Reduction的类型必须是mean reduce
且不能是reduce_all
reduce的coeff值必须为1
只能reduce一个维度，且必须是2或者3
继续找后继layer为Reduction的layer，同样需要满足上面的条件
创建一个pooling layer，将其type设为average pooling，且是global pooling
删除第二个reduction layer，将创建的pooling节点插入到图中，将第一个reduction layer标记为ncnnfused



## 3 eliminate

这类优化是用来删除某些layer，如下的结构：

```
deopout
optimizer.eliminate_dropout();

1x1_pooling
optimizer.eliminate_pooling1x1();

noop
optimizer.eliminate_noop();

split
optimizer.eliminate_split();

global_pooling + flatten --> global_pooling
optimizer.eliminate_flatten_after_global_pooling();

global_pooling + reshape --> global_pooling
optimizer.eliminate_reshape_after_global_pooling();

reshape + binary_op --> binare_op
optimizer.eliminate_reshape_before_binaryop();

inner_product + flatten --> inner_product
optimizer.eliminate_flatten_after_innerproduct();

```

split 做 eliminate 的过程：

```
int NetOptimize::eliminate_split() {
    // 遍历所有的layer
    const size_t layer_count = layers.size();
    for (size_t i = 0; i < layer_count; i++) {
        // 只care类型为Split的layer，
        if (layers[i]->type != "Split")
            continue;
        ncnn::Layer* split = layers[i];
        // 判断这个split layer的输出只有为1、且有consumer的情况下才符合条件
        int real_split_output_count = 0;
        int real_split_top_blob_index = -1;
        size_t top_blob_count = split->tops.size();
        for (size_t j = 0; j < top_blob_count; j++) {
            int top_blob_index_final = split->tops[j];
            if (blobs[top_blob_index_final].consumer != -1) {
                real_split_output_count += 1;
                real_split_top_blob_index = j;
            }
        }
        if (real_split_output_count > 1)
            continue;
        // 寻找split的输入
        int bottom_blob_index = split->bottoms[0];
        int top_i = -1;
        int j = i - 1;
        for (; j >= 0; j--) {
            if (layers[j]->type == "ncnnfused")
                continue;
            for (size_t k = 0; k < layers[j]->tops.size(); k++) {
                if (layers[j]->tops[k] == bottom_blob_index) {
                    top_i = k;
                    break;
                }
            }
            if (top_i != -1)
                break;
        }
        if (j == -1)
            continue;
        // 将split的输入直接连接到split的输出上，将split标记为ncnnfused
        ncnn::Layer* any = layers[j];
        int top_blob_index_final = split->tops[real_split_top_blob_index];
        any->tops[top_i] = top_blob_index_final;
        blobs[top_blob_index_final].producer = j;
        split->type = "ncnnfused";
    }
    return 0;
}

```

找到 layer type 为 Split 的 layer，它还必须满足这些条件：
split layer 的输出只有为 1
输出 blob 有 consumer
寻找 split layer 的输入
将 split 的输入直接连接到 split 的输出上，将 split 标记为 ncnnfused



## 4 内存估计

ncnnoptimize 通过下面函数来实现估计 memory 的用量，

```
optimizer.estimate_memory_footprint();

```

这个函数是前面的提到的 ModelWriter 类中的一个成员函数，它通过使用了下面这个自定义的 Allocator 来做统计：

    class MemoryFootprintAllocator : public ncnn::Allocator {
    public:
        MemoryFootprintAllocator() {
            current_memory_usage = 0;
            memory_footprint = 0;
        }
    
        virtual void* fastMalloc(size_t size) {
            ncnn::MutexLockGuard g(lock);
            void* ptr = ncnn::fastMalloc(size);
            bookkeeper[ptr] = size;
            current_memory_usage += size;
            memory_footprint = std::max(memory_footprint, current_memory_usage);
            return ptr;
        }
        virtual void fastFree(void* ptr) {
            ncnn::MutexLockGuard g(lock);
            size_t size = bookkeeper[ptr];
            current_memory_usage -= size;
            bookkeeper.erase(bookkeeper.find(ptr));
            ncnn::fastFree(ptr);
        }
    public:
        int current_memory_usage;
        int memory_footprint;
        ncnn::Mutex lock;
        std::map<void*, size_t> bookkeeper;
    };
    
current_memory_usage 统计的是模型推理过程中要求分配的内存总量
memory_footprint 统计的是模型推理过程中实际分配的内存总量，因为 fastMalloc 调用的是系统的 malloc 来做的内存管理，系统的 malloc 实际上是通过内存池来实现的
在 estimate_memory_footprint 函数中做一遍推理，就可以把上面的数据统计出来：

    int ModelWriter::estimate_memory_footprint() {
        if (has_custom_layer) {
            fprintf(stderr, "model has custom layer, estimate_memory_footprint skipped\n");
            return -1;
        }
        const size_t layer_count = layers.size();
        const size_t blob_count = blobs.size();
        // 1. 创建用于统计内存使用的memory allocator
        // 2. 创建用于模型推理的extractor，且设置为light mode，即不用的内存会被释放掉
        MemoryFootprintAllocator allocator;
        ncnn::Extractor ex = create_extractor();
        ex.set_light_mode(true);
        ex.set_blob_allocator(&allocator);
        ex.set_workspace_allocator(&allocator);
    
        // prepare Input blobs
        for (size_t i = 0; i < layer_count; i++) {
            const ncnn::Layer* layer = layers[i];
            if (layer->type == "ncnnfused")
                continue;
            if (layer->type != "Input")
                continue;
            ncnn::Input* input = (ncnn::Input*)layer;
            int w = input->w;
            int h = input->h;
            int c = input->c;
            int dims = 0;
            if (w == 0 && h == 0 && c == 0) dims = 0;
            if (w != 0 && h == 0 && c == 0) dims = 1;
            if (w != 0 && h != 0 && c == 0) dims = 2;
            if (w != 0 && h != 0 && c != 0) dims = 3;
            if (dims == 0) {
                fprintf(stderr, "Input layer %s without shape info, estimate_memory_footprint skipped\n", layer->name.c_str());
                return -1;
            }
            // 使用假输入数据作为模型输入
            ncnn::Mat m;
            if (dims == 1) m.create(w, 4u, &allocator);
            if (dims == 2) m.create(w, h, 4u, &allocator);
            if (dims == 3) m.create(w, h, c, 4u, &allocator);
            ex.input(layer->tops[0], m);
            fprintf(stderr, "input = %s\n", blobs[layer->tops[0]].name.c_str());
        }
        // find output blobs and do inference
        std::vector<ncnn::Mat> outputs;
        for (size_t i = 0; i < blob_count; i++) {
            const ncnn::Blob& blob = blobs[i];
            if (blob.producer == -1 || blob.consumer != -1)
                continue;
            if (layers[blob.producer]->type == "ncnnfused")
                continue;
            // treat blob without any consumers as output
            ncnn::Mat m;
            ex.extract(int(i), m);
            outputs.push_back(m);
            fprintf(stderr, "extract = %s\n", blob.name.c_str());
        }
        fprintf(stderr, "estimated memory footprint = %.2f KB = %.2f MB\n", allocator.memory_footprint / 1024.f, allocator.memory_footprint / 1024.f / 1024.f);
        return 0;
    }
    

