# Ubuntu系统对idea等软件设置桌面快捷方式

我们在Ubuntu上安装例如idea，pycharm的时候会遇到每次打开需要进入安装的目录bin目录下进行./***.sh操作
我们在桌面创建快捷方式
打开终端

```
tom@ubuntu-desktop:~/Desktop$ pwd
/home/tom/Desktop
```

创建快捷方式

```shell
touch idea.desktop
```

在桌面创建好以后，进入文件进行编辑

编辑此文件

```
vi idea.desktop
```

按A进入编辑，然后复制下面的东西, 添加以下内容

```toml
[Desktop Entry]
Name=IntelliJ IDEA
Comment=IntelliJ IDEA
Exec=/home/bin/idea.sh
#此处是你启动的 ./**.sh的路径
Icon=/home/bin/idea.png
#此处是你的文件解压后自带的图标的位置
Terminal=false
Type=Application
Categories=Developer;
```

！WQ退出
然后给文件权限

```shell
chmod u+x idea.desktop
```

进入ubuntu桌面, 在图标上点击右键菜单-> Allow Launching



idea等一系列的文件操作流程如出一辙，我在编辑postman的时候遇到了文件里面的编写东西有一些改变，没有深究其中原理，搬来用

```toml
[Desktop Entry]
Encoding=UTF-8
Name=postman
Exec=/media/Postman/Postman
Icon=/media/Postman/app/resources/app/assets/icon.png
Terminal=false
Type=Application
Categories=Development;
```