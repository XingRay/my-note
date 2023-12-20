jdeps 

用法: jdeps <选项> <路径...>]                           
其中 <路径> 可以是 .class 文件, 目录, JAR 文件的路径名。
                                                        
可能的选项包括:                                         
  -h -? --help                  输出此帮助消息          
  -dotoutput <目录>                                     
  --dot-output <目录>            DOT 文件输出的目标目录 
  -s       -summary             仅输出被依赖对象概要。  
  -v       -verbose             输出所有类级别被依赖对象
                                等同于 -verbose:class -filter:none。
  -verbose:package              默认情况下输出程序包级别被依赖对象,
                                其中包括同一程序包中的被依赖对象
  -verbose:class                默认情况下输出类级别被依赖对象,
                                其中包括同一程序包中的被依赖对象
  -apionly
  --api-only                    通过公共类 (包括字段类型, 方法
                                参数类型, 返回类型, 受控异常错误
                                类型等) 的公共和受保护成员的签名
                                限制对 API (即被依赖对象)
                                进行分析。
  -jdkinternals
  --jdk-internals               在 JDK 内部 API 上查找类级别的被依赖对象。
                                除非指定了 -include 选项, 否则默认情况下,
                                它分析 --class-path 上的所有类和输入文件。
                                此选项不能与 -p, -e 和 -s 选项
                                一起使用。
                                警告: 无法访问 JDK 内部 API。
  -cp <路径>
  -classpath <路径>
  --class-path <路径>           指定查找类文件的位置
  --module-path <模块路径>      指定模块路径
  --upgrade-module-path <模块路径>  指定升级模块路径
  --system <java 主目录>        指定替代系统模块路径
  --add-modules <模块名称>[,<模块名称>...]
                                将模块添加到根集以进行分析
  --multi-release <版本>        指定处理多发行版 jar 文件时的
                                版本。<版本> 应为大于等于 9
                                的整数或基数。
  -q       -quiet               隐藏警告消息
  -version --version            版本信息

模块被依赖对象分析选项:
  -m <模块名称>
  --module <模块名称>        指定用于分析的根模块
  --generate-module-info <目录> 在指定目录下生成 module-info.java。
                                将分析指定的 JAR 文件。
                                此选项不能与 --dot-output
                                或 --class-path 一起使用。对打开的
                                模块使用 --generate-open-module 选项。
  --generate-open-module <dir>  以打开模块的方式为指定目录下的
                                指定 JAR 文件生成 module-info.java。
                                此选项不能与 --dot-output 或
                                --class-path 一起使用。
  --check <模块名称>[,<模块名称>...
                                分析指定模块的被依赖对象
                                它输出模块描述符, 分析之后
                                生成的模块被依赖对象以及
                                转换减少之后的图形。它还
                                指示任何未使用的合格导出。
  --list-deps                   列出模块的被依赖对象。它还会
                                输出内部 API 程序包（如果引用）。
                                此选项传递分析类路径和模块路径
                                上的库（如果引用）。
                                将 --no-recursive 选项用于
                                被依赖对象的非传递分析。
  --list-reduced-deps           与 --list-deps 相同, 不列出
                                模块图中的隐式读取维边。
                                如果模块 M1 读取 M2, 并且 M2 需要
                                M3 上的过渡, 则 M1 隐式读取 M3
                                并且不在图中显示。
  --print-module-deps           与 --list-reduced-deps 相同, 输出
                                逗号分隔的模块被依赖对象列表。
                                此输出可由 jlink --add-modules
                                用于创建定制映像, 其中包含
                                这些模块及其过渡被依赖对象。
  --ignore-missing-deps         忽略缺少的被依赖对象。

用于筛选被依赖对象的选项:
  -p <程序包>
  -package <程序包>
  --package <程序包>            查找与给定程序包名称匹配的被依赖对象
                                (可多次指定)。
  -e <正则表达式>
  -regex <正则表达式>
  --regex <正则表达式>               查找与指定模式匹配的被依赖对象。
  --require <模块名称>          查找与给定模块名称匹配的
                                被依赖对象 (可多次指定)。--package,
                                --regex, --requires 是互斥的。
  -f <正则表达式>  -filter <正则表达式>    筛选与指定模式匹配的被依赖对象。
                                    如果多次指定, 则将使用最后一个
                                    被依赖对象。
  -filter:package                   筛选位于同一程序包内的被依赖对象。
                                    这是默认值。
  -filter:archive                   筛选位于同一档案内的被依赖对象。
  -filter:module                筛选位于同一模块内的被依赖对象。
  -filter:none                  不使用 -filter:package 和 -filter:archive 筛选。
                                    通过 -filter 选项指定的筛选
                                    仍旧适用。

  --missing-deps                查找缺少的被依赖对象。此选项
                                不能与 -p、-e 和 -s 选项一起使用。

用于筛选要分析的类的选项:
  -include <正则表达式>             将分析限制为与模式匹配的类
                                    此选项筛选要分析的类的列表。
                                    它可以与向被依赖对象应用模式的
                                -p 和 -e 结合使用
  -P       -profile             显示包含程序包的配置文件
  -R
  --recursive                   递归遍历所有运行时被依赖对象。
                                -R 选项表示 -filter:none。如果指定了 -p、
                                -e、-f 选项，则只分析
                                匹配的被依赖对象。
  --no-recursive                不递归遍历被依赖对象。
  -I
  --inverse                     根据其他指定选项分析被依赖对象,
                                然后查找直接和间接依赖于匹配
                                节点的所有 Artifact。
                                这相当于编译时视图分析的
                                逆向, 输出被依赖对象概要。
                                此选项必须与 --require,
                                --package 或 --regex 选项一起使用。
  --compile-time                过渡被依赖对象的编译时视图,
                                例如 -R 选项的编译时视图。
                                根据其他指定选项分析被依赖对象
                                如果从目录, JAR 文件或模块中
                                找到被依赖对象, 则将分析
                                该所在档案中的所有类。