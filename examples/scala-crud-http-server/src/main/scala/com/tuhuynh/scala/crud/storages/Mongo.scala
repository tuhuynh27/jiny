package com.tuhuynh.scala.crud.storages

import com.tuhuynh.scala.crud.entities.Mouse
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object Mongo {
  def getCodec: CodecRegistry = {
    fromRegistries(fromProviders(classOf[Mouse]), DEFAULT_CODEC_REGISTRY)
  }
}
