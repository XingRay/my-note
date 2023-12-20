# SpringCloud-Kubernetes配置中心

https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/

在k8s环境中使用k8s作为配置中心



## 1 环境准备

环境:

Kubenetes : v1.24.9

KubeSphere :  3.3.2

Harbor : v2.8.2

Java: 20

SpringBoot : 3.1.1

SpringCloud : 2022.0.3

maven : 3.9.3



需要先部署k8s集群,可以使用 KubeKey进行部署

https://github.com/kubesphere/kubekey



部署Harbor服务, 并创建一个robot_admin 账号, 创建一个项目 springcloud-k8s 



## 2 目标

创建一个测试模块: member模块, member模块通过SpringCloud-Kubernetes从Kubernetes中的Configmap获取配置,并且支持热更新, 多环境, 配置文件拆分



### 2.1 创建项目 springcloud-k8s

pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.1</version>
    </parent>

    <groupId>com.xingray</groupId>
    <artifactId>springcloud-k8s</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <modules>
        <module>service-member</module>
        <module>service-order</module>
        <module>lib-k8s-order-api</module>
    </modules>

    <properties>
        <maven.compiler.source>20</maven.compiler.source>
        <maven.compiler.target>20</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2022.0.3</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-kubernetes-dependencies</artifactId>
                <version>3.0.3</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.yaml</groupId>
                <artifactId>snakeyaml</artifactId>
                <version>2.0</version>
            </dependency>

            <dependency>
                <groupId>commons-fileupload</groupId>
                <artifactId>commons-fileupload</artifactId>
                <version>1.5</version>
            </dependency>

        </dependencies>
    </dependencyManagement>

</project>
```



### 2.4 创建模块 service-member

pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.xingray</groupId>
        <artifactId>springcloud-k8s</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>service-member</artifactId>

    <properties>
        <maven.compiler.source>20</maven.compiler.source>
        <maven.compiler.target>20</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-kubernetes-fabric8</artifactId>
        </dependency>

        <dependency>
            <groupId>com.xingray</groupId>
            <artifactId>lib-k8s-order-api</artifactId>
            <version>1.0.0</version>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>3.1.1</version>
            </plugin>
        </plugins>
    </build>
</project>
```



MemberApplication.java

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {OrderApi.class})
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}
```



CustomProperty.java

```java
@ConfigurationProperties(prefix = "my.custom")
public class CustomProperty {
    private String url;

    private String user;

    private String password;

    private String name;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CustomConfig{" +
                "url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
```



CustomConfig.java

```java
@Configuration
@EnableConfigurationProperties({CustomProperty.class})
@RefreshScope
public class CustomConfig {
    private CustomProperty customProperty;

    public CustomConfig(CustomProperty customProperty) {
        this.customProperty = customProperty;
    }

    public CustomProperty getCustomProperty() {
        return customProperty;
    }

    public void setCustomProperty(CustomProperty customProperty) {
        this.customProperty = customProperty;
    }

    @Override
    public String toString() {
        return "CustomConfig{" +
                "customProperty=" + customProperty +
                '}';
    }
}
```



PropertyController.java

```java
@RestController
@RequestMapping("/property")
public class PropertyController {

    private final CustomConfig customConfig;

    public PropertyController(CustomConfig customConfig) {
        this.customConfig = customConfig;
    }

    @GetMapping("/custom")
    public String getCustomConfig() {
        return customConfig.toString();
    }
}
```



application.yml

```yaml
spring:
  application:
    name: member
  profiles:
    active:
      - ${env:dev}
      - custom
```



application-prod.yml

```yaml
server:
  port: 8080

spring:
  config:
    import:
      - kubernetes:member.yml
      - kubernetes:member-prod.yml
      - kubernetes:member-custom.yml
  cloud:
    kubernetes:
      discovery:
        enabled: true
        namespaces:
          - springcloud-k8s
        include-not-ready-addresses: true

      config:
        enabled: true
        namespace: springcloud-k8s
        name: ${spring.application.name}-configmap

      reload:
        enabled: true
        monitoring-config-maps: true
        monitoring-secrets: true
        period: 500

      secrets:
        enable-api: true
        namespace: springcloud-k8s
        name: ${spring.application.name}-secret

management:
  endpoint:
    restart:
      enabled: true
    health:
      enabled: true
    info:
      enabled: true
```



Dockerfile

```dockerfile
FROM openjdk:20-jdk
LABEL maintainer=leixing

WORKDIR /app

#启动自行加载   服务名-prod.yml配置
#ENV PARAMS="--server.port=8080 --spring.profiles.active=prod,custom"
ENV PARAMS="--server.port=8080 --env=prod"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

COPY target/*.jar /app/app.jar
EXPOSE 8080

#
ENTRYPOINT ["/bin/sh","-c","java -Xmx1024m -Xms128m -Dfile.encoding=utf8 -Duser.timezone=Asia/Shanghai -Djava.security.egd=file:/dev/./urandom -jar app.jar ${PARAMS}"]
```



### 2.5 编译并推送镜像

在项目的根目录创建: build.cmd

```bash
@echo off
setlocal

REM 检查是否传递了参数
if "%~1"=="" (
    set "version=1.0.0"
) else (
    set "version=%~1"
)
set registryUrl=192.168.0.140:50003
set registryUsername=robot_admin
set registryPassword=rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e

echo version : %version%

cmd /c mvn clean package -Dmaven.test.skip=true
cmd /c docker login %registryUrl% -u %registryUsername% -p %registryPassword%

cmd /c docker build -t springcloud-k8s/member:%version% -f service-member/Dockerfile service-member
cmd /c docker tag springcloud-k8s/member:%version% %registryUrl%/springcloud-k8s/member:%version%
cmd /c docker push %registryUrl%/springcloud-k8s/member:%version%

endlocal
```

在项目的根目录执行

```bash
build.cmd 1.0.0
```



### 2.6 授权

在k8s的master节点上创建文件

account.yaml

```yaml
#kind: ServiceAccount
#apiVersion: v1
#metadata:
#  namespace: springcloud-k8s
#  name: default
#---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  namespace: springcloud-k8s
  name: role-app
rules:
  - apiGroups: [""]
    resources: ["configmaps", "pods", "services", "endpoints", "secrets"]
    verbs: ["get", "list", "watch"]

---

kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: rolebinding-default-role-app
  namespace: springcloud-k8s
subjects:
  - kind: ServiceAccount
    name: default
    apiGroup: ""
roleRef:
  kind: Role
  name: role-app
  apiGroup: ""
```

在k8s上运行的微服务使用的是 命名空间(springcloud-k8s)下的 default 账号, 要使用该账号获取k8s上endpoint和service等资源的信息就需要授权, 具体参考k8s的rbac , 这里作为测试,将后续可能需要的权限都授予了, 实际生产环境要根据需要设置

在k8s主节点上执行

```bash
kubectl apply -f account.yaml
```



### 2.6 创建Configmap

在k8s中的 springcloud-k8s 命名空间中创建configmap , 名为 member-config ,用于测试的值如下:

```yaml
kind: ConfigMap
apiVersion: v1
metadata:
  name: member-configmap
  namespace: springcloud-k8s
  annotations:
    kubesphere.io/creator: admin
data:
  member-custom.yml: |-
    my:
      custom:
        name: ccc
  member-prod.yml: |-
    my:
      custom:
        user: bbb
  member.yml: |-
    my:
      custom:
        url: aaa
```



### 2.7 创建服务

1 在KubeSphere平台上创建企业空间 test 和项目 springcloud-k8s

2 创建 secret, 名为 harbor, 类型为 镜像仓库密钥, 地址为 192.168.0.140:50003 ,  账号密码为 robot_admin 和 rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e ,注意这里harbor的参数要根据实际情况调整

3 创建 member 服务, 选择无状态服务, 镜像服务器选择 harbor (192.168.0.140:50003), 镜像选择 springcloud-k8s/member:1.0.0 , 选择同步主机时区.

4 其他按照默认即可



### 2.8 测试

pod都运行正常后, 在任意pod上进入容器内部执行

```bash
curl http://member.springcloud-k8s:8080/property/custom
```

输出:

```bash
CustomConfig{customProperty=CustomConfig{url='aaa', user='bbb', password='null', name='ccc'}}
```

那么说明配置读取成功, 修改configmap中的值

```yaml
kind: ConfigMap
apiVersion: v1
metadata:
  name: member-configmap
  namespace: springcloud-k8s
  annotations:
    kubesphere.io/creator: admin
data:
  member-custom.yml: |-
    my:
      custom:
        name: 333
  member-prod.yml: |-
    my:
      custom:
        user: 222
  member.yml: |-
    my:
      custom:
        url: 111
```

再次测试:

```bash
curl http://member.springcloud-k8s:8080/property/custom
```

输出

```bash
CustomConfig{customProperty=CustomConfig{url='111', user='222', password='null', name='333'}}
```

说明动态配置(热更新)生效



### 创建secret

在springcloud-k8s 命名空间下创建secret , 在KuberSphere中创建secret ,key: my.custom.password , value 为: 444

```yaml
kind: Secret
apiVersion: v1
metadata:
  name: member-secret
  namespace: springcloud-k8s
  annotations:
    kubesphere.io/creator: admin
data:
  my.custom.password: NDQ0
type: Opaque
```

其中 `data.my.custom.password` 的值是加密的, 实际原始值是 `444` ,  再次测试

```bash
curl http://member.springcloud-k8s:8080/property/custom
```

输出:

```bash
CustomConfig{customProperty=CustomConfig{url='111', user='222', password='444', name='333'}}
```

secret 可以正确读取了 ,修改secret中的 my.custom.password 为 ddd ,

```yaml
kind: Secret
apiVersion: v1
metadata:
  name: member-secret
  namespace: springcloud-k8s
  annotations:
    kubesphere.io/creator: admin
data:
  my.custom.password: ZGRk
type: Opaque
```

再次测试

```bash
curl http://member.springcloud-k8s:8080/property/custom
```

输出:

```
CustomConfig{customProperty=CustomConfig{url='111', user='222', password='ddd', name='333'}}
```



#### 加载多个secret

修改配置类: CustomProperty.java

```java
@ConfigurationProperties(prefix = "my.custom")
public class CustomProperty {
    private String url;
    private String user;
    private String password;
    private String password2;
    private String name;
    // getter setter toString 
}
```

修改项目配置文件 `application-prod.yml`  : 

```yaml
server:
  port: 8080

logging:
  level:
    root: info

spring:
  config:
    import:
      - kubernetes:member.yml
      - kubernetes:member-prod.yml
      - kubernetes:member-custom.yml
  cloud:
    kubernetes:
      discovery:
        enabled: true
        namespaces:
          - springcloud-k8s
        include-not-ready-addresses: true

      config:
        enabled: true
        namespace: springcloud-k8s
        name: ${spring.application.name}-configmap

      reload:
        enabled: true
        monitoring-config-maps: true
        monitoring-secrets: true
        period: 500

      secrets:
        enable-api: true
        namespace: springcloud-k8s
        name: ${spring.application.name}-secret
        sources:
          - name: ${spring.application.name}-secret-2
          - name: ${spring.application.name}-secret

management:
  endpoint:
    restart:
      enabled: true
    health:
      enabled: true
    info:
      enabled: true
```

这里引用2个secret

```yaml
secrets:
        enable-api: true
        namespace: springcloud-k8s
        name: ${spring.application.name}-secret
        sources:
          - name: ${spring.application.name}-secret-2
          - name: ${spring.application.name}-secret
```

在k8s集群中的命名空间`springcloud-k8s`中再创建一个`secret` : member-secret-2

```yaml
kind: Secret
apiVersion: v1
metadata:
  name: member-secret-2
  namespace: springcloud-k8s
  annotations:
    kubesphere.io/creator: admin
data:
  my.custom.password2: cXFx
type: Opaque
```

再次测试

```bash
curl http://member.springcloud-k8s:8080/property/custom
```

输出:

```bash
CustomConfig{customProperty=CustomProperty{url='111', user='222', password='ddd', password2='qqq', name='333'}}
```

可以看到 password 和 password2 的值





## 3 附: 在KubeSphere中创建的资源

harbor密钥

```yaml
kind: Secret
apiVersion: v1
metadata:
  name: harbor
  namespace: springcloud-k8s
  annotations:
    kubesphere.io/creator: admin
    secret.kubesphere.io/force-insecure: 'true'
data:
  .dockerconfigjson: >-
    eyJhdXRocyI6eyJodHRwOi8vMTkyLjE2OC4wLjE0MDo1MDAwMyI6eyJ1c2VybmFtZSI6InJvYm90X2FkbWluIiwicGFzc3dvcmQiOiJyUlNpMm5YcHo3dW55Mk9uREZXa0VQUVYyeExvRmYxZSIsImVtYWlsIjoiIiwiYXV0aCI6ImNtOWliM1JmWVdSdGFXNDZjbEpUYVRKdVdIQjZOM1Z1ZVRKUGJrUkdWMnRGVUZGV01uaE1iMFptTVdVPSJ9fX0=
type: kubernetes.io/dockerconfigjson
```



账号

```yaml
kind: ServiceAccount
apiVersion: v1
metadata:
  name: default
  namespace: springcloud-k8s
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: >
      {"apiVersion":"v1","kind":"ServiceAccount","metadata":{"annotations":{},"name":"default","namespace":"springcloud-k8s"}}
```



部署

deployment-member.yaml

```yaml
kind: Deployment
apiVersion: apps/v1
metadata:
  name: member-v1
  namespace: springcloud-k8s
  labels:
    app: member
    version: v1
  annotations:
    deployment.kubernetes.io/revision: '13'
    kubesphere.io/creator: admin
spec:
  replicas: 1
  selector:
    matchLabels:
      app: member
      version: v1
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: member
        version: v1
      annotations:
        kubesphere.io/creator: admin
        kubesphere.io/imagepullsecrets: '{"container-694nnn":"harbor"}'
        kubesphere.io/restartedAt: '2023-07-21T04:16:17.860Z'
    spec:
      volumes:
        - name: host-time
          hostPath:
            path: /etc/localtime
            type: ''
      containers:
        - name: container-694nnn
          image: '192.168.0.140:50003/springcloud-k8s/member:1.0.9'
          ports:
            - name: tcp-8080
              containerPort: 8080
              protocol: TCP
          resources: {}
          volumeMounts:
            - name: host-time
              readOnly: true
              mountPath: /etc/localtime
          terminationMessagePath: /dev/termination-log
          terminationMessagePolicy: File
          imagePullPolicy: IfNotPresent
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      dnsPolicy: ClusterFirst
      serviceAccountName: default
      serviceAccount: default
      securityContext: {}
      imagePullSecrets:
        - name: harbor
      schedulerName: default-scheduler
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 25%
      maxSurge: 25%
  revisionHistoryLimit: 10
  progressDeadlineSeconds: 600
```



服务

service-member.yaml

```yaml
kind: Service
apiVersion: v1
metadata:
  name: member
  namespace: springcloud-k8s
  labels:
    app: member
    version: v1
  annotations:
    kubesphere.io/creator: admin
    kubesphere.io/serviceType: statelessservice
spec:
  ports:
    - name: tcp-8080
      protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 31847
  selector:
    app: member
  clusterIP: 10.233.13.182
  clusterIPs:
    - 10.233.13.182
  type: NodePort
  sessionAffinity: None
  externalTrafficPolicy: Cluster
  ipFamilies:
    - IPv4
  ipFamilyPolicy: SingleStack
  internalTrafficPolicy: Cluster
```


