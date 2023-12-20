## ffmpeg日志系统

相关头文件和函数

```c

#include <libavutil/log.h>

av_log_set_level(AV_LOG_DEBUG)

av_log(NULL, AV_LOG_INFO, "...%s", op)

```

av_log_set_level
设置日志级别，常用日志级别有：
AV_LOG_ERROR
AV_LOG_WARNING
AV_LOG_INFO
AV_LOG_DEBUG

av_log
输出日志，第一个参数设置为NULL
