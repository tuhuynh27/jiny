package com.jinyframework.examples.scalacrud.handlers

import com.jinyframework.core.RequestBinderBase.{Handler, HttpResponse}

object CatHandler {
  val getCats: Handler = _ =>
    HttpResponse.of("Cats")
}
