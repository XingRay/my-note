# javascript 函数



## 1 函数

函数（Function）
函数也是一个对象
它具有其他对象所有的功能
函数中可以存储代码，且可以在需要时调用这些代码

语法：

```javascript
function 函数名(){
    语句...
}
```

调用函数：
调用函数就是执行函数中存储的代码
语法：
函数对象()

使用 `typeof` 检查函数对象时会返回 `function`

```javascript
function fn(){
    console.log("你好！")
    console.log("Hello!")
    console.log("萨瓦迪卡")
    console.log("阿尼哈撒有")
}

console.log(typeof fn)

fn()
```



## 2 函数的创建方式

函数的定义方式：
1 函数声明

```javascript
function 函数名(){
  语句...
}
```

2 函数表达式

```javascript
const 变量 = function(){
  语句...
}
```

3 箭头函数

```javascript
() => {
  语句...
}
```



```javascript
function fn(){
    console.log("函数声明所定义的函数~")
}

const fn2 = function(){
    console.log("函数表达式")
}

const fn3 = () => {
    console.log("箭头函数")
}

const fn4 = () => console.log("箭头函数")


console.log(typeof fn)
console.log(typeof fn2)
console.log(typeof fn3)
console.log(typeof fn4)

fn4()
```



## 3 参数

定义一个可以求任意两个数和的函数

```javascript
function sum(){
    let a = 123
    let b = 456
    console.log(a + b)
}

const sum2 = () => console.log(1 + 1)
sum()
```

形式参数
在定义函数时，可以在函数中指定数量不等的形式参数（形参）
在函数中定义形参，就相当于在函数内部声明了对应的变量但是没有赋值

实际参数
在调用函数时，可以在函数的()传递数量不等的实参
实参会赋值给其对应的形参

参数：
1 如果实参和形参数量相同，则对应的实参赋值给对应的形参
2 如果实参多余形参，则多余的实参不会使用
3 如果形参多余实参，则多余的形参为undefined



参数的类型
JS中不会检查参数的类型，可以传递任何类型的值作为参数

1 函数声明

```javascript
function 函数名([参数]){
    语句...
}
```

2 函数表达式

```javascript
const 变量 = function([参数]){
语句...
}
```

3 箭头函数

```javascript
([参数]) => {
语句...
}
```

```javascript
function fn(a, b){
    console.log("a =", a, a.name)
    console.log("b =", b)
}

fn(1)
fn(true, "hello")
fn(null, 11n)
fn({name:"孙悟空"},"hello")

function sum(a, b){
    console.log(a + b)
}

sum(123, 456)
```



## 4 箭头函数的参数

```javascript
const fn = (a, b) => {
    console.log("a =", a);
    console.log("b =", b);
}

// 当箭头函数中只有一个参数时，可以省略()
const fn2 = a => {
    console.log("a =", a);
}

fn2(123)

// 定义参数时，可以为参数指定默认值
// 默认值，会在没有对应实参时生效
const fn3 = (a=10, b=20, c=30) => {
    console.log("a =", a);
    console.log("b =", b);
    console.log("c =", c);
}

fn3(1, 2)
```



## 5 对象作为参数

```javascript
function fn(a) {
    console.log("a =", a)
    console.log(a.name)

    // a = {} // 修改变量时，只会影响当前的变量
    a.name = "猪八戒" // 修改对象时，如果有其他变量指向该对象则所有指向该对象的变量都会受到影响
    console.log(a)

}

// 对象可以作为参数传递
let obj = {name: "孙悟空"}

// 传递实参时，传递并不是变量本身，而是变量中存储的值
// fn(obj)

// console.log(obj)

let obj2 = {name: "沙和尚"}

// 函数每次调用，都会重新创建默认值
function fn2(a = {name: "沙和尚"}) {
    console.log("a =", a)
    a.name = "唐僧"
    console.log("a =", a)
}

fn2() // 沙和尚 唐僧
fn2() // 沙和尚 唐僧 or 唐僧 唐僧
```



## 6 函数作为参数

```javascript
function fn(a) {
    console.log("a =", a)
    a()
}
```

在JS中，函数也是一个对象（一等函数）
别的对象能做的事情，函数也可以

```javascript
let obj = {name: "孙悟空"}

function fn2() {
    console.log("我是fn2")
}

fn(fn2)

fn(function () {
    console.log("我是匿名函数~")
})

fn(() => console.log("我是箭头函数"))
```



## 7 函数的返回值

```javascript
function sum(a, b) {
    // console.log(a + b)
    // 计算完成后，将计算的结果返回而不是直接打印
    return a + b
}
```



在函数中，可以通过return关键字来指定函数的返回值
返回值就是函数的执行结果，函数调用完毕返回值便会作为结果返回

任何值都可以作为返回值使用（包括对象和函数之类）
如果return后不跟任何值，则相当于返回undefined
如果不写return，那么函数的返回值依然是undefined

return一执行函数立即结束

```javascript
function fn() {
    // return {name:"孙悟空"}
    // return ()=>alert(123)
    // return

    alert(123)
    return
    alert(456)
}

let result = fn()

// result = sum(123, 456)
// result = sum(10, result)

console.log("result =", result)
```



## 8 箭头函数的返回值

箭头函数的返回值可以直接写在箭头后
如果直接在箭头后设置对象字面量为返回值时，对象字面量必须使用()括起来

```javascript
const sum = (a, b) => a + b
const fn = () => ({name:"孙悟空"})
let result = sum(123, 456)
result = fn()
console.log(result)
```



## 9 作用域

作用域（scope）
作用域指的是一个变量的可见区域
作用域有两种：

**全局作用域**
全局作用域在网页运行时创建，在网页关闭时消耗
所有直接编写到script标签中的代码都位于全局作用域中
全局作用域中的变量是全局变量，可以在任意位置访问

**局部作用域**
块作用域
块作用域是一种局部作用域
块作用域在代码块执行时创建，代码块执行完毕它就销毁
在块作用域中声明的变量是局部变量，只能在块内部访问，外部无法访问



```javascript
let a = "变量a"

{
    let b = "变量b"

    {
        {
            console.log(b)
        }
    }
}

{
    console.log(b)
}
```



## 10 函数作用域

函数作用域
函数作用域也是一种局部作用域
函数作用域在函数调用时产生，调用结束后销毁
函数每次调用都会产生一个全新的函数作用域
在函数中定义的变量是局部变量，只能在函数内部访问，外部无法访问

```javascript
function fn(){
    let a = "fn中的变量a"
    console.log(a)
}

fn()

console.log(a)
```



## 11 作用域链

作用域链
当我们使用一个变量时，JS解释器会优先在当前作用域中寻找变量，
如果找到了则直接使用
如果没找到，则去上一层作用域中寻找，找到了则使用
如果没找到，则继续去上一层寻找，以此类推
如果一直到全局作用域都没找到，则报错 xxx is not defined

```javascript
let a = 10
{
    let a = "第一代码块中的a"
    {
        let a = "第二代码块中的a"
        console.log(a)
    }
}

let b = 33

function fn(){
    let b = 44

    function f1(){
        let b = 55
        console.log(b)
    }

    f1()

}

fn()
```



## 12 window对象

Window对象
在浏览器中，浏览器为我们提供了一个window对象，可以直接访问
window对象代表的是浏览器窗口，通过该对象可以对浏览器窗口进行各种操作, 除此之外window对象还负责存储JS中的内置对象和浏览器的宿主对象
window对象的属性可以通过window对象访问，也可以直接访问
函数就可以认为是window对象的方法

```javascript
// alert(window)
// window.alert(123)
// window.console.log("哈哈")

window.a = 10 // 向window对象中添加的属性会自动成为全局变量
console.log(a)
```



var 用来声明变量，作用和let相同，但是var不具有块作用域
在全局中使用var声明的变量，都会作为window对象的属性保存
使用function声明的函数，都会作为window的方法保存
使用let声明的变量不会存储在window对象中，而存在一个秘密的小地方（无法访问）
var虽然没有块作用域，但有函数作用域

```javascript
var b = 20 // window.b = 20

function fn(){
    alert('我是fn')
}

console.log(window.b)
window.fn()

let c = 33
window.c = 44
console.log(c)

function fn2(){
    var d = 10 // var虽然没有块作用域，但有函数作用域
    d = 10 // 在局部作用域中，如果没有使用var或let声明变量，则变量会自动成为window对象的属性 也就是全局变量
}

fn2()
console.log(d)
```



## 13 变量的提升

使用var声明的变量，它会在所有代码执行前被声明
所以我们可以在变量声明前就访问变量

函数的提升
使用函数声明创建的函数，会在其他代码执行前被创建
所以我们可以在函数声明前调用函数

let声明的变量实际也会提升，但是在赋值之前解释器禁止对该变量的访问

```javascript
console.log(b)
let b = 10

fn()
function fn(){
  alert("我是fn函数~")
}

fn2()
var fn2 = function(){

}

console.log(a)
var a = 10
a = 10 
window.a = 10
```



## 14 练习

```javascript
var a = 1

function fn() {
    a = 2
    console.log(a) // 2
}

fn()
console.log(a) // 2
```



变量和函数的提升同样适用于函数作用域

```javascript
var a = 1

function fn() {
    console.log(a) //undefined
    var a = 2
    console.log(a) // 2
}

fn()
console.log(a) // 1
```



定义形参就相当于在函数中声明了对应的变量，但是没有赋值

```javascript
var a = 1

function fn(a) {
    console.log(a) //undefined
    a = 2
    console.log(a) // 2
}

fn()
console.log(a) // 1 
```

```javascript
var a = 1

function fn(a) {
    console.log(a) //10
    a = 2
    console.log(a) // 2
}

fn(10)
console.log(a) // 1
```

```javascript
var a = 1

function fn(a) {
    console.log(a) //1
    a = 2
    console.log(a) // 2
}

fn(a)
console.log(a) // 1
```

```javascript
var a = 1

console.log(a) // 1

function a() {
    alert(2)
}

console.log(a) // 1

var a = 3

console.log(a) // 3

var a = function () {
    alert(4)
}

console.log(a) // 4

var a

console.log(a) // 4
```



## 15 debug

```javascript
debugger // 在代码中打了一个断点

console.log(a) // 2

var a = 1
console.log(a) // 1

function a() {
    alert(2)
}

console.log(a) // 1
var a = 3

console.log(a) // 3
var a = function () {
    alert(4)
}

console.log(a) // 4
var a
console.log(a) // 4
```



## 16 立即执行函数

在开发中应该尽量减少直接在全局作用域中编写代码！
所以我们的代码要尽量编写的局部作用域
如果使用let声明的变量，可以使用{}来创建块作用域

```javascript
{
    let a = 10
}

{
    let a = 20
}
```

```javascript
function fn(){
    var a = 10
}

fn()

function fn2(){
    var a = 20
}

fn2()
```

希望可以创建一个只执行一次的匿名函数



立即执行函数（ `IIFE` ）
立即是一个匿名的函数，并它只会调用一次
可以利用 `IIFE` 来创建一个一次性的函数作用域，避免变量冲突的问题

```javascript
(function () {
    let a = 10
    console.log(111)
}());


(function () {
    let a = 20
    console.log(222)
}())
```



## 17 this

函数在执行时，JS解析器每次都会传递进一个隐含的参数, 这个参数就叫做 this

this会指向一个对象, this所指向的对象会根据函数调用方式的不同而不同
1 以函数形式调用时，this指向的是window
2 以方法的形式调用时，this指向的是调用方法的对象



通过this可以在方法中引用调用方法的对象

```javascript
function fn() {
    console.log(this === window)
    console.log("fn打印", this)
}

const obj = { name: "孙悟空" }
obj.test = fn

const obj2 = { name: "猪八戒", test: fn }

fn()
window.fn()
obj.test() // {name:"孙悟空"}
obj2.test() // {name:"猪八戒", test:fn}

const obj3 = {
    name: "沙和尚",
    sayHello: function () {
        console.log(this.name)
    },
}
const obj4 = {
    name: "唐僧",
    sayHello: function(){
        console.log(this.name)
    }
}

// 为两个对象添加一个方法，可以打印自己的名字
obj3.sayHello()
obj4.sayHello()
```



## 18 箭头函数的this

箭头函数：

```javascript
([参数]) => 返回值
```

例子：
无参箭头函数：

```javascript
() => 返回值
```

一个参数的：

```javascript
a => 返回值
```

多个参数的：

```javascript
(a, b) => 返回值
```

只有一个语句的函数：

```javascript
() => 返回值
```

只返回一个对象的函数：

```javascript
() => ({...})
```

有多行语句的函数：

```javascript
() => {
    ....    
    return 返回值
}
```

箭头函数没有自己的this，它的this有外层作用域决定
箭头函数的this和它的调用方式无关

```javascript
function fn() {
    console.log("fn -->", this)
}

const fn2 = () => {
    console.log("fn2 -->", this) // 总是window
}

fn() // window
fn2() // window

const obj = {
    name: "孙悟空",
    fn, // fn:fn
    fn2,
    sayHello() {
        console.log(this.name)

        function t() {
            console.log("t -->", this)
        }

        t()

        const t2 = () => {
            console.log("t2 -->", this)
        }

        t2()
    }
}

obj.fn() // obj
obj.fn2() // window

obj.sayHello()
```



## 19 严格模式

JS运行代码的模式有两种：
**正常模式**
默认情况下代码都运行在正常模式中，
在正常模式，语法检查并不严格
它的原则是：能不报错的地方尽量不报错
这种处理方式导致代码的运行性能较差

**严格模式**
在严格模式下，语法检查变得严格
1 禁止一些语法
2 更容易报错
3 提升了性能

在开发中，应该**尽量使用严格模式**，
这样可以将一些隐藏的问题消灭在萌芽阶段，
同时也能提升代码的运行性能



```javascript
"use strict" // 全局的严格模式

let a = 10
console.log(a)

function fn(){
    "use strict" // 函数的严格的模式
}
```



## 20 递归
调用自身的函数称为递归函数
递归的作用和循环是基本一直

递归的核心思想就是将一个大的问题拆分为一个一个小的问题，小的问题解决了，大的问题也就解决了

编写递归函数，一定要包含两个要件：
1 基线条件 ——  递归的终止条件
2 递归条件 ——  如何对问题进行拆分

递归的作用和循环是一致的，不同点在于，递归思路的比较清晰简洁，循环的执行性能比较好
在开发中，一般的问题都可以通过循环解决，也是尽量去使用循环，少用递归
只在一些使用循环解决比较麻烦的场景下，才使用递归



示例: 

创建一个函数，可以用来求任意数的阶乘

1! 1
2! 1 x 2 = 2
3! 1 x 2 x 3 = 6
...
10! 1 x 2 x 3 x 4 x 5 x 6 x 7 x 8 x 9 x 10 = xxx



循环:

```javascript
function jieCheng(num){
    // 创建一个变量用了记录结果
    let result = 1

    for(let i=2; i<=num; i++){
        result *= i
    }

    return result
}

let result = jieCheng(3)

console.log(result)
```



如果用递归来解决阶乘的问题？
5! = 4! x 5
4! = 3! x 4
3! = 2! x 3
2! = 1! x 2
1! = 1



```javascript
function jieCheng2(num){
    // 基线条件
    if(num === 1){
        return 1
    }
    // 递归条件
    // num! = (num-1)! * num
    return jieCheng2(num-1) * num
}

result = jieCheng2(5)
/*
    jieCheng2(5)
        - return jieCheng2(4) * 5
         - return jieCheng2(3) * 4
          - return jieCheng2(2) * 3
            - return jieCheng2(1) * 2
             - return 1
*/
console.log(result)
```



练习

一对兔子出生后的两个月后每个月都能生一对小兔子
编写一个函数，可以用来计算第n个月的兔子的数量

1   2   3   4   5   6   7   8   9   10  11  12
1   1   2   3   5   8   13  21  34 ....
规律，当前数等于前两个数之和（斐波那契数列）, 求斐波那契数列中的第n个数

```javascript
function fib(n) {
    // 确定基线条件
    if (n < 3) {
        return 1
    }

    // 设置递归条件
    // 第n个数 = 第n-1个数 + 第n-2个数
    return fib(n - 1) + fib(n - 2)
}

let result = fib(10)

console.log(result)
```





## 21 可变参数

arguments
arguments是函数中又一个隐含参数
arguments是一个类数组对象（伪数组）
和数组相似，可以通过索引来读取元素，也可以通过for循环变量，但是它不是一个数组对象，不能调用数组的方法
arguments用来存储函数的实参，
无论用户是否定义形参，实参都会存储到arguments对象中
可以通过该对象直接访问实参



```javascript
function fn() {
    console.log(arguments[2])
    console.log(Array.isArray(arguments))
    for (let i = 0; i < arguments.length; i++) {
        console.log(arguments[i])
    }

    for (let v of arguments) {
        console.log(v)
    }

    arguments.forEach((ele) => console.log(ele))
}

fn(1, 10, 33)
```



定义一个函数，可以求任意个数值的和

```javascript
function sum() {
    // 通过arguments，可以不受参数数量的限制更加灵活的创建函数
    let result = 0

    for (let num of arguments) {
        result += num
    }

    return result
}
```



可变参数，在定义函数时可以将参数指定为可变参数
可变参数可以接收任意数量实参，并将他们统一存储到一个数组中返回
可变参数的作用和 `arguments` 基本是一致，但是也具有一些不同点：
1 可变参数的名字可以自己指定
2 可变参数就是一个数组，可以直接使用数组的方法
3 可变参数可以配合其他参数一起使用

```javascript
function fn2(...abc) {
    console.log(abc)
}

function sum2(...num) {
    return num.reduce((a, b) => a + b, 0)
}
```

当可变参数和普通参数一起使用时，需要将可变参数写到最后

```javascript
function fn3(a, b, ...args) {
    for (let v of arguments) {
        console.log(v)
    }
    console.log(args)
}

fn3(123, 456, "hello", true, "1111")
```



## 22 call apply bind

根据函数调用方式的不同，this的值也不同：
1 以函数形式调用，this是window
2 以方法形式调用，this是调用方法的对象
3 构造函数中，this是新建的对象
4 箭头函数没有自己的this，由外层作用域决定
5 通过call和apply调用的函数，它们的第一个参数就是函数的this
6 通过bind返回的函数，this由bind第一个参数决定（无法修改）

bind() 是函数的方法，可以用来创建一个新的函数
bind可以为新函数绑定 this
bind可以为新函数绑定参数

箭头函数没有自身的this，它的this由外层作用域决定，
也无法通过call apply 和 bind修改它的this 
箭头函数中没有arguments



```javascript
function fn() {
    console.log("函数执行了~", this)
}

const obj = { name: "孙悟空", fn }
```



调用函数除了通过 函数() 这种形式外，还可以通过其他的方式来调用函数
比如，我们可以通过调用函数的 `call()` 和 `apply()` 来个方法来调用函数

```javascript
函数.call()
函数.apply()
```

`call` 和 `apply` 除了可以调用函数，还可以用来指定函数中的 `this`
`call` 和 `apply` 的第一个参数，将会成为函数的 `this`
通过 `call` 方法调用函数，函数的实参直接在第一个参数后一个一个的列出来
通过 `apply` 方法调用函数，函数的实参需要通过一个数组传递

```javascript
fn.call(obj)
fn.apply(console)

function fn2(a, b) {
    console.log("a =", a, "b =", b, this)
}

fn2.call(obj, "hello", true)
fn2.apply(obj, ["hello", true])
```



```javascript
function fn(a, b, c) {
    console.log("fn执行了~~~~", this)
    console.log(a, b, c)
}

const obj = {name:"孙悟空"}
const newFn = fn.bind(obj, 10, 20, 30)
newFn()

const arrowFn = () => {
    console.log(this)
}
arrowFn.call(obj)
const newArrowFn = arrowFn.bind(obj)
newArrowFn()

class MyClass{
    fn = () => {
        console.log(this)
    }
}

const mc = new MyClass()
mc.fn.call(window)
```



