# javascript 面向对象



## 1 面向对象

面向对象编程（OOP）
1 程序是干嘛的？
程序就是对现实世界的抽象（照片就是对人的抽象）

2 对象是干嘛的？
一个事物抽象到程序中后就变成了对象
在程序的世界中，一切皆对象

3 面向对象的编程
面向对象的编程指，程序中的所有操作都是通过对象来完成
做任何事情之前都需要先找到它的对象，然后通过对象来完成各种操作



一个事物通常由两部分组成：数据和功能
一个对象由两部分组成：属性和方法
事物的数据到了对象中，体现为属性
事物的功能到了对象中，体现为方法

如:

数据: 姓名 年龄 身高 体重

功能: 睡 吃

```javascript
const five = {
    // 添加属性
    name:"王老五",
    age:48,
    height:180,
    weight:100,

    // 添加方法
    sleep(){
        console.log(this.name + "睡觉了~")
    },

    eat(){
        console.log(this.name + "吃饭了~")
    }
}
```



## 2 类

使用Object创建对象的问题：
1 无法区分出不同类型的对象
2 不方便批量创建对象

在JS中可以通过类（`class`）来解决这个问题：
1 类是对象模板，可以将对象中的属性和方法直接定义在类中定义后，就可以直接通过类来创建对象
2 通过同一个类创建的对象，我们称为同类对象
可以使用 `instanceof` 来检查一个对象是否是由某个类创建
如果某个对象是由某个类所创建，则我们称该对象是这个类的实例

语法：

```javascript
class 类名 {} // 类名要使用大驼峰命名
```

```javascript
const 类名 = class {}  
```

通过类创建对象

```javascript
new 类()
```

```javascript
const Person = class {}

// Person类专门用来创建人的对象
class Person {

}

// Dog类式专门用来创建狗的对象
class Dog {

}


const p1 = new Person()  // 调用构造函数创建对象
const p2 = new Person()

const d1 = new Dog()
const d2 = new Dog()

console.log(p1 instanceof Person) // true
console.log(d1 instanceof Person) // false


const five = {
    // 添加属性
    name: "王老五",
    age: 48,
    height: 180,
    weight: 100,

    // 添加方法
    sleep() {
        console.log(this.name + "睡觉了~")
    },

    eat() {
        console.log(this.name + "吃饭了~")
    },
}


const yellow = {
    name: "大黄",
    age: 3,
    sleep() {
        console.log(this.name + "睡觉了~")
    },

    eat() {
        console.log(this.name + "吃饭了~")
    },
}
```



## 3 属性

类是创建对象的模板，要创建第一件事就是定义类

类的代码块，默认就是严格模式，
类的代码块是用来设置对象的属性的，不是什么代码都能写

```javascript
class Person{
    name = "孙悟空" // Person的实例属性name p1.name
    age = 18       // 实例属性只能通过实例访问 p1.age

    static test = "test静态属性" // 使用static声明的属性，是静态属性（类属性） Person.test
    static hh = "静态属性"   // 静态属性只能通过类去访问 Person.hh
}

const p1 = new Person()
const p2 = new Person()

console.log(p1)
console.log(p2)
```



## 4 方法

```javascript
class Person {
    name = "孙悟空"
    
    // 添加方法的一种方式
    sayHello = function () {

    }

    // 添加方法（实例方法） 实例方法中this就是当前实例
    sayHello() {
        console.log('大家好，我是' + this.name)
    }

    // 静态方法（类方法） 通过类来调用 静态方法中this指向的是当前类
    static test() {
        console.log("我是静态方法", this)
    }

}

const p1 = new Person()
console.log(p1)
Person.test()
p1.sayHello()
```



## 5 构造函数

```javascript
class Person {
    name = "孙悟空" // 当我们在类中直接指定实例属性的值时，
    // 意味着我们创建的所有对象的属性都是这个值
    age = 18
    gender = "男"

    sayHello() {
        console.log(this.name)
    }
}

// 创建一个Person的实例
const p1 = new Person("孙悟空", 18, "男")
const p2 = new Person("猪八戒", 28, "男")
const p3 = new Person("沙和尚", 38, "男")

console.log(p1)
console.log(p2)
console.log(p3)
```



在类中可以添加一个特殊的方法constructor
该方法我们称为构造函数（构造方法）
构造函数会在我们调用类创建对象时执行

可以在构造函数中，为实例属性进行赋值
在构造函数中，this表示当前所创建的对象

```javascript
class Person {
    constructor(name, age, gender) {
        console.log("构造函数执行了~", name, age, gender)
        this.name = name
        this.age = age
        this.gender = gender
    }
}

const p1 = new Person("孙悟空", 18, "男")
const p2 = new Person("猪八戒", 28, "男")
const p3 = new Person("沙和尚", 38, "男")

console.log(p1)
console.log(p2)
console.log(p3)
```



## 6 封装

面向对象的特点：
封装、继承和多态

1 封装
对象就是一个用来存储不同属性的容器
对象不仅存储属性，还要负责数据的安全
直接添加到对象中的属性，并不安全，因为它们可以被任意的修改



如何确保数据的安全：

1 私有化数据
将需要保护的数据设置为私有，只能在类内部使用

2 提供setter和getter方法来开放对数据的操作
属性设置私有，通过getter setter方法操作属性带来的好处
1  可以控制属性的读写权限
2  可以在方法中对属性的值进行验证



封装主要用来保证数据的安全
实现封装的方式：
1 属性私有化 加#, 实例使用#开头就变成了私有属性，私有属性只能在类内部访问
2 通过getter和setter方法来操作属性

```javascript
get 属性名(){
    return this.#属性
}
```

```javascript
set 属性名(参数){
    this.#属性 = 参数
}
```

```javascript
class Person {
    // #address = "花果山" // 实例使用#开头就变成了私有属性，私有属性只能在类内部访问

    #name
    #age
    #gender

    constructor(name, age, gender) {
        this.#name = name
        this.#age = age
        this.#gender = gender
    }

    sayHello() {
        console.log(this.#name)
    }

    // getter方法，用来读取属性
    getName(){
        return this.#name
    }

    // setter方法，用来设置属性
    setName(name){
        this.#name = name
    }

    getAge(){
        return this.#age
    }

    setAge(age){

        if(age >= 0){
            this.#age = age
        }
    }

    get gender(){
        return this.#gender
    }

    set gender(gender){
        this.#gender = gender
    }
}

const p1 = new Person("孙悟空", 18, "男")

// p1.age = "hello"

// p1.getName()
p1.setAge(-11) // p1.age = 11  p1.age

// p1.setName('猪八戒')


p1.gender = "女"
console.log(p1.gender)
```



## 7 多态

在JS中不会检查参数的类型，所以这就意味着任何数据都可以作为参数传递
要调用某个函数，无需指定的类型，只要对象满足某些条件即可
如果一个东西走路像鸭子，叫起来像鸭子，那么它就是鸭子
多态为我们提供了灵活性

```javascript
class Person{
    constructor(name){
        this.name = name
    }
}

class Dog{
    constructor(name){
        this.name = name
    }
}

class Test{

}

const dog = new Dog('旺财')
const person = new Person("孙悟空")
const test = new Test()

console.log(dog)
console.log(person)
```



定义一个函数，这个函数将接收一个对象作为参数，他可以输出hello并打印对象的name属性

```javascript
function sayHello(obj){
    // if(obj instanceof Person){
    console.log("Hello,"+obj.name)
    // }
}

sayHello(dog)
```



## 8 继承

可以通过extends关键来完成继承
当一个类继承另一个类时，就相当于将另一个类中的代码复制到了当前类中（简单理解）
继承发生时，被继承的类称为 父类（超类），继承的类称为 子类
通过继承可以减少重复的代码，并且可以在不修改一个类的前提对其进行扩展

通过继承可以在不修改一个类的情况下对其进行扩展
OCP 开闭原则
程序应该对修改关闭，对扩展开放



封装 —— 安全性
继承 —— 扩展性
多态 —— 灵活性

```javascript
class Animal{
    constructor(name){
        this.name = name
    }

    sayHello(){
        console.log("动物在叫~")
    }
}

class Dog extends Animal{

}

class Cat extends Animal{

}

class Snake extends Animal{

}

const dog = new Dog("旺财")
const cat = new Cat("汤姆")

dog.sayHello()
cat.sayHello()
console.log(dog)
console.log(cat)
```

```javascript
class Animal{
    constructor(name){
        this.name = name
    }

    sayHello(){
        console.log("动物在叫~")
    }
}

class Dog extends Animal{

    // 在子类中，可以通过创建同名方法来重写父类的方法
    sayHello(){
        console.log("汪汪汪")
    }

}

class Cat extends Animal{

    // 重写构造函数
    constructor(name, age){
        // 重写构造函数时，构造函数的第一行代码必须为super()
        super(name) // 调用父类的构造函数

        this.age = age

    }

    sayHello(){

        // 调用一下父类的sayHello
        super.sayHello() // 在方法中可以使用super来引用父类的方法

        console.log("喵喵喵")
    }
}


const dog = new Dog("旺财")
const cat = new Cat("汤姆", 3)

dog.sayHello()
cat.sayHello()
console.log(dog)
console.log(cat)
```



## 9 对象的结构

对象中存储属性的区域实际有两个：
1 对象自身
直接通过对象所添加的属性，位于对象自身中
在类中通过 x = y 的形式添加的属性，位于对象自身中

2 原型对象（prototype）
对象中还有一些内容，会存储到其他的对象里（原型对象）
在对象中会有一个属性用来存储原型对象，这个属性叫做 `__proto__`



原型对象也负责为对象存储属性，
当我们访问对象中的属性时，会优先访问对象自身的属性，
对象自身不包含该属性时，才会去原型对象中寻找
会添加到原型对象中的情况：
1 在类中通过xxx(){}方式添加的方法，位于原型中
2 主动向原型中添加的属性或方法



```javascript
class Person {
    name = "孙悟空"
    age = 18

    constructor() {
        this.gender = "男"
    }

    sayHello() {
        console.log("Hello，我是", this.name)
    }
}

const p = new Person()
p.address = "花果山"
p.sayHello = "hello"

console.log(p.sayHello)
```



## 10 原型对象

访问一个对象的原型对象

```javascript
对象.__proto__
```

```javascript
Object.getPrototypeOf(对象)
```

原型对象中的数据：
1 对象中的数据（属性、方法等）
2 constructor （对象的构造函数）

注意：
原型对象也有原型，这样就构成了一条原型链，根据对象的复杂程度不同，原型链的长度也不同
p对象的原型链：p对象 --> 原型 --> 原型 --> null
obj对象的原型链：obj对象 --> 原型 --> null

原型链：
读取对象属性时，会优先对象自身属性，
如果对象中有，则使用，没有则去对象的原型中寻找
如果原型中有，则使用，没有则去原型的原型中寻找
直到找到Object对象的原型（Object的原型没有原型（为null））
如果依然没有找到，则返回undefined

作用域链，是找变量的链，找不到会报错
原型链，是找属性的链，找不到会返回undefined

```javascript
class Person {
    name = "孙悟空"
    age = 18

    sayHello() {
        console.log("Hello，我是", this.name)
    }
}
const p = new Person()
console.log(p)

console.log(p.__proto__.__proto__.__proto__)
console.log(p.constructor)

console.log(Object.getPrototypeOf(p) === p.__proto__)

const obj = {} // obj.__proto__
```



所有的同类型对象它们的原型对象都是同一个，
也就意味着，同类型对象的原型链是一样的

原型的作用：
原型就相当于是一个公共的区域，可以被所有该类实例访问，
可以将该类实例中，所有的公共属性（方法）统一存储到原型中
这样我们只需要创建一个属性，即可被所有实例访问

JS中继承就是通过原型来实现的,
当继承时，子类的原型就是一个父类的实例

在对象中有些值是对象独有的，像属性（name，age，gender）每个对象都应该有自己值，
但是有些值对于每个对象来说都是一样的，像各种方法，对于一样的值没必要重复的创建

尝试：
函数的原型链是什么样子的？
Object的原型链是什么样子的？



```javascript
class Person {
    name = "孙悟空"
    age = 18

    sayHello() {
        console.log("Hello，我是", this.name)
    }
}

class Dog {}

const p = new Person()
const p2 = new Person()

p.sayHello = "hello"

const d = new Dog()
const d2 = new Dog()

console.log(p)
console.log(p2)
console.log(p.__proto__ === p2.__proto__)

class Animal{

}

class Cat extends Animal{

}

class TomCat extends Cat{

}

// TomCat --> cat --> Animal实例 --> object --> Object原型 --> null

// cat --> Animal实例 --> object --> Object原型 --> null
// p对象 --> object --> Object原型 --> null
const cat = new Cat()

console.log(cat.__proto__.__proto__.__proto__.__proto__)
```



## 11 修改原型

大部分情况下，我们是不需要修改原型对象
注意：
千万不要通过类的实例去修改原型
1 通过一个对象影响所有同类对象，这么做不合适
2 修改原型先得创建实例，麻烦
3 危险

处理通过 `__proto__` 能访问对象的原型外，
还可以通过类的 `prototype` 属性，来访问实例的原型
修改原型时，最好通过通过类去修改
好处：
1 一修改就是修改所有实例的原型
2 无需创建实例即可完成对类的修改

原则：
1 原型尽量不要手动改
2 要改也不要通过实例对象去改
3 通过 `类.prototype` 属性去修改
4 最好不要直接给 `prototype` 去赋值

```javascript
class Person {
    name = "孙悟空"
    age = 18

    sayHello() {
        console.log("Hello，我是", this.name)
    }
}

Person.prototype.fly = () => {
    console.log("我在飞！")
}

class Dog{

}

const p = new Person()
const p2 = new Person()

// 通过对象修改原型，向原型中添加方法，修改后所有同类实例都能访问该方法 不要这么做
p.__proto__.run = () => {
    console.log('我在跑~')
}

p.__proto__ = new Dog() // 直接为对象赋值了一个新的原型 不要这么做

console.log(p)
console.log(p2)

p.run()
p2.run()

console.log(Person.prototype) // 访问Person实例的原型对象

p.fly()
p2.fly()
```



## 12 instanceof 和 hasOwn

instanceof 用来检查一个对象是否是一个类的实例
instanceof检查的是对象的原型链上是否有该类实例
只要原型链上有该类实例，就会返回true

dog -> Animal的实例 -> Object实例 -> Object原型

Object是所有对象的原型，所以任何和对象和Object进行instanceof运算都会返回true

```javascript
class Animal {}
class Dog extends Animal {}
const dog = new Dog()

console.log(dog instanceof Dog) // true
console.log(dog instanceof Animal) // true
console.log(dog instanceof Object) // true

const obj = new Object()

console.log(obj.__proto__)
console.log(Object.prototype)
dog.__proto__ // Dog.prototype
```



in
使用in运算符检查属性时，无论属性在对象自身还是在原型中，都会返回true



用来检查一个对象的自身是否含有某个属性 (不推荐使用, 对象为 null 时会出错)

```javascript
对象.hasOwnProperty(属性名) 
```


用来检查一个对象的自身是否含有某个属性

```javascript
Object.hasOwn(对象, 属性名) 
```

```javascript
class Person {
    name = "孙悟空"
    age = 18

    sayHello() {
        console.log("Hello，我是", this.name)
    }
}

const p = new Person()

console.log("sayHello" in p)
console.log(p.hasOwnProperty("sayHello"))
console.log(p.__proto__.__proto__.hasOwnProperty("hasOwnProperty"))
console.log(Object.hasOwn(p, "sayHello"))
```



## 13 旧类

早期 JS 中，直接通过函数来定义类
一个函数如果直接调用 `xxx()` 那么这个函数就是一个普通函数
一个函数如果通过 `new` 调用 `new xxx()` 那么这个函数就是一个构造函数



```javascript
var Person = (function () {
    function Person(name, age) {
        // 在构造函数中，this表示新建的对象
        this.name = name
        this.age = age

        this.sayHello = function () {
            console.log(this.name)
        }
    }

    // 向原型中添加属性（方法）
    Person.prototype.sayHello = function () {
        console.log(this.name)
    }

    // 静态属性
    Person.staticProperty = "xxx"
    // 静态方法
    Person.staticMethod = function () {
    }

    return Person
})()

const p = new Person("孙悟空", 18)

console.log(p)
```

等价于：

```
class Person{

}
```



```javascript
var Animal = (function () {
    function Animal() {

    }

    return Animal
})()


var Cat = (function () {
    function Cat() {

    }

    // 继承Animal
    Cat.prototype = new Animal()

    return Cat
})()

var cat = new Cat()

console.log(cat)
```



## 14 new运算符

new运算符是创建对象时要使用的运算符
使用new时，到底发生了哪些事情：
https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/new

当使用new去调用一个函数时，这个函数将会作为构造函数调用，

使用new调用函数时，将会发生这些事：
1 创建一个普通的JS对象（Object对象 {}）, 为了方便，称其为新对象
2 将构造函数的prototype属性设置为新对象的原型
3 使用实参来执行构造函数，并且将新对象设置为函数中的this
4 如果构造函数返回的是一个非原始值，则该值会作为new运算的返回值返回（千万不要这么做）
如果构造函数的返回值是一个原始值或者没有指定返回值，则新的对象将会作为返回值返回
通常不会为构造函数指定返回值

```javascript
function MyClass(){
    var newInstance = {}
    newInstance.__proto__ = MyClass.prototype
}

var mc = new MyClass()
console.log(mc)
class Person{
    constructor(){
    }
}

new Person()
```



## 15 总结

面向对象本质就是，编写代码时所有的操作都是通过对象来进行的。
面向对象的编程的步骤：
1 找对象
2 搞对象

学习对象：
1 明确这个对象代表什么，有什么用
2 如何获取到这个对象
3 如何使用这个对象（对象中的属性和方法）

对象的分类：
内建对象
由ES标准所定义的对象
比如 Object Function String Number ....

宿主对象
由浏览器提供的对象
BOM 、DOM

自定义对象
由开发人员自己创建的对象

