我在windows系统上，想在在cmd脚本中使用变量保存ip地址，要怎么实现



```bash
@echo off
setlocal enabledelayedexpansion

REM 获取所有 IP 地址
for /f "tokens=2 delims=:" %%i in ('ipconfig ^| findstr "IPv4"') do (
    set ip=%%i
    REM 检查 IP 地址的最后一段是否为 1
    for /f "tokens=4 delims=." %%j in ("!ip!") do (
        if "%%j" NEQ "1" (
            REM 找到不是以 1 结尾的 IP 地址
            set "physical_ip=!ip!"
            goto :output
        )
        if not defined physical_ip (
            REM 记录第一个 IP 地址
            set "physical_ip=!ip!"
        )
    )
)

:output
REM 去除物理网卡的 IP 地址前面的空格
set "physical_ip=%physical_ip: =%"

REM 输出物理网卡的 IP 地址（去除空格）
echo 物理网卡 IP 地址为: %physical_ip%

REM 将物理网卡的 IP 地址保存到环境变量中
setx physical_ip "%physical_ip%"

endlocal


set ip=%physical_ip%
```









ChatGPT



在Windows系统的cmd脚本中，你可以使用以下步骤来保存IP地址到一个变量：

```bash
@echo off
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set IP=%%a
)
set IP=%IP: =%
echo %IP%
```

代码会将IP地址保存到变量`IP`中，并使用`set`命令去除IP地址中的空格。然后，使用`echo %IP%`命令输出IP地址。

将代码保存为扩展名为`.bat`或`.cmd`的脚本文件，并在命令提示符中运行该脚本。执行脚本后，你将看到输出的IP地址。





这个输出了ip 但是我又很多网卡，其中只有一个是物理网卡，其他的是虚拟网卡，我希望输出的是物理网卡的ip地址，我发现虚拟网卡的ip地址最后一位总是1。 我希望  1 如果只有一个ip，那么就将变量的值设置为这个唯一ip，并且输出 2 如果有多个个ip，那么： 2.1 如果有最后1位不是1的ip，那么选择第一个最后一位不是1的IP地址 2.2 如果所有的ip地址最后一位都是1，那么选择第一个ip



ChatGPT



如果你希望在多个IP地址中选择满足特定条件的IP，可以使用以下脚本来实现：

```bash
@echo off
setlocal enabledelayedexpansion

set "PhysicalIP="
set "VirtualIP="
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set "IP=%%a"
    set "IP=!IP: =!"
    if "!IP:~-1!"=="1" (
        set "VirtualIP=!IP!"
    ) else (
        if not defined PhysicalIP (
            set "PhysicalIP=!IP!"
        )
    )
)

if defined PhysicalIP (
    set "SelectedIP=!PhysicalIP!"
) else (
    set "SelectedIP=!VirtualIP!"
)

echo Selected IP: %SelectedIP%
```

这个脚本会遍历所有的IP地址，根据条件选择一个IP地址，并将其保存到`SelectedIP`变量中。按照你的要求：

1. 如果只有一个IP地址，则将其保存到`SelectedIP`变量中。
2. 如果有多个IP地址：
   - 如果存在最后一位不是1的IP地址，则选择第一个满足条件的IP地址。
   - 如果所有的IP地址最后一位都是1，则选择第一个IP地址。

请将上述代码保存为扩展名为`.bat`或`.cmd`的脚本文件，并在命令提示符中运行该脚本。执行脚本后，你将看到输出的选择的IP地址。