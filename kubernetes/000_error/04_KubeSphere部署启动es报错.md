## KubeSphere部署启动es报错

在KubeSphere中，按照 `myNote\software-project\deploy\kubernetes\16_KubeSphere_应用部署_elasticsearch.md`  教程部署es，其中 `elasticsearch.yml`配置文件的内容如下：

```properties
cluster.name: "project-test-es-cluster"
network.host: 0.0.0.0
```

其他的设置和步骤按照部署文档操作，部署完成后启动pod会报错，查看pod日志：

```
Exception in thread "main" java.nio.file.FileSystemException: /usr/share/elasticsearch/config/elasticsearch.yml.cvrM1wfsRz-M7StPG7vFHw.tmp -> /usr/share/elasticsearch/config/elasticsearch.yml: Device **or** resource busy
```

问题解决方法：

在 `elasticsearch.yml` 显式声明

```properties
xpack.security.enabled: false
```

或者

```properties
xpack.security.enabled: true
```





根据github的issue讨论 https://github.com/elastic/elasticsearch/issues/85463 ，该问题的原因是由 AutoConfigureNode CLI 产生的，而不是es。启动流程

```bash
Container => /usr/local/bin/docker-entrypoint.sh => /usr/share/elasticsearch/bin/elasticsearch
```



Clarification: From the stack trace, AutoConfigureNode CLI is experiencing the error, not Elasticsearch.

Startup: Container => `/usr/local/bin/docker-entrypoint.sh` => `/usr/share/elasticsearch/bin/elasticsearch`

Looking at `/usr/share/elasticsearch/bin/elasticsearch`, it seems like the variable ATTEMPT_SECURITY_AUTO_CONFIG=true triggers a call to AutoConfigureNode CLI before Elasticsearch. The stack trace is for AutoConfigureNode CLI, not Elasticsearch.

Excerpt of the AutoConfigure CLI command:

```bash
ES_MAIN_CLASS=org.elasticsearch.xpack.security.cli.AutoConfigureNode \
ES_ADDITIONAL_SOURCES="x-pack-env;x-pack-security-env" \
ES_ADDITIONAL_CLASSPATH_DIRECTORIES=lib/tools/security-cli \
bin/elasticsearch-cli "${ARG_LIST[@]}" <<<"$KEYSTORE_PASSWORD"
```

Excerpt of the Elasticsearch daemon command:

```bash
    "$JAVA" \
    "$XSHARE" \
    $ES_JAVA_OPTS \
    -Des.path.home="$ES_HOME" \
    -Des.path.conf="$ES_PATH_CONF" \
    -Des.distribution.flavor="$ES_DISTRIBUTION_FLAVOR" \
    -Des.distribution.type="$ES_DISTRIBUTION_TYPE" \
    -Des.bundled_jdk="$ES_BUNDLED_JDK" \
    -cp "$ES_CLASSPATH" \
    org.elasticsearch.bootstrap.Elasticsearch \
    "${ARG_LIST[@]}" \
    <<<"$KEYSTORE_PASSWORD" &
```



Reproduce original issue by executing

```
> docker run --name elastic1 -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -v C:\Docker\elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml --rm -it docker.elastic.co/elasticsearch/elasticsearch:8.0.0 
Exception in thread "main" java.nio.file.FileSystemException: /usr/share/elasticsearch/config/elasticsearch.yml.Occjcc_mS06vpoRLwlpUwA.tmp -> /usr/share/elasticsearch/config/elasticsearch.yml: Device or resource busy
        at java.base/sun.nio.fs.UnixException.translateToIOException(UnixException.java:100)
        at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:106)
        at java.base/sun.nio.fs.UnixCopyFile.move(UnixCopyFile.java:416)
        at java.base/sun.nio.fs.UnixFileSystemProvider.move(UnixFileSystemProvider.java:267)
        at java.base/java.nio.file.Files.move(Files.java:1432)
        at org.elasticsearch.xpack.security.cli.AutoConfigureNode.fullyWriteFile(AutoConfigureNode.java:1136)
        at org.elasticsearch.xpack.security.cli.AutoConfigureNode.fullyWriteFile(AutoConfigureNode.java:1148)
        at org.elasticsearch.xpack.security.cli.AutoConfigureNode.execute(AutoConfigureNode.java:687)
        at org.elasticsearch.cli.EnvironmentAwareCommand.execute(EnvironmentAwareCommand.java:77)
        at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:112)
        at org.elasticsearch.cli.Command.main(Command.java:77)
        at org.elasticsearch.xpack.security.cli.AutoConfigureNode.main(AutoConfigureNode.java:157)
```

Extract interesting files from container (Prerequisite: All `C:\Docker` to file sharing accept list)

```
> docker run --name elastic1 -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -v "C:\Docker":/mnt/local --rm -it docker.elastic.co/elasticsearch/elasticsearch:8.0.0 bash
elasticsearch@9d37e1eb7777:~$ cp /usr/share/elasticsearch/config/elasticsearch.yml /mnt/local/elasticsearch.yml
elasticsearch@9d37e1eb7777:~$ cp /usr/share/elasticsearch/config/elasticsearch.yml /mnt/local/elasticsearch2.yml
elasticsearch@9d37e1eb7777:~$ cp /usr/local/bin/docker-entrypoint.sh               /mnt/local/docker-entrypoint.sh
elasticsearch@9d37e1eb7777:~$ cp /usr/share/elasticsearch/bin/elasticsearch        /mnt/local/elasticsearch
```

Start in bash as root user, switch to elasticsearch, manually run `docker-entrypoint.sh` to reproduce the original error

```bash
> docker run -u root --name elastic1 -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -v C:\Docker\elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml -v C:\Docker\elasticsearch2.yml:/usr/share/elasticsearch/config/elasticsearch2.yml --rm -it docker.elastic.co/elasticsearch/elasticsearch:8.0.0 bash

root@62b736fca663:/usr/share/elasticsearch# ls -l /usr/share/elasticsearch/config/elasticsearch*.yml
-rw-rw-r-- 1 root root 1042 Feb  3 16:47 /usr/share/elasticsearch/config/elasticsearch-plugins.example.yml
-rwxr-xr-x 1 root root   53 Mar 29 19:01 /usr/share/elasticsearch/config/elasticsearch.yml
-rwxr-xr-x 1 root root   53 Mar 29 19:01 /usr/share/elasticsearch/config/elasticsearch2.yml

root@62b736fca663:/usr/share/elasticsearch# df -a | grep elasticsearch
grpcfuse       998896636 190624520 808272116  20% /usr/share/elasticsearch/config/elasticsearch.yml
grpcfuse       998896636 190624520 808272116  20% /usr/share/elasticsearch/config/elasticsearch2.yml

root@62b736fca663:/usr/share/elasticsearch# su - elasticsearch

elasticsearch@62b736fca663:~$ /usr/local/bin/docker-entrypoint.sh
Exception in thread "main" java.nio.file.FileSystemException: /usr/share/elasticsearch/config/elasticsearch.yml.JrtBhUSPQ4eNKgiJ3atKQQ.tmp -> /usr/share/elasticsearch/config/elasticsearch.yml: Device or resource busy
        at java.base/sun.nio.fs.UnixException.translateToIOException(UnixException.java:100)
        at java.base/sun.nio.fs.UnixException.rethrowAsIOException(UnixException.java:106)
        at java.base/sun.nio.fs.UnixCopyFile.move(UnixCopyFile.java:416)
        at java.base/sun.nio.fs.UnixFileSystemProvider.move(UnixFileSystemProvider.java:267)
        at java.base/java.nio.file.Files.move(Files.java:1432)
        at org.elasticsearch.xpack.security.cli.AutoConfigureNode.fullyWriteFile(AutoConfigureNode.java:1136)
        at org.elasticsearch.xpack.security.cli.AutoConfigureNode.fullyWriteFile(AutoConfigureNode.java:1148)
        at org.elasticsearch.xpack.security.cli.AutoConfigureNode.execute(AutoConfigureNode.java:687)
        at org.elasticsearch.cli.EnvironmentAwareCommand.execute(EnvironmentAwareCommand.java:77)
        at org.elasticsearch.cli.Command.mainWithoutErrorHandling(Command.java:112)
        at org.elasticsearch.cli.Command.main(Command.java:77)
        at org.elasticsearch.xpack.security.cli.AutoConfigureNode.main(AutoConfigureNode.java:157)

elasticsearch@62b736fca663:~$ ls -l /usr/share/elasticsearch/config/elasticsearch*.yml
-rw-rw-r-- 1 root          root          1042 Feb  3 16:47 /usr/share/elasticsearch/config/elasticsearch-plugins.example.yml
-rwxr-xr-x 1 elasticsearch elasticsearch   53 Mar 29 19:01 /usr/share/elasticsearch/config/elasticsearch.yml
-rwxr-xr-x 1 root          root            53 Mar 29 19:01 /usr/share/elasticsearch/config/elasticsearch2.yml
```



Check elasticsearch.yml ownership and permissions before and after manually running docker-entrypoint.sh.

```
>docker run -u root --name elastic1 -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" --rm -it docker.elastic.co/elasticsearch/elasticsearch:8.0.0 bash

root@40b71bc4c3ae:/usr/share/elasticsearch# ls -l /usr/share/elasticsearch/config/elasticsearch.yml
-rw-rw-r-- 1 root root 53 Feb  3 22:53 /usr/share/elasticsearch/config/elasticsearch.yml

root@40b71bc4c3ae:/usr/share/elasticsearch# su - elasticsearch

elasticsearch@40b71bc4c3ae:~$ /usr/local/bin/docker-entrypoint.sh > /dev/null 2> /dev/null &
[1] 18

elasticsearch@40b71bc4c3ae:~$ ls -l /usr/share/elasticsearch/config/elasticsearch.yml
-rw-rw-r-- 1 elasticsearch elasticsearch 1106 Mar 29 20:47 /usr/share/elasticsearch/config/elasticsearch.yml
```



If the operator does not mount elasticsearch.yml, I assume they want elasticsearch.yml autoconfiguration.
If the operator mounts elasticsearch.yml, I assume they don't want elasticsearch.yml autoconfiguration.

From looking at the startup scripts, I don't see an option to skip autoconfiguration. The only way seems to be if ENROLLMENT_TOKEN is set.

- `/usr/local/bin/docker-entrypoint.sh` looks for it and calls `/usr/share/elasticsearch/bin/elasticsearch --enrollment-token $ENROLLMENT_TOKEN` .
- `/usr/share/elasticsearch/bin/elasticsearch` only skips autoconfiguration (i.e. ATTEMPT_SECURITY_AUTO_CONFIG=false) if one of these parameters are present: --enrollment-token, --help, -h, --version, or -v



根据上面的讨论，得出原因是由于默认 AutoConfigure CLI  会在es启动之前运行，而由于 elasticsearch.yml 文件拥有者和运行的账号不一致导致权限问题，绕过这个问题的办法是不触发 AutoConfigure CLI ，



```
I found that if I explicitly set `xpack.security.enabled: true` and bind mount a keystore that has a `bootstrap.password` set, then bind mounting the elasticsearch.yml works fine. I haven't dug into the details of why or if that is correct behavior, but that is what I have observed.

Here is very simple single node cluster with a bind mounted elasticsearch.yml and keystore : https://github.com/jakelandis/es-docker-simple
```



This is expected because [enabling security explicitly makes the startup process skip security auto-configuration](https://github.com/elastic/elasticsearch/blob/06226397fb95c1e72693008ebf929a7066e22c64/x-pack/plugin/security/cli/src/main/java/org/elasticsearch/xpack/security/cli/AutoConfigureNode.java#L215-L217). The original error was thrown during security auto-configuration. Since it is skipped, the error no longer happens. But I believe the intention for this issue is whether we could either (1) detect the original bind mount situation and automatically skip auto configuration (IIUC, this is our preference) or (2) have auto configuration work if the the bind mount meets certain requirements.



then the config

```
cluster.name: docker-cluster
network.host: 0.0.0.0
xpack.security.enabled: false
```

works

but the config

```
cluster.name: docker-cluster
network.host: 0.0.0.0
```

got this error.







从上面的讨论得出，只要显式地设置  `xpack.security.enabled` 配置的值就可以跳过 auto configuration ，配置为 true 和 false 都可以



https://github.com/elastic/elasticsearch/issues/85463

https://discuss.elastic.co/t/when-mounting-elasticsearch-yml-docker-displays-device-or-resource-busy/300981