# javascript 数组



## 01 简介

数组（Array）
数组也是一种复合数据类型，在数组可以存储多个不同类型的数据
数组中存储的是有序的数据，数组中的每个数据都有一个唯一的索引
可以通过索引来操作获取数据
数组中存储的数据叫做元素
索引（index）是一组大于0的整数
创建数组
通过Array()来创建数组，也可以通过[]来创建数组

向数组中添加元素
语法：
数组[索引] = 元素

读取数组中的元素
语法：
数组[索引]
如果读取了一个不存在的元素，不好报错而是返回undefined

length
获取数组的长度
获取的实际值就是数组的最大索引 + 1
向数组最后添加元素：
数组[数组.length] = 元素
length是可以修改的



```javascript
const obj = { name: "孙悟空", age: 18 }

const arr = new Array()
const arr2 = [1, 2, 3, 4, 5] // 数组字面量

arr[0] = 10
arr[1] = 22
arr[2] = 44
arr[3] = 88
arr[4] = 99

// 使用数组时，应该避免非连续数组，因为它性能不好
arr[100] = 99

console.log(arr[1])
console.log(typeof arr) // object
console.log(arr.length)

arr[arr.length] = 33
arr[arr.length] = 55

arr.length = 5

console.log(arr)
```



## 02 遍历数组

遍历数组简单理解，就是获取到数组中的每一个元素

```javascript
// 任何类型的值都可以成为数组中的元素
let arr = [1, "hello", true, null, { name: "孙悟空" }, () => {}]

// 创建数组时尽量要确保数组中存储的数据的类型是相同
arr = ["孙悟空", "猪八戒", "沙和尚"]

console.log(arr)

console.log(arr[0])
console.log(arr[1])
console.log(arr[2])

arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧", "白骨精"]

for(let i=0; i<arr.length; i++){
    console.log(arr[i])
}

for (let i = arr.length - 1; i >= 0; i--) {
    console.log(arr[i])
}
```



定义一个Person类，类中有两个属性name和age
然后创建几个Person对象，将其添加到一个数组中

遍历数组，并打印未成年人的信息

```javascript
class Person {
    constructor(name, age) {
        this.name = name
        this.age = age
    }
}

const personArr = [
    new Person("孙悟空", 18),
    new Person("沙和尚", 38),
    new Person("红孩儿", 8),
]

for(let i=0; i<personArr.length; i++){
    if(personArr[i].age < 18){
        console.log(personArr[i])
    }
}
```



## 03 for-of 语句

`for-of` 语句可以用来遍历可迭代对象

语法：

```javascript
for(变量 of 可迭代的对象){
  语句...
}
```

执行流程：
for-of的循环体会执行多次，数组中有几个元素就会执行几次，
每次执行时都会将一个元素赋值给变量

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]

for(let value of arr){
    console.log(value)
}


for(let value of "hello"){
    console.log(value)
}
```



## 04 数组的方法

https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Array

### Array.isArray()

用来检查一个对象是否是数组    

```javascript
console.log(Array.isArray({ name: "孙悟空" })) // false
console.log(Array.isArray([1, 2, 3])) // true
```



### at()

可以根据索引获取数组中的指定元素
at可以接收负索引作为参数

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]
console.log(arr.at(-2))
console.log(arr[arr.length - 2])
```



### concat()

用来连接两个或多个数组
非破坏性方法，不会影响原数组，而是返回一个新的数组

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]
const arr2 = ["白骨精", "蜘蛛精", "玉兔精"]
let result = arr.concat(arr2, ["牛魔王","铁扇公主"])
console.log(result)
```



### indexOf()

获取元素在数组中第一次出现的索引
 参数：
1 要查询的元素
2 查询的其实位置

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]
let result = arr.indexOf("沙和尚", 3)
result = arr.indexOf("白骨精")
```



### lastIndexOf()

 获取元素在数组中最后一次出现的位置

 返回值：
找到了则返回元素的索引，
没有找到返回-1

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]
let result = arr.lastIndexOf("沙和尚", 3)
```



### join()

 将一个数组中的元素连接为一个字符串
 ["孙悟空", "猪八戒", "沙和尚", "唐僧", "沙和尚"] -> "孙悟空,猪八戒,沙和尚,唐僧,沙和尚"
 参数：
指定一个字符串作为连接符

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]
let result = arr.join()
result = arr.join("@-@")
result = arr.join("")
```



### slice()

 用来截取数组（非破坏性方法）
 参数：
1 截取的起始位置（包括该位置）
2 截取的结束位置（不包括该位置）
 第二个参数可以省略不写，如果省略则会一直截取到最后
 索引可以是负值

如果将两个参数全都省略，则可以对数组进行浅拷贝（浅复制）

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]
result = arr.slice(0, 2)
result = arr.slice(1, 3)
result = arr.slice(1, -1)
result = arr.slice()

console.log(result)
```



### push()

向数组的末尾添加一个或多个元素，并返回新的长度

```javascript
let arr = ["孙悟空", "猪八戒", "沙和尚"]
let result = arr.push("唐僧", "白骨精")
console.log(arr, result)
```



### pop()

删除并返回数组的最后一个元素

```javascript
let arr = ["孙悟空", "猪八戒", "沙和尚"]
let result = arr.pop()
console.log(arr, result)
```



### unshift()

向数组的开头添加一个或多个元素，并返回新的长度

```javascript
let arr = ["孙悟空", "猪八戒", "沙和尚"]
let result = arr.unshift("牛魔王")
console.log(arr, result)
```



### shift()

删除并返回数组的第一个元素

```javascript
let arr = ["孙悟空", "猪八戒", "沙和尚"]
let result = arr.shift()
console.log(arr, result)
```



### splice()

可以删除、插入、替换数组中的元素
参数：
1 删除的起始位置
2 删除的数量
3 要插入的元素

返回值：
返回被删除的元素

```javascript
let = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]
let result = arr.splice(1, 3)
result = arr.splice(1, 1, "牛魔王", "铁扇公主", "红孩儿")
result = arr.splice(1, 0, "牛魔王", "铁扇公主", "红孩儿")

console.log(result)
console.log(arr)
```



### reverse()

反转数组

```javascript
let arr = ["a", "b", "c", "d"]
arr.reverse()

console.log(arr)
```



### sort()

sort用来对数组进行排序（会对改变原数组）
sort默认会将数组升序排列
注意：sort默认会按照Unicode编码进行排序，所以如果直接通过sort对数字进行排序
可能会得到一个不正确的结果
参数：
可以传递一个回调函数作为参数，通过回调函数来指定排序规则
(a, b) => a - b 升序排列
(a, b) => b - a 降序排列

```javascript
// let arr = ["a", "c", "e", "f", "d", "b"]
let arr = [2, 3, 1, 9, 0, 4, 5, 7, 8, 6, 10]
console.log(arr)

arr.sort()
console.log(arr)

arr.sort((a, b) => a - b)
console.log(arr)

arr.sort((a, b) => b - a)
console.log(arr)
```



### forEach()

用来遍历数组
它需要一个回调函数作为参数，这个回调函数会被调用多次
数组中有几个元素，回调函数就会调用几次
每次调用，都会将数组中的数据作为参数传递
回调函数中有三个参数：
element 当前的元素
index 当前元素的索引
array 被遍历的数组

```javascript
let arr = ["孙悟空", "猪八戒", "沙和尚", "唐僧"]

arr.forEach((element, index, array) => {
    console.log(array)
})

arr.forEach((element, index) => console.log(index, element))
```



### filter()

将数组中符合条件的元素保存到一个新数组中返回
需要一个回调函数作为参数，会为每一个元素去调用回调函数，并根据返回值来决定是否将元素添加到新数组中
非破坏性方法，不会影响原数组

```javascript
let arr = [1, 2, 3, 4, 5, 6, 7, 8]

// 获取数组中的所有偶数
let result = arr.filter((ele) => ele > 5)
console.log(result)
```



### map()

根据当前数组生成一个新数组
需要一个回调函数作为参数，
回调函数的返回值会成为新数组中的元素
非破坏性方法不会影响原数组

```javascript
let arr = [1, 2, 3, 4, 5, 6, 7, 8]
let result = arr.map((ele) => ele * 2)
console.log(result)
```

```javascript
let arr = ["孙悟空", "猪八戒", "沙和尚"]
let result = arr.map((ele) => "<li>" + ele + "</li>")
console.log(result)
```



### reduce()

可以用来将一个数组中的所有元素整合为一个值
参数：
1 回调函数，通过回调函数来指定合并的规则
2 可选参数，初始值

```javascript
let arr = [1, 2, 3, 4, 5, 6, 7, 8]

let result = arr.reduce((a, b) => {
    /*
        1, 2
        3, 3
        6, 4
        10, 5
    */
    console.log(a, b)
    return a * b
})

// 初始值为 10
result = arr.reduce((result, item) => result + item, 10)
console.log(result)
```



## 05 数组的复制

```
const arr = ["孙悟空", "猪八戒", "沙和尚"]

const arr2 = arr  // 不是复制
arr2[0] = "唐僧"

// 如何去复制一个对象 复制必须要产生新的对象
// 当调用slice时，会产生一个新的数组对象，从而完成对数组的复制
const arr3 = arr.slice()


console.log(arr === arr2)
console.log(arr2)

arr3[0] = "唐僧"

console.log(arr)
console.log(arr3)
```



## 06 浅拷贝和深拷贝

浅拷贝（shallow copy）
通常对对象的拷贝都是浅拷贝
浅拷贝顾名思义，只对对象的浅层进行复制（只复制一层）
如果对象中存储的数据是原始值，那么拷贝的深浅是不重要
浅拷贝只会对对象本身进行复制，不会复制对象中的属性（或元素）



深拷贝（deep copy）
深拷贝指不仅复制对象本身，还复制对象中的属性和元素
因为性能问题，通常情况不太使用深拷贝

```javascript
// 创建一个数组
const arr = [{name:"孙悟空"}, {name:"猪八戒"}]
const arr2 = arr.slice() // 浅拷贝

const arr3 = structuredClone(arr) // 专门用来深拷贝的方法

console.log(arr)
console.log(arr3)
```



## 07 对象的复制

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚"]
const arr2 = arr.slice()

console.log(arr === arr2) // false
```



展开运算符 `...`
可以将一个数组中的元素展开到另一个数组中或者作为函数的参数传递
通过它也可以对数组进行浅复制

```javascript
const arr = ["孙悟空", "猪八戒", "沙和尚"]

const arr3 = [arr[0], arr[1], arr[2]]
const arr3 = [...arr]
const arr3 = ["唐僧", ...arr, "白骨精"]

console.log(arr)
console.log(arr3)
```

```javascript
function sum(a, b, c) {
    return a + b + c
}

const arr4 = [10, 20, 30]

let result = sum(arr4[0], arr4[1], arr4[2])
result = sum(...arr4)
console.log(result)
```



对象的复制
`Object.assign`(目标对象, 被复制的对象)
将被复制对象中的属性复制到目标对象里，并将目标对象返回

也可以使用展开运算符对对象进行复制

```javascript
const obj = { name: "孙悟空", age: 18 }

const obj2 = Object.assign({}, obj)
const obj2 = { address: "花果山", age: 28 }

Object.assign(obj2, obj)
console.log(obj2)

const obj3 = { address: "高老庄", ...obj, age: 48 } // 将obj中的属性在新对象中展开
```



## 08 高维数组

数组中可以存储任意类型的数据，也可以存数组,
如果一个数组中的元素还是数组，则这个数组我们就称为是二维数组

```javascript
const arr3 = [["孙悟空", 18, "男"], ["猪八戒", 28, "男"]]

for (let stu of arr3) {
    for (let v of stu) {
        console.log(v)
    }
}

let [[name, age, gender], obj] = arr3

console.log(name, age, gender)
console.log(obj)
```



## 练习

### 去重

编写代码去除数组中重复的元素

```javascript
const arr = [1, 2, 1, 3, 2, 2, 4, 5, 5, 6, 7]

// 编写代码去除数组中重复的元素

// 分别获取数组中的元素
for (let i = 0; i < arr.length; i++) {
    // 获取当前值后边的所有值
    for (let j = i + 1; j < arr.length; j++) {
        // 判断两个数是否相等
        if (arr[i] === arr[j]) {
            // 出现了重复元素，删除后边的元素
            arr.splice(j, 1)

            /* 
                当arr[i] 和 arr[j]相同时，它会自动的删除j位置的元素，然后j+1位置的元素，会变成j位置的元素
                而j位置已经比较过了，不会重复比较，所以会出现漏比较的情况

                解决办法，当删除一个元素后，需要将该位置的元素在比较一遍
            */
            j--
        }
    }
}

console.log(arr)
```

```javascript
const arr = [1, 2, 1, 3, 2, 2, 4, 5, 5, 6, 7]
const newArr = []

for(let ele of arr){
    if(newArr.indexOf(ele) === -1){
        newArr.push(ele)
    }
}

console.log(newArr)
```



### 排序

有一个数组：
[9,1,3,2,8,0,5,7,6,4]

编写代码对数组进行排序 --> 0, 1, 2, 3, 4, 5, 6, 7, 8, 9



#### 冒泡排序

思路1：
9, 1, 3, 2, 8, 0, 5, 7, 6, 4
 比较相邻的两个元素，然后根据大小来决定是否交换它们的位置
 例子：
第一次排序：1, 3, 2, 8, 0, 5, 7, 6, 4, 9
第二次排序：1, 2, 3, 0, 5, 7, 6, 4, 8, 9
第三次排序：1, 2, 0, 3, 5, 6, 4, 7, 8, 9
...
倒数第二次 0, 1, 2, 3, 4, 5, 6, 7, 8, 9

 这种排序方式，被称为冒泡排序，冒泡排序是最慢的排序方式，
数字少还可以凑合用，不适用于数据量较大的排序

```javascript
const arr = [9, 1, 3, 2, 8, 0, 5, 7, 6, 4]
for (let j = 0; j < arr.length - 1; j++) {
    for (let i = 0; i < arr.length - 1; i++) {
        // arr[i] 前边的元素 arr[i+1] 后边元素
        if (arr[i] < arr[i + 1]) {
            // 大数在前，小数在后，需要交换两个元素的位置
            let temp = arr[i] // 临时变量用来存储arr[i]的值
            arr[i] = arr[i + 1] // 将arr[i+1]的值赋给arr[i]
            arr[i + 1] = temp // 修改arr[i+1]的值
        }
    }
}

console.log(arr)
```



#### 选择排序

思路2：
9, 1, 3, 2, 8, 0, 5, 7, 6, 4
 取出一个元素，然后将其他元素和该元素进行比较，如果其他元素比该元素小则交换两个元素的位置
 例子：
0, 9, 3, 2, 8, 1, 5, 7, 6, 4
0, 1, 9, 3, 8, 2, 5, 7, 6, 4
0, 1, 2, 9, 8, 3, 5, 7, 6, 4
...

 选择排序

```javascript
const arr = [9, 1, 3, 2, 8, 0, 5, 7, 6, 4]
console.log(arr)

for(let i=0; i<arr.length; i++){
    for(let j=i+1; j<arr.length; j++){
        if(arr[i] > arr[j]){
            // 交换两个元素的位置
            let temp = arr[i]
            arr[i] = arr[j]
            arr[j] = temp
        }
    }
}

console.log(arr)
```