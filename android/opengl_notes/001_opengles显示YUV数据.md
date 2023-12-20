# opengles显示YUV数据



工程文件名为：com.example.threetextureyuv

1、yuv回顾
1）yuv的由来
是在保持图片质量的前提下降低图片内存大小提供传输效率，并且传统的BGR格式 对于黑白图片不支持亮度的调节。

Y”表示明亮度（Luminance、Luma），“U”和“V”则是色度、浓度。

一般RGB用于渲染，YUV用于传输。

2）介绍多种YUV格式
多种YUV格式都是从采样格式和存储格式进行分析了解的。

采样格式主要分为YUV4:4:4，YUV4:2:2，YUV4:2:0。

YUV4:4:4：完全采用表示每个像素点都有一个Y,U,V。一个YUV占 8+8+8 = 24bits,3个字节。

YUV4:2:2: 就是2:1的水平取样，垂直完全采样，表示水平的两个像素有两个Y但是只有一个U一个V的采用格式。一个YUV占 8+4+4 = 16bits 2个字节。

YUV4:2:0：就是2:1的水平取样，2:1的垂直采样，表示上下左右四个像素点有4个Y但是只取一个U和一个V，一个YUV占 8+2+2 = 12bits 1.5个字节

[外链图片转存失败,源站可能有防盗链机制,建议将图片保存下来直接上传(img-YbzsyGnW-1686050027461)(C:\Users\CreatWall_zhouwen\Desktop\pic\pic\yuv.jpg)]

存储格式：粗略分为planar和packed。

planar的YUV格式表示先连续存储所有像素点的Y，再紧接着存储所有的U，再就是V。Y、U和V组件存储为三个独立的数组中。

packed的YUV格式表示每个像素点的YUV是连续交替存储的，先存储像素点1的YUV再存在像素点2的YUV像素点。Y、U和V组件存储在一个数组中。每个像素点的Y,U,V是连续交错存储的

里面还存在小的分支就是UV的顺序反过来，先V再U这种。

具体类型

YUV420P



YU12：安卓的模式。存储顺序是先存Y，再存U，最后存V。YYYYUUUVVV

YV12：存储顺序是先存Y，再存V，最后存U。YYYVVVUUU

YUV420SP



NV12 和 NV21 格式都属于YUV420SP类型。

NV12 是 IOS 中有的模式，它的存储顺序是先存 Y 分量，再 UV 进行交替存储。

NV21 是 安卓 中有的模式，它的存储顺序是先存 Y 分量，在 VU 交替存储。

3)YUV与RGB的转换
yuv转RGB

可以使用YUV矩阵乘以以下矩阵则可以得到对于的RGB矩阵

(1.0, 1.0, 1.0, //第一列

0.0，-0.338，1.732， //第二列

1.371，-0.698， 0.0)

还可以计算

B = 1.164(Y - 16) + 2.018(U - 128)
G = 1.164(Y - 16) - 0.813(V - 128) - 0.391(U - 128)
R = 1.164(Y - 16) + 1.596(V - 128)

RGB转YUV

Y = (0.257 * R) + (0.504 * G) + (0.098 * B) + 16
V = (0.439 * R) - (0.368 * G) - (0.071 * B) + 128
U = -(0.148 * R) - (0.291 * G) + (0.439 * B) + 128

2、纹理升级使用
1）概念：
纹理我们理解的都是将一张图片“贴到”你想要的位置，那么他是如何贴的，其实归根到底还是对图片进行采用，再将采用到的颜色绘制到对应的顶点位置。

attritude：一般用于各个顶点各不相同的量。如顶点位置、纹理坐标、法向量、颜色等等。
uniform：一般用于对于物体中所有顶点或者所有的片段都相同的量。着色器中的常量值，在链接阶段，链接器将分配常量在项目里的实际地址，那个地址是被应用程序使用和加载的标识。比如光源位置、统一变换矩阵、颜色等。
varying：表示易变量，一般用于顶点着色器传递到片段着色器的量。
sampler1D：1D纹理着色器
sampler2D：2D纹理着色器
sampler3D：3D纹理着色器

2）如何进行映射
顶点坐标和纹理坐标的对应，我们需要指定图形的每个顶点各自对应纹理的哪个部分。所以图形的每个顶点都会关联一个纹理的坐标，用来标明该从纹理图像的哪个部分采样。

3)纹理选择
[外链图片转存失败,源站可能有防盗链机制,建议将图片保存下来直接上传(img-6LvG45bz-1686050027468)(C:\Users\CreatWall_zhouwen\Desktop\pic\pic\texture.png)]

L是表示亮度，A表示透明度。

yuv420p：yuv的数据分开的，uv的纹理大小与y不同，因此创建三个纹理： y、u、v，并且都是单通道GL_LUMINANCE：表示 灰度图，单通道

nv21：因为uv的数据是柔和在一起的，因此创建2个纹理：y， u，创建y的时候format选择GL_LUMINANCE，而创建uv的format为GL_LUMINANCE_ALPHA ：表示的是带 alpha通道的灰度图，即有2个通道，包含r和 a。

3、实践
1）准备数据
使用ffmpeg将图片转换为对应的YUV数据

ffmpeg -i awesomeface.png -s 512x512 -pix_fmt nv21 awesomeface1.yuv

ffmpeg -i awesomeface.png -s 512x512 -pix_fmt yuv420p awesomeface.yuv

nv21是YUV420SP格式的先存 Y 分量，在 VU 交替存储。

yuv420p 为先存 Y 分量，再U再V顺序存储。

2）显示NV21格式的YUV图片
[1]、获取解析NV21格式数据
用来管理图片的YUV数据，目前是NV21格式那么会使用到 ppPlane[0]存储Y数据，ppPlane[1]存储UV数据。

```
struct NativeImage
{
    int width;
    int height;
    int format;
    uint8_t *ppPlane[3];

    NativeImage()
    {
        width = 0;
        height = 0;
        format = 0;
        ppPlane[0] = nullptr;
        ppPlane[1] = nullptr;
        ppPlane[2] = nullptr;
    }
};

```

通过AAsset读取AAsset下的资源文件

```
unsigned char *LoadFileContent(char *filepath) {
    // read shader code form asset
    unsigned char *fileContent = nullptr;
    AAsset *asset = AAssetManager_open(g_mrg, filepath, AASSET_MODE_UNKNOWN);
    if (asset == nullptr) {

        LOGD("LoadFileContent asset is null, load shader error ");
    }
    int filesSize_v = AAsset_getLength(asset);
    fileContent = new unsigned char[filesSize_v];
    AAsset_read(asset, fileContent, filesSize_v);
    fileContent[filesSize_v] = '\0';
    AAsset_close(asset);
    LOGD("LoadFileContent asset is %s", fileContent);
    return fileContent;
}
```

最后直接调用赋值，就将YUV数据绑定到自己类中，暂时没有考虑申请内存，还是使用AAsset读取时创建的内存。

```
void TextureYUV::getTextureYUV(int format, int width, int height, uint8_t *pData) {
    m_RenderImage.format = format;
    m_RenderImage.width = width;
    m_RenderImage.height = height;
    m_RenderImage.ppPlane[0] = pData;
    m_RenderImage.ppPlane[1] = m_RenderImage.ppPlane[0] + width * height;
}
```

[2]、着色器的编写
顶点着色器

```
#version 300 es
layout (location = 0) in vec4 vPosition;
layout (location = 1) in vec2 aTexCoord;

out vec2 v_texCoord;

void main()
{
    gl_Position = vPosition;
    v_texCoord = aTexCoord;
}
```

片段着色器

```
#version 300 es
precision mediump float;
in vec2 v_texCoord;//由顶点着色器传来的纹理坐标
out vec4 outColor;

//sampler2D就是表示纹理单元类型的 在调用glDrawElements之前绑定纹理了，它会自动把纹理赋值给片段着色器的采样器：
//uniform 表示常量，在应用程序中也能通过相应函数进行获取
uniform sampler2D y_texture;
uniform sampler2D uv_texture;

void main()
{
    vec3 yuv;
    //texture 是OpenGL ES内置函数，称之为采样器，获取纹理上指定位置的颜色值。
    //y_texture  绑定定义的纹理
    //v_texCoord  由顶点坐标那边传来的纹理坐标
    yuv.x = texture(y_texture, v_texCoord).r;
    // shader 会将数据归一化，而 uv 的取值区间本身存在-128到正128 然后归一化到0-1 为了正确计算成rgb，则需要归一化到 -0.5 - 0.5的区间
    //GL_LUMINANCE_ALPHA ：表示的是带 alpha通道的灰度图，即有2个通道，包含r和 a
    yuv.y = texture(uv_texture, v_texCoord).a-0.5;
    yuv.z = texture(uv_texture, v_texCoord).r-0.5;
    //YUV转RGB的矩阵
    highp vec3 rgb = mat3( 1,       1,         1,
                     0,        -0.344,    1.770,
                     1.403,  -0.714,       0) * yuv;
    //最后输出颜色
    outColor = vec4(rgb, 1);
}
```

w为什么要减0.5，因为归一化。YUV 格式图像 UV 分量的默认值分别是 127 ，Y 分量默认值是 0 ，8 个 bit 位的取值范围是 0 ~ 255，由于在 shader 中纹理采样值需要进行归一化，所以 UV 分量的采样值需要分别减去 0.5 ，确保 YUV 到 RGB 正确转换。

[3]、绘画类编写
方式一、使用VAO，VBO
这样就是定义一个顶点数组，有5维的。

```
float vertices[] = {
        //---- 位置 ----    - 纹理坐标 -
        -1.0f,  0.78f, 0.0f,    0.0f,  0.0f,   // 右上
        -1.0f, -0.78f, 0.0f,   0.0f,  1.0f,   // 右下
        1.0f,  -0.78f, 0.0f,  1.0f,  1.0f,  // 左下
        1.0f,   0.78f, 0.0f,  1.0f,  0.0f    // 左上
};
//索引数组
unsigned int indices[] = {
        0, 1, 2, 0, 2, 3
};
```

在创建程序中同样跟之前程序一致使用VAO，

```
void TextureYUV::CreateProgram(const char *ver, const char *frag) {
	/* -----着色器和程序------ */
	//创建顶点，片段着色器编译并附加到程序链接program
	//.
	//.
	//.
	//.
	
	/* -----顶点相关------ */
	glGenVertexArrays(1, &VAO);
    //创建顶点缓冲区
    glGenBuffers(1, &VBO);
    //绑定顶点数组
    glBindVertexArray(VAO);
    //将顶点缓冲区绑定为GL_ARRAY_BUFFER
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    //将定义的顶点数组绑定到GL_ARRAY_BUFFER这个缓存中，这个缓存对应顶点缓冲区数据
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    /* -----绑定纹理相关------ */
    //将GL_ELEMENT_ARRAY_BUFFER缓冲区绑定到EBO
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
    //将索引坐标绑定到GL_ELEMENT_ARRAY_BUFFER
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(indices), indices, GL_STATIC_DRAW);

    //指定配置的顶点属性，第一个index参数对应顶点着色器的location值
    glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 5*sizeof(float ), (void *)0);
    //启用顶点属性
    glEnableVertexAttribArray(0);

    //纹理属性绑定
    glVertexAttribPointer(1, 2, GL_FLOAT, GL_FALSE, 5*sizeof(float ), (void *)(3*sizeof(float)));
    glEnableVertexAttribArray(1);

    //将顶点数组和顶点缓冲区置空则不让其他修改
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
    
     /* -----纹理相关------ */
	//获取Uniform的位置
    m_ySamplerLoc = glGetUniformLocation (program, "y_texture" );
    m_uvSamplerLoc = glGetUniformLocation(program, "uv_texture");
    //创建纹理
    GLuint textureIds[2] = {0};
    glGenTextures(2, textureIds);
    m_yTextureId = textureIds[0];
    m_uvTextureId = textureIds[1];

    //绑定设置纹理 m_yTextureId
    glBindTexture(GL_TEXTURE_2D, m_yTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, m_RenderImage.width, m_RenderImage.height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, m_RenderImage.ppPlane[0]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
    //绑定设置纹理 m_uvTextureId
    glBindTexture(GL_TEXTURE_2D, m_uvTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE_ALPHA, m_RenderImage.width >> 1, m_RenderImage.height >> 1, 0, GL_LUMINANCE_ALPHA, GL_UNSIGNED_BYTE, m_RenderImage.ppPlane[1]);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glBindTexture(GL_TEXTURE_2D, GL_NONE);
}
```



绘画时也是与之前一致绑定VAO再绘画

```
void TextureYUV::Draw() {
    glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(0.2f, 0.9f, 0.3f, 1.0f);

    //指定使用的着色器程序
    glUseProgram(program);

    // 绑定Y数据纹理、设置sampler2D y_texture纹理为第0个纹理
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, m_yTextureId);
    glUniform1i(m_ySamplerLoc, 0);

    // // 绑定Y数据纹理、设置sampler2D uv_texture纹理为第1个纹理
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, m_uvTextureId);
    glUniform1i(m_uvSamplerLoc, 1);

    //绑定顶点数组   如果使用绑定顶点形式则需要绑定顶点数组
    glBindVertexArray(VAO);
    //画纹理
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, indices);
    glBindVertexArray(0);
    //glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices);
}
```

方式二、不使用VBO
顶点定义则是分开的，顶点坐标和纹理坐标

```
GLfloat verticesCoords[] = {
        -1.0f,  0.78f, 0.0f,  // Position 0
        -1.0f, -0.78f, 0.0f,  // Position 1
        1.0f,  -0.78f, 0.0f,  // Position 2
        1.0f,   0.78f, 0.0f,  // Position 3
};

GLfloat textureCoords[] = {
        0.0f,  0.0f,        // TexCoord 0
        0.0f,  1.0f,        // TexCoord 1
        1.0f,  1.0f,        // TexCoord 2
        1.0f,  0.0f         // TexCoord 3
};
GLushort indices[] = { 0, 1, 2, 0, 2, 3 };
```

在程序创建也有部分不一样，没有在创建VAO这些了

```
void TextureYUV::CreateProgram(const char *ver, const char *frag) {
	/* -----着色器和程序------ */
	//创建顶点，片段着色器编译并附加到程序链接program
	//.
	//.
	//.
	//.
	
	/* -----顶点相关------ */
	// 加载顶点坐标
    glVertexAttribPointer (0, 3, GL_FLOAT,GL_FALSE, 3 * sizeof (GLfloat), verticesCoords);
    // 加载纹理坐标
    glVertexAttribPointer (1, 2, GL_FLOAT,GL_FALSE, 2 * sizeof (GLfloat), textureCoords);
    glEnableVertexAttribArray (0);
    glEnableVertexAttribArray (1);
    
     /* -----纹理相关------ */
     //纹理使用与方法一一致的
}
```

绘画时也有一点不一致，没有VAO顶点缓冲区了，直接绘画

```
void TextureYUV::Draw() {
    glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(0.2f, 0.9f, 0.3f, 1.0f);

    //指定使用的着色器程序
    glUseProgram(program);

    // 绑定Y数据纹理、设置sampler2D y_texture纹理为第0个纹理
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, m_yTextureId);
    glUniform1i(m_ySamplerLoc, 0);

    // // 绑定Y数据纹理、设置sampler2D uv_texture纹理为第1个纹理
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, m_uvTextureId);
    glUniform1i(m_uvSamplerLoc, 1);

    //画纹理
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices);
}
```

3）显示YUV420P格式的YUV图片
因为存储数据的不一致，YUV420P他的YUV数据都是全部分开的，那么对应纹理就会采用三个GL_LUMINANCE纹理。而之前NV21他的UV数据交叉在一起的所以使用的时GL_LUMINANCE纹理存Y数据，GL_LUMINANCE_ALPHA纹理存UV数据对应r,a的维。

所以在编写opengles代码的时候就会有三处地方有区别，片段着色器会多引入一个纹理，在程序中创建绑定纹理时需多创建一个并且是GL_LUMINANCE格式的，在绘画的时候也需要激活三层纹理。

[1]、解析YUV420P的数据

```
void TextureYUV::getTextureYUV(int format, int width, int height, uint8_t *pData) {
    m_RenderImage.format = format;
    m_RenderImage.width = width;
    m_RenderImage.height = height;
    if(format == 0)//NV21
    {
        m_RenderImage.ppPlane[0] = pData;
        m_RenderImage.ppPlane[1] = m_RenderImage.ppPlane[0] + width * height;
    }
    else
    {
        m_RenderImage.ppPlane[0] = pData;
        //U存储的地址就是首地址+Y的数据长度
        m_RenderImage.ppPlane[1] = m_RenderImage.ppPlane[0] + width * height;
        //V存储的地址就是U存储的地址+U的数据长度
        m_RenderImage.ppPlane[2] = m_RenderImage.ppPlane[1] + width * height/4;
    }
}
```

[2]、片段着色器的编写

```
#version 300 es
precision mediump float;
in vec2 v_texCoord;//由顶点着色器传来的纹理坐标
out vec4 outColor;

//sampler2D就是表示纹理单元类型的 在调用glDrawElements之前绑定纹理了，它会自动把纹理赋值给片段着色器的采样器：
//uniform 表示常量，在应用程序中也能通过相应函数进行获取
uniform sampler2D y_texture;
uniform sampler2D u_texture;
uniform sampler2D v_texture;
void main()
{
    vec3 yuv;
    //texture 是OpenGL ES内置函数，称之为采样器，获取纹理上指定位置的颜色值。
    //y_texture  绑定定义的纹理
    //v_texCoord  由顶点坐标那边传来的纹理坐标
    yuv.x = texture(y_texture, v_texCoord).r;
    // shader 会将数据归一化，而 uv 的取值区间本身存在-128到正128 然后归一化到0-1 为了正确计算成rgb，则需要归一化到 -0.5 - 0.5的区间
    //GL_LUMINANCE_ALPHA ：表示的是带 alpha通道的灰度图，即有2个通道，包含r和 a
    yuv.y = texture(u_texture, v_texCoord).r-0.5;
    yuv.z = texture(v_texture, v_texCoord).r-0.5;
    //YUV转RGB的矩阵
    highp vec3 rgb = mat3( 1,       1,         1,
                     0,        -0.344,    1.770,
                     1.403,  -0.714,       0) * yuv;
    //最后输出颜色
    outColor = vec4(rgb, 1);
}
```



[3]、绘画类的编写

```
/* -----纹理相关------ */
//获取Uniform的位置
m_ySamplerLoc = glGetUniformLocation (program, "y_texture" );
m_uSamplerLoc = glGetUniformLocation(program, "u_texture");
m_vSamplerLoc = glGetUniformLocation(program, "v_texture");
//创建纹理
GLuint textureIds[3] = {0};
glGenTextures(3, textureIds);
m_yTextureId = textureIds[0];
m_uTextureId = textureIds[1];
m_vTextureId = textureIds[2];

//绑定设置纹理 m_yTextureId
glBindTexture(GL_TEXTURE_2D, m_yTextureId);
glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, m_RenderImage.width, m_RenderImage.height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, m_RenderImage.ppPlane[0]);
glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
glBindTexture(GL_TEXTURE_2D, GL_NONE);
//绑定设置纹理 m_uTextureId
glBindTexture(GL_TEXTURE_2D, m_uTextureId);
glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, m_RenderImage.width >> 1, m_RenderImage.height >> 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, m_RenderImage.ppPlane[1]);
glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
glBindTexture(GL_TEXTURE_2D, GL_NONE);
//绑定设置纹理 m_vTextureId
glBindTexture(GL_TEXTURE_2D, m_vTextureId);
glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, m_RenderImage.width >> 1, m_RenderImage.height >> 1, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, m_RenderImage.ppPlane[2]);
glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
glBindTexture(GL_TEXTURE_2D, GL_NONE);
```

```
void TextureYUV::DrawYUV() {
    glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(0.2f, 0.9f, 0.3f, 1.0f);

    //指定使用的着色器程序
    glUseProgram(program);

    // 绑定Y数据纹理、设置sampler2D y_texture纹理为第0个纹理
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, m_yTextureId);
    glUniform1i(m_ySamplerLoc, 0);

    // // 绑定Y数据纹理、设置sampler2D uv_texture纹理为第1个纹理
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, m_uTextureId);
    glUniform1i(m_uSamplerLoc, 1);

    // // 绑定Y数据纹理、设置sampler2D uv_texture纹理为第1个纹理
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, m_vTextureId);
    glUniform1i(m_vSamplerLoc, 2);

    //绑定顶点数组   如果使用绑定顶点形式则需要绑定顶点数组
    glBindVertexArray(VAO);
    //画纹理
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, indices);
    glBindVertexArray(0);
    //glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, indices);
}
```



参考链接

ffmpeg支持的pix_format https://blog.csdn.net/Lixiaohua_video/article/details/77882396

音视频基础知识-YUV图像 https://mp.weixin.qq.com/s/YBj1mrX0CDkg3xrg9DXYvA

Android平台上基于OpenGl渲染yuv视频 https://blog.csdn.net/sinat_23092639/article/details/103046553

yuv格式介绍与opengl 显示 yuv数据 https://blog.csdn.net/zhangpengzp/article/details/89532590

OpenGL ES 3.0 开发（三）：YUV 渲染 https://blog.csdn.net/Kennethdroid/article/details/97153407


