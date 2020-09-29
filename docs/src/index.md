---
home: true
heroImage: https://i.imgur.com/OpG00Ct.png
tagline: Lightweight, modern, simple Java HTTP Server/Client for rapid development in the API era
actionText: Quick Start →
actionLink: /guide/
features:
- title: Expressive
  details: Already know basic Java (or Kotlin/Scala)? Read the guide and start building things in no time (no implicit annotations)
- title: Lightweight
  details: Tiny-in-size with servlet-free (yeah, no servlet container) and no dependency, build and start are really fast, it also embeddable
- title: Performant
  details: Blazing fast routing performance with minimal optimization efforts and reactive support out-of-the-box
footer: Apache-2.0 Licensed | Copyright © 2020 Tu Huynh
---

You have your RESTful API ready to serve JSON in less than ten lines of code

 ```java
import com.jinyframework.*;
import com.google.gson.Gson;

class Main {
    static void main() {
        val server = HTTPServer.port(1234);
        server.setupResponseTransformer(gson::toJson);
        server.get("/ping", ctx -> HttpResponse.of("Pong"));
        server.start();
    }
} 
```
