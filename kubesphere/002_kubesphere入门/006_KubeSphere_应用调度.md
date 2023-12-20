# KubeSphere应用调度



目的:

通过定向调度策略将应用部署到指定的宿主机上



场景:

在实际环境中,不同的服务会资源由不同的要求,根据实际需求,我们需要将这些应用部署到特定的服务器,从而提升服务器性能和服务器资源的利用率



应用调度类型与场景

nodeSelector 定向调度

nodeSelector 是节点选择约束的最简单推荐形式, nodeSelector是PodSpec的一个字段. 通过设置Label相关策略将pod关联到对应label的节点上



nodeName 定向调度

nodeName 是节点选择约束的最简单方法,但是由于其自身限制,通常不使用,nodeName是PodSpec的一个字段, 如果指定nodeName,调度器将优先在指定的node上运行pod



nodeAddinity定向调度

nodeAffinity类似于nodeSelector, 使用nodeAffinity 可以根据节点的labels限制pod能够调度到哪些节点, 当前nodeAffinity有软亲和性和硬亲和性两种类型,节点亲和性与nodeSelector相比, 亲和 / 反亲和 功能极大地扩展了可以表达约束的类型,增强了如下的能力:

1 语言更具有表现力

2 在调度器无法满足要求时, 仍然调度该pod





操作概览

1 登录KubeSphere

2 给Worker节点添加Label

3 使用nodeSelector调度应用

4 使用nodeName调度应用

5 使用nodeAffinity调度应用

6 无法调度异常排查











### 1 登录KubeSphere

在web页面登录



### 2 给Worker节点添加Label

平台 - 集群管理

节点 - 集群节点 点击 k8s-worker01 节点 , 在节点详情页,点击更多,点击 `编辑标签`, 点击 `添加` 

key: env

value: test

点击 `保存`



节点 - 集群节点 点击 k8s-worker02 节点 , 在节点详情页,点击更多,点击 `编辑标签`, 点击 `添加` 

key: env

value: dev

点击 `保存`



### 3 使用nodeSelector调度应用

进入 工作空间 - 工作空间 - `测试项目的工作空间` - projects - `测试项目`-应用工作负载 - 工作负载 - 部署

点击 `创建` 

name: nodeselect-assign-pod

点击下一步

镜像使用nginx镜像作为测试,作为测试不需要访问pod, 删除端口设置, 点击`下一步`

在`高级设置`中, 勾选 设置节点调度策略(Set Node Scheduling Policy)

输入

key: env

value: test

这里 key-value 需要与前面给节点设置的Label中的key-value一致

点击 添加节点选择器(Add Node Selector)

设置完成后点击创建, 进入pod的详情页,可以看到该节点已经调度到 k8s-worker01 节点上了



如果需要该pod调度到另外的节点,可以修改pdo调度配置

在部署详情页,点击 `更多`,点击 `编辑YAML`

找到

```yaml
nodeSelector:
	env: test
```

修改为

```bash
nodeSelector:
	env: dev
```

由于 k8s-worker01 的 Label一致就改为与 k8s-worker02 的Label一致,点击`更新`,此时会重新创建一个 pod ,并且被调度到 k8s-worker02 节点



### 4 使用nodeName调度应用

创建一个部署, 与使用 nginx,删除端口设置,在高级设置中,使用默认设置即可, 创建完成后进入这个部署的详情页, 点击更多 - 编辑YAML

找到

```yaml
spec:
  template:
    spec:
      containers:
        - name: xxx
          ...
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      ...
```

在`spec.template.spec`下添加一条:  nodeName: k8s-worker01

```yaml
spec:
  template:
    spec:
      containers:
        - name: xxx
          ...
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      nodeName: k8s-worker01
      ...
```

点击更新,更新后可以看到重新创建了pod,并且自动调度到 k8s-worker01 节点上了



### 5 使用nodeAffinity调度应用

#### 5.1 硬亲和性



#### 5.2 软亲和性

设置软亲和性后,即使不符合调度条件,也可以调度



创建一个部署, 与使用 nginx,删除端口设置,添加资源限制,模式部署条件 预留和限制均修改为 1Core 1024Mi ,高级设置保持默认即可

创建完成后进入部署详情页, 修改YAML

找到

```yaml
spec:
  template:
    spec:
      containers:
        - name: xxx
          ...
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      nodeName: k8s-worker01
      ...
```

添加 affinity 配置, 修改后如下:

```yaml
spec:
  template:
    spec:
      containers:
        - name: xxx
          ...
      restartPolicy: Always
      terminationGracePeriodSeconds: 30
      ...
      affinity:
        nodeAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              preference:
                matchExpressions:
                  - key: env
                    operator: In
                    values:
                      - dev
```

软亲和性通过设置权重,使pod更倾向于运行在匹配的节点上,当没有节点满足亲和性要求时,pod将会部署在其他的节点上.

点击`更新`

可以看到pod已经调度到 k8s-worker02 (env=dev) 节点上了

增加部署的副本数,可以看到 使用软亲和性的pod可以调度到其他不符合要求的节点, 而使用硬亲和性的pod不会被调度到不满足要求的节点上



### 6 无法调度异常排查





