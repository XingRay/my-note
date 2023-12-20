# Add Logging to a Native Executable

By default, a native executable produced by Native Image supports logging via the `java.util.logging.*` API.

## Default Logging Configuration

The default logging configuration in a native executable is based on the `logging.properties` file found in the JDK. This file configures a `java.util.logging.ConsoleHandler` which will only show messages at the `INFO` level and above. Custom logging configuration can be loaded either at executable build time or at runtime as described below.

If you require additional logging handlers, you must register the corresponding classes for reflection. For example, if you use `java.util.logging.FileHandler` then provide the following reflection configuration:

```json
{
    "name" : "java.util.logging.FileHandler",
    "methods" : [
      { "name" : "<init>", "parameterTypes" : [] },
    ]
  }
Copy
```

For more details, see [Reflection Support](https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Reflection/).

## Initializing a Logger at Build Time

The logger can be initialized at executable build time with a custom *logging.properties* configuration file, as illustrated in following example.

1. Make sure you have installed a GraalVM JDK. The easiest way to get started is with [SDKMAN!](https://sdkman.io/jdks#graal). For other installation options, visit the [Downloads section](https://www.graalvm.org/downloads/).

2. Save the following Java code into a file named

    

   LoggerBuildTimeInit.java

   , then compile it using

    

   ```
   javac
   ```

   :

   ```java
    import java.io.IOException;
    import java.util.logging.Level;
    import java.util.logging.LogManager;
    import java.util.logging.Logger;
   
    public class LoggerBuildTimeInit {
        private static final Logger LOGGER;
        static {
            try {
                LogManager.getLogManager().readConfiguration(LoggerBuildTimeInit.class.getResourceAsStream("/logging.properties"));
            } catch (IOException | SecurityException | ExceptionInInitializerError ex) {
                Logger.getLogger(LoggerBuildTimeInit.class.getName()).log(Level.SEVERE, "Failed to read logging.properties file", ex);
            }
            LOGGER = Logger.getLogger(LoggerBuildTimeInit.class.getName());
        }
   
        public static void main(String[] args) throws IOException {
            LOGGER.log(Level.WARNING, "Danger, Will Robinson!");
        }
    } 
   Copy
   ```

3. Download the [*logging.properties*](https://www.graalvm.org/docs/reference-manual/native-image/assets/logging.properties) resource file and save it in the same directory as *LoggerBuildTimeInit.java*.

4. Build and run the native executable:

   ```shell
    native-image LoggerBuildTimeInit --initialize-at-build-time=LoggerBuildTimeInit
   Copy
   ```

   ```shell
    ./loggerbuildtimeinit
   Copy
   ```

   It should produce output that looks similar to:

   ```shell
    WARNING: Danger, Will Robinson! [Wed May 18 17:20:39 BST 2022]
   Copy
   ```

   This demonstrates that the *logging.properties* file is processed at when the executable is built. The file does not need to be included in the native executable and reduces the size of the resulting executable file.

   `LoggerHolder.LOGGER` is also initialized at build time and is readily available at runtime, therefore improving the startup time. Unless your application needs to process a custom *logging.properties* configuration file at runtime, this approach is recommended.

## Initializing a Logger at Run Time

The logger can also be initialized at run time, as shown in the following example.

1. Save the following Java code into a file named *LoggerRunTimeInit.java*, then compile it using `javac`:

   ```java
    import java.io.IOException;
    import java.util.logging.Level;
    import java.util.logging.LogManager;
    import java.util.logging.Logger;
       
    public class LoggerRunTimeInit {
        public static void main(String[] args) throws IOException {
            LogManager.getLogManager().readConfiguration(LoggerRunTimeInit.class.getResourceAsStream("/logging.properties"));
            Logger logger = Logger.getLogger(LoggerRunTimeInit.class.getName());
            logger.log(Level.WARNING, "Danger, Will Robinson!");
        }
    }
   Copy
   ```

2. Download the [*logging.properties*](https://www.graalvm.org/docs/reference-manual/native-image/assets/logging.properties) resource file and save it in the same directory as *LoggerRunTimeInit.java*.

3. Build and run the native executable

   ```shell
    native-image LoggerRunTimeInit -H:IncludeResources="logging.properties"
   ```

   ```shell
    ./loggerruntimeinit
   ```

   It should produce output that looks similar to:

   ```
    WARNING: Danger, Will Robinson! [Wed May 18 17:22:40 BST 2022]
   ```

   In this case, the *logging.properties* file needs to be available for runtime processing and it must be included in the executable via the `-H:IncludeResources=logging.properties` option. For more details on this option, see [Use of Resources in a Native Executable](https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/Resources/).

### Related Documentation

- [Reachability Metadata: Reflection](https://www.graalvm.org/latest/reference-manual/native-image/metadata/#reflection)
- [Native Image Build Configuration](https://www.graalvm.org/latest/reference-manual/native-image/overview/BuildConfiguration/)