package com.jinyframework.examples.niocrud.handlers;

import com.google.gson.Gson;
import com.jinyframework.core.RequestBinderBase.Context;
import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.examples.niocrud.entities.Cat;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import lombok.val;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

public class CatHandler {
    private final Gson gson = new Gson();
    private final MongoCollection<Cat> catCollection;

    public CatHandler(MongoDatabase mongoDatabase) {
        this.catCollection = mongoDatabase.getCollection("cat", Cat.class);
    }

    public CompletableFuture<HttpResponse> getCats(Context ctx) {
        return Mono.from(catCollection.find())
                .map(HttpResponse::of).toFuture();
    }

    public CompletableFuture<HttpResponse> addCat(Context ctx) {
        val body = ctx.getBody();
        val newCat = gson.fromJson(body, Cat.class);

        return Mono.from(catCollection.insertOne(newCat)).map(HttpResponse::of).toFuture();
    }
}
