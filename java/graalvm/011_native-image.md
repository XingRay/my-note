```shell
native-image --help
```

输出:

```shell
GraalVM Native Image (https://www.graalvm.org/native-image/)

This tool can ahead-of-time compile Java code to native executables.

Usage: native-image [options] class [imagename] [options]
           (to build an image for a class)
   or  native-image [options] -jar jarfile [imagename] [options]
           (to build an image for a jar file)
   or  native-image [options] -m <module>[/<mainclass>] [options]
       native-image [options] --module <module>[/<mainclass>] [options]
           (to build an image for a module)

where options include:

    @argument files       one or more argument files containing options
    -cp <class search path of directories and zip/jar files>
    -classpath <class search path of directories and zip/jar files>
    --class-path <class search path of directories and zip/jar files>
                          A ; separated list of directories, JAR archives,
                          and ZIP archives to search for class files.
    -p <module path>
    --module-path <module path>...
                          A ; separated list of directories, each directory
                          is a directory of modules.
    --add-modules <module name>[,<module name>...]
                          root modules to resolve in addition to the initial module.
                          <module name> can also be ALL-DEFAULT, ALL-SYSTEM,
                          ALL-MODULE-PATH.
    -D<name>=<value>      set a system property
    -J<flag>              pass <flag> directly to the JVM running the image generator
    --diagnostics-mode    enable diagnostics output: class initialization, substitutions, etc.
    --enable-preview      allow classes to depend on preview features of this release
    --enable-native-access <module name>[,<module name>...]
                          modules that are permitted to perform restricted native operations.
                          <module name> can also be ALL-UNNAMED.
    --verbose             enable verbose output
    --version             print product version and exit
    --help                print this help message
    --help-extra          print help on non-standard options

    --auto-fallback       build stand-alone image if possible
    --color               color build output ('always', 'never', or 'auto')
    --configure-reflection-metadata
                          enable runtime instantiation of reflection objects for non-invoked
                          methods.
    --enable-all-security-services
                          add all security service classes to the generated image.
    --enable-http         enable http support in the generated image
    --enable-https        enable https support in the generated image
    --enable-monitoring   enable monitoring features that allow the VM to be inspected at
                          run time. Comma-separated list can contain 'heapdump', 'jfr',
                          'jvmstat', 'jmxserver' (experimental), 'jmxclient'
                          (experimental), or 'all' (deprecated behavior: defaults to 'all'
                          if no argument is provided). For example:
                          '--enable-monitoring=heapdump,jfr'.
    --enable-sbom         embed a Software Bill of Materials (SBOM) in the executable or
                          shared library for passive inspection. A comma-separated list
                          can contain 'cyclonedx',  'strict' (defaults to 'cyclonedx' if
                          no argument is provided), or 'export' to save the SBOM to the
                          native executable's output directory. The optional 'strict' flag
                          aborts the build if any class cannot be matched to a library in
                          the SBOM. For example: '--enable-sbom=cyclonedx,strict'.
    --enable-url-protocols
                          list of comma separated URL protocols to enable.
    --features            a comma-separated list of fully qualified Feature implementation
                          classes
    --force-fallback      force building of fallback image
    --gc=<value>          select native-image garbage collector implementation. Allowed
                          options for <value>:
                          'G1': G1 garbage collector
                          'epsilon': Epsilon garbage collector
                          'serial': Serial garbage collector (default)
    --initialize-at-build-time
                          a comma-separated list of packages and classes (and implicitly all
                          of their superclasses) that are initialized during image
                          generation. An empty string designates all packages.
    --initialize-at-run-time
                          a comma-separated list of packages and classes (and implicitly all
                          of their subclasses) that must be initialized at runtime and not
                          during image building. An empty string is currently not
                          supported.
    --install-exit-handlers
                          provide java.lang.Terminator exit handlers
    --libc                selects the libc implementation to use. Available implementations:
                          glibc, musl, bionic
    --link-at-build-time  require types to be fully defined at image build-time. If used
                          without args, all classes in scope of the option are required to
                          be fully defined.
    --link-at-build-time-paths
                          require all types in given class or module-path entries to be
                          fully defined at image build-time.
    --list-cpu-features   show CPU features specific to the target platform and exit.
    --list-modules        list observable modules and exit.
    --native-compiler-options
                          provide custom C compiler option used for query code compilation.
    --native-compiler-path
                          provide custom path to C compiler used for query code compilation
                          and linking.
    --native-image-info   show native-toolchain information and image-build settings
    --no-fallback         build stand-alone image or report failure
    --parallelism         the maximum number of threads to use concurrently during native
                          image generation.
    --pgo                 a comma-separated list of files from which to read the data
                          collected for profile-guided optimization of AOT compiled code
                          (reads from default.iprof if nothing is specified). Each file
                          must contain a single PGOProfiles object, serialized in JSON
                          format, optionally compressed by gzip.
    --pgo-instrument      instrument AOT compiled code to collect data for profile-guided
                          optimization into default.iprof file
    --pgo-sampling        perform profiling by sampling the AOT compiled code to collect
                          data for profile-guided optimization.
    --report-unsupported-elements-at-runtime
                          report usage of unsupported methods and fields at run time when
                          they are accessed the first time, instead of as an error during
                          image building
    --shared              build shared library
    --silent              silence build output
    --static              build statically linked executable (requires static libc and zlib)
    --strict-image-heap   enable the strict image heap mode that allows all classes to be
                          used at build-time but also requires types of all objects in the
                          heap to be explicitly marked for build-time initialization.
    --target              selects native-image compilation target (in <OS>-<architecture>
                          format). Defaults to host's OS-architecture pair.
    --trace-class-initialization
                          comma-separated list of fully-qualified class names that class
                          initialization is traced for.
    --trace-object-instantiation
                          comma-separated list of fully-qualified class names that object
                          instantiation is traced for.
    -O                    control code optimizations: b - optimize for fastest build time, 0
                          - no optimizations, 1 - basic optimizations, 2 - advanced
                          optimizations, 3 - all optimizations for best performance.
    -da                   also -da[:[packagename]...|:classname] or
                          -disableassertions[:[packagename]...|:classname]. Disable
                          assertions with specified granularity at run time.
    -dsa                  also -disablesystemassertions. Disables assertions in all system
                          classes at run time.
    -ea                   also -ea[:[packagename]...|:classname] or
                          -enableassertions[:[packagename]...|:classname]. Enable
                          assertions with specified granularity at run time.
    -esa                  also -enablesystemassertions. Enables assertions in all system
                          classes at run time.
    -g                    generate debugging information
    -march                generate instructions for a specific machine type. Defaults to
                          'x86-64-v3' on AMD64 and 'armv8-a' on AArch64. Use
                          -march=compatibility for best compatibility, or -march=native
                          for best performance if the native executable is deployed on the
                          same machine or on a machine with the same CPU features. To list
                          all available machine types, use -march=list.
    -o                    name of the output file to be generated
```

