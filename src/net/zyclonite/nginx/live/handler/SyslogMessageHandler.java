/*
 * nginxlive
 *
 * Copyright (c) 2015   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginx.live.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramPacket;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoService;
import net.zyclonite.nginx.live.model.LogLine;
import net.zyclonite.nginx.live.util.*;
import net.zyclonite.nginx.live.verticles.SyslogVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zyclonite on 27/03/15.
 */
public class SyslogMessageHandler implements Handler<DatagramPacket> {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogMessageHandler.class);
    private final ObjectMapper jsonmapper;
    private final SyslogVerticle syslogVerticle;
    private final MongoService mongoService;
    private final EventBus eventBus;
    private final Pattern utf8escapeFix = Pattern.compile("\\\\x([0-9A-F]{2})");// match \x00 to \xFF

    public SyslogMessageHandler(final Vertx vertx, final SyslogVerticle syslogVerticle, final JsonObject config) {
        this.eventBus = vertx.eventBus();
        this.syslogVerticle = syslogVerticle;
        this.mongoService = MongoService.createEventBusProxy(vertx, config.getJsonObject(Constants.MONGO_SERVICE).getString("address"));
        this.jsonmapper = new ObjectMapper();

        final SimpleModule module = new SimpleModule("SyslogNullDeserializerModule", new Version(1, 0, 0, null, null, null));
        module.addDeserializer(String.class, new SyslogNullStringDeserializer());
        module.addDeserializer(Integer.class, new SyslogNullIntegerDeserializer());
        module.addDeserializer(Long.class, new SyslogNullLongDeserializer());
        module.addDeserializer(Double.class, new SyslogNullDoubleDeserializer());
        this.jsonmapper.registerModule(module);
        this.jsonmapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void handle(final DatagramPacket packet) {
        final String[] headerbody = packet.data().toString().split("nginxjson: ");
        final String jsonLogLine = fixUTF8escape(headerbody[1]);
        LogLine line;
        try {
            line = jsonmapper.readValue(jsonLogLine, LogLine.class);
        } catch (final IOException e) {
            LOG.warn(e.getMessage(), jsonLogLine);
            return;
        }
        line.setLocation(syslogVerticle.getLocation(line.getRemoteAddr()));

        mongoService.insert(Constants.MONGO_ACCESSLOG, line.toJsonObject(), result -> {
            if(result.failed()) {
                LOG.warn("could not insert logline to database " + result.cause().getMessage());
            }
        });

        eventBus.publish(Constants.PUBLIC_ACCESSLOG, line.toJsonObject());
    }

    private String fixUTF8escape(final String input) {
        final Matcher matcher = utf8escapeFix.matcher(input);
        if(matcher.find() && matcher.groupCount() == 1) {
            return matcher.replaceAll("\\\\u00"+matcher.group(1));
        }
        return input;
    }
}
