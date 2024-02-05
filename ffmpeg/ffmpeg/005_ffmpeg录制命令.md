## ffmpeg录制命令

### 1. 录屏命令
ffmpeg -f [src] -i [device] -r [frame_rate] [out_file]
-f：设置采集数据的库
-i：指令从哪儿采集数据，参数是一个文件索引号
-r：指定帧率
out.yuv 输出文件，格式为yuv， yuv格式是一种原始格式数据
如：
ffmpeg -video_size 1024x768 -framerate 25 -f x11grab -i :0.0+100,200 output.mp4

ffmpeg -f x11grab -i :0.0+0,0 -s 1920x1080 -r 25 -vcodec rawvideo -pix_fmt yuv420p test.mkv

### 2.录音命令
ubuntu下用ffmpeg命令可以录制视频文件和音频文件，其中录制音频文件很简单，其基本格式为：

ffmpeg -f alsa <input_options> -i <input_device> .. output.wav

其中，ffmpeg命令的主要参数有： 
-i 设定输入流 
-f 设定输出格式 
-ss 开始时间

关于音频的参数主要有： 
-ar 设定采样率 
-ac 设定声音的channel

可以使用arecord -l命令来查看你自己电脑上的音频输入设备，例如我的电脑：

arecord -l

**** List of CAPTURE Hardware Devices ****
card 0: PCH [HDA Intel PCH], device 0: ALC892 Analog [ALC892 Analog]
  Subdevices: 1/1
  Subdevice #0: subdevice #0
card 0: PCH [HDA Intel PCH], device 2: ALC892 Alt Analog [ALC892 Alt Analog]
  Subdevices: 1/1
  Subdevice #0: subdevice #0

其中card只有card0, card0下device有0和2

表示设备可以用hw后面加参数表示，其含义如下：

 hw:<X>,<Y>, where <X>=card, <Y>=device. 

所以我的音频输入设备可以用hw:0,0和hw:0,2表示。
用命令

ffmpeg -f alsa -ar 16000 -i hw:1,0 out.wav

就可以录制一个alsa格式，采样率为16000，名称为 out.wav 文件。

