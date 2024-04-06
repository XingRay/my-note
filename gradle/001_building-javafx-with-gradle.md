# building-javafx-with-gradle

https://foojay.io/today/building-javafx-with-gradle/



*A good reason to use JavaFX: FX and nice GUIs can be created fast!*



My front-end applications are based on other Maven projects whose builds I install in my local Maven repository.

Some of them use open source libraries that are not perfectly modularized, and I assume they never will be. I also don't want to modularize every project in my library toolset, even if they are modules used by different projects.

Sooner or later you might run into the modularization jungle anyway and problems when, for example, the same module names are used for different modules. It seems that `jakarta.activation` is such a prominent case where the same module name was used for the API and the implementation.

There may also be other stumbling blocks encountered when trying to create modularized applications.

I know I could probably solve everything properly by excluding certain JARs from the Maven dependencies and going through each one, but at the end of the day I just don't want to spend time doing that, so I decided to create two Gradle build scripts that apply to modular and non-modular projects.



## Modular

First, let's have a look at the modular version of the build-script `build.gradle`; see comments for explanations:



```groovy
buildscript {
    ext {
        // Variables if any
        //commonsVersion = '1.0.0'
    }
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    // Stuff we need
    id 'java'
    id 'org.javamodularity.moduleplugin' version "1.8.12"
    id 'application'
    id 'org.openjfx.javafxplugin' version '0.0.13'
    // We use the Badass JLink Plugin from Serban Iordache
    // to build modular applications!
    id 'org.beryx.jlink' version '2.25.0'
}

apply plugin: 'java'
// Does some integration to eclipse, including linking Java FX SDK
apply plugin: 'eclipse'

jar {
    baseName = 'autumo-europa-client'
    version = '1.0.0'
}

repositories {
    mavenLocal()
    mavenCentral()
}

targetCompatibility = "17"
sourceCompatibility = "11"

project.description = "autumo Europa - Secure Communication."
project.ext.buildDate = new Date()
project.version = "1.0.0"

dependencies {
    implementation "ch.autumo.commons:autumo-commons:1.0.0"
    implementation 'org.apache.logging.log4j:log4j-api:2.17.1'
    implementation 'org.apache.logging.log4j:log4j-core:2.17.1'
    implementation 'org.apache.logging.log4j:log4j-slf4j-impl:2.17.1'
    implementation 'org.apache.commons:commons-lang3:3.12.0'
}

javafx {
    version = "19"
    // Found by eclipse
    modules = [ 'javafx.controls', 'javafx.fxml' ]
}

application {
    mainModule = 'ch.autumo.europa.client'
    mainClass = 'ch.autumo.europa.client.EuropaClient'
}

run {
    jvmArgs = ['-Djdk.gtk.version=2']
}

// JLink for modular projects
jlink {
    // Some default options
    options = ['--strip-debug', '--compress', '2', '--no-header-files', '--no-man-pages']
    launcher {
        name = 'Europa Client'
        jvmArgs = ['-Djdk.gtk.version=2']
    }
    // Does some magic to log4j JARs that don't want to merge
    forceMerge('log4j-api') 

    // Pack it!
    jpackage {
        // Could be taken from command line, here it is defined statically
        // project.findProperty('installerOs')
        //    (example: -PinstallerOs=mac)
        targetPlatformName = 'mac' 
        // Resource directory for native package overrides,
        // you can do lots of magic here too...
        resourceDir = file('package/')

        if (targetPlatformName == 'mac') { // we are on mac
            targetPlatform("mac") {
                // Use downloaded Eclipse Temurin JDK
                jdkHome = '/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home'
                // Use another local JDK
                //jdkHome = '/Library/Java/JavaVirtualMachines/jdk-17.0.2.jdk/Contents/Home'
            }
            installerType = 'pkg' // we want to have macOS PKG
        }
        if (targetPlatformName == 'win') { // we are on Windows
            targetPlatform("win") {
                jdkHome = 'C:/Applications/JDKs/Adoptium/temurin-17.jdk'
            }
            installerType = 'exe'
        }
        if (targetPlatformName == 'linux') { // we are on linux
            targetPlatform("linux") {
            }
            installerType = 'deb'
        }  

        // Add jpackage-specific options
        installerOptions = [
            '--name', 'Europa-Client', // installer name
            '--description', project.description,
            '--copyright', 'Copyrigth 2022 autumo GmbH',
            '--vendor', 'autumo GmbH'
        ]

        // We also could take the installer type from comand line
        // installerType = project.findProperty('installerType')
        // We would pass this from the command line
        //    (example: -PinstallerType=msi)

        // Add platform-specific options for the target image and for jpackage
        if (installerType == 'pkg') {
            imageOptions += ['--icon', 'src/main/resources/icon.icns']
            installerOptions += [
                '--license-file', 'package/LICENSE-OS-Installer.txt'
            ]
        }
        if (installerType == 'exe') {
            imageOptions += ['--icon', 'src/main/resources/icon.ico']
            installerOptions += [
                // '--win-per-user-install', // Install only for current user
                // '--win-console', // Shows what Java outputs to the console
                '--win-dir-chooser',  
                '--win-menu', '--win-shortcut'
            ]
        }
        if (installerType in ['deb', 'rpm']) {
            imageOptions += ['--icon', 'src/main/resources/icon_256x256.png']
            installerOptions += [
                '--linux-menu-group', 'Network',
                '--linux-shortcut'
            ]
        }
        if (installerType == 'deb') {
            installerOptions += [
                '--linux-deb-maintainer', 'info@autumo.ch'
            ]
        }
        if (installerType == 'rpm') {
            installerOptions += [
                '--linux-rpm-license-type', 'GPLv3'
            ]
        }
    }
}

jpackage {
    // Could be used for pre-checks;
    // e.g., are certain command line arguments defined?
    doFirst {
        // project.findProperty('installerOs')
        //    (example: -PinstallerOs=mac)
        // project.getProperty('installerType') // throws exception if its missing
    }
}
```



kts

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.beryx.jlink") version "2.25.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_17
}

val javafxVersion = "19"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("ch.autumo.commons:autumo-commons:1.0.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.1")
    implementation("org.apache.commons:commons-lang3:3.12.0")
}

javafx {
    version = javafxVersion
    modules = listOf("javafx.controls", "javafx.fxml")
}

application {
    mainModule = "ch.autumo.europa.client"
    mainClass.set("ch.autumo.europa.client.EuropaClient")
}

tasks.named<JavaCompile>("compileJava") {
    doFirst {
        options.encoding = "UTF-8"
    }
}

tasks.named<KotlinCompile>("compileKotlin") {
    doFirst {
        kotlinOptions.jvmTarget = "17"
    }
}

run {
    jvmArgs = listOf("-Djdk.gtk.version=2")
}

jlink {
    options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = "Europa Client"
        jvmArgs = listOf("-Djdk.gtk.version=2")
    }
    forceMerge("log4j-api")

    jpackage {
        targetPlatformName = "mac"
        resourceDir = file("package/")

        if (targetPlatformName == "mac") {
            targetPlatform("mac") {
                jdkHome = "/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
            }
            installerType = "pkg"
        }
        if (targetPlatformName == "win") {
            targetPlatform("win") {
                jdkHome = "C:/Applications/JDKs/Adoptium/temurin-17.jdk"
            }
            installerType = "exe"
        }
        if (targetPlatformName == "linux") {
            targetPlatform("linux")
            installerType = "deb"
        }

        installerOptions = listOf(
            "--name", "Europa-Client",
            "--description", project.description,
            "--copyright", "Copyrigth 2022 autumo GmbH",
            "--vendor", "autumo GmbH"
        )

        if (installerType == "pkg") {
            imageOptions += listOf("--icon", "src/main/resources/icon.icns")
            installerOptions += listOf(
                "--license-file", "package/LICENSE-OS-Installer.txt"
            )
        }
        if (installerType == "exe") {
            imageOptions += listOf("--icon", "src/main/resources/icon.ico")
            installerOptions += listOf(
                "--win-dir-chooser",
                "--win-menu", "--win-shortcut"
            )
        }
        if (installerType in listOf("deb", "rpm")) {
            imageOptions += listOf("--icon", "src/main/resources/icon_256x256.png")
            installerOptions += listOf(
                "--linux-menu-group", "Network",
                "--linux-shortcut"
            )
        }
        if (installerType == "deb") {
            installerOptions += listOf(
                "--linux-deb-maintainer", "info@autumo.ch"
            )
        }
        if (installerType == "rpm") {
            installerOptions += listOf(
                "--linux-rpm-license-type", "GPLv3"
            )
        }
    }
}
```



## Non-Modular

When it comes to building non-modular JavaFX applications, we use the "The Badass Runtime Plugin" instead the "The Badass JLink Plugin" from Serban Iordache.

The build-script `build.gradle` for the non-modular version looks like this; see comments for explanations:



buildscript **{**

​    ext **{**

​    **}**

​    repositories **{**

​        mavenLocal**()**

​        mavenCentral**()**

​    **}**

**}**

plugins **{**

​    // Stuff we need

​    id 'java'

​    id 'application'

​    id 'org.openjfx.javafxplugin' version '0.0.13'

​    // We use the Badass Runtime Plugin from Serban Iordache

​    // to build modular applications!

​    id 'org.beryx.runtime' version '1.13.0'

**}**

apply plugin: 'java'

// Does some integration to eclipse, including linking Java FX SDK

apply plugin: 'eclipse'

jar **{**

​    baseName = 'autumo-documents-ui'

​    version = '1.0.0'

**}**

repositories **{**

​    mavenLocal**()**

​    mavenCentral**()**

**}**

targetCompatibility = "17"

sourceCompatibility = "8"

project.description = "The documetns viewer shows the ability of the autumo documents component to extract any file for indexing."

project.ext.buildDate = new Date**()**

project.version = "1.0.0"

javafx **{**

​    version = "19"

​    modules = **[** 'javafx.controls', 'javafx.fxml', 'javafx.web' **]**

​    // I want to specifially define where the JavaFX SDK is

​    sdk = '/Users/Mike/Library/Java/javafx-sdk-19' // on mac

​    //sdk = 'C:/Applications/JavaFX/javafx-sdk-19' // on windows

**}**

configurations **{**

​    // In case of conflicts I could exclude modules

​    //all*.exclude group: 'xml-apis'

**}**

dependencies **{**

​    implementation "ch.autumo.commons:autumo-commons:1.0.0"

​    implementation "ch.autumo.beetroot:autumo-beetroot:1.3.3"

​    implementation "ch.autumo.search:autumo-search:1.0.0"   

​    implementation "ch.autumo.documents:autumo-documents:1.0.0"

​    implementation "org.apache.poi:poi:5.2.3"

​    implementation "org.apache.poi:poi-ooxml:5.2.3"

​    implementation "org.apache.poi:poi-scratchpad:5.2.3"

​    implementation "org.apache.tika:tika-core:2.6.0"

​    implementation "org.apache.tika:tika-parsers-standard-package:2.6.0"

​    implementation "org.apache.pdfbox:pdfbox:2.0.27"

​    implementation "org.apache.pdfbox:preflight:2.0.27"

​    implementation "commons-dbutils:commons-dbutils:2.0-SNAPSHOT"

​    implementation "org.jsoup:jsoup:1.15.3"

​    implementation "com.google.zxing:core:3.5.0"

​    implementation "com.google.zxing:javase:3.5.0"

​    implementation "com.zaxxer:HikariCP:5.0.1"

​    implementation "org.apache.lucene:lucene-core:9.4.0"

​    implementation "org.apache.lucene:lucene-queryparser:9.4.0"

​    implementation "org.apache.lucene:lucene-highlighter:9.4.0"

​    implementation "org.apache.lucene:lucene-analyzers-common:8.11.2"

​    implementation "org.apache.lucene:lucene-analyzers-phonetic:8.11.2"

​    //implementation(files('lib/afile.jar')) // no local files needed

**}**

application **{**

​    mainClass = 'ch.autumo.documents.ui.Main'

**}**

run **{**

​    jvmArgs = **[**'-Djdk.gtk.version=2'**]**

**}**

// Runtime for non-modular projects

runtime **{**

​    // Some default options

​    options = **[**'--strip-debug', '--compress', '2', '--no-header-files', '--no-man-pages'**]**

​    // The modules !

​    modules = **[**'java.naming', 'jdk.charsets', 'java.xml', 'javafx.controls', 'javafx.fxml', 'javafx.web', 'java.sql' **]**

​    // Pack it!

​    jpackage **{**

​        // Could be taken from command line, here it is defined statically

​        // project.findProperty('installerOs')

​        //    (example: -PinstallerOs=mac)

​        targetPlatformName = 'mac'

​        // The app name

​        imageName = 'autumo Documents Viewer'

​        jvmArgs = **[**'-Djdk.gtk.version=2'**]**

​        // Resource directory for native package overrides,

​        // you can do lots of magic here too...     

​        resourceDir = file**(**'package/'**)**

​        **if** **(**targetPlatformName == 'mac'**)** **{**

​            targetPlatform**(**"mac"**)** **{**

​                // Use downloaded Bellsoft Liberica JDK with JavaFX bundled!

​                jdkHome = '/Volumes/Fastdrive/Development/JDKs/Bellsoft/mac/jdk-17.0.5-full.jdk'

​                // We also could directly download a JDK

​                //jdkHome = jdkDownload("https://download.java.net/java/GA/jdk17.0.1/2a2082e5a09d4267845be086888add4f/12/GPL/openjdk-17.0.1_macos-x64_bin.tar.gz")

​            **}**

​            installerType = 'pkg'

​        **}**

​        **if** **(**targetPlatformName == 'win'**)** **{**

​            targetPlatform**(**"win"**)** **{**

​                jdkHome = 'C:/Applications/JDKs/Bellsoft/jdk-17.0.5-full'

​            **}**

​            installerType = 'exe'

​        **}**

​        **if** **(**targetPlatformName == 'linux'**)** **{**

​            targetPlatform**(**"linux"**)** **{**

​            **}**

​            installerType = 'deb'

​        **}**    

​        // Add jpackage-specific options

​        installerOptions = **[**

​            '--name', 'autumo-Documents-Viewer', // installer name

​            '--description', project.description,

​            '--copyright', 'Copyright 2022 autumo GmbH',

​            '--vendor', 'autumo GmbH'

​        **]**

​        // We also could take the installer type from comand line

​        // installerType = project.findProperty('installerType')

​        // We would pass this from the command line

​        //    (example: -PinstallerType=msi)

​        **if** **(**installerType == 'pkg'**)** **{**

​            imageOptions += **[**'--icon', 'src/main/resources/icon.icns'**]**

​            installerOptions += **[**

​                '--license-file', 'package/LICENSE-OS-Installer.txt'

​            **]**

​        **}**

​        **if** **(**installerType == 'exe'**)** **{**

​            imageOptions += **[**'--icon', 'src/main/resources/icon.ico'**]** 

​            installerOptions += **[**

​                // '--win-per-user-install', // Install only for current user

​                // '--win-console', // Shows what Java outputs to the console

​                '--win-dir-chooser',

​                '--win-menu', '--win-shortcut'

​            **]**

​        **}**

​        **if** **(**installerType **in** **[**'deb', 'rpm'**])** **{**

​            imageOptions += **[**'--icon', 'src/main/resources/icon_256x256.png'**]**

​            installerOptions += **[**

​                '--linux-menu-group', 'Utility',

​                '--linux-shortcut'

​            **]**

​        **}**

​        **if** **(**installerType == 'deb'**)** **{**

​            installerOptions += **[**

​                '--linux-deb-maintainer', 'info@autumo.ch'

​            **]**

​        **}**

​        **if** **(**installerType == 'rpm'**)** **{**

​            installerOptions += **[**

​                '--linux-rpm-license-type', 'GPLv3'

​            **]**

​        **}**

​    **}**

**}**

jpackage **{**

​    // Could be used for pre-checks;

​    // e.g., are certain command line arguments defined?

​    doFirst **{**

​        // project.findProperty('installerOs')

​        //    (example: -PinstallerOs=mac)

​        // project.getProperty('installerType') // throws exception if its missing

​    **}**

**}**



## Does it work?

Yes it does, and it works on all platforms!

I'm pretty sure there are still things "under the lid" in the build scripts that I don't quite understand yet, but the above scripts work and I can build modular and non-modular JavaFX applications with a JDK of my choice.

The day is saved, you might want to use these scripts as templates! Enjoy 💫



## References

Some more references to this article:

- The Badass JLink Plugin: [https://badass-jlink-plugin.beryx.org](https://badass-jlink-plugin.beryx.org/)

- The Badass Runtime Plugin: [https://badass-runtime-plugin.beryx.org](https://badass-runtime-plugin.beryx.org/)

- Java FX Gradle plugin: https://github.com/openjfx/javafx-gradle-plugin

- Bellsoft's Liberica JDK: https://bell-sw.com/libericajdk

- Ecipse/Adoptium Temurin JDK: [https://adoptium.net](https://adoptium.net/)

- Open Java FX: [https://openjfx.io](https://openjfx.io/)

- Eclipse IDE: https://www.eclipse.org/downloads/packages

  

**Don’t Forget to Share This Post!**

- 
- 
- 
- 

![Avatar photo](D:\my-note\gradle\assets\cropped-prof-96x96.jpeg)

AuthorMichael Gasche

Michael has worked as a software engineer, project manager, consultant and CEO and has over 20 years of experience in the Java programming language. He finally decided to develop his own toolset and make better Java products and components that are not based on multiple layers of configuration frameworks and…

[Show More](https://foojay.io/today/author/michael-gasche/)

- 
- 

Topics:

- [Gradle](https://foojay.io/today/tag/gradle/)
- [JavaFX](https://foojay.io/today/tag/javafx/)
- [jpackage](https://foojay.io/today/tag/jpackage/)
- [Modules](https://foojay.io/today/tag/modules/)

## Related Articles

[View All](https://foojay.io/today/)

- [A Better Way To Use Gradle With Github Actions](https://foojay.io/today/a-better-way-to-use-gradle-with-github-actions/)

  To enhance the experience of building Gradle projects on GitHub Actions, the Gradle team has developed the gradle-build-action.

  [Read More](https://foojay.io/today/a-better-way-to-use-gradle-with-github-actions/)

  ![img](D:\my-note\gradle\assets\11fda5a58859359f21cc12f972857a19.jpeg)

  - [Daz DeBoer](https://foojay.io/today/author/daz-deboer/)

  September 21, 2022

  - [DevOps](https://foojay.io/today/category/devops/)
  - [Gradle](https://foojay.io/today/category/tools/gradle/)

- [Compilation Avoidance with Gradle](https://foojay.io/today/compilation-avoidance-with-gradle/)

  Learn what ABI-based compilation means for the average workflow, turning out to be one of the best performance enhancements for any build!

  [Read More](https://foojay.io/today/compilation-avoidance-with-gradle/)

  ![img](D:\my-note\gradle\assets\67ee166ef85e0e34fd8fd7c045d285da.png)

  - [Amanda Martin](https://foojay.io/today/author/amanda-martin/)

  December 13, 2022

  - [DevOps](https://foojay.io/today/category/devops/)
  - [Java Core](https://foojay.io/today/category/java-core/)

- [Introducing Gradle Test Suites](https://foojay.io/today/introducing-gradle-test-suites/)

  Did you know? In Gradle 7.3, released November 2021, the Gradle team introduced a new feature called Declarative Test Suites.

  [Read More](https://foojay.io/today/introducing-gradle-test-suites/)

  ![img](D:\my-note\gradle\assets\fc40d4df085543c6b66b174d6556029d.png)

  - [Tom Tresansky](https://foojay.io/today/author/tom-tresansky/)

  December 06, 2022

  - [Gradle](https://foojay.io/today/category/tools/gradle/)
  - [Java Core](https://foojay.io/today/category/java-core/)
  - [Tutorials](https://foojay.io/today/category/tutorial/)

## Author(s)

- ![Avatar photo](D:\my-note\gradle\assets\cropped-prof-96x96.jpeg)

  **[Michael Gasche](https://foojay.io/today/author/michael-gasche/)**

  - 
  - 

  Michael has worked as a software engineer, project manager, consultant and CEO and has over 20 years of experience in the Java programming language. He finally decided to develop his ... [Learn more](https://foojay.io/today/author/michael-gasche/)

## Comments (4)

Leave a Comment

![img](D:\my-note\gradle\assets\19105dcded085177d51b9a248b07732f.jpeg)

**Roman**

Awesome!
Still I don’t quite understand why to use javafx plugin along with Liberica full jdk

1:55 pm, January 9, 2023

![Avatar photo](D:\my-note\gradle\assets\cropped-prof-60x60.jpeg)

**[Michael Gasche](https://www.autumo.ch/)**

When I am not mistaken, the JavaFX plugin is used during development in the Gradle-project, but Liberica’s Java FX is bundled within the jpackage.

3:09 pm, January 11, 2023

![img](D:\my-note\gradle\assets\31749c96d2e55b82d8ec22a374bdd971.jpeg)

**Mike**

The badass jlink plugin (https://badass-jlink-plugin.beryx.org/releases/latest/) makes this super easy. An entire build.gradle for a JavaFX app is ~25 lines long (not including dependencies) when using this plugin, that includes packaging a slimmed down runtime with jlink/jpackage.

4:23 pm, January 20, 2023

![Avatar photo](D:\my-note\gradle\assets\cropped-prof-60x60.jpeg)

**[Michael Gasche](https://www.autumo.ch/)**

That’s exactly why is used it 👀

4:25 pm, January 20, 2023





