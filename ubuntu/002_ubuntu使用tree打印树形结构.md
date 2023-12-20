# ubuntu使用tree打印树形结构

ubuntu 使用 tree 命令能打印目录结构

```bash
sudo apt-get install tree 
```

安装后使用tree就行了

```
.
├── index.php
├── phpQuery
│   ├── phpQuery
│   │   ├── bootstrap.example.php
│   │   ├── Callback.php
│   │   ├── compat
│   │   │   └── mbstring.php
│   │   ├── DOMDocumentWrapper.php
│   │   ├── DOMEvent.php
│   │   ├── phpQueryEvents.php
│   │   ├── phpQueryObject.php
│   │   ├── plugins
│   │   │   ├── example.php
│   │   │   ├── Scripts
│   │   │   │   ├── __config.example.php
│   │   │   │   ├── example.php
│   │   │   │   ├── fix_webroot.php
│   │   │   │   ├── google_login.php
│   │   │   │   ├── print_source.php
│   │   │   │   └── print_websafe.php
│   │   │   ├── Scripts.php
│   │   │   └── WebBrowser.php
│   │   └── Zend
│   │       ├── Exception.php
│   │       ├── Http
│   │       │   ├── Client
│   │       │   │   ├── Adapter
│   │       │   │   │   ├── Exception.php
│   │       │   │   │   ├── Interface.php
│   │       │   │   │   ├── Proxy.php
│   │       │   │   │   ├── Socket.php
│   │       │   │   │   └── Test.php
│   │       │   │   └── Exception.php
│   │       │   ├── Client.php
│   │       │   ├── CookieJar.php
│   │       │   ├── Cookie.php
│   │       │   ├── Exception.php
│   │       │   └── Response.php
│   │       ├── Json
│   │       │   ├── Decoder.php
│   │       │   ├── Encoder.php
│   │       │   └── Exception.php
│   │       ├── Loader.php
│   │       ├── Registry.php
│   │       ├── Uri
│   │       │   ├── Exception.php
│   │       │   └── Http.php
│   │       ├── Uri.php
│   │       └── Validate
│   │           ├── Abstract.php
│   │           ├── Alnum.php
│   │           ├── Alpha.php
│   │           ├── Barcode
│   │           │   ├── Ean13.php
│   │           │   └── UpcA.php
│   │           ├── Barcode.php
│   │           ├── Between.php
│   │           ├── Ccnum.php
│   │           ├── Date.php
│   │           ├── Digits.php
│   │           ├── EmailAddress.php
│   │           ├── Exception.php
│   │           ├── File
│   │           │   ├── Count.php
│   │           │   ├── Exists.php
│   │           │   ├── Extension.php
│   │           │   ├── FilesSize.php
│   │           │   ├── ImageSize.php
│   │           │   ├── MimeType.php
│   │           │   ├── NotExists.php
│   │           │   ├── Size.php
│   │           │   └── Upload.php
│   │           ├── Float.php
│   │           ├── GreaterThan.php
│   │           ├── Hex.php
│   │           ├── Hostname
│   │           │   ├── At.php
│   │           │   ├── Ch.php
│   │           │   ├── De.php
│   │           │   ├── Fi.php
│   │           │   ├── Hu.php
│   │           │   ├── Interface.php
│   │           │   ├── Li.php
│   │           │   ├── No.php
│   │           │   └── Se.php
│   │           ├── Hostname.php
│   │           ├── Identical.php
│   │           ├── InArray.php
│   │           ├── Interface.php
│   │           ├── Int.php
│   │           ├── Ip.php
│   │           ├── LessThan.php
│   │           ├── NotEmpty.php
│   │           ├── Regex.php
│   │           └── StringLength.php
│   └── phpQuery.php
├── rank.html
└── score.txt

15 directories, 83 files
```