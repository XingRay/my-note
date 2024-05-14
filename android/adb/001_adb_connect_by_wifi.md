# adb connect by wifi

[ADB : unable to connect to 192.168.1.10:5555](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555)

[Ask Question](https://stackoverflow.com/questions/ask)

Asked 8 years, 6 months ago

Modified [4 months ago](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555?lastactivity "2023-12-26 19:48:12Z")

Viewed 258k times

 Part of [Mobile Development](https://stackoverflow.com/collectives/mobile-dev) Collective

87

[](https://stackoverflow.com/posts/33462720/timeline)

I cannot use `adb connect` to connect via WiFi to my device (Nexus 5, Android 6), but since I'm developing a cordova plugin using USB OTG, I really need to connect via WiFi.

I tried to `adb kill-server`, and all solutions provided on SO, and none is working for me... Before it was working ok (i.e android 5).

Any idea?

[](https://stackoverflow.com/collectives/mobile-dev)

- [android](https://stackoverflow.com/questions/tagged/android "show questions tagged 'android'")
- [adb](https://stackoverflow.com/questions/tagged/adb)

[Share](https://stackoverflow.com/q/33462720/8273792 "Short permalink to this question")

[Edit](https://stackoverflow.com/posts/33462720/edit "Revise and improve this post")

Follow

[edited Jul 31, 2021 at 16:03](https://stackoverflow.com/posts/33462720/revisions "show all edits to this post")

[

![auspicious99's user avatar](https://www.gravatar.com/avatar/a04df04c51da9c785c5d21f04e4d5dc1?s=64&d=identicon&r=PG)

](https://stackoverflow.com/users/1578867/auspicious99)

[auspicious99](https://stackoverflow.com/users/1578867/auspicious99)

4,22111 gold badge4646 silver badges6565 bronze badges

asked Nov 1, 2015 at 13:37

[

![xavier.seignard's user avatar](https://www.gravatar.com/avatar/25b428538acac597a983e72cc7c323fc?s=64&d=identicon&r=PG)

](https://stackoverflow.com/users/572543/xavier-seignard)

[xavier.seignard](https://stackoverflow.com/users/572543/xavier-seignard)

11k1313 gold badges5454 silver badges7777 bronze badges

- 4
  
    Your android device might have use another IP address. Double check it, and verify your adb is in tcpip mode `adb tcpip 5555` 
    
    – [Simon Marquis](https://stackoverflow.com/users/3615879/simon-marquis "7,426 reputation")
    
     [Nov 1, 2015 at 22:36](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment54721711_33462720)
    
- no, you can imagine everything is double checked 
  
    – [xavier.seignard](https://stackoverflow.com/users/572543/xavier-seignard "10,974 reputation")
    
     [Nov 2, 2015 at 6:21](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment54729112_33462720)
    
- In what state is your device's adb? Try to kill it (plug in usb or use a terminal app) 
  
    – [Simon Marquis](https://stackoverflow.com/users/3615879/simon-marquis "7,426 reputation")
    
     [Nov 2, 2015 at 9:54](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment54734968_33462720)
    
- 1
  
    What are you using to make your phone listen for adb connections? 
    
    – [Nanoc](https://stackoverflow.com/users/2260916/nanoc "2,381 reputation")
    
     [Nov 10, 2015 at 11:43](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment55031792_33462720)
    
- 2
  
    Try different port, e.g.: `adb kill-server && adb tcpip 5236 && adb connect 192.168.1.10:5236`. Make sure the IP address is correct, and your computer and the target device are on the same network. 
    
    – [ozbek](https://stackoverflow.com/users/1893766/ozbek "21,073 reputation")
    
     [Nov 11, 2015 at 12:24](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment55074368_33462720)
    

[Show **2** more comments](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Expand to show all comments on this post")

## 22 Answers

Sorted by:

                                              Highest score (default)                                                                   Trending (recent votes count more)                                                                   Date modified (newest first)                                                                   Date created (oldest first)                              

179

+50

[](https://stackoverflow.com/posts/33654987/timeline)

I had the same issue since the android 6 upgrading. I noticed that for some reason the device is playing "hard to get" when you try to contact it over WIFI.

Try these following steps:

1. Make sure that `Aggressive Wi-Fi to Cellular handover` under Networking section in the device's developer options is turned off.
   
2. ping continuously from your pc to the device to make sure it's not in network idle mode `ping -t 192.168.1.10` (windows cmd), unlock the device and even try to surf to some website just to make it get out of the network idle.
   
3. If ping doesn't work, turn off / on Android Wifi and go back to step 2.
   
4. When it replies to the ping, connect it via usb, and:
   
    `adb usb`
    
    `adb tcpip 5555`
    
    `adb connect 192.168.10.1:5555`
    
5. In case it's still not connected, try to switch the USB connection mode as MTP / PTP / Camera while the device is connected through USB and repeat these steps over again...
   

If the above points are not working please try running ADB as admin. For Ubuntu -

```go
`sudo adb start-server`
```

[Share](https://stackoverflow.com/a/33654987/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/33654987/edit "Revise and improve this post")

Follow

[edited Sep 21, 2021 at 8:13](https://stackoverflow.com/posts/33654987/revisions "show all edits to this post")

[

![HackRx's user avatar](https://www.gravatar.com/avatar/370fc6cf753a6b982e1940e5dbd56980?s=64&d=identicon&r=PG&f=y&so-version=2)

](https://stackoverflow.com/users/11902885/hackrx)

[HackRx](https://stackoverflow.com/users/11902885/hackrx)

30833 silver badges1414 bronze badges

answered Nov 11, 2015 at 16:20

[

![Eliran Kuta's user avatar](https://i.stack.imgur.com/OGu7i.png?s=64&g=1)

](https://stackoverflow.com/users/2746793/eliran-kuta)

[Eliran Kuta](https://stackoverflow.com/users/2746793/eliran-kuta)

4,25833 gold badges2525 silver badges2828 bronze badges

- 1
  
    Thanks Eliran Kuta The ping stuff did the trick! Weird issue though. 
    
    – [xavier.seignard](https://stackoverflow.com/users/572543/xavier-seignard "10,974 reputation")
    
     [Nov 12, 2015 at 22:59](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment55139015_33654987) 
    
- I made restart to the phone - and then the ping helped me. thanks. 
  
    – [STF](https://stackoverflow.com/users/5624602/stf "1,493 reputation")
    
     [Jan 9, 2018 at 9:07](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment83308387_33654987)
    
- 2
  
    step 4 was critical to solve the issue. I never tough that switching from MEDIA to Camera will solve the issue. but it did! 
    
    – [dsncode](https://stackoverflow.com/users/6318630/dsncode "2,451 reputation")
    
     [Apr 15, 2018 at 7:45](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment86693979_33654987)
    
- awesome!!! Had the same problem, just surfed a short while with my Samsung Galaxy, tried again - ping worked and tried to connect - voila :-) 
  
    – [Björn Hallström](https://stackoverflow.com/users/2365568/bj%c3%b6rn-hallstr%c3%b6m "3,805 reputation")
    
     [Jun 4, 2018 at 12:54](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment88371568_33654987)
    
- sudo adb start-server 
  
    – [Dipendra Sharma](https://stackoverflow.com/users/8886090/dipendra-sharma "2,462 reputation")
    
     [Jan 9, 2019 at 5:33](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment95039993_33654987)
    

[Show **5** more comments](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Expand to show all comments on this post")

46

[](https://stackoverflow.com/posts/68603189/timeline)

You need to pair your computer first.

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

[Share](https://stackoverflow.com/a/68603189/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/68603189/edit "Revise and improve this post")

Follow

[edited Dec 26, 2023 at 19:48](https://stackoverflow.com/posts/68603189/revisions "show all edits to this post")

[

![milosmns's user avatar](https://www.gravatar.com/avatar/2a1adcb8ed924ecd1045fd6252a7df18?s=64&d=identicon&r=PG)

](https://stackoverflow.com/users/2102748/milosmns)

[milosmns](https://stackoverflow.com/users/2102748/milosmns)

3,71444 gold badges3737 silver badges5050 bronze badges

answered Jul 31, 2021 at 14:53

[

![Think Big's user avatar](https://i.stack.imgur.com/qS47w.png?s=64&g=1)

](https://stackoverflow.com/users/5220303/think-big)

[Think Big](https://stackoverflow.com/users/5220303/think-big)

1,2611717 silver badges2424 bronze badges

- 3
  
    This is the way. 
    
    – [tazboy](https://stackoverflow.com/users/391162/tazboy "1,734 reputation")
    
     [Jul 4, 2022 at 14:09](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment128687579_68603189)
    
- 1
  
    Can confirm in 2022 this is the way I managed to fix it on Oneplus 8 
    
    – [Connor Brady](https://stackoverflow.com/users/11900299/connor-brady "77 reputation")
    
     [Sep 26, 2022 at 9:53](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment130405549_68603189)
    
- 2
  
    @connor-brady this is not related to the device , related to android version. Your android version of device must be greater than 11. [developer.android.com/studio/command-line/adb](https://developer.android.com/studio/command-line/adb) 
    
    – [Think Big](https://stackoverflow.com/users/5220303/think-big "1,261 reputation")
    
     [Sep 28, 2022 at 10:05](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment130451916_68603189)
    
- 1
  
    It worked, this is the correct one. 
    
    – [Khemraj Sharma](https://stackoverflow.com/users/6891563/khemraj-sharma "58,416 reputation")
    
     [Jul 26, 2023 at 19:00](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment135349626_68603189)
    
- 2
  
    Works for me as well in 2023 Thank You 
    
    – [Paras Rawat](https://stackoverflow.com/users/14254287/paras-rawat "61 reputation")
    
     [Aug 6, 2023 at 17:36](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment135475694_68603189)
    

[Show **2** more comments](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Expand to show all comments on this post")

15

[](https://stackoverflow.com/posts/60984409/timeline)

I had the same issue. i tried all commands like `adb kill-server` then `adb tcpip 5555` then `adb connect <IPAddress>:5555` but the issue remain same

the IP address which i used to connect ... showing me message `unable to connect .....`

what i did is go to phone's **settings**

then **About phone**

then **Status**`

then check **IP address**

Now try to connect phone with that **IP address**

**Note :** - The problem is the IP address changed which i used to connect

[Share](https://stackoverflow.com/a/60984409/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/60984409/edit "Revise and improve this post")

Follow

answered Apr 2, 2020 at 4:48

[

![vijay saini's user avatar](https://lh4.googleusercontent.com/-pfvSJvhQabY/AAAAAAAAAAI/AAAAAAAAAiY/Gkez1aqE8RM/photo.jpg?sz=64)

](https://stackoverflow.com/users/9521008/vijay-saini)

[vijay saini](https://stackoverflow.com/users/9521008/vijay-saini)

40155 silver badges66 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

12

[](https://stackoverflow.com/posts/61493361/timeline)

I had the same problem. The solution was as follows.

In Developer Options. + Activate "Allow ADB debugging in load only mode." + In Spanish, "Permitir depuración ADB en modo solo carga."

Explanation My problem was as follows: I was doing all the steps:

- adb kill-server
- adb start-server
- adb tcpip 5555
- adb connect (your ip).

After completing these steps, I disconnected the phone from the USB cable, and the connection was lost, I could not make the wireless connection.

However, this worked for me on a Huawei ALE-23 cell phone, but it did not work for me on the Huawei Y9S cell phone (Android 10), it failed.

I solved it only by activating the option "Allow ADB debugging in load only mode" in the cell Huawei Y9S.

Cheers!!!.

[Share](https://stackoverflow.com/a/61493361/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/61493361/edit "Revise and improve this post")

Follow

answered Apr 29, 2020 at 2:46

[

![IVAN MANJARREZ's user avatar](https://lh5.googleusercontent.com/-KXIZAqI5M5M/AAAAAAAAAAI/AAAAAAAAAAA/AAKWJJNAM0RouQNrNLppvxvWplphArZPrg/photo.jpg?sz=64)

](https://stackoverflow.com/users/13429879/ivan-manjarrez)

[IVAN MANJARREZ](https://stackoverflow.com/users/13429879/ivan-manjarrez)

12111 silver badge22 bronze badges

- "Allowing ADB debugging in load only mode" did the trick, Huawei P 30 Lite. Thanks! 
  
    – [Luigi Blu](https://stackoverflow.com/users/5764580/luigi-blu "129 reputation")
    
     [May 23, 2020 at 8:59](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment109603722_61493361)
    
- Yes, Activate "Allow ADB debugging in charge only mode." did the trick on Huawei Mate. 
  
    – [LXJ](https://stackoverflow.com/users/13834948/lxj "1,428 reputation")
    
     [Dec 12, 2021 at 17:38](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment124315949_61493361)
    

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

10

[](https://stackoverflow.com/posts/63622195/timeline)

Make sure you are not connected to a VPN. I was able to connect to adb as soon as I disconnected from the VPN. For a sure way to connect do this:

1. Unplug USB
2. Restart Android device
3. Shutdown Android Studio or any other IDE using ADB
4. `adb kill-server`
5. Plug back in USB after restart
6. `adb devices` This automatically starts the server. You sould see the device plugged in via USB
7. `adb shell ip addr show wlan0` to get your devices IP address
8. `adb tcpip 5555` Set the port to 5555 that you want to connect through
9. `adb connect 192.168.0.6:5555` Replace IP address with one from step 6.
10. Disconnect the USB

[Share](https://stackoverflow.com/a/63622195/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/63622195/edit "Revise and improve this post")

Follow

answered Aug 27, 2020 at 18:30

[

![Nick Gallimore's user avatar](https://i.stack.imgur.com/2V4D2.jpg?s=64&g=1)

](https://stackoverflow.com/users/3097454/nick-gallimore)

[Nick Gallimore](https://stackoverflow.com/users/3097454/nick-gallimore)

1,2431414 silver badges3535 bronze badges

- and if u get `failed to connect to '192.168.1.2:5555': Connection timed out`, switch off wifi on ur mobile and back on and try the connect command again 
  
    – [lordvcs](https://stackoverflow.com/users/2532763/lordvcs "2,724 reputation")
    
     [Oct 22, 2021 at 7:04](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment123151467_63622195)
    
- Worked like a charm - the only additional thing that I did was to keep the "Troubleshoot device connection issues" tab open on my Android studio while I was trying to connect to the device. All worked well. Thanks a lot. 
  
    – [arun](https://stackoverflow.com/users/15180061/arun "368 reputation")
    
     [Oct 29, 2021 at 0:30](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment123313141_63622195)
    

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

4

[](https://stackoverflow.com/posts/63353527/timeline)

I used the same approach as @IVAN MANJARREZ [ADB : unable to connect to 192.168.1.10:5555](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555/61493361#61493361)

The only difference was I used Huawei - p20 pro. Where for p20 - pro, you have to search for and activate "Allow ADB debugging in charge only mode"

[Share](https://stackoverflow.com/a/63353527/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/63353527/edit "Revise and improve this post")

Follow

answered Aug 11, 2020 at 7:33

[

![mladjo's user avatar](https://www.gravatar.com/avatar/d30d7eb0730e91635b043e039d87c152?s=64&d=identicon&r=PG&f=y&so-version=2)

](https://stackoverflow.com/users/5307049/mladjo)

[mladjo](https://stackoverflow.com/users/5307049/mladjo)

4111 bronze badge

- @xavier.seignard is asking specifically for suopport for the Nexus 5 device. If you'd like to link to another question, a better way would be a short comment on the question. 
  
    – [Jan Heinrich Reimer](https://stackoverflow.com/users/2037482/jan-heinrich-reimer "781 reputation")
    
     [Aug 11, 2020 at 11:32](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment112032487_63353527)
    
- Thanks mate it worked for my device **Honor Play** 
  
    – [Amit Kumar](https://stackoverflow.com/users/6186070/amit-kumar "603 reputation")
    
     [Apr 10, 2021 at 4:10](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment118484153_63353527)
    

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

4

[](https://stackoverflow.com/posts/64412791/timeline)

I solved this problem by disabling USB debugging and enabling it again

[Share](https://stackoverflow.com/a/64412791/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/64412791/edit "Revise and improve this post")

Follow

answered Oct 18, 2020 at 11:31

[

![Ramonzito's user avatar](https://lh6.googleusercontent.com/-ENMHVyvtS5o/AAAAAAAAAAI/AAAAAAAASC4/4mxu0yejTK8/photo.jpg?sz=64)

](https://stackoverflow.com/users/11832226/ramonzito)

[Ramonzito](https://stackoverflow.com/users/11832226/ramonzito)

8111 silver badge44 bronze badges

- 1
  
    That worked for me as well. Thanks. 
    
    – [Loathing](https://stackoverflow.com/users/904156/loathing "5,203 reputation")
    
     [Jan 13, 2022 at 13:41](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment124980558_64412791)
    

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

3

[](https://stackoverflow.com/posts/62947443/timeline)

I have tried everything but still, nothing worked for me. Then randomly, I used the following command:

> $ adb tcpip 5555
> 
> error: no devices/emulators found
> 
> $ adb connect 192.168.0.104:5555
> 
> failed to connect to 192.168.0.104:5555
> 
> $ adb connect 192.168.0.104:5555
> 
> already connected to 192.168.0.104:5555

It was connected at this point but my device was offline. I was always able to connect at the second attempt but my device was always offline. At this point, I connected my device to my PC with USB.

> $ adb tcpip 5555
> 
> error: more than one device/emulator
> 
> $ adb disconnect
> 
> disconnected everything
> 
> $ adb tcpip 5555
> 
> restarting in TCP mode port: 5555
> 
> $ adb connect 192.168.0.104:5555
> 
> connected to 192.168.0.104:5555

I disconnected my USB and voila! my device was still connected. I am sorry but I can't explain why it worked. I was randomly trying different things on internet. I had used the same commands several times but in different orders but they didn't work. I hope it will help someone.

[Share](https://stackoverflow.com/a/62947443/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/62947443/edit "Revise and improve this post")

Follow

answered Jul 17, 2020 at 5:08

[

![Shunan's user avatar](https://www.gravatar.com/avatar/aad1c0de1391305bf2975e1ca8fa138b?s=64&d=identicon&r=PG&f=y&so-version=2)

](https://stackoverflow.com/users/6390459/shunan)

[Shunan](https://stackoverflow.com/users/6390459/shunan)

3,22466 gold badges2828 silver badges5151 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

3

[](https://stackoverflow.com/posts/74441834/timeline)

I'll add my two cents.

For some devices, you can directly connect by the address and port as specified in the Wireless Debugging section e.g 192.155.230.241:43522

use `adb connect <IP:PORT>`

If not successful, use `adb connect <IP:5555>`

If still not successful, it probably means you will have to pair your device to your machine via adb first. This usually comes on some devices with above commands but if not then,

use `adb pair <IP:PORT>`

On Device > Developer Options > Wireless Debugging > select Pair device with pairing code

Enter this pairing code on console and device will pair.

Now use `adb connect <IP:PORT>`

Also note, You don't need USB debugging on or any cable connections first and TCPIP configuring with this way.

[Share](https://stackoverflow.com/a/74441834/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/74441834/edit "Revise and improve this post")

Follow

answered Nov 15, 2022 at 7:14

[

![PrasadW's user avatar](https://www.gravatar.com/avatar/ccdc6582d297b01883d160a227eca1bc?s=64&d=identicon&r=PG)

](https://stackoverflow.com/users/1531761/prasadw)

[PrasadW](https://stackoverflow.com/users/1531761/prasadw)

41777 silver badges1919 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

1

[](https://stackoverflow.com/posts/73936352/timeline)

Similar issue happened to me when **I tried to connect wirelessly** to my phone. I got this error:

```css
failed to connect to 192.168.1.187:42534
```

I tried to restart everything, phone, PC, adb server. The issue was simply that you have to **connect by cable first to authorize the client** and then you can unplag the cable and connect wirelessly.

[Share](https://stackoverflow.com/a/73936352/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/73936352/edit "Revise and improve this post")

Follow

answered Oct 3, 2022 at 13:42

[

![arenaq's user avatar](https://i.stack.imgur.com/NT8in.jpg?s=64&g=1)

](https://stackoverflow.com/users/2658949/arenaq)

[arenaq](https://stackoverflow.com/users/2658949/arenaq)

2,36222 gold badges2727 silver badges3333 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

1

[](https://stackoverflow.com/posts/72967601/timeline)

I had the same issue. i tried all commands like adb kill-server then adb tcpip 5555 then adb connect :5555 but the issue remain same

the IP address which i used to connect ... showing me message unable to connect .....

what i did is go to phone's **Settings**

then **About** phone

then **Status**`

then check IP address

Now try to connect phone with that IP address

Note : - The problem is the IP address changed which i used to connect

[Share](https://stackoverflow.com/a/72967601/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/72967601/edit "Revise and improve this post")

Follow

answered Jul 13, 2022 at 13:59

[

![dukizwe's user avatar](https://i.stack.imgur.com/5z97j.jpg?s=64&g=1)

](https://stackoverflow.com/users/13452723/dukizwe)

[dukizwe](https://stackoverflow.com/users/13452723/dukizwe)

39211 gold badge44 silver badges1010 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

1

[](https://stackoverflow.com/posts/57502075/timeline)

The critical step in getting this to work is disconnecting the usb cable after issuing the adb connect xx.x.x.xx:5555 command. At this point you are connected but unauthorized. Execute adb kill-server and re-issue the connect command. Verify with execution of adb shell date.

[Share](https://stackoverflow.com/a/57502075/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/57502075/edit "Revise and improve this post")

Follow

answered Aug 14, 2019 at 21:07

[

![michael masse's user avatar](https://www.gravatar.com/avatar/c5608421104afdec503e4e69acedce6e?s=64&d=identicon&r=PG)

](https://stackoverflow.com/users/11929315/michael-masse)

[michael masse](https://stackoverflow.com/users/11929315/michael-masse)

733 bronze badges

- this worked in my case . Scenario. Had 1 running android 9 ( a9) and other 10 (a10) .I had 2 aliases `root# alias a1 a2 alias a1='adb kill-server && sleep 2 && adb tcpip 5555 && adb connect 192.168.43.1:5555' alias a2='adb -s 192.168.43.1:5555 shell'` . on a9 running a1 and a2 seperately will work but a1 && a2 does not . 50% times it gives me "vendor keys not verified" . On a10 . a1 works. a2 will not . I have to disconnect usb & run a2 without alias per above. I wonder why that happens 
  
    – [user1874594](https://stackoverflow.com/users/1874594/user1874594 "2,425 reputation")
    
     [Mar 17, 2022 at 10:17](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment126391852_57502075)
    

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

1

[](https://stackoverflow.com/posts/63388021/timeline)

connect using USB and Just use this command

`adb tcpip 5555`

It will restart services and then `adb connect <device-ip>:5555`

Note: to find device IP, you can navigate to `wifi -> YOUR_NETWORK -> IP address`

[Share](https://stackoverflow.com/a/63388021/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/63388021/edit "Revise and improve this post")

Follow

[edited Sep 22, 2021 at 16:01](https://stackoverflow.com/posts/63388021/revisions "show all edits to this post")

[

![HackRx's user avatar](https://www.gravatar.com/avatar/370fc6cf753a6b982e1940e5dbd56980?s=64&d=identicon&r=PG&f=y&so-version=2)

](https://stackoverflow.com/users/11902885/hackrx)

[HackRx](https://stackoverflow.com/users/11902885/hackrx)

30833 silver badges1414 bronze badges

answered Aug 13, 2020 at 4:12

[

![Yogesh Shinde's user avatar](https://i.stack.imgur.com/LZ9tl.jpg?s=64&g=1)

](https://stackoverflow.com/users/6315851/yogesh-shinde)

[Yogesh Shinde](https://stackoverflow.com/users/6315851/yogesh-shinde)

39344 silver badges1111 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/74773961/timeline)

In my case just restarting the device worked. Just try if you are lucky!

[Share](https://stackoverflow.com/a/74773961/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/74773961/edit "Revise and improve this post")

Follow

answered Dec 12, 2022 at 16:05

[

![Anish Vahora's user avatar](https://graph.facebook.com/894439887292631/picture?type=large)

](https://stackoverflow.com/users/4896012/anish-vahora)

[Anish Vahora](https://stackoverflow.com/users/4896012/anish-vahora)

20333 silver badges1010 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/56566081/timeline)

In my case I had to shut of and on the wifi adb debugger app, on the device. On another USB device I had to shitch off and on developer mode, then re-set the development options. Also reset my pc.

Seem that adb in some way made a mess with global communication and all the debug communication have to be reset on both sides.

After this the devices start to comunicate again

[Share](https://stackoverflow.com/a/56566081/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/56566081/edit "Revise and improve this post")

Follow

answered Jun 12, 2019 at 15:49

[

![Luca C.'s user avatar](https://i.stack.imgur.com/Jld5i.png?s=64&g=1)

](https://stackoverflow.com/users/571410/luca-c)

[Luca C.](https://stackoverflow.com/users/571410/luca-c)

12.2k11 gold badge8787 silver badges7878 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/74880352/timeline)

[Dec-2022] I suffered from this problem around 2-3 months.

I tried all the cmd way but my device(**Android 12**) was not connecting wirelessly. _As my laptop & mobile was both connected 5 ghz connection of my router._

> **After switching my mobile to normal 2.4 ghz wifi network. it connected with `adb connect {ip-of-device}` !!**

So, if your wifi network supports both the 2.4 and 5 ghz then try connecting your devices(laptop/mobile) to another combination. Then try to connect with `adb connect {ip-of-device}`

[Share](https://stackoverflow.com/a/74880352/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/74880352/edit "Revise and improve this post")

Follow

answered Dec 21, 2022 at 18:29

[

![Darshan Rathod's user avatar](https://i.stack.imgur.com/f7zeW.jpg?s=64&g=1)

](https://stackoverflow.com/users/8231767/darshan-rathod)

[Darshan Rathod](https://stackoverflow.com/users/8231767/darshan-rathod)

61155 silver badges1616 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/72395370/timeline)

## I created this powershell script that automates the connection to the phone, to make it work follow the steps and reassign the variables to make it work on your device

```perl
#Important!!!
# Enable developer options on your phone and follow the steps below:
# Step 0: Developer options > (USB debugging = true) and (Permanently accept the digital signature of the computer)
# Step 1: Developer Options > Active Screen = true
# Step 2: Developer options > Allow ADB debugging in load-only mode = true

$customPort = '5555'
$ipPhone = '192.168.1.53'
$fullIpPhone = $ipPhone + ':' + $customPort

Write-Host 
Write-Host '=============Start script============='
adb usb

Write-Host 
Write-Host '=============Start clean network============='
adb devices
adb disconnect
adb kill-server
adb devices

Write-Host 
Write-Host '=============Start connection============='
adb tcpip  $customPort
# adb tcpip 5555 # $customPort
adb connect $fullIpPhone
# adb connect 192.168.1.53:5555 # $fullIpPhone
adb devices

Write-Host 
Write-Host '=============End script============='
```

[Share](https://stackoverflow.com/a/72395370/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/72395370/edit "Revise and improve this post")

Follow

answered May 26, 2022 at 16:53

[

![Fred's user avatar](https://lh4.googleusercontent.com/-DvVXyRtMors/AAAAAAAAAAI/AAAAAAAAAX8/AMZuucmMA58i3dcDhrTzAMYGphCoHEnSgg/photo.jpg?sz=64)

](https://stackoverflow.com/users/14168668/fred)

[Fred](https://stackoverflow.com/users/14168668/fred)

111 bronze badge

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/63473268/timeline)

So my situation was that i restarted it in tcp mode but still couldn't connect, i had an `No route to host` error ,so i tried pinging it but i couldn't find it either even though it was on the arp table. so what i noticed was that when i connect it by usb, it pings successfully. so i had to do the  `adb connect ip` before i could unplug the usb.

[Share](https://stackoverflow.com/a/63473268/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/63473268/edit "Revise and improve this post")

Follow

[edited Aug 18, 2020 at 17:10](https://stackoverflow.com/posts/63473268/revisions "show all edits to this post")

answered Aug 18, 2020 at 16:54

[

![williamjnrkdd's user avatar](https://lh6.googleusercontent.com/-GvRRV_W0hF8/AAAAAAAAAAI/AAAAAAAAAAA/ACHi3rfEAOd_WcPNcOlq9S_PxARU4dfMdQ/photo.jpg?sz=64)

](https://stackoverflow.com/users/12928322/williamjnrkdd)

[williamjnrkdd](https://stackoverflow.com/users/12928322/williamjnrkdd)

3966 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/69489568/timeline)

I faced the same issue with the ADB over WiFi connection between my Android 10 phone and Windows 10 PC, which was OK before and suddenly this happened. After reading this question and answers above, I first turned off WiFi on my router and turned it back on.(I repeat not the router, just WiFi) It worked.

[Share](https://stackoverflow.com/a/69489568/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/69489568/edit "Revise and improve this post")

Follow

answered Oct 8, 2021 at 1:47

[

![Nuwan Thisara's user avatar](https://i.stack.imgur.com/ssPKE.png?s=64&g=1)

](https://stackoverflow.com/users/3339899/nuwan-thisara)

[Nuwan Thisara](https://stackoverflow.com/users/3339899/nuwan-thisara)

28455 silver badges1616 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/58854407/timeline)

```sql
sudo adb start-server
```

Just worked for me. after A day of looking for a solution.

[Share](https://stackoverflow.com/a/58854407/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/58854407/edit "Revise and improve this post")

Follow

answered Nov 14, 2019 at 10:20

[

![Ian Samz's user avatar](https://i.stack.imgur.com/5FyBv.png?s=64&g=1)

](https://stackoverflow.com/users/6523474/ian-samz)

[Ian Samz](https://stackoverflow.com/users/6523474/ian-samz)

1,9512222 silver badges2121 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/63927968/timeline)

I ran into this same issue on not being able to connect via Wi-Fi but was using the [snap version](https://snapcraft.io/scrcpy) of `scrcpy`. I kept seeing messages like `adb server version (40) doesn't match this client (39); killing...` when I would run `adb` or `scrcpy` commands but it still worked, until I tried to connect over Wi-Fi.

```
$ scrcpy 
INFO: scrcpy 1.16 <https://github.com/Genymobile/scrcpy>
adb server version (40) doesn't match this client (39); killing...
* daemon started successfully
adb: error: failed to get feature set: no devices/emulators found
ERROR: "adb push" returned with value 1
```

I believe this means it was unable to push the server app to the device.

Running the commands to setup Wi-Fi using the adb built into the snap, [scrpy.adb](https://github.com/sisco311/scrcpy-snap#you-can-also-use-the-builtin-adb-tool) solved this for me.

```ruby
$ scrcpy.adb tcpip 5555
$ scrcpy.adb connect 192.168.1.25:5555
connected to 192.168.1.25:5555
$ scrcpy
INFO: scrcpy 1.16 <https://github.com/Genymobile/scrcpy>
/usr/local/share/scrcpy/scrcpy-server: 1 file pushed. 0.6 MB/s (33622 bytes in 0.051s)
[server] INFO: Device: XXXXX XXXXXXX (Android 9)
INFO: Renderer: opengl
INFO: OpenGL version: 4.6.0 NVIDIA 390.25
INFO: Trilinear filtering enabled
INFO: Initial texture: 1440x2560
```

[Share](https://stackoverflow.com/a/63927968/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/63927968/edit "Revise and improve this post")

Follow

answered Sep 16, 2020 at 21:11

[

![frederickjh's user avatar](https://www.gravatar.com/avatar/59cef2134917a46c1c836512b1c336ab?s=64&d=identicon&r=PG)

](https://stackoverflow.com/users/6079319/frederickjh)

[frederickjh](https://stackoverflow.com/users/6079319/frederickjh)

1,81911 gold badge1212 silver badges1010 bronze badges

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

0

[](https://stackoverflow.com/posts/76566045/timeline)

Ok this might sound silly but make sure you have connected your phone to the computer through usb, and not to the electrical plug! (if you are using two different cables).

[Share](https://stackoverflow.com/a/76566045/8273792 "Short permalink to this answer")

[Edit](https://stackoverflow.com/posts/76566045/edit "Revise and improve this post")

Follow

answered Jun 27, 2023 at 14:43

[

![Antonin GAVREL's user avatar](https://i.stack.imgur.com/EePWR.jpg?s=64&g=1)

](https://stackoverflow.com/users/3161139/antonin-gavrel)

[Antonin GAVREL](https://stackoverflow.com/users/3161139/antonin-gavrel)

10.6k1111 gold badges5858 silver badges8888 bronze badges

- 1
  
    🤣🤣🤣🤣🤣🤣🤣🤣🤣🤣🤣🤣 good one! it happens even to the best! 
    
    – [xavier.seignard](https://stackoverflow.com/users/572543/xavier-seignard "10,974 reputation")
    
     [Jul 6, 2023 at 7:07](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555#comment135099490_76566045) 
    

[Add a comment](https://stackoverflow.com/questions/33462720/adb-unable-to-connect-to-192-168-1-105555# "Use comments to ask for more information or suggest improvements. Avoid comments like “+1” or “thanks”.")

**[Highly active question](https://stackoverflow.com/help/privileges/protect-questions)**. Earn 10 reputation (not counting the [association bonus](https://meta.stackexchange.com/questions/141648/what-is-the-association-bonus-and-how-does-it-work)) in order to answer this question. The reputation requirement helps protect this question from spam and non-answer activity.

## 

Not the answer you're looking for? Browse other questions tagged 

- [android](https://stackoverflow.com/questions/tagged/android "show questions tagged 'android'")
- [adb](https://stackoverflow.com/questions/tagged/adb "show questions tagged 'adb'")

 or [ask your own question](https://stackoverflow.com/questions/ask).