# OpenGL ES 高级进阶：多渲染目标（MRT）

[程序员kenney](https://juejin.cn/user/1961184473926766/posts)

2020-09-0615,655阅读3分钟

大家好，我是程序员kenney，今天给大家介绍一个`OpenGL ES 3.0`中的新特性，多渲染目标（Multiple Render Target）。

这里所说的渲染目标，就是`frame buffer`上`color attachment`所绑定的`texture`，我们来回顾一下`frame buffer`：

![img](D:\my-note\opengl\assets\4688c1f8d5c34f45b79460fcb3ef38e2tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

`frame buffer`本身并没有什么实际内容，它是通过将它的各种`attachment`给绑定相应的对象而实现相应的功能，对应渲染内容来说，就是`color attachment`，可以通过`glFramebufferTexture2D()`将`texture`绑定到`color attachment`上，这时绑定这个`frame buffer`进行渲染，就会渲染到绑定的`texture`。

更多`frame buffer`的细节可以参考我的另一篇文章：[Android OpenGL ES 2.0 手把手教学（7）- 帧缓存FrameBuffer](https://juejin.cn/post/6844903842006433805)

在`OpenGL ES 2.0`中，只能绑定0号`color attachment`即`GL_COLOR_ATTACHMENT0`，`OpenGL ES 2.0`官方文档对`attachment`参数的解析原文：Specifies the attachment point to which an image from *texture* should be attached. Must be one of the following symbolic constants: `GL_COLOR_ATTACHMENT0`, `GL_DEPTH_ATTACHMENT`, or `GL_STENCIL_ATTACHMENT`。

同时在`shader`中，也不能指定输出到哪个`color attachment`上，只能输出到`gl_FragColor`，也就是对应`GL_COLOR_ATTACHMENT0`，来回顾一下：

```kotlin
kotlin复制代码// OpenGL ES 2.0中glFramebufferTexture2D
glFramebufferTexture2D(GL_FRAMEBUFFER, 
                       GL_COLOR_ATTACHMENT0, 
                       GL_TEXTURE_2D, 
                       target, 
                       0)
kotlin复制代码// OpenGL ES 2.0中fragment shader
...
void main() {
    gl_FragColor = ...;
}
```

这就意味着，在`OpenGL ES 2.0`中，如果想一次渲染到1个以上的`texture`上是不可能的。

而在`OpenGL ES 3.0`中，通过多渲染目标（Multiple Render Target）这个新特性则可以实现，多渲染目标就是多个`color attachment`上绑定了多个`texture`。那如何绑定多个渲染目标呢？其实很简单，还是通过`glFramebufferTexture2D()`：

```kotlin
kotlin复制代码glFramebufferTexture2D(GL_FRAMEBUFFER, 
                       GL_COLOR_ATTACHMENT0 + i, 
                       GL_TEXTURE_2D, 
                       targets[i], 
                       0)
```

然后再看`fragment shader`：

```kotlin
kotlin复制代码#version 300 es
...
layout(location = 0) out vec4 fragColor0;
layout(location = 1) out vec4 fragColor1;
layout(location = 2) out vec4 fragColor2;
...
void main() {
    fragColor0 = ...;
    fragColor1 = ...;
    fragColor2 = ...;
}
```

可以看到，这时`fragment shader`中输出颜色不再像`OpenGL ES 2.0`那样是给`gl_FragColor`，而是给一些自己定义的颜色输出变量，定义这些变量时可以指定`location`，这里的`location`就对应了`draw buffers`数组中指定的`color attachment`的位置。

最后，还需要通过`glDrawBuffers()`设置`draw buffers`，这是干什么用的呢？就是告诉`OpenGL`，用于承载渲染的`buffer`是哪些，这里注意，虽然我们在`glFramebufferTexture2D()`中已经分别绑定了要渲染到的`color attachment`，但不是绑了多少它就对应渲染多少，而是还可以通过`draw buffers`来指定是哪些以及顺序，这样就更为灵活：

```kotlin
kotlin复制代码val attachments = intArrayOf(
    GL_COLOR_ATTACHMENT0, 
    GL_COLOR_ATTACHMENT1, 
    GL_COLOR_ATTACHMENT2)
val attachmentBuffer = IntBuffer.allocate(attachments.size)
attachmentBuffer.put(attachments)
attachmentBuffer.position(0)
glDrawBuffers(attachments.size, attachmentBuffer)
```

这样一来，`fragment shader`中的`fragColor0`、`fragColor1`、`fragColor2`就分别对应了`GL_COLOR_ATTACHMENT0`、`GL_COLOR_ATTACHMENT1`、`GL_COLOR_ATTACHMENT2`。

Tips：为什么我们在`OpenGL ES 2.0`中不需要设置`draw buffers`？因为默认就是`GL_COLOR_ATTACHMENT0`，如果强行设置成其它的也没有效果。

下面来看我们的例子：

```kotlin
kotlin复制代码#version 300 es
precision mediump float;
layout(location = 0) out vec4 fragColor0;
layout(location = 1) out vec4 fragColor1;
layout(location = 2) out vec4 fragColor2;
uniform sampler2D u_texture;
in vec2 v_textureCoordinate;
void main() {
    vec4 color = texture(u_texture, v_textureCoordinate);
    fragColor0 = vec4(1.0, color.g, color.b, color.a);
    fragColor1 = vec4(color.r, 1.0, color.b, color.a);
    fragColor2 = vec4(color.r, color.g, 1.0, color.a);
}
```

这里我将三个输颜色的R、G、B通道分别设成1以得成3种不同的效果，于是MRT渲染完之后，这3种效果就同时渲染到了`frame buffer`绑定的3个`color attachment`上，然后再把它们渲染出来看效果：

![img](D:\my-note\opengl\assets\6d10a5bd5a9a4d9f8ca18c66f66ce4d7tplv-k3u1fbpfcp-zoom-in-crop-mark1512000.webp)

可以看到我们在一次渲染中同时把3种不同的效果渲染到了3个纹理上，这在一些场景下还是很有用的，可以有效地减少`draw call`次数。

代码在我`github`的`OpenGLESPro`项目中，本文对应的是`SampleMultiRenderTarget`，项目链接：[github.com/kenneycode/…](https://link.juejin.cn/?target=https://github.com/kenneycode/OpenGLESPro)

感谢阅读！