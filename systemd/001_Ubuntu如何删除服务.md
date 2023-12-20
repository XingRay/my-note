### Ubuntu如何删除服务

最近在Ubuntu 16.04上面有些服务已经不再使用了，留着不仅占资源，同时也不便于管理。

在Ubuntu上删除服务的具体步骤如下：

```
systemctl stop [servicename]
systemctl disable [servicename]
rm /etc/systemd/system/[servicename]
rm /etc/systemd/system/[servicename] # and symlinks that might be related
rm /usr/lib/systemd/system/[servicename] 
rm /usr/lib/systemd/system/[servicename] # and symlinks that might be related
systemctl daemon-reload
systemctl reset-failed
```

这两个命令在Ubuntu18.04中没有找到相关位置：

```
rm /usr/lib/systemd/system/[servicename] 
rm /usr/lib/systemd/system/[servicename] # and symlinks that might be related
```

可以不用理会。



**参考资料：**

1、[How to remove systemd services](https://superuser.com/questions/513159/how-to-remove-systemd-services)



