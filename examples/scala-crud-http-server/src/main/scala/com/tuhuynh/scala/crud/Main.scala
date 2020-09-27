package com.tuhuynh.scala.crud

import com.tuhuynh.jerrymouse.HttpServer
import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse
import com.tuhuynh.scala.crud.factories.app.AppFactory
import com.tuhuynh.scala.crud.routers.{CatRouter, MouseRouter}

object Main extends App {
  val server = HttpServer.port(1234)
  server.setupResponseTransformer(s => AppFactory.getGson.toJson(s))

  server.get("/", _ => HttpResponse.of("Hello Scala"))
  server.use("/cat", CatRouter.getRouter)
  server.use("/mouse", MouseRouter.getRouter)

  server.start()
}
