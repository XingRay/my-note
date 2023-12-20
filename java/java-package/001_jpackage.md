# java 打包工具

## 1 jdeps

```shell
jdeps -h
```

输出:

```shell
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
  -P       -profile             Show profile containing a package.  This option
                                is deprecated and may be removed in a future release.
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
```





## 2 jpackage

```shell
jpackage -h
```

输出:

```shell
用法：jpackage <options>
                        
示例用法:               
--------------
    生成适合主机系统的应用程序包：
        对于模块化应用程序：
            jpackage -n name -p modulePath -m moduleName/className
        对于非模块化应用程序：
            jpackage -i inputDir -n name \
                --main-class className --main-jar myJar.jar
        从预构建的应用程序映像：
            jpackage -n name --app-image appImageDir
    生成应用程序映像：
        对于模块化应用程序：
            jpackage --type app-image -n name -p modulePath \
                -m moduleName/className
        对于非模块化应用程序：
            jpackage --type app-image -i inputDir -n name \
                --main-class className --main-jar myJar.jar
        要为 jlink 提供您自己的选项，请单独运行 jlink：
            jlink --output appRuntimeImage -p modulePath \
                --add-modules moduleName \
                --no-header-files [<additional jlink options>...]
            jpackage --type app-image -n name \
                -m moduleName/className --runtime-image appRuntimeImage
    生成 Java 运行时程序包：
        jpackage -n name --runtime-image <runtime-image>

一般选项：
  @<filename>
          从文件读取选项和/或模式
          可以多次使用此选项。
  --type -t <type>
          要创建的程序包的类型
          有效值为：{"app-image", "exe", "msi"}
          如果未指定此选项，则将创建与平台相关的
          默认类型。
  --app-version <version>
          应用程序和/或程序包的版本
  --copyright <copyright string>
          应用程序的版权
  --description <description string>
          应用程序的说明
  --help -h
          将用法文本输出到输出流并退出，用法文本中包含
          适用于当前平台的每个有效选项的列表和说明
  --icon <file path>
          应用程序包图标的路径
          （绝对路径或相对于当前目录的路径）
  --name -n <name>
          应用程序和/或程序包的名称
  --dest -d <destination path>
          用来放置所生成的输出文件的路径
          （绝对路径或相对于当前目录的路径）
          默认为当前的工作目录。
  --temp <directory path>
          用来创建临时文件的新目录或空白目录的路径
          （绝对路径或相对于当前目录的路径）
          如果指定，则在任务完成时将不删除临时目录，
          必须手动删除临时目录。
          如果未指定，则将创建一个临时目录，
          并在任务完成时删除该临时目录。
  --vendor <vendor string>
          应用程序的供应商
  --verbose
          启用详细的输出
  --version
          将产品版本输出到输出流并退出。

用来创建运行时映像的选项：
  --add-modules <模块名称>[,<模块名称>...]
          要添加的模块的逗号 (",") 分隔列表
          此模块列表连同主模块（如果指定）
          将作为 --add-module 参数传递到 jlink。
          如果未指定，则仅使用主模块（如果指定了 --module），
          或者使用默认的模块集（如果指定了
          --main-jar）。
          可以多次使用此选项。
  --module-path -p <module path>...
          路径的 ; 分隔列表
          每个路径要么是模块的目录，要么是
          模块化 jar 的路径。
          （每个路径可以是绝对路径，也可以是相对于当前目录的路径。）
          可以多次使用此选项。
  --jlink-options <jlink 选项>
          要传递给 jlink 的选项列表（用空格分隔）
          如果未指定，则默认为 "--strip-native-commands
          --strip-debug --no-man-pages --no-header-files"。
          可以多次使用此选项。
  --runtime-image <directory path>
          将复制到应用程序映像的预定义
          运行时映像的路径
          （绝对路径或相对于当前目录的路径）
          如果未指定 --runtime-image，jpackage 将运行 jlink 以
          使用如下选项创建运行时映像：
          --strip-debug、--no-header-files、--no-man-pages 和
          --strip-native-commands。

用来创建应用程序映像的选项：
  --input -i <directory path>
          包含要打包的文件的输入目录的路径
          （绝对路径或相对于当前目录的路径）
          输入目录中的所有文件将打包到
          应用程序映像中。
  --app-content <additional content>[,<additional content>...]
          要添加到应用程序有效负载中的文件和/或
          目录的逗号分隔路径列表。
          此选项可以多次使用。

用来创建应用程序启动程序的选项：
  --add-launcher <launcher name>=<file path>
          启动程序的名称和包含关键字-值对列表的
          属性文件的路径
          （绝对路径或相对于当前目录的路径）
          可以使用关键字 "module"、"main-jar"、"main-class"、"description"、
          "arguments"、"java-options"、"app-version"、"icon"、
          "launcher-as-service"、
          "win-console"、"win-shortcut"、"win-menu"、
          "linux-app-category" 和 "linux-shortcut"。
          这些选项将添加到原始命令行选项中或者用来覆盖
          原始命令行选项，以构建额外的替代启动程序。
          将从命令行选项构建主应用程序启动程序。
          可以使用此选项构建额外的替代启动程序，
          可以多次使用此选项来构建
          多个额外的启动程序。
  --arguments <main class arguments>
          在没有为启动程序提供命令行参数时，
          要传递到主类的命令行参数
          可以多次使用此选项。
  --java-options <java options>
          要传递到 Java 运行时的选项
          可以多次使用此选项。
  --main-class <class name>
          要执行的应用程序主类的限定名称
          只有在指定了 --main-jar 时才能使用此选项。
  --main-jar <main jar file>
          应用程序的主 JAR；包含主类
          （指定为相对于输入路径的路径）
          可以指定 --module 或 --main-jar 选项，但是不能同时指定
          两者。
  --module -m <module name>[/<main class>]
          应用程序的主模块（以及可选的主类）
          此模块必须位于模块路径中。
          如果指定了此选项，则将在 Java 运行时映像中
          链接主模块。可以指定 --module 或 --main-jar 选项，
          但是不能同时指定这两个选项。

用来创建应用程序启动程序的与平台相关的选项：
  --win-console
          为应用程序创建控制台启动程序，应当为
          需要控制台交互的应用程序指定

用来创建应用程序包的选项：
  --about-url <url>
          应用程序主页的 URL
  --app-image <directory path>
          用来构建可安装程序包的
          预定义应用程序映像的位置
          （绝对路径或相对于当前目录的路径）
  --file-associations <file path>
          包含关键字-值对列表的属性文件的路径
          （绝对路径或相对于当前目录的路径）
          可以使用关键字 "extension"、"mime-type"、"icon" 和 "description"
          来描述此关联。
          可以多次使用此选项。
  --install-dir <directory path>
          默认安装位置下面的相对子路径
  --license-file <file path>
          许可证文件的路径
          （绝对路径或相对于当前目录的路径）
  --resource-dir <directory path>
          覆盖 jpackage 资源的路径
          可以通过向该目录中添加替代资源来覆盖 jpackage 的
          图标、模板文件和其他资源。
          （绝对路径或相对于当前目录的路径）
  --runtime-image <directory path>
          要安装的预定义运行时映像的路径
          （绝对路径或相对于当前目录的路径）
          在创建运行时程序包时需要使用选项。
  --launcher-as-service
          请求创建安装程序，以将主
          应用程序启动程序注册为后台服务类型应用程序。

用来创建应用程序包的与平台相关的选项：
  --win-dir-chooser
          添加一个对话框以允许用户选择
          产品的安装目录。
  --win-help-url <url>
          用户可以从中获取更多信息或技术支持的 URL
  --win-menu
          请求为此应用程序添加开始菜单快捷方式
  --win-menu-group <menu group name>
          此应用程序所在的开始菜单组
  --win-per-user-install
          请求基于每个用户执行安装
  --win-shortcut
          请求为此应用程序添加桌面快捷方式
  --win-shortcut-prompt
          添加一个对话框以允许用户选择是否将由安装程序
          创建快捷方式。
  --win-update-url <url>
          可用应用程序更新信息的 URL
  --win-upgrade-uuid <id string>
          与此程序包的升级相关联的 UUID
```



