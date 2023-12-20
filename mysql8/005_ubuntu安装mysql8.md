## ubuntu 22.04安装mysql 8.0与避坑指南

MySQL 是一个开源数据库管理系统，可作为流行的 LAMP（Linux、Apache、MySQL、PHP/Python/Perl）堆栈的一部分安装。 它实现了关系模型并使用结构化查询语言（ SQL)来管理其数据。

本教程将介绍如何在 Ubuntu 22.04 服务器上安装 MySQL 8.0 版。 通过完成它，你将拥有一个可用的关系数据库，并且可以使用它来构建您的下一个网站或应用程序。

## 安装MySQL

在 Ubuntu 22.04 上，您可以使用 APT 包存储库安装 MySQL。 在撰写本文时，默认 Ubuntu 存储库中可用的 MySQL 版本为 8.0.33 版。

```Bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql.service
```

这些命令将安装并启动 MySQL，但不会提示设置密码或进行任何其他配置更改。

## 配置MySQL

对于 MySQL 的全新安装，您需要运行数据库管理系统包含的安全脚本。 该脚本更改了一些不太安全的默认选项，例如不允许远程 root 登录和删除示例用户。

> 警告：自 2022 年 7 月起，如果您在没有进一步配置的情况下运行 mysql_secure_installation 脚本，将会发生错误。 原因是此脚本将尝试为安装的根 MySQL 帐户设置密码，但默认情况下在 Ubuntu 安装上，此帐户未配置为使用密码进行连接。

> 在 2022 年 7 月之前，此脚本会在尝试设置根帐户密码并继续执行其余提示后静默失败。 然而，在撰写本文时，脚本将在您输入并确认密码后返回以下错误：

```Bash
Output... Failed! Error: SET PASSWORD has no significance for user 'root'@'localhost' as the authentication method used doesn't store authentication data in the MySQL server. Please consider using ALTER USER instead if you want to change authentication parameters.New password:
```

> 这将导致脚本进入递归循环，您只能通过关闭终端窗口才能退出。

> 由于 mysql_secure_installation 脚本执行许多其他操作，这些操作对确保 MySQL 安装安全很有用，因此仍然建议您在开始使用 MySQL 管理数据之前运行它。 但是，为避免进入此递归循环，您需要首先调整 root MySQL 用户的身份验证方式。

> 首先进入MySQL终端

```Bash
sudo mysql
```

> 然后运行以下 ALTER USER 命令将 root 用户的身份验证方法更改为使用密码的方法。 以下示例将身份验证方法更改为 mysql_native_password：

```SQL
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
```

> 然后退出MySQL

```SQL
exit
```

> 之后，您可以毫无问题地运行 mysql_secure_installation 脚本。

以sudo权限运行mysql_secure_installation

```Bash
sudo mysql_secure_installation
```

这将引导您完成一系列提示，您可以在其中对 MySQL 安装的安全选项进行一些更改。 第一个提示将询问您是否要设置验证密码插件，该插件可用于在将新 MySQL 用户视为有效之前测试其密码强度。

如果您选择设置验证密码插件，则您创建的任何使用密码进行身份验证的 MySQL 用户都需要具有满足您选择的策略的密码：

```Bash
Output
Securing the MySQL server deployment.Connecting to MySQL using a blank password.VALIDATE PASSWORD COMPONENT can be used to test passwords
and improve security. It checks the strength of password
and allows the users to set only those passwords which are
secure enough. Would you like to setup VALIDATE PASSWORD component?Press y|Y for Yes, any other key for No: YThere are three levels of password validation policy:LOW    Length >= 8
MEDIUM Length >= 8, numeric, mixed case, and special characters
STRONG Length >= 8, numeric, mixed case, special characters and dictionary                  filePlease enter 0 = LOW, 1 = MEDIUM and 2 = STRONG:2
```

无论您是否选择设置验证密码插件，下一个提示将是为 MySQL root 用户设置密码。 输入并确认您选择的安全密码：

```Bash
Output
Please set the password for root here.New password:Re-enter new password:
```

请注意，即使您已经为 root MySQL 用户设置了密码，该用户当前未配置为在连接到 MySQL shell 时使用密码进行身份验证。

如果您使用了验证密码插件，您将收到有关新密码强度的反馈。 然后脚本将询问您是否要继续使用刚刚输入的密码，或者是否要输入一个新密码。 假设您对刚刚输入的密码的强度感到满意，请输入 Y 以继续脚本：

```Bash
Output
Estimated strength of the password: 100
Do you wish to continue with the password provided?(Press y|Y for Yes, any other key for No) : Y
```

从那里，您可以按 Y，然后按 ENTER 接受所有后续问题的默认值。 这将删除一些匿名用户和测试数据库，禁用远程 root 登录，并加载这些新规则，以便 MySQL 立即执行您所做的更改。

> 注意：安全脚本完成后，您可以重新打开 MySQL 并将 root 用户的身份验证方法改回默认值 auth_socket。 要使用密码以 root MySQL 用户身份进行身份验证，请运行以下命令：

```Bash
mysql -u root -p
```

> 然后使用此命令返回使用默认身份验证方法：

> 这意味着您可以使用 sudo mysql 命令再次以 root 用户身份连接到 MySQL。

> 脚本完成后，您的 MySQL 安装将得到保护。 您现在可以继续使用 MySQL 客户端创建专用数据库用户。

## 创建专用 MySQL 用户并授予权限

安装后，MySQL 会创建一个 root 用户帐户，您可以使用它来管理数据库。 该用户对 MySQL 服务器具有完全权限，这意味着它对每个数据库、表、用户等具有完全控制权。 因此，最好避免在管理功能之外使用此帐户。 此步骤概述了如何使用 root MySQL 用户创建新用户帐户并授予其权限。

一旦您有权访问 MySQL 终端，您就可以使用 CREATE USER 语句创建一个新用户。 这些遵循以下一般语法：

```SQL
CREATE USER 'username'@'host' IDENTIFIED WITH authentication_plugin BY 'password';
```

在 CREATE USER 之后，您指定一个用户名。 紧接着是一个 @ 符号，然后是该用户将从中连接的主机名。 如果您只打算从您的 Ubuntu 服务器本地访问此用户，您可以指定 localhost。 将用户名和主机用单引号括起来并不总是必要的，但这样做有助于防止错误。

```SQL
CREATE USER 'sammy'@'localhost' IDENTIFIED BY 'password';
```

某些版本的 PHP 存在一个已知问题，会导致 caching_sha2_password 出现问题。 如果您计划将此数据库与 PHP 应用程序（例如 phpMyAdmin）一起使用，您可能希望创建一个用户，该用户将使用较旧但仍然安全的 mysql_native_password 插件进行身份验证：

```SQL
CREATE USER 'sammy'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
```

如果您不确定，您始终可以创建一个使用 caching_sha2_plugin 进行身份验证的用户，然后稍后使用此命令对其进行更改：

```SQL
ALTER USER 'sammy'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
```

创建新用户后，您可以授予他们适当的权限。 授予用户权限的一般语法如下（需要替换PRIVILEGE为CREATE等）：

```SQL
GRANT PRIVILEGE ON database.table TO 'username'@'host'  WITH GRANT OPTION;
```

此示例语法中的 PRIVILEGE 值定义允许用户对指定数据库和表执行的操作。 您可以在一个命令中向同一用户授予多个权限，方法是用逗号分隔每个权限。 您还可以通过输入星号 (*) 代替数据库和表名称来全局授予用户权限。 在 SQL 中，星号是用于表示“所有”数据库或表的特殊字符。

为了说明这一点，以下命令授予用户创建、更改和删除数据库、表和用户的全局权限，以及从服务器上的任何表插入、更新和删除数据的权力。 它还授予用户使用 SELECT 查询数据、使用 REFERENCES 关键字创建外键以及使用 RELOAD 权限执行 FLUSH 操作的能力。 但是，您应该只授予用户他们需要的权限，因此请根据需要随意调整您自己用户的权限。

运行此 GRANT 语句，将 sammy 替换为您自己的 MySQL 用户名，以将这些权限授予您的用户：

```SQL
GRANT CREATE, ALTER, DROP, INSERT, UPDATE, INDEX, DELETE, SELECT, REFERENCES, RELOAD on *.* TO 'sammy'@'localhost' WITH GRANT OPTION;
```

请注意，此语句还包括 WITH GRANT OPTION。 这将允许您的 MySQL 用户将其拥有的任何权限授予系统上的其他用户。

警告：一些用户可能想授予他们的 MySQL 用户 ALL PRIVILEGES 权限，这将为他们提供类似于 root 用户权限的广泛超级用户权限，如下所示：

```SQL
GRANT ALL PRIVILEGES ON *.* TO 'sammy'@'localhost' WITH GRANT OPTION;
```

不应轻易授予如此广泛的权限，因为任何有权访问此 MySQL 用户的人都将完全控制服务器上的每个数据库。

在此之后，最好运行 FLUSH PRIVILEGES 命令。 这将释放服务器由于前面的 CREATE USER 和 GRANT 语句而缓存的所有内存：

```SQL
FLUSH PRIVILEGES;
```

然后你就可以推出mysql终端

```SQL
exit
```

将来，要以新的 MySQL 用户身份登录，您将使用如下命令：

```Bash
mysql -u sammy -p
```

-p 标志将导致 MySQL 客户端提示您输入 MySQL 用户的密码以进行身份验证。

最后，让我们测试一下MySQL的安装。

## 测试MySQL

不管你如何安装它，MySQL 应该已经自动开始运行了。 要对此进行测试，请检查其状态。

```Bash
systemctl status mysql.service
```

输出内容如下所示：

```Bash
● mysql.service - MySQL Community ServerLoaded: loaded (/lib/systemd/system/mysql.service; enabled; vendor preset: enabled)Active: active (running) since Tue 2023-06-13 15:43:21 CST; 35min agoProcess: 248356 ExecStartPre=/usr/share/mysql/mysql-systemd-start pre (code=exited, status=0/SUCCESS)Main PID: 248364 (mysqld)Status: "Server is operational"Tasks: 38 (limit: 18804)Memory: 371.1MCPU: 10.291sCGroup: /system.slice/mysql.service└─248364 /usr/sbin/mysqld
```

如果 MySQL 没有运行，您可以使用 sudo systemctl start mysql 启动它。

对于额外的检查，您可以尝试使用 mysqladmin 工具连接到数据库，这是一个允许您运行管理命令的客户端。 例如，此命令表示以名为 sammy (-u sammy) 的 MySQL 用户身份连接，提示输入密码 (-p)，并返回版本。 请务必将 sammy 更改为您的专用 MySQL 用户的名称，并在出现提示时输入该用户的密码：

```Bash
sudo mysqladmin -p -u sammy version
```

以下是输出示例：

```Bash
mysqladmin  Ver 8.0.33-0ubuntu0.22.04.2 for Linux on x86_64 ((Ubuntu))
Copyright (c) 2000, 2023, Oracle and/or its affiliates.Oracle is a registered trademark of Oracle Corporation and/or its
affiliates. Other names may be trademarks of their respective
owners.Server version    8.0.33-0ubuntu0.22.04.2
Protocol version  10
Connection    Localhost via UNIX socket
UNIX socket    /var/run/mysqld/mysqld.sock
Uptime:      37 min 20 secThreads: 2  Questions: 41  Slow queries: 0  Opens: 199  Flush tables: 3  Open tables: 118  Queries per second avg: 0.018
```

这意味着 MySQL 已启动并正在运行。

## 结论

至此，您现在已经在服务器上安装了基本的 MySQL 设置。