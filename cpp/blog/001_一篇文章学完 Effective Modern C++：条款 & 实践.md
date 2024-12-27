## 一篇文章学完 Effective Modern C++：条款 & 实践

原文链接：

[一篇文章学完 Effective Modern C++：条款 & 实践illurin.com/articles/effective-modern-cpp/](https://link.zhihu.com/?target=https%3A//illurin.com/articles/effective-modern-cpp/)

在阅读完 *Effective C++* 后，笔者继续阅读了原作者针对 C++11/14 而写的 *Effective Modern C++*，并结合自己的理解对原书内容进行总结归纳，写下阅读笔记以便日后参考。

如果你对 *Effective C++* 的内容感兴趣，可以参阅：

[一篇文章学完 Effective C++：条款 & 实践304 赞同 · 34 评论文章![img](./assets/v2-e83b37dab63bc425f07d5141b68c6227_r.jpg)](https://zhuanlan.zhihu.com/p/613356779)

## 第一章：类型推导

### 条款 1：理解模板类型推导

函数模板大致形如：

```cpp
template<typename T>
void f(ParamType param);
```

在编译期，编译器会通过表达式推导出两个类型：一个是`T`的类型，另一个是`ParamType`的类型，这两个类型往往不一样，`ParamType`常包含一些饰词，如`const`或引用符号等限定词。

**情况 1：ParamType 是个指针或引用，但不是个万能引用**

1. 若表达式具有引用类型，则先将引用部分忽略。
2. 对表达式的类型和`ParamType`进行匹配来决定`T`的类型。

```cpp
template<typename T>
void f(T& param);

int x = 27;
const int cx = x;
const int& rx = x;

f(x);    // T 的类型为 int, paramType 为 int&
f(cx);   // T 的类型为 const int, paramType 为 const int&
f(rx);   // T 的类型为 const int, paramType 为 const int&
```

若我们假定`param`具有常引用类型，则`T`的类型推导结果中也就没必要包含`const`了：

```cpp
template<typename T>
void f(const T& param);

f(x);    // T 的类型为 int, paramType 为 const int&
f(cx);   // T 的类型为 int, paramType 为 const int&
f(rx);   // T 的类型为 int, paramType 为 const int&
```

如果`param`是个指针（或指向 const 对象的指针）而非引用，运作方式本质上并无不同：

```cpp
template<typename T>
void f(T* param);

int x = 27;
const int* px = &x;

f(&x);   // T 的类型为 int, paramType 为 int*
f(px);   // T 的类型为 const int, paramType 为 const int*
```

**情况 2：ParamType 是个万能引用**

> 详细说明请参考**条款 24**。

1. 如果表达式是个左值，则`T`和`ParamType`都会被推导为左值引用。
2. 如果表达式是个右值，则遵循情况 1 中的规则。

```cpp
template<typename T>
void f(T&& param);

int x = 27;
const int cx = x;
const int& rx = x;

// 左值的情况

f(x);    // T 的类型为 int&, paramType 为 int&
f(cx);   // T 的类型为 const int&, paramType 为 const int&
f(rx);   // T 的类型为 const int&, paramType 为 const int&

// 右值的情况

f(27)    // T 的类型为 int, paramType 为 int&&
```

**情况 3：ParamType 既非指针也非引用**

这种情况即为按值传递，无论传入的是什么，`param`都会是它的一个副本。

```cpp
template<typename T>
void f(T param);

f(x);    // T 和 param 的类型均为 int
f(cx);   // T 和 param 的类型均为 int
f(rx);   // T 和 param 的类型均为 int
```

需要注意的是对于指向 const 对象的 const 指针的传递，仅有指针本身的常量性会被忽略：

```cpp
template<typename T>
void f(T param);

const char* const ptr = "Fun with pointers";

f(ptr);    // T 和 param 的类型均为 const char*
```

**数组实参：**

按值传递给函数模板的数组类型将退化为指针类型，但按引用传递却能推导出真正的数组类型：

```cpp
template<typename T>
void f(T& param);

const char name[] = "J. P. Briggs";

f(name);   // T 的类型为 const char[13], paramType 为 const char (&)[13]
```

利用声明数组引用这一能力可以创造出一个模板，用来推导出数组含有的元素个数：

```cpp
template<typename T, std::size_t N>
constexpr std::size_t arraySize(T (&)[N]) noexcept {
    return N;
}
```

**函数实参：**

函数类型同样也会退化成函数指针，并且和数组类型的规则类似：

```cpp
void someFunc(int, double);

template<typename T>
void f1(T param);

template<typename T>
void f2(T& param);

f1(someFunc);   // param 被推导为函数指针，具体类型为 void (*)(int, double)
f2(someFunc);   // param 被推导为函数引用，具体类型为 void (&)(int, double)
```

### 条款 2：理解 auto 类型推导

`auto`类型推导除了在一个例外情况下，和模板类型推导的规则一模一样，同样可以分为三种情况：

```cpp
// 情况 3

auto x = 27;        // 类型为 int
const auto cx = x;  // 类型为 const int

// 情况 1

const auto& rx = x; // 类型为 const int&

// 情况 2

auto&& uref1 = x;   // 类型为 int&
auto&& uref2 = cx;  // 类型为 const int&
auto&& uref3 = 27;  // 类型为 int&&
```

数组和函数实参的非引用退化规则也同样适用：

```cpp
const char name[] = "J. P. Briggs"; // 类型为 const char[13]
auto arr1 = name;                   // 类型为 const char*
auto& arr2 = name;                  // 类型为 const char (&)[13]

void someFunc(int, double);         // 类型为 void(int, double)
auto func1= someFunc;               // 类型为 void (*)(int, double)
auto& func2= someFunc;              // 类型为 void (&)(int, double)
```

下面我们将讨论例外情况：`auto`会假定用大括号括起的初始化表达式代表一个`std::initializer_list`，但模板类型推导不会。

```cpp
auto x3 = { 27 };   // 类型为 std::initializer_list<int>，值为 { 27 }
auto x4{ 27 };      // 同上

auto x5 = { 1, 2, 3.0 };    // 错误，类型不一致
                            // 无法推导出 std::initializer_list<T> 中的 T

template<typename T>
void f1(T param);
f1({ 11, 23, 9 });   // 错误

template<typename T>
void f2(std::initializer_list<T> param);
f2({ 11, 23, 9 });   // 正确，ParamType 为 std::initializer_list<int>
```

> 需要特别注意的是，2014 年 C++ 标准委员会通过了 [N3922 提案](https://link.zhihu.com/?target=https%3A//www.open-std.org/jtc1/sc22/wg21/docs/papers/2014/n3922.html)，修改了`auto`对于大括号初始化的类型推断规则。上面所提及的`auto x4{ 27 }`这行代码中，`x4`推导出的的类型已经不再是`std::initializer_list<int>`，而是`int`。

在 C++14 中，允许使用`auto`来说明函数返回值需要推导，而且 lambda 表达式也会在形参声明中用到`auto`。然而这些`auto`用法使用的是模板类型推导而非`auto`类型推导，因此也不能使用大括号括起的初始化表达式。

### 条款 3：理解 decltype

绝大多数情况下，`decltype`会得出变量或表达式的类型而不作任何修改。对于类型为`T`的左值表达式，除非该表达式仅有一个名字，否则`decltype`总是得出类型`T&`：

```cpp
int x = 0;
decltype(x);    // 推导结果为 int
decltype((x));  // 推导结果为 int&
```

在 C++11 中，`decltype`的主要用途是声明返回值类型依赖于形参类型的函数模板，这需要用到**返回值类型尾置语法（trailing return type syntax）**：

```cpp
template<typename Container, typename Index>
auto authAndAccess(Container& c, Index i) -> decltype(c[i]) {
    authenticateUser();
    return c[i];
}
```

C++11 允许对单表达式的 lambda 的返回值实施类型推导，而 C++14 将这个允许范围扩张到了一切函数和一切 lambda，包括那些多表达式的。这就意味着在 C++14 中可以去掉返回值类型尾置语法，仅保留前导`auto`。

但编译器会为`auto`指定为返回值类型的函数实施模板类型推导，这样就会留下隐患（例如忽略初始化表达的引用性），使用`decltype(auto)`来说明我们采用的是`decltype`的规则，就可以解决这个问题：

```cpp
template<typename Container, typename Index>
decltype(auto) authAndAccess(Container& c, Index i) {
    authenticateUser();
    return c[i];
}
```

在初始化表达式处也可以应用`decltype`类型推导规则：

```cpp
Widget w;
const Widget& cw = w;

auto myWidget1 = cw;            // auto 推导出类型为 Widget
decltype(auto) myWidget2 = cw;  // decltype 推导出类型为 const Widget&
```

在上述情形中，我们无法向函数传递右值容器，若想要采用一种既能绑定到左值也能绑定到右值的引用形参，就需要借助万能引用，并应用`std::forward`（参考**条款 25**）：

```cpp
template<typename Container, typename Index>
decltype(auto) authAndAccess(Container&& c, Index i) {
    authenticateUser();
    return std::forward<Container>(c)[i];
}
```

### 条款 4：掌握查看类型推导结果的方法

**1. IDE 编辑器**

**2. 编译器诊断信息**

```cpp
template<typename T>    // 只声明 TD 而不定义
class TD;               // TD 是 “类型显示类”（Type Displayer）的缩写

TD<decltype(x)> xType;  // 诱发包括 x 和 y 的类型的错误信息
TD<decltype(y)> yType;
```

**3. 运行时输出**

针对某个对象调用`typeid`，可以得到一个`std::type_info`对象，其拥有一个成员函数`name`，该函数产生一个代表类型的 C-style 的字符串。

但遗憾的是，不同编译器对于`std::type_info::name`的实现各不相同，无法保证完全可靠。并且按照标准，`std::type_info::name`中处理类型的方式和向函数模板按值传参一样，因此类型的引用性以及`const`和`volatile`限定符也将被忽略。

原书中介绍了 Boost.TypeIndex 第三方库用于代替`typeid`：

```cpp
#include <boost/type_index.hpp>

template<typename T>
void f(const T& param) {
    using std::cout;
    using boost::typeindex::type_id_with_cvr;

    // 显示 T 的类型
    cout << "T =          "
         << type_id_with_cvr<T>().pretty_name()
         << '\n';

    // 显示 param 的类型
    cout << "param =          "
         << type_id_with_cvr<decltype(param)>().pretty_name()
         << '\n';
    ...
}
```

## 第二章：auto

### 条款 5：优先选用 auto，而非显式类型声明

`auto`变量要求必须初始化，基本上可以避免会导致兼容性和效率问题的类型不匹配现象，还可以简化重构流程，通常也比显式指定类型要少打一些字，但在使用时需要注意**条款 2** 和**条款 6** 中提到的问题。

使用`auto`和`std::function`都可以存储闭包：

```cpp
// C++14 允许在 lambda 表达式的形参中使用 auto

auto derefLess = [](const auto& p1, const auto& p2)
                  { return *p1 < *p2; };

std::function<bool(const std::unique_ptr<Widget>&,
                   const std::unique_ptr<Widget>&)>
    derefUPLess = [](const std::unique_ptr<Widget>& p1,
                     const std::unique_ptr<Widget>& p2)
                   { return *p1 < *p2; };
```

使用`auto`声明的、存储着一个闭包的变量和该闭包是同一类型，从而它要求的内存量也和该闭包相同；而使用`std::function`声明的、存储着一个闭包的变量是`std::function`的一个实例，不管给定的签名如何，它都占有固定大小的内存，而这个大小对于其存储的闭包而言并不一定够用，如果是这样，那么`std::function`的构造函数就会分配堆上的内存来存储该闭包。再有，编译器的细节一般都会限制内联，并会产生间接函数调用。

综上所述，`std::function`通常比起`auto`更大更慢，还可能导致内存消耗异常，因此实际使用时更推荐`auto`。

考虑以下代码的隐患：

```cpp
std::unordered_map<std::string, int> m;
...

for (const std::pair<std::string, int>& p : m) {
    ...
}
```

`std::unordered_map`的键值部分是 const 的，所以哈希表中的`std::pair`类型应为`std::pair<const std::string, int>`而非`std::pair<std::string, int>`，类型的不匹配会导致额外的临时对象被复制出来，降低了运行效率。

使用`auto`就可以轻松避免这种问题：

```cpp
for (const auto& p : m) {
    ...
}
```

### 条款 6：当 auto 推导的类型不符合要求时，使用显式类型初始化惯用法

“隐形” 的代理类型可以导致`auto`根据初始化表达式推导出 “错误的” 类型，应该防止写出这样的代码：

```cpp
auto someVar = " 隐形 " 代理类型表达式;
```

一个隐形代理类的典型例子是`std::vector<bool>`，它经过了特化，与一般的`std::vector`的行为不同，和`std::bitset`的行为相似，使用一种压缩形式表示其持有的`bool`元素，每个`bool`元素用一个比特来表示。因此，`std::vector<bool>`的`operator[]`并不会直接返回一个`bool&`，而是会返回一个具有类似行为的`std::vector<bool>::reference`类型的对象，并可以隐式转换为`bool`类型。

```cpp
std::vector<bool> features(const Widget& w);
Widget w;

bool highPriority1 = features(w)[5];    // 得到正确的 bool 变量
auto highPriority2 = features(w)[5];    // 错误地得到了 std::vector<bool>::reference 对象
```

除了`std::vector<bool>`以外，标准库中的智能指针和另外一些 C++ 库中的类也使用了代理类的设计模式，例如为了提高数值计算代码效率的**表达式模板**技术：

```cpp
Matrix sum = m1 + m2 + m3 + m4; // 通过使 operator+ 返回结果的代理来提高效率
```

> 在实际编写代码时，记得通过查看文档或头文件中的函数原型来确认手头上的类是否为代理类。

解决代理类问题的做法是：使用带显式类型的初始值设定项来强制`auto`推导出你想要的类型。

```cpp
auto highPriority = static_cast<bool>(features(w)[5]);
```

这种用法并不仅限于会产生代理类型的初始值设定项，它同样可以应用于你想要强调创建一个类型不同于初始化表达式类型的场合，例如：

```cpp
double calcEpsilon();

float ep1 = calcEpsilon();                      // 进行从 double 到 float 的隐式类型转换
auto ep2 = static_cast<float>(calcEpsilon());   // 强调了类型转换的存在
```

## 第三章：转向现代 C++

### 条款 7：在创建对象时注意区分 () 和 {}

为了着手解除众多的初始化语法带来的困惑，也为了解决这些语法不能覆盖所有初始化场景的问题，C++11 引入了统一初始化，以**大括号初始化（braced initialize）** 的形式存在：

```cpp
// 下面两种写法等价
int x{ 0 };
int y = { 0 };
```

大括号可以用于指定容器的初始内容：

```cpp
std::vector<int> v{ 1, 3, 5 };
```

大括号和等号可以用于为非静态成员指定默认初始化值，而小括号不行：

```cpp
class Widget {
    ...

private:
    int x{ 0 }; // 可行
    int y = 0;  // 可行
    int z(0);   // 不可行！
};
```

不可复制的对象可以采用大括号和小括号进行初始化，而不能使用等号：

```cpp
std::atomic<int> ai1{ 0 };  // 可行
std::atomic<int> ai2(0);    // 可行
std::atomic<int> ai3 = 0;   // 不可行！
```

大括号初始化禁止内建类型之间进行**隐式窄化类型转换（narrowing conversion）**：

```cpp
double x, y, z;

int sum1{ x + y + z };  // 错误！double 之和可能无法用 int 表达
int sum2(x + y + z);    // 正确，表达式的值被截断为 int
int sum3 = x + y + z;   // 同上
```

大括号初始化可以避免**最令人烦恼的解析语法（most vexing parse）**：

```cpp
Widget w1(10);  // 调用 Widget 构造函数
Widget w2();    // 声明了一个名为 w2，返回值为 Widget 对象的函数
Widget w3{};    // 调用没有形参的 Widget 构造函数
```

大括号初始化也有一些缺点，其中一个是**条款 2** 中提到的 auto 推导问题，另一个则产生于对带有`std::initializer_list`类型形参的重载版本的强烈偏向性：

```cpp
class Widget {
public:
    Widget(int i, bool b);
    Widget(int i, double d);
    Widget(std::initializer_list<int> f);

    operator int() const;
    ...
};

Widget w1(10, true);        // 调用第一个构造函数
Widget w2{ 10, true };      // 调用带有 std::initializer_list 形参的构造函数

Widget w3(10, 5.0);         // 调用第二个构造函数
Widget w4{ 10, 5.0 };       // 错误！禁止窄化类型转换

Widget w5(w4);              // 调用拷贝构造函数
Widget w6{ w4 };            // 将 w4 强制转换为 int 后，调用带有 std::initializer_list 形参的构造函数

Widget w7(std::move(w4));   // 调用移动构造函数
Widget w8{ std::move(w4) }; // 情况和 w6 相同
```

只有在找不到任何办法把大括号初始值设定项中的实参转换为`std::initializer_list`模板中的类型时，编译器才会退而检查普通的重载决议。

值得注意的是，一对空大括号代表的意义是 “没有实参”，而非 “空的`std::initializer_list`”，后者可以用套娃的括号来表示：

```cpp
Widget w1;      // 调用默认构造函数
Widget w2{};    // 仍然调用默认构造函数
Widget w3();    // 变成了函数声明语句
Widget w4({});  // 调用带有 std::initializer_list 形参的构造函数
                // 并传入空的 std::initializer_list
Widget w5{{}};  // 同上
```

在使用模板进行对象创建时，到底该使用小括号还是大括号会成为一个棘手的问题。举例来说，如果你想以任意数量的实参来创建一个任意类型的对象，那么，一个可变参数模板将会是不错的选择：

```cpp
template<typename T, typename... Ts>
void doSomeWork(Ts&&... params) {
    // 利用 params 创建 T 类型的局部对象
    ...
}

doSomeWork<std::vector<int>>(10, 20);
```

然而此时，在模板内部创建局部对象时，对小括号和大括号的选择将会影响实际创建出的内容：

```cpp
// 得到一个包含 10 个元素的 std::vector
T localObject(std::forward<Ts>(params)...);

// 得到一个包含 2 个元素的 std::vector
T localObject{ std::forward<Ts>(params)... };
```

标准库函数`std::make_unique`和`std::make_shared`也面临着这个问题，它们的解决方案是在内部使用小括号，并将这个决定写进文档中，作为其接口的组成部分。

### 条款 8：优先选用 nullptr，而非 0 或 NULL

`nullptr`的实际类型是`std::nullptr_t`，该类型可以隐式转换到所有的裸指针类型，因此`nullptr`可以扮演所有类型的指针。与`0`和`NULL`不同，`nullptr`不具备整数类型，因此不具有多义性。

`0`和`NULL`导致的重载问题提醒我们应当尽量避免在整型和指针类型之间进行重载：

```cpp
void f(int);
void f(bool);
void f(void*);  // f 的三个重载版本

f(0);           // 调用 f(int)
f(NULL);        // 可能无法通过编译，但一般会调用 f(int)，绝不会调用 f(void*)
f(nullptr);     // 调用 f(void*)
```

`nullptr`在有模板的前提下表现最亮眼：模板类型推导会将`0`和`NULL`推导成 “错误” 类型（即它们的真实类型，而非空指针这个含义），而使用`nullptr`的话，模板就不会带来特殊的麻烦。考虑如下情形：

```cpp
int f1(std::shared_ptr<Widget> spw);
double f2(std::unique_ptr<Widget> upw);
bool f3(Widget* pw);

template<typename FuncType, typename MuxType, typename PtrType>
decltype(auto) lockAndCall(FuncType func, MuxType& mutex, PtrType ptr) {
    std::lock_guard<std::mutex> g(mutex);
    return func(ptr);
}
...

std::mutex f1m, f2m, f3m;
auto result1 = lockAndCall(f1, f1m, 0);         // 错误！
auto result2 = lockAndCall(f2, f2m, NULL);      // 错误！
auto result3 = lockAndCall(f3, f3m, nullptr);   // 正确
```

### 条款 9：优先选用别名声明，而非 typedef

很多人发现别名声明在处理涉及函数指针的类型时，比`typedef`更容易理解：

```cpp
typedef void (*FP)(int, const std::string&)
```

代替为：

```cpp
using FP = void (*)(int, const std::string&);
```

但别名声明的压倒性优势在于**别名模板（alias template）**，它给予了 C++11 程序员一种直截了当的表达机制，用以表达 C++98 程序员不得不用嵌套在模板化的`struct`里面的`typedef`才能硬搞出来的东西。考虑如下情形：

```cpp
template<typename T>
struct MyAllocList {
    typedef std::list<T, MyAlloc<T>> type;
};

template<typename T>
class Widget {
private:
    typename MyAllocList<T>::type list;
    ...
};
```

使用别名模板，就可以让整个写法更简洁，并且可以摆脱类型前的`typename`限定符：

```cpp
template<typename T>
using MyAllocList = std::list<T, MyAlloc<T>>;

template<typename T>
class Widget {
private:
    MyAllocList<T> list;
    ...
};
```

在 C++11 中，标准库的`<type_traits>`给出了一整套用于进行值类别转换的模板，它们是使用`typedef`实现的，对于给定待变换类型 T，其结果类型需要通过`std::transformation<T>::type`的方式获得。而在 C++14 中，所有的值类别转换都加上了对应的别名模板，通过`std::transformation_t<T>`的方式使用，这显然比`typedef`实现的版本更加好用。

```cpp
std::remove_const<T>::type          // C++11: const T -> T
std::remove_const_t<T>              // C++14 中的等价物

std::remove_reference<T>::type      // C++11: T&/T&& -> T
std::remove_reference_t<T>          // C++14 中的等价物

std::add_lvalue_reference<T>::type  // C++11: T -> T&
std::add_lvalue_reference_t<T>      // C++14 中的等价物
```

### 条款 10：优先选用限定作用域的枚举类型，而非不限作用域的枚举类型

C++98 中的枚举类型被称为不限作用域的枚举类型，与之相对的即是 C++11 中引入的限定作用域的枚举类型，即枚举类`enum class`，它的优点很明显：不会产生名称污染。

除此以外，枚举类还是强类型的，而不限范围的枚举类型中的枚举量可以隐式转换到整型（并由此更进一步转换到浮点型）：

```cpp
enum Color { black, white, red };

std::vector<std::size_t> primeFactors(std::size_t x);

Color c = red;

if (c < 14.5) {
    auto factors = primeFactors(c);
    ...
}
```

想要用`enum class`代替`enum`，对其施以强制类型转换即可，但是无法确保转换的合法性：

```cpp
enum class Color { black, white, red };

std::vector<std::size_t> primeFactors(std::size_t x);

Color c = Color::red;

if (static_cast<double>(c) < 14.5) {
    auto factors = primeFactors(static_cast<std::size_t>(c));
    ...
}
```

对于不限范围的枚举类型，编译器为了节约使用内存，通常会为枚举类型选用足够表示枚举量取值的最小底层类型。即使在某些情况下，编译器会采取空间换时间的策略，导致放弃选择尺寸最小的类型，然而它仍然需要保留优化空间的能力。因此，在 C++98 中，`enum`只允许在声明处定义，没有提供对前置声明的支持。

而在 C++11 中，无论是`enum class`还是`enum`都可以进行前置声明，`enum class`的默认底层类型是`int`，而`enum`不具备默认底层类型，只有在指定了的前提下才可以进行前置声明：

```cpp
enum class Status;                  // 底层类型是 int
enum class Status : std::uint32_t;  // 底层类型是 std::uint32_t
enum Color : std::uint8_t;          // 底层类型是 std::uint8_t
```

底层类型指定同样也可以在定义时进行：

```cpp
enum class Status : std::uint32_t {
    good = 0,
    failed = 1,
    incomplete = 100,
    corrupt = 200,
    audited = 500,
    indetermine = 0xFFFFFFFF
};
```

不限范围的枚举类型在你需要更便捷地为数字和名称建立联系时，还是比较好用的，例如在访问元组的元素时，你可以使用枚举量而非直接使用难懂的数字：

```cpp
using UserInfo = std::tuple<std::string, std::string, std::size_t>;
enum UserInfoFields { uiName, uiEmail, uiReputation };

UserInfo uInfo;
...

auto val = std::get<uiEmail>(uInfo);
```

而使用`enum class`就要啰嗦得多：

```cpp
auto val = std::get<static_cast<std::size_t>(UserInfoFields::uiEmail)>(uInfo);
```

如果你实在无法忍受名称污染，执意打算使用`enum class`，那么可以考虑使用以下辅助类来简化书写：

```cpp
template<typename E>    // C++14
constexpr auto toUType(E enumerator) noexcept {
    return static_cast<std::underlying_type_t<E>>(enumerator);
}
...

auto val = std::get<toUType(UserInfoFields::uiEmail)>(uInfo);
```

### 条款 11：优先选用删除函数，而非 private 未定义函数

删除函数和将函数声明为 private 看起来只是风格不同的选择，但其实有更多值得思考的微妙之处，例如：被删除的函数无法通过任何方法调用，对于成员和友元函数中的代码也是如此。

习惯上，删除函数会被声明为 public，而非 private，这样做的理由是：C++ 会先校验可访问性，后校验删除状态，当我们尝试调用某个 private 删除函数时，编译器可能只会提醒函数无法访问，而非更应关心的函数是否被删除。

以下是 C++11 中`std::basic_ios`阻止被复制的方法：

```cpp
template<class charT, class traits = char_traits<charT>>
class basic_ios : public ios_base {
public:
    ...
    basic_ios(const basic_ios&) = delete;
    basic_ios& operator=(const basic_ios&) = delete;
    ...
};
```

任何函数都能被删除，藉此我们可以过滤掉不想要的函数重载版本：

```cpp
bool isLucky(int number);       // 原始版本

bool isLucky(char) = delete;    // 拒绝 char 类型

bool isLucky(bool) = delete;    // 拒绝 bool 类型

bool isLucky(double) = delete;  // 拒绝 double 和 float 类型
```

`float`类型的参数会优先转换到`double`类型，因此传入`float`时会调用`double`类型的重载版本，但由于这个重载版本被删除了，所以编译会被阻止。

删除函数还可以阻止那些不应该进行的模板具现。举例来说，假设你需要一个和内建指针协作的模板，却不想要它对`void*`和`char*`指针进行处理，那么可以写出以下代码：

```cpp
template<typename T>
void processPointer(T* ptr);

template<>
void processPointer<void>(void*) = delete;

template<>
void processPointer<char>(char*) = delete;

template<>
void processPointer<const void>(const void*) = delete;

template<>
void processPointer<const char>(const char*) = delete;

// 删去其它版本，如 volatile void* 和 volatile char*
// 与其它标准字符类型，如 std::wchar_t, std::char16_t 和 std::char32_t
```

成员函数模板可以在类外被删除：

```cpp
class Widget {
public:
    template<typename T>
    void processPointer(T* ptr) { ... }
    ...
};

template<>
void Widget::processPointer<void>(void*) = delete;
```

### 条款 12：为意在改写的函数添加 override 声明

如果要使虚函数重写发生，有一系列要求需要满足：

1. 基类中的函数必须是虚函数。
2. 基类和派生类中的**函数名称**必须完全相同（析构函数除外）。
3. 基类和派生类中的**函数形参类型**必须完全相同。
4. 基类和派生类中的**函数常量性**必须完全相同。
5. 基类和派生类中的**函数返回值**和**异常规格**必须兼容。
6. 基类和派生类的**函数引用限定符**必须完全相同。

由于对声明派生类中的重写，保证正确性很重要，而出错又很容易，C++11 提供了`override`声明来显式地标明派生类中的函数是为了重写基类版本：

```cpp
class Base {
public:
    virtual void mf1() const;
    virtual void mf2(int);
    virtual void mf3() &;
    virtual void mf4() const;
};

class Derived : public Base {
public:
    virtual void mf1() const override;
    virtual void mf2(int x) override;
    virtual void mf3() & override;
    void mf4() const override;  // 加个 "virtual" 没问题，但也没必要
};
```

这样做的好处不仅在于让编译器提醒你想要重写的函数实际上并未重写，还可以让你在打算更改基类中虚函数的签名时，衡量一下其所造成的影响。

> `override`和`final`是 C++11 中加入的**语境关键字（contextual keyword）**，它们的特点是仅会在特定语境下才发挥被保留的意义，因此如果你有一些遗留代码，其中已经用过`override`和`final`作为名称的话，并不需要为它们改名。

**函数引用限定符（reference qualifier）：** 限制成员函数仅用于左值对象或右值对象。

```cpp
class Widget {
public:
   void doWork() &;    // 仅在 *this 是左值时调用
   void doWork() &&;   // 仅在 *this 是右值时调用
};
...

Widget makeWidget();
Widget w;
...

w.doWork();            // 以左值调用 Widget::doWork &
makeWidget().doWork(); // 以右值调用 Widget::doWork &&
```

带引用限定符的成员函数并不常见，但有时也是需要的。举例来说，假设我们的`Widget`类中有个`std::vector`类型的数据成员，我们提供一个函数让用户能对这个数据成员直接访问，但对于左值对象和右值对象有不同的行为：

```cpp
class Widget {
public:
    using DataType = std::vector<double>;

    DataType& data() &              // 对于左值 Widget 类型，返回左值
    { return values; }

    DataType data() &&              // 对于右值 Widget 类型，返回右值
    { return std::move(values); }
    ...

private:
    DataType values;
};
...

Widget makeWidget();
Widget w;
...

auto vals1 = w.data();              // 调用 Widget::data 的左值重载版本
                                    // vals1 采用拷贝构造完成初始化

auto vals2 = makeWidget().data();   // 调用 Widget::data 的右值重载版本
                                    // vals2 采用移动构造完成初始化
```

### 条款 13：优先选用 const_iterator，而非 iterator

`const_iterator`是 STL 中提供的与指向 const 的指针含义相同之物，它们指向不可被修改的值。任何时候只要你需要一个迭代器而其所指向的内容没有修改的必要，那就应该使用 const_iterator。

但在 C++98 中，`const_iterator`得到的支持不够全面，想要获取它们就很不容易，而获取到了以后使用它们的方法也很受限。例如在 C++98 中，我们会被迫写出以下代码：

```cpp
typedef std::vector<int>::iterator IterT;
typedef std::vector<int>::const_iterator ConstIterT;

std::vector<int> values;
...

ConstIterT ci = 
    std::find(static_cast<ConstIterT>(values.begin()),
              static_cast<ConstIterT>(values.end()),
              1983);                            // const_iterator 作为参数，返回 const_iterator

values.insert(static_cast<IterT>(ci), 1998);    // C++98 中 insert 只能接受 iterator
                                                // 从 const_iterator 到 iterator 不存在可移植的类型转换
                                                // 可能无法通过编译
```

而在 C++11 中，这些现象得到了彻底的改变，获取和使用`const_iterator`都变得容易了。要把原始的、使用`iterator`的 C++98 代码修改成使用`const_iterator`的 C++11 代码也很简单：

```cpp
std::vector<int> values;
...

auto it = std::find(values.cbegin(), values.cend(), 1983);

values.insert(it, 1998);
```

C++11 对于`const_iterator`支持的唯一缺陷是只为`begin`和`end`提供了对应的非成员函数版本，而没有为`cbegin`、`cend`、`rbegin`、`cend`、`crbegin`和`crend`这些返回`const_iterator`的函数提供对应的非成员函数版本，这个问题在 C++14 中得到了解决。想要自己实现它们也很简单，如下就是非成员函数版本的`cbegin`的一个实现方式：

```cpp
template<class C>
auto cbegin(const C& container) -> decltype(std::begin(container)) {
    return std::begin(container);
}
```

该模板在传入一个内建数组时也管用，此时`container`会成为一个 const 数组的引用。

> C++11 的非成员函数版本的`begin`为内建数组提供了一个特化版本，它返回一个指向数组首元素的指针。由于 const 数组的元素都为 const，所以若给`begin`传入一个 const 数组，则返回的指针是个指向 const 的指针，即数组意义下的 const_iterator。

由于内建数组和第三方库的存在，最通用化的代码往往不会假定成员函数的存在，而是更多地采用非成员函数版本，例如以下`findAndInsert`模板的通用形式：

```cpp
template<typename C, typename V>
void findAndInsert(C& container, const V& targetVal, const V& insertVal) {
    using std::cbegin;
    using std::cend;

    auto it = std::find(cbegin(container), cend(container), targetVal);

    container.insert(it, insertVal);
}
```

### 条款 14：只要函数不会抛出异常，就为其加上 noexcept 声明

在 C++11 中，C++98 风格的异常规范已经被弃用，而转为为不会抛出异常的函数提供`noexcept`声明，函数是否要加上这个声明，事关接口声明。

调用方可以查询函数的`noexcept`状态，而查询结果可能会影响调用代码的异常安全性和运行效率。这么一来，函数是否带有`noexcept`声明就是和成员函数是否带有 const 声明同等重要的信息。当你明明知道一个函数不会抛出异常却未给它加上`noexcept`声明的话，就属于接口规格设计缺陷。

相当于不带`noexcept`声明的函数，带有`noexcept`声明的函数有更多机会得到优化：

```cpp
RetType function(params) noexcept;  // 最优化

RetType function(params) throw();   // 优化不足

RetType function(params);           // 优化不足
```

在带有`noexcept`声明的函数中，优化器不需要在异常传出函数的前提下，将运行时栈保持在可展开状态；也不需要在异常逸出函数的前提下，保证所有其中的对象以其被构造顺序的逆序完成析构。而那些以`throw()`异常规格声明的函数就享受不到这样的优化灵活性，和那些没有加上异常规格的函数一样。

`noexcept`属性对于移动操作、swap、内存释放函数和析构函数最有价值。C++11 STL 中的大部分函数遵循 “能移动则移动，必须复制才复制” 策略，但这必须保证在使用移动操作代替复制操作后，函数依旧具备强异常安全性。为了得知移动操作会不会产生异常，就需要校验这个操作是否带有`noexcept`声明。

`swap`函数是许多 STL 算法实现的核心组件，它的广泛使用昭示着针对其实施`noexcept`声明带来的收益是可观的。标准库中的`swap`是否带有`noexcept`声明，取决于用户定义的`swap`自身。例如，标准库为数组和`std::pair`准备的`swap`函数如下：

```cpp
template<class T, size_t N>
void swap(T (&a)[N],
          T (&b)[N]) noexcept(noexcept(swap(*a, *b)));

template<class T1, class T2>
struct pair {
    ...
    void swap(pair& p) noexcept(noexcept(swap(first, p.first)) &&
                                noexcept(swap(second, p.second)));
    ...
};
```

这些函数带有条件式`noexcept`声明，它们到底是否具备`noexcept`属性，取决于它的`noexcept`分句中的表达式是否结果为`noexcept`。在此处，数组和`std::pair`的`swap`具备`noexcept`属性的前提是，其每一个元素的`swap`都具备`noexcept`属性。

对于某些函数来说，具备`noexcept`属性是如此之重要，所以它们默认就是如此。在 C++11 中，内存释放函数和所有的析构函数都默认隐式地具备`noexcept`属性。析构函数未隐式地具备`noexcept`属性的唯一情况，就是所有类中有数据成员（包括继承而来的成员，以及在其他数据成员中包含的数据成员）的类型显式地将其析构函数声明为`noexcept(false)`，即可能抛出异常。

> 不具备`noexcept`属性的析构函数很少见，标准库里一个都没有，而如果标准库使用了某个对象，其析构函数抛出了异常，则该行为是未定义的。

大多数函数都是**异常中立（exception-neutral）** 的，不具备`noexcept`属性。此类函数自身并不抛出异常，但它们调用的函数可能会抛出异常，这些异常会经由异常中立函数传至调用栈的更深一层。

C++ 允许带有`noexcept`声明的函数依赖于缺乏`noexcept`保证的代码：

```cpp
void setup();
void cleanup();

void doWork() noexcept {
    setup();
    ...
    cleanup();
}
```

值得一提的是，有些库的接口设计者会把函数区分为带有**宽松规约（wide constract）** 和带有**狭隘规约（narrow constract）** 的不同种类。带有宽松规约的函数是没有前置条件的，要调用这样的函数也无须关心程序状态；而对于带有狭隘规约的函数，如果前置条件被违反，则结果将成为未定义的。一般而言，我们只会把`noexcept`声明保留给那些带有宽松规约的函数。

### 条款 15：只要有可能使用 constexpr，就使用它

**`constexpr`对象：** 具备 const 属性，并由编译期已知的值完成初始化。

在编译阶段就已知的值拥有许多特权，它们可能被放置在只读内存里（对于嵌入式开发尤为重要）；在编译阶段就已知的常量整型值可以用在 C++ 要求整型常量表达式的语境中，包括数组的尺寸规格、整型模板实参、枚举量的值、对齐规格等，如下所示：

```cpp
int sz;                             // 非 constexpr 变量

...

const auto arraySize = sz;          // 正确，arraySize 是 sz 的一个 const 副本
std::array<int, arraySize> data;    // 错误！arraySize 的值非编译期可知

constexpr auto arraySize1 = sz;     // 错误！sz 的值在编译期未知
std::array<int, sz> data1;          // 错误！问题同上

constexpr auto arraySize2 = 10;     // 正确，10 是编译期常量
std::array<int, arraySize2> data2;  // 正确，arraySize2 是编译期常量
```

**`constexpr`函数：**

- `constexpr`函数可以用在要求编译期常量的语境中。在这种情况下，若传给一个`constexpr`函数的实参值是在编译期已知的，则结果也会在编译期计算出来；如果任何一个实参值在编译期未知，则代码将无法通过编译。
- `constexpr`函数也可以运用在非编译期常量的语境中，此时传入的值可以有一个或多个在编译期未知。它的运作方式和普通函数无异，同样在运行期完成结果的计算。
- 在 C++11 中，`constexpr`函数不得包含多于一个可执行语句，即一条`return`语句；而到了 C++14，就没有了这种限制。

```cpp
constexpr int pow(int base, int exp) noexcept {     // C++11
    return (exp == 0 ? 1 : base * pow(base, exp - 1));
}

constexpr int pow(int base, int exp) noexcept {     // C++14
    auto result = 1;
    for (int i = 0; i < exp; ++i) result *= base;

    return result;
}

constexpr auto numConds = 5;
std::array<int, pow(3, numConds)> results;
```

`constexpr`函数仅限于传入和返回**字面类型（literal type）**，这些类型能够持有编译期可以决议的值。在 C++11 中，除了`void`的所有内建类型都是字面类型；此外，我们也可以自定义字面类型，这需要将其构造函数和部分成员函数声明为`constexpr`函数：

```cpp
class Point {
public:
    constexpr Point(double xVal = 0, double yVal = 0) noexcept :
        x(xVal), y(yVal) {}

    constexpr double xValue() const noexcept { return x; }
    constexpr double yValue() const noexcept { return y; }

    void setX(double newX) noexcept { x = newX; }
    void setY(double newY) noexcept { y = newY; }

private:
    double x, y;
};

constexpr Point p1(9.4, 27.7);  // 在编译期执行 constexpr 构造函数
constexpr Point p2(28.8, 5.3);  // 同上

constexpr Point midpoint(const Point& p1, const Point& p2) noexcept {
    return { (p1.xValue() + p2.xValue()) / 2,
             (p1.yValue() + p2.yValue()) / 2 }; // 调用 constexpr 成员函数
}

constexpr auto mid = midpoint(p1, p2);  // 使用 constexpr 函数的返回值来初始化 constexpr 对象
```

在 C++14 中，就连返回值类型为`void`的 setter 函数也可以声明为`constexpr`函数，这就使以下代码变为可能：

```cpp
class Point {
public:
    ...
    constexpr void setX(double newX) noexcept { x = newX; }
    constexpr void setY(double newY) noexcept { y = newY; }
    ...
};
...

constexpr Point reflection(const Point& p) noexcept {
    Point result;

    result.setX(-p.xValue());
    result.setY(-p.yValue());

    return result;
}

constexpr auto reflectionMid = reflection(mid);
```

需要注意的是，一旦你把一个对象或函数声明成了`constexpr`，而后来你又感觉对`constexpr`运用不当，然后进行了移除，那么这会导致非常多客户代码无法通过编译。因此，“只要有可能使用`constexpr`，就使用它” 这句话中的 “只要有可能” 的含义就是你是否有一个长期的承诺，将由`constexpr`带来的种种限制施加于相关的函数和对象上。

### 条款 16：保证 const 成员函数的线程安全性

对于 const 成员函数，我们通常认为它代表的是读操作，而多个线程在没有同步的情况下执行读操作应该是安全的。因此，我们需要保证 const 成员函数的线程安全性，除非可以确信它们不会在并发语境中被使用。

考虑如下情形，我们将计算出的多项式的根存入缓存中，以避免代价高昂的重复计算：

```cpp
class Polynomial {
public:
    using RootsType = std::vector<double>;

    RootsType roots() const {
        if (!rootsAreValid) {       // 如果缓存无效
            ...
            rootsAreValid = true;   // 则计算根，并将其存入 rootVals
        }
        return rootsVals;
    }

private:
    mutable bool rootsAreValid{ false };
    mutable RootsType rootVals{};
};
```

由于 mutable 成员变量的存在，可能有不同的多个线程通过`roots`成员函数在没有同步的情况下读写同一块内存，造成**数据竞争（data race）**，这会导致未定义行为的出现。

有两种方法可以解决这个问题，最简单的方法也是最常见的，引入一个 mutex 互斥量：

```cpp
class Polynomial {
public:
    using RootsType = std::vector<double>;

    RootsType roots() const {
        std::lock_guard<std::mutex> g(m);   // 互斥量加锁

        if (!rootsAreValid) {       // 如果缓存无效
            ...
            rootsAreValid = true;   // 则计算根，并将其存入 rootVals
        }
        return rootsVals;
    }                                       // 互斥量解锁

private:
    mutable std::mutex m;                   // 添加 mutable 的互斥量
    mutable bool rootsAreValid{ false };
    mutable RootsType rootVals{};
};
```

另一种方法是使用`std::atomic`类型的变量，这会比使用互斥量提供更好的性能，但更适用于对单个变量或内存区域的操作。以下情况更适合使用`std::atomic`来确保线程安全性：

```cpp
class Point {
public:
    ...

    double distanceFromOrigin() const noexcept {
        ++callCount;                                // 带原子性的自增操作

        return std::sqrt((x * x) + (y * y));
    }

private:
    mutable std::atomic<unsigned> callCount{ 0 };   // 存储调用次数
    double x, y;
};
```

无论是`std::mutex`还是`std::atomic`都是只移类型，无法进行复制，因此加入它们都会使类失去可复制性，但仍然可以移动。

### 条款 17：理解特殊成员函数的生成机制

在 C++11 中，支配特殊成员函数的机制如下（所有生成的默认特殊函数都是 inline 的，且具有 public 访问权限）：

- **默认构造函数：** 与 C++98 的机制相同。仅当类中不包含用户声明的构造函数时才生成。
- **析构函数：** 与 C++98 的机制基本相同，唯一的区别在于析构函数默认为 noexcept（参考**条款 14**）。仅当基类的析构函数为虚时，派生类的析构函数才为虚。
- **拷贝构造函数：** 运行期行为与 C++98 相同：**按成员**进行**非静态**数据成员的拷贝构造。仅当类中不包含用户声明的拷贝构造函数时才生成。如果该类声明了移动操作，则拷贝构造函数将被删除。在已经存在拷贝赋值运算符或析构函数的情况下，仍然生成拷贝构造函数已经成为了被废弃的行为（但未被禁止），原因见三者法则。
- **拷贝赋值运算符：** 运行期行为与 C++98 相同：**按成员**进行**非静态**数据成员的拷贝赋值。仅当类中不包含用户声明的拷贝赋值运算符时才生成。如果该类声明了移动操作，则拷贝赋值运算符将被删除。在已经存在拷贝构造函数或析构函数的情况下，仍然生成拷贝构造函数已经成为了被废弃的行为（但未被禁止），原因见三者法则。

> **三者法则（Rule of Three）：** 如果你声明了拷贝构造函数、拷贝赋值运算符或析构函数中的任何一个，你就得同时声明所有这三个。
> 三者法则根植于这样的思想：如果有改写拷贝操作的需求，往往意味着该类需要执行某种资源管理，而这就意味着：
>
> 1. 在一种拷贝操作中进行的任何资源管理，也极有可能在另一种拷贝操作中也需要进行。
> 2. 该类的析构函数也会参与到该资源的管理中（通常是对资源进行释放）。
>
> 三者法则对移动操作也同样成立。

- **移动构造函数和移动赋值运算符：** 都**按成员**进行**非静态**数据成员的移动操作。仅当类中不包含用户声明的拷贝操作、移动操作和析构函数时才生成。声明一个移动构造函数会阻止编译器生成移动赋值运算符，而声明一个移动赋值运算符也会阻止编译器生成移动构造函数。

> 声明拷贝操作（无论是拷贝构造还是拷贝赋值）的行为表明了对象的常规拷贝方式（按成员拷贝）对于该类并不适用，那么编译器就会认为按成员移动极有可能也不适用于移动操作。因此，一旦显式声明了拷贝操作，编译器就不再会为其生成移动操作，反之亦然。

如果你有一些代码依赖于编译器自动生成的特殊函数，并且你确信这些函数会正确执行，那么可以用`=default`显式指定让它们生成：

```cpp
class Base {
public:
    virtual ~Base() = default;              // 使析构函数成为虚的

    Base(Base&&) = default;                 // 提供移动操作的支持
    Base& operator=(Base&&) = default;

    Base(const Base&) = default;            // 提供拷贝操作的支持
    Base& operator=(const Base&) = default;
    ...
};
```

成员函数模板在任何情况下都不会抑制特殊成员函数的生成，例如下面这个类：

```cpp
class Widget {
    ...
    template<typename T>
    Widget(const T& rhs);               // 以任意类型构造 Widget

    template<typename T>
    Widget& operator=(const T& rhs);    // 以任意类型对 Widget 赋值
    ...
};
```

编译器会始终生成`Widget`的拷贝和移动操作，即使这些模板的具现化生成了拷贝构造函数和拷贝赋值运算符的函数签名。

## 第四章：智能指针

以下理由使得裸指针不受欢迎：

1. 裸指针没有在声明中指出，其指向的内容是单个对象还是数组。
2. 裸指针没有在声明中指出，是否该在其指向的对象使用完后进行析构。
3. 无法得知怎样析构裸指针才是适当的，是使用`delete`运算符，还是有专门用于析构的函数。
4. 在已知使用`delete`的情况下，难以确定该用`delete`还是`delete[]`。
5. 很难保证对指针所指向对象的析构，在所有代码路径上只执行一次。
6. 没有正规的方式来检测指针是否空悬（dangle）。

因此，在大多数时候，应该优先选用智能指针。`std::auto_ptr`是从 C++98 中残留下来的弃用特性，应该被 C++11 中的 `std::unique_ptr`所替代。

### 条款 18：使用 std::unique_ptr 管理具备专属所有权的资源

`std::unique_ptr`是小巧、高速的、具备只移类型的智能指针，对于托管的指针实施专属所有权语义。它和裸指针所占大小相同，并且不允许被拷贝，在执行析构操作时，同时析构其所管理的资源。

`std::unique_ptr`的一个常见用法是在继承体系中，作为工厂函数的返回值类型：

```cpp
class Investment { 
    ...
    virtual ~Investment();  // 必备的虚析构函数！
    ...
};

class Stock : public Investment { ... };

class Bond : public Investment { ... };

class RealEstate : public Investment { ... };

template<typename... Ts>
std::unique_ptr<Investment> makeInvestment(Ts&&... params); // 返回 std::unique_ptr
...

{
    ...
        auto pInvestment = makeInvestment( arguments );
    ...
}   // *pInvestment 在此处析构
```

默认地，资源析构采用`delete`运算符来完成，但也可以指定自定义删除器，并且删除器将会被视作`std::unique_ptr`类型的一部分。下面的例子中使用了 lambda 表达式作为自定义删除器，并在删除时写入一条日志：

```cpp
auto delInvmt = [](Investment* pInvestment) {
    makeLogEntry(pInvestment);
    delete pInvestment;
};

template<typename... Ts>
std::unique_ptr<Investment, decltype(delInvmt)> makeInvestment(Ts&&... params); // 改进后的返回值类型
```

在 C++14 中，由于有了函数返回值类型推导（参考**条款 3**），`makeInvestment`可以用更加简单的、封装性更好的方法实现，自定义删除器也可以放在函数内部，完整的代码演示如下：

```cpp
template<typename... Ts>
auto makeInvestment(Ts&&... params) {
    // 现在自定义删除器位于函数内部
    auto delInvmt = [](Investment* pInvestment) {
        makeLogEntry(pInvestment);
        delete pInvestment;
    };

    std::unique_ptr<Investment, decltype(delInvmt)> pInv(nullptr, delInvmt);    // 待返回的指针

    // 使用 reset 来让 pInv 获取 new 产生的对象的所有权
    // 对每一次 new 的调用结果，都使用 std::forward 对实参进行完美转发（参考条款 25）

    if ( /* 应创建一个 Stock 类型的对象 */ ) {
        pInv.reset(new Stock(std::forward<Ts>(params)...));
    }
    else if ( /* 应创建一个 Bond 类型的对象 */ ) {
        pInv.reset(new Bond(std::forward<Ts>(params)...));
    }
    else if ( /* 应创建一个 RealEstate 类型的对象 */ ) {
        pInv.reset(new RealEstate(std::forward<Ts>(params)...));
    }

    return pInv;
}
```

在使用自定义删除器后，`std::unique_ptr`的大小可能不再和裸指针相同：有状态的删除器和采用函数指针的删除器会增加`std::unique_ptr`类型的对象尺寸大小。无状态的函数对象（例如无捕获的 lambda 表达式）不会浪费任何内存空间，而函数指针通常会使`std::unique_ptr`的大小增加一到两个字长（word），这意味着无捕获的 lambda 表达式往往是用作删除器的最佳选择。

`std::unique_ptr`提供了两种形式，一种是单个对象（`std::unique_ptr<T>`），另一种是数组（`std::unique_ptr<T[]>`）。为了避免二义性，单个对象形式不提供索引运算符（`operator[]`），而数组形式不提供解引用运算符（`operator*`和`operator->`）。但实际上，数组形式用到的场合非常少，唯一的应用场合大概是在使用 C 风格 API 时，它返回了存放在堆上的裸指针；大部分时候我们会优先考虑`std::array`、`std::vector`和`std::string`这些数据结构。

`std::unique_ptr`可以方便高效地转换为`std::shared_ptr`：

```cpp
std::shared_ptr<Investment> sp = makeInvestment( arguments );
```

### 条款 19：使用 std::shared_ptr 管理具备共享所有权的资源

`std::shared_ptr`提供了方便的手段，实现了任意资源在共享所有权语义下进行生命周期管理的垃圾回收。与`std::unique_ptr`相比，`std::shared_ptr`所占大小通常是裸指针的两倍，它还会带来控制块的开销，并且要求成本高昂的原子化的引用计数操作。

默认的资源析构通过`delete`运算符来完成，但同时也支持自定义删除器。与`std::unique_ptr`不同的是，删除器的类型对`std::shared_ptr`的类型没有影响，也不会影响`std::shared_ptr`的尺寸大小：

```cpp
auto loggingDel = [](Widget* pw) {
    makeLogEntry(pw);
    delete pw;
};

std::unique_ptr<Widget, decltype(loggingDel)> upw(new Widget, loggingDel);
std::shared_ptr<Widget>                       spw(new Widget, loggingDel);
```

这使得`std::shared_ptr`的设计更具弹性，拥有不同类型自定义删除器的`std::shared_ptr`也可以被放在同一个容器中：

```cpp
auto customDeleter1 = [](Widget* pw) {};    // 自定义删除器
auto customDeleter2 = [](Widget* pw) {};    // 各有不同的类型

std::shared_ptr<Widget> pw1(new Widget, customDeleter1);
std::shared_ptr<Widget> pw2(new Widget, customDeleter2);

std::vector<std::shared_ptr<Widget>> vpw{ pw1, pw2 };
```

**控制块（control block）：** 每一个由`std::shared_ptr`管理的对象都拥有一个控制块，它的内存被动态分配在堆上，除了包含引用计数以外，还包含作用于`std::weak_ptr`的弱计数（参考**条款 20**），自定义删除器和分配器等内容。

一个对象的控制块应该在创建首个指向该对象的`std::shared_ptr`时确定，因此，控制块的创建遵循以下规则：

1. 使用`std::make_shared`（参考**条款 21**）总是会创建一个控制块。
2. 从具备专属所有权的指针（`std::unique_ptr`或`std::auto_ptr`）出发构造一个`std::shared_ptr`时，会创建一个控制块。
3. 用裸指针作为实参调用`std::shared_ptr`的构造函数时，会创建一个控制块。

由以上规则我们可以得出，应该避免使用裸指针类型的变量来创建`std::shared_ptr`。用同一个裸指针构造出不止一个`std::shared_ptr`将会使对象拥有多重的控制块，这会导致对资源的多次析构，从而产生未定义行为，如下所示：

```cpp
auto pw = new Widget; // pw 是个裸指针
...
std::shared_ptr<Widget> spw1(pw, loggingDel);
...
std::shared_ptr<Widget> spw2(pw, loggingDel);
```

应该改为：

```cpp
std::shared_ptr<Widget> spw1(new Widget, loggingDel);
...
std::shared_ptr<Widget> spw2(spw1);
```

当你希望一个托管到`std::shared_ptr`的类能够安全地由`this`指针创建一个`std::shared_ptr`时，应该使该类继承自`std::enable_shared_from_this`，如下所示：

```cpp
class Widget : public std::enable_shared_from_this<Widget> {
public:
    ...
    void process();
    ...
};
```

`std::enable_shared_from_this`定义了一个成员函数`std::shared_from_this`，它会创建一个`std::shared_ptr`指向当前对象，但不会重复创建控制块：

```cpp
std::vector<std::shared_ptr<Widget>> processedWidget;

void Widget::process() {
    // 处理对象本身
    ...

    // 将指向当前对象的 std::shared_ptr 加入 processedWidget
    processedWidget.emplace_back(shared_from_this());
}
```

为了避免用户在`std::shared_ptr`指向该对象前就调用了`std::shared_from_this`（这会导致其无法查询到对象拥有的控制块，产生未定义行为），继承自`std::enable_shared_from_this`的类通常会将其构造函数声明为 private，并且只允许通过调用返回`std::shared_ptr`的工厂函数来创建对象。例如，以下是`Widget`类的一个可能实现：

```cpp
class Widget : public std::enable_shared_from_this<Widget> {
public:
    // 将实参完美转发给 private 构造函数的工厂函数
    template<typename... Ts>
    static std::shared_ptr<Widget> create(Ts&&... params);
    ...
    void process();
    ...

private:
    Widget( ... );  // 构造函数
};
```

`std::unique_ptr`可以轻易转换为`std::shared_ptr`，反之却并不成立，一旦资源的生存期被托管给了`std::shared_ptr`，就不能回收该资源的所有权，并让一个`std::unique_ptr`来托管它。并且和`std::unique_ptr`不同，`std::shared_ptr`直到 C++17 才拥有处理数组的能力（`std::shared_ptr<T[]>`），在 C++11/14 中，它的 API 仅被设计用来处理指向单个对象的指针。

### 条款 20：对于类似 std::shared_ptr 但有可能空悬的指针使用 std::weak_ptr

`std::weak_ptr`并不是一种独立的智能指针，而是`std::shared_ptr`的一种扩充。它一般是通过`std::shared_ptr`来创建的，两者会指向相同位置，但`std::weak_ptr`并不影响所指向对象的引用计数，而是会影响控制块中的弱计数。

使用`expired`函数来检测`std::weak_ptr`的空悬：

```cpp
auto spw = std::make_shared<Widget>();
std::weak_ptr<Widget> wpw(spw);
...

spw = nullptr;      // Widget 对象被析构，wpw 空悬

if (wpw.expired())  // 若 wpw 不再指向任何对象
    ...
```

通过`std::weak_ptr`创建`std::shared_ptr`，可以在未失效情况下提供对资源的访问：

```cpp
// 使用 lock 函数时，若 wpw 失效，则 spw1 和 spw2 为空
std::shared_ptr<Widget> spw1 = wpw.lock();
auto spw2 = wpw.lock();

// 直接构造时，若 wpw 失效，则抛出 std::bad_weak_ptr 类型的异常
std::shared_ptr<Widget> spw3(wpw);
```

`std::weak_ptr`有以下可能的用武之地：

- 创建带缓存的工厂函数：

```cpp
std::shared_ptr<const Widget> fastLoadWidget(WidgetID id) {
    static std::unordered_map<WidgetID, std::weak_ptr<const Widget>> cache;

    auto objPtr = cache[id].lock(); // 如果对象不在缓存中，则返回空指针

    if (!objPtr) {                  // 加载并缓存对象
        objPtr = loadWidget(id);
        cache[id] = objPtr;
    }
    return objPtr;
}
```

- 观察者设计模式（Observer design pattern）：多个观察者（observer）对象同时监听一个主题（subject）对象，主题对象会在其发生状态改变时发出通知。主题对象不会控制其观察者的生存期，但需要确认当一个观察者对象被析构后，主题对象不会再访问它。一种合理的设计就是让每个主题对象持有指向其观察者对象的`std::weak_ptr`，以便在使用之前确认它是否空悬。
- 避免`std::shared_ptr`循环引用：

```cpp
class A {
public:
    ...
    std::shared_ptr<B> pb;
};

class B {
public:
    ...
    std::shared_ptr<A> pa;
};
...

auto pa = std::make_shared<A>();
auto pb = std::make_shared<B>();

pa->pb = pb;
pb->pa = pa;
```

在这种情况下，`A`和`B`互相保存着指向对方的`std::shared_ptr`，产生了循环引用，两者会永久保持彼此的引用计数至少为一，这会阻止`A`和`B`被析构，实际上产生了内存泄漏。

将其中一者改为`std::weak_ptr`可以避免循环的产生：

```cpp
class A {
public:
    ...
    std::shared_ptr<B> pb;
};

class B {
public:
    ...
    std::weak_ptr<A> pa;
};
```

### 条款 21：优先选用 std::make_unique 和 std::make_shared，而非直接使用 new

`std::make_shared`是 C++11 的一部分，但`std::make_unique`到了 C++14 才被加入标准库，不过要写出一个基础版本的`std::make_unique`非常容易：

```cpp
template<typename T, typename... Ts>
std::unique_ptr<T> make_unique(Ts&&... params) {
    return std::unique_ptr<T>(new T(std::forward<Ts>(params)...));
}
```

相对于直接使用`new`运算符，make 函数有以下优势：

- 消除重复代码：

```cpp
auto upw1(std::make_unique<Widget>());      // 使用 make 函数
std::unique_ptr<Widget> upw2(new Widget);   // 不使用 make 函数

auto spw1(std::make_shared<Widget>());      // 使用 make 函数
std::shared_ptr<Widget> spw2(new Widget);   // 不使用 make 函数
```

- 改进了异常安全性：

```cpp
processWidget(std::shared_ptr<Widget>(new Widget), computePriority());  // 有潜在的内存泄漏风险

processWidget(std::make_shared<Widget>(), computePriority());           // 不具有潜在的内存泄漏风险
```

在直接使用`new`运算符的情况下，由于分配`Widget`对象、执行`std::shared_ptr`构造函数、执行`computePriority`函数三者并不存在固定顺序，`computePriority`函数可能会晚于`Widget`对象的分配，先于`std::shared_ptr`的构造函数执行，此时若`computePriority`产生异常，那么分配的`Widget`内存就会发生泄漏。使用`std::make_shared`则不会产生这个问题。

- 使用`std::make_shared`和`std::allocate_shared`有助于生成的尺寸更小、速度更快的目标代码。

> `std::make_shared`会将指向的对象和与其相关联的控制块分配在单块内存中，这种优化减少了程序的静态尺寸，并且因为只进行一次内存分配，还可以加块代码的运行速度。使用`std::make_shared`还可以减少对控制块一些簿记信息（bookkeeping information）的需要，潜在地减少了程序的内存占用量（memory footprint）。`std::allocate_shared`也是同理。

虽然有着如此多的优势，但还是有一些情形下，不能或者不应该使用 make 函数：

- 使用 make 函数无法自定义删除器，以及直接传递大括号内的初始值设定项：

```cpp
// 想要自定义删除器，就只能使用 new 运算符
std::unique_ptr<Widget, decltype(widgetDeleter)> upw(new Widget, widgetDeleter);
std::shared_ptr<Widget>                          spw(new Widget, widgetDeleter);

// 只能间接传递初始化列表给 make 函数
auto initList = { 10, 20 };
auto spv = std::make_shared<std::vector<int>>(initList);
```

- 不建议对自定义内存管理方式的类使用 make 函数：通常情况下，类自定义的`operator new`和`operator delete`被设计成用来分配和释放能精确容纳该类大小的内存块，但`std::allocate_shared`所要求的内存大小并不等于动态分配对象的大小，而是在其基础上加上控制块的大小。因此，使用 make 函数去创建重载了`operator new`和`operator delete`类的对象，通常并不是个好主意。
- 当处于特别关注内存的系统中时，若存在非常大的对象和比相应的`std::shared_ptr`生存期更久的`std::weak_ptr`，不建议使用 make 函数：这会导致对象的析构和内存的释放之间产生延迟，而若直接使用`new`运算符，内存的释放就不必等待`std::weak_ptr`的析构。

如果你发现自己处于不应该使用`std::make_shared`的情形下，又不想受到之前所述异常安全问题的影响。最好的方法是确保在直接使用`new`时，立即将结果传递给智能指针的构造函数，并且在这条语句中不做其它任何事：

```cpp
std::shared_ptr<Widget> spw(new Widget, cusDel);

processWidget(std::move(spw), computePriority());
```

### 条款 22：使用 Pimpl 惯用法时，将特殊成员函数的定义放到实现文件中

Pimpl 惯用法的第一部分，是声明一个指针类型的数据成员，指向一个非完整类型：

```cpp
class Widget {
    Widget();
    ~Widget();
    ...

private:
    struct Impl;
    Impl* pImpl;
};
```

第二部分是动态分配和回收持有原始类中数据成员的对象，而分配和回收的代码被放在实现文件中：

```cpp
struct Widget::Impl {                   // Widget::Impl 的实现
    std::string name;                   // 包含在原始 Widget 类中的数据成员
    std::vector<double> data;
    Gadget g1, g2, g3;
};

Widget::Widget() : pImpl(new Impl) {}   // 为 Widget 对象分配数据成员所需内存

Widget::~Widget() { delete pImpl; }     // 为 Widget 对象析构数据成员
```

上面展示的是 C++98 的写法，使用了裸指针、裸`new`运算符和裸`delete`运算符。而到了 C++11，使用`std::unique_ptr`替代指向`Impl`的裸指针成为了首选：

```cpp
// 声明代码位于头文件 widget.h 内

class Widget {
    Widget();
    ...

private:
    struct Impl;
    std::unique_ptr<Impl> pImpl;
};

// 实现代码位于实现文件 widget.cpp 内

struct Widget::Impl {   // 同前
    std::string name;
    std::vector<double> data;
    Gadget g1, g2, g3;
};

Widget::Widget() : pImpl(std::make_unique<Impl>()) {}
```

遗憾的是，这段代码本身能通过编译，但在创建对象时却会报错。因为编译器自动生成的析构函数默认是`inline`的，而`std::unique_ptr`的默认删除器要求其指向完整类型，所以即使默认特殊函数的实现有着正确行为，我们仍必须将其声明和实现分离：

```cpp
// 声明代码位于头文件 widget.h 内

class Widget {
    Widget();
    ~Widget()
    ...

private:
    struct Impl;
    std::unique_ptr<Impl> pImpl;
};

// 实现代码位于实现文件 widget.cpp 内

...

Widget::Widget() : pImpl(std::make_unique<Impl>()) {}

Widget::~Widget() {}    // 写成 Widget::~Widget() = default; 效果相同
```

在**条款 17** 中我们提到，声明析构函数会阻止编译器生成移动操作，所以假如你需要支持移动操作，也必须采用声明和实现分离的方法：

```cpp
// 声明代码位于头文件 widget.h 内

class Widget {
    ...
    Widget(Widget&& rhs);
    Widget& operator=(Widget&& rhs);
    ...
};

// 实现代码位于实现文件 widget.cpp 内

...

Widget::Widget(Widget&& rhs) = default;
Widget& Widget::operator=(Widget&& rhs) = default;
```

编译器不会为带有`std::unique_ptr`这种只移类型的类生成拷贝操作，假如你需要支持拷贝操作，则需要自行编写执行深拷贝的函数实现，并且同样需要遵守前面所说的规则：

```cpp
// 声明代码位于头文件 widget.h 内

class Widget {
    ...
    Widget(const Widget& rhs);
    Widget& operator=(const Widget& rhs);
    ...
};

// 实现代码位于实现文件 widget.cpp 内

...

Widget::Widget(const Widget& rhs)
    : pImpl(std::make_unique<Impl>(*rhs.pImpl)) {}

Widget& Widget::operator=(const Widget& rhs) {
    *pImpl = *rhs.pImpl;
    return *this;
}
```

上述建议仅仅适用于`std::unique_ptr`，而不适用于`std::shared_ptr`。对于`std::shared_ptr`而言，删除器类型并非智能指针类型的一部分，这就会导致更大的运行时数据结构以及更慢的目标代码，但在使用编译器生成的特殊函数时，并不要求其指向完整类型。以下代码并不会产生问题：

```cpp
class Widget {
    Widget();
    ...         // 不再需要析构函数或移动操作的声明

private:
    struct Impl;
    std::shared_ptr<Impl> pImpl;
};
```

就 Pimpl 惯用法而言，并不需要在`std::unique_ptr`和`std::shared_ptr`的特性之间作出权衡，因为`Widget`和`Impl`之间的关系是专属所有权，所以在此处`std::unique_ptr`就是完成任务的合适工具。

## 第五章：右值引用、移动语义和完美转发

在阅读本章中的条款时，需要铭记一点：形参总是左值，即使其类型是右值引用。例如给定函数：

```cpp
void f(Widget&& w);
```

尽管形参`w`的类型是指向`Widget`对象的右值引用，可以传入绑定到右值的实参，但它仍然是个左值。

### 条款 23：理解 std::move 和 std::forward

`std::move`执行的是向右值的无条件强制类型转换，就其自身而言，它不会移动任何东西。它的基本实现大致是这样的：

```cpp
// C++11 版本
template<typename T>
typename remove_reference<T>::type&& move(T&& param) {
    using ReturnType = typename remove_reference<T>::type&&;
    return static_cast<ReturnType>(param);
}

// C++14 版本
template<typename T>
decltype(auto) move(T&& param) {
    using ReturnType = remove_reference_t<T>&&;
    return static_cast<ReturnType>(param);
}
```

`std::move`并不改变常量性，也不保证经过其强制类型转换后的对象可被移动，针对常量对象执行的移动操作可能会悄无声息地转化为拷贝操作，如下所示：

```cpp
class Annotation {
public:
    explicit Annotation(const std::string text)
        : value(std::move(text)) { ... }    // 想要将 text “移动入” value
    ...                                     // 但实际上执行了 std::string 的拷贝构造函数
                                            // 而非移动构造函数 string(string&&)
private:
    std::string value;
};
```

因此，如果想要取得对某个对象执行移动操作的能力，就不要将其声明为常量。

与`std::move`不同，`std::forward`是有条件的。仅当传入的实参被绑定到右值时，`std::forward`才会针对该实参执行向右值的强制类型转换，它同样不会转发任何东西。

`std::forward`的一个典型应用场景，是某个函数模板使用万能引用作为形参，随后将其传递给另一个函数：

```cpp
void process(const Widget& lvalArg);    // 处理左值
void process(Widget&& rvalArg);         // 处理右值

template<typename T>
void logAndProcess(T&& param) {         // 使用万能引用作为实参
    auto now = std::chrono::system_clock::now();
    makeLogEntry("Calling 'process'", now);

    process(std::forward<T>(param));
}
```

若在调用`logAndProcess`时传入左值，那么该左值自然会传递给处理左值版本的`process`函数；若在调用`logAndProcess`时传入右值，由于函数形参皆为左值，必须要通过`std::forward`将`param`强制转换为右值类型，才能得以正确调用处理右值版本的`process`函数。`std::forward`会通过模板类型`T`来判断是否该对`param`进行强制类型转换，具体的原理细节参考**条款 28**。

尽管`std::move`和`std::forward`归根结底都是强制类型转换，但两者的行为具有本质上的不同：前者用于为移动操作进行铺垫，而后者仅仅用于转发一个对象到另一个函数，在此过程中该对象仍保持原来的左值性或右值性。

> 需要注意的是，在运行期，`std::move`和`std::forward`都不会做任何操作。

### 条款 24：区分万能引用和右值引用

如果函数模板形参的类型为`T&&`，并且 T 的类型需要推导得到，或一个对象使用`auto&&`声明其类型，则此处的`T&&`和`auto&&`表示**万能引用（universal reference）**；如果类型声明不是标准的`type&&`形式，或者并未发生类型推导，则此处的`type&&`表示右值引用。

符合万能引用的情形如下：

```cpp
auto&& var2 = var1;

template<typename T>
void f(T&& param);

template<class T, class Allocator = allocator<T>>
class vector {
public:
    template<class... Args>
    void emplace_back(Args&&... args);
    ...
};

auto timeFuncInvocation = [](auto&& func, auto&&... params) {   // C++14
    std::forward<decltype(func)>(func)(                         // 调用 func
        std::forward<decltype(params)>(params)...               // 取用 params
    );
};
```

类型声明不是标准`type&&`的情形如下：

```cpp
template<typename T>
void f(std::vector<T>&& param); // param 是右值引用

template<typename T>
void f(const T&& param);        // param 是右值引用
```

类型是`T&&`，但并未发生类型推导的情形如下：

```cpp
template<class T, class Allocator = allocator<T>>
class vector {
public:
    void push_back(T&& x);  // x 是右值引用
    ...
};
```

若使用右值来初始化万能引用，就会得到一个右值引用；同理，若使用左值来初始化万能引用，就会得到一个左值引用。如下所示：

```cpp
template<typename T>
void f(T&& param);  // param 是万能引用

Widget w;
f(w);               // 左值被传递给 f，param 的类型为 Widget&

f(std::move(w));    // 右值被传递给 f，param 的类型为 Widget&&
```

### 条款 25：针对右值引用实施 std::move，针对万能引用实施 std::forward

右值引用一定会被绑定到右值，因此当转发右值引用给其他函数时，应当通过`std::move`对其实施向右值的无条件强制类型转换：

```cpp
class Widget {
public:
    Widget(Widget&& rhs)
        : name(std::move(rhs.name)),
          p(std::move(rhs.p)) { ... }
    ...

private:
    std::string name;
    std::shared_ptr<SomeDataStructure> p;
};
```

而万能引用不一定会被绑定到右值，因此当转发万能引用时，应当通过`std::forward`对其实施向右值的有条件强制类型转换：

```cpp
class Widget {
public:
    template<typename T>
    void setName(T&& newName) {
        name = std::forward<T>(newName);
    }
    ...
};
```

虽然针对右值引用实施`std::forward`也能硬弄出正确行为，但代码啰嗦、易错，且不符合习惯用法；而针对万能引用实施`std::move`会造成更加严重的后果，这会导致某些左值遭受意外的改动：

```cpp
class Widget {
public:
    template<typename T>
    void setName(T&& newName) {
        name = std::move(newName);
    }
    ...

private:
    std::string name;
    std::shared_ptr<SomeDataStructure> p;
};

std::string getWidgetName();    // 工厂函数

Widget w;
auto n = getWidgetName();
w.setName(n);               // 将 n 移入 w
...                         // n 的值变为未知
```

一种手法是将万能引用的版本改成对左值和右值分别进行重载：

```cpp
class Widget {
public:
    void setName(const std::string& newName) {
        name = newName;
    }

    void setName(std::string&& newName) {
        name = std::move(newName);
    }
    ...
};
```

这种手法虽然看似可以解决问题，但是拥有更大的缺点：第一，需要编写和维护更多源码；第二，效率会大打折扣（产生额外的临时对象）；第三，可扩展性太差。因此，正确地使用万能引用才是问题的唯一解决之道。

在有些情况下，你可能想在函数内将某个对象不止一次地绑定到右值引用或万能引用，并且想保证在完成对该对象地其它所有操作之前，其值不会发生移动，那么就得仅在最后一次使用该引用时，对其实施`std::move`或`std::forward`：

```cpp
template<typename T>
void setSignText(T&& text) {
    sign.setText(text);                         // 使用 text，但不修改其值

    auto now = std::chrono::system_clock::now();

    signHistory.add(now, std::forward<T>(now)); // 有条件地将 text 强制转换为右值
}
```

在极少数的情况下，你需要用`std::move_if_noexcept`来代替`std::move`。

> `std::move_if_noexcept`是`std::move`的一个变体，它是否会将对象强制转换为右值，取决于其类型的移动构造函数是否带有 noexcept 声明。

在按值返回的函数中，如果返回的是绑定到右值引用或万能引用的对象，则当你返回该引用时，应当对其实施`std::move`或`std::forward`，这样可以避免编译器将其视作左值，从而消除拷贝左值进入返回值存储位置的额外开销：

```cpp
// 按值返回右值引用形参
Matrix operator+(Matrix&& lhs, const Matrix& rhs) {
    lhs += rhs;
    return std::move(lhs);
}

// 按值返回万能引用形参
template<typename T>
Fraction reduceAndCopy(T&& frac) {
    frac.reduce();
    return std::forward<T>(frac);
}
```

但是若局部对象可能适用于**返回值优化（return value optimization，RVO）**，则请勿对其实施`std::move`或`std::forward`。这是因为当 RVO 的前提条件得到满足时，要么发生**拷贝省略（copy elision）**，要么`std::move`会隐式地被实施于返回的局部对象上；而人为地添加`std::move`或`std::forward`，会导致编译器失去执行 RVO 的能力。

下面的`makeWidget`函数满足 RVO 的两个前提条件：局部对象类型和函数返回值类型相同，且返回的就是局部对象本身：

```cpp
Widget makeWidget(Widget w) {
    ...
    return w;
}
```

但由于函数形参不适合实施拷贝省略，所以编译器必须处理以上代码，使其与以下代码等价：

```cpp
Widget makeWidget(Widget w) {
    ...
    return std::move(w);
}
```

### 条款 26：避免对万能引用类型进行重载

形参为万能引用的函数是 C++ 中最贪婪的，它们会在具现过程中和几乎所有实参类型产生精确匹配（极少的不适用实参将在**条款 30** 中介绍），这就是为何把重载和万能引用两者结合通常不会达到预期效果。考虑如下情形：

```cpp
template<typename T>
void logAndAdd(T&& name) {
    auto now = std::chrono::system_clock::now();
    log(now, "logAndAdd");
    names.emplace(std::forward<T>(name));
}

std::string nameFromIdx(int idx);
void logAndAdd(int idx) {
    auto now = std::chrono::system_clock::now();
    log(now, "logAndAdd");
    names.emplace(nameFromIdx(idx));
}
...

short nameIdx;      // 用 short 类型持有索引值
...

logAndAdd(nameIdx); // 调用的却是万能引用版本
```

`logAndAdd`有两个重载版本，形参类型为万能引用的版本可以将`T`推导为`short`，从而产生精确匹配；而形参类型为`int`的版本却只能在类型提升后才可以匹配到`short`类型的实参。因此，形参类型为万能引用的版本才是被优先调用的版本。

当完美转发出现在类的构造函数中时，情况会变得更加复杂：

```cpp
class Person {
public:
    template<typename T>        // 完美转发构造函数
    explicit Person(T&& n)
        : name(std::forward<T>(n)) {}

    explicit Person(int idx);   // 形参为 int 的构造函数

    Person(const Person& rhs);  // 拷贝构造函数（由编译器生成）

    Person(Person&& rhs);       // 移动构造函数（由编译器生成）
    ...

private:
    std::string name;
};
```

对于非常量的左值类型，完美转发构造函数一般都会优先于拷贝构造函数形成匹配；而对于常量左值类型，完美转发构造函数和拷贝构造函数具有相等的匹配程度，此时由于非函数模板会优先于函数模板被匹配，编译器才会转向调用拷贝构造函数：

```cpp
Person p("Nancy");
auto cloneOfP(p);           // 调用完美转发构造函数，无法通过编译

const Person cp("Nancy");   // 对象成为了常量
auto cloneOfCp(cp);         // 会正确调用拷贝构造函数
```

完美转发构造函数还会劫持派生类中对基类的拷贝和移动构造函数的调用：

```cpp
class SpecialPerson : public Person {
public:
    SpecialPerson(const SpecialPerson& rhs) // 拷贝构造函数
        : Person(rhs) {}                    // 调用的是基类的完美转发构造函数！

    SpecialPerson(SpecialPerson&& rhs)      // 移动构造函数
        : Person(std::move(rhs)) {}         // 调用的是基类的完美转发构造函数！
};
```

### 条款 27：熟悉对万能引用类型进行重载的替代方案

**1. 放弃重载**

**2. 传递`const T&`类型的形参**

```cpp
void logAndAdd(const std::string& name) {
    auto now = std::chrono::system_clock::now();
    log(now, "logAndAdd");
    names.emplace(name);
}
```

这种做法可以避免重载万能引用带来的不良影响，但会舍弃一些性能。

**3. 传值**

把传递的形参从引用类型换成值类型，是一种经常能够提升性能，却不会增加任何复杂性的方法，尽管这有些反直觉。这种设计遵循了**条款 41** 的建议——当你知道肯定需要复制形参时，考虑按值传递对象：

```cpp
class Person {
public:
    explicit Person(std::string n)  // 替换掉 T&& 类型的构造函数
        : name(std::move(n)) {}

    explicit Person(int idx)        // 同前
        : name(nameFromIdx(idx)) {}

private:
    std::string name;
};
```

**4. 使用标签分派（tag dispatch）**

在这个方案中，我们将实际重载和实现功能的函数改为`logAndAddImpl`，而`logAndAdd`仅仅用于执行完美转发和标签分派：

```cpp
template<typename T>
void logAndAdd(T&& name) {
    logAndAddImpl(std::forward<T>(name),
        std::is_integral<typename std::remove_reference<T>::type>());    // C++14 可以使用 std::remove_reference_t
}
```

针对`std::is_integral`产生的布尔值，我们可以写出两个`logAndAddImpl`重载版本，用于区分它们的类型`std::false_type`和`std::true_type`就是所谓 “标签”。具体实现代码如下：

```cpp
template<typename T>
void logAndAddImpl(T&& name, std::false_type) { // 非整型实参
    auto now = std::chrono::system_clock::now();
    log(now, "logAndAdd");
    names.emplace(std::forward<T>(name));
}

std::string nameFromIdx(int idx);
void logAndAddImpl(int idx, std::true_type) {   // 整型实参
    logAndAdd(nameFromIdx(idx));
}
```

**5. 对接受万能引用的模板施加限制**

通过 SFINAE 技术和`std::enable_if`，我们可以让一些模板在满足了指定条件的情况下才被启用，它的使用方式大致如下：

```cpp
class Person {
public:
    template<typename T,
             typename = typename std::enable_if<condition>::type>    // C++14 可以使用 std::enable_if_t
    explicit Person(T&& n);
    ...
};
```

此处我们想要`T`是`Person`以外的类型时，才启用该模板构造函数，则我们可以写下面这样的条件：

```cpp
!std::is_same<Person, typename std::decay<T>::type>::value  // C++17 可以使用 std::is_same_v
```

`std::decay`用于使类型完全退化，在此处用来移除`T`的引用和 cv 限定符（即`const`或`volatile`限定符），使我们可以更加纯粹地关注类型本身。`std::decay`还可以用于把数组和函数类型强制转换为指针类型（参考**条款 1**），它当然也拥有更易用的 C++14 版本，即`std::decay_t`。

写出这个条件并不意味着完成，**条款 26** 中还提到了在派生类中调用基类的拷贝和移动构造函数时，错误调用完美转发构造函数的问题。因此，我们想要的是为`Person`和继承自`Person`的类型都不一样的实参类型启用模板构造函数。标准库中的`std::is_base_of`用于判断一个类型是否由另一个类型派生而来，用它代替`std::is_same`就可以得到我们想要的东西（C++17 可以使用`std::is_base_of_v`）：

```cpp
class Person {
public:
    template<
        typename T,
        typename = typename std::enable_if<
                       !std::is_base_of<Person, 
                                        typename std::decay<T>::type
                                       >::value
                   >::type
    >
    explicit Person(T&& n);
    ...
};
```

再加上处理整型实参的构造函数重载版本，并进一步限制模板构造函数，禁止其接受整型实参，我们得到的完美的`Person`类代码如下所示：

```cpp
class Person {
public:
    template<
        typename T,
        typename = std::enable_if_t<
            !std::is_base_of<Person, std::decay_t<T>>::value
            &&
            !std::is_integral<std::remove_reference_t<T>>::value
        >
    >
    explicit Person(T&& n)              // 接受 std::string 类型以及可以强制转换为 
    : name(std::forward<T>(n)) { ... }  // std::string 类型的实参的构造函数

    explicit Person(int idx)            // 接受整型实参的构造函数
    : name(nameFromIdx(idx)) { ... }

    ...                                 // 拷贝和移动构造函数等

private:
    std::string name;
};
```

**权衡**

本条款讨论的前三种方案（舍弃重载，传递`const T&`类型的形参和传值）都需要对待调用的函数形参逐一指定类型，而后两种方案（使用标签分派和对接受万能引用的模板施加限制）则使用了完美转发，因此无需指定形参类型。

按照常理，完美转发的效率更高，因为它将类型保持和形参声明时完全一致，所以会避免创建临时对象。但完美转发也有一些不足：首先是针对某些类型无法实现完美转发（参考**条款 30**），其次是完美转发会使得在传递非法形参时，出现更难理解的错误信息。

`std::is_constructible`这个类型特征（type trait）可以在编译期判断某个类型的对象是否可以用另一类型的对象（或不同类型的多个对象）来构造，我们可以用它来验证转发函数的万能引用形参是否合法。下面是增加了`static_assert`后的`Person`类，它可以产生更明确的报错信息：

```cpp
class Person {
public:
    template<typename T, typename = std::enable_if_t<...>>  // 同前
    explicit Person(T&& n)
        : name(std::forward<T>(n)) {
        // 断言可以用 T 类型的对象构造 std::string
        static_assert(
            std::is_constructible<std::string, T>::value,   // C++17 可以使用 std::is_constructible_v
            "Parameter n can't be used to construct a std::string"
        );

        ... // 构造函数通常要完成的工作
    }
    ...
};
```

### 条款 28：理解引用折叠

在**条款 24** 中我们了解了万能引用和右值引用的区别，但实际上万能引用并非一种新的引用类型，其实它就是在满足下面两个条件的语境中的右值引用：

1. 类型推导的过程中会区分左值和右值；
2. 会发生**引用折叠（reference collapsing）**。

C++ 标准禁止直接声明 “引用的引用” ，但引用折叠不受此限制。当左值被传递给接受万能引用的函数模板时，会发生下面这样的状况：

```cpp
template<typename T>
void func(T&& param);

func(w);    // 调用 func 并传入左值，T 推导出的类型为 Widget&
```

代入`T`的推导类型，我们可以得到下面这样的具现化模板：

```cpp
void func(Widget& && param);
```

出现了引用的引用！然而这并不违规，引用折叠的规则会把双重引用折叠成单个引用，规则如下：

> 如果任一引用为左值引用，则结果为左值引用，否则（即两个皆为右值引用），结果为右值引用。

所以实际上的函数签名为：

```cpp
void func(Widget& param);
```

引用折叠是使`std::forward`得以运作的关键，也是将左值或右值信息编码到万能引用形参`T`中的实现途径。`std::forward`的任务是，当且仅当编码在`T`中的信息表明传递的实参是右值，即`T`推导出的类型是个非引用类型时，对左值形参实施到右值的强制类型转换。它的基本实现大致是这样的：

```cpp
// C++11 版本
template<typename T>
T&& forward(typename remove_reference<T>::type& param) {
    return static_cast<T&&>(param);
}

// C++14 版本
template<typename T>
T&& forward(remove_reference_t<T>& param) {
    return static_cast<T&&>(param);
}
```

假设传递给函数`forward`的实参类型是左值`Widget`，则模板的具现化结果可以写成：

```cpp
Widget& && forward(remove_reference_t<Widget&>& param) {
    return static_cast<Widget& &&>(param);
}
```

发生引用折叠后，生成的最终结果如下，可以看出左值类型并不会发生改变：

```cpp
Widget& forward(Widget& param) {
    return static_cast<Widget&>(param);
}
```

假设传递给函数`forward`的实参类型是右值`Widget`，则模板的具现化结果可以写成：

```cpp
Widget&& forward(remove_reference_t<Widget>& param) {
    return static_cast<Widget&&>(param);
}
```

发生引用折叠后，生成的最终结果如下，显然左值类型的形参会被强制转换为右值：

```cpp
Widget&& forward(Widget& param) {
    return static_cast<Widget&&>(param);
}
```

引用折叠会在四种语境中出现：模板具现化，`auto`类型推断，创建和使用`typedef`和别名声明，以及`decltype`。

`auto`类型推断中发生的引用折叠：

```cpp
Widget w;                       // 变量（左值）
Widget widgetFunction();        // 返回右值的函数

auto&& w1 = w;                  // 推导出 Widget& && w1，引用折叠后为 Widget& w1
auto&& w2 = widgetFunction();   // 推导出 Widget&& w1，不会发生引用折叠
```

创建和使用`typedef`中发生的引用折叠：

```cpp
template<typename T>
class Widget {
public:
    typedef T&& RvalueRefToT;
    ...
};

Widget<int&> w; // 用左值引用类型来具现化 Widget 模板

// 具现化后得到 typedef int& && RvalueRefToT
// 引用折叠后为 typedef int& RvalueRefToT
```

最后一种会发生引用折叠的语境在`decltype`的运用中：如果在分析一个涉及`decltype`的类型时出现了引用的引用，则引用折叠会介入并将其消灭。

### 条款 29：假定移动操作不存在、成本高、未被使用

在下面几个情形下，C++11 的移动语义不会给你带来什么好处：

- **没有移动操作：** 待移动的对象未能提供移动操作。因此，移动请求就变成了拷贝请求。
- **移动未能更快：** 待移动的对象虽然有移动操作，但并不比其拷贝操作更快。

> 移动操作不比拷贝操作更快的例子：`std::array`将数据直接存储在对象内，移动`std::array`需要逐个移动容器内的每个元素；开启了**短字符串优化（small string optimization，SSO）** 的`std::string`，它会将字符串存储在`std::string`对象的某个缓冲区内，而非使用堆上的内存。

- **移动不可用：** 移动本可以发生的情况下，要求移动操作不会抛出异常，但该操作未加上`noexcept`声明。
- **源对象是左值：** 只有右值可以作为移动操作的源。

因此，我们应该为通用的代码假定移动操作不存在、成本高且未被使用。然而，对于已知的类型或支持移动语义的代码中，就不需要作上述假定，在你知道移动操作成本低廉的情况下，可以放心大胆地将拷贝操作替换为相对不那么昂贵的移动操作。

### 条款 30：熟悉完美转发的失败情形

完美转发的含义是我们不仅转发对象，还转发其特征：类型，是左值还是右值，以及是否带有`const`和`volatile`限定符。出于此目的，我们会运用万能引用来将左、右值信息编码到类型中，而用于转发的函数自然也该是泛型的，它的标准形式如下：

```cpp
template<typename T>
void fwd(T&& param) {
    f(std::forward<T>(param));
}
```

为了使转发函数能接受任意数量的形参，使用可变参数模板也在我们的考虑范围内：

```cpp
template<typename... Ts>
void fwd(Ts&&... param) {
    f(std::forward<Ts>(param)...);
}
```

若用相同实参调用`f`和`fwd`会执行不同的操作，则称`fwd`将实参完美转发到`f`失败。完美转发的失败情形源于模板类型推导失败，或推导出错误的类型。下面我们将了解会造成完美转发失败的典型例子。

**大括号初始值设定项（Braced initializers）**

```cpp
void f(const std::vector<int>& v);

f({ 1, 2, 3 });         // “{ 1, 2, 3 }” 会隐式转换为 std::vector<int>
fwd({ 1, 2, 3 });       // 无法通过编译！
```

由于`fwd`的形参为被声明为`std::initializer_list`，编译器就会被禁止在`fwd`的调用过程中从表达式`{ 1, 2, 3 }`出发来推导类型。既然无法推导出形参的类型，那么编译器也只能拒绝对`fwd`的调用。

我们可以通过先用`auto`声明一个局部变量，再传递给`fwd`来避免这个问题：

```cpp
auto il = { 1, 2, 3 };  // il 的类型被推导为 std::initializer_list<int>
fwd(il);                // 没问题，将 il 完美转发给 f
```

**0 和 NULL 作空指针**

**条款 8** 中曾经说明过，当你试图将`0`或`NULL`作为空指针传递给模板时，类型推导就会发生错误，将实参推导为一个整型而非指针类型，结果不管是`0`还是`NULL`都不会作为空指针被完美转发。解决方法非常简单，传一个`nullptr`而不是`0`或`NULL`。

**仅有声明的整型`static const`成员变量**

```cpp
class Widget {
public:
    static const std::size_t MinVals = 28;  // 仅提供 MinVals 的声明
    ...
};                                          // 未给出 MinVals 的定义

void f(std::size_t v);

f(Widget::MinVals);                         // 没问题，当作 “f(28)” 处理
fwd(Widget::MinVals);                       // 可能无法通过链接
```

一般而言，编译器会绕过`MinVals`缺少定义的事实，并用其值替换所有涉及到`MinVals`的地方，但并不会为其实际分配存储空间。这就导致如果我们尝试对`MinVals`实施取地址或对它进行引用，就会导致链接无法通过，这也就是为何不能将`Widget::MinVals`作为实参传递给`fwd`。

按照标准，按引用传递`MinVals`时要求`MinVals`有定义。然而并不是所有实现都遵循了这个规定，对于一些编译器和链接器，你会发现将`MinVals`用于完美转发并不会产生错误，甚至对它取地址也不会。但为了代码的可移植性，还是应当重视此处所讲的规则，为`static const`成员变量提供定义：

```cpp
const std::size_t Widget::MinVals;  // 在 Widget 的 .cpp 文件中
```

**重载函数的名称和模板名称**

```cpp
void f(int (*pf)(int)); // 或者 void f(int pf(int))

int processVal(int value);
int processVal(int value, int priority);

f(processVal);          // 没问题
fwd(processVal);        // 错误！无法确定是哪个重载版本

template<typename T>
T workOnVal(T param) { ... }

fwd(workOnVal);         // 错误！无法确定是 workOnVal 的哪个实例
```

当我们将`processVal`传递给`f`时，由于`f`的形参类型是已知的，编译器自然也知道它需要的是`processVal`的哪个重载版本；但纯粹的函数名称`processVal`并不包含类型信息，类型推导更是无从谈起，将它传递给`fwd`只会造成完美转发失败而已。

要让`fwd`接受重载函数的名称或模板名称，只能手动指定需要转发的哪个重载版本或模板实例。例如下面的做法就是合理的：

```cpp
using ProcessFuncType = int (*)(int);
ProcessFuncType processValPtr = processVal;     // 指定了需要的 processVal 签名

fwd(processValPtr);                             // 没问题
fwd(static_cast<ProcessFuncType>(workOnVal));   // 也没问题
```

**位域**

```cpp
struct IPv4Header {                 // 用于表示 IPv4 头部的模型
    std::uint32_t version:4,
                  IHL:4,
                  DSCP:6,
                  ECN:2,
                  totalLength:16;
    ...
};

void f(std::size_t sz);

IPv4Header h;
...

f(h.totalLength);                   // 没问题
fwd(h.totalLength);                 // 错误！
```

C++ 标准规定：非常引用不得绑定到位域。位域是由机器字的若干任意部分组成的，但这样的实体是无法对其直接取地址的，无法将指针指向它，因此也无法对其进行引用。

> 实际上常引用也不可能绑定到位域，它们绑定到的是 “常规” 对象（某种标准整型，例如`int`），其中拷贝了位域的值。

将位域传递给转发函数的可能途径是制作一个副本，并以该副本调用转发函数：

```cpp
// 拷贝位域值，使用的初始化形式参考条款 6
auto length = static_cast<std::uint16_t>(h.totalLength);

fwd(length);    // 转发该副本
```

## 第六章：lambda 表达式

在开始本章之前，需要理解几个基本的概念：

- **lambda 表达式（lambda expression）：** 表达式的一种，它的基本写法如下所示：

```cpp
[](int val){ return 0 < val && val < 10; }
```

- **闭包（closure）：** lambda 所创建的运行期对象，根据不同的捕获模式，闭包会持有数据的副本或引用。
- **闭包类（closure class）：** 实例化闭包的类，每个 lambda 都会使编译器生成唯一的闭包类。lambda 中的语句会成为其闭包类的成员函数中的可执行指令。

> lambda 表达式和闭包类存在于编译期，而闭包存在于运行期。

### 条款 31：避免默认捕获模式

C++11 中有两种默认捕获模式：按引用或按值。按引用捕获会导致闭包内包含指向局部变量的引用，或指向定义 lambda 的作用域内形参的引用，一旦由 lambda 所创建的闭包越过了该局部变量或形参的生命周期，那么闭包内的引用就会发生空悬：

```cpp
using FilterContainer = std::vector<std::function<bool(int)>>;
FilterContainer filters;                                // 元素为筛选函数的容器

void addDivisorFilter() {
    auto calc1 = computeSomeValue1();
    auto calc2 = computeSomeValue2();

    auto divisor = computeDivisor(calc1, calc2);

    filters.emplace_back(
        [&](int value) { return value % divisor == 0; } // 危险！指向 divisor 的引用可能空悬
    );
}
```

换作用显式方式按引用捕获`divisor`，问题依旧会发生，但更容易看出 lambda 依赖于`divisor`的生命周期这一问题：

```cpp
filters.emplace_back(
    [&divisor](int value) { return value % divisor == 0; }
);
```

一种权宜之计是不使用容器来存放筛选函数，转而使用 C++11 的`std::all_of`对每个元素逐一进行判断。但如果将该 lambda 拷贝到其它闭包比`divisor`生命周期更长的语境中，则空悬引用的问题仍会发生：

```cpp
template<typename C>
void workWithContainer(const C& container) {
    auto calc1 = computeSomeValue1();
    auto calc2 = computeSomeValue2();

    auto divisor = computeDivisor(calc1, calc2);

    using ContElemT = typename C::value_type;           // 取得容器中的元素类型（参考条款 13）

    using std::begin;
    using std::end;

    if (std::all_of(begin(container), end(container),   // 判断是否所有元素都是 divisor 的倍数
        [&](const ContElemT& value)                     // C++14 可以直接写成 const auto& value
        { return value % divisor == 0; })) {
        ...
    }
    else {
        ...
    }
}
```

在本例中，使用默认的按值捕获模式就足以解决问题：

```cpp
filters.emplace_back(
    [=](int value) { return value % divisor == 0; }
);
```

但是默认的按值捕获模式也有其问题：默认的按值捕获极易受到空悬指针的影响（尤其是`this`指针），并且会误导人们认为 lambda 是独立的。考虑如下情形：

```cpp
class Widget {
public:
    ...
    void addFilter() const;

private:
    int divisor;    // 用于 Widget 的 filters
};

void Widget::addFilter() const {
    filters.emplace_back(
        [=](int value) { return value % divisor == 0; }
    );
}
```

对于此处的 lambda 而言，`divisor`既不是局部变量，也不是形参，按理来讲是压根无法被捕获的。事实也确实如此，被捕获的实际上是`Widget`的`this`指针，而不是`divisor`。对于编译器来说，`addFilter`的代码相当于：

```cpp
void Widget::addFilter() const {
    auto currentObjectPtr = this;

    filters.emplace_back(
        [currentObjectPtr](int value)
        { return value % currentObjectPtr->divisor == 0; }
    );
}
```

理解了这一点，也就能理解 lambda 闭包的存活依赖于它含有的`this`指针副本所指向的`Widget`对象的生命周期。假如面临以下代码，空悬指针的问题将出现在我们的眼前：

```cpp
using FilterContainer = std::vector<std::function<bool(int)>>;
FilterContainer filters;                    // 同前

void doSomeWork() {
    auto pw = std::make_unique<Widget>();   // 创建 Widget，std::make_unique 的使用参考条款 21

    pw->addFilter();                        // 添加使用了 Widget::divisor 的筛选函数
    ...
}                                           // Widget 被销毁，filters 现在持有空悬指针！
```

一种解决方法是将你想捕获的成员变量拷贝至局部变量中，之后再捕获该副本局部变量：

```cpp
void Widget::addFilter() const {
    auto divisorCopy = divisor;

    filters.emplace_back(
        [divisorCopy](int value) { return value % divisorCopy == 0; }
    );
}
```

在 C++14 中，捕获成员变量的一种更好的方法是使用**广义 lambda 捕获（generalized lambda capture，参考条款 32）**：

```cpp
void Widget::addFilter() const {
    filters.emplace_back(
        [divisor = divisor](int value)  // C++14: 将 divisor 拷贝入闭包并使用副本
        { return value % divisor == 0; }
    );
}
```

默认的按值捕获模式的另一个缺点是，它会使人们误认为闭包是独立的，与闭包外的数据变化相隔绝。但实际上并非如此，lambda 可能不仅依赖于局部变量和形参，还会依赖于**静态存储期（static storage duration）对象**，这样的对象可以在 lambda 中使用，但却无法被捕获。下面这个例子足以体现这一点会造成的问题：

```cpp
void addDivisorFilter() {
    static auto calc1 = computeSomeValue1();
    static auto calc2 = computeSomeValue2();

    static auto divisor = computeDivisor(calc1, calc2);

    filters.emplace_back(
        [=](int value)                      // 未捕获任何东西！
        { return value % divisor == 0; }    // 引用上面的 static 对象
    );

    ++divisor;                              // 意外修改了 divisor
                                            // 导致每个 lambda 都出现新的行为
}
```

### 条款 32：使用初始化捕获将对象移入闭包

C++14 中新增的**初始化捕获（init capture，又称广义 lambda 捕获）**，可以让你指定：

1. 由 lambda 生成的闭包类中成员变量的名字。
2. 一个用于初始化该成员变量的表达式。

下面演示了如何使用初始化捕获将`std::unique_ptr`移入闭包内：

```cpp
class Widget {
public:

    bool isValidated() const;
    bool isProcessed() const;
    bool isArchived() const;

private:

};

auto pw = std::make_unique<Widget>();   // 创建 Widget，std::make_unique 的使用参考条款 21

...                                     // 配置 *pw

auto func = [pw = std::move(pw)]        // 使用 std::move(pw) 初始化闭包类的数据成员
            { return pw->isValidated() && pw->isArchived(); };
```

如果经由`std::make_unique`创建的对象已具备被 lambda 捕获的合适状态，则闭包类成原对象可以直接由`std::make_unique`完成初始化：

```cpp
auto func = [pw = std::make_unique<Widget>()]
            { return pw->isValidated() && pw->isArchived(); };
```

在初始化捕获的代码中，位于`=`左侧的是所指定的闭包类成员变量的名称，右侧的则是其初始化表达式。值得一提的是，`=`的左右两侧位于不同的作用域，左侧作用域就是闭包的作用域，而右侧作用域则与 lambda 定义所在的作用域相同。

在 C++11 中，我们虽然无法使用初始化捕获，但是可以依靠原理相同的手写类达到目的：

```cpp
class IsValAndArch {
public:
    using DataType = std::unique_ptr<Widget>;

    explicit IsValAndArch(DataType&& ptr)   // std::move 的使用参考条款 25
        : pw(std::move(ptr)) {}

    bool operator()() const {               // 编写仿函数
        return pw->isValidated() && pw->isArchived();
    }

private:
    DataType pw;
};

auto func = IsValAndArch(std::make_unique<Widget>());
```

如果你非要使用 lambda，按移动捕获也可以通过以下方法模拟：

1. 将需要捕获的对象移至`std::bind`所产生的函数对象中。
2. 给予 lambda 一个指向想要 “捕获” 的对象的引用。

```cpp
std::vector<double> data;

auto func =
    std::bind([](const std::vector<double>& data)   // C++11 模拟初始化捕获
              { /* 使用 data */ },
              std::move(data));
```

`std::bind`的第一个实参是个可调用对象，接下来的所有实参表示传给该对象的值。和 lambda 表达式类似，`std::bind`也会生成函数对象（原书中称其为**绑定对象，bind object**），其中含有传递给`std::bind`的所有实参的副本，其中左值实参执行的是拷贝构造，而右值实参执行的则是移动构造。因此，在此处用`std::move(data)`作为实参可以让`data`被移入绑定对象中，被 lambda 的左值引用形参所接受。

默认情况下，lambda 生成的闭包类中的`operator()`成员函数会带有`const`限定符，它会导致是闭包类里的所有成员变量在 lambda 的函数体内都会带有`const`限定符。但是，绑定对象里通过移动构造得到的`data`副本却并不带有`const`限定符。因此，为了防止该`data`副本在 lambda 中被意外修改，我们会将其形参声明为常引用。但如果 lambda 在声明时带有`mutable`限定符，则闭包里的`operator()`就不再会带有`const`限定符，也不必再将形参声明为常引用：

```cpp
auto func =
    std::bind([](std::vector<double>& data) mutable
              { /* 使用 data */ },
              std::move(data));
```

回到之前的例子，使用 C++14 在闭包内创建`std::unique_ptr`：

```cpp
auto func = [pw = std::make_unique<Widget>()]
            { return pw->isValidated() && pw->isArchived(); };
```

它在 C++11 中的模拟代码可以这样编写：

```cpp
auto func = std::bind([](const std::unique_ptr<Widget>& pw)
                      { return pw->isValidated() && pw->isArchived(); },
                      std::make_unique<Widget>());
```

### 条款 33：对 auto&& 类型的形参使用 decltype 以对其实施 std::forward

C++14 支持**泛型 lambda（generic lambda）**，可以在声明形参时使用`auto`，即闭包类中的`operator()`可以用模板实现。例如，给定以下 lambda：

```cpp
auto f = [](auto x) { return func(normalize(x)); };
```

则闭包类的`operator()`实现大致如下：

```cpp
class SomeCompilerGeneratedClassName {
public:
    template<typename T>
    auto operator()(T x) const {    // auto 类型的返回值，参考条款 3
        return func(normalize(x));
    }
    ...
};
```

在此处，lambda 总会传递左值给`normalize`，这对于一个会区别对待左、右值的`normalize`显然是不行的。如果想要保留实参的左、右值性，就需要将形参声明为万能引用（参考**条款 24**），并使用`std::forward`将其转发给`normalize`（参考**条款 25**）。这样的改造十分简单，唯一的问题是在 lambda 中，我们并没有可用的模板形参`T`，只能对要转发的形参使用`decltype`，以取得其类型：

```cpp
auto f = [](auto&& param) {
    return func(normalize(std::forward<decltype(param)>(param)));
};
```

**条款 28** 说明了，使用`std::forward`的惯例是：用左值引用类型的模板形参来表明想要返回左值，用非引用类型的模板形参来表明想要返回右值。而在此处，如果`param`是左值，则`decltype(param)`会产生左值引用类型，这符合惯例；但如果`param`是右值，则`decltype(param)`会产生右值引用类型，不符合惯例的非引用，在这种情况下，`std::forward`将被具现化为：

```cpp
Widget&& && forward(remove_reference_t<Widget&>& param) {
    return static_cast<Widget&& &&>(param);
}
```

由于引用折叠的存在，`Widget&& &&`将被折叠为`Widget&&`，所以实际上生成的代码和使用非引用类型作为模板形参生成的版本并没有什么区别。综上所述，在此处使用`decltype(param)`并不会产生任何问题，这是个非常不错的结果。

C++14 的 lambda 也支持可变参数，只需稍加改动，就可以得到能接受多个参数的完美转发 lambda 版本：

```cpp
auto f = [](auto&&... params) {
    return func(normalize(std::forward<decltype(params)>(params)...));
};
```

### 条款 34：优先选用 lambda，而非 std::bind

之所以优先选用 lambda 而非 std::bind，最主要的原因是 lambda 具有更高的可读性。举个例子，假设我们有个函数用来设置警报声：

```cpp
// 表示时刻的类型
using Time = std::chrono::steady_clock::time_point;
enum class Sound { Beep, Siren, Whistle };

// 表示时长的类型
using Duration = std::chrono::steady_clock::duration;

// 在时刻 t，发出声音 s，持续时长 d
void setAlarm(Time t, Sound s, Duration d);
```

我们可以编写一个 lambda，设置在一小时后发出警报并持续 30 秒，同时提供接口，以指定发出的声音：

```cpp
auto setSoundL = [](Sound s) {
    using namespace std::chrono;
    using namespace std::literals;              // 引入 C++14 中的字面量后缀

    setAlarm(steady_clock::now() + 1h, s, 30s); // C++11 需要用 hours 和 seconds 代替后缀
}
```

接下来我们将尝试使用`std::bind`来编写相应的代码。下面的这段代码并不正确，但足以让我们发现`std::bind`的难用之处：

```cpp
using namespace std::chrono;
using namespace std::literals;

using namespace std::placeholders;  // 引入占位符

auto setSoundB = std::bind(setAlarm,
                           steady_clock::now() + 1h,
                           _1,
                           30s);
```

占位符`_1`表示它在`std::bind`形参列表中的映射位置，在此处表示调用`setSoundB`时传入的第一个实参，会作为第二个实参传递给`setAlarm`。这显然已经不如 lambda 直观。

更重要的是上述代码所隐含的问题：调用时间的错误。在`std::bind`的调用中，`steady_clock::now() + 1h`作为实参被传递给了`std::bind`，而非`setAlarm`，这意味着该表达式会在调用`std::bind`的时刻计算出具体值，而非调用`setAlarm`的时刻，这与我们的意图显然不符。想要解决这个问题，就要嵌套第二层`std::bind`的调用：

```cpp
auto setSoundB = std::bind(setAlarm,
                           std::bind(std::plus<>(), // C++11 无法省略 std::plus<steady_clock::time_point>()
                                     steady_clock::now(),
                                     1h),
                           _1,
                           30s);
```

另一个`std::bind`不如 lambda 的例子是重载。假如有个重载版本会接受第四个形参，用于指定警报的音量：

```cpp
enum class Volume { Normal, Loud, LoudPlusPlus };
void setAlarm(Time t, Sound s, Duration d, Volume v);
```

原来的 lambda 仍会正常运作，而`std::bind`会立刻发生错误，因为它只有函数名，并不知道要调用哪个重载版本的函数。为使得`std::bind`的调用能通过编译，必须将`setAlarm`强制转换到适当的函数指针类型：

```cpp
using SetAlarm3ParamType = void (*)(Time t, Sound s, Duration d);

auto setSoundB = std::bind(static_cast<SetAlarm3ParamType>(setAlarm),
                           std::bind(std::plus<>(), steady_clock::now(), 1h),
                           _1,
                           30s);
```

下面是一个更极端的例子，演示了`std::bind`到底有多晦涩：

```cpp
// lambda 版本（C++14）
auto betweenL = [lowVal, highVal](const auto& val) {
    return lowVal <= val && val <= highVal;
};

// std::bind 版本（C++14）
auto betweenB = std::bind(std::logical_and<>(),
                          std::bind(std::less_equal<>(), lowVal, std::placeholders::_1),
                          std::bind(std::less_equal<>(), std::placeholders::_1, highVal),)
```

`std::bind`总是拷贝其实参，调用时需要借助`std::ref`来达到按引用存储实参的目的，这一点同样也不如 lambda 来得明确：

```cpp
auto compressRateL = [&w](CompLevel lev) { return compress(w, lev); };
auto compressRateB = std::bind(compress, std::ref(w), std::placeholders::_1);
```

除了可读性以外，编译器通常能更好地以内联优化 lambda 所调用的函数，而对于使用函数指针的`std::bind`则很难做到，这会导致使用 lambda 有可能会生成比使用`std::bind`运行得更快的代码。

在 C++11 中，`std::bind`仅在两个受限的场合还有使用的理由：

1. 移动捕获（参考**条款 32**）；
2. 多态函数对象（这在 C++14 中可以被泛型 lambda 轻易实现）：

```cpp
class PolyWidget {
public:
    template<typename T>
    void operator()(const T& param);
    ...
};

PolyWidget pw;
auto boundPW = std::bind(pw, std::placeholders::_1);

// 可以用不同类型的实参调用 PolyWidget::operator()
boundPW(1930);
boundPW(nullptr);
boundPW("Rosebud"); // 原书作者玩的《公民凯恩》梗（应该是吧）
```

## 第七章：并发 API

### 条款 35：优先选用基于任务而非基于线程的程序设计

如果你想以异步方式执行函数`doAsyncWork`，你可以选择**基于线程（thread-based）** 的方式：

```cpp
int doAsyncWork();

std::thread t(doAsyncWork);
```

也可以将`doAsyncWork`传递给`std::async`，这是**基于任务（task-based）** 的方式：

```cpp
auto fut = std::async(doAsyncWork); // 需要 #include <future>
```

“线程” 在带有并发的 C++ 软件中有三重含义：

- **硬件线程（hardware threads）** 是实际执行计算的线程。现代计算机架构会为每个 CPU 内核提供一个或多个硬件线程。
- **软件线程（software threads，也称系统线程）** 是操作系统（或嵌入式系统）用于实现跨进程的管理，以及进行硬件线程调度的线程。通常，能够创建的软件线程比硬件线程要多，因为当一个软件线程被阻塞时，运行其它未阻塞线程能够提高吞吐率。
- **`std::thread`** 是 C++ 进程中的对象，用作底层软件线程的句柄。有些`std::thread`对象表现为 “null” 句柄，表示其无软件线程，可能的原因有：处于默认构造状态（没有要执行的函数），被移动了（被移动的目标对象成为了该软件线程的句柄），被联结（join）了（函数已执行结束），被分离（detach）了（与其软件线程的连接被切断）。

软件线程和硬件线程都是有限的。如果你试图创建多于系统能提供的数量的线程，就会抛出`std::system_error`异常，即使待执行的函数带有`noexcept`限定符也一样。如果非阻塞的软件线程数量超过了硬件线程数量，就会产生**资源超额（oversubscription）** 问题，此时线程调度器会将软件线程的 CPU 时间切片，分配到硬件线程之上。当一个软件线程的时间片执行结束，就会让给另一个软件线程，并产生上下文切换。在这种情况下，新的软件线程几乎不能命中 CPU 缓存，同时还会污染为旧线程所准备的数据（旧线程很可能还会再被调度到同一内核上运行），这会造成高昂的线程管理开销。

> 避免资源超额很困难，因为软件线程和硬件线程的最佳比例取决于软件线程的执行频率，那是动态改变的，例如一个程序从 IO 密集型变成计算密集型，会使执行频率发生改变。而且该比例还依赖于上下文切换的开销以及软件线程对于 CPU 缓存的使用效率。计算机本身的架构也会对其具体细节产生很大的影响。

比起基于线程，基于任务的设计能够减轻手动管理线程的艰难，而且它提供了一种很自然的方式（藉由`get`函数），让你检查异步执行函数的结果（即返回值或异常）。

虽然说了这么多，但仍有以下几种情况，直接使用线程会更合适：

- **你需要访问非常底层的线程 API。** C++ 并发 API 通常会采用特定平台的低级 API 来实现，例如 pthread 和 Windows 线程库，它们提高的 API 比 C++ 更丰富。为了访问底层的线程 API，`std::thread`通常会提供`native_handle`成员函数，而`std::async`的返回值`std::future`则没有该功能。
- **你需要且有能力为你的应用优化线程的使用。** 例如在完成性能分析的情况下为专一硬件平台开发应用。
- **你需要实现实现超越 C++ 并发 API 的线程技术。** 例如为 C++ 未提供线程池的平台实现线程池。

### 条款 36：如果异步是必要的，则指定 std::launch::async

在调用`std::async`时，有以下两种启动策略可以选择：

- `std::launch::async`启动策略意味着函数必须以异步方式在另一条线程上执行。
- `std::launch::deferred`启动策略意味着函数会被推迟到`std::async`所返回的`std::future`的`get`或`wait`函数得到调用时才执行（这是个简化说法，关键点其实是`std::future`引用的共享状态，参考**条款 38**）。在那之后，调用`get`或`wait`的线程将会被阻塞，直至函数执行结束为止。如果`get`或`wait`都没得到调用，则函数将不会被执行。

`std::async`的默认启动策略既允许任务以异步方式执行，也允许任务以同步方式执行，即下面两个调用是等价的：

```cpp
auto fut1 = std::async(f);

auto fut2 = std::async(std::launch::async | std::launch::deferred,
                       f);
```

这种弹性使得`std::async`和标准库的线程管理组件能够承担起线程的创建和销毁，避免资源超额，以及负载均衡的责任。但也会带来一些意料之外的问题：

- 无法预知`f`是否会与调用`std::async`的线程并发执行，它也可能会被推迟执行。
- 无法预知`f`是否会在与调用`get`和`wait`函数的线程不同的线程上执行。
- 无法预知`f`在读或写此**线程本地存储（thread-local，TLS）** 时，会在哪个线程的本地存储上完成操作，这会影响到`thread_local`变量的使用。
- 就连`f`是否会被执行这种基本的事情都无法预知。

延迟启动策略还会影响以超时为条件的基于 wait 的循环：

```cpp
using namespace std::literals;

void f() {
    std::this_thread::sleep_for(1s);
}

auto fut = std::async(f);

while (fut.wait_for(100ms) != std::future_status::ready) {  // 循环至 f 完成执行
    ...
    // 若 f 被推迟执行，则 fut.wait_for 返回 std::future_status::deferred
    // 循环永远不会被终止！
}
```

解决这个问题的方法很简单，只需要确认任务是否被推迟，可以通过调用一个基于超时的函数（例如`wait_for`）并检查其返回值来实现：

```cpp
auto fut = std::async(f);

if (fut.wait_for(0s) == std::future_status::deferred) { // 如果任务被推迟了
    ... // 调用 fut 的 wait 或 get，以同步方式执行 f
}
else {                                                  // 如果任务未被推迟
    while (fut.wait_for(100ms) != std::future_status::ready) {
        ... // 不断去做并发任务，直至 f 完成执行
    }
    ... // fut 已经就绪
}
```

综上所述，如果需要执行的任务满足以下条件，就可以使用`std::async`的默认启动策略：

1. 任务不需要与调用`get`或`wait`的线程并发执行。
2. 读或写哪个线程的`thread_local`变量并无影响。
3. 保证在`std::async`返回的`std::future`对象上调用`get`或`wait`，或者可以接受任务可能永不执行。
4. 使用`wait_for`或`wait_until`的代码会考虑到任务被推迟的可能性。

如果其中任何一个条件不满足，就需要确保任务以异步方式执行：

```cpp
auto fut = std::async(std::launch::async, f);
```

也可以编写一个辅助函数来自动执行`std::async`的异步启动策略：

```cpp
template<typename F, typename... Ts>
inline std::future<typename std::result_of<F(Ts...)>::type> // C++14 可以直接用 auto 推导返回值类型
reallyAsync(F&& f, Ts&&... params) {
    return std::async(std::launch::async,
                      std::forward<F>(f),
                      std::forward<Ts>(params)...);
}
```

> 用于获取可调用对象返回值类型的`std::result_of`在 C++17 后被弃用，其替代品为`std::invoke_result`。

### 条款 37：使 std::thread 对象在所有路径皆不可联结

当`std::thread`处于可联结的状态时，它对应于正在运行或可能将要运行的底层执行线程，这包括正在等待调度的或者被阻塞的线程，以及运行结束的线程。

以下几种`std::thread`对象处于不可联结的状态：

- 默认构造的`std::thread`。
- 已移动的`std::thread`。
- 已联结（join）的`std::thread`。
- 已分离（detach）的`std::thread`。

当`std::thread`执行析构时，若其处于可联结状态，就会导致程序终止运行（通常会调用`std::abort`）。考虑以下代码：

```cpp
constexpr auto tenMillion = 10'000'000;             // C++14 的单引号数字分隔符

bool doWork(std::function<bool(int)> filter, int maxVal = tenMillion) {
    std::vector<int> goodVals;

    std::thread t([&filter, maxVal, &goodVals] {    // 遍历 goodVals
                      for (auto i = 0; i <= maxVal; ++i)
                      { if (filter(i)) goodVals.push_back(i); }
                  });

    auto nh = t.native_handle();                    // 使用 t 的原生句柄来设定线程的优先级

    if (conditionAreSatisfied()) {
        t.join();                                   // 让 t 结束运行
        performComputation(goodVals);
        return true;                                // 计算已实施
    }

    return false;                                   // 计算未实施
}                                                   // 此处析构 std::thread 会导致程序终止
```

标准委员会并没有选择让`std::thread`在销毁时，隐式执行`join`或`detach`，因为这带来的问题会比直接让程序终止运行还要严重：

- **隐式`join`** 会使`std::thread`的析构函数等待底层异步线程执行完毕。这听上去很合理，但却可能导致难以追踪的性能异常。例如，即使`conditionAreSatisfied`已经返回`false`了，`doWork`仍然会继续执行遍历操作，这是违反直觉的。
- **隐式`detach`** 会使`std::thread`的析构函数分离`std::thread`对象和底层执行线程之间的连接，而该底层执行线程会继续运行。这会导致更要命的调试问题。假如`conditionAreSatisfied`返回了`false`，则`doWork`也会直接返回，同时销毁局部变量并弹出栈帧。但线程仍然在`doWork`的调用点继续运行，并导致栈帧上的内存被意外修改，

我们可以编写一个 RAII 类，并让调用者自行选择在销毁时为`std::thread`调用`join`还是`detach`：

```cpp
class ThreadRAII {
public:
    enum class DtorAction { join, detach };     // 关于枚举类，参考条款 20

    ThreadRAII(std::thread&& t, DtorAction a)   // 对 t 执行操作 a
        : action(action), t(std::move(t)) {}

    ~ThreadRAII() {
        // 先校验 t 是否处于可联结状态
        // 对不可联结的 std::thread 调用 join 或 detach 是未定义行为
        if (t.joinable()) {
            if (action == DtorAction::join) {
                t.join();
            }
            else {
                t.detach();
            }
        }
    }

    ThreadRAII(ThreadRAII&&) = default;         // 支持移动操作
    ThreadRAII& operator=(ThreadRAII&&) = default;

    std::thread& get() { return t; }            // 返回底层的 std::thread 对象

private:
    DtorAction action;
    std::thread t;                              // 使 t 最后被初始化，确保它可以安全访问其它成员
};
```

> 不需要担心在`t.joinable()`的执行和`join`或`detach`的调用之间，有另一个线程会让`t`变得不可联结。因为`std::thread`对象只能通过调用成员函数来从可联结状态转换为不可联结状态，而当`ThreadRAII`对象的析构函数被调用时，不应该有其它线程调用该对象的成员函数。一般地，若要在一个对象上同时调用两个成员函数，只有当所有这些函数都带有`const`限定符时才安全（参考**条款 16**）。

在`doWork`函数的代码中，可以这样使用`ThreadRAII`：

```cpp
bool doWork(std::function<bool(int)> filter, int maxVal = tenMillion) {
    std::vector<int> goodVals;

    ThreadRAII t(std::thread([&filter, maxVal, &goodVals] {
                                 for (auto i = 0; i <= maxVal; ++i)
                                 { if (filter(i)) goodVals.push_back(i); }
                             }),
                 ThreadRAII::DtorAction::join);

    auto nh = t.get().native_handle();

    if (conditionAreSatisfied()) {
        t.get().join();
        performComputation(goodVals);
        return true;
    }

    return false;
}
```

### 条款 38：注意不同线程句柄的析构行为

future 位于通信信道的一端，被调用者通过该信道将结果发送给调用者。被调用者（通常以异步方式运行）将其计算所得的结果写入信道（通常经过`std::promise`对象），而调用者则使用 future 来读取该结果。

但被调用者的结果要存储在哪里呢？既不能存储在被调用者的`std::promise`对象中，因为它是个局部对象，在被调用者执行结束后会被销毁；也不能存储在调用者的 future 中，因为`std::future`可能会被用来创建`std::shared_future`，而后者会导致原始`std::future`析构之后被多次拷贝，但被调用者的结果并不能保证一定可以被拷贝，很难做到使它与最后一个指向它的 future 生命周期一样长。

因此该结果会被存储在位于两者外部的某个位置，这个位置称为**共享状态（shared state）**，通常用堆上的对象来表示，但是其类型、接口和实现皆未在标准中指定。我们可以把调用者，被调用者以及共享状态之间的关系用下图来表示：

![img](./assets/v2-006b36a9b3ae2533a73d8c1646d49511_1440w.jpg)

共享状态的存在非常重要，因为 future 对象的析构行为取决于与其关联的共享状态。具体来说就是：

- 常规的 future 对象在析构时仅会析构该 future 的成员变量。这相当于对底层执行线程执行了隐式`detach`。
- 引用了共享状态（使用`std::async`启动未延迟任务时创建的）的最后一个 future 对象的析构函数将会被阻塞住，直至该任务结束。这相当于对正在运行`std::async`所创建任务的线程执行了隐式`join`。

也就是说，只有在满足下列条件时，future 的非常规析构行为才会被触发：

1. future 所引用的共享状态是在调用`std::async`时创建的；
2. 该任务执行异步启动策略，即`std::launch::async`（参考**条款 36**）；
3. 该 future 是最后一个引用了该共享状态的 future。

future 的 API 没有提供任何办法判断它引用的共享状态是否诞生于`std::async`的调用，因此任意给定一个 future 对象，我们没有办法判断它是否会在析构函数中阻塞。这可能会导致一些意外的情况：

```cpp
// 该容器的析构函数可能会发生阻塞
// 因为它持有 future 可能会有一个或多个
// 满足触发非常规析构行为的条件
std::vector<std::future<void>> futs;    // 关于 std::future<void>，参考条款 39

class Widget {                          // Widget 对象的析构函数可能会发生阻塞
public:
    ...

private:
    std::shared_future<double> fut;
};
```

使用`std::packaged_task`也能创建出共享对象，但是其衍生的 future 都会执行常规的析构行为：

```cpp
int calcValue();

{
    std::packaged_task<int()>       // 给 calcValue 加上包装
    pt(calcValue);                  // 使之能以异步方式执行

    auto fut = pt.get_future();     // 取得 pt 的 future
    std::thread t(std::move(pt));   // std::packaged_task 是只移类型

    ...                             // 析构 std::thread（参考条款 37）
}                                   // 以常规方式析构 future 对象 fut
```

### 条款 39：考虑对一次性事件通信使用 void 的 futures

有的时候，让一个任务能够在发生了特定事件后，通知另一个异步运行的任务，会是很有用的。为了实现这种简单的事件通信，使用条件变量会是一个显而易见的做法：

```cpp
std::condition_variable cv;             // 事件的条件变量
std::mutex m;                           // 配合 cv 使用的互斥量

...                                     // 检测事件
cv.notify_one();                        // 通知反应任务，对多个任务使用 notify_all

// 反应任务的代码
...                                     // 准备作出反应
{
    std::unique_lock<std::mutex> lk(m); // 为互斥量加锁

    cv.wait(lk);                        // 等待通知到来
    ...                                 // 针对事件作出反应
}                                       // 通过 lk 的析构函数为 m 解锁
...                                     // 继续等待反应
```

这种途径会导致以下几个问题：

- **代码异味（code smell）：** 虽然代码能够运行，但总感觉哪里不太对劲。此处对互斥量的使用似乎有些多余，互斥量是用于控制共享数据访问的，但检测和反应任务之间大可以根本不需要这种介质。
- 如果检测任务在反应任务调用`wait`之前就通知了条件变量，则反应任务将会失去响应。
- 反应任务的`wait`语句无法应对**虚假唤醒（spurious wakeups）**，即使条件变量没有得到通知，针对该条件变量等待的代码也有可能被唤醒。如果反应线程可以确认它所等待的事件是否已经发生，那么我们可以通过将 lambda 传递给`wait`来处理这种情况：

```cpp
cv.wait(lk, [] { return 事件是否真的已经发生; });
```

基于 flag 的设计可以避免上述问题，但这一设计基于轮询而非阻塞，会对 CPU 核心产生额外的性能消耗：

```cpp
std::atomic<bool> flag(false);  // 共享的 bool flag
                                // 关于 std::atomic，参考条款 40
...                             // 检测事件
flag = true;                    // 通知反应任务

// 反应任务的代码
...                             // 准备作出反应
while (!flag);                  // 等待事件
...                             // 针对事件作出反应
```

条件变量可以和 flag 一起使用，但这样的通信机制设计看起来不太自然：

```cpp
std::condition_variable cv;                 // 同前
std::mutex m;

bool flag(false);                           // 非 std::atomic 对象

...                                         // 检测事件
{
    std::lock_guard<std::mutex> g(m);       // 为 m 加锁
    flag = true;                            // 通知反应任务（第一部分）
}
cv.notify_one();                            // 通知反应任务（第二部分）

// 反应任务的代码
...                                         // 准备作出反应
{
    std::unique_lock<std::mutex> lk(m);     // 同前

    cv.wait(lk, [&flag] { return flag; });  // 使用 lambda 应对虚假唤醒
    ...                                     // 针对事件作出反应
}
...                                         // 继续等待反应
```

另外一种方法是摆脱条件变量，互斥量和 flag，让反应任务去等待检测任务设置的 future。这种设计简单易行，检测任务有一个`std::promise`对象，反应任务有对应的 future。当检测任务发现它查找的事件已经发生时，它会设置`std::promise`对象；与此同时，反应任务调用`wait`以等待它的 future。由于在此处我们并不会真正向信道发送任何数据，所以对于`std::promise`、`std::future`和`std::shared_future`的模板类型形参，都只需使用`void`即可：

```cpp
std::promise<void> p;

...                     // 检测事件
p.set_value();          // 通知反应任务

// 反应任务的代码
...                     // 准备作出反应
p.get_future().wait();  // 等待 p 对应的 future
...                     // 针对事件作出反应
```

这种手法有两个最大的局限性：

- `std::promise`和 future 之间依赖共享状态，而共享状态会带来在堆上分配和回收空间的成本。
- `std::promise`对象只能设置一次，这意味着该手法只能应用于一次性通信的情况。

假如你想创建多个能暂停一次的线程，使用`void` future 手法就是合理的选择。代码演示如下：

```cpp
std::promise<void> p;

void detect() {
    auto sf = p.get_future().share();                   // sf 的类型是 std::shared_future<void>

    std::vector<std::thread> vt;                        // 反应任务的容器

    for (int i = 0; i < threadsToRun; ++i) {
        vt.emplace_back([sf] { sf.wait(); react(); });  // sf 局部副本之上的 wait
    }

    ...                                                 // 若在此处抛出异常，则 detect 会失去响应！

    p.set_value();                                      // 让所有线程取消暂停

    ...                                                 // 完成其它工作

    for (auto& t : vt) {                                // 把所有线程设为不可联结的状态
        t.join();
    }
}
```

### 条款 40：对并发使用 std::atomic，对特殊内存使用 volatile

`std::atomic`可以保证它提供的操作被其它线程视为具有原子性，它产生的效果和受到互斥锁保护的操作类似，但是通常`std::atomic`的原子操作是通过特定的机器指令实现的，这比锁的实现更高效。考虑以下应用了`std::atomic`的代码：

```cpp
std::atomic<int> ai(0); // 将 ai 初始化为 0
ai = 10;                // 原子地将 ai 设为 10
std::cout << ai;        // 原子地读取 ai 地值
++ai;                   // 原子地将 ai 自增为 11
--ai;                   // 原子地将 ai 自减为 10
```

需要注意的是，在`std::cout << ai`语句中，`std::atomic`仅能保证对于`ai`的读取操作具有原子性，而不能保证整条语句都具有原子性，在读取`ai`的值和调用`operator<<`之间，可能会有别的线程修改了`ai`的值，但这对使用按值传参的`operator<<`并没有什么影响。

`ai`的自增和自减操作是**读取-修改-写入（read-modify-write，RWM）** 操作，`std::atomic`能确保它们整体以原子方式执行。这是`std::atomic`最重要的特性之一：`std::atomic`对象之上的所有成员函数都能被其它线程视为原子性的。

`std::atomic`在 RWM 上具有的优势与`volatile`相比十分明显：

```cpp
std::atomic<int> ac(0); // atomic counter
volatile int vc(0);     // volatile counter

/* 在两个同时运行的线程中各自执行自增操作 */

// 线程 1
++ac;
++vc;

// 线程 2
++ac;
++vc;
```

在两个执行结束后，`ac`的值一定为 2，而`vc`却不一定，数据竞争导致它的最终结果实际上是无法预测的，下面是一种可能的情况：

1. 线程 1 读取`vc`的值为 0；
2. 线程 2 读取`vc`的值仍为 0；
3. 线程 1 将读取的值 0 自增为 1，写入`vc`；
4. 线程 2 也将读取的值 0 自增为 1，写入`vc`；
5. `vc`最终的值为 1。

除了 RWM 以外，`std::atomic`还在确保**顺序一致性（sequential consistency）** 上具有优势，这种一致性是它默认采用的（尽管 C++ 还支持其它的一致性模型，但它们的安全性无法得到保证），它规定：在源代码中，**任何位于`std::atomic`变量的写入操作之前的代码不得发生于写入操作之后**。使用`std::atomic`可以保证以下代码中的赋值语句不会进行重新排序：

```cpp
std::atomic<bool> valAvailable(false);
auto imptValue = computeImportantValue();   // 计算出值
valAvailable = true;                        // 通知其它任务，值已可用
```

如果不使用`std::atomic`，语句的顺序可能会被编译器或底层硬件重新排列，以使得代码运行得更快：

```cpp
// 重新排序后的结果
valAvailable = true;
auto imptValue = computeImportantValue();
```

`std::atomic`是只移类型，因此以下代码无法通过编译：

```cpp
std::atomic<int> x(0);

auto y = x;
y = x;
```

正确的方式是调用`std::atomic`的成员函数`load`和`store`来以原子方式读取和写入：

```cpp
std::atomic<int> y(x.load());
y.store(x.load());
```

尽管在很多时候`load`和`store`并不是必要的，但是有些开发者还是很喜欢使用它们，因为这样做可以在代码中明确强调所使用的变量并非常规。这在很大程度上是一个代码风格的问题。

`volatile`是用来处理特殊内存的工具，它会被用在读写操作不应该被优化的内存上。一般来讲，编译器会为常规内存的冗余读取和写入自动执行优化，例如以下代码：

```cpp
auto y = x; // 读取 x
y = x;      // 再次读取 x

x = 10;     // 写入 x
x = 20;     // 再次写入 x
```

在经过优化后就能变成十分精简的版本：

```cpp
auto y = x; // 读取 x

x = 20;     // 写入 x
```

但对于特殊内存，我们可能不想要编译器去执行这种优化。例如用于内存映射 I/O 的内存，这种内存的位置实际上会被用于与外部设备通信，而非用于读取或写入常规内存。这时，`volatile`就能派上用场：

```cpp
volatile int x; // 以 volatile 声明 x
...             // 初始化 x

auto y = x;     // 读取 x
y = x;          // 再次读取 x（不会被优化掉）

x = 10;         // 写入 x（不会被优化掉）
x = 20;         // 再次写入 x
```

`std::atomic`和`volatile`用于不同的目的，它们甚至可以一起使用：

```cpp
volatile std::atomic<int> vai;  // 针对 vai 的操作具有原子性
                                // 并且不会被优化掉
```

## 第八章：微调

### 条款 41：对于移动成本低且总是被拷贝的可拷贝形参，考虑将其按值传递

为了实现对传入函数的左值实参执行拷贝，对右值实参执行移动，我们一共有以下三种方法：

```cpp
class Widget {                                  // 方法一：
public:                                         // 对左值和右值分别重载
    void addName(const std::string& newName) {
        names.push_back(newName);
    }

    void addName(std::string&& newName) {
        names.push_back(std::move(newName));
    }
    ...

private:
    std::vector<std::string> names;
};

class Widget {                                  // 方法二：
public:                                         // 使用万能引用
    template<typename T>
    void addName(T&& newName) {
        names.push_back(std::forward<T>(newName));
    }
    ...
};

class Widget {                                  // 方法三：
public:                                         // 按值传递参数
    void addName(std::string newName) {
        names.push_back(std::move(newName));
    }
    ...
};
```

在 C++98 中，按值传递的形参总会通过拷贝构造函数创建，但在 C++11 后，形参仅在传入左值时才会被拷贝构造，而如果传入的是个右值，它会被移动构造。

对于可拷贝的，移动开销低的，并且总是会被拷贝的形参而言，按值传递和按引用传递的效率很接近，而且按值传递更容易实现，还可能会生成更少的目标代码。

对于不可拷贝的形参，由于它的拷贝构造函数已被禁用，我们也不需要为其左值类型的实参提供支持，只需要编写一个接受右值引用的版本就行了。考虑一个类，它含有一个`std::unique_ptr`类型的数据成员和对应的 setter，而`std::unique_ptr`是个只移类型，所以我们只需要编写单个函数：

```cpp
class Widget {
public:
    ...
    void setPtr(std::unique_ptr<std::string>&& ptr) {
        p = std::move(ptr);
    }

private:
    std::unique_ptr<std::string> p;
};

Widget w;
...
w.setPtr(std::make_unique<std::string>("Modern C++"));
```

使用按值传参的前提是形参移动的成本足够低廉，因为按值传参会比按引用传参多一次额外的移动操作，如果这个前提不成立，那么执行不必要的移动就与执行不必要的拷贝没有什么区别。另外，你应当只对一定会被拷贝的形参考虑使用按值传参，以下代码就是一个反例：

```cpp
class Widget {
public:
    void addName(std::string newName) {
        if ((newName.length() >= minLen) &&
            (newName.length() <= maxLen)) {
            names.push_back(std::move(newName));
        }
    }
    ...

private:
    std::vector<std::string> names;
};
```

即使没有向`names`添加任何内容，该函数也会造成构造和析构`newName`的开销，而如果使用按引用传参，就可以避免这笔开销。

通过构造拷贝形参的开销可能会比通过赋值拷贝形参要大得多。考虑以下代码：

```cpp
class Password {
public:
    explicit Password(std::string pwd)  // 按值传参
        : text(std::move(pwd)) {}       // 对 text 进行构造

    void changeTo(std::string newPwd) { // 按值传参
        text = std::move(newPwd);       // 对 text 进行赋值
    }

private:
    std::string text;                   // 表示密码
};
...

std::string initPwd("Supercalifragilisticexpialidocious");  // 旧密码
Password p(initPwd);
...

std::string newPassword = "Beware the Jabberwock";          // 新密码
p.changeTo(newPassword);
```

在此处，旧密码比新密码更长，因此不需要进行任何内存分配和回收。如果采用重载的方式，可能就不会发生任何动态内存管理操作：

```cpp
class Password {
public:
    ...
    void changeTo(std::string& newPwd) {    // 对左值的重载
        text = newPwd;                      // 若 text.capacity() >= newPwd.size()
                                            // 则可以复用 text 的内存
    }
    ...
};
```

在此情形下，使用按值传参就会造成额外的内存分配和回收的开销，这可能会比移动`std::string`的开销高出几个数量级。

综上所述，通过赋值拷贝一个形参进行按值传参所造成的额外开销，取决于传参的类型，左值和右值的比例，这个类型是否需要动态分配内存，以及，如果需要分配内存的话，赋值操作符的具体实现，还有赋值目标所占的内存是否至少和赋值源所占的内存一样大。对于`std::string`来说，开销还取决于实现是否使用了 SSO（参考**条款 29**），如果是，那么要赋的值是否匹配 SSO 缓冲区。

最后要注意的一点是，按值传参肯定会导致**对象切片（object slicing）** 的问题，所以基类类型不适合用于按值传递：

```cpp
class Widget { ... };                           //基类

class SpecialWidget : public Widget { ... };    //派生类

void processWidget(Widget w);                   // 针对任意类型的 Widget 的函数
                                                // 包括派生类型
... 

SpecialWidget sw;
...

processWidget(sw);                              // 发生对象切片，processWidget 只能看到 Widget
                                                // 而非 SpecialWidget
```

### 条款 42：考虑置入而非插入

假如你想向 STL 容器中添加新元素，**插入函数（insertion function）** 通常是合乎逻辑的选择，但对于性能狂人而言，其背后所隐含的临时对象带来的开销是难以忍受的。考虑以下代码：

```cpp
std::vector<std::string> vs;    // 持有 std::string 对象的容器
vs.push_back("xyzzy");          // 添加字符串字面量
```

此处添加的字符串字面量和`std::string`类型并不匹配，因此需要先创建一个`std::string`类型的临时对象，然后再将其绑定到`push_back`函数的右值引用形参。换句话说，你可以把这句调用看作下面这样：

```cpp
vs.push_back(std::string("xyzzy"));
```

在这之后，`push_back`会在`std::vector`中构造出一个形参的副本，这个过程是通过调用移动构造函数来完成的（这已经是第二次调用构造函数了）。在`push_back`返回后，临时对象将立刻被销毁，这又调用了`std::string`的析构函数。

从原理上来说，**置入函数（emplacement function）** 在大部分时候应该比插入函数更高效，而且不会有更低效的可能性。`emplace_back`函数使用了完美转发，因此调用它不会带来任何的临时对象：

```cpp
vs.emplace_back("xyzzy");
```

但令人遗憾的是，插入函数还是有可能比置入函数更快的，这取决于传递的实参类型，使用的容器种类，置入或插入到容器中的位置，容器中类型的构造函数的异常安全性，和对于禁止重复值的容器（`std::set`，`std::map`，`std::unordered_set`和`set::unordered_map`）而言，要添加的值是否已经在容器中。不过在以下这些情况，置入函数很有可能会运行得更快：

- 待添加的值是通过构造而非赋值方式加入容器。一个反例是向`std::vector`中已经被占据的位置置入对象：

```cpp
std::vector<std::string> vs;
...                                 // 向 vs 中添加元素

vs.emplace(vs.begin(), "xyzzy");    // 向 vs 的开头添加元素，该位置已经存在对象
                                    // 使用的是赋值而非构造方式
```

> 基于节点的容器一般都使用构造来添加新元素，而大多数标准库容器都是基于节点的，除了`std::vector`，`std::deque`和`std::string`等（`std::array`也不是基于节点的，但是它不支持置入和插入，所以和我们的讨论无关）。在不是基于节点的容器中，你可以确信`emplace_back`是使用构造来向容器添加元素的，这对于`std::deque`的`emplace_front`也同样成立。

- 传递的实参类型和容器所持有的类型不同。
- 容器不会因为存在重复值而拒绝待添加的值。

在面对`new Widget`这样的表达式时，置入函数也没有什么优势。考虑以下两种向`std::shared_ptr`容器中添加新元素的方式：

```cpp
void killWidget(Widget* pWidget);   // 自定义删除器

ptrs.push_back(std::shared_ptr<Widget>(new Widget, killWidget));
// 和 ptrs.push_back({ new Widget, killWidget }) 等价

ptrs.emplace_back(new Widget, killWidget);
```

此处使用`push_back`仍然会创建出`std::shared_ptr`类型的临时对象，但该临时对象却拥有了正面意义，如果在为链表节点分配内存时抛出了内存不足的异常，那么该`std::shared_ptr`临时对象可以自动调用`killWidget`来释放`Widget`的内存；但在使用`emplace_back`的情况下，起到保障作用的`std::shared_ptr`临时对象将不再存在，如果发生同样的异常，那么`Widget`的内存将不可避免地被泄漏。

如果你参考**条款 21** 所述，使用独立语句将`new Widget`产生的指针转交给资源管理对象，那么使用置入函数和插入函数的效果也是差不多的：

```cpp
std::shared_ptr<Widget> spw(new Widget, killWidget);

ptrs.push_back(std::move(spw));
// 或 ptrs.emplace_back(std::move(spw))
```

最后需要注意的一点是，置入函数可能会执行在插入函数中会被拒绝的类型转换。这是因为置入函数使用的是直接初始化，而插入函数使用的是拷贝初始化，只有直接初始化会将带有`explicit`限定符的构造函数纳入考虑范围。因此在使用置入函数时，要特别关注是否传递了正确的实参：

```cpp
std::vector<std::regex> regexes;    // C++11 提供了对正则表达式的支持

regexes.push_back(nullptr);         // 无法通过编译！
regexes.emplace_back(nullptr);      // 能通过编译，但会产生未定义行为
                                    // 相当于执行 std::regex(nullptr)
```



