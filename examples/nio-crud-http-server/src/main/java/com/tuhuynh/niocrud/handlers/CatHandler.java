package com.tuhuynh.niocrud.handlers;

import com.google.gson.Gson;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.RequestContext;
import com.tuhuynh.jerrymouse.core.nio.AsyncHelper;
import com.tuhuynh.niocrud.entities.Cat;
import com.tuhuynh.niocrud.utils.ResponseHelper;
import lombok.val;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class CatHandler {
    private final Gson gson = new Gson();
    private final MongoCollection<Cat> catCollection;

    public CatHandler(MongoClient client) {
        this.catCollection = client.getDatabase("default").getCollection("cat", Cat.class);
    }

    public CompletableFuture<HttpResponse> getCats(RequestContext ctx) {
        val async = AsyncHelper.make();
        val catArr = new ArrayList<Cat>();

        catCollection.find().subscribe(new Subscriber<Cat>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1000);
            }

            @Override
            public void onNext(Cat cat) {
                catArr.add(cat);
            }

            @Override
            public void onError(Throwable t) {
                async.resolve("Error " + t.getMessage());
            }

            @Override
            public void onComplete() {
                async.resolve(gson.toJson(catArr));
            }
        });
        return async.submit();
    }

    public CompletableFuture<HttpResponse> addCat(RequestContext ctx) {
        val body = ctx.getBody();
        val newCat = gson.fromJson(body, Cat.class);
        catCollection.insertOne(newCat).subscribe(new Subscriber<InsertOneResult>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(1);
            }

            @Override
            public void onNext(InsertOneResult insertOneResult) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
        return ResponseHelper.success("Done");
    }
}
