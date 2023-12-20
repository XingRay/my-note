## webclient总结对比

### 1 方案

1. okhttp
2. okhttp+retrofit
3. spring WebClient
4. openfeign+okhttp
5. springcloud+openfeign+okhttp



### 2. 场景

场景1.  纯java客户端

场景2. android客户端

场景3. 微服务客户端



### 3. 需求

1. 声明式接口
2. 支持响应式和sse
3. ssl配置
4. 代理配置
5. 负载均衡

6. 请求返回日志
7. JsonToBean
8. 自定义json转换器
9. \* 底层支持netty
10. \* 支持pb



|                  | okhttp | okhttp+retrofit | spring WebClient | openfeign+okhttp | springcloud+openfeign+okhttp |
| ---------------- | ------ | --------------- | ---------------- | ---------------- | ---------------------------- |
| 声明式接口       | n      | y               | y                | y                | y                            |
| 响应式           | n      | y               | y                | n                | n                            |
| sse              | y      | n               | y                | n                | n                            |
| ssl配置          | y      | y               | y                | ?                | ?                            |
| 代理配置         | y      | y               | y                | ?                | ?                            |
| 负载均衡         | n      | n               | ?                | y                | y                            |
| 请求返回日志     | y      | y               | ?                | y                | y                            |
| JsonToBean       | n      | y               | y                | y                | y                            |
| 自定义json转换器 | ？     | ？              | ？               | ？               | ？                           |
| netty            | n      | n               | y                | n                | n                            |
| protobuf         | n      | n               | y                | n                | n                            |



