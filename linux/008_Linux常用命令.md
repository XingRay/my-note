# Linux常用命令

xargs :

```bash
kubectl get pod --all-namespaces | grep Error | awk '{print $1,$2}' | xargs -n 2 sh -c 'kubectl delete pod $1 -n $0 --grace-period=0 --force'
```

```bash
kubectl get pod --all-namespaces | grep Error | awk '{print $1,$2}' | xargs -n 2 sh -c 'kubectl delete pod $1 -n $0'
```



获取Linux指令结果的指定列、指定行

一、通过awk返回指定列
以ps -ef 的返回结果为例：

1、返回第一列

```shell
ps -ef|awk '{print $1}'
```

2、返回第一列和第x列

```shell
ps -ef|awk -v n=2 '{print $1,$(n+1)}'
```

3、返回带abc关键字的第一列(记不住就用grep去筛)

```shell
ps -ef|awk '/abc/{print $1}'
```

4、返回最后一列

```shell
ps -ef|awk '{print $NF}'
```

5、返回有多少列(以最后一行的列数为准)

```shell
ps -ef |awk 'END{print NF}'
```

6、返回有多少行

```shell
ps -ef |awk 'END{print NR}'
```

//当然大可不必这样，wc -l 就好

对返回的列，可以再加管道后跟 

```shell
head -n 6
```

 或者 

```shell
tail -n 6
```

 继续截取



二、通过sed获取指定行
1、返回第一行

```shell
ps -ef|sed -n 2p
```

//注意别1p，1p是个表头

2、返回第一行到第十行（带表头）

```shell
ps -ef|sed -n 1,10p
```

3、awk+sed返回某行某列

```shell
ps -ef|sed -n 2p|awk '{print $2}'
```
