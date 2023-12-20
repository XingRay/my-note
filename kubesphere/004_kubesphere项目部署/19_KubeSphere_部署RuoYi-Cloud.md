## KubeSphere部署RuoYi-Cloud

[toc]



RuoYi-Cloud地址：

https://gitee.com/y_project/RuoYi-Cloud

clone：

```bash
git clone git@gitee.com:y_project/RuoYi-Cloud.git
```

使用idea打开项目



### 1 本地部署

#### 1.1 下载安装启动nacos 

参考 `myNote\software-project\deploy\nacos\nacos-standalone-windwos.md`



#### 1.2 导入 ruoyi-cloud 的初始数据

##### 导入 ry_config

在项目的 `/sql` 目录下有项目设置的初始化数据的脚本，当前版本为：`ry_config_20220929.sql`，在mysql客户端中直接执行该脚本。该项目会直接使用 ry_config作为nacos的数据库，而不使用nacos自带的数据库，因此nacos也需要更新设置，可以考虑为 RuoYi-Cloud 单独运行一份nacos，停止运行并复制当前部署的nacos到指定目录，如：

```bash
D:\webapp\ruoyi-cloud\standalone\nacos-server-2.2.3\nacos
```

修改 `config/application.properties` 文件中的数据库配置，由数据库 nacos 转换为 ry-config ，将：

```properties
### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
```

更改为：

```properties
### Connect URL of DB:
db.url.0=jdbc:mysql://127.0.0.1:3306/ry-config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
```

以单节点模式启动 nacos 。运行 `/script/nacos-standalone-start.cmd`， nacos启动后，使用账号密码 nacos/nacos 登录进入，即可在 配置管理-配置列表 中看到 RuoYi-Cloud 的默认配置数据：

| Data Id                | Group         | 归属应用 | 操作                         |
| :--------------------- | :------------ | :------- | :--------------------------- |
| application-dev.yml    | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |
| ruoyi-gateway-dev.yml  | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |
| ruoyi-auth-dev.yml     | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |
| ruoyi-monitor-dev.yml  | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |
| ruoyi-system-dev.yml   | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |
| ruoyi-gen-dev.yml      | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |
| ruoyi-job-dev.yml      | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |
| ruoyi-file-dev.yml     | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |
| sentinel-ruoyi-gateway | DEFAULT_GROUP |          | 详情\|示例代码\|编辑\|删除\| |



##### 导入 ry.sql

在项目的 `/sql` 目录下有项目数据的初始化数据的脚本，当前版本为：`ry_20230223.sql`，这个数据库需要自行创建，根据 ry_config 导入 nacos 中的配置文件，如 ruoyi-system-dev.yml 中配置的数据库连接为：

```bash
url: jdbc:mysql://localhost:3306/ry-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
```

其中配置的数据库为 ry-cloud ，字符编码为 utf8 ，在mysql客户端中连接mysql，创建 ry-cloud 数据库，导入 ry_20230223.sql 。



##### 导入 quartz.sql

在 ruoyi-job-dev.yml 中查看数据库配置：

```bash
url: jdbc:mysql://localhost:3306/ry-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
```

可以看到任务调度模块连接的数据库还是 ry-cloud ，将脚本 quartz.sql 导入数据库。



##### 导入 ry_seata.sql

当前版本为： ry_seata_20210128.sql ，该脚本会自动创建数据库，直接导入，在数据库中会创建数据库 ry_seata 。



#### 1.3 修改配置

导入完成后，需要在nacos中修改配置， 修改mysql的账号密码。需要修改的配置文件有： 

```bash
ruoyi-system-dev.yml
ruoyi-gen-dev.yml
ruoyi-job-dev.yml
```

将mysql相关配置修改为：

```yaml
# spring配置
spring:
  datasource:
      datasource:
          # 主库数据源
          master:
            driver-class-name: com.mysql.cj.jdbc.Driver
            url: jdbc:mysql://localhost:3306/ry-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
            username: root
            password: 123456
```



#### 1.4 运行前端项目

在 RuoYi-Cloud\ruoyi-ui 目录下运行：

```bash
npm install --registry=https://registry.npm.taobao.org
```

```bash
npm run dev
```



#### 1.5 修改redis配置

运行后自动进入管理平台主界面，此时验证码还无法加载。还需要在nacos中的各个配置文件中修改redis配置。首先需要在启动一个redis服务，这里可以直接使用前面在KubeSphere中配置的redis服务，暴露的service的端口为 30318，密码为：123456， 使用集群任意节点ip+30318，如 192.168.0.112:30318，就可以连入redis服务。修改 RuoYi-Cloud 中的配置文件中的redis相关配置，相关文件有：

```bash
ruoyi-gateway-dev.yml  
ruoyi-auth-dev.yml  
ruoyi-system-dev.yml    
ruoyi-gen-dev.yml   
ruoyi-job-dev.yml
```

将其中的redis相关配置修改如下：

```yaml
spring:
  redis:
    host: 192.168.0.112
    port: 30318
    password: 123456
```



#### 1.6 启动微服务

启动gateway ，直接启动 gateway项目会报错：

```bash
com.alibaba.nacos.api.exception.NacosException: user not found!
```

原因是由于服务端设置了鉴权，`nacos.core.auth.enabled=true`(参照官方文档https://nacos.io/zh-cn/docs/auth.html) 客户端需增加相关配置(username和password)，修改 `bootstrap.yml`

```yaml
spring:
  cloud:
    nacos:
      discovery:
        # 服务注册地址
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
      config:
        # 配置中心地址
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
```

再启动就不会报错了。其他的各个服务也需要做同样的修改。分别启动各个服务即可。



#### 1.7 测试

前端ui项目和所有微服务都正常启动后，在浏览器中访问：  http://localhost:80/ 进入首页，此时验证码可以正常显示，说明请求过程正常。本地项目部署完成





### 2 k8s手动部署

云上部署需要考虑4个方面：

1 中间件

有状态： 中间件要以有状态服务的方式上云

数据导入： 中间件可能需要导入默认数据



2 微服务

无状态：微服务上云一般是以无状态服务的方式部署

制作镜像：要自己制作每一个微服务模块的镜像



3 网络

各种访问地址：上云以后各个组件、各个网络的模型。微服务访问中间件一般使用内网的IP或者内网的域名，



4 配置

生产配置分离：生产环境、测试环境、开发环境配置分离，

URL：各种配置中的URL地址，在k8s集群中要优先以集群内的地址访问如nacos、mysql、redis等中间件。



#### 2.1 部署中间件

由于之前在KubeSphere中已经部署了redis和mysql，但是nacos没有部署，这里需要先将mysql中的数据从本地库迁移到k8s中的mysql中。

##### 2.1.1 mysql数据迁移

如： navicat-工具-数据传输。

在mysql-k8s中创建数据库 ry-config ry-cloud ry-seata ，字符集都选择为 utf8mb4 。注意在传输文件之要打开源和目标mysql上的各个数据库，否则会传输失败。

点击 `工具-数据传输`，源选择本地数据库，目标选择 `连接`，选择 `mysql-k8s` 源和目标都各自选择 ry-config ry-cloud ry-seata 三个数据库 ，点击下一步，在 `数据库对象`  在 `表 视图 函数 事件`中都选择`运行期间的全部*`，点击`下一步`，点击`确认`，点击`开始`



##### 2.1.2 nacos部署

采用集群模式在k8s上部署nacos， 参考 `myNote\software-project\deploy\kubernetes\18_KubeSphere_部署nacos.md`，注意这里使用的nacos主配置文件中要修改使用的数据库，数据库设置如下：

```properties
#*************** Config Module Related Configurations ***************#
### If use MySQL as datasource:
### Deprecated configuration property, it is recommended to use `spring.sql.init.platform` replaced.
# spring.datasource.platform=mysql
spring.sql.init.platform=mysql

### Count of DB:
db.num=1

### Connect URL of DB:
db.url.0=jdbc:mysql://mysql-standalone-o4av.project-test:3306/ry-config?characterEncoding=utf8&connectTimeout=3000&socketTimeout=3000&autoReconnect=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
db.user.0=root
db.password.0=123456
```

注意

```
1 修改数据库地址为mysql服务的dns，如自动生成的headless服务的dns：mysql-standalone-o4av或者自定义服务的dns
2 数据库由 nacos 改为 ry-config
```

按照nacos部署文档部署好nacos集群后，在浏览器上访问 `http://192.168.0.112:3****/nacos`  使用nacos/nacos 账号登录，进入 `配置管理-配置列表` 可以看到RuoYi-Cloud项目的默认配置。



##### 2.1.3 修改配置文件

由于微服务上线后需要使用`prod`环境，这里需要先创建一个prod的命名空间，再在nacos中将`dev`的环境的配置文件复制一份到`prod`环境。

在`命名空间`中，点击`新建命名空间`

```bash
命名空间ID：
命名空间名：prod
描述：生产环境
```

点击`确定`

`配置管理-配置列表-public` 全选配置文件，点击 `克隆`，在`克隆配置`页，选择`目标空间`为 `prod`，将下面列出的所有配置文件中的配置文件名进行修改：

```bash
<application-name>-dev.yml
```

修改为：

```bash
<application-name>-prod.yml
```

配置文件名中没有 `-dev` 的不用修改。修改完成后点击 `开始克隆`，此时再切换到 `配置管理-配置列表-prod`可以看到复制的配置文件。



#### 2.2 部署微服务

在 `RuoYi-Cloud` 项目中已经提供了docker镜像脚本，在 `RuoYi-Cloud\docker` 目录下可以看到各个镜像打包需要的dockerfile，微服务打包所需要的dockerfile在 `RuoYi-Cloud\docker\ruoyi` 目录下，如 `RuoYi-Cloud\docker\ruoyi\modules\system\dockerfile`,内容如下：

```dockerfile
# 基础镜像
FROM  openjdk:8-jre
# author
MAINTAINER ruoyi

# 挂载目录
VOLUME /home/ruoyi
# 创建目录
RUN mkdir -p /home/ruoyi
# 指定路径
WORKDIR /home/ruoyi
# 复制jar文件到路径
COPY ./jar/ruoyi-modules-system.jar /home/ruoyi/ruoyi-modules-system.jar
# 启动系统服务
ENTRYPOINT ["java","-jar","ruoyi-modules-system.jar"]
```

这里使用了基础镜像为 openjdk:8-jre ，这里需要修改为使用jdk，因为jdk中有很多可以用于在线调试的工具，方便诊断和修复线上问题，修改后的dockerfile如下：注意：**要将nacos的地址替换为k8s集群中的nacos的headless服务的dns，这里是 nacos-cluster.project-test**

```dockerfile
FROM openjdk:8-jdk
#LABEL maintainer=your-name

#docker run -e PARAMS="--server.port 9090"
ENV PARAMS="--server.port=8080 --spring.profiles.active=prod --spring.cloud.nacos.discovery.server-addr=nacos-cluster.project-test:8848 --spring.cloud.nacos.discovery.namespace=b304af82-49b7-47d9-b4b5-0a6153efa584 --spring.cloud.nacos.config.server-addr=nacos-cluster.project-test:8848 --spring.cloud.nacos.config.namespace=b304af82-49b7-47d9-b4b5-0a6153efa584 --spring.cloud.nacos.config.file-extension=yml"

# 设置统一时区
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

COPY target/*.jar /app.jar
EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","java -Dfile.encoding=utf8 -Djava.security.egd=file:/dev/./urandom -jar app.jar ${PARAMS}"]
```

注意

```bash
1 这里要将nacos的访问地址改为实际项目中k8s中的nacos的headless服务的dns，这里是 `nacos-cluster.project-test`
2 nacos的prod的namespace要使用id来进行设置，不能设置为 prod ,必须是类似 b304af82-49b7-47d9-b4b5-0a6153efa584 的 id
3 配置文件后缀是yml还是yaml，或者properties，要根据nacos上的配置文件的实际情况设置，这里设置为 yml 。
4 所有的容器在pod暴露的端口统一改为8080，不同的服务在不同的pod启动，不会有端口冲突
5 设置统一时区
6 激活prod环境
```

将上面的dockerfile脚本内容保存为文件`Dockerfile`并复制到各个微服务项目的根目录，与target目录同级。



微服务上云部署分为几步：

1 打包，使用maven打成可执行的jar包

2 将jar包和dockerfile上传到服务器

3 制作镜像，docker根据Dockerfile把可执行的jar制作成docker镜像

4 推送镜像到镜像服务器，如阿里云的镜像仓库或者自建的harbor镜像私服

5 应用部署，通过KubeSphere部署应用到k8s



##### 2.2.1 打包

在idea中在maven选项中，在根节点选择 clean package，选择跳过测试，执行。打包前注意整个项目的文件编码统一成utf8。制作好可执行jar包后进行简单的测试：

启动本地nacos

先开启 RuoYi-Cloud专用的本地nacos服务， `ruoyi-cloud\nacos\script\nacos-standalone-start.cmd`，使用浏览器登录本地nacos http://localhost:8848/nacos，确认开发环境的配置是否存在。



启动测试微服务

将打包生成的jar包，如ruoyi-auth.jar ，复制到一个临时目录，执行：

```bash
java -Dfile.encoding=utf8 -jar ruoyi-auth.jar
```

可以看到输出

```bash
认证授权中心启动成功
```

说明启动成功



##### 2.2.2 上传到服务器

将jar包和dockerfile上传到服务器，根据dockerfile的内容，需要有一个target目录来保存jar包，将各个微服务的jar包安装服务名保存到指定目录，目录结构为：

```bash
└───ruoyi-output
    ├───ruoyi-auth
    │   │   Dockerfile
    │   │
    │   └───target
    │           ruoyi-auth.jar
    │
    ├───ruoyi-gateway
    │   │   Dockerfile
    │   │
    │   └───target
    │           ruoyi-gateway.jar
    │
    ├───ruoyi-modules-file
    │   │   Dockerfile
    │   │
    │   └───target
    │           ruoyi-modules-file.jar
    │
    ├───ruoyi-modules-gen
    │   │   Dockerfile
    │   │
    │   └───target
    │           ruoyi-modules-gen.jar
    │
    ├───ruoyi-modules-job
    │   │   Dockerfile
    │   │
    │   └───target
    │           ruoyi-modules-job.jar
    │
    ├───ruoyi-modules-system
    │   │   Dockerfile
    │   │
    │   └───target
    │           ruoyi-modules-system.jar
    │
    └───ruoyi-visual-monitor
        │   Dockerfile
        │
        └───target
                ruoyi-visual-monitor.jar
```

将这个`ruoyi-ouput`整体目录上传至k8s任意节点，如master01节点。



##### 2.2.3 制作镜像

上传完成后，在服务器上，进入各个服务器的文件夹使用docker cli 根据Dockerfile把可执行的jar制作成docker镜像，执行：

```bash
docker build -t <image-name>:<image-version> -f <dockerfile> <work-directory>
```

在 `/ruoyi-output/ruoyi-auth` 目录执行：

```bash
docker build -t ruoyi-auth:v1.0 -f Dockerfile .
```

其他微服务也依次执行这个命令生成docker镜像。

也可以在 /ruoyi-output 目录下执行下列指令一次性打包所有镜像：

```bash
cd ruoyi-auth
docker build -t ruoyi-auth:v1.0 -f Dockerfile .
cd ..

cd ruoyi-gateway
docker build -t ruoyi-gateway:v1.0 -f Dockerfile .
cd ..

cd ruoyi-modules-file
docker build -t ruoyi-modules-file:v1.0 -f Dockerfile .
cd ..

cd ruoyi-modules-gen
docker build -t ruoyi-modules-gen:v1.0 -f Dockerfile .
cd ..

cd ruoyi-modules-job
docker build -t ruoyi-modules-job:v1.0 -f Dockerfile .
cd ..

cd ruoyi-modules-system
docker build -t ruoyi-modules-system:v1.0 -f Dockerfile .
cd ..

cd ruoyi-visual-monitor
docker build -t ruoyi-visual-monitor:v1.0 -f Dockerfile .
cd ..
```

执行完成后，列出docker中的镜像，执行：

```bash
docker images
```

输出：

```
root@k8s-master01:~/ruoyi-output# docker images
REPOSITORY             TAG       IMAGE ID       CREATED              SIZE
ruoyi-visual-monitor   v1.0      11e17c88495f   8 seconds ago        592MB
ruoyi-modules-system   v1.0      542aa00e1e99   25 seconds ago       632MB
ruoyi-modules-job      v1.0      85bb3c36229f   42 seconds ago       629MB
ruoyi-modules-gen      v1.0      d480eff61241   59 seconds ago       628MB
ruoyi-modules-file     v1.0      3aa9e76e1f7c   About a minute ago   622MB
ruoyi-gateway          v1.0      64bed895df33   About a minute ago   625MB
ruoyi-auth             v1.0      baf4d08bf774   5 minutes ago        617MB
```

这些镜像不能只在master01节点，实际项目中k8s集群会有很多节点，每个节点都要能下载到这些镜像，因此需要将镜像推送到远程镜像仓库。



##### 2.2.4 推送镜像

这里使用阿里云的镜像仓库。



1 开通阿里云镜像管理平台

进入阿里云平台： https://cr.console.aliyun.com/cn-hangzhou/instances ，点击 `个人实例`，点击`创建个人版`，弹出的提示中勾选 知晓...  ,点击`确定`，开通完成后跳转到 https://cr.console.aliyun.com/cn-hangzhou/instance/new ，点击 设置Registry登录密码，设置自己的密码。设置完成后跳转到镜像仓库列表： https://cr.console.aliyun.com/cn-hangzhou/instance/repositories ，



2 创建命令空间

在`仓库管理-命名空间`中点击`创建命名空间`，输入自定义的命名空间名，点击`确认`即可。实际项目中一般需要设置仓库为`私有`。



3 创建镜像仓库

在`仓库管理-镜像仓库`中点击`创建镜像仓库`输入仓库信息：

```bash
命名空间：选择刚才创建的命名空间
仓库名称：project-test
仓库类型：私有
摘要：测试项目的docker镜像仓库
描述信息：测试项目的docker镜像仓库
```

点击`下一步`，进入`代码源`设置页，这里选择`本地仓库`，点击`创建镜像仓库`即可。这时在`仓库管理-镜像仓库`中可以看到新创建的仓库，点击仓库名，进入仓库的基本信息页，可以看到仓库的基本信息：

```bash
仓库名称：project-test
仓库地域：华东1（杭州）
仓库类型：私有
代码仓库：无
公网地址：registry.cn-hangzhou.aliyuncs.com/<namespace>/project-test
专有网络：registry-vpc.cn-hangzhou.aliyuncs.com/<namespace>/project-test
经典网络：registry-internal.cn-hangzhou.aliyuncs.com/<namespace>/project-test
摘要：测试项目的docker镜像仓库
```

操作指南：

```bash
1. 登录阿里云Docker Registry
$ docker login --username=******@***.com registry.cn-hangzhou.aliyuncs.com
用于登录的用户名为阿里云账号全名，密码为开通服务时设置的密码。

您可以在访问凭证页面修改凭证密码。

2. 从Registry中拉取镜像
$ docker pull registry.cn-hangzhou.aliyuncs.com/<namespace>/project-test:[镜像版本号]
3. 将镜像推送到Registry
$ docker login --username=******@***.com registry.cn-hangzhou.aliyuncs.com
$ docker tag [ImageId] registry.cn-hangzhou.aliyuncs.com/<namespace>/project-test:[镜像版本号]
$ docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/project-test:[镜像版本号]
请根据实际镜像信息替换示例中的[ImageId]和[镜像版本号]参数。

4. 选择合适的镜像仓库地址
从ECS推送镜像时，可以选择使用镜像仓库内网地址。推送速度将得到提升并且将不会损耗您的公网流量。

如果您使用的机器位于VPC网络，请使用 registry-vpc.cn-hangzhou.aliyuncs.com 作为Registry的域名登录。

5. 示例
使用"docker tag"命令重命名镜像，并将它通过专有网络地址推送至Registry。

$ docker images
REPOSITORY                                                         TAG                 IMAGE ID            CREATED             VIRTUAL SIZE
registry.aliyuncs.com/acs/agent                                    0.7-dfb6816         37bb9c63c8b2        7 days ago          37.89 MB
$ docker tag 37bb9c63c8b2 registry-vpc.cn-hangzhou.aliyuncs.com/acs/agent:0.7-dfb6816
使用 "docker push" 命令将该镜像推送至远程。

$ docker push registry-vpc.cn-hangzhou.aliyuncs.com/acs/agent:0.7-dfb6816
```



在`仓库管理-访问凭证`可以看到想要给这个仓库上传镜像前，需要按照阿里云的要求通过指令进行登录，执行：

```bash
sudo docker login --username=<your-account-email> registry.cn-hangzhou.aliyuncs.com
```

如：

```bash
sudo docker login --username=project-test@qq.com registry.cn-hangzhou.aliyuncs.com
```

需要输入刚才设置的密码，执行结果如下：

```bash
root@k8s-master01:~/ruoyi-output# docker login --username=****@****.com registry.cn-hangzhou.aliyuncs.com
Password:
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
```

登录完成后可以开始推送镜像，执行：

查看镜像信息，获取镜像id

```bash
docker images
REPOSITORY             TAG       IMAGE ID       CREATED              SIZE
ruoyi-visual-monitor   v1.0      11e17c88495f   8 seconds ago        592MB
ruoyi-modules-system   v1.0      542aa00e1e99   25 seconds ago       632MB
ruoyi-modules-job      v1.0      85bb3c36229f   42 seconds ago       629MB
ruoyi-modules-gen      v1.0      d480eff61241   59 seconds ago       628MB
ruoyi-modules-file     v1.0      3aa9e76e1f7c   About a minute ago   622MB
ruoyi-gateway          v1.0      64bed895df33   About a minute ago   625MB
ruoyi-auth             v1.0      baf4d08bf774   5 minutes ago        617MB
```

重新打标签：，要加上阿里云的前缀+命名空间前缀，注意把`<namespace>` 替换成实际的命名空间

```bash
docker tag <image-id> <image-server-domain>/<namespace>/<image-name>:<version>
```

执行：

```bash
docker tag 11e17c88495f registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-visual-monitor:v1.0
docker tag 542aa00e1e99 registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-modules-system:v1.0
docker tag 85bb3c36229f registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-modules-job:v1.0
docker tag d480eff61241 registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-modules-gen:v1.0
docker tag 3aa9e76e1f7c registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-modules-file:v1.0
docker tag 64bed895df33 registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-gateway:v1.0
docker tag baf4d08bf774 registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-auth:v1.0
```

推送：注意把`<namespace>` 替换成实际的命名空间

```bash
docker push <image-server-domain>/<namespace>/<image-name>:<version>
```

执行：

```bash
docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-visual-monitor:v1.0
docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-modules-system:v1.0
docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-modules-job:v1.0
docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-modules-gen:v1.0
docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-modules-file:v1.0
docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-gateway:v1.0
docker push registry.cn-hangzhou.aliyuncs.com/<namespace>/ruoyi-auth:v1.0
```

执行完成后可以在仓库管理-镜像仓库中看到上传的各个镜像，每一个镜像都是由单独的镜像仓库保存。



整个过程可以通过一个脚本执行

```bash
vi build_and_push_images.sh 
```

内容如下：注意把脚本的`namespace`的值`xxxx`替换为阿里云中设置的`namespace`值

```bash
#!/bin/bash

# 检查是否传递了version参数，如果没有则默认为1.0.0
if [ -z "$1" ]; then
    version="1.0.0"
else
    version="$1"
fi

# 使用变量version替换Docker命令中的版本号，并执行相应的操作
echo version: "$version"

image_server = "registry.cn-hangzhou.aliyuncs.com"
namespace="xxxx"
work_directory="ruoyi-output"

cd "$work_directory"

for module in ruoyi-auth ruoyi-gateway ruoyi-modules-file ruoyi-modules-gen ruoyi-modules-job ruoyi-modules-system ruoyi-visual-monitor
do
	# 构建镜像
	echo start build image "$module"
	cd "$module"
	docker build -t "$module":"$version" -f Dockerfile .
	cd ..
	echo build image "$module" finshed
	
	# 重新打tag
	echo start retag image "$module"
	docker tag "$module":"$version" "$image_server"/"$namespace"/"$module":"$version"
	echo retag image "$module" finshed

	# 推送镜像
	echo start push image "$module"
	docker push "$image_server"/"$namespace"/"$module":"$version"
	echo push image "$module" finshed
done

echo done
```

也可以直接打包成阿里云镜像，跳过重新打tag：

```bash
#!/bin/bash

# 检查是否传递了version参数，如果没有则默认为1.0.0
if [ -z "$1" ]; then
    version="1.0.0"
else
    version="$1"
fi

# 使用变量version替换Docker命令中的版本号，并执行相应的操作
echo version: "$version"

image_server = "registry.cn-hangzhou.aliyuncs.com"
namespace="xxxx"
work_directory="ruoyi-output"

cd "$work_directory"

for module in ruoyi-auth ruoyi-gateway ruoyi-modules-file ruoyi-modules-gen ruoyi-modules-job ruoyi-modules-system ruoyi-visual-monitor
do
	# 构建镜像
	echo start build image "$module"
	cd "$module"
	docker build -t "$image_server"/"$namespace"/"$module":"$version" -f Dockerfile .
	cd ..
	echo build image "$module" finshed
	
	# 推送镜像
	echo start push image "$module"
	docker push "$image_server"/"$namespace"/"$module":"$version"
	echo push image "$module" finshed
done

echo done
```

保存退出后执行：

```bash
chmod +x build_and_push_images.sh
./build_and_push_images.sh 1.0.0
```

脚本的参数为打包镜像的版本，不传递默认为 `1.0.0` 可以根据实际需要修改，如 `./build_and_push_images.sh v1.0` `./build_and_push_images.sh 2.0`等，优化点：这里还可以优化，将dockerfile中的nacos的相关配置设置为镜像的环境变量，在KubeSphere创建部署的时候设置环境变量的方式传递nacos的相关配置。



在KubeSphere记录阿里云镜像仓库的账号密码：

在KubeSphere平台，进入项目，在`配置-保密字典`中点击`创建`，输入基本信息：

```bash
名称：aliyun-image-registry-secret
别名：aliyun-docker-image-registry-secret
描述：阿里云镜像仓库密码
```

点击`下一步`，进入`数据设置`：

选择`类型`为：`镜像服务信息`，输入信息：用户名，邮箱，密码要替换为阿里云镜像服务的信息。

```bash
镜像服务地址：registry.cn-hangzhou.aliyuncs.com
用户名：xxx
邮箱：xxx@xxx.com
密码：xxxxx
```

点击密码框右边的`验证`，验证通过后点击`创建`





##### 2.2.5 应用部署

###### 1 修改配置文件

通过KubeSphere部署应用到k8s，按照自底向上的原则，先部署微服务，再部署网关，最后部署ui前端项目。在部署微服务前，要先将线上k8s中部署的nacos中的各个微服务的prod环境配置进行修改。将配置文件中关于 mysql、redis、es等中间件的地址、账号、密码等信息修改为实际项目中线上环境的值。中间件的地址使用各个中间件的headless服务的dns。注意端口要使用内部service的端口，而不是用于外部访问的NodePort的随机`3****`端口。

这里要修改的中间件有：

mysql

```yaml
mysql-standalone-z28c.project-test

# spring配置
spring:
  datasource:
    dynamic:
      datasource:
          master:
            driver-class-name: com.mysql.cj.jdbc.Driver
            url: jdbc:mysql://mysql-standalone-z28c.project-test:3306/ry-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
            username: root
            password: 123456
```

需要修改相关中间件配置的配置文件有

```bash
ruoyi-system-prod.yml
ruoyi-gen-prod.yml
ruoyi-job-prod.yml
```



redis

```yaml
spring:
  redis:
    host: redis-standalone-mp59.project-test
    port: 6379
    password: 123456
```

需要修改相关中间件配置的配置文件有：

```bash
ruoyi-gateway-prod.yml  
ruoyi-auth-prod.yml  
ruoyi-system-prod.yml    
ruoyi-gen-prod.yml   
ruoyi-job-prod.yml
```



###### 2 部署微服务

例如部署`ruoyi-visual-monitor`， 在 `应用负载-服务` 创建 `无状态服务` 

基本信息：

```bash
名称：ruoyi-visual-monitor
别名：project-test-ruoyi-visual-monitor
版本：v1
描述信息：RuoYi-Cloud 监控系统
```

点击`下一步`



容器设置：

​	服务器：由默认的dockerhub改为使用阿里云镜像服务地址 https://registry.cn-hangzhou.aliyuncs.com

​	镜像：xxxx/ruoyi-visual-monitor:1.0.0，不需要服务器地址，格式为：`<namesapce>/<image-name>:<image-version>`

使用默认端口

资源限制：cpu不限制，内存：2048Mi， 这里要注意内存不能太小，否则容易产生OOM

勾选：同步主机时区

点击`√`点击`下一步`



挂载存储：跳过

点击`下一步`



高级设置：跳过，注意**不要勾选**外网访问

点击`创建`



其他各个微服务都是一样的流程，各个微服务部署的信息如下：

| 名称                 | 别名                              | 版本 | 描述信息                 | 容器（使用阿里云镜像仓库）      | 内存(Mi) |
| -------------------- | --------------------------------- | ---- | ------------------------ | ------------------------------- | -------- |
| ruoyi-visual-monitor | project-test-ruoyi-visual-monitor | v1   | RuoYi-Cloud 监控系统     | xxxx/ruoyi-visual-monitor:1.0.0 | 2048     |
| ruoyi-auth           | project-test-ruoyi-auth           | v1   | RuoYi-Cloud 认证模块     | xxxx/ruoyi-auth:1.0.0           | 2048     |
| ruoyi-modules-file   | project-test-ruoyi-modules-file   | v1   | RuoYi-Cloud 文件存储模块 | xxxx/ruoyi-modules-file:1.0.0   | 2048     |
| ruoyi-modules-gen    | project-test-ruoyi-modules-gen    | v1   | RuoYi-Cloud 代码生成模块 | xxxx/ruoyi-modules-gen:1.0.0    | 2048     |
| ruoyi-modules-job    | project-test-ruoyi-modules-job    | v1   | RuoYi-Cloud 定时任务模块 | xxxx/ruoyi-modules-job:1.0.0    | 2048     |
| ruoyi-modules-system | project-test-ruoyi-modules-system | v1   | RuoYi-Cloud 系统模板     | xxxx/ruoyi-modules-system:1.0.0 | 2048     |
| ruoyi-gateway        | project-test-ruoyi-gateway        | v1   | RuoYi-Cloud 网关         | xxxx/ruoyi-gateway:1.0.0        | 2048     |



其中部署网关项目的时候要注意在网关项目的 `bootstrap.yml` 文件中有 `sentinel` 相关的配置，其中设置了数据存储到 `nacos`，所以需要在 nacos 中的 `ruoyi-gateway-prod.yml` 覆盖这部分配置：

`bootstrap.yml` 中的配置：

```yaml
spring:
  cloud:
    sentinel:
      datasource:
        ds1:
          nacos:
            server-addr: 127.0.0.1:8848
            dataId: sentinel-ruoyi-gateway
            groupId: DEFAULT_GROUP
            data-type: json
            rule-type: gw-flow
```

复制到线上 `nacos`中的 `ruoyi-gateway-prod.yml` 并修改为：

```yaml
spring:
  cloud:
    sentinel:
      # 取消控制台懒加载
      eager: true
      transport:
        # 控制台地址
        dashboard: 127.0.0.1:8718
      # nacos配置持久化
      datasource:
        ds1:
          nacos:
            server-addr: nacos-cluster.project-test:8848
            namespace: b304af82-49b7-47d9-b4b5-0a6153efa584
            dataId: sentinel-ruoyi-gateway
            groupId: DEFAULT_GROUP
            data-type: json
            rule-type: gw-flow
```

配置了线上的nacos的headless服务的dns和prod环境的namespace

```yaml
server-addr: nacos-cluster.project-test:8848
namespace: b304af82-49b7-47d9-b4b5-0a6153efa584
```



注意：这里网关项目不需要暴露外部端口，是由于后面要部署前端项目，前端项目本身是一个nginx服务，只需要暴露前端项目给外部访问，也就是暴露这个nginx服务在外面即可，用户的流量先访问这个nginx，再由nginx根据url匹配的方式将需要访问后端的请求转发给后端的gateway，再由gateway转给微服务。





#### 2.3 部署前端项目

ui项目时使用vue.js，部署方式与java实现的微服务不一样，但是也是分为同样的几步：

1 制作制品包

2 上传到服务器

3 制作镜像

4 推送到镜像仓库

5 线上部署



##### 2.3.1 制作制品包

打包前需要修改一下前端请求的url，在 `vue.config.js` 文件中有服务器地址相关配置：

```javascript
  // webpack-dev-server 相关配置
  devServer: {
    host: '0.0.0.0',
    port: port,
    open: true,
    proxy: {
      // detail: https://cli.vuejs.org/config/#devserver-proxy
      [process.env.VUE_APP_BASE_API]: {
        target: `http://localhost:8080`,
        changeOrigin: true,
        pathRewrite: {
          ['^' + process.env.VUE_APP_BASE_API]: ''
        }
      }
    },
    disableHostCheck: true
  }
```

将其中的

```javascript
target: `http://localhost:8080`,
```

域名修改线上 `gateway` 服务的headless服务的dns：

```javascript
target: `http://ruoyi-gateway.project-test:8080`,
```



在 `RuoYi-Cloud\ruoyi-ui` 目录下执行：

```bash
npm run build:prod
```

打包成功后会在 `RuoYi-Cloud\ruoyi-ui` 目录下输出一个 `dist` 文件夹，这个目录就是前端项目的制品包。把这个包交给nginx就可以执行了。下面就需要写好一份nginx配置文件，给后面的nginx运行环境做配置。在 `RuoYi-Cloud\docker\nginx` 目录下存放着nginx的配置文件模板。nginx目录结构为：

```bash
 ───nginx
    │   dockerfile
    │
    ├───conf
    │       nginx.conf
    │
    └───html
        └───dist
                readme.txt
```

其中 `/nginx/html/dist` 用于存放ui项目的制品包，把ui项目打包的`dist`覆盖这个 `/nginx/html/dist` 目录即可。`/nginx/conf/nginx.conf` 是nginx的配置文件，需要进行修改，原始的配置文件：

```nginx
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  65;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root   /home/ruoyi/projects/ruoyi-ui;
            try_files $uri $uri/ /index.html;
            index  index.html index.htm;
        }

        location /prod-api/{
            proxy_set_header Host $http_host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header REMOTE-HOST $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_pass http://ruoyi-gateway:8080/;
        }

        # 避免actuator暴露
        if ($request_uri ~ "/actuator") {
            return 403;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```

其中：

```nginx
server {
        listen       80;
        server_name  localhost;
   		...
}
```

表式只监听domain为localhost的请求，在线上要修改为 _ ,表示监听任意请求，有了域名后也可以修改为项目的域名，修改后如下：

```nginx
server {
        listen       80;
        server_name  _;
   		...
}
```



生产环境下ui页面的所有的请求都会以 `/prod-api/`开头，这些请求都会被下面的配置匹配：

```nginx
location /prod-api/{
            proxy_set_header Host $http_host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header REMOTE-HOST $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_pass http://ruoyi-gateway:8080/;
}
```

其中代理配置为：

```bash
proxy_pass http://ruoyi-gateway:8080/;
```

这里要改为项目实际的gateway的dns，修改如下：

```nginx
location /prod-api/{
            proxy_set_header Host $http_host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header REMOTE-HOST $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_pass http://ruoyi-gateway.project-test:8080/;
}
```



修改后的完整配置文件：

```nginx
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  65;

    server {
        listen       80;
        server_name  _;

        location / {
            root   /home/ruoyi/projects/ruoyi-ui;
            try_files $uri $uri/ /index.html;
            index  index.html index.htm;
        }

        location /prod-api/{
            proxy_set_header Host $http_host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header REMOTE-HOST $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_pass http://ruoyi-gateway.project-test:8080/;
        }

        # 避免actuator暴露
        if ($request_uri ~ "/actuator") {
            return 403;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```





##### 2.3.2 上传到服务器

将dist目录添加到nginx配置模板并且修改了nginx配置文件后，将nginx目录压缩成压缩包 `ruoyi-ui.zip` 上传至服务器。



##### 2.3.3 制作镜像

在服务器上执行：

安装解压工具：

```bash
apt install -y unzip
```

解压：

```bash
unzip ruoyi-ui.zip
```

制作镜像：

```bash
cd ruoyi-ui
docker build -t ruoyi-ui:1.0.0 -f dockerfile .
docker tag ruoyi-ui:1.0.0 registry.cn-hangzhou.aliyuncs.com/leixing/ruoyi-ui:1.0.0
cd ..
```

或者直接制作阿里云的镜像包：

```bash
cd ruoyi-ui
docker build -t registry.cn-hangzhou.aliyuncs.com/leixing/ruoyi-ui:1.0.0 .
cd ..
```



##### 2.3.4 推送到镜像仓库

服务器上执行：

```bash
docker push registry.cn-hangzhou.aliyuncs.com/leixing/ruoyi-ui:1.0.0
```



##### 2.3.5 线上部署

部署方式与java微服务是一样的流程，部署配置如下：

| 名称     | 别名                  | 版本 | 描述信息             | 容器（使用阿里云镜像仓库） | 内存(Mi) |
| -------- | --------------------- | ---- | -------------------- | -------------------------- | -------- |
| ruoyi-ui | project-test-ruoyi-ui | v1   | RuoYi-Cloud 前端页面 | xxxx/ruoyi-ui:1.0.0        | 2048     |

注意`ruoyi-ui`项目需要暴露端口，在 `高级设置` 中要勾选 `外部访问` 访问模式选择 `NodePort`。这里项目的压缩包/解压后的文件夹名为模块名/镜像名可以将解压后的文件夹放在 /ruoyi-output 内，再将前面的一键打包推送脚本修改即可所有的模块一起打包推送：

```bash
#!/bin/bash

# 检查是否传递了version参数，如果没有则默认为1.0.0
if [ -z "$1" ]; then
    version="1.0.0"
else
    version="$1"
fi

# 使用变量version替换Docker命令中的版本号，并执行相应的操作
echo version: "$version"

image_server = "registry.cn-hangzhou.aliyuncs.com"
namespace="xxxx"
work_directory="ruoyi-output"

cd "$work_directory"

for module in ruoyi-auth ruoyi-gateway ruoyi-modules-file ruoyi-modules-gen ruoyi-modules-job ruoyi-modules-system ruoyi-visual-monitor ruoyi-ui
do
	# 构建镜像
	echo start build image "$module"
	cd "$module"
	docker build -t "$module":"$version" -f Dockerfile .
	cd ..
	echo build image "$module" finshed
	
	# 重新打tag
	echo start retag image "$module"
	docker tag "$module":"$version" "$image_server"/"$namespace"/"$module":"$version"
	echo retag image "$module" finshed

	# 推送镜像
	echo start push image "$module"
	docker push "$image_server"/"$namespace"/"$module":"$version"
	echo push image "$module" finshed
done

echo done
```

也可以直接打包成阿里云镜像，跳过重新打tag：

```bash
#!/bin/bash

# 检查是否传递了version参数，如果没有则默认为1.0.0
if [ -z "$1" ]; then
    version="1.0.0"
else
    version="$1"
fi

# 使用变量version替换Docker命令中的版本号，并执行相应的操作
echo version: "$version"

image_server = "registry.cn-hangzhou.aliyuncs.com"
namespace="xxxx"
work_directory="ruoyi-output"

cd "$work_directory"

for module in ruoyi-auth ruoyi-gateway ruoyi-modules-file ruoyi-modules-gen ruoyi-modules-job ruoyi-modules-system ruoyi-visual-monitor ruoyi-ui
do
	# 构建镜像
	echo start build image "$module"
	cd "$module"
	docker build -t "$image_server"/"$namespace"/"$module":"$version" -f Dockerfile .
	cd ..
	echo build image "$module" finshed
	
	# 推送镜像
	echo start push image "$module"
	docker push "$image_server"/"$namespace"/"$module":"$version"
	echo push image "$module" finshed
done

echo done
```



修改完成后，下次上传`ruoyi-ui`后执行：

```bash
unzip ruoyi-ui.zip
mv ruoyi-ui ./ruoyi-output/ruoyi-ui
./build_and_push_images.sh 1.0.1
```

或则不用改压缩包的名字，直接制作压缩文件为 nginx.zip，上传到服务器上后执行

```bash
unzip nginx.zip
mv nginx ./ruoyi-output/ruoyi-ui
./build_and_push_images.sh 1.0.1
```

这里版本号可以根据实际情况进行修改。

