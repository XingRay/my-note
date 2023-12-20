## VSCode环境配置



### 1. 导入模板

文件-->首选项-->用户代码片段-->点击新建代码片段--取名 vue.json 确定

```json
{
	"生成 vue 模板": {
		"prefix": "vue",
		"body": [
			"<template>",
			"<div></div>",
			"</template>",
			"",
			"<script>",
			"//这里可以导入其他文件（比如：组件，工具 js，第三方插件 js，json文件，图片文件等等）",
			"//例如：import 《组件名称》 from '《组件路径》';",
			"",
			"export default {",
			"//import 引入的组件需要注入到对象中才能使用",
			"components: {},",
			"props: {},",
			"data() {",
			"//这里存放数据",
			"return {",
			"",
			"};",
			"},",
			"//计算属性 类似于 data 概念",
			"computed: {},",
			"//监控 data 中的数据变化",
			"watch: {},",
			"//方法集合",
			"methods: {",
			"",
			"},",
			"//生命周期 - 创建完成（可以访问当前 this 实例）",
			"created() {",
			"",
			"},",
			"//生命周期 - 挂载完成（可以访问 DOM 元素）",
			"mounted() {",
			"",
			"},",
			"beforeCreate() {}, //生命周期 - 创建之前",
			"beforeMount() {}, //生命周期 - 挂载之前",
			"beforeUpdate() {}, //生命周期 - 更新之前",
			"updated() {}, //生命周期 - 更新之后",
			"beforeDestroy() {}, //生命周期 - 销毁之前",
			"destroyed() {}, //生命周期 - 销毁完成",
			"activated() {}, //如果页面有 keep-alive 缓存功能，这个函数会触发",
			"}",
			"</script>",
			"<style scoped>",
			"$4",
			"</style>"
		],
		"description": "生成 vue 模板"
	},
	"http-get 请求": {
		"prefix": "httpget",
		"body": [
			"this.\\$http({",
			"url: this.\\$http.adornUrl(''),",
			"method: 'get',",
			"params: this.\\$http.adornParams({})",
			"}).then(({data}) => {",
			"})"
		],
		"description": "httpGET 请求"
	},
	"http-post 请求": {
		"prefix": "httppost",
		"body": [
			"this.\\$http({",
			"url: this.\\$http.adornUrl(''),",
			"method: 'post',",
			"data: this.\\$http.adornData(data, false)",
			"}).then(({ data }) => { });"
		],
		"description": "httpPOST 请求"
	},
}
```



### 2. 插件

1. Auto Close Tag
2. Auto Rename Tag
3. Chinese(Simplified)
4. ESLint
5. Html CSS Suppport
6. HTML/CSS/Javascript Snippets
7. JavaScript(ES6) code Snippets
8. Live Server
9. open in browser
10. Vetur
11. Vue3 Snippets
12. Vue Language Features(Volar)



