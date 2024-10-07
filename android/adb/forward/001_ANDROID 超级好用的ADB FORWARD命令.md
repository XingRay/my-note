# ANDROID: 超级好用的ADB FORWARD命令

之前，我们使用adb forward命令的时候，只是单纯地将设备中的某些TCP端口给forward出来，如我们最常用的gdb调试native的代码，会将设备的5039端口给forward出来，给gdb客户端访问。那么adb forward有什么更加强大的功能呢？

我们先看一下adb命令中关于forward的一些帮助信息（看了一下，还有reverse命令，也一同列了出来）：

```
  adb forward --list           - list all forward socket connections.
                                 the format is a list of lines with the following format:
                                    <serial> " " <local> " " <remote> "\n"
  adb forward <local> <remote> - forward socket connections
                                 forward specs are one of: 
                                   tcp:<port>
                                   localabstract:<unix domain socket name>
                                   localreserved:<unix domain socket name>
                                   localfilesystem:<unix domain socket name>
                                   dev:<character device name>
                                   jdwp:<process pid> (remote only)
  adb forward --no-rebind <local> <remote>
                               - same as 'adb forward <local> <remote>' but fails
                                 if <local> is already forwarded
  adb forward --remove <local> - remove a specific forward socket connection
  adb forward --remove-all     - remove all forward socket connections
  adb reverse --list           - list all reverse socket connections from device
  adb reverse <remote> <local> - reverse socket connections
                                 reverse specs are one of:
                                   tcp:<port>
                                   localabstract:<unix domain socket name>
                                   localreserved:<unix domain socket name>
                                   localfilesystem:<unix domain socket name>
  adb reverse --no-rebind <remote> <local>
                               - same as 'adb reverse <remote> <local>' but fails
                                 if <remote> is already reversed.
  adb reverse --remove <remote>
                               - remove a specific reversed socket connection
  adb reverse --remove-all     - remove all reversed socket connections from device
```

我之前也写道可以将Android系统中的设备forward出来，如将input设备forward出来：

```
adb forward tcp:8424 dev:/dev/input/event0
```

但把这个设备forward出来用处也不大，还要专门写一个程序来读写这个端口。

最近一直在做移植Brillo到RPi 2B上面，今天PRi 2B已经有双系统了：可以在断电开机的时候先运行recovery系统，以便我们将Brillo系统修改出问题不能再次开机的时候，可以方便地进行修复。这里的recovery系统，这个recovery系统其实也是一个Brillo系统，并不像Android系统的recovery系统，有sideload, 可以通过adb烧机。PRi 2B上面也没有什么好用的bootloader, 或者是带有fastboot协议，可以很方便地更新boot, system分区。这也是做recovery系统的初衷。现在系统是做好了，可以也想不出里面有什么好用的工具，可以更新system image。

即使不做Brillo系统system image的更新，在recovery系统下，对Brillo系统分区中的文件进行更新也是个很麻烦的事情，因为Brillo系统有了SELinux这个安全模块，现在的copy(adb push/sync)文件并不是简单地将文件copy进去就了事了。adb push/sync文件的时候，会对push进来的文件重新设置mode和SELinux label, 具体请看代码(system/core/adb/file_sync_service.cpp @brillo-m8-release)：

```
static bool handle_send_file(int s, const char* path, uid_t uid,
                             gid_t gid, mode_t mode, std::vector<char>& buffer, bool do_unlink) {
    syncmsg msg;
    unsigned int timestamp = 0;

    int fd = adb_open_mode(path, O_WRONLY | O_CREAT | O_EXCL | O_CLOEXEC, mode);
    if (fd < 0 && errno == ENOENT) {
        if (!secure_mkdirs(adb_dirname(path))) {
            SendSyncFailErrno(s, "secure_mkdirs failed");
            goto fail;
        }
        fd = adb_open_mode(path, O_WRONLY | O_CREAT | O_EXCL | O_CLOEXEC, mode);
    }
    if (fd < 0 && errno == EEXIST) {
        fd = adb_open_mode(path, O_WRONLY | O_CLOEXEC, mode);
    }
    if (fd < 0) {
        SendSyncFailErrno(s, "couldn't create file");
        goto fail;
    } else {
        if (fchown(fd, uid, gid) == -1) {
            SendSyncFailErrno(s, "fchown failed");
            goto fail;
        }

        // Not all filesystems support setting SELinux labels. http://b/23530370.
        selinux_android_restorecon(path, 0);

        // fchown clears the setuid bit - restore it if present.
        // Ignore the result of calling fchmod. It's not supported
        // by all filesystems. b/12441485
        fchmod(fd, mode);
    }
    // ...
}
```

想要更新system image需要将system image先push到设备中，再通过dd命令写入，也是件非常麻烦的事情。在不停地思考，如果系统中有个nc(netcat)命令就好了，于是运行adb shell nc；不行，没有这个命令。再想想，再想想。对了，可以通过adb forward命令将分区给forward出来，这是个方法。那用什么命令可以读写这个TCP端口呢，dd行不行。如果dd可以的话，我们就可以去试试看一下dd能不能将system image直接写入到这个分区。看了一下dd的手册，没有这样的写法，只好再次放弃了。脑子灵光一闪，又想到了nc命令。。。

- adb forward与nc配合使用真是太无敌了（太激动了！！！）

首先，我们将system image所在的分区(/dev/block/by-name/system)通过adb forward命令forward出来：

```
$ adb forward tcp:8424 dev:/dev/block/by-name/system
```

除了使用TCP端口进行forward之外，我们还可以使用unix domain socket进行forward：

```
$ adb forward localfilesystem:socket dev:/dev/block/mmcblk0p6
```

再通过adb forward –list命令，我们就可以看到分区已经被forward出来了：

```
$ adb forward --list
10.0.0.18:5555 tcp:8424 dev:/dev/block/by-name/system
```

我们先试试将分区中的内容给读取出来看看：

```
$ nc 127.0.0.1 8424 > system.bin
```

查看文件格式与大小都没有什么问题，使用fsck.ext4检查也没有问题：

```
$ file system.bin
system.bin: Linux rev 1.0 ext4 filesystem data, UUID=da594c53-9beb-f85c-85c5-cedf76546f7a, volume name "system" (extents) (large files)
$ ls -l system.bin 
-rw-rw-r-- 1 hzak hzak 268435456 Jan  3 04:56 system.bin
$ fsck.ext4 -n -f -v system.bin 
e2fsck 1.42.9 (4-Feb-2014)
Pass 1: Checking inodes, blocks, and sizes
Pass 2: Checking directory structure
Pass 3: Checking directory connectivity
Pass 4: Checking reference counts
Pass 5: Checking group summary information

         713 inodes used (4.35%, out of 16384)
           0 non-contiguous files (0.0%)
           0 non-contiguous directories (0.0%)
             # of inodes with ind/dind/tind blocks: 0/0/0
             Extent depth histogram: 555
       19539 blocks used (29.81%, out of 65536)
           0 bad blocks
           0 large files

         532 regular files
          22 directories
           0 character device files
           0 block device files
           0 fifos
           0 links
         150 symbolic links (150 fast symbolic links)
           0 sockets
------------
         704 files
```

将system.img文件写进去看看：

```
$ cat system.img | nc 127.0.0.1 8424 > /dev/null
# or
$ nc 127.0.0.1 8424 < system.img > /dev/null
```

对于Unix domain socket:

```
$ cat system.img | nc -U socket
```

NOTE:

1. 在写文件的同时，nc也会从TCP端口中读取数据
2. 待写入的设备不能挂载，不然写入会失败
3. 写完之后再读取出来数据不一致，why?

~~`在这里，我突然联想到某手机平台不带fastboot协议，每次烧机都得用专门的工具，真是太不方便了。现在想想，有了这个方法，fastboot都得找地方哭去。`~~

[**2016-01-04 21:56:03**]

今天分析了相关的代码，在设备端(adbd进程中）读写dev文件使用的是fd是同一个，读写的时候fd中的pos会发生变化。而在进行nc 127.0.0.1 8424 < system.img的时候，会首先去读取文件，所以fd中的pos发生了变化，写入的位置也就发生了改变。要解决这个问题，就要避免读写使用同一个fd进行操作。同时我们可以知道，单独地去读取文件是不会有问题的。

这也可以理解adb forward中的dev只支持character device，而不支持block device。

NOTE：

关于adbd的调试，可以参考文件: system/core/adb/adb_trace.cpp @brillo-m8-release, 设置系统属性persist.adb.trace_mask为all， 再重启adbd之后，可以在/data/adb/文件夹下看到log文件。

 

同时，由于Brillo系统中curl命令，可以通过在host(PC)中通过python建立一个简单的http server, 再Brillo系统中使用curl命令可以实现系统分区的更新：

如在host (PC, IP地址10.0.0.10)中建立简单的http server:

```
cd out/target/product/rpi && python -m SimpleHTTPServer
```

在Brillo系统命令行中执行如下命令 (需要以root用户执行）：

```
# curl http://10.0.0.10:8000/system.img -o /dev/block/mmcblk0p6
```

可以同时看到传输速度与剩余时间也是不错的选择：

```
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100  256M  100  256M    0     0  10.3M      0  0:00:24  0:00:24 --:--:-- 10.5M
```

### 相关文档：

1. [Brillo: 使用iw命令设置无线网卡工作模式](https://www.brobwind.com/archives/266)
2. [Brillo: 将系统移植到Raspberry Pi 2B(树莓派)上](https://www.brobwind.com/archives/96)
3. [Brillo: 让RPi 2B(树莓派)摆脱网线的束缚 － 通过wifi进行连接](https://www.brobwind.com/archives/179)
4. [RPI：运行在raspberrypi 3b上的google iot 1.0.5系统](https://www.brobwind.com/archives/1525)
5. [RPI：在树莓派（RPI 3B）上运行Android 9.0系统](https://www.brobwind.com/archives/1534)