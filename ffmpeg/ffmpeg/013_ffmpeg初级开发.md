## ffmpeg初级开发

### 学习内容
1. ffmpeg日志的使用及目录操作
2. ffmpeg的基本概念及常用的结构体
3. 复用/解复用及流操作

### ffmpeg代码结构
libavcodec 提供一系列编码器的实现
libavformat 实现在流协议，容器格式及其本IO访问
libavutil 包括hash器，解码器和各种工具函数
libavfilter 提供了各种音视频过滤器
libavdevice 提供了访问捕获设备和回放设备的接口
libswresample 实现了混音和重采样
libswscale 实现了色彩转换和缩放功能

