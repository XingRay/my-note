# msys2配置



更新系统

```
pacman -Syu
```

安装 mingw-w64 GCC

```
pacman -S mingw-w64-ucrt-x86_64-gcc
```

安装fortran

```
pacman -S mingw-w64-x86_64-gcc-fortran
```

将 D:\develop\msys2\mingw64\bin 添加到 PATH

测试

```shell
PS C:\Users\leixing> gfortran --version
GNU Fortran (Rev8, Built by MSYS2 project) 15.2.0
Copyright (C) 2025 Free Software Foundation, Inc.
This is free software; see the source for copying conditions.  There is NO
warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
```

