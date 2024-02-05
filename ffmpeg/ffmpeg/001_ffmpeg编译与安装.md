## ffmpeg的编译与安装

### 下载ffmpeg源码
git clone https://git.ffmpeg.org/ffmpeg.git

### 配置
PATH="$HOME/bin:$PATH" PKG_CONFIG_PATH="$HOME/software/ffmpeg/lib/pkgconfig" ./configure --prefix="$HOME/software/ffmpeg" --pkg-config-flags="--static" --extra-cflags="-I$HOME/software/ffmpeg/include" --extra-ldflags="-L$HOME/software/ffmpeg/lib" --bindir="$HOME/bin" --enable-gpl --enable-libass --enable-libfdk-aac --enable-libfreetype --enable-libmp3lame --enable-libopus --enable-libtheora --enable-libvorbis --enable-libx264 --enable-nonfree --enable-ffplay

### 编译与安装
make && make install

### 配置项
需要调试可以使用下面的配置

启用调试
--enable-debug 

关闭优化
--disable-optimizations  
