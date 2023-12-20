## VisualVM入门

http://visualvm.github.io/



### 1.下载安装

http://visualvm.github.io/download.html

下载后解压到指定文件夹，需要预先安装 **OracleJDK**，运行指令运行：

```bash
D:\develop\java\visualvm\visualvm_216\bin\visualvm.exe  --jdkhome "D:\develop\java\jdk\oracle\jdk-20"
```



### 2.安装插件

运行后在菜单上点击 tools  -> plugins 在 Avariable Plugins 选择插件进行安装，选择安装 VisualGC插件



### 3.连接Java应用

在左侧 Applications中双击要连接的应用名，上面的tab栏中有各个监控信息的tab

其中就有VisualGC提供的 VisualGC 视图页。

在visualGC页可以看到虚拟机的GC信息，包括 MetaSpace OldGen Survivor 1 Survivor0 Eden 各个区的信息

可以在 `Eden Space` 和 `Old Gen` 中可以看到 **yongGC的占用时间**（从启动开始计算）和 **FullGC的占用时间**

