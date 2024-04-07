# Java 读取jar包中的资源文件夹



记录将项目打成一个可执行jar包后，如何读取jar包中的资源文件夹。



最近在做一个工具包的时候，有这么一种需求：在运行main函数时，需要将resources资源文件夹下的xml子文件夹中的文件加载。原本可以使用Main.class.getClassLoader().getResoruceAsStream()将文件一一加载，但是xml子文件夹中的文件非常多，不可能一个个列文件名。所以最初我的写法是：

```
File parent = new File(ClassLoader.getSystemClassLoader().getResource("").getPath() + "/xml");
File[] files = parent.listFiles();
for (int i = 0; i < xmlFiles.length; i++) {
    File xmlFile = xmlFiles[i];
    //...
}
```

在IDE中运行并没有问题，能够正常得读取到资源文件夹和文件。但是当我使用maven将工程打成jar包，试图使用命令行启动时，跑到这里就会抛空指针异常了。不断的测试和查资料，终于明白，当打成一个jar包后，整个jar包是一个文件，只能使用流的方式读取资源，这时候就不能通过File来操作资源了，得通过getResourceAsStream来读取文件内容并操作。在IDE中之所以能正常运行，是因为IDE中的资源文件在target/classes目录下，是正常的文件系统结构。

然而问题来了，我不是读一个文件，而是试图读取一个文件夹，依次读取文件夹下所有文件资源。我试图按照网上的说法，读取资源，然后创建一个临时文件，在操作该临时文件，例：

```
File file = null;
URL res = PostHelper.class.getClassLoader().getResource("xml/");
if (res.toString().startsWith("jar:")) {
    try {
        InputStream input = PostHelper.class.getClassLoader().getResourceAsStream("xml/");
        file = File.createTempFile("tempfile", ".tmp");
        OutputStream out = new FileOutputStream(file);
        int read;
        byte[] bytes = new byte[1024];
        while ((read = input.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        file.deleteOnExit();
    } catch (IOException ex) {
        ex.printStackTrace();
    }
} else {
    file = new File(res.getFile());
}
```

然而这种方式，也只能操作文件，因为创建出来的是临时文件而非临时文件夹。估计还是有办法解决的，将输入流转为文件夹？这里我也没继续研究了，而是再次搜索解决方案。最终的解决方法如下所示，经过测试，可以完美解决这个问题，顺利读取资源子文件夹中所有文件：

```
URL url = Main.class.getClassLoader().getResource("xml/");
String jarPath = url.toString().substring(0, url.toString().indexOf("!/") + 2);

URL jarURL = new URL(jarPath);
JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
JarFile jarFile = jarCon.getJarFile();
Enumeration<JarEntry> jarEntrys = jarFile.entries();

while (jarEntrys.hasMoreElements()) {
    JarEntry entry = jarEntrys.nextElement();
    String name = entry.getName();
    if (name.startsWith("xml/") && !entry.isDirectory()) {
        doWithInputStream(Main.class.getClassLoader().getResourceAsStream(name));
    }
}
```





## 获取指定包名下继承或者实现某接口的所有类(扫描文件目录和所有jar)

```java
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by fanlinlong on 2017/4/13.
 */
public class Itest {
    public static void main(String[] args) {

        // 包下面的类
        Set<Class<?>> classes = getClasses("com.packge");
        if (classes == null) {
            System.out.printf("null");
        } else {
            System.out.printf(classes.size() + "");
            // 某类或者接口的子类
            Set<Class<?>> inInterface = getByInterface(BaseBean.class, classes);
            System.out.printf(inInterface.size() + "");

        }

    }

    /**
     * 从包package中获取所有的Class
     *
     * @param pack
     * @return
     */
    public static Set<Class<?>> getClasses(String pack) {

        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 是否循环迭代
        boolean recursive = true;
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(
                    packageDirName);
            // 循环迭代下去
            while (dirs.hasMoreElements()) {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    System.err.println("file类型的扫描");
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath,
                            recursive, classes);
                } else if ("jar".equals(protocol)) {
                    // 如果是jar包文件
                    // 定义一个JarFile
//                    System.err.println("jar类型的扫描");
                    JarFile jar;
                    try {
                        // 获取jar
                        jar = ((JarURLConnection) url.openConnection())
                                .getJarFile();
                        // 从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        // 同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            // 如果是以/开头的
                            if (name.charAt(0) == '/') {
                                // 获取后面的字符串
                                name = name.substring(1);
                            }
                            // 如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                // 如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    // 获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx)
                                            .replace('/', '.');
                                }
                                // 如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive) {
                                    // 如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class")
                                            && !entry.isDirectory()) {
                                        // 去掉后面的".class" 获取真正的类名
                                        String className = name.substring(
                                                packageName.length() + 1, name
                                                        .length() - 6);
                                        try {
                                            // 添加到classes
                                            classes.add(Class
                                                    .forName(packageName + '.'
                                                            + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        // log.error("在扫描用户定义视图时从jar包获取文件出错");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有Class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    public static void findAndAddClassesInPackageByFile(String packageName,
                                                        String packagePath, final boolean recursive, Set<Class<?>> classes) {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory())
                        || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles) {
            // 如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "."
                                + file.getName(), file.getAbsolutePath(), recursive,
                        classes);
            } else {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0,
                        file.getName().length() - 6);
                try {
                    // 添加到集合中去
                    //classes.add(Class.forName(packageName + '.' + className));
                    //经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    // log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                    e.printStackTrace();
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------------------------

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Set<Class<?>> getByInterface(Class clazz, Set<Class<?>> classesAll) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        //获取指定接口的实现类
        if (!clazz.isInterface()) {
            try {
                /**
                 * 循环判断路径下的所有类是否继承了指定类
                 * 并且排除父类自己
                 */
                Iterator<Class<?>> iterator = classesAll.iterator();
                while (iterator.hasNext()) {
                    Class<?> cls = iterator.next();
                    /**
                     * isAssignableFrom该方法的解析，请参考博客：
                     * http://blog.csdn.net/u010156024/article/details/44875195
                     */
                    if (clazz.isAssignableFrom(cls)) {
                        if (!clazz.equals(cls)) {//自身并不加进去
                            classes.add(cls);
                        } else {

                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("出现异常");
            }
        }
        return classes;
    }
}
```

