# vue中的ajax

1 xhr (XMLHttpRequest)

原生 api

```javascript
const xhr = new XMLHttpRequest()
xhr.open()
xhr.send()
```

不直接使用, 一般使用二次封装



2 jQuery

对 xhr的封装

```javascript
$.get()
$.post()
```



3 axios

对 xhr的封装, promise 风格



4 fetch

原生api, promise 风格



5 vue-resource

vue官方出的网络库, 现在交由其他团队维护, 更新较少, 了解即可



在 vue项目中推荐使用 axios



## 1 axios

### 1 安装

```shell
npm install axios
```



### 2 引入

```javascript
import axios from 'axios'
```



### 3 使用

```javascript
axios.get("http://localhost:5000/students").then(
    response =>{
        console.log("请求成功了", response.data)
    },
    error =>{
        console.log("请求失败了", error.message)
    }
)
```



此时发送请求会提示 

```shell
blocked by CORS policy: No 'Access-Control-Allow-Origin' header is preset
```

CORS 即 同源策略, 要求请求的3个部分一致:

1 协议 http / https

2 主机名 localhost / baidu.com / 192.168.0.100 ...

3 端口 5000 / 80 / 443 ...



三样任何一部分不一致, 那么这个请求就违反了同源策略, 也就是发生了 跨域问题, 请求发给服务器执行, 服务器执行之后正常返回. 浏览器接收到返回后检测出有跨域问题, 那么返回结果就会**被浏览器拦截**, 不会交给 js .



解决跨域问题:

1 cors 

服务器携带特殊的响应头, 浏览器看到了响应头时,就不会再拦截



2 jsonp

利用`<script>` 浏览器不会检查同源的特点



3 代理服务器

网站所在服务器中转请求

浏览器( localhost:8080 ) -> 代理服务器( localhost:8080 ) -> 数据服务器 ( localhost:5000 )



代理服务器:

1 nginx

2 vue-cli



这里介绍 使用vue-cli 配置代理服务器

https://cli.vuejs.org/zh/config/#devserver-proxy

在 vue.config.js 中添加配置



开启代理服务器（方式一）

```javascript
module.exports = {
  pages: {
    index: {
      //入口
      entry: 'src/main.js',
    },
  },
  lintOnSave:false, //关闭语法检查

  devServer: {
    proxy: 'http://localhost:5000'
  }
}
```

这样配置并不是把所有的请求都转发给 5000 服务器, 如果网站 / 代理服务器本身就有的资源会直接返回, 如果不存在才会去请求被代理的 5000 服务器

项目中的 public 目录就是网站 / 代理服务器的根路径

这种方式不能配置多个代理, 也不能灵活控制是否使用代理

说明：

1 优点：配置简单，请求资源时直接发给前端（8080）即可。

2 缺点：不能配置多个代理，不能灵活的控制请求是否走代理。

3 工作方式：若按照上述配置代理，当请求了前端不存在的资源时，那么该请求会转发给服务器 （优先匹配前端资源）



开启代理服务器（方式二）

```javascript
module.exports = {
  pages: {
    index: {
      //入口
      entry: 'src/main.js',
    },
  },
  lintOnSave:false, //关闭语法检查
  devServer: {
    proxy: {
      // 匹配所有以 '/api1'开头的请求路径
      '/api1': {
        target: 'http://localhost:5000',
        changeOrigin: true,
		pathRewrite:{
          '^/api1':''
        }
      },
      // 匹配所有以 '/api2'开头的请求路径
      '/api2': {
        target: 'http://localhost:5001',
        changeOrigin: true,
		pathRewrite:{
          '^/api2':''
        }
      },
        
      '/foo': {
        target: 'http://localhost:5002',
				pathRewrite:{'^/demo':''
                            },
        // ws: true, //用于支持websocket
        // changeOrigin: true //用于控制请求头中的host值
      }
     
    }
  }
}
```

target: 目标服务器

pathRewrite: 路径重写



changeOrigin:

changeOrigin设置为true时，服务器收到的请求头中的host为目标服务器 localhost:5000 
changeOrigin设置为false时，服务器收到的请求头中的host为代理服务器的为真实 host,  localhost:8080
changeOrigin默认值为true



说明：

1 优点：可以配置多个代理，且可以灵活的控制请求是否走代理。

2 缺点：配置略微繁琐，请求资源时必须加前缀。



## 2 vue-resource

https://github.com/pagekit/vue-resource

vue 官方提供的网络请求库, 后来交给第三方第三方那个维护, 更新比较少了, 了解即可



### 1 安装

```shell
npm install vue-respource
```



### 2 引入

main.js

```javascript
import vueResource from 'vue-resource'

// ...
Vuew.use(vueResource)
// ...
```

引入之后, vm 和 vc 就多了一个 $http 属性



### 3 使用

```javascript
this.$http.get("http://xxx").then()
this.$http.post("http://xxx").then()
```

使用方式和返回值与 axios一模一样



## 3 插槽 slot

### 1 作用

让父组件可以向子组件指定位置插入html结构，也是一种组件间通信的方式，适用于 <strong style="color:red">父组件 ===> 子组件</strong> 。



### 2 分类

默认插槽、具名插槽、作用域插槽



### 3 使用方式

#### 1 默认插槽

父组件中：

```vue
<Category>
   <div>html结构1</div>
</Category>
```

子组件中

```vue
<template>
    <div>
       <!-- 定义插槽 -->
       <slot>插槽默认内容...</slot>
    </div>
</template>
```

父组件中可以在子组件的标签体重放入元素, 放入的元素将被渲染到子元素中 slot 声明的位置

注意: 如果父组件没有给插槽填入元素, 那么组件中将显示插槽中定义的默认内容



#### 2 具名插槽

给插槽取名字, 填入元素的时候, 根据名字决定将元素插入哪个插槽



父组件中

```vue
<Category>
    <div slot="center">html结构1</div>
    <div v-slot:footer>html结构2</div>
    <div v-slot:footer>html结构3</div>
</Category>
```

可以在父组件中的模板中子组件的标签体内的元素设置 slot 属性, 属性名对应子组件中申明的插槽的名称

可以在父组件中定义多个元素使用同一个插槽名, 不会产生覆盖, 而是会追加到指定插槽的位置



```vue
<Category>
    <template slot="center">
      <div>html结构1</div>
    </template>

    <template v-slot:footer>
       <div>html结构2</div>
       <div>html结构3</div>
    </template>
</Category>
```

是使用template:

1 可以在里面定义多个子元素

2 template 不会生成元素

3 template支持 `v-slot` 指令, 注意后面的值不要引号



子组件中

```vue
<template>
    <div>
       <!-- 定义插槽 -->
       <slot name="center">插槽默认内容...</slot>
       <slot name="footer">插槽默认内容...</slot>
    </div>
</template>
```



#### 3 作用域插槽

当数据是在子组件中定义 / 获取 , 并且子组件定义了插槽, 插槽内要填入的元素是由父组件决定的, 但是填入的元素的内容又由定义在子组件中的数据决定, 那么在父组件中要怎么引用定义在子组件中的数据呢? 

数据由子组件决定, 视图 ( 样式 / 结构 ) 由父组件决定

这就要用到作用域插槽了.



##### 1 理解

数据在组件的自身，但根据数据生成的结构需要组件的使用者来决定。（games数据在Category组件中，但使用数据所遍历出来的结构由App组件决定）

数据由子组件传( 定义插槽 ) 递给父组件 ( 使用插槽 )



##### 2 具体编码

父组件中, 在使用插槽处通过使用 `<template>`标签, 并且在 template 上定义属性 `scope` (旧写法) 或者 `slot-scope`  (新写法),  取值为数据的名称,可以自定义, 如 scopeData ,  那么子组件中传递给插槽的数据就会被放入这个对象中, 作为这个对象的属性, 如 `scopeData.games`, 有了数据,就可以用于渲染插槽中的元素了

```vue
<Category>
    <template scope="scopeData">
        <!-- 生成的是ul列表 -->
        <ul>
            <li v-for="g in scopeData.games" :key="g">{{g}}</li>
        </ul>
    </template>
</Category>

<Category>
    <template slot-scope="scopeData">
        <!-- 生成的是h4标题 -->
        <h4 v-for="g in scopeData.games" :key="g">{{g}}</h4>
    </template>
</Category>
```



子组件中

在插槽处通过 :games="games" 的方式向插槽传递数据

```vue
<template>
    <div>
        <slot :games="games"></slot>
    </div>
</template>

<script>
    export default {
        name:'Category',
        props:['title'],
        //数据在子组件自身
        data() {
            return {
                games:['红色警戒','穿越火线','劲舞团','超级玛丽']
            }
        },
    }
</script>
```



作用域插槽可以与默认插槽和具名插槽配合使用





