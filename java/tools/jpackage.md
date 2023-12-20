jpackage -h
用法：jpackage <options>

示例用法:
--------------
    生成适合主机系统的应用程序包：
        对于模块化应用程序：
            jpackage -n name -p modulePath -m moduleName/className
        对于非模块化应用程序：
            jpackage -i inputDir -n name --main-class className --main-jar myJar.jar
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
            jlink --output appRuntimeImage -p modulePath -m moduleName \
                --no-header-files [<additional jlink options>...]
            jpackage --type app-image -n name \
                -m moduleName/className --runtime-image appRuntimeImage
    生成 Java 运行时程序包：
        jpackage -n name --runtime-image <runtime-image>
--------------

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
  --name -n <name>
          应用程序和/或程序包的名称
  --dest -d <destination path>
          用来放置所生成的输出文件的路径
          默认为当前的工作目录。
          （绝对路径或相对于当前目录的路径）
  --temp <file path>
          用来创建临时文件的新目录或空白目录的路径
          （绝对路径或相对于当前目录的路径）
          如果指定，则在任务完成时将不删除临时目录，
          必须手动删除临时目录
          如果未指定，则将创建一个临时目录，
          并在任务完成时删除该临时目录。
  --vendor <vendor string>
          应用程序的供应商
  --verbose
          启用详细的输出
  --version
          将产品版本输出到输出流并退出

用来创建运行时映像的选项：
  --add-modules <模块名称>[,<模块名称>...]
          要添加的模块的逗号 (",") 分隔列表。
          此模块列表连同主模块（如果指定）
          将作为 --add-module 参数传递到 jlink。
          如果未指定，则仅使用主模块（如果指定了 --module），
          或者使用默认的模块集（如果指定了
          --main-jar）。
          可以多次使用此选项。
  --module-path -p <module path>...
          路径的 ; 分隔列表
          每个路径要么是模块的目录，要么是
          模块 jar 的路径。
          （每个路径可以是绝对路径，也可以是相对于当前目录的路径）
          可以多次使用此选项。
  --jlink-options <jlink 选项>
          要传递给 jlink 的选项列表（用空格分隔）
          如果未指定，则默认为 "--strip-native-commands
          --strip-debug --no-man-pages --no-header-files"
          可以多次使用此选项。
  --runtime-image <file path>
          将复制到应用程序映像的预定义
          运行时映像的路径
          （绝对路径或相对于当前目录的路径）
          如果未指定 --runtime-image，jpackage 将运行 jlink 以
          使用如下选项创建运行时映像：
          --strip-debug、--no-header-files、--no-man-pages 和
          --strip-native-commands。

用来创建应用程序映像的选项：
  --icon <icon file path>
          应用程序包图标的路径
          （绝对路径或相对于当前目录的路径）
  --input -i <input path>
          包含要打包的文件的输入目录的路径
          （绝对路径或相对于当前目录的路径）
          输入目录中的所有文件将打包到
          应用程序映像中。

用来创建应用程序启动程序的选项：
  --add-launcher <launcher name>=<file path>
          启动程序的名称和包含关键字-值对列表的
          属性文件的路径
          （绝对路径或相对于当前目录的路径）
          可以使用关键字 "module"、"main-jar"、"main-class"、
          "arguments"、"java-options"、"app-version"、"icon" 和
          "win-console"。
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
  --app-image <file path>
          用来构建可安装程序包的
          预定义应用程序映像的位置
          （绝对路径或相对于当前目录的路径）
  --file-associations <file path>
          包含关键字-值对列表的属性文件的路径
          （绝对路径或相对于当前目录的路径）
          可以使用关键字 "extension"、"mime-type"、"icon" 和 "description"
          来描述此关联。
          可以多次使用此选项。
  --install-dir <file path>
          默认安装位置下面的相对子路径
  --license-file <file path>
          许可证文件的路径
          （绝对路径或相对于当前目录的路径）
  --resource-dir <path>
          覆盖 jpackage 资源的路径
          可以通过向该目录中添加替代资源来覆盖 jpackage 的
          图标、模板文件和其他资源。
          （绝对路径或相对于当前目录的路径）
  --runtime-image <file-path>
          要安装的预定义运行时映像的路径
          （绝对路径或相对于当前目录的路径）
          在创建运行时程序包时需要使用选项。

用来创建应用程序包的与平台相关的选项：
  --win-dir-chooser
          添加一个对话框以允许用户选择
          产品的安装目录
  --win-menu
          将该应用程序添加到系统菜单中
  --win-menu-group <menu group name>
          启动该应用程序所在的菜单组
  --win-per-user-install
          请求基于每个用户执行安装
  --win-shortcut
          为应用程序创建桌面快捷方式
  --win-upgrade-uuid <id string>
          与此程序包升级相关联的 UUID
    
    
    
    







jpackage Sample usages:
--------------

    Generate an application package suitable for the host system:
        For a modular application:
            jpackage -n name -p modulePath -m moduleName/className
        For a non-modular application:
            jpackage -i inputDir -n name \
                --main-class className --main-jar myJar.jar
        From a pre-built application image:
            jpackage -n name --app-image appImageDir
    Generate an application image:
        For a modular application:
            jpackage --type app-image -n name -p modulePath \
                -m moduleName/className
        For a non-modular application:
            jpackage --type app-image -i inputDir -n name \
                --main-class className --main-jar myJar.jar
        To provide your own options to jlink, run jlink separately:
            jlink --output appRuntimeImage -p modulePath \
                --add-modules moduleName \
                --no-header-files [<additional jlink options>...]
            jpackage --type app-image -n name \
                -m moduleName/className --runtime-image appRuntimeImage
    Generate a Java runtime package:
        jpackage -n name --runtime-image <runtime-image>

Generic Options:
  @<filename>
          Read options and/or mode from a file
          This option can be used multiple times.
  --type -t <type>
          The type of package to create
          Valid values are: {"app-image", "exe", "msi"}
          If this option is not specified a platform dependent
          default type will be created.
  --app-version <version>
          Version of the application and/or package
  --copyright <copyright string>
          Copyright for the application
  --description <description string>
          Description of the application
  --help -h
          Print the usage text with a list and description of each valid
          option for the current platform to the output stream, and exit
  --icon <file path>
          Path of the icon of the application package
          (absolute path or relative to the current directory)
  --name -n <name>
          Name of the application and/or package
  --dest -d <destination path>
          Path where generated output file is placed
          (absolute path or relative to the current directory)
          Defaults to the current working directory.
  --temp <directory path>
          Path of a new or empty directory used to create temporary files
          (absolute path or relative to the current directory)
          If specified, the temp dir will not be removed upon the task
          completion and must be removed manually.
          If not specified, a temporary directory will be created and
          removed upon the task completion.
  --vendor <vendor string>
          Vendor of the application
  --verbose
          Enables verbose output
  --version
          Print the product version to the output stream and exit.

Options for creating the runtime image:
  --add-modules <module name>[,<module name>...]
          A comma (",") separated list of modules to add
          This module list, along with the main module (if specified)
          will be passed to jlink as the --add-module argument.
          If not specified, either just the main module (if --module is
          specified), or the default set of modules (if --main-jar is
          specified) are used.
          This option can be used multiple times.
  --module-path -p <module path>...
          A ; separated list of paths
          Each path is either a directory of modules or the path to a
          modular jar.
          (Each path is absolute or relative to the current directory.)
          This option can be used multiple times.
  --jlink-options <jlink options>
          A space separated list of options to pass to jlink
          If not specified, defaults to "--strip-native-commands
          --strip-debug --no-man-pages --no-header-files".
          This option can be used multiple times.
  --runtime-image <directory path>
          Path of the predefined runtime image that will be copied into
          the application image
          (absolute path or relative to the current directory)
          If --runtime-image is not specified, jpackage will run jlink to
          create the runtime image using options:
          --strip-debug, --no-header-files, --no-man-pages, and
          --strip-native-commands.

Options for creating the application image:
  --input -i <directory path>
          Path of the input directory that contains the files to be packaged
          (absolute path or relative to the current directory)
          All files in the input directory will be packaged into the
          application image.

Options for creating the application launcher(s):
  --add-launcher <launcher name>=<file path>
          Name of launcher, and a path to a Properties file that contains
          a list of key, value pairs
          (absolute path or relative to the current directory)
          The keys "module", "main-jar", "main-class",
          "arguments", "java-options", "app-version", "icon", and
          "win-console" can be used.
          These options are added to, or used to overwrite, the original
          command line options to build an additional alternative launcher.
          The main application launcher will be built from the command line
          options. Additional alternative launchers can be built using
          this option, and this option can be used multiple times to
          build multiple additional launchers.
  --arguments <main class arguments>
          Command line arguments to pass to the main class if no command
          line arguments are given to the launcher
          This option can be used multiple times.
  --java-options <java options>
          Options to pass to the Java runtime
          This option can be used multiple times.
  --main-class <class name>
          Qualified name of the application main class to execute
          This option can only be used if --main-jar is specified.
  --main-jar <main jar file>
          The main JAR of the application; containing the main class
          (specified as a path relative to the input path)
          Either --module or --main-jar option can be specified but not
          both.
  --module -m <module name>[/<main class>]
          The main module (and optionally main class) of the application
          This module must be located on the module path.
          When this option is specified, the main module will be linked
          in the Java runtime image.  Either --module or --main-jar
          option can be specified but not both.

用来创建应用程序启动程序的与平台相关的选项：
  --win-console
          为应用程序创建控制台启动程序，应当为
          需要控制台交互的应用程序指定

Options for creating the application package:
  --about-url <url>
          URL of the application's home page
  --app-image <directory path>
          Location of the predefined application image that is used
          to build an installable package
          (absolute path or relative to the current directory)
  --file-associations <file path>
          Path to a Properties file that contains list of key, value pairs
          (absolute path or relative to the current directory)
          The keys "extension", "mime-type", "icon", and "description"
          can be used to describe the association.
          This option can be used multiple times.
  --install-dir <directory path>
          默认安装位置下面的相对子路径
  --license-file <file path>
          Path to the license file
          (absolute path or relative to the current directory)
  --resource-dir <directory path>
          Path to override jpackage resources
          Icons, template files, and other resources of jpackage can be
          Request to add desktop shortcut for this application
  --win-shortcut-prompt
          Adds a dialog to enable the user to choose if shortcuts
          will be created by installer.
  --win-update-url <url>
          URL of available application update information
  --win-upgrade-uuid <id string>
          UUID associated with upgrades for this package









# JEP 343: Packaging Tool (Incubator)

| Owner       | Kevin Rushforth                                              |
| ----------- | ------------------------------------------------------------ |
| Type        | Feature                                                      |
| Scope       | JDK                                                          |
| Status      | Closed / Delivered                                           |
| Release     | 14                                                           |
| Component   | tools / jpackage                                             |
| Discussion  | core dash libs dash dev at openjdk dot java dot net          |
| Effort      | M                                                            |
| Duration    | M                                                            |
| Relates to  | [JEP 311: Java Packager API & CLI](http://openjdk.java.net/jeps/311) |
|             | [JEP 392: Packaging Tool](http://openjdk.java.net/jeps/392)  |
| Reviewed by | Alan Bateman, Alexander Matveev, Alexey Semenyuk, Andy Herrick, Mandy Chung, William Harnois |
| Endorsed by | Brian Goetz                                                  |
| Created     | 2018/04/04 19:22                                             |
| Updated     | 2021/08/28 00:10                                             |
| Issue       | [8200758](https://bugs.openjdk.java.net/browse/JDK-8200758)  |

## Summary

Create a tool for packaging self-contained Java applications.

## Goals

Create a simple packaging tool, based on the JavaFX `javapackager` tool, that:

- Supports native packaging formats to give end users a natural installation experience. These formats include `msi` and `exe` on Windows, `pkg` and `dmg` on macOS, and `deb` and `rpm` on Linux.
- Allows launch-time parameters to be specified at packaging time.
- Can be invoked directly, from the command line, or programmatically, via the `ToolProvider` API.

## Non-Goals

- The following features of the

   

  ```
  javapackager
  ```

   

  tool will not be supported:

  - Java Web Start application support,
  - JavaFX-specific features,
  - `jdeps` usage for determining required modules, and
  - the Ant plugin.

- There will be no GUI for the tool; a command-line interface (CLI) is sufficient.

- There will be no support for cross compilation. For example, in order to create Windows packages one must run the tool on Windows. The packaging tool will depend upon platform-specific tools.

- There will be no special support for legal files beyond what is already provided in JMOD files. There will be no aggregation of individual license files.

- There will be no native splash screen support.

- There will be no auto-update mechanism.

- The tool will not be available on Solaris.

## Motivation

Many Java applications need to be installed on a native platform in a first-class way, rather than simply being placed on the class path or the module path. It is not sufficient for the application developer to deliver a simple JAR file; they must deliver an installable package suitable for the native platform. This allows Java applications to be distributed, installed, and uninstalled in a manner that is familiar to users. For example, on Windows users expect to be able to double-click on a package to install their software, and then use the control panel to remove the software; on macOS, users expect to be able to double-click on a DMG file and drag their application to the Application folder.

A packaging tool can also help fill gaps left by other technologies such as Java Web Start, which was removed from Oracle’s JDK 11, and `pack200`, which was deprecated in JDK 11 for removal in a future release. Developers can use `jlink` to strip the JDK down to the minimal set of modules that are needed, and then use the packaging tool to produce a compressed, installable image that can be deployed to target machines.

To address these requirements previously, a packaging tool called `javapackager` was distributed with Oracle’s JDK 8. However, it was removed from Oracle’s JDK 11 as part of the removal of JavaFX.

## Description

The `jpackage` tool packages a Java application into a platform-specific package that includes all of the necessary dependencies. The application may be provided as a collection of ordinary JAR files or as a collection of modules. The supported platform-specific package formats are:

- Linux: `deb` and `rpm`
- macOS: `pkg` and `dmg`
- Windows: `msi` and `exe`

By default, `jpackage` produces a package in the format most appropriate for the system on which it is run.

### Basic usage: Non-modular applications

Suppose you have an application composed of JAR files, all in a directory named `lib`, and that `lib/main.jar` contains the main class. Then the command

```
$ jpackage --name myapp --input lib --main-jar main.jar
```

will package the application in the local system's default format, leaving the resulting package file in the current directory. If the `MANIFEST.MF` file in `main.jar` does not have a `Main-Class` attribute then you must specify the main class explicitly:

```
$ jpackage --name myapp --input lib --main-jar main.jar \
  --main-class myapp.Main
```

The name of the package will be `myapp`, though the name of the package file itself will be longer, and end with the package type (e.g., `myapp.exe`). The package will include a launcher for the application, also called `myapp`. To start the application, the launcher will place every JAR file that was copied from the input directory on the class path of the JVM.

If you wish to produce a package in a format other than the default, then use the `--type` option. For example, to produce a `pkg` file rather than `dmg` file on macOS:

```
$ jpackage --name myapp --input lib --main-jar main.jar --type pkg
```

### Basic usage: Modular applications

If you have a modular application, composed of modular JAR files and/or JMOD files in a `lib` directory, with the main class in the module `myapp`, then the command

```
$ jpackage --name myapp --module-path lib -m myapp
```

will package it. If the `myapp` module does not identify its main class then, again, you must specify that explicitly:

```
$ jpackage --name myapp --module-path lib -m myapp/myapp.Main
```

(When packaging a modular JAR or a JMOD file you can specify the main class with the `--main-class` option to the `jar` and `jmod` tools.)

### Package metadata

The `jpackage` tool allows you to specify various kinds of metadata for your package. The options common to all platforms are:

- `--app-version <version>`
- `--copyright <string>`
- `--description <string>`
- `--license-file <file>`
- `--name <string>`
- `--vendor <string>`

The tool uses the arguments provided to these options in the manner appropriate to the package's type. Platform-specific package metadata options are described [below](http://openjdk.java.net/jeps/343#Platform-specific-details).

### File associations

You can define one or more file-type associations for your application via the `--file-associations` option, which can be used more than once. The argument to this option is a properties file with values for one or more of the following keys:

- `extension` specifies the extension of files to be associated with the application,
- `mime-type` specifies the MIME type of files to be associated with the application,
- `icon` specifies an icon, within the application image, for use with this association, and
- `description` specifies a short description of the association.

### Launchers

By default, the `jpackage` tool creates a simple native launcher for your application. You can customize the default launcher via the following options:

- `--arguments <string>` — Command-line arguments to pass to the main class if no command line arguments are given to the launcher (this option can be used multiple times)
- `--java-options <string>` — Options to pass to the JVM (this option can be used multiple times)

If your application requires additional launchers then you can add them via the `--add-launcher` option:

- `--add-launcher <launcher-name>=<file>`

The named `<file>` should be a properties file with values for one or more of the keys `app-version` `icon` `arguments` `java-options` `main-class` `main-jar` `module`, or `win-console`. The values of these keys will be interpreted as arguments to the options of the same name, but with respect to the launcher being created rather than the default launcher. The `--add-launcher` option can be used multiple times.

### Application images

The `jpackage` tool constructs an *application image* as input to the platform-specific packaging tool that it invokes in its final step. Normally this image is a temporary artifact, but sometimes you need to customize it before it's packaged. You can, therefore, run the `jpackage` tool in two steps. First, create the initial application image with the special package type `app-image`:

```
$ jpackage --name myapp --module-path lib -m myapp --type app-image
```

This will produce an application image in the `myapp` directory. Customize that image as needed, and then create the final package via the `--app-image` option:

```
$ jpackage --name myapp --app-image myapp
```

### Runtime images

An application image contains both the files comprising your application as well as the JDK *runtime image* that will run your application. By default, the `jpackage` tool invokes the the [`jlink` tool](https://openjdk.java.net/jeps/282) to create the runtime image. The content of the image depends upon the type of the application:

- For a non-modular application composed of JAR files, the runtime image contains the same set of JDK modules that is [provided to class-path applications in the unnamed module](https://openjdk.java.net/jeps/261#Root-modules) by the regular `java` launcher.
- For a modular application composed of modular JAR files and/or JMOD files, the runtime image contains the application's main module and the transitive closure of all of its dependencies. It will not include all the available service providers; if you want those to be bound then specify the `--bind-services` option to the `jpackage` tool.

In either case, if you want additional modules to be added to the runtime image you can use the `--add-modules` option with the `jpackage` tool. The list of modules in a runtime image is available in the image's `release` file.

Runtime images created by the `jpackage` tool do not contain debug symbols, the usual JDK commands, man pages, or the `src.zip` file.

If you wish to customize the runtime image further then you can invoke `jlink` yourself and pass the resulting image to the `jpackage` tool via the `--runtime-image` option. For example, if you've used the [`jdeps` tool](https://docs.oracle.com/en/java/javase/13/docs/specs/man/jdeps.html) to determine that your non-modular application only needs the `java.base` and `java.sql` modules, you could reduce the size of your package significantly:

```
$ jlink --add-modules java.base,java.sql --output myjre
$ jpackage --name myapp --input lib --main-jar main.jar --runtime-image myjre
```

#### Platform-specific details

This section describes the platform-specific aspects of the `jpackage` tool, including application image layouts and platform-specific options. The command `jpackage --help` will print a summary of all options.

The application images created by the `jpackage` tool contain some files not shown in the layouts below; such files should be considered implementation details that are subject to change.

##### Linux

```
myapp/
  bin/              // Application launcher(s)
    myapp
  lib/
    app/
      myapp.cfg     // Configuration info, created by jpackage
      myapp.jar     // JAR files, copied from the --input directory
      mylib.jar
      ...
    runtime/        // JDK runtime image
```

The default installation directory on Linux is `/opt`. This can be overridden via the `--install-dir` option.

Linux-specific options:

- `--linux-package-name <package name>` — Name for the Linux package, defaults to the application name
- `--linux-deb-maintainer <email address>` — Maintainer for a DEB package
- `--linux-menu-group <menu-group-name>` — Menu group this application is placed in
- `--linux-package-deps <deps>` — Required packages or capabilities for the application
- `--linux-rpm-license-type <type string>` — Type of the license (`License: <value>` of the RPM `.spec` file)
- `--linux-app-release <release value>` — Release value of the RPM `<name>.spec` file, or the Debian revision value of the DEB control file
- `--linux-app-category <category value>` — Group value of the RPM `<name>.spec` file, or the Section value of the DEB control file
- `--linux-shortcut` Creates a shortcut for the application

##### macOS

```
MyApp.app/
  Contents/
    Info.plist
    MacOS/          // Application launcher(s)
      MyApp
    Resources/      // Icons, etc.
    app/
      MyApp.cfg     // Configuration info, created by jpackage
      myapp.jar     // JAR files, copied from the --input directory
      mylib.jar
      ...
    runtime/        // JDK runtime image
```

The default installation directory on macOS is `/Applications`. This can be overridden via the `--install-dir` option.

macOS-specific options:

- `--mac-package-identifier <string>` — An identifier that uniquely identifies the application for macOS (defaults to the main class name; limited to alphanumeric, hyphen, and period characters)
- `--mac-package-name <string>` — Name of the application as it appears in the menu bar (defaults to the application name; must be less than 16 characters long and be suitable for displaying in the menu bar and the application Info window)
- `--mac-package-signing-prefix <string>` — When signing the application bundle, the value prepended to all components that need to be signed but don't have an existing bundle identifier
- `--mac-sign` — Request that the bundle be signed
- `--mac-signing-keychain <file>` — Path of the keychain to search for the signing identity (defaults to the standard keychains)
- `--mac-signing-key-user-name <team name>` — Team name portion of the Apple signing identity (for example, "Developer ID Application: ")

##### Windows

```
MyApp/
  MyApp.exe         // Application launcher(s)
  app/
    MyApp.cfg     // Configuration info, created by jpackage
    myapp.jar     // JAR files, copied from the --input directory
    mylib.jar
    ...
  runtime/        // JDK runtime image
```

The default installation directory on Windows is `C:/Program Files/`. This can be overridden via the `--install-dir` option.

Windows-specific options:

- `--win-console` — Creates a console launcher for the application (should be specified for applications which require console interactions)
- `--win-dir-chooser` — Adds a dialog to enable the user to choose a directory in which to install the application
- `--win-menu` — Adds the application to the system menu
- `--win-menu-group <menu-group-name>` — Start Menu group in which to place this application
- `--win-per-user-install` — Install the application on a per-user basis
- `--win-shortcut` — Create a desktop shortcut for the application
- `--win-upgrade-uuid <string>` — UUID associated with upgrades for this package

### Delivering `jpackage`

The `jpackage` tool will be delivered in the JDK as an [incubator module](https://openjdk.java.net/jeps/11) named `jdk.incubator.jpackage`. As a feature delivered in an incubator module, the `jpackage` tool's command line options, application layout, and other exported interfaces are not guaranteed to be stable and may be revised in a future release. The tool will display a warning when run from the command line. The `jdk.incubator.jpackage` module will not be resolved by default, and will cause a warning to be displayed when it is resolved.

The `jpackage` tool is based on the `javapackager` tool, with all features related to Java Web Start and JavaFX removed. The command-line interface (CLI) conforms to [JEP 293 (Guidelines for JDK Command-Line Tool Options)](http://openjdk.java.net/jeps/293). In addition to the command-line interface, `jpackage` is accessible via the [ToolProvider API](https://docs.oracle.com/javase/10/docs/api/java/util/spi/ToolProvider.html) (`java.util.spi.ToolProvider`) under the name `"jpackage"`.

## Testing

Most tests can be done with automated scripts, but there are a few considerations to be aware of:

- Testing the native packages may require optional tools to be installed; those tests will need to be written such that they are skipped on systems without the necessary tools.
- Verifying some types of native packages (e.g., `exe` on Windows or `dmg` on macOS) may require some manual testing.
- We need to ensure that native packages can be installed and uninstalled cleanly, so that developers can test in their local environment without fear of polluting their systems.

## Dependencies

Native packages will be generated using tools on the target platform. For Windows, there is an additional tool that developers will need to install if they want to generate native packages:

- Wix, a third-party tool, is required to generate `msi` or `exe` packages

There are efforts underway to enhance `jlink` to generate native launchers in a future version of the JDK. Some level of coordination may be needed between `jlink` and `jpackage`.

