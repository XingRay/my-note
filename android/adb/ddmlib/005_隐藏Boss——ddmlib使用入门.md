# 隐藏Boss——ddmlib使用入门

2016-05-041597

版权

**简介：** ddmlib使用入门ddmlib是DDMS工具的核心，堪称Android SDK中最不为人知的隐藏Boss，它封装了一系列对ADB的功能封装。

# ddmlib使用入门

ddmlib是DDMS工具的核心，堪称Android SDK中最不为人知的隐藏Boss，它封装了一系列对ADB的功能封装。

DDMS工具虽然已经非常强大，可以展示非常多的Android性能监测数据，但是，它有一个很大的缺点，就是很多数据不能导出，而且很多功能也不能达到自定义的需求，因此，基于这些问题，利用ddmlib来完成自定义的功能定制，就是非常有用的了。

完成DDMS功能的自定义设置，就需要使用到ddmlib这个jar，同时，为了了解DDMS是如何实现这些功能的，还需要引人DDMS的一些库，来了解其指令的实现原理，如图所示：

分别是ddmlib.jar、ddms.jar和ddmuilib.jar，其中ddmlib.jar是核心功能，其它两个是为了查看其实现原理而引人的。

## 搭建研究环境

在IDEA中创建一个Java项目，并导入这些jar包：

```avrasm
.
├── lib
│   ├── ddmlib.jar
│   ├── ddms.jar
│   ├── ddmuilib.jar
│   └── guava-18.0.jar
```

可以看见这里多了一个guava的jar包，该jar是Google的一些拓展库，在导入这些jar包的时候需要进行依赖。这些jar全部引人后，研究DDMS的环境就搭建好了。点击每一个jar，就可以查看其相关的方法和代码了，如图所示：



## 利用ddmlib连接设备

要使用ddmlib，首先需要连接设备，这是学习、研究ddmlib.jar的第一步，代码如下所示：

```java
import com.android.ddmlib.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        IDevice device;
        AndroidDebugBridge.init(false);
        AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
                "/Users/xuyisheng/Library/Android/sdk/platform-tools/adb", false);
        waitForDevice(bridge);
        IDevice devices[] = bridge.getDevices();
        device = devices[0];
    }

    private static void waitForDevice(AndroidDebugBridge bridge) {
        int count = 0;
        while (!bridge.hasInitialDeviceList()) {
            try {
                Thread.sleep(100);
                count++;
            } catch (InterruptedException ignored) {
            }
            if (count > 300) {
                System.err.print("Time out");
                break;
            }
        }
    }
}
```

这里的代码中使用循环来进行处理的原因是，ADB需要时间来进行设备连接，所以需要等待一段时间来进行连接，一旦设备连接成功，就可以通过IDevice类来进行设备操作了。

## ddmlib api使用示例

ddmlib提供了很多API，但是其文档很少，很多东西只能从源码中找，这里举一个例子，利用ddmlib来进行设备截图，代码如下所示：

```csharp
private static void takeScreenshot(IDevice device) {
    try {
        RawImage rawScreen = device.getScreenshot();
        if (rawScreen != null) {
            int width = rawScreen.width;
            int height = rawScreen.height;
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int index = 0;
            int indexInc = rawScreen.bpp >> 3;
            for (int y = 0; y < rawScreen.height; y++) {
                for (int x = 0; x < rawScreen.width; x++, index += indexInc) {
                    int value = rawScreen.getARGB(index);
                    image.setRGB(x, y, value);
                }
            }
            ImageIO.write(image, "PNG", new File("/Users/xuyisheng/Downloads/temp/test.png"));
        }
    } catch (TimeoutException | AdbCommandRejectedException | IOException e) {
        e.printStackTrace();
    }
}
```

利用IDevice的API就可以完成设备的截图操作。

## DDMS功能自定义

要使用ddmlib来实现DDMS的功能自定义，就需要先了解DDMS是如何获取这些数据的，例如，我们需要了解DDMS是如何统计cpuinfo、meminfo和gfxinfo，也就是下面这个界面：



假如我们要做App的性能监测，那么这里的CPU、Memory、Frame信息是非常好的，但是DDMS却不能导出数据，所以我们需要进行自定义，那么这个功能，DDMS是如何实现的呢？打开ddmsuilib.jar，如图所示：



找到其中的SysinfoPanel类，从命名就基本可以确定，这个就是我们在DDMS中看见的那个界面，进入代码就更可以确定了，如图所示：



在这里，就可以找到相应的实现原理了，原来就是dumpsys cpuinfo”, “cat /proc/meminfo ; procrank”, “dumpsys gfxinfo而已。OK，掌握了这个方法，再查看其它的功能，就非常简单了。

## 开源项目

Github上对ddmlib研究的人并不多，可想而知，这个隐藏Boss藏的有多深，目前所知的比较出名的是下面这个项目：

https://github.com/cosysoft/device

但这个项目是运行不起来的，因为它引用了一些携程内部的服务器地址，需要做修改才能运行，但它的原理还是不错的，对ddmlib的研究也挺深入的。

文章标签：

[Java](https://developer.aliyun.com/label/article_de-3-100001)

[Android开发](https://developer.aliyun.com/label/article_de-3-100015)

[索引](https://developer.aliyun.com/label/article_de-3-100070)

[API](https://developer.aliyun.com/label/article_de-3-100252)