## 文件的删除与重命名

相关头文件和函数

```cpp

#include <libacformat/avformat.h>

/**
 * Move or rename a resource.
 *
 * @note url_src and url_dst should share the same protocol and authority.
 *
 * @param url_src url to resource to be moved
 * @param url_dst new url to resource if the operation succeeded
 * @return >=0 on success or negative on error.
 */
int avpriv_io_move(const char *url_src, const char *url_dst);

/**
 * Delete a resource.
 *
 * @param url resource to be deleted.
 * @return >=0 on success or negative on error.
 */
int avpriv_io_delete(const char *url);


```