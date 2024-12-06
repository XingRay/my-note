# WSL移动或者安装到指定目录



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