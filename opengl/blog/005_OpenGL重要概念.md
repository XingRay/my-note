# OpenGL重要概念

OpenGL下文简称GL

## 1，OpenGL Context

OpenGL规格书定义了一个重要的概念Context，Context包含了所有被openGL系统用作渲染的所有信息，当系统得到一个渲染指令之后，对应的Context就会发生变化。openGL就是一个有效的Context,如果没有Context就没有OpenGL。

OpenGL context 由特定的应用决定，意味着你不能查看其他应用的context。但是，每个应用都可以拥有多个context。每个context是完全独立的分离的。但是有一个例外，Objects（见下文）可以在不同的contexts中相互分享，如果context在装载创建的过程中有初始化的话。

所有的OpenGL语句将会操作在名为"current"的context上，这个非常像一个[全局变量](https://zhida.zhihu.com/search?content_id=219107787&content_type=Article&match_order=1&q=全局变量&zhida_source=entity)，所有的操作都作用在这个全局变量上。

虽然OpenGL的规格书详细的记录了contexts的行为，但是并没有包含如何创建他们。如何创建取决于平台相关的API，因此，虽然说OpenGL的代码是平台无关的，但是还是依赖平台指定的代码来对context进行创建和销毁。(wglMakeCurrent,wglCreateContext)

## 2, State

GL的context包含了rendering system的重要信息，这些信息被称为 State，这些更加抽象的会被理解为是一个GL "State machine"[状态机](https://zhida.zhihu.com/search?content_id=219107787&content_type=Article&match_order=1&q=状态机&zhida_source=entity)，其实也就是储存在context中的一系列value而已。

每一个在GL context中的独立的状态都有一个独一无二的[标识符](https://zhida.zhihu.com/search?content_id=219107787&content_type=Article&match_order=1&q=标识符&zhida_source=entity)，这些在规格书里都严格限定了。不是所有的标识符都是[枚举类型](https://zhida.zhihu.com/search?content_id=219107787&content_type=Article&match_order=1&q=枚举类型&zhida_source=entity)，它也可以是一个数组。

当context被创建后，每个state都会被初始化为一个合理的值。一般会有一个state table去初始化每个state。

GL的函数可以被分为三个大类

1， 设置state到context的函数

2， 从context中查询state的函数

3，渲染给到context中的state的函数

## 3, Command and Synchronization

OpenGL规范通常定义了命令的行为，这样它们将按照给定的顺序执行，并且每个后续命令的行为必须与所有之前的命令都已完成且其内容可见一样。然而，重要的是要记住这是OpenGL的指定方式，而不是它的实现方式。

GL的实现会尽可能的做到同步。事实是，当一个渲染指令丢下去并且指令正确返回，但是实际上这个渲染操作并没有开始，更不用谈他已经结束了。只要你没有查询这个渲染指令会改变的state(实际并没有渲染完，因此state保持不变)，你不会意识到这个渲染指令并没有执行完。

可以用一个词组来理解上文说的现象，"as if"，当GL语句执行完了，只能认为他“好像”执行完了。实现必须使得呈现看起来同步，同时也需要足够的异步，因为如果想获得最佳的性能，必须避免将渲染中间过程的结果呈现出来。

同步的含义在于，一个渲染指令下去，渲染指令返回并不意味着这个渲染操作已经结束了，再实际实现的时候可能会有一些延时，这是出于性能考虑。

## 4, Object Model

有一部分GL的state会被聚合为一个objects，这样就可以使用同一个GL指令对一组state进行初始化等操作。几乎所有的GL Object都遵循着相同的命名规范，名字的类型是GLuints类型。使用glGen* ,glBind,glDelete来对不同的object进行操作。0号比较特殊，对绝大多数object来说，0代表他不是一个object，对于framebuffer来说，0代表默认[帧缓存](https://zhida.zhihu.com/search?content_id=219107787&content_type=Article&match_order=1&q=帧缓存&zhida_source=entity)。

## 5, Memory Model

GL的Memory Model管理一个由GL命令设置的state何时对其他的命令可见。一般来说内存模型是一个完全顺序同步的，当state被一个指令设置之后，立刻可以被接下来的的指令看到（之前的指令没法看到）。

## 6, Rendering Pipeline

渲染管道是当用户发出绘图命令时，OpenGL渲染系统所采取的步骤序列。管道是一系列连续的步骤，其中每个步骤执行一些计算，并将其数据传递给下一步进行进一步处理。

OpenGL规范对呈现对象的顺序非常清楚。具体来说，对象是按照用户提供的精确顺序呈现的。虽然实现可以在内部自由地调整顺序，但呈现的输出必须像按顺序处理的一样。

## 7, Errors and Debugging

...

## 8, Shaders

在[渲染管线](https://zhida.zhihu.com/search?content_id=219107787&content_type=Article&match_order=1&q=渲染管线&zhida_source=entity)(rendering pipeline)中，有些是可以改变的，给这个管线中进行编程，就是shader的作用。

## 9, Framebuffer

Framebuffer是一个OpenGL对象，它将图像聚合在一起，呈现操作可以将其用作最终目的地。OpenGL上下文通常有一个Default Framebuffer，它通常与窗口或其他显示设备相关联。打算直接可视化的呈现操作应该放在默认的framebuffer中。

用户可以构造Framebuffer对象，从纹理或渲染[缓冲区](https://zhida.zhihu.com/search?content_id=219107787&content_type=Article&match_order=1&q=缓冲区&zhida_source=entity)提供的图像构建Framebuffer。这对于在屏幕外渲染数据非常有用;稍后可以将数据传输到默认帧缓冲区。

