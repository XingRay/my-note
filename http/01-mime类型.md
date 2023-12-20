# MIME 类型

MIME (Multipurpose Internet Mail Extensions) 是描述消息内容类型的标准，用来表示文档、文件或字节流的性质和格式。

MIME 消息能包含文本、图像、音频、视频以及其他应用程序专用的数据。

浏览器通常使用 MIME 类型（而不是文件扩展名）来确定如何处理URL，因此 We b服务器在响应头中添加正确的 MIME 类型非常重要。如果配置不正确，浏览器可能会无法解析文件内容，网站将无法正常工作，并且下载的文件也会被错误处理。

### 语法

MIME 类型通用结构：

```
type/subtype
```

MIME 的组成结构非常简单，由类型与子类型两个字符串中间用 **/** 分隔而组成，不允许有空格。type 表示可以被分多个子类的独立类别，subtype 表示细分后的每个类型。

MIME类型对大小写不敏感，但是传统写法都是小写。

两种主要的 MIME 类型在默认类型中扮演了重要的角色：

- **text/plain** 表示文本文件的默认值。
- **application/octet-stream** 表示所有其他情况的默认值。

### 常见的 MIME 类型

- 超文本标记语言文本 **.html、.html**：**text/html**
- 普通文本 **.txt**： **text/plain**
- RTF 文本 **.rtf**： **application/rtf**
- GIF 图形 **.gif**： **image/gif**
- JPEG 图形 **.jpeg、.jpg**： **image/jpeg**
- au 声音文件 **.au**： **audio/basic**
- MIDI 音乐文件 **mid、.midi**： **audio/midi、audio/x-midi**
- RealAudio 音乐文件 **.ra、.ram**： **audio/x-pn-realaudio**
- MPEG 文件 **.mpg、.mpeg**： **video/mpeg**
- AVI 文件 **.avi**： **video/x-msvideo**
- GZIP 文件 **.gz**： **application/x-gzip**
- TAR 文件 **.tar**： **application/x-tar**

| 类型          | 描述                                                         | 典型示例                                                     |
| :------------ | :----------------------------------------------------------- | :----------------------------------------------------------- |
| `text`        | 表明文件是普通文本，理论上是人类可读                         | `text/plain`, `text/html`, `text/css, text/javascript`       |
| `image`       | 表明是某种图像。不包括视频，但是动态图（比如动态gif）也使用image类型 | `image/gif`, `image/png`, `image/jpeg`, `image/bmp`, `image/webp`, `image/x-icon`, `image/vnd.microsoft.icon` |
| `audio`       | 表明是某种音频文件                                           | `audio/midi`, `audio/mpeg, audio/webm, audio/ogg, audio/wav` |
| `video`       | 表明是某种视频文件                                           | `video/webm`, `video/ogg`                                    |
| `application` | 表明是某种二进制数据                                         | `application/octet-stream`, `application/pkcs12`, `application/vnd.mspowerpoint`, `application/xhtml+xml`, `application/xml`, `application/pdf` |

## MIME 对照表

| 媒体类型                                                     | 文件扩展名             | 说明                                                         |
| :----------------------------------------------------------- | :--------------------- | :----------------------------------------------------------- |
| **application/msword**                                       | doc                    | 微软 Office Word 格式（Microsoft Word 97 - 2004 document）   |
| **application/vnd.openxmlformats-officedocument.wordprocessingml.document** | docx                   | 微软 Office Word 文档格式                                    |
| **application/vnd.ms-excel**                                 | xls                    | 微软 Office Excel 格式（Microsoft Excel 97 - 2004 Workbook   |
| **application/vnd.openxmlformats-officedocument.spreadsheetml.sheet** | xlsx                   | 微软 Office Excel 文档格式                                   |
| **application/vnd.ms-powerpoint**                            | ppt                    | 微软 Office PowerPoint 格式（Microsoft PowerPoint 97 - 2003 演示文稿） |
| **application/vnd.openxmlformats-officedocument.presentationml.presentation** | pptx                   | 微软 Office PowerPoint 文稿格式                              |
| **application/x-gzip**                                       | gz, gzip               | GZ 压缩文件格式                                              |
| **application/zip**                                          | zip, 7zip              | ZIP 压缩文件格式                                             |
| **application/rar**                                          | rar                    | RAR 压缩文件格式                                             |
| **application/x-tar**                                        | tar, tgz               | TAR 压缩文件格式                                             |
| **application/pdf**                                          | pdf                    | PDF 是 Portable Document Format 的简称，即便携式文档格式     |
| **application/rtf**                                          | rtf                    | RTF 是指 Rich Text Format，即通常所说的富文本格式            |
| **image/gif**                                                | gif                    | GIF 图像格式                                                 |
| **image/jpeg**                                               | jpg, jpeg              | JPG(JPEG) 图像格式                                           |
| **image/jp2**                                                | jpg2                   | JPG2 图像格式                                                |
| **image/png**                                                | png                    | PNG 图像格式                                                 |
| **image/tiff**                                               | tif, tiff              | TIF(TIFF) 图像格式                                           |
| **image/bmp**                                                | bmp                    | BMP 图像格式（位图格式）                                     |
| **image/svg+xml**                                            | svg, svgz              | SVG 图像格式                                                 |
| **image/webp**                                               | webp                   | WebP 图像格式                                                |
| **image/x-icon**                                             | ico                    | ico 图像格式，通常用于浏览器 Favicon 图标                    |
| **application/kswps**                                        | wps                    | 金山 Office 文字排版文件格式                                 |
| **application/kset**                                         | et                     | 金山 Office 表格文件格式                                     |
| **application/ksdps**                                        | dps                    | 金山 Office 演示文稿格式                                     |
| **application/x-photoshop**                                  | psd                    | Photoshop 源文件格式                                         |
| **application/x-coreldraw**                                  | cdr                    | Coreldraw 源文件格式                                         |
| **application/x-shockwave-flash**                            | swf                    | Adobe Flash 源文件格式                                       |
| **text/plain**                                               | txt                    | 普通文本格式                                                 |
| **application/x-javascript**                                 | js                     | Javascript 文件类型                                          |
| **text/javascript**                                          | js                     | 表示 Javascript 脚本文件                                     |
| **text/css**                                                 | css                    | 表示 CSS 样式表                                              |
| **text/html**                                                | htm, html, shtml       | HTML 文件格式                                                |
| **application/xhtml+xml**                                    | xht, xhtml             | XHTML 文件格式                                               |
| **text/xml**                                                 | xml                    | XML 文件格式                                                 |
| **text/x-vcard**                                             | vcf                    | VCF 文件格式                                                 |
| **application/x-httpd-php**                                  | php, php3, php4, phtml | PHP 文件格式                                                 |
| **application/java-archive**                                 | jar                    | Java 归档文件格式                                            |
| **application/vnd.android.package-archive**                  | apk                    | Android 平台包文件格式                                       |
| **application/octet-stream**                                 | exe                    | Windows 系统可执行文件格式                                   |
| **application/x-x509-user-cert**                             | crt, pem               | PEM 文件格式                                                 |
| **audio/mpeg**                                               | mp3                    | mpeg 音频格式                                                |
| **audio/midi**                                               | mid, midi              | mid 音频格式                                                 |
| **audio/x-wav**                                              | wav                    | wav 音频格式                                                 |
| **audio/x-mpegurl**                                          | m3u                    | m3u 音频格式                                                 |
| **audio/x-m4a**                                              | m4a                    | m4a 音频格式                                                 |
| **audio/ogg**                                                | ogg                    | ogg 音频格式                                                 |
| **audio/x-realaudio**                                        | ra                     | Real Audio 音频格式                                          |
| **video/mp4**                                                | mp4                    | mp4 视频格式                                                 |
| **video/mpeg**                                               | mpg, mpe, mpeg         | mpeg 视频格式                                                |
| **video/quicktime**                                          | qt, mov                | QuickTime 视频格式                                           |
| **video/x-m4v**                                              | m4v                    | m4v 视频格式                                                 |
| **video/x-ms-wmv**                                           | wmv                    | wmv 视频格式（Windows 操作系统上的一种视频格式）             |
| **video/x-msvideo**                                          | avi                    | avi 视频格式                                                 |
| **video/webm**                                               | webm                   | webm 视频格式                                                |
| **video/x-flv**                                              | flv                    | 一种基于 flash 技术的视频格式                                |

### 按照内容类型排列的 MIME 类型列表

| 类型/子类型                             | 扩展名  |
| :-------------------------------------- | :------ |
| application/envoy                       | evy     |
| application/fractals                    | fif     |
| application/futuresplash                | spl     |
| application/hta                         | hta     |
| application/internet-property-stream    | acx     |
| application/mac-binhex40                | hqx     |
| application/msword                      | doc     |
| application/msword                      | dot     |
| application/octet-stream                | *       |
| application/octet-stream                | bin     |
| application/octet-stream                | class   |
| application/octet-stream                | dms     |
| application/octet-stream                | exe     |
| application/octet-stream                | lha     |
| application/octet-stream                | lzh     |
| application/oda                         | oda     |
| application/olescript                   | axs     |
| application/pdf                         | pdf     |
| application/pics-rules                  | prf     |
| application/pkcs10                      | p10     |
| application/pkix-crl                    | crl     |
| application/postscript                  | ai      |
| application/postscript                  | eps     |
| application/postscript                  | ps      |
| application/rtf                         | rtf     |
| application/set-payment-initiation      | setpay  |
| application/set-registration-initiation | setreg  |
| application/vnd.ms-excel                | xla     |
| application/vnd.ms-excel                | xlc     |
| application/vnd.ms-excel                | xlm     |
| application/vnd.ms-excel                | xls     |
| application/vnd.ms-excel                | xlt     |
| application/vnd.ms-excel                | xlw     |
| application/vnd.ms-outlook              | msg     |
| application/vnd.ms-pkicertstore         | sst     |
| application/vnd.ms-pkiseccat            | cat     |
| application/vnd.ms-pkistl               | stl     |
| application/vnd.ms-powerpoint           | pot     |
| application/vnd.ms-powerpoint           | pps     |
| application/vnd.ms-powerpoint           | ppt     |
| application/vnd.ms-project              | mpp     |
| application/vnd.ms-works                | wcm     |
| application/vnd.ms-works                | wdb     |
| application/vnd.ms-works                | wks     |
| application/vnd.ms-works                | wps     |
| application/winhlp                      | hlp     |
| application/x-bcpio                     | bcpio   |
| application/x-cdf                       | cdf     |
| application/x-compress                  | z       |
| application/x-compressed                | tgz     |
| application/x-cpio                      | cpio    |
| application/x-csh                       | csh     |
| application/x-director                  | dcr     |
| application/x-director                  | dir     |
| application/x-director                  | dxr     |
| application/x-dvi                       | dvi     |
| application/x-gtar                      | gtar    |
| application/x-gzip                      | gz      |
| application/x-hdf                       | hdf     |
| application/x-internet-signup           | ins     |
| application/x-internet-signup           | isp     |
| application/x-iphone                    | iii     |
| application/x-javascript                | js      |
| application/x-latex                     | latex   |
| application/x-msaccess                  | mdb     |
| application/x-mscardfile                | crd     |
| application/x-msclip                    | clp     |
| application/x-msdownload                | dll     |
| application/x-msmediaview               | m13     |
| application/x-msmediaview               | m14     |
| application/x-msmediaview               | mvb     |
| application/x-msmetafile                | wmf     |
| application/x-msmoney                   | mny     |
| application/x-mspublisher               | pub     |
| application/x-msschedule                | scd     |
| application/x-msterminal                | trm     |
| application/x-mswrite                   | wri     |
| application/x-netcdf                    | cdf     |
| application/x-netcdf                    | nc      |
| application/x-perfmon                   | pma     |
| application/x-perfmon                   | pmc     |
| application/x-perfmon                   | pml     |
| application/x-perfmon                   | pmr     |
| application/x-perfmon                   | pmw     |
| application/x-pkcs12                    | p12     |
| application/x-pkcs12                    | pfx     |
| application/x-pkcs7-certificates        | p7b     |
| application/x-pkcs7-certificates        | spc     |
| application/x-pkcs7-certreqresp         | p7r     |
| application/x-pkcs7-mime                | p7c     |
| application/x-pkcs7-mime                | p7m     |
| application/x-pkcs7-signature           | p7s     |
| application/x-sh                        | sh      |
| application/x-shar                      | shar    |
| application/x-shockwave-flash           | swf     |
| application/x-stuffit                   | sit     |
| application/x-sv4cpio                   | sv4cpio |
| application/x-sv4crc                    | sv4crc  |
| application/x-tar                       | tar     |
| application/x-tcl                       | tcl     |
| application/x-tex                       | tex     |
| application/x-texinfo                   | texi    |
| application/x-texinfo                   | texinfo |
| application/x-troff                     | roff    |
| application/x-troff                     | t       |
| application/x-troff                     | tr      |
| application/x-troff-man                 | man     |
| application/x-troff-me                  | me      |
| application/x-troff-ms                  | ms      |
| application/x-ustar                     | ustar   |
| application/x-wais-source               | src     |
| application/x-x509-ca-cert              | cer     |
| application/x-x509-ca-cert              | crt     |
| application/x-x509-ca-cert              | der     |
| application/ynd.ms-pkipko               | pko     |
| application/zip                         | zip     |
| audio/basic                             | au      |
| audio/basic                             | snd     |
| audio/mid                               | mid     |
| audio/mid                               | rmi     |
| audio/mpeg                              | mp3     |
| audio/x-aiff                            | aif     |
| audio/x-aiff                            | aifc    |
| audio/x-aiff                            | aiff    |
| audio/x-mpegurl                         | m3u     |
| audio/x-pn-realaudio                    | ra      |
| audio/x-pn-realaudio                    | ram     |
| audio/x-wav                             | wav     |
| image/bmp                               | bmp     |
| image/cis-cod                           | cod     |
| image/gif                               | gif     |
| image/ief                               | ief     |
| image/jpeg                              | jpe     |
| image/jpeg                              | jpeg    |
| image/jpeg                              | jpg     |
| image/pipeg                             | jfif    |
| image/svg+xml                           | svg     |
| image/tiff                              | tif     |
| image/tiff                              | tiff    |
| image/x-cmu-raster                      | ras     |
| image/x-cmx                             | cmx     |
| image/x-icon                            | ico     |
| image/x-portable-anymap                 | pnm     |
| image/x-portable-bitmap                 | pbm     |
| image/x-portable-graymap                | pgm     |
| image/x-portable-pixmap                 | ppm     |
| image/x-rgb                             | rgb     |
| image/x-xbitmap                         | xbm     |
| image/x-xpixmap                         | xpm     |
| image/x-xwindowdump                     | xwd     |
| message/rfc822                          | mht     |
| message/rfc822                          | mhtml   |
| message/rfc822                          | nws     |
| text/css                                | css     |
| text/h323                               | 323     |
| text/html                               | htm     |
| text/html                               | html    |
| text/html                               | stm     |
| text/iuls                               | uls     |
| text/plain                              | bas     |
| text/plain                              | c       |
| text/plain                              | h       |
| text/plain                              | txt     |
| text/richtext                           | rtx     |
| text/scriptlet                          | sct     |
| text/tab-separated-values               | tsv     |
| text/webviewhtml                        | htt     |
| text/x-component                        | htc     |
| text/x-setext                           | etx     |
| text/x-vcard                            | vcf     |
| video/mpeg                              | mp2     |
| video/mpeg                              | mpa     |
| video/mpeg                              | mpe     |
| video/mpeg                              | mpeg    |
| video/mpeg                              | mpg     |
| video/mpeg                              | mpv2    |
| video/quicktime                         | mov     |
| video/quicktime                         | qt      |
| video/x-la-asf                          | lsf     |
| video/x-la-asf                          | lsx     |
| video/x-ms-asf                          | asf     |
| video/x-ms-asf                          | asr     |
| video/x-ms-asf                          | asx     |
| video/x-msvideo                         | avi     |
| video/x-sgi-movie                       | movie   |
| x-world/x-vrml                          | flr     |
| x-world/x-vrml                          | vrml    |
| x-world/x-vrml                          | wrl     |
| x-world/x-vrml                          | wrz     |
| x-world/x-vrml                          | xaf     |
| x-world/x-vrml                          | xof     |

### 按照文件扩展名排列的 MIME 类型列表

| 扩展名  | 类型/子类型                             |
| :------ | :-------------------------------------- |
|         | application/octet-stream                |
| 323     | text/h323                               |
| acx     | application/internet-property-stream    |
| ai      | application/postscript                  |
| aif     | audio/x-aiff                            |
| aifc    | audio/x-aiff                            |
| aiff    | audio/x-aiff                            |
| asf     | video/x-ms-asf                          |
| asr     | video/x-ms-asf                          |
| asx     | video/x-ms-asf                          |
| au      | audio/basic                             |
| avi     | video/x-msvideo                         |
| axs     | application/olescript                   |
| bas     | text/plain                              |
| bcpio   | application/x-bcpio                     |
| bin     | application/octet-stream                |
| bmp     | image/bmp                               |
| c       | text/plain                              |
| cat     | application/vnd.ms-pkiseccat            |
| cdf     | application/x-cdf                       |
| cer     | application/x-x509-ca-cert              |
| class   | application/octet-stream                |
| clp     | application/x-msclip                    |
| cmx     | image/x-cmx                             |
| cod     | image/cis-cod                           |
| cpio    | application/x-cpio                      |
| crd     | application/x-mscardfile                |
| crl     | application/pkix-crl                    |
| crt     | application/x-x509-ca-cert              |
| csh     | application/x-csh                       |
| css     | text/css                                |
| dcr     | application/x-director                  |
| der     | application/x-x509-ca-cert              |
| dir     | application/x-director                  |
| dll     | application/x-msdownload                |
| dms     | application/octet-stream                |
| doc     | application/msword                      |
| dot     | application/msword                      |
| dvi     | application/x-dvi                       |
| dxr     | application/x-director                  |
| eps     | application/postscript                  |
| etx     | text/x-setext                           |
| evy     | application/envoy                       |
| exe     | application/octet-stream                |
| fif     | application/fractals                    |
| flr     | x-world/x-vrml                          |
| gif     | image/gif                               |
| gtar    | application/x-gtar                      |
| gz      | application/x-gzip                      |
| h       | text/plain                              |
| hdf     | application/x-hdf                       |
| hlp     | application/winhlp                      |
| hqx     | application/mac-binhex40                |
| hta     | application/hta                         |
| htc     | text/x-component                        |
| htm     | text/html                               |
| html    | text/html                               |
| htt     | text/webviewhtml                        |
| ico     | image/x-icon                            |
| ief     | image/ief                               |
| iii     | application/x-iphone                    |
| ins     | application/x-internet-signup           |
| isp     | application/x-internet-signup           |
| jfif    | image/pipeg                             |
| jpe     | image/jpeg                              |
| jpeg    | image/jpeg                              |
| jpg     | image/jpeg                              |
| js      | application/x-javascript                |
| latex   | application/x-latex                     |
| lha     | application/octet-stream                |
| lsf     | video/x-la-asf                          |
| lsx     | video/x-la-asf                          |
| lzh     | application/octet-stream                |
| m13     | application/x-msmediaview               |
| m14     | application/x-msmediaview               |
| m3u     | audio/x-mpegurl                         |
| man     | application/x-troff-man                 |
| mdb     | application/x-msaccess                  |
| me      | application/x-troff-me                  |
| mht     | message/rfc822                          |
| mhtml   | message/rfc822                          |
| mid     | audio/mid                               |
| mny     | application/x-msmoney                   |
| mov     | video/quicktime                         |
| movie   | video/x-sgi-movie                       |
| mp2     | video/mpeg                              |
| mp3     | audio/mpeg                              |
| mpa     | video/mpeg                              |
| mpe     | video/mpeg                              |
| mpeg    | video/mpeg                              |
| mpg     | video/mpeg                              |
| mpp     | application/vnd.ms-project              |
| mpv2    | video/mpeg                              |
| ms      | application/x-troff-ms                  |
| mvb     | application/x-msmediaview               |
| nws     | message/rfc822                          |
| oda     | application/oda                         |
| p10     | application/pkcs10                      |
| p12     | application/x-pkcs12                    |
| p7b     | application/x-pkcs7-certificates        |
| p7c     | application/x-pkcs7-mime                |
| p7m     | application/x-pkcs7-mime                |
| p7r     | application/x-pkcs7-certreqresp         |
| p7s     | application/x-pkcs7-signature           |
| pbm     | image/x-portable-bitmap                 |
| pdf     | application/pdf                         |
| pfx     | application/x-pkcs12                    |
| pgm     | image/x-portable-graymap                |
| pko     | application/ynd.ms-pkipko               |
| pma     | application/x-perfmon                   |
| pmc     | application/x-perfmon                   |
| pml     | application/x-perfmon                   |
| pmr     | application/x-perfmon                   |
| pmw     | application/x-perfmon                   |
| pnm     | image/x-portable-anymap                 |
| pot,    | application/vnd.ms-powerpoint           |
| ppm     | image/x-portable-pixmap                 |
| pps     | application/vnd.ms-powerpoint           |
| ppt     | application/vnd.ms-powerpoint           |
| prf     | application/pics-rules                  |
| ps      | application/postscript                  |
| pub     | application/x-mspublisher               |
| qt      | video/quicktime                         |
| ra      | audio/x-pn-realaudio                    |
| ram     | audio/x-pn-realaudio                    |
| ras     | image/x-cmu-raster                      |
| rgb     | image/x-rgb                             |
| rmi     | audio/mid                               |
| roff    | application/x-troff                     |
| rtf     | application/rtf                         |
| rtx     | text/richtext                           |
| scd     | application/x-msschedule                |
| sct     | text/scriptlet                          |
| setpay  | application/set-payment-initiation      |
| setreg  | application/set-registration-initiation |
| sh      | application/x-sh                        |
| shar    | application/x-shar                      |
| sit     | application/x-stuffit                   |
| snd     | audio/basic                             |
| spc     | application/x-pkcs7-certificates        |
| spl     | application/futuresplash                |
| src     | application/x-wais-source               |
| sst     | application/vnd.ms-pkicertstore         |
| stl     | application/vnd.ms-pkistl               |
| stm     | text/html                               |
| svg     | image/svg+xml                           |
| sv4cpio | application/x-sv4cpio                   |
| sv4crc  | application/x-sv4crc                    |
| swf     | application/x-shockwave-flash           |
| t       | application/x-troff                     |
| tar     | application/x-tar                       |
| tcl     | application/x-tcl                       |
| tex     | application/x-tex                       |
| texi    | application/x-texinfo                   |
| texinfo | application/x-texinfo                   |
| tgz     | application/x-compressed                |
| tif     | image/tiff                              |
| tiff    | image/tiff                              |
| tr      | application/x-troff                     |
| trm     | application/x-msterminal                |
| tsv     | text/tab-separated-values               |
| txt     | text/plain                              |
| uls     | text/iuls                               |
| ustar   | application/x-ustar                     |
| vcf     | text/x-vcard                            |
| vrml    | x-world/x-vrml                          |
| wav     | audio/x-wav                             |
| wcm     | application/vnd.ms-works                |
| wdb     | application/vnd.ms-works                |
| wks     | application/vnd.ms-works                |
| wmf     | application/x-msmetafile                |
| wps     | application/vnd.ms-works                |
| wri     | application/x-mswrite                   |
| wrl     | x-world/x-vrml                          |
| wrz     | x-world/x-vrml                          |
| xaf     | x-world/x-vrml                          |
| xbm     | image/x-xbitmap                         |
| xla     | application/vnd.ms-excel                |
| xlc     | application/vnd.ms-excel                |
| xlm     | application/vnd.ms-excel                |
| xls     | application/vnd.ms-excel                |
| xlt     | application/vnd.ms-excel                |
| xlw     | application/vnd.ms-excel                |
| xof     | x-world/x-vrml                          |
| xpm     | image/x-xpixmap                         |
| xwd     | image/x-xwindowdump                     |
| z       | application/x-compress                  |
| zip     | application/zip                         |