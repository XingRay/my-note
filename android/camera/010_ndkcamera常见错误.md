undefined reference to `AImageReader_new'

https://stackoverflow.com/questions/51053580/undefined-reference-to-aimagereader-new



CAMERA NDK I add `.h(#include <media/NdkImageReader.h>)` to `.cpp.` when compile project, the function from `.h (#include <media/NdkImageReader.h>)`undefined reference.

```cpp
#include <media/NdkImageReader.h>
#include <media/NdkImage.h>
error: CMakeFiles/native-camera2-lib.dir/native-camera2-lib.cpp.o: In function` `Java_com_example_ts_camerandk_NativeCamera_openCamera':  D:\AndroidStudioProjects\camerandk\app\src\main\jni\native-camera2-lib.cpp:(.text+0x348): undefined reference to AImageReader_new'  D:\AndroidStudioProjects\camerandk\app\src\main\jni\native-camera2-lib.cpp:(.text+0x378): undefined reference to AImageReader_setImageListener  CMakeFiles/native-camera2-lib.dir/native-camera2-lib.cpp.o: In function Java_com_example_ts_camerandk_NativeCamera_startPreview':  D:\AndroidStudioProjects\camerandk\app\src\main\jni\native-camera2-lib.cpp:(.text+0x6e4): undefined reference to AImageReader_getWindow  clang++.exe: error: linker command failed with exit code 1 (use -v to see invocation)
enter code here
```



Check your cmake target libraries line in CMakeLists.txt file. You are forgetting to include `mediandk`

```scss
target_link_libraries(... camera2ndk mediandk ...)
```

Media NDK is a different library, and is not a part of Camera or Camera2.



