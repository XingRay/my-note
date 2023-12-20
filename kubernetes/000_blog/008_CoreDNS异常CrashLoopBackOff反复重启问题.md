# CoreDNS异常CrashLoopBackOff反复重启问题

本次异常出现情况，coredns状态CrashLoopBackOff，无法正常运行

```shell
[root@master1 ~]# kubectl get po -n kube-system 
NAME                                         READY   STATUS             RESTARTS   AGE
calico-kube-controllers-578894d4cd-l9rkm     1/1     Running            2          55d
calico-node-5rnjk                            1/1     Running            1          63d
coredns-59799fb945-tcjsl                     0/1     CrashLoopBackOff   11         35m
coredns-59799fb945-zlqkt                     0/1     CrashLoopBackOff   11         35m
```

通过日志发现，CoreDNS日志出现Loop…detected…

```shell
[root@master1 ~]# kubectl logs -f -n kube-system coredns-59799fb945-zlqkt 
.:53
[INFO] plugin/reload: Running configuration MD5 = 4e235fcc3696966e76816bcd9034ebc7
CoreDNS-1.6.7
linux/amd64, go1.13.6, da7f65b
[FATAL] plugin/loop: Loop (172.16.25.188:64140 -> :53) detected for zone ".", see https://coredns.io/plugins/loop#troubleshooting. Query: "HINFO 6627366374841107488.3394449046415853644."
```

这意味着loop检测插件已检测到上游 DNS 服务器之一中的无限转发循环。这是一个致命错误，因为无限循环操作将消耗内存和 CPU，直到主机最终因内存不足而死亡。

转发循环通常由以下原因引起：

1 最常见的是，CoreDNS 将请求直接转发给自身。例如，通过环回地址，例如127.0.0.1，::1或127.0.0.53
2 较不常见的是，CoreDNS转发到上游服务器，而上游服务器又将请求转发回CoreDNS。



要解决此问题，请在您的 Corefile 中查找forward检测到循环的区域的任何s。确保它们没有转发到本地地址或另一个将请求转发回 CoreDNS 的 DNS 服务器。如果forward正在使用文件（例如/etc/resolv.conf），请确保该文件不包含本地地址。



### 对 Kubernetes 集群中的循环进行故障排除

当部署在 Kubernetes 中的 CoreDNS Pod 检测到循环时，CoreDNS Pod 将开始“CrashLoopBackOff”。这是因为每当 CoreDNS 检测到循环并退出时，Kubernetes 都会尝试重新启动 Pod。



Kubernetes 集群中转发循环的一个常见原因是与主机节点（例如systemd-resolved）上的本地 DNS 缓存的交互。例如，在某些配置中systemd-resolved会将环回地址127.0.0.53作为名称服务器放入/etc/resolv.conf. kubelet默认情况下，Kubernetes（通过）将/etc/resolv.conf使用defaultdnsPolicy将此文件传递给所有Pod，使它们无法进行 DNS 查找（包括 CoreDNS Pod）。CoreDNS 将此/etc/resolv.conf 用作将请求转发到的上游列表。由于它包含一个环回地址，CoreDNS 最终将请求转发给自己。



解决此问题的方法有很多，此处列出了一些方法：



将以下内容添加到您的kubelet配置 yaml 中：（resolvConf:或通过--resolv-conf1.10 中弃用的命令行标志）。您的“真实” resolv.conf是包含上游服务器的实际 IP 且没有本地/环回地址的地址。此标志告诉kubelet将替代项传递resolv.conf给 Pod。对于使用 的系统systemd-resolved， /run/systemd/resolve/resolv.conf通常是“真实”的位置resolv.conf，尽管这可能会因您的发行版而异。



禁用主机节点上的本地DNS缓存，并恢复/etc/resolv.conf到原来的。



一个快速而肮脏的解决方法是编辑您的 Corefile，替换forward . /etc/resolv.conf为您上游 DNS 的 IP 地址，例如forward . 8.8.8.8. 但这只是解决了 CoreDNS 的问题，kubelet 会继续将无效的转发resolv.conf到所有defaultdnsPolicy Pod，让它们无法解析 DNS。



比较直接解决方法：

```
[root@master1 ~]# kubectl edit -n kube-system cm coredns 
apiVersion: v1
data:
  Corefile: |
    .:53 {
        errors
        health {
           lameduck 5s
        }
        ready
        kubernetes cluster.local in-addr.arpa ip6.arpa {
           pods insecure
           fallthrough in-addr.arpa ip6.arpa
           ttl 30
        }
        prometheus :9153
        forward . /etc/resolv.conf
        cache 30
        loop   #将loop插件直接删除，避免内部循环
        reload
        loadbalance
    }
kind: ConfigMap
metadata:
  creationTimestamp: "2021-03-29T07:09:36Z"
```

修改完CM后,将coredns的pod重新删除后就恢复正常

```shell
kubectl delete -n kube-system po coredns-59799fb945-tcjsl

kubectl delete -n kube-system po coredns-59799fb945-zlqkt
```

```shell
[root@master1 ~]# kubectl get po -n kube-system 
NAME                                         READY   STATUS    RESTARTS   AGE
calico-kube-controllers-578894d4cd-l9rkm     1/1     Running   2          55d
calico-node-5rnjk                            1/1     Running   1          63d
coredns-59799fb945-hsvd8                     1/1     Running   3          2m38s
coredns-59799fb945-p2fkg                     1/1     Running   2          2m29s
```

到此问题解决完成；




https://coredns.io/plugins/loop/#troubleshooting







# loop

 [Source](https://github.com/coredns/coredns/tree/master/plugin/loop)

*loop* detects simple forwarding loops and halts the server.

## [Description](https://coredns.io/plugins/loop/#description)

The *loop* plugin will send a random probe query to ourselves and will then keep track of how many times we see it. If we see it more than twice, we assume CoreDNS has seen a forwarding loop and we halt the process.

The plugin will try to send the query for up to 30 seconds. This is done to give CoreDNS enough time to start up. Once a query has been successfully sent, *loop* disables itself to prevent a query of death.

Note that *loop* will *only* send “looping queries” for the first zone given in the Server Block.

The query sent is `<random number>.<random number>.zone` with type set to HINFO.

## [Syntax](https://coredns.io/plugins/loop/#syntax)

```txt
loop
```

## [Examples](https://coredns.io/plugins/loop/#examples)

Start a server on the default port and load the *loop* and *forward* plugins. The *forward* plugin forwards to it self.

```txt
. {
    loop
    forward . 127.0.0.1
}
```

After CoreDNS has started it stops the process while logging:

```txt
plugin/loop: Loop (127.0.0.1:55953 -> :1053) detected for zone ".", see https://coredns.io/plugins/loop#troubleshooting. Query: "HINFO 4547991504243258144.3688648895315093531."
```

## [Limitations](https://coredns.io/plugins/loop/#limitations)

This plugin only attempts to find simple static forwarding loops at start up time. To detect a loop, the following must be true:

- the loop must be present at start up time.
- the loop must occur for the `HINFO` query type.

## [Troubleshooting](https://coredns.io/plugins/loop/#troubleshooting)

When CoreDNS logs contain the message `Loop ... detected ...`, this means that the `loop` detection plugin has detected an infinite forwarding loop in one of the upstream DNS servers. This is a fatal error because operating with an infinite loop will consume memory and CPU until eventual out of memory death by the host.

A forwarding loop is usually caused by:

- Most commonly, CoreDNS forwarding requests directly to itself. e.g. via a loopback address such as `127.0.0.1`, `::1` or `127.0.0.53`
- Less commonly, CoreDNS forwarding to an upstream server that in turn, forwards requests back to CoreDNS.

To troubleshoot this problem, look in your Corefile for any `forward`s to the zone in which the loop was detected. Make sure that they are not forwarding to a local address or to another DNS server that is forwarding requests back to CoreDNS. If `forward` is using a file (e.g. `/etc/resolv.conf`), make sure that file does not contain local addresses.

### [Troubleshooting Loops In Kubernetes Clusters](https://coredns.io/plugins/loop/#troubleshooting-loops-in-kubernetes-clusters)

When a CoreDNS Pod deployed in Kubernetes detects a loop, the CoreDNS Pod will start to “CrashLoopBackOff”. This is because Kubernetes will try to restart the Pod every time CoreDNS detects the loop and exits.

A common cause of forwarding loops in Kubernetes clusters is an interaction with a local DNS cache on the host node (e.g. `systemd-resolved`). For example, in certain configurations `systemd-resolved` will put the loopback address `127.0.0.53` as a nameserver into `/etc/resolv.conf`. Kubernetes (via `kubelet`) by default will pass this `/etc/resolv.conf` file to all Pods using the `default` dnsPolicy rendering them unable to make DNS lookups (this includes CoreDNS Pods). CoreDNS uses this `/etc/resolv.conf` as a list of upstreams to forward requests to. Since it contains a loopback address, CoreDNS ends up forwarding requests to itself.

There are many ways to work around this issue, some are listed here:

- Add the following to your `kubelet` config yaml: `resolvConf: <path-to-your-real-resolv-conf-file>` (or via command line flag `--resolv-conf` deprecated in 1.10). Your “real” `resolv.conf` is the one that contains the actual IPs of your upstream servers, and no local/loopback address. This flag tells `kubelet` to pass an alternate `resolv.conf` to Pods. For systems using `systemd-resolved`, `/run/systemd/resolve/resolv.conf` is typically the location of the “real” `resolv.conf`, although this can be different depending on your distribution.
- Disable the local DNS cache on host nodes, and restore `/etc/resolv.conf` to the original.
- A quick and dirty fix is to edit your Corefile, replacing `forward . /etc/resolv.conf` with the IP address of your upstream DNS, for example `forward . 8.8.8.8`. But this only fixes the issue for CoreDNS, kubelet will continue to forward the invalid `resolv.conf` to all `default` dnsPolicy Pods, leaving them unable to resolve DNS.

