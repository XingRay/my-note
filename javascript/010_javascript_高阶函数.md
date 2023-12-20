# javascript 高阶函数

如果一个函数的参数或返回值是函数，则这个函数就称为高阶函数
为什么要将函数作为参数传递？（回调函数有什么作用？）
将函数作为参数，意味着可以对另一个函数动态的传递代码



案例:

filter()函数用来对数组进行过滤

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
    new Person("白骨精", 16),
]

function filter(arr) {

    const newArr = []

    for (let i = 0; i < arr.length; i++) {
        if (arr[i].age < 18) {
            newArr.push(arr[i])
        }
    }

    return newArr
}

result = filter(personArr)
console.log(result)
```

目前我们的函数只能过滤出数组中age属性小于18的对象，
我们希望过滤更加灵活：
比如：过滤数组中age大于18的对象
age大于60的对象
age大于n的对象
过滤数组中name为xxx的对象
过滤数组中的偶数
...

一个函数的参数也可以是函数，
如果将函数作为参数传递，那么我们就称这个函数为回调函数（callback）

```javascript
function filter(arr, cb) {
    const newArr = []

    // console.log("-->", cb)
    // cb()

    for (let i = 0; i < arr.length; i++) {
        // arr[i].age >= 18
        // arr[i].age > 60
        // arr[i].age > n
        // arr[i].name === "xxx"
        // arr[i] % 2 === 0
        if (cb(arr[i])) {
            newArr.push(arr[i])
        }
    }

    return newArr
}

function fn(a){
    return a.name === "孙悟空"
}

result = filter(personArr, fn)
console.log(result)
```





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
    new Person("白骨精", 16),
]

function filter(arr, cb) {
    const newArr = []

    for (let i = 0; i < arr.length; i++) {
        if (cb(arr[i])) {
            newArr.push(arr[i])
        }
    }

    return newArr
}

// 我们这种定义回调函数的形式比较少见，通常回调函数都是匿名函数

function fn(a) {
    return a.name === "孙悟空"
}

result = filter(personArr, a => a.name === "孙悟空")
result = filter(personArr, a => a.age >= 18)

const arr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
result = filter(arr, a => a % 2 === 0)

console.log(result)
```



希望在 someFn() 函数执行时，可以记录一条日志
在不修改原函数的基础上，为其增加记录日志的功能
可以通过高阶函数，来动态的生成一个新函数

```javascript
function someFn() {
    return "hello"
}

function outer(cb){
    return () => {
        console.log("记录日志~~~~~")
        const result = cb()
        return result
    }
}

let result = outer(someFn)
// console.log(result)

function test(){
    console.log("test~~~~")
    return "test"
}

let newTest = outer(test)
newTest()
```



