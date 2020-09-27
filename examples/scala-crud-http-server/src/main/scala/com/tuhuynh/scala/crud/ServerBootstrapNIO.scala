package com.tuhuynh.scala.crud

import com.tuhuynh.jerrymouse.NIOHttpServer
import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse

object ServerBootstrapNIO extends Runnable {
  private val server: NIOHttpServer = NIOHttpServer.port(1235)

  server.get("/", _ => HttpResponse.ofAsync("Hello"))

  override def run(): Unit = server.start()
}
