## 裁剪与合并命令

### 1.音视频裁剪

ffmpeg -i in.mp4 -ss 00:00:00 -t 10 out.ts

-i 输入文件
-s 起始时间 格式为hh:mm:ss
-t 裁剪的时长，单位为秒


### 2. 音视频合并

ffmpeg -f concat -i input.txt out.flv

-f contact 表示要合并后面的文件
-i 输入， input.txt包含制定格式的文件列表
    input.txt内每一行代表一个单独的文件，格式为:
    file filename
    如：
    file '1.mp4'
    file '2.mp4'


