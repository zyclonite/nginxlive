/*
 * nginxlive
 *
 * Copyright (c) 2015-2016   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginxlive.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoService;
import net.zyclonite.nginxlive.annotation.Queryable;
import net.zyclonite.nginxlive.model.LogLine;
import net.zyclonite.nginxlive.util.Constants;
import net.zyclonite.nginxlive.util.ModelHelper;

import java.util.List;

/**
 * Created by zyclonite on 29/10/15.
 */
public class BootstrapVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(BootstrapVerticle.class);

    @Override
    public void start(final Future<Void> fut) {
        final MongoService mongoService = MongoService.createEventBusProxy(vertx, config().getJsonObject(Constants.MONGO_SERVICE).getString("address"));
        createIndexes(mongoService, Constants.MONGO_ACCESSLOG, LogLine.class, v -> fut.complete());
    }

    private void createIndexes(final MongoService mongoService, final String collection, final Class type, final Handler<Void> handler) {
        final List<String> queryableFields = ModelHelper.getQueryableFields(type, Queryable.class);
        final JsonArray indexes = new JsonArray();
        queryableFields.forEach(field -> indexes.add(new JsonObject()
                .put("name", field + "_autoidx")
                .put("key", new JsonObject().put(field, 1))
                .put("background", true)));
        mongoService.runCommand("createIndexes", new JsonObject()
                .put("createIndexes", collection)
                .put("indexes", indexes), res -> {
            if(res.succeeded()) {
                log.debug(res.result());
            }
            handler.handle(null);
        });
    }
}
