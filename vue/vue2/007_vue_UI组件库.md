# UI 组件库



## 1 移动端常用 UI 组件库

1 Vant 

https://youzan.github.io/vant



2 Cube UI 

https://didi.github.io/cube-ui



3 Mint UI 

http://mint-ui.github.io



## 2 PC 端常用 UI 组件库



1 Element UI 

https://element.eleme.cn



2 IView UI 

https://www.iviewui.com



## 3 element-ui

https://element.eleme.io/#/zh-CN/component/quickstart



### 1 安装

```shell
npm install element-ui
```

按需引入需要的插件

```shell
npm install -D babel-plugin-component
```



### 2 修改插件配置

修改babel插件配置文件 babel.conf.js

注意: 

1 官方文档中使用的配置文件为 .babelrc , 新版本的babel的配置文件已经改为了 babel.conf.js

2 官方文档中 preset中关于按需加载的配置为 ["es2015", { "modules": false }] , 这个已经过时了, 要使用 ["@babel/preset-env", {"modules": false}]

```javascript
module.exports = {
  presets: [
      // vue-cli 生成的配置
      '@vue/cli-plugin-babel/preset',
      
      // element-ui 按需加载配置
      ["@babel/preset-env", {"modules": false}]
  ],
  plugins: [
    [
      "component",
      {
        "libraryName": "element-ui",
        "styleLibraryName": "theme-chalk"
      }
    ]
  ]
}
```



### 3 引入

main.js

```javascript
import Vue from 'vue';
import { Button, Select } from 'element-ui';
import App from './App.vue';

Vue.component(Button.name, Button);
Vue.component(Select.name, Select);
/* 
或写为
Vue.use(Button)
Vue.use(Select)
 */

new Vue({
  el: '#app',
  render: h => h(App)
});
```



完整组件列表和引入方式（完整组件列表以 [components.json](https://github.com/ElemeFE/element/blob/master/components.json) 为准）

```javascript
import Vue from 'vue';
import {
  Pagination,
  Dialog,
  Autocomplete,
  Dropdown,
  DropdownMenu,
  DropdownItem,
  Menu,
  Submenu,
  MenuItem,
  MenuItemGroup,
  Input,
  InputNumber,
  Radio,
  RadioGroup,
  RadioButton,
  Checkbox,
  CheckboxButton,
  CheckboxGroup,
  Switch,
  Select,
  Option,
  OptionGroup,
  Button,
  ButtonGroup,
  Table,
  TableColumn,
  DatePicker,
  TimeSelect,
  TimePicker,
  Popover,
  Tooltip,
  Breadcrumb,
  BreadcrumbItem,
  Form,
  FormItem,
  Tabs,
  TabPane,
  Tag,
  Tree,
  Alert,
  Slider,
  Icon,
  Row,
  Col,
  Upload,
  Progress,
  Spinner,
  Badge,
  Card,
  Rate,
  Steps,
  Step,
  Carousel,
  CarouselItem,
  Collapse,
  CollapseItem,
  Cascader,
  ColorPicker,
  Transfer,
  Container,
  Header,
  Aside,
  Main,
  Footer,
  Timeline,
  TimelineItem,
  Link,
  Divider,
  Image,
  Calendar,
  Backtop,
  PageHeader,
  CascaderPanel,
  Loading,
  MessageBox,
  Message,
  Notification
} from 'element-ui';

Vue.use(Pagination);
Vue.use(Dialog);
Vue.use(Autocomplete);
Vue.use(Dropdown);
Vue.use(DropdownMenu);
Vue.use(DropdownItem);
Vue.use(Menu);
Vue.use(Submenu);
Vue.use(MenuItem);
Vue.use(MenuItemGroup);
Vue.use(Input);
Vue.use(InputNumber);
Vue.use(Radio);
Vue.use(RadioGroup);
Vue.use(RadioButton);
Vue.use(Checkbox);
Vue.use(CheckboxButton);
Vue.use(CheckboxGroup);
Vue.use(Switch);
Vue.use(Select);
Vue.use(Option);
Vue.use(OptionGroup);
Vue.use(Button);
Vue.use(ButtonGroup);
Vue.use(Table);
Vue.use(TableColumn);
Vue.use(DatePicker);
Vue.use(TimeSelect);
Vue.use(TimePicker);
Vue.use(Popover);
Vue.use(Tooltip);
Vue.use(Breadcrumb);
Vue.use(BreadcrumbItem);
Vue.use(Form);
Vue.use(FormItem);
Vue.use(Tabs);
Vue.use(TabPane);
Vue.use(Tag);
Vue.use(Tree);
Vue.use(Alert);
Vue.use(Slider);
Vue.use(Icon);
Vue.use(Row);
Vue.use(Col);
Vue.use(Upload);
Vue.use(Progress);
Vue.use(Spinner);
Vue.use(Badge);
Vue.use(Card);
Vue.use(Rate);
Vue.use(Steps);
Vue.use(Step);
Vue.use(Carousel);
Vue.use(CarouselItem);
Vue.use(Collapse);
Vue.use(CollapseItem);
Vue.use(Cascader);
Vue.use(ColorPicker);
Vue.use(Transfer);
Vue.use(Container);
Vue.use(Header);
Vue.use(Aside);
Vue.use(Main);
Vue.use(Footer);
Vue.use(Timeline);
Vue.use(TimelineItem);
Vue.use(Link);
Vue.use(Divider);
Vue.use(Image);
Vue.use(Calendar);
Vue.use(Backtop);
Vue.use(PageHeader);
Vue.use(CascaderPanel);

Vue.use(Loading.directive);

Vue.prototype.$loading = Loading.service;
Vue.prototype.$msgbox = MessageBox;
Vue.prototype.$alert = MessageBox.alert;
Vue.prototype.$confirm = MessageBox.confirm;
Vue.prototype.$prompt = MessageBox.prompt;
Vue.prototype.$notify = Notification;
Vue.prototype.$message = Message;
```



components.json

https://github.com/ElemeFE/element/blob/master/components.json

```javascript
{
  "pagination": "./packages/pagination/index.js",
  "dialog": "./packages/dialog/index.js",
  "autocomplete": "./packages/autocomplete/index.js",
  "dropdown": "./packages/dropdown/index.js",
  "dropdown-menu": "./packages/dropdown-menu/index.js",
  "dropdown-item": "./packages/dropdown-item/index.js",
  "menu": "./packages/menu/index.js",
  "submenu": "./packages/submenu/index.js",
  "menu-item": "./packages/menu-item/index.js",
  "menu-item-group": "./packages/menu-item-group/index.js",
  "input": "./packages/input/index.js",
  "input-number": "./packages/input-number/index.js",
  "radio": "./packages/radio/index.js",
  "radio-group": "./packages/radio-group/index.js",
  "radio-button": "./packages/radio-button/index.js",
  "checkbox": "./packages/checkbox/index.js",
  "checkbox-button": "./packages/checkbox-button/index.js",
  "checkbox-group": "./packages/checkbox-group/index.js",
  "switch": "./packages/switch/index.js",
  "select": "./packages/select/index.js",
  "option": "./packages/option/index.js",
  "option-group": "./packages/option-group/index.js",
  "button": "./packages/button/index.js",
  "button-group": "./packages/button-group/index.js",
  "table": "./packages/table/index.js",
  "table-column": "./packages/table-column/index.js",
  "date-picker": "./packages/date-picker/index.js",
  "time-select": "./packages/time-select/index.js",
  "time-picker": "./packages/time-picker/index.js",
  "popover": "./packages/popover/index.js",
  "tooltip": "./packages/tooltip/index.js",
  "message-box": "./packages/message-box/index.js",
  "breadcrumb": "./packages/breadcrumb/index.js",
  "breadcrumb-item": "./packages/breadcrumb-item/index.js",
  "form": "./packages/form/index.js",
  "form-item": "./packages/form-item/index.js",
  "tabs": "./packages/tabs/index.js",
  "tab-pane": "./packages/tab-pane/index.js",
  "tag": "./packages/tag/index.js",
  "tree": "./packages/tree/index.js",
  "alert": "./packages/alert/index.js",
  "notification": "./packages/notification/index.js",
  "slider": "./packages/slider/index.js",
  "loading": "./packages/loading/index.js",
  "icon": "./packages/icon/index.js",
  "row": "./packages/row/index.js",
  "col": "./packages/col/index.js",
  "upload": "./packages/upload/index.js",
  "progress": "./packages/progress/index.js",
  "spinner": "./packages/spinner/index.js",
  "message": "./packages/message/index.js",
  "badge": "./packages/badge/index.js",
  "card": "./packages/card/index.js",
  "rate": "./packages/rate/index.js",
  "steps": "./packages/steps/index.js",
  "step": "./packages/step/index.js",
  "carousel": "./packages/carousel/index.js",
  "scrollbar": "./packages/scrollbar/index.js",
  "carousel-item": "./packages/carousel-item/index.js",
  "collapse": "./packages/collapse/index.js",
  "collapse-item": "./packages/collapse-item/index.js",
  "cascader": "./packages/cascader/index.js",
  "color-picker": "./packages/color-picker/index.js",
  "transfer": "./packages/transfer/index.js",
  "container": "./packages/container/index.js",
  "header": "./packages/header/index.js",
  "aside": "./packages/aside/index.js",
  "main": "./packages/main/index.js",
  "footer": "./packages/footer/index.js",
  "timeline": "./packages/timeline/index.js",
  "timeline-item": "./packages/timeline-item/index.js",
  "link": "./packages/link/index.js",
  "divider": "./packages/divider/index.js",
  "image": "./packages/image/index.js",
  "calendar": "./packages/calendar/index.js",
  "backtop": "./packages/backtop/index.js",
  "infinite-scroll": "./packages/infinite-scroll/index.js",
  "page-header": "./packages/page-header/index.js",
  "cascader-panel": "./packages/cascader-panel/index.js",
  "avatar": "./packages/avatar/index.js",
  "drawer": "./packages/drawer/index.js",
  "statistic": "./packages/statistic/index.js",
  "popconfirm": "./packages/popconfirm/index.js",
  "skeleton": "./packages/skeleton/index.js",
  "skeleton-item": "./packages/skeleton-item/index.js",
  "empty": "./packages/empty/index.js",
  "descriptions": "./packages/descriptions/index.js",
  "descriptions-item": "./packages/descriptions-item/index.js",
  "result": "./packages/result/index.js"
}
```



### 4 全局配置

在引入 Element 时，可以传入一个全局配置对象。该对象目前支持 `size` 与 `zIndex` 字段。`size` 用于改变组件的默认尺寸，`zIndex` 设置弹框的初始 z-index（默认值：2000）。按照引入 Element 的方式，具体操作如下：

按需引入 Element：

```javascript
import Vue from 'vue';
import { Button } from 'element-ui';

Vue.prototype.$ELEMENT = { size: 'small', zIndex: 3000 };
Vue.use(Button);
```





## 4 ant design

### 1 安装

vue2 只能使用 1.x.x 版本

```shell
npm install ant-design-vue@1
```

按需引入需要的插件

```shell
npm install -D babel-plugin-component
```



2 配置babel

```javascript
module.exports = {
  presets: [
    // vue-cli 生成的配置
    '@vue/cli-plugin-babel/preset',

    // element-ui 按需加载配置
    ["@babel/preset-env", {"modules": false}],

    // ant-design-vue 按需加载
    //["import", { "libraryName": "ant-design-vue", "libraryDirectory": "es", "style": "css" }]
    [
      'import',
      { libraryName: 'ant-design-vue', libraryDirectory: 'es', style: 'css'}
    ]
  ],
  plugins: [
    [
      "component",
      {
        "libraryName": "element-ui",
        "styleLibraryName": "theme-chalk"
      }
    ]
  ]
}
```



3 引入

main.js

示例同时引入了 element-ui 和 ant-design

```javascript
/* 
	该文件是整个项目的入口文件
*/
//引入Vue
import Vue from 'vue'
//引入App组件，它是所有组件的父组件
import App from './App.vue'

import {Button, Select, Row} from 'element-ui';

Vue.use(Button)
Vue.use(Select)
Vue.use(Row)

import 'ant-design-vue/dist/antd.css'

// 注册全部组件
// import Antd from 'ant-design-vue'
// Vue.use(Antd)

// 按需引入
import {
    // 避免与element-ui 的 button 冲突
    Button as AntButton
} from 'ant-design-vue'
// 新增代码：注册特定组件
Vue.component(AntButton.name, AntButton)

//关闭vue的生产提示
Vue.config.productionTip = false

new Vue({
    el: '#app',
    render: h => h(App),
})
```



App.vue

```vue
<template>
  <div id="root">
    <div class="todo-container">
      <div class="todo-wrap">
        <el-row>
          <el-button>默认按钮</el-button>
          <el-button type="primary">主要按钮</el-button>
          <el-button type="success">成功按钮</el-button>
          <el-button type="info">信息按钮</el-button>
          <el-button type="warning">警告按钮</el-button>
          <el-button type="danger">危险按钮</el-button>
        </el-row>

        <el-row>
          <el-button plain>朴素按钮</el-button>
          <el-button type="primary" plain>主要按钮</el-button>
          <el-button type="success" plain>成功按钮</el-button>
          <el-button type="info" plain>信息按钮</el-button>
          <el-button type="warning" plain>警告按钮</el-button>
          <el-button type="danger" plain>危险按钮</el-button>
        </el-row>

        <el-row>
          <el-button round>圆角按钮</el-button>
          <el-button type="primary" round>主要按钮</el-button>
          <el-button type="success" round>成功按钮</el-button>
          <el-button type="info" round>信息按钮</el-button>
          <el-button type="warning" round>警告按钮</el-button>
          <el-button type="danger" round>危险按钮</el-button>
        </el-row>

        <el-row>
          <el-button icon="el-icon-search" circle></el-button>
          <el-button type="primary" icon="el-icon-edit" circle></el-button>
          <el-button type="success" icon="el-icon-check" circle></el-button>
          <el-button type="info" icon="el-icon-message" circle></el-button>
          <el-button type="warning" icon="el-icon-star-off" circle></el-button>
          <el-button type="danger" icon="el-icon-delete" circle></el-button>
        </el-row>

        <HeaderBar :addTodo="addTodo"/>
        <List :todoList="todoList" :deleteTodo="deleteTodo" :todoFinishedChanged="todoFinishedChanged"/>
        <FooterBar :totalCount="totalCount" :finishedCount="finishedCount" :isAllFinished="isAllFinished"
                   :toggleAllFinishedStatus="toggleAllFinishedStatus" :clearFinishedTodo="clearFinishedTodo"/>

        <a-button type="primary">Primary</a-button>
      </div>
    </div>



  </div>
</template>

<script>
//引入组件
import HeaderBar from './components/HeaderBar.vue'
import FooterBar from './components/FooterBar.vue'
import List from "@/components/List.vue";
import {nanoid} from "nanoid";
import 'ant-design-vue/dist/antd.css'

export default {
  name: 'App',
  components: {
    HeaderBar,
    FooterBar,
    List
  },
  data() {
    return {
      todoList: [
        {
          "id": "001", "title": "aaa", "finished": false
        },
        {
          "id": "002", "title": "bbb", "finished": false
        },
        {
          "id": "003", "title": "ccc", "finished": false
        }
      ]
    }
  },
  methods: {
    addTodo(title) {
      if (title === null || title.length === 0) {
        alert("title can not be empty !")
        return
      }
      this.todoList.unshift({id: nanoid(), title, finished: false})
    },

    deleteTodo(todoObj) {
      let index = this.todoList.indexOf(todoObj)
      if (index < 0) {
        console.log("todoObj:", todoObj, " not found in todoList, delete failed !")
        return
      }
      this.todoList.splice(index, 1)
    },

    todoFinishedChanged(todoObj, finished) {
      todoObj.finished = finished
    },

    toggleAllFinishedStatus() {
      if (this.todoList.length === 0) {
        return
      }

      let allFinished = !this.isAllFinished
      this.todoList.forEach(todoObj => {
        todoObj.finished = allFinished
      })
    },

    clearFinishedTodo() {
      console.log("clearFinishedTodo")
      if (this.todoList.length === 0) {
        return
      }
      for (let i = 0, length = this.todoList.length; i < length;) {
        let todoObj = this.todoList[i]
        if (todoObj.finished) {
          this.todoList.splice(i, 1)
          length--
        } else {
          i++
        }
      }
    },

    onChange(date, dateString) {
      console.log(date, dateString);
    },
  },

  computed: {
    totalCount() {
      return this.todoList.length
    },

    finishedCount() {
      return this.todoList.reduce((sum, todoObj) => {
        if (todoObj.finished) {
          sum++
        }
        return sum
      }, 0)
    },

    isAllFinished() {
      if (this.totalCount === 0) {
        return false
      }

      return this.finishedCount === this.totalCount
    },
  }
}
</script>

<style>
/*base*/
body {
  background: #fff;
}

.btn {
  display: inline-block;
  padding: 4px 12px;
  margin-bottom: 0;
  font-size: 14px;
  line-height: 20px;
  text-align: center;
  vertical-align: middle;
  cursor: pointer;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.2), 0 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.btn-danger {
  color: #fff;
  background-color: #da4f49;
  border: 1px solid #bd362f;
}

.btn-danger:hover {
  color: #fff;
  background-color: #bd362f;
}

.btn:focus {
  outline: none;
}

.todo-container {
  width: 600px;
  margin: 0 auto;
}

.todo-container .todo-wrap {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
}
</style>
```



