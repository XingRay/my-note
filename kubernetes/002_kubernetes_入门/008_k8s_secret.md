### Secret

专门用于保存敏感信息，如密码、oauth令牌、ssh密钥等。原理与configmap基本一致，只是secret保存的是敏感信息，configmap保存的是明文信息。

典型的应用场景：从私有的镜像仓库拉取镜像时需要登录，需要填写username和password。账号密码信息直接写在配置文件里不安全，容易泄漏，这时可以使用secret的方式把登录信息保存起来。

比如通过配置文件定义部署

```bash
vi private-nginx.yaml
```

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: private-nginx
spec:
  containers:
  - name: private-nginx
    image: leifengyang/guignginx:v1.0
```

这里镜像使用的是 leifengyang/guignginx:v1.0 这是一个私有的镜像，需要使用账号密码登录之后才能下载。因此直接部署执行：

```bash
kubectl apply -f private-nginx.yaml
```

是无法部署成功的，在下载镜像这一步会提示服务器 access denied

删除该pod

```bash
kubectl delete -f private-nginx.yaml
```



直接提供账号密码不安全，因此可以在整个k8s集群中预先设置好账号密码，使用指令创建secret，指令的开头是固定的

```
kubectl create secret docker-registry
```

完整的格式如下：

```bash
kubectl create secret docker-registry <secret-name> \
  --docker-server=<你的镜像仓库服务器> \
  --docker-username=<你的用户名> \
  --docker-password=<你的密码> \
  --docker-email=<你的邮箱地址>
```

如：

```bash
root@k8s-master01:~# kubectl create secret docker-registry leifengyang-docker \
--docker-username=leifengyang \
--docker-password=Lfy123456 \
--docker-email=534096094@qq.com

secret/leifengyang-docker created
```

以yaml的格式查看secret：

```bash
root@k8s-master01:~# kubectl get secret leifengyang-docker -oyaml
```

输出：

```yaml
apiVersion: v1
data:
  .dockerconfigjson: eyJhdXRocyI6eyJodHRwczovL2luZGV4LmRvY2tlci5pby92MS8iOnsidXNlcm5hbWUiOiJsZWlmZW5neWFuZyIsInBhc3N3b3JkIjoiTGZ5MTIzNDU2IiwiZW1haWwiOiI1MzQwOTYwOTRAcXEuY29tIiwiYXV0aCI6ImJHVnBabVZ1WjNsaGJtYzZUR1o1TVRJek5EVTIifX19
kind: Secret
metadata:
  creationTimestamp: "2023-06-20T09:49:48Z"
  name: leifengyang-docker
  namespace: default
  resourceVersion: "108688"
  uid: 49116854-0849-4218-a96d-e7eafdc399d6
type: kubernetes.io/dockerconfigjson
```

可以看到secret的内容

```yaml
data:
  .dockerconfigjson: eyJhdXRocyI6eyJodHRwczovL2luZGV4LmRvY2tlci5pby92MS8iOnsidXNlcm5hbWUiOiJsZWlmZW5neWFuZyIsInBhc3N3b3JkIjoiTGZ5MTIzNDU2IiwiZW1haWwiOiI1MzQwOTYwOTRAcXEuY29tIiwiYXV0aCI6ImJHVnBabVZ1WjNsaGJtYzZUR1o1TVRJek5EVTIifX19
```

configmap的data部分是明文存储的，而secret的数据部分是加密存储（可以用base64解码）的。

创建好secret后，这时再创建pod的部署文件：

```
vi private-nginx-secret.yaml
```

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: private-nginx-secret
spec:
  containers:
  - name: private-nginx-secret
    image: leifengyang/guignginx:v1.0
  imagePullSecrets:
  - name: leifengyang-docker
```

注意这里添加了参数

```yaml
imagePullSecrets:
  - name: leifengyang-docker
```

引用了名为 leifengyang-docker 的secret，这样就可以正常从私有仓库下载镜像了。