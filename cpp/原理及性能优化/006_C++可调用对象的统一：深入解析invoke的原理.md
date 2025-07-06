# C++可调用对象的统一：深入解析invoke的原理

## **0.简介**

在C++编程中，可调用对象的种类有很多（比如普通函数、函数指针、成员函数指针等），在C++17之前，调用这些可调用对象需要使用各自特定的语法，为了解决这个问题，C++17引入了[invoke函数模板](https://zhida.zhihu.com/search?content_id=259315677&content_type=Article&match_order=1&q=invoke函数模板&zhida_source=entity)，通过一种统一方式调用各种可调用对象，让开发者无需关心可调用对象的具体类型，从而简化代码，提高代码通用性。本文将从invoke的原理，源码实现，使用三个方面进行深入解读。

## **1.原理**

invoke的实现主要依靠以下几个特性：

1）[SFINAE](https://zhida.zhihu.com/search?content_id=259315677&content_type=Article&match_order=1&q=SFINAE&zhida_source=entity)（Substitution Failure Is Not An Error）：模板展开失败不是错误，而是会继续匹配其他版本，通过这个实现匹配不同类型的调用对象。

2）[类型推导](https://zhida.zhihu.com/search?content_id=259315677&content_type=Article&match_order=1&q=类型推导&zhida_source=entity)：使用 decltype 和 invoke_result 的手段精确推导返回类型。

3）[完美转发](https://zhida.zhihu.com/search?content_id=259315677&content_type=Article&match_order=1&q=完美转发&zhida_source=entity)：通过完美转发来实现参数值类型的完美传递。

4）异常规范一致性：通过是否允许抛出异常的判断来实例化代码，保持和原有函数一致的属性。

## **2.源码解读**

上面介绍了invoke的实现原理，此处将展示代码中如何利用这两个特性去实现invoke，其中实现思路是可以借鉴的，代码不同平台可能不一致，此处以g++的编译器为例来看，外层代码较为简单：

```text
/// Invoke a callable object.
  template<typename _Callable, typename... _Args>
    inline _GLIBCXX20_CONSTEXPR invoke_result_t<_Callable, _Args...>
    invoke(_Callable&& __fn, _Args&&... __args)
    noexcept(is_nothrow_invocable_v<_Callable, _Args...>)
    {
      return std::__invoke(std::forward<_Callable>(__fn),
         std::forward<_Args>(__args)...);
    }
```

可以通过这最外层代码来逐步解读，首先看声明，使用的是模板和可变参数，其返回值为invoke_result_t<_Callable, _Args...>，内部本质上就是使用decltype来进行的类型推导；然后接下来一行是invoke声明，参数通过万能引用传递；再接下来就是noexcept(is_nothrow_invocable_v<_Callable, _Args...>)，其内函数作用是判断是否能抛出异常，保证和传入可调用对象一致；再来看其函数体，里面使用完美转发的内容，我们来看一下__invoke里面具体的实现，其内部调用了__invoke_impl（其通过SFINAE来实现不同的可调用对象调用，简单列举部分）：

```text
template<typename _Res, typename _Fn, typename... _Args>
    constexpr _Res
    __invoke_impl(__invoke_other, _Fn&& __f, _Args&&... __args)
    { return std::forward<_Fn>(__f)(std::forward<_Args>(__args)...); }
  template<typename _Res, typename _MemFun, typename _Tp, typename... _Args>
    constexpr _Res
    __invoke_impl(__invoke_memfun_ref, _MemFun&& __f, _Tp&& __t,
      _Args&&... __args)
    { return (__invfwd<_Tp>(__t).*__f)(std::forward<_Args>(__args)...); }
  template<typename _Res, typename _MemFun, typename _Tp, typename... _Args>
    constexpr _Res
    __invoke_impl(__invoke_memfun_deref, _MemFun&& __f, _Tp&& __t,
      _Args&&... __args)
    {
      return ((*std::forward<_Tp>(__t)).*__f)(std::forward<_Args>(__args)...);
    }
```

## **3.使用例子**

我们可以以使用invoke进而让容器统一存储可调用对象为例：

```text
#include <iostream>
#include <functional>
#include <string>
#include <memory>
#include <vector>
#include <utility>
// 1. 定义普通函数
int sub(int a, int b) {
    std::cout << "sub"<<std::endl;
    return a - b;
}
// 2. 定义类
class MulCalculator {
public:
    int mul(int a, int b) const {
        std::cout << "mul"<<std::endl;
        return a * b;
    }
    static std::string name() {
        std::cout << "name"<<std::endl;
        return "MulCalculator";
    }
};
// 3. 定义函数对象类
struct Divider{
    double operator()(double a, double b) const {
        std::cout << "div"<<std::endl;
        return a / b;
    }
};
int main() {
    MulCalculator calc;
    Divider divider;
    using FuncType = std::function<void()>;
    std::vector<FuncType> functions;
    functions.push_back([&]() { std::invoke(&MulCalculator::mul, calc, 1, 2); });
    
    functions.push_back([&]() { std::invoke(&MulCalculator::name); });
    
    functions.push_back([&]() { std::invoke(divider, 10, 20); });
    
    functions.push_back([&]() { std::invoke(sub, 10, 20); });
    for (auto& func : functions) {
    
        func();
    
    }
    return 0;
}
```

