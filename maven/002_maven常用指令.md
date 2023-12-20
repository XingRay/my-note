# Maven常用指令



1、命令行方式跳过测试
我们可以通过使用命令将项目打包，添加跳过测试的命令就可以了，可以用两种命令来跳过测试：

mvn package -DskipTests=true
-DskipTests=true，不执行测试用例，但编译测试用例类生成相应的class文件至 target/test-classes 下。
mvn package -Dmaven.test.skip=true
-Dmaven.test.skip=true，不执行测试用例，也不编译测试用例类。
在使用 mvn package 进行编译、打包时，Maven会执行 src/test/java 中的 JUnit 测试用例，有时为了跳过测试，会使用参数 -DskipTests=true 和 -Dmaven.test.skip=true，这两个参数的主要区别是：

使用 -Dmaven.test.skip=true，不但跳过单元测试的运行，也跳过测试代码的编译；
使用 -DskipTests=true 跳过单元测试，但是会继续编译。