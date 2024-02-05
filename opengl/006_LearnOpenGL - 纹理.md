# LearnOpenGL - 纹理



## 1 了解纹理

简而言之就是一张图，把他附着在一个物体上，让这个物体看起来更逼真。说白了，OpenGL不就是为了渲染更真实的物体，让虚拟的物体在屏幕上看起来更像一个真实的物件，但是前面说了，计算机绘制就会画点线三角形，而那些特别复杂的形状无非就是成千上万的三角形组成的，完了我们在这每一个三角形上上一上色，贴上贴纸让这个物体看起来更真实，而这些贴纸就是纹理。其实就是让一堆计算机形状看起来更真实。



### 1.1 先看一下图像占用内存大小的计算方式

图像占用的存储空间 = 图像的高度 x 图像的宽度 x 每个像素点的字节数

每个像素占用的字节数由系统决定

在32位机器上，如果数据在内存中按照32位的边界对齐（地址为4字节的倍数），那么硬件提取数据的速度就会快得多，同样在64位机器上，如数据地址按照8字节对齐，他对数据存取效率会非常高。

譬如：如果一个图像每行有99个像素，每个像素有RGB 3个颜色通道，那么该图像每行需要的存储空间为：99*3 = 297个字节。按照默认的4字节对齐，可以算出，每行实际分配的存储空间为 300个字节，虽然这会浪费存储空间，但会提升CPU抓取数据的效率。

在许多硬件平台上，考虑到性能的原因位图和像素图的每一行的数据会从特殊的字节对齐地址开始。绝大多数编译器会自动把变量和缓冲区放置在当前计算机架构优化的对齐地址上。OpenGL默认是4字节对齐的，可以通过glPixelStorei来设置像素的存储方式，通过glPixelStoref来恢复像素的存储方式

```
//改变像素存储方式
void glPixelStorei(GLenum pname, GLint param)
//恢复像素存储方式
void glPxielStoref (GLenum pname, GLFloat param)
参数1：GL_UNPACK_ALIGNMENT,指定OpenGL如何从数据缓存区中解包图像数据；
参数2：表示参数GL_UNPACK_ALIGNMENT设置的值。GL_UNPACK_ALIGNMENT指内存中的每一个像素行起点的排列请求，允许设置为1（byte排列）、2（排列为偶数byte的行）、4（字word排列）8（行从双字节边界开始）
例：glPxielStorei(GL_UNPACK_ALIGNMENT, 1)
```

```
从颜色缓冲区内容作为像素图直接读取
void glReadPixels(GLint x,GLint y,GLSizei width,GLSizei height, GLenum format, GLenum type,const void * pixels);
//参数1：x,矩形左下⻆的窗口坐标
//参数2：y,矩形左下⻆的窗口坐标
//参数3：width,矩形的宽，以像素为单位
//参数4：height,矩形的高，以像素为单位
//参数5：format,OpenGL 的像素格式，参考下表1
//参数6：type,解释参数pixels指向的数据，告诉OpenGL 使用缓存区中的什么 数据类型来存储颜色分量，像素数据的数据类型，参考下表2
//参数7：pixels,指向图形数据的指针
```

![OpenGL像素格式](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70.png)

![在这里插入图片描述](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706278782467-3.png)

纹理坐标

我们将图片贴到三角形上去时，可想而知是需要告诉OpenGL怎么去对应坐标的。

纹理坐标系以纹理左下角为坐标原点，向右为x正轴方向，向上为y轴正轴方向。他的总长度是1。即纹理图片的四个角的坐标分别是：(0,0)、(1,0)、(0,1)、(1,1)，分别对应左下、右下、左上、右上四个顶点。二维纹理常用(s, t)坐标表示:

![OpenGL二位纹理坐标](D:\my-note\opengl\assets\20200418234018598.png)



```
//设置纹理坐标
//注意这里的参数1：texture，纹理层次，对于使用存储着色器来进行渲染，设置为0
//后面两个参数对应 x y
void MultiTexCoord2f(GLuint texture, GLclampf s, GLclampf t);
```

如何把纹理坐标应用到三角形上？其纹理坐标就是：

```
GLfloat texCoords[] = {
0.0f, 0.0f, // 左下角
1.0f, 0.0f, // 右下角
0.5f, 1.0f // 顶部位置
};
```

![在这里插入图片描述](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706278835914-8.png)



载入纹理图像

```
void glTexImage1D（GLenum target ， GLint level， GLint internalformat， GLsizei width， GLint border， GLenum format， GLenum type， void*data）
```

```
void glTexImage2D（GLenum target ， GLint level， GLint internalformat， GLsizei width， GLsizei height， GLint border， GLenum format， GLenum type， void*data）
```

```
void glTexImage3D（GLenum target ， GLint level， GLint internalformat， GLsizei width， GLsizei height，GLsizei depth， GLint border， GLenum format， GLenum type， void*data）
```

```
参数1：target：GL_TEXTURE_2D,GL_TEXTURE_1D, GL_TEXTURE_3D
参数2：level指定所加载的mip贴图层次，一般我们都把这个参数设置为0.
参数3：internalformat：每个纹理单元中存储多少颜色成分。
参数4：width，height，depth参数：指的是加载纹理的宽度、高度、深度；需要注意的是这些数必须是2的整数次方。这个因为OpenGL旧版本上遗留下的一个要求，当然现在已经支持可以不是2的整数次方，但是开发者已经习惯使用2的证书此房去设置这些参数。
参数5：border：允许为纹理贴图制定一个边界宽度
参数6：format、type、data参数与glDrawPxiels函数对于的参数相同。
```



更新纹理

```
void glTexSubImage1D（GLenum target，GLint level，GLint xOffset， GLint yoffset，GLSizei width， GLenum format， GLenum type，const GLvoid * data）
```

```
void glTexSubImage2D（GLenum target，GLint level，GLint xOffset， GLint yoffset，GLSizei width， GLSizei height， GLenum format， GLenum type，const GLvoid * data）
```

```
void glTexSubImage3D（GLenum target，GLint level，GLint xOffset， GLint yoffset，GLSizei width， GLSizei height，GLsizei depth， GLenum format， GLenum type，const GLvoid * data）
```

1D，2D，3D的区别只在于3D比1D多了depth.height，2D只比1D多了height



插入替换纹理

```
void glCopyTexSubImage1D(GLenum target, GLint level, GLint xoffset,GLsizei width,GLsizei height)
```

```
void glCopyTexSubImage2D(GLenum target, GLint level, GLint xoffset, GLint yOffset,GLsizei width,GLsizei height)
```

```
void glCopyTexSubImage2D(GLenum target, GLint level, GLint xoffset， GLint yOffset,GLint zOffset,GLsizei width,GLsizei height)
```

1D，2D，3D的区别只在于1D只有xOffset，2D有xOffset yOffset，3D比2D多了zOffset



### 使用颜色缓冲区加载数据，形成新的纹理

```
void glCopyTexImage1D(GLenum target, GLint level, GLenum internalformat, GLint x, GLint y,GLsizei width, GLint border)
```

```
void glCopyTexImage2D(GLenum target, GLint level, GLenum internalformat, GLint x, GLint y,GLsizei width, GLsizei height, GLint border)
```

x,y在颜色缓冲区中指定了开始读取纹理数据的位置：缓冲区里面的数据是源缓冲区通过glReadBuffer设置的
注意：不存在glCopyTexImage3D，因为我们无法从2D颜色缓冲区中获取体积数据



### 使用函数分配纹理对象*

指定纹理对象的数量和指针，（指针指向一个无符号整形数据，由纹理对象标识符填充）

```
void glGenTexTures（GLsizei n，GLuint * textures）；
```



### 绑定纹理状态*

```
void glBindTexture（GLenum target， GLunit texture）；
```

参数1：target：GL_TEXTURE_2D,GL_TEXTURE_1D, GL_TEXTURE_3D
参数2：需要绑定的纹理对象



### 删除绑定纹理对象*

```
void glDeleteTexture（GLsizei n， GLuint * texture）；
```

纹理对象以及纹理对象指针（指针指向一个无符号整形数组，由纹理对象标识符填充）



### 测试纹理对象是否有效

如果texture是一个已经分配空间的纹理对象，那么这个函数会返回GL_TURE，否则会返回GL_FALST

```
GLboolean glIsTexture（GLuint texture）；
```



3 设置纹理贴图参数

```
glTextureParameter（GLenum target， GLenum pname， GLFloat param）；
```

```
glTextureParameter（GLenum target， GLenum pname， GLint param）；
```

```
glTextureParameter（GLenum target， GLenum pname， GLFloat * param）；
```

```
glTextureParameter（GLenum target， GLenum pname， GLint *param）
```

参数1： target，指定这些参数应用哪个纹理模式上，比如GL_TEXTURE_2D,GL_TEXTURE_1D, GL_TEXTURE_3D
参数2： pname，指定需要设置哪个纹理参数
参数3： 设定特定的纹理参数的值



3.1 设置过滤方式（也叫取样）*
邻近过滤 GL_NEAREST
邻近过滤是最简单最快速的过滤方法，它总是把最邻近的纹理单元取到纹理坐标中。最显著的特征就是当纹理被拉伸到特别大的时候会出现大片斑驳状像素。

![在这里插入图片描述](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706279058691-11.png)

线性过滤GL_LINEAR
线性过滤会把这个纹理坐标周围的纹理单元加权平均值应用到这个纹理坐标中。周围坐标的纹理单元距离越近，则权值越大，同时更接近真实

![在这里插入图片描述](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706279070295-14.png)



样例

```
//纹理放大时，使用临近过滤
glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
//纹理缩小时，使用临近过滤（推荐）
glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
//纹理放大时，使用线性过滤 （推荐）
glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
//纹理缩小时，使用线性过滤
glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
当然放大缩小时也可以随意搭配过滤方式
```

![在这里插入图片描述](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706279092681-17.png)



### 3.2 设置环绕方式

```
//纹理坐标里的 s t r 分别代表 x y z轴
//指定横轴的环绕方式为GL_CLAMP_TO_EDGE
glTextParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAR_S,GL_CLAMP_TO_EDGE);
//指定纵轴的环绕方式为GL_CLAMP_TO_EDGE
glTextParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAR_T,GL_CLAMP_TO_EDGE);
```

```
参数1:  GL_TEXTURE_1D、GL_TEXTURE_2D、GL_TEXTURE_3D
参数2:  GL_TEXTURE_WRAP_S、GL_TEXTURE_T、GL_TEXTURE_R,针对s,t,r坐标S 、T、 R 坐标系对应着世界坐标系的X， Y， Z
参数3:  GL_REPEAT、GL_CLAMP、GL_CLAMP_TO_EDGE、GL_CLAMP_TO_BORDER
```

GL_REPEAT:OpenGL在纹理坐标超过1.0的⽅向上对纹理进⾏重复;
GL_CLAMP:所需的纹理单元取⾃纹理边界或TEXTURE_BORDER_COLOR.
GL_CLAMP_TO_EDGE:环绕模式强制对范围之外的纹理坐标沿着合法的纹理单元的最后一行或者最后⼀列来进行采样。
GL_CLAMP_TO_BORDER:在纹理坐标在0.0到1.0范围之外的只使⽤边界纹理单元。边界纹理单元是作为围绕基本图像的额外的⾏和列列，并与基本纹理图像⼀起加载的

![img](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706279126627-20.png)



### 4 Mip贴图

Mip贴图是一种功能强大的纹理技巧。它可以提高渲染性能同时可以改善场景的显示质量。

想象一下，假设我们有一个包含着上千物体的大房间，每个物体上都有纹理。有些物体会很远，但其纹理会拥有与近处物体同样高的分辨率。由于远处的物体可能只产生很少的片段，OpenGL从高分辨率纹理中为这些片段获取正确的颜色值就很困难，因为它需要对一个跨过纹理很大部分的片段只拾取一个纹理颜色。在 小物体上这会产生不真实的感觉，更不用说对它们使用高分辨率纹理浪费内存的 问题了。

OpenGL使用一种叫做多级渐远纹理(Mipmap)的概念来解决这个问题，它简单来说就是一系列的纹理图像，后一个纹理图像是前一个的二分之一。多级渐远纹理 背后的理念很简单：距观察者的距离超过一定的阈值，OpenGL会使用不同的多级渐远纹理，即最适合物体的距离的那个。由于距离远，解析度不高也不会被用户注意到。同时，多级渐远纹理另一加分之处是它的性能非常好。让我们看一下 多级渐远纹理是什么样子的：



![在这里插入图片描述](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706279148237-23.png)





手工为每个纹理图像创建一系列多级渐远纹理很麻烦，幸好OpenGL有一
个glGenerateMipmaps函数，在创建完一个纹理后调用它就会承担接下来的所有 工作了

#### 常见问题：



1.闪烁问题，当屏幕上被渲染物体的表面与它所应用的纹理图像相比显得非常小时，就会出现闪烁效果。类似闪光，当纹理图像采样区域的移动幅度与它在屏幕大小相比显得不成比例时，也会发生这种现象。处于运动状态时，会比较容易看到闪烁的负面效果。



2.性能问题，加载大的纹理到内存并对它们进行过滤处理，但屏幕上实际只是显示的只是很少一部分的片段。纹理越大，这个问题所造成的性能影响也很明显。这2种问题，我当然可以用很简单的方法解决，就是使用更小的纹理图像。但是这种解决方法又将产生一个新的问题，就是当一个物体更靠近观察者时，它必须渲染的比原来更大一些，这样，纹理就不得不拉伸。拉伸的结果，形成视觉效果很差的模糊或者斑驳状的纹理化效果。



从根本上解决方案，就是使用Mip贴图。不是单纯的把单个图像加载到纹理状态中，而是把一系列从最大到最小的图像加载到单个"Mip贴图"纹理状态。然 后QpenGL使用一组的新的过滤模式，为一个特定的几何图形选择具有最佳过滤 效果的纹理。



Mip纹理由一系列的纹理图像组成，每个图像大小在每个轴的方向上都缩小一 半。或者是原来图像像素的总是的四分之一。Mip贴图每个图像大小都依次减半，直到最后一个图像大小是1*1的纹理单元为止。



上面提到的glTexParameteri()函数，谈到了 level参数。level参数在Mib贴图发挥作用。因为它指定图像数据用于那个mip层。第一层是0，后面依次类推。如果Mip贴图未使用，那么只有第0层才会被加载。在默认情况下， 为了能使用mip贴图，所以的mip层都要加载。我们可以通过设置GL_TEXTURE_BASE_LEVE L和GL_TEXTURE_MAX_LEVEL纹理参数特别设置需要使用的基层和最大层。

```
//设置mip贴图基层
glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_BASE_LEVEL,0);
//设置mip贴图最大层
glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAX_LEVEL,0);
```

我们虽然可以通过设置GL_TEXTURE_BASE_LEVEL和GL_TEXTURE_MAX_LEVEL纹理参 数控制那些mip层被加载。但是我们仍然可以使用GL_TEXTURE_MIN_LOD和GL_TEXTURE_MAX_LOD参数限制已加载的Mip层的使用范围。



### 4.1 什么时候生成Mip贴图？

只有minFilter等于以下四种模式，才可以生成Mip贴图
GL_NEAREST_MIPMAP_NEAREST：具有非常好的性能，并且闪烁现象非常弱
GL_LINEAR_MIPMAP_NEAREST：常常用于对游戏进行加速，它使用了高质量的线 性过滤器
GL_LINEAR_MIPMAP_LINEAR和GL_NEAREST_MIPMAP_LINEAR：过滤器在 Mip 层之间执行了一些额外的插值，以消除他们之间的过滤痕迹。
GL_LINEAR_MIPMAP_LINEAR：三线性Mip贴图。纹理过滤的黄金准则，具有最高的精度。

```
	if(minFilter == GL_LINEAR_MIPMAP_LINEAR || 
		minFilter ==GL_LINEAR_MIPMAP_NEAREST || 
		minFilter == GL_NEAREST_MIPMAP_LINEAR || 
		minFilter == GL_NEAREST_MIPMAP_NEAREST)
		//4.纹理生成所有的Mip层
		//参数:GL_TEXTURE_1D、GL_TEXTURE_2D、GL_TEXTURE_3D 
		glGenerateMipmap(GL_TEXTURE_2D);
```

glGenerateMipmap 函数解析

目的：为纹理对象生成一组完整的mipmap

```
void glGenerateMipmap (GLenum target);
参数：指定将生成mipmap的纹理对象绑定到的活动纹理单元的纹理目标.GL_TEXTURE _1D、 GL_TEXTURE_2D、 GL_TEXTURE_3D
```

描述：glGenerateMipmap计算从零级数组派生的一组完整的mipmap数组。无论先前的内容如何，最多包括1x1维度纹理图像的数组级别都将替换为派生数组。零级纹理图像保持不变(原图)。
派生的mipmap数组的内部格式都与零级纹理图像的内部格式相匹配。通过将零级纹理图像的宽度和高度减半来计算派生数组的尺寸，然后将每个阵列级别的尺寸减半， 直到达到1x1尺寸纹理图像。



Mip贴图过滤

![在这里插入图片描述](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706279237264-26.png)


一个常见的错误是，将放大过滤的选项设置为多级渐远纹理过滤选项之一。这样没有任何效果，因为多级渐远纹理主要是使用在纹理被缩小的情况下的：纹理放大不会使用多级渐远纹理，为放大过滤设置多级渐远纹理的选项会产生一 个 GL_INVALID_ENUM 错误代码。

```
//GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER（缩小过滤器）,GL_NEAREST （最邻近过滤）
glTexParameteri(GL_TEXTURE_2D_, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
```

```
//GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER（缩小过滤器）,GL_LINEAR （线性过滤）
glTexParameteri(GL_TEXTURE_2D_, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
```

```
//GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER（缩小过滤器）,GL_NEAREST_MIPMAP_ NEAREST （选择最邻近的Mip层,并执行最邻近过滤）
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPM AP_NEAREST );
```

```
//GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER（缩小过滤器）,GL_NEAREST_MIPMAP_ LINEAR （在Mip层之间执行线性插补,并执行最邻近过滤）
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPM AP_LINEAR);
```

```
//GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER（缩小过滤器）,GL_NEAREST_MIPMAP_L INEAR （选择最邻近Mip层,并执行线性过滤）
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMA P_NEAREST );
```

```
//GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER（缩小过滤器）,GL_LINEAR_MIPMAP_LI NEAR （在Mip层之间执行线性插示,异执行线性过滤,又称为三硬性过滤） glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMA P_LINEAR);
```



### 各向异性过滤

各向异性纹理过滤(Anisotropic texture filteri ng)并不是penGL核心规范中的一部分。但它是一种得到广泛使用的扩展。可以极大提高纹理过滤操作的质量。

前面说到2种基本过滤，最邻近过滤(GL_NEAREST )和线性过滤(GL_LINEAR)。当一个纹理贴图被过滤时，OpenGL使用纹理坐标来判断一个特定的几何片段将落在纹理什么地方。然后，紧邻这个位置的纹理单使用GL_NEAREST和GL_LINEAR过滤操作进行采样。

当几何图形进行纹理贴图时，如果它的观察方向和观察点恰好垂直。那么这个过程是相当完美的。如下图，

![在这里插入图片描述](D:\my-note\opengl\assets\watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3NodWFuOTk5OQ==,size_16,color_FFFFFF,t_70-1706279292924-29.png)

当我们从一个角度倾斜地观察这个几何图形时，对周围纹理单元进行常规采样， 会导致一些纹理信息丢失(看上去显得模糊)。

为了更加逼真和准确的采样应该沿着包含纹理的平面方向进行延伸。如果我们进行处理纹理过滤时，考虑了观察角度，那么这个过滤方法就叫“各向异性过滤”。

在Mip贴图纹理过滤模型中，或者其他所有的基本纹理过滤我们都可以应用各向异性过滤。

应用各向异性过滤，需要2个步骤。

1、查询得到支持的各向异性过滤的最大数量，可以使用glGetFloatv函 数，并以 GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT 参数。

```
GLfloat flargest;
glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, &fLargest)
```

2、我们可以使用glTexParameter函数以及GL_TEXTURE_MAX_ANISOTROPY_EXT，设置各向异性过滤数据。

```
//设置纹理参数(各向异性采样)
glTexParameterf (GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, fLargest)
//设置各向同性过滤,数量为1.0表示(各向同性采样)
glTexParameterf (GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY_EXT, 1.0f)；
```

注意：

各向异性过滤所应用的数量越大，沿着最大变化方向（沿最强的观察点）所采样 的纹理单元就越多。值1.0表示常规的纹理过滤（各向同性过滤）。
各向异性过滤，是会增加额外的工作，包括其他纹理单元。很可能对性能造成影 响。但是，在现代硬件上，应用这个特性对速度造成影响不大。最重要的是，目前它已经成为流行游戏、动画和模拟程序的一个标准特性。

