## 直播流命令

### 推流
ffmpeg -re -i out.mp4 -c copy -f flv rtmp://server/live/streamName

-re 降低帧率，


### 拉流
ffmpeg -i rtmp://server/live/streamName -c copy dump.flv




1，RTMP协议直播源

香港卫视：rtmp://live.hkstv.hk.lxdns.com/live/hks2

 

2，RTSP协议直播源

珠海过澳门大厅摄像头监控：rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp

大熊兔（点播）：rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov

 

3，HTTP协议直播源

香港卫视：http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8

CCTV1高清：http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8

CCTV3高清：http://ivi.bupt.edu.cn/hls/cctv3hd.m3u8

CCTV5高清：http://ivi.bupt.edu.cn/hls/cctv5hd.m3u8

CCTV5+高清：http://ivi.bupt.edu.cn/hls/cctv5phd.m3u8

CCTV6高清：http://ivi.bupt.edu.cn/hls/cctv6hd.m3u8

苹果提供的测试源（点播）：http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8


rtmp://219.232.160.120/livestream/c64024e7cd451ac19613345704f985fa        深圳卫视高清-高清画质        
rtmp://219.232.160.120/livestream/3c7b0e7ffa2bb75c01aa7635cb7cc12f        深圳卫视高清-标准画质        
rtmp://219.232.160.122/livestream/5b53152f52f2efe9b4f85bd55f3751dd        深圳卫视-高清画质        
rtmp://219.232.160.122/livestream/d46024701fdfe213f7e7dd8e4fb9a1d2        深圳卫视-标准画质        
rtmp://219.232.160.122/livestream/20ef0fed3abb88b0beeabac91e7056f5        深圳都市-高清画质        
rtmp://219.232.160.122/livestream/dc53abc28d10dafa5ea9088a463a5340        深圳都市-标准画质        
rtmp://219.232.160.120/livestream/b119789b6fd4f2cb86a59cb0addf7776        深圳电视剧-高清画质        
rtmp://219.232.160.121/livestream/c0c2132dcc654f5c9ab55971f081d5fa        深圳电视剧-标准画质        
rtmp://219.232.160.120/livestream/77e19c3214ef876a1e71b4f92bd49040        深圳财经-高清画质        
rtmp://219.232.160.120/livestream/bd0ec42624e97bb1406b6c04e20a40b0        深圳财经-标准画质        
rtmp://219.232.160.121/livestream/39291c6066742446fbb141f226a6b932        深圳娱乐-高清画质        
rtmp://219.232.160.120/livestream/2c3398a96dea0312429f4893f15761af        深圳娱乐-标准画质        
rtmp://219.232.160.121/livestream/00c64c8b1c0776fb5f7b69fd25e670c2        深圳少儿-高清画质        
rtmp://219.232.160.121/livestream/98109e00e70a37faf1c3d58a70a58662        深圳少儿-标准画质        
rtmp://219.232.160.122/livestream/ca1721e961a3251686db703008379d93        深圳公共-高清画质        
rtmp://219.232.160.122/livestream/f32c31db27e429dee3b5f55b40f5b663        深圳公共-标准画质        
rtmp://219.232.160.122/livestream/d4dd66dada59eade1bf6146369196f04        宜和购物-高清画质        
rtmp://219.232.160.122/livestream/98478888e1ffce210c542e92eb254d61        宜和购物-标准画质        
rtmp://219.232.160.122/livestream/29236636460b82032853fc991b9b6935        气象频道-高清画质        
rtmp://219.232.160.122/livestream/3a7cf633eeaf9b7bc524775a77efeafa        气象频道-标准画质        
rtmp://219.232.160.122/livestream/167b464162827dfc763b603cc34ad751        财富天下-高清画质        
rtmp://219.232.160.122/livestream/60f2761b50b17a81e4a95c88eff793bb        财富天下-标准画质        
rtmp://219.232.160.121/livestream/3e6fde267767ca49d8d0d45a4816a95b        广东卫视-高清画质        
rtmp://219.232.160.121/livestream/e6b65f216a24d0a17f993ee27396b47a        广东卫视-标准画质        
rtmp://219.232.160.122/livestream/48a26bba89a367c8a636749b4fc300ad        南方卫视-高清画质        
rtmp://219.232.160.122/livestream/076adbd60ce61eff6b48683288d451ca        南方卫视-标准画质        
rtmp://219.232.160.122/livestream/3b786ebe62277605c47071b0621245da        南方综艺-高清画质        
rtmp://219.232.160.122/livestream/0d58f98a6bb513600fcd5980c723171d        南方综艺-标准画质        
rtmp://219.232.160.120/livestream/ab41805ee2440bf3748c8e3f1ec6be18        南方少儿-高清画质        
rtmp://219.232.160.120/livestream/f1bba4f47fe8578ae5902246300790b3        南方少儿-标准画质        
rtmp://219.232.160.120/livestream/c2f182999d5c66b9602bfab52c862567        吉林卫视-高清画质        
rtmp://219.232.160.120/livestream/d1aa4fba0a224d32c594cb3e6320b834        吉林卫视-标准画质        
rtmp://219.232.160.121/livestream/0e60e1dd7fb4237dce7adbaf41c44d64        东南卫视-高清画质        
rtmp://219.232.160.121/livestream/3b7c0fa3a385b7c790e92f34f0fb1abc        东南卫视-标准画质        
rtmp://219.232.160.122/livestream/79f11bbdc29595b03cde2c26c8e64f1d        浙江卫视-高清画质        
rtmp://219.232.160.122/livestream/1b1e3ddcec63866949d63cc7342df283        浙江卫视-标准画质        
rtmp://219.232.160.121/livestream/bd86ad811f1d77da0d1a8597ccdb51c5        重庆卫视-高清画质        
rtmp://219.232.160.121/livestream/bd86ad811f1d77da0d1a8597ccdb51c5        重庆卫视-标准画质        
rtmp://219.232.160.121/livestream/0547ce2bf066625d9bf1775a01b11c77        广西卫视-高清画质        
rtmp://219.232.160.121/livestream/d02bb5156a4cad08753af40176684f15        广西卫视-标准画质        
rtmp://219.232.160.121/livestream/6f3c16428bf06033aeb7e944000ee639        安徽卫视-高清画质        
rtmp://219.232.160.121/livestream/19eccd26be7f974fbbad97aa86aaebfb        安徽卫视-标准画质        
rtmp://219.232.160.121/livestream/b0dcabbb6a0e468a300825ae52271085        黑龙江卫视-高清画质        
rtmp://219.232.160.121/livestream/54445648da3e2c6baa828afb7bfef032        黑龙江卫视-标准画质        
rtmp://219.232.160.122/livestream/d59fa94440cc2669b1bdb5ec7d5fc78b        河南卫视-高清画质        
rtmp://219.232.160.122/livestream/39a18160b7c75e2e1504a22dddb9b45a        河南卫视-标准画质        
rtmp://219.232.160.121/livestream/0b4d575e42911ddc5eb5fbff054c64ca        山东卫视-高清画质        
rtmp://219.232.160.121/livestream/a65bda496f47dedb0dccac2a39db33b7        山东卫视-标准画质        
rtmp://219.232.160.120/livestream/3100bff89b4dbdc0a80187b8925b462f        宁夏卫视-高清画质        
rtmp://219.232.160.120/livestream/f10db3c24c211ede3c92daf585016cf1        宁夏卫视-标准画质  


香港卫视,rtmp://live.hkstv.hk.lxdns.com/live/hks

香港财经,rtmp://202.69.69.180:443/webcast/bshdlive-pc

韩国GoodTV,rtmp://mobliestream.c3tv.com:554/live/goodtv.sdp

韩国朝鲜日报,rtmp://live.chosun.gscdn.com/live/tvchosun1.stream

美国1,rtmp://ns8.indexforce.com/home/mystream

美国2,rtmp://media3.scctv.net/live/scctv_800

美国中文电视,rtmp://media3.sinovision.net:1935/live/livestream

湖南卫视,rtmp://58.200.131.2:1935/livetv/hunantv
