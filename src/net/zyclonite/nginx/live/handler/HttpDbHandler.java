/*
 * nginxlive
 *
 * Copyright (c) 2015   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginx.live.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import net.zyclonite.nginx.live.util.Constants;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Created by zyclonite on 01/12/15.
 */
public class HttpDbHandler implements Handler<RoutingContext> {
    private static final Logger log = LoggerFactory.getLogger(HttpDbHandler.class);
    private static final int PAGESIZE = 100;
    private final MongoService mongoService;
    private final Router router;

    public HttpDbHandler(final Vertx vertx, final JsonObject config) {
        this.mongoService = MongoService.createEventBusProxy(vertx, config.getJsonObject(Constants.MONGO_SERVICE).getString("address"));
        this.router = Router.router(vertx);
        registerHandler();
    }

    private void registerHandler() {
        router.get("/access").handler(routingContext -> {
            final HttpServerResponse response = routingContext.response();
            final Buffer buffer = Buffer.buffer();
            response.putHeader("content-type", "application/json");
            JsonObject query = new JsonObject();
            if(routingContext.request().params().contains("query")) {
                try {
                    final String jsonquery = new String(Base64.getDecoder().decode(routingContext.request().params().get("query")));
                    query = new JsonObject(jsonquery);
                } catch (final DecodeException e) {
                }
            }
            final FindOptions options = new FindOptions();
            options.setLimit(PAGESIZE);
            options.setSort(new JsonObject().put("time", -1));
            if(routingContext.request().params().contains("page")) {
                final int page = Integer.valueOf(routingContext.request().params().get("page"));
                options.setSkip(page * PAGESIZE);
            } else {
                options.setSkip(0);
            }
            mongoService.findWithOptions(Constants.MONGO_ACCESSLOG, query, options, res -> {
                if (res.succeeded()) {
                    final JsonArray resArr = new JsonArray(res.result());
                    buffer.appendString(resArr.encode());
                } else {
                    buffer.appendString("error");
                    response.setStatusCode(500);
                    log.warn("Could not get db response " + res.cause().getMessage());
                }
                response.putHeader("content-length", String.valueOf(buffer.length()));
                response.write(buffer).end();
            });
        });

        router.get("/hits/:year/:month").handler(routingContext -> {
            final HttpServerResponse response = routingContext.response();
            final Buffer buffer = Buffer.buffer();
            response.putHeader("content-type", "application/json");
            JsonObject query = new JsonObject();
            if(routingContext.request().params().contains("query")) {
                try {
                    final String jsonquery = new String(Base64.getDecoder().decode(routingContext.request().params().get("query")));
                    query = new JsonObject(jsonquery);
                } catch (final DecodeException e) {
                }
            }
            final JsonObject command = getHitsCommand(routingContext.request().getParam("year"), routingContext.request().getParam("month"), query);
            mongoService.runCommand("aggregate", command, res -> {
                if (res.succeeded()) {
                    final JsonArray resArr = res.result().getJsonArray("result");
                    buffer.appendString(resArr.encode());
                } else {
                    buffer.appendString("error");
                    response.setStatusCode(500);
                    log.warn("Could not get db response " + res.cause().getMessage());
                }
                response.putHeader("content-length", String.valueOf(buffer.length()));
                response.write(buffer).end();
            });
        });
    }

    private static JsonObject getHitsCommand(final String yearParam, final String monthParam, final JsonObject query) throws NumberFormatException {
        final int year = Integer.valueOf(yearParam);
        final int month = Integer.valueOf(monthParam);
        if(year < 1970 && year > 3000 || month < 1 && month > 31) {
            throw new NumberFormatException("month or year out of bounds");
        }
        final ZonedDateTime time = ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        final JsonObject from = new JsonObject().put("$date", time.format(DateTimeFormatter.ISO_INSTANT));
        final JsonObject to = new JsonObject().put("$date", time.plusMonths(1).format(DateTimeFormatter.ISO_INSTANT));

        query.put("time", new JsonObject().put("$gte", from).put("$lt", to));

        final JsonArray pipeline = new JsonArray();
        pipeline.add(new JsonObject().put("$match", query));
        pipeline.add(new JsonObject().put("$project", new JsonObject()
                .put("_id", 0)
                .put("day", new JsonObject().put("$dayOfMonth", "$time"))
                .put("serverName", 1)
                .put("bytesSent", 1)
                .put("requestLength", 1)));
        pipeline.add(new JsonObject().put("$group", new JsonObject()
                .put("_id", new JsonObject().put("day", "$day").put("host", "$serverName"))
                .put("hits", new JsonObject().put("$sum", 1))
                .put("bytesSent", new JsonObject().put("$sum", "$bytesSent"))
                .put("bytesReceived", new JsonObject().put("$sum", "$requestLength"))));
        pipeline.add(new JsonObject().put("$sort", new JsonObject()
                .put("_id.day", 1)));

        final JsonObject command = new JsonObject()
                .put("aggregate", Constants.MONGO_ACCESSLOG)
                .put("pipeline", pipeline);
        return command;
    }

    @Override
    public void handle(final RoutingContext context) {
        router.handleContext(context);
    }
}
