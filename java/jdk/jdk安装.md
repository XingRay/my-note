## jdk安装

### 1. ubuntu

#### 1.1 通过压缩包手动安装

1.1.1 下载jdk压缩包

https://jdk.java.net/

目前最新版为19，进入页面：

https://jdk.java.net/19/

找到下载列表

## Builds

| Linux / AArch64 | [tar.gz](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_linux-aarch64_bin.tar.gz) ([sha256](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_linux-aarch64_bin.tar.gz.sha256)) | 194649390 |
| --------------: | ------------------------------------------------------------ | --------- |
|     Linux / x64 | [tar.gz](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_linux-x64_bin.tar.gz) ([sha256](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_linux-x64_bin.tar.gz.sha256)) | 195931906 |
| macOS / AArch64 | [tar.gz](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_macos-aarch64_bin.tar.gz) ([sha256](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_macos-aarch64_bin.tar.gz.sha256)) | 190625723 |
|     macOS / x64 | [tar.gz](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_macos-x64_bin.tar.gz) ([sha256](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_macos-x64_bin.tar.gz.sha256)) | 192580989 |
|   Windows / x64 | [zip](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_windows-x64_bin.zip) ([sha256](https://download.java.net/java/GA/jdk19.0.2/fdb695a9d9064ad6b064dc6df578380c/7/GPL/openjdk-19.0.2_windows-x64_bin.zip.sha256)) | 194455638 |

选择Linux / x64版本下载



1.1.2 上传至ubuntu服务器

使用terminal工具的sftp上传至用户目录即可， 可以使用 [mobaxterm](https://mobaxterm.mobatek.net/download.html) Home Edition [下载](https://mobaxterm.mobatek.net/download-home-edition.html)

1.1.3 复制/移动到指定目录

```bash
sudo cp ./openjdk-19.0.2_linux-x64_bin.tar.gz /usr/local/software/java/jdk/19/
```

1.1.4 解压

```
cd /usr/local/software/java/jdk/19/
sudo tar -zxvf ./openjdk-19.0.2_linux-x64_bin.tar.gz
```

1.1.5 修改环境配置文件

```bash
sudo nano /etc/profile
```

将Java配置信息写入profile文件，如下所示：

```bash
export JAVA_HOME=/usr/local/software/java/jdk/19/jdk-19.0.2
export CLASSPATH=.:$JAVA_HOME/lib/

# other paths

PATH=$JAVA_HOME/bin:$PATH
export PATH
```

执行以下命令让配置生效：

```bash
source /etc/profile
```

1.1.6 测试

```bash
java --version
```

出现以下提示即为安装成功：

```bash
openjdk version "19.0.2" 2023-01-17
OpenJDK Runtime Environment (build 19.0.2+7-44)
OpenJDK 64-Bit Server VM (build 19.0.2+7-44, mixed mode, sharing)
```



1.1.7 设置软连接

```bash
sudo ln -s /usr/local/software/java/jdk/19/jdk-19.0.2/bin/java  /usr/bin/java
```

测试

```bash
sudo java --version
```



### 2. windows

### 3. mac
