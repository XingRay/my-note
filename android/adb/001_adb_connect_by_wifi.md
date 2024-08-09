# adb connect by wifi

```
adb kill-server
```

```
adb start-server
```

```
adb usb
```

```
adb tcpip 5555
```

```
adb connect 192.168.10.1:5555
```

```
 adb devices
```



##### On your Android device

_(assuming you have already enabled developer options)_

1. Go to Settings
2. Select `System`
3. Select `Developer Options`
4. Enable `USB Debugging` and `Wireless Debugging`
5. Select `Wireless Debugging`
6. Select `Pair device using a pairing code`
7. Note the **IP address** and the **port**

##### On your computer

_(assuming you have installed `adb` and have it on the path)_

1. Go to your command line app
2. Run `adb pair ip:port pairing-code`
3. Run `adb connect ip:port`

Your device is now connected via ADB's TCP/IP protocol. Now, if you go to Android Studio or other tools, you should be able to the see newly connected device.



```
adb -s 192.168.0.103:5555 shell input swipe 100 500 100 100
```

```
adb -s 192.168.0.103:5555 shell input tap 100 200
```



```
# 录制脚本示例

shell input swipe 100 200 300 400 1000
# 回放脚本示例
shell input-playback /sdcard/test.txt
```



录制事件

```
adb -s 192.168.0.103:5555 shell getevent -l > events.txt
```

