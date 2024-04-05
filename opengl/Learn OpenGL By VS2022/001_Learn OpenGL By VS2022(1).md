# Learn OpenGL By VS2022(1)

https://space.bilibili.com/354822098/article



2023年09月06日 20:3977浏览 · 1喜欢 · 0评论

![img](./assets/d6061cb52e498316b2f09502d31da8db4aa7fe2f.jpg@96w_96h_1c_1s_!web-avatar.avif)

[齿轮c](https://space.bilibili.com/354822098)

粉丝：3文章：12

关注

**序言**

开设这个专栏是为了可以督促自己坚持学习完TheCherno老哥的OpenGL系列，以及交流下自己学习OpenGL的感悟。本专栏中的操作不会完全照搬TheCherno老哥的操作，介意这点的朋友请见谅。如果这篇专栏帮助到了各位学习OpenGL的朋友，那我倍感荣幸。



![img](./assets/4adb9255ada5b97061e610b682b8636764fe50ed.png@progressive.webp)

**
**

**一、配置OpenGL环境**

从官网得到glfw和glew的压缩包

glfw官网：https://www.glfw.org/

glew官网：https://glew.sourceforge.net/

![img](./assets/b0dfd71a7f9b6eba7c434d553deb41a92d218636.png@1256w_176h_!web-article-pic.avif)图1.1 glew和glfw压缩包

复制glfw官网Documentation板块的代码，代码如下：

```cpp
#include <GLFW/glfw3.h>

int main(void)
{
    GLFWwindow* window;

    /* Initialize the library */
    if (!glfwInit())
        return -1;

    /* Create a windowed mode window and its OpenGL context */
    window = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);
    if (!window)
    {
        glfwTerminate();
        return -1;
    }

    /* Make the window's context current */
    glfwMakeContextCurrent(window);

    /* Loop until the user closes the window */
    while (!glfwWindowShouldClose(window))
    {
        /* Render here */
        glClear(GL_COLOR_BUFFER_BIT);

        /* Swap front and back buffers */
        glfwSwapBuffers(window);

        /* Poll for and process events */
        glfwPollEvents();
    }

    glfwTerminate();
    return 0;
}
```

在VS2022创建一个C++空项目

![img](./assets/f5ad6502dd74925de979fca36d554b8adf049163.png@1256w_266h_!web-article-pic.avif)图1.2 C++空项目

在源项目文件夹处选择添加新建项、选择C++文件（其实在哪添加都无所谓啦反正解决方案下的四个文件夹都是虚拟的，只是为了方便管理项目文件才有四个文件夹之分）。将glfw官网复制的代码复制到cpp文件中，

![img](./assets/b23dfc2148884b4370c3e621a3368133e1ed3211.png@1256w_652h_!web-article-pic.avif)图1.3 创建、粘贴代码后的界面

接着就可以看到大量报错，不要慌！不要慌！报错是因为我们还没有配置编译、链接环境，接下来让我们配置环境。虽然TheCherno配置的是x86环境，但本人更喜欢x64环境，所以以下配置皆为x64环境。需要注意的是，x86和x64指的不是计算机是x86还是x64的，而是指你希望这段代码在什么环境下跑起来，在VS2022环境中是可以切换x86编译模式和x64编译模式的。

打开VS2022用于保存项目的文件夹，找到刚才你创建的项目并打开它，在.sln文件的同级目录下创建文件夹“Append”并在“Append”文件夹中分别创建文件夹“GLFW”和“GLEW”为了方便描述、便于区分，我将在下文中用“File1”和“File2”对这两个文件夹进行指代。

解压“glfw-3.3.8.bin.WIN64.zip”和“glew-2.1.0-win32.zip”得到“glfw-3.3.8.bin.WIN64”和“glew-2.1.0-win32”将“glfw-3.3.8.bin.WIN64\include”和“glfw-3.3.8.bin.WIN64\lib-vc2022”放入“File1”中。

![img](./assets/7040e06cbbd162ea2fca6bb0448614fe5dd028fe.png@1256w_368h_!web-article-pic.avif)图1.4 GLFW文件夹

将“glew-2.1.0-win32\glew-2.1.0\include”和“D:\VisualStudioProject\aaOpenGL\glew-2.1.0-win32\glew-2.1.0\lib\Release\x64\glew32s.lib”放入“File2”中。

![img](./assets/b1f05989385eb4a6ec7ad32fe26d748bf1c8888b.png@1256w_284h_!web-article-pic.avif)图1.5 GLEW文件夹

接着重新打开刚才VS2022的项目，右击项目名，点击“属性”。

![img](./assets/af8d17a64ed59a4509bed33937c2ee52e6163232.png@!web-article-pic.avif)图1.6 比如我的项目名为OpenGL

首先更改最上面的“配置”和“平台”，分别更改为“所有配置”和“活动（x64）”。接着点击“C/C++”的“常规”，点击附加包含目录右边的下三角，点击编辑双击下图蓝色区域添加路径。

![img](./assets/a1e8b415345b9555a2398fb0d6d9d54d97069938.png@!web-article-pic.avif)图1.7 添加路径方法

TheCherno这里是利用solutiondir这一相对路径进行配置的，但我们可以使用另一个方法，点击输入行右侧的省略号就可以直接选择路径了，超级方便！像下图这样添加路径，注意只要“OpenGL”字样后面的路径一致就可以了。接着点击“确定”，点击右下角的“应用”。



![img](./assets/601ff08107417d1061e0f20408388a088d6a4e8b.png@!web-article-pic.avif)图1.8 添加附加包路径

现在你会发现你的代码不再直接标红报错了，这时按住“Ctrl+F7”进行生成，可以看到生成成功。

![img](./assets/0e5234d348191ca3bdaf33b59666d253c5aef521.png@!web-article-pic.avif)图1.9 代码生成成功

接着依旧打开刚才的“属性”点击“链接器”的“常规”点击“附加库目录”的下三角，点击编辑，像刚才那样添加如下两个地址。依然是只要保证“OpenGL”字样后面的路径一致即可

![img](./assets/47da206d9555e7587ad90a3c51ffd00fad643449.png@!web-article-pic.avif)图1.10 添加链接器路径

接着点击“确定”，点击右下角的“应用”。再点击“链接器”的“输入”在“附加依赖项”中添加下面三个依赖项，注意依赖项之间用分号间隔。接着点击“确定”，点击右下角的“应用”。

![img](./assets/3b4edd50fe81c055ed86953b9564bde4147a7567.png@1256w_272h_!web-article-pic.avif)图1.11 添加附加依赖项

现在按“F5”就可以正常编译和链接了，程序运行结果如下。顺便提一嘴，要是朋友们细心一点的话就可以发现新添加的附加依赖项名字就是“File1”和“File2”中一些我们需要的lib库的名称。

![img](./assets/533d2eb4d6162c4734487046ea1f596bbef91d28.png@1256w_704h_!web-article-pic.avif)图1.12 运行结果

漂亮，到这里你距离完全配置完成已经仅剩一点操作了。在代码最开始加入以下语句“#include<GL/glew.h>”，必须注意的是，这段语句必须写在“#include<GLFW/glfw3.h>”之前。接着在“glfwInit()”的代码段之后写上“glewInit();”。

![img](./assets/b1b72b235a80cd9998b9ddd7a10971981d611ad3.png@!web-article-pic.avif)图1.13 添加代码

接着再次按“Ctrl+F7”生成代码，可以看到代码生成成功，但是直接编译会报错。我们右击“glewInit()”点击“转到声明”，在这个文件中我们可以看到下图的代码。

![img](./assets/10e067f3cc07b8444c59ddeb48c9efa434643abe.png@!web-article-pic.avif)图1.14 glewInit()声明所在文件的部分代码

根据这一段的描述我们可以知道如果我们什么也不做，那么glew会默认使用动态库。但是我们希望它调用静态库，这要怎么做呢？还是这段代码告诉我们，只要宏定义一个“GLEW_STATIC”就可以使用静态库了。所以我们再次打开“属性”界面，在“C/C++”的“预处理器”的“预处理器定义”中添加“GLEW_STATIC”。点击右下角的应用，再点击确定。

![img](./assets/6bdf19452edffcfa3fdc0124b6aea68dcd6410e5.png@1256w_210h_!web-article-pic.avif)图1.15 预处理器定义添加GLEW_STATIC

现在，再次按“F5”运行，可以看到运行成功，至此环境完全配置成功！可喜可贺，可喜可贺。



![img](./assets/4adb9255ada5b97061e610b682b8636764fe50ed.png@progressive.webp)



**二、用传统OpenGL画点小玩意**

需要注意的是，由于是使用传统OpenGL，所以不需要glew，glew也不会对下面的代码产生影响。首先在“while (!glfwWindowShouldClose(window))”代码块中添加如下代码：

```cpp
glBegin(GL_TRIANGLES);
glVertex2f(-0.5f, -0.5f);
glVertex2f( 0.0f,  0.5f);
glVertex2f( 0.5f, -0.5f);
glEnd();
```

![img](./assets/2b238a54ac196a347497ca9bdc84b176b2fa1191.png@!web-article-pic.avif)图2.1 添加后的while代码块

现在再运行代码就可以看到屏幕上出现了一个白色的三角形。这里必须提及一点，glBegin()语句是很老之前的OpenGL语句了（感谢OpenGL新版本依旧支持glBegin()语句），那时候程序必须现在CPU后在GPU运行，而现在可以直接将程序放在GPU中运行。

![img](./assets/aedb74f2a2c6c22117b31feb4a095b902bb68431.png@1256w_1016h_!web-article-pic.avif)图2.2 运行结果

![img](./assets/4adb9255ada5b97061e610b682b8636764fe50ed.png@progressive.webp)

**
**

**三、对本专栏用到的所有代码的详细解释**

为了方便大家（更是方便我自己），这里给出本专栏所用一些变量、函数的解释：

- GLFWwindow* window

  GLFWwindow*用于声明一个窗口对象指针，窗口对象封装了窗口和上下文，这里不得不说明一些概念。

  句柄：用于标识和引用窗口的变量或对象，可以理解为窗口的一个标号。

  窗口：一个可视化区域，用于显示和交互应用程序的内容。

  上下文：包含了当前OpenGL状态、着色器程序、纹理和缓冲对象等。它通常与窗口关联，并用于进行OpenGL渲染。可以理解为一种存储了很多与当前绑定对象有关信息的数据结构。一个线程中只能有一个当前的上下文，即主上下文，主上下文是OpenGL中唯一直接与OpenGL进行交互的上下文。

- window = glfwCreateWindow(640, 480, "Hello World", NULL, NULL);

  glfwCreateWindow(width, height, title, monitor, share)用于创建窗口，width指定宽度，height指定高度，这两者都是以像素为单位。title指定窗口标题，要求传入的是string类型。monitor是否在特定显示器上启用全屏模式，NULL表示不使用全屏模式，若要使用全屏模式需要传入一个GLFWmonitor*类型指定显示器对象。share表示是否与其他窗口共享资源的上下文，NULL表示不共享，可以传入一个GLFWwindow*类型变量来和指定窗口共享上下文。请注意GLFWwindow和GLFWmonitor是不同的！！！

- if (!glfwInit())

  glfInit()初始化glfw库，如果初始化失败则会返回False，成功则返回True

- glfwTerminate();

  glfwTerminate()可以终止程序。

- glewInit();

  glewInit()初始化glew库，如果初始化失败返回False，成功则返回True

- glfwMakeContextCurrent(window);

  glfwMakeContextCurrent(window)用于将指定的OpenGL上下文即指定GLFWwindow对应的上下文设置为当前线程的活动上下文（）。window必须是GLFWwindow*类型。这里不得不再提一嘴：活动上下文和主上下文之间的联系是，活动上下文可以与主上下文共享OpenGL状态和资源。这意味着在活动上下文中创建的纹理、缓冲区等资源可以在主上下文中使用，并且对这些资源的更改也会在主上下文中反映出来。这种共享状态和资源的机制使得主上下文和活动上下文能够协同工作，共同完成复杂的渲染任务。需要注意的是，每个线程只能有一个活动上下文，而主上下文则是整个应用程序的主要上下文。活动上下文是在主上下文中创建的，并且与主上下文共享状态和资源，但活动上下文在后台运行，可以在自己的线程中独立执行渲染任务。

- while (!glfwWindowShouldClose(window))

  glfwWindowShouldClose(window)用于判断用户是否关闭指定窗口。如果用户准备关闭参数window所指定的窗口，那么此接口将会返回GL_TRUE，否则将会返回GL_FALSE。其中window必须为GLFWwindow*类型

- glClear(GL_COLOR_BUFFER_BIT);

  glClear(|)函数用于将当前选定的缓冲区的内容清除为预定义的值。它可以清除的缓冲区包括颜色缓冲区、深度缓冲区和模板缓冲区。且可选参数可以用位或运算符(|)进行组合。这些参数包括了：

  GL_COLOR_BUFFER_BIT: 清除颜色缓冲区，将颜色设置为预定义的值，默认值为(0, 0, 0, 0)即黑色。

  GL_DEPTH_BUFFER_BIT: 清除深度缓冲区，将深度设置为预定义的值，默认值为1.0

  GL_STENCIL_BUFFER_BIT: 清除模板缓冲区，将模板值设置为预定义的值，默认值为0。

- glBegin(GL_TRIANGLES);

  glBegin(choose)函数用于指示开始一组定义几何图元的绘制操作。它将设置OpenGL的渲染状态以开始绘制一些图元，如点、线、三角形等。choose的可选参数为

  GL_POINTS: 指定绘制点的模式。

  GL_LINES: 指定绘制线段的模式。

  GL_LINE_STRIP: 指定绘制连续线段的模式，除了第一个和最后一个顶点之间的线段外，从第二个顶点开始的每两个顶点之间都会绘制一条线段。

  GL_LINE_LOOP: 指定绘制闭合线段的模式，从第一个顶点开始的每两个顶点之间都会绘制一条线段，并且最后一个顶点与第一个顶点之间也会绘制一条线段。

  GL_TRIANGLES: 指定绘制三角形的模式。

  GL_TRIANGLE_STRIP: 指定绘制连续三角形的模式，除了第一个三角形外，从第二个顶点开始的每三个顶点之间都会绘制一个三角形。

  GL_TRIANGLE_FAN: 指定绘制以一个公共顶点为中心的连续三角形的模式，从第二个顶点开始的每两个顶点之间都会绘制一个三角形，而公共顶点与前一个顶点和最后一个顶点之间会绘制一个三角形。

  注意该函数在旧版OpenGL中意义重大，但新版OpenGL中此函数的存在感基本为0。

- glVertex2f(-0.5f, -0.5f);

  glVertex2f(x, y)函数用于指定绘制操作中的一个顶点的坐标。它将在glBegin()和glEnd()之间的绘制操作中多次调用，以指定绘制图元的各个顶点的位置。x和y类型应为float。

- glEnd();

  glEnd()函数的作用是告诉OpenGL一组顶点的绘制操作已经完成，并且可以开始执行绘制图元的操作。在调用glEnd函数之前，必须先调用glBegin函数开始一个绘制操作，并且在这个绘制操作期间调用一系列的glVertex函数来指定顶点。这个操作是在后缓冲区完成而非前缓冲区。

- glfwSwapBuffers(window);

  glfwSwapBuffers(window)函数的作用是交换指定窗口的前后缓冲区，以更新窗口显示的内容，执行函数后会更新窗口的显示。window应当为GLFWwindow*类型。在OpenGL中，绘制操作首先会将图形数据绘制在后缓冲区中，而窗口实际显示的是前缓冲区的内容。当完成一帧的绘制操作后，使用glfwSwapBuffers函数可以将后缓冲区的内容与前缓冲区进行交换。但是该函数执行后窗口不会立刻更新，必须和glfwPollEvents()函数配合使用后窗口才能更新。

- glfwPollEvents();

  glfwPollEvents()函数的作用是处理并接收窗口事件，例如鼠标移动、键盘输入等。通常，该函数需要在每一帧渲染循环中被调用，以便及时地接收并处理用户的输入。

  