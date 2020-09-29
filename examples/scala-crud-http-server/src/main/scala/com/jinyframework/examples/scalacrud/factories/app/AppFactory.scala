package com.jinyframework.examples.scalacrud.factories.app

import com.google.gson.Gson
import com.jinyframework.examples.scalacrud.storages.Mongo
import org.mongodb.scala.{MongoClient, MongoDatabase}

object AppFactory {
  private val gsonInstance = new Gson()
  private val mongoClientInstance: MongoClient = MongoClient()

  def getGson: Gson = gsonInstance

  def getMongoDatabase: MongoDatabase = {
    mongoClientInstance.getDatabase("scala").withCodecRegistry(Mongo.getCodec)
  }
}
