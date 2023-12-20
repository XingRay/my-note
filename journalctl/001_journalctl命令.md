# 深入探究journalctl命令：多角度解析查看和管理 Linux 日志

更新：2023-05-19 11:57

## 一、journalctl基本介绍

`journalctl `是Linux系统的一个系统日志查看工具。使用 `journalctl` 命令可以查看系统日志并进行过滤和排序。它能够读取系统日志存储在文件系统中的二进制格式，并支持诸如进程ID、单元或其他标准日志记录字段的过滤器。同时，它还提供了丰富的格式选项，以便轻松查看和管理日志。

默认情况下，`journalctl` 会显示最近一个工作日的记

## 二、journalctl常用参数

### 1. -u, --unit

`-u`（或者 `--unit= `）参数可以过滤日志记录的单元（systemd Unit）。如果要查找特定单元的日志记录，可以使用以下命令：

```
journalctl -u nginx.service 
```

上述命令显示 `nginx.service` 的所有日志记录。如果要按逆序排序，则使用以下命令：

```
journalctl -u nginx.service -r
```

### 2. -f, --follow

`-f` 选项会实时地跟踪日志，而不是只显示最新的日志文件，这会非常有用。例如，如果要监视系统日志，以便及时掌握有关任何系统异常的信息，则可以使用以下命令：

```
journalctl -f 
```

上述命令将按实时更新跟踪日志。

### 3. -n, --lines

`-n` 选项会显示指定的最近日志条数。例如，如果要查看前20个日志条目，请使用以下命令：

```
journalctl -n 20 
```

### 4. -k, --dmesg

`-k` 选项会显示内核消息。例如，以下命令将显示与内核消息相关的所有日志记录：

```
journalctl -k
```

如果只想查看最新的10个内核消息，请使用以下命令：

```
journalctl -k -n 10 
```

## 三、journalctl高级用法

### 1. 自定义时间戳格式

`journalctl` 默认使用 RFC 3339 格式的时间戳，例如：2021-08-28T06:30:57.953662+08:00。如果想要使用不同的时间戳格式，则需要使用 `--output= `和 `--since= `选项来自定义输出。例如，以下命令将跟踪从三小时前直到现在的各个记录，并使用自定义的时间戳格式（YYYY-MM-DD hh:mm:ss）输出：

```
 journalctl --since "3 hours ago" --until "now" --output=short-precise 
```

### 2. 查找关键字

`journalctl` 可以通过 grep 和正则表达式查找具有特定单词和短语的日志条目，使用 `--grep= `或 `--regex= `选项。例如，以下命令将显示包含单词“error”的所有日志记录：

```
 journalctl --grep="error"
```

### 3. 按字段查询特定记录

`journalctl `可以根据记录的不同字段来查找特定记录。使用`-f 或 --field=` 选项，其后接着查询的值来完成查询。例如，以下命令将搜索日志记录，查找其中的日志来源（以 MESSAGE_ID_FIELD=SYSLOG_IDENTIFIER 为前缀的日志）：

```
 journalctl -f _SYSTEMD_UNIT="sshd.service"
```

## 四、journalctl配置文件

### 1. 查看默认配置文件位置

要查找 journalctl 的默认配置文件，需要先使用以下命令启动查找：

```
journalctl  --show-cursor 
```

然后查找下面一行代码：

```
...
Cursor: cursor: s=ba22a36bfcce4227a9ade40cb0d2b018;i=346;b=cbf1824ec8f7498d93b88e70925ee735;m=280bfc1e4;t=5c0ff82b4b0df;x=e4ae2415b3d1fed1
...
```

其中包括一个“cursor”，这是指向最新系统日志条目的指针，包括是哪个配置文件用来初始化这个系统：

```
...
m=280bfc1e4;
...
```

可以看到，这个配置文件的路径是：

```
/var/log/journal/280bfc1e4b0df
/system.journal
```

### 2. 修改默认配置文件

要修改默认配置文件的大小或最大存储容量等选项，需要修改 /etc/systemd/journald.conf 文件。打开文件并照需修改。例如，如果要将 Journal 存储的最大容量增加到100M，可以在文件中搜索 SystemMaxUse ，并将它修改为100M：

```
# SystemMaxUse=50M
SystemMaxUse=100M
```

然后保存文件并重新加载 `systemd-journald` 服务：

```
systemctl reload systemd-journald.service 
```

## 总结

以上是对于 journalctl 命令的一些详细介绍和实战演示。通过学习这些用法，可以更加高效地查看和管理系统日志。在具体的使用中，按需选择已提到的参数和配置文件选项，会让你在实际中更加得心应手。