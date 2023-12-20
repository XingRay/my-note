# Windows常用命令

展示目录树

```cmd
tree /f
```

输出：

```cmd
D:.
│  .gitignore
│  graalvm-demo.iml
│  native-image-01-helloworld.iml
│  native-image-02-jarfile.iml
│  pom.xml
│  readme.md
│
├─.idea
│      compiler.xml
│      encodings.xml
│      jarRepositories.xml
│      misc.xml
│      modules.xml
│      vcs.xml
│      workspace.xml
│
├─native-image
│  │  native-image.iml
│  │  pom.xml
│  │
│  ├─native-image-01-helloworld
│  │      build.cmd
│  │      HelloWorld.java
│  │
│  └─native-image-02-jarfile
│      │  build.bat
│      │  pom.xml
│      │
│      └─src
│          ├─main
│          │  ├─java
│          │  │  │  module-info.java
│          │  │  │
│          │  │  └─com
│          │  │      └─xingray
│          │  │          └─nativeimage
│          │  │              └─jarfile
│          │  │                      HelloWorld.java
│          │  │
│          │  └─resources
│          └─test
│              └─java
└─note
        01-graalvm入门.md

```



打开系统属性/高级 面板

```cmd
SystemPropertiesAdvanced
```



修改镜像为widows程序

```bash
EDITBIN /SUBSYSTEM:WINDOWS target\launcher.exe
```
