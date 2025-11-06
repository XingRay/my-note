JJWT 实现JWT


1什么是JJWT
JJWT 是一个提供端到端的 JWT 创建和验证的 Java 库。永远免费和开源 (Apache License，版本2.0)，JJWT 很容易使用和理解。它被设计成一个以建筑为中心的流畅界面，隐藏了它的大部分复杂性。

2JJWT快速入门
2.1token的创建
2.1.1maven引入依赖
<dependency>
	<groupId>io.jsonwebtoken</groupId>
	<artifactId>jjwt</artifactId>
	<version>0.9.1</version>
</dependency>
一键获取完整项目代码
2.1.2 创建 CreateJWT 类，用于生成 token
public class CreateJWT {
    public static void main(String[] args) {
        JwtBuilder jwtBuilder = Jwts.builder().setId("666777")
                .setSubject("脑浆消融")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "HelloWorld");
        System.out.println(jwtBuilder.compact());
    }
}
复制代码
一键获取完整项目代码

setIssuedAt：用于设置签发时间。
signWith：用于设置签名秘钥。
2.1.3 测试
第一次：

eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2NjY3NzciLCJzdWIiOiLohJHmtYbmtojono0iLCJpYXQiOjE2MDg4MDY3MDd9.v1SRR_xChK-K_T5GuHObQy5BnCOyZgGxBX-vrqBWwZg
一键获取完整项目代码
再次运行，会发现每次运行的结果是不一样的，因为我们的载荷中包含了时间：

eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2NjY3NzciLCJzdWIiOiLohJHmtYbmtojono0iLCJpYXQiOjE2MDg4MDY3ODV9.Da6HfKuSowFkWKmazLzFQSvkWzMPYNCEuNu12Q7e8mM
一键获取完整项目代码
2.2 token 的解析
我们刚才已经创建了 token ，在 web 应用中这个操作是由服务端进行然后发给客户端，客户端在下次向服务端发送请求时需要携带这个 token（这就好像是拿着一张门票一样），那服务端接到这个 token 应该解析出 token 中的信息（例如用户 id），根据这些信息查询数据库返回相应的结果。

public class ParserJwtTest {

    public static void main(String[] args) {
        Claims claims = Jwts.parser().setSigningKey("HelloWorld")
                .parseClaimsJws("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2NjY3NzciLCJzdWIiOiLohJHmtYbmtojono0iLCJpYXQiOjE2MDg4MDY3ODV9.Da6HfKuSowFkWKmazLzFQSvkWzMPYNCEuNu12Q7e8mM")
                .getBody();
        System.out.println("用户ID：" + claims.getId());
        System.out.println("用户名：" + claims.getSubject());
        System.out.println("登陆时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(claims.getIssuedAt()));
    }
}
一键获取完整项目代码



 

试着将 token 或签名秘钥篡改一下，会发现运行时就会报错，所以解析 token 也就是验证 token。

例如，把 token 末尾的 mM 删除掉，就会报错：



 

2.3 token 过期校验
有很多时候，我们并不希望签发的 token 是永久生效的，所以我们可以为 token 添加一个过期时间。

public class CreateJWT {
    public static void main(String[] args) {
        JwtBuilder jwtBuilder = Jwts.builder().setId("666777")
                .setSubject("脑浆消融")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "HelloWorld")
                .setExpiration(new Date(new Date().getTime() + 60000));
        System.out.println(jwtBuilder.compact());
    }
}
复制代码
一键获取完整项目代码

setExpiration 方法：用于设置过期时间。
我们修改 Jwt 解析类，多添加一个过期时间：

public class ParserJwtTest {
    public static void main(String[] args) {
        Claims claims = Jwts.parser().setSigningKey("HelloWorld")
                .parseClaimsJws("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2NjY3NzciLCJzdWIiOiLohJHmtYbmtojono0iLCJpYXQiOjE2MDg4MDk3NzksImV4cCI6MTYwODgwOTgzOX0.kAroSbh6Q5PzoA0iUxNtlpBVipvA6Zb2O3OcEFJkF88")
                .getBody();
        System.out.println("用户ID：" + claims.getId());
        System.out.println("用户名：" + claims.getSubject());
        System.out.println("登陆时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(claims.getIssuedAt()));
        System.out.println("过期时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(claims.getExpiration()));
    }
}
一键获取完整项目代码

1 分钟之内，token 可以使用:



 

1 分钟之后，过期时会引发 io.jsonwebtoken.ExpiredJwtException 异常：



2.4 自定义 claims
我们刚才的例子只是存储了 id 和 subject 两个信息，如果你想存储更多的信息（例如角色）可以自定义 claims。

多添加两个 claims：

public class CreateJWT {
    public static void main(String[] args) {
        JwtBuilder jwtBuilder = Jwts.builder().setId("666777")
                .setSubject("脑浆消融")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, "HelloWorld")
                .setExpiration(new Date(new Date().getTime() + 60000))
                .claim("sex", "man")
                .claim("age", "25");
        System.out.println(jwtBuilder.compact());
    }
}
一键获取完整项目代码

Jwt 解析类，打印出性别和年龄：

public class ParserJwtTest {
    public static void main(String[] args) {
        Claims claims = Jwts.parser().setSigningKey("HelloWorld")
                .parseClaimsJws("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2NjY3NzciLCJzdWIiOiLohJHmtYbmtojono0iLCJpYXQiOjE2MDg4MDk3NzksImV4cCI6MTYwODgwOTgzOX0.kAroSbh6Q5PzoA0iUxNtlpBVipvA6Zb2O3OcEFJkF88")
                .getBody();
        System.out.println("用户ID：" + claims.getId());
        System.out.println("用户名：" + claims.getSubject());
        System.out.println("登陆时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(claims.getIssuedAt()));
        System.out.println("过期时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(claims.getExpiration()));
        System.out.println("性别：" + claims.get("sex"));
        System.out.println("年龄：" + claims.get("age"));
    }
}
一键获取完整项目代码

结果：




Zohhh、
关注

————————————————
版权声明：本文为CSDN博主「Zohhh、」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/Ssolitude123/article/details/127875636