OpenCV从入门到精通

风萧萧1999

于 2024-01-31 19:22:58 发布

阅读量1.1k
 收藏 21

点赞数 20
文章标签： opencv 人工智能 计算机视觉
版权

GitCode 开源社区
文章已被社区收录
加入社区
1、引言
OpenCV是一个开源计算机视觉库，提供了丰富的图像处理和计算机规觉算法。它支持多种编程语言，包括Java本文将介绍如何在Java中使用OpenCV进行图像处理和计算机视觉任务。


2、引入安装
在使用OpenCV之前，我们首先需要安装OpenCV库：
1.下载OpenCV库文件。可以从OpenCV官方网站 (
2.解压下载的压缩包。
3.在Java项目中导入OpenCV库，将解压后的OpenCV库文件夹中的 opencv<version>;jar 文件添加到Java项目的依赖中，也可以maven引入：

<dependency>
    <groupId>org.openpnp</groupId>
    <artifactId>opencv</artifactId>
    <version>3.4.2-1</version>
</dependency>
3、功能模块
OpenCV的主要功能包括：

1.图像处理：包括图像加载、保存、绘制、变换等。
2.视频分析：提供视频的读取、写入、帧处理、光流估计等功能。
3.物体检测和跟踪：包括人脸检测、目标跟踪、运动检测等。
4.图像特征提取和描述：包括关键点检测、特征匹配、图像描述符等。
5.机器学习和深度学习：提供了各种机器学习和深度学习算法的实现，例如支持向量机（SVM）、神经网络等。
6.相机校准和三维重建：用于相机参数校准、立体视觉和三维重建等任务。
7.图像配准和拼接：用于图像配准、图像融合和全景图拼接等任务。
8.图像分割和轮廓分析：用于图像分割、轮廓提取和形状识别等任务。
9.图像滤波和增强：提供各种图像滤波器和增强技术，如均值滤波、锐化等。
10.图像和视频编解码：包括各种图像和视频编解码器的支持。
在以上功能中，大家使用最多的是图像处理、物体检测和跟踪、相机校准和三维重建、机器学习等功能。

4、常用函数目录




 5、代码示例
5.1 加载和显示图像
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
public class ImageProcessing {
    public static void main(String[] args) (
        // 册裁OpencV库
        System.loadLibrary(Core.NATIVE LIBRARY NAME);
        // 加裁图像
        Mat image = HighGui.imread("path/to/image .jpg");
        // 显示图像
        HighGui.imshow("Image", image);
        HighGui.waitKey();
    }
}
上述代码中，我们首先加载了OpenCV库，然后，使用 Highgui.imread()函数加载了一张图像，最后，使用 Highgui.imshow()函数显示图像，并通过 HiphGui.iwaitkey()函数等待用户按下任意键关闭图像窗口。

5.2 图像处理

OpenCV提供了许多图像处理函数，可以对图像进行各种操作。以下是一些常用的图像处理操作的示例代码:


import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {
    public static void main(string[] args) (
        // 加裁Opencv库
        System.loadLibrary(Core.NATIVE LIBRARY NAME);
        // 加裁登色图像
        Mat image = Imgcodecs.imread("path/to/image.jpg");
        // 转为灰度图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR BGR2GRAY);

        // 显示灰度图像
        HighGui.imshow("Gray Image", grayImage);
        HighGui.waitKey();
    }
}

上代码中，我们首先使用Imgcodecs.imread()加载了一张彩色图像。然后，使用Imgproc.cvtColor 函数将彩色图像转换为灰度图像，最后，使用 Hiohguitimshowl函数显示灰度图像。



5.3 边缘检测
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.highgui.HighGui;import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImageProcessing {
    public static void main(String[] args) (
        // opencv库
        System.loadLibrary(Core .NATIVE LIBRARY NAME);
        // 加裁东度图像
        Mat grayImage = Imgcodecs.imread("path/to/gray image.jpg", Imgcodecs.IMREAD GRAYSCALE);
        // 边您检测
        Mat edges = new Mat();
        Imgproc.Canny(grayImage, edges,100，200);
        // 显示边缘图像
        HighGui.imshow("Edges", edges);
        HighGui.waitKey();
    }
}
上述代码中，我们首先使用Imgcodecs.imread函数加载了一张灰度图像，然后，使用 Imgproc.Canny函数进行边家检测。最后，使用HighGui.imshow函数显示边图像。

6、应用场景

大致可以分为以下几大方向：

图像识别
目标检测
图像分割
风格迁移
图像重构
超分辨率
图像生成
人脸识别
详见：https://paperswithcode.com/sota
