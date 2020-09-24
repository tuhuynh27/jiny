package com.tuhuynh.crud.handlers;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.tuhuynh.crud.entities.Cat;
import com.tuhuynh.crud.utils.ResponseHelper;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.HttpResponse;
import com.tuhuynh.jerrymouse.core.RequestBinderBase.RequestContext;
import lombok.val;

import java.util.ArrayList;

public class CatHandler {
    private final Gson gson = new Gson();
    private final MongoCollection<Cat> catCollection;

    public CatHandler(MongoClient client) {
        this.catCollection = client.getDatabase("default").getCollection("cat", Cat.class);
    }

    public HttpResponse getCats(RequestContext ctx) {
        val catArr = new ArrayList<Cat>();
        catCollection.find().forEach(catArr::add);
        return HttpResponse.of(catArr.toArray()).transform(gson::toJson);
    }

    public HttpResponse addCat(RequestContext ctx) {
        val body = ctx.getBody();
        val newCat = gson.fromJson(body, Cat.class);
        catCollection.insertOne(newCat);
        return ResponseHelper.success("Done");
    }
}
