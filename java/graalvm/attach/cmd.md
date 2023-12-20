java --module-path target\classes --module com.xingray.nativeimage.javafx


java --module-path target\classes;output\dependency --class-path output\dependency --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher

java -agentlib:native-image-agent=config-output-dir=src\main\resources\META-INF\native-image --module-path target\classes;output\dependency --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher



native-image --no-fallback -H:+ReportExceptionStackTraces --module-path target\classes;output\dependency --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher


native-image --no-fallback -H:+ReportExceptionStackTraces --module-path target\classes;output\dependency --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher -H:ConfigurationFileDirectories=META-INF/native-image



java --module-path target\classes --module com.xingray.nativeimage.javafx


java --module-path target\classes;output\dependency --class-path output\dependency --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher

java -agentlib:native-image-agent=config-output-dir=src\main\resources\META-INF\native-image --module-path target\classes;output\dependency --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher

%GRAALVM_HOME_19%\bin\native-image --no-fallback -H:+ReportExceptionStackTraces -H:+TraceNativeToolUsage --class-path src\main\resources --module-path target\classes;output\dependency;D:\develop\java\javafx\19\javafx-sdk-19.0.2.1\lib;D:\develop\java\javafx\19\javafx-sdk-19.0.2.1\bin --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher launcher

%GRAALVM_HOME_19%\bin\native-image --no-fallback -H:+ReportExceptionStackTraces -H:+TraceNativeToolUsage --class-path src\main\resources --module-path target\classes  --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher launcher


launcher.exe -Djavafx.verbose=true -Dprism.verbose=true


%GRAALVM_HOME_19%\bin\native-image --no-fallback --no-server -H:+ReportExceptionStackTraces -H:+TraceNativeToolUsage --class-path src\main\resources --module-path target\classes;output\dependency  --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher launcher