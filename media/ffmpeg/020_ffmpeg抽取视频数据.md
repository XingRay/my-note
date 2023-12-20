## 抽取视频数据

### 基本概念

1. StartCode
每一帧前面的特征码，区分帧与帧之间的间隔

2. SPS/PPS
解码的视频参数，比如宽高（分辨率）、帧率
一般多媒体文件有一个就可以
视频分辨率发生变化时需要更新SPS/PPS
直播流中可能会丢数据，在每个关键帧前面增加SPS/PPS,数据量很小，不会增加网络负担

3. codec->extradata 获取 SPS/PPS

### 步骤

1. log级别 输入参数判断
2. register_all
3. avformat_alloc_contetx
4. av_guess_format
5. avformat_new_stream
6. avio_open
7. fopen(outPath)
8. avformat_open_input
9. avformat_find_stream_info
10. av_dump_format
11. av_frame_alloc
12. av_init_packet
13. av_find_best_stream
14. avformat_write_header
15. av_read_frame
16. ** h264_mp4toannexb 设置startCode SPS PPS **
17. av_packet_unref 


h264_mp4toannexb
AvPacket* in

1. out = av_packet_alloc
2. 帧头4字节是大小，要取出，大端数据
3. 第一个字节后5为是ANL单元的类型
SPS =7
PPS = 8
关键帧 = 5
非关键帧=1

4. 获取SPS和PPS
5. 获取StartCode
6. fwrite/fflush

### startCode
00000001 SPS PPS 特征码 4字节
000001 非SPS PPS 特征码 3字节

### SPS PPS
扩展数据前4字节无用
后第一个字节低2位是SPS PPS长度数据所需的字节数

后一个字节的低5为是SPS 和PPS的个数，一般只有一个 很少有多个

循环读取SPS和PPS


#### 示例代码（未完成）

```cpp

extern "C" {
#include <stdint.h>
#include <libavutil/log.h>
#include <libavformat/avformat.h>
}

#define ERROR_LEN 1024

void makeADTSHeader(uint8_t *header, int frameSize, int sampleRate, int channelNum);

int h264_mp4ToAnnexb(AVFormatContext *inputContext, AVPacket *in, FILE *outFile);

int h264ExtraDataToAnnexb(uint8_t *codecExtraData, int extraDataSize, AVPacket *outExtraData, int padding);

int alloc_and_copy(AVPacket *out, uint8_t *data, int size, const uint8_t *buff, uint32_t inSize);

int main(int argc, char **argv) {
    int ret;
    char errors[ERROR_LEN];

    FILE *outFile;
    AVFormatContext *inputContext = NULL;

    const char *inputPath = argv[1];
    const char *outPath = argv[2];

    av_log_set_level(AV_LOG_INFO);

    av_register_all();

    if (argc < 3) {
        av_log(NULL, AV_LOG_ERROR, "input params error， input path and outputFile path required!");
    }

    av_log(NULL, AV_LOG_INFO, "\ninputPath: %s, outputPath: %s\n", inputPath, outPath);

    AVFormatContext *outContext = avformat_alloc_context();
    AVOutputFormat *format = av_guess_format(NULL, outPath, NULL);
    if (format == NULL) {
        av_log(NULL, AV_LOG_ERROR, "can't guess file format of %s\n", outPath);
        return -1;
    }
    outContext->oformat = format;

    AVStream *outStream = avformat_new_stream(outContext, NULL);
    if (outStream == NULL) {
        av_log(NULL, AV_LOG_ERROR, "failed to create out stream!\n");
        return -1;
    }

    ret = avio_open(&outContext->pb, outPath, AVIO_FLAG_WRITE);
    if (ret < 0) {
        av_strerror(ret, errors, ERROR_LEN);
        av_log(NULL, AV_LOG_ERROR, "can't open file %s, %d(%s)\n", outPath, ret, errors);
        return -1;
    }


    do {
        outFile = fopen(outPath, "w+");
        if (outFile == NULL) {
            av_log(NULL, AV_LOG_ERROR, "can't open output file: %s\n", outPath);
            ret = -1;
            break;
        }

        ret = avformat_open_input(&inputContext, inputPath, NULL, NULL);
        if (ret < 0) {
            av_strerror(ret, errors, ERROR_LEN);
            av_log(NULL, AV_LOG_ERROR, "can't open input file: %s, %d(%s)\n", inputPath, ret, errors);
            break;
        }

        ret = avformat_find_stream_info(inputContext, NULL);
        if (ret < 0) {
            av_strerror(ret, errors, ERROR_LEN);
            av_log(NULL, AV_LOG_ERROR, "can't find stream information: %s, %d(%s)\n", inputPath, ret, errors);
            break;
        }

        av_dump_format(inputContext, 0, inputPath, 0);
        av_dump_format(outContext, 0, outPath, 1);

        AVFrame *frame = av_frame_alloc();
        if (frame == NULL) {
            av_log(NULL, AV_LOG_ERROR, "can't allocate frame");
            ret = -1;
            break;
        }

        AVPacket packet;
        av_init_packet(&packet);
        packet.data = NULL;
        packet.size = 0;

        int videoStreamIndex = av_find_best_stream(inputContext, AVMEDIA_TYPE_VIDEO, -1, -1, NULL, 0);
        if (videoStreamIndex < 0) {
            ret = -1;
            av_strerror(ret, errors, ERROR_LEN);
            av_log(NULL, AV_LOG_ERROR, "can't find %s stream in input file: %s\n",
                   av_get_media_type_string(AVMEDIA_TYPE_VIDEO),
                   inputPath);
            break;
        }

        while (av_read_frame(inputContext, &packet) >= 0) {
            if (packet.stream_index == videoStreamIndex) {
                h264_mp4ToAnnexb(inputContext, &packet, outFile);
            }
            av_packet_unref(&packet);
        }
    } while (false);

    if (outFile != NULL) {
        fclose(outFile);
    }

    avformat_close_input(&inputContext);

    return ret;
}

int h264_mp4ToAnnexb(AVFormatContext *inputContext, AVPacket *in, FILE *outFile) {

    int ret = 0;
    uint32_t nalSize = 0;
    uint8_t unitType = 0;
    uint32_t cumulSize = 0;
    int i;

    AVPacket *out = av_packet_alloc();
    AVPacket spsppsPacket;

    const uint8_t *buff = in->data;
    int buffSize = in->size;
    const uint8_t *buffEnd = in->data + in->size;

    do {
        ret = AVERROR(EINVAL);
        if (buff + 4 > buffEnd) {
            ret = -1;
            break;
        }

        for (nalSize = 0, i = 0; i < 4; i++) {
            //高地址为size的低位，需要做调整
            nalSize = (nalSize << 8) | buff[i];
        }

        buff += 4;
        // 第一字节的后5位是NAL单元的类型
        unitType = *buff & 0x1f;

        if (nalSize > buffSize || nalSize < 0) {
            ret = -1;
            break;
        }

        if (unitType == 5) {
            // 关键帧
            AVCodecContext *pAvCodecContext = inputContext->streams[in->stream_index]->codec;
            uint8_t *extraData = pAvCodecContext->extradata;
            if (extraData != NULL) {
                h264ExtraDataToAnnexb(extraData, pAvCodecContext->extradata_size, &spsppsPacket,
                                      AV_INPUT_BUFFER_PADDING_SIZE);
            }

            // 特征码
            ret = alloc_and_copy(out, spsppsPacket.data, spsppsPacket.size, buff, nalSize);
            if (ret < 0) {
                break;
            }
        } else {
            ret = alloc_and_copy(out, NULL, 0, buff, nalSize);
            if (ret < 0) {
                break;
            }
        }

        ret = fwrite(out->data, 1, out->size, outFile);
        if (ret != out->size) {
            av_log(NULL, AV_LOG_ERROR, "warning: length of writen data (%d) isn't equals packet.szie (%d)\n",
                   ret, out->size);
        }

        fflush(outFile);

        buff += nalSize;
        cumulSize += nalSize;
    } while (cumulSize < buffSize);

    av_packet_free(&out);

    return ret;
}


#ifndef AV_RB16
#define AV_RB16(x) \
        ((((const uint8_t*)(x))[0]<<8)|\
        ((const uint8_t*)(x))[1])
#endif

int h264ExtraDataToAnnexb(uint8_t *codecExtraData, int extraDataSize, AVPacket *outExtraData, int padding) {
    uint16_t unitSize = 0;
    uint64_t totalSize = 0;
    uint8_t *out = NULL;
    uint8_t unitNb = 0;
    uint8_t spsDone = 0;
    uint8_t spsSeen = 0;
    uint8_t ppsSeen = 0;
    uint8_t spsOffset = 0;
    uint8_t ppsOffset = 0;

    const uint8_t *extraData = codecExtraData + 4;

    static const uint8_t naluHeader[4] = {0, 0, 0, 1};
    int lengthSize = (*extraData++ & 0x03) + 1;
    int ret = 0;

    spsOffset = -1;
    ppsOffset = -1;

    unitNb = *extraData++ & 0x1f;
    if (!unitNb) {
        goto __pps;
    } else {
        spsOffset = 0;
        spsSeen = 1;
    }

    while (unitNb--) {
        unitSize = AV_RB16(extraData);
        totalSize += unitSize + 4;

        if (totalSize > INT_MAX - padding) {
            av_log(NULL, AV_LOG_ERROR, "too big extradata size, corrupted stream or invalid MAP4/AVCC bitstream\n");
            av_free(out);
            return AVERROR(EINVAL);
        }

        if (extraData + 2 + unitSize > codecExtraData + extraDataSize) {
            av_log(NULL, AV_LOG_ERROR, "packet header is not contained in global extradata\n");
            av_free(out);
            return AVERROR(EINVAL);
        }

        ret = av_reallocp(&out, totalSize + padding);
        if (ret < 0) {
            return ret;
        }

        memcpy(out + totalSize - unitSize - 4, naluHeader, 4);
        memcpy(out + totalSize - unitSize, extraData + 2, unitSize);

        extraData += 2 + unitSize;

        __pps:
        if (!unitNb && !spsDone++) {
            unitNb = *extraData++;
            if (unitNb) {
                ppsOffset = totalSize;
                ppsSeen = 1;
            }
        }
    }

    if (out != NULL) {
        memset(out + totalSize, 0, padding);
    }

    if (spsSeen == 0) {
        av_log(NULL, AV_LOG_ERROR,
               "Warning: SPS NALU missing or invalid. The resulting stream may not play\n");
    }

    if (ppsSeen == 0) {
        av_log(NULL, AV_LOG_ERROR,
               "Warning: PPS NALU missing or invalid. The resulting stream may not play\n");
    }

    outExtraData->data = out;
    outExtraData->size = totalSize;

    return lengthSize;
}

#ifndef AV_WB32
#define AV_WB32(p, val) do{ \
        uint32_t d = (val); \
        ((uint8_t*)(p))[3] = (d);\
        ((uint8_t*)(p))[2] = (d)>>8;\
        ((uint8_t*)(p))[1] = (d)>>16;\
        ((uint8_t*)(p))[0] = (d)>>24;\
    }while(false)

#endif

int alloc_and_copy(AVPacket *out, uint8_t *spspps, int spsppsSize, const uint8_t *in, uint32_t inSize) {
    uint32_t offset = out->size;
    uint8_t nalHeaderSize = offset != 0 ? 3 : 4;
    int ret;

    ret = av_grow_packet(out, spsppsSize + inSize + nalHeaderSize);
    if (ret < 0) {
        return ret;
    }

    if (spspps != NULL) {
        memcpy(out->data + offset, spspps, spsppsSize);
    }

    memcpy(out->data + spsppsSize + nalHeaderSize + offset, in, inSize);
    if (offset != 0) {
        AV_WB32(out->data + spsppsSize, 1);
    } else {
        uint8_t *buf = out->data + offset + spsppsSize;
        buf[0] = 0;
        buf[1] = 0;
        buf[2] = 1;
    }

    return 0;
}

```