# Termux 软件仓库

线路选择 自动 IPv4 IPv6 

 是否使用 HTTPS

 是否使用 sudo

## Termux 是什么

> Termux is a terminal emulator and Linux environment bringing powerful terminal access to Android.

Termux 是运行在 Android 上的 terminal。不需要 root，运行于内部存储（不在 SD 卡上）。

自带了一个包管理器，可以安装许多现代化的开发和系统维护工具。比如：

- neovim
- tmux
- zsh
- clang
- gcc
- weechat
- irssi
- ...

## 如何使用 Termux 镜像

### 图形界面（TUI）替换

在较新版的 Termux 中，官方提供了图形界面（TUI）来半自动替换镜像，推荐使用该种方式以规避其他风险。
在 Termux 中执行如下命令



```
termux-change-repo
```

在图形界面引导下，使用自带方向键可上下移动。
第一步使用空格选择需要更换的仓库，之后在第二步选择相应镜像源。确认无误后回车，镜像源会自动完成更换。

### 命令行替换

使用如下命令行替换官方源为镜像源



```
sed -i 's@^\(deb.*stable main\)$@#\1\ndeb https://mirrors.tuna.tsinghua.edu.cn/termux/apt/termux-main stable main@' $PREFIX/etc/apt/sources.list
apt update && apt upgrade
```

社区源（如果已经启用）

x11-repo



```
sed -i 's@^\(deb.*x11 main\)$@#\1\ndeb https://mirrors.tuna.tsinghua.edu.cn/termux/apt/termux-x11 x11 main @' $PREFIX/etc/apt/sources.list.d/x11.list 
apt update && apt upgrade
```

root-repo



```
sed -i 's@^\(deb.*root main\)$@#\1\ndeb https://mirrors.tuna.tsinghua.edu.cn/termux/apt/termux-root root main @' $PREFIX/etc/apt/sources.list.d/root.list 
apt update && apt upgrade
```

### 手动修改

编辑 `$PREFIX/etc/apt/sources.list` 修改为如下内容



```
# The termux repository mirror
deb https://mirrors.tuna.tsinghua.edu.cn/termux/apt/termux-main stable main
```

社区源（如果已经启用）

x11-repo



```
# The termux repository mirror
deb https://mirrors.tuna.tsinghua.edu.cn/termux/apt/termux-x11 x11 main
```

root-repo



```
# The termux repository mirror
deb https://mirrors.tuna.tsinghua.edu.cn/termux/apt/termux-root root stable
```

请使用内置或安装在 Termux 里的文本编辑器，例如 `vi` / `vim` / `nano` 等，**不要使用 RE 管理器等其他具有 ROOT 权限的外部 APP** 来修改 Termux 的文件

注：Termux 会自动将环境变量 `$PREFIX` 设定为 `/data/data/com.termux/files/usr`

### 警告

- 镜像仅适用于 Android 7.0 (API 24) 及以上版本，旧版本系统使用本镜像可能导致程序错误。
- Google Play 上的 Termux 已被弃用，如安装会产生兼容性问题。请通过 GitHub 或 F-Droid（[镜像帮助](https://mirrors.tuna.tsinghua.edu.cn/help/fdroid/)）来安装 Termux。