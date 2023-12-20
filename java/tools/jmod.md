# jmod

示例：

jmod create --class-path target/classes ./jmod/PackageDemo-1.0.0.jmod





用法: jmod (create|extract|list|describe|hash) <选项> <jmod 文件>

主操作模式:
  create    - 创建新的 jmod 档案
  extract   - 从档案中提取所有文件
  list      - 输出所有条目的名称
  describe  - 输出模块详细信息
  hash      - 记录绑定模块的散列。

 Option                              Description
------                              -----------
  -?, -h, --help                      输出此帮助消息
  --class-path <path>                 包含类的应用程序 jar 文件|目录
  --cmds <path>                       本机命令的位置
  --config <path>                     用户可编辑配置文件的位置
  --dir <path>                        提取操作的目标目录
  --dry-run                           散列模式的模拟运行
  --exclude <pattern-list>            排除与所提供逗号分隔的模式列表匹配的文件, 每个元素使用以下格式之一:
                                        <glob 模式>, glob:<glob 模式> 或 regex:<正则
                                        表达式模式>
  --hash-modules <regex-pattern>      计算和记录散列, 以将打包模块绑定到与指定 <正则表达式模式> 匹配并直接或间
                                        接依赖于的模块。散列记录在所创建的 JMOD 文件中, 或者记录在
                                        jmod hash 命令指定的模块路径的 JMOD 文件或模块化 JAR
                                        中。
  --header-files <path>               标头文件的位置
  --help-extra                        输出额外选项的帮助
  --legal-notices <path>              法律声明位置
  --libs <path>                       本机库的位置
  --main-class <String: class-name>   主类
  --man-pages <path>                  帮助页的位置
  --module-version <module-version>   模块版本
  -p, --module-path <path>            模块路径
  --target-platform <String: target-  目标平台
    platform>
  --version                           版本信息
  @<filename>                         从指定文件读取选项





