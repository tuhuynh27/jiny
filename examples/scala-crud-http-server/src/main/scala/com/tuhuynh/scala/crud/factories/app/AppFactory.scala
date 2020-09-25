package com.tuhuynh.scala.crud.factories.app

import com.google.gson.Gson
import com.tuhuynh.scala.crud.storages.Mongo
import org.mongodb.scala.{MongoClient, MongoDatabase}

object AppFactory {
  private val gsonInstance = new Gson()
  private val mongoClientInstance: MongoClient = MongoClient()

  def getGson: Gson = gsonInstance

  def getMongoDatabase: MongoDatabase = {
    mongoClientInstance.getDatabase("scala").withCodecRegistry(Mongo.getCodec)
  }
}
