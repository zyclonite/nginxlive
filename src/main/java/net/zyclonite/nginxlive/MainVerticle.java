package net.zyclonite.nginxlive;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoServiceVerticle;
import net.zyclonite.nginxlive.util.Constants;
import net.zyclonite.nginxlive.verticles.BootstrapVerticle;
import net.zyclonite.nginxlive.verticles.HttpVerticle;
import net.zyclonite.nginxlive.verticles.SyslogVerticle;

/**
 * Created by zyclonite on 06/11/2016.
 */
@SuppressWarnings("unused")
public class MainVerticle extends AbstractVerticle {
    private static Logger logger = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(final Future<Void> fut) throws Exception {
        final Future<Void> mongoService = deployVerticle(new MongoServiceVerticle(), config().getJsonObject(Constants.MONGO_SERVICE), false);
        mongoService
                .compose(f -> runVerticleJob(new BootstrapVerticle(), config()))
                .compose(f -> {
                    deployVerticle(new HttpVerticle(), config(), false);
                    deployVerticle(new SyslogVerticle(), config(), false);
                }, fut);
    }

    @Override
    public void stop(final Future<Void> fut) throws Exception {
        logger.debug("stopped");
        fut.complete();
    }

    private Future<Void> deployVerticle(final Verticle verticle, final JsonObject config, final boolean worker) {
        final Future<Void> fut = Future.future();
        vertx.deployVerticle(verticle, new DeploymentOptions().setConfig(config).setWorker(worker), res -> {
            if (res.succeeded()) {
                fut.complete();
                logger.info("Deployed " + verticle.getClass().getSimpleName() + " " + res.result());
            } else {
                fut.fail(res.cause());
                logger.error("Deployment of " + verticle.getClass().getSimpleName() + " failed! " + res.cause());
            }
        });
        return fut;
    }

    private Future<Void> runVerticleJob(final Verticle verticle, final JsonObject config) {
        final Future<Void> fut = Future.future();
        vertx.deployVerticle(verticle, new DeploymentOptions().setConfig(config), dRes -> {
            if (dRes.succeeded()) {
                final String id = dRes.result();
                logger.info("Deployed " + verticle.getClass().getSimpleName() + " " + id);
                vertx.undeploy(id, uRes -> {
                    if (uRes.succeeded()) {
                        fut.complete();
                        logger.info("Undeployed " + verticle.getClass().getSimpleName() + " " + id);
                    } else {
                        fut.fail(uRes.cause());
                        logger.error("Undeployed of " + verticle.getClass().getSimpleName() + " failed! " + uRes.cause());
                    }
                });
            } else {
                fut.fail(dRes.cause());
                logger.error("Deployment of " + verticle.getClass().getSimpleName() + " failed! " + dRes.cause());
            }
        });
        return fut;
    }
}
