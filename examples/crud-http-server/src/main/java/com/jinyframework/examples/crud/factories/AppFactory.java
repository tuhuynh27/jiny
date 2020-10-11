package com.jinyframework.examples.crud.factories;

import com.google.gson.Gson;
import com.jinyframework.examples.crud.storage.MongoDB;
import com.jinyframework.examples.crud.storage.MySQL;
import com.mongodb.client.MongoClient;
import lombok.Setter;

import java.sql.Connection;

@Setter
public class AppFactory {
    private static Gson gsonInstance;
    private static MongoClient mongoClientInstance;
    private static Connection sqlConnectionInstance;

    public static Gson getGson() {
        if (gsonInstance == null) {
            gsonInstance = new Gson();
        }

        return gsonInstance;
    }

    public static MongoClient getMongoClient() {
        if (mongoClientInstance == null) {
            mongoClientInstance = MongoDB.init();
        }
        return mongoClientInstance;
    }

    public static Connection getSQLConnection() {
        if (sqlConnectionInstance == null) {
            sqlConnectionInstance = MySQL.init();
        }

        return sqlConnectionInstance;
    }
}
