# WebGL 基础 - GLSL

[Eagle_Clark](https://juejin.cn/user/2875978150314408/posts)

2022-12-11308阅读9分钟

专栏： 

Web可视化

> 官方规范
>
> [WebGL 规范](https://link.juejin.cn/?target=https%3A%2F%2Fregistry.khronos.org%2Fwebgl%2Fspecs%2Flatest%2F2.0%2F)
>
> [OpenGL ES 规范](https://link.juejin.cn/?target=https%3A%2F%2Fwww.khronos.org%2Ffiles%2Fopengles_shading_language.pdf)
>
> 其它参考资料
>
> [learnopengl-cn](https://link.juejin.cn/?target=https%3A%2F%2Flearnopengl-cn.readthedocs.io%2Fzh%2Flatest%2F)
>
> [OpenGLES-基础篇](https://link.juejin.cn/?target=https%3A%2F%2Fcolin1994.github.io%2F2017%2F11%2F11%2FOpenGLES-Lesson04%2F)
>
> [OpenGLES-高级篇](https://link.juejin.cn/?target=https%3A%2F%2Fcolin1994.github.io%2F2017%2F11%2F12%2FOpenGLES-Lesson05%2F)

GLSL - OpenGL Shading Language 也称作 GLslang，是一个类C语言的。当我们想要调用 GPU 的能力的时候就需要使用 GLSL，因为 GPU 是识别不了 JS 的。

下面将从 GLSL 的内置变量、数据类型、内置函数、矩阵运算等方面简要介绍一下 GLSL 的使用方法。

# 内置变量

内置变量都以 `gl_` 开头，所以我们在自定义变量的时候需要避开这个前缀。

## 顶点着色器

### gl_Position

顶点坐标，类型 `vec4`。

### gl_PointSize

点的大小，浮点数，1.0 代表大小为 1 x 1 px 的点，100.0 代表大小为 100 x 100 px 的点。

## 片元着色器

### gl_FragColor

当前片元的颜色，类型 `vec4`。

### gl_FragCoord

`gl_FragCoord` 是一个只读变量（mediump vec4），存储了片元的窗口相对坐标 `x`、`y`、`z` 及 `1/w`。该值是在顶点处理阶段之后对图元插值生成片段计算所得。`z` 分量是深度值用来表示片段的深度。

# 限定符

## 存储限定符

在声明变量时，应根据需要使用存储限定符来修饰

### const

常量限定符。

```glsl
glsl
复制代码const float pi = 3.14159;
```

### attribute

`attribute` 变量只能定义在顶点着色器中，它的作用是接收 `JS` 程序传递过来的与顶点有关的数据，比如：顶点坐标、顶点颜色、法线等都可以通过 `attribute` 变量传递过来。

```glsl
glsl
复制代码attribute vec4 position;
```

### uniform

`uniform` 用来修饰全局变量，它既可以在顶点着色器中定义，也可以在片元着色器中定义，用来接收与顶点无关的数据。比如：我们会用 `uniform` 变量来接收投影矩阵、模型矩阵等。

```glsl
glsl
复制代码uniform mat4 viewProjMatrix;
```

### varying

`varying` 变量一般是成对定义的，即在顶点着色器定义，在片元着色器中使用。它所修饰的变量在传递给片元着色器之前会进行插值化处理。

```glsl
glsl
复制代码varying vec4 color;
```

## 精度限定符

`OpenGL ES` 与 `OpenGL` 之间的一个区别就是在 GLSL 中引入了精度限定符。精度限定符可使着色器的编写者明确定义着色器变量计算时使用的精度，变量可以选择被声明为低、中或高精度。精度限定符可告知编译器使其在计算时缩小变量潜在的精度变化范围，当使用低精度时，`OpenGL ES` 的实现可以更快速和低功耗地运行着色器，效率的提高来自于精度的舍弃，如果精度选择不合理，着色器运行的结果会很失真。

### highp

满足顶点着色语言的最低要求。对片段着色语言是可选项。

```glsl
glsl复制代码highp vec4 position;
precision highp float;
```

### mediump

满足片段着色语言的最低要求，其对于范围和精度的要求必须不低于lowp并且不高于highp。

```glsl
glsl
复制代码precision mediump float;
```

### lowp

范围和精度可低于mediump，但仍可以表示所有颜色通道的所有颜色值。

```glsl
glsl
复制代码varying lowp vec4 color;
```

## 参数限定符

### in

默认使用的缺省限定符，指明参数传递的是值，并且函数不会修改传入的值，用于值传递。

### inout

指明参数传入的是引用，如果在函数中对参数的值进行了修改，当函数结束后参数的值也会修改，用于引用传递。

### out

参数的值不会传入函数，但是在函数内部修改其值，函数结束后其值会被修改。

# 数据类型

## 基础类型

### void

用于无返回值的函数或空的参数列表

### bool

布尔类型。

### int

整数，可以是负数。

### float

浮点数。

```glsl
glsl复制代码float size = 1.0; // 正确
float size = 1; // 报错
```

## 向量

颜色信息、齐次坐标、法向量等都是向量存储的。

### 浮点向量

- vec2：存储2个浮点数。
- vec3：存储3个浮点数。
- vec4：存储4个浮点数。

### 整型向量

- ivec2：存储2个整数。
- ivec3：存储3个整数。
- ivec4：存储4个整数。

### 布尔向量

- bvec2：存储2个布尔值。
- bvec3：存储3个布尔值。
- bvec4：存储4个布尔值。

### 向量的使用技巧

每个向量我们都可以用 `{s、t、p、q}`,`{r、g、b、a}`,`{x、y、z、w}`来表示，获取各个位置的元素。

```glsl
glsl复制代码vec4 v = vec(1, 2, 3, 4);

// 访问第一个元素
v.s;
v.r;
v.x;
v[0];

// 访问第二个元素
v.t;
v.g;
v.y;
v[1];
// 访问第三个元素
v.p;
v.b;
v.z;
v[2];
// 访问第四个元素
v.q;
v.a;
v.w;
v[3];

// 给低维向量
// xyzw 方式赋值
vec2 v1 = v.xy;
// stpq 赋值
vec2 v1 = v.st;
// rgba 赋值
vec2 v1 = v.rg;

// 构造函数式
vec2 v1 = vec2(v.x, v.y);
vec2 v1 = vec2(v.s, v.t);
vec2 v1 = vec2(v.r, v.g);

// 还可以是下面这种形式
vec2 v1 = vec2(v.xy);
vec2 v1 = vec2(v.xx);
```

### 向量的运算

#### 向量与常数

向量与常数进行加、减、乘、除就是向量各分量皆进行加、减、乘、除即可。

```glsl
glsl复制代码// 加法
vec4 v1 = v + f; // = (x + f, y + f, z + f, w + f)
// 减法
vec4 v1 = v - f; // = (x - f, y - f, z - f, w - f)
// 乘法
vec4 v1 = v * f; // = (x * f, y * f, z * f, w * f)
// 除法
vec4 v1 = v / f; // = (x / f, y / f, z / f, w / f)
```

#### 向量与向量

向量与向量之间运算之后会返回一个新的向量，不过两个向量必须维度相同才能就行运算。

```glsl
glsl复制代码// 加法
vec4 v3 = v1 + v2; // = (x1 + x2, y1 + y2, z1 + z2, w1 + w2)
// 减法
vec4 v3 = v1 - v2; // = (x1 - x2, y1 - y2, z1 - z2, w1 - w2)

// 乘法
vec4 v3 = v1 * v2; // = (v1 * v2, y1 * y2, z1 * z2, w1 * w2)
// 减法
vec4 v3 = v1 / v2; // = (x1 / x2, y1 / y2, z1 / z2, w1 / w2)
```

注意：向量的乘法是有三种的，还有两种是 `点乘` 和 `叉乘`。

```glsl
glsl复制代码// 点乘
float v3 = dot(v1, v2);

// 叉乘
float v3 = cross(v1, v2);
```

## 矩阵

### 分类与构造

二阶矩阵 mat2、三阶矩阵 mat3、四阶矩阵 mat4，用得最多的应该是四阶矩阵，所以下面举例都用四阶矩阵。

```glsl
glsl复制代码mat4 m = mat4(
    1, 2, 3, 4, //第一列
    5, 6, 7, 8, //第二列
    9, 10, 11, 12, //第三列
    13, 14, 15,16 // 第四列
);

// 对角矩阵
mat4 a = mat4(1.0);
// 1.0 0 0 0
// 0 1.0 0 0
// 0 0 1.0 0
// 0 0 0 1.0

// 利用向量构造矩阵
//第一列
vec4 c0 = vec4(1, 2, 3, 4);
//第二列
vec4 c1 = vec4(5, 6, 7, 8);
//第三列
vec4 c2 = vec4(1, 2, 3, 4);
//第四列
vec4 c3 = vec4(5, 6, 7, 8);

mat4 m = mat4(c0, c1, c2, c4);

// 向量与数字混合构造矩阵
vec4 c0 = vec4(1, 2, 3, 4);
vec4 c1 = vec4(5, 6, 7, 8);
vec4 c2 = vec4(1, 2, 3, 4);

mat4 m = mat4(c0, c1, c2, 5, 6, 7, 8);
```

### 矩阵的运算

矩阵运算主要是乘法运算，矩阵乘法使用 `*` 来表示。

在 GLSL 中矩阵是以`列主序`存储的，指在内存中矩阵是逐列存储，举个例子：

```glsl
glsl复制代码// 代码中的样子
0 1 2 3
4 5 6 7
8 9 0 1
2 3 4 5

// 在内存中存储为
0 4 8 2 1 5 9 3 2 6 0 4 3 7 1 5
    
// 实际数学上的矩阵是
0 4 8 2
1 5 9 3
2 6 0 4
3 7 1 5
```

所以，矩阵在和向量相乘时要放在乘号左侧（矩阵乘法不满足交换律，左乘右乘结果不相同）。

```glsl
glsl复制代码mat4 m = mat4(1.0);
vec4 v1 = m * vec4(1, 2, 3, 4);
```

求转置、求逆：

```glsl
glsl复制代码mat4 m0 = mat4(1.0);
// 转置
mat4 m1 = transpose(m0);
// 求逆
mat4 m2 = inverse(m0);
```

## 其它

### sampler2D

2D 纹理采样器。

```glsl
glsl
复制代码uniform sampler2D u_Texture;
```

### samplerCube

3D纹理采样器。

### 数组

注意：除了 uniform 变量之外，数组的索引只允许使用常数整型表达式；在 GLSL 中不能在创建的同时给数组初始化，即数组中的元素需要在定义数组之后逐个初始化，且数组不能使用 const 限定符。

```glsl
glsl复制代码// 声明了一个长度为4的浮点型数组，变量名为floatArray
float floatArray[4];

// 声明了一个长度为2的四维向量数组，变量名为vecArray
vec4 vecArray[2];

// 未设置长度，稍后可以重新声明
int indices[];
```

### 结构体

```glsl
glsl复制代码struct customStruct
{
  vec4 color;
  vec2 position;
} customVertex;

customVertex = customStruct(vec4(0.0, 1.0, 0.0, 0.0), // color
							vec2(0.5, 0.5)); 		  // position

vec4 color = customVertex.color;
vec2 position = customVertex.position;
```

产生一个新的类型叫做 `customStruct`，一个名为 `customVertex` 的变量。

# 函数

GLSL 着色器是从 main 函数开始执行的。另外， GLSL 也支持自定义函数。当然，如果一个函数在定以前被调用，则需要先声明其原型。

值得注意的一点是，GLSL 中函数不能够递归调用，且必须声明返回值类型（无返回值时声明为void）。

## 自定义函数

```glsl
glsl复制代码vec4 getPosition(){ 
    vec4 v4 = vec4(0.0, 0.0, 0.0, 1.0);
    return v4;
}

void doubleSize(inout float size){
    size = size * 2.0;
}

void main() {
    float psize = 10.0;
    doubleSize(psize);
    gl_Position = getPosition();
    gl_PointSize = psize;
}
```

## 内置函数

### 向量函数

| 函数名    | 作用                                                         |
| --------- | ------------------------------------------------------------ |
| cross     | 叉乘                                                         |
| dot       | 点乘                                                         |
| normalize | 单位化向量，返回一个和原向量方向相同，但是 长度为1的单位向量 |
| reflect   | 根据入神向量和法线向量，计算出反射向量                       |
| length    | 计算向量长度                                                 |
| distance  | 计算两个向量之间的距离                                       |

### 常用数学函数

| 函数名 | 作用               |
| ------ | ------------------ |
| abs    | 取绝对值           |
| floor  | 向下取整           |
| round  | 四舍五入           |
| ceil   | 向上取整           |
| fract  | 取浮点数的小数部分 |
| mod    | 取模               |
| min    | 最小值             |
| max    | 最大值             |
| pow    | 乘方               |
| sqrt   | 开方               |

### 三角函数

| 函数名  | 作用                                  |
| ------- | ------------------------------------- |
| radians | 将角度（如90度）转化为弧度（PI/2）    |
| degrees | 将弧度（如PI / 2）转化为角度（90 度） |
| sin     | 求弧度的正弦                          |
| cos     | 求弧度的余弦                          |
| tan     | 求弧度的正切                          |
| asin    | 根据正弦值求对应的弧度                |
| acos    | 根据余弦值求对应的弧度                |
| atan    | 根据正切值求对应的弧度                |

# 流程控制语句

## if-else

```glsl
glsl复制代码if (color.a < 0.25) {
	color *= color.a;
} else {
	color = vec4(0.0);
}
```

## for

```glsl
glsl复制代码for (int i = 0; i < 3; i++) {
	sum += i;
}
glsl复制代码float myArr[4];
for (int i = 0; i < 3; i++) {
  	// 错误, [ ]中只能为常量或 uniform 变量，不能为整数量变量（如：i，j，k）
	sum += myArr[i]; 
}
...
uniform int loopIter;
// 错误, 循环变量 loopIter 的值必须是编译时已知
for (int i = 0; i < loopIter; i++) {
	sum += i;
}
```

# 预处理

一下是宏及宏的条件判断：

```glsl
glsl复制代码#define
#undef
#if
#ifdef
#ifndef
#else
#elif
#endif
```

宏不能带参数定义，使用 `#if`，`#else` 和 `#elif` 可以用来判断宏是否被定义过。以下是一些预先定义好的宏及他们的描述：

```glsl
glsl复制代码__LINE__ 	// 当前源码中的行号.
__FILE__ 	// OpenGL ES 2.0 中始终为 0.
__VERSION__ // 一个整数,指示当前的 glsl版本. 比如 100 ps: 100 = v1.00
GL_ES 		// 如果当前是在 OPGL ES 环境中运行则 GL_ES 被设置成1,一般用来检查当前环境是不是 OPENGL ES.
```

通过判断系统环境，来选择合适的精度

```glsl
glsl复制代码#ifdef GL_ES
precision highp float;
#endif
```

自定义宏：

```glsl
glsl复制代码#define NUM 100
#if NUM==100
#endif
```

标签：