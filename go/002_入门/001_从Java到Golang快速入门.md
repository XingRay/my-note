## 从Java到Golang快速入门

Golang从09年发布，中间经历了多个版本的演进，已经渐渐趋于成熟，并且出现了很多优秀的开源项目，比如我们熟知的docker，etcd，kubernetes等，其媲美于C的性能、Python的开发效率，又被称为21世纪的C语言，尤其适合开发后台服务。这篇文章主要是介绍Golang的一些主要特性，和Java做一个对比，以便更好的理解Golang这门语言。

关于Golang环境的搭建就不讲了，可以参考官方文档或者Google一下，配置下SDK和PATH即可，非常简单，我们就从Go版本的Hello World开始

## Hello World

每种语言都有自己的Hello World，Go也不例外，Go版本的如下：

```go
package main
 
import "fmt"
 
func main() {
    fmt.Println("Hello, 世界")
}
```

我们使用go run运行后，会在控制台终端看到`Hello, 世界`的输出。我们来看下这段代码：

1. package 是一个关键字，定义一个包，和Java里的package一样，也是模块化的关键。
2. main包是一个特殊的包名，它表示当前是一个可执行程序，而不是一个库。
3. import 也是一个关键字，表示要引入的包，和Java的import关键字一样，引入后才可以使用它。
4. fmt是一个包名，这里表示要引入fmt这个包，这样我们就可以使用它的函数了。
5. main函数是主函数，表示程序执行的入口，Java也有同名函数，但是多了一个String[]类型的参数。
6. Println是fmt包里的函数，和Java里的system.out.println作用类似，这里输出一段文字。

整段代码非常简洁，关键字、函数、包等和Java非常相似，不过注意，go是不需要以;(分号)结尾的。

## 变量

go语言变量的声明和java的略有不同，以声明一个int类型，变量名为age为例，go语言变量生成如下：

```go
var age int =10
```

同样的变量，在java中的声明是：

```go
int age = 10;
```

可以看到go的变量声明，修饰变量的类型在变量的后面，而且是以var关键字开头。

```go
var 变量名 类型 = 表达式
```

最后面的赋值可以在声明的时候忽略，这样变量就有一个默认的值，称之为`零值`。`零值`是一个统称，以类型而定，比如int类型的零值为0，string类型的零值是”“空字符串。

在go中除了以var声明变量之外，还有一种简短的变量声明方式:=,比如上面例子，可以如下简单声明：

```go
age := 10
```

这种方式和上面的例子等价，但是少了var和变量类型，所以简短方便，用的多。使用这种方式，变量的类型由go根据值推导出来，比如这里默认是int。

## 常量

有了变量，就少不了常量，和var关键字不一样，go的常量使用const声明，这个和C里的常量一样。

```go
const age = 10
```

这样就声明了一个常量age，其值是10，因为我们这里没有指定常量的类型，所以常量的类型是根据值推导出来的。所以等价的我们也可以指定常量类型，如下：

```go
const age int = 10
```

相比来说，java下的常量定义就要复杂一些，要有static final修饰符，才是常量：

```go
private static  final int AGE = 10;
```

这个和go的实现等价，但是它的定义修饰符比go多多了，而且常量类型不能省略。

## 大小写标记访问权限

我们上面的go例子中我特意用了小些的变量名age，甚至常量我也没有写成AGE，但是在java中，对于常量我们的习惯是全部大些。

在go中不能随便使用大小写的问题，是因为大小写具有特殊意义，在go中，大些字母开头的变量或者函数等是public的，可以被其他包访问；小些的则是private的，不能被其他包访问到。这样就省去了public和private声明的烦恼，使代码变的更简洁。

特别说明，这些导出规则只适用于包级别名字定义，不能使函数内部的定义。

## 包

包的规则和java很像，每个包都有自己独立的空间，所以可以用来做模块化，封装，组织代码等。
和java不同的是，go的包里可以有函数，比如我们常用的fmt.Println(),但是在在java中没有这种用法，java的方法必须是属于一个类或者类的实例的。

要使用一个包，就需要先导入，使用import关键字，和java也一样，可以参见前面的helloworld示例。

如果我们需要导入多个包的时候，可以像java一样，一行行导入，也可以使用快捷方式一次导入，这个是java所没有的。

```go
import (
    "io"
    "log"
    "net"
    "strconv"
)
```

## 类型转换

go对于变量的类型有严格的限制，不同类型之间的变量不能进行赋值、表达式等操作，必须要要转换成同一类型才可以，比如int32和int64两种int类型的变量不能直接相加，要转换成一样才可以。

```go
    var a int32 = 13
    var b int64 = 20
 
    c := int64(a) + b
```

这种限制主要是防止我们误操作，导致一些莫名其妙的问题。在java中因为有自动转型的概念，所以可以不同类型的可以进行操作，比如int可以和double相加，int类型可以通过`+`和字符串拼接起来，这些在go中都是不可行的。

## map

map类型，Java里是Map接口，go里叫做字典，因为其常用，在go中，被优化为一个语言上支持的结构，原生支持，就像一个关键字一样，而不是java里的要使用内置的sdk集合库，比如HashMap等。

```go
    ages := make(map[string]int)
 
    ages["linday"] = 20
    ages["michael"] = 30
 
    fmt.Print(ages["michael"])
```

go里要创建一个map对应，需要使用关键字make，然后就可以对这个map进行操作。

map的结构也非常简单，符合KV模型，定义为map[key]value, 方括号里是key的类型，方括号外紧跟着对应的value的类型，这些明显和Java的Map接口不同。如果在go中我们要删除map中的一个元素怎么办？使用内置的delete函数就可以,如下代码删除ages这个map中，key为michael的元素。

```
delete(ages,"michael")
```

如果我们想遍历map中的KV值怎么办？答案是使用range风格的for循环，可比Java Map的遍历简洁多了。

```go
    for name,age := range ages {
        fmt.Println("name:",name,",age:",age)
    }
```

range一个map，会返回两个值，第一个是key，第二个是value，这个也是go多值返回的优势，下面会讲。

## 函数方法

在go中，函数和方法是不一样的，我们一般称包级别的(直接可以通过包调用的)称之为函数，比如fmt.Println()；把和一个类型关联起来的函数称之为方法，如下示例：

```go
package lib
 
import "time"
 
type Person struct {
    age  int
    name string
}
 
func (p Person) GetName() string {
    return p.name
}
 
func GetTime() time.Time{
    return time.Now()
}
```

其中GetTime()可以通过lib.GetTime()直接调用，称之为函数；而GetName()则属于Person这个结构体的函数，只能声明了Person类型的实例后才可以调用，称之为方法。

不管是函数还是方法，定义是一摸一样的。而在这里，最可以讲的就是多值返回，也就是可以同时返回多个值，这就大大为我们带来了方便，比如上个遍历map的例子，直接可以获取KV，如果只能返回一个值，我们就需要调用两次方法才可以。

```go
func GetTime() (time.Time,error){
    return time.Now(),nil
}
```

多值返回也很简单，返回的值使用逗号隔开即可。如果要接受多值的返回，也需要以逗号分隔的变量，有几个返回值，就需要几个变量，比如这里：

```go
now,_:=GetTime()
```

如果有个返回值，我们用不到，不想浪费一个变量接收怎么办？这时候可以使用空标志符`_`,这是java没有的。

```go
now,_:=GetTime()
```

## 指针

Go的指针和C中的声明定义是一样的，其作用类似于Java引用变量效果。

```go
    var age int = 10
    var p *int = &age
    *p = 11
    fmt.Println(age)
```

其中指针p指向变量age的内存地址，如果修改*p的值，那么变量age的值也同时会被修改，例子中打印出来的值为11，而不是10.

相对应java引用类型的变量，可以理解为一个HashMap类型的变量，这个变量传递给一个方法，在该方法里对HashMap修改，删除，就会影响原来的HashMap。引用变量集合类最容易理解，自己的类也可以，不过基本类型不行，基本类型不是引用类型的，他们在方法传参的时候，是拷贝的值。

## 结构体替代类

Go中没有类型的概念，只有结构体，这个和C是一样的。

```go
type Person struct {
    age  int
    name string
}
```

Go中的结构体是不能定义方法的，只能是变量，这点和Java不一样的,如果要访问结构体内的成员变量，通过`.`操作符即可。

```go
func (p Person) GetName() string {
    return p.name
}
```

这就是通过`.`操作符访问变量的方式，同时它也是一个为结构体定义方法的例子，和函数不一样的是，在`func`关键字后要执行该方法的接收者，这个方法就是属于这个接收者，例子中是Person这个结构体。

在Go中如果想像Java一样，让一个结构体继承另外一个结构体怎么办？也有办法，不过在Go中称之为组合或者嵌入。

```go
type Person struct {
    age  int
    name string
    Address
}
 
type Address struct {
    city string
}
```

结构体Address被嵌入了Person中，这样Person就拥有了Address的变量和方法，就想自己的一样，这就是组合的威力。通过这种方式，我们可以把简单的对象组合成复杂的对象，并且他们之间没有强约束关系，Go倡导的是组合，而不是继承、多态。

## 接口

Go的接口和Java类型，不过它不需要强制实现，在Go中，如果你这个类型（基本类型，结构体等都可以）拥有了接口的所有方法，那么就默认为这个类型实现了这个接口，是隐式的，不需要和java一样，强制使用`implement`强制实现。

```go
type Stringer interface {
    String() string
}
 
func (p Person) String() string {
    return "name is "+p.name+",age is "+strconv.Itoa(p.age)
}
```

以上实例中可以看到，Person这个结构体拥有了fmt.Stringer接口的方法，那么就说明Person实现了fmt.Stringer接口。

接口也可以像结构体一样组合嵌套，这里不再赘述。

## 并发

Go并发主要靠go goroutine支持，也称之为go协程或者go程，他是语言层面支持的，非常轻量级的多任务支持，也可以把他简单的理解为java语言的线程，不过是不一样的。

```
go run()
```

这就启动一个goroutine来执行run函数，代码非常简洁，如果在java中，需要先New一个Thread，然后在重写他的run方法，然后在start才可以开始。

两个goroutine可以通过channel来通信，channel是一个特殊的类型，也是go语言级别上的支持，他类似于一个管道，可以存储信息，也可以从中读取信息。

```go
package main
 
import "fmt"
 
func main() {
    result:=make(chan int)
 
    go func() {
        sum:=0
        for i:=0;i<10;i++{
            sum=sum+i
        }
        result<-sum
    }()
    fmt.Print(<-result)
}
```

以上示例使用一个单独的goroutine求和，当得到结果时，存放在result这个chan里，然后供main goroutine读取出来。当result没有被存储值的时候，读取result是阻塞的，所以会等到结果返回，协同工作，通过chan通信。

对于并发，go还提供了一套同步机制，都在sync包里，有锁，有一些常用的工具函数等，和java的concurrent框架差不多。

## 异常机制

相比java的Exception来说，go有两种机制，不过最常用的还是error错误类型，panic只用于严重的错误。

```go
type error interface {
    Error() string
}
```

go内置的error类型非常简洁，只用实现Error方法即可，可以打印一些详细的错误信息，比如常见的函数多值返回，最后一个返回值经常是error，用于传递一些错误问题，这种方式要比java throw Exception的方法更优雅。

## Defer代替finally

go中没有java的finally了，那么如果我们要关闭一些一些连接，文件流等怎么办呢，为此go为我们提供了defer关键字，这样就可以保证永远被执行到，也就不怕关闭不了连接了。

```go
f,err:=os.Open(filename)
defer f.Close()
readAll(f)
```

## 统一编码风格

在编码中，我们有时为了是否空行，大括号是否独占一行等编码风格问题争论不休，到了Go这里就终止了，因为go是强制的，比如花括号不能独占一行，比如定义的变量必须使用，否则就不能编译通过。

第二种就是go fmt这个工具提供的非强制性规范，虽然不是强制的，不过也建议使用，这样整个团队的代码看着就像一个人写的。很多go代码编辑器都提供保存时自动gofmt格式的话，所以效率也非常高。

## 便捷的部署

go最终生成的是一个可执行文件，不管你的程序依赖多少库，都会被打包进行，生成一个可执行文件，所以相比java庞大的jar库来说，他的部署非常方便，执行运行这个可执行文件就好了。

对于Web开发，更方便，不用安装jdk，tomcat容器等等这些环境，直接一个可执行文件，就启动了。对于go这种便捷的部署方式，我觉得他更能推进docker的服务化，因为docker就是倡导一个实例一个服务，而且不用各种依赖，layer层级又没那么多，docker image也会小很多。

最后，go目前已经在TIOBE语言排行榜上名列13名了，上升速度还是非常快的，而且随着服务化，容器化，他的优势会越来越多的显现出来，得到更广泛的应用。

如果你感兴趣，那么开始吧，提前准备，机会来的时候，就不会错过了。

尊重原创文章转载自 [飞雪无情](https://link.jianshu.com/?t=http%3A%2F%2Fblog.csdn.net%2Fmichael__li)
[http://blog.csdn.net/michael__li/article/details/53941388](https://link.jianshu.com/?t=http%3A%2F%2Fblog.csdn.net%2Fmichael__li%2Farticle%2Fdetails%2F53941388)