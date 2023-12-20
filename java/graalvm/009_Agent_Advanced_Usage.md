## Agent Advanced Usage 

### Caller-based Filters 

By default, the agent filters dynamic accesses which Native Image supports without configuration. The filter mechanism works by identifying the Java method performing the access, also referred to as *caller* method, and matching its declaring class against a sequence of filter rules. The built-in filter rules exclude dynamic accesses which originate in the JVM, or in parts of a Java class library directly supported by Native Image (such as `java.nio`) from the generated configuration files. Which item (class, method, field, resource, etc.) is being accessed is not relevant for filtering.

In addition to the built-in filter, custom filter files with additional rules can be specified using the `caller-filter-file` option. For example: `-agentlib:caller-filter-file=/path/to/filter-file,config-output-dir=...`

Filter files have the following structure:

```json
{ "rules": [
    {"excludeClasses": "com.oracle.svm.**"},
    {"includeClasses": "com.oracle.svm.tutorial.*"},
    {"excludeClasses": "com.oracle.svm.tutorial.HostedHelper"}
  ],
  "regexRules": [
    {"includeClasses": ".*"},
    {"excludeClasses": ".*\\$\\$Generated[0-9]+"}
  ]
}
Copy
```

The `rules` section contains a sequence of rules. Each rule specifies either `includeClasses`, which means that lookups originating in matching classes will be included in the resulting configuration, or `excludeClasses`, which excludes lookups originating in matching classes from the configuration. Each rule defines a pattern to match classes. The pattern can end in `.*` or `.**`, interpreted as follows: - `.*` matches all classes in the package and only that package; - `.**` matches all classes in the package as well as in all subpackages at any depth. Without `.*` or `.**`, the rule applies only to a single class with the qualified name that matches the pattern. All rules are processed in the sequence in which they are specified, so later rules can partially or entirely override earlier ones. When multiple filter files are provided (by specifying multiple `caller-filter-file` options), their rules are chained together in the order in which the files are specified. The rules of the built-in caller filter are always processed first, so they can be overridden in custom filter files.

In the example above, the first rule excludes lookups originating in all classes from package `com.oracle.svm` and from all of its subpackages (and their subpackages, etc.) from the generated metadata. In the next rule however, lookups from those classes that are directly in package `com.oracle.svm.tutorial` are included again. Finally, lookups from the `HostedHelper` class is excluded again. Each of these rules partially overrides the previous ones. For example, if the rules were in the reverse order, the exclusion of `com.oracle.svm.**` would be the last rule and would override all other rules.

The `regexRules` section also contains a sequence of rules. Its structure is the same as that of the `rules` section, but rules are specified as regular expression patterns which are matched against the entire fully qualified class identifier. The `regexRules` section is optional. If a `regexRules` section is specified, a class will be considered included if (and only if) both `rules` and `regexRules` include the class and neither of them exclude it. With no `regexRules` section, only the `rules` section determines whether a class is included or excluded.

For testing purposes, the built-in filter for Java class library lookups can be disabled by adding the `no-builtin-caller-filter` option, but the resulting metadata files are generally unsuitable for the build. Similarly, the built-in filter for Java VM-internal accesses based on heuristics can be disabled with `no-builtin-heuristic-filter` and will also generally lead to less usable metadata files. For example: `-agentlib:native-image-agent=no-builtin-caller-filter,no-builtin-heuristic-filter,config-output-dir=...`

### Access Filters 

Unlike the caller-based filters described above, which filter dynamic accesses based on where they originate, *access filters* apply to the *target* of the access. Therefore, access filters enable directly excluding packages and classes (and their members) from the generated configuration.

By default, all accessed classes (which also pass the caller-based filters and the built-in filters) are included in the generated configuration. Using the `access-filter-file` option, a custom filter file that follows the file structure described above can be added. The option can be specified more than once to add multiple filter files and can be combined with the other filter options, for example, `-agentlib:access-filter-file=/path/to/access-filter-file,caller-filter-file=/path/to/caller-filter-file,config-output-dir=...`.

### Specify Configuration Files as Arguments 

A directory containing configuration files that is not part of the class path can be specified to `native-image` via `-H:ConfigurationFileDirectories=/path/to/config-dir/`. This directory must directly contain all files: `jni-config.json`, `reflect-config.json`, `proxy-config.json` and `resource-config.json`. A directory with the same metadata files that is on the class path, but not in `META-INF/native-image/`, can be provided via `-H:ConfigurationResourceRoots=path/to/resources/`. Both `-H:ConfigurationFileDirectories` and `-H:ConfigurationResourceRoots` can also take a comma-separated list of directories.

### Injecting the Agent via the Process Environment 

Altering the `java` command line to inject the agent can prove to be difficult if the Java process is launched by an application or script file, or if Java is even embedded in an existing process. In that case, it is also possible to inject the agent via the `JAVA_TOOL_OPTIONS` environment variable. This environment variable can be picked up by multiple Java processes which run at the same time, in which case each agent must write to a separate output directory with `config-output-dir`. (The next section describes how to merge sets of configuration files.) In order to use separate paths with a single global `JAVA_TOOL_OPTIONS` variable, the agent’s output path options support placeholders:

```shell
export JAVA_TOOL_OPTIONS="-agentlib:native-image-agent=config-output-dir=/path/to/config-output-dir-{pid}-{datetime}/"
Copy
```

The `{pid}` placeholder is replaced with the process identifier, while `{datetime}` is replaced with the system date and time in UTC, formatted according to ISO 8601. For the above example, the resulting path could be: `/path/to/config-output-dir-31415-20181231T235950Z/`.

### Trace Files 

In the examples above, `native-image-agent` has been used to both keep track of the dynamic accesses on a JVM and then to generate a set of configuration files from them. However, for a better understanding of the execution, the agent can also write a *trace file* in JSON format that contains each individual access:

```shell
$JAVA_HOME/bin/java -agentlib:native-image-agent=trace-output=/path/to/trace-file.json ...
Copy
```

The `native-image-configure` tool can transform trace files to configuration files. The following command reads and processes `trace-file.json` and generates a set of configuration files in the directory `/path/to/config-dir/`:

```shell
native-image-configure generate --trace-input=/path/to/trace-file.json --output-dir=/path/to/config-dir/
Copy
```

### Interoperability 

The agent uses the JVM Tool Interface (JVMTI) and can potentially be used with other JVMs that support JVMTI. In this case, it is necessary to provide the absolute path of the agent:

```shell
/path/to/some/java -agentpath:/path/to/graalvm/jre/lib/amd64/libnative-image-agent.so=<options> ...
Copy
```

### Experimental Options 

The agent has options which are currently experimental and might be enabled in future releases, but can also be changed or removed entirely. See the [ExperimentalAgentOptions.md](https://www.graalvm.org/latest/reference-manual/native-image/metadata/ExperimentalAgentOptions/) guide.

## Native Image Configure Tool 

When using the agent in multiple processes at the same time as described in the previous section, `config-output-dir` is a safe option, but it results in multiple sets of configuration files. The `native-image-configure` tool can be used to merge these configuration files:

```shell
native-image-configure generate --input-dir=/path/to/config-dir-0/ --input-dir=/path/to/config-dir-1/ --output-dir=/path/to/merged-config-dir/
Copy
```

This command reads one set of configuration files from `/path/to/config-dir-0/` and another from `/path/to/config-dir-1/` and then writes a set of configuration files that contains both of their information to `/path/to/merged-config-dir/`. An arbitrary number of `--input-dir` arguments with sets of configuration files can be specified. See `native-image-configure help` for all options.

### Further Reading [#](https://www.graalvm.org/latest/reference-manual/native-image/metadata/AutomaticMetadataCollection/#further-reading)

- [Build a Native Executable with Reflection](https://www.graalvm.org/latest/reference-manual/native-image/guides/configure-with-tracing-agent/)
- [Reachability Metadata](https://www.graalvm.org/latest/reference-manual/native-image/metadata/)
- [Experimental Agent Options](https://www.graalvm.org/latest/reference-manual/native-image/metadata/ExperimentalAgentOptions/)