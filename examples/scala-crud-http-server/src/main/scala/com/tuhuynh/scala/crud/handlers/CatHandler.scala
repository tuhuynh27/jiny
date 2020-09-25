package com.tuhuynh.scala.crud.handlers

import com.tuhuynh.jerrymouse.core.RequestBinderBase.{HttpResponse, RequestHandlerBIO}

object CatHandler {
  val getCats: RequestHandlerBIO = _ =>
    HttpResponse.of("Cats")
}
