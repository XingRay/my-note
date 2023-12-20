# Ubuntu提示Temporary failure in name resolution



解决ubuntu开机resolve.conf被覆盖为127.0.0.53无法导致上网的问题



方法一：修改DNS
`/etc/resolve.conf` 编辑很快就会被覆盖，因为其第一行写着：

```
This file is managed by man:systemd-resolved(8). Do not edit.
```

故修改 `/etc/systemd/resolved.conf`

```toml
[Resolve]
DNS=8.8.8.8 114.114.114.114
```

当然，错误的时钟也会给网络带来问题：

ntp同步时钟

```shell
sudo ntpdate edu.ntp.org.cn
```

