# MESA : GL Dispatch

OpenGL函数的分发机制相当复杂，这个文章试图解释其中的一些议题，并且给读者介绍Mesa的分发层实现。

## 1 GL Dispatch的复杂之处

每一个GL应用至少拥有一个名叫GL context的对象，这个对象是所有GL function隐含的内置参数，储存了所有应用当前状态下的所有GL状态。例如纹理、VBO之类的上下文信息都存放在[context对象](https://zhida.zhihu.com/search?content_id=217439824&content_type=Article&match_order=1&q=context对象&zhida_source=entity)中。一个应用可以包含多个context，当前时刻哪个context被使用是由一个和窗口相关的函数去指定的（glXMakeContextCurrent）。

例如在使用GLX实现OpenGL With X-Windows的时候，每一个GL的[函数指针](https://zhida.zhihu.com/search?content_id=217439824&content_type=Article&match_order=1&q=函数指针&zhida_source=entity)都是通过glXGetProcAddress函数获得的，获得的函数指针都是contex independent 的，无论是哪个上下文对象被激活，都是相同的函数被调用。

这是GL Dispatch复杂的原因之一！

一个应用包含两个GL contexts。一个context是直接渲染上下文，其中函数调用直接路由到应用程序[地址空间](https://zhida.zhihu.com/search?content_id=217439824&content_type=Article&match_order=1&q=地址空间&zhida_source=entity)中加载的驱动程序（One context is a direct rendering context where function calls are routed directly to a driver loaded within the application’s address space），另外一个context间接渲染上下文，其中函数调用将会被转化为GLX协议并且发送给服务端（The other context is an indirect rendering context where function calls are converted to GLX protocol and sent to a server）。在这两种情况下，类似glVertex3fv这样的gl函数都需要做出相同的正确的事情。

高度优化的驱动或者GLX协议实现会希望更具当前的状态更改GL函数的行为，例如，glFogCoordf函数会根据fog是否enable而产生不同的影响。

在[多线程](https://zhida.zhihu.com/search?content_id=217439824&content_type=Article&match_order=1&q=多线程&zhida_source=entity)的环境中，每一个线程包含不同的GL context是完全可能的。这个意味着可怜的glVertex3fv函数必须要去知道哪个contex是当前线程的contex，函数在什么时候被调用。

## 2 Mesa实现的概览

Mesa 每个线程使用两个指针，第一个指针记录了当前线程使用的context地址，第二个指针记录了一个与[contex](https://zhida.zhihu.com/search?content_id=217439824&content_type=Article&match_order=17&q=contex&zhida_source=entity)对应的dispatch table地址。dispatch table储存了一个真实实现此功能的函数指针。每个时刻，一个新的context被创建在一个线程中，这两个指针都会被更新。

其实 glVertex3fv这样的函数实现在概念上很简单：

1. 获取当前的dispatch table指针
2. 从dispatch table指针中获取确实实现glVertex3fv的函数指针
3. 调用真实的函数指针

这个函数可以被声明的非常简单，甚至就几行C代码。见src/mesa/glapi/glapitemp.h可以看到类似的代码。

```text
void glVertex3f(GLfloat x, GLfloat y, GLfloat z)
{
    const struct _glapi_table * const dispatch = GET_DISPATCH();

    (*dispatch->Vertex3f)(x, y, z);
}
```

但是上述这个简单的实现存在问题，他给每次调用此GL函数带来很大的开销。

在多线程的环境里，一个原始的GET_DISPATCH()函数会包含一个调用`_glapi_get_dispatch()`or`_glapi_tls_Dispatch` 来优化开销。

## 3 优化

近一些年产生了很多优化的方法来减少因为GL dispatch带来的性能损失，这个章节将会描述这些优化方法，列出了每种优化的好处，以及在什么时候可以用此优化在什么时候不能。

### 3.1 ELF TLS

从2.4.20 Linux Kernel以来，每个线程都会被分配一个全局的储存区域，可以使用GCC的一些扩展(ELF TLS)指令使得某些变量存放在此区域。如果可以把dispatch table指针存放在这个区域，那么调用pthread_getspecific 函数和_glapi_Dispatch函数就可以避免了，这些函数会产生大量的软件开销。

mesa不支持2.4.20以前的linux内核，所以理论上完全支持ELF TLS。

dispatch table 指针被存放在一个新的变量中 名为 _glapi_tls_Dispatch。使用一个新的变量名意味着libGL可以实现两种不同的接口，这使得libGL可以使用任意一个接口的直接渲染[驱动程序](https://zhida.zhihu.com/search?content_id=217439824&content_type=Article&match_order=2&q=驱动程序&zhida_source=entity)。一旦正确声明了指针，GET_DISPACH 就变成了一个简单的变量引用。

```text
extern __THREAD_INITIAL_EXEC struct _glapi_table *_glapi_tls_Dispatch;

#define GET_DISPATCH() _glapi_tls_Dispatch
```

## 3.2 Assembly Language Dispatch Stubs

许多平台在合理优化dispatch stubs的tail-call的时候存在困难，特别是X86这种依靠栈来传递参数的。其实所有的分发规则非常简单，给这个分发的函数直接创建[汇编语言](https://zhida.zhihu.com/search?content_id=217439824&content_type=Article&match_order=1&q=汇编语言&zhida_source=entity)版本也非常简单，使用汇编语言优化displatch stubs不一定可以产生非常显著的性能优化，这由平台和应用程序来决定的。但是毫无疑问的是，如果使用汇编来优化，至少可以优化整体的代码尺寸。

创建汇编stubs最大的障碍是处理不同的dispatch table访问方式，主要由下述四种方式

1. 直接使用 `_glapi_Dispatch` 在非多线程的环境下
2. 使用`_glapi_Dispatch`and`_glapi_get_dispatch` 在多线程的环境下
3. 使用_glapi_tls_Dispatch，在支持TLS的多线程环境下

如果想为新的设备实现汇编stubs需要着重关心第3点，看下最新的平台是否支持TLS。否则实现第2中方式。第一种方式几乎不可能，因为只支持单线程的平台非常的少。

选择dispatch的方式是由非常少的宏来决定的。

> 如果HAVE_PTHREAD被定义，则方式2打开
> 如果HAVE_PTHREAD未定义，则方式1打开



参考资料

[GL Dispatch — The Mesa 3D Graphics Library latest documentation](https://docs.mesa3d.org/dispatch.html)

