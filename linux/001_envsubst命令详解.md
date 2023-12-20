## envsubst命令详解

Linux envsubst Command

https://www.baeldung.com/linux/envsubst-command



### 1 基本语法

```bash
envsubst [SHELL-FORMAT]
```

envsubst 用于将 stdin 中的 变量占位符 替换为 bash变量的值，并输入到 stdout。（若变量不存在，则替换为空字符串。）

变量占位符支持两种格式：`$VARIABLE` 和 `${VARIABLE}`
SHELL-FORMAT：用于限定需要替换的变量,多个变量可以通过逗号或者空格分隔。默认全部替换。



### 2 示例

#### 2.1 简单示例

执行命令：

```bash
echo 'my name is $USER, directory is $HOME.' | envsubst | cat
```

等价于 

```bash
echo 'my name is ${USER}, directory is ${HOME}.' | envsubst | cat
```

输出结果：

```bash
my name is root, directory is /root.
```



#### 2.2 限定需要替换的变量

执行命令：

```bash
echo 'my name is $USER, directory is $HOME.' | envsubst '$USER' | cat
```

输出结果：

```bash
my name is root, directory is $HOME.
```

可以看到，只指定了`$USER`，则`$HOME`不会被替换为值。

执行命令：

```bash
echo 'my name is $USER, directory is $HOME.' | envsubst '$USER,$HOME' | cat
```

等价于 

```bash
echo 'my name is $USER, directory is $HOME.' | envsubst '$USER $HOME' | cat）
```

输出结果：

```bash
my name is root, directory is /root.
```



#### 2.3 替换模板文件中的变量，并输出到文件

假设模板文件 info.template 的内容为：

```bash
my name is $USER, directory is $HOME.
```

目标写入文件为：`my.txt`

执行命令：

```bash
cat info.template | envsubst '$USER,$HOME' > my.txt
```

最终，`my.txt` 文件的内容为：

```bash
my name is root, directory is /root.
```

另一种命令格式

```bash
envsubst '$USER,$HOME' < info.template > my.txt
```

