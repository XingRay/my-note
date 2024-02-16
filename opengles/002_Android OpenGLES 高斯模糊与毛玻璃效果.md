# Android OpenGLES 高斯模糊与毛玻璃效果

[mirai](https://juejin.cn/user/114004940844871/posts)

2021-06-204,976阅读7分钟

### 实现效果

![毛玻璃.png](D:\my-note\opengles\assets\e6e367b602e24ba4905a8dfd847ad849tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

### 一、均值模糊

所谓模糊，就是让图像看不清，那么让图片看不清有哪些方法呢

- 缩小图片

  缩小图片比较好理解，当我们将原本`1080*960`的图片，按照比例缩小为`540*480`，即缩小为原来的二分之一，但显示的时候，我们还是让他以`1080*960`大小显示，此时图片看起来就比原来的模糊

- 像素取周边像素的平均值

  当某张人像的图片非常看不清楚时，我们可能会说，这张照片的人眼睛鼻子糊成一块都看不清了，其实也就是像素与像素之间变得平滑，而不是高清图的那种轮廓分明

下面，我们正在在做图片模糊处理的时候，两种方法都需要结合起来使用

**像素取周边像素的平均值**

看下面的表格

![image-20210620155530699.png](D:\my-note\opengles\assets\6618dffaeb32499cad20f2088a4ef9fbtplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

假如这张表格上的数值是像素的值，那么可以看到，中心点像素的值是`2`，而周边的像素值是`1`（当然，这些值是笔者自定义的，你也可以自定义其他值），接下来要对中心点的像素做`模糊处理`，使用`均值模糊`，将所有像素`加起来`，再`除上`总的个数，最终得到的结果是

中心点像素=(1+1+1+...+2+...+1+1+1)/9=1中心点像素=(1+1+1+...+2+...+1+1+1)/9=1

![示例表格2.jpeg](D:\my-note\opengles\assets\77db01df79a54fcea9f4c9bd31b6eb0dtplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

可以看到，中心点的像素值从`2变成了1`，这完成了一个像素点的模糊处理，如果是整个图的话，需要遍历每一个元素，对每个元素做同样的模糊操作即可

其实这里模糊的操作，还涉及到一个概念，就是模糊半径`blurRadius`和模糊步长`blurOffset`，上图模糊半径是`1`，模糊步长也是`1`，模糊半径和模糊步长越大，则图片越模糊

**OpenGLES 实现**

接下来使用`OpenGLES`实现均值模糊

众所周知，`OpenGLES`的片元着色器是对`每个片元`做处理，那么其实就可以在片元着色器中对`每个像素`做`模糊处理`

***顶点着色器***

```c++
c++复制代码attribute vec4 aPos;
attribute vec2 aCoordinate;
varying vec2 vCoordinate;
void main(){
    vCoordinate = aCoordinate;
    gl_Position = aPos;
}
```

顶点着色器比较常规，未做任何特殊处理

***片元着色器***

```c++
c++复制代码precision mediump float;
uniform sampler2D uSampler;
varying vec2 vCoordinate;
uniform int uBlurRadius;
uniform vec2 uBlurOffset;

// 边界值处理
vec2 clampCoordinate(vec2 coordinate) {
    return vec2(clamp(coordinate.x, 0.0, 1.0), clamp(coordinate.y, 0.0, 1.0));
}

void main(){
    vec4 sourceColor = texture2D(uSampler, vCoordinate);

    if (uBlurRadius <= 1) {
        gl_FragColor = sourceColor;
        return;
    }

    vec3 finalColor = sourceColor.rgb;

    for (int i = 0; i < uBlurRadius; i++) {
        finalColor += texture2D(uSampler, clampCoordinate(vCoordinate + uBlurOffset * float(i))).rgb;
        finalColor += texture2D(uSampler, clampCoordinate(vCoordinate - uBlurOffset * float(i))).rgb;
    }

    finalColor /= float(2 * uBlurRadius + 1);

    gl_FragColor = vec4(finalColor, sourceColor.a);
}
```

片元着色器相对来说就比较复杂，我们慢慢来分析

有两个`uniform`的变量，分别是

- `uBlurRadius`

  `int`类型的变量，表示`模糊半径`

- `uBlurOffset`

  `vec2`类型的变量，有两个分量`x`和`y`，表示`水平`和`垂直`方向的`模糊步长`

片元着色器里面自定义了一个`clampCoordinate`函数，该函数的作用是让纹理坐标保证在`0-1`的范围而不超出去

接下来分析下`主函数`

- `vec4 sourceColor = texture2D(uSampler, vCoordinate);`

  获取`当前采样点`的`像素值`

- 判断模糊半径是否`小于等于1`，是则`不做模糊`，直接返回

- 新建一个`vec4`变量`finalColor`，表示最终的像素值

- `for循环`中根据`模糊半径`和`模糊步长`获取周边的`像素值`

- `finalColor`最终需要除上所有的像素个数

通过以上步骤我们就完成了`均值模糊`

有的人可能注意到了这里我只做了一个`for`循环，按照上图的分析，应该是有两个for循环嵌套才对啊，应该是下面的逻辑

```c++
c++复制代码for (int i = 0; i < uBlurRadius; i++) {
	for (int j = 0; j < uBlurRadius; j++) {
		......
	}
}
```

其实这里的片元着色器只是做一个方向的模糊，外部调用时，分别做两次渲染（使用`FBO`），水平方向渲染一次`uBlurOffset = vec2(blurOffsetW, 0.0)`，垂直方向渲染一次`uBlurOffset = vec2(0.0, blurOffsetH)`

那么为什么要这样做呢，其实这样做主要是为了渲染的效率，因为如果用两个`for循环`，那么总的就得计算`uBlurRadius * uBlurRadius`次，而如果分为两次，则总的循环次数就变为`uBlurRadius + uBlurRadius`，渲染的效率可以得到大大的提升，特别是当模糊半径比较大的时候

具体如何调用，这里代码就不提出来了，可到GitHub中查看[MeanBlurFilter](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2FJYangkai%2FMediaDemo%2Fblob%2Fmaster%2Fmedia%2Fsrc%2Fmain%2Fjava%2Fcom%2Fyk%2Fmedia%2Fopengles%2Frender%2Ffilter%2FMeanBlurFilter.java)

**实现效果**

```
uBlurRadius = 30
uBlurOffset = 1
```

![均值模糊1.png](D:\my-note\opengles\assets\5f55d2b3ed6e4a829bb63283ca9739ebtplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

注意到，模糊的效果一般，当我们加大模糊步长至

```
uBlurRadius = 30
uBlurOffset = 5
```

则得到如下结果

![均值模糊2.png](D:\my-note\opengles\assets\d6ac6b7baf4841b1b581817116b8df7btplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

啊，费了半天的努力，效果就这，就这？？？

emmm，不慌，先来分析下为什么出现这种结果

其实上面`uBlurOffset = 1`的时候，效果就不怎么好，当到5的时候，效果更差，这是因为

周边像素离当前像素越近，则说明它们之间的差异越大，而计算的时候，我们却不考虑该情况，直接计算平均值，这就导致了渲染出来的效果很差

那么，有方法解决吗，答案当然是有，接下来先来了解`正态分布`

### 二、正态分布

> 正态分布（Normal distribution），也称“常态分布”，又名[高斯分布](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E9%AB%98%E6%96%AF%E5%88%86%E5%B8%83%2F10145793)（Gaussian distribution），最早由[棣莫弗](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E6%A3%A3%E8%8E%AB%E5%BC%97)（Abraham de Moivre）在求[二项分布](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E4%BA%8C%E9%A1%B9%E5%88%86%E5%B8%83)的渐近公式中得到。C.F.高斯在研究测量误差时从另一个角度导出了它。P.S.拉普拉斯和高斯研究了它的性质。是一个在[数学](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E6%95%B0%E5%AD%A6%2F107037)、物理及工程等领域都非常重要的[概率](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E6%A6%82%E7%8E%87)分布，在统计学的许多方面有着重大的影响力。

概念性的了解即可，重要的是正态分布的一个公式，正态分布密度公式（`μ = 0`，即均值为`0`）

�(�)=12�����(−�22�2)*f*(*x*)=2*π**σ*1*e**x**p*(−2*σ*2*x*2)

**正态分布图像**

![正态分布图.jpeg](D:\my-note\opengles\assets\02c79d277f784fb788856df47ee114d7tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

那么得到该公式后，我们应该怎么做呢

还记得模糊半径吗，模糊半径就相当于x的取值范围，比如，`uBlurRadius = 10`，那么x的取值范围就是`0-9`

`x`取值越大，则最终计算的结果就越小，也即，离中心像素点越远，关系就越小，反之越大

细心的人已经发现该公式里面有一个`σ`变量，其实`σ`表示`标准差`

> 标准差（Standard Deviation） ，是离均差平方的算术[平均数](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E5%B9%B3%E5%9D%87%E6%95%B0%2F11031224)（即：方差）的[算术平方根](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E7%AE%97%E6%9C%AF%E5%B9%B3%E6%96%B9%E6%A0%B9%2F1944252)，用σ表示。标准差也被称为标准偏差，或者实验标准差，在[概率](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E6%A6%82%E7%8E%87%2F828845)统计中最常使用作为[统计分布](https://link.juejin.cn/?target=https%3A%2F%2Fbaike.baidu.com%2Fitem%2F%E7%BB%9F%E8%AE%A1%E5%88%86%E5%B8%83%2F8478867)程度上的测量依据。
>
> 标准差是方差的算术平方根。标准差能反映一个数据集的离散程度。平均数相同的两组数据，标准差未必相同。

从图像上看，`σ`越大，正态分布图像就`越平坦`，`σ`越小，则正态分布就会`集中`在`中心位置`，且`越高`

下面开始进入正题，`高斯模糊`和`毛玻璃`的实现

### 三、高斯模糊

```
正态分布`，也就`高斯分布`，利用正态分布的密度函数做模糊处理，则称为`高斯模糊
```

之前做`均值模糊`的时候，我们是将周边像素`相加后取平均值`，高斯模糊同样也需要与周边像素相加并平均，只不过是使用`加权平均`

- 首先，根据正态分布的`密度函数`，计算出模糊半径内的所有`权重`
- 因为要保证所有权重相加后为1，则需要让每个权重除上`总的权重`
- 计算模糊的时候，采样得到的像素值需要乘上对应的`权重`

**OpenGLES 实现**

***顶点着色器***

```c++
c++复制代码attribute vec4 aPos;
attribute vec2 aCoordinate;
varying vec2 vCoordinate;
void main(){
    vCoordinate = aCoordinate;
    gl_Position = aPos;
}
```

和`均值模糊`一样，顶点着色器未做任何特殊处理

***片元着色器***

```c++
c++复制代码precision mediump float;
uniform sampler2D uSampler;
varying vec2 vCoordinate;
// 模糊半径
uniform int uBlurRadius;
// 模糊步长
uniform vec2 uBlurOffset;
// 总权重
uniform float uSumWeight;
// PI
const float PI = 3.1415926;

// 边界值处理
vec2 clampCoordinate(vec2 coordinate) {
    return vec2(clamp(coordinate.x, 0.0, 1.0), clamp(coordinate.y, 0.0, 1.0));
}

// 计算权重
float getWeight(int i) {
    float sigma = float(uBlurRadius) / 3.0;
    return (1.0 / sqrt(2.0 * PI * sigma * sigma)) * exp(-float(i * i) / (2.0 * sigma * sigma)) / uSumWeight;
}

void main(){
    vec4 sourceColor = texture2D(uSampler, vCoordinate);

    if (uBlurRadius <= 1) {
        gl_FragColor = sourceColor;
        return;
    }

    float weight = getWeight(0);

    vec3 finalColor = sourceColor.rgb * weight;

    for (int i = 1; i < uBlurRadius; i++) {
        weight = getWeight(i);
        finalColor += texture2D(uSampler, clampCoordinate(vCoordinate - uBlurOffset * float(i))).rgb * weight;
        finalColor += texture2D(uSampler, clampCoordinate(vCoordinate + uBlurOffset * float(i))).rgb * weight;
    }

    gl_FragColor = vec4(finalColor, sourceColor.a);
}
```

大概一看，`高斯模糊`的片元着色器和`均值模糊`的片元着色器长得差不多，仔细读的话，会发现在`for循环`中有`些许不同`，就是采样后的像素值，需要`乘上对应权重`

有一点需要注意的是，`GLSL`中，不能传入不定长的数组，而当我们需要改变`模糊半径`时，得重新计算`高斯模糊权重`，所以这里笔者分为`两个部分`计算，`Java部分`根据模糊半径计算`总权重值`传入`GLSL`，`片元着色器`中，根据`for循环`，计算对应的`权重值`，这样就可以满足我们的需求

具体的调用代码这里就不贴出来了，可以到`GitHub`中查看[GaussianBlurFilter](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2FJYangkai%2FMediaDemo%2Fblob%2Fmaster%2Fmedia%2Fsrc%2Fmain%2Fjava%2Fcom%2Fyk%2Fmedia%2Fopengles%2Frender%2Ffilter%2FGaussianBlurFilter.java)

**实现效果**

```
uBlurRadius = 30
uBlurOffset = 1
```

![高斯模糊.png](D:\my-note\opengles\assets\a6dd94f5d1a14da49918c82df4acfa7etplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

可以看到，对比`均值模糊`，`高斯模糊`看起来更自然，效果更好

高斯模糊实现了，那么`毛玻璃`效果如何实现呢，其实只需要修改一些参数即可，比如`增大模糊半径，模糊步长`

### 四、毛玻璃

上面已经实现了高斯模糊，我们可以通过增加模糊步长来实现毛玻璃效果，比如

```
uBlurRadius = 30
uBlurOffset = 5
```

![毛玻璃.png](D:\my-note\opengles\assets\664b9a32e291435580625301ed0eb967tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

当然，我们也可以通过增加`模糊半径`来实现

那么问题来了，`模糊半径`可以一直增大吗，答案是`不能`，因为根据不同机型的`性能`，如果过大的增加`模糊半径`，则会造成`画面卡顿`，特别是实时渲染的时候

那么有解决方法吗，答案是有，可以在做模糊之前，将`纹理缩小几倍`，因为本身模糊就不需要图片的`细节`，我们`缩小`后，不影响模糊的效果，缩小之后，渲染的效率就会得到提高，从而就可以`增大模糊半径`实现更加不错的效果

### GitHub

欢迎Star，[MediaDemo](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2FJYangkai%2FMediaDemo)