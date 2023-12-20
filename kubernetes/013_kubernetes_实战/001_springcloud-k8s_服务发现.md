# SpringCloud-Kubernetes 服务发现

[toc]

https://docs.spring.io/spring-cloud-kubernetes/docs/current/reference/html/

在k8s环境中使用k8s作为注册中心



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



## 2 注册中心和服务发现

这里有两个模块, member模块和order模块作为演示, member模块通过openfeign调用order模块的接口



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



### 2.2 创建模块 service-order

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

    <artifactId>service-order</artifactId>

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



OrderApplication.java

```java
@SpringBootApplication
@EnableDiscoveryClient
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```



OrderController.java

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    @GetMapping("/order")
    public String getOrder() {
        return "{order info from order-service of member xxx}";
    }
}
```



application.yml

```yaml
server:
  port: 41000

spring:
  application:
    name: order
  cloud:
    kubernetes:
      discovery:
        enabled: true
        namespaces:
          - springcloud-k8s
        include-not-ready-addresses: true
```



Dockerfile

```dockerfile
FROM openjdk:20-jdk
LABEL maintainer=leixing

#启动自行加载   服务名-prod.yml配置
ENV PARAMS="--server.port=8080 --spring.profiles.active=prod"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

COPY target/*.jar /app.jar
EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","java -Xmx1024m -Xms128m -Dfile.encoding=utf8 -Duser.timezone=Asia/Shanghai -Djava.security.egd=file:/dev/./urandom -jar /app.jar ${PARAMS}"]
```



### 2.3 创建模块 lib-k8s-order-api

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

    <artifactId>lib-k8s-order-api</artifactId>

    <properties>
        <maven.compiler.source>20</maven.compiler.source>
        <maven.compiler.target>20</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>
    </dependencies>

</project>
```



OrderApi.java

```java
@FeignClient(value = "order", contextId = "order", path = "/order")
public interface OrderApi {
    @GetMapping("/order")
    String getOrder();
}
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



MemberController.java

```java
@RestController
@RequestMapping("/member")
public class MemberController {

    private final OrderApi orderApi;

    public MemberController(OrderApi orderApi) {
        this.orderApi = orderApi;
    }

    @GetMapping("/order")
    public String getMemberOrder() {
        String order = orderApi.getOrder();
        return "member: abc, get order info from order-service: " + order;
    }
}
```



application.yml

```yaml
server:
  port: 41010

spring:
  application:
    name: member
  cloud:
    kubernetes:
      discovery:
        enabled: true
        namespaces:
          - springcloud-k8s
        include-not-ready-addresses: true
```



Dockerfile

```dockerfile
FROM openjdk:20-jdk
LABEL maintainer=leixing

#启动自行加载   服务名-prod.yml配置
ENV PARAMS="--server.port=8080 --spring.profiles.active=prod"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

COPY target/*.jar /app.jar
EXPOSE 8080

#
ENTRYPOINT ["/bin/sh","-c","java -Xmx1024m -Xms128m -Dfile.encoding=utf8 -Duser.timezone=Asia/Shanghai -Djava.security.egd=file:/dev/./urandom -jar /app.jar ${PARAMS}"]
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

cmd /c docker build -t springcloud-k8s/order:%version% -f service-order/Dockerfile service-order
cmd /c docker tag springcloud-k8s/order:%version% %registryUrl%/springcloud-k8s/order:%version%
cmd /c docker push %registryUrl%/springcloud-k8s/order:%version%

endlocal
```

在项目的根目录执行

```bash
build.cmd
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

在k8s上运行的微服务使用的是 命名空间(springcloud-k8s)下的 default 账号, 要使用该账号获取k8s上endpoint和service等资源的信息就需要授权, 具体参考k8s的rbac , 这里作为测试,将后续可能需要的权限都授予了, 实际生产环境要根据需要设置, 服务发现只需要 endpoints 和 services 的权限即可

在k8s主节点上执行

```bash
kubectl apply -f account.yaml
```



### 2.7 创建服务

1 在KubeSphere平台上创建企业空间 test 和项目 springcloud-k8s

2 创建secret, 名为 harbor, 类型为 镜像仓库密钥, 地址为 192.168.0.140:50003 ,  账号密码为 robot_admin 和 rRSi2nXpz7uny2OnDFWkEPQV2xLoFf1e ,注意这里harbor的参数要根据实际情况调整

3 创建 member / order 服务, 选择无状态服务, 镜像服务器选择 harbor (192.168.0.140:50003), 镜像选择 springcloud-k8s/member:1.0.0 和 springcloud-k8s/order:1.0.0 .  选择同步主机时区

4 其他按照默认即可



### 2.8 测试

pod都运行正常后, 在任意pod上进入容器内部执行

```bash
curl http://member.springcloud-k8s:8080/member/order
```

输出:

```bash
member: abc, get order info from order-service: {order info from order-service of member xxx}
```

那么说明远程调用成功

另外再member容器内进行测试:

```bash
sh-4.4# curl order.springcloud-k8s.svc.cluster.local:8080/order/order
{order info from order-service of member xxx}

sh-4.4# curl order.springcloud-k8s.svc:8080/order/order
{order info from order-service of member xxx}

sh-4.4# curl order.springcloud-k8s:8080/order/order
{order info from order-service of member xxx}

sh-4.4# curl order:8080/order/order
{order info from order-service of member xxx}
```

在k8s中可以通过 fully qualified domain name (FQDN) 访问服务, 格式为 `{service-name}.{namespace}.svc.{cluster-name}:{service-port}`其中:

service-name : 就是创建服务时指定的服务名, 如这里的 member / order

namespace : 是项目名, 如这里的 springcloud-k8s

cluster-name : 是集群的名称, 在 .kube 目录中的config文件中可以找到,如:

```yaml
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: LS0t...==
    server: https://lb.kubesphere.local:6443
  name: cluster.local
contexts:
- context:
    cluster: cluster.local
    user: kubernetes-admin
  name: kubernetes-admin@cluster.local
current-context: kubernetes-admin@cluster.local
kind: Config
preferences: {}
users:
- name: kubernetes-admin
  user:
    client-certificate-data: LS0tL...==
    client-key-data: LS0t...==
```

可以看到集群名为 `cluster.local`

```
clusters:
- cluster:
  name: cluster.local
```



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
    deployment.kubernetes.io/revision: '3'
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
        kubesphere.io/imagepullsecrets: '{"container-ugf0zy":"harbor"}'
        kubesphere.io/restartedAt: '2023-07-18T05:15:31.816Z'
    spec:
      volumes:
        - name: host-time
          hostPath:
            path: /etc/localtime
            type: ''
      containers:
        - name: container-ugf0zy
          image: '192.168.0.140:50003/springcloud-k8s/member:1.0.0'
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



deployment-order.yaml

```yaml
kind: Deployment
apiVersion: apps/v1
metadata:
  name: order-v1
  namespace: springcloud-k8s
  labels:
    app: order
    version: v1
  annotations:
    deployment.kubernetes.io/revision: '1'
    kubesphere.io/creator: admin
spec:
  replicas: 1
  selector:
    matchLabels:
      app: order
      version: v1
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: order
        version: v1
      annotations:
        kubesphere.io/creator: admin
        kubesphere.io/imagepullsecrets: '{"container-glrr3j":"harbor"}'
    spec:
      volumes:
        - name: host-time
          hostPath:
            path: /etc/localtime
            type: ''
      containers:
        - name: container-glrr3j
          image: '192.168.0.140:50003/springcloud-k8s/order:1.0.0'
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
  selector:
    app: member
  clusterIP: 10.233.54.196
  clusterIPs:
    - 10.233.54.196
  type: ClusterIP
  sessionAffinity: None
  ipFamilies:
    - IPv4
  ipFamilyPolicy: SingleStack
  internalTrafficPolicy: Cluster
```



service-order.yaml

```yaml
kind: Service
apiVersion: v1
metadata:
  name: order
  namespace: springcloud-k8s
  labels:
    app: order
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
  selector:
    app: order
  clusterIP: 10.233.43.69
  clusterIPs:
    - 10.233.43.69
  type: ClusterIP
  sessionAffinity: None
  ipFamilies:
    - IPv4
  ipFamilyPolicy: SingleStack
  internalTrafficPolicy: Cluster
```



