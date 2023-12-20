# kubernetes ReplicaSet

RC、RS  

RC： ReplicasController：副本控制器

RS：ReplicasSet：副本集；Deployment【滚动更新特性】默认控制的是他

RC是老版，RS是新版（可以有复杂的选择器【表达式】）。  RS支持复杂选择器

```yaml
matchExpressions:
  - key: pod-name
    value: [aaaa,bbb]
    operator: DoesNotExist
```

| operator     | 说明                                                         |
| ------------ | ------------------------------------------------------------ |
| In           | value: [aaaa,bbb]必须存在，表示key指定的标签的值是这个集合内的 |
| NotIn        | value: [aaaa,bbb]必须存在，表示key指定的标签的值不是这个集合内的 |
| Exists       | 只要有key指定的标签即可，不用管值是多少                      |
| DoesNotExist | 只要Pod上没有key指定的标签，不用管值是多少                   |

虽然ReplicasSet强大，但是我们也不直接写RS；都是直接写Deployment的，Deployment会自动产生RS

Deployment每次的滚动更新都会产生新的RS



