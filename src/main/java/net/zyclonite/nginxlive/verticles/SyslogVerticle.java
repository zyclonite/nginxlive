/*
 * nginxlive
 *
 * Copyright (c) 2015-2016   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginxlive.verticles;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.zyclonite.nginxlive.handler.SyslogMessageHandler;
import net.zyclonite.nginxlive.util.Constants;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by zyclonite on 29/10/15.
 */
public class SyslogVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(SyslogVerticle.class);
    private static final int MMDB_UPDATE_FREQUENCY = 24; //hours
    private DatagramSocket server;
    private DatabaseReader mmDBreader;
    public static final String SERVICE = "syslog";

    @Override
    public void start(final Future<Void> fut) {
        if(config().getJsonObject(Constants.MMDB_SERVICE).getBoolean("enabled", false)) {
            vertx.<Void>executeBlocking(longTask -> {
                try {
                    loadMaxMindDB();
                    longTask.complete();
                } catch (final IOException e) {
                    longTask.fail(e);
                }
            }, asyncResult -> {
                if (asyncResult.failed()) {
                    log.warn("initial load of mmDB failed " + asyncResult.cause().getMessage());
                }
            });

            vertx.setPeriodic(1000 * 60 * 60 * MMDB_UPDATE_FREQUENCY, id -> vertx.<Void>executeBlocking(longTask -> {
                try {
                    loadMaxMindDB();
                    longTask.complete();
                } catch (final IOException e) {
                    longTask.fail(e);
                }
            }, asyncResult -> {
                if (asyncResult.succeeded()) {
                    log.info("mmDB reloaded");
                } else {
                    log.warn("reload of mmDB failed " + asyncResult.cause().getMessage());
                }
            }));
        }

        server = vertx.createDatagramSocket(new DatagramSocketOptions())
                .listen(config().getJsonObject(SERVICE).getInteger("port", Constants.DEFAULT_SYSLOG_PORT),
                        config().getJsonObject(SERVICE).getString("host", "0.0.0.0"),
                        result -> {
                            if (result.succeeded()) {
                                result.result().handler(new SyslogMessageHandler(vertx, this, config()));
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

    public String getLocation(final String originatorIPAddress) {
        if(mmDBreader == null) {
            return null;
        }
        final InetAddress ipAddress;
        try {
            ipAddress = InetAddress.getByName(originatorIPAddress);
        } catch (final UnknownHostException e) {
            return null;
        }
        try {
            final CityResponse cityResponse = mmDBreader.city(ipAddress);
            final String iso2 = cityResponse.getCountry().getIsoCode();
            return iso2 + " - " + cityResponse.getCity().getName();
        } catch (final IOException e) {
            return null;
        } catch (final GeoIp2Exception e) {
            try {
                final CountryResponse countryResponse = mmDBreader.country(ipAddress);
                final String iso2 = countryResponse.getCountry().getIsoCode();
                return iso2;
            } catch (final IOException | GeoIp2Exception ex) {
                return null;
            }
        }

    }

    private void loadMaxMindDB() throws IOException {
        final File database = new File(config().getString("cityMMDB", "./GeoIP2-City.mmdb"));
        final DatabaseReader replacementReader = new DatabaseReader.Builder(database).build();
        if(mmDBreader != null) {
            mmDBreader.close();
        }
        mmDBreader = replacementReader;
    }
}
