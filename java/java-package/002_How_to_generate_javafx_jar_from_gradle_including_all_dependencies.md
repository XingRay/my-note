# How to generate javafx jar from gradle including all dependencies



https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies





[Ask Question](https://stackoverflow.com/questions/ask)

Asked 2 years, 2 months ago

Modified [2 years, 2 months ago](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies?lastactivity)

Viewed 2k times



0



Been stuck with this issue for days. I'm trying to export a jar file using the gradle build command but it provides a small jar file. When I run the jar file it gives the following error, indicating that the javafx dependency was not included in the build:

```java
Exception in thread "main" java.lang.NoClassDefFoundError: javafx/application/Application
    at java.base/java.lang.ClassLoader.defineClass1(Native Method)
    at java.base/java.lang.ClassLoader.defineClass(ClassLoader.java:1012)
    at java.base/java.security.SecureClassLoader.defineClass(SecureClassLoader.java:150)
    at java.base/jdk.internal.loader.BuiltinClassLoader.defineClass(BuiltinClassLoader.java:862)
    at java.base/jdk.internal.loader.BuiltinClassLoader.findClassOnClassPathOrNull(BuiltinClassLoader.java:760)
    at java.base/jdk.internal.loader.BuiltinClassLoader.loadClassOrNull(BuiltinClassLoader.java:681)
    at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:639)
    at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
    at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:520)
    at newcare.home.Main_1.main(Main_1.java:6)
Caused by: java.lang.ClassNotFoundException: javafx.application.Application
    at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
    at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
    at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:520)
    ... 10 more
```

I've seen posts such as this [Creating a Java Gradle project and building the .jar file in IntelliJ IDEA - How to?](https://stackoverflow.com/questions/37100082/creating-a-java-gradle-project-and-building-the-jar-file-in-intellij-idea-how) They seem to help others but not me. Here's my build.gradle:

```java
plugins {
 id 'java'
 id 'application'
 id 'org.openjfx.javafxplugin' version '0.0.8'
}
group 'newcare'
version '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

sourceCompatibility = 1.8

dependencies {
    testImplementation group: 'junit', name: 'junit', version: '4.12'
    implementation group: 'org.jasypt', name: 'jasypt', version: '1.9.2'
    implementation 'com.google.code.gson:gson:2.8.7'
    implementation 'joda-time:joda-time:2.10.13'
    implementation 'org.ocpsoft.prettytime:prettytime:4.0.4.Final'


    // here starts JavaFX
    implementation 'org.openjfx:javafx:14'

    implementation 'org.openjfx:javafx-base:14'
    implementation 'org.openjfx:javafx-graphics:14'
    implementation 'org.openjfx:javafx-controls:14'
    implementation 'org.openjfx:javafx-fxml:14'
    implementation 'org.openjfx:javafx-swing:14'
    implementation 'org.openjfx:javafx-media:14'
    implementation 'org.openjfx:javafx-web:14'
}

javafx{
    modules = ['javafx.controls', 'javafx.fxml', 'javafx.media', 'javafx.graphics']
    version = '11.0.2'
}

mainClassName = 'newcare.home.Main_1'




jar {
    from {
        configurations.runtime.collect {
            it.isDirectory() ? it : zipTree(it)
        }
        configurations.compile.collect {
            it.isDirectory() ? it : zipTree(it)
        }
    }
    manifest {
        attributes "Main-Class": "$mainClassName"
    }

    exclude 'META-INF/*.RSA', 'META-INF/*.SF','META-INF/*.DSA'
}




sourceSets {
    main {
        resources {
            srcDirs = ["src/main/java"]
            includes = ["**/*.fxml","**/*.css","**/*.png","**/*.jpg"]
        }
    }
}
```

I've also tried to use the build artifacts methods but nothing seems to work.

- [java](https://stackoverflow.com/questions/tagged/java)
- [gradle](https://stackoverflow.com/questions/tagged/gradle)
- [javafx](https://stackoverflow.com/questions/tagged/javafx)

[Share](https://stackoverflow.com/q/70175403/8273792)

[Edit](https://stackoverflow.com/posts/70175403/edit)

Follow

asked Nov 30, 2021 at 20:05

![Patrice Andala's user avatar](D:\my-note\java\java-package\assets\picture.jpeg)

[Patrice Andala](https://stackoverflow.com/users/5251944/patrice-andala)

**194**22 silver badges1414 bronze badges

- I don't know about gradle, but the maven equivalent of this question is: [Maven Shade JavaFX runtime components are missing](https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing) 

  – [jewelsea](https://stackoverflow.com/users/1155209/jewelsea)

   [Nov 30, 2021 at 20:15](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment124051726_70175403)

- Use the Gradle Shadow plugin (I think that's what it's called). Also, if you're using the JavaFX plugin, then you shouldn't be manually adding JavaFX to `dependencies` (you'd use the `javafx` extension to declare which modules you want, and the version; see the documentation for details). And why use JavaFX 14 when version 17.0.1 is the latest release? Note the latest version for the JavaFX plugin is 0.0.10. 

  – [Slaw](https://stackoverflow.com/users/6395627/slaw)

   [Nov 30, 2021 at 20:16](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment124051770_70175403) 

- Thanks. I shall take the advice and get back to you guys 

  – [Patrice Andala](https://stackoverflow.com/users/5251944/patrice-andala)

   [Nov 30, 2021 at 20:25](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment124051953_70175403)

- This is a different solution, but, as you are using gradle, I advise you investigate the [badass runtime plugin](https://badass-runtime-plugin.beryx.org/releases/latest/) and the [badass jlink plugin](https://badass-jlink-plugin.beryx.org/releases/latest/) with a goal to creating appropriate runtime images rather than an unsupported configuration like a jar with dependencies. 

  – [jewelsea](https://stackoverflow.com/users/1155209/jewelsea)

   [Nov 30, 2021 at 23:35](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment124055245_70175403) 

[Add a comment](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#)



## 1 Answer

Sorted by:

​                              Highest score (default)                                            Trending (recent votes count more)                                            Date modified (newest first)                                            Date created (oldest first)                    





7







***Preface:** Although it's possible to create a fat/uber JAR file which includes JavaFX, this is not the preferred approach. That results in JavaFX being loaded from the class-path, which is not a supported configuration. However, if you really want to create a fat/uber JAR file then skip to the "Creating a Fat/Uber JAR" section.*

------

# Self-Contained Application

The preferred approach for deploying a JavaFX application these days, is to create a custom run-time image and package it into a platform-specific installer/executable.

## JLink & JPackage

The [`jlink`](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jlink.html) tool is used to create custom run-time images. That's really just a fancy way of saying "a custom JRE that contains only the modules you need". The result is a self-contained application, though not a very user-friendly one, in my opinion.

Note the `jlink` tool only works with explicitly named modules (i.e. there's a `module-info.class` file present).

The [`jpackage`](https://docs.oracle.com/en/java/javase/17/docs/specs/man/jpackage.html) tool essentially takes a custom run-time image and generates a platform-specific executable for it (e.g. a `.exe` file on Windows). Then the application is packaged into a platform-specific installation file (e.g. a `.msi` file on Windows). This tool has a [user guide](https://docs.oracle.com/en/java/javase/17/jpackage/packaging-overview.html).

Note the `jpackage` tool supports creating non-modular applications. You can even configure it so all modules end up in the custom run-time image, while your non-modular application and any other non-modular dependencies are placed on the class-path.

#### Native Code

The JavaFX framework requires platform-specific native code to work. This means you need to make sure that native code is included in the custom run-time image. There are two ways you can do this:

1. Use the JavaFX JAR files from Maven Central, not the JavaFX SDK.
   - The JAR files published to Maven Central embed the native code. However, JavaFX will have to extract the native code to some place on your computer in order to use it.
2. (Preferred) Use the JavaFX JMOD files, which can be downloaded from [gluonhq.com](https://gluonhq.com/products/javafx/).
   - You would point `jlink` / `jpackage` at the JMOD files *instead of* the regular JAR files.
   - This results in the native code being included in the same way as all the native code needed by the JRE itself. Now there's no need to extract the native code, which makes this the better option in my opinion.

## Gradle

There are two plugins I'd recommend for using `jlink` / `jpackage` from Gradle.

- [The Badass JLink Plugin](https://badass-jlink-plugin.beryx.org/releases/latest/) (for modular applications)
- [The Badass Runtime Plugin](https://badass-runtime-plugin.beryx.org/releases/latest/) (for non-modular applications)

------

# Creating a "Fat/Uber JAR"

When using Gradle, I recommend the [Gradle Shadow Plugin](https://imperceptiblethoughts.com/shadow/) for creating so-called fat/uber JAR files. It does much of the configuration for you, such as excluding signature files. And it pulls appropriate defaults from the already-existing `jar` task. It also adds the `shadowJar` task for building the fat/uber JAR file.

## Example

Here is a sample application that creates a fat/uber JAR file. Note I use the Kotlin DSL for Gradle instead of the Groovy DSL, but you can use whichever.

Used:

- Gradle 7.3
- Java 17.0.1 (JavaFX not included)

**settings.gradle.kts**

```kotlin
rootProject.name = "sample"
```

**build.gradle.kts**

```kotlin
plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "sample"
version = "1.0"

java {
    modularity.inferModulePath.set(false)
}

javafx {
    version = "17.0.1"
    modules("javafx.controls")
}

application {
    mainClass.set("sample.Main")
}

repositories {
    mavenCentral()
}

tasks {
    shadowJar {
        exclude("module-info.class")
    }
}
```

**Main.java**

```java
package sample;

import javafx.application.Application;

public class Main {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
```

**App.java**

```java
package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane(new Label("Hello, World!"));
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.setTitle("Sample");
        primaryStage.show();
    }
}
```

Running `gradle shadowJar` will give you a fat/uber JAR file at `build/libs/sample-1.0-all.jar`.

A few notes:

- I configure the `shadowJar` plugin to exclude `module-info.class` files, because fat/uber JAR files do not work well with the module-path.
  - A JAR can only contain one module.
  - Running the application with `-jar` puts the JAR on the class-path.
  - Bonus: This avoids having multiple `module-info.class` files in the root of the JAR, one for each modular dependency.
- The `sample.Main` class is necessary, because JavaFX technically does not support being loaded from the class-path. If the main class is assignable to `javafx.application.Application`, and the `javafx.graphics` module is not found in the boot layer, then the application will fail to start. The separate main class hacks around that.
- On JavaFX 16+ a warning will be emitted because JavaFX does not support being loaded from the class-path.
- This JAR file will only work for the platform you ran Gradle on, because only that platform's native code is included.
  - If you want a cross-platform JAR then you need to manually add the JavaFX dependencies for each platform so that the native code for each platform is embedded in the JAR file.



[Share](https://stackoverflow.com/a/70175935/8273792)

[Edit](https://stackoverflow.com/posts/70175935/edit)

Follow

[edited Dec 1, 2021 at 1:08](https://stackoverflow.com/posts/70175935/revisions)

answered Nov 30, 2021 at 20:53

![Slaw's user avatar](D:\my-note\java\java-package\assets\af0f9bfe593f36642a032cd7ea611e7d.png)

[Slaw](https://stackoverflow.com/users/6395627/slaw)

**40.8k**88 gold badges5858 silver badges8787 bronze badges

- Creating a fat/uber jar using shadow plugin seems to have worked but it won't run. Apparently it is not recognizing my fxml files. I get the error: NullPointerException: Location is required. on lines where I try to add fxml files using the code: Parent root = FXMLLoader.load(getClass().getResource("../fxml/Login.fxml")); 

  – [Patrice Andala](https://stackoverflow.com/users/5251944/patrice-andala)

   [Dec 2, 2021 at 2:19](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment124083682_70175935)

- 1

  Using `..` is not supported by the resource API. For `Class#getResource(String)`, the argument must either be an absolute path (rooted at the root of the class-path/module-path) or a path relative from the class's location. Although using `..` may work before your code is packaged, that is merely an accident of the implementation. See [How do I determine the correct path for FXML files, CSS files, Images, and other resources needed by my JavaFX Application?](https://stackoverflow.com/questions/61531317/) for more detail. 

  – [Slaw](https://stackoverflow.com/users/6395627/slaw)

   [Dec 2, 2021 at 2:25](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment124083749_70175935) 

- Much appreciated 

  – [Patrice Andala](https://stackoverflow.com/users/5251944/patrice-andala)

   [Dec 2, 2021 at 2:49](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment124083988_70175935)

- Error: JavaFX runtime components are missing, and are required to run this application 

  – [Tristate](https://stackoverflow.com/users/3290745/tristate)

   [Aug 17, 2022 at 13:20](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment129604293_70175935)

- @Tristate Are you saying you get that error when trying one of the solutions in my answer? If so, which solution? The self-contained application, or the fat/uber JAR? If the former, then as far as I know, that error should not be possible with a self-contained application where JavaFX is in the custom run-time image. If the latter, then did you follow the concepts in my answer exactly? I just copy-pasted the setup for the fat/uber JAR file and everything worked as expected. I also tried the latest versions of everything involved and it still worked. 

  – [Slaw](https://stackoverflow.com/users/6395627/slaw)

   [Aug 17, 20](https://stackoverflow.com/questions/70175403/how-to-generate-javafx-jar-from-gradle-including-all-dependencies#comment129612038_70175935)