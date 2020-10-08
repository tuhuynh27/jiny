# Proxy Mode

Use Jiny as an HTTP Reverse Proxy

With reverse proxy, you can run several servers in one JVM process which expose in the one port (Proxy's port). That means you can embed a Jiny Server/NIO Server in an existed Spring Boot Application easily.

:::tip WIP
This reverse proxy under-the-hood run in asynchronous non-blocking mode, [similar to Nginx](https://www.nginx.com/blog/inside-nginx-how-we-designed-for-performance-scale/), with default `total_cpu_core` * 2 event-loop threads.
:::

```scala
import com.jinyframework.HttpProxy

object Main extends App {
  new Thread(ServerBootstrap).start() // Port 1234
  new Thread(ServerBootstrapNIO).start() // Port 1235

  val proxy = HttpProxy.port(1111)
  proxy.use("/server1", "localhost:1234")
  proxy.use("/server2", "localhost:1235")
  proxy.start()
}
```

:::warning WIP
This feature is in experimental
:::
