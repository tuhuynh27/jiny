package com.jinyframework.examples.crud.handlers;

import com.github.jknack.handlebars.Handlebars;
import com.google.gson.Gson;
import com.jinyframework.examples.crud.entities.Cat;
import com.jinyframework.examples.crud.utils.ResponseHelper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.jinyframework.core.RequestBinderBase.HttpResponse;
import com.jinyframework.core.RequestBinderBase.RequestContext;
import lombok.val;

import java.io.IOException;
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
        return HttpResponse.of(catArr.toArray());
    }

    public HttpResponse addCat(RequestContext ctx) {
        val body = ctx.getBody();
        val newCat = gson.fromJson(body, Cat.class);
        catCollection.insertOne(newCat);
        return ResponseHelper.success("Done");
    }

    public HttpResponse template(RequestContext ctx) throws IOException {
        val hb = new Handlebars();
        val template = hb.compileInline("<b>Hello {{this}}</b>");
        return HttpResponse.of(template.apply(ctx.getQuery().get("name")));
    }
}
