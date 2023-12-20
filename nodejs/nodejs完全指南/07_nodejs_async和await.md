# async 和 await

通过async可以来创建一个异步函数
异步函数的返回值会自动封装到一个 Promise 中返回

在 `async` 声明的异步函数中可以使用 `await` 关键字来调用异步函数



函数返回 promise 对象

```javascript
function fn() {
    return Promise.resolve(10)
}
fn().then(r => {
    console.log(r)
})
```

输出: 10



使用 await

```javascript
async function fn2() {
    return 10
}
fn2().then(r => {
    console.log(r)
})
```

输出: 10



这两种实现方式是等价的, 



Promise解决了异步调用中回调函数问题，
虽然通过链式调用解决了回调地狱，但是链式调用太多以后还是不好看
想以同步的方式去调用异步的代码



当我们通过 await 去调用异步函数时，它会暂停代码的运行
直到异步代码执行有结果时，才会将结果返回



注意 await 只能用于 **async声明的异步函数** 中，或 **es模块的顶级作用域** 中
await 阻塞的只是异步函数内部的代码，不会影响外部代码
通过 await 调用异步代码时，需要通过 try-catch 来处理异常



es模块的顶级作用域 示例:

```html
<!DOCTYPE html>
<html lang="zh">
    <head>
        <meta charset="UTF-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <title>Document</title>
        <script type="module">
            await console.log(123)
        </script>
    </head>
    <body></body>
</html>
```

注意:  `<script type="module">` 以模块化的方式引入js代码, 这样就可以在这个脚本中 ( 顶级作用域中 ) 直接写 await



示例:

```javascript
function sum(a, b) {
    return new Promise(resolve => {
        setTimeout(() => {
            resolve(a + b)
        }, 500);
    })
}

function fn3() {
    sum(123, 456)
        .then(r => sum(r, 8))
        .then(r => sum(r, 9))
        .then(r => console.log("fn3 => ", r))
}

fn3()
console.log("333")

async function fn4() {
    try {
        let result = await sum(123, 456)
        result = await sum(result, 8)
        result = await sum(result, 9)
        console.log("fn4 => ", result)
    } catch (e) {
        console.log("出错了~~")
    }
}

fn4()
console.log("444")
```

输出:

```shell
333
444
fn3 =>  596
fn4 =>  596
```



如果async声明的函数中没有写await，那么它里边就会依次执行

```javascript
async function fn4(){
    console.log(1)
    console.log(2)
    console.log(3)
}

fn4()
```



等价于:

```javascript
function fn5(){
    return new Promise(resolve => {
        console.log(1)
        console.log(2)
        console.log(3)
        resolve()
    })
}

fn5()
```



当我们使用await调用函数后，当前函数后边的所有代码会在当前函数执行完毕后，被放入到微任务队里中

```javascript
async function fn4() {
    console.log(1)
    await console.log(2)
    // await后边的所有代码，都会放入到微任务队列中执行
    console.log(3)
}
```

等价于:

```javascript
function fn5() {
    return new Promise(resolve => {
        console.log(1)
        // 加了await
        console.log(2)

        resolve()
    }).then(r => {
        console.log(3)
    })
}
```



要在非模块化的js中使用 await 那么就必须要有 async

```javascript
async function fn6(){
    await console.log("哈哈")
}
fn6()
```

这样会声明一个单独的函数, 再调用函数. 如果不希望声明一个函数, 可以使用 **匿名立即执行函数** (注意前面手动加上 `;` )

```javascript
;(async () => {
    await console.log("哈哈")
})()
```

