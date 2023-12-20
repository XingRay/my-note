https://github.com/winsw/winsw/releases



你使用 sc 命令也好，使用图形化界面在 `服务` 中点点点也好，能操控的仅仅是 Windows 系统的「服务」。

如果，一个程序它只是普通的程序，而不是 Windows 系统的服务，那么很自然，你就无法使用 sc 命令和 `服务` 界面来开关它们。你只能手动开关它们，甚至要保证它们的运行界面 / 命令行控制台窗口保持、不能推出。很显然，这不太方便，特别是对于服务端程序更是如此。

Windows Service Wrapper 工具可以将任意的可执行程序<small>（ .exe ）</small>转 windows 服务。

**tip 提示**
其实，是否使用 Windows Service Wrapper 工具是一个「好不好」的问题，而非「对不对」的问题，你如果不使用它也是可以的。

## 安装和使用

### 第 1 步：安装 .NET Framework

因为 Windows Service Wrapper 依赖于 .NET Framework ，因此确保你的电脑已经安装过 .NET Framework 。<small>这里，我们预期你的 Windows 上安装了 .NET Framework 4.6.1+ 。</small>

由于好多常用、常见软件都依赖于它，因此你的 Windows 可能已经安装了 .NET Framework 4.6.1+。如果没有安装，请到 [官方网址](https://links.jianshu.com/go?to=https%3A%2F%2Fdotnet.microsoft.com%2Fzh-cn%2Fdownload%2Fdotnet-framework) 下载。

[直接点击下载 .NET Framework 4.6.1 离线版](https://links.jianshu.com/go?to=https%3A%2F%2Fdownload.microsoft.com%2Fdownload%2FE%2F4%2F1%2FE4173890-A24A-4936-9FC9-AF930FE3FA40%2FNDP461-KB3102436-x86-x64-AllOS-ENU.exe)

- 如果你看到安装进度条，那么就意味着你之前没有安装过，或者是安装的 .NET Framework 版本低于 4.6.1；
- 如果你看见「这台计算机中已经安装了 .NET Framework 4.6.1 或版本更高的更新」那就意味着你之前转过，且不低于 4.6.1 版本。

### 第 2 步：下载 Windows Service Wrapper

下载 Windows Service Wrapper 工具：[官方 github for .NET 4.6.1 版](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fwinsw%2Fwinsw%2Freleases%2Fdownload%2Fv2.11.0%2FWinSW.NET461.exe)

### 第 3 步：配置

1. Take WinSW.exe or WinSW.zip from the distribution, and rename the .exe to your taste (such as myapp.exe).
2. Write myapp.xml (see the XML config file specification and samples for more details).
3. Place those two files side by side, because that's how WinSW discovers its co-related configuration.

### 第 4 步：使用

1. Run `myapp.exe install` to install the service.
2. Run `myapp.exe start` to start the service.

## Sample configuration file



```xml
<service>
  <id>jenkins</id>
  <name>Jenkins</name>
  <description>This service runs Jenkins continuous integration system.</description>
  <env name="JENKINS_HOME" value="%BASE%"/>
  <executable>java</executable>
  <arguments>-Xrs -Xmx256m -jar "%BASE%\jenkins.war" --httpPort=8080</arguments>
  <log mode="roll"></log>
</service>
```

## Usage

| Command   | Description                                              |
| :-------- | :------------------------------------------------------- |
| install   | Installs the service.                                    |
| uninstall | Uninstalls the service.                                  |
| start     | Starts the service.                                      |
| stop      | Stops the service.                                       |
| restart   | Stops and then starts the service.                       |
| status    | Checks the status of the service.                        |
| refresh   | Refreshes the service properties without reinstallation. |
| customize | Customizes the wrapper executable.                       |
| dev       | Experimental commands.                                   |

Experimental commands:

| Command  | Description                                          |
| :------- | :--------------------------------------------------- |
| dev ps   | Draws the process tree associated with the service.  |
| dev kill | Terminates the service if it has stopped responding. |
| dev list | Lists services managed by the current executable.    |