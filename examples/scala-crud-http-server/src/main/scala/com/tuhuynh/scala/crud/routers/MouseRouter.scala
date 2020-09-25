package com.tuhuynh.scala.crud.routers

import com.tuhuynh.jerrymouse.core.bio.HttpRouter
import com.tuhuynh.scala.crud.handlers.MouseHandler

object MouseRouter {
  def getRouter: HttpRouter = {
    val router = new HttpRouter()
    router.get("/", MouseHandler.getMouses)
    router.post("/", MouseHandler.addMouse)
    router
  }
}
