package com.jinyframework.examples.scalacrud.routers

import com.jinyframework.core.bio.HttpRouter
import com.jinyframework.examples.scalacrud.handlers.MouseHandler

object MouseRouter {
  def getRouter: HttpRouter = {
    val router = new HttpRouter()
    router.get("/", MouseHandler.getMouses)
    router.post("/", MouseHandler.addMouse)
    router
  }
}
