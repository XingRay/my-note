ddmlib-25.3.0.jar IDevice类

雲驊

于 2019-10-27 23:44:09 发布

阅读量772
 收藏 1

点赞数 1
分类专栏： Jar包文档
版权

Jar包文档
专栏收录该内容
2 篇文章0 订阅
订阅专栏
ddmlib获取安卓设备信息。
ddmlib的版本是25.3.0

+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
以下是class类
public final class IDevice
一个设备。它可以是物理设备或模拟器
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++

String PROP_BUILD_VERSION = "ro.build.version.release";
String PROP_BUILD_API_LEVEL = "ro.build.version.sdk";
String PROP_BUILD_CODENAME = "ro.build.version.codename";
String PROP_BUILD_TAGS = "ro.build.tags";
String PROP_BUILD_TYPE = "ro.build.type";
String PROP_DEVICE_MODEL = "ro.product.model";
String PROP_DEVICE_MANUFACTURER = "ro.product.manufacturer";
String PROP_DEVICE_CPU_ABI_LIST = "ro.product.cpu.abilist";
String PROP_DEVICE_CPU_ABI = "ro.product.cpu.abi";
String PROP_DEVICE_CPU_ABI2 = "ro.product.cpu.abi2";
String PROP_BUILD_CHARACTERISTICS = "ro.build.characteristics";
String PROP_DEVICE_DENSITY = "ro.sf.lcd_density";
String PROP_DEVICE_EMULATOR_DENSITY = "qemu.sf.lcd_density";
String PROP_DEVICE_LANGUAGE = "persist.sys.language";
String PROP_DEVICE_REGION = "persist.sys.country";
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
String PROP_DEBUGGABLE = "ro.debuggable";
1
/** Serial number of the first connected emulator. */
String FIRST_EMULATOR_SN = "emulator-5554"; //$NON-NLS-1$
/** Device change bit mask: {@link DeviceState} change. */
int CHANGE_STATE = 0x0001;
/** Device change bit mask: {@link Client} list change. */
int CHANGE_CLIENT_LIST = 0x0002;
/** Device change bit mask: build info change. */
int CHANGE_BUILD_INFO = 0x0004;
1
2
3
4
5
6
7
8
设备级软件功能

enum Feature {
    SCREEN_RECORD,      // 屏幕录像机可用?
    PROCSTATS,          // 可以使用procstats服务(dumpsys procstats)
}
1
2
3
4
设备级硬件功能

enum HardwareFeature {
    WATCH("watch"),
    TV("tv");

    private final String mCharacteristic;
    
    HardwareFeature(String characteristic) {
        mCharacteristic = characteristic;
    }
    
    public String getCharacteristic() {
        return mCharacteristic;
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
@deprecated Use {@link #PROP_BUILD_API_LEVEL}.

@Deprecated
String PROP_BUILD_VERSION_NUMBER = PROP_BUILD_API_LEVEL;
1
2
String MNT_EXTERNAL_STORAGE = "EXTERNAL_STORAGE"; //$NON-NLS-1$
String MNT_ROOT = "ANDROID_ROOT"; //$NON-NLS-1$
String MNT_DATA = "ANDROID_DATA"; //$NON-NLS-1$
1
2
3
设备的状态

enum DeviceState {
    BOOTLOADER("bootloader"), //$NON-NLS-1$
    OFFLINE("offline"), //$NON-NLS-1$
    ONLINE("device"), //$NON-NLS-1$
    RECOVERY("recovery"), //$NON-NLS-1$
    /** 设备通过“adb sideload”或恢复菜单处于“sideload”状态 */
    SIDELOAD("sideload"), //$NON-NLS-1$
    UNAUTHORIZED("unauthorized"), //$NON-NLS-1$
    DISCONNECTED("disconnected"), //$NON-NLS-1$
    ;

    private String mState;
    
    DeviceState(String state) {
        mState = state;
    }
    
    /**
     * 从adb devices返回的字符串中返回{@link DeviceState}
     * @param声明设备状态
     * @return {@link DeviceState}对象或null(如果状态未知)
     */
    @Nullable
    public static DeviceState getState(String state) {
        for (DeviceState deviceState : values()) {
            if (deviceState.mState.equals(state)) {
                return deviceState;
            }
        }
        return null;
    }
    
    public String getState() {
        return mState;
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
在设备上创建的Unix域套接字的名称空间

enum DeviceUnixSocketNamespace {
    ABSTRACT("localabstract"),      //$NON-NLS-1$
    FILESYSTEM("localfilesystem"),  //$NON-NLS-1$
    RESERVED("localreserved");      //$NON-NLS-1$

    private String mType;
    
    DeviceUnixSocketNamespace(String type) {
        mType = type;
    }
    
    String getType() {
        return mType;
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
返回设备的序列号

@NonNull
String getSerialNumber();
1
2
返回模拟器正在运行的AVD的名称
只有当{@link #isEmulator()}返回true时才有效
If the emulator is not running any AVD (for instance it’s running from an Android source
tree build), this method will return “<build>”.
@return the name of the AVD or null if there isn’t any.

@Nullable
String getAvdName();
1
2
/**
 * Returns the state of the device.
 */
 DeviceState getState();
 1
 2
 3
 4
 返回缓存的设备属性
 它包含的是’getprop’
 @deprecated use {@link #getSystemProperty(String)} instead

@Deprecated
Map<String, String> getProperties();
1
2
返回此设备的属性数
@deprecated实现细节

@Deprecated
int getPropertyCount();
1
2
该方法尝试通过{@link #getSystemProperty(String)}检索一个属性，等待时间非常短
注意:如果希望控制超时，最好使用{@link #getSystemProperty(String)}
@param指定要返回的值的名称
@return the value or null 如果属性值不是立即可用的

@Nullable
String getProperty(@NonNull String name);
1
2
如果属性被缓存，返回true

boolean arePropertiesSet();
1
{@link #getProperty(String)}的一个变体，将尝试直接从设备中检索给定的属性，而不使用缓存
* This method should (only) be used for any volatile properties.
*
* @param name the name of the value to return.
* @return the value or null if the property does not exist
* @throws TimeoutException in case of timeout on the connection.
* @throws AdbCommandRejectedException if adb rejects the command
* @throws ShellCommandUnresponsiveException in case the shell command doesn’t send output for a
* given time.
* @throws IOException in case of I/O error on the connection.
* @deprecated use {@link #getSystemProperty(String)}

@Deprecated
String getPropertySync(String name) throws TimeoutException,
        AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException;
1
2
3
试图从缓存中检索属性的{@link #getProperty(String)}和{@link #getPropertySync(String)}的组合。如果没有找到，将同步尝试直接查询设备，如果成功，将重新填充缓存。
*
* @param name the name of the value to return.
* @return the value or null if the property does not exist
* @throws TimeoutException in case of timeout on the connection.
* @throws AdbCommandRejectedException if adb rejects the command
* @throws ShellCommandUnresponsiveException in case the shell command doesn’t send output for a
* given time.
* @throws IOException in case of I/O error on the connection.
* @deprecated use {@link #getSystemProperty(String)} instead
*/

@Deprecated
String getPropertyCacheOrSync(String name) throws TimeoutException,
        AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException;
1
2
3
返回此设备是否支持给定的软件特性

boolean supportsFeature(@NonNull Feature feature);
1
/** Returns whether this device supports the given hardware feature. */
1
boolean supportsFeature(@NonNull HardwareFeature feature);
1
返回一个挂载点
@param 指定要返回的挂载点的名称
@see #MNT_EXTERNAL_STORAGE
@see #MNT_ROOT
@see #MNT_DATA

@Nullable
String getMountPoint(@NonNull String name);
1
2
如果设备已准备好，则返回
@return true if {@link #getState()} returns {@link DeviceState#ONLINE}.

boolean isOnline();
1
Returns true 如果设备是模拟器

boolean isEmulator();
1
如果设备脱机，则返回。
@return true if {@link #getState()} returns {@link DeviceState#OFFLINE}.

boolean isOffline();
1
如果设备处于引导加载程序模式，则返回
@return true if {@link #getState()} returns {@link DeviceState#BOOTLOADER}.

boolean isBootLoader();
1
返回此 {@link Device} 是否有 {@link Client}s.

boolean hasClients();
1
返回客户端数组

Client[] getClients();
1
根据应用程序名称返回{@link Client}
@param applicationName 应用程序的名称
@return the Client object or null 如果没有找到匹配项

Client getClient(String applicationName);
1
返回一个{@link SyncService}对象，用于将文件推入/拉出设备
@return null if the SyncService couldn’t be created. This can happen if adb refuse to open the connection because the {@link IDevice} is invalid (or got disconnected).
@throws TimeoutException in case of timeout on the connection.
@throws AdbCommandRejectedException if adb rejects the command
@throws IOException if the connection with adb failed.

SyncService getSyncService()
        throws TimeoutException, AdbCommandRejectedException, IOException;
1
2
为该设备返回一个{@link FileListingService}。

FileListingService getFileListingService();
1
获取设备的屏幕截图并以{@link RawImage}的形式返回
@return the screenshot as a RawImage or null if something went wrong.
@throws TimeoutException in case of timeout on the connection.
@throws AdbCommandRejectedException if adb rejects the command
@throws IOException in case of I/O error on the connection.

RawImage getScreenshot() throws TimeoutException, AdbCommandRejectedException, IOException;
1
RawImage getScreenshot(long timeout, TimeUnit unit)
        throws TimeoutException, AdbCommandRejectedException, IOException;
1
2
/**
 * Initiates screen recording on the device if the device supports {@link Feature#SCREEN_RECORD}.
 */
 1
 2
 3
 void startScreenRecorder(@NonNull String remoteFilePath,
        @NonNull ScreenRecorderOptions options, @NonNull IShellOutputReceiver receiver) throws
        TimeoutException, AdbCommandRejectedException, IOException,
        ShellCommandUnresponsiveException;
 1
 2
 3
 4
 @deprecated Use {@link #executeShellCommand(String, IShellOutputReceiver, long, java.util.concurrent.TimeUnit)}.

@Deprecated
void executeShellCommand(String command, IShellOutputReceiver receiver,
        int maxTimeToOutputResponse)
        throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
        IOException;
1
2
3
4
5
在设备上执行shell命令，并将结果发送给一个receiver，
这类似于打电话给executeShellCommand(command, receiver, DdmPreferences.getTimeOut()).
@param command shell命令执行
@param receiver 将接收shell输出的{@link IShellOutputReceiver}命令
@throws TimeoutException 在连接超时的情况下
@throws AdbCommandRejectedException 如果adb拒绝命令
@throws ShellCommandUnresponsiveException 如果shell命令在给定的时间内没有发送输出。
@throws IOException 以防连接上的I/O错误。
@see #executeShellCommand(String, IShellOutputReceiver, int)
@see DdmPreferences#getTimeOut()

void executeShellCommand(String command, IShellOutputReceiver receiver)
        throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException,
        IOException;
1
2
3
运行事件日志服务并将事件日志输出到{@link LogReceiver}。
*

This call is blocking until {@link LogReceiver#isCancelled()} returns true.
* @param receiver the receiver to receive the event log entries.
* @throws TimeoutException in case of timeout on the connection. This can only be thrown if the
* timeout happens during setup. Once logs start being received, no timeout will occur as it’s
* not possible to detect a difference between no log and timeout.
* @throws AdbCommandRejectedException if adb rejects the command
* @throws IOException in case of I/O error on the connection.
*/

void runEventLogService(LogReceiver receiver)
        throws TimeoutException, AdbCommandRejectedException, IOException;
1
2
/**
 * Runs the log service for the given log and outputs the log to the {@link LogReceiver}.
 * <p>This call is blocking until {@link LogReceiver#isCancelled()} returns true.
 *
 * @param logname the logname of the log to read from.
 * @param receiver the receiver to receive the event log entries.
 * @throws TimeoutException in case of timeout on the connection. This can only be thrown if the
 *            timeout happens during setup. Once logs start being received, no timeout will
 *            occur as it's not possible to detect a difference between no log and timeout.
 * @throws AdbCommandRejectedException if adb rejects the command
 * @throws IOException in case of I/O error on the connection.
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
 void runLogService(String logname, LogReceiver receiver)
        throws TimeoutException, AdbCommandRejectedException, IOException;
 1
 2
 /**
 * Creates a port forwarding between a local and a remote port.
 *
 * @param localPort the local port to forward
 * @param remotePort the remote port.
 * @throws TimeoutException in case of timeout on the connection.
 * @throws AdbCommandRejectedException if adb rejects the command
 * @throws IOException in case of I/O error on the connection.
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
 void createForward(int localPort, int remotePort)
        throws TimeoutException, AdbCommandRejectedException, IOException;
 1
 2
 /**
 * Creates a port forwarding between a local TCP port and a remote Unix Domain Socket.
 *
 * @param localPort the local port to forward
 * @param remoteSocketName name of the unix domain socket created on the device
 * @param namespace namespace in which the unix domain socket was created
 * @throws TimeoutException in case of timeout on the connection.
 * @throws AdbCommandRejectedException if adb rejects the command
 * @throws IOException in case of I/O error on the connection.
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
 void createForward(int localPort, String remoteSocketName,
        DeviceUnixSocketNamespace namespace)
        throws TimeoutException, AdbCommandRejectedException, IOException;
 1
 2
 3
 /**
 * Removes a port forwarding between a local and a remote port.
 *
 * @param localPort the local port to forward
 * @param remotePort the remote port.
 * @throws TimeoutException in case of timeout on the connection.
 * @throws AdbCommandRejectedException if adb rejects the command
 * @throws IOException in case of I/O error on the connection.
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
 void removeForward(int localPort, int remotePort)
        throws TimeoutException, AdbCommandRejectedException, IOException;
 1
 2
 /**
 * Removes an existing port forwarding between a local and a remote port.
 *
 * @param localPort the local port to forward
 * @param remoteSocketName the remote unix domain socket name.
 * @param namespace namespace in which the unix domain socket was created
 * @throws TimeoutException in case of timeout on the connection.
 * @throws AdbCommandRejectedException if adb rejects the command
 * @throws IOException in case of I/O error on the connection.
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
 void removeForward(int localPort, String remoteSocketName,
        DeviceUnixSocketNamespace namespace)
        throws TimeoutException, AdbCommandRejectedException, IOException;
 1
 2
 3
 /**
 * Returns the name of the client by pid or <code>null</code> if pid is unknown
 * @param pid the pid of the client.
 */
 1
 2
 3
 4
 String getClientName(int pid);
 1
 Push 一个文件
 @param 本地文件路径
 @param 远程文件路径。
 @throws IOException，以防连接上的I/O错误。
 @throws 如果adb拒绝该命令，则AdbCommandRejectedException
 @throws 超时读取设备响应时的TimeoutException
 @throws 如果文件不能被推送，则使用SyncException

void pushFile(String local, String remote)
        throws IOException, AdbCommandRejectedException, TimeoutException, SyncException;
1
2
/**
 * Pulls a single file.
 *
 * @param remote the full path to the remote file
 * @param local The local destination.
 *
 * @throws IOException in case of an IO exception.
 * @throws AdbCommandRejectedException if adb rejects the command
 * @throws TimeoutException in case of a timeout reading responses from the device.
 * @throws SyncException in case of a sync exception.
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
 void pullFile(String remote, String local)
        throws IOException, AdbCommandRejectedException, TimeoutException, SyncException;
 1
 2
 /**
 * Installs an Android application on device. This is a helper method that combines the
 * syncPackageToDevice, installRemotePackage, and removePackage steps
 *
 * @param packageFilePath the absolute file system path to file on local host to install
 * @param reinstall set to <code>true</code> if re-install of app should be performed
 * @param extraArgs optional extra arguments to pass. See 'adb shell pm install --help' for
 *            available options.
 * @throws InstallException if the installation fails.
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
 void installPackage(String packageFilePath, boolean reinstall, String... extraArgs)
        throws InstallException;
 1
 2
 /**
 * Installs an Android application made of several APK files (one main and 0..n split packages)
 *
 * @param apks list of apks to install (1 main APK + 0..n split apks)
 * @param reinstall set to <code>true</code> if re-install of app should be performed
 * @param installOptions optional extra arguments to pass. See 'adb shell pm install --help' for
 *            available options.
 * @param timeout installation timeout
 * @param timeoutUnit {@link TimeUnit} corresponding to the timeout parameter
 * @throws InstallException if the installation fails.
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
 void installPackages(@NonNull List<File> apks, boolean reinstall,
        @NonNull List<String> installOptions, long timeout, @NonNull TimeUnit timeoutUnit)
        throws InstallException;
 1
 2
 3
 /**
 * Pushes a file to device
 *
 * @param localFilePath the absolute path to file on local host
 * @return {@link String} destination path on device for file
 * @throws TimeoutException in case of timeout on the connection.
 * @throws AdbCommandRejectedException if adb rejects the command
 * @throws IOException in case of I/O error on the connection.
 * @throws SyncException if an error happens during the push of the package on the device.
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
 String syncPackageToDevice(String localFilePath)
        throws TimeoutException, AdbCommandRejectedException, IOException, SyncException;
 1
 2
 /**
 * Installs the application package that was pushed to a temporary location on the device.
 *
 * @param remoteFilePath absolute file path to package file on device
 * @param reinstall set to <code>true</code> if re-install of app should be performed
 * @param extraArgs optional extra arguments to pass. See 'adb shell pm install --help' for
 *            available options.
 * @throws InstallException if the installation fails.
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
 void installRemotePackage(String remoteFilePath, boolean reinstall,
        String... extraArgs) throws InstallException;
 1
 2
 /**
 * Removes a file from device.
 *
 * @param remoteFilePath path on device of file to remove
 * @throws InstallException if the installation fails.
 */
 1
 2
 3
 4
 5
 6
 void removeRemotePackage(String remoteFilePath) throws InstallException;
 1
 /**
 * Uninstalls an package from the device.
 *
 * @param packageName the Android application package name to uninstall
 * @return a {@link String} with an error code, or <code>null</code> if success.
 * @throws InstallException if the uninstallation fails.
 */
 1
 2
 3
 4
 5
 6
 7
 String uninstallPackage(String packageName) throws InstallException;
 1
 重启设备
 @param into是 要重新启动到的引导加载程序名称，或仅重新启动设备的空值
 @throws 连接超时时的TimeoutException
 @throws AdbCommandRejectedException if adb rejects the command
 @throws IOException

void reboot(String into)
        throws TimeoutException, AdbCommandRejectedException, IOException;
1
2
请求adb守护进程成为设备上的root
* This may silently fail, and can only succeed on developer builds.
* See “adb root” for more information.
*
* @throws TimeoutException in case of timeout on the connection.
* @throws AdbCommandRejectedException if adb rejects the command.
* @throws ShellCommandUnresponsiveException if the root status cannot be queried.
* @throws IOException
* @return true if the adb daemon is running as root, otherwise false.
*/

boolean root() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException;
1
/**
 * Queries the current root-status of the device.
 * See "adb root" for more information.
 *
 * @throws TimeoutException in case of timeout on the connection.
 * @throws AdbCommandRejectedException if adb rejects the command.
 * @return true if the adb daemon is running as root, otherwise false.
 */
 1
 2
 3
 4
 5
 6
 7
 8
 boolean isRoot() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException;
 1
 返回设备的电池水平，从0到100%。
 电池电量可能被缓存。只有在上次查询成功后的5分钟内，才查询设备的电池电量。
 @return the battery level or null 如果无法取回
 @deprecated use {@link #getBattery()}

@Deprecated
Integer getBatteryLevel() throws TimeoutException,
        AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException;
1
2
3
返回设备的电池水平，从0到100%。
电池电量可能被缓存。只有在freshnessMs ms自上次查询成功后已过期时，才查询设备的电池电量。
@param freshnessMs
@return the battery level or null if it could not be retrieved
@throws ShellCommandUnresponsiveException
@deprecated use {@link #getBattery(long, TimeUnit)}

@Deprecated
Integer getBatteryLevel(long freshnessMs) throws TimeoutException,
        AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException;
1
2
3
返回设备的电池水平，从0到100%
电池电量可能被缓存。只有在上次查询成功后的5分钟内，才查询设备的电池电量。
@return一个{@link Future}，可以用来查询电池电量。未来会返回一个{@link ExecutionException}如果电池电量无法恢复。
@return一个{@link Future}，可以用来查询电池电量。

@NonNull
Future<Integer> getBattery();
1
2
返回设备的电池水平，从0到100%
电池电量可能被缓存。只有当freshness Time间自上次查询成功后已过期时，才查询设备的电池电量。
@param freshnessTime所需的最新电池水平
freshnessTime的{@link timeUnit}
@return一个{@link Future}，可以用来查询电池电量。未来会返回一个{@link ExecutionException}如果电池电量无法恢复。

@NonNull
Future<Integer> getBattery(long freshnessTime, @NonNull TimeUnit timeUnit);
1
2
返回此设备支持的ABIs。
ABIs按首选顺序排序，第一个ABI是首选的。
@return the list of ABIs.

@NonNull
List<String> getAbis();
1
2
通过读取系统属性值，返回设备屏幕的密度桶
{@link #PROP_DEVICE_DENSITY}.
@return 密度，如果无法确定，则返回-1

int getDensity();
1
返回用户的语言
@return 用户的语言，如果未知则为null

@Nullable
String getLanguage();
1
2
返回用户所在的区域
@return 用户的区域，如果未知则为空

@Nullable
String getRegion();
1
2
/**
 * Returns the API level of the device.
 *
 * @return the API level if it can be determined, {@link AndroidVersion#DEFAULT} otherwise.
 */
 1
 2
 3
 4
 5
 @NonNull
 AndroidVersion getVersion();
 1
 2