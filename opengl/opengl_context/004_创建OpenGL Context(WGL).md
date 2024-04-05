# 创建OpenGL Context(WGL)



# 关于platform的注意事项

创建OpenGL context之后才会存在OpenGL。这个创建过程不归OpenGL Specification管，而是归各个platform的API管。本文讨论基于Windows的初始化过程。许多Windows上的初始化函数是以”wgl”开头的。

本文假设读者知道Win32 API的基础知识。读者应知道window handle(HWND)和device context(DC)是什么，以及如何创建他们。本文不是讲解如何创建窗口的教程。

## 创建一个简单的Context

这一节是创建Context的基础知识。

### **窗口**

创建HWND时，要确保它有CS_OWNDC设置。

### **像素格式**

MS Windows里，每个窗口都有一个Device Context(DC)与之关联。DC里存储有像素格式PixelFormat。你创建的OpenGL Context里有个默认的framebuffer，PixelFormat是描述此framebuffer的属性的数据结构。

设置PixelFormat的方式并不直观。首先你创建一个你想要的*pixelFormat*，然后交给ChoosePixelFormat函数，此函数会查找能够支持的PixelFormat列表，返回最接近*pixelFormat*的编号。然后你就可以用此编号指定DC的PixelFormat。

上面描述的数据结构就是PIXELFORMATDESCRIPTOR。

```
 1 PIXELFORMATDESCRIPTOR pfd =
 2 {
 3     sizeof(PIXELFORMATDESCRIPTOR),
 4     1,
 5     PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER,    //Flags
 6     PFD_TYPE_RGBA,            //The kind of framebuffer. RGBA or palette.
 7     32,                        //Colordepth of the framebuffer.
 8     0, 0, 0, 0, 0, 0,
 9     0,
10     0,
11     0,
12     0, 0, 0, 0,
13     24,                        //Number of bits for the depthbuffer
14     8,                        //Number of bits for the stencilbuffer
15     0,                        //Number of Aux buffers in the framebuffer.
16     PFD_MAIN_PLANE,
17     0,
18     0, 0, 0
19 };
```



你看，很多field都是0。就这样，没问题。我们需要关心的field，你可能需要用的field，都用注释标记了。关于PixelFormat的更多flags，请查询Windows SDK文档。

上文说到ChoosePixelFormat函数，它接收一个DC和一个PFD，返回一个编号。如果返回的是0，那就意味着找不到匹配的PixelFormat，或者PDF内容错误。

有了PixelFormat编号，就可以用SetPixelFormat指定给DC。这个函数接收DC、编号和PFD的指针。别激动，这个函数没有读取PFD里的任何重要信息。

### **创建****Context**

接下来创建context就简单了。调用wglCreateContext。这个函数接收DC，返回OpenGL Context的句柄。

在使用OpenGL前，要用wglMakeCurrent 把context设置为current。如果已经有current context，这个函数会把旧context替换掉。后续的OpenGL函数调用会影响新context中的状态。如果你传入NULL，那么旧context会被移除，后续OpenGL函数调用会失败（崩溃）。

current context是线程专用的。每个线程可以将一个不同的context设置为current。将同一个context设置为多个线程的current是危险的。

### **删除****Context**

严格来说这不是创建Context的内容，但是你应当指定如何删除Context。

首先要确定你想删除的Context不是current。给wglMakeCurrent 传入NULL参数。

现在可以调用wglDeleteContext 来删除它了。

## 创建合适的Context

除非你只想做一个很简单的程序，否则你不应当使用上述的简单步骤创建的context。有一些功能强大的WGL扩展函数助你创建高级context，但是创建context 过程会复杂些。

### **创建一个傻帽****Context**

关键问题是这样的：你用来获取WGL扩展的函数，其本身就是一个OpenGL扩展。因此，首先要有一个OpenGL Context，然后才能使用WGL扩展。所以，为了能够使用那些“创建context的函数”，我们首先要“创建一个context”。幸运的是，这个context用不着是我们最后的context。我们只需创建一个傻帽context来获取函数指针，然后直接使用这些函数即可。

警告：不幸的是，Windows不允许用户改变一个窗口的PixelFormat。你只能设置一次。因此，如果你想通过傻帽Context使用一个不同的PixelFormat，你必须在用完傻帽Context后彻底销毁这个窗口并重建之。

对于傻帽Context，一个好的PixelFormat选择是32位RGBA颜色缓存+24位深度缓存+8位模版缓存。我们上面就是这样设置的PFD。这通常都能得到一个硬件加速的PixelFormat。

所以，这一步就是重复上文的代码，创建一个傻帽Context，设置为current。

### **获取****WGL****扩展**

*Main article: [Load OpenGL Functions#Windows 2](https://www.khronos.org/opengl/wiki/Load_OpenGL_Functions#Windows_2)*

如果你使用了[加载扩展的库](https://www.khronos.org/opengl/wiki/Extension_Loading_Library)，现在就可以调用任何你需要的函数。如果没有，你就得自己[手动加载](https://www.khronos.org/opengl/wiki/Load_OpenGL_Functions#Windows_2)。

有不少扩展可以实施高端的context创建工作。其中大多数是围绕PixelFormat的创建和一个Exception。

### **Pixel Format****扩展**

PFD是帮助创建Context的很好的方式，但是有个缺点：不可扩展。因此，产生了WGL_ARB_pixel_format扩展。这个扩展定义了一种新的获取PixelFormat编号的机制，此机制的核心是由一个’属性\值’的数组。

只有在定义了此扩展的机器上才能使用它。这个扩展已经存在很长时间了，即使很老的显卡也支持它。所以你可以打赌认为你的机器环境是实现了WGL_ARB_pixel_format的。

此扩展提供了几个新的函数，我们感兴趣的是下面这个：

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
1 BOOL wglChoosePixelFormatARB(   HDC hdc,
2                                 const int *piAttribIList,
3                                 const FLOAT *pfAttribFList,
4                                 UINT nMaxFormats,
5                                 int *piFormats,
6                                 UINT *nNumFormats);
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

wglChoosePixelFormatARB 类似ChoosePixelFormat。他接收的不是固定的PFD结构体，而是一个’属性\值’的数组。很多属性都直接对应PFD里的字段，但有些属性是新的。而且，此函数能够返回多个符合要求的PixelFormat，并按照从最符合到最不符合的顺序排序。“最符合”是由具体OpenGL实现来决定的。

总之，使用方法很简单。piAttribIList 是整数属性列表。每2个元素构成一个’属性\值’对。属性’0’表示列表结束，并且其后不需要值。你可以传入NULL，此函数会当作你传入一个空列表。

类似的，pfAttribFList 是浮点属性列表。每2个元素构成一个’属性\值’对。如何将整型的属性放到float类型里？非常小心地放。你需要用static-cast（C++里）或者用其它技巧让bit-pattern保持相同。

nMaxFormats 是将要保存到piFormats里的数量的最大值。因此piFormats 应当至少有那么多个元素。nNumFormats 是返回值，告诉你piFormats真正存储了多少个元素。

如果函数返回FALSE，就意味着没有找到合适的PixelFormat。此时piFormats 就是未定义的状态（OpenGL实现可以随意修改其内容）如果函数返回不是FALSE，那么就成功了，你得到了PixelFormat编号。

下面的示例代码演示了如何使用此函数产生和上文近似的PixelFormat：

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

```
 1 const int attribList[] =
 2 {
 3     WGL_DRAW_TO_WINDOW_ARB, GL_TRUE,
 4     WGL_SUPPORT_OPENGL_ARB, GL_TRUE,
 5     WGL_DOUBLE_BUFFER_ARB, GL_TRUE,
 6     WGL_PIXEL_TYPE_ARB, WGL_TYPE_RGBA_ARB,
 7     WGL_COLOR_BITS_ARB, 32,
 8     WGL_DEPTH_BITS_ARB, 24,
 9     WGL_STENCIL_BITS_ARB, 8,
10     0,        //End
11 };
12 
13 int pixelFormat;
14 UINT numFormats;
15 
16 wglChoosePixelFormatARB(hdc, attribList, NULL, 1, &pixelFormat, &numFormats);
```

[![复制代码](./assets/copycode.gif)](javascript:void(0);)

有一些扩展，给这个函数增加了新的属性。你可能想要用到的有：

- [WGL_ARB_pixel_format_float](http://www.opengl.org/registry/specs/ARB/color_buffer_float.txt):支持浮点framebuffer。
- [WGL_ARB_framebuffer_sRGB](http://www.opengl.org/registry/specs/ARB/framebuffer_sRGB.txt): 支持sRGB格式的colorbuffer。
- [WGL_ARB_multisample](http://www.opengl.org/registry/specs/ARB/multisample.txt): 支持多重采样的framebuffer。

得到了PixelFormat编号，你就可以用SetPixelFormat指定给DC。

### **用****Attributes****创建****Context**

为了移除旧功能，OpenGL3.0及其以上版本创造了一个“不推荐\可移除”的模型。但是这带来一点问题。在之前的OpenGL版本，新版OpenGL是旧版的超集(superset)。因此，如果你想要的是1.5版context，结果得到的是2.0版，那没问题。你只是得到了额外的你用不到的功能。一旦出现了移除旧功能的可能性，这种超集关系就没有了。

因此出现了WGL_ARB_create_context扩展。它提供了代替wglCreateContext的函数。类似wglChoosePixelFormatARB，它提供了一种扩展机制，使你能够增加新的创建context所用的选项。

如果傻帽context没有提供这个扩展，那么你就不能用它。你就只能用wglCreateContext 了。

如果它提供了这个扩展，那么会有一些平常得不到的选项供我们选用：

- 保证获取OpenGL3.0或者更高版本的Context。
- 创建OpenGL3.2或者更高班的core context，且没有兼容旧特性。
- 不用窗口，创建context，用于离屏渲染。这可能会做不成。

遗留问题：（这里有一堆没什么用的话，略过不译）如果定义了WGL_ARB_create_context_profile，那就用上述方法。如果没有，那就只能用wglCreateContext 直接创建GL3.0或更高版本context。

wglCreateContextAttribsARB 签名如下：

```
1 HGLRC wglCreateContextAttribsARB(HDC hDC, HGLRC hshareContext, const int *attribList);
```

这里的attribList 与wglChoosePixelFormatARB里的类似。它是一系列’属性\值’对，以单独的0作为最后一个元素结尾。

你可以用WGL_CONTEXT_MAJOR_VERSION_ARB 和WGL_CONTEXT_MINOR_VERSION_ARB这2个属性指定你想要哪个版本。

你请求一个版本，然后你得到哪个版本？这个规则比较复杂，简单来说有两条：

1. 它总会返回一个等于或高于你要求的版本的OpenGL Context。
2. 它永远不会返回一个没有实现你要求的版本里的core feature的OpenGL版本。

如果定义WGL_ARB_create_context_profile了，那么你可以用WGL_CONTEXT_PROFILE_MASK_ARB 属性来选择一个core配置(WGL_CONTEXT_CORE_PROFILE_BIT_ARB)或者一个兼容配置(WGL_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB)。注意，这些都是bit组合，所以你可以同时要求他们俩（不过你只会得到兼容配置）。这里面的细节就值得[深入讨论](https://www.khronos.org/opengl/wiki/Core_And_Compatibility_in_Contexts)了。

你也可以用WGL_CONTEXT_FLAGS_ARB属性指定若干flag。你可以用它请求一个向前兼容的context(WGL_CONTEXT_FORWARD_COMPATIBLE_BIT_ARB)且（或）一个debug context(WGL_CONTEXT_DEBUG_BIT_ARB)。Debug context常常实现了ARB_debug_output，能够提供加强了的error输出。向前兼容的context必须彻底移除deprecated特性，**实际上你永远都不应该用这个选项**。

hshareContext 是个特殊的参数。如果你有2个GL Context，且你想让他们共享[对象](https://www.khronos.org/opengl/wiki/OpenGL_Objects)，那你可以用wglShareLists函数。但是你必须在创建对象（在任意两个context里）之前使用。wglCreateContextAttribsARB 直接配合这个功能。

 

See Also

- [Core And Compatibility in Contexts](https://www.khronos.org/opengl/wiki/Core_And_Compatibility_in_Contexts)
- [Tutorial: OpenGL 3.0 Context Creation (GLX)](https://www.khronos.org/opengl/wiki/Tutorial:_OpenGL_3.0_Context_Creation_(GLX))
- [Tutorial: OpenGL 3.1 The First Triangle (C++/Win)](https://www.khronos.org/opengl/wiki/Tutorial:_OpenGL_3.1_The_First_Triangle_(C%2B%2B/Win))

References

- [WGL_ARB_pixel_format Specification](http://www.opengl.org/registry/specs/ARB/wgl_pixel_format.txt)

- [WGL_ARB_pixel_format_float Specification](http://www.opengl.org/registry/specs/ARB/color_buffer_float.txt)
- [WGL_ARB_framebuffer_sRGB Specification](http://www.opengl.org/registry/specs/ARB/framebuffer_sRGB.txt)

- [WGL_ARB_create_context Specification](http://www.opengl.org/registry/specs/ARB/wgl_create_context.txt)