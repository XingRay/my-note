# Promise

## 由来

异步调用必须要通过回调函数来返回数据，
当我们进行一些复杂的调用的时，会出现“回调地狱”

```javascript
function sum(a, b, cb) {
    setTimeout(() => {
        cb(a + b)
    }, 1000)

}


sum(123, 456, (result)=>{
    sum(result, 7, (result)=>{
        sum(result, 8, result => {
            sum(result, 9, result => {
                sum(result, 10, result => {
```java
                    console.log(result)
                })
            })
        })
    })
})
```

问题：
```
异步必须通过回调函数来返回结果，回调函数一多就很痛苦


Promise
Promise可以帮助我们解决异步中的回调函数的问题
Promise就是一个用来存储数据的容器
它拥有着一套特殊的存取数据的方式
这个方式使得它里边可以存储异步调用的结果


## 创建Promise

创建Promise时，构造函数中需要一个函数作为参数
Promise构造函数的回调函数，它会在创建Promise时调用，调用时会有两个参数传递进去

resolve 和 reject 是两个函数，通过这两个函数可以向Promise中存储数据
resolve在执行正常时存储数据，reject在执行错误时存储数据

通过函数来向Promise中添加数据，好处就是可以用来添加异步调用的数据

```javascript
const promise = new Promise((resolve, reject) => {
    // 执行正常
    resolve("resolve返回的数据")
})

```java
console.log(promise)
```

```shell
Promise {[[PromiseState]]: "fulfilled", [[PromiseResult]]: "resolve返回的数据", Symbol(async_id_symbol): 2, Symbol(trigger_async_id_symbol): 1}
```


```javascript
const promise = new Promise((resolve, reject) => {
    setTimeout(() => {
        resolve("哈哈")
    }, 1000)
})
console.log(promise)
```

```shell
Promise {[[PromiseState]]: "pending", [[PromiseResult]]: undefined, Symbol(async_id_symbol): 2, Symbol(trigger_async_id_symbol): 1}
```


```javascript
const promise = new Promise((resolve, reject) => {
    // 执行错误
    reject("reject返回的数据")
})
console.log(promise)
```

```javascript
Promise {[[PromiseState]]: "rejected", [[PromiseResult]]: "reject返回的数据", Symbol(async_id_symbol): 2, Symbol(trigger_async_id_symbol): 1}
```


```javascript
const promise = new Promise((resolve, reject) => {
    // 执行出错
```
    throw new Error("哈哈，出错了")
})
```java
console.log(promise)
```

```shell
```
Promise {[[PromiseState]]: "rejected", [[PromiseResult]]: Error: 哈哈，出错了\n at D:\code\study\front\test01\src\test.js:3:11\n at new Promise (<anonymous>)\n …, Symbol(async_id_symbol): 2, Symbol(trigger_async_id_symbol): 1}
```


## then()

可以通过Promise的实例方法`then`来读取Promise中存储的数据
then需要两个回调函数作为参数，回调函数用来获取Promise中的数据
通过resolve存储的数据，会调用第一个函数返回，
可以在第一个函数中编写处理数据的代码

通过reject存储的数据或者出现异常时，会调用第二个函数返回
可以在第二个函数中编写处理异常的代码

```javascript
promise.then((result) => {
```java
    console.log("1", result)
}, (reason) => {
    console.log("2", reason)
})
```


```
## PromiseResult 和 PromiseState

### PromiseResult

用来存储数据

### PromiseState

记录Promise的状态（三种状态）

| Promise的状态 | 含义   | 触发时机                     |
| ------------- | ------ | ---------------------------- |
| pending       | 进行中 | 初始值                       |
| fulfilled     | 完成   | 通过resolve存储数据时        |
| rejected      | 拒绝   | 出错了或通过reject存储数据时 |

**PromiseState 只能修改一次** ，修改以后永远不会在变


### 状态转换流程

当Promise创建时，PromiseState 初始值为pending，
当通过resolve存储数据时 PromiseState 变为fulfilled（完成）
PromiseResult 变为存储的数据

当通过 reject 存储数据或出错时 PromiseState 变为rejected（拒绝，出错了）
PromiseResult 变为存储的数据 或 异常对象


当我们通过then读取数据时，相当于为Promise设置了回调函数，
如果 PromiseState 变为fulfilled，则调用then的第一个回调函数来返回数据
如果 PromiseState 变为rejected，则调用then的第二个回调函数来返回数据


示例:

```javascript
const promise = new Promise((resolve, reject) => {
    resolve("哈哈")
})

```java
console.log(promise)
promise.then(result => {
    console.log(result)
}, reason => {
    console.log("出错了")
})
```


```
## catch()

catch() 用法和then类似，但是只需要一个回调函数作为参数
catch()中的回调函数只会在Promise被拒绝时才调用
catch() 相当于 then(null, reason => {})
catch() 就是一个专门处理Promise异常的方法

```javascript
const promise = new Promise((resolve, reject) => {
    reject("出错了")
})

```java
console.log(promise)

promise.catch(reason => {
    console.log(reason)    
    console.log(222222)
})
```


```
## finally()

无论是正常存储数据还是出现异常了，finally总会执行
但是finally的回调函数中不会接收到数据
finally()通常用来编写一些无论成功与否都要执行代码

```javascript
const promise = new Promise((resolve, reject) => {
    reject("出错了")
})

```java
console.log(promise)

promise
    .then(result => {
        console.log(result)
    }, reason => {
        console.log(reason)
    })
    .finally(() => {
        console.log("没有什么能够阻挡我执行的！")
    })
```


```
## 调用链

Promise就是一个用来存储数据对象
但是由于Promise存取的方式的特殊，所以可以直接将异步调用的结果存储到Promise中
对Promise进行链式调用时
后边的方法（then和catch）读取的上一步的执行结果
如果上一步的执行结果不是当前想要的结果，则跳过当前的方法
当Promise出现异常时，而整个调用链中没有出现catch，则异常会向外抛出

示例:

```javascript
const promise = new Promise((resolve, reject) => {
    reject("周一到周五19点，不见不散")
})

promise
    .then(r => console.log("第一个then", r))
    .catch(r => {
        throw new Error("报个错玩")
    
    	// 前面报错, 后面不会执行
```java
        console.log("出错了")
        return "嘻嘻"
    })
    // 上面的promise报错, 这里不会执行then的第一个回调
    .then(r => console.log("第二个then", r))
    // 会执行 catch
    .catch(r => {
        console.log("出错了")
    })
```


promise中的
then
catch
finally

这三个方法都会返回一个新的Promise

```javascript
const promise = new Promise((resolve, reject) => {
    resolve("hello")
})
const promise2 = promise.then(result =>{
    return "111"
}, reason => {})

console.log(promise)
console.log(promise2)

promise2.then(result => {
    console.log("result:", result)
}, reason => {
    console.log("reason:", reason)
})
```

```shell
Promise {[[PromiseState]]: "fulfilled", [[PromiseResult]]: "hello", Symbol(async_id_symbol): 2, Symbol(trigger_async_id_symbol): 1}
test.js:9
Promise {[[PromiseState]]: "pending", [[PromiseResult]]: undefined, Symbol(async_id_symbol): 3, Symbol(trigger_async_id_symbol): 2}
test.js:12result: 111
```


then 的 两个回调 (result => {} , reason => {})
catch

的回调方法相当于里面最后调用了 `return new Promise(xxx)`

```
这个返回的 Promise 对象中会存储回调函数(then / catch )的返回值 , 如果 回调函数没有返回值, 则 promise 中的数据字段为 undefined 

注意上面流程的执行顺序, 由于 promise 是异步调用,  `console.log(promise2)` 实际是在 `promise.then(...)` 之前执行的, 所以调用 `console.log(promise2)` 时, promise2 还没有设置数据, 因此状态为  pending , 要正确获取 promise2 中数据, 要通过 promise2 调用 then 方法.


finally 的回调函数的返回值不会存储到 finally 方法返回的 promise 对象中

```javascript
const p1 = new Promise((resolve, reject) => {
    resolve("hello")
})
const p2 = p1.then(result => {
    return "111"
}, reason => {
})

p2.then(result => {
```java
    console.log("p2 result:", result)
}, reason => {
    console.log("p2 reason:", reason)
})

const p3 = p2.finally(()=>{
    // 这个返回值不会保留, p3 中储存的还是 p2 中的数据
    return "222"
})

p3.then(result => {
    console.log("p3 result:", result)
}, reason => {
    console.log("p3 reason:", reason)
})
```

输出:

```shell
p2 result: 111
p3 result: 111
```


利用 then 和 catch 回调函数的返回值成为新的 promise 对象数据这一点, 可以形成链式调用:

```javascript
const p1 = new Promise((resolve, reject) => {
    resolve("hello")
}).then(result => {
    console.log("回调函数", result)
    return "锄禾日当午"
}).then(result => {
    console.log("第二个then", result)
    return "超哥真快乐"
}).then(result => {
    console.log(result)
})
```

输出:

```shell
回调函数 hello       
第二个then 锄禾日当午
超哥真快乐      
```


回到开始的问题:

```javascript
function sum(a, b, cb) {
    setTimeout(() => {
        cb(a + b)
    }, 1000);
}
```

可以通过 promise 改造

```javascript
function sum(a, b) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve(a + b)
        }, 1000)
    })
}
```


错误的调用方式:

```javascript
sum(123, 456).then(result => {
    sum(result, 7).then(result =>{
        sum(result, 8).then(result => {
            console.log(result)
        })
    })
})
```

这样会虽然可以获得结果, 但是还是有回调地域 , 失去了 promise 的意义了


正确的调用方式:

```javascript
sum(123, 456)
    .then(result => result + 7)
    .then(result => result + 8)
    .then(result => console.log(result))
```

利用 then 返回新的 promise 的特点, 实现链式调用, 这样的代码逻辑清晰


```
## 静态方法

### Promise.resolve() 

创建一个立即完成的Promise

```javascript
Promise.resolve(10).then(r => console.log(r))
```


### Promise.reject() 

创建一个立即拒绝的Promise

```javascript
Promise.reject("错误").catch((reason)=>{
```java
    console.log(reason)
})
```


```
### Promise.all([...]) 

同时返回多个Promise的执行结果
其中有一个报错，就返回错误

```javascript
function sum(a, b) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve(a + b)
        }, 1000)
    })
}

Promise.all([
    sum(123, 456),
    sum(5, 6),
    sum(33, 44)
]).then(result => {
```java
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})

Promise.all([
    sum(123, 456),
    sum(5, 6),
    Promise.reject("出错了!!!"),
    sum(33, 44)
]).then(result => {
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})
```

输出:

```shell
reason: 出错了!!!
result: [ 579, 11, 77 ]
```


```
### Promise.allSettled([...]) 

同时返回多个Promise的执行结果(无论成功或失败)
{status: 'fulfilled', value: 579}
{status: 'rejected', reason: '哈哈'}

```javascript
function sum(a, b) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve(a + b)
        }, 1000)
    })
}

Promise.allSettled([
    sum(123, 456),
    sum(5, 6),
    sum(33, 44)
]).then(result => {
```java
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})

Promise.allSettled([
    sum(123, 456),
    sum(5, 6),
    Promise.reject("出错了!!!"),
    sum(33, 44)
]).then(result => {
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})
```

输出:

```json
result: [                             
  { status: 'fulfilled', value: 579 },
  { status: 'fulfilled', value: 11 }, 
  { status: 'fulfilled', value: 77 }  
]                                     
result: [                                     
  { status: 'fulfilled', value: 579 },        
  { status: 'fulfilled', value: 11 },         
  { status: 'rejected', reason: '出错了!!!' },
  { status: 'fulfilled', value: 77 }          
]
```


```
### Promise.race([...]) 

返回执行最快的Promise（不考虑对错）

```javascript
function sum(a, b) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve(a + b)
        }, 1000)
    })
}

Promise.race([
    sum(123, 456),
    sum(5, 6),
    sum(33, 44)
]).then(result => {
```java
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})

Promise.race([
    sum(123, 456),
    sum(5, 6),
    Promise.reject("出错了!!!"),
    sum(33, 44)
]).then(result => {
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})
```

输出:

```
reason: 出错了!!!
result: 579
```


```
### Promise.any([...])

返回执行最快的完成的Promise, 优先考虑正确执行的, 如果有一个正确执行的,就返回正确执行的结果, 如果所有的 promise 都返回错误, 则会将所有的错误的 reason 放入 errors 数组返回

```javascript
function sum(a, b) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve(a + b)
        }, 1000)
    })
}

Promise.any([
    sum(123, 456),
    sum(5, 6),
    sum(33, 44)
]).then(result => {
```java
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})

Promise.any([
    sum(123, 456),
    sum(5, 6),
    Promise.reject("出错了!!!"),
    sum(33, 44)
]).then(result => {
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})

Promise.any([
    Promise.reject("出错了 111"),
    Promise.reject("出错了 222"),
    Promise.reject("出错了 333"),
]).then(result => {
    console.log("result:", result)
}).catch(reason => {
    console.log("reason:", reason)
})
```

```shell
reason: [AggregateError: All promises were rejected] {  
  [errors]: [ '出错了 111', '出错了 222', '出错了 333' ]
}                                                       
result: 579
result: 579
```


```
## 执行原理

Promise在执行，then就相当于给Promise了回调函数
当Promise的状态从pending 变为 fulfilled时，
then的回调函数会被放入到任务队列中


promise 内部通过 `queueMicrotask()` 用来向**微任务队列**中添加一个任务


`setTimeout()` 开启了一个定时器, 定时器的作用是间隔一段时间后，将函数放入到任务队列中, 这个任务队列叫做 **宏任务队列**


JS是单线程的，它的运行时基于事件循环机制（event loop）


调用栈
栈
栈是一种数据结构，后进先出
调用栈中，放的是要执行的代码


任务队列
队列
队列是一种数据结构，先进先出
任务队列的是将要执行的代码


当调用栈中的代码执行完毕后，队列中的代码才会按照顺序依次进入到栈中执行


在JS中任务队列有两种
宏任务队列 （大部分代码都去宏任务队列中去排队）
微任务队列 （Promise的回调函数（then、catch、finally））


整个流程
① 执行调用栈中的代码
② 执行微任务队列中的所有任务
③ 执行宏任务队列中的所有任务


### 测试

```javascript
setTimeout(() => {
```java
    console.log(1)
}, 0)

Promise.resolve(1).then(() => {
    console.log(2)
})
```

输出:

```shell
```

从这个实验可以看出:  **微任务队列 优先级高于 宏任务队列**


```javascript
setTimeout(() => {
    console.log(1)
})

Promise.resolve().then(() => {
    setTimeout(()=>{
        console.log(2)
    })
})
```

输出:

```shell
```

注意: 这里 promise 在 微任务队列执行时往 宏任务队列 中放入一个任务


```javascript
Promise.resolve().then(() => {
    Promise.resolve().then(()=>{
        console.log(1)
    })
})

queueMicrotask(() => {
    console.log(2)
})
```

输出:

```shell
```

这里可以看出 queueMicrotask 与 promise 都是放在同一个队列, promise 任务在队列前面, queueMicrotask 在队列后面, 但是 promise 在执行时又往 微任务队列中插入一个 任务, 这个插入的任务在 queueMicrotask  任务的后面, 所以输出为 2 1 


```
### 练习

阅读下列代码，并说出执行结果

```javascript
```java
console.log(1);
setTimeout(() => console.log(2));
Promise.resolve().then(() => console.log(3));
Promise.resolve().then(() => setTimeout(() => console.log(4)));
Promise.resolve().then(() => console.log(5));
setTimeout(() => console.log(6));
console.log(7);
```

```javascript
```


```