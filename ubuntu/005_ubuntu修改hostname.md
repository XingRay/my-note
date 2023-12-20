# 如何在Ubuntu上修改hostname

更新：2023-05-17 06:20

### 一、概述

Hostname是计算机在网络中的标识符，它可以用来区分同一网络内的不同设备。在Ubuntu中，我们可以通过修改/etc/hostname文件来更改计算机的hostname，也可以通过hostname命令来达到同样的效果。

### 二、通过修改/etc/hostname文件来修改hostname

1、登录Ubuntu系统后，打开终端，输入以下命令来编辑/etc/hostname文件：

```
$ sudo nano /etc/hostname
```

2、在打开的文本编辑器中，将当前主机名替换为新主机名，并保存文件。

3、打开/etc/hosts文件，将其中的旧主机名替换为新主机名，并保存文件。

```
$ sudo nano /etc/hosts
```

4、最后，重启计算机，新主机名就会生效。

### 三、通过hostname命令来修改hostname

1、登录Ubuntu系统后，打开终端，输入以下命令来修改当前主机名：

```
$ sudo hostnamectl set-hostname new-hostname
```

2、在/etc/hosts文件中，将旧主机名替换为新主机名，并保存文件。

```
$ sudo nano /etc/hosts
```

3、最后，重启计算机，新主机名就会生效。

### 四、注意事项

1、如果要在局域网内使用新的主机名，需要将该主机名添加到DNS服务器或DHCP服务器中。

2、修改主机名可能会影响系统的某些配置和服务，如samba服务、VPN等，需要做出相应的配置调整。

3、在修改完主机名后，可能需要重新运行一些应用程序或系统服务，以保证它们使用新的主机名。

### 五、总结

本文介绍了在Ubuntu系统中如何通过修改/etc/hostname文件和使用hostname命令来修改主机名的方法。同时，还提供了修改主机名时需要注意的一些事项，以便用户能够更好地掌握此技能。