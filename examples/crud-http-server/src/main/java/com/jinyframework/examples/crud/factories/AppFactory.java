package com.jinyframework.examples.crud.factories;

import com.google.gson.Gson;
import com.jinyframework.examples.crud.storage.MongoDB;
import com.mongodb.client.MongoClient;
import lombok.Setter;

@Setter
public class AppFactory {
    private static Gson gsonInstance;
    private static MongoClient mongoClientInstance;

    public static Gson getGsonInstance() {
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
}
