# Java调用第三方dll文件的使用方法 

System.load()或System.loadLibrary()

https://www.cnblogs.com/wq-9/p/17027155.html



System.load 和 System.loadLibrary详解
1.它们都可以用来装载库文件，不论是JNI库文件还是非JNI库文件。在任何本地方法被调用之前必须先用这个两个方法之一把相应的JNI库文件装载。

2.System.load 参数为库文件的绝对路径，可以是任意路径。


例如你可以这样载入一个windows平台下JNI库文件：

```
System.load(“C://Documents and Settings//TestJNI.dll”);
```

System.loadLibrary 参数为库文件名，不包含库文件的扩展名。
例如你可以这样载入一个windows平台下JNI库文件

```
System. loadLibrary (“TestJNI”);
```

这里，TestJNI.dll 必须是在java.library.path这一jvm变量所指向的路径中。
可以通过如下方法来获得该变量的值：

```
System.getProperty(“java.library.path”);
```


默认情况下，在Windows平台下，该值包含如下位置：
1）和jre相关的一些目录
2）程序当前目录
3）Windows目录
4）系统目录（system32）
5）系统环境变量path指定目录

 

4.如果你要载入的库文件静态链接到其它动态链接库，例如TestJNI.dll 静态链接到dependency.dll, 那么你必须注意：


1）如果你选择
System.load(“C://Documents and Settings// TestJNI.dll”);
那么即使你把dependency.dll同样放在C://Documents and Settings//下，load还是会因为找不到依赖的dll而失败。因为jvm在载入TestJNI.dll会先去载入TestJNI.dll所依赖的库文件dependency.dll，而dependency.dll并不位于java.library.path所指定的目录下，所以jvm找不到dependency.dll。
你有两个方法解决这个问题：一是把C://Documents and Settings//加入到java.library.path的路径中，例如加入到系统的path中。二是先调用
System.load(“C://Documents and Settings// dependency.dll”); 让jvm先载入dependency.dll，然后再调用System.load(“C://Documents and Settings// TestJNI.dll”);


2）如果你选择
System. loadLibrary (“TestJNI”);
那么你只要把dependency.dll放在任何java.library.path包含的路径中即可，当然也包括和TestJNI.dll相同的目录。


https://blog.csdn.net/aas3637721/article/details/102716494

 

Java调用第三方dll文件的使用方法 System.load()或System.loadLibrary()

[![复制代码](D:\my-note\java\native\assets\copycode.gif)](javascript:void(0);)

```
public class OtherAdapter {
static
    {
        //System.loadLibrary("Connector");//载入需要调用的dll  Connector.dll
        System.load("d://Connector.dll");//载入dll  Connector.dll
    }
 
    //用native关键字修饰将被其它语言实现的方法
    //dll文件中对应的函数声明
       public native static int _PWLogon(String lpszDS,String lpszUser,String lpszPWD);
    //函数声明
       public native static void _PWLogout();
    public native static String _GetPWLastError();
    public native static String _GetPWFolders(long lParentFolderno);
    public native static String _GetPWDocuments(long lFolderno);
    public native static String _GetPWFiles(long lFolderno,long lDocno);
 
   //public native static String pirntStr(String msg);//函数声明
   public static void main(String[] args){
        //本地方法的调用
        int rs=_PWLogon("服务器","账号","密码");
        System.out.println("用户登录状态："+rs);
                if(rs==1)
                {
                    System.out.println("获取顶级目录："+_GetPWFolders(0));
 
                    System.out.println("获取父子目录："+_GetPWDocuments(54));
 
                    System.out.println("下载指定文件："+_GetPWFiles(54,2));
 
                    _PWLogout();
                }
                else
                {
                    System.out.println("获取错误信息："+_GetPWLastError());
                }
    }
}
```

[![复制代码](D:\my-note\java\native\assets\copycode.gif)](javascript:void(0);)

https://www.shuzhiduo.com/A/Gkz1oP4n5R/