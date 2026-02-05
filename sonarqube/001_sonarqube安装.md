SonarQube安装、出现启动出错并解决记录、配合idea配置使用，gradle项目配置


说明:
Sonarqube是一个功能非常强大的代码质量检查、管理的开源工具。它通过插件的形式能够识别常见的多种编程语言（例如Java, C++, Pythod等）代码质量问题。需要部署在java环境才能使用。

1.下载SonarQube
跳转SonarQube官网
下载对应jdk版本的Sonarqube版本（我个人使用:jdk8-对应Sonarqube7.7）


2.Sonarqube安装
直接解压缩，然后进入对应系统的文件夹


3.汉化 (看你个人需要)
跳转汉化包下载


下载完直接丢到lib下common


4.启动Sonarqube
直接双击启动，并访问 http://127.0.0.1:9000,账号密码都是 admin

若启动失败，在D:\sonarqube\sonarqube-7.7\logs\web.log查看原因


出现问题一：端口号被占用
netstat -aon|findstr “9000”


修改默认端口9000，sonar.web.port

修改为9999，9999没有被占用


重新启动


访问：http://127.0.0.1:9000，启动成功

修改密码

可以修改账号密码，也可以新建群组分配权限等

5.结合idea
下载插件


安装之后重启idea生效，在Settings->Tools 中找到 SonarLint


会自动跳转


我的项目依赖是用的gradle的，不是maven
build.gradle里主要配置如下：

plugins {
  id "org.sonarqube" version "2.7"
}
sonarqube {
  properties {
    property "sonar.host.url", "http://localhost:9999"
    property "sonar.login", "账号"
    property "sonar.password", "密码"
  }
}
apply plugin: 'org.sonarqube'


成功！看页面
