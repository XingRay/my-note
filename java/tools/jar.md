jar

用法: jar [OPTION...] [ [--release VERSION] [-C dir] files] ...
jar 创建类和资源的档案, 并且可以处理档案中的
单个类或资源或者从档案中还原单个类或资源。

 示例:

 # 创建包含两个类文件的名为 classes.jar 的档案:

 jar --create --file classes.jar Foo.class Bar.class

 # 使用现有的清单创建档案, 其中包含 foo/ 中的所有文件:

 jar --create --file classes.jar --manifest mymanifest -C foo/ .

 # 创建模块化 jar 档案, 其中模块描述符位于

 # classes/module-info.class:

 jar --create --file foo.jar --main-class com.foo.Main --module-version 1.0
     -C foo/ classes resources

 # 将现有的非模块化 jar 更新为模块化 jar:

 jar --update --file foo.jar --main-class com.foo.Main --module-version 1.0
     -C foo/ module-info.class

 # 创建包含多个发行版的 jar, 并将一些文件放在 META-INF/versions/9 目录中:

 jar --create --file mr.jar -C foo classes --release 9 -C foo9 classes

要缩短或简化 jar 命令, 可以在单独的文本文件中指定参数,
并使用 @ 符号作为前缀将此文件传递给 jar 命令。

 示例:

 # 从文件 classes.list 读取附加选项和类文件列表

 jar --create --file my.jar @classes.list


 主操作模式:

  -c, --create               创建档案
  -i, --generate-index=FILE  为指定的 jar 档案生成
                             索引信息
  -t, --list                 列出档案的目录
  -u, --update               更新现有 jar 档案
  -x, --extract              从档案中提取指定的 (或全部) 文件
  -d, --describe-module      输出模块描述符或自动模块名称
      --validate             Validate the contents of the jar archive. This option
                             will validate that the API exported by a multi-release
                             jar archive is consistent across all different release
                             versions.

 在任意模式下有效的操作修饰符:

  -C DIR                     更改为指定的目录并包含
                             以下文件
  -f, --file=FILE            档案文件名。省略时, 基于操作
                             使用 stdin 或 stdout
      --release VERSION      将下面的所有文件都放在
                             jar 的版本化目录中 (即 META-INF/versions/VERSION/)
  -v, --verbose              在标准输出中生成详细输出

 在创建和更新模式下有效的操作修饰符:

  -e, --main-class=CLASSNAME 捆绑到模块化或可执行
                             jar 档案的独立应用程序
                             的应用程序入口点
  -m, --manifest=FILE        包含指定清单文件中的
                             清单信息
  -M, --no-manifest          不为条目创建清单文件
                             这些模块按指定模式匹配并直接或
                             间接依赖于所创建的模块化 jar 或
                             所更新的非模块化 jar
  -p, --module-path          模块被依赖对象的位置, 用于生成
                             散列

 只在创建, 更新和生成索引模式下有效的操作修饰符:

  -0, --no-compress          仅存储; 不使用 ZIP 压缩

 其他选项:

  -?, -h, --help[:compat]    提供此帮助，也可以选择性地提供兼容性帮助
      --help-extra           提供额外选项的帮助
      --version              输出程序版本

 如果模块描述符 'module-info.class' 位于指定目录的
 根目录中, 或者位于 jar 档案本身的根目录中, 则
 该档案是一个模块化 jar。以下操作只在创建模块化 jar,
 或更新现有的非模块化 jar 时有效: '--module-version',
 '--hash-modules' 和 '--module-path'。

 如果为长选项提供了必需参数或可选参数, 则它们对于
 任何对应的短选项也是必需或可选的。





