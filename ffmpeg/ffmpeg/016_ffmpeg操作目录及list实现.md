## ffmpeg操作目录及list实现

### 相关函数及结构体

```c

/**
 * Open directory for reading.
 *
 * @param s       directory read context. Pointer to a NULL pointer must be passed.
 * @param url     directory to be listed.
 * @param options A dictionary filled with protocol-private options. On return
 *                this parameter will be destroyed and replaced with a dictionary
 *                containing options that were not found. May be NULL.
 * @return >=0 on success or negative on error.
 */
int avio_open_dir(AVIODirContext **s, const char *url, AVDictionary **options);

/**
 * Get next directory entry.
 *
 * Returned entry must be freed with avio_free_directory_entry(). In particular
 * it may outlive AVIODirContext.
 *
 * @param s         directory read context.
 * @param[out] next next entry or NULL when no more entries.
 * @return >=0 on success or negative on error. End of list is not considered an
 *             error.
 */
int avio_read_dir(AVIODirContext *s, AVIODirEntry **next);

/**
 * Close directory.
 *
 * @note Entries created using avio_read_dir() are not deleted and must be
 * freeded with avio_free_directory_entry().
 *
 * @param s         directory read context.
 * @return >=0 on success or negative on error.
 */
int avio_close_dir(AVIODirContext **s);


/**
 * Describes single entry of the directory.
 *
 * Only name and type fields are guaranteed be set.
 * Rest of fields are protocol or/and platform dependent and might be unknown.
 */
typedef struct AVIODirEntry

typedef struct AVIODirContext

```
示例：

 ```c

extern "C" {
#include <stdint.h>
#include <libavutil/log.h>
#include <libavformat/avformat.h>
}

int main(int argc, char **argv) {
    av_log_set_level(AV_LOG_INFO);
    int ret;

    AVIODirContext *dirContext = NULL;
    AVIODirEntry *entry = NULL;

    char *path = "./";

    ret = avio_open_dir(&dirContext, path, NULL);
    if (ret < 0) {
        av_log(NULL, AV_LOG_ERROR, "cann't open dir : %d", ret);
        goto exit;
    }

    while (true) {
        ret = avio_read_dir(dirContext, &entry);
        if (ret < 0) {
            goto exit;
        }

        if (entry == NULL) {
            break;
        }

        av_log(NULL, AV_LOG_INFO, "%12"PRId64", %s \n", entry->size, entry->name);
    }

    exit:
    {
        avio_free_directory_entry(&entry);
        avio_close_dir(&dirContext);
    }
    
    return ret;
}

 ```
 