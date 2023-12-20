```dockerfile
FROM openjdk:8-jdk
LABEL maintainer=leixing


#启动自行加载   服务名-prod.yml配置
ENV PARAMS="--server.port=8080 --spring.profiles.active=prod --spring.cloud.nacos.discovery.server-addr=nacos-standalone.project-test:8848 --spring.cloud.nacos.discovery.namespace=75f51239-c1f1-4172-976e-e846464448cb --spring.cloud.nacos.config.server-addr=nacos-standalone.project-test:8848 --spring.cloud.nacos.config.namespace=75f51239-c1f1-4172-976e-e846464448cb --spring.cloud.nacos.config.file-extension=yml"
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

COPY target/*.jar /app.jar
EXPOSE 8080

#
ENTRYPOINT ["/bin/sh","-c","java -Xmx1024m -Xms128m -Dfile.encoding=utf8 -Duser.timezone=Asia/Shanghai -Djava.security.egd=file:/dev/./urandom -jar /app.jar ${PARAMS}"]
```





```dockerfile
FROM maven:3.5-jdk-8 AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package


FROM gcr.io/distroless/java:8
ARG DEPENDENCY=/usr/src/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","DemoApplication.Application"]
```





执行

```bash
docker build -t app:1.0.0 -f Dockerfile .
```

在项目根目录

```bash
docker build -t member:1.0.0 -f service-member/Dockerfile service-member
```

```bash
docker run -d --name member member:1.0.0 
```



