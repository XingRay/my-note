# NDK OpenGL ES 3.0 开发（十四）：粒子（Particles）

**OpenGL ES 粒子（Particles）**

**
**

![图片](assets/014_粒子（Particles）/640.gif)

粒子爆炸

# 

[NDK OpenGL ES 3.0 开发（十三）：](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654161641&idx=1&sn=ba1e1c329fa0ae973b09345bc55ca9ff&chksm=8cf399dabb8410ccd5541d25b22d44eeac29abadf2526d19514628cb335c0ccf43de97f70519&scene=21#wechat_redirect)[实例化（Instancing）](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654161641&idx=1&sn=ba1e1c329fa0ae973b09345bc55ca9ff&chksm=8cf399dabb8410ccd5541d25b22d44eeac29abadf2526d19514628cb335c0ccf43de97f70519&scene=21#wechat_redirect)一文中我们了解到 OpenGL ES 实例化（Instancing）是一种只调用一次渲染函数就能绘制出很多物体的技术，可以实现将数据一次性发送给 GPU ，避免了 CPU 多次向 GPU 下达渲染命令，提升了渲染性能。

而**粒子系统本质上是通过一次或者多次渲染绘制出大量位置、形状或者颜色不同的物体（粒子），形成大量粒子运动的视觉效果。****所以，粒子系统天然适合用OpenGL ES 实例化（Instancing）实现。**

定义粒子，通常一个粒子有一个生命值，生命值结束该粒子消失，还有描述粒子在（x, y, z）三个方向的位置（偏移）和运动速度，以及粒子的颜色等属性。本文中粒子的定义：

```
struct Particle {
    GLfloat dx,dy,dz;//offset 控制粒子的位置
    GLfloat dxSpeed,dySpeed,dzSpeed;//speed 控制粒子的运动速度
    GLubyte r,g,b,a; //r,g,b,a 控制粒子的颜色
    GLfloat life; //控制粒子的生命值
};
```

渲染粒子需要用到的顶点着色器：

```
#version 300 es
precision mediump float;
layout(location = 0) in vec3 a_vertex;//顶点坐标
layout(location = 1) in vec2 a_texCoord;//纹理坐标 
layout(location = 2) in vec3 a_offset;//位置偏移
layout(location = 3) in vec4 a_particlesColor;//粒子颜色（照在粒子表面光的颜色）
uniform mat4 u_MVPMatrix;//变换矩阵
out vec2 v_texCoord;
out vec4 v_color;
void main()
{
    gl_Position = u_MVPMatrix * vec4(a_vertex - vec3(0.0, 0.95, 0.0) + a_offset, 1.0);
    // vec3(0.0, 0.95, 0.0) 是为了使粒子整体向 y 轴负方向有一个偏移
    v_texCoord = a_texCoord;
    v_color = a_particlesColor;
}
```

渲染粒子需要用到的片段着色器：

```
#version 300 es
precision mediump float;
in vec2 v_texCoord;
in vec4 v_color;
layout(location = 0) out vec4 outColor;
uniform sampler2D s_TextureMap;
void main()
{
    outColor = texture(s_TextureMap, v_texCoord) * v_color;
}
```

属性`a_offset`是粒子的位置偏移，最终确定粒子的位置，属性`a_particlesColor`表示照在粒子表面光的颜色，这两个属性均为实例化数组，因为每个粒子有不同的位置和颜色。

设置属性`a_offset`和 `a_particlesColor`为实例化数组：

```
glVertexAttribDivisor(0, 0);
glVertexAttribDivisor(1, 0);
glVertexAttribDivisor(2, 1);
glVertexAttribDivisor(3, 1);
```

`glVertexAttribDivisor(0, 0)`表示实例化绘制时对 index =0 的属性不更新；`glVertexAttribDivisor(2, 1)` 用于指定 index = 2 的属性为实例化数组，1 表示每绘制一个实例，更新一次数组中的元素。

因为每次实例化渲染粒子时，都要更新 `a_offset`和 `a_particlesColor`实例化数组，所以设置其对应的 VBO 为动态类型 `GL_DYNAMIC_DRAW` 。

```
glGenBuffers(1, &m_ParticlesPosVboId);
glBindBuffer(GL_ARRAY_BUFFER, m_ParticlesPosVboId);
// Initialize with empty (NULL) buffer : it will be updated later, each frame.
glBufferData(GL_ARRAY_BUFFER, MAX_PARTICLES * 3 * sizeof(GLfloat), NULL, GL_DYNAMIC_DRAW);

glGenBuffers(1, &m_ParticlesColorVboId);
glBindBuffer(GL_ARRAY_BUFFER, m_ParticlesColorVboId);
// Initialize with empty (NULL) buffer : it will be updated later, each frame.
glBufferData(GL_ARRAY_BUFFER, MAX_PARTICLES * 4 * sizeof(GLubyte), NULL, GL_DYNAMIC_DRAW);
```

新粒子的速度、偏移以及颜色都是随机生成的，生成新粒子的实现为：

```
void ParticlesSample::Init()
{
    for (int i = 0; i < MAX_PARTICLES; i++)
    {
        GenerateNewParticle(m_ParticlesContainer[i]);
    }
}

void ParticlesSample::GenerateNewParticle(Particle &particle)
{
    particle.life = 5.0f;
    particle.cameraDistance = -1.0f;
    particle.dx = (rand() % 2000 - 1000.0f) / 3000.0f;
    particle.dy = (rand() % 2000 - 1000.0f) / 3000.0f;
    particle.dz = (rand() % 2000 - 1000.0f) / 3000.0f;

    float spread = 1.5f;

    glm::vec3 maindir = glm::vec3(0.0f, 2.0f, 0.0f);
    glm::vec3 randomdir = glm::vec3(
            (rand() % 2000 - 1000.0f) / 1000.0f,
            (rand() % 2000 - 1000.0f) / 1000.0f,
            (rand() % 2000 - 1000.0f) / 1000.0f
    );

    glm::vec3 speed = maindir + randomdir * spread;
    particle.dxSpeed = speed.x;
    particle.dySpeed = speed.y;
    particle.dzSpeed = speed.z;

    particle.r = static_cast<unsigned char>(rand() % 256);
    particle.g = static_cast<unsigned char>(rand() % 256);
    particle.b = static_cast<unsigned char>(rand() % 256);
    particle.a = static_cast<unsigned char>((rand() % 256) / 3);

}
```

查找生命值结束的粒子：

```
int ParticlesSample::FindUnusedParticle()
{
    for (int i = m_LastUsedParticle; i < MAX_PARTICLES; i++)
    {
        if (m_ParticlesContainer[i].life <= 0)
        {
            m_LastUsedParticle = i;
            return i;
        }
    }

    for (int i = 0; i < m_LastUsedParticle; i++)
    {
        if (m_ParticlesContainer[i].life <= 0)
        {
            m_LastUsedParticle = i;
            return i;
        }
    }
    return -1;
}
```

更新粒子(更新粒子的位置、运动速度和生命值)，然后更新实例化数组：

```
int ParticlesSample::UpdateParticles()
{
    LOGCATE("ParticlesSample::UpdateParticles");

    //每次生成 300 个新粒子，产生爆炸的效果
    int newParticles = 300;
    for (int i = 0; i < newParticles; i++)
    {
        int particleIndex = FindUnusedParticle();
        if (particleIndex >= 0)
        {
            GenerateNewParticle(m_ParticlesContainer[particleIndex]);
        }
    }

    int particlesCount = 0;
    for (int i = 0; i < MAX_PARTICLES; i++)
    {

        Particle &p = m_ParticlesContainer[i]; // shortcut
        //生命值大于 0 的粒子进行更新
        if (p.life > 0.0f)
        {
            float delta = 0.1f;
            glm::vec3 speed = glm::vec3(p.dxSpeed, p.dySpeed, p.dzSpeed), pos = glm::vec3(p.dx,
                                                                                          p.dy,
                                                                                          p.dz);
            //更新粒子生命值
            p.life -= delta;
            if (p.life > 0.0f)
            {
                //更新粒子速度
                speed += glm::vec3(0.0f, 0.081f, 0.0f) * delta * 0.5f;
                pos += speed * delta;

                p.dxSpeed = speed.x;
                p.dySpeed = speed.y;
                p.dzSpeed = speed.z;

                //更新粒子位置
                p.dx = pos.x;
                p.dy = pos.y;
                p.dz = pos.z;

                m_pParticlesPosData[3 * particlesCount + 0] = p.dx;
                m_pParticlesPosData[3 * particlesCount + 1] = p.dy;
                m_pParticlesPosData[3 * particlesCount + 2] = p.dz;
                //不更新粒子的颜色
                m_pParticlesColorData[4 * particlesCount + 0] = p.r;
                m_pParticlesColorData[4 * particlesCount + 1] = p.g;
                m_pParticlesColorData[4 * particlesCount + 2] = p.b;
                m_pParticlesColorData[4 * particlesCount + 3] = p.a;

            }
            particlesCount++;

        }
    }

    //更新实例化数组
    glBindBuffer(GL_ARRAY_BUFFER, m_ParticlesPosVboId);
    glBufferData(GL_ARRAY_BUFFER, MAX_PARTICLES * 3 * sizeof(GLfloat), NULL,
                 GL_DYNAMIC_DRAW); // Buffer orphaning, a common way to improve streaming perf. See above link for details.
    GO_CHECK_GL_ERROR();
    glBufferSubData(GL_ARRAY_BUFFER, 0, particlesCount * sizeof(GLfloat) * 3, m_pParticlesPosData);
    GO_CHECK_GL_ERROR();

    glBindBuffer(GL_ARRAY_BUFFER, m_ParticlesColorVboId);
    glBufferData(GL_ARRAY_BUFFER, MAX_PARTICLES * 4 * sizeof(GLubyte), NULL,
                 GL_DYNAMIC_DRAW); // Buffer orphaning, a common way to improve streaming perf. See above link for details.
    glBufferSubData(GL_ARRAY_BUFFER, 0, particlesCount * sizeof(GLubyte) * 4,
                    m_pParticlesColorData);
    return particlesCount;
}
```

每次绘制时，先获取生命值大于 0 粒子的数量再进行绘制：

```
void ParticlesSample::Draw(int screenW, int screenH)
{
    LOGCATE("ParticlesSample::Draw()");
    if (m_ProgramObj == GL_NONE || m_TextureId == GL_NONE) return;
    glEnable(GL_DEPTH_TEST);
    glClear(GL_STENCIL_BUFFER_BIT | GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glClearColor(1.0, 1.0, 1.0, 1.0);
    glDisable(GL_BLEND);

    UpdateMVPMatrix(m_MVPMatrix, m_AngleX, m_AngleY, (float) screenW / screenH);

    //每次获取生命值大于 0 粒子的数量
    int particleCount = UpdateParticles();

    // Use the program object
    glUseProgram(m_ProgramObj);

    glBindVertexArray(m_VaoId);
    glUniformMatrix4fv(m_MVPMatLoc, 1, GL_FALSE, &m_MVPMatrix[0][0]);

    // Bind the RGBA map
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, m_TextureId);
    glUniform1i(m_SamplerLoc, 0);

    glDrawArraysInstanced(GL_TRIANGLES, 0, 36, particleCount);
}
```

**实现代码路径见阅读原文。**