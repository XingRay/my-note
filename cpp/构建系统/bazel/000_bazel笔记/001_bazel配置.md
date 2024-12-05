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



`.bazelrc` 文件的路径：

全局配置：默认位于用户目录下，例如：

Windows：`C:\Users\<用户名>\.bazelrc`

Linux/macOS：`~/.bazelrc`



**工作区配置**：可以在当前项目的根目录下创建一个 `.bazelrc` 文件，项目级别的配置会覆盖全局配置。

如果需要指定一个自定义 `.bazelrc` 文件，可以通过 `--bazelrc=<路径>` 启动选项加载：

```bash
bazel --bazelrc=custom_path/.bazelrc build ...
```
