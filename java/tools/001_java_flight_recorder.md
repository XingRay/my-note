# Java Flight Recorder——飞行记录仪



一 点睛
Java Flight Recorder 是 JMC 的其中一个组件，能够以极低的性能开销收集 Java 虚拟机的性能数据。与其他工具相比，JFR 的性能开销很小，在默认配置下平均低于 1%。JFR 能够直接访问虚拟机内的敌据并且不会影响虚拟机的优化。因此它非常适用于生产环境下满负荷运行的 Java 程序。

Java Flight Recorder 和 JDK Mission Control 共同创建了一个完整的工具链。JDK Mission Control 可对 Java Flight Recorder 连续收集低水平和详细的运行时信息进行高效、详细的分析。

当启用时 JFR 将记录运行过程中发生的一系列事件。其中包括 Java 层面的事件，如线程事件、锁事件，以及 Java 虚拟机内部的事件，如新建对象，垃圾回收和即时编译事件。按照发生时机以及持续时间来划分，JFR 的事件共有四种类型，它们分别为以下四种：

- 瞬时事件（Instant Event) ，用户关心的是它们发生与否，例如异常、线程启动事件。

- 持续事件(Duration Event) ，用户关心的是它们的持续时间，例如垃圾回收事件。

- 计时事件(Timed Event) ，是时长超出指定阈值的持续事件。

- 取样事件（Sample Event)，是周期性取样的事件。

取样事件的其中一个常见例子便是方法抽样（Method Sampling），即每隔一段时问统计各个线程的栈轨迹。如果在这些抽样取得的栈轨迹中存在一个反复出现的方法，那么我们可以推测该方法是热点方法

二 启动飞行记录

![img](D:\my-note\java\tools\assets\watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAY2hlbmdxaXVtaW5n,size_13,color_FFFFFF,t_70,g_se,x_16.png)



三 下一步

![img](D:\my-note\java\tools\assets\watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAY2hlbmdxaXVtaW5n,size_13,color_FFFFFF,t_70,g_se,x_16-1708837916759-3.png)

四 下一步
设置方法如下

![img](D:\my-note\java\tools\assets\watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAY2hlbmdxaXVtaW5n,size_20,color_FFFFFF,t_70,g_se,x_16.png)

五 代码

```
package chapter03;
 
import java.util.ArrayList;
import java.util.Random;
 
/**
* -Xms600m -Xmx600m -XX:SurvivorRatio=8 -XX:+UnlockCommercialFeatures -XX:+FlightRecorder
* 老年代：400m
* 伊甸园：160m
* s0:20m
* s1:20m
*/
public class OOMTest {
    public static void main(String[] args) {
        ArrayList<Picture> list = new ArrayList<>();
        while (true) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            list.add(new Picture(new Random().nextInt(100 * 50)));
        }
    }
}
 
class Picture {
    private byte[] pixels;
 
 
    public Picture(int length) {
        this.pixels = new byte[length];
    }
}
```

六 功能界面

![img](D:\my-note\java\tools\assets\watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAY2hlbmdxaXVtaW5n,size_20,color_FFFFFF,t_70,g_se,x_16-1708837939451-8.png)