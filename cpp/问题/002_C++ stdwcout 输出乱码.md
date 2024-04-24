# C++ std::wcout 输出乱码



### 遇到问题

最近使用 C++ 遇到了中文处理问题，今天下午想测试 C++ 宽字符使用时，发现如下程序并不能如愿输出汉字。

```
int main()
{
    std::wstring wstr(L"你好");
    std::wcout << wstr << std::endl;
    return 0;
}
```

在网上多处搜索处理方法后仍不能解决，于是自己探索了一波。收获蛮大的，于是记录下。

> 该文章程序及结果皆在 Linux 环境下。



### 结论

结论是我在一些程序中总结出来的，其中若有错误，希望指正。

1. Linux 下 `printf` 和 `wprintf` 不能混用，即同时使用。具体原因可自行搜索，网上有很多资料。
2. C++ 中默认情况下 `std::cout` 与 `std::wcout` 分别与 `printf` 和 `wprintf` 同步，维护共同缓冲区。为方便理解，我们可以假设 `std::cout` 调用 `printf`，`std::wcout` 调用 `wprintf`（这里只是假设，方便我们理解）。
3. 综合上述 1、2 两条，可知在开启 `sync_with_stdio` 情况下 `std::cout` 及 `std::wcout` 不能混用。 先使用 `std::cout` 后使用 `std::wcout` 会造成 `std::wcout` 输出乱码，先使用 `std::wcout` 的话 `std::cout` 压根不能输出东西。 关闭情况下两者可混用。
4. C 语言库中设置 `locale` 函数为 `setlocale()`， C++ 语言库中设置 `std::locale` 函数为 `std::locale::global()`。可认为 C 语言库和 C++ 库分别有不同的 `locale` 。`setlocale()` 函数仅设置 C 语言中的 `locale`，而 `std::locale::global()` 函数两者都会设置。
5. 开启 `sync_with_stdio` 情况下，可认为 `std::cout` 与 `std::wcout` 使用的是 C 语言中的 `locale`。而关闭情况下则使用 C++ 自己的 `std::locale`。
6. `std::wcout` 输出中文时依靠地区 `locale`，故我们在使用其输出中文之前要设置 `locale`。
7. 在开启 `sync_with_stdio` 情况下，想用 `std::wcout` 输出汉字要设置 C 语言中的 `locale`，使用两种方法都可以。而关闭情况下，只能使用 C++ 的方法，**且要在关闭同步之前设置**，也可以使用 `std::wcout.imbus( std::locale("") )` 单独设置。

### 其他收获

- `std::cout` 中 c 表示字符， `std::wcout` 中 wc 表示宽字符。

### 测试程序

这里展示部分测试程序及运行结果（Ubuntu 下），测试程序可根据结论中的几点自行结合测试。

#### 开启 sync_with_stdio （默认）

```
int main()
{
    std::wstring wstr = L"你能输出中文";
    std::wcout << wstr << std::endl;
    return 0;
}
```

> ??????

“你能输出中文” 共 6 个汉字，输出中共 6 个问号。 `wstr` 中有多少个汉字，输出中就多少个 ？

------

```
int main()
{
    std::locale::global( std::locale("") );
    //setlocale() 与上句相替换结果一样
    std::wstring wstr = L"你能输出中文";
    std::wcout << wstr << std::endl;
    return 0;
}
```

> 你能输出中文

------

```
int main()
{
    std::cout << "你好" << std::endl;
    std::wstring wstr = L"你能输出中文？";
    std::wcout << wstr << std::endl;
    return 0;
}
```

> 你好
>
> wcout 输出乱码

------

```
int main()
{
    std::wstring wstr = L"你能输出中文？";
    std::wcout << wstr << std::endl;
    std::cout << "你好" << std::endl;
    std::cout << "你好" << std::endl;
    return 0;
}
```

> ??????

`cout` 并不会输出

------

#### 关闭 sync_with_stdio

```
int main()
{
    std::ios::sync_with_stdio(false);
    // setlocale( LC_ALL, ""); //有没有这句输出都一样
    std::wstring wstr = L"你能输出中文？";
    std::wcout << wstr << std::endl;
    return 0;
}
```

> 输出为空

------

```
int main()
{
    std::ios::sync_with_stdio(false);
    std::locale::global( std::local("") );
    std::wstring wstr = L"你能输出中文？";
    std::wcout << wstr << std::endl;
    return 0;
}
```

> 你能输出中文？

------

```
int main()
{
    std::ios::sync_with_stdio(false);
    std::locale::global( std::local("") );		//删除此句结果相同
    std::cout << "你好" << std::endl;
    std::wstring wstr = L"你能输出中文？";
    std::wcout << wstr << std::endl;
    return 0;
}
```

> 你好

------

```
int main()
{
    std::ios::sync_with_stdio(false);
    std::wcout.imbue( std::locale("") );
    std::cout << "你好" << std::endl;
    std::wstring wstr = L"你能输出中文？";
    std::wcout << wstr << std::endl;
    return 0;
}
```

> 你好
>
> 你能输出中文？





测试代码:

```
#include <iostream>
#include "Log.h"
#include <array>
#include <string>
#include <Windows.h>
#include <locale>

class Point {
    int x;
    int y;

public:
    void print() {
        std::cout << "{x:" << x << ", y:" << y << "}" << std::endl;
    }
};

class Entity {
    // 默认构造
    // Entity(){}

    // 删除默认构造, 比如用于单例模式,
    // Entity() = delete
public:
    int x;
    int y;
};

// 设置枚举的底层类型
enum Example {
    A = 1,
    B = 2,
    C = 3,
};

int main() {
    SetConsoleOutputCP(CP_UTF8);
    std::locale loc( "chs" );
//locale loc( "Chinese-simplified" );
    std::wcout.imbue(loc);
    std::locale::global( std::locale("") );

    std::cout << "Hello, World!" << std::endl;
    // '<<' 操作符重载, '<<' 实际上是方法, 类似于 std::cout.print("Hello, World!").print(std::endl);

    int a = 1;
    int b = 10;
    std::cout << "line1" << std::endl;
    std::cout << "line2" << std::endl;

    log("this is log");
    char result[20];
    sprintf(result, "a+b = %d", a + b);
    log(reinterpret_cast<const char *>(result));

    if (b == true) {
        log("b==true");
    } else {
        log("b!=true");
    }

    int c = b + true;
    std::cout << "c:" << c << std::endl;

    Point p;
    int *px = (int *) &p;
    *px = 8;
    p.print();

    Entity entity;
    // error
    // std::cout << entity.x << std::endl;

    std::array<int, 5> nums;
    for (int i = 0; i < nums.size(); i++) {
        nums[i] = i;
    }

    for (const auto &item: nums) {
        std::cout << item << std::endl;
    }

//    std::string str1 = "aa"+"bb";
    std::string str1 = std::string("aa") + "bb";
    std::cout << "str1: " << str1 << std::endl;

    using namespace std::string_literals;
    std::string str2 = "aa"s + "bb";
    std::cout << "str2: " << str2 << std::endl;

    std::string str3 = "测试";
    std::cout << "str3: " << str3 << std::endl;

    const wchar_t *str4 = L"aaabbb";
    std::wcout<<"str4:"<<str4<<std::endl;

    const wchar_t *str41 = L"中文测试";
    std::wcout<<"str41:"<<str41<<std::endl;

    const char16_t *str5 = u"aaabbb";
    // 输出 char16_t 类型的字符串
    std::cout << "UTF-16 string: ";
    for (int i = 0; str5[i] != u'\0'; ++i) {
        std::cout << static_cast<char>(str5[i]); // 将 char16_t 转换为 char 输出
    }
    std::cout << std::endl;

    const char32_t *str6 = U"aaabbb";
    // 输出 char32_t 类型的字符串
    std::cout << "UTF-32 string: ";
    for (int i = 0; str6[i] != U'\0'; ++i) {
        std::cout << static_cast<char>(str6[i]); // 将 char32_t 转换为 char 输出
    }
    std::cout << std::endl;

    const char* str7 = R"(---
aaa
bbb
)";
    std::cout << "str7: " << str7 << std::endl;


    // 使用 char 类型输出中文
    std::cout << "这是一段中文文本。" << std::endl;

    // 使用 wchar_t 类型输出中文
    const wchar_t *chineseStr = L"这也是一段中文文本。";
    std::wcout << "chineseStr: "<<chineseStr << std::endl;

    setlocale(LC_ALL ,"chs");
    std::wcout << L"你好，世界!" << std::endl; // 输出宽字符字符串


    std::wcout << L"中国" << std::endl;

    std::cout << "end" << std::endl;
    int input = std::cin.get();
    return 0;
}
```

