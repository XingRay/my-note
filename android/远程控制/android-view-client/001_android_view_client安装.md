# android view client 安装

https://darpan.blog/code/setup-androidviewclient-windows/

https://github.com/dtmilano/AndroidViewClient/wiki



```shell
# 安装本体app
adb -s 29061FDH3007C7 install .\app-debug.apk
# 安装测试app
adb -s 29061FDH3007C7 install .\app-debug-androidTest.apk

# 授权
adb -s 29061FDH3007C7 shell pm grant "com.dtmilano.android.culebratester2" 'android.permission.DUMP'
adb -s 29061FDH3007C7 shell pm grant "com.dtmilano.android.culebratester2" 'android.permission.PACKAGE_USAGE_STATS'
adb -s 29061FDH3007C7 shell pm grant "com.dtmilano.android.culebratester2" 'android.permission.CHANGE_CONFIGURATION'

#启动测试
adb -s 29061FDH3007C7 shell am instrument -w -r -e debug false -e class "com.dtmilano.android.culebratester2.UiAutomatorHelper" "com.dtmilano.android.culebratester2.test/androidx.test.runner.AndroidJUnitRunner"

# 端口转发
# 如果连接多台设备, 不同设备的本机端口可以变化 
# 第一个是本地端口,可以自行设置, 后面访问本地http接口需要使用这个
# 第二个是设备端口, 由测试app监听, 如果要修改则需要修改app源码
adb -s 29061FDH3007C7 forward tcp:9987 tcp:9987
```



通过本机接口访问:

```shell
# 这里端口由上面转发命令决定
http://localhost:9987/
# 如:
http://localhost:9987/v2/uiDevice/screenshot
```

