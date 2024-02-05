## 实战：抽取音频数据

### 相关API
```cppp

// 初始化数据包结构体
av_init_packet()

// 在多媒体文件中找打最好的流
av_find_best_stream()

// 从流中获取数据包
// av_read_frame其实是读取包，历史遗留问题，名字没改
av_read_frame()

// 减少包数据的引用计数，ffmpeg会自动回收对象
av_packet_unref()

```

AAC的音频文件格式有ADIF ＆ ADTS：

ADIF：Audio Data Interchange Format 音频数据交换格式。这种格式的特征是可以确定的找到这个音频数据的开始，不需进行在音频数据流中间开始的解码，

即它的解码必须在明确定义的开始处进行。故这种格式常用在磁盘文件中。

 
ADTS的全称是Audio Data Transport Stream。是AAC音频的传输流格式。

AAC音频格式在MPEG-2（ISO-13318-7 2003）中有定义。AAC后来又被采用到MPEG-4标准中。

这种格式的特征是它是一个有同步字的比特流，解码可以在这个流中任何位置开始。它的特征类似于mp3数据流格式。

 

简单说，ADTS可以在任意帧解码，也就是说它每一帧都有头信息。ADIF只有一个统一的头，所以必须得到所有的数据后解码。

且这两种的header的格式也是不同的，目前一般编码后的和抽取出的都是ADTS格式的音频流。

 

有的时候当你编码AAC裸流的时候，会遇到写出来的AAC文件并不能在PC和手机上播放，很大的可能就是AAC文件的每一帧里缺少了ADTS头信息文件的包装拼接。

只需要加入头文件ADTS即可。一个AAC原始数据块长度是可变的，对原始帧加上ADTS头进行ADTS的封装，就形成了ADTS帧。

每一帧的ADTS的头文件都包含了音频的采样率，声道，帧长度等信息，这样解码器才能解析读取。
一般情况下ADTS的头信息都是7个字节，分为2部分：
adts_fixed_header();
adts_variable_header();
其一为固定头信息，紧接着是可变头信息。固定头信息中的数据每一帧都相同，而可变头信息则在帧与帧之间可变。

|adts_fixed_header|位数|类型|说明|
|:-|:-|:-|:-|
|syncword|12|bslbf|同步头 总是0xFFF, all bits must be 1，代表着一个ADTS帧的开始|
|id|1|bslbf|MPEG标识符，0标识MPEG-4，1标识MPEG-2|
|layer|2|uimsbf|always: '00'|
|protection_absent|1|bslbf|表示是否误码校验。Warning, set to 1 if there is no CRC and 0 if there is CRC|
|profile|2|uimsbf|表示使用哪个级别的AAC，如01 Low Complexity(LC)--- AAC LC。有些芯片只支持AAC|
|sampling_frequency_index|4|uimsbf|表示使用的采样率下标，通过这个下标在 Sampling Frequencies[ ]数组中查找得知采样率的值|
|private_bit|1|bslbf|0|
|channel_configuration|3|uimsbf|表示声道数，比如2表示立体声双声道|
|original_copy|1|bslbf|set to 0 when encoding, ignore when decoding|
|home|1|bslbf|set to 0 when encoding, ignore when decoding|

adts_variable_header table

|adts_variable_header|位数|类型|说明|
|:-|:-|:-|:-|
|copyright_identification_bit|1|bslbf|set to 0 when encoding, ignore when decoding|
|copyright_identification_start|1|bslbf|set to 0 when encoding, ignore when decoding|
|frame_length|13|bslbf|一个ADTS帧的长度包括ADTS头和AAC原始流,frame length, this value must include 7 or 9 bytes of header length:aac_frame_length =(protection_absent == 1 ? 7 : 9) + size(AACFrame)protection_absent=0时, header length=9bytesprotection_absent=1时, header length=7bytes|
|adts_buffer_fullness|11|bslbf|0x7FF 说明是码率可变的码流|
|number_of_raw_data_blocks_in_frame|2|nimsfb|表示ADTS帧中有number_of_raw_data_blocks_in_frame + 1个AAC原始帧, 所以说number_of_raw_data_blocks_in_frame == 0 表示说ADTS帧中有一个AAC数据块|

在MPEG-2 AAC中定义了3种profile：

|index|profile|
|:-|:-|
|0|Main profile|
|1|Low Complexity(LC)|
|2|Scalable Sampling Rate profile (SSR)|
|3|(reserved)|

profile的值等于 Audio Object Type的值减1
profile = MPEG-4 Audio Object Type - 1

|Object Type ID| Audio Object Type|
|:-|:-|
|0|Null|
|1|AAC Main|
|2|AAC LC (Low Complexity)|
|3|AAC SSR (Scalable Sample Rate)|
|4|AAC LTP (Long Term Prediction)|
|5|SBR (Spectral Band Replication)|
|6|AAC Scalable|
|7|TwinVQ|
|8|CELP (Code Excited Linear Prediction)|
|9|HXVC (Harmonic Vector eXcitation Coding)|
|10|Reserved|
|11|Reserved|
|12|TTSI (Text-To-Speech Interface)|
|13|Main Synthesis|
|14|Wavetable Synthesis|
|15|General MIDI|
|16|Algorithmic Synthesis and Audio Effects|
|17|ER (Error Resilient) AAC LC|
|18|Reserved|
|19|ER AAC LTP|
|20|ER AAC Scalable|
|21|ER TwinVQ|
|22|ER BSAC (Bit-Sliced Arithmetic Coding)|
|23|ER AAC LD (Low Delay)|
|24|ER CELP|
|25|ER HVXC|
|26|ER HILN (Harmonic and Individual Lines plus Noise)|
|27|ER Parametric|
|28|SSC (SinuSoidal Coding)|
|29|PS (Parametric Stereo)|
|30|MPEG Surround|
|31|(Escape value)|
|32|Layer-1|
|33|Layer-2|
|34|Layer-3|
|35|DST (Direct Stream Transfer)|
|36|ALS (Audio Lossless)|
|37|SLS (Scalable LosslesS)|
|38|SLS non-core|
|39|ER AAC ELD (Enhanced Low Delay)|
|40|SMR (Symbolic Music Representation) Simple|
|41|SMR Main|
|42|USAC (Unified Speech and Audio Coding) (no SBR)|
|43|SAOC (Spatial Audio Object Coding)|
|44|LD MPEG Surround|
|45|USAC|

sampling_frequency_index：表示使用的采样率下标，通过这个下标在 Sampling Frequencies[ ]数组中查找得知采样率的值
Table: Sampling Frequency Index

|SamplingFrequencyIndex |Value|
|:-|:-|
|0|96000 Hz|
|1|88200 Hz|
|2|64000 Hz|
|3|48000 Hz|
|4|44100 Hz|
|5|32000 Hz|
|6|24000 Hz|
|7|22050 Hz|
|8|16000 Hz|
|9|12000 Hz|
|10|11025 Hz|
|11|8000 Hz|
|12|7350 Hz|
|13|Reserved|
|14|Reserved|
|15|frequency is written explictly|

Channel Configurations
These are the channel configurations:
|value|number of Channels|channel to speaker mapping|
|:-|:-|:-|
|0|-|Defined in AOT Specifc Config|
|1|1 channel|front-center|
|2|2 channels|front-left, front-right|
|3|3 channels|front-center, front-left, front-right|
|4|4 channels|front-center, front-left, front-right, back-center|
|5|5 channels|front-center, front-left, front-right, back-left, back-right|
|6|6 channels|front-center, front-left, front-right, back-left, back-right, LFE-channel|
|7|8 channels|front-center, front-left, front-right, side-left, side-right, back-left, back-right, LFE-channel|
|8-15|-|Reserved

### 示例代码
```cpp

extern "C" {
#include <stdint.h>
#include <libavutil/log.h>
#include <libavformat/avformat.h>
}

void makeADTSHeader(uint8_t *header, int frameSize);

int main(int argc, char **argv) {
    int ret;

    av_log_set_level(AV_LOG_INFO);
    av_register_all();

    if (argc < 3) {
        av_log(NULL, AV_LOG_ERROR, "input params error， input path and outputFile path required!");
    }

    const char *inputPath = argv[1];
    const char *outPath = argv[2];
    av_log(NULL, AV_LOG_INFO, "\ninputPath: %s, outputPath: %s\n", inputPath, outPath);

    AVFormatContext *context = NULL;
    AVPacket avPacket;
    FILE *outputFile = NULL;

    do {
        ret = avformat_open_input(&context, inputPath, NULL, NULL);
        if (ret < 0) {
            av_log(NULL, AV_LOG_ERROR, "cann't open %s", inputPath);
            ret = -1;
            break;
        }

        ret = av_find_best_stream(context, AVMEDIA_TYPE_AUDIO, -1, -1, NULL, 0);
        if (ret < 0) {
            av_log(NULL, AV_LOG_ERROR, "can not find audio stream in %s", inputPath);
            ret = -1;
            break;
        }
        int audioStreamIndex = ret;

        av_dump_format(context, audioStreamIndex, inputPath, 0);

        av_init_packet(&avPacket);
        outputFile = fopen(outPath, "w+");
        if (outputFile == NULL) {
            av_log(NULL, AV_LOG_ERROR, "can not open input file: %s", inputPath);
            ret = -1;
            break;
        }

        uint8_t header[7];

        while (av_read_frame(context, &avPacket) >= 0) {
            if (avPacket.stream_index == audioStreamIndex) {
                makeADTSHeader(header, avPacket.size);
                ret = fwrite(header, 1, 7, outputFile);
                if (ret != 7) {
                    av_log(NULL, AV_LOG_ERROR, "write adts header error: %s. headerSize = %d, writenLen = %d",
                           inputPath, 7, ret);
                    break;
                }

                ret = fwrite(avPacket.data, 1, avPacket.size, outputFile);
                if (ret != avPacket.size) {
                    av_log(NULL, AV_LOG_ERROR, "write file error: %s. dataSize = %d, writenLen = %d", inputPath,
                           avPacket.size, ret);
                    break;
                }
            }
            av_packet_unref(&avPacket);
        }
        fflush(outputFile);

    } while (false);

    if (outputFile != NULL) {
        fclose(outputFile);
    }

    avformat_close_input(&context);

    return ret;
}

void makeADTSHeader(uint8_t *header, int frameSize) {
    memset(header, 0, 7);

    uint8_t id = 1;
    uint8_t protection_absent = 1;
    uint8_t profile = 1;
    uint8_t sampling_frequency_index = 3;
    uint8_t channel_configuration = 2;
    uint16_t frame_length = (protection_absent == 1 ? 7 : 9) + frameSize;
    uint16_t adts_buffer_fullness = 0x7ff;
    uint8_t number_of_raw_data_blocks_in_frame = 0;

    //syncword ：同步头 总是0xFFF, all bits must be 1，代表着一个ADTS帧的开始
    header[0] = 0xff;
    header[1] = 0xf0;

    // ID：MPEG标识符，0标识MPEG-4，1标识MPEG-2
    header[1] |= (id & 0x01) << 3;

    // Layer：always: '00'
    header[1] |= 0 << 2;

    // protection_absent：表示是否误码校验。Warning, set to 1 if there is no CRC and 0 if there is CRC
    header[1] |= protection_absent & 0x01;

    // profile：表示使用哪个级别的AAC，如01 Low Complexity(LC)--- AAC LC。有些芯片只支持AAC LC 。
    // 在MPEG-2 AAC中定义了3种：
    //
    // 0 Main profile
    // 1 Low Complexity profile(LC)
    // 2 Scalable Sampling Rate profile(SSR)
    // 3 (reserved)
    //
    // profile的值等于 Audio Object Type的值减1
    // profile = MPEG-4 Audio Object Type - 1
    header[2] = (profile & 0x03) << 6;

    // sampling_frequency_index：表示使用的采样率下标，通过这个下标在 Sampling Frequencies[ ]数组中查找得知采样率的值。
    // samplingFrequencyIndex Value
    // 0x0  96000
    // 0x1  88200
    // 0x2  64000
    // 0x3  48000
    // 0x4  44100
    // 0x5  32000
    // 0x6  24000
    // 0x7  22050
    // 0x8  16000
    // 0x9  12000
    // 0xa  11025
    // 0xb  8000
    // 0xc  7350
    // 0xd  reserved
    // 0xd  reserved
    // 0xd  escape value
    header[2] |= (sampling_frequency_index & 0x0f) << 2;

    // private bit
    header[2] |= 0 << 1;

    // channel_configuration: 表示声道数，比如2表示立体声双声道
    //
    //value     number of channels channel to speaker mapping
    // 0: Defined in AOT Specifc Config
    // 1: 1 channel: front-center
    // 2: 2 channels: front-left, front-right
    // 3: 3 channels: front-center, front-left, front-right
    // 4: 4 channels: front-center, front-left, front-right, back-center
    // 5: 5 channels: front-center, front-left, front-right, back-left, back-right
    // 6: 6 channels: front-center, front-left, front-right, back-left, back-right, LFE-channel
    // 7: 8 channels: front-center, front-left, front-right, side-left, side-right, back-left, back-right, LFE-channel
    // 8-15: Reserved
    header[2] |= (channel_configuration & 0x04) >> 2;
    header[3] = (channel_configuration & 0x03) << 6;

    // original_copy
    header[3] |= (0 << 5);

    // home
    header[3] |= (0 << 4);

    // copyright_identification_bit
    header[3] |= (0 << 3);
    // copyright_identification_start
    header[3] |= (0 << 2);

    // frame_length : 一个ADTS帧的长度包括ADTS头和AAC原始流.
    //
    // frame length, this value must include 7 or 9 bytes of header length:
    // aac_frame_length = (protection_absent == 1 ? 7 : 9) + size(AACFrame)
    // protection_absent=0时, header length=9bytes
    // protection_absent=1时, header length=7bytes
    header[3] |= (frame_length >> 11) & 0x3;
    header[4] = (frame_length >> 3) & 0xff;
    header[5] = (frame_length & 0x7) << 5;

    // adts_buffer_fullness：0x7FF 说明是码率可变的码流。
    header[5] |= ((adts_buffer_fullness >> 6) & 0x1f);
    header[6] = (adts_buffer_fullness & 0x3f) << 2;

    // number_of_raw_data_blocks_in_frame：表示ADTS帧中有number_of_raw_data_blocks_in_frame + 1个AAC原始帧。
    // 所以说number_of_raw_data_blocks_in_frame == 0 表示说ADTS帧中有一个AAC数据块。
    header[6] |= number_of_raw_data_blocks_in_frame & 0x03;
}

```
