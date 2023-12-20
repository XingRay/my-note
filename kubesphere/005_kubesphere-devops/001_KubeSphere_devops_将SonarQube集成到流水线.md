# KubeSphere-DevOps 将 SonarQube 集成到流水线

官方文档:

https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-integrate/sonarqube/



[SonarQube](https://www.sonarqube.org/) 是一种主流的代码质量持续检测工具。您可以将其用于代码库的静态和动态分析。SonarQube 集成到 KubeSphere 流水线后，如果在运行的流水线中检测到问题，您可以直接在仪表板上查看常见代码问题，例如 Bug 和漏洞。

本教程演示如何将 SonarQube 集成到流水线中。在[使用 Jenkinsfile 创建流水线](https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-use/pipelines/create-a-pipeline-using-jenkinsfile/)之前，请先参考以下步骤。



## 1 准备工作

您需要[启用 KubeSphere DevOps 系统](https://www.kubesphere.io/zh/docs/v3.3/pluggable-components/devops/)。



## 2 安装 SonarQube 服务器

要将 SonarQube 集成到您的流水线，必须先安装 SonarQube 服务器。

### 2.1 请先安装 Helm，以便后续使用该工具安装 SonarQube。例如，运行以下命令安装 Helm 3：

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

https://get.helm.sh/helm-v3.12.1-linux-amd64.tar.gz ,解压出来有一个单独的可执行文件,复制到 /usr/local/bin ,添加可执行权限即可.

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



### 2.2 执行以下命令安装 SonarQube 服务器。

```bash
helm upgrade --install sonarqube sonarqube --repo https://charts.kubesphere.io/main -n kubesphere-devops-system  --create-namespace --set service.type=NodePort
```

备注

请您确保使用 Helm 3 安装 SonarQube Server。--set service.type=NodePort  这样会暴露服务到公网 只能在测试环境下这样使用.



### 2.3 您会获取以下提示内容：

```bash
root@k8s-master01:~# helm upgrade --install sonarqube sonarqube --repo https://charts.kubesphere.io/main -n kubesphere-devops-system  --create-namespace --set service.type=NodePort
Release "sonarqube" does not exist. Installing it now.
NAME: sonarqube
LAST DEPLOYED: Tue Jul 11 00:45:05 2023
NAMESPACE: kubesphere-devops-system
STATUS: deployed
REVISION: 1
NOTES:
1. Get the application URL by running these commands:
  export NODE_PORT=$(kubectl get --namespace kubesphere-devops-system -o jsonpath="{.spec.ports[0].nodePort}" services sonarqube-sonarqube)
  export NODE_IP=$(kubectl get nodes --namespace kubesphere-devops-system -o jsonpath="{.items[0].status.addresses[0].address}")
  echo http://$NODE_IP:$NODE_PORT
```



## 3 获取 SonarQube 控制台地址

### 3.1 执行以下命令以获取 SonarQube NodePort。

```
export NODE_PORT=$(kubectl get --namespace kubesphere-devops-system -o jsonpath="{.spec.ports[0].nodePort}" services sonarqube-sonarqube)
export NODE_IP=$(kubectl get nodes --namespace kubesphere-devops-system -o jsonpath="{.items[0].status.addresses[0].address}")
echo http://$NODE_IP:$NODE_PORT
```



### 3.2 您可以获得如下输出（本示例中端口号为 `31377`，可能与您的端口号不同）：

```bash
http://192.168.0.151:30424
```



## 4 配置 SonarQube 服务器

### 4.1 访问 SonarQube 控制台

1 查看 SonarQube 的状态

执行以下命令查看 SonarQube 的状态。请注意，只有在 SonarQube 启动并运行后才能访问 SonarQube 控制台。

```bash
kubectl get pod -n kubesphere-devops-system
```

```bash
root@k8s-master01:~# kubectl get pod -n kubesphere-devops-system
NAME                                  READY   STATUS      RESTARTS       AGE
devops-28150080-hh5rh                 0/1     Completed   0              83m
devops-28150110-gkc55                 0/1     Completed   0              53m
devops-28150140-mcc4l                 0/1     Completed   0              23m
devops-apiserver-b88d4ddbf-rsf84      1/1     Running     0              127m
devops-controller-95bd45bbb-kfwrt     1/1     Running     0              127m
devops-jenkins-86b97d5754-h4mqn       1/1     Running     1 (122m ago)   127m
s2ioperator-0                         1/1     Running     0              127m
sonarqube-postgresql-0                1/1     Running     0              38m
sonarqube-sonarqube-c7d797894-4mvnr   1/1     Running     0              4m42s
```

如果sonarkube无法正常启动,可能是由于没有调度到合适的节点,这时可以在 集群-应用负载-工作负载 中找到或者搜索 sonarqube-sonarqube ,点击 ... 点击重新创建即可.



2 在浏览器中访问 SonarQube 控制台 `http://<Node IP>:<NodePort>`。如 http://192.168.0.151:30424



3 点击右上角的 **Log in**，然后使用默认帐户 `admin/admin` 登录。首次登录时要求修改密码,这里修改为 admin / 123456

备注

取决于您的实例的部署位置，您可能需要设置必要的端口转发规则，并在您的安全组中放行该端口，以便访问 SonarQube。



### 4.2：创建 SonarQube 管理员令牌 (Token)

1 点击右上角字母 **A**，然后从菜单中选择 **My Account** 以转到 **Profile** 页面。

![SonarQube 配置-1](assets/001_KubeSphere_devops_接入SonarQube/sonarqube-config-1.png)



2 点击 **Security** 并输入令牌名称，例如 `kubesphere`。

![SonarQube 配置-2](assets/001_KubeSphere_devops_接入SonarQube/sonarqube-config-2.png)



3 点击 **Generate** 并复制此令牌。

![SonarQube 配置-3](assets/001_KubeSphere_devops_接入SonarQube/sonarqube-config-3.png)

警告

如提示所示，您无法再次查看此令牌，因此请确保复制成功。这里是 

```bash
New token "kubesphere" has been created. Make sure you copy it now, you won't be able to see it again!
```

```bash
8630e8c2395a8a50a6d0d5c4098c644616ce09ad
```



### 4.3 创建 Webhook 服务器

1 执行以下命令获取 SonarQube Webhook 的地址。

```bash
export NODE_PORT=$(kubectl get --namespace kubesphere-devops-system -o jsonpath="{.spec.ports[0].nodePort}" services devops-jenkins)
export NODE_IP=$(kubectl get nodes --namespace kubesphere-devops-system -o jsonpath="{.items[0].status.addresses[0].address}")
echo http://$NODE_IP:$NODE_PORT/sonarqube-webhook/
```



2 预期输出结果：

```bash
root@k8s-master01:~# export NODE_PORT=$(kubectl get --namespace kubesphere-devops-system -o jsonpath="{.spec.ports[0].nodePort}" services sonarqube-sonarqube)
export NODE_IP=$(kubectl get nodes --namespace kubesphere-devops-system -o jsonpath="{.items[0].status.addresses[0].address}")
echo http://$NODE_IP:$NODE_PORT
http://192.168.0.151:30424
root@k8s-master01:~# export NODE_PORT=$(kubectl get --namespace kubesphere-devops-system -o jsonpath="{.spec.ports[0].nodePort}" services devops-jenkins)
export NODE_IP=$(kubectl get nodes --namespace kubesphere-devops-system -o jsonpath="{.items[0].status.addresses[0].address}")
echo http://$NODE_IP:$NODE_PORT/sonarqube-webhook/
http://192.168.0.151:30180/sonarqube-webhook/
```

webhook地址为:

```
http://192.168.0.151:30180/sonarqube-webhook/
```



3 依次点击 **Administration**、**Configuration** 和 **Webhooks** 创建一个 Webhook。

![SonarQube Webhook-1](assets/001_KubeSphere_devops_接入SonarQube/sonarqube-webhook-1.png)



4 点击 **Create**。

![SonarQube Webhook-2](assets/001_KubeSphere_devops_接入SonarQube/sonarqube-webhook-2.png)



5 在弹出的对话框中输入 **Name** 和 **Jenkins Console URL**（即 SonarQube Webhook 地址）。点击 **Create** 完成操作。

![Webhook 页面信息](assets/001_KubeSphere_devops_接入SonarQube/webhook-page-info.png)



name:  kubesphere-jenkins

URL: http://192.168.0.151:30180/sonarqube-webhook/

### 4.4 将 SonarQube 配置添加到 ks-installer

1 执行以下命令编辑 `ks-installer`。

```
kubectl edit cc -n kubesphere-system ks-installer
```



2 搜寻至 `devops`。添加字段 `sonarqube` 并在其下方指定 `externalSonarUrl` 和 `externalSonarToken`。

```yaml
devops:
  enabled: true
  jenkinsJavaOpts_MaxRAM: 2g
  jenkinsJavaOpts_Xms: 512m
  jenkinsJavaOpts_Xmx: 512m
  jenkinsMemoryLim: 2Gi
  jenkinsMemoryReq: 1500Mi
  jenkinsVolumeSize: 8Gi
  sonarqube: # Add this field manually.
    externalSonarUrl: http://192.168.0.151:30424 # The SonarQube IP address.
    externalSonarToken: 8630e8c2395a8a50a6d0d5c4098c644616ce09ad # The SonarQube admin token created above.
```

```yaml
  # 手动添加这条记录
  sonarqube:
    # 配置sonarQube地址,
    externalSonarUrl: http://192.168.0.151:30424
    # 配置token,就是前面生成的token
    externalSonarToken: 8630e8c2395a8a50a6d0d5c4098c644616ce09ad
```



3 完成操作后保存此文件。



### 4.5 将 SonarQube 服务器添加至 Jenkins

1 执行以下命令获取 Jenkins 的地址。

```
export NODE_PORT=$(kubectl get --namespace kubesphere-devops-system -o jsonpath="{.spec.ports[0].nodePort}" services devops-jenkins)
export NODE_IP=$(kubectl get nodes --namespace kubesphere-devops-system -o jsonpath="{.items[0].status.addresses[0].address}")
echo http://$NODE_IP:$NODE_PORT
```

```bash
root@k8s-master01:~# kubectl edit cc -n kubesphere-system ks-installer
clusterconfiguration.installer.kubesphere.io/ks-installer edited
root@k8s-master01:~# export NODE_PORT=$(kubectl get --namespace kubesphere-devops-system -o jsonpath="{.spec.ports[0].nodePort}" services devops-jenkins)
export NODE_IP=$(kubectl get nodes --namespace kubesphere-devops-system -o jsonpath="{.items[0].status.addresses[0].address}")
echo http://$NODE_IP:$NODE_PORT
http://192.168.0.151:30180
```

2 您可以获得以下输出，获取 Jenkins 的端口号。

```bash
http://192.168.0.151:30180
```



3 请使用地址 `http://<Node IP>:30180` 访问 Jenkins。安装 KubeSphere 时，默认情况下也会安装 Jenkins 仪表板。此外，Jenkins 还配置有 KubeSphere LDAP，这意味着您可以直接使用 KubeSphere 帐户（例如 `admin/P@88w0rd`）登录 Jenkins。有关配置 Jenkins 的更多信息，请参见 [Jenkins 系统设置](https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-use/pipelines/jenkins-setting/)。

备注

取决于您的实例的部署位置，您可能需要设置必要的端口转发规则，并在您的安全组中放行端口 `30180`，以便访问 Jenkins。

使用KubeSphere默认账号密码(KubeSphere账号密码修改了,这里Jenkins也还是使用这个密码) `admin/P@88w0rd` 登录Jenkins



4 点击左侧导航栏中的**系统管理**。



5 向下翻页找到并点击**系统配置**。



6 搜寻到 **SonarQube servers**，然后点击 **Add SonarQube**。



7 输入 **Name** 和 **Server URL** (`http://<Node IP>:<NodePort>`)  http://192.168.0.151:30424  。点击**添加**，选择 **Jenkins**，然后在弹出的对话框中用 SonarQube 管理员令牌创建凭证（如下方第二张截图所示）。创建凭证后，从 **Server authentication token** 旁边的下拉列表中选择该凭证。点击 **保存** 完成操作。

![sonarqube-jenkins-settings](assets/001_KubeSphere_devops_接入SonarQube/sonarqube-jenkins-settings.png)

![add-credentials](assets/001_KubeSphere_devops_接入SonarQube/add-credentials.png)

备注

如果点击**添加**按钮无效（Jenkins 已知问题），您可以前往**系统管理**下的 **Manage Credentials** 并点击 **Stores scoped to Jenkins** 下的 **Jenkins**，再点击**全局凭据 (unrestricted)**，然后点击左侧导航栏的**添加凭据**，参考上方第二张截图用 SonarQube 管理员令牌添加凭证。添加凭证后，从 **Server authentication token** 旁边的下拉列表中选择该凭证。

![image-20230711020132575](assets/001_KubeSphere_devops_将SonarQube集成到流水线/image-20230711020132575.png)

这里将secret设置为前面SonarQube生成的Token,这里是 

```bash
8630e8c2395a8a50a6d0d5c4098c644616ce09ad
```





### 4.6 将 sonarqubeURL 添加到 KubeSphere 控制台

您需要指定 `sonarqubeURL`，以便可以直接从 KubeSphere 控制台访问 SonarQube。

1 执行以下命令：

```bash
kubectl edit  cm -n kubesphere-system  ks-console-config
```



2 搜寻到 `data.client.enableKubeConfig`，在下方添加 `devops` 字段并指定 `sonarqubeURL`。

```
client:
  enableKubeConfig: true
  devops: # 手动添加该字段。
    sonarqubeURL: http://192.168.0.151:30424 # SonarQube IP 地址。
```



3 保存该文件。



### 4.7 重启服务

执行以下命令。

```
kubectl -n kubesphere-devops-system rollout restart deploy devops-apiserver
kubectl -n kubesphere-system rollout restart deploy ks-console
```



## 5 为新项目创建 SonarQube Token

您需要一个 SonarQube 令牌，以便您的流水线可以在运行时与 SonarQube 通信。



1 在 SonarQube 控制台上 http://192.168.0.151:30424 ，点击 **Add a project**。

![image-20230711021612098](assets/001_KubeSphere_devops_将SonarQube集成到流水线/image-20230711021612098.png)

选择手动创建

![image-20230711021728515](assets/001_KubeSphere_devops_将SonarQube集成到流水线/image-20230711021728515.png)



2 输入项目密钥，例如 `java-demo`，然后点击 **Set Up**。

![Jenkins 项目密钥](assets/001_KubeSphere_devops_接入SonarQube/jenkins-projet-key.png)



3 输入项目名称，例如 `java-sample`，然后点击 **Generate**。

![创建令牌](assets/001_KubeSphere_devops_接入SonarQube/generate-a-token.png)

```bash
Provide a token
java-sample: d8d2ba26f7fcc18cc2b0762f767265f1d9c50c1c
The token is used to identify you when an analysis is performed. If it has been compromised, you can revoke it at any point of time in your user account.
```



4 创建令牌后，点击 **Continue**。

![令牌已创建](assets/001_KubeSphere_devops_接入SonarQube/token-created.png)



5 分别选择 **Maven**。复制下图所示绿色框中的序列号，如果要在流水线中使用，则需要在[凭证](https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-use/devops-settings/credential-management/#创建凭证)中添加此序列号。

![image-20230711022115983](assets/001_KubeSphere_devops_将SonarQube集成到流水线/image-20230711022115983.png)

```bash
mvn sonar:sonar \
  -Dsonar.projectKey=java-demo \
  -Dsonar.host.url=http://192.168.0.151:30424 \
  -Dsonar.login=d8d2ba26f7fcc18cc2b0762f767265f1d9c50c1c
```



## 6 创建项目代码仓库

fork项目

登录gitlab https://gitlab.com/

进入项目 https://gitlab.com/ks-devops/devops-maven-sample ,点击 fork, 选择自己账号的命名空间即可.



创建凭证

进入刚fork好的项目空间, 进入 settings - access tokens ,

Add a project access token

token name :  maven-sample

Select a role : Developer

Select scopes: [√]read api



点击 create project access token

会自动创建一个token

这里是 

```bash
glpat-71smsVbcWTenfbFkxXE4
```

点击copy按钮,保存到本地.



添加访问ssh key 创建 

https://gitlab.com/-/profile/keys

添加本地的ssh-key



下载源码

将项目clone到本地

```bash
git clone git@gitlab.com:leixing/devops-maven-sample.git
```





## 7 在KubeSphere中创建测试项目的企业空间



创建企业空间

企业空间 - 创建 - 基本信息

```bash
名称 test-company
别名 test.com
管理员 admin
描述 测试项目企业空间
```



创建devops项目

企业空间 - devops项目 - 创建 

```
名称 devops-maven-sample
别名 sample
描述 测试项目
```



项目设置

创建凭证





创建 SonarQube 凭证

devops-maven-sample - DevOps项目项目设置 - 凭证  点击创建

创建凭证

```
名称 : sonar-token
类型 : 访问令牌
令牌 : d8d2ba26f7fcc18cc2b0762f767265f1d9c50c1c
描述 : maven-sample SonarQube 访问令牌
```





创建流水线

进入 devops-maven-sample 项目, 流水线-创建

基本信息

```bash
名称 pipeline-maven-sample
DevOps项目 devops-maven-sample
描述 maven-sample 测试项目流水线
```



选择代码仓库

点击  代码仓库 -  选择流水线使用的代码仓库

选择代码仓库

点击 gitlab 

```bash
Gitlab 服务器地址 https://gitlab.com
项目组/所有者 leixing
凭证
代码仓库 leixing/devops-maven-sample
```

点击 `√`,

点击`下一步`



高级设置

脚本路径

这里根据项目实际的`Jenkinsfile`的路径及文件名设置,

路径 Jenkins-online





通过Jenkinsfile创建流水线 :

```groovy
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
          git(url: 'https://gitlab.com/leixing/devops-maven-sample.git', credentialsId: 'gitlab-account', branch: 'sonarqube', changelog: true, poll: false)
          sh 'ls -al'
        }
      }
    }

    stage('sonarqube analysis') {
      steps {
        container('maven') {
          withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
            withSonarQubeEnv('sonar') {
                // 先编译,再执行代码质量检查,如果前面执行过单元测试,这里就不需要编译
              sh "mvn compile sonar:sonar -gs `pwd`/configuration/settings.xml -Dsonar.login=$SONAR_TOKEN"
            }

          }

          timeout(time: 1, unit: 'HOURS') {
            waitForQualityGate true
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
    DOCKERHUB_NAMESPACE = 'johnniang'
    GITHUB_ACCOUNT = 'kubesphere'
    APP_NAME = 'devops-java-sample'
    SONAR_CREDENTIAL_ID = 'sonar-token'
    SONAR_TOKEN = 'd8d2ba26f7fcc18cc2b0762f767265f1d9c50c1c'
  }
  
  parameters {
    string(name: 'TAG_NAME', defaultValue: '', description: '')
  }
}
```



运行测试流水线







# 使用 Jenkinsfile 创建流水线

Jenkinsfile 是一个文本文件，它包含 Jenkins 流水线的定义，并被检入源代码控制仓库。Jenkinsfile 将整个工作流存储为代码，因此它是代码审查和流水线迭代过程的基础。有关更多信息，请参见 [Jenkins 官方文档](https://www.jenkins.io/zh/doc/book/pipeline/jenkinsfile/)。

本教程演示如何基于 GitHub 仓库中的 Jenkinsfile 创建流水线。您可以使用该流水线将示例应用程序分别部署到可从外部访问的开发环境和生产环境。

备注

KubeSphere 中可以创建两种类型的流水线：一种是本教程中介绍的基于 SCM 中 Jenkinsfile 创建的流水线，另一种是[通过图形编辑面板创建的流水线](https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-use/pipelines/create-a-pipeline-using-graphical-editing-panel/)。Jenkinsfile in SCM 需要源代码管理 (SCM) 中有内置 Jenkinsfile，换句话说，Jenkinsfile 作为 SCM 的一部分。KubeSphere DevOps 系统会根据代码仓库的现有 Jenkinsfile 自动构建 CI/CD 流水线。您可以定义工作流，例如 `stage` 和 `step`。

## 准备工作

- 您需要有一个 [Docker Hub](https://hub.docker.com/) 帐户和一个 [GitHub](https://github.com/) 帐户。
- 您需要[启用 KubeSphere DevOps 系统](https://www.kubesphere.io/zh/docs/v3.3/pluggable-components/devops/)。
- 您需要创建一个企业空间、一个 DevOps 项目和一个用户 (`project-regular`)，需要邀请该用户至 DevOps 项目中并赋予 `operator` 角色。如果尚未准备就绪，请参见[创建企业空间、项目、用户和角色](https://www.kubesphere.io/zh/docs/v3.3/quick-start/create-workspace-and-project/)。
- 您需要设置 CI 专用节点用于运行流水线。请参考[为依赖项缓存设置 CI 节点](https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-use/devops-settings/set-ci-node/)。
- 您需要安装和配置 SonarQube。请参考[将 SonarQube 集成到流水线](https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-integrate/sonarqube/)。如果您跳过这一部分，则没有下面的 **SonarQube 分析**阶段。

## 流水线概述

本示例流水线包括以下八个阶段。

![流水线概览](assets/001_KubeSphere_devops_将SonarQube集成到流水线/pipeline-overview.png)

备注

- **阶段 1：Checkout SCM**：从 GitHub 仓库检出源代码。
- **阶段 2：单元测试**：待该测试通过后才会进行下一阶段。
- **阶段 3：SonarQube 分析**：SonarQube 代码质量分析。
- **阶段 4：构建并推送快照镜像**：根据**策略设置**中选定的分支来构建镜像，并将 `SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER` 标签推送至 Docker Hub，其中 `$BUILD_NUMBER` 为流水线活动列表中的运行序号。
- **阶段 5：推送最新镜像**：将 SonarQube 分支标记为 `latest`，并推送至 Docker Hub。
- **阶段 6：部署至开发环境**：将 SonarQube 分支部署到开发环境，此阶段需要审核。
- **阶段 7：带标签推送**：生成标签并发布到 GitHub，该标签会推送到 Docker Hub。
- **阶段 8：部署至生产环境**：将已发布的标签部署到生产环境。

## 动手实验

### 步骤 1：创建凭证

1. 以 `project-regular` 身份登录 KubeSphere 控制台。转到您的 DevOps 项目，在 **DevOps 项目设置**下的**凭证**页面创建以下凭证。有关如何创建凭证的更多信息，请参见[凭证管理](https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-use/devops-settings/credential-management/)。

   备注

   如果您的帐户或密码中包含任何特殊字符，例如 `@` 和 `$`，可能会因为无法识别而在流水线运行时导致错误。在这种情况下，您需要先在一些第三方网站（例如 [urlencoder](https://www.urlencoder.org/)）上对帐户或密码进行编码，然后将输出结果复制粘贴作为您的凭证信息。

   | 凭证 ID         | 类型         | 用途       |
   | :-------------- | :----------- | :--------- |
   | dockerhub-id    | 用户名和密码 | Docker Hub |
   | github-id       | 用户名和密码 | GitHub     |
   | demo-kubeconfig | kubeconfig   | Kubernetes |

2. 您还需要为 SonarQube 创建一个凭证 (`sonar-token`)，用于上述的阶段 3（SonarQube 分析）。请参阅[为新项目创建 SonarQube 令牌 (Token)](https://www.kubesphere.io/zh/docs/v3.3/devops-user-guide/how-to-integrate/sonarqube/#为新项目创建-sonarqube-token)，在**访问令牌**类型的凭证的**令牌**字段中输入 SonarQube 令牌。点击**确定**完成操作。

3. 您还需要创建具有如下图所示权限的 GitHub 个人访问令牌 (PAT)，然后在 DevOps 项目中，使用生成的令牌创建用于 GitHub 认证的帐户凭证（例如，`github-token`）。

   ![github-token-scope](assets/001_KubeSphere_devops_将SonarQube集成到流水线/github-token-scope.png)

   备注

   如需创建 GitHub 个人访问令牌，请转到您 GitHub 帐户的 **Settings**，点击 **Developer settings**，选择 **Personal access tokens**，然后点击 **Generate new token**。

4. 您可以在列表页中看到已创建的五个凭证。

### 步骤 2：在 GitHub 仓库中修改 Jenkinsfile

1. 登录 GitHub 并 Fork GitHub 仓库 [devops-maven-sample](https://github.com/kubesphere/devops-maven-sample) 至您的 GitHub 个人帐户。

2. 在您自己的 GitHub 仓库 **devops-maven-sample** 中，点击根目录中的文件 `Jenkinsfile-online`。

3. 点击右侧的编辑图标，编辑环境变量。

   ![jenkins-edit--2](assets/001_KubeSphere_devops_将SonarQube集成到流水线/jenkins-edit--2.png)

   | 条目                     | 值                     | 描述信息                                                     |
   | :----------------------- | :--------------------- | :----------------------------------------------------------- |
   | DOCKER_CREDENTIAL_ID     | dockerhub-id           | 您在 KubeSphere 中为 Docker Hub 帐户设置的**名称**。         |
   | GITHUB_CREDENTIAL_ID     | github-id              | 您在 KubeSphere 中为 GitHub 帐户设置的**名称**，用于将标签推送至您的 GitHub 仓库。 |
   | KUBECONFIG_CREDENTIAL_ID | demo-kubeconfig        | 您在 KubeSphere 中为 kubeconfig 设置的**名称**，用于访问运行中的 Kubernetes 集群。 |
   | REGISTRY                 | docker.io              | 默认为 `docker.io`，用作推送镜像的地址。                     |
   | DOCKERHUB_NAMESPACE      | your-dockerhub-account | 请替换为您的 Docker Hub 帐户名，也可以替换为该帐户下的 Organization 名称。 |
   | GITHUB_ACCOUNT           | your-github-account    | 请替换为您的 GitHub 帐户名。例如，如果您的 GitHub 地址是 `https://github.com/kubesphere/`，则您的 GitHub 帐户名为 `kubesphere`，也可以替换为该帐户下的 Organization 名称。 |
   | APP_NAME                 | devops-maven-sample    | 应用名称。                                                   |
   | SONAR_CREDENTIAL_ID      | sonar-token            | 您在 KubeSphere 中为 SonarQube 令牌设置的**名称**，用于代码质量检测。 |

   备注

   Jenkinsfile 中 `mvn` 命令的参数 `-o` 表示开启离线模式。本教程中已下载相关依赖项，以节省时间并适应某些环境中的网络干扰。离线模式默认开启。

4. 编辑环境变量后，点击页面底部的 **Commit changes**，更新 SonarQube 分支中的文件。

### 步骤 3：创建项目

您需要创建两个项目，例如 `kubesphere-sample-dev` 和 `kubesphere-sample-prod`，分别代表开发环境和生产环境。待流水线成功运行，将在这两个项目中自动创建应用程序的相关部署 (Deployment) 和服务 (Service)。

备注

您需要提前创建 `project-admin` 帐户，用作 CI/CD 流水线的审核者。有关更多信息，请参见[创建企业空间、项目、用户和角色](https://www.kubesphere.io/zh/docs/v3.3/quick-start/create-workspace-and-project/)。

1. 以 `project-admin` 身份登录 KubeSphere。在您创建 DevOps 项目的企业空间中创建以下两个项目。请确保邀请 `project-regular` 帐户至这两个项目中并赋予 `operator` 角色。

   | 项目名称               | 别名                    |
   | :--------------------- | :---------------------- |
   | kubesphere-sample-dev  | development environment |
   | kubesphere-sample-prod | production environment  |

2. 项目创建后，会显示在项目列表中。

### 步骤 4：创建流水线

1. 登出 KubeSphere，然后以 `project-regular` 身份重新登录，转到 DevOps 项目 `demo-devops`，点击**创建**。

2. 在弹出的对话框中填入基本信息，将其命名为 `jenkinsfile-in-scm` 并在**代码仓库**下指定一个代码仓库。

3. 在 **GitHub** 选项卡，从**凭证**的下拉菜单中选择 **github-token**，然后点击**确定**来选择您的仓库。

4. 选择您的 GitHub 帐户，与该令牌相关的所有仓库将在右侧列出。选择 **devops-maven-sample** 并点击**选择**，点击**下一步**继续。

5. 在**高级设置**中，选中**删除旧分支**旁边的方框。本教程中，您可以为**分支保留天数（天）\**和\**分支最大数量**使用默认值。

   删除旧分支意味着您将一并丢弃分支记录。分支记录包括控制台输出、已归档制品以及特定分支的其他相关元数据。更少的分支意味着您可以节省 Jenkins 正在使用的磁盘空间。KubeSphere 提供两个选项来确定何时丢弃旧分支：

   - 分支保留天数（天）：超过保留期限的分支将被删除。
   - 分支最大数量：分支数量超过最大数量时，删除最旧的分支。

   备注

   **分支保留天数（天）\**和\**分支最大数量**可以同时应用于分支。只要某个分支满足其中一个字段所设置的条件，则会删除该分支。例如，如果您将保留天数和最大分支数分别指定为 2 和 3，待某个分支的保留天数超过 2 或者分支保留数量超过 3，则会删除该分支。KubeSphere 默认用 7 和 5 预填充这两个字段。

6. 在**策略设置**中，KubeSphere 默认提供四种策略。本示例中不会使用**从 Fork 仓库中发现 PR** 这条策略，因此您可以删除该策略。您无需修改设置，可以直接使用默认值。

   Jenkins 流水线运行时，开发者提交的 Pull Request (PR) 也将被视为一个单独的分支。

   **发现分支**

   - **排除已提交 PR 的分支**：不扫描源分支，例如源仓库的 master 分支。需要合并这些分支。
   - **只包括已提交 PR 的分支**：仅扫描 PR 分支。
   - **包括所有分支**：拉取源仓库中的所有分支。

   **从原仓库发现 PR**

   - **拉取 PR 合并后的代码**：PR 合并到目标分支后，基于源代码创建并运行流水线。
   - **拉取 PR 提交时的代码**：根据 PR 本身的源代码创建并运行流水线。
   - **分别创建两个流水线**：KubeSphere 创建两个流水线，一个流水线使用 PR 与目标分支合并后的源代码版本，另一个使用 PR 本身的源代码版本。

   备注

   您需要选择 GitHub 作为代码仓库才能启用此处的**策略设置**设置。

7. 向下滚动到**脚本路径**。该字段指定代码仓库中的 Jenkinsfile 路径。它表示仓库的根目录。如果文件位置变更，则脚本路径也需要更改。请将其更改为 `Jenkinsfile-online`，这是示例仓库中位于根目录下的 Jenkinsfile 的文件名。

8. 在**扫描触发器**中，点击**定时扫描**并设置时间间隔为 **5 分钟**。点击**创建**完成配置。

   备注

   您可以设置特定的时间间隔让流水线扫描远程仓库，以便根据您在**策略设置**中设置的策略来检测代码更新或新的 PR。

### 步骤 5：运行流水线

1. 流水线创建后，点击该流水线名称进入其详情页面。

   备注

   - 您可以点击该流水线右侧的 ![icon](assets/001_KubeSphere_devops_将SonarQube集成到流水线/three-dots.png)，然后选择**复制**来创建该流水线的副本。如需并发运行不包含多分支的多个流水线，您可以将这些流水线全选，然后点击**运行**来批量运行它们。
   - 流水线详情页显示**同步状态**，即 KubeSphere 和 Jenkins 的同步结果。若同步成功，您会看到**成功**图标中打上绿色的对号。

2. 在**运行记录**选项卡下，正在扫描三个分支。点击右侧的**运行**，流水线将根据您设置的行为策略来运行。从下拉列表中选择 **sonarqube**，然后添加标签号，例如 `v0.0.2`。点击**确定**触发新活动。

   备注

   - 如果您在此页面上未看到任何运行记录，则需要手动刷新浏览器或点击下拉菜单（**更多操作**按钮）中的**扫描远程分支**。
   - 标签名称用于在 GitHub 和 Docker Hub 中指代新生成的发布版本和镜像。现有标签名称不能再次用于字段 `TAG_NAME`。否则，流水线将无法成功运行。

3. 稍等片刻，您会看到一些运行停止，一些运行失败。点击第一个活动查看其详细信息。

   备注

   活动失败可能由不同因素所引起。本示例中，在上述步骤中编辑分支环境变量时，仅更改了 sonarqube 分支的 Jenkinsfile。相反地，dependency 和 master 分支中的这些变量保持不变（使用了错误的 GitHub 和 Docker Hub 帐户），从而导致失败。您可以点击该活动，查看其日志中的详细信息。导致失败的其他原因可能是网络问题、Jenkinsfile 中的编码不正确等等。

4. 流水线在 `deploy to dev` 阶段暂停，您需要手动点击**继续**。请注意，在 Jenkinsfile 中分别定义了三个阶段 `deploy to dev`、`push with tag` 和 `deploy to production`，因此将对流水线进行三次审核。

   在开发或生产环境中，可能需要具有更高权限的人员（例如版本管理员）来审核流水线、镜像以及代码分析结果。他们有权决定流水线是否能进入下一阶段。在 Jenkinsfile 中，您可以使用 `input` 来指定由谁审核流水线。如果您想指定一个用户（例如 `project-admin`）来审核，您可以在 Jenkinsfile 中添加一个字段。如果有多个用户，则需要通过逗号进行分隔，如下所示：

   ```
   ···
   input(id: 'release-image-with-tag', message: 'release image with tag?', submitter: 'project-admin,project-admin1')
   ···
   ```

   备注

   在 KubeSphere 3.3 中，如果不指定审核员，那么能够运行流水线的帐户也能够继续或终止该流水线。如果指定审核员，流水线创建者或者您指定的审核员账户均有权限继续或终止流水线。

### 步骤 6：检查流水线状态

1. 在**运行状态**中，您可以查看流水线的运行状态。请注意，流水线在刚创建后将继续初始化几分钟。示例流水线有八个阶段，它们已在 [Jenkinsfile-online](https://github.com/kubesphere/devops-maven-sample/blob/sonarqube/Jenkinsfile-online) 中单独定义。
2. 点击右上角的**查看日志**来查看流水线运行日志。您可以看到流水线的动态日志输出，包括可能导致流水线无法运行的错误。对于每个阶段，您都可以点击该阶段来查看其日志，而且可以将日志下载到本地计算机进行进一步分析。

### 步骤 7：验证结果

1. 流水线成功运行后，点击**代码检查**通过 SonarQube 查看结果，如下所示。

2. 按照 Jenkinsfile 中的定义，通过流水线构建的 Docker 镜像也已成功推送到 Docker Hub。在 Docker Hub 中，您会看到带有标签 `v0.0.2` 的镜像，该标签在流水线运行之前已指定。

3. 同时，GitHub 中已生成一个新标签和一个新发布版本。

4. 示例应用程序将部署到 `kubesphere-sample-dev` 和 `kubesphere-sample-prod`，并创建相应的部署和服务。转到这两个项目，预期结果如下所示：

   | 环境        | URL                         | 命名空间               | 部署          | 服务          |
   | :---------- | :-------------------------- | :--------------------- | :------------ | :------------ |
   | Development | `http://{$NodeIP}:{$30861}` | kubesphere-sample-dev  | ks-sample-dev | ks-sample-dev |
   | Production  | `http://{$NodeIP}:{$30961}` | kubesphere-sample-prod | ks-sample     | ks-sample     |

   备注

   您可能需要在您的安全组中放行该端口，以便通过 URL 访问应用程序。

### 步骤 8：访问示例服务

1. 以 `admin` 身份登录 KubeSphere 并使用**工具箱**中的 **kubectl** 访问该服务。转到 `kubesphere-sample-dev` 项目，然后在**应用负载**下的**服务**中点击 `ks-sample-dev`。在详情页获取 Endpoint 用于访问该服务。

2. 在右下角的**工具箱**中使用 **kubectl** 执行以下命令：

   ```
   curl 10.233.120.230:8080
   ```

3. 预期输出:

   ```
   Really appreciate your star, that's the power of our life.
   ```

   备注

   使用 `curl` 访问 Endpoint，或者访问 {$Virtual IP}:{$Port} 或 {$Node IP}:{$NodePort}。

4. 同样地，您可以在项目 `kubesphere-sample-prod` 中测试服务，您将看到相同的输出结果。

   ```
   $ curl 10.233.120.236:8080
   Really appreciate your star, that's the power of our life.
   ```







