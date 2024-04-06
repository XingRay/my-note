# Java AWT/Swing/SWT/JavaFX杂谈



## 1 背景

最近想做一个实用的小工具，能屏幕截图，录屏和录制课件，简单的图像处理，和制作gif表情包。翻出了很久以前用Java awt/swing写的一个屏幕截图小程序，能运行，但是屏幕截图到剪贴板后，发现不能直接粘贴到网页，很纳闷。研究了半天，猜想是剪贴板上的BufferedImage格式不被网页识别，但如果贴到别的应用中，如word, QQ聊天中，又是可以的。不得已，切换到JavaFX提供的剪贴板功能，发现居然是可以的，看来JavaFX比swing更好用一点。于是决定将该程序移植到JavaFX框架上。

问题缘起
Java屏幕截图到剪贴板:

```
Robot ro = new Robot();
Toolkit tk = Toolkit.getDefaultToolkit();
Dimension di = tk.getScreenSize();
Rectangle rec = new Rectangle(0, 0, di.width, di.height);
BufferedImage bi = ro.createScreenCapture(rec);
// 接下来选择要截图的区域, 然后粘贴到剪贴板
Transferable trans = new Transferable() {
  ......  // 此处代码网上到处可见，省略掉            
};
Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
```

在网页中采用Javascript读取剪贴板上的图片，目前web编辑器支持直接粘贴图片是必备功能。

```
window.addEventListener("paste", function (e) {
	var items = e.clipboardData.items;
    for (var i = 0; i < items.length; i++) {
         if (items[i].type.indexOf("image") == -1) continue;
         // Retrieve image on clipboard as blob
         var blob = items[i].getAsFile();
         var canvas = document.getElementById("mycanvas");
         var ctx = canvas.getContext('2d');

         // Create an image to render the blob on the canvas
         var img = new Image();
         // Once the image loads, render the img on the canvas
         img.onload = function () {
             // Update dimensions of the canvas with the dimensions of the image
              canvas.width = this.width;
              canvas.height = this.height;

              // Draw the image
              ctx.drawImage(img, 0, 0);
          };
         // Crossbrowser support for URL
         var URLObj = window.URL || window.webkitURL;

         // Creates a DOMString containing a URL representing the object 
         // namely the original Blob
         img.src = URLObj.createObjectURL(imageBlob);
     }            
});
```

上述代码都堂堂正正，无可指摘，但就是不work。于是切换到JavaFX:

```
public static void copyToClipboardImage(BufferedImage image) {
        final javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        javafx.scene.image.Image _image = SwingFXUtils.toFXImage(image, null);
        content.putImage(_image);
        clipboard.setContent(content);
    }
```

这里，将java.awt.BufferedImage转换为javafx.scene.image.Image，才能放到剪贴板。这样网页才能识别了。



## 2 重新学习AWT/Swing和JavaFX

上个世纪我就开始用java.awt.Applet进行编程，虽然界面简陋，但也能满足需求。很快swing就出来了，UI控件非常丰富，如果美工给力，完全不输于MFC/WPF等界面框架。总而言之，做跨平台的桌面应用开发，awt/swing是选择之一。目前，其它的界面框架如QT，Electron, 还有SWT，也都是常见选项。但awt/swing灵活性不如JavaFX，目前正慢慢老去，但还不会马上退出历史舞台。
当然，JavaFX还没有完全替代awt/swing的全部功能，例如系统托盘的实现，还得依赖awt类库。

AWT/Swing
为了方便了解swing的UI体系，可以看下面两张图：

![在这里插入图片描述](D:\my-note\java\awt_swing\assets\ec9ba8116b9a46e2ac26b73d56c6f7d9.png)

再看一张：

![在这里插入图片描述](D:\my-note\java\awt_swing\assets\439d362b587d4e65ba1d841a7c32ae04.png)

SWT/JFace
由于不满于swing的性能和丑陋的界面，IBM于2001左右推出了接近原生UI的编程框架swt。该UI类体系和swing也有很大的相似性：

![在这里插入图片描述](D:\my-note\java\awt_swing\assets\dd6e08804b1541b08528dd54aced8744.png)

最基本的SWT例子：

```
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SWTFirst {
  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);

    shell.setText("Hello, world!");

    shell.open();
    // Set up the event loop.
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        // If no more entries in the event queue
        display.sleep();
      }
    }
    display.dispose();
  }
}
```

Shell相当于JFrame。在此基础上，IBM进一步推出JFace和RCP框架，让开发者一度看到了Java统治桌面端开发的希望。可惜，世易时移，桌面开发已经逐步让位于web和移动开发。这些优秀的技术也没能大面积使用开来。但福音就是，她们还活着，而且还在发展。

JavaFX基础

![在这里插入图片描述](D:\my-note\java\awt_swing\assets\1a05326d1b1c46a3a76921964d255de8.png)


JavaFX提供了几种预定义的布局，如HBox, VBox, Border Pane, Stack Pane, Text Flow, Anchor Pane, Title Pane, Grid Pane, Flow Panel。

State舞台是一个类似于Swing中的JWindow的顶级容器，代表一个窗口。它用于容纳场景Scene，场景Scene是一个类似于Swing的JFrame的容器。

```
Stage stage = new Stage();

Group root = new Group();
root.getChildren().add(box);
Scene scene = new Scene(root, 400, 300);
scene.setFill(Color.BLACK);
stage.setWidth(600);
stage.setHeight(600);   
stage.setTitle("Hello");
stage.setScene(scene);
stage.show();       
```

jfx支持布局配置化，且支持采用CSS定义。

```
try {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("NewWindow.fxml"));
        /* 
         * if "fx:controller" is not set in fxml
         * fxmlLoader.setController(NewWindowController);
         */
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        Stage stage = new Stage();
        stage.setTitle("New Window");
        stage.setScene(scene);
        stage.show();
    } catch (IOException e) {
        Logger logger = Logger.getLogger(getClass().getName());
        logger.log(Level.SEVERE, "Failed to create new Window.", e);
    }
```



3 awt/swing向JavaFX(后续叫jfx)移植中的几点笔记
界面控件的移植
JFrame -> Application & Stage： swing中我们在main()函数里创建一个JFrame来启动界面，jfx中是通过Application.launch() 来完成的，主类继承Application。通常我们无需显式创建一个Stage实例，这点和swing不同。
JPanel -> Scene & Pane： Scene对应于JPanel, 但实际盛放控件的容器是Pane，有了Pane，再创建一个scene 即可. Pane才是带布局(layout)的容器。
JButton -> Button
JLabel -> Label：大部分UI组件，将J去掉就成了jfx的对应物。
In many cases, removing the J will do the trick…
JOptionPane -> Dialog
ActionListener -> EventHandler< ActionEvent >) : swing需要实现一个listener的全部方法，jfx不需要。Java8开始利用函数式编程，写法更简洁：

```
scene.setOnMousePressed(event -> onMousePressed(event));
scene.setOnMouseReleased(event -> onMouseReleased(event));
```

BufferedImage
BufferedImage在swing图形绘制中扮演着重要角色。在JavaFX 2.2中，类似的类为javafx.scene.image.WritableImage, 它是javafx.scene.image.Image的子类。
获得WritableImage对象：

```
// Obtain a snapshot of the canvas
WritableImage image = canvas.snapshot(null, null);
```

将BufferedImage转为javafx.scene.image.Image:

```
BufferedImage image = ...;
javafx.scene.image.Image _image = SwingFXUtils.toFXImage(image, null);
```

Canvas与图形绘制
在awt/swing中，每个组件都有一个paint(Graphics g)方法。该方法被框架调用，完成组件绘制。但JavaFX中没有这么一个方法。我们可以先创建一个Canvas对象，放到组件里，然后再获得图形设备来完成绘制。

```
Canvas canvas = new Canvas(bounds.getWidth(), bounds.getHeight());
GraphicsContext gc = canvas.getGraphicsContext2D();
gc.setFill(Color.GREEN);
gc.setStroke(Color.BLUE);
gc.setLineWidth(5);
gc.strokeLine(40, 10, 10, 40);
gc.fillOval(10, 60, 30, 30);
gc.strokeOval(60, 60, 30, 30);
gc.fillRoundRect(110, 60, 30, 30, 10, 10);
gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
```

组件的重绘
在awt/swing框架中，每个组件提供了paintComponent()，能实现组件的界面绘制，该方法无需用户显式调用。重绘在界面移动，大小改变时都会发生。

```
public class MyPanel extends JPanel {
    private static final long serialVersionUID = -26977787951133480871L;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.clearRect(0, 0, getWidth(), getHeight());
        g.setColor(java.awt.Color.RED);
        g.fillRect(10, 10, getWidth() - 20, getHeight() - 20);

        // Paint your custom image here:
        g.drawImage(someImage, 0, 0, null);
    }
}
```

在JavaFX中，可以通过重写layoutChildren()方法实现类似的功能：

```
public class MyPane extends Pane {
    private final Canvas canvas;

    public MyPane() {
        canvas = new Canvas(getWidth(), getHeight());
        getChildren().add(canvas);
        widthProperty().addListener(e -> canvas.setWidth(getWidth()));
        heightProperty().addListener(e -> canvas.setHeight(getHeight()));
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setFill(Color.RED);
        gc.fillRect(10, 10, getWidth() - 20, getHeight() - 20);

        // Paint your custom image here:
        gc.drawImage(someImage, 0, 0);
    }
}
```



4 关于Java GUI编程的一些有趣话题
JavaFX是用来取代swing的吗？
官方的回答是Yes，但是swing还会存在很长一段时间。个人的判断，JavaFX界面更好看，更现代，但swing也比较成熟稳定。在选择哪个框架上，官方给了一些建议:

![在这里插入图片描述](D:\my-note\java\awt_swing\assets\f8d20440dc5f45729f249112118b1f81.png)

在2020年的更新说明
JavaFX technology seems to be going quite well in the ensuing years after this Question was originally posted. Java has been release on its 6-month train schedule, and JavaFX releases have arrived to match shortly after each one.

Oracle has completed the process of making JavaFX open-source, known as OpenJFX. The codebase is now housed as a sub-project on the OpenJDK project. Leadership has been assigned to the Gluon company.

The popularity and community seem to be growing. The competitors have fallen away, such as Microsoft Silverlight and Adobe Flash both having died.

Oracle continues to support Swing, but only in maintenance-mode. JavaFX, in contrast, continues to grow and improve.

Oracle sells support for JavaFX, as do other vendors such as Azul Systems.

Some vendors supplying Java implementations bundle the JavaFX/OpenJFX libraries with their JVMs. These include Azul Systems with their ZuluFX product, and BellSoft with their LibericaFX product.

Though cutting-edge presently, there is work being done to build a native apps for iOS and other platforms using OpenJFX with ahead-of-time compilation using GraalVM.

See:
Java Client Roadmap Update by Oracle, 2020-05
JavaFX FAQ by Oracle
OpenJFX
JavaFX page on Wikipedia
OpenJDK wiki page for JavaFX

AWT/Swing和JavaFX能否共存？
可以共存，但建议不要混和使用。
implementations in JavaFX8 (accesible with Java8) has one important point Swing JComponents could be accesible from JavaFX containers and JavaFX Components will be accesible in Swing JContainers (implemented in newer JavaFX2.2),

Java能否胜任2D/3D游戏编程?
我们知道，有了Netty，Java已经成功征服了服务端编程，可以说，服务端才是Java真正的用武之地。那有了JavaFX，加上SWT/JFace，是不是桌面端都能Java开发呢？如果不涉及图像处理，那答案几乎是肯定的。从JBuilder到Netbean/Eclipse到Intellij Idea已经证明了这一点，这些都是重量级桌面软件的代表。
首先，Java开发2D游戏完全没问题。目前已经有 JOGL 和 LWJGL 两个基础库能用于创建 2D 和 3D 游戏，这两个框架在底层构建于OpenGL之上，已经比较好地解决了诸如纹理和利用图形卡渲染的根本问题。对于3D游戏，目前已有jMonkeyEngine游戏引擎，应付中小型游戏应该还是足够的。

有哪些采用Java GUI实现的软件？
JBuilder
上古产品，IDE神器，也是采用Swing制作的，一直不肯开源，终导致被Eclipse灭亡。

IntelliJ IDEA
主窗口为IdeFrameImpl. 窗口里面是pure Swing components. 一些插件会采用JavaFX . 从 2020.2 版本开始采用一个Chromium组件JCEF.


而且IDEA在对一些Swing组件做了增强，如JFrame和JWindow，使得它们更加精致漂亮。JetBrains团队还有自己的JVM，基于OpenJDK。


一些UI特性也只能跑在JetBrains自家的JVM上。大部分对话框基于DialogWrapper类，参见com.intellij.openapi.ui.impl.DialogWrapperPeerImpl.MyDialog。

源码位于：https://github.com/JetBrains/intellij-community/blob/master/platform/platform-api/src/com/intellij/openapi/ui/DialogWrapper.java

主要的Java图像处理库
强大的图像处理库ImageJ
OpenIMAJ
JFree: https://github.com/jfree
总结
本文试图给不熟悉Java界面编程的同学一个简要的入门指引，抛砖引玉而已。