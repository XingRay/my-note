# javascript运算符

## 1 运算符（操作符）

运算符可以用来对一个或多个操作数（值）进行运算

算术运算符：
加法运算符: +
减法运算符: -
乘法运算符: *
除法运算符: /
幂运算: **
模运算: %  两个数相除取余数

```javascript
let a = 1 + 1
a = 10 - 5
a = 2 * 4
a = 10 / 5
a = 10 / 3
a = 10 / 0 // Infinity
a = 10 ** 4
a = 9 ** .5 // 开方
a = 10 % 2
a = 10 % 3
a = 10 % 4
```

注意：

**除0**得到的是 `Infinity`

算术运算时，除了字符串的加法, 其他运算的操作数是非数值时，都会转换为数值然后再运算



JS是一门弱类型语言，当进行运算时会通过自动的类型转换来完成运算

```javascript
a = 10 - '5' // 10 - 5
a = 10 + true // 10 + 1
a = 5 + null // 5 + 0
a = 6 - undefined // 6 - NaN -> NaN
```



当任意一个值和字符串做加法运算时，它会先将其他值转换为字符串，然后再做拼串的操作, 可以利用这一特点来完成类型转换, 可以通过为任意类型 + 一个空串的形式来将其转换为字符串. 其原理和String()函数相同，但使用起来更加简洁

```javascript
a = 'hello' + 'world'	// "helloworld"
a = '1' + 2 			// "12"
a = true + '' 			// "true"
```



## 2 赋值运算符

赋值运算符 `=` 

用来将一个值赋值给一个变量 , 将符号右侧的值赋值给左侧的变量

```javascript
let a = 10
a = 5 // 将右边的值 赋值 给左边的变量
let b = a // 一个变量只有在 = 左边时才是变量，在 = 右边时它是值, 等价于 let b = 5

a = 66
a + 11		// a 的值没有改变
a = a + 11 	// 大部分的运算符都不会改变变量的值，赋值运算符除外
```



简写

```javascript
a += n 		// 等价于 a = a + n
a -= n 		// 等价于 a = a - n
a *= n 		// 等价于 a = a * n
a /= n 		// 等价于 a = a / n
a %= n 		// 等价于 a = a % n
a **= n 	// 等价于 a = a ** n
```



空赋值

只有当变量的值为 `null` 或 `undefined` 时才会对变量进行赋值 `??=`

```javascript
a = null
a ??= 101 	// a 的值为 101

a = undefined
a ??= 101 	// a 的值为 101

a = 100
a ??=101	// 赋值失败, a 的值为 100
```



## 3 一元运算符 + -

元: 操作数



正号 `+` 

不会改变数值的符号

```javascript
let a = 10
a = +a			// 不会改变 a 的值
```



负号 `-` 

可以对数值进行符号位取反

```javascript
let a = -10
a = -a			// a 的值变为 10
```



当我们对非数值类型进行正负运算时，会先将其转换为数值然后再运算

```javascript
let b = '123'	// string 123
b = +b 			// number 123  等价于执行了 b = Number(b)  
```



## 4 自增和自减

### 自增运算符: `++` 

`++` 使用后会使得原来的变量立刻增加1 , 自增分为前自增( `++a` )和后自增( `a++` ) , 无论是 `++a` 还是 `a++` 都会使原变量立刻增加1 , 不同的是 `++a` 和 `a++` 所返回的值不同

`a++` 是自增前的值 **旧值**

`++a` 是自增后的值 **新值**

```javascript
let a = 10
let b = a++
console.log("a = ", a, " b = ", b)		// a = 11 , b = 10

a = 10
let c = ++a
console.log("a = ", a, " c = ", c)		// a = 11, c = 11
```

复杂一点的示例:

```javascript
let n = 5
let result = n++ + ++n + n		// 5 + 7 + 7 = 19
```



### 自减运算符 `--`

使用后会使得原来的变量立刻减小1, 自减分为前自减( `--a` )和后自减( `a--` ) , 无论是 `--a` 还是 `a--` 都会使原变量立刻减少1, 不同的是 `--a` 和 `a--` 的值不同

`--a` 是**新值**
`a--` 是**旧值**

示例:

```javascript
let a = 5
b = --a
console.log("a = ", a, " b = ", b)		// a =  4  b =  4

a = 5
c = a--
console.log("a = ", a, " c = ", c)		// a =  4  c =  5
```



## 5 逻辑运算符

### 1 逻辑非 `!`

! 可以用来对一个值进行非运算, 它可以对一个布尔值进行取反操作

true --> false
false --> true

```javascript
let a = false
a = !a
console.log(a)		// true
```

如果对一个非布尔值进行取反，它会先将其转换为布尔值然后再取反, 可以利用这个特点将其他类型转换为布尔值

```javascript
let a = 1			// 1 转为 boolean 为 true
a = !a
console.log(a)		// false

a = NaN				// NaN 转为 boolean 为 false
a = !a
console.log(a)		// true

a = 123 			// 123 转为 boolean 为 true
a = !!a				// 听过 !a 转化为 boolean型, 但是取值不满足需要, 再次取反就可以将其他类型转化为 boolean
```



### 2 逻辑与 `&&`

可以对两个值进行与运算, 当 `&&` 左右都为 `true` 时，则返回 `true` ，否则返回 `false`

```javascript
let result = true && true 	// true
result = true && false 		// false
result = false && true 		// false
result = false && false 	// false
```



逻辑短路

如果第一个值为 `false`，则直接返回第一个值,  右边的表达式会**直接跳过**, 如果第一个值为true，则返回第二个值

```javascript
true && alert(123) 			// 第一个值为true，alert会执行
false && alert(123) 		// 第一个值为false，alert不会执行
```



对于非布尔值进行与运算，它会转换为布尔值然后运算, 但是最终会返回原值

```javascript
result = 1 && 2 		// true && true  -> true    左边为 true, result 的值为右边表达式的值 2 
result = 1 && 0 		// true && false -> false   左边为 true, result 的值为右边表达式的值 0
result = 0 && NaN 		// false && false -> false  左边为 false, 跳过右边的表达式, result 的值为左边表达式的值 0
```



### 3 逻辑或 `||`

可以对两个值进行或运算, 当||左右有true时，则返回true，否则返回false

```javascript
result = true || false 		// true
result = false || true 		// true
result = true || true 		// true
result = false || false 	// false
```



逻辑短路

或运算也是短路的或，如果第一个值为true，则不看第二个值,  或运算是找true，如果找到true则直接返回，没有true才会返回false, 

```javascript
false || alert(123) 		// 第一个值为false，alert会执行
true || alert(123) 			// 第一个值为true，alert不会执行
```



对于非布尔值或运算，它会转换为布尔值然后运算, 但是最终会**返回原值**, 如果第一个值为true，则返回第一个, 如果第一个值为false，则返回第二个

```javascript
result = 1 || 2 				// 第一个为 true, 直接返回原值: 		1
result = "hello" || NaN 		// 第一个为 true, 直接返回原值: 		"hello"
result = NaN || 1 				// 第一个为false, 返回右边表达式的值:  1
result = NaN || null 			// 第一个为false, 返回右边的表达式的值: null
```



## 6 关系运算符

关系运算符用来检查两个值之间的关系是否成立, 成立返回`true`，不成立返回`false`

| 符号 | 作用                           |
| ---- | ------------------------------ |
| `>`  | 用来检查左值是否大于右值       |
| `>=` | 用来检查左值是否大于或等于右值 |
| `<`  | 用来检查左值是否小于右值       |
| `<=` | 用来检查左值是否小于或等于右值 |

```javascript
let result = 10 > 5 		// true
result = 5 > 5 				// false
result = 5 >= 5 			// true
```



不同类型的值的比较
当对非数值进行关系运算时，它会先将前转换为数值然后再比较 ,

```javascript
result = 5 < "10" 		// 将 "10" 转化为 10 再比较, 结果为: true
result = "1" > false 	// 将 "1" 转化为 1, 将 false 转化为 0 再比较, 结果为: true
```

当关系运算符的两端是两个字符串，它不会将字符串转换为数值，而是逐位的比较字符的Unicode编码   利用这个特点可以对字符串按照字母排序  

```javascript
result = "a" < "b" 		// a在b前, a编码值小于b, 结果为: true
result = "z" < "f" 		// z在f后, z编码值大于f, 结果为: false
result = "abc" < "b" 	// "abc"取出"a"与"b"进行比较, 结果为: true
```

注意比较两个字符串格式的数字时一定要进行类型转换 , 否则容易产生错误

```javascript
result = "12" < "2" 	// 这里当做字符串比较, "12"中的"1"的字符编码小于"2"的字符编码, 所以结果返回: true
result = +"12" < "2" 	// 通过 + 号将"12"转化为数值, 再与"2"比较, 这样就不是字符串比较, "2"也会转化为数值再比较, 结果为: false
```



示例:

检查num是否在5和10之间

```javascript
let num = 4
// result = 5 < num < 10 // 错误的写法
result = num > 5 && num < 10	// false
```



## 7 相等运算符 `==`

相等运算符 `==` 用来比较两个值是否相等, 

```javascript
let result = 1 == 1 				// true
result = 1 == 2 					// false
```

使用相等运算符比较两个不同类型的值时，它会将其转换为相同的类型（通常转换为数值）然后再比较, 类型转换后值相同也会返回true

```javascript
result = 1 == '1' 					// true
result = true == "1" 				// true
```

`null` 和 `undefined` 进行相等比较时会返回 `true`

```javascript
result = null == undefined 			// true
```

`NaN` 不和任何值相等，包括它自身

```javascript
result = NaN == NaN 				// false
NaN > 0 							// false
NaN >= 0 							// false
NaN < 0 							// false
NaN <= 0 							// false
NaN == NaN							// false
```



## 8 全等运算符`===`

用来比较两个值是否全等, 它不会进行自动的类型转换，如果两个值的类型不同直接返回 `false`

```javascript
result = 1 === 1 					// true
result = 1 === "1" 					// false "1" -> 1
```

`null` 和 `undefined` 进行全等比较时会返回 `false`

```javascript
result = null === undefined 		// false
```



## 9 不等运算符 `!=`

用来检查两个值是否不相等, 会自动的进行类型转换

```javascript
result = 1 != 1 					// false
result = 1 != "1" 					// false
```



## 10 不全等运算符 `!==`

比较两个值是否不全等, 不做自动的类型转换

```javascript
result = 1 !== "1" 		// true
```



## 11 条件运算符 `? :`

条件表达式 ? 表达式1 : 表达式2

执行顺序：
条件运算符在执行时，会先对条件表达式进行求值判断，
如果结果为true，则执行表达式1
如果结果为false，则执行表达式2

```javascript
true ? alert(1) : alert(2)		// 1
false ? alert(1) : alert(2)		// 2
```

使用示例:

```javascript
let a = 100
let b = 200
let max = a > b ? a : b
```



## 12 运算符的优先级

和数学一样，JS中的运算符也有优先级，比如先乘除和加减。可以通过优先级的表格来查询运算符的优先级
https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Operator_Precedence

在表格中位置越靠上的优先级越高，优先级越高越先执行，优先级一样自左向右执行,  优先级我们不需要记忆，甚至表格都不需要看,  因为`()`拥有最高的优先级，使用运算符时，如果遇到拿不准的，可以直接通过`()`来改变优先级即可

示例:

```javascript
let a = 1 + 2 * 3 		// 7
a = (1 && 2) || 3		// 2
```

