## Apache Maven Dependency Plugin

https://maven.apache.org/plugins/maven-dependency-plugin/

The dependency plugin provides the capability to manipulate artifacts. It can copy and/or unpack artifacts from local or remote repositories to a specified location.

### Goals Overview

The Dependency plugin has several goals:

- [dependency:analyze](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-mojo.html) analyzes the dependencies of this project and determines which are: used and declared; used and undeclared; unused and declared.
- [dependency:analyze-dep-mgt](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-dep-mgt-mojo.html) analyzes your projects dependencies and lists mismatches between resolved dependencies and those listed in your dependencyManagement section.
- [dependency:analyze-only](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-only-mojo.html) is the same as analyze, but is meant to be bound in a pom. It does not fork the build and execute test-compile.
- [dependency:analyze-report](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-report-mojo.html) analyzes the dependencies of this project and produces a report that summarises which are: used and declared; used and undeclared; unused and declared.
- [dependency:analyze-duplicate](https://maven.apache.org/plugins/maven-dependency-plugin/analyze-duplicate-mojo.html) analyzes the `<dependencies/>` and `<dependencyManagement/>` tags in the pom.xml and determines the duplicate declared dependencies.
- [dependency:build-classpath](https://maven.apache.org/plugins/maven-dependency-plugin/build-classpath-mojo.html) tells Maven to output the path of the dependencies from the local repository in a classpath format to be used in java -cp. The classpath file may also be attached and installed/deployed along with the main artifact.
- [dependency:copy](https://maven.apache.org/plugins/maven-dependency-plugin/copy-mojo.html) takes a list of artifacts defined in the plugin configuration section and copies them to a specified location, renaming them or stripping the version if desired. This goal can resolve the artifacts from remote repositories if they don't exist in either the local repository or the reactor.
- [dependency:copy-dependencies](https://maven.apache.org/plugins/maven-dependency-plugin/copy-dependencies-mojo.html) takes the list of project direct dependencies and optionally transitive dependencies and copies them to a specified location, stripping the version if desired. This goal can also be run from the command line.
- [dependency:display-ancestors](https://maven.apache.org/plugins/maven-dependency-plugin/display-ancestors-mojo.html) displays all ancestor POMs of the project. This may be useful in a continuous integration system where you want to know all parent poms of the project. This goal can also be run from the command line.
- [dependency:get](https://maven.apache.org/plugins/maven-dependency-plugin/get-mojo.html) resolves a single artifact, eventually transitively, from a specified remote repository.
- [dependency:go-offline](https://maven.apache.org/plugins/maven-dependency-plugin/go-offline-mojo.html) tells Maven to resolve everything this project is dependent on (dependencies, plugins, reports) in preparation for going offline.
- [dependency:list](https://maven.apache.org/plugins/maven-dependency-plugin/list-mojo.html) alias for resolve that lists the dependencies for this project.
- [dependency:list-classes](https://maven.apache.org/plugins/maven-dependency-plugin/list-classes-mojo.html) displays the fully package-qualified names of all classes found in a specified artifact.
- [dependency:list-repositories](https://maven.apache.org/plugins/maven-dependency-plugin/list-repositories-mojo.html) displays all project dependencies and then lists the repositories used.
- [dependency:properties](https://maven.apache.org/plugins/maven-dependency-plugin/properties-mojo.html) set a property for each project dependency containing the to the artifact on the file system.
- [dependency:purge-local-repository](https://maven.apache.org/plugins/maven-dependency-plugin/purge-local-repository-mojo.html) tells Maven to clear dependency artifact files out of the local repository, and optionally re-resolve them.
- [dependency:resolve](https://maven.apache.org/plugins/maven-dependency-plugin/resolve-mojo.html) tells Maven to resolve all dependencies and displays the version. **JAVA 9 NOTE:** *will display the module name when running with Java 9.*
- [dependency:resolve-plugins](https://maven.apache.org/plugins/maven-dependency-plugin/resolve-plugins-mojo.html) tells Maven to resolve plugins and their dependencies.
- [dependency:sources](https://maven.apache.org/plugins/maven-dependency-plugin/sources-mojo.html) tells Maven to resolve all dependencies and their source attachments, and displays the version.
- [dependency:tree](https://maven.apache.org/plugins/maven-dependency-plugin/tree-mojo.html) displays the dependency tree for this project.
- [dependency:unpack](https://maven.apache.org/plugins/maven-dependency-plugin/unpack-mojo.html) like copy but unpacks.
- [dependency:unpack-dependencies](https://maven.apache.org/plugins/maven-dependency-plugin/unpack-dependencies-mojo.html) like copy-dependencies but unpacks.

### Usage

General instructions on how to use the Dependency Plugin can be found on the [usage page](https://maven.apache.org/plugins/maven-dependency-plugin/usage.html). Some more specific use cases are described in the examples given below.

In case you still have questions regarding the plugin's usage, please have a look at the [FAQ](https://maven.apache.org/plugins/maven-dependency-plugin/faq.html) and feel free to contact the [user mailing list](https://maven.apache.org/plugins/maven-dependency-plugin/mailing-lists.html). The posts to the mailing list are archived and could already contain the answer to your question as part of an older thread. Hence, it is also worth browsing/searching the [mail archive](https://maven.apache.org/plugins/maven-dependency-plugin/mailing-lists.html).

If you feel like the plugin is missing a feature or has a defect, you can file a feature request or bug report in our [issue tracker](https://maven.apache.org/plugins/maven-dependency-plugin/issue-management.html). When creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. For this reason, entire debug logs, POMs or most preferably little demo projects attached to the issue are very much appreciated. Of course, patches are welcome, too. Contributors can check out the project from our [source repository](https://maven.apache.org/plugins/maven-dependency-plugin/scm.html) and will find supplementary information in the [guide to helping with Maven](http://maven.apache.org/guides/development/guide-helping.html).

### Examples

The following examples show how to use the dependency plugin in more advanced use-cases:

- [Copying Specific Artifacts](https://maven.apache.org/plugins/maven-dependency-plugin/examples/copying-artifacts.html)
- [Copying Project Dependencies](https://maven.apache.org/plugins/maven-dependency-plugin/examples/copying-project-dependencies.html)
- [Unpacking Specific Artifacts](https://maven.apache.org/plugins/maven-dependency-plugin/examples/unpacking-artifacts.html)
- [Unpacking the Project Dependencies](https://maven.apache.org/plugins/maven-dependency-plugin/examples/unpacking-project-dependencies.html)
- [Rewriting target path and file name](https://maven.apache.org/plugins/maven-dependency-plugin/examples/unpacking-filemapper.html)
- [Using Project Dependencies' Sources](https://maven.apache.org/plugins/maven-dependency-plugin/examples/using-dependencies-sources.html)
- [Failing the Build on Dependency Analysis Warnings](https://maven.apache.org/plugins/maven-dependency-plugin/examples/failing-the-build-on-dependency-analysis-warnings.html)
- [Exclude Dependencies from Dependency Analysis](https://maven.apache.org/plugins/maven-dependency-plugin/examples/exclude-dependencies-from-dependency-analysis.html)
- [Filtering the Dependency Tree](https://maven.apache.org/plugins/maven-dependency-plugin/examples/filtering-the-dependency-tree.html)
- [Resolving Conflicts Using the Dependency Tree](https://maven.apache.org/plugins/maven-dependency-plugin/examples/resolving-conflicts-using-the-dependency-tree.html)
- [Purging the local repository](https://maven.apache.org/plugins/maven-dependency-plugin/examples/purging-local-repository.html)

### Resources

Here is a link that provides more reference regarding dependencies (i.e. dependency management, transitive dependencies).

- [Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html)

- ## dependency:copy-dependencies

  **Full name**:

  org.apache.maven.plugins:maven-dependency-plugin:3.3.0:copy-dependencies

  **Description**:

  Goal that copies the project dependencies from the repository to a defined location.

  **Attributes**:

  - Requires a Maven project to be executed.
  - Requires dependency resolution of artifacts in scope: `test`.
  - The goal is thread-safe and supports parallel builds.
  - Since version: `1.0`.
  - Binds by default to the [lifecycle phase](http://maven.apache.org/ref/current/maven-core/lifecycles.html): `process-sources`.

  


Copying project dependencies
Project dependencies are the dependencies declared in your pom. To copy them with their transitive dependencies, use the dependency:copy-dependencies mojo and configure the plugin like the sample below:

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.3.0</version>
        <executions>
          <execution>
            <id>copy-dependencies</id>
            <phase>package</phase>
            <goals>
              <goal>copy-dependencies</goal>
            </goals>
            <configuration>
              <outputDirectory>${project.build.directory}/alternateLocation</outputDirectory>
              <overWriteReleases>false</overWriteReleases>
              <overWriteSnapshots>false</overWriteSnapshots>
              <overWriteIfNewer>true</overWriteIfNewer>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>

```

Excluding transitive dependencies
As mentioned, transitive dependencies are copied by default. However, they can also be excluded by setting the excludeTransitive property to true.

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-dependency-plugin</artifactId>
        <version>3.3.0</version>
        <executions>
          <execution>
            <id>copy-dependencies</id>
            <phase>package</phase>
            <goals>
              <goal>copy-dependencies</goal>
            </goals>
            <configuration>
              <outputDirectory>${project.build.directory}/alternateLocation</outputDirectory>
              <overWriteReleases>false</overWriteReleases>
              <overWriteSnapshots>true</overWriteSnapshots>
              <excludeTransitive>true</excludeTransitive>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

#### 注：可以使用一下配置复制依赖项到项目根目录下，避免每次clean之后需要重新运行copy-denpendency

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>2.8</version>
    <configuration>
        <outputDirectory>${project.basedir}/dependency</outputDirectory>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
                        <goals>
                <goal>copy</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 