package com.tuhuynh.jerrymouse.core.nio

import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

import com.tuhuynh.jerrymouse.core.RequestBinderBase
import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse

class AsyncHelper(val promise: CompletableFuture[HttpResponse] = new CompletableFuture[RequestBinderBase.HttpResponse]) {
  def resolve[T](t: T): Unit = {
    promise.complete(HttpResponse.of(t))
  }

  def reject[T <: Error](t: T): Unit = {
    promise.complete(HttpResponse.of(t))
  }

  def then(action: Consumer[_ >: RequestBinderBase.HttpResponse]): CompletableFuture[Void] = promise.thenAccept(action)

  def submit(): CompletableFuture[HttpResponse] = promise
}
