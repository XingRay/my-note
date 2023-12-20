# nodejs 模块化

## 概述

早期的网页中，是没有一个实质的模块规范的
我们实现模块化的方式，就是最原始的通过script标签来引入多个js文件

随着代码数量的增多所编写的程序复杂度越来越高。此时如果依然将所有的代码编写到同一个文件中，代码将变得非常难以维护。模块化是解决这种问题的关键



问题
1 无法选择要引入模块的哪些内容
2 在复杂的模块场景下非常容易出错
......



### 什么是模块？

模块简单理解其实就是一个代码片段，本来写在一起的JS代码，按照不同的功能将它拆分为一个一个小的代码片段，这个代码片段就是一个模块。简单来说，就是化整为零。



### 模块化带来的好处

模块化后不再是所有代码写在一起，而是按功能做了不同的区分，维护代码时可以比较快捷的找到那些要修改的代码。再者，模块化后，代码被拆分后更容易被复用，同样一个模块可以在不同的项目中使用，大大的降低了开发成本。



### script标签模块的问题

例如: jQuery可以理解为是一种模块。但是jQuery这种模块化的方式存在有许多的不足。jQuery是通过script标签引入的形式来完成模块化的，引入后实际效果是向全局作用域中添加了一个变量$（或jQuery）这样很容易出现模块间互相覆盖的情况。并且当我们使用了较多的模块时，有一些模块是相互依赖的，必须先引入某个组件再引入某个组件，模块才能够正常工作。比如jQuery和jQueryUI，就必须先引入jQuery，如果引入顺序出错将导致jQueryUI无法使用。这还仅仅是两个组件，而实际开发中的依赖关系往往更加复杂，像是a依赖b，b依赖c，c依赖d这种关系，必须按照d、c、b、a的顺序进行引入，有一个顺序错误就会导致其他的一起失效。所以通过script标签实现的模块化是非常的不靠谱的。



### 解决方案

在js中引入模块化的解决方案

在node中，默认支持的模块化规范叫做`CommonJS`，
在CommonJS中，一个js文件就是一个模块



## CommonJS规范

一直到2015年，JavaScript中一直都没有一个内置的模块化系统。但是随着JavaScript项目越来越复杂，模块化的需求早已迫在眉睫。于是大神门开始着手自定义一个模块化系统，CommonJS便是其中的佼佼者。同时它也是Node.js中默认使用的模块化标准。



### 模块内默认私有

模块就是一个js文件，在模块内部任何变量或其他对象都是私有的，不会暴露给外部模块。



### module对象

在CommonJS模块化规范中，在模块内部定义了一个module对象，module对象内部存储了当前模块的基本信息，同时module对象中有一个属性名为exports，exports用来指定需要向外部暴露的内容。只需要将需要暴露的内容设置为exports或exports的属性，其他模块即可通过require来获取这些暴露的内容。



### 引入模块

使用

```javascript
require("模块的路径")
```

函数来引入模块



引入自定义模块时
模块名要以`./` 或 `../`开头



默认情况下，Node.js会将以下内容视为CommonJS模块：

使用.cjs为扩展名的文件
当前的package.json的type属性为commonjs时，扩展名为.js的文件
当前的package.json不包含type属性时，扩展名为.js的文件 ( 也就是 type 的默认值就是 commonjs )
文件的扩展名是mjs、cjs、json、node、js以外的值时（type不是module时）



扩展名可以省略
在CommonJS中，如果省略的js文件的扩展名,  nodejs 会自动为文件补全扩展名

如:

```javascript
const m1 = require("./m1")
```

会优先寻找 `./m1.js` 如果没有js 它会寻找 `./m1.json`

优先级: js --> json --> node（特殊）



引入核心模块时
直接写核心模块的名字即可,  也可以在核心模块前添加 node:

如引入核心模块 path

```javascript
const path = require("path")
const path = require("node:path")
```



在定义模块时，模块中的内容默认是不能被外部看到的
可以通过exports来设置要向外部暴露的内容

访问 exports 的方式有两种：
exports
module.exports

示例:

```javascript
console.log(exports)
console.log(module.exports)
console.log(module.exports === exports)
```

输出:

```shell
{}
{}  
true
```

当我们在其他模块中引入当前模块时，require函数返回的就是exports
可以将希望暴露给外部模块的内容设置为exports的属性



可以通过exports 一个一个的导出值

m1.js

```javascript
exports.a = "孙悟空"
exports.b = {name:"白骨精"}
exports.c = function fn(){
    console.log("哈哈")
}
```

test.js

```javascript
const m1 = require("./m1")
console.log(m1)
```

输出:

```shell
{ a: '孙悟空', b: { name: '白骨精' }, c: [Function: fn] }
```



也可以直接通过 module.exports 同时导出多个值

m2.js

```javascript
module.exports = {
    a: "哈哈",
    b: [1, 3, 5, 7],
    c: () =>{
        console.log(111)
    }
}
```

test.js

```javascript
const m2 = require("./m2")
console.log(m2)
```

输出:

```shell
{ a: '哈哈', b: [ 1, 3, 5, 7 ], c: [Function: c] }
```



部分导入

m3.js

```javascript
module.exports = {
    name:"孙悟空",
    age:18,
    gender:"男"
}
```

test.js

只导入部分属性

```javascript
const name = require("./m3").name
console.log(name)
```

或者使用解构表达式

```javascript
const {name, age, gender} = require("./m3")
console.log(name, age, gender)
```





如果没有js文件, 会寻找json文件, 或者直接指定json文件加载

m3.json

```json
{
    "name":"孙悟空"
}
```

test.js

```javascript
const m3 = require("./m3.json")
console.log(m3)
```

输出:

```shell
{ name: '孙悟空' } 
```



### 引入es6模块

require()是同步加载模块的方法，所以无法用来加载ES6的模块。当我们需要在CommonJS中加载ES模块时，需要通过import()方法来加载。

```javascript
import("./m2.mjs").then(m2 => {
    console.log(m2)
})
```



### 文件作为模块

当我们加载一个自定义的文件模块时，模块的路径必须以/、./或../开头。如果不以这些开头，node会认为你要加载的是核心模块或 node_modules 中的模块。

当我们要加载的模块是一个文件模块时，CommonJS规范会先去寻找该文件，比如：require("./m1") 时，会首先寻找名为 m1 的文件。如果这个文件没有找到，它会自动为文件添加扩展名然后再查询。扩展名的顺序为：js、json和node。还是上边的例子，如果没有找到m1，则会按照顺序搜索m1.js、m1.json、m1.node哪个先找到则返回哪个，如果都没有找到则报错。



### 文件夹作为模块

当我们使用一个文件夹作为模块时，文件夹中必须有一个模块的主文件。如果文件夹中含有 package.json 文件且文件中设置 main 属性，则 main 属性指定的文件会成为主文件，导入模块时就是导入该文件。如果没有 package.json ，则node会按照 index.js 、index.node 的顺序寻找主文件。



### Node_modules

如果我们加载的模块没有以/、./或../开头，且要加载的模块不是核心模块，node会自动去 node_modules 目录下去加载模块。node会先去当前目录下的node_modules 下去寻找模块，找到则使用，没找到则继续去上一层目录中寻找，以此类推，知道找到根目录下的 node_modules 为止。

比如，当前项目的目录为：'C:\Users\lilichao\Desktop\Node-Course\myProject\node_modules'，则模块查找顺序依次为：

‘C:\Users\lilichao\Desktop\Node-Course\node_modules’,
‘C:\Users\lilichao\Desktop\node_modules’,
‘C:\Users\lilichao\node_modules’,
‘C:\Users\node_modules’,
‘C:\node_modules’



### 模块的包装

每一个CommonJS模块在执行时，外层都会被套上一个函数：

```javascript
(function(exports, require, module, __filename, __dirname) {
	// 模块代码会被放到这里
});
```

所以我们之所以能在CommonJS 模块中使用 exports、require 并不是因为它们是全局变量。它们实际上以参数的形式传递进模块的。

exports，用来设置模块向外部暴露的内容

require，用来引入模块的方法

module，当前模块的引用

__filename，模块的路径

__dirname，模块所在目录的路径





## ES模块化

默认情况下，node中的模块化标准是 CommonJS



要想使用ES的模块化，可以采用以下两种方案
1 使用mjs作为扩展名
2 修改package.json将模块化规范设置为ES模块



当在 package.json中设置 "type": "module" 当前项目下所有的js文件都默认为es module

package.json

```json
{
  "type": "module"
}
```



### 导入模块

m4.mjs

```javascript
export let a = 1
export let b = "bbb"
export const c = {name: "猪八戒"}
export let d = (a, b) => a + b
```



test.mjs

导入m4模块，es模块不能省略扩展名（官方标准）

```javascript
import {a, b, c, d} from "./m4.mjs"
console.log(a, b, c, d)
```

输出:

```shell
1 bbb { name: '猪八戒' } [Function: d]
```



通过 `as` 来指定别名

test.mjs

```javascript
import {a as hello, b, c, d} from "./m4.mjs"
console.log(hello, b, c, d)
```

```shell
1 bbb { name: '猪八戒' } [Function: d]
```



一次性导入所有

`import *` 

```javascript
import * as m4 from "./m4.mjs"
console.log(m4)
```

输出:

```shell
[Module: null prototype] {
  a: 1,                   
  b: 'bbb',               
  c: { name: '猪八戒' },  
  d: [Function: d]        
}
```

开发时要尽量避免`import *` 情况

在后台项目中区别不大, 但是在前端项目, 如 vue / react 中,  import * 会导致将模块中不论是否使用到的内容全部导入, 前端项目要使用打包工具进行打包, 如  webpack 或 vite ,  打包工具会对文件进行检索, 把需要使用到的文件(脚本/图片/样式...)全部打包, 使用 import * 将会使得打包工具认为模块中所有的资源全部进行打包, 会使得项目变得很大,性能也会受到影响, 尽量按需引用



### export default

导入模块的默认导出
默认导出的内容，可以随意命名

m5.mjs

设置默认导出

```javascript
export default function sum(a, b) {
    return a + b
}
export let a = 111
```

test.mjs

默认导出的内容，可以随意命名, 但是不要写到 `{}` 中, `{}` 中的是命名导出

```javascript
import sum, { a } from "./m5.mjs"
console.log(sum, a)
```

输出:

```shell
[Function: sum] 111
```



 一个模块中只有一个默认导出

```
export default function sum(a, b) {
    return a + b
}
let d = 20
export default d 
```

报错:

```shell
SyntaxError: Duplicate export of 'default'
```



通过ES模块化，**导入的内容都是常量**

m6.mjs

```javascript
export let a = 111
export let b = { name: "猪八戒" }
```

test.mjs

```
import {a , b} from "./m6.mjs"
a = 333
```

输出:

```shell
TypeError: Assignment to constant variable. 
```

但是注意: 导出的对象的字段是可以修改的

test.mjs

```javascript
import {a , b} from "./m6.mjs"
b.name = "沙和尚"
console.log(b)
```

输出:

```shell
{ name: '沙和尚' }
```



es模块都是运行在严格模式下的
ES模块化，在浏览器中同样支持，但是通常我们不会直接使用
通常都会结合打包工具使用



