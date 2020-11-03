package com.jinyframework.examples.crud.handlers;

import com.github.jknack.handlebars.Handlebars;
import com.google.gson.Gson;
import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.examples.crud.entities.Cat;
import com.jinyframework.examples.crud.factories.AppFactory;
import com.jinyframework.examples.crud.utils.ResponseHelper;
import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.IOException;
import java.util.ArrayList;

@RequiredArgsConstructor
public class CatHandler {
    private final Gson gson = AppFactory.getGson();
    private final MongoCollection<Cat> catCollection = AppFactory.getMongoClient()
            .getDatabase("default").getCollection("cat", Cat.class);

    public HttpResponse getCats(Context ctx) {
        val catArr = new ArrayList<Cat>();
        catCollection.find().forEach(catArr::add);
        return HttpResponse.of(catArr.toArray());
    }

    public HttpResponse addCat(Context ctx) {
        val body = ctx.getBody();
        val newCat = gson.fromJson(body, Cat.class);
        catCollection.insertOne(newCat);
        return ResponseHelper.success("Done");
    }

    public HttpResponse template(Context ctx) throws IOException {
        val hb = new Handlebars();
        val template = hb.compileInline("<b>Hello {{this}}</b>");
        return HttpResponse.of(template.apply(ctx.getQuery().get("name")));
    }
}
