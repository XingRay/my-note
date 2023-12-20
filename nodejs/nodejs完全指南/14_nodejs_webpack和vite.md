# webpack和vite



## 构建工具

当我们习惯了在 node 中编写代码的方式后，在回到前端编写 html、css、js 这些东西会感觉到各种的不便。比如：不能放心的使用模块化规范（浏览器兼容性问题）、即使可以使用模块化规范也会面临模块过多时的加载问题。

我们就迫切的希望有一款工具可以对代码进行打包，将多个模块打包成一个文件。这样一来即解决了兼容性问题，又解决了模块过多的问题。

构建工具就起到这样一个作用，通过构建工具可以将使用 ESM 规范编写的代码转换为旧的 JS 语法，这样可以使得所有的浏览器都可以支持代码。



## Webpack

### 使用步骤

1 初始化项目

```shell
yarn init -y
```



2 安装依赖

```
yarn add -D webpack webpack-cli
```



3 在项目中创建`src`目录，然后编写代码（index.js）

4 执行`yarn webpack`来对代码进行打包（打包后观察 dist 目录）



配置文件（webpack.config.js）

```javascript
const path = require("path")
module.exports = {
    mode: "production",
    entry: "./src/index.js",
    output: {},
    module: {
        rules: [
            {
                test: /\.css$/i,
                use: ["style-loader", "css-loader"]
            }
        ]
    }
}
```



### babel

在编写 js 代码时，经常需要使用一些 js 中的新特性，而新特性在旧的浏览器中兼容性并不好。此时就导致我们无法使用一些新的特性。

但是我们现在希望能够使用新的特性，我们可以采用折中的方案。依然使用新特性编写代码，但是代码编写完成时我们可以通过一些工具将新代码转换为旧代码。

babel 就是这样一个工具，可以将新的 js 语法转换为旧的 js，以提高代码的兼容性。

我们如果希望在 webpack 支持 babel，则需要向 webpack 中引入 babel 的 loader

使用步骤

#### 1 安装

```shell
npm install -D babel-loader @babel/core @babel/preset-env
```



#### 2 配置

```javascript
module: {
    rules: [
        {
            test: /\.m?js$/,
            exclude: /(node_modules|bower_components)/,
            use: {
                loader: "babel-loader",
                options: {
                    presets: ["@babel/preset-env"]
                }
            }
        }
    ]
}
```

#### 3 设置兼容列表

在 package.json 中设置兼容列表

```json
"browserslist": [
        "defaults"
 ]
```

https://github.com/browserslist/browserslist



### 插件（plugin）

插件用来为 webpack 来扩展功能

html-webpack-plugin

这个插件可以在打包代码后，自动在打包目录生成 html 页面

使用步骤：

1 安装依赖

2 配置插件

```javascript
plugins: [
    new HTMLPlugin({
        // title: "Hello Webpack",
        template: "./src/index.html"
    })
]
```



### 开发服务器（webpack-dev-server）

安装：

```shell
yarn add  -D webpack-dev-server
```

启动

```shell
yarn webpack serve --open
```

在配置中添加配置字段

配置源码的映射

```json
devtool:"inline-source-map"
```





## Vite

Vite 也是前端的构建工具

相较于 webpack，vite 采用了不同的运行方式：

开发时，并不对代码打包，而是直接采用 ESM 的方式来运行项目

在项目部署时，在对项目进行打包

除了速度外，vite 使用起来也更加方便



### 基本使用：

1安装开发依赖 vite

2 vite 的源码目录就是项目根目录

3 开发命令：

启动开发服务器

```shell
vite
```



 打包代码

```shell
vite build
```



 预览打包后代码

```shell
vite preview
```



使用命令构建

```bash
npm create vite@latest
yarn create vite
pnpm create vite
```

配置文件：`vite.config.js`

格式：

```javascript
import { defineConfig } from "vite"
import legacy from "@vitejs/plugin-legacy"

export default defineConfig({
    plugins: [
        legacy({
            targets: ["defaults"]
            // targets: ["defaults", "ie 11"]
        })
    ]
})
```

## 