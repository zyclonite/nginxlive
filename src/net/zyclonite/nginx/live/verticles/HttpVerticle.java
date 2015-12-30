/*
 * nginxlive
 *
 * Copyright (c) 2015   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginx.live.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import net.zyclonite.nginx.live.handler.HttpDbHandler;
import net.zyclonite.nginx.live.handler.HttpIoHandler;
import net.zyclonite.nginx.live.util.Constants;

/**
 * Created by zyclonite on 29/10/15.
 */
public class HttpVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(HttpVerticle.class);
    private HttpServer server;
    public static final String SERVICE = "http";

    @Override
    public void start(final Future<Void> fut) {
        final Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());

        router.route("/io/*").handler(SockJSHandler.create(vertx).socketHandler(new HttpIoHandler(vertx)));
        router.route("/db/*").handler(new HttpDbHandler(vertx, config()));

        router.route("/status").handler(routingContext -> {
            final HttpServerResponse response = routingContext.response();
            final Buffer buffer = Buffer.buffer();
            buffer.appendString("status");
            response.putHeader("content-type", "application/json");
            response.putHeader("content-length", String.valueOf(buffer.length()));
            response.write(buffer).end();
        });

        router.route().last().handler(StaticHandler.create());

        server = vertx.createHttpServer()
             .requestHandler(router::accept)
             .listen(config().getJsonObject(SERVICE).getInteger("port", Constants.DEFAULT_HTTP_PORT),
                     config().getJsonObject(SERVICE).getString("host", "127.0.0.1"),
                     result -> {
                         if (result.succeeded()) {
                             fut.complete();
                         } else {
                             fut.fail(result.cause());
                         }
                     }
             );
    }

    @Override
    public void stop() {
        if(server != null) {
            server.close();
        }
    }
}