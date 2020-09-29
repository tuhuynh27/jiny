# Non-Blocking Mode

"Asynchronous" Non-blocking I/O version of Jiny framework is using Java SE's `java.net` & `java.nio` packages.

This is kinda "naive Netty clone with Go-Gin interface" also with an easy-to-use API covering HTTP protocol support (Netty is just a TCP Framework).

:::tip Why NIO?
NIO Mode solve the [C10K Problem](http://www.kegel.com/c10k.html), by using least OS thread than the normal BIO mode
:::

## NIO Insight

This server uses the latest AsynchronousServerSocketChannel (aka NIO.2 AIO API) API of Java SE 7, which use the Proactor Pattern and it is fully Async I/O (with underlying OS support for epoll/kqueue edge-triggered syscalls).

The old versions (< 0.1.6) use SocketServerChannel and a Selector (aka NIO API) which is just an I/O Multiplexer aka "polling mechanism" (with epoll / kqueue level-triggered syscalls).

## Quick Start

This NIO HTTP Server API is very similar with standard BIO API

```java
import com.jinyframework.NIOHttpServer;
import com.jinyframework.core.RequestBinder.HttpResponse;

class Main {
    static void main(String[] args) throws IOException {
        val server = NIOHTTPServer.port(1234);
        server.use("/", ctx -> HttpResponse.ofAsync("Hello World"));
        server.start(); // Listen and serve on localhost:1234
    }
}
```

:::warning WIP
This feature is in experimental
:::
