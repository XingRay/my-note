# How To Work With Files In Java

You can use this guide to learn how to work with files in Java through the Path API. From reading and writing files, to watching directories & using in-memory file systems.

## Java’s File APIs

Java has two file APIs.

- The original `*java.io.File*` API, available since Java 1.0 (1996).
- The newer `*java.nio.file.Path*` API, available since Java 1.7 (2011).

### What is the difference between the File and Path APIs?

The old file API is used in a ton of older projects, frameworks and libraries. Despite its age, it is not deprecated (and likely never will be) and you can still use it with any of the latest Java versions.

Nevertheless, `*java.nio.file.Path*` does everything `*java.io.File*` can, but generally in a better way and more. A few examples:

- File Features: The new classes support symlinks, proper file attributes and metadata support (think: PosixFileAttributes), ACLs and more.
- Better usage: E.g. when deleting a file, you get an exception with a meaningful error message (no such file, file locked, etc.), instead of a simple boolean saying `*false*`.
- Decoupling: Enabling support for in-memory file systems, which we’ll cover later.

(For a full list of differences between the two APIs, check out this article: https://www.oracle.com/technical-resources/articles/javase/nio.html)

### Which file API should I use?

For the reasons mentioned above, if you are starting a new Java project, it is highly recommended to use the `*Paths*` API over the `*File*` API. (Even though *file* reads so much nicer than *path*, doesn’t it?)

Hence, we will focus solely on the `*Paths*` API in this article.

## Paths API

To work with files in Java, you first need a reference to a file (big surprise!). As we just mentioned above, starting with Java 7, you would use the Paths API to reference files, so it all starts with constructing `*Path*` objects.

Let’s see some code.

```
public static void main(String[] args) throws URISyntaxException {

    // Java11+  : Path.of()

    Path path = Path.of("c:\\dev\\licenses\\windows\\readme.txt");
    System.out.println(path);

    path = Path.of("c:/dev/licenses/windows/readme.txt");
    System.out.println(path);

    path = Path.of("c:" , "dev", "licenses", "windows", "readme.txt");
    System.out.println(path);

    path = Path.of("c:" , "dev", "licenses", "windows").resolve("readme.txt"); // resolve == getChild()
    System.out.println(path);

    path = Path.of(new URI("file:///c:/dev/licenses/windows/readme.txt"));
    System.out.println(path);

    // Java < 11 equivalent: Paths.get()
    path = Paths.get("c:/dev/licenses/windows/readme.txt");
    System.out.println(path);

    // etc...
}
```

Let’s break this down:

```
Path path = Path.of("c:\\dev\\licenses\\windows\\readme.txt");
System.out.println(path);

path = Path.of("c:/dev/licenses/windows/readme.txt");
System.out.println(path);
```

Starting with Java 11, you should use the static `*Path.of*` method to construct paths (we’ll cover the Java7-10 equivalent in a second).

It does not matter if you are using forward slashes e.g. on Windows, as the Path API is smart enough to construct the right path, independently of the OS and any forward-backward slash issues.

So, both lines above will return the following result, when running the main method.

```
c:\dev\licenses\windows\readme.txt
c:\dev\licenses\windows\readme.txt
```

There are more choices you have when constructing paths: You don’t have to specify the complete path as one long string:

```
path = Path.of("c:" , "dev", "licenses", "windows", "readme.txt");
System.out.println(path);

path = Path.of("c:" , "dev", "licenses", "windows").resolve("readme.txt"); // resolve == getChild()
System.out.println(path);
```

Instead, you can pass a sequence of strings to the `*Path.of*` method, or construct the parent directory and use it to get a child file (`*.resolve(child)*`).

Again, the output will be the same as before.

```
c:\dev\licenses\windows\readme.txt
c:\dev\licenses\windows\readme.txt
```

Last but not least, you can also pass URIs into the `*Path.of*` call.

```
path = Path.of(new URI("file:///c:/dev/licenses/windows/readme.txt"));
System.out.println(path);
```

It sounds like a broken record, but the output….will be the same.

```
c:\dev\licenses\windows\readme.txt
```

So, you have a variety of choices constructing your Path objects.

Two important points, however:

1. Constructing a path object or resolving a child, **does not** mean the file or directory actually exists. The path is merely a reference to a *potential* file. So, you’ll have to separately verify its existence.

2. Pre Java-11, `*Path.of*` was called `*Paths.get*`, which you’ll need to use if you’re stuck on older Java versions or building a library that needs some backward compatibility. Starting with Java 11, `*Paths.get*` internally redirects to `*Path.of*`.

   ```
   // Java < 11 equivalent: Paths.get()
   path = Paths.get("c:/dev/licenses/windows/readme.txt");
   System.out.println(path);
   ```

Once you have a path object, you can finally *do* something with it. Let’s see what and how in the next section.

## Common File Operations

When working with files or paths, you will likely be using the `*java.nio.file.Files*` class. It contains a ton of common & useful static methods, that operate on files and directories.

Use this section as a quick cheat sheet, the headings are self-explanatory.

### How to check if a file exists

```
Path path = Path.of("c:\\dev\\licenses\\windows\\readme.txt");
boolean exists = Files.exists(path);
System.out.println("exists = " + exists);
```

Checks if a file or directory exists. Also lets you specify additional parameters, to define how symlinks are handled, i.e. followed (default) or not.

When running this snippet, you’ll get a simple boolean flag back.

```
exists = true
```

### How to get the last modified date of a file

```
Path path = Path.of("c:\\dev\\licenses\\windows\\readme.txt");
FileTime lastModifiedTime = Files.getLastModifiedTime(path);
System.out.println("lastModifiedTime = " + lastModifiedTime);
```

Self-explanatory. Returns the last date your file was modified as a `*FileTime*` object.

```
lastModifiedTime = 2020-05-20T08:41:30.905176Z
```

### How to compare files (Java12+)

```
Path path = Path.of("c:\\dev\\licenses\\windows\\readme.txt");
long mismatchIndex = Files.mismatch(path, Paths.get("c:\\dev\\whatever.txt"));
System.out.println("mismatch = " + mismatchIndex);
```

This is a relatively new addition to Java, available since Java 12. It compares the sizes and bytes of two files and returns the position of the first (byte) mismatch. Or, -1L if there was no mismatch.

Hence, if you are comparing two completely different files, you’ll get this as console output: the very first byte already didn’t match, hence the mismatch is *position zero*.

```
mismatch = 0
```

### How to get the owner of a file

```
Path path = Path.of("c:\\dev\\licenses\\windows\\readme.txt");
UserPrincipal owner = Files.getOwner(path);
System.out.println("owner = " + owner);
```

Self explanatory. Returns the owner of a file or directory as `*UserPrincipal*` (which extends from `*Principal*`). On Windows, this will be a WindowsUserPrincipal, which contains the user’s account name (shown below), as well as his `*sid*`, his unique security identifier on your Windows machine.

```
owner = DESKTOP-168M0IF\marco_local (User)
```

### How to create temp files

```
Path tempFile1 = Files.createTempFile("somePrefixOrNull", ".jpg");
System.out.println("tempFile1 = " + tempFile1);

Path tempFile2 = Files.createTempFile(path.getParent(), "somePrefixOrNull", ".jpg");
System.out.println("tempFile2 = " + tempFile2);

Path tmpDirectory = Files.createTempDirectory("prefix");
System.out.println("tmpDirectory = " + tmpDirectory);
```

Let’s break this down.

```
Path tempFile1 = Files.createTempFile("somePrefixOrNull", ".jpg");
System.out.println("tempFile1 = " + tempFile1);
```

When creating temp files, you can specify a prefix (first param) and a suffix (second param). Both can be null.

The prefix will be prefixed (duh!) to the temp file name, the suffix is essentially the file extension, and if you leave it out a default extension of ".tmp" will be used.

The file will be created in the [default temporary-file directory](https://stackoverflow.com/questions/1924136/environment-variable-to-control-java-io-tmpdir).

```
Path tempFile2 = Files.createTempFile(path.getParent(), "somePrefixOrNull", ".jpg");
System.out.println("tempFile2 = " + tempFile2);
```

Instead of the default temp directory, you can also specify your own directory where you want the temp file to be created.

```
Path tmpDirectory = Files.createTempDirectory("prefix");
System.out.println("tmpDirectory = " + tmpDirectory);
```

In addition to files, you can also create temp directories. As you don’t need the suffix parameter when creating dirs, you only have to choice of specifying a prefix parameter.

When running the code snippet from above, you’ll get the following (or similar) output:

```
tempFile1 = C:\Users\marco\AppData\Local\Temp\somePrefixOrNull8747488053128491901.jpg
tempFile2 = c:\dev\licenses\windows\somePrefixOrNull11086918945318459411.jpg
tmpDirectory = C:\Users\marco\AppData\Local\Temp\prefix9583768274092262832
```

**Note**: Temp files, contrary to popular belief, **do not** delete themselves. You have to make sure to explicitly delete them, when creating them in unit tests or when running in production.

### How to create files and directories

You’ve seen how to create temp files, and it’s the very same thing with normal files and directories. You’ll just call different methods:

```
Path newDirectory = Files.createDirectories(path.getParent().resolve("some/new/dir"));
System.out.println("newDirectory = " + newDirectory);

Path newFile = Files.createFile(newDirectory.resolve("emptyFile.txt"));
System.out.println("newFile = " + newFile);
```

Some people are confused by this: The `*.resolve*` call does not create the file, it merely returns a *reference* to the (child) file you are about to create.

When running the code snippet from above, you’ll get the following (or similar) output:

```
newDirectory = c:\dev\licenses\windows\some\new\dir
newFile = c:\dev\licenses\windows\some\new\dir\emptyFile.txt
```

### How to get the Posix permissions of a file

If you are running your Java program on a Unix-like system (including Linux and MacOS), you can get a file’s Posix permissions. Think: "-rw-rw-rw-" or "-rwxrwxrwx" etc.

```
Path path = Path.of("c:\\dev\\licenses\\windows\\readme.txt");
try {
    Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
    System.out.println("permissions = " + permissions);
} catch (UnsupportedOperationException e) {
    System.err.println("Looks like you're not running on a posix file system");
}
```

Running this on Linux or MacOS, you would get this kind of output:

```
OWNER_WRITE
OWNER_READ
GROUP_WRITE
OTHERS_READ
...
```

## Writing & Reading Files

### How to write strings to files

We haven’t talked about the core of file-handling just yet: Writing to and reading from files.

Let’s see how you can do that:

```
Path utfFile = Files.createTempFile("some", ".txt");
Files.writeString(utfFile, "this is my string ää öö üü"); // UTF 8
System.out.println("utfFile = " + utfFile);

Path iso88591File = Files.createTempFile("some", ".txt");
Files.writeString(iso88591File, "this is my string ää öö üü", StandardCharsets.ISO_8859_1); // otherwise == utf8
System.out.println("iso88591File = " + iso88591File);
```

Starting with Java 11 (more specifically 11.0.2/12.0, as there was [a bug](https://bugs.openjdk.java.net/browse/JDK-8209576) in previous versions), you should be using the `*Files.writeString*` method to write string content to a file. By default, it will write a UTF-8 file, which you can, however, override by specifying a different encoding.

### How to write bytes to files

```
Path anotherIso88591File = Files.createTempFile("some", ".txt");
Files.write(anotherIso88591File, "this is my string ää öö üü".getBytes(StandardCharsets.ISO_8859_1));
System.out.println("anotherIso88591File = " + anotherIso88591File);
```

If you want to write bytes to a file (and in older Java versions < 11 you’d have to use the same API for writing strings), you need to call `*Files.write*`.

### Options when writing files

```
Path anotherUtf8File = Files.createTempFile("some", ".txt");
Files.writeString(anotherUtf8File, "this is my string ää öö üü", StandardCharsets.UTF_8,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
System.out.println("anotherUtf8File = " + anotherUtf8File);

Path oneMoreUtf8File = Files.createTempFile("some", ".txt");
Files.write(oneMoreUtf8File, "this is my string ää öö üü".getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
System.out.println("oneMoreUtf8File = " + oneMoreUtf8File);
```

When calling either of the `*write*` methods, the file will *automatically* be created (and truncated if it already exists). Which means, we wouldn’t have had to create explicit temporary files, like we did above.

If you don’t want that behavior (i.e. fail if the file already exists) and get a corresponding exception, you’ll need to pass in [another OpenOption](https://docs.oracle.com/javase/7/docs/api/java/nio/file/StandardOpenOption.html).

### Using Writers and OutputStreams

```
try (BufferedWriter bufferedWriter = Files.newBufferedWriter(utfFile)) {
    // handle reader
}

try (OutputStream os = Files.newOutputStream(utfFile)) {
    // handle outputstream
}
```

Last but not least, if you want to work directly with writers or output streams, make sure to call the corresponding `*Files*` methods and not construct the writers or streams by hand.

### How to read strings from files

Reading files is very similar to writing:

```
String s = Files.readString(utfFile);// UTF 8
System.out.println("s = " + s);

s = Files.readString(utfFile, StandardCharsets.ISO_8859_1); // otherwise == utf8
System.out.println("s = " + s);
```

On Java11+, you should be using the `*Files.readString*` method to read a string from a file. Make sure to pass in the appropriate file encoding; by default, Java will use the UTF-8 encoding to read in files.

### How to read bytes from files

```
s = new String(Files.readAllBytes(utfFile), StandardCharsets.UTF_8);
System.out.println("s = " + s);
```

If you want to read bytes from a file (and in older Java versions < 11 you’d have to use the same API for reading strings), you need to call `*Files.readAllBytes*`.

In case the final result should be a string, you’d then have to construct it yourself, with the appropriate encoding.

### Using Readers and InputStreams

```
try (BufferedReader bufferedReader = Files.newBufferedReader(utfFile)) {
    // handle reader
}

try (InputStream is = Files.newInputStream(utfFile)) {
    // handle inputstream
}
```

As always, you can fall back to using readers or inputstreams directly. For that, use the corresponding `*Files*` methods.

### A friendly reminder: File Encodings

I’ve mentioned it a couple of times over the previous sections:

You *absolutely should use an explicit encoding*, whenever creating, writing to or reading from files, though it’s of big help that the new Java 11 methods default to UTF-8, and not the platform-specific encoding.

## Moving, Deleting & Listing Files

There are a couple of things you need to watch out for, when moving or deleting files. Let’s see some code:

### How to move files

```
Path utfFile = Files.createTempFile("some", ".txt");

try {
    Files.move(utfFile, Path.of("c:\\dev"));  // this is wrong!
} catch (FileAlreadyExistsException e) {
    // welp, that din't work!
}
```

There is a `*Files.move*` method, but it *does not* move a file to a designated directory (which you might expect).

- test.jpg → c:\temp does not work.
- test.jpg → c:\temp\test.jpg works.

```
Files.move(utfFile, Path.of("c:\\dev").resolve(utfFile.getFileName().toString()));
```

So, you don’t move files to folders, but you "move" them to their full new path, including the filename and extension.

### File Move Options

```
Path utfFile2 = Files.createTempFile("some", ".txt");
Files.move(utfFile2, Path.of("c:\\dev").resolve(utfFile.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);

Path utfFile3 = Files.createTempFile("some", ".txt");
Files.move(utfFile3, Path.of("c:\\dev").resolve(utfFile.getFileName().toString()), StandardCopyOption.ATOMIC_MOVE);
```

When moving files, you can also specify how you want to move to happen, depending on the capabilities of the underlying file system.

- By default, if the target file already exists, a `*FileAlreadyExistsException*` will be thrown.
- If you specify the `*StandardCopyOption.REPLACE_EXISTING*` option, the target file will be overwritten.
- If you specify the `*StandardCopyOption.ATOMIC_MOVE*` option, you can move a file into a directory and be guaranteed that any process watching the directory accesses a complete file and not just a partial file.

### How to delete files

Deleting files and folders is an area, where the Java Path API falls short a tiny bit. Let’s see why:

```
try {
    Files.delete(tmpDir);
} catch (DirectoryNotEmptyException e) {
    e.printStackTrace();
}
```

There is the `*Files.delete*` method, which allows you to delete files and directories, but directories only if they are empty.

There is unfortunately no flag to purge a non-empty directory, and you’ll simply get a `*DirectoryNotEmptyException*`.

### How to delete non-empty directories

There are some 3rd-party helper libraries to work around this, but if you want to use a plain Java version to delete a non-empty directory tree, this is what you’ll want to do:

```
try (Stream<Path> walk = Files.walk(tmpDir)) {
    walk.sorted(Comparator.reverseOrder()).forEach(path -> {
        try {
            Files.delete(path);
        } catch (IOException e) {
            // something could not be deleted..
            e.printStackTrace();
        }
    });
}
```

`*Files.walk*` will walk a file tree depth-first, starting with the directory you specify. The `*reverseOrder*` comparator will make sure that you delete all children, before deleting the actual directory.

Unfortunately, you’ll also need to catch the IOException, when using `*Files.delete*` inside the `*forEach*` consumer. A whole lot of code for deleting a non-empty directory, isn’t it?

Which brings us to the topic of listing files:

### How to list files in the same directory

There are various ways how you can list all files in a given directory. If you only want to list files on the same levels as the directory (not recursively deeper), you can use these two methods:

```
try (var files = Files.list(tmpDirectory)) {
    files.forEach(System.out::println);
}

try (var files = Files.newDirectoryStream(tmpDirectory, "*.txt")) {
    files.forEach(System.out::println);
}
```

Note, that `*newDirectoryStream*` (as opposed to `*Files.list*`) does not return a `*java.util.stream.Stream*`. Instead, it returns a `*DirectoryStream*`, which is a class that got introduced in Java 1.7, before the release of the Streams API in Java 8.

It does, however, allow you to specify a `*glob*` pattern (like *.txt), which does the job for simple listings, and is maybe a bit easier to read than fumbling with real Streams and the corresponding *filter* methods.

Also note, that the streams returned by both methods must also be closed (e.g. with a try-with-resources statement), otherwise the JVM will keep the file handle on the directory open, which (on Windows) effectively locks it.

### How to list files recursively

If you want to recursively list all files of a file tree, you’ll need to employ the method we used for deleting directories: `*Files.walk*`.

```
try (var files = Files.walk(tmpDirectory)) {
    files.forEach(System.out::println);
}
```

Note, that the stream returned by `*Files.walk*` must also be closed (e.g. with a try-with-resources statement), otherwise the JVM will keep the file handle on the directory open, which (on Windows) effectively locks it.

## Absolute, Relative & Canonical Files

Let’s quickly talk about the concepts of absolute, relative & canonical paths. It’s best demonstrated with some code examples:

### Relative Paths

```
Path p = Paths.get("./src/main/java/../resources/some.properties");
System.out.println("p.isAbsolute() = " + p.isAbsolute());
```

Here, you’re constructing a new path, based on the current directory (.), even including a (..) at some point. Hence, the path is `*relative*` to your current directory, and `*path.isAbsolute*` will return false.

```
p.isAbsolute() = false
```

### Absolute Paths

```
Path p2 = p.toAbsolutePath();
System.out.println("p2 = " + p2);
System.out.println("p2.isAbsolute() = " + p2.isAbsolute());
```

When you call `*toAbsolutePath*` on the path, it will get converted to an..well…absolute path, in my case containing `*C:\dev\java-files*`. Note, the absolute path *still* contains the dots, for current directory and upper-directory!

```
p2 = C:\dev\java-file-article\.\src\main\java\..\resources\some.properties
p2.isAbsolute() = true
```

### Normalized Paths

How to get rid of the dots? You’ll need to call `*normalize*`.

```
Path p3 = p.normalize().toAbsolutePath();
System.out.println("p3 = " + p3);
System.out.println("p3.isAbsolute() = " + p3.isAbsolute());
```

This normalized, absolute path, is also what you could have called the *canonical path*.

### Relativizing Paths

```
p3 = C:\dev\java-file-article\src\main\resources\some.properties
p3.isAbsolute() = true
```

Last but not least, you can also go the other way. Instead of making relative paths absolute, you can make absolute paths relative.

```
Path relativizedPath = Paths.get("C:\\dev\\java-file-article\\").relativize(p3);
System.out.println("relativizedPath = " + relativizedPath);
```

You’re essentially saying, given a certain base path, what is the relative path of my current (absolute) path. You’ll get the following output:

```
relativizedPath = src\main\resources\some.properties
```

## Watching Files & Directories

Some projects need to watch directories for newly created (think: uploaded) files and do something with them. You have two popular choices, when it comes to *watching* for changes in a directory, in Java.

### Java’s WatchService

With Java 7, Java its [WatchService](https://docs.oracle.com/javase/7/docs/api/java/nio/file/WatchService.html). It is a somewhat low-level way of watching for changes in a specified directory.

The WatchService will get notified of *native file events* (Windows, Linux), with the notable exception being MacOS, where it falls back to polling directories for changes - which is pretty much what all other watch-libraries do by default (see next section).

Here’s some code, which you **should not blindly copy & paste**, but which will give you an idea of what a WatchService looks like.

```
public static void main(String[] args) throws IOException {

    WatchService watcher = FileSystems.getDefault().newWatchService();

    Path dir = Path.of("c:\\someDir\\");

    dir.register(watcher,
            ENTRY_CREATE,
            ENTRY_DELETE,
            ENTRY_MODIFY);

    for (;;) {

        WatchKey key;
        try {
            key = watcher.take();
        } catch (InterruptedException x) {
            return;
        }

        for (WatchEvent<?> event: key.pollEvents()) {
            WatchEvent.Kind<?> kind = event.kind();

            if (kind == OVERFLOW) {
                continue;
            }

            WatchEvent<Path> ev = (WatchEvent<Path>)event;
            Path filename = ev.context();

            Path changedFile = dir.resolve(filename);

            // do something with the file
        }

        boolean valid = key.reset();
        if (!valid) {
            break;
        }
    }
}
```

Discussing a [full WatchService implementation](https://docs.oracle.com/javase/tutorial/essential/io/notification.html) here does not really fit into the scope of this article, but note:

There’s a couple of things to watch out for (no pun intended) when using WatchService:

- You might assume that you get *one* event, whenever e.g. a file is updated, but this can easily result in two events: One for the updated content and one for updating the last-modified timestamp, happening within a short period of time.
- Complex IDEs like IntelliJ or even smaller text editors like Notepad++ don’t just save a file and its contents in one go. They copy contents to tmp files, delete them, then save the content to your actual file, etc. Again, there can be multiple updates happening to the same or even multiple files, whereas you, as the end-user, ideally would like to have just *one* updated event.
- Hence, you’ll need to apply [some workarounds](https://stackoverflow.com/questions/16777869/java-7-watchservice-ignoring-multiple-occurrences-of-the-same-event). The unaccepted answer with 40+ upvotes (`*Thread.sleep*`) has worked somewhat reliably for me, in the past).

Last, but not least, you might want to have a look at this [superb article](https://blog.arkey.fr/2019/09/13/watchservice-and-bind-mount/), which talks about Java’s WatchService, Containers and issues with bind mounts.

### Apache Commons-IO

There’s another library that lets you watch directories for incoming changes: [Commons IO](https://commons.apache.org/proper/commons-io/). It has the easier API from a usage perspective, but differs in two aspects from WatchService:

1. It only works with `*java.io.Files*`, not `*java.nio.file.Paths*`.
2. It uses polling, i.e. it calls the listFiles() method of the File class and compares the output with the listFiles() output of the previous iteration to see what changed.

Again, a full implementation is outside the scope of this article, but you might want to have a look at this [Gist](https://gist.github.com/marcomachado/6581811) for a working code example or use the [JavaDoc](https://commons.apache.org/proper/commons-io/javadocs/api-release/index.html?org/apache/commons/io/monitor/package-summary.html) on `*FileAlterationMonitor*` or `*FileAlterationObserver*` as a starting point.

Here’s what the code roughly looks like, which **you should not blindly copy & paste**:

```
public static void main(String[] args) throws IOException {
    FileAlterationObserver observer = new FileAlterationObserver(folder);
    FileAlterationMonitor monitor =
            new FileAlterationMonitor(pollingInterval);
    FileAlterationListener listener = new FileAlterationListenerAdaptor() {
        // Is triggered when a file is created in the monitored folder
        @Override
        public void onFileCreate(File file) {
            // do something
        }

        // Is triggered when a file is deleted from the monitored folder
        @Override
        public void onFileDelete(File file) {
           // do something
        }
    };
}
```

## In-Memory File Systems

Some developers assume that working with files always means you’ll actually have to write them to your disk.

During testing, this leads to creating a lot of temp files and directories and then having to make sure to delete them again.

But, with Java’s `*Path*`-API, there’s a much better way: In-Memory File Systems.

They let you write and read files, completely in-memory, without ever hitting your disk. Super-fast and a great fit for testing (as long as you don’t run out of memory, erm… ).

There are two Java in-memory file systems that are worth looking at.

### Memory File System

One choice is [Memory File System](https://github.com/marschall/memoryfilesystem). Let’s see how you would create an in-memory filesystem with it.

```
package com.marcobehler.files;

import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class MemoryFileSystem {

    public static void main(String[] args) throws IOException {

        try (FileSystem fileSystem = MemoryFileSystemBuilder.newMacOs().build()) {

            Path inMemoryFile = fileSystem.getPath("/somefile.txt");
            Files.writeString(inMemoryFile, "Hello World");

            System.out.println(Files.readString(inMemoryFile));
        }
    }
}
```

Let’s break it down.

```
try (FileSystem fileSystem = MemoryFileSystemBuilder.newMacOs().build()) {
```

The only Memory File System-specific line is this one. You need to create a `*FileSystem*` that you will use later on to create and read/write your `*Paths*`.

By calling `*newLinux()*` or `*newWindows()*` or `*newMacOs()*` you can control the semantics of the created file system.

```
Path inMemoryFile = fileSystem.getPath("/somefile.txt");
Files.writeString(inMemoryFile, "Hello World");

System.out.println(Files.readString(inMemoryFile));
```

You are writing to a file called `*somefile.txt*` and reading in the file contents a couple of lines later.

This is the plain `*java.nio.file.Path-API*`, with one huge difference. You need to get your Path from the `*fileSystem*`, *not* via `*Path.of*` or `*Paths.get*`.

You’ll see why that is the case, after looking at JimFS.

### JimFS

Another choice is [JimFS](https://github.com/google/jimfs). Let’s see how you would create an in-memory filesystem with it.

```
package com.marcobehler.files;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

public class JimFSSystem {

    public static void main(String[] args) throws IOException {

        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());) {

            Path inMemoryFile = fileSystem.getPath("/tmp/somefile.txt");
            Files.writeString(inMemoryFile, "Hello World");

            System.out.println(Files.readString(inMemoryFile));
        }
    }
}
```

Let’s break it down.

```
try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix());) {
```

The only Memory File System-specific line is this one. You need to create a `*FileSystem*` that you will use later on to create and read/write your `*Paths*`.

With the `*Configuration.unix/windows/macOs*` parameter, you can control the semantics of the created file system.

```
Path inMemoryFile = fileSystem.getPath("/tmp/somefile.txt");
Files.writeString(inMemoryFile, "Hello World");

System.out.println(Files.readString(inMemoryFile));
```

You are writing to a file called `*somefile.txt*` and reading in the file contents a couple lines later.

This is the plain `*java.nio.file.Path-API*`, with one huge difference. You need to get your Path from the `*fileSystem*`, *not* via `*Path.of*` or `*Paths.get*`.

Let’s see why that is, now.

### How to make your application work with in-memory filesystems: anchors

When you look at the implementation of `*Path.of*` or `*Paths.get*`, you will see this:

```
public static Path of(String first, String... more) {
    return FileSystems.getDefault().getPath(first, more);
}
```

So, while this method (and others) are very convenient, using them will imply you want to access your `*default*` FileSystem, the one your JVM is running on (WindowsFileSystem, UnixFileSystem etc.), *not* your `*in-memory*` FileSystem.

Hence, when wanting to make sure your code works against in-memory file systems, you must make sure to *never* call these helpers methods. Instead, you should always use the `*FileSystem*` or a `*Path*` as an anchor, like you are doing in the examples above.

Depending on your project (think: legacy), this is quite a challenge to pull off.

## Fin

By now you should have a pretty good overview of how to work with files in Java.

- How to do all basic file operations, from reading, writing, listing, moving & deleting.
- How relative, absolute & canonical paths work.
- How to watch directories and files.
- How you can use in-memory file systems for testing.

Feedback, corrections and random input are always welcome! Simply leave a comment down below.

Thanks for reading.

## Acknowledgements

Many thanks to [konrad](https://www.reddit.com/user/__konrad/), [jonhanson](https://www.reddit.com/user/jonhanson/) and [DasBrain](https://www.reddit.com/user/DasBrain/) on Reddit for pointing out various small issues (needed auto-closing of file listing streams, bugs with Files.writeString, usage of the var-keyword for better readability).