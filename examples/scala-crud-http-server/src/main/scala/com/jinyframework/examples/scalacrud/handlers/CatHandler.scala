package com.jinyframework.examples.scalacrud.handlers

import com.jinyframework.core.AbstractRequestBinder.{Handler, HttpResponse}

object CatHandler {
  val getCats: Handler = _ =>
    HttpResponse.of("Cats")
}
