# STF-minitouch的使用

stf
专栏收录该内容
1 篇文章0 订阅
订阅专栏
我们经常会遇到这样子的情况,需要演示一些手机上的一些界面的时候。不能够把手机上的影像投影到电脑上，同时在电脑上操作手机。也可能是我了解的比较少吧。

最近在论坛上看到了 [STF 框架之 minitouch 工具] (https://testerhome.com/topics/4400) 其实里面已经介绍的很详细了，只是使用C#的来实现的。对于c#确实不太熟悉，所以就拿了 minicap_java DoctorQ使用java基于minicap实现的一个GUI展示Android手机屏幕的一个工具来继续完善它，使它能够支持一些鼠标点击操作。

介绍
minitouch提供了一个socket接口用来出来在Android设备上的多点触摸事件以及手势。它能够支持api 10以上的设备且不需要通过root. 但是根据不同的cpu的ABI需要使用不同的minitouch。

使用
首先我们需要找出你的设备所支持的ABI

ABI=$(adb shell getprop ro.product.cpu.abi | tr -d '\r')
1
注意：如果你有多台设备连接的情况下并且你没有设置$ANDROID_SERIAL的话，你需要去指定设备 -s <serial>

推送对应的文件到设备上。

adb push libs/$ABI/minitouch /data/local/tmp/
1
注意如果你的SDK<16的情况下，你需要使用minitouch-nopie

当然你还需要更改下minitouch的执行权限。

chmod 777 /data/local/tmp/minitouch
1
并且通过下面的命令来判断是否已经操作成功了。

adb shell /data/local/tmp/minitouch -h
1
下来我们可以直接通过adb shell /data/local/tmp/minitouch 来执行，这个时候设备就开始监听了。

这个时候除非出现错误的消息或者说程序退出，我们需要进行端口转发 通过如下命令：

adb forward tcp:1111 localabstract:minitouch
1
现在我们就需要去连接对应的端口，获取数据了。
提示文档里面给的是通过nc localhost 1111,但是我们肯定不是这样子的，我们需要自己去创建一个socket来进行连接。获取socket对应的数据。
socket命令
d <contact> <x> <y> <pressure>
1
例如：d 0 10 10 50

压力值50 在点 10，10 使用一个触点按下。

m <contact> <x> <y> <pressure>
1
例如: m 0 10 10 50
压力值为50在 10,10滑动。

u <contact>
1
例如：u 0
手势抬起

实现
这里我们只说明鼠标的一些事件的实现。

mp.addMouseListener(new MouseListener() {
    @Override
      public void mouseClicked(MouseEvent e) {

      }
    
      @Override
      public void mousePressed(MouseEvent e) {
    
          System.out.println("i press"+e.getX()+","+e.getY());
          Point point = pointConvert(e.getPoint());
          if (outputStream != null) {
              String command = String.format("d 0 %s %s 50\n", (int)point.getX(), (int)point.getY());
              executeTouch(command);
          }
      }
    
      @Override
      public void mouseReleased(MouseEvent e) {
          System.out.println("i release");
          if (outputStream != null) {
              String command =  "u 0\n";
              executeTouch(command);
          }
      }
    
      @Override
      public void mouseEntered(MouseEvent e) {
    
      }
    
      @Override
      public void mouseExited(MouseEvent e) {
    
      }
    });


    mp.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent e) {
          System.out.println(e.getPoint().getX()+","+e.getPoint().getY());
          Point point = pointConvert(e.getPoint());
          if (outputStream != null) {
              String command = String.format("m 0 %s %s 50\n", (int)point.getX(), (int)point.getY());
              executeTouch(command);
          }
      }
    
      @Override
      public void mouseMoved(MouseEvent e) {
    
      }
});

private Point pointConvert(Point point)
    {
        Point realpoint = new Point((int)((point.getX()*1.0 / width) * banner.getMaxX()) , (int)((point.getY()*1.0 /height) * banner.getMaxY()) );
        return realpoint;
    }

    private void executeTouch(String command) {
        if (outputStream != null) {
            try {
                System.out.println("command" + command);
                outputStream.write(command.getBytes());
                outputStream.flush();
                String endCommand = "c\n";
                outputStream.write(endCommand.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
minicapDemo具体可参考代码。

参考文档
STF 框架之 minicap 工具
STF 框架之 minitouch 工具

感想
上面的项目基本上都没改什么，只是简单的加了几句代码而已。还是要感谢几个大神。