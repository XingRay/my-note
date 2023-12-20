# Heml入门

## 安装 Helm



运行以下命令安装 Helm 3

```
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
```

如果执行失败,可以使用浏览器下载 https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 下载下来是一个脚本文件,保存到本地再上传到服务器,添加执行权限

```bash
sudo chmod +x get-helm-3
```

执行脚本

```bash
./get-helm-3
```

这个脚本会从 https://github.com/helm/helm 下载最新的release版本

```bash
root@k8s-master01:~# ./get-helm-3
Helm v3.12.1 is available. Changing from version v3.9.0.
Downloading https://get.helm.sh/helm-v3.12.1-linux-amd64.tar.gz
Verifying checksum... Done.
Preparing to install helm into /usr/local/bin
helm installed into /usr/local/bin/helm
```

如果脚本执行失败,可以在浏览器中下载helm 进入 https://github.com/helm/helm/releases 下载最新版本,下载地址为:

Linux https://get.helm.sh/helm-v3.12.3-linux-amd64.tar.gz

windows: https://get.helm.sh/helm-v3.12.3-windows-amd64.zip

,解压出来有一个单独的可执行文件,复制到 /usr/local/bin ,添加可执行权限即可.

```bash
sudo cp helm /usr/local/bin/helm
sudo chmod +x /usr/local/bin/helm
```





查看 Helm 版本。

```bash
root@k8s-master01:~# helm version
version.BuildInfo{Version:"v3.12.1", GitCommit:"f32a527a060157990e2aa86bf45010dfb3cc8b8d", GitTreeState:"clean", GoVersion:"go1.20.4"}
```

备注

有关更多信息，请参见 [Helm 文档](https://helm.sh/zh/docs/intro/install/)。





安装Habor为例

1 将harbor官方仓库信息导入本机

```bash
helm repo add harbor https://helm.goharbor.io
```

```bash
helm repo list
```

```bash
root@k8s-master01:~# helm repo list
NAME    URL
harbor  https://helm.goharbor.io
```



2 获取Harbor的Chart

```bash
helm fetch harbor/harbor
```

```bash
root@k8s-master01:~# helm fetch harbor/harbor
root@k8s-master01:~# ls
harbor-1.12.2.tgz
```

```bash
tar -xf harbor-1.12.2.tgz
```

```bash
root@k8s-master01:~# tar -xf harbor-1.12.2.tgz
root@k8s-master01:~# cd harbor/
root@k8s-master01:~/harbor# ls -al
total 260
drwxr-xr-x  4 root root   4096 Jul 12 00:01 .
drwx------  9 root root   4096 Jul 12 00:01 ..
-rw-r--r--  1 root root    567 Jun  6 14:47 Chart.yaml
drwxr-xr-x  2 root root   4096 Jul 12 00:01 conf
-rw-r--r--  1 root root     57 Jun  6 14:47 .helmignore
-rw-r--r--  1 root root  11357 Jun  6 14:47 LICENSE
-rw-r--r--  1 root root 192242 Jun  6 14:47 README.md
drwxr-xr-x 15 root root   4096 Jul 12 00:01 templates
-rw-r--r--  1 root root  33874 Jun  6 14:47 values.yaml
```



3 修改配置文件

```bash
vi values.yaml
```

```yaml

```

渲染模板, 确认修改配置是否正确

```bash
helm template . 
```

返回上级目录

```bash
cd ..
```



4 将harbor安装到namespace harbor 中

```bash
heml install <app-name> <chart-name> -n <namespace>
```

```bash
heml install my-harbor harbor -n harbor
```



5 查看harbor的安装情况

```bash
helm list -n harbor
```



6 登录harbor

登录harbor, 账号 admin / Harbor12345



```bash
kubectl get pod -n harbor
```



7 上传chart到harbor

项目-library-HelmChart 上传,选择 harbor-1.12.2.tgz 文件





## 使用Helm 自制Chart

### 1 创建chart目录

```
helm create hello-chart
```

| 文件/目录   | 说明                                                      |
| ----------- | --------------------------------------------------------- |
| chart.yaml  | 声明了当前chart的名称,版本等基本信息                      |
| values.yaml | 提供应用安装时的默认参数                                  |
| templates/  | 包含应用部署所需要使用的YAML文件,如:Deployment和service等 |
| charts/     | 当前chart依赖的其他chart                                  |





```bash
root@k8s-master01:~# helm create hello-chart
Creating hello-chart
root@k8s-master01:~# cd hello-chart/
root@k8s-master01:~/hello-chart# ls
charts  Chart.yaml  templates  values.yaml
```



```bash
vi Chart.yaml
```

```properties
apiVersion: v2
name: hello-chart
description: A Helm chart for Kubernetes

# A chart can be either an 'application' or a 'library' chart.
#
# Application charts are a collection of templates that can be packaged into versioned archives
# to be deployed.
#
# Library charts provide useful utilities or functions for the chart developer. They're included as
# a dependency of application charts to inject those utilities and functions into the rendering
# pipeline. Library charts do not define any templates and therefore cannot be deployed.
type: application

# This is the chart version. This version number should be incremented each time you make changes
# to the chart and its templates, including the app version.
# Versions are expected to follow Semantic Versioning (https://semver.org/)
version: 0.1.0

# This is the version number of the application being deployed. This version number should be
# incremented each time you make changes to the application. Versions are not expected to
# follow Semantic Versioning. They should reflect the version the application is using.
# It is recommended to use it with quotes.
appVersion: "1.16.0"

```



默认配置:

```bash
 vi values.yaml
```

```properties
# Default values for hello-chart.
# This is a YAML-formatted file.
# Declare variables to be passed into your templates.

replicaCount: 1

image:
  repository: nginx
  pullPolicy: IfNotPresent
  # Overrides the image tag whose default is the chart appVersion.
  tag: ""

imagePullSecrets: []
nameOverride: ""
fullnameOverride: ""

serviceAccount:
  # Specifies whether a service account should be created
  create: true
  # Annotations to add to the service account
  annotations: {}
  # The name of the service account to use.
  # If not set and create is true, a name is generated using the fullname template
  name: ""

podAnnotations: {}

podSecurityContext: {}
  # fsGroup: 2000

securityContext: {}
  # capabilities:
  #   drop:
  #   - ALL
  # readOnlyRootFilesystem: true
  # runAsNonRoot: true
  # runAsUser: 1000

service:
  type: ClusterIP
  port: 80

ingress:
  enabled: false
  className: ""
  annotations: {}
    # kubernetes.io/ingress.class: nginx
    # kubernetes.io/tls-acme: "true"
  hosts:
    - host: chart-example.local
      paths:
        - path: /
          pathType: ImplementationSpecific
  tls: []
  #  - secretName: chart-example-tls
  #    hosts:
  #      - chart-example.local

resources: {}
  # We usually recommend not to specify default resources and to leave this as a conscious
  # choice for the user. This also increases chances charts run on environments with little
  # resources, such as Minikube. If you do want to specify resources, uncomment the following
  # lines, adjust them as necessary, and remove the curly braces after 'resources:'.
  # limits:
  #   cpu: 100m
  #   memory: 128Mi
  # requests:
  #   cpu: 100m
  #   memory: 128Mi

autoscaling:
  enabled: false
  minReplicas: 1
  maxReplicas: 100
  targetCPUUtilizationPercentage: 80
  # targetMemoryUtilizationPercentage: 80

nodeSelector: {}

tolerations: []

affinity: {}

```

可以修改这个配置



```bash
root@k8s-master01:~/hello-chart# cd templates/
root@k8s-master01:~/hello-chart/templates# ls
deployment.yaml  _helpers.tpl  hpa.yaml  ingress.yaml  NOTES.txt  serviceaccount.yaml  service.yaml  tests
```

```bash
vi deployment.yaml
```

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "hello-chart.fullname" . }}
  labels:
    {{- include "hello-chart.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "hello-chart.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      {{- with .Values.podAnnotations }}
      annotations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      labels:
        {{- include "hello-chart.selectorLabels" . | nindent 8 }}
    spec:
      {{- with .Values.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      serviceAccountName: {{ include "hello-chart.serviceAccountName" . }}
      securityContext:
        {{- toYaml .Values.podSecurityContext | nindent 8 }}
      containers:
        - name: {{ .Chart.Name }}
          securityContext:
            {{- toYaml .Values.securityContext | nindent 12 }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: {{ .Values.service.port }}
              protocol: TCP
          livenessProbe:
            httpGet:
              path: /
              port: http
          readinessProbe:
            httpGet:
              path: /
              port: http
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
      {{- with .Values.nodeSelector }}
      nodeSelector:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.affinity }}
      affinity:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.tolerations }}
      tolerations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
```



```bash
cd ..
```



### 2 渲染chart并输出

```bash
root@k8s-master01:~/hello-chart# ls
charts  Chart.yaml  templates  values.yaml
```

```bash
helm template <work-dir>
```

```bash
helm template .
```

```yaml
---
# Source: hello-chart/templates/serviceaccount.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: release-name-hello-chart
  labels:
    helm.sh/chart: hello-chart-0.1.0
    app.kubernetes.io/name: hello-chart
    app.kubernetes.io/instance: release-name
    app.kubernetes.io/version: "1.16.0"
    app.kubernetes.io/managed-by: Helm
---
# Source: hello-chart/templates/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: release-name-hello-chart
  labels:
    helm.sh/chart: hello-chart-0.1.0
    app.kubernetes.io/name: hello-chart
    app.kubernetes.io/instance: release-name
    app.kubernetes.io/version: "1.16.0"
    app.kubernetes.io/managed-by: Helm
spec:
  type: ClusterIP
  ports:
    - port: 80
      targetPort: http
      protocol: TCP
      name: http
  selector:
    app.kubernetes.io/name: hello-chart
    app.kubernetes.io/instance: release-name
---
# Source: hello-chart/templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: release-name-hello-chart
  labels:
    helm.sh/chart: hello-chart-0.1.0
    app.kubernetes.io/name: hello-chart
    app.kubernetes.io/instance: release-name
    app.kubernetes.io/version: "1.16.0"
    app.kubernetes.io/managed-by: Helm
spec:
  replicas: 1
  selector:
    matchLabels:
      app.kubernetes.io/name: hello-chart
      app.kubernetes.io/instance: release-name
  template:
    metadata:
      labels:
        app.kubernetes.io/name: hello-chart
        app.kubernetes.io/instance: release-name
    spec:
      serviceAccountName: release-name-hello-chart
      securityContext:
        {}
      containers:
        - name: hello-chart
          securityContext:
            {}
          image: "nginx:1.16.0"
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 80
              protocol: TCP
          livenessProbe:
            httpGet:
              path: /
              port: http
          readinessProbe:
            httpGet:
              path: /
              port: http
          resources:
            {}
---
# Source: hello-chart/templates/tests/test-connection.yaml
apiVersion: v1
kind: Pod
metadata:
  name: "release-name-hello-chart-test-connection"
  labels:
    helm.sh/chart: hello-chart-0.1.0
    app.kubernetes.io/name: hello-chart
    app.kubernetes.io/instance: release-name
    app.kubernetes.io/version: "1.16.0"
    app.kubernetes.io/managed-by: Helm
  annotations:
    "helm.sh/hook": test
spec:
  containers:
    - name: wget
      image: busybox
      command: ['wget']
      args: ['release-name-hello-chart:80']
  restartPolicy: Never
```



```bash
cd ..
```



### 3 安装chart

安装hello-chart

```bash
root@k8s-master01:~# ls
hello-chart
```

```bash
helm install first-chart hello-chart/ -n default
```



显示

```bash
helm list -n <namespace>
```

```bash
helm list -n default
```

```
root@k8s-master01:~# helm list -n default
NAME            NAMESPACE       REVISION        UPDATED                                 STATUS          CHART                   APP VERSION
first-chart     default         1               2023-07-12 00:36:40.490774884 +0800 CST deployed        hello-chart-0.1.0       1.16.0
```

```bash
root@k8s-master01:~# kubectl get pod -n default
NAME                                       READY   STATUS    RESTARTS   AGE
first-chart-hello-chart-848c44d649-9l56w   1/1     Running   0          117s
```

```bash
root@k8s-master01:~# kubectl get service -n default
NAME                      TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)   AGE
first-chart-hello-chart   ClusterIP   10.233.17.21   <none>        80/TCP    5m4s
kubernetes                ClusterIP   10.233.0.1     <none>        443/TCP   26h
```



修改 chart 配置文件之后 ,应用更新

```bash
helm upgrade first-chart hello-chart -n default
```



卸载

```bash
helm uninstall first-chart -n default
```



### 4 打包chart

开发完成后打包

```
helm package hello-chart
```

```bash
root@k8s-master01:~# helm package hello-chart
Successfully packaged chart and saved it to: /root/hello-chart-0.1.0.tgz
root@k8s-master01:~# ls
hello-chart  hello-chart-0.1.0.tgz
```



### 5 推送

将chart推送至应用仓库,使用harbor上传即可



