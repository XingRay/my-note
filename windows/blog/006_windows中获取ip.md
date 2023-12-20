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