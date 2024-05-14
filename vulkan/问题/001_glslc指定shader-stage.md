# glslc 指定 shader-stage



在使用glslc编译shader, 指令如下: 

```
glslc shader/vertex.glsl -o shader/vertex.spv 
```

运行报错: glsl file encountered but no -fshader-stage specified ahead



遇到的错误是由于 `glslc` 不能自动识别你的 GLSL 文件的类型（如顶点着色器、片段着色器等）。需要明确地告诉 `glslc` 你的 GLSL 文件的类型。

可以通过在命令中添加 `-fshader-stage` 参数来指定着色器阶段。例如，如果正在编译一个顶点着色器，可以这样写：

```
glslc -fshader-stage=vertex shader/vertex.glsl -o shader/vertex.spv
```

对于其他类型的着色器，您可以使用相应的阶段名称：

- 顶点着色器：`vertex`
- 片段着色器：`fragment`
- 几何着色器：`geometry`
- 计算着色器：`compute`
- 曲面细分控制着色器：`tesscontrol`
- 曲面细分评估着色器：`tesseval`

举例，如果有一个片段着色器，可以这样编译：

```
glslc -fshader-stage=fragment shader/fragment.glsl -o shader/fragment.spv
```

通过明确指定 `-fshader-stage` 参数，应该能够成功编译你的 GLSL 着色器文件。