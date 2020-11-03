package com.jinyframework.examples.crud.handlers;

import com.google.gson.Gson;
import com.jinyframework.core.AbstractRequestBinder.Context;
import com.jinyframework.core.AbstractRequestBinder.HttpResponse;
import com.jinyframework.examples.crud.entities.Tiger;
import com.jinyframework.examples.crud.factories.AppFactory;
import com.jinyframework.examples.crud.factories.RepositoryFactory;
import com.jinyframework.examples.crud.repositories.TigerRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class TigerHandler {
    private final Gson gson = AppFactory.getGson();
    private final TigerRepository tigerRepository = RepositoryFactory.getTigerRepository();

    public HttpResponse getTigers(Context ctx) {
        return HttpResponse.of(tigerRepository.list());
    }

    public HttpResponse getTiger(Context ctx) {
        val id = Integer.parseInt(ctx.pathParam("id"));
        return HttpResponse.of(tigerRepository.find(id));
    }

    public HttpResponse addTiger(Context ctx) {
        val body = ctx.getBody();
        val newTiger = gson.fromJson(body, Tiger.class);
        val added = tigerRepository.save(newTiger);
        return HttpResponse.of(added);
    }
}
