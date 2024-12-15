# WSL移动或者安装到指定目录

wsl2默认安装路径:

```shell
C:\Users\<your-name>\AppData\Local\Packages\CanonicalGroupLimited.Ubuntu24.04LTS_xxx\LocalState\ext4.vhdx
```



## 1 移动已安装的WSL

关闭WSL

```shell
wsl --shutdown
```



列出安装的WSL分发版

```shell
wsl --list --verbose
```

```shell
  NAME            STATE           VERSION
* Ubuntu-24.04    Running         2
```



查看状态 ( WSL版本 )

```shell
wsl --status
```

```shell
默认分发: Ubuntu-24.04
默认版本: 2
```



导出:

```shell
wsl --export Ubuntu-24.04 D:\tmp\ubuntu_backup.tar
```



然后，注销或卸载原有的 Ubuntu 发行版：

```shell
wsl --unregister Ubuntu-24.04
```



接下来，从备份文件重新安装 Ubuntu 到新的位置

```shell
wsl --import Ubuntu-24.04 D:\vm\wsl2\Ubuntu-24.04 D:\tmp\ubuntu_backup.tar
```





## 2 安装WSL到指定目录



如果你想让新安装的 WSL2 发行版默认安装到其他位置，可以使用环境变量来配置新的安装目录。例如，修改 `WSL2` 默认存储位置：

打开 PowerShell 以管理员身份运行。

设置新的安装目录：

```shell
setx WSL_DISTRO_DEFAULT_LOCATION "D:\vm\wsl2"
```

这将指示 Windows 在 `D:\vm\wsl2` 中安装新下载或创建的 WSL2 分发版。





# Windows启用WSL2并完成默认安装位置变更

 原创

[晨曦I蜗牛](https://blog.51cto.com/ivandu)2024-03-07 09:18:25博主文章分类：[DevOps](https://blog.51cto.com/ivandu/category16)©著作权

***文章标签\*[windows](https://blog.51cto.com/topic/windows.html)[WSL](https://blog.51cto.com/topic/wsl.html)[Linux](https://blog.51cto.com/topic/linux.html)[发行版](https://blog.51cto.com/topic/faxingban.html)[Windows](https://blog.51cto.com/topic/windows.html)*****文章分类\*[JavaScript](https://blog.51cto.com/nav/javascript)[前端开发](https://blog.51cto.com/nav/web)*****阅读数\**\*211\****



### 一 前置条件

#### 1.1 启用“适用于 Linux 的 Windows 子系统”

以管理员权限运行cmd.exe或PowerShell，输入如下内容：

```plain
dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
```

也可在程序和功能中点击“启用或关闭Windows功能”，勾选“虚拟机平台”。

#### 1.2 启用虚拟机功能

启用虚拟机功能，安装 WSL 2 之前，必须启用“虚拟机平台”可选功能。 使用管理员权限运行cmd.exe或PowerShell，输入如下内容：

```plain
dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
```

也可在程序和功能中点击“启用或关闭Windows功能”，勾选“适用于 Linux 的 Windows 子系统”。
下载 Linux 内核更新包：
`wsl.exe --install`或`wsl.exe --update`，[ 适用于 x64 计算机的 WSL2 Linux 内核更新包](https://wslstorestorage.blob.core.windows.net/wslblob/wsl_update_x64.msi)，单击即可下载。

### 二 系统配置

将 WSL 2 设置为默认版本：

```plain
wsl --set-default-version 2
```

列出目前支持的发行版信息：

```plain
wsl --list --online
```

安装所选的 Linux 分发即可体验。如：

```plain
wsl --install -d AlmaLinux-8
```

### 三 导出并完成WSL迁移

列出当前系统已安装的WSL发行版信息：

```plain
wsl -l -v
```

停止WSL及相关发行版：

```plain
wsl --shutdown
```

导出需要迁移的发行版，默认为tar包：

```plain
wsl --export AlmaLinux-8 F:\AlmaLinux-8.tar
```

取消注册分发版并删除根文件系统:

```plain
wsl --unregister AlmaLinux-8
```

从备份恢复、导入发行版：

```plain
wsl --import AlmaLinux-8 D:\Work\WSL\AlmaLinux-8 F:\AlmaLinux-8.tar --version 2
```

### 参考资料

1、[ WSL 的手动安装步骤](https://learn.microsoft.com/zh-cn/windows/wsl/install-manual#step-4---download-the-linux-kernel-update-package)