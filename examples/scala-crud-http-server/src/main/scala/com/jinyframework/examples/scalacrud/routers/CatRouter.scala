package com.jinyframework.examples.scalacrud.routers

import com.jinyframework.core.bio.HttpRouter
import com.jinyframework.examples.scalacrud.handlers.CatHandler

object CatRouter {
  def getRouter: HttpRouter = {
    val router = new HttpRouter()
    router.get("/", CatHandler.getCats)
    router
  }
}
