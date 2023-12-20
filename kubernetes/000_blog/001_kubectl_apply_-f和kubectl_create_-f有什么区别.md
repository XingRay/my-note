## kubectl apply -f和kubectl create -f有什么区别



kubectl create属于Imperative command（祈使式命令），它明确告诉kubectl要创建某个资源或对象；

kubectl apply是Declarative command（声明式命令），apply并不告诉kubectl具体做什么，而是由kubectl根据后面-f中的yaml文件与k8s中对应的object对比，自动探测要进行哪些操作，比如如果object不存在，则create；如果已经存在，则对比差异，update and replace

 

https://coding.imooc.com/learn/questiondetail/82743.html





一、kubectl create -f
用于创建 Kubernetes 对象。如果对应的资源已经存在，则会返回错误，此时需要先删除原有的资源对象，然后再执行创建操作。如果资源对象不存在，则会自动创建对应的资源对象
二、kubectl apply -f
用于创建或更新一个 Kubernetes 对象。如果该资源对象已经存在，则会首先尝试更新对应的字段值和配置，如果不存在则会自动创建资源对象。同时 kubectl apply 还提供了许多可选的参数，例如 --force、--validate、--record 等，可以使更新操作更加精确和可控
三、综上所述
kubectl create -f 适用于初始化资源对象的场景；而kubectl apply -f 则更加适合更新和修改已有的资源对象，因为它会对比新的 YAML 配置文件和已有的资源对象配置，只更新需要更新的部分，而不会覆盖已有的全部配置



