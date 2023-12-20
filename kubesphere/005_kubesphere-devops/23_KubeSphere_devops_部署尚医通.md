## KubeSphere-devops 部署尚医通

[toc]

尚医通项目是一个在线预约挂号的系统，项目架构如下：

![page_1 - 副本.png](assets/1632189649799-e486abb0-5195-4282-aef0-c8a3692c0054.png)

模块说明：

```bash
yygh-parent
|---common                                  						//通用模块
|---hospital-manage                         						//医院后台				[9999]   
|---model															//数据模型
|---server-gateway													//网关    		     [80]
|---service															//微服务层
|-------service-cmn													//公共服务				[8202]
|-------service-hosp												//医院数据服务		   [8201]
|-------service-order												//预约下单服务		   [8206]
|-------service-oss													//对象存储服务		   [8205]
|-------service-sms													//短信服务				[8204]
|-------service-statistics											//统计服务				[8208]
|-------service-task												//定时服务				[8207]
|-------service-user												//会员服务				[8203]


====================================================================

yygh-admin																	//医院管理后台		[9528]
yygh-site																		//挂号平台				[3000]
```



尚医通项目地址：



后端：

https://gitee.com/leifengyang/yygh-parent



管理平台ui

https://gitee.com/leifengyang/yygh-admin



用户端ui

https://gitee.com/leifengyang/yygh-site



由于后面需要修改源码中的部分配置，首先将这三个项目fork到自己的代码仓库。

下载自己代码仓库中的项目源码：

```bash
git clone git@gitee.com:xxxx/yygh-parent.git
git clone git@gitee.com:xxxx/yygh-admin.git
git clone git@gitee.com:xxxx/yygh-site.git
```



项目部署

### 1 部署中间件

中间件信息统计表：

| 中间件        | 集群内地址                                 | 外部访问地址              | 账号/密码         | 已确认 |
| ------------- | ------------------------------------------ | ------------------------- | ----------------- | ------ |
| nacos         | nacos-standalone.project-test:8848         | 192.168.0.112:32555/nacos | nacos/nacos       | Y      |
| mysql         | mysql-standalone.project-test:3306         | 192.168.0.112:30131       | root/123456       | Y      |
| redis         | redis-standalone.project-test:6379         | 192.168.0.112:32568       | - /123456         | Y      |
| sentinel      | sentinel-standalone.project-test:8080      | 192.168.0.112:31952       | sentinel/sentinel | Y      |
| mongodb       | mongodb-standalone.project-test:27017      | 192.168.0.112:32631       | root/123456       | Y      |
| rabbitmq      | rabbitmq-standalone.project-test:5672      | 192.168.0.112:31294       | admin/admin       | Y      |
| elasticsearch | elasticsearch-standalone.project-test:9200 | 192.168.0.112:32130       |                   | Y      |

注意：

1 如果存在自动生成的service，导致服务名带有随机后缀的可以单独删除该service重新创建。 

2 外部访问地址的端口 3xxxx 是随机产生的NodePort端口，这里要根据实际情况做记录。

3 注意服务之间的依赖关系，比如mysql的访问地址变化了，nacos中的mysql配置也要一起修改。



#### 1.1 部署sentinel

创建有状态服务， 需要使用 dockerhub上的制作好的镜像  https://hub.docker.com/r/leifengyang/sentinel  leifengyang/sentinel:1.8.2 默认端口是8080，选择同步主机时区。

创建单独的服务用于外部访问-指定工作负载-NodePort，端口 HTTP-8080



#### 1.2 部署mongodb

应用负载-应用-基于模板的应用-创建-从应用模板-应用仓库：bitnami - 搜索：mongodb-点击mongodb - 安装

基本信息：

​	名称： mongodb-standalone

​	版本： 13.15.4[6.0.7] 最新版本

​	描述：测试项目的mongodb单节点服务

应用配置：

MongoDB&reg; architecture：	standalone

MongoDB&reg; admin user： root

MongoDB&reg; admin password： 123456

pv-size 8GB

点击 `安装`



外部访问：

应用负载-服务-创建-指定工作负载

```
名称：mongodb-standalone-service
别名：project-test-mongodb-standalone-service
描述：测试项目的mongodb的单节点服务
```

内部访问方式：虚拟IP地址

指定工作负载： mongodb-standalone

勾选`外部访问模式`，访问模式：NodePort

点击`创建`





### 2 初始化数据

#### 2.1 mysql

连接线上的mysql，运行/data/sql 目录下的SQL脚本



#### 2.2 mongo

1 安装mongosh 

https://www.mongodb.com/try/download/shell ，

https://downloads.mongodb.com/compass/mongosh-1.10.1-win32-x64.zip

下载后将 bin 目录配置到系统 path



2 登录mongodb

```bash
mongosh --username mongo --password 123456 --host 192.168.0.112 --port 32631 --authenticationDatabase admin
```



3 创建数据库

```bash
use yygh_hosps
```



4 创建用户

再给数据库创建一个用户，授予权限 数据库管理、读写、用户管理权限（可选）。

```bash
db.createUser({user:"root", pwd:"123456", roles:[{role:"dbAdmin",db:"yygh_hosps"},{role:'readWrite', db:'yygh_hosps'}, {role:'userAdmin', db:'yygh_hosps'}]})
```



5 安装 MongoDB Command Line Database Tools

https://www.mongodb.com/compatibility/json-to-mongodb

下载 MongoDB Command Line Database Tools

https://www.mongodb.com/try/download/database-tools

https://fastdl.mongodb.org/tools/db/mongodb-database-tools-windows-x86_64-100.7.3.zip

下载后将 bin 目录添加到系统path



6 导入数据

数据保存在 /data/json 目录下，在项目的根目录执行执行

```bash
mongoimport --uri mongodb://root:123456@192.168.0.112:32631/yygh_hosps --collection Department --jsonArray data\json\Department.json

mongoimport --uri mongodb://root:123456@192.168.0.112:32631/yygh_hosps --collection Hospital --jsonArray data\json\Hospital.json

mongoimport --uri mongodb://root:123456@192.168.0.112:32631/yygh_hosps --collection Schedule --jsonArray data\json\Schedule.json
```



### 3 生产环境与开发环境的配置隔离

在线上nacos中，创建 `<微服务名>-<环境名>.yml`的配置文件，生产环境的环境名就是prod，如service-cmn-prod.yml

在将项目中的开发环境的配置文件，如 service-cmn项目下的application-dev.yml 的内容复制过去，再将线上的生产环境的配置文件根据实际情况进行修改。要修改的是各个中间件（mysql，redis，sentinel，mongodb ...）的访问地址，账号密码等。

注意：hospital-manage项目是个单体应用，配置文件没有保存到nacos，需要直接在项目中修改 application.prod.yml 文件

**tips**：在idea中可以使用`ctrl+shift+R` 全局替换的方式来进行配置的替换。



修改完成后在nacos的public命名空间下会有一下配置文件：

server-gateway-prod.yml

```yaml
server:
  port: 80
spring:
  application:
    name: server-gateway
  cloud:
    gateway:
      discovery:      #是否与服务发现组件进行结合，通过 serviceId(必须设置成大写) 转发到具体的服务实例。默认为false，设为true便开启通过服务中心的自动根据 serviceId 创建路由的功能。
        locator:      #路由访问方式：http://Gateway_HOST:Gateway_PORT/大写的serviceId/**，其中微服务应用名默认大写访问。
          enabled: true
      routes:
        - id: service-user
          uri: lb://service-user
          predicates:
            - Path=/*/user/**
        - id: service-cmn
          uri: lb://service-cmn
          predicates:
            - Path=/*/cmn/**
        - id: service-sms
          uri: lb://service-sms
          predicates:
            - Path=/*/sms/**
        - id: service-hosp
          uri: lb://service-hosp
          predicates:
            - Path=/*/hosp/**
        - id: service-order
          uri: lb://service-order
          predicates:
            - Path=/*/order/**
        - id: service-statistics
          uri: lb://service-statistics
          predicates:
            - Path=/*/statistics/**
        - id: service-cms
          uri: lb://service-cms
          predicates:
            - Path=/*/cms/**
        - id: service-oss
          uri: lb://service-oss
          predicates:
            - Path=/*/oss/**

```



hospital-manage-prod.yml

```yaml
server:
  port: 8080
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath:mapper/*.xml
spring:
  thymeleaf:
    mode: LEGACYHTML5
    #编码 可不用配置
    encoding: UTF-8
    #开发配置为false,避免修改模板还要重启服务器
    cache: false
    #配置模板路径，默认是templates，可以不用配置
    prefix: classpath:/templates/
  redis:
    host: redis-standalone.project-test
    port: 6379
    database: 0
    timeout: 1800000
    password:
    lettuce:
      pool:
        max-active: 20 #最大连接数
        max-wait: -1    #最大阻塞等待时间(负数表示没限制)
        max-idle: 5    #最大空闲
        min-idle: 0     #最小空闲
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://mysql-standalone.project-test:3306/yygh_manage?characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456
    hikari:
      connection-test-query: SELECT 1
      connection-timeout: 60000
      idle-timeout: 500000
      max-lifetime: 540000
      maximum-pool-size: 12
      minimum-idle: 10
      pool-name: GuliHikariPool
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

```



service-cmn-prod.yml

```yaml
server:
  port: 8202
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath:mapper/*.xml
  global-config:
    db-config:
      logic-delete-value: 1
      logic-not-delete-value: 0
spring:
  cloud:
    sentinel:
      transport:
        dashboard: http://sentinel-standalone.project-test:8080
  redis:
    host: redis-standalone.project-test
    port: 6379
    database: 0
    timeout: 1800000
    password:
    lettuce:
      pool:
        max-active: 20 #最大连接数
        max-wait: -1    #最大阻塞等待时间(负数表示没限制)
        max-idle: 5    #最大空闲
        min-idle: 0     #最小空闲
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://mysql-standalone.project-test:3306/yygh_cmn?characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456
    hikari:
      connection-test-query: SELECT 1
      connection-timeout: 60000
      idle-timeout: 500000
      max-lifetime: 540000
      maximum-pool-size: 12
      minimum-idle: 10
      pool-name: GuliHikariPool
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

```



service-hosp-prod.yml

```yaml
server:
  port: 8201
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath:mapper/*.xml
feign:
  sentinel:
    enabled: true
  client:
    config:
      default:   #配置全局的feign的调用超时时间  如果 有指定的服务配置 默认的配置不会生效
        connectTimeout: 30000 # 指定的是 消费者 连接服务提供者的连接超时时间 是否能连接  单位是毫秒
        readTimeout: 50000  # 指定的是调用服务提供者的 服务 的超时时间（）  单位是毫秒
spring:
  main:
    allow-bean-definition-overriding: true #当遇到同样名字的时候，是否允许覆盖注册
  cloud:
    sentinel:
      transport:
        dashboard: http://sentinel-standalone.project-test:8080
  data:
    mongodb:
      host: mongodb-standalone.project-test
      port: 27017
      database: yygh_hosps #指定操作的数据库
      username: root
      password: 123456
  rabbitmq:
    host: rabbitmq-standalone.project-test
    port: 5672
    username: admin
    password: admin
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://mysql-standalone.project-test:3306/yygh_hosp?characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456
    hikari:
      connection-test-query: SELECT 1
      connection-timeout: 60000
      idle-timeout: 500000
      max-lifetime: 540000
      maximum-pool-size: 12
      minimum-idle: 10
      pool-name: GuliHikariPool
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
  redis:
    host: redis-standalone.project-test

```



service-order-prod.yml

```yaml
server:
  port: 8206
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath:mapper/*.xml
feign:
  sentinel:
    enabled: true
  client:
    config:
      default:   #配置全局的feign的调用超时时间  如果 有指定的服务配置 默认的配置不会生效
        connectTimeout: 30000 # 指定的是 消费者 连接服务提供者的连接超时时间 是否能连接  单位是毫秒
        readTimeout: 50000  # 指定的是调用服务提供者的 服务 的超时时间（）  单位是毫秒
spring:
  main:
    allow-bean-definition-overriding: true #当遇到同样名字的时候，是否允许覆盖注册
  cloud:
    sentinel:
      transport:
        dashboard: http://sentinel-standalone.project-test:8080
  rabbitmq:
    host: rabbitmq-standalone.project-test
    port: 5672
    username: admin
    password: admin
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://mysql-standalone.project-test:3306/yygh_order?characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456
    hikari:
      connection-test-query: SELECT 1
      connection-timeout: 60000
      idle-timeout: 500000
      max-lifetime: 540000
      maximum-pool-size: 12
      minimum-idle: 10
      pool-name: GuliHikariPool
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
  redis:
    host: redis-standalone.project-test
# 微信
#weixin:
#  appid: wx8397f8696b538317
#  partner: 1473426802
#  partnerkey: T6m9iK73b0kn9g5v426MKfHQH7X8rKwb
#  notifyurl: http://a31ef7db.ngrok.io/WeChatPay/WeChatPayNotify
weixin:
  appid: wx74862e0dfcf69954
  partner: 1558950191
  partnerkey: T6m9iK73b0kn9g5v426MKfHQH7X8rKwb
  notifyurl: http://qyben.free.idcfengye.com/api/order/weixin/notify
  cert: C:\Users\lfy\Desktop\yygh-parent\service\service-order\src\main\resources\apiclient_cert.p12

```



service-oss-prod.yml

```yaml
server:
  port: 8205

spring:
  cloud:
    sentinel:
      transport:
        dashboard: http://sentinel-standalone.project-test:8080
#阿里云 OSS
aliyun:
  oss:
    file:
      endpoint: oss-cn-beijing.aliyuncs.com
      keyid: LTAI4FhtGRtRGtPvmLBv8vxk
      keysecret: sq8e8WLYoKwJoCNLbjRdlSTaOaFumD
      #bucket可以在控制台创建，也可以使用java代码创建
      bucketname: online-teach-file

```



service-sms-prod.yml

```yaml
server:
  port: 8204

spring:
  cloud:
    sentinel:
      transport:
        dashboard: http://sentinel-standalone.project-test:8080
  rabbitmq:
    host: rabbitmq-standalone.project-test
    port: 5672
    username: admin
    password: admin
  redis:
    host: redis-standalone.project-test
#阿里云 短信
aliyun:
  sms:
    regionId: default
    accessKeyId: LTAI0YbQf3pX8WqC
    secret: jX8D04DmDI3gGKjW5kaFYSzugfqmmT

```



service-statistics-prod.yml

```yaml
server:
  port: 8208
feign:
  sentinel:
    enabled: true
  client:
    config:
      default:   #配置全局的feign的调用超时时间  如果 有指定的服务配置 默认的配置不会生效
        connectTimeout: 30000 # 指定的是 消费者 连接服务提供者的连接超时时间 是否能连接  单位是毫秒
        readTimeout: 50000  # 指定的是调用服务提供者的 服务 的超时时间（）  单位是毫秒
spring:
  main:
    allow-bean-definition-overriding: true #当遇到同样名字的时候，是否允许覆盖注册
  cloud:
    sentinel:
      transport:
        dashboard: http://sentinel-standalone.project-test:8080

```



service-task-prod.yml

```yaml
server:
  port: 8207
spring:
  cloud:
    sentinel:
      transport:
        dashboard: sentinel-standalone.project-test:8080
  rabbitmq:
    host: rabbitmq-standalone.project-test
    port: 5672
    username: admin
    password: admin

```



service-user-prod.yml

```yaml
server:
  port: 8203
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  mapper-locations: classpath:mapper/*.xml
feign:
  sentinel:
    enabled: true
  client:
    config:
      default:   #配置全局的feign的调用超时时间  如果 有指定的服务配置 默认的配置不会生效
        connectTimeout: 30000 # 指定的是 消费者 连接服务提供者的连接超时时间 是否能连接  单位是毫秒
        readTimeout: 50000  # 指定的是调用服务提供者的 服务 的超时时间（）  单位是毫秒
spring:
  main:
    allow-bean-definition-overriding: true #当遇到同样名字的时候，是否允许覆盖注册
  cloud:
    sentinel:
      transport:
        dashboard: http://sentinel-standalone.project-test:8080
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://mysql-standalone.project-test:3306/yygh_user?characterEncoding=utf-8&useSSL=false
    username: root
    password: 123456
    hikari:
      connection-test-query: SELECT 1
      connection-timeout: 60000
      idle-timeout: 500000
      max-lifetime: 540000
      maximum-pool-size: 12
      minimum-idle: 10
      pool-name: GuliHikariPool
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
wx:
  open:
    # 微信开放平台 appid
    app_id: wxc606fb748aedee7c
    # 微信开放平台 appsecret
    app_secret: 073e8e1117c1054b14586c8aa922bc9c
    # 微信开放平台 重定向url（需要在微信开放平台配置）
    redirect_url: http://qyben.free.idcfengye.com/api/user/weixin/callback
    #redirect_url: http://qyben.free.idcfengye.com/api/user/weixin/callback
#wx:
#  open:
#    # 微信开放平台 appid
#    app_id: wxed9954c01bb89b47
#    # 微信开放平台 appsecret
#    app_secret: 2cf9a4a81b6151d560e9bbc625c3297b
#    # 微信开放平台 重定向url（需要在微信开放平台配置）
#    redirect_url: http://guli.shop/api/ucenter/wx/callback
yygh:
  #预约挂号平台baserul
  baseUrl: http://localhost:3000

```



### 4 创建devops项目

切换为具有项目管理员权限的账号，如 pm-wang 的账号，在企业空间下，点击 `dev-ops项目`， 点击`创建`

```bash
名称：project-test-devops
别名：project-test-devops@test.com
描述：测试项目的devops流水线
```

回到`devops项目`，可以看到刚创建的devops项目：project-test-devops ，点击项目名进入项目详情。



在`DevOps项目设置-DevOps项目成员`页面中点击`邀请`，邀请开发人员进入项目

 dev-zhao operator

seller-qian viewer



### 5 创建流水线

在传统的项目中，要使用 `jenkins` 自动化部署，需要在项目的根目录放一个 `jenkinsfile` 的文件，里面按照jenkins的要求编码流水线的声明。参考： https://www.jenkins.io/doc/book/pipeline/   

jenkinsfile : 

```bash
pipeline {
    agent any 
    stages {
        stage('Build') { 
            steps {
                // 
            }
        }
        stage('Test') { 
            steps {
                // 
            }
        }
        stage('Deploy') { 
            steps {
                // 
            }
        }
    }
}
```

在KubeShpere中，可以在UI页面中进行操作创建流水线。



切换回dev-zhao账号，进入企业空间，点击左边的`DevOps项目`就可以看到创建好的DevOps项目：project-test-devops。点击项目名称进入项目空间。

在 `流水线`页面点击`创建` ，

基本信息：

```bash
名称：project-test-pipeline
DevOps项目：project-test-devops
描述：测试项目的流水线
代码仓库：
```

点击`下一步`，点击`创建`，创建完后，可以在`流水线`页面看到刚创建好的流水线： `project-test-pipeline`，点击流水线的名字进入流水线详情页。点击`编辑流水线`，选择模板：`持续集成&交付 (CI/CD)` ，点击`下一步`，点击`创建`。这样就进入了流水线的可视化编辑页面。点击`确定`。回到流水线详情页，点击 `编辑Jenkinsfile`，就可以看到生成的Jenkinsfile文件的内容了。

jenkinsfile:

```bash
pipeline {
  agent {
    node {
      label 'maven'
    }

  }
  stages {
    stage('clone code') {
      steps {
        container('maven') {
          checkout([$class: 'GitSCM', branches: [[name: '*/$BRANCH_NAME']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[url: 'https://github.com/kubesphere/devops-python-sample.git']]])
        }

      }
    }

    stage('unit test') {
      steps {
        container('maven') {
          sh 'mvn clean -o -gs `pwd`/configuration/settings.xml test'
        }

      }
    }

    stage('build & push') {
      steps {
        container('maven') {
          sh 'mvn -o -Dmaven.test.skip=true -gs `pwd`/configuration/settings.xml clean package'
          sh 'docker build -f Dockerfile-online -t $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
          withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
            sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
            sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
          }

        }

      }
    }

    stage('push latest') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
          sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
        }

      }
    }

    stage('deploy to dev') {
      steps {
        container('maven') {
          input(id: 'deploy-to-dev', message: 'deploy to dev?')
          withCredentials([kubeconfigContent(credentialsId : 'KUBECONFIG_CREDENTIAL_ID' ,variable : 'KUBECONFIG_CONFIG' ,)]) {
            sh 'mkdir -p ~/.kube/'
            sh 'echo "$KUBECONFIG_CONFIG" > ~/.kube/config'
            sh 'envsubst < deploy/dev-ol/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

    stage('deploy to production') {
      steps {
        container('maven') {
          input(id: 'deploy-to-production', message: 'deploy to production?')
          withCredentials([kubeconfigContent(credentialsId : 'KUBECONFIG_CREDENTIAL_ID' ,variable : 'KUBECONFIG_CONFIG' ,)]) {
            sh 'mkdir -p ~/.kube/'
            sh 'echo "$KUBECONFIG_CONFIG" > ~/.kube/config'
            sh 'envsubst < deploy/prod-ol/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

  }
  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'docker_username'
    GITHUB_ACCOUNT = 'kubesphere'
    APP_NAME = 'devops-java-sample'
  }
  parameters {
    string(name: 'TAG_NAME', defaultValue: '', description: '')
  }
}
```

可以看到默认的流水线模板定义的过程：

1 拉取源码

2 单元测试

3 构建镜像并推送到镜像仓库

4 将镜像重新打tag为latest版本并推送至镜像仓库

5 部署到开发环境

6 部署到生产环境





### 6 编辑流水线

在流水线详情页，点击`编辑流水线`。点击空白处可以看到右侧会有显示：`代理`

代理（Agent）：就是要用到什么功能，就使用Jenkins的什么代理。常用的代理 base go maven nodejs，如果是要打包构建java项目，可以选择maven，如果是前端项目，则需要选择nodejs。go语言的项目选择go，其他任意项目可以选择base

参考：

KubeSphere文档：

 https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-use/pipelines/choose-jenkins-agent/



Jenkins官方文档：

https://www.jenkins.io/zh/doc/book/pipeline/syntax/#%e4%bb%a3%e7%90%86



### podTemplate base

| 名称               | 类型 / 版本                               |
| :----------------- | :---------------------------------------- |
| Jenkins Agent 标签 | base                                      |
| 容器名称           | base                                      |
| 操作系统           | centos-7                                  |
| Docker             | 18.06.0                                   |
| Helm               | 2.11.0                                    |
| Kubectl            | 稳定版                                    |
| 内置工具           | unzip、which、make、wget、zip、bzip2、git |

### podTemplate nodejs

| 名称               | 类型 / 版本                               |
| :----------------- | :---------------------------------------- |
| Jenkins Agent 标签 | nodejs                                    |
| 容器名称           | nodejs                                    |
| 操作系统           | centos-7                                  |
| Node               | 9.11.2                                    |
| Yarn               | 1.3.2                                     |
| Docker             | 18.06.0                                   |
| Helm               | 2.11.0                                    |
| Kubectl            | 稳定版                                    |
| 内置工具           | unzip、which、make、wget、zip、bzip2、git |

### podTemplate maven

| 名称               | 类型 / 版本                               |
| :----------------- | :---------------------------------------- |
| Jenkins Agent 标签 | maven                                     |
| 容器名称           | maven                                     |
| 操作系统           | centos-7                                  |
| Jdk                | openjdk-1.8.0                             |
| Maven              | 3.5.3                                     |
| Docker             | 18.06.0                                   |
| Helm               | 2.11.0                                    |
| Kubectl            | 稳定版                                    |
| 内置工具           | unzip、which、make、wget、zip、bzip2、git |

### podTemplate go

| 名称               | 类型 / 版本                               |
| :----------------- | :---------------------------------------- |
| Jenkins Agent 标签 | go                                        |
| 容器名称           | go                                        |
| 操作系统           | centos-7                                  |
| Go                 | 1.11                                      |
| GOPATH             | /home/jenkins/go                          |
| GOROOT             | /usr/local/go                             |
| Docker             | 18.06.0                                   |
| Helm               | 2.11.0                                    |
| Kubectl            | 稳定版                                    |
| 内置工具           | unzip、which、make、wget、zip、bzip2、git |



可以选择全局的agent，可以为每一步单独再指定agent，这里默认选择maven， 

```bash
类型：node
label：maven
```



要使用maven，需要先确认各个节点上是否下载好了镜像，各个node上执行：

```bash
nerdctl pull kubesphere/builder-maven:v3.2.0-podman
nerdctl pull jenkins/inbound-agent:4.10-2
```

这两个镜像确认下载完成后再使用maven作为agent。





#### 6.1 拉取源码

点击 clone code 卡片，在右侧编辑

删除默认添加的任务。

条件：下载源码没有前置条件。

任务：执行操作需要在一个容器中运行，点击`添加步骤`，第一个步骤一般都是要指定一个容器，因为后续的步骤都是要在一个容器中执行的，这里点击 `指定容器`，要在这个容器中执行后续的任务，后续的步骤要点击添加`嵌套步骤`，点击 `git 通过git拉取代码`，在输入git信息的页面，先创建访问代码仓库（这里是gitee）的凭证，点击`创建凭证`，输入信息：

```bash
名称：gitee-xxx
类型：用户名和密码
用户名：xxx@xx.com
密码/令牌：*********
描述：gitee账号和密码
```

点击确定，在git信息页面输入自己代码仓库的git地址：

```bash
Url： https://gitee.com/xxxx/yygh-parent.git
凭证名称：gitee-xxx
分支：master
```

注意：这里url不能为： `git@gitee.xxxx/yygh-parent.git`



为了验证代码是否拉取成功，可以在拉取代码后，在执行shell指令，显示一下，这里要点击`添加嵌套步骤`，选择 `shell` ，在弹窗中输入

```bash
ls -al
```

点击`确定`。



点击`确定`，完成第一步的编辑，再点击`确定`，保存整个流水线。回到流水线详情页。点击`执行`，此时在流水线详情页-`运行记录`中可以看到每一次流水线`运行记录`。点击运行记录，进入运行记录详情页，可以看到图形化的运行记录，点击`查看日志`，可以看到流水线运行的日志。



#### 6.2 单元测试

这里暂时跳过，修改如下：

名称：单元测试

代理：none （使用默认的代理）



把默认的任务删除，只添加一个shell脚本任务：

添加步骤：指定容器：maven

添加嵌套步骤：shell脚本

```bash
ls -al
```

后续再改为实际的单元测试任务。



#### 6.3 源码编译

##### 添加maven镜像服务

源码编译真正要使用到maven命令了，在使用maven之前，需要修改一下maven的配置文件，添加镜像加速服务。使用**系统管理员账号admin**登录，

点击左上角 平台管理-集群管理-配置-配置字典，在ConfigMap配置列表中找到 ks-devops-agent 。

或者进入企业空间：`system-workspace`，点击`项目-kubesphere-devops-worker-配置-配置字典`，在列表中可以看到 `ks-devops-agent` 。

点击 ks-devops-agent 进入配置详情页，`更多操作-编辑设置`，点击`MavenSetting` 右侧的编辑图标，修改maven配置中的

```xml
<mirrors>
</mirrors>
```

添加阿里云

```xml
<mirrors>
  <mirror>
    <id>aliyunmaven</id>
    <mirrorOf>*</mirrorOf>
    <name>阿里云公共仓库</name>
    <url>https://maven.aliyun.com/repository/public</url>
  </mirror>
</mirrors>
```



##### 添加stage

切换回dev-zhao账号，继续编辑流水线。

点击单元测试后的 `+` ，添加一个stage，填写信息：

名称：源码编译

代理：none

条件：无

任务：

​	指定容器：maven

​	嵌套任务：

​		shell：

```bash
mvn -Dmaven.test.skip=true clean package
```

为了观察打包结果，再添加一个shell命令：

```bash
ls -al
```

运行流水线，第一次编辑指令时间为2.36min，再次执行，第二次编译使用了16s，从这里可以看出k8s集群内部对maven有缓存机制。



#### 6.4 制作镜像

接下来要为每一个微服务构建镜像，这些构建任务相互之间是没有依赖关系的，可以并行执行，在 `build&push` 卡片下面点击`添加并行阶段`，这样会在下面再创建一个新的卡片，表示一个并行的stage。这里先点击新创建的并行stage的卡片，点击右上角的删除按钮将这个stage删除。先完成一个stage再重新并行stage。



点击`build&push`卡片重新编辑，清空之前生成的任务：

名称：制作镜像

代理：none

任务：

​	添加步骤：

​		指定容器：maven

​	添加嵌套步骤：shell

```bash
cd hospital-manage
docker build -t hospital-manage:latest -f Dockerfile .
cd ..
```

这样运行会报错 `docker: command not found`，原因是KubeSphere的maven容器内已经没有docker了，而是使用了podman，所以上面的脚本要修改为：

```bash
cd hospital-manage
podman build -t hospital-manage:latest -f Dockerfile .
cd ..
```

再次执行流水线，可以看到构建镜像完成了。下面要为每一个微服务创建一个并行stage，先创建一个并行stage，只添加一个shell任务执行 `ls`即可。保存流水线配置，回到流水线详情页，点击 `编辑Jenkinsfile`，可以看到现在的Jenkinsfile的内容如下：

```bash
pipeline {
  agent {
    node {
      label 'maven'
    }

  }
  stages {
    stage('拉取代码') {
      agent none
      steps {
        container('maven') {
          git(url: 'https://gitee.com/xxxxxxxxxxx/yygh-parent.git', credentialsId: 'gitee-xxxxxxx', branch: 'master', changelog: true, poll: false)
          sh 'ls -al'
        }
      }
    }

    stage('单元测试') {
      agent none
      steps {
        container('maven') {
          sh 'ls -al'
        }

      }
    }

    stage('源码编译') {
      agent none
      steps {
        container('maven') {
          sh 'mvn -Dmaven.test.skip=true clean package'
          sh 'ls -al'
        }

      }
    }

    stage('default-3') {
      parallel {
        stage('制作镜像') {
          agent none
          steps {
            container('maven') {
              sh '''cd hospital-manage
podman build -t hospital-manage:latest -f Dockerfile .
cd ..'''
            }

          }
        }

        stage('stage-pipiline-test') {
          agent none
          steps {
            sh 'ls'
          }
        }

      }
    }

    stage('push latest') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
          sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
        }

      }
    }

    stage('deploy to dev') {
      steps {
        container('maven') {
          input(id: 'deploy-to-dev', message: 'deploy to dev?')
          withCredentials([kubeconfigContent(credentialsId : 'KUBECONFIG_CREDENTIAL_ID' ,variable : 'KUBECONFIG_CONFIG' ,)]) {
            sh 'mkdir -p ~/.kube/'
            sh 'echo "$KUBECONFIG_CONFIG" > ~/.kube/config'
            sh 'envsubst < deploy/dev-ol/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

    stage('deploy to production') {
      steps {
        container('maven') {
          input(id: 'deploy-to-production', message: 'deploy to production?')
          withCredentials([kubeconfigContent(credentialsId : 'KUBECONFIG_CREDENTIAL_ID' ,variable : 'KUBECONFIG_CONFIG' ,)]) {
            sh 'mkdir -p ~/.kube/'
            sh 'echo "$KUBECONFIG_CONFIG" > ~/.kube/config'
            sh 'envsubst < deploy/prod-ol/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

  }
  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'docker_username'
    GITHUB_ACCOUNT = 'kubesphere'
    APP_NAME = 'devops-java-sample'
  }
  parameters {
    string(name: 'TAG_NAME', defaultValue: '', description: '')
  }
}
```



启动构建镜像的stage配置如下：

```bash
stage('default-3') {
      parallel {
        stage('制作镜像') {
          agent none
          steps {
            container('maven') {
              sh '''cd hospital-manage
podman build -t hospital-manage:latest -f Dockerfile .
cd ..'''
            }

          }
        }

        stage('stage-pipiline-test') {
          agent none
          steps {
            sh 'ls'
          }
        }

      }
    }
```

编辑Jenkinsfile，将配置好的 hospital-manage 模块的制作镜像流程为每一个微服务复制一份再修改，这样比在ui页面中编辑会快很多，修改完成后完整的Jenkinsfile如下：

```bash
pipeline {
  agent {
    node {
      label 'maven'
    }

  }
  stages {
    stage('拉取代码') {
      agent none
      steps {
        container('maven') {
          git(url: 'https://gitee.com/xxxxxxxxxxx/yygh-parent.git', credentialsId: 'gitee-xxxxxxx', branch: 'master', changelog: true, poll: false)
          sh 'ls -al'
        }
      }
    }

    stage('单元测试') {
      agent none
      steps {
        container('maven') {
          sh 'ls -al'
        }

      }
    }

    stage('源码编译') {
      agent none
      steps {
        container('maven') {
          sh 'mvn -Dmaven.test.skip=true clean package'
          sh 'ls -al'
        }

      }
    }

    stage('制作镜像') {
          parallel {
            stage('制作镜像-hospital-manage') {
              agent none
              steps {
                container('maven') {
                  sh '''cd hospital-manage
    podman build -t hospital-manage:latest -f Dockerfile .
    cd ..'''
                }

              }
            }

            parallel {
            stage('制作镜像-server-gateway') {
              agent none
              steps {
                container('maven') {
                  sh '''cd server-gateway
    podman build -t server-gateway:latest -f Dockerfile .
    cd ..'''
                }

              }
            }

            parallel {
            stage('制作镜像-service-cmn') {
              agent none
              steps {
                container('maven') {
                  sh '''cd service/service-cmn
    podman build -t service-cmn:latest -f Dockerfile .
    cd ../..'''
                }

              }
            }

            parallel {
            stage('制作镜像-service-hosp') {
              agent none
              steps {
                container('maven') {
                  sh '''cd service/service-hosp
    podman build -t service-hosp:latest -f Dockerfile .
    cd ../..'''
                }

              }
            }

            parallel {
            stage('制作镜像-service-order') {
              agent none
              steps {
                container('maven') {
                  sh '''cd service/service-order
    podman build -t service-order:latest -f Dockerfile .
    cd ../..'''
                }

              }
            }

            parallel {
            stage('制作镜像-service-oss') {
              agent none
              steps {
                container('maven') {
                  sh '''cd service/service-oss
    podman build -t service-oss:latest -f Dockerfile .
    cd ../..'''
                }

              }
            }

            parallel {
            stage('制作镜像-service-sms') {
              agent none
              steps {
                container('maven') {
                  sh '''cd service/service-sms
    podman build -t service-sms:latest -f Dockerfile .
    cd ../..'''
                }

              }
            }

            parallel {
            stage('制作镜像-service-statistics') {
              agent none
              steps {
                container('maven') {
                  sh '''cd service/service-statistics
    podman build -t service-statistics:latest -f Dockerfile .
    cd ../..'''
                }

              }
            }

            parallel {
            stage('制作镜像-service-task') {
              agent none
              steps {
                container('maven') {
                  sh '''cd service/service-task
    podman build -t service-task:latest -f Dockerfile .
    cd ../..'''
                }

              }
            }

            parallel {
            stage('制作镜像-service-user') {
              agent none
              steps {
                container('maven') {
                  sh '''cd service/service-user
    podman build -t service-user:latest -f Dockerfile .
    cd ../..'''
                }

              }
            }

          }
        }

    stage('push latest') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
          sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest '
        }

      }
    }

    stage('deploy to dev') {
      steps {
        container('maven') {
          input(id: 'deploy-to-dev', message: 'deploy to dev?')
          withCredentials([kubeconfigContent(credentialsId : 'KUBECONFIG_CREDENTIAL_ID' ,variable : 'KUBECONFIG_CONFIG' ,)]) {
            sh 'mkdir -p ~/.kube/'
            sh 'echo "$KUBECONFIG_CONFIG" > ~/.kube/config'
            sh 'envsubst < deploy/dev-ol/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

    stage('deploy to production') {
      steps {
        container('maven') {
          input(id: 'deploy-to-production', message: 'deploy to production?')
          withCredentials([kubeconfigContent(credentialsId : 'KUBECONFIG_CREDENTIAL_ID' ,variable : 'KUBECONFIG_CONFIG' ,)]) {
            sh 'mkdir -p ~/.kube/'
            sh 'echo "$KUBECONFIG_CONFIG" > ~/.kube/config'
            sh 'envsubst < deploy/prod-ol/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

  }
  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'docker_username'
    GITHUB_ACCOUNT = 'kubesphere'
    APP_NAME = 'devops-java-sample'
  }
  parameters {
    string(name: 'TAG_NAME', defaultValue: '', description: '')
  }
}
```

这里制作镜像时，需要下载java8的基础镜像，但是会有一定概率失败，原因时这里镜像的构建是在Jenkins的代理：maven容器中运行的，并且使用的是podman进行构建，podman默认是从dockerhub官方镜像仓库中拉取镜像的，因此会有一定的概率下载失败。解决办法：需要修改podman的配置。

但是podman运行在maven容器中，而maven容器是动态创建的，因此在容器内修改配置时没有意义的，容器销毁重建之后，修改的配置也就失效了。要让配置生效，就必须要将跑podman的配置挂载到configmap中。具体的操作如下：

1 在项目 **kubesphere-devops-worker 中修改 configmap/ks-devops-agent 添加数据**

键：PodmanSetting

值：pomand配置文件，例如：

```ini
unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "jkzyghm3.mirror.aliyuncs.com"
insecure = true

#Har
[[registry]]
prefix = "192.168.0.112:30002"
location = "192.168.0.112:30002"
insecure = true

```

注意：`xxx.mirror.aliyuncs.com`要替换成自己的阿里云镜像加速地址

2 在项目 **kubesphere-devops-system 中修改 configmap/jenkins-casc-config，在要使用的agent的容器配置中修改数据挂载：**

```yaml
					containers:
                    - name: "maven"
                      resources:
                        requests:
                          ephemeral-storage: "1Gi"
                        limits:
                          ephemeral-storage: "10Gi"
                      volumeMounts:
                      - name: config-volume
                        mountPath: /opt/apache-maven-3.5.3/conf/settings.xml
                        subPath: settings.xml
                    volumes:
                      - name: config-volume
                        configMap:
                          name: ks-devops-agent
                          items:
                          - key: MavenSetting
                            path: settings.xml
                    securityContext:
                      fsGroup: 1000
```

修改为：

```yaml
					containers:
                    - name: "maven"
                      resources:
                        requests:
                          ephemeral-storage: "1Gi"
                        limits:
                          ephemeral-storage: "10Gi"
                      volumeMounts:
                      - name: config-volume
                        mountPath: /opt/apache-maven-3.5.3/conf/settings.xml
                        subPath: settings.xml
                      - name: podman-config-volume
                        mountPath: /etc/containers/registries.conf
                        subPath: registries.conf                     
                    volumes:
                      - name: config-volume
                        configMap:
                          name: ks-devops-agent
                          items:
                          - key: MavenSetting
                            path: settings.xml
                      - name: podman-config-volume
                        configMap:
                          name: ks-devops-agent
                          items:
                          - key: PodmanSetting
                            path: registries.conf
                    securityContext:
                      fsGroup: 1000
```

也就是增加了

```yaml
volumeMounts:
- name: podman-config-volume
  mountPath: /etc/containers/registries.conf
  subPath: registries.conf 
```

和

```yaml
volumes:
- name: podman-config-volume
  configMap:
    name: ks-devops-agent
    items:
    - key: PodmanSetting
      path: registries.conf
```

`jenkins-casc-config`内有2个配置文件，都进行了同样的修改，这里只改了maven，其他的agent根据需要可以做类似的修改。修改完成后挂载到configmap中的podman的配置就可以生效了。



#### 6.5 推送镜像

推送镜像也需要并行执行，这里先编译好一个作为示例。编辑卡片 push latest ，需改信息：

名称：推送镜像

代理：

​	类型：none



删除默认的条件



默认的脚本：

```bash
docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest
```

```bash
docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$APP_NAME:latest
```

这里使用了环境变量，环境变量的定义在dockerfile的最下面：

```bash
environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'docker.io'
    DOCKERHUB_NAMESPACE = 'docker_username'
    GITHUB_ACCOUNT = 'kubesphere'
    APP_NAME = 'devops-java-sample'
}
```

在这里定义的变量，可以在Dockerfile中的任意位置通过 ${value_name}的方式引用，如 $REGISTRY ，这里需要修改环境变量，要把镜像推送到阿里云镜像仓库，需要修改镜像仓库地址为namespace，修改为：

```bash
environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
    DOCKERHUB_NAMESPACE = 'xxxxxxx'
    GITHUB_ACCOUNT = 'kubesphere'
    APP_NAME = 'devops-java-sample'
  }
```



配置环境变量后，脚本的内容应该是：

```bash
podman tag  hospital-manage:latest $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER
```

其中 $BUILD_NUMBER 是流水线-运行记录-运行id，先将镜像命名为临时镜像，后面在重新命名为正式镜像。

```bash
podman push  $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER
```



想要推送镜像到阿里云私有仓库，首先需要配置阿里云镜像仓库的账号密码。

1 清空默认任务



2 制定容器

添加步骤：指定容器：maven



3 添加凭证

点击`添加嵌套步骤-添加凭证`，在`添加凭证`页中点击`创建凭证`，输入信息：

```bash
名称：aliyun-registry
类型：用户名和密码/username_password
用户名：xxxx@xx.com
密码/令牌：••••••••••••••
描述：阿里云镜像仓库登录凭证
```

返回添加凭证页，选择刚创建的凭证。

```bash
凭证名称：aliyun-registry
密码变量：REGISTRY-PASSWORD
用户名变量：REGISTRY-USERNAME
```

其中定义了账号和密码的环境变量，可以在自身的嵌套步骤中使用、



4 登录阿里云镜像仓库

在添加凭证步骤中添加嵌套步骤：shell

```bash
echo $REGISTRY-PASSWORD | podman login $REGISTRY --username=$REGISTRY-USERNAME --password-stdin
```



5 重命名镜像

在添加凭证步骤中添加嵌套步骤：shell

```bash
podman tag  hospital-manage:latest $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER
```



6 推送镜像

在添加凭证步骤中添加嵌套步骤：shell

```bash
podman push  $REGISTRY/$DOCKERHUB_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER
```



#### 6.5 部署到开发环境 

审核：@pm-wang 是否发布到开发环境？  

这样就必须要 dev-zhao点击确认才能继续往下走，开发环境不需要，生产环境需要，这里删除即可。

给每一个微服务准备一个`deployment.yml`,用于将微服务自动化部署到k8s。

官方示例：

```bash
		stage('deploy to dev') {
             steps {
                 container ('maven') {
                      withCredentials([
                          kubeconfigFile(
                          credentialsId: env.KUBECONFIG_CREDENTIAL_ID,
                          variable: 'KUBECONFIG')
                          ]) {
                          sh 'envsubst < deploy/all-in-one/devops-sample.yaml | kubectl apply -f -'
                      }
                 }
             }
        }
```

要执行 `kubectl apply -f` 需要有权限才能执行，默认安装完成后，只能在master节点执行k8s的指令，如 kubectl get pod -A ，工作节点是没有权限的，执行会报错。原因是在master节点中，在 /root目录下有一个 .kube 的目录， .kube 目录下有一个  config 文件，这个config文件就是集群kubectl的权限配置文件。里面定义了执行kubectl命令时，真实访问的地址，如 https://k8s-master01:6443  .



在`project-test`项目中创建一个阿里云镜像仓库的凭证。



部署过程中的问题：

1 nacos服务版本太高，要重新部署nacos，版本2.0.3，使用镜像 `nacos/nacos-server:v2.0.3`，给项目创建一个独立的命名空间

2 pod的内存限制太小，需要在各个微服的 `deploy.yml` 文件中修改资源限制：

```yaml
          resources:
            limits:
              cpu: 500m
              memory: 1024Mi
```

3 java启动是jvm默认的堆内存大小只有宿主机的1/4，这里需要手动指定，否则会oom，修改`Dockerfile`中的启动参数：

```bash
ENTRYPOINT ["/bin/sh","-c","java -Xmx1024m -Xms128m -Dfile.encoding=utf8  -Djava.security.egd=file:/dev/./urandom -jar /app.jar ${PARAMS}"]
```



4 dockerfile中nacos的配置要修改，如：

```bash
ENV PARAMS="--server.port=8080 --spring.profiles.active=prod --spring.cloud.nacos.discovery.server-addr=nacos-standalone.project-test:8848 --spring.cloud.nacos.discovery.namespace=75f51239-c1f1-4172-976e-e846464448cb --spring.cloud.nacos.config.server-addr=nacos-standalone.project-test:8848 --spring.cloud.nacos.config.namespace=75f51239-c1f1-4172-976e-e846464448cb --spring.cloud.nacos.config.file-extension=yml"
```

这里 namespace 那使用nacos系统随机生成的命名空间ID，而不能使用命名空间的名字。

5 使用了mongodb 的服务在配置文件中设置了mongodb连接信息，但是初始化时会尝试连接配置的数据库，此时在mongodb中还没有创建该数据库，因此连接 失败，提示 Authentication failed 。





完整的Dockerfile：

```bash
FROM openjdk:8-jdk
LABEL maintainer=leixing


#启动自行加载   服务名-prod.yml配置
ENV PARAMS="--server.port=8080 --spring.profiles.active=prod --spring.cloud.nacos.discovery.server-addr=nacos-standalone.project-test:8848 --spring.cloud.nacos.discovery.namespace=75f51239-c1f1-4172-976e-e846464448cb --spring.cloud.nacos.config.server-addr=nacos-standalone.project-test:8848 --spring.cloud.nacos.config.namespace=75f51239-c1f1-4172-976e-e846464448cb --spring.cloud.nacos.config.file-extension=yml"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

COPY target/*.jar /app.jar
EXPOSE 8080

#
ENTRYPOINT ["/bin/sh","-c","java -Xmx1024m -Xms128m -Dfile.encoding=utf8  -Djava.security.egd=file:/dev/./urandom -jar /app.jar ${PARAMS}"]
```



完整的deploy.yml：

```bash
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: service-cmn
  name: service-cmn
  namespace: project-test
spec:
  progressDeadlineSeconds: 600
  replicas: 1
  selector:
    matchLabels:
      app: service-cmn
  strategy:
    rollingUpdate:
      maxSurge: 50%
      maxUnavailable: 50%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: service-cmn
    spec:
      imagePullSecrets:
        - name: harbor-robot-test
      containers:
        - image: $REGISTRY/$REGISTRY_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER
 #         readinessProbe:
 #           httpGet:
 #             path: /actuator/health
 #             port: 8080
 #           timeoutSeconds: 10
 #           failureThreshold: 30
 #           periodSeconds: 5
          imagePullPolicy: Always
          name: app
          ports:
            - containerPort: 8080
              protocol: TCP
          resources:
            limits:
              cpu: 500m
              memory: 1024Mi
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app: service-cmn
  name: service-cmn
  namespace: project-test
spec:
  ports:
    - name: tcp-8080
      port: 8080
      protocol: TCP
      targetPort: 8080
  selector:
    app: service-cmn
  sessionAffinity: None
  type: ClusterIP
```







#### 6.6 部署到生产环境



```bash
pipeline {
  agent {
    node {
      label 'maven'
    }

  }
  stages {
    stage('拉取代码') {
      agent none
      steps {
        container('maven') {
          git(url: 'https://gitee.com/xxxxxxxxxxx/yygh-parent.git', credentialsId: 'gitee-xxxxxxx', branch: 'master', changelog: true, poll: false)
          sh 'ls -al'
        }

      }
    }

    stage('单元测试') {
      agent none
      steps {
        container('maven') {
          sh 'ls -al'
        }

      }
    }

    stage('源码编译') {
      agent none
      steps {
        container('maven') {
          sh 'mvn -Dmaven.test.skip=true clean package'
          sh 'ls -al'
        }

      }
    }

    stage('制作镜像') {
      parallel {
        stage('制作镜像-hospital-manage') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd hospital-manage
              podman build -t hospital-manage:latest -f Dockerfile .
              cd ..
              '''
            }

          }
        }

        stage('制作镜像-server-gateway') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd server-gateway
              podman build -t server-gateway:latest -f Dockerfile .
              cd ..
              '''
            }

          }
        }

        stage('制作镜像-service-cmn') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd service/service-cmn
              podman build -t service-cmn:latest -f Dockerfile .
              cd ../..
              '''
            }

          }
        }

        stage('制作镜像-service-hosp') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd service/service-hosp
              podman build -t service-hosp:latest -f Dockerfile .
              cd ../..
              '''
            }

          }
        }

        stage('制作镜像-service-order') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd service/service-order
              podman build -t service-order:latest -f Dockerfile .
              cd ../..
              '''
            }

          }
        }

        stage('制作镜像-service-oss') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd service/service-oss
              podman build -t service-oss:latest -f Dockerfile .
              cd ../..
              '''
            }

          }
        }

        stage('制作镜像-service-sms') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd service/service-sms
              podman build -t service-sms:latest -f Dockerfile .
              cd ../..
              '''
            }

          }
        }

        stage('制作镜像-service-statistics') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd service/service-statistics
              podman build -t service-statistics:latest -f Dockerfile .
              cd ../..
              '''
            }

          }
        }

        stage('制作镜像-service-task') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd service/service-task
              podman build -t service-task:latest -f Dockerfile .
              cd ../..
              '''
            }

          }
        }

        stage('制作镜像-service-user') {
          agent none
          steps {
            container('maven') {
              sh '''
              cd service/service-user
              podman build -t service-user:latest -f Dockerfile .
              cd ../..
              '''
            }

          }
        }

      }
    }

    stage('推送镜像') {
      parallel {
        stage('推送镜像-hospital-manage') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  hospital-manage:latest $REGISTRY/$REGISTRY_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/hospital-manage:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-server-gateway') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  server-gateway:latest $REGISTRY/$REGISTRY_NAMESPACE/server-gateway:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/server-gateway:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-service-cmn') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  service-cmn:latest $REGISTRY/$REGISTRY_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/service-cmn:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-service-hosp') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  service-hosp:latest $REGISTRY/$REGISTRY_NAMESPACE/service-hosp:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/service-hosp:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-service-order') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  service-order:latest $REGISTRY/$REGISTRY_NAMESPACE/service-order:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/service-order:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-service-oss') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  service-oss:latest $REGISTRY/$REGISTRY_NAMESPACE/service-oss:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/service-oss:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-service-sms') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  hospitalservice-sms:latest $REGISTRY/$REGISTRY_NAMESPACE/service-sms:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/service-sms:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-service-statistics') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  service-statistics:latest $REGISTRY/$REGISTRY_NAMESPACE/service-statistics:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/service-statistics:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-service-task') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  service-task:latest $REGISTRY/$REGISTRY_NAMESPACE/service-task:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/service-task:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

        stage('推送镜像-service-user') {
          agent none
          steps {
            container('maven') {
              withCredentials([usernamePassword(credentialsId : 'aliyun-registry' ,passwordVariable : 'ALIYUN_REGISTRY_PASSWORD' ,usernameVariable : 'ALIYUN_REGISTRY_USERNAME' ,)]) {
                sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
                sh 'podman tag  service-user:latest $REGISTRY/$REGISTRY_NAMESPACE/service-user:SNAPSHOT-$BUILD_NUMBER'
                sh 'docker push  $REGISTRY/$REGISTRY_NAMESPACE/service-user:SNAPSHOT-$BUILD_NUMBER'
              }
            }
          }
        }

      }
    }

    stage('deploy to dev') {
      steps {
        container('maven') {
          input(id: 'deploy-to-dev', message: 'deploy to dev?')
          withCredentials([kubeconfigContent(credentialsId : 'KUBECONFIG_CREDENTIAL_ID' ,variable : 'KUBECONFIG_CONFIG' ,)]) {
            sh 'mkdir -p ~/.kube/'
            sh 'echo "$KUBECONFIG_CONFIG" > ~/.kube/config'
            sh 'envsubst < deploy/dev-ol/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

    stage('deploy to production') {
      steps {
        container('maven') {
          input(id: 'deploy-to-production', message: 'deploy to production?')
          withCredentials([kubeconfigContent(credentialsId : 'KUBECONFIG_CREDENTIAL_ID' ,variable : 'KUBECONFIG_CONFIG' ,)]) {
            sh 'mkdir -p ~/.kube/'
            sh 'echo "$KUBECONFIG_CONFIG" > ~/.kube/config'
            sh 'envsubst < deploy/prod-ol/deploy.yaml | kubectl apply -f -'
          }

        }

      }
    }

  }
  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'    
    GITHUB_ACCOUNT = 'kubesphere'
	REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
	REGISTRY_USERNAME = 'xxxxxx@qq.com'
	REGISTRY_PASSWORD = 'xxxxxxxxxxxx'
    REGISTRY_NAMESPACE = 'xxxxxxx'
  }
  parameters {
    string(name: 'TAG_NAME', defaultValue: '', description: '')
  }
}
```



### 7 系统邮件

在流水线编辑页，点击最有一步右边的`＋`，添加一个stage

名称：发送邮件

代理：

​	类型：none

条件：无

步骤：

点击`添加步骤`，选择邮件，输入邮件信息：

```bash
收件人：xxxxxx@xx.com
抄送：xxxxx@xx.com
主题：project-test构建报告
Body：
project-test构建成功
```

其中在Body中可以使用环境变量， 如 `$BUILD_NUMBER`  `$REGISTRY` `$REGISTRY_NAMESPACE`



要使用发邮件的功能，还需要先配置，



1 注册邮箱

这里以163邮箱为例，https://mail.163.com/ 注册邮箱

2 登录邮箱

3 设置

设置-POP3/SMTP/IMAP

POP3/SMTP/IMAP:
	开启服务：
		IMAP/SMTP服务已关闭 | 开启
		POP3/SMTP服务已关闭 | 开启

在 POP3/SMTP服务  处点击`开启` 

发送短信验证后会，点击我已发送，弹窗显示 `授权密码` ，注意保存，只会显示一次。

注意这里点击邮箱设置页中的 POP3/SMTP/IMAP服务能让你在本地客户端上收发邮件，[了解更多 >](https://help.mail.163.com/faq.do?m=list&categoryID=90)  

点击了解更多可以看到 https://help.mail.163.com/faq.do?m=list&categoryID=90 点击 如何开启客户端协议？进入

https://help.mail.163.com/faqDetail.do?code=d7a5dc8471cd0c0e8b4b8f4f8e49998b374173cfe9171305fa1ce630d7f67ac2a5feb28b66796d3b

可以看到邮箱服务器的设置信息：

| 协议类型 | 协议功能 | 服务器地址   | 非SSL端口号 | SSL端口号 |
| -------- | -------- | ------------ | ----------- | --------- |
| SMTP     | 发送邮件 | smtp.163.com | 25          | 465       |
| POP      | 接收邮件 | pop.163.com  | 110         | 995       |
| IMAP     | 接收邮件 | imap.163.com | 143         | 993       |



1 配置全局邮箱

https://www.kubesphere.io/zh/docs/v3.3/cluster-administration/platform-settings/notification-management/configure-email/#%E9%85%8D%E7%BD%AE%E9%82%AE%E4%BB%B6%E6%9C%8D%E5%8A%A1%E5%99%A8



全局邮箱接收的是KubeSphere系统平台级信息，使用admin登录平台，`平台管理-平台设置-通知管理-通知渠道`，切换到`邮件`，

```bash
邮件
向邮件地址发送通知。

服务器设置
SMTP 服务器地址： smtp.163.com  25


SMTP 用户名：
xxxx@163.com

SMTP 密码
•••••••••••••••• （163邮箱授权密码）

发件人邮箱*
xxxx@163.com

接收设置
receiver-01@xx.com
receiver-02@xx.com
```

配置完成后点击发送测试信息，如果配置的接收邮箱可以收到邮件则说明配置成功。



2 配置devops流水线邮箱

https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-use/pipelines/jenkins-email/#%E8%AE%BE%E7%BD%AE%E7%94%B5%E5%AD%90%E9%82%AE%E4%BB%B6%E6%9C%8D%E5%8A%A1%E5%99%A8



1 点击左上角的**平台管理**，然后选择**集群管理**。

2 转到**应用负载**下的**工作负载**，然后从`下拉列表`中选择 **kubesphere-devops-system** 项目。点击 `devops-jenkins` 右侧的 菜单按钮 并选择**编辑 YAML** 以编辑其 YAML 配置文件。

3 向下滚动到下图所示的需要指定的字段。

```yaml
			- name: EMAIL_SMTP_HOST
              value: mail.example.com
            - name: EMAIL_SMTP_PORT
              value: '465'
            - name: EMAIL_USE_SSL
              value: 'false'
            - name: EMAIL_FROM_NAME
              value: KubeSphere
            - name: EMAIL_FROM_ADDR
              value: admin@example.com
            - name: EMAIL_FROM_PASS
              value: P@ssw0rd
```

修改为：

```
			- name: EMAIL_SMTP_HOST
              value: smtp.163.com
            - name: EMAIL_SMTP_PORT
              value: '465'
            - name: EMAIL_USE_SSL
              value: 'true'
            - name: EMAIL_FROM_NAME
              value: KubeSphere
            - name: EMAIL_FROM_ADDR
              value: xxxx@163.com
            - name: EMAIL_FROM_PASS
              value: ••••••••••••••••
```

这里设置开启了SSL， 端口使用SMTP的SSL端口 465  ，完成修改后，点击**确定**以保存。此时工作负载 devops-jenkins 会重新创建。重新创建完成后可以在devops项目中创建一条流水线测试邮箱是否配置成功。流水线的Jenkinsfile如下：

```groovy
pipeline {
  agent {
    node {
      label 'maven'
    }

  }
  stages {
    stage('发送邮件') {
      agent none
      steps {
        mail(to: '<receiver-01>@xx.com', cc: '<receiver-02>@xx.com', subject: 'project-test构建报告', body: "project-test构建报告：\n$BUILD_NUMBER 构建成功")
      }
    }

  }
  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    GITHUB_ACCOUNT = 'kubesphere'
    REGISTRY = '192.168.0.112:30003'
    REGISTRY_NAMESPACE = 'project-test'
  }
  parameters {
    string(name: 'TAG_NAME', defaultValue: '', description: '')
  }
}
```

注意：要想使用环境变量，就一定要用 `"  "` 包裹body的内容，直接在ui界面使用会无法解析环境变量，要直接修改Jenkinsfile的方式修改 body 字段。



### 测试后端服务

在部署文件中，所有其他的微服务的 service的type字段的值都是ClusterIP，只有 gateway项目时 NodePort，这样会使得gateway项目默认有暴露的端口，比如这里是 32607 ，方法是在deploy.yml中的Service的声明中设置：

```yaml
apiVersion: v1
kind: Service
metadata:
  labels:
    app: server-gateway
  name: server-gateway
  namespace: project-test
spec:
  ports:
    - name: tcp-8080
      port: 8080
      protocol: TCP
      targetPort: 8080
      nodePort: 32607
  selector:
    app: server-gateway
  sessionAffinity: None
  type: NodePort
```

spec.type: NodePort 端口暴露方式为NodePort

```yaml
spec:
  ports:
    - name: tcp-8080
      port: 8080
      protocol: TCP
      targetPort: 8080
      nodePort: 32607
```

手动指定这个暴露的端口类型为NodePort时，nodeport端口为 32607 。



可以在本地启动前端项目，将API的baseurl改为gateway访问地址即可。



##### 本地运行 yygh-admin 项目

打开 `yygh-admin` 项目，修改开发环境的配置  config/dev.env.js

修改：

```javascript
module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  // BASE_API: '"http://localhost"',
  BASE_API: '"http://139.198.165.238:32607"'
})
```

为：

```javascript
module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  // BASE_API: '"http://localhost"',
  BASE_API: '"http://192.168.0.112:32607"'
})
```



注意

1 运行前端项目的nodejs版本不能太高，经测试 `10.24.1` 版本可以编译运行

2 前端项目中在代码中设置了固定的登录token， 在 yygh-admin/src/store/modules/users.js 中：

```javascript
Login({ commit }) {
      const data = {
        'token': 'eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJSCjAK0A0Ndg1S0lFKrShQsjI0s7C0NDY1MDTQUSotTi3yTAGKQZh-ibmpQB2JKbmZeUq1AMlpr79BAAAA.XKTQ7ZdPfW_QSUzLHFusz8kv-NOjPL4zeW9-59vEwfPQNs3wJH4UetRJVkGfvJwMeoBKSn-kwMs45OdKWsVSWg'
      }
      setToken(data.token)// 将token存储在cookie中
      commit('SET_TOKEN', data.token)
    },
```

这里的token的过期时间是2021年，需要重新生成token，在 yygh-parent项目中 yygh-parent\common\common-util\src\main\java\com\atguigu\yygh\common\helper\JwtHelper.java 中执行main方法即可生成。将生成的toekn复制到前端项目 yygh-admin代码中。然后将浏览器中的缓存都清除，再重新编译运行前端项目即可。

```bash
npm insta node-sass --sass_binary_site=https://npm.taobao.org/mirrors/node-sass/
npm install --registry=https://registry.npm.taobao.org
npm run dev
```

初次运行容易产生超时错误，反复运行几次即可。



##### 本地运行 yygh-site 项目

打开 `yygh-site` 项目，修改访问地址，修改 yygh-site/utils/request.js

修改：

```javascript
// 创建axios实例
const service = axios.create({
  baseURL: 'http://139.198.165.238:32607', //生产环境
  timeout: 15000 // 请求超时时间
})
```

为：

```javascript
// 创建axios实例
const service = axios.create({
  baseURL: 'http://192.168.0.112:32607', //生产环境
  timeout: 15000 // 请求超时时间
})
```

注意 yygh-site 使用了 Nuxt.js技术，Nuxt是属于服务端渲染技术，发起数据请求的是部署项目的机器，也就是在k8s集群中的pod，因此在线上环境这里baseURL可以改成网关的集群内部dns （ `server-gateway.project-test:8080` ）来进行访问，这样就不需要通过外网再访问集群。

部署线上环境的配置

```
// 创建axios实例
const service = axios.create({
  baseURL: 'http://server-gateway.project-test:8080', //生产环境
  timeout: 15000 // 请求超时时间
})
```

运行：

```bash
npm install --registry=https://registry.npm.taobao.org
npm run dev
```





#### 部署前端项目

前端项目需要使用nodejs，因此需要选择nodejs代理



##### 部署yygh_admin项目

流水线上要执行的步骤：

1 拉取代码

```bash
地址： https://gitee.com/leixing1012/yygh-admin.git
密钥：使用之前配置 gitee密钥
分支：master
```



2 项目编译

```bash
npm i node-sass --sass_binary_site=https://npm.taobao.org/mirrors/node-sass/
npm install --registry=https://registry.npm.taobao.org
npm run build
```

注意构建使用`npm run build`，构建完成会在项目的根目录生成`dist`目录。将这个目录交给nginx就可以运行了。



3 制作镜像

基于nginx镜像制作，Dockerfile如下：

```bash
FROM nginx

#将dist目录内容复制到nginx容器html内部
COPY dist /usr/share/nginx/html/

EXPOSE 80
```

将Dockerfile放在项目的根目录下，执行命令构建

```bash
docker build -t yygh-admin:latest -f Dockerfile  .
```



4 推送镜像

登录

```bash
echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin
```

重新打tag

```bash
podman tag yygh-admin:latest $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER
```

推送

```bash
docker push $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER
```



5 部署

```bash
nvsubst < deploy/deploy.yml | kubectl apply -f -
```

等价于 

```bash
kubectl apply -f deploy/deploy.yml
```



6 发送邮件

```bash
收件人: leixing1012@163.com
抄送: leixing1012@qq.com
主题：project-test构建报告
body: 
project-test构建报告：
$BUILD_NUMBER 构建成功
```



根据上面的步骤，可以编写流水线`Jenkinsfile`保存到项目的根目录。

```groovy
pipeline {
  agent {
    node {
      label 'nodejs'
    }
  }

  stages {
    stage('拉取代码') {
      agent none
      steps {
        container('nodejs') {
          git(url: 'https://gitee.com/leixing1012/yygh-admin.git', credentialsId: 'gitee-leixing', branch: 'master', changelog: true, poll: false)
          sh 'ls -al'
        }
      }
    }

    stage('项目编译') {
      agent none
      steps {
        container('nodejs') {
          sh 'node -v'
          sh 'npm -v'
          sh 'npm i node-sass --sass_binary_site=https://npm.taobao.org/mirrors/node-sass/'
          sh 'npm install --registry=https://registry.npm.taobao.org'
          sh 'npm run build'
          sh 'ls'
        }
      }
    }

    stage('构建镜像') {
      agent none
      steps {
        container('nodejs') {
          sh 'ls'
          sh 'podman build -t yygh-admin:latest -f Dockerfile  .'
        }
      }
    }

    stage('推送镜像') {
      agent none
      steps {
        container('nodejs') {
          withCredentials([usernamePassword(credentialsId: 'harbor-robot-test', passwordVariable: 'REGISTRY_PASSWORD', usernameVariable: 'REGISTRY_USERNAME',)]) {
            sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
            sh 'podman tag yygh-admin:latest $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER'
            sh 'podman push $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER'
          }
        }
      }
    }

    stage('部署到生产环境') {
      agent none
      steps {
        container('nodejs') {
          withCredentials([kubeconfigFile(credentialsId: env.KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG')]) {
            sh 'envsubst < deploy/deploy.yml | kubectl apply -f -'
          }
        }
      }
    }

    stage('发送邮件') {
      agent none
      steps {
        mail(to: 'leixing1012@163.com', cc: 'leixing1012@qq.com', subject: 'project-test构建报告', body: "project-test构建报告：\n$BUILD_NUMBER 构建成功")
      }
    }
  }

  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    GITHUB_ACCOUNT = 'kubesphere'
    REGISTRY = '192.168.0.112:30003'
    REGISTRY_NAMESPACE = 'project-test'
  }
}
```

更新完代码和配置文件后提交到服务器，然后登录到 `KubeSphere`，到`devops`项目中新建流水线

输入基本信息：

```bash
名称：project-test-ui-admin
描述：测试项目前端-后台管理页面
```

点击代码仓库：

选择代码仓库：

```bash
代码仓库地址：https://gitee.com/xxxx/yygh-admin.git
凭证：gitee-xxxx
```

点击`√`，点击`下一步`，进入 `高级设置` 页：

```bash
分支设置
	[]删除旧分支

正则过滤
	[]使用正则表达式过滤分支，PR和标签

脚本路径
路径
Jenkinsfile
设置 Jenkinsfile 在代码仓库中的的路径。

扫描触发器
[]定时扫描

构建触发器
[]通过流水线事件触发

克隆设置
[]启用浅克隆

Webhook
Webhook 推送 URL
http://192.168.0.112:30880/devops_webhook/git/?url=https://gitee.com/leixing1012/yygh-admin.git
```

主要是取消勾选 `删除旧分支`，脚本路径设置为：实际Jenkinfile相对于项目代码的路径。注意记录Webhook的URL，后面需要在git代码仓库中配置。

点击`创建`即可。进入代码仓库： https://gitee.com/xxxxxxxx/yygh-admin  点击`管理-WebHooks-添加webHook`,在url处输出上面的webhook的URL。

注意这里的地址是无法添加到git仓库的webhooks中的，需要有公网ip。有了公网ip后，将hook地替换为公网ip的URL即可。没有配置公网ip，可以在KubeShpere中手动启动流水线。



也可以使用上面的Jenkinsfile通过手动创建流水线。

启动流水线之后会发现无法推送到私有的harbor仓库，原因是没有为nodejs的agent配置密钥。与前面为maven的agent配置密钥和挂载podman一样，修改 `kudesphere-devops-system`项目下的 `jenkins-casc-config` 配置，修改如下：

jenkins.yaml:

```yaml
jenkins:
  mode: EXCLUSIVE
  numExecutors: 0
  scmCheckoutRetryCount: 2
  disableRememberMe: true

  clouds:
    - kubernetes:
        name: "kubernetes"
        serverUrl: "https://kubernetes.default"
        skipTlsVerify: true
        namespace: "kubesphere-devops-worker"
        credentialsId: "k8s-service-account"
        jenkinsUrl: "http://devops-jenkins.kubesphere-devops-system:80"
        jenkinsTunnel: "devops-jenkins-agent.kubesphere-devops-system:50000"
        containerCapStr: "10"
        connectTimeout: "60"
        readTimeout: "60"
        maxRequestsPerHostStr: "32"
        templates:
          - name: "base"
            namespace: "kubesphere-devops-worker"
            label: "base"
            nodeUsageMode: "NORMAL"
            idleMinutes: 0
            containers:
            - name: "base"
              image: "kubesphere/builder-base:v3.2.2-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "base"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                securityContext:
                  fsGroup: 1000

          - name: "nodejs"
            namespace: "kubesphere-devops-worker"
            label: "nodejs"
            nodeUsageMode: "EXCLUSIVE"
            idleMinutes: 0
            containers:
            - name: "nodejs"
              image: "kubesphere/builder-nodejs:v3.2.0-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_nodejs_yarn_cache"
                mountPath: "/root/.yarn"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_nodejs_npm_cache"
                mountPath: "/root/.npm"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "nodejs"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                  volumeMounts:
                  - name: podman-config-volume
                    mountPath: /etc/containers/registries.conf
                    subPath: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/ca.crt
                    subPath: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.cert
                    subPath: tls.cert
                  - name: harbor-ca-tls-key-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.key
                    subPath: tls.key
                volumes:
                  - name: podman-config-volume
                    configMap:
                      name: ks-devops-agent
                      items:
                      - key: PodmanSetting
                        path: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: ca.crt
                        path: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.cert
                        path: tls.cert
                  - name: harbor-ca-tls-key-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.key
                        path: tls.key    
                securityContext:
                  fsGroup: 1000

          - name: "maven"
            namespace: "kubesphere-devops-worker"
            label: "maven"
            nodeUsageMode: "EXCLUSIVE"
            idleMinutes: 0
            containers:
            - name: "maven"
              image: "kubesphere/builder-maven:v3.2.0-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_maven_cache"
                mountPath: "/root/.m2"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "maven"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                  volumeMounts:
                  - name: config-volume
                    mountPath: /opt/apache-maven-3.5.3/conf/settings.xml
                    subPath: settings.xml
                  - name: podman-config-volume
                    mountPath: /etc/containers/registries.conf
                    subPath: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/ca.crt
                    subPath: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.cert
                    subPath: tls.cert
                  - name: harbor-ca-tls-key-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.key
                    subPath: tls.key
                volumes:
                  - name: config-volume
                    configMap:
                      name: ks-devops-agent
                      items:
                      - key: MavenSetting
                        path: settings.xml
                  - name: podman-config-volume
                    configMap:
                      name: ks-devops-agent
                      items:
                      - key: PodmanSetting
                        path: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: ca.crt
                        path: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.cert
                        path: tls.cert
                  - name: harbor-ca-tls-key-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.key
                        path: tls.key
                securityContext:
                  fsGroup: 1000

          - name: "mavenjdk11"
            label: "jdk11 maven java"
            inheritFrom: "maven"
            containers:
            - name: "maven"
              image: "kubesphere/builder-maven:v3.2.1-jdk11-podman"

          - name: "go"
            namespace: "kubesphere-devops-worker"
            label: "go"
            nodeUsageMode: "EXCLUSIVE"
            idleMinutes: 0
            containers:
            - name: "go"
              image: "kubesphere/builder-go:v3.2.0-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_go_cache"
                mountPath: "/home/jenkins/go/pkg"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "go"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                securityContext:
                  fsGroup: 1000

          - name: "go16"
            label: "go16"
            inheritFrom: "go"
            containers:
            - name: "go"
              image: "kubesphere/builder-go:v3.2.2-1.16-podman"
          - name: "go17"
            label: "go17"
            inheritFrom: "go"
            containers:
            - name: "go"
              image: "kubesphere/builder-go:v3.2.2-1.17-podman"
          - name: "go18"
            label: "go18"
            inheritFrom: "go"
            containers:
            - name: "go"
              image: "kubesphere/builder-go:v3.2.2-1.18-podman"

          - name: "python"
            namespace: "kubesphere-devops-worker"
            label: "python"
            nodeUsageMode: "EXCLUSIVE"
            idleMinutes: 0
            containers:
            - name: "python"
              image: "kubesphere/builder-python:v3.2.0-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_python_pip_cache"
                mountPath: "/root/.cache/pip"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_python_pipenv_cache"
                mountPath: "/root/.local/share/virtualenvs"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "python"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                securityContext:
                  fsGroup: 1000

  securityRealm:
    ldap:
      configurations:
      - displayNameAttributeName: "uid"
        mailAddressAttributeName: "mail"
        inhibitInferRootDN: false
        managerDN: "cn=admin,dc=kubesphere,dc=io"
        managerPasswordSecret: "admin"
        rootDN: "dc=kubesphere,dc=io"
        userSearchBase: "ou=Users"
        userSearch: "(&(objectClass=inetOrgPerson)(|(uid={0})(mail={0})))"
        groupSearchBase: "ou=Groups"
        groupSearchFilter: "(&(objectClass=posixGroup)(cn={0}))"
        server: "ldap://openldap.kubesphere-system.svc:389"
      disableMailAddressResolver: false
      disableRolePrefixing: true


unclassified:
  location:
    url: "http://jenkins.devops.kubesphere.local"
  kubespheretokenauthglobalconfiguration:
    cacheConfiguration:
      size: 20
      ttl: 300
    enabled: true
    server: "http://devops-apiserver.kubesphere-devops-system:9090/"
  eventDispatcher:
    receiver: "http://devops-apiserver.kubesphere-devops-system:9090/v1alpha3/webhooks/jenkins"
  gitLabServers:
    servers:
    - name: "https://gitlab.com"
      serverUrl: "https://gitlab.com"
```



jenkins_user.yaml:

```yaml
jenkins:
  mode: EXCLUSIVE
  numExecutors: 0
  scmCheckoutRetryCount: 2
  disableRememberMe: true

  clouds:
    - kubernetes:
        name: "kubernetes"
        serverUrl: "https://kubernetes.default"
        skipTlsVerify: true
        namespace: "kubesphere-devops-worker"
        credentialsId: "k8s-service-account"
        jenkinsUrl: "http://devops-jenkins.kubesphere-devops-system:80"
        jenkinsTunnel: "devops-jenkins-agent.kubesphere-devops-system:50000"
        containerCapStr: "10"
        connectTimeout: "60"
        readTimeout: "60"
        maxRequestsPerHostStr: "32"
        templates:
          - name: "base"
            namespace: "kubesphere-devops-worker"
            label: "base"
            nodeUsageMode: "NORMAL"
            idleMinutes: 0
            containers:
            - name: "base"
              image: "kubesphere/builder-base:v3.2.2-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "base"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                securityContext:
                  fsGroup: 1000

          - name: "nodejs"
            namespace: "kubesphere-devops-worker"
            label: "nodejs"
            nodeUsageMode: "EXCLUSIVE"
            idleMinutes: 0
            containers:
            - name: "nodejs"
              image: "kubesphere/builder-nodejs:v3.2.0-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_nodejs_yarn_cache"
                mountPath: "/root/.yarn"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_nodejs_npm_cache"
                mountPath: "/root/.npm"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "nodejs"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                  volumeMounts:
                  - name: podman-config-volume
                    mountPath: /etc/containers/registries.conf
                    subPath: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/ca.crt
                    subPath: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.cert
                    subPath: tls.cert
                  - name: harbor-ca-tls-key-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.key
                    subPath: tls.key
                volumes:
                  - name: podman-config-volume
                    configMap:
                      name: ks-devops-agent
                      items:
                      - key: PodmanSetting
                        path: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: ca.crt
                        path: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.cert
                        path: tls.cert
                  - name: harbor-ca-tls-key-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.key
                        path: tls.key    
                securityContext:
                  fsGroup: 1000

          - name: "maven"
            namespace: "kubesphere-devops-worker"
            label: "maven"
            nodeUsageMode: "EXCLUSIVE"
            idleMinutes: 0
            containers:
            - name: "maven"
              image: "kubesphere/builder-maven:v3.2.0-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_maven_cache"
                mountPath: "/root/.m2"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "maven"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                  volumeMounts:
                  - name: config-volume
                    mountPath: /opt/apache-maven-3.5.3/conf/settings.xml
                    subPath: settings.xml
                  - name: podman-config-volume
                    mountPath: /etc/containers/registries.conf
                    subPath: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/ca.crt
                    subPath: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.cert
                    subPath: tls.cert
                  - name: harbor-ca-tls-key-volume
                    mountPath: /etc/docker/certs.d/192.168.0.112:30003/tls.key
                    subPath: tls.key
                volumes:
                  - name: config-volume
                    configMap:
                      name: ks-devops-agent
                      items:
                      - key: MavenSetting
                        path: settings.xml
                  - name: podman-config-volume
                    configMap:
                      name: ks-devops-agent
                      items:
                      - key: PodmanSetting
                        path: registries.conf
                  - name: harbor-ca-ca-crt-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: ca.crt
                        path: ca.crt
                  - name: harbor-ca-tls-cert-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.cert
                        path: tls.cert
                  - name: harbor-ca-tls-key-volume
                    configMap:
                      name: harbor-ca
                      items:
                      - key: tls.key
                        path: tls.key
                securityContext:
                  fsGroup: 1000

          - name: "mavenjdk11"
            label: "jdk11 maven java"
            inheritFrom: "maven"
            containers:
            - name: "maven"
              image: "kubesphere/builder-maven:v3.2.1-jdk11-podman"

          - name: "go"
            namespace: "kubesphere-devops-worker"
            label: "go"
            nodeUsageMode: "EXCLUSIVE"
            idleMinutes: 0
            containers:
            - name: "go"
              image: "kubesphere/builder-go:v3.2.0-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_go_cache"
                mountPath: "/home/jenkins/go/pkg"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "go"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                securityContext:
                  fsGroup: 1000

          - name: "go16"
            label: "go16"
            inheritFrom: "go"
            containers:
            - name: "go"
              image: "kubesphere/builder-go:v3.2.2-1.16-podman"
          - name: "go17"
            label: "go17"
            inheritFrom: "go"
            containers:
            - name: "go"
              image: "kubesphere/builder-go:v3.2.2-1.17-podman"
          - name: "go18"
            label: "go18"
            inheritFrom: "go"
            containers:
            - name: "go"
              image: "kubesphere/builder-go:v3.2.2-1.18-podman"

          - name: "python"
            namespace: "kubesphere-devops-worker"
            label: "python"
            nodeUsageMode: "EXCLUSIVE"
            idleMinutes: 0
            containers:
            - name: "python"
              image: "kubesphere/builder-python:v3.2.0-podman"
              command: "cat"
              args: ""
              ttyEnabled: true
              privileged: true
              resourceRequestCpu: "100m"
              resourceLimitCpu: "4000m"
              resourceRequestMemory: "100Mi"
              resourceLimitMemory: "8192Mi"
            - name: "jnlp"
              image: "jenkins/inbound-agent:4.10-2"
              args: "^${computer.jnlpmac} ^${computer.name}"
              resourceRequestCpu: "50m"
              resourceLimitCpu: "500m"
              resourceRequestMemory: "400Mi"
              resourceLimitMemory: "1536Mi"
            workspaceVolume:
              emptyDirWorkspaceVolume:
                memory: false
            volumes:
            - hostPathVolume:
                hostPath: "/var/run/docker.sock"
                mountPath: "/var/run/docker.sock"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_python_pip_cache"
                mountPath: "/root/.cache/pip"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_python_pipenv_cache"
                mountPath: "/root/.local/share/virtualenvs"
            - hostPathVolume:
                hostPath: "/var/data/jenkins_sonar_cache"
                mountPath: "/root/.sonar/cache"
            yaml: |
              spec:
                affinity:
                  nodeAffinity:
                    preferredDuringSchedulingIgnoredDuringExecution:
                    - weight: 1
                      preference:
                        matchExpressions:
                        - key: node-role.kubernetes.io/worker
                          operator: In
                          values:
                          - ci
                tolerations:
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "NoSchedule"
                - key: "node.kubernetes.io/ci"
                  operator: "Exists"
                  effect: "PreferNoSchedule"
                containers:
                - name: "python"
                  resources:
                    requests:
                      ephemeral-storage: "1Gi"
                    limits:
                      ephemeral-storage: "10Gi"
                securityContext:
                  fsGroup: 1000

  securityRealm:
    ldap:
      configurations:
      - displayNameAttributeName: "uid"
        mailAddressAttributeName: "mail"
        inhibitInferRootDN: false
        managerDN: "cn=admin,dc=kubesphere,dc=io"
        managerPasswordSecret: "admin"
        rootDN: "dc=kubesphere,dc=io"
        userSearchBase: "ou=Users"
        userSearch: "(&(objectClass=inetOrgPerson)(|(uid={0})(mail={0})))"
        groupSearchBase: "ou=Groups"
        groupSearchFilter: "(&(objectClass=posixGroup)(cn={0}))"
        server: "ldap://openldap.kubesphere-system.svc:389"
      disableMailAddressResolver: false
      disableRolePrefixing: true


unclassified:
  location:
    url: "http://jenkins.devops.kubesphere.local"
  kubespheretokenauthglobalconfiguration:
    cacheConfiguration:
      size: 20
      ttl: 300
    enabled: true
    server: "http://devops-apiserver.kubesphere-devops-system:9090/"
  eventDispatcher:
    receiver: "http://devops-apiserver.kubesphere-devops-system:9090/v1alpha3/webhooks/jenkins"
  gitLabServers:
    servers:
    - name: "https://gitlab.com"
      serverUrl: "https://gitlab.com"
```



修改部署文件：

修改项目目录下的 /deply/deploy.yml文件，注意修改的部分：

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  namespace: project-test
spec:
  template:
    spec:
      imagePullSecrets:
        - name: harbor-robot-test
      containers:
        - image: $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER
---
apiVersion: v1
kind: Service
metadata:
  namespace: project-test
```

修改完后完整的部署文件为：

deploy.yml :

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: yygh-admin
  name: yygh-admin
  namespace: project-test
spec:
  progressDeadlineSeconds: 600
  replicas: 1
  selector:
    matchLabels:
      app: yygh-admin
  strategy:
    rollingUpdate:
      maxSurge: 50%
      maxUnavailable: 50%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: yygh-admin
    spec:
      imagePullSecrets:
        - name: harbor-robot-test
      containers:
        - image: $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER
 #         readinessProbe:
 #           httpGet:
 #             path: /actuator/health
 #             port: 8080
 #           timeoutSeconds: 10
 #           failureThreshold: 30
 #           periodSeconds: 5
          imagePullPolicy: Always
          name: app
          ports:
            - containerPort: 80
              protocol: TCP
          resources:
            limits:
              cpu: 300m
              memory: 600Mi
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app: yygh-admin
  name: yygh-admin
  namespace: project-test
spec:
  ports:
    - name: http
      port: 80
      protocol: TCP
      targetPort: 80
      nodePort: 32248
  selector:
    app: yygh-admin
  sessionAffinity: None
  type: NodePort
```



##### 部署yygh_site项目

本地运行：

```
npm install --registry=https://registry.npm.taobao.org --production
npm run build
```

与admin项目部署方式一致。



Jenkinsfile：

```groovy
pipeline {
  agent {
    node {
      label 'nodejs'
    }

  }
  stages {
    stage('拉取代码') {
      agent none
      steps {
        container('nodejs') {
          git(url: 'https://gitee.com/leixing1012/yygh-site.git', credentialsId: 'gitee-leixing', branch: 'master', changelog: true, poll: false)
          sh 'ls -al'
        }
      }
    }

    stage('项目编译') {
      agent none
      steps {
        container('nodejs') {
          sh 'ls -al'
          sh 'node -v'
          sh 'npm -v'
          sh 'npm install --registry=https://registry.npm.taobao.org'
          sh 'npm run build'
        }
      }
    }

    stage('构建镜像') {
      agent none
      steps {
        container('nodejs') {
          sh 'ls -al'
          sh 'podman build -t yygh-site:latest -f Dockerfile  .'
        }

      }
    }

    stage('推送镜像') {
      agent none
      steps {
        container('nodejs') {
          withCredentials([usernamePassword(credentialsId: 'harbor-robot-test', passwordVariable: 'REGISTRY_PASSWORD', usernameVariable: 'REGISTRY_USERNAME',)]) {
            sh 'echo $REGISTRY_PASSWORD | podman login  $REGISTRY --username=$REGISTRY_USERNAME --password-stdin'
            sh 'podman tag yygh-site:latest $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER'
            sh 'podman push  $REGISTRY/$REGISTRY_NAMESPACE/yygh-admin:SNAPSHOT-$BUILD_NUMBER'
          }
        }
      }
    }

    stage('部署到生产环境') {
      agent none
      steps {
        container('nodejs') {
          withCredentials([kubeconfigFile(credentialsId: env.KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG')]) {
            sh 'envsubst < deploy/deploy.yml | kubectl apply -f -'
          }
        }
      }
    }

    stage('发送邮件') {
      agent none
      steps {
        mail(to: 'leixing1012@163.com', cc: 'leixing1012@qq.com', subject: 'project-test构建报告', body: "project-test-yygh-site 构建报告：\n$BUILD_NUMBER 构建成功")
      }
    }
  }

  environment {
    DOCKER_CREDENTIAL_ID = 'dockerhub-id'
    GITHUB_CREDENTIAL_ID = 'github-id'
    KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
    GITHUB_ACCOUNT = 'kubesphere'
    REGISTRY = '192.168.0.112:30003'
    REGISTRY_NAMESPACE = 'project-test'
  }
}
```



Dockerfile：

```bash
FROM node:14.17.6

WORKDIR /app
#把.nuxt目录下的所有内容复制到/app/.nuxt/
COPY .  /app/

#安装核心依赖  npm cache clean -f
RUN ["npm","install","--registry=https://registry.npm.taobao.org"]
RUN ["npm","run","build"]
EXPOSE 3000
CMD ["npm", "run", "start"]
```



deploy.yml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: yygh-site
  name: yygh-site
  namespace: project-test   #一定要写名称空间
spec:
  progressDeadlineSeconds: 600
  replicas: 1
  selector:
    matchLabels:
      app: yygh-site
  strategy:
    rollingUpdate:
      maxSurge: 50%
      maxUnavailable: 50%
    type: RollingUpdate
  template:
    metadata:
      labels:
        app: yygh-site
    spec:
      imagePullSecrets:
        - name: harbor-robot-test
      containers:
        - image: $REGISTRY/$REGISTRY_NAMESPACE/yygh-site:SNAPSHOT-$BUILD_NUMBER
 #         readinessProbe:
 #           httpGet:
 #             path: /actuator/health
 #             port: 8080
 #           timeoutSeconds: 10
 #           failureThreshold: 30
 #           periodSeconds: 5
          imagePullPolicy: Always
          name: app
          ports:
            - containerPort: 3000
              protocol: TCP
          resources:
            limits:
              cpu: 300m
              memory: 600Mi
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
      dnsPolicy: ClusterFirst
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
---
apiVersion: v1
kind: Service
metadata:
  labels:
    app: yygh-site
  name: yygh-site
  namespace: project-test
spec:
  ports:
    - name: http
      port: 3000
      protocol: TCP
      targetPort: 3000
      nodePort: 32070
  selector:
    app: yygh-site
  sessionAffinity: None
  type: NodePort
```



镜像优化

按照上面的脚本制作的镜像很大，在Harbor上可以看到镜像的大小为 393.68MiB 。可以优化镜像制作的流程，让镜像更小，优化后的镜像仅为 88.77MiB 

参考：

https://www.yii666.com/article/690348.html?action=onAll

优化后的Dockerfile如下：

```bash
FROM node:14 AS build

WORKDIR /build

COPY package*.json /build/

RUN ["npm","install","--registry=https://registry.npm.taobao.org", "--production"]

COPY  .  /build/

RUN ["npm","run","build"]




FROM node:14-alpine AS release

WORKDIR /run

COPY package*.json /run

RUN ["npm","install","--registry=https://registry.npm.taobao.org", "--production"]

COPY --from=build /build/.nuxt /run/.nuxt

EXPOSE 3000

CMD ["npm", "run", "start"]
```



过程：

1 使用完整版本的镜像 node:14 ，取名为 build 

2 将项目当前的 package*.json 复制到容器的 /build 目录

3 容器内的工作目录是 /build，此时在工作目录执行目录内只有 package*.json 文件

```bash
npm install --registry=https://registry.npm.taobao.org --production
```

根据package.json 文件安装依赖，此时会在容器内的 /build 目录下生成 mode_modules 目录，里面是项目的生产级依赖。

前三步在代码没有修改的情况下是可以缓存的，这样可以加速构建过程。

4 将项目目录，也就是源码的更目录全部复制到容器的 /build 目录内

5 在容器内的 /build 目录执行：

```bash
npm run build
```

由于使用了 nuxt.js ，此时会在容器内的 /build 目录下生成  .nuxt 目录

6 基于node的轻量级容器 node:14-alpine 构建容器。

7 将当前目录（项目源码目录）下的 package*.json复制到 /run 容器的目录

8 在容器内的 /run 目录下运行：

```bash
npm install --registry=https://registry.npm.taobao.org --production
```

安装项目的生产级依赖，保存在 /run/node_modules

9 将上一步 build 容器中的 /build/.nuxt 目录复制到  当前镜像中的 /run/.nuxt目录

10 开放端口，运行

```bash
npm run start
```







