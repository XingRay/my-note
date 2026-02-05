# Gradle依赖引入介绍

## 1. 引入关键字列表

| 4.x+版本                  | 老版本（弃用）     | 说明                                |
| ------------------------- | ------------------ | ----------------------------------- |
| api                       | compile            | 打包并传递                          |
| implementation            | compile            | 打包不传递                          |
| compileOnly               | provided           | 只在编译时用                        |
| runtimeOnly               | apk                | 只在运行时用 打包到apk              |
| testImplementation        | testCompile        | 只在test用 打包到测试包             |
| androidTestImplementation | androidTestCompile | 只在android test用 打包到测试包     |
| debugImplementation       | debugCompile       | 只在debug模式有效 打包到debug包     |
| releaseImplementation     | releaseCompile     | 只在release模式有效 打包到release包 |


## 2. 关键字说明

### api

打包输出到aar或apk，并且依赖向上传递。

### implementation

打包输出到aar或apk，依赖不传递。

### compileOnly

编译时使用，不会打包到输出（aar或apk）。

### runtimeOnly

只在生成apk的时候参与打包，编译时不会参与，很少用。

### testImplementation

只在单元测试代码的编译以及最终打包测试apk时有效。

### androidTestImplementation

只在Android相关单元测试代码的编译以及最终打包测试apk时有效。

### debugImplementation

只在 debug 模式的编译和最终的 debug apk 打包时有效

### releaseImplementation

仅仅针对 Release 模式的编译和最终的 Release apk 打包。

## 3. 各种依赖写法

#### 本地项目依赖


```java
dependencies {
    implementation project(':projectABC')
}
```

#### 本地包依赖


```java
dependencies {
    // 批量引入
    implementation fileTree(dir: 'libs', include: ['*.jar'])

    // 单个引入
    implementation files('libs/aaa.jar', 'libs/bbb.jar')
    implementation files('x/y/z/ccc.jar')
}
```

#### 远程包依赖


```java
dependencies {
    // 简写
    implementation 'androidx.appcompat:appcompat:1.0.2'
    // 完整写法
    implementation  group: 'androidx.appcompat', name:'appcompat', version:'1.0.2'
}
```

###### 根据Task类型（debug, release, test）引入


```java
dependencies {
    // test
    testImplementation 'junit:junit:4.12'
    // android test  
    androidTestImplementation 'com.android.support.test:runner:1.0.1'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.1'
    // debug
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.0-beta-2'
    // release
    releaseImplementation 'com.squareup.leakcanary:leakcanary-android-no-op:2.0-beta-2'
}
```

###### 排除引用（解决引入冲突）


```java
dependencies {
    implementation ('com.github.bumptech.glide:glide:4.9.0'){
        exclude group:'com.android.support', module: 'support-fragment'
        exclude group:'com.android.support', module: 'support-core-ui'
        exclude group:'com.android.support', module: 'support-compat'
        exclude group:'com.android.support', module: 'support-annotations'
    }
}
```

最后编辑于 ：2022.11.07 11:47:59


依赖的方式
Gradle中的依赖方式有直接依赖、项目依赖和本地jar包依赖三种：

dependencies {
    // 1、依赖当前项目下的某个模块[子工程]
    implementation project(':subject01')
    // 2、直接依赖本地的某个jar文件
    implementation files('libs/foo.jar', 'libs/bar.jar')
    // 2、配置某文件夹作为依赖项
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    // 3、直接依赖
    implementation 'org.apache.logging.log4j:log4j:2.17.2'
}
直接依赖
在项目中直接导入的依赖，即为直接依赖，如：

    implementation 'org.apache.logging.log4j:log4j:2.17.2'
完整写法如下，其中group/name/version共同定位一个远程jar包：

    implementation group: 'org.apache.logging.log4j', name: 'log4j;, version: '2.17.2'
项目依赖
依赖项目中的另一个模块，被依赖的模块需要是library模块，并且在settings.gradle中配置：

    implementation project(':subject01')
本地jar包依赖
即依赖本地jar包，有如下两种方式：

    // 2、直接依赖本地的某个jar文件
    implementation files('libs/foo.jar', 'libs/bar.jar')
    // 2、配置某文件夹作为依赖项
    implementation fileTree(dir: 'libs', include: ['*.jar'])
依赖的类型

其中java插件的功能，java-library插件都提供。

api和implementation的区别
如下表所示：

api	implementation
编译时	能进行依赖传递；底层变，上层全部都要变；编译速度慢	不能进行依赖传递；底层变，上层不会变化；编译速度快
运行时	运行时会加载，所有模块的类都会被加载	运行时会加载，所有模块的类都会被加载
应用场景	适用于多模块依赖，避免重复依赖	多数情况下使用implementation
以下图为例：

当libC发生变化时，libA和projectX也随之变化，都需要重新编译；当libD发生变化时，直接依赖它的libB随之变化，而没有直接依赖libD的projectX不会发生变化，也只有libD和libB要重新编译。

再考虑一种场景：一个工程中，moduleA依赖moduleB和moduleC，moduleB也依赖moduleC，因此可以让moduleB以api的方式依赖moduleC，moduleA则只implementation依赖moduleB即可。

再例如，一个工程中有ABCD四个模块：
1）、A implmentation B，B implementation C，则A不可用C；
2）、A implmentation B，B api C，则A可用C；
3）、A implmentation B，B implementation C，C api D，则B可用D，A不可用D；
4）、A implmentation B，B api C，C api D，则A可用D。

任何情况下，发生依赖的模块里所有的类都会被加载。

依赖冲突及解决方案
依赖冲突是指，在编译过程中，若存在对某个包的多版本依赖，构建系统要选择哪个进行构建，如下图所示：

其中，ABC都是本地项目或模块，log4j时远程依赖。编译时，B和C各用各的log4j，彼此没有冲突。但打包时，只能有一个版本的代码被打到jar包中，因此就发生了冲突。

事实上，gradle默认会选择最新的版本去打包，因为新版本的jar包一般都是向下兼容的，因此推荐这种官方的默认解决方法，不过gradle也提供了一系列的解决依赖冲突的方法：移除某个依赖；不允许依赖传递或强制使用某个版本。

移除某个依赖
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'

    implementation('org.hibernate:hibernate-core:3.6.3.Final'){
        // 排除某一个库(slf4j)依赖:如下三种写法都行
        exclude group: 'org.slf4j'
        exclude module: 'slf4j-api'
        exclude group: 'org.slf4j',module: 'slf4j-api'
    
    // 排除之后,使用手动的引入即可。
    implementation 'org.slf4j:slf4j-api:1.4.0'
}
不允许依赖传递
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
    implementation('org.hibernate:hibernate-core:3.6.3.Final'){
        // 不允许依赖传递，一般不用
        transitive(false)
    }
    // 排除之后,使用手动的引入即可
    implementation 'org.slf4j:slf4j-api:1.4.0'
}
不允许依赖传递，则该依赖的所有内部依赖均不会添加到编译或运行时的类路径中，

强制使用某个版本
dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.1'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine:5.8.1'
    implementation('org.hibernate:hibernate-core:3.6.3.Final')
    // 强制使用某个版本【官方建议使用这种方式】
    implementation('org.slf4j:slf4j-api:1.4.0!!')
    // 这种效果和上面那种一样,强制指定某个版本
    implementation('org.slf4j:slf4j-api:1.4.0'){
        version{
            strictly("1.4.0")
        }
    }
}
依赖冲突时立刻构建失败
事实上，我们可以配置当Gradle遇到依赖冲突时，立刻构建失败，从而找出项目或模块中的所有的依赖冲突：

// 项目或模块的build.gradle

configurations.all {
    Configuration config -> {
        // 当遇到版本冲突时直接构建失败
        config.resolutionStrategy.failOnVersionConflict()
    }
}