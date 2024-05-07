# #pragma 详解

在#Pragma 是预处理指令它的作用是设定编译器的状态或者是指示编译器完成一些特定的动作。 #pragma 指令对每个编译器给出了一个方法,在保持与 C 和 C ++语言完全兼容的情况下,给出主机或操作系统专有的特征。依据定义,编译指示是机器或操作系统专有的,且对于每个编译器都是不同的。其格式一般为: 

```
#Pragma Para
```

其中 Para 为参数，下面来看一些常用的参数。



(1)message 参数。 

Message 参数是我最喜欢的一个参数，它能够在编译信息输出窗口中输出相应的信息，这对于源代码信息的控制是非常重要的。其使用方法为：

```
#Pragma message(“消息文本” )
```

当编译器遇到这条指令时就在编译输出窗口中将消息文本打印出来。当我们在程序中定义了许多宏来控制源代码版本的时候，我们自己有可能都会忘记有没有正确的设置这些宏，此时我们可以用这条指令在编译的时候就进行检查。假设我们希望判断自己有没有在源代码的什么地方定义了`_X86`这个宏可以用下面的方法

```
#ifdef _X86
#Pragma message(“_X86 macro activated!”) 
#endif
```

当我们定义了_X86这个宏以后，应用程序在编译时就会在编译输出窗口里显示 “ _X86 macro activated!” 。我们就不会因为不记得自己定义的一些特定的宏而抓耳挠腮了。



(2)另一个使用得比较多的 pragma 参数是 code_seg。格式如：

```
#pragma code_seg( [\section-name\[,\section-class\] ] )
```

它能够设置程序中函数代码存放的代码段，使用没有 section-name 字符串的#pragmacode_seg 可在编译开始时将其复位，当我们开发驱动程序

的时候就会使用到它。



(3)#pragma once (比较常用）

只要在头文件的最开始加入这条指令就能够保证头文件被编译一次，这条指令实际上在 VC6中就已经有了，但是考虑到兼容性并没有太多的使用它。



(4)#pragma hdrstop 表示预编译头文件到此为止，后面的头文件不进行预编译。 

BCB 可以预编译头文件以加快链接的速度，但如果所有头文件都进行预编译又可能占太多磁盘空间，所以使用这个选项排除一些头文件。有时单元之间有依赖关系，比如单元 A 依赖单元 B，所以单元 B 要先于单元 A 编译。你可以用

```
#pragma startup
```

指定编译优先级，如果使用了

```
#pragma package(smart_init)
```

BCB 就会根据优先级的大小先后编译。



(5)#pragma resource \*.dfm\表示把*.dfm 文件中的资源加入工程。 

*.dfm 中包括窗体外观的定义。



(6） #pragma warning( disable : 4507 34; once : 4385; error : 164 )

等价于：

```
#pragma warning(disable:4507 34) // 不显示4507和34号警告信息
#pragma warning(once:4385) // 4385号警告信息仅报告一次
#pragma warning(error:164) // 把164号警告信息作为一个错误。
```

同时这个 pragma warning 也支持如下格式：

```
#pragma warning( push [ ,n ] ) 
#pragma warning( pop )
```



这里 n 代表一个警告等级(1---4)。



保存所有警告信息的现有的警告状态。

```
#pragma warning( push ) 
```



保存所有警告信息的现有的警告状态，并且把全局警告等级设定为 n。

```
#pragma warning( push, n) 
```



向栈中弹出最后一个警告信息，在入栈和出栈之间所作的一切改动取消。

```
#pragma warning( pop )
```

例如：

```
#pragma warning( push ) 
#pragma warning( disable : 4705 ) 
#pragma warning( disable : 4706 ) 
#pragma warning( disable : 4707 ) 
//.......
#pragma warning( pop )
```

在这段代码的最后，重新保存所有的警告信息(包括4705， 4706和4707)。



（ 7） pragma comment(...)

该指令将一个注释记录放入一个对象文件或可执行文件中。常用的 lib 关键字，可以帮我们连入一个库文件。



（ 8）·通过#pragma pack(n)改变 C 编译器的字节对齐方式在 C 语言中，结构是一种复合数据类型，其构成元素既可以是基本数据类型（如 int、long、 float  等）的变量，也可以是一些复合数据类型（如数组、结构、联合等）的数据单元。在结构中，编译器为结构的每个成员按其自然对界（ alignment）条件分配空间。各个成员按照它们被声明的顺序在内存中顺序存储，第一个成员的地址和整个结构的地址相同。

例如，下面的结构各成员空间分配情况：

```
struct test {
char x1; 
short x2; 
float x3;
char x4; 
};
```

结构的第一个成员 x1，其偏移地址为0，占据了第1个字节。第二个成员 x2为short 类型，其起始地址必须2字节对界，因此，编译器在 x2和 x1之间填充了一个空字节。结构的第三个成员 x3和第四个成员 x4恰好落在其自然对界地址上，在它们前面不需要额外的填充字节。在 test 结构中，成员 x3要求4字节对界，是该结构所有成员中要求的最大对界单元，因而 test 结构的自然对界条件为4字节，编译器在成员 x4后面填充了3个空字节。整个结构所占据空间为12字节。



更改 C编译器的缺省字节对齐方式

在缺省情况下， C 编译器为每一个变量或是数据单元按其自然对界条件分配空间。一般地，可以通过下面的方法来改变缺省的对界条件：



· 使用伪指令

```
#pragma pack (n)
```

C 编译器将按照 n 个字节对齐。



· 使用伪指令

```
#pragma pack ()
```

取消自定义字节对齐方式。



另外，还有如下的一种方式：

```
__attribute((aligned (n)))
```

让所作用的结构成员对齐在 n 字节自然边界上。



如果结构中有成员的长度大于 n，则按照最大成员的长度来对齐。

```
__attribute__ ((packed))
```

取消结构在编译过程中的优化对齐，按照实际占用字节数进行对齐。以上的 n = 1, 2, 4, 8, 16... 第一种方式较为常见。



应用实例

在网络协议编程中，经常会处理不同协议的数据报文。一种方法是通过指针偏移的方法来得到各种信息，但这样做不仅编程复杂，而且一旦协议有变化，程序修改起来也比较麻烦。在了解了编译器对结构空间的分配原则之后，我们完全可以利用这一特性定义自己的协议结构，通过访问结构的成员来获取各种信息。这样做，不仅简化了编程，而且即使协议发生变化，我们也只需修改协议结构的定义即可，其它程序无需修改，省时省力。下面以 TCP 协议首部为例，说明如何定义协议结构。

其协议结构定义如下：

```
#pragma pack(1) // 按照1字节方式进行对齐

struct TCPHEADER {
short SrcPort; // 16位源端口号
short DstPort; // 16位目的端口号
int SerialNo; // 32位序列号
int AckNo; // 32位确认号
unsigned char HaderLen : 4; // 4位首部长度
unsigned char Reserved1 : 4; // 保留6位中的4位
unsigned char Reserved2 : 2; // 保留6位中的2位
unsigned char URG : 1; 
unsigned char ACK : 1; 
unsigned char PSH : 1; 
unsigned char RST : 1; 
unsigned char SYN : 1; 
unsigned char FIN : 1;

short WindowSize; // 16位窗口大小
short TcpChkSum; // 16位 TCP 检验和
short UrgentPointer; // 16位紧急指针
};
#pragma pack() // 取消1字节对齐方式
```



指定连接要使用的库

比如我们连接的时候用到了 WSock32.lib，你当然可以不辞辛苦地把它加入到你的工程中。但是我觉得更方便的方法是使用 #pragma 指示符，指定要连接的库:

```
#pragma comment(lib, "WSock32.lib")
```



附加：

每种 C 和 C++的实现支持对其宿主机或操作系统唯一的功能。例如，一些程序需要精确控制超出数据所在的储存空间，或着控制特定函数接受参数的方式。#pragma 指示使每个编译程序在保留 C 和 C++语言的整体兼容性时提供不同机器和操作系统特定的功能。编译指示被定义为机器或操作系统特定的，并且通常每种编译程序是不同的。



语法：

```
#pragma token_string
```

“ token_string”是一系列字符用来给出所需的特定编译程序指令和参数。数字符号“ #”必须是包含编译指令的行中第一个非空白字符；而空白字符可以隔开数字符号“ #”和关键字“ pragma”。在#pragma 后面，写任何翻译程序能够作为预处理符号分析的文本。 #pragma 的参数类似于宏扩展。

如果编译程序发现它不认得一个编译指示，它将给出一个警告，可是编译会继续下去。

为了提供新的预处理功能，或者为编译程序提供由实现定义的信息，编译指示可以用在一个条件语句内。 C 和 C++编译程序可以识别下列编译程序指令。

```
alloc_text 
comment 
init_seg* 
optimize 
auto_inline 
component 
inline_depth 
pack 
bss_seg 
data_seg
inline_recursion 
pointers_to_members*
check_stack 
function 
intrinsic 
setlocale 
code_seg 
hdrstop 
message 
vtordisp*
const_seg 
include_alias 
once 
warning
```

*仅用于 C++编译程序。



1 alloc_text

```
#pragma alloc_text( "textsection", function1, ... )
```

命名特别定义的函数驻留的代码段。该编译指示必须出现在函数说明符和函数定义之间。

alloc_text 编译指示不处理 C++成员函数或重载函数。它仅能应用在以 C连接方式说明的函数——就是说，函数是用 extern "C"连接指示符说明的。如果你试图将这个编译指示应用于一个具有 C++连接方式的函数时，将出现一个编译程序错误。



由于不支持使用__based 的函数地址，需要使用 alloc_text 编译指示来指定段位置。由 textsection 指定的名字应该由双引号括起来。



alloc_text 编译指示必须出现在任何需要指定的函数说明之后，以及这些函数的定义之前。



在 alloc_text 编译指示中引用的函数必须和该编译指示处于同一个模块中。如果不这样做，使以后一个未定义的函数被编译到一个不同的代码段时，错误会也可能不会被捕获。即使程序一般会正常运行，但是函数不会分派到应该在的段。



alloc_text 的其它限制如下：

它不能用在一个函数内部。

它必须用于函数说明以后，函数定义以前。



2 auto_inline

```
#pragma auto_inline( [{on | off}] )
```

当指定 off 时将任何一个可以被考虑为作为自动嵌入扩展候选的函数排除出该范围。为了使用 auto_inline 编译指示，将其紧接着写在一个函数定义之前或之后（不是在其内部）。 该编译指示将在其出现以后的第一个函数定义开始起作用。 auto_inline 编译指示对显式的 inline 函数不起作用。



3 bss_seg

```
#pragma data_seg( ["section-name"[, "section-class"] ] )
```

为未初始化数据指定缺省段。 data_seg 编译指示除了工作于已初始化数据而不是未初始化的以外具有一样的效果。在一些情况下，你能使用 bss_seg

将所有未初始化数据安排在一个段中来加速你的装载时间。

```
#pragma bss_seg( "MY_DATA" )
```

将导致把#pragma 语句之后的未初始化的数据安排在一个叫做 MY_DATA的段中。用 bss_seg 编译指示分配的数据不包含任何关于其位置的信息。



第二个参数 section-class 是用于兼容2.0版本以前的 Visual C++的，现在将忽略它。



4 check_stack

```
#pragma check_stack([ {on | off}] ) 
#pragma check_stack{+ | –}
```

如果指定 off（或者“ -”）指示编译程序关闭堆栈探测，或者指定 on（或“ +”）打开堆栈探测。如果没有给出参数，堆栈探测将根据默认设置决定。该编译指示将在出现该指示之后的第一个函数开始生效。堆栈探测既不是宏和能够生成嵌入代码函数的一部分。

如果你没有给出 check­_stack 编译指示的参数，堆栈检查将恢复到在命令行指定的行为。详细情况见编译程序参考。 #pragma check_stack 和/Gs 选项的互相作用情况在表2.1中说明。

表 2.1 使用 check_stack 编译指示编译指示

用/Gs 选项编译？行为



\#pragma check_stack()或#pragma check_stack

是 后续的函数关闭堆栈检查



\#pragma check_stack()或#pragma check_stack

否 后续的函数打开堆栈检查



\#pragma check_stack(on)或#pragma check_stack(+)

是或者否



后续的函数打开堆栈检查



\#pragma check_stack(off)或#pragma check_stack(-)

是或者否



后续的函数关闭堆栈检查



5 code_seg



\#pragma code_seg( ["section-name"[,"section-class"] ] )



指定分配函数的代码段。 code_seg 编译指示为函数指定默认的段。你也能够像段名一样指定一个可选的类名。使用没有段名字符串的#pragma code_seg

将恢复分配到编译开始时候的状态。



6 const_seg



\#pragma const_seg( ["section-name"[, "section-class"] ] )



指定用于常量数据的默认段。 data_seg 编译指示除了可以工作于所有数据



以外具有一样的效果。你能够使用该编译指示将你的常量数据保存在一个只



读的段中。



\#pragma const_seg( "MY_DATA" )



导致在#pragma 语句后面的常量数据分配在一个叫做 MY_DATA 的段中。用 const_seg 

编译指示分配的数据不包含任何关于其位置的信息。



第二个参数 

section-class 是用于兼容2.0版本以前的 Visual C++

的，现在将忽略它。



7 comment



\#pragma comment( comment-type [, commentstring] )



将描述记录安排到目标文件或可执行文件中去。 comment-type 是下面说明的 五 个 预 定 义 标 识 符 中 的 一 个 ， 用 来 指 定 描 述 记 录 的 类 型 。 可 选 的commentstring 是一个字符串文字值用于为一些描述类型提供附加的信息。因为 commentstring 是一个字符串文字值，所以它遵从字符串文字值的所有规则，例如换码字符、嵌入的引号（ "

）和联接。



7-1 compiler



在目标文件中放置编译程序名和版本号。该描述记录被连接程序忽略。如



果你为这个记录类型提供一个 

commentstring 

参数，编译程序将生成一个警告。



7-2 exestr



将 commentstring 

放置到目标文件中去。在连结时，这个字符串再被放到可执行文件去中。当可执行文件被装载时这个字符串不会被装入内存，然而，



它可以被一个能够在文件中搜索可打印字符串的程序找到。该描述记录的一



个用处是在可执行文件中嵌入版本号或者类似的信息。



7-3 lib



将一个库搜索记录放置到目标文件中去。该描述类型必须有包含你要连接



程序搜索的库名（和可能的路径）的 

commentstring 

参数。因为在目标文件中该库名先于默认的库搜索记录，所以连接程序将如同你在命令行输入这些库



一样来搜索它们。你可以在一个源文件中放置多个库搜索记录，每个记录将



按照它们出现在源文件中的顺序出现在目标文件中。



7-4 linker



在目标文件中放置连接程序选项。你可以用这个描述类型指定连接程序选项来代替在 Project Setting 对话框中 Link 页内的选项。例如，你可以指定/include 

选项以强迫包含一个符号：



\#pragma comment(linker, "/include:__mySymbol")



7-5 user



在目标文件中包含一个普通描述记录。 commentstring 

参数包含描述的文本。该描述记录将被连接程序忽略。



下面的编译指示导致连接程序在连接时搜索 

EMAPI.LIB 库。连接程序首先在当前工作目录然后在 LIB 

环境变量指定的路径中搜索。



\#pragma comment( lib, "emapi" )



下面的编译指示导致编译程序将其名字和版本号放置到目标文件中去。



The following pragma causes the compiler to place the name and version number of the compiler in the object file:



\#pragma comment( compiler )



注意，对于具有 commentstring 

参数的描述记录，你可以使用其它用作字符串文字量的宏来提供宏扩展为字符串文字量。你也能够联结任何字符串文



字量和宏的组合来扩展成为一个字符串文字量。例如，下面的语句是可以接



受的：



\#pragma comment( user, "Compiled on " __DATE__ " at " __TIME__ )



8 component



\#pragma component( browser, { on | off }[, references [, name ]] ) #pragma component( minrebuild, on | off )



从源文件内控制浏览信息和依赖信息的收集。8-1 浏览信息（ Browser

）



你可以将收集打开或关闭，你也可以指定收集时忽略特别的名字。使用 

on 或 off 在编译指示以后控制浏览信息的收集。例如： 

\#pragma component(browser, off)



终止编译程序收集浏览信息。



注意，为了用这个编译指示打开浏览信息的收集，必须先从 

Project Setting

对话框或者命令行允许浏览信息。



references 选项可以有也可以没有 name 参数。使用没有 name 参数的references 

选项将打开或者关闭引用信息的收集（然而继续收集其它浏览信息）。 例如：



\#pragma component(browser, off, references)



终止编译程序收集引用信息。



使用有 

name 和 off 参数的 references 

选项将阻止从浏览信息窗口中出现引用到的名字。用这个语法将忽略你不感兴趣的名字和类型从而减少浏览信息



文件的大小。例如：



\#pragma component(browser, off, references, DWORD)



从这一点以后忽略 DWORD 的引用。你能够用 on 恢复 DWORD 

的引用收集：



\#pragma component(browser, on, references, DWORD)



这是唯一的方法可以恢复收集指定名字的引用，你必须显式地打开任何你



关闭的名字。



为了防止预处理程序扩展名字（就像扩展 

NULL 到0）， 用引号括起来： 

\#pragma component(browser, off, references, "NULL")



8-2 

最小化重建（ Minimal Rebuild

）



Visual C++的最小化重建功能要求编译程序创建并保存需要大量磁盘空间的 C++类依赖信息。为了节省磁盘空间，你能够在你不需要收集依赖信息时使用#pragma component(minrebuild,off)，例如，没有改变过头文件。在未修改过的类之后插入#pragma component(minrebuild,on)

重新打开依赖信息。



详见 

Enable Minimal Rebuild(/Gm)

编译程序选项。



9 data_seg



\#pragma data_seg( ["section-name"[, "section-class"] ] )



指定数据的默认段。例如：



\#pragma data_seg( "MY_DATA" )



导致在#pragma 语句后分配的数据保存在一个叫做 MY_DATA 的段中。用 data_seg 

编译指示分配的数据不包含任何关于其位置的信息。



第二个参数 

section-class 是用于兼容2.0版本以前的 Visual C++

的，现在将忽略它。



10 function



\#pragma function( function1 [, function2, ...] )



指定必须生成对编译指示中参数列表内函数的调用。如果你使用 intrinsic编译指示（或者/Oi）来告诉编译程序生成内含函数（内含函数如同嵌入代码一样生成，不作为一个函数调用）， 你能够用 function 编译指示显式地强迫函数调用。当遇到一个 function 编译指示，它将在其后面遇到的第一个包含有



内含函数的函数定义处生效。其持续作用到源文件的尾部或者出现对同一个



内含函数指定 

intrinsic 编译指示。 function 

编译指示只能用于函数外——在全局层次。



为了列出具有内含形式的函数表，参见

\#pragma intrinsic

。



11 hdrstop



\#pragma hdrstop [( "filename" )]



控制预编译头文件的工作方式。 filename 是要使用或者创建（依赖于是否指定了/Yu 或/Yc）预编译头文件的名字。如果 filename 不包括一个指定路径，将假定预编译头文件和源文件处于同一个目录中。当指定自动预编译头文件选项/YX 

时，所有指定的文件名将被忽略。



如果有

/YX 或者/Yc 选项，而且 C 或 C++文件包含了一个 hdrstop 

编译指示时，编译程序保存编译指示之前的编译状态。编译指示之后的编译状态不



被保存。



hdrstop 

编译选项不能出现在一个头文件内。它只能出现在源文件的文件级，它也不能出现在任何数据或者函数的说明或定义之中。



注意，除非指定没有文件名的

/YX 选项或者/Yu 或/Yc 选项，否则 hdrstop

编译指示将被忽略。



用一个文件名命名要保存编译状态的预编译头文件。在 

hdrstop 和 filename之间的空格是可选的。在 hdrstop 编译指示中的文件名是一个字符串，这样它服从于 C 或 C++

的字符串规则。特别的，你必须像下面例子里面显示的用引号括起来。



\#pragma hdrstop( "c:\projects\include\myinc.pch" )



预编译头文件的文件名按照如下规则决定，按照优先次序：



/Fp 

编译程序选项的参数；



由

\#pragma hdrstop 的 filename 

参数；



原文件名的基本文件名加上

.PCH 

扩展名。



12 include_alias



\#pragma include_alias( "long_filename", "short_filename" ) #pragma include_alias( <long_filename>, <short_filename> )



指定作为 long_filename 别名的 short_filename。一些文件系统允许超出8.3FAT 文件系统限制的长头文件名。编译程序不能简单地将长文件名截断为8.3名字，因为长头文件名的前8个字符可能不是唯一的。无论何时编译程序遇到 long_filename 串，它代替 short_filename，并且用 short_filename 

搜索头



文件。这个编译指示必须出现在相应的

\#include 指示之前。例如： 

// First eight characters of these two files not unique.



\#pragma include_alias( "AppleSystemHeaderQuickdraw.h", "quickdra.h" ) #pragma include_alias( "AppleSystemHeaderFruit.h", "fruit.h" ) #pragma include_alias( "GraphicsMenu.h", "gramenu.h" )



\#include "AppleSystemHeaderQuickdraw.h" #include "AppleSystemHeaderFruit.h"



\#include "GraphicsMenu.h"



这个别名在搜索时精确匹配，包括拼写和双引号、尖括号。 include_alias

编译指示在文件名上执行简单的字符串匹配，不进行其它的文件名验证。例



如，给出下列指示：



\#pragma include_alias("mymath.h", "math.h") #include "./mymath.h"



\#include "sys/mymath.h"



并不执行别名替代，因为头文件名字符串没有精确匹配。另外，在/Yu， /Yc 和/YX 编译程序选项，或 hdrstop 编译指示中作为参数的头文件名不被替



换。例如，如果你的源文件包含下列指示：



\#include <AppleSystemHeaderStop.h>



相应的编译程序选项必须是：



/YcAppleSystemHeaderStop.h



你能够用 include­_alias 

编译指示将任何头文件映射到其它文件。例如：



\#pragma include_alias( "api.h", "c:\version1.0\api.h" ) #pragma include_alias( <stdio.h>, <newstdio.h> ) #include "api.h"



\#include <stdio.h>



不要混淆用双引号和尖括号括起来的文件名。例如，给出上面的#pragma include_alias 指示时，在下面的#include 

指示中编译程序不执行替换。



\#include <api.h> #include "stdio.h"



还有，下面的指示将产生一个错误：



\#pragma include_alias(<header.h>, "header.h") // Error



注意，在错误信息中报告的文件名，或者预定义宏__FILE__

的值，是执行替换以后的文件名。例如，在下列指示之后：



\#pragma include_alias( "VeryLongFileName.H", "myfile.h" ) #include "VeryLongFileName.H"



文件 VeryLongFileName.H 产生下列错误信息： 

myfile.h(15) : error C2059 : syntax error



还要注意的是不支持传递性。给出下面的指示：



\#pragma include_alias( "one.h", "two.h" ) #pragma include_alias( "two.h", "three.h" ) #include "one.h"



编译程序将搜索 two.h 而不是 three.h

。



13 init_seg



C++

特有



\#pragma init_seg({ compiler | lib | user | "section-name" [, "func-name"]} )



指定影响启动代码执行的关键字或代码段。因为全局静态对象的初始化可



以包含执行代码，所以你必须指定一个关键字来定义什么时候构造对象。在



使用需要初始化的动态连接库（ 

DLL）或程序库时使用 init_seg 

编译指示是尤其重要的。



init_seg 编译指示的选项有： 

13-1 compiler



由 Microsoft C 运行时间库保留。在这个组中的对象将第一个构造。

13-2 lib



用于第三方类库开发者的初始化。在这个组中的对象将在标记为构造compiler 

的对象之后，其它对象之前构造。



13-3 user



用于任何其它用户。在这个组中的对象将最后构造。



13-4 section-name



允许显式地指定初始化段。在用户指定的 section-name 中的对象将不会隐式地构造，而它们的地址将会被放置在由 section-name 

命名的段中。



13-5 func-name



指定当程序退出时，作为 atexit 函数调用的函数。这个函数必须具有和atexit 

函数相同的形式：



int funcname(void (__cdecl *)(void));



如果你需要延迟初始化，你能够选择指定显式的段名。随后你必须调用每



个静态对象的构造函数。



14 inline_depth



\#pragma inline_depth( [0... 255] )



通过控制能够被扩展的一系列函数调用（从0到255次）来控制嵌入函数扩



展的发生次数，这个编译指示控制用 inline， __inline 标记的或在/Ob2

选项下能自动嵌入的嵌入函数。



inline_depth 编译指示控制能够被扩展的一系列函数调用。例如，如果嵌入深度是4，并且如果 A 调用 B 然后调用 C，所有的3次调用都将做嵌入扩展。然而，如果设置的最近一次嵌入深度是2，则只有 A 和 B 被扩展，而 C 

仍然作为函数调用。



为了使用这个编译指示，你必须设置编译程序选项

/Ob 为1或者2。用这个编译指示指定的深度设定在该指示后面的第一个函数开始生效。如果你在括号内不指定一个值， inline_depth 设置嵌入深度到默认值8

。



在扩展时，嵌入深度可以被减少而不能被增加。如果嵌入深度是

6，同时在扩展过程中预处理程序遇到一个 inline_depth 编译指示设置为8，则深度保持为6

。



嵌入深度

0将拒绝嵌入扩展，深度255

将设置在嵌入扩展时没有限制。如果用一个没有指定值的编译指示，则使用为默认值。



15 inline_recursion



\#pragma inline_recursion( [{on | off}] )



控制直接或者相互间的递归函数调用式的嵌入扩展。用这个编译指示控制用 inline， __inline 标记的或在/Ob2选项下能自动嵌入的嵌入函数。使用这个编译指示需要设置编译程序选项/Ob 为1或者2。默认的 inline_recursion 状态是 off

。这个编译指示在出现该编译指示之后第一个函数调用起作用，并不影响函数的定义。



inline_recursion 编译指示控制如何扩展递归函数。如果 inline_recursion 是off，并且如果一个嵌入函数调用了它自己（直接的或者间接的）， 函数将仅仅



扩展一次。如果 inline_recursion 是 on,函数将扩展多次直到达到 inline_depth

的值或者容量限制。



16 intrinsic



\#pragma intrinsic( function1 [, function2, ...] )



指定对在编译指示参数表中函数调用是内含的。编译程序像嵌入代码一样



生成内含函数，而不是函数调用。下面列出了具有内含形式的库函数。一旦遇到 

intrinsic 编译指示，它从第一个包含指定内含函数的函数定义开始起作用。作用持续到源文件尾部或者出现包含相同内含函数的 function 

编译指示。



intrinsic 

编译指示只能用在函数定义外——在全局层次。下列函数具有内含形式：



_disable _enable _inp _inpw _lrotl _lrotr _outp _outpw _rotl _rotr _strset abs fabs labs memcmp memcpy memset strcat strcmp strcpy



strlen



使用内含函数的程序更快，因为它们没有函数调用的额外代价，然而因为



有附加的代码生成，可能比较大。



注意， 

_alloca 和 setjmp 函数总是内含的，这个行为不受 intrinsic 

编译指示影响。



下列浮点函数没有内含形式。然而它们具有直接将参数通过浮点芯片传送



而不是推入程序堆栈的版本。



acos asin cosh fmod pow sinh tanh



当你同时指定/Oi 和/Og 编译程序选项（或者任何包含/Og， /Ox， /O1和/O2

的选项）时下列浮点函数具有真正的内含形式。



atan exp log10 sqrt atan2 log



sin tan cos



你可以用编译程序选项/Op 或/Za 

来覆盖真内含浮点选项的生成。在这种情况下，函数会像一般库函数一样被生成，同时直接将参数通过浮点芯片传



送而不是推入程序堆栈。



17 message



\#pragma message( messagestring )



不中断编译，发送一个字符串文字量到标准输出。 message 

编译指示的典型运用是在编译时显示信息。



下面的代码段用 

message 

编译指示在编译过程中显示一条信息：



\#if _M_IX86 == 500



\#pragma message( "Pentium processor build" )



\#endif



messagestring 

参数可以是一个能够扩展成字符串文字量的宏，并且你能够用字符串文字量和宏的任何组合来构造。例如，下面的语句显示被编译文件



的文件名和文件最后一次修改的日期和时间。



\#pragma message( "Compiling " __FILE__ )



\#pragma message( "Last modified on " __TIMESTAMP__ )



18 once



\#pragma once



指定在创建过程中该编译指示所在的文件仅仅被编译程序包含（打开）一



次。该编译指示的一种常见用法如下：



//header.h #pragma once



// Your C or C++ code would follow:



19 optimize



仅在专业版和企业版中存在



\#pragma optimize( "[optimization-list]", {on | off} )



代码优化仅有 Visual C++专业版和企业版支持。详见 Visual C++ Edition

。



指定在函数层次执行的优化。 

optimize 编译选项必须在函数外出现，并且在该编译指示出现以后的第一个函数定义开始起作用。 on 和 off 参数打开或关闭在 optimization-list 

指定的选项。



optimization-list 能够是0或更多个在表2.2

中给出的参数：



表 

2.2 optimize 

编译指示的参数参数



优化类型



a



假定没有别名。



g



允许全局优化。



p



增强浮点一致性。s 或 

t



指定更短或者更快的机器代码序列。



w



假定在函数调用中没有别名。



y



在程序堆栈中生成框架指针。



这些和在

/O 编译程序选项中使用的是相同的字母。例如：



\#pragma optimize( "atp", on )



用空字符串（ ""）的 optimize 

编译指示是一种特别形式。它要么关闭所有的优化选项，要么恢复它们到原始（或默认）的设定。



\#pragma optimize( "", off )



. . .



\#pragma optimize( "", on )



20 pack #pragma pack( [ n] )



指定结构和联合成员的紧缩对齐。尽管用/Zp 选项设定整个翻译单元的结构和联合成员的紧缩对齐，可以用 pack 

编译指示在数据说明层次设定紧缩对齐。从出现该编译指示后的第一个结构或者联合说明开始生效。这个编译指



示不影响定义。



当你使用

\#pragma pack(n)，其中 n 是1， 2， 4， 8或者16，第一个以后的每个结构成员保存在较小的成员类型或者 n 字节边界上。如果你使用没有参数的#pragma pack，结构成员将被紧缩到由/Zp 指定的值。默认的/Zp 紧缩的大小是/Zp8

。



编译程序还支持下面的增强语法：



\#pragma pack( [ [ { push | pop}, ] [ identifier, ] ] [ n ] )



该语法允许你将使用不同紧缩编译指示的组件合并到同一个翻译单元内。



每次出现有 

push 参数的 pack 编译指示将保存当前的紧缩对齐值到一个内部的编译程序堆栈。编译指示的参数列表从左向右读取。如果你使用了 push，当前紧缩值被保存。如果你提供了一个 n 值，这个值将成为新的紧缩值。如果你指定了一个你选定的标示符，这个标示符将和新的紧缩值关联。



每次出现有 pop 参数的 pack 编译指示从内部编译程序堆栈顶部取出一个值并将那个值作为新的紧缩对齐。如果你用了 pop，而内部编译程序堆栈是空的，对齐值将从命令行得到，同时给出一个警告。如果你用了 pop 并指定了 n 的值，那个值将成为新的紧缩值。如果你用了 pop 

并指定了一个标示符，将移去所有保存在堆栈中的的值直到匹配的找到匹配的标示符，和该标示符



关联的紧缩值也被从堆栈中移出来成为新的紧缩值。如果没有找到匹配的标示符，将从命令行获取紧缩值并产生一个

1级警告。默认的紧缩对齐是8

。



pack 

编译指示的新的增强功能允许你编写头文件保证在使用头文件之前和其后的紧缩值是一样的：



/* File name: include1.h */



\#pragma pack( push, enter_include1 ) /* Your include-file code ... */ #pragma pack( pop, enter_include1 ) /* End of include1.h */



在前面的例子中，进入头文件时将当前紧缩值和标示符 enter_include1关联并推入，被记住。在头文件尾部的 pack 编译选项移去所有在头文件中可能遇到的紧缩值并移去和 enter_include1

关联的紧缩值。这样头文件保证了在使用头文件之前和其后的紧缩值是一样的。



新功能也允许你在你的代码内用 

pack 

编译指示为不同的代码，例如头文件设定不同的紧缩对齐。



\#pragma pack( push, before_include1 ) #include "include1.h"



\#pragma pack( pop, before_include1 )



在上一个例子中，你的代码受到保护，防止了在 include.h 中的任何紧缩



值的改变。



21 pointers_to_members



C++

特有



| #pragma [most-general-representation] ) | pointers_to_members(pointer-declaration, |
| --------------------------------------- | ---------------------------------------- |
|                                         |                                          |

指定是否能够在相关类定义之前说明一个指向类成员的指针，并且用于控



制 指 针 的 大 小 和 解 释 指 针 的 代 码 。 你 能 够 在 你 的 源 代 码 中 使 用

pointers_to_members 编译知识来代替/vmx 

编译程序选项。



pointer-declaration 参数指出是否在相关函数定义之前或其后你已经说明了一个指向成员的指针。 pointer-declaration 

参数是下面两个符号之一：



参数



说明



full_generality



生成安全的，但是有时不能优化的代码。如果有一些指向成员的指针在相关 类 定 义 之 前 说 明 ， 你 要 用 full_generality 。 这 个 参 数 总 是 使 用 由most-general-representation 

指定的指针表示方式。



best_case



对于所有指向成员的指针用最佳的表示方式生成安全的，优化的代码。需要在说明一个指向类成员指针之前定义类。默认是 best_case

。



most-general-representaion 

参数指出在一个翻译单元中编译程序能够安全引用任何指向类成员指针的最小指针表示方式。这个参数可以是下列之一：



参数



说明



single_inheritance



最普通的表示方式是单继承，指向成员函数。如果用于指向具有多重或者



虚拟继承方式类成员的指针，将产生一个错误。



multi_inheritance



最普通的表示方式是多重继承，指向成员函数。如果用于指向具有虚拟继



承方式类成员的指针，将产生一个错误。



virtual_inheritance



最普通的表示方式是虚拟继承，指向成员函数。不会产生错误。当使用#pragma pointers_to_members (full_generality)

时这是默认的参数。



22 setlocale



\#pragma setlocale( "locale-string" )



定义用于翻译宽字符常数和字符串文字量时用的地区（国家和语言）。 由



于用于从多字节字符转换到宽字符的算法根据地区或者由于在运行可执行程



序不同的地方进行编译而不同，这个编译指示提供一种在编译时指定目标地区的方式。这保证宽字符字符串将以正确的格式保存。默认的 

locale-string 是“ C”。“ C”地区将字符串中的每个字符作为 wchar_t（即 unsigned int

）映射其值。



23 vtordisp



C++

特有



\#pragma vtordisp({on | off} )



允许隐藏的附加 vtordisp 构造函数/析构函数替换成员。 vtordisp 

编译指示仅能够用于具有虚拟基类的代码。如果派生类从一个虚拟基类重载了一个虚



拟函数，并且如果派生类的构造函数或析构函数用指向虚拟基类的指针调用



了 这 个 函 数 ， 编 译 程 序 将 根 据 虚 拟 基 类 在 类 中 引 入 一 个 附 加 的 隐 藏“ 

vtordisp”域。



vtodisp 编译选项影响它后面的类布局。 /vd0和/vd1选项为整个模块指定了相同的行为。指定 off 将禁止隐藏的 vtordisp 成员，指定 on（默认）将在它们需要的时候允许 vtordisp。仅在不可能出现类的构造函数和析构函数通过 

this



指针调用其指向对象中的虚拟函数时才关闭 vtordisp。

\#pragma vtordisp( off )



class GetReal : virtual public { ... }; #pragma vtordisp( on )



24 warning



\#pragma warning( warning-specifier : warning-number-list



[,warning-specifier : warning-number-list...] ) #pragma warning( push[ , n ] ) #pragma warning( pop )



允许有选择地修改编译程序警告信息的行为。warning-specifier 能够是下列值之一： 

warning-specifier



含义



once



只显示指定信息一次。



default



对指定信息应用默认的编译程序选项。



1,2,3,4



对指定信息引用给定的警告等级。



disable



不显示指定信息。



error



对指定信息作为错误显示。



warning-number_list 能够包含任何警告编号。如下，在一个编译指示中可



以指定多个选项：



\#pragma warning( disable : 4507 34; once : 4385; error : 164 )



这等价于：



\#pragma warning( disable : 4507 34 ) // Disable warning messages // 4507 and 34.



\#pragma warning( once : 4385 ) // Issue warning 4385



// only once.



\#pragma warning( error : 164 ) // Report warning 164



// as an error.



对于那些关于代码生成的，大于4699的警告标号， warning 编译指示仅在函数定义外时有效。如果指定的警告编号大于4699并且用于函数内时被忽略。下面例子说明了用 warning 

编译指示禁止、然后恢复有关代码生成警告信息的正确位置：



int a;



\#pragma warning( disable : 4705 ) void func()



{



a; }



\#pragma warning( default : 4705 ) warning 

编译指示也支持下面语法：



\#pragma warning( push [ ,n ] ) #pragma warning( pop )



这里 n 表示警告等级（ 1到4

）。



warning(push)编译指示保存所有警告的当前警告状态。 warning(push,n)保存所有警告的当前状态并将全局警告等级设置为 n

。



warning(pop)弹出最后一次推入堆栈中的警告状态。任何在 push 和 pop 之间改变的警告状态将被取消。考虑下面的例子：



\#pragma warning( push ) #pragma warning( disable : 4705 ) #pragma warning( disable : 4706 ) #pragma warning( disable : 4707 ) // Some code



\#pragma warning( pop )



在这些代码的结束， pop 恢复了所有警告的状态（包括4705， 4706和4707

）到代码开始时候的样子。



当你编写头文件时，你能用 

push 和 pop 来保证任何用户修改的警告状态不会影响正常编译你的头文件。在头文件开始的地方使用 push，在结束地方使用 pop。例如，假定你有一个不能顺利在4级警告下编译的头文件，下面的代码改变警告等级到3

，然后在头文件的结束时恢复到原来的警告等级。



\#pragma warning( push, 3 ) // Declarations/ definitions #pragma warning( pop )