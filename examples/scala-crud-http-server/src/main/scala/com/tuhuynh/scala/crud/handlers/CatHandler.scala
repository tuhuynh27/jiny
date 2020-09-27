package com.tuhuynh.scala.crud.handlers

import com.tuhuynh.jerrymouse.core.RequestBinderBase.{HttpResponse, Handler}

object CatHandler {
  val getCats: Handler = _ =>
    HttpResponse.of("Cats")
}
