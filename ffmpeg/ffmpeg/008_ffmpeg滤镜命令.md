## ffmpeg滤镜命令

滤镜命令可以用来实现各种效果，比如 加水印、去水印，画中画，视频裁剪，音频倍速等

### ffmpeg处理音视频流程

输入文件 == demuxer ==> 编码数据包 == decoder ==> 解码后的数据帧

== encoder ==> 编码数据帧 == muxer ==> 输出文件

拿到解码后的数据后可以进行各种处理，所有的滤镜处理都是对解码后的数据进行的

### 滤镜处理流程

Decoded Frames == filter ==> Filtered Frames == encoder ==> Encoded Data


### 滤镜命令

#### 1.视频裁剪
使用crop滤镜：

ffmpeg -i in.mov -vf crop=in_w-200:in_h-200 -c:v libx264 -c:a copy out.mp4

-i 输入文件
-vf 视频滤镜
    crop 使用crop滤镜，每个滤镜有不同的参数，crop的参数格式为crop=out_w:out_h:x:y
    out_w 输出视频宽度
    out_h 输出视频高度
    x 裁剪区左上角x坐标
    y 裁剪区左上角y坐标

    这里是in_w-200:in_h-200
    in_w视频本身的宽度， in_h视频本身的高度，in_w-200:in_h-200就是宽高各减去200
    裁剪区默认为原视频的中心，也可以按照如下方式设置：
    crop=in_w/2:in_h/2:in_w/2:in_h/2 截取右下角1/4区
    crop=in_w/2:in_h/2:0:0 截取左上角1/4区

-c:v 使用的视频的编码器，这里使用 x264

-c:a 使用的音频编码器，copy即为复制，不处理


也可以使用音频滤镜，需要通过 -af 指定音频滤镜参数

### 2.视频添加水印
ffmpeg -i 01.mp4 -vf "movie=01.png[watermark];[in][watermark] overlay=10:10[out]" -c:v libx264 -c:a copy  out.mp4

-i 输入文件
-vf 视频滤镜，这里使用加水印滤镜 movie,
    movie参数：01.png[watermark];[in][watermark] overlay=10:10[out]
    01.png 水印文件
    
    overlay值
    左上角 10:10
    右上角 main_w-overlay_w-10:10
    左下角 10:main_h-overlay_h-10
    右下角 main_w-overlay_w-10 : main_h-overlay_h-10
    
    其中可以使用视频和水印的参数：
    main_w 视频单帧图像宽度
    main_h 视频单帧图像高度
    overlay_w 水印图片的宽度
    overlay_h 水印图片的高度

注意这里要加上 -c:v libx264 -c:a copy 设置编码格式没有的话会导致编码格式设置为默认格式导致视频画面变差
加水印后的编码格式默认为音频编码格式adpcm_swf，视频编码flv1
