## 图片与视频互转命令

### 1. 视频转图片

ffmpeg -i in.flv -r 1 -f image2 image-%3d.jpeg

-i 输入
-r 帧率，参数为1表示每秒转换1张图片
-f 要转换成的格式 image2是image的第二版的格式

image-%3d.jpeg 输出文件，注意不能写成.jpg


### 2. 图片转视频

ffmpeg -i image-%003d.jpeg out.mp4

-i 输入的图片
