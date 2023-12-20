## Windows命令删除目录中的文件及子文件夹

比如,有一个名为变量的变量`%pathtofolder%`,因为它表明它是文件夹的完整路径.

我想删除此目录中的每个文件和子文件夹,但不删除目录本身.

但是,可能会出现"此文件/文件夹已在使用中"的错误...当发生这种情况时,它应该继续并跳过该文件/文件夹.

任何人都可以给我一些命令吗？



[`rmdir`](https://technet.microsoft.com/en-us/library/bb490990.aspx) 是我最喜欢的工作命令.它适用于删除包含子文件夹的大型文件和文件夹.未创建备份,因此请确保在运行此命令之前已安全地复制了文件.

```
RMDIR "FOLDERNAME" /S /Q
```

这会以静默方式删除文件夹以及所有文件和子文件夹.



- 这似乎消除了OP想要保留的路径. (61认同)
- 为了不删除文件夹本身,请将其作为当前目录,然后使用"." 作为FOLDERNAME.之后重新创建它不一定是相同的,因为ACL可能会丢失.`cd"FOLDERNAME"``RD./ S/Q` (10认同)
- 这将删除文件夹本身，因此您需要添加另一个命令：`md FOLDERNAME`（请参见下面@rakoczyn的回答）。 (4认同)
- 你在这里创建"备份"是什么意思？我看不到任何会产生任何备份的东西...... (4认同)

------

[Iai*_*ain ](https://qa.1r1g.com/sf/users/18699971/) 242



您可以使用此shell脚本清理`C:\Temp` [源中](https://qa.1r1g.com/sf/answers/105205481/)的文件夹和文件:

```
del /q "C:\Temp\*"
FOR /D %%p IN ("C:\Temp\*.*") DO rmdir "%%p" /s /q
```

创建包含上述命令的批处理文件(例如,delete.bat).转到delete.bat文件所在的位置,然后运行命令:delete.bat



- 我尝试了但遗憾的是文件未删除,只删除了子文件夹.例如,我有一个名为Test的文件夹.它包含2个名为"Ripon"和"Wasim"的文件夹,它包含一个名为"riponalwasim.txt"的文件.子文件夹Ripon和Wasim已删除,但riponalwasim.txt未删除. (20认同)
- 如果您希望直接从命令行运行,例如`%p`,请使用单个百分号 (15认同)
- 遗憾的是，这需要2行，但至少行得通！我已经厌倦了寻找一个可行的“单线”，并且会喜欢这个，谢谢！ (2认同)
- 您可以使用'&&'运算符在一行中执行两个命令而不使用bat文件(注意单个'%'而不是'%%').del/s/q"myfolder\*"&& FOR/D%p IN("myfolder\*")DO rmdir"%p"/ s/q (2认同)

------

[woj*_*rak ](https://qa.1r1g.com/sf/users/88859011/) 84



我能想到的最简单的解决方案是删除整个目录

```
RD /S /Q folderPath
```

然后再次创建此目录:

```
MD folderPath
```



- 此外,糟糕的解决方案,如果目录具有特殊权限,你只需要*ahem*(哎呀) (22认同)
- 这是一个糟糕的解决方案,因为它在目录节点的文件系统上引入了竞争,即如果紧接执行后第二个命令可能会失败. (13认同)
- 如果您允许删除但不创建新文件夹怎么办？我希望它可能:) (5认同)
- RD 和 RMDIR 是相同的命令，既然您在有人说出相同答案的 9 个月后发布了此答案，为什么人们会给您积分？ (2认同)

------

[fox*_*ive ](https://qa.1r1g.com/sf/users/160960201/) 51



这将删除文件夹和文件,并将文件夹留在后面.

```
pushd "%pathtofolder%" && (rd /s /q "%pathtofolder%" 2>nul & popd)
```



- 我讨厌Windows无法删除某些程序正在使用的文件和文件夹.而且我喜欢你在这个单行中用你的方式. (6认同)
- 对于任何不确定这种方法的人:它肯定不会重新创建目录.没有`2> nul`,它输出"进程无法访问该文件,因为它正被另一个进程使用." (4认同)
- @mlvljr仅当您要保留父文件夹时.`RD /？`将显示清空文件夹的简单方法. (3认同)
- 所以这个超级魔法就是我们如何在 Windows 中清空文件夹？？感人的 ：） (2认同)
- 如果您需要回到起始位置，这很好，否则只需 `CD mypath &amp;&amp; RD /S .` 就足够了。我不喜欢你必须重复你的路径两次，因为长路径变得难以阅读。所以我会在开头添加一个`set p="mypath"`。 (2认同)

------

[the*_*p3r ](https://qa.1r1g.com/sf/users/11064861/) 37



```
@ECHO OFF

Set dir=path-to-dir

Echo Deleting all files from %dir%
del %dir%\* /F /Q

Echo Deleting all folders from %dir%
for /d %%p in (%dir%\*) Do rd /Q /S "%%p"
@echo Folder deleted.


exit
```

...删除给定目录下的所有文件和文件夹,但不删除目录本身.



- 这应该是正确的答案..只需简单.. deletemyfoldercontents.bat文件..弹出你的路径,它的工作就像一个魅力.. (3认同)
- __DEL__命令行缺少选项'/ A'来删除具有隐藏属性集的文件，并且`％dir％\ *`应该用双引号括起来，例如'“％dir％\ *”`才能用于包含空格的目录或以下字符之一“＆（）[] {} ^ =;！'+，`〜''。并且__FOR__命令行应修改为`for / F“ eol = | delims =” %% I in（'dir“％dir％\ *” / AD / B 2 ^&gt; nul'）do rd / Q / S“由于__FOR__而导致的％dir％\ %% I“`将忽略具有隐藏属性集的目录。带有选项/ AD / B的__DIR__输出仅具有名称的__all__目录。顺便说一句：`dir`不是环境变量的好名字。 (2认同)

------

[O.B*_*adr ](https://qa.1r1g.com/sf/users/148035051/) 29



```
CD [Your_Folder]
RMDIR /S /Q .
```

您将收到一条错误消息,告诉您RMDIR命令无法访问当前文件夹,因此无法删除它.

**更新**:

从[这个](https://qa.1r1g.com/sf/ask/137605121/#comment76234643_32464925)有用的评论(感谢[Moritz Both](https://qa.1r1g.com/sf/users/20784921/)),您可以添加`&&`,因此`RMDIR`如果`CD`命令失败将无法运行(例如错误的目录名称):

```
CD [Your_Folder] && RMDIR /S /Q .
```

从[Windows命令行参考](https://technet.microsoft.com/en-us/library/cc754993(v=ws.11).aspx):

> **/ S:**删除目录树(指定目录及其所有子目录,包括所有文件).
>
> **/ Q:**指定安静模式.删除目录树时不提示确认.(注意/ q仅在指定了/ s时有效.)



- 这是正确的解决方案.在命令之间添加`&&`而不是换行符,你可以安全地从失败的CD中获取. (5认同)

------

[Mof*_*ofi ](https://qa.1r1g.com/sf/users/215219511/) 10



张贴于2018年6月1日，答案没有**不同之处**张贴的单个命令行的**[foxidrive](https://qa.1r1g.com/sf/answers/1196836791/)**，真的会删除所有文件和所有文件夹/目录中`%PathToFolder%`。这就是使用非常简单的单个命令行发布更多答案的原因，以删除文件夹的所有文件和子文件夹以及具有更复杂解决方案的批处理文件，解释了为什么使用**DEL**在 2018-06-01 发布的所有其他答案和**FOR**与**RD**未能彻底清理的文件夹。

------

简单的单命令行解决方案，当然也可以在批处理文件中使用：

```
pushd "%PathToFolder%" 2>nul && ( rd /Q /S "%PathToFolder%" 2>nul & popd )
```

此命令行包含三个依次执行的命令。

第一个命令**PUSHD**将当前目录路径推入堆栈，然后创建`%PathToFolder%`当前目录以运行命令进程。

默认情况下，这也适用于[UNC](https://en.wikipedia.org/wiki/Uniform_Naming_Convention)路径，因为默认情况下启用了命令扩展，在这种情况下，**PUSHD**创建一个指向指定网络资源的临时驱动器号，然后使用新定义的驱动器号更改当前驱动器和目录。

**如果指定的目录根本不存在，\**PUSHD 会\**输出以下错误消息来处理\**STDERR\**：**

**该系统找不到指定的路径。通过将其重定向`2>nul`到设备\**NUL\**来抑制此错误消息。只有当当前命令进程的当前目录更改为指定目录成功，即指定目录完全存在时，才会执行下一个命令\**RD\**。该命令\**RD\**与选择`/Q`和`/S`删除一个目录\**悄悄\**所有\**子目录\**即使指定的目录中，有隐藏属性或具有只读属性设置的文件或文件夹。系统属性永远不会阻止删除文件或文件夹。未删除的有：用作任何正在运行的进程的当前目录的文件夹。如果某个文件夹用作任何正在运行的进程的当前目录，则无法删除该文件夹的整个文件夹树。当前由任何正在运行的进程打开的文件，在文件打开时设置了文件访问权限，以防止在运行的应用程序/进程打开时删除文件。这样一个打开的文件也防止删除整个文件夹树到打开的文件。当前用户没有删除文件/文件夹所需的 (NTFS) 权限的文件/文件夹，这也防止删除此文件/文件夹的文件夹树。此命令行使用不删除文件夹的第一个原因删除指定文件夹的所有文件和子文件夹，但不删除文件夹本身。该文件夹临时成为运行命令进程的当前目录，以防止删除文件夹本身。当然，这会导致命令\**RD\**输出错误消息：该进程无法访问该文件，因为它正被另一个进程使用。\*File\*在这里是错误的术语，因为实际上该文件夹正被另一个进程使用，即执行命令\**RD\**的当前命令进程。好吧，实际上文件夹是文件系统的一个特殊文件，它具有解释此错误消息的文件属性\*目录\*。但我不想太深入地研究文件系统管理。此错误消息与所有其他错误消息一样，可能由于上述三个原因而发生，通过将其`2>nul`从句柄\**STDERR\**重定向到设备\**NUL\**来抑制。第三个命令\**POPD\**的执行独立于命令\**RD\**的退出值。\**POPD\**从栈中弹出\**PUSHD\**推送的目录路径，将运行命令进程的当前目录改为该目录，即恢复初始当前目录。在 UNC 文件夹路径的情况下，\**POPD\**会删除由\**PUSHD\**创建的临时驱动器号。\**注意：\** 如果初始当前目录是要清理的目录的子目录而不再存在，则\**POPD\**可能会以静默方式无法恢复初始当前目录。在这种特殊情况下`%PathToFolder%`仍然是当前目录。因此，建议不要从`%PathToFolder%`.\**一个更\*有趣的\*事实：\** 我尝试了命令行也使用 UNC 路径，方法是通过共享`C:\Temp`名称共享本地目录`Temp`并使用`\\%COMPUTERNAME%\Temp\CleanTest`分配给`PathToFolder`Windows 7环境变量的UNC 路径。如果运行命令行的当前目录是共享本地的子目录使用 UNC 路径访问的文件夹，即`C:\Temp\CleanTest\Subfolder1`，`Subfolder1`被\**RD\**删除，并且下一个\**POPD\**无法静默地`C:\Temp\CleanTest\Subfolder1`重新创建当前目录，导致`Z:\CleanTest`保留为正在运行的命令进程的当前目录。所以在这种非常非常特殊的情况下，临时驱动器号会一直保留，直到当前目录被更改，例如`cd /D %SystemRoot%`到真正存在的本地目录。不幸的是，如果\**POPD\**无法恢复初始当前目录，则它不会以大于 0 的值退出，从而无法仅使用\**POPD\**的退出代码来检测这种非常特殊的错误情况。但是，可以假设没有人遇到过这种非常特殊的错误情况，因为 UNC 路径通常不用于访问本地文件和文件夹。为了更好地理解所使用的命令，打开一个命令提示符窗口，在那里执行以下命令，并仔细阅读为每个命令显示的帮助。`pushd /?``popd /?``rd /?`[使用Windows批处理文件的多个命令一行](https://qa.1r1g.com/sf/answers/1774080661/)介绍了经营者`&&`和`&`用在这里。接下来，让我们看看在使用该命令的批处理文件解决方案\**DEL\**删除的文件`%PathToFolder%`和\**FOR\**和\**RD\**删除子文件夹中`%PathToFolder%`。`@echo off setlocal EnableExtensions DisableDelayedExpansion rem Clean the folder for temporary files if environment variable rem PathToFolder is not defined already outside this batch file. if not defined PathToFolder set "PathToFolder=%TEMP%" rem Remove all double quotes from folder path. set "PathToFolder=%PathToFolder:"=%" rem Did the folder path consist only of double quotes? if not defined PathToFolder goto EndCleanFolder rem Remove a backslash at end of folder path. if "%PathToFolder:~-1%" == "\" set "PathToFolder=%PathToFolder:~0,-1%" rem Did the folder path consist only of a backslash (with one or more double quotes)? if not defined PathToFolder goto EndCleanFolder rem Delete all files in specified folder including files with hidden rem or read-only attribute set, except the files currently opened by rem a running process which prevents deletion of the file while being rem opened by the application, or on which the current user has not rem the required permissions to delete the file. del /A /F /Q "%PathToFolder%\*" >nul 2>nul rem Delete all subfolders in specified folder including those with hidden rem attribute set recursive with all files and subfolders, except folders rem being the current directory of any running process which prevents the rem deletion of the folder and all folders above, folders containing a file rem opened by the application which prevents deletion of the file and the rem entire folder structure to this file, or on which the current user has rem not the required permissions to delete a folder or file in folder tree rem to delete. for /F "eol=| delims=" %%I in ('dir "%PathToFolder%\*" /AD /B 2^>nul') do rd /Q /S "%PathToFolder%\%%I" 2>nul :EndCleanFolder endlocal `批处理文件首先确保环境变量`PathToFolder`确实是用文件夹路径定义的，没有双引号，末尾没有反斜杠。最后的反斜杠不会有问题，但文件夹路径中的双引号可能会出现问题，因为`PathToFolder`在批处理文件执行期间，值与其他字符串连接在一起。重要的是两行：`del /A /F /Q "%PathToFolder%\*" >nul 2>nul for /F "eol=| delims=" %%I in ('dir "%PathToFolder%\*" /AD /B 2^>nul') do rd /Q /S "%PathToFolder%\%%I" 2>nul `\**DEL\**命令用于删除指定目录下的所有文件。该选项`/A`对于处理真正的所有文件是必要的，包括具有隐藏属性的文件，\**DEL\**将在不使用选项的情况下忽略该属性`/A`。该选项`/F`是强制删除具有只读属性集的文件所必需的。该选项 `/Q`对于运行多个文件的静默删除而不提示用户是否真正删除多个文件是必要的。`>nul`有必要将写入处理\**STDOUT\**的文件名的输出重定向到设备\**NUL\**，因为当前打开了文件或用户没有删除文件的权限，因此无法删除其\**NUL\**。`2>nul`必须将无法从句柄\**STDERR\**删除的每个文件的错误消息输出重定向到设备\**NUL\**。\**FOR\**和\**RD\**命令用于删除指定目录下的所有子目录。但是`for /D`没有使用，因为在这种情况下\**FOR\**忽略了具有隐藏属性集的子目录。因此，`for /F`用于在后台启动的单独命令进程中运行以下命令行`%ComSpec% /c`：`dir "%PathToFolder%\*" /AD /B 2>nul `\**DIR\**以裸格式输出，因为`/B`目录条目具有属性`D`，即指定目录中所有子目录的名称独立于其他属性，例如没有路径的隐藏属性。`2>nul`用于将\**DIR\**在没有从句柄\**STDERR 中\**找到的\**目录\**上输出的错误消息重定向到设备\**NUL\**。重定向操作符`>`必须`^`在\**FOR\**命令行上使用插入字符 , 进行转义，当 Windows 命令解释器在执行在单独的命令进程中执行嵌入命令行的命令\**FOR\**之前处理此命令行时，将被解释为文字字符`dir`在后台。\**FOR\**处理为处理已启动命令进程的\**STDOUT\**而写入的捕获输出，这些\**输出\**是没有路径的子目录的名称，并且从不包含在双引号中。\**带有选项的\*\*FOR 会\*\*`/F`忽略此处不会出现的空行，因为带有选项的\*\*DIR\*\*`/B`不会输出空行。\**\**\*\*FOR\*\*也会忽略以分号开头的行，分号是默认的行尾字符。目录名可以以分号开头。出于这个原因`eol=|`，用于将竖线字符定义为行尾字符，目录或文件的名称中不能包含该字符。\*\*FOR\*\*将使用空格和水平制表符作为分隔符将行拆分为子字符串，并将仅将第一个空格/制表符分隔的字符串分配给指定的循环变量`I`。由于目录名称可以包含一个或多个空格，因此这里不需要这种拆分行为。因此`delims=`用于定义一个空的分隔符列表以禁用行拆分行为并分配给循环变量`I`，始终是完整的目录名称。命令\*\*FOR\*\*为每个没有路径的目录名称运行命令\*\*RD\*\*，这就是为什么在\*\*RD\*\*命令行上必须再次指定文件夹路径并与子文件夹名称连接的原因。要了解使用的命令及其工作原理，请打开命令提示符窗口，在那里执行以下命令，并仔细阅读为每个命令显示的所有帮助页面。`del /?``dir /?``echo /?``endlocal /?``for /?``goto /?``if /?``rd /?``rem /?``set /?``setlocal /?`\****



***\*[Sir\*_\*dda ](https://qa.1r1g.com/sf/users/144053171/) 9RD代表REMOVE目录./ S:除文件夹本身外,还删除所有文件和子文件夹.使用此选项可删除整个文件夹树./ Q:安静 - 不显示YN确认示例:`RD /S /Q C:/folder_path/here `被赞是唯一一个解释 `/S` 和 `/Q` 含义的人 (2认同)No.`rd`相当于`rmdir`并将删除目录本身,而不是OP想要的. (2认同)[rhe\*_\*lem ](https://qa.1r1g.com/sf/users/35849761/) 8我用Powershell`Remove-Item c:\scripts\* -recurse `它将删除文件夹的内容,而不是文件夹本身.从批处理命令行：`powershell -Command“ Remove-Item'PathToMyDirectory \ \*'-Recurse -Force”` (2认同)[Fil\*_\*ikj ](https://qa.1r1g.com/sf/users/164921501/) 8使用[记事本](http://en.wikipedia.org/wiki/Notepad_(software))创建一个文本文档并复制/粘贴：`rmdir /s/q "%temp%" mkdir "%temp%" `选择\*另存为\*和文件名：delete_temp.bat保存类型：所有文件，然后单击Save按钮。它适用于任何类型的帐户（管理员或标准用户）。运行它！我在这个例子中使用了一个临时变量，但你可以使用任何其他变量！PS：仅适用于 Windows 操作系统！\****

***\*归档时间：\*\*13 年，4 月 前\*\*查看次数：\*\*921944 次\*\*最近记录：\*\*[3 年，7 月 前](https://qa.1r1g.com/sf/ask/137605121/?lastactivity)\*\*[如何通过cmd调用删除文件夹中的所有文件和文件夹 77](https://qa.1r1g.com/sf/ask/105203941/)
[deltree发生了什么事,它的替代品是什么？ 76](https://qa.1r1g.com/sf/ask/23722681/)
[如何通过命令提示符清空recyclebin？ 76](https://qa.1r1g.com/sf/ask/646726741/)
[使用Windows批处理文件的多行命令的单行 13](https://qa.1r1g.com/sf/ask/1774034601/)
[更多相关链接](https://qa.1r1g.com/sf/ask/linked/id137605121/)相关归档[Node.js/Windows错误:ENOENT,stat'C:\ Users\RT\AppData\Roaming \npm' 979](https://qa.1r1g.com/sf/ask/1756529351/)
[如何使用Python查找脚本的目录？ 451](https://qa.1r1g.com/sf/ask/345436451/)
[如何在Windows批处理文件中获取Java程序的退出状态 25](https://qa.1r1g.com/sf/ask/62094651/)
[如何在java中检查Path是否存在？ 23](https://qa.1r1g.com/sf/ask/34102701/)
[如何判断正在运行批处理文件的Windows和/或cmd.exe的版本？ 14](https://qa.1r1g.com/sf/ask/125491831/)
[在CLI中列出CVS中模块中的所有标签 9](https://qa.1r1g.com/sf/ask/220073311/)
[php exec()在unicode模式下？ 9](https://qa.1r1g.com/sf/ask/933262501/)
[Eclipse终端在某个点之后修剪字符.限制宽度大小 8](https://qa.1r1g.com/sf/ask/3251902531/)
[安装Keras后Anaconda Prompt Stuck/Closing 8](https://qa.1r1g.com/sf/ask/3744164441/)
[Rails Presenters文件夹有什么用？ 7](https://qa.1r1g.com/sf/ask/2552108191/)
难疑归档[什么是"大O"符号的简单英语解释？ 4851](https://qa.1r1g.com/sf/ask/34108091/)
[为什么Java的+ =, - =,\*=,/ =复合赋值运算符需要转换？ 3547](https://qa.1r1g.com/sf/ask/609743361/)
[在一行中初始化ArrayList 2626](https://qa.1r1g.com/sf/ask/70355141/)
[如何在Node.js中退出 1762](https://qa.1r1g.com/sf/ask/368630671/)
[如何检查字符串"StartsWith"是否是另一个字符串？ 1660](https://qa.1r1g.com/sf/ask/45263991/)
[@\*-\*@如何禁用浏览器的集成开发人员工具？ 1652](https://qa.1r1g.com/sf/ask/1518485251/)
[感叹号在功能之前做了什么？ 1190](https://qa.1r1g.com/sf/ask/262892451/)
[为什么有两种方法可以在Git中取消暂存文件？ 1109](https://qa.1r1g.com/sf/ask/484338501/)
[Django会扩展吗？ 1101](https://qa.1r1g.com/sf/ask/62035501/)
[测量Python中经过的时间？ 1031](https://qa.1r1g.com/sf/ask/515956101/)
\****