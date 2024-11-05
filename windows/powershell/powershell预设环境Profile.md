powershell启动时自动加载4个预设配置文件

查找这4个预设文件:

```
 $PROFILE | Format-List * -Force
```

```shell
AllUsersAllHosts       : C:\Program Files\PowerShell\7\profile.ps1
AllUsersCurrentHost    : C:\Program Files\PowerShell\7\Microsoft.PowerShell_profile.ps1
CurrentUserAllHosts    : D:\OneDrive\文档\PowerShell\profile.ps1
CurrentUserCurrentHost : D:\OneDrive\文档\PowerShell\Microsoft.PowerShell_profile.ps1
Length                 : 58
```

文件不一定都存在,如果存在则会自动加载

