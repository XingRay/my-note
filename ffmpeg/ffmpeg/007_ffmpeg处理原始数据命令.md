## ffmpeg处理原始数据

原始数据就是ffmpeg解码后的数据，音频就是PCM数据，视频就是YUV数据

### 1.提取YUV数据

ffmpeg -i input.mp4 -an -c:v rawvideo -pix_fmt yuv420p out.yuv

-i 输入文件
-an 不使用音频编码器，输出文件中没有音频部分
-c:v 对视频进行编码，参数 raw_video 指定为原始数据的方式编码
-pix_fmt 视频的像素格式，常用为yuv420p

** yuv 格式
yuv444
yuv422
yuv420

yuv420最常用

提取之后使用下面的命令来播放：

ffplay -s 1280x800 out.yuv

播放时需要通过-s 指定原始视频的尺寸，比如1280x800

### 2.提取PCM数据

ffmpeg -i input.mp4 -vn -ar 44100 -ac 2 f s16le out.pcm

-i 输入文件
-vn 不使用视频编码器，输出文件中没有视频部分
-ar audio rate 音频的采样率 这里使用44.1k，其他常用的还有48k，32k，16k
-ac audio channel 声道，有单声道、双声道还是立体声，ac2表示双声道-c:a
-f 抽取出的数据的格式，
    s16le 
    s：signed有符号的， 
    16 16位编码 
    le： litteEnd 小端数据 

抽取后可以使用一下命令进行播放：

ffplay -ar 48000 -ac 2 -f s16le out.pcm 

其中 -ar -ac -f 参数必须要指定，并且和抽取时的参数一致

 