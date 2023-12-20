# javascript 闭包

案例:

创建一个函数，第一次调用时打印1，第二次调用打印2，以此类推

```javascript
let num = 0

function fn(){
    num++
    console.log(num)
}

fn()
```

这里 num 可以被外部访问,  可以利用函数，来隐藏不希望被外部访问到的变量



## 概念

闭包就是能访问到外部函数作用域中变量的函数



什么时候使用：
当我们需要隐藏一些不希望被别人访问的内容时就可以使用闭包



构成闭包的要件：
1 函数的嵌套
2 内部函数要引用外部函数中的变量
3 内部函数要作为返回值返回



```javascript
function outer(){
    let num = 0 // 位于函数作用域中

    return () => {
        num++
        console.log(num)
    }
}

const newFn = outer()

// console.log(newFn)
newFn()
newFn()
newFn()
```



## 作用域

函数在作用域，在函数创建时就已经确定的（词法作用域）和调用的位置无关

闭包利用的就是词法作用域

```javascript
function fn(){
    console.log(a)
}


function fn2(){
    let a = "fn2中的a"

    fn()
}

fn2()


function fn3(){
    let a = "fn3中的a"

    function fn4(){
        console.log(a)
    }

    return fn4
}

let fn4 = fn3()

fn4()
```



## 生命周期

1 闭包在外部函数调用时产生，外部函数每次调用都会产生一个全新的闭包
2 在内部函数丢失时销毁（内部函数被垃圾回收了，闭包才会消失）

注意事项：
闭包主要用来隐藏一些不希望被外部访问的内容，
这就意味着闭包需要占用一定的内存空间

相较于类来说，闭包比较浪费内存空间（类可以使用原型而闭包不能），
需要执行次数较少时，使用闭包
需要大量创建实例时，使用类

```javascript
function outer(){
    let someVariable = "someValue"

    return function(){
        console.log(someVariable)
    }
}

function outer2(){
    let num = 0
    return () => {
        num++
        console.log(num)
    }
}

let fn1 = outer2() // 独立闭包
let fn2 = outer2() // 独立闭包

fn1()
fn2()

fn1 = null
fn2 = null
```

