# JWT Middleware

JSON Web Token (JWT) is an open standard (RFC 7519) that defines a compact and self-contained way for securely transmitting information between parties as a JSON object. This information can be verified and trusted because it is digitally signed. JWTs can be signed using a secret (with the HMAC algorithm) or a public/private key pair using RSA or ECDSA.

## Install

Latest version: ![Maven Central](https://img.shields.io/maven-central/v/com.jinyframework/cors-middleware?style=flat-square)

`build.gradle`

```groovy
dependencies {
    compile group: 'com.jinyframework', name: 'cors-middleware', version: '{latest_version}'
}
```

## Configuration

Example usage:

```java
package com.jinyframework.middlewares;

import com.google.gson.Gson;
import com.jinyframework.HttpServer;
import com.jinyframework.middlewares.jwt.Jwt;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.HashMap;

import static com.jinyframework.core.AbstractRequestBinder.HttpResponse.of;

public final class JwtExample {
    public static void main(String[] args) {
        val server = HttpServer.port(1234);
        val secretKey = Jwt.genKey("HS256");
        // val secretKey = "a very long key a very long key a very long key a very long key";
        val authComponent = Jwt.newAuthComponent(Jwt.Config.builder()
                .secretKey(secretKey)
                .authenticator(ctx -> {
                    val loginReq = new Gson().fromJson(ctx.getBody(), LoginReq.class);
                    if ("admin".equals(loginReq.getUsername())
                            && "admin".equals(loginReq.getPassword())) {
                        val claims = new HashMap<String, Object>();
                        claims.put("aud", "client");
                        claims.put("sub", "userName");
                        claims.put("iss", "host");
                        return claims;
                    }
                    return null;
                })
                .userRetriever((ctx, claims) -> claims)
                .build());
        server.use("/login", authComponent.handleLogin());
        server.use("/path", authComponent.handleVerify(),
                ctx -> of(ctx.dataParam(Jwt.Config.USER_KEY_DEFAULT)));
        server.start();
    }

    @Setter
    @Getter
    public static class LoginReq {
        String username;
        String password;
    }
}

```