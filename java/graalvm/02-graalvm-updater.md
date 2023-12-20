## graalvm updater

使用`list`指令查看当前已安装的模块

```cmd
gu list
```

结果：

```cmd
ComponentId              Version             Component name                Stability                     Origin
---------------------------------------------------------------------------------------------------------------------------------
graalvm                  22.3.1              GraalVM Core                  Experimental
native-image             22.3.1              Native Image                  Experimental
```



使用`gu available`查看可以下载的模块

```cmd
gu available
```

结果如下

```cmd
Downloading: Component catalog from www.graalvm.org
ComponentId              Version             Component name                Stability                     Origin
--------------------------------------------------------------------------------------------------------------------------------
js                       22.3.1              Graal.js                      Experimental                  github.com
llvm                     22.3.1              LLVM Runtime Core             Experimental                  github.com
llvm-toolchain           22.3.1              LLVM.org toolchain            Experimental                  github.com
native-image             22.3.1              Native Image                  Experimental                  github.com
nodejs                   22.3.1              Graal.nodejs                  Experimental                  github.com
visualvm                 22.3.1              VisualVM                      Experimental                  github.com
wasm                     22.3.1              GraalWasm                     Experimental                  github.com
```

第一列时模块id，`gu install ComponentId`即可下载，如：

```cmd
gu install js
```

结果如下：

```cmd
Downloading: Component catalog from www.graalvm.org
Processing Component: Graal.js
Downloading: Component js: Graal.js from github.com
Installing new component: Graal.js (org.graalvm.js, version 22.3.1)
```

这样就安装好了`js`模块，安装其他模块

```cmd
gu install llvm
gu install llvm-toolchain
gu install nodejs
gu install visualvm
gu install wasm
```

也可以先下载，再本地安装，下载地址 [github](https://github.com/graalvm/graalvm-ce-builds/releases/)

```cmd
gu -L install component.jar
```

卸载模块

```cmd
gu remove ComponentId
```



一个模块只能安装一次，但是可以替换，使用 `-r`  和 `-L`参数，如：

```cmd
gu install -L -r component.jar
gu install -r ruby
```

等价于 先运行`gu remove` ，再运行 `gu install`



在目录中寻找并安装组件

```cmd
gu -C /path/to/download/dir install componentId
```

如：

```cmd
gu -C /tmp/instalables install ruby
```



升级GraalVM

```cmd
gu upgrade
```



没有网络的情况

```cmd
gu -L install /path/to/file
```

`-L` 等价于 `--local-file` 或者 `--file`