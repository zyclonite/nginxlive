/*
 * nginxlive
 *
 * Copyright (c) 2015-2016   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginxlive.handler;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import net.zyclonite.nginxlive.util.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zyclonite on 01/12/15.
 */
public class HttpIoHandler implements Handler<SockJSSocket> {
    private static final Logger log = LoggerFactory.getLogger(HttpIoHandler.class);
    private final EventBus eventBus;

    public HttpIoHandler(final Vertx vertx) {
        this.eventBus = vertx.eventBus();
    }

    @Override
    public void handle(final SockJSSocket sockJSSocket) {
        final Map<String, MessageConsumer<JsonObject>> subscriptions = new HashMap<>();
        final String writeHandlerId = sockJSSocket.writeHandlerID();
        log.debug("new sockjs io connection from "+sockJSSocket.remoteAddress().host());
        sockJSSocket.handler(buffer -> subscribe(subscriptions, writeHandlerId, buffer));
        sockJSSocket.endHandler(aVoid -> {
            log.debug("sockjs io end connection ("+sockJSSocket.remoteAddress().host()+")");
            subscriptions.values().forEach(MessageConsumer::unregister);
            subscriptions.clear();
        });
    }

    private void subscribe(final Map<String, MessageConsumer<JsonObject>> subscriptions, final String writeHandlerId, final Buffer message) {
        final JsonObject json = new JsonObject(message.getString(0, message.length()));
        final String name = json.getString("name");
        if(subscriptions.containsKey(name)) {
            subscriptions.remove(name).unregister();
        }
        final String type = json.getString("type");
        switch(type) {
            case "access":
                subscriptions.put(name, subscribeToLiveLogs(writeHandlerId));
                break;
            case "unsubscribe":
                subscriptions.remove(name).unregister();
                break;
            case "unsubscribeall":
                subscriptions.values().forEach(MessageConsumer::unregister);
                subscriptions.clear();
                break;
        }
    }

    private MessageConsumer<JsonObject> subscribeToLiveLogs(final String destination) {
        return eventBus.consumer(Constants.PUBLIC_ACCESSLOG, message -> eventBus.send(destination, Buffer.buffer(message.body().encode())));
    }
}
