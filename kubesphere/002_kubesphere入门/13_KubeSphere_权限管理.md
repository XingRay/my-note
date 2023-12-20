## KubeSphere权限管理

https://www.syrr.cn/news/789.html?action=onClick



### 1 创建账号

KubeSphere安装完成后只有一个 admin账号，该账号为 `platform-admin` ，可以管理平台所有的资源。使用admin登录KubeSphere平台 http://192.168.0.112:30880 

账号：admin

密码：P@88w0rd

为了测试方便，可以重置密码为 Admin123



点击右上角`平台管理-访问控制-用户`，点击用户列表右边的创建按钮，创建以下用户：

| 用户名      | 邮箱                 | 平台角色                  | 密码     | 描述                 |
| ----------- | -------------------- | ------------------------- | -------- | -------------------- |
| boos-lei    | boos-lei@test.com    | platform-self-provisioner | Admin123 | test公司的老板       |
| hr-li       | hr-li@test.com       | platform-regular          | Admin123 | test公司人力资源管理 |
| pm-wang     | pm-wang@test.com     | platform-regular          | Admin123 | test公司项目经理     |
| dev-zhao    | dev-zhao@test.com    | platform-regular          | Admin123 | test公司开发工程师   |
| seller-qian | seller-qian@test.com | platform-regular          | Admin123 | test公司销售         |

其中 boss-lei 为 platform-self-provisioner，可以创建企业空间，其他账号均为 platform-regular ，平台的普通用户。



### 2 创建企业空间

切换为 boss-lei 账号，在访问控制-企业空间中的企业空间列表中点击创建按钮，输入

```bash
名称：test-company
别名：test.com
管理员：boss-lei
描述：test公司的企业空间
```

在企业空间列表中点击新创建的 test-company，进入企业空间。



### 3 邀请员工进入企业空间

点击左侧的 `企业空间设置 - 企业空间成员` ，点 `邀请`，在邀请成员页面中在成员列表中点击`＋`进行邀请，邀请时需要指定`分配角色`，邀请的用户和角色为

| 成员        | 角色                          | 角色说明                                     |
| ----------- | ----------------------------- | -------------------------------------------- |
| hr-li       | test-company-admin            | 企业空间管理员，管理企业空间中的所有资源     |
| pm-wang     | test-company-self-provisioner | 预备项目管理员，可以创建项目并成为项目管理员 |
| dev-zhao    | test-company-regular          | 普通成员，查看企业空间中的设置               |
| seller-qian | test-company-viewer           | 观察者，查看企业空间中的所有资源             |



### 4 创建项目空间

切换为 pm-wang 账号，登录后会自动进入test-company的企业空间。 点击左侧的 `项目`，项目列表上点击 `创建`，创建项目，

```
名称：project-test
别名为：测试项目
描述为：这是一个测试项目
```



### 5 邀请员工进入项目空间

在项目列表中点击新创建的`project-test`，进入项目空间， 点击左边的 `项目设置-项目成员`， 在右侧的项目成员列表中点击 `邀请` 邀请的用户和角色为

| 成员        | 角色     | 角色说明                                   |
| ----------- | -------- | ------------------------------------------ |
| dev-zhao    | operator | 操作者，管理项目中除了用户和角色之外的资源 |
| seller-qian | viewer   | 观察者，查看项目中的所有资源               |



