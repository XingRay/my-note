# java执行windows下cmd命令的方法



Runtime rt = Runtime.getRuntime();
Process p = rt.exec("cmd.exe /c shutdown -a");
System.out.println(p.toString());



```shell
# 是执行完dir命令后关闭命令窗口。
cmd /c dir 

# 是执行完dir命令后不关闭命令窗口。
cmd /k dir 

# 会打开一个新窗口后执行dir指令，原窗口会关闭。
cmd /c start dir 

# 会打开一个新窗口后执行dir指令，原窗口不会关闭。
cmd /k start dir 

```



示例：



```java
executor.execute("mvn.cmd -v");
executor.execute("cmd /c mvn package");
```