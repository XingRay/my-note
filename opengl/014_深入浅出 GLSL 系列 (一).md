# 深入浅出 GLSL 系列 (一)

![img](D:\my-note\opengl\assets\168169645281d201tplv-t2oaga2asx-jj-mark3024000q75.png)

今天我们来谈一谈如何开始学习 GLSL ，这是一种适用于可编程渲染管线的着色器语言。 虽然大部分前端 er 们可能对其感觉有些陌生，不过没关系，就像舒马赫并不需要非常了解赛车的每一个零件，这不阻碍他成为一个伟大的车手。

如今 WebGL 逐渐流行，前端er 们确实需要了解一些 3D 图形编程基础，但学习它目的不是为了写出多么高性能绚丽的 shader，而是可以帮助您更深层次的理解图形渲染的本质。这样您在今后使用 Threejs 等高级库时，就能做到知根知底，胸有成竹。

如何开始呢？ 着色器是个什么概念呢？ 什么叫可编程管线？ 带着这些疑问，故事还得从固定管线时代开始说起。

![img](D:\my-note\opengl\assets\1681696452e09e41tplv-t2oaga2asx-jj-mark3024000q75.png) 可编程管线 vs 固定管线

![img](D:\my-note\opengl\assets\1681696452aaab2atplv-t2oaga2asx-jj-mark3024000q75.png)

Monochrome Display Adapter (IBM, 1981)

在早些时候，显示卡的每一项功能都是由固定硬件模块实现的。 比如实现光照、阴影、着色、等每一项工作都需要对应的集成电路来完成。 这样一来，可以实现的效果就非常有限。由于当时 PC 机并没有大规模流行， 真正需要显示卡的地方其实是图形工作站， 工作站的渲染工作类型相对固定，所以采用固定管线是可以胜任的，但随着个人PC的流行，采用固定管线的方案就逐渐被配抛弃了，当然这里面还有很多公司之间里的利益博弈。就省略不提了。

是用固定管线并不意味着你不需要写代码了，只不过你编写程序的创造力受到的很大限制。 OpenGL 4.0 前是支持固定管线的，在以往的旧代码中，OpenGL 的 API 是这个样子。

![img](D:\my-note\opengl\assets\16816964529b640dtplv-t2oaga2asx-jj-mark3024000q75.png)

对于这些功能你只能选择用或不用、无法有其他修改。 编程时需要把它们一个个串联起来， 实现一个复杂效果的过程，简直可以戏称为 “花式调开关”。

![img](D:\my-note\opengl\assets\168169646b376db9tplv-t2oaga2asx-jj-mark3024000q75.png)

固定管线被抛弃的另一个重要原因是，计算资源无法灵活配置，造成浪费。

打个比方说，你是一个老板。手下有 100 名工人。工厂有 3 种工作。“制作电饭煲” 、“制作微波炉” 、“制作台灯” ，这 3 种工作都有对应的厂区，因为无法预测未来工作每一种产品的订单量，所以你只能预先分配这些工人，一旦订单种类不均衡，便会出现有些厂区忙的要死，有些却被闲置的现象。

![img](D:\my-note\opengl\assets\1681696470981ff3tplv-t2oaga2asx-jj-mark3024000q75.png)

为了避免浪费，同时解放更多的功能，可编程管线诞生了。在它的管理下厂区不再是固定的，而是当订单确定后，再组成一个个临时的厂区。

![img](D:\my-note\opengl\assets\1681696476045092tplv-t2oaga2asx-jj-mark3024000q75.png)

这样对于任何一种任务，都可以做到最大限度的提升效率，这种可以随机应变的 “厂区” 便是可编程管线。以前固定管线的所有功能，现在都可以通过编程来实现了，且编程带来的灵活性大大提高了渲染性能。

![img](D:\my-note\opengl\assets\168169647c71ea6etplv-t2oaga2asx-jj-mark3024000q75.png)

左侧为固定管线，右侧为可编程管线。

![img](D:\my-note\opengl\assets\1681696452e09e41tplv-t2oaga2asx-jj-mark3024000q75.png) GLSL编程概念

需要一种语言来控制这些 “工人” 组成临时的生产线，这便是着色器存在的意义。本文中提及的 GLSL 是一种在 OpenGL 中使用的着色器语言，但并不是唯一的着色器语言。 除了 GLSL 还有微软的 HLSL 和英伟达的 CG ，这些我们只要了解即可。

**顶点着色器 与 片元着色器**

一个着色器程序分为两大部分，即 “顶点着色器” 与 “片元着色器” 。简单来说，前者多用于模型构建，后者用于在光栅化时表现出更多细节，一个着色器程序必须同时包含这两部分，程序会先通过 “顶点着色器” 处理再交与 “片元着色器” 渲染细节。

![img](D:\my-note\opengl\assets\168169648b861dadtplv-t2oaga2asx-jj-mark3024000q75.png)

举个例子：比如你想绘制一个蓝色四面体，那就需要用到 8 个顶点 和 “蓝” 这两个参数。 其中 8 个顶点数据先传入 “顶点着色器” 这时一个四面体的模型便建立了，而 “蓝色” 这一参数属于纹理细节，将在 “片元着色器” 中被处理。

![img](D:\my-note\opengl\assets\168169648d0fc635tplv-t2oaga2asx-jj-mark3024000q75.png)

我们来看一个超简单的顶点着色器实例：

```glsl
glsl
复制代码attribute vec3 aPosition;attribute vec3 aColor;varying vec4 vColor;void main(void) {    vColor = aColor;    gl_Position = aPosition;}
```

一个简单的片元着色器实例：

```glsl
glsl
复制代码varying vec4 vColor;void main(void){    gl_FragColor = vColor;}
```

**内置变量**

上例着色器中， `gl_Position` 、 `gl_FragColor` 等这些以 `gl_` 开头的变量都是 `内置变量`，通过给这些特殊的变量赋值，可以完成与硬件的通讯。其中 `gl_Position` 用于放置顶点坐标信息， `gl_FragColor` 用于设置当前片段的颜色。

可以看出 GLSL 是一种面向过程的编程语言，有着与 C 语言类似的语法，但没有 C 语言复杂的指针概念。 常用基本的类型如下：

| 类型                | 说明                                            |
| ------------------- | ----------------------------------------------- |
| void                | 空类型,即不返回任何值                           |
| bool                | 布尔类型 true,false                             |
| int                 | 带符号的整数 signed integer                     |
| float               | 带符号的浮点数 floating scalar                  |
| vec2, vec3, vec4    | n维浮点数向量 n-component floating point vector |
| bvec2, bvec3, bvec4 | n维布尔向量 Boolean vector                      |
| ivec2, ivec3, ivec4 | n维整数向量 signed integer vector               |
| mat2, mat3, mat4    | 2x2, 3x3, 4x4 浮点数矩阵 float matrix           |
| sampler2D           | 2D纹理 a 2D texture                             |
| samplerCube         | 盒纹理 cube mapped texture                      |

其中比较有趣的是 GLSL 中向量的访问是非常人性化的。比如说 vector.xyzw 这说明 vector 表示的是一个三维坐标，其中 xyzw 是可以自由组合的，比如 vector.xy 、 vector.xyz 甚至 vector.zxy 都是可以的。

```apache
apache
复制代码vec4 vector=vec4(1.0,2.0,3.0,1.0);vec3 xyz = vector.xyz; //vec3(1.0,2.0,3.0)vec2 xy = vector.xy; //vec3(1.0,2.0)
```

不仅 vector.xyzw 支持这种特性 同理 vector.rgba （颜色）、vector.stpq（纹理坐标）都可以。

```apache
apache
复制代码vec4 vector=vec4(1.0,2.0,3.0,1.0);vec3 xyz = vector.xyz; //vec3(1.0,2.0,3.0)vec4 rgba=vector.rgba;vec3 rgb=rgba.rgb;vec4 stpq=vector.stpq;... ...
```

通过这一特性，我们可以轻松地将一个多维向量分解。

![img](D:\my-note\opengl\assets\1681696452e09e41tplv-t2oaga2asx-jj-mark3024000q75.png) 变量限定符

下面来讲讲上例中的 attribute 与 varying 的意思。 其实这些都是 GLSL 的 **变量限定符** ，一般用来声明与其宿主程序沟通的接口，什么意思呢？ 假设在 WebGL 环境中， GLSL 的宿主程序就是 javascript， 所有数据均由 javascript 通过事先定义好的变量限定符传入 GLSL。

除了 attribute 和 varying 还有其他修饰符，具体见下表：

| 修饰符    | 说明                                                         |
| --------- | ------------------------------------------------------------ |
| none      | (默认的可省略)本地变量,可读可写,函数的输入参数既是这种类型   |
| const     | 声明变量或函数的参数为只读类型                               |
| attribute | 只能存在于 vertex shader 中,一般用于保存顶点或法线数据,它可以在数据缓冲区中读取数据 |
| uniform   | 在运行时shader无法改变 uniform 变量, 一般用来放置程序传递给 shader 的变换矩阵，材质，光照参数等等. |
| varying   | 主要负责在 vertex 和 fragment 之间传递变量                   |

值得注意的是如果在 顶点着色器 与 片元着色器 中存在同名的 varying 变量，则其值可以由顶点着色器传递与片元着色器，如上例中都存在名为 vColor 的 varying 变量，执行 `vColor=aColor` 后在片元着色器中便可以取到 vColor 的值。

在 GLSL 的宿主程序（ WebGL 中为 javascript ）运行时， 会将四面体的顶点与颜色数据分别传给 aPosition 、 aColor。 一般来讲，给 gl_FragColor 赋值是整个着色器的最后一步工作，意为该点的最终颜色已确定。整体流程如下图所示：

![img](D:\my-note\opengl\assets\168169649401e8ddtplv-t2oaga2asx-jj-mark3024000q75.png)

![img](D:\my-note\opengl\assets\1681696452e09e41tplv-t2oaga2asx-jj-mark3024000q75.png) 函数参数限定符

GLSL 允许自定义函数，但参数默认是以值形式（ in 限定符）传入的，也就是说任何变量在传入时都会被拷贝一份，若想以引用方式传参，需要增加函数参数限定符。

| 限定符            | 说明                                |
| ----------------- | ----------------------------------- |
| < none: default > | 默认使用 in 限定符                  |
| in                | 复制到函数中在函数中可读写          |
| out               | 返回时从函数中复制出来 (可写不可读) |
| inout             | 复制到函数中并在返回时复制出来      |

其中使用 inout 方式传递的参数便与其他 OOP 语言中的引用传递类似，参数可读写，函数内对参数的修改会影响到传入参数本身。

```angelscript
angelscript
复制代码vec4 getPosition(out vec4 p){     p = vec4(0.,0.,0.,1.);    return v4;}void doubleSize(inout float size){    size= size*2.0  ;}
```

![img](D:\my-note\opengl\assets\1681696452e09e41tplv-t2oaga2asx-jj-mark3024000q75.png) 流控制

在语法上，GLSL 与 C 非常相似，但多了一种特殊的控制语句 discard，它会立即跳出片元着色器，不在向下执行任何语句。

```awk
awk
复制代码// for loop 循环for (l = 0; l < numLights; l++){    if (!lightExists[l]);        continue;    color += light[l];}...// while 循环while (i < num){    sum += color[i];    i++;}...do{    color += light[lightNum];    lightNum--;}while (lightNum > 0)...// 条件判断if (true)    discard;
```

![img](D:\my-note\opengl\assets\1681696452e09e41tplv-t2oaga2asx-jj-mark3024000q75.png) 总结

本篇文章我们主要介绍了下面的内容：

- 概念上理解可编程管线与固定管线的区别，以及固定管线被淘汰的原因。
- 理解顶点着色器与片元着色器的分工，以及 attribute 等关键修饰词的用法。
- 理解宿主程序与 GLSL 的合作方式。
- 了解简单的 GLSL 编程流程控制语法。

在接下来的文章中，将结合实例较深入的探索顶点着色器与片元着色器的使用方法。欢迎继续关注。

![img](D:\my-note\opengl\assets\1681696452e09e41tplv-t2oaga2asx-jj-mark3024000q75.png) 延伸阅读

[1] shader 赏析库： https://www.shadertoy.com/

[2] 着色器语言 GLSL 入门大全: https://github.com/wshxbqq/GLSL-Card

![img](D:\my-note\opengl\assets\167f79e133e2d931tplv-t2oaga2asx-jj-mark3024000q75.png)

![img](D:\my-note\opengl\assets\167f79e1362d0fc7tplv-t2oaga2asx-jj-mark3024000q75.png)

标签：

[前端](https://juejin.cn/tag/前端)