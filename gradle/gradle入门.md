# Gradle入门



https://gradle.org/

当前gradle最新版本为 8.2.1

查看版本兼容性

https://docs.gradle.org/8.2.1/userguide/compatibility.html

| Java version | First Gradle version to support it |
| ------------ | ---------------------------------- |
| 8            | 2.0                                |
| 9            | 4.3                                |
| 10           | 4.7                                |
| 11           | 5.0                                |
| 12           | 5.4                                |
| 13           | 6.0                                |
| 14           | 6.3                                |
| 15           | 6.7                                |
| 16           | 7.0                                |
| 17           | 7.3                                |
| 18           | 7.5                                |
| 19           | 7.6                                |
| 20           | 8.1 ⚠                              |

⚠: Indicates that the Java version can be used for compilation and tests, but not yet running Gradle itself.

可以看到兼容的最新java版本为 java19



查看idea插件对gradle的兼容,进入idea的安装路径

```bash
ideaIU-2022.3.win\plugins\gradle\lib
```

可以看到

```bash
    gradle-api-8.0.jar
    gradle-api-impldep-8.0.jar
    gradle-launcher-8.0.jar
    gradle-tooling-extension-api.jar
    gradle-tooling-extension-impl.jar
    gradle-wrapper-8.0.jar
    gradle.jar
```

插件支持gradle8.0版本



下载最新版本的gradle

https://gradle.org/releases/



将gradle添加到系统path, 测试

```bash
C:\Users\leixing\Desktop>gradle -v

Welcome to Gradle 8.2.1!

Here are the highlights of this release:
 - Kotlin DSL: new reference documentation, assignment syntax by default
 - Kotlin DSL is now the default with Gradle init
 - Improved suggestions to resolve errors in console output
 - Reduced sync memory consumption

For more details see https://docs.gradle.org/8.2.1/release-notes.html


------------------------------------------------------------
Gradle 8.2.1
------------------------------------------------------------

Build time:   2023-07-10 12:12:35 UTC
Revision:     a38ec64d3c4612da9083cc506a1ccb212afeecaa

Kotlin:       1.8.20
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          1.8.0_43 (Oracle Corporation 25.40-b25)
OS:           Windows 8.1 6.3 x86
```

或者:

```bash
C:\Users\leixing\Desktop>gradle -v

------------------------------------------------------------
Gradle 8.2.1
------------------------------------------------------------

Build time:   2023-07-10 12:12:35 UTC
Revision:     a38ec64d3c4612da9083cc506a1ccb212afeecaa

Kotlin:       1.8.20
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          20.0.1 (Oracle Corporation 20.0.1+9-29)
OS:           Windows 10 10.0 amd64
```





创建gradle项目

