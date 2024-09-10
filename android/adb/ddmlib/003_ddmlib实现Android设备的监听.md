ddmlib实现Android设备的监听

雲驊

于 2019-10-19 22:06:01 发布

阅读量760
 收藏 2

点赞数
分类专栏： Android自动化测试教程（java版）
版权

Android自动化测试教程（java版）
专栏收录该内容
2 篇文章0 订阅
订阅专栏
谷歌的ddmlib包提供了解决方案。
AndroidDebugBridge实现设备监听。
IDevice实现设备信息获取。

++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

本节实例在maven工程配置
本节示例AndroidDebugBridge

++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

pom.xml

<dependency>
    <groupId>com.android.tools.ddms</groupId>
    <artifactId>ddmlib</artifactId>
    <version>25.3.0</version>
</dependency>
1
2
3
4
5
用到： init(false); createBridge();

MyDevices.java

public class MyDevice {
	/*
	 * 获取当前所连接设备的信息
	 */
	public IDevice[] iDevice() {
		//初始化ddmlib
		AndroidDebugBridge.init(false);
		//创建debug bridge
		AndroidDebugBridge adb = AndroidDebugBridge.createBridge();//等待获取到设备
//		AndroidDebugBridge adb = AndroidDebugBridge.createBridge(
//				"F:\\Android\\android-sdk\\platform-tools\\adb.exe",false);
		
		waitForDevice(adb);
		
		return adb.getDevices();
	}
	/*
	 * 设置等待时间，直到获取到设备信息。
	 * 等待超过0.3秒，抛出异常
	 */
	private static void waitForDevice(AndroidDebugBridge bridge) {
		int count = 0;
		while(!bridge.hasInitialDeviceList()) {
			try {
				Thread.sleep(100);
				count++;
			} catch(InterruptedException ignored) {		
			}
			if(count>300) {
				System.out.println("Time out");
				break;
			}
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
App.java

public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello AutoTest!" );
        MyDevice mDevice = new MyDevice();
        for(IDevice iDevice : mDevice.iDevice()) {
        	System.out.println("获取当前设备 " + iDevice);
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
结果如下：


+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
检测adb版本

pom.xml

<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>28.1-jre</version>
</dependency>
1
2
3
4
5
Guava 定义了 ListenableFuture接口并继承了JDK concurrent包下的Future接口，
ListenableFuture 允许你注册回调方法(callbacks)，在运算（多线程执行）完成的时候进行调用, 
或者在运算（多线程执行）完成后立即执行。
这样简单的改进，使得可以明显的支持更多的操作，这样的功能在JDK concurrent中的Future是不支持的。 
在高并发并且需要大量Future对象的情况下，推荐尽量使用ListenableFuture来代替..

ListenableFuture 中的基础方法是addListener(Runnable, Executor), 该方法会在多线程运算完的时候，
在Executor中执行指定的Runnable。
1
2
3
4
5
6
7
8
//用到的类
AndroidDebugBridge
ListenableFuture<AdbVersion>//此接口在guava.jar中
//用到的函数
getAdbVersion(@NonNull final File adb)
1
2
3
4
5
MyDevices.java

/*
	 * 获取adb版本
	 */
	public AdbVersion getAdbVersion() {
		File adbVer = new File("F:\\Android\\android-sdk\\platform-tools\\adb.exe");
        ListenableFuture<AdbVersion> future = AndroidDebugBridge.getAdbVersion(adbVer);
        AdbVersion version = null;
        try {
        	/*
        	* TimeUnit提供了可读性更好的线程暂停操作，通常用来替换Thread.sleep()
        	* 例：TimeUnit.MINUTES.sleep(4);  // sleeping for 4 minutes
        	* 指定DAYS、HOURS、MINUTES,SECONDS、MILLISECONDS和NANOSECONDS
        	*/
        	version = future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return version;
        }
        return version;
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
App.java

    private static AdbVersion AdbVersion;
    
    public static void main( String[] args )
    {
        System.out.println( "Hello Springboot!" );
        MyDevice mDevice = new MyDevice();
        AdbVersion = mDevice.getAdbVersion();
        System.out.println("当前adb版本: " + AdbVersion.toString());
        for(IDevice iDevice : mDevice.iDevice()) {
        	System.out.println("获取当前设备 " + iDevice);
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
结果：