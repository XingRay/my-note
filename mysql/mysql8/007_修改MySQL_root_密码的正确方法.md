## 修改 MySQL root 密码的正确方法

### 步骤 1：登录 MySQL（使用默认插件）

```
mysql -u root -p
```

输入当前密码（如果不知道密码，请跳到"忘记密码解决方案"部分）

### 步骤 2：使用正确的插件修改密码

```
-- 使用默认的 caching_sha2_password 插件
ALTER USER 'root'@'localhost' IDENTIFIED BY '您的新密码';

-- 或者显式指定插件
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '您的新密码';
```

### 步骤 3：刷新权限

```
FLUSH PRIVILEGES;
```

### 步骤 4：退出并测试新密码

```
EXIT;
mysql -u root -p
```

输入新密码验证是否成功

## 🔄 如果忘记当前密码（重置密码方案）

### 方法 1：使用初始化文件

1. 停止 MySQL 服务：`net stop mysql`
2. 创建初始化文件 `init.sql`：`ALTER USER 'root'@'localhost' IDENTIFIED BY '您的新密码';`
3. 以安全模式启动 MySQL：`mysqld --init-file=D:\\path\\to\\init.sql --console`
4. 看到密码重置成功后，按 Ctrl+C 停止服务
5. 正常启动 MySQL 服务：`net start mysql`

### 方法 2：使用安全模式（推荐）

1. 停止 MySQL 服务：`net stop mysql`
2. 以跳过权限检查模式启动：`mysqld --console --skip-grant-tables --shared-memory`
3. 打开另一个命令提示符窗口，登录 MySQL（无需密码）：`mysql -u root`
4. 在 MySQL 命令行中执行：`FLUSH PRIVILEGES; ALTER USER 'root'@'localhost' IDENTIFIED BY '您的新密码'; EXIT;`
5. 关闭第一个窗口（按 Ctrl+C）
6. 正常启动 MySQL 服务：`net start mysql`

## ⚙️ 配置 MySQL 使用旧版身份验证（可选）

如果您确实需要使用 `mysql_native_password`，可以这样配置：

### 1. 修改 root 用户使用旧插件

```
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '您的新密码';
```

### 2. 修改全局配置（在 my.ini 中）

```
[mysqld]
default_authentication_plugin=mysql_native_password
```

重启 MySQL 服务使配置生效。

## 📝 完整操作示例（安全模式重置）

```
# 停止服务
net stop mysql

# 以跳过权限模式启动
mysqld --console --skip-grant-tables --shared-memory

# 在另一个窗口登录
mysql -u root

# MySQL 命令行
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'MyNewStrongPassword!';
EXIT;

# 关闭第一个窗口（Ctrl+C）
# 正常启动服务
net start mysql

# 使用新密码登录测试
mysql -u root -p
```

## 💡 密码安全建议

1. **使用强密码**：至少12个字符包含大小写字母、数字和特殊符号示例：`MyDB!Passw0rd@2025`
2. **避免常见错误**：不要使用 `root`作为密码不要使用连续数字或简单模式不要使用个人信息作为密码
3. **定期更换密码**：`-- 设置密码过期策略 ALTER USER 'root'@'localhost' PASSWORD EXPIRE INTERVAL 90 DAY;`

## 📋 验证密码策略

```
SHOW VARIABLES LIKE 'validate_password%';
```

如果需要调整密码策略：

```
SET GLOBAL validate_password.policy = LOW;
SET GLOBAL validate_password.length = 8;
```

## 总结

MySQL 8.x 默认使用更安全的 `caching_sha2_password`身份验证插件，这是导致您遇到错误的主要原因。通过上述方法，您可以：

1. 使用正确的插件重置密码
2. 或配置 MySQL 使用旧版身份验证
3. 或完全重置忘记的密码

建议使用默认的 `caching_sha2_password`插件以获得更好的安全性，除非您的应用程序明确要求使用旧版插件。