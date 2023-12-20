# javascript 内建对象



## 01 解构赋值

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚"]
let a = arr[0]
let b = arr[1]
let c = arr[2]
```

解构赋值

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚"]
let [a, b, c] = arr
```

声明同时解构

```javascript
let [d, e, f, g] = ["唐僧", "白骨精", "蜘蛛精", "玉兔精"]
console.log(d, e, f, g)
```

```javascript
let [d, e, f, g] = [1, 2, 3]
```

```javascript
let [d, e, f = 77, g = 10] = [1, 2, 3]
```

```javascript
let [d, e, f = 77, g = g] = [1, 2, 3]
```

解构数组时，可以使用`...`来设置获取多余的元素

```javascript
let [n1, n2, ...n3] = [4, 5, 6, 7]
console.log(n1, n2, n3)
```

解构返回值

```javascript
function fn() {
    return ["二郎神", "猪八戒"]
}

let [name1, name2] = fn()
```



可以通过解构赋值来快速交换两个变量的值

```javascript
let a1 = 10
let a2 = 20

// let temp = a1
// a1 = a2
// a2 = temp

;[a1, a2] = [a2, a1] // [20, 10]

const arr2 = ["孙悟空", "猪八戒"]
console.log(arr2)

;[arr2[0], arr2[1]] = [arr2[1], arr2[0]]
console.log(arr2)
console.log("a1 =", a1)
console.log("a2 =", a2)
```



## 02 对象的解构

声明变量同时解构对象

```javascript
const obj = { name: "孙悟空", age: 18, gender: "男" }
let { name, age, gender } = obj
```



没有的属性返回undefined

```javascript
const obj = { name: "孙悟空", age: 18, gender: "男" }
let name, age, gender
;({ name, age, gender } = obj)
let { address } = obj
console.log(name, age, gender)
```



```javascript
const obj = { name: "孙悟空", age: 18, gender: "男" }
let {name:a, age:b, gender:c, address:d="花果山"} = obj
console.log(a, b, c, d)
```



## 03 对象的序列化

概念

JS中的对象使用时都是存在于计算机的内存中的
序列化指将对象转换为一个可以存储的格式
在JS中对象的序列化通常是将一个对象转换为字符串（JSON字符串）
序列化的用途（对象转换为字符串有什么用）：
对象转换为字符串后，可以将字符串在不同的语言之间进行传递
甚至人可以直接对字符串进行读写操作，使得JS对象可以不同的语言之间传递



用途

1 作为数据交换的格式
2 用来编写配置文字



如何进行序列化：
在JS中有一个工具类 JSON （JavaScript Object Notation） JS对象表示法
JS对象序列化后会转换为一个字符串，这个字符串我们称其为JSON字符串

也可以手动的编写JSON字符串，在很多程序的配置文件就是使用JSON编写的



编写JSON的注意事项：

1 JSON字符串有两种类型：
JSON对象 {}
JSON数组 []

2 JSON字符串的属性名必须使用双引号引起来

3 JSON中可以使用的属性值（元素）
数字（Number）
字符串（String） 必须使用双引号
布尔值（Boolean）
空值（Null）
对象（Object {}）
数组（Array []）

4 JSON的格式和JS对象的格式基本上一致的，
注意：JSON字符串如果属性是最后一个，则不要再加



JSON.stringify() 可以将一个对象转换为JSON字符串

JSON.parse() 可以将一个JSON格式的字符串转换为JS对象



```javascript
const obj = {
    name: "孙悟空",
    age: 18,
}

// 将obj转换为JSON字符串
const str = JSON.stringify(obj)
// 将JSON字符串转换为obj
const obj2 = JSON.parse(str)

console.log(obj)
console.log(str) // {"name":"孙悟空","age":18}
console.log(obj2)

const str2 = `{"name":"猪八戒","age":28}`
const str3 = "{}"
const str4 = '["hello", true, []]'

console.log(str2)
```



## 04 深复制

```javascript
const obj = {
    name: "孙悟空",
    friend: {
        name: "猪八戒",
    },
}

// 对obj进行浅复制
const obj2 = Object.assign({}, obj)

// 对obj进行深复制
const obj3 = structuredClone(obj)

// 利用JSON来完成深复制
const str = JSON.stringify(obj)
const obj4 = JSON.parse(str)

const obj5 = JSON.parse(JSON.stringify(obj))
```



## 05 Map

Map用来存储键值对结构的数据（key-value）
Object中存储的数据就可以认为是一种键值对结构
Map和Object的主要区别：
Object中的属性名只能是字符串或符号，如果传递了一个其他类型的属性名，
JS解释器会自动将其转换为字符串
Map中任何类型的值都可以称为数据的key

```javascript
const obj2 = {}

const obj = {
    "name":"孙悟空",
    'age':18,
    [Symbol()]:"哈哈",
    [obj2]:"嘻嘻"
}

console.log(obj)
```



创建：
new Map()

属性和方法：
map.size() 获取map中键值对的数量
map.set(key, value) 向map中添加键值对
map.get(key) 根据key获取值
map.delete(key) 删除指定数据
map.has(key) 检查map中是否包含指定键
map.clear() 删除全部的键值对

```javascript
// 创建一个Map
const map = new Map()

map.set("name", "孙悟空")
map.set(obj2, "呵呵")
map.set(NaN, "哈哈哈")

map.delete(NaN)
// map.clear()

console.log(map)
console.log(map.get("name"))
console.log(map.has("name"))
```



```javascript
const map = new Map()

map.set("name", "孙悟空")
map.set("age", 18)
map.set({}, "呵呵")

// 将map转换为数组
const arr = Array.from(map) // [["name","孙悟空"],["age",18]]
const arr = [...map]

console.log(arr)

const map2 = new Map([
    ["name", "猪八戒"],
    ["age", 18],
    [{}, () => {}],
])

console.log(map2)

// 遍历map
for (const [key, value] of map) {
    // const [key, value] = entry
    console.log(key, value)
}

map.forEach((key, value)=>{
    console.log(key, value)
})

/* 
    map.keys() - 获取map的所有的key
    map.values() - 获取map的所有的value
*/

for(const key of map.keys()){
    console.log(key)
}
```



## 06 Set

Set用来创建一个集合
它的功能和数组类似，不同点在于Set中不能存储重复的数据

使用方式：
创建
new Set()
new Set([...])

方法
size 获取数量
add() 添加元素
has() 检查元素
delete() 删除元素

```javascript
// 创建一个Set
const set = new Set()

// 向set中添加数据
set.add(10)
set.add("孙悟空")
set.add(10)

console.log(set)

for(const item of set){
    console.log(item)
}

const arr = [...set]

console.log(arr)

const arr2 = [1,2,3,2,1,3,4,5,4,6,7,7,8,9,10]
const set2 = new Set(arr2)

console.log([...set2])
```



## 07 Math

Math一个工具类
Math中为我们提供了数学运算相关的一些常量和方法
常量：
Math.PI 圆周率
方法：
Math.abs() 求一个数的绝对值
Math.min() 求多个值中的最小值
Math.max() 求多个值中的最大值
Math.pow() 求x的y次幂
Math.sqrt() 求一个数的平方根

Math.floor() 向下取整
Math.ceil() 向上取整
Math.round() 四舍五入取整
Math.trunc() 直接去除小数位

Math.random() 生成一个0-1之间的随机数

```javascript
console.log(Math.PI)

let result = Math.abs(10)
result = Math.abs(-10)

result = Math.min(10, 20, 30, 44, 55, -1)
result = Math.max(10, 20, 30, 44, 55, -1)
result = Math.pow(4, 2) // 4 ** 2
result = Math.sqrt(4) // 4 ** .5

result = Math.floor(1.2)
result = Math.ceil(1.2)
result = Math.round(1.4)
result = Math.trunc(1.5)
```



生成0-5之间的随机数
Math.random() --> 0 - 1
生成 0-x之间的随机数：
Math.round(Math.random() * x)
Math.floor(Math.random() * (x + 1))

生成 x-y 之间的随机数
Math.round(Math.random() * (y-x) + x)

```javascript
for (let i = 0; i < 50; i++) {
    result = Math.round(Math.random() * 5)
    result = Math.floor(Math.random() * 6)

    // 1-6
    result = Math.round(Math.random() * 5 + 1)

    // 11 - 20
    result = Math.round(Math.random() * 9 + 11)

    console.log(result)
}
```



## 08 Date

 在JS中所有的和时间相关的数据都由Date对象来表示
 对象的方法：
getFullYear() 获取4位年份
getMonth() 返当前日期的月份（0-11）
getDate() 返回当前是几日
getDay() 返回当前日期是周几（0-6） 0表示周日
......

getTime() 返回当前日期对象的时间戳
时间戳：自1970年1月1日0时0分0秒到当前时间所经历的毫秒数
计算机底层存储时间时，使用都是时间戳
Date.now() 获取当前的时间戳

```javascript
let d = new Date() // 直接通过new Date()创建时间对象时，它创建的是当前的时间的对象

// 可以在Date()的构造函数中，传递一个表示时间的字符串
// 字符串的格式：月/日/年 时:分:秒
// 年-月-日T时:分:秒
d = new Date("2019-12-23T23:34:35")

// new Date(年份, 月, 日, 时, 分, 秒, 毫秒)
d = new Date(2016, 0, 1, 13, 45, 33)
d = new Date()

result = d.getFullYear()
result = d.getMonth()
result = d.getDate()
result = d.getDay()
result = d.getTime()

console.log(result) // 1659088108520 毫秒
```



toLocaleString()
可以将一个日期转换为本地时间格式的字符串
参数：

1 描述语言和国家信息的字符串
zh-CN 中文中国
zh-HK 中文香港
en-US 英文美国

2 需要一个对象作为参数，在对象中可以通过对象的属性来对日期的格式进行配置
dateStyle 日期的风格
timeStyle 时间的风格
  full
  long
  medium
  short
hour12 是否采用12小时值
  true
  false
weekday 星期的显示方式
  long
  short
  narrow

year
  numeric
  2-digit

```javascript
result = d.toLocaleString("zh-CN", {
    year: "numeric",
    month: "long",
    day: "2-digit",
    weekday: "short",
})

console.log(result)
```



## 09 包装类

在JS中，除了直接创建原始值外，也可以创建原始值的对象

通过 new String() 可以创建String类型的对象
通过 new Number() 可以创建Number类型的对象
通过 new Boolean() 可以创建Boolean类型的对象
但是千万不要这么做



包装类：
JS中一共有5个包装类
String --> 字符串包装为String对象
Number --> 数值包装为Number对象
Boolean --> 布尔值包装为Boolean对象
BigInt --> 大整数包装为BigInt对象
Symbol --> 符号包装为Symbol对象



通过包装类可以将一个原始值包装为一个对象，
当我们对一个原始值调用方法或属性时，JS解释器会临时将原始值包装为对应的对象
然后调用这个对象的属性或方法

由于原始值会被临时转换为对应的对象，这就意味着对象中的方法都可以直接通过原始值来调用

```javascript
let str = new String("hello")
let num = new Number(11)
let bool = new Boolean(true)
let bool2 = new Boolean(true)

alert(bool == bool2)	// false

let str = "hello"
str.name = "哈哈"

let num = 11
num = num.toString()
// null.toString()
console.log(num)
```



## 10 字符串

https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String

字符串：
字符串其本质就是一个字符数组
"hello" --> ["h", "e", "l", "l", "o"]



字符串的很多方法都和数组是非常类似的属性和方法：
length 获取字符串的长度
`字符串[索引]` 获取指定位置的字符

```javascript
let str = "hello"
console.log(str[0])
```



### str.at()

根据索引获取字符，可以接受负索引 （实验方法）

```javascript
let str = "hello"
console.log(str.at(0))
```



### str.charAt()

根据索引获取字符

```javascript
let str = "hello"
console.log(str.charAt(0))
```



### str.concat()

用来连接两个或多个字符串

```javascript
const str1 = 'Hello';
const str2 = 'World';

console.log(str1.concat(' ', str2));
// Expected output: "Hello World"

console.log(str2.concat(', ', str1));
// Expected output: "World, Hello"
```



### str.includes()

用来检查字符串中是否包含某个内容
有返回true
没有返回false

```javascript
const str = "hello hello how are you"
console.log(str.includes("how", 13))
```



### str.indexOf()

```javascript
const paragraph = 'The quick brown fox jumps over the lazy dog. If the dog barked, was it really lazy?';

const searchTerm = 'dog';
const indexOfFirst = paragraph.indexOf(searchTerm);

console.log(`The index of the first "${searchTerm}" from the beginning is ${indexOfFirst}`);
// Expected output: "The index of the first "dog" from the beginning is 40"

console.log(`The index of the 2nd "${searchTerm}" is ${paragraph.indexOf(searchTerm, indexOfFirst + 1)}`);
// Expected output: "The index of the 2nd "dog" is 52"
```



### str.lastIndexOf()

```javascript
const paragraph = 'The quick brown fox jumps over the lazy dog. If the dog barked, was it really lazy?';

const searchTerm = 'dog';

console.log(`The index of the first "${searchTerm}" from the end is ${paragraph.lastIndexOf(searchTerm)}`);
// Expected output: "The index of the first "dog" from the end is 52"
```



### str.startsWith()

检查一个字符串是否以指定内容开头

```javascript
let str = "hello hello how are you"
console.log(str.startsWith("hello"))
```



### str.endsWith()

检查一个字符串是否以指定内容结尾

```javascript
let str = "hello hello how are you"
console.log(str.endsWith("you"))
```



### str.padStart()

```javascript
let str = "123"
console.log(str.padStart(7, "0")) // 0000123

const str1 = '5';

console.log(str1.padStart(2, '0'));
// Expected output: "05"

const fullNumber = '2034399002125581';
const last4Digits = fullNumber.slice(-4);
const maskedNumber = last4Digits.padStart(fullNumber.length, '*');

console.log(maskedNumber);
// Expected output: "************5581"
```



### str.padEnd()

通过添加指定的内容，使字符串保持某个长度

```javascript
let str = "123"
console.log(str.padEnd(7, "0")) // 1230000

const str1 = 'Breaded Mushrooms';

console.log(str1.padEnd(25, '.'));
// Expected output: "Breaded Mushrooms........"

const str2 = '200';

console.log(str2.padEnd(5));
// Expected output: "200  "
```



### str.replace()

使用一个新字符串替换一个指定内容

```javascript
let str = "hello hello how are you"
let result = str.replace("hello", "abc")
console.log(result)
```



### str.replaceAll()

使用一个新字符串替换所有指定内容

```javascript
let str = "hello hello how are you"
let result = str.replaceAll("hello", "abc")
console.log(result)
```



### str.slice()

对字符串进行切片

```javascript
let str = "hello hello how are you"
let result = str.slice(12, 15)
console.log(result) // how
```



### str.substring()

截取字符串

```javascript
let str = "hello hello how are you"
let result = str.substring(12, 15)
console.log(result) // how

result = str.substring(15, 12)
console.log(result) // how
```



### str.split()

用来将一个字符串拆分为一个数组

```javascript
let str = "abc@bcd@efg@jqk" // ["abc", "bcd", "efg", "jqk"]
let result = str.split("@")
console.log(result)
```



### str.toLowerCase()

将字符串转换为小写

```javascript
let str = "abcdABCD"
let result = str.toLowerCase()
console.log(result)
```



### str.toUpperCase()

将字符串转换为大写

```javascript
let str = "abcdABCD"
let result = str.toUpperCase()
console.log(result)
```



### str.trim()

去除前后空格

```javascript
let str = "    ab  c     "
let result = str.trim()
console.log(result)
```



### str.trimStart()

去除开始空格

```javascript
let str = "    ab  c     "
let result = str.trimStart()
console.log(result)
```



### str.trimEnd()

去除结束空格

```javascript
let str = "    ab  c     "
let result = str.trimEnd()
console.log(result)
```



### split()

可以根据正则表达式来对一个字符串进行拆分

```javascript
let str = "a@b@c@d"
let result = str.split("@")

str = "孙悟空abc猪八戒adc沙和尚"
result = str.split(/a[bd]c/)
```



### search()

可以去搜索符合正则表达式的内容第一次在字符串中出现的位置

```javascript
let str = "dajsdh13715678903jasdlakdkjg13457890657djashdjka13811678908sdadadasd"
let result = str.search("abc")
result = str.search(/1[3-9]\d{9}/)
```



### replace()

根据正则表达式替换字符串中的指定内容

```javascript
let str = "dajsdh13715678903jasdlakdkjg13457890657djashdjka13811678908sdadadasd"
let result = str.replace(/1[3-9]\d{9}/g, "哈哈哈")
```



### match()

根据正则表达式去匹配字符串中符合要求的内容

```
let str = "dajsdh13715678903jasdlakdkjg13457890657djashdjka13811678908sdadadasd"
let result = str.match(/1[3-9]\d{9}/g)
```



### matchAll()

根据正则表达式去匹配字符串中符合要求的内容(必须设置g 全局匹配)
它返回的是一个迭代器

```
let str = "dajsdh13715678903jasdlakdkjg13457890657djashdjka13811678908sdadadasd"
let result = str.matchAll(/1[3-9](\d{9})/g)
```



## 11 正则表达式

正则表达式
正则表达式用来定义一个规则
通过这个规则计算机可以检查一个字符串是否符合规则
或者将字符串中符合规则的内容提取出来
正则表达式也是JS中的一个对象，
所以要使用正则表达式，需要先创建正则表达式的对象



### 创建

```javascript
// new RegExp() 可以接收两个参数（字符串） 1.正则表达式 2.匹配模式
let reg = new RegExp("a", "i") // 通过构造函数来创建一个正则表达式的对象

// 使用字面量来创建正则表达式：/正则/匹配模式
reg = /a/i
reg = /\w/

reg = new RegExp("\\w")
console.log(reg)
```



### 测试

```javascript


reg = new RegExp("a") // /a/ 表示，检查一个字符串中是否有a

// 通过正则表达式检查一个字符串是否符合规则
let str = "a"

let result = reg.test(str) // true

result = reg.test("b") // false
result = reg.test("abc") // true
result = reg.test("bcabc") // true

reg = /a/

console.log(result);
```



### 语法

1 在正则表达式中大部分字符都可以直接写

2 `|` 在正则表达式中表示或

3 `[]` 表示或（字符集）
  `[a-z]` 任意的小写字母
  `[A-Z]` 任意的大写字母
  `[a-zA-Z]` 任意的字母
  `[0-9]`任意数字

4 `[^]` 表示除了
`[^x]` 除了x

5 `.` 表示除了换行外的任意字符

6 在正则表达式中使用`\`作为转义字符

7 其他的字符集
`\w` 任意的单词字符 `[A-Za-z0-9_]`
`\W` 除了单词字符 `[^A-Za-z0-9_]`
`\d` 任意数字 `[0-9]`
`\D` 除了数字 `[^0-9]`
`\s` 空格
`\S` 除了空格
`\b` 单词边界
`\B` 除了单词边界

8 开头和结尾
`^` 表示字符串的开头
`$` 表示字符串的结尾



```javascript
let re = /abc|bcd/
re = /[a-z]/
re = /[A-Z]/
re = /[A-Za-z]/
re = /[a-z]/i // 匹配模式i表示忽略大小写
re = /[^a-z]/ // 匹配包含小写字母以外内容的字符串
re = /./
re = /\./
re = /\w/
re = /^a/ // 匹配开始位置的a
re = /a$/ // 匹配结束位置的a
re = /^a$/ // 只匹配字母a，完全匹配，要求字符串必须和正则完全一致
re = /^abc$/

let result = re.test('aa')
console.log(result)
```



### 量词

`{m}` 正好m个
`{m,}` 至少m个, 大于等于m个
`{m,n}` m~n个, 大于等于m, 小于等于n

`+`  一个以上，相当于{1,}
`*`  任意数量 0~无穷
`?`  0-1次, 相当于`{0,1}`

```javascript
let re = /a{3}/
re = /^a{3}$/
re = /^(ab){3}$/
re = /^[a-z]{3}$/
re = /^[a-z]{1,}$/
re = /^[a-z]{1,3}$/
re = /^ba+$/
re = /^ba*$/
re = /^ba?$/
let result = re.test("baa")
console.log(result)
```



### 捕获

```javascript
re.exec()
```

获取字符串中符合正则表达式的内容

```javascript
let str = "abcaecafcacc"

// 提取出str中符合axc格式的内容

// g表示全局匹配
let re = /a(([a-z])c)/ig

let result = re.exec(str)
console.log(result)

while(result){
    // result[0] 正则表达式整体匹配的字符串内容
    // result[1] 正则表达式整体匹配的字符串内容中, 正则表达式中第一个括号匹配的内容, 从左往右数 '(' 以及匹配的 ')' 组成的括号
    // result[2] 第二个括号匹配的内容
    console.log(result[0], result[1], result[2])
    // 反复调用会向字符串中上一次匹配的内容后面继续寻找匹配正则表达式的部分, 直到字符串结束则返回null
    result = re.exec(str)
}
```



练习:

在字符串中找出电话号码

dajsdh13715678903jasdlakdkjg13457890657djashdjka13811678908sdadadasd

用自己的语言来把描述出来
1    3         501789087
1    3到9之间   任意数字 x 9

```javascript
let re = /1[3-9]\d{9}/g
re = /(1[3-9]\d)\d{4}(\d{4})/g

let str = "dajsdh13715678903jasdlakdkjg13457890657djashdjka13811678908sdadadasd"
let result

while (result = re.exec(str)) {
    console.log(result[0], result[1], result[2])
    console.log(result[1]+"****"+result[2])
}

re = /^1[3-9]\d{9}$/
console.log(re.test("13456789042"))
```



## 12 垃圾回收

垃圾回收（Garbage collection）
和生活一样，生活时间长了以后会产生生活垃圾, 程序运行一段时间后也会产生垃圾.



在程序的世界中，什么是垃圾？
如果一个对象没有任何的变量对其进行引用，那么这个对象就是一个垃圾



垃圾对象的存在，会严重的影响程序的性能
在JS中有自动的垃圾回收机制，这些垃圾对象会被解释器自动回收，我们无需手动处理
对于垃圾回收来说，我们唯一能做的事情就是将不再使用的变量设置为null



```javascript
let obj = {name:"孙悟空"}
let obj2 = obj

obj = null
obj2 = null
```



