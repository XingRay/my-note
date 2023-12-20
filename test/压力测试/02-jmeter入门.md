## Jmeter入门

Jmeter是用于压力测试的工具

https://jmeter.apache.org/

### 1. 下载安装

https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.5.zip

将压缩包下载到指定目录，双击 `jmeter.bat`即可运行

可选操作：

将路径添加至系统环境变量

```bash
%JMETER_HOME% = D:\develop\jmeter\apache-jmeter-5.5
```

将 `%JMETER_HOME%` 添加到 `Path`

JMeter推荐将gui作为配置界面，配置生成后通过console模式启动测试。参考官方文档的 Best Practice

https://jmeter.apache.org/usermanual/best-practices.html



### 2. 测试

#### 2.1 创建线程组

TestPlan->Add->Threads(Users)->ThreadGroup

设置

name ：线程组的名称

Number of Threads（users） ： 设置线程数

Ramp-up peroid (seconds) ：线程准备的时间

Loop Count [ infinite] ：每个线程循环执行请求的次数，勾选 infinite 则无限次



#### 2.2 创建Http请求

线程组 -> Add -> Sampler -> HttpRequest

设置：

name： 请求的名字



**Basic**：

Protocol ：请求的协议，一般是http或者 https

ServerName or IP : 请求对象的IP或者域名

PortNumber: 端口号

HttpRequest:  请求方式  

​	Path：请求路径



**Advanced**

[ ] Retrieve All Embeded Resources



#### 2.3 创建报告

创建结果树报告：

线程组 -> Add -> Listener -> View Results Tree

报告中包含请求的结果



创建总结报告：

线程组 -> Add -> Listener -> Summary Report

包含请求时间的最小值、最大值、平均值、标准差、错误率、吞吐率、发送速率、接收速率、平均字节数



创建聚合报告：

线程组 -> Add -> Listener -> Aggregate Report

包含请求时间的平均值、中位数、90%线、95%线、99%线、最小值、最大值、误率、吞吐率、发送速率、接收速率





#### 2.4 测试启动脚本

通过jmeter生成一个测试计划文件，命名为 test.jmx，保存在指定的目录，然后运行启动脚本，运行完成后会生成一个 result.jmx和 report 目录，report目录内时html页面的测试报告。

```bash
Set dir=report
del %dir%\* /F /Q
for /d %%p in (%dir%\*) Do rd /Q /S "%%p"

DEL result.jmx

jmeter -n -t test.jmx  -l result.jmx -e -o report
```

