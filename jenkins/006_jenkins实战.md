# Jenkins实战  

整体流程

1 创建一个springboot测试项目, 代码在本地修改

2 提交到远程gitee

3 触发jenkins整个自动化构建流程（打包，测试，发布，部署）





## 1 开发测试项目

准备一个git项目进行测试

我们以gitee为例，github可能太慢了。需要idea安装gitee插件。或者自己熟悉手动命令也行。

步骤：

1 idea创建Spring Boot项目, 名为 devops-demo

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
        <version>3.1.2</version>
    </parent>

    <groupId>com.xingray</groupId>
    <artifactId>devops-demo</artifactId>
    <version>1.0.0</version>

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
            <groupId>org.yaml</groupId>
            <artifactId>snakeyaml</artifactId>
            <version>2.0</version>
        </dependency>
    </dependencies>
</project>
```

application.yaml

```yaml
server:
  port: 8080
```

DevopsApplication.java

```java
package com.xingray.devops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevopsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevopsApplication.class, args);
    }
}
```

HelloController.java

```java
package com.xingray.devops.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(){
        return "hello devops, now is "+new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
    }
}
```



## 2 配置git仓库

在本地代码仓库根目录创建git 仓库

```shell
git init
git add .
git commit -m "init"
```



gitee创建一个空仓库, 命名为 `devops-demo`

![image-20230805190048770](assets/006_jenkins实战/image-20230805190048770.png)

将项目内容推送到 gitee

```shell
git remote add origin git@gitee.com:leixing1012/devops-demo.git
git push -u origin "master"
```

```shell
D:\code\demo\java\devops-demo>git remote add origin git@gitee.com:leixing1012/devops-demo.git

D:\code\demo\java\devops-demo>git push -u origin "master"
Enumerating objects: 15, done.
Counting objects: 100% (15/15), done.
Delta compression using up to 24 threads
Compressing objects: 100% (8/8), done.
Writing objects: 100% (15/15), 1.82 KiB | 1.82 MiB/s, done.
Total 15 (delta 0), reused 0 (delta 0), pack-reused 0
remote: Powered by GITEE.COM [GNK-6.4]
To gitee.com:leixing1012/devops-demo.git
 * [new branch]      master -> master
branch 'master' set up to track 'origin/master'.
```



## 3 创建流水线配置文件

在项目根目录中创建一个Jenkinsfile文件, 文件名可以任取, 这里为了方便idea识别, 设置为 build.jenkinsfile

![image-20230809145043529](assets/006_jenkins实战/image-20230809145043529.png)



## 4 在jenkins中创建流水线

创建一个名为 devops-demo 的流水线项目，使用项目自己的流水线

![image-20230809125448388](assets/006_jenkins实战/image-20230809125448388.png)



![image-20230809125520904](assets/006_jenkins实战/image-20230809125520904.png)

点击确定, 到项目配置页, 这里配置流水线脚本来源于 git , 这里用gitee, 私有项目需要通过设置访问凭证, 这里设置使用 gitee 账号密码访问代码库.

![image-20230809142520764](assets/006_jenkins实战/image-20230809142520764.png)



![image-20230809150122491](assets/006_jenkins实战/image-20230809150122491.png)

注意这个脚本路径要设置为项目源码中 Jenkinsfile 相对于项目源码根目录的路径, 上面创建的Jenkinsfile 为项目源码根目录下的 build.jenkinsfile, 这里就设置为 build.jenkinsfile 即可, 分支根据实际项目情况选择, 这里demo项目只有master分支, 所以这里设置为master分支.



开启构建

可以使用传统的jenkins页面中操作构建流程

![image-20230809143017848](assets/006_jenkins实战/image-20230809143017848.png)

也可以在新版的 blueocean 中操作流水线

![image-20230809143119549](assets/006_jenkins实战/image-20230809143119549.png)

![image-20230809143314028](assets/006_jenkins实战/image-20230809143314028.png)

点击 `运行` 按钮开始运行流水线, 由于现在Jenkinsfile 的内容为空, 无法运行流水线, 

![image-20230809143726127](assets/006_jenkins实战/image-20230809143726127.png)

因此需要修改 Jenkinsfile , 每一次修改完成后将修改提交至 gitee , 再在Jenkins   web 控制台中这个页面点击 `运行` 即可. 如果Jenkins 有公网IP 可以配置 WebHook 的方式开启自动化构建



## 5 编写 Jenkinsfile

https://www.jenkins.io/doc/book/pipeline/

https://www.jenkins.io/doc/book/pipeline/syntax/



流水线语法:

流水线分为2种:

1 声明式

2 脚本式

一般使用的是声明式流水线, 下面使用声明式流水线的语法配置构建流水线



### 5.1 基础格式

```groovy
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}
```

pipeline : 声明是流水线

agent any : 每一个阶段都需要通过代理来实现, 默认使用 any 代理即可, any 代理表示任意可用的节点都可以执行, 在集群模式下, 代理可以是各个工作节点. 代理的类型还有例如 maven nodejs java 等, 可以为每一个阶段单独设置代理, 直接在pipeline中设置的是此流水线的默认代理

stages: 构建阶段

stage: 构建阶段, 可以设置名称

steps: 操作步骤, 里面可以是 命令行如

```shell
echo 'xxx'
echo "env_key is ${env_key}"
```

或者

```shell
sh 'echo xxx'
sh "echo env_key is ${env_key}"
```

使用基础格式编辑好 build.jenkinsfile 后提交到git仓库, 然后在Jenkins的 blueocean 页面中点击 `运行`

![image-20230809151530749](assets/006_jenkins实战/image-20230809151530749.png)

此时会开始构建

![image-20230809151607491](assets/006_jenkins实战/image-20230809151607491.png)

构建成功

![image-20230809151644076](assets/006_jenkins实战/image-20230809151644076.png)

点击构建记录, 查看构建流程的详细信息

![image-20230809151718528](assets/006_jenkins实战/image-20230809151718528.png)

点击各个阶段可以看到各个阶段的输出

![](assets/006_jenkins实战/image-20230809151809966.png)

![](assets/006_jenkins实战/image-20230809151833076.png)

在 steps 中可以写哪些内容, 可以在 Jenkins 的web 页面的项目页进入流水线语法生成器生成代码

![image-20230809152352878](assets/006_jenkins实战/image-20230809152352878.png)



![image-20230809152533195](assets/006_jenkins实战/image-20230809152533195.png)



pipeline中的全局设置, 比如环境变量, 也可以通过 Jenkins 的语法生成器生成脚本代码

![image-20230809152953116](assets/006_jenkins实战/image-20230809152953116.png)

通过这里生成的代码要放在 pipeline 下.



### 5.2 环境变量

示例:

```groovy
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
    }

    stages {
        stage('Check') {
            steps {
				sh "pwd && ls -alh"
				sh "printenv"
            }
        }
        stage('Build') {
            steps {
                echo 'Building..'
                echo "env_key01 is ${env_key01}"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh 'echo env_key02 is ${env_key02}'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                sh "echo env_key03 is ${env_key03}"
            }
        }
    }
}
```

可以看到各个阶段的日志:

Check:

```shell
Selected Git installation does not exist. Using Default
The recommended git tool is: NONE
using credential gitee
 > git rev-parse --resolve-git-dir /var/jenkins_home/workspace/devops-demo/.git # timeout=10
Fetching changes from the remote Git repository
 > git config remote.origin.url https://gitee.com/leixing1012/devops-demo.git # timeout=10
Fetching upstream changes from https://gitee.com/leixing1012/devops-demo.git
 > git --version # timeout=10
 > git --version # 'git version 2.36.2'
using GIT_ASKPASS to set credentials gitee账号密码
 > git fetch --tags --force --progress -- https://gitee.com/leixing1012/devops-demo.git +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git rev-parse refs/remotes/origin/master^{commit} # timeout=10
Checking out Revision e802ca6dc308ec8dbdadac82ec7813cdc3613c2d (refs/remotes/origin/master)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f e802ca6dc308ec8dbdadac82ec7813cdc3613c2d # timeout=10
Commit message: "更新 build.jenkinsfile 添加环境变量"
 > git rev-list --no-walk 946007af0841c6f03bf2cd60bfb05bff66e773dc # timeout=10
+ pwd
/var/jenkins_home/workspace/devops-demo
+ ls -alh
total 28K
drwxr-sr-x 4 root jenkins 4.0K Aug  9 08:24 .
drwxr-sr-x 4 root jenkins 4.0K Aug  9 07:15 ..
drwxr-sr-x 8 root jenkins 4.0K Aug  9 08:24 .git
-rw-r--r-- 1 root jenkins  528 Aug  9 07:15 .gitignore
-rw-r--r-- 1 root jenkins  749 Aug  9 08:24 build.jenkinsfile
-rw-r--r-- 1 root jenkins 1.2K Aug  9 07:15 pom.xml
drwxr-sr-x 3 root jenkins 4.0K Aug  9 07:15 src
+ printenv
JENKINS_HOME=/var/jenkins_home
GIT_PREVIOUS_SUCCESSFUL_COMMIT=946007af0841c6f03bf2cd60bfb05bff66e773dc
JENKINS_UC_EXPERIMENTAL=https://updates.jenkins.io/experimental
CI=true
RUN_CHANGES_DISPLAY_URL=http://192.168.0.140:8080/job/devops-demo/9/display/redirect?page=changes
HOSTNAME=5c17fd2ae6e8
LD_LIBRARY_PATH=/opt/java/openjdk/lib/server:/opt/java/openjdk/lib:/opt/java/openjdk/../lib
SHLVL=2
NODE_LABELS=built-in
HUDSON_URL=http://192.168.0.140:8080/
GIT_COMMIT=e802ca6dc308ec8dbdadac82ec7813cdc3613c2d
HOME=/root
BUILD_URL=http://192.168.0.140:8080/job/devops-demo/9/
HUDSON_COOKIE=0f17d865-1e76-48c4-8ea7-c0145949d288
JENKINS_SERVER_COOKIE=durable-778f5e407d2e6a134ba852205d9e32b9186eb7fe4584ebab0f7f006124ee37db
env_key01=env_value01
env_key02=env_value02
env_key03=env_value03
JENKINS_UC=https://updates.jenkins.io
WORKSPACE=/var/jenkins_home/workspace/devops-demo
REF=/usr/share/jenkins/ref
NODE_NAME=built-in
RUN_ARTIFACTS_DISPLAY_URL=http://192.168.0.140:8080/job/devops-demo/9/display/redirect?page=artifacts
STAGE_NAME=Check
GIT_BRANCH=origin/master
EXECUTOR_NUMBER=1
RUN_TESTS_DISPLAY_URL=http://192.168.0.140:8080/job/devops-demo/9/display/redirect?page=tests
BUILD_DISPLAY_NAME=#9
JENKINS_VERSION=2.346.3
JENKINS_INCREMENTALS_REPO_MIRROR=https://repo.jenkins-ci.org/incrementals
HUDSON_HOME=/var/jenkins_home
JOB_BASE_NAME=devops-demo
PATH=/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
BUILD_ID=9
BUILD_TAG=jenkins-devops-demo-9
JENKINS_URL=http://192.168.0.140:8080/
LANG=C.UTF-8
JOB_URL=http://192.168.0.140:8080/job/devops-demo/
GIT_URL=https://gitee.com/leixing1012/devops-demo.git
BUILD_NUMBER=9
JENKINS_NODE_COOKIE=a330d564-d21e-4d8d-b8e3-f408fd62e696
RUN_DISPLAY_URL=http://192.168.0.140:8080/job/devops-demo/9/display/redirect
JENKINS_SLAVE_AGENT_PORT=50000
HUDSON_SERVER_COOKIE=025c6fe57a0c3649
JOB_DISPLAY_URL=http://192.168.0.140:8080/job/devops-demo/display/redirect
JOB_NAME=devops-demo
COPY_REFERENCE_FILE_LOG=/var/jenkins_home/copy_reference_file.log
PWD=/var/jenkins_home/workspace/devops-demo
JAVA_HOME=/opt/java/openjdk
GIT_PREVIOUS_COMMIT=946007af0841c6f03bf2cd60bfb05bff66e773dc
WORKSPACE_TMP=/var/jenkins_home/workspace/devops-demo@tmp
```

Build:

```shell
Building..
env_key01 is env_value01
```

Test:

```shell
Testing..
+ echo env_key02 is env_value02
env_key02 is env_value02
```

Deploy

```shell
Deploying....
+ echo env_key03 is env_value03
env_key03 is env_value03
```



可以看到Check阶段输出了所有的环境变量, 其中比较重要的有:

```shell
WORKSPACE=/var/jenkins_home/workspace/devops-demo
```

WORKSPACE 工作空间, Jenkins_home 目录 `/var/jenkins_home` 已经挂到了宿主机 `/var/lib/docker/volumes/jenkins-data/_data` 目录下 , Jenkins 中的每一个流水线都是在 jenkins_home 下的workspace 中单独创建一个以项目名为名称的目录, 如这里的 `/var/jenkins_home/workspace/devops-demo` 目录, 这个目录是 jenkins 容器中的目录, 在宿主机中为

```shell
WORKSPACE_TMP=/var/jenkins_home/workspace/devops-demo@tmp
```

WORKSPACE_TMP 工作空间的临时目录, 构建期间的临时数据的目录, 如  maven 代理编辑java程序的 target 目录等



```shell
root@ubuntu-dev:/var/lib/docker/volumes/jenkins-data/_data/workspace# ls
devops-demo  devops-demo@tmp
```



```shell
BUILD_NUMBER=9
```

BUILD_NUMBER 表示构建号,当前是第几次构建, 每一次启动构建, BUILD_NUMBER 会自增



在页面上可以从任意阶段重新执行流水线, 如:

![image-20230809162708139](assets/006_jenkins实战/image-20230809162708139.png)





### 5.3 常用环境配置

首先检查 java git docker maven 是否可用

```shell
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
    }

    stages {
        stage('Check') {
            steps {
				sh "pwd && ls -alh"
				sh "java -version"
				sh "git --version"
				sh "docker version"
				sh "mvn -v"
            }
        }
        stage('Build') {
            steps {
                echo 'Building..'
                echo "env_key01 is ${env_key01}"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh 'echo env_key02 is ${env_key02}'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                sh "echo env_key03 is ${env_key03}"
            }
        }
    }
}
```

输出

```shell
Selected Git installation does not exist. Using Default
The recommended git tool is: NONE
using credential gitee
 > git rev-parse --resolve-git-dir /var/jenkins_home/workspace/devops-demo/.git # timeout=10
Fetching changes from the remote Git repository
 > git config remote.origin.url https://gitee.com/leixing1012/devops-demo.git # timeout=10
Fetching upstream changes from https://gitee.com/leixing1012/devops-demo.git
 > git --version # timeout=10
 > git --version # 'git version 2.36.2'
using GIT_ASKPASS to set credentials gitee账号密码
 > git fetch --tags --force --progress -- https://gitee.com/leixing1012/devops-demo.git +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git rev-parse refs/remotes/origin/master^{commit} # timeout=10
Checking out Revision 687b7e6d7d524696d695c5adc84577cde8e5422c (refs/remotes/origin/master)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f 687b7e6d7d524696d695c5adc84577cde8e5422c # timeout=10
Commit message: "更新 build.jenkinsfile 添加环境变量"
 > git rev-list --no-walk c0fccb30828b4819b3ccd5faa38c1dd461a0b570 # timeout=10
+ pwd
/var/jenkins_home/workspace/devops-demo
+ ls -alh
total 28K
drwxr-sr-x 4 root jenkins 4.0K Aug  9 09:34 .
drwxr-sr-x 4 root jenkins 4.0K Aug  9 07:15 ..
drwxr-sr-x 8 root jenkins 4.0K Aug  9 09:34 .git
-rw-r--r-- 1 root jenkins  528 Aug  9 07:15 .gitignore
-rw-r--r-- 1 root jenkins  817 Aug  9 09:34 build.jenkinsfile
-rw-r--r-- 1 root jenkins 1.2K Aug  9 07:15 pom.xml
drwxr-sr-x 3 root jenkins 4.0K Aug  9 07:15 src
+ java -version
openjdk version "11.0.16.1" 2022-08-12
OpenJDK Runtime Environment Temurin-11.0.16.1+1 (build 11.0.16.1+1)
OpenJDK 64-Bit Server VM Temurin-11.0.16.1+1 (build 11.0.16.1+1, mixed mode)
+ git --version
git version 2.36.2
+ docker version
Client:
 Version:           20.10.16
 API version:       1.41
 Go version:        go1.18.5
 Git commit:        aa7e414fdcb23a66e8fabbef0a560ef1769eace5
 Built:             Tue Aug  2 11:08:17 2022
 OS/Arch:           linux/amd64
 Context:           default
 Experimental:      true

Server: Docker Engine - Community
 Engine:
  Version:          24.0.5
  API version:      1.43 (minimum version 1.12)
  Go version:       go1.20.6
  Git commit:       a61e2b4
  Built:            Fri Jul 21 20:35:18 2023
  OS/Arch:          linux/amd64
  Experimental:     false
 containerd:
  Version:          1.6.22
  GitCommit:        8165feabfdfe38c65b599c4993d227328c231fca
 runc:
  Version:          1.1.8
  GitCommit:        v1.1.8-0-g82f18fe
 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0
+ mvn -v
/var/jenkins_home/workspace/devops-demo@tmp/durable-1b0bb94d/script.sh: line 1: mvn: not found
script returned exit code 127
```

可以看到 java git docker 环境都已经配置好了, 但是没有 maven 环境, 要编译打包 java 项目就需要先在Jenkins中配置好 maven 环境, 可以使用 Jenkins 的配置方式, 但是比较麻烦, 还可以使用 docker容器代理的方式, 参考 : https://www.jenkins.io/doc/book/pipeline/docker/ , 

1 安装 docker pipeline 插件  https://plugins.jenkins.io/docker-workflow/ 

![image-20230809174833409](assets/006_jenkins实战/image-20230809174833409.png)

安装插件后可以配置一个默认代理, 如下:

```groovy
pipeline {
    agent {
        docker { image 'node:18.17.0-alpine3.18' }
    }
    stages {
        stage('Test') {
            steps {
                sh 'node --version'
            }
        }
    }
}
```

但是一般大型的项目各个阶段需要的环境不一样, 所以可以使用多阶段的构建流水线, 如下:

```groovy
pipeline {
    agent none
    stages {
        stage('Back-end') {
            agent {
                docker { image 'maven:3.9.3-eclipse-temurin-17-alpine' }
            }
            steps {
                sh 'mvn --version'
            }
        }
        stage('Front-end') {
            agent {
                docker { image 'node:18.17.0-alpine3.18' }
            }
            steps {
                sh 'node --version'
            }
        }
    }
}
```



在示例项目中, 修改 build.jenkinsfile 如下:

```groovy
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
    }

    stages {
        stage('Check') {
            steps {
				sh "pwd && ls -alh"
				sh "java -version"
				sh "git --version"
				sh "docker version"

            }
        }
        stage('Build') {
            agent{
                docker{
                    image 'maven:3.9-eclipse-temurin-20-alpine'
                }
            }
            steps {
                echo 'Building..'
                echo "env_key01 is ${env_key01}"
                sh "mvn -v"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh 'echo env_key02 is ${env_key02}'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                sh "echo env_key03 is ${env_key03}"
            }
        }
    }
}
```

```shell
Selected Git installation does not exist. Using Default
The recommended git tool is: NONE
using credential gitee
 > git rev-parse --resolve-git-dir /var/jenkins_home/workspace/devops-demo@2/.git # timeout=10
Fetching changes from the remote Git repository
 > git config remote.origin.url https://gitee.com/leixing1012/devops-demo.git # timeout=10
Fetching upstream changes from https://gitee.com/leixing1012/devops-demo.git
 > git --version # timeout=10
 > git --version # 'git version 2.36.2'
using GIT_ASKPASS to set credentials gitee账号密码
 > git fetch --tags --force --progress -- https://gitee.com/leixing1012/devops-demo.git +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git rev-parse refs/remotes/origin/master^{commit} # timeout=10
Checking out Revision 7f3b24c5c0c33bcf5df734999fb87733603b0919 (refs/remotes/origin/master)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f 7f3b24c5c0c33bcf5df734999fb87733603b0919 # timeout=10
Commit message: "更新 build.jenkinsfile 添加环境变量"
+ docker inspect -f . maven:3.9-eclipse-temurin-20-alpine

Error: No such object: maven:3.9-eclipse-temurin-20-alpine
+ docker pull maven:3.9-eclipse-temurin-20-alpine
3.9-eclipse-temurin-20-alpine: Pulling from library/maven
31e352740f53: Already exists
b74c4e203881: Pulling fs layer
8db2f5bca0f2: Pulling fs layer
42cb67a7d7a0: Pulling fs layer
898b1e6cf835: Pulling fs layer
f2ab47d47a05: Pulling fs layer
62e3e5752c05: Pulling fs layer
f6fd8ae820ca: Pulling fs layer
ca42826b66d9: Pulling fs layer
310ce2bb1192: Pulling fs layer
f2ab47d47a05: Waiting
ca42826b66d9: Waiting
310ce2bb1192: Waiting
62e3e5752c05: Waiting
898b1e6cf835: Waiting
f6fd8ae820ca: Waiting
42cb67a7d7a0: Verifying Checksum
42cb67a7d7a0: Download complete
898b1e6cf835: Download complete
b74c4e203881: Verifying Checksum
b74c4e203881: Download complete
b74c4e203881: Pull complete
f2ab47d47a05: Download complete
62e3e5752c05: Verifying Checksum
62e3e5752c05: Download complete
ca42826b66d9: Download complete
f6fd8ae820ca: Verifying Checksum
f6fd8ae820ca: Download complete
310ce2bb1192: Download complete
8db2f5bca0f2: Verifying Checksum
8db2f5bca0f2: Download complete
8db2f5bca0f2: Pull complete
42cb67a7d7a0: Pull complete
898b1e6cf835: Pull complete
f2ab47d47a05: Pull complete
62e3e5752c05: Pull complete
f6fd8ae820ca: Pull complete
ca42826b66d9: Pull complete
310ce2bb1192: Pull complete
Digest: sha256:0a3b5e83c310523dd554e63f5079da5acaf169256556e87276fed6d148d90eb9
Status: Downloaded newer image for maven:3.9-eclipse-temurin-20-alpine
docker.io/library/maven:3.9-eclipse-temurin-20-alpine
Building..
env_key01 is env_value01
+ mvn -v
Apache Maven 3.9.3 (21122926829f1ead511c958d89bd2f672198ae9f)
Maven home: /usr/share/maven
Java version: 20.0.2, vendor: Eclipse Adoptium, runtime: /opt/java/openjdk
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.15.0-78-generic", arch: "amd64", family: "unix"
```

可以看到在 Build 阶段中 mvn 环境已经配置好了



### 5.4 项目打包

使用maven指令打包即可

```shell
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
    }

    stages {
        stage('Check') {
            steps {
                echo 'Checking..'
				sh "pwd && ls -ailh"
				sh "java -version"
				sh "git --version"
				sh "docker version"
            }
        }
        stage('Build') {
            agent{
                docker{
                    image 'maven:3.9-eclipse-temurin-20-alpine'
                }
            }
            steps {
                echo 'Building..'
                sh "pwd && ls -ailh"
                sh "tree /"
                sh "mvn -v"
                sh "mvn clean package -Dmaven.test.skip=true"
                sh "pwd && ls -ailh"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh "pwd && ls -ailh"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                sh "pwd && ls -ailh"
            }
        }
    }
}
```

执行日志如下:

```shell
Selected Git installation does not exist. Using Default
The recommended git tool is: NONE
using credential gitee
 > git rev-parse --resolve-git-dir /var/jenkins_home/workspace/devops-demo@2/.git # timeout=10
Fetching changes from the remote Git repository
 > git config remote.origin.url https://gitee.com/leixing1012/devops-demo.git # timeout=10
Fetching upstream changes from https://gitee.com/leixing1012/devops-demo.git
 > git --version # timeout=10
 > git --version # 'git version 2.36.2'
using GIT_ASKPASS to set credentials gitee账号密码
 > git fetch --tags --force --progress -- https://gitee.com/leixing1012/devops-demo.git +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git rev-parse refs/remotes/origin/master^{commit} # timeout=10
Checking out Revision 833fbeff0984f6322412192f266385c1ef877bd8 (refs/remotes/origin/master)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f 833fbeff0984f6322412192f266385c1ef877bd8 # timeout=10
Commit message: "更新 build.jenkinsfile 添加环境变量"
+ docker inspect -f . maven:3.9-eclipse-temurin-20-alpine
.
Building..
+ pwd
/var/jenkins_home/workspace/devops-demo@2
+ ls -ailh
total 32K    
1320920 drwxr-sr-x    5 root     1000        4.0K Aug  9 11:27 .
1320349 drwxr-sr-x    6 root     1000        4.0K Aug  9 09:45 ..
1320877 drwxr-sr-x    8 root     1000        4.0K Aug  9 11:27 .git
1320949 -rw-r--r--    1 root     1000         528 Aug  9 09:45 .gitignore
1320950 -rw-r--r--    1 root     1000        1.1K Aug  9 11:27 build.jenkinsfile
1321048 -rw-r--r--    1 root     1000        1.2K Aug  9 09:45 pom.xml
1321049 drwxr-sr-x    3 root     1000        4.0K Aug  9 09:45 src
1327553 drwxr-sr-x    6 root     1000        4.0K Aug  9 10:56 target
+ tree /
/
├── bin
│   ├── arch -> /bin/busybox
│   ├── ash -> /bin/busybox
│   ├── base64 -> /bin/busybox
│   ├── bash
│   ├── bbconfig -> /bin/busybox
│   ├── busybox
├── entrypoint.sh
├── home
├── lib
├── media
├── mnt
├── opt
│   └── java
│       └── openjdk
│           ├── NOTICE
│           ├── bin
│           │   ├── jar
│           │   ├── jarsigner
│           │   ├── java
│           │   ├── javac
│           │   ├── javadoc
│           │   ├── javap
│           │   ├── jcmd
│           │   ├── jconsole
│           │   ├── jdb
│           │   ├── jdeprscan
│           │   ├── jdeps
│           │   ├── jfr
│           │   ├── jhsdb
│           │   ├── jimage
│           │   ├── jinfo
│           │   ├── jlink
│           │   ├── jmap
│           │   ├── jmod
│           │   ├── jpackage
│           │   ├── jps
│           │   ├── jrunscript
│           │   ├── jshell
│           │   ├── jstack
│           │   ├── jstat
│           │   ├── jstatd
│           │   ├── jwebserver
│           │   ├── keytool
│           │   ├── rmiregistry
│           │   └── serialver
│           ├── conf
│           │   ├── logging.properties
│           │   ├── management
│           │   │   ├── jmxremote.access
│           │   │   ├── jmxremote.password.template
│           │   │   └── management.properties
│           │   ├── net.properties
│           │   ├── sdp
│           │   │   └── sdp.conf.template
│           │   ├── security
│           │   │   ├── java.policy
│           │   │   ├── java.security
│           │   │   └── policy
│           │   │       ├── README.txt
│           │   │       ├── limited
│           │   │       │   ├── default_US_export.policy
│           │   │       │   ├── default_local.policy
│           │   │       │   └── exempt_local.policy
│           │   │       └── unlimited
│           │   │           ├── default_US_export.policy
│           │   │           └── default_local.policy
│           │   └── sound.properties
│           ├── include
│           ├── jmods
│           │   ├── jdk.xml.dom.jmod
│           │   └── jdk.zipfs.jmod
│           ├── legal
│           ├── lib
│           ├── man
│           └── release
├── proc
├── root
├── run
│   └── docker.sock
├── sbin
│   ├── acpid -> /bin/busybox
│   ├── adjtimex -> /bin/busybox
│   ├── apk
├── usr
│   ├── bin
│   │   ├── mvn -> /usr/share/maven/bin/mvn

│   ├── local
│   │   ├── bin
│   │   │   └── mvn-entrypoint.sh
│   │   ├── lib
│   │   └── share
│   │       └── ca-certificates
│   └── share
│       ├── maven
│       │   ├── LICENSE
│       │   ├── NOTICE
│       │   ├── README.txt
│       │   ├── bin
│       │   │   ├── m2.conf
│       │   │   ├── mvn
│       │   │   ├── mvn.cmd
│       │   │   ├── mvnDebug
│       │   │   ├── mvnDebug.cmd
│       │   │   └── mvnyjp
│       │   ├── boot
│       │   │   ├── plexus-classworlds-2.7.0.jar
│       │   │   └── plexus-classworlds.license
│       │   ├── conf
│       │   │   ├── logging
│       │   │   │   └── simplelogger.properties
│       │   │   ├── settings.xml
│       │   │   └── toolchains.xml
│       │   ├── lib
│       │   │   ├── aopalliance-1.0.jar
│       │   │   ├── aopalliance.license
│       │   │   ├── commons-cli-1.5.0.jar
│       │   │   ├── commons-cli.license
│       │   │   ├── commons-codec-1.11.jar
│       │   │   ├── commons-codec.license
│       │   │   ├── commons-lang3-3.12.0.jar
│       │   │   ├── commons-lang3.license
│       │   │   ├── ext
│       │   │   │   ├── README.txt
│       │   │   │   ├── hazelcast
│       │   │   │   │   └── README.txt
│       │   │   │   └── redisson
│       │   │   │       └── README.txt
│       │   │   ├── failureaccess-1.0.1.jar
│       │   │   ├── failureaccess.license
│       │   │   ├── guava-31.1-jre.jar
│       │   │   ├── guava.license
│       │   │   ├── guice-5.1.0.jar
│       │   │   ├── guice.license
│       │   │   ├── httpclient-4.5.14.jar
│       │   │   ├── httpclient.license
│       │   │   ├── httpcore-4.4.16.jar
│       │   │   ├── httpcore.license
│       │   │   ├── jansi-2.4.0.jar
│       │   │   ├── jansi-native
│       │   │   │   ├── README.txt
│       │   │   │   └── Windows
│       │   │   │       ├── x86
│       │   │   │       │   └── jansi.dll
│       │   │   │       └── x86_64
│       │   │   │           └── jansi.dll
│       │   │   ├── jansi.license
│       │   │   ├── javax.annotation-api-1.3.2.jar
│       │   │   ├── javax.annotation-api.license
│       │   │   ├── javax.inject-1.jar
│       │   │   ├── javax.inject.license
│       │   │   ├── jcl-over-slf4j-1.7.36.jar
│       │   │   ├── jcl-over-slf4j.license
│       │   │   ├── maven-artifact-3.9.3.jar
│       │   │   ├── maven-builder-support-3.9.3.jar
│       │   │   ├── maven-compat-3.9.3.jar
│       │   │   ├── maven-core-3.9.3.jar
│       │   │   ├── maven-embedder-3.9.3.jar
│       │   │   ├── maven-model-3.9.3.jar
│       │   │   ├── maven-model-builder-3.9.3.jar
│       │   │   ├── maven-plugin-api-3.9.3.jar
│       │   │   ├── maven-repository-metadata-3.9.3.jar
│       │   │   ├── maven-resolver-api-1.9.13.jar
│       │   │   ├── maven-resolver-connector-basic-1.9.13.jar
│       │   │   ├── maven-resolver-impl-1.9.13.jar
│       │   │   ├── maven-resolver-named-locks-1.9.13.jar
│       │   │   ├── maven-resolver-provider-3.9.3.jar
│       │   │   ├── maven-resolver-spi-1.9.13.jar
│       │   │   ├── maven-resolver-transport-file-1.9.13.jar
│       │   │   ├── maven-resolver-transport-http-1.9.13.jar
│       │   │   ├── maven-resolver-transport-wagon-1.9.13.jar
│       │   │   ├── maven-resolver-util-1.9.13.jar
│       │   │   ├── maven-settings-3.9.3.jar
│       │   │   ├── maven-settings-builder-3.9.3.jar
│       │   │   ├── maven-shared-utils-3.3.4.jar
│       │   │   ├── maven-slf4j-provider-3.9.3.jar
│       │   │   ├── org.eclipse.sisu.inject-0.3.5.jar
│       │   │   ├── org.eclipse.sisu.inject.license
│       │   │   ├── org.eclipse.sisu.plexus-0.3.5.jar
│       │   │   ├── org.eclipse.sisu.plexus.license
│       │   │   ├── plexus-cipher-2.0.jar
│       │   │   ├── plexus-cipher.license
│       │   │   ├── plexus-component-annotations-2.1.0.jar
│       │   │   ├── plexus-component-annotations.license
│       │   │   ├── plexus-interpolation-1.26.jar
│       │   │   ├── plexus-interpolation.license
│       │   │   ├── plexus-sec-dispatcher-2.0.jar
│       │   │   ├── plexus-sec-dispatcher.license
│       │   │   ├── plexus-utils-3.5.1.jar
│       │   │   ├── plexus-utils.license
│       │   │   ├── slf4j-api-1.7.36.jar
│       │   │   ├── slf4j-api.license
│       │   │   ├── wagon-file-3.5.3.jar
│       │   │   ├── wagon-http-3.5.3.jar
│       │   │   ├── wagon-http-shared-3.5.3.jar
│       │   │   └── wagon-provider-api-3.5.3.jar
│       │   └── ref
│       │       └── settings-docker.xml
└── var
    ├── jenkins_home
    │   ├── caches
    │   │   ├── durable-task
    │   │   ├── git-aec517593adb1f841a25e3de5a347ec2
    │   │   └── git-aec517593adb1f841a25e3de5a347ec2@tmp
    │   ├── config.xml
    │   ├── copy_reference_file.log
    │   ├── credentials.xml
    │   ├── fingerprints
    │   │   └── f2
    │   │       └── 30
    │   │           └── 7ff7c2ce523108a3e7745b03ccd9.xml
    │   ├── hudson.model.UpdateCenter.xml
    │   ├── hudson.plugins.git.GitTool.xml
    │   ├── identity.key.enc
    │   ├── jenkins.install.InstallUtil.installingPlugins
    │   ├── jenkins.install.InstallUtil.lastExecVersion
    │   ├── jenkins.install.UpgradeWizard.state
    │   ├── jenkins.model.JenkinsLocationConfiguration.xml
    │   ├── jenkins.security.apitoken.ApiTokenPropertyConfiguration.xml
    │   ├── jenkins.telemetry.Correlator.xml
    │   ├── jobs
    │   │   └── devops-demo
    │   │       ├── builds
    │   │       │   ├── 1
    │   │       │   │   ├── build.xml
    │   │       │   │   ├── log
    │   │       │   │   └── log-index
    │   │       │   ├── legacyIds
    │   │       │   └── permalinks
    │   │       ├── config.xml
    │   │       └── nextBuildNumber
    │   ├── logs
    │   │   └── tasks
    │   ├── nodeMonitors.xml
    │   ├── nodes
    │   ├── org.jenkinsci.plugins.workflow.flow.FlowExecutionList.xml
    │   ├── plugins
    │   ├── queue.xml
    │   ├── queue.xml.bak
    │   ├── secret.key
    │   ├── secret.key.not-so-secret
    │   ├── secrets
    │   ├── updates
    │   ├── userContent
    │   │   └── readme.txt
    │   ├── users
    │   │   ├── leixing1012_16547958101531502079
    │   │   │   └── config.xml
    │   │   ├── tom_12407799279045396642
    │   │   │   └── config.xml
    │   │   └── users.xml
    │   ├── war
    │   │   ├── META-INF
    │   │   │   ├── JENKINS.RSA
    │   │   │   ├── JENKINS.SF
    │   │   │   ├── MANIFEST.MF
    │   │   │   └── maven
    │   │   │       └── org.jenkins-ci.main
    │   │   │           └── jenkins-war
    │   │   │               ├── pom.properties
    │   │   │               └── pom.xml
    │   │   ├── WEB-INF
    │   │   │   ├── classes
    │   │   │   │   ├── META-INF
    │   │   │   │   │   ├── licenses.html
    │   │   │   │   │   └── licenses.xml
    │   │   │   │   ├── dependencies.txt
    │   │   │   │   └── images
    │   │   │   ├── detached-plugins
    │   │   │   │   ├── antisamy-markup-formatter.hpi
    │   │   │   │   ├── apache-httpcomponents-client-4-api.hpi
    │   │   │   │   ├── bootstrap5-api.hpi
    │   │   │   │   ├── bouncycastle-api.hpi
    │   │   │   │   ├── caffeine-api.hpi
    │   │   │   │   ├── checks-api.hpi
    │   │   │   │   ├── command-launcher.hpi
    │   │   │   │   ├── commons-lang3-api.hpi
    │   │   │   │   ├── commons-text-api.hpi
    │   │   │   │   ├── display-url-api.hpi
    │   │   │   │   ├── echarts-api.hpi
    │   │   │   │   ├── font-awesome-api.hpi
    │   │   │   │   ├── instance-identity.hpi
    │   │   │   │   ├── ionicons-api.hpi
    │   │   │   │   ├── jackson2-api.hpi
    │   │   │   │   ├── jakarta-activation-api.hpi
    │   │   │   │   ├── jakarta-mail-api.hpi
    │   │   │   │   ├── javax-activation-api.hpi
    │   │   │   │   ├── javax-mail-api.hpi
    │   │   │   │   ├── jaxb.hpi
    │   │   │   │   ├── jdk-tool.hpi
    │   │   │   │   ├── jquery3-api.hpi
    │   │   │   │   ├── junit.hpi
    │   │   │   │   ├── mailer.hpi
    │   │   │   │   ├── matrix-auth.hpi
    │   │   │   │   ├── matrix-project.hpi
    │   │   │   │   ├── mina-sshd-api-common.hpi
    │   │   │   │   ├── mina-sshd-api-core.hpi
    │   │   │   │   ├── plugin-util-api.hpi
    │   │   │   │   ├── popper2-api.hpi
    │   │   │   │   ├── scm-api.hpi
    │   │   │   │   ├── script-security.hpi
    │   │   │   │   ├── snakeyaml-api.hpi
    │   │   │   │   ├── sshd.hpi
    │   │   │   │   ├── structs.hpi
    │   │   │   │   ├── trilead-api.hpi
    │   │   │   │   ├── workflow-api.hpi
    │   │   │   │   ├── workflow-step-api.hpi
    │   │   │   │   └── workflow-support.hpi
    │   │   │   ├── hudson
    │   │   │   ├── ibm-web-bnd.xmi
    │   │   │   ├── jboss-deployment-structure.xml
    │   │   │   ├── jboss-web.xml
    │   │   │   ├── lib
    │   │   │   │   ├── access-modifier-annotation-1.31.jar
    │   │   │   │   ├── annotation-indexer-1.17.jar
    │   │   │   │   ├── ant-1.10.13.jar
    │   │   │   │   ├── ant-launcher-1.10.13.jar
    │   │   │   │   ├── antlr4-runtime-4.12.0.jar
    │   │   │   │   ├── args4j-2.33.jar
    │   │   │   │   ├── asm-9.5.jar
    │   │   │   │   ├── asm-analysis-9.5.jar
    │   │   │   │   ├── asm-commons-9.5.jar
    │   │   │   │   ├── asm-tree-9.5.jar
    │   │   │   │   ├── asm-util-9.5.jar
    │   │   │   │   ├── bridge-method-annotation-1.26.jar
    │   │   │   │   ├── cli-2.401.3.jar
    │   │   │   │   ├── commons-beanutils-1.9.4.jar
    │   │   │   │   ├── commons-codec-1.15.jar
    │   │   │   │   ├── commons-collections-3.2.2.jar
    │   │   │   │   ├── commons-compress-1.23.0.jar
    │   │   │   │   ├── commons-discovery-0.5.jar
    │   │   │   │   ├── commons-fileupload-1.5.jar
    │   │   │   │   ├── commons-io-2.11.0.jar
    │   │   │   │   ├── commons-jelly-1.1-jenkins-20230124.jar
    │   │   │   │   ├── commons-jelly-tags-define-1.1-jenkins-20230124.jar
    │   │   │   │   ├── commons-jelly-tags-fmt-1.0.jar
    │   │   │   │   ├── commons-jelly-tags-xml-1.1.jar
    │   │   │   │   ├── commons-jexl-1.1-jenkins-20111212.jar
    │   │   │   │   ├── commons-lang-2.6.jar
    │   │   │   │   ├── crypto-util-1.8.jar
    │   │   │   │   ├── dom4j-2.1.4.jar
    │   │   │   │   ├── embedded_su4j-1.1.jar
    │   │   │   │   ├── ezmorph-1.0.6.jar
    │   │   │   │   ├── failureaccess-1.0.1.jar
    │   │   │   │   ├── groovy-all-2.4.21.jar
    │   │   │   │   ├── guava-31.1-jre.jar
    │   │   │   │   ├── guice-6.0.0.jar
    │   │   │   │   ├── j-interop-2.0.8-kohsuke-1.jar
    │   │   │   │   ├── j-interopdeps-2.0.8-kohsuke-1.jar
    │   │   │   │   ├── jakarta.annotation-api-2.1.1.jar
    │   │   │   │   ├── jakarta.inject-api-2.0.1.jar
    │   │   │   │   ├── jakarta.servlet.jsp.jstl-api-1.2.7.jar
    │   │   │   │   ├── jansi-1.11.jar
    │   │   │   │   ├── javax.annotation-api-1.3.2.jar
    │   │   │   │   ├── javax.inject-1.jar
    │   │   │   │   ├── jaxen-2.0.0.jar
    │   │   │   │   ├── jbcrypt-1.0.0.jar
    │   │   │   │   ├── jcifs-1.3.18-kohsuke-1.jar
    │   │   │   │   ├── jcip-annotations-1.0.jar
    │   │   │   │   ├── jcl-over-slf4j-2.0.7.jar
    │   │   │   │   ├── jcommon-1.0.23.jar
    │   │   │   │   ├── jenkins-core-2.401.3.jar
    │   │   │   │   ├── jenkins-stapler-support-1.1.jar
    │   │   │   │   ├── jfreechart-1.0.19.jar
    │   │   │   │   ├── jline-2.14.6.jar
    │   │   │   │   ├── jna-5.13.0.jar
    │   │   │   │   ├── json-lib-2.4-jenkins-3.jar
    │   │   │   │   ├── jzlib-1.1.3-kohsuke-1.jar
    │   │   │   │   ├── kxml2-2.3.0.jar
    │   │   │   │   ├── listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar
    │   │   │   │   ├── localizer-1.31.jar
    │   │   │   │   ├── log4j-over-slf4j-2.0.7.jar
    │   │   │   │   ├── memory-monitor-1.12.jar
    │   │   │   │   ├── metainf-services-1.9.jar
    │   │   │   │   ├── mxparser-1.2.2.jar
    │   │   │   │   ├── relaxngDatatype-20020414.jar
    │   │   │   │   ├── remoting-3107.v665000b_51092.jar
    │   │   │   │   ├── robust-http-client-1.2.jar
    │   │   │   │   ├── sezpoz-1.13.jar
    │   │   │   │   ├── slf4j-api-2.0.7.jar
    │   │   │   │   ├── slf4j-jdk14-2.0.7.jar
    │   │   │   │   ├── spotbugs-annotations-4.7.3.jar
    │   │   │   │   ├── spring-aop-5.3.27.jar
    │   │   │   │   ├── spring-beans-5.3.27.jar
    │   │   │   │   ├── spring-context-5.3.27.jar
    │   │   │   │   ├── spring-core-5.3.27.jar
    │   │   │   │   ├── spring-expression-5.3.27.jar
    │   │   │   │   ├── spring-security-core-5.8.2.jar
    │   │   │   │   ├── spring-security-crypto-5.8.2.jar
    │   │   │   │   ├── spring-security-web-5.8.2.jar
    │   │   │   │   ├── spring-web-5.3.27.jar
    │   │   │   │   ├── stapler-1777.v7c6fe6d54a_0c.jar
    │   │   │   │   ├── stapler-adjunct-codemirror-1.3.jar
    │   │   │   │   ├── stapler-adjunct-timeline-1.5.jar
    │   │   │   │   ├── stapler-groovy-1777.v7c6fe6d54a_0c.jar
    │   │   │   │   ├── stapler-jelly-1777.v7c6fe6d54a_0c.jar
    │   │   │   │   ├── symbol-annotation-1.24.jar
    │   │   │   │   ├── task-reactor-1.8.jar
    │   │   │   │   ├── tiger-types-2.2.jar
    │   │   │   │   ├── txw2-20110809.jar
    │   │   │   │   ├── version-number-1.11.jar
    │   │   │   │   ├── websocket-jetty10-2.401.3.jar
    │   │   │   │   ├── websocket-jetty9-2.401.3.jar
    │   │   │   │   ├── websocket-spi-2.401.3.jar
    │   │   │   │   ├── windows-package-checker-1.2.jar
    │   │   │   │   ├── winp-1.30.jar
    │   │   │   │   ├── xpp3-1.1.4c.jar
    │   │   │   │   └── xstream-1.4.20.jar
    │   │   │   ├── sun-web.xml
    │   │   │   ├── update-center-rootCAs
    │   │   │   │   ├── jenkins-update-center-root-ca
    │   │   │   │   ├── jenkins-update-center-root-ca-2
    │   │   │   │   ├── jenkins-update-center-root-ca-2.txt
    │   │   │   │   └── jenkins-update-center-root-ca.txt
    │   │   │   └── web.xml
    │   │   ├── apple-touch-icon.png
    │   │   ├── bootstrap
    │   │   ├── css
    │   │   ├── executable
    │   │   │   ├── Main.class
    │   │   │   └── winstone.jar
    │   │   ├── favicon.ico
    │   │   ├── favicon.svg
    │   │   ├── help
    │   │   ├── images
    │   │   ├── jsbundles
    │   │   ├── mask-icon.svg
    │   │   ├── robots.txt
    │   │   └── scripts
    │   └── workspace
    │       ├── devops-demo
    │       │   ├── build.jenkinsfile
    │       │   ├── pom.xml
    │       │   └── src
    │       │       └── main
    │       │           ├── java
    │       │           │   └── com
    │       │           │       └── xingray
    │       │           │           └── devops
    │       │           │               ├── DevopsApplication.java
    │       │           │               └── controller
    │       │           │                   └── HelloController.java
    │       │           └── resources
    │       │               └── application.yaml
    │       ├── devops-demo@2
    │       │   ├── build.jenkinsfile
    │       │   ├── pom.xml
    │       │   ├── src
    │       │   │   └── main
    │       │   │       ├── java
    │       │   │       │   └── com
    │       │   │       │       └── xingray
    │       │   │       │           └── devops
    │       │   │       │               ├── DevopsApplication.java
    │       │   │       │               └── controller
    │       │   │       │                   └── HelloController.java
    │       │   │       └── resources
    │       │   │           └── application.yaml
    │       │   └── target
    │       │       ├── classes
    │       │       │   ├── application.yaml
    │       │       │   └── com
    │       │       │       └── xingray
    │       │       │           └── devops
    │       │       │               ├── DevopsApplication.class
    │       │       │               └── controller
    │       │       │                   └── HelloController.class
    │       │       ├── devops-demo-1.0.0.jar
    │       │       ├── generated-sources
    │       │       │   └── annotations
    │       │       ├── maven-archiver
    │       │       │   └── pom.properties
    │       │       └── maven-status
    │       │           └── maven-compiler-plugin
    │       │               └── compile
    │       │                   └── default-compile
    │       │                       ├── createdFiles.lst
    │       │                       └── inputFiles.lst
    │       ├── devops-demo@2@tmp
    │       │   └── durable-156daf1e
    │       │       ├── jenkins-log.txt
    │       │       └── script.sh
    │       └── devops-demo@tmp
    ├── lib
    │   ├── apk
    │   ├── misc
    │   └── udhcpd
    ├── local
    ├── lock
    │   └── subsys
    ├── log
    ├── mail
    ├── opt
    ├── run -> /run
    ├── spool
    │   ├── cron
    │   │   └── crontabs -> /etc/crontabs
    │   └── mail -> /var/mail
    └── tmp

9574 directories, 60934 files
+ mvn -v
Apache Maven 3.9.3 (21122926829f1ead511c958d89bd2f672198ae9f)
Maven home: /usr/share/maven
Java version: 20.0.2, vendor: Eclipse Adoptium, runtime: /opt/java/openjdk
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.15.0-78-generic", arch: "amd64", family: "unix"
+ mvn clean package '-Dmaven.test.skip=true'
[INFO] Scanning for projects...
Downloading from central: https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-starter-parent/3.1.2/spring-boot-starter-parent-3.1.2.pom
Progress (1): 1.4/13 kB
Progress (1): 2.7/13 kB
Progress (1): 4.1/13 kB
Progress (1): 5.5/13 kB
Progress (1): 6.8/13 kB
Progress (1): 8.2/13 kB
Progress (1): 9.6/13 kB
Progress (1): 11/13 kB 
Progress (1): 12/13 kB
Progress (1): 13 kB   

[INFO] Building jar: /var/jenkins_home/workspace/devops-demo@2/target/devops-demo-1.0.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:18 min
[INFO] Finished at: 2023-08-09T11:28:27Z
[INFO] ------------------------------------------------------------------------
+ pwd
/var/jenkins_home/workspace/devops-demo@2
+ ls -ailh
total 32K    
1320920 drwxr-sr-x    5 root     1000        4.0K Aug  9 11:28 .
1320349 drwxr-sr-x    6 root     1000        4.0K Aug  9 09:45 ..
1320877 drwxr-sr-x    8 root     1000        4.0K Aug  9 11:27 .git
1320949 -rw-r--r--    1 root     1000         528 Aug  9 09:45 .gitignore
1320950 -rw-r--r--    1 root     1000        1.1K Aug  9 11:27 build.jenkinsfile
1321048 -rw-r--r--    1 root     1000        1.2K Aug  9 09:45 pom.xml
1321049 drwxr-sr-x    3 root     1000        4.0K Aug  9 09:45 src
1327706 drwxr-sr-x    6 root     1000        4.0K Aug  9 11:28 target
```

Test:

```shell
Testing..
+ pwd
/var/jenkins_home/workspace/devops-demo
+ ls -ailh
total 28K
1320350 drwxr-sr-x 4 root jenkins 4.0K Aug  9 11:27 .
1320349 drwxr-sr-x 6 root jenkins 4.0K Aug  9 09:45 ..
1320324 drwxr-sr-x 8 root jenkins 4.0K Aug  9 11:27 .git
1320379 -rw-r--r-- 1 root jenkins  528 Aug  9 07:15 .gitignore
1320380 -rw-r--r-- 1 root jenkins 1.1K Aug  9 11:27 build.jenkinsfile
1320432 -rw-r--r-- 1 root jenkins 1.2K Aug  9 07:15 pom.xml
1320433 drwxr-sr-x 3 root jenkins 4.0K Aug  9 07:15 src
```

从日志中可以看到

1 maven容器使用的文件系统是jenkins镜像文件系统+ maven可执行程序, 其中maven的目录为 `/usr/share/maven`, 通过链接

```shell
/usr/bin/mvn -> /usr/share/maven/bin/mvn
```

使得可以在 jenkins_home 目录中使用 mvn 指令,  maven 默认使用的是自带的配置文件, 路径为

```shell
/usr/share/maven/conf/settings.xml
```



存在问题如下:

1 maven的配置使用的是默认配置,  连接的是maven官方仓库下载依赖, 需要修改 maven 的镜像源改为国内的加速服务器, 如阿里云的maven镜像仓库

2 maven的本地仓库地址需要挂载到本地, 避免每次编译时重复下载依赖的jar包

3 maven代理的工作目录是 `/var/jenkins_home/workspace/devops-demo@2` 不是流水线的工作目录 `/var/jenkins_home/workspace/devops-demo`, 后续的流程会无法访问 maven 编译的产物, 如 Test 阶段的默认路径是 `/var/jenkins_home/workspace/devops-demo`, 该目录下就没有 maven 打包产物



解决方案:

1 maven运行时指定setting.xml文件为 `/var/jenkins_home/app/maven/conf/settings.xml`, 后续有其他代理的配置可以统一在 `${JENKINS_HOME}/app` 下创建目录, 如 `${JENKINS_HOME}/app/nodejs`

在宿主机上执行:

```shell
cd /var/lib/docker/volumes/jenkins-data/_data
mkdir -p app/maven/conf
cd app/maven/conf/
vi setting.xml
```

输入配置

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<!--
 | This is the configuration file for Maven. It can be specified at two levels:
 |
 |  1. User Level. This settings.xml file provides configuration for a single user,
 |                 and is normally provided in ${user.home}/.m2/settings.xml.
 |
 |                 NOTE: This location can be overridden with the CLI option:
 |
 |                 -s /path/to/user/settings.xml
 |
 |  2. Global Level. This settings.xml file provides configuration for all Maven
 |                 users on a machine (assuming they're all using the same Maven
 |                 installation). It's normally provided in
 |                 ${maven.conf}/settings.xml.
 |
 |                 NOTE: This location can be overridden with the CLI option:
 |
 |                 -gs /path/to/global/settings.xml
 |
 | The sections in this sample file are intended to give you a running start at
 | getting the most out of your Maven installation. Where appropriate, the default
 | values (values used when the setting is not specified) are provided.
 |
 |-->
<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.2.0 https://maven.apache.org/xsd/settings-1.2.0.xsd">
  <!-- localRepository
   | The path to the local repository maven will use to store artifacts.
   |
   | Default: ${user.home}/.m2/repository
  <localRepository>/path/to/local/repo</localRepository>
  -->
  <localRepository>/app/maven/repository</localRepository>
    
  <!-- interactiveMode
   | This will determine whether maven prompts you when it needs input. If set to false,
   | maven will use a sensible default value, perhaps based on some other setting, for
   | the parameter in question.
   |
   | Default: true
  <interactiveMode>true</interactiveMode>
  -->

  <!-- offline
   | Determines whether maven should attempt to connect to the network when executing a build.
   | This will have an effect on artifact downloads, artifact deployment, and others.
   |
   | Default: false
  <offline>false</offline>
  -->

  <!-- pluginGroups
   | This is a list of additional group identifiers that will be searched when resolving plugins by their prefix, i.e.
   | when invoking a command line like "mvn prefix:goal". Maven will automatically add the group identifiers
   | "org.apache.maven.plugins" and "org.codehaus.mojo" if these are not already contained in the list.
   |-->
  <pluginGroups>
    <!-- pluginGroup
     | Specifies a further group identifier to use for plugin lookup.
    <pluginGroup>com.your.plugins</pluginGroup>
    -->
  </pluginGroups>

  <!-- TODO Since when can proxies be selected as depicted? -->
  <!-- proxies
   | This is a list of proxies which can be used on this machine to connect to the network.
   | Unless otherwise specified (by system property or command-line switch), the first proxy
   | specification in this list marked as active will be used.
   |-->
  <proxies>
    <!-- proxy
     | Specification for one proxy, to be used in connecting to the network.
     |
    <proxy>
      <id>optional</id>
      <active>true</active>
      <protocol>http</protocol>
      <username>proxyuser</username>
      <password>proxypass</password>
      <host>proxy.host.net</host>
      <port>80</port>
      <nonProxyHosts>local.net|some.host.com</nonProxyHosts>
    </proxy>
    -->
  </proxies>

  <!-- servers
   | This is a list of authentication profiles, keyed by the server-id used within the system.
   | Authentication profiles can be used whenever maven must make a connection to a remote server.
   |-->
  <servers>
    <!-- server
     | Specifies the authentication information to use when connecting to a particular server, identified by
     | a unique name within the system (referred to by the 'id' attribute below).
     |
     | NOTE: You should either specify username/password OR privateKey/passphrase, since these pairings are
     |       used together.
     |
    <server>
      <id>deploymentRepo</id>
      <username>repouser</username>
      <password>repopwd</password>
    </server>
    -->

    <!-- Another sample, using keys to authenticate.
    <server>
      <id>siteServer</id>
      <privateKey>/path/to/private/key</privateKey>
      <passphrase>optional; leave empty if not used.</passphrase>
    </server>
    -->
  </servers>

  <!-- mirrors
   | This is a list of mirrors to be used in downloading artifacts from remote repositories.
   |
   | It works like this: a POM may declare a repository to use in resolving certain artifacts.
   | However, this repository may have problems with heavy traffic at times, so people have mirrored
   | it to several places.
   |
   | That repository definition will have a unique id, so we can create a mirror reference for that
   | repository, to be used as an alternate download site. The mirror site will be the preferred
   | server for that repository.
   |-->
  <mirrors>
    <!-- mirror
     | Specifies a repository mirror site to use instead of a given repository. The repository that
     | this mirror serves has an ID that matches the mirrorOf element of this mirror. IDs are used
     | for inheritance and direct lookup purposes, and must be unique across the set of mirrors.
     |
    <mirror>
      <id>mirrorId</id>
      <mirrorOf>repositoryId</mirrorOf>
      <name>Human Readable Name for this Mirror.</name>
      <url>http://my.repository.com/repo/path</url>
    </mirror>
     -->
    <mirror>
      <id>maven-default-http-blocker</id>
      <mirrorOf>external:http:*</mirrorOf>
      <name>Pseudo repository to mirror external repositories initially using HTTP.</name>
      <url>http://0.0.0.0/</url>
      <blocked>true</blocked>
    </mirror>
      
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>

  <!-- profiles
   | This is a list of profiles which can be activated in a variety of ways, and which can modify
   | the build process. Profiles provided in the settings.xml are intended to provide local machine-
   | specific paths and repository locations which allow the build to work in the local environment.
   |
   | For example, if you have an integration testing plugin - like cactus - that needs to know where
   | your Tomcat instance is installed, you can provide a variable here such that the variable is
   | dereferenced during the build process to configure the cactus plugin.
   |
   | As noted above, profiles can be activated in a variety of ways. One way - the activeProfiles
   | section of this document (settings.xml) - will be discussed later. Another way essentially
   | relies on the detection of a property, either matching a particular value for the property,
   | or merely testing its existence. Profiles can also be activated by JDK version prefix, where a
   | value of '1.4' might activate a profile when the build is executed on a JDK version of '1.4.2_07'.
   | Finally, the list of active profiles can be specified directly from the command line.
   |
   | NOTE: For profiles defined in the settings.xml, you are restricted to specifying only artifact
   |       repositories, plugin repositories, and free-form properties to be used as configuration
   |       variables for plugins in the POM.
   |
   |-->
  <profiles>
    <!-- profile
     | Specifies a set of introductions to the build process, to be activated using one or more of the
     | mechanisms described above. For inheritance purposes, and to activate profiles via <activatedProfiles/>
     | or the command line, profiles have to have an ID that is unique.
     |
     | An encouraged best practice for profile identification is to use a consistent naming convention
     | for profiles, such as 'env-dev', 'env-test', 'env-production', 'user-jdcasey', 'user-brett', etc.
     | This will make it more intuitive to understand what the set of introduced profiles is attempting
     | to accomplish, particularly when you only have a list of profile id's for debug.
     |
     | This profile example uses the JDK version to trigger activation, and provides a JDK-specific repo.
    <profile>
      <id>jdk-1.4</id>

      <activation>
        <jdk>1.4</jdk>
      </activation>

      <repositories>
        <repository>
          <id>jdk14</id>
          <name>Repository for JDK 1.4 builds</name>
          <url>http://www.myhost.com/maven/jdk14</url>
          <layout>default</layout>
          <snapshotPolicy>always</snapshotPolicy>
        </repository>
      </repositories>
    </profile>
    -->

    <!--
     | Here is another profile, activated by the property 'target-env' with a value of 'dev', which
     | provides a specific path to the Tomcat instance. To use this, your plugin configuration might
     | hypothetically look like:
     |
     | ...
     | <plugin>
     |   <groupId>org.myco.myplugins</groupId>
     |   <artifactId>myplugin</artifactId>
     |
     |   <configuration>
     |     <tomcatLocation>${tomcatPath}</tomcatLocation>
     |   </configuration>
     | </plugin>
     | ...
     |
     | NOTE: If you just wanted to inject this configuration whenever someone set 'target-env' to
     |       anything, you could just leave off the <value/> inside the activation-property.
     |
    <profile>
      <id>env-dev</id>

      <activation>
        <property>
          <name>target-env</name>
          <value>dev</value>
        </property>
      </activation>

      <properties>
        <tomcatPath>/path/to/tomcat/instance</tomcatPath>
      </properties>
    </profile>
    -->
  </profiles>

  <!-- activeProfiles
   | List of profiles that are active for all builds.
   |
  <activeProfiles>
    <activeProfile>alwaysActiveProfile</activeProfile>
    <activeProfile>anotherAlwaysActiveProfile</activeProfile>
  </activeProfiles>
  -->
</settings>
```

setting.xml中指定了maven容器中的maven仓库地址为 `/app/maven/repository`, 这个路径要在 jenkinsfile 中配置 maven 容器参数时挂载到宿主机的docker的卷中. 另外添加了阿里云maven镜像服务器.  使用 `mvn -s settings.xml xxx`  的方式以指定的配置文件执行 mvn 命令.



2 将maven的配置文件和本地仓库挂载到宿主机, 可以挂载到任意目录, 推荐挂载到 jenkins_home 目录下, 这样可以便于jenkins的备份和恢复

3 强制指定maven代理的工作目录为流水线的默认工作目录



修改如下:

```groovy
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
        build_dir="${WORKSPACE}"
    }

    stages {
        stage('Check') {
            steps {
                echo 'Checking..'
				sh "pwd && ls -ailh"
				sh "java -version"
				sh "git --version"
				sh "docker version"
            }
        }
        stage('Build') {
            agent{
                docker{
                    image 'maven:3.9-eclipse-temurin-20-alpine'
                    args "-v maven-repository:/app/maven/repository"
                }
            }
            steps {
                echo 'Building..'
                echo "build_dir is ${build_dir}"
                sh "cd ${build_dir} && pwd && ls -ailh"
                sh "mvn -v"
                sh "cd ${build_dir} && mvn -s ${JENKINS_HOME}/app/maven/conf/settings.xml clean package -Dmaven.test.skip=true"
                sh "cd ${build_dir} && pwd && ls -ailh"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh "pwd && ls -ailh"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                sh "pwd && ls -ailh"
            }
        }
    }
}
```

更新 build.jenkinsfile 后, 启动流水线, 第一次运行还会下载依赖的jar包, 第二次运行就会使用本地仓库的缓存了.

第二次执行流水线, Build 阶段的日志:

```shell
Selected Git installation does not exist. Using Default
The recommended git tool is: NONE
using credential gitee
 > git rev-parse --resolve-git-dir /var/jenkins_home/workspace/devops-demo@2/.git # timeout=10
Fetching changes from the remote Git repository
 > git config remote.origin.url https://gitee.com/leixing1012/devops-demo.git # timeout=10
Fetching upstream changes from https://gitee.com/leixing1012/devops-demo.git
 > git --version # timeout=10
 > git --version # 'git version 2.36.2'
using GIT_ASKPASS to set credentials gitee账号密码
 > git fetch --tags --force --progress -- https://gitee.com/leixing1012/devops-demo.git +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git rev-parse refs/remotes/origin/master^{commit} # timeout=10
Checking out Revision 7a438143647588caccf2c0d8a0428d2ff769fe79 (refs/remotes/origin/master)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f 7a438143647588caccf2c0d8a0428d2ff769fe79 # timeout=10
Commit message: "更新 build.jenkinsfile 添加环境变量"
+ docker inspect -f . maven:3.9-eclipse-temurin-20-alpine
.
Building..
build_dir is /var/jenkins_home/workspace/devops-demo
+ cd /var/jenkins_home/workspace/devops-demo
+ pwd
/var/jenkins_home/workspace/devops-demo
+ ls -ailh
total 32K    
1320350 drwxr-sr-x    5 root     1000        4.0K Aug  9 14:48 .
1320349 drwxr-sr-x    6 root     1000        4.0K Aug  9 09:45 ..
1320324 drwxr-sr-x    8 root     1000        4.0K Aug  9 14:50 .git
1320379 -rw-r--r--    1 root     1000         528 Aug  9 07:15 .gitignore
1320380 -rw-r--r--    1 root     1000        1.3K Aug  9 14:47 build.jenkinsfile
1320432 -rw-r--r--    1 root     1000        1.2K Aug  9 07:15 pom.xml
1320433 drwxr-sr-x    3 root     1000        4.0K Aug  9 07:15 src
1327793 drwxr-sr-x    6 root     1000        4.0K Aug  9 14:48 target
+ mvn -v
Apache Maven 3.9.3 (21122926829f1ead511c958d89bd2f672198ae9f)
Maven home: /usr/share/maven
Java version: 20.0.2, vendor: Eclipse Adoptium, runtime: /opt/java/openjdk
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.15.0-78-generic", arch: "amd64", family: "unix"
+ cd /var/jenkins_home/workspace/devops-demo
+ mvn -s /var/jenkins_home/app/maven/conf/settings.xml clean package '-Dmaven.test.skip=true'
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< com.xingray:devops-demo >-----------------------
[INFO] Building devops-demo 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.2.0:clean (default-clean) @ devops-demo ---
[INFO] Deleting /var/jenkins_home/workspace/devops-demo/target
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ devops-demo ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] Copying 0 resource from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.11.0:compile (default-compile) @ devops-demo ---
[INFO] Changes detected - recompiling the module! :source
[INFO] Compiling 2 source files with javac [debug release 17] to target/classes
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ devops-demo ---
[INFO] Not copying test resources
[INFO] 
[INFO] --- compiler:3.11.0:testCompile (default-testCompile) @ devops-demo ---
[INFO] Not compiling test sources
[INFO] 
[INFO] --- surefire:3.0.0:test (default-test) @ devops-demo ---
[INFO] Tests are skipped.
[INFO] 
[INFO] --- jar:3.3.0:jar (default-jar) @ devops-demo ---
[INFO] Building jar: /var/jenkins_home/workspace/devops-demo/target/devops-demo-1.0.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  2.400 s
[INFO] Finished at: 2023-08-09T14:50:28Z
[INFO] ------------------------------------------------------------------------
+ cd /var/jenkins_home/workspace/devops-demo
+ pwd
/var/jenkins_home/workspace/devops-demo
+ ls -ailh
total 32K    
1320350 drwxr-sr-x    5 root     1000        4.0K Aug  9 14:50 .
1320349 drwxr-sr-x    6 root     1000        4.0K Aug  9 09:45 ..
1320324 drwxr-sr-x    8 root     1000        4.0K Aug  9 14:50 .git
1320379 -rw-r--r--    1 root     1000         528 Aug  9 07:15 .gitignore
1320380 -rw-r--r--    1 root     1000        1.3K Aug  9 14:47 build.jenkinsfile
1320432 -rw-r--r--    1 root     1000        1.2K Aug  9 07:15 pom.xml
1320433 drwxr-sr-x    3 root     1000        4.0K Aug  9 07:15 src
1327793 drwxr-sr-x    6 root     1000        4.0K Aug  9 14:50 target
```

此时可以看到 target 目录



### 5.5 项目部署

在项目源码根目录下创建 Dockerfile

```dockerfile
FROM eclipse-temurin:20.0.2_9-jdk-alpine
LABEL maintainer=leixing

#启动自行加载   服务名-prod.yml配置
ENV PARAMS="--server.port=8080 --spring.profiles.active=prod"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

COPY target/*.jar /app.jar
EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","java -Xmx1024m -Xms128m -Dfile.encoding=utf8 -Duser.timezone=Asia/Shanghai -Djava.security.egd=file:/dev/./urandom -jar /app.jar ${PARAMS}"]
```

修改 build.jenkinsfile : 

```groovy
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
        build_dir="${WORKSPACE}"
    }

    stages {
        stage('Check') {
            steps {
                echo 'Checking..'
				sh "pwd && ls -ailh"
				sh "java -version"
				sh "git --version"
				sh "docker version"
            }
        }
        stage('Build') {
            agent{
                docker{
                    image 'maven:3.9-eclipse-temurin-20-alpine'
                    args "-v maven-repository:/app/maven/repository"
                }
            }
            steps {
                echo 'Building..'
                echo "build_dir is ${build_dir}"
                sh "cd ${build_dir} && pwd && ls -ailh"
                sh "mvn -v"
                sh "cd ${build_dir} && mvn -s ${JENKINS_HOME}/app/maven/conf/settings.xml clean package -Dmaven.test.skip=true"
                sh "cd ${build_dir} && pwd && ls -ailh"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh "pwd && ls -ailh"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                echo "BUILD_ID is ${BUILD_ID}"
                sh "pwd && ls -ailh"
                sh "if docker ps -a --format \"{{.Names}}\" | grep -wq devops-demo; then docker stop devops-demo && docker rm devops-demo; fi"
                sh "docker build -t devops-demo:snapshot-${BUILD_ID} -f Dockerfile ."
                sh "docker run --name devops-demo -d -p 58080:8080 devops-demo:snapshot-${BUILD_ID}"
            }
        }
    }
}
```



### 5.7 发送报告

邮件发送

后置执行, 在哪个阶段想要感知消息, 可以在该阶段中添加 post 后置执行处理器, 也可以在pipeline中添加全局的处理器

```groovy
post {
  always {
	//Run the steps in the post section regardless of the completion status of the Pipeline’s or stage’s run.
  }
  unstable {
    // Only run the steps in post if the current Pipeline’s run has an "unstable" status, usually caused by test failures, code violations, etc. This is typically denoted by yellow in the web UI.
  }
  notBuilt {
    // One or more steps need to be included within each condition's block.
  }
  cleanup {
    // Run the steps in this post condition after every other post condition has been evaluated, regardless of the Pipeline or stage’s status.
  }
  regression {
    // Only run the steps in post if the current Pipeline’s or status is failure, unstable, or aborted and the previous run was successful.
  }
  aborted {
    // Only run the steps in post if the current Pipeline’s run has an "aborted" status, usually due to the Pipeline being manually aborted. This is typically denoted by gray in the web UI.
  }
  success {
    // Only run the steps in post if the current Pipeline’s or stage’s run has a "success" status, typically denoted by blue or green in the web UI.
  }
  failure {
    // Only run the steps in post if the current Pipeline’s or stage’s run has a "failed" status, typically denoted by red in the web UI.
  }
  unsuccessful {
    // Only run the steps in post if the current Pipeline’s or stage’s run has not a "success" status. This is typically denoted in the web UI depending on the status previously mentioned (for stages this may fire if the build itself is unstable).
  }
  fixed {
    // Only run the steps in post if the current Pipeline’s run is successful and the previous run failed or was unstable.
  }
  changed {
    // Only run the steps in post if the current Pipeline’s run has a different completion status from its previous run.
  }
}
```

示例如下:

```groovy
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
        build_dir="${WORKSPACE}"
    }

    stages {
        stage('Check') {
            steps {
                echo 'Checking..'
				sh "pwd && ls -ailh"
				sh "java -version"
				sh "git --version"
				sh "docker version"
            }
        }
        stage('Build') {
            agent{
                docker{
                    image 'maven:3.9-eclipse-temurin-20-alpine'
                    args "-v maven-repository:/app/maven/repository"
                }
            }
            steps {
                echo 'Building..'
                echo "build_dir is ${build_dir}"
                sh "cd ${build_dir} && pwd && ls -ailh"
                sh "mvn -v"
                sh "cd ${build_dir} && mvn -s ${JENKINS_HOME}/app/maven/conf/settings.xml clean package -Dmaven.test.skip=true"
                sh "cd ${build_dir} && pwd && ls -ailh"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh "pwd && ls -ailh"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                echo "BUILD_ID is ${BUILD_ID}"
                sh "pwd && ls -ailh"
                sh "if docker ps -a --format \"{{.Names}}\" | grep -wq devops-demo; then docker stop devops-demo && docker rm devops-demo; fi"
                sh "docker build -t devops-demo:snapshot-${BUILD_ID} -f Dockerfile ."
                sh "docker run --name devops-demo -d -p 58080:8080 devops-demo:snapshot-${BUILD_ID}"
            }

            post {
              always {
            	//Run the steps in the post section regardless of the completion status of the Pipeline’s or stage’s run.
            	echo "step always"
              }
              unstable {
                // Only run the steps in post if the current Pipeline’s run has an "unstable" status, usually caused by test failures, code violations, etc. This is typically denoted by yellow in the web UI.
                echo "step unstable"
              }
              notBuilt {
                // One or more steps need to be included within each condition's block.
                echo "step notBuilt"
              }
              cleanup {
                // Run the steps in this post condition after every other post condition has been evaluated, regardless of the Pipeline or stage’s status.
                echo "step cleanup"
              }
              regression {
                // Only run the steps in post if the current Pipeline’s or status is failure, unstable, or aborted and the previous run was successful.
                echo "step regression"
              }
              aborted {
                // Only run the steps in post if the current Pipeline’s run has an "aborted" status, usually due to the Pipeline being manually aborted. This is typically denoted by gray in the web UI.
                echo "step aborted"
              }
              success {
                // Only run the steps in post if the current Pipeline’s or stage’s run has a "success" status, typically denoted by blue or green in the web UI.
                echo "step success"
              }
              failure {
                // Only run the steps in post if the current Pipeline’s or stage’s run has a "failed" status, typically denoted by red in the web UI.
                echo "step failure"
              }
              unsuccessful {
                // Only run the steps in post if the current Pipeline’s or stage’s run has not a "success" status. This is typically denoted in the web UI depending on the status previously mentioned (for stages this may fire if the build itself is unstable).
                echo "step unsuccessful"
              }
              fixed {
                // Only run the steps in post if the current Pipeline’s run is successful and the previous run failed or was unstable.
                echo "step fixed"
              }
              changed {
                // Only run the steps in post if the current Pipeline’s run has a different completion status from its previous run.
                echo "step changed"
              }
            }
        }
    }

    post {
      always {
        //Run the steps in the post section regardless of the completion status of the Pipeline’s or stage’s run.
        echo "pipeline always"
      }
      unstable {
        // Only run the steps in post if the current Pipeline’s run has an "unstable" status, usually caused by test failures, code violations, etc. This is typically denoted by yellow in the web UI.
        echo "pipeline unstable"
      }
      notBuilt {
        // One or more steps need to be included within each condition's block.
        echo "pipeline notBuilt"
      }
      cleanup {
        // Run the steps in this post condition after every other post condition has been evaluated, regardless of the Pipeline or stage’s status.
        echo "pipeline cleanup"
      }
      regression {
        // Only run the steps in post if the current Pipeline’s or status is failure, unstable, or aborted and the previous run was successful.
        echo "pipeline regression"
      }
      aborted {
        // Only run the steps in post if the current Pipeline’s run has an "aborted" status, usually due to the Pipeline being manually aborted. This is typically denoted by gray in the web UI.
        echo "pipeline aborted"
      }
      success {
        // Only run the steps in post if the current Pipeline’s or stage’s run has a "success" status, typically denoted by blue or green in the web UI.
        echo "pipeline success"
      }
      failure {
        // Only run the steps in post if the current Pipeline’s or stage’s run has a "failed" status, typically denoted by red in the web UI.
        echo "pipeline failure"
      }
      unsuccessful {
        // Only run the steps in post if the current Pipeline’s or stage’s run has not a "success" status. This is typically denoted in the web UI depending on the status previously mentioned (for stages this may fire if the build itself is unstable).
        echo "pipeline unsuccessful"
      }
      fixed {
        // Only run the steps in post if the current Pipeline’s run is successful and the previous run failed or was unstable.
        echo "pipeline fixed"
      }
      changed {
        // Only run the steps in post if the current Pipeline’s run has a different completion status from its previous run.
        echo "pipeline changed"
      }
    }
}
```

输出:

```shell
Deploying....
BUILD_ID is 28
+ pwd
/var/jenkins_home/workspace/devops-demo
+ ls -ailh
total 40K
1320350 drwxr-sr-x 5 root jenkins 4.0K Aug 10 08:16 .
1320349 drwxr-sr-x 6 root jenkins 4.0K Aug  9 09:45 ..
1320324 drwxr-sr-x 8 root jenkins 4.0K Aug 10 08:16 .git
1320379 -rw-r--r-- 1 root jenkins  528 Aug  9 07:15 .gitignore
1328377 -rw-r--r-- 1 root jenkins  486 Aug  9 15:21 Dockerfile
1320380 -rw-r--r-- 1 root jenkins 6.6K Aug 10 08:16 build.jenkinsfile
1320432 -rw-r--r-- 1 root jenkins 1.8K Aug  9 15:21 pom.xml
1320433 drwxr-sr-x 3 root jenkins 4.0K Aug  9 07:15 src
1327794 drwxr-sr-x 6 root jenkins 4.0K Aug 10 08:16 target
+ docker ps -a --format '{{.Names}}'
+ grep -wq devops-demo
+ docker stop devops-demo
devops-demo
+ docker rm devops-demo
devops-demo
+ docker build -t devops-demo:snapshot-28 -f Dockerfile .
#1 [internal] load .dockerignore
#1 sha256:39cf6b49f897496554e0449d0c56eb4d53030f8b60df2af8172f520e7e97c1b5
#1 transferring context: 2B done
#1 DONE 0.0s

#2 [internal] load build definition from Dockerfile
#2 sha256:fa9377a557135b7fd0510ad77a3b633c005893464350a241a4cb6dda8a50009e
#2 transferring dockerfile: 528B done
#2 DONE 0.0s

#3 [internal] load metadata for docker.io/library/eclipse-temurin:20.0.2_9-jdk-alpine
#3 sha256:7f51d7ff6f99073874646a179ae12beb4a9c1ac06f4636d3c3d08f42659b6af4
#3 DONE 1.9s

#7 [1/3] FROM docker.io/library/eclipse-temurin:20.0.2_9-jdk-alpine@sha256:5235f80f138cdb17ce4c8231c5b889a1f5adab928afe1710354f1a0c58902ffb
#7 sha256:b2b3aa5ba00f3d129d7e114460f387501967d9615f7c3d7baa894728fe529fd5
#7 DONE 0.0s

#5 [internal] load build context
#5 sha256:252aa179469855238297dec18389b33a6c85920d68d3d645db97a9de18cb242d
#5 transferring context: 18.98MB 0.2s done
#5 DONE 0.2s

#6 [2/3] RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
#6 sha256:949096dd90fcd3bce276497fdeaef19c88269ba448ae4ab8bccafef55835dbc4
#6 CACHED

#4 [3/3] COPY target/*.jar /app.jar
#4 sha256:381c44c09c6896585ba74b5ae31c763897a45503bed680212199f700f22ecfc9
#4 DONE 0.1s

#8 exporting to image
#8 sha256:347639a19dfd1607aa16b7c193b93aedb326d02adbd3c898b61994dde3afaefa
#8 exporting layers 0.1s done
#8 writing image sha256:f18c43748e6407343f5edc77b884d9dde67d3405410f496cc0e487dcead5df30
#8 writing image sha256:f18c43748e6407343f5edc77b884d9dde67d3405410f496cc0e487dcead5df30 done
#8 naming to docker.io/library/devops-demo:snapshot-28 done
#8 DONE 0.1s
+ docker run --name devops-demo -d -p 58080:8080 devops-demo:snapshot-28
992d4135bc58970d8ba0b5178d5fa7ec3bf583e4294f5802e2dfa42ca207b3c1
step always
step success
step cleanup
pipeline always
pipeline success
pipeline cleanup
```



要发送邮件, 要使用邮件插件, 邮件发送插件默认已经安装, 但是需要配置

1 配置邮件发送方的邮箱

![image-20230810162803019](assets/006_jenkins实战/image-20230810162803019.png)



![image-20230810162835978](assets/006_jenkins实战/image-20230810162835978.png)



http://192.168.0.140:8080/manage/configure



![image-20230810162937869](assets/006_jenkins实战/image-20230810162937869.png)

在这里配置邮件发送方的邮箱

在下面配置 extented email 插件的邮箱账号配置, 这里使用的是 qq邮箱, 需要现在下面的凭据中添加qq邮箱的账号和**授权码**

![image-20230810190307055](assets/006_jenkins实战/image-20230810190307055.png)



在下面的邮件通知中还需要配置

![image-20230810190504025](assets/006_jenkins实战/image-20230810190504025.png)



配置完成后可以在这里进行测试

![image-20230810190554430](assets/006_jenkins实战/image-20230810190554430.png)



在流水线文件中配置自动发送邮件通知,注意根据实际项目修改模板 / subject( 邮件标题 ) / to ( 收件人 )

```groovy
emailext body: '''
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <title>第${BUILD_NUMBER}次构建日志</title>
</head>

<body leftmargin="8" marginwidth="0" topmargin="8" marginheight="4" offset="0">
    <table width="95%" cellpadding="0" cellspacing="0"
        style="font-size: 11pt;font-family: Tahoma, Arial, Helvetica, sans-serif">
        <h3>本邮件由系统自动发出，请勿回复！</h3>

        <tr>
            <br />各位同事，大家好，以下为 ${PROJECT_DISPLAY_NAME} 项目构建信息</br>
            <td>
                <font color="#CC0000">构建结果 : ${BUILD_STATUS} </font>
            </td>
        </tr>

        <tr>
            <td><br />
                <b>
                    <font color="#0B610B">构建信息</font>
                </b>
                <hr size="2" width="100%" align="center" />
            </td>
        </tr>
        <tr>
            <td>项目名称</td>
            <td>${PROJECT_DISPLAY_NAME}</td>
        </tr>
        <tr>
            <td>项目 Url</td>
            <td><a href="${PROJECT_URL}">${PROJECT_URL}</a></td>
        </tr>
        <tr>
            <td>项目 Url - BlueOcean</td>
            <td><a href="${ENV, var="JOB_DISPLAY_URL"}">${ENV, var="JOB_DISPLAY_URL"}</a></td>
        </tr>
        <tr>
            <td>触发原因</td>
            <td>${CAUSE}</td>
        </tr>
        <tr>
            <td>最近提交</td>
            <td>${CHANGES}</td>
        </tr>
        <tr>
            <td>commit ID</td>
            <td>${ENV, var="GIT_COMMIT"}</td>
        </tr>
        <tr>
            <td>git 地址</td>
            <td><a href="${ENV, var="GIT_URL"}">${ENV, var="GIT_URL"}</a></td>
        </tr>
        <tr>
            <td>git 分支</td>
            <td>${ENV, var="GIT_BRANCH"}</td>
        </tr>
        <tr>
            <td>构建编号</td>
            <td>第 <b>${BUILD_NUMBER}</b> 次构建</td>
        </tr>
        <tr>
            <td>构建 Url</td>
            <td><a href="${BUILD_URL}">${BUILD_URL}</a></td>
        </tr>
        <tr>
            <td>构建 Url - BlueOcean</td>
            <td><a href="${ENV, var="RUN_DISPLAY_URL"}">${ENV, var="RUN_DISPLAY_URL"}</a></td>
        </tr>
        <tr>
            <td>构建状态</td>
            <td>${BUILD_STATUS}</td>
        </tr>
        <tr>
            <td>构建日志</td>
            <td><a href="${BUILD_URL}console">${BUILD_URL}console</a></td>
        </tr>
        <tr>
            <td>构建日志- BlueOcean</td>
            <td><a href="${ENV, var="RUN_ARTIFACTS_DISPLAY_URL"}">${ENV, var="RUN_ARTIFACTS_DISPLAY_URL"}</a></td>
        </tr>
        <tr>
            <td>工作目录</td>
            <td>${ENV, var="WORKSPACE"}</td>
        </tr>
    </table>


    </br></br></br></br></br></br></br></br></br></br>
    <ul>
        <li>BUILD_NUMBER ： ${BUILD_NUMBER}</li>
        <li>BUILD_URL ： ${BUILD_URL}</li>
        <li>JENKINS_URL ： ${JENKINS_URL}</li>
        <li>GIT_BRANCH ： ${GIT_BRANCH}</li>
        <li>JOB_DESCRIPTION ： ${JOB_DESCRIPTION}</li>
        <li>CAUSE ： ${CAUSE}</li>
        <li>CHANGES ： ${CHANGES}</li>
        <li>PROJECT_NAME ： ${PROJECT_NAME}</li>
        <li>PROJECT_DISPLAY_NAME ： ${PROJECT_DISPLAY_NAME}</li>
        <li>PROJECT_URL ： ${PROJECT_URL}</li>
        <li>BUILD_STATUS ： ${BUILD_STATUS}</li>
        <li>CHANGES_SINCE_LAST_SUCCESS ： ${CHANGES_SINCE_LAST_SUCCESS}</li>
        <li>CHANGES_SINCE_LAST_UNSTABLE ： ${CHANGES_SINCE_LAST_UNSTABLE}</li>
        <li>FAILED_TESTS ： ${FAILED_TESTS}</li>
        <li>TEST_COUNTS ： ${TEST_COUNTS}</li>
        <!--<li>BUILD_LOG  ： ${BUILD_LOG }</li>-->

        <li>ENV.JOB_NAME ： ${ENV, var="JOB_NAME"}</li>
        <li>ENV.BUILD_ID ： ${ENV, var="BUILD_ID"}</li>
        <li>ENV.NODE_NAME ： ${ENV, var="NODE_NAME"}</li>
        <li>ENV.BUILD_TAG ： ${ENV, var="BUILD_TAG"}</li>
        <li>ENV.EXECUTOR_NUMBER ： ${ENV, var="EXECUTOR_NUMBER"}</li>
        <li>ENV.JAVA_HOME ： ${ENV, var="JAVA_HOME"}</li>
        <li>ENV.WORKSPACE ： ${ENV, var="WORKSPACE"}</li>
        <li>ENV.SVN_REVISION ： ${ENV, var="SVN_REVISION"}</li>
        <li>ENV.CVS_BRANCH ： ${ENV, var="CVS_BRANCH"}</li>
        <li>ENV.GIT_COMMIT ： ${ENV, var="GIT_COMMIT"}</li>
        <li>ENV.GIT_URL ： ${ENV, var="GIT_URL"}</li>
    </ul>

</body>

</html>
''',
 mimeType: 'text/html', subject: '${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志', to: 'leixing1012@163.com'
```

完整的流水线配置文件为:

```groovy
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
        build_dir="${WORKSPACE}"
    }

    stages {
        stage('Check') {
            steps {
                echo 'Checking..'
				sh "pwd && ls -ailh"
				sh "java -version"
				sh "git --version"
				sh "docker version"
            }
        }
        stage('Build') {
            agent{
                docker{
                    image 'maven:3.9-eclipse-temurin-20-alpine'
                    args "-v maven-repository:/app/maven/repository"
                }
            }
            steps {
                echo 'Building..'
                echo "build_dir is ${build_dir}"
                sh "cd ${build_dir} && pwd && ls -ailh"
                sh "mvn -v"
                sh "cd ${build_dir} && mvn -s ${JENKINS_HOME}/app/maven/conf/settings.xml clean package -Dmaven.test.skip=true"
                sh "cd ${build_dir} && pwd && ls -ailh"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh "pwd && ls -ailh"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                echo "BUILD_ID is ${BUILD_ID}"
                sh "pwd && ls -ailh"
                sh "if docker ps -a --format \"{{.Names}}\" | grep -wq devops-demo; then docker stop devops-demo && docker rm devops-demo; fi"
                sh "docker build -t devops-demo:snapshot-${BUILD_ID} -f Dockerfile ."
                sh "docker run --name devops-demo -d -p 58080:8080 devops-demo:snapshot-${BUILD_ID}"
            }

            post {
              always {
            	//Run the steps in the post section regardless of the completion status of the Pipeline’s or stage’s run.
            	echo "step always"
              }
              unstable {
                // Only run the steps in post if the current Pipeline’s run has an "unstable" status, usually caused by test failures, code violations, etc. This is typically denoted by yellow in the web UI.
                echo "step unstable"
              }
              notBuilt {
                // One or more steps need to be included within each condition's block.
                echo "step notBuilt"
              }
              cleanup {
                // Run the steps in this post condition after every other post condition has been evaluated, regardless of the Pipeline or stage’s status.
                echo "step cleanup"
              }
              regression {
                // Only run the steps in post if the current Pipeline’s or status is failure, unstable, or aborted and the previous run was successful.
                echo "step regression"
              }
              aborted {
                // Only run the steps in post if the current Pipeline’s run has an "aborted" status, usually due to the Pipeline being manually aborted. This is typically denoted by gray in the web UI.
                echo "step aborted"
              }
              success {
                // Only run the steps in post if the current Pipeline’s or stage’s run has a "success" status, typically denoted by blue or green in the web UI.
                echo "step success"
              }
              failure {
                // Only run the steps in post if the current Pipeline’s or stage’s run has a "failed" status, typically denoted by red in the web UI.
                echo "step failure"
              }
              unsuccessful {
                // Only run the steps in post if the current Pipeline’s or stage’s run has not a "success" status. This is typically denoted in the web UI depending on the status previously mentioned (for stages this may fire if the build itself is unstable).
                echo "step unsuccessful"
              }
              fixed {
                // Only run the steps in post if the current Pipeline’s run is successful and the previous run failed or was unstable.
                echo "step fixed"
              }
              changed {
                // Only run the steps in post if the current Pipeline’s run has a different completion status from its previous run.
                echo "step changed"
              }
            }
        }

        stage('Release') {
            input {
                message "Should we continue?"
                ok "确认"
                submitter "alice,bob"
                parameters {
                    string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
                    string(defaultValue: 'input text', description: '字符类型文本', name: 'input_string', trim: true)
                    booleanParam(defaultValue: true, description: '布尔类型输入', name: 'input_boolean')
                    text(defaultValue: 'input-text-default', description: '文本类型输入', name: 'input_text')
                    choice(choices: ['option-01', 'option-02', 'option-03'], description: '选项输入', name: 'input_options')
                    credentials(credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl', defaultValue: 'gitee', description: '凭据类型参数', name: 'input_credentials', required: true)
                    password(defaultValue: '123456', description: '密码类型输入', name: 'input_password')
                }
            }

            steps {
                echo 'Release..'
                sh "pwd && ls -ailh"

                echo "PERSON : ${PERSON}"

                echo "input_credentials : ${input_credentials}"
                echo "input_string : ${input_string}"
                echo "input_password : ${input_password}"
                echo "input_boolean : ${input_boolean}"
                echo "input_text : ${input_text}"
                echo "input_options : ${input_options}"

                echo 'Release success'
            }
        }
    }

    post {
      always {
        //Run the steps in the post section regardless of the completion status of the Pipeline’s or stage’s run.
        echo "pipeline always"
      }
      unstable {
        // Only run the steps in post if the current Pipeline’s run has an "unstable" status, usually caused by test failures, code violations, etc. This is typically denoted by yellow in the web UI.
        echo "pipeline unstable"
      }
      notBuilt {
        // One or more steps need to be included within each condition's block.
        echo "pipeline notBuilt"
      }
      cleanup {
        // Run the steps in this post condition after every other post condition has been evaluated, regardless of the Pipeline or stage’s status.
        echo "pipeline cleanup"
      }
      regression {
        // Only run the steps in post if the current Pipeline’s or status is failure, unstable, or aborted and the previous run was successful.
        echo "pipeline regression"
      }
      aborted {
        // Only run the steps in post if the current Pipeline’s run has an "aborted" status, usually due to the Pipeline being manually aborted. This is typically denoted by gray in the web UI.
        echo "pipeline aborted"
      }
      success {
        // Only run the steps in post if the current Pipeline’s or stage’s run has a "success" status, typically denoted by blue or green in the web UI.
        echo "pipeline success"
        echo "BUILD_ID is ${BUILD_ID}"

        sh "printenv"

        echo "JOB_DISPLAY_URL is ${JOB_DISPLAY_URL}"
        echo "RUN_DISPLAY_URL is ${RUN_DISPLAY_URL}"
        echo "RUN_ARTIFACTS_DISPLAY_URL is ${RUN_ARTIFACTS_DISPLAY_URL}"
        echo "RUN_CHANGES_DISPLAY_URL is ${RUN_CHANGES_DISPLAY_URL}"
        echo "RUN_TESTS_DISPLAY_URL is ${RUN_TESTS_DISPLAY_URL}"
        echo "CI is ${CI}"
        echo "BUILD_NUMBER is ${BUILD_NUMBER}"
        echo "BUILD_ID is ${BUILD_ID}"
        echo "BUILD_DISPLAY_NAME is ${BUILD_DISPLAY_NAME}"
        echo "JOB_NAME is ${JOB_NAME}"
        echo "JOB_BASE_NAME is ${JOB_BASE_NAME}"
        echo "BUILD_TAG is ${BUILD_TAG}"
        echo "EXECUTOR_NUMBER is ${EXECUTOR_NUMBER}"
        echo "NODE_NAME is ${NODE_NAME}"
        echo "NODE_LABELS is ${NODE_LABELS}"
        echo "WORKSPACE is ${WORKSPACE}"
        echo "WORKSPACE_TMP is ${WORKSPACE_TMP}"
        echo "JENKINS_HOME is ${JENKINS_HOME}"
        echo "JENKINS_URL is ${JENKINS_URL}"
        echo "BUILD_URL is ${BUILD_URL}"
        echo "JOB_URL is ${JOB_URL}"


        echo "currentBuild.number is ${currentBuild.number}"
        echo "currentBuild.result is ${currentBuild.result}"
        echo "currentBuild.currentResult is ${currentBuild.currentResult}"
        echo "currentBuild.displayName is ${currentBuild.displayName}"
        echo "currentBuild.fullDisplayName is ${currentBuild.fullDisplayName}"
        echo "currentBuild.projectName is ${currentBuild.projectName}"
        echo "currentBuild.fullProjectName is ${currentBuild.fullProjectName}"
        echo "currentBuild.description is ${currentBuild.description}"
        echo "currentBuild.id is ${currentBuild.id}"
        echo "currentBuild.timeInMillis is ${currentBuild.timeInMillis}"
        echo "currentBuild.startTimeInMillis is ${currentBuild.startTimeInMillis}"
        echo "currentBuild.duration is ${currentBuild.duration}"
        echo "currentBuild.durationString is ${currentBuild.durationString}"
        echo "currentBuild.previousBuild is ${currentBuild.previousBuild}"
        echo "currentBuild.previousBuildInProgress is ${currentBuild.previousBuildInProgress}"
        echo "currentBuild.previousBuiltBuild is ${currentBuild.previousBuiltBuild}"
        echo "currentBuild.previousCompletedBuild is ${currentBuild.previousCompletedBuild}"
        echo "currentBuild.previousFailedBuild is ${currentBuild.previousFailedBuild}"
        echo "currentBuild.previousNotFailedBuild is ${currentBuild.previousNotFailedBuild}"
        echo "currentBuild.previousSuccessfulBuild is ${currentBuild.previousSuccessfulBuild}"
        echo "currentBuild.nextBuild is ${currentBuild.nextBuild}"
        echo "currentBuild.absoluteUrl is ${currentBuild.absoluteUrl}"
        echo "currentBuild.buildVariables is ${currentBuild.buildVariables}"
        echo "currentBuild.changeSets is ${currentBuild.changeSets}"
        echo "currentBuild.upstreamBuilds is ${currentBuild.upstreamBuilds}"
//         echo "currentBuild.rawBuild is ${currentBuild.rawBuild}"
        echo "currentBuild.keepLog is ${currentBuild.keepLog}"
        echo "currentBuild.previousNotFailedBuild is ${currentBuild.previousNotFailedBuild}"
        echo "currentBuild.previousNotFailedBuild is ${currentBuild.previousNotFailedBuild}"

//         emailext body: '${FILE, path="email.html"}',
        emailext body: '''
        <!DOCTYPE html>
        <html lang="zh-CN">

        <head>
            <meta charset="UTF-8">
            <title>第${BUILD_NUMBER}次构建日志</title>
        </head>

        <body leftmargin="8" marginwidth="0" topmargin="8" marginheight="4" offset="0">
            <table width="95%" cellpadding="0" cellspacing="0"
                style="font-size: 11pt;font-family: Tahoma, Arial, Helvetica, sans-serif">
                <h3>本邮件由系统自动发出，请勿回复！</h3>

                <tr>
                    <br />各位同事，大家好，以下为 ${PROJECT_DISPLAY_NAME} 项目构建信息</br>
                    <td>
                        <font color="#CC0000">构建结果 : ${BUILD_STATUS} </font>
                    </td>
                </tr>

                <tr>
                    <td><br />
                        <b>
                            <font color="#0B610B">构建信息</font>
                        </b>
                        <hr size="2" width="100%" align="center" />
                    </td>
                </tr>
                <tr>
                    <td>项目名称</td>
                    <td>${PROJECT_DISPLAY_NAME}</td>
                </tr>
                <tr>
                    <td>项目 Url</td>
                    <td><a href="${PROJECT_URL}">${PROJECT_URL}</a></td>
                </tr>
                <tr>
                    <td>项目 Url - BlueOcean</td>
                    <td><a href="${ENV, var="JOB_DISPLAY_URL"}">${ENV, var="JOB_DISPLAY_URL"}</a></td>
                </tr>
                <tr>
                    <td>触发原因</td>
                    <td>${CAUSE}</td>
                </tr>
                <tr>
                    <td>最近提交</td>
                    <td>${CHANGES}</td>
                </tr>
                <tr>
                    <td>commit ID</td>
                    <td>${ENV, var="GIT_COMMIT"}</td>
                </tr>
                <tr>
                    <td>git 地址</td>
                    <td><a href="${ENV, var="GIT_URL"}">${ENV, var="GIT_URL"}</a></td>
                </tr>
                <tr>
                    <td>git 分支</td>
                    <td>${ENV, var="GIT_BRANCH"}</td>
                </tr>
                <tr>
                    <td>构建编号</td>
                    <td>第 <b>${BUILD_NUMBER}</b> 次构建</td>
                </tr>
                <tr>
                    <td>构建 Url</td>
                    <td><a href="${BUILD_URL}">${BUILD_URL}</a></td>
                </tr>
                <tr>
                    <td>构建 Url - BlueOcean</td>
                    <td><a href="${ENV, var="RUN_DISPLAY_URL"}">${ENV, var="RUN_DISPLAY_URL"}</a></td>
                </tr>
                <tr>
                    <td>构建状态</td>
                    <td>${BUILD_STATUS}</td>
                </tr>
                <tr>
                    <td>构建日志</td>
                    <td><a href="${BUILD_URL}console">${BUILD_URL}console</a></td>
                </tr>
                <tr>
                    <td>构建日志- BlueOcean</td>
                    <td><a href="${ENV, var="RUN_ARTIFACTS_DISPLAY_URL"}">${ENV, var="RUN_ARTIFACTS_DISPLAY_URL"}</a></td>
                </tr>
                <tr>
                    <td>工作目录</td>
                    <td>${ENV, var="WORKSPACE"}</td>
                </tr>
            </table>


            </br></br></br></br></br></br></br></br></br></br>
            <ul>
                <li>BUILD_NUMBER ： ${BUILD_NUMBER}</li>
                <li>BUILD_URL ： ${BUILD_URL}</li>
                <li>JENKINS_URL ： ${JENKINS_URL}</li>
                <li>GIT_BRANCH ： ${GIT_BRANCH}</li>
                <li>JOB_DESCRIPTION ： ${JOB_DESCRIPTION}</li>
                <li>CAUSE ： ${CAUSE}</li>
                <li>CHANGES ： ${CHANGES}</li>
                <li>PROJECT_NAME ： ${PROJECT_NAME}</li>
                <li>PROJECT_DISPLAY_NAME ： ${PROJECT_DISPLAY_NAME}</li>
                <li>PROJECT_URL ： ${PROJECT_URL}</li>
                <li>BUILD_STATUS ： ${BUILD_STATUS}</li>
                <li>CHANGES_SINCE_LAST_SUCCESS ： ${CHANGES_SINCE_LAST_SUCCESS}</li>
                <li>CHANGES_SINCE_LAST_UNSTABLE ： ${CHANGES_SINCE_LAST_UNSTABLE}</li>
                <li>FAILED_TESTS ： ${FAILED_TESTS}</li>
                <li>TEST_COUNTS ： ${TEST_COUNTS}</li>
                <!--<li>BUILD_LOG  ： ${BUILD_LOG }</li>-->

                <li>ENV.JOB_NAME ： ${ENV, var="JOB_NAME"}</li>
                <li>ENV.BUILD_ID ： ${ENV, var="BUILD_ID"}</li>
                <li>ENV.NODE_NAME ： ${ENV, var="NODE_NAME"}</li>
                <li>ENV.BUILD_TAG ： ${ENV, var="BUILD_TAG"}</li>
                <li>ENV.EXECUTOR_NUMBER ： ${ENV, var="EXECUTOR_NUMBER"}</li>
                <li>ENV.JAVA_HOME ： ${ENV, var="JAVA_HOME"}</li>
                <li>ENV.WORKSPACE ： ${ENV, var="WORKSPACE"}</li>
                <li>ENV.SVN_REVISION ： ${ENV, var="SVN_REVISION"}</li>
                <li>ENV.CVS_BRANCH ： ${ENV, var="CVS_BRANCH"}</li>
                <li>ENV.GIT_COMMIT ： ${ENV, var="GIT_COMMIT"}</li>
                <li>ENV.GIT_URL ： ${ENV, var="GIT_URL"}</li>
            </ul>

        </body>

        </html>
        ''',
         mimeType: 'text/html', subject: '${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志', to: 'leixing1012@163.com'
      }

      failure {
        // Only run the steps in post if the current Pipeline’s or stage’s run has a "failed" status, typically denoted by red in the web UI.
        echo "pipeline failure"
      }

      unsuccessful {
        // Only run the steps in post if the current Pipeline’s or stage’s run has not a "success" status. This is typically denoted in the web UI depending on the status previously mentioned (for stages this may fire if the build itself is unstable).
        echo "pipeline unsuccessful"
      }

      fixed {
        // Only run the steps in post if the current Pipeline’s run is successful and the previous run failed or was unstable.
        echo "pipeline fixed"
      }

      changed {
        // Only run the steps in post if the current Pipeline’s run has a different completion status from its previous run.
        echo "pipeline changed"
      }
    }
}
```

可以将邮件的模版保存到单独的文件

```groovy
emailext body: '${FILE, path="email.html"}',
```

在body中可以使用 `${ENV, var="JOB_NAME"}` 的方式引用环境变量中的值, 注意**不能**直接使用 `${JOB_NAME}`



其他的通知方式, 如 短信通知 / 钉钉通知 / 微信通知 可以使用 http 请求的方式实现

```shell
curl http://www.wechat.com/xxx/xxx
```



### 5.6 手动确认

https://www.jenkins.io/doc/pipeline/steps/pipeline-input-step/



```groovy
stage('Release') {
    input {
        message "Should we continue?"
        ok "确认"
        submitter "alice,bob"
        parameters {
            string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
            string(defaultValue: 'input text', description: '字符类型文本', name: 'input_string', trim: true)
            booleanParam(defaultValue: true, description: '布尔类型输入', name: 'input_boolean')
            text(defaultValue: 'input-text-default', description: '文本类型输入', name: 'input_text')
            choice(choices: ['option-01', 'option-02', 'option-03'], description: '选项输入', name: 'input_options')
            credentials(credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl', defaultValue: 'gitee', description: '凭据类型参数', name: 'input_credentials', required: true)
            password(defaultValue: '123456', description: '密码类型输入', name: 'input_password')
        }
    }

    steps {
        echo 'Release..'
        sh "pwd && ls -ailh"

        echo "PERSON : ${PERSON}"

        echo "input_credentials : ${input_credentials}"
        echo "input_string : ${input_string}"
        echo "input_password : ${input_password}"
        echo "input_boolean : ${input_boolean}"
        echo "input_text : ${input_text}"
        echo "input_options : ${input_options}"

        echo 'Release success'
    }
}
```

![image-20230811062858030](assets/006_jenkins实战/image-20230811062858030.png)

![image-20230811062909963](assets/006_jenkins实战/image-20230811062909963.png)

### 5.7 引用凭证

1. 通过环境变量

```groovy
environment{
    // 自动添加 GITEE_SECRET GITEE_SECRET 参数
    GITEE_SECRET = credentials("gitee")
}

// ...
stage("push image"){
    echo "GITEE_SECRET_USR : ${GITEE_SECRET_USR}"
    echo "GITEE_SECRET_PSW : ${GITEE_SECRET_PSW}"
	// ...
    sh "docker login registry.cn-hangzhou.aliyuncs.com -u ${GITEE_SECRET_USR} -p ${GITEE_SECRET_PSW}"
    sh "docker tag xxx:v1.x registry.cn-hangzhou.aliyuncs.com/registry_namespace_xxx/xxx:v1.x"
    sh "docker push registry.cn-hangzhou.aliyuncs.com/registry_namespace_xxx/xxx:v1.x"
    
    
}
```

2 通过凭证环境

```groovy
// ...
stage("push image"){
    withCredentials([usernamePassword(credentialsId: 'gitee', passwordVariable: 'password', usernameVariable: 'username')]) {
        echo "username : ${username}"
        echo "password : ${password}"
        // ...
        sh "docker login registry.cn-hangzhou.aliyuncs.com -u ${username} -p ${password}"
        sh "docker tag xxx:v1.x registry.cn-hangzhou.aliyuncs.com/registry_namespace_xxx/xxx:v1.x"
        sh "docker push registry.cn-hangzhou.aliyuncs.com/registry_namespace_xxx/xxx:v1.x"
    }
}
```



### 5.8 脚本

示例 :

```groovy
script {
    def option = "${input_options}"

    if(option == "option-01"){
        echo "option is option-01"
    }else if(option == "option-02"){
        sh "echo option is option-02"
    }else {
        sh "echo option is option-03"
    }
}
```

完整示例 :

```groovy
stage('Release') {
    input {
        message "Should we continue?"
        ok "确认"
        submitter "alice,bob"
        parameters {
            string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
            string(defaultValue: 'input text', description: '字符类型文本', name: 'input_string', trim: true)
            booleanParam(defaultValue: true, description: '布尔类型输入', name: 'input_boolean')
            text(defaultValue: 'input-text-default', description: '文本类型输入', name: 'input_text')
            choice(choices: ['option-01', 'option-02', 'option-03'], description: '选项输入', name: 'input_options')
            credentials(credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl', defaultValue: 'gitee', description: '凭据类型参数', name: 'input_credentials', required: true)
            password(defaultValue: '123456', description: '密码类型输入', name: 'input_password')
        }
    }

    steps {
        echo 'Release..'
        sh "pwd && ls -ailh"

        echo "PERSON : ${PERSON}"

        echo "input_credentials : ${input_credentials}"
        echo "input_string : ${input_string}"
        echo "input_password : ${input_password}"
        echo "input_boolean : ${input_boolean}"
        echo "input_text : ${input_text}"
        echo "input_options : ${input_options}"

        script {
            def option = "${input_options}"

            if(option == "option-01"){
                echo "option is option-01"
            }else if(option == "option-02"){
                sh "echo option is option-02"
            }else {
                sh "echo option is option-03"
            }
        }

        echo "GITEE_SECRET_USR : ${GITEE_SECRET_USR}"
        echo "GITEE_SECRET_PSW : ${GITEE_SECRET_PSW}"

        withCredentials([usernamePassword(credentialsId: 'gitee', passwordVariable: 'password', usernameVariable: 'username')]){
            echo "username : ${username}"
            echo "password : ${password}"
        }

        echo 'Release success'
    }
}
```



通过脚本可以实现: 一次代码提交后, 分析是哪个模块的代码进行了修改 ( 新增 / 删除 / 修改 ) , 然后分析出需要重新部署哪些微服务 

Generic Webhook Trigger + script



任意用户提交一个 issue , Jenkins 触发自动根据 issue 创建一个 fix-xxx 分支, 让开发确认和修复这个 issue , 转成 PR





完整的 Jenkinsfile 示例 :

```
pipeline {
    agent any

    environment {
        env_key01 = "env_value01"
        env_key02 = "env_value02"
        env_key03 = "env_value03"
        build_dir = "${WORKSPACE}"
        GITEE_SECRET = credentials("gitee")
    }

    stages {
        stage('Check') {
            steps {
                echo 'Checking..'
				sh "pwd && ls -ailh"
				sh "java -version"
				sh "git --version"
				sh "docker version"
            }
        }
        stage('Build') {
            agent{
                docker{
                    image 'maven:3.9-eclipse-temurin-20-alpine'
                    args "-v maven-repository:/app/maven/repository"
                }
            }
            steps {
                echo 'Building..'
                echo "build_dir is ${build_dir}"
                sh "cd ${build_dir} && pwd && ls -ailh"
                sh "mvn -v"
                sh "cd ${build_dir} && mvn -s ${JENKINS_HOME}/app/maven/conf/settings.xml clean package -Dmaven.test.skip=true"
                sh "cd ${build_dir} && pwd && ls -ailh"
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh "pwd && ls -ailh"
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                echo "BUILD_ID is ${BUILD_ID}"
                sh "pwd && ls -ailh"
                sh "if docker ps -a --format \"{{.Names}}\" | grep -wq devops-demo; then docker stop devops-demo && docker rm devops-demo; fi"
                sh "docker build -t devops-demo:snapshot-${BUILD_ID} -f Dockerfile ."
                sh "docker run --name devops-demo -d -p 58080:8080 devops-demo:snapshot-${BUILD_ID}"
            }

            post {
              always {
            	//Run the steps in the post section regardless of the completion status of the Pipeline’s or stage’s run.
            	echo "step always"
              }
              unstable {
                // Only run the steps in post if the current Pipeline’s run has an "unstable" status, usually caused by test failures, code violations, etc. This is typically denoted by yellow in the web UI.
                echo "step unstable"
              }
              notBuilt {
                // One or more steps need to be included within each condition's block.
                echo "step notBuilt"
              }
              cleanup {
                // Run the steps in this post condition after every other post condition has been evaluated, regardless of the Pipeline or stage’s status.
                echo "step cleanup"
              }
              regression {
                // Only run the steps in post if the current Pipeline’s or status is failure, unstable, or aborted and the previous run was successful.
                echo "step regression"
              }
              aborted {
                // Only run the steps in post if the current Pipeline’s run has an "aborted" status, usually due to the Pipeline being manually aborted. This is typically denoted by gray in the web UI.
                echo "step aborted"
              }
              success {
                // Only run the steps in post if the current Pipeline’s or stage’s run has a "success" status, typically denoted by blue or green in the web UI.
                echo "step success"
              }
              failure {
                // Only run the steps in post if the current Pipeline’s or stage’s run has a "failed" status, typically denoted by red in the web UI.
                echo "step failure"
              }
              unsuccessful {
                // Only run the steps in post if the current Pipeline’s or stage’s run has not a "success" status. This is typically denoted in the web UI depending on the status previously mentioned (for stages this may fire if the build itself is unstable).
                echo "step unsuccessful"
              }
              fixed {
                // Only run the steps in post if the current Pipeline’s run is successful and the previous run failed or was unstable.
                echo "step fixed"
              }
              changed {
                // Only run the steps in post if the current Pipeline’s run has a different completion status from its previous run.
                echo "step changed"
              }
            }
        }

        stage('Release') {
            input {
                message "Should we continue?"
                ok "确认"
                submitter "alice,bob"
                parameters {
                    string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
                    string(defaultValue: 'input text', description: '字符类型文本', name: 'input_string', trim: true)
                    booleanParam(defaultValue: true, description: '布尔类型输入', name: 'input_boolean')
                    text(defaultValue: 'input-text-default', description: '文本类型输入', name: 'input_text')
                    choice(choices: ['option-01', 'option-02', 'option-03'], description: '选项输入', name: 'input_options')
                    credentials(credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl', defaultValue: 'gitee', description: '凭据类型参数', name: 'input_credentials', required: true)
                    password(defaultValue: '123456', description: '密码类型输入', name: 'input_password')
                }
            }

            steps {
                echo 'Release..'
                sh "pwd && ls -ailh"

                echo "PERSON : ${PERSON}"

                echo "input_credentials : ${input_credentials}"
                echo "input_string : ${input_string}"
                echo "input_password : ${input_password}"
                echo "input_boolean : ${input_boolean}"
                echo "input_text : ${input_text}"
                echo "input_options : ${input_options}"

                script {
                    def option = "${input_options}"

                    if(option == "option-01"){
                        echo "option is option-01"
                    }else if(option == "option-02"){
                        sh "echo option is option-02"
                    }else {
                        sh "echo option is option-03"
                    }
                }

                echo "GITEE_SECRET_USR : ${GITEE_SECRET_USR}"
                echo "GITEE_SECRET_PSW : ${GITEE_SECRET_PSW}"

                withCredentials([usernamePassword(credentialsId: 'gitee', passwordVariable: 'password', usernameVariable: 'username')]) {
                    echo "username : ${username}"
                    echo "password : ${password}"
                }

                echo 'Release success'
            }
        }
    }

    post {
      always {
        //Run the steps in the post section regardless of the completion status of the Pipeline’s or stage’s run.
        echo "pipeline always"
      }
      unstable {
        // Only run the steps in post if the current Pipeline’s run has an "unstable" status, usually caused by test failures, code violations, etc. This is typically denoted by yellow in the web UI.
        echo "pipeline unstable"
      }
      notBuilt {
        // One or more steps need to be included within each condition's block.
        echo "pipeline notBuilt"
      }
      cleanup {
        // Run the steps in this post condition after every other post condition has been evaluated, regardless of the Pipeline or stage’s status.
        echo "pipeline cleanup"
      }
      regression {
        // Only run the steps in post if the current Pipeline’s or status is failure, unstable, or aborted and the previous run was successful.
        echo "pipeline regression"
      }
      aborted {
        // Only run the steps in post if the current Pipeline’s run has an "aborted" status, usually due to the Pipeline being manually aborted. This is typically denoted by gray in the web UI.
        echo "pipeline aborted"
      }
      success {
        // Only run the steps in post if the current Pipeline’s or stage’s run has a "success" status, typically denoted by blue or green in the web UI.
        echo "pipeline success"
        echo "BUILD_ID is ${BUILD_ID}"

        sh "printenv"

        echo "JOB_DISPLAY_URL is ${JOB_DISPLAY_URL}"
        echo "RUN_DISPLAY_URL is ${RUN_DISPLAY_URL}"
        echo "RUN_ARTIFACTS_DISPLAY_URL is ${RUN_ARTIFACTS_DISPLAY_URL}"
        echo "RUN_CHANGES_DISPLAY_URL is ${RUN_CHANGES_DISPLAY_URL}"
        echo "RUN_TESTS_DISPLAY_URL is ${RUN_TESTS_DISPLAY_URL}"
        echo "CI is ${CI}"
        echo "BUILD_NUMBER is ${BUILD_NUMBER}"
        echo "BUILD_ID is ${BUILD_ID}"
        echo "BUILD_DISPLAY_NAME is ${BUILD_DISPLAY_NAME}"
        echo "JOB_NAME is ${JOB_NAME}"
        echo "JOB_BASE_NAME is ${JOB_BASE_NAME}"
        echo "BUILD_TAG is ${BUILD_TAG}"
        echo "EXECUTOR_NUMBER is ${EXECUTOR_NUMBER}"
        echo "NODE_NAME is ${NODE_NAME}"
        echo "NODE_LABELS is ${NODE_LABELS}"
        echo "WORKSPACE is ${WORKSPACE}"
        echo "WORKSPACE_TMP is ${WORKSPACE_TMP}"
        echo "JENKINS_HOME is ${JENKINS_HOME}"
        echo "JENKINS_URL is ${JENKINS_URL}"
        echo "BUILD_URL is ${BUILD_URL}"
        echo "JOB_URL is ${JOB_URL}"


        echo "currentBuild.number is ${currentBuild.number}"
        echo "currentBuild.result is ${currentBuild.result}"
        echo "currentBuild.currentResult is ${currentBuild.currentResult}"
        echo "currentBuild.displayName is ${currentBuild.displayName}"
        echo "currentBuild.fullDisplayName is ${currentBuild.fullDisplayName}"
        echo "currentBuild.projectName is ${currentBuild.projectName}"
        echo "currentBuild.fullProjectName is ${currentBuild.fullProjectName}"
        echo "currentBuild.description is ${currentBuild.description}"
        echo "currentBuild.id is ${currentBuild.id}"
        echo "currentBuild.timeInMillis is ${currentBuild.timeInMillis}"
        echo "currentBuild.startTimeInMillis is ${currentBuild.startTimeInMillis}"
        echo "currentBuild.duration is ${currentBuild.duration}"
        echo "currentBuild.durationString is ${currentBuild.durationString}"
        echo "currentBuild.previousBuild is ${currentBuild.previousBuild}"
        echo "currentBuild.previousBuildInProgress is ${currentBuild.previousBuildInProgress}"
        echo "currentBuild.previousBuiltBuild is ${currentBuild.previousBuiltBuild}"
        echo "currentBuild.previousCompletedBuild is ${currentBuild.previousCompletedBuild}"
        echo "currentBuild.previousFailedBuild is ${currentBuild.previousFailedBuild}"
        echo "currentBuild.previousNotFailedBuild is ${currentBuild.previousNotFailedBuild}"
        echo "currentBuild.previousSuccessfulBuild is ${currentBuild.previousSuccessfulBuild}"
        echo "currentBuild.nextBuild is ${currentBuild.nextBuild}"
        echo "currentBuild.absoluteUrl is ${currentBuild.absoluteUrl}"
        echo "currentBuild.buildVariables is ${currentBuild.buildVariables}"
        echo "currentBuild.changeSets is ${currentBuild.changeSets}"
        echo "currentBuild.upstreamBuilds is ${currentBuild.upstreamBuilds}"
//         echo "currentBuild.rawBuild is ${currentBuild.rawBuild}"
        echo "currentBuild.keepLog is ${currentBuild.keepLog}"
        echo "currentBuild.previousNotFailedBuild is ${currentBuild.previousNotFailedBuild}"
        echo "currentBuild.previousNotFailedBuild is ${currentBuild.previousNotFailedBuild}"

//         emailext body: '${FILE, path="email.html"}',
        emailext body: '''
        <!DOCTYPE html>
        <html lang="zh-CN">

        <head>
            <meta charset="UTF-8">
            <title>第${BUILD_NUMBER}次构建日志</title>
        </head>

        <body leftmargin="8" marginwidth="0" topmargin="8" marginheight="4" offset="0">
            <table width="95%" cellpadding="0" cellspacing="0"
                style="font-size: 11pt;font-family: Tahoma, Arial, Helvetica, sans-serif">
                <h3>本邮件由系统自动发出，请勿回复！</h3>

                <tr>
                    <br />各位同事，大家好，以下为 ${PROJECT_DISPLAY_NAME} 项目构建信息</br>
                    <td>
                        <font color="#CC0000">构建结果 : ${BUILD_STATUS} </font>
                    </td>
                </tr>

                <tr>
                    <td><br />
                        <b>
                            <font color="#0B610B">构建信息</font>
                        </b>
                        <hr size="2" width="100%" align="center" />
                    </td>
                </tr>
                <tr>
                    <td>项目名称</td>
                    <td>${PROJECT_DISPLAY_NAME}</td>
                </tr>
                <tr>
                    <td>项目 Url</td>
                    <td><a href="${PROJECT_URL}">${PROJECT_URL}</a></td>
                </tr>
                <tr>
                    <td>项目 Url - BlueOcean</td>
                    <td><a href="${ENV, var="JOB_DISPLAY_URL"}">${ENV, var="JOB_DISPLAY_URL"}</a></td>
                </tr>
                <tr>
                    <td>触发原因</td>
                    <td>${CAUSE}</td>
                </tr>
                <tr>
                    <td>最近提交</td>
                    <td>${CHANGES}</td>
                </tr>
                <tr>
                    <td>commit ID</td>
                    <td>${ENV, var="GIT_COMMIT"}</td>
                </tr>
                <tr>
                    <td>git 地址</td>
                    <td><a href="${ENV, var="GIT_URL"}">${ENV, var="GIT_URL"}</a></td>
                </tr>
                <tr>
                    <td>git 分支</td>
                    <td>${ENV, var="GIT_BRANCH"}</td>
                </tr>
                <tr>
                    <td>构建编号</td>
                    <td>第 <b>${BUILD_NUMBER}</b> 次构建</td>
                </tr>
                <tr>
                    <td>构建 Url</td>
                    <td><a href="${BUILD_URL}">${BUILD_URL}</a></td>
                </tr>
                <tr>
                    <td>构建 Url - BlueOcean</td>
                    <td><a href="${ENV, var="RUN_DISPLAY_URL"}">${ENV, var="RUN_DISPLAY_URL"}</a></td>
                </tr>
                <tr>
                    <td>构建状态</td>
                    <td>${BUILD_STATUS}</td>
                </tr>
                <tr>
                    <td>构建日志</td>
                    <td><a href="${BUILD_URL}console">${BUILD_URL}console</a></td>
                </tr>
                <tr>
                    <td>构建日志- BlueOcean</td>
                    <td><a href="${ENV, var="RUN_ARTIFACTS_DISPLAY_URL"}">${ENV, var="RUN_ARTIFACTS_DISPLAY_URL"}</a></td>
                </tr>
                <tr>
                    <td>工作目录</td>
                    <td>${ENV, var="WORKSPACE"}</td>
                </tr>
            </table>


            </br></br></br></br></br></br></br></br></br></br>
            <ul>
                <li>BUILD_NUMBER ： ${BUILD_NUMBER}</li>
                <li>BUILD_URL ： ${BUILD_URL}</li>
                <li>JENKINS_URL ： ${JENKINS_URL}</li>
                <li>GIT_BRANCH ： ${GIT_BRANCH}</li>
                <li>JOB_DESCRIPTION ： ${JOB_DESCRIPTION}</li>
                <li>CAUSE ： ${CAUSE}</li>
                <li>CHANGES ： ${CHANGES}</li>
                <li>PROJECT_NAME ： ${PROJECT_NAME}</li>
                <li>PROJECT_DISPLAY_NAME ： ${PROJECT_DISPLAY_NAME}</li>
                <li>PROJECT_URL ： ${PROJECT_URL}</li>
                <li>BUILD_STATUS ： ${BUILD_STATUS}</li>
                <li>CHANGES_SINCE_LAST_SUCCESS ： ${CHANGES_SINCE_LAST_SUCCESS}</li>
                <li>CHANGES_SINCE_LAST_UNSTABLE ： ${CHANGES_SINCE_LAST_UNSTABLE}</li>
                <li>FAILED_TESTS ： ${FAILED_TESTS}</li>
                <li>TEST_COUNTS ： ${TEST_COUNTS}</li>
                <!--<li>BUILD_LOG  ： ${BUILD_LOG }</li>-->

                <li>ENV.JOB_NAME ： ${ENV, var="JOB_NAME"}</li>
                <li>ENV.BUILD_ID ： ${ENV, var="BUILD_ID"}</li>
                <li>ENV.NODE_NAME ： ${ENV, var="NODE_NAME"}</li>
                <li>ENV.BUILD_TAG ： ${ENV, var="BUILD_TAG"}</li>
                <li>ENV.EXECUTOR_NUMBER ： ${ENV, var="EXECUTOR_NUMBER"}</li>
                <li>ENV.JAVA_HOME ： ${ENV, var="JAVA_HOME"}</li>
                <li>ENV.WORKSPACE ： ${ENV, var="WORKSPACE"}</li>
                <li>ENV.SVN_REVISION ： ${ENV, var="SVN_REVISION"}</li>
                <li>ENV.CVS_BRANCH ： ${ENV, var="CVS_BRANCH"}</li>
                <li>ENV.GIT_COMMIT ： ${ENV, var="GIT_COMMIT"}</li>
                <li>ENV.GIT_URL ： ${ENV, var="GIT_URL"}</li>
            </ul>

        </body>

        </html>
        ''',
         mimeType: 'text/html', subject: '${ENV, var="JOB_NAME"}-第${BUILD_NUMBER}次构建日志', to: 'leixing1012@163.com'
      }

      failure {
        // Only run the steps in post if the current Pipeline’s or stage’s run has a "failed" status, typically denoted by red in the web UI.
        echo "pipeline failure"
      }

      unsuccessful {
        // Only run the steps in post if the current Pipeline’s or stage’s run has not a "success" status. This is typically denoted in the web UI depending on the status previously mentioned (for stages this may fire if the build itself is unstable).
        echo "pipeline unsuccessful"
      }

      fixed {
        // Only run the steps in post if the current Pipeline’s run is successful and the previous run failed or was unstable.
        echo "pipeline fixed"
      }

      changed {
        // Only run the steps in post if the current Pipeline’s run has a different completion status from its previous run.
        echo "pipeline changed"
      }
    }
}
```







### 



Jenkins的工作流程

1 先定义一个流水线项目，指定项目的git位置

流水线启动

1 先去git位置自动拉取代码

2 解析拉取代码里面的Jenkinsfile文件

3 按照Jenkinsfile指定的流水线开始加工项目



Jenkins重要的点

0、jenkins的家目录 /var/jenkins_home 已经被我们docker外部挂载了 ； 

```shell
/var/lib/docker/volumes/jenkins-data/_data
```

1、WORKSPACE（工作空间）= /var/jenkins_home/workspace/java-devops-demo

每一个流水线项目，占用一个文件夹位置

BUILD_NUMBER=5；当前第几次构建

WORKSPACE_TMP（临时目录）=/var/jenkins_home/workspace/java-devops-demo@tmp

1 JOB_URL=http://139.198.9.163:8080/job/java-devops-demo/1



2、常用的环境如果没有，jenkins配置环境一大堆操作

3、jenkins_url ： http://139.198.27.103:8080



2、远程构建触发

期望效果： 远程的github代码提交了，jenkins流水线自动触发构建。

实现流程：

1、保证jenkins所在主机能被远程访问

2、jenkins中远程触发需要权限，我们应该使用用户进行授权

3、配置gitee/github，webhook进行触发

```shell
#远程构建即使配置了github 的webhook，默认会403.我们应该使用用户进行授权
1、创建一个用户
2、一定随便登陆激活一次
3、生成一个apitoken
http://leifengyang:113620edce6200b9c78ecadb26e9cf122e@139.198.186.134:8080/job/dev
ops-java-demo/build?token=leifengyang
```

![image-20230805142419111](assets/006_jenkins实战/image-20230805142419111.png)

远程触发： JENKINS_URL /job/simple-java-maven-app/build?token= TOKEN_NAME 请求即可  



6、流水线语法1、基础格式

```
pipeline {
agent any
environment {
CC = 'clang'
} s
tages {
stage('Example') {
steps {
sh 'printenv'
sh 'echo $CC'
}
}
}
```

2、环境变量  

3、密钥管理  

4、自定义agent  

![image-20230805142510161](assets/006_jenkins实战/image-20230805142510161.png)

```
//需要安装docker、docker pipeline插件
pipeline {
agent none
stages {
stage('Example Build') {
agent {
docker 'maven:3-alpine'
//args 是指定 docker run 的所有指令
args '-v /var/jenkins_home/maven/.m2:/root/.m2'
} s
teps {
echo 'Hello, Maven'
sh 'mvn --version'
}
} s
tage('Example Test') {
agent { docker 'openjdk:8-jre' }
steps {
echo 'Hello, JDK'
sh 'java -version'
}
}
}
}
```

5、参数化

6、条件表达式

7、修改jenkins插件源

```shell
 http://mirror.xmission.com/jenkins/updates/update-center.json
```

jenkins插件中心的插件;

![image-20230809184321004](assets/006_jenkins实战/image-20230809184321004.png)

![image-20230809184325260](assets/006_jenkins实战/image-20230809184325260.png)



8、推荐安装的插件



Docker Pipeline && Docker

安装Docker Pipeline会自动安装docker相关的

这个允许我们自定义agent使用docker环境



Git Parameter

解析git参数，允许我们选择分支进行构建



Active Choices

可以做到参数的级联选择



Generic Webhook Trigger

通用的webhook触发器，构建更强大的webhook功能



Role-based Authorization Strategy

RBAC权限指定，给一个用户精确指定权限



List Git Branches Parameter

列出分支参数



Build With Parameters

基于自定义参数构建



可以使用在线安装, 也可以选择离线下载后, 解压到 jenkins_home 中的 plugins 目录下



三、Jenkins On Docker

四、Jenkins On Kubernetes