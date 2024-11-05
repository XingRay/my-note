# Protoc编译proto文件到源码



```
protoc --cpp_out=output_directory path/to/your_proto_file/example.proto
```



```
Get-ChildItem -Path "C:\path\to\input_dir" -Recurse -Filter "*.proto" | ForEach-Object {
    & protoc --cpp_out="C:\path\to\output_dir" $_.FullName
}
```





```
param (
    [string]$protoDir = ".",
    [string]$outputDir = ".",
    [string]$language = "cpp"
)

# 确保输出目录存在
if (!(Test-Path -Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir
}

# 查找所有 .proto 文件并编译
Get-ChildItem -Path $protoDir -Recurse -Filter *.proto | ForEach-Object {
    $protoFile = $_.FullName
    Write-Host "Compiling $protoFile to $language..."

    # 根据指定语言选择输出选项
    switch ($language) {
        "cpp" {
            $outOption = "--cpp_out=$outputDir"
        }
        "python" {
            $outOption = "--python_out=$outputDir"
        }
        "java" {
            $outOption = "--java_out=$outputDir"
        }
        "csharp" {
            $outOption = "--csharp_out=$outputDir"
        }
        default {
            Write-Host "Unsupported language: $language. Supported languages are cpp, python, java, csharp."
            exit 1
        }
    }

    # 调用 protoc 生成代码
    & "protoc" "-I=$protoDir" $outOption "$protoFile"
}

Write-Host "Compilation completed."

```

