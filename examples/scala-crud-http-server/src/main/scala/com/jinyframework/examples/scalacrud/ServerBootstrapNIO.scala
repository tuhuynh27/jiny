package com.jinyframework.examples.scalacrud

import com.jinyframework.NIOHttpServer
import com.jinyframework.core.RequestBinderBase.HttpResponse

object ServerBootstrapNIO extends Runnable {
  private val server: NIOHttpServer = NIOHttpServer.port(1235)

  server.get("/", _ => HttpResponse.ofAsync("Hello"))

  override def run(): Unit = server.start()
}
