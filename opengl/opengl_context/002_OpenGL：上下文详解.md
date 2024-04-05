# OpenGL：上下文详解



## 1 前言

### 1.1 上下文介绍

OpenGL上下文是什么？有什么用？前言部分将会回答这两个问题，而至于具体怎么实现，实现的结构如何将在第二部分来回答。

首先OpenGL的上下文是什么呢？看到一个比较形象的比喻，就好比一个画家在画图，OpenGL就是这个画家（可以发出各种指令），而画家作画需要用到的画笔、画布等东西就是Contex，Contex的切换就像画家同时作多幅画，当要画另外一幅画的时候，画家需要放下原来的画笔，拿起这幅画所需要的画笔。想到了什么？上下文（Context）是线程私有的，在线程中绘制的时候，需要为每一个线程指定一个Current Context的，多个线程不能指向同一个Context。



OpenGL直到创建了OpenGL Context后才会存在。由不同平台的API自己去创建。 以下讨论的是基于Windows平台的，所以叫WGL， 而Linux 平台借助X11接口，称为GLX。因此，许多Windows平台下的接口都以wgl开头。



### 1.2 上下文的创建方法

对于Windows平台首先创建一个设备上下文（Device Context，DC），DC的创建可以输入一个GPU参数，从而指定DC和DC对应的上下文将在哪个GPU上跑。以DC为输入，可以创建一个绘制上下文。创建绘制上下文以后，调用MakeCurrent，将创建的上下文设置为当前的绘制上下文。glew库中的wglew.h封装了windows下创建上下文的方法。

### 1.3 句柄的概念

（创建设备上下成功之后回传传给设备环境句柄，经常会用到句柄的概念，介绍一下句柄。句柄实际上是Windows在内存中维护的一个对象内存物理地址列表的整数索引，而不是对象的地址指针。因为Windows的内存管理经常会将空闲对象的内存释放掉，当需要访问时再重新提交到物理内存，所以对象的物理地址是变换的，不允许程序直接通过物理地址来访问对象。程序将需要访问对象的句柄传递给系统，系统根据句柄检索对象列表从而获得对象及其物理地址。）

![img](./assets/v2-352b739a3ea80878e5e1720a9408a158_1440w.webp)



## 2 CreateWindowGL

首先介绍一下Windows程序必要的步骤。

1、定义程序入口WinMain函数

2、定义窗口处理函数（可以自定义，作用：处理信息）

3、注册窗口类函数（向操作系统内核中写入一段数据）

4、创建窗口（在内存中创建窗口）

5、显示窗口（绘制窗口图像，如果和OpenGL结合，还需要注意OpenGL中的上下文）

5.1、创建OpenGL上下文

5.2、使上下文成为当前上下文

6、消息循环（获取，翻译，派发）

7、消息处理

### 2.1 定义程序入口WinMain函数

### 2.2 定义窗口处理函数

窗口处理函数是非常重要的，第三步中注册窗口类的函数会用到的WNDPROC lpfnWndProc，在Windows中，应用程序通过要求Windows完成指定操作，而承担这项通信任务的API函数就是Windows的相应窗口函数WndProc。在DOS里，程序能直接控制事件的发生顺序；而在Windows里，应用程序不直接调用任何窗口函数，而是等待Windows调用窗口函数，请求完成任务或返回信息。

总而言之，WndProc函数的作用就是拦截并处理系统信息以及**自定义信息**，比如windows程序会产生很多消息，单机鼠标移动窗口都会产生信息，这个函数就是默认的消息处理函数。可以重载这个函数来制定自己的消息处理流程。在接下来结合OpenGL的实现中，我会在在WndProc函数中的WM_PAINT中来定义绘制函数。这也是一个主回调函数。

### 2.3 注册窗口类函数

**2.3.1 窗口类的概念以及分类**

窗口类包含窗口分各种参数信息结构，每个窗口都有一个窗口类，基于窗口类来创建的窗口。并且每个窗口类都有一个名称，使用前必须注册到操作系统。

窗口的分类有以下几种：

- 系统已经定义好的窗口类：所有应用程序都可以使用，例如Button（按钮），edit（编辑框）。
- 应用程序全局窗口类：由用户自己定义，当前应用程序所有模块中都可以使用。
- 应用程序局部窗口类：由用户自己定义，当前应用程序中只有本模块可以使用。

**2.3.2 注册窗口类的函数**

```cpp
typedef struct tagWNDCLASSA {
  UINT      style;               //窗口风格
  WNDPROC   lpfnWndProc;         //窗口处理函数
  int       cbClsExtra;          //窗口类的附加数据buff的大小
  int       cbWndExtra;          //窗口类的附加数据buff的大小
  HINSTANCE hInstance;           //当前模块的实例句柄,该实例包含类的窗口过程。
  HICON     hIcon;               //窗口图标句柄
  HCURSOR   hCursor;             //鼠标句柄
  HBRUSH    hbrBackground;       //绘制窗口背景的画刷句柄
  LPCSTR    lpszMenuName;        //窗口菜单的资源id字符串
  LPCSTR    lpszClassName;       //窗口类的名称
} WNDCLASSA, *PWNDCLASSA, *NPWNDCLASSA, *LPWNDCLASSA;
```

### 2.3.3 注册窗口类的代码

```cpp
    //注册窗口类
    WNDCLASS wndcls = { 0 };
    wndcls.style = CS_HREDRAW | CS_VREDRAW;
    wndcls.lpfnWndProc = WinProc;
    wndcls.hInstance = hInstance;
    wndcls.hIcon = LoadIcon(NULL, IDI_INFORMATION);
    wndcls.hCursor = LoadCursor(NULL, IDC_ARROW);
    wndcls.hbrBackground = (HBRUSH)GetStockObject(WHITE_BRUSH);
    wndcls.lpszClassName = L"zhuhanruitest";
    RegisterClass(&wndcls);
```

### 2.4 创建窗口

WIN32终于到了创建窗口相关的准备工作，首先介绍一下创建窗口函数：

```text
HWND CreateWindowExW(
  [in]           DWORD     dwExStyle,          //窗口的拓展风格
  [in, optional] LPCWSTR   lpClassName,        //已经注册的窗口类名称
  [in, optional] LPCWSTR   lpWindowName,       //窗口标题栏名称
  [in]           DWORD     dwStyle,            //窗口的基本风格
  [in]           int       X,
  [in]           int       Y,
  [in]           int       nWidth,
  [in]           int       nHeight,            //以上四个参数是决定窗口的位置
  [in, optional] HWND      hWndParent,         //父窗口句柄
  [in, optional] HMENU     hMenu,              //窗口的菜单句柄
  [in, optional] HINSTANCE hInstance,          //当前应用程序实例句柄
  [in, optional] LPVOID    lpParam             //窗口创建时的附加参数
);
```

### 2.5 显示窗口

对上述进行总结，首先创建窗口并获取设备上下文，并指给设备环境句柄。代码如下：

```cpp
HWND hwnd = CteateWindow......
HDC hdc = GetDC(hwnd);
```

然后得到设备上下文就可以创建传说中的OpenGL上下文。

**2.5.1 创建OpenGL上下文**

具体怎么获得OpenGL的上下文呢？通过wglCreateContext接口来获得OpenGL上下文，代码

```cpp
HGLRC hglrc = wglCreateContext(hdc);  //创建OpenGL Context，hglrc。
```

**2.5.2 使上下文成为当前上下文**

然后通过wglMakeCurrent接口来设置当前的上下文，代码如下：

```text
wglMakeCurrent(hdc,hglrc)   //两个参数，一个设备上下文，一个OpenGL上下文。
```

[Windows编程基础，第一个Windows程序，注册窗口，创建窗口-CSDN博客](https://blog.csdn.net/qq_73985089/article/details/130491700)

### 3 WIN32+OpenGL使用方法实战

再总结一下，如果创建一个没有任何窗口库的win32 OpenGL窗口，需要经历以下几个步骤。

Step1：注册窗口类

Step2：创建窗口

Step3：创建OpenGL上下文

Step4：使上下文成为当前上下文

Step5：显示窗口

```cpp
首先定义全局变量：
HDC mDeviceContext;
HWND mWindowHandle;
HGLRC mRenderContext;
LRESULT handleMessage(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam);
代码步骤一：主函数入口定义，Step1步骤注册窗口类函数在此定义，
int WINAPI WinMain( __in HINSTANCE hInstance, __in_opt HINSTANCE hPrevInstance, __in LPSTR lpCmdLine, __in int nShowCmd )
{
    //注册窗口类
    WNDCLASS wndClass = { 0 };
    wndClass.style = CS_HREDRAW | CS_VREDRAW | CS_OWNDC;
    wndClass.lpfnWndProc = WinProc;
    wndClass.cbClsExtra = 0;
    wndClass.cbWndExtra = 8;
    wndClass.hInstance = hInstance;
    wndClass.hIcon = NULL;
    wndClass.hCursor = LoadCursor(NULL, IDC_ARROW);
    wndClass.hbrBackground = (HBRUSH)GetStockObject(BLACK_BRUSH);
    wndClass.lpszMenuName = NULL;
    wndClass.lpszClassName = L"xxx";
    RegisterClass(&wndClass);
    //创建窗口
    createWindowGL(mDeviceContext, hInstance);     //该函数是自定义函数，里面主要进行了窗口的创建，像素格式的配置，上下文的创建与绑定。
    /*******************************************************************************/
    //显示窗口
    ShowWindow(mWindowHandle, SW_NORMAL);
    //deleteOpenGLSet();
    //更新窗口
    UpdateWindow(mWindowHandle);
    //deleteOpenGLSet();
    //消息循环
    MSG msg;
    BOOL bRet;
    while ((bRet = GetMessage(&msg, mWindowHandle, 0, 0)) != 0)
    {
        if (bRet == -1)
        {
            break;
        }
        else
        {
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
    }
    return msg.wParam;
}
```

然后step2-step4都是在createWindowGL函数中进行的，函数具体代码如下：

```cpp
void destroyWindow(HWND handle)  //销毁窗口句柄函数
{
    DestroyWindow(handle);
}
BOOL createWindowGL(HDC hDC,HINSTANCE hInstance)  //创建窗口函数
{
	PIXELFORMATDESCRIPTOR pfd =
	{
		sizeof(PIXELFORMATDESCRIPTOR),
		1,
		PFD_DRAW_TO_WINDOW | //绘制到窗口
		PFD_SUPPORT_OPENGL | //支持opengl
		PFD_DOUBLEBUFFER,    //采用双缓冲
		PFD_TYPE_RGBA,       //像素类型 RGBA
		32,                  //像素位数 4*8- 32
		0, 0, 0, 0, 0, 0,
		0,0,
		0, 0, 0, 0, 0,
		32,                  //深度缓冲区位数
		0,                   //模板缓冲
		0,
		PFD_MAIN_PLANE,
		0,
		0, 0,
	};
    mWindowHandle = CreateWindow(L"xxx", L"1xxx", 
                                 WS_POPUP | WS_CLIPCHILDREN | WS_CLIPSIBLINGS,
                                 100, 100, SCR_WIDTH, SCR_HEIGHT, NULL, NULL,
                                  hInstance, NULL);  //Step2步骤，该函数是进行窗口的创建，进行尺寸、name的设置之类的
    if (mWindowHandle == NULL) {
        cout << "Could not create Window, error:" << GetLastError() << endl;
        return false;
    }
    mDeviceContext = GetDC(mWindowHandle); //Step3步骤，通过m_mWindowHandle窗口句柄创建mDeviceContext设备上下文,
    if (mDeviceContext == NULL)
    {
        destroyWindow(mWindowHandle);
        mWindowHandle = NULL;
        return false;
    }
    //
	int pixelformat;
	if (0 == (pixelformat = ChoosePixelFormat(mDeviceContext, &pfd))) //选择像素格式
	{
		return FALSE;
	}
	if (!::SetPixelFormat(mDeviceContext, pixelformat, &pfd))         //设置像素格式
	{
		return FALSE;
	}
	//HGLRC tempContext = wglCreateContext(hdc);
	//wglMakeCurrent(hdc, tempContext);
    mRenderContext = wglCreateContext(mDeviceContext);   //通过设备上下创建opengl渲染上下文
    cout << "Create renfer context is :" << (unsigned int)mRenderContext << endl;
    if (mRenderContext == NULL)
    {
        destroyWindow(mWindowHandle);
        return false;
    }
    wglMakeCurrent(mDeviceContext,mRenderContext);  //将opengl上下文进行初绑定为当前的上下文，
                                                    //makecurrent的参数是设备上下文和对应的openg上下文
    glewInit();           //opengl相关的初始化
    InitFramebuffer1();  //初始化帧缓冲定义函数，要小心不能在渲染循环中初始化，这是我自己做的测试demo，测试帧缓冲用的
    InitShaderAndBuffer();//这是我自己做的测试demo，测试画三角形用的，是定义了shader并进行了vao、vbo的顶点配置。
    wglMakeCurrent(mDeviceContext, NULL);
    return TRUE;
}
```

上面配置完成之后，还需要对注册窗口时绑定的WinProc这个回调函数进行配置，代码如下：

```cpp
LRESULT CALLBACK WinProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)  //我分开来写了，其实都是一样的。
{

    handleMessage(hWnd, uMsg, wParam, lParam);
    return handleMessage(hWnd, uMsg, wParam, lParam);
}
LRESULT handleMessage(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    switch (message)
    {
    case WM_DESTROY:
    {
        return 0;
    }
    case WM_PAINT:
    {
        wglMakeCurrent(mDeviceContext, mRenderContext);//hglrcopengl设备上下文--》绑定-->hdc当前设备上下文
        onPaint();//绘制函数，在这个函数中绘制自己的图形，相当于glfw里面的while循环
        SwapBuffers(mDeviceContext);//交换前后缓冲区
        wglMakeCurrent(mDeviceContext, NULL);//释放设备上下文
        break;
    }
    case WM_SIZE:
    {
        UINT cx = LOWORD(lParam);
        UINT cy = HIWORD(lParam);
        glViewport(0, 0, cx, cy);//根据窗口的实时变化重绘窗口
        break;
    }
    case WM_MOVE:
    {

        POINTS points = MAKEPOINTS(lParam);


        return 0;
    }
    default:
        return DefWindowProc(hWnd, message, wParam, lParam);
    }
    return 0;
}

在这里主要是需要定义onPaint()函数，其实这个时候初始化已经完场了，可以自由发挥了。
void onPaint()   //相当于glfw中while循环里的东西
{
    drawScene();
}
```

[https://www.codenong.com/cs105518391/](https://link.zhihu.com/?target=https%3A//www.codenong.com/cs105518391/)

[VS 2022配置openGL环境（GLFW+GLEW）](https://link.zhihu.com/?target=https%3A//blog.csdn.net/FallenChild/article/details/128044052)

[21.1 GLFW多线程渲染 | 21. 多线程渲染 | 游戏引擎 浅入浅出 | 游戏人生](https://link.zhihu.com/?target=https%3A//www.thisisgame.com.cn/tutorial%3Fbook%3Dcpp-game-engine-book%26lang%3Dzh%26md%3D21.%20multithreaded_rendering/21.1%20glfw_multithreaded_rendering.md)

[openGL之多线程渲染 - yang131 - 博客园](https://link.zhihu.com/?target=https%3A//www.cnblogs.com/yang131/p/16753516.html)

编辑于 2023-12-27 19:04・IP 属地美国