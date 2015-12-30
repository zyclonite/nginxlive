/*
 * nginxlive
 *
 * Copyright (c) 2015   zyclonite networx
 * https://zyclonite.net
 * Lukas Prettenthaler
 */

package net.zyclonite.nginx.live;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoServiceVerticle;
import net.zyclonite.nginx.live.util.Constants;
import net.zyclonite.nginx.live.util.Filesystem;
import net.zyclonite.nginx.live.verticles.BootstrapVerticle;
import net.zyclonite.nginx.live.verticles.HttpVerticle;
import net.zyclonite.nginx.live.verticles.SyslogVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zyclonite on 29/10/15.
 */
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    protected final CountDownLatch stopLatch = new CountDownLatch(1);
    private Vertx vertx;
    private JsonObject config;

    public Application() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        log.info("application starting...");

        try {
            config = Filesystem.readConfig(Constants.DEFAULT_CONFIG);
        } catch (final DecodeException | FileNotFoundException e) {
            log.error("Error loading configuration: " + e.getMessage());
            shutdown(1);
        }

        final VertxOptions options = (new VertxOptions())
                .setWorkerPoolSize(10)
                .setBlockedThreadCheckInterval(100L);
        this.vertx = Vertx.vertx(options);

        try {
            deployVerticles();
        } catch (final InterruptedException e) {
            log.error(e.getMessage());
            shutdown(1);
        }

        addShutdownHook();
        block();
    }

    public static void main(final String[] args) {
        new Application();
    }

    public Vertx getVertx() {
        return vertx;
    }

    public JsonObject getConfig() {
        return config;
    }

    private void deployVerticles() throws InterruptedException {
        deployVerticleSync("MongoServiceVerticle", new MongoServiceVerticle(), config.getJsonObject(Constants.MONGO_SERVICE), false);
        runVerticleJob("BootstrapVerticle", new BootstrapVerticle(), config);
        deployVerticle("HttpVerticle", new HttpVerticle(), config, false);
        deployVerticle("SyslogVerticle", new SyslogVerticle(), config, false);
    }

    private void deployVerticle(final String name, final Verticle verticle, final JsonObject config, final boolean worker) {
        vertx.deployVerticle(verticle, new DeploymentOptions().setConfig(config).setWorker(worker), res -> {
            if (res.succeeded()) {
                log.info("Deployed " + name + " " + res.result());
            } else {
                log.error("Deployment of " + name + " failed! " + res.cause());
            }
        });
    }

    private void deployVerticleSync(final String name, final Verticle verticle, final JsonObject config, final boolean worker) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        vertx.deployVerticle(verticle, new DeploymentOptions().setConfig(config).setWorker(worker), res -> {
            if (res.succeeded()) {
                log.info("Deployed " + name + " " + res.result());
                latch.countDown();
            } else {
                log.error("Deployment of " + name + " failed! " + res.cause());
            }
        });
        if (!latch.await(10, TimeUnit.SECONDS)) {
            log.error("One of the required verticles could not be deployed!");
            shutdown(1);
        }
    }

    private void runVerticleJob(final String name, final Verticle verticle, final JsonObject config) {
        vertx.deployVerticle(verticle, new DeploymentOptions().setConfig(config), dRes -> {
            if (dRes.succeeded()) {
                final String id = dRes.result();
                log.info("Deployed " + name + " " + id);
                vertx.undeploy(id, uRes -> {
                    if (uRes.succeeded()) {
                        log.info("Undeployed " + name + " " + id);
                    } else {
                        log.error("Undeployed of " + name + " failed! " + uRes.cause());
                    }
                });
            } else {
                log.error("Deployment of " + name + " failed! " + dRes.cause());
            }
        });
    }

    private void block() {
        while (true) {
            try {
                stopLatch.await();
                break;
            } catch (final InterruptedException e) {
                //Ignore
            }
        }
    }

    private void unblock() {
        stopLatch.countDown();
    }

    private void shutdown(final int status) {
        System.exit(status);
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown-Hook") {
            @Override
            public void run() {
                if(vertx != null) {
                    vertx.eventBus().close(null); //to prevent new messages if hazelcast is already shutdown
                    final CountDownLatch latch = new CountDownLatch(1);
                    vertx.close(ar -> {
                        if (!ar.succeeded()) {
                            log.error("Failure in stopping Vert.x", ar.cause());
                        }
                        latch.countDown();
                    });
                    try {
                        if (!latch.await(2, TimeUnit.MINUTES)) {
                            log.error("Timed out waiting to undeploy all");
                        }
                    } catch (final InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }
                log.info("application stopped");
            }
        });
    }
}
