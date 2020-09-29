package com.jinyframework.examples.scalacrud.entities

import org.mongodb.scala.bson.ObjectId

object Mouse {
  def apply(name: String, owner: String): Mouse =
    Mouse(new ObjectId(), name, owner)
}

case class Mouse(_id: ObjectId, name: String, owner: String)