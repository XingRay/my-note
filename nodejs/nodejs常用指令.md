# npm设置/取消淘宝镜像代理

时间:2022-08-08

本文章向大家介绍npm设置/取消淘宝镜像代理，主要内容包括其使用实例、应用技巧、基本知识点总结和需要注意事项，具有一定的参考价值，需要的朋友可以参考一下。

设置淘宝镜像代理

```
npm config set registry https://registry.npm.taobao.org
```

取消代理

```
npm config set registry https://registry.npmjs.org
```

查看npm代理

```
npm info underscore
```



初始化项目

```bash
npm init -y
```



安装模块，比如 vue

```bash
npm install vue
```

指定版本：

```bash
npm install vue@^2
```

