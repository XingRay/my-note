# DDMLIB的源码解析

在 Android 应用开发中，我们一般会使用到 Android Studio 的各种开发工具。比如过滤打印log的 logcat ；获取 App 的View树的 Layout Inspector;以及对 App 进行debug 调试的 Debug等等。上述提到的Android Studio提供的功能都离不开DDMLIB。

DDMLIB 是对Android的adb 命令进行的一层java封装。ddmlib内部帮你封装了一个个的adb命令，你可以通过调用ddmlib提供的接口发送相应的adb命令，ddmlib会接收adb的响应，并解析数据回调给我们进行处理。

## 预备知识

### adb介绍

adb是Android调试桥，可以执行各种设备操作。如图，adb包含三部分：

客户端：用来发送命令，运行在PC
守护程序（adbd）：用于在手机或者模拟器上执行命令
服务器：用于管理客户端和adbd之间的通信

### NIO介绍

在ddmlib中使用nio与服务器进行通信，这里介绍一下nio。nio是非阻塞式IO，这里的非阻塞式是指发起IO请求时，如果没有没有数据准备好，会直接返回，而不会阻塞线程。而传统IO即BIO会阻塞线程，直到有数据准备好才执行。具体可以看Java NIO浅析



## 概述

在ddmlib中有几个核心类，如下所示：

AndroidDebugBridge：代表adb的客户端
Device：代表adb连接的手机或者模拟器
Client：代表设备中的app
ClientData：存储app的数据，如堆、线程、hprof等
MonitorThread：监视连接的线程，侧重点在监听发送adb命令后数据的响应上
ChunkHandler：处理adb服务器返回的数据
DeviceMonitor：监听设备连接状态的改变，侧重点在设备状态改变上
AndroidDebugBridge
AndroidDebugBridge的使用示例如下所示：

```
//这里的boolean表示是否debug
AndroidDebugBridge.initIfNeeded(false);
AndroidDebugBridge bridge = AndroidDebugBridge.createBridge("adb的路径", false);
while(true){//循环等待adb连接好设备       
    if (bridge.hasInitialDeviceList()){               
        IDevice[] devices = bridge.getDevices();
        break;      
   }
}
```

initIfNeeded用来设置AndroidDebugBridge的模式，它有两种模式，一种是普通模式，一种是debug模式，false表示选择普通模式。initIfNeeded方法内部调用init方法。代码如下所示，在init方法中创建并启动了MonitorThread，并给该线程注册各种ChunkHandler。这个注册的作用在下面具体介绍。

    public static synchronized void init(boolean clientSupport) {
        
        ...
    
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


createBridge方法用来获取AndroidDebugBridge对象，如下所示该方法内部创建了一个AndroidDebugBridge对象，并最后调用了DeviceMonitor的start方法。

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
    
            ...
            return sThis;
        }
    }
    
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


可以看出AndroidDebugBridge将adb连接的任务交给DeviceMonitor来完成了。实际上，通过AndroidDebugBridge对象调用的hasInitialDeviceList和getDevices方法最后也是通过DeviceMonitor来实现的。

DeviceMonitor

```
/**
 * Starts the monitoring.
 */
void start() {
    mDeviceListMonitorTask = new DeviceListMonitorTask(mServer, new DeviceListUpdateListener());
    new Thread(mDeviceListMonitorTask, "Device List Monitor").start(); //$NON-NLS-1$
}
```

DeviceMonitor的start方法启动了线程用来监听设备列表，并设置了设备更新监听。该线程的实现如下所示：

    @Override
    public void run() {
        do {
            if (mAdbConnection == null) {
                Log.d("DeviceMonitor", "Opening adb connection");
                mAdbConnection = openAdbConnection();//与adb服务器建立连接
                ...
            }
    
            try {
                if (mAdbConnection != null && !mMonitoring) {
                    mMonitoring = sendDeviceListMonitoringRequest();//发送获取设备列表的请求
                }
    
                if (mMonitoring) {
                    int length = readLength(mAdbConnection, mLengthBuffer);
    
                    if (length >= 0) {
                        // 解析获取的数据，并回调通知AndroidDebugBridge
                        processIncomingDeviceData(length);
    
                        // flag the fact that we have build the list at least once.
                        mInitialDeviceListDone = true;
                    }
                  }
                  ...
        } while (!mQuit);//循环，确保获取
    }



### Device

Device类是IDevice的实现类，通过这个类可以对设备进行截屏、安装卸载app、上传下载文件等功能，这里就不多介绍了。拿到Device的实例后，就可以调用getClient方法获取Client对象，下面介绍Client。



### Client

Client代表一个设备上的app进程，通过它我们就可以获取app的UI、内存、hprof等信息。Client本身只存储基本的信息，app的详细信息保存在ClientData中，每一个Client都有一个对应的ClientData。需要注意的是ClientData保存的hprof信息是二进制数组，需要自己进行解析。

这里以获取UI信息为例，介绍Client的使用流程及其内部原理。如下代码所示，在获取到Client对象后，调用HandleViewDebug的dumpViewHierarchy传入Client对象以及对象的一些参数，就可以获取对应的UI数据了。

    IDevice device = devices[0];
    Client[] clients = device.getClients();
    HandleViewDebug.dumpViewHierarchy(clients[0], "viewRoot", false, true, new ViewDumpHandler() {
        @Override
        protected void handleViewDebugResult(ByteBuffer data) {
            //处理响应数据
        }
    });


HandleViewDebug是怎么做到这个功能的？这个就要从最开始的init方法内部调用MonitorThread的start方法开始。下面是简化的MonitorThread的run方法的代码

    @Override
    public void run() {
        // create a selector 1.创建一个selector，在nio中，通过selector来获取有数据到达的Channel
        try {
            mSelector = Selector.open();
        } 
        
        ...
    
        while (!mQuit) {
    
            try {
                /*
                 * sync with new registrations: we wait until addClient is done before going through
                 * and doing mSelector.select() again.
                 * @see {@link #addClient(Client)}
                 */
                 //2.DeviceMonitor创建Client后，会调用MonitorThread的addClient方法将Client的Channel注册到Selector中
                synchronized (mClientList) {
                }
    
               ...
                
                Set<SelectionKey> keys = mSelector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
    
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
    
                    try {
                    //3.adb服务器有数据返回时调用
                        if (key.attachment() instanceof Client) {
                            processClientActivity(key);
                        }
                        ...
                }
            } catch (Exception e) {
              ...
            }
        }
    }


在该方法中，第一步是创建Selector，用来处理注册的Channel；第二步，DeviceMonitor创建Client后，会调用MonitorThread的addClient方法将Client的Channel注册到Selector中。还记得init方法中注册的各种ChunkHandler吗，在addClient方法中，会将这些ChunkHandler添加到Client中。第三步，等adb服务器有数据返回时调用processClientActivity方法。那什么时候会有数据返回？答案是我们调用HandleViewDebug#dumpViewHierarchy方法时，该方法的源码如下：

    public static void dumpViewHierarchy(@NonNull Client client, @NonNull String viewRoot,
            boolean skipChildren, boolean includeProperties, @NonNull ViewDumpHandler handler)
                    throws IOException {
        ByteBuffer buf = allocBuffer(4      // opcode
                + 4                         // view root length
                + viewRoot.length() * 2     // view root
                + 4                         // skip children
                + 4);                       // include view properties
        JdwpPacket packet = new JdwpPacket(buf);
        ByteBuffer chunkBuf = getChunkDataBuf(buf);
    
        chunkBuf.putInt(VURT_DUMP_HIERARCHY);
        chunkBuf.putInt(viewRoot.length());
        ByteBufferUtil.putString(chunkBuf, viewRoot);
        chunkBuf.putInt(skipChildren ? 1 : 0);
        chunkBuf.putInt(includeProperties ? 1 : 0);
    
        finishChunkPacket(packet, CHUNK_VURT, chunkBuf.position());
        //上面的是拼接请求包的代码，发起请求是client调用了send方法
        client.send(packet, handler);
    }


我们知道当DeviceMonitor创建Client后会注册Channel到MonitorThread的Selector中，所以Client的请求会走到MonitorThread的run方法的第三步的processClientActivity方法，源码如下：

    private void processClientActivity(SelectionKey key) {
        Client client = (Client)key.attachment();
    
        try {
            if (!key.isReadable() || !key.isValid()) {
                Log.d("ddms", "Invalid key from " + client + ". Dropping client.");
                dropClient(client, true /* notify */);
                return;
            }
    
            client.read();//读取数据
    
            /*
             * See if we have a full packet in the buffer. It's possible we have
             * more than one packet, so we have to loop.
             */
            JdwpPacket packet = client.getJdwpPacket();
            while (packet != null) {
                client.incoming(packet, client.getDebugger());
    
               ...
    }


在rocessClientActivity方法，先读取数据，在调用Client的incoming方法对数据进行处理，该方法的源码如下：

    public void incoming(@NonNull JdwpPacket packet, @Nullable JdwpAgent target) throws IOException {
        mProtocol.incoming(packet, target);
        int id = packet.getId();
        if (packet.isReply()) {
            JdwpInterceptor interceptor = mReplyInterceptors.remove(id);
            if (interceptor != null) {
                packet = interceptor.intercept(this, packet);
            }
        }
        for (JdwpInterceptor interceptor : mInterceptors) {
            if (packet == null) break;
            packet = interceptor.intercept(this, packet);
        }
    
        if (target != null && packet != null) {
            target.send(packet);
        }
    }


可以看到最后调用了JdwpInterceptor的intercept方法，从上面的分析我们知道在ddClient方法中，会将这些ChunkHandler添加到Client中(ChunkHandler实现了JdwpInterceptor），即最后会调到HandleViewDebug的intercept方法。通过这个流程HandleViewDebug就可以获取到UI的数据信息并对其进行解析，最后通过回调交给我们处理。

总结
本篇文章主要介绍了ddmlib的几个核心类，及其源码的实现；重点讲解了通过ddmlib获取UI信息的过程和源码。