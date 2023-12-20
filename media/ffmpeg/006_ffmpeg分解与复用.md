## ffmpeg分解与复用

输入文件 ==demuxer==> 编码数据包 ==muxer==>输出文件

### 1. 多媒体格式转换

ffmpeg -i 01.mp4 -vcodec copy -acodec copy out.flv

-i 输入文件
-vcodec 视频编码处理方式 copy 复制，不做其他处理
-acodec 音频编码处理方式 copy 复制，不做其他处理

提取的时候不要音频或者视频，可以使用下面的参数：
-an 不使用音频编码器，输出的文件中没有音频
-vn 不使用视频编码器，输出的文件中没有视频

