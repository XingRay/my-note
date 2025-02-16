# Mediapipe源码分析



下面以分析 face_detector 的逻辑为例



## 1 环境搭建

要分析Mediapipe的代码逻辑, 需要先创建 Mediapipe 代码阅读环境, Mediapipe 使用bazel 编译, 但是目前bazel的插件没有可以完全正常使用的, 所以需要重新引入cmake对Mediapipe进行代码管理,

首先下载Mediapipe源码

```
git clone https://github.com/google-ai-edge/mediapipe
```

然后根据文档说明在windows平台编译Mediapipe, 如何编译Mediapipe见 [001_windows上编译mediapipe.md](./001_windows上编译mediapipe.md)

编译完成后, bazel 会下载项目依赖的其他第三方开源项目的依赖保存到 bazel-out 目录



在 Clion 中创建一个c++项目, 目录结构如下:

```
build
code
  src
    external
      com_google_absl
    mediapipe
      calculators
      framework
      tasks
      util
CMakeLists.txt
main.cpp
```

其中 external 保存依赖的第三方库, mediapipe 目录保存 Mediapipe 项目源码, 但是为了方便阅读进保留 c++的源代码和头文件, 以及 protobuf 编译产出的 c++源文件和头文件, 通常是如:

```
xxx.pb.cc
xxx.pb.h
```



在CMake 配置文件中简单引入所有源码即可:

```
cmake_minimum_required(VERSION 3.30)
project(mediapipe_core)

set(CMAKE_CXX_STANDARD 23)


# Main executable
file(GLOB PROJECT_SOURCES
        "code/src/*.c"
        "code/src/*.cpp"
        "code/src/*.cc"
)
add_executable(mediapipe_core main.cpp)

target_include_directories(${PROJECT_NAME} PUBLIC
        "code/include"
        "code/src"
        "code/src/external/com_google_absl"
)
```



这样clion就可以管理大部分源码, 可以正常跳转即可, 



## 2 face_detector 任务的预处理和后处理分析

首先 face_detector 的功能定义的头文件是

```
code/src/mediapipe/tasks/cc/vision/face_detector/face_detector.h
```

可以在对应的源文件

```
code/src/mediapipe/tasks/cc/vision/face_detector/face_detector.cc
```

中看到使用的计算图 Graph 的名称:

```
constexpr char kFaceDetectorGraphTypeName[] =
    "mediapipe.tasks.vision.face_detector.FaceDetectorGraph";
```

对应的文件是

```
code/src/mediapipe/tasks/cc/vision/face_detector/face_detector_graph.cc
```

构造子图的方法:

```
absl::StatusOr<FaceDetectionOuts> BuildFaceDetectionSubgraph(
      const FaceDetectorGraphOptions& subgraph_options,
      const core::ModelResources& model_resources, Source<Image> image_in,
      Source<NormalizedRect> norm_rect_in, Graph& graph) {
      ...
}
```

在这个方法中可以看到预处理使用了子图:

```
std::string GetImagePreprocessingGraphName() {
    return "mediapipe.tasks.components.processors.ImagePreprocessingGraph";
  }
```

对应的预处理子图源码

```
code/src/mediapipe/tasks/cc/components/processors/image_preprocessing_graph.cc
```

以及ssd anchor 算子

```
auto& ssd_anchor = graph.AddNode("SsdAnchorsCalculator");
```

对应源码:

```
code/src/mediapipe/calculators/tflite/ssd_anchors_calculator.cc
```

可以从这个源码中分析出构造anchors 的流程