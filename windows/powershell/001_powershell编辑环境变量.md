# Powershell编辑环境变量



通过管理员权限启动powershell



1 设置环境变量(永久)

```powershell
[System.Environment]::SetEnvironmentVariable("MY_VAR", "C:\Path\To\Value", [System.EnvironmentVariableTarget]::Machine)
```

或者

```powershell
setx MY_VARIABLE "Hello, World!"
```

使用 `setx` 设置的变量在当前会话中不可用，必须打开一个新的 PowerShell 会话才能访问它





2 当前会话有效

使用 `$Env:` 设置的环境变量只在当前 PowerShell 会话中有效。关闭会话后，变量将丢失。

设置一个新的环境变量

```
$Env:MY_VARIABLE = "Hello, World!"
```

在设置环境变量后，可以通过 `$Env:` 访问它：

```
$Env:MY_VARIABLE
```





2 读取环境变量

```powershell
$env:PATH
```

```powershell
[System.Environment]::GetEnvironmentVariable("PATH")
```

还可以指定作用域（例如 `User` 或 `Machine`）：

```powershell
# 读取系统作用域（Machine）下的 PATH 环境变量
[System.Environment]::GetEnvironmentVariable("PATH", "Machine")
```



3 查看所有环境变量

```shell
Get-ChildItem Env:
```





## 添加到 PATH

### 1. 临时添加路径到 PATH

使用 `$Env:PATH` 来动态添加路径，这种方法只在当前 PowerShell 会话中有效：

```
powershell复制代码# 添加新路径到现有 PATH
$Env:PATH += ";C:\Path\To\Add"
```

注意 `;` 用于分隔多个路径。



### 2. 永久添加路径到 PATH

如果希望永久添加路径到系统的 `PATH` 环境变量，可以使用 `setx` 命令。以下是示例：

```
powershell复制代码# 永久添加新路径到 PATH
setx PATH "$($Env:PATH);C:\Path\To\Add"
```



注意, 使用 setx PATH 时,如果PATH拼接之后过长(最多1024)会截断,所以PATH较长时可以使用下面的方法:

```
function path-add {
    param (
        [string]$NewPath,
        [string]$BackupFile = "$HOME\.powershell\path_backup.txt"
    )

    # 创建 .powershell 目录（如果不存在）
    $backupDir = Split-Path -Path $BackupFile -Parent
    if (-not (Test-Path -Path $backupDir)) {
        New-Item -ItemType Directory -Path $backupDir | Out-Null
    }

    # 获取当前 PATH
    $currentPath = [System.Environment]::GetEnvironmentVariable("PATH", "Machine")

    # 将当前 PATH 保存到备份文件
    $currentPath | Out-File -FilePath $BackupFile

    # 检查当前 PATH 是否已以 ';' 结尾，若不是则添加
    if ($currentPath -notmatch ';$') {
        $currentPath += ';'
    }

    # 添加新的路径
    $newPath = "$currentPath$NewPath"

    # 设置新的 PATH
    [System.Environment]::SetEnvironmentVariable("PATH", $newPath, "Machine")

    Write-Host "已成功添加 '$NewPath' 到 PATH，并备份当前值到 '$BackupFile'。"
}
```

将代码添加到 profile 中使用即可,如:

```powershell
setx JAVA_HOME D:\dev\jdk21
path-add %JAVA_HOME%\bin
```





### 注意事项

新开会话：使用 `setx` 后，需要关闭当前 PowerShell 窗口并重新打开，才能在新会话中看到更新的 `PATH` 变量。

注意字符限制：`setx` 对环境变量的长度有限制（通常为 1024 个字符），如果 `PATH` 变量太长，可能会导致无法添加更多路径。

替换 vs. 添加：如果使用 `setx` 命令并希望完全替换现有的 `PATH`，可以直接将新路径指定为 `setx PATH "C:\NewPath"`，这样会覆盖旧的 `PATH` 值。





### 示例

```
powershell复制代码# 临时添加路径
$Env:PATH += ";C:\NewPath"

# 永久添加路径
setx PATH "$($Env:PATH);C:\NewPath"
```

这段代码将 `C:\NewPath` 添加到当前会话的 `PATH`，并且通过 `setx` 命令将其永久添加到系统的 `PATH` 变量中。



