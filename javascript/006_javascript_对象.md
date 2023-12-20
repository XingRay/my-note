# javascript 对象



## 1 对象

数据类型：
原始值
1 数值 Number
2 大整数 BigInt
3 字符串 String
4 布尔值 Boolean
5 空值 Null
6 未定义 Undefined
7 符号 Symbol

对象
对象是JS中的一种复合数据类型，
它相当于一个容器，在对象中可以存储各种不同类型数据

原始值只能用来表示一些简单的数据，不能表示复杂数据

比如：现在需要在程序中表示一个人的信息

```javascript
let name = "孙悟空"
let age = 18
let gender = "男"
```

创建对象

```javascript
let obj = Object()
obj.name = "孙悟空"
obj.age = 18
obj.gender = "男"
```

修改属性

```javascript
obj.name = "Tom sun"
console.log(obj.name)
```

删除属性

```javascript
delete obj.name
console.log(obj.name)
```



属性名
通常属性名就是一个字符串，所以属性名可以是任何值，没有什么特殊要求
但是如果你的属性名太特殊了，不能直接使用，需要使用[]来设置
虽然如此，但是我们还是强烈建议属性名也按照标识符的规范命名

也可以使用符号（`symbol`）作为属性名，来添加属性
获取这种属性时，也必须使用 `symbol`
使用`symbol`添加的属性，通常是那些不希望被外界访问的属性

使用[]去操作属性时，可以使用变量

属性值
对象的属性值可以是任意的数据类型，也可以是一个对象

使用 `typeof` 检查一个对象时，会返回 `object`

```javascript
obj.name = "孙悟空"
obj.if = "哈哈" // 不建议
obj.let = "嘻嘻"// 不建议
obj["1231312@#@!#!#!"] = "呵呵"// 不建议

let mySymbol = Symbol()
let newSymbol = Symbol()
// 使用symbol作为属性名
obj[mySymbol] = "通过symbol添加的属性"
console.log(obj[mySymbol])


obj.age = 18
obj["gender"] = "男"

let str = "address"

obj[str] = "花果山" // 等价于 obj["address"] = "花果山"

obj.str = "哈哈" // 使用.的形式添加属性时，不能使用变量

obj.a = 123
obj.b = 'hello'
obj.c = true
obj.d = 123n
obj.f = Object()
obj.f.name = "猪八戒"
obj.f.age = 28

console.log(obj.f.name)
console.log(obj.gender)
console.log(obj["gender"])
console.log(typeof obj)
```



对象字面量
可以直接使用{} 来创建对象
使用{}所创建的对象，可以直接向对象中添加属性
语法：

```javascript
{
  属性名:属性值,
  [属性名]:属性值,
}
```

```javascript
let mySymbol = Symbol()

let obj2 = {
    name:"孙悟空",
    age:18,
    ["gender"]:"男",
    [mySymbol]:"特殊的属性",
    hello:{
        a:1,
        b:true
    }
}

console.log(obj)
console.log(obj2)
```





## 2 in 运算符

用来检查对象中是否含有某个属性
语法 属性名 in obj
如果有返回true，没有返回false

```javascript
console.log("name" in obj)
```

枚举属性，指将对象中的所有的属性全部获取

### for-in语句

语法：

```javascript
for(let propName in 对象){
    语句...
}
```

for-in的循环体会执行多次，有几个属性就会执行几次，
每次执行时，都会将一个属性名赋值给我们所定义的变量

注意：并不是所有的属性都可以枚举，比如 使用符号添加的属性

```javascript
let obj = {
    name:'孙悟空',
    age:18,
    gender:"男",
    address:"花果山",
    [Symbol()]:"测试的属性" // 符号添加的属性是不能枚举
}

for(let propName in obj){
    console.log(propName, obj[propName])
}
```



## 3 可变类型

原始值都属于不可变类型，一旦创建就无法修改
在内存中不会创建重复的原始值

```javascript
let a = 10
let b = 10
a = 12 // 当我们为一个变量重新赋值时，绝对不会影响其他变量

console.log("a =", a)
console.log("b =", b)
```



对象属于可变类型
对象创建完成后，可以任意的添加删除修改对象中的属性
注意：
当对两个对象进行相等或全等比较时，比较的是对象的内存地址
如果有两个变量同时指向一个对象，
通过一个变量修改对象时，对另外一个变量也会产生影响

```javascript
// let obj = {name:"孙悟空"}
let obj = Object()
obj.name = "孙悟空"
obj.age = 18

let obj2 = Object()
let obj3 = Object()

// console.log(obj2 == obj3) // false
let obj4 = obj
obj4.name = "猪八戒" // 当修改一个对象时，所有指向该对象的变量都会收到影响

console.log("obj", obj)
console.log("obj4", obj4)
console.log(obj === obj4)
```



## 4 变量的修改

修改对象
修改对象时，如果有其他变量指向该对象
则所有指向该对象的变量都会受到影响

修改变量
修改变量时，只会影响当前的变量

在使用变量存储对象时，很容易因为改变变量指向的对象，提高代码的复杂度
所以通常情况下，声明存储对象的变量时会使用 `const`

注意：
`const` 只是禁止变量被重新赋值，对对象的修改没有任何影响

```javascript
const obj = {
    name: "孙悟空",
}

const obj2 = obj

// obj2 = {}

obj2.name = "猪八戒" // 修改对象

// obj2 = null // 修改变量

// console.log(obj)
// console.log(obj2)

const obj3 = {
    name:"猪八戒"
}

obj3.name = "沙和尚"

console.log(obj3)
```



## 5 方法

方法（method）
当一个对象的属性指向一个函数，
那么我们就称这个函数是该对象的方法
调用函数就称为调用对象的方法

```javascript
let obj = {}

obj.name = "孙悟空"
obj.age = 18

// 函数也可以成为一个对象的属性
obj.sayHello = function(){
    alert("hello")
}

console.log(obj)
obj.sayHello()
document.write()

String()
```

