# OpenGL ES EGL & TLS（线程局部存储） & G3D

http://java-admin.iteye.com/blog/734817



1. 什么是EGL

EGL是用来管理绘图表面的（Drawing surfaces），并且提供了如下的机制
（1） 与本地窗口系统进行通信
（2） 查找绘图表面可用的类型和配置信息
（3） 创建绘图表面
（4） 同步OpenGL ES 2.0和其他的渲染API（Open VG、本地窗口系统的绘图命令等）
（5） 管理渲染资源，比如材质

2. EGL 和 OpenGL ES API的联系

（1） 通过解析OpenGL ES API函数库 libGLES_android.so来获取函数指针，进行调用。
（2） 通过线程局部存储机制进行联系

关于通过函数指针进行联系在前面已经分析过了。下面着重分析通过线程局部存储机制进行联系分析一下。

2.1什么是线程局部存储（TLS）
TLS 让多线程程序设计更容易一些。TLS 是一个机制，经由它，程序可以拥有全域变量，但处于「每一线程各不相同」的状态。也就是说，进程中的所有线程都可以拥有全域变量，但这些变量只对特定对某个线程才有意义。http://xianjunzhang.blog.sohu.com/21537031.html
2.2 TLS的好处
你可能有一个多线程程序，每一个线程都对不同的文件写文件（也因此它们使用不同的文件handle）。这种情况下，把每一个线程所使用的文件handle 储存在TLS 中，将会十分方便。当线程需要知道所使用的handle，它可以从TLS 获得。重点在于：线程用来取得文件handle 的那一段代码在任何情况下都是相同的，而从TLS中取出的文件handle 却各不相同。非常灵巧，不是吗？有全域变数的便利，却又分属各线程。http://xianjunzhang.blog.sohu.com/21537031.html 
2.3 OpenGL ES中的TLS
2.3.1 TLS的初始化
应用程序通过EGL调用eglGetDisplay（）函数时，会调用到libEGL.so中，通过看其源码egl.so可以发现，其中有条语句


static pthread_once_t once_control = PTHREAD_ONCE_INIT;
static int sEarlyInitState = pthread_once(&once_control, &early_egl_init);

这两条语句会先于eglGetDisplay函数执行。第二条语句中将函数指针early_egl_init作为参数传入，会执行回调，并且保证单个线程只会执行一次。在early_egl_init（）中，对TLS机制进行初始化。将TLS里放入一个结构体指针，这个指针指向gHooksNoContext（gl_hooks_t类型），这个结构体里的每个函数指针被初始化为了gl_no_context。也就是现在如果通过TLS调用的OpenGL ES API都会调到gl_no_context这个函数中。
综上，这两条语句完成了TLS的初始化。
另一处初始化时在eglInitialize函数中，同样设置成了gHooksNoContext。
2.3.2 TLS的赋值
在eglMakeCurrent中，会将渲染上下文绑定到渲染面。在EGL中首先会处理完一系列和本地窗口系统的变量后，调用libGLES_android.so中的eglMakeCurrent，调用成功的话会设置TLS。将TLS指针指向前面已经初始化化好的gl_hooks_t结构体指针，这个结构体里的成员都已经指向了libGLES_android.so中的OpenGL API函数，至此EGL的大部分初始化工作就已经完成了。基本就可以使用OpenGL ES API进行绘图了。

static inline void setGlThreadSpecific(gl_hooks_t const *value) {
  gl_hooks_t const * volatile * tls_hooks = get_tls_hooks();
  tls_hooks[TLS_SLOT_OPENGL_API] = value;
}

 

3. 调用OpenGL ES API函数

在通过EGL对本地窗口系统做一系列初始化之后，就需要调用真正的OpenGL ES API进行3D绘图了，对于很多没有接触过OpenGL和计算机图形学的人来说，这部分可能是比较困难的（很多算法我都不懂。。。）。下面脱离具体的算法实现，看看要调用OpenGL ES API，Android 的3D系统做了什么。
具体分析可以参考Android源码中OpenGL ES的测试代码。
在应用程序中要调用一个OpenGL ES API，需要包含对应的头文件。比如OpenGL ES 1.1对应的头文件是<GLES/gl.h>。但是在具体的执行中，调用到了那个库中呢？
分析tests中的tritex测试案例。它的Andoird.mk是这样的。

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
tritex.cpp

LOCAL_SHARED_LIBRARIES := \
libcutils \
  libEGL \
  libGLESv1_CM \
  libui

LOCAL_MODULE:= test-opengl-tritex

PRODUCT_COPY_FILES := \
  out/target/product/generic/system/bin/test-opengl-tritex:/nfs/gltest/

LOCAL_MODULE_TAGS := optional

include $(BUILD_EXECUTABLE)  

从中可以看出，它链接的时候使用的libGLESv1_CM.so
而生成libGLESV1_CM.so的Android.mk是这样写的。

include $(CLEAR_VARS)

LOCAL_SRC_FILES:=   \
GLES_CM/gl.cpp.arm \
\#

LOCAL_SHARED_LIBRARIES += libcutils libEGL
LOCAL_LDLIBS := -lpthread -ldl
LOCAL_MODULE:= libGLESv1_CM

\# needed on sim build because of weird logging issues
ifeq ($(TARGET_SIMULATOR),true)
else
  LOCAL_SHARED_LIBRARIES += libdl
  \# we need to access the private Bionic header <bionic_tls.h>
  LOCAL_C_INCLUDES += bionic/libc/private
endif

LOCAL_CFLAGS += -DLOG_TAG=\"libGLESv1\"
LOCAL_CFLAGS += -DGL_GLEXT_PROTOTYPES -DEGL_EGLEXT_PROTOTYPES
LOCAL_CFLAGS += -fvisibility=hidden

ifeq ($(ARCH_ARM_HAVE_TLS_REGISTER),true)
LOCAL_CFLAGS += -DHAVE_ARM_TLS_REGISTER
endif

include $(BUILD_SHARED_LIBRARY) 
源文件只有一个gl.cpp.
这个文件里的函数看起来只有两个函数（怎么可能！！！），其实不然。
其中一句

extern "C" {
\#include "gl_api.in"
\#include "glext_api.in"
} 
C语言中对#include的处理类似于宏的展开，直接把文件包含进来进行编译的。
gl_api.in中，实际上这些函数的定义。

……
void API_ENTRY(glClearColor)(GLclampf red, GLclampf green, GLclampf blue, GLclampf alpha) {
  CALL_GL_API(glClearColor, red, green, blue, alpha);
}
void API_ENTRY(glClearDepthf)(GLclampf depth) {
  CALL_GL_API(glClearDepthf, depth);
}
…… 
gl.cpp中去掉条件编译


\#define GET_TLS(reg) \
      "mov  " #reg ", #0xFFFF0FFF   \n" \
      "ldr  " #reg ", [" #reg ", #-15] \n"

\#define API_ENTRY(_api) __attribute__((naked)) _api

\#define CALL_GL_API(_api, ...)               \
     asm volatile(                     \
      GET_TLS(r12)                    \
      "ldr  r12, [r12, %[tls]] \n"            \
      "cmp  r12, #0      \n"            \
      "ldrne pc, [r12, %[api]] \n"            \
      "bx  lr         \n"            \
      :                          \
      : [tls] "J"(TLS_SLOT_OPENGL_API*4),         \
       [api] "J"(__builtin_offsetof(gl_hooks_t, gl._api))  \
      :                          \
      );

  \#define CALL_GL_API_RETURN(_api, ...) \
    CALL_GL_API(_api, __VA_ARGS__) \
    return 0; // placate gcc's warnings. never reached. 
CALL_GL_API这个带参数的宏。它的意思是获取TLS_SLOT_OPENGL_API的TLS，如果它的值不是NULL，就跳到相应的OpenGL ES API的地址去执行。这个地方为什么会跳过去呢？？
因为从线程局部存储保存的线程指针，指向了一个gl_hooks_t指针，而这个指针指向的结构体里的成员已经在EGL中被初始化为了libGLES_android.so里的函数地址了。所以就可以跳过去了。

从以上分析可以看出libGLESv1_CM.so只是一个wrapper，对OpenGL ES API进行了一个简单的包裹，真正的实现还是在libGLES_andoird.so中的。
对于libGLESV2.so是同样的道理。

这样做的好处：

1. 两套OpenGL ES API对应一个实现库，便于维护
2. 可以对OpenGL ES API进行更改，而不需要改变对外的接口，易于程序移植和兼容。
3. 需要注意的问题

OpenGL ES API很多函数并没有实现。软件加速库不支持OpenGL ES2.0

Loader.cpp中的init_api函数通过dlsym函数，对so文件进行解析，返回了函数的指针，在对每个函数进行跟踪的过程中发现，原来glShaderSource并没有在openGL ES的源码中实现，而且发现很多函数都没有在OpenGL ES的源码中实现。


I/libagl ( 2445): in eglChooseConfig
I/libagl ( 2445): in eglCreateWindowSurface
I/libagl ( 2445): in eglCreateContext
I/libagl ( 2445): in eglMakeCurrent
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API
E/libEGL ( 2445): called unimplemented OpenGL ES API



5. 学习OpenGL ES 应用程序，了解基本的OpenGL ES API

学习OpenGL ES API是为了了解基本的API函数的工作原理和一些3D术语，从而配合三星s3c6410（0718）文档，实现自己的OpenGL ES硬件加速库。
![img](D:\my-note\opengles\android\assets\a4943901a140da357aec2c5c.jpg)

（1） Vertex Arrays/Buffer Objects
（2） Vertex Shader
实现对定点通用的一些操作
Vertex shaders can be used for traditional vertex-based operations such as transforming the position by a matrix, computing the lighting equation to generate a per-vertex color, and generating or transforming texture coordinates. Alternately, because the vertex shader is specified by the application, vertex shaders can be used to do custom vertex transformations
（3） Primitive Assembly
将Vertex Shader中的数据汇编成可以画出来的几何图形，比如三角形、直线等
（4） Rasterizition
将前一步的数据进行绘制，同时将前几个阶段的图元转换成二维的片段，传递给fragment shader
（5） Fragment Shader
对Fragment进行操作。
（6） Per-Fragment Shader
对每个片段进行判断(是否可见)和处理（混合、抖动处理）。
（7） 在FrameBuffer中进行显示。

6 s3c6410 驱动分析
6410的g3d的驱动代码位于Kernel/drivers/media/video/Samsung/g3d中的s3c_fimg3d.c中。

从s3c_fimg3d.c中我们可以获取什么知识？？Almost Nothing！
我这里有从网上下载的另一个版本的6410的g3d的驱动，比较全。
点击打开

小小分析：
在驱动中的s3c_g3d_probe中， 
主要做了get_resouce request_mem_region ioremap等操作
/* get the memory region for the post processor driver */
res = platform_get_resource(pdev, IORESOURCE_MEM, 0);
这句话获取了s3c-g3d的IO资源。

s3c_g3d probe() called
res.start=72000000,res.end=72ffffff,rest.name=s3c-g3d,res.flags=512
before ioremap:
s3c_g3d_mem->start=72000000,s3c_g3d_mem->end=72ffffff,s3c_g3d_mem->name=s3c-g3d,s3c_g3d_mem->flags=-2147483648
after io remap:
s3c_g3d_base=d5000000
s3c_g3d version : 0x1050000
S3C G3D Init : Done

应用程序怎么访问g3d的寄存器呢？是通过mmap操作来访问的，下面是一个通过应用程序访问g3d寄存器的一个测试。


int main()
{
  struct s3c_3d_mem_alloc mem_para;
int fd=open("/dev/s3c-g3d",O_RDWR| O_SYNC);
char arg[100];
int i;
if(fd==-1)
{
  printf("open s3c-g3d error\n");
  return 0;
}

printf("test mmap of default\n");

  struct stat file_stat; 
  if (fstat(fd, &file_stat) < 0)
  { 
    printf("fstat wrong"); 
   exit(1); 
  } 
    
  printf("file size:%d\n", file_stat.st_size);
  
  void *sfp;
  
  if((sfp=mmap(NULL, 2048, PROT_READ|PROT_WRITE,MAP_SHARED, fd, 0)) == MAP_FAILED) 
  {  
    printf("mmap wrong!"); 
    exit(0); 

  } 
  printf("sfp=%p\n",sfp);
printf("version=%x\n",*((int*)(sfp+0x10)));//偏移为10的寄存器是g3d版本寄存器
return 0;
}

打印信息：
/ # ./test

in s3c_g3d_open******
test mmap of default
file size:0
hahahahahhahha&&&&&&&&&&&&&&&&MMAP : vma->end = 0x4001c000, vma->start = 0x4001b000, size = 4096
sfp=0x4001b000
version=1050000/*****这是在应用程序中获取的g3d的version信息，说明可以通过读取寄存器获取数据******/
  
  

硬件加速的实现方法

1. 1.修改Loader.cpp文件里的Loader：：loader函数，添加gConfig.add( entry_t(0, 1, "mmoid") );或者修改egl.cfg文件。此文件位于/system/lib/egl/下，如果不存在，可以自己动手新建。格式是“dpy impl tag”比如自己添加的硬件加速库是libGLES_mmoid.so,则需要在此文件里这样编写

0 1 mmoid
修改后要重启才可生效。

2. 以上只是方法，要实现硬件加速，必须自己编写libGLES_mmoid.so。这个过程最复杂。需要对g3d的驱动进行调用，从上面的测试例子中可以看出，驱动中只是做了个简单的封装，对g3d的调用实际上是从应用程序层次进行调用的。这样的话，需要自己编写对g3d调用的实现库，实际上是对g3d驱动的封装。
3. 在opengl api中实现对上述封装库的调用。


7 s3c6410 文档分析
在熟悉s3c6410的g3d文档，搞清楚各个寄存器的使用方法ing。编写调用g3d驱动的实现库。



8. 遇到的问题

（1）没有可供分析的sample代码，只能自己分析。
（2）OpenGL ES专业术语和算法比较多，短时间不好掌握
（3）这部分OpenGL ES的实现是不是ip厂商会提供呢？在0718上是不是有已经实现了的厂商提供的二进制OpenGL ES硬件加速代码了呢？
（4）OpenGL ES的很多操作都是Pipeline，想要实现部分函数的硬件加速，是不是其他函数也能影响到呢？

 