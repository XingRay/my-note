## idea 设置



设置terminal编码问题

编写脚本，名为 cmd-utf8.cmd

```cmd
 chcp 65001 >nul
 call %SystemRoot%\System32\cmd.exe
```

保存在指定路径，如

```cmd
D:\softwares\windows\scripts
```

打开idea设置，Tools > Terminal >Application Settings

Shell path: `D:\softwares\windows\scripts\cmd-utf8.cmd`

其中 `chcp` 可以查看和指定字符集 65001 为utf8， 936 为GBK
