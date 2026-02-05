# OpenGL 创建OpenGL上下文(OpenGL Context WGL)


## OpenGL Context

OpenGL Context是实现完整的opengl的一部分。OpenGL直到创建了OpenGL Context后才会存在。由不同平台的API自己去创建。 以下讨论的是基于Windows平台的，所以叫WGL， 而Linux 平台借助X11接口，称为GLX。因此，许多Windows平台下的接口都以wgl开头。


## 窗口

### 创建HWND

```
HWND hwnd = CreateWindowEx(
        0,                              //窗口扩展风格
        _T("OpenGLWindowClass"),        //指向注册类名的指针
        _T("OpenGL Window"),            //指向窗口名称的指针
        WS_OVERLAPPEDWINDOW,            //窗口风格
        CW_USEDEFAULT,                  //窗口水平位置
        CW_USEDEFAULT,                  //窗口垂直位置
        windowWidth,                    //窗口宽度
        windowHeight,                   //窗口深度
        NULL,                           //父窗口的句柄
        NULL,                           //菜单的句柄或是子窗口的标识符
        hInstance,                      //应用程序实例的句柄
        NULL                            //指向窗口的创建数据
    );

HDC hdc = GetDC(hwnd);					// Device Context
```

​    


### Pixel Format

每个窗口在MS Windows系统中，都有一个Device Context(DC)与之关联。 该对象可以存储一种称为"Pixel Format"的东西。这是一个通用的结构，来描述O彭GL 上下文创建时应该具有的默认的framebuffer的属性。
设置pixel format的属性是非直观的。创建pixel format之前，你需要填充一个结构体。该结构体如下：

```
PIXELFORMATDESCRIPTOR pfd =
{
	sizeof(PIXELFORMATDESCRIPTOR),
	1,
	PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER,    // Flags
	PFD_TYPE_RGBA,        // The kind of framebuffer. RGBA or palette.
	32,                   // Colordepth of the framebuffer.
	0, 0, 0, 0, 0, 0,
	0,
	0,
	0,
	0, 0, 0, 0,
	24,                   // Number of bits for the depthbuffer
	8,                    // Number of bits for the stencilbuffer
	0,                    // Number of Aux buffers in the framebuffer.
	PFD_MAIN_PLANE,
	0,
	0, 0, 0
};
```

```
PIXELFORMATDESCRIPTOR pfd = {};
pfd.nSize = sizeof(PIXELFORMATDESCRIPTOR);
// 只设置了重要的部分，即上面结构体添加注释的部分
pfd.nVersion = 1;
pfd.dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER;
pfd.iPixelType = PFD_TYPE_RGBA;
pfd.cColorBits = 32;
pfd.cDepthBits = 24;
pfd.cStencilBits = 8;
pfd.iLayerType = PFD_MAIN_PLANE;

// 需要转换成pixel format number
int pixelFormat = ChoosePixelFormat(hdc, &pfd); //入参为device context 和 pfd结构体
```


该结构传递给一个函数（ChoosePixelFormat， 如上所示），该函数返回一系列匹配上述结构体的pixel format的列表的数量。 然后这个数量被用来设置DC下的pixel format的数量。
使用的接口为：SetPixelFormat

```
SetPixelFormat(hdc, pixelFormat, &pfd)
```


### 创建上下文(Create Context)

接下来我们就可以通过wglCreateContext接口，得到OpengGL的上下文了

```
HGLRC hglrc = wglCreateContext(hdc);			//创建OpenGL Context, hglrc
```


创建之后，必须使该上下文为当前上下文，使用wglMakeCurrent 接口

```
wglMakeCurrent(hdc, hglrc)
```


### MakeCurrent

当新创建的OpengGL Context通过结果wglMakeCurrent设置之后，原来旧的上下文(old context)就会被新的OpenGL context替代。在这之后，OpenGL API就会参考新的上下文的状态。

当前上下文(current context)是线程相关的(thread-specific), 每个线程可以拥有一个不同的current context。 多线程公用一个current context是非常危险的。


### 删除上下文(Delete Context)

在删除之前，必须保证当前上下文不是之前创建的OpenGL Context， 可以通过传递NULL值来实现这一操作。之后通过wglDeleteContext 删除之前创建的OpenGL Context

```
wglMakeCurrent(NULL, NULL);  //确保删除的不是current context
wglDeleteContext(hglrc);		
```


### 如何正确创建Context

除非是非常简单的Application, 否则不应使用上述简单的创建步骤。有一系列WGL extension 来更好的创建Context。当然这比上面简单的创建过程相对复杂一些。


### 创建一个假的Context

key point: 用于获取WGL extension的接口自身，就是一个OpenGL extension. 因此像其他OpenGL API一样，调用该接口需要一个OpenGL Context。 因此我们不得不创建一个OpenGL Context， 用来为接口调用创造环境，幸运的是，该上下文并不是最终的context。我们要做的就是创建一个dummy context，来获取函数指针(get function pointers), 然后直接使用这些接口。


注意： 不幸的是， Windows 不允许用户修改窗口的pixel format. 你只能设置一次。因此，如果你想使用不同于dummy context使用的像素格式，你需要销毁窗口，在我们使用完dummy context之后重新创建你想要的特定pixel format。


一个较好的dummy context正如我们上面所示的pixel format, 这通常会得到硬件加速的像素格式。

因此这一步就意味着创建一个像之前小节里面的Context。


### 获取WGL Extensions

如果借助扩展加载库(extension loading library), 现在是时候调用所需的函数来加载感兴趣的函数指针了。 如果不借助扩展加载库，那么需要手动完成。


### 加载OpenGL Functions

在创建完OpenGL Context之后，加载OpenGL Functions是非常中还要的任务。

查询函数指针并不在OpenGL API中，而是和具体平台相关。


### Windows

获取函数的接口：wglGetProcAddress

```
void *GetAnyGLFuncAddress(const char *name)     //name 表示function的名称，大小写敏感，严格匹配
{
  void *p = (void *)wglGetProcAddress(name);    // 获取函数地址
  if(p == 0 ||
    (p == (void*)0x1) || (p == (void*)0x2) || (p == (void*)0x3) ||
    (p == (void*)-1) )							// 其他情况
  {
    HMODULE module = LoadLibraryA("opengl32.dll");
    p = (void *)GetProcAddress(module, name);	
  }

  return p;
}
```


wglGetProcAddress 不会返回 从OpenGL32.DLL 里面通过export 声明的任何OpenGL API的函数指针。OpenGL version 1.1也是如此。 幸运的是，这些接口可以通过Win32下的GetProcAddress来获取。换句话说，GetProcAddress不会得到wglGetProcAddress能获取到的函数指针。因此在wglGetProcAddress失败的情况下，继续使用GetProcAddress接口来尝试获取。


### 函数原型(Function Prototypes)

这些都依赖于获取函数指针，上面我知道如何查询它们，我们还必须有地方存储这些它们。

C语言的函数指针有具体的声明，如

```
int (*c_fun_point)(int a, int b, char *c);   			//  函数指针
typedef int (*C_Fuc_Point)(int a, int b, char *c);    	//  C_Fun_Point 为返回类型为int的函数指针
```

如果指针实际使用的函数签名与我们存储指针值的函数签名不同，那么就会出现各种问题。OpenGL Registry（OpenGL 注册机制）提供了对一个头文件的访问，该头文件包含了所有扩展和扩展核心函数(version1.2 和之上)的函数指针定义。 该头文件为 glext.h。另外 glxext.h和wglext.h头文件，包含了GLX 和 WGL的extension。

如果不使用OpenGL 加载库，那就必须要么使用这些其中的某个头文件，或者自己生成一个头文件。

ext.h 头文件本身并不定义实际的函数指针，它们定义了函数指针的类型，以glUseProgram为例， ext.h有一个函数指针的类型

```
typedef void (APIENTRYP PFNGLUSEPROGRAMPROC) (GLuint program);
```

类型定义的名称为 PFNGLUSEPROGRAMPROC, Pointer to the FunctioN glUseProgram, 是一个PROCedure.
APIENTRYP是使一切正常工作所需的一些宏观魔术的一部分。

使用typedef使我们可以更容易地在代码中定义适当类型的函数指针:

```
PFNGLUSEPROGRAMPROC glUseProgram;
```

这不是头文件定义的;我们必须自己定义它。而大多数OpenGL加载库不需要你自己定义它们（这就是推荐使用OpenGL加载库的原因，如GLAD）。


### 函数检索(Function Retrieval)

一旦我们有了一个实际的函数指针和我们的函数检索函数，我们就可以得到问题中的函数指针了。然而一个问题产生了： 我们应该得到这个函数吗？

如果函数是OpenGL core function, 我们需要检车OpenGL version. 在3.0之前，*glGetString(GL_VERSION)*获取版本号。3.0和之后的版本，通过glGetIntegerv(GL_MAJOR_VERSION) 和 GL_MINOR_VERSION 获取大小版本号。

当明确某个接口是可以获取之后，可以通过下面的方式进行提取

```
//In a header somewhere.
```cpp
```cpp
#include <glext.h>
PFNGLUSEPROGRAMPROC glUseProgram;

//In an initialization routine
glUseProgram = (PFNGLUSEPROGRAMPROC)wglGetProcAddress("glUseProgram");
```


```
```
### Pixel Format Extension

之前小节描述的PFD struct有一个巨大的漏洞： 不可扩展。因此，有一个可扩展的pixel format:
WGL_ARB_pixel_format。这个扩展定义了一种新的获取pixel format number的机制，基于提供一系列的属性和值。

为了使用该特性，扩展

```
BOOL wglChoosePixelFormatARB(   HDC hdc,
                                const int *piAttribIList,
                                const FLOAT *pfAttribFList,
                                UINT nMaxFormats,
                                int *piFormats,
                                UINT *nNumFormats);
```

该函数的作用和ChoosePixelFormat作用一样，这里的入口参数从PFD struct 变成了属性和值的列表。


**piAttribIList**: 一系列整数属性， 每个元素由属性/值(attribute/value) 构成。 属性0表示属性列表结束，并且不需要值。

**pfAttribFList** 是浮点属性列表（a list of floating-point attributes), 每个元素由属性/值构成。 如何将属性是整数的类型放入到float类型的列表呢？ 必须非常小心，如果是C++， 需要使用static_cast ， 如果是C, 其他技巧使C保持整数和浮点数之间的位模式相同。

**nMaxFormats** 存储在piFormats中最大的数量

**piFormats**: 包含条目的列表

**nNumFormats** 是一个返回值，表示有多少条目存储在上述列表中


如果上述函数返回False， 表示未找到合适的pixel format. 尽管没有找到pixel format, piFormat 列表处于未定义的状态。

```
const int attribList[] =
{
    WGL_DRAW_TO_WINDOW_ARB, GL_TRUE,
    WGL_SUPPORT_OPENGL_ARB, GL_TRUE,
    WGL_DOUBLE_BUFFER_ARB, GL_TRUE,
    WGL_PIXEL_TYPE_ARB, WGL_TYPE_RGBA_ARB,
    WGL_COLOR_BITS_ARB, 32,
    WGL_DEPTH_BITS_ARB, 24,
    WGL_STENCIL_BITS_ARB, 8,
    0, // End
};

int pixelFormat;
UINT numFormats;

wglChoosePixelFormatARB(hdc, attribList, NULL, 1, &pixelFormat, &numFormats);
```


除了wglChoosePixelFormatARB接口，还有以下三个接口：

```
WGL_ARB_pixel_format_float(...) // Allows for floating-point framebuffers.
WGL_ARB_framebuffer_sRGB(...)   // Allows for color buffers to be in sRGB format.
WGL_ARB_multisample(...)        // Allows for multisampled framebuffers.
```

一旦获取了pixel format的数量，接下来就可以调用**SetPixelFormat** 设置


### 创建带有属性的上下文

WGL_ARB_create_context， 替代之前的wglCreateContext. 就像wglChoosePixelFormatARB, 该接口也增加了扩展机制。
如果上下文没有暴露这个扩展， 则不能使用该特性，而只能使用常规的wglCreateContext。
如果明确声明了这个扩展，则具有以下新的特性：


保证获取到的OpenGL Context 版本不低于3.0

创建OpenGL 3.2 或者更高版本的core， 不带有兼容性

创建不带有窗口的Context, 用来做离屏渲染(off-screen rendering)

```
HGLRC wglCreateContextAttribsARB(HDC hDC, HGLRC hshareContext, const int *attribList);
```


example

```
int pArray_0[7] = (WGL_CONTEXT_MAJOR_VERSION_ARB, 4, 
				   WGL_CONTEXT_MINOR_VERSION_ARB, 6, 
				   WGL_CONTEXT_PROFILE_MASK_ARB, WGL_CONTEXT_COMPATIBILITY_PROFILE_BIT_ARB, 
				   0);
wglCreateContextAttribsARB(hDC = hdc_1, hShareContext = NULL, attribList = pArray_0) = gc_2;
wglMakeCurrent(hdc = hdc_1, hglrc = gc_2);
```


### 示例

```C++
#include <Windows.h>
#include <GL/gl.h>
#include <tchar.h>
#include <iostream>


#define SIMPLE_DEMO 0

#if defined SIMPLE_DEMO && SIMPLE_DEMO ==1

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow)
{
    // Create a window
    const char* className = "OpenGLWindowClass";
    const char* windowTitle = "OpenGL Window";
    const int windowWidth = 800;
    const int windowHeight = 600;

    WNDCLASSEX wc = {};
    wc.cbSize = sizeof(WNDCLASSEX);
    wc.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
    wc.lpfnWndProc = WindowProc;
    wc.hInstance = hInstance;
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    wc.lpszClassName = _T("OpenGLWindowClass");

    if (!RegisterClassEx(&wc))
    {
        MessageBox(NULL, _T("Failed to register window class"), _T("Error"), MB_OK | MB_ICONERROR);
        return -1;
    }

    HWND hwnd = CreateWindowEx(
        0,                              //窗口扩展风格
        _T("OpenGLWindowClass"),        //指向注册类名的指针
        _T("OpenGL Window"),            //指向窗口名称的指针
        WS_OVERLAPPEDWINDOW,            //窗口风格
        CW_USEDEFAULT,                  //窗口水平位置
        CW_USEDEFAULT,                  //窗口垂直位置
        windowWidth,                    //窗口宽度
        windowHeight,                   //窗口深度
        NULL,                           //父窗口的句柄
        NULL,                           //菜单的句柄或是子窗口的标识符
        hInstance,                      //应用程序实例的句柄
        NULL                            //指向窗口的创建数据
    );

    if (!hwnd)
    {
        MessageBox(NULL, _T("Failed to create window"), _T("Error"), MB_OK | MB_ICONERROR);
        return -1;
    }
    else
    {
        UpdateWindow(hwnd);
        ShowWindow(hwnd, SW_SHOW);
    }

    HDC hdc = GetDC(hwnd);

    // Set pixel format
    PIXELFORMATDESCRIPTOR pfd = {};
    pfd.nSize = sizeof(PIXELFORMATDESCRIPTOR);
    pfd.nVersion = 1;
    pfd.dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER;
    pfd.iPixelType = PFD_TYPE_RGBA;
    pfd.cColorBits = 32;
    pfd.cDepthBits = 24;
    pfd.cStencilBits = 8;
    pfd.iLayerType = PFD_MAIN_PLANE;

    int pixelFormat = ChoosePixelFormat(hdc, &pfd);
    if (!SetPixelFormat(hdc, pixelFormat, &pfd))
    {
        MessageBox(NULL, _T("Failed to set pixel format"), _T("Error"), MB_OK | MB_ICONERROR);
        return -1;
    }

    // Create and activate OpenGL context
    HGLRC hglrc = wglCreateContext(hdc);
    if (!wglMakeCurrent(hdc, hglrc))
    {
        MessageBox(NULL, _T("Failed to make OpenGL context current"), _T("Error"), MB_OK | MB_ICONERROR);
        return -1;
    }

    MessageBoxA(0, (char*)glGetString(GL_VERSION), "OPENGL VERSION", 0);

    // Now you have an active OpenGL context
    
    // Main message loop
    MSG msg = {};
    while (GetMessage(&msg, NULL, 0, 0))
    {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    
    // Clean up and exit
    wglMakeCurrent(NULL, NULL);
    wglDeleteContext(hglrc);
    ReleaseDC(hwnd, hdc);
    DestroyWindow(hwnd);

    return 0;
}

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
    switch (uMsg)
    {
    case WM_CLOSE:
        PostQuitMessage(0);
        return 0;

    case WM_DESTROY:
        return 0;
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

#else 
// Sample code showing how to create a modern OpenGL window and rendering context on Win32.

#include <stdbool.h>

typedef HGLRC WINAPI wglCreateContextAttribsARB_type(HDC hdc, HGLRC hShareContext, const int* attribList);

wglCreateContextAttribsARB_type* wglCreateContextAttribsARB;

// See https://www.opengl.org/registry/specs/ARB/wgl_create_context.txt for all values
#define WGL_CONTEXT_MAJOR_VERSION_ARB             0x2091
#define WGL_CONTEXT_MINOR_VERSION_ARB             0x2092
#define WGL_CONTEXT_PROFILE_MASK_ARB              0x9126

#define WGL_CONTEXT_CORE_PROFILE_BIT_ARB          0x00000001

typedef BOOL WINAPI wglChoosePixelFormatARB_type(HDC hdc, const int* piAttribIList, const FLOAT* pfAttribFList, UINT nMaxFormats, int* piFormats, UINT* nNumFormats);

wglChoosePixelFormatARB_type* wglChoosePixelFormatARB;

// See https://www.opengl.org/registry/specs/ARB/wgl_pixel_format.txt for all values
#define WGL_DRAW_TO_WINDOW_ARB                    0x2001
#define WGL_ACCELERATION_ARB                      0x2003
#define WGL_SUPPORT_OPENGL_ARB                    0x2010
#define WGL_DOUBLE_BUFFER_ARB                     0x2011
#define WGL_PIXEL_TYPE_ARB                        0x2013
#define WGL_COLOR_BITS_ARB                        0x2014
#define WGL_DEPTH_BITS_ARB                        0x2022
#define WGL_STENCIL_BITS_ARB                      0x2023

#define WGL_FULL_ACCELERATION_ARB                 0x2027
#define WGL_TYPE_RGBA_ARB                         0x202B

static void
fatal_error(const char* msg)
{
    MessageBoxA(NULL, msg, "Error", MB_OK | MB_ICONEXCLAMATION);
    exit(EXIT_FAILURE);
}

static void
init_opengl_extensions(void)
{
    // Before we can load extensions, we need a dummy OpenGL context, created using a dummy window.
    // We use a dummy window because you can only set the pixel format for a window once. For the
    // real window, we want to use wglChoosePixelFormatARB (so we can potentially specify options
    // that aren't available in PIXELFORMATDESCRIPTOR), but we can't load and use that before we
    // have a context.
    WNDCLASSA window_class = {};
    window_class.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
    window_class.lpfnWndProc = DefWindowProcA;
    window_class.hInstance = GetModuleHandle(0);
    window_class.lpszClassName = "Dummy_WGL_djuasiodwa";

    if (!RegisterClassA(&window_class)) {
        fatal_error("Failed to register dummy OpenGL window.");
    }

    HWND dummy_window = CreateWindowExA(
        0,
        window_class.lpszClassName,
        "Dummy OpenGL Window",
        0,
        CW_USEDEFAULT,
        CW_USEDEFAULT,
        CW_USEDEFAULT,
        CW_USEDEFAULT,
        0,
        0,
        window_class.hInstance,
        0);

    if (!dummy_window) {
        fatal_error("Failed to create dummy OpenGL window.");
    }

    HDC dummy_dc = GetDC(dummy_window);

    PIXELFORMATDESCRIPTOR pfd = {};
    pfd.nSize = sizeof(pfd);
    pfd.nVersion = 1;
    pfd.iPixelType = PFD_TYPE_RGBA;
    pfd.dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL | PFD_DOUBLEBUFFER;
    pfd.cColorBits = 32;
    pfd.cAlphaBits = 8;
    pfd.iLayerType = PFD_MAIN_PLANE;
    pfd.cDepthBits = 24;
    pfd.cStencilBits = 8;

    int pixel_format = ChoosePixelFormat(dummy_dc, &pfd);
    if (!pixel_format) {
        fatal_error("Failed to find a suitable pixel format.");
    }
    if (!SetPixelFormat(dummy_dc, pixel_format, &pfd)) {
        fatal_error("Failed to set the pixel format.");
    }

    HGLRC dummy_context = wglCreateContext(dummy_dc);
    if (!dummy_context) {
        fatal_error("Failed to create a dummy OpenGL rendering context.");
    }

    if (!wglMakeCurrent(dummy_dc, dummy_context)) {
        fatal_error("Failed to activate dummy OpenGL rendering context.");
    }

    wglCreateContextAttribsARB = (wglCreateContextAttribsARB_type*)wglGetProcAddress("wglCreateContextAttribsARB");
    wglChoosePixelFormatARB = (wglChoosePixelFormatARB_type*)wglGetProcAddress("wglChoosePixelFormatARB");

    wglMakeCurrent(dummy_dc, 0);
    wglDeleteContext(dummy_context);
    ReleaseDC(dummy_window, dummy_dc);
    DestroyWindow(dummy_window);
}

static HGLRC
init_opengl(HDC real_dc)
{
    init_opengl_extensions();

    // Now we can choose a pixel format the modern way, using wglChoosePixelFormatARB.
    int pixel_format_attribs[] = {
        WGL_DRAW_TO_WINDOW_ARB,     GL_TRUE,
        WGL_SUPPORT_OPENGL_ARB,     GL_TRUE,
        WGL_DOUBLE_BUFFER_ARB,      GL_TRUE,
        WGL_ACCELERATION_ARB,       WGL_FULL_ACCELERATION_ARB,
        WGL_PIXEL_TYPE_ARB,         WGL_TYPE_RGBA_ARB,
        WGL_COLOR_BITS_ARB,         32,
        WGL_DEPTH_BITS_ARB,         24,
        WGL_STENCIL_BITS_ARB,       8,
        0
    };

    int pixel_format;
    UINT num_formats;
    wglChoosePixelFormatARB(real_dc, pixel_format_attribs, 0, 1, &pixel_format, &num_formats);
    if (!num_formats) {
        fatal_error("Failed to set the OpenGL 3.3 pixel format.");
    }

    PIXELFORMATDESCRIPTOR pfd;
    DescribePixelFormat(real_dc, pixel_format, sizeof(pfd), &pfd);
    if (!SetPixelFormat(real_dc, pixel_format, &pfd)) {
        fatal_error("Failed to set the OpenGL 3.3 pixel format.");
    }

    // Specify that we want to create an OpenGL 4.6 core profile context
    int gl33_attribs[] = {
        WGL_CONTEXT_MAJOR_VERSION_ARB, 4,
        WGL_CONTEXT_MINOR_VERSION_ARB, 6,
        WGL_CONTEXT_PROFILE_MASK_ARB,  WGL_CONTEXT_CORE_PROFILE_BIT_ARB,
        0,
    };

    HGLRC gl33_context = wglCreateContextAttribsARB(real_dc, 0, gl33_attribs);
    if (!gl33_context) {
        fatal_error("Failed to create OpenGL 3.3 context.");
    }

    if (!wglMakeCurrent(real_dc, gl33_context)) {
        fatal_error("Failed to activate OpenGL 3.3 rendering context.");
    }

    return gl33_context;
}

static LRESULT CALLBACK
window_callback(HWND window, UINT msg, WPARAM wparam, LPARAM lparam)
{
    LRESULT result = 0;

    switch (msg) {
    case WM_CLOSE:
    case WM_DESTROY:
        PostQuitMessage(0);
        break;
    default:
        result = DefWindowProcA(window, msg, wparam, lparam);
        break;
    }

    return result;
}

static HWND
create_window(HINSTANCE inst)
{
    WNDCLASSA window_class = {};
    window_class.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
    window_class.lpfnWndProc = window_callback;
    window_class.hInstance = inst;
    window_class.hCursor = LoadCursor(0, IDC_ARROW);
    window_class.hbrBackground = 0;
    window_class.lpszClassName = "WGL_fdjhsklf";

    if (!RegisterClassA(&window_class)) {
        fatal_error("Failed to register window.");
    }

    // Specify a desired width and height, then adjust the rect so the window's client area will be
    // that size.
    RECT rect = { };
    rect.right = 1024;
    rect.bottom = 576;

    DWORD window_style = WS_OVERLAPPEDWINDOW;
    AdjustWindowRect(&rect, window_style, false);

    HWND window = CreateWindowExA(
        0,
        window_class.lpszClassName,
        "OpenGL",
        window_style,
        CW_USEDEFAULT,
        CW_USEDEFAULT,
        rect.right - rect.left,
        rect.bottom - rect.top,
        0,
        0,
        inst,
        0);

    if (!window) {
        fatal_error("Failed to create window.");
    }

    return window;
}


int WINAPI
WinMain(HINSTANCE inst, HINSTANCE prev, LPSTR cmd_line, int show)
{
    HWND window = create_window(inst);
    HDC gldc = GetDC(window);
    HGLRC glrc = init_opengl(gldc);

    ShowWindow(window, show);
    UpdateWindow(window);


    bool running = true;
    while (running) {
        MSG msg;
        while (PeekMessageA(&msg, 0, 0, 0, PM_REMOVE)) {
            if (msg.message == WM_QUIT) {
                running = false;
            }
            else {
                TranslateMessage(&msg);
                DispatchMessageA(&msg);
            }
        }

        glClearColor(1.0f, 0.5f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Do OpenGL rendering here

        SwapBuffers(gldc);
    }

    return 0;
}

#endif
```

