# windows备份环境变量（导入导出）



导出（备份）
win+r regedit 回车

系统环境变量
地址栏输入

```
计算机\HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Control\Session Manager\Environment 
```

![在这里插入图片描述](assets/008_windows环境变量导入导出/86cd945b55c947f2ba7abddcd9376f5e.png)

回车
右键导出，命名为系统环境变量保存

![在这里插入图片描述](assets/008_windows环境变量导入导出/1ebf6de38e7848db8396da69e72cfaa4.png)




用户环境变量
地址栏输入

```bash
计算机\HKEY_CURRENT_USER\Environment
```

![在这里插入图片描述](assets/008_windows环境变量导入导出/7fd63d4c580f4f73a5e21d574eb8c369.png)

回车
右键导出，命名为用户环境变量保存

![在这里插入图片描述](assets/008_windows环境变量导入导出/3d2a26f66fa34379b530788c22ec42de.png)

导入（恢复）
双击.reg即可

![在这里插入图片描述](assets/008_windows环境变量导入导出/a2d8c84590804ace9ec2b461c0228ca4.png)

![在这里插入图片描述](assets/008_windows环境变量导入导出/6a6f4b9dd01b4e778fc25d3259fdc460.png)