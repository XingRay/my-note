# Docker镜像的导入导出



#### 1 镜像的导出

通过镜像ID保存

方式一：

```bash
docker save image_id > image-save.tar
```

例如：

```bash
docker images
```

```
REPOSITORY   TAG         IMAGE ID      CREATED       SIZE
openjdk      8-jre       26ac3f63d29f  2 months ago  273MB
nginx        1.21.3      87a94228f133  4 months ago  133MB
```



```bash
docker save 87a94228f133 > nginx-save.tar
```



方式二：

```bash
docker save -o image-save.tar image_id
```

例如：

```bash
docker images
```



```bash
REPOSITORY   TAG         IMAGE ID      CREATED       SIZE
openjdk      8-jre       26ac3f63d29f  2 months ago  273MB
nginx        1.21.3      87a94228f133  4 months ago  133MB
```

```bash
docker save -o nginx-save.tar 87a94228f133
```



通过镜像`repository`和`tag`保存

```bash
docker save -o image-save.tar repository:tag
```

例如：

```bash
docker images
```

```bash
REPOSITORY   TAG          IMAGE ID      CREATED       SIZE
openjdk      8-jre        26ac3f63d29f  2 months ago  273MB
nginx        1.21.3       87a94228f133  4 months ago  133MB
```

```bash
docker save -o nginx-save.tar nginx:1.21.3
```

**注意**: 在windows系统中文件名是不能有`:` 的,导出的文件名不能使用类似于 `nginx:1.21.3.tar` ,可以使用 `_` 代替 `:` ,如: `nginx_1.21.3.tar`



#### 1.1.2、镜像的导入

方式一：

```
docker load < nginx-save.tar
```

方式二：

```
docker load -i nginx-save.tar
```

注意

使用 `image_id`作为参数的方式导出的镜像包进行导入会出现 `none`的情况，需要手动打标签

```
docker tag 87a94228f133 nginx:1.21.3
```

使用镜像 `repository:tag` 作为导出参数的方式则正常