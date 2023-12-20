## 单独使用 OpenFeign



# Form Encoder

[![build_status](https://camo.githubusercontent.com/e02c64dab1a2889b96dc8c9c05f99e141955f11c10d49590a1c30b60203ffad2/68747470733a2f2f7472617669732d63692e6f72672f4f70656e466569676e2f666569676e2d666f726d2e7376673f6272616e63683d6d6173746572)](https://travis-ci.org/OpenFeign/feign-form) [![maven_central](https://camo.githubusercontent.com/b5ce1256d4f9d9c806d60584682c9996f2ef39c778740b11e1b8c1d6c66fbf53/68747470733a2f2f6d6176656e2d6261646765732e6865726f6b756170702e636f6d2f6d6176656e2d63656e7472616c2f696f2e6769746875622e6f70656e666569676e2e666f726d2f666569676e2d666f726d2f62616467652e737667)](https://maven-badges.herokuapp.com/maven-central/io.github.openfeign.form/feign-form) [![License](https://camo.githubusercontent.com/3dd7ad58e2131abc7d81a11d7ec34406fe9fb0853c90ec16a35bb900f1fcf3cd/687474703a2f2f696d672e736869656c64732e696f2f3a6c6963656e73652d6170616368652d627269676874677265656e2e737667)](http://www.apache.org/licenses/LICENSE-2.0.html)

This module adds support for encoding **application/x-www-form-urlencoded** and **multipart/form-data** forms.

## Add dependency

Include the dependency to your app:

**Maven**:

```
<dependencies>
  ...
  <dependency>
    <groupId>io.github.openfeign.form</groupId>
    <artifactId>feign-form</artifactId>
    <version>3.8.0</version>
  </dependency>
  ...
</dependencies>
```

**Gradle**:

```
compile 'io.github.openfeign.form:feign-form:3.8.0'
```

## Requirements

The `feign-form` extension depend on `OpenFeign` and its *concrete* versions:

- all `feign-form` releases before **3.5.0** works with `OpenFeign` **9.\*** versions;
- starting from `feign-form`'s version **3.5.0**, the module works with `OpenFeign` **10.1.0** versions and greater.

> **IMPORTANT:** there is no backward compatibility and no any gurantee that the `feign-form`'s versions after **3.5.0** work with `OpenFeign` before **10.\***. `OpenFeign` was refactored in 10th release, so the best approach - use the freshest `OpenFeign` and `feign-form` versions.

Notes:

- [spring-cloud-openfeign](https://github.com/spring-cloud/spring-cloud-openfeign) uses `OpenFeign` **9.\*** till **v2.0.3.RELEASE** and uses **10.\*** after. Anyway, the dependency already has suitable `feign-form` version, see [dependency pom](https://github.com/spring-cloud/spring-cloud-openfeign/blob/master/spring-cloud-openfeign-dependencies/pom.xml#L19), so you don't need to specify it separately;
- `spring-cloud-starter-feign` is a **deprecated** dependency and it always uses the `OpenFeign`'s **9.\*** versions.

## Usage

Add `FormEncoder` to your `Feign.Builder` like so:

```java
SomeApi github = Feign.builder()
                      .encoder(new FormEncoder())
                      .target(SomeApi.class, "http://api.some.org");
```

Moreover, you can decorate the existing encoder, for example JsonEncoder like this:

```java
SomeApi github = Feign.builder()
                      .encoder(new FormEncoder(new JacksonEncoder()))
                      .target(SomeApi.class, "http://api.some.org");
```

And use them together:

```java
interface SomeApi {

  @RequestLine("POST /json")
  @Headers("Content-Type: application/json")
  void json (Dto dto);

  @RequestLine("POST /form")
  @Headers("Content-Type: application/x-www-form-urlencoded")
  void from (@Param("field1") String field1, @Param("field2") String[] values);
}
```

You can specify two types of encoding forms by `Content-Type` header.

### application/x-www-form-urlencoded

```java
interface SomeApi {

  @RequestLine("POST /authorization")
  @Headers("Content-Type: application/x-www-form-urlencoded")
  void authorization (@Param("email") String email, @Param("password") String password);

  // Group all parameters within a POJO
  @RequestLine("POST /user")
  @Headers("Content-Type: application/x-www-form-urlencoded")
  void addUser (User user);

  class User {

    Integer id;

    String name;
  }
}
```

### multipart/form-data

```java
interface SomeApi {

  // File parameter
  @RequestLine("POST /send_photo")
  @Headers("Content-Type: multipart/form-data")
  void sendPhoto (@Param("is_public") Boolean isPublic, @Param("photo") File photo);

  // byte[] parameter
  @RequestLine("POST /send_photo")
  @Headers("Content-Type: multipart/form-data")
  void sendPhoto (@Param("is_public") Boolean isPublic, @Param("photo") byte[] photo);

  // FormData parameter
  @RequestLine("POST /send_photo")
  @Headers("Content-Type: multipart/form-data")
  void sendPhoto (@Param("is_public") Boolean isPublic, @Param("photo") FormData photo);

  // Group all parameters within a POJO
  @RequestLine("POST /send_photo")
  @Headers("Content-Type: multipart/form-data")
  void sendPhoto (MyPojo pojo);

  class MyPojo {

    @FormProperty("is_public")
    Boolean isPublic;

    File photo;
  }
}
```

In the example above, the `sendPhoto` method uses the `photo` parameter using three different supported types.

- `File` will use the File's extension to detect the `Content-Type`;
- `byte[]` will use `application/octet-stream` as `Content-Type`;
- `FormData` will use the `FormData`'s `Content-Type` and `fileName`;
- Client's custom POJO for grouping parameters (including types above).

`FormData` is custom object that wraps a `byte[]` and defines a `Content-Type` and `fileName` like this:

```java
  FormData formData = new FormData("image/png", "filename.png", myDataAsByteArray);
  someApi.sendPhoto(true, formData);
```

### Spring MultipartFile and Spring Cloud Netflix @FeignClient support

You can also use Form Encoder with Spring `MultipartFile` and `@FeignClient`.

Include the dependencies to your project's pom.xml file:

```java
<dependencies>
  <dependency>
    <groupId>io.github.openfeign.form</groupId>
    <artifactId>feign-form</artifactId>
    <version>3.8.0</version>
  </dependency>
  <dependency>
    <groupId>io.github.openfeign.form</groupId>
    <artifactId>feign-form-spring</artifactId>
    <version>3.8.0</version>
  </dependency>
</dependencies>
@FeignClient(
    name = "file-upload-service",
    configuration = FileUploadServiceClient.MultipartSupportConfig.class
)
public interface FileUploadServiceClient extends IFileUploadServiceClient {

  public class MultipartSupportConfig {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Encoder feignFormEncoder () {
      return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }
  }
}
```

Or, if you don't need Spring's standard encoder:

```java
@FeignClient(
    name = "file-upload-service",
    configuration = FileUploadServiceClient.MultipartSupportConfig.class
)
public interface FileUploadServiceClient extends IFileUploadServiceClient {

  public class MultipartSupportConfig {

    @Bean
    public Encoder feignFormEncoder () {
      return new SpringFormEncoder();
    }
  }
}
```

Thanks to [tf-haotri-pham](https://github.com/tf-haotri-pham) for his feature, which makes use of Apache commons-fileupload library, which handles the parsing of the multipart response. The body data parts are held as byte arrays in memory.

To use this feature, include SpringManyMultipartFilesReader in the list of message converters for the Decoder and have the Feign client return an array of MultipartFile:

```java
@FeignClient(
    name = "${feign.name}",
    url = "${feign.url}"
    configuration = DownloadClient.ClientConfiguration.class
)
public interface DownloadClient {

  @RequestMapping("/multipart/download/{fileId}")
  MultipartFile[] download(@PathVariable("fileId") String fileId);

  class ClientConfiguration {

    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Decoder feignDecoder () {
      List<HttpMessageConverter<?>> springConverters =
            messageConverters.getObject().getConverters();

      List<HttpMessageConverter<?>> decoderConverters =
            new ArrayList<HttpMessageConverter<?>>(springConverters.size() + 1);

      decoderConverters.addAll(springConverters);
      decoderConverters.add(new SpringManyMultipartFilesReader(4096));

      HttpMessageConverters httpMessageConverters = new HttpMessageConverters(decoderConverters);

      return new SpringDecoder(new ObjectFactory<HttpMessageConverters>() {

        @Override
        public HttpMessageConverters getObject() {
          return httpMessageConverters;
        }
      });
    }
  }
}
```