# 将windows系统上的SSH密钥复制到ubuntu中使用



要将 Windows 上配置好的 GitHub SSH 密钥复制到 WSL 中的 Ubuntu 环境，可以按照以下步骤操作：

------

### **1. 找到 Windows 上的 SSH 密钥**

默认情况下，SSH 密钥保存在以下目录：

```shell
C:\Users\<你的用户名>\.ssh\
```

**文件包括：**

- `id_rsa`（私钥）
- `id_rsa.pub`（公钥）

------

### **2. 在 WSL 中创建 `.ssh` 目录**

在 WSL 的 Ubuntu 环境中，确保存在 `~/.ssh` 目录并设置正确权限：

```
mkdir -p ~/.ssh
chmod 700 ~/.ssh
```

------



### **3. 复制密钥文件到 WSL**

#### **方法 1：直接从 Windows 路径复制**

1. 在 WSL 中挂载了 Windows 文件系统，可以直接访问 Windows 的 `.ssh` 文件夹。路径通常如下：

   ```
   /mnt/c/Users/<你的用户名>/.ssh/
   ```

2. 复制密钥文件到 WSL：

   ```
   cp /mnt/c/Users/<你的用户名>/.ssh/id_rsa ~/.ssh/
   cp /mnt/c/Users/<你的用户名>/.ssh/id_rsa.pub ~/.ssh/
   ```

3. 设置正确的权限：

   ```
   chmod 600 ~/.ssh/id_rsa
   chmod 644 ~/.ssh/id_rsa.pub
   ```

   

#### **方法 2：通过剪贴板粘贴**

1. 打开 Windows 的 `.ssh` 文件夹，用文本编辑器（如 Notepad）打开 `id_rsa` 和 `id_rsa.pub`。

2. 在 WSL 中创建并编辑密钥文件：

   ```
   nano ~/.ssh/id_rsa
   ```

3. 将 

   ```
   id_rsa
   ```

    的内容粘贴进去，保存后退出。

   ```
   chmod 600 ~/.ssh/id_rsa
   ```

4. 同样操作 

   ```
   id_rsa.pub
   ```

   ，并设置权限：

   ```
   nano ~/.ssh/id_rsa.pub
   chmod 644 ~/.ssh/id_rsa.pub
   ```

------

### **4. 添加私钥到 SSH Agent（可选）**

1. 启动 SSH Agent：

   ```
   eval $(ssh-agent)
   ```

2. 添加私钥到 Agent：

   ```
   ssh-add ~/.ssh/id_rsa
   ```

------

### **5. 测试 SSH 配置**

测试连接到 GitHub：

```
ssh -T git@github.com
```

如果配置正确，你会看到类似如下的输出：

```
Hi <Your GitHub Username>! You've successfully authenticated, but GitHub does not provide shell access.
```

这样，你就可以在 WSL 中使用 Windows 配置好的 GitHub SSH 密钥了！