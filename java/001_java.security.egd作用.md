# java.security.egd 作用

**SecureRandom**在java各种组件中使用广泛，可以**可靠的产生随机数**。但在大量产生随机数的场景下，性能会较低。这时可以使用"-Djava.security.egd=file:/dev/./urandom"加快随机数产生过程。
以产生[uuid](https://so.csdn.net/so/search?q=uuid&spm=1001.2101.3001.7020)的时候使用nextBytes产生随机数为入口，我们看一下SecureRandom的代码逻辑。

```java
   public static UUID randomUUID() {
        SecureRandom ng =Holder.numberGenerator;
        byte[] randomBytes = new byte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f;  /* clear version       */
        randomBytes[6]  |=0x40;  /* set to version 4     */
        randomBytes[8] &= 0x3f;  /* clear variant       */
        randomBytes[8]  |=0x80;  /* set to IETF variant  */
        return newUUID(randomBytes);
    }
12345678910
```

使用了SecureRandom.next*的方法。

在使用SecureRandom产生下一个随机数的时候调用nextLong或者nextBytes，最终会调用SecureRandom的nextBytes。

```java
 public long nextLong() { 
        // it's okay that the bottom wordremains signed. 
        return ((long)(next(32)) << 32)+ next(32); 
    } 
 
    final protected int next(int numBits) { 
        int numBytes = (numBits+7)/8; 
        byte b[] = new byte[numBytes]; 
        int next = 0; 
 
        nextBytes(b);
        for (int i = 0; i < numBytes; i++) 
            next = (next << 8)+ (b[i] & 0xFF); 
        return next >>> (numBytes*8 -numBits); 
    }

12345678910111213141516
```

而nextBytes是一个同步的方法，在多线程使用时，可能会产生性能瓶颈。

```java
synchronized public void nextBytes(byte[] bytes) { 
       secureRandomSpi.engineNextBytes(bytes); 
    }
123
```

secureRandomSpi被初始化为sun.security.provider.SecureRandom

secureRandomSpi是SecureRandom.NativePRNG的一个实例。

使用jvm参数-Djava.security.debug=all ，可以打印securityprovider列表，**从中可以看出，SecureRandom.NativePRNG由sun.security.provider.NativePRNG提供服务**。

```java
Provider: Set SUN provider property[SecureRandom.NativePRNG/sun.security.provider.NativePRNG]
1
```

分析openjdk的源码，NativePRNG.engineNextBytes调用了NativePRNG.RandomIO.ensureBufferValid，而ensureBufferValid直接从urandom读取数据：

```java
private void ensureBufferValid() throws IOException {
            ...
            readFully(urandomIn, urandomBuffer);
            ...
        }
12345
```

通过测试可以发现**，hotspot需要使用配置项"-Djava.security.egd=file:/dev/./urandom"才能从urandom读取数据，这里openjdk做了优化，直接从urandom读取数据**。

**/dev/random在产生大量随机数的时候比/dev/urandom慢**，所以，建议在大量使用随机数的时候，**将随机数发生器指定为/dev/./urandom**。

注意：jvm参数值为/dev/./urandom而不是/dev/urandom，这里是jdk的一个bug引起。

bug产生的原因请注意下面第四行源码，如果java.security.egd参数指定的是file:/dev/random或者file:/dev/urandom，则调用了无参的NativeSeedGenerator构造函数，而无参的构造函数将默认使用file:/dev/random 。openjdk的代码和hotspot的代码已经不同，openjdk在后续产生随机数的时候没有使用这个变量。

```java
abstract class SeedGenerator {
......
    static {
        String egdSource = SunEntries.getSeedSource();
        if (egdSource.equals(URL_DEV_RANDOM) || egdSource.equals(URL_DEV_URANDOM)) {
            try {
                instance = new NativeSeedGenerator();
                if (debug != null) {
                    debug.println("Using operating system seed generator");
                }
            } catch (IOException e) {
                if (debug != null) {
                    debug.println("Failed to use operating system seed "
                                  + "generator: " + e.toString());
                }
            }
        } else if (egdSource.length() != 0) {
            try {
                instance = new URLSeedGenerator(egdSource);
                if (debug != null) {
                    debug.println("Using URL seed generator reading from "
                                  + egdSource);
                }
            } catch (IOException e) {
                if (debug != null)
                    debug.println("Failed to create seed generator with "
                                  + egdSource + ": " + e.toString());
            }
        }
......
    }
12345678910111213141516171819202122232425262728293031
```

在启动应用时配置 `-Djava.security.egd=file:/dev/./urandom` 可以一定程度上加快应用启动。

借鉴：https://blog.51cto.com/leo01/1795447





### java.security.SecureRandom源码分析

# java.security.SecureRandom源码分析

 原创

[mfcliu](https://blog.51cto.com/leo01)2016-07-04 00:11:09博主文章分类：[java.basic](https://blog.51cto.com/leo01/category3)©著作权

***文章标签\*[java](https://blog.51cto.com/topic/java.html)[uuid](https://blog.51cto.com/topic/uuid.html)[generator](https://blog.51cto.com/topic/generator.html)*****文章分类\*[Java](https://blog.51cto.com/nav/java)[后端开发](https://blog.51cto.com/nav/program)*****阅读数\**\*10000+\****

SecureRandom在java各种组件中使用广泛，可以可靠的产生随机数。但在大量产生随机数的场景下，性能会较低。这时可以使用[ "-Djava.security.egd=](http://file/dev/urandom"这个参数将不生效。)[ file:/dev/./urandom](http://file/dev/urandom)[ "](http://file/dev/urandom"这个参数将不生效。)加快随机数产生过程。

以产生uuid的时候使用nextBytes产生随机数为入口，我们看一下SecureRandom的代码逻辑。

 

```java
   public static UUID randomUUID() {
        SecureRandom ng =Holder.numberGenerator;
 
        byte[] randomBytes = newbyte[16];
        ng.nextBytes(randomBytes);
        randomBytes[6] &= 0x0f;  /* clear version       */
        randomBytes[6]  |=0x40;  /* set to version 4     */
        randomBytes[8] &= 0x3f;  /* clear variant       */
        randomBytes[8]  |=0x80;  /* set to IETF variant  */
        return newUUID(randomBytes);
    }1.2.3.4.5.6.7.8.9.10.11.
```



 使用了SecureRandom.next*的方法。

 

在使用SecureRandom产生下一个随机数的时候调用nextLong或者nextBytes，最终会调用SecureRandom的nextBytes。

```java
    public long nextLong() { 
        // it's okay that the bottom wordremains signed. 
        return ((long)(next(32)) << 32)+ next(32); 
    } 
 
    final protected int next(int numBits) { 
        int numBytes = (numBits+7)/8; 
        byte b[] = new byte[numBytes]; 
        int next = 0; 
 
        nextBytes(b);
        for (int i = 0; i < numBytes; i++) 
            next = (next << 8)+ (b[i] & 0xFF); 
        return next >>> (numBytes*8 -numBits); 
    }1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.
```



而nextBytes是一个同步的方法，在多线程使用时，可能会产生性能瓶颈。

```java
synchronized public void nextBytes(byte[] bytes) { 
       secureRandomSpi.engineNextBytes(bytes); 
    }1.2.3.
```



secureRandomSpi被初始化为sun.security.provider.SecureRandom

secureRandomSpi是SecureRandom.NativePRNG的一个实例。



使用jvm参数-Djava.security.debug=all ，可以打印securityprovider列表，从中可以看出，SecureRandom.NativePRNG由sun.security.provider.NativePRNG提供服务。

**Provider: Set SUN provider property[SecureRandom.NativePRNG/sun.security.provider.NativePRNG]**



分析openjdk的源码，NativePRNG.engineNextBytes调用了NativePRNG.RandomIO.ensureBufferValid，而ensureBufferValid直接从urandom读取数据：

```java
private void ensureBufferValid() throws IOException {
            ...
            readFully(urandomIn, urandomBuffer);
            ...
        }1.2.3.4.5.
```

通过测试可以发现，hotspot需要使用配置项[ "-Djava.security.egd=](http://file/dev/urandom"这个参数将不生效。)[ file:/dev/./urandom](http://file/dev/urandom)[ "](http://file/dev/urandom"这个参数将不生效。)才能从urandom读取数据，这里openjdk做了优化，直接从urandom读取数据。

 

/dev/random在产生大量随机数的时候比/dev/urandom慢，所以，建议在大量使用随机数的时候，将随机数发生器指定为/dev/./urandom。



注意：jvm参数值为/dev/./urandom而不是/dev/urandom，这里是jdk的一个bug引起。

bug产生的原因请注意下面第四行源码，如果java.security.egd参数指定的是file:/dev/random或者file:/dev/urandom，则调用了无参的NativeSeedGenerator构造函数，而无参的构造函数将默认使用file:/dev/random 。openjdk的代码和hotspot的代码已经不同，openjdk在后续产生随机数的时候没有使用这个变量。

```java
abstract class SeedGenerator {
......
    static {
        String egdSource = SunEntries.getSeedSource();
        if (egdSource.equals(URL_DEV_RANDOM) || egdSource.equals(URL_DEV_URANDOM)) {
            try {
                instance = new NativeSeedGenerator();
                if (debug != null) {
                    debug.println("Using operating system seed generator");
                }
            } catch (IOException e) {
                if (debug != null) {
                    debug.println("Failed to use operating system seed "
                                  + "generator: " + e.toString());
                }
            }
        } else if (egdSource.length() != 0) {
            try {
                instance = new URLSeedGenerator(egdSource);
                if (debug != null) {
                    debug.println("Using URL seed generator reading from "
                                  + egdSource);
                }
            } catch (IOException e) {
                if (debug != null)
                    debug.println("Failed to create seed generator with "
                                  + egdSource + ": " + e.toString());
            }
        }
......
    }1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18.19.20.21.22.23.24.25.26.27.28.29.30.31.
```



