## 安装nacos

### 1. 环境

os：windows 10

java： openjdk19

nacos： nacos-server-2.2.3



### 2. 安装过程



#### 2.1 安装配置Java

参考 \software-project\deploy\java\jdk\jdk安装.md



#### 2.2 下载nacos

下载地址 ：[nacos](https://github.com/alibaba/nacos) [nacos-release](https://github.com/alibaba/nacos/releases)



#### 2.3 安装

下载 nacos-server-2.2.3.zip 到指定目录，解压即可，如：

```cmd
D:\develop\nacos\standalone\nacos-server-2.2.3
```



#### 2.4 运行

创建启动脚本及停止脚本，方便一键启动及停止

nacos-standalone-start.cmd

```cmd
D:\develop\nacos\standalone\nacos-server-2.2.3\nacos\bin\startup.cmd -m standalone
```

nacos-standalone-stutdown.cmd

```cmd
D:\develop\nacos\standalone\nacos-server-2.2.3\nacos\bin\shutdown.cmd
```

此时启动会使用系统配置的 JAVA_HOME 指定的jdk，如果需要自己指定jdk，可以在启动文件startup.sh中加入指定 JAVA_HOME ：

```cmd
set JAVA_HOME=D:\develop\java\jdk\openjdk\jdk-17
```

打开 D:\develop\nacos\standalone\nacos-server-2.2.3\nacos\bin\startup.cmd 文件，修改如下：

```bash
@echo off
rem Copyright 1999-2018 Alibaba Group Holding Ltd.
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

set JAVA_HOME=D:\develop\java\jdk\openjdk\jdk-17

if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1
set "JAVA=%JAVA_HOME%\bin\java.exe"
echo "JAVA=%JAVA%"
setlocal enabledelayedexpansion
...
```

单机模式启动nacos

```bash
D:\develop\nacos\standalone\nacos-server-2.2.3\nacos\bin\startup.cmd -m standalone
```

或者直接通过一键启动脚本启动，双击 nacos-standalone-start.cmd 启动脚本即可。



nacos启动后，在浏览器中打开http://ip:8848/nacos 如 http://192.168.0.1:8848/nacos，或者 http://localhost:8848/nacos 可以看到Nacos的界面，输入初始用户名密码（nacos/nacos），即可登录Nacos

停止nacos:

```cmd
D:\develop\nacos\standalone\nacos-server-2.2.3\nacos\bin\shutdown.cmd
```

或者使用一键停止脚本，双击 nacos-standalone-shutdown.cmd 停止脚本即可。



### 3. 配置Nacos数据库

采用MySQL数据库存储配置Nacos信息

#### 3.1 创建数据库
首先在MySQL中创建一个名称为nacos的表：

```sql
CREATE DATABASE `nacos` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```


然后创建用于访问nacos数据库的用户并授权：

创建用户

```sql
create user 'nacos'@'%' identified by 'nacos';
```

nacos.* 表示nacos的所有表

授予权限

```sql
grant all privileges on nacos.* to 'nacos'@'%' with grant option;
flush privileges;
```



#### 3.2 导入SQL语句

将nacos安装目录中\nacos\conf文件夹下的 mysql-schema.sql 导入数据库nacos中。



#### 3.3 配置Nacos

打开nacos配置文件 `D:\develop\nacos\standalone\nacos-server-2.2.3\nacos\conf\application.properties`：

修改一下内容

```properties
#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
### Deprecated configuration property, it is recommended to use `spring.sql.init.platform` replaced.
# spring.datasource.platform=mysql
spring.sql.init.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=root
db.password.0=123456
```

配置完成后，重启nacos服务即可。在2.0版本后数据库会无法连接而报错，需要将url后面加上 &allowPublicKeyRetrieval=true 如下：

```properties
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

在2.2.0.1版本之后还需要手动添加密钥，参考 [nacos-auth](https://nacos.io/zh-cn/docs/v2/guide/user/auth.html) , 自定义密钥时，推荐将配置项设置为Base64编码的字符串，且原始密钥长度不得低于32字符。例如下面的的例子：

开启鉴权：

```properties
### The auth system to use, currently only 'nacos' and 'ldap' is supported:
nacos.core.auth.system.type=nacos

### If turn on auth system:
nacos.core.auth.enabled=true
```

配置自定义密钥：

这里key-value可以自己定义，只要集群中的各个节点保持一致即可，例如：

```properties
### Since 1.4.1, worked when nacos.core.auth.enabled=true and nacos.core.auth.enable.userAgentAuthWhite=false.
### The two properties is the white list for auth and used by identity the request from other server.
nacos.core.auth.server.identity.key=nacos-core-auth-server-identity-key
nacos.core.auth.server.identity.value=nacos-core-auth-server-identity-value
```

secret.key可以自己定义一串密码，在通过base64编码工具编码即可，如 https://base64.us/ 

```properties
### The default token (Base64 String):
nacos.core.auth.plugin.nacos.token.secret.key=bmFjb3MuY29yZS5hdXRoLnBsdWdpbi5uYWNvcy50b2tlbi5zZWNyZXQua2V5
```



### 4. 配置Nacos为系统服务

在 Windows 环境下想要启动 [nacos](https://so.csdn.net/so/search?q=nacos&spm=1001.2101.3001.7020) 需要运行启动脚本。这样的启动方式需要保证 cmd 窗口一直开着，只要把这个窗口关掉，nacos 服务就停了。所以为了避免人为的误关窗口，把 nacos 注册成一个 windows-servive 就是一个好的选择。这样不仅可以保证 nacos 一直在后台运行，还可以通过注册的服务名自定义开机自启动等。

#### 4.1 安装winsw

将nacos配置为自启动服务，需要使用 WinSW ，项目地址：[winsw](https://github.com/winsw/winsw) [release](https://github.com/winsw/winsw/releases)

下载 [WinSW-x64.exe](https://github.com/winsw/winsw/releases/download/v3.0.0-alpha.11/WinSW-x64.exe) 保存到本地目录，并且将文件简化为 winsw.exe, 如：

```cmd
D:\develop\windows-service-wrapper\winsw.exe
```

#### 4.2 编译nacos服务配置文件

首先，在指定目录 D:\develop\nacos\standalone\script 文件夹下创建 nacos-windows-service.xml 文件，然后将服务信息输入到该文件中：

```xml
<service>
  <id>nacos</id>
  <name>nacos</name>
  <description>Nacos: Dynamic Naming and Configuration Service</description>
  
  <executable>D:\develop\nacos\standalone\nacos-server-2.2.3\nacos\bin\startup.cmd</executable>
  <arguments>-m standalone</arguments>
  <workingdirectory>D:\develop\nacos\standalone\work</workingdirectory>
  
  <stopexecutable>D:\develop\nacos\standalone\nacos-server-2.2.3\nacos\bin\shutdown.cmd</stopexecutable>
  <stoparguments></stoparguments>
  
  <log mode="roll" />
  <logpath>D:\develop\nacos\standalone\work\logs</logpath>

  <!--
    OPTION: depend
    Optionally specifies services that must start before this service starts.
  -->
  <depend>mysql</depend>
  <onfailure action="restart" />
</service>

```

4.3 编写服务一键脚本

服务安装脚本 D:\develop\nacos\standalone\script\nacos-service-install.cmd

```cmd
D:\develop\windows-service-wrapper\winsw.exe install D:\develop\nacos\standalone\script\nacos-windows-service.xml
```

服务启动脚本 D:\develop\nacos\standalone\script\nacos-service-start.cmd

```
D:\develop\windows-service-wrapper\winsw.exe start D:\develop\nacos\standalone\script\nacos-windows-service.xml
```

查看服务状态脚本 D:\develop\nacos\standalone\script\nacos-service-status.cmd

```
D:\develop\windows-service-wrapper\winsw.exe status D:\develop\nacos\standalone\script\nacos-windows-service.xml
```

卸载服务脚本：

```bash
D:\develop\windows-service-wrapper\winsw.exe uninstall D:\develop\nacos\standalone\script\nacos-windows-service.xml
```

双击服务安装脚本安装windows服务，再运行服务启动脚本，运行成功后可以通过查看服务状态脚本查看服务状态。为测试效果，可以直接重启Windows，当系统重启后，nacos服务将自动启动。