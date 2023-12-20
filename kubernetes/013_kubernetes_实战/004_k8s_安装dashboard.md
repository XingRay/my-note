## k8s-dashboard

dashboard官网：

https://github.com/kubernetes/dashboard



```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.7.0/aio/deploy/recommended.yaml
```

无法下载可以使用浏览器下载后上传

```bash
kubectl apply -f recommended.yaml
```

pod运行状态为running后，设置访问端口：

```bash
kubectl edit svc kubernetes-dashboard -n kubernetes-dashboard
```

将

```bash
type: ClusterIP
```

修改为：

```bash
type: NodePort
```



执行：

```bash
kubectl get svc -A |grep kubernetes-dashboard
```

输出：

```bash
kubernetes-dashboard   dashboard-metrics-scraper   ClusterIP   10.96.67.179    <none>        8000/TCP                 6m32s
kubernetes-dashboard   kubernetes-dashboard        NodePort    10.96.208.222   <none>        443:32077/TCP            6m32s
```

注意要开放 32077 端口 （30000-32767之前的某个端口）才能访问。使用集群的任意ip加上这个端口就可以访问dashboard了，注意使用https。如：

```bash
https://192.168.0.112:32077
```



登录dashboard需要令牌，下面生成令牌：

准备一个yaml文件：

```bash
vi dashboard.yaml
```

输入：

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: admin-user
  namespace: kubernetes-dashboard
---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: admin-user
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: cluster-admin
subjects:
- kind: ServiceAccount
  name: admin-user
  namespace: kubernetes-dashboard
```

保存文件，执行：

```bash
kubectl apply -f dashboard.yaml
```

提示账号已创建：

```bash
serviceaccount/admin-user created
clusterrolebinding.rbac.authorization.k8s.io/admin-user created
```

这个账号不是通过账号密码登录，而是需要令牌，执行指令获取访问令牌：

```bash
kubectl -n kubernetes-dashboard create token admin-user
```

输出：

```bash
eyJhbGciOiJSUzI1NiIsImtpZCI6Ilk5OFFuZ0c5QkNuYk9OZWkzeWl1YW9LeFZkTVA2YW9QaEM2QW8tckw5XzAifQ.eyJhdWQiOlsiaHR0cHM6Ly9rdWJlcm5ldGVzLmRlZmF1bHQuc3ZjLmNsdXN0ZXIubG9jYWwiXSwiZXhwIjoxNjg3MTU2MzQ3LCJpYXQiOjE2ODcxNTI3NDcsImlzcyI6Imh0dHBzOi8va3ViZXJuZXRlcy5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsIiwia3ViZXJuZXRlcy5pbyI6eyJuYW1lc3BhY2UiOiJrdWJlcm5ldGVzLWRhc2hib2FyZCIsInNlcnZpY2VhY2NvdW50Ijp7Im5hbWUiOiJhZG1pbi11c2VyIiwidWlkIjoiYzM0MTQ1YWItOTNlOC00NmNhLWIxODMtZTBiOWExMmY5YWMzIn19LCJuYmYiOjE2ODcxNTI3NDcsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlcm5ldGVzLWRhc2hib2FyZDphZG1pbi11c2VyIn0.g_9uaAEBzMu6DSeLw31wiXNf97ZFUCRiT2H16wVZhL_8cbINYRGqwgF8bkdXtMKmiTnKtjWw9W13T9Vi6S8FngVA8NSvLAZuy5lQV9tlFV_s-_1jiPT2_g_suLm66ePlx4ErBUcayqgdUMccNRRtjR0mssP5b8YhOuoiTjsORYSUDGCrc1Lmldx5Fk-VOAtQE8g0lzll6qgdzXg6GOuFEgOxSHLt2uD5-FWpeXV32Gv75WvKyL6yD-9JYMqdIUze66jh64-FBEZSHMQLcezPzj8wZg48lghJRZnjbgQ4XjriqDP7h4-33ZM3o53fxGIb6AxCBmO5wcQwlBUg6mgcLw
```

在 https://192.168.0.112:32077 登录页选择token，在输入框中输入上面产生的token即可登录进dashboard了。



