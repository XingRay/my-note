## Java项目打包

java --module-path target\classes --module com.xingray.nativeimage.javafx


java --module-path target\classes;output\dependency --class-path output\dependency --module com.xingray.nativeimage.javafx/com.xingray.nativeimage.javafx.Launcher





打包后的exe文件双击打开会有一个控制台窗口，可以使用以下的指令取消掉窗口：

```cmd
EDITBIN /SUBSYSTEM:WINDOWS launcher.exe
```

参考：https://github.com/oracle/graal/issues/2256

https://learn.microsoft.com/en-us/cpp/build/reference/subsystem?view=msvc-170&viewFallbackFrom=vs-2019

https://learn.microsoft.com/en-us/cpp/build/reference/subsystem-specify-subsystem?view=msvc-170

https://learn.microsoft.com/en-us/cpp/build/reference/editbin-reference?view=msvc-170

https://learn.microsoft.com/en-us/cpp/build/reference/linker-options?view=msvc-170

## Syntax

> **`/SUBSYSTEM:`** { **`BOOT_APPLICATION`** | **`CONSOLE`** | **`EFI_APPLICATION`** |
>   **`EFI_BOOT_SERVICE_DRIVER`** | **`EFI_ROM`** | **`EFI_RUNTIME_DRIVER`** | **`NATIVE`** |
>   **`POSIX`** | **`WINDOWS`** }
>   [ **`,`***`major`* [ **`.`***`minor`* ]]



## Arguments

**`BOOT_APPLICATION`**
An application that runs in the Windows boot environment. For more information about boot applications, see [About BCD](https://learn.microsoft.com/en-us/previous-versions/windows/desktop/bcd/about-bcd).

**`CONSOLE`**
Win32 character-mode application. The operating system provides a console for console applications. If `main` or `wmain` is defined for native code, `int main(array<String ^> ^)` is defined for managed code, or you build the application completely by using `/clr:safe`, CONSOLE is the default.

**`EFI_APPLICATION`**
**`EFI_BOOT_SERVICE_DRIVER`**
**`EFI_ROM`**
**`EFI_RUNTIME_DRIVER`**
The Extensible Firmware Interface subsystems. For more information, see the [UEFI specification](https://uefi.org/specifications). For examples, see the Intel [UEFI Driver and Application Tool Resources](https://www.intel.com/content/www/us/en/architecture-and-technology/unified-extensible-firmware-interface/uefi-driver-and-application-tool-resources.html). The minimum version and default version is 1.0.

**`NATIVE`**
Kernel mode drivers for Windows NT. This option is normally reserved for Windows system components. If [`/DRIVER:WDM`](https://learn.microsoft.com/en-us/cpp/build/reference/driver-windows-nt-kernel-mode-driver?view=msvc-170) is specified, `NATIVE` is the default.

**`POSIX`**
Application that runs with the POSIX subsystem in Windows NT.

**`WINDOWS`**
The application doesn't require a console, probably because it creates its own windows for interaction with the user. If `WinMain` or `wWinMain` is defined for native code, or `WinMain(HINSTANCE *, HINSTANCE *, char *, int)` or `wWinMain(HINSTANCE *, HINSTANCE *, wchar_t *, int)` is defined for managed code, `WINDOWS` is the default.

*`major`* and *`minor`*
(Optional) Specify the minimum required version of the subsystem. The arguments are decimal numbers in the range 0 through 65,535. There are no upper bounds for version numbers.
