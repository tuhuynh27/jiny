package com.tuhuynh.scala.crud.routers

import com.tuhuynh.jerrymouse.core.bio.HttpRouter
import com.tuhuynh.scala.crud.handlers.CatHandler

object CatRouter {
  def getRouter: HttpRouter = {
    val router = new HttpRouter()
    router.get("/", CatHandler.getCats)
    router
  }
}
