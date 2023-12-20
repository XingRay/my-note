## Ubuntu20.04使用apt下载和卸载openJDK



1、使用apt下载openJDK
我们先使用apt来下载openJDK

更新软件源

```
sudo apt update
```

查找openjdk，找到自己想要的版本

```
sudo apt search openjdk
```

```
Sorting... Done
Full Text Search... Done
crypto-policies/jammy 20190816git-1 all
  unify the crypto policies used by different applications and libraries

default-jdk/jammy 2:1.11-72build2 amd64
  Standard Java or Java compatible Development Kit

default-jdk-doc/jammy 2:1.11-72build2 amd64
  Standard Java or Java compatible Development Kit (documentation)

default-jdk-headless/jammy 2:1.11-72build2 amd64
  Standard Java or Java compatible Development Kit (headless)

default-jre/jammy 2:1.11-72build2 amd64
  Standard Java or Java compatible Runtime

default-jre-headless/jammy 2:1.11-72build2 amd64
  Standard Java or Java compatible Runtime (headless)

java-package/jammy 0.62 all
  Utility for creating Java Debian packages

jtreg/jammy 5.1-b01-2 all
  Regression Test Harness for the OpenJDK platform

jtreg6/jammy-updates,jammy-security 6.1+2-1ubuntu1~22.04 all
  Regression Test Harness for the OpenJDK platform

libasmtools-java/jammy-updates,jammy-security 7.0-b09-2ubuntu1~22.04 all
  OpenJDK AsmTools

libeclipse-collections-java/jammy 11.0.0.M3-2 all
  Eclipse Collections - comprehensive collections library for Java

libhsdis0-fcml/jammy 1.2.2-2 amd64
  HotSpot disassembler plugin using FCML

libjax-maven-plugin/jammy 0.1.8+dfsg-1 all
  Using the xjc goal with OpenJDK 11+

libreoffice/jammy-updates 1:7.3.7-0ubuntu0.22.04.2 amd64
  office productivity suite (metapackage)

openjdk-11-dbg/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 amd64
  Java runtime based on OpenJDK (debugging symbols)

openjdk-11-demo/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 amd64
  Java runtime based on OpenJDK (demos and examples)

openjdk-11-doc/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 all
  OpenJDK Development Kit (JDK) documentation

openjdk-11-jdk/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 amd64
  OpenJDK Development Kit (JDK)

openjdk-11-jdk-headless/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 amd64
  OpenJDK Development Kit (JDK) (headless)

openjdk-11-jre/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 amd64
  OpenJDK Java runtime, using Hotspot JIT

openjdk-11-jre-dcevm/jammy 11.0.12+7-1 amd64
  Alternative VM for OpenJDK 11 with enhanced class redefinition

openjdk-11-jre-headless/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 amd64
  OpenJDK Java runtime, using Hotspot JIT (headless)

openjdk-11-jre-zero/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 amd64
  Alternative JVM for OpenJDK, using Zero

openjdk-11-source/jammy-updates,jammy-security 11.0.19+7~us1-0ubuntu1~22.04.1 all
  OpenJDK Development Kit (JDK) source files

openjdk-17-dbg/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 amd64
  Java runtime based on OpenJDK (debugging symbols)

openjdk-17-demo/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 amd64
  Java runtime based on OpenJDK (demos and examples)

openjdk-17-doc/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 all
  OpenJDK Development Kit (JDK) documentation

openjdk-17-jdk/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 amd64
  OpenJDK Development Kit (JDK)

openjdk-17-jdk-headless/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 amd64
  OpenJDK Development Kit (JDK) (headless)

openjdk-17-jre/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 amd64
  OpenJDK Java runtime, using Hotspot JIT

openjdk-17-jre-headless/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 amd64
  OpenJDK Java runtime, using Hotspot JIT (headless)

openjdk-17-jre-zero/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 amd64
  Alternative JVM for OpenJDK, using Zero

openjdk-17-source/jammy-updates,jammy-security 17.0.7+7~us1-0ubuntu1~22.04.2 all
  OpenJDK Development Kit (JDK) source files

openjdk-18-dbg/jammy-updates,jammy-security 18.0.2+9-2~22.04 amd64
  Java runtime based on OpenJDK (debugging symbols)

openjdk-18-demo/jammy-updates,jammy-security 18.0.2+9-2~22.04 amd64
  Java runtime based on OpenJDK (demos and examples)

openjdk-18-doc/jammy-updates,jammy-security 18.0.2+9-2~22.04 all
  OpenJDK Development Kit (JDK) documentation

openjdk-18-jdk/jammy-updates,jammy-security 18.0.2+9-2~22.04 amd64
  OpenJDK Development Kit (JDK)

openjdk-18-jdk-headless/jammy-updates,jammy-security 18.0.2+9-2~22.04 amd64
  OpenJDK Development Kit (JDK) (headless)

openjdk-18-jre/jammy-updates,jammy-security 18.0.2+9-2~22.04 amd64
  OpenJDK Java runtime, using Hotspot JIT

openjdk-18-jre-headless/jammy-updates,jammy-security 18.0.2+9-2~22.04 amd64
  OpenJDK Java runtime, using Hotspot JIT (headless)

openjdk-18-jre-zero/jammy-updates,jammy-security 18.0.2+9-2~22.04 amd64
  Alternative JVM for OpenJDK, using Zero

openjdk-18-source/jammy-updates,jammy-security 18.0.2+9-2~22.04 all
  OpenJDK Development Kit (JDK) source files

openjdk-19-dbg/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 amd64
  Java runtime based on OpenJDK (debugging symbols)

openjdk-19-demo/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 amd64
  Java runtime based on OpenJDK (demos and examples)

openjdk-19-doc/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 all
  OpenJDK Development Kit (JDK) documentation

openjdk-19-jdk/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 amd64
  OpenJDK Development Kit (JDK)

openjdk-19-jdk-headless/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 amd64
  OpenJDK Development Kit (JDK) (headless)

openjdk-19-jre/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 amd64
  OpenJDK Java runtime, using Hotspot JIT

openjdk-19-jre-headless/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 amd64
  OpenJDK Java runtime, using Hotspot JIT (headless)

openjdk-19-jre-zero/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 amd64
  Alternative JVM for OpenJDK, using Zero

openjdk-19-source/jammy-updates,jammy-security 19.0.2+7-0ubuntu3~22.04 all
  OpenJDK Development Kit (JDK) source files

openjdk-8-dbg/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 amd64
  Java runtime based on OpenJDK (debugging symbols)

openjdk-8-demo/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 amd64
  Java runtime based on OpenJDK (demos and examples)

openjdk-8-doc/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 all
  OpenJDK Development Kit (JDK) documentation

openjdk-8-jdk/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 amd64
  OpenJDK Development Kit (JDK)

openjdk-8-jdk-headless/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 amd64
  OpenJDK Development Kit (JDK) (headless)

openjdk-8-jre/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 amd64
  OpenJDK Java runtime, using Hotspot JIT

openjdk-8-jre-headless/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 amd64
  OpenJDK Java runtime, using Hotspot JIT (headless)

openjdk-8-jre-zero/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 amd64
  Alternative JVM for OpenJDK, using Zero/Shark

openjdk-8-source/jammy-updates,jammy-security 8u372-ga~us1-0ubuntu1~22.04 all
  OpenJDK Development Kit (JDK) source files

uwsgi-app-integration-plugins/jammy 2.0.20-4 amd64
  plugins for integration of uWSGI and application

uwsgi-plugin-jvm-openjdk-11/jammy 2.0.20-4 amd64
  Java plugin for uWSGI (OpenJDK 11)

uwsgi-plugin-jwsgi-openjdk-11/jammy 2.0.20-4 amd64
  JWSGI plugin for uWSGI (OpenJDK 11)

uwsgi-plugin-ring-openjdk-11/jammy 2.0.20-4 amd64
  Closure/Ring plugin for uWSGI (OpenJDK 11)

uwsgi-plugin-servlet-openjdk-11/jammy 2.0.20-4 amd64
  JWSGI plugin for uWSGI (OpenJDK 11)
```

安装openjdk

```
sudo apt install openjdk-8-jdk
```

2、检查java是否安装成功
使用java -version查看java是否安装成功

```
java -version
```


有版本的显示就是安装成功了

3、更换java版本

我们可以通过如下命令来更换java版本：

```
sudo update-alternatives --config java
```

如果我们只用apt安装过一个版本的java，那么会显示如下结果

```
There is only one alternative in link group java (providing /usr/bin/java): /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
Nothing to configure.
```

因为我这个时候只下载了一个openjdk11，所以显示无需配置

那么为了演示如何更换版本，我再用apt安装一个openjdk16



在我们安装openjdk16之后，输入java -version



这个时候如何更换为java11呢？



sudo update-alternatives --config java



这个时候我们手动选择openjdk11，版本就会变更为openjdk11啦

4、java卸载
如果你想卸载某个apt安装的java版本，那么使用以下指令

sudo apt-get --purge remove openjdk-16-jdk

sudo apt-get --purge remove openjdk-16-jdk-headless

sudo apt-get --purge remove openjdk-16-jre

sudo apt-get --purge remove openjdk-16-jre-headless

我这里卸载的是openjdk16，其他版本的更改一下名字就行了

我们还可以通过以下指令来看我们安装了多少openJDK

apt list --installed | grep openjdk

那么我卸载完openjdk-16-jdk之后，就剩下openjdk-11jdk了



那么这个时候我们再使用更改java版本的指令就会



到这里openjdk16就卸载完成了！
