# AMD +Vmware workstation 16 +Macos11 bigsur 配置摘要

## 回顾：

AMD 3900x 在[win10](https://link.zhihu.com/?target=http%3A//bbs.pcbeta.com/forum-548-1.html)下，一直在用vmware15运行catalina，后来又升级到bigsur，教程参阅网上。功能正常，基本流畅，但因为amd和vm的兼容性，必须采用以下配置要点（也留下小缺憾）：

- 必须使用vmware 15.1以下版本
- 虚拟机配置必须采用vmware 10硬件配置
- 虚机vmx配置文件内添加以下配置行

```text
smc.version = "0"
cpuid.0.eax = "0000:0000:0000:0000:0000:0000:0000:1011"
cpuid.0.ebx = "0111:0101:0110:1110:0110:0101:0100:0111"
cpuid.0.ecx = "0110:1100:0110:0101:0111:0100:0110:1110"
cpuid.0.edx = "0100:1001:0110:0101:0110:1110:0110:1001"
cpuid.1.eax = "0000:0000:0000:0001:0000:0110:0111:0001"
cpuid.1.ebx = "0000:0010:0000:0001:0000:1000:0000:0000"
cpuid.1.ecx = "1000:0010:1001:1000:0010:0010:0000:0011"
cpuid.1.edx = "0000:1111:1010:1011:1111:1011:1111:1111"
featureCompat.enable = "FALSE"
```

## 目标：

VMware workstation pro 16 发布，有黑暗界面吸引，更可和hyper-v 、wsl共存，搭配运行macos 11 beta6/7 诱惑无限。

## 实践 （AMD+Vmware16+macos11）

readme记录摘要分享如下： INTEL（估计）用前三项，AMD需要加第四步

1. 下载autounlocker1.11，下载地址：[https://github.com/paolo-projects/auto-unlocker/releases](https://link.zhihu.com/?target=https%3A//github.com/paolo-projects/auto-unlocker/releases)
2. 运行autounlocker前，注意首先逐一手动关闭vm相关的5个win10服务项，然后用管理员权限运行
3. 不必再使用vm10虚拟机硬件配置，直接使用最新vm 16硬件配置+macos11.1操作系统选项，来新建虚拟机或修改兼容性
4. 【AMDer注意】虚机vmx配置文件内添加/更改配置项如下：（仔细找不同：）

```text
smc.version = "0"
cpuid.0.eax = "0000:0000:0000:0000:0000:0000:0000:1011"
cpuid.0.ebx = "0111:0101:0110:1110:0110:0101:0100:0111"
cpuid.0.ecx = "0110:1100:0110:0101:0111:0100:0110:1110"
cpuid.0.edx = "0100:1001:0110:0101:0110:1110:0110:1001"
cpuid.1.eax = "0000:0000:0000:0001:0000:0110:0111:0001"
cpuid.1.ebx = "0000:0010:0000:0001:0000:1000:0000:0000"
cpuid.1.ecx = "1000:0010:1001:1000:0010:0010:0000:0011"
cpuid.1.edx = "0000:0111:1000:1011:1111:1011:1111:1111"
```
