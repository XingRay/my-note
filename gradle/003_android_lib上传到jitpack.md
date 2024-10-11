# 使用 Jitpack 发布你的 Android 库

在开发 Android 应用时，我们经常会把一些通用的代码抽取出来，打包成一个库，方便在其他项目中使用。这时候，我们就需要把这个库发布到某个地方，方便其他项目引用。你一定在 build.gradle 中看到过这样的代码：



```xml
dependencies {
    implementation("<group>:<artifact>:<version>")
}
```

这里介绍一种简单的方式，使用 Jitpack 发布你的 Android 库。

## 什么是 Jitpack？

Jitpack 是一个基于 GitHub 的自动化构建工具，它可以帮助你将 GitHub 上的项目构建成一个 Maven 仓库，方便其他项目引用。

## 马上动手

既然要发布库，那么首先你需要有一个库，新建一个 Android 项目，在 Android Studio 中切换到 Project 视图，现在我们有一个 `app` 模块。右键项目新建一个 module，选择 Android Library，命名为 `mylibrary` ，这个模块就是我们要发布的库。

在库里新建一个单例类：



```kotlin
object MyLibrary {
    fun getMessage() = "Hello from MyLibrary!"
}
```

在 `mylibrary` 的 build.gradle.kts 中引入 mavenpublish 插件



```objectivec
plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish") // 引入 maven 插件
}
```

我们还要在 `mylibrary` 的 build.gradle.kts 中添加发布配置 （我这里使用的是 Kotlin DSL，如果你使用的是 Groovy DSL，请自行转换或者选择将 build 配置从 Groovy 迁移到 KTS



```kotlin
val GROUP_ID = "com.github.bqliang"
val ARTIFACT_ID = "jitpack-lib-sample"
val VERSION = latestGitTag().ifEmpty { "1.0.0-SNAPSHOT" }

fun latestGitTag(): String {
    val process = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start()
    return  process.inputStream.bufferedReader().use {bufferedReader ->
        bufferedReader.readText().trim()
    }
}


publishing { // 发布配置
    publications { // 发布的内容
        register<MavenPublication>("release") { // 注册一个名字为 release 的发布内容
            groupId = GROUP_ID
            artifactId = ARTIFACT_ID
            version = VERSION

            afterEvaluate { // 在所有的配置都完成之后执行
                // 从当前 module 的 release 包中发布
                from(components["release"])
            }
        }
    }
}
```

这里我们使用了 `latestGitTag()` 方法来获取 git 仓库中最新的 tag作为版本号。如果没有 tag，那么就使用 1.0.0-SNAPSHOT 作为版本号。

现在，我们就可以在 Android Studio 中点击 `mylibrary` 的右侧的 Gradle 选项卡，找到 `publishToMavenLocal` 任务，双击运行，就可以将 `mylibrary` 发布到本地的 Maven 仓库中。

我们已经将 `mylibrary` 发布到了本地的 Maven 仓库中，现在我们可以在 `app` 模块中引用它了。在 `app` 的 build.gradle.kts 中添加依赖：



```bash
dependencies {
    implementation("com.github.bqliang:jitpack-lib-sample:1.0.0-SNAPSHOT")
}
```

这里的 `com.github.bqliang:jitpack-lib-sample:1.0.0-SNAPSHOT` 就是我们在 `mylibrary` 的 build.gradle.kts 中配置的 `<GROUP_ID>:<ARTIFACT_ID>:<VERSION>`，如果我们后续要发布新的版本，只需要给 git 仓库打一个 tag，然后重新运行 `publishToMavenLocal` 任务，就可以将新版本发布到本地的 Maven 仓库中了。当然，你如果不喜欢使用 git tag 作为版本号，也可以直接在 `mylibrary` 的 `build.gradle.kts` 中写死版本号。

因为是从本地的 Maven 仓库中引用，所以我们还需要在项目的 `settings.gradle.kts` 中添加本地 Maven 仓库地址：



```cpp
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        mavenLocal()
    }
}
```

Sync 一下项目，就可以在 `app` 模块中使用 `MyLibrary` 了：



```kotlin
fun main {
    println(MyLibrary.getMessage())
}
```

因为我们的库和 app 是在同一个项目中，所以其实我们可以直接在 `app` 模块中引用 `mylibrary`：



```bash
dependencies {
    implementation(project(":mylibrary"))
    // implementation("com.github.bqliang:jitpack-lib-sample:1.0.0-SNAPSHOT")
}
```

这在调试修改库的时候会非常方便，因为我们可以直接在 `app` 模块中调试库的代码而不需要每次都 publish 一下。

等等，我们好像忘了什么？对了，我们还没有发布到 Jitpack 上呢！ 把仓库推送到 GitHub 上，然后打开 Jitpack 官网，输入 输入`<GitHub用户名>/<仓库名>` 搜索你的仓库（注意仓库可见性要设置为 public），点击 Get it，就可以看到引用的方式了：



```xml
dependencies {
    implementation("com.github.<GitHub用户名>:<仓库名>:<版本号>")
}
```

现在，我们就可以在任何项目中使用我们的库了！

注意不要忘了在项目的 `settings.gradle.kts` 中添加 Jitpack 的仓库地址：



```cpp
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven(url = "https://jitpack.io")
    }
}
```

注意！`implementation("com.github.<GitHub用户名>:<仓库名>:<版本号>")` 这里的版本号不是我们在 `mylibrary` 的 `build.gradle.kts` 中配置的版本号。`<GitHub用户名>` 和 `<Github仓库名>` 和我们在 `mylibrary` 的 `build.gradle.kts` 中配置的 `<GROUP_ID>` 和 `<ARTIFACT_ID>`当然也不是同样的东西。

其实在 Get it 的时候，我们可以选择 release 或者 branch 或者 commit id，如果选择了 release，那么版本号就是 release 的 tag，如果选择了 branch，那么版本号就是 `<分支名-SNAPSHOT>`，如果选择了 commit id，那么版本号就是 commit id。

注意 SNAPSHOT 版本的缓存问题，我们可以在 build.gradle.kts 中添加以下配置来解决：



```bash
configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
```

我们还可以利用 GitHub Action 实现推送 tag 时自动发布 Github Release，从而触发 Jitpack 的构建。 在项目的根目录下创建 `.github/workflows/release.yml` 文件：



```dart
name: publish release
on:
  push:
    tags:
      - "*"

jobs:
    release:
      runs-on: ubuntu-latest
      permissions:
        contents: write
      steps:
        - uses: ncipollo/release-action@v1
          with:
            generateReleaseNotes: true
```

这样，每当我们推送 tag 的时候，GitHub Action 就会自动发布 Release，然后触发 Jitpack 的构建，最后就可以在任何项目中使用我们的库了。