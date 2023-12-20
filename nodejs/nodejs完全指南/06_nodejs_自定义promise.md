# 自定义 promise



定义类的思路
1 先把功能都分析清楚了，在动手
2 写一点想一点，走一步看一步



```javascript
const PROMISE_STATE = {
    PENDING:0,
    FULFILLED:1,
    REJECTED:2
}

class MyPromise {

    // 创建一个变量用来存储Promise的结果
    #result
    // 创建一个变量来记录Promise的状态
    #state = PROMISE_STATE.PENDING //pending 0 fulfilled 1 rejected 2

    constructor(executor) {
        // 接收一个 执行器 作为参数
        executor(this.#resolve.bind(this), this.#reject.bind(this)) // 调用回调函数
    }

    // 私有的resolve() 用来存储成功的数据
    #resolve(value) {
        // 禁止值被重复修改
        // 如果state不等于0，说明值已经被修改 函数直接返回
        if (this.#state !== 0) return

        this.#result = value
        this.#state = 1 // 数据填充成功
    }

    // #resolve = () => {
    //     console.log(this)
    // }

    // 私有的reject() 用来存储拒绝的数据
    #reject(reason) { }


    // 添加一个用来读取数据的then方法
    then(onFulfilled, onRejected) {
        if (this.#state === 1) {
            onFulfilled(this.#result)
        }
    }
}

const mp = new MyPromise((resolve, reject) => {
    resolve("孙悟空")
})

mp.then((result) => {
    console.log("读取数据", result)
})
```





```javascript
const PROMISE_STATE = {
    PENDING: 0,
    FULFILLED: 1,
    REJECTED: 2
}

class MyPromise {

    // 创建一个变量用来存储Promise的结果
    #result
    // 创建一个变量来记录Promise的状态
    #state = PROMISE_STATE.PENDING //pending 0 fulfilled 1 rejected 2

    // 创建一个变量来存储回调函数
    #callback

    constructor(executor) {
        // 接收一个 执行器 作为参数
        executor(this.#resolve.bind(this), this.#reject.bind(this)) // 调用回调函数
    }

    // 私有的resolve() 用来存储成功的数据
    #resolve(value) {
        // 禁止值被重复修改
        // 如果state不等于0，说明值已经被修改 函数直接返回
        if (this.#state !== PROMISE_STATE.PENDING) return

        this.#result = value
        this.#state = PROMISE_STATE.FULFILLED // 数据填充成功

        // 当resolve执行时，说明数据已经进来了，需要调用then的回调函数
        this.#callback && this.#callback(this.#result)
    }

    // #resolve = () => {
    //     console.log(this)
    // }

    // 私有的reject() 用来存储拒绝的数据
    #reject(reason) { }


    // 添加一个用来读取数据的then方法
    then(onFulfilled, onRejected) {
        if(this.#state === PROMISE_STATE.PENDING){
            // 进入判断说明数据还没有进入Promise，将回调函数设置为callback的值
            this.#callback = onFulfilled
        }else if (this.#state === PROMISE_STATE.FULFILLED) {
            /* 
                目前来讲，then只能读取已经存储进Promise的数据，
                    而不能读取异步存储的数据
            */
            onFulfilled(this.#result)
        }
    }


}

const mp = new MyPromise((resolve, reject) => {
    setTimeout(() => {
        resolve("孙悟空")
    }, 1000)
})

mp.then((result) => {
    console.log("读取数据", result)
})
```



```javascript
const PROMISE_STATE = {
    PENDING: 0,
    FULFILLED: 1,
    REJECTED: 2
}

class MyPromise {

    // 创建一个变量用来存储Promise的结果
    #result
    // 创建一个变量来记录Promise的状态
    #state = PROMISE_STATE.PENDING //pending 0 fulfilled 1 rejected 2

    // 创建一个变量来存储回调函数
    // 由于回调函数可能有多个，所以我们使用数组来存储回调函数
    #callbacks = []

    constructor(executor) {
        // 接收一个 执行器 作为参数
        executor(this.#resolve.bind(this), this.#reject.bind(this)) // 调用回调函数
    }

    // 私有的resolve() 用来存储成功的数据
    #resolve(value) {
        // 禁止值被重复修改
        // 如果state不等于0，说明值已经被修改 函数直接返回
        if (this.#state !== PROMISE_STATE.PENDING) return

        this.#result = value
        this.#state = PROMISE_STATE.FULFILLED // 数据填充成功

        // 当resolve执行时，说明数据已经进来了，需要调用then的回调函数
        queueMicrotask(() => {
            // 调用callbacks中的所有函数
            this.#callbacks.forEach(cb => {
                cb()
            })
        })
    }

    // #resolve = () => {
    //     console.log(this)
    // }

    // 私有的reject() 用来存储拒绝的数据
    #reject(reason) { }


    // 添加一个用来读取数据的then方法
    then(onFulfilled, onRejected) {
        if (this.#state === PROMISE_STATE.PENDING) {
            // 进入判断说明数据还没有进入Promise，将回调函数设置为callback的值
            // this.#callback = onFulfilled
            this.#callbacks.push(() => {
                onFulfilled(this.#result)
            })
        } else if (this.#state === PROMISE_STATE.FULFILLED) {
            /* 
                目前来讲，then只能读取已经存储进Promise的数据，
                    而不能读取异步存储的数据
            */
            // onFulfilled(this.#result)

            /* 
                then的回调函数，应该放入到微任务队列中执行，而不是直接调用
            */
            queueMicrotask(() => {
                onFulfilled(this.#result)
            })
        }
    }


}

const mp = new MyPromise((resolve, reject) => {
    setTimeout(() => {
        resolve("孙悟空")
    }, 1000)
})

mp.then((result) => {
    console.log("读取数据1", result)
})

mp.then((result) => {
    console.log("读取数据2", result)
})

mp.then((result) => {
    console.log("读取数据3", result)
})

// const p = Promise.resolve('孙悟空')

// p.then(r => console.log("第一次读", r))
// p.then(r => console.log("第二次读", r))
```



```javascript
const PROMISE_STATE = {
    PENDING: 0,
    FULFILLED: 1,
    REJECTED: 2
}

class MyPromise {

    // 创建一个变量用来存储Promise的结果
    #result
    // 创建一个变量来记录Promise的状态
    #state = PROMISE_STATE.PENDING //pending 0 fulfilled 1 rejected 2

    // 创建一个变量来存储回调函数
    // 由于回调函数可能有多个，所以我们使用数组来存储回调函数
    #callbacks = []

    constructor(executor) {
        // 接收一个 执行器 作为参数
        executor(this.#resolve.bind(this), this.#reject.bind(this)) // 调用回调函数
    }

    // 私有的resolve() 用来存储成功的数据
    #resolve(value) {
        // 禁止值被重复修改
        // 如果state不等于0，说明值已经被修改 函数直接返回
        if (this.#state !== PROMISE_STATE.PENDING) return

        this.#result = value
        this.#state = PROMISE_STATE.FULFILLED // 数据填充成功

        // 当resolve执行时，说明数据已经进来了，需要调用then的回调函数
        queueMicrotask(() => {
            // 调用callbacks中的所有函数
            this.#callbacks.forEach(cb => {
                cb()
            })
        })
    }

    // #resolve = () => {
    //     console.log(this)
    // }

    // 私有的reject() 用来存储拒绝的数据
    #reject(reason) { }


    // 添加一个用来读取数据的then方法
    then(onFulfilled, onRejected) {

        /* 
            谁将成为then返回的新Promise中的数据？？？
                then中回调函数的返回值，会成为新的Promise中的数据
        */

        return new MyPromise((resolve, reject) => {
            if (this.#state === PROMISE_STATE.PENDING) {
                // 进入判断说明数据还没有进入Promise，将回调函数设置为callback的值
                // this.#callback = onFulfilled
                this.#callbacks.push(() => {
                    resolve(onFulfilled(this.#result))
                    
                })
            } else if (this.#state === PROMISE_STATE.FULFILLED) {
                /* 
                    目前来讲，then只能读取已经存储进Promise的数据，
                        而不能读取异步存储的数据
                */
                // onFulfilled(this.#result)

                /* 
                    then的回调函数，应该放入到微任务队列中执行，而不是直接调用
                */
                queueMicrotask(() => {
                    resolve(onFulfilled(this.#result))
                })
            }

        })
    }


}

const mp = new MyPromise((resolve, reject) => {
    setTimeout(() => {
        resolve("孙悟空")
    }, 1000)
})

mp.then((result) => {
    console.log("读取数据1", result)
    return "猪八戒"
}).then(r => {
    console.log("读取数据2", r)
    return "沙和尚"
}).then(r => {
    console.log("读取数据3", r)
})




// const p = Promise.resolve('孙悟空')

// p.then(r => console.log("第一次读", r))
// p.then(r => console.log("第二次读", r))
```

