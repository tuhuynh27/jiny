package com.jinyframework.examples.scalacrud

import com.jinyframework.HttpServer
import com.jinyframework.core.AbstractRequestBinder.HttpResponse
import com.jinyframework.examples.scalacrud.factories.app.AppFactory
import com.jinyframework.examples.scalacrud.routers.{CatRouter, MouseRouter}

object ServerBootstrap extends Runnable {
  private val server: HttpServer = HttpServer.port(1234)
  server.useTransformer(s => AppFactory.getGson.toJson(s))

  server.get("/", _ => HttpResponse.of("Hello Scala"))
  server.use("/cat", CatRouter.getRouter)
  server.use("/mouse", MouseRouter.getRouter)

  override def run(): Unit = server.start()
}
