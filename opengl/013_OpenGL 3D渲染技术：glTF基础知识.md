# OpenGL 3D渲染技术：glTF基础知识

[程序员kenney](https://juejin.cn/user/1961184473926766/posts)

2020-11-2113,260阅读7分钟

大家好，我是程序员kenney，今天给大家介绍`glTF`的基础知识。

### `glTF`是什么？

它是`GL Transmission Format`的缩写，是`khronos`推出的一种描述3D模型的格式，目标是使其成为一种通用的3D模型格式。它的官方github是[github.com/KhronosGrou…](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2FKhronosGroup%2FglTF%EF%BC%8C%E6%9C%AC%E6%96%87%E7%9A%84%E4%B8%AD%E5%9B%BE%E7%89%87%E6%9D%A5%E8%87%AA%E4%BA%8E%E5%85%B6%E4%B8%AD%E3%80%82)

我们知道3D模型有各种各样的格式，如`obj`、`FBX`、`dea`等，格式的多种多样带来了一个问题的就是各种软件、渲染引擎、游戏引擎之间的3D模型的不通用，例如在3D设计软件中设计好一个3D模型之后，想把它放到一个渲染引擎中渲染，如果没有一个通用的格式，渲染引擎可能也有一套自己的格式，这样就要做格式转换，而有些格式是不开源的，需要官方提供转换工具，非常麻烦。

而如果有一个通用的格式大家都支持，这就好办了，就像图片中的`jpeg``一样，通常的图片编辑软件都能导出jpeg`，通常的图片查看软件也都能打开，这就非常方便，而`glTF`就是要成为3D模型界的"`jpeg`。

`glTF`是一用`json`格式编写的，目前现在推出了2.0版本，本篇文章也是讲解2.0版本，`glTF`格式中可以描述的信息相当丰富，本篇文章先给大家介绍一些基础的字段：

- 场景和节点描述：`scenes`、`nodes`
- 网格描述：`meshes`
- 数据描述：`buffers`、`bufferViews`、`accessors`
- 材质描述：`materials`
- 纹理、图片和采样器描述：`textures`、`images`、`samplers`
- 摄像机描述：`cameras`

### 总览

我们先来从一张图总体上看一下`glTF`文件的结构，每一种类型的字段，基本是都是用数组来组织，在每个字段中，又可以通过索引去引用其它字段。

它类似一个树状，但又不是严格的树。最顶层的是场景，场景中包含了一些节点，节点中又可以套子节点，节点中可以有`mesh`、摄像机，如果是`mesh`，还可以为它指定材质及`mesh`网格数据，材质中可以指定纯色及图片纹理及其采样配置，还可以描述蒙皮及动画。

![img](D:\my-note\opengl\assets\19d11d22e5bf46e290d409e23df53d64tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

### 场景和节点

场景和节点分别用`scenes`、`nodes`字段描述，场景就是代表要渲染的一些东西的集合，场景里面包含一些节点，每个节点可以认为是一个物体（也可以是摄像机），节点也可以是空的，节点中可以继续挂子结点。

![img](D:\my-note\opengl\assets\1df326788e9b453087152249e94d0e4ctplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

来看上面这个例子，`scene`字段指定了使用第0个场景，一般来说，同一时刻只会渲染一个场景。`scenes`字段是一个场景数组，描述所有场景，这个例子中场景只有一个，它里面的`nodes`字段描述这个场景中有哪些节点，这里是用索引为0、1、2的节点，它就对应了下面`nodes`数组中第0、1、2个节。`nodes`字段描述了所有节点，节点里可以用`children`描述子节点，子节点的描述方式也是索引号。这样，通过上图左中的这段`json`，我们就描述了如上图右中的那样一个场景结构。

![img](D:\my-note\opengl\assets\95f6cd0d54f14db49ff8c03c5b718db6tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

可以为每个结点指定它的变换参数，这个变换参数是它相对于父节点的变换，可以用变换矩阵描述，也可以分别指定平移、旋转和缩放。还可以指定它的`mesh`及摄像机，`mesh`就描述了这个节点是什么物体，而`camera`会影响这个节点被观察的样子。

### 网格描述

有了节点之后，我们要描述节点的东西是什么，一个东西首先要有形状，比如一个立方体，或者一个圆柱，这就需要`meshs`来描述：

![img](D:\my-note\opengl\assets\99b1a60d7e874107a07541da235e1bd9tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

`meshs`同样也是一个数组，每个`mesh`中由`primitives`描述图元，其中的`mode`是图元的种类，也就是点、线或者三角形，`indices`表示`accessors`中的索引，后面会讲到。`attributes`描述的是`attribute`的结构，里面可以有顶点坐标、纹理坐标、法向量等，同样也是以索引的方式引用`accessors`。最后的`material`表示这个`mesh`的材质，也是用索引来引用。

### 数据描述

`glTF`中用`buffers`、`bufferViews`、`accessors`来描述数据：

![img](D:\my-note\opengl\assets\7e3d3d5ccbf84ab498a9259f01444598tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

`buffers`是数据块数组，每个`buffer`由长度和路径描述，`bufferViews`同样也是数组，其中每个`bufferView`表示一个`buffer`的视图。这个怎么理解呢？`buffer`是一堆纯数据，它本身是不描述这堆数据用来做什么的，而`bufferView`就可以理解成如何去看待这堆数据。

其中`buffer`表示它对应的`buffers`中的索引。

`target`描述了它是用来干什么的，比如上图中的`34963`就对应了`GL_ELEMENT_ARRAY_BUFFER`，这个值是和`OpenGL`库里定义的值是一致的，这样使用起来就相当友好，`glBindBuffer()`可以直接传这里解析出来的`target`值而不用做映射转换。

`byteOffset`描述的是这块`buffer`的偏移字节，`byteLength`是字节长度，`bytteStride`描述的是一个数据的开始距离下一个数据开始的跨度，如果相同语义的数据不是连续存在放的，`bytteStride`就会比一份数据的长度大，因为它要跨过其它类型的数据。

再往下是`accessors`，它同样也是一个数组，其中每个`accessor`描述了对应`bufferView`中的数据以什么样的方式进行访问，在上图上，偏移了4个字段，以`VEC2`的方式取数据，即二维向量，向量每个成份的类型`componentType`是5126，它对应的是`GL_FLOAT`，`count`表示取数据的个数。

### 材质描述

`materials`中每个元素是一个`material`：

![img](D:\my-note\opengl\assets\0a04f31d9311400d89affc76b1437470tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

例如上图中的例子通过金属度-粗糙度模型来描述材质，其中有一些材质纹理图，比如基础纹理图、法向图、遮挡纹理图等，每个纹理图通过`index`来对应纹理图数组中的索引，纹理坐标也是类似的。

### 纹理、图片和采样器描述

纹理、图片和采样器分别通过`textures`、`images`、`samplers`描述，看下面的例子：

![img](D:\my-note\opengl\assets\0bc1c5c9476c448cb334e503a9a8a412tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

`textures`中每个元素通过`source`索引到`images`数组中，从而找到对应的纹理数据，`images`数组中的元素可以以图片路径的方式提供图片，也可以用`bufferView`的方式，`bufferView`最终又对应到一个数据块上。`samplers`则描述了采样器的配置，大家一看里面的字段就会很熟悉，同样的，它也是直接对应了`OpenGL`库里定义的值，解析出来直接使用，非常方便。

### 摄像机描述

摄像机用`cameras`描述：

![img](D:\my-note\opengl\assets\6bf1523ba0d740db83b42c216328759atplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

熟悉`OpenGL`的同学一眼就能看出里面定义了一个透视投影相机和正交投影相机，以及视角、近/远平面等参数，这个比较简单，没有太多好说的。

### 完整例子

我们来看一下完整的例子，`glTF`官方提供了很多`sample models`：[github.com/KhronosGrou…](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2FKhronosGroup%2FglTF-Sample-Models)

我选取了一个比较简单的立方体，大家看完文章可以尝试阅读一下它的`glTF`文件，看看是否能理解其中的描述：

```json
json复制代码{
   "accessors" : [
      {
         "bufferView" : 0,
         "byteOffset" : 0,
         "componentType" : 5123,
         "count" : 36,
         "max" : [
            35
         ],
         "min" : [
            0
         ],
         "type" : "SCALAR"
      },
      {
         "bufferView" : 1,
         "byteOffset" : 0,
         "componentType" : 5126,
         "count" : 36,
         "max" : [
            1.000000,
            1.000000,
            1.000001
         ],
         "min" : [
            -1.000000,
            -1.000000,
            -1.000000
         ],
         "type" : "VEC3"
      },
      {
         "bufferView" : 2,
         "byteOffset" : 0,
         "componentType" : 5126,
         "count" : 36,
         "max" : [
            1.000000,
            1.000000,
            1.000000
         ],
         "min" : [
            -1.000000,
            -1.000000,
            -1.000000
         ],
         "type" : "VEC3"
      },
      {
         "bufferView" : 3,
         "byteOffset" : 0,
         "componentType" : 5126,
         "count" : 36,
         "max" : [
            1.000000,
            -0.000000,
            -0.000000,
            1.000000
         ],
         "min" : [
            0.000000,
            -0.000000,
            -1.000000,
            -1.000000
         ],
         "type" : "VEC4"
      },
      {
         "bufferView" : 4,
         "byteOffset" : 0,
         "componentType" : 5126,
         "count" : 36,
         "max" : [
            1.000000,
            1.000000
         ],
         "min" : [
            -1.000000,
            -1.000000
         ],
         "type" : "VEC2"
      }
   ],
   "asset" : {
      "generator" : "VKTS glTF 2.0 exporter",
      "version" : "2.0"
   },
   "bufferViews" : [
      {
         "buffer" : 0,
         "byteLength" : 72,
         "byteOffset" : 0,
         "target" : 34963
      },
      {
         "buffer" : 0,
         "byteLength" : 432,
         "byteOffset" : 72,
         "target" : 34962
      },
      {
         "buffer" : 0,
         "byteLength" : 432,
         "byteOffset" : 504,
         "target" : 34962
      },
      {
         "buffer" : 0,
         "byteLength" : 576,
         "byteOffset" : 936,
         "target" : 34962
      },
      {
         "buffer" : 0,
         "byteLength" : 288,
         "byteOffset" : 1512,
         "target" : 34962
      }
   ],
   "buffers" : [
      {
         "byteLength" : 1800,
         "uri" : "Cube.bin"
      }
   ],
   "images" : [
      {
         "uri" : "Cube_BaseColor.png"
      },
      {
         "uri" : "Cube_MetallicRoughness.png"
      }
   ],
   "materials" : [
      {
         "name" : "Cube",
         "pbrMetallicRoughness" : {
            "baseColorTexture" : {
               "index" : 0
            },
            "metallicRoughnessTexture" : {
               "index" : 1
            }
         }
      }
   ],
   "meshes" : [
      {
         "name" : "Cube",
         "primitives" : [
            {
               "attributes" : {
                  "NORMAL" : 2,
                  "POSITION" : 1,
                  "TANGENT" : 3,
                  "TEXCOORD_0" : 4
               },
               "indices" : 0,
               "material" : 0,
               "mode" : 4
            }
         ]
      }
   ],
   "nodes" : [
      {
         "mesh" : 0,
         "name" : "Cube"
      }
   ],
   "samplers" : [
      {}
   ],
   "scene" : 0,
   "scenes" : [
      {
         "nodes" : [
            0
         ]
      }
   ],
   "textures" : [
      {
         "sampler" : 0,
         "source" : 0
      },
      {
         "sampler" : 0,
         "source" : 1
      }
   ]
}
```

`glTF`有很多方法能方便地查看效果，可以在线看，比如[gltf-viewer.donmccurdy.com](https://link.juejin.cn/?target=https%3A%2F%2Fgltf-viewer.donmccurdy.com)，上面那个`glTF`例子的渲染效果是这样的：

![img](D:\my-note\opengl\assets\f722634c25a24d18a1586b37d2672a97tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

谢谢阅读！如有疑问，欢迎在评论区交流~

欢迎关注我的github：[www.github.com/kenneycode](https://link.juejin.cn/?target=https%3A%2F%2Fwww.github.com%2Fkenneycode)