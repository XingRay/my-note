## 屏幕外渲染（使用FBO和RenderBuffer）和颜色、深度、模板的像素转移

https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth



我必须在OpenGL中进行屏幕外渲染，然后将图像传递给QImage。另外，为了锻炼，我想把深度和模板缓冲区也转移到CPU上。

对于在屏幕外绘制，我使用了帧缓冲区对象和渲染缓冲区（而不是纹理，因为我不需要纹理）。

像素传输操作与颜色缓冲区（实际的原始图像）工作...我看到了我所期望的。但是深度和模板不起作用...奇怪的图像为深度，没有为模板。

首先，简单的部分，我实际上画的是：

```
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();
    glTranslatef(-1.5f,0.0f,-6.0f);
    glBegin(GL_TRIANGLES);
    glColor3f(1.0f,0.0f,0.0f);
    glVertex3f( 0.0f, 1.0f, 0.0f);
    glColor3f(0.0f,1.0f,0.0f);
    glVertex3f(-1.0f,-1.0f, -1.0f);
    glColor3f(0.0f,0.0f,1.0f);
    glVertex3f( 1.0f,-1.0f, 0.0f);
    glEnd();
```

这里是FBO和3渲染缓冲区的初始化：

```
// frame buffer object
glGenFramebuffers(1, &fboId);
glBindFramebuffer(GL_FRAMEBUFFER, fboId);

// render buffer as color buffer
glGenRenderbuffers(1, &colorBuffer);
glBindRenderbuffer(GL_RENDERBUFFER, colorBuffer);
glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA, width, height);
glBindRenderbuffer(GL_RENDERBUFFER, 0);
// attach render buffer to the fbo as color buffer
glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorBuffer);

// render buffer as depth buffer
glGenRenderbuffers(1, &depthBuffer);
glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
glBindRenderbuffer(GL_RENDERBUFFER, 0);
// attach render buffer to the fbo as depth buffer
glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);

glGenRenderbuffers(1, &stencilBuffer);
glBindRenderbuffer(GL_RENDERBUFFER, stencilBuffer);
glRenderbufferStorage(GL_RENDERBUFFER, GL_STENCIL_INDEX, width, height);
glBindRenderbuffer(GL_RENDERBUFFER, 0);
// attach render buffer to the fbo as stencil buffer
glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_BUFFER, GL_RENDERBUFFER, stencilBuffer);
```

最后，这里有3种像素传输方法：

```
QImage FBO::getImage()
{
    // this is called Pixel Transfer operation: http://www.opengl.org/wiki/Pixel_Transfer
    uchar *pixels;
    pixels = new uchar[width * height * 4];
    for(int i=0; i < (width * height * 4) ; i++ ) {
        pixels[i] = 0;
    }

    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glReadPixels( 0,0,  width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;
}

QImage FBO::getDepth()
{
    // this is called Pixel Transfer operation: http://www.opengl.org/wiki/Pixel_Transfer
    uchar *pixels;
    pixels = new uchar[width * height * 4];
    for(int i=0; i < (width * height * 4) ; i++ ) {
        pixels[i] = 0;
    }

    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
    glReadPixels( 0,0,  width, height, GL_DEPTH_COMPONENT, GL_FLOAT, pixels);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;
}

QImage FBO::getStencil()
{
    // this is called Pixel Transfer operation: http://www.opengl.org/wiki/Pixel_Transfer
    uchar *pixels;
    pixels = new uchar[width * height * 4];
    for(int i=0; i < (width * height * 4) ; i++ ) {
        pixels[i] = 0;
    }

    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, stencilBuffer);
    glReadPixels( 0,0,  width, height, GL_STENCIL_INDEX, GL_FLOAT, pixels);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;

}
```

这里是2个屏幕截图（颜色和深度，用模具我得到一个空的图像）：

![img](D:\my-note\opengl\assets\Qv4nB.png)

![img](D:\my-note\opengl\assets\eJgGq.png)

颜色正是我正在绘制的（翻转，但我认为这是正常的）。深度...我期待一张带有渐变灰色三角形的白色图像...当然，我在图像格式上犯了一些错误（`GL_FLOAT`？），但我尝试了一些组合，这是最好的结果...紫色背景，里面有闪光的颜色...和模板缓冲区...我期待黑色背景中的白色三角形轮廓，但是...不知道为什么我什么也没看到...

好的，不要单独使用模具缓冲区，所以我已经重构了一点。

宣布FBO时：

```
// render buffer for both depth and stencil buffer
glGenRenderbuffers(1, &depthStencilBuffer);
glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
glBindRenderbuffer(GL_RENDERBUFFER, 0);
// attach render buffer to the fbo as depth buffer
glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilBuffer);
```

深度的像素转移：

```
QImage FBO::getDepth()
{
    std::vector<uchar> pixels;
    pixels.reserve(width * height*4);
    for(int i=0; i < (width * height*4) ; i++ ) {
        pixels.push_back(0);
    }

    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
    glReadPixels( 0,0,  width, height,  GL_DEPTH24_STENCIL8, GL_UNSIGNED_BYTE, pixels.data());

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    std::vector<uchar> _pixels;
    _pixels.reserve(width * height*4);
    for(int i=0; i < (width * height*4) ; i++ ) {
        uchar p_red = pixels[i];
        uchar p_green = pixels[i+1];
        uchar p_blue = pixels[i+2];

        uchar p_stencil = pixels[i+3];

        _pixels.push_back(p_red);
        _pixels.push_back(p_green);
        _pixels.push_back(p_blue);

        _pixels.push_back(255); // alpha
    }

    QImage qi = QImage(_pixels.data(), width, height, QImage::Format_ARGB32);
    //qi = qi.rgbSwapped();

    return qi;
}
```

模具类似，但使用带有rgb组件的`p_模具`

..结果图像是深度和模板的黑色图像

感谢Nicolas的回答，我设法为深度和模板缓冲区使用渲染缓冲区，并提取深度组件以适应`QImage::Format_ARGB32`与此代码：

```
QImage FBO1::getDepth()
{
    // sizeof( GLuint ) = 4 byte
    // sizeof( uchar ) = 1 byte

    std::vector<GLuint> pixels(width * height);
    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
    glReadPixels( 0,0,  width, height,  GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, pixels.data());
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    std::vector<uchar> _pixels;
    for(int i=0; i < (width * height) ; i++ ) {
        GLuint color = pixels[i];

        float temp = (color & 0xFFFFFF00) >> 8; // 24 bit of depth component

        // remap temp that goes from 0 to 0xFFFFFF (24 bits) to
        // _temp that goes from 0 to 0xFF (8 bits)
        float _temp = (temp / 0xFFFFFF) * 0xFF;

        _pixels.push_back((uchar)_temp);
        _pixels.push_back((uchar)_temp);
        _pixels.push_back((uchar)_temp);
        _pixels.push_back(255);
    }

    QImage qi = QImage(_pixels.data(), width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;
}
```

..模具组件仍然存在一些问题（以下代码不起作用：它会生成故障图像）：

```
QImage FBO1::getStencil()
{
    // sizeof( GLuint ) = 4 byte
    // sizeof( uchar ) = 1 byte

    std::vector<GLuint> pixels(width * height);
    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
    glReadPixels( 0,0,  width, height,  GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, pixels.data());
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    std::vector<uchar> _pixels;
    for(int i=0; i < (width * height); i++ ) {
        GLuint color = pixels[i];

        uchar temp = (color & 0x000000FF); // last 8 bit of depth component

        _pixels.push_back(temp);
        _pixels.push_back(temp);
        _pixels.push_back(temp);
        _pixels.push_back(255);
    }

    QImage qi = QImage(_pixels.data(), width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;
}
```

## 共2个答案

**匿名用户**





深度...我期待一个带有渐变灰色三角形的白色图像...

您的代码并没有这样建议。

```
uchar *pixels;
pixels = new uchar[width * height * 4];
```

这将创建一个整数数组。这也是内存泄漏；使用`std::向量`

```
glReadPixels( 0,0,  width, height, GL_DEPTH_COMPONENT, GL_FLOAT, pixels);
这会告诉OpenGL将浮点写入整数数组。因此，在编写时，OpenGL将像素视为GLfloats的数组。
QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
我假设这将解释为某种每分量8位的整数图像格式。所以你把floats解释为8位整数。我们没有理由期望这种行为是理性的。
您应该将深度缓冲区读取为浮点数并以更合理的方式将其转换为像素颜色，或者您应该将深度缓冲区读取为整数值，让OpenGL为您进行灰度转换。
此外，您应该始终使用组合的深度/模具图像，而不是两个单独的渲染缓冲区。
glReadPixels( 0,0,  width, height,  GL_DEPTH24_STENCIL8, GL_UNSIGNED_BYTE, pixels.data());
你似乎不明白像素传输是如何工作的。
首先，像素传输格式指定您正在读取的组件。它没有指定它们的大小。GL_DEPTH24_STENCIL8是一种图像格式，而不是像素传输格式。如果您想从图像中读取深度和模板，请使用GL_DEPTH_STENCIL。像素传输格式没有大小。
所以这个函数只是给你一个OpenGL错误。
大小来自第二个参数，像素传输类型。在这种情况下，GL_UNSIGNED_BYTE意味着它将读取深度和模板，将每个值转换为无符号的8位值，并将其中两个每个像素存储到pixels.data（）中。
深度缓冲区每像素仅存储1个值。仅限深度/模板存储2。你不能用OpenGL将它们复制成每像素4分量的格式。因此，无论以后如何构建QImage，它都必须为我提供一种每像素1或2个值的方法。
一般来说，如果您想要读取深度缓冲区，并且希望深度缓冲区的数据实际上是有意义的，您可以这样做：
std::vector<GLuint> pixels(width * height);  //std::vector value-initializes its elements.

glBindFramebuffer(GL_FRAMEBUFFER, fboId);
glReadPixels( 0,0,  width, height,  GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, pixels.data());
如何将它们放入QImage中进行可视化取决于您。但这会为您提供一个无符号整数数组，其中高24位是深度分量，低8位是模板。

```

``

```
匿名用户深度图似乎可以从以下代码中看出： glReadPixels( 0,0,  width, height, GL_DEPTH_COMPONENT, GL_FLOAT, pixels);

 glBindFramebuffer(GL_FRAMEBUFFER, 0);

 QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
QImage不知道如何处理浮点数，因此它将每组32位（在一个浮点数内）解释为ARGB组件，每组8位。浮点在低位有一个25位尾数，它很好地映射到这些组件中。你看到的这些带只是不断增加的尾数，被裁剪成一些位数。QImage真的是一个非常有限的东西（它甚至不能处理HDR配置，比如每个通道16位，这有点令人沮丧）。无论如何，您最好的选择是将此浮点数转换为范围0...255并将其作为灰度图像传递给QImage。此外，单独的深度模板也有助于提高性能。尽可能使用组合格式。尼科尔·博拉斯的答案和我写的一样。此外，他还指出了内存泄漏。
```





https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth



# [Render off screen (with FBO and RenderBuffer) and pixel transfer of color, depth, stencil](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth)

[Ask Question](https://stackoverflow.com/questions/ask)

Asked 10 years, 7 months ago

Modified [10 years, 6 months ago](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth?lastactivity)

Viewed 6k times



2



I'have to render off screen in OpenGL and then pass the image to a QImage. Plus, just for exercise I'd like to transfer to the CPU also the depth and the stencil buffer.

For drawing offscreen I've used Frame Buffer Object with Render Buffer (and not with texture because I don't need the texture).

Pixel transfer operation with color buffers (the actual raw image) works.. I see what I'm expecting. But depth and stencil are not working.. strange image for depth and nothing with for the stencil.

First, the easy part, what I'm actually drawing:

```cpp
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity();
    glTranslatef(-1.5f,0.0f,-6.0f);
    glBegin(GL_TRIANGLES);
    glColor3f(1.0f,0.0f,0.0f);
    glVertex3f( 0.0f, 1.0f, 0.0f);
    glColor3f(0.0f,1.0f,0.0f);
    glVertex3f(-1.0f,-1.0f, -1.0f);
    glColor3f(0.0f,0.0f,1.0f);
    glVertex3f( 1.0f,-1.0f, 0.0f);
    glEnd();
```

Here the initalization of the FBO and of the 3 render buffer:

```cpp
// frame buffer object
glGenFramebuffers(1, &fboId);
glBindFramebuffer(GL_FRAMEBUFFER, fboId);

// render buffer as color buffer
glGenRenderbuffers(1, &colorBuffer);
glBindRenderbuffer(GL_RENDERBUFFER, colorBuffer);
glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA, width, height);
glBindRenderbuffer(GL_RENDERBUFFER, 0);
// attach render buffer to the fbo as color buffer
glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, colorBuffer);

// render buffer as depth buffer
glGenRenderbuffers(1, &depthBuffer);
glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width, height);
glBindRenderbuffer(GL_RENDERBUFFER, 0);
// attach render buffer to the fbo as depth buffer
glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);

glGenRenderbuffers(1, &stencilBuffer);
glBindRenderbuffer(GL_RENDERBUFFER, stencilBuffer);
glRenderbufferStorage(GL_RENDERBUFFER, GL_STENCIL_INDEX, width, height);
glBindRenderbuffer(GL_RENDERBUFFER, 0);
// attach render buffer to the fbo as stencil buffer
glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_STENCIL_BUFFER, GL_RENDERBUFFER, stencilBuffer);
```

And finally here 3 methods for the pixel transfer:

```cpp
QImage FBO::getImage()
{
    // this is called Pixel Transfer operation: http://www.opengl.org/wiki/Pixel_Transfer
    uchar *pixels;
    pixels = new uchar[width * height * 4];
    for(int i=0; i < (width * height * 4) ; i++ ) {
        pixels[i] = 0;
    }

    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glReadPixels( 0,0,  width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;
}

QImage FBO::getDepth()
{
    // this is called Pixel Transfer operation: http://www.opengl.org/wiki/Pixel_Transfer
    uchar *pixels;
    pixels = new uchar[width * height * 4];
    for(int i=0; i < (width * height * 4) ; i++ ) {
        pixels[i] = 0;
    }

    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
    glReadPixels( 0,0,  width, height, GL_DEPTH_COMPONENT, GL_FLOAT, pixels);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;
}

QImage FBO::getStencil()
{
    // this is called Pixel Transfer operation: http://www.opengl.org/wiki/Pixel_Transfer
    uchar *pixels;
    pixels = new uchar[width * height * 4];
    for(int i=0; i < (width * height * 4) ; i++ ) {
        pixels[i] = 0;
    }

    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, stencilBuffer);
    glReadPixels( 0,0,  width, height, GL_STENCIL_INDEX, GL_FLOAT, pixels);

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;

}
```

Here the 2 screenshot (color and depth, with stencil I get an empty QImage):

![enter image description here](D:\my-note\opengl\assets\Qv4nB-1707715968125-7.png)

![enter image description here](D:\my-note\opengl\assets\eJgGq-1707715968125-8.png)

The color one is exactly what I'm drawing (flipped but it is normal I think). The depth.. I'm expecting a white image with a gradient gray triangle.. For sure I'm making some mistake in the format of the image (`GL_FLOAT`?) but I've tried some combinations and this is the best result.. a violet background with a glitchy colors inside it.. and with the stencil buffer.. i'm expecting a white triangle outline in a black background but.. no idea why I don't see anything..

## EDIT:

Ok, never use stencil buffer alone, so I've refactored a little bit.

when declaring the FBO:

```cpp
// render buffer for both depth and stencil buffer
glGenRenderbuffers(1, &depthStencilBuffer);
glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, width, height);
glBindRenderbuffer(GL_RENDERBUFFER, 0);
// attach render buffer to the fbo as depth buffer
glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, depthStencilBuffer);
```

pixel transfer of the depth:

```cpp
QImage FBO::getDepth()
{
    std::vector<uchar> pixels;
    pixels.reserve(width * height*4);
    for(int i=0; i < (width * height*4) ; i++ ) {
        pixels.push_back(0);
    }

    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
    glReadPixels( 0,0,  width, height,  GL_DEPTH24_STENCIL8, GL_UNSIGNED_BYTE, pixels.data());

    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    std::vector<uchar> _pixels;
    _pixels.reserve(width * height*4);
    for(int i=0; i < (width * height*4) ; i++ ) {
        uchar p_red = pixels[i];
        uchar p_green = pixels[i+1];
        uchar p_blue = pixels[i+2];

        uchar p_stencil = pixels[i+3];

        _pixels.push_back(p_red);
        _pixels.push_back(p_green);
        _pixels.push_back(p_blue);

        _pixels.push_back(255); // alpha
    }

    QImage qi = QImage(_pixels.data(), width, height, QImage::Format_ARGB32);
    //qi = qi.rgbSwapped();

    return qi;
}
```

the stencil is similar but using `p_stencil` with rgb component

.. the result image is a black image for both depth and stencil

## EDIT

thanks to Nicolas answer I managed to use a renderbuffer for both depth and stencil buffer and extract the depth component to fit a `QImage::Format_ARGB32` with this code:

```cpp
QImage FBO1::getDepth()
{
    // sizeof( GLuint ) = 4 byte
    // sizeof( uchar ) = 1 byte

    std::vector<GLuint> pixels(width * height);
    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
    glReadPixels( 0,0,  width, height,  GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, pixels.data());
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    std::vector<uchar> _pixels;
    for(int i=0; i < (width * height) ; i++ ) {
        GLuint color = pixels[i];

        float temp = (color & 0xFFFFFF00) >> 8; // 24 bit of depth component

        // remap temp that goes from 0 to 0xFFFFFF (24 bits) to
        // _temp that goes from 0 to 0xFF (8 bits)
        float _temp = (temp / 0xFFFFFF) * 0xFF;

        _pixels.push_back((uchar)_temp);
        _pixels.push_back((uchar)_temp);
        _pixels.push_back((uchar)_temp);
        _pixels.push_back(255);
    }

    QImage qi = QImage(_pixels.data(), width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;
}
```

.. Still some problems with stencil component (the following code does not work: it produces glitches image):

```cpp
QImage FBO1::getStencil()
{
    // sizeof( GLuint ) = 4 byte
    // sizeof( uchar ) = 1 byte

    std::vector<GLuint> pixels(width * height);
    glBindFramebuffer(GL_FRAMEBUFFER, fboId);
    glBindRenderbuffer(GL_RENDERBUFFER, depthStencilBuffer);
    glReadPixels( 0,0,  width, height,  GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, pixels.data());
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    std::vector<uchar> _pixels;
    for(int i=0; i < (width * height); i++ ) {
        GLuint color = pixels[i];

        uchar temp = (color & 0x000000FF); // last 8 bit of depth component

        _pixels.push_back(temp);
        _pixels.push_back(temp);
        _pixels.push_back(temp);
        _pixels.push_back(255);
    }

    QImage qi = QImage(_pixels.data(), width, height, QImage::Format_ARGB32);
    qi = qi.rgbSwapped();

    return qi;
}
```

- [c++](https://stackoverflow.com/questions/tagged/c%2b%2b)
- [opengl](https://stackoverflow.com/questions/tagged/opengl)
- [fbo](https://stackoverflow.com/questions/tagged/fbo)
- [depth-buffer](https://stackoverflow.com/questions/tagged/depth-buffer)
- [stencil-buffer](https://stackoverflow.com/questions/tagged/stencil-buffer)

[Share](https://stackoverflow.com/q/17711673/8273792)

Edit

Follow

[edited Jun 20, 2020 at 9:12](https://stackoverflow.com/posts/17711673/revisions)

![Community's user avatar](D:\my-note\opengl\assets\a007be5a61f6aa8f3e85ae2fc18dd66e.png)

[Community](https://stackoverflow.com/users/-1/community)Bot

**1**11 silver badge

asked Jul 17, 2013 at 22:54

![nkint's user avatar](D:\my-note\opengl\assets\1d20b1339a3743da887052b2c6099c0f.png)

[nkint](https://stackoverflow.com/users/433685/nkint)

**11.6k**3131 gold badges104104 silver badges176176 bronze badges

[Add a comment](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#)



## 2 Answers

Sorted by:

​                              Highest score (default)                                            Trending (recent votes count more)                                            Date modified (newest first)                                            Date created (oldest first)                    





3







> The depth.. I'm expecting a white image with a gradient gray triangle..

Your code doesn't suggest that.

```cpp
uchar *pixels;
pixels = new uchar[width * height * 4];
```

This creates an array of *integers*. It's also a memory leak; use `std::vector<uchar>` instead.

```cpp
glReadPixels( 0,0,  width, height, GL_DEPTH_COMPONENT, GL_FLOAT, pixels);
```

This tells OpenGL to write *floats* to your array of *integers*. So OpenGL will treat `pixels` as an array of `GLfloat`s when writing.

```cpp
QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
```

I'll assume that this is going to interpret this as some kind of 8-bit-per-component *integer* image format. So you're interpreting `float`s as 8-bit integers. There's no reason to expect this to behave rationally.

You should either be reading the depth buffer as floats and converting that to your pixel colors in a more reasonable way, or you should be reading the depth buffer as an integer value, letting OpenGL do the greyscale conversion for you.

Also, you should *always* be using a [combined depth/stencil image](http://www.opengl.org/wiki/Image_Format#Depth_stencil_formats), not two separate renderbuffers.

------

```cpp
glReadPixels( 0,0,  width, height,  GL_DEPTH24_STENCIL8, GL_UNSIGNED_BYTE, pixels.data());
```

You don't seem to understand how [pixel transfers](http://www.opengl.org/wiki/Pixel_Transfer) work.

First, the [pixel transfer format](http://www.opengl.org/wiki/Pixel_Transfer#Pixel_format) specifies which components that you're reading. It does *not* specify their sizes. `GL_DEPTH24_STENCIL8` is [an *image format*](http://www.opengl.org/wiki/Image_Formats), not a pixel transfer format. If you want to read the depth and stencil from an image, you use `GL_DEPTH_STENCIL`. Pixel transfer formats don't have sizes.

So this function is just giving you an OpenGL error.

The size comes from the second parameter, the [pixel transfer type](http://www.opengl.org/wiki/Pixel_Transfer#Pixel_type). In this case `GL_UNSIGNED_BYTE` means that it will read the depth and stencil, convert each into an unsigned, 8-bit value, and store two of those per-pixel into `pixels.data()`.

Depth buffers only store 1 value per pixel. Depth/stencil only store 2. You *cannot* copy them into a 4-component-per-pixel format with OpenGL. Therefore, however you build your QImage later, it *must* me some method that takes 1 or 2 values per pixel.

Generally speaking, if you want to read the depth buffer, and you want the depth buffer's data to actually be meaningful, you do this:

```cpp
std::vector<GLuint> pixels(width * height);  //std::vector value-initializes its elements.

glBindFramebuffer(GL_FRAMEBUFFER, fboId);
glReadPixels( 0,0,  width, height,  GL_DEPTH_STENCIL, GL_UNSIGNED_INT_24_8, pixels.data());
```

How you put those in a QImage for visualization is up to you. But this gets you an array of unsigned ints, where the high 24 bits are the depth component, and the low 8 bits are the stencil.



[Share](https://stackoverflow.com/a/17711994/8273792)

Edit

Follow

[edited Jul 18, 2013 at 18:35](https://stackoverflow.com/posts/17711994/revisions)

answered Jul 17, 2013 at 23:27

![Nicol Bolas's user avatar](D:\my-note\opengl\assets\a356923f858fbe363dad1dc566837fc6.png)

[Nicol Bolas](https://stackoverflow.com/users/734069/nicol-bolas)

**459k**6363 gold badges795795 silver badges10021002 bronze badges

- ok thanks, depth buffer was ok, but then i've refactored for have depth AND stencil in the same buffer and.. all black. see the edit 

  – [nkint](https://stackoverflow.com/users/433685/nkint)

   [Jul 18, 2013 at 10:57](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#comment25830009_17711994)

- woa, this is much more deeper then I usually goes in my computer. thanks for the trip! 

  – [nkint](https://stackoverflow.com/users/433685/nkint)

   [Jul 19, 2013 at 9:28](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#comment25867134_17711994)

- anyway, I can correctly get the depth extracting with bit-shift operation the first 24 bit and remapping them into a QImage ARGB32 but I am still getting glitches with the stencil. do I have to enable it with something more then `glEnable(GL_STENCIL_TEST);` ? 

  – [nkint](https://stackoverflow.com/users/433685/nkint)

   [Jul 19, 2013 at 9:31](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#comment25867231_17711994)

[Add a comment](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#)





2



Well, the depth picture looks like one can excpect from this code:

```cpp
 glReadPixels( 0,0,  width, height, GL_DEPTH_COMPONENT, GL_FLOAT, pixels);

 glBindFramebuffer(GL_FRAMEBUFFER, 0);

 QImage qi = QImage(pixels, width, height, QImage::Format_ARGB32);
```

QImage doesn't know how to deal with floats, so it interprets each every group of 32 bits (within a float) as if they were ARGB components, 8 bits each. A float has a 25 bits mantissa in the lower bits, which nicely maps into those components. Those bands you see is simply the increasing mantissa, cropped to some number of bits.

QImage is really a very limited thing (it can't even deal with HDR configurations, like 16 bits per channel, which is kind of frustrating). Anyway, your best bet is to convert this floats into the range 0…255 and pass that as a grayscale image to QImage.

Also separate depth stencil is ditremental for performance. Always use a combined format, where possible.

… well, there comes Nicol Bolas' answer, which is right the same as I wrote. Plus he pointed out the memory leak.



[Share](https://stackoverflow.com/a/17712055/8273792)

Edit

Follow

answered Jul 17, 2013 at 23:33

![datenwolf's user avatar](D:\my-note\opengl\assets\oZ8Up.png)

[datenwolf](https://stackoverflow.com/users/524368/datenwolf)

**160k**1313 gold badges187187 silver badges302302 bronze badges

- ok thanks, depth buffer was ok, but then i've refactored for have depth AND stencil in the same buffer and.. all black. see the edit 

  – [nkint](https://stackoverflow.com/users/433685/nkint)

   [Jul 18, 2013 at 10:56](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#comment25829964_17712055)

- @nkint I'm not sure using a combined attachment is such a good idea, since Qt cannot make something meaningful of that either, I guess. And `glReadPixels` doesn't work with a combined format either. That's why it's all black. The good reason the stencil attachment didn't work at all might be your use of `GL_STENCIL_BUFFER` instead of `GL_STENCIL_ATTACHMENT` in the first example (if that wasn't a typo). 

  – [Christian Rau](https://stackoverflow.com/users/743214/christian-rau)

   [Jul 18, 2013 at 11:16](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#comment25830609_17712055) 

- @nkint Ok, `glReadPixels` *does* work with a combined format, but only with a limited range of types (what you probably want is `GL_UNSIGNED_INT_24_8`) and not with a sized internal format (use plain `GL_DEPTH_STENCIL` instead). 

  – [Christian Rau](https://stackoverflow.com/users/743214/christian-rau)

   [Jul 18, 2013 at 11:23](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#comment25830814_17712055) 

- ohu.. you are right.. if I use `GL_STENCIL_ATTACHMENT` with a render buffer for stencil only I get `GL_FRAMEBUFFER_UNSUPPORTED` 

  – [nkint](https://stackoverflow.com/users/433685/nkint)

   [Jul 18, 2013 at 11:27](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#comment25830952_17712055)

[Add a comment](https://stackoverflow.com/questions/17711673/render-off-screen-with-fbo-and-renderbuffer-and-pixel-transfer-of-color-depth#)



Your Answer