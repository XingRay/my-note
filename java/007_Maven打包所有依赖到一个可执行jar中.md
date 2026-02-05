# Maven打包所有依赖到一个可执行jar中

6月前作者：拾年一剑分类： [博客文章](https://www.yii666.com/blog.html)阅读(35)原文[违法举报](https://www.yii666.com/jubao.html?jubaourl=https://www.yii666.com/blog/373757.html)

文章来源地址https://www.yii666.com/blog/373757.html*文章地址https://www.yii666.com/blog/373757.html*

### 文章目录

- - 需求
  - 遇到的问题
  - - 找不到主类
    - 打包时没有包含第三方依赖
    - - 情况1
      - 情况2
  - 扩展问题
  - - 不希望依赖的jar包变成class
    - 项目里多个主类，如何动态指定

网址:yii666.com<

## 需求

普通的Maven Java项目（非Springboot项目），需要打包成一个jar包（包含所有的第三方依赖jar包），能够放在服务器上单独运行。

## 遇到的问题

### 找不到主类

大家都知道，如果使用常见的`maven-jar-plugin`打包，只能将自己项目里的源码编译打包，不会包含第三方的jar包。

如果该项目没有第三方依赖包，则可以通过`maven-jar-plugin`打包，直接执行打好的jar包（java -jar xxx.jar），可能会遇到找不到主类的情况，可以通过下面的方式解决：指定主类

```xml
<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.0.2</version>
    <configuration>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>com.xxx.AppMain</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

注意：

> META-INF文件夹是MANIFEST.MF文件的主页。 此文件包含有关JAR内容的元数据。
> JAR包中的/META-INF/MANIFEST.MF元数据文件必须包含Main-Class(主类)信息。(工程里的src/META-INF/MANIFEST.MF)
> 项目所有的依赖都必须在Classpath中，其可以通过 MANIFEST.MF 指定或者隐式设置。

### 打包时没有包含第三方依赖

如果该项目有第三方依赖包，通过上面的方式打包，是不会包含第三方依赖的，直接运行生成的jar包会出错（相关依赖不存在）。
想要打包时包含第三方依赖，又可以分两种情况：

#### 情况1

打包成可执行jar文件，但是将所有依赖（包括外部依赖）单独打包到另外一个指定文件夹下，通过指定Class-Path的方式关联。

1. 打包依赖到指定文件夹

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>2.10</version>
    <executions>
        <execution>
            <id>copy-dependencies</id>
            <phase>package</phase>
            <goals>
                <goal>copy-dependencies</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/lib</outputDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

关于Maven dependency plugin可以参考我的另外一篇博客，有详细介绍：网址:yii666.com

> Maven dependency plugin使用**文章来源地址:https://www.yii666.com/blog/373757.html**

1. 指定启动入口，并关联依赖

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <configuration>
        <archive>
            <manifest>
                <!-- 是否绑定依赖，将外部jar包依赖加入到classPath中 -->
                <addClasspath>true</addClasspath>
                <!-- 依赖前缀，与之前设置的文件夹路径要匹配 -->
                <classpathPrefix>lib/</classpathPrefix>
                <!-- 主函数的入口 -->
                <mainClass>com.xxx.AppMain</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

配置好后，可以通过下面的命令编译打包：
`mvn clean pacakge -DskipTests=true`

注意，复杂情况下，classpath需要在运行时指定，如 java -cp …

#### 情况2

将整个工程打成一个可执行jar包，包含所有的依赖。

需要通过`maven-assembly-plugin`插件来打包，可以实现该需求。

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>2.3.2</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
            </configuration>
        </plugin>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <archive>
                    <manifest>
                        <mainClass>com.xxx.AppMain</mainClass>
                    </manifest>
                </archive>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

配置好后，可以通过下面的命令编译打包：
`mvn assembly:assembly`
执行成功后会在target文件夹下多出一个以-jar-with-dependencies结尾的jar包. 这个jar包就包含了项目所依赖的所有jar的`class`。

## 扩展问题

### 不希望依赖的jar包变成class

可以通过修改插件的配置做到

1. 在本地maven仓库里找到maven-assembly-plugin

```shell
cd ~/.m2/repository/org/apache/maven/plugins/maven-assembly-plugin/
```

1. 进入打包时运行使用的版本里，如2.2-beta-5；
2. 解压maven-assembly-plugin-2.2-beta-5.jar；
3. 进入解压好的文件夹找到assemblies\jar-with-dependencies.xml，
   把里面的UNPACK改成FALSE，保存即可；
4. 还原解压后的文件为jar包
   例如，在～/.m2/repository/org/apache/maven/plugins/maven-assembly-plugin/2.2-beta-5 路径里，执行下面的命令

```shell
jar cvfm maven-assembly-plugin-2.2-beta-5.jar maven-assembly-plugin-2.2-beta-5/META-INF/MANIFEST.MF -C maven-assembly-plugin-2.2-beta-5 .
```

1. 再次使用`mvn assembly:assembly`打包，编译好的以-jar-with-dependencies结尾的jar包. 这个jar包就包含了项目所依赖的所有jar文件，不再是class；

### 项目里多个主类，如何动态指定

可以通过自定义property属性，在执行maven命令时，动态指定来实现，配置如下：

例如，main.class 则为自定义的；

```xml
<properties>
    <main.class>com.xxx.AppMain</main.class>
</properties>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>2.3.2</version>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
            </configuration>
        </plugin>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <archive>
                    <manifest>
                        <mainClass>${main.class}</mainClass>
                    </manifest>
                </archive>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

运行maven命令时，动态指定：

```shell
mvn -Dmain.class=com.xxx.AppMain2 assembly:assembly
```