# 【C/C++】详解 | #pragma预处理器参数详解

§ - 1 前言
在阅读本文前，您需要知道：

#define预处理器
条件预处理器
之前在网上浏览的时候，发现全网没几篇文章能系统全面的讲解这个预处理器，我自己琢磨了一下，写了

§ - 2 简介
有一说一，所有预处理其中，#pragma应该最复杂了。

它的作用是设定编译器的状态或者是指示编译器完成一些特定的动作。#pragma指令对每个编译器给出了一个方法，在保持与C和C++语言完全兼容的情况下，给出主机或操作系统专有的特征。依据定义，编译指示是机器或操作系统专有的，且对于每个编译器都是不同的。

听着很绕对吧，我们慢慢来。

§ - 3 基本语法
#pragma的基本语法是：

#pragma PARA(...)
1
其中PARA是命令参数，可以是以下值（仅列出常用命令）：

P	A	R	A
alloc_text	comment	init_seg1	optimize
auto_inline	component	inline_depth	pack
bss_seg	data_seg	inline_recursion	pointers_to_members
check_stack	function	intrinsic	setlocale
code_seg	hdrstop	message	vtordisp
const_seg	include_alias	once	warning
以下，正文。

一、message参数
message可以在不中断编译的情况下发送 字面字符串常量 到 标准输出 。
它通常用于编译时显示一些信息。
语法如下：2

#pragma message(msg·string)
//注：【msg·string】意思即参数msg需要string类型
//forC：string即char*/char[]，字符串
1
2
3
其中，msg即发送的信息。它必须是string（即char[]、char*）
【实例】以下代码将在编译时发送 hello world到标准输出并在运行时输出HELLO WORLD!!!

#include<iostream>
//forC: #include<stdio.h> 
#pragma message("hello world\n")
int main(){
	std::cout<<"HELLO WORLD!!!";
	//forC: printf("HELLO WORLD!!!");
	return 0;
}
1
2
3
4
5
6
7
8
你能够用字符串文字量和宏（但必须指示为字符串形式）的任何组合来构造（中间要有空格）：

#define MAXN 114514
#pragma message("MAXN:"MAXN)//**非法**!!!
1
2
#define MAXN "114514"
#pragma message("MAXN:"MAXN)//**警告**!!!
/*警告：
warning: invalid suffix on literal; C++11 requires a space between literal and string macro
*/
1
2
3
4
5
#define MAXN "114514"
#pragma message("MAXN:" MAXN)//合法
/*输出：
MAXN:114514
*/
//【不会记录二者间的空格】
1
2
3
4
5
6
#pragma message("Hello" " " "World" "!" "\n")
//Hello World!
1
2




二、once参数
once参数在标头文件（*.h / *.c / *.cpp）的开头使用，目的是防止该文件被包含(#include)多次，效果同#ifndef - #define - ... - #endif。





三、hdrstop参数
hdrstop（即HeaDeR STOP），表示仅编译这前的 头文件，后方不再编译。





四、code_seg参数
code_seg，网上各种说法不一，总结一下，

code_seg参数可以设置程序中函数代码存放的代码段

指定函数在.obj文件中存放的节，函数在.obj文件中默认的存放节为.text节，如果code_seg没有带参数的话,则函数存放在.text节中。

它的语法是：3

#pragma code_seg( 												\
	[ 															\
		[ { push | pop}, ]										\
		[ identifier·idt, ] 									\
	]															\
	[ 															\
		segment-name·string 									\
		[, segment-class·string ] 								\
	]															\
) 
1
2
3
4
5
6
7
8
9
10
参数：

p u s h pushpush 或 p o p poppop 【可选】：
push将一个记录放到内部编译器的堆栈中,可选参数可以为一个标识符或者节名；
pop(可选参数)将一个记录从堆栈顶端弹出,该记录可以为一个标识符或者节名。
i d e n t i f i e r identifieridentifier：标识符（不能是C/C++关键字、字面常量、宏）【可选】，当使用push指令时,为压入堆栈的记录指派的一个标识符,当该标识符被删除的时候和其相关的堆栈中的记录将被弹出堆栈。
s e g m e n t segmentsegment - n a m e namename ：字符串【可选】，表示函数存放的节名4。
s e g m e n t segmentsegment - c l a s s classclass ：字符串【可选】，表示函数存放类名。
实例：

//默认情况下,函数被存放在.text节中
void func1 () {                    
	// stored in .text
}

//将函数存放在.my_data1节中
#pragma code_seg(".my_data1")
void func2 () {                    
	// stored in my_data1
}

//r1为标识符,将函数放入.my_data2节中
#pragma code_seg(push, r1, ".my_data2")
void func3 () {                    
	// stored in my_data2
}

/*------------------------------*/

//例如
#pragma  code_seg(“PAGE”)
//作用是将此部分代码放入分页内存中运行。

#pragma  code_seg()
//将代码段设置为默认的代码段

#pragma  code_seg("INIT")
//加载到INIT内存区域中，成功加载后，可以退出内存
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
关于分页内存，请参考分页内存与非分页内存。





五、warning参数
语法：

#pragma warning(														\
	warning-specifier·kyw : warning-order[list]·int						\
	[																	\
		;																\
		warning-specifier·kyw : warning-order[list]·int					\
		[...]															\
	]																	\
)
1
2
3
4
5
6
7
8
#pragma warning( push [, warning-lv] )
1
#pragma warning( pop )
1
参数：

w a r n i n g warningwarning - s p e c i f i e r specifierspecifier · 标识符，可以为：
o n c e onceonce —— 只显示指定信息一次。
d e f a u l t defaultdefault —— 对指定信息应用默认的编译程序选项。
1 | 2 | 3 | 4 —— 对指定信息引用给定的警告等级。
d i s a b l e disabledisable —— 不显示指定信息。
e r r o r errorerror —— 将指定信息作为错误提出。
w a r n i n g warningwarning - o r d e r orderorder · int，可以是任意警告编号。
你可以同时对多组编号操作，如下：

#pragma warning( 		\
	disable : 4507 34; 	\
	once : 4385; 		\
	error : 164 		\
)
/* 请注意，行末的【\】是必须的，
 * 如果没有它则意味着#pragma的结束，编译器无法理解后面的参数
 */
 //因此，除非必须，请尽量将它们写在一行
 1
 2
 3
 4
 5
 6
 7
 8
 9
 这相当于：

#pragma warning(disable : 4507 34)
#pragma warning(once : 4385)
#pragma warning(error : 164)
1
2
3
对于那些关于代码生成的，大于4699的警告标号，warning编译指示仅在函数定义外时有效。如果指定的警告编号大于4699并且用于函数内时被忽略。

【实例】- 使用warning参数禁止，再解除警告。

int a;
#pragma warning( disable : 4705 )
void func(){
	cout<<"HELLO WORLD!!!";
	//forC: printf("HELLO WORLD!!!");
    a;
}
#pragma warning( default : 4705 )

int main(){
	cout<<"hello world!\n";
	//forC: printf("hello world!\n");
	func();
	return 0;
}
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
输出：

hello world!
HELLO WORLD!!!
1
2
可以看出，对于被禁止的警告，编译器将跳过警告语句。


关于warning的另外两种方式：

#pragma warning( push [, warning-lv·int【1,4】] )
1
#pragma warning( pop )
1
其中，w a r n i n g warningwarning - l v lvlv 是警告等级，为1 | 2 | 3 | 4 。
warning(push)表示保存当前所有警告方法，warning(push,lv)表示保存警告状态并将 全局警告等级 设置为lv。
warning(pop)将上一个设置的警告状态取消（可以看做是一个堆栈，将顶元素弹出），任何在push和pop间设定的状态状态将被取消，因此，在设定状态后使用warning(push)是明智的选择。
考虑以下代码：

#pragma warning( push )
#pragma warning(disable : 4507 34)
#pragma warning(once : 4385)
#pragma warning(error : 164)
/* Some Code */
#pragma warning( pop )
1
2
3
4
5
6
运行完后，所有的警告状态都将被取消而非仅取消error:164。
以下：

#pragma warning( push )
#pragma warning(disable : 4507 34)
#pragma warning(once : 4385)
#pragma warning(error : 164)

//***
#pragma warning( push )
//***

/* Some Code */
#pragma warning( pop )
1
2
3
4
5
6
7
8
9
10
11
此时仅error:164会被取消。


当你编写头文件时，你能用push和pop来保证任何用户修改的警告状态不会影响正常编译你的头文件。在头文件开始的地方使用push，在结束地方使用pop。例如，假定你有一个不能顺利在4级警告下编译的头文件，下面的代码改变警告等级到3，然后在头文件的结束时恢复到原来的警告等级。

#pragma warning( push , 3 )
//Body of hdr
#pragma warning( pop )
1
2
3




六、pack参数
参考：#pragma pack

该参数指定结构和联合成员的紧缩对齐。尽管用/Zp选项设定整个翻译单元的结构和联合成员的紧缩对齐，可以用pack编译指示在数据说明层次设定紧缩对齐。从出现该编译指示后的第一个结构或者联合说明开始生效。这个编译指示不影响定义。

当你使用#pragma pack(n)，其中n是1，2，4，8或者16，第一个以后的每个结构成员保存在较小的成员类型或者n字节边界上。如果你使用没有参数的#pragma pack，结构成员将被紧缩到由/Zp指定的值。默认的/Zp紧缩的大小是/Zp8。

语法：

//一般使用
#pragma pack( [ n ] )
1
2
//完整
#pragma pack( [ [ { push | pop } ] [ , identifier ] ] [ n ] )
1
2
该语法允许你将使用不同紧缩编译指示的组件合并到同一个翻译单元内。

每次出现有push参数的pack编译指示将保存当前的紧缩对齐值到一个内部的编译程序堆栈。编译指示的参数列表从左向右读取。如果你使用了push，当前紧缩值被保存。如果你提供了一个n值，这个值将成为新的紧缩值。如果你指定了一个你选定的标示符，这个标示符将和新的紧缩值关联。

每次出现有pop参数的pack编译指示从内部编译程序堆栈顶部取出一个值并将那个值作为新的紧缩对齐。如果你用了pop，而内部编译程序堆栈是空的，对齐值将从命令行得到，同时给出一个警告。如果你用了pop并指定了n的值，那个值将成为新的紧缩值。如果你用了pop并指定了一个标示符，将移去所有保存在堆栈中的的值直到匹配的找到匹配的标示符，和该标示符关联的紧缩值也被从堆栈中移出来成为新的紧缩值。如果没有找到匹配的标示符，将从命令行获取紧缩值并产生一个1级警告。默认的紧缩对齐是8。

使用后者的语法允许你编写头文件保证在使用头文件之前和其后的紧缩值是一样的：

//文件：hdr.h
#pragma pack(push,enter_hdr)
//BODY
#pragma pack(pop,enter_hdr)
//结束
1
2
3
4
5
上述代码使得进入头文件时将当前紧缩值和标示符enter_hdr关联并推入，被记住。在头文件尾部的pack编译选项移去所有在头文件中可能遇到的紧缩值并移去和enter_hdr关联的紧缩值。这样头文件保证了在使用头文件之前和其后的紧缩值是一样的。

类似地，你可以在包含不同头文件时使用不同紧缩值（如果文件内部没有定义）：

#pragma pack(push,ent_hdr_)
#include"hdr.h"
#pragma pack(pop,ent_hdr_)
1
2
3
上述代码保护了hdr.h中的紧缩值。




八、intrinsic参数
语法：

#pragma intrinsic( [ func1 [ , func2 [...] ] ] )
1
该预处理将参数列表中的函数名（无需带【()】）声明为内含函数（内联），见内含函数

intrinsic编译提示指定对在编译指示参数表中函数调用是内含的。 编译程序像嵌入代码一样生成内含函数，而不是函数调用。 下面列出了 具有内含形式的库函数。 一旦遇到 intrinsic 编译指示， 它从第一个包含指定内含函数的函数定义开始起作用。 作用持续到源文件尾部或者出现包含相同内含函数的 function 编译指示。

以下函数具有内含形式：

_disable	_enable	_inp	_inpw	_lrotl	_lrotr
_outp	_outpw	_rotl	_rotr	_strset	abs
fabs	labs	memcmp	memcpy	memset	strcat
strcmp	strcpy	strlen	-	-	-
请注意，下列浮点函数没有内含形式：（ 然而它们具有直接将参数通过浮点芯片传送而不是推入程序堆栈的版本。）

acos	asin	cosh	fmod	pow	sinh	tanh
当你同时指定/Oi 和/Og 编译程序选项（或者任何包含/Og， /Ox， /O1 和/O2 的选项） 时下列浮点函数具有真正的内含形式：

atan	exp	log10	sqrt	atan2	log	sin	tan	cos
你可以用编译程序选项/Op 或/Za 来覆盖真内含浮点选项的生成。 在这种情况下， 函数会像一般库函数一样被生成， 同时直接将参数通过浮点芯片传送而不是推入程序堆栈。





七、function参数
语法：

#pragma function( func1 [ , func2 [...] ] )
1
内含函数的执行是将函数代码嵌入调用处（并非直接调用），使用function显示地强制调用该函数。其持续作用到源文件的尾部或者出现对同一个内含函数指定intrinsic编译指示。function编译指示只能用于函数外——在全局层次。





八、init_seg参数
【注：此预编译提示仅C++特有】
语法：

#pragma init_seg(										\
	{ 													\
		compiler 		| 								\
		lib 			| 								\
		user			| 								\
		section-name·string [, func-name·string]		\
	} 													\
)
1
2
3
4
5
6
7
8
该编译提示是指定影响启动代码执行的关键字或代码段。

因为全局静态对象的初始化可以包含执行代码，所以你必须指定一个关键字来定义什么时候构造对象。在使用需要初始化的动态连接库（DLL）或程序库时使用init_seg编译指示是尤其重要的。

参数选项：

c o m p i l e r compilercompiler —— 由Microsoft C运行时间库保留。在这个组中的对象将第一个构造。
l i b liblib —— 用于第三方类库开发者的初始化。在这个组中的对象将在标记为构造compiler的对象之后，其它对象之前构造。
u s e r useruser —— 用于任何其它用户。在这个组中的对象将最后构造。
s e l e c t i o n selectionselection - n a m e namename · 字符串 —— 允许显式地指定初始化段。在用户指定的section-name中的对象将不会隐式地构造，而它们的地址将会被放置在由section-name命名的段中。
f u n c funcfunc - n a m e namename · 字符串 【可选】—— 指定当程序退出时，作为atexit函数调用的函数。这个函数必须具有和atexit函数相同的形式：（如果你需要延迟初始化，你能够选择指定显式的段名。随后你必须调用每个静态对象的构造函数。）
int func_name(void (__cdecl *)(void));
1




九、inline_depth参数
语法：

#pragma inline_depth( dep·int【0,255】 )
//所需参数为[0,255]的整数
1
2
该编译提示通过控制能够被扩展的一系列函数调用（从0到255次）来控制嵌入函数扩展的发生次数，这个编译指示控制用inline，__inline标记的或在/Ob2选项下能自动嵌入的嵌入函数。

inline_depth编译指示控制能够被扩展的一系列函数调用。例如，如果嵌入深度是4，并且如果A调用B然后调用C，所有的3次调用都将做嵌入扩展。然而，如果设置的最近一次嵌入深度是2，则只有A和B被扩展，而C仍然作为函数调用。

为了使用这个编译指示，你必须设置编译程序选项/Ob为1或者2。用这个编译指示指定的深度设定在该指示后面的第一个函数开始生效。如果你在括号内不指定一个值，inline_depth设置嵌入深度到默认值8。

在扩展时，嵌入深度可以被减少而不能被增加。如果嵌入深度是6，同时在扩展过程中预处理程序遇到一个inline_depth编译指示设置为8，则深度保持为6。

嵌入深度0将拒绝嵌入扩展，深度255将设置在嵌入扩展时没有限制。如果用一个没有指定值的编译指示，则使用为默认值。





十、auto_inline参数
语法：

#pragma auto_inline( { on | off } )
1
当指定off时将任何一个可以被考虑为作为自动嵌入扩展候选的函数排除出该范围。为了使用auto_inline编译指示，将其紧接着写在一个函数定义之前或之后（不是在其内部）。该编译指示将在其出现以后的第一个函数定义开始起作用。auto_inline编译指示对显式的inline函数不起作用。




十一、check_stack
语法：

#pragma check_stack( [ { on | off } ] )
1
#pragma check_stack( [ { + | - } ] )
1
当指定为off|-时编译程序关闭堆栈探测，若是on|+则打开堆栈探测，对于未给出参数的，以默认设置决定。
如果你没有给出check­_stack编译指示的参数，堆栈检查将恢复到在命令行指定的行为：

使用/Gs编译？	结果(前提是未给出参数，否则按参数)
是	后续的函数关闭堆栈检查
否	后续的函数开启堆栈检查



十二、const_seg参数
语法：

#pragma const_seg( 												\
	[ 															\
		[ { push | pop}, ]										\
		[ identifier·idt, ] 									\
	]															\
	[ 															\
		segment-name·string 									\
		[, segment-class·string ] 								\
	]															\
) 
1
2
3
4
5
6
7
8
9
10
类似于code_seg，它指定用于常量数据的默认段。

用const_seg编译指示分配的数据不包含任何关于其位置的信息。

第二个参数segment-class是用于兼容2.0版本以前的Visual C++的，现在将忽略它。





十三、bss_seg参数
语法：

#pragma bss_seg( 												\
	[ 															\
		[ { push | pop}, ]										\
		[ identifier·idt, ] 									\
	]															\
	[ 															\
		segment-name·string 									\
		[, segment-class·string ] 								\
	]															\
) 
1
2
3
4
5
6
7
8
9
10
它为未初始化数据指定缺省段。在一些情况下，你能使用bss_seg将所有未初始化数据安排在一个段中来加速你的装载时间。

用bss_seg编译指示分配的数据不包含任何关于其位置的信息。

第二个参数segment-class是用于兼容2.0版本以前的Visual C++的，现在将忽略它。





十四、alloc_text参数
语法：

#pragma alloc_text( txtforfunc·string , func1 [ , func2... ] )
1
命名特别定义的函数驻留的代码段。该编译指示必须出现在函数说明符和函数定义之间。（即在函数说明后，定义前）
注意：alloc_text不接受C++中方法（类成员函数）及重载函数的处理，仅能以C的方式说明（extern "C"）。如果你试图将这个编译指示应用于一个具有C++连接方式的函数时，将出现一个编译程序错误。
另外，该编译提示不能用在函数内部

结语
精力有限，只能先写到这了，下期见！


@HaohaoCppDebuger |寻兰 
2021/12/2 

-----THE END-----
THANK YOU !




加粗 命令为仅C++，下同。 ↩︎

代码段中以//forC: ...注释的语句为C语言写法。下同。 ↩︎

代码中 [ ] 括住的部分为可选。下同。
{ XX | YY } 表示XX或YY任选。下同。
语法代码段的换行实际不合法，注意行末【\】。下同。 ↩︎

该指令用来指定函数在.obj文件中存放的节,观察OBJ文件可以使用VC自带的dumpbin命令行程序,函数在.obj文件中默认的存放节为.text节，如果code_seg没有带参数的话,则函数存放在.text节中。

↩︎



# [#pragma预处理命令](https://www.cnblogs.com/qinfengxiaoyue/archive/2012/06/05/2535524.html)

\#pragma可以说是C++中最复杂的预处理指令了，下面是最常用的几个#pragma指令：

**#pragma comment(lib,"XXX.lib")**

表示链接XXX.lib这个库，和在工程设置里写上XXX.lib的效果一样。

**#pragma comment(linker,"/ENTRY:main_function")**

表示指定链接器选项/ENTRY:main_function

**#pragma once**

表示这个文件只被包含一次

**#pragma warning(disable:4705)**

表示屏蔽警告4705

 

   C和C++程序的每次执行都支持其所在的主机或操作系统所具有的一些独特的特点。例如，有些程序需要精确控制数据存放的内存区域或控制某个函

数接收的参数。#pragma为编译器提供了一种在不同机器和操作系统上编译以保持C和C++完全兼容的方法。#pragma是由机器和相关的操作系统定义

的，通常对每个编译器来说是不同的。

​    如果编译器遇到不认识的pragma指令，将给出警告信息，然后继续编译。Microsoft C and C++ 的编译器可识别以下指令：alloc_text，

auto_inline，bss_seg，check_stack，code_seg，comment，component，conform，const_seg，data_seg，deprecated，

fenv_access，float_control，fp_contract，function，hdrstop，include_alias，init_seg，inline_depth，inline_recursion，intrinsic，

make_public，managed，message，omp，once，optimize，pack，pointers_to_members，pop_macro，push_macro，region,

endregion，runtime_checks，section，setlocale，strict_gs_check，unmanaged，vtordisp，warning。其中conform，init_seg，

pointers_to_members，vtordisp仅被C++编译器支持。

 

​    以下是常用的pragma指令的详细解释。

**1.#pragma once**。保证所在文件只会被包含一次，它是基于磁盘文件的，而#ifndef则是基于宏的。

 

**2.#pragma warning**。允许有选择性的修改编译器的警告消息的行为。有如下用法：

> \#pragma warning(disable:4507 34; once:4385; error:164) 等价于：  
>
> \#pragma warning(disable:4507 34) // 不显示4507和34号警告信息  
>
> \#pragma warning(once:4385)    // 4385号警告信息仅报告一次  
>
> \#pragma warning(error:164)    // 把164号警告信息作为一个错误
>
> \#pragma warning(default:176)   // 重置编译器的176号警告行为到默认状态 
>
> 同时这个pragma warning也支持如下格式,其中n代表一个警告等级(1---4)：       
>
> \#pragma warning(push)  // 保存所有警告信息的现有的警告状态 
>
> \#pragma warning(push,n) // 保存所有警告信息的现有的警告状态，并设置全局报警级别为n  
>
> \#pragma warning(pop)  //
>
> 例如： 
>
> \#pragma warning(push)  
>
> \#pragma warning(disable:4705)
>
> \#pragma warning(disable:4706)
>
> \#pragma warning(disable:4707)
>
> \#pragma warning(pop)     
>
> 在这段代码后，恢复所有的警告信息(包括4705，4706和4707)。

**3.#pragma hdrstop**。表示预编译头文件到此为止，后面的头文件不进行预编译。BCB可以预编译头文件以 加快链接的速度，但如果所有头文件都进

行预编译又可能占太多磁盘空间，所以使用这个选项排除一些头文 件。

 

**4.#pragma message**。在标准输出设备中输出指定文本信息而不结束程序运行。用法如下：

\#pragma message("消息文本")。当编译器遇到这条指令时就在编译输出窗口中将“消息文本”打印出来。

 

**5.#pragma data_seg**。一般用于DLL中，它能够设置程序中的初始化变量在obj文件中所在的数据段。如果未指定参数，初始化变量将放置在默认数

据段.data中，有如下用法：

```
  1: #pragma data_seg("Shared")   // 定义了数据段"Shared"，其中有两个变量a和b
  2: int a = 0;                   // 存储在数据段"Shared"中
  3: int b;                       // 存储在数据段".bss"中，因为没有初始化
  4: #pragma data_seg()           // 表示数据段"Shared"结束，该行代码为可选的
```



对变量进行专门的初始化是很重要的，否则编译器将把它们放在普通的未初始化数据段中而不是放在shared中。如上述的变量b其实是放在了未初始化数

据段.bss中。

```
  1: #pragma data_seg("Shared")
  2: int j = 0;                      // 存储在数据段"Shared"中
  3: #pragma data_seg(push, stack1, "Shared2") //定义数据段Shared2，并将该记录赋予别名stack1，然后放入内部编译器栈中
  4: int l = 0;                      // 存储在数据段"Shared2"中
  5: #pragma data_seg(pop, stack1)   // 从内部编译器栈中弹出记录，直到弹出stack1，如果没有stack1，则不做任何操作
  6: int m = 0;                      // 存储在数据段"Shared"中，如果没有上述pop段，则该变量将储在数据段"Shared2"中
```

 

**6.#pragma code_seg**。它能够设置程序中的函数在obj文件中所在的代码段。如果未指定参数，函数将放置在默认代码段.text中，有如下用法：

```
  1: void func1() {                  // 默认存储在代码段.text中
  2: }
  3: 
  4: #pragma code_seg(".my_data1")
  5: 
  6: void func2() {                  // 存储在代码段.my_data1中
  7: }
  8: 
  9: #pragma code_seg(push, r1, ".my_data2")
 10: 
 11: void func3() {                  // 存储在代码段.my_data2中
 12: }
 13: 
 14: #pragma code_seg(pop, r1)
 15: 
 16: void func4() {                  // 存储在代码段.my_data1中
 17: }
```



**7.#pragma pack**。用来改变编译器的字节对齐方式。常规用法为：

\#pragma pack(n)  //将编译器的字节对齐方式设为n，n的取值一般为1、2、4、8、16，一般默认为8

\#pragma pack(show) //以警告信息的方式将当前的字节对齐方式输出

\#pragma pack(push) //将当前的字节对齐方式放入到内部编译器栈中

\#pragma pack(push,4) //将字节对齐方式4放入到内部编译器栈中，并将当前的内存对齐方式设置为4

\#pragma pack(pop) //将内部编译器栈顶的记录弹出，并将其作为当前的内存对齐方式

\#pragma pack(pop,4) //将内部编译器栈顶的记录弹出，并将4作为当前的内存对齐方式

\#pragma pack(pop,r1) //r1为自定义的标识符，将内部编译器中的记录弹出，直到弹出r1，并将r1的值作为当前的内存对齐方式；如果r1不存在，当

不做任何操作

一个例子：

以如下结构为例: struct {
          char a;
          WORD b;
          DWORD c;
          char d;
         }
在Windows默认结构大小: sizeof(struct) = 4+4+4+4=16;
与#pragma pack(4)一样
若设为 #pragma pack(1), 则结构大小: sizeof(struct) = 1+2+4+1=8;
若设为 #pragma pack(2), 则结构大小: sizeof(struct) = 2+2+4+2=10;
在#pragma pack(1)时:空间是节省了,但访问速度降低了;
有什么用处???
在系统通讯中,如和硬件设备通信,和其他的操作系统进行通信时等,必须保证双方的一致性。

 

**8.#pragma comment**。将一个注释记录放置到对象文件或可执行文件中。

其格式为：#pragma comment( comment-type [,"commentstring"] )。其中，comment-type是一个预定义的标识符，指定注释的类型，应该是**compiler，exestr，lib，linker，user**之一。

**compiler**：放置编译器的版本或者名字到一个对象文件，该选项是被linker忽略的。

**exestr**：在以后的版本将被取消。

**lib**：放置一个库搜索记录到对象文件中，这个类型应该与commentstring（指定Linker要搜索的lib的名称和路径）所指定的库类型一致。在对象文件中，库的名字跟在默认搜索记录后面；linker搜索这个这个库就像你在命令行输入这个命令一样。你可以在一个源文件中设置多个库搜索记录，它们在obj

文件中出现的顺序与在源文件中出现的顺序一样。

如果默认库和附加库的次序是需要区别的，使用/Zl编译开关可防止默认库放到object模块中。

**linker**：指定一个连接选项，这样就不用在命令行输入或者在开发环境中设置了。只有下面的linker选项能被传给Linker：

1. /DEFAULTLIB
2. /EXPORT
3. /INCLUDE
4. /MANIFESTDEPENDENCY
5. /MERGE
6. /SECTION

(1)/DEFAULTLIB:library

/DEFAULTLIB选项将一个library添加到LINK在解析引用时搜索的库列表。用/DEFAULTLIB指定的库在命令行上指定的库之后和obj文件中指定的默认

库之前被搜索。

忽略所有默认库(/NODEFAULTLIB)选项重写/DEFAULTLIB:library。如果在两者中指定了相同的library名称，忽略库(/NODEFAULTLIB:library)选项

将重写/DEFAULTLIB:library。

 

(2)/EXPORT:entryname[,@ordinal[,NONAME]][,DATA]

使用该选项，可以从程序导出函数以便其他程序可以调用该函数，也可以导出数据。通常在DLL中定义导出。

entryname是调用程序要使用的函数或数据项的名称。ordinal为导出表的索引，取值范围在1至65535；如果没有指定ordinal，则LINK将分配一个。

NONAME关键字只将函数导出为序号，没有entryname。DATA 关键字指定导出项为数据项。客户程序中的数据项必须用extern __declspec

(dllimport)来声明。

有三种导出定义的方法，按照建议的使用顺序依次为：

1. 源代码中的__declspec(dllexport)
2. .def文件中的EXPORTS语句
3. LINK命令中的/EXPORT规范

所有这三种方法可以用在同一个程序中。LINK在生成包含导出的程序时还要创建导入库，除非在生成过程中使用了.exp 文件。

LINK使用标识符的修饰形式。编译器在创建obj文件时修饰标识符。如果entryname以其未修饰的形式指定给链接器（与其在源代码中一样），则LINK

将试图匹配该名称。如果无法找到唯一的匹配名称，则LINK发出错误信息。当需要将标识符指定给链接器时，请使用Dumpbin工具获取该标识符的修饰

名形式。

 

(3)/INCLUDE:symbol

/INCLUDE选项通知链接器将指定的符号添加到符号表。若要指定多个符号，请在符号名称之间键入逗号(,)、分号(;)或空格。在命令行上，对每个符号需指定一次/INCLUDE:symbol。

链接器通过将包含符号定义的对象添加到程序来解析symbol。该功能对于添加不会链接到程序的库对象非常有用。

用该选项所指定的符号将覆盖通过/OPT:REF对该符号进行的移除操作。

 

(4)/MANIFESTDEPENDENCY:manifest_dependency

/MANIFESTDEPENDENCY允许你指定位于manifest文件的<dependency>段的属性。/MANIFESTDEPENDENCY信息可以通过下面两种方式传递给LINK:

直接在命令行运行/MANIFESTDEPENDENCY

通过#pragma comment

 

(5)/MERGE:from=to

/MERGE选项将第一个段(from)与第二个段(to)进行联合，并将联合后的段命名为to的名称。

如果第二个段不存在，LINK将段(from)重命名为to的名称。

/MERGE选项对于创建VxDs和重写编译器生成的段名非常有用。

 

(6)/SECTION:name,[[!]{DEKPRSW}][,ALIGN=#]

/SECTION选项用来改变段的属性，当指定段所在的obj文件编译的时候重写段的属性集。

可移植的可执行文件(PE)中的段(section)与新可执行文件(NE)中的节区(segment)或资源大致相同。

段(section)中包含代码或数据。与节区(segment)不同的是，段(section)是没有大小限制的连续内存块。有些段中的代码或数据是你的程序直接定义和

使用的，而有些数据段是链接器和库管理器(lib.exe)创建的，并且包含了对操作系统来说很重要的信息。

/SECTION选项中的name是大小写敏感的。

不要使用以下名称，因为它们与标准名称会冲突，例如，.sdata是RISC平台使用的。

.arch

.bss

.data

.edata

.idata

.pdata

.rdata

.reloc

.rsrc

.sbss

.sdata

.srdata

.text

.xdata

为段指定一个或多个属性。属性不是大小写敏感的。对于一个段，你必须将希望它具有的属性都进行指定；如果某个属性未指定，则认为是不具备这个属

性。如果你未指定R，W或E，则已存在的读，写或可执行状态将不发生改变。

要对某个属性取否定意义，只需要在属性前加感叹号(!)。

E：可执行的

R：可读取的

W：可写的

S：对于载入该段的镜像的所有进程是共享的

D：可废弃的

K：不可缓存的

P：不可分页的

注意K和P是表示否定含义的。

PE文件中的段如果没有E，R或W属性集，则该段是无效的。

ALIGN=#选项让你为一个具体的段指定对齐值。

user：放置一个常规注释到一个对象文件中，该选项是被linker忽略的。

 

**9.#pragma section**。创建一个段。

其格式为：#pragma section( "section-name" [, attributes] )

section-name是必选项，用于指定段的名字。该名字不能与标准段的名字想冲突。可用/SECTION查看标准段的名称列表。

attributes是可选项，用于指定段的属性。可用属性如下，多个属性间用逗号(,)隔开：

read：可读取的

write：可写的

execute：可执行的

shared：对于载入该段的镜像的所有进程是共享的

nopage：不可分页的，主要用于Win32的设备驱动程序中

nocache：不可缓存的，主要用于Win32的设备驱动程序中

discard：可废弃的，主要用于Win32的设备驱动程序中

remove：非内存常驻的，仅用于虚拟设备驱动(VxD)中

如果未指定属性，默认属性为read和write。

在创建了段之后，还要使用__declspec(allocate)将代码或数据放入段中。

例如：

//pragma_section.cpp

\#pragma section("mysec",read,write)

int j = 0;

__declspec(allocate("mysec"))

int i = 0;

int main(){}

该例中, 创建了段"mysec"，设置了read,write属性。但是j没有放入到该段中，而是放入了默认的数据段中，因为它没有使用__declspec(allocate)进

行声明；而i放入了该段中，因为使用__declspec(allocate)进行了声明。

 

**10.#pragma push_macro与#pragma pop_macro**。前者将指定的宏压入栈中，相当于暂时存储，以备以后使用；后者将栈顶的宏出栈，弹出的宏将覆盖当前名称相同的宏。例如：

```
  1: #include <stdio.h>
  2: #define X 1
  3: #define Y 2
  4: 
  5: int main() {
  6: printf("%d",X);
  7:    printf("\n%d",Y);
  8:    #define Y 3   // C4005
  9:    #pragma push_macro("Y")
 10:    #pragma push_macro("X")
 11: printf("\n%d",X);
 12:    #define X 2   // C4005
 13: printf("\n%d",X);
 14:    #pragma pop_macro("X")
 15:    printf("\n%d",X);
 16:    #pragma pop_macro("Y")
 17:    printf("\n%d",Y);
 18: }
```



输出结果：

1

2

1

2

1

3

