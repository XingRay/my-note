# git Filename too long解决方案



## 问题

git clone代码时提示**Filename too long**，一般是在windows下出现的问题。

## 解决方法

用管理员打开命令窗口，输入**git config --system core.longpaths true**解决。