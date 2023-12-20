jlink

用法: jlink <选项> --module-path <模块路径> --add-modules <模块>[,<模块>...]
可能的选项包括:
      --add-modules <mod>[,<mod>...]    除了初始模块之外要解析的
                                        根模块。<mod> 还可以为 ALL-MODULE-PATH。
      --bind-services                   链接服务提供方模块及其
                                        被依赖对象
  -c, --compress=<0|1|2>                Enable compression of resources:
                                          Level 0: No compression
                                          Level 1: Constant string sharing
                                          Level 2: ZIP
      --disable-plugin <pluginname>     Disable the plugin mentioned
      --endian <little|big>               所生成 jimage
                                          的字节顺序 (默认值: native)
  -h, --help, -?                        输出此帮助消息
      --ignore-signing-information        在映像中链接已签名模块化
                                          JAR 的情况下隐藏致命错误。
                                          已签名模块化 JAR 的签名
                                          相关文件将不会复制到
                                          运行时映像。
      --launcher <名称>=<模块>[/<主类>]
                                        为模块和主类添加给定
                                        名称的启动程序命令
                                        (如果指定)
      --limit-modules <模块>[,<模块>...]  限制可观察模块的领域
      --list-plugins                    List available plugins
  -p, --module-path <path>              模块路径。
                                        如果未指定，将使用 JDK 的 jmods
                                        目录（如果存在该目录）。如果指定，
                                        但它不包含 java.base 模块，
                                        则将添加 JDK 的 jmods 目录
                                        （如果存在该目录）。
      --no-header-files                 Exclude include header files
      --no-man-pages                    Exclude man pages
      --output <路径>                     输出路径的位置
      --save-opts <文件名>                将 jlink 选项保存在指定文件中
  -G, --strip-debug                     Strip debug information
      --suggest-providers [<名称>,...]  建议可从模块路径中实现
                                        给定服务类型的提供方
  -v, --verbose                         启用详细跟踪
      --version                           版本信息
      @<文件名>                           从文件中读取选项







