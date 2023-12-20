# 在docker上安装

https://www.jenkins.io/doc/book/installing/docker/



## 1 镜像选择

jenkinsci/jenkins  https://hub.docker.com/r/jenkinsci/jenkins/  是没有 blueocean 插件的, 

官方推荐使用安装好了 blueocean 插件的镜像 jenkinsci/blueocean 

blueocean 插件说明:

https://www.jenkins.io/doc/book/blueocean/



## 2 安装指令

```shell
docker run \
--name jenkins \
# 使用 root 账户运行jenkins
-u root \
-d \
-p 8080:8080 \
-p 50000:50000 \

# /var/jenkins_home jenkins的家目录包含了jenkins的所有配置。以后要注意备份 /var/jenkins_home （以文件的方式固化的）
-v jenkins-data:/var/jenkins_home \

#自己构建镜像 RUN的时候就把时区设置好
#如果是别人的镜像，比如从 docker hub 下载的镜像，有的默认的时区是UTC, 因此需要调整时区
# 在容器运行时通过 -v /etc/localtime:/etc/localtime:ro 参数调整时区
-v /etc/localtime:/etc/localtime:ro \

# /var/run/docker.sock 表示Docker守护程序通过其监听的基于Unix的套接字。 
# 该映射允许jenkinsci/blueocean 容器与Docker守护进程通信， 
# 如果 jenkinsci/blueocean 容器需要实例化其他Docker容器，则该守护进程是必需的。 
# 如果运行声明式管道，其语法包含agent部分用 docker；例如， agent { docker { ... } } 此选项是必需的。
-v /var/run/docker.sock:/var/run/docker.sock \
# docker启动时自动启动这个容器
--restart=always \

# jenkinsci/jenkins 是没有 blueocean插件的，得自己装
# jenkinsci/blueocean：带了blueocean插件
# 如果你的jenkins 安装插件装不上。使用这个镜像【 registry.cnqingdao.aliyuncs.com/lfy/jenkins:plugins-blueocean 】
# 默认访问账号/密码是【admin/admin】
jenkinsci/blueocean
```

去注释版:

```shell
docker run \
--name jenkins \
-u root \
-d \
-p 8080:8080 \
-p 50000:50000 \
-v jenkins-data:/var/jenkins_home \
-v /etc/localtime:/etc/localtime:ro \
-v /var/run/docker.sock:/var/run/docker.sock \
--restart=always \
jenkinsci/blueocean
```



## 3 官方文档

1 Open up a terminal window.

2 Create a [bridge network](https://docs.docker.com/network/bridge/) in Docker using the following [`docker network create`](https://docs.docker.com/engine/reference/commandline/network_create/) command:

```
docker network create jenkins
```



3 In order to execute Docker commands inside Jenkins nodes, download and run the `docker:dind` Docker image using the following [`docker run`](https://docs.docker.com/engine/reference/run/) command:

```shell
docker run \
  
  # ( Optional ) Specifies the Docker container name to use for running the image. By default, Docker generates a unique name for the container.
  --name jenkins-docker \
  
  # ( Optional ) Automatically removes the Docker container (the instance of the Docker image) when it is shut down.
  --rm \
  
  # ( Optional ) Runs the Docker container in the background. You can stop this instance by running docker stop jenkins-docker.
  --detach \
  
  # Running Docker in Docker currently requires privileged access to function properly. This requirement may be relaxed with newer Linux kernel versions.
  --privileged \
  
  # This corresponds with the network created in the earlier step.
  --network jenkins \
  
  # Makes the Docker in Docker container available as the hostname docker within the jenkins network.
  --network-alias docker \
  
  # Enables the use of TLS in the Docker server. Due to the use of a privileged container, this is recommended, though it requires the use of the shared volume described below. This environment variable controls the root directory where Docker TLS certificates are managed.
  --env DOCKER_TLS_CERTDIR=/certs \
  
  # Maps the /certs/client directory inside the container to a Docker volume named jenkins-docker-certs as created above.
  --volume jenkins-docker-certs:/certs/client \
  
  # Maps the /var/jenkins_home directory inside the container to the Docker volume named jenkins-data. This allows for other Docker containers controlled by this Docker container’s Docker daemon to mount data from Jenkins.
  --volume jenkins-data:/var/jenkins_home \
  
  #   ( Optional ) Exposes the Docker daemon port on the host machine. This is useful for executing docker commands on the host machine to control this inner Docker daemon.	
  --publish 2376:2376 \
  
  # The docker:dind image itself. Download this image before running, by using the command: docker image pull docker:dind.
  docker:dind \
  
  # The storage driver for the Docker volume. Refer to the Docker storage drivers documentation for supported options.
  --storage-driver overlay2
```

去掉注释的版本:

```shell
docker run --name jenkins-docker --rm --detach \
  --privileged --network jenkins --network-alias docker \
  --env DOCKER_TLS_CERTDIR=/certs \
  --volume jenkins-docker-certs:/certs/client \
  --volume jenkins-data:/var/jenkins_home \
  --publish 2376:2376 \
  docker:dind --storage-driver overlay2
```



Customize the official Jenkins Docker image, by executing the following two steps:

1 Create a Dockerfile with the following content:

```
FROM jenkins/jenkins:2.401.3-jdk17
USER root
RUN apt-get update && apt-get install -y lsb-release
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc \
  https://download.docker.com/linux/debian/gpg
RUN echo "deb [arch=$(dpkg --print-architecture) \
  signed-by=/usr/share/keyrings/docker-archive-keyring.asc] \
  https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
RUN apt-get update && apt-get install -y docker-ce-cli
USER jenkins
RUN jenkins-plugin-cli --plugins "blueocean docker-workflow"
```



2 Build a new docker image from this Dockerfile, and assign the image a meaningful name, such as "myjenkins-blueocean:2.401.3-1":

```
docker build -t myjenkins-blueocean:2.401.3-1 .
```



If you have not yet downloaded the official Jenkins Docker image, the above process automatically downloads it for you.

3 Run your own `myjenkins-blueocean:2.401.3-1` image as a container in Docker using the following [`docker run`](https://docs.docker.com/engine/reference/run/) command:

```shell
docker run \

  # ( Optional ) Specifies the Docker container name for this instance of the Docker image.
  --name jenkins-blueocean \
  
  # Always restart the container if it stops. If it is manually stopped, it is restarted only when Docker daemon restarts or the container itself is manually restarted.
  --restart=on-failure \
  
  #   ( Optional ) Runs the current container in the background, known as "detached" mode, and outputs the container ID. If you do not specify this option, then the running Docker log for this container is displayed in the terminal window.	
  --detach \
  
  # Connects this container to the jenkins network previously defined. The Docker daemon is now available to this Jenkins container through the hostname docker.
  --network jenkins \
  
  # Specifies the environment variables used by docker, docker-compose, and other Docker tools to connect to the Docker daemon from the previous step.
  --env DOCKER_HOST=tcp://docker:2376 \
  --env DOCKER_CERT_PATH=/certs/client \
  --env DOCKER_TLS_VERIFY=1 \
  
  # Maps, or publishes, port 8080 of the current container to port 8080 on the host machine. The first number represents the port on the host, while the last represents the container’s port. For example, to access Jenkins on your host machine through port 49000, enter -p 49000:8080 for this option.
  --publish 8080:8080 \
  
  # ( Optional ) Maps port 50000 of the current container to port 50000 on the host machine. This is only necessary if you have set up one or more inbound Jenkins agents on other machines, which in turn interact with your jenkins-blueocean container, known as the Jenkins "controller". Inbound Jenkins agents communicate with the Jenkins controller through TCP port 50000 by default. You can change this port number on your Jenkins controller through the Security page. For example, if you update the TCP port for inbound Jenkins agents of your Jenkins controller to 51000, you need to re-run Jenkins via the docker run … command. Specify the "publish" option as follows: the first value is the port number on the machine hosting the Jenkins controller, and the last value matches the changed value on the Jenkins controller, for example,--publish 52000:51000. Inbound Jenkins agents communicate with the Jenkins controller on that port (52000 in this example). Note that WebSocket agents do not need this configuration.
  --publish 50000:50000 \
  
  # Maps the /var/jenkins_home directory in the container to the Docker volume with the name jenkins-data. Instead of mapping the /var/jenkins_home directory to a Docker volume, you can also map this directory to one on your machine’s local file system. For example, specify the option --volume $HOME/jenkins:/var/jenkins_home to map the container’s /var/jenkins_home directory to the jenkins subdirectory within the $HOME directory on your local machine — typically /Users/<your-username>/jenkins or /home/<your-username>/jenkins. NOTE: If you change the source volume or directory for this, the volume from the docker:dind container above needs to be updated to match this.
  --volume jenkins-data:/var/jenkins_home \
  
  # Maps the /certs/client directory to the previously created jenkins-docker-certs volume. The client TLS certificates required to connect to the Docker daemon are now available in the path specified by the DOCKER_CERT_PATH environment variable.
  --volume jenkins-docker-certs:/certs/client:ro \
  
  # The name of the Docker image, which you built in the previous step.
  myjenkins-blueocean:2.401.3-1 
```

去注释版:

```shell
docker run --name jenkins-blueocean --restart=on-failure --detach \
  --network jenkins --env DOCKER_HOST=tcp://docker:2376 \
  --env DOCKER_CERT_PATH=/certs/client --env DOCKER_TLS_VERIFY=1 \
  --publish 8080:8080 --publish 50000:50000 \
  --volume jenkins-data:/var/jenkins_home \
  --volume jenkins-docker-certs:/certs/client:ro \
  myjenkins-blueocean:2.401.3-1
```

 

5 Proceed to the [Post-installation setup wizard](https://www.jenkins.io/doc/book/installing/docker/#setup-wizard).



