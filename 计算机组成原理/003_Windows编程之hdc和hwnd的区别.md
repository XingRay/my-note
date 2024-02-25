# Windows编程之hdc和hwnd的区别

> 在windows编程中**类型名前面加H**的基本是句柄
>  **常用句柄 :**
>  HBITMAP 保存位图信息的内存域的句柄
>  HBRUSH 画刷句柄
>  HCTR 子窗口控件句柄
>  HCURSOR 鼠标光标句柄
>  HDC 设备描述表句柄
>  HDLG 对话框句柄
>  HFONT 字体句柄
>  HICON 图标句柄
>  HINSTANCE 应用程序实例句柄
>  HMENU 菜单句柄
>  HMODULE 模块句柄
>  HPALETTE 颜色调色板句柄
>  HPEN 笔的句柄
>  HWND 窗口句柄

原文地址: https://blog.csdn.net/wumenglu1018/article/details/52832321

我刚学习Windows程序设计时，对那些句柄理解很含糊，尤其是HDC和HWND。用的很多，但其实还是不知道两者的真正区别，先来看一下其他博主的理解。

### hWnd(Handle of Window)

- h: 是类型描述，表示句柄；
- wnd: 是变量对象描述，表示窗口
- 窗口句柄: 其中包含窗口的属性。
- **例如**: 窗口的大小、显示位置、父窗口。

### hDC(Handle to Device Context)

- 是图像的设备描述表，窗口显示上下文句柄，其中可以进行图形显示。

利用**hDC=GetDC(hWnd)**，可以获得一个窗口的图形设备描述表。可以通过**ReleaseDC()**函数释放。

**hWnd**句柄是描述一个窗口的形状、位置、大小、是否显示、它的父窗口、兄弟窗口、等等的一组数据结构；
 **hDC**句柄是一个实实在在的用于具体表现这个窗口时，需要对这个窗口有个场合来实现的地方。

**hWnd**是窗体句柄；hDC是设备场景句柄。
 **hWnd**与窗口管理有关；**hDC**与绘图API（GDI函数）有关。
 **hWnd**是windows给窗口发送消息（事件）用的；**hDC**是把窗口绘制在屏幕上用的。
 有了**hWnd**，可以使用API的**GetDC()**函数得到与其相关的hDC：**hDC=GetDC(hWnd)**。

我们看到了哈，HWND和HDC都是句柄，不过前者是HWND是窗口句柄，HDC是设备描述表的句柄。（犯了错误，应该先把句柄是什么说清楚）那么句柄是什么呢？这个解释得很细，但可能不是很容易理解：http://blog.csdn.net/wenzhou1219/article/details/17659485

在Windows标编程设计中，使用了大量的句柄来标识对象。一个句柄是指使用的一个唯一的整数值，即一个4字节（64位程序中为8字节）长的数值，来标识应用程序中的不同对象和同类中的不同的实例，例如：一个窗口、按钮、图标、滚动条、输出设备、孔健、文件等。应用程序能通过句柄来访问相应的对象的信息。但是句柄不是指针，程序不能利用句柄来直接阅读文件中的信息。如果句柄不在I/O文件中，它是毫无用处的。我们来看看另一个好理解的说法：在进程的地址空间中设一张表，表里头专门保存一些编号和由这个编号对应一个地址，而由那个地址去引用实际的对象，这个编号跟那个地址在数值上没有任何规律性的联系，纯粹是个映射而已。在Windows系统中，这个编号就叫做"句柄"。

句柄实际上是一种指向某种资源的指针，但与指针又有所不同：HWND是跨进程可见的，而指针从来都是属于某个特定进程的。指针对应着一个数据在内存中的地址，得到了指针就可以自由地修改该数据。Windows并不希望一般程序修改其内部数据结构，因为这样太不安全。所以Windows给每个使用GlobalAlloc等函数声明的内存区域指定一个句柄(本质上仍是一个指针，但不要直接操作它)，平时我们只是在调用API函数时利用这个句柄来说明要操作哪段内存。

因为设备描述表中记录和某设备相关的各种信息，比如对于显示器来说，记录了显示器的尺寸、分辨率，还有当前选择的画笔、画刷、字体等GDI对象的信息。可以将HDC理解做一个设备的表面，比如显示器的声明、打印机的表面等等，我们可以使用这个HDC在这些表面上绘制图形——很多GDI绘图函数，都需要使用这个HDC作为参数的。

举例看一下他们分别用到什么地方了吧：



```cpp
 HWND hwnd;//窗口句柄
    char szAppName[] = "window1";

//创建窗口
    hwnd = CreateWindow(szAppName, //窗口类型名
            TEXT("The First Experiment"), //窗口实例的标题
            WS_OVERLAPPEDWINDOW, //窗口风格
            CW_USEDEFAULT, //窗口左上角位置坐标值x
            CW_USEDEFAULT, //窗口左上角位置坐标值y
            800, //窗口的宽度
            600, //窗口的高度
            NULL, //父窗口的句柄
            NULL, //主菜单的句柄
            hInstance, //应用程序实例句柄
            NULL );
　　　　//显示窗口
    ShowWindow(hwnd, iCmdShow);
    UpdateWindow(hwnd);
```



```cpp
    static int nWidth, nHeight;
    HDC hdc;//定义设备环境句柄   
    HBRUSH hB;//定义画笔句刷

case WM_LBUTTONDOWN://按下鼠标左键则用户区被刷成灰色
            nWidth = GetSystemMetrics(SM_CXFULLSCREEN);  //屏幕宽度    
            nHeight = GetSystemMetrics(SM_CYFULLSCREEN); //屏幕高度
            hdc=GetDC(hwnd);
            hB = (HBRUSH)GetStockObject(GRAY_BRUSH);//灰色画刷
            SelectObject(hdc, hB);
            Rectangle(hdc, 0, 0, nWidth, nHeight);//将用户区重新刷成灰色
            DeleteObject(hB);//删除画刷
            return 0;
```

