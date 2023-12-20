## ElementUI 入门



### 1. 安装

在项目根目录运行：

```bash
npm install element-ui
```



### 2. 导入

main.js中导入elementui 模块和样式，以及启用elementui

```javascript
import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

Vue.use(ElementUI);
Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})
```

**注意**

```javascript
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
```

后面一定要有 `;` ,不然后导致无法识别控件



