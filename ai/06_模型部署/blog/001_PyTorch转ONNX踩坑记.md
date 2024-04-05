# PyTorch转ONNX踩坑记

## 背景：

使用PyTorch训练了一个文字检测器准备上线使用，我的网络中包含[Deformable Multi-Scale Attention](https://link.zhihu.com/?target=https%3A//github.com/fundamentalvision/Deformable-DETR)，是一个非官方Op。下面开始踩坑之旅。

### BTW：

问：直接用pth上线不行吗？为什么要转ONNX？

答：Python是一门解释型语言，直接用pth上线会导致 inference 效率很低。通过转ONNX可以实现动态图到静态图的转换，充分发挥编译带来的性能提升。

## 第一步：

按照 PyTorch [官方示例代码](https://link.zhihu.com/?target=https%3A//pytorch.org/tutorials/advanced/super_resolution_with_onnxruntime.html)进行转换，报错：

_any() takes 2 positional arguments but 4 were given.

参考这个[链接](https://link.zhihu.com/?target=https%3A//www.codeleading.com/article/90936323209/)，解决方案如下：

找到 torch/onnx/symbolic_opset9.py 中的如下代码

```python3
def _any(g, input):
    input = _cast_Long(g, input, False)  # type: ignore[name-defined]
    input_sum = sym_help._reducesum_helper(g, input, keepdims_i=0)
    return gt(g, input_sum, g.op("Constant", value_t=torch.LongTensor([0])))
 
def _all(g, input):
    return g.op("Not", _any(g, g.op("Not", input)))
```

替换为

```python3
def _any(g, *args):
    # aten::any(Tensor self)
    if len(args) == 1:
        input = args[0]
        dim, keepdim = None, 0
    # aten::any(Tensor self, int dim, bool keepdim)
    else:
        input, dim, keepdim = args
        dim = [_parse_arg(dim, "i")]
        keepdim = _parse_arg(keepdim, "i")
    input = _cast_Long(g, input, False)  # type: ignore[name-defined]
    input_sum = sym_help._reducesum_helper(g, input,
                                           axes_i=dim, keepdims_i=keepdim)
    return gt(g, input_sum, g.op("Constant", value_t=torch.LongTensor([0])))
 
def _all(g, *args):
    input = g.op("Not", args[0])
    # aten::all(Tensor self)
    if len(args) == 1:
        return g.op("Not", _any(g, input))
    # aten::all(Tensor self, int dim, bool keepdim)
    else:
        return g.op("Not", _any(g, input, args[1], args[2]))
```

## 第二步：

再次 export ONNX，报错：

This often indicates that the tracer has encountered untraceable code.

显然，这是因为第三方的 DeformMSAttention 造成的，PyTorch 无法 trace 数据流动。这里有两条路子可以走：

1. 为 DeformMSAttention 支持 PyTorch 的 trace。并且，还需要实现它的 ONNX Op。非常麻烦。
2. 将原先的 C++/CUDA 代码用纯 PyTorch 代码代替掉，这样就可以 trace 了

代码来自[这里](https://link.zhihu.com/?target=https%3A//github.com/mlpc-ucsd/TESTR/blob/main/adet/layers/ms_deform_attn.py%23L39)，粘贴如下：

```python3
def ms_deform_attn_core_pytorch(value, value_spatial_shapes, sampling_locations, attention_weights):
    # for debug and test only,
    # need to use cuda version instead
    N_, S_, M_, D_ = value.shape
    _, Lq_, M_, L_, P_, _ = sampling_locations.shape
    value_list = value.split([H_ * W_ for H_, W_ in value_spatial_shapes], dim=1)
    sampling_grids = 2 * sampling_locations - 1
    sampling_value_list = []
    for lid_, (H_, W_) in enumerate(value_spatial_shapes):
        # N_, H_*W_, M_, D_ -> N_, H_*W_, M_*D_ -> N_, M_*D_, H_*W_ -> N_*M_, D_, H_, W_
        value_l_ = value_list[lid_].flatten(2).transpose(1, 2).reshape(N_*M_, D_, H_, W_)
        # N_, Lq_, M_, P_, 2 -> N_, M_, Lq_, P_, 2 -> N_*M_, Lq_, P_, 2
        sampling_grid_l_ = sampling_grids[:, :, :, lid_].transpose(1, 2).flatten(0, 1)
        # N_*M_, D_, Lq_, P_
        sampling_value_l_ = F.grid_sample(value_l_, sampling_grid_l_,
                                          mode='bilinear', padding_mode='zeros', align_corners=False)
        sampling_value_list.append(sampling_value_l_)
    # (N_, Lq_, M_, L_, P_) -> (N_, M_, Lq_, L_, P_) -> (N_, M_, 1, Lq_, L_*P_)
    attention_weights = attention_weights.transpose(1, 2).reshape(N_*M_, 1, Lq_, L_*P_)
    output = (torch.stack(sampling_value_list, dim=-2).flatten(-2) * attention_weights).sum(-1).view(N_, M_*D_, Lq_)
    return output.transpose(1, 2).contiguous()
```

## 第三步：

继续 export ONNX，报错：

RuntimeError: Exporting the operator grid_sampler to ONNX opset version 11 is not supported.

这个报错的原因很简单。步骤二中添加的代码虽然是纯 PyTorch 实现，可以被 trace，但是 grid_sample 这个 Op 太新了，在我使用的 PyTorch 1.10.0 版本还没有添加到 ONNX opset。

本来这个问题已经不是问题了，因为 grid_sample 这个函数在最近发布的 PyTorch 1.12.0 中已经实现了支持，见[发布报告](https://link.zhihu.com/?target=https%3A//github.com/pytorch/pytorch/releases/tag/v1.12.0)。

但是坑爹的是，我的检测模型是基于 Detectron2 来实现的，Detectron2 已经一年多没有更新了。如[项目主页](https://link.zhihu.com/?target=https%3A//detectron2.readthedocs.io/en/latest/tutorials/install.html)所示，目前的 v0.6 版本最多只支持到 CUDA11.3 + PyTorch 1.10.0。

那么如何解决这个问题呢？这里还是有两条路子可以走

1. 在 PyTorch 1.12.0 以及适配的 TorchVision 0.13.0 版本下，自行 build detectron2

- 经尝试，可以成功 build，但在执行时报错，ImportError: cannot import name 'is_fx_tracing' from 'torch.fx._symbolic_trace'，不知如何解决。
- 替换掉 grid_sample 算子，见第四步

## 第四步

参考这个[链接](https://link.zhihu.com/?target=https%3A//blog.csdn.net/JoeyChen1219/article/details/121141318)，将 grid_sample 替换为 mmcv 里的 bilinear_grid_sample。附代码如下：

```text
def bilinear_grid_sample(im, grid, align_corners=False):
    """Given an input and a flow-field grid, computes the output using input
    values and pixel locations from grid. Supported only bilinear interpolation
    method to sample the input pixels.

    Args:
        im (torch.Tensor): Input feature map, shape (N, C, H, W)
        grid (torch.Tensor): Point coordinates, shape (N, Hg, Wg, 2)
        align_corners (bool): If set to True, the extrema (-1 and 1) are
            considered as referring to the center points of the input’s
            corner pixels. If set to False, they are instead considered as
            referring to the corner points of the input’s corner pixels,
            making the sampling more resolution agnostic.

    Returns:
        torch.Tensor: A tensor with sampled points, shape (N, C, Hg, Wg)
    """
    n, c, h, w = im.shape
    gn, gh, gw, _ = grid.shape
    assert n == gn

    x = grid[:, :, :, 0]
    y = grid[:, :, :, 1]

    if align_corners:
        x = ((x + 1) / 2) * (w - 1)
        y = ((y + 1) / 2) * (h - 1)
    else:
        x = ((x + 1) * w - 1) / 2
        y = ((y + 1) * h - 1) / 2

    x = x.view(n, -1)
    y = y.view(n, -1)

    x0 = torch.floor(x).long()
    y0 = torch.floor(y).long()
    x1 = x0 + 1
    y1 = y0 + 1

    wa = ((x1 - x) * (y1 - y)).unsqueeze(1)
    wb = ((x1 - x) * (y - y0)).unsqueeze(1)
    wc = ((x - x0) * (y1 - y)).unsqueeze(1)
    wd = ((x - x0) * (y - y0)).unsqueeze(1)

    # Apply default for grid_sample function zero padding
    im_padded = F.pad(im, pad=[1, 1, 1, 1], mode='constant', value=0)
    padded_h = h + 2
    padded_w = w + 2
    # save points positions after padding
    x0, x1, y0, y1 = x0 + 1, x1 + 1, y0 + 1, y1 + 1

    # Clip coordinates to padded image size
    x0 = torch.where(x0 < 0, torch.tensor(0), x0)
    x0 = torch.where(x0 > padded_w - 1, torch.tensor(padded_w - 1), x0)
    x1 = torch.where(x1 < 0, torch.tensor(0), x1)
    x1 = torch.where(x1 > padded_w - 1, torch.tensor(padded_w - 1), x1)
    y0 = torch.where(y0 < 0, torch.tensor(0), y0)
    y0 = torch.where(y0 > padded_h - 1, torch.tensor(padded_h - 1), y0)
    y1 = torch.where(y1 < 0, torch.tensor(0), y1)
    y1 = torch.where(y1 > padded_h - 1, torch.tensor(padded_h - 1), y1)

    im_padded = im_padded.view(n, c, -1)

    x0_y0 = (x0 + y0 * padded_w).unsqueeze(1).expand(-1, c, -1)
    x0_y1 = (x0 + y1 * padded_w).unsqueeze(1).expand(-1, c, -1)
    x1_y0 = (x1 + y0 * padded_w).unsqueeze(1).expand(-1, c, -1)
    x1_y1 = (x1 + y1 * padded_w).unsqueeze(1).expand(-1, c, -1)

    Ia = torch.gather(im_padded, 2, x0_y0)
    Ib = torch.gather(im_padded, 2, x0_y1)
    Ic = torch.gather(im_padded, 2, x1_y0)
    Id = torch.gather(im_padded, 2, x1_y1)

    return (Ia * wa + Ib * wb + Ic * wc + Id * wd).reshape(n, c, gh, gw)
```

## 第五步

继续 export ONNX，报错：

RuntimeError: view size is not compatible with input tensor's size and stride (at least one dimension spans across two contiguous subspaces). Use .reshape(...) instead.

解决方案是将

```text
    x = x.view(n, -1) 
    y = y.view(n, -1)
```

修改为

```text
    x = x.contiguous().view(n, -1) 
    y = y.contiguous().view(n, -1)
```

## 第六步

继续 export ONNX，报错：

RuntimeError: Expected all tensors to be on the same device, but found at least two devices, cuda:0 and cpu!

解决方案是将

```text
    x0 = torch.where(x0 < 0, torch.tensor(0), x0)
    x0 = torch.where(x0 > padded_w - 1, torch.tensor(padded_w - 1), x0)
    x1 = torch.where(x1 < 0, torch.tensor(0), x1)
    x1 = torch.where(x1 > padded_w - 1, torch.tensor(padded_w - 1), x1)
    y0 = torch.where(y0 < 0, torch.tensor(0), y0)
    y0 = torch.where(y0 > padded_h - 1, torch.tensor(padded_h - 1), y0)
    y1 = torch.where(y1 < 0, torch.tensor(0), y1)
    y1 = torch.where(y1 > padded_h - 1, torch.tensor(padded_h - 1), y1)
```

修改为

```text
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    x0 = torch.where(x0 < 0, torch.tensor(0).to(device), x0)
    x0 = torch.where(x0 > padded_w - 1, torch.tensor(padded_w - 1).to(device), x0)
    x1 = torch.where(x1 < 0, torch.tensor(0).to(device), x1)
    x1 = torch.where(x1 > padded_w - 1, torch.tensor(padded_w - 1).to(device), x1)
    y0 = torch.where(y0 < 0, torch.tensor(0).to(device), y0)
    y0 = torch.where(y0 > padded_h - 1, torch.tensor(padded_h - 1).to(device), y0)
    y1 = torch.where(y1 < 0, torch.tensor(0).to(device), y1)
    y1 = torch.where(y1 > padded_h - 1, torch.tensor(padded_h - 1).to(device), y1)
```

## Export 成功！