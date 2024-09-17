# ddmlib-25.3.0.jar AndroidDebugBridge类


ddmlib获取安卓设备信息。
ddmlib的版本是25.3.0

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
以下是class类
public final class AndroidDebugBridge
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++

ddmlib支持最大和最小版本的adb

private static final AdbVersion MIN_ADB_VERSION = AdbVersion.parseFrom("1.0.20");
1
private static final String ADB = "adb"; //$NON-NLS-1$
private static final String DDMS = "ddms"; //$NON-NLS-1$
private static final String SERVER_PORT_ENV_VAR = "ANDROID_ADB_SERVER_PORT"; //$NON-NLS-1$
1
2
3
在哪里能找到 adb bridge

static final String DEFAULT_ADB_HOST = "127.0.0.1"; //$NON-NLS-1$
static final int DEFAULT_ADB_PORT = 5037;
1
2
adb服务器启动的端口

private static int sAdbServerPort = 0;
1
private static InetAddress sHostAddr;
private static InetSocketAddress sSocketAddr;
1
2
private static AndroidDebugBridge sThis;
private static boolean sInitialized = false;
private static boolean sClientSupport;
1
2
3
adb的全路径

private String mAdbOsLocation = null;
1
private boolean mVersionCheck;
1
private boolean mStarted = false;
1
private DeviceMonitor mDeviceMonitor;
1
同步性 lock object

private static final Object sLock = new Object();
1
@GuardedBy("sLock")
private static final Set<IDebugBridgeChangeListener> sBridgeListeners =
            Sets.newCopyOnWriteArraySet();
1
2
3
private static final Set<IDeviceChangeListener> sDeviceListeners =
            Sets.newCopyOnWriteArraySet();
private static final Set<IClientChangeListener> sClientListeners =
            Sets.newCopyOnWriteArraySet();
1
2
3
4
实现这个接口的类，提供了处理 {@link AndroidDebugBridge} 变化的方法

public interface IDebugBridgeChangeListener {
	//当一个新的{@link AndroidDebugBridge}被连接时发送。
	//这是从一个非UI线程发送的。
	//桥接新的{@link AndroidDebugBridge}对象，如果初始化桥接时出错，则为空
	void bridgeChanged(@Nullable AndroidDebugBridge bridge);
}
1
2
3
4
5
6
实现这个接口的类提供了处理 {@link IDevice} 添加、删除和更改的方法。

public interface IDeviceChangeListener {
	//当a设备连接到{@link AndroidDebugBridge}时发送。
	//这是从一个非UI线程发送的。
	//安装新设备
	void deviceConnected(@NonNull IDevice device);
	//当a设备连接到{@link AndroidDebugBridge}时发送。
	//这是从一个非UI线程发送的。
	//安装新设备
	void deviceDisconnected(@NonNull IDevice device);
	/**
	*当设备数据改变，或客户端在设备上启动/终止时发送。
	*这是从一个非UI线程发送的。
	*设备被更新的设备。
	*changeMask描述发生了什么的掩模。它可以包含以下任何内容：
	*values: {@link IDevice#CHANGE_BUILD_INFO}, 
	*        {@link IDevice#CHANGE_STATE},
	*        {@link IDevice#CHANGE_CLIENT_LIST}
	**/
	void deviceChanged(@NonNull IDevice device, int changeMask);
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
实现此接口的类提供处理{@link Client}更改的方法。

public interface IClientChangeListener {
    /**
    * 当现有客户端信息更改时发送。
    * 这是从一个非UI线程发送的。
    * 设备被更新的设备。
    * 描述改变的属性的位掩码。它可以包含以下任何内容: 
    * value: {@link Client#CHANGE_INFO},
    * {@link Client#CHANGE_DEBUGGER_STATUS}, {@link Client#CHANGE_THREAD_MODE},
    * {@link Client#CHANGE_THREAD_DATA}, {@link Client#CHANGE_HEAP_MODE},
    * {@link Client#CHANGE_HEAP_DATA}, {@link Client#CHANGE_NATIVE_HEAP_DATA}
    */
    void clientChanged(@NonNull Client client, int changeMask);
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
只有在需要时才初始化库。
clientSupport表明，library是否应该使监测和交互的应用程序，在设备上运行。
#init(boolean)

public static synchronized void initIfNeeded(boolean clientSupport) {
	if (sInitialized) {
		return;
	}
	
	init(clientSupport);
}
1
2
3
4
5
6
7
初始化 ddmlib
这必须在{@link #createBridge(String, boolean)}之前调用一次。
Library可以通过两种方式初始化：
1、clientSupport == true。library监视设备及其上运行的应用程序。它将作为sort调试器连接到每个应用程序，以便能够通过 JDWP packets 与它们交互。
2、clientSupport == false。library只有 监控设备。应用程序中被搁浅，让其他工具建立在 ddmlib 将连接一个调试器到它们。
同一时间，只有一个工具可以在模式1中运行。
注意，模式1不阻止在设备上运行的应用程序的调试。
模式1 让调试器连接到 ddmlib 充当代理之间的调试器和应用程序调试。
看{@link Client#getDebuggerListenPort()}，
ddmlib的首选项也应该使用从默认值更改的任何默认值进行初始化。
当应用程序退出时，应该调用{@link #terminate()}。
clientSupport表明library是否应该使监测和交互的应用程序在设备上运行。
@see AndroidDebugBridge#createBridge(String, boolean)
@see DdmPreferences

public static synchronized void init(boolean clientSupport) {
if (sInitialized) {
            throw new IllegalStateException("AndroidDebugBridge.init() has already been called.");
        }
        sInitialized = true;
        sClientSupport = clientSupport;

        // Determine port and instantiate socket address.
        initAdbSocketAddr();
    
        MonitorThread monitorThread = MonitorThread.createInstance();
        monitorThread.start();
    
        HandleHello.register(monitorThread);
        HandleAppName.register(monitorThread);
        HandleTest.register(monitorThread);
        HandleThread.register(monitorThread);
        HandleHeap.register(monitorThread);
        HandleWait.register(monitorThread);
        HandleProfiling.register(monitorThread);
        HandleNativeHeap.register(monitorThread);
        HandleViewDebug.register(monitorThread);
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
终止ddmlib。这必须在应用程序终止时调用。

public static synchronized void terminate() {
        // 杀掉监测服务
        if (sThis != null && sThis.mDeviceMonitor != null) {
            sThis.mDeviceMonitor.stop();
            sThis.mDeviceMonitor = null;
        }

        MonitorThread monitorThread = MonitorThread.getInstance();
        if (monitorThread != null) {
            monitorThread.quit();
        }
    
        sInitialized = false;
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
返回ddmlib是否设置支持监测，和{@link Client}s 运行在 {@link IDevice}s 上的交互。

static boolean getClientSupport() {
    return sClientSupport;
}
1
2
3
返回主机上ADB服务器的套接字地址。

public static InetSocketAddress getSocketAddress() {
    return sSocketAddr;
}
1
2
3
创建一个未链接到任何特定可执行文件的{@link AndroidDebugBridge}。
这个bridge预计adb将继续运营。它将无法 “启动/停止/重启” adb。
如果一个bridge已经启动,它是直接返回,没有改变 (类似的调用 {@link #getBridge()})
@return 一个连接的bridge

public static AndroidDebugBridge createBridge() {
    synchronized (sLock) {
        if (sThis != null) {
            return sThis;
        }
        try {
            sThis = new AndroidDebugBridge();
            sThis.start();
        } catch (InvalidParameterException e) {
            sThis = null;
        }
        // 将更改listeners
        for (IDebugBridgeChangeListener listener : sBridgeListeners) {
            // 我们试图捕获任何异常，这样坏的listener就不会杀死我们的线程
            try {
                listener.bridgeChanged(sThis);
            } catch (Exception e) {
                Log.e(DDMS, e);
            }
        }
        return sThis;
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
从命令行工具的位置创建一个新的debug bridge
任何现有的服务器将被断开连接,除非是相同的位置和 forceNewBridge 设置为false。
命令行工具’adb’ forceNewBridge强制创建一个新bridge的位置，即使一个具有相同位置的已经存在。
连接bridge，或者null如果有错误在创建或连接bridge

@Nullable
public static AndroidDebugBridge createBridge(@NonNull String osLocation,
                                                  boolean forceNewBridge) {
        synchronized (sLock) {
            if (sThis != null) {
                if (sThis.mAdbOsLocation != null && sThis.mAdbOsLocation.equals(osLocation) &&
                        !forceNewBridge) {
                    return sThis;
                } else {
                    // 停止当前服务器
                    sThis.stop();
                }
            }
            try {
                sThis = new AndroidDebugBridge(osLocation);
                if (!sThis.start()) {
                    return null;
                }
            } catch (InvalidParameterException e) {
                sThis = null;
            }
            // 使listeners相信这一变化
            for (IDebugBridgeChangeListener listener : sBridgeListeners) {
                // 我们试图捕获任何异常，这样坏的listener就不会杀死我们的线程
                try {
                    listener.bridgeChanged(sThis);
                } catch (Exception e) {
                    Log.e(DDMS, e);
            }
        }
        return sThis;
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
返回当前debug bridge。如果没有创建可以是null。

@Nullable
    public static AndroidDebugBridge getBridge() {
        return sThis;
    }
1
2
3
4
断开当前debug bridge，并销毁object
这也会停止当前的adb主机服务器。
必须使用{@link #createBridge(String, boolean)}创建新object。

public static void disconnectBridge() {
        synchronized (sLock) {
            if (sThis != null) {
                sThis.stop();
                sThis = null;
                // notify the listeners.
                for (IDebugBridgeChangeListener listener : sBridgeListeners) {
                    // we attempt to catch any exception so that a bad listener doesn't kill our
                    // thread
                    try {
                        listener.bridgeChanged(sThis);
                    } catch (Exception e) {
                        Log.e(DDMS, e);
                    }
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
当一个新的{@link AndroidDebugBridge}连接之时，把listener添加到将要被通知的listeners集合中，通过将定义的消息之一发送给它在{@link IDebugBridgeChangeListener}接口中这种形式被连接。
@param listener 应该通知的listener 。

    public static void addDebugBridgeChangeListener(@NonNull IDebugBridgeChangeListener listener) {
        synchronized (sLock) {
            sBridgeListeners.add(listener);
    
            if (sThis != null) {
                // we attempt to catch any exception so that a bad listener doesn't kill our thread
                try {
                    listener.bridgeChanged(sThis);
                } catch (Exception e) {
                    Log.e(DDMS, e);
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
Removes the listener from the collection of listeners who will be notified when a new
{@link AndroidDebugBridge} is started.
将listener从listeners 集合中移除，当新的{@link AndroidDebugBridge}启动时将通知listeners 。
@param listener 此listener不应该再被通知。

    public static void removeDebugBridgeChangeListener(IDebugBridgeChangeListener listener) {
        synchronized (sLock) {
            sBridgeListeners.remove(listener);
        }
    }
1
2
3
4
5
将listener添加到listeners集合中，当listeners连接一个{@link IDevice}时将被通知，分离，或者当它的属性，或者它的{@link Client}列表改变了，通过向它发送{@link IDeviceChangeListener} interface中的被定义的一条消息
@param listener The listener which should be notified.

    public static void addDeviceChangeListener(@NonNull IDeviceChangeListener listener) {
        sDeviceListeners.add(listener);
    }
1
2
3
Removes the listener from the collection of listeners who will be notified when a {@link IDevice} is connected, disconnected, or when its properties or its {@link Client} list changed.
@param listener The listener which should no longer be notified.

    public static void removeDeviceChangeListener(IDeviceChangeListener listener) {
        sDeviceListeners.remove(listener);
    }
1
2
3
 * Adds the listener to the collection of listeners who will be notified when a {@link Client}
 * property changed, by sending it one of the messages defined in the {@link
 * IClientChangeListener} interface.
 *
 * @param listener The listener which should be notified.
1
2
3
4
5
    public static void addClientChangeListener(IClientChangeListener listener) {
        sClientListeners.add(listener);
    }
1
2
3
 * Removes the listener from the collection of listeners who will be notified when a {@link
 * Client} property changes.
 *
 * @param listener The listener which should no longer be notified.
1
2
3
4
    public static void removeClientChangeListener(IClientChangeListener listener) {
        sClientListeners.remove(listener);
    }
1
2
3
 * Returns the devices.
 * @see #hasInitialDeviceList()
1
2
    @NonNull
    public IDevice[] getDevices() {
        synchronized (sLock) {
            if (mDeviceMonitor != null) {
                return mDeviceMonitor.getDevices();
            }
        }
        return new IDevice[0];
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
 * Returns whether the bridge has acquired the initial list from adb after being created.
 * <p>Calling {@link #getDevices()} right after {@link #createBridge(String, boolean)} will
 * generally result in an empty list. This is due to the internal asynchronous communication
 * mechanism with <code>adb</code> that does not guarantee that the {@link IDevice} list has been
 * built before the call to {@link #getDevices()}.
 * <p>The recommended way to get the list of {@link IDevice} objects is to create a
 * {@link IDeviceChangeListener} object.
1
2
3
4
5
6
7
    public boolean hasInitialDeviceList() {
        if (mDeviceMonitor != null) {
            return mDeviceMonitor.hasInitialDeviceList();
        }

        return false;
    }
1
2
3
4
5
6
7
 * Sets the client to accept debugger connection on the custom "Selected debug port".
 * @param selectedClient the client. Can be null.
1
2
    public void setSelectedClient(Client selectedClient) {
        MonitorThread monitorThread = MonitorThread.getInstance();
        if (monitorThread != null) {
            monitorThread.setSelectedClient(selectedClient);
        }
    }
1
2
3
4
5
6
 * Returns whether the {@link AndroidDebugBridge} object is still connected to the adb daemon.
1
    public boolean isConnected() {
        MonitorThread monitorThread = MonitorThread.getInstance();
        if (mDeviceMonitor != null && monitorThread != null) {
            return mDeviceMonitor.isMonitoring() && monitorThread.getState() != State.TERMINATED;
        }
        return false;
    }
1
2
3
4
5
6
7
返回{@link AndroidDebugBridge}对象试图连接adb守护进程的次数

    public int getConnectionAttemptCount() {
        if (mDeviceMonitor != null) {
            return mDeviceMonitor.getConnectionAttemptCount();
        }
        return -1;
    }
1
2
3
4
5
6
返回{@link AndroidDebugBridge}对象试图重启adb守护进程的次数

    public int getRestartAttemptCount() {
        if (mDeviceMonitor != null) {
            return mDeviceMonitor.getRestartAttemptCount();
        }
        return -1;
    }
1
2
3
4
5
6
新建一个 bridge.
* @param osLocation the location of the command line tool
* @throws InvalidParameterException

    private AndroidDebugBridge(String osLocation) throws InvalidParameterException {
        if (osLocation == null || osLocation.isEmpty()) {
            throw new InvalidParameterException();
        }
        mAdbOsLocation = osLocation;
        try {
            checkAdbVersion();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
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
创建不链接到任何特定adb可执行文件的新bridge

    private AndroidDebugBridge() {
    }
1
2
查询adb的版本号，并检查它至少是{@link #MIN_ADB_VERSION}

    private void checkAdbVersion() throws IOException {
        // default is bad check
        mVersionCheck = false;
        if (mAdbOsLocation == null) {
            return;
        }
        File adb = new File(mAdbOsLocation);
        ListenableFuture<AdbVersion> future = getAdbVersion(adb);
        AdbVersion version;
        try {
            version = future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return;
        } catch (java.util.concurrent.TimeoutException e) {
            String msg = "Unable to obtain result of 'adb version'";
            Log.logAndDisplay(LogLevel.ERROR, ADB, msg);
            return;
        } catch (ExecutionException e) {
            Log.logAndDisplay(LogLevel.ERROR, ADB, e.getCause().getMessage());
            Throwables.propagateIfInstanceOf(e.getCause(), IOException.class);
            return;
        }
        if (version.compareTo(MIN_ADB_VERSION) > 0) {
            mVersionCheck = true;
        } else {
            String message = String.format(
                    "Required minimum version of adb: %1$s."
                            + "Current version is %2$s", MIN_ADB_VERSION, version);
            Log.logAndDisplay(LogLevel.ERROR, ADB, message);
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
    public static ListenableFuture<AdbVersion> getAdbVersion(@NonNull final File adb) {
        final SettableFuture<AdbVersion> future = SettableFuture.create();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProcessBuilder pb = new ProcessBuilder(adb.getPath(), "version");
                pb.redirectErrorStream(true);
                Process p = null;
                try {
                    p = pb.start();
                } catch (IOException e) {
                    future.setException(e);
                    return;
                }
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        AdbVersion version = AdbVersion.parseFrom(line);
                        if (version != AdbVersion.UNKNOWN) {
                            future.set(version);
                            return;
                        }
                        sb.append(line);
                        sb.append('\n');
                    }
                } catch (IOException e) {
                    future.setException(e);
                    return;
                } finally {
                    try {
                        br.close();
                    } catch (IOException e) {
                        future.setException(e);
                    }
                }
                future.setException(new RuntimeException(
                        "Unable to detect adb version, adb output: " + sb.toString()));
            }
        }, "Obtaining adb version").start();
        return future;
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
 * Starts the debug bridge.
 *
 * @return true if success.
1
2
3
    boolean start() {
        if (mAdbOsLocation != null && sAdbServerPort != 0 && (!mVersionCheck || !startAdb())) {
            return false;
        }
        mStarted = true;
        // now that the bridge is connected, we start the underlying services.
        mDeviceMonitor = new DeviceMonitor(this);
        mDeviceMonitor.start();
        return true;
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
 * Kills the debug bridge, and the adb host server.
 * @return true if success
1
2
    boolean stop() {
        // if we haven't started we return false;
        if (!mStarted) {
            return false;
        }
        // kill the monitoring services
        if (mDeviceMonitor != null) {
            mDeviceMonitor.stop();
            mDeviceMonitor = null;
        }
        if (!stopAdb()) {
            return false;
        }
        mStarted = false;
        return true;
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
 * Restarts adb, but not the services around it.
 * @return true if success.
1
2
    public boolean restart() {
        if (mAdbOsLocation == null) {
            Log.e(ADB,
                    "Cannot restart adb when AndroidDebugBridge is created without the location of adb."); //$NON-NLS-1$
            return false;
        }
        if (sAdbServerPort == 0) {
            Log.e(ADB, "ADB server port for restarting AndroidDebugBridge is not set."); //$NON-NLS-1$
            return false;
        }
        if (!mVersionCheck) {
            Log.logAndDisplay(LogLevel.ERROR, ADB,
                    "Attempting to restart adb, but version check failed!"); //$NON-NLS-1$
            return false;
        }
        synchronized (this) {
            stopAdb();
            boolean restart = startAdb();
            if (restart && mDeviceMonitor == null) {
                mDeviceMonitor = new DeviceMonitor(this);
                mDeviceMonitor.start();
            }
            return restart;
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
 * Notify the listener of a new {@link IDevice}.
 * <p>
 * The notification of the listeners is done in a synchronized block. It is important to
 * expect the listeners to potentially access various methods of {@link IDevice} as well as
 * {@link #getDevices()} which use internal locks.
 * <p>
 * For this reason, any call to this method from a method of {@link DeviceMonitor},
 * {@link IDevice} which is also inside a synchronized block, should first synchronize on
 * the {@link AndroidDebugBridge} lock. Access to this lock is done through {@link #getLock()}.
 * @param device the new <code>IDevice</code>.
 * @see #getLock()
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
    static void deviceConnected(@NonNull IDevice device) {
        for (IDeviceChangeListener listener : sDeviceListeners) {
            // we attempt to catch any exception so that a bad listener doesn't kill our thread
            try {
                listener.deviceConnected(device);
            } catch (Exception e) {
                Log.e(DDMS, e);
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
 * Notify the listener of a disconnected {@link IDevice}.
 * <p>
 * The notification of the listeners is done in a synchronized block. It is important to
 * expect the listeners to potentially access various methods of {@link IDevice} as well as
 * {@link #getDevices()} which use internal locks.
 * <p>
 * For this reason, any call to this method from a method of {@link DeviceMonitor},
 * {@link IDevice} which is also inside a synchronized block, should first synchronize on
 * the {@link AndroidDebugBridge} lock. Access to this lock is done through {@link #getLock()}.
 * @param device the disconnected <code>IDevice</code>.
 * @see #getLock()
 */
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
  static void deviceDisconnected(@NonNull IDevice device) {
        for (IDeviceChangeListener listener : sDeviceListeners) {
            // we attempt to catch any exception so that a bad listener doesn't kill our
            // thread
            try {
                listener.deviceDisconnected(device);
            } catch (Exception e) {
                Log.e(DDMS, e);
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
 * Notify the listener of a modified {@link IDevice}.
 * <p>
 * The notification of the listeners is done in a synchronized block. It is important to
 * expect the listeners to potentially access various methods of {@link IDevice} as well as
 * {@link #getDevices()} which use internal locks.
 * <p>
 * For this reason, any call to this method from a method of {@link DeviceMonitor},
 * {@link IDevice} which is also inside a synchronized block, should first synchronize on
 * the {@link AndroidDebugBridge} lock. Access to this lock is done through {@link #getLock()}.
 * @param device the modified <code>IDevice</code>.
 * @see #getLock()
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
    static void deviceChanged(@NonNull IDevice device, int changeMask) {
        // Notify the listeners
        for (IDeviceChangeListener listener : sDeviceListeners) {
            // we attempt to catch any exception so that a bad listener doesn't kill our
            // thread
            try {
                listener.deviceChanged(device, changeMask);
            } catch (Exception e) {
                Log.e(DDMS, e);
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
 * Notify the listener of a modified {@link Client}.
 * <p>
 * The notification of the listeners is done in a synchronized block. It is important to
 * expect the listeners to potentially access various methods of {@link IDevice} as well as
 * {@link #getDevices()} which use internal locks.
 * <p>
 * For this reason, any call to this method from a method of {@link DeviceMonitor},
 * {@link IDevice} which is also inside a synchronized block, should first synchronize on
 * the {@link AndroidDebugBridge} lock. Access to this lock is done through {@link #getLock()}.
 * @param client the modified <code>Client</code>.
 * @param changeMask the mask indicating what changed in the <code>Client</code>
 * @see #getLock()
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
    static void clientChanged(@NonNull Client client, int changeMask) {
        // Notify the listeners
        for (IClientChangeListener listener : sClientListeners) {
            // we attempt to catch any exception so that a bad listener doesn't kill our
            // thread
            try {
                listener.clientChanged(client, changeMask);
            } catch (Exception e) {
                Log.e(DDMS, e);
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
 * Returns the {@link DeviceMonitor} object.
1
    DeviceMonitor getDeviceMonitor() {
        return mDeviceMonitor;
    }
1
2
3
 * Starts the adb host side server.
 * @return true if success
1
2
    synchronized boolean startAdb() {
        if (mAdbOsLocation == null) {
            Log.e(ADB,
                "Cannot start adb when AndroidDebugBridge is created without the location of adb."); //$NON-NLS-1$
            return false;
        }
        if (sAdbServerPort == 0) {
            Log.w(ADB, "ADB server port for starting AndroidDebugBridge is not set."); //$NON-NLS-1$
            return false;
        }
        Process proc;
        int status = -1;
        String[] command = getAdbLaunchCommand("start-server");
        String commandString = Joiner.on(' ').join(command);
        try {
            Log.d(DDMS, String.format("Launching '%1$s' to ensure ADB is running.", commandString));
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            if (DdmPreferences.getUseAdbHost()) {
                String adbHostValue = DdmPreferences.getAdbHostValue();
                if (adbHostValue != null && !adbHostValue.isEmpty()) {
                    //TODO : check that the String is a valid IP address
                    Map<String, String> env = processBuilder.environment();
                    env.put("ADBHOST", adbHostValue);
                }
            }
            proc = processBuilder.start();
            ArrayList<String> errorOutput = new ArrayList<String>();
            ArrayList<String> stdOutput = new ArrayList<String>();
            status = grabProcessOutput(proc, errorOutput, stdOutput, false /* waitForReaders */);
        } catch (IOException ioe) {
            Log.e(DDMS, "Unable to run 'adb': " + ioe.getMessage()); //$NON-NLS-1$
            // we'll return false;
        } catch (InterruptedException ie) {
            Log.e(DDMS, "Unable to run 'adb': " + ie.getMessage()); //$NON-NLS-1$
            // we'll return false;
        }
        if (status != 0) {
            Log.e(DDMS,
                String.format("'%1$s' failed -- run manually if necessary", commandString)); //$NON-NLS-1$
            return false;
        } else {
            Log.d(DDMS, String.format("'%1$s' succeeded", commandString)); //$NON-NLS-1$
            return true;
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
    private String[] getAdbLaunchCommand(String option) {
        List<String> command = new ArrayList<String>(4);
        command.add(mAdbOsLocation);
        if (sAdbServerPort != DEFAULT_ADB_PORT) {
            command.add("-P"); //$NON-NLS-1$
            command.add(Integer.toString(sAdbServerPort));
        }
        command.add(option);
        return command.toArray(new String[command.size()]);
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
 * Stops the adb host side server.
 *
 * @return true if success
1
2
3
    private synchronized boolean stopAdb() {
        if (mAdbOsLocation == null) {
            Log.e(ADB,
                "Cannot stop adb when AndroidDebugBridge is created without the location of adb.");
            return false;
        }
        if (sAdbServerPort == 0) {
            Log.e(ADB, "ADB server port for restarting AndroidDebugBridge is not set");
            return false;
        }
        Process proc;
        int status = -1;
        String[] command = getAdbLaunchCommand("kill-server"); //$NON-NLS-1$
        try {
            proc = Runtime.getRuntime().exec(command);
            status = proc.waitFor();
        }
        catch (IOException ioe) {
            // we'll return false;
        }
        catch (InterruptedException ie) {
            // we'll return false;
        }
        String commandString = Joiner.on(' ').join(command);
        if (status != 0) {
            Log.w(DDMS, String.format("'%1$s' failed -- run manually if necessary", commandString));
            return false;
        } else {
            Log.d(DDMS, String.format("'%1$s' succeeded", commandString));
            return true;
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
 * Get the stderr/stdout outputs of a process and return when the process is done.
 * Both <b>must</b> be read or the process will block on windows.
 * @param process The process to get the output from
 * @param errorOutput The array to store the stderr output. cannot be null.
 * @param stdOutput The array to store the stdout output. cannot be null.
 * @param waitForReaders if true, this will wait for the reader threads.
 * @return the process return code.
 * @throws InterruptedException
1
2
3
4
5
6
7
8
    private static int grabProcessOutput(final Process process, final ArrayList<String> errorOutput,
      final ArrayList<String> stdOutput, boolean waitForReaders)
            throws InterruptedException {
        assert errorOutput != null;
        assert stdOutput != null;
        // read the lines as they come. if null is returned, it's
        // because the process finished
        Thread t1 = new Thread("adb:stderr reader") { //$NON-NLS-1$
            @Override
            public void run() {
                // create a buffer to read the stderr output
                InputStreamReader is = new InputStreamReader(process.getErrorStream(),
                  Charsets.UTF_8);
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            Log.e(ADB, line);
                            errorOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    // do nothing.
                } finally {
                    Closeables.closeQuietly(errReader);
                }
            }
        };
        Thread t2 = new Thread("adb:stdout reader") { //$NON-NLS-1$
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream(),
                  Charsets.UTF_8);
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            Log.d(ADB, line);
                            stdOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                    // do nothing.
                } finally {
                    Closeables.closeQuietly(outReader);
                }
            }
        };
        t1.start();
        t2.start();
        // it looks like on windows process#waitFor() can return
        // before the thread have filled the arrays, so we wait for both threads and the
        // process itself.
        if (waitForReaders) {
            try {
                t1.join();
            } catch (InterruptedException e) {
            }
            try {
                t2.join();
            } catch (InterruptedException e) {
            }
        }
        // get the return code from the process
        return process.waitFor();
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
 * Returns the singleton lock used by this class to protect any access to the listener.
 * <p>
 * This includes adding/removing listeners, but also notifying listeners of new bridges,
 * devices, and clients.
1
2
3
4
    private static Object getLock() {
        return sLock;
    }
1
2
3
 * Instantiates sSocketAddr with the address of the host's adb process.
1
    private static void initAdbSocketAddr() {
        try {
            sAdbServerPort = getAdbServerPort();
            sHostAddr = InetAddress.getByName(DEFAULT_ADB_HOST);
            sSocketAddr = new InetSocketAddress(sHostAddr, sAdbServerPort);
        } catch (UnknownHostException e) {
            // localhost should always be known.
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
 * Returns the port where adb server should be launched. This looks at:
 * <ol>
 *     <li>The system property ANDROID_ADB_SERVER_PORT</li>
 *     <li>The environment variable ANDROID_ADB_SERVER_PORT</li>
 *     <li>Defaults to {@link #DEFAULT_ADB_PORT} if neither the system property nor the env var
 *     are set.</li>
 * </ol>
 *
 * @return The port number where the host's adb should be expected or started.
1
2
3
4
5
6
7
8
9
    private static int getAdbServerPort() {
        // check system property
        Integer prop = Integer.getInteger(SERVER_PORT_ENV_VAR);
        if (prop != null) {
            try {
                return validateAdbServerPort(prop.toString());
            } catch (IllegalArgumentException e) {
                String msg = String.format(
                        "Invalid value (%1$s) for ANDROID_ADB_SERVER_PORT system property.",
                        prop);
                Log.w(DDMS, msg);
            }
        }
        // when system property is not set or is invalid, parse environment property
        try {
            String env = System.getenv(SERVER_PORT_ENV_VAR);
            if (env != null) {
                return validateAdbServerPort(env);
            }
        } catch (SecurityException ex) {
            // A security manager has been installed that doesn't allow access to env vars.
            // So an environment variable might have been set, but we can't tell.
            // Let's log a warning and continue with ADB's default port.
            // The issue is that adb would be started (by the forked process having access
            // to the env vars) on the desired port, but within this process, we can't figure out
            // what that port is. However, a security manager not granting access to env vars
            // but allowing to fork is a rare and interesting configuration, so the right
            // thing seems to be to continue using the default port, as forking is likely to
            // fail later on in the scenario of the security manager.
            Log.w(DDMS,
                    "No access to env variables allowed by current security manager. "
                            + "If you've set ANDROID_ADB_SERVER_PORT: it's being ignored.");
        } catch (IllegalArgumentException e) {
            String msg = String.format(
                    "Invalid value (%1$s) for ANDROID_ADB_SERVER_PORT environment variable (%2$s).",
                    prop, e.getMessage());
            Log.w(DDMS, msg);
        }
        // use default port if neither are set
        return DEFAULT_ADB_PORT;
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
 * Returns the integer port value if it is a valid value for adb server port
 * @param adbServerPort adb server port to validate
 * @return {@code adbServerPort} as a parsed integer
 * @throws IllegalArgumentException when {@code adbServerPort} is not bigger than 0 or it is
 * not a number at all
1
2
3
4
5
    private static int validateAdbServerPort(@NonNull String adbServerPort)
            throws IllegalArgumentException {
        try {
            // C tools (adb, emulator) accept hex and octal port numbers, so need to accept them too
            int port = Integer.decode(adbServerPort);
            if (port <= 0 || port >= 65535) {
                throw new IllegalArgumentException("Should be > 0 and < 65535");
            }
            return port;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid port number");
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
文章知识点与官方知识档案匹配，可进一步学习相关知识
Java技能树首页概览150832 人正在系统学习中