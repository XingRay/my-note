# OpenGL 3D渲染技术：坐标系及矩阵变换

[程序员kenney](https://juejin.cn/user/1961184473926766/posts)

2019-06-105,958阅读10分钟

今天给大家讲讲`OpenGL ES`中的坐标系和矩阵变换，`OpenGL ES` 中的坐标系实际上有很多，在我之前的文章中，因为对应的效果对坐标系的要求不高，所用的坐标实际上是跳过的一系列的坐标变换，这点后面会给大家说，而矩阵变换就是将坐标从一个坐标系转换到另一个坐标系下。

我们先来了解一下`OpenGL ES`中的虚拟摄像机，在`OpenGL ES`中，有一个虚拟的摄像机，我们渲染出来的景像实际上就是这个虚拟摄像机所拍摄到的景像，这个虚拟摄像机的效果和我们真实生活中的摄像机效果更类似，我们可以通过调整虚拟摄像机的位置、朝向等参数来得到不同的观察结果，进而得到不同的渲染画面，举个形象一点的例子，例如我们平时玩的3D游戏，有相当一部分是用`OpenGL ES`渲染的，我们控制角色移动靠近一个物体时，物体就会变大，就像我们拿着一个摄像机朝一个物体走过去一样，拍摄到的物体就会变大，角色转身时，能看到不同的画面，就想是我们拿着一个摄像机朝不同的方向拍。

不过这个摄像机最终在`OpenGL ES`中是以矩阵的形式呈现出来的，我们先来看一张整体流程图：

![img](D:\my-note\opengl\assets\16b41d221372258dtplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

这张图给我们展示了`OpenGL ES` 中的坐标系及矩阵变换过程，具体过程是这样：

- 首先`OpenGL ES`有个世界坐标系，我们渲染的物体就是在世界坐标系中，我们的模型需要放到世界坐标系中，那么当我们还没放的时候，模型就和世界坐标系没有联系，它就还处于自己的坐标系中，我们叫做模型坐标系、局部空间、局部坐标系，也就是图中的**LOCAL SPACE**。
- 当我们把模型放到世界坐标系中，模型就在世界坐标系里有了坐标，也就是原来在**LOCAL SPACE**中的那些坐标值，变成了世界坐标系中的坐标值，帮助我们完成这个变换的就是模型矩阵，对应图中的**MODEL MATRIX**，于是这样我们就把模型放到了世界坐标系**WORLD SPACE**中
- 放到世界坐标系后，是不是就确定了我们渲染出来看到的样子？还没有，大家可以想像一下，我把一个东西放在世界坐标系的某个地方，我可以从近处看观察它，也可以从远处观察它，还可以从上下左右观察它，甚至还可以倒着观察它，因此还需要确定我们观察它的状态。这里实际上就是在确定虚拟摄像机的摆放，从API的层面上看，我们只需要设置`Camera`的位置、朝向的点坐标、以及`Camera`的上方向向量就能将观察状态定下来，而这些设置最终会转换成`OpenGL ES`中的视图矩阵，对应图中的**VIEW MATRIX**
- 经过`View Matrix`的变换后，我们观察它的结果就确定了，图中是从距离它一定的距离、上往下观察它，这时候的点坐标就来到了视图坐标系下，对应图中的`VIEW SPACE`
- 这时候，我们能看到什么东西，基本已经确定了，不过还有一步投影变换，这是什么东西？大家想像一下，我们看到同一个东西，是不是通常都是近大远小？那么如何实现近大远小？就要靠投影变换，`OpenGL ES`提供正交投影和透视投影，正交投影没有近大远小的效果，不管在什么距离上看，都一样大，透视投影则有近大远小的效果，也是符合我们实际生活的一种效果，透视投影应用得比较多，可看下面这张经典图：

![img](D:\my-note\opengl\assets\16b41d32d6478670tplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

- 经过投影变换后，就会转换到裁剪坐标系**CLIP SPACE**，这一步不仅做了投影，也做了裁剪，也就是裁剪出上图中左图的梯形区域和右图中的矩阵区域，不在这个区域中的物体不会在渲染的图面中看到。我们玩游戏的时候，大家可能会碰到这样的情况，就是人物走到一个物体的近处，如果很靠近这个物体，画面可能会穿进这个物体中，这就是因为物体的一部分超出了近平面，被裁剪掉了。
- 再下一步是到**NDC（设备标准化坐标）坐标系**（图中省略了这一步直接到屏幕坐标系了），正如其名，这一步的坐标都是经过标准化的，在可视范围内的坐标值都是在-1~~1之间，大家会想我们之前的教程，里面用的坐标是不是都是-1~~1的？我们那种写法实际上就是在用NDC坐标直接来渲染，并没有经过矩阵变换，因此功能比较简单，还用不上矩阵变换。
- 最后就到了我们的屏幕坐标系，这个坐标系大家应该非常熟悉了，android中的各种view里用的坐标就是屏幕坐标。

**这有一个初学者可能会误解的点**，就是认为`OpenGL ES`的坐标范围就是-1~~1，超出-1或1就会超出屏幕就看不见，这种理解其实不准确，坐标范围是多少，取决于说的是什么坐标系，我们在平时做的更多是2D渲染，常常就是像我之前的教程里写的坐标那样，直接使用-1~~1的NDC坐标系，不需要矩阵变换，但实际上`OpenGL ES`的世界坐标系是没有范围的，是负无穷到正无穷，至于某些坐标下的东西是否最后能渲染出来看到，这就取决于前面说的矩阵变换过程，例如将虚拟摄像机对准一个距离999999的物体，并且物体在裁剪区域内，也是能看到的，并不是说坐标一定要是-1~1。

矩阵变换主要还是用在3D渲染和一些特殊的2D效果上，例如一个偏转变形的2D平面，如果直接设置NDC坐标，出来的效果会有畸变，需要自己进行透视矫正，关于透视矫正，这里先不展开说了。

接下来我们来看一下如何在`OpenGL ES`中使用矩阵变换，首先看模型矩阵，前面提到过，模型矩阵是把坐标从模型的局部坐标系转换到世界坐标系，这个变换不仅是位置的变换，还可以有旋转和缩放，例如把一个物体缩小一点、旋转一点后放到世界坐标系中的某个位置上，因此模型矩阵实际上包含的平移、旋转和缩放，它就等于平移矩阵、旋转矩阵和缩放矩阵相乘：

```kotlin
kotlin复制代码val translateMatrix = getIdentity()
val rotateMatrix = getIdentity()
val scaleMatrix = getIdentity()
val modelMatrix = getIdentity()

// 模型矩阵计算
// Calculate the Model matrix
Matrix.translateM(translateMatrix, 0, translateX, translateY, translateZ)
Matrix.rotateM(rotateMatrix, 0, rotateX, 1f, 0f, 0f)
Matrix.rotateM(rotateMatrix, 0, rotateY, 0f, 1f, 0f)
Matrix.rotateM(rotateMatrix, 0, rotateZ, 0f, 0f, 1f)
Matrix.scaleM(scaleMatrix, 0, scaleX, scaleY, scaleZ)
Matrix.multiplyMM(modelMatrix, 0, rotateMatrix, 0, scaleMatrix, 0)
Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, translateMatrix, 0)
```

视图矩阵则是对应前面说的虚拟摄像机，它共由虚拟摄像机的位置、朝向的点坐标、以及虚拟摄像机的上方向向量确定，`OpenGL ES`提供了方法来得到视图矩阵，我们只需要给它传递这些参数就行了：

```kotlin
kotlin复制代码val viewMatrix = getIdentity()
// 视图矩阵计算
// Calculate the View matrix
Matrix.setLookAtM(
    viewMatrix, 
    0, 
    cameraPositionX, cameraPositionY, cameraPositionZ, 
    lookAtX, lookAtY, lookAtZ, 
    cameraUpX, cameraUpY, cameraUpZ
)
```

接下来是投影矩阵，前面提到投影矩阵有正交投影和透视投影两种，本文中使用透视投影，它也是由`OpenGL ES`提供的方法来得到，所需要的参数为近平面矩阵的上、下、左、右坐标，近平面距离和远平面矩离（这张图中的Left、Right、Bottom、Top标在了远平面上，实际在`OpenGL ES`中生成透视投影矩阵的方法参数中的`left`、`抄下t`、`bottom`、`up`指的是近平面）：

![img](D:\my-note\opengl\assets\16b41d372d5860batplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

生成透视投影矩阵代码如下：

```kotlin
kotlin复制代码val projectMatrix = getIdentity()
// 透视投影矩阵计算
// Calculate the Project matrix
Matrix.frustumM(
    projectMatrix,
    0,
    nearPlaneLeft, nearPlaneRight, nearPlaneBottom, nearPlaneTop, 
    nearPlane, 
    farPlane
)
```

现在，模型矩阵、视图矩阵和投影矩阵都生成了，下面将这三个矩阵相乘得到最终的变换矩阵（MVP）：

```kotlin
kotlin复制代码val mvpMatrix = getIdentity()
// MVP矩阵计算
// Calculate the MVP matrix
Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, mvpMatrix, 0)
```

然后将`MVP`矩阵传递到`Vertex Shader`中与顶点相乘进行矩阵变换：

```kotlin
kotlin复制代码#version 300 es
precision mediump float;
layout(location = 0) in vec4 a_position;
layout(location = 1) in vec2 a_textureCoordinate;
layout(location = 2) uniform mat4 u_mvp;
out vec2 v_textureCoordinate;
void main() {
    v_textureCoordinate = a_textureCoordinate;
    gl_Position = u_mvp * a_position;
}"
```

我做一了个demo，可以调节各种参数实时查看效果：

![img](D:\my-note\opengl\assets\16b41d3d0ea452cdtplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

这个demo是渲染一个立方体，立方体每个面上贴上一个花的纹理，上面这张图是一个初始状态，我们来看一下初始的参数：

```kotlin
kotlin复制代码var translateX = 0f         
var translateY = 0f
var translateZ = 0f
var rotateX = 0f
var rotateY = 0f
var rotateZ = 0f
var scaleX = 1f
var scaleY = 1f
var scaleZ = 1f
var cameraPositionX = 0f
var cameraPositionY = 0f
var cameraPositionZ = 5f
var lookAtX = 0f
var lookAtY = 0f
var lookAtZ = 0f
var cameraUpX = 0f
var cameraUpY = 1f
var cameraUpZ = 0f
var nearPlaneLeft = -1f
var nearPlaneRight = 1f
var nearPlaneBottom = -glSurfaceViewHeight.toFloat() / glSurfaceViewWidth
var nearPlaneTop = glSurfaceViewHeight.toFloat() / glSurfaceViewWidth
var nearPlane = 2f
var farPlane = 100f
```

和模型矩阵**Model Matrix**相关的参数是`translate`、`rorate`和`scale`，这里初始时我们不对模型进行变换。

和视图矩阵**VIEW MATRIX**相关的参数是`CameraPosition`、`lookAt`和`CameraUp`，我们把虚拟摄像机放在`(0, 0, 5)`这个位置，并且让它对向`(0, 0, 0)`，并且摄像机的上方向是`(0, 1, 0)`，也就是把摄像正立着。

和投影矩阵**PROJECT MATRIX**相关的参数是近平面`nearPlane`上下左右和距离、以及远平面`farPlane`距离，我们将近平面左右设为-1和1，并且上下根据`GLSurfaceView`和尺寸设置，这样是为了不变形，近平面设置为2，远平面设置为100。

我们渲染的这个立方体顶点坐标都是-1和1，也就是在原点那里，所以上面的图中我们看到的效果就是从Z轴上的`(0, 0, 5)`这个位置正对看向这个立方体，因此只能看到正面。

下面我调节一些参数看看效果：

![img](D:\my-note\opengl\assets\16b41d40d9af8635tplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

上面中我们通过设置模型矩阵的参数将立方体变换到了`(3, 4, -5)`这个位置，因为我们的摄像机没动，还是对着原点看，那么相当于这个立方体在摄像机的右上方，因此摄像机能看到这个立方体的左面和下面。

再继续看：

![img](D:\my-note\opengl\assets\16b41d445dc808a3tplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

这个是把立方体旋转了一下，没什么好解释的。

再来看：

![img](D:\my-note\opengl\assets\16b41d495de63fa5tplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

这个是旋转加上了缩放，把x坐标变小、y坐标变大了，所以横向的边就短了，竖向的就长了。

再来：

![img](D:\my-note\opengl\assets\16b41d4c09a91a13tplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

这个是立方体旋转加上再把虚拟摄像机看的点从`(0, 0, 0)` 变成了`(0, 2, 0)`，因此效果就是立方体就在视野下方了。

再看：

![img](D:\my-note\opengl\assets\16b41d4e4651f951tplv-t2oaga2asx-zoom-in-crop-mark1512000.webp)

这个是把虚拟摄像机的上方向从`(0, 1, 0)`变成了`(1， 1， 0)`，也就是本来虚拟摄像机是正立着的，现在变换歪了45度，所以拍摄到的画面也歪了45度。

好了，还有很多种情况，大家可以到demo里玩玩，看看渲染出来的效果是否与自己的理解和预期一致。

这节的内容比较复杂，如果有疑问，欢迎给我留言讨论哈。

代码在我`github`的`OpenGLESPro`项目中，本文对应的是`SampleMatrixTransform`，项目链接：[github.com/kenneycode/…](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2Fkenneycode%2FOpenGLESPro)

感谢阅读！