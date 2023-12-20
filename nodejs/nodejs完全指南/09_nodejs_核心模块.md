# nodejs 核心模块

核心模块是node中的内置模块，这些模块有的可以直接在node中使用，有的直接引入即可使用。
window 是浏览器的宿主对象 node中是没有的



## global

global 是node中的全局对象，作用类似于window
ES标准下，全局对象的标准名应该是 globalThis

在浏览器中的 globalThis 就是 window , 在 node 中就是 global



在node中

```javascript
console.log(globalThis)
```

输出:

```shell
<ref *1> Object [global] {                          
  global: [Circular *1],                            
  queueMicrotask: [Function: queueMicrotask],       
  clearImmediate: [Function: clearImmediate],       
  setImmediate: [Function: setImmediate] {          
    [Symbol(nodejs.util.promisify.custom)]: [Getter]
  },                                                
  structuredClone: [Function: structuredClone],     
  clearInterval: [Function: clearInterval],         
  clearTimeout: [Function: clearTimeout],           
  setInterval: [Function: setInterval],             
  setTimeout: [Function: setTimeout] {              
    [Symbol(nodejs.util.promisify.custom)]: [Getter]
  },
  atob: [Function: atob],
  btoa: [Function: btoa],
  performance: Performance {
    nodeTiming: PerformanceNodeTiming {
      name: 'node',
      entryType: 'node',
      startTime: 0,
      duration: 36.50070000067353,
      nodeStart: 2.242800001055002,
      v8Start: 4.986200001090765,
      bootstrapComplete: 24.50280000269413,
      environment: 12.399500001221895,
      loopStart: 31.136100001633167,
      loopExit: -1,
      idleTime: 0.024
    },
    timeOrigin: 1695584264636.992
  },
  fetch: [AsyncFunction: fetch]
}
```



## process

表示当前的node进程
通过该对象可以获取进程的信息，或者对进程做各种操作



如何使用
1  process是一个全局变量，不需要导入任何模块就可以直接使用

```javascript
console.log(process)
```

输出:

```
process {             
  version: 'v18.18.0',
  versions: {         
    node: '18.18.0',  
    acorn: '8.10.0',  
    ada: '2.6.0',     
    ares: '1.19.1',   
    brotli: '1.0.9',  
    cldr: '43.1',     
    icu: '73.2',      
    llhttp: '6.0.11', 
    modules: '108',   
    napi: '9',
    nghttp2: '1.55.0',
    nghttp3: '0.7.0',
    ngtcp2: '0.8.1',
    openssl: '3.0.10+quic',
    simdutf: '3.2.14',
    tz: '2023c',
    undici: '5.22.1',
    unicode: '15.0',
    uv: '1.46.0',
    uvwasi: '0.0.18',
    v8: '10.2.154.26-node.26',
    zlib: '1.2.13.1-motley'
  },
  arch: 'x64',
  platform: 'win32',
  release: {
    name: 'node',
    lts: 'Hydrogen',
    sourceUrl: 'https://nodejs.org/download/release/v18.18.0/node-v18.18.0.tar.gz',
    headersUrl: 'https://nodejs.org/download/release/v18.18.0/node-v18.18.0-headers.tar.gz',
    libUrl: 'https://nodejs.org/download/release/v18.18.0/win-x64/node.lib'
  },
  _rawDebug: [Function: _rawDebug],
  moduleLoadList: [
    'Internal Binding builtins',
    'Internal Binding errors',
    'Internal Binding util',
    ... 100 more items
  ],
  binding: [Function: binding],
  _linkedBinding: [Function: _linkedBinding],
  _events: [Object: null prototype] {
    newListener: [Function: startListeningIfSignal],
    removeListener: [Function: stopListeningIfSignal],
    warning: [Function: onWarning],
    exit: [Function: handleProcessExit],
    SIGWINCH: [
      [Function: refreshStdoutOnSigWinch],
      [Function: refreshStderrOnSigWinch]
    ]
  },
  _eventsCount: 5,
  _maxListeners: undefined,
  domain: null,
  _exiting: [Getter/Setter],
  config: [Getter/Setter],
  dlopen: [Function: dlopen],
  uptime: [Function: uptime],
  _getActiveRequests: [Function: _getActiveRequests],
  _getActiveHandles: [Function: _getActiveHandles],
  getActiveResourcesInfo: [Function: getActiveResourcesInfo],
  reallyExit: [Function: reallyExit],
  _kill: [Function: _kill],
  cpuUsage: [Function: cpuUsage],
  resourceUsage: [Function: resourceUsage],
  memoryUsage: [Function: memoryUsage] { rss: [Function: rss] },
  constrainedMemory: [Function: constrainedMemory],
  kill: [Function: kill],
  exit: [Function: exit],
  hrtime: [Function: hrtime] { bigint: [Function: hrtimeBigInt] },
  openStdin: [Function (anonymous)],
  allowedNodeEnvironmentFlags: [Getter/Setter],
  assert: [Function: deprecated],
  features: {
    inspector: true,
    debug: false,
    uv: true,
    ipv6: true,
    tls_alpn: true,
    tls_sni: true,
    tls_ocsp: true,
    tls: true,
    cached_builtins: [Getter]
  },
  _fatalException: [Function (anonymous)],
  setUncaughtExceptionCaptureCallback: [Function: setUncaughtExceptionCaptureCallback],
  hasUncaughtExceptionCaptureCallback: [Function: hasUncaughtExceptionCaptureCallback],
  emitWarning: [Function: emitWarning],
  nextTick: [Function: nextTick],
  _tickCallback: [Function: runNextTicks],
  _debugProcess: [Function: _debugProcess],
  _debugEnd: [Function: _debugEnd],
  _startProfilerIdleNotifier: [Function (anonymous)],
  _stopProfilerIdleNotifier: [Function (anonymous)],
  stdout: [Getter],
  stdin: [Getter],
  stderr: [Getter],
  abort: [Function: abort],
  umask: [Function: wrappedUmask],
  chdir: [Function: wrappedChdir],
  cwd: [Function: wrappedCwd],
  env: {
    NVM_SYMLINK: 'C:\\Program Files\\nodejs',
    ...
  },
  title: 'C:\\Program Files\\nodejs\\node.exe',
  argv: [
    'C:\\Program Files\\nodejs\\node.exe',
    'D:\\code\\study\\front\\test01\\src\\test.mjs'
  ],
  execArgv: [],
  pid: 6848,
  ppid: 8880,
  execPath: 'C:\\Program Files\\nodejs\\node.exe',
  debugPort: 9229,
  argv0: 'C:\\Program Files\\nodejs\\node.exe',
  exitCode: undefined,
  _preload_modules: [],
  report: [Getter],
  setSourceMapsEnabled: [Function: setSourceMapsEnabled],
  [Symbol(kCapture)]: false
}
```



2  有哪些属性和方法：



### exit()

结束当前进程，终止node

```javascript
process.exit()
```

示例:

```javascript
console.log(11111)
process.exit(0)
console.log(22222)
console.log(33333)
```

输出:

```shell
11111

Process finished with exit code 0
```



### nextTick()

将函数插入到 **tick队列** 中

```javascript
process.nextTick(callback[, …args])
```

tick队列中的代码，会在下一次事件循环之前执行
会在微任务队列和宏任务队列中任务之前执行

执行顺序:  调用栈 => tick队列 => 微任务队列 => 宏任务队列

示例:

```javascript
setTimeout(() => {
    console.log(1) // 宏任务队列
})

queueMicrotask(() => {
    console.log(2)
}) // 微任务队列

process.nextTick(() => {
    console.log(3) // tick队列
})

console.log(4) // 调用栈
```

输出:

```shell
4
2
3
1
```



## path

表示的路径
通过path可以用来获取各种路径



要使用path，需要先对其进行引入

```javascript
const path = require("node:path")
```



#### resolve()

用来生成一个绝对路径

```javascript
path.resolve([…paths]) 
```



相对路径：./xxx  ../xxx/xxx

```
const path = require("node:path")

const result = path.resolve("./m1.js")
console.log(result)
```

输出:

```shell
D:\code\study\front\test01\src\m1.js
```



```javascript
const path = require("node:path")

let result = path.resolve("./hello.js")
console.log(result)
result = path.resolve(
    "C:\\Users\\lilichao\\Desktop\\Node-Course\\03_包管理器",
    "../../hello.js")
console.log(result)
```

输出:

```shell
D:\code\study\front\test01\src\hello.js
C:\Users\lilichao\Desktop\hello.js 
```



绝对路径：
在计算机本地
c:\xxx
/User/xxxx

```javascript
const path = require("node:path")

const result = path.resolve("D:\\tmp\\file.txt")
console.log(result)
```

输出:

```shell
D:\tmp\file.txt
```



在网络中
http://www.xxxxx/...
https://www.xxx/...

```javascript
const path = require("node:path")

const result = path.resolve("https://img2.baidu.com/it/u=1772657338,1845236717&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500")
console.log(result)
```

输出:

```javascript
D:\code\study\front\test01\src\https:\img2.baidu.com\it\u=1772657338,1845236717&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500
```



如果直接调用resolve，则返回当前的工作目录

```javascript
const path = require("node:path")

const result = path.resolve()
console.log(result)
```


注意，我们通过不同的方式执行代码时，它的工作目录是有可能发生变化的

如果将一个相对路径作为参数，
则resolve会自动将其转换为绝对路径
此时根据工作目录的不同，它所产生的绝对路径也不同

一般会将一个绝对路径作为第一个参数，
一个相对路径作为第二个参数
这样它会自动计算出最终的路径



最终形态
以后在使用路径时，尽量通过path.resolve()来生成路径

```javascript
const path = require("node:path")
const result = path.resolve(__dirname, "./hello.js")
console.log(result)
```

输出:

```
D:\code\study\front\test01\src\hello.js
```

这样的好处是在不同的运行方式下得到的结果是一致的 .



## fs

fs （File System）
fs用来帮助node来操作磁盘中的文件
文件操作也就是所谓的I/O，input output



### 普通版 api

使用fs普通版api模块，需要引入

```javascript
const fs = require("node:fs")
```



#### 同步读取文件

readFileSync()

```javascript
const fs = require("node:fs")
const buff = fs.readFileSync(path.resolve(__dirname, "./hello.txt"))
console.log(buff)
console.log(buff.toString())
```

输出:

```shell
<Buffer 68 65 6c 6c 6f 20 6e 6f 64 65 6a 73 0d 0a>
hello nodejs
```

注意这个api会导致阻塞, 不要用于读取大文件



#### 异步读取文件

readFile()

示例:

```javascript
const fs = require("node:fs")
fs.readFile(
    path.resolve(__dirname, "./hello.txt"),
    (err, buffer) => {
        if (err) {
            console.log("出错了~")
        } else {
            console.log(buffer)
            console.log(buffer.toString())
        }
    }
)
```

输出:

```shell
<Buffer 68 65 6c 6c 6f 20 6e 6f 64 65 6a 73 0d 0a>
hello nodejs
```



### promise版api

使用fs promise版api，需要引入

```javascript
const fs = require("node:fs/promises")
```

api 返回值均为 promise

示例:

```javascript
const fs = require("node:fs/promises")
fs.readFile(path.resolve(__dirname, "./hello.txt"))
    .then(buffer => {
        console.log(buffer.toString())
    })
    .catch(e => {
        console.log("出错了~")
    })
```

输出:

```shell
hello nodejs
```



使用 await

```javascript
const fs = require("node:fs/promises")
; (async () => {
    try {
        const buffer = await fs.readFile(path.resolve(__dirname, "./hello.txt"))
        console.log(buffer.toString())
    } catch (e) {
        console.log("出错了~~")
    }
})()
```

输出:

```shell
hello nodejs
```





### 其他常用api

#### fs.readFile()

读取文件



#### fs.appendFile() 

创建新文件，或将数据添加到已有文件中

```javascript
const fs = require("node:fs/promises")
const path = require("node:path")
fs.appendFile(
    path.resolve(__dirname, "./hello.txt"),
    "hahahaha"
).then(r => {
    console.log("添加成功")
})
```



#### fs.mkdir() 

创建目录

mkdir可以接收一个 配置对象作为第二个参数，
通过该对象可以对方法的功能进行配置
recursive 默认值为false
设置true以后，会自动创建不存在的上一级目录

示例:

```javascript
const fs = require("node:fs/promises")
const path = require("node:path")
fs.mkdir(path.resolve(__dirname, "./hello/abc"), { recursive: true })
    .then(r => {
        console.log("操作成功~")
    })
    .catch(err => {
        console.log("创建失败", err)
    })
```



#### fs.rmdir() 

删除目录

```javascript
const fs = require("node:fs/promises")
const path = require("node:path")
fs.rmdir(path.resolve(__dirname, "./hello"), { recursive: true })
    .then(r => {
        console.log("删除成功")
    })
```



#### fs.rm() 

删除文件



#### fs.rename() 

重命名, 可以实现剪切功能, 功能等人同于 mv 指令

示例:

```javascript
const fs = require("node:fs/promises")
const path = require("node:path")
fs.rename(
    path.resolve(__dirname, "../abc.jpg"),
    path.resolve(__dirname, "./def.jpg")
).then(r => {
    console.log("重命名成功")
})
```



#### fs.copyFile() 

复制文件



组合应用 - 复制文件

```javascript
const fs = require("node:fs/promises")
const path = require("node:path")
fs.readFile(path.resolve(__dirname, "./hello.txt"))
    .then(buffer => {
        return fs.appendFile(
            path.resolve(__dirname, "./hello-copy.txt"),
            buffer
        )
    })
    .then(() => {
        console.log("操作结束")
    })
```





