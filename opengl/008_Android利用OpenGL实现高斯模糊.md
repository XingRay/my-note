# Android利用OpenGL实现高斯模糊

[滑板上的老砒霜](https://juejin.cn/user/3650034331820743/posts)

2018-08-264,390阅读4分钟

# 0.前言

最近有个需求，需要实现图片的高斯模糊，问题来了，怎么搞： 1.java算法，操控bitmap实现高斯算法 2.renderscript 3.ndk 4.opengl 其中处理大图的时候，opengl无疑是效率最好的，java是最差的，ndk和renderscript差不多。这里我决定用opengl来实现。

先看效果



![img](D:\my-note\opengl\assets\16575f73733dbdb4tplv-t2oaga2asx-jj-mark3024000q75.png)



原图



![img](D:\my-note\opengl\assets\16575f7b3461e874tplv-t2oaga2asx-jj-mark3024000q75.png)



模糊后

# 1.高斯算法

如何实现模糊，先理解什么是模糊，模糊就可以理解为一个中间点像素取周围相邻像素的平均值，这就实现了一次模糊，相当于使中间节点失去了细节，实现了一种平滑。接下来的问题就是，既然每个点都要取周边像素的平均值，那么应该如何分配权重呢？如果使用简单平均，显然不是很合理，因为图像都是连续的，越靠近的点关系越密切，越远离的点关系越疏远。因此，加权平均更合理，距离越近的点权重越大，距离越远的点权重越小。这里就用到了高斯函数，也是我们经常接触的正态分布。



![img](D:\my-note\opengl\assets\16575be2c2772371tplv-t2oaga2asx-jj-mark3024000q75.png)



这里我们需要一个二维正态分布



![img](D:\my-note\opengl\assets\16575bfcb6720347tplv-t2oaga2asx-jj-mark3024000q75.png)



这里就不普及什么是高斯函数了，不懂得回去翻翻高中课本吧。

# 2.代码实现

```
复制代码class BlurImageView(context: Context, attributeSet: AttributeSet?) : GLSurfaceView(context, attributeSet)
{
    init
    {
        setEGLContextClientVersion(3)

    }

    fun setImageBitmap(bitmap: Bitmap)
    {
        setRenderer(BlurImageViewRender(context, bitmap))
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}
```

可以看到BlurImageView是继承自GLSurfaceView，因为使用opengles，GLSurfaceView集成了EGL，不需要我们自己处理了，接着看BlurImageViewRender

```
复制代码class BlurImageViewRender(private val context: Context, private val bitmap: Bitmap) : GLSurfaceView.Renderer
{


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?)
    {

    }

    private fun readSlgl(fileName: String): String
    {
        val buffer = StringBuffer()
        try
        {
            val inReader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
            var item = inReader.readLine()
            while (item != null)
            {
                buffer.append(item).append("\n")
                item = inReader.readLine()
            }
            inReader.close()
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }

        return buffer.toString()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int)
    {
        val vertex = readSlgl("vertex.slgl")
        val fragment = readSlgl("fragment.slgl")
        prepare(vertex, fragment, bitmap, width, height)
        //        bitmap.recycle()
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?)
    {
        draw()
    }

    companion object
    {
        init
        {
            System.loadLibrary("blurimageview")
        }
    }

    external fun prepare(vertex: String, fragment: String, bitmap: Bitmap, scrWidth: Int, scrHeight: Int)

    external fun draw()
}
```

可以看到这里有两个native方法，一个prepare，一个draw，preapre就是准备相应的顶点数据，生成program等操作，draw就是opengl用来绘制的。 整个代码的关键就是片段着色器了。首先我们根据上一节的分析要实现高斯模糊过滤我们需要一个二维四方形作为权重，从这个二维高斯曲线方程中去获取它。然而这个过程有个问题，就是很快会消耗极大的性能。以一个32×32的模糊kernel为例，我们必须对每个fragment从一个纹理中采样1024次！

幸运的是，高斯方程有个非常巧妙的特性，它允许我们把二维方程分解为两个更小的方程：一个描述水平权重，另一个描述垂直权重。我们首先用水平权重在整个纹理上进行水平模糊，然后在经改变的纹理上进行垂直模糊。利用这个特性，结果是一样的，但是可以节省难以置信的性能，因为我们现在只需做32+32次采样，不再是1024了！这叫做两步高斯模糊。

```
复制代码#version 300 es
precision mediump float;
in vec2 textureCoord;
uniform sampler2D sampler;
out vec4 fragColor;
uniform bool isVertical;
void main()
{
   vec2 tex_offset =vec2(1.0/300.0,1.0/300.0);
   vec4 orColor=texture(sampler,textureCoord);
   float orAlpha=orColor.a;
   float weight[5] = float[] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
   vec3 color=orColor.rgb*weight[0];
   if(!isVertical)
   {
     for(int i=1;i<5;i++)
     {
       color+=texture(sampler,textureCoord+vec2(tex_offset.x * float(i), 0.0)).rgb*weight[i];
       color+=texture(sampler,textureCoord-vec2(tex_offset.x * float(i), 0.0)).rgb*weight[i];

     }
   }
   else
   {
      for(int i=1;i<5;i++)
      {
        color+=texture(sampler,textureCoord+vec2(0.0,tex_offset.y * float(i))).rgb*weight[i];
        color+=texture(sampler,textureCoord-vec2(0.0,tex_offset.y * float(i))).rgb*weight[i];
      }
   }
   fragColor=vec4(color,orAlpha);
}
```

这里我们取了一个9*9的高斯核，isVertical用来判断是进行垂直模糊还是水平模糊。 先看水平模糊

```
复制代码for(int i=1;i<5;i++)
     {
       color+=texture(sampler,textureCoord+vec2(tex_offset.x * float(i), 0.0)).rgb*weight[i];
       color+=texture(sampler,textureCoord-vec2(tex_offset.x * float(i), 0.0)).rgb*weight[i];

     }
```

我们根据当前位置的像素偏移的位置的像素乘以相应的权重，然后相加求平均值，就像上一节所说的那样。 垂直模糊与水平模糊一样，就是偏移是相对于y坐标的。

```
复制代码void prepareFrameBuffer(int width, int height) {

    glGenFramebuffers(2, FBUFFERS);
    glGenTextures(2, FBUFFERTEXTURE);
    for (int i = 0; i < 2; i++) {
        glBindFramebuffer(GL_FRAMEBUFFER, FBUFFERS[i]);
        glBindTexture(GL_TEXTURE_2D, FBUFFERTEXTURE[i]);
        glTexImage2D(
                GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, NULL
        );
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glGenerateMipmap(GL_TEXTURE_2D);
        glFramebufferTexture2D(
                GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, FBUFFERTEXTURE[i], 0
        );
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            LOGE("frame buffer not completed");
        }
    }

}
```

先生成了两个帧缓冲，一个用来绘制水平高斯模糊，一个用来绘制垂直高斯模糊。 接着看draw方法

```
复制代码JNIEXPORT void JNICALL
Java_com_skateboard_blurimageview_BlurImageViewRender_draw(JNIEnv *env, jobject thiz) {
    int isVertical = 0;
    bool isFirst = true;
    glUseProgram(program);
    for (int i = 0; i < 12; i++) {
        glBindFramebuffer(GL_FRAMEBUFFER, FBUFFERS[isVertical]);
        int isVerticalLocation = glGetUniformLocation(program, "isVertical");
        glUniform1i(isVerticalLocation, isVertical);
        if (isFirst) {
            glBindTexture(GL_TEXTURE_2D, texture);
            isFirst = false;
        } else {
            glBindTexture(GL_TEXTURE_2D, FBUFFERTEXTURE[!isVertical]);
        }
        glBindVertexArray(VAO);
        int modelLocation = glGetUniformLocation(program, "model");
        glm::mat4 modelMatrix = glm::mat4(1.0f);
        modelMatrix = glm::rotate(modelMatrix, glm::radians(180.0f), glm::vec3(0.0, 0.0, 1.0));
        glUniformMatrix4fv(modelLocation, 1, GL_FALSE, &modelMatrix[0][0]);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        isVertical = !isVertical;
    }
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(1.0, 1.0, 1.0, 1.0);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glBindVertexArray(VAO);
    setMatrix();
    glBindTexture(GL_TEXTURE_2D, FBUFFERTEXTURE[0]);
    glDrawArrays(GL_TRIANGLES, 0, 6);
    glBindVertexArray(0);
}
```

可以发现已一共做了12次高斯模糊（6次水平6次垂直），次数越多模糊程度越大。 其他相关代码就是opengles的一些基础，比如生成program，生成纹理等，就不说了。

# 3.最后

最后附上源码地址 [github](https://link.juejin.cn/?target=https://github.com/skateboard1991/BlurImageView)



![img](D:\my-note\opengl\assets\16575efc5ee9d40ftplv-t2oaga2asx-jj-mark3024000q75.png)



关注我的公众号