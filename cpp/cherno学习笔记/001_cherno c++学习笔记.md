# cherno c++学习笔记



1.
预处理就是将头文件里面的全部内容拷贝到当前文件中。

2.预处理里面的#define A b 就是把A改成b显示在编译后。

3.函数可以在一个cpp文件定义，在另一个文件声明后即可使用。当要使用头文件 .h里的函数时，则要求预处理加上所指向的头文件。

4.运行包含编译和链接，当单文件可以编译的时候不一定代表就可以运行。

5.当我们运行报错时error后会跟随两种错误，一种是 C开头的编译时语法报错，另一种是link链接时报错。

6.运行后的exe文件必须有个入口，基本上为main

7.

如果log函数定义在另一个文件中，但将另一个文件函数名字改错为logr时，函数multiply也会报错。这时把函数里的 log("multiply"); 引掉，没有问题，但是如果将 std::cout << multiply(5,8) <<std::endl; 引掉则链接会报错，因为技术上说你可以能会在另一个文件中使用它，因此可以在 int multiply(int a, int b) 定义时在最前面加上 static 表示只在这个文件使用，变为 static int multiply(int a, int b)。（应该叫静态变量函数）。

int multiply(int a, int b)
{
	log("multiply");
	return a*b;
}

int main(void)
{
	std::cout << multiply(5,8) <<std::endl;
	std::cin.get();
	
}
8.

函数在调用时必须找到一模一样的内容格式，不可以更改，链接时会出错，必须要完全识别才可以。

完全相同的函数可以在一个或者多个文件重复声明，但不可以重复定义。

9.

要记住在头文件写类似 #include “s.h" 时，如果已经在多个.cpp文件预处理则会出现函数已经定义的问题。

1）一种方法是在 .h 文件定义时在最前面加上 static 表示其他文件对着这个函数 include时只对那个文件内部有效。每个文件会有自己版本的该函数，对其他文件编译好的 obj文件不可见。

2）第二种是将 .h文件定义时最前面加上 inline变成内联函数，直接把函数的身体拿来取代调用。就是 .cpp文件调用函数时直接把 例如 log(1111) 形式变成 1111（假如log函数为

void log(const char* message)
{
	std::cout <<message <<std::endl;
}
这样的话）

3）第三种就是作者常用的把.h文件中只留下函数的声明，而把定义部分放到两个.cpp文件中的一个文件。函数可以声明多次。

10.数据类型内存大小
在C++中不同类型变量之间唯一的区别就是大小，表现为变量占据了多少内存。

int 为4字节，可存储大概   -21亿  ~   21亿 ，

char  为 1字节；char*（即指针变量）:为4个字节 ；short 为2字节；long一般为4字节。long long 一般为8字节，

float 一般为4字节，double 为8字节。bool类型为1字节。

定义float 要在数据后加上 f(or F),不然就算 定义 也会默认变成double (隐式转换）。

11.数据类型会支持你输出的数据为什么类型，例如定义 char a = 65,cout <<a就是A;而如果定义为int 则输出65.

12.头文件#include内容时就是把该文件内容完整复制到该位置。

13.寻址是只能寻址到1 byte 而不能寻址到1bit.

14.

有返回值的函数可以直接定义一个变量用来存储返回值。

15.

函数封装重复的事件（代码）可以简化代码。 但是使用太过分了不行，函数调用栈，参数存放在栈上，函数主要目的是为了防止代码重复。

16.

只有main函数可以不用指定返回值，默认返回0.

17.

vs文件夹随便在哪建文件都可以，只是方便管理而不是实际文件夹，例如可以在程序文件夹建立头文件，但一般还是对应点比较好。

18.

把 .h文件都放函数声明，然后在一个.cpp存放函数定义，在其他包含 .main() 的.cpp 直接使用，当然都要有.h的头文件。

19.

#pragma once是最常用也是最新的，也是基本上所有的编辑器都支持它。

#开头的为预处理命令（指令）。#pragma once意思是只#include这个文件一次，他是头文件保护符，防止我们在一个.cpp多次include一个.h，但不是防止在多个.cpp    include。 为什么会出现一个.cpp多次#include呢，因为会出现一个.h 叫player，可能include <log.h>,然后player 又被include到其他文件。比如两个.h,其中一个include另一个.h，但是.cpp同时include两个.h。

20.
除了#pragma once ,还可以使用下面的方式，但是此方法不常用（后面又觉得其实接下来的这种方法使用更普遍)。它的原理是首先会检查_log_h这个符号是否存在，不存在的话就将两行中间的代码都Include到编译里，如果被定义了则两行中间的都会被禁用。也就是第一次运行时已经定义了 _LOG_H,则后面不会再重复了。

#ifndef _LOG_H
#define _LOG_H


......

 


#endif
#ifndef x                 //先测试x是否被宏定义过
#define x
   程序段1   //如果x没有被宏定义过，定义x，并编译程序段 1
#endif   
　　程序段2　　 //如果x已经定义过了则编译程序段2的语句，“忽视”程序段 1

#define  宏定义

在C或C++语言源程序中允许用一个标识符来表示一个字符串，称为“宏”。“define”为宏定义命令。

被定义为“宏”的标识符称为“宏名”。在编译预处理时，对程序中所有出现的“宏名”，都用宏定义中的字符串去代换，这称为“宏代换”或“宏展开”。

21.

#include中的 " " (引号)表示文件存在于该文件的相对位置，但对于<>(方括号）来说，永远没有说这个文件的相对位置，他们必须存在于所有include目录的某一个里面。

现在<>(方括号）只用于编译器的include路径，而" "则用于所有，即使把方括号改为引号也可以正常运行。

" "会先查本地工程文件目录，查找不到后再去标准库中查找，

<>  则是直接查找标准库。

22.
内存就是一切，debug设置断点的时候一定要放在实际执行的代码上（前面的灰色区域）。要确保是在debug的模式下，而不是release(这将会导致程序被重新安排）。当断点运行时，在断点处会显示黄色箭头表示运行到此处，指示当前指令所在的位置，此时按下continue按钮将会和平常一样继续执行程序。

1.F11时，没有开始执行代码，只设置函数堆栈框架，此时鼠标放在message 上会显示该变量被设置为 “hello　world”。



 ２.再按下F12时，从main.cpp跳转到log.cpp,此时黄色箭头在代码前，意味着还没有执行该行代码。



此时再按下F10时，打印出"hello world".

 

 再按下F10则会回到main.cpp，箭头还在原位置，再按下则会到达下一个位置.



此时箭头虽然指向该行，但实际还并未执行这一行，即实际创建并设置变量的这行，当前调试器显示的是内存，变量目前未设置任何值，是一个未初始化的内存，但也只是显示给我们的，并不是内存实际的值。



 当前版本是繁体，，最下面的autos和locals会向你展示局部变量或对自己重要的变量。最后一个watch会让我们监控变量。要查看使用的話只需要点击输入需要的再确认即可。



 当程序逐步完善时，这些值将更新到内存实际存在的有内容。这时需要内存视图。在调试->windows->内存>内存1.



 中间显示数据实际值，是以十六进制表示，右边是对这些数字的ASCIL解释。当我们想要知道a的实际存储在程序内存的位置时，要知道它的内存地址，可在上面输入   &a     代表地址。为什么未初始化的值为cc，意味着它是一个未初始化的堆栈内存。

 

 这时a的值开始变化，可以设置是否为十六位显示。这四个字节（byte）的内存被设置为8，这里两个数字等于一字节（byte）。这也是为什么用十六进制数字表示，每两个十六进制数字总是对齐为一个字节内存。



 运行到string这行时，它是实际内存，能看到字符串的内存地址，就是结尾为90的数字，将地址复制到watch窗口显示，会看到如下图显示内容，前面显示数字，后面则为ascil解释，为“hello"。

 运行到如下时，在watch输入 ”&c” 可以看到 c 的值以及后面它对应的ascil表示。

 如果一直按F10，将会一直在for循环内进行，直到结束循环后跳出。

如果我不打算直接按跳出，而是要运行完for循环再到下面，则可以再log函数前设置个断点，再点击继续。程序将运行直到下一个断点。此时将运行到显示如下内容。

一般的Debug过程就是如此。

23.
记住条件结构就是当条件为真就跳转并执行代码的某一部分

bool comparisonresult = x == 5;

此时的  ” ==“ 双等号被称作等于号，它被用来确认 x 是否等于 5 ，等于5就会返回真，不等就会返回假。

if(comparisonresult == true)       //or if(comparisonresult)    表示判断都可以。



 当运行到此处时，返回值为假，再向下运行，不进入循环，直接跳到循环下方。

设置断点编译后，右击选择 go to disassembly(ctrl+g)   (不过我的版本没找到）跳转到界面有源代码以及对应的汇编语言。

bool 工作原理就是读取内存值看是否为0 ，然后再进行接下来的运行。

上面写成bool是为了表示的更清楚些，这里可以直接写成

int  x = 6；

if（ x == 5)
{
  log("hello world");
}
此时你会发现代码更加简洁。在for语句的下面只有一行的话，可以不用加上{}，但是最好不要和if语句写在一行，应为打断点时不知道到底是看哪个。

const char* ptr = "nullptr";
	
	if(ptr)    
	{
	  log(ptr); 
	 
	}
	else 
		log("ptr is null");
	 
	std::cin.get();
如果是这样的话，字符串则输出“nullptr"，要是没有引号则表示空值，显示else的内容。

const char* ptr = nullptr;
	
	if(ptr)    
	{
	  log(ptr); 
	 
	}
	else 
		log("ptr is null");
	 
	std::cin.get();
null  (空值）。

const char* ptr = nullptr;
	
	if(ptr)    
	{
	  log(ptr); 
	}
	else if(ptr == "hello")
	{
		log("ptr is hello");
	}
	 
	else 
		log("ptr is null");
	 
	std::cin.get();
当使用else if 语句时，当前面的if条件为假时才会检测else if 后面的条件。

const char* ptr = nullptr;
	
	if(ptr)    
	{
	  log(ptr); 
	}
	else 
	   {
	      if(ptr == "hello")
	        {
		       log("ptr is hello");
	        }
	 
	   }
	 
	std::cin.get();
else if 其实就是上图所展示的结合体，如if和log结合一样的效果，在c中并不是关键字。

24.文件存放
可以新建资源文件夹来更合理的存放文件



 点击展示全部文件按钮



此时所展示的就是真正硬盘所保存的文件

 

 在这创建一个src文件夹存放程序，这时文件夹将会显示，将main.cpp拖进去。

 默认的文件夹就是筛选器，是用来给你分类的假象，不是实际目录。

而关于debug文件夹存放在这的位置问题，则可以右击专案点击属性，记得改为所有组态，平台也要改为所有平台，并将相对目录改为指定文件夹。将是输出目录（在屏幕内输入的存储位置）放入解决方案的目录。

 将下面的文件目录也设置好，不同的就是加个intermediates来选择文件夹。

                             $(SolutionDir)bin\$(Platform)\$(Configuration)\

$(SolutionDir)bin\intermediates\$(Platform)\$(Configuration)\ 



如果不知道宏的意思，可以点击右侧下拉框进入编辑的宏查看。 

25.循环
循环就是多次执行同样的代码

1）.for循环：
for（）括号里有三部分，

第一部分是一个变量声明，一般在这声明并初始化个临时变量  int i = 0;  有说法是 i 代表迭代器，因为 i 标志了迭代。

第二部分是条件，满足条件的时候一直执行for循环里的代码;

第三部分是下一次for循环前会执行的代码；

for(int i=0;i<5;i++)
	{
		log("hello world!");
	}
第一部分在for循环之前就执行一次，第二部分在执行每次循环前会检验，第三部分在每次循环结束后执行。


int i=0；
for(;i<5;)
	{
		log("hello world!");
        i++；
	}
当然，写成上面这样也是可以的，甚至扩充成下面这样



 2）.while循环



 3).do ... while;



 do ... while不太常见，它 与while 不一样的地方在于它会先运行一次再进行条件判断。

26.控制流语句
控制流语句，主要有三个，分别是 Continue\break\return .

1）.continue只能用在循环内部，简单的意思是如果还能进行迭代的话，忽略下面的步骤而直接进行下一次的迭代。否则，就会结束循环。

2）.break主要用在循环里，但是它也经常出现在switch语句中，它的功能是直接跳出循环。

3）.return是功能最强大的一个，因为return会直接完全的退出函数。

for(int i = 0;i < 5;i++)
	{
		log("hello world!");
		continue;
	}
正常运行，因为已经在最后了，但是当下面这样时，就会影响。



 ，当将continue替换成break时，将会直接跳出for循环。



 return可以在代码任何地方，会退出函数（ int main() ).

27.指针
指针只是一个地址，它是一个整数，存储着一个内存地址。类型只是我们虚构出来的，让我们过的更轻松而已。

再次强调，指针只是存放地址的整数，如果我们给一个指针一个类型，也只是说在那个地址的数据可能是我们给他的的类型，除此他没有任何意义。

void* ptr = 0;
这样是不行的，地址为0，我们没法读取和写入，程序会崩溃。


void* ptr = NULL;




 当为NULL时，可以看到它只是一个给0 的#define。

void* ptr = nullptr; 空指针，没鸟用。

int var = 8;
	void* ptr = &var;
	std::cin.get();
在上面，我们知道了拿到变量 var 的地址，并把它赋给了一个叫ptr的新变量。



 

 可以看到ptr的值为var的地址，显示var的值。

指针可以看作是门牌，内存则是房间。

此时将void* ptr = &var  替换成   int* ptr = &var再运行时会发现并没有什么不同，和之前的运行结果一样。

但是当改为 double* 时，会报错，因为取值的是整形数据int 的地址，指针的类型，决定了它被加1 的时候，字节走了多少个。类型不对，指针+1后跳的地方可能不对。

double* ptr = &var
强制类型转换不会改变内存，只改变编译器如何解读它。

int var = 8;
void* ptr = &var;
*ptr;
*ptr为解引用那个指针，也就是现在在访问那块数据，可以读取或者写入那块数据。



 通过断点来看到，在运行完解引用后，值已经变成10（十六进制的a)。写入那个内存。

char* buffer = new char[8];
分配了8个字节的内存，并返回一个指向这块内存的开始地址的指针。

接下来用 memset()函数，它接受一个指针，该指针指向这块内存的开始处。接受一个值，比如0，然后是大小。这里是用0来填充buffer开始在八个字节内存大小的内容。

指针也是变量，也存储在内存中，这也是为什么我们可以设置指针的指针。



 当我们使用指针的指针时，可以看到输入ptr的地址存放buffer的地址，此时逆序（两个数字不需要逆转，只要后往前输就行）输入（小端模式）四个字节地址，则会显示buffer指针指向的值。



28.引用
引用只是基于指针的一种高级修饰，使得代码更易读。也可以说是给地址处的数据赋予一定的语义，成为引用。

引用必须引用一个已经存在的变量，它本身并不是一个新的变量，只是其他变量的引用。

在上面指针时，&加在已存在的变量前可以指向它的内存地址。在引用时，&贴着一个已经存在的变量，它是变量类型的一部分。

void Increment(int* value)
{
	(*value)++;
}

int main(void)
{
	int a = 5;
	Increment(&a);
	

	LOG(a);
	std::cin.get();
设置int* value 将把函数的形参变成一个指针，调用函数时将a的内存地址而不是a本身传递给函数。将函数内改成解引用的形式从而改变地址存储的数值而不是地址本身，如果不加解引用*,加的就是内存地址而不是实际的值。因为操作符优先级，所以先设置括号保证先解引用。

void Increment(int& value)
{
	value++;
}

int main(void)
{
	int a = 5;
	Increment(a);
而将其改为引用的方式效果是一样的。

int a = 7;
int b = 8;

int& ref = a;

ref = b;    //引用不可以这样用，此时a = 8,b = 8.
上面的用法是不可以的，此时a = 8,b = 8。因为引用就是直接操作原值。

声明一个引用时，必须立即赋值，因为它必须是某物的引用，它不是真正的变量。

	int a = 5;
	int b = 8;
	 
	int* ref = &a;
	*ref = 2;
	ref = &b;
	*ref = 1;	
	 
	LOG(a);
	LOG(b);
输出a = 2,b = 1。（指针就是地址，解引用是值）。

29.类
class类在{}后还要加个   “  ；” 号，千万不要忘记。

class和struct区别在于，class默认private,struct默认public。

类的本质是一个类型，可以把他当成其他变量来创建。我们使用Player player;创建一个新变量叫做player，类型为Player。由类这种类型构成的变量称为对象，新的对象变量称为实例。这一句所做的是实例化了一个Player对象。若需给变量赋值，使用player.加变量名即可访问。

如果需要写一个函数来操作x和y，需要使用引用传递，因为需要修改Player对象：

class Player
{
public:
	int x,y;
	int speed;
};
void Move(Player& player ,int xa,int ya);

void Move(Player& player,int xa,int ya)
{
	player.x += xa * player.speed;
	player.y += ya * player.speed;
}

int main(void)
{
	Player player;
	player.x = 5;
    Move（player,1,1);

	std::cin.get();
}
简单设置一个,下面将函数移到类中，因为类可以包含函数，这叫方法。

class Player
{
public:
	int x,y;
	int speed;

void Move(int xa,int ya)
{
	x += xa * speed;
	y += ya * speed;
}

};
int main(void)
{
	Player player;
	player.x = 2;
    player.y = 2;
    player.speed = 2;   #实例化

	player.Move(1,1);
关于私有成员只有类内可见，你在内部写一个公有函数调用这些私有变量，函数外调用这个公有函数就行。

c++中结构体存在的唯一原因是想和c维持兼容性，因为c没有类。

struct 更偏向存储数据，而其他的则更偏向使用类。

30.
视频20

class Log
{
private:
	int m_LogLevel;
	
public:
	void SetLevel(int level)
	{
	}

	void Warn(const char* message)
	{
	}

};


int main(void)
{
	Log log;
	log.SetLevel(LoglevelWarning);
	log.Warn("Hello!");
private中使用 m_     ，是为了表示类的变量，而且是私有的。

在一个类中可以设置多个不同或相同的公开或私有，存放不同的部分，例如公共方法，公共变量，公共静态变量。

下面这个例子并不是一个好的代码，但是逻辑比较简单（2012编译不出来，但是2019可以编译）

class Log
{
public:
	const int LogLevelError = 0;
	const int LogLevelWarning = 1;
	const int LogLevelInfo = 2;

private:
	int m_LogLevel = LogLevelInfo;
	 
	
public:
	/*m_LogLevel = LogLevelInfo;*/
	void SetLevel(int level)
	{
		m_LogLevel = level;
	}

	void Error(const char* message)
	{
		if(m_LogLevel >= LogLevelError)
		{
		std::cout <<"[ERROR]:"<< message <<std::endl;
	    }
	}
	 
	void Warn(const char* message)
	{
		if(m_LogLevel >= LogLevelWarning)
		{
		std::cout <<"[WARNING]:"<< message <<std::endl;
	    }
	}
	 
	void Info(const char* message)
	{
		if(m_LogLevel >= LogLevelInfo)
		{
		std::cout <<"[INFO]:"<< message <<std::endl;
		}
	}

};


int main(void)
{
	Log log;
	log.SetLevel(log.LogLevelWarning);
	log.Warn("Hello!");
	log.Error("hello!");
	log.Info("hello!");

	std::cin.get();
}
31.
s_是为了表示这个变量是static的。static表示这个变量在link的时候只对这个编译单元（一个.cpp文件编译后产生的中间文件（.obj)  ）里的东西可见。在link到它实际定义时，linker不会在这个编译单元.obj外面找它的定义。有人理解“静态”就是“原封不动”。类似在class里声明私有成员，其他编译单元不能访问s_Variable.

编译四步骤的文件格式变化 ，由本来的 .c ,.h 文件 预编译后变成 .i ，编译后 变成 .s 文件 ，汇编后变成  .o 文件，最后 link 成  .exe 文件。



static int s_Variable = 5;
在static.cpp里只输入这一行，而在main.cpp里输入如下代码

int s_Variable = 10;

int main(void)
{
	
	std::cout << s_Variable << std::endl;
	 
	std::cin.get();
编译不会显示错误，而当把第一个文件的static去掉时，编译不会出错，但是链接会报错，因为多个文件定义了。

一种解法是直接将main.cpp里的第一行前面加上extern 从而代码变成

extern int s_Variable;
它会在另外的编译单元里找定义，也叫外部链接。只要在定义时尽量在.cpp文件中进行，而不要在.h 文件中定义。定义好了之后，可以在.h文件中利用extern关键字进行声明。

什么时候用static，想想什么时候用私有成员。如果不想全局可见的话。全局变量违背类的封装和信息隐蔽。

32.


下面的则是直接使用初始化器来进行初始化。

在下面再调用 print 函数，例如

e.print();

e1.print();

可以看到按正常的输出两组不同的数，但是将其设置为static时，如下



 可以看到结果输出一样，因为static会使全局共用一块内存，所以第二处改动了，第一处也改了。所有实例共享一个静态变量。 

静态内存：
static定义的变量最终只会分配一次内存，如果再次调用该函数，不会重新分配内存给变量，而是使用上次分配的内存。

33.
void Function()
{
	static int i = 0;
}
函数内的话是当我们第一次调用这个函数的时候，i的值被初始化为0，后续调用不会再创建一个新的变量。



 当没有static时，都是输出1。而当我们设置为static时，



 则为1，2，3，类似于在此声明（全局变量）。共用一块内存，只会初始化一次。

static int i = 0;    //有没有static都一样

void Function()
{


	i++;
	std::cout << i << std::endl;

}
普通全局变量vs静态局部变量。普通全局变量缺点是在其他地方可以访问到它。

class Singleton
{
private:
	static Singleton* s_Instance;
public:
	static Singleton& Get() { return *s_Instance; }

	void Hello() {}
};

Singleton* Singleton::s_Instance = nullptr;

 

int main(void)
{
	Singleton::Get().Hello();
因为Singleton::Get()是一个实例，所以这个实例可以调用类中的方法。方法的返回值是个单例。get返回对象引用，后面调用hello就是对象调用了。可以去看看单例模式。

class Singleton
{

public:
	static Singleton& Get() 
	{ 
		static Singleton instance;
		return instance; 
	}

	void Hello() {}
};

int main(void)
{
	Singleton::Get().Hello();
当然也可以使用静态的方法写成上面这样。

 Singleton instance;
当没有static关键字时，因为实例是在堆栈上创建，运行到return instance;下面的}后就会销毁，然后函数结束。

static Singleton Get() 
当没有引用时，不能保证每次get函数只返回同一个实例。返回实例的引用，避免拷贝从而返回实例本身。用&代表Get（）完全等于instance，如果去掉会在内存的另一个地方创建instance的拷贝，这两个地方的内容是一致的，有&更好。

34.枚举
枚举里面元素最后不需要加分号    ；     。

enum Example
{
	A,B,C
};

int a = 0;
int b = 1;
int c = 2;

int main(void)
{
	Example value = B;

	if(value == B)
	{
	 
	}
 Example value = B;  正确枚举值就像分配值一样，而如果直接赋值，就会出错，就比如像 Example value = 5 这样。（后面试了下可以）

enum Example
{
	A = 5,B = 2,C = 6
};
可以指定值，不指定一般从 0 开始，递增下去。如果未全部指定，也会递增下去。

class Log
{
public:
	enum Level
	{
		Error,Warning,Info
	};

private:
	Level m_LogLevel = Info;
	 
public:

	void SetLevel(Level level)
	{
		m_LogLevel = level;
	}
	 
	void Error(const char* message)
	{
		if(m_LogLevel >= Error)
		{
		std::cout <<"[ERROR]:"<< message <<std::endl;
	    }
	}
	 
	void Warn(const char* message)
	{
		if(m_LogLevel >= Warning)
		{
		std::cout <<"[WARNING]:"<< message <<std::endl;
	    }
	}
	 
	void Info(const char* message)
	{
		if(m_LogLevel >= Info)
		{
		std::cout <<"[INFO]:"<< message <<std::endl;
		}
	}

};
将日志的例子拿来改，main函数里用Error是不可以的，因为上面存在error的函数。

int main(void)
{
	Log log;
	log.SetLevel(log::Error);
此时就应该向枚举添加级别前缀

enum Level
	{
		levelError,levelWarning,levelInfo
	};
class Log
{
public:
	enum Level
	{
		levelError,levelWarning,levelInfo
	};

private:
	Level m_LogLevel = levelInfo;
	 
public:

	void SetLevel(Level level)
	{
		m_LogLevel = level;
	}
	 
	void Error(const char* message)
	{
		if(m_LogLevel >= levelError)
		{
		std::cout <<"[ERROR]:"<< message <<std::endl;
	    }
	}
	 
	void Warn(const char* message)
	{
		if(m_LogLevel >= levelWarning)
		{
		std::cout <<"[WARNING]:"<< message <<std::endl;
	    }
	}
	 
	void Info(const char* message)
	{
		if(m_LogLevel >= levelInfo)
		{
		std::cout <<"[INFO]:"<< message <<std::endl;
		}
	}

};
改完后的代码如上图。

35.


 并没有初始化内存，要做的实际上是初始化内存，并将其设置为零或者类似的东西，



 创建一个Init,将x,y设为0，但是当我们要使用时，每次都要先调用Init函数，很麻烦，构造一个空函数。



 当创建一个Entity()后，可以如上图所示使用。



带有参数的构造函数可以直接将参数设置，从而更加简洁高效。构造函数的重载。

class Log
{
private:
	Log() {}
public:
	static void Write()
	{
	}
};

 int  main(void)
{ 
	Log::Write();
	Log 1;
当在entity类中再定义个Log类时，主函数中的 log 1;  语句会报错。

而当将private: 

Log() {}  去掉，则可以。

可将代码改成

class Log
{
public:
    Log() = delete;

	static void Write()
	{
	}
};
c++11之后的delete表示函数不可被调用。使class不能实例化。构造函数的作用是对class内部变量初始化。

36.构造函数
在c++的类中，构造函数是一种特殊的成员函数，在每次创建一个类的时候会默认调用构造函数进行初始化工作。

构造函数用来完成一些必要的初始化工作，有了构造函数之后，就无需再单独写初始化函数，并且也不必担心忘记调用初始化函数。故构造函数的作用：初始化对象的数据成员

构造函数对变量初始化，析构函数清理内存。

构造函数和析构函数实际的区别是在析构函数前面放的波浪号。

Entity()
	{
		std::cout << "Created Entity!" << std::endl;
	}
构造函数，输出创建Entity，下面为析构函数，输出销毁Entity。

~Entity()
	{
		std::cout << "Destroyed Entity!" << std::endl;

	}


 当这种写法时，仅在主函数退出时看到析构函数被调用，不会真正看到，因为我们的程序将在之后立即关闭。所以要写一个函数执行所有实体操作，并且在下面调用。



 而像这样，则会创建一个实体，然后打印下X,Y。最后销毁实体。



 设置断点，f5运行时什么都不显示，当运行下一行时，显示创建实体。再接着f10，则会看到显示X,Y的值，此时到达作用域的结尾，再次运行时会跳到下面的位置



 因为对象是创建在堆栈上，它应该在结束后自动销毁。

37.泛式和继承
继承可以利用共同的代码，避免代码冗余。

记住创建子类时，它将包含父类所包含的所有内容。



 因为entity中有两个浮点数 X和Y,打印entity对象的大小，则可以使用sizeof(entity)，得到大小为8，因为我们在entity中有两个浮点数。

接下来打印Player，如果是改为下面这样，那Player本身只有一个const char指针，指针实际是整形，在32位上应该是四字节的内存。

class Player:public Entity

 


std::cout <<sizeof(player) <<std::endl;
但是因为我们是继承了所有，所以在那个entity类中，因此它实际上的应该是 4+4+4 ，即 12。



 player继承了entity，可以访问entity里面的public变量或函数等。已经将entity所拥有的所有内容复制粘贴到player类中。每个类的内存都可能不同。

另外要了解虚函数表和多态。

多态：同样的调用语句根据子类不同有不同的表现形式。

38.
虚函数允许在子类中重写父类的方法。

假如B是从A派生来的，也就是B是A的子类，如果我们在A类中创建一个方法并将其指定为virtual（虚），我们可以选择在B类中覆盖该方法来做其他的事。



Entity是基类，有一个Get Name的公共方法。Player构造函数使用了初始化列表。构造列表，在构造的时候先于函数体执行，代表将name赋值给m_Name.

 该程序运行后输出 “Entity” 和 “Cherno" 两个单词。



 当在下面设置一个p，是指向Player类型的指针，虽然输出是”Entity“，但实际上是Player的一个实例，关于基类指针指向派生类，如果是private则不允许访问，如果是public继承，则可以。



 当这样调用的时候会发现两次都显示 ”Entity“ ，是因为Print Name中获取Entity类型的指针，在调用Get Name函数时，将查看Entity，然后在那调用Get Name函数。我们希望的是Player调用的时候实际上是Player，而不是”Entity“ ，这种情况是虚函数出现在虚函数中。

怎样正确的显示呢，我们可以写成下面这样，虚函数表

class Entity
{
public:
	virtual std::string GetName() { return "Entity"; }


 在前面加一个 virtual， 如果下面的被覆盖，可以通过Player中的

std::string GetName() { return m_Name;}
更改指向正确的功能,当然也可以加上  "override"，更好。

std::string GetName() override { return m_Name;}
virtual会创建虚函数表，虚表中类似存储函数指针，不同类初始化时，保存的函数指针不同，因此不同实例的基类指针访问虚函数时，获取的函数指针不同，完成重载。

显示的使用 override可以告诉编译器去寻找基类中指定的 virtual function。

39.纯虚函数
纯虚函数本质犹如其他语言中的抽象方法和接口。纯虚函数允许我们定义一个在基类中没有实现的函数，然后迫使在子类中实际实现。只有定义没有实现，需要子类的具体实现。

在面向对象程序设计中，创建一个只包含未实现方法并且交由子类去实际实现功能的类是非常普遍的，通常被称为接口。

接口类实际上不包含实现方法，所以无法实例化该类。

class Entity
{
public:
	virtual std::string GetName() = 0；
}；
将上个代码中此部分删除函数体，取而写上  “ = 0”，此时虽然还是被定义为虚函数，但是 = 0实际上已经将它变成一个纯虚函数。

当改为纯虚函数后，会发现下面的main函数中的

Entity* e = new Entity();
代码显示异常，就拿Player来说，当提供一些字符的时候，他能正常运行。

Entity* e = new Player(" ");
例如，Player类就是在另一个实现Get Name函数的子类的子类（父类的父类是纯虚函数（这段有些存疑，忘了是不是这种说法，感觉有些不靠谱啊），父类覆写实现以后就不是虚函数，子类就可以直接继承。这样也可以，但是我们所要的因为Get Name函数实现了原有的纯虚函数（剔除抽象类（抽象类就是有>=    1个纯虚函数，可以有成员变量）的性质），使我们能创建这个类的实例化。

class Printable
{
public:
	virtual std::string GetClassName() = 0;
};


//创建一个Printable类，类内仅需要虚函数Get Class Name，并将其设置为纯虚函数。
void Print(Printable obj)
{
	 std::cout <<obj->GetClassName() << std::endl;
}

//再主函数上方加上一个构造函数Print（）
class Entity : public Printable
//设计Entity实现接口，在后面加上 : public Printable
class Player : public Entity,Printable
在后面加上，Printable，仍然可以把他当作普通接口。

class Printable
{
public:
	virtual std::string GetClassName() = 0;
};

class Entity : public Printable
{
public:
	virtual std::string GetName() { return "Entity"; }
	std::string GetClassName() {return "Entity";}

};
class Player : public Entity,Printable
{
private:
	std::string m_Name;
public:
	Player(const std::string& name)
		: m_Name(name) { }

	std::string GetName() { return m_Name;}

};
void PrintName(Entity* entity)
{
	std::cout << entity->GetName() <<std::endl;
}

void Print(Printable* obj)
{
	 std::cout <<obj->GetClassName() << std::endl;
}


 int  main(void)
{ 
	Entity* e = new Entity();
	/*PrintName(e);*/

	Player* p = new Player("Cherno");
	/*PrintName(p);*/
	
	Print(e);
	Print(p);
	 
	std::cin.get();
当写成上面这样的话，还无法输出为两个 “entity” ，因为Player也是Printable的子类，但是没有覆写GetClassName方法，继承Entity中的方法，当然如果两个继承中把Printable去掉就可以了，父类覆写也可以的。此时main()函数中的PLayer会显示未覆写getclassname*()的方法。

public:
	Player(const std::string& name)
		: m_Name(name) { }

	std::string GetName() { return m_Name;}
	std::string GetClassName() {return "Entity";}   //当没有这行的时候，main函数里player 指针的设置会显示纯虚函数Player    没有覆写项。
当将entity类中加上 override

class Entity : public Printable
{
public:
	virtual std::string GetName() { return "Entity"; }
	std::string GetClassName() override {return "Entity";}
并粘贴修改到Player类中

public:
	Player(const std::string& name)
		: m_Name(name) { }

	std::string GetName() { return m_Name;}
	std::string GetClassName() override {return "Player";}
则运行后可以打印出“entity”以及一个“Player”。

可以创建一个全新的类，比如一个Printable类型的A，但是必须包含Get Class Name函数，没有的话就不能实例化。它继承于Printable，并且保证它重写了GetClassName 函数。

class A : public Printable
{
public:
	std::string GetClassName() override { return "A";}
};
此时从原则上讲已经实例化了该函数。可以在main函数直接调用print函数

Print（new A());
这样写可能会造成内存泄露，最好除了测试不要写。

40.访问控制
它本质指的是类中的成员数据及成员函数的可访问性（谁能访问，调用，使用它们等等）。它和性能什么的没什么关系，只是纯粹语言中存在的东西，使得你更好的编写、组织代码，仅此而已。

C++中有三个基本的访问控制修饰符:public、private、protected。

在class中，不定义访问控制，就是private。在struct中，不定义访问控制就是public。



 如果设置为私有，则在主函数中就不可以将2赋值给x。



 而Entity的派生类player，在player的构造函数也不能访问x。只允许类内以及友元才能访问这些变量。对函数也是一样的。

class Entity 
{
private:
	int x,y;

	void Prite() { }
public:
	Entity()
	{
		x = 0;
		Prite();
	}

};
      但是像这样添加一个Print函数，在类内调用这个函数是可以的。

protected表示类内以及所有的继承体系中的派生类都可以访问这些属性。表示只能在本类和子类中访问使用。

​                                                                                                                                                 

 当将Private改为Protected后，类Player中可以x赋值为2，但在主函数中因为是一个完全不同的函数而且还不是派生类。

至于Public就是都可以访问。

41.Array（数组）
Array是一些元素的集合，是一些按照特定的顺序排列的东西。在我们的例子中，数组基本上就是用来表示存放一堆变量的集合。通常来说都是相同的类型。数组非常重要和有用的原因是因为有非常多的场景我们想要去表示一大堆数据的整个集合。



 当想要打印这个数组时，只会打印它的内存地址，因为它实际上是一个指针。

​                   

example[-1] = 8;
example[5] = 9;
   当访问一个不在数组中的索引时，比如 —1或5，那么会造成内存访问冲突，因为正在尝试访问不属于自己的内存。在调试时会显示出，但release模式下可能不会收到错误，意味着已经改动了不属于自己的内存。

for(int i = 0;i < 5;i++)
	{
		example[i] = 2;
	}
     将所有成员值设为2，一般不用i <=4,这样写有性能问题，因为做的是小于和等于比较。                     打上断点，输入Example可以看到一行2，数据很重要的一点就是数组中的数据是连续的，也就是说它们把数据放在了一行。                                                                                                                这里得到了一行被分成几个4字节的20字节的内存。但不是真正的分割，但是当通过代码访问时，是这个效果。                                                                                                                                         

int example[5];
   一个数组就是一个指针，这里是一个指向包含5 个整数的内存块的整形指针。                                   

	int example[5];
	int* ptr = example;
	
	example[2] = 5;　
所以可以设置一个整形指针。访问元素二号，赋值为5，结果就是会写入从指针偏移8字节的内存中。这里还可以使用指针重写，就是指针加2，因为我们要向前访问2个元素，，然后解引用，把它设为6.

     *(ptr + 2) = 6;
因为是整形指针，所以为8个字节，要是char 的话

*（int*)((char*)ptr + 8) = 6;
因为需要的是Int指针类型，还需要转换一下，才指向整形。

可以在堆上创建一个数组。

int example[5];
	int* another = new int[5];
第一行是创建在栈上的，它会在跳出这个作用域时被销毁。

第二行是建立在堆上的，会一直存活直到我们把它销毁或者程序结束。

int example[5];
	for(int i = 0;i < 5;i++)
	{
		example[i] = 2;
	}

	int* another = new int[5];
	for(int i = 0;i < 5;i++)
	{
		another[i] = 2;
	}
	 
	delete [] another;
分别对栈和堆存储使用for循环，编译后查看内存都是五个连续的2.那为什么要使用new关键字动态分配，而不是在栈上创建他们呢。最大的原因就是生存期，new分配的内存会一直存在，直到你手动删除它。



 创建一个Entity类，使用栈的形式，输入&e，查看Entity e 内存地址，可以看到正常显示五个2，

当改为在堆上创建时，

 int* example = new int[5];
 再次输入内存地址时，可以发现并没有五个2，而是一串内存地址，其实这就是个指针，因为字节序问题，将地址反转输入，就可以跳到真正的内存处看到数据。这就是内存间接寻址。p->p->array.

计算大小或数量。可以使用sizeof().

int length = sizeof(a) / sizeof(int);
这个只能在栈上使用，在堆上使用时，因为是整形指针，为4字节，所以4/4就会变成1.

const int size = 5;
int example[size];
下面的size会报错，因为在栈中为数组申请内存时，它必须是一个编译时就知道的常量。所以要用static标记，还可以用constexpr表达式。类中的常量表达式必须是静态的。

constexpr int size = 5;
int example[size];
static const int size = 5;
int example[size];
statc const int size = 5;
int example[size];

std::array<int,5> another


Entity()
{
     for(int i = 0; i < another.size();i++)
当使用array方法时，可以如上使用。

42.string(字符串)（P32）
　string本质上来说就是一串字符，字符就是指字母，数字和符号这些东西，本质上就是文本。

c++默认处理的字符方式就是Ascll字符。

const char* name = "Cherno";
c++11后这种声明必须加上const,字符串是不可变的，不能扩展一个字符串让它变得更长。



 因为使用了const ，所以不可以直接赋值，如果需要更改，必须将const 去除

一般不用new，则不要在下面使用delete。关于堆和栈，之后会讲到。



 运行后可以在内存视图输入name （name就是指针），可以看到在前面有一堆的内存，在后面可以看到：“ Cherno ”的字符串。还可以看到设置为 0 的字符，就是空终止符，可以知道字符串在哪里结束。可以通过终止符知道字符串的大小。

ascll码，大写A对应十进制为 65 ，十六进制为 41  ，小写a对应十进制为97，十六进制为61. 



 当通过设置字符数组的形式打印时，可以看到后面被设置为cc，被称为内存守卫，为了让我们知道这些内存在我们数组外面。当将其大小改为7，在后面加上一个‘\0‘或者直接加个 0 都可以，就可以正常打印。0

//char* name = " Cherno";
std::string name = "Cherno";
怎么使用string呢，先把char*改为std::string，记得加个头文件 #include<string>;

为什么还需要加 #include<string>，因为输出string时，  <<  符号需要重载。

有了#include<string> 头文件后，还可以进行其他操作。

    std::string name = "cherno";
    
    std::cout << name.size() <<std::endl;
可以直接使用size()，得到字符串长度为 6 。

当使用strlen()时，需要注意对象为 const char* 或 char* ,string会报错。正确输出长度也是 6 （不包含空字节）。



std::string name = "cherno" + “hello！”;   //会报错，不能将两个 const char 数组相加。
	
上面这段代码会报错，不可以尝试将两个 const char数组相加。它们不是真正的 string 。

如果你想这么做，可以分成多行，

std::string name = "cherno";
name += " hello! ";
上面这样就是将一个指针加到字符串 name 上了。然后  +=   这个操作符在 string 类中被重载了。

std::string name =std::string( "cherno") + " hello!";
	
或者像上面这样，作者喜欢这样显示地调用 string 构造函数将其中一个传入string构造函数中，相当于你在创建一个字符串，然后附加这个给他，这很好。

如果你想查找字符串中的文本，可以使用  .find()，然后传入你要查找的文本。

std::cout << name.find( 'e' ) << std::endl;
例如上面这样，结果就为  2 。

std::cout << name.find( "no" ) << std::endl;
而当查找 “no”，则为4，为起始位置。

如果写一个叫 PrintString 的函数，想给他传一个字符串，不会写成下面这样，这只是一个拷贝。当像这样将类对象传入一个函数时，实际上做的是创建这个对象的一个拷贝然后传给这个函数。

void PrintString(std::string string)
{
	std::cout << string << std::endl;
}
如果我想写一个string  += " h" ,不会影响到下面传入的原始字符串。这明显只是一个只读函数，不会修改任何东西。

void PrintString(std::string string)
{
    string += "h";
	std::cout << string << std::endl;
}
在main() 函数中输出

int main(void)
{ 
	std::string name = std::string("cherno") + " hello!";
	PrintString(name);
结果是输出   cherno hello! h    

复制字符串意味着我们必须在堆上动态的创建全新的 char数组来存储我们之前已经得到的完全相同的文本。这并不快，复制字符串实际上很慢。 所以任何时候像这样传入一个只读字符串时，确保通过常量引用来传递他。

void PrintString(const std::string& string)
写成上面这样，&  告诉我们是一个引用，意味着不会被复制。const 意味着不会修改它。但是函数里不可以修改了。

43.String Literals
string字面上,下面双引号之间的就是字符串。鼠标放上去显示 (const char[7])"cherno".

可以手动写成 “cherno\0" 或者 ”cherno";0    标记着字符串的结尾。这个并不是字符 ‘0’。

int main(void)
{
    "cherno";

}
如果在中间放入一个空终止符  "\0" ，那么可以得到



 在右下角能看到输出字符串有两个 .       这是   '\0'的效果。

当用strlen(name)输出字符串长度时，输出为3。

char* name = "cherno";
name[2] = 'a';
如果在main函数里这样写法，好像可以，但其实是不可以的。因为你实际上是在用一个指针指向那个字符串字面量的内存位置，但是字符串字面量是存储在内存的只读部分。当我们将它打印在控制台时，会发现不会改变，并且在debug模式下会报错，显示试图对只读内存进行写操作。

如果真的想要改写，只需要把类型定义为一个数组而不是指针。

char name[] = "cherno";
name[2] = 'a';
当然，从C++11 开始，有的编译器比如Clang实际上只允许你写成  const char* （const 不再允许省略，包括新版本的VS）。

char* name =（char*)"cherno";
name[2] = 'a';
想要修改的话可以试试上面这种，手动将字符串转换成char*  .(近版好像不可以了）。

const wchar_t* name2 = L"cherno";
上面这种为 宽字符，在后面内容的 前面必须加上一个大写 的   L   ，表示接下来的字符串字面量是由宽字符组成的。

const char16_t* name3 = u"cherno";

const char32_t* name4 = U"cherno";
除了上面的，你还可以在普通的文本前面加上   u8     前缀。

const char* name = u8"cherno";
本质上来说，char就是一个 1 字节的字符，char16是 2 字节，每个字符占  16bit。然后就是char32就是每个字符占 32bit,也就是4字节，这就是utf32.

关于wchar和 char16，好像每个字符都是 2字节，但是wchar实际上是由编译器决定的。可能是1字节，或者2字节，或者4字节。但实际中通常为2或者4字节。（在windows上是 2 字节，Linux是4字节，maybe在mac上也为4字节）。 

using namespace std::string_literals;


std::string name0 = "cherno"s + "hello";
在C++14的string_literals库里，可以在字符串末尾加上字母 s ,这实际上是一个函数，

using namespace std::string_literals;


std::wstring name0 = L"cherno"s + L"hello";

 


using namespace std::string_literals;


std::u32string name0 = U"cherno"s + U"hello";
可以像上面这样操作。别忘记前后也要一起改。

也可以使用另一种，比如字母 R   。R 很有用。

可以写const char ,然后要在字符串前面加上字母 R,意味着他会忽略转义符。实际中，要打印很多行的字符串这很有用。

const char* exampe = R"(Line1
Line2
Line3
Line4)";
OR 

const char* ex = "Line1"
         "Line2"
         "Line3";
像上面这样也可以。

char name[] = "cherno";
name[2] = 'a';
关于字符串字面量肯内存的关系，我们做的就是获取到 Cherno 然后把它复制到变量 name 中，创建了一个变量。如果不写这个代码，就会试着修改指向常量数据的指针。我们实际上就是尝试往常量数据里写数据。我们移动一个数值 97 到 name 变量中偏移 2 字节的位置。就是 name[2] = 'a';  做的事。

44.Const
基本上 cosnt就是你做出承诺某些东西是不变的，不会改动的。

const int max = 90;
int* a = new int;

*a = 2;
a = (int*)&max;
上面这样可以重新分配指针，这样就会指向其他的东西，比如指向max，但是为了绕开 const 限制，可以把他强制转换成 int* 类型（通常情况下是不应该这么做的）。

const int max = 90;
const int* a = new int;
将上面的靠下一行前加上 const，表示不能再去修改指针的指向内容了。

不可以改写指针指向的内容，但可以把指针重新指向其他地方。

const int max = 90;
int* const  a = new int;
将上面 const 位置改变，效果与之前相反。此时可以改变指针指向的内容，但是不能把指针自身重新赋值指向其他东西。注意  const int*   和    int const* 是一样的。 （关键是看 const 是在 *  前面还是后面。）

要让指针变成常量，使它不能重新分配，要把const 放在 * 后面，变量名之前。

 如果是下面这种写法啊，则表示都不可以更改。

const int* const a = new int
class Entity
{
private:
	int m_X,m_Y;
public:
	int GetX() const 
	{
		return m_X;
	}
    
    void SetX(int x)
    {
        m_X = x;
    }
};
上面这个类中，将 const 放在方法名的右边，在参数列表的后边写上const。

这是 const 的第三种用法，他和变量没有关系，而是用在方法名的后面，且只有在类中才可以这么用。这意味着这个方法不会修改任何实际的类，因此可以看到我们不能修改类的成员变量。这是一个只读的方法。下面的SeTX()因为需要修改数据，所以不可以用 const 。

class Entity
{
private:
	int* m_X,m_Y;
public:
	const int* const GetX() const 
	{
		return m_X;
	}


    void SetX(int x)
    {
        m_X = x;
    }

};
上面可见，如果m_X是指针的话，那么下面可以一行可以写三次 const，表示三种限制。

注意

int* m_X, *m_Y
只有像上面这样，m_X 和 m_Y才都是指针，必须在每个变量前面也加上 *   符号才可以表示指针。

void PrintEntity(const Entity& e)
{
	std::cout <<e.GetX() << std::endl;
}
上面又设置了一个函数，来使用GetX() 方法。并且希望能用常量引用传递这个参数，就不用复制Entity类，因为这要占用 8 字节。（不复制所有的对象，特别是只读的，所以用常量引用来传递那些参数）。

此时引用类似指针，如果是指针，可以更改它的指向，但不可以修改他的内容。引用也是，不可以修改 e 的内容。但是他们的工作方式不同。如果你重新分配这个引用，实际上实在修改这个对象而不是其他的对象。  这不像指针那样有指针本身和指针指向的内容，因为引用就是内容。引用就是那个 Entity，因此你不能修改Entity，尽管是引用。


public:
	int GetX() 
	{
		return m_X;
	}
上面把 GetX方法后面的 const 去掉的话，下面PrintEntity()函数就会报错，因为GetX()函数就不可以保证不会修改Entity 了。

就算是不直接修改Entity，但是也不可以间接调用一个可以修改 entity 的方法。所以必须把方法标记为const，这意味着可以通过const Entity 调用任何const函数。

所以有时候会设置两个函数，一个是有 const 的函数，另一个没有设置 const ，来保证需求。

记住没有修改类或者不应该修改类时，最好记得把你的方法标记为 const 。因为这样就能阻止别人在有常量引用或类似情况下使用你的方法。

(

     这里我自己设置了关于GetY()方法 的一个函数，当参数不为引用时，会报异常但是也可以运行。

void Print(Entity& e)
{
	int y;
	std::cin >> y ;

	std::cout << e.GetY(y)<< std::endl;

}
代码贴上，见上面。

）

当有一种情况下，虽然标记了 const ，但是你是真的想要修改数据，那么可以使用  mutable，意思是可以被更改的（可变的）（允许 const 的方法修改变量）。例如下面这样

class Entity
{
private:
	int m_X,m_Y;
    mutable int var;

public:
	int GetX() const 
	{
        var = 2;
		return m_X;

	}
当这样写后，虽然方法是 const 的，但是还是可以修改 var  的值。

45.Mutable关键字
mutable共有两种用法，一种就是上面的和类里面的 const 一起用，另一种就是用在 lambda 表达式中（或者同时包含两种情况），

class Entity
{
private:
    std::string m_name;
public:
	const std::string& GetName() const 
       { 
             return m_name;
       }

};
上面设置了一个 Entity类，public部分设置了一个简单的 getter函数，用 const std::string& 传参。函数返回 m_name。 

至于为什么要设置为 const ,如果下面有一些 const Entity对象，我们就可以调用const方法，但是如果没有加 const 就不可以调用。例如下面这样

int main(void)
{
    const Entity e;
    e.Getname();
mutable在类 const 最常用的情况就是，例如需要计数类中的调用情况，在类中设置一个常数，然后在方法中累加

class Entity
{
private:
    std::string m_name;
    
    mutable int count = 0;

public:
	const std::string& GetName() const 
       { 
             count++;
             return m_name;
       }

};
这是一个很常用的方法。

下面就是另一种用法，这就是一个 lambda.

int x = 8;
auto f = []()
{
     std::cout << "hello" << std::endl;
}
基本上来说，lambda 就是一个一次性的小函数，你可以写出来并赋值给夜光表变量，就像我们这里做的。

int x = 8;
auto f = [&x]()
{
     std::cout << x << std::endl;
}
上面这样，如果要把 x 传过去，需要定义一些捕获函数，可以向上面这样通过引用传递变量，或者直接直接传值。或者通过 = 传值。或者直接用 个  & 对所有进行引用传递。

int x = 8;
auto f = [=]()
{
     int y = x;
     y++;

     std::cout << x << std::endl;
}
上面的是按值传递，但是不可以直接使用   x++ ,因为这样会报异常。要创建另一个新的局部变量，然后把 x 赋值给他，再修改这个新变量。所以可以使用 mutable ，

int x = 8;
auto f = [=]() mutable
{
     x++;
     std::cout << x << std::endl;
}
上面这样和前面的局部变量一样，它会先创建一个局部变量，但是代码看起来会干净很多。

当然，如果在外面调用这个 f ，那么 x 的值任然是8，并不会自增为9，因为并不是通过引用来传递他的，是通过值传递的。只是复制了 8 这个值 传递进了 lambda。

实际情况下并不会在 lambda 中使用 mutable，甚至没见过。

46.构造函数初始化列表
就是我们在构造函数中初始化类成员的一种方式。

class Entity
{
private:
    std::string m_name;
public:
	Entity()
	{
		m_name = "unknown";
	}

	Entity(const std::string& name)
	{
		m_name = name;
	}
你可能在其他语言中这么写，但是在C++中还有另一种方式，就是初始化成员列表。

int main(void)
{ 
	Entity e0;
	std::cout << e0.GetName() <<std::endl;

	Entity e1("cherno");
	std::cout << e1.GetName() << std::endl;
上面的就是根据两个不同的构造函数所列举的两个调用，输出结果是    unknown      cherno      两个。

下面进入初始化成员列表

class Entity
{
private:
    std::string m_name;
public:
	Entity()
         ： m_name("unknown") { }
：   可以写在同一行或者下一行缩进都可以，喜欢的话可以写在下一行，然后可以列出你想要初始化的成员，这里我们是 m_name ，然后在后面的括号里给他一个值，这里是 “unknown” ，

class Entity
{
private:
    std::string m_name;
    int m_score;
public:
	Entity()
         ： m_name("unknown"), m_score(0)
      {


      }
要是有多个变量，可以参考上面的写法。（对了，需要注意的是，如果要进行多个变量成员初始化，要按照上面的相同的顺序写，不然有些编译器可能会警告你。不管你怎么写初始化列表，他都会按照类成员的定义顺序进行初始化。

Entity(const std::string& name)
       ：m_name(name)
	{
		
	}
之前下面的就如上面写的这样，用 （） 代替  =   ，然后将他们移动到列表中。

现在到了为什么我们要这么做的时候了，嗯，如果有很多的成员变量时，在大括号写可能会感觉杂乱，很难看出来构造函数在做什么，可能你后面还有许多其他要做的事，而不应该大部分都在做初始化变量。要想隐藏他们，就可以这样写。

class Example
{
public:
	Example()
	{
	std::cout << "created entity!" << std::endl;
    }

    Example(int x)
    {
    std::cout << "created Entity with " << x << "!" << std::endl;
    }
};

class Entity
{
private:
    std::string m_name;
	Example m_Example;

public:
	Entity()
	{
		m_name = std::string ("unknown");
		m_Example = Example(8);
	}

	Entity(const std::string& name)
	{
		m_name = name;
	}
	 
	const std::string& GetName() const 
	{ 
		return m_name;
	}

};

int main(void)
{ 
	Entity e0;
上面代码得到的结果见下图，主函数使用了默认构造函数创建了一个 Entity 对象。



 一个为默认构造函数，没有参数；一个是有个整形参数的构造函数。实际上创建了两个 Entity。 

Example m_Example;
 一个是在这里创建的，就像写在下面的一样。


public:
	Entity()
	{
        Example m_Example;
		m_name = std::string ("unknown");
		m_Example = Example(8);
	}
意味着他在主函数创建一个 Entity对象，则在成员区域也会创建一个 Example对象.在成员区域并不意味着就不会进行代码进行创建实例。

m_Example = Example(8);
然后上面这个代码还在这里创建了一个新的 Example对象，然后把它赋值给 m_Example   (旧的）。我们创建了一个实例,扔掉它，然后用新的实例覆盖掉它。我们建了两个对象而不是一个。

这时候就可以选择把它移动到初始化列表中，我们有两种选择，一种是可以像前面的那样写

class Entity
{
private:
    std::string m_name;
	Example m_Example;

public:
	Entity()
         ：m_Example(Example(8))
	{
		m_name = std::string ("unknown");
		
	}
就像上面这样，如果运行就会发现只创建了一个实例，直接输出上面两行的 下面一行输出。

就算你不喜欢这样的代码风格，也要去习惯它们，因为这不仅仅是代码风格的问题，实际上还有功能上的区别。如果不使用他们，可能造成性能上的浪费。并非所有情况都是这样，对于整形这样的基本类型，他不会被初始化，直到你通过显示赋值来初始化他们。作者总是使用他们，他不会区分原始类型和类类型。

47.三元运算符
三元运算符其实就是一个问号和一个冒号。

static int s_level = 1;
static int s_speed = 2;

int main(void)
{ 
	if (s_level > 5)
		s_speed = 10;
	else
		s_speed = 5;

 

例如上面这样，if语句 可以直接写成下面这个三元运算式， 判断式为真，则为问号后的值，判断式为假，则为冒号后的值。

s_speed = s_level > 5 ? 10 : 5;
下面这种用法也很有用

std::string rank = s_level > 10 ? "master" : " beginner";
向上面这样，设置一个等级变量，然后用等级当判断条件来选择。

不然就要像下面这样写

std::string otherrank;

if (s_level > 10)
    otherrank = "master";
else
    otherrank = " beginner";
但是这样的话实际上还会创建一个变量，因为这种声明方式，实际上会构造一个空字符串对象，然后又会被后面的代码中的字符串对象覆盖。

而使用三元运算符实际上不会构造中间字符串，这与返回值优化有关，关于这点以后再讨论，这是一种高级编译器特性，是一种优化方式。

s_speed = s_level > 5 ? s_level > 10 ? 15 : 10 : 5;
还可以像上面这样嵌套，多个条件使用。上面这个判断是如果 level 大于 5 ， 然后再添加一个条件，如果 level 大于 10 .就把 speed 设置为15，否则就是 10 ，再然后就是 5 .这逻辑也很好理解，在技术层面可行，就是分为 <5 ,> 5 & < 10 , > 10三种，可以将它按部分分开来看，注意前一个是条件为真。

s_speed = s_level > 5 && s_level < 100 ? s_level > 10 ? 15 : 10 : 5;


s_speed = (s_level > 5 && s_level < 100) ? s_level > 10 ? 15 : 10 : 5;
上面两行语句执行是一样的。证明上面的那行语句执行可以有下面的结果。尽量不要这么写，没太大意义。尽量不要用三元运算符嵌套，没什么意义。

48.创建对象
基本上我们编写了一个类，并且到了我们实际开始使用该类的时候，就需要实例化它，除非是完全静态的类。

一般有两种选择，区别是内存来自哪里以及对象实际上会创建在哪里。

应用程序会把内存分为两个主要部分：堆和栈.

栈对象有一个自动的生存周期，是由它声明的地方的作用域决定的。一旦超过这个作用域，他的内存就会被释放掉。当这个作用域结束时，栈会弹出，在这个栈上，这个作用域的所有东西都会被释放。

但是堆不一样，一旦你在堆上分配一个对象，你实际上已经在堆上创建了一个一直存在那里的对象。直到你决定：不再需要他，想释放那个对象。

using String = std::string;

class Entity
{
private:
	String m_name;
public:
	Entity() : m_name("unknown") {}
	Entity(const String& name) : m_name(name) {}

	const String& getname() const { return m_name;}
};
类里有一个字符串成员，和一个不接受任何参数的构造函数，还有一个构造函数接受一个字符串参数，然后把 m_name 设置为这个参数，最后还有一个简单的 getname（） 方法。

Entity entity;
首先就是在栈上创建它，先写实例化的类的类型，然后是空格，然后再给他一个名字，叫 entity。

因为已经这么写了，所以会默认调用构造函数。可能看起来这会导致空指针或者空引用异常，因为看起来就像是根本没有初始化对象。但是已经初始化了，只要上面类里面有默认构造函数，就是完全有效的代码。（这指punlic里第一行）。

Entity entity("cherno");


Entity entity = Entity("cherno");
上面这样就是指定一个参数，用括号括起来给它传一个名字。也可以像下面一样加等号，然后写类型，其实就是构造函数。

我们希望什么时候像这样来创建对象，几乎所有时候。如果可以像这样创建对象的话，那就这么来创建。这在C++中是初始化对象最快的方式和最受管控的方式。

什么时候不可以这样用，一个是如果你想让他在函数生存期之外也能存活。

void Function() 
{
   Entity entity = Entity("cherno");
}
上面如果还存在一个函数，在这个函数里创建 Entity,一旦到达上述代码最下面的大括号，这个Entity就会在内存中销毁。因为当我们调用 Function时，就是为这个函数创建了一个包含了生命的所有局部变量的栈结构。

当这个函数结束时，栈结构会被销毁，也就是说栈上的所有内存，在栈上创建的所有变量都会消失。

int main(void)
{
       Entity* e;
        {
            Entity entity = Entity("cherno");
            e = &entity;
            std::cout << entity.Getname() << std::endl; 
        }
如果创建一个Entity 指针，就是一个指向 Entity 的变量。把它赋值为 Entity 的内存地址，也就是我们在栈上创建的那个对象。

上面的代码可以简化一下为下面这样，一般也是写成下面这样。

Entity entity("cherno");
运行后先是创建了一个新的 Entity 对象，名字是 Cherno，然后按 F10向下运行会设置夜光表 e 指针。鼠标移上去，可以看到指向了正确的内存地址。



 继续向下运行，当到下面这步时，结果如下图



 而当再往下运行时，就会发现指针还是指向相同的地址，但是 m_name已经没有了。因为那个对象被释放或者销毁了，这个叫 cherno 的 entity 对象已经不存在了，已经不在栈结构里了，这就是 cherno 的终结。

如果想要第一行的 cherno 在作用域外任然存在的话，那就不能将它分配到栈上，而是必须要分配到堆上。另一个不想在栈上分配的原因可能是，如果这个 entity 太大了，同时我们可能有很多的entity。栈通常都很小，一般就一两兆，取决于你的平台和编译器。如果你有很大的类或者想要创建非常多的类，在栈上你可能没有足够的空间来分配。因此你得在堆上进行分配。

如果想把这些代码改成在堆上分配，首先要做的就是改变这里的类型。

int main(void)
{ 
       Entity* e;
        {
            Entity entity("cherno");
            e = &entity;
            std::cout << entity.getname() << std::endl; 
        }
上面的

Entity entity("cherno");
就得改变成下面这样      类型不再是Entity，而是 Entity* 。

Entity* entity = new Entity("cherno");
用 new 来给 entity 赋值，在这里最大的区别不是看到的那个指针，而是这个 new 关键字。

当我们调用 new Entity 时，实际上发生的是我们在堆上分配了内存。它返回了这个 entity 在堆上 被分配的内存地址，这就是为什么要声明成 Entity* 类型。

左 栈 右（ new ) 堆

因为性能的原因，在堆上分配要比在栈上花费更长的时间。而且如果在堆上分配的话，那你必须要手动去释放分配的内存。所以如果我们使用了 new ，那么我们就要负责去释放这些内存，c++ 不会知道这个 Entity对象已经使用完了。

我们告诉它的方式就是调用 delete 方法，然后是变量名： delete entity

e = entity;
上面的这样已经可以把 & 去掉了，因为 entity 已经是个指针了。既然是指针，我们可以将它先解引用，然后调用 Getname ,或者使用  -> 运算符。例如下面这样

 std::cout << （*entity）.getname() << std::endl;


 std::cout << entity -> getname() << std::endl;  
当将 delete 放到cin下面时，将原本的 delete entity  改为 delete e    。

  Entity* e;
        {
            Entity* entity = new Entity("cherno");
            e = entity;
            std::cout << entity -> getname() << std::endl; 
        }


	std::cin.get();
	delete e;
上面就是我们要的样子，在堆上创建了一个 Entity对象，然后把它赋值给e，不会拷贝任何数据，实际上做的就是存储 entity 的内存地址。当赋值 entity 对象时，只复制了内存地址。

当我们编译时可以看到，直到运行到 cin 时，e 的 m_name 还是 cherno ,因为它只会在调用 delete 后才会被释放。

上面就是我们在 c++ 中创建对象的两种方式，如果对象非常非常大，或者你想显示的控制对象的生存周期，就在堆上创建。 如果不是这两种情况的话，那就在栈上分配，这更简单，会自动回收，当然也更快。而在堆上的话，就需要手动进行 delete 。切记，如果你忘记调用 delete，这会导致内存泄露。现在好多人用了 new 后忘记 使用 delete释放内存，以后可以用智能指针。

使用智能指针实际上还可以在堆上进行分配，而且仍然可以获得那种大小优势，而且就是当指针超出作用域时，对象会被自动删除。或者是那种共享指针，当没有指向他的引用时也会被自动删除。

总之就两种创建对象的方式：

堆和栈

如果可以首选使用      栈分配。

49.new关键字
实际上当你编写 C++程序的时候，你需要关心内存，性能和优化等问题。因为如果你不考虑这些的话，为什么要用 C++ 呢。

new的主要目的就是分配内存，具体来说就是在堆上分配内存。你先写上 new ，然后写数据类型，它可以是一个类，也可以是一个基本类型，又或者是一个数组。根据你写的类型，以字节为单位决定了要分配的内存大小。

比如你写 new int ,它会请求分配 四个字节的内存。一旦有了那个数字，它会请求操作系统，应该说是 C标准库，需要4个字节，请把它给我。现在需要找到一个包含4 字节的连续内存块。找到后就会返回一个指向那个内存地址的指针。 这样你就可以开始使用你的数据了，存储或者访问都可以，或者读写。

查找连续四字节内存，不是像激光扫描一样一段一段去找，有一个叫做空闲列表的东西，它会维护那些有空闲字节的地址。（没有你想的那么慢，但还是很慢）。

这就是主要内容，new 主要就是找到一个满足我们需求的足够大的内存块。然后给我们一个指向那个内存地址的指针。

下面举个例子，

class Entity
{
private:
	std::string m_name;
public:
	Entity() : m_name("unknown") {}
	Entity(const std::string& name) : m_name(name) {}

	const std::string& getname() const { return m_name;}
};
还是上面那个类，在主函数可以像平常那样直接

int a = 2;
也可以通过使用 new 关键字在堆上创建来选择动态分配内存。

int* b = new int;
上面就是一个在堆上分配的 4 字节 的整数。b 存储的就是它的内存地址。

如果我们想用new 关键字在堆上分配 Entity类，可以写成下面这样

Entity* e = new Entity;


Entity* e = new Entity（）;

上面的Entity后加不加括号都可以，不加括号是因为它有默认构造函数。如果喜爱那个要一个 Entity数组，可以加上方括号。

Entity* e = new Entity【50】;
如果是上面这样的数组，你会在内存中得到连续的 50 个 Entity。就像在栈上同一行连续分配了 50 个 Entity，不同点就是你是在堆上分配的。但是这个例子中的每个 Entity，并不是真的在另一个内存地址。你有一个包含 50 个 Entity的内存块，他们在同一排。

如果改成下面这样，他就在堆上只分配了夜光表单一的对象。

Entity* e = new Entity（）;
使用 new关键字，不仅是在堆上分配足够的内存来存储这个 Entity, 我们还调用了构造函数，这就是 new 关键字做的另一个重要的事情。不仅分配空间，还调用了构造函数。

new 只是一个操作符，就像加减乘除一样，这意味着我们可以重载这个操作符，并改变它的行为。

然后可以看到实际上只是一个函数， _Size是他分配的内存大小。返回的是一个 void 指针。但基本上void指针就是一个没有类型胡指针，指针就是一个内存地址。指针确实需要一个类型，他需要类型是为了你能够以想要的方式来操纵他。但核心的概念就是：指针就是一个内存地址，只是一个数字。所以它为什么需要一个特定的类型呢。

在这里，new 实际上做的事依赖于 C++库。所以如果你写自己的 C++编译器和库，理论上你可以让他做任何事。但是通常，调用 New 关键字，会调用底层的 C函数： malloc , 它是用来分配内存的。他的实际作用是传入一个size,也就是我们要多少个字节，然后返回一个 void 指针。

所以上面的代码就相当于我们写：

Entity* e = (Entity*)malloc(sizeof(Entity))
要转换成 Entity* 类型，C不用，但 C++需要上面两行代码的区别就是上面那行代码调用了 Entity的构造函数。而下面做的仅仅是分配了内存，然后返回给我们一个指向那个内存的指针，没有调用构造函数。

在 C++里，不应该像下面这样分配内存，所以上面的才是你应该选择的，用 new 别忘了使用 delete 。一旦分配了这些变量，像 b 或 e ,必须使用delete 。 delete只是一个常规函数，调用了C函数的 free ，free 可以释放 malloc 申请的内存。这一点很重要，因为当我们使用 new 时，内存还没被释放。它没有被标记为空闲，就不会被放回空闲列表，因此当我们调用 new 时，这些内存就不能再被分配了，知道我们调用 delete ,必须手动释放它。

有很多C++策略可以让这个过程自动化，比如基于作用域的指针，也有些高级的策略，比如引用计数。

int* b = new int【50】;
上面这里的 b 使用了 数组操作符来进行分配。请记住，这里的 new 也包含了数组操作符，和没有数组操作符是有区别的。如果我们使用了 new[], 那我们也应该使用 delete[] ，因为这个操作符就是这样的。

new 支持一种叫 placement new 的用法，决定了他的内存来自哪里。所以实际上并没有分配内存，只是调用了构造函数，并在一个特定的内存地址上初始化了你的 Entity 。

Entity* e = new(b) Entity();
上面这样就是基于 placement new 的用法，指定内存地址为 b 。

这里只是介绍下语法。

50.隐式构造函数和隐式转换以及 explicit关键字。
隐式的意思是不会明确的告诉你要做什么，有点像在某种情况下全自动工作。实际上 C++允许编译器对代码进行一次隐式的转换。比如将一种数据类型当作另一种类型使用，在这两种类型之间就会有类型转换，C++允许隐式的转换，不需要使用 cast 等做强制转换。

class Entity
{
private:
	std::string m_name;
	int m_age;
public:
	Entity(const std::string& name)
		:m_name(name),m_age(-1){}
	Entity(int age)
		: m_name("unknown"), m_age(age) {}

};
像上面这样设置一个 类，第一个构造函数应该给 age 赋值，但是将他设为  -1 ,意味着他是无效的，没有提供 age 。

Entity a("cherno");
Entity b(22);


Entity a = Entity("cherno");
Entity b = Entity(22);
上面两种和大部分人使用或者实例化对象方式一样，但是也可以用下面这样 直接设置（我的不会报错）（有人报错的是没有把数组转换到 entity 的 适当构造函数）。

Entity a = "cherno";
Entity b =  22;
上面不可以在其他语言中使用，好像有些奇怪，特别是第二行，类中有 name 字符串，还可以赋值为 22 ，其实这就是隐式转换或者隐式构造函数。他隐式的把 22转换成一个 Entity 对象，构造出一个  Entity 。因为 Entity 有一个构造函数，接受一个整形参数 age ，还有一个构造函数接受一个字符串参数 name:cherno 。

void printentity(const Entity& entity)
{

}

 


int main(void)
{
        printentity(22);
上面这样的代码是可以的，可以调用这个函数，传入 22。

我们并没有对上面这个函数进行重载，比如让他接受一个整形等。

void printentity(int age)
类似上面这样。

主函数直接调用函数直接传入 22 是调用上面的第二个构造函数把 22 作为唯一参数，就可以创建一个 Entity对象。

int main(void)
{
       printentity("cherno");
但是像上面这个，传入字符串则会报错，因为这个 " cherno" 字符串不是 std::string 类型的，是一个 char 数组。为了能让这个正常工作，C++需要做两次转换，一次是从 cosnt char 数组到 string ,然后是从string 转到 Entity，但是只允许进行一次隐式转换。

int main(void)
{
       printentity(std::string("cherno"));
为了可以正常进行，只能像上面这样包裹在string 的构造函数里或者使用 Entity包裹。

int main(void)
{
       printentity(Entity("cherno"));
但一般尽量减少使用，我们是用它来简化代码的，而不是总是用构造函数来包裹。

一般写成下面这样好点,更清晰，

Entity b(22);
如果你想用一个整数构造一个 Entity对象，那你必须显示的调用这个构造函数，explicit会禁用隐式转换，explicit关键字放在构造函数前面。如果你在构造函数前面加上 explicit ，就意味着这个构造函数不会进行隐式转换。如果你想用一个整数构造一个 entity 对象，那你就必须显示的调用这个构造函数。


explicit Entity(int age)
		: m_name("unknown"), m_age(age) {}

};
上面这样在构造函数前加上 explicit ，会发现主函数中的两种关于此构造函数的写法都会报错，需要显示的把它转化为一个 Entity 。 

Entity b = (Entity)22;
如果将字符串的构造函数也加上 explicit ，那么除了

  printentity(Entity("cherno"));
上面这个可以工作，其他的包含隐式转换的都不可以，因为这个实际上还是调用了 Entity 构造函数，这也是 explicit 关键字的唯一功能。它被用来当你想要显示的调用构造函数，而不是让 C++编译器隐式的把任何整形转换成 Entity。

有时可以在数学运算库的地方用到 explicit，因为可以把数字和向量来比较。

当你在写低级封装的时候，它可以派上用场，可以防止偶然转换和导致性能问题或者 BUG。

51.操作符及其重载
操作符基本上就是一种我们用来代替函数执行某些事情的符号。重载在这里是给他一个新的含义，或者增加参数，或者重新创建。它允许你在程序中定义或者更改一个操作符的行为，这是非常有用的。

当你定义了一个数学相关的类，然后你需要把两个数学对象相加，那么重载加号操作符就是顺理成章的做法。

class Vector
{
public:
	float x,y;

	Vector(float x,float y)
		: x(x) , y (y) {}

};

 

int main(void)
{ 
      Vector position(4.0f,4.0f);
	  Vector speed(0.5f,1.5f);

当上面这样，但是没用运算符重载应该怎么写，

Vector result = position.Add(speed);
例如上面这样，这时需要在上面定义Add函数，使其返回一个全新的 vector ，为了避免复制，通过  const 引用方式给他传入一个 Vector 。然后给他标记为 const ，这样就不会修改这个类，会创建一个新的 Vector 作为结果。

	Vector Add(const Vector& other) const 
	{
		return Vector(x + other.x,y + other.y);
	}
上面为在 类里面再建一个Add函数，然后函数里返回值，看起来可以工作。但是，如果我们要通过某种方式来改变 speed ,我们可能会有 powerup函数来使速度更快一些，大概是 10% 左右。

我们也可能想要 speed 成倍增加等，这样的话，我们可能要这么写 代码：

speed.Multiply(powerup)

 Vector result = position.Add(speed.Multiply(powerup);
在主函数后面接着写 上面的代码 。

还需要添加 Multiply 方法，把Add函数复制粘贴，并将Add改为 Multiply ,将加号改为乘号。(注意是再加一个，不是更改）

	Vector Multiply(const Vector& other) const 
	{
		return Vector(x * other.x, y * other.y);
	}
此时主函数显示如下图：

  Vector position(4.0f,4.0f);
	  Vector speed(0.5f,1.5f);
	  Vector powerup(1.1f ,1.1f);

	  Vector result = position.Add(speed.Multiply(powerup));
接下来就是在 C++中，我们可以通过重载操作符来处理 Vector 。

  Vector result = position + speed * powerup;
操作符重新定义和定义其他的函数是一样的，当然返回类型和上面 Add函数是一样的。

Vector operator+(const Vector& other) const 
	{
		return Add(other);
	}
先是定义+  ，我们不用写函数名，而是写 operator，然后是操作符，也就是 + ，然后是括号，传入需要的参数，最后再加上 const ,因为他和其他的函数一样，不会修改类。

上面这样就创建了 + 操作符，此时到主函数注释掉后面的代码是不会报错的。

  Vector result = position + speed;   //* powerup;
因为定义操作符 + 和其他函数一样，所以也可以反过来这么做。不是用操作符 + 来调用 Add函数，而是在 Add函数里调用 + 操作符。

Vector operator+(const Vector& other) const 
	{
		return *this + other;
	}
上面将它转变成 *this,它的解引用是什么？在这里它是一个 const 指针，解引用后就是一个普通的 Vector 对象，然后加上 other ，大多数人都是这样写的。

同理，定义 * 也是同样的方式。

Vector operator*(const Vector& other) const 
	{
		return Multiply(other);
	}
 Vector result = position.Add(speed.Multiply(powerup));


 Vector result = position + speed * powerup;
上面的主函数代码行，比较后可以发现，使用运算符重载后使用起来感觉好很多。

std::cout <<
上图是当我们使用 cout时，左边是　cout 类，右边是某种数据类型，所以可以输入 result ,然后再输入 std::endl;

但是现在不可以这么做，因为这个操作符还没有被重载。它接受两个参数，一个是输出流也就是 cout ，另一个是Vector, 我们就可以在 Vector类外面对它进行重载，因为他其实和 Vector没什么关系。

std::ostream& operator <<(std::ostream&　stream, const Vector& other)
上面先输入operator，然后是 << ,然后传入一个类，因为这是在类外面定义的操作符重载，所以还是需要一个对存在的流的引用，在这里就是 cout (指的是下面主函数里的 ),然后就是const Vector& other, 在这里输入 stream << 然后打印出other.x 。

最后得到结果为    4.55，5.56        。

操作符重载其实也就是函数。

最后介绍下 == 重载

bool operator == (const Vector& other) const
{
     return x == other.x && y == other.y;
}
这里使用 bool 是因为返回的是 true  or    false      。这里只是做了比较而不会修改类。函数内返回的只是检查这些浮点数是否相等。

然后在下面的主函数里 输入 

if( reault1 == result2)
{

}
类似这样。

然后如果还想要一个不等于的操作符，只要把这个代码改为 ！=， 然后返回 == 操作符的取反就可以了。这种写法更好点。

bool operator != (const Vector& other) const
{
     return !(*this == other);
}
把 *this == other 放在括号里，然后前面加上 ！ ，计算相等的相反结果。

或者像作者之前那么写的那样，但是这样看起来会有点奇怪，不太推荐，下面这样

return !operator == (other);
在函数里返回这样。

作者写库的时候喜欢一起使用，会有 Add函数，也会有 + 操作符。这样就可以提供给使用你 API 的人自己选择了。

52.this 关键字
通过 this 我们可以访问成员函数，就是属于某个类的函数或方法。在函数内部，我们就可以引用this , this 就是指向这个函数所属的当前对象实例的指针。

所以我们写一个非静态的方法，为了去调用这个方法，我们需要先实例化一个对象，然后再去调用这个方法，所以这个方法必须由一个有效对象来调用，而 this 关键字就是指向那个对象的指针。

class Entity
{
public:
	int x,y;

	Entity(int x, int y)
	{
	 
	}
};
先创建一个 Entity 类，然后如果想用这些参数的值给那些成员变量赋值，当然可以用成员初始化的方式来进行赋值，都没什么问题，如下图一样

Entity(int x, int y)
         ：x(x) , y(y)
{

}
如果想在内部赋值，那可能会有些麻烦，因为参数的名字 是一样的。

x = x;
如果像上面这样写，那么就相当于一个参数自己给自己赋值，真正想做的是引用属于这个类的 x 和 y ，这个类的成员。

此时使用 this 就可以帮助我们实现，this 是指向当前对象的 指针，

Entity(int x, int y)
	{
		Entity* e = this;
	}
Entity* 就是 this 的类型，鼠标放在 this 上可以看到是 Entity* const this ,

大多数人都不会在等式左边写成，

Entity* const e = this;
一般不会写成上面这样，因为加上 const 的话，意味着 右边的 this 不允许重新给他赋值。

Entity(int x, int y)
	{
		Entity* e = this;
        
        e -> x = x;
    }
我们想要给 Ｘ　赋值，那么只要写ｅ->x,然后用 x 给他赋值

Entity(int x, int y)
	{
		this -> x = x;
        this -> y = y;
	}
再简单点就可以写成上面这样，this是个指针，所以要对他解引用，要使用箭头符号。

接下来如果我们要写一个返回其中一个变量的函数的话，在函数后面加上 const 是很常见的，因为他不会修改这个 class ， 

在这个函数里，我们不可以使用 Entity* e = this, 而应该使用 const Entity ，因为上面的函数后面加上 const 就意味着我们不能修改这个类，所以this指针必须是 const 的，

int Getx() const
	{
		const Entity* e = this;

		return x;
	}
因为函数名称后面加上了 const ，所以就不可以像上面那样使用 this 指针。

另一个用到的场景就是如果我们想要调用这个Entity类外面的函数，他不是 Entity的方法，但我们想在这个类内部调用一个外部的函数，然后这个函数接收一个 Entity类型作为参数，

void printentity(entity* e);
在类上面声明函数，然后在类下面进行函数定义，

void printentity(entity* e)
{

}
我们希望在这个类里面调用 print entity，传递这个Entity类的当前实例到这个函数里，可以传入 this，这就会传入已经设置了 x 和 y 的当前实例。如果想传入一个常量引用，要做的就是在这里进行解引用 this 。像下面这样，

Entity(int x, int y)
	{
		this -> x = x;
        this -> y = y;
      
        printentity(*this);
     
    }
在非 cosnt 函数值通过解引用 this ，我们就可以赋值给 Entity&，像下面这样

Entity(int x, int y)
	{
		this -> x = x;
        this -> y = y;
        
        Entity& e = *this;
      
        printentity(*this);
     
    }
如果是在const 方法中，我们会得到一个 const 引用，

int Getx() const
	{
		const Entity& e = *this;
		
	}
因为这是一个指向当前类的指针，我们可以做一些非常离奇的操作。

Entity(int x, int y)
	{
		this -> x = x;
        this -> y = y;
        
        Entity& e = *this;
      
        printentity(*this);
     
        delete this;
     
    }
比如在类里加上 delete this ，不过尽量避免这样做，因为这样正在从成员函数里释放内存。在这之后，你去访问任何成员数据都会失败，因为内存已经被释放掉了。所以不要这样写。

53.对象的生存周期
栈作用域的生存周期：

基本上来说，栈就是一种你可以在他的顶部添加东西的数据结构，在 c++ 中，每次我们进入一个作用域时，我们就是在 push栈帧，也不一定就是一个栈帧。

当在push 数据时，就好像将一本书放在书堆上，在这个作用域声明的变量，就相当于你在这本书里写的内容。一旦这个作用域结束，你就把这本书从书堆里拿出来，每个基于栈的变量，就是你在那本书里创建的对象就都结束了。

class Entity
{
     private:
          int x;
};
上面在栈上初始化的变量，就是不是在堆上分配的变量，这个变量在这个类作用域里，也就是说当这个类销毁，这个变量也会销毁。

class Entity
{
     private:
          int x;

     public:
    	 Entity()
    	 {
    		 std::cout <<" created entity!" << std::endl;
    	 }
    	 ~Entity()
    	 {
    		 std::cout <<" destroyed Entity!" << std::endl;
    	 }
};


int main(void)
{ 
	{
		Entity e;
	}
上面设置了一个构造函数，一个析构函数，为了不在堆上创建，在主函数作用域声明Entity，这样写会在栈上创建，会调用默认的构造函数。

在代码行设置断点，编译到 最后的  } 时，构造函数会输出 “ created entity！” ，按 f10 向下编译，已经超过作用域结尾，销毁 Entity ，很明显内存也就被释放了。

如果我们想要在堆上对他进行分配，把这个转换为指针，

{
		Entity* e = new Entity();
}
这时再次打断点编译，直到超出作用域，到达下面的 cin.get(),也不会输出 ” destroyed entity!"  ,entity永远不会销毁，当然，当程序终止的时候，操作系统会清理这些内存。

int* CreateArrey()
{
	int array[50];
	return array;
}
上方这个函数实际上是不行的，虽然看起来没什么问题。

像这样创建一个数组，因为我们没有使用 new 关键字，所以他不是在堆上分配的，只是在栈上分配了他，当我们返回一个指向它的指针时，也就是返回一个指向栈内存的指针，所以一旦离开了这个作用域，这个栈内存就会被回收，

int main(void)
{
      int* a = CreateArray();
如果在主函数里调用该函数，则会出错误。

所以，要写这样的代码一般有两种选择，

一种是在堆上分配这个数组，这样他就会一直存在，像下面这样

int* array = new int[50];
或者还可以把这里创建的数组赋值给一个在这个作用域外的变量

int main(void)
{
      int array[50];
      int* a = CreateArray(array);
在这里创建一个大小为 50 的数组，然后把这个数组作为一个参数传给这个函数。当然上面create array函数里就不需要再创建数组了

void CreateArray(int* array)
{

 

}
因为只是闯入了一个指针，所以不会做分配的操作。

一定记住在局部作用域创建数组是一个经典的错误，不可以创建一个基于栈的变量，然后返回指向它的指针。

可以利用类的作用域来实现像是智能指针 smart_ptr，或是 unique_ptr 

最简单的就是作用域指针了，本质上就是一个类，是一个指针的包装器，在构造时在堆上分配指针，然后析构时删除指针，

{
		Entity* e = new Entity();
}
像是上面这个，Entity,我还是想在堆上分配它，想用 new 关键字创建它，但是想要实现在跳出作用域时自动删除它。这时可以使用标准库中的 unique_ptr，这是夜光表作用域指针，

class ScopedPtr
{
private:
	Entity* m_ptr;
public:
	ScopedPtr(Entity* ptr)
		: m_ptr(ptr)
	{

	}
	 
	~ScopedPtr()
	{
		delete m_ptr;
	}

};
上面写一个ScopedPtr类，只有一个Entity指针m_ptr，在这里把它赋值给 m_ptr,在构造函数中接受一个 Entity 指针，在这里把它赋值给 m_ptr,在析构函数中，只是像这样调用delete删除m_ptr。

在下面我们看怎么调用它，这里我们不适合用 new 来创建 Entity，而是ScopedPtr。

{
		ScopedPtr e = new Entity();

		Entity* e = new Entity();
}
当然上面的也可以这样写

ScopedPtr e(new Entity());
上面这样写的话就会默认使用构造函数，当然像之前这样写是为了和之前保持一致，看起来差不多，当然这里是隐式转换，但是这两个虽然看起来差不多，不同的是上面的一旦超过这个作用域，就会被销毁。

因为这个 Scoptr类的对象是在栈上分配的，也就是说当 e 被销毁时，调用析构函数，这里的Entity指针也会被删除。

private:
	Entity* m_ptr;


 在主函数打上断点，然后编译，再按 F10向下编译，结果如上图所示，可以看到控制台打印了  构造函数的  created Entity!  ，再向下编译一行，析构函数打印出 destroyed Entity!，是使用 new 在堆上分配的。之后会教使用智能指针，比使用 new 要更好些。所以这种可以自动构造，自动析构，离开作用域之后就自动销毁的栈变量是非常有用的。

例如一个计时器，加入你想计算在你基准测试范围内的时间，可以写一个 timer 类，在对象创建构造时开始计时，然后在对象销毁时停止计时并且打印出计时。你只要在函数开头加上一行代码，那这个整个作用域就会被计时。不需要手动去停止，因为一旦超过作用域，就会自动停止。

如果你想要给函数加锁，一般多线程访问它的时候不会出错，可以写一个自动的作用域锁，在函数开始时锁定他，然后再结束时解锁，当然很快就到多线程了。

54.智能指针
指针意味着当你调用 new时，不用调用 delete,而实际上有了智能指针，我们甚至不必调用新指针，而且很多人倾向于具有这种编程风格并且尽可能安全。智能指针本质上是真正的原始指针的包装。

当你创建一个智能指针，它会调用 new 并分配您的内存，然后基于您使用该内存的智能指针。这些内存会在某一时刻自动释放。

第一种，最简单的智能边框（unique_ptr)，它是作用域指针，意味着当该指针超出范围时，将被销毁，然后调用 delete。他们必须是唯一的，你不能复制一个unique_ptr指针，因为如果你复制一个unique_ptr指针，你将有两个指针，两个唯一指向同一块内存。而当其中一个指针死亡时，将释放那块内存，这意味着你第二个unique_ptr指向已释放的内存。所以你无法复制唯一的指针。unique_ptr是你想要一个作用域指针时，他是你唯一的参考。

下面例举一个例子：

记得要使用智能指针，包括内存，要加头文件，

#include <memory>
现在在上一节的代码基础上，有个 Entity 类，他只是一个构造函数和析构函数，这样我们就了解了这些智能指针的行为。

主函数创建一个新作用域，是一个空作用域（两个大括号），使用unique_ptr分配Entity。后面<>内输入一个模板参数Entity，然后起个名字，叫做 entity，然后可以调用这里的构造函数 然后输入 new Entity() ，

int main(void)
{ 
	{
		std::unique_ptr<Entity> entity = new Entity();


	}
这样写实际上是不可以的，因为这样的话，看下unique_ptr可以发现构造函数实际上是显式的，意味着必须显式的调用构造函数，没有隐式的转换，

int main(void)
{ 
	{
		std::unique_ptr<Entity> entity( new Entity());


	}
上面这样用法就是 unique_ptr的一种方式，然后就可以访问它，如果想在这里调用一个函数，这里还没有任何函数，


	{
		std::unique_ptr<Entity> entity( new Entity());
	 
	    entity-> Print();
	}
记得在Entity类里面创建个 Print（）方法，主函数里面就可以直接用箭头操作符访问它。

	{
		std::unique_ptr<Entity> entity = std::make_unique<Entity>();
	 
	    entity-> Print();
	}
上面这种是更好的方法，把 entity赋值给 std::make_unique ，这对于 unique_ptr很重要，主要原因是出于异常安全，最好的方式是调用 make_unique，但是 make_unique<>()是在 C++14引入的，c++11并不支持。因为如果构造函数碰巧抛出异常，他会稍微安全一点。你最终并不会得到一个没有引用的悬空指针，从而造成内存泄露。无论无何，一旦我们得到了这个 unique_ptr,我们就可以调用任何我们想要的方法。

所以打上断点编译上面的代码时，我们可以看到，在没跳出作用域时，会打印出 Created Entity!,而当跳出作用域后，则会显示Destroyed。

这就是最简单的智能指针，是非常有用的而且低开销的，他只是一个栈分配对象，当栈分配对象死亡时，他将调用 delete 在你的指针上，并释放内存，使得这个指针，可以被传递到一个函数中，或者另一个类中，但是你不能复制它。

	{
		std::unique_ptr<Entity> entity = std::make_unique<Entity>();
	    std::unique_ptr<Entity> e0 = entity;

 

	    entity-> Print();
	}
如果要尝试在这里做另一个 unique_ptr 叫做 e0,或者类似的东西，赋值为 Entity。但是是不可以的，到定义会发现，拷贝构造函数和拷贝构造操作符实际上已经被删除了。这就是为什么你会得到一个编译错误。是专门为了防止你自挖坟墓的。因为如果复制后，只要一个 unique_ptr死亡，那么所有都要死，因为这个堆分配对象的底层内存会被释放。

第二种，共享指针 shared_ptr , 它有些不同，他更牛逼一点，因为他还在底层做了很多事情。shared_ptr实现的方式实际上取决于编译器和你在编译器中使用的标准库，然而在我所见过的所有系统中，它使用的是引用计数。也就是工作方式是通过引用计数，引用计数基本上是一种方法，可以跟踪你的指针有多少个引用，一旦引用技术达到零，就会被删除。

举个例子，创建一个指针 shared_ptr，再创建另一个shared_ptr来复制他，那么引用计数就是2，第一个和第二个，一共 2 个。当第一个死的时候，引用计数器现在减少 1 ，然后当最后一个 shared_ptr死的时候，引用计数回到 零，内存被释放，

	{
		std::unique_ptr<Entity> entity = std::make_unique<Entity>();
	  
	    std::shared_ptr<Entity> sharedEntity = std::make_shared<Entity>();
	    
	    std::shared_ptr<Entity> sharedEntity(new Entity());

 

	    entity-> Print();
	}
上面写了一种写法，第二种其实是不可以的。先在<> 内写入模板参数，Entity，然后名称为 sharedEntity ，最后加上后面的内容。

在 unique_ptr中，不直接调用 new 的原因是异常安全，但在shared_ptr中不同，它需要分配另一块内存，叫做控制块，用来存储引用计数。如果你首先创建一个 new Entity ,然后将其传递给 shared_ptr构造函数，他必须分配，做两次内存分配，先做一次 new Entity的分配，然后是 shared_ptr的控制内存块的分配。然后如果你用shared_ptr,你能把他们组合起来。这样更有效率，而且对于那些讨厌 new 和 delete 的人，显然会从你的代码库中删除 new 关键字。因为他们会使用std::make_shared 而不是 new Entity，我们会更喜欢使用 make_shared，所以有了共享指针 shared_ptr，当然也可以复制，只要代码是正确的是可以正常工作的。

	{
		std::unique_ptr<Entity> entity = std::make_unique<Entity>();
	 
	    std::shared_ptr<Entity> sharedEntity = std::make_shared<Entity>();
	    
	    std::shared_ptr<Entity> e0 = sharedEntity;

 

	    entity-> Print();
	}
我们也可以将复制移到外面，设置两个作用域，就像下面这样

{	
       std::shared_ptr<Entity> e0 = sharedEntity;


    {
    	std::unique_ptr<Entity> entity = std::make_unique<Entity>();
     
        std::shared_ptr<Entity> sharedEntity = std::make_shared<Entity>();


​        

	    entity-> Print();
	}

}
设置两个作用域，在第一个作用域中，有了 e0 ，在第二个作用域中，有了 sharedEntity,要将有e0赋值给 sharedEntity,将无关的 unique_ptr删掉，然后写成下面这样，

{	
       std::shared_ptr<Entity> e0;


    {
     
        std::shared_ptr<Entity> sharedEntity = std::make_shared<Entity>()；
     
        e0 = sharedEntity;
    }

}
当我们编译时，第一件要做的事就是创建 Entity，然后分配下面的赋值语句，当第一个作用域死亡时，这个sharedEntity死掉了。然而，尽管已经超出第二个作用域，但是析构函数没有打印 destroyed Entity，因为 e0 任然是活着的，并且持有对该 Entity的引用，

而当我们编译到超过第一个作用域时，也就是所有作用域时，这时候析构函数将打印 Destroyed Entity! 。

当所有的引用都消失了，当所有的栈分配对象，追踪shared_ptr的，在他们从内存释放后，所有都死亡后，那就是你的底层 Entity 被删除的时候。

最后，还有一个东西可以和shared_ptr一起使用，叫做弱指针 weak_ptr，他只是像声明其他东西一样声明，可以给他赋值为 sharedEntity，

{	
       std::shared_ptr<Entity> e0;


    {
     
        std::shared_ptr<Entity> sharedEntity = std::make_shared<Entity>()；
     
        std::weak_ptr<Entity> weakEntity = sharedEntity;
     
        e0 = sharedEntity;
    }

}
就像上面这样，这里所做的和之前复制 sharedEntity所做的一样，但之前会增加引用计数，而这里的则不会。他不会增加引用计数，当你将一个 shared_ptr赋值给另外一个 shared_ptr，他会增加引用计数，但是当你把一个 shared_ptr赋值给一个 weak_ptr时，不会增加引用计数。

实际可以理解为 weak_ptr可以被复制，但是同时不会增加额外的控制块来控制计数，仅声明这个指针还活着。

你可能会问关于 weak_ptr，底层的对象还活着，但是他不会让让他保持存活，因为实际上不会增加引用计数， 如果把shared_ptr换成一个 weak_ptr，然后重复之前的事，

	{
	   std::weak_ptr<Entity> e0;
	 
	{
	    std::shared_ptr<Entity> sharedEntity = std::make_shared<Entity>();
	 
	    e0 = sharedEntity;
	}
打上断点，编译到第二个作用域第一行时，构造函数会打印出 Created Entity!   ，继续向下编译，当超过第一个作用域时，就是它被摧毁时，所以现在这个 weak_ptr 指向一个无效的 Entity .

你可以一直使使用他们，他们会让你的内存管理自动化，防止你因为忘记调用 delete，而意外的泄露内存。

shared_ptr是有一点花费的，因为他的引用计数系统，但是话又说回来，许多倾向于编写自己内存管理系统的人，也一样会有一些开销。

现在来说这是一件很微妙的话题，因为现在C++新一代程序只使用这些功能，但还是很多人使用 new 和 delete ，当然可以两者都有，因为总有一个时间你可能想用 unique_ptr和 shared_ptr，但也需要 new 和 delete 。 所以现在还不能说智能指针已经完全取代了 new 和 delete.

当你要声明一个堆分配的对象，并且你并不希望自己来清理，因为你不想显式的调用 delete ，或者显式的管理内存时，你就应该使用智能指针，尽量使用 unique_ptr，因为他有一个较低的开销，但是如果你需要在对象之间共享，不能使用 unique_ptr 的时候，就可以使用shared_ptr， 但是要按这个顺序，有限选择 unique——ptr,然后选择shared_ptr。

55.C++的复制与拷贝构造函数
拷贝指的是要求复制数据，复制内存。当我们想要把一个对象或者原语或一段数据从一个地方复制到另一个地方时，我们实际上有两个副本，大多数时候，我们想要复制对象，以某种方式修改他们，但是，如果我们可以避免复制，可以避免不必要的复制，当我们只想读取的时候，或者是想修改一个已经存在的对象。我们当然想（不要复制） ，因为复制需要时间。

在下面例举一个例子介绍复制如何产生需要的效果，以及当我们不想复制的时候，我们可以做些什么来移除复制。以及我们需要添加复制，如何正确的复制。

int main(void)
{
	int a = 2;
	
	int b = a;
上面创建两个基本类型，a和 b，然后，把 a 赋值给 b．实际上做的是在创建一个　他的　副本，所以　ａ　和　ｂ　是两个独立的变量，他们有不同的内存地址。由于这个原因，当下面将　３　赋值给　ｂ　时，ａ还是２。

ｂ　＝　３；
内存中有两个不同的值。

在类中也是相似的情况，可能是一个两个分量的向量，浮点 x 和 浮点 y 。

创建一个 Vector类。然后设置两个 public 浮点型 x , y  。在主函数设置 vector 并将他 =  a ， 然后创建 b 的 x 为 5 。

	Vector a = {2,3};
	Vector b = a;
	b.x = 5;
将代码这样编写，会发现 a.x 的值仍然是 2，因为复制的是值，将 a的值给了 b ，就像上面那个整数例子一样，他们是两个独立的变量。它们占用了两个不同的内存地址。

如果你要在堆中使用 new 关键字进行分配，所以将 a 写成这样。

Vector* a = new Vector();
Vector* b = a;
这里需要定义一个构造函数。现在 vector是一个指针，所以说第二行代码中，vector* b = a;

没有复制实际的向量，这个实际向量包含了  x 和 y 变量 。实际上现在有两个指针，他们本质上有相同的值。如果在下面添加一个 b++,此时 a 指针仍然是完整的。

b++;
但是如果我访问这个内存地址，设置为某个值，在这种情况下实惠同时影响 a 和 b 。

b -> x = 2;
是的，编译时上面那种写法 &a 和 &b 是两个不同的地址，下面这种写法则是显示同一个地址。

这样的话就不是影响指针，而是影响内存地址。

当使用赋值运算符时，将一个变量设置为另一个变量时，你总是（总是标记 * 号），你总是在复制值。为什么 总是要打上引号，因为还有 引用，如果你复制一个引用，你实际上是在改变指向，因为引用只是别名（并没有复制），所以引用除外。

每当你编写一个变量被赋值另一个变量的代码时，你总是在复制，在指针的情况下，你在复制指针，也就是内存地址，也就是那串地址数字，而不是指针指向的实际内存。

写一个字符串类，使用非常原始的方式，不使用现代化的代码方法。

首先放置一个字符数组，

char* m_Buffer;
这将指向我的字符缓冲区，然后 m_Size来保存 string大小。

unsigned int m_Size;
在public 中创建一个构造函数，参数是 const char* string .

然后要做的第一件事就是计算这个字符串多长，把字符串数据复制到缓冲区中，使用一个C 函数，也就是 strlen 或者叫 string length ，来得到这个 string 大小。

便知道了缓冲区 buffer 大小，等于这个 string大小，new char[m_Size],

m_Buffer = new char[m_Size];
实际上，上面这行在【】里还需要在最后加上一个空终止符，也就是最后还需要 +1,但是先不写看看。

下面就是把这个指针复制到实际的缓冲区，这样缓冲区就会被我们字符填充。

for (int i = 0;i < m_Size;i++)
			m_Buffer[i] = string[i];
可以使用for循环，遍历每个字符，然后一个一个复制，当然，更简洁的方法就是使用 memcpy ，注意三个参数顺序是：目的、来源和大小。

memcpy(m_Buffer,string,m_Size);
最后的大小，每个字符是一个字节的内存，记得我们之前故意少一个空终止符，现在写可以打印字符串的东西，用它来打印字符串。

想要用cout 输出，需要重载左移操作符，（这里我的12版本 在 operator异常）

std::ostream& operator <<(std::ostream& stream,const String& string)
	{
		stream << string.GetBuffer();
		return stream;

	}
或者不使用 GetBuffer（），将第一行代码拷贝到上面的类里，然后设置为友元（friend）。

friend std::ostream& operator <<(std::ostream& stream,const String& string);
std::ostream& operator <<(std::ostream& stream,const String& string)
	{
		stream << string.m_Buffer();
		return stream;

	}
这样的话，那我们可以写成下面这样，可以看到正在访问一个私有成员，当然如果没有上面的友元函数，就不可以，因为 m_Buffer是string 类的私有成员，

String string = "cherno";
	std::cout <<string << std::endl;
主函数编写上面代码行，然后运行（我的12版本在重载那边的 string.m_Buffer(),会在 string异常，如果正常输出的话，结果就是 cherno 加上一群随机字符，因为这里我们没有加空终止符。

所以先去上面将空终止符加上，也就是在后面加一。

m_Buffer = new char[m_Size + 1];
也可以使用 strcpy函数（拷贝时，包含了空终止符）

为了简单，

memcpy(m_Buffer,string,m_Size + 1);
他几乎复制了这个长度的字符串（ m_Size + 1) 的字符串。再运行就可以正常打印出 cherno。

但这是在这个假设的基础上，假设这个字符串，这个char* ,正常通过空终止符结束，如果不能保证的话，可以手动在后面加上自己的空终止符，然后按 F5运行。

m_Buffer[m_Size] = 0;
这就是最基本的String类设置，可以看到copying内容。

public:
	String(const char* string)
	{
		m_Size = strlen(string);
		m_Buffer = new char[m_Size];
		
		memcpy(m_Buffer,string,m_Size);
	 
		m_Buffer[m_Size] = 0;
	}
上面还存在一个内存泄露，第二行 new char 那边，我们没有使用 delete，当然如果使用 智能指针或者 vector ，就不需要 delete,但是，因为我们使用 New 关键字，并且分配原始数组，所以需要使用析构函数来delete，

	~String()
	{
		delete [] m_Buffer;
	}
运行一切正常，


	String string = "cherno";
	String second = string;

 

	std::cout <<string << std::endl;
	std::cout <<second << std::endl;
如果创建第二个字符串，并将 string字符串复制给他，然后输出他的内容，就会得到两个 cherno,然后敲击回车，却会发现代码出现问题，，它崩溃了，如果你看看调用堆栈（call stack) .

至于为什么会这样，当我们复制string时，c++自动为我们做的是它将所有类成员变量，

private:
	char* m_Buffer;
	unsigned int m_Size;
而这些（成员变量）组成了类（实例的内存空间），是由一个 char* 以及一个 unsigned int  组成，

他将这些值复制到一个新的内存地址里面，（新的内存地址）包含了second 字符串。（浅拷贝）。

现在问题是主函数有两个 string ，因为他们直接进行了复制，这种复制被称为 “   浅拷贝   ”  ，所做的就是复制这个指针，内存中的这两个 String(对象），有相同的 char* 的值。换句话说，相同的内存地址，这个 m_Buffer的内存地址，对于这两个 String(对象） 来说是相同的，程序会崩溃。是因为当我们到达作用域的尽头时，这两个 String 都被销毁了。析构函数会被调用，然后执行 delete [] m_Buffer 两次，程序试图两次释放同一个内存块（析构函数 内），这就是为什么程序会崩溃，因为内存已经释放了。已经不是我们的了，我们已经不可以再释放了。

或者设置断点，可以看到 string 和 second 指针地址是一样的，如果我们想要修改second ，不完全修改，只是将字母 e 改为 字母 a ,变成 Cherno 。

我们范文第二个索引，并把它赋值 a ，

second[2] = ' a ';
写在主函数两个 string 后，当然为了让这个 【】操作符起作用，需要写下（操作符重载），然后写返回值 char& ,然后是 operator，然后是索引操作符【】，unsigned int index（作为参数），不做安全检查来确保我们在范围内，只是返回 m_Buffer[index] ,这样会干净简洁点。

char& operator[] (unsigned int index)
	{
		return m_Buffer[index];
	}
放在 friend 友元函数，析构函数中间，

此时我们还是继续运行，然后结构函数 打印出两个  “ cherno ” ，然后依旧会崩溃。

看起来是复制了，其实还没有，真正要做的是，分配一个新的 char 数组，来存储复制的字符串，而我们现在做的是复制指针，这两个字符串对象指向完全相同的内存缓冲区，当我们同时改变了他们，因为它们指向同一个内存块，或当我们删除一个时，它会把他们两个都删除，还是因为指向同一个内存块。我们想要复制内存块，希望第二个字符串拥有自己的指针，以拥有自己唯一的内存块。这样当我们修改第一个内存块时，就不会触及第一个字符串。

我们能做到这一点的方法，是执行一种叫做深度复制（深拷贝）的东西 。 也就是说我们实际上复制了整个对象，不是我们在上面看到的那种 浅拷贝。比如这个对象是由什么组成的，因为我们看对象的内存，只是一个指针和一个 int ，但是浅拷贝不会去到指针的内容或者指针所指向的地方，也不会去复制它。

深拷贝根据定义复制整个对象。我们可以写出克隆，比如方法或函数，或者类似的东西，然后让他返回一个新字符串。（一般不太用）。

我们使用的是 拷贝构造函数。拷贝构造函数是一个构造函数，当你复制第二个字符串时，它会被调用。当你把一个字符串赋值给一个对象时，（这个对象）也是一个字符串，当你创建一个新的变量并给它分配另一个变量时，它（这个变量）和你正在创建的变量有相同的类型。你拷贝这个变量，也就是所谓的拷贝构造函数。

拷贝构造函数的函数签名，对同样的类对象的常引用 const & ,然后可以叫他 other 。C++会自动为你提供一个拷贝构造函数。他所做的就是  内存复制，将 other 对象的内存，浅层拷贝进这些成员变量。

String(const String& other)
		:m_Buffer(other.m_Buffer),m_Size(other.m_Size)
	{

	}
这是 C++默认提供的构造函数。这样做不行，因为我们不仅仅想复制指针，我们想复制指针所指向的内存。

如果我们决定不需要拷贝构造函数，不允许复制。我们就可以将这个构造函数声明为 delete 。

String(const String& other) = delete;
上面这样写的话，那么主函数里面的复制代码就不可以正常编译。

String second = string;
这就是 unique_ptr所做的，也就是智能指针。这就是我们禁用它（拷贝构造函数） 的方法，找到自己的拷贝构造函数，

接下来要做的就是 复制 m_Size,这是一个整数，可以做浅拷贝，然后在两个{ } 括号中间分配一个新的缓冲区，大小就是 m_Size,已经在上面赋值了，不过要加上 1 。我们要从 other 字符串对象复制，所以真正需要做的就是复制 other 的缓冲区，这里改为 other.m_Buffer,赋值给这个字符串对象的 m_Buffer,

String(const String& other)
		: m_Size(other.m_Size)
	{
		m_Buffer = new char[m_Size + 1];
		memcpy(m_Buffer,other.m_Buffer,m_Size + 1);
	}
知道 other 的大小，other字符串 可以这样写。已经有了一个空终止符，因为它是一个字符串，必须有空终止符。上面这样就是进行深拷贝所使用的代码，

void PrintString(String string)
{
	std::cout << string << std::endl;
}
创建一个 Print String函数，传入一个字符串，输出字符串，

    PrintString(string);
    PrintString(second);
然后还是打印出两个 cherno ，但是我们实际上不必要复制这个，到构造函数这边，

String(const String& other)
		: m_Size(other.m_Size)
	{
		std::cout << "Copied String!";
就像上面这样添加一行代码，

String second = string;
然后看主函数中这行代码，看起来只复制了一次，实际上还做了额外2 次复制操作。得到的结果是

Copied String！

Copied String！

cherno

Copied String！

cherno

可以看到有三个 string 的复制，但是我们不需要这么多。

当我们每次复制一个字符串时，我们在堆上分配内存，

     m_Buffer = new char[m_Size + 1];
     memcpy(m_Buffer,other.m_Buffer,m_Size + 1);
复制所有内存，最后我们释放内存。我们真正想做的是，将所有的字符串直接进入这个 PrintString 函数，因为我们知道我们不需要复制它，不需要它的另一个副本，我们可以直接引用所有的string，方法就是直接传引用，

void PrintString(String& string)
{
	std::cout << string << std::endl;
}
我们可以直接引用现有的 string，这个类（String类）实际上不会修改字符串，标记为 const  引用。

void PrintString(String& string)
{
    string[2] = 'a';
	std::cout << string << std::endl;
}
如果没有 const ，就可以编辑现有的字符串。除此之外，也意味着我们可以将临时的右值，传递到实际的函数中，如果我们改变这个函数签名，接受一个字符串的 const 引用，而不仅仅是一个字符串，

class String
{
private:
	char* m_Buffer;
	unsigned int m_Size;
public:
	String(const char* string)
	{
		m_Size = strlen(string);
		m_Buffer = new char[m_Size];
		
		memcpy(m_Buffer,string,m_Size);
	 
		m_Buffer[m_Size] = 0;
	}
	 
	String(const String& other)
		: m_Size(other.m_Size)
	{
		std::cout << "Copied String!";
	 
		m_Buffer = new char[m_Size + 1];
		memcpy(m_Buffer,other.m_Buffer,m_Size + 1);
	}
	 
	~String()
	{
		delete [] m_Buffer;
	}
	 
	char& operator[] (unsigned int index)
	{
		return m_Buffer[index];
	}
	 
	friend std::ostream& operator <<(std::ostream& stream,const String& string);

 

};

std::ostream& operator <<(std::ostream& stream,const String& string)
	{
		stream << string.m_Buffer();
		return stream;

	}

void PrintString(const String& string)
{
	std::cout << string << std::endl;
}

 

int main(void)
{
	String string = "cherno";
	String second = string;


    second[2] = 'a';
     
    PrintString(string);
    PrintString(second);
     
    std::cin.get();
    system("pause");
     
    return 0;
}
输出为

Copied String！

cherno

cherno

如果在 PrintString函数中突然决定，还是要复制，

只需要在该函数里输入正确的代码

Void PrintString(const String& string)
{
       String copuy = string;
总是要通过 const 引用去传递对象，在某种情况下，复制可能更快，但在基础使用中，用 const引用更好。因为写的函数本身你可以决定是否要复制，在函数的内部，但是没有理由到处复制，会拖慢你的程序。不管字符串是你自己的 String 类，还是标准库里面的 String，总是通过 const 引用传递。

56.箭头操作符
讨论箭头运算符对结构体和类的指针可以做什么，然后实现自己运算符重载，

代码是一个基本的 Entity类，在主函数创建这个对象，然后调用Print。

class Entity
{
public:
	void Print() const { std::cout << "Hello!" << std::endl; }

};

int main()
{
	Entity e;
	e.Print();
当这个Entity对象是指针的话，要么是在堆上分配，要么是有一个指向它的指针，为了调用 Print函数，

Entity* ptr = &e;
因为是指针，不可以使用 . 什么的写法（指针是一个数值，不是对象，怎么能调用方法），所以不能像下面这样使用

ptr.Print();
需要的是逆向引用（*ptr),可以像下面这样，用ptr前面的 *号来逆向引用它，然后用entity来替换这个，像下面这样

Entity* ptr = &e;
	Entity& entity = *ptr;
	entity.Print();
还可以用指针，将前面用圆括号括起来，然后逆向引用，

（*ptr).Print();
如果前面不加括号是不可以的，因为运算符优先级，会尝试先调用 Print ,然后再逆向引用Print之后的结果。必须先逆向引用，然后调用 Print，

Entity* ptr = &e;
	
	ptr -> Print();
可以使用一个箭头替换所有这些，像这样打印出来，实际上就相当于逆向引用了 Entity 指针，然后调用 Print() .

这是一个快捷方式，本来需要手动去逆向引用你，用圆括号把他们括起来，然后调用函数或者变量，现在一个箭头就可以。

变量也可以

public:
      int x;


Entity* ptr = &e;
	
	ptr -> x = 2;
 基本上使用箭头访问 x ，然后将它设置为想要的任何值，就是箭头操作符的默认用法了。百分九十以上是这么用。作为一个c++ 操作符，可以重载它，在自己的自定义类中使用。

写一个 ScopedPtr 类，

当构造函数时，取一个 Entity作为参数，然后将他分配给 m_obj，在析构中，delete m_obj,现在当我的Entity超出范围时，这个类会自动删除。   

class ScopedPtr
{
private:
	Entity* m_Obj;
public:
	ScopedPtr(Entity* entity)
		: m_Obj(entity)
	{
	}
	~ScopedPtr()
	{
		delete m_Obj;
	}
};
现在想要访问Entity类中的 Print（）函数，访问变量，这是主函数里不可以使用圆点，但可以将ｍ＿obj 变成 Public的，或者，返回一个Entity 指针，就像 GetObject 。

	Entity* GetObject() { return m_Obj;}

};


int main()
{
    ScopedPtr entity = new Entity();

	entity.GetObject() -> Print();
这方法看起来太乱了，希望可以像使用堆分配Entity一样使用，

	Entity* GetObject() { return m_Obj;}

};

int main()
{
    Entity* entity = new Entity();

	entity -> Print();
上面这样看起来好不少，

int main()
{
   Entity* entity = new Entity();

	entity -> Print();
上面不用写 Get Object（） ，写成下面这样，

Entity* operator->()
{
     return m_Obj;

}
主函数改写为下面这样

int main()
{
    ScopedPtr entity = new Entity();

	entity -> Print();
运算符已经重载，这可以正常编译，如果将其改为 const ，那么上面重载改为 const ，返回一个　const Entity,并标记此运算符重载为 const 。当然上面调用的函数也要标记为 const 。要保持所有都是 const 。（const 指针 只能调用 const  方法）

const Entity* GetObject() const
	{
		return m_Obj;
	}

};

int main()
{
     const ScopedPtr entity = new Entity();
（重载-> 使其表现为指针）。

好像 这就是一个常量指针，像（Entity*)一样。

const Entity* entity = new Entity();
这两行代码没什么区别，当然，因为是 ScopedPtr,所以将会对实际的对象进行自动删除（析构）。

#include<iostream>
#include<string>


class Entity
{
public:
	void Print() const { std::cout << "Hello!" << std::endl; }

};

class ScopedPtr
{
private:
	Entity* m_Obj;

public:
	ScopedPtr(Entity* entity)
		: m_Obj(entity)
	{

	}
	 
	~ScopedPtr()
	{
		delete m_Obj;
	}
	 
	Entity* GetObject() 
	{
		return m_Obj;
	}
	 
	const Entity* GetObject() const
	{
		return m_Obj;
	}

};


int main()
{
     const ScopedPtr entity = new Entity();

	entity.GetObject() -> Print();
	 
	std::cin.get();

}
如何使用箭头操作符，来获取内存中的某个成员变量的偏移量。

结构体是由浮点数组成的，有浮点数 x,y,z，每一个有四个字节，所以 x 的偏移量是 0 ，因为在结构体第一项，y的偏移量是4个字节，因为 float 在结构体中有4 个字节，而 z 的偏移量会是 8 个字节。

如果将变量位置变换

float x ,z ,y;
在类中，他们的工作方式是一样的。但他们在内存中的布局会不同。

想访问这些变量的地址，但不是通过有效的地址，地址从零开始。

写一个 0 ，然后把他转换成一个 Vector3指针，然后用箭头访问 x,将会得到这些内存的布局。

（（Vector3*)0) -> x;
接着要做的就是取这个 x 的内存地址，然后得到这个 x 的偏移量，因为从 0 开始，也可以写成 nullptr,再把它转成 Int 类型，然后让他等于 offset ，再打印出来，运行结果打印出 0 .

int offset = (int)&（（Vector3*)nullptr) -> x;
接着把它改为 y ，结果就为 4 .

int offset = (int)&（（Vector3*)nullptr) -> y;
最后改为 z ，当然结果是 8 ，

int offset = (int)&（（Vector3*)nullptr) -> z;
也就是先构造一个 Vector3类型的空对象指针，然后用该指针去调用对象的成员变量，最后用 int&得到变量的地址。



57.动态数组（std::vector)
特别是标准库中的 vector类，现在开始写一些 Ｃ＋＋标准库里的东西，这个还是很重要的。

标准库，在这种情况下应该叫标准模板库，本质上就是一个库，里面装满了容器，容器类型，这些容器包含特定的数据，之所以被称为标准模板库，因为它可以模板化任何东西。整个库模板化意味着容器的底层数据类型。

可以说容器包含的数据类型，实际上是由你决定，所有东西由模板组成。使用模板库并不需要多了解模板，只要知道模板可以处理你提供的底层数据类型，这意味着你不需要编写自己的数据结构或类似的东西。

Vector 本质上是一个动态数组，不是向量。他像是一个集合，一个不强制其实际元素具有唯一性的集合。和数组不同的是，他没有固定的大小，也就意味着，当你创建它时，创建一个动态数组时，不需要固定大小，也可以给一个固定大小，如果想用一个特定的大小初始化它。

一般情况下，不给他一个大小，只需要创建这个 Vector或者这个数组，然后把元素放进去，每放一个元素，数组大小会增加。

我们会对他们进行优化，使他们标准模板库中的快得多，因为标准模板库速度不是优先考虑的东西，所以很多时候，工作室或团队都会创建自己的容器库，

struct Vertex
{
	float x , y , z ;

};

std::ostream& operator << (std:: ostream& stream, const Vertex& vertex)
{
	stream << vertex.x << " , " << vertex.y << " , " << vertex.z;
}

上面这里的代码是一个基本结构体和 输出运算符的重载。

如果我们想要静态数组，不考虑 std::array 类的话，创建一个静态数组，需要绑定大小，即使在堆上创建，

Vertex* vertices = new Vertex[5];
	vertices[4];
可以访问索引 0~4，当想要超过时，会遇到麻烦，因为我们想不断地添加顶点，

例如有一个用户名输入，可能 10 个顶点之后，就不能再输入更多了，需要一种方式，当达到最大容量时，重新调整容量。

vertices[10];
第二种解决问题方法就是分配变态数量的 vertex，基本上也是程序的容量上限了，所以在某种程度上支持无数个  vertex(顶点） ，

Vertex* vertices = new Vertex[5000000];
但这种也并不理想，因为意味着你会用掉那么多的内存，如果我们只有5 个就是巨大的浪费，所以可以用vector类来代替，

需要在前面加上头文件 #include <vector>,创建时，也加上std::vector，然后输入数据类型，再加上名称。注意在数据类型那边，并没有存储一堆 vertex指针，实际上只是 把 vertex存在一条直线（一段内存内） 上。

有很多人会问，是否应该把指向堆分配的类对象的指针，存储在自己的 vector中，或者应该存储 栈分配，一条线上分配的 vertex类或者结构体。要视情况而定。因为很难决定是使用vertex对象还是 vetex指针，主要考虑的是存储vertex对象比存储指针在技术上更优，因为如果是对象，内存分配是在一条线上。动态数组，是内存连续的数组，意味着它在内存中不是碎片，而是一条线， 

如果你像这样将 vertex对象存储在一条直线上，有一个顶点，x、y、z；x、y、z......,一个接着一个，这是最优的，因为如果想要遍历或者设置或者改变他们，读取他们或者不管想做什么，在某种意义上说，他们都在同一条高速缓存线上，相当于链表了。

试着一条直线分配，唯一问题是如果要调整vector的大小，需要复制所有的数据，如果碰巧有一个字符串的 vector ，需要调整vector的大小，确实需要重新分配和复制所有的东西，是一个非常缓慢的操作。

而指针并不一样，实际的内存保持不变，因为你只是正确的保存了指向内存的指针，所以实际的内存保持不变，到了调整大小的时候，他只是副本也就是整数，只是实际数据的内存地址，而数据仍然被存储，在内存中的不同位置。

所以，尽量选择使用对象（而不是指针），指针是一种就像栈分配和堆分配一样。指针是最后选择。

std::vector<Vertex> vertices;

vertices.push_back({1，2，3});
vertices.push_back({4，5，6});
怎么在有了上面的这一行后加入东西进去，只要输入vertices。push_back，其他语言可能是合适呢么 add（）什么，在C++中就是 push_back(),然后是要加入 vertex，所以在这样情况下，可以使用并初始化一个列表，来指定 x ,y ,z ，比如上面这样写，先1，2，3，再4，5，6。

因为 vector是一个完整的类，实际上知道他的大小，可以访问它，

	std::vector<Vertex> vertices;
	 
	vertices.push_back({1,2,3});
	 
	vertices.push_back({4,5,6});
	 
	for(int i = 0;i < vertices.size(); i++ )
	     std::cout <<vertices[1] << std::endl;
先是写一个for 循环，取得数组大小，因为c++可以对索引操作符重载，所以可以这样输入，就好像是一个数组，输出结果为 打印出

1，2，3

4，5，6

（但是我的 12版本在上面初始化 1，2，3时会左边{ 报异常。

也可以使用基于range的for循环语句，

for(Vertex v :vertices)
	     std::cout <<  v  << std::endl;
就像上面这样，输出依然和上面一样，但是这样写是将每个 vertex复制到这个 for 范围循环中，如果想尽可能避免复制，

for(const Vertex& v :vertices)
	     std::cout <<  v  << std::endl;
像上面这样加上一个 & 符号，（加不加 const 都可以） ，只要有这个符号，就不会复制数据。

最后想要清除 vertex 列表，只要输入下面这行代码，会将数组大小设置为 0 .

vertices.clear();
我们也可以单独移除某个 vertex ，使用下面的代码 

vertices.erase()
只看up的界面显示是 erase 会返回 vector_iterator ，忽略返回的这些类型，看erase的函数，你可以看到 ，参数是 const iterator ,这意味着我们不可以在这取数值 2，或者类似的东西，需要的是一个迭代器（iterator）。

这里以移除第二个元素为例举，也就是索引 1 ，实际上可以通过 vertices的开始，也就是 vertices.begin，代码为下面这样

vertices.erase(vertices.begin() + 1);
上面代码就会从第二个元素开始删除。

再试试将 for循环移到 erase后面，输出结果确实是移除第二行元素，

1，2，3

4，5，6

1，2，3

相比之前输出少了一行4，5，6。

另一点需要注意的是，当将这些 vector 传递给函数或类似其他时，确保是通过引用传递它们的，不会修改的话，就用const引用，这样做可以确保你没有把整个数组，复制到这个函数，所以做这些时，确实是通过引用传递的，

void Function(const std::vector<Vertex>& vertices)
{


}
struct Vertex
{
	float x , y , z ;

};

std::ostream& operator << (std:: ostream& stream, const Vertex& vertex)
{
	stream << vertex.x << " , " << vertex.y << " , " << vertex.z;
	return stream;
}

void Function(const std::vector<Vertex>& vertices)
{


}

int main()
{
	std::vector<Vertex> vertices;

	vertices.push_back({1,2,3});
	 
	vertices.push_back({4,5,6});
	 
	for(int i = 0;i < vertices.size(); i++ )
	     std::cout <<vertices[1] << std::endl;
	 
	for(Vertex v :vertices)
	     std::cout <<  v  << std::endl;
	 
	vertices.erase(2);

 

	std::cin.get();

}








58.std::vector 使用优化（第一次优化相关）
C++很适合优化，优化很重要的规则之一就是要很好地了解你的环境，是指是指事情是如何运作的以及应该怎么做，会发生什么。

先要回顾下 vector是怎么工作的，创建一个 vector，然后开始push_back 元素，也就是向数组中添加元素。这个vector 的容量一定要足够大，能够容纳需要的元素，如果不够容纳新的元素，需要做的是，vector 需要分配新的内存，至少需要能容纳想要加入的新元素。

当前 vector 的内容，从内存中的旧位置复制到内存中的新位置，然后删除旧位置的内存。

所以当我们 push_back一个新元素时，如果容量用完，就会调整大小，重新分配，这就是拖慢代码运行速度的一个原因。需要不断地进行分配，是一个缓慢的操作过程，需要复制所有现有的元素，需要重新分配，这是我们要避免的。这也就是实际上对于复制的优化策略。

如果处理的是 vector ，特别是基于 vector 的对象，如何避免复制对象，我们没有存储 vector指针，存储的是 vector 对象，

struct Vertex
{
	float x , y , z ;

	Vertex(float x,float y,float z)
		:x(x),y(y),z(z)
	{
	 
	}

};
上述为之前的顶点 vertex 类，还添加了一个构造函数，

 

int main()
{
	std::vector<Vertex> vertices;

	vertices.push_back({1,2,3});
	 
	vertices.push_back({4,5,6});
主函数已经有了两个 vertex ，已经被 push_back,看看代码实际发生了什么，实际发生了多少次复制，可以在之前放一个断点，或者在控制台打印些什么，看看拷贝构造函数什么时候被调用，

Vertex(const Vertex& vertex)
		:x(vertex.x),y(vertex.y),z(vertex.z)
	{
         std::cout << "copied " << std::endl;

	}
创建一个拷贝构造函数，这个 vertex类的构造函数，其实不需要初始化列表，但还是写了，

打印到控制台，会打印出三个  copied  .

在主函数添加一个元素，并且用上构造函数，

int main()
{
	std::vector<Vertex> vertices;

	vertices.push_back(Vertex(1,2,3));
	 
	vertices.push_back(Vertex(4,5,6));
	
	vertices.push_back(Vertex(7,8,9));
把现有的东西，使用创建的新的构造函数来替换，这只是默认的构造函数，和前面代码（干的事情）完全一样的。但这样更好点，你可以知道发生了什么。如果运行这段代码，会打印出 六次

copied  ，证明复制了六次我的vertex 。

将断点打在 123那行，进行编译，会发现什么都不会打印，然后向下编译，会打印出一个 copied

，已经push_back一个元素，一个vertex ,有了一个复制，产生这样的原因就是我们在主函数的当前栈帧中构造它，所以是在main 的栈上创建它，然后要做的就是把它放到这个 vector 中，所以需要从那个main类中，是从main 函数中，从 main 函数（把这个创建的 vertex）放到实际的vector中，，放到 vector 分配的内存中。我们所做的就是将它（vertex）从main函数复制到 vector类中，这也是我们犯得第一个错误，也是可以优化的第一件事，可以在适当的位置构造那个vertex，在vector分配的内存中。

代码再向下运行一行，会打印出三个  copied,我们知道其中一份拷贝会在哪里出现，在构造第二行 vertex 对象，在 main 函数内部，在构造这个vertex对象，在 main 函数内部，然后把它放入 vector vertices中，这就产生了一个复制，那么为什么会有一个多余的复制，将鼠标放在 vertex上面，看实际的 vertices vector 可以看到 capacity 的容量为 2 ，这意味着这个 vector在物理上有足够的内存来存储两个顶点（两个顶点对象）。

再继续编译，将最后一个 push进去，然后 按下 F10， 这时它的 capacity 容量就会扩大为 3， 需要将容量调整到3 或更高的值，这样就有足够的内存，来放入我们的第三个顶点 vertex。

这也是另一个潜在的优化策略，vector在这里改变了两次大小，当然默认情况下大小为1，有第二个元素就移动到2，有第三个元素就移动到 3。

如果我们了解我们的环境，如果知道计划放进三个 vertex对象，为什么不直接告诉vector制造足够的3 个对象的内存，就不必调整两次大小了，从一开始就给三个元素留下足够的内存，因为打算将他们放进去，这也就是第二种优化策略。

vector是堆空间，可拓展，可估一个大概 size ，知道数值也只是知道当前需要放几个，数组大小还需要一直扩大。

vertices.reserve(3);
在主函数添加上述代码，想要容量为3 ，如上，括号里的 3 就是要设置的容量。这与调整大小（resize)，或在构造函数中传入 3 是不同的，


	std::vector<Vertex> vertices（3）;
	 
	vertices.reserve(3);
上面两行代码，第一行就是直接构造函数传入 3,（但是在12版本是无法编译的，不过听说在后面版本是可以的）。12版本如果这样就会得到，vertex没有正确的默认构造函数可用。因为这实际上不仅仅是分配足够多的内存，来存储三个 vertex 对象，实际上会构造三个 vertex 对象，而我们并不想构造对象，只是想要有足够的内存来容纳他们。这也就是为什么使用 reserve，它可以确保我们有足够的内存。

所以第一步是创造vertices vertor 然后reserve 3，然后把元素 push_back，如果这样写后，会得到 3 个 copied 。这为我们节省了很多的复制copies 。

实际上这种写法还是得到了一个复制的 copies，因为第一个 vertex 是在 Main函数中构造的，然后复制到实际的 vector 中，想在实际的 vector 中构造，不使用 push_back ，而是使用 emplace_back ，这样就不是传递以及构建的vector 对象，只是传递了构造函数的参数列表。

    std::vector<Vertex> vertices;
     
    vertices.reserve(3);
     
    vertices.emplace_back(1,2,3);
     
    vertices.emplace_back(4,5,6);
     
    vertices.emplace_back(7,8,9);
上面这样写告诉上面第一行的vector,在实际的vector内存中，使用以下参数，构造一个 vertex对象。将上述代码进行编译，会发现控制台很清爽，没有了复制的 copies。

我们 如何优化它，只要意识到如何工作的就行了，如上示例，意识到 vertex 对象实际上是被复制了 六次，优化起来也就不难了，也会比最初的代码运行起来快很多。

 

59.C++库（静态链接）
因为现在做项目都是多人合作，所以要使用包管理器，要链接到其他的代码仓库之类的东西，虽然每次迁移项目，link错误都不会少。

对于大多数严肃的项目，绝对推荐实际构建源代码。如果使用的是VS,那么可以添加另一个项目，该项目包含你的依赖库的源代码，然后将其编译为静态或动态库。然而，如果拿不到源代码，或者你的计划这只是一个快速项目，不想花太多时间去设置他们，因为这是一种一次性的东西，或者是一个不那么重要的项目，那么更推荐链接二进制文件，会更快更容易，具体来说将以二进制文件的形式进行链接，而不是获取实际依赖库的源代码并自己进行编译。

我们只讨论处理二进制，确切的说是GLFW库。另一件事，或许你实际项目中，或你实际想链接的库中，二进制文件不可用，可能被迫去构建它，对于mac和linux 系统尤其如此，在 unix系统，人们通常喜欢自己构建代码。自己编译它一是想修改库，稍微改变一下.    



 看右侧橙色按钮，第一个是点击下载 glfw的源代码，但是点击它上面的那个灰色栏里面的 Download,就可以看到 windows预编译二进制文件，



 可以看到对于 linux 需要自己编译，但在之前up打开的时候，mac也没有做出来。对于wndows，我们有32bit 和 64bit可以选择，至于到底选择几位的包和你使用的操作系统环境没什么关系，并不是你是64位windows就是使用 64 bit 二进制文件，而是意味着你选择想要的东西（32 or 64） ，是你的目标程序。所以如果编译我的应用程序，作为X86也就是 Win32程序，那么就要32bit 的二进制文件。要是编译一个64 bit 应用程序，那就64 bit 二进制文件。一定要把他们匹配起来，不然他们无法进行链接。

库通常包含两部分，includes 和 library，包含目录和库目录。其中includes 是一堆我们需要使用的头文件，这样就可以实际使用预购建的二进制文件中的函数，然后 lib 目录有那些预先构建的二进制文件，这里通常就有两部分，包含动态库和静态库，但并不是所有库都为你提供了这两种库。

GLFW库提供了动态库和静态库，你可以选择是静态链接还是动态链接。这里简单讲下两者区别，之后会详细介绍。

静态链接意味着这个库会被放到你的可执行文件中，在你的 EXE 文件中,或者其他操作系统下的可执行文件。

动态链接库是在运行时被链接的，所以你仍然有一些链接，你可以选择在程序运行时，装载动态链接库，有一个叫做 loadLibrary的函数，可以在 WindowsApi中使用它作为例子。它会载入你的动态库，可以从中拉出函数，然后开始调用函数。你可以在应用程序启动时，加载你的 dll 文件，这就是动态链接库。

主要区别是库文件是否被编译到 exe文件中或链接到 exe 文件中，还是只是一个单独的文件，在运行时，需要把它放在你的 exe 文件旁边或者某个地方。然后你的 exe文件可以加载它，这和（静态链接）是不同的东西，因为这种依赖性，你需要有 exe文件和 dll 文件（在一起）。

所以 cherno 通常喜欢静态的。尽可能的链接。

静态链接在技术上更快，因为编译器或链接器实际上可以执行链接时优化之类的，可以在技术上产生更快的应用程序，因为有几种优化方法可以应用。知道在链接时我们要链接的函数。而动态库我们不知道会发生什么，必须保持他的完整，当（动态链接）库被运行时的程序装载时，程序部分将被补充完整。所以静态链接是最好的选择，而动态链接一般内存占用更少。

这里实验两种方法，在项目中，必须把它指向头文件（includes) ,就知道哪些函数是可用的，然后就有了这些函数声明，实际上是符号声明，因为他们也可以是变量，只是用函数作为例子。

然后还要将链接器指向库文件，告诉链接器这是我的库文件，想让其将它连接起来，就能得到正确的函数定义，下面对静态库和动态库都这样操作。



 下载使用 32 bit 包，解压后放入项目文档中，这里实际上已经附加了使用不同的编译器编译出来的库文件，选择 include 和 lib-vc2015 （选最新的，他当时是2015，但现在已经可以选2022）两个包，选择什么其实不那么重要，说到底只 是一种编译过的二进制文件，up 现在的工具链希望选择 vc2015版本，因为现在使用的是17版本的 VS ，虽然现在我用的 是12版本的 VS ，但我也选 15 版本的文件。



 这个包比当时多了一个 glfw3_mt.lib的文件，其他都是一样的（起码文件名是的），其中dll是一种运行时动态链接库，在运行时时动态链接时会用到他。

glfw3dll.lib实际上是一种静态库，这个是与 dll 一起用的，这样就不需要实际询问 dll 。就有了一堆指向所有这些函数的函数指针，也就是说，下面这个 glfw3dll.lib 实际包含了 glfw3.dll中所有的函数、符号的位置，所以可以在编译时链接他们，。相比之下，如果没有下面的  glfw3_mt.lib文件，还是可以使用上面的 dll 文件，要通过文件名来访问 dll 文件内的函数，要访问 GLFW_INIT函数或者其他函数，但下面的那个 lib 文件已经包含了所有的函数位置，链接器就可以直接链接到他们。

glfw.lib可以看到静态链接库比其他大得多，如果我们不想要编译时链接，就链接这个（lib) ,而如果这样做，那在 exe运行时，就不需要这个 dll，下面就链接它们。

先点击整个项目名，右击选择属性，记得上面选择所有组态，选择c/c++里面的第一个选项 （一般），然后第一个路径，要填写 include 的文件路径，但是你可以看到前面的路径名是本地的路径名，当其他人从 github 拷贝时，不能正常使用，所以需要一个相对于这个实际目录的路径，



 上面的是自己电脑的路径，

可以使用宏   $SolutionDir   ，在 VS 中可以使用，在这个下面你可以编辑，



 可以看到宏的值，只能显示到项目名，所以要在后面加上后面的路径名，

  

上面可以看到，include文件夹里的 GLFW文件夹有两个  .h 文件，所以要在 .cpp 文件中写上预编译文件   #include "GLFW/glfw3.h"   ,然后按下 CTRL + F7 ,编译是没有问题的



 

 

因为这是一个编译器指定的包含路径，也可以使用尖括号  <> ，这就引出了使用引号还是尖括号的区别，实际上没有什么区别。使用引号的话，会先检查相对路径，如果没有找到任何相对于这个文件的东西，也就是相对于 main.cpp文件，他就会去找编译器，会检查编译器的 include 路径。

up选择依据是，如果这个源文件在 vs 中，所以如果GLFW.h 在解决方案的某个地方，也许在另一个项目中，都可以，那么就用引号。如果是一个完全的外部依赖，或者外部的库，不在 vs 中和实际解决方案一起编译，那就用尖括号，来表示它实际上是外部的，而这个例子就可以使用尖括号。

#include<GLFW/glfw3.h>
像上面这样写预编译，现在还没链接到库文件，但这样写就告诉编译器这里有一堆函数，



 可以将预编译名称选起来然后右击选择开启文件查看。

GLFWAPI int glfwInit(void);
尝试调用这个函数，

int a = glfwInit();
OK，看起来没问题，编译后发现也没有问题，但是当构建项目时，link会出现错误，错误原因是未解析的外部符号 unresolvesd external symbol  glfwInit,这也是造成许多问题的原因，意味着你并没有链接到实际的库，链接器找不到这个 glfwLnit 函数。

让我们再去头文件里面看看，它只有一个分号，但是并没有告诉我们这个函数的实际作用，只告诉我们这个函数存在，需要做的就是找到这个函数的定义，我们知道glfwInit() 函数，不带参数，返回一个 int ,可以写这个 glfwInit函数的实现，让它返回 0 或类似的东西。

 



 优化好后可以得到下面这样的情况，这样的话编译或者运行是没有问题的，构建会成功的，因为实际上提供了这个函数的定义，



 主代码如下图，

#include<iostream>
#include"GLFW/glfw3.h"

int glfwInit() 
{
	return 0;
}


int main(void)
{
	int a = glfwInit();

	std::cout <<  a  <<std::endl;
	 
	std::cout << "hello world!"<<std::endl;
	 
	std::cin.get();

 

}
那么明显并不想要定义，而是想要库里面那个，所以需要链接到库。

需要设路径，还是右击项目名称，右击选择属性，然后左边栏选择 linker（ 链接器） ，在子项选择input（ 输入） ，然后第一个路径要包含 glfw3.lib ，不想写绝对路径就还可以$(SolutionDir),但是想保持更加干净怎么办，回到 linker -> 一般，设置附加库目录，



 具体设置栏如上图所示，要包含 glfw3.lib库文件的根目录，然后设置完参考就如上图所示，

然后再回到 linker -> input里面去输入路径（但是最后我设置好后显示 预设的程式库和 其他的库冲突，然后下面还有一堆解析错误，回去使用新版本试试）。

如果我们将关于他的头文件删除，当然是无法编译的，因为没有这个函数，但是可以写一个没有参数的 glfw3 函数，然后这样也是可以的，但是又因为它是 C 的库，所以还是会得到一个错误。

那么就需要在函数声明前面加上 extern " C " ,意思是保持这个名字的原貌，因为你会链接到一个在 Ｃ语言建立的库，

extern "C" int glfwInit();
这样不管编译还是构建都正常运行，打印出结果为 1 .

整体代码如下，注意这样使用的时候是不需要再加上头文件的

#include<iostream>


extern "C" int glfwInit();

int main(void)
{
	int a = glfwInit();

	std::cout <<  a  <<std::endl;
	 
	std::cout << "hello world!"<<std::endl;
	 
	std::cin.get();

但是效果加在预编译一样，

这些头文件当然可以自己编写内容，库的链接也好，头文件也好，只是将系统的所有组成部分连接在了一起。因此头文件通过提供声明告诉我们哪些函数是可用的，然后库函数为我们提供了定义，这样就可以链接到那些函数，并在 C++中调用函数时执行正确的代码。

 

 

 

 

60.动态库
动态链接是发生在运行时的，静态链接是发生在编译时的，当你编译一个静态库的时候，将其链接到可执行文件，也就是应用程序，或者链接到一个动态库，这有点像你取出那个静态库的内容，然后把这些内容放到其他的二进制数据中，它实际上在你的动态库中或者在你的可执行文件中。

编译器和链接器完全知道，静态链接时，实际进入应用程序的代码，其他以后会展开，现在只要记住静态链接允许更多的优化发生。编译器和链接器可以看到更多的东西（从静态链接中），特别是链接器，可以看到更多。

而动态链接，发生在运行时意味着只有当你真正启动你的可执行文件时，动态链接库才会被加载，所以他并不是可执行文件的一部分。

当启动一个普通的可执行文件时，可执行文件会被加载到内存中，然而，如果有一个动态链接库，就意味着实际上链接到另一个库，一个外部二进制文件，在运行时动态的（链接）。运行时，将一个额外的文件加载到内存中。

可执行文件的工作方式变了，现在需要某些库，某些动态库，某些外部文件，这也就是你会看到过在 windows上，有时当你启动应用程序时，能看到一个错误消息弹出，比如需要 dll，或者没有找到 dll 。这样就不能启动应用程序，这也是动态链接的一种形式，喜欢叫他为 “ 彼此彼此” 。

因为可执行文件知道动态链接库的存在，可执行文件实际上把动态库作为一项需要，虽然动态库仍然是，一个单独的文件，一个单独的模块，并且在运行时加载，也可以完全动态的加载动态库，这样可执行文件就与动态库完全没有任何关系了，可以启动可执行文件，应用程序，甚至不会要求你包含一个特定的动态库，但是可执行文件中，可以写代码，去查找并在运行时加载某些动态库，然后获取函数指针或任何你需要的那个动态库中的东西。

#include<GLFW/glfw3.h>
使用动态链接的时候头文件相对于静态链接并不需要改变，同时支持静态和动态链接。接着还是右键项目名，然后 属性，接着 linker 里面的路径改为动态文件路径。 在文件夹里还有一个 glfw3dll.lib文件，这个基本上就是一堆指向dll文件的指针，我们就不用在运行时去检索所有东西的位置，同时编译这两个文件是非常重要的，因为如果尝试使用不同的静态库，在运行时链接到 dll，可能会得到不匹配的函数和错误类型的内存地址，函数指针不会正常工作。

所以呢，这两个文件是由 glfw 发行的，所以他们是同时编译的，是直接相关的，不可以把它们分开（指的是 glfw3.dll 和 glfw3dll.lib ) ,

然后还是右键项目名称选择属性，然后点击 linker 里面的第二项目录修改路径为

glfw3dll.lib

然后再运行，会显示 exe 正常构建成，但是会跳出弹窗，显示代码行执行不能继续，是因为 glfw3.dll 没有找到，这就是告诉程序要使用 glfw3.dll，请加载。简单的做法就是将 .dll文件复制到 debug文件夹里，然后再运行就可以了。

这就是全部内容，链接到静态库，确保在一个可访问的地方有 dll文件。

GLFWAPI int glfwInit(void);
右击打开头文件的glfw3.h可以发现他的在返回类型和实际函数名之前定义了 GLFWAPI，

#if defined(_WIN32) && defined(_GLFW_BUILD_DLL)
 /* We are building GLFW as a Win32 DLL */
 #define GLFWAPI __declspec(dllexport)
#elif defined(_WIN32) && defined(GLFW_DLL)
 /* We are calling GLFW as a Win32 DLL */
 #define GLFWAPI __declspec(dllimport)
#elif defined(__GNUC__) && defined(_GLFW_BUILD_DLL)
 /* We are building GLFW as a shared / dynamic library */
 #define GLFWAPI __attribute__((visibility("default")))
#else
 /* We are building or calling GLFW as a static library */
 #define GLFWAPI
#endif
上上面就是一个函数的定义，有整个东西，如果我们在 windows 系统下需要构建 dll ，然后就会输出dll函数，这很重要，如果 没有构建，程序就不可以运行。

 #define GLFWAPI __declspec(dllexport)
然后定义了 win32 和 glfw_dll ，意思是将 glfw 作为 win32_dll调用，也就是 declspec (dllimport) ，最后就是构建静态库了，调用 glfw 静态库，这里定义 #define GLFWAPI 为 nothing .

 #define GLFWAPI
所以现在的问题是我们把他当作真正的 dll来使用，在 C/C++ - >prerocessor 添加预处理定义 GLFW_DLL,



 然后运行，正常运行，为什么不需要定义 declspec dllimport ，（可能是那个静态的库里的指向动态库函数的指针加了这层封装）（导入函数，dllimport是优化：导入数据，dllimport是必须）。

 

 

61.多项目且共享单库
如果项目规模很大，不仅可以帮你用代码创建模块或库，并多次重复使用这些代码。而且还允许混合语言。（这基于做的事相当大的项目），

创建一个新的方案，命名为 game ，他将像我们的游戏项目，实际的可执行程序，然后做一个名为 Engine的库，链接到游戏中，

所以接着就在 game 专案里添加一个Engine的项目，



 注意是在右键专案然后添加项目，得到的折叠后就像上面这样，



 右键打开game属性，看一下配置类型在一般属性下设置为 应用程序。另一个 专案 Engine右键属性，因为需要静态链接，要确保配置类型设置为静态库，



 就像上图这样配置好，注意这里上面的平台什么的都要修改为所有都可以。将他设置为一个静态库，然后写一些代码来测试。当然还是老方法，先删掉专案里所有的虚拟文件夹，然后添加一个新的文件夹，命名为 src 。将两个文件夹相同操作，然后 Game项目再加一个 application.cpp 文件，这基本上是源文件，也就是主文件。

在 另一个专案 Engine 添加一个头文件 Engine.h   和一个 Engine.cpp 项目。

#pragma once 

namespace engine {
	   void PrintMessage();

}
上面的函数不需要参数之类的，不用实现这个函数，因为要去 c++ 文件， include 这个头文件，

#include "Engine.h"
#include<iostream>


namespace engine {
	   void PrintMessage()
	   {
		   std::cout << "Hello World!" << std::endl;
	   }
}
上面是在 Engine.cpp 文件的代码，很简单就是一个函数，先不处理 class 类类似的东西，然后回到 application.cpp 写主函数。

目标是在这能够调用 Engine::PrintMessage ，想要这些代码能够成功的执行和链接，所有这些写入到自己的可执行文件。

int main(void) 
{
	engine::PrintMessage();
接下来就是包含一个头文件，来定义这个，技术上讲，这个例子很简单，可以写上 

namespace engine {
	void PrintMessage();

}
然后之前主函数的错误就会消失了。

Ctrl + F7 编译代码，会成功编译。这并不需要链接，因为已经有了定义，如果 build 项目，会显示一个 link 错误。因为上面图那几行代码就是 Engine.h里面的代码，所以可以不用写，而是用那个头文件。

一种是相对于当前的路径，右键application.cpp文件，



 显示.cpp在这个文件夹，然后返回到Engine.h文件夹，



 记住该文件所在的目录，然后加入头文件 

#include"../../Engine/src/Engine.h"
还是写一下吧，就是后退两个文件夹，然后进入文件夹。然后他运行时错误消失了（试过了，不管是12版本还是19版本都不可以，都会出现），但是这个方法并不太理想，有一个相对路径，返回到目录，然后到一个完全不同的项目，到 src ，看起来是有点乱，如果移动文件夹或重命名项目名之类的都会崩溃，所以真正想做的是使用绝对路径，特别是使用编译器的包含路径。



 还是得注意要先把上面的设置为全部，然后下面这样设置，就是包含目录。SolutionDir只是一个宏，就是包含  sln 文件的目录，我们再在后面添加项目名等细分。

#include "Engine.h"
设置完就直接头文件写成这样。（但我的12版本还是会报错,19版本也是报错) ,

至于头文件使用引号还是尖括号，还是偏向于使用引号，只有包含一些解决方案之外的东西，才使用尖括号。

右键 专案名 Engine ，然后会显示 lib ，然后可以去那个路径，就可以看到这个 lib 文件，可以到链接器设置把他作为输入，接着要做的就是点击 专案名 Game，点击 add，然后是 reference(还是报错) ，然后我们可以在项目和解决方案中，选择这个 Engine项目（有个Engine选项，可以勾选出来。)然后就会把那个 lib 文件链接到我们的可执行文件中，就像我们已经把它添加到连接器输入一样。

但是这也给我们一个好处，除了不需要处理链接设置输入文件之外，如果把 engine 的名字改为 Core什么的。如果他现在叫 Core,需要回到 Game 的链接器设置将输入改为Core.lib而不是 engine.lib.

Engine现在是Game 的依赖，所以 Game 依赖于 Engine ，意味着如果Engine内部的某些东西发生变化，然后我们要去编译 Game,那么 Game实际上是要编译引擎和游戏，所以总是在处理最新的代码，而不是会突然忘记编译 Engine ，然后就是各种不能用。这都是自动处理的，为我们创建了一个完整的依赖关系图。

如何证明这一点，右键 Engine,然后点击 clean solution ，然后到 Game 这里，建置，实际上是先构建Game，需要Engine才能工作，因为 Game引用了 Engine，

#include "Engine.h"
#include<iostream>

 

int main(void) 
{
	engine::PrintMessage();

	std::cin.get();

}
将 application.cpp代码写成上方这样，然后运行，正常打印出 "hello world!" ,这是使用静态链接，之后会使用 动态链接，只不过会有些不同。

打开 debug目录下 ，可以看到 game.exe文件，如果我取这个 game.exe文件，然后打开一个完全不同的文件夹，桌面上的 dist 目录， 把 game.exe粘贴到这里，就是单一个 .exe 文件，就是运行这个程序所需要的，而点击就会正常运行，就是这样的，当静态链接时，所有的东西都会被放入这个 exe文件，没有外部文件依赖，

 

 

 

 

 

62.处理多返回值（具体代码是在他的 openl ，看下 52节） 
一般知道，一个函数有多种返回类型，可以是整数，字符串等等，但是之前使用的一次只可以返回一种，如何一次返回多种类型，可能需要返回 vector 或数组，后者类似的东西，只包含这两个元素，不过处于一些原因，也不是最好的做法。

struct ShaderProgramsource
{
	std::string VertexSource;
	std::string FragmentSource;

};
创建一个叫做 Shader Program source 的结构体，只包含这两个字符串，当然如果还想返回一个整数或其他不同类型的东西，可以把它添加到结构体中并返回他。

struct ShaderProgramsource
{
	std::string VertexSource;
	std::string FragmentSource;
    int a;

};
这也是 up最喜欢的一种方式。

static ShaderProgramSource ParseShader(const std::string& filepath)
上方为原函数写法

接下来这种方法不需要 C++ 提供的特定类（方法） ，要让返回类型为 void ,实际上要做的是取两个字符串作为引用，字符串 vertexSource或其他，

static void ParseShader(const std::string& filepath,std::string& vertexSource,std::string& fragmentSource)
现在这些参数通过引用传递，可以写入实际参数，而且在这里的结尾，不是返回一些东西，

std::string vs = ss[0].str();
std::string fs = ss[1].str();


vertexSource = ss[0].str();
return { ss[0].str(), ss[1].str() };
上面是修改前(加上之前一部分），这样写，需要返回上面做这两个字符串，

std::string vs = ss[0].str();
std::string fs = ss[1].str();


vertexSource = vs;
fragmentSourceSource = fs;
当然，下面主函数调用这个函数时，需要把字符串取出来，这个函数返回的是 void ，所以需要把原函数的赋值去掉，对于 ParseShader函数，要先创建两个字符串，vs和fs，然后这样传递，

std::string vs,fs;
ParseShader("res/shaders/Basic.shader",vs,fs);
对于这个 CreateShader函数，提供这两个字符串（vs,fs) 作为参数就可以了，这也是一种（多返回值）的处理方法。

技术上来说，这是最理想的方法之一，因为没有字符串复制，虽然重新分配了字符串，或许有些问题，但性能方面还是很好的。因为在上面两行代码中，第一行构造了这个字符串在main函数的栈帧中，然后第二行代码传入指向这两个东西的指针，当要改变（这两个）字符串时，把它写入（这两个）内存，预先分配了内存，Parse Shader不做任何动态内存分配。

实际分配字符串时（下面这里），需要做一些动态内存分配，因为需要将上面的 vs,fs 字符串复制到下面的字符串中，所以仍然有一个复制。

vertexSource = vs;
fragmentSourceSource = fs;
为了使上面更明显一点，可以传递指针，指针的好处就是传递引用的话，需要传递一个有效的变量，

static void ParseShader(const std::string& filepath,std::string* vertexSource,std::string* fragmentSource)
而用一个指针，可以传递 null之类的，然后做一个检查。如果 vs 被制定了，或者说是 vertexSource ，

std::string vs = ss[0].str();
std::string fs = ss[1].str();

if(vertexSource)

vertexSource = vs;
fragmentSourceSource = fs;
喜欢在输出参数的名字前加上out 之类的，

static void ParseShader(const std::string& filepath,std::string* outVertexSource,std::string* outFragmentSource)
这是一个输出的东西，if(outVertexSource) ,那么做逆行引用（解引用*） ，然后给他赋值Vs，

std::string vs = ss[0].str();
std::string fs = ss[1].str();

if(outVertexSource)
   *outVertexSource = vs;
fragmentSourceSource = fs;
这样处理后就可以提供参数了，首先需要明确的得到这些变量的内存地址，

std::string vs,fs;
ParseShader("res/shaders/Basic.shader",&vs,&fs);
这代表 outVertexSource ，意味着你要写入 vs地址，你可以指定为 nullptr ，意思是不要给我任何东西，只关心 fragmentSource ,是没有问题的。因为要求传入 nullptr ，而这是一个指针，这个函数可以正确处理。这是一种通过使用输入参数来处理多种返回类型的方式。

介绍另一个简单的方式，返回一个数组。

可以返回一个数组，就像返回一个指针，然后在这里的最后，去掉额外的参数。

static std::string* ParseShader(const std::string& filepath)
然后在最后可以 return new std::string ,这是新的数组，是一个2个元素的数组，可以出啊内Vertex Shader 或者 FragmentShader，现在是使用 new ，导致了堆分配，

std::string vs = ss[0].str();
std::string fs = ss[1].str();

return new std::string[] {vs ,fs };
可是想要避免这样，特别是因为这些字符已经是堆分配的，这里就不谈论性能，

可以通过设置这个等于 std::string ，shaders或 sources ，现在这种类型的数组有些烦，

std::string vs,fs;
ParseShader("res/shaders/Basic.shader",&vs,&fs);
上面这两行代码改为下面这行代码，

std::string* sources = ParseShader("res/shaders/Basic.shader",&vs,&fs);
至于为什么说shaders或 sources 类型的数组有些烦，因为并不知道有多大，只是一个指针。

最后一个方法就是返回一个 array，类型是string，大小是2，如下面代码

static std::array<std::string,2> ParseShader(const std::string& filepath)
最后返回一个 std::array ,不知道如何接受参数甚至不知道是否就收参数，

return std::array<std::string,2>(vs,fs)
因为不太使用 array ,所以还是使用下面的这样

std::string vs = ss[0].str();
std::string fs = ss[1].str();

std::array<std::string,2> results;
results[0] = vs;
results[1] = fs;
return results;
这里 incomplete type not allowed ，使用数组感觉不太舒服，所以需要，

 #include <array>; 
如果要使用 std::array ，对于上上面的 ParseShader函数，可能会有所变化，因为可能有两种以上的（返回）类型，所以另一种方法就是返回 vector，

#include <vector>

static std::vector<std::string> ParseShader(const std::string& filepath)
改为上方这样，这与数组方法 （std::array )区别，主要是 array会在栈上创建，而 vector会把它的底层存储在堆上，所以技术上讲，返回 std::array 会更快。而 vactor也是创建一个 vector，这里结束（不需要长度），接着赋值 results ，并返回 results，

std::array<std::string,2> results（2）;
这里可以保留 2 个元素位置或者其他个（元素位置）。这里的在内存分配上不太严肃。会涉及到重复复制的问题，但是这里不讨论性能相关。

下面讨论一种通用的方法，返回俩哥哥不同类型的变量，类型可能会变化也可能不会变化。也是 C++ 提供的一种形式。

两种方式一种叫做 tuple(元组）的东西，一个是叫做 pair 的东西。

tuple基本上是一个类，可以包含 x 个变量，但他不关心类型，下面就返回一个 std::tuple，

static std::tuple<std::string，std::string> ParseShader(const std::string& filepath)
模板类型是 std::string  和 std::string (没错的，看代码，别理解错意思啦）

如果还想返回一个整形，可以再后面再加一个

static std::tuple<std::string，std::string，int> ParseShader(const std::string& filepath)
不过这里不需要写的，这里只返回两个字符串。

然后#include 头文件，

#include <utility>
他实际上包含了 tuple(不确定，所以又加了个 functional ).

#include < functional>
噢，好像是 utility 只是提供了 make_tuple 这样的工具，因此最后返回的可以写成，

return std::make_pair<std::string,std::string>(vs,fs);
上面这是在 unility 里面的东西，特别是用于创建一对。两个std::string ，然后再传进去  vs 和  fs .

但是这里写完后显示异常， (no instance currently matches the argument list.) ,初步感觉是没有包含什么，然后发现可以将最后的返回不需要指定两个std::string(模板参数）,所以可以去除。

return std::make_pair(vs,fs);
系统会自己搞定，所以只要返回 std::make_pair就会自己返回tuple ,而在这个 tuple 的例子中，还需要在下面这里返回一个 tuple ，模板参数是两个 std::string ,这个要修改的还是之前未修改的

（对了，有人说 make_pair返回的是 pair 而不是 tuple) 

std::tuple<std::string,std::string> source = ParseShader("res/shaders/Basic.shader");
这个 sources 作为 CreateShader函数的参数，这里返回的类型可以使用 auto关键字替代。也就是改为下面这样的

auto source = ParseShader("res/shaders/Basic.shader");
从 tuple里面取数据很讨厌，因为需要std:get ，然后模板参数是索引，所以取值为 0.接着是想要从中获取它的值的 tuple 元组，这里是 sourcs,这就是想要的 VertexShader  (vs) .

auto source = ParseShader("res/shaders/Basic.shader");
std::string vs = std::get<0>(sources);
如果要在 CreateShader函数中使用，要传入 std::get<0>(sources)就是到在下面那行代码修改传入，源代码就是下面这样的，

unsigned int shader = CreateShader(source.VertexSource,source.FragmentSource);
传入后变成下面这样

auto source = ParseShader("res/shaders/Basic.shader");
std::string vs = CreateShader(std::get<0>(sources),std::get<1>(sources));
转入的两个就是我们从tuple中得到这两个东西的方法。但是这样写是很不好的。

第一点就是 0,1是实际变量的名字，很难分辨，哪个是 vertex哪个是 fragment，当然从逻辑上讲 是对应的，但还是有人会不知道。所以不可以使用数字来处理名字，这些命名的变量。

(看到有人说 解包  ）这个例子有两个返回值，可以返回一个std::pair，

static std::pair<std::string，std::string> ParseShader(const std::string& filepath)
返回值是两个字符串，与 tuple 区别是仍然可以用 std::get，但是还可以通过简单使用 sources.first，因为这里是只两个，所以接着的就是 sources.second ，感觉这样更好一些。

auto source = ParseShader("res/shaders/Basic.shader");
unsigned int shader = CreateShader(sources.first,sources.second);
但这样还是不知道变量第一个和第二个是什么。所以这就是为什么 cherno喜欢使用 struct 结构体来做。返回多个变量时，可以创造一个结构体。

struct ShaderProgramSource

{
    std::string VertexSource;
    std::string FragmentSource;


};
和下面的返回一对（pair)差不多，但是可以对变量命名。 所以可以定义 std::string vertexSource,

然后是 结构体FragmentSource.上面的这个背后 就是一个pair ,由两个 string 组成。

static ShaderProgramSource ParseShader(const std::string& filepath)
这里所有的东西都是在栈上创建的，如果只是像上面那样返回一个 ShaderProgramSource ,然后可以简单的返回，只是修改了 return 这一行。

return { vs,fs };
在主函数实际使用时也不用使用什么 first ，

auto source = ParseShader("res/shaders/Basic.shader");
unsigned int shader = CreateShader(sources.VertexSource,sources.FragmentSource);
这样就清晰明了了。

（传引用和创建结构体比较有用）。

 

 

 

63.C++模板（templates)












