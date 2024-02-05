# 原创：学习如何使用 FFmpeg 打造自己的播放器

Android FFmpeg 音视频系列：



- [FFmpeg 编译和集成](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654162543&idx=1&sn=894a6bfb0f8f652ef53860075af1754b&chksm=8cf39d5cbb84144a9d62fa80cbeed1843aadfe97bf8a30ab02474f98ec86be649d65e301674b&scene=21#wechat_redirect)
- [FFmpeg + ANativeWindow 实现视频解码播放](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654162564&idx=1&sn=6785c7f9b6bdccbd400f792e9389b15c&chksm=8cf39db7bb8414a14a4acdea47e866f4b19ebdf80ed5aa7663a678c9571d505ecda294b65a05&scene=21#wechat_redirect)
- [FFmpeg + OpenSLES 实现音频解码播放](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654162604&idx=1&sn=c4e6d5a53fddcc327861cb1956285c9c&chksm=8cf39d9fbb8414898778a461b8249b698486dff85d52f0f4a86deebb711597ac685fdc99c8c3&scene=21#wechat_redirect)
- [FFmpeg + OpenGLES 实现音频可视化播放](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654162642&idx=1&sn=d25b3204928fdea29bee287024a763a1&chksm=8cf39de1bb8414f70f98b79c201dbd53f1679bceac37109e07a1072ff32c220c07eebe93111b&scene=21#wechat_redirect)
- [FFmpeg + OpenGLES 实现视频解码播放和视频滤镜](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654162883&idx=1&sn=40e6a50ad4ca715dbceaa3782ae2fdc1&chksm=8cf39cf0bb8415e6411b87ed6a0edad423ce2b869399b788815785ea3f0584599a0973c1cf81&scene=21#wechat_redirect)
- [FFmpeg 播放器实现简单音视频同步的三种方式](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654163000&idx=1&sn=80b75d043ae5a71e4fe59fe982129afe&chksm=8cf3830bbb840a1d6b8093781c6d5fdbf957c5ca7246cac11d04a9607271a1b45170613b1bba&scene=21#wechat_redirect)
- [FFmpeg + OpenGL ES 实现 3D 全景播放器](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654163056&idx=1&sn=94d3d8ae3c004207b4c019a236b9129d&chksm=8cf38343bb840a5504d0cdc05cece6dbe4ab5fa89da71e59334f418634efd218129a6c7ac3a6&scene=21#wechat_redirect)
- [FFmpeg 播放器视频渲染优化](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654163238&idx=1&sn=3f778082ee5a278bdd05743c33247127&chksm=8cf38215bb840b03fb6de375e22521fa3db762acdc09a37f9240648845e1057ff4c65d4a5661&scene=21#wechat_redirect)
- [FFmpeg、x264以及fdk-aac 编译整合](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654164725&idx=1&sn=f643b096b3e87b1d1cd6068f2a25825a&chksm=8cf385c6bb840cd0d151b3140079e310c888442d96fe7857decfc708aaf96c37c374f2cefd62&scene=21#wechat_redirect)
- [FFmpeg 视频录制 - 视频添加滤镜和编码](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654164776&idx=1&sn=5e9f1307349b2ec16452c9b33cc289cb&chksm=8cf3841bbb840d0dc08a8c447c83f21fff1fe205a9096ef705795d7341862ac8308206339753&scene=21#wechat_redirect)
- [FFmpeg + Android AudioRecorder 音频录制编码](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654164836&idx=1&sn=24d5c252a9568145734c86b10d98f9d1&chksm=8cf38457bb840d4162d025c58a11bc91b6c16efcf9e02cdc8ec1f5849139f7ed4dfcf32bf09f&scene=21#wechat_redirect)
- [Android FFmpeg 实现带滤镜的微信小视频录制功能](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654164869&idx=1&sn=dd3dc715f31de1b750d97a0ab71c1e62&chksm=8cf384b6bb840da0332a11c8aeeccb83dad0d8bf2a665cc4ab29e3542906592bbd57d60e1e6d&scene=21#wechat_redirect)
- [Android FFmpeg 流媒体边播放边录制功能](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654165251&idx=1&sn=a5448fe687ac42e6ab96279831aedade&chksm=8cf38a30bb8403265dad19af9953b7525333634471b389087ed898b54bb93f2b5feb0ce97860&scene=21#wechat_redirect)
- [Android FFmpeg + MediaCodec 实现视频硬解码](http://mp.weixin.qq.com/s?__biz=MzIwNTIwMzAzNg==&mid=2654170440&idx=1&sn=a754ec5830bbb562923691e48be5f744&chksm=8cf3be7bbb84376de561ce962111bc21044d8905583504498bc444ad422196d6b92ac21c5103&scene=21#wechat_redirect)



前面 FFmpeg 系列的文章中，已经实现了 **FFmpeg 的编译和集成，基于 FFmpeg 实现音视频的播放、录制，并结合 OpenGL 添加丰富的滤镜**等功能，这些 demo 基本上将 FFmpeg 使用涉及到的知识点基本上覆盖了。



学完这些的你肯定有一些想法，**比如使用 FFmpeg 打造一个自己的通用播放器、 做一个音视频剪辑软件等等**，那么接下来推荐做的是学习一些优秀的开源项目，音视频的开源项目首推 ExoPlayer、 ijkplayer。



但是这些著名的开源项目代码量比较大且功能繁多，对一些刚入门的开发者来说学习起来比较吃力，也不容易坚持看下来。



![图片](assets/012_原创：学习如何使用 FFmpeg 打造自己的播放器/640.jpeg)

Fanplayer



所以我们可以从一些中等代码量的优秀开源项目开始学习起来，基于此，在学完本文 FFmpeg 系列的基础上，接下来可以学习和研究开源跨平台播放器 Fanplayer 。



项目地址：https://github.com/rockcarry/fanplayer



**fanplayer 是一个基于 FFmpeg 实现的支持 Android 和 Windows 平台的通用播放器，支持硬解码、倍速播放、流媒体播放等功能**，播放器常用的功能基本上都支持，项目结构清晰，非常方便入手学习。 



但是 fanplayer 需要你自己在 linux 环境下编译一遍 FFmpeg 源码生成依赖库，不过编译脚本作者都写好了，需要自己动手编译一遍 FFmpeg 并集成到项目中去。



实在嫌麻烦的同学，我这里已经把项目编译和集成完毕，直接拉下来看项目代码即可



项目代码：https://github.com/githubhaohao/Fanplayer-android



接下来简单讲解下 fanplayer 项目的源码供你参考，其中 **Java 代码比较简单，就是 SurfaceView 的 surface 传下来构建 NativeWindow** ，这里重点讲解 C 部分实现。



JNI 入口函数定义在文件 fanplayer_jni.cpp ，定义了几个播放器常用的 API：



```
static const JNINativeMethod g_methods[] = {
    { "nativeOpen"              , "(Ljava/lang/String;Ljava/lang/Object;IILjava/lang/String;)J", (void*)nativeOpen },
    { "nativeClose"             , "(J)V"  , (void*)nativeClose    },
    { "nativePlay"              , "(J)V"  , (void*)nativePlay     },
    { "nativePause"             , "(J)V"  , (void*)nativePause    },
    { "nativeSeek"              , "(JJ)V" , (void*)nativeSeek     },
    { "nativeSetParam"          , "(JIJ)V", (void*)nativeSetParam },
    { "nativeGetParam"          , "(JI)J" , (void*)nativeGetParam },
    { "nativeSetDisplaySurface" , "(JLjava/lang/Object;)V", (void*)nativeSetDisplaySurface },
};

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved)
{
    DO_USE_VAR(reserved);

    JNIEnv* env = NULL;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK || !env) {
        __android_log_print(ANDROID_LOG_ERROR, "fanplayer_jni", "ERROR: GetEnv failed\n");
        return -1;
    }

    jclass cls = env->FindClass("com/rockcarry/fanplayer/MediaPlayer");
    int ret = env->RegisterNatives(cls, g_methods, sizeof(g_methods)/sizeof(g_methods[0]));
    if (ret != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, "fanplayer_jni", "ERROR: failed to register native methods !\n");
        return -1;
    }

    // for g_jvm
    g_jvm = vm;
    av_jni_set_java_vm(vm, NULL);
    return JNI_VERSION_1_4;
}
```



接下来就是 ffplayer.c 文件，封装了整个播放器，包含了三个子模块，分别是解复用、视频解码和音频解码模块，三个模块分别位于三个子线程中：



```
// 函数实现
void* player_open(char *file, void *win, PLAYER_INIT_PARAMS *params)
{
    PLAYER *player = NULL;

    //........代码省略

    pthread_create(&player->avdemux_thread, NULL, av_demux_thread_proc, player);
    pthread_create(&player->adecode_thread, NULL, audio_decode_thread_proc, player);
    pthread_create(&player->vdecode_thread, NULL, video_decode_thread_proc, player);
    return player; // return

error_handler:
    player_close(player);
    return NULL;
}
```



**解复用、视频解码和音频解码模块**三个子线程是通过 packet 队列进行通信，生产者和消费者模型。



文件 adev-android.cpp ，音频播放是通过 JNI 创建了 AudioTrack 对象，开启了一个子线程不断地从保存 PCM 数据的队列（链表）中读取数据：



```
// 接口函数实现
void* adev_create(int type, int bufnum, int buflen, CMNVARS *cmnvars)
{
    //.......省略代码

    jclass jcls         = env->FindClass("android/media/AudioTrack");
    ctxt->jmid_at_init  = env->GetMethodID(jcls, "<init>" , "(IIIIII)V");
    ctxt->jmid_at_close = env->GetMethodID(jcls, "release", "()V");
    ctxt->jmid_at_play  = env->GetMethodID(jcls, "play"   , "()V");
    ctxt->jmid_at_pause = env->GetMethodID(jcls, "pause"  , "()V");
    ctxt->jmid_at_write = env->GetMethodID(jcls, "write"  , "([BII)I");

    // new AudioRecord
    #define STREAM_MUSIC        3
    #define ENCODING_PCM_16BIT  2
    #define CHANNEL_STEREO      3
    #define MODE_STREAM         1
    jobject at_obj = env->NewObject(jcls, ctxt->jmid_at_init, STREAM_MUSIC, ADEV_SAMPLE_RATE, CHANNEL_STEREO, ENCODING_PCM_16BIT, ctxt->buflen * 2, MODE_STREAM);
    ctxt->jobj_at  = env->NewGlobalRef(at_obj);
    env->DeleteLocalRef(at_obj);

    // start audiotrack
    env->CallVoidMethod(ctxt->jobj_at, ctxt->jmid_at_play);

    // create mutex & cond
    pthread_mutex_init(&ctxt->lock, NULL);
    pthread_cond_init (&ctxt->cond, NULL);

    // create audio rendering thread
    pthread_create(&ctxt->thread, NULL, audio_render_thread_proc, ctxt);
    return ctxt;
}
```



解码后的视频图像直进行渲染，视频渲染走的是 ffrender.c 的 render_video ，然后调用 vdev-android.cpp 中的 vdev_android_lock：



```
static void vdev_android_lock(void *ctxt, uint8_t *buffer[8], int linesize[8], int64_t pts)
{
    VDEVCTXT *c = (VDEVCTXT*)ctxt;
    if (c->status & VDEV_ANDROID_UPDATE_WIN) {
        if (c->win    ) { ANativeWindow_release(c->win); c->win = NULL; }
        if (c->surface) c->win = ANativeWindow_fromSurface(get_jni_env(), (jobject)c->surface);
        if (c->win    ) ANativeWindow_setBuffersGeometry(c->win, c->vw, c->vh, DEF_WIN_PIX_FMT);
        c->status &= ~VDEV_ANDROID_UPDATE_WIN;
    }
    if (c->win) {
        ANativeWindow_Buffer winbuf;
        if (0 == ANativeWindow_lock(c->win, &winbuf, NULL)) {
            buffer  [0] = (uint8_t*)winbuf.bits;
            linesize[0] = winbuf.stride * 4;
            linesize[6] = c->vw;
            linesize[7] = c->vh;
        }
    }
    c->cmnvars->vpts = pts;
}
```



音视频同步用的是视频向音频同步的方式，并且参考 2 帧的理论渲染间隔进行微调，代码位于 vdev-cmn.c 中的 vdev_avsync_and_complete：



```
void vdev_avsync_and_complete(void *ctxt)
{
    LOGCATE("vdev_avsync_and_complete");
    VDEV_COMMON_CTXT *c = (VDEV_COMMON_CTXT*)ctxt;
    int     tickframe, tickdiff, scdiff, avdiff = -1;
    int64_t tickcur, sysclock;

    if (!(c->status & VDEV_PAUSE)) {

        //++ frame rate & av sync control ++//
        tickframe   = 100 * c->tickframe / c->speed; //c->speed 默认 100
        tickcur     = av_gettime_relative() / 1000; //当前系统时间
        tickdiff    = (int)(tickcur - c->ticklast); //2帧渲染的（实际上的）时间间隔
        c->ticklast = tickcur;

        //(tickcur - c->cmnvars->start_tick) 播放了多久，系统时钟时间，单位都是 ms
        sysclock= c->cmnvars->start_pts + (tickcur - c->cmnvars->start_tick) * c->speed / 100;
        scdiff  = (int)(sysclock - c->cmnvars->vpts - c->tickavdiff); // diff between system clock and video pts
        avdiff  = (int)(c->cmnvars->apts  - c->cmnvars->vpts - c->tickavdiff); // diff between audio and video pts
        avdiff  = c->cmnvars->apts <= 0 ? scdiff : avdiff; // if apts is invalid, sync video to system clock

        //tickdiff：两次渲染的实际间隔 ，tickframe 根据帧率计算的理论上的渲染间隔
        if (tickdiff - tickframe >  5) c->ticksleep--;
        if (tickdiff - tickframe < -5) c->ticksleep++;
        if (c->cmnvars->vpts >= 0) {
            if      (avdiff >  500) c->ticksleep -= 3;
            else if (avdiff >  50 ) c->ticksleep -= 2;
            else if (avdiff >  30 ) c->ticksleep -= 1;
            else if (avdiff < -500) c->ticksleep += 3;
            else if (avdiff < -50 ) c->ticksleep += 2;
            else if (avdiff < -30 ) c->ticksleep += 1;
        }
        if (c->ticksleep < 0) c->ticksleep = 0;
        LOGCATE("vdev_avsync_and_complete tickdiff=%d, tickframe=%d, c->ticksleep=%d", tickdiff, tickframe, c->ticksleep);
        //-- frame rate & av sync control --//
    } else {
        c->ticksleep = c->tickframe;
    }

    if (c->ticksleep > 0 && c->cmnvars->init_params->avts_syncmode != AVSYNC_MODE_LIVE_SYNC0) av_usleep(c->ticksleep * 1000);
    av_log(NULL, AV_LOG_INFO, "d: %3d, s: %3d\n", avdiff, c->ticksleep);
}
```



以上即是 fanplayer 项目的核心代码片段。