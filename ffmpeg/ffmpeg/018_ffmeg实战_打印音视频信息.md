## 实战：打印音视频信息

### 相关API

```cpp

// 注册协议，所有ffmpeg程序启动前都要调用
av_register_all()

// 打开、关闭多媒体文件
avformat_open_input()/avformat_close_input()

// 打印多媒体信息
av_dump_format()

```

示例代码：

```cpp

extern "C" {
#include <stdint.h>
#include <libavutil/log.h>
#include <libavformat/avformat.h>
}

int main(int argc, char **argv) {
    av_log_set_level(AV_LOG_INFO);
    av_register_all();

    int ret;

    AVFormatContext* context = NULL;

    const char *path = "01.mp4";

    ret = avformat_open_input(&context, path, NULL, NULL);
    if(ret<0){
        av_log(NULL, AV_LOG_ERROR, "cannot open %s", path);
        return -1;
    }

    av_dump_format(context, 0, path, 0);

    avformat_close_input(&context);

    return ret;
}

```