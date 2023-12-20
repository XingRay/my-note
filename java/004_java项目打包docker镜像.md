# Java项目打包dock镜像

1 maven 打包

```
mvn clean package -Dmaven.test.skip=true
```



2 本地运行

```bash
java -Xmx1024m -Xms128m -Dfile.encoding=utf8 -jar ./app.jar --spring.cloud.config.uri=http://192.168.0.108:8888 --spring.cloud.config.profile=dev --spring.cloud.config.label=master
```



3 dockerfile

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

