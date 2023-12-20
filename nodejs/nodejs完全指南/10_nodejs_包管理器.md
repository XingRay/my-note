# nodejs 包管理器

随着项目复杂度的提升，在开发中不可能所有的代码都要手动一行一行的编写，于是就需要一些将一些现成写好的代码引入到项目中，比如jQuery。jQuery这种外部代码在项目中，将其称之为包。

越是复杂的项目，其中需要引入的包也就越多。随着包数量的增多，包的管理就成为了问题。如何下载包？如何删除包？如何更新包？等等一些列的问题需要处理。包管理器便是解决这个问题的工具。



## package.json

包是一组编写好的代码，可以直接引入到项目中使用。具体来说，包其实就是一个文件夹，文件夹中会包含一个或多个js文件，这些js文件就是包中存放的各种代码。除了必要的代码外，包中还有一个东西是必须的，它叫做包的描述文件 —— package.json

package.json顾名思义，它就是一个用来**描述包的json文件**。它里边需要一个json格式的数据（json对象），在json文件中通过各个属性来描述包的基本信息，像包名、版本、依赖等包相关的一切信息。node中通过该文件对项目进行描述, 每一个node项目必须有package.json . 它大概长成这个样子：

```json
{
  "name": "my-awesome-package",
  "version": "1.0.0",
  "author": "Your Name <email@example.com>"
}
```



### package.json中包含的字段

name（必备）

包的名称，可以包含小写字母、_和-



version（必备）

包的版本，需要遵从x.x.x的格式
规则：
版本从1.0.0开始
修复错误，兼容旧版（补丁）1.0.1、1.0.2
添加功能，兼容旧版（小更新）1.1.0
更新功能，影响兼容（大更新）2.0.0



dependencies

表示当前项目的包的依赖包，也就意味着当前项目的包必须有这些包才能够正常的运行。设置依赖项后，当在项目中执行npm install后，依赖项中的包会自动下载到当前项目中。

设置依赖项时"lodash": "^4.17.21"前边的 loadsh 表示包的名字，后边是包的版本。

"^4.17.21"表示匹配最新的4.x.x的版本，也就是如果后期lodash包更新到了4.18.1，当前项目的包也会一起更新，但是如果更新到了5.0.0，我们的包是不会随之更新的。

如果是"~4.17.21"，~表示匹配最小依赖，也就是4.17.x。

如果是"*"则表示匹配最新版本，即x.x.x（不建议使用）。

当然也可以不加任何前缀，这样只会匹配到当前版本。



author

包的作者，格式：Your Name <email@example.com>



description

包的描述



repository

仓库地址（git）



scripts

自动脚本



除了上述的字段外，package.json中还有一些其他字段

通常情况下，每一个node项目都可以被认为是一个包。都应该为其创建 package.json 描述文件。同时，npm可以快速的创建package.json文件。只需要进入项目并输入 `npm init` 即可进入npm的交互界面，只需根据提示输入相应信息即可。输入后根据提示输入相应信息即可



## NPM

node中的包管理局叫做npm（node package manage），npm是世界上最大的包管理库。作为开发人员，我们可以将自己开发的包上传到npm中共别人使用，也可以直接从npm中下载别人开发好的包，在自家项目中使用。

npm由以下三个部分组成：

1. npm网站 （通过npm网站可以查找包，也可以管理自己开发提交到npm中的包。https://www.npmjs.com/）
2. npm CLI（Command Line Interface 即 命令行）（通过npm的命令行，可以在计算机中操作npm中的各种包（下载和上传等））
3. 仓库（仓库用来存储包以及包相关的各种信息）

npm在安装 nodejs 时已经一起安装，所以只要 nodejs 正常安装了，npm自然就可以直接使用了。可以在命令行中输入`npm -v`来查看npm是否安装成功。

```shell
npm -v
9.8.1
```



### 命令

https://docs.npmjs.com/cli/v8/commands



#### 初始化项目

```shell
npm init
```

创建package.json文件（需要回答问题）



#### 自动初始化项目

```shell
npm init -y
```

创建package.json文件（所有值都采用默认值）



#### 配置镜像

npm的服务器位于国外，有时访问速度会比较慢，可以通过配置国内镜像来解决该问题，配置代码：

```shell
npm set registry https://registry.npmmirror.com
```

修改后，直接使用 npm 时访问的就是国内的 npm 镜像服务器，如果想恢复到原版的配置，可以执行以下命令：

```shell
npm config delete registry
```



#### 安装依赖包

```shell
npm install 包名
```

将指定包下载到当前项目中

install时发生了什么？
① 将包下载当前项目的node_modules目录下
② 会在package.json的dependencies属性中添加一个新属性
如: 

```json
"lodash": "^4.17.21"
```

③ 会自动添加package-lock.json文件,  这个文件主要是用来记录当前项目的下包的结构和版本的，提升重新下载包的速度，以及确保包的版本正确。



可以在安装时直接指定，要安装的包的版本，像是这样：

```shell
npm install lodash@3.2.0
```

```shell
npm install lodash@"> 3.2.0"
```

如果不希望包出现在package.json的依赖中，可以添加–no-save指令禁止：

```shell
npm install lodash --no-save
```

可以通过 `-D` 或 `–save-dev`，将其添加到开发依赖

```shell
npm install lodash -D
```



#### 全局安装依赖包

全局安装指，直接将包安装到计算机本地，通常全局安装的都是一些命令行工具，全局安装后工具使用起来也会更加便利。全局安装只需要在执行install指令时添加-g指令即可。

```shell
npm install 包名 -g 
```

比如，现在我们尝试全局安装laughs组件：

```shell
npm install laughs -g
```



#### 自动安装所有依赖

```
npm install
```



#### 卸载依赖包

```shell
npm uninstall 包名
```



#### 引入依赖包

引入从 npm下载的包时，不需要书写路径，直接写包名即可

示例:

```javascript
const _ = require("lodash")
console.log(_)
```



## Yarn

Yarn（Yet Another Resource Navigator）

早期的npm存在有很多问题，不是非常的好用。yarn的出现就是为了解决npm中的各种问题，如何解决呢？方案很简单使用yarn替换掉npm。但是随着时间的推进，npm也在进行不断的迭代，所以到今天npm和其他工具的差距并不是非常的大



### 启用yarn

#### 启用corepack

在新版本的node中，corepack中已经包含了yarn，可以通过启用corepack的方式使yarn启用。首先执行以下命令启用corepack：

```shell
corepack enable
```



#### 查看yarn版本

```bash
yarn -v
```



#### 切换最新版：

```bash
corepack prepare yarn@stable --activate
```



#### 切换为1.x.x的版本：

```bash
corepack prepare yarn@1 --activate
```



### 命令

#### 初始化

初始化，创建package.json

```shell
yarn init
```



#### 镜像配置

配置：

```shell
yarn config set registry https://registry.npmmirror.com
```

恢复：

```shell
yarn config delete registry
```



#### 添加依赖

```shell
yarn add xxx
```



#### 添加开发依赖

```shell
yarn add xxx -D
```



#### 移除包

```shell
yarn remove xxx
```



#### 自动安装依赖

```shell
yarn
```



#### 执行自定义脚本

```shell
yarn run
```



#### 执行自定义脚本

```shell
yarn <指令>
```



#### 全局安装

```shell
yarn global add xxx
```



#### 全局移除

```shell
yarn global remove xxx
```



#### 全局安装目录

```shell
yarn global bin
```



## Pnpm

pnpm又是一款node中的包管理器



### 安装

```bash
npm install -g pnpm
```



### 命令

pnpm init（初始化项目，添加package.json）

pnpm add xxx（添加依赖）

pnpm add -D xxx（添加开发依赖）

pnpm add -g xxx（添加全局包）

pnpm install（安装依赖）

pnpm remove xxx（移除包）

### Pnpm镜像配置

配置：

```bash
pnpm config set registry https://registry.npmmirror.com
```

恢复：

```bash
pnpm config delete registry
```

