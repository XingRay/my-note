

# javascript 流程控制

流程控制语句可以用来改变程序执行的顺序

1 条件判断语句

2 条件分支语句

3 循环语句



## 1 代码块

使用 `{}` 来创建代码块，代码块可以用来对代码进行分组

```javascript
let a = 10
alert(123)
console.log("哈哈")
document.write("嘻嘻")
```

可以使用代码块进行分组

```javascript
{
	let a = 10
	alert(123)   
}
{
    console.log("哈哈")
    document.write("嘻嘻")    
}
```

同一个代码中的代码，就是同一组代码，一个代码块中的代码要么都执行要么都不执行



## 2 `let` 和 `var` 的区别

在 JS 中，使用 let 声明的变量具有块作用域, 在代码块中声明的变量无法在代码块的外部访问

```javascript
{
	let a = 10
	console.log(a)		// 可以正常访问 a
}
console.log(a)			// 无法访问 a, 报错 Uncaught ReferenceError: a is not defined
```



使用`var`声明的变量，不具有块作用域

```javascript
{
	var a = 10
	console.log(a)		// 可以正常访问 a
}
console.log(a)			// 可以正常访问 a
```

不建议使用`var`, 会使得变量不易维护



## 3 if 语句

### if

语法：

```javascript
if(条件表达式){
	语句...
}
```

执行流程

`if`语句在执行会先对`if`后的条件表达式进行求值判断，如果结果为 `true`，则执行`if`后的语句, 如果为`false`则不执行

`if`语句只会控制紧随其后其后的那一行代码

```javascript
let a = 10
if(a < 10)
	alert('哈哈哈')		// 根据 if 的条件决定这行是否执行
	alert(11111)		  // 注意: 无论 a 为什么值, 这行都会执行, if 只能控制下面 1行 代码
```

如果希望可以控制多行代码，可以使用`{}`将语句扩起来

```javascript
let a = 10
if(a > 10){
	alert('a比10大')
    alert(1111)
}
```

最佳实践：即使if后只有1行代码，我们也应该编写代码块，这样结构会更加的清晰

如果if后的添加表达式不是布尔值，会转换为布尔值然后再运算

```javascript
if(100){
	alert('你猜我执行吗？')		// 会执行, 100 转化为boolean 值为 true
}

let a = 10

if(a === 10){
	alert('a的值是10！')		// 根据 a 是否 全等于 10 决定, 不做类型转化
}
if(a == 10){
	alert('a的值是10！')		// 根据 a 是否 等于 10 决定, 可以做类型转化
}
if(a = 10){				   		// 千万不能这样写, 这个是赋值, 不是判断
	alert('a的值是10！')
}
```



### if-else

语法：

```javascript
if( 条件表达式 ){
	语句...
}else{
	语句...
}
```

执行流程：
if-else执行时，先对条件表达式进行求值判断，
如果结果为true 则执行if后的语句
如果结果为false 则执行else后的语句

示例:

```javascript
let age = 10

if(age >= 60){
	alert('你已经退休了！')
}else{
	alert('你还没有退休！')
}
```



### if-else if-else

语法：

```javascript
if(条件表达式){
	语句...
}else if(条件表达式){
	语句...
}else if(条件表达式){
	语句...
}else if(条件表达式){
	语句...
}else{
	语句...
}
```

执行流程：
if-else if-else语句，会自上向下依次对if后的条件表达式进行求值判断，
如果条件表达式结果为true，则执行当前if后的语句，执行完毕语句结束
如果条件表达式结果为false，则继续向下判断，直到找到true为止
如果所有的条件表达式都是false，则执行else后的语句

注意：
if-else if-else语句中只会有一个代码块被执行，一旦有执行的代码块，下边的条件都不会在继续判断了, 所以一定要注意，条件的编写顺序

示例:

```javascript
let age = 200

if(age >= 100){
	alert('你真是一个长寿的人！')
}else if(age >= 80){
	alert('你比楼上那位还年轻不小！')
}else if(age >= 60 ){
	alert('你已经退休了！')
}else if(age >= 30){
	alert('你已经步入中年了！')
}else if(age >= 18){
	alert('你已经成年了！')
}else{
	alert('你还未成年！')
}

age = 62

if(age >= 18 && age < 30){
	alert('你已经成年了！')
}else if(age >= 30 && age < 60){
	alert('你已经步入中年了！')
}else if(age >= 60){
	alert('你已经退休了！')
}
```



### 练习

参考:

`prompt()` 可以用来获取用户输入的内容, 它会将用户输入的内容以字符串的形式返回，可以通过变量来接收

```javascript
let num = +prompt("请输入一个整数：")
alert(typeof num)
```



#### 练习1

编写一个程序，获取一个用户输入的整数。然后通过程序显示这个数是奇数还是偶数。

```javascript
let num = +prompt("请输入一个整数：")
if(num % 2 == 0 ){
    alert("偶数")
}else{
    alert("奇数")
}
```



#### 练习2

从键盘输入小明的期末成绩:
当成绩为100时，'奖励一辆BMW'
当成绩为[80-99]时，'奖励一台iphone'
当成绩为[60-79]时，'奖励一本参考书'
其他时，什么奖励也没有

```javascript
//从键盘输入小明的期末成绩
let score = +prompt('请输入小明的期末成绩：')

// 检查score是否合法
if(isNaN(score) || score<0 || score>100){
    alert('请输入一个合法的分数')
}else{
    // 当成绩为100时，'奖励一辆BMW'
    if(score === 100){
        alert('汽车一辆~')
    }else if(score >= 80){
       // 当成绩为[80-99]时，'奖励一台iphone'
       alert('手机一台~')
    }else if(score >= 60){
        // 当成绩为[60-79]时，'奖励一本参考书'
        alert('参考书一本~')
    }else{
        // 其他时，什么奖励也没有
        alert('啥也没有~')
    }
}
```



#### 练习3

大家都知道，男大当婚，女大当嫁。那么女方家长要嫁女儿，当然要提出一定的条件：
高：180cm以上; 富:1000万以上; 帅:500以上;
如果这三个条件同时满足，则:'我一定要嫁给他'
如果三个条件有为真的情况，则:'嫁吧，比上不足，比下有余。'
如果三个条件都不满足，则:'不嫁！'

```javascript
let height = +prompt('请输入你的身高（厘米）：')
let money = +prompt('请输入你的身价（万）：')
let face = +prompt('请输入你的颜值（像素）：')

// height 180↑  money 1000↑ face 500↑
if(height>180 && money>1000 && face>500){
    // 如果这三个条件同时满足，则:'我一定要嫁给他'
    alert('我一定要嫁给他！')
}else if(height>180 || money>1000 || face>500){
    // 如果三个条件有为真的情况，则:'嫁吧，比上不足，比下有余。'
    alert('嫁吧，比上不足，比下有余。')
}else{
    // 如果三个条件都不满足，则:'不嫁！'
    alert('不嫁！')
}
```



## 4 switch语句

### 语法

```javascript
switch(表达式){
    case 表达式:
        代码...
        break
    case 表达式:
        代码...
        break
    case 表达式:
        代码...
        break
    case 表达式:
        代码...
        break
    default:
        代码...
        break
}
```



### 执行的流程

switch语句在执行时，会依次将switch后的表达式和case后的表达式进行全等比较
如果比较结果为true，则自当前case处开始执行代码
如果比较结果为false，则继续比较其他case后的表达式，直到找到true为止
如果所有的比较都是false，则执行default后的语句



### 注意

当比较结果为true时，会从当前case处开始执行代码
也就是说case是代码执行的起始位置
这就意味着只要是当前case后的代码，都会执行
可以使用break来避免执行其他的case



### 总结

switch语句和if语句的功能是重复，switch能做的事if也能做，反之亦然。
它们最大的不同在于，switch在多个全等判断时，结构比较清晰



### 示例

```javascript
// 根据用户输入的数字显示中文
let num = +prompt("请输入一个数字")
/* 
1 壹
2 贰
3 叁
*/
switch (num) {
    case 1:
        alert("壹")
        break // break可以用来结束switch语句
    case 2:
        alert("贰")
        break
    case 3:
        alert("叁")
        break
    default:
        alert("我是default")
        break
}
```



## 5 循环语句

通过循环语句可以使指定的代码反复执行
JS中一共有三种循环语句
while语句
do-while语句
for语句



### while

#### 语法：

```javascript
while(条件表达式){
    语句...
}
```

#### 执行流程：

while语句在执行时，会先对条件表达式进行判断，
如果结果为true，则执行循环体，执行完毕，继续判断
如果为true，则再次执行循环体，执行完毕，继续判断，如此重复
知道条件表达式结果为false时，循环结束



#### 死循环

当一个循环的条件表达式恒为true时，这个循环就是一个死循环，会一直执行（慎用）

```javascript
while(true){
    alert('哈哈')
}
```



通常编写一个循环，要有三个要件
1 初始化表达式（初始化变量）
2 条件表达式（设置循环运行的条件）
3 更新表单式（修改初始化变量）

示例:

```javascript
// 初始化表达式
let a = 0

// 条件表达式
while(a < 3){
    console.log(a)

    // 更新表达式
    a++
}
```

```javascript
let i = 0
while(1){
    console.log(i)
    i++

    if(i >= 5){
        break
    }
}
```



#### 练习

假设银行存款的年利率为5%，问1000块存多少年可以变成5000块

```javascript
// 创建一个变量表示钱数
let money = 1000

// 1000 存一年是多少钱？
// money *= 1.05
// money *= 1.05
// money *= 1.05

// 创建一个计数器来记录循环执行的次数
let year = 0

// 编写循环，计算存款的年数
while(money < 5000){
    money *= 1.05 // 循环没执行一次，就相当于钱存了一年
    year++
}

console.log(`需要存${year}年，最终的钱数为${money}元！`)
```



### do-while

#### 语法

```javascript
do{
    语句...
}while(条件表达式)
```



#### 执行顺序

do-while语句在执行时，会先执行do后的循环体，
执行完毕后，会对while后的条件表达式进行判断
如果为false，则循环终止
如果为true，则继续执行循环体，以此类推



#### 和while的区别

while语句是先判断再执行
do-while语句是先执行再判断

实质的区别 : do-while语句可以确保循环至少执行一次

while

```javascript
let i = 10
while (i < 5) {
    console.log(i)
    i++
}
```

do-while:

```javascript
let i = 10
do{
    console.log(i)
    i++
}while(i < 5)
```



### for循环

for循环和while没有本质区别，都是用来反复执行代码 
不同点就是语法结构，for循环更加清晰

#### 语法

```javascript
for(①初始化表达式; ②条件表达式; ④更新表达式){
    ③语句...
}
```
执行流程：
① 执行初始化表达式，初始化变量
② 执行条件表达式，判断循环是否执行（true执行，false终止）
③ 判断结果为true，则执行循环体
④ 执行更新表达式，对初始化变量进行修改
⑤ 重复②，知道判断为false为止



初始化表达式，在循环的整个的生命周期中只会执行1次
for循环中的三个表达式都可以省略
使用let在for循环的()中声明的变量是局部变量，只能在for循环内部访问
使用var在for循环的()中声明的变量可以在for循环的外部访问
创建死循环的方式：

```javascript
while(1){}
```

```javascript
for(;;){}
```



#### 练习1

求100以内所有3的倍数（求它们个数和总和）

```
...
```



while

```javascript
let i = 0
while(i < 5){
    console.log(i)
    i++
}
```

```
for(let i=0; i<5; i++){
    console.log(i)
}
```



死循环

```
for (; ;) {
    alert(1)
}
```

```javascript
let i = 0
for (; i < 5;) {
    console.log(i)
    i++
}
```



求100以内所有3的倍数（求它们个数和总和）

```javascript
let count = 0 // 计数器
let result = 0 // 用来存储计算结果（累加结果）

// 求100以内所有的数
for(let i=1; i<=100; i++){

    // 获取3的倍数
    if(i % 3 === 0){
        count++
        result += i
    }
}

console.log(`3的倍数一共有${count}个，总和为${result}`)
```

```
let count = 0 // 计数器
let result = 0 // 用来存储计算结果（累加结果）

for(let i=3; i<=100; i+=3){
    count++
    result += i
}

console.log(`3的倍数一共有${count}个，总和为${result}`)
```



求 1000 以内的水仙花数
水仙花数
一个n位数（n >= 3），如果它各个位上数字的n次幂之和还等于这个数，那么这个数就是一个水仙花数
如: 153 --> 1  5  3 --> 1  125  27 --> 153

```javascript
获取所有的三位数
for(let i=100; i<1000; i++){

    // 如果i的百位数字 十位数字 个位数字的立方和 还等于 i 则i就是水仙花数
    let bai = parseInt(i / 100)

    // 获取十位数
    let shi = parseInt((i - bai * 100) / 10)

    // 获取个位数
    let ge = i % 10

    // console.log(i,"-->",bai, shi, ge)

    // 判断i是否是水仙花数
    if(bai**3 + shi**3 + ge**3 === i){
        console.log(i)
    }

}
```

```javascript
for (let i = 100; i < 1000; i++) {
    let strI = i + ""
    if (strI[0] ** 3 + strI[1] ** 3 + strI[2] ** 3 === i) {
        console.log(i)
    }
}
```



获取用户输入的大于1的整数（暂时不考虑输错的情况）然后编写代码检查这个数字是否是质数，并打印结果

质数: 一个数如果只能被1和它本身整除，那么这个数就是质数,  1既不是质数也不是合数

```javascript
// 获取用户输入的数值
let num = +prompt("请输入一个大于1的整数：")

// 用来记录num的状态，默认为true，num是质数
let flag = true

for (let i = 2; i < num; i++) {
    if (num % i === 0) {
        // 如果num能被i整除，说明num一定不是质数   
        // 当循环执行时，如果从来没有进入过判断（判断代码没有执行），则说明9是质数
        // 如果判断哪怕只执行了一次，也说明 9 不是质数  
        flag = false
    }
}

if (flag) {
    alert(`${num}是质数！`)
} else {
    alert(`${num}不是质数！`)
}
```



编写代码检查9是否是质数

检查9有没有1和9以外的其他因数
如果有，说明9不是质数
如果没有，说明9是质数

获取所有的可能整除9的数（1-9）
2 3 4 5 6 7 8

检查这一堆数中是否有能整除9的数

```javascript
let flag = true
// 获取所有的可能整除9的数
for (let i = 2; i < 9; i++) {
    if (9 % i === 0) {
        // 如果9能被i整除，说明9一定不是质数
        // 当循环执行时，如果从来没有进入过判断（判断代码没有执行），则说明9是质数
        // 如果判断哪怕只执行了一次，也说明 9 不是质数
        flag = false
    }
}

if (flag) {
    alert('9是质数！')
} else {
    alert('9不是质数！')
}
```



在循环中也可以嵌套其他的循环

希望在网页中打印出如下图形
```
*****
*****
*****
*****
*****


*
**
***
****
*****


*****
****
***
**
*
```

当循环发生嵌套时，外层循环每执行一次，内层循环就会执行一个完整的周期

```javascript
for (let i = 0; i < 5; i++) {
    for (let j = 0; j < 5 - i; j++) {
        document.write("*&nbsp;&nbsp;")
    }

    document.write("<br>")
}

document.write("<hr>")

for (let i = 0; i < 5; i++) {
    for (let j = 0; j < i + 1; j++) {
        document.write("*&nbsp;&nbsp;")
    }

    document.write("<br>")
}

document.write("<hr>")

// 这个for循环，可以用来控制图形的高度
for (let i = 0; i < 5; i++) {
    // 创建一个内层循环来控制图形的宽度
    for (let j = 0; j < 5; j++) {
        document.write("*&nbsp;&nbsp;")
    }

    document.write("<br>")
}
```





练习：
在网页中打印99乘法表
1x1=1
1x2=2 2x2=4
1x3=3 2x3=6 3x3=9
...                          9x9=81

```javascript
// 创建一个外层循环，控制图形高度
for (let i = 1; i <= 9; i++) {
    // 创建内层循环控制图形的宽度
    for (let j = 1; j < i + 1; j++) {
        document.write(`<span>${j} × ${i} = ${i*j}</span>`)
    }
    // 打印换行
    document.write("<br>")
}
```



编写代码，求100以内所有的质数

```javascript
for (let i = 2; i < 100; i++) {
    // 检查i是否是质数，是质数则输出
    // 创建一个变量，记录i的状态，默认i是质数
    let flag = true

    // 获取 1-i 之间的数
    for (let j = 2; j < i; j++) {
        // 判断i能不能被j整除
        if (i % j === 0) {
            // 进入判断，说明i不是质数，修改flag为false
            flag = false
        }
    }

    // 判断结果
    if (flag) {
        console.log(i)
    }
}
```



### break和continue



break

break用来终止switch和循环语句
break执行后，当前的switch或循环会立刻停止
break会终止离他最近的循环

```javascript
for (let i = 0; i < 5; i++) {
    console.log(i)

    for (let j = 0; j < 5; j++) {
        if (j === 1) break
        console.log('内层循环--->', j)
    }
}

for (let i = 0; i < 5; i++) {
    if (i === 3) {
        break
    }
    console.log(i)
}
```



continue

continue用来跳过当次循环

```javascript
for (let i = 0; i < 5; i++) {
    console.log(i)

    for (let j = 0; j < 5; j++) {
        if (j === 1) continue
        console.log("内层循环--->", j)
    }
}

for (let i = 0; i < 5; i++) {
    if (i === 3) {
        continue
    }
    console.log(i)
}
```



质数练习代码优化:

优化前
1 10000以内：290ms
2 100000以内：28202ms

第一次优化：
1 10000以内：30ms
2 100000以内：2331ms
3 1000000以内：192003ms

第二次优化：
1 10000以内：3ms
2 100000以内：20ms
3 1000000以内：394ms

问题：如何修改代码，提升其性能

36
1 36
2 18
3 12
4 9
6 6

问题：如何修改代码，提升其性能

第一次优化:

```javascript
// 开始一个计时器
console.time('质数练习')

for (let i = 2; i < 1000000; i++) {
    let flag = true

    for (let j = 2; j < i; j++) {
        if (i % j === 0) {
            flag = false
            // 进入判断说明i一定不是质数，后续检查没有执行的必要
            break
        }
    }

    if (flag) {
        //console.log(i)
    }
}

// 停止计时器
console.timeEnd('质数练习')
```

第二次优化:

```javascript
console.time('质数练习')

for (let i = 2; i < 1000000; i++) {
    let flag = true

    for (let j = 2; j <= i ** .5; j++) {
        // j 小于 i 的平方根才进行判断, j 如果大于 i 的平方根, 那么 i 一定不能整除 j
        if (i % j === 0) {
            flag = false
            // 进入判断说明i一定不是质数，后续检查没有执行的必要
            break
        }
    }

    if (flag) {
        // console.log(i)
    }
}

// 停止计时器
console.timeEnd('质数练习')
```

