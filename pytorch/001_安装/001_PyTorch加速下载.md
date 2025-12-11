# PyTorch 加速下载，使用国内的镜像源（2025年9月）

PyTorch 是一个非常流行的深度学习框架，但是，官方的 PyTorch 下载地址在国外，访问速度很慢。

![img](https://pica.zhimg.com/v2-8fdbd6870182dd0b84a8b1eaae3188b0_1440w.jpg)

GPU 版本的索引是 PyTorch 自家的



由于众所周知的原因，下载速度和链接很不稳定。网上给的答案都是去使用[阿里云镜像](https://zhida.zhihu.com/search?content_id=258075695&content_type=Article&match_order=1&q=阿里云镜像&zhida_source=entity)，然而阿里云的镜像好久没更新了，根本没法满足现在的需求。

![img](https://pic3.zhimg.com/v2-454e6559cacdba79afe8dd722e025d12_1440w.jpg)

甚至还有拿 AI 洗稿的，清华和上交根本没有 PyTorch 镜像源……



所以真的没办法了吗？

**使用南京大学镜像源**
这里介绍一个比较冷门但质量稳定的镜像源：**[南京大学开源镜像站](https://zhida.zhihu.com/search?content_id=258075695&content_type=Article&match_order=1&q=南京大学开源镜像站&zhida_source=entity)**。它们一直在同步官方最新的 PyTorch 镜像源，并且速度非常快。

![img](https://picx.zhimg.com/v2-2e5f016f0b51b13e16dbd505a246452f_1440w.jpg)

咱也不知道为啥同步失败了，但总归够用了



使用方法就是将安装命令中的 [https://download.pytorch.org/whl/](https://link.zhihu.com/?target=https%3A//download.pytorch.org/whl/) 替换为 [https://mirrors.nju.edu.cn/pytorch/whl/](https://link.zhihu.com/?target=https%3A//mirrors.nju.edu.cn/pytorch/whl/)。

比如，你要安装 [CUDA 12.6](https://zhida.zhihu.com/search?content_id=258075695&content_type=Article&match_order=1&q=CUDA+12.6&zhida_source=entity) 的 PyTorch，官方的安装命令是：

```bash
pip3 install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu126
```

只需要改成：

```bash
pip3 install torch torchvision torchaudio --index-url https://mirrors.nju.edu.cn/pytorch/whl/cu126
```

