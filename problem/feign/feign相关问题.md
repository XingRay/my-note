### 1.  日志显示敏感信息

1.现象：
登录接口 post请求 login， feign日志:
```bash
POST http://localhost:30000/user/login?username=admin&password=123456 HTTP/1.1
```
在feign请求抛出异常时， 在捕获的异常中同样会有密码明文，怎么隐藏密码？

2. 原因
3. 解决方案





