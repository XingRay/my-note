# flex & bison 基础概述

https://blog.csdn.net/JiMoKuangXiangQu/article/details/128200598



## 1 前言

限于作者能力水平，本文可能存在谬误，因此而给读者带来的损失，作者不做任何承诺。



## 2 本文目标

. 简单介绍 flex 和 bison 的基础使用方法
. 简要分析 flex, bison 生成代码的工作流程



## 3 flex & bison

3.1 背景
本文所有分析，基于 Ubuntu 16 系统。

3.2 flex
3.1.1 flex 简介
flex用来生成词法分析器(lexical analysis, 或 scanner)，而词法分析器的作用，简单来讲，就是将输入，按定义的正则表示式模式，解析分割成一个个记号(token)。

```
# 生成词法分析器
                             flex
XXX.l(词法分析器规则定义文件) ======> 词法分析器

# 通过词法分析器，将输入数据流，解析成一个个记号(tokens)
           词法分析器
输入数据流 ===========> 一个个记号(tokens)
```



### 3.1.2 flex 使用例子

(1) 初次使用，先运行如下命令安装 flex：

```
sudo apt-get install flex
```



(2) 编写 flex 程序(用来生成词法分析器的规则文件XXX.l)。
我们先来了解一下 flex 程序的编写规则，flex 程序分为3个部分：

```
定义部分：
包含选项(option)，文字块，开始条件，转换等。
本部分中以空白行开头、或包含在%{和%}之间的部分，都会被原封不动的拷贝到C代码中。
%%
规则部分：
包含正则模式行和模式行匹配时执行的C代码。
以空白行开头、或包含在%{和%}之间的部分，都被认为是C代码，它们会被原封不动的拷贝到yylex()函数中。
大多数flex程序都具有二义性，即相同的输入，可能被多种模式不同的正则模式匹配。flex通过两个简单的规则来解决它：
. 词法分析器匹配输入时匹配尽可能多的字符串；
. 如果两个模式都可以匹配的话，匹配更早出现的模式。
%%
用户子程序部分：
这个部分通常包含在模式规则匹配时，执行的C代码调用的函数。
本部分会原封不动的拷贝到C代码中。
```



这3个部分，用2个%%分隔，前2个部分是必须的，但它们的内容可以为空，第3部分和它之前的%%可以省略。

了解 flex 程序的编写规则后，接下来，我们以一个统计字符数、单词数目、行数的 flex 程序为例，来演示一下flex的使用，flex 程序count-words.l如下：

```
%{
int chars = 0; /* 字符计数 */
int words = 0; /* 单词计数 */
int lines = 0; /* 行计数 */
%}

%%

[a-zA-Z]+	{ words++; chars += strlen(yytext); }
\n		{ chars++; lines++; }
.		{ chars++; }

%%

int main(int argc, char *argv[])
{
	yylex();

	printf("%8d%8d%8d\n", chars, words, lines);

	return 0;
}
```



编写一个简单的 Makefile 来编译我们的词法分析器：

```
count-words: count-words.l
	flex --noyywrap count-words.l # 生成词法分析器代码 lex.yy.c
	gcc -o $@ lex.yy.c # 编译词法分析器 lex.yy.c 

clean:
	-rm -f lex.yy.c count-words
```

编译和运行:

```
make # 编译生成词法分析器程序 count-words
./count-words # 运行词法分析器，按 Ctrl + D 结束数据输入
```



测试中我们发现，词法分析器从标准输入接收数据，这是默认的行为。如果我们想改变该默认行为，转而将文件作为输入，只需按如下修改 flex 程序count-words.l就可以达到目标：

```
%option noyywrap

%{
/* 统计单个文件数据 */
int chars = 0;
int words = 0;
int lines = 0;

/* 统计所有文件数据 */
int total_chars = 0;
int total_words = 0;
int total_lines = 0;
%}

%%

[a-zA-Z]+	{ words++; chars += strlen(yytext); }
\n		{ chars++; lines++; }
.		{ chars++; }

%%

int main(int argc, char *argv[])
{
	int i;

	if (argc < 2) { /* 没有给定文件列表，仍然从标准输入获取数据 */
		yylex();
		printf("%8d%8d%8d\n", chars, words, lines);
		return 0;
	}

	/* 遍历所有输入文件，统计每一个文件的数据 */
	for (i = 1; i < argc; i++) {
		FILE *fp = fopen(argv[i], "r");

		if (!fp) {
			perror(argv[i]);
			return -1;
		}

		/* 复位当前文件的统计数据 */
		chars = words = lines = 0;
		
		yyrestart(fp); /* 调用 yyrestart() 接口重置词法分析器的输入流到文件 @argv[i] */
		yylex(); /* 调用词法分析器进行数据统计 */
		fclose(fp); /* 关闭当前文件 */
		printf("%8d%8d%8d %s\n", chars, words, lines, argv[i]);

		/* 记录当前文件的统计数据 */
		total_chars += chars;
		total_words += words;
		total_lines += lines;
	}

	if (argc > 1)
		printf("%8d%8d%8d total\n", total_chars, total_words, total_lines);

	return 0;
}
```



修改后重新编译运行:

```
make
./count-words a.txt b.txt # a.txt, b.txt 作为输入
```


如果不指定输入（具体是修改yyin全局变量），程序默认使用标准输入；如果要修改输入，我们可以通过yyrestart()修改。



### 3.1.3 flex 生成代码流程简析

通常，我们有必要简单的分析下 flex 生成的代码，以帮助我们理解和更好的使用工具。下面简要分析 flex 生成代码的工作流程：

```
/* 
 * 在分析具体代码前，我们先聊一下 flex 的 fl 库。
 * flex工具带有一个微型的 fl 库，它定义了 main(), yywrap() 接口。 其中：
 *  . main() 函数调用 yylex() 做词法分析;
 *  . yywrap() 简单地返回 1。
 *    yywrap() 的作用是，在yylex()发现到达输入数据末尾时，调用 yywrap()，看是否还有数据，如果有，yywrap() 应该
 *    返回0，否则返回1。
 *
 * 如果我们的flex程序，不自己实现 main() 和 yywrap()，则在编译时，可以给 gcc 指定 -lfl 选项。另外，可以通过给 
 * flex 传递 --noyywrap 选项，或者在 flex 程序中，指定 %option noyywrap 来告诉flex ，我们不调用 yywrap() ，
 * 以此来屏蔽编译链接报错。
 */
/* 接下来，进入具体的代码流程分析 */
main()
	yylex() /* 进入词法分析器入口 */
		/* yylex() 初次调用的初始化。后续 yylex() 调用会在之前的上下文下继续工作。 */
		if ( !(yy_init) )
		{
			(yy_init) = 1;
			...
			if ( ! (yy_start) )
				(yy_start) = 1;	/* first start state */
				
			if ( ! yyin ) /* 没有设定输入, 默认将 stdin 作为输入 */
				yyin = stdin;
				
			if ( ! yyout ) /* 没有设定输出, 默认将 stdout 作为输入 */
				yyout = stdout;
				
			/* 输入缓冲初始化 */
			if ( ! YY_CURRENT_BUFFER ) {
				yyensure_buffer_stack (); /* 创建 yy_buffer_state 输入缓冲管理对象指针栈 */
				YY_CURRENT_BUFFER_LVALUE =
					yy_create_buffer(yyin,YY_BUF_SIZE ); /* 创建栈顶 YY_BUF_SIZE 大小的输入缓冲 */
			}
			
			/*
		 	 * 获取栈顶输入缓冲如下状态:
		 	 * . yy_n_chars: 读到栈顶输入缓冲空间的字符个数
		 	 * . yytext, yy_c_buf_p: 栈顶输入缓冲空间当前位置指针(char *)
		 	 * . yyin: 输入缓冲输入文件
		 	 * . yy_hold_char: 栈顶输入缓冲空间当前字符
		 	 */
			yy_load_buffer_state( );
		}
		
		/* 扫描循环，直到输入结束 */
		while ( /*CONSTCOND*/1 )		/* loops until end-of-file is reached */
		{
			/* 正则匹配状态机循环 */
			yy_current_state = (yy_start);
		yy_match:
			do {
				YY_CHAR yy_c = yy_ec[YY_SC_TO_UI(*yy_cp)] ;
				...
				yy_current_state = yy_nxt[yy_base[yy_current_state] + (unsigned int) yy_c];
				++yy_cp;
			}
			while ( yy_base[yy_current_state] != 17 );
			
			...
			
			/* 一个正则模式匹配完成的后惯例动作:
			 * yytext: 当前正则模式匹配的内容
			 * yyleng: 当前正则模式匹配内容长度
			 * yy_hold_char: 当前正则模式匹配内容的最后一个字符(也即当前字符)
			 * *yy_cp = '\0';
			 * yy_c_buf_p: 下一个待解析字符位置指针
			 */
			YY_DO_BEFORE_ACTION;
		do_action:	/* This label is used only to access EOF actions. */
			/* 状态机正则匹配结束，按匹配的正则做用户定义的动作 */
			switch ( yy_act )
			{ /* beginning of action switch */
			...
			case 1:
				YY_RULE_SETUP
#line 18 "calculator.l"
				{ return ADD; } /* 正则模式匹配时，执行的C代码 */
				YY_BREAK
			...
			default:
				YY_FATAL_ERROR(
					"fatal flex scanner internal error--no action found" );
			}
		}
```

其实 yylex() 的工作逻辑很简单，可以总结如下：

```
while (1)
	从输入读取数据
	按正则模式匹配输入数据
	如果有匹配的模式，执行匹配模式的C代码
	否则，报错
```

注意到，yylex() 是有返回值的，不出错的情形下，它返回匹配模式的 token ，这就是它可以和 bison 生成代码一起协作的基础。



### 3.1.4 flex 小结

上面我们简单介绍了 flex 的基础用法，但很多时候，这些并不足够。下面列举几个对我们日常很常见也很有用的 flex 用法。



### 3.1.4.1 option 选项

##### (1) 生成可重入词法分析器。

一方面，生成的词法分析器代码，有很多全局变量； 另一方面，在词法分析器入口 yylex() 返回后，下一次调用会接着使用上一次运行后的上下文继续执行。以上两点，不能满足要求可重入的调用的上下文。此时，我们可以通过%option reentrant 选项来生成可重入的词法分析器。此时，我们通过如下代码片段构建可重入的词法分析器：

```
yyscan_t scanner;

yylex_init(&scanner) / yylex_init_extra(extra, &scanner); /* 创建词法分析器上下文 */
while (yylex(scanner)) { /* 基于独立的上下文进行词法分析 */
	...
}
yylex_destroy(scanner); /* 销毁词法分析器上下文 */
```



##### (2) 改变生成代码函数名。

我们有时候可能不想使用 yylex() 等其它词法分析器接口名，可以通过%option prefix="XXX"作为词法分析器接口名前缀。如：%option prefix="parse_events_"，那生成代码中，yylex() 则变为 parse_events_lex()，当然，还有更多函数名的变换。



##### (3) 自动维护行号代码。

我们可以自己在规则中，更新 yylineno 来维护行号。当然，也可以通过 %option yylineno选项，让 flex 帮我们自动生成行号维护代码。



##### (4) 与 bison 协同工作选项。

默认生成的代码，yylex() 函数是没有参数的，除了可以通过%option reentrant选项来增加词法分析器的上下文参数外，我们还可以通过%option bison-bridge和%option bison-locations来改变 yylex() 原型：

```
%option bison-bridge: 为 yylex() 增加参数 YYSTYPE *yylval_param, 用来记录词法分析器解析的 token 的值。
%option bison-locations: 为 yylex() 增加参数 YYLTYPE *yylloc_param, 用来存储行列信息。
```



如果有以下选项配置:

```
%option reentrant
%option bison-bridge
%option bison-locations
...
%%
...
%%
...
```

则生成的 yylex() 函数原型为：

```
int yylex(YYSTYPE *yylval_param, YYLTYPE *yylloc_param, yyscan_t yyscanner);
```



### 3.1.4.2 定义

类似于C中的宏定义，主要是将正则规则中重复的部分抽离出来，免得重写。我们看一个例子：

```
group		[^,{}/]*[{][^}]*[}][^,{}/]*
%%
{group}		{
			BEGIN(INITIAL);
			REWIND(0);
		}
%%
...
```



上例中，在第一部分定义了 group ，然后再第二部分规则中引用，使用 {} 括起来。



### 3.1.4.3 定义特定状态下才会执行的规则

```

%x IFILE

%%

^"#"[ \t]*include[ \t]*[\"<] { BEGIN IFILE; }

<IFILE>[^ \t\n\">]+	{
						{
							int c;
							while ((c = input()) && c != '\n');
						}
						yylineno++;
						if (!newfile(yytext))
							yyterminate(); /* no such file */
						BEGIN INITIAL;
					}

<IFILE>.|\n			{
						fprintf(stderr, "%4d bad include line\n", yylineno);
						yyterminate();
					}

<<EOF>>				{ if (!popfile()) yyterminate(); }

^.					{ fprintf(yyout, "%4d %s", yylineno, yytext); }
^\n					{ fprintf(yyout, "%4d %s", yylineno++, yytext); }
\n					{ ECHO; yylineno++; }
.					{ ECHO; }

%%
...
```



上面我们通过 %x 定义了一个 exclusive 的状态 IFILE，在该状态下，只有以 <IFILE> 开头的规则才会被执行，它用来解析 #include 预处理符号。其中，特殊符号<<EOF>>表示遇到文件结尾；词法分析器用 input() 从输入读取一个字符，yylineno 记录行号，yytext 记录当前匹配的文本，ECHO 回显匹配的文本。
词法分析器的初始状态为INITIAL(即0)，可以通过YY_START或YYSTATE获取当前状态，通过BEGIN来切换当前状态，如上例中的BEGIN IFILE;来切换词法分析器的状态为IFILE，在状态IFILE下，只有以<IFILE>开头的规则才会被执行。
另外还可以通过%s定义可共享状态。假设我们通过%s SS定义了状态SS，和%x定义不同的是，除了将<SS>开头的规则限制在只能在SS状态下执行外，而剩余的其它规则，也可以在SS状态下执行。
通常来讲，%x是更加有用的，因为它将分析限制于特定的上下文，这可以简化我们词法分析器的设计。



## 3.2 bison

### 3.2.1 bison 简介

bison 基于给定的语法，来生成一个可以识别这个语法中有效语句的语法分析器。我们简单的看一下语法分析器的生成流程：

```
# 生成语法分析器
       bison -d
XXX.y ==========> XXX.tab.l, XXX.tab.h

# 与词法分析器协作分析语法
                      tokens
                 ----------------
                |                |
                |                V
            词法分析器        语法分析器
                ^                |   
                |                V
            输入数据流      合乎语法的语句
```



### 3.2.2 flex + bison 使用例子

#### (1) 初次使用，先运行如下命令安装 bison：

```
sudo apt-get install bison
```



#### (2) 编写 bison 程序。

我们先来说一下 bison 程序(XXX.y)的编写规则，bison 程序分为3个部分：

```
定义部分：
包含选项(option)，文字块（%{和%}、%code），声明（%union,%start,%token,%type,%left,%right,%nonassoc等）。
%%
规则部分：
包含语法规则和规则匹配时执行的C代码。
%{和%}之间的部分，原封不动的拷贝到 yyparse() 中。
%%
用户子程序部分：
这个部分通常包含语法规则匹配时，执行的C代码调用的函数。
本部分原封不动的拷贝到C代码中。
```



这3个部分，用2个%%分隔，前2个部分是必须的，但它们的内容可以为空，第3部分和它之前的%%可以省略。
接下来，我们一个简单计算器为例，构建计算器的 flex 和 bison 程序如下。

```
%{
/*
 * calculator.l
 */

#include "calculator.tab.h"
%}

%%

"+"	{ return ADD; }
"-"	{ return SUB; }
"*"	{ return MUL; }
"/"	{ return DIV; }
"|"	{ return ABS; }
"("	{ return OP; }
")"	{ return CP; }
[0-9]+	{ yylval = atoi(yytext); return NUMBER; }

\n	{ return EOL; }
"//".*
[ \t]	{ /* ignore white space */ }
.	{ printf("Mystery character %c\n", *yytext); }

%%
```



其中 yylval 记录当前识别到的 token 的值，可以导出给语法分析器使用。

```
%{
/*
 * calculator.y
 */

#include <stdio.h>

extern int yylex(void);

void yyerror(char *s);
%}

/* declare tokens */
%token NUMBER
%token ADD SUB MUL DIV ABS
%token OP CP
%token EOL

%%

callist: /* nothing */
 | callist exp EOL { printf(" = %d\n", $2); }
 ;

exp: factor
 | exp ADD factor { $$ = $1 + $3; }
 | exp SUB factor { $$ = $1 - $3; }
 ;

factor: term
 | factor MUL term { $$ = $1 * $3; }
 | factor DIV term { $$ = $1 / $3; }
 ;

term: NUMBER
 | ABS term { $$ = $2 >= 0? $2 : - $2; }
 | OP exp CP { $$ = $2; }
 ;

%%

int main(int argc, char *argv[])
{
	yyparse();
}

void yyerror(char *s)
{
	fprintf(stderr, "error: %s\n", s);
}
```

上面 bison 程序中的 $$, $1,... 等是用描述每条语法规则中语法符号的值，$$ 表示规则左边符号的值，$1,$2,...依次表示规则右边第1个，第2个，...符号的值。
接下来编写用来编译的 Makefile ：

```
calculator: calculator.l calculator.y
	bison -d calculator.y
	flex calculator.l
	gcc -o calculator calculator.tab.c lex.yy.c -lfl

clean:
	-rm -f calculator lex.yy.c *.tab.*
```

编译运行：

```
make # 编译生成计算器程序 calculator
./calculator # 运行计算器程序，按 Ctrl + D 结束数据输入
```



### 3.2.3 flex 与 bison 协作流程简析

```
main() /* calculator.tab.c: bison -d 生成的代码 */
	yyparse()
		...
		yychar = YYEMPTY; /* Cause a token to be read.  */
		goto yysetstate;
		...
	yynewstate: /* 下一状态 */
	/* In all cases, when you get here, the value and location stacks
	   have just been pushed.  So pushing a state here evens the stacks.  */
		yyssp++;
	
	yysetstate:
		...
		yyn = yypact[yystate];
		...
		if (yychar == YYEMPTY)
		{
			YYDPRINTF ((stderr, "Reading a token: "));
			yychar = yylex (); /* 调用词法分析器解析一个token */
				/* 
				 * 参看 3.1.3 小节，词法分析器工作流程
				 */
		}
		
		...
		
		/* Discard the shifted token.  */
		yychar = YYEMPTY;
		yystate = yyn;
		YY_IGNORE_MAYBE_UNINITIALIZED_BEGIN
		*++yyvsp = yylval;
		YY_IGNORE_MAYBE_UNINITIALIZED_END
	
		goto yynewstate; /* 进入下一分析状态 */
```



### 3.2.4 bison 小结

上面我们简单介绍了 bison 的基础用法，但很多时候，这些并不足够。下面列举几个对我们日常很常见也很有用的 bison 用法。



#### 3.2.4.1 option 选项

(1) 要求 bison 版本。

```
%require "2.4"
%%
...
%%
...
```

(2) 自定义语法分析器入口 yyparse() 函数原型。

默认生成语法分析器入口 yyparse() 函数是没有参数的，但有时候，我们需要给它传递参数，这时我们可以通过%parse-param选项来自定义参数列表。如：

```
%parse-param {void *_parse_state}
%parse-param {void *scanner}
%%
...
%%
...
```

则 yyparse() 函数的原型定义为：

```
int yyparse (void *_parse_state, void *scanner);
```



(3) 生成可重入的语法分析器。

```
%define api.pure
%%
...
%%
...
```

还可以使用%pure-parser代替%define api.pure。这两个选项通常结合%parse-param使用，给 yyparse() 传递参数。



(4) 定义词法分析器解析符号的ID。

```
/* declare tokens */
%token NUMBER
%token ADD SUB MUL DIV ABS
%token OP CP
%token EOL

%%
...
%%
...
```

上面的 bison 程序，会在 *.tab.h 中对应生成如下的 token 定义：

```
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    NUMBER = 258,
    ADD = 259,
    SUB = 260,
    MUL = 261,
    DIV = 262,
    ABS = 263,
    OP = 264,
    CP = 265,
    EOL = 266
  };
#endif
```

%token 用来定义 token 编号，给词法分析器的 token 编号。bison 以 258 为生成符号的起始编号，避免和 ascii 值冲突。另一种 token 编号的使用方法是单引号内含字符的方式，如 ‘+’ ，则用 + 的 ascii 值作为其 token 编号，不必额外定义。



(5) 定义语法分析器规则中符号的数值类型。

在语法分析的规则中，有时候需要通过 %union 指定符号的数值类型。我们看个简单的例子：

```
%union {
  struct ast *a;
  double d;
}

/* declare tokens */
%token <d> NUMBER
%token EOL

%type <a> exp factor term

%%
...
%%
...
```

其中，用 %union 声明被转化为如下C代码段：

```
/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED

union YYSTYPE
{
#line 8 "calculator.y" /* yacc.c:1909  */

	struct ast *a;
	double d;

#line 64 "calculator.tab.h" /* yacc.c:1909  */
};

typedef union YYSTYPE YYSTYPE;
# define YYSTYPE_IS_TRIVIAL 1
# define YYSTYPE_IS_DECLARED 1
#endif
```

也即定义为 YYSTYPE 类型。如果不声明%union，则 YYSTYPE 定义为 int 。声明%token <d> NUMBER表示，语法符号NUMBER的数据类型为，声明%union中，数据成员d的数据类型double；声明%type <a> exp factor term表示，语法符号exp,factor,term的数据类型为，声明%union中，数据成员a的数据类型struct ast *。

语法会存在二义性，简单来讲，语法二义性就是一段输入，可匹配到不同的语法规则情形。本文对语法的二义性未做描述。



## 4 后记

人的精力时间总是有限的，我认为我们学习一样技能，总是、也应该是出于某种目的的。对于那些有明显规律的(可用正则表达式和语法描述的)输入，flex 和 bison 可以极大地提高我们的生产效率、代码的可维护性。对于更加复杂的输入，手工编写的代码，阅读、调试困难，可维护度、扩展性极差，这时候应该利用 flex 和 bison，它们都是久经历史考验的，只要能正确设计正则规则、语法规则，它们就能帮我们保证程序的正确性和效率。
本文仅对 flex 和 bison 做了简单基础性地描述，更多的细节，以及对齐内部的实现原理等方面未做展开，读者可阅读后面的资料，进行补充。



## 5 推荐阅读 & 参考资料

参考资料：

```
[1]《flex & bison》, John R. Levine
[2] [flex](https://www.gnu.org/savannah-checkouts/gnu/www/software/flex/flex.html)
[3] [GNU Bison](https://www.gnu.org/software/bison/)
```

推荐阅读：

```
《A Retargetable C Compiler_Design and Implementation》
《Advanced Compiler Design and Implementation》
《Building an Optimizing Compiler》
《Compiler Construction Principles And Practice》
《Compiler Design in C》
《Compilers Principles Techniques and Tools》
《Crafting a Compiler》
《Engineering a Compiler》
《Introduction to Compiler Construction》
《Language Implementation Patterns》
《Modern Compiler Implementation in C》
《Modern Compiler Design》
《Programming Language Pragmatics》
《The Implementation of Functional Programming Languages》
《计算机程序的构造和解释》
```

