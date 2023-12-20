## Ubuntu系统关机缓慢问题解决



## 修改默认的等待时间

```bash
sudo vi /etc/systemd/system.conf
```

可以看到默认谢了很多内容并且被注释掉了，找到其中的几行：

```bash
DefaultTimeoutStartSec=90s
DefaultTimeoutStopSec=90s
DefaultTRestartSec=100ms
```

解除注释并把其中的`DefaultTimeoutStopSec=90s`时间改短，例如

```bash
 DefaultTimeoutStopSec=1s
```

**注意**：

千万不要改上面的那个`StartSec`，看到有博文中说将其改成1s，这将导致你的系统无法启动：1s时间不足以支持你的系统启动。如果你不慎将其改变，可以在grub引导中选择recovery模式的root终端，用vim将其改回来，回头是岸 : )

## 配置config生效

```bash
sudo systemctl daemon-reload
```