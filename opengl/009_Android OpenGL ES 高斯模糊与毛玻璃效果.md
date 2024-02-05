# Android OpenGL ES 高斯模糊与毛玻璃效果

发布于 2021-11-26 11:06:33

1.5K0

举报

文章被收录于专栏：[字节流动](https://cloud.tencent.com/developer/column/87404)

![img](D:\my-note\opengl\assets\2a1f1c4c4f1f3444c040ca67580f3d3f.jpeg)

#### 

#### **一、均值模糊**

所谓模糊，就是让图像看不清，那么让图片看不清有哪些方法呢

- **缩小图片** 缩小图片比较好理解，当我们将原本1080*960的图片，按照比例缩小为540*480，即缩小为原来的二分之一，但显示的时候，我们还是让他以1080*960大小显示，此时图片看起来就比原来的模糊
- **像素取周边像素的平均值** 当某张人像的图片非常看不清楚时，我们可能会说，这张照片的人眼睛鼻子糊成一块都看不清了，**其实也就是像素与像素之间变得平滑，而不是高清图的那种轮廓分明**

下面，我们正在在做图片模糊处理的时候，两种方法都需要结合起来使用

**像素取周边像素的平均值**

看下面的表格

![img](D:\my-note\opengl\assets\66721d2b6c2c3d954b0347a558e8ca69.jpeg)

假如这张表格上的数值是像素的值，那么可以看到，中心点像素的值是2，而周边的像素值是1（当然，这些值是笔者自定义的，你也可以自定义其他值），接下来要对中心点的像素做模糊处理，**使用均值模糊，将所有像素加起来，再除上总的个数**，最终得到的结果是

中心点像素 = (1 + 1 + 1 + ... + 2 + ... + 1 + 1 + 1)/9 = 1中心点像素=(1+1+1+...+2+...+1+1+1)/9=1

![img](D:\my-note\opengl\assets\3bcf327b1705071771c438a2d33fcb9c.jpeg)

可以看到，中心点的像素值从2变成了1，这完成了一个像素点的模糊处理，如果是整个图的话，需要遍历每一个元素，对每个元素做同样的模糊操作即可。

其实这里模糊的操作，**还涉及到一个概念，就是模糊半径blurRadius和模糊步长blurOffset，上图模糊半径是1，模糊步长也是1，模糊半径和模糊步长越大，则图片越模糊**

**OpenGLES 实现**

接下来使用OpenGLES实现均值模糊

众所周知，OpenGLES的片元着色器是对每个片元做处理，那么其实就可以在片元着色器中对每个像素做模糊处理

顶点着色器

```javascript
attribute vec4 aPos;
attribute vec2 aCoordinate;
varying vec2 vCoordinate;
void main(){
vCoordinate = aCoordinate;
gl_Position = aPos;
}
```

复制

```javascript

```

复制

**顶点着色器比较常规，未做任何特殊处理**

片元着色器

```javascript

```

复制

```javascript
precision mediump float;
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

复制

```javascript

```

复制

片元着色器相对来说就比较复杂，我们慢慢来分析

有两个uniform的变量，分别是

- uBlurRadius **int类型的变量，表示模糊半径**
- uBlurOffset vec2类型的变量，有两个分量x和y，**表示水平和垂直方向的模糊步长**

片元着色器里面自定义了一个**clampCoordinate**函数，该函数的作用是让纹理坐标保证在0-1的范围而不超出去。

接下来分析下主函数

- **vec4 sourceColor = texture2D(uSampler, vCoordinate);** 获取当前采样点的像素值
- **判断模糊半径是否小于等于1，是则不做模糊，直接返回**
- 新建一个vec4变量finalColor，表示最终的像素值
- **for循环中根据模糊半径和模糊步长获取周边的像素值**
- finalColor最终需要除上所有的像素个数

通过以上步骤我们就完成了均值模糊

有的人可能注意到了这里我只做了一个for循环，按照上图的分析，应该是有两个for循环嵌套才对啊，应该是下面的逻辑

```javascript
for (int i = 0; i < uBlurRadius; i++) {
    for (int j = 0; j < uBlurRadius; j++) {
        ......
    }
}
```

复制

```javascript

```

复制

其实这里的片元着色器只是做一个方向的模糊，外部调用时，分别做两次渲染（使用FBO），水平方向渲染一次uBlurOffset = vec2(blurOffsetW, 0.0)，垂直方向渲染一次uBlurOffset = vec2(0.0, blurOffsetH)。

那么为什么要这样做呢，**其实这样做主要是为了渲染的效率，因为如果用两个for循环，那么总的就得计算uBlurRadius \* uBlurRadius次**，而如果分为两次，则总的循环次数就变为uBlurRadius + uBlurRadius，渲染的效率可以得到大大的提升，特别是当模糊半径比较大的时候。

具体如何调用，这里代码就不提出来了，可到GitHub中查看 MeanBlurFilter

https://github.com/JYangkai/MediaDemo

实现效果

uBlurRadius = 30

uBlurOffset = 1

![img](D:\my-note\opengl\assets\c9b7def8a11fba0c910ac4cf63a78048.jpeg)

注意到，模糊的效果一般，当我们加大模糊步长至

uBlurRadius = 30

uBlurOffset = 5

则得到如下结果

![img](D:\my-note\opengl\assets\63de98f8232d64b5bd1a8ebd567bbd84.jpeg)

**啊，费了半天的努力，效果就这，就这？？？**

emmm，不慌，先来分析下为什么出现这种结果

其实上面uBlurOffset = 1的时候，效果就不怎么好，当到5的时候，效果更差，这**是因为**

**周边像素离当前像素越近，则说明它们之间的差异越大，而计算的时候，我们却不考虑该情况，直接计算平均值，这就导致了渲染出来的效果很差**。

那么，有方法解决吗，答案当然是有，接下来先来了解正态分布。

#### **二、正态分布**

> 正态分布（Normal distribution），也称“常态分布”，又名高斯分布（Gaussian distribution），最早由棣莫弗（Abraham de Moivre）在求二项分布的渐近公式中得到。C.F.高斯在研究测量误差时从另一个角度导出了它。P.S.拉普拉斯和高斯研究了它的性质。是一个在数学、物理及工程等领域都非常重要的概率分布，在统计学的许多方面有着重大的影响力。

概念性的了解即可，重要的是正态分布的一个公式，正态分布密度公式（μ = 0，即均值为

![img](D:\my-note\opengl\assets\49d60a49c22c73a73e3966bb01ed47a5.png)

正态分布图像

![img](D:\my-note\opengl\assets\2bec4d362e79df00ffbedacab1e94d91.jpeg)

那么得到该公式后，我们应该怎么做呢

还记得模糊半径吗，模糊半径就相当于x的取值范围，比如，uBlurRadius = 10，那么x的取值范围就是0-9

**x取值越大，则最终计算的结果就越小，也即，离中心像素点越远，关系就越小，反之越大**

**细心的人已经发现该公式里面有一个σ变量，其实σ表示标准差**

> 标准差（Standard Deviation） ，是离均差平方的算术平均数（即：方差）的算术平方根，用σ表示。标准差也被称为标准偏差，或者实验标准差，在概率统计中最常使用作为统计分布程度上的测量依据。 标准差是方差的算术平方根。标准差能反映一个数据集的离散程度。平均数相同的两组数据，标准差未必相同。

**从图像上看，σ越大，正态分布图像就越平坦，σ越小，则正态分布就会集中在中心位置，且越高**

下面开始进入正题，高斯模糊和毛玻璃的实现

#### **三、高斯模糊**

正态分布，也就高斯分布，利用正态分布的密度函数做模糊处理，则称为高斯模糊。

之前做均值模糊的时候，我们是将周边像素相加后取平均值，**高斯模糊同样也需要与周边像素相加并平均，只不过是使用加权平均。**

- **首先，根据正态分布的密度函数，计算出模糊半径内的所有权重**
- **因为要保证所有权重相加后为1，则需要让每个权重除上总的权重**
- **计算模糊的时候，采样得到的像素值需要乘上对应的权重**

**OpenGLES 实现**

顶点着色器

```javascript
attribute vec4 aPos;
attribute vec2 aCoordinate;
varying vec2 vCoordinate;
void main(){
    vCoordinate = aCoordinate;
    gl_Position = aPos;
}
```

复制

```javascript

```

复制

和均值模糊一样，顶点着色器未做任何特殊处理

片元着色器

```javascript
precision mediump float;
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

复制

```javascript

```

复制

大概一看，高斯模糊的片元着色器和均值模糊的片元着色器长得差不多，仔细读的话，会发现在for循环中有些许不同，**就是采样后的像素值，需要乘上对应权重**。

有一点需要注意的是，GLSL中，不能传入不定长的数组，**而当我们需要改变模糊半径时，得重新计算高斯模糊权重，所以这里笔者分为两个部分计算，****Java****部分根据模糊半径计算总权重值传入GLSL**，片元着色器中，根据for循环，计算对应的权重值，这样就可以满足我们的需求。

具体的调用代码这里就不贴出来了，可以到 GitHub 中查看 GaussianBlurFilter。

实现效果

uBlurRadius = 30

uBlurOffset = 1

![img](D:\my-note\opengl\assets\cb03a6d634d2afcc367f2f7f0aed3951.jpeg)

可以看到，对比均值模糊，高斯模糊看起来更自然，效果更好。

高斯模糊实现了，那么毛玻璃效果如何实现呢，其实只需要修改一些参数即可，比如增大模糊半径，模糊步长。

#### **四、毛玻璃**

上面已经实现了高斯模糊，我们可以通过增加模糊步长来实现毛玻璃效果，比如

uBlurRadius = 30

uBlurOffset = 5

![img](D:\my-note\opengl\assets\2a1f1c4c4f1f3444c040ca67580f3d3f.jpeg)

当然，我们也可以通过增加模糊半径来实现。

那么问题来了，**模糊半径可以一直增大吗，答案是不能，因为根据不同机型的性能，如果过大的增加模糊半径，则会造成画面卡顿，特别是实时渲染的时候**。

那么有解决方法吗，答案是有，**可以在做模糊之前，将纹理缩小几倍，因为本身模糊就不需要图片的细节，我们缩小后，不影响模糊的效果，缩小之后，渲染的效率就会得到提高，从而就可以增大模糊半径实现更加不错的效果。**

#### 源码：https://github.com/JYangkai/MediaDemo

作者：mirai

链接：https://juejin.cn/post/6975806731473387528

-- END --