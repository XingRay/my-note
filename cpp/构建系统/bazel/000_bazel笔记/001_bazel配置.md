# bazel配置



windows

```bash
C:\Users\<user_name>\.bazelrc
```

内容:

```bash
startup --output_user_root="bazel_output"


build --distdir="D:/develop/bazel/dist"
build --repository_cache="D:/develop/bazel/repository_cache"
build --experimental_convenience_symlinks=ignore
```



相当于:

```bash
bazel --output_user_root="bazel_output" build --distdir="D:/develop/bazel/dist" --repository_cache="D:/develop/bazel/repository_cache" --experimental_convenience_symlinks=ignore xxx
```

